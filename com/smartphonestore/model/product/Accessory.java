package com.smartphonestore.model.product;

import com.smartphonestore.exception.ValidationException;

public class Accessory extends Product implements IDiscountable {
    private String accessoryType;// Loại phụ kiện

    public Accessory(String id, String name, String brand, double price,
         int stockQuantity, int minStockThreshold, String accessoryType) {
        super(id, name, brand, price, stockQuantity, minStockThreshold);
        setAccessoryType(accessoryType);
    }

    //Phương thức kiểm tra phụ kiện có tương thích với điện thoại không
    public boolean isCompatibleWith(String phoneModelName) {
        if (phoneModelName == null || phoneModelName.trim().isEmpty()) {
            System.out.println("[X] Tên dòng máy không được để trống!");
            return false;
        }
        String phoneLower = phoneModelName.toLowerCase();//chuyển về chữ thường
        String accessoryNameLower = getName().toLowerCase();
        // TRƯỜNG HỢP 1: Ốp lưng hoặc Kính cường lực -> Bắt buộc phải khớp đúng dòng máy
        if ("OP_LUNG".equalsIgnoreCase(accessoryType) || "CUONG_LUC".equalsIgnoreCase(accessoryType)) {
            if (accessoryNameLower.contains(phoneLower)) {
                System.out.printf("[OK] TƯƠNG THÍCH: '%s' vừa khít với dòng máy '%s'!%n", getName(), phoneModelName);
                return true;
            } else {
                System.out.printf("[X] KHÔNG TƯƠNG THÍCH: '%s' không lắp vừa cho dòng máy '%s'!%n", getName(), phoneModelName);
                return false;
            }
        }
    
        // TRƯỜNG HỢP 2: CỦ SẠC HOẶC TAI NGHE (SAC, TAI_NGHE)
        if (getBrand().equalsIgnoreCase("ALL")) {
            System.out.printf("[OK] TƯƠNG THÍCH: '%s' là phụ kiện chuẩn đa năng, dùng tốt cho mọi dòng máy!%n", getName());
            return true;
        }
        if (phoneLower.contains(getBrand().toLowerCase())) {
            System.out.printf("[OK] TƯƠNG THÍCH: Cùng hệ sinh thái thương hiệu '%s'!%n", getBrand());
            return true;
        }
        if (accessoryNameLower.contains("type-c")) {
            System.out.printf("[OK] TƯƠNG THÍCH: '%s' dùng cổng Type-C chuẩn quốc tế, cắm vừa cho '%s'!%n", getName(), phoneModelName);
            return true;
        }
        System.out.printf("[X] CẢNH BÁO: '%s' (Hãng %s) có thể không tương thích tốt với máy '%s'!%n", getName(), getBrand(), phoneModelName);
        return false; 
    }

    //Nạp chồng (Overloading): Cho phép truyền thẳng đối tượng Phone vào kiểm tra
    public boolean isCompatibleWith(Phone phone) {
        if (phone == null) {
            System.out.println("[X] Đối tượng điện thoại truyền vào bị null!");
            return false;
        }
        return isCompatibleWith(phone.getName());
    }

    @Override
    public int getWarrantyMonths() {
        return 6; // Phụ kiện bảo hành 6 tháng
    }

    @Override
    public double calculateDiscountedPrice(double discountRate) {
        if (discountRate < 0.0 || discountRate > 0.5) {
            System.out.println("[!] Phụ kiện chỉ được phép giảm giá tối đa 50% (0.0 -> 0.5)!");
            return getPrice();
        }
        double finalPrice = getPrice() * (1 - discountRate);
        if (finalPrice < 1000) {
            finalPrice = 1000;
        }
        return finalPrice;
    }

    @Override 
    public String toFileString() {
        return String.format("%s|%s|%s|%.0f|%d|ACCESSORY|%d|%s",
                getId(), getName(), getBrand(), getPrice(), getStockQuantity(),
                getMinStockThreshold(), accessoryType);
    }

    @Override
    public void displayInfo() {
        System.out.printf("[PHỤ KIỆN]   ID: %-6s | Tên: %-26s | Hãng: %-10s | Giá: %,12.0f đ | Tồn: %-3d | Loại: %-9s%s%n",
                getId(), getName(), getBrand(), getPrice(), getStockQuantity(), getAccessoryType(),
                isLowStock() ? " [SẮP HẾT HÀNG]" : "");
    }

    public String getAccessoryType() {
        return accessoryType;
    }

    public void setAccessoryType(String accessoryType) {
        if (accessoryType == null || !accessoryType.trim().toUpperCase().matches("^(SAC|TAI_NGHE|OP_LUNG|CUONG_LUC)$")) {
            throw new ValidationException("[LỖI]: Loại phụ kiện phải là SAC, TAI_NGHE, OP_LUNG hoặc CUONG_LUC!");
        }
        this.accessoryType = accessoryType.trim().toUpperCase();
    }
}
