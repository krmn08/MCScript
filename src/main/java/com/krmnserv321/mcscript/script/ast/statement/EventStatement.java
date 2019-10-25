package com.krmnserv321.mcscript.script.ast.statement;

import com.krmnserv321.mcscript.script.ast.Token;
import com.krmnserv321.mcscript.script.ast.expression.Identifier;
import org.bukkit.event.EventPriority;

public class EventStatement extends Statement {
    private Identifier eventName;
    private EventPriority priority;
    private Block body;

    public EventStatement(Token token, Identifier eventName, EventPriority priority, Block body) {
        super(token);
        this.eventName = eventName;
        this.priority = priority;
        this.body = body;
    }

    public Identifier getEventName() {
        return eventName;
    }

    public void setEventName(Identifier eventName) {
        this.eventName = eventName;
    }

    public EventPriority getPriority() {
        return priority;
    }

    public void setPriority(EventPriority priority) {
        this.priority = priority;
    }

    public Block getBody() {
        return body;
    }

    public void setBody(Block body) {
        this.body = body;
    }

    @Override
    public String toString() {
        return getTokenLiteral() + eventName + " { " + body + " }";
    }
}
