package com.krmnserv321.mcscript.script.ast.expression.literal;

import com.krmnserv321.mcscript.script.ast.Token;
import com.krmnserv321.mcscript.script.ast.expression.Expression;

public abstract class Literal extends Expression {
    public Literal(Token token) {
        super(token);
    }

    public abstract Object getValue();

    @Override
    public String toString() {
        return getTokenLiteral();
    }
}
