package com.smartphonestore.model.sale;

public class CashPayment extends Payment {
    private final double cashGiven;
    public CashPayment(String paymentId, String invoiceId, double amount, String transactionCode, double cashGiven) {
        super(paymentId, invoiceId, amount, transactionCode);
        if (cashGiven < amount) throw new IllegalArgumentException("Tiền khách đưa chưa đủ.");
        this.cashGiven = cashGiven;
    }
    public double calculateChange() { return cashGiven - amount; }
    @Override public String getMethod() { return "CASH"; }
    @Override public String getExtraData() { return String.format(java.util.Locale.US, "%.0f", cashGiven); }
}
