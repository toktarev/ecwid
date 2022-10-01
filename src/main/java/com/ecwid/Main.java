package com.ecwid;

public class Main {
    public static void main(String[] args) {
        if (args.length != 1) {
            throw new IllegalStateException("Please use only 1 input argument as input file name path");
        }

        final String fileName = args[0];
        final FileProcessor processor = new FileProcessor(fileName, 1024 * 1024);
        System.out.println("Number of unique lines: " + processor.process());
        System.exit(0);
    }
}
