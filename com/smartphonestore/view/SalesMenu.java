package com.smartphonestore.view;

import com.smartphonestore.manager.*;
import com.smartphonestore.model.auth.Account;
import com.smartphonestore.model.product.SaleProduct;
import com.smartphonestore.model.sale.*;
import java.time.LocalDate;
import java.util.*;

public class SalesMenu {
    private final SalesManager salesManager;
    private final ProductGateway productGateway;
    private final Account currentUser;
    private final Scanner scanner = new Scanner(System.in);

    public SalesMenu(SalesManager salesManager, ProductGateway productGateway, Account currentUser) {
        this.salesManager = Objects.requireNonNull(salesManager);
        this.productGateway = Objects.requireNonNull(productGateway);
        this.currentUser = Objects.requireNonNull(currentUser);
    }
    public void displayMenu() {
        while (true) {
            System.out.println("\\n===== MENU BÁN HÀNG =====");
            System.out.println("1. Bán hàng");
            System.out.println("2. Tra cứu");
            System.out.println("3. Chăm sóc khách hàng ");
            System.out.println("4. Doanh số của tôi");
            System.out.println("5. Thu cũ đổi mới");
            System.out.println("6. Đổi mật khẩu (tích hợp ChangePasswordView của TV1)");
            System.out.println("0. Đăng xuất");
            int choice = readInt("Chọn: ", 0, 6);
            switch (choice) {
                case 1 -> salesMenu();
                case 2 -> lookupMenu();
                case 3 -> System.out.println("Tích hợp CustomerManagementMenu của TV4 tại đây.");
                case 4 -> showMyRevenue();
                case 5 -> tradeInMenu();
                case 6 -> System.out.println("Gọi ChangePasswordView của TV1 tại đây.");
                case 0 -> { return; }
            }
        }
    }
    private void salesMenu() {
        while (true) {
            System.out.println("\\n--- BÁN HÀNG ---\\n1. Lập hóa đơn\\n2. Thanh toán\\n3. In hóa đơn\\n4. Hủy hóa đơn chưa thanh toán\\n0. Quay lại");
            int c = readInt("Chọn: ", 0, 4);
            switch (c) {
                case 1 -> createInvoice();
                case 2 -> payInvoice();
                case 3 -> printInvoice();
                case 4 -> cancelInvoice();
                case 0 -> { return; }
            }
        }
    }
    private void createInvoice() {
        try {
            String customer = readLine("Mã khách/SĐT (Enter = KHACHLE): ");
            Map<String,Integer> items = new LinkedHashMap<>();
            while (true) {
                String id = readLine("Mã sản phẩm (0 để kết thúc): ");
                if (id.equals("0")) break;
                int qty = readInt("Số lượng: ", 1, 9999);
                items.merge(id, qty, Integer::sum);
            }
            Invoice invoice = salesManager.createInvoice(currentUser.getId(), customer, items);
            System.out.println("Đã tạo hóa đơn " + invoice.getId() + " - Tổng: " + money(invoice.getTotal()));
        } catch (RuntimeException ex) { System.out.println("Lỗi: " + ex.getMessage()); }
    }
    private void payInvoice() {
        String id = readLine("Mã hóa đơn: ");
        System.out.println("1. Tiền mặt  2. Chuyển khoản");
        int m = readInt("Phương thức: ", 1, 2);
        boolean ok = salesManager.payInvoice(id, m == 1 ? PaymentMethod.CASH : PaymentMethod.BANK_TRANSFER);
        System.out.println(ok ? "Thanh toán thành công." : "Không thể thanh toán hóa đơn.");
    }
    private void printInvoice() {
        Invoice i = salesManager.findInvoice(readLine("Mã hóa đơn: "));
        if (i == null) { System.out.println("Không tìm thấy hóa đơn."); return; }
        System.out.println("\\nHÓA ĐƠN: " + i.getId() + " | NV: " + i.getStaffId() + " | KH: " + i.getCustomerId());
        i.getDetails().forEach(d -> System.out.printf("%-20s x%-3d %12s%n", d.getProductName(), d.getQuantity(), money(d.getLineTotal())));
        System.out.println("Giảm trừ: " + money(i.getDiscountAmount()));
        System.out.println("TỔNG: " + money(i.getTotal()) + " | " + i.getStatus());
    }
    private void cancelInvoice() {
        boolean ok = salesManager.cancelUnpaidInvoice(readLine("Mã hóa đơn: "));
        System.out.println(ok ? "Đã hủy hóa đơn." : "Chỉ hủy được hóa đơn chưa thanh toán.");
    }
    private void lookupMenu() {
        while (true) {
            System.out.println("\\n--- TRA CỨU ---\\n1. Hóa đơn\\n2. Tồn kho (chỉ xem)\\n0. Quay lại");
            int c = readInt("Chọn: ", 0, 2);
            if (c == 0) return;
            if (c == 1) printInvoice();
            if (c == 2) {
                String keyword = readLine("Tên/mã sản phẩm: ");
                List<SaleProduct> found = productGateway.findByNameOrBrand(keyword);
                found.forEach(p -> System.out.printf("%s | %s | Giá: %s | Còn: %d%n",
                        p.getId(), p.getName(), money(p.getSalePrice()), p.getStockQuantity()));
            }
        }
    }
    private void showMyRevenue() {
        LocalDate today = LocalDate.now();
        System.out.println("Doanh thu hôm nay: " + money(salesManager.revenueByStaff(currentUser.getId(), today.getMonthValue(), today.getYear())));
        System.out.println("Doanh thu tháng này: " + money(salesManager.revenueByStaff(currentUser.getId(), today.getMonthValue(), today.getYear())));
    }
    private void tradeInMenu() {
        try {
            if (!readLine("Máy cũ đạt kiểm tra? (Y/N): ").equalsIgnoreCase("Y")) {
                System.out.println("Đã dừng quy trình thu cũ đổi mới."); return;
            }
            String oldSerial = readLine("Serial máy cũ: ");
            String customer = readLine("Mã khách/SĐT: ");
            String product = readLine("Mã máy mới: ");
            int qty = readInt("Số lượng máy mới: ", 1, 99);
            double value = readDouble("Giá thu máy cũ: ", 1);
            Invoice i = salesManager.createTradeInInvoice(currentUser.getId(), customer, oldSerial, product, qty, value);
            System.out.println("Đổi mới thành công. Hóa đơn " + i.getId() + ", cần thanh toán: " + money(i.getTotal()));
            printInvoiceById(i);
        } catch (RuntimeException ex) { System.out.println("Lỗi thu cũ đổi mới: " + ex.getMessage()); }
    }
    private void printInvoiceById(Invoice i) {
        System.out.println("Trừ máy thu cũ: " + money(i.getDiscountAmount()) + " | Tổng: " + money(i.getTotal()));
    }
    private int readInt(String prompt, int min, int max) {
        while (true) try { System.out.print(prompt); int n = Integer.parseInt(scanner.nextLine().trim()); if (n >= min && n <= max) return n; }
        catch (NumberFormatException ignored) {}
        System.out.println("Vui lòng nhập số trong khoảng " + min + "-" + max);
    }
    private double readDouble(String prompt, double min) {
        while (true) try { System.out.print(prompt); double n = Double.parseDouble(scanner.nextLine().trim()); if (n >= min) return n; }
        catch (NumberFormatException ignored) {}
        System.out.println("Vui lòng nhập số >= " + min);
    }
    private String readLine(String prompt) { System.out.print(prompt); return scanner.nextLine().trim(); }
    private String money(double value) { return String.format("%,.0f đ", value); }
}
