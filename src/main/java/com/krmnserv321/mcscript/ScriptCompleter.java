package com.krmnserv321.mcscript;

import com.krmnserv321.mcscript.script.Lexer;
import com.krmnserv321.mcscript.script.Parser;
import com.krmnserv321.mcscript.script.Program;
import com.krmnserv321.mcscript.script.eval.*;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.krmnserv321.mcscript.script.eval.EvalUtils.isStatic;
import static com.krmnserv321.mcscript.script.eval.EvalUtils.toProperty;

public class ScriptCompleter {
    private static final Pattern IDENTIFIER_PATTERN = Pattern.compile("[a-zA-Z][a-zA-Z0-9_]*$");

    private final Environment environment;
    private final String src;

    private final String prefix;

    public ScriptCompleter(Environment environment, String src) {
        this.environment = environment;
        if (src.isEmpty()) {
            this.src = "";
            prefix = "";
            return;
        }

        int len = src.length();
        int l = src.charAt(len - 1) == '.' ? len - 1 : len;
        int begin = l;
        for (int i = l - 1; i > 0; i--) {
            if (src.charAt(i) == ')') {
                int count = 1;
                i--;
                while (i > 0 && count > 0) {
                    if (src.charAt(i) == ')') {
                        count++;
                    } else if (src.charAt(i) == '(') {
                        count--;
                    }

                    i--;
                }
            }

            Matcher matcher = IDENTIFIER_PATTERN.matcher(src.substring(0, i + 1));
            if (matcher.find()) {
                begin = matcher.start();
                i = begin - 1;
            } else {
                break;
            }

            if (i > 0 && src.charAt(i) != '.') {
                break;
            }
        }

        this.src = src.substring(begin);

        String[] split = src.split(" ");
        if (split.length > 0) {
            String last = split[split.length - 1];
            if (!last.isEmpty() && last.charAt(last.length() - 1) == '.') {
                prefix = last;
            } else {
                Matcher ident = IDENTIFIER_PATTERN.matcher(last);
                prefix = ident.find() ? last.substring(0, ident.start(0)) : "";
            }
        } else {
            prefix = "";
        }
    }

    public List<String> complete() {
        List<String> strings = new ArrayList<>();
        int start = 0;
        int count = 0;
        for (int i = 0; i < src.toCharArray().length; i++) {
            if (src.charAt(i) == '(') {
                count++;
            } else if (src.charAt(i) == ')') {
                count--;
            }

            if (count == 0 && src.charAt(i) == '.') {
                String s = src.substring(start, i);
                if (!s.isEmpty()) {
                    strings.add(s);
                }
                i++;
                start = i;
            }
        }

        Matcher matcher = IDENTIFIER_PATTERN.matcher(src);
        String identifier = matcher.find() ? matcher.group(0) : "";

        if (start == src.length()) {
            strings.add("");
        } else if (start < src.length()) {
            strings.add(src.substring(start));
        }

        if (strings.isEmpty()) {
            return getDefaultComplements().stream()
                    .map(this::addPrefix)
                    .collect(Collectors.toList());
        }

        String last = strings.get(strings.size() - 1);
        if (!last.isEmpty() && !last.equals(identifier)) {
            String lowerCase = identifier.toLowerCase();
            return getDefaultComplements().stream()
                    .filter(s -> s.toLowerCase().contains(lowerCase))
                    .map(this::addPrefix)
                    .collect(Collectors.toList());
        }

        if (strings.size() == 1) {
            String lowerCase = last.toLowerCase();
            return getDefaultComplements().stream()
                    .filter(s -> s.toLowerCase().contains(lowerCase))
                    .map(this::addPrefix)
                    .collect(Collectors.toList());
        }

        String first = strings.get(0);
        List<Class<?>> returnTypes;
        int index = 1;
        if (first.charAt(first.length() - 1) == ')') {
            String function = removeArguments(first);
            boolean unsafe = false;
            if (function.charAt(function.length() - 1) == '!') {
                unsafe = true;
                function = function.substring(0, function.length() - 1);
            }

            Object evaluated = eval(function);
            if (evaluated instanceof ScriptError) {
                return Collections.emptyList();
            }

            if (evaluated instanceof Function) {
                Class<?> returnType = ((Function) evaluated).getReturnType();
                if (returnType == null) {
                    return Collections.emptyList();
                }
                returnTypes = Collections.singletonList(returnType);
            } else if (evaluated instanceof ClassObject) {
                returnTypes = Collections.singletonList(((ClassObject) evaluated).getObject());
            } else if (evaluated instanceof BuiltinCallable) {
                String name = ((BuiltinCallable) evaluated).getName();
                returnTypes = Builtin.get(name).stream()
                        .map(BuiltinFunction::getReturnType)
                        .collect(Collectors.toList());
            } else if (evaluated instanceof ClassMethod) {
                ClassMethod classMethod = (ClassMethod) evaluated;
                Class<?> receiver = classMethod.getReceiver();
                Method[] methods = unsafe ? receiver.getDeclaredMethods() : receiver.getMethods();
                returnTypes = Arrays.stream(methods)
                        .filter(method -> method.getName().equals(classMethod.getName()))
                        .map(Method::getReturnType)
                        .collect(Collectors.toList());
            } else if (evaluated instanceof CallableMethod) {
                returnTypes = Collections.singletonList(((CallableMethod) evaluated).getMethod().getReturnType());
            } else if (evaluated instanceof InstanceMethod) {
                InstanceMethod instanceMethod = (InstanceMethod) evaluated;
                Class<?> receiver = instanceMethod.getReceiver().getClass();
                Method[] methods = unsafe ? receiver.getDeclaredMethods() : receiver.getMethods();
                returnTypes = Arrays.stream(methods)
                        .filter(method -> method.getName().equals(instanceMethod.getName()))
                        .map(Method::getReturnType)
                        .collect(Collectors.toList());
            } else {
                return Collections.emptyList();
            }
        } else {
            Object evaluated = eval(first);
            if (evaluated instanceof ScriptError) {
                return Collections.emptyList();
            }

            if (evaluated instanceof ClassObject) {
                Class<?> object = ((ClassObject) evaluated).getObject();
                if (strings.size() == 2) {
                    return getMembers(Collections.singletonList(object), strings.get(1), true).stream()
                            .map(this::addPrefix)
                            .collect(Collectors.toList());
                }

                String member = strings.get(index);
                boolean unsafe = false;
                boolean call = false;
                returnTypes = new ArrayList<>();

                if (member.charAt(member.length() - 1) == ')') {
                    call = true;
                    member = removeArguments(member);
                }

                if (member.charAt(member.length() - 1) == '!') {
                    unsafe = true;
                    member = member.substring(0, member.length() - 1);
                }

                if (call) {
                    Method[] methods = unsafe ? object.getDeclaredMethods() : object.getMethods();
                    for (Method method : methods) {
                        if (method.getName().equals(member)) {
                            if (isStatic(method)) {
                                returnTypes.add(method.getReturnType());
                            }
                        }
                    }
                } else {
                    if (object.isEnum()) {
                        returnTypes = Collections.singletonList(object);
                    } else {
                        Field[] fields = unsafe ? object.getDeclaredFields() : object.getFields();
                        for (Field field : fields) {
                            if (field.getName().equals(member)) {
                                if (isStatic(field)) {
                                    returnTypes.add(field.getType());
                                }
                            }
                        }

                        Class<?> propertyType = getPropertyType(object, member, unsafe, true);
                        if (propertyType != null) {
                            returnTypes.add(propertyType);
                        }
                    }
                }

                index++;
            } else {
                returnTypes = Collections.singletonList(evaluated.getClass());
            }
        }

        for (int i = index; i < strings.size() - 1; i++) {
            String member = strings.get(i);
            boolean unsafe = false;

            if (member.charAt(member.length() - 1) == ')') {
                member = removeArguments(member);
                if (member.charAt(member.length() - 1) == '!') {
                    unsafe = true;
                    member = member.substring(0, member.length() - 1);
                }
                returnTypes = getFunctionTypes(returnTypes, member, unsafe);
            } else {
                if (member.charAt(member.length() - 1) == '!') {
                    unsafe = true;
                    member = member.substring(0, member.length() - 1);
                }
                returnTypes = getFieldTypes(returnTypes, member, unsafe);
            }

            if (returnTypes.isEmpty()) {
                return Collections.emptyList();
            }
        }

        return getMembers(returnTypes, strings.get(strings.size() - 1), false).stream()
                .map(this::addPrefix)
                .collect(Collectors.toList());
    }

    private List<String> getMembers(List<Class<?>> receivers, String member, boolean staticMember) {
        List<String> result = new ArrayList<>();
        member = member.toLowerCase();
        PublicEnvironment publicEnvironment = environment.getPublicEnvironment();
        for (Class<?> receiver : receivers) {
            Class<?> superclass = receiver.getSuperclass();
            if (superclass != null) {
                result.addAll(getMembers(Collections.singletonList(superclass), member, staticMember));
            }
            result.addAll(getMembers(Arrays.asList(receiver.getInterfaces()), member, staticMember));

            Method[] methods = receiver.getDeclaredMethods();
            for (Method method : methods) {
                if (staticMember == isStatic(method)) {
                    String name = method.getName();
                    String unsafe = Modifier.isPublic(method.getModifiers()) ? "" : "!";
                    if (checkMember(name, member)) {
                        result.add(method.getName() + unsafe + "()");
                    }

                    if (name.startsWith("get") || name.startsWith("set")) {
                        String sub = name.substring(3);
                        if (checkMember(sub, member)) {
                            result.add(toProperty(sub) + unsafe);
                        }
                    }

                    if (name.startsWith("is")) {
                        if (checkMember(name, member)) {
                            result.add(name + unsafe);
                        }
                    }
                }
            }

            Field[] fields = receiver.getDeclaredFields();
            for (Field field : fields) {
                if (staticMember == isStatic(field)) {
                    String name = field.getName();
                    String unsafe = Modifier.isPublic(field.getModifiers()) ? "" : "!";
                    if (checkMember(name, member)) {
                        result.add(name + unsafe);
                    }
                }
            }

            Map<String, Function> extensionMap = publicEnvironment.getExtensionMap(receiver);
            if (extensionMap != null) {
                for (String key : extensionMap.keySet()) {
                    if (checkMember(key, member)) {
                        result.add(key + "()");
                    }
                }
            }
        }

        if (staticMember && checkMember("class", member)) {
            result.add("class");
        }

        return result;
    }

    private boolean checkMember(String memberName, String name) {
        return memberName.toLowerCase().contains(name);
    }

    private List<Class<?>> getFieldTypes(List<Class<?>> receivers, String fieldName, boolean unsafe) {
        List<Class<?>> result = new ArrayList<>();
        for (Class<?> receiver : receivers) {
            Field[] fields = unsafe ? receiver.getDeclaredFields() : receiver.getFields();
            for (Field field : fields) {
                if (!isStatic(field)) {
                    if (field.getName().equals(fieldName)) {
                        result.add(field.getType());
                    }
                }
            }

            Class<?> propertyType = getPropertyType(receiver, fieldName, unsafe, false);
            if (propertyType != null) {
                result.add(propertyType);
            }
        }

        return result;
    }

    private List<Class<?>> getFunctionTypes(List<Class<?>> receivers, String function, boolean unsafe) {
        PublicEnvironment publicEnvironment = environment.getPublicEnvironment();

        List<Class<?>> result = new ArrayList<>();
        for (Class<?> receiver : receivers) {
            Method[] methods = unsafe ? receiver.getDeclaredMethods() : receiver.getMethods();
            for (Method method : methods) {
                if (!isStatic(method)) {
                    if (method.getName().equals(function)) {
                        result.add(method.getReturnType());
                    }
                }
            }

            Function extension = publicEnvironment.getExtension(receiver, function);
            if (extension != null) {
                Class<?> returnType = extension.getReturnType();
                if (returnType != null) {
                    result.add(returnType);
                }
            }
        }

        return result;
    }

    private Class<?> getPropertyType(Class<?> receiver, String property, boolean unsafe, boolean staticProperty) {
        Method[] methods = unsafe ? receiver.getDeclaredMethods() : receiver.getMethods();
        for (Method method : methods) {
            if (staticProperty == isStatic(method)) {
                String name = method.getName();
                if (name.startsWith("get")) {
                    if (toProperty(name.substring(3)).equals(property)) {
                        return method.getReturnType();
                    }
                }

                if (name.startsWith("is")) {
                    if (name.equals(property)) {
                        return method.getReturnType();
                    }
                }
            }
        }

        for (Method method : methods) {
            if (staticProperty == isStatic(method)) {
                String name = method.getName();
                if (name.startsWith("set")) {
                    if (toProperty(name.substring(3)).equals(property)) {
                        return method.getReturnType();
                    }
                }
            }
        }

        return null;
    }


    private Object eval(String input) {
        Parser parser = new Parser(new Lexer(input));
        Program program = parser.parseProgram();
        if (!parser.getErrors().isEmpty()) {
            return new ScriptError(program.getToken(), "parser error");
        }

        return program.eval(environment);
    }

    private String removeArguments(String str) {
        int pos = str.length() - 1;
        int count = 0;
        for (; pos > 0; pos--) {
            if (str.charAt(pos) == ')') {
                count++;
            } else if (str.charAt(pos) == '(') {
                count--;
            }

            if (count == 0) {
                break;
            }
        }

        return str.substring(0, pos);
    }

    private String addPrefix(String s) {
        return prefix + s;
    }

    private List<String> getDefaultComplements() {
        List<String> result = new ArrayList<>();
        environment.getAll().entrySet().stream()
                .map(entry -> {
                    String key = entry.getKey();
                    Object value = entry.getValue();
                    if (!(value instanceof ClassObject) && value instanceof Callable) {
                        return key + "()";
                    }

                    return key;
                })
                .forEach(result::add);
        PublicEnvironment publicEnvironment = environment.getPublicEnvironment();
        publicEnvironment.getStoreMap().entrySet().stream()
                .map(entry -> {
                    String key = entry.getKey();
                    Object value = entry.getValue();
                    if (!(value instanceof ClassObject) && value instanceof Callable) {
                        return key + "()";
                    }

                    return key;
                })
                .forEach(result::add);
        Map<Class<?>, Map<String, Function>> extensionMap = publicEnvironment.getExtensionMap();
        extensionMap.values().stream()
                .map(Map::keySet)
                .flatMap(Collection::stream)
                .map(s -> s + "()")
                .forEach(result::add);
        Builtin.getBuiltinMap().keySet().stream()
                .map(s -> s + "()")
                .forEach(result::add);
        return result;
    }
}
