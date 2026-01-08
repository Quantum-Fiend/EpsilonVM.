package com.epsilon.frontend.ast;

import java.util.List;

public class BlockStmt extends Stmt {
    public final List<Stmt> statements;

    public BlockStmt(List<Stmt> statements) {
        this.statements = statements;
    }

    public String toString() {
        return "{ ... }";
    }
}
