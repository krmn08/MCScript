package com.krmnserv321.mcscript.script.ast.expression;

import com.krmnserv321.mcscript.script.ast.Token;

public class PrefixExpression extends Expression {
    private String operator;
    private Expression right;

    public PrefixExpression(Token token, String operator, Expression right) {
        super(token);
        this.operator = operator;
        this.right = right;
    }

    public String getOperator() {
        return operator;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }

    public Expression getRight() {
        return right;
    }

    public void setRight(Expression right) {
        this.right = right;
    }

    @Override
    public String toString() {
        return operator + right;
    }
}
