package com.krmnserv321.mcscript.script.ast.statement;

import com.krmnserv321.mcscript.script.ast.Token;
import com.krmnserv321.mcscript.script.ast.expression.Expression;

public class ReturnStatement extends Statement {
    private Expression value;

    public ReturnStatement(Token token, Expression value) {
        super(token);
        this.value = value;
    }

    public Expression getValue() {
        return value;
    }

    public void setValue(Expression value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return getTokenLiteral() + " " + value;
    }
}
