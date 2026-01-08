package com.epsilon.frontend;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Compiler {
    private final Chunk chunk;
    private int registerCount = 0;
    private final Map<String, Integer> variables = new HashMap<>();

    public Compiler() {
        this.chunk = new Chunk();
    }

    public Chunk compile(List<Stmt> statements) {
        for (Stmt stmt : statements) {
            compile(stmt);
        }
        chunk.write(Opcode.HALT, (byte) 0, (byte) 0, (byte) 0);
        return chunk;
    }

    private int evaluate(Expr expr, int targetReg) {
        if (expr instanceof LiteralExpr) {
            LiteralExpr e = (LiteralExpr) expr;
            int k = chunk.addConstant(e.value);
            // LOADK target, k
            chunk.write(Opcode.LOADK, (byte) targetReg, (byte) (k >> 8), (byte) (k & 0xFF));
            return targetReg;
        } else if (expr instanceof VariableExpr) {
            VariableExpr e = (VariableExpr) expr;
            if (variables.containsKey(e.name.lexeme)) {
                return variables.get(e.name.lexeme);
            }
            throw new RuntimeException("Undefined variable: " + e.name.lexeme);
        } else if (expr instanceof BinaryExpr) {
            BinaryExpr e = (BinaryExpr) expr;

            // Constant Folding Optimization
            if (e.left instanceof LiteralExpr && e.right instanceof LiteralExpr) {
                Object leftVal = ((LiteralExpr) e.left).value;
                Object rightVal = ((LiteralExpr) e.right).value;

                if (leftVal instanceof Number && rightVal instanceof Number) {
                    double l = ((Number) leftVal).doubleValue();
                    double r = ((Number) rightVal).doubleValue();
                    Object result = null;
                    switch (e.operator.type) {
                        case PLUS:
                            result = l + r;
                            break;
                        case MINUS:
                            result = l - r;
                            break;
                        case STAR:
                            result = l * r;
                            break;
                        case SLASH:
                            result = l / r;
                            break;
                        default:
                            break;
                    }
                    if (result != null) {
                        int k = chunk.addConstant(result);
                        chunk.write(Opcode.LOADK, (byte) targetReg, (byte) (k >> 8), (byte) (k & 0xFF));
                        return targetReg;
                    }
                }
            }

            int left = evaluate(e.left, targetReg);
            int rightReg = allocReg(); // Need a specific new reg for right operand
            int right = evaluate(e.right, rightReg);

            byte op = 0;
            switch (e.operator.type) {
                case PLUS:
                    op = Opcode.ADD;
                    break;
                case MINUS:
                    op = Opcode.SUB;
                    break;
                case STAR:
                    op = Opcode.MUL;
                    break;
                case SLASH:
                    op = Opcode.DIV;
                    break;
                case EQUAL_EQUAL:
                    op = Opcode.COMPARE;
                    break; // Needs subtype in VM or expansion
                case LESS:
                    op = Opcode.COMPARE;
                    break;
                case GREATER:
                    op = Opcode.COMPARE;
                    break;
                default:
                    throw new RuntimeException("Unknown operator type: " + e.operator.type);
            }

            chunk.write(op, (byte) targetReg, (byte) left, (byte) right);
            freeReg(); // Free rightReg
            return targetReg;
        }
        return targetReg;
    }

    private void compile(Stmt stmt) {
        if (stmt instanceof PrintStmt) {
            int reg = evaluate(((PrintStmt) stmt).expression, allocReg());
            chunk.write(Opcode.PRINT, (byte) reg, (byte) 0, (byte) 0);
            freeReg();
        } else if (stmt instanceof VarDeclStmt) {
            VarDeclStmt v = (VarDeclStmt) stmt;
            int reg = allocReg(); // Variables are assigned to registers
            variables.put(v.name.lexeme, reg);
            if (v.initializer != null) {
                evaluate(v.initializer, reg);
            }
        } else if (stmt instanceof ExpressionStmt) {
            int reg = allocReg();
            evaluate(((ExpressionStmt) stmt).expression, reg);
            freeReg();
        } else if (stmt instanceof BlockStmt) {
            for (Stmt s : ((BlockStmt) stmt).statements) {
                compile(s);
            }
        } else if (stmt instanceof IfStmt) {
            IfStmt i = (IfStmt) stmt;
            int condReg = evaluate(i.condition, allocReg());

            int jumpIfFalse = chunk.write(Opcode.JMP_IF_NOT, (byte) condReg, (byte) 0, (byte) 0);
            freeReg();

            compile(i.thenBranch);

            if (i.elseBranch != null) {
                int jumpAfter = chunk.write(Opcode.JMP, (byte) 0, (byte) 0, (byte) 0);
                // Patch jumpIfFalse
                int current = chunk.count();
                chunk.patch(jumpIfFalse, (short) (current - jumpIfFalse));

                compile(i.elseBranch);

                // Patch jumpAfter
                int after = chunk.count();
                chunk.patch(jumpAfter, (short) (after - jumpAfter));
            } else {
                int current = chunk.count();
                chunk.patch(jumpIfFalse, (short) (current - jumpIfFalse));
            }
        } else if (stmt instanceof WhileStmt) {
            WhileStmt w = (WhileStmt) stmt;
            int start = chunk.count();
            int condReg = evaluate(w.condition, allocReg());
            int jumpExit = chunk.write(Opcode.JMP_IF_NOT, (byte) condReg, (byte) 0, (byte) 0);
            freeReg();

            compile(w.body);

            int jumpBack = chunk.write(Opcode.JMP, (byte) 0, (byte) 0, (byte) 0);
            chunk.patch(jumpBack, (short) (start - jumpBack));

            int exit = chunk.count();
            chunk.patch(jumpExit, (short) (exit - jumpExit));
        }
    }

    // Very simple allocator
    private int allocReg() {
        return registerCount++;
    }

    private void freeReg() {
        registerCount--; // Only works for stack-like usage in expression evaluation
    }
}
