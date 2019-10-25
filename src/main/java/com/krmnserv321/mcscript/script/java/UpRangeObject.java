package com.krmnserv321.mcscript.script.java;

import java.util.NoSuchElementException;

public class UpRangeObject extends RangeObject {
    public UpRangeObject(int start, int end) {
        super(start, end);
    }

    @Override
    public boolean contains(Number num) {
        return num.doubleValue() >= start && num.doubleValue() <= end;
    }

    @Override
    public boolean hasNext() {
        return num <= end;
    }

    @Override
    public Integer next() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        int ret = num;
        num += step;
        return ret;
    }
}
