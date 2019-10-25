package com.krmnserv321.mcscript.script.ast.expression;

import com.krmnserv321.mcscript.script.ast.Token;

import java.util.ArrayList;
import java.util.List;

public class ListLiteral extends Expression {
    private List<Expression> elements = new ArrayList<>();

    public ListLiteral(Token token) {
        super(token);
    }

    public List<Expression> getElements() {
        return elements;
    }

    @Override
    public String toString() {
        return elements.toString();
    }
}
