package com.krmnserv321.mcscript.script.ast.statement;

import com.krmnserv321.mcscript.script.ast.Token;
import com.krmnserv321.mcscript.script.ast.expression.Expression;
import com.krmnserv321.mcscript.script.ast.expression.Identifier;

public class ForEachStatement extends Statement {
    private Identifier variable1;
    private Identifier variable2;
    private Expression iterable;
    private Block body;

    public ForEachStatement(Token token, Identifier variable1, Identifier variable2, Expression iterable, Block body) {
        super(token);
        this.variable1 = variable1;
        this.variable2 = variable2;
        this.iterable = iterable;
        this.body = body;
    }

    public Identifier getVariable1() {
        return variable1;
    }

    public void setVariable1(Identifier variable1) {
        this.variable1 = variable1;
    }

    public Identifier getVariable2() {
        return variable2;
    }

    public void setVariable2(Identifier variable2) {
        this.variable2 = variable2;
    }

    public Expression getIterable() {
        return iterable;
    }

    public void setIterable(Expression iterable) {
        this.iterable = iterable;
    }

    public Block getBody() {
        return body;
    }

    public void setBody(Block body) {
        this.body = body;
    }

    @Override
    public String toString() {
        if (variable2 == null) {
            return getTokenLiteral() + " (" + variable1 + " in " + iterable + ") " + body;
        }
        return getTokenLiteral() + " (" + variable1 + ", " + variable2 + " in " + iterable + ") " + body;
    }
}
