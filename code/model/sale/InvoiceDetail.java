package com.smartphonestore.model.sale;

import java.util.*;

public class InvoiceDetail {
    private String invoiceId, productId, productName;
    private double unitPrice;
    private int quantity;
    private List<String> serialNumbers;

    // Constructor cu de doc/tuong thich du lieu chi tiet hoa don chua co serial.
    public InvoiceDetail(String invoiceId, String productId, String productName, double unitPrice, int quantity) {
        this(invoiceId, productId, productName, unitPrice, quantity, Collections.emptyList());
    }

    public InvoiceDetail(String invoiceId, String productId, String productName, double unitPrice,
                         int quantity, List<String> serialNumbers) {
        if (invoiceId == null || invoiceId.trim().isEmpty()) throw new IllegalArgumentException("Ma hoa don khong duoc trong");
        if (productId == null || productId.trim().isEmpty()) throw new IllegalArgumentException("Ma san pham khong duoc trong");
        if (productName == null || productName.trim().isEmpty()) throw new IllegalArgumentException("Ten san pham khong duoc trong");
        if (unitPrice < 0 || quantity <= 0) throw new IllegalArgumentException("Gia phai >= 0 va so luong phai > 0");
        this.invoiceId = invoiceId.trim(); this.productId = productId.trim();
        this.productName = productName.trim(); this.unitPrice = unitPrice; this.quantity = quantity;
        this.serialNumbers = new ArrayList<>();
        if (serialNumbers != null) {
            for (String serial : serialNumbers) {
                if (serial == null || serial.trim().isEmpty()) throw new IllegalArgumentException("Serial/IMEI khong duoc trong");
                this.serialNumbers.add(serial.trim());
            }
        }
        if (!this.serialNumbers.isEmpty() && this.serialNumbers.size() != quantity)
            throw new IllegalArgumentException("So serial phai bang so luong san pham");
    }

    public double calculateLineTotal() { return unitPrice * quantity; }
    public String toFileString() {
        return String.join("|", invoiceId, productId, productName.replace("|", "/"),
                Double.toString(unitPrice), Integer.toString(quantity), String.join(",", serialNumbers));
    }
    public static InvoiceDetail fromFileString(String line) {
        String[] p = line.split("\\|", -1);
        if (p.length != 5 && p.length != 6) throw new IllegalArgumentException("Dong chi tiet hoa don khong hop le");
        List<String> serials = new ArrayList<>();
        if (p.length == 6 && !p[5].trim().isEmpty()) serials.addAll(Arrays.asList(p[5].split(",")));
        return new InvoiceDetail(p[0], p[1], p[2], Double.parseDouble(p[3]), Integer.parseInt(p[4]), serials);
    }
    public String getInvoiceId(){return invoiceId;}
    public String getProductId(){return productId;}
    public String getProductName(){return productName;}
    public double getUnitPrice(){return unitPrice;}
    public int getQuantity(){return quantity;}
    public List<String> getSerialNumbers(){return new ArrayList<>(serialNumbers);}
}
