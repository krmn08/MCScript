package com.krmnserv321.mcscript.script.java;

import java.util.Iterator;

public abstract class RangeObject implements Iterator<Integer>, Iterable<Integer> {
    protected int start;
    protected int end;
    protected int num;
    protected int step = 1;

    protected RangeObject(int start, int end) {
        this.start = start;
        this.end = end;
        this.num = start;
    }

    public void setStep(int step) {
        this.step = step;
    }

    public abstract boolean contains(Number num);

    @Override
    public Iterator<Integer> iterator() {
        return this;
    }

    @Override
    public String toString() {
        return start + ".." + end;
    }
}