package com.krmnserv321.mcscript.script.eval;

import com.krmnserv321.mcscript.script.java.FieldMap;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

import static com.krmnserv321.mcscript.script.eval.EvalUtils.isStatic;
import static com.krmnserv321.mcscript.script.eval.EvalUtils.toProperty;
import static com.krmnserv321.mcscript.script.eval.Evaluator.NONE_OBJECT;

public final class Builtin {
    private static Map<String, List<BuiltinFunction>> builtinMap = new HashMap<>();

    private static int iota = 0;

    static {
        addFunction(new BuiltinFunction("Field", Object.class) {
            @Override
            public Object call(FunctionArguments args) {
                Object o = args.get(0);
                FieldMap fieldMap = new FieldMap();
                if (o instanceof ClassObject) {
                    Class type = ((ClassObject) o).getObject();
                    for (Method method : type.getMethods()) {
                        String name = method.getName();
                        if (name.equals("getClass")) {
                            continue;
                        }

                        if (name.startsWith("get") && method.getParameters().length == 0) {
                            String sub = name.substring(3);
                            fieldMap.put(toProperty(sub), method.getGenericReturnType().getTypeName());
                        }

                        if (name.startsWith("is") && method.getParameters().length == 0) {
                            String sub = name.substring(2);
                            fieldMap.put(toProperty(sub), method.getGenericReturnType().getTypeName());
                        }
                    }

                    for (Field field : type.getFields()) {
                        if (isStatic(field)) {
                            try {
                                fieldMap.put(field.getName(), field.get(null));
                            } catch (IllegalAccessException ignored) {
                            }
                        } else {
                            fieldMap.put(field.getName(), field.getGenericType().getTypeName());
                        }
                    }
                } else {
                    Class type = o.getClass();
                    for (Method method : type.getMethods()) {
                        String name = method.getName();
                        if (name.equals("getClass")) {
                            continue;
                        }

                        if (name.startsWith("get") && method.getParameters().length == 0) {
                            if (!method.isAccessible()) {
                                method.setAccessible(true);
                            }
                            try {
                                String sub = name.substring(3);
                                fieldMap.put(toProperty(sub), EvalUtils.toString(method.invoke(o)));
                            } catch (IllegalAccessException | InvocationTargetException ignored) {
                            }
                        }

                        if (name.startsWith("is") && method.getParameters().length == 0) {
                            if (!method.isAccessible()) {
                                method.setAccessible(true);
                            }
                            try {
                                String sub = name.substring(2);
                                fieldMap.put(toProperty(sub), EvalUtils.toString(method.invoke(o)));
                            } catch (IllegalAccessException | InvocationTargetException ignored) {
                            }
                        }
                    }

                    for (Field field : type.getFields()) {
                        if (!field.isAccessible()) {
                            field.setAccessible(true);
                        }

                        try {
                            fieldMap.put(field.getName(), field.get(o));
                        } catch (IllegalAccessException ignored) {
                        }
                    }
                }

                return fieldMap;
            }
        });

        addFunction(new BuiltinFunction("Load", String.class) {
            @Override
            public Object call(FunctionArguments args) {
                String path = args.get(0);
                return YamlConfiguration.loadConfiguration(new File(path));
            }
        });

        addFunction(new BuiltinFunction("ToString", Object.class) {
            @Override
            public Object call(FunctionArguments args) {
                return EvalUtils.toString(args.get(0));
            }
        });

        addFunction(new BuiltinFunction("Add", Collection.class, Object.class) {
            @Override
            public Object call(FunctionArguments args) {
                Collection<Object> collection = args.get(0);
                Object value = args.get(1);
                return collection.add(value);
            }
        });

        addFunction(new BuiltinFunction("Remove", List.class, int.class) {
            @Override
            public Object call(FunctionArguments args) {
                List<Object> list = args.get(0);
                int value = args.get(1);
                return list.remove(value);
            }
        });

        addFunction(new BuiltinFunction("Remove", Collection.class, Object.class) {
            @Override
            public Object call(FunctionArguments args) {
                Collection<Object> collection = args.get(0);
                Object value = args.get(1);
                return collection.remove(value);
            }
        });

        addFunction(new BuiltinFunction("Length", Object.class) {
            @Override
            public Object call(FunctionArguments args) {
                Object o = args.get(0);
                if (o instanceof CharSequence) {
                    CharSequence sequence = (CharSequence) o;
                    return sequence.length();
                }

                return Array.getLength(o);
            }
        });

        addFunction(new BuiltinFunction("Array", true, ClassObject.class, int.class) {
            @Override
            public Object call(FunctionArguments args) {
                ClassObject type = args.get(0);
                int[] dimensions = args.get(1);

                return Array.newInstance(type.getObject(), dimensions);
            }
        });

        addFunction(new BuiltinFunction("Array", ClassObject.class, List.class) {
            @Override
            public Object call(FunctionArguments args) {
                ClassObject type = args.get(0);
                List list = args.get(1);

                Object array = Array.newInstance(type.getObject(), list.size());
                for (int i = 0; i < list.size(); i++) {
                    Array.set(array, i, list.get(i));
                }

                return array;
            }
        });

        addFunction(new BuiltinFunction("Array", ClassObject.class, List.class, java.util.function.Function.class) {
            @Override
            public Object call(FunctionArguments args) {
                ClassObject type = args.get(0);
                List list = args.get(1);
                java.util.function.Function function = args.get(2);

                Object array = Array.newInstance(type.getObject(), list.size());
                for (int i = 0; i < list.size(); i++) {
                    //noinspection unchecked
                    Array.set(array, i, function.apply(list.get(i)));
                }

                return array;
            }
        });

        addFunction(new BuiltinFunction("Char", Integer.class) {
            @Override
            public Character call(FunctionArguments args) {
                Integer integer = args.get(0);
                return (char) integer.intValue();
            }
        });

        addFunction(new BuiltinFunction("Int", Character.class) {
            @Override
            public Integer call(FunctionArguments args) {
                Character character = args.get(0);
                return (int) character;
            }
        });

        addFunction(new BuiltinFunction("Cast", ClassObject.class, Number.class) {
            @Override
            public Object call(FunctionArguments args) {
                ClassObject object = args.get(0);
                Number number = args.get(1);
                return EvalUtils.cast(number.doubleValue(), EvalUtils.toWrapperClass(object.getObject()).getSimpleName());
            }
        });

        addFunction(new BuiltinFunction("Print", Object.class) {
            @Override
            public Object call(FunctionArguments args) {
                Object object = args.get(0);
                System.out.print(object);
                return NONE_OBJECT;
            }
        });

        addFunction(new BuiltinFunction("Println", Object.class) {
            @Override
            public Object call(FunctionArguments args) {
                Object object = args.get(0);
                System.out.println(object);
                return NONE_OBJECT;
            }
        });

        addFunction(new BuiltinFunction("ExecTime", Function.class) {
            @Override
            public Object call(FunctionArguments args) {
                Function function = args.get(0);

                long time = System.currentTimeMillis();
                Object o = function.call();
                time = System.currentTimeMillis() - time;

                if (o instanceof ScriptError) {
                    return o;
                }

                return time;
            }
        });

        addFunction(new BuiltinFunction("Iota", Integer.class) {
            @Override
            public Object call(FunctionArguments args) {
                Integer i = args.get(0);
                iota = i;
                return i;
            }
        });

        addFunction(new BuiltinFunction("Iota") {
            @Override
            public Object call(FunctionArguments args) {
                return ++iota;
            }
        });
    }

    private Builtin() {
    }

    public static void addFunction(BuiltinFunction function) {
        String name = function.getName();
        if (!builtinMap.containsKey(name)) {
            builtinMap.put(name, new ArrayList<>());
        }

        builtinMap.get(name).add(function);
    }

    public static List<BuiltinFunction> get(String name) {
        return builtinMap.get(name);
    }

    public static boolean contains(String name) {
        return builtinMap.containsKey(name);
    }
}
