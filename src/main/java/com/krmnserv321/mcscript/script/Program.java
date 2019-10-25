package com.krmnserv321.mcscript.script;

import com.krmnserv321.mcscript.script.ast.Node;
import com.krmnserv321.mcscript.script.ast.Token;
import com.krmnserv321.mcscript.script.ast.TokenType;
import com.krmnserv321.mcscript.script.ast.statement.Statement;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Program extends Node {
    private List<Statement> statements = new ArrayList<>();

    public Program() {
        super(new Token(TokenType.Root, "root"));
    }

    public List<Statement> getStatements() {
        return statements;
    }

    @Override
    public String toString() {
        return statements.stream()
                .map(Statement::toString)
                .collect(Collectors.joining("\n"));
    }

    @Override
    public String getTokenLiteral() {
        if (statements.size() > 0) {
            return statements.get(0).getTokenLiteral();
        } else {
            return "";
        }
    }
}
