package com.krmnserv321.mcscript.script.eval;

import com.krmnserv321.mcscript.script.ast.Token;

public class ScriptError {
    private Token token;
    private String message;

    public ScriptError(Token token, String message) {
        this.token = token;
        this.message = message;
    }

    public ScriptError(Token token, Throwable throwable) {
        this.token = token;
        this.message = throwable.toString();
    }

    public Token getToken() {
        return token;
    }

    public int getLineNumber() {
        return token.getLineNumber();
    }

    public String getMessage() {
        return message;
    }

    public void print() {
        System.out.println(this);
    }

    @Override
    public String toString() {
        String path = token.getPath();

        if (path.isEmpty()) {
            return "line:" + getLineNumber() + " " + message;
        }
        return path + " line:" + getLineNumber() + " " + message;
    }
}
