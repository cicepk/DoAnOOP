package src.com.smartphonestore.model.product;

import src.com.smartphonestore.exception.ValidationException;

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
        System.out.println("SOS che do khan cap cua may " + getName() +
                " hien dang " + (sosModeEnable ? "BAT" : "TAT"));
    }

    @Override 
    public String toFileString() {
        return String.format("%s|%s|%s|%.0f|%d|FEATUREPHONE|%d|%d",
                getId(), getName(), getBrand(), getPrice(), getStockQuantity(), getMinStockThreshold(), batteryLifeDays);
    }

    @Override
    public void displayInfo() {
        System.out.printf("[PHO THONG]  ID: %-6s | Ten: %-26s | Hang: %-10s | Gia: %,12.0f d | Ton: %-3d | Pin cho: %2d ngay | Phim SOS: %-3s%s%n",
                getId(), getName(), getBrand(), getPrice(), getStockQuantity(), batteryLifeDays,
                (sosModeEnable ? "BAT" : "TAT"),
                isLowStock() ? " [SAP HET HANG]" : "");
    }

    public int getBatteryLifeDays() {
        return batteryLifeDays;
    }

    public void setBatteryLifeDays(int batteryLifeDays) {
        if (batteryLifeDays <= 0) {
            throw new ValidationException("[LOI]: Thoi luong pin cho phai lon hon 0 ngay!");
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
