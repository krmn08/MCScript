package com.krmnserv321.mcscript.script.ast.expression;

import com.krmnserv321.mcscript.script.ast.Token;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class MapLiteral extends Expression {
    private final Map<Expression, Expression> map = new LinkedHashMap<>();

    public MapLiteral(Token token) {
        super(token);
    }

    public Map<Expression, Expression> getMap() {
        return map;
    }

    @Override
    public String toString() {
        return "{" + map.entrySet().stream().map(entry -> entry.getKey() + ":" + entry.getValue()).collect(Collectors.joining(", ")) + "}";
    }
}
