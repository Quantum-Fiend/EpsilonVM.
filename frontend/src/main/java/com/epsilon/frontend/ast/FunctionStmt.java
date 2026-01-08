package com.epsilon.frontend.ast;

import com.epsilon.frontend.Token;
import java.util.List;

public class FunctionStmt extends Stmt {
    public final Token name;
    public final List<Token> params;
    public final List<Stmt> body;

    public FunctionStmt(Token name, List<Token> params, List<Stmt> body) {
        this.name = name;
        this.params = params;
        this.body = body;
    }

    public String toString() {
        return "fn " + name.lexeme + "(...) {...}";
    }
}
