package src.com.smartphonestore.view;

import src.com.smartphonestore.exception.ValidationException;
import src.com.smartphonestore.manager.ProductManager;
import src.com.smartphonestore.model.product.*;
import java.util.*;

public class WarehouseMenu {
    private final ProductManager prd;
    private final Scanner sc;

    public WarehouseMenu(ProductManager prd) {
        this.prd = prd;
        this.sc = new Scanner(System.in);
    }

    public WarehouseMenu() {
        this(new ProductManager());
    }

    public void displayMenu() {
        boolean running = true;
        while (running) {
            System.out.println("\n==============================================================");
            System.out.println("             MENU NHAN VIEN KHO (THANH VIEN 2)                ");
            System.out.println("==============================================================");
            System.out.println(" [1] Xem danh muc toan bo san pham & Ton kho");
            System.out.println(" [2] Xem bang chi tiet tung con may (Serial/IMEI & Ngay nhap)");
            System.out.println(" [3] Lap phieu nhap hang vao kho (Nhap lo / Nhap moi)");
            System.out.println(" [4] Cap nhat thong tin / Gia ban san pham theo Ma ID");
            System.out.println(" [5] Xoa san pham khoi danh muc kho theo Ma ID");
            System.out.println(" [6] Tim kiem san pham theo Ten / Thuong hieu");
            System.out.println(" [7] Canh bao & Loc danh sach hang sap het (isLowStock)");
            System.out.println(" [8] Ap dung chuong trinh chiet khau san pham (IDiscountable)");
            System.out.println(" [9] Kiem tra tinh nang doc quyen (Thu cu doi moi / SOS / Tuong thich)");
            System.out.println(" [0] Thoat (Luu toan bo du lieu)");
            System.out.println("==============================================================");
            System.out.print("➤ Chon chuc nang (0-9): ");

            String choiceStr = sc.nextLine().trim();
            int input;
            try {
                input = Integer.parseInt(choiceStr);
            } catch (NumberFormatException e) {
                System.out.println("[X] Vui long nhap so tu 0 den 9!");
                continue;
            }

            switch (input) {
                case 1: {
                    System.out.println("\n========== DANH MUC TOAN BO SAN PHAM TRONG KHO ==========");
                    List<Product> list = prd.getALL();
                    if (list.isEmpty()) {
                        System.out.println("[!] Kho hien dang trong, chua co san pham nao!");
                    } else {
                        for (Product p : list) {
                            p.displayInfo();
                        }
                    }
                    System.out.println("==========================================================");
                    break;
                }

                // ==============================================================
                // CHỨC NĂNG 2: XEM BẢNG CHI TIẾT SẢN PHẨM (THEO HÃNG / MÃ SP)
                // ==============================================================
                case 2: {
                    System.out.println("\n--- XEM BANG CHI TIET TUNG CON MAY TRONG KHO ---");
                    System.out.println(" [1] Xem tat ca chi tiet con may trong kho");
                    System.out.println(" [2] Xem chi tiet loc theo Hang (Apple, Samsung, Xiaomi...)");
                    System.out.println(" [3] Xem chi tiet theo Ma san pham cu the (vi du PRD01)");
                    System.out.println(" [0] Quay lai Menu chinh");
                    System.out.print("➤ Chon che do xem (0-3): ");

                    String subChoice = sc.nextLine().trim();
                    List<ProductDetail> detailsToShow = new ArrayList<>();
                    String title = "TAT CA SAN PHAM TRONG KHO";

                    if ("1".equals(subChoice)) {
                        detailsToShow = prd.getAllProductDetails();
                        title = "TAT CA CON MAY TRONG KHO";
                    } else if ("2".equals(subChoice)) {
                        System.out.print("Nhap ten hang muon xem (vi du Apple, Samsung, Xiaomi, Nokia): ");
                        String brand = sc.nextLine().trim();
                        detailsToShow = prd.getDetailsByBrand(brand);
                        title = "HANG SAN XUAT: " + brand.toUpperCase();
                    } else if ("3".equals(subChoice)) {
                        System.out.print("Nhap ma san pham (vi du PRD01, PRD02): ");
                        String pid = ProductManager.normalizeId(sc.nextLine().trim());
                        detailsToShow = prd.getDetailsByProductId(pid);
                        Product p = prd.findByID(pid);
                        title = "MA SAN PHAM: " + pid.toUpperCase() + (p != null ? " (" + p.getName() + ")" : "");
                    } else if ("0".equals(subChoice)) {
                        break;
                    } else {
                        System.out.println("[X] Lua chon khong hop le!");
                        break;
                    }

                    System.out.println("\n========================================================================================================================");
                    System.out.println("                                  BANG CHI TIET CON MAY - " + title);
                    System.out.println("========================================================================================================================");
                    System.out.printf(" %-7s │ %-24s │ %-8s │ %-16s │ %-10s │ %-14s │ %-12s │ %-9s%n",
                            "MA SP", "TEN SAN PHAM", "HANG", "SO SERI / IMEI", "NGAY NHAP", "GIA NHAP", "NHA CUNG CAP", "TRANG THAI");
                    System.out.println("────────┼──────────────────────────┼──────────┼──────────────────┼────────────┼────────────────┼──────────────┼──────────");

                    if (detailsToShow.isEmpty()) {
                        System.out.println("  [!] Khong tim thay con may nao phu hop trong du lieu chi tiet.");
                    } else {
                        int inStockCount = 0;
                        int soldCount = 0;
                        for (ProductDetail d : detailsToShow) {
                            Product p = prd.findByID(d.getProductId());
                            String pName = p != null ? p.getName() : "Khong xac dinh";
                            String pBrand = p != null ? p.getBrand() : "N/A";
                            d.displayRow(pName, pBrand);
                            if ("IN_STOCK".equalsIgnoreCase(d.getStatus())) inStockCount++;
                            else soldCount++;
                        }
                        System.out.println("========================================================================================================================");
                        System.out.printf("Tong cong: %d con may (Trong kho: %d | Da xuat ban: %d)%n",
                                detailsToShow.size(), inStockCount, soldCount);
                    }
                    break;
                }

                // ==============================================================
                // CHỨC NĂNG 3: LẬP PHIẾU NHẬP HÀNG (VÒNG LẶP WHILE - NHẬP ĐẾN KHI GÕ 0)
                // ==============================================================
                case 3: {
                    System.out.println("\n==============================================================");
                    System.out.println("       LAP PHIEU NHAP HANG VAO KHO (GOODS RECEIPT NOTE)       ");
                    System.out.println("==============================================================");
                    System.out.print("Nhap ten Nha cung cap (Enter mac dinh: FPT_Synnex): ");
                    String supplier = sc.nextLine().trim();
                    if (supplier.isEmpty()) supplier = "FPT_Synnex";

                    String today = java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"));
                    System.out.print("Nhap ngay nhap kho [dd/MM/yyyy] (Enter mac dinh hom nay: " + today + "): ");
                    String importDate = sc.nextLine().trim();
                    if (importDate.isEmpty()) importDate = today;

                    int sessionItemCount = 0;
                    int sessionTotalQty = 0;
                    double sessionTotalCost = 0;

                    // VÒNG LẶP WHILE NHẬP HÀNG LIÊN TỤC CHO ĐẾN KHI GÕ 0
                    while (true) {
                        System.out.println("\n--------------------------------------------------------------");
                        System.out.print("➤ Nhap Ma san pham muon nhap (vi du: PRD01, hoac go '0' de dung): ");
                        String rawId = sc.nextLine().trim();

                        if ("0".equals(rawId)) {
                            System.out.println("[OK] Ket thuc cac mat hang trong phieu nhap!");
                            break;
                        }
                        if (rawId.isEmpty()) {
                            System.out.println("[X] Ma san pham khong duoc de trong!");
                            continue;
                        }

                        String id = ProductManager.normalizeId(rawId);
                        Product p = prd.findByID(id);

                        if (p != null) {
                            // TRƯỜNG HỢP 1: SẢN PHẨM ĐÃ CÓ TRONG KHO
                            System.out.printf(">> [SAN PHAM CO SAN]: %s | Hang: %s | Ton hien tai: %d chiec%n",
                                    p.getName(), p.getBrand(), p.getStockQuantity());

                            int qty;
                            while (true) {
                                System.out.print("Nhap so luong hang moi ve: ");
                                try {
                                    qty = Integer.parseInt(sc.nextLine().trim());
                                    if (qty > 0) break;
                                    System.out.println("[X] So luong phai lon hon 0!");
                                } catch (NumberFormatException e) {
                                    System.out.println("[X] So luong phai la so nguyen!");
                                }
                            }

                            double importPrice;
                            while (true) {
                                System.out.print("Nhap gia nhap moi chiec (VND): ");
                                try {
                                    importPrice = Double.parseDouble(sc.nextLine().trim());
                                    if (importPrice > 0) break;
                                    System.out.println("[X] Gia nhap phai lon hon 0 VND!");
                                } catch (NumberFormatException e) {
                                    System.out.println("[X] Gia nhap phai la so hop le!");
                                }
                            }

                            // Nhập số IMEI/Seri cho từng máy trong lô
                            List<ProductDetail> batchDetails = new ArrayList<>();
                            System.out.printf(">> Nhap so Seri/IMEI cho %d con may moi:%n", qty);
                            for (int i = 1; i <= qty; i++) {
                                String serial;
                                while (true) {
                                    System.out.printf(" - Seri/IMEI may [%d/%d] (Enter de tu dong sinh): ", i, qty);
                                    String inputSn = sc.nextLine().trim();
                                    if (inputSn.isEmpty()) {
                                        serial = "IMEI" + id.replace("PRD", "") + (System.currentTimeMillis() % 1000000) + String.format("%02d", i);
                                        System.out.println("   -> Tu dong cap IMEI: " + serial);
                                    } else {
                                        serial = inputSn.toUpperCase();
                                    }

                                    if (prd.isSerialExists(serial)) {
                                        System.out.println("   [X] So Seri/IMEI nay da ton tai trong kho! Vui long nhap lai.");
                                        continue;
                                    }
                                    break;
                                }
                                batchDetails.add(new ProductDetail(p.getId(), p.getName(), serial, importDate, importPrice, supplier, "IN_STOCK"));
                            }

                            p.increaseStock(qty);
                            prd.addProductDetails(batchDetails);
                            prd.saveToFile();

                            sessionItemCount++;
                            sessionTotalQty += qty;
                            sessionTotalCost += (qty * importPrice);

                            System.out.printf("[OK] Nhap them thanh cong %d chiec '%s'! Ton kho moi: %d chiec%n",
                                    qty, p.getName(), p.getStockQuantity());

                        } else {
                            // TRƯỜNG HỢP 2: SẢN PHẨM MỚI TOANH CHƯA CÓ TRONG KHO
                            if (!id.matches("^PRD\\d{2,}$")) {
                                String autoId = prd.generateNextID();
                                System.out.printf(">> [THONG TIN]: Ma '%s' chua dung chuan 'PRDxx'. He thong tu dong cap ma: %s%n", id, autoId);
                                id = autoId;
                            }

                            System.out.printf(">> [SAN PHAM MOI]: Ma '%s' chua tung co trong kho!%n", id);
                            System.out.print("Ban co muon khai bao san pham moi nay luon khong? (Y/N): ");
                            String confirm = sc.nextLine().trim();
                            if (!confirm.equalsIgnoreCase("Y")) {
                                System.out.println("[!] Bo qua ma nay.");
                                continue;
                            }

                            System.out.println("\n--- KHAI BAO THONG SO SAN PHAM MOI ---");
                            int typeChoice;
                            while (true) {
                                System.out.println("Chon dong san pham:");
                                System.out.println(" [1] Smartphone (Dien thoai thong minh)");
                                System.out.println(" [2] Featurephone (Dien thoai pho thong)");
                                System.out.println(" [3] Accessory (Phu kien)");
                                System.out.print("Chon (1-3): ");
                                try {
                                    typeChoice = Integer.parseInt(sc.nextLine().trim());
                                    if (typeChoice >= 1 && typeChoice <= 3) break;
                                } catch (NumberFormatException ignored) {}
                                System.out.println("[X] Vui long chon 1, 2 hoac 3!");
                            }

                            String name;
                            while (true) {
                                System.out.print("Nhap ten san pham: ");
                                name = sc.nextLine().trim();
                                if (!name.isEmpty()) break;
                                System.out.println("[X] Ten san pham khong duoc de trong!");
                            }

                            String brand;
                            while (true) {
                                System.out.print("Nhap hang san xuat (Apple, Samsung, Xiaomi...): ");
                                brand = sc.nextLine().trim();
                                if (!brand.isEmpty()) break;
                                System.out.println("[X] Hang san xuat khong duoc de trong!");
                            }

                            double sellPrice;
                            while (true) {
                                System.out.print("Nhap gia ban niem yet cho khach (VND): ");
                                try {
                                    sellPrice = Double.parseDouble(sc.nextLine().trim());
                                    if (sellPrice > 0) break;
                                    System.out.println("[X] Gia ban phai lon hon 0 VND!");
                                } catch (NumberFormatException e) {
                                    System.out.println("[X] Gia ban phai la so!");
                                }
                            }

                            int threshold;
                            while (true) {
                                System.out.print("Nhap nguong canh bao ton kho toi thieu: ");
                                try {
                                    threshold = Integer.parseInt(sc.nextLine().trim());
                                    if (threshold >= 0) break;
                                    System.out.println("[X] Nguong ton kho khong duoc am!");
                                } catch (NumberFormatException e) {
                                    System.out.println("[X] Nguong ton kho phai la so nguyen!");
                                }
                            }

                            int qty;
                            while (true) {
                                System.out.print("Nhap so luong nhap lo dau tien: ");
                                try {
                                    qty = Integer.parseInt(sc.nextLine().trim());
                                    if (qty > 0) break;
                                    System.out.println("[X] So luong nhap phai lon hon 0!");
                                } catch (NumberFormatException e) {
                                    System.out.println("[X] So luong phai la so nguyen!");
                                }
                            }

                            double importPrice;
                            while (true) {
                                System.out.print("Nhap gia nhap von moi chiec (VND): ");
                                try {
                                    importPrice = Double.parseDouble(sc.nextLine().trim());
                                    if (importPrice > 0) break;
                                    System.out.println("[X] Gia nhap phai lon hon 0 VND!");
                                } catch (NumberFormatException e) {
                                    System.out.println("[X] Gia nhap phai la so!");
                                }
                            }

                            Product newProduct = null;
                            try {
                                if (typeChoice == 1) {
                                    String os;
                                    while (true) {
                                        System.out.print("Nhap he dieu hanh (iOS / Android): ");
                                        os = sc.nextLine().trim();
                                        if (!os.isEmpty()) break;
                                    }
                                    int storage;
                                    while (true) {
                                        System.out.print("Nhap dung luong ROM (GB, vi du 128, 256): ");
                                        try {
                                            storage = Integer.parseInt(sc.nextLine().trim());
                                            if (storage > 0) break;
                                        } catch (NumberFormatException ignored) {}
                                        System.out.println("[X] Dung luong ROM phai la so > 0!");
                                    }
                                    newProduct = new Smartphone(id, name, brand, sellPrice, qty, threshold, os, storage);

                                } else if (typeChoice == 2) {
                                    int battery;
                                    while (true) {
                                        System.out.print("Nhap thoi luong pin cho (so ngay, vi du 7, 10): ");
                                        try {
                                            battery = Integer.parseInt(sc.nextLine().trim());
                                            if (battery > 0) break;
                                        } catch (NumberFormatException ignored) {}
                                        System.out.println("[X] Thoi luong pin phai la so > 0!");
                                    }
                                    newProduct = new Featurephone(id, name, brand, sellPrice, qty, threshold, "KaiOS", battery);

                                } else {
                                    String accType;
                                    while (true) {
                                        System.out.print("Nhap loai phu kien (SAC, TAI_NGHE, OP_LUNG, CUONG_LUC): ");
                                        accType = sc.nextLine().trim().toUpperCase();
                                        if (accType.matches("^(SAC|TAI_NGHE|OP_LUNG|CUONG_LUC)$")) break;
                                        System.out.println("[X] Chi duoc chon: SAC, TAI_NGHE, OP_LUNG hoac CUONG_LUC!");
                                    }
                                    newProduct = new Accessory(id, name, brand, sellPrice, qty, threshold, accType);
                                }
                            } catch (ValidationException ve) {
                                System.out.println(ve.getMessage());
                                System.out.println("[X] Tao san pham moi that bai do loi hop le!");
                                continue;
                            } catch (Exception ex) {
                                System.out.println("[X] Loi khi khoi tao san pham: " + ex.getMessage());
                                continue;
                            }

                            // Nhập danh sách IMEI/Seri cho sản phẩm mới
                            List<ProductDetail> batchDetails = new ArrayList<>();
                            System.out.printf(">> Nhap so Seri/IMEI cho %d con may moi:%n", qty);
                            for (int i = 1; i <= qty; i++) {
                                String serial;
                                while (true) {
                                    System.out.printf(" - Seri/IMEI may [%d/%d] (Enter de tu dong sinh): ", i, qty);
                                    String inputSn = sc.nextLine().trim();
                                    if (inputSn.isEmpty()) {
                                        serial = "IMEI" + id.replace("PRD", "") + (System.currentTimeMillis() % 1000000) + String.format("%02d", i);
                                        System.out.println("   -> Tu dong cap IMEI: " + serial);
                                    } else {
                                        serial = inputSn.toUpperCase();
                                    }

                                    if (prd.isSerialExists(serial)) {
                                        System.out.println("   [X] So Seri/IMEI nay da ton tai trong kho! Vui long nhap lai.");
                                        continue;
                                    }
                                    break;
                                }
                                batchDetails.add(new ProductDetail(id, name, serial, importDate, importPrice, supplier, "IN_STOCK"));
                            }

                            boolean added = prd.add(newProduct);
                            if (added) {
                                prd.addProductDetails(batchDetails);
                                sessionItemCount++;
                                sessionTotalQty += qty;
                                sessionTotalCost += (qty * importPrice);
                                System.out.printf("[OK] Da tao thanh cong san pham moi '%s' (Ma: %s) va luu %d con may vao kho!%n", name, id, qty);
                            } else {
                                System.out.println("[X] Loi khi luu san pham moi!");
                            }
                        }
                    }

                    // TỔNG KẾT PHIẾU NHẬP SAU KHI THOÁT VÒNG LẶP WHILE
                    if (sessionTotalQty > 0) {
                        System.out.println("\n==============================================================");
                        System.out.println("       BIEN BAN TONG KET PHIEU NHAP HANG (GOODS RECEIPT)      ");
                        System.out.println("==============================================================");
                        System.out.println(" Nha cung cap  : " + supplier);
                        System.out.println(" Ngay nhap     : " + importDate);
                        System.out.println(" So dong mat hang: " + sessionItemCount + " dong");
                        System.out.println(" Tong so may nhap: " + sessionTotalQty + " chiec");
                        System.out.printf(" Tong tien nhap: %,.0f VND%n", sessionTotalCost);
                        System.out.println("==============================================================");
                        System.out.println("[OK] Du lieu da duoc dong bo vao 'products.txt' va 'product_details.txt'!");
                    } else {
                        System.out.println("[!] Khong co san pham nao duoc nhap trong phien nay.");
                    }
                    break;
                }

                case 4: {
                    System.out.println("\n--- CAP NHAT THONG TIN / GIA BAN SAN PHAM ---");
                    System.out.print("Nhap ma ID san pham can sua (vi du PRD01): ");
                    String id = ProductManager.normalizeId(sc.nextLine().trim());
                    Product p = prd.findByID(id);
                    if (p == null) {
                        System.out.println("[X] Khong tim thay san pham co ma: " + id);
                    } else {
                        System.out.println("Thong tin hien tai:");
                        p.displayInfo();
                        System.out.print("Nhap ten moi (bam Enter de giu nguyen): ");
                        String newName = sc.nextLine().trim();
                        if (newName.isEmpty()) newName = p.getName();

                        double newPrice = p.getPrice();
                        while (true) {
                            System.out.print("Nhap gia moi (nhap 0 neu giu nguyen): ");
                            try {
                                double inputPrice = Double.parseDouble(sc.nextLine().trim());
                                if (inputPrice == 0) break;
                                if (inputPrice > 0) {
                                    newPrice = inputPrice;
                                    break;
                                }
                                System.out.println("[X] Gia moi phai lon hon 0 VND!");
                            } catch (NumberFormatException e) {
                                System.out.println("[X] Gia moi phai la so hop le!");
                            }
                        }

                        int newThreshold = p.getMinStockThreshold();
                        while (true) {
                            System.out.print("Nhap nguong ton kho moi (nhap -1 neu giu nguyen): ");
                            try {
                                int inputThreshold = Integer.parseInt(sc.nextLine().trim());
                                if (inputThreshold == -1) break;
                                if (inputThreshold >= 0) {
                                    newThreshold = inputThreshold;
                                    break;
                                }
                                System.out.println("[X] Nguong ton kho khong duoc am!");
                            } catch (NumberFormatException e) {
                                System.out.println("[X] Nguong ton kho phai la so nguyen hop le!");
                            }
                        }

                        boolean ok = prd.updateProductInfo(p.getId(), newName, newPrice, newThreshold);
                        if (ok) {
                            System.out.println("[OK] Cap nhat thong tin san pham thanh cong!");
                        } else {
                            System.out.println("[X] Cap nhat that bai!");
                        }
                    }
                    break;
                }

                case 5: {
                    System.out.println("\n--- XOA SAN PHAM KHOI KHO THEO MA ID ---");
                    System.out.print("Nhap ma ID san pham can xoa: ");
                    String id = ProductManager.normalizeId(sc.nextLine().trim());
                    Product p = prd.findByID(id);
                    if (p == null) {
                        System.out.println("[X] Khong tim thay san pham co ma: " + id);
                    } else {
                        System.out.println("San pham muon xoa:");
                        p.displayInfo();
                        System.out.print("Ban co chac chan muon xoa khong? (Y/N): ");
                        String confirm = sc.nextLine().trim();
                        if (confirm.equalsIgnoreCase("Y")) {
                            boolean deleted = prd.deleteID(p.getId());
                            if (deleted) {
                                System.out.println("[OK] Da xoa san pham va toan bo chi tiet may lien quan khoi kho!");
                            } else {
                                System.out.println("[X] Xoa that bai!");
                            }
                        } else {
                            System.out.println("[!] Da huy thao tac xoa.");
                        }
                    }
                    break;
                }

                case 6: {
                    System.out.println("\n--- TIM KIEM SAN PHAM THEO TEN / THUONG HIEU ---");
                    System.out.print("Nhap tu khoa tim kiem (Ten may hoac Ten hang): ");
                    String keyword = sc.nextLine().trim();
                    List<Product> results = prd.findByNameOrBrand(keyword);
                    if (results.isEmpty()) {
                        System.out.println("[!] Khong tim thay san pham nao khop voi tu khoa: " + keyword);
                    } else {
                        System.out.printf("Tim thay %d san pham phu hop:%n", results.size());
                        for (Product p : results) {
                            p.displayInfo();
                        }
                    }
                    break;
                }

                case 7: {
                    System.out.println("\n--- CANH BAO: DANH SACH SAN PHAM SAP HET HANG ---");
                    List<Product> lowStock = prd.getLowStockProducts();
                    if (lowStock.isEmpty()) {
                        System.out.println("[OK] Ton kho an toan! Hien khong co san pham nao cham nguong het hang.");
                    } else {
                        System.out.printf("[!] CO %d SAN PHAM DANG CHAM HOAC DUOI NGUONG BAO DONG:%n", lowStock.size());
                        for (Product p : lowStock) {
                            p.displayInfo();
                        }
                    }
                    break;
                }

                case 8: {
                    System.out.println("\n--- TINH GIA CHIET KHAU KHUYEN MAI (IDiscountable) ---");
                    System.out.print("Nhap ma ID san pham muon tinh giam gia: ");
                    String id = ProductManager.normalizeId(sc.nextLine().trim());
                    Product p = prd.findByID(id);
                    if (p == null) {
                        System.out.println("[X] Khong tim thay san pham co ma: " + id);
                    } else if (!(p instanceof IDiscountable)) {
                        System.out.println("[!] San pham nay (Featurephone) khong ho tro chiet khau IDiscountable!");
                    } else {
                        System.out.printf("San pham: %s | Gia goc: %,.0f VND%n", p.getName(), p.getPrice());
                        double rate;
                        while (true) {
                            System.out.print("Nhap ty le chiet khau (vi du 0.1 cho 10%%, 0.2 cho 20%%): ");
                            try {
                                rate = Double.parseDouble(sc.nextLine().trim());
                                if (rate >= 0.0 && rate <= 1.0) break;
                                System.out.println("[X] Ty le chiet khau phai tu 0.0 den 1.0 (0% den 100%)!");
                            } catch (NumberFormatException e) {
                                System.out.println("[X] Ty le chiet khau phai la so hop le!");
                            }
                        }

                        double discountedPrice = prd.applyDiscount(p.getId(), rate);
                        if (discountedPrice > 0) {
                            System.out.printf("[OK] Da ap dung giam gia %.0f%% thanh cong!%n", rate * 100);
                            System.out.printf("[OK] Gia moi cua '%s': %,.0f VND (Da dong bo vao 'products.txt' & 'product_details.txt')%n", p.getName(), discountedPrice);
                        } else {
                            System.out.println("[X] Khong ap dung duoc chiet khau!");
                        }
                    }
                    break;
                }

                // ==========================================
                // CASE 9: TEST TÍNH NĂNG ĐỘC QUYỀN
                // ==========================================
                case 9: {
                    System.out.println("\n--- KIEM TRA TINH NANG DOC QUYEN CUA TUNG DONG SAN PHAM ---");
                    System.out.println(" [1] Thu cu doi moi (applyTradeInDiscount: Giam 10% cho Smartphone)");
                    System.out.println(" [2] Bat / Tat che do khan cap SOS (toggleEmergencySOS cho Featurephone)");
                    System.out.println(" [3] Kiem tra Phu kien co tuong thich voi dien thoai khong (isCompatibleWith)");
                    System.out.println(" [0] Quay lai Menu chinh");
                    System.out.print("➤ Chon tinh nang muon test (0-3): ");

                    String subChoice = sc.nextLine().trim();
                    switch (subChoice) {
                        case "1": {
                            System.out.println("\n>> TEST: THU CU DOI MOI (applyTradeInDiscount)");
                            System.out.print("Nhap ma ID Smartphone (vi du PRD01, PRD02): ");
                            String id = ProductManager.normalizeId(sc.nextLine().trim());
                            Product p = prd.findByID(id);
                            if (p == null) {
                                System.out.println("[X] Khong tim thay san pham co ma: " + id);
                            } else if (!(p instanceof Smartphone)) {
                                System.out.println("[X] San pham nay khong phai la Smartphone!");
                            } else {
                                Smartphone sp = (Smartphone) p;
                                System.out.printf("Dien thoai: %s | Gia niem yet: %,.0f VND%n", sp.getName(), sp.getPrice());
                                double tradeInPrice = sp.applyTradeInDiscount();
                                System.out.printf("[OK] Gia sau khi tro gia Thu cu doi moi (giam 10%%): %,.0f VND%n", tradeInPrice);
                            }
                            break;
                        }

                        case "2": {
                            System.out.println("\n>> TEST: PHIM KHAN CAP SOS (toggleEmergencySOS)");
                            System.out.print("Nhap ma ID Featurephone (vi du PRD04, PRD05): ");
                            String id = ProductManager.normalizeId(sc.nextLine().trim());
                            Product p = prd.findByID(id);
                            if (p == null) {
                                System.out.println("[X] Khong tim thay san pham co ma: " + id);
                            } else if (!(p instanceof Featurephone)) {
                                System.out.println("[X] San pham nay khong phai la Dien thoai pho thong (Featurephone)!");
                            } else {
                                Featurephone fp = (Featurephone) p;
                                System.out.printf("Dien thoai: %s | Trang thai SOS hien tai: %s%n",
                                        fp.getName(), fp.isSosModeEnable() ? "BAT" : "TAT");
                                System.out.println(">> Thuc hien dao trang thai phim SOS:");
                                fp.toggleEmergencySOS();
                            }
                            break;
                        }

                        case "3": {
                            System.out.println("\n>> TEST: TUONG THICH PHU KIEN (isCompatibleWith)");
                            System.out.print("Nhap ma ID Phu kien (vi du PRD06, PRD07, PRD08): ");
                            String id = ProductManager.normalizeId(sc.nextLine().trim());
                            Product p = prd.findByID(id);
                            if (p == null) {
                                System.out.println("[X] Khong tim thay phu kien co ma: " + id);
                            } else if (!(p instanceof Accessory)) {
                                System.out.println("[X] San pham nay khong phai la Phu kien (Accessory)!");
                            } else {
                                Accessory acc = (Accessory) p;
                                System.out.printf("Phu kien: %s (Loai: %s)%n", acc.getName(), acc.getAccessoryType());
                                System.out.print("Nhap ten dong dien thoai muon kiem tra (vi du: iPhone 15, Samsung S24): ");
                                String phoneName = sc.nextLine().trim();
                                acc.isCompatibleWith(phoneName);
                            }
                            break;
                        }

                        case "0":
                            System.out.println("[!] Quay lai Menu chinh.");
                            break;

                        default:
                            System.out.println("[X] Lua chon khong hop le!");
                            break;
                    }
                    break;
                }

                case 0:
                    System.out.println("\n[OK] Dang luu toan bo du lieu vao file va thoat Menu Kho...");
                    prd.saveToFile();
                    prd.saveDetailsToFile();
                    running = false;
                    break;

                default:
                    System.out.println("[X] Lua chon khong hop le! Vui long chon tu 0 den 9.");
                    break;
            }
        }
    }

    public static void main(String[] args) {
        WarehouseMenu menu = new WarehouseMenu();
        menu.displayMenu();
    }
}
