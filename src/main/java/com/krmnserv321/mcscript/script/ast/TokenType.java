package com.krmnserv321.mcscript.script.ast;

public enum TokenType {
    Root,

    Illegal,
    EOF,

    Identifier,

    Character,
    String,
    Integer,
    Long,
    Double,
    Float,

    Constant,

    Assign,
    ColonAssign,
    Plus,
    Minus,
    Multi,
    Divide,
    IntDivide,
    Mod,
    Power,
    Not,

    Question,

    BitAnd,
    BitOr,
    BitXor,
    BitNot,

    LeftShift,
    RightShift,

    PlusAssign,
    MinusAssign,
    MultiAssign,
    DivideAssign,
    IntDivideAssign,
    ModAssign,
    PowerAssign,
    AndAssign,
    OrAssign,
    XorAssign,
    LeftShiftAssign,
    RightShiftAssign,

    And,
    Or,

    Equal,
    NotEqual,

    LessThanEqual,
    GreaterThanEqual,

    LessThan,
    GreaterThan,

    NullCheck,

    Dot,
    Range,
    Comma,
    Colon,
    Semicolon,

    Arrow,

    Runnable,
    Event,

    LParen,
    RParen,
    LBrace,
    RBrace,
    LBracket,
    RBracket,

    Import,
    Null,
    InstanceOf,
    Is,
    IsNot,
    In,
    NotIn,
    Until,
    Step,
    Function,
    Command,
    True,
    False,
    While,
    For,
    Break,
    Continue,
    Switch,
    Default,
    If,
    Elif,
    Else,
    Return,
    VarArg,
    Throw,
    Try,
    Catch,
    Defer,

    BooleanClass,
    ByteClass,
    CharClass,
    ShortClass,
    IntClass,
    LongClass,
    FloatClass,
    DoubleClass,
}
