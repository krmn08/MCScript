package com.krmnserv321.mcscript.script.eval;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PublicEnvironment {
    private final ClassLoader loader;

    private final Map<String, Object> storeMap = new HashMap<>();
    private final Map<Class<?>, Map<String, Function>> extensionMap = new HashMap<>();
    private final List<String> packageList = new ArrayList<>();

    public PublicEnvironment(ClassLoader loader) {
        this.loader = loader;

        importClass(System.class);
        importClass(Math.class);
        importClass(Object.class);
        importClass(StringBuilder.class);
        importClass(String.class);
        importClass(Boolean.class);
        importClass(Character.class);
        importClass(Number.class);
        importClass(Byte.class);
        importClass(Short.class);
        importClass(Integer.class);
        importClass(Long.class);
        importClass(Float.class);
        importClass(Double.class);
    }

    public void importPackage(String pkg) {
        packageList.add(pkg);
    }

    public Class<?> checkClassName(String name) {
        for (String pkg : packageList) {
            try {
                return loader.loadClass(pkg + "." + name);
            } catch (ClassNotFoundException ignored) {
            }
        }

        return null;
    }

    public void importClass(Class<?> clazz) {
        put(clazz.getSimpleName(), ClassObject.of(clazz));
    }

    public void importClass(String fqcn) {
        try {
            Class<?> clazz = loader.loadClass(fqcn);
            put(clazz.getSimpleName(), ClassObject.of(clazz));
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public Object get(String key) {
        return storeMap.get(key);
    }

    public Function getExtension(Class<?> type, String name) {
        Map<String, Function> extensionMap = getExtensionMap(type);
        if (extensionMap != null) {
            return extensionMap.get(name);
        }

        return null;
    }

    public void put(String key, Object value) {
        storeMap.put(key, value);
    }

    public void putExtension(Class<?> type, String name, Function function) {
        if (!extensionMap.containsKey(type)) {
            extensionMap.put(type, new HashMap<>());
        }
        extensionMap.get(type).put(name, function);
    }

    public Map<String, Function> getExtensionMap(Class<?> type) {
        for (Class<?> c : extensionMap.keySet()) {
            if (c.isAssignableFrom(type)) {
                return extensionMap.get(c);
            }
        }

        return null;
    }

    public boolean isPublic(String key) {
        return storeMap.containsKey(key);
    }

    public ClassLoader getClassLoader() {
        return loader;
    }

    public Map<String, Object> getStoreMap() {
        return storeMap;
    }

    public Map<Class<?>, Map<String, Function>> getExtensionMap() {
        return extensionMap;
    }
}
