package com.smartphonestore.util;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;

/**
 * Định dạng ngày DÙNG CHUNG cho cả nhóm: dd/MM/yyyy.
 * TV3 ghi ngày bán, TV4 đọc ngày bán để tính hạn bảo hành, nên hai bên phải dùng cùng một định dạng.
 */
public final class DateUtil {
    public static final String PATTERN = "dd/MM/yyyy";
    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("dd/MM/uuuu").withResolverStyle(ResolverStyle.STRICT);

    private DateUtil() { }

    public static String format(LocalDate date) {
        return date.format(FORMATTER);
    }

    public static String today() {
        return format(LocalDate.now());
    }

    /** Đọc chuỗi dd/MM/yyyy (chấp nhận cả "dd/MM/yyyy HH:mm:ss", chỉ lấy phần ngày). Sai định dạng thì ném IllegalArgumentException. */
    public static LocalDate parse(String text) {
        if (text == null || text.trim().isEmpty()) {
            throw new IllegalArgumentException("Ngày không được để trống");
        }
        String s = text.trim();
        if (s.length() > 10) s = s.substring(0, 10);
        try {
            return LocalDate.parse(s, FORMATTER);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Ngày không hợp lệ (cần dạng " + PATTERN + "): " + text);
        }
    }
}
