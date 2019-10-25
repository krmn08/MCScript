package com.krmnserv321.mcscript.script.ast.expression;

import com.krmnserv321.mcscript.script.ast.Arguments;
import com.krmnserv321.mcscript.script.ast.Token;
import com.krmnserv321.mcscript.script.ast.statement.Block;
import com.krmnserv321.mcscript.script.ast.statement.Statement;

public class FunctionLiteral extends Expression {
    private Arguments parameters = new Arguments();
    private Statement body;

    public FunctionLiteral(Token token, Statement body) {
        super(token);
        this.body = body;
    }

    public Arguments getParameters() {
        return parameters;
    }

    public Statement getBody() {
        return body;
    }

    public void setBody(Statement body) {
        this.body = body;
    }

    @Override
    public String toString() {
        if (body instanceof Block) {
            return getTokenLiteral() + "(" + parameters + ") " + body;
        }

        return getTokenLiteral() + "(" + parameters + "): " + body;
    }
}
