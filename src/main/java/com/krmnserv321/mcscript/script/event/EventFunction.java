package com.krmnserv321.mcscript.script.event;

import com.krmnserv321.mcscript.script.ast.Token;
import com.krmnserv321.mcscript.script.ast.statement.Block;
import com.krmnserv321.mcscript.script.eval.Environment;
import com.krmnserv321.mcscript.script.eval.Function;
import org.bukkit.event.EventPriority;

public class EventFunction extends Function {
    private EventPriority priority;

    public EventFunction(Environment environment, Token token, EventPriority priority, Block body) {
        super(environment, body);
        setToken(token);
        this.priority = priority;
    }

    EventPriority getPriority() {
        return priority;
    }
}
