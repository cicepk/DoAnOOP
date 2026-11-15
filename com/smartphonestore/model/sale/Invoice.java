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
    private final double discountAmount; // Tiền giảm giá VIP trên tiền hàng (khách lẻ / khách thường = 0)
    
    // Constructor vẫn gọi đc
    public Invoice(String id, String customerId, String salesStaffId, String creationDate,
                   double totalAmount, String status, double vatTaxRate, List<InvoiceDetail> details) {
        this(id, customerId, salesStaffId, creationDate, totalAmount, status, vatTaxRate, 0, details);
    }
    // Constructor mới thêm tiền giảm giá VIP
    public Invoice(String id, String customerId, String salesStaffId, String creationDate,
               double totalAmount, String status, double vatTaxRate, double discountAmount,
               List<InvoiceDetail> details) {
        if (blank(id)) throw new IllegalArgumentException("Mã hóa đơn không được trống.");
            if (totalAmount < 0 || vatTaxRate < 0 || vatTaxRate > 1)
                throw new IllegalArgumentException("Tổng tiền/VAT không hợp lệ.");
            this.id = id.trim();
            this.customerId = clean(customerId);
            this.salesStaffId = clean(salesStaffId);
            this.creationDate = blank(creationDate) ? DateUtil.nowText() : creationDate.trim();
            this.totalAmount = totalAmount;
            this.status = blank(status) ? "UNPAID" : status.trim().toUpperCase();
            this.vatTaxRate = vatTaxRate;
            this.details = details == null ? new ArrayList<>() : new ArrayList<>(details);
        if (discountAmount < 0 || discountAmount > totalAmount)
            throw new IllegalArgumentException("Tiền giảm giá không hợp lệ.");
        this.discountAmount = discountAmount;
    }
    private static boolean blank(String s) { return s == null || s.trim().isEmpty(); }
    private static String clean(String s) { return s == null ? "" : s.trim(); }
    //VAT tính trên tiền hàng SAU khi trừ giảm giá VIP (trước đây tính trên giá gốc)
    public double calculateVatAmount() { return (totalAmount - discountAmount) * vatTaxRate; }
    public double calculateFinalTotal() { return totalAmount - discountAmount + calculateVatAmount(); }
    public void setStatus(String status) { this.status = status; }
    public void setDetails(List<InvoiceDetail> details) { this.details = details == null ? new ArrayList<>() : new ArrayList<>(details); }

    @Override public void printReceipt() {
        System.out.println("\n================ HÓA ĐƠN BÁN LẺ ================");
        System.out.printf("Mã HĐ: %s%nKhách hàng: %s%nNhân viên: %s%nNgày lập: %s%n",
                id, customerId.isEmpty() ? "Khách lẻ" : customerId, salesStaffId, creationDate);
        System.out.println("-------------------------------------------------");
        for (InvoiceDetail d : details) {
            System.out.printf("%s | %s | %,.0f VND%n", d.getProductId(), d.getProductName(), d.getUnitPrice());
            System.out.println("  Serial/IMEI: " + d.getSerialNumber());
        }
        System.out.printf("Tiền hàng: %,.0f VND%n", totalAmount);
        //Chỉ in dòng giảm giá khi khách được giảm
        if (discountAmount > 0) System.out.printf("Giảm giá VIP: -%,.0f VND%n", discountAmount);
        System.out.printf("VAT (%.0f%%): %,.0f VND%nTỔNG THANH TOÁN: %,.0f VND%n",
            vatTaxRate * 100, calculateVatAmount(), calculateFinalTotal());
        System.out.println("Trạng thái: " + status);
        System.out.println("=================================================");
    }

    // Format: id|customerId|staffId|creationDate|totalAmount|status|vatTaxRate|discountAmount
    public String toFileString() {
        return String.join("|", id, customerId, salesStaffId, creationDate,
                String.format(java.util.Locale.US, "%.0f", totalAmount), status,
                String.format(java.util.Locale.US, "%.4f", vatTaxRate),
                String.format(java.util.Locale.US, "%.0f", discountAmount));
    }
    public static Invoice fromFileString(String line) {
        String[] p = line.split("\\|", -1);
        if (p.length != 7 && p.length != 8) throw new IllegalArgumentException("Dòng hóa đơn phải có 7 cột hoặc 8 cột.");
        double discount = p.length == 8 ? Double.parseDouble(p[7]) : 0;   // dòng cũ: không giảm
        return new Invoice(p[0], p[1], p[2], p[3], Double.parseDouble(p[4]), p[5], Double.parseDouble(p[6]),discount,null);
    }

    public String getId() { return id; }
    public String getCustomerId() { return customerId; }
    public String getSalesStaffId() { return salesStaffId; }
    public String getCreationDate() { return creationDate; }
    public double getTotalAmount() { return totalAmount; }
    public String getStatus() { return status; }
    public double getVatTaxRate() { return vatTaxRate; }
    public double getDiscountAmount() { return discountAmount; }
}