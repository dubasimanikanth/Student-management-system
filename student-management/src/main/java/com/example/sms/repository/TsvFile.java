package com.example.sms.repository;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/** Tiny helper for reading and writing tab-separated files. */
final class TsvFile {

    private TsvFile() {
    }

    static List<String[]> read(Path file) {
        if (!Files.exists(file)) {
            return new ArrayList<>();
        }
        try {
            List<String[]> rows = new ArrayList<>();
            for (String line : Files.readAllLines(file, StandardCharsets.UTF_8)) {
                if (!line.isBlank()) {
                    rows.add(line.split("\t", -1));
                }
            }
            return rows;
        } catch (IOException e) {
            throw new UncheckedIOException("Could not read " + file, e);
        }
    }

    static void write(Path file, List<String[]> rows) {
        List<String> lines = new ArrayList<>();
        for (String[] row : rows) {
            String[] clean = new String[row.length];
            for (int i = 0; i < row.length; i++) {
                clean[i] = row[i].replaceAll("[\\t\\r\\n]", " ");
            }
            lines.add(String.join("\t", clean));
        }
        try {
            if (file.getParent() != null) {
                Files.createDirectories(file.getParent());
            }
            Files.write(file, lines, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException("Could not write " + file, e);
        }
    }
}
