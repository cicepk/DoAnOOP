package com.smartphonestore.model.auth;
import com.smartphonestore.model.enums.Role;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Account {
    private final String id;
    private final String username;
    private String password;
    private final Role role;
    private String status;
    private String lastLoginTime;
    private final String ownerId;
    
    public Account(String id, String username, String password, Role role,
                   String status, String lastLoginTime, String ownerId) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.role = role;
        this.status = status;
        this.lastLoginTime = lastLoginTime;
        this.ownerId = ownerId;
    }
    public String getId() { return id; }
    public String getUsername() { return username; }
    public String getRole() { return role.name(); }
    public String getSatus() { return status; }
    public String getLastLoginTime() { return lastLoginTime; }
    public String getOwnerId() { return ownerId; }
    // check đăng nhập
    public boolean checkLogin(String userName, String userPassword) {
        return username.equalsIgnoreCase(userName) //check username kh viết hoa
            && password.equals(userPassword) // check pass có viết hoa
            && status.equals("ACtive"); // check active
    }
    // Đổi pass
    public void changePass (String newPassword) {
        password = newPassword;
    }

    public void updateLastLogin() {
        // Khuôn ngày/tháng/năm giờ:phút:giây
        DateTimeFormatter format = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        // Lấy giờ hiện tại, đổi thành chữ theo khuôn
        lastLoginTime = LocalDateTime.now().format(format);
    }

    public void lockToggle() {
        // Nếu đang khóa thì mở, mở thì khóa
        if (status.equals("ACTIVE")) {
            status = "LOCKED";
        } else {
            status = "ACTIVE";
        }
    }

    public String toFileString() {
        return id + " | " + username + " | " + password + " | " +  role.name() + " | " 
            + status + " | " + lastLoginTime + " | " + ownerId;
    }

    public void display() {
        // %-6s: chuỗi, canh trái, rộng 6 ô. %n: xuống dòng
        System.out.printf("%-6s | %-12s | %-10s | %-7s | %-6s | %s%n",
            id, username, role.name(), status, ownerId, lastLoginTime);
    }

}