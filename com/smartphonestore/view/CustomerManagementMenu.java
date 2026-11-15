package com.smartphonestore.view;

import com.smartphonestore.exception.ValidationException;
import com.smartphonestore.manager.AccountManager;
import com.smartphonestore.manager.CustomerManager;
import com.smartphonestore.manager.SalesManager;
import com.smartphonestore.manager.ProductManager;
import com.smartphonestore.model.product.ProductDetail;
import com.smartphonestore.model.sale.Invoice;
import com.smartphonestore.model.sale.InvoiceDetail;
import com.smartphonestore.model.product.Product;
import com.smartphonestore.model.customer.*;
import com.smartphonestore.model.enums.Role;
import com.smartphonestore.util.DateUtil;
import com.smartphonestore.util.InputHelper;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Menu "QUẢN LÝ KHÁCH HÀNG" dành cho ADMIN và nhân viên BÁN HÀNG (TV4).
 * Gọi từ AdminMenu (TV1) và SalesMenu (TV3):
 *     new CustomerManagementMenu(customerManager, accountManager, SalesManager, productManager, role).show();
 */
public class CustomerManagementMenu {
    private final CustomerManager manager;
    private final AccountManager accountManager;
    private final SalesManager salesManager;
    private final ProductManager productManager;
    private final Role role;

    public CustomerManagementMenu(CustomerManager manager, AccountManager accountManager, SalesManager salesManager, ProductManager productManager, Role role) {
        if (role != Role.ADMIN && role != Role.SALES) {
            throw new IllegalArgumentException("Chỉ Quản trị viên và Nhân viên bán hàng được vào menu này");
        }
        this.manager = manager;
        this.accountManager = accountManager;
        this.salesManager = salesManager;
        this.productManager = productManager;
        this.role = role;
    }

    /** Chạy đến khi chọn [0], sau đó quay lại menu trước. */
    public void show() {
        while (true) {
            printMenu();
            int choice = InputHelper.readInt("Chọn: ", 0, 11);
            if (choice == 0) return;
            try {
                switch (choice) {
                    case 1: listCustomers(); break;
                    case 2: showDetail(); break;
                    case 3: addCustomer(); break;
                    case 4: deleteCustomer(); break;
                    case 5: searchCustomer(); break;
                    case 6: checkRank(); break;
                    case 7: purchaseHistory(); break;
                    case 8: upgradeVip(); break;
                    case 9: viewReviews(); break;
                    case 10: receiveClaim(); break;
                    case 11: claimMenu(); break;
                    default: break;
                }
            } catch (IllegalArgumentException e) {
                System.out.println("(!) " + e.getMessage());
            }
        }
    }

    private void printMenu() {
        System.out.println("\n========== QUẢN LÝ KHÁCH HÀNG (" + role.getDisplayName() + ") ==========");
        System.out.println("[1] Xem danh sách khách hàng");
        System.out.println("[2] Xem chi tiết một khách theo mã");
        System.out.println("[3] Thêm khách hàng mới");
        System.out.println("[4] Xóa khách hàng (chỉ Admin)");
        System.out.println("[5] Tìm kiếm khách theo tên / SĐT");
        System.out.println("[6] Tra cứu hạng & tiến trình lên VIP");
        System.out.println("[7] Lịch sử mua hàng của khách");
        System.out.println("[8] Nâng khách lên VIP");
        System.out.println("[9] Xem đánh giá (theo khách / theo sản phẩm)");
        System.out.println("[10] Tiếp nhận bảo hành (nhập số seri)");
        System.out.println("[11] Tra cứu & xử lý phiếu bảo hành");
        System.out.println("[0] Quay lại menu trước");
    }

    // ---------- Tiện ích ----------
    private Customer askCustomer() {
        String id = InputHelper.readString("Nhập mã khách (ví dụ CUS01): ");
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
        String phone = InputHelper.readString("SĐT (10 hoặc 11 số, bắt đầu bằng 0): ");
        String email = InputHelper.readString("Email: ");
        Customer c = manager.addCustomer(name, phone, email);
        System.out.println("Đã thêm khách hàng, mã: " + c.getId());
        while (true) {
            try {
                createAccountFor(c);
                break;
            } catch (IllegalArgumentException e) {
                System.out.println("(!) " + e.getMessage());
                if (!confirm("Nhập lại?")) {
                    System.out.println("Khách đã được thêm nhưng chưa có tài khoản.");
                    break;
                }
            }
        }
    }

    private void deleteCustomer() {
        if (role != Role.ADMIN) {
            System.out.println("(!) Chỉ Quản trị viên mới được xóa khách hàng.");
            return;
        }
        Customer c = askCustomer();
        // Khách mới (chưa thanh toán lần nào) có totalSpent = 0 nhưng vẫn có thể đang có hóa đơn UNPAID,
        // xóa đi thì lúc thanh toán sẽ không còn hồ sơ để cộng chi tiêu
        if (salesManager.hasInvoicesOfCustomer(c.getId())) {
            System.out.println("(!) Khách đã có hóa đơn (kể cả chưa thanh toán hoặc đã hủy), không thể xóa.");
            return;
        }
        System.out.println("Sắp xóa: " + c.getId() + " - " + c.getFullName());
        if (!confirm("Xác nhận xóa?")) {
            System.out.println("Đã hủy.");
            return;
        }
        if (manager.deleteCustomer(c.getId())) {
            accountManager.removeAccountsByOwnerId(c.getId());   // tài khoản đăng nhập của khách bị xóa
            System.out.println("Đã xóa khách và tài khoản đăng nhập liên kết (nếu có).");
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
        
        System.out.printf("%s (%s) - đã chi tiêu %,.0f đ%n", c.getFullName(), c.getId(), c.getTotalSpent());
        System.out.println("Hạng: " + c.evaluateRank());
    }

    private void purchaseHistory() {
    Customer c = askCustomer();
    System.out.println("--- Lịch sử mua hàng của " + c.getFullName() + " (" + c.getId() + ") ---");
    boolean any = false;
    int paidCount = 0;
    double paidTotal = 0;
    for (Invoice inv : salesManager.getAllInvoices()) {
        if (!c.getId().equalsIgnoreCase(inv.getCustomerId())) continue;
        any = true;
        System.out.printf("%s | %s | %s | %,.0f đ (gồm VAT)%n",
            inv.getId(), inv.getCreationDate(), inv.getStatus(), inv.calculateFinalTotal());
        for (InvoiceDetail d : salesManager.getDetailsByInvoice(inv.getId())) {
            ProductDetail pd = productManager.findDetailBySerial(d.getSerialNumber());
            Product p = (pd == null) ? null : productManager.findByID(pd.getProductId());
            System.out.printf("      - %s | seri %s | %,.0f đ%n",
                    p == null ? "(không rõ sản phẩm)" : p.getName(),
                    d.getSerialNumber(), d.calculateLineTotal());
        }
        if ("PAID".equals(inv.getStatus())) {
            paidCount++;
            paidTotal += inv.calculateFinalTotal();
        }
    }
    if (!any) {
        System.out.println("Khách này chưa có hóa đơn nào.");
        return;
    }
    System.out.printf("Đã thanh toán: %d hóa đơn, tổng %,.0f đ%n", paidCount, paidTotal);
}

    private void upgradeVip() {
        Customer c = askCustomer();
        Customer vip = manager.upgradeToVip(c.getId());
        System.out.println("Đã nâng " + vip.getFullName() + " lên VIP. " + vip.evaluateRank());
    }

    // ---------- Tài khoản ----------
    private void createAccountFor(Customer c) {
        System.out.println("Cấp tài khoản cho " + c.getId() + " - " + c.getFullName());
        String username = InputHelper.readString("Tên đăng nhập (4-20 ký tự chữ, số, _ hoặc .): ");
        String password = InputHelper.readString("Mật khẩu (từ 6 ký tự): ");
        String error = accountManager.createAccount(username, password, Role.CUSTOMER, c.getId());
        if (error != null) throw new IllegalArgumentException(error);
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

    /** Hỏi số seri: nhập trực tiếp, hoặc tra theo khách nếu khách quên seri. */
    private String askSerial() {
        System.out.println("[1] Nhập số seri   [2] Khách quên seri (tra theo khách)");
        if (InputHelper.readInt("Chọn: ", 1, 2) == 1) {
            return InputHelper.readString("Số seri máy: ").trim().toUpperCase();
        }
        return pickSerialOfCustomer();
    }

    /** Liệt kê máy khách đã mua và thanh toán, nhân viên chọn một máy. Trả null nếu hủy. */
    private String pickSerialOfCustomer() {
        String key = InputHelper.readString("Mã khách hoặc SĐT: ");
        Customer c = manager.findById(key);
        if (c == null) {
            Customer[] found = manager.search(key);
            if (found.length == 0) throw new IllegalArgumentException("Không tìm thấy khách " + key);
            if (found.length > 1) {
                throw new IllegalArgumentException("Có " + found.length
                        + " khách khớp, hãy nhập mã khách (dùng mục [5] để tìm)");
            }
            c = found[0];
        }

        List<String> serials = new ArrayList<>();
        System.out.println("--- Máy khách " + c.getFullName() + " (" + c.getId() + ") đã mua ---");
        for (Invoice inv : salesManager.getAllInvoices()) {
            if (!"PAID".equals(inv.getStatus()) || !c.getId().equalsIgnoreCase(inv.getCustomerId())) continue;
            LocalDate saleDate = DateUtil.parse(inv.getCreationDate());
            for (InvoiceDetail d : salesManager.getDetailsByInvoice(inv.getId())) {
                ProductDetail pd = productManager.findDetailBySerial(d.getSerialNumber());
                if (pd == null) continue;
                Product p = productManager.findByID(pd.getProductId());
                if (p == null) continue;
                LocalDate expiry = saleDate.plusMonths(p.getWarrantyMonths());
                String state = LocalDate.now().isAfter(expiry) ? "đã hết hạn " : "còn đến ";
                serials.add(pd.getSerialNumber());
                System.out.printf("[%d] %s | %s | mua %s | %s | BH %s%s%n", serials.size(),
                        pd.getSerialNumber(), p.getName(), DateUtil.format(saleDate),
                        pd.getStatus(), state, DateUtil.format(expiry));
            }
        }
        if (serials.isEmpty()) {
            throw new IllegalArgumentException("Khách này chưa có máy nào đã mua và thanh toán");
        }
        int pick = InputHelper.readInt("Chọn máy (0 = hủy): ", 0, serials.size());
        return pick == 0 ? null : serials.get(pick - 1);
    }

    // ---------- Bảo hành ----------
    /** Nhân viên chỉ nhập số seri: hệ thống tự tìm người mua, ngày bán và thời hạn bảo hành. */
    private void receiveClaim() {
       String serial = askSerial();
        if (serial == null) return;        // nhân viên chọn hủy
        String buyerId = salesManager.getBuyerIdBySerial(serial);              // TV3
        if (buyerId == null) {
            throw new ValidationException("Seri này chưa bán, chưa thanh toán, đã hủy hoặc hóa đơn không có mã khách");
        }
        ProductDetail detail = productManager.findDetailBySerial(serial);

        if (detail == null) {
            throw new ValidationException("Không có số seri " + serial + " trong kho");
        }

        // Chỉ tiếp nhận bảo hành nếu máy đã bán
        if (!ProductManager.SOLD.equals(detail.getStatus())) {
            throw new ValidationException(
                "Seri đang ở trạng thái " + detail.getStatus() + ", không nhận bảo hành"
            );
        }
        String productId = detail.getProductId();
        LocalDate saleDate = DateUtil.parse(salesManager.getSaleDateBySerial(serial));   // TV3
        Product product = productManager.findByID(productId);
        if (product == null) {
            throw new ValidationException("Không tìm thấy sản phẩm " + productId + " của số seri " + serial);
        }
        int months =product.getWarrantyMonths();             
        Customer buyer = manager.findById(buyerId);
        if (buyer == null) {
            throw new ValidationException("Người mua " + buyerId + " chưa có hồ sơ khách hàng");
        }
        System.out.println("Người mua : " + buyer.getId() + " - " + buyer.getFullName());
        System.out.println("Sản phẩm  : " + productId + " | bán ngày " + DateUtil.format(saleDate)
                + " | bảo hành " + months + " tháng");
        String issue = InputHelper.readString("Mô tả lỗi: ");
        if (!productManager.setWarrantyStatus(serial, true)) {            // SOLD -> WARRANTY
            throw new ValidationException("Không chuyển được seri " + serial + " sang WARRANTY");
        }
        WarrantyClaim w;
        try{
         w= manager.addClaim(serial, productId, buyer.getId(), issue, saleDate, months);
        } catch(RuntimeException e) {
            productManager.setWarrantyStatus(serial, false);              // hoàn tác: WARRANTY -> SOLD
            throw e;
        }

        System.out.println("Đã tiếp nhận phiếu " + w.getClaimId() + ". Còn bảo hành đến "
                + DateUtil.format(w.getWarrantyExpiryDate()) + ". " + w.getExpectedCompletionText() + ".");
    }

    private void claimMenu() {
        System.out.println("[1] Xem phiếu bảo hành của một khách");
        System.out.println("[2] Tra phiếu theo mã phiếu hoặc số seri");
        System.out.println("[3] Cập nhật trạng thái phiếu");
        System.out.println("[4] Xóa phiếu đã đóng (chỉ Admin)");
        System.out.println("[0] Quay lại Menu trước");
        int sub = InputHelper.readInt("Chọn: ", 0, 4);
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
            case 4:
                deleteClaim();
                break;
            default:
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
        String key = InputHelper.readString("Nhập mã phiếu (CLM01) hoặc số seri: ");
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
        if (w.isClosed()) {
            productManager.setWarrantyStatus(w.getSerialNumber(), false);
        }   
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
