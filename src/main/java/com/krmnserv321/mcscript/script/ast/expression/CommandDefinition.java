package com.krmnserv321.mcscript.script.ast.expression;

import com.krmnserv321.mcscript.script.ast.Token;
import com.krmnserv321.mcscript.script.ast.statement.Block;

public class CommandDefinition extends FunctionDefinition {
    private boolean isVarArgs;

    public CommandDefinition(Token token, boolean isVarArgs, Identifier name, Block body) {
        super(token, name, body);
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
        return getTokenLiteral() + " " + getName() + "(" + getParameters() + ") " + getBody();
    }
}