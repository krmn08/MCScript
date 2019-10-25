package com.krmnserv321.mcscript.script.ast.expression;

import com.krmnserv321.mcscript.script.ast.Token;
import com.krmnserv321.mcscript.script.ast.statement.Statement;

import java.util.LinkedHashMap;
import java.util.Map;

public class SwitchExpression extends Expression {
    private Expression value;

    private Map<Expression, Statement> caseMap = new LinkedHashMap<>();

    public SwitchExpression(Token token, Expression value) {
        super(token);
        this.value = value;
    }

    public Expression getValue() {
        return value;
    }

    public void setValue(Expression value) {
        this.value = value;
    }

    public Map<Expression, Statement> getCaseMap() {
        return caseMap;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getTokenLiteral())
                .append(" (")
                .append(value)
                .append(") { ");
        caseMap.forEach((k, v) ->
                sb.append(k)
                .append(" -> ")
                .append(v)
                .append(" ")
        );
        sb.append("}");
        return sb.toString();
    }
}
