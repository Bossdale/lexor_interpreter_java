package org.lexor.runtime;

public class ContinueSignal extends RuntimeException {
    public ContinueSignal() {
        super(null, null, true, false);
    }
}
