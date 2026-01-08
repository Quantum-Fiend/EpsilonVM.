package com.epsilon.frontend.ast;

public class IfStmt extends Stmt {
    public final Expr condition;
    public final Stmt thenBranch;
    public final Stmt elseBranch;

    public IfStmt(Expr condition, Stmt thenBranch, Stmt elseBranch) {
        this.condition = condition;
        this.thenBranch = thenBranch;
        this.elseBranch = elseBranch;
    }

    public String toString() {
        if (elseBranch == null) {
            return "if (" + condition + ") " + thenBranch;
        }
        return "if (" + condition + ") " + thenBranch + " else " + elseBranch;
    }
}
