package com.krmnserv321.mcscript.script.eval;

import com.krmnserv321.mcscript.MCScript;
import com.krmnserv321.mcscript.script.Program;
import com.krmnserv321.mcscript.script.ast.Arguments;
import com.krmnserv321.mcscript.script.ast.Node;
import com.krmnserv321.mcscript.script.ast.Token;
import com.krmnserv321.mcscript.script.ast.TokenType;
import com.krmnserv321.mcscript.script.ast.expression.*;
import com.krmnserv321.mcscript.script.ast.expression.literal.Literal;
import com.krmnserv321.mcscript.script.ast.expression.literal.ObjectLiteral;
import com.krmnserv321.mcscript.script.ast.statement.*;
import com.krmnserv321.mcscript.script.event.EventAdapter;
import com.krmnserv321.mcscript.script.event.EventFunction;
import com.krmnserv321.mcscript.script.java.*;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.util.Consumer;

import java.lang.reflect.*;
import java.util.*;

import static com.krmnserv321.mcscript.script.eval.EvalUtils.*;

@SuppressWarnings("unchecked")
public final class Evaluator {
    private static final Break BREAK = new Break();
    private static final Continue CONTINUE = new Continue();

    public static final NoneObject NONE_OBJECT = new NoneObject();
    private static final Class<Void> VOID = Void.TYPE;

    private Evaluator() {
    }

    @SuppressWarnings("unused")
    private enum NumberType {
        Integer,
        Long,
        Float,
        Double
    }

    public static Object eval(Environment environment, Node node) {
        if (node instanceof Program) {
            return evalProgram(environment, (Program) node);
        } else if (node instanceof ObjectLiteral) {
            return ((ObjectLiteral) node).getValue();
        } else if (node instanceof ExpressionStatement) {
            return eval(environment, ((ExpressionStatement) node).getExpression());
        } else if (node instanceof Block) {
            return evalBlock(environment, (Block) node);
        } else if (node instanceof DeferStatement) {
            environment.addDefer(environment, ((DeferStatement) node).getStatement());
        } else if (node instanceof ReturnStatement) {
            Object value = eval(environment, ((ReturnStatement) node).getValue());
            if (isError(value)) {
                return value;
            }

            return new ReturnValue(value);
        } else if (node instanceof VarArgExpression) {
            VarArgExpression expression = (VarArgExpression) node;
            Object evaluated = eval(environment, expression.getExpression());
            if (isError(evaluated)) {
                return evaluated;
            }

            if (evaluated != null && evaluated.getClass().isArray()) {
                VarArgObject object = new VarArgObject();
                List<Object> arguments = object.getArguments();
                int length = Array.getLength(evaluated);

                for (int i = 0; i < length; i++) {
                    arguments.add(Array.get(evaluated, i));
                }
                return object;
            }

            return new ScriptError(node.getToken(), evaluated + " is not an array");
        } else if (node instanceof BreakStatement) {
            return BREAK;
        } else if (node instanceof ContinueStatement) {
            return CONTINUE;
        } else if (node instanceof ThrowStatement) {
            ThrowStatement statement = (ThrowStatement) node;
            Object evaluated = eval(environment, statement.getMessage());
            if (isError(evaluated)) {
                return evaluated;
            }

            if (evaluated instanceof String) {
                return new ScriptError(node.getToken(), (String) evaluated);
            }

            return new ScriptError(node.getToken(), evaluated + " is not a string");
        } else if (node instanceof Literal) {
            return ((Literal) node).getValue();
        } else if (node instanceof MultiValueLiteral) {
            MultiValueLiteral literal = (MultiValueLiteral) node;
            int size = literal.getElements().size();
            Object[] elements = new Object[size];
            for (int i = 0; i < size; i++) {
                Expression element = literal.getElements().get(i);
                Object evaluated = eval(environment, element);
                if (isError(evaluated)) {
                    return evaluated;
                }

                elements[i] = evaluated;
            }

            MultiValue multiValue = new MultiValue();
            multiValue.setElements(elements);

            return multiValue;
        } else if (node instanceof ListLiteral) {
            ListLiteral literal = (ListLiteral) node;
            List<Object> list = new ArrayList<>();
            for (Expression element : literal.getElements()) {
                Object e = eval(environment, element);
                if (isError(e)) {
                    return e;
                }

                list.add(e);
            }

            return list;
        } else if (node instanceof MapLiteral) {
            MapLiteral literal = (MapLiteral) node;
            Map<Object, Object> map = new LinkedHashMap<>();
            for (Map.Entry<Expression, Expression> entry : literal.getMap().entrySet()) {
                Object key = eval(environment, entry.getKey());
                if (isError(key)) {
                    return key;
                }

                Object value = eval(environment, entry.getValue());
                if (isError(value)) {
                    return value;
                }

                map.put(key, value);
            }

            return map;
        } else if (node instanceof PairLiteral) {
            PairLiteral pairLiteral = (PairLiteral) node;
            Object f = eval(environment, pairLiteral.getFirst());
            if (isError(f)) {
                return f;
            }

            Object s = eval(environment, pairLiteral.getSecond());
            if (isError(s)) {
                return s;
            }

            return new Pair(f, s);
        } else if (node instanceof StringExpression) {
            StringExpression expression = (StringExpression) node;

            StringBuilder sb = new StringBuilder();
            for (Expression exp : expression.getExpressions()) {
                Object evaluated = eval(environment, exp);
                if (isError(evaluated)) {
                    return evaluated;
                }

                sb.append(evaluated);
            }

            return sb.toString();
        } else if (node instanceof Identifier) {
            String ident = node.getTokenLiteral();

            if (Builtin.contains(ident)) {
                return new BuiltinCallable(ident);
            }

            Object value = environment.get(ident);
            if (value instanceof NullObject) {
                return null;
            }

            if (value == null) {
                Class<?> clazz = environment.checkClassName(ident);
                if (clazz == null) {
                    return new ScriptError(node.getToken(), "identifier not found: " + ident);
                }

                environment.putConstant(ident, ClassObject.of(clazz));
                value = clazz;
            }

            return value;
        } else if (node instanceof Keyword) {
            switch (node.getToken().getType()) {
                case BooleanClass:
                    return ClassObject.of(boolean.class);
                case CharClass:
                    return ClassObject.of(char.class);
                case ByteClass:
                    return ClassObject.of(byte.class);
                case ShortClass:
                    return ClassObject.of(short.class);
                case IntClass:
                    return ClassObject.of(int.class);
                case LongClass:
                    return ClassObject.of(long.class);
                case FloatClass:
                    return ClassObject.of(float.class);
                case DoubleClass:
                    return ClassObject.of(double.class);
            }

            return new ScriptError(node.getToken(), "unknown keyword: " + node);
        } else if (node instanceof WhileStatement) {
            return evalWhileStatement(environment, (WhileStatement) node);
        } else if (node instanceof ForStatement) {
            return evalForStatement(environment, (ForStatement) node);
        } else if (node instanceof ForEachStatement) {
            return evalForEachStatement(environment, (ForEachStatement) node);
        } else if (node instanceof TryStatement) {
            return evalTryStatement(environment, (TryStatement) node);
        } else if (node instanceof CommandDefinition) {
            CommandDefinition definition = (CommandDefinition) node;
            String name = definition.getName().toString();
            Function function = new Function(environment, null, definition.getBody());
            function.getParameters().addAll(definition.getParameters());
            function.setToken(node.getToken());
            ScriptCommand command = new ScriptCommand(name, definition.isVarArgs(), function);

            environment.putConstant(name, command);

            MCScript.registerCommand(command);
            return command;
        } else if (node instanceof ExtensionDefinition) {
            ExtensionDefinition statement = (ExtensionDefinition) node;
            Object type = eval(environment, statement.getName());
            if (isError(type)) {
                return type;
            }

            if (type instanceof ClassObject) {
                PublicEnvironment publicEnvironment = environment.getPublicEnvironment();

                Identifier returnType = statement.getReturnType();
                if (returnType == null) {
                    Function function = new Function(environment, null, statement.getBody());
                    function.getParameters().addAll(statement.getParameters());
                    publicEnvironment.putExtension(((ClassObject) type).getObject(), statement.getExtension().toString(), function);

                    return function;
                } else {
                    Object evaluated = eval(environment, returnType);
                    if (isError(evaluated)) {
                        return evaluated;
                    }

                    if (evaluated instanceof ClassObject) {
                        Function function = new Function(environment, ((ClassObject) evaluated).getObject(), statement.getBody());
                        function.getParameters().addAll(statement.getParameters());
                        publicEnvironment.putExtension(((ClassObject) type).getObject(), statement.getExtension().toString(), function);

                        return function;
                    } else {
                        return new ScriptError(node.getToken(), evaluated + " is not a type");
                    }
                }
            }

            return new ScriptError(node.getToken(), type + " is not a type");
        } else if (node instanceof FunctionDefinition) {
            FunctionDefinition fun = (FunctionDefinition) node;
            Arguments parameters = fun.getParameters();
            Statement body = fun.getBody();

            Function function;

            Identifier returnType = fun.getReturnType();
            if (returnType == null) {
                function = new Function(environment, null, body);
            } else {
                Object evaluated = eval(environment, returnType);
                if (isError(evaluated)) {
                    return evaluated;
                }

                if (evaluated instanceof ClassObject) {
                    function = new Function(environment, ((ClassObject) evaluated).getObject(), body);
                } else {
                    return new ScriptError(node.getToken(), evaluated + " is not a type");
                }
            }
            function.getParameters().addAll(parameters);

            String name = fun.getName().toString();

            if (Character.isUpperCase(name.charAt(0))) {
                PublicEnvironment publicEnvironment = environment.getPublicEnvironment();
                if (!publicEnvironment.isPublic(name)) {
                    function.setEnvironment(new Environment(publicEnvironment));
                    publicEnvironment.put(name, function);
                    return function;
                }

                return new ScriptError(node.getToken(), "cannot overwrite a public object");
            } else {
                environment.putConstant(name, function);
            }

            return function;
        } else if (node instanceof FunctionLiteral) {
            FunctionLiteral fun = (FunctionLiteral) node;
            Arguments parameters = fun.getParameters();
            Statement body = fun.getBody();
            Function function = new Function(environment, null, body, fun instanceof LambdaExpression);
            function.getParameters().addAll(parameters);
            return function;
        } else if (node instanceof EventStatement) {
            EventStatement statement = (EventStatement) node;
            Token token = statement.getEventName().getToken();
            EventAdapter.register(new EventFunction(environment, token, statement.getPriority(), statement.getBody()));
        } else if (node instanceof CallExpression) {
            CallExpression call = (CallExpression) node;
            Expression callFunction = call.getFunction();
            Object fun = eval(environment, callFunction);
            if (isError(fun)) {
                return fun;
            }

            if (fun instanceof ClassObject) {
                Object args = evalJavaArguments(environment, call);
                if (isError(args)) {
                    return args;
                }

                ClassObject classObject = (ClassObject) fun;
                classObject.setToken(node.getToken());

                if (callFunction instanceof Identifier) {
                    if (((Identifier) callFunction).isUnsafe()) {
                        classObject.setUnsafe();
                    }
                }

                return classObject.call((Pair[]) args);
            } else if (fun instanceof ScriptCallable) {
                Object args = evalArguments(environment, call.getArguments());
                if (isError(args)) {
                    return args;
                }

                ScriptCallable function = ((ScriptCallable) fun);
                function.setToken(node.getToken());
                return function.call((Pair[]) args);
            } else if (fun instanceof JavaCallable) {
                Object args = evalJavaArguments(environment, call);
                if (isError(args)) {
                    return args;
                }

                JavaCallable callable = (JavaCallable) fun;
                callable.setToken(node.getToken());
                return callable.call((Pair[]) args);
            }

            return new ScriptError(node.getToken(), fun + " is not a function");
        } else if (node instanceof RunnableLiteral) {
            RunnableLiteral literal = (RunnableLiteral) node;
            Expression count = literal.getCount();
            if (count != null) {
                Object evaluated = eval(environment, count);
                if (isError(evaluated)) {
                    return evaluated;
                }

                if (evaluated instanceof Number) {
                    Function function = new Function(environment, null, literal.getBody());
                    function.setToken(node.getToken());
                    return new RunnableObject(new ScriptRunnable(function, ((Number) evaluated).intValue()));
                }

                return new ScriptError(node.getToken(), evaluated + " is not a number");
            }

            Function function = new Function(environment, null, literal.getBody());
            function.setToken(node.getToken());
            return new RunnableObject(new ScriptRunnable(function, -1));
        } else if (node instanceof AssignExpression) {
            return evalAssignExpression(environment, (AssignExpression) node);
        } else if (node instanceof ConstantExpression) {
            ConstantExpression expression = (ConstantExpression) node;
            Object value = eval(environment, expression.getValue());
            if (isError(value)) {
                return value;
            }

            String name = expression.getName().toString();
            if (Builtin.contains(name)) {
                return new ScriptError(node.getToken(), "cannot overwrite a builtin function");
            }

            environment.putConstant(name, value);
            return value;
        } else if (node instanceof PublicExpression) {
            PublicExpression expression = (PublicExpression) node;
            Object value = eval(environment, expression.getValue());
            if (isError(value)) {
                return value;
            }

            String name = expression.getName().toString();
            if (Builtin.contains(name)) {
                return new ScriptError(node.getToken(), "cannot overwrite a builtin function");
            }

            PublicEnvironment publicEnvironment = environment.getPublicEnvironment();

            if (!publicEnvironment.isPublic(name)) {
                publicEnvironment.put(name, value);
                return value;
            }

            return new ScriptError(node.getToken(), "cannot overwrite a public object");
        } else if (node instanceof ImportStatement) {
            ImportStatement statement = (ImportStatement) node;
            Expression expression = statement.getExpression();

            Object evaluated = eval(environment, expression);
            if (isError(evaluated)) {
                return evaluated;
            } else if (evaluated instanceof ClassObject) {
                String fqcn = expression.toString();
                String name = fqcn.substring(fqcn.lastIndexOf('.') + 1);
                if (Builtin.contains(name)) {
                    return new ScriptError(node.getToken(), "cannot overwrite a builtin function");
                }
                environment.putConstant(name, evaluated);
            } else if (evaluated instanceof PackageObject) {
                PackageObject object = (PackageObject) evaluated;
                environment.addPackage(object.getName());
            } else {
                return new ScriptError(node.getToken(), expression + " is not class");
            }
        } else if (node instanceof NullCheckExpression) {
            NullCheckExpression expression = (NullCheckExpression) node;
            Object nullable = eval(environment, expression.getNullable());
            if (isError(nullable)) {
                return nullable;
            }

            Object evaluated = eval(environment, expression.getExpression());
            if (isError(evaluated)) {
                return evaluated;
            }

            return nullable != null ? nullable : evaluated;
        } else if (node instanceof SwitchExpression) {
            return evalSwitchExpression(environment, (SwitchExpression) node);
        } else if (node instanceof IfExpression) {
            return evalIfExpression(environment, (IfExpression) node);
        } else if (node instanceof TernaryOperator) {
            TernaryOperator operator = (TernaryOperator) node;
            Object condition = eval(environment, operator.getCondition());
            if (isError(condition)) {
                return condition;
            }

            if (condition instanceof Boolean) {
                if ((Boolean) condition) {
                    return eval(environment, operator.getConsequence());
                } else {
                    return eval(environment, operator.getAlternative());
                }
            }

            return new ScriptError(node.getToken(), condition + " is not a boolean");
        } else if (node instanceof AccessExpression) {
            AccessExpression expression = (AccessExpression) node;
            Object le = eval(environment, expression.getLeft());
            if (isError(le)) {
                return le;
            }

            Object ae = eval(environment, expression.getAccessor());
            if (isError(ae)) {
                return ae;
            }

            if (le instanceof List) {
                List<Object> list = (List<Object>) le;
                if (isNotFloatNumber(ae)) {
                    int index = ((Number) ae).intValue();
                    try {
                        if (index < 0) {
                            return list.get(list.size() + index);
                        }
                        return list.get(index);
                    } catch (IndexOutOfBoundsException e) {
                        return new ScriptError(node.getToken(), "index out of bounds: " + index);
                    }
                } else if (ae instanceof Pair) {
                    Pair pair = (Pair) ae;
                    Object first = pair.getFirst();
                    Object second = pair.getSecond();
                    Object f = first == NONE_OBJECT ? 0 : first;
                    Object s = second == NONE_OBJECT ? list.size() : second;

                    if (isNotFloatNumber(f) && isNotFloatNumber(s)) {
                        int size = list.size();
                        int index1 = ((Number) f).intValue();
                        int index2 = ((Number) s).intValue();

                        int i1 = index1 < 0 ? size + index1 : index1;
                        int i2 = index2 < 0 ? size + index2 : index2;

                        try {
                            return list.subList(i1, i2);
                        } catch (IllegalArgumentException e) {
                            return new ScriptError(node.getToken(), e.getMessage());
                        } catch (IndexOutOfBoundsException e) {
                            return new ScriptError(node.getToken(), "index out of bounds: " + f + ":" + s);
                        }
                    }
                }

                return new ScriptError(node.getToken(), ae + " is not an integer");
            } else if (le instanceof String) {
                CharSequence str = (CharSequence) le;
                if (isNotFloatNumber(ae)) {
                    int index = ((Number) ae).intValue();
                    try {
                        if (index < 0) {
                            return str.charAt(str.length() + index);
                        }
                        return str.charAt(index);
                    } catch (StringIndexOutOfBoundsException e) {
                        return new ScriptError(node.getToken(), "index out of bounds: " + index);
                    }
                } else if (ae instanceof Pair) {
                    Pair pair = (Pair) ae;
                    Object first = pair.getFirst();
                    Object second = pair.getSecond();
                    Object f = first == NONE_OBJECT ? 0 : first;
                    Object s = second == NONE_OBJECT ? str.length() : second;

                    if (isNotFloatNumber(f) && isNotFloatNumber(s)) {
                        int length = str.length();
                        int index1 = ((Number) f).intValue();
                        int index2 = ((Number) s).intValue();

                        int i1 = index1 < 0 ? length + index1 : index1;
                        int i2 = index2 < 0 ? length + index2 : index2;

                        try {
                            return str.subSequence(i1, i2);
                        } catch (IllegalArgumentException e) {
                            return new ScriptError(node.getToken(), e.getMessage());
                        } catch (IndexOutOfBoundsException e) {
                            return new ScriptError(node.getToken(), "index out of bounds: " + f + ":" + s);
                        }
                    }
                }

                return new ScriptError(node.getToken(), ae + " is not an integer");
            } else if (le instanceof Map) {
                return ((Map<Object, Object>) le).get(ae);
            } else if (le instanceof ConfigurationSection) {
                if (ae instanceof String) {
                    return ((ConfigurationSection) le).get((String) ae);
                }

                return new ScriptError(node.getToken(), ae + " is not a string");
            } else if (le != null && le.getClass().isArray()) {
                if (isNotFloatNumber(ae)) {
                    int index = ((Number) ae).intValue();
                    try {
                        return Array.get(le, index < 0 ? Array.getLength(le) - index : index);
                    } catch (ArrayIndexOutOfBoundsException e) {
                        return new ScriptError(node.getToken(), "index out of bounds: " + index);
                    }
                }
                return new ScriptError(node.getToken(), expression.getAccessor() + " is not an integer");
            }

            return new ScriptError(node.getToken(), "cannot access: " + le);
        } else if (node instanceof PrefixExpression) {
            PrefixExpression prefix = (PrefixExpression) node;
            String operator = prefix.getOperator();
            Object right = eval(environment, prefix.getRight());
            if (isError(right)) {
                return right;
            }

            return evalPrefixExpression(node.getToken(), operator, right);
        } else if (node instanceof InfixExpression) {
            InfixExpression infix = (InfixExpression) node;
            String operator = infix.getOperator();
            Expression l = infix.getLeft();
            if (operator.equals(".*")) {
                Object le = eval(environment, l);
                if (le instanceof Identifier) {
                    return new PackageObject(l.toString());
                }
                
                return new ScriptError(infix.getToken(), l + " is not an identifier");
            }

            Expression r = infix.getRight();
            if (operator.equals(".")) {
                Object le;
                if (l instanceof InfixExpression) {
                    le = eval(environment, l);
                    if (isError(le)) {
                        return le;
                    }
                } else {
                    le = l;
                }

                if (r instanceof Identifier) {
                    if (le instanceof Node) {
                        Object evaluated = eval(environment, (Node) le);
                        if (isError(evaluated)) {
                            if (le instanceof Identifier) {
                                String name = le + "." + r.getTokenLiteral();
                                try {
                                    PublicEnvironment publicEnvironment = environment.getPublicEnvironment();
                                    return ClassObject.of(publicEnvironment.getClassLoader().loadClass(name));
                                } catch (ClassNotFoundException e) {
                                    return new Identifier(new Token(TokenType.Identifier, name));
                                }
                            }

                            return evaluated;
                        }

                        if (((Identifier) r).isUnsafe()) {
                            return findUnsafeMember(environment, infix.getToken(), evaluated, r.getTokenLiteral());
                        }

                        return findMember(environment, infix.getToken(), evaluated, r.getTokenLiteral());
                    }

                    if (((Identifier) r).isUnsafe()) {
                        return findUnsafeMember(environment, infix.getToken(), le, r.getTokenLiteral());
                    }

                    return findMember(environment, infix.getToken(), le, r.getTokenLiteral());
                }

                return new ScriptError(infix.getToken(), r + " is not an identifier");
            }

            Object left = eval(environment, l);
            if (isError(left)) {
                return left;
            }

            Object right = eval(environment, r);
            if (isError(right)) {
                return right;
            }

            return evalInfixExpression(node.getToken(), operator, left, right);
        }

        return NONE_OBJECT;
    }

    private static Object evalProgram(Environment environment, Program program) {
        Object result = NONE_OBJECT;

        Stack<DeferObject> deferStack = new Stack<>();
        environment.setDeferStack(deferStack);

        for (Statement statement : program.getStatements()) {
            result = eval(environment, statement);

            if (result instanceof ReturnValue) {
                while (!deferStack.isEmpty()) {
                    DeferObject pop = deferStack.pop();
                    eval(pop.getEnvironment(), pop.getStatement());
                }

                return ((ReturnValue) result).getValue();
            } else if (isError(result)) {
                while (!deferStack.isEmpty()) {
                    DeferObject pop = deferStack.pop();
                    eval(pop.getEnvironment(), pop.getStatement());
                }

                return result;
            }
        }

        while (!deferStack.isEmpty()) {
            DeferObject pop = deferStack.pop();
            eval(pop.getEnvironment(), pop.getStatement());
        }

        return result;
    }

    private static Object evalBlock(Environment environment, Block statement) {
        Object result = NONE_OBJECT;

        for (Statement stmt : statement.getStatements()) {
            result = eval(environment, stmt);

            if (result instanceof ReturnValue || result == BREAK || result == CONTINUE || isError(result)) {
                return result;
            }
        }

        return result;
    }

    private static Object evalWhileStatement(Environment environment, WhileStatement statement) {
        Environment enclosedEnv = newEnclosedEnvironment(environment);

        if (statement.isInfinite()) {
            while (true) {
                Object evaluated = eval(enclosedEnv, statement.getBody());
                if (isError(evaluated)) {
                    return evaluated;
                }

                if (evaluated == BREAK) {
                    break;
                }
            }
        } else {
            while (true) {
                Object con = eval(enclosedEnv, statement.getCondition());
                if (isError(con)) {
                    return con;
                }

                if (con instanceof Boolean) {
                    if (!(Boolean) con) {
                        break;
                    }
                } else {
                    return new ScriptError(statement.getToken(), con + " is not a boolean");
                }

                Object evaluated = eval(enclosedEnv, statement.getBody());
                if (isError(evaluated)) {
                    return evaluated;
                }

                if (evaluated == BREAK) {
                    break;
                }
            }
        }

        return NONE_OBJECT;
    }

    private static Object evalForEachStatement(Environment environment, ForEachStatement statement) {
        Environment enclosedEnv = newEnclosedEnvironment(environment);

        Object iterable = eval(enclosedEnv, statement.getIterable());
        if (isError(iterable)) {
            return iterable;
        }

        if (iterable == null) {
            return new ScriptError(statement.getToken(), "iterable is null");
        }

        if (iterable instanceof Iterable) {
            Iterable<Object> itr = (Iterable<Object>) iterable;
            if (statement.getVariable2() == null) {
                for (Object o : itr) {
                    enclosedEnv.put(statement.getVariable1().toString(), o);

                    Object evaluated = eval(enclosedEnv, statement.getBody());
                    if (isError(evaluated)) {
                        return evaluated;
                    }

                    if (evaluated == BREAK) {
                        break;
                    }
                }
            } else {
                int count = 0;
                for (Object o : itr) {
                    enclosedEnv.put(statement.getVariable1().toString(), count);
                    enclosedEnv.put(statement.getVariable2().toString(), o);

                    Object evaluated = eval(enclosedEnv, statement.getBody());
                    if (isError(evaluated)) {
                        return evaluated;
                    }

                    if (evaluated == BREAK) {
                        break;
                    }

                    count++;
                }
            }

            return NONE_OBJECT;
        } else if (iterable instanceof Map) {
            Map<Object, Object> map = (Map<Object, Object>) iterable;
            if (statement.getVariable2() == null) {
                for (Map.Entry<Object, Object> entry : map.entrySet()) {
                    enclosedEnv.put(statement.getVariable1().toString(), entry.getKey());

                    Object evaluated = eval(enclosedEnv, statement.getBody());
                    if (isError(evaluated)) {
                        return evaluated;
                    }

                    if (evaluated == BREAK) {
                        break;
                    }
                }
            } else {
                for (Map.Entry<Object, Object> entry : map.entrySet()) {
                    enclosedEnv.put(statement.getVariable1().toString(), entry.getKey());
                    enclosedEnv.put(statement.getVariable2().toString(), entry.getValue());

                    Object evaluated = eval(enclosedEnv, statement.getBody());
                    if (isError(evaluated)) {
                        return evaluated;
                    }

                    if (evaluated == BREAK) {
                        break;
                    }
                }
            }

            return NONE_OBJECT;
        } else if (iterable.getClass().isArray()) {
            int length = Array.getLength(iterable);
            if (statement.getVariable2() == null) {
                for (int i = 0; i < length; i++) {
                    Object o = Array.get(iterable, i);
                    enclosedEnv.put(statement.getVariable1().toString(), o);

                    Object evaluated = eval(enclosedEnv, statement.getBody());
                    if (isError(evaluated)) {
                        return evaluated;
                    }

                    if (evaluated == BREAK) {
                        break;
                    }
                }
            } else {
                for (int i = 0; i < length; i++) {
                    Object o = Array.get(iterable, i);
                    enclosedEnv.put(statement.getVariable1().toString(), i);
                    enclosedEnv.put(statement.getVariable2().toString(), o);

                    Object evaluated = eval(enclosedEnv, statement.getBody());
                    if (isError(evaluated)) {
                        return evaluated;
                    }

                    if (evaluated == BREAK) {
                        break;
                    }
                }
            }

            return NONE_OBJECT;
        }

        return new ScriptError(statement.getToken(), iterable + " is not an iterable");
    }

    private static Object evalForStatement(Environment environment, ForStatement statement) {
        Environment enclosedEnv = newEnclosedEnvironment(environment);
        Object init = eval(enclosedEnv, statement.getInitExpression());
        if (isError(init)) {
            return init;
        }

        while (true) {
            Object con = eval(enclosedEnv, statement.getCondition());
            if (isError(con)) {
                return con;
            }
            if (con instanceof Boolean) {
                if (!(Boolean) con) {
                    break;
                }
            } else {
                return new ScriptError(statement.getToken(), con + " is not a boolean");
            }

            Object evaluated = eval(enclosedEnv, statement.getBody());
            if (isError(evaluated)) {
                return evaluated;
            }

            if (evaluated == BREAK) {
                break;
            }

            Object e = eval(enclosedEnv, statement.getLoopExpression());
            if (isError(e)) {
                return e;
            }
        }

        return NONE_OBJECT;
    }

    private static Object evalTryStatement(Environment environment, TryStatement statement) {
        Object evaluated = eval(newEnclosedEnvironment(environment), statement.getBody());
        if (isError(evaluated)) {
            Environment enclosedEnv = newEnclosedEnvironment(environment);
            enclosedEnv.putConstant(statement.getError().toString(), new ErrorObject((ScriptError) evaluated));
            Object c = eval(enclosedEnv, statement.getCatchBody());
            if (isError(c)) {
                return c;
            }
        }

        return NONE_OBJECT;
    }

    private static Object evalSwitchExpression(Environment environment, SwitchExpression expression) {
        Environment enclosedEnv = newEnclosedEnvironment(environment);
        Object value = eval(enclosedEnv, expression.getValue());
        if (isError(value)) {
            return value;
        }

        if (value == null) {
            return new ScriptError(expression.getToken(), "value is null");
        }

        Statement def = null;

        for (Map.Entry<Expression, Statement> entry : expression.getCaseMap().entrySet()) {
            Expression caseExpression = entry.getKey();
            if (caseExpression instanceof MultiValueLiteral) {
                List<Expression> caseList = ((MultiValueLiteral) caseExpression).getElements();

                for (Expression exp : caseList) {
                    Object caseVal = eval(enclosedEnv, exp);
                    if (isError(caseVal)) {
                        return caseVal;
                    }

                    if (value.equals(caseVal)) {
                        return eval(newEnclosedEnvironment(enclosedEnv), entry.getValue());
                    }
                }
            } else {
                if (caseExpression == null) {
                    def = entry.getValue();
                } else {
                    Object caseVal = eval(enclosedEnv, caseExpression);
                    if (isError(caseVal)) {
                        return caseVal;
                    }

                    if (value.equals(caseVal)) {
                        return eval(newEnclosedEnvironment(enclosedEnv), entry.getValue());
                    }
                }
            }
        }

        if (def != null) {
            return eval(newEnclosedEnvironment(enclosedEnv), def);
        }

        return NONE_OBJECT;
    }

    private static Object evalIfExpression(Environment environment, IfExpression expression) {
        Environment enclosedEnv = newEnclosedEnvironment(environment);
        Object con = eval(enclosedEnv, expression.getCondition());
        if (isError(con)) {
            return con;
        }

        if (con instanceof Boolean) {
            Boolean bool = ((Boolean) con);
            if (bool) {
                return eval(enclosedEnv, expression.getConsequence());
            } else {
                for (IfExpression exp : expression.getElifList()) {
                    Object o = eval(enclosedEnv, exp.getCondition());
                    if (isError(o)) {
                        return o;
                    }
                    if (o instanceof Boolean) {
                        Boolean b = (Boolean) o;
                        if (b) {
                            return eval(enclosedEnv, exp.getConsequence());
                        }
                    } else {
                        return new ScriptError(expression.getToken(), o + " is not a boolean");
                    }
                }

                Statement alternative = expression.getAlternative();
                if (alternative != null) {
                    return eval(enclosedEnv, alternative);
                }
            }
        } else {
            return new ScriptError(expression.getToken(), con + " is not a boolean");
        }

        return NONE_OBJECT;
    }

    private static Object evalArguments(Environment environment, Arguments arguments) {
        Pair[] result = new Pair[arguments.size()];
        for (int i = 0; i < arguments.size(); i++) {
            PairLiteral pair = arguments.get(i);
            Expression first = pair.getFirst();
            Expression second = pair.getSecond();
            if (second == None.NONE) {
                Object left = eval(environment, first);
                if (isError(left)) {
                    return left;
                }

                result[i] = new Pair(left, None.NONE);
            } else {
                Object right = eval(environment, second);
                if (isError(right)) {
                    return right;
                }

                if (first instanceof Identifier) {
                    result[i] = new Pair(first, right);
                } else {
                    return new ScriptError(pair.getToken(), first + " is not an identifier");
                }
            }
        }
        return result;
    }

    private static Object evalJavaArguments(Environment environment, CallExpression expression) {
        Arguments arguments = expression.getArguments();
        List<Pair> result = new ArrayList<>();

        for (PairLiteral pair : arguments) {
            Expression first = pair.getFirst();
            Expression second = pair.getSecond();

            Object left = eval(environment, first);
            if (isError(left)) {
                return left;
            }

            if (second == None.NONE) {
                if (left instanceof VarArgObject) {
                    VarArgObject object = (VarArgObject) left;
                    for (Object argument : object.getArguments()) {
                        result.add(new Pair(argument, None.NONE));
                    }
                } else {
                    result.add(new Pair(left, None.NONE));
                }
            } else {
                Object right = eval(environment, second);
                if (isError(right)) {
                    return right;
                }

                if (right instanceof ClassObject) {
                    ClassObject type = ((ClassObject) right);
                    if (left instanceof VarArgObject) {
                        VarArgObject object = (VarArgObject) left;
                        for (Object argument : object.getArguments()) {
                            if (!isAssignable(type.getObject(), argument)) {
                                return new ScriptError(expression.getToken(), "argument type mismatch: " + argument + ": " + right);
                            }
                            result.add(new Pair(argument, right));
                        }
                    } else {
                        if (!isAssignable(type.getObject(), left)) {
                            return new ScriptError(expression.getToken(), "argument type mismatch: " + left + ": " + right);
                        }
                        result.add(new Pair(left, right));
                    }
                } else {
                    return new ScriptError(expression.getToken(), right + " is not a type");
                }
            }
        }

        return result.toArray(new Pair[0]);
    }

    public static Object applyFunction(Function function, Object... args) {
        Environment environment = newEnclosedEnvironment(function.getEnvironment());

        Arguments params = function.getParameters();
        int size = params.size();
        if (args.length > size) {
            throw new IllegalArgumentException("argument mismatch: (" + params + ")");
        }

        Object[] values = new Object[size];

        Arrays.fill(values, None.NONE);
        System.arraycopy(args, 0, values, 0, args.length);

        for (int i = 0; i < size; i++) {
            if (values[i] == None.NONE) {
                Expression second = params.get(i).getSecond();
                if (second == None.NONE) {
                    throw new IllegalArgumentException("argument mismatch: (" + params + ")");
                }

                Object evaluated = eval(environment, second);
                if (isError(evaluated)) {
                    return evaluated;
                }

                values[i] = evaluated;
            }
        }

        for (int i = 0; i < size; i++) {
            environment.put(params.get(i).getFirst().toString(), values[i]);
        }

        Stack<DeferObject> deferStack = new Stack<>();
        environment.setDeferStack(deferStack);

        Object result = eval(environment, function.getBody());
        if (result instanceof ReturnValue) {
            while (!deferStack.isEmpty()) {
                DeferObject defer = deferStack.pop();
                eval(defer.getEnvironment(), defer.getStatement());
            }

            return ((ReturnValue) result).getValue();
        }

        while (!deferStack.isEmpty()) {
            DeferObject defer = deferStack.pop();
            eval(defer.getEnvironment(), defer.getStatement());
        }

        return result;
    }

    public static Object applyFunctionWithPair(Function function, Pair... args) {
        Environment environment = newEnclosedEnvironment(function.getEnvironment());

        Arguments params = function.getParameters();
        int size = params.size();
        if (args.length > size) {
            return new ScriptError(function.getToken(), "argument mismatch: (" + params + ")");
        }

        Object[] values = new Object[size];
        Arrays.fill(values, None.NONE);

        for (Pair pair : args) {
            Object first = pair.getFirst();
            Object second = pair.getSecond();
            if (second == None.NONE) {
                for (int i = 0; i < size; i++) {
                    if (values[i] == None.NONE) {
                        values[i] = first;
                        break;
                    }
                }
            } else {
                String name = first.toString();
                for (int i = 0; i < size; i++) {
                    String param = params.get(i).getFirst().toString();
                    if (param.equals(name)) {
                        values[i] = second;
                    }
                }
            }
        }

        for (int i = 0; i < size; i++) {
            if (values[i] == None.NONE) {
                Expression expression = params.get(i).getSecond();
                if (expression == None.NONE) {
                    return new ScriptError(function.getToken(), "argument mismatch: (" + params + ")");
                }

                Object evaluated = eval(environment, expression);
                if (isError(evaluated)) {
                    return evaluated;
                }

                values[i] = evaluated;
            }
        }

        for (int i = 0; i < size; i++) {
            environment.put(params.get(i).getFirst().toString(), values[i]);
        }

        Stack<DeferObject> deferStack = new Stack<>();
        environment.setDeferStack(deferStack);

        Object result = eval(environment, function.getBody());
        if (result instanceof ReturnValue) {
            while (!deferStack.isEmpty()) {
                DeferObject defer = deferStack.pop();
                eval(defer.getEnvironment(), defer.getStatement());
            }

            return ((ReturnValue) result).getValue();
        }

        while (!deferStack.isEmpty()) {
            DeferObject defer = deferStack.pop();
            eval(defer.getEnvironment(), defer.getStatement());
        }

        return result;
    }

    private static Object evalAssignExpression(Environment environment, AssignExpression expression) {
        Token token = expression.getToken();
        String operator = expression.getOperator();
        Expression name = expression.getName();
        Expression value = expression.getValue();
        if (name instanceof MultiValueLiteral) {
            List<Expression> left = ((MultiValueLiteral) name).getElements();
            Object[] ret = new Object[left.size()];

            int leftSize = left.size();
            if (value instanceof MultiValueLiteral) {
                List<Expression> right = ((MultiValueLiteral) value).getElements();
                int rightSize = right.size();

                if (leftSize > rightSize) {
                    Expression lastExp = right.get(rightSize - 1);
                    if (":".equals(operator)) {
                        for (int i = 0; i < leftSize; i++) {
                            Expression l = left.get(i);
                            Object assign;
                            if (i < rightSize) {
                                assign = assign(environment, token, operator, l, right.get(i));

                            } else {
                                assign = assign(environment, token, operator, l, lastExp);

                            }
                            if (isError(assign)) {
                                return assign;
                            }

                            ret[i] = assign;
                        }
                    } else {
                        Object last = eval(environment, lastExp);
                        for (int i = 0; i < leftSize; i++) {
                            Expression l = left.get(i);
                            Object assign;
                            if (i < rightSize - 1) {
                                assign = assign(environment, token, operator, l, right.get(i));
                            } else {
                                assign = assign(environment, token, operator, l, last);
                            }
                            if (isError(assign)) {
                                return assign;
                            }

                            ret[i] = assign;
                        }
                    }
                } else {
                    for (int i = 0; i < leftSize; i++) {
                        Object assign = assign(environment, token, operator, left.get(i), right.get(i));
                        if (isError(assign)) {
                            return assign;
                        }

                        ret[i] = assign;
                    }
                }
            } else {
                Object evaluated = eval(environment, value);
                if (isError(evaluated)) {
                    return evaluated;
                }

                if (evaluated instanceof MultiValue) {
                    Object[] elements = ((MultiValue) evaluated).getElements();
                    int rightSize = elements.length;

                    if (leftSize > rightSize) {
                        Object last = elements[rightSize - 1];
                        for (int i = 0; i < leftSize; i++) {
                            Object assign;
                            if (i < rightSize - 1) {
                                assign = assign(environment, token, operator, left.get(i), elements[i]);
                            } else {
                                assign = assign(environment, token, operator, left.get(i), last);
                            }
                            if (isError(assign)) {
                                return assign;
                            }

                            ret[i] = assign;
                        }
                    } else {
                        for (int i = 0; i < leftSize; i++) {
                            Object assign = assign(environment, token, operator, left.get(i), elements[i]);
                            if (isError(assign)) {
                                return assign;
                            }

                            ret[i] = assign;
                        }
                    }
                } else {
                    if (":".equals(operator)) {
                        Object assign = assign(environment, token, operator, left.get(0), evaluated);
                        if (isError(assign)) {
                            return assign;
                        }

                        ret[0] = assign;

                        for (int i = 1; i < left.size(); i++) {
                            Expression l = left.get(i);
                            assign = assign(environment, token, operator, l, value);
                            if (isError(assign)) {
                                return assign;
                            }

                            ret[i] = assign;
                        }
                    } else {
                        for (int i = 0; i < left.size(); i++) {
                            Expression l = left.get(i);
                            Object assign = assign(environment, token, operator, l, evaluated);
                            if (isError(assign)) {
                                return assign;
                            }

                            ret[i] = assign;
                        }
                    }
                }
            }

            MultiValue multi = new MultiValue();
            multi.setElements(ret);
            return multi;
        } else {
            if (value instanceof MultiValueLiteral) {
                return assign(environment, token, operator, name, ((MultiValueLiteral) value).getElements().get(0));
            }

            return assign(environment, token, operator, name, value);
        }
    }

    private static Object assign(Environment environment, Token token, String operator, Expression name, Object value) {
        if (environment.isConstant(name.toString())) {
            return new ScriptError(token, "cannot assign to a constant");
        }

        Object right;

        if (value instanceof Node) {
            right = eval(environment, (Node) value);
            if (isError(right)) {
                return right;
            }
        } else {
            right = value;
        }

        if (operator == null || operator.equals(":")) {
            if (name instanceof Identifier) {
                String ident = name.toString();
                if (Builtin.contains(ident)) {
                    return new ScriptError(token, "cannot overwrite a builtin function");
                }

                if (environment.getPublicEnvironment().isPublic(ident)) {
                    return new ScriptError(token, "cannot overwrite a public object");
                }

                environment.put(ident, right);
                return right;
            } else if (name instanceof InfixExpression) {
                InfixExpression infix = (InfixExpression) name;
                if (infix.getOperator().equals(".")) {
                    Expression r = infix.getRight();
                    if (r instanceof Identifier) {
                        String field = r.toString();
                        Object evaluated = eval(environment, infix.getLeft());
                        if (isError(evaluated)) {
                            return evaluated;
                        }

                        if (((Identifier) r).isUnsafe()) {
                            return setUnsafeField(infix.getToken(), evaluated, field, right);
                        }

                        return setField(infix.getToken(), evaluated, field, right);
                    }
                }
            } else if (name instanceof AccessExpression) {
                AccessExpression access = (AccessExpression) name;
                Object le = eval(environment, access.getLeft());
                if (isError(le)) {
                    return le;
                }

                Object ae = eval(environment, access.getAccessor());
                if (isError(ae)) {
                    return ae;
                }

                if (le instanceof List) {
                    List<Object> list = (List<Object>) le;
                    if (isNotFloatNumber(ae)) {
                        int index = ((Number) ae).intValue();
                        try {
                            if (index < 0) {
                                list.set(list.size() + index, right);
                            }
                            list.set(index, right);
                            return right;
                        } catch (IndexOutOfBoundsException e) {
                            return new ScriptError(token, "index out of bounds: " + index);
                        }
                    }

                    return new ScriptError(token, access.getAccessor() + " is not an integer");
                } else if (le instanceof Map) {
                    ((Map<Object, Object>) le).put(ae, right);
                    return right;
                } else if (le instanceof ConfigurationSection) {
                    if (ae instanceof String) {
                        ((ConfigurationSection) le).set((String) ae, right);
                        return right;
                    }

                    return new ScriptError(token, access.getAccessor() + " is not a string");
                } else if (le != null && le.getClass().isArray()) {
                    if (isNotFloatNumber(ae)) {
                        int index = ((Number) ae).intValue();
                        try {
                            Array.set(le, index < 0 ? Array.getLength(le) + index : index, right);
                            return right;
                        } catch (ArrayIndexOutOfBoundsException e) {
                            return new ScriptError(token, "index out of bounds: " + index);
                        }
                    }
                    return new ScriptError(token, ae + " is not an integer");
                }

                return new ScriptError(token, "cannot access: " + access.getLeft());
            }

            return new ScriptError(token, name + " is not an identifier");
        }

        Object left = eval(environment, name);
        if (isError(left)) {
            return left;
        }

        Object ret = evalInfixExpression(token, operator, left, right);
        return assign(environment, token, null, name, ret);
    }

    private static Object evalPrefixExpression(Token token, String operator, Object right) {
        switch (operator) {
            case "!":
                if (right instanceof Boolean) {
                    Boolean bool = (Boolean) right;
                    return !bool;
                }
                break;
            case "-":
                if (right instanceof Number) {
                    double num = ((Number) right).doubleValue();
                    return EvalUtils.cast(-num, right.getClass().getSimpleName());
                }
                break;
            case "~":
                if (isNotFloatNumber(right)) {
                    long num = ((Number) right).longValue();
                    return EvalUtils.cast(~num, right.getClass().getSimpleName());
                }
                break;
        }

        return new ScriptError(token, "invalid operator: " + operator + right);
    }

    private static Object evalInfixExpression(Token token, String operator, Object left, Object right) {
        switch (operator) {
            case "==":
                if (left == null) {
                    return right == null;
                }
                return left.equals(right);
            case "!=":
                if (left == null) {
                    return right != null;
                }
                return !left.equals(right);
            case "is":
                return left == right;
            case "isnot":
                return left != right;
        }

        if (right instanceof ClassObject) {
            if (operator.equals("instanceof")) {
                return ((ClassObject) right).getObject().isAssignableFrom(left.getClass());
            }
        }

        if (left instanceof Number && right instanceof Number) {
            if (isNotFloatNumber(left) && isNotFloatNumber(right)) {
                if (left instanceof Integer && right instanceof Integer) {
                    int l = (int) left;
                    int r = (int) right;
                    switch (operator) {
                        case "..":
                            if (l < r) {
                                return new UpRangeObject(l, r);
                            } else {
                                return new DownRangeObject(l, r);
                            }
                        case "until":
                            if (l < r) {
                                return new UpRangeObject(l, r - 1);
                            } else {
                                return new DownRangeObject(l, r + 1);
                            }
                    }
                }
                long l = ((Number) left).longValue();
                long r = ((Number) right).longValue();

                String className = minPrecedence(left.getClass(), right.getClass());

                switch (operator) {
                    case "&":
                        return EvalUtils.cast(l & r, className);
                    case "|":
                        return EvalUtils.cast(l | r, className);
                    case "^":
                        return EvalUtils.cast(l ^ r, className);
                    case "<<":
                        return EvalUtils.cast(l << r, className);
                    case ">>":
                        return EvalUtils.cast(l >> r, className);
                }
            }

            double l = ((Number) left).doubleValue();
            double r = ((Number) right).doubleValue();

            String className = minPrecedence(left.getClass(), right.getClass());

            switch (operator) {
                case "+":
                    return EvalUtils.cast(l + r, className);
                case "-":
                    return EvalUtils.cast(l - r, className);
                case "*":
                    return EvalUtils.cast(l * r, className);
                case "/":
                    if (r == 0) {
                        return new ScriptError(token, "/ by zero");
                    }
                    return EvalUtils.cast(l / r, className);
                case "%":
                    if (r == 0) {
                        return new ScriptError(token, "% by zero");
                    }
                    return EvalUtils.cast(l % r, className);
                case "//":
                    if (r == 0) {
                        return new ScriptError(token, "// by zero");
                    }
                    return (int) (l / r);
                case "**":
                    return EvalUtils.cast(Math.pow(l, r), className);
                case "<":
                    return l < r;
                case ">":
                    return l > r;
                case "<=":
                    return l <= r;
                case ">=":
                    return l >= r;
            }
        } else if (left instanceof String || right instanceof String) {
            if (operator.equals("+")) {
                return left.toString() + right.toString();
            }
        } else if (right instanceof Collection) {
            switch (operator) {
                case "in":
                    return ((Collection<Object>) right).contains(left);
                case "notin":
                    return !((Collection<Object>) right).contains(left);
            }
        } else if (right instanceof Map) {
            switch (operator) {
                case "in":
                    return ((Map<Object, Object>) right).containsKey(left);
                case "notin":
                    return !((Map<Object, Object>) right).containsKey(left);
            }
        } else if (right.getClass().isArray()) {
            int length = Array.getLength(right);
            switch (operator) {
                case "in":
                    for (int i = 0; i < length; i++) {
                        if (Array.get(right, i).equals(left)) {
                            return true;
                        }
                    }
                    return false;
                case "notin":
                    for (int i = 0; i < length; i++) {
                        if (Array.get(right, i).equals(left)) {
                            return false;
                        }
                    }
                    return true;
            }
        } else if (left instanceof Boolean && right instanceof Boolean) {
            boolean l = (boolean) left;
            boolean r = (boolean) right;

            switch (operator) {
                case "||":
                    return l || r;
                case "&&":
                    return l && r;
            }
        } else if (left instanceof RangeObject && right instanceof Integer) {
            if (operator.equals("step")) {
                RangeObject range = (RangeObject) left;
                range.setStep((Integer) right);

                return range;
            }
        } else if (left instanceof Number && right instanceof RangeObject) {
            switch (operator) {
                case "in":
                    return ((RangeObject) right).contains((Number) left);
                case "notin":
                    return !((RangeObject) right).contains((Number) left);
            }

        }

        return new ScriptError(token, "unknown operator: " + left + " " + operator + " " + right);
    }

    private static String minPrecedence(Class<?> left, Class<?> right) {
        String l = left.getSimpleName();
        String r = right.getSimpleName();
        int pre1 = NumberType.valueOf(l).ordinal();
        int pre2 = NumberType.valueOf(r).ordinal();

        if (pre1 > pre2) {
            return l;
        } else {
            return r;
        }
    }

    private static boolean isError(Object object) {
        return object instanceof ScriptError;
    }

    private static Object setField(Token token, Object receiver, String name, Object value) {
        if (receiver instanceof ClassObject) {
            Class<?> clazz = ((ClassObject) receiver).getObject();
            try {
                Field field = clazz.getField(name);
                if (!isStatic(field)) {
                    throw new NoSuchFieldException();
                }

                if (!isAssignable(field.getType(), value)) {
                    return new ScriptError(token, "type mismatch: " + name);
                }

                if (!field.isAccessible()) {
                    field.setAccessible(true);
                }

                field.set(null, value);
                return value;
            } catch (NoSuchFieldException e) {
                Property property = new Property(clazz, name);
                try {
                    Method method = property.getWriteMethod();
                    if (!isStatic(method)) {
                        throw new NoSuchMethodException();
                    }

                    if (!isAssignable(method.getParameters()[0].getType(), value)) {
                        return new ScriptError(token, "type mismatch: " + name);
                    }

                    if (!method.isAccessible()) {
                        method.setAccessible(true);
                    }

                    Object ret = method.invoke(null, value);
                    if (method.getReturnType() == VOID) {
                        return NONE_OBJECT;
                    }

                    return ret;
                } catch (NoSuchMethodException ex) {
                    return new ScriptError(token, "member not found: " + name);
                } catch (IllegalAccessException ex) {
                    return new ScriptError(token, "illegal access: " + name);
                } catch (InvocationTargetException ex) {
                    return new ScriptError(token, "internal error: " + name);
                }
            } catch (IllegalAccessException e) {
                return new ScriptError(token, "illegal access: " + name);
            }
        } else if (receiver instanceof Map) {
            ((Map<Object, Object>) receiver).put(name, value);
            return value;
        } else if (receiver instanceof ConfigurationSection) {
            ((ConfigurationSection) receiver).set(name, value);
            return value;
        }

        Class<?> clazz = receiver.getClass();
        try {
            Field field = clazz.getField(name);
            if (!isAssignable(field.getType(), value)) {
                return new ScriptError(token, "type mismatch: " + name);
            }

            if (!field.isAccessible()) {
                field.setAccessible(true);
            }

            field.set(receiver, value);
            return value;
        } catch (NoSuchFieldException e) {
            Property property = new Property(clazz, name);
            try {
                Method method = property.getWriteMethod();
                if (!isAssignable(method.getParameters()[0].getType(), value)) {
                    return new ScriptError(token, "type mismatch: " + name);
                }

                if (!method.isAccessible()) {
                    method.setAccessible(true);
                }

                Object ret = method.invoke(receiver, value);
                if (method.getReturnType() == VOID) {
                    return NONE_OBJECT;
                }

                return ret;
            } catch (NoSuchMethodException ex) {
                return new ScriptError(token, "member not found: " + name);
            } catch (IllegalAccessException ex) {
                return new ScriptError(token, "illegal access: " + name);
            } catch (InvocationTargetException ex) {
                return new ScriptError(token, "internal error: " + name);
            }
        } catch (IllegalAccessException e) {
            return new ScriptError(token, "illegal access: " + name);
        }
    }

    private static Object setUnsafeField(Token token, Object receiver, String name, Object value) {
        if (receiver instanceof ClassObject) {
            Class<?> clazz = ((ClassObject) receiver).getObject();
            try {
                Field field = clazz.getDeclaredField(name);
                if (!isStatic(field)) {
                    return new NoSuchFieldException();
                }

                if (!isAssignable(field.getType(), value)) {
                    return new ScriptError(token, "type mismatch: " + name);
                }

                if (!field.isAccessible()) {
                    field.setAccessible(true);
                }

                field.set(null, value);
                return value;
            } catch (NoSuchFieldException e) {
                Property property = new Property(clazz, name);
                try {
                    Method method = property.getDeclaredWriteMethod();
                    if (!isStatic(method)) {
                        return new NoSuchMethodException();
                    }

                    if (!isAssignable(method.getParameters()[0].getType(), value)) {
                        return new ScriptError(token, "type mismatch: " + name);
                    }

                    if (!method.isAccessible()) {
                        method.setAccessible(true);
                    }

                    Object ret = method.invoke(null, value);
                    if (method.getReturnType() == VOID) {
                        return NONE_OBJECT;
                    }

                    return ret;
                } catch (NoSuchMethodException ex) {
                    return new ScriptError(token, "member not found: " + name);
                } catch (IllegalAccessException ex) {
                    return new ScriptError(token, "illegal access: " + name);
                } catch (InvocationTargetException ex) {
                    return new ScriptError(token, "internal error: " + name);
                }
            } catch (IllegalAccessException e) {
                return new ScriptError(token, "illegal access: " + name);
            }
        } else if (receiver instanceof Map) {
            ((Map<Object, Object>) receiver).put(name, value);
            return value;
        } else if (receiver instanceof ConfigurationSection) {
            ((ConfigurationSection) receiver).set(name, value);
            return value;
        }

        Class<?> clazz = receiver.getClass();
        try {
            Field field = clazz.getDeclaredField(name);
            if (!isAssignable(field.getType(), value)) {
                return new ScriptError(token, "type mismatch: " + name);
            }

            if (!field.isAccessible()) {
                field.setAccessible(true);
            }

            field.set(receiver, value);
            return value;
        } catch (NoSuchFieldException e) {
            Property property = new Property(clazz, name);
            try {
                Method method = property.getDeclaredWriteMethod();
                if (!isAssignable(method.getParameters()[0].getType(), value)) {
                    return new ScriptError(token, "type mismatch: " + name);
                }

                if (!method.isAccessible()) {
                    method.setAccessible(true);
                }

                Object ret = method.invoke(receiver, value);
                if (method.getReturnType() == VOID) {
                    return NONE_OBJECT;
                }

                return ret;
            } catch (NoSuchMethodException ex) {
                return new ScriptError(token, "member not found: " + name);
            } catch (IllegalAccessException ex) {
                return new ScriptError(token, "illegal access: " + name);
            } catch (InvocationTargetException ex) {
                return new ScriptError(token, "internal error: " + name);
            }
        } catch (IllegalAccessException e) {
            return new ScriptError(token, "illegal access: " + name);
        }
    }

    private static Object findMember(Environment environment, Token token, Object receiver, String member) {
        if (receiver == null) {
            return new ScriptError(token, "receiver is null");
        }

        boolean mapOrSection = false;

        if (receiver instanceof ClassObject) {
            Class<?> clazz = ((ClassObject) receiver).getObject();
            if (member.equals("class")) {
                return clazz;
            }
            String errorName = clazz.getSimpleName() + "." + member;
            try {
                Field field = clazz.getField(member);
                if (!isStatic(field)) {
                    throw new NoSuchFieldException();
                }

                if (!field.isAccessible()) {
                    field.setAccessible(true);
                }

                return field.get(null);
            } catch (NoSuchFieldException ex) {
                try {
                    Property property = new Property(clazz, member);
                    Method readMethod = property.getReadMethod();
                    if (!isStatic(readMethod)) {
                        throw new NoSuchMethodException();
                    }

                    if (!readMethod.isAccessible()) {
                        readMethod.setAccessible(true);
                    }

                    return readMethod.invoke(null);
                } catch (NoSuchMethodException exception) {
                    int count = 0;
                    Method method = null;
                    for (Method m : clazz.getMethods()) {
                        if (isStatic(m)) {
                            if (m.getName().equals(member)) {
                                method = m;
                                count++;

                                if (count == 2) {
                                    break;
                                }
                            }
                        }
                    }

                    if (count == 1) {
                        return new CallableMethod(null, method);
                    } else if (method != null) {
                        return new ClassMethod(clazz, member, false);
                    }

                    return new ScriptError(token, "member not found: " + errorName);
                } catch (IllegalAccessException exception) {
                    return new ScriptError(token, "illegal access: " + errorName);
                } catch (InvocationTargetException exception) {
                    return new ScriptError(token, "internal error: " + errorName);
                }
            } catch (IllegalAccessException ex) {
                return new ScriptError(token, "internal error: " + errorName);
            }
        } else if (receiver instanceof Map) {
            Map<Object, Object> map = (Map<Object, Object>) receiver;

            if (map.containsKey(member)) {
                return map.get(member);
            }
            mapOrSection = true;
        } else if (receiver instanceof ConfigurationSection) {
            ConfigurationSection section = (ConfigurationSection) receiver;

            if (section.contains(member)) {
                return section.get(member);
            }
            mapOrSection = true;
        }

        Class<?> clazz = receiver.getClass();
        String errorName = clazz.getSimpleName() + "." + member;
        try {
            Field field = clazz.getField(member);
            if (!field.isAccessible()) {
                field.setAccessible(true);
            }

            return field.get(receiver);
        } catch (NoSuchFieldException ex) {
            try {
                Property property = new Property(clazz, member);
                Method readMethod = property.getReadMethod();
                if (!readMethod.isAccessible()) {
                    readMethod.setAccessible(true);
                }

                return readMethod.invoke(receiver);
            } catch (NoSuchMethodException exception) {
                PublicEnvironment publicEnvironment = environment.getPublicEnvironment();
                Function extension = publicEnvironment.getExtension(clazz, member);
                if (extension != null) {
                    extension.getEnvironment().putConstant("this", receiver);
                    return extension;
                }

                int count = 0;
                Method method = null;
                for (Method m : clazz.getMethods()) {
                    if (m.getName().equals(member)) {
                        method = m;
                        count++;

                        if (count == 2) {
                            break;
                        }
                    }
                }

                if (count == 1) {
                    return new CallableMethod(receiver, method);
                } else if (method != null) {
                    return new InstanceMethod(publicEnvironment, receiver, member, false);
                }

                if (mapOrSection) {
                    return null;
                }

                return new ScriptError(token, "member not found: " + errorName);
            } catch (IllegalAccessException exception) {
                return new ScriptError(token, "illegal access: " + errorName);
            } catch (InvocationTargetException exception) {
                return new ScriptError(token, "internal error: " + errorName);
            }
        } catch (IllegalAccessException ex) {
            return new ScriptError(token, "internal error: " + errorName);
        }
    }

    private static Object findUnsafeMember(Environment environment, Token token, Object receiver, String member) {
        if (receiver == null) {
            return new ScriptError(token, "receiver is null");
        }

        boolean mapOrSection = false;

        if (receiver instanceof ClassObject) {
            Class<?> clazz = ((ClassObject) receiver).getObject();
            if (member.equals("class")) {
                return clazz;
            }
            String errorName = clazz.getSimpleName() + "." + member;
            try {
                Field field = clazz.getDeclaredField(member);
                if (!isStatic(field)) {
                    throw new NoSuchFieldException();
                }

                if (!field.isAccessible()) {
                    field.setAccessible(true);
                }

                return field.get(null);
            } catch (NoSuchFieldException ex) {
                try {
                    Property property = new Property(clazz, member);
                    Method readMethod = property.getDeclaredReadMethod();
                    if (!isStatic(readMethod)) {
                        throw new NoSuchMethodException();
                    }

                    if (readMethod.isAccessible()) {
                        readMethod.setAccessible(true);
                    }

                    return readMethod.invoke(null);
                } catch (NoSuchMethodException exception) {
                    int count = 0;
                    Method method = null;
                    for (Method m : clazz.getDeclaredMethods()) {
                        if (isStatic(m)) {
                            if (m.getName().equals(member)) {
                                method = m;
                                count++;

                                if (count == 2) {
                                    break;
                                }
                            }
                        }
                    }

                    if (count == 1) {
                        return new CallableMethod(null, method);
                    } else if (method != null) {
                        return new ClassMethod(clazz, member, true);
                    }

                    return new ScriptError(token, "member not found: " + errorName);
                } catch (IllegalAccessException exception) {
                    return new ScriptError(token, "illegal access: " + errorName);
                } catch (InvocationTargetException exception) {
                    return new ScriptError(token, "internal error: " + errorName);
                }
            } catch (IllegalAccessException ex) {
                return new ScriptError(token, "internal error: " + errorName);
            }
        } else if (receiver instanceof Map) {
            Map<Object, Object> map = (Map<Object, Object>) receiver;

            if (map.containsKey(member)) {
                return map.get(member);
            }
            mapOrSection = true;
        } else if (receiver instanceof ConfigurationSection) {
            ConfigurationSection section = (ConfigurationSection) receiver;

            if (section.contains(member)) {
                return section.get(member);
            }
            mapOrSection = true;
        }

        Class<?> clazz = receiver.getClass();
        String errorName = clazz.getSimpleName() + "." + member;
        try {
            Field field = clazz.getDeclaredField(member);
            if (!field.isAccessible()) {
                field.setAccessible(true);
            }

            return field.get(receiver);
        } catch (NoSuchFieldException ex) {
            try {
                Property property = new Property(clazz, member);
                Method readMethod = property.getDeclaredReadMethod();
                if (readMethod.isAccessible()) {
                    readMethod.setAccessible(true);
                }

                return readMethod.invoke(receiver);
            } catch (NoSuchMethodException exception) {
                PublicEnvironment publicEnvironment = environment.getPublicEnvironment();
                Function extension = publicEnvironment.getExtension(clazz, member);
                if (extension != null) {
                    extension.getEnvironment().putConstant("this", receiver);
                    return extension;
                }

                int count = 0;
                Method method = null;
                for (Method m : clazz.getDeclaredMethods()) {
                    if (m.getName().equals(member)) {
                        method = m;
                        count++;

                        if (count == 2) {
                            break;
                        }
                    }
                }

                if (count == 1) {
                    return new CallableMethod(receiver, method);
                } else if (method != null) {
                    return new InstanceMethod(publicEnvironment, receiver, member, true);
                }

                if (mapOrSection) {
                    return null;
                }

                return new ScriptError(token, "member not found: " + errorName);
            } catch (IllegalAccessException exception) {
                return new ScriptError(token, "illegal access: " + errorName);
            } catch (InvocationTargetException exception) {
                return new ScriptError(token, "internal error: " + errorName);
            }
        } catch (IllegalAccessException ex) {
            return new ScriptError(token, "internal error: " + errorName);
        }
    }

    public static Object newInstance(ClassObject object, Pair... args) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, InstantiationException, ArgumentMismatchException {
        Constructor<?>[] constructors;
        if (object.isUnsafe()) {
            constructors = object.getObject().getConstructors();
        } else {
            constructors = object.getObject().getDeclaredConstructors();
        }

        if (constructors.length == 0) {
            throw new NoSuchMethodException();
        }

        for (Constructor<?> constructor : constructors) {
            if (checkParameters(constructor, args)) {
                if (!constructor.isAccessible()) {
                    constructor.setAccessible(true);
                }
                return constructor.newInstance(convertArgs(object.getToken(), constructor, args));
            }
        }

        throw new ArgumentMismatchException();
    }

    public static Object callMethod(ClassMethod method, Pair... args) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, ArgumentMismatchException {
        Class<?> clazz = method.getReceiver();
        String name = method.getName();

        boolean found = false;

        Method[] methods = method.isUnsafe() ? clazz.getDeclaredMethods() : clazz.getMethods();

        for (Method m : methods) {
            if (!isStatic(m) || !m.getName().equals(name)) {
                continue;
            }

            found = true;

            if (checkParameters(m, args)) {
                if (!m.isAccessible()) {
                    m.setAccessible(true);
                }

                Object ret = m.invoke(null, convertArgs(method.getToken(), m, args));
                if (m.getReturnType() == VOID) {
                    return NONE_OBJECT;
                }

                return ret;
            }
        }

        if (found) {
            throw new ArgumentMismatchException();
        }

        throw new NoSuchMethodException();
    }

    public static Object callMethod(InstanceMethod method, Pair... args) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, ArgumentMismatchException {
        Object receiver = method.getReceiver();
        String name = method.getName();

        Class<?> type = receiver.getClass();
        boolean found = false;
        Method[] methods = method.isUnsafe() ? type.getDeclaredMethods() : type.getMethods();
        for (Method m : methods) {
            if (!m.getName().equals(name)) {
                continue;
            }

            found = true;

            if (checkParameters(m, args)) {
                if (!m.isAccessible()) {
                    m.setAccessible(true);
                }

                Object ret = m.invoke(receiver, convertArgs(method.getToken(), m, args));
                if (m.getReturnType() == VOID) {
                    return NONE_OBJECT;
                }

                return ret;
            }
        }

        if (found) {
            throw new ArgumentMismatchException();
        }

        throw new NoSuchMethodException();
    }

    public static Object callMethod(CallableMethod callable, Pair... args) throws InvocationTargetException, IllegalAccessException {
        Object receiver = callable.getReceiver();
        Method method = callable.getMethod();

        if (!method.isAccessible()) {
            method.setAccessible(true);
        }

        Object ret = method.invoke(receiver, convertArgs(callable.getToken(), method, args));
        if (method.getReturnType() == VOID) {
            return NONE_OBJECT;
        }

        return ret;
    }

    public static Object callBuiltin(BuiltinCallable callable, Pair... args) throws ArgumentMismatchException {
        String name = callable.getName();
        for (BuiltinFunction function : Builtin.get(name)) {
            if (checkParameters(function, args)) {
                try {
                    return function.call(new FunctionArguments(convertArgs(callable.getToken(), function, args)));
                } catch (Exception e) {
                    return new ScriptError(callable.getToken(), e);
                }
            }
        }

        throw new ArgumentMismatchException();
    }

    private static boolean checkParameters(Executable executable, Pair... args) {
        Parameter[] params = executable.getParameters();
        if (executable.isVarArgs()) {
            if (params.length - 1 > args.length) {
                return false;
            }
        } else if (params.length != args.length) {
            return false;
        }

        for (int i = 0; i < params.length; i++) {
            Class<?> param = params[i].getType();
            if (params[i].isVarArgs()) {
                for (int j = i; j < args.length; j++) {
                    Pair pair = args[j];
                    Object o = pair.getSecond();
                    if (o != None.NONE) {
                        if (param.getComponentType() != ((ClassObject) o).getObject()) {
                            return false;
                        }
                    } else if (!isAssignable(param.getComponentType(), pair.getFirst())) {
                        return false;
                    }
                }
                return true;
            }

            Pair pair = args[i];
            Object o = pair.getSecond();
            if (o != None.NONE) {
                if (param != ((ClassObject) o).getObject()) {
                    return false;
                }
            } else if (!isAssignable(param, pair.getFirst())) {
                return false;
            }
        }
        return true;
    }

    private static boolean checkParameters(BuiltinFunction function, Pair... args) {
        Class<?>[] params = function.getParameters();
        if (function.isVarArgs()) {
            if (params.length - 1 > args.length) {
                return false;
            }
        } else if (params.length != args.length) {
            return false;
        }

        for (int i = 0; i < params.length; i++) {
            Class<?> param = params[i];
            if (i + 1 == params.length && function.isVarArgs()) {
                for (int j = i; j < args.length; j++) {
                    Pair pair = args[j];
                    Object o = pair.getSecond();
                    Class<?> componentType = pair.getFirst().getClass().isArray() ? param.getComponentType() : param;
                    if (o != None.NONE) {
                        if (componentType != ((ClassObject) o).getObject()) {
                            return false;
                        }
                    } else if (!isAssignable(componentType, pair.getFirst())) {
                        return false;
                    }
                }
                return true;
            }

            Pair pair = args[i];
            Object o = pair.getSecond();
            if (o != None.NONE) {
                if (param != ((ClassObject) o).getObject()) {
                    return false;
                }
            } else if (!isAssignable(param, pair.getFirst())) {
                return false;
            }
        }
        return true;
    }

    private static Object[] convertArgs(Token token, Executable executable, Pair... args) {
        Object[] values = Arrays.stream(args).map(Pair::getFirst).toArray();
        Parameter[] params = executable.getParameters();
        if (executable.isVarArgs()) {
            Object[] result = new Object[params.length];

            for (int i = 0; i < params.length; i++) {
                if (params[i].isVarArgs()) {
                    Class<?> type = params[i].getType().getComponentType();
                    Object varArgs = Array.newInstance(type, values.length - i);
                    if (type.isAnnotationPresent(FunctionalInterface.class) || type == Consumer.class) {
                        for (int j = 0; i + j < values.length; j++) {
                            Object o = values[i + j];
                            if (o instanceof Callable) {
                                Callable callable = (Callable) o;
                                callable.setToken(token);
                                Array.set(varArgs, j, toJavaFunction((Callable) o, type));
                            }
                        }
                        result[i] = varArgs;
                        return result;
                    }

                    for (int j = 0; i + j < values.length; j++) {
                        Array.set(varArgs, j, values[i + j]);
                    }
                    result[i] = varArgs;

                    return result;
                }

                Class<?> type = params[i].getType();
                if (type.isAnnotationPresent(FunctionalInterface.class) || type == Consumer.class) {
                    if (values[i] instanceof Callable) {
                        Callable callable = (Callable) values[i];
                        callable.setToken(token);
                        result[i] = toJavaFunction((Callable) values[i], type);
                        continue;
                    }
                }

                result[i] = values[i];
            }
            return result;
        }

        for (int i = 0; i < params.length; i++) {
            Class<?> type = params[i].getType();
            if (type.isAnnotationPresent(FunctionalInterface.class) || type == Consumer.class) {
                if (values[i] instanceof Callable) {
                    Callable callable = (Callable) values[i];
                    callable.setToken(token);
                    values[i] = toJavaFunction((Callable) values[i], type);
                }
            }
        }
        return values;
    }

    private static Object[] convertArgs(Token token, BuiltinFunction function, Pair... args) {
        Object[] values = Arrays.stream(args).map(Pair::getFirst).toArray();
        Class<?>[] params = function.getParameters();
        if (function.isVarArgs()) {
            Object[] result = new Object[params.length];

            for (int i = 0; i < params.length; i++) {
                if (i + 1 == params.length && function.isVarArgs()) {
                    Class<?> type = values[i].getClass().isArray() ? params[i].getComponentType() : params[i];
                    Object varArgs = Array.newInstance(type, values.length - i);
                    if (type.isAnnotationPresent(FunctionalInterface.class) || type == Consumer.class) {
                        for (int j = 0; i + j < values.length; j++) {
                            Object o = values[i + j];
                            if (o instanceof Callable) {
                                Callable callable = (Callable) o;
                                callable.setToken(token);
                                Array.set(varArgs, j, toJavaFunction(callable, type));
                            } else {
                                Array.set(varArgs, j, o);
                            }
                        }
                        result[i] = varArgs;
                        return result;
                    }

                    for (int j = 0; i + j < values.length; j++) {
                        Array.set(varArgs, j, autoCast(type, values[i + j]));
                    }
                    result[i] = varArgs;

                    return result;
                }

                Class<?> type = params[i];
                if (type.isAnnotationPresent(FunctionalInterface.class) || type == Consumer.class) {
                    if (values[i] instanceof Callable) {
                        Callable callable = (Callable) values[i];
                        callable.setToken(token);
                        result[i] = toJavaFunction(callable, type);
                        continue;
                    }
                }

                result[i] = autoCast(type, values[i]);
            }
            return result;
        }

        for (int i = 0; i < params.length; i++) {
            Class<?> type = params[i];
            if (type.isAnnotationPresent(FunctionalInterface.class) || type == Consumer.class) {
                if (values[i] instanceof Callable) {
                    Callable callable = (Callable) values[i];
                    callable.setToken(token);
                    values[i] = toJavaFunction((Callable) values[i], type);
                }
            }
        }
        return values;
    }

    private static Object autoCast(Class<?> param, Object o) {
        Class<?> type = o.getClass();

        if (type == Byte.class) {
            if (param == Short.class || param == Integer.class || param == Long.class || param == Float.class || param == Double.class) {
                return EvalUtils.cast((double) o, param.getSimpleName());
            }
        } else if (type == Short.class || type == Character.class) {
            if (param == Integer.class || param == Long.class || param == Float.class || param == Double.class) {
                return EvalUtils.cast((double) o, param.getSimpleName());
            }
        } else if (type == Integer.class || type == Long.class) {
            if (param == Float.class || param == Double.class) {
                return EvalUtils.cast((double) o, param.getSimpleName());
            }
        }

        return o;
    }

    private static class Property {
        private final Class<?> receiver;
        private final String property;

        private Property(Class<?> receiver, String property) {
            this.receiver = receiver;
            this.property = property;
        }

        private Method getReadMethod() throws NoSuchMethodException {
            for (Method method : receiver.getMethods()) {
                if (method.getParameterCount() == 0) {
                    String name = method.getName();
                    if (name.startsWith("is")) {
                        if (name.equals(property)) {
                            return method;
                        }
                    }

                    if (name.startsWith("get")) {
                        if (toProperty(name.substring(3)).equals(property)) {
                            return method;
                        }
                    }
                }
            }

            throw new NoSuchMethodException();
        }

        private Method getDeclaredReadMethod() throws NoSuchMethodException {
            for (Method method : receiver.getDeclaredMethods()) {
                if (method.getParameterCount() == 0) {
                    String name = method.getName();
                    if (name.startsWith("is")) {
                        if (name.equals(property)) {
                            return method;
                        }
                    }

                    if (name.startsWith("get")) {
                        if (toProperty(name.substring(3)).equals(property)) {
                            return method;
                        }
                    }
                }
            }

            throw new NoSuchMethodException();
        }

        private Method getWriteMethod() throws NoSuchMethodException {
            for (Method method : receiver.getMethods()) {
                if (method.getParameterCount() == 1) {
                    String name = method.getName();
                    if (name.startsWith("set")) {
                        if (toProperty(name.substring(3)).equals(property)) {
                            return method;
                        }
                    }
                }
            }

            throw new NoSuchMethodException();
        }

        private Method getDeclaredWriteMethod() throws NoSuchMethodException {
            for (Method method : receiver.getDeclaredMethods()) {
                if (method.getParameterCount() == 1) {
                    String name = method.getName();
                    if (name.startsWith("set")) {
                        if (toProperty(name.substring(3)).equals(property)) {
                            return method;
                        }
                    }
                }
            }

            throw new NoSuchMethodException();
        }
    }

    private static class Break {
        @Override
        public String toString() {
            return "break";
        }
    }

    private static class Continue {
        @Override
        public String toString() {
            return "continue";
        }
    }
}
