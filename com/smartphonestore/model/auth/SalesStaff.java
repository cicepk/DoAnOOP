package com.smartphonestore.model.auth;
import com.smartphonestore.model.enums.Role;

public class SalesStaff extends Employee {
    private double commissionRate;
    private double monthlyRevenue;

    public SalesStaff(String id, String fullName, String phoneNumber,
                      double baseSalary, int hireYear, double commissionRate, double monthlyRevenue) {
        super(id, fullName, phoneNumber, baseSalary, Role.SALES, hireYear);

        this.commissionRate = commissionRate;
        this.monthlyRevenue = monthlyRevenue;
    }
    @Override
    public double calculateSalary() {
        return baseSalary + monthlyRevenue * commissionRate;
    }
    public void addMonthlyRevenue(double amount) {
        if (amount > 0) {
            monthlyRevenue += amount;
        }
    }
    @Override
    public String getExtraForFile() {
        return commissionRate + "|" + String.format("%.0f", monthlyRevenue);
    }

    @Override
    public String getExtraInfo() {
        return "Hoa hồng: " + String.format("%.1f", commissionRate * 100) + "%, doanh số: "
             + String.format("%,.0f", monthlyRevenue);
    }

    public boolean setCommissionRate(double commissionRate) {
        if (commissionRate < 0 || commissionRate > 1) return false;
        this.commissionRate = commissionRate;
        return true;
    }
    public double getCommissionRate() { return commissionRate; }
    public double getMonthlyRevenue() { return monthlyRevenue; }
}