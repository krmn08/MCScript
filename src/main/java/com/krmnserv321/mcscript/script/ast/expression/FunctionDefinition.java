package com.krmnserv321.mcscript.script.ast.expression;

import com.krmnserv321.mcscript.script.ast.Token;
import com.krmnserv321.mcscript.script.ast.statement.Statement;

public class FunctionDefinition extends FunctionLiteral {
    private Identifier name;
    private Identifier returnType;

    public FunctionDefinition(Token token, Identifier name, Identifier returnType, Statement body) {
        super(token, body);
        this.name = name;
        this.returnType = returnType;
    }

    public Identifier getName() {
        return name;
    }

    public void setName(Identifier name) {
        this.name = name;
    }

    public Identifier getReturnType() {
        return returnType;
    }

    public void setReturnType(Identifier returnType) {
        this.returnType = returnType;
    }

    @Override
    public String toString() {
        if (returnType != null) {
            return getTokenLiteral() + " " + name + "(" + getParameters() + "): " + returnType + " " + getBody();
        }
        return getTokenLiteral() + " " + name + "(" + getParameters() + ") " + getBody();
    }
}
