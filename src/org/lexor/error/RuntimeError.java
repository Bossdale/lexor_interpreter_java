package org.lexor.error;

public class RuntimeError extends LexorException {
    public RuntimeError(int line, String message) {
        super("[Runtime Error] Line " + line + ": " + message);
    }
    // For errors without a line context:
    public RuntimeError(String message) {
        super("[Runtime Error] " + message);
    }
}