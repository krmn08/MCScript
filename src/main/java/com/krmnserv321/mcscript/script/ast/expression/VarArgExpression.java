package com.krmnserv321.mcscript.script.ast.expression;

import com.krmnserv321.mcscript.script.ast.Token;

public class VarArgExpression extends Expression {
    private Expression expression;

    public VarArgExpression(Token token, Expression expression) {
        super(token);
        this.expression = expression;
    }

    public Expression getExpression() {
        return expression;
    }

    public void setExpression(Expression expression) {
        this.expression = expression;
    }

    @Override
    public String toString() {
        return getTokenLiteral() + " " + expression;
    }
}
