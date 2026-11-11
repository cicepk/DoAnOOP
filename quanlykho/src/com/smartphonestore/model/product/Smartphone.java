package src.com.smartphonestore.model.product;

import src.com.smartphonestore.exception.ValidationException;

public class Smartphone extends Phone implements IDiscountable {
    // thuộc tính riêng ROM
    private int storageCapacity;

    public Smartphone(String id, String name, String brand, double price, int stockQuantity, int minStockThreshold,
            String operatingSystem, int storageCapacity) {
        super(id, name, brand, price, stockQuantity, minStockThreshold, operatingSystem);
        setStorageCapacity(storageCapacity);
    }

    // Phương thức riêng: thu cũ đổi mới giảm 10% giá máy
    public double applyTradeInDiscount() {
        return getPrice() * 0.90;
    }

    // ghi đè hàm giảm giá từ interface
    @Override 
    public double calculateDiscountedPrice(double discountRate) {
        return getPrice() * (1 - discountRate); // phần trăm giá tiền mà customer phải trả
    }

    // ghi đè hàm lưu file
    @Override 
    public String toFileString() {
        return String.format("%s|%s|%s|%.0f|%d|SMARTPHONE|%d|%d",
                getId(), getName(), getBrand(), getPrice(), getStockQuantity(),
                getMinStockThreshold(), storageCapacity);
    }

    // Ghi đè hàm in thông tin ra console
    @Override
    public void displayInfo() {
        System.out.printf("[SMARTPHONE] ID: %-6s | Ten: %-26s | Hang: %-10s | Gia: %,12.0f d | Ton: %-3d | HDH: %-7s | ROM: %4dGB | Han BH: %2d thang%s%n",
                getId(), getName(), getBrand(), getPrice(), getStockQuantity(), getOperatingSystem(), getStorageCapacity(), getWarrantyMonths(),
                isLowStock() ? " [SAP HET HANG]" : "");
    }

    public int getStorageCapacity() {
        return storageCapacity;
    }

    public void setStorageCapacity(int storageCapacity) {
        if (storageCapacity <= 0) {
            throw new ValidationException("[LOI]: Dung luong ROM phai lon hon 0 GB!");
        }
        this.storageCapacity = storageCapacity;
    }
}
