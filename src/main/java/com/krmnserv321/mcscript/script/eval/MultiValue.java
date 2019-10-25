package com.krmnserv321.mcscript.script.eval;

import java.util.Arrays;
import java.util.stream.Collectors;

public class MultiValue {
    private Object[] elements;

    void setElements(Object[] elements) {
        this.elements = elements;
    }

    Object[] getElements() {
        return elements;
    }

    @Override
    public String toString() {
        return Arrays.stream(elements).map(Object::toString).collect(Collectors.joining(", "));
    }
}
