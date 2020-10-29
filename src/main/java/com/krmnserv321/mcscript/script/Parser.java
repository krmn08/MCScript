package com.krmnserv321.mcscript.script;

import com.krmnserv321.mcscript.script.ast.Arguments;
import com.krmnserv321.mcscript.script.ast.Precedence;
import com.krmnserv321.mcscript.script.ast.Token;
import com.krmnserv321.mcscript.script.ast.TokenType;
import com.krmnserv321.mcscript.script.ast.expression.*;
import com.krmnserv321.mcscript.script.ast.expression.literal.*;
import com.krmnserv321.mcscript.script.ast.statement.*;
import org.bukkit.event.EventPriority;

import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Parser {
    private static final Map<TokenType, Precedence> precedenceMap = new HashMap<TokenType, Precedence>() {
        {
            put(TokenType.Assign, Precedence.Assign);
            put(TokenType.ColonAssign, Precedence.Assign);
            put(TokenType.PlusAssign, Precedence.Assign);
            put(TokenType.MinusAssign, Precedence.Assign);
            put(TokenType.MultiAssign, Precedence.Assign);
            put(TokenType.DivideAssign, Precedence.Assign);
            put(TokenType.IntDivideAssign, Precedence.Assign);
            put(TokenType.ModAssign, Precedence.Assign);
            put(TokenType.PowerAssign, Precedence.Assign);
            put(TokenType.AndAssign, Precedence.Assign);
            put(TokenType.OrAssign, Precedence.Assign);
            put(TokenType.XorAssign, Precedence.Assign);
            put(TokenType.LeftShiftAssign, Precedence.Assign);
            put(TokenType.RightShiftAssign, Precedence.Assign);
            put(TokenType.Comma, Precedence.Comma);
            put(TokenType.Arrow, Precedence.Lambda);
            put(TokenType.Colon, Precedence.Pair);
            put(TokenType.Question, Precedence.Question);
            put(TokenType.NullCheck, Precedence.NullCheck);
            put(TokenType.Or, Precedence.Or);
            put(TokenType.And, Precedence.And);
            put(TokenType.Equal, Precedence.Equals);
            put(TokenType.NotEqual, Precedence.Equals);
            put(TokenType.Is, Precedence.Equals);
            put(TokenType.IsNot, Precedence.Equals);
            put(TokenType.In, Precedence.Equals);
            put(TokenType.NotIn, Precedence.Equals);
            put(TokenType.InstanceOf, Precedence.LessGreater);
            put(TokenType.LessThanEqual, Precedence.LessGreater);
            put(TokenType.GreaterThanEqual, Precedence.LessGreater);
            put(TokenType.LessThan, Precedence.LessGreater);
            put(TokenType.GreaterThan, Precedence.LessGreater);
            put(TokenType.Range, Precedence.Range);
            put(TokenType.Until, Precedence.Range);
            put(TokenType.Step, Precedence.Range);
            put(TokenType.BitOr, Precedence.BitOr);
            put(TokenType.BitXor, Precedence.BitXor);
            put(TokenType.BitAnd, Precedence.BitAnd);
            put(TokenType.LeftShift, Precedence.Shift);
            put(TokenType.RightShift, Precedence.Shift);
            put(TokenType.Plus, Precedence.Sum);
            put(TokenType.Minus, Precedence.Sum);
            put(TokenType.Multi, Precedence.Product);
            put(TokenType.Divide, Precedence.Product);
            put(TokenType.IntDivide, Precedence.Product);
            put(TokenType.Mod, Precedence.Product);
            put(TokenType.Power, Precedence.Power);
            put(TokenType.LBracket, Precedence.Access);
            put(TokenType.LParen, Precedence.Call);
            put(TokenType.Dot, Precedence.Dot);
            put(TokenType.Wildcard, Precedence.Dot);
        }
    };

    private static final Pattern TEMPLATE_PATTERN = Pattern.compile("\\{.+?}");
    private static final Pattern IDENTITY_PATTERN = Pattern.compile("[a-zA-Z]+[a-zA-Z0-9_]*");

    private final Lexer lexer;

    private Token curToken;
    private Token peekToken;

    private final List<String> errors = new ArrayList<>();

    private final Map<TokenType, PrefixParseFunction> prefixParseMap = new HashMap<>();
    private final Map<TokenType, InfixParseFunction> infixParseMap = new HashMap<>();

    public Parser(Lexer lexer) {
        this.lexer = lexer;

        prefixParseMap.put(TokenType.Identifier, this::parseIdentifier);
        prefixParseMap.put(TokenType.BooleanClass, this::parseKeyword);
        prefixParseMap.put(TokenType.CharClass, this::parseKeyword);
        prefixParseMap.put(TokenType.ByteClass, this::parseKeyword);
        prefixParseMap.put(TokenType.ShortClass, this::parseKeyword);
        prefixParseMap.put(TokenType.IntClass, this::parseKeyword);
        prefixParseMap.put(TokenType.LongClass, this::parseKeyword);
        prefixParseMap.put(TokenType.FloatClass, this::parseKeyword);
        prefixParseMap.put(TokenType.DoubleClass, this::parseKeyword);

        prefixParseMap.put(TokenType.Constant, this::parseConstantExpression);
        prefixParseMap.put(TokenType.Public, this::parsePublicExpression);

        prefixParseMap.put(TokenType.Null, this::parseNullLiteral);
        prefixParseMap.put(TokenType.Character, this::parseCharacterLiteral);
        prefixParseMap.put(TokenType.String, this::parseStringExpression);
        prefixParseMap.put(TokenType.Integer, this::parseIntegerLiteral);
        prefixParseMap.put(TokenType.Long, this::parseLongLiteral);
        prefixParseMap.put(TokenType.Double, this::parseDoubleLiteral);
        prefixParseMap.put(TokenType.Float, this::parseFloatLiteral);
        prefixParseMap.put(TokenType.True, this::parseBooleanLiteral);
        prefixParseMap.put(TokenType.False, this::parseBooleanLiteral);

        prefixParseMap.put(TokenType.LBracket, this::parseListLiteral);
        prefixParseMap.put(TokenType.LBrace, this::parseMapLiteral);

        prefixParseMap.put(TokenType.BitNot, this::parsePrefixExpression);
        prefixParseMap.put(TokenType.Not, this::parsePrefixExpression);
        prefixParseMap.put(TokenType.Minus, this::parsePrefixExpression);

        prefixParseMap.put(TokenType.VarArg, this::parseVarArgExpression);

        prefixParseMap.put(TokenType.LParen, this::parseGroupedExpression);

        prefixParseMap.put(TokenType.Switch, this::parseSwitchExpression);
        prefixParseMap.put(TokenType.If, this::parseIfExpression);
        prefixParseMap.put(TokenType.Runnable, this::parseRunnableLiteral);
        prefixParseMap.put(TokenType.Function, this::parseFunctionLiteral);
        prefixParseMap.put(TokenType.Command, this::parseCommandDefinition);

        infixParseMap.put(TokenType.Assign, this::parseAssignExpression);
        infixParseMap.put(TokenType.ColonAssign, this::parseAssignExpression);
        infixParseMap.put(TokenType.PlusAssign, this::parseAssignExpression);
        infixParseMap.put(TokenType.MinusAssign, this::parseAssignExpression);
        infixParseMap.put(TokenType.MultiAssign, this::parseAssignExpression);
        infixParseMap.put(TokenType.DivideAssign, this::parseAssignExpression);
        infixParseMap.put(TokenType.IntDivideAssign, this::parseAssignExpression);
        infixParseMap.put(TokenType.ModAssign, this::parseAssignExpression);
        infixParseMap.put(TokenType.PowerAssign, this::parseAssignExpression);
        infixParseMap.put(TokenType.AndAssign, this::parseAssignExpression);
        infixParseMap.put(TokenType.OrAssign, this::parseAssignExpression);
        infixParseMap.put(TokenType.XorAssign, this::parseAssignExpression);
        infixParseMap.put(TokenType.LeftShiftAssign, this::parseAssignExpression);
        infixParseMap.put(TokenType.RightShiftAssign, this::parseAssignExpression);

        infixParseMap.put(TokenType.Arrow, this::parseLambdaExpression);
        infixParseMap.put(TokenType.Comma, this::parseMultiValueLiteral);

        infixParseMap.put(TokenType.Dot, this::parseInfixExpression);
        infixParseMap.put(TokenType.Range, this::parseInfixExpression);
        infixParseMap.put(TokenType.Plus, this::parseInfixExpression);
        infixParseMap.put(TokenType.Minus, this::parseInfixExpression);
        infixParseMap.put(TokenType.Multi, this::parseInfixExpression);
        infixParseMap.put(TokenType.Divide, this::parseInfixExpression);
        infixParseMap.put(TokenType.IntDivide, this::parseInfixExpression);
        infixParseMap.put(TokenType.Mod, this::parseInfixExpression);
        infixParseMap.put(TokenType.Power, this::parseInfixExpression);

        infixParseMap.put(TokenType.BitAnd, this::parseInfixExpression);
        infixParseMap.put(TokenType.BitOr, this::parseInfixExpression);
        infixParseMap.put(TokenType.BitXor, this::parseInfixExpression);

        infixParseMap.put(TokenType.LeftShift, this::parseInfixExpression);
        infixParseMap.put(TokenType.RightShift, this::parseInfixExpression);

        infixParseMap.put(TokenType.And, this::parseInfixExpression);
        infixParseMap.put(TokenType.Or, this::parseInfixExpression);

        infixParseMap.put(TokenType.Equal, this::parseInfixExpression);
        infixParseMap.put(TokenType.NotEqual, this::parseInfixExpression);
        infixParseMap.put(TokenType.LessThanEqual, this::parseInfixExpression);
        infixParseMap.put(TokenType.GreaterThanEqual, this::parseInfixExpression);

        infixParseMap.put(TokenType.LessThan, this::parseInfixExpression);
        infixParseMap.put(TokenType.GreaterThan, this::parseInfixExpression);

        infixParseMap.put(TokenType.InstanceOf, this::parseInfixExpression);
        infixParseMap.put(TokenType.Is, this::parseInfixExpression);
        infixParseMap.put(TokenType.IsNot, this::parseInfixExpression);
        infixParseMap.put(TokenType.In, this::parseInfixExpression);
        infixParseMap.put(TokenType.NotIn, this::parseInfixExpression);
        infixParseMap.put(TokenType.Until, this::parseInfixExpression);
        infixParseMap.put(TokenType.Step, this::parseInfixExpression);

        infixParseMap.put(TokenType.Colon, this::parsePairLiteral);
        infixParseMap.put(TokenType.Question, this::parseTernaryOperator);
        infixParseMap.put(TokenType.NullCheck, this::parseNullCheckExpression);
        infixParseMap.put(TokenType.LParen, this::parseCallExpression);
        infixParseMap.put(TokenType.LBracket, this::parseAccessExpression);

        infixParseMap.put(TokenType.Wildcard, this::parseWildcard);

        nextToken();
        nextToken();
    }

    public Program parseProgram() {
        List<Statement> statements = new ArrayList<>();

        while (curToken.getType() != TokenType.EOF) {
            if (curToken.getType() == TokenType.Semicolon) {
                nextToken();
                continue;
            }

            Statement statement = parseStatement();
            if (statement != null) {
                statements.add(statement);
            }
            nextToken();
        }

        Program program = new Program();
        statements.sort(Comparator.comparing(statement -> {
            if (statement instanceof ImportStatement) {
                return 0;
            } else if (statement instanceof ExpressionStatement) {
                return ((ExpressionStatement) statement).getExpression() instanceof FunctionDefinition ? 0 : 1;
            } else {
                return 1;
            }
        }));
        program.getStatements().addAll(statements);

        return program;
    }

    public List<String> getErrors() {
        return errors;
    }

    private void nextToken() {
        curToken = peekToken;
        peekToken = lexer.nextToken();
    }

    private Statement parseStatement() {
        switch (curToken.getType()) {
            case Import:
                return parseImportStatement();
            case Return:
                return parseReturnStatement();
            case Event:
                return parseEventStatement();
            case While:
                return parseWhileStatement();
            case For:
                return parseForStatement();
            case Break:
                return new BreakStatement(curToken);
            case Continue:
                return new ContinueStatement(curToken);
            case Throw:
                return parseThrowStatement();
            case Try:
                return parseTryStatement();
            case Defer:
                return parseDeferStatement();
            default:
                return parseExpressionStatement();
        }
    }

    private ImportStatement parseImportStatement() {
        Token token = curToken;
        nextToken();
        return new ImportStatement(token, parseExpression(Precedence.Lowest));
    }

    private ReturnStatement parseReturnStatement() {
        Token token = curToken;
        TokenType type = peekToken.getType();
        if (type != TokenType.Semicolon && type != TokenType.RBrace && type != TokenType.EOF) {
            nextToken();
            return new ReturnStatement(token, parseExpression(Precedence.Lowest));
        }
        return new ReturnStatement(token, None.NONE);
    }

    private DeferStatement parseDeferStatement() {
        Token token = curToken;
        nextToken();
        return new DeferStatement(token, parseStatement());
    }

    private EventStatement parseEventStatement() {
        Token token = curToken;
        if (!expectPeek(TokenType.Identifier)) {
            return null;
        }

        Identifier ident = new Identifier(curToken);

        EventPriority priority = EventPriority.NORMAL;

        if (peekToken.getType() == TokenType.LParen) {
            nextToken();
            nextToken();
            try {
                priority = EventPriority.valueOf(curToken.toString());
            } catch (IllegalArgumentException e) {
                addError(curToken + " is not an event priority");
                return null;
            }
            if (!expectPeek(TokenType.RParen)) {
                return null;
            }
        }

        if (!expectPeek(TokenType.LBrace)) {
            return null;
        }

        return new EventStatement(token, ident, priority, parseBlock());
    }

    private WhileStatement parseWhileStatement() {
        Token token = curToken;
        if (peekToken.getType() == TokenType.LBrace) {
            nextToken();
            Block body = parseBlock();
            return new WhileStatement(token, null, body, true);
        }

        nextToken();
        Expression condition = parseExpression(Precedence.Lowest);

        if (!expectPeek(TokenType.LBrace)) {
            return null;
        }

        Block body = parseBlock();

        return new WhileStatement(token, condition, body, false);
    }

    private Statement parseForStatement() {
        Token token = curToken;

        nextToken();
        if (peekToken.getType() == TokenType.Comma) {
            Identifier left = new Identifier(curToken);

            nextToken();
            nextToken();

            Identifier right = new Identifier(curToken);

            if (!expectPeek(TokenType.In)) {
                return null;
            }
            nextToken();

            Expression iterable = parseExpression(Precedence.Lowest);

            if (!expectPeek(TokenType.LBrace)) {
                return null;
            }

            Block body = parseBlock();

            return new ForEachStatement(token, left, right, iterable, body);
        } else if (peekToken.getType() == TokenType.In) {
            Identifier left = new Identifier(curToken);

            nextToken();
            nextToken();

            Expression iterable = parseExpression(Precedence.Lowest);

            if (!expectPeek(TokenType.LBrace)) {
                return null;
            }

            Block body = parseBlock();

            return new ForEachStatement(token, left, null, iterable, body);
        } else {
            Expression expression = parseExpression(Precedence.Lowest);
            if (!expectPeek(TokenType.Semicolon)) {
                return null;
            }
            nextToken();

            Expression condition = parseExpression(Precedence.Lowest);

            if (!expectPeek(TokenType.Semicolon)) {
                return null;
            }
            nextToken();

            Expression loopExpression = parseExpression(Precedence.Lowest);

            if (!expectPeek(TokenType.LBrace)) {
                return null;
            }

            Block body = parseBlock();

            return new ForStatement(token, expression, condition, loopExpression, body);
        }
    }

    private ThrowStatement parseThrowStatement() {
        Token token = curToken;
        nextToken();
        return new ThrowStatement(token, parseExpression(Precedence.Lowest));
    }

    private TryStatement parseTryStatement() {
        Token token = curToken;
        nextToken();
        Block body = parseBlock();
        if (!expectPeek(TokenType.Catch)) {
            return null;
        }

        if (!expectPeek(TokenType.Identifier)) {
            return null;
        }
        Identifier error = new Identifier(curToken);

        if (!expectPeek(TokenType.LBrace)) {
            return null;
        }
        return new TryStatement(token, body, error, parseBlock());
    }

    private ExpressionStatement parseExpressionStatement() {
        return new ExpressionStatement(curToken, parseExpression(Precedence.Lowest));
    }

    private Expression parseExpression(Precedence precedence) {
        PrefixParseFunction prefix = prefixParseMap.get(curToken.getType());
        if (prefix == null) {
            addError("no prefix parse function for " + curToken.getType() + " found");
            return null;
        }

        Expression leftExp = prefix.get();

        while (peekToken.getType() != TokenType.Semicolon && precedence.ordinal() < peekPrecedence().ordinal()) {
            InfixParseFunction infix = infixParseMap.get(peekToken.getType());
            if (infix == null) {
                return leftExp;
            }

            nextToken();
            leftExp = infix.apply(leftExp);
        }

        return leftExp;
    }

    private Identifier parseIdentifier() {
        Token token = curToken;
        if (peekToken.getType() == TokenType.Not) {
            nextToken();
            return new Identifier(token, true);
        }

        return new Identifier(token);
    }

    private Keyword parseKeyword() {
        return new Keyword(curToken);
    }

    private NullLiteral parseNullLiteral() {
        return new NullLiteral(curToken);
    }

    private CharacterLiteral parseCharacterLiteral() {
        return new CharacterLiteral(curToken, curToken.toString().charAt(0));
    }

    private IntegerLiteral parseIntegerLiteral() {
        try {
            String num = curToken.toString();

            if (num.length() > 1 && num.charAt(0) == '0') {
                switch (num.charAt(1)) {
                    case 'x':
                        return new IntegerLiteral(curToken, Integer.parseInt(num.substring(2), 16));
                    case 'b':
                        return new IntegerLiteral(curToken, Integer.parseInt(num.substring(2), 2));
                    default:
                        return new IntegerLiteral(curToken, Integer.parseInt(num.substring(1), 8));
                }
            }

            return new IntegerLiteral(curToken, Integer.parseInt(num));
        } catch (NumberFormatException e) {
            addError("could not parse " + curToken + " as integer");
            return null;
        }
    }

    private LongLiteral parseLongLiteral() {
        try {
            return new LongLiteral(curToken, Long.parseLong(curToken.toString()));
        } catch (NumberFormatException e) {
            addError("could not parse " + curToken + " as long");
            return null;
        }
    }

    private DoubleLiteral parseDoubleLiteral() {
        try {
            return new DoubleLiteral(curToken, Double.parseDouble(curToken.toString()));
        } catch (NumberFormatException e) {
            addError("could not parse " + curToken + " as double");
            return null;
        }
    }

    private FloatLiteral parseFloatLiteral() {
        try {
            return new FloatLiteral(curToken, Float.parseFloat(curToken.toString()));
        } catch (NumberFormatException e) {
            addError("could not parse " + curToken + " as double");
            return null;
        }
    }

    private BooleanLiteral parseBooleanLiteral() {
        return new BooleanLiteral(curToken, curToken.getType() == TokenType.True);
    }

    private ListLiteral parseListLiteral() {
        ListLiteral literal = new ListLiteral(curToken);
        List<Expression> list = parseExpressionList();
        if (list == null) {
            return null;
        }
        literal.getElements().addAll(list);
        return literal;
    }

    private MapLiteral parseMapLiteral() {
        MapLiteral literal = new MapLiteral(curToken);
        skipSemicolon();

        if (peekToken.getType() == TokenType.RBrace) {
            nextToken();
            nextToken();
            return literal;
        }

        Map<Expression, Expression> map = literal.getMap();
        nextToken();
        skipSemicolon();

        Expression expression = parseExpression(Precedence.Comma);
        skipSemicolon();

        if (expression instanceof PairLiteral) {
            PairLiteral pair = (PairLiteral) expression;
            map.put(pair.getFirst(), pair.getSecond());
        } else {
            addError(expression + " is not a pair");
        }
        skipSemicolon();

        while (peekToken.getType() == TokenType.Comma) {
            nextToken();
            skipSemicolon();
            nextToken();
            Expression exp = parseExpression(Precedence.Comma);
            if (exp instanceof PairLiteral) {
                PairLiteral pair = (PairLiteral) exp;
                map.put(pair.getFirst(), pair.getSecond());
            } else {
                addError(expression + " is not a pair");
            }
        }

        skipSemicolon();

        if (!expectPeek(TokenType.RBrace)) {
            return null;
        }
        return literal;
    }

    private StringExpression parseStringExpression() {
        Token token = curToken;
        StringExpression expression = new StringExpression(token);
        String literal = token.toString();

        StringBuilder value = new StringBuilder(literal);
        List<Expression> expressions = expression.getExpressions();

        Matcher matcher;
        int start = value.indexOf("$");
        while (start != -1) {
            String str = value.substring(start + 1);
            matcher = TEMPLATE_PATTERN.matcher(str);
            String group;
            Lexer lexer;
            if (matcher.lookingAt()) {
                group = matcher.group();
                lexer = new Lexer(token.getPath(), group.substring(1, group.length() - 1));
            } else {
                matcher = IDENTITY_PATTERN.matcher(str);
                if (!matcher.lookingAt()) {
                    expressions.add(new StringLiteral(token, value.substring(0, start + 1)));
                    value.delete(0, start + 1);
                    start = value.indexOf("$");
                    continue;
                }

                group = matcher.group();
                lexer = new Lexer(token.getPath(), group);
            }

            lexer.setLineCount(token.getLineNumber());
            Parser parser = new Parser(lexer);

            expressions.add(new StringLiteral(token, value.substring(0, start)));
            value.delete(0, start);

            Expression exp = parser.parseExpression(Precedence.Lowest);
            if (parser.errors.isEmpty()) {
                expressions.add(exp);
                value.delete(0, group.length() + 1);
            } else {
                errors.addAll(parser.errors);
                return null;
            }

            start = value.indexOf("$");
        }

        expressions.add(new StringLiteral(token, value.toString()));
        return expression;
    }

    private PrefixExpression parsePrefixExpression() {
        Token token = curToken;
        String operator = token.toString();

        nextToken();

        return new PrefixExpression(token, operator, parseExpression(Precedence.Prefix));
    }

    private VarArgExpression parseVarArgExpression() {
        Token token = curToken;
        nextToken();
        return new VarArgExpression(token, parseExpression(Precedence.Lowest));
    }

    private ConstantExpression parseConstantExpression() {
        Token token = curToken;
        if (!expectPeek(TokenType.Identifier)) {
            return null;
        }

        Identifier ident = new Identifier(curToken);

        if (!expectPeek(TokenType.Assign)) {
            return null;
        }
        nextToken();

        return new ConstantExpression(token, ident, parseExpression(Precedence.Lowest));
    }

    private PublicExpression parsePublicExpression() {
        Token token = curToken;
        if (!expectPeek(TokenType.Identifier)) {
            return null;
        }

        Identifier ident = new Identifier(curToken);

        if (!expectPeek(TokenType.Assign)) {
            return null;
        }
        nextToken();

        return new PublicExpression(token, ident, parseExpression(Precedence.Lowest));
    }

    private AssignExpression parseAssignExpression(Expression left) {
        Token token = curToken;
        nextToken();
        switch (token.toString()) {
            case ":=":
                return new AssignExpression(token, left, parseExpression(Precedence.Lowest), ":");
            case "+=":
                return new AssignExpression(token, left, parseExpression(Precedence.Lowest), "+");
            case "-=":
                return new AssignExpression(token, left, parseExpression(Precedence.Lowest), "-");
            case "*=":
                return new AssignExpression(token, left, parseExpression(Precedence.Lowest), "*");
            case "/=":
                return new AssignExpression(token, left, parseExpression(Precedence.Lowest), "/");
            case "//=":
                return new AssignExpression(token, left, parseExpression(Precedence.Lowest), "//");
            case "%=":
                return new AssignExpression(token, left, parseExpression(Precedence.Lowest), "%");
            case "**=":
                return new AssignExpression(token, left, parseExpression(Precedence.Lowest), "**");
            case "&=":
                return new AssignExpression(token, left, parseExpression(Precedence.Lowest), "&");
            case "|=":
                return new AssignExpression(token, left, parseExpression(Precedence.Lowest), "|");
            case "^=":
                return new AssignExpression(token, left, parseExpression(Precedence.Lowest), "^");
            case "<<=":
                return new AssignExpression(token, left, parseExpression(Precedence.Lowest), "<<");
            case ">>=":
                return new AssignExpression(token, left, parseExpression(Precedence.Lowest), ">>");
            default:
                return new AssignExpression(token, left, parseExpression(Precedence.Lowest));
        }
    }

    private LambdaExpression parseLambdaExpression(Expression left) {
        Token token = curToken;
        Arguments arguments = new Arguments();
        Statement body;

        if (peekToken.getType() == TokenType.LBrace) {
            nextToken();
            body = parseBlock();
        } else {
            nextToken();
            body = parseStatement();
        }

        if (left instanceof MultiValueLiteral) {
            MultiValueLiteral literal = (MultiValueLiteral) left;
            for (Expression element : literal.getElements()) {
                if (element instanceof PairLiteral) {
                    arguments.add((PairLiteral) element);
                } else {
                    arguments.add(new PairLiteral(new Token(TokenType.Colon, token.getLineNumber(), ":"), element, None.NONE));
                }
            }
        } else {
            if (left instanceof PairLiteral) {
                arguments.add((PairLiteral) left);
            } else {
                arguments.add(new PairLiteral(new Token(TokenType.Colon, token.getLineNumber(), ":"), left, None.NONE));
            }
        }

        LambdaExpression lambda = new LambdaExpression(token, body);
        lambda.getParameters().addAll(arguments);

        return lambda;
    }

    private MultiValueLiteral parseMultiValueLiteral(Expression left) {
        MultiValueLiteral literal = new MultiValueLiteral(curToken);
        List<Expression> elements = new ArrayList<>();
        elements.add(left);

        skipSemicolon();
        nextToken();

        elements.add(parseExpression(Precedence.Comma));

        while (peekToken.getType() == TokenType.Comma) {
            nextToken();
            skipSemicolon();
            nextToken();
            elements.add(parseExpression(Precedence.Comma));
        }

        literal.getElements().addAll(elements);
        return literal;
    }

    private AccessExpression parseAccessExpression(Expression left) {
        Token token = curToken;
        nextToken();
        Expression accessor;
        if (curToken.getType() == TokenType.Colon) {
            if (peekToken.getType() == TokenType.RBracket) {
                accessor = new PairLiteral(curToken, None.NONE, None.NONE);
            } else {
                nextToken();
                accessor = new PairLiteral(token, None.NONE, parseExpression(Precedence.Lowest));
            }
        } else {
            Expression exp = parseExpression(Precedence.Pair);
            if (peekToken.getType() == TokenType.Colon) {
                nextToken();
                Token t = curToken;
                if (peekToken.getType() == TokenType.RBracket) {
                    accessor = new PairLiteral(t, exp, None.NONE);
                } else {
                    nextToken();
                    accessor = new PairLiteral(t, exp, parseExpression(Precedence.Lowest));
                }
            } else {
                accessor = exp;
            }
        }

        if (!expectPeek(TokenType.RBracket)) {
            return null;
        }

        return new AccessExpression(token, left, accessor);
    }

    private InfixExpression parseInfixExpression(Expression left) {
        Token token = curToken;
        String operator = token.toString();

        Precedence precedence = curPrecedence();
        skipSemicolon();

        nextToken();

        return new InfixExpression(token, operator, left, parseExpression(precedence));
    }

    private Expression parseGroupedExpression() {
        nextToken();
        if (curToken.getType() == TokenType.RParen) {
            if (!expectPeek(TokenType.Arrow)) {
                return null;
            }

            return parseLambdaExpression(new MultiValueLiteral(curToken));
        }

        Expression expression = parseExpression(Precedence.Lowest);
        if (!expectPeek(TokenType.RParen)) {
            return null;
        }

        return expression;
    }

    private SwitchExpression parseSwitchExpression() {
        Token token = curToken;

        nextToken();
        Expression value = parseExpression(Precedence.Lowest);

        if (!expectPeek(TokenType.LBrace)) {
            return null;
        }
        skipSemicolon();

        Map<Expression, Statement> caseMap = new LinkedHashMap<>();

        while (peekToken.getType() != TokenType.RBrace) {
            nextToken();
            Expression caseExpression = null;
            if (curToken.getType() != TokenType.Default) {
                caseExpression = parseExpression(Precedence.Lambda);
            }

            if (!expectPeek(TokenType.Arrow)) {
                return null;
            }
            nextToken();

            Statement statement;

            if (curToken.getType() == TokenType.LBrace) {
                statement = parseBlock();
            } else {
                statement = parseStatement();
            }

            caseMap.put(caseExpression, statement);
            skipSemicolon();
        }

        if (!expectPeek(TokenType.RBrace)) {
            return null;
        }

        SwitchExpression expression = new SwitchExpression(token, value);
        expression.getCaseMap().putAll(caseMap);
        return expression;
    }

    private IfExpression parseIfExpression() {
        Token token = curToken;

        nextToken();
        Expression condition = parseExpression(Precedence.Lowest);

        if (!expectPeek(TokenType.LBrace)) {
            return null;
        }

        Block consequence = parseBlock();

        skipSemicolon();

        List<IfExpression> elifList = new ArrayList<>();
        while (peekToken.getType() == TokenType.Elif) {
            nextToken();

            elifList.add(parseElif());
            skipSemicolon();
        }

        if (peekToken.getType() == TokenType.Else) {
            nextToken();
            if (!expectPeek(TokenType.LBrace)) {
                return null;
            }

            Block alternative = parseBlock();

            IfExpression expression = new IfExpression(token, condition, consequence, alternative);
            expression.getElifList().addAll(elifList);
            return expression;
        }

        IfExpression expression = new IfExpression(token, condition, consequence);
        expression.getElifList().addAll(elifList);
        return expression;
    }

    private IfExpression parseElif() {
        Token token = curToken;

        nextToken();
        Expression condition = parseExpression(Precedence.Lowest);

        if (!expectPeek(TokenType.LBrace)) {
            return null;
        }

        Block consequence = parseBlock();

        return new IfExpression(token, condition, consequence);
    }

    private Block parseBlock() {
        Block block = new Block(curToken);
        List<Statement> statements = new ArrayList<>();

        skipSemicolon();

        while (peekToken.getType() != TokenType.RBrace && peekToken.getType() != TokenType.EOF) {
            nextToken();
            Statement stmt = parseStatement();
            if (stmt != null) {
                statements.add(stmt);
            }
            skipSemicolon();
        }

        if (!expectPeek(TokenType.RBrace)) {
            return null;
        }

        block.getStatements().addAll(statements);

        return block;
    }

    private RunnableLiteral parseRunnableLiteral() {
        Token token = curToken;
        Expression count = null;
        if (peekToken.getType() == TokenType.LParen) {
            nextToken();
            nextToken();
            count = parseExpression(Precedence.Lowest);
            if (!expectPeek(TokenType.RParen)) {
                return null;
            }
        }

        if (!expectPeek(TokenType.LBrace)) {
            return null;
        }

        Block body = parseBlock();

        return new RunnableLiteral(token, count, body);
    }

    @SuppressWarnings("Duplicates")
    private FunctionLiteral parseFunctionLiteral() {
        Token token = curToken;
        Identifier name = null;
        Identifier ext = null;
        if (peekToken.getType() == TokenType.Identifier) {
            nextToken();
            name = new Identifier(curToken);
            if (peekToken.getType() == TokenType.Dot) {
                nextToken();
                if (!expectPeek(TokenType.Identifier)) {
                    return null;
                }

                ext = new Identifier(curToken);
            }
        }

        if (!expectPeek(TokenType.LParen)) {
            return null;
        }

        Arguments params = parseArguments();

        if (params == null) {
            return null;
        }

        for (PairLiteral param : params) {
            if (!(param.getFirst() instanceof Identifier)) {
                addError(param.getFirst() + " is not an identifier");
                return null;
            }
        }

        Identifier returnType = null;
        if (peekToken.getType() == TokenType.Colon) {
            nextToken();
            nextToken();
            returnType = parseIdentifier();
        }

        if (!expectPeek(TokenType.LBrace)) {
            return null;
        }
        Statement statement = parseBlock();

        FunctionLiteral function;
        if (name == null) {
            function = new FunctionLiteral(token, statement);
        } else {
            if (ext == null) {
                function = new FunctionDefinition(token, name, returnType, statement);
            } else {
                function = new ExtensionDefinition(token, name, ext, returnType, statement);
            }
        }

        function.getParameters().addAll(params);

        return function;
    }

    @SuppressWarnings("Duplicates")
    private CommandDefinition parseCommandDefinition() {
        Token token = curToken;
        boolean isVarArgs;
        if (peekToken.getType() == TokenType.BitNot) {
            nextToken();
            isVarArgs = true;
        } else {
            isVarArgs = false;
        }

        if (!expectPeek(TokenType.Identifier)) {
            return null;
        }
        Identifier name = new Identifier(curToken);

        if (!expectPeek(TokenType.LParen)) {
            return null;
        }

        Arguments params = parseArguments();

        if (params == null) {
            return null;
        }

        for (PairLiteral param : params) {
            if (!(param.getFirst() instanceof Identifier)) {
                addError(param.getFirst() + " is not an identifier");
                return null;
            }
        }

        if (!expectPeek(TokenType.LBrace)) {
            return null;
        }

        Block body = parseBlock();

        CommandDefinition function = new CommandDefinition(token, isVarArgs, name, body);
        function.getParameters().addAll(params);

        return function;
    }

    private NullCheckExpression parseNullCheckExpression(Expression left) {
        Token token = curToken;
        nextToken();
        Expression expression = parseExpression(Precedence.Lowest);
        if (expression == null) {
            return null;
        }

        return new NullCheckExpression(token, left, expression);
    }

    private InfixExpression parseWildcard(Expression left) {
        Token token = curToken;
        return new InfixExpression(token, token.toString(), left, None.NONE);
    }

    private TernaryOperator parseTernaryOperator(Expression left) {
        Token token = curToken;
        nextToken();
        Expression consequence = parseExpression(Precedence.Pair);
        if (consequence == null) {
            return null;
        }

        if (!expectPeek(TokenType.Colon)) {
            return null;
        }
        nextToken();

        Expression alternative = parseExpression(Precedence.Lowest);
        if (alternative == null) {
            return null;
        }

        return new TernaryOperator(token, left, consequence, alternative);
    }

    private CallExpression parseCallExpression(Expression function) {
        CallExpression expression = new CallExpression(curToken, function);
        Arguments args = parseArguments();
        if (args == null) {
            return null;
        }

        expression.getArguments().addAll(args);
        return expression;
    }

    private PairLiteral parsePairLiteral(Expression left) {
        Token token = curToken;
        nextToken();
        Expression expression = parseExpression(Precedence.Pair);
        return new PairLiteral(token, left, expression);
    }

    private Arguments parseArguments() {
        Arguments arguments = new Arguments();

        skipSemicolon();
        if (peekToken.getType() == TokenType.RParen) {
            nextToken();
            return arguments;
        }
        nextToken();

        Expression exp = parseExpression(Precedence.Comma);
        if (exp == null) {
            return null;
        }
        skipSemicolon();

        PairLiteral pair;
        if (exp instanceof PairLiteral) {
            pair = (PairLiteral) exp;
        } else {
            pair = new PairLiteral(new Token(TokenType.Colon, exp.getToken().getLineNumber(), ":"), exp, None.NONE);
        }

        arguments.add(pair);

        while (peekToken.getType() == TokenType.Comma) {
            nextToken();
            skipSemicolon();
            nextToken();
            exp = parseExpression(Precedence.Comma);
            if (exp == null) {
                return null;
            }

            if (exp instanceof PairLiteral) {
                pair = (PairLiteral) exp;
            } else {
                pair = new PairLiteral(new Token(TokenType.Colon, exp.getToken().getLineNumber(), ":"), exp, None.NONE);
            }

            arguments.add(pair);
        }

        skipSemicolon();
        if (!expectPeek(TokenType.RParen)) {
            return null;
        }

        return arguments;
    }

    private List<Expression> parseExpressionList() {
        List<Expression> args = new ArrayList<>();
        skipSemicolon();

        if (peekToken.getType() == TokenType.RBracket) {
            nextToken();
            return args;
        }
        nextToken();
        skipSemicolon();

        args.add(parseExpression(Precedence.Comma));

        while (peekToken.getType() == TokenType.Comma) {
            nextToken();
            skipSemicolon();
            nextToken();
            args.add(parseExpression(Precedence.Comma));
        }

        skipSemicolon();

        if (!expectPeek(TokenType.RBracket)) {
            return null;
        }

        return args;
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    private boolean expectPeek(TokenType type) {
        if (peekToken.getType() == type) {
            nextToken();
            return true;
        }
        addError("expected next token to be " + type + ", got " + peekToken.getType() + " instead");
        return false;
    }

    private Precedence peekPrecedence() {
        Precedence precedence = precedenceMap.get(peekToken.getType());
        if (precedence != null) {
            return precedence;
        }
        return Precedence.Lowest;
    }

    private Precedence curPrecedence() {
        Precedence precedence = precedenceMap.get(curToken.getType());
        if (precedence != null) {
            return precedence;
        }
        return Precedence.Lowest;
    }

    private void skipSemicolon() {
        while (peekToken.getType() == TokenType.Semicolon) {
            nextToken();
        }
    }

    private void addError(String message) {
        errors.add("line:" + curToken.getLineNumber() + " " + message);
    }

    private interface PrefixParseFunction extends Supplier<Expression> {
    }

    private interface InfixParseFunction extends Function<Expression, Expression> {
    }
}
