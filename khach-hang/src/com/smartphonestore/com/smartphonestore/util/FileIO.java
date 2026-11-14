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

// GIẢ LẬP: chép đúng theo QUY_CHUAN_CODE.md mục 4. Dùng bản của nhóm khi ghép.
public class FileIO {
    public static List<String> readLines(String filePath) {
        List<String> lines = new ArrayList<>();
        Path path = Paths.get(filePath);
        if (!Files.exists(path)) return lines;

        try (BufferedReader br = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (!line.isEmpty()) {
                    lines.add(line);
                }
            }
        } catch (IOException e) {
            System.err.println("Lỗi đọc file: " + filePath + " - " + e.getMessage());
        }
        return lines;
    }

    public static boolean writeLines(String filePath, List<String> lines) {
        Path path = Paths.get(filePath);
        try {
            if (path.getParent() != null && !Files.exists(path.getParent())) {
                Files.createDirectories(path.getParent());
            }
            try (BufferedWriter bw = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
                for (String line : lines) {
                    bw.write(line);
                    bw.newLine();
                }
                return true;
            }
        } catch (IOException e) {
            System.err.println("Lỗi ghi file: " + filePath + " - " + e.getMessage());
            return false;
        }
    }
}
