package org.lexor.runtime.values;

public class StringValue implements RuntimeValue {
    private final String value;
    public StringValue(String value) { this.value = value; }
    @Override public Object getValue() { return value; }
    @Override public String asString() { return value; }
}