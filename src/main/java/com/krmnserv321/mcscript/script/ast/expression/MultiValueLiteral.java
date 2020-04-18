package com.krmnserv321.mcscript.script.ast.expression;

import com.krmnserv321.mcscript.script.ast.Token;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class MultiValueLiteral extends Expression {
    private final List<Expression> elements = new ArrayList<>();

    public MultiValueLiteral(Token token) {
        super(token);
    }

    public List<Expression> getElements() {
        return elements;
    }

    @Override
    public String toString() {
        return elements.stream().map(Expression::toString).collect(Collectors.joining(", "));
    }
}
