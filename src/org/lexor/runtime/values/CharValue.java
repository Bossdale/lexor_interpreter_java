package org.lexor.runtime.values;

public class CharValue implements RuntimeValue {
    private final char value;
    public CharValue(char value) { this.value = value; }
    @Override public Object getValue() { return value; }
    @Override public String asString() { return String.valueOf(value); }
}