package com.krmnserv321.mcscript.script.ast.expression;

import com.krmnserv321.mcscript.script.ast.Token;
import com.krmnserv321.mcscript.script.ast.expression.literal.StringLiteral;

import java.util.ArrayList;
import java.util.List;

public class StringExpression extends Expression {
    private final List<Expression> expressions = new ArrayList<>();

    public StringExpression(Token token) {
        super(token);
    }

    public List<Expression> getExpressions() {
        return expressions;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        for (Expression expression : expressions) {
            if (expression instanceof StringLiteral) {
                sb.append(expression);
            } else {
                sb.append("${")
                        .append(expression)
                        .append("}");
            }
        }

        return "\"" + sb + "\"";
    }
}
