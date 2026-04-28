package org.lexor.runtime;

import org.lexor.runtime.values.RuntimeValue;
import org.lexor.error.RuntimeError;

import java.util.HashMap;
import java.util.Map;

public class Environment {
    private final Environment parent;
    private final Map<String, RuntimeValue> values = new HashMap<>();

    // Constructor for the Global Scope
    public Environment() {
        this.parent = null;
    }

    // Constructor for Local Scopes (Blocks)
    public Environment(Environment parent) {
        this.parent = parent;
    }

    // Defines a new variable in the CURRENT scope
    public void define(String name, RuntimeValue value) {
        values.put(name, value);
    }

    // Assigns an existing variable (checks current scope, then climbs to parents)
    public void assign(String name, RuntimeValue value) {
        if (values.containsKey(name)) {
            values.put(name, value);
            return;
        }
        if (parent != null) {
            parent.assign(name, value);
            return;
        }
        throw new RuntimeError("Undefined variable '" + name + "'. It may not have been declared.");
    }

    // Retrieves a variable (checks current scope, then climbs to parents)
    public RuntimeValue get(String name) {
        if (values.containsKey(name)) {
            return values.get(name);
        }
        if (parent != null) {
            return parent.get(name);
        }
        throw new RuntimeError("Undefined variable '" + name + "'. It may not have been declared.");
    }
}