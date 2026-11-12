package com.smartphonestore.view;

import com.smartphonestore.manager.*;
import com.smartphonestore.model.sale.*;
import java.util.*;

public class SalesMenu {
    private final Scanner sc=new Scanner(System.in);
    private final SalesManager manager;
    private final String staffId;
    private int paymentSeq=1;
    public SalesMenu(SalesManager manager,String staffId){
        this.manager=manager;
        this.staffId=staffId==null?"NV01":staffId;
    }
    public void show(){
        boolean running=true;
        while(running){
            System.out.println("\n========== MENU NHAN VIEN BAN HANG - THANH VIEN 3 ==========");
            System.out.println("[1] Xem danh sach hoa don");
            System.out.println("[2] Lap hoa don ban le moi");
            System.out.println("[3] Xem chi tiet san pham trong hoa don");
            System.out.println("[4] In hoa don VAT");
            System.out.println("[5] Thanh toan tien mat");
            System.out.println("[6] Thanh toan chuyen khoan / QR");
            System.out.println("[7] Tim hoa don theo ma");
            System.out.println("[8] Huy hoa don chua thanh toan");
            System.out.println("[9] Thong ke doanh thu hom nay");
            System.out.println("[10] Dang xuat");System.out.println("[0] Thoat");
            int c=readInt("Chon: ",0,10);
            try{
                switch(c){
                    case 1: listInvoices();break;
                    case 2: createInvoice();break;
                    case 3: showDetails();break;
                    case 4: printInvoice();break;
                    case 5: cashPayment();break;
                    case 6: electronicPayment();break;
                    case 7: searchInvoice();break;
                    case 8: cancelInvoice();break;
                    case 9: System.out.printf("Doanh thu da thanh toan hom nay: %,.0f VND%n",manager.revenueToday());break;
                    case 10: running=false;break;
                    case 0: System.out.println("Ket thuc module ban hang.");return;
                }
            }catch(RuntimeException e){System.out.println("Loi: "+e.getMessage());}
        }
    }
    private void listInvoices(){
        List<Invoice> a=manager.getAllInvoices();if(a.isEmpty()){System.out.println("Chua co hoa don.");return;}
        for(Invoice i:a)System.out.printf("%-9s | %-10s | %-12s | %,12.0f VND | %s%n",i.getId(),i.getCustomerId(),i.getCreationDate(),i.calculateFinalTotal(),i.getStatus());
    }
    private void createInvoice(){
        String id=manager.nextInvoiceId(), customer=readText("Ma khach hang (Enter neu khach le): ",true);
        double vat=readDouble("VAT (mac dinh 8%, nhap 8): ",0,100)/100.0;
        List<InvoiceDetail> lines=new ArrayList<>();
        while(true){
            String pid=readText("Ma san pham (0 de ket thuc): ",false);if(pid.equals("0"))break;
            InventoryGateway.ProductInfo p=manager.findProduct(pid);
            if(p==null){System.out.println("Khong tim thay san pham.");continue;}
            System.out.printf("San pham: %s | Gia: %,.0f VND%n",p.name,p.price);
            int qty=readInt("So luong: ",1,100000);
            List<String> serials = new ArrayList<>();
            for (int i = 1; i <= qty; i++) {
                serials.add(readText("Nhap Serial/IMEI cua san pham thu " + i + ": ", false));
            }
            lines.add(new InvoiceDetail(id,p.id,p.name,p.price,qty,serials));
        }
        if(lines.isEmpty()){System.out.println("Khong tao hoa don rong.");return;}
        manager.createInvoice(id,customer,staffId,vat,lines);
        System.out.println("Da tao hoa don "+id+". Trang thai UNPAID.");
    }
    private void showDetails(){String id=readText("Ma hoa don: ",false);List<InvoiceDetail> ds=manager.getDetailsByInvoice(id);
        if(ds.isEmpty()){System.out.println("Khong co chi tiet.");return;}
        for(InvoiceDetail d:ds) {
            System.out.printf("%s | %s | SL %d | %,.0f | Thanh tien %,.0f%n",d.getProductId(),d.getProductName(),d.getQuantity(),d.getUnitPrice(),d.calculateLineTotal());
            if (!d.getSerialNumbers().isEmpty()) System.out.println("  Serial/IMEI: " + String.join(", ", d.getSerialNumbers()));
        }
    }
    private void printInvoice(){Invoice i=manager.findInvoice(readText("Ma hoa don: ",false));if(i==null){System.out.println("Khong tim thay.");return;}i.setDetails(manager.getDetailsByInvoice(i.getId()));i.printReceipt();}
    private void cashPayment(){
        Invoice i=unpaidInvoice();if(i==null)return;double total=i.calculateFinalTotal();
        double given=readDouble("Tong can thanh toan "+String.format("%,.0f",total)+" VND. Tien khach dua: ",total,1e15);
        CashPayment p=new CashPayment(nextPaymentId(),i.getId(),total,"CASH-"+System.nanoTime(),given);
        if(manager.addPayment(p))System.out.printf("Thanh toan thanh cong. Tien thoi: %,.0f VND%n",p.calculateChange());
        else System.out.println("Khong the ghi nhan thanh toan.");
    }
    private void electronicPayment(){
        Invoice i=unpaidInvoice();if(i==null)return;
        String bank=readText("Ngan hang/vi dien tu: ",false);
        ElectronicPayment p=new ElectronicPayment(nextPaymentId(),i.getId(),i.calculateFinalTotal(),"QR-"+System.nanoTime(),bank);
        System.out.println("Chuoi du lieu QR tham chieu (demo): "+p.generatePaymentQRCode());
        if(readText("Da xac nhan khach chuyen khoan? (Y/N): ",false).equalsIgnoreCase("Y")){
            if(manager.addPayment(p))System.out.println("Da ghi nhan thanh toan.");else System.out.println("Ghi nhan that bai.");
        }else System.out.println("Chua xac nhan; hoa don van UNPAID.");
    }
    private Invoice unpaidInvoice(){Invoice i=manager.findInvoice(readText("Ma hoa don: ",false));if(i==null){System.out.println("Khong tim thay hoa don.");return null;}
        if(!"UNPAID".equals(i.getStatus())){System.out.println("Hoa don khong o trang thai UNPAID.");return null;}return i;}
    private String nextPaymentId(){return "PAY"+System.nanoTime();}
    private void searchInvoice(){Invoice i=manager.findInvoice(readText("Nhap ma hoa don: ",false));if(i==null)System.out.println("Khong tim thay.");else{System.out.println(i.getId()+" | "+i.getStatus()+" | "+i.calculateFinalTotal());}}
    private void cancelInvoice(){String id=readText("Ma hoa don can huy: ",false);System.out.println(manager.cancelInvoice(id)?"Da huy hoa don va hoan ton kho (neu da ket noi kho).":"Khong huy duoc. Kiem tra ma hoa don/trang thai.");}
    private String readText(String p,boolean empty){while(true){System.out.print(p);String s=sc.nextLine().trim();if(empty||!s.isEmpty())return s;System.out.println("Khong duoc de trong.");}}
    private int readInt(String p,int min,int max){while(true){try{int n=Integer.parseInt(readText(p,false));if(n>=min&&n<=max)return n;}catch(NumberFormatException e){}System.out.println("Nhap so nguyen tu "+min+" den "+max+".");}}
    private double readDouble(String p,double min,double max){while(true){try{double n=Double.parseDouble(readText(p,false).replace(",",""));if(n>=min&&n<=max)return n;}catch(NumberFormatException e){}System.out.println("Gia tri khong hop le.");}}
}
