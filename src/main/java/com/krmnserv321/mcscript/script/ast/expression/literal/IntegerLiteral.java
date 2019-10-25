package com.krmnserv321.mcscript.script.ast.expression.literal;

import com.krmnserv321.mcscript.script.ast.Token;

public class IntegerLiteral extends Literal {
    private Integer value;

    public IntegerLiteral(Token token, Integer value) {
        super(token);
        this.value = value;
    }

    @Override
    public Integer getValue() {
        return value;
    }

    public void setValue(Integer value) {
        this.value = value;
    }
}
