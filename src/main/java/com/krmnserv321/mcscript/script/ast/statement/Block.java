package com.krmnserv321.mcscript.script.ast.statement;

import com.krmnserv321.mcscript.script.ast.Token;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Block extends Statement {
    private List<Statement> statements = new ArrayList<>();

    public Block(Token token) {
        super(token);
    }

    public List<Statement> getStatements() {
        return statements;
    }

    @Override
    public String toString() {
        return "{ " + statements.stream()
                .map(Statement::toString)
                .collect(Collectors.joining(" ")) + " }";
    }
}
