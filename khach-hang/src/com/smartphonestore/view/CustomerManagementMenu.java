package com.smartphonestore.view;

import com.smartphonestore.manager.AccountManager;
import com.smartphonestore.manager.CustomerManager;
import com.smartphonestore.manager.InvoiceManager;
import com.smartphonestore.manager.ProductManager;
import com.smartphonestore.model.customer.*;
import com.smartphonestore.model.enums.Role;
import com.smartphonestore.util.DateUtil;
import com.smartphonestore.util.InputHelper;
import java.time.LocalDate;
import java.util.List;

/**
 * Menu "QUẢN LÝ KHÁCH HÀNG" dành cho ADMIN và nhân viên BÁN HÀNG (TV4).
 * Gọi từ AdminMenu (TV1) và SalesMenu (TV3):
 *     new CustomerManagementMenu(customerManager, accountManager, invoiceManager, productManager, role).show();
 */
public class CustomerManagementMenu {
    private final CustomerManager manager;
    private final AccountManager accountManager;
    private final InvoiceManager invoiceManager;
    private final ProductManager productManager;
    private final Role role;

    public CustomerManagementMenu(CustomerManager manager, AccountManager accountManager, InvoiceManager invoiceManager, ProductManager productManager, Role role) {
        if (role != Role.ADMIN && role != Role.SALES) {
            throw new IllegalArgumentException("Chỉ Quản trị viên và Nhân viên bán hàng được vào menu này");
        }
        this.manager = manager;
        this.accountManager = accountManager;
        this.invoiceManager = invoiceManager;
        this.productManager = productManager;
        this.role = role;
    }

    /** Chạy đến khi chọn [0], sau đó quay lại menu trước. */
    public void show() {
        while (true) {
            printMenu();
            int choice = InputHelper.readInt("Chọn: ", 0, 12);
            if (choice == 0) return;
            try {
                switch (choice) {
                    case 1: listCustomers(); break;
                    case 2: showDetail(); break;
                    case 3: addCustomer(); break;
                    case 4: deleteCustomer(); break;
                    case 5: searchCustomer(); break;
                    case 6: checkRank(); break;
                    case 7: calcVipDiscount(); break;
                    case 8: upgradeVip(); break;
                    case 9: issueAccount(); break;
                    case 10: viewReviews(); break;
                    case 11: receiveClaim(); break;
                    case 12: claimMenu(); break;
                    default: break;
                }
            } catch (IllegalArgumentException e) {
                System.out.println("(!) " + e.getMessage());
            }
        }
    }

    private void printMenu() {
        System.out.println("\n========== QUẢN LÝ KHÁCH HÀNG (" + role.getDisplayName() + ") ==========");
        System.out.println("[1]  Xem danh sách khách hàng");
        System.out.println("[2]  Xem chi tiết một khách theo mã");
        System.out.println("[3]  Thêm khách hàng mới");
        System.out.println("[4]  Xóa khách hàng (chỉ Admin)");
        System.out.println("[5]  Tìm kiếm khách theo tên / SĐT");
        System.out.println("[6]  Tra cứu hạng & tiến trình lên VIP");
        System.out.println("[7]  Tính ưu đãi VIP cho một đơn hàng");
        System.out.println("[8]  Nâng khách lên VIP");
        System.out.println("[9]  Cấp tài khoản đăng nhập cho khách");
        System.out.println("[10] Xem đánh giá (theo khách / theo sản phẩm)");
        System.out.println("[11] Tiếp nhận bảo hành (nhập số seri)");
        System.out.println("[12] Tra cứu & xử lý phiếu bảo hành");
        System.out.println("[0]  Quay lại menu trước");
    }

    // ---------- Tiện ích ----------
    private Customer askCustomer() {
        String id = InputHelper.readString("Nhập mã khách (ví dụ KH001): ");
        Customer c = manager.findById(id);
        if (c == null) throw new IllegalArgumentException("Không tìm thấy khách hàng " + id);
        return c;
    }

    private boolean confirm(String question) {
        return InputHelper.readInt(question + " [1] Đồng ý  [2] Hủy: ", 1, 2) == 1;
    }

    // ---------- Khách hàng ----------
    private void listCustomers() {
        Customer[] all = manager.getAllCustomers();
        if (all.length == 0) {
            System.out.println("Chưa có khách hàng nào.");
            return;
        }
        Customer.printHeader();
        for (Customer c : all) c.display();
        System.out.println("Tổng: " + all.length + " khách hàng.");
    }

    private void showDetail() {
        Customer c = askCustomer();
        Customer.printHeader();
        c.display();
        System.out.println("Hạng        : " + c.evaluateRank());
        List<CustomerReview> rv = manager.getReviewsByCustomer(c.getId());
        System.out.println("Đánh giá    : " + rv.size() + " đánh giá");
        List<WarrantyClaim> cl = manager.getClaimsByCustomer(c.getId());
        System.out.println("Bảo hành    : " + cl.size() + " phiếu (" + manager.countOpenClaims(c.getId()) + " đang xử lý)");
    }

    private void addCustomer() {
        String name = InputHelper.readString("Họ tên: ");
        String phone = InputHelper.readString("SĐT (10 số, bắt đầu bằng 0): ");
        String email = InputHelper.readString("Email: ");
        Customer c = manager.addCustomer(name, phone, email);
        System.out.println("Đã thêm khách hàng, mã: " + c.getId());
        if (InputHelper.readInt("Cấp tài khoản đăng nhập ngay? [1] Có  [2] Để sau: ", 1, 2) == 1) {
            createAccountFor(c);
        }
    }

    private void deleteCustomer() {
        if (role != Role.ADMIN) {
            System.out.println("(!) Chỉ Quản trị viên mới được xóa khách hàng.");
            return;
        }
        Customer c = askCustomer();
        System.out.println("Sắp xóa: " + c.getId() + " - " + c.getFullName());
        if (!confirm("Xác nhận xóa?")) {
            System.out.println("Đã hủy.");
            return;
        }
        if (manager.deleteCustomer(c.getId())) {
            accountManager.removeAccountsByOwnerId(c.getId());   // tài khoản đăng nhập của khách cũng bị xóa
            System.out.println("Đã xóa khách hàng và tài khoản liên kết (nếu có).");
        }
    }

    private void searchCustomer() {
        String keyword = InputHelper.readString("Nhập tên hoặc SĐT: ");
        Customer[] result = manager.search(keyword);
        if (result.length == 0) {
            System.out.println("Không có kết quả.");
            return;
        }
        Customer.printHeader();
        for (Customer c : result) c.display();
    }

    private void checkRank() {
        Customer c = askCustomer();
        // Đa hình: STANDARD và VIP cùng gọi evaluateRank() nhưng kết quả khác nhau
        ILoyaltyRankable r = (ILoyaltyRankable) c;
        System.out.printf("%s (%s) - đã chi tiêu %,.0f đ%n", c.getFullName(), c.getId(), c.getTotalSpent());
        System.out.println("Hạng: " + r.evaluateRank());
    }

    private void calcVipDiscount() {
        Customer c = askCustomer();
        double total = InputHelper.readDouble("Tổng tiền đơn hàng (đ): ", 1);
        double discount = manager.getVipDiscount(c.getId(), total);
        if (discount <= 0) {
            System.out.println(c.getFullName() + " là khách " + c.getType() + ", không có ưu đãi giảm giá.");
            return;
        }
        System.out.printf("Khách VIP %s: giảm %,.0f đ, còn phải trả %,.0f đ%n",
                c.getFullName(), discount, total - discount);
    }

    private void upgradeVip() {
        Customer c = askCustomer();
        Customer vip = manager.upgradeToVip(c.getId());
        System.out.println("Đã nâng " + vip.getFullName() + " lên VIP. " + vip.evaluateRank());
    }

    // ---------- Tài khoản ----------
    private void issueAccount() {
        createAccountFor(askCustomer());
    }

    private void createAccountFor(Customer c) {
        System.out.println("Cấp tài khoản cho " + c.getId() + " - " + c.getFullName());
        String username = InputHelper.readString("Tên đăng nhập (4-20 ký tự chữ, số, _ hoặc .): ");
        if (!username.matches("[A-Za-z0-9_.]{4,20}")) {
            throw new IllegalArgumentException("Tên đăng nhập phải dài 4-20 ký tự, chỉ gồm chữ, số, _ hoặc .");
        }
        String password = InputHelper.readString("Mật khẩu (từ 6 ký tự): ");
        if (password.length() < 6 || password.contains("|")) {
            throw new IllegalArgumentException("Mật khẩu phải từ 6 ký tự và không chứa ký tự |");
        }
        if (!accountManager.createCustomerAccount(c.getId(), username, password)) {
            throw new IllegalArgumentException(
                    "Không tạo được tài khoản: tên đăng nhập đã tồn tại hoặc khách này đã có tài khoản");
        }
        System.out.println("Đã cấp tài khoản '" + username + "' cho khách " + c.getId() + ".");
    }

    // ---------- Đánh giá ----------
    private void viewReviews() {
        System.out.println("[1] Theo khách hàng   [2] Theo sản phẩm");
        int sub = InputHelper.readInt("Chọn: ", 1, 2);
        List<CustomerReview> list;
        if (sub == 1) {
            Customer c = askCustomer();
            list = manager.getReviewsByCustomer(c.getId());
        } else {
            String pid = InputHelper.readString("Mã sản phẩm: ");
            list = manager.getReviewsByProduct(pid);
            if (!list.isEmpty()) {
                System.out.printf("Điểm trung bình: %.1f / 5 (%d đánh giá)%n", manager.getAverageRating(pid), list.size());
            }
        }
        if (list.isEmpty()) {
            System.out.println("Không có đánh giá nào.");
            return;
        }
        CustomerReview.printHeader();
        for (CustomerReview r : list) r.display();
    }

    // ---------- Bảo hành ----------
    /** Nhân viên chỉ nhập số seri: hệ thống tự tìm người mua, ngày bán và thời hạn bảo hành. */
    private void receiveClaim() {
        String serial = InputHelper.readString("Số seri máy: ").trim().toUpperCase();
        String buyerId = invoiceManager.getBuyerIdBySerial(serial);              // TV3
        if (buyerId == null) {
            throw new IllegalArgumentException("Seri này chưa được bán hoặc hóa đơn đã bị hủy");
        }
        String productId = invoiceManager.getProductIdBySerial(serial);          // TV3
        LocalDate saleDate = DateUtil.parse(invoiceManager.getSaleDateBySerial(serial));   // TV3
        int months = productManager.getWarrantyMonths(productId);                // TV2
        Customer buyer = manager.findById(buyerId);
        if (buyer == null) {
            throw new IllegalArgumentException("Người mua " + buyerId + " chưa có hồ sơ khách hàng");
        }
        System.out.println("Người mua : " + buyer.getId() + " - " + buyer.getFullName());
        System.out.println("Sản phẩm  : " + productId + " | bán ngày " + DateUtil.format(saleDate)
                + " | bảo hành " + months + " tháng");
        String issue = InputHelper.readString("Mô tả lỗi: ");
        WarrantyClaim w = manager.addClaim(serial, productId, buyer.getId(), saleDate, months, issue);
        System.out.println("Đã tiếp nhận phiếu " + w.getClaimId() + ". Còn bảo hành đến "
                + DateUtil.format(w.getWarrantyExpiryDate()) + ". " + w.getExpectedCompletionText() + ".");
    }

    private void claimMenu() {
        System.out.println("[1] Xem phiếu bảo hành của một khách");
        System.out.println("[2] Tra phiếu theo mã phiếu hoặc số seri");
        System.out.println("[3] Cập nhật trạng thái phiếu");
        System.out.println("[4] Xóa phiếu đã đóng (chỉ Admin)");
        int sub = InputHelper.readInt("Chọn: ", 1, 4);
        switch (sub) {
            case 1:
                printClaims(manager.getClaimsByCustomer(askCustomer().getId()));
                break;
            case 2:
                searchClaim();
                break;
            case 3:
                updateClaimStatus();
                break;
            default:
                deleteClaim();
                break;
        }
    }

    private void printClaims(List<WarrantyClaim> list) {
        if (list.isEmpty()) {
            System.out.println("Không có phiếu bảo hành nào.");
            return;
        }
        WarrantyClaim.printHeader();
        for (WarrantyClaim w : list) w.display();
    }

    private void searchClaim() {
        String key = InputHelper.readString("Nhập mã phiếu (BH001) hoặc số seri: ");
        WarrantyClaim w = manager.findClaimById(key);
        if (w != null) {
            WarrantyClaim.printHeader();
            w.display();
        } else {
            printClaims(manager.getClaimsBySerial(key));
        }
    }

    private void updateClaimStatus() {
        String claimId = InputHelper.readString("Mã phiếu bảo hành: ");
        WarrantyClaim w = manager.findClaimById(claimId);
        if (w == null) throw new IllegalArgumentException("Không tìm thấy phiếu " + claimId);
        System.out.println("Trạng thái hiện tại: " + w.getStatusDisplay());
        System.out.println("[1] Đang sửa  [2] Hoàn tất  [3] Từ chối");
        int s = InputHelper.readInt("Trạng thái mới: ", 1, 3);
        String newStatus;
        if (s == 1) newStatus = WarrantyClaim.DANG_SUA;
        else if (s == 2) newStatus = WarrantyClaim.HOAN_TAT;
        else newStatus = WarrantyClaim.TU_CHOI;
        manager.updateClaimStatus(w.getClaimId(), newStatus);   // phiếu đã đóng thì ném lỗi
        System.out.println("Đã cập nhật.");
    }

    private void deleteClaim() {
        if (role != Role.ADMIN) {
            System.out.println("(!) Chỉ Quản trị viên mới được xóa phiếu bảo hành.");
            return;
        }
        String claimId = InputHelper.readString("Mã phiếu cần xóa: ");
        System.out.println(manager.deleteClaim(claimId) ? "Đã xóa." : "Không tìm thấy mã này.");
    }
}
