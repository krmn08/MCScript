package com.krmnserv321.mcscript.script.ast.statement;

import com.krmnserv321.mcscript.script.ast.Token;
import com.krmnserv321.mcscript.script.ast.expression.Expression;

public class WhileStatement extends Statement {
    private Expression condition;
    private Block body;
    private boolean infinite;

    public WhileStatement(Token token, Expression condition, Block body, boolean infinite) {
        super(token);
        this.condition = condition;
        this.body = body;
        this.infinite = infinite;
    }

    public Expression getCondition() {
        return condition;
    }

    public void setCondition(Expression condition) {
        this.condition = condition;
    }

    public Block getBody() {
        return body;
    }

    public void setBody(Block body) {
        this.body = body;
    }

    public void setInfinite(boolean infinite) {
        this.infinite = infinite;
    }

    public boolean isInfinite() {
        return infinite;
    }

    @Override
    public String toString() {
        if (infinite) {
            return getTokenLiteral() + " " + body;
        }
        return getTokenLiteral() + " (" + condition + ") " + body;
    }
}
