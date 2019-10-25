package com.krmnserv321.mcscript.script.eval;

import com.krmnserv321.mcscript.script.ast.statement.Statement;

public class DeferObject {
    private Environment environment;
    private Statement statement;

    DeferObject(Environment environment, Statement statement) {
        this.environment = environment;
        this.statement = statement;
    }

    public Environment getEnvironment() {
        return environment;
    }

    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    public Statement getStatement() {
        return statement;
    }

    public void setStatement(Statement statement) {
        this.statement = statement;
    }

    @Override
    public String toString() {
        return statement.toString();
    }
}
