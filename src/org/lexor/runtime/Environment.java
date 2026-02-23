package org.lexor.runtime;

import org.lexor.runtime.values.RuntimeValue;
import java.util.HashMap;
import java.util.Map;

public class Environment {
    // Maps variable names to their actual stored values during execution
    private final Map<String, RuntimeValue> memory = new HashMap<>();

    // Used during declaration (e.g., DECLARE INT x = 5)
    public void define(String name, RuntimeValue value) {
        memory.put(name, value);
    }

    // Used during assignment (e.g., x = 10)
    public void assign(String name, RuntimeValue value) {
        // We don't need to check if it exists here; Phase 3 already guaranteed it does!
        memory.put(name, value);
    }

    // Used when evaluating variables in expressions (e.g., PRINT: x)
    public RuntimeValue get(String name) {
        return memory.get(name);
    }
}