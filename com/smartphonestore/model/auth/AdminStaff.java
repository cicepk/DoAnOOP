package com.smartphonestore.model.auth;
import com.smartphonestore.model.enums.Role;

public class AdminStaff extends Employee {
    // Phụ cấp
    private double managementBonus;

    public AdminStaff(String id, String fullName, String phoneNumber,
                      double baseSalary, int hireYear, double managementBonus) {
        super(id, fullName, phoneNumber, baseSalary, Role.ADMIN, hireYear);

        this.managementBonus = managementBonus;
    }
    @Override
    public double calculateSalary() {
        return baseSalary + managementBonus;
    }
     @Override
    public String getExtraForFile() {
        return String.format("%.0f", managementBonus);  // Phần riêng ghi xuống file
    }

    @Override
    public String getExtraInfo() {
        return "Phụ cấp: " + String.format("%,.0f", managementBonus);  // Phần riêng in ra màn hình
    }
    // Setter để menu [5] sửa
    public boolean setManagementBonus(double managementBonus) {
        if (managementBonus < 0) return false;
        this.managementBonus = managementBonus;
        return true;
    }
    public double getManagementBonus() {
        return managementBonus;
    }
}