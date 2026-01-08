package com.epsilon.frontend.ast;

public class ExpressionStmt extends Stmt {
    public final Expr expression;

    public ExpressionStmt(Expr expression) {
        this.expression = expression;
    }

    public String toString() {
        return expression + ";";
    }
}
