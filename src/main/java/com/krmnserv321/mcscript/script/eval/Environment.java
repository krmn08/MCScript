package com.krmnserv321.mcscript.script.eval;

import com.krmnserv321.mcscript.script.ast.statement.Statement;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

public class Environment {
    private static final NullObject NULL = new NullObject();

    private PublicEnvironment publicEnvironment;
    private Environment outer;

    private Map<String, Object> storeMap = new HashMap<>();
    private Map<String, Object> constMap = new HashMap<>();

    private Stack<DeferObject> deferStack;

    public Environment(PublicEnvironment publicEnvironment) {
        this.publicEnvironment = publicEnvironment;
    }

    public Environment(PublicEnvironment publicEnvironment, Stack<DeferObject> deferStack) {
        this.publicEnvironment = publicEnvironment;
        this.deferStack = deferStack;
    }

    @SuppressWarnings("WeakerAccess")
    public void setOuter(Environment outer) {
        this.outer = outer;
    }

    @SuppressWarnings("WeakerAccess")
    public void setDeferStack(Stack<DeferObject> deferStack) {
        this.deferStack = deferStack;
    }

    public Object get(String key) {
        Object value = constMap.get(key);
        if (value == null) {
            value = storeMap.get(key);
        }

        if (value == null) {
            value = publicEnvironment.get(key);
        }

        if (value == null) {
            if (outer != null) {
                value = outer.get(key);
            }

            if (value == null && storeMap.containsKey(key)) {
                value = NULL;
            }
        }

        return value;
    }

    @SuppressWarnings("WeakerAccess")
    public void put(String key, Object value) {
        Map<String, Object> map = getOuterMap(key);
        if (map == null) {
            storeMap.put(key, value);
        } else {
            map.put(key, value);
        }
    }

    public void putConstant(String key, Object value) {
        constMap.put(key, value);
    }

    @SuppressWarnings("WeakerAccess")
    public void addDefer(Environment environment, Statement statement) {
        if (deferStack != null) {
            deferStack.push(new DeferObject(environment, statement));
        } else if (outer != null) {
            outer.addDefer(environment, statement);
        }
    }

    @SuppressWarnings("WeakerAccess")
    public boolean isConstant(String key) {
        boolean contains = constMap.containsKey(key);

        if (contains) {
            return true;
        }

        if (outer != null) {
            return outer.isConstant(key);
        }

        return publicEnvironment.isPublic(key);
    }

    @SuppressWarnings("WeakerAccess")
    public PublicEnvironment getPublicEnvironment() {
        return publicEnvironment;
    }

    private Map<String, Object> getOuterMap(String key) {
        if (storeMap.containsKey(key)) {
            return storeMap;
        } else if (outer != null) {
            return outer.getOuterMap(key);
        }

        return null;
    }
}