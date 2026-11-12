package com.smartphonestore.view;

import com.smartphonestore.manager.AccountManager;
import com.smartphonestore.manager.EmployeeManager;
import com.smartphonestore.model.auth.Account;
import com.smartphonestore.model.auth.AdminStaff;
import com.smartphonestore.model.auth.Employee;
import com.smartphonestore.model.auth.SalesStaff;
import com.smartphonestore.model.auth.WarehouseStaff;
import com.smartphonestore.model.enums.Role;
import com.smartphonestore.util.InputHelper;
import java.time.Year;
import java.util.List;

public class AdminMenu {
    private final AccountManager accountManager;
    private final EmployeeManager employeeManager;
    private final Account currentAccount; // admin đang đăng nhập

    public AdminMenu(AccountManager accountManager, EmployeeManager employeeManager, Account currentAccount) {
        this.accountManager = accountManager;
        this.employeeManager = employeeManager;
        this.currentAccount = currentAccount;
    }

    public void displayMenu() {
        while (true) {
            System.out.println("\n==============================================================");
            System.out.println("             MENU DÀNH CHO ADMIN                ");

            System.out.println("==============================================================");
            System.out.println(" [1] Xem danh sách toàn bộ tài khoản hệ thống");
            System.out.println(" [2] Đổi mật khẩu & Khóa / Mở khóa tài khoản");
            System.out.println(" [3] Xem danh sách toàn bộ nhân sự cửa hàng");
            System.out.println(" [4] Thêm nhân viên mới (Admin / Kho / Bán hàng)");
            System.out.println(" [5] Cập nhật thông tin, lương & chức vụ theo Mã ID");
            System.out.println(" [6] Xóa nhân viên theo Mã ID");
            System.out.println(" [7] Tìm kiếm nhân viên theo Tên hoặc Mã ID");
            System.out.println(" [8] Bảng tính lương nhân viên tháng này");
            System.out.println(" [9] Đăng xuất");
            System.out.println(" [10] Quản lý khách hàng (TV4)");
            System.out.println(" [0] Thoát chương trình");

            System.out.println("==============================================================");
            int choice = InputHelper.readInt("=> Chọn chức năng (0-10): ", 0, 10);

            switch (choice) {
                case 1: showAllAccounts(); break;
                case 2: manageAccount(); break;
                case 3: showAllEmployees(); break;
                case 4: addEmployee(); break;
                case 5: updateEmployee(); break;
                case 6: deleteEmployee(); break;
                case 7: searchEmployee(); break;
                case 8: showPayroll(); break;
                case 9: return; // quay về LoginView
                case 10: openCustomerMenu(); break;
                case 0:
                    accountManager.saveToFile();
                    employeeManager.saveToFile();
                    System.out.println("\n[✓] Đã lưu dữ liệu. Tạm biệt!");
                    System.exit(0);
                    break;
            }
        }
    }
    // ===================== [1] [2] TÀI KHOẢN =====================
    private void showAllAccounts() {
        System.out.println("\n--- DANH SÁCH TÀI KHOẢN ---");
        System.out.printf("%-6s | %-12s | %-10s | %-7s | %-6s | %s%n",
            "Mã", "Username", "Vai trò", "Status", "Mã LK", "Đăng nhập cuối");
        for (Account acc : accountManager.getAll()) {
            acc.display();
        }
    }

    private void manageAccount() {
        showAllAccounts();
        String id = InputHelper.readString("=> Nhập mã tài khoản (VD: ACC02): ");
        Account acc = accountManager.findById(id);
        if (acc == null) {
            System.out.println("[X] Không tìm thấy tài khoản " + id);
            return;
        }
        System.out.println(" [1] Đổi mật khẩu");
        System.out.println(" [2] Khóa / Mở khóa (hiện tại: " + acc.getStatus() + ")");
        System.out.println(" [0] Quay lại");
        int choice = InputHelper.readInt("=> Chọn (0-2): ", 0, 2);

        if (choice == 1) {
            String newPassword = InputHelper.readString("=> Mật khẩu mới (từ 6 ký tự): ");
            if (accountManager.changePassword(acc.getId(), newPassword)) {
                System.out.println("[✓] Đổi mật khẩu thành công!");
            } else {
                System.out.println("[X] Mật khẩu phải từ 6 ký tự và không chứa ký tự '|'.");
            }
        } else if (choice == 2) {
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
    }
    // ===================== [3] [7] XEM / TÌM NHÂN VIÊN =====================
    private void showAllEmployees() {
        System.out.println("\n--- DANH SÁCH NHÂN SỰ ---");
        printEmployees(employeeManager.getAll());
    }

    private void searchEmployee() {
        System.out.println("\n--- TÌM KIẾM NHÂN VIÊN ---");
        String keyword = InputHelper.readString("=> Nhập tên hoặc mã cần tìm: ");
        List<Employee> result = employeeManager.searchByNameOrId(keyword);
        System.out.println("Tìm thấy " + result.size() + " nhân viên:");
        printEmployees(result);
    }
    // In nhân viên
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
    // ===================== [4] THÊM NHÂN VIÊN =====================
    private void addEmployee() {
        System.out.println("\n--- THÊM NHÂN VIÊN MỚI ---");
        Role role = chooseStaffRole("Chọn loại nhân viên");
        if (role == null) return;

        String id = employeeManager.generateNextId();
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

        if (InputHelper.readYesNo("=> Tạo tài khoản đăng nhập cho nhân viên này? (Y/N): ")) {
            createAccountForEmployee(id, role);
        }
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
            default: { // SALES - doanh số bắt đầu = 0, TV3 bán hàng sẽ cộng dần
                double rate = InputHelper.readDouble("=> Tỷ lệ hoa hồng (VD 0.03 = 3%): ", 0, 1);
                return new SalesStaff(id, name, phone, salary, hireYear, rate, 0);
            }
        }
    }

    private void createAccountForEmployee(String employeeId, Role role) {
        if (accountManager.findByLinkedId(employeeId) != null) {
            System.out.println("[X] Nhân viên này đã có tài khoản.");
            return;
        }
        String username = InputHelper.readString("=> Tên đăng nhập: ");
        String password = InputHelper.readString("=> Mật khẩu (từ 6 ký tự): ");
        String error = accountManager.createAccount(username, password, role, employeeId);
        if (error == null) {
            System.out.println("[✓] Đã tạo tài khoản " + username);
        } else {
            System.out.println("[X] " + error);
        }
    }

    // ===================== [5] CẬP NHẬT NHÂN VIÊN =====================

    private void updateEmployee() {
        System.out.println("\n--- CẬP NHẬT NHÂN VIÊN ---");
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

    // ===================== [6] XÓA NHÂN VIÊN =====================

    private void deleteEmployee() {
        System.out.println("\n--- XÓA NHÂN VIÊN ---");
        Employee e = findEmployeeByInput();
        if (e == null) return;

        if (e.getId().equalsIgnoreCase(currentAccount.getLinkedId())) {
            System.out.println("[X] Không thể tự xóa chính mình.");
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

    // ===================== [8] BẢNG LƯƠNG =====================

    private void showPayroll() {
        System.out.println("\n--- BẢNG LƯƠNG THÁNG NÀY ---");
        System.out.printf("%-6s | %-22s | %-10s | %15s | %s%n",
            "Mã", "Họ tên", "Chức vụ", "Lương tháng", "Chi tiết");
        for (Employee e : employeeManager.getAll()) {
            // Đa hình: cùng gọi calculateSalary() nhưng mỗi lớp con tính một kiểu
            System.out.printf("%-6s | %-22s | %-10s | %,15.0f | %s%n",
                e.getId(), e.getFullName(), e.getRole().name(), e.calculateSalary(), e.getExtraInfo());
        }
        System.out.printf("TỔNG QUỸ LƯƠNG: %,.0f VNĐ%n", employeeManager.getTotalSalary());
    }

    // ===================== [10] KHÁCH HÀNG (TV4) =====================

    private void openCustomerMenu() {
        // Khi TV4 làm xong: new CustomerMenu(customerManager, Role.ADMIN).displayMenu();
        System.out.println("             MENU DÀNH CHO KHÁCH                ");
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
