package org.lexor.lexer;

public enum TokenType {
    // Single-character symbols and operators
    LEFT_PAREN, RIGHT_PAREN,       // ( )
    LEFT_BRACKET, RIGHT_BRACKET,   // [ ] Escape codes
    PLUS, MINUS, STAR, SLASH, MODULO, // + - * / %
    AMPERSAND,                     // & (Concatenator)
    DOLLAR,                        // $ (Next line / Carriage return)
    EQUAL,                         // = (For variable assignment)

    // Literals
    IDENTIFIER,                    // Variable names (e.g., _myVar, val1)
    INT_LITERAL,                   // Integer numbers (e.g., 42)
    FLOAT_LITERAL,                 // Floating point numbers (e.g., 3.14)
    CHAR_LITERAL,                  // Single characters
    BOOL_LITERAL,                  // true or false

    // Reserved Keywords (All caps as per specification)
    SCRIPT, AREA, START, END,      // SCRIPT AREA, START SCRIPT, END SCRIPT
    INT, CHAR, BOOL, FLOAT,        // Data types

    EOF                            // End Of File
}