package org.lexor.runtime.values;

public class BoolValue implements RuntimeValue {
    private final boolean value;
    public BoolValue(boolean value) { this.value = value; }
    @Override public Object getValue() { return value; }
    @Override public String asString() { return value ? "TRUE" : "FALSE"; }
    @Override public String toString() { return asString(); }
}