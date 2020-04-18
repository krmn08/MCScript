package com.krmnserv321.mcscript.script.eval;

import com.google.common.base.Objects;
import com.krmnserv321.mcscript.script.ast.expression.None;
import com.krmnserv321.mcscript.script.java.Pair;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;

class ClassObject extends JavaCallable {
    private final Class<?> object;
    private boolean unsafe;

    private ClassObject(Class<?> object) {
        this.object = object;
    }

    static ClassObject of(Class<?> object) {
        return new ClassObject(object);
    }

    Class<?> getObject() {
        return object;
    }

    void setUnsafe() {
        this.unsafe = true;
    }

    boolean isUnsafe() {
        return unsafe;
    }

    @Override
    public Object call(Object... args) {
        try {
            return Evaluator.newInstance(this, Arrays.stream(args).map(o -> new Pair(o, None.NONE)).toArray(Pair[]::new));
        } catch (NoSuchMethodException e) {
            return new ScriptError(token, "constructor not found: " + object.getSimpleName());
        } catch (ArgumentMismatchException e) {
            return new ScriptError(token, "argument mismatch: " + this);
        } catch (IllegalAccessException e) {
            return new ScriptError(token, "illegal access: " + object.getSimpleName());
        } catch (InstantiationException e) {
            return new ScriptError(token, "cannot instantiate abstract class or interface");
        } catch (InvocationTargetException e) {
            return new ScriptError(token, e.getCause());
        } finally {
            unsafe = false;
        }
    }

    @Override
    public Object call(Pair... args) {
        try {
            return Evaluator.newInstance(this, args);
        } catch (NoSuchMethodException e) {
            return new ScriptError(token, "constructor not found: " + object.getSimpleName());
        } catch (ArgumentMismatchException e) {
            return new ScriptError(token, "argument mismatch: " + this);
        } catch (IllegalAccessException e) {
            return new ScriptError(token, "illegal access: " + object.getSimpleName());
        } catch (InstantiationException e) {
            return new ScriptError(token, "cannot instantiate abstract class or interface");
        } catch (InvocationTargetException e) {
            return new ScriptError(token, e.getCause());
        } finally {
            unsafe = false;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ClassObject that = (ClassObject) o;
        return Objects.equal(object, that.object);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(object);
    }

    @Override
    public String toString() {
        return "ClassObject(" + object.getTypeName() + ")";
    }
}
