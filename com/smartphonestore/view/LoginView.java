package com.smartphonestore.view;

import com.smartphonestore.manager.AccountManager;
import com.smartphonestore.manager.EmployeeManager;
import com.smartphonestore.model.auth.Account;
import com.smartphonestore.model.auth.Employee;
import com.smartphonestore.model.enums.Role;
import com.smartphonestore.util.InputHelper;

public class LoginView {
    private final AccountManager accountManager;
    private final EmployeeManager employeeManager;

    public LoginView(AccountManager accountManager, EmployeeManager employeeManager) {
        this.accountManager = accountManager;
        this.employeeManager = employeeManager;
    }

    public void start() {
        while (true) {
            System.out.println("\n==============================================================");
            System.out.println("        HỆ THỐNG QUẢN LÝ CỬA HÀNG ĐIỆN THOẠI - ĐĂNG NHẬP       ");

            System.out.println("==============================================================");
            System.out.println(" [1] Đăng nhập");
            System.out.println(" [2] Đăng ký tài khoản khách hàng");
            System.out.println(" [0] Thoát chương trình");

            System.out.println("==============================================================");
            int choice = InputHelper.readInt("=> Chọn thao tác (0-2): ", 0, 2);

            switch (choice) {
                case 1: {
                    Account acc = handleLogin();
                    if (acc != null) {
                        routeByRole(acc);
                    }
                    break;
                }
                case 2:
                    handleRegister();
                    break;
                case 0:
                    System.out.println("\n[✓] Tạm biệt!");
                    return;
            }
        }
    }

    private Account handleLogin() {
        System.out.println("\n--- ĐĂNG NHẬP ---");
        String username = InputHelper.readString("=> Tên đăng nhập: ");
        String password = InputHelper.readString("=> Mật khẩu: ");

        if (accountManager.isLocked(username)) {
            System.out.println("[X] Tài khoản đã bị khóa. Vui lòng liên hệ Quản trị viên.");
            return null;
        }
        // Lấy giờ đăng nhập trước, vì login() sẽ ghi đè thành giờ hiện tại
        Account before = accountManager.findByUsername(username);
        String previousLogin = (before != null) ? before.getLastLoginTime() : "";

        Account acc = accountManager.login(username, password);
        if (acc == null) {
            System.out.println("[X] Sai tên đăng nhập hoặc mật khẩu!");
            return null;
        }
        System.out.println("\n[✓] ĐĂNG NHẬP THÀNH CÔNG!");
        System.out.println("Xin chào: " + getDisplayName(acc) + " | Vai trò: " + acc.getRole().getDisplayName());
        System.out.println("Lần đăng nhập trước: " + previousLogin);
        return acc;
    }
    // Nhân viên thì hiện họ tên, khách thì hiện username (hồ sơ khách do TV4 quản lý)
    private String getDisplayName(Account acc) {
        if (acc.getRole().isStaff()) {
            Employee e = employeeManager.findById(acc.getLinkedId());
            if (e != null) return e.getFullName();
        }
        return acc.getUsername();
    }

    // Khách phải có hồ sơ CUSxx tại cửa hàng trước (nhân viên tạo), rồi mới tự đăng ký tài khoản
    private void handleRegister() {
        System.out.println("\n--- ĐĂNG KÝ TÀI KHOẢN KHÁCH HÀNG ---");
        System.out.println("(Bạn cần có mã khách hàng do cửa hàng cấp, ví dụ CUS01)");
        String customerId = InputHelper.readString("➤ Mã khách hàng: ").toUpperCase();

        if (!accountManager.customerExists(customerId)) {
            System.out.println("[X] Không tìm thấy hồ sơ khách hàng " + customerId
                + ". Vui lòng liên hệ quầy để được tạo hồ sơ.");
            return;
        }
        if (accountManager.findByLinkedId(customerId) != null) {
            System.out.println("[X] Mã khách hàng " + customerId + " đã có tài khoản.");
            return;
        }

        String username = InputHelper.readString("➤ Tên đăng nhập mới: ");
        String password = InputHelper.readString("➤ Mật khẩu (từ 6 ký tự): ");
        String confirm = InputHelper.readString("➤ Nhập lại mật khẩu: ");
        if (!password.equals(confirm)) {
            System.out.println("[X] Mật khẩu nhập lại không khớp!");
            return;
        }

        String error = accountManager.createAccount(username, password, Role.CUSTOMER, customerId);
        if (error == null) {
            System.out.println("[✓] Đăng ký thành công! Hãy đăng nhập bằng tài khoản vừa tạo.");
        } else {
            System.out.println("[X] " + error);
        }
    }
    private void routeByRole(Account acc) {
        switch (acc.getRole()) {
            case ADMIN:
                new AdminMenu(accountManager, employeeManager, acc).displayMenu();
                break;
            case WAREHOUSE:
                new WarehouseMenu().displayMenu(); // TV2 - hàm không static nên phải new
                break;
            case SALES:
                // TV3: thay bằng new SalesMenu(...).displayMenu() khi TV3 làm xong
                System.out.println("[!] Menu Bán hàng (TV3) đang được phát triển.");
                break;
            case CUSTOMER:
                // TV4: thay bằng menu khách tự xem, truyền acc.getLinkedId() (mã CUSxx)
                System.out.println("[!] Menu Khách hàng (TV4) đang được phát triển. Mã khách: "
                    + acc.getLinkedId());
                break;
        }
        System.out.println("[✓] Đã đăng xuất.");
    }
}