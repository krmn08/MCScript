package com.krmnserv321.mcscript.script.java;

import com.google.common.base.Objects;

public class Pair {
    private Object first;
    private Object second;

    public Pair(Object first, Object second) {
        this.first = first;
        this.second = second;
    }

    public Object getFirst() {
        return first;
    }

    public void setFirst(Object first) {
        this.first = first;
    }

    public Object getSecond() {
        return second;
    }

    public void setSecond(Object second) {
        this.second = second;
    }

    @Override
    public String toString() {
        return first + ":" + second;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Pair pair = (Pair) o;
        return Objects.equal(first, pair.first) &&
                Objects.equal(second, pair.second);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(first, second);
    }
}
