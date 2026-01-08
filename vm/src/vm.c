#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include "vm.h"
#include "opcode.h"

#include <time.h>

// Native Functions
Value nativeClock(int argCount, Value* args) {
    return (Value){VAL_FLOAT, .as.floating = (double)clock() / CLOCKS_PER_SEC};
}

void defineNative(VM* vm, const char* name, NativeFn function) {
    // For this prototype, we'll simple add it to a global list or just pre-define ID mapping.
    // Ideally, we push to stack, allocate ObjNative, and store in a global hash map.
    // Here we will just hardcode "clock" -> ID 0 logic in interpreter if call is made 
    // OR we allocate ObjNative and assume lookup happens.
    
    // Proper way: Allocate Native Object.
    Obj* obj = allocateObject(sizeof(ObjNative), OBJ_NATIVE, vm);
    ObjNative* native = (ObjNative*)obj;
    native->function = function;
    native->name = strdup(name);
    // Store in global table (omitted for simplicity, just demonstrating creation)
}

void initVM(VM* vm) {
    vm->pc = 0;
    vm->is_running = false;
    vm->objects = NULL;
    vm->grayCount = 0;
    vm->grayCapacity = 0;
    vm->grayStack = NULL;
    memset(vm->registers, 0, sizeof(vm->registers));
    
    // Register Natives
    defineNative(vm, "clock", nativeClock);
}

void freeObject(Obj* object) {
    switch (object->type) {
        case OBJ_STRING: {
            ObjString* string = (ObjString*)object;
            free(string->chars);
            free(string);
            break;
        }
        // Handle others
    }
}

void freeVM(VM* vm) {
    Obj* object = vm->objects;
    while (object != NULL) {
        Obj* next = object->next;
        freeObject(object);
        object = next;
    }
    free(vm->grayStack);
}

void* reallocate(void* pointer, size_t oldSize, size_t newSize, VM* vm);

Obj* allocateObject(size_t size, ObjType type, VM* vm) {
    Obj* object = (Obj*)reallocate(NULL, 0, size, vm);
    object->type = type;
    object->is_marked = false;
    
    object->next = vm->objects;
    vm->objects = object;
    return object;
}

void markObject(VM* vm, Obj* object) {
    if (object == NULL) return;
    if (object->is_marked) return;

    #ifdef DEBUG_LOG_GC
    printf("%p mark ", (void*)object);
    printValue(OBJ_VAL(object));
    printf("\n");
    #endif

    object->is_marked = true;

    if (vm->grayCapacity < vm->grayCount + 1) {
        vm->grayCapacity = (vm->grayCapacity < 8) ? 8 : vm->grayCapacity * 2;
        vm->grayStack = (Obj**)realloc(vm->grayStack, sizeof(Obj*) * vm->grayCapacity);
    }

    vm->grayStack[vm->grayCount++] = object;
}

void markValue(VM* vm, Value value) {
    if (value.type == VAL_OBJ) markObject(vm, value.as.obj);
}

static void markRoots(VM* vm) {
    for (int i = 0; i < MAX_REGISTERS; i++) {
        markValue(vm, vm->registers[i]);
    }
}

static void blackenObject(VM* vm, Obj* object) {
    #ifdef DEBUG_LOG_GC
    printf("%p blacken ", (void*)object);
    printValue(OBJ_VAL(object));
    printf("\n");
    #endif

    switch (object->type) {
        case OBJ_NATIVE:
        case OBJ_STRING:
            break;
        case OBJ_FUNCTION:
            // Function marking logic (if added)
            break;
        default: break;
    }
}

static void traceReferences(VM* vm) {
    while (vm->grayCount > 0) {
        Obj* object = vm->grayStack[--vm->grayCount];
        blackenObject(vm, object);
    }
}

static void sweep(VM* vm) {
    Obj* previous = NULL;
    Obj* object = vm->objects;
    while (object != NULL) {
        if (object->is_marked) {
            object->is_marked = false;
            previous = object;
            object = object->next;
        } else {
            Obj* unreached = object;
            object = object->next;
            if (previous != NULL) {
                previous->next = object;
            } else {
                vm->objects = object;
            }

            freeObject(unreached);
        }
    }
}

void collectGarbage(VM* vm) {
    #ifdef DEBUG_LOG_GC
    printf("-- GC Begin --\n");
    size_t before = vm->bytesAllocated;
    #endif

    markRoots(vm);
    traceReferences(vm);
    sweep(vm);

    vm->nextGC = vm->bytesAllocated * 2;

    #ifdef DEBUG_LOG_GC
    printf("-- GC End --\n");
    printf("   collected %zu bytes (from %zu to %zu) next at %zu\n",
           before - vm->bytesAllocated, before, vm->bytesAllocated, vm->nextGC);
    #endif
}

void* reallocate(void* pointer, size_t oldSize, size_t newSize, VM* vm) {
    vm->bytesAllocated += newSize - oldSize;
    if (newSize > oldSize) {
        #ifdef DEBUG_STRESS_GC
        collectGarbage(vm);
        #endif

        if (vm->bytesAllocated > vm->nextGC) {
            collectGarbage(vm);
        }
    }

    if (newSize == 0) {
        free(pointer);
        return NULL;
    }
    return realloc(pointer, newSize);
}

static void printValue(Value v) {
    switch (v.type) {
        case VAL_INT: printf("%lld", v.as.integer); break;
        case VAL_FLOAT: printf("%f", v.as.floating); break;
        case VAL_OBJ: {
             Obj* obj = v.as.obj;
             if (obj->type == OBJ_STRING) {
                 printf("%s", ((ObjString*)obj)->chars);
             } else {
                 printf("<obj>");
             }
             break;
        }
    }
}

void interpret(VM* vm, Chunk* chunk) {
    vm->chunk = chunk;
    vm->pc = 0;
    vm->is_running = true;
    
    while (vm->is_running && vm->pc < chunk->count) {
        u32 instruction = chunk->code[vm->pc++];
        u8 op = (instruction >> 24) & 0xFF;
        u8 rA = (instruction >> 16) & 0xFF;
        u8 rB = (instruction >> 8) & 0xFF;
        u8 rC = instruction & 0xFF;
        u16 immD = instruction & 0xFFFF; // For instructions using AD format
        
        #ifdef DEBUG_TRACE_EXECUTION
        printf("PC: %04d | OP: %02X | A: %d | B: %d | C: %d\n", vm->pc-1, op, rA, rB, rC);
        #endif

        switch (op) {
            case OP_HALT:
                vm->is_running = false;
                break;
                
            case OP_LOADK: {
                // LOADK rA, k (using immD)
                u16 k = immD;
                if (k < chunk->constants_count) {
                    vm->registers[rA] = chunk->constants[k];
                } else {
                    printf("Runtime Error: Constant index out of bounds\n");
                    vm->is_running = false;
                }
                break;
            }
            
            case OP_ADD: {
                Value b = vm->registers[rB];
                Value c = vm->registers[rC];
                if (b.type == VAL_INT && c.type == VAL_INT) {
                    vm->registers[rA] = (Value){VAL_INT, .as.integer = b.as.integer + c.as.integer};
                } else if (b.type == VAL_FLOAT || c.type == VAL_FLOAT) {
                    f64 valB = (b.type == VAL_INT) ? (f64)b.as.integer : b.as.floating;
                    f64 valC = (c.type == VAL_INT) ? (f64)c.as.integer : c.as.floating;
                    vm->registers[rA] = (Value){VAL_FLOAT, .as.floating = valB + valC};
                }
                break;
            }
            case OP_SUB: {
                Value b = vm->registers[rB];
                Value c = vm->registers[rC];
                vm->registers[rA] = (Value){VAL_INT, .as.integer = b.as.integer - c.as.integer};
                break;
            }
            case OP_MUL: {
                Value b = vm->registers[rB];
                Value c = vm->registers[rC];
                vm->registers[rA] = (Value){VAL_INT, .as.integer = b.as.integer * c.as.integer};
                break;
            }
            case OP_JMP: {
                vm->pc += (i16)immD;
                break;
            }
            case OP_JMP_IF_NOT: {
                Value cond = vm->registers[rA];
                bool isTrue = (cond.type == VAL_INT) ? cond.as.integer != 0 : cond.as.floating != 0.0;
                if (!isTrue) vm->pc += (i16)immD;
                break;
            }
            case OP_COMPARE: {
                // For this prototype, we'll assume EQUAL if A=0, LESS if A=1, etc.
                // But let's just do a simple EQUAL for now or use the high bits of opcode
                // Better: define OP_EQ, OP_LT, OP_LE etc.
                // For now, let's assume it's just 'less than' if we only need that for fib.
                Value b = vm->registers[rB];
                Value c = vm->registers[rC];
                vm->registers[rA] = (Value){VAL_INT, .as.integer = b.as.integer < c.as.integer};
                break;
            }
            case OP_PRINT:
                printValue(vm->registers[rA]);
                printf("\n");
                break;
                
            default:
                printf("Runtime Error: Unknown Opcode %02X\n", op);
                vm->is_running = false;
                break;
        }
    }
}

// File Loader
// File loader implementation moved below to support read helpers
static u32 readInt(FILE* file) {
    u8 bytes[4];
    fread(bytes, 1, 4, file);
    return (bytes[0] << 24) | (bytes[1] << 16) | (bytes[2] << 8) | bytes[3];
}

static i64 readLong(FILE* file) {
    u8 bytes[8];
    fread(bytes, 1, 8, file);
    return ((i64)bytes[0] << 56) | ((i64)bytes[1] << 48) | ((i64)bytes[2] << 40) | ((i64)bytes[3] << 32) |
           ((i64)bytes[4] << 24) | ((i64)bytes[5] << 16) | ((i64)bytes[6] << 8) | (i64)bytes[7];
}

void loadFile(const char* path, Chunk* chunk, VM* vm) {
    FILE* file = fopen(path, "rb");
    if (!file) {
        fprintf(stderr, "Could not open file '%s'\n", path);
        exit(1);
    }
    
    u32 magic = readInt(file);
    u32 version = readInt(file);
    
    if (magic != 0x45564D00) {
         fprintf(stderr, "Invalid magic number: %X\n", magic);
    }
    
    // Constants
    u32 poolSize = readInt(file);
    chunk->constants_count = poolSize;
    for (u32 i = 0; i < poolSize; i++) {
        u8 type;
        fread(&type, 1, 1, file);
        
        if (type == 1) { // Int
             chunk->constants[i].type = VAL_INT;
             chunk->constants[i].as.integer = readLong(file);
        } else if (type == 3) { // String
             u32 len = readInt(file);
             ObjString* string = (ObjString*)allocateObject(sizeof(ObjString), OBJ_STRING, vm);
             string->chars = malloc(len + 1);
             string->length = len;
             fread(string->chars, 1, len, file);
             string->chars[len] = '\0';
             chunk->constants[i].type = VAL_OBJ;
             chunk->constants[i].as.obj = (Obj*)string;
        }
    }
    
    // Functions
    readInt(file); // Func count
    readInt(file); // Name index
    fgetc(file); // Arg count
    fgetc(file); // Reg count
    u32 instrCount = readInt(file);
    
    chunk->count = instrCount;
    chunk->code = malloc(sizeof(u32) * instrCount);
    for (u32 i = 0; i < instrCount; i++) {
        chunk->code[i] = readInt(file);
    }
    
    fclose(file);
}
