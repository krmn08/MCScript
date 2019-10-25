package com.krmnserv321.mcscript.script.ast;

public enum Precedence {
    Lowest,
    Assign,
    Comma,
    Lambda,
    Pair,
    Question,
    NullCheck,
    Or,
    And,
    Equals,
    LessGreater,
    Range,
    BitOr,
    BitXor,
    BitAnd,
    Shift,
    Sum,
    Product,
    Power,
    Prefix,
    Access,
    Call,
    Dot,
}
