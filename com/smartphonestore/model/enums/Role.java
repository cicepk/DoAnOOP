package com.smartphonestore.model.enums;
// Cửa hàng offline: chỉ nhân viên có tài khoản, khách không đăng nhập
public enum Role {
    ADMIN("Quản trị viên"),
    WAREHOUSE("Nhân viên kho"),
    SALES("Nhân viên bán hàng");

    private final String displayName;
    Role(String displayName) {
        this.displayName = displayName;
    }
    public String getDisplayName() {
        return displayName;
    }
    public static Role fromString(String text) {
        if (text == null) return null;
        // values() là 3 phần tử trong Role
        for (Role r : Role.values()) {
            // name : admin, bỏ qua viết hoa, xóa khoảng trắng
            if (r.name().equalsIgnoreCase(text.trim())) return r;
        }
        return null; // dòng cũ "CUSTOMER" trả null -> AccountManager bỏ qua dòng đó
    }
}
