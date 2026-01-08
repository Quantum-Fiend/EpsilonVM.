package com.epsilon.frontend.ast;

import com.epsilon.frontend.Token;

public class ReturnStmt extends Stmt {
    public final Token keyword;
    public final Expr value;

    public ReturnStmt(Token keyword, Expr value) {
        this.keyword = keyword;
        this.value = value;
    }

    public String toString() {
        if (value == null)
            return "return;";
        return "return " + value + ";";
    }
}
