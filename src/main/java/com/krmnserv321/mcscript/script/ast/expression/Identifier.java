package com.krmnserv321.mcscript.script.ast.expression;

import com.krmnserv321.mcscript.script.ast.Token;

public class Identifier extends Expression {
    private boolean unsafe;

    public Identifier(Token token) {
        super(token);
    }

    public Identifier(Token token, boolean unsafe) {
        super(token);
        this.unsafe = unsafe;
    }

    public void setUnsafe(boolean unsafe) {
        this.unsafe = unsafe;
    }

    public boolean isUnsafe() {
        return unsafe;
    }

    @Override
    public String toString() {
        String literal = getTokenLiteral();
        return unsafe ? literal + "!" : literal;
    }
}
