package com.krmnserv321.mcscript.script.ast.expression;

import com.krmnserv321.mcscript.script.ast.Token;
import com.krmnserv321.mcscript.script.ast.statement.Statement;

public class CommandDefinition extends FunctionDefinition {
    private boolean isVarArgs;

    public CommandDefinition(Token token, boolean isVarArgs, Identifier name, Statement body) {
        super(token, name, null, body);
        this.isVarArgs = isVarArgs;
    }

    public boolean isVarArgs() {
        return isVarArgs;
    }

    public void setVarArgs(boolean varArgs) {
        isVarArgs = varArgs;
    }

    @Override
    public String toString() {
        String tilde = isVarArgs ? "~" : "";
        if (getReturnType() != null) {
            return getTokenLiteral() + " " + tilde + getName() + "(" + getParameters() + "): " + getReturnType() + " " + getBody();
        }
        return getTokenLiteral() + " " + tilde + getName() + "(" + getParameters() + ") " + getBody();
    }
}