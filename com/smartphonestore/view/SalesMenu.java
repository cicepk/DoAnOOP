package com.smartphonestore.view;

import com.smartphonestore.manager.AccountManager;
import com.smartphonestore.manager.CustomerManager;
import com.smartphonestore.manager.EmployeeManager;
import com.smartphonestore.manager.ProductManager;
import com.smartphonestore.manager.SalesManager;
import com.smartphonestore.model.auth.Employee;
import com.smartphonestore.model.auth.SalesStaff;
import com.smartphonestore.model.enums.Role;
import com.smartphonestore.model.sale.CashPayment;
import com.smartphonestore.model.sale.ElectronicPayment;
import com.smartphonestore.model.sale.Invoice;
import com.smartphonestore.model.sale.InvoiceDetail;
import com.smartphonestore.util.InputHelper;
import java.util.ArrayList;
import java.util.List;

/** Menu bán hàng; các manager được tạo một lần tại Main và truyền vào đây. */
public class SalesMenu {
    private final SalesManager salesManager;
    private final CustomerManager customerManager;
    private final EmployeeManager employeeManager;
    private final AccountManager accountManager;
    private final ProductManager productManager;
    private final String staffId;

    public SalesMenu(SalesManager salesManager, CustomerManager customerManager,
                     EmployeeManager employeeManager, String staffId,AccountManager accountManager, ProductManager productManager) {
        if (salesManager == null) throw new IllegalArgumentException("SalesManager không được null.");
        this.salesManager = salesManager;
        this.customerManager = customerManager;
        this.employeeManager = employeeManager;
        this.staffId = staffId == null ? "" : staffId.trim();
        this.accountManager = accountManager;
        this.productManager = productManager;
    }

    public void show() {
        boolean running = true;
        while (running) {
            System.out.println("\n========== MENU NHÂN VIÊN BÁN HÀNG ==========");
            System.out.println("[1] Xem danh sách hóa đơn");
            System.out.println("[2] Lập hóa đơn bán lẻ mới");
            System.out.println("[3] Xem chi tiết hóa đơn");
            System.out.println("[4] In hóa đơn VAT");
            System.out.println("[5] Thanh toán tiền mặt");
            System.out.println("[6] Thanh toán chuyển khoản / QR");
            System.out.println("[7] Tìm hóa đơn theo mã");
            System.out.println("[8] Hủy hóa đơn chưa thanh toán");
            System.out.println("[9] Thống kê doanh thu hôm nay");
            System.out.println("[10] Quản lý khách hàng (TV4)");
            System.out.println("[11] Đăng xuất");
            System.out.println("[0] Thoát chương trình");
            int choice = InputHelper.readInt("Chọn: ", 0, 11);
            try {
                switch (choice) {
                    case 1: showAllInvoices(); break;
                    case 2: createInvoice(); break;
                    case 3: showInvoiceDetails(); break;
                    case 4: printInvoice(); break;
                    case 5: payCash(); break;
                    case 6: payElectronic(); break;
                    case 7: searchInvoice(); break;
                    case 8: cancelInvoice(); break;
                    case 9: System.out.printf("Doanh thu đã thanh toán hôm nay: %,.0f VND%n", salesManager.revenueToday()); break;
                    case 10: openCustomerMenu(); break;
                    case 11: running = false; break;
                    
                    case 0: System.out.println("Kết thúc chương trình."); System.exit(0); break;
                    default: break;
                }
            } catch (RuntimeException e) {
                System.out.println("(!) " + e.getMessage());
            }
        }
    }

    private void showAllInvoices() {
        List<Invoice> invoices = salesManager.getAllInvoices();
        if (invoices.isEmpty()) { System.out.println("Chưa có hóa đơn."); return; }
        for (Invoice invoice : invoices) {
            System.out.printf("%-8s | %-8s | %-8s | %-20s | %,.0f VND | %s%n",
                    invoice.getId(), invoice.getCustomerId(), invoice.getSalesStaffId(),
                    invoice.getCreationDate(), invoice.calculateFinalTotal(), invoice.getStatus());
        }
    }

    private void createInvoice() {
        String invoiceId = salesManager.nextInvoiceId();
        String customerId = InputHelper.readString("Mã khách hàng (nhập KHACHLE nếu khách lẻ): ")
                .trim().toUpperCase();
        if ("KHACHLE".equals(customerId)) {
            customerId = "";
        } else if (customerManager == null || customerManager.findById(customerId) == null) {
            System.out.println("(!) Không tìm thấy khách hàng " + customerId
                    + ". Tạo hồ sơ khách trước hoặc chọn KHACHLE.");
            return;
        }
        List<InvoiceDetail> lines = new ArrayList<>();
        System.out.println("Quét/nhập từng Serial/IMEI. Nhập 0 để kết thúc.");
        while (true) {
            String serial = InputHelper.readString("Serial/IMEI: ");
            if ("0".equals(serial)) break;
            boolean duplicate = false;
            for (InvoiceDetail detail : lines)
                if (detail.getSerialNumber().equalsIgnoreCase(serial)) duplicate = true;
            if (duplicate) { System.out.println("Serial này đã có trong hóa đơn."); continue; }
            try {
                InvoiceDetail detail = salesManager.buildDetail(invoiceId, serial);
                lines.add(detail);
                System.out.printf("+ %s | %s | %,.0f VND%n", detail.getProductId(), detail.getProductName(), detail.getUnitPrice());
            } catch (IllegalArgumentException e) {
                System.out.println("(!) " + e.getMessage());
            }
        }
        if (lines.isEmpty()) { System.out.println("Không tạo hóa đơn rỗng."); return; }

        // [TC07] Tính ưu đãi VIP: trước đây không gọi getVipDiscount nên khách VIP trả đủ giá.
        // getVipDiscount (TV4) trả về 0 cho khách thường; khách lẻ (mã rỗng) thì bỏ qua.
        double subtotal = 0;
        for (InvoiceDetail detail : lines) subtotal += detail.calculateLineTotal();
        double discount = 0;
        if (!customerId.isEmpty() && customerManager != null) {
            discount = customerManager.getVipDiscount(customerId, subtotal);
            if (discount > 0) System.out.printf("Khách VIP: giảm %,.0f VND trên tiền hàng.%n", discount);
        }

        double vatPercent = InputHelper.readDouble("VAT (%), nhập 8 nếu VAT 8%: ", 0);
        if (vatPercent > 100) throw new IllegalArgumentException("VAT không được vượt quá 100%.");
        // [TC07] Gọi hàm createInvoice mới có tham số giảm giá
        salesManager.createInvoice(invoiceId, customerId, staffId, vatPercent / 100.0, discount, lines);
        System.out.println("Đã tạo hóa đơn " + invoiceId + " (UNPAID).");
    }

    private void showInvoiceDetails() {
        String id = InputHelper.readString("Mã hóa đơn: ");
        List<InvoiceDetail> details = salesManager.getDetailsByInvoice(id);
        if (details.isEmpty()) { System.out.println("Không có chi tiết hóa đơn."); return; }
        for (InvoiceDetail detail : details) {
            System.out.printf("%s | %s | Serial: %s | %,.0f VND%n", detail.getProductId(),
                    detail.getProductName(), detail.getSerialNumber(), detail.getUnitPrice());
        }
    }

    private void printInvoice() {
        Invoice invoice = salesManager.findInvoice(InputHelper.readString("Mã hóa đơn: "));
        if (invoice == null) { System.out.println("Không tìm thấy hóa đơn."); return; }
        invoice.setDetails(salesManager.getDetailsByInvoice(invoice.getId()));
        invoice.printReceipt();
    }

    private Invoice findUnpaidInvoice() {
        Invoice invoice = salesManager.findInvoice(InputHelper.readString("Mã hóa đơn cần thanh toán: "));
        if (invoice == null || !"UNPAID".equals(invoice.getStatus())) {
            System.out.println("Không tìm thấy hóa đơn UNPAID."); return null;
        }
        return invoice;
    }

    private void payCash() {
        Invoice invoice = findUnpaidInvoice(); if (invoice == null) return;
        double total = invoice.calculateFinalTotal();
        double given = InputHelper.readDouble("Cần thu " + String.format("%,.0f", total) + " VND. Tiền khách đưa: ", total);
        CashPayment payment = new CashPayment(salesManager.nextPaymentId(), invoice.getId(), total,
                "CASH-" + System.currentTimeMillis(), given);
        if (completePayment(invoice, payment))
            System.out.printf("Thanh toán thành công. Tiền thối: %,.0f VND%n", payment.calculateChange());
    }

    private void payElectronic() {
        Invoice invoice = findUnpaidInvoice(); if (invoice == null) return;
        String bank = InputHelper.readString("Ngân hàng/ví điện tử: ");
        ElectronicPayment payment = new ElectronicPayment(salesManager.nextPaymentId(), invoice.getId(),
                invoice.calculateFinalTotal(), "QR-" + System.currentTimeMillis(), bank);
        System.out.println("Chuỗi QR tham chiếu (demo): " + payment.generatePaymentQRCode());
        boolean confirmed = InputHelper.readYesNo("Đã xác nhận giao dịch? (Y/N): ");
        if (confirmed) completePayment(invoice, payment);
        else System.out.println("Chưa xác nhận; hóa đơn vẫn UNPAID.");
    }

    private boolean completePayment(Invoice invoice, com.smartphonestore.model.sale.Payment payment) {
        if (!salesManager.addPayment(payment)) { System.out.println("Ghi nhận thanh toán thất bại."); return false; }
        double paidTotal = payment.getAmount();
        if (!invoice.getCustomerId().isEmpty() && customerManager != null)
            customerManager.recordPurchase(invoice.getCustomerId(), paidTotal);
        if (employeeManager != null) {
            Employee employee = employeeManager.findById(staffId);
            if (employee instanceof SalesStaff) {
                ((SalesStaff) employee).addMonthlyRevenue(paidTotal);
                employeeManager.saveToFile();
            }
        }
        System.out.println("Đã ghi nhận thanh toán thành công.");
        return true;
    }

    private void searchInvoice() {
        Invoice invoice = salesManager.findInvoice(InputHelper.readString("Nhập mã hóa đơn: "));
        if (invoice == null) System.out.println("Không tìm thấy hóa đơn.");
        else System.out.printf("%s | %s | %,.0f VND%n", invoice.getId(), invoice.getStatus(), invoice.calculateFinalTotal());
    }
    private void cancelInvoice() {
        String id = InputHelper.readString("Mã hóa đơn cần hủy: ");
        System.out.println(salesManager.cancelInvoice(id) ? "Đã hủy hóa đơn và trả serial về kho." : "Không hủy được hóa đơn.");
    }
    // ===================== [10] KHÁCH HÀNG (TV4) =====================

    // Mở menu Quản lý khách hàng của TV4 với quyền ADMIN (được xóa phiếu bảo hành).
    // Chọn [0] trong menu đó sẽ quay lại Menu Admin.
    private void openCustomerMenu() {
        new CustomerManagementMenu(customerManager, accountManager, salesManager,
                                   productManager, Role.SALES).show();
    }
}
