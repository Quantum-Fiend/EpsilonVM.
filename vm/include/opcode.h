#ifndef OPCODE_H
#define OPCODE_H

typedef enum {
    OP_HALT = 0x00,
    OP_LOADK = 0x01,
    OP_LOAD = 0x02,
    
    OP_ADD = 0x10,
    OP_SUB = 0x11,
    OP_MUL = 0x12,
    OP_DIV = 0x13,
    
    OP_JMP = 0x20,
    OP_JMP_IF = 0x21,
    OP_JMP_IF_NOT = 0x22,
    OP_COMPARE = 0x23,
    
    OP_CALL = 0x30,
    OP_RET = 0x31,
    
    OP_PRINT = 0x40
} Opcode;

#endif
