package com.krmnserv321.mcscript.script.ast.expression;

import com.krmnserv321.mcscript.script.ast.Token;

public class InfixExpression extends Expression {
    private String operator;

    private Expression left;
    private Expression right;

    public InfixExpression(Token token, String operator, Expression left, Expression right) {
        super(token);
        this.operator = operator;
        this.left = left;
        this.right = right;
    }

    public String getOperator() {
        return operator;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }

    public Expression getLeft() {
        return left;
    }

    public void setLeft(Expression left) {
        this.left = left;
    }

    public Expression getRight() {
        return right;
    }

    public void setRight(Expression right) {
        this.right = right;
    }

    @Override
    public String toString() {
        if (operator.equals(".")) {
            return left + operator + right;
        }
        return left + " " + operator + " " + right;
    }
}
