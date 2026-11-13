package com.smartphonestore.util;

import java.util.Scanner;

public class InputHelper {
    private static final Scanner scanner = new Scanner(System.in);

    // Đọc chuỗi bắt buộc (không được để trống)
    public static String readString(String text) {
        while (true) {
            System.out.print(text);
            String input = scanner.nextLine().trim();
            if (!input.isEmpty()) return input;
            System.out.println("(!) Du lieu khong duoc de trong. Vui long nhap lai!");
        }
    }

    // Đọc chuỗi tùy chọn (cho phép Enter để lấy giá trị mặc định)
    public static String readOptionalString(String text) {
        System.out.print(text);
        return scanner.nextLine().trim();
    }

    // Đọc số nguyên trong khoảng [min, max]
    public static int readInt(String text, int min, int max) {
        while (true) {
            System.out.print(text);
            try {
                int value = Integer.parseInt(scanner.nextLine().trim());
                if (value >= min && value <= max) return value;
                System.out.printf("(!) Vui long nhap so trong khoang [%d - %d].%n", min, max);
            } catch (NumberFormatException e) {
                System.out.println("(!) Loi: Phai nhap so nguyen hop le!");
            }
        }
    }

    // Đọc số thực >= min
    public static double readDouble(String text, double min) {
        while (true) {
            System.out.print(text);
            try {
                double value = Double.parseDouble(scanner.nextLine().trim());
                if (value >= min) return value;
                System.out.printf("(!) Gia tri phai >= %,.0f.%n", min);
            } catch (NumberFormatException e) {
                System.out.println("(!) Loi: Phai nhap so thuc hop le!");
            }
        }
    }

    // Đọc số thực trong khoảng [min, max]
    public static double readDouble(String text, double min, double max) {
        while (true) {
            double value = readDouble(text, min);
            if (value <= max) return value;
            System.out.println("(!) Gia tri phai <= " + max + ".");
        }
    }

    // Hỏi Yes/No (Y/N)
    public static boolean readYesNo(String text) {
        while (true) {
            System.out.print(text);
            String input = scanner.nextLine().trim();
            if (input.equalsIgnoreCase("Y")) return true;
            if (input.equalsIgnoreCase("N")) return false;
            System.out.println("(!) Vui long nhap Y hoac N!");
        }
    }
}
