package com.epsilon.frontend.ast;

import com.epsilon.frontend.Token;

public class VariableExpr extends Expr {
    public final Token name;

    public VariableExpr(Token name) {
        this.name = name;
    }

    public String toString() {
        return name.lexeme;
    }
}
