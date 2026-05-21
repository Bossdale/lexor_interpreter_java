package org.lexor.runtime.values;

import java.text.DecimalFormat;

public class FloatValue implements RuntimeValue {
    private final float value;
    private static final DecimalFormat FORMAT = new DecimalFormat("0.0#");

    public FloatValue(float value) {
        this.value = value;
    }

    @Override public Object getValue() {return value;}
    @Override public String asString() {return FORMAT.format(value);}
    @Override public String toString() {return asString(); }
}