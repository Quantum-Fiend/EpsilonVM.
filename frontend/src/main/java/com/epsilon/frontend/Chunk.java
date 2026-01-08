package com.epsilon.frontend;

import java.util.ArrayList;
import java.util.List;

public class Chunk {
    public final List<Integer> code = new ArrayList<>();
    public final List<Object> constants = new ArrayList<>();

    public int write(byte opcode, byte a, byte b, byte c) {
        int instruction = (opcode & 0xFF) << 24 | (a & 0xFF) << 16 | (b & 0xFF) << 8 | (c & 0xFF);
        code.add(instruction);
        return code.size() - 1;
    }

    public void patch(int index, short offset) {
        int instr = code.get(index);
        instr = (instr & 0xFFFF0000) | (offset & 0xFFFF);
        code.set(index, instr);
    }

    public int count() {
        return code.size();
    }

    public int addConstant(Object value) {
        constants.add(value);
        return constants.size() - 1;
    }

    public String disassemble() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < code.size(); i++) {
            int instr = code.get(i);
            int op = (instr >> 24) & 0xFF;
            int a = (instr >> 16) & 0xFF;
            int b = (instr >> 8) & 0xFF;
            int c = instr & 0xFF;

            sb.append(String.format("%04d  ", i));
            switch (op) {
                case Opcode.HALT:
                    sb.append("HALT");
                    break;
                case Opcode.LOADK:
                    sb.append(String.format("LOADK r%d, %d ('%s')", a, (b << 8) | c, constants.get((b << 8) | c)));
                    break;
                case Opcode.LOAD:
                    sb.append(String.format("LOAD r%d, r%d", a, b));
                    break;
                case Opcode.ADD:
                    sb.append(String.format("ADD r%d, r%d, r%d", a, b, c));
                    break;
                case Opcode.SUB:
                    sb.append(String.format("SUB r%d, r%d, r%d", a, b, c));
                    break;
                case Opcode.MUL:
                    sb.append(String.format("MUL r%d, r%d, r%d", a, b, c));
                    break;
                case Opcode.DIV:
                    sb.append(String.format("DIV r%d, r%d, r%d", a, b, c));
                    break;
                case Opcode.PRINT:
                    sb.append(String.format("PRINT r%d", a));
                    break;
                case Opcode.JMP:
                    sb.append(String.format("JMP %d", (short) (instr & 0xFFFF)));
                    break;
                case Opcode.JMP_IF:
                    sb.append(String.format("JMP_IF r%d, %d", a, (short) (instr & 0xFFFF)));
                    break;
                default:
                    sb.append(String.format("UNKNOWN %d", op));
            }
            sb.append("\n");
        }
        return sb.toString();
    }
}
