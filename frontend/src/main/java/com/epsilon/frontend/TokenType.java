package com.epsilon.frontend;

public enum TokenType {
    // Keywords
    FUNCTION, RETURN, VAR, IF, ELSE, WHILE, PRINT,

    // Literals
    IDENTIFIER, INT_LITERAL, FLOAT_LITERAL, STRING_LITERAL,

    // Operators
    PLUS, MINUS, STAR, SLASH, EQUAL, EQUAL_EQUAL,
    BANG_EQUAL, LESS, LESS_EQUAL, GREATER, GREATER_EQUAL,

    // Delimiters
    LPAREN, RPAREN, LBRACE, RBRACE, SEMICOLON, COMMA,

    // End of File
    EOF
}
