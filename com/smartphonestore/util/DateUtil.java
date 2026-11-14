package com.smartphonestore.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;

/**
 * Định dạng ngày DÙNG CHUNG cho cả nhóm.
 *   - Ngày:         dd/MM/yyyy            (ví dụ 18/10/2026)
 *   - Ngày kèm giờ: dd/MM/yyyy HH:mm:ss   (ví dụ 18/10/2026 14:00:00)
 */
public final class DateUtil {
    public static final String DATE_PATTERN = "dd/MM/yyyy";
    public static final String DATE_TIME_PATTERN = "dd/MM/yyyy HH:mm:ss";

    // Dùng "uuuu" (không dùng "yyyy") vì chế độ STRICT bắt buộc có kỷ nguyên nếu dùng yyyy
    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("dd/MM/uuuu").withResolverStyle(ResolverStyle.STRICT);
    private static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("dd/MM/uuuu HH:mm:ss").withResolverStyle(ResolverStyle.STRICT);

    private DateUtil() { }

    // ---------- Ngày ----------
    public static String format(LocalDate date) {
        return date.format(DATE_FORMATTER);
    }

    public static String today() {
        return format(LocalDate.now());
    }

    /** Đọc ngày dd/MM/yyyy. Nếu chuỗi có kèm giờ thì chỉ lấy phần ngày. Sai định dạng thì ném IllegalArgumentException. */
    public static LocalDate parse(String text) {
        if (text == null || text.trim().isEmpty()) {
            throw new IllegalArgumentException("Ngày không được để trống");
        }
        String s = text.trim();
        if (s.length() > 10) s = s.substring(0, 10);
        try {
            return LocalDate.parse(s, DATE_FORMATTER);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Ngày không hợp lệ (cần dạng " + DATE_PATTERN + "): " + text);
        }
    }

    // ---------- Ngày kèm giờ ----------
    public static String formatDateTime(LocalDateTime dateTime) {
        return dateTime.format(DATE_TIME_FORMATTER);
    }

    /** Thời điểm hiện tại dạng chữ, ví dụ "13/11/2026 00:19:45". */
    public static String nowText() {
        return formatDateTime(LocalDateTime.now());
    }

    /** Đọc "dd/MM/yyyy HH:mm:ss". Nếu chỉ có ngày thì coi là 00:00:00 của ngày đó. */
    public static LocalDateTime parseDateTime(String text) {
        if (text == null || text.trim().isEmpty()) {
            throw new IllegalArgumentException("Ngày giờ không được để trống");
        }
        String s = text.trim();
        try {
            if (s.length() <= 10) return LocalDate.parse(s, DATE_FORMATTER).atStartOfDay();
            return LocalDateTime.parse(s, DATE_TIME_FORMATTER);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Ngày giờ không hợp lệ (cần dạng " + DATE_TIME_PATTERN + "): " + text);
        }
    }
}