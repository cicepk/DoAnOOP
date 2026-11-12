package com.smartphonestore.model.sale;

public class CashPayment extends Payment {
    private double cashGiven;
    public CashPayment(String paymentId,String invoiceId,double amount,String transactionCode,double cashGiven){
        super(paymentId,invoiceId,amount,transactionCode);
        if(cashGiven<amount) throw new IllegalArgumentException("Tien khach dua chua du");
        this.cashGiven=cashGiven;
    }
    public double calculateChange(){return cashGiven-amount;}
    @Override public String getMethod(){return "CASH";}
    @Override public String getExtraData(){return Double.toString(cashGiven);}
}
