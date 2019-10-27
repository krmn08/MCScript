package com.krmnserv321.mcscript.script.ast.expression;

import com.krmnserv321.mcscript.script.ast.Arguments;
import com.krmnserv321.mcscript.script.ast.Token;
import com.krmnserv321.mcscript.script.ast.statement.Statement;

public class LambdaExpression extends FunctionLiteral {
    public LambdaExpression(Token token, Statement body) {
        super(token, body);
    }

    @Override
    public String toString() {
        Arguments parameters = getParameters();
        if (parameters.isEmpty()) {
            return "() " + getTokenLiteral() + " " + getBody();
        } else if (parameters.size() >= 2) {
            return "(" + parameters + ") " + getTokenLiteral() + " " + getBody();
        }

        return parameters + " " + getTokenLiteral() + " " + getBody();
    }
}
