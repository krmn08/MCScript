package com.krmnserv321.mcscript.script.ast.expression;

import com.krmnserv321.mcscript.script.ast.Token;

public class AssignExpression extends Expression {
    private Expression name;
    private Expression value;
    private String operator;

    public AssignExpression(Token token, Expression name, Expression value) {
        super(token);
        this.name = name;
        this.value = value;
    }

    public AssignExpression(Token token, Expression name, Expression value, String operator) {
        super(token);
        this.name = name;
        this.value = value;
        this.operator = operator;
    }

    public Expression getName() {
        return name;
    }

    public void setName(Expression name) {
        this.name = name;
    }

    public Expression getValue() {
        return value;
    }

    public void setValue(Expression value) {
        this.value = value;
    }

    public String getOperator() {
        return operator;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }

    @Override
    public String toString() {
        return name + " " + getTokenLiteral() + " " + value;
    }
}
