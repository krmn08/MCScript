package com.krmnserv321.mcscript.script.eval;

class ReturnValue {
    private final Object value;

    ReturnValue(Object value) {
        this.value = value;
    }

    Object getValue() {
        return value;
    }
}
