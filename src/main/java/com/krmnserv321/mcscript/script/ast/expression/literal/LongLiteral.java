package com.krmnserv321.mcscript.script.ast.expression.literal;

import com.krmnserv321.mcscript.script.ast.Token;

public class LongLiteral extends Literal {
    private Long value;

    public LongLiteral(Token token, Long value) {
        super(token);
        this.value = value;
    }

    @Override
    public Long getValue() {
        return value;
    }

    public void setValue(Long value) {
        this.value = value;
    }
}
