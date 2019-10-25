package com.krmnserv321.mcscript.script.ast.expression.literal;

import com.krmnserv321.mcscript.script.ast.Token;

public class FloatLiteral extends Literal {
    private Float value;

    public FloatLiteral(Token token, Float value) {
        super(token);
        this.value = value;
    }

    @Override
    public Float getValue() {
        return value;
    }

    public void setValue(Float value) {
        this.value = value;
    }
}
