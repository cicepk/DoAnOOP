package com.smartphonestore.model.sale;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public abstract class Payment {
    protected String paymentId, invoiceId, paymentDate, transactionCode;
    protected double amount;
    protected boolean completed;

    public Payment(String paymentId,String invoiceId,double amount,String transactionCode) {
        if(paymentId==null||paymentId.trim().isEmpty()||invoiceId==null||invoiceId.trim().isEmpty())
            throw new IllegalArgumentException("Ma thanh toan/hoa don khong duoc trong");
        if(amount<0) throw new IllegalArgumentException("So tien khong hop le");
        this.paymentId=paymentId.trim(); this.invoiceId=invoiceId.trim(); this.amount=amount;
        this.transactionCode=transactionCode==null?"":transactionCode.trim();
        this.paymentDate=LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        this.completed=false;
    }
    public boolean verifyPaymentStatus(){return completed;}
    public void complete(){completed=true;}
    public abstract String getMethod();
    public abstract String getExtraData();
    public String toFileString(){
        return String.join("|",paymentId,invoiceId,Double.toString(amount),paymentDate,
                transactionCode,getMethod(),Boolean.toString(completed),getExtraData().replace("|","/"));
    }
    public String getInvoiceId(){return invoiceId;} public double getAmount(){return amount;}
    public String getPaymentId(){return paymentId;}
}
