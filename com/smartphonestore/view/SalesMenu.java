package com.smartphonestore.view;

import com.smartphonestore.manager.*;
import com.smartphonestore.model.auth.Account;
import com.smartphonestore.model.auth.Employee;
import com.smartphonestore.model.auth.SalesStaff;
import com.smartphonestore.model.customer.Customer;
import com.smartphonestore.model.product.Product;
import com.smartphonestore.model.product.ProductDetail;
import com.smartphonestore.model.sale.*;
import com.smartphonestore.util.DateUtil;
import com.smartphonestore.util.InputHelper;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/** Menu nghiệp vụ bán hàng; mọi thao tác quay lại menu trước, không kết thúc JVM. */
public class SalesMenu {
    private final SalesManager salesManager;
    private final CustomerManager customerManager;
    private final EmployeeManager employeeManager;
    private final AccountManager accountManager;
    private final ProductManager productManager;
    private final Account currentUser;
    private final String staffId;

    /** Constructor chuẩn: tài khoản cung cấp mã nhân viên liên kết EMPxx. */
    public SalesMenu(SalesManager salesManager, CustomerManager customerManager,
                     EmployeeManager employeeManager, AccountManager accountManager,
                     ProductManager productManager, Account currentUser) {
        if (salesManager == null || currentUser == null)
            throw new IllegalArgumentException("Thiếu SalesManager hoặc tài khoản đăng nhập.");
        this.salesManager = salesManager;
        this.customerManager = customerManager;
        this.employeeManager = employeeManager;
        this.accountManager = accountManager;
        this.productManager = productManager;
        this.currentUser = currentUser;
        this.staffId = currentUser.getLinkedId() == null ? "" : currentUser.getLinkedId().trim();
    }

    public void show() {
        while (true) {
            System.out.println("\n========== BÁN HÀNG ==========");
            System.out.println("[1] Bán hàng");
            System.out.println("[2] Tra cứu");
            System.out.println("[3] Chăm sóc khách hàng");
            System.out.println("[4] Doanh số của tôi");
            System.out.println("[5] Thu cũ đổi mới");
            System.out.println("[6] Đổi mật khẩu");
            System.out.println("[0] Đăng xuất");
            int choice = InputHelper.readInt("Chọn: ", 0, 6);
            try {
                switch (choice) {
                    case 0: return;
                    case 1: saleMenu(); break;
                    case 2: lookupMenu(); break;
                    case 3: openCustomerMenu(); break;
                    case 4: showMyRevenue(); break;
                    case 5: tradeIn(); break;
                    case 6: ChangePasswordView.show(accountManager, currentUser); break;
                    default: break;
                }
            } catch (RuntimeException e) { System.out.println("(!) " + e.getMessage()); }
        }
    }

    private void saleMenu() {
        while (true) {
            System.out.println("\n--- BÁN HÀNG ---");
            System.out.println("[1] Lập hóa đơn");
            System.out.println("[2] Thanh toán (tiền mặt / chuyển khoản)");
            System.out.println("[3] In hóa đơn");
            System.out.println("[4] Hủy hóa đơn chưa thanh toán");
            System.out.println("[0] Quay lại");
            int c = InputHelper.readInt("Chọn: ", 0, 4);
            if (c == 0) return;
            try {
                switch (c) {
                    case 1: createInvoice(); break;
                    case 2:
                        int method = InputHelper.readInt("[1] Tiền mặt  [2] Chuyển khoản/QR: ", 1, 2);
                        if (method == 1) payCash(); else payElectronic();
                        break;
                    case 3: printInvoice(); break;
                    case 4: cancelInvoice(); break;
                    default: break;
                }
            } catch (RuntimeException e) { System.out.println("(!) " + e.getMessage()); }
        }
    }

    private void lookupMenu() {
        while (true) {
            System.out.println("\n--- TRA CỨU ---");
            System.out.println("[1] Tra cứu hóa đơn");
            System.out.println("[2] Xem tồn kho (chỉ xem)");
            System.out.println("[0] Quay lại");
            int c = InputHelper.readInt("Chọn: ", 0, 2);
            if (c == 0) return;
            if (c == 1) searchInvoice();
            else {
                String kw = InputHelper.readOptionalString("Tên / hãng (Enter = tất cả): ");
                List<Product> list = kw.isEmpty() ? productManager.getALL() : productManager.findByNameOrBrand(kw);
                if (list.isEmpty()) System.out.println("Không tìm thấy sản phẩm.");
                for (Product p : list)
                    System.out.printf("%s | %-35s | %,12.0f đ | còn %d%n", p.getId(), p.getName(), p.getSalePrice(), productManager.countInStock(p.getId()));
            }
        }
    }

    /**
     * Hỏi khách theo mã hoặc SĐT; không thấy thì cho tạo nhanh hồ sơ hoặc bán khách lẻ.
     * @return mã CUSxx, "" nếu khách lẻ, null nếu nhân viên chọn hủy
     */
    private String askCustomerId() {
        String key = InputHelper.readString("Mã khách / SĐT (KHACHLE nếu khách lẻ): ").trim();
        if ("KHACHLE".equalsIgnoreCase(key) || customerManager == null) return "";
        Customer customer = customerManager.findById(key);
        if (customer == null) customer = customerManager.findByPhone(key);
        if (customer == null) {
            System.out.println("Không tìm thấy khách " + key + ".");
            int option = InputHelper.readInt("[1] Tạo nhanh hồ sơ  [2] Bán khách lẻ  [0] Hủy: ", 0, 2);
            if (option == 0) return null;
            if (option == 2) return "";
            String name = InputHelper.readString("Họ tên: ");
            String phone = key.matches("0\\d{9,10}") ? key : InputHelper.readString("SĐT: ");
            String email = InputHelper.readString("Email (ví dụ ten@gmail.com): ");   // CustomerManager bắt buộc email hợp lệ
            customer = customerManager.addCustomer(name, phone, email);
            System.out.println("Đã tạo khách " + customer.getId());
        }
        return customer.getId();
    }

    private void createInvoice() {
        String invoiceId = salesManager.nextInvoiceId();
        String customerId = askCustomerId();
        if (customerId == null) return;

        List<InvoiceDetail> lines = new ArrayList<>();
        System.out.println("Quét/nhập từng Serial/IMEI. Nhập 0 để kết thúc.");
        while (true) {
            String serial = InputHelper.readString("Serial/IMEI: ");
            if ("0".equals(serial)) break;
            boolean duplicate = false;
            for (InvoiceDetail d : lines) if (d.getSerialNumber().equalsIgnoreCase(serial)) duplicate = true;
            if (duplicate) { System.out.println("Serial này đã có trong hóa đơn."); continue; }
            try {
                InvoiceDetail detail = salesManager.buildDetail(invoiceId, serial);
                lines.add(detail);
                System.out.printf("+ %s | %s | %,.0f đ%n", detail.getProductId(), detail.getProductName(), detail.getUnitPrice());
            } catch (IllegalArgumentException e) { System.out.println("(!) " + e.getMessage()); }
        }
        if (lines.isEmpty()) { System.out.println("Không tạo hóa đơn rỗng."); return; }
        double subtotal = 0;
        for (InvoiceDetail d : lines) subtotal += d.calculateLineTotal();
        double discount = 0;
        if (!customerId.isEmpty() && customerManager != null) discount = customerManager.getVipDiscount(customerId, subtotal);
        double vatPercent = InputHelper.readDouble("VAT (%), nhập 8 nếu VAT 8%: ", 0, 100);
        salesManager.createInvoice(invoiceId, customerId, staffId, vatPercent / 100.0, discount, lines);
        System.out.println("Đã tạo hóa đơn " + invoiceId + " (UNPAID).");
    }

    private void printInvoice() {
        printInvoiceById(InputHelper.readString("Mã hóa đơn: "));
    }
    private Invoice findUnpaidInvoice() {
        Invoice invoice = salesManager.findInvoice(InputHelper.readString("Mã hóa đơn cần thanh toán: "));
        if (invoice == null || !"UNPAID".equals(invoice.getStatus())) { System.out.println("Không tìm thấy hóa đơn UNPAID."); return null; }
        return invoice;
    }
    private void payCash() {
        Invoice invoice = findUnpaidInvoice(); if (invoice == null) return;
        double total = invoice.calculateFinalTotal();
        double given = InputHelper.readDouble("Cần thu " + String.format("%,.0f", total) + " đ. Tiền khách đưa: ", total);
        CashPayment payment = new CashPayment(salesManager.nextPaymentId(), invoice.getId(), total, "CASH-" + System.currentTimeMillis(), given);
        if (completePayment(invoice, payment)) System.out.printf("Tiền thối: %,.0f đ%n", payment.calculateChange());
    }
    private void payElectronic() {
        Invoice invoice = findUnpaidInvoice(); if (invoice == null) return;
        String bank = InputHelper.readText("Ngân hàng/ví điện tử: ");
        ElectronicPayment payment = new ElectronicPayment(salesManager.nextPaymentId(), invoice.getId(), invoice.calculateFinalTotal(), "QR-" + System.currentTimeMillis(), bank);
        System.out.println("Mã QR tham chiếu (demo): " + payment.generatePaymentQRCode());
        if (InputHelper.readYesNo("Đã xác nhận giao dịch? (Y/N): ")) completePayment(invoice, payment);
        else System.out.println("Chưa xác nhận; hóa đơn vẫn UNPAID.");
    }
    private boolean completePayment(Invoice invoice, Payment payment) {
        if (!salesManager.addPayment(payment)) { System.out.println("Ghi nhận thanh toán thất bại."); return false; }
        if (!invoice.getCustomerId().isEmpty() && customerManager != null) {
            try { customerManager.recordPurchase(invoice.getCustomerId(), payment.getAmount()); }
            catch (IllegalArgumentException e) { System.out.println("(!) Không cộng được chi tiêu khách: " + e.getMessage()); }
        }
        if (employeeManager != null) {
            Employee employee = employeeManager.findById(staffId);
            if (employee instanceof SalesStaff) { ((SalesStaff) employee).addMonthlyRevenue(payment.getAmount()); employeeManager.saveToFile(); }
        }
        System.out.println("Đã ghi nhận thanh toán thành công."); return true;
    }
    private void searchInvoice() {
        Invoice invoice = salesManager.findInvoice(InputHelper.readString("Nhập mã hóa đơn: "));
        if (invoice == null) { System.out.println("Không tìm thấy hóa đơn."); return; }
        System.out.printf("%s | %s | %s | %,.0f đ%n", invoice.getId(), invoice.getCreationDate(),
                invoice.getStatus(), invoice.calculateFinalTotal());
        for (InvoiceDetail d : salesManager.getDetailsByInvoice(invoice.getId()))
            System.out.printf("   - %s | %s | Serial: %s | %,.0f đ%n",
                    d.getProductId(), d.getProductName(), d.getSerialNumber(), d.getUnitPrice());
    }
    private void cancelInvoice() {
        String id = InputHelper.readString("Mã hóa đơn cần hủy: ");
        System.out.println(salesManager.cancelInvoice(id) ? "Đã hủy hóa đơn và trả serial về kho." : "Không hủy được hóa đơn.");
    }
    private void showMyRevenue() {
        LocalDate today = LocalDate.now();
        System.out.printf("Doanh số hôm nay   : %,.0f đ%n", salesManager.revenueByStaffOnDate(staffId, today));
        System.out.printf("Doanh số tháng này : %,.0f đ%n", salesManager.revenueByStaff(staffId, today.getMonthValue(), today.getYear()));
    }
    private void tradeIn() {
        if (!InputHelper.readYesNo("Máy cũ đạt kiểm tra (màn hình, nguồn, iCloud/FRP)? (Y/N): ")) { System.out.println("Máy cũ không đạt, dừng quy trình."); return; }
        String oldSerial = InputHelper.readString("Seri/IMEI máy cũ: ").trim().toUpperCase();
        ProductDetail oldDetail = productManager.findDetailBySerial(oldSerial);
        if (oldDetail != null) { System.out.println("(!) Seri máy cũ đã tồn tại trong dữ liệu kho; cần xác minh trước khi thu."); return; }
        String oldProductId = InputHelper.readString("Mã sản phẩm phân loại máy cũ (PRDxx): ").trim().toUpperCase();
        Product oldProduct = productManager.findByID(oldProductId);
        if (oldProduct == null) { System.out.println("(!) Không có sản phẩm " + oldProductId); return; }
        String newSerial = InputHelper.readString("Seri/IMEI máy mới: ").trim().toUpperCase();
        String invoiceId = salesManager.nextInvoiceId();
        InvoiceDetail newLine = salesManager.buildDetail(invoiceId, newSerial);
        double tradeValue = InputHelper.readDouble("Giá thu máy cũ: ", 1, newLine.calculateLineTotal());
        double vat = InputHelper.readDouble("VAT (%): ", 0, 100) / 100.0;
        String customerId = askCustomerId();
        if (customerId == null) { System.out.println("Đã hủy giao dịch."); return; }
        System.out.printf("Máy mới %,.0f đ - máy cũ %,.0f đ = khách bù %,.0f đ (chưa VAT)%n", newLine.calculateLineTotal(), tradeValue, newLine.calculateLineTotal() - tradeValue);
        if (!InputHelper.readYesNo("Xác nhận đổi? (Y/N): ")) return;
        if (!salesManager.createTradeInInvoice(invoiceId, customerId, staffId, newLine, tradeValue, vat)) return;
        if (!productManager.addTradeInSerial(oldProductId, oldProduct.getName(), oldSerial, tradeValue, staffId)) {
            salesManager.cancelInvoice(invoiceId);
            throw new IllegalStateException("Không lưu được máy cũ vào kho; hóa đơn mới đã được hủy.");
        }
        if (!salesManager.saveTradeIn(invoiceId, oldSerial, tradeValue))
            throw new IllegalStateException("Hóa đơn đã lập nhưng không lưu được lịch sử trade_ins.txt.");
        printInvoiceById(invoiceId);
    }
    private void printInvoiceById(String id) {
        Invoice invoice = salesManager.findInvoice(id);
        if (invoice == null) { System.out.println("Không tìm thấy hóa đơn."); return; }
        invoice.setDetails(salesManager.getDetailsByInvoice(invoice.getId()));
        if (salesManager.isTradeInInvoice(invoice.getId())) invoice.setDiscountLabel("Trừ máy thu cũ");
        invoice.printReceipt();
    }
    // Truyền tài khoản đang đăng nhập: Admin vào qua Quản lý bán hàng vẫn thấy mục ★
    private void openCustomerMenu() {
        new CustomerManagementMenu(customerManager, salesManager, productManager, currentUser).show();
    }
}
