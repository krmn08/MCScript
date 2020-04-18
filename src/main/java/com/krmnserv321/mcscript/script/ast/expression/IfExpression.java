package com.krmnserv321.mcscript.script.ast.expression;

import com.krmnserv321.mcscript.script.ast.Token;
import com.krmnserv321.mcscript.script.ast.statement.Statement;

import java.util.ArrayList;
import java.util.List;

public class IfExpression extends Expression {
    private Expression condition;
    private Statement consequence;

    private final List<IfExpression> elifList = new ArrayList<>();

    private Statement alternative;

    public IfExpression(Token token, Expression condition, Statement consequence) {
        super(token);
        this.condition = condition;
        this.consequence = consequence;
    }

    public IfExpression(Token token, Expression condition, Statement consequence, Statement alternative) {
        this(token, condition, consequence);
        this.alternative = alternative;
    }

    public Expression getCondition() {
        return condition;
    }

    public void setCondition(Expression condition) {
        this.condition = condition;
    }

    public Statement getConsequence() {
        return consequence;
    }

    public void setConsequence(Statement consequence) {
        this.consequence = consequence;
    }

    public List<IfExpression> getElifList() {
        return elifList;
    }

    public Statement getAlternative() {
        return alternative;
    }

    public void setAlternative(Statement alternative) {
        this.alternative = alternative;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getTokenLiteral())
                .append(" (")
                .append(condition)
                .append(") ")
                .append(consequence);
        for (IfExpression expression : elifList) {
            sb.append(" elif (")
                    .append(expression.getCondition())
                    .append(") ")
                    .append(expression.getConsequence());
        }
        if (alternative != null) {
            sb.append(" else ")
                    .append(alternative);
        }

        return sb.toString();
    }
}
