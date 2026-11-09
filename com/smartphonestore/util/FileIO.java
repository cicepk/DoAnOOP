package com.smartphonestore.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class FileIO {
    private static final String COMMENT_PREFIX = "#";

    // Trả về các dòng dữ liệu, bỏ dòng trống và dòng chú thích #
    public static List<String> readLines(String filePath) {
        List<String> dataLines = new ArrayList<>();
        try {
            for (String line : readAllLines(Paths.get(filePath))) {
                if (!line.isEmpty() && !line.startsWith(COMMENT_PREFIX)) {
                    dataLines.add(line);
                }
            }
        } catch (IOException e) {
            System.err.println("Lỗi đọc file: " + filePath + " - " + e.getMessage());
        }
        return dataLines;
    }

    // Ghi đè dữ liệu nhưng giữ lại dòng chú thích # ở đầu file
    public static boolean writeLines(String filePath, List<String> lines) {
        Path path = Paths.get(filePath);
        try {
            List<String> comments = new ArrayList<>();
            for (String line : readAllLines(path)) {
                if (line.startsWith(COMMENT_PREFIX)) {
                    comments.add(line);
                }
            }
            if (path.getParent() != null) {
                Files.createDirectories(path.getParent());
            }
            try (BufferedWriter bw = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
                for (String comment : comments) {
                    bw.write(comment);
                    bw.newLine();
                }
                for (String line : lines) {
                    bw.write(line);
                    bw.newLine();
                }
            }
            return true;
        } catch (IOException e) {
            System.err.println("Lỗi ghi file: " + filePath + " - " + e.getMessage());
            return false;
        }
    }

    private static List<String> readAllLines(Path path) throws IOException {
        List<String> lines = new ArrayList<>();
        if (!Files.exists(path)) {
            return lines;
        }
        try (BufferedReader br = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            String line;
            while ((line = br.readLine()) != null) {
                // Notepad có thể thêm ký tự BOM vào đầu file
                lines.add(line.replace("﻿", "").trim());
            }
        }
        return lines;
    }
}
