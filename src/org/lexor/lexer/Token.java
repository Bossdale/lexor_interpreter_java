package org.lexor.lexer;

public class Token {
    public final TokenType type;
    public final String lexeme;
    public final int line;

    public Token(TokenType type, String lexeme, int line) {
        this.type = type;
        this.lexeme = lexeme;
        this.line = line;
    }

    // This makes printing and debugging much easier later
    @Override
    public String toString() {
        return String.format("[%s] '%s' (Line %d)", type, lexeme, line);
    }
}