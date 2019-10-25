package com.krmnserv321.mcscript.script.java;

import java.util.*;

public class FieldMap extends LinkedHashMap<String, Object> {
    @Override
    public String toString() {
        List<String> list = new ArrayList<>();
        for (Map.Entry<String, Object> entry : entrySet()) {
            list.add(entry.getKey() + "=" + entry.getValue());
        }

        return String.join("\n", list);
    }
}
