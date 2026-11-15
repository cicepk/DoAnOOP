package com.smartphonestore.model.product;

import com.smartphonestore.exception.ValidationException;

public class Featurephone extends Phone {
    private int batteryLifeDays;
    private boolean sosModeEnable;// Trạng thái phím khẩn cấp SOS

    public Featurephone(String id, String name, String brand, double price, int stockQuantity, int minStockThreshold,
            String operatingSystem, int batteryLifeDays) {
        super(id, name, brand, price, stockQuantity, minStockThreshold, operatingSystem);
        setBatteryLifeDays(batteryLifeDays);
        this.sosModeEnable = true;
    }

    // phương thức riêng: bật/tắt phím SOS
    public void toggleEmergencySOS() {
        this.sosModeEnable = !this.sosModeEnable;
        System.out.println("Chế độ khẩn cấp SOS của máy " + getName() +
                " hiện đang " + (sosModeEnable ? "BẬT" : "TẮT"));
    }

    @Override 
    public String toFileString() {
        return String.format("%s|%s|%s|%.0f|%d|FEATUREPHONE|%d|%d",
                getId(), getName(), getBrand(), getPrice(), getStockQuantity(), getMinStockThreshold(), batteryLifeDays);
    }

    @Override
    public void displayInfo() {
        System.out.printf("[PHỔ THÔNG]  ID: %-6s | Tên: %-26s | Hãng: %-10s | Giá: %,12.0f đ | Tồn: %-3d | Pin chờ: %2d ngày | Phím SOS: %-3s%s%n",
                getId(), getName(), getBrand(), getPrice(), getStockQuantity(), batteryLifeDays,
                (sosModeEnable ? "BẬT" : "TẮT"),
                isLowStock() ? " [SẮP HẾT HÀNG]" : "");
    }

    public int getBatteryLifeDays() {
        return batteryLifeDays;
    }

    public void setBatteryLifeDays(int batteryLifeDays) {
        if (batteryLifeDays <= 0) {
            throw new ValidationException("[LỖI]: Thời lượng pin chờ phải lớn hơn 0 ngày!");
        }
        this.batteryLifeDays = batteryLifeDays;
    }

    public boolean isSosModeEnable() {
        return sosModeEnable;
    }

    public void setSosModeEnable(boolean sosModeEnable) {
        this.sosModeEnable = sosModeEnable;
    }
}
