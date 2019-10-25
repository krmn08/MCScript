package com.krmnserv321.mcscript.script.eval;


public abstract class BuiltinFunction {
    private String name;
    private boolean varArgs;
    private Class[] parameters;

    public BuiltinFunction(String name, Class... parameters) {
        this(name, false, parameters);
    }

    public BuiltinFunction(String name, boolean varArgs, Class... parameters) {
        this.name = name;
        this.varArgs = varArgs;
        this.parameters = parameters;
    }

    public String getName() {
        return name;
    }

    public boolean isVarArgs() {
        return varArgs;
    }

    public Class[] getParameters() {
        return parameters;
    }

    protected abstract Object call(FunctionArguments args);
}