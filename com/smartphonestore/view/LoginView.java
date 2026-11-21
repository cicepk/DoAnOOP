package com.smartphonestore.view;

import com.smartphonestore.manager.AccountManager;
import com.smartphonestore.manager.CustomerManager;
import com.smartphonestore.manager.EmployeeManager;
import com.smartphonestore.manager.ProductManager;
import com.smartphonestore.manager.SalesManager;
import com.smartphonestore.model.auth.Account;
import com.smartphonestore.model.auth.Employee;
import com.smartphonestore.util.InputHelper;

// Cửa hàng offline: chỉ nhân viên đăng nhập, khách không có tài khoản
public class LoginView {
    private final AccountManager accountManager;
    private final EmployeeManager employeeManager;
    private final ProductManager productManager;
    private final SalesManager salesManager;
    private final CustomerManager customerManager;

    public LoginView(AccountManager accountManager, EmployeeManager employeeManager, ProductManager productManager,
                     SalesManager salesManager, CustomerManager customerManager) {
        this.accountManager = accountManager;
        this.employeeManager = employeeManager;
        this.productManager = productManager;
        this.salesManager = salesManager;
        this.customerManager = customerManager;
    }

    public void start() {
        while (true) {
            System.out.println("\n==============================================================");
            System.out.println("        HỆ THỐNG QUẢN LÝ CỬA HÀNG ĐIỆN THOẠI - ĐĂNG NHẬP       ");
            System.out.println("==============================================================");
            System.out.println(" [1] Đăng nhập");
            System.out.println(" [0] Thoát chương trình");
            System.out.println("==============================================================");
            int choice = InputHelper.readInt("=> Chọn thao tác (0-1): ", 0, 1);

            if (choice == 0) {
                // Chỉ màn hình đăng nhập được thoát chương trình
                System.out.println("\n[✓] Tạm biệt!");
                return;
            }
            Account acc = handleLogin();
            if (acc != null) {
                routeByRole(acc);
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

    // Tài khoản nào cũng là nhân viên -> hiện họ tên, không tìm thấy hồ sơ thì hiện username
    private String getDisplayName(Account acc) {
        Employee e = employeeManager.findById(acc.getLinkedId());
        return (e != null) ? e.getFullName() : acc.getUsername();
    }

    // Truyền nguyên Account vào menu: menu lấy được role (ẩn mục ★) và mã EMPxx (ghi hóa đơn, phiếu nhập)
    private void routeByRole(Account acc) {
        switch (acc.getRole()) {
            case ADMIN:
                new AdminMenu(accountManager, employeeManager, productManager,
                              salesManager, customerManager, acc).displayMenu();
                break;
            case WAREHOUSE:
                new WarehouseMenu(productManager, acc).displayMenu();
                break;
            case SALES:
                // Truyền cả Account: lấy mã EMPxx ghi hóa đơn và dùng cho Đổi mật khẩu
                new SalesMenu(salesManager, customerManager, employeeManager,
                              accountManager, productManager, acc).show();
                break;
        }
        System.out.println("[✓] Đã đăng xuất.");
    }
}
