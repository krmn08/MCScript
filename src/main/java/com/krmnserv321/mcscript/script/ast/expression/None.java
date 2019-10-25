package com.krmnserv321.mcscript.script.ast.expression;

import com.krmnserv321.mcscript.script.ast.Token;
import com.krmnserv321.mcscript.script.ast.TokenType;

public class None extends Expression {
    public static final None NONE = new None();

    public None() {
        super(new Token(TokenType.Illegal, "none"));
    }

    @Override
    public String toString() {
        return "";
    }
}
