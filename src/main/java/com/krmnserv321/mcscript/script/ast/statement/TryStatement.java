package com.krmnserv321.mcscript.script.ast.statement;

import com.krmnserv321.mcscript.script.ast.Token;
import com.krmnserv321.mcscript.script.ast.expression.Identifier;

public class TryStatement extends Statement {
    private Block body;
    private Identifier error;
    private Block catchBody;

    public TryStatement(Token token, Block body, Identifier error, Block catchBody) {
        super(token);
        this.body = body;
        this.error = error;
        this.catchBody = catchBody;
    }

    public Block getBody() {
        return body;
    }

    public void setBody(Block body) {
        this.body = body;
    }

    public Identifier getError() {
        return error;
    }

    public void setError(Identifier error) {
        this.error = error;
    }

    public Block getCatchBody() {
        return catchBody;
    }

    public void setCatchBody(Block catchBody) {
        this.catchBody = catchBody;
    }

    @Override
    public String toString() {
        return getTokenLiteral() + " " + body + " catch (" + error + ") " + catchBody;
    }
}
