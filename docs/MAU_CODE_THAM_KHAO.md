# MẪU CODE THAM KHẢO & TRIỂN KHAI HỆ THỐNG
# HỆ THỐNG BÁN HÀNG ĐIỆN THOẠI OFFLINE (JAVA CONSOLE)

Tài liệu này cung cấp mã nguồn khung (Skeleton / Boilerplate Code) chuẩn mực, sẵn sàng biên dịch và chạy thử. Mã nguồn triển khai trực tiếp luồng: **Màn hình Đăng nhập ➔ Điều hướng theo 3 Vai trò (Admin, Kho, Bán hàng) ➔ Bảng Menu riêng biệt từng vai trò**.

---

## 1. Định Nghĩa Vai Trò & Tài Khoản Đăng Nhập

### 1.1. Enum `Role.java`
```java
package com.smartphonestore.model.enums;

public enum Role {
    ADMIN("Quản trị viên"),
    WAREHOUSE("Nhân viên kho"),
    SALES("Nhân viên bán hàng"),
    CUSTOMER("Khách hàng / User");

    private final String description;

    Role(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public static Role fromString(String roleStr) {
        for (Role r : Role.values()) {
            if (r.name().equalsIgnoreCase(roleStr.trim())) {
                return r;
            }
        }
        return CUSTOMER; // Mặc định là Khách hàng nếu không tìm thấy
    }
}
```

### 1.2. Model `Account.java` (Thành viên 1)
```java
package com.smartphonestore.model.auth;

import com.smartphonestore.model.enums.Role;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Account {
    // Thuộc tính chung
    private String id;
    private String username;
    private String password;
    private Role role;
    private String status; // "ACTIVE" hoặc "LOCKED"

    // THUỘC TÍNH RIÊNG BIỆT (Đặc trưng cho Account)
    private String lastLoginTime;

    public Account(String id, String username, String password, Role role, String status, String lastLoginTime) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.role = role;
        this.status = status;
        this.lastLoginTime = (lastLoginTime == null || lastLoginTime.isEmpty()) ? "Chưa đăng nhập" : lastLoginTime;
    }

    // PHƯƠNG THỨC RIÊNG BIỆT 1: Xác thực mật khẩu
    public boolean checkLogin(String inputUser, String inputPass) {
        return this.username.equalsIgnoreCase(inputUser.trim()) 
            && this.password.equals(inputPass) 
            && "ACTIVE".equalsIgnoreCase(this.status);
    }

    // PHƯƠNG THỨC RIÊNG BIỆT 2: Đổi mật khẩu và cập nhật thời gian
    public void changePassword(String newPassword) {
        this.password = newPassword;
        this.lastLoginTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));
    }

    public void updateLastLogin() {
        this.lastLoginTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));
    }

    // Chuyển đối tượng sang dòng text lưu file
    public String toFileString() {
        return String.join("|", id, username, password, role.name(), status, lastLoginTime);
    }

    // Getter & Setter
    public String getId() { return id; }
    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public Role getRole() { return role; }
    public String getStatus() { return status; }
    public String getLastLoginTime() { return lastLoginTime; }
    public void setStatus(String status) { this.status = status; }
}
```

---

## 2. Quản Lý Tài Khoản: `AccountManager.java`

```java
package com.smartphonestore.manager;

import com.smartphonestore.model.auth.Account;
import com.smartphonestore.model.enums.Role;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;

public class AccountManager {
    private final List<Account> accounts = new ArrayList<>();
    private final String filePath = "data/accounts.txt";

    public AccountManager() {
        loadFromFile();
        // Nếu file chưa có tài khoản nào, khởi tạo tài khoản mặc định
        if (accounts.isEmpty()) {
            accounts.add(new Account("ACC01", "admin", "admin123", Role.ADMIN, "ACTIVE", "Chưa đăng nhập"));
            accounts.add(new Account("ACC02", "kho01", "kho123", Role.WAREHOUSE, "ACTIVE", "Chưa đăng nhập"));
            accounts.add(new Account("ACC03", "banhang01", "sales123", Role.SALES, "ACTIVE", "Chưa đăng nhập"));
            accounts.add(new Account("ACC05", "khach01", "khach123", Role.CUSTOMER, "ACTIVE", "Chưa đăng nhập"));
            saveToFile();
        }
    }

    public Account authenticate(String username, String password) {
        for (Account acc : accounts) {
            if (acc.checkLogin(username, password)) {
                acc.updateLastLogin();
                saveToFile();
                return acc;
            }
        }
        return null;
    }

    public boolean registerCustomer(String username, String password) {
        if (findByUsername(username) != null) return false;
        String newId = "ACC" + String.format("%02d", accounts.size() + 1);
        Account newAcc = new Account(newId, username, password, Role.CUSTOMER, "ACTIVE", "Vừa tạo tài khoản");
        return add(newAcc);
    }

    public boolean add(Account acc) {
        for (Account existing : accounts) {
            if (existing.getUsername().equalsIgnoreCase(acc.getUsername())) {
                return false; // Trùng username
            }
        }
        accounts.add(acc);
        return saveToFile();
    }

    public boolean deleteById(String id) {
        boolean removed = accounts.removeIf(a -> a.getId().equalsIgnoreCase(id));
        if (removed) saveToFile();
        return removed;
    }

    public Account findByUsername(String username) {
        for (Account acc : accounts) {
            if (acc.getUsername().equalsIgnoreCase(username)) return acc;
        }
        return null;
    }

    public List<Account> getAll() {
        return new ArrayList<>(accounts);
    }

    public void loadFromFile() {
        accounts.clear();
        Path path = Paths.get(filePath);
        if (!Files.exists(path)) return;

        try (BufferedReader br = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) continue;
                String[] parts = line.split("\\|", -1);
                if (parts.length >= 6) {
                    accounts.add(new Account(
                        parts[0], parts[1], parts[2],
                        Role.fromString(parts[3]),
                        parts[4], parts[5]
                    ));
                }
            }
        } catch (IOException e) {
            System.err.println("Lỗi nạp file accounts: " + e.getMessage());
        }
    }

    public boolean saveToFile() {
        try {
            Path path = Paths.get(filePath);
            if (path.getParent() != null && !Files.exists(path.getParent())) {
                Files.createDirectories(path.getParent());
            }
            try (BufferedWriter bw = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
                for (Account acc : accounts) {
                    bw.write(acc.toFileString());
                    bw.newLine();
                }
                return true;
            }
        } catch (IOException e) {
            System.err.println("Lỗi lưu file accounts: " + e.getMessage());
            return false;
        }
    }
}
```

---

## 3. Điểm Vào Hệ Thống & Bộ Điều Hướng Menu: `Main.java`

Mã nguồn dưới đây thực hiện hoàn chỉnh luồng:
1. Nạp dữ liệu khi khởi động.
2. Hiển thị Màn hình Đăng nhập (Login Loop).
3. Kiểm tra Role và chuyển tới `showAdminMenu()`, `showWarehouseMenu()`, hoặc `showSalesMenu()`.
4. Hỗ trợ Đăng xuất và Thoát ứng dụng lưu dữ liệu.

```java
package com.smartphonestore;

import com.smartphonestore.manager.AccountManager;
import com.smartphonestore.model.auth.Account;
import com.smartphonestore.model.enums.Role;
import java.util.Scanner;

public class Main {
    private static final Scanner scanner = new Scanner(System.in);
    private static final AccountManager accountManager = new AccountManager();
    private static Account currentAccount = null;

    public static void main(String[] args) {
        System.out.println("==============================================================");
        System.out.println("   CHÀO MỪNG ĐẾN VỚI HỆ THỐNG QUẢN LÝ CỬA HÀNG ĐIỆN THOẠI   ");
        System.out.println("==============================================================");

        // Vòng lặp chính của ứng dụng
        while (true) {
            // Bước 1: Màn hình Đăng nhập
            currentAccount = handleLogin();
            if (currentAccount == null) {
                System.out.println("\n[!] Tạm biệt! Đã đóng ứng dụng.");
                break;
            }

            // Bước 2: Điều hướng theo Role
            boolean keepRunningRoleMenu = true;
            while (keepRunningRoleMenu) {
                Role role = currentAccount.getRole();
                switch (role) {
                    case ADMIN:
                        keepRunningRoleMenu = showAdminMenu();
                        break;
                    case WAREHOUSE:
                        keepRunningRoleMenu = showWarehouseMenu();
                        break;
                    case SALES:
                        keepRunningRoleMenu = showSalesMenu();
                        break;
                    case CUSTOMER:
                        keepRunningRoleMenu = showUserMenu();
                        break;
                    default:
                        System.out.println("[!] Vai trò không hợp lệ!");
                        keepRunningRoleMenu = false;
                        break;
                }
            }
        }
    }

    // ==============================================================
    // 1. MÀN HÌNH ĐĂNG NHẬP & ĐĂNG KÝ (LOGIN & REGISTER)
    // ==============================================================
    private static Account handleLogin() {
        while (true) {
            System.out.println("\n--------------------------------------------------------------");
            System.out.println("                 MÀN HÌNH ĐĂNG NHẬP HỆ THỐNG [TV1]            ");
            System.out.println("--------------------------------------------------------------");
            System.out.println(" [1] Đăng nhập tài khoản có sẵn");
            System.out.println(" [2] Đăng ký tài khoản Khách hàng mới (User)");
            System.out.println(" [0] Thoát hệ thống");
            System.out.println("(Tài khoản mẫu: admin/admin123 | kho01/kho123 | banhang01/sales123 | khach01/khach123)");
            System.out.print("➤ Chọn thao tác (0-2): ");
            
            int authChoice = readIntChoice(0, 2);
            if (authChoice == 0) return null;

            if (authChoice == 2) {
                System.out.println("\n--- ĐĂNG KÝ TÀI KHOẢN KHÁCH HÀNG MỚI [TV1] ---");
                System.out.print("➤ Nhập Tên đăng nhập mới: ");
                String newUsername = scanner.nextLine().trim();
                System.out.print("➤ Nhập Mật khẩu: ");
                String newPassword = scanner.nextLine().trim();
                
                boolean ok = accountManager.registerCustomer(newUsername, newPassword);
                if (ok) {
                    System.out.println("[✓] Đăng ký thành công! Hãy đăng nhập để mua sắm.");
                } else {
                    System.out.println("[X] Tên đăng nhập đã tồn tại! Vui lòng chọn tên khác.");
                }
                continue;
            }

            // Đăng nhập
            System.out.print("➤ Nhập Tên đăng nhập (Username): ");
            String username = scanner.nextLine().trim();
            System.out.print("➤ Nhập Mật khẩu (Password): ");
            String password = scanner.nextLine().trim();

            Account acc = accountManager.authenticate(username, password);
            if (acc != null) {
                System.out.println("\n[✓] ĐĂNG NHẬP THÀNH CÔNG!");
                System.out.printf("Xin chào: %s | Vai trò: %s (%s)%n", 
                    acc.getUsername(), acc.getRole().name(), acc.getRole().getDescription());
                System.out.printf("Lần đăng nhập trước: %s%n", acc.getLastLoginTime());
                return acc;
            } else {
                System.out.println("\n[X] ĐĂNG NHẬP THẤT BẠI! Sai tài khoản, mật khẩu hoặc tài khoản đã bị khóa.");
                System.out.print("Bạn có muốn thử lại không? (Y/N): ");
                String retry = scanner.nextLine().trim();
                if (!retry.equalsIgnoreCase("Y")) {
                    return null;
                }
            }
        }
    }

    // ==============================================================
    // 2. MENU 1: QUẢN TRỊ VIÊN (DO THÀNH VIÊN 1 TỰ CODE 100% - ĐÃ RÚT GỌN TINH GIẢN)
    // Quản lý: Account, Employee, AdminStaff, WarehouseStaff, SalesStaff
    // ==============================================================
    private static boolean showAdminMenu() {
        System.out.println("\n==============================================================");
        System.out.println("             MENU DÀNH CHO ADMIN (THÀNH VIÊN 1)               ");
        System.out.println("==============================================================");
        System.out.println(" [1] Xem danh sách toàn bộ tài khoản hệ thống (Account)");
        System.out.println(" [2] Đổi mật khẩu & Khóa / Mở khóa tài khoản (Account)");
        System.out.println(" [3] Xem danh sách toàn bộ nhân sự cửa hàng (Employee)");
        System.out.println(" [4] Thêm nhân viên mới (AdminStaff / WarehouseStaff / SalesStaff)");
        System.out.println(" [5] Cập nhật thông tin & lương nhân viên theo Mã ID");
        System.out.println(" [6] Xóa nhân viên khỏi hệ thống theo Mã ID");
        System.out.println(" [7] Tìm kiếm nhân viên theo Tên hoặc Mã ID");
        System.out.println(" [8] Xuất bảng tính lương chi tiết nhân viên (ISalaryCalculable)");
        System.out.println(" [9] Đăng xuất (Quay lại màn hình Đăng nhập)");
        System.out.println(" [0] Thoát toàn bộ chương trình");
        System.out.println("==============================================================");
        System.out.print("➤ Chọn chức năng (0-9): ");

        int choice = readIntChoice(0, 9);
        switch (choice) {
            case 1:
                System.out.println("\n--- [TV1: ADMIN] DANH SÁCH TÀI KHOẢN HỆ THỐNG ---");
                for (Account a : accountManager.getAll()) {
                    System.out.printf("ID: %-6s | User: %-12s | Role: %-10s | Status: %-8s | Last Login: %s%n",
                        a.getId(), a.getUsername(), a.getRole().name(), a.getStatus(), a.getLastLoginTime());
                }
                break;
            case 2:
                System.out.println("\n--- [TV1: ADMIN] ĐỔI MẬT KHẨU / KHÓA TÀI KHOẢN ---");
                break;
            case 3:
                System.out.println("\n--- [TV1: ADMIN] XEM DANH SÁCH NHÂN SỰ CỬA HÀNG ---");
                break;
            case 4:
                System.out.println("\n--- [TV1: ADMIN] THÊM NHÂN VIÊN MỚI (Admin/Kho/Sales) ---");
                break;
            case 5:
                System.out.println("\n--- [TV1: ADMIN] CẬP NHẬT THÔNG TIN LƯƠNG NHÂN VIÊN ---");
                break;
            case 6:
                System.out.println("\n--- [TV1: ADMIN] XÓA NHÂN VIÊN THEO MÃ ID ---");
                break;
            case 7:
                System.out.println("\n--- [TV1: ADMIN] TÌM KIẾM NHÂN VIÊN THEO TÊN / ID ---");
                break;
            case 8:
                System.out.println("\n--- [TV1: ADMIN] XUẤT BẢNG LƯƠNG (ISalaryCalculable.calculateSalary) ---");
                break;
            case 9:
                System.out.println("\n[✓] Đã đăng xuất khỏi tài khoản Quản trị viên.");
                return false;
            case 0:
                System.out.println("\n[✓] Đang lưu dữ liệu và thoát...");
                System.exit(0);
        }
        return true;
    }

    // ==============================================================
    // 3. MENU 2: NHÂN VIÊN KHO (DO THÀNH VIÊN 2 TỰ CODE 100%)
    // Quản lý: Product, Phone, Smartphone, FeaturePhone, Accessory
    // ==============================================================
    private static boolean showWarehouseMenu() {
        System.out.println("\n==============================================================");
        System.out.println("             MENU NHÂN VIÊN KHO (THÀNH VIÊN 2)                ");
        System.out.println("==============================================================");
        System.out.println(" [1] Xem danh mục toàn bộ sản phẩm & Tồn kho (Product)");
        System.out.println(" [2] Thêm sản phẩm mới: Smartphone (Điện thoại thông minh)");
        System.out.println(" [3] Thêm sản phẩm mới: FeaturePhone (Điện thoại phổ thông)");
        System.out.println(" [4] Thêm sản phẩm mới: Accessory (Phụ kiện sạc, tai nghe, ốp)");
        System.out.println(" [5] Cập nhật thông tin / Giá bán sản phẩm theo Mã ID");
        System.out.println(" [6] Xóa sản phẩm khỏi danh mục kho theo Mã ID");
        System.out.println(" [7] Tìm kiếm sản phẩm theo Tên / Thương hiệu");
        System.out.println(" [8] Nhập hàng tăng số lượng tồn kho (Stock In)");
        System.out.println(" [9] Cảnh báo & Lọc danh sách hàng sắp hết (isLowStock)");
        System.out.println(" [10] Áp dụng chương trình chiết khấu sản phẩm (IDiscountable)");
        System.out.println(" [11] Đăng xuất (Quay lại màn hình Đăng nhập)");
        System.out.println(" [0] Thoát toàn bộ chương trình");
        System.out.println("==============================================================");
        System.out.print("➤ Chọn chức năng (0-11): ");

        int choice = readIntChoice(0, 11);
        switch (choice) {
            case 1:
                System.out.println("\n--- [TV2: KHO] HIỂN THỊ TỒN KHO TOÀN BỘ SẢN PHẨM ---");
                break;
            case 2:
                System.out.println("\n--- [TV2: KHO] THÊM MỚI SMARTPHONE (Dung lượng RAM/ROM) ---");
                break;
            case 3:
                System.out.println("\n--- [TV2: KHO] THÊM MỚI FEATUREPHONE (Pin bền, SOS) ---");
                break;
            case 4:
                System.out.println("\n--- [TV2: KHO] THÊM MỚI ACCESSORY (Phụ kiện) ---");
                break;
            case 5:
                System.out.println("\n--- [TV2: KHO] CẬP NHẬT THÔNG TIN / GIÁ BÁN THEO ID ---");
                break;
            case 6:
                System.out.println("\n--- [TV2: KHO] XÓA SẢN PHẨM KHỎI KHO THEO ID ---");
                break;
            case 7:
                System.out.println("\n--- [TV2: KHO] TÌM KIẾM SẢN PHẨM THEO TÊN / HÃNG ---");
                break;
            case 8:
                System.out.println("\n--- [TV2: KHO] NHẬP HÀNG TĂNG TỒN KHO (increaseStock) ---");
                break;
            case 9:
                System.out.println("\n--- [TV2: KHO] CẢNH BÁO TỒN KHO DƯỚI NGƯỠNG (isLowStock) ---");
                break;
            case 10:
                System.out.println("\n--- [TV2: KHO] TÍNH GIÁ CHIẾT KHẤU (calculateDiscountedPrice) ---");
                break;
            case 11:
                System.out.println("\n[✓] Đã đăng xuất khỏi tài khoản Nhân viên kho.");
                return false;
            case 0:
                System.out.println("\n[✓] Đang lưu dữ liệu và thoát...");
                System.exit(0);
        }
        return true;
    }

    // ==============================================================
    // 4. MENU 3: NHÂN VIÊN BÁN HÀNG (DO THÀNH VIÊN 3 TỰ CODE 100%)
    // Quản lý: Invoice, InvoiceDetail, Payment, CashPayment, ElectronicPayment
    // ==============================================================
    private static boolean showSalesMenu() {
        System.out.println("\n==============================================================");
        System.out.println("            MENU NHÂN VIÊN BÁN HÀNG (THÀNH VIÊN 3)            ");
        System.out.println("==============================================================");
        System.out.println(" [1] Xem danh sách toàn bộ hóa đơn bán hàng đã lập (Invoice)");
        System.out.println(" [2] Lập Hóa đơn bán lẻ mới (*Gọi 1 lệnh trừ kho của TV2)");
        System.out.println(" [3] Xem chi tiết các dòng mặt hàng trong hóa đơn (InvoiceDetail)");
        System.out.println(" [4] In phiếu hóa đơn thanh toán chuẩn VAT (IPrintable)");
        System.out.println(" [5] Xử lý Thanh toán Tiền mặt (CashPayment - Tự tính tiền thối)");
        System.out.println(" [6] Xử lý Thanh toán Chuyển khoản QR (ElectronicPayment)");
        System.out.println(" [7] Tìm kiếm hóa đơn bán hàng theo Mã ID");
        System.out.println(" [8] Hủy hóa đơn bán lẻ theo Mã ID (Cancel Invoice)");
        System.out.println(" [9] Thống kê tổng doanh thu bán lẻ trong ngày");
        System.out.println(" [10] Đăng xuất (Quay lại màn hình Đăng nhập)");
        System.out.println(" [0] Thoát toàn bộ chương trình");
        System.out.println("==============================================================");
        System.out.print("➤ Chọn chức năng (0-10): ");

        int choice = readIntChoice(0, 10);
        switch (choice) {
            case 1:
                System.out.println("\n--- [TV3: BÁN HÀNG] DANH SÁCH TOÀN BỘ HÓA ĐƠN ---");
                break;
            case 2:
                System.out.println("\n--- [TV3: BÁN HÀNG] LẬP HÓA ĐƠN BÁN LẺ (Trừ kho TV2) ---");
                break;
            case 3:
                System.out.println("\n--- [TV3: BÁN HÀNG] CHI TIẾT CÁC DÒNG HÀNG (InvoiceDetail) ---");
                break;
            case 4:
                System.out.println("\n--- [TV3: BÁN HÀNG] IN PHIẾU HÓA ĐƠN (IPrintable.printReceipt) ---");
                break;
            case 5:
                System.out.println("\n--- [TV3: BÁN HÀNG] THANH TOÁN TIỀN MẶT (CashPayment.calculateChange) ---");
                break;
            case 6:
                System.out.println("\n--- [TV3: BÁN HÀNG] THANH TOÁN CHUYỂN KHOẢN (generatePaymentQRCode) ---");
                break;
            case 7:
                System.out.println("\n--- [TV3: BÁN HÀNG] TÌM KIẾM HÓA ĐƠN THEO MÃ ID ---");
                break;
            case 8:
                System.out.println("\n--- [TV3: BÁN HÀNG] HỦY HÓA ĐƠN THEO MÃ ID ---");
                break;
            case 9:
                System.out.println("\n--- [TV3: BÁN HÀNG] THỐNG KÊ TỔNG DOANH THU BÁN LẺ ---");
                break;
            case 10:
                System.out.println("\n[✓] Đã đăng xuất khỏi tài khoản Bán hàng.");
                return false;
            case 0:
                System.out.println("\n[✓] Đang lưu dữ liệu và thoát...");
                System.exit(0);
        }
        return true;
    }

    // ==============================================================
    // 5. MENU 4: CỔNG THÔNG TIN KHÁCH HÀNG / USER (DO THÀNH VIÊN 4 TỰ CODE 100%)
    // Dành cho: Người mua hàng (User/Customer) quản lý tài khoản & dịch vụ của chính mình
    // Quản lý: Customer, StandardCustomer, VipCustomer, CustomerReview, WarrantyClaim
    // ==============================================================
    private static boolean showUserMenu() {
        System.out.println("\n==============================================================");
        System.out.println("         CỔNG THÔNG TIN KHÁCH HÀNG (CUSTOMER / USER)          ");
        System.out.println("                     (THÀNH VIÊN 4 PHỤ TRÁCH)                 ");
        System.out.println("==============================================================");
        System.out.println(" [1] Xem thông tin cá nhân & Chi tiêu tích lũy (My Profile)");
        System.out.println(" [2] Cập nhật thông tin cá nhân của bạn (SĐT, Email)");
        System.out.println(" [3] Tra cứu hạng thành viên & Tiến trình lên VIP (ILoyaltyRankable)");
        System.out.println(" [4] Xem đặc quyền giảm giá & Ưu đãi khách hàng VIP (VipCustomer)");
        System.out.println(" [5] Gửi đánh giá phản hồi chất lượng sản phẩm & dịch vụ (1-5 sao)");
        System.out.println(" [6] Xem lại lịch sử các đánh giá của bạn (CustomerReview)");
        System.out.println(" [7] Gửi yêu cầu tiếp nhận bảo hành sửa chữa máy (WarrantyClaim)");
        System.out.println(" [8] Tra cứu tiến trình xử lý phiếu bảo hành của bạn");
        System.out.println(" [9] Đăng xuất (Quay lại màn hình Đăng nhập)");
        System.out.println(" [0] Thoát toàn bộ chương trình");
        System.out.println("==============================================================");
        System.out.print("➤ Chọn chức năng (0-9): ");

        int choice = readIntChoice(0, 9);
        switch (choice) {
            case 1:
                System.out.println("\n--- [TV4: USER] THÔNG TIN CÁ NHÂN & CHI TIÊU TÍCH LŨY ---");
                break;
            case 2:
                System.out.println("\n--- [TV4: USER] CẬP NHẬT SĐT & EMAIL LIÊN HỆ ---");
                break;
            case 3:
                System.out.println("\n--- [TV4: USER] XẾP HẠNG THÀNH VIÊN (ILoyaltyRankable.evaluateRank) ---");
                break;
            case 4:
                System.out.println("\n--- [TV4: USER] ĐẶC QUYỀN GIẢM GIÁ VIP (VipCustomer.calculateVipDiscount) ---");
                break;
            case 5:
                System.out.println("\n--- [TV4: USER] GỬI ĐÁNH GIÁ CHẤT LƯỢNG (CustomerReview 1-5 sao) ---");
                break;
            case 6:
                System.out.println("\n--- [TV4: USER] XEM LỊCH SỬ ĐÁNH GIÁ CỦA BẢN THÂN ---");
                break;
            case 7:
                System.out.println("\n--- [TV4: USER] GỬI YÊU CẦU BẢO HÀNH THIẾT BỊ (WarrantyClaim) ---");
                break;
            case 8:
                System.out.println("\n--- [TV4: USER] TRA CỨU TIẾN TRÌNH XỬ LÝ BẢO HÀNH ---");
                break;
            case 9:
                System.out.println("\n[✓] Đã đăng xuất khỏi tài khoản Khách hàng.");
                return false;
            case 0:
                System.out.println("\n[✓] Đang lưu dữ liệu và thoát...");
                System.exit(0);
        }
        return true;
    }

    // Helper kiểm tra nhập số nguyên an toàn
    private static int readIntChoice(int min, int max) {
        while (true) {
            try {
                int value = Integer.parseInt(scanner.nextLine().trim());
                if (value >= min && value <= max) return value;
            } catch (NumberFormatException ignored) {}
            System.out.printf("(!) Vui lòng nhập số nguyên từ %d đến %d: ", min, max);
        }
    }
}
```

---

## 4. Mẫu Cặp Lớp Kế Thừa & Đa Hình Tiêu Biểu

### 4.1. Interface `ISalaryCalculable.java`
```java
package com.smartphonestore.model.auth;

public interface ISalaryCalculable {
    double calculateSalary();
}
```

### 4.2. Lớp Cha `Employee.java`
```java
package com.smartphonestore.model.auth;

import java.time.Year;

public abstract class Employee implements ISalaryCalculable {
    protected String id;
    protected String fullName;
    protected String phoneNumber;
    protected double baseSalary;
    protected String role;

    // THUỘC TÍNH RIÊNG BIỆT CỦA EMPLOYEE
    protected int hireYear;

    public Employee(String id, String fullName, String phoneNumber, double baseSalary, String role, int hireYear) {
        this.id = id;
        this.fullName = fullName;
        this.phoneNumber = phoneNumber;
        this.baseSalary = baseSalary;
        this.role = role;
        this.hireYear = hireYear;
    }

    // PHƯƠNG THỨC RIÊNG BIỆT: Tính thâm niên công tác
    public int getSeniorityYears() {
        int currentYear = Year.now().getValue();
        return Math.max(0, currentYear - hireYear);
    }

    public String getId() { return id; }
    public String getFullName() { return fullName; }
    public double getBaseSalary() { return baseSalary; }
}
```

### 4.3. Lớp Con `SalesStaff.java` (Nhân viên bán hàng)
```java
package com.smartphonestore.model.auth;

public class SalesStaff extends Employee {
    // THUỘC TÍNH RIÊNG BIỆT CỦA SALES STAFF
    private double commissionRate; // Ví dụ: 0.03 (tức 3%)
    private double monthlyRevenue; // Doanh số bán trong tháng

    public SalesStaff(String id, String fullName, String phoneNumber, double baseSalary, int hireYear, double commissionRate) {
        super(id, fullName, phoneNumber, baseSalary, "SALES", hireYear);
        this.commissionRate = commissionRate;
        this.monthlyRevenue = 0.0;
    }

    public void addMonthlyRevenue(double amount) {
        if (amount > 0) this.monthlyRevenue += amount;
    }

    // PHƯƠNG THỨC RIÊNG BIỆT (Ghi đè tính lương theo hoa hồng doanh số)
    @Override
    public double calculateSalary() {
        return this.baseSalary + (this.monthlyRevenue * this.commissionRate);
    }

    public double getCommissionRate() { return commissionRate; }
    public double getMonthlyRevenue() { return monthlyRevenue; }
}
```
