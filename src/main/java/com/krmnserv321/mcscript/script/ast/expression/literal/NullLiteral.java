package com.krmnserv321.mcscript.script.ast.expression.literal;

import com.krmnserv321.mcscript.script.ast.Token;

public class NullLiteral extends Literal {
    public NullLiteral(Token token) {
        super(token);
    }

    @Override
    public Object getValue() {
        return null;
    }
}
