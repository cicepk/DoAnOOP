package com.smartphonestore.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;

public final class DateUtil {
    public static final String DATE_PATTERN = "dd/MM/yyyy";
    public static final String DATE_TIME_PATTERN = "dd/MM/yyyy HH:mm:ss";

    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("dd/MM/uuuu").withResolverStyle(ResolverStyle.STRICT);
    private static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("dd/MM/uuuu HH:mm:ss").withResolverStyle(ResolverStyle.STRICT);

    private DateUtil() { }

    public static String format(LocalDate date) {
        return date.format(DATE_FORMATTER);
    }

    public static String today() {
        return format(LocalDate.now());
    }

    public static LocalDate parse(String text) {
        if (text == null || text.trim().isEmpty()) {
            throw new IllegalArgumentException("Ngay khong duoc de trong");
        }
        String s = text.trim();
        if (s.length() > 10) s = s.substring(0, 10);
        try {
            return LocalDate.parse(s, DATE_FORMATTER);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Ngay khong hop le (can dang " + DATE_PATTERN + "): " + text);
        }
    }

    public static String formatDateTime(LocalDateTime dateTime) {
        return dateTime.format(DATE_TIME_FORMATTER);
    }

    public static String nowText() {
        return formatDateTime(LocalDateTime.now());
    }

    public static LocalDateTime parseDateTime(String text) {
        if (text == null || text.trim().isEmpty()) {
            throw new IllegalArgumentException("Ngay gio khong duoc de trong");
        }
        String s = text.trim();
        try {
            if (s.length() <= 10) return LocalDate.parse(s, DATE_FORMATTER).atStartOfDay();
            return LocalDateTime.parse(s, DATE_TIME_FORMATTER);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Ngay gio khong hop le (can dang " + DATE_TIME_PATTERN + "): " + text);
        }
    }
}
