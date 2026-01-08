# EpsilonVM Memory Model

## 1. Stack & Registers
EpsilonVM uses a **Register-Based Architecture**, unlike the stack-based JVM.
- Each stack frame allocates a sliding window of virtual registers (`r0` - `r255`).
- Instructions operate directly on these registers, reducing the number of `PUSH`/`POP` operations.

## 2. Heap Layout
The Heap is managed by a Mark-Sweep Garbage Collector.

### Object Header (`Obj`)
Every heap-allocated object starts with a common header:
```c
struct Obj {
    ObjType type;    // INT, STRING, FUNCTION...
    bool is_marked;  // GC Mark Bit
    Obj* next;       // Intrusive Linked List for tracking
};
```

### String Interning
Strings are currently allocated as `ObjString`. Optimizations for string interning (deduplication) are planned for v2.

## 3. Garbage Collection
Algorithm: **Stop-the-World Mark-Sweep**.

1.  **Mark Phase**: 
    - Start from Roots (VM Stack, Registers, Global Variables).
    - Traverse object graph (Gray/Black worklist).
    - Set `is_marked = true`.

2.  **Sweep Phase**:
    - Iterate through the global `vm->objects` linked list.
    - If `is_marked` is false, `free()` the memory and remove from list.
    - If `is_marked` is true, reset bit for next cycle.

## 4. Native Interface (FFI)
Native functions are wrapped in `ObjNative`. They reside on the heap but contain function pointers to C-land code.
