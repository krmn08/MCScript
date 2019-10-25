package com.krmnserv321.mcscript.script.ast.statement;

import com.krmnserv321.mcscript.script.ast.Token;
import com.krmnserv321.mcscript.script.ast.expression.Expression;

public class ForStatement extends Statement {
    private Expression initExpression;
    private Expression condition;
    private Expression loopExpression;

    private Block body;

    public ForStatement(Token token, Expression initStatement, Expression condition, Expression loopExpression, Block body) {
        super(token);
        this.initExpression = initStatement;
        this.condition = condition;
        this.loopExpression = loopExpression;
        this.body = body;
    }

    public Expression getInitExpression() {
        return initExpression;
    }

    public void setInitExpression(Expression initExpression) {
        this.initExpression = initExpression;
    }

    public Expression getCondition() {
        return condition;
    }

    public void setCondition(Expression condition) {
        this.condition = condition;
    }

    public Expression getLoopExpression() {
        return loopExpression;
    }

    public void setLoopExpression(Expression loopExpression) {
        this.loopExpression = loopExpression;
    }

    public Block getBody() {
        return body;
    }

    public void setBody(Block body) {
        this.body = body;
    }

    @Override
    public String toString() {
        return getTokenLiteral() + " (" + initExpression + "; " + condition + "; " + loopExpression + ") " + body;
    }
}
