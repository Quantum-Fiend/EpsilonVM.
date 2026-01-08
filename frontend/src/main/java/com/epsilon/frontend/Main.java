package com.epsilon.frontend;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class Main {
    public static void main(String[] args) throws IOException {
        String source;
        boolean checkOnly = false;
        String filePath = null;

        for (String arg : args) {
            if (arg.equals("--check"))
                checkOnly = true;
            else
                filePath = arg;
        }

        if (filePath != null) {
            source = new String(Files.readAllBytes(Paths.get(filePath)));
        } else {
            source = "var x = 10; var y = 20; print x + y; fn add(a, b) { return a + b; }";
        }

        if (!checkOnly) {
            System.out.println("Compiling Source:\n" + source + "\n");
        }

        Lexer lexer = new Lexer(source);
        List<Token> tokens = lexer.scanTokens();

        if (!checkOnly) {
            System.out.println("Tokens:");
            for (Token t : tokens)
                System.out.println(t);
        }

        Parser parser = new Parser(tokens);
        List<Stmt> ast;
        try {
            ast = parser.parse();
        } catch (Exception e) {
            if (checkOnly)
                System.exit(1);
            throw e;
        }

        if (checkOnly)
            return;

        System.out.println("\nAST:");
        for (Stmt stmt : ast) {
            System.out.println(stmt);
        }

        System.out.println("\n----------------- Compilation -----------------\n");
        Compiler compiler = new Compiler();
        Chunk chunk = compiler.compile(ast);
        System.out.println(chunk.disassemble());

        System.out.println("Writing to program.evm...");
        BytecodeSerializer serializer = new BytecodeSerializer();
        try {
            serializer.serialize(chunk, "program.evm");
            System.out.println("Successfully wrote program.evm");
        } catch (IOException e) {
            System.err.println("Failed to write bytecode file: " + e.getMessage());
        }
    }
}
