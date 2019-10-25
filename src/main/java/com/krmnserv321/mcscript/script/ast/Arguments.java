package com.krmnserv321.mcscript.script.ast;

import com.krmnserv321.mcscript.script.ast.expression.None;
import com.krmnserv321.mcscript.script.ast.expression.PairLiteral;

import java.util.ArrayList;
import java.util.stream.Collectors;

public class Arguments extends ArrayList<PairLiteral> {
    @Override
    public String toString() {
        return stream().map(literal -> {
            if (literal.getFirst() == None.NONE) {
                return literal.getSecond().toString();
            } else if (literal.getSecond() == None.NONE) {
                return literal.getFirst().toString();
            } else {
                return literal.getFirst() + ":" + literal.getSecond();
            }
        }).collect(Collectors.joining(", "));
    }
}
