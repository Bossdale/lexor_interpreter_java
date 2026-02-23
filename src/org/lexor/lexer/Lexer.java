package org.lexor.lexer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Lexer {
    private final String source;
    private final List<Token> tokens = new ArrayList<>();
    private int start = 0;
    private int current = 0;
    private int line = 1;

    // A map to quickly check if an identifier is actually a reserved keyword
    private static final Map<String, TokenType> keywords;
    static {
        keywords = new HashMap<>();
        keywords.put("SCRIPT", TokenType.SCRIPT);
        keywords.put("AREA", TokenType.AREA);
        keywords.put("START", TokenType.START);
        keywords.put("END", TokenType.END);

        // ADD THIS:
        keywords.put("DECLARE", TokenType.DECLARE);

        // Make sure you have your data types too!
        keywords.put("INT", TokenType.INT);
        keywords.put("FLOAT", TokenType.FLOAT);
        keywords.put("CHAR", TokenType.CHAR);
        keywords.put("BOOL", TokenType.BOOL);

        // And your other statement keywords...
        keywords.put("PRINT", TokenType.PRINT);
        keywords.put("SCAN", TokenType.SCAN);
        keywords.put("IF", TokenType.IF);
        keywords.put("ELSE", TokenType.ELSE);
        keywords.put("REPEAT", TokenType.REPEAT);
        keywords.put("WHEN", TokenType.WHEN);
        keywords.put("FOR", TokenType.FOR);
        keywords.put("AND", TokenType.AND);
        keywords.put("OR", TokenType.OR);
        keywords.put("NOT", TokenType.NOT);
    }

    public Lexer(String source) {
        this.source = source;
    }

    public List<Token> scanTokens() {
        while (!isAtEnd()) {
            start = current; // Reset the start pointer for the next token
            scanToken();
        }
        tokens.add(new Token(TokenType.EOF, "", line));
        return tokens;
    }

    private void scanToken() {
        char c = advance();
        switch (c) {
            case '(': addToken(TokenType.LEFT_PAREN); break;
            case ')': addToken(TokenType.RIGHT_PAREN); break;
            case '[': addToken(TokenType.LEFT_BRACKET); break;
            case ']': addToken(TokenType.RIGHT_BRACKET); break;
            case '+': addToken(TokenType.PLUS); break;
            case '-': addToken(TokenType.MINUS); break;
            case '*': addToken(TokenType.STAR); break;
            case '/': addToken(TokenType.SLASH); break;
            case '&': addToken(TokenType.AMPERSAND); break;
            case '$': addToken(TokenType.DOLLAR); break; // Spec: $ signifies next line
            case '=': addToken(TokenType.EQUAL); break;
            case ':': addToken(TokenType.COLON); break;

            case '%':
                if (match('%')) {
                    // It's a comment '%%', ignore everything until the end of the line
                    while (peek() != '\n' && !isAtEnd()) advance();
                } else {
                    addToken(TokenType.MODULO);
                }
                break;

            // Ignore whitespace
             case ' ':
            case '\r':
            case '\t':
                break;

            case '\n':
                line++; // Track line numbers for errors
                break;

            default:
                if (isDigit(c)) {
                    number();
                } else if (isAlpha(c)) {
                    identifier();
                } else {
                    // Later, we will connect this to your custom error package
                    System.err.println("Lexical Error at line " + line + ": Unexpected character '" + c + "'");
                }
                break;
        }
    }

    private void identifier() {
        // Spec: letters, underscores, or digits
        while (isAlphaNumeric(peek())) advance();

        String text = source.substring(start, current);
        TokenType type = keywords.getOrDefault(text, TokenType.IDENTIFIER);
        addToken(type);
    }

    private void number() {
        boolean isFloat = false;
        while (isDigit(peek())) advance();

        // Look for a fractional part
        if (peek() == '.' && isDigit(peekNext())) {
            isFloat = true;
            advance(); // Consume the "."
            while (isDigit(peek())) advance();
        }

        addToken(isFloat ? TokenType.FLOAT_LITERAL : TokenType.INT_LITERAL);
    }

    // --- Helper Methods ---

    private boolean isAtEnd() {
        return current >= source.length();
    }

    private char advance() {
        return source.charAt(current++);
    }

    private boolean match(char expected) {
        if (isAtEnd() || source.charAt(current) != expected) return false;
        current++;
        return true;
    }

    private char peek() {
        if (isAtEnd()) return '\0';
        return source.charAt(current);
    }

    private char peekNext() {
        if (current + 1 >= source.length()) return '\0';
        return source.charAt(current + 1);
    }

    private boolean isAlpha(char c) {
        return (c >= 'a' && c <= 'z') ||
                (c >= 'A' && c <= 'Z') ||
                c == '_';
    }

    private boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }

    private boolean isAlphaNumeric(char c) {
        return isAlpha(c) || isDigit(c);
    }

    private void addToken(TokenType type) {
        String text = source.substring(start, current);
        tokens.add(new Token(type, text, line));
    }
}