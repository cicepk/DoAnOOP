package com.smartphonestore.model.sale;

import com.smartphonestore.util.DateUtil;
import java.util.ArrayList;
import java.util.List;

public class Invoice implements IPrintable {
    private final String id;
    private final String customerId;
    private final String salesStaffId;
    private final String creationDate;
    private final double totalAmount; // Tong tien truoc VAT
    private String status;
    private final double vatTaxRate;
    private List<InvoiceDetail> details;

    public Invoice(String id, String customerId, String salesStaffId, String creationDate,
                   double totalAmount, String status, double vatTaxRate, List<InvoiceDetail> details) {
        if (blank(id)) throw new IllegalArgumentException("Ma hoa don khong duoc trong.");
        if (totalAmount < 0 || vatTaxRate < 0 || vatTaxRate > 1)
            throw new IllegalArgumentException("Tong tien/VAT khong hop le.");
        this.id = id.trim();
        this.customerId = clean(customerId);
        this.salesStaffId = clean(salesStaffId);
        this.creationDate = blank(creationDate) ? DateUtil.nowText() : creationDate.trim();
        this.totalAmount = totalAmount;
        this.status = blank(status) ? "UNPAID" : status.trim().toUpperCase();
        this.vatTaxRate = vatTaxRate;
        this.details = details == null ? new ArrayList<>() : new ArrayList<>(details);
    }

    private static boolean blank(String s) { return s == null || s.trim().isEmpty(); }
    private static String clean(String s) { return s == null ? "" : s.trim(); }
    public double calculateVatAmount() { return totalAmount * vatTaxRate; }
    public double calculateFinalTotal() { return totalAmount + calculateVatAmount(); }
    public void setStatus(String status) { this.status = status; }
    public void setDetails(List<InvoiceDetail> details) { this.details = details == null ? new ArrayList<>() : new ArrayList<>(details); }

    @Override public void printReceipt() {
        System.out.println("\n================ HOA DON BAN LE ================");
        System.out.printf("Ma HD: %s%nKhach hang: %s%nNhan vien: %s%nNgay lap: %s%n",
                id, customerId.isEmpty() ? "Khach le" : customerId, salesStaffId, creationDate);
        System.out.println("-------------------------------------------------");
        for (InvoiceDetail d : details) {
            System.out.printf("%s | %s | %,.0f VND%n", d.getProductId(), d.getProductName(), d.getUnitPrice());
            System.out.println("  Serial/IMEI: " + d.getSerialNumber());
        }
        System.out.println("-------------------------------------------------");
        System.out.printf("Tien hang: %,.0f VND%nVAT (%.0f%%): %,.0f VND%nTONG THANH TOAN: %,.0f VND%n",
                totalAmount, vatTaxRate * 100, calculateVatAmount(), calculateFinalTotal());
        System.out.println("Trang thai: " + status);
        System.out.println("=================================================");
    }

    // Format: id|customerId|staffId|creationDate|totalAmount|status|vatTaxRate
    public String toFileString() {
        return String.join("|", id, customerId, salesStaffId, creationDate,
                String.format(java.util.Locale.US, "%.0f", totalAmount), status,
                String.format(java.util.Locale.US, "%.4f", vatTaxRate));
    }
    public static Invoice fromFileString(String line) {
        String[] p = line.split("\\|", -1);
        if (p.length != 7) throw new IllegalArgumentException("Dong hoa don phai co 7 cot.");
        return new Invoice(p[0], p[1], p[2], p[3], Double.parseDouble(p[4]), p[5], Double.parseDouble(p[6]), null);
    }

    public String getId() { return id; }
    public String getCustomerId() { return customerId; }
    public String getSalesStaffId() { return salesStaffId; }
    public String getCreationDate() { return creationDate; }
    public double getTotalAmount() { return totalAmount; }
    public String getStatus() { return status; }
    public double getVatTaxRate() { return vatTaxRate; }
}
