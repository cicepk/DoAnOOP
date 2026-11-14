package com.smartphonestore.view;

import com.smartphonestore.exception.ValidationException;
import com.smartphonestore.manager.ProductManager;
import com.smartphonestore.model.product.*;
import com.smartphonestore.util.DateUtil;
import com.smartphonestore.util.InputHelper;
import java.util.*;

public class WarehouseMenu {
    private final ProductManager prd;

    // Constructor duy nhất nhận ProductManager dùng chung cho toàn bộ hệ thống
    public WarehouseMenu(ProductManager prd) {
        this.prd = prd;
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

            int input = InputHelper.readInt("➤ Chon chuc nang (0-9): ", 0, 9);

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

                    int subChoice = InputHelper.readInt("➤ Chon che do xem (0-3): ", 0, 3);
                    List<ProductDetail> detailsToShow = new ArrayList<>();
                    String title = "TAT CA SAN PHAM TRONG KHO";

                    if (subChoice == 1) {
                        detailsToShow = prd.getAllProductDetails();
                        title = "TAT CA CON MAY TRONG KHO";
                    } else if (subChoice == 2) {
                        String brand = InputHelper.readString("Nhap ten hang muon xem (vi du Apple, Samsung, Xiaomi, Nokia): ");
                        detailsToShow = prd.getDetailsByBrand(brand);
                        title = "HANG SAN XUAT: " + brand.toUpperCase();
                    } else if (subChoice == 3) {
                        String rawId = InputHelper.readString("Nhap ma san pham (vi du PRD01, PRD02): ");
                        String pid = ProductManager.normalizeId(rawId);
                        detailsToShow = prd.getDetailsByProductId(pid);
                        Product p = prd.findByID(pid);
                        title = "MA SAN PHAM: " + pid.toUpperCase() + (p != null ? " (" + p.getName() + ")" : "");
                    } else {
                        break;
                    }

                    System.out.println("\n===============================================================================================================================");
                    System.out.println("                                       BANG CHI TIET CON MAY - " + title);
                    System.out.println("===============================================================================================================================");
                    System.out.printf(" %-7s │ %-24s │ %-8s │ %-18s │ %-19s │ %-14s │ %-12s │ %-9s%n",
                            "MA SP", "TEN SAN PHAM", "HANG", "SO SERI / IMEI", "NGAY GIO NHAP", "GIA NHAP", "NHA CUNG CAP", "TRANG THAI");
                    System.out.println("────────┼──────────────────────────┼──────────┼────────────────────┼─────────────────────┼────────────────┼──────────────┼──────────");

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
                        System.out.println("===============================================================================================================================");
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
                    String supplier = "FPT_Synnex";
                    if (!InputHelper.readYesNo("Su dung Nha cung cap mac dinh 'FPT_Synnex'? (Y/N): ")) {
                        supplier = InputHelper.readString("Nhap ten Nha cung cap: ");
                    }

                    String now = DateUtil.nowText();
                    String importDate = now;
                    if (!InputHelper.readYesNo("Su dung thoi gian nhap kho hien tai (" + now + ")? (Y/N): ")) {
                        while (true) {
                            String text = InputHelper.readString("Nhap ngay gio nhap kho [dd/MM/yyyy HH:mm:ss]: ");
                            try {
                                importDate = DateUtil.formatDateTime(DateUtil.parseDateTime(text)); // chi go ngay thi tu dong lay 00:00:00
                                break;
                            } catch (IllegalArgumentException e) {
                                System.out.println("[X] " + e.getMessage());
                            }
                        }
                    }

                    int sessionItemCount = 0;
                    int sessionTotalQty = 0;
                    double sessionTotalCost = 0;

                    // VÒNG LẶP WHILE NHẬP HÀNG LIÊN TỤC CHO ĐẾN KHI GÕ 0
                    while (true) {
                        System.out.println("\n--------------------------------------------------------------");
                        String rawId = InputHelper.readString("➤ Nhap Ma san pham muon nhap (vi du: PRD01, hoac go '0' de dung): ");

                        if ("0".equals(rawId)) {
                            System.out.println("[OK] Ket thuc cac mat hang trong phieu nhap!");
                            break;
                        }

                        String id = ProductManager.normalizeId(rawId);
                        Product p = prd.findByID(id);

                        if (p != null) {
                            // TRƯỜNG HỢP 1: SẢN PHẨM ĐÃ CÓ TRONG KHO
                            System.out.printf(">> [SAN PHAM CO SAN]: %s | Hang: %s | Ton hien tai: %d chiec%n",
                                     p.getName(), p.getBrand(), p.getStockQuantity());

                            int qty = InputHelper.readInt("Nhap so luong hang moi ve: ", 1, 100000);
                            double importPrice = InputHelper.readDouble("Nhap gia nhap moi chiec (VND): ", 1);

                            // Nhập số IMEI/Seri cho từng máy trong lô
                            List<ProductDetail> batchDetails = new ArrayList<>();
                            Set<String> batchSerials = new HashSet<>(); // serial da nhap trong lo nay
                            boolean autoSn = InputHelper.readYesNo(String.format("Ban co muon tu dong sinh Seri/IMEI cho ca lo %d may khong? (Y/N): ", qty));
                            for (int i = 1; i <= qty; i++) {
                                String serial;
                                if (autoSn) {
                                    int k = i;
                                    do { // sinh lai toi khi khong trung kho va khong trung lo
                                        serial = "IMEI" + id.replace("PRD", "") + (System.currentTimeMillis() % 1000000) + String.format("%02d", k++);
                                    } while (prd.isSerialExists(serial) || batchSerials.contains(serial));
                                    System.out.printf(" - May [%d/%d] -> Tu dong cap IMEI: %s%n", i, qty, serial);
                                } else {
                                    while (true) {
                                        serial = InputHelper.readString(String.format(" - Seri/IMEI may [%d/%d]: ", i, qty)).toUpperCase();
                                        if (prd.isSerialExists(serial) || batchSerials.contains(serial)) {
                                            System.out.println("   [X] Seri/IMEI nay da co trong kho hoac trong lo dang nhap! Vui long nhap lai.");
                                            continue;
                                        }
                                        break;
                                    }
                                }
                                batchSerials.add(serial);
                                batchDetails.add(new ProductDetail(p.getId(), p.getName(), serial, importDate, importPrice, supplier, ProductManager.IN_STOCK));
                            }

                            prd.addProductDetails(batchDetails);
                            prd.syncStockFromDetails(); // thay cho p.increaseStock(qty): ton kho = so serial IN_STOCK thực tế

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
                            boolean confirm = InputHelper.readYesNo("Ban co muon khai bao san pham moi nay luon khong? (Y/N): ");
                            if (!confirm) {
                                System.out.println("[!] Bo qua ma nay.");
                                continue;
                            }

                            System.out.println("\n--- KHAI BAO THONG SO SAN PHAM MOI ---");
                            System.out.println("Chon dong san pham:");
                            System.out.println(" [1] Smartphone (Dien thoai thong minh)");
                            System.out.println(" [2] Featurephone (Dien thoai pho thong)");
                            System.out.println(" [3] Accessory (Phu kien)");
                            int typeChoice = InputHelper.readInt("Chon (1-3): ", 1, 3);

                            String name = InputHelper.readString("Nhap ten san pham: ");
                            String brand = InputHelper.readString("Nhap hang san xuat (Apple, Samsung, Xiaomi...): ");
                            double sellPrice = InputHelper.readDouble("Nhap gia ban niem yet cho khach (VND): ", 1);
                            int threshold = InputHelper.readInt("Nhap nguong canh bao ton kho toi thieu: ", 0, 10000);
                            int qty = InputHelper.readInt("Nhap so luong nhap lo dau tien: ", 1, 100000);
                            double importPrice = InputHelper.readDouble("Nhap gia nhap von moi chiec (VND): ", 1);

                            Product newProduct = null;
                            try {
                                if (typeChoice == 1) {
                                    String os = InputHelper.readString("Nhap he dieu hanh (iOS / Android): ");
                                    int storage = InputHelper.readInt("Nhap dung luong ROM (GB, vi du 128, 256): ", 1, 2048);
                                    newProduct = new Smartphone(id, name, brand, sellPrice, qty, threshold, os, storage);
                                } else if (typeChoice == 2) {
                                    int battery = InputHelper.readInt("Nhap thoi luong pin cho (so ngay, vi du 7, 10): ", 1, 60);
                                    newProduct = new Featurephone(id, name, brand, sellPrice, qty, threshold, "KaiOS", battery);
                                } else {
                                    String accType;
                                    while (true) {
                                        accType = InputHelper.readString("Nhap loai phu kien (SAC, TAI_NGHE, OP_LUNG, CUONG_LUC): ").toUpperCase();
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
                            Set<String> batchSerials = new HashSet<>(); // serial da nhap trong lo nay
                            boolean autoSn = InputHelper.readYesNo(String.format("Ban co muon tu dong sinh Seri/IMEI cho ca lo %d may khong? (Y/N): ", qty));
                            for (int i = 1; i <= qty; i++) {
                                String serial;
                                if (autoSn) {
                                    int k = i;
                                    do { // sinh lai toi khi khong trung kho va khong trung lo
                                        serial = "IMEI" + id.replace("PRD", "") + (System.currentTimeMillis() % 1000000) + String.format("%02d", k++);
                                    } while (prd.isSerialExists(serial) || batchSerials.contains(serial));
                                    System.out.printf(" - May [%d/%d] -> Tu dong cap IMEI: %s%n", i, qty, serial);
                                } else {
                                    while (true) {
                                        serial = InputHelper.readString(String.format(" - Seri/IMEI may [%d/%d]: ", i, qty)).toUpperCase();
                                        if (prd.isSerialExists(serial) || batchSerials.contains(serial)) {
                                            System.out.println("   [X] Seri/IMEI nay da co trong kho hoac trong lo dang nhap! Vui long nhap lai.");
                                            continue;
                                        }
                                        break;
                                    }
                                }
                                batchSerials.add(serial);
                                batchDetails.add(new ProductDetail(id, name, serial, importDate, importPrice, supplier, ProductManager.IN_STOCK));
                            }

                            boolean added = prd.add(newProduct);
                            if (added) {
                                prd.addProductDetails(batchDetails);
                                prd.syncStockFromDetails(); // dong bo ton kho chinh xac tu serial IN_STOCK
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
                    String rawId = InputHelper.readString("Nhap ma ID san pham can sua (vi du PRD01): ");
                    String id = ProductManager.normalizeId(rawId);
                    Product p = prd.findByID(id);
                    if (p == null) {
                        System.out.println("[X] Khong tim thay san pham co ma: " + id);
                    } else {
                        System.out.println("Thong tin hien tai:");
                        p.displayInfo();
                        String newName = p.getName();
                        if (InputHelper.readYesNo("Ban co muon doi ten san pham khong? (Y/N): ")) {
                            newName = InputHelper.readString("Nhap ten moi: ");
                        }

                        double inputPrice = InputHelper.readDouble("Nhap gia moi (nhap 0 neu giu nguyen): ", 0);
                        double newPrice = (inputPrice == 0) ? p.getPrice() : inputPrice;

                        int inputThreshold = InputHelper.readInt("Nhap nguong ton kho moi (nhap -1 neu giu nguyen): ", -1, 10000);
                        int newThreshold = (inputThreshold == -1) ? p.getMinStockThreshold() : inputThreshold;

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
                    String rawId = InputHelper.readString("Nhap ma ID san pham can xoa: ");
                    String id = ProductManager.normalizeId(rawId);
                    Product p = prd.findByID(id);
                    if (p == null) {
                        System.out.println("[X] Khong tim thay san pham co ma: " + id);
                    } else {
                        System.out.println("San pham muon xoa:");
                        p.displayInfo();
                        boolean confirm = InputHelper.readYesNo("Ban co chac chan muon xoa khong? (Y/N): ");
                        if (confirm) {
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
                    String keyword = InputHelper.readString("Nhap tu khoa tim kiem (Ten may hoac Ten hang): ");
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
                    String rawId = InputHelper.readString("Nhap ma ID san pham muon tinh giam gia: ");
                    String id = ProductManager.normalizeId(rawId);
                    Product p = prd.findByID(id);
                    if (p == null) {
                        System.out.println("[X] Khong tim thay san pham co ma: " + id);
                    } else if (!(p instanceof IDiscountable)) {
                        System.out.println("[!] San pham nay (Featurephone) khong ho tro chiet khau IDiscountable!");
                    } else {
                        System.out.printf("San pham: %s | Gia goc: %,.0f VND%n", p.getName(), p.getPrice());
                        double rate = InputHelper.readDouble("Nhap ty le chiet khau (vi du 0.1 cho 10%, 0.2 cho 20%): ", 0.0, 1.0);

                        // [TC08] Trước đây gọi ngay prd.applyDiscount(): hàm này setPrice + saveToFile,
                        // nên giá gốc bị ghi đè và lần sau giảm chồng lên giá đã giảm
                        // (29.990.000 -> 26.991.000 -> 24.291.900).
                        // Giờ: chỉ TÍNH thử giá sau giảm (đa hình qua IDiscountable), hỏi Y/N rồi mới lưu.
                        double preview = ((IDiscountable) p).calculateDiscountedPrice(rate);
                        System.out.printf("Gia sau giam %.0f%%: %,.0f VND%n", rate * 100, preview);
                        if (preview <= 0 || preview == p.getPrice()) {
                            System.out.println("[X] Khong ap dung duoc chiet khau!");
                        } else if (InputHelper.readYesNo("Luu gia moi nay vao 'products.txt'? (Y/N): ")) {
                            prd.applyDiscount(p.getId(), rate);   // [TC08] chỉ đổi giá khi người dùng đồng ý
                            System.out.printf("[OK] Gia moi cua '%s': %,.0f VND (Da dong bo vao 'products.txt')%n", p.getName(), p.getPrice());
                        } else {
                            System.out.println("[OK] Chi xem thu, gia ban giu nguyen.");
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

                    int subChoice = InputHelper.readInt("➤ Chon tinh nang muon test (0-3): ", 0, 3);
                    switch (subChoice) {
                        case 1: {
                            System.out.println("\n>> TEST: THU CU DOI MOI (applyTradeInDiscount)");
                            String rawId = InputHelper.readString("Nhap ma ID Smartphone (vi du PRD01, PRD02): ");
                            String id = ProductManager.normalizeId(rawId);
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

                        case 2: {
                            System.out.println("\n>> TEST: PHIM KHAN CAP SOS (toggleEmergencySOS)");
                            String rawId = InputHelper.readString("Nhap ma ID Featurephone (vi du PRD04, PRD05): ");
                            String id = ProductManager.normalizeId(rawId);
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

                        case 3: {
                            System.out.println("\n>> TEST: TUONG THICH PHU KIEN (isCompatibleWith)");
                            String rawId = InputHelper.readString("Nhap ma ID Phu kien (vi du PRD38, PRD39, PRD41): ");
                            String id = ProductManager.normalizeId(rawId);
                            Product p = prd.findByID(id);
                            if (p == null) {
                                System.out.println("[X] Khong tim thay phu kien co ma: " + id);
                            } else if (!(p instanceof Accessory)) {
                                System.out.println("[X] San pham nay khong phai la Phu kien (Accessory)!");
                            } else {
                                Accessory acc = (Accessory) p;
                                System.out.printf("Phu kien: %s (Loai: %s)%n", acc.getName(), acc.getAccessoryType());
                                String phoneName = InputHelper.readString("Nhap ten dong dien thoai muon kiem tra (vi du: iPhone 15, Samsung S24): ");
                                acc.isCompatibleWith(phoneName);
                            }
                            break;
                        }

                        case 0:
                            System.out.println("[!] Quay lai Menu chinh.");
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
            }
        }
    }
}
