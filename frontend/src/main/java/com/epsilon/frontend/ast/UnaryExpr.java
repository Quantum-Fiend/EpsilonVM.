package com.epsilon.frontend.ast;

import com.epsilon.frontend.Token;

public class UnaryExpr extends Expr {
    public final Token operator;
    public final Expr right;

    public UnaryExpr(Token operator, Expr right) {
        this.operator = operator;
        this.right = right;
    }

    public String toString() {
        return "(" + operator.lexeme + right + ")";
    }
}
