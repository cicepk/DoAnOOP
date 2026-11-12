package com.smartphonestore.model.sale;

public class ElectronicPayment extends Payment {
    private String bankOrWalletName;
    public ElectronicPayment(String paymentId,String invoiceId,double amount,String transactionCode,String bankOrWalletName){
        super(paymentId,invoiceId,amount,transactionCode);
        if(bankOrWalletName==null||bankOrWalletName.trim().isEmpty()) throw new IllegalArgumentException("Ten ngan hang/vi khong duoc trong");
        this.bankOrWalletName=bankOrWalletName.trim();
    }
    // Chuoi tham chieu de tao QR trong ung dung ngan hang; khong phai QR thanh toan that.
    public String generatePaymentQRCode(){
        return "PAY|"+invoiceId+"|"+String.format(java.util.Locale.US,"%.0f",amount)+"|"+bankOrWalletName+"|"+transactionCode;
    }
    @Override public String getMethod(){return "ELECTRONIC";}
    @Override public String getExtraData(){return bankOrWalletName;}
}
