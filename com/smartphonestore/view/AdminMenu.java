package com.smartphonestore.view;

import com.smartphonestore.manager.AccountManager;
import com.smartphonestore.manager.CustomerManager;
import com.smartphonestore.manager.EmployeeManager;
import com.smartphonestore.manager.ProductManager;
import com.smartphonestore.manager.SalesManager;
import com.smartphonestore.model.auth.Account;
import com.smartphonestore.model.auth.AdminStaff;
import com.smartphonestore.model.auth.Employee;
import com.smartphonestore.model.auth.SalesStaff;
import com.smartphonestore.model.auth.WarehouseStaff;
import com.smartphonestore.model.enums.Role;
import com.smartphonestore.util.InputHelper;
import java.time.LocalDate;
import java.time.Year;
import java.util.ArrayList;
import java.util.List;

/**
 * Menu ADMIN (chủ / quản lý cửa hàng, toàn quyền).
 * [1] Báo cáo  [2] Nhân sự  [3] Kho  [4] Bán hàng  [5] Đổi mật khẩu  [0] Đăng xuất
 * Kho và Bán hàng dùng lại nguyên menu của nhân viên, truyền Account admin vào để hiện thêm mục ★.
 */
public class AdminMenu {
    private final AccountManager accountManager;
    private final EmployeeManager employeeManager;
    private final ProductManager productManager;   // chuyển tiếp cho menu Kho, Bán hàng, Báo cáo
    private final SalesManager salesManager;       // doanh số, báo cáo, chặn xóa nhân viên đã bán hàng
    private final CustomerManager customerManager; // chuyển tiếp cho menu Bán hàng
    private final Account currentAccount;          // admin đang đăng nhập

    public AdminMenu(AccountManager accountManager, EmployeeManager employeeManager, ProductManager productManager,
                     SalesManager salesManager, CustomerManager customerManager, Account currentAccount) {
        this.accountManager = accountManager;
        this.employeeManager = employeeManager;
        this.productManager = productManager;
        this.salesManager = salesManager;
        this.customerManager = customerManager;
        this.currentAccount = currentAccount;
    }

    // ===================== MENU CHÍNH =====================
    public void displayMenu() {
        while (true) {
            System.out.println("\n==============================================================");
            System.out.println("                         MENU QUẢN TRỊ                          ");
            System.out.println("==============================================================");
            System.out.println(" [1] Báo cáo thống kê");
            System.out.println(" [2] Quản lý nhân sự");
            System.out.println(" [3] Quản lý kho");
            System.out.println(" [4] Quản lý bán hàng");
            System.out.println(" [5] Đổi mật khẩu");
            System.out.println(" [0] Đăng xuất");
            System.out.println("==============================================================");
            int choice = InputHelper.readInt("=> Chọn chức năng (0-5): ", 0, 5);

            switch (choice) {
                case 1: new ReportMenu(salesManager, productManager).show(); break;
                case 2: staffMenu(); break;
                case 3: new WarehouseMenu(productManager, currentAccount).displayMenu(); break;
                case 4: openSalesMenu(); break;
                case 5: ChangePasswordView.show(accountManager, currentAccount); break;
                case 0: return; // về LoginView, không System.exit (manager đã lưu file sau mỗi thao tác)
            }
        }
    }

    // [4] Dùng lại nguyên menu Bán hàng; mã EMPxx của admin được ghi vào hóa đơn nếu admin bán
    private void openSalesMenu() {
        new SalesMenu(salesManager, customerManager, employeeManager,
                      accountManager, productManager, currentAccount).show();
    }

    // ===================== [2] QUẢN LÝ NHÂN SỰ =====================
    private void staffMenu() {
        while (true) {
            System.out.println("\n--- QUẢN LÝ NHÂN SỰ ---");
            System.out.println(" [1] Cập nhật nhân viên");
            System.out.println(" [2] Xem thông tin");
            System.out.println(" [0] Quay lại");
            int choice = InputHelper.readInt("=> Chọn (0-2): ", 0, 2);
            switch (choice) {
                case 1: updateStaffMenu(); break;
                case 2: viewStaffMenu(); break;
                case 0: return;
            }
        }
    }

    private void updateStaffMenu() {
        while (true) {
            System.out.println("\n--- CẬP NHẬT NHÂN VIÊN ---");
            System.out.println(" [1] Thêm nhân viên (tạo tài khoản kèm theo)");
            System.out.println(" [2] Sửa thông tin (Tên, Chức vụ, Lương)");
            System.out.println(" [3] Xóa / Cho nghỉ việc");
            System.out.println(" [4] Cấp lại mật khẩu");
            System.out.println(" [5] Khóa / Mở khóa tài khoản");
            System.out.println(" [0] Quay lại");
            int choice = InputHelper.readInt("=> Chọn (0-5): ", 0, 5);
            switch (choice) {
                case 1: addEmployee(); break;
                case 2: updateEmployee(); break;
                case 3: deleteEmployee(); break;
                case 4: resetPassword(); break;
                case 5: toggleLock(); break;
                case 0: return;
            }
        }
    }

    private void viewStaffMenu() {
        while (true) {
            System.out.println("\n--- XEM THÔNG TIN NHÂN SỰ ---");
            System.out.println(" [1] Danh sách nhân viên");
            System.out.println(" [2] Tài khoản nhân viên");
            System.out.println(" [3] Bảng lương");
            System.out.println(" [4] Doanh số nhân viên");
            System.out.println(" [0] Quay lại");
            int choice = InputHelper.readInt("=> Chọn (0-4): ", 0, 4);
            switch (choice) {
                case 1: showAllEmployees(); break;
                case 2: showAllAccounts(); break;
                case 3: showPayroll(); break;
                case 4: showStaffRevenue(); break;
                case 0: return;
            }
        }
    }

    // ===================== TÀI KHOẢN =====================
    private void showAllAccounts() {
        System.out.println("\n--- TÀI KHOẢN NHÂN VIÊN ---");
        System.out.printf("%-6s | %-12s | %-10s | %-7s | %-6s | %s%n",
            "Mã", "Username", "Vai trò", "Status", "Mã NV", "Đăng nhập cuối");
        for (Account acc : accountManager.getAll()) {
            acc.display();
        }
    }

    // Chọn tài khoản theo mã ACCxx, dùng chung cho Cấp lại mật khẩu và Khóa / Mở khóa
    private Account chooseAccount() {
        showAllAccounts();
        String id = InputHelper.readString("=> Nhập mã tài khoản (VD: ACC02): ");
        Account acc = accountManager.findById(id);
        if (acc == null) System.out.println("[X] Không tìm thấy tài khoản " + id);
        return acc;
    }

    // Admin chỉ cấp lại mật khẩu cho người khác; mật khẩu của mình thì tự đổi ở [5]
    private void resetPassword() {
        System.out.println("\n--- CẤP LẠI MẬT KHẨU ---");
        Account acc = chooseAccount();
        if (acc == null) return;
        if (acc.getId().equalsIgnoreCase(currentAccount.getId())) {
            System.out.println("[!] Đây là tài khoản của bạn, hãy dùng mục [5] Đổi mật khẩu ở menu chính.");
            return;
        }
        String newPassword = InputHelper.readString("=> Mật khẩu mới cho " + acc.getUsername() + " (từ 6 ký tự): ");
        if (accountManager.changePassword(acc.getId(), newPassword)) {
            System.out.println("[✓] Đã cấp lại mật khẩu. Hãy nhắc nhân viên tự đổi lại mật khẩu riêng.");
        } else {
            System.out.println("[X] Mật khẩu phải từ 6 ký tự và không chứa ký tự '|'.");
        }
    }

    private void toggleLock() {
        System.out.println("\n--- KHÓA / MỞ KHÓA TÀI KHOẢN ---");
        Account acc = chooseAccount();
        if (acc == null) return;
        if (acc.getId().equalsIgnoreCase(currentAccount.getId())) {
            System.out.println("[X] Không thể tự khóa tài khoản đang đăng nhập.");
            return;
        }
        if (accountManager.toggleLock(acc.getId())) {
            System.out.println("[✓] Trạng thái mới: " + acc.getStatus());
        } else {
            System.out.println("[X] Không thể khóa Admin cuối cùng đang hoạt động.");
        }
    }

    // ===================== XEM / TÌM NHÂN VIÊN =====================
    private void showAllEmployees() {
        System.out.println("\n--- DANH SÁCH NHÂN SỰ ---");
        printEmployees(employeeManager.getAll());
        if (InputHelper.readYesNo("=> Tìm nhân viên theo tên hoặc mã? (Y/N): ")) {
            searchEmployee();
        }
    }

    private void searchEmployee() {
        String keyword = InputHelper.readString("=> Nhập tên hoặc mã cần tìm: ");
        List<Employee> result = employeeManager.searchByNameOrId(keyword);
        System.out.println("Tìm thấy " + result.size() + " nhân viên:");
        printEmployees(result);
    }

    private void printEmployees(List<Employee> list) {
        if (list.isEmpty()) {
            System.out.println("(Không có nhân viên nào)");
            return;
        }
        System.out.printf("%-6s | %-22s | %-11s | %-10s | %12s | %-6s | %s%n",
            "Mã", "Họ tên", "SĐT", "Chức vụ", "Lương CB", "T.niên", "Thông tin riêng");
        for (Employee e : list) {
            e.display();
        }
    }

    // ===================== THÊM NHÂN VIÊN =====================
    // Nhân viên nào cũng cần tài khoản -> thêm nhân viên xong tạo tài khoản luôn
    private void addEmployee() {
        System.out.println("\n--- THÊM NHÂN VIÊN MỚI ---");
        Role role = chooseStaffRole("Chọn loại nhân viên");
        if (role == null) return;

        String id = employeeManager.generateNextId(usedEmployeeIds());
        System.out.println("Mã nhân viên mới: " + id);
        String name = inputName();
        String phone = inputPhone();
        double salary = InputHelper.readDouble("=> Lương cơ bản: ", 0);
        int hireYear = InputHelper.readInt("=> Năm vào làm: ", 2000, Year.now().getValue());

        Employee e = createEmployee(role, id, name, phone, salary, hireYear);
        if (!employeeManager.add(e)) {
            System.out.println("[X] Thêm nhân viên thất bại!");
            return;
        }
        System.out.println("[✓] Đã thêm nhân viên " + id + " - " + name);

        System.out.println("--- Tạo tài khoản đăng nhập cho " + id + " ---");
        while (!createAccountForEmployee(id, role)) {
            if (!InputHelper.readYesNo("=> Nhập lại thông tin tài khoản? (Y/N): ")) {
                System.out.println("[!] Nhân viên chưa có tài khoản. Có thể tạo sau ở mục Sửa thông tin.");
                return;
            }
        }
    }

    // Mã EMPxx còn nằm trong tài khoản (đã khóa) hoặc hóa đơn của nhân viên đã xóa: không cấp lại
    private List<String> usedEmployeeIds() {
        List<String> ids = new ArrayList<>(salesManager.getAllStaffIds());
        for (Account acc : accountManager.getAll()) {
            ids.add(acc.getLinkedId());
        }
        return ids;
    }

    // Trả về null nếu chọn Hủy
    private Role chooseStaffRole(String title) {
        System.out.println(title + ": [1] Quản trị  [2] Thủ kho  [3] Bán hàng  [0] Hủy");
        int type = InputHelper.readInt("=> Chọn (0-3): ", 0, 3);
        switch (type) {
            case 1: return Role.ADMIN;
            case 2: return Role.WAREHOUSE;
            case 3: return Role.SALES;
            default: return null;
        }
    }

    // Dùng chung cho Thêm mới và Đổi chức vụ: nhập phần riêng rồi tạo đúng lớp con
    private Employee createEmployee(Role role, String id, String name, String phone,
                                    double salary, int hireYear) {
        switch (role) {
            case ADMIN: {
                double bonus = InputHelper.readDouble("=> Phụ cấp quản lý: ", 0);
                return new AdminStaff(id, name, phone, salary, hireYear, bonus);
            }
            case WAREHOUSE: {
                String zone = inputWarehouseZone();
                int overtime = InputHelper.readInt("=> Số giờ tăng ca (0-200): ", 0, 200);
                return new WarehouseStaff(id, name, phone, salary, hireYear, zone, overtime);
            }
            default: { // SALES - doanh số lấy từ hóa đơn thật
                double rate = InputHelper.readDouble("=> Tỷ lệ hoa hồng (VD 0.03 = 3%): ", 0, 1);
                return new SalesStaff(id, name, phone, salary, hireYear, rate, 0);
            }
        }
    }

    // Trả true nếu tạo được tài khoản
    private boolean createAccountForEmployee(String employeeId, Role role) {
        if (accountManager.findByLinkedId(employeeId) != null) {
            System.out.println("[X] Nhân viên này đã có tài khoản.");
            return true;
        }
        String username = InputHelper.readString("=> Tên đăng nhập: ");
        String password = InputHelper.readString("=> Mật khẩu (từ 6 ký tự): ");
        String error = accountManager.createAccount(username, password, role, employeeId);
        if (error == null) {
            System.out.println("[✓] Đã tạo tài khoản " + username);
            return true;
        }
        System.out.println("[X] " + error);
        return false;
    }

    // ===================== SỬA THÔNG TIN NHÂN VIÊN =====================
    private void updateEmployee() {
        System.out.println("\n--- SỬA THÔNG TIN NHÂN VIÊN ---");
        Employee e = findEmployeeByInput();
        if (e == null) return;

        System.out.println(" [1] Họ tên        [2] Số điện thoại   [3] Lương cơ bản");
        System.out.println(" [4] Thông tin riêng theo chức vụ    [5] Đổi chức vụ");
        System.out.println(" [6] Tạo tài khoản đăng nhập         [0] Quay lại");
        int choice = InputHelper.readInt("=> Chọn (0-6): ", 0, 6);

        boolean ok;
        switch (choice) {
            case 1 -> ok = e.setFullName(inputName());
            case 2 -> ok = e.setPhoneNumber(inputPhone());
            case 3 -> ok = e.setBaseSalary(InputHelper.readDouble("=> Lương cơ bản mới: ", 0));
            case 4 -> ok = updateExtraInfo(e);
            case 5 -> {
                changeRole(e); return;
            }
            case 6 -> {
                createAccountForEmployee(e.getId(), e.getRole()); return;
            }
            default -> {
                return;
            }
        }
        // Các setter chỉ sửa trong bộ nhớ, nên phải lưu file
        if (ok && employeeManager.saveToFile()) {
            System.out.println("[✓] Cập nhật thành công!");
        } else {
            System.out.println("[X] Cập nhật thất bại!");
        }
    }

    // Kiểm tra lớp con thật sự bằng instanceof rồi ép kiểu để gọi setter riêng
    private boolean updateExtraInfo(Employee e) {
        if (e instanceof AdminStaff) {
            AdminStaff admin = (AdminStaff) e;
            return admin.setManagementBonus(InputHelper.readDouble("=> Phụ cấp quản lý mới: ", 0));
        }
        if (e instanceof WarehouseStaff) {
            WarehouseStaff ws = (WarehouseStaff) e;
            boolean zoneOk = ws.setWarehouseZone(inputWarehouseZone());
            boolean hoursOk = ws.setOvertimeHours(InputHelper.readInt("=> Số giờ tăng ca (0-200): ", 0, 200));
            return zoneOk && hoursOk;
        }
        if (e instanceof SalesStaff) {
            SalesStaff ss = (SalesStaff) e;
            return ss.setCommissionRate(InputHelper.readDouble("=> Tỷ lệ hoa hồng mới (0 - 1): ", 0, 1));
        }
        return false;
    }

    private void changeRole(Employee e) {
        Role newRole = chooseStaffRole("Chức vụ mới (hiện tại: " + e.getRole().getDisplayName() + ")");
        if (newRole == null) return;
        if (newRole == e.getRole()) {
            System.out.println("[!] Chức vụ không đổi. Dùng mục [4] để sửa thông tin riêng.");
            return;
        }
        if (e.getId().equalsIgnoreCase(currentAccount.getLinkedId())) {
            System.out.println("[X] Không thể tự đổi chức vụ của chính mình.");
            return;
        }
        if (e.getRole() == Role.ADMIN && employeeManager.countByRole(Role.ADMIN) <= 1) {
            System.out.println("[X] Đây là Admin cuối cùng, không thể đổi chức vụ.");
            return;
        }
        // Giữ thông tin chung, nhập lại phần riêng theo chức vụ mới
        Employee updated = createEmployee(newRole, e.getId(), e.getFullName(), e.getPhoneNumber(),
                                          e.getBaseSalary(), e.getHireYear());
        if (!employeeManager.replace(updated)) {
            System.out.println("[X] Đổi chức vụ thất bại!");
            return;
        }
        accountManager.updateRoleByLinkedId(e.getId(), newRole); // chưa có tài khoản thì bỏ qua
        System.out.println("[✓] Đã đổi " + e.getId() + " sang " + newRole.getDisplayName());
    }

    // ===================== XÓA / CHO NGHỈ VIỆC =====================
    // Đã lập hóa đơn -> chỉ khóa tài khoản, giữ hồ sơ để báo cáo doanh số còn tên người lập
    // Chưa lập hóa đơn nào -> xóa hẳn như cũ
    private void deleteEmployee() {
        System.out.println("\n--- XÓA / CHO NGHỈ VIỆC ---");
        Employee e = findEmployeeByInput();
        if (e == null) return;

        if (e.getId().equalsIgnoreCase(currentAccount.getLinkedId())) {
            System.out.println("[X] Không thể tự xóa chính mình.");
            return;
        }

        if (salesManager.hasInvoicesOfStaff(e.getId())) {
            System.out.println("[!] Nhân viên này đã lập hóa đơn, xóa hẳn sẽ mất người lập trong báo cáo doanh số.");
            if (!InputHelper.readYesNo("=> Cho " + e.getFullName() + " nghỉ việc (khóa tài khoản)? (Y/N): ")) {
                System.out.println("[!] Đã hủy.");
                return;
            }
            if (e.getRole() == Role.ADMIN && countActiveAdminAccounts() <= 1) {
                System.out.println("[X] Đây là Admin cuối cùng đang hoạt động, không thể cho nghỉ việc.");
                return;
            }
            if (accountManager.lockByLinkedId(e.getId())) {
                System.out.println("[✓] Đã cho nghỉ việc: tài khoản của " + e.getId() + " đã bị khóa.");
            } else {
                System.out.println("[!] Nhân viên này chưa có tài khoản, không cần khóa.");
            }
            return;
        }

        if (!InputHelper.readYesNo("=> Xác nhận xóa " + e.getFullName() + "? (Y/N): ")) {
            System.out.println("[!] Đã hủy.");
            return;
        }
        if (!employeeManager.delete(e.getId())) {
            System.out.println("[X] Không thể xóa (Admin cuối cùng hoặc lỗi lưu file).");
            return;
        }
        System.out.println("[✓] Đã xóa nhân viên " + e.getId());
        if (accountManager.lockByLinkedId(e.getId())) {
            System.out.println("[✓] Đã khóa tài khoản đăng nhập của nhân viên này.");
        }
    }

    private int countActiveAdminAccounts() {
        int count = 0;
        for (Account acc : accountManager.getAll()) {
            if (acc.getRole() == Role.ADMIN && acc.getStatus().equals("ACTIVE")) count++;
        }
        return count;
    }

    // Tài khoản bị khóa = đã nghỉ việc / tạm ngưng -> không tính lương
    private boolean isAccountLocked(Employee e) {
        Account acc = accountManager.findByLinkedId(e.getId());
        return acc != null && acc.getStatus().equals("LOCKED");
    }

    // ===================== BẢNG LƯƠNG =====================
    // Lương bán hàng = lương CB + doanh số tháng này (lấy từ hóa đơn PAID thật) x tỷ lệ hoa hồng
    private void showPayroll() {
        LocalDate now = LocalDate.now();
        int month = now.getMonthValue();
        int year = now.getYear();
        System.out.println("\n--- BẢNG LƯƠNG THÁNG " + month + "/" + year + " ---");
        System.out.printf("%-6s | %-22s | %-10s | %15s | %s%n",
            "Mã", "Họ tên", "Chức vụ", "Lương tháng", "Chi tiết");
        double total = 0;
        for (Employee e : employeeManager.getAll()) {
            if (isAccountLocked(e)) {
                System.out.printf("%-6s | %-22s | %-10s | %15s | %s%n",
                    e.getId(), e.getFullName(), e.getRole().name(), "-", "Tài khoản đã khóa (nghỉ việc)");
                continue;
            }
            double salary;
            String detail;
            if (e instanceof SalesStaff) {
                SalesStaff ss = (SalesStaff) e;
                double revenue = salesManager.revenueByStaff(e.getId(), month, year);
                salary = ss.getBaseSalary() + revenue * ss.getCommissionRate();
                detail = String.format("Hoa hồng %.1f%% | Doanh số tháng: %,.0f",
                    ss.getCommissionRate() * 100, revenue);
            } else {
                salary = e.calculateSalary(); // đa hình: mỗi lớp con tính một kiểu
                detail = e.getExtraInfo();
            }
            total += salary;
            System.out.printf("%-6s | %-22s | %-10s | %,15.0f | %s%n",
                e.getId(), e.getFullName(), e.getRole().name(), salary, detail);
        }
        System.out.printf("TỔNG QUỸ LƯƠNG: %,.0f VNĐ%n", total);
    }

    // ===================== DOANH SỐ NHÂN VIÊN =====================
    private void showStaffRevenue() {
        System.out.println("\n--- DOANH SỐ NHÂN VIÊN ---");
        int month = InputHelper.readInt("=> Tháng (1-12): ", 1, 12);
        int year = InputHelper.readInt("=> Năm: ", 2000, Year.now().getValue());

        System.out.printf("%-6s | %-22s | %-10s | %18s%n", "Mã", "Họ tên", "Chức vụ", "Doanh số (VNĐ)");
        double total = 0;
        for (Employee e : employeeManager.getAll()) {
            if (e.getRole() == Role.WAREHOUSE) continue; // nhân viên kho không bán hàng
            double revenue = salesManager.revenueByStaff(e.getId(), month, year);
            total += revenue;
            System.out.printf("%-6s | %-22s | %-10s | %,18.0f%n",
                e.getId(), e.getFullName(), e.getRole().name(), revenue);
        }
        System.out.printf("TỔNG DOANH SỐ THÁNG %d/%d: %,.0f VNĐ%n", month, year, total);
    }

    // ===================== HÀM NHẬP LIỆU DÙNG CHUNG =====================
    private Employee findEmployeeByInput() {
        String id = InputHelper.readString("=> Nhập mã nhân viên (VD: EMP02): ");
        Employee e = employeeManager.findById(id);
        if (e == null) {
            System.out.println("[X] Không tìm thấy nhân viên " + id);
            return null;
        }
        printEmployees(List.of(e));
        return e;
    }

    private String inputName() {
        while (true) {
            String name = InputHelper.readString("=> Họ tên: ");
            if (Employee.isValidText(name)) return name;
            System.out.println("(!) Họ tên không được chứa ký tự '|'.");
        }
    }

    private String inputPhone() {
        while (true) {
            String phone = InputHelper.readString("=> Số điện thoại (10 số, bắt đầu bằng 0): ");
            if (Employee.isValidPhone(phone)) return phone;
            System.out.println("(!) Số điện thoại không hợp lệ!");
        }
    }

    private String inputWarehouseZone() {
        while (true) {
            String zone = InputHelper.readString("=> Khu kho (KHO_A ... KHO_Z): ").toUpperCase();
            if (zone.matches("KHO_[A-Z]")) return zone;
            System.out.println("(!) Khu kho phải có dạng KHO_A, KHO_B, ...");
        }
    }
}
