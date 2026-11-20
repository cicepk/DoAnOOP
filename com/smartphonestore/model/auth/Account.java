package com.smartphonestore.model.auth;

public class Account {
    private final String id;
    private final String username;
    private final String role;

    public Account(String id, String username, String role) {
        if (id == null || id.isBlank()) throw new IllegalArgumentException("Mã nhân viên không hợp lệ");
        this.id = id.trim();
        this.username = username == null ? "" : username.trim();
        this.role = role == null ? "SALES" : role.trim();
    }
    public String getId() { return id; }
    public String getUsername() { return username; }
    public String getRole() { return role; }
}
