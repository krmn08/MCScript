package com.krmnserv321.mcscript.script.eval;

import java.util.ArrayList;
import java.util.List;

public class VarArgObject {
    private List<Object> arguments = new ArrayList<>();

    List<Object> getArguments() {
        return arguments;
    }

    @Override
    public String toString() {
        return arguments.toString();
    }
}
