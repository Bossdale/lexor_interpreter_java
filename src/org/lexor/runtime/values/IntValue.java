package org.lexor.runtime.values;

public class IntValue implements RuntimeValue {
    private final int value;
    public IntValue(int value) { this.value = value; }
    @Override public Object getValue() { return value; }
    @Override public String asString() { return String.valueOf(value); }
}