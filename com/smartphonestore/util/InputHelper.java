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
    // Chuỗi sẽ được ghi vào file: không cho chứa '|' vì '|' là ký tự ngăn cột trong file .txt
    public static String readText(String text) {
        while (true) {
            String input = readString(text);
            if (!input.contains("|")) return input;
            System.out.println("(!) Không được chứa ký tự '|'. Vui lòng nhập lại!");
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
                // parseDouble nhận cả chữ "Infinity" / "NaN", phải chặn riêng
                if (Double.isNaN(value) || Double.isInfinite(value)) {
                    System.out.println("(!) Lỗi: Phải nhập số thực hợp lệ!");
                    continue;
                }
                if (value >= min) return value;
                System.out.printf("(!) Giá trị phải >= %.0f.%n", min);
            } catch (NumberFormatException e) {
                System.out.println("(!) Lỗi: Phải nhập số thực hợp lệ!");
            }
        }
    }
    // Số thực trong khoảng [min, max] (VD: tỷ lệ hoa hồng 0 - 1)
    public static double readDouble(String text, double min, double max) {
        while (true) {
            double value = readDouble(text, min);
            if (value <= max) return value;
            System.out.println("(!) Giá trị phải <= " + max + ".");
        }
    }
    // Hỏi Y/N, nhập sai thì hỏi lại
    public static boolean readYesNo(String text) {
        while (true) {
            System.out.print(text);
            String input = scanner.nextLine().trim();
            if (input.equalsIgnoreCase("Y")) return true;
            if (input.equalsIgnoreCase("N")) return false;
            System.out.println("(!) Vui lòng nhập Y hoặc N!");
        }
    }

    // Đọc chuỗi tùy chọn (cho phép nhấn Enter để bỏ qua / nhận giá trị mặc định)
    public static String readOptionalString(String text) {
        System.out.print(text);
        return scanner.nextLine().trim();
    }
}