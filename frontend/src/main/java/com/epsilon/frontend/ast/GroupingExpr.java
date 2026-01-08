package com.epsilon.frontend.ast;

public class GroupingExpr extends Expr {
    public final Expr expression;

    public GroupingExpr(Expr expression) {
        this.expression = expression;
    }

    public String toString() {
        return "(group " + expression + ")";
    }
}
