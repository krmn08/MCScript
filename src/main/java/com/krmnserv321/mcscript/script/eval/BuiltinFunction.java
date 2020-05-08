package com.krmnserv321.mcscript.script.eval;

public abstract class BuiltinFunction {
    private final String name;
    private final boolean varArgs;
    private final Class<?> returnType;
    private final Class<?>[] parameters;

    public BuiltinFunction(String name, Class<?> returnType, Class<?>... parameters) {
        this(name, false, returnType, parameters);
    }

    public BuiltinFunction(String name, boolean varArgs, Class<?> returnType, Class<?>... parameters) {
        this.name = name;
        this.varArgs = varArgs;
        this.returnType = returnType;
        this.parameters = parameters;
    }

    public String getName() {
        return name;
    }

    public boolean isVarArgs() {
        return varArgs;
    }

    public Class<?> getReturnType() {
        return returnType;
    }

    public Class<?>[] getParameters() {
        return parameters;
    }

    protected abstract Object call(FunctionArguments args);
}