package com.epsilon.frontend.ast;

import com.epsilon.frontend.Token;

public class VarDeclStmt extends Stmt {
    public final Token name;
    public final Expr initializer;

    public VarDeclStmt(Token name, Expr initializer) {
        this.name = name;
        this.initializer = initializer;
    }

    public String toString() {
        return "var " + name.lexeme + " = " + initializer + ";";
    }
}
