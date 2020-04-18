package com.krmnserv321.mcscript.script.eval;

import com.krmnserv321.mcscript.script.ast.expression.None;
import com.krmnserv321.mcscript.script.java.Pair;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;

public class CallableMethod extends JavaCallable {
    private final Object receiver;
    private final Method method;

    CallableMethod(Object receiver, Method method) {
        this.receiver = receiver;
        this.method = method;
    }

    @Override
    public Object call(Object... args) {
        try {
            return Evaluator.callMethod(this, Arrays.stream(args).map(o -> new Pair(o, None.NONE)).toArray(Pair[]::new));
        } catch (IllegalArgumentException e) {
            return new ScriptError(token, "argument mismatch: " + this);
        } catch (IllegalAccessException e) {
            return new ScriptError(token, "illegal access: " + this);
        } catch (InvocationTargetException e) {
            return new ScriptError(token, e.getCause());
        }
    }

    @Override
    public Object call(Pair... args) {
        try {
            return Evaluator.callMethod(this, args);
        } catch (IllegalArgumentException e) {
            return new ScriptError(token, "argument mismatch: " + this);
        } catch (IllegalAccessException e) {
            return new ScriptError(token, "illegal access: " + this);
        } catch (InvocationTargetException e) {
            return new ScriptError(token, e.getCause());
        }
    }

    Object getReceiver() {
        return receiver;
    }

    Method getMethod() {
        return method;
    }

    @Override
    public String toString() {
        return method.toString();
    }
}
