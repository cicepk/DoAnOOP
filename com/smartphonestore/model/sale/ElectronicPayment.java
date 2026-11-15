package com.smartphonestore.model.sale;

public class ElectronicPayment extends Payment {
    private final String bankOrWalletName;
    public ElectronicPayment(String paymentId, String invoiceId, double amount, String transactionCode, String bankOrWalletName) {
        super(paymentId, invoiceId, amount, transactionCode);
        if (bankOrWalletName == null || bankOrWalletName.trim().isEmpty())
            throw new IllegalArgumentException("Tên ngân hàng/ví điện tử không được trống.");
        if (bankOrWalletName.contains("|"))
            throw new IllegalArgumentException("Tên ngân hàng/ví điện tử không được chứa ký tự '|'.");
        this.bankOrWalletName = bankOrWalletName.trim();
    }
    // Chuoi tham chieu demo, khong phai QR ngan hang that.
    public String generatePaymentQRCode() {
        return "PAY|" + invoiceId + "|" + String.format(java.util.Locale.US, "%.0f", amount)
                + "|" + bankOrWalletName + "|" + transactionCode;
    }
    @Override public String getMethod() { return "ELECTRONIC"; }
    @Override public String getExtraData() { return bankOrWalletName; }
}