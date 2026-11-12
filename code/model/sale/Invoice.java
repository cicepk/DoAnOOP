package com.smartphonestore.model.sale;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class Invoice implements IPrintable {
    private String id, customerId, salesStaffId, creationDate, status;
    private double totalAmount;
    private double vatTaxRate;
    private List<InvoiceDetail> details;

    public Invoice(String id, String customerId, String salesStaffId, String creationDate,
                   double totalAmount, String status, double vatTaxRate, List<InvoiceDetail> details) {
        if (id == null || id.trim().isEmpty()) throw new IllegalArgumentException("Mã hóa đơn không được trống");
        if (totalAmount < 0 || vatTaxRate < 0) throw new IllegalArgumentException("Số tiền/thuế không hợp lệ");
        this.id=id.trim(); this.customerId=clean(customerId); this.salesStaffId=clean(salesStaffId);
        this.creationDate=creationDate == null ? LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) : creationDate;
        this.totalAmount=totalAmount; this.status=status == null ? "UNPAID" : status;
        this.vatTaxRate=vatTaxRate; this.details=details;
    }
    private String clean(String s){return s == null ? "" : s.trim();}
    public double calculateFinalTotal(){return totalAmount * (1.0 + vatTaxRate);}
    public void setStatus(String status){this.status=status;}
    public void setDetails(List<InvoiceDetail> details){this.details=details;}
    public void printReceipt() {
        System.out.println("\n================ HOA DON BAN LE ================");
        System.out.printf("Ma HD: %s%nKhach hang: %s%nNhan vien: %s%nNgay lap: %s%n",
                id, customerId.isEmpty()?"Khach le":customerId, salesStaffId, creationDate);
        System.out.println("-------------------------------------------------");
        System.out.printf("%-8s %-20s %5s %12s %14s%n","Ma SP","Ten san pham","SL","Don gia","Thanh tien");
        if (details != null) {
            for (InvoiceDetail d : details) {
                System.out.printf("%-8s %-20s %5d %12.0f %14.0f%n", d.getProductId(),
                        shorten(d.getProductName(),20), d.getQuantity(), d.getUnitPrice(), d.calculateLineTotal());
                if (!d.getSerialNumbers().isEmpty())
                    System.out.println("         Serial/IMEI: " + String.join(", ", d.getSerialNumbers()));
            }
        }
        System.out.println("-------------------------------------------------");
        System.out.printf("Cong tien hang: %,15.0f VND%n",totalAmount);
        System.out.printf("VAT (%.0f%%):    %,15.0f VND%n",vatTaxRate*100,totalAmount*vatTaxRate);
        System.out.printf("TONG THANH TOAN:%,15.0f VND%n",calculateFinalTotal());
        System.out.println("Trang thai: "+status);
        System.out.println("=================================================");
    }
    private String shorten(String s,int n){return s.length()<=n?s:s.substring(0,n-1)+"…";}
    public String toFileString(){
        return String.join("|",id,customerId,salesStaffId,creationDate,Double.toString(totalAmount),status,Double.toString(vatTaxRate));
    }
    public static Invoice fromFileString(String line){
        String[] p=line.split("\\|",-1); if(p.length!=7) throw new IllegalArgumentException("Dong hoa don khong hop le");
        return new Invoice(p[0],p[1],p[2],p[3],Double.parseDouble(p[4]),p[5],Double.parseDouble(p[6]),null);
    }
    public String getId(){return id;} public String getCustomerId(){return customerId;}
    public String getSalesStaffId(){return salesStaffId;} public String getCreationDate(){return creationDate;}
    public double getTotalAmount(){return totalAmount;} public String getStatus(){return status;}
    public double getVatTaxRate(){return vatTaxRate;}
}
