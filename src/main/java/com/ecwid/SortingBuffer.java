package com.ecwid;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;

public class SortingBuffer implements Iterable<String> {
    private final int sortBufferSize;

    private final List<String> lines;

    public SortingBuffer(final int sortBufferSize) {
        this.sortBufferSize = sortBufferSize;
        this.lines = new ArrayList<>(sortBufferSize);
    }

    public void clear() {
        lines.clear();
    }

    public boolean add(String line) {
        if (lines.size() == sortBufferSize) {
            return false;
        }

        lines.add(line);
        return true;
    }

    public void sort() {
        Collections.sort(lines);
    }

    @Override
    public Iterator<String> iterator() {
        return lines.iterator();
    }

    public boolean isEmpty() {
        return lines.isEmpty();
    }
}
