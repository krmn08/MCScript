package com.krmnserv321.mcscript.script.ast.statement;

import com.krmnserv321.mcscript.script.ast.Node;
import com.krmnserv321.mcscript.script.ast.Token;

public abstract class Statement extends Node {
    public Statement(Token token) {
        super(token);
    }
}
