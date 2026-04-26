package org.lexor.error;

public class SyntaxError extends LexorException {
    public SyntaxError(int line, String message) {
        super(String.format("Syntax Error at line %d: %s", line, message));
    }
}