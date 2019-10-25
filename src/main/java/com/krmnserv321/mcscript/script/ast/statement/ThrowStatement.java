package com.krmnserv321.mcscript.script.ast.statement;

import com.krmnserv321.mcscript.script.ast.Token;
import com.krmnserv321.mcscript.script.ast.expression.Expression;

public class ThrowStatement extends Statement {
    private Expression message;

    public ThrowStatement(Token token, Expression message) {
        super(token);
        this.message = message;
    }

    public Expression getMessage() {
        return message;
    }

    public void setMessage(Expression message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return getTokenLiteral() + " " + message;
    }
}
