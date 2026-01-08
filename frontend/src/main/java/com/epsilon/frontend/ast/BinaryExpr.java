package com.epsilon.frontend.ast;

import com.epsilon.frontend.Token;

public class BinaryExpr extends Expr {
    public final Expr left;
    public final Token operator;
    public final Expr right;

    public BinaryExpr(Expr left, Token operator, Expr right) {
        this.left = left;
        this.operator = operator;
        this.right = right;
    }

    public String toString() {
        return "(" + left + " " + operator.lexeme + " " + right + ")";
    }
}
