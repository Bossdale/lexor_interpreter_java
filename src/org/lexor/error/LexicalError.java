package org.lexor.error;

public class LexicalError extends LexorException {
    public LexicalError(int line, String message) {
        super(String.format("Lexical Error at line %d: %s", line, message));
    }
}