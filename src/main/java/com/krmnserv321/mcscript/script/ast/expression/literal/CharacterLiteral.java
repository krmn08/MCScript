package com.krmnserv321.mcscript.script.ast.expression.literal;

import com.krmnserv321.mcscript.script.ast.Token;

public class CharacterLiteral extends Literal {
    private Character value;

    public CharacterLiteral(Token token, Character value) {
        super(token);
        this.value = value;
    }

    @Override
    public Character getValue() {
        return value;
    }

    public void setValue(Character value) {
        this.value = value;
    }
}
