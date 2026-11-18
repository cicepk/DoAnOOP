package com.smartphonestore.manager;

import com.smartphonestore.model.auth.Account;
import com.smartphonestore.model.enums.Role;
import com.smartphonestore.util.FileIO;
import java.util.ArrayList;
import java.util.List;

public class AccountManager {
    private static final String FILE_PATH = "data/accounts.txt";

    private final List<Account> accounts = new ArrayList<>();

    public AccountManager() {
        loadFromFile();
    }

    // ===================== ĐỌC / GHI FILE =====================
    // Format: id|username|password|role|status|lastLoginTime|linkedId

    private void loadFromFile() {
        accounts.clear();
        for (String line : FileIO.readLines(FILE_PATH)) {
            // Chia phần tử format, -1 nếu rỗng phần tử vẫn trả về 7 ptu
            String[] parts = line.split("\\|", -1);
            if (parts.length < 7) {
                System.err.println("Bỏ qua dòng thiếu cột trong accounts.txt: " + line);
                continue;
            }
            Role role = Role.fromString(parts[3]);
            if (role == null) {
                System.err.println("Bỏ qua dòng sai role trong accounts.txt: " + line);
                continue;
            }
            Account acc = new Account(
                parts[0].trim(), parts[1].trim(), parts[2].trim(), role, 
                parts[4].trim().toUpperCase(), parts[5].trim(), parts[6].trim().toUpperCase() 
            );
            if (!acc.isLinkValid()) {
                System.err.println("Bỏ qua dòng có linkedId sai loại trong accounts.txt: " + line);
                continue;
            }
            accounts.add(acc);
        }
    }

    public boolean saveToFile() {
        List<String> lines = new ArrayList<>();
        for (Account acc : accounts) {
            lines.add(acc.toFileString());
        }
        // FileIO.writeLines giữ lại dòng chú thích # ở đầu file
        return FileIO.writeLines(FILE_PATH, lines);
    }

    // ===================== TRA CỨU =====================

    public List<Account> getAll() {
        // Trả bản sao bên ngoài kh thể xóa trực tiếp list
        return new ArrayList<>(accounts);
    }
    // Tìm bằng id
    public Account findById(String id) {
        if (id == null) return null;
        for (Account acc : accounts) {
            if (acc.getId().equalsIgnoreCase(id.trim())) return acc;
        }
        return null;
    }

    // Tìm bằng username
    public Account findByUsername(String username) {
        if (username == null) return null;
        for (Account acc : accounts) {
            if (acc.getUsername().equalsIgnoreCase(username.trim())) return acc;
        }
        return null;
    }

    // Tìm bằng EMP/CUS
    public Account findByLinkedId(String linkedId) {
        if (linkedId == null) return null;
        for (Account acc : accounts) {
            if (acc.getLinkedId().equalsIgnoreCase(linkedId.trim())) return acc;
        }
        return null;
    }
    // ===================== ĐĂNG NHẬP =====================

    // Đúng user + pass + ACTIVE thì cập nhật giờ đăng nhập và trả về tài khoản, sai trả về null
    public Account login(String username, String password) {
        Account acc = findByUsername(username);
        if (acc == null || !acc.checkLogin(username, password)) return null;
        acc.updateLastLogin();
        saveToFile();
        return acc;
    }
    // Dùng để báo riêng "tài khoản bị khóa" thay vì "sai mật khẩu"
    public boolean isLocked(String username) {
        Account acc = findByUsername(username);
        return acc != null && acc.getStatus().equals("LOCKED");
    }
    // ===================== THÊM / SỬA =====================

    // Lấy số lớn nhất rồi + 1 
    public String nextId() {
        int max = 0;
        for (Account acc : accounts) {
            try {
                // Cắt 3 ký tự đầu ACC
                int num = Integer.parseInt(acc.getId().substring(3));
                if (num > max) max = num;
            } catch (NumberFormatException | StringIndexOutOfBoundsException ignored) {
                // Mã kh đúng bỏ qua
            }
        }
        // mặc định ACC0X
        return String.format("ACC%02d", max + 1);
    }
    // Trả về null nếu tạo thành công, ngược lại trả về lý do lỗi để menu in ra
    public String createAccount(String username, String password, Role role, String linkedId) {
        if (username == null || username.trim().isEmpty() || username.contains("|") || 
        username.contains(" ")) {
            return "Tên đăng nhập không được rỗng, không chứa khoảng trắng hoặc ký tự '|'.";
        }
        if (findByUsername(username) != null) return "Username đã tồn tại";
        if (password == null || password.length() < 6 || password.contains("|")) {
            return "Mật khẩu phải từ 6 ký tự trở lên và không chứa ký tự '|'.";
        }
        if (role == null) return "Vai trò không hợp lệ.";
        if (!Account.isValidLink(role, linkedId)) {
            return role.isStaff()
                ? "Tài khoản nhân viên phải liên kết với mã EMPxx."
                : "Tài khoản khách hàng phải liên kết với mã CUSxx.";
        }
        if (findByLinkedId(linkedId) != null) {
            return "Mã " + linkedId.toUpperCase() + " đã có tài khoản rồi.";
        }
        Account acc = new Account(nextId(), username.trim(), password, role, 
                            "ACTIVE", "", linkedId.trim().toUpperCase());
        accounts.add(acc);
        saveToFile();
        return null;
    }

    public boolean changePassword(String id, String newPass) {
        Account acc = findById(id);
        if (acc == null || !acc.changePassword(newPass)) return false;
        return saveToFile();
    }
    // Đếm số lượng acc Admin
    private int countActiveAdmins() {
        int count = 0;
        for (Account acc : accounts) {
            if (acc.getRole() == Role.ADMIN && acc.getStatus().equals("ACTIVE")) count++;
        }
        return count;
    }
     // Khóa <-> mở khóa. Không cho khóa ADMIN cuối cùng đang hoạt động
    public boolean toggleLock(String id) {
        Account acc = findById(id);
        if (acc == null) return false;
        // Luôn giữ 1 acc Admin Active
        if (acc.getRole() == Role.ADMIN && acc.getStatus().equals("ACTIVE") &&
                countActiveAdmins() <= 1 ) {
                    return false;
                }
        acc.lockToggle();
        return saveToFile();
    }
    // Nhân viên đổi chức vụ thì tài khoản đổi role theo. Trả về false nếu nhân viên chưa có tài khoản
    public boolean updateRoleByLinkedId(String employeeId, Role newRole) {
        Account acc = findByLinkedId(employeeId);
        if (acc == null || !acc.setRole(newRole)) return false;
        return saveToFile();
    }
    // Xóa nhân viên thì khóa tài khoản của người đó (giữ lại để còn lịch sử)
    public boolean lockByLinkedId(String linkedId) {
        Account acc = findByLinkedId(linkedId);
        if (acc == null) return false;
        if (acc.getStatus().equals("ACTIVE")) acc.lockToggle();
        return saveToFile();
    }

    // ===================== DÀNH CHO TV4 (KHÁCH HÀNG) =====================

    // Nhân viên cấp tài khoản cho khách. False nếu trùng username, khách đã có tài khoản,
    // mật khẩu không hợp lệ hoặc mã không phải CUSxx
    public boolean createCustomerAccount(String customerId, String username, String password) {
        return createAccount(username, password, Role.CUSTOMER, customerId) == null;
    }

    // Xóa khách thì XÓA luôn tài khoản (không khóa như nhân viên),
    // vì mã khách có thể được cấp lại, tài khoản cũ sẽ đăng nhập nhầm vào khách mới.
    // Chỉ xóa tài khoản CUSTOMER để không lỡ tay xóa tài khoản nhân viên
    public boolean removeAccountsByOwnerId(String ownerId) {
        if (ownerId == null) return false;
        String id = ownerId.trim();
        boolean removed = accounts.removeIf(acc ->
            acc.getRole() == Role.CUSTOMER && acc.getLinkedId().equalsIgnoreCase(id));
        if (!removed) return false;
        return saveToFile();
    }
}