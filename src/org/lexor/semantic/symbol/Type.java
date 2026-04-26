package org.lexor.semantic.symbol;

public enum Type {
    INT,    // 4-byte integer
    FLOAT,  // 4-byte decimal
    CHAR,   // Single character
    BOOL,   // true / false
    UNKNOWN,// Used for semantic error handling
    STRING
}
