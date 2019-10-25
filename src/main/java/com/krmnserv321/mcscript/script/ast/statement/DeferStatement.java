package com.krmnserv321.mcscript.script.ast.statement;

import com.krmnserv321.mcscript.script.ast.Token;

public class DeferStatement extends Statement {
    private Statement statement;

    public DeferStatement(Token token, Statement statement) {
        super(token);
        this.statement = statement;
    }

    public Statement getStatement() {
        return statement;
    }

    public void setStatement(Statement statement) {
        this.statement = statement;
    }

    @Override
    public String toString() {
        return getTokenLiteral() + " " + statement;
    }
}
