package org.lexor.runtime.values;

// Base interface for all LEXOR values
public interface RuntimeValue {
    Object getValue();
    String asString();
}

