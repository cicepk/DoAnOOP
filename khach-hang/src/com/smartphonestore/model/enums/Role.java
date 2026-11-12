package com.smartphonestore.model.enums;

// GIẢ LẬP: bản thật do TV1 giữ. Cần có giá trị CUSTOMER.
public enum Role {
    ADMIN("Quản trị viên"),
    WAREHOUSE("Nhân viên kho"),
    SALES("Nhân viên bán hàng"),
    CUSTOMER("Khách hàng / User");

    private final String displayName;

    Role(String displayName) { this.displayName = displayName; }

    public String getDisplayName() { return displayName; }
}
