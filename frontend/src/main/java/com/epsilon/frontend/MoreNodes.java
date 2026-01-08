package com.epsilon.frontend;

import java.util.List;

// Additional Statements
class ExpressionStmt extends Stmt {
    public final Expr expression;

    public ExpressionStmt(Expr expression) {
        this.expression = expression;
    }

    public String toString() {
        return expression + ";";
    }
}

class BlockStmt extends Stmt {
    public final List<Stmt> statements;

    public BlockStmt(List<Stmt> statements) {
        this.statements = statements;
    }

    public String toString() {
        return "{ ... }";
    }
}

// Additional Expressions
class UnaryExpr extends Expr {
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

class GroupingExpr extends Expr {
    public final Expr expression;

    public GroupingExpr(Expr expression) {
        this.expression = expression;
    }

    public String toString() {
        return "(group " + expression + ")";
    }
}

// Control Flow
class IfStmt extends Stmt {
    public final Expr condition;
    public final Stmt thenBranch;
    public final Stmt elseBranch;

    public IfStmt(Expr condition, Stmt thenBranch, Stmt elseBranch) {
        this.condition = condition;
        this.thenBranch = thenBranch;
        this.elseBranch = elseBranch;
    }

    public String toString() {
        return "if (" + condition + ") " + thenBranch + (elseBranch != null ? " else " + elseBranch : "");
    }
}

class WhileStmt extends Stmt {
    public final Expr condition;
    public final Stmt body;

    public WhileStmt(Expr condition, Stmt body) {
        this.condition = condition;
        this.body = body;
    }

    public String toString() {
        return "while (" + condition + ") " + body;
    }
}

class ReturnStmt extends Stmt {
    public final Token keyword;
    public final Expr value;

    public ReturnStmt(Token keyword, Expr value) {
        this.keyword = keyword;
        this.value = value;
    }

    public String toString() {
        return "return " + (value != null ? value : "") + ";";
    }
}
