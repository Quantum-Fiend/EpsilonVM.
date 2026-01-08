package com.epsilon.frontend;

public class Opcode {
    public static final byte HALT = 0x00;
    public static final byte LOADK = 0x01; // rA = Const[k]
    public static final byte LOAD = 0x02; // rA = rB

    // Arithmetic
    public static final byte ADD = 0x10; // rA = rB + rC
    public static final byte SUB = 0x11;
    public static final byte MUL = 0x12;
    public static final byte DIV = 0x13;

    // Control Flow
    public static final byte JMP = 0x20; // pc += offset
    public static final byte JMP_IF = 0x21; // if (rA) pc += offset
    public static final byte JMP_IF_NOT = 0x22;// if (!rA) pc += offset

    public static final byte COMPARE = 0x23; // rA = (rB op rC)

    // Functions
    public static final byte CALL = 0x30;
    public static final byte RET = 0x31;

    // System
    public static final byte PRINT = 0x40; // print rA
}
