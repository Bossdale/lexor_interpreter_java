package org.lexor.runtime;

public class BreakSignal extends RuntimeException {
    public BreakSignal() {
        super(null, null, true, false);
    }
}
