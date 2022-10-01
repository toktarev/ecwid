package com.ecwid;

import java.io.*;
import java.util.Iterator;

public class FileIterator implements Iterator<String>, AutoCloseable {
    private static final int FILE_BUFFER_SIZE = 256;

    private final BufferedReader reader;
    private String line;

    public FileIterator(final File file) {
        try {
            this.reader = new BufferedReader(new FileReader(file), FILE_BUFFER_SIZE);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean hasNext() {
        if (line != null) {
            return true;
        }

        try {
            line = reader.readLine();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }

        return line != null;
    }

    @Override
    public String next() {
        if (!hasNext()) {
            throw new IllegalStateException();
        }

        try {
            return line;
        } finally {
            line = null;
        }
    }

    @Override
    public void close() throws Exception {
        reader.close();
    }
}

