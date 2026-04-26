package org.lexor.runtime.values;

// Base interface for all LEXOR values
public interface RuntimeValue {
    Object getValue();
    String asString();
}

// TODO 7 — Add toString() to all RuntimeValue implementations
//  Currently, RuntimeValue has asString() but no toString().
//  If any Java code accidentally calls .toString() on a value object
//  (e.g., inside a string concatenation like "Value: " + someRuntimeValue),
//  it prints the default Java object reference (org.lexor.runtime.values.IntValue@3764951d) instead of the actual value.
//  This will produce confusing debug output.

// Add this to each value class — IntValue, FloatValue, BoolValue, CharValue, StringValue:

// TODO: Override toString() in every RuntimeValue subclass so Java's own string
//       coercion (e.g., in error messages or debug prints) shows the actual value
//       instead of the default object reference hash.

// In IntValue.java:
// @Override
// public String toString() { return asString(); }

// In FloatValue.java:
// @Override
// public String toString() { return asString(); }

// In BoolValue.java:
// @Override
// public String toString() { return asString(); }

// In CharValue.java:
// @Override
// public String toString() { return asString(); }

// In StringValue.java:
// @Override
// public String toString() { return asString(); }
