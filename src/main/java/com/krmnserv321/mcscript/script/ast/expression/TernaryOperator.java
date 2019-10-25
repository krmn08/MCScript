package com.krmnserv321.mcscript.script.ast.expression;

import com.krmnserv321.mcscript.script.ast.Token;

public class TernaryOperator extends Expression {
    private Expression condition;
    private Expression consequence;
    private Expression alternative;

    public TernaryOperator(Token token, Expression condition, Expression consequence, Expression alternative) {
        super(token);
        this.condition = condition;
        this.consequence = consequence;
        this.alternative = alternative;
    }

    public Expression getCondition() {
        return condition;
    }

    public void setCondition(Expression condition) {
        this.condition = condition;
    }

    public Expression getConsequence() {
        return consequence;
    }

    public void setConsequence(Expression consequence) {
        this.consequence = consequence;
    }

    public Expression getAlternative() {
        return alternative;
    }

    public void setAlternative(Expression alternative) {
        this.alternative = alternative;
    }

    @Override
    public String toString() {
        return condition + " " + getTokenLiteral() + " " + consequence + " : " + alternative;
    }
}
