package org.lexor.semantic.symbol;

public class Symbol {
    private final String name;
    private final Type type;
    private final int line;

    // TODO: Track whether the variable has been explicitly assigned a value.
    //       This lets the semantic analyzer warn when a declared-but-uninitialized
    //       variable is read before being written.
    private boolean initialized;

    public Symbol(String name, Type type, int line) {
        this.name = name;
        this.type = type;
        this.line = line;
        this.initialized = false;
    }

    public String getName() {
        return name;
    }

    public Type getType() {
        return type;
    }

    public int getLine() {
        return line;
    }

    public boolean isInitialized() {
        return initialized;
    }

    public void setInitialized(boolean initialized) {
        this.initialized = initialized;
    }
}