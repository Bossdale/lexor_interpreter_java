package org.lexor.semantic.symbol;

import java.util.HashMap;
import java.util.Map;
import java.util.Collection;
import java.util.Collections;

public class SymbolTable {
    private final SymbolTable parent;
    private final Map<String, Symbol> symbols = new HashMap<>();

    public SymbolTable() {
        this.parent = null;
    }

    public SymbolTable(SymbolTable parent) {
        this.parent = parent;
    }

    public void define(String name, Type type, int line, boolean initialized) {
        if (symbols.containsKey(name)) {
            throw new org.lexor.error.SemanticError(line,
                    "Variable '" + name + "' is already declared in this scope.");
        }
        symbols.put(name, new Symbol(name, type, line, initialized));
    }

    public Symbol resolve(String name, int line) {
        if (symbols.containsKey(name)) {
            return symbols.get(name);
        }
        if (parent != null) {
            return parent.resolve(name, line);
        }
        throw new org.lexor.error.SemanticError(line, "Undeclared or out-of-scope variable '" + name + "'.");
    }

    public int getSymbolCount() {
        return symbols.size();
    }

    // Returns all symbols in the CURRENT scope only (read-only view).
    public Collection<Symbol> getAllSymbols() {
        return Collections.unmodifiableCollection(symbols.values());
    }

    // TODO: Returns a formatted string of all symbols for debug output.
    //       Useful for printing the symbol table state after semantic analysis.
   public String dump() {
       StringBuilder sb = new StringBuilder();
       sb.append("=== Symbol Table Dump ===\n");
       for (Symbol sym : symbols.values()) {
           sb.append(String.format("  %-15s | Type: %-6s | Line: %d | Init: %s\n",
                   sym.getName(),
                   sym.getType(),
                   sym.getLine(),
                   sym.isInitialized() ? "YES" : "NO"));
       }
       if (parent != null) {
           sb.append("--- Parent Scope ---\n");
           sb.append(parent.dump());
       }
       return sb.toString();
   }



}