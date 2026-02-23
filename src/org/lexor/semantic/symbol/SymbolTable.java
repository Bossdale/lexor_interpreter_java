package org.lexor.semantic.symbol;

import java.util.HashMap;
import java.util.Map;

public class SymbolTable {
    private final Map<String, Symbol> symbols = new HashMap<>();

    public void define(String name, Type type, int line){
        if(symbols.containsKey(name)){
            throw new RuntimeException("Semantic Error at line " + line + "Variable: '" + name + "' is already declared.");
        }
        symbols.put(name, new Symbol(name, type));
    }

    public Symbol resolve(String name, int line){
        if(!symbols.containsKey(name)){
            throw new RuntimeException("Semantic Error at line " + line + "Variable: '" + name + "' has not been declared.");
        }
        return symbols.get(name);
    }
}
