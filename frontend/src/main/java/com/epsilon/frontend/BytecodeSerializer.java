package com.epsilon.frontend;

import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class BytecodeSerializer {
    private static final int MAGIC = 0x45564D00; // "EVM\0"
    private static final int VERSION = 1;

    public void serialize(Chunk chunk, String filename) throws IOException {
        try (DataOutputStream out = new DataOutputStream(new FileOutputStream(filename))) {
            // Header
            out.writeInt(MAGIC);
            out.writeInt(VERSION);

            // Constant Pool
            out.writeInt(chunk.constants.size());
            for (Object constant : chunk.constants) {
                if (constant instanceof Integer) {
                    out.writeByte(1); // Type 1: Int
                    out.writeLong((Integer) constant); // Write as 64-bit for VM compatibility
                } else if (constant instanceof Double) {
                    out.writeByte(2); // Type 2: Float
                    out.writeDouble((Double) constant);
                } else if (constant instanceof String) {
                    out.writeByte(3); // Type 3: String
                    byte[] bytes = ((String) constant).getBytes(StandardCharsets.UTF_8);
                    out.writeInt(bytes.length);
                    out.write(bytes);
                } else {
                    throw new RuntimeException("Unknown constant type: " + constant.getClass());
                }
            }

            // Instructions (Main Function Body for now)
            // In the spec, we have [Functions] section. For v1, let's treat top-level as
            // function 0.
            // Or simplified: Just code dump for now if we haven't implemented full Function
            // Table in Compiler.
            // Let's stick to the spec's "Functions" structure to be professional.

            // Functions Count (1: Main)
            out.writeInt(1);

            // Function 0 (Main)
            out.writeInt(0); // Name Index (0 placeholder or add "main" to const pool)
            out.writeByte(0); // Arg Count
            out.writeByte(255); // Register Count (Max for now)
            out.writeInt(chunk.code.size()); // Instruction Count (number of ints)

            for (int instr : chunk.code) {
                out.writeInt(instr);
            }
        }
    }
}
