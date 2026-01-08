package com.epsilon.frontend;

import java.util.ArrayList;
import java.util.List;

public class Parser {
    private final List<Token> tokens;
    private int current = 0;

    public Parser(List<Token> tokens) {
        this.tokens = tokens;
    }

    public List<Stmt> parse() {
        List<Stmt> statements = new ArrayList<>();
        while (!isAtEnd()) {
            statements.add(declaration());
        }
        return statements;
    }

    private Stmt declaration() {
        try {
            if (match(TokenType.FUNCTION))
                return function("function");
            if (match(TokenType.VAR))
                return varDeclaration();
            return statement();
        } catch (RuntimeException e) {
            synchronize();
            return null;
        }
    }

    private Stmt function(String kind) {
        Token name = consume(TokenType.IDENTIFIER, "Expect " + kind + " name.");
        consume(TokenType.LPAREN, "Expect '(' after " + kind + " name.");
        List<Token> parameters = new ArrayList<>();
        if (!check(TokenType.RPAREN)) {
            do {
                if (parameters.size() >= 255) {
                    error(peek(), "Can't have more than 255 parameters.");
                }
                parameters.add(consume(TokenType.IDENTIFIER, "Expect parameter name."));
            } while (match(TokenType.COMMA));
        }
        consume(TokenType.RPAREN, "Expect ')' after parameters.");
        consume(TokenType.LBRACE, "Expect '{' before " + kind + " body.");
        List<Stmt> body = block();
        return new FunctionStmt(name, parameters, body);
    }

    private Stmt varDeclaration() {
        Token name = consume(TokenType.IDENTIFIER, "Expect variable name.");
        Expr initializer = null;
        if (match(TokenType.EQUAL)) {
            initializer = expression();
        }
        consume(TokenType.SEMICOLON, "Expect ';' after variable declaration.");
        return new VarDeclStmt(name, initializer);
    }

    private Stmt statement() {
        if (match(TokenType.IF))
            return ifStatement();
        if (match(TokenType.WHILE))
            return whileStatement();
        if (match(TokenType.RETURN))
            return returnStatement();
        if (match(TokenType.PRINT))
            return printStatement();
        if (match(TokenType.LBRACE))
            return new BlockStmt(block());

        return expressionStatement();
    }

    private Stmt printStatement() {
        Expr value = expression();
        consume(TokenType.SEMICOLON, "Expect ';' after value.");
        return new PrintStmt(value);
    }

    private Stmt ifStatement() {
        consume(TokenType.LPAREN, "Expect '(' after 'if'.");
        Expr condition = expression();
        consume(TokenType.RPAREN, "Expect ')' after if condition.");

        Stmt thenBranch = statement();
        Stmt elseBranch = null;
        if (match(TokenType.ELSE)) {
            elseBranch = statement();
        }

        return new IfStmt(condition, thenBranch, elseBranch);
    }

    private Stmt whileStatement() {
        consume(TokenType.LPAREN, "Expect '(' after 'while'.");
        Expr condition = expression();
        consume(TokenType.RPAREN, "Expect ')' after while condition.");
        Stmt body = statement();

        return new WhileStmt(condition, body);
    }

    private Stmt returnStatement() {
        Token keyword = previous();
        Expr value = null;
        if (!check(TokenType.SEMICOLON)) {
            value = expression();
        }

        consume(TokenType.SEMICOLON, "Expect ';' after return value.");
        return new ReturnStmt(keyword, value);
    }

    private Stmt expressionStatement() {
        Expr expr = expression();
        consume(TokenType.SEMICOLON, "Expect ';' after expression.");
        return new ExpressionStmt(expr); // Needs ExpressionStmt
    }

    private List<Stmt> block() {
        List<Stmt> statements = new ArrayList<>();
        while (!check(TokenType.RBRACE) && !isAtEnd()) {
            statements.add(declaration());
        }
        consume(TokenType.RBRACE, "Expect '}' after block.");
        return statements;
    }

    private Expr expression() {
        return equality();
    }

    private Expr equality() {
        Expr expr = comparison();
        while (match(TokenType.BANG_EQUAL, TokenType.EQUAL_EQUAL)) {
            Token operator = previous();
            Expr right = comparison();
            expr = new BinaryExpr(expr, operator, right);
        }
        return expr;
    }

    private Expr comparison() {
        Expr expr = term();
        while (match(TokenType.GREATER, TokenType.GREATER_EQUAL,
                TokenType.LESS, TokenType.LESS_EQUAL)) {
            Token operator = previous();
            Expr right = term();
            expr = new BinaryExpr(expr, operator, right);
        }
        return expr;
    }

    private Expr term() {
        Expr expr = factor();
        while (match(TokenType.MINUS, TokenType.PLUS)) {
            Token operator = previous();
            Expr right = factor();
            expr = new BinaryExpr(expr, operator, right);
        }
        return expr;
    }

    private Expr factor() {
        Expr expr = unary();
        while (match(TokenType.SLASH, TokenType.STAR)) {
            Token operator = previous();
            Expr right = unary();
            expr = new BinaryExpr(expr, operator, right);
        }
        return expr;
    }

    private Expr unary() {
        if (match(TokenType.BANG_EQUAL, TokenType.MINUS)) {
            Token operator = previous();
            Expr right = unary();
            return new UnaryExpr(operator, right); // Needs UnaryExpr
        }
        return primary();
    }

    private Expr primary() {
        if (match(TokenType.INT_LITERAL) || match(TokenType.FLOAT_LITERAL) || match(TokenType.STRING_LITERAL)) {
            return new LiteralExpr(previous().literal);
        }
        if (match(TokenType.IDENTIFIER)) {
            return new VariableExpr(previous());
        }
        if (match(TokenType.LPAREN)) {
            Expr expr = expression();
            consume(TokenType.RPAREN, "Expect ')' after expression.");
            return new GroupingExpr(expr); // Needs GroupingExpr
        }
        throw error(peek(), "Expect expression.");
    }

    private boolean match(TokenType... types) {
        for (TokenType type : types) {
            if (check(type)) {
                advance();
                return true;
            }
        }
        return false;
    }

    private Token consume(TokenType type, String message) {
        if (check(type))
            return advance();
        throw error(peek(), message);
    }

    private boolean check(TokenType type) {
        if (isAtEnd())
            return false;
        return peek().type == type;
    }

    private Token advance() {
        if (!isAtEnd())
            current++;
        return previous();
    }

    private boolean isAtEnd() {
        return peek().type == TokenType.EOF;
    }

    private Token peek() {
        return tokens.get(current);
    }

    private Token previous() {
        return tokens.get(current - 1);
    }

    private RuntimeException error(Token token, String message) {
        System.err.println("[" + token.line + ":" + token.column + "] " + message);
        return new RuntimeException(message);
    }

    private void synchronize() {
        advance();
        while (!isAtEnd()) {
            if (previous().type == TokenType.SEMICOLON)
                return;
            switch (peek().type) {
                case FUNCTION:
                case VAR:
                case IF:
                case WHILE:
                case PRINT:
                case RETURN:
                    return;
                default:
                    // Keep advancing
            }
            advance();
        }
    }
}
