package com.ecwid;

import org.junit.Test;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertEquals;

public class TestLinesCalculator {

    @Test
    public void test() throws Exception {
        final String path = "src/test/resources/Lines";

        final File file = new File(path);
        final String absolutePath = file.getAbsolutePath();
        final FileProcessor processor = new FileProcessor(absolutePath, 16);

        try (final FileIterator fileIterator = new FileIterator(file)) {
            Set<String> set = new HashSet<>();

            while (fileIterator.hasNext()) {
                set.add(fileIterator.next());
            }

            assertEquals(set.size(), processor.process());
        }
    }
}
