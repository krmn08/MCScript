package com.krmnserv321.mcscript.script.ast.expression;

import com.krmnserv321.mcscript.script.ast.Arguments;
import com.krmnserv321.mcscript.script.ast.Token;

public class CallExpression extends Expression {
    private Expression function;
    private Arguments arguments = new Arguments();

    public CallExpression(Token token, Expression function) {
        super(token);
        this.function = function;
    }

    public Expression getFunction() {
        return function;
    }

    public void setFunction(Expression function) {
        this.function = function;
    }

    public Arguments getArguments() {
        return arguments;
    }

    @Override
    public String toString() {
        return function + "(" + arguments + ")";
    }
}
