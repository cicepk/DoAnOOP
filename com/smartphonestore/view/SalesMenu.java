package com.smartphonestore.view;

import com.smartphonestore.manager.CustomerManager;
import com.smartphonestore.manager.EmployeeManager;
import com.smartphonestore.manager.SalesManager;
import com.smartphonestore.model.auth.Employee;
import com.smartphonestore.model.auth.SalesStaff;
import com.smartphonestore.model.sale.CashPayment;
import com.smartphonestore.model.sale.ElectronicPayment;
import com.smartphonestore.model.sale.Invoice;
import com.smartphonestore.model.sale.InvoiceDetail;
import com.smartphonestore.util.InputHelper;
import com.smartphonestore.model.enums.Role;
import com.smartphonestore.manager.AccountManager;
import com.smartphonestore.manager.ProductManager;

import java.util.ArrayList;
import java.util.List;

/** Menu ban hang; cac manager duoc tao mot lan tai Main va truyen vao day. */
public class SalesMenu {
    private final SalesManager salesManager;
    private final CustomerManager customerManager;
    private final EmployeeManager employeeManager;
    private final AccountManager accountManager;
    private final ProductManager productManager;
    private final String staffId;

    public SalesMenu(SalesManager salesManager, CustomerManager customerManager,
                     EmployeeManager employeeManager, String staffId,AccountManager accountManager, ProductManager productManager) {
        if (salesManager == null) throw new IllegalArgumentException("SalesManager khong duoc null.");
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
            System.out.println("\n========== MENU NHAN VIEN BAN HANG ==========");
            System.out.println("[1] Xem danh sach hoa don");
            System.out.println("[2] Lap hoa don ban le moi");
            System.out.println("[3] Xem chi tiet hoa don");
            System.out.println("[4] In hoa don VAT");
            System.out.println("[5] Thanh toan tien mat");
            System.out.println("[6] Thanh toan chuyen khoan / QR");
            System.out.println("[7] Tim hoa don theo ma");
            System.out.println("[8] Huy hoa don chua thanh toan");
            System.out.println("[9] Thong ke doanh thu hom nay");
            System.out.println("[10] Quản lý khách hàng (TV4)");
            System.out.println("[0] Thoat chuong trinh");
            int choice = InputHelper.readInt("Chon: ", 0, 10);
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
                    case 9: System.out.printf("Doanh thu da thanh toan hom nay: %,.0f VND%n", salesManager.revenueToday()); break;
                    case 10: openCustomerMenu(); break;
                    case 11: running = false; break;
                    
                    case 0: System.out.println("Ket thuc chuong trinh."); System.exit(0); break;
                    default: break;
                }
            } catch (RuntimeException e) {
                System.out.println("(!) " + e.getMessage());
            }
        }
    }

    private void showAllInvoices() {
        List<Invoice> invoices = salesManager.getAllInvoices();
        if (invoices.isEmpty()) { System.out.println("Chua co hoa don."); return; }
        for (Invoice invoice : invoices) {
            System.out.printf("%-8s | %-8s | %-8s | %-20s | %,.0f VND | %s%n",
                    invoice.getId(), invoice.getCustomerId(), invoice.getSalesStaffId(),
                    invoice.getCreationDate(), invoice.calculateFinalTotal(), invoice.getStatus());
        }
    }

    private void createInvoice() {
        String invoiceId = salesManager.nextInvoiceId();
        String customerId = InputHelper.readString("Ma khach hang (nhap KHACHLE neu khach le): ")
                .trim().toUpperCase();
        if ("KHACHLE".equals(customerId)) {
            customerId = "";
        } else if (customerManager == null || customerManager.findById(customerId) == null) {
            System.out.println("(!) Khong tim thay khach hang " + customerId
                    + ". Tao ho so khach truoc hoac chon KHACHLE.");
            return;
        }
        List<InvoiceDetail> lines = new ArrayList<>();
        System.out.println("Quet/nhap tung Serial/IMEI. Nhap 0 de ket thuc.");
        while (true) {
            String serial = InputHelper.readString("Serial/IMEI: ");
            if ("0".equals(serial)) break;
            boolean duplicate = false;
            for (InvoiceDetail detail : lines)
                if (detail.getSerialNumber().equalsIgnoreCase(serial)) duplicate = true;
            if (duplicate) { System.out.println("Serial nay da co trong hoa don."); continue; }
            try {
                InvoiceDetail detail = salesManager.buildDetail(invoiceId, serial);
                lines.add(detail);
                System.out.printf("+ %s | %s | %,.0f VND%n", detail.getProductId(), detail.getProductName(), detail.getUnitPrice());
            } catch (IllegalArgumentException e) {
                System.out.println("(!) " + e.getMessage());
            }
        }
        if (lines.isEmpty()) { System.out.println("Khong tao hoa don rong."); return; }

        // [TC07] Tính ưu đãi VIP: trước đây không gọi getVipDiscount nên khách VIP trả đủ giá.
        // getVipDiscount (TV4) trả về 0 cho khách thường; khách lẻ (mã rỗng) thì bỏ qua.
        double subtotal = 0;
        for (InvoiceDetail detail : lines) subtotal += detail.calculateLineTotal();
        double discount = 0;
        if (!customerId.isEmpty() && customerManager != null) {
            discount = customerManager.getVipDiscount(customerId, subtotal);
            if (discount > 0) System.out.printf("Khach VIP: giam %,.0f VND tren tien hang.%n", discount);
        }

        double vatPercent = InputHelper.readDouble("VAT (%), nhap 8 neu VAT 8%: ", 0);
        if (vatPercent > 100) throw new IllegalArgumentException("VAT khong duoc vuot qua 100%.");
        // [TC07] Gọi hàm createInvoice mới có tham số giảm giá
        salesManager.createInvoice(invoiceId, customerId, staffId, vatPercent / 100.0, discount, lines);
        System.out.println("Da tao hoa don " + invoiceId + " (UNPAID).");
    }

    private void showInvoiceDetails() {
        String id = InputHelper.readString("Ma hoa don: ");
        List<InvoiceDetail> details = salesManager.getDetailsByInvoice(id);
        if (details.isEmpty()) { System.out.println("Khong co chi tiet hoa don."); return; }
        for (InvoiceDetail detail : details) {
            System.out.printf("%s | %s | Serial: %s | %,.0f VND%n", detail.getProductId(),
                    detail.getProductName(), detail.getSerialNumber(), detail.getUnitPrice());
        }
    }

    private void printInvoice() {
        Invoice invoice = salesManager.findInvoice(InputHelper.readString("Ma hoa don: "));
        if (invoice == null) { System.out.println("Khong tim thay hoa don."); return; }
        invoice.setDetails(salesManager.getDetailsByInvoice(invoice.getId()));
        invoice.printReceipt();
    }

    private Invoice findUnpaidInvoice() {
        Invoice invoice = salesManager.findInvoice(InputHelper.readString("Ma hoa don can thanh toan: "));
        if (invoice == null || !"UNPAID".equals(invoice.getStatus())) {
            System.out.println("Khong tim thay hoa don UNPAID."); return null;
        }
        return invoice;
    }

    private void payCash() {
        Invoice invoice = findUnpaidInvoice(); if (invoice == null) return;
        double total = invoice.calculateFinalTotal();
        double given = InputHelper.readDouble("Can thu " + String.format("%,.0f", total) + " VND. Tien khach dua: ", total);
        CashPayment payment = new CashPayment(salesManager.nextPaymentId(), invoice.getId(), total,
                "CASH-" + System.currentTimeMillis(), given);
        if (completePayment(invoice, payment))
            System.out.printf("Thanh toan thanh cong. Tien thoi: %,.0f VND%n", payment.calculateChange());
    }

    private void payElectronic() {
        Invoice invoice = findUnpaidInvoice(); if (invoice == null) return;
        String bank = InputHelper.readString("Ngan hang/vi dien tu: ");
        ElectronicPayment payment = new ElectronicPayment(salesManager.nextPaymentId(), invoice.getId(),
                invoice.calculateFinalTotal(), "QR-" + System.currentTimeMillis(), bank);
        System.out.println("Chuoi QR tham chieu (demo): " + payment.generatePaymentQRCode());
        boolean confirmed = InputHelper.readYesNo("Da xac nhan giao dich? (Y/N): ");
        if (confirmed) completePayment(invoice, payment);
        else System.out.println("Chua xac nhan; hoa don van UNPAID.");
    }

    private boolean completePayment(Invoice invoice, com.smartphonestore.model.sale.Payment payment) {
        if (!salesManager.addPayment(payment)) { System.out.println("Ghi nhan thanh toan that bai."); return false; }
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
        System.out.println("Da ghi nhan thanh toan thanh cong.");
        return true;
    }

    private void searchInvoice() {
        Invoice invoice = salesManager.findInvoice(InputHelper.readString("Nhap ma hoa don: "));
        if (invoice == null) System.out.println("Khong tim thay hoa don.");
        else System.out.printf("%s | %s | %,.0f VND%n", invoice.getId(), invoice.getStatus(), invoice.calculateFinalTotal());
    }
    private void cancelInvoice() {
        String id = InputHelper.readString("Ma hoa don can huy: ");
        System.out.println(salesManager.cancelInvoice(id) ? "Da huy hoa don va tra serial ve kho." : "Khong huy duoc hoa don.");
    }
    // ===================== [10] KHÁCH HÀNG (TV4) =====================

    // Mở menu Quản lý khách hàng của TV4 với quyền ADMIN (được xóa phiếu bảo hành).
    // Chọn [0] trong menu đó sẽ quay lại Menu Admin.
    private void openCustomerMenu() {
        new CustomerManagementMenu(customerManager, accountManager, salesManager,
                                   productManager, Role.SALES).show();
    }
}
