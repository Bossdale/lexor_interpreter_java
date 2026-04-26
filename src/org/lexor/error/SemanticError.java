package org.lexor.error;

public class SemanticError extends LexorException {
    public SemanticError(int line, String message) {
        super(String.format("Semantic Error at line %d: %s", line, message));
    }
}