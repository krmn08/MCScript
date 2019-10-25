package com.krmnserv321.mcscript.script.eval;

import com.krmnserv321.mcscript.script.ast.Arguments;
import com.krmnserv321.mcscript.script.ast.statement.Block;
import com.krmnserv321.mcscript.script.ast.statement.Statement;
import com.krmnserv321.mcscript.script.java.Pair;

public class Function extends ScriptCallable {
    private Environment environment;

    private Arguments parameters = new Arguments();
    private Statement body;
    private boolean lambda;

    public Function(Environment environment, Statement body) {
        this(environment, body, false);
    }

    public Function(Environment environment, Statement body, boolean lambda) {
        this.environment = environment;
        this.body = body;
        this.lambda = lambda;
    }

    Arguments getParameters() {
        return parameters;
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
            }
            return parameters + " -> " + body;
        }

        if (body instanceof Block) {
            return "fun(" + parameters + ") " + body;
        }

        return "fun(" + parameters + "): " + body;
    }
}
