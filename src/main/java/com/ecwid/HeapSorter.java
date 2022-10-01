package com.ecwid;

import java.util.List;
import java.util.Iterator;
import java.util.PriorityQueue;

public class HeapSorter {
    private static class Element implements Comparable<Element> {
        private final int index;

        public String inputString;

        public Element(int index) {
            this.index = index;
        }

        public int getIndex() {
            return index;
        }

        @Override
        public int compareTo(Element o) {
            return inputString.compareTo(o.inputString);
        }
    }

    private void processInput(Iterator<String>[] iterators,
                              PriorityQueue<Element> minHeap,
                              Element[] elements,
                              int idx) {
        final Iterator<String> iterator = iterators[idx];

        if (iterator == null) {
            return;
        }

        if (iterator.hasNext()) {
            elements[idx].inputString = iterator.next();
            minHeap.add(elements[idx]);
        } else {
            iterators[idx] = null;
        }
    }

    public void sortAndEmit(final List<? extends Iterable<String>> sortedInputs,
                            final Emiter emiter) {
        final Iterator<String>[] iterators = new Iterator[sortedInputs.size()];
        final PriorityQueue<Element> minHeap = new PriorityQueue<>(sortedInputs.size());
        final Element[] elements = new Element[sortedInputs.size()];

        for (int idx = 0; idx < sortedInputs.size(); idx++) {
            iterators[idx] = sortedInputs.get(idx).iterator();
            elements[idx] = new Element(idx);
            processInput(iterators, minHeap, elements, idx);
        }

        while (!minHeap.isEmpty()) {
            final Element nextElement = minHeap.poll();
            emiter.emit(nextElement.inputString);
            int nextIndex = nextElement.getIndex();
            processInput(iterators, minHeap, elements, nextIndex);
        }
    }
}
