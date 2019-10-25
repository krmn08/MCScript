package com.krmnserv321.mcscript.script.ast.expression.literal;

import com.krmnserv321.mcscript.script.ast.Token;

public class DoubleLiteral extends Literal {
    private Double value;

    public DoubleLiteral(Token token, Double value) {
        super(token);
        this.value = value;
    }

    @Override
    public Double getValue() {
        return value;
    }

    public void setValue(Double value) {
        this.value = value;
    }
}
