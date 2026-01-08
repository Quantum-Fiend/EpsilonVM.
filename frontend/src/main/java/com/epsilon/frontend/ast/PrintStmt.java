package com.epsilon.frontend.ast;

public class PrintStmt extends Stmt {
    public final Expr expression;

    public PrintStmt(Expr expression) {
        this.expression = expression;
    }

    public String toString() {
        return "print " + expression + ";";
    }
}
