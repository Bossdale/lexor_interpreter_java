package org.lexor.semantic.symbol;

public class Symbol {
    private final String symbol;
    private final Type type;

    public Symbol(String symbol, Type type) {
        this.symbol = symbol;
        this.type = type;
    }

    public String getSymbol() {
        return symbol;
    }

    public Type getType() {
        return type;
    }
}
