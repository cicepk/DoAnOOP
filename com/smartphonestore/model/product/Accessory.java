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
            System.out.println("[X] Ten dong may khong duoc de trong!");
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
        if ("SAC".equals(type)) {
            if (isIPhoneInput) {
                if (hasIPhoneTrait) {
                    System.out.printf("[OK] TUONG THICH TOI UU: '%s' la cu sac chinh hang Apple, ho tro sac nhanh chuan PD toi uu cho tat ca cac dong iPhone (bao gom '%s')!%n",
                            getName(), phoneModelName);
                } else {
                    System.out.printf("[OK] TUONG THICH: '%s' (Hang %s) co dac tinh Type-C / GaN chuan quoc te (USB-PD), sac nhanh an toan cho tat ca cac dong iPhone (bao gom '%s')!%n",
                            getName(), getBrand(), phoneModelName);
                }
                return true;
            } else if (isSamsungInput) {
                if (hasSamsungTrait) {
                    System.out.printf("[OK] TUONG THICH TOI UU: '%s' la cu sac chinh hang Samsung (Super Fast Charging), toi uu cho tat ca cac dong Samsung Galaxy (bao gom '%s')!%n",
                            getName(), phoneModelName);
                } else {
                    System.out.printf("[OK] TUONG THICH: '%s' dung cong nghe Type-C / GaN chuan quoc te, sac nhanh an toan cho cac dong may Samsung (bao gom '%s')!%n",
                            getName(), phoneModelName);
                }
                return true;
            } else {
                System.out.printf("[OK] TUONG THICH: '%s' dung cong nghe Type-C chuan quoc te (USB-PD), tuong thich va sac tot cho '%s'!%n",
                        getName(), phoneModelName);
                return true;
            }
        }

        // =========================================================================
        // TRƯỜNG HỢP 2: TAI NGHE (TAI_NGHE)
        // Tai nghe Bluetooth (AirPods, Galaxy Buds, Sony, Marshall...) kết nối chuẩn quốc tế.
        // =========================================================================
        if ("TAI_NGHE".equals(type)) {
            if (isIPhoneInput) {
                if (hasIPhoneTrait) {
                    System.out.printf("[OK] TUONG THICH TOI UU: '%s' mang dac tinh dong bo he sinh thai Apple, ho tro am thanh khong gian & Pop-up cho tat ca cac dong iPhone (bao gom '%s')!%n",
                            getName(), phoneModelName);
                } else {
                    System.out.printf("[OK] TUONG THICH: '%s' ho tro Bluetooth chuan quoc te, ket noi nghe nhac & dam thoai tot voi '%s'!%n",
                            getName(), phoneModelName);
                }
                return true;
            } else if (isSamsungInput) {
                if (hasSamsungTrait) {
                    System.out.printf("[OK] TUONG THICH TOI UU: '%s' mang dac tinh dong bo Galaxy Wearable, toi uu cho tat ca cac dong Samsung Galaxy (bao gom '%s')!%n",
                            getName(), phoneModelName);
                } else {
                    System.out.printf("[OK] TUONG THICH: '%s' ho tro Bluetooth chuan quoc te, ket noi tot voi '%s'!%n",
                            getName(), phoneModelName);
                }
                return true;
            } else {
                System.out.printf("[OK] TUONG THICH: '%s' ho tro Bluetooth chuan quoc te, ket noi tot voi '%s'!%n",
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
                if (isIPhoneInput) {
                    if (accName.contains("magsafe")) {
                        System.out.printf("[OK] TUONG THICH: '%s' so huu dac tinh sac tu tinh MagSafe & thiet ke chuan dong iPhone, hoan toan phu hop voi dong may '%s' (va cac dong iPhone 12, 13, 14, 15, 16)!%n",
                                getName(), phoneModelName);
                    } else {
                        System.out.printf("[OK] TUONG THICH: '%s' mang dac tinh thiet ke chuan cho he sinh thai iPhone, hoan toan phu hop voi dong may '%s' (va cac dong iPhone 12, 13, 14, 15)!%n",
                                getName(), phoneModelName);
                    }
                    return true;
                } else {
                    System.out.printf("[X] KHONG TUONG THICH: '%s' mang dac tinh thiet ke rieng cho dong iPhone (Apple), khong phu hop voi dong may '%s' cua hang khac!%n",
                            getName(), phoneModelName);
                    return false;
                }
            }

            // B. Phụ kiện có đặc tính dòng Samsung (Samsung, Galaxy...)
            if (hasSamsungTrait) {
                if (isSamsungInput) {
                    System.out.printf("[OK] TUONG THICH: '%s' mang dac tinh thiet ke rieng cho he sinh thai Samsung Galaxy, hoan toan phu hop voi dong may '%s' (va toan bo dong Samsung Galaxy)!%n",
                            getName(), phoneModelName);
                    return true;
                } else {
                    System.out.printf("[X] KHONG TUONG THICH: '%s' mang dac tinh thiet ke rieng cho dong Samsung Galaxy, khong phu hop voi dong may '%s'!%n",
                            getName(), phoneModelName);
                    return false;
                }
            }

            // C. Phụ kiện chuẩn phổ thông (Kính cường lực KingKong, MiPow...)
            // Nếu người dùng nhập máy cảm ứng (iPhone, Samsung, Xiaomi...) đều tương thích
            if (accName.contains(phone) || phone.contains(accName) || isIPhoneInput || isSamsungInput) {
                System.out.printf("[OK] TUONG THICH: '%s' co dac tinh phu hop voi kich thuoc man hinh cua dong may '%s'!%n",
                        getName(), phoneModelName);
                return true;
            }

            System.out.printf("[X] KHONG TUONG THICH: '%s' khong phu hop voi dong may '%s'!%n", getName(), phoneModelName);
            return false;
        }

        // =========================================================================
        // TRƯỜNG HỢP MẶC ĐỊNH
        // =========================================================================
        if (getBrand().equalsIgnoreCase("ALL") || phone.contains(brand)) {
            System.out.printf("[OK] TUONG THICH: '%s' tuong thich tot voi '%s'!%n", getName(), phoneModelName);
            return true;
        }

        System.out.printf("[X] CANH BAO: '%s' (Hang %s) co the khong tuong thich tot voi may '%s'!%n", getName(), getBrand(), phoneModelName);
        return false;
    }

    //Nạp chồng (Overloading): Cho phép truyền thẳng đối tượng Phone vào kiểm tra
    public boolean isCompatibleWith(Phone phone) {
        if (phone == null) {
            System.out.println("[X] Doi tuong dien thoai truyen vao bi null!");
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
            System.out.println("[!] Phu kien chi duoc phep giam gia toi da 50% (0.0 -> 0.5)!");
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
        System.out.printf("[PHU KIEN]   ID: %-6s | Ten: %-26s | Hang: %-10s | Gia: %,12.0f d | Ton: %-3d | Loai: %-9s%s%n",
                getId(), getName(), getBrand(), getPrice(), getStockQuantity(), getAccessoryType(),
                isLowStock() ? " [SAP HET HANG]" : "");
    }

    public String getAccessoryType() {
        return accessoryType;
    }

    public void setAccessoryType(String accessoryType) {
        if (accessoryType == null || !accessoryType.trim().toUpperCase().matches("^(SAC|TAI_NGHE|OP_LUNG|CUONG_LUC)$")) {
            throw new ValidationException("[LOI]: Loai phu kien phai la SAC, TAI_NGHE, OP_LUNG hoac CUONG_LUC!");
        }
        this.accessoryType = accessoryType.trim().toUpperCase();
    }
}
