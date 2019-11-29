package com.krmnserv321.mcscript.script.eval;

import java.lang.reflect.Member;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.function.Function;
import java.util.function.*;

public final class EvalUtils {
    private EvalUtils() {
    }

    public static Class<?> toPrimitiveClass(Class<?> clazz) {
        if (clazz == Boolean.class) {
            return boolean.class;
        } else if (clazz == Character.class) {
            return char.class;
        } else if (clazz == Byte.class) {
            return byte.class;
        } else if (clazz == Short.class) {
            return short.class;
        } else if (clazz == Integer.class) {
            return int.class;
        } else if (clazz == Long.class) {
            return long.class;
        } else if (clazz == Float.class) {
            return float.class;
        } else if (clazz == Double.class) {
            return double.class;
        } else {
            return clazz;
        }
    }

    public static Class<?> toWrapperClass(Class<?> clazz) {
        if (clazz == boolean.class) {
            return Boolean.class;
        } else if (clazz == char.class) {
            return Character.class;
        } else if (clazz == byte.class) {
            return Byte.class;
        } else if (clazz == short.class) {
            return Short.class;
        } else if (clazz == int.class) {
            return Integer.class;
        } else if (clazz == long.class) {
            return Long.class;
        } else if (clazz == float.class) {
            return Float.class;
        } else if (clazz == double.class) {
            return Double.class;
        } else {
            return clazz;
        }
    }

    public static Object cast(double num, String className) {
        switch (className) {
            case "Byte":
                return (byte) num;
            case "Short":
                return (short) num;
            case "Integer":
                return (int) num;
            case "Long":
                return (long) num;
            case "Float":
                return (float) num;
            case "Double":
                return num;
        }

        throw new IllegalArgumentException("invalid number class");
    }

    public static Object cast(long num, String className) {
        switch (className) {
            case "Byte":
                return (byte) num;
            case "Short":
                return (short) num;
            case "Integer":
                return (int) num;
            case "Long":
                return num;
        }

        throw new IllegalArgumentException("invalid number class");
    }

    public static Environment newEnclosedEnvironment(Environment outer) {
        Environment environment = new Environment(outer.getPublicEnvironment());
        environment.setOuter(outer);
        return environment;
    }

    public static boolean isNotFloatNumber(Object o) {
        return o instanceof Long || o instanceof Integer || o instanceof Short || o instanceof Byte;
    }

    public static Object toJavaFunction(Callable callable, Class<?> type) {
        if (type == BiConsumer.class) {
            return (BiConsumer<Object, Object>) callable::call;
        } else if (type == BiFunction.class) {
            return (BiFunction<Object, Object, Object>) callable::call;
        } else if (type == BinaryOperator.class) {
            return (BinaryOperator<Object>) callable::call;
        } else if (type == BiPredicate.class) {
            return (BiPredicate<Object, Object>) (o, o2) -> (boolean) callable.call(o, o2);
        } else if (type == BooleanSupplier.class) {
            return (BooleanSupplier) () -> (boolean) callable.call();
        } else if (type == Consumer.class) {
            return (Consumer<Object>) callable::call;
        } else if (type == DoubleBinaryOperator.class) {
            return (DoubleBinaryOperator) (o, o2) -> (double) callable.call(o, o2);
        } else if (type == DoubleConsumer.class) {
            return (DoubleConsumer) callable::call;
        } else if (type == DoubleFunction.class) {
            return (DoubleFunction<Object>) callable::call;
        } else if (type == DoublePredicate.class) {
            return (DoublePredicate) (o) -> (boolean) callable.call(o);
        } else if (type == DoubleSupplier.class) {
            return (DoubleSupplier) () -> (double) callable.call();
        } else if (type == DoubleToIntFunction.class) {
            return (DoubleToIntFunction) (o) -> (int) callable.call(o);
        } else if (type == DoubleToLongFunction.class) {
            return (DoubleToLongFunction) (o) -> (long) callable.call(o);
        } else if (type == DoubleUnaryOperator.class) {
            return (DoubleUnaryOperator) (o) -> (double) callable.call(o);
        } else if (type == Function.class) {
            return (Function<Object, Object>) callable::call;
        } else if (type == IntBinaryOperator.class) {
            return (IntBinaryOperator) (o, o2) -> (int) callable.call(o, o2);
        } else if (type == IntConsumer.class) {
            return (IntConsumer) callable::call;
        } else if (type == IntFunction.class) {
            return (IntFunction<Object>) callable::call;
        } else if (type == IntPredicate.class) {
            return (IntPredicate) (o) -> (boolean) callable.call(o);
        } else if (type == IntSupplier.class) {
            return (IntSupplier) () -> (int) callable.call();
        } else if (type == IntToDoubleFunction.class) {
            return (IntToDoubleFunction) (o) -> (double) callable.call(o);
        } else if (type == IntToLongFunction.class) {
            return (IntToLongFunction) (o) -> (long) callable.call(o);
        } else if (type == IntUnaryOperator.class) {
            return (IntUnaryOperator) (o) -> (int) callable.call(o);
        } else if (type == LongBinaryOperator.class) {
            return (LongBinaryOperator) (o, o2) -> (long) callable.call(o);
        } else if (type == LongConsumer.class) {
            return (LongConsumer) callable::call;
        } else if (type == LongFunction.class) {
            return (LongFunction<Object>) callable::call;
        } else if (type == LongPredicate.class) {
            return (LongPredicate) (o) -> (boolean) callable.call(o);
        } else if (type == LongSupplier.class) {
            return (LongSupplier) () -> (long) callable.call();
        } else if (type == LongToDoubleFunction.class) {
            return (LongToDoubleFunction) (o) -> (double) callable.call(o);
        } else if (type == LongToIntFunction.class) {
            return (LongToIntFunction) (o) -> (int) callable.call(o);
        } else if (type == LongUnaryOperator.class) {
            return (LongUnaryOperator) (o) -> (long) callable.call(o);
        } else if (type == ObjDoubleConsumer.class) {
            return (ObjDoubleConsumer<Object>) callable::call;
        } else if (type == ObjIntConsumer.class) {
            return (ObjIntConsumer<Object>) callable::call;
        } else if (type == ObjLongConsumer.class) {
            return (ObjLongConsumer<Object>) callable::call;
        } else if (type == Predicate.class) {
            return (Predicate<Object>) (o) -> (boolean) callable.call(o);
        } else if (type == Supplier.class) {
            return (Supplier<Object>) callable::call;
        } else if (type == ToDoubleBiFunction.class) {
            return (ToDoubleBiFunction<Object, Object>) (o, o2) -> (long) callable.call(o, o2);
        } else if (type == ToDoubleFunction.class) {
            return (ToDoubleFunction<Object>) (o) -> (double) callable.call(o);
        } else if (type == ToIntBiFunction.class) {
            return (ToIntBiFunction<Object, Object>) (o, o2) -> (int) callable.call(o, o2);
        } else if (type == ToIntFunction.class) {
            return (ToIntFunction<Object>) (o) -> (int) callable.call(o);
        } else if (type == ToLongBiFunction.class) {
            return (ToLongBiFunction<Object, Object>) (o, o2) -> (long) callable.call(o, o2);
        } else if (type == ToLongFunction.class) {
            return (ToLongFunction<Object>) (o) -> (long) callable.call(o);
        } else if (type == UnaryOperator.class) {
            return (UnaryOperator<Object>) callable::call;
        } else if (type == org.bukkit.util.Consumer.class) {
            return (org.bukkit.util.Consumer<Object>) callable::call;
        } else if (type == Runnable.class) {
            return (Runnable) callable::call;
        }

        throw new IllegalArgumentException();
    }

    public static String toString(Object o) {
        if (o == null) {
            return "null";
        }

        Class<?> type = o.getClass();
        if (type.isArray()) {
            Class<?> componentType = type.getComponentType();
            if (componentType.isArray()) {
                return Arrays.deepToString((Object[]) o);
            }

            if (componentType == boolean.class) {
                return Arrays.toString((boolean[]) o);
            } else if (componentType == char.class) {
                return Arrays.toString((char[]) o);
            } else if (componentType == byte.class) {
                return Arrays.toString((byte[]) o);
            } else if (componentType == short.class) {
                return Arrays.toString((short[]) o);
            } else if (componentType == int.class) {
                return Arrays.toString((int[]) o);
            } else if (componentType == long.class) {
                return Arrays.toString((long[]) o);
            } else if (componentType == float.class) {
                return Arrays.toString((float[]) o);
            } else if (componentType == double.class) {
                return Arrays.toString((double[]) o);
            } else {
                return Arrays.toString((Object[]) o);
            }
        }

        return o.toString();
    }

    public static String toProperty(String name) {
        int pos = 0;
        for (char c : name.toCharArray()) {
            if (Character.isLowerCase(c)) {
                break;
            }

            pos++;
        }

        return name.substring(0, pos).toLowerCase() + name.substring(pos);
    }

    public static boolean isStatic(Member member) {
        return Modifier.isStatic(member.getModifiers());
    }

    @SuppressWarnings({"BooleanMethodIsAlwaysInverted"})
    public static boolean isAssignable(Class<?> param, Object o) {
        if (param.isAnnotationPresent(FunctionalInterface.class) || param == org.bukkit.util.Consumer.class) {
            if (o instanceof Callable) {
                return true;
            }
        }

        Class<?> type = getClassFromNullable(o);
        if (type == NullClass.class) {
            return !param.isPrimitive();
        }

        if (param.isPrimitive()) {
            type = EvalUtils.toPrimitiveClass(type);
            if (type == param) {
                return true;
            }

            if (type == byte.class) {
                return param == short.class || param == int.class || param == long.class || param == float.class || param == double.class;
            } else if (type == short.class || type == char.class) {
                return param == int.class || param == long.class || param == float.class || param == double.class;
            } else if (type == int.class) {
                return param == long.class || param == float.class || param == double.class;
            } else if (type == long.class) {
                return param == float.class || param == double.class;
            } else if (type == float.class) {
                return param == double.class;
            }

            return false;
        }

        if (type == Byte.class) {
            if (param == Short.class || param == Integer.class || param == Long.class || param == Float.class || param == Double.class) {
                return true;
            }
        } else if (type == Short.class || type == Character.class) {
            if (param == Integer.class || param == Long.class || param == Float.class || param == Double.class) {
                return true;
            }
        } else if (type == Integer.class) {
            if (param == Long.class || param == Float.class || param == Double.class) {
                return true;
            }
        } else if (type == Long.class) {
            if (param == Float.class || param == Double.class) {
                return true;
            }
        } else if (type == Float.class) {
            if (param == Double.class) {
                return true;
            }
        }

        return param.isAssignableFrom(type);
    }

    private static Class<?> getClassFromNullable(Object o) {
        if (o == null) {
            return NullClass.class;
        }
        return o.getClass();
    }

    private static class NullClass {}
}
