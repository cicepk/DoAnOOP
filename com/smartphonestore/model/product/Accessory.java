package com.smartphonestore.model.product;

import com.smartphonestore.exception.ValidationException;

public class Accessory extends Product implements IDiscountable {
    private String accessoryType;// Loại phụ kiện

    public Accessory(String id, String name, String brand, double price,
         int stockQuantity, int minStockThreshold, String accessoryType) {
        super(id, name, brand, price, stockQuantity, minStockThreshold);
        setAccessoryType(accessoryType);
    }

    // Phương thức kiểm tra phụ kiện có tương thích với điện thoại không
    public boolean isCompatibleWith(String phoneModelName) {
        if (phoneModelName == null || phoneModelName.trim().isEmpty()) {
            System.out.println("[X] Ten dong may khong duoc de trong!");
            return false;
        }
        String phoneLower = phoneModelName.toLowerCase().trim();
        String accessoryNameLower = getName().toLowerCase();
        String type = getAccessoryType();

        // =========================================================================
        // NHÓM 1: CỦ SẠC (SAC)
        // Tất cả củ sạc (Apple, Samsung, Anker, Ugreen...) đều dùng chuẩn sạc nhanh
        // quốc tế USB-PD / Type-C hoặc GaN đa cổng, tương thích sạc tốt cho mọi Smartphone.
        // =========================================================================
        if ("SAC".equalsIgnoreCase(type)) {
            // Cùng hệ sinh thái chính hãng (Apple sạc iPhone, Samsung sạc Samsung)
            if ((phoneLower.contains("iphone") || phoneLower.contains("apple")) && getBrand().equalsIgnoreCase("Apple")) {
                System.out.printf("[OK] TUONG THICH TOI UU: '%s' la cu sac chinh hang Apple, ho tro sac nhanh chuan PD cho '%s'!%n", 
                        getName(), phoneModelName);
                return true;
            }
            if ((phoneLower.contains("samsung") || phoneLower.contains("galaxy")) && getBrand().equalsIgnoreCase("Samsung")) {
                System.out.printf("[OK] TUONG THICH TOI UU: '%s' la cu sac chinh hang Samsung (Super Fast Charging), toi uu cho '%s'!%n", 
                        getName(), phoneModelName);
                return true;
            }
            // Sạc đa năng (Anker, Ugreen...) hoặc sạc chuẩn Type-C/GaN chéo hãng
            System.out.printf("[OK] TUONG THICH: '%s' (Hang %s) dung cong Type-C / GaN chuan quoc te (USB Power Delivery), ho tro sac nhanh an toan cho '%s'!%n",
                    getName(), getBrand(), phoneModelName);
            return true;
        }

        // =========================================================================
        // NHÓM 2: TAI NGHE (TAI_NGHE)
        // Tai nghe Bluetooth (AirPods, Galaxy Buds, Sony, Marshall...) kết nối chuẩn quốc tế
        // với mọi Smartphone có Bluetooth.
        // =========================================================================
        if ("TAI_NGHE".equalsIgnoreCase(type)) {
            if ((phoneLower.contains("iphone") || phoneLower.contains("apple")) && getBrand().equalsIgnoreCase("Apple")) {
                System.out.printf("[OK] TUONG THICH TOI UU: '%s' ket noi he sinh thai Apple, ho tro am thanh khong gian & Pop-up tien loi cho '%s'!%n", 
                        getName(), phoneModelName);
                return true;
            }
            if ((phoneLower.contains("samsung") || phoneLower.contains("galaxy")) && getBrand().equalsIgnoreCase("Samsung")) {
                System.out.printf("[OK] TUONG THICH TOI UU: '%s' ket noi he sinh thai Galaxy toi uu cho '%s'!%n", 
                        getName(), phoneModelName);
                return true;
            }
            System.out.printf("[OK] TUONG THICH: '%s' (Hang %s) ho tro Bluetooth chuan quoc te, ket noi dam thoai & nghe nhac tot voi '%s'!%n",
                    getName(), getBrand(), phoneModelName);
            return true;
        }

        // =========================================================================
        // NHÓM 3: ỐP LƯNG (OP_LUNG) & KÍNH CƯỜNG LỰC (CUONG_LUC)
        // Bắt buộc phải khớp đúng khuôn máy / kích thước màn hình
        // =========================================================================
        if ("OP_LUNG".equalsIgnoreCase(type) || "CUONG_LUC".equalsIgnoreCase(type)) {
            boolean isCase = "OP_LUNG".equalsIgnoreCase(type);

            // Chuẩn hóa tên nhập vào (xử lý "ip 14" -> "iphone 14", "galaxy s24" -> "samsung s24 / s24")
            String normPhone = phoneLower.replaceAll("\\bip\\s*", "iphone ")
                                         .replaceAll("\\bgalaxy\\s*", "")
                                         .trim();

            // 1. Kiểm tra khớp trực tiếp 2 chiều
            boolean matched = accessoryNameLower.contains(phoneLower) || accessoryNameLower.contains(normPhone);

            // 2. Đối chiếu các dòng máy phổ biến
            if (!matched) {
                String[] popularModels = {
                    "iphone 16 pro max", "iphone 16 pro", "iphone 16 plus", "iphone 16",
                    "iphone 15 pro max", "iphone 15 pro", "iphone 15 plus", "iphone 15",
                    "iphone 14 pro max", "iphone 14 pro", "iphone 14 plus", "iphone 14",
                    "iphone 13 pro max", "iphone 13 pro", "iphone 13",
                    "s24 ultra", "s24 plus", "s24", "samsung s24",
                    "s23 fe", "samsung s23", "a55", "samsung a55",
                    "z fold6", "z flip6"
                };
                for (String m : popularModels) {
                    if (accessoryNameLower.contains(m) && (phoneLower.contains(m) || normPhone.contains(m))) {
                        matched = true;
                        break;
                    }
                }
            }

            boolean isMagSafe = accessoryNameLower.contains("magsafe");

            if (matched) {
                if (isCase && isMagSafe) {
                    System.out.printf("[OK] TUONG THICH: '%s' vua khit voi dong may '%s' va ho tro sac khong day tu tinh MagSafe!%n", getName(), phoneModelName);
                } else if (isCase) {
                    System.out.printf("[OK] TUONG THICH: '%s' vua khit voi dong may '%s'!%n", getName(), phoneModelName);
                } else {
                    System.out.printf("[OK] TUONG THICH: '%s' vua khit voi man hinh cua dong may '%s'!%n", getName(), phoneModelName);
                }
                return true;
            } else {
                if (isCase) {
                    System.out.printf("[X] KHONG TUONG THICH: '%s' duoc duc theo khuon may rieng, khong lap vua cho dong may '%s'!%n", getName(), phoneModelName);
                } else {
                    System.out.printf("[X] KHONG TUONG THICH: '%s' khong phu hop voi kich thuoc man hinh cua dong may '%s'!%n", getName(), phoneModelName);
                }
                return false;
            }
        }

        // =========================================================================
        // TRƯỜNG HỢP KHÁC (MẶC ĐỊNH)
        // =========================================================================
        if (getBrand().equalsIgnoreCase("ALL") || phoneLower.contains(getBrand().toLowerCase())) {
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
