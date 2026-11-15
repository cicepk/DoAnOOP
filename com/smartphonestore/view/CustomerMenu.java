<<<<<<< HEAD
package com.smartphonestore.view;

import com.smartphonestore.manager.CustomerManager;
import com.smartphonestore.manager.ProductManager;
import com.smartphonestore.manager.SalesManager;
import com.smartphonestore.model.product.Product;
import com.smartphonestore.model.product.ProductDetail;
import com.smartphonestore.model.sale.Invoice;
import com.smartphonestore.model.sale.InvoiceDetail;
import com.smartphonestore.model.customer.*;
import com.smartphonestore.util.InputHelper;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Menu "KHÁCH HÀNG" cho người đăng nhập bằng tài khoản role CUSTOMER (TV4).
 * Menu không bao giờ hỏi mã khách: khách chỉ thấy dữ liệu của chính mình (customerId lấy từ tài khoản đăng nhập).
 *     boolean dangXuat = new CustomerMenu(customerManager, account.getLinkedId(), productManager).show();
 */
public class CustomerMenu {
    private final CustomerManager manager;
    private final String customerId;
    private final ProductManager productManager;
    private final SalesManager salesManager;

    public CustomerMenu(CustomerManager manager, String customerId, ProductManager productManager, SalesManager salesManager) {
        this.manager = manager;
        this.customerId = customerId;
        this.productManager = productManager;
        this.salesManager = salesManager;
    }

    /**
     * @return true nếu khách chọn Đăng xuất (quay lại màn hình đăng nhập),
     *         false nếu chọn Thoát chương trình
     */
    public boolean show() {
        while (true) {
            Customer me = manager.findById(customerId);
            if (me == null) {
                System.out.println("(!) Hồ sơ khách hàng không còn tồn tại. Vui lòng liên hệ cửa hàng.");
                return true;
            }
            printMenu(me);
            int choice = InputHelper.readInt("Chọn: ", 0, 8);
            if (choice == 8) return true;
            if (choice == 0) return false;
            try {
                switch (choice) {
                    case 1: viewProfile(me); break;
                    case 2: updateContact(); break;
                    case 3: checkRank(me); break;
                    case 4: vipPrivileges(me); break;
                    case 5: addReview(); break;
                    case 6: myReviews(); break;
                    case 7: myClaims(); break;
                    default: break;
                }
            } catch (IllegalArgumentException e) {
                System.out.println("(!) " + e.getMessage());
            }
        }
    }

    private void printMenu(Customer me) {
        System.out.println("\n========== KHÁCH HÀNG: " + me.getFullName() + " (" + me.getType() + ") ==========");
        System.out.println("[1] Xem hồ sơ & chi tiêu tích lũy");
        System.out.println("[2] Cập nhật SĐT / Email");
        System.out.println("[3] Hạng thành viên & tiến trình lên VIP");
        System.out.println("[4] Đặc quyền giảm giá VIP");
        System.out.println("[5] Gửi đánh giá sản phẩm");
        System.out.println("[6] Đánh giá của tôi (xem / sửa / xóa)");
        System.out.println("[7] Phiếu bảo hành của tôi");
        System.out.println("[8] Đăng xuất");
        System.out.println("[0] Thoát chương trình");
    }

    private void viewProfile(Customer me) {
        Customer.printHeader();
        me.display();
    }

    /** Các sản phẩm khách này đã mua và đã thanh toán, mỗi sản phẩm chỉ xuất hiện một lần. */
    private List<Product> getPurchasedProducts() {
        List<Product> result = new ArrayList<>();
        Set<String> seen = new HashSet<>();
        for (Invoice inv : salesManager.getAllInvoices()) {
            if (!"PAID".equals(inv.getStatus()) || !customerId.equalsIgnoreCase(inv.getCustomerId())) continue;
            for (InvoiceDetail d : salesManager.getDetailsByInvoice(inv.getId())) {
                ProductDetail pd = productManager.findDetailBySerial(d.getSerialNumber());
                if (pd == null) continue;
                if (!seen.add(pd.getProductId())) continue;
                Product p = productManager.findByID(pd.getProductId());
                if (p != null) result.add(p);
            }
        }
        return result;
    }

   private void updateContact() {
    Customer me = manager.findById(customerId);
    if (me == null) throw new IllegalArgumentException("Không tìm thấy hồ sơ của bạn");

    System.out.println("SĐT hiện tại  : " + me.getPhoneNumber());
    System.out.println("Email hiện tại: " + me.getEmail());
    System.out.println("[1] Sửa cả SĐT và email");
    System.out.println("[2] Chỉ sửa SĐT");
    System.out.println("[3] Chỉ sửa email");
    System.out.println("[0] Hủy");
    int choice = InputHelper.readInt("Chọn: ", 0, 3);
    if (choice == 0) return;

    String phone = me.getPhoneNumber();   // giữ nguyên nếu không sửa
    String email = me.getEmail();
    if (choice == 1 || choice == 2) {
        phone = InputHelper.readString("SĐT mới (10, bắt đầu bằng 0): ");
    }
    if (choice == 1 || choice == 3) {
        email = InputHelper.readString("Email mới: ");
    }
    manager.updateContact(customerId, phone, email);
    System.out.println("Đã cập nhật thông tin.");
}

    private void checkRank(Customer me) {
        System.out.printf("Tổng chi tiêu tích lũy: %,.0f đ%n", me.getTotalSpent());
        System.out.println("Hạng của bạn: " + me.evaluateRank());   // đa hình: STANDARD / VIP
    }

    private void vipPrivileges(Customer me) {
        if (!(me instanceof VipCustomer)) {
            System.out.println("Bạn chưa phải khách VIP. " + me.evaluateRank());
            return;
        }
        VipCustomer vip = (VipCustomer) me;
        System.out.printf("Ưu đãi của bạn: giảm %.0f%% trên mọi đơn hàng.%n", vip.getVipDiscountRate() * 100);
        double total = InputHelper.readDouble("Nhập giá trị đơn hàng dự kiến để xem mức giảm (đ): ", 1);
        double discount = manager.getVipDiscount(customerId, total);
        System.out.printf("Bạn được giảm %,.0f đ, còn phải trả %,.0f đ%n", discount, total - discount);
    }

    private void addReview() {
    List<Product> bought = getPurchasedProducts();
    if (bought.isEmpty()) {
        System.out.println("Bạn chưa có sản phẩm nào đã mua và thanh toán nên chưa thể đánh giá.");
        return;
    }
    System.out.println("--- Sản phẩm bạn đã mua ---");
    for (int i = 0; i < bought.size(); i++) {
        System.out.printf("[%d] %s%n", i + 1, bought.get(i).getName());
    }
    int pick = InputHelper.readInt("Chọn sản phẩm cần đánh giá (0 = hủy): ", 0, bought.size());
    if (pick == 0) return;
    Product product = bought.get(pick - 1);

    int stars = InputHelper.readInt("Số sao (1-5): ", 1, 5);
    String comment = InputHelper.readString("Nhận xét: ");
    CustomerReview r = manager.addReview(customerId, product.getId(), stars, comment);
    System.out.println("Cảm ơn bạn! Đã ghi nhận đánh giá " + r.getReviewId());
    }

    private void myReviews() {
        List<CustomerReview> list = manager.getReviewsByCustomer(customerId);
        if (list.isEmpty()) {
            System.out.println("Bạn chưa có đánh giá nào.");
            return;
        }
        CustomerReview.printHeader();
        for (CustomerReview r : list) r.display();
        System.out.println("[1] Sửa một đánh giá  [2] Xóa một đánh giá  [3] Quay lại");
        int sub = InputHelper.readInt("Chọn: ", 1, 3);
        if (sub == 1) {
            String id = InputHelper.readString("Mã đánh giá cần sửa: ");
            int stars = InputHelper.readInt("Số sao mới (1-5): ", 1, 5);
            String comment = InputHelper.readString("Nhận xét mới: ");
            manager.updateReview(id, customerId, stars, comment);
            System.out.println("Đã cập nhật đánh giá.");
        } else if (sub == 2) {
            String id = InputHelper.readString("Mã đánh giá cần xóa: ");
            if (InputHelper.readInt("Xác nhận xóa? [1] Đồng ý  [2] Hủy: ", 1, 2) == 1) {
                manager.deleteReview(id, customerId);
                System.out.println("Đã xóa đánh giá.");
            }
        }
    }

    private void myClaims() {
        List<WarrantyClaim> list = manager.getClaimsByCustomer(customerId);
        if (list.isEmpty()) {
            System.out.println("Bạn chưa có phiếu bảo hành nào. Cần bảo hành, vui lòng mang máy đến cửa hàng.");
            return;
        }
        WarrantyClaim.printCustomerHeader();
        for (WarrantyClaim w : list) w.displayForCustomer();
    }
}
=======
package com.smartphonestore.view;

import com.smartphonestore.manager.CustomerManager;
import com.smartphonestore.manager.ProductManager;
import com.smartphonestore.manager.SalesManager;
import com.smartphonestore.model.product.Product;
import com.smartphonestore.model.product.ProductDetail;
import com.smartphonestore.model.sale.Invoice;
import com.smartphonestore.model.sale.InvoiceDetail;
import com.smartphonestore.model.customer.*;
import com.smartphonestore.util.InputHelper;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Menu "KHÁCH HÀNG" cho người đăng nhập bằng tài khoản role CUSTOMER (TV4).
 * Menu không bao giờ hỏi mã khách: khách chỉ thấy dữ liệu của chính mình (customerId lấy từ tài khoản đăng nhập).
 *     boolean dangXuat = new CustomerMenu(customerManager, account.getLinkedId(), productManager).show();
 */
public class CustomerMenu {
    private final CustomerManager manager;
    private final String customerId;
    private final ProductManager productManager;
    private final SalesManager salesManager;

    public CustomerMenu(CustomerManager manager, String customerId, ProductManager productManager, SalesManager salesManager) {
        this.manager = manager;
        this.customerId = customerId;
        this.productManager = productManager;
        this.salesManager = salesManager;
    }

    /**
     * @return true nếu khách chọn Đăng xuất (quay lại màn hình đăng nhập),
     *         false nếu chọn Thoát chương trình
     */
    public boolean show() {
        while (true) {
            Customer me = manager.findById(customerId);
            if (me == null) {
                System.out.println("(!) Hồ sơ khách hàng không còn tồn tại. Vui lòng liên hệ cửa hàng.");
                return true;
            }
            printMenu(me);
            int choice = InputHelper.readInt("Chọn: ", 0, 8);
            if (choice == 8) return true;
            if (choice == 0) return false;
            try {
                switch (choice) {
                    case 1: viewProfile(me); break;
                    case 2: updateContact(); break;
                    case 3: checkRank(me); break;
                    case 4: vipPrivileges(me); break;
                    case 5: addReview(); break;
                    case 6: myReviews(); break;
                    case 7: myClaims(); break;
                    default: break;
                }
            } catch (IllegalArgumentException e) {
                System.out.println("(!) " + e.getMessage());
            }
        }
    }

    private void printMenu(Customer me) {
        System.out.println("\n========== KHÁCH HÀNG: " + me.getFullName() + " (" + me.getType() + ") ==========");
        System.out.println("[1] Xem hồ sơ & chi tiêu tích lũy");
        System.out.println("[2] Cập nhật SĐT / Email");
        System.out.println("[3] Hạng thành viên & tiến trình lên VIP");
        System.out.println("[4] Đặc quyền giảm giá VIP");
        System.out.println("[5] Gửi đánh giá sản phẩm");
        System.out.println("[6] Đánh giá của tôi (xem / sửa / xóa)");
        System.out.println("[7] Phiếu bảo hành của tôi");
        System.out.println("[8] Đăng xuất");
        System.out.println("[0] Thoát chương trình");
    }

    private void viewProfile(Customer me) {
        Customer.printHeader();
        me.display();
    }

    /** Các sản phẩm khách này đã mua và đã thanh toán, mỗi sản phẩm chỉ xuất hiện một lần. */
    private List<Product> getPurchasedProducts() {
        List<Product> result = new ArrayList<>();
        Set<String> seen = new HashSet<>();
        for (Invoice inv : salesManager.getAllInvoices()) {
            if (!"PAID".equals(inv.getStatus()) || !customerId.equalsIgnoreCase(inv.getCustomerId())) continue;
            for (InvoiceDetail d : salesManager.getDetailsByInvoice(inv.getId())) {
                ProductDetail pd = productManager.findDetailBySerial(d.getSerialNumber());
                if (pd == null) continue;
                if (!seen.add(pd.getProductId())) continue;
                Product p = productManager.findByID(pd.getProductId());
                if (p != null) result.add(p);
            }
        }
        return result;
    }

   private void updateContact() {
    Customer me = manager.findById(customerId);
    if (me == null) throw new IllegalArgumentException("Không tìm thấy hồ sơ của bạn");

    System.out.println("SĐT hiện tại  : " + me.getPhoneNumber());
    System.out.println("Email hiện tại: " + me.getEmail());
    System.out.println("[1] Sửa cả SĐT và email");
    System.out.println("[2] Chỉ sửa SĐT");
    System.out.println("[3] Chỉ sửa email");
    System.out.println("[0] Hủy");
    int choice = InputHelper.readInt("Chọn: ", 0, 3);
    if (choice == 0) return;

    String phone = me.getPhoneNumber();   // giữ nguyên nếu không sửa
    String email = me.getEmail();
    if (choice == 1 || choice == 2) {
        phone = InputHelper.readString("SĐT mới (10 hoặc 11 số, bắt đầu bằng 0): ");
    }
    if (choice == 1 || choice == 3) {
        email = InputHelper.readString("Email mới: ");
    }
    manager.updateContact(customerId, phone, email);
    System.out.println("Đã cập nhật thông tin.");
}

    private void checkRank(Customer me) {
        System.out.printf("Tổng chi tiêu tích lũy: %,.0f đ%n", me.getTotalSpent());
        System.out.println("Hạng của bạn: " + me.evaluateRank());   // đa hình: STANDARD / VIP
    }

    private void vipPrivileges(Customer me) {
        if (!(me instanceof VipCustomer)) {
            System.out.println("Bạn chưa phải khách VIP. " + me.evaluateRank());
            return;
        }
        VipCustomer vip = (VipCustomer) me;
        System.out.printf("Ưu đãi của bạn: giảm %.0f%% trên mọi đơn hàng.%n", vip.getVipDiscountRate() * 100);
        double total = InputHelper.readDouble("Nhập giá trị đơn hàng dự kiến để xem mức giảm (đ): ", 1);
        double discount = manager.getVipDiscount(customerId, total);
        System.out.printf("Bạn được giảm %,.0f đ, còn phải trả %,.0f đ%n", discount, total - discount);
    }

    private void addReview() {
    List<Product> bought = getPurchasedProducts();
    if (bought.isEmpty()) {
        System.out.println("Bạn chưa có sản phẩm nào đã mua và thanh toán nên chưa thể đánh giá.");
        return;
    }
    System.out.println("--- Sản phẩm bạn đã mua ---");
    for (int i = 0; i < bought.size(); i++) {
        System.out.printf("[%d] %s%n", i + 1, bought.get(i).getName());
    }
    int pick = InputHelper.readInt("Chọn sản phẩm cần đánh giá (0 = hủy): ", 0, bought.size());
    if (pick == 0) return;
    Product product = bought.get(pick - 1);

    int stars = InputHelper.readInt("Số sao (1-5): ", 1, 5);
    String comment = InputHelper.readString("Nhận xét: ");
    CustomerReview r = manager.addReview(customerId, product.getId(), stars, comment);
    System.out.println("Cảm ơn bạn! Đã ghi nhận đánh giá " + r.getReviewId());
    }

    private void myReviews() {
        List<CustomerReview> list = manager.getReviewsByCustomer(customerId);
        if (list.isEmpty()) {
            System.out.println("Bạn chưa có đánh giá nào.");
            return;
        }
        CustomerReview.printHeader();
        for (CustomerReview r : list) r.display();
        System.out.println("[1] Sửa một đánh giá  [2] Xóa một đánh giá  [3] Quay lại");
        int sub = InputHelper.readInt("Chọn: ", 1, 3);
        if (sub == 1) {
            String id = InputHelper.readString("Mã đánh giá cần sửa: ");
            int stars = InputHelper.readInt("Số sao mới (1-5): ", 1, 5);
            String comment = InputHelper.readString("Nhận xét mới: ");
            manager.updateReview(id, customerId, stars, comment);
            System.out.println("Đã cập nhật đánh giá.");
        } else if (sub == 2) {
            String id = InputHelper.readString("Mã đánh giá cần xóa: ");
            if (InputHelper.readInt("Xác nhận xóa? [1] Đồng ý  [2] Hủy: ", 1, 2) == 1) {
                manager.deleteReview(id, customerId);
                System.out.println("Đã xóa đánh giá.");
            }
        }
    }

    private void myClaims() {
        List<WarrantyClaim> list = manager.getClaimsByCustomer(customerId);
        if (list.isEmpty()) {
            System.out.println("Bạn chưa có phiếu bảo hành nào. Cần bảo hành, vui lòng mang máy đến cửa hàng.");
            return;
        }
        WarrantyClaim.printCustomerHeader();
        for (WarrantyClaim w : list) w.displayForCustomer();
    }
}
>>>>>>> pkiet
