package com.smartphonestore.model.sale;

public class InvoiceDetail {
    private final String productId, productName, serial;
    private final int quantity;
    private final double unitPrice;

    public InvoiceDetail(String productId, String productName, String serial, int quantity, double unitPrice) {
        if (quantity <= 0 || unitPrice < 0) throw new IllegalArgumentException("Chi tiết hóa đơn không hợp lệ");
        this.productId = productId; this.productName = productName; this.serial = serial;
        this.quantity = quantity; this.unitPrice = unitPrice;
    }
    public String getProductId() { return productId; }
    public String getProductName() { return productName; }
    public String getSerial() { return serial; }
    public int getQuantity() { return quantity; }
    public double getUnitPrice() { return unitPrice; }
    public double getLineTotal() { return quantity * unitPrice; }
}
