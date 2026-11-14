package com.smartphonestore.model.auth;

import com.smartphonestore.model.enums.Role;
import java.time.LocalDate;

public abstract class Employee implements ISalaryCalculable {
    protected String id;
    protected String fullName;
    protected String phoneNumber;
    protected double  baseSalary;
    protected Role role;
    protected int hireYear;

    public Employee(String id, String fullName, String phoneNumber, 
        double baseSalary, Role role, int hireYear) {
            this.id = id;
            this.fullName = fullName;
            this.phoneNumber = phoneNumber;
            this.baseSalary = baseSalary;
            this.role = role;
            this.hireYear = hireYear;
    }
    public int getSeniorityYears(){
        // Lấy năm nay - năm vào làm, không để âm
        return Math.max(0, LocalDate.now().getYear() - hireYear);
    }
    // Lớp con viết riêng trong file
    public abstract String getExtraForFile();
    // Lớp con in ra riêng 
    public abstract String getExtraInfo();
    public String toFileString() {
        return id + "|" + fullName + "|" + phoneNumber + "|"
            + String.format("%.0f", baseSalary) + "|" + role.name() + "|"  // %.0f: số thực, 0 chữ số thập phân
            + hireYear + "|" + getExtraForFile();  // Tự gọi đúng hàm của lớp con
    }
    public void display() {
        System.out.printf("%-6s | %-22s | %-11s | %-10s | %,12.0f | %2d năm | %s%n",
            id, fullName, phoneNumber, role.name(), baseSalary, getSeniorityYears(), getExtraInfo());
        // %,12.0f: có dấu phân cách hàng nghìn, rộng 12 ô. %2d: số nguyên rộng 2 ô
    }
    public String getId() { return id; }
    public String getFullName() { return fullName; }
    public double getBaseSalary() { return baseSalary; } 
    public String getPhoneNumber() { return phoneNumber; }
    public Role getRole() { return role; }
    public int getHireYear() { return hireYear; }

    // Cho menu kiểm tra trước khi tạo object
    public static boolean isValidText(String text) {
        return text != null && !text.trim().isEmpty() && !text.contains("|");
    }
    public static boolean isValidPhone(String phone) {
        return phone != null && phone.matches("0\\d{9}"); // 0 + 9 chữ số
    }

    public boolean setFullName(String fullName) {
        if (!isValidText(fullName)) return false;
        this.fullName = fullName.trim();
        return true;
    }

    public boolean setPhoneNumber(String phoneNumber) {
        if (!isValidPhone(phoneNumber)) return false;
        this.phoneNumber = phoneNumber;
        return true;
    }

    public boolean  setBaseSalary(double baseSalary) {
        // Lương không âm
        if (baseSalary < 0) return false;
        this.baseSalary = baseSalary;
        return true;
    }

}