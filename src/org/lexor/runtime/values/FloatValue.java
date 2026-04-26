package org.lexor.runtime.values;

public class FloatValue implements RuntimeValue {
    private final float value;
    public FloatValue(float value) { this.value = value; }
    @Override public Object getValue() { return value; }
    @Override public String asString() { return String.valueOf(value); }
    @Override public String toString() { return asString(); }
}