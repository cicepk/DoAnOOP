package com.smartphonestore.model.auth;
import com.smartphonestore.model.enums.Role;

public class WarehouseStaff extends Employee {
    // Cố định 50k/h
    private static final double OVERTIME_PAY = 50000;
    private static final int MAX_OVERTIME_HOURS = 200;
    private String warehouseZone;
    private int overtimeHours;

    public WarehouseStaff(String id, String fullName, String phoneNumber,
                      double baseSalary, int hireYear, String warehouseZone, int overtimeHours) {
        super(id, fullName, phoneNumber, baseSalary, Role.WAREHOUSE, hireYear);

        this.warehouseZone = warehouseZone;
        this.overtimeHours = overtimeHours;
    }
    @Override
    public double calculateSalary() {
        return baseSalary + overtimeHours * OVERTIME_PAY;
    }
    // Ghi riêng xuống file
    @Override
    public String getExtraForFile() {
        return warehouseZone + "|" + overtimeHours;
    }
    // In ra màn hình
    @Override
    public String getExtraInfo() {
        return "Khu: " + warehouseZone + ", tăng ca: " + overtimeHours + " giờ";
    }

    public boolean setWarehouseZone(String warehouseZone) {
        if (warehouseZone == null) return false;
        String zone = warehouseZone.trim().toUpperCase();
        if (!zone.matches("KHO_[A-Z]")) return false;
        this.warehouseZone = zone;
        return true;
    }

    public boolean setOvertimeHours(int overtimeHours) {
        if (overtimeHours < 0 || overtimeHours > MAX_OVERTIME_HOURS) return false;
        this.overtimeHours = overtimeHours;
        return true;
    }

    public String getWarehouseZone() { return warehouseZone; }
    public int getOvertimeHours() { return overtimeHours; }

}