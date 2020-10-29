package com.krmnserv321.mcscript.script.ast.expression;

import com.krmnserv321.mcscript.script.ast.Token;

public class PublicExpression extends Expression {
    private Identifier name;
    private Expression value;

    public PublicExpression(Token token, Identifier name, Expression value) {
        super(token);
        this.name = name;
        this.value = value;
    }

    public Identifier getName() {
        return name;
    }

    public void setName(Identifier name) {
        this.name = name;
    }

    public Expression getValue() {
        return value;
    }

    public void setValue(Expression value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return getTokenLiteral() + " " + name + " = " + value;
    }
}
