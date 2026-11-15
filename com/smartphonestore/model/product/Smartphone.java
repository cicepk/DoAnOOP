package com.smartphonestore.model.product;

import com.smartphonestore.exception.ValidationException;

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
        System.out.printf("[SMARTPHONE] ID: %-6s | Tên: %-26s | Hãng: %-10s | Giá: %,12.0f đ | Tồn: %-3d | HĐH: %-7s | ROM: %4dGB | Hạn BH: %2d tháng%s%n",
                getId(), getName(), getBrand(), getPrice(), getStockQuantity(), getOperatingSystem(), getStorageCapacity(), getWarrantyMonths(),
                isLowStock() ? " [SẮP HẾT HÀNG]" : "");
    }

    public int getStorageCapacity() {
        return storageCapacity;
    }

    public void setStorageCapacity(int storageCapacity) {
        if (storageCapacity <= 0) {
            throw new ValidationException("[LỖI]: Dung lượng ROM phải lớn hơn 0 GB!");
        }
        this.storageCapacity = storageCapacity;
    }
}
