package com.epsilon.frontend.ast;

public class LiteralExpr extends Expr {
    public final Object value;

    public LiteralExpr(Object value) {
        this.value = value;
    }

    public String toString() {
        return String.valueOf(value);
    }
}
