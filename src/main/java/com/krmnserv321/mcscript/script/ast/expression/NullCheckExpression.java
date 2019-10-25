package com.krmnserv321.mcscript.script.ast.expression;

import com.krmnserv321.mcscript.script.ast.Token;

public class NullCheckExpression extends Expression {
    private Expression nullable;
    private Expression expression;

    public NullCheckExpression(Token token, Expression nullable, Expression expression) {
        super(token);
        this.nullable = nullable;
        this.expression = expression;
    }

    public Expression getNullable() {
        return nullable;
    }

    public void setNullable(Expression nullable) {
        this.nullable = nullable;
    }

    public Expression getExpression() {
        return expression;
    }

    public void setExpression(Expression expression) {
        this.expression = expression;
    }

    @Override
    public String toString() {
        return nullable + " ?: " + expression;
    }
}
