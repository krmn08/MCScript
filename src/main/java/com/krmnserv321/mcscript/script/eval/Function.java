package com.krmnserv321.mcscript.script.eval;

import com.krmnserv321.mcscript.script.ast.Arguments;
import com.krmnserv321.mcscript.script.ast.statement.Statement;
import com.krmnserv321.mcscript.script.java.Pair;

public class Function extends ScriptCallable {
    private Environment environment;

    private final Arguments parameters = new Arguments();
    private final Class<?> returnType;
    private final Statement body;
    private final boolean lambda;

    public Function(Environment environment, Class<?> returnType, Statement body) {
        this(environment, returnType, body, false);
    }

    public Function(Environment environment, Class<?> returnType, Statement body, boolean lambda) {
        this.environment = environment;
        this.returnType = returnType;
        this.body = body;
        this.lambda = lambda;
    }

    Arguments getParameters() {
        return parameters;
    }

    public Class<?> getReturnType() {
        return returnType;
    }

    public Statement getBody() {
        return body;
    }

    public Environment getEnvironment() {
        return environment;
    }

    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    public boolean isLambda() {
        return lambda;
    }

    @Override
    public Object call(Object... args) {
        return Evaluator.applyFunction(this, args);
    }

    @Override
    public Object call(Pair... args) {
        return Evaluator.applyFunctionWithPair(this, args);
    }

    @Override
    public String toString() {
        if (lambda) {
            if (parameters.isEmpty()) {
                return "() -> " + body;
            } else if (parameters.size() >= 2) {
                return "(" + parameters + ") -> " + body;
            }

            return parameters + " -> " + body;
        }

        if (returnType != null) {
            return "fun(" + parameters + "): " + returnType.getSimpleName() + " " + body;
        }

        return "fun(" + parameters + ") " + body;
    }
}
