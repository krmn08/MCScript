package com.krmnserv321.mcscript.script.java;

import com.krmnserv321.mcscript.script.eval.ScriptError;

public class ErrorObject {
    private ScriptError error;

    public ErrorObject(ScriptError error) {
        this.error = error;
    }

    public int getLineNumber() {
        return error.getLineNumber();
    }

    public String getMessage() {
        return error.getMessage();
    }

    public void print() {
        error.print();
    }

    @Override
    public String toString() {
        return error.toString();
    }
}
