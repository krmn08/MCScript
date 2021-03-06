package com.krmnserv321.mcscript.script.eval;

import com.krmnserv321.mcscript.script.ast.statement.Statement;

import java.util.*;

public class Environment {
    private static final NullObject NULL = new NullObject();

    private final PublicEnvironment publicEnvironment;
    private Environment outer;

    private final Map<String, Object> storeMap = new HashMap<>();
    private final Map<String, Object> constMap = new HashMap<>();

    private final List<String> packageList = new ArrayList<>();

    private Stack<DeferObject> deferStack;

    public Environment(PublicEnvironment publicEnvironment) {
        this.publicEnvironment = publicEnvironment;
    }

    public Environment(PublicEnvironment publicEnvironment, Stack<DeferObject> deferStack) {
        this.publicEnvironment = publicEnvironment;
        this.deferStack = deferStack;
    }

    public void setOuter(Environment outer) {
        this.outer = outer;
    }

    public void setDeferStack(Stack<DeferObject> deferStack) {
        this.deferStack = deferStack;
    }

    public Object get(String key) {
        Object value = constMap.get(key);
        if (value == null && constMap.containsKey(key)) {
            return NULL;
        }

        if (value == null) {
            value = publicEnvironment.get(key);
        }

        if (value == null && publicEnvironment.isPublic(key)) {
            return NULL;
        }

        if (value == null) {
            value = storeMap.get(key);
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

    public void addPackage(String pkg) {
        packageList.add(pkg);
    }

    public Class<?> checkClassName(String name) {
        ClassLoader loader = publicEnvironment.getClassLoader();
        for (String pkg : packageList) {
            try {
                return loader.loadClass(pkg + "." + name);
            } catch (ClassNotFoundException ignored) {
            }
        }

        if (outer != null) {
            return outer.checkClassName(name);
        }

        return publicEnvironment.checkClassName(name);
    }

    public void addDefer(Environment environment, Statement statement) {
        if (deferStack != null) {
            deferStack.push(new DeferObject(environment, statement));
        } else if (outer != null) {
            outer.addDefer(environment, statement);
        }
    }

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

    public PublicEnvironment getPublicEnvironment() {
        return publicEnvironment;
    }

    public Map<String, Object> getAll() {
        Map<String, Object> map = new HashMap<>(storeMap);
        map.putAll(constMap);
        if (outer != null) {
            map.putAll(outer.getAll());
        }

        return map;
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