package com.krmnserv321.mcscript.script.ast.expression;

import com.krmnserv321.mcscript.script.ast.Node;
import com.krmnserv321.mcscript.script.ast.Token;

public abstract class Expression extends Node {
    public Expression(Token token) {
        super(token);
    }
}
