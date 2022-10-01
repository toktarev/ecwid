package com.ecwid;

import java.io.File;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicLong;

public class FileProcessor {

    private static final int PROGRESS_BAR_CHUNK = 50000000;

    private final File inputFile;

    private final FilePartitionProcessor[] processors = new FilePartitionProcessor[Runtime.getRuntime().availableProcessors() / 2];

    public FileProcessor(final String file,
                         final int sortBufferSize) {
        this.inputFile = new File(file);

        if (!this.inputFile.exists()) {
            throw new RuntimeException("Input file does not exist");
        }

        for (int idx = 0; idx < processors.length; idx++) {
            processors[idx] = new FilePartitionProcessor(
                    idx,
                    inputFile,
                    sortBufferSize
            );
        }
    }

    public long process() {
        long time = System.currentTimeMillis();
        long totalProcessedLines = 0;

        try (final FileIterator iterator = new FileIterator(inputFile)) {
            while (iterator.hasNext()) {
                final String line = iterator.next();
                final int index = Math.abs(line.hashCode()) % processors.length;

                if ((totalProcessedLines > 0) && (totalProcessedLines % PROGRESS_BAR_CHUNK == 0)) {
                    System.out.println("Lines processed " + totalProcessedLines + ", time (ms):" + (System.currentTimeMillis() - time));
                    time = System.currentTimeMillis();
                }

                processors[index].accept(line);
                totalProcessedLines++;
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        final AtomicLong result = new AtomicLong();
        Arrays.stream(processors).parallel().forEach(
                filePartitionProcessor ->
                        result.addAndGet(filePartitionProcessor.calculate())
        );

        return result.get();
    }
}
