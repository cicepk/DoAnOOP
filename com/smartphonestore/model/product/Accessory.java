package com.smartphonestore.model.product;

import com.smartphonestore.exception.ValidationException;

public class Accessory extends Product implements IDiscountable {
    private String accessoryType;// Loại phụ kiện

    public Accessory(String id, String name, String brand, double price,
         int stockQuantity, int minStockThreshold, String accessoryType) {
        super(id, name, brand, price, stockQuantity, minStockThreshold);
        setAccessoryType(accessoryType);
    }

    // Phương thức kiểm tra phụ kiện có tương thích với điện thoại không theo đặc tính sản phẩm
    public boolean isCompatibleWith(String phoneModelName) {
        if (phoneModelName == null || phoneModelName.trim().isEmpty()) {
            System.out.println("[X] Tên dòng máy không được để trống!");
            return false;
        }

        String phone = phoneModelName.toLowerCase().trim();
        String accName = getName().toLowerCase();
        String brand = getBrand().toLowerCase();
        String type = getAccessoryType() != null ? getAccessoryType().toUpperCase() : "";

        // 1. Nhận diện dòng máy / hệ sinh thái mà người dùng nhập vào
        boolean isIPhoneInput = phone.contains("iphone") || phone.contains("apple") || phone.matches(".*\\bip\\s*\\d+.*");
        boolean isSamsungInput = phone.contains("samsung") || phone.contains("galaxy") || phone.matches(".*\\b(s24|s23|s22|s21|a55|a54|fold|flip)\\b.*");

        // 2. Nhận diện đặc tính của phụ kiện
        // Đặc tính dòng iPhone: có từ khóa magsafe, iphone, airpods, hoặc hãng Apple
        boolean hasIPhoneTrait = accName.contains("magsafe") || accName.contains("iphone") 
                                || accName.contains("airpods") || brand.equals("apple");

        // Đặc tính dòng Samsung: có từ khóa samsung, galaxy, buds, hoặc hãng Samsung
        boolean hasSamsungTrait = accName.contains("samsung") || accName.contains("galaxy") 
                                 || accName.contains("buds") || brand.equals("samsung");

        // =========================================================================
        // TRƯỜNG HỢP 1: CỦ SẠC (SAC)
        // Các củ sạc hiện nay (Apple, Samsung, Anker, Ugreen...) đều dùng chuẩn sạc nhanh
        // quốc tế Type-C / GaN (USB Power Delivery), tương thích sạc tốt cho mọi dòng điện thoại.
        // =========================================================================
        if ("SAC".equals(type)) { //nếu là Sạc thì...
            if (isIPhoneInput) {
                if (hasIPhoneTrait) {
                    System.out.printf("[OK] TƯƠNG THÍCH TỐI ƯU: '%s' là củ sạc chính hãng Apple, hỗ trợ sạc nhanh chuẩn PD tối ưu cho tất cả các dòng iPhone (bao gồm '%s')!%n",
                            getName(), phoneModelName);
                } else {
                    System.out.printf("[OK] TƯƠNG THÍCH: '%s' (Hãng %s) có đặc tính Type-C / GaN chuẩn quốc tế (USB-PD), sạc nhanh an toàn cho tất cả các dòng iPhone (bao gồm '%s')!%n",
                            getName(), getBrand(), phoneModelName);
                }
                return true;
            } else if (isSamsungInput) {
                if (hasSamsungTrait) {
                    System.out.printf("[OK] TƯƠNG THÍCH TỐI ƯU: '%s' là củ sạc chính hãng Samsung (Super Fast Charging), tối ưu cho tất cả các dòng Samsung Galaxy (bao gồm '%s')!%n",
                            getName(), phoneModelName);
                } else {
                    System.out.printf("[OK] TƯƠNG THÍCH: '%s' dùng công nghệ Type-C / GaN chuẩn quốc tế, sạc nhanh an toàn cho các dòng máy Samsung (bao gồm '%s')!%n",
                            getName(), phoneModelName);
                }
                return true;
            } else {
                System.out.printf("[OK] TƯƠNG THÍCH: '%s' dùng công nghệ Type-C chuẩn quốc tế (USB-PD), tương thích và sạc tốt cho '%s'!%n",
                        getName(), phoneModelName);
                return true;
            }
        }

        // =========================================================================
        // TRƯỜNG HỢP 2: TAI NGHE (TAI_NGHE)
        // Tai nghe Bluetooth (AirPods, Galaxy Buds, Sony, Marshall...) kết nối chuẩn quốc tế.
        // =========================================================================
        if ("TAI_NGHE".equals(type)) { // nếu là tai nghe thì...
            if (isIPhoneInput) {
                if (hasIPhoneTrait) {
                    System.out.printf("[OK] TƯƠNG THÍCH TỐI ƯU: '%s' mang đặc tính đồng bộ hệ sinh thái Apple, hỗ trợ âm thanh không gian & Pop-up cho tất cả các dòng iPhone (bao gồm '%s')!%n",
                            getName(), phoneModelName);
                } else {
                    System.out.printf("[OK] TƯƠNG THÍCH: '%s' hỗ trợ Bluetooth chuẩn quốc tế, kết nối nghe nhạc & đàm thoại tốt với '%s'!%n",
                            getName(), phoneModelName);
                }
                return true;
            } else if (isSamsungInput) {
                if (hasSamsungTrait) {
                    System.out.printf("[OK] TƯƠNG THÍCH TỐI ƯU: '%s' mang đặc tính đồng bộ Galaxy Wearable, tối ưu cho tất cả các dòng Samsung Galaxy (bao gồm '%s')!%n",
                            getName(), phoneModelName);
                } else {
                    System.out.printf("[OK] TƯƠNG THÍCH: '%s' hỗ trợ Bluetooth chuẩn quốc tế, kết nối tốt với '%s'!%n",
                            getName(), phoneModelName);
                }
                return true;
            } else {
                System.out.printf("[OK] TƯƠNG THÍCH: '%s' hỗ trợ Bluetooth chuẩn quốc tế, kết nối tốt với '%s'!%n",
                        getName(), phoneModelName);
                return true;
            }
        }

        // =========================================================================
        // TRƯỜNG HỢP 3: ỐP LƯNG (OP_LUNG) & KÍNH CƯỜNG LỰC (CUONG_LUC)
        // Kiểm tra tính tương thích theo ĐẶC TÍNH DÒNG MÁY (iPhone, Samsung...)
        // =========================================================================
        if ("OP_LUNG".equals(type) || "CUONG_LUC".equals(type)) {
            // A. Phụ kiện có đặc tính dòng iPhone (MagSafe, iPhone, Apple...)
            if (hasIPhoneTrait) {
                if (isIPhoneInput) { //nếu điên thoại nhập vào là iphone
                    if (accName.contains("magsafe")) {
                        System.out.printf("[OK] TƯƠNG THÍCH: '%s' sở hữu đặc tính sạc từ tính MagSafe & thiết kế chuẩn dòng iPhone, hoàn toàn phù hợp với dòng máy '%s' (và các dòng iPhone 12, 13, 14, 15, 16)!%n",
                                getName(), phoneModelName);
                    } else { // ko có magsafe nhưng có đặc tính  của iphone bình thường
                        System.out.printf("[OK] TƯƠNG THÍCH: '%s' mang đặc tính thiết kế chuẩn cho hệ sinh thái iPhone, hoàn toàn phù hợp với dòng máy '%s' (và các dòng iPhone 12, 13, 14, 15)!%n",
                                getName(), phoneModelName);
                    }
                    return true;
                } else {
                    System.out.printf("[X] KHÔNG TƯƠNG THÍCH: '%s' mang đặc tính thiết kế riêng cho dòng iPhone (Apple), không phù hợp với dòng máy '%s' của hãng khác!%n",
                            getName(), phoneModelName);
                    return false;
                }
            }

            // B. Phụ kiện có đặc tính dòng Samsung (Samsung, Galaxy...)
            if (hasSamsungTrait) {
                if (isSamsungInput) {
                    System.out.printf("[OK] TƯƠNG THÍCH: '%s' mang đặc tính thiết kế riêng cho hệ sinh thái Samsung Galaxy, hoàn toàn phù hợp với dòng máy '%s' (và toàn bộ dòng Samsung Galaxy)!%n",
                            getName(), phoneModelName);
                    return true;
                } else {
                    System.out.printf("[X] KHÔNG TƯƠNG THÍCH: '%s' mang đặc tính thiết kế riêng cho dòng Samsung Galaxy, không phù hợp với dòng máy '%s'!%n",
                            getName(), phoneModelName);
                    return false;
                }
            }

            // C. Phụ kiện chuẩn phổ thông (Kính cường lực KingKong, MiPow...)
            // Nếu người dùng nhập máy cảm ứng (iPhone, Samsung, Xiaomi...) đều tương thích
            if (accName.contains(phone) || phone.contains(accName) || isIPhoneInput || isSamsungInput) {
                System.out.printf("[OK] TƯƠNG THÍCH: '%s' có đặc tính phù hợp với kích thước màn hình của dòng máy '%s'!%n",
                        getName(), phoneModelName);
                return true;
            }

            System.out.printf("[X] KHÔNG TƯƠNG THÍCH: '%s' không phù hợp với dòng máy '%s'!%n", getName(), phoneModelName);
            return false;
        }

        // =========================================================================
        // TRƯỜNG HỢP MẶC ĐỊNH
        // =========================================================================
        if (getBrand().equalsIgnoreCase("ALL") || phone.contains(brand)) {
            System.out.printf("[OK] TƯƠNG THÍCH: '%s' tương thích tốt với '%s'!%n", getName(), phoneModelName);
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
