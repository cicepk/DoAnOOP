package com.smartphonestore.model.sale;

/** Moi dong chi tiet la mot thiet bi vat ly va bat buoc co mot serial/IMEI. */
public class InvoiceDetail {
    private final String invoiceId;
    private final String productId;
    private final String productName;
    private final String serialNumber;
    private final double unitPrice;
    private final int quantity = 1;

    public InvoiceDetail(String invoiceId, String productId, String productName,
                         String serialNumber, double unitPrice) {
        if (blank(invoiceId) || blank(productId) || blank(productName))
            throw new IllegalArgumentException("Mã hóa đơn, mã sản phẩm và tên sản phẩm không được trống.");
        if (blank(serialNumber) || serialNumber.contains("|"))
            throw new IllegalArgumentException("Serial/IMEI không hợp lệ.");
        if (unitPrice < 0) throw new IllegalArgumentException("Đơn giá không được âm.");
        this.invoiceId = invoiceId.trim();
        this.productId = productId.trim().toUpperCase();
        this.productName = productName.trim().replace('|', '/');
        this.serialNumber = serialNumber.trim().toUpperCase();
        this.unitPrice = unitPrice;
    }

    private static boolean blank(String value) { return value == null || value.trim().isEmpty(); }
    public double calculateLineTotal() { return unitPrice; }

    // Format: invoiceId|productId|productName|serialNumber|unitPrice|quantity
    public String toFileString() {
        return String.join("|", invoiceId, productId, productName, serialNumber,
                String.format(java.util.Locale.US, "%.0f", unitPrice), "1");
    }

    public static InvoiceDetail fromFileString(String line) {
        String[] p = line.split("\\|", -1);
        if (p.length != 6 || !"1".equals(p[5].trim()))
            throw new IllegalArgumentException("Chi tiết hóa đơn phải có 6 cột và quantity = 1.");
        return new InvoiceDetail(p[0], p[1], p[2], p[3], Double.parseDouble(p[4]));
    }

    public String getInvoiceId() { return invoiceId; }
    public String getProductId() { return productId; }
    public String getProductName() { return productName; }
    public String getSerialNumber() { return serialNumber; }
    public double getUnitPrice() { return unitPrice; }
    public int getQuantity() { return quantity; }
}
