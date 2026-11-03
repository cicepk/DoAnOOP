package com.smartphonestore.model.enums;
public enum Role {
    ADMIN("Quản trị viên"),
    WAREHOUSE("Nhân viên kho"),
    SALES("Nhân viên bán hàng"),
    CUSTOMER("Khách hàng / User");

    private final String displayName;
    Role(String displayName) {
        this.displayName = displayName;
    }
    public String getDisplayName() { 
        return displayName; 
    }
    public static Role fromString(String text) {
        if (text == null) return null;
        // value là 4 phần tử trong Role
        for (Role r : Role.values()) {
            // name : admin, bỏ qua viết hoa, xóa khoảng trắng
            if (r.name().equalsIgnoreCase(text.trim())) return r;
        }
        return null;
    }
}