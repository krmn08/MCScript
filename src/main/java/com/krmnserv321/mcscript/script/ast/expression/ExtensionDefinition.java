package com.krmnserv321.mcscript.script.ast.expression;

import com.krmnserv321.mcscript.script.ast.Token;
import com.krmnserv321.mcscript.script.ast.statement.Statement;

public class ExtensionDefinition extends FunctionDefinition {
    private Identifier extension;

    public ExtensionDefinition(Token token, Identifier type, Identifier extension, Identifier returnType, Statement body) {
        super(token, type, returnType, body);
        this.extension = extension;
    }

    public Identifier getExtension() {
        return extension;
    }

    public void setExtension(Identifier extension) {
        this.extension = extension;
    }

    @Override
    public String toString() {
        if (getReturnType() != null) {
            return getTokenLiteral() + " " + getName() + "." + getExtension() + "(" + getParameters() + "): " + getReturnType() + " " + getBody();
        }
        return getTokenLiteral() + " " + getName() + "." + getExtension() + "(" + getParameters() + ") " + getBody();
    }
}
