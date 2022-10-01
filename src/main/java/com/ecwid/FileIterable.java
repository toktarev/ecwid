package com.ecwid;

import java.io.File;
import java.util.Iterator;

public class FileIterable implements Iterable<String> {
    private final File file;

    public FileIterable(final File file) {
        this.file = file;
    }

    @Override
    public Iterator<String> iterator() {
        return new FileIterator(file);
    }
}
