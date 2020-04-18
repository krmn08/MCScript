package com.krmnserv321.mcscript.script.eval;

import com.google.common.base.Objects;
import com.krmnserv321.mcscript.script.ast.expression.None;
import com.krmnserv321.mcscript.script.java.Pair;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;

class InstanceMethod extends JavaCallable {
    private final PublicEnvironment publicEnvironment;
    private final Object receiver;
    private final String fieldName;
    private final boolean unsafe;

    InstanceMethod(PublicEnvironment publicEnvironment, Object receiver, String fieldName, boolean unsafe) {
        this.publicEnvironment = publicEnvironment;
        this.receiver = receiver;
        this.fieldName = fieldName;
        this.unsafe = unsafe;
    }

    PublicEnvironment getPublicEnvironment() {
        return publicEnvironment;
    }

    Object getReceiver() {
        return receiver;
    }

    String getFieldName() {
        return fieldName;
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
            e.printStackTrace();
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
            e.printStackTrace();
            return new ScriptError(token, "method not found: " + this);
        } catch (IllegalAccessException e) {
            return new ScriptError(token, "illegal access: " + this);
        } catch (InvocationTargetException e) {
            return new ScriptError(token, e.getCause());
        }
    }

    @Override
    public String toString() {
        return receiver.getClass().getName() + "." + fieldName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        InstanceMethod that = (InstanceMethod) o;
        return unsafe == that.unsafe &&
                Objects.equal(publicEnvironment, that.publicEnvironment) &&
                Objects.equal(receiver, that.receiver) &&
                Objects.equal(fieldName, that.fieldName);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(publicEnvironment, receiver, fieldName, unsafe);
    }
}
