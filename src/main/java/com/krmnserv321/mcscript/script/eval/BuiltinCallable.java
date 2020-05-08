package com.krmnserv321.mcscript.script.eval;

import com.krmnserv321.mcscript.script.ast.expression.None;
import com.krmnserv321.mcscript.script.java.Pair;

import java.util.Arrays;

public class BuiltinCallable extends JavaCallable {
    private final String name;

    BuiltinCallable(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public Object call(Object... args) {
        try {
            return Evaluator.callBuiltin(this, Arrays.stream(args).map(o -> new Pair(o, None.NONE)).toArray(Pair[]::new));
        } catch (ArgumentMismatchException e) {
            return new ScriptError(token, "argument mismatch: " + this);
        }
    }

    @Override
    public Object call(Pair... args) {
        try {
            return Evaluator.callBuiltin(this, args);
        } catch (ArgumentMismatchException e) {
            return new ScriptError(token, "argument mismatch: " + this);
        }
    }

    @Override
    public String toString() {
        return name;
    }
}
