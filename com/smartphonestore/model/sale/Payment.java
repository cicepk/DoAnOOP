package com.smartphonestore.model.sale;

import com.smartphonestore.util.DateUtil;

public abstract class Payment {
    protected final String paymentId;
    protected final String invoiceId;
    protected final double amount;
    protected final String paymentDate;
    protected final String transactionCode;
    protected boolean completed;

    public Payment(String paymentId, String invoiceId, double amount, String transactionCode) {
        if (blank(paymentId) || blank(invoiceId)) throw new IllegalArgumentException("Ma thanh toan/hoa don khong duoc trong.");
        if (amount < 0) throw new IllegalArgumentException("So tien thanh toan khong hop le.");
        this.paymentId = paymentId.trim();
        this.invoiceId = invoiceId.trim();
        this.amount = amount;
        this.transactionCode = transactionCode == null ? "" : transactionCode.trim();
        this.paymentDate = DateUtil.nowText();
        this.completed = false;
    }
    private static boolean blank(String s) { return s == null || s.trim().isEmpty(); }
    public void complete() { completed = true; }
    public boolean verifyPaymentStatus() { return completed; }
    public abstract String getMethod();
    public abstract String getExtraData();

    // Format 7 cot: id|invoiceId|amount|paymentDate|method|transactionCode|extraData
    public String toFileString() {
        return String.join("|", paymentId, invoiceId,
                String.format(java.util.Locale.US, "%.0f", amount), paymentDate,
                getMethod(), transactionCode.replace('|', '/'), getExtraData().replace('|', '/'));
    }
    public String getInvoiceId() { return invoiceId; }
    public double getAmount() { return amount; }
    public String getPaymentId() { return paymentId; }
}
