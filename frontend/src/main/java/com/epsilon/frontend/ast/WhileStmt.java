package com.epsilon.frontend.ast;

public class WhileStmt extends Stmt {
    public final Expr condition;
    public final Stmt body;

    public WhileStmt(Expr condition, Stmt body) {
        this.condition = condition;
        this.body = body;
    }

    public String toString() {
        return "while (" + condition + ") " + body;
    }
}
