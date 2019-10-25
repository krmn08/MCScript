package com.krmnserv321.mcscript.script.ast.expression.literal;

import com.krmnserv321.mcscript.script.ast.Token;

public class BooleanLiteral extends Literal {
    private Boolean value;

    public BooleanLiteral(Token token, Boolean value) {
        super(token);
        this.value = value;
    }

    @Override
    public Boolean getValue() {
        return value;
    }

    public void setValue(Boolean value) {
        this.value = value;
    }
}
