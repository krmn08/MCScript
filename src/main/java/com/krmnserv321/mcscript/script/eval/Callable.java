package com.krmnserv321.mcscript.script.eval;

import com.krmnserv321.mcscript.script.ast.Token;
import com.krmnserv321.mcscript.script.java.Pair;

public abstract class Callable {
    protected Token token;

    public void setToken(Token token) {
        this.token = token;
    }

    public Token getToken() {
        return token;
    }

    public abstract Object call(Object... args);
    public abstract Object call(Pair... args);
}
