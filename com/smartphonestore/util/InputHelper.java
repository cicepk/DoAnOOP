package com.smartphonestore.util;
import java.util.Scanner; 

public class InputHelper {
    private static final Scanner scanner = new Scanner(System.in);

    public static String readString(String text) {
        while (true) {
            System.out.print(text);
            String input = scanner.nextLine().trim();
            if (!input.isEmpty()) return input;
            System.out.println("(!) Dữ liệu không được để trống. Vui lòng nhập lại!");
        }
    }
    public static int readInt(String text, int min, int max) {
        while(true) {
            System.out.print(text);
            try {
                int value = Integer.parseInt(scanner.nextLine().trim());
                if (value >= min && value <= max) return value;
                System.out.printf("(!) Vui lòng nhập số trong khoảng [%d - %d].%n", min, max);
            } catch (NumberFormatException e) {
                System.out.println("(!) Lỗi: Phải nhập số nguyên hợp lệ!");
            }
        }
    }
    public static double readDouble(String text, double min) {
        while (true) {
            System.out.print(text);
            try {
                double value = Double.parseDouble(scanner.nextLine().trim());
                if (value >= min) return value;
                System.out.printf("(!) Giá trị phải >= %.0f.%n", min);
            } catch (NumberFormatException e) {
                System.out.println("(!) Lỗi: Phải nhập số thực hợp lệ!");
            }
        }
    }
}