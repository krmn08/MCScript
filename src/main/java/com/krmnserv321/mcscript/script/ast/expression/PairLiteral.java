package com.krmnserv321.mcscript.script.ast.expression;

import com.krmnserv321.mcscript.script.ast.Token;

public class PairLiteral extends Expression {
    private Expression first;
    private Expression second;

    public PairLiteral(Token token, Expression first, Expression second) {
        super(token);
        this.first = first;
        this.second = second;
    }

    public Expression getFirst() {
        return first;
    }

    public void setFirst(Expression first) {
        this.first = first;
    }

    public Expression getSecond() {
        return second;
    }

    public void setSecond(Expression second) {
        this.second = second;
    }

    @Override
    public String toString() {
        return first + getTokenLiteral() + second;
    }
}
