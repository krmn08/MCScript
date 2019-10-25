package com.krmnserv321.mcscript.script.eval;

import java.util.HashMap;
import java.util.Map;

public class PublicEnvironment {
    private ClassLoader loader;

    private Map<String, Object> storeMap = new HashMap<>();
    private Map<Class, Map<String, Function>> extensionMap = new HashMap<>();

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

    public void importClass(Class clazz) {
        put(clazz.getSimpleName(), ClassObject.of(clazz));
    }

    public void importClass(String fqcn) {
        try {
            Class clazz = loader.loadClass(fqcn);
            put(clazz.getSimpleName(), ClassObject.of(clazz));
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public Object get(String key) {
        return storeMap.get(key);
    }

    @SuppressWarnings("WeakerAccess")
    public Function getExtension(Class type, String name) {
        Map<String, Function> extensionMap = getExtensionMap(type);
        if (extensionMap != null) {
            return extensionMap.get(name);
        }

        return null;
    }

    public void put(String key, Object value) {
        storeMap.put(key, value);
    }

    @SuppressWarnings("WeakerAccess")
    public void putExtension(Class type, String name, Function function) {
        if (!extensionMap.containsKey(type)) {
            extensionMap.put(type, new HashMap<>());
        }
        extensionMap.get(type).put(name, function);
    }

    private Map<String, Function> getExtensionMap(Class type) {
        for (Class<?> c : extensionMap.keySet()) {
            if (c.isAssignableFrom(type)) {
                return extensionMap.get(c);
            }
        }

        return null;
    }

    @SuppressWarnings("WeakerAccess")
    public boolean isPublic(String key) {
        return storeMap.containsKey(key);
    }

    @SuppressWarnings("WeakerAccess")
    public ClassLoader getClassLoader() {
        return loader;
    }
}
