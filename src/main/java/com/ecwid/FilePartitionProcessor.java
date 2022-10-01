package com.ecwid;

import java.io.*;
import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.Future;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ExecutionException;

public class FilePartitionProcessor {
    private static final int FILE_BUFFER_SIZE = 1024;

    private final int idx;

    private Future sortFuture;

    private final File inputFile;

    private SortingBuffer buffer;

    private final int sortBufferSize;

    private final SortingBuffer extraBuffer;

    private final List<File> sortResultFiles = new ArrayList<>();

    private final ExecutorService executorService = Executors.newFixedThreadPool(1);

    public FilePartitionProcessor(final int idx,
                                  final File inputFile,
                                  final int sortBufferSize) {
        this.idx = idx;
        this.inputFile = inputFile;
        this.sortBufferSize = sortBufferSize;
        this.buffer = new SortingBuffer(sortBufferSize);
        extraBuffer = new SortingBuffer(sortBufferSize - 1);
    }

    private void sortAndFlush() {
        buffer.sort();

        final File newSortingFIle = new File(inputFile.getParent() + "/chunk_" + idx + "_" + System.nanoTime());
        System.out.println("Flushed to file: " + newSortingFIle.getAbsolutePath() + ", active files: " + sortResultFiles.size());

        final List<Iterable<String>> sortedInputs = new ArrayList<>();
        sortedInputs.add(buffer);
        mergeAndWrite(newSortingFIle, sortedInputs);

        sortResultFiles.add(newSortingFIle);
    }

    private void mergeAndWrite(final File newSortingFIle,
                               final List<Iterable<String>> sortedInputs) {
        try (final BufferedWriter outputStream = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(newSortingFIle)), FILE_BUFFER_SIZE)) {
            final HeapSorter heapSorter = new HeapSorter();
            heapSorter.sortAndEmit(sortedInputs, line -> {
                try {
                    outputStream.write(line);
                    outputStream.newLine();
                } catch (IOException exception) {
                    throw new RuntimeException(exception);
                }
            });
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    public void accept(final String line) {
        if (sortFuture != null) {
            if (!sortFuture.isDone()) {
                if (!extraBuffer.add(line)) {
                    awaitSortingFuture();
                    onSortingDone(line);
                }

                return;
            }

            onSortingDone(line);
            return;
        }

        if (!buffer.add(line)) {
            sortFuture = executorService.submit(this::sortAndFlush);
            extraBuffer.add(line);
        }
    }

    private void awaitSortingFuture() {
        try {
            sortFuture.get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    private void onSortingDone(String line) {
        sortFuture = null;
        buffer = new SortingBuffer(sortBufferSize);
        copyFromExtra(line);
    }

    private void copyFromExtra(final String line) {
        for (final String extraLine : extraBuffer) {
            buffer.add(extraLine);
        }

        if (line != null) {
            buffer.add(line);
        }

        extraBuffer.clear();
    }

    public long calculate() {
        if (sortFuture != null) {
            awaitSortingFuture();
            onSortingDone(null);
        }

        if (!buffer.isEmpty()) {
            buffer.sort();
        }

        return calculateUniqueNumbers();
    }

    private long calculateUniqueNumbers() {
        final HeapSorter heapSorter = new HeapSorter();
        final List<Iterable<String>> sortedInputs = new ArrayList<>();
        sortedInputs.add(buffer);
        sortResultFiles.forEach(file -> sortedInputs.add(new FileIterable(file)));

        final MutableString line = new MutableString();
        final MutableLong counter = new MutableLong();

        heapSorter.sortAndEmit(sortedInputs, nextLine -> {
            if ((line.value == null) || (!line.value.equals(nextLine))) {
                System.out.println("Next line: " + nextLine);
                counter.value++;
            }
            line.value = nextLine;
        });

        buffer = null;
        sortResultFiles.forEach(File::delete);
        sortResultFiles.clear();
        return counter.value;
    }
}
