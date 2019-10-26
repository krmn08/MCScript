package com.krmnserv321.mcscript.script;

import com.krmnserv321.mcscript.script.ast.Token;
import com.krmnserv321.mcscript.script.ast.TokenType;

import java.util.HashMap;
import java.util.Map;

public class Lexer {
    private static final Token EOF = new Token(TokenType.EOF, "");

    private static final Token CONSTANT = new Token(TokenType.Constant, "const");

    private static final Token ASSIGN = new Token(TokenType.Assign, "=");
    private static final Token COLON_ASSIGN = new Token(TokenType.ColonAssign, ":=");
    private static final Token PLUS = new Token(TokenType.Plus, "+");
    private static final Token MINUS = new Token(TokenType.Minus, "-");
    private static final Token MULTI = new Token(TokenType.Multi, "*");
    private static final Token DIVIDE = new Token(TokenType.Divide, "/");
    private static final Token INT_DIVIDE = new Token(TokenType.IntDivide, "//");
    private static final Token MOD = new Token(TokenType.Mod, "%");
    private static final Token POWER = new Token(TokenType.Power, "**");
    private static final Token NOT = new Token(TokenType.Not, "!");

    private static final Token QUESTION = new Token(TokenType.Question, "?");

    private static final Token BIT_AND = new Token(TokenType.BitAnd, "&");
    private static final Token BIT_OR = new Token(TokenType.BitOr, "|");
    private static final Token BIT_XOR = new Token(TokenType.BitXor, "^");
    private static final Token BIT_NOT = new Token(TokenType.BitNot, "~");

    private static final Token LEFT_SHIFT = new Token(TokenType.LeftShift, "<<");
    private static final Token RIGHT_SHIFT = new Token(TokenType.RightShift, ">>");

    private static final Token PLUS_ASSIGN = new Token(TokenType.PlusAssign, "+=");
    private static final Token MINUS_ASSIGN = new Token(TokenType.MinusAssign, "-=");
    private static final Token MULTI_ASSIGN = new Token(TokenType.MultiAssign, "*=");
    private static final Token DIVIDE_ASSIGN = new Token(TokenType.DivideAssign, "/=");
    private static final Token INT_DIVIDE_ASSIGN = new Token(TokenType.IntDivideAssign, "//=");
    private static final Token MOD_ASSIGN = new Token(TokenType.ModAssign, "%=");
    private static final Token POWER_ASSIGN = new Token(TokenType.PowerAssign, "**=");
    private static final Token AND_ASSIGN = new Token(TokenType.AndAssign, "&=");
    private static final Token OR_ASSIGN = new Token(TokenType.OrAssign, "|=");
    private static final Token XOR_ASSIGN = new Token(TokenType.XorAssign, "^=");
    private static final Token LEFT_SHIFT_ASSIGN = new Token(TokenType.LeftShiftAssign, "<<=");
    private static final Token RIGHT_SHIFT_ASSIGN = new Token(TokenType.RightShiftAssign, ">>=");

    private static final Token AND = new Token(TokenType.And, "&&");
    private static final Token OR = new Token(TokenType.Or, "||");

    private static final Token EQUAL = new Token(TokenType.Equal, "==");
    private static final Token NOT_EQUAL = new Token(TokenType.NotEqual, "!=");

    private static final Token LESS_THAN_EQUAL = new Token(TokenType.LessThanEqual, "<=");
    private static final Token GREATER_THAN_EQUAL = new Token(TokenType.GreaterThanEqual, ">=");

    private static final Token LESS_THAN = new Token(TokenType.LessThan, "<");
    private static final Token GREATER_THAN = new Token(TokenType.GreaterThan, ">");

    private static final Token NULL_CHECK = new Token(TokenType.NullCheck, "?:");

    private static final Token DOT = new Token(TokenType.Dot, ".");
    private static final Token RANGE = new Token(TokenType.Range, "..");
    private static final Token COMMA = new Token(TokenType.Comma, ",");
    private static final Token COLON = new Token(TokenType.Colon, ":");
    private static final Token SEMICOLON = new Token(TokenType.Semicolon, ";");

    private static final Token ARROW = new Token(TokenType.Arrow, "->");

    private static final Token EVENT = new Token(TokenType.Event, "@");
    private static final Token RUNNABLE = new Token(TokenType.Runnable, "runnable");

    private static final Token L_PAREN = new Token(TokenType.LParen, "(");
    private static final Token R_PAREN = new Token(TokenType.RParen, ")");
    private static final Token L_BRACE = new Token(TokenType.LBrace, "{");
    private static final Token R_BRACE = new Token(TokenType.RBrace, "}");
    private static final Token L_BRACKET = new Token(TokenType.LBracket, "[");
    private static final Token R_BRACKET = new Token(TokenType.RBracket, "]");

    private static final Token IMPORT = new Token(TokenType.Import, "import");
    private static final Token NULL = new Token(TokenType.Null, "null");
    private static final Token INSTANCEOF = new Token(TokenType.InstanceOf, "instanceof");
    private static final Token IS = new Token(TokenType.Is, "is");
    private static final Token IS_NOT = new Token(TokenType.IsNot, "isnot");
    private static final Token IN = new Token(TokenType.In, "in");
    private static final Token NOT_IN = new Token(TokenType.NotIn, "notin");
    private static final Token UNTIL = new Token(TokenType.Until, "until");
    private static final Token STEP = new Token(TokenType.Step, "step");
    private static final Token FUNCTION = new Token(TokenType.Function, "fun");
    private static final Token COMMAND = new Token(TokenType.Command, "command");
    private static final Token TRUE = new Token(TokenType.True, "true");
    private static final Token FALSE = new Token(TokenType.False, "false");
    private static final Token WHILE = new Token(TokenType.While, "while");
    private static final Token FOR = new Token(TokenType.For, "for");
    private static final Token BREAK = new Token(TokenType.Break, "break");
    private static final Token CONTINUE = new Token(TokenType.Continue, "continue");
    private static final Token SWITCH = new Token(TokenType.Switch, "switch");
    private static final Token DEFAULT = new Token(TokenType.Default, "default");
    private static final Token IF = new Token(TokenType.If, "if");
    private static final Token ELIF = new Token(TokenType.Elif, "elif");
    private static final Token ELSE = new Token(TokenType.Else, "else");
    private static final Token RETURN = new Token(TokenType.Return, "return");
    private static final Token VAR_ARG = new Token(TokenType.VarArg, "vararg");
    private static final Token THROW = new Token(TokenType.Throw, "throw");
    private static final Token TRY = new Token(TokenType.Try, "try");
    private static final Token CATCH = new Token(TokenType.Catch, "catch");
    private static final Token DEFER = new Token(TokenType.Defer, "defer");

    private static final Token BOOLEAN_CLASS = new Token(TokenType.BooleanClass, "boolean");
    private static final Token CHAR_CLASS = new Token(TokenType.CharClass, "char");
    private static final Token BYTE_CLASS = new Token(TokenType.ByteClass, "byte");
    private static final Token SHORT_CLASS = new Token(TokenType.ShortClass, "short");
    private static final Token INT_CLASS = new Token(TokenType.IntClass, "int");
    private static final Token LONG_CLASS = new Token(TokenType.LongClass, "long");
    private static final Token FLOAT_CLASS = new Token(TokenType.FloatClass, "float");
    private static final Token DOUBLE_CLASS = new Token(TokenType.DoubleClass, "double");

    private static Token[] TOKENS = {
            ASSIGN,
            PLUS,
            MINUS,
            MULTI,
            DIVIDE,
            MOD,
            NOT,

            QUESTION,

            BIT_AND,
            BIT_OR,
            BIT_XOR,
            BIT_NOT,

            LESS_THAN,
            GREATER_THAN,

            DOT,
            COMMA,
            COLON,
            SEMICOLON,

            EVENT,

            L_PAREN,
            R_PAREN,
            L_BRACE,
            R_BRACE,
            L_BRACKET,
            R_BRACKET
    };

    private static Token[] KEYWORDS = {
            RUNNABLE,

            CONSTANT,

            IMPORT,
            NULL,
            INSTANCEOF,
            IS,
            IS_NOT,
            IN,
            NOT_IN,
            UNTIL,
            STEP,
            FUNCTION,
            COMMAND,
            TRUE,
            FALSE,
            WHILE,
            FOR,
            BREAK,
            CONTINUE,
            SWITCH,
            DEFAULT,
            IF,
            ELIF,
            ELSE,
            RETURN,
            VAR_ARG,
            THROW,
            TRY,
            CATCH,
            DEFER,

            BOOLEAN_CLASS,
            CHAR_CLASS,
            BYTE_CLASS,
            SHORT_CLASS,
            INT_CLASS,
            LONG_CLASS,
            FLOAT_CLASS,
            DOUBLE_CLASS
    };

    private static Map<Character, Character> escapeMap = new HashMap<Character, Character>() {
        {
            put('t', '\t');
            put('b', '\b');
            put('n', '\n');
            put('f', '\f');
            put('r', '\r');
            put('\\', '\\');
            put('"', '"');
        }
    };

    private String path = "";
    private String input;
    private int pos;
    private int readPos;
    private int lineCount = 1;
    private char ch;

    public Lexer(String input) {
        this.input = input;
        readChar();
    }

    public Lexer(String path, String input) {
        this.path = path;
        this.input = input;
        readChar();
    }

    public Token nextToken() {
        Token token = null;

        skipWhitespace();

        skipComment();

        skipWhitespace();

        if (ch == '\\') {
            readChar();
            if (ch == '\r' && peekChar() == '\n') {
                readChar();
            }
            readChar();
            return nextToken();
        }

        if (ch == 0) {
            Token eof = EOF;
            eof.setPath(path);
            eof.setLineNumber(lineCount);
            return eof.clone();
        } else {
            Token semicolon = SEMICOLON;
            semicolon.setPath(path);
            semicolon.setLineNumber(lineCount);
            if (ch == '\r') {
                readChar();
                if (ch == '\n') {
                    readChar();
                    lineCount++;
                    return semicolon.clone();
                } else {
                    return new Token(TokenType.Illegal, path, lineCount, "CR");
                }
            } else if (ch == '\n') {
                readChar();
                lineCount++;
                return semicolon.clone();
            }
        }

        for (Token t : TOKENS) {
            if (t.toString().charAt(0) == ch) {
                token = t;
            }
        }

        if (token != null) {
            switch (peekChar()) {
                case '<':
                    if (ch == '<') {
                        readChar();
                        if (peekChar() == '=') {
                            readChar();
                            token = LEFT_SHIFT_ASSIGN;
                        } else {
                            token = LEFT_SHIFT;
                        }
                    }

                    break;
                case '>':
                    if (ch == '-') {
                        readChar();
                        token = ARROW;
                    } else if (ch == '>') {
                        readChar();
                        if (peekChar() == '=') {
                            readChar();
                            token = RIGHT_SHIFT_ASSIGN;
                        } else {
                            token = RIGHT_SHIFT;
                        }
                    }
                    break;
                case '*':
                    if (ch == '*') {
                        readChar();
                        if (peekChar() == '=') {
                            readChar();
                            token = POWER_ASSIGN;
                        } else {
                            token = POWER;
                        }
                    }
                    break;
                case '/':
                    if (ch == '/') {
                        readChar();
                        if (peekChar() == '=') {
                            readChar();
                            token = INT_DIVIDE_ASSIGN;
                        } else {
                            token = INT_DIVIDE;
                        }
                    }
                    break;
                case '&':
                    if (ch == '&') {
                        readChar();
                        token = AND;
                    }
                    break;
                case '|':
                    if (ch == '|') {
                        readChar();
                        token = OR;
                    }
                    break;
                case '.':
                    if (ch == '.') {
                        readChar();
                        token = RANGE;
                    }
                    break;
                case ':':
                    if (ch == '?') {
                        readChar();
                        token = NULL_CHECK;
                    }
                    break;
                case '=':
                    switch (ch) {
                        case ':':
                            readChar();
                            token = COLON_ASSIGN;
                            break;
                        case '=':
                            readChar();
                            token = EQUAL;
                            break;
                        case '+':
                            readChar();
                            token = PLUS_ASSIGN;
                            break;
                        case '-':
                            readChar();
                            token = MINUS_ASSIGN;
                            break;
                        case '*':
                            readChar();
                            token = MULTI_ASSIGN;
                            break;
                        case '/':
                            readChar();
                            token = DIVIDE_ASSIGN;
                            break;
                        case '%':
                            readChar();
                            token = MOD_ASSIGN;
                            break;
                        case '!':
                            readChar();
                            token = NOT_EQUAL;
                            break;
                        case '<':
                            readChar();
                            token = LESS_THAN_EQUAL;
                            break;
                        case '>':
                            readChar();
                            token = GREATER_THAN_EQUAL;
                            break;
                        case '&':
                            readChar();
                            token = AND_ASSIGN;
                            break;
                        case '|':
                            readChar();
                            token = OR_ASSIGN;
                            break;
                        case '^':
                            readChar();
                            token = XOR_ASSIGN;
                            break;
                    }
                    break;
            }
        } else {
            if (ch == '\'') {
                String c = readChars();
                if (c == null) {
                    return new Token(TokenType.Illegal, path, lineCount, Character.toString(ch));
                }
                return new Token(TokenType.Character, path, lineCount, c);
            } else if (ch == '"') {
                String string = readString();
                if (string == null) {
                    return new Token(TokenType.Illegal, lineCount, Character.toString(ch));
                }

                return new Token(TokenType.String, lineCount, string);
            } else if (ch == '`') {
                readChar();
                if (Character.isLetter(ch)) {
                    String ident = readIdentifier();
                    if (ch != '`') {
                        readChar();
                        return new Token(TokenType.Illegal, path, lineCount, Character.toString(ch));
                    }

                    readChar();

                    return new Token(TokenType.Identifier, path, lineCount, ident);
                }
                return new Token(TokenType.Illegal, path, lineCount, Character.toString(ch));
            } else if (Character.isLetter(ch)) {
                String ident = readIdentifier();
                for (Token keyword : KEYWORDS) {
                    if (keyword.toString().equals(ident)) {
                        keyword.setPath(path);
                        keyword.setLineNumber(lineCount);
                        return keyword.clone();
                    }
                }
                return new Token(TokenType.Identifier, path, lineCount, ident);
            } else if (Character.isDigit(ch)) {
                String num = readNumber().replace("_", "");
                TokenType type;
                switch (Character.toLowerCase(ch)) {
                    case 'l':
                        readChar();
                        type = TokenType.Long;
                        break;
                    case 'd':
                        readChar();
                        type = TokenType.Double;
                        break;
                    case 'f':
                        readChar();
                        type = TokenType.Float;
                        break;
                    default:
                        if (num.contains(".")) {
                            type = TokenType.Double;
                        } else {
                            type = TokenType.Integer;
                        }
                }
                return new Token(type, path, lineCount, num);
            } else {
                token = new Token(TokenType.Illegal, Character.toString(ch));
            }
        }

        readChar();

        token.setPath(path);
        token.setLineNumber(lineCount);

        return token.clone();
    }

    private void readChar() {
        if (readPos >= input.length()) {
            ch = 0;
        } else {
            ch = input.charAt(readPos);
        }

        pos = readPos;
        readPos++;
    }

    private String readChars() {
        readChar();
        if (ch == 0) {
            return null;
        }
        if (ch == '\\') {
            char peek = peekChar();
            if (peek == 'u') {
                readChar();
                StringBuilder c = new StringBuilder();
                for (int i = 0; i < 4; i++) {
                    readChar();
                    if (ch == 0) {
                        return null;
                    }
                    c.append(ch);
                }
                readChar();
                readChar();
                try {
                    return String.valueOf(Character.toChars(Integer.parseInt(c.toString(), 16)));
                } catch (NumberFormatException e) {
                    return null;
                }
            } else if (escapeMap.containsKey(peek)) {
                readChar();
                readChar();
                if (ch != '\'') {
                    return null;
                }
                readChar();
                return escapeMap.get(peek).toString();
            }
            return null;
        }
        String c = String.valueOf(input.charAt(pos));
        readChar();
        if (ch != '\'') {
            return null;
        }
        readChar();
        return c;
    }

    private String readString() {
        readChar();
        StringBuilder sb = new StringBuilder();
        while (ch != '"') {
            if (ch == 0) {
                return null;
            }
            char peek = peekChar();
            if (ch == '\\') {
                if (peek == 'u') {
                    readChar();
                    StringBuilder c = new StringBuilder();
                    for (int i = 0; i < 4; i++) {
                        readChar();
                        if (ch == 0) {
                            return null;
                        }
                        c.append(ch);
                    }
                    try {
                        sb.append(Character.toChars(Integer.parseInt(c.toString(), 16)));
                    } catch (NumberFormatException e) {
                        return null;
                    }
                    readChar();
                } else if (escapeMap.containsKey(peek)) {
                    sb.append(escapeMap.get(peek));
                    readChar();
                    readChar();
                } else {
                    return null;
                }
            } else {
                sb.append(ch);
                readChar();
            }
        }

        readChar();

        return sb.toString();
    }

    private void skipComment() {
        boolean line;

        while (true) {
            if (ch == '#') {
                line = true;
            } else if (ch == '/' && peekChar() == '*') {
                line = false;
                readChar();
                readChar();
            } else {
                break;
            }

            if (line) {
                while (ch != '\n') {
                    if (ch == 0) {
                        return;
                    }
                    readChar();
                }
                readChar();
            } else {
                while (ch != '*' || peekChar() != '/') {
                    if (ch == 0) {
                        return;
                    }
                    readChar();
                }
                readChar();
                readChar();
            }
        }
    }

    private String readIdentifier() {
        int p = pos;

        while (isIdent(ch)) {
            readChar();
        }

        return input.substring(p, pos);
    }

    private String readNumber() {
        int p = pos;
        boolean hex = false;
        if (ch == '0') {
            readChar();
            switch (ch) {
                case 'x':
                    hex = true;
                case 'b':
                    readChar();
            }
        }

        boolean dot = false;
        while (true) {
            if (!dot && ch == '.') {
                dot = true;
            } else if (!Character.isDigit(ch) && ch != '_' && (!hex || !Character.isLetter(ch))) {
                break;
            }
            readChar();
        }

        if (input.charAt(pos - 1) == '.') {
            pos--;
            readPos--;
            ch = '.';
            return input.substring(p, pos);
        }

        return input.substring(p, pos);
    }

    private void skipWhitespace() {
        while (ch == ' ' || ch == '\t') {
            readChar();
        }
    }

    private char peekChar() {
        if (readPos >= input.length()) {
            return 0;
        }

        return input.charAt(readPos);
    }

    private boolean isIdent(char ch) {
        return ch >= 'A' && ch <= 'Z' || ch >= 'a' && ch <= 'z' || ch >= '0' && ch <= '9' || ch == '_';
    }

    public void setLineCount(int lineCount) {
        this.lineCount = lineCount;
    }
}
