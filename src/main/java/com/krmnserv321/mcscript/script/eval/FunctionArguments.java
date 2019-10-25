package com.krmnserv321.mcscript.script.eval;

class FunctionArguments {
    private Object[] args;

    FunctionArguments(Object... args) {
        this.args = args;
    }

    public <T> T get(int index) {
        //noinspection unchecked
        return (T) args[index];
    }
}
