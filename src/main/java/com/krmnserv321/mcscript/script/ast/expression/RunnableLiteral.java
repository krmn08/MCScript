package com.krmnserv321.mcscript.script.ast.expression;

import com.krmnserv321.mcscript.script.ast.Token;
import com.krmnserv321.mcscript.script.ast.statement.Block;

public class RunnableLiteral extends Expression {
    private Expression count;
    private Block body;

    public RunnableLiteral(Token token, Expression count, Block body) {
        super(token);
        this.count = count;
        this.body = body;
    }

    public Expression getCount() {
        return count;
    }

    public void setCount(Expression count) {
        this.count = count;
    }

    public Block getBody() {
        return body;
    }

    public void setBody(Block body) {
        this.body = body;
    }

    @Override
    public String toString() {
        if (count == null) {
            return getTokenLiteral() + " " + body;
        }
        return getTokenLiteral() + "(" + count + ")" + " " + body;
    }
}
