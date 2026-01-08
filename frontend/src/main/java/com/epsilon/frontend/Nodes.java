package com.epsilon.frontend;

import java.util.List;

abstract class ASTNode {
    public abstract String toString();
}

abstract class Expr extends ASTNode {}
abstract class Stmt extends ASTNode {}

// Expressions
class BinaryExpr extends Expr {
    public final Expr left;
    public final Token operator;
    public final Expr right;
    public BinaryExpr(Expr left, Token operator, Expr right) {
        this.left = left; this.operator = operator; this.right = right;
    }
    public String toString() { return "(" + left + " " + operator.lexeme + " " + right + ")"; }
}

class LiteralExpr extends Expr {
    public final Object value;
    public LiteralExpr(Object value) { this.value = value; }
    public String toString() { return String.valueOf(value); }
}

class VariableExpr extends Expr {
    public final Token name;
    public VariableExpr(Token name) { this.name = name; }
    public String toString() { return name.lexeme; }
}

// Statements
class VarDeclStmt extends Stmt {
    public final Token name;
    public final Expr initializer;
    public VarDeclStmt(Token name, Expr initializer) {
        this.name = name; this.initializer = initializer;
    }
    public String toString() { return "var " + name.lexeme + " = " + initializer + ";"; }
}

class PrintStmt extends Stmt {
    public final Expr expression;
    public PrintStmt(Expr expression) { this.expression = expression; }
    public String toString() { return "print " + expression + ";"; }
}

class FunctionStmt extends Stmt {
    public final Token name;
    public final List<Token> params;
    public final List<Stmt> body;
    public FunctionStmt(Token name, List<Token> params, List<Stmt> body) {
        this.name = name; this.params = params; this.body = body;
    }
    public String toString() { return "fn " + name.lexeme + "(...) {...}"; }
}
