package com.krmnserv321.mcscript.script.ast.expression;

import com.krmnserv321.mcscript.script.ast.Token;
import com.krmnserv321.mcscript.script.ast.statement.Statement;

public class LambdaExpression extends FunctionLiteral {
    public LambdaExpression(Token token, Statement body) {
        super(token, body);
    }

    @Override
    public String toString() {
        return getParameters() + " " + getTokenLiteral() + " " + getBody();
    }
}
