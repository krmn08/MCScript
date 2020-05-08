package com.krmnserv321.mcscript.script.eval;

import com.google.common.base.Objects;
import com.krmnserv321.mcscript.script.ast.expression.None;
import com.krmnserv321.mcscript.script.java.Pair;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;

public class ClassMethod extends JavaCallable {
    private final Class<?> receiver;
    private final String name;
    private final boolean unsafe;

    public ClassMethod(Class<?> receiver, String name, boolean unsafe) {
        this.receiver = receiver;
        this.name = name;
        this.unsafe = unsafe;
    }

    public Class<?> getReceiver() {
        return receiver;
    }

    public String getName() {
        return name;
    }

    boolean isUnsafe() {
        return unsafe;
    }

    @Override
    public Object call(Object... args) {
        try {
            return Evaluator.callMethod(this, Arrays.stream(args).map(o -> new Pair(o, None.NONE)).toArray(Pair[]::new));
        } catch (ArgumentMismatchException e) {
            return new ScriptError(token, "argument mismatch: " + this);
        } catch (NoSuchMethodException e) {
            return new ScriptError(token, "method not found: " + this);
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
        } catch (ArgumentMismatchException e) {
            return new ScriptError(token, "argument mismatch: " + this);
        } catch (NoSuchMethodException e) {
            return new ScriptError(token, "method not found: " + this);
        } catch (IllegalAccessException e) {
            return new ScriptError(token, "illegal access: " + this);
        } catch (InvocationTargetException e) {
            return new ScriptError(token, e.getCause());
        }
    }

    @Override
    public String toString() {
        return receiver.getName() + "." + name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ClassMethod that = (ClassMethod) o;
        return unsafe == that.unsafe &&
                Objects.equal(receiver, that.receiver) &&
                Objects.equal(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(receiver, name, unsafe);
    }
}
