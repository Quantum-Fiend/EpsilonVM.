#ifndef VM_H
#define VM_H

#include "common.h"

#define MAX_REGISTERS 256
#define MAX_CONSTANTS 256

typedef enum {
    VAL_INT,
    VAL_FLOAT,
    // VAL_STRING, // Replaced by OBJ_STRING
    VAL_OBJ
} ValueType;

typedef struct Obj Obj;

struct Obj {
    ObjType type;
    bool is_marked; // For GC
    Obj* next;      // For linked list of all objects
};

typedef struct {
    Obj obj;
    char* chars;
    int length;
} ObjString;

typedef Value (*NativeFn)(int argCount, Value* args);

typedef struct {
    Obj obj;
    NativeFn function;
    char* name; // Debug name
} ObjNative;

typedef struct {
    ValueType type;
    union {
        i64 integer;
        f64 floating;
        Obj* obj;
    } as;
} Value;

#define IS_OBJ(value) ((value).type == VAL_OBJ)
#define AS_OBJ(value) ((value).as.obj)
#define OBJ_VAL(object) ((Value){VAL_OBJ, {.obj = (Obj*)(object)}})
#define OBJ_TYPE(value) (AS_OBJ(value)->type)

typedef struct {
    u32* code;
    int count;
    Value constants[MAX_CONSTANTS];
    int constants_count;
} Chunk;

typedef struct {
    Chunk* chunk;
    u32 pc;
    Value registers[MAX_REGISTERS];
    bool is_running;
    
    Obj* objects;
    int grayCount;
    int grayCapacity;
    Obj** grayStack;

    // Memory tracking for GC
    size_t bytesAllocated;
    size_t nextGC;
} VM;

void defineNative(const char* name, NativeFn function);


void initVM(VM* vm);
void freeVM(VM* vm);
void interpret(VM* vm, Chunk* chunk);
void loadFile(const char* path, Chunk* chunk, VM* vm);

// Memory Management
void* reallocate(void* pointer, size_t oldSize, size_t newSize, VM* vm);
Obj* allocateObject(size_t size, ObjType type, VM* vm);
void collectGarbage(VM* vm);


#endif
