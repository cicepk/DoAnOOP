package com.smartphonestore.model.auth;
import com.smartphonestore.model.enums.Role;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Account {
    private final String id;
    private final String username;
    private String password;
    private Role role;
    private String status;
    private String lastLoginTime;
    // Nhân viên (ADMIN/WAREHOUSE/SALES) -> mã EMPxx trong employees.txt
    // Khách hàng (CUSTOMER)             -> mã CUSxx trong customers.txt
    private final String linkedId;

    public Account(String id, String username, String password, Role role,
                   String status, String lastLoginTime, String linkedId) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.role = role;
        this.status = status;
        // File có thể để trống cột này
        this.lastLoginTime = (lastLoginTime == null || lastLoginTime.trim().isEmpty())
            ? "Chưa đăng nhập" : lastLoginTime;
        this.linkedId = linkedId;
    }
    // Kiểm tra mã liên kết có đúng loại với role không (dùng trước khi tạo tài khoản)
    public static boolean isValidLink(Role role, String linkedId) {
        if (role == null || linkedId == null) return false;
        String prefix = role.isStaff() ? "EMP" : "CUS";
        return linkedId.trim().toUpperCase().startsWith(prefix);
    }
    public boolean isLinkValid() {
        return isValidLink(role, linkedId);
    }
    public String getId() { return id; }
    public String getUsername() { return username; }
    public Role getRole() { return role; }
    public String getStatus() { return status; }
    public String getLastLoginTime() { return lastLoginTime; }
    public String getLinkedId() { return linkedId; }
    // Đổi chức vụ nhân viên thì role tài khoản phải đổi theo
    public boolean setRole(Role role) {
        if (role == null) {
            return false;
        }
        // Không cho đổi qua lại giữa nhân viên và khách, vì linkedId sẽ sai loại
        if (role.isStaff() != this.role.isStaff()) {
            return false;
        }
        this.role = role;
        return true;
    }
    // check đăng nhập
    public boolean checkLogin(String userName, String userPassword) {
        return username.equalsIgnoreCase(userName) //check username kh viết hoa
            && password.equals(userPassword) // check pass có viết hoa
            && status.equals("ACTIVE"); // check active
    }
    // Đổi pass
    public boolean changePassword(String newPassword) {
        if (newPassword == null || newPassword.length() < 6 || newPassword.contains("|")) {
            return false;
        }
        password = newPassword;
        return true;
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
        return id + "|" + username + "|" + password + "|" + role.name() + "|"
            + status + "|" + lastLoginTime + "|" + linkedId;
    }

    public void display() {
        // %-6s: chuỗi, canh trái, rộng 6 ô. %n: xuống dòng
        System.out.printf("%-6s | %-12s | %-10s | %-7s | %-6s | %s%n",
            id, username, role.name(), status, linkedId, lastLoginTime);
    }

}