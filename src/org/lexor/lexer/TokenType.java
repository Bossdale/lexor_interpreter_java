package org.lexor.lexer;

public enum TokenType {
    // Single-character symbols and operators
    LEFT_PAREN, RIGHT_PAREN,       // ( )
    LEFT_BRACKET, RIGHT_BRACKET,   // [ ] Escape codes
    PLUS, MINUS, STAR, SLASH, MODULO, // + - * / %
    AMPERSAND,                     // & (Concatenator)
    DOLLAR,                        // $ (Next line / Carriage return)
    EQUAL,                         // = (Assignment)
    COMMA, COLON,                  // , :

    // Unary Operators 
    UNARY_PLUS, UNARY_MINUS,

    // Relational Operators
    GREATER, GREATER_EQUAL,        // >  >=
    LESS, LESS_EQUAL,              // <  <=
    EQUAL_EQUAL, NOT_EQUAL,        // ==  <>

    // Literals
    IDENTIFIER,
    INT_LITERAL,
    FLOAT_LITERAL,
    CHAR_LITERAL,
    BOOL_LITERAL,
    STRING_LITERAL,
    ESCAPE_LITERAL,

    // Reserved Keywords (Control Flow & I/O)
    SCRIPT, AREA, START, END,      // Program structure
    DECLARE,                       // Declaration marker
    INT, CHAR, BOOL, FLOAT, STRING, // Data types
    IF, ELSE,                      // Conditionals
    REPEAT, WHEN, FOR,             // Loops
    BREAK, CONTINUE,               // Loop control
    SCAN, PRINT,                   // I/O
    AND, OR, NOT,                  // Logical operators

    EOF
}