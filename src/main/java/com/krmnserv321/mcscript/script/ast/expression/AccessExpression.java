package com.krmnserv321.mcscript.script.ast.expression;

import com.krmnserv321.mcscript.script.ast.Token;

public class AccessExpression extends Expression {
    private Expression left;
    private Expression accessor;

    public AccessExpression(Token token, Expression left, Expression accessor) {
        super(token);
        this.left = left;
        this.accessor = accessor;
    }

    public Expression getLeft() {
        return left;
    }

    public void setLeft(Expression left) {
        this.left = left;
    }

    public Expression getAccessor() {
        return accessor;
    }

    public void setAccessor(Expression accessor) {
        this.accessor = accessor;
    }

    @Override
    public String toString() {
        return left + "[" + accessor + "]";
    }
}
