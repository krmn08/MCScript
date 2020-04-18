package com.krmnserv321.mcscript.script.ast;

import com.krmnserv321.mcscript.script.eval.Environment;
import com.krmnserv321.mcscript.script.eval.Evaluator;

public abstract class Node {

    private final Token token;

    public Node(Token token) {
        this.token = token;
    }

    public Token getToken() {
        return token;
    }

    public String getTokenLiteral() {
        return token.toString();
    }

    public Object eval(Environment environment) {
        return Evaluator.eval(environment, this);
    }

    @Override
    public String toString() {
        return getTokenLiteral();
    }
}
