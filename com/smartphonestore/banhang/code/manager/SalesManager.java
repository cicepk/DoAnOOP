package com.smartphonestore.manager;

import com.smartphonestore.model.sale.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.LocalDate;
import java.util.*;

public class SalesManager {
    private final List<Invoice> invoices=new ArrayList<>();
    private final List<InvoiceDetail> details=new ArrayList<>();
    private final List<Payment> payments=new ArrayList<>();
    private final String invoiceFile="data/invoices.txt", detailFile="data/invoice_details.txt", paymentFile="data/payments.txt";
    private InventoryGateway inventory;

    public SalesManager(){this(null);}
    public SalesManager(InventoryGateway inventory){this.inventory=inventory; loadAll();}
    public void setInventoryGateway(InventoryGateway inventory){this.inventory=inventory;}
    public InventoryGateway.ProductInfo findProduct(String productId){return inventory==null?null:inventory.findById(productId);}
    private List<String> read(String file){
        try{return Files.exists(Paths.get(file))?Files.readAllLines(Paths.get(file),StandardCharsets.UTF_8):new ArrayList<>();}
        catch(IOException e){System.err.println("Loi doc "+file+": "+e.getMessage());return new ArrayList<>();}
    }
    private boolean write(String file,List<String> lines){
        try{
            Path p=Paths.get(file); if(p.getParent()!=null) Files.createDirectories(p.getParent());
            Files.write(p,lines,StandardCharsets.UTF_8); return true;
        }catch(IOException e){System.err.println("Loi ghi "+file+": "+e.getMessage());return false;}
    }
    private void loadAll(){
        for(String s:read(invoiceFile)) try{if(!s.trim().isEmpty()) invoices.add(Invoice.fromFileString(s));}catch(Exception e){System.err.println("Bo qua hoa don loi: "+s);}
        for(String s:read(detailFile)) try{if(!s.trim().isEmpty()) details.add(InvoiceDetail.fromFileString(s));}catch(Exception e){System.err.println("Bo qua chi tiet loi: "+s);}
        // Payment persistence is loaded as records for audit; only new in-session payment objects are used for payment actions.
    }
    private boolean saveInvoices(){List<String> a=new ArrayList<>();for(Invoice i:invoices)a.add(i.toFileString());return write(invoiceFile,a);}
    private boolean saveDetails(){List<String> a=new ArrayList<>();for(InvoiceDetail d:details)a.add(d.toFileString());return write(detailFile,a);}
    private boolean savePayments(){List<String> a=read(paymentFile);for(Payment p:payments)a.add(p.toFileString());return write(paymentFile,a);}
    public List<Invoice> getAllInvoices(){return new ArrayList<>(invoices);}
    public List<InvoiceDetail> getDetailsByInvoice(String id){List<InvoiceDetail> out=new ArrayList<>();for(InvoiceDetail d:details)if(d.getInvoiceId().equalsIgnoreCase(id))out.add(d);return out;}
    public Invoice findInvoice(String id){for(Invoice i:invoices)if(i.getId().equalsIgnoreCase(id))return i;return null;}
    public String nextInvoiceId(){return nextId("HD",invoices.size()+1);}
    private String nextId(String prefix,int n){String id;do{id=prefix+String.format("%04d",n++);}while(findInvoice(id)!=null);return id;}
    public boolean createInvoice(String id,String customerId,String staffId,double vat,List<InvoiceDetail> newDetails){
        if(newDetails==null||newDetails.isEmpty())throw new IllegalArgumentException("Hoa don phai co it nhat mot san pham");
        if(findInvoice(id)!=null)throw new IllegalArgumentException("Ma hoa don da ton tai");
        double subtotal=0;
        Set<String> serialsInNewInvoice = new HashSet<>();
        Set<String> serialsAlreadySold = new HashSet<>();
        for (InvoiceDetail oldDetail : details)
            for (String serial : oldDetail.getSerialNumbers()) serialsAlreadySold.add(serial.toUpperCase(Locale.ROOT));
        for(InvoiceDetail d:newDetails){
            if(!d.getInvoiceId().equals(id)) throw new IllegalArgumentException("InvoiceId chi tiet khong khop");
            for (String serial : d.getSerialNumbers()) {
                String key = serial.toUpperCase(Locale.ROOT);
                if (!serialsInNewInvoice.add(key) || serialsAlreadySold.contains(key))
                    throw new IllegalArgumentException("Serial/IMEI da duoc su dung: " + serial);
            }
            subtotal+=d.calculateLineTotal();
        }
        // Kiem tra du thong tin truoc, sau do moi tru kho. Neu mot dong that bai thi dung va hoan lai cac dong tru truoc.
        List<InvoiceDetail> reduced=new ArrayList<>();
        for(InvoiceDetail d:newDetails){
            if(inventory!=null){
                if(!inventory.decreaseStock(d.getProductId(),d.getQuantity())){
                    for(InvoiceDetail old:reduced) inventory.increaseStock(old.getProductId(),old.getQuantity());
                    throw new IllegalStateException("Khong du ton kho cho san pham "+d.getProductId());
                }
                reduced.add(d);
            }
        }
        Invoice inv=new Invoice(id,customerId,staffId,null,subtotal,"UNPAID",vat,new ArrayList<>(newDetails));
        invoices.add(inv);details.addAll(newDetails);
        if(!saveInvoices()||!saveDetails()){
            invoices.remove(inv);details.removeAll(newDetails);
            if(inventory!=null)for(InvoiceDetail d:reduced)inventory.increaseStock(d.getProductId(),d.getQuantity());
            throw new IllegalStateException("Khong the luu hoa don; da huy thao tac");
        }
        return true;
    }
    public boolean addPayment(Payment p){
        Invoice inv=findInvoice(p.getInvoiceId());
        if(inv==null||"CANCELLED".equals(inv.getStatus()))return false;
        if("PAID".equals(inv.getStatus()))return false;
        p.complete(); payments.add(p); inv.setStatus("PAID");
        if(!saveInvoices()||!savePayments()){inv.setStatus("UNPAID");payments.remove(p);return false;}
        return true;
    }
    public boolean cancelInvoice(String id){
        Invoice inv=findInvoice(id);if(inv==null||"CANCELLED".equals(inv.getStatus()))return false;
        if("PAID".equals(inv.getStatus()))return false; // Khong huy hoa don da thanh toan de tranh sai lech doi soat.
        if(inventory!=null)for(InvoiceDetail d:getDetailsByInvoice(id))inventory.increaseStock(d.getProductId(),d.getQuantity());
        inv.setStatus("CANCELLED");return saveInvoices();
    }
    public double revenueToday(){
        LocalDate today=LocalDate.now();double sum=0;
        for(Invoice i:invoices)if("PAID".equals(i.getStatus())&&i.getCreationDate().startsWith(today.toString()))sum+=i.calculateFinalTotal();
        return sum;
    }
}
