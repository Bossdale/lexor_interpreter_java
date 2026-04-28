package org.lexor.semantic.symbol;

public class Symbol {
    private final String name;
    private final Type type;
    private final int line;

    private boolean initialized;

    public Symbol(String name, Type type, int line, boolean initialized) {
        this.name = name;
        this.type = type;
        this.line = line;
        this.initialized = initialized;
    }

    public String getName()  { return name; }
    public Type getType()    { return type; }
    public int getLine()     { return line; }

    public boolean isInitialized() { return initialized; }
    public void markInitialized()  { this.initialized = true; }
}