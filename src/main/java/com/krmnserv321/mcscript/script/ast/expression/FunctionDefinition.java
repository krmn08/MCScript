package com.krmnserv321.mcscript.script.ast.expression;

import com.krmnserv321.mcscript.script.ast.Token;
import com.krmnserv321.mcscript.script.ast.statement.Statement;

public class FunctionDefinition extends FunctionLiteral {
    private Identifier name;

    public FunctionDefinition(Token token, Identifier name, Statement body) {
        super(token, body);
        this.name = name;
    }

    public Identifier getName() {
        return name;
    }

    public void setName(Identifier name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return getTokenLiteral() + " " + name + "(" + getParameters() + ") " + getBody();
    }
}
