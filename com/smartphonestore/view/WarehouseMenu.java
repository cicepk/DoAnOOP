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
            System.out.println("             MENU NHÂN VIÊN KHO (THÀNH VIÊN 2)                ");
            System.out.println("==============================================================");
            System.out.println(" [1] Xem danh mục toàn bộ sản phẩm & Tồn kho");
            System.out.println(" [2] Xem bảng chi tiết từng máy (Serial/IMEI & Ngày nhập)");
            System.out.println(" [3] Lập phiếu nhập hàng vào kho (Nhập lô / Nhập mới)");
            System.out.println(" [4] Cập nhật thông tin / Giá bán sản phẩm theo Mã ID");
            System.out.println(" [5] Xóa sản phẩm khỏi danh mục kho theo Mã ID");
            System.out.println(" [6] Tìm kiếm sản phẩm theo Tên / Thương hiệu");
            System.out.println(" [7] Cảnh báo & Lọc danh sách hàng sắp hết (isLowStock)");
            System.out.println(" [8] Áp dụng chương trình chiết khấu sản phẩm (IDiscountable)");
            System.out.println(" [9] Kiểm tra tính năng độc quyền (Thu cũ đổi mới / SOS / Tương thích)");
            System.out.println(" [0] Thoát (Lưu toàn bộ dữ liệu)");
            System.out.println("==============================================================");

            int input = InputHelper.readInt("➤ Chọn chức năng (0-9): ", 0, 9);

            switch (input) {
                case 1: {
                    System.out.println("\n========== DANH MỤC TOÀN BỘ SẢN PHẨM TRONG KHO ==========");
                    List<Product> list = prd.getALL();
                    if (list.isEmpty()) {
                        System.out.println("[!] Kho hiện đang trống, chưa có sản phẩm nào!");
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
                    System.out.println("\n--- XEM BẢNG CHI TIẾT TỪNG MÁY TRONG KHO ---");
                    System.out.println(" [1] Xem tất cả chi tiết máy trong kho");
                    System.out.println(" [2] Xem chi tiết lọc theo Hãng (Apple, Samsung, Xiaomi...)");
                    System.out.println(" [3] Xem chi tiết theo Mã sản phẩm cụ thể (ví dụ PRD01)");
                    System.out.println(" [0] Quay lại Menu chính");

                    int subChoice = InputHelper.readInt("➤ Chọn chế độ xem (0-3): ", 0, 3);
                    List<ProductDetail> detailsToShow = new ArrayList<>();
                    String title = "TẤT CẢ SẢN PHẨM TRONG KHO";

                    if (subChoice == 1) {
                        detailsToShow = prd.getAllProductDetails();
                        title = "TẤT CẢ MÁY TRONG KHO";
                    } else if (subChoice == 2) {
                        String brand = InputHelper.readString("Nhập tên hãng muốn xem (ví dụ Apple, Samsung, Xiaomi, Nokia): ");
                        detailsToShow = prd.getDetailsByBrand(brand);
                        title = "HÃNG SẢN XUẤT: " + brand.toUpperCase();
                    } else if (subChoice == 3) {
                        String rawId = InputHelper.readString("Nhập mã sản phẩm (ví dụ PRD01, PRD02): ");
                        String pid = ProductManager.normalizeId(rawId);
                        detailsToShow = prd.getDetailsByProductId(pid);
                        Product p = prd.findByID(pid);
                        title = "MÃ SẢN PHẨM: " + pid.toUpperCase() + (p != null ? " (" + p.getName() + ")" : "");
                    } else {
                        break;
                    }

                    System.out.println("\n===============================================================================================================================");
                    System.out.println("                                       BẢNG CHI TIẾT TỪNG MÁY - " + title);
                    System.out.println("===============================================================================================================================");
                    System.out.printf(" %-7s │ %-24s │ %-8s │ %-18s │ %-19s │ %-14s │ %-12s │ %-9s%n",
                            "MÃ SP", "TÊN SẢN PHẨM", "HÃNG", "SỐ SERI / IMEI", "NGÀY GIỜ NHẬP", "GIÁ NHẬP", "NHÀ CUNG CẤP", "TRẠNG THÁI");
                    System.out.println("────────┼──────────────────────────┼──────────┼────────────────────┼─────────────────────┼────────────────┼──────────────┼──────────");

                    if (detailsToShow.isEmpty()) {
                        System.out.println("  [!] Không tìm thấy máy nào phù hợp trong dữ liệu chi tiết.");
                    } else {
                        int inStockCount = 0;
                        int soldCount = 0;
                        for (ProductDetail d : detailsToShow) {
                            Product p = prd.findByID(d.getProductId());
                            String pName = p != null ? p.getName() : "Không xác định";
                            String pBrand = p != null ? p.getBrand() : "N/A";
                            d.displayRow(pName, pBrand);
                            if ("IN_STOCK".equalsIgnoreCase(d.getStatus())) inStockCount++;
                            else soldCount++;
                        }
                        System.out.println("===============================================================================================================================");
                        System.out.printf("Tổng cộng: %d máy (Trong kho: %d | Đã xuất bán: %d)%n",
                                detailsToShow.size(), inStockCount, soldCount);
                    }
                    break;
                }

                // ==============================================================
                // CHỨC NĂNG 3: LẬP PHIẾU NHẬP HÀNG (VÒNG LẶP WHILE - NHẬP ĐẾN KHI GÕ 0)
                // ==============================================================
                case 3: {
                    System.out.println("\n==============================================================");
                    System.out.println("       LẬP PHIẾU NHẬP HÀNG VÀO KHO (GOODS RECEIPT NOTE)       ");
                    System.out.println("==============================================================");
                    String supplier = "FPT_Synnex";
                    if (!InputHelper.readYesNo("Sử dụng Nhà cung cấp mặc định 'FPT_Synnex'? (Y/N): ")) {
                        supplier = InputHelper.readText("Nhập tên Nhà cung cấp: ");
                    }

                    String now = DateUtil.nowText();
                    String importDate = now;
                    if (!InputHelper.readYesNo("Sử dụng thời gian nhập kho hiện tại (" + now + ")? (Y/N): ")) {
                        while (true) {
                            String text = InputHelper.readString("Nhập ngày giờ nhập kho [dd/MM/yyyy HH:mm:ss]: ");
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
                        String rawId = InputHelper.readString("➤ Nhập Mã sản phẩm muốn nhập (ví dụ: PRD01, hoặc gõ '0' để dừng): ");

                        if ("0".equals(rawId)) {
                            System.out.println("[OK] Kết thúc các mặt hàng trong phiếu nhập!");
                            break;
                        }

                        String id = ProductManager.normalizeId(rawId);
                        Product p = prd.findByID(id);

                        if (p != null) {
                            // TRƯỜNG HỢP 1: SẢN PHẨM ĐÃ CÓ TRONG KHO
                            System.out.printf(">> [SẢN PHẨM CÓ SẴN]: %s | Hãng: %s | Tồn hiện tại: %d chiếc%n",
                                     p.getName(), p.getBrand(), p.getStockQuantity());

                            int qty = InputHelper.readInt("Nhập số lượng hàng mới về: ", 1, 100000);
                            double importPrice = InputHelper.readDouble("Nhập giá nhập mỗi chiếc (VND): ", 1);

                            // Nhập số IMEI/Seri cho từng máy trong lô
                            List<ProductDetail> batchDetails = new ArrayList<>();
                            Set<String> batchSerials = new HashSet<>(); // serial da nhap trong lo nay
                            boolean autoSn = InputHelper.readYesNo(String.format("Bạn có muốn tự động sinh Seri/IMEI cho cả lô %d máy không? (Y/N): ", qty));
                            for (int i = 1; i <= qty; i++) {
                                String serial;
                                if (autoSn) {
                                    int k = i;
                                    do { // sinh lai toi khi khong trung kho va khong trung lo
                                        serial = "IMEI" + id.replace("PRD", "") + (System.currentTimeMillis() % 1000000) + String.format("%02d", k++);
                                    } while (prd.isSerialExists(serial) || batchSerials.contains(serial));
                                    System.out.printf(" - Máy [%d/%d] -> Tự động cấp IMEI: %s%n", i, qty, serial);
                                } else {
                                    while (true) {
                                        serial = InputHelper.readString(String.format(" - Seri/IMEI máy [%d/%d]: ", i, qty)).toUpperCase();
                                        if (prd.isSerialExists(serial) || batchSerials.contains(serial)) {
                                            System.out.println("   [X] Seri/IMEI này đã có trong kho hoặc trong lô đang nhập! Vui lòng nhập lại.");
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

                            System.out.printf("[OK] Nhập thêm thành công %d chiếc '%s'! Tồn kho mới: %d chiếc%n",
                                    qty, p.getName(), p.getStockQuantity());

                        } else {
                            // TRƯỜNG HỢP 2: SẢN PHẨM MỚI TOANH CHƯA CÓ TRONG KHO
                            if (!id.matches("^PRD\\d{2,}$")) {
                                String autoId = prd.generateNextID();
                                System.out.printf(">> [THÔNG TIN]: Mã '%s' chưa đúng chuẩn 'PRDxx'. Hệ thống tự động cấp mã: %s%n", id, autoId);
                                id = autoId;
                            }

                            System.out.printf(">> [SẢN PHẨM MỚI]: Mã '%s' chưa từng có trong kho!%n", id);
                            boolean confirm = InputHelper.readYesNo("Bạn có muốn khai báo sản phẩm mới này luôn không? (Y/N): ");
                            if (!confirm) {
                                System.out.println("[!] Bỏ qua mã này.");
                                continue;
                            }

                            System.out.println("\n--- KHAI BÁO THÔNG SỐ SẢN PHẨM MỚI ---");
                            System.out.println("Chọn dòng sản phẩm:");
                            System.out.println(" [1] Smartphone (Điện thoại thông minh)");
                            System.out.println(" [2] Featurephone (Điện thoại phổ thông)");
                            System.out.println(" [3] Accessory (Phụ kiện)");
                            int typeChoice = InputHelper.readInt("Chọn (1-3): ", 1, 3);

                            String name = InputHelper.readText("Nhập tên sản phẩm: ");
                            String brand = InputHelper.readText("Nhập hãng sản xuất (Apple, Samsung, Xiaomi...): ");
                            double sellPrice = InputHelper.readDouble("Nhập giá bán niêm yết cho khách (VND): ", 1);
                            int threshold = InputHelper.readInt("Nhập ngưỡng cảnh báo tồn kho tối thiểu: ", 0, 10000);
                            int qty = InputHelper.readInt("Nhập số lượng nhập lô đầu tiên: ", 1, 100000);
                            double importPrice = InputHelper.readDouble("Nhập giá nhập vốn mỗi chiếc (VND): ", 1);

                            Product newProduct = null;
                            try {
                                if (typeChoice == 1) {
                                    String os = InputHelper.readString("Nhập hệ điều hành (iOS / Android): ");
                                    int storage = InputHelper.readInt("Nhập dung lượng ROM (GB, ví dụ 128, 256): ", 1, 2048);
                                    newProduct = new Smartphone(id, name, brand, sellPrice, qty, threshold, os, storage);
                                } else if (typeChoice == 2) {
                                    int battery = InputHelper.readInt("Nhập thời lượng pin chờ (số ngày, ví dụ 7, 10): ", 1, 60);
                                    newProduct = new Featurephone(id, name, brand, sellPrice, qty, threshold, "KaiOS", battery);
                                } else {
                                    String accType;
                                    while (true) {
                                        accType = InputHelper.readString("Nhập loại phụ kiện (SAC, TAI_NGHE, OP_LUNG, CUONG_LUC): ").toUpperCase();
                                        if (accType.matches("^(SAC|TAI_NGHE|OP_LUNG|CUONG_LUC)$")) break;
                                        System.out.println("[X] Chỉ được chọn: SAC, TAI_NGHE, OP_LUNG hoặc CUONG_LUC!");
                                    }
                                    newProduct = new Accessory(id, name, brand, sellPrice, qty, threshold, accType);
                                }
                            } catch (ValidationException ve) {
                                System.out.println(ve.getMessage());
                                System.out.println("[X] Tạo sản phẩm mới thất bại do dữ liệu không hợp lệ!");
                                continue;
                            } catch (Exception ex) {
                                System.out.println("[X] Lỗi khi khởi tạo sản phẩm: " + ex.getMessage());
                                continue;
                            }

                            // Nhập danh sách IMEI/Seri cho sản phẩm mới
                            List<ProductDetail> batchDetails = new ArrayList<>();
                            Set<String> batchSerials = new HashSet<>(); // serial da nhap trong lo nay
                            boolean autoSn = InputHelper.readYesNo(String.format("Bạn có muốn tự động sinh Seri/IMEI cho cả lô %d máy không? (Y/N): ", qty));
                            for (int i = 1; i <= qty; i++) {
                                String serial;
                                if (autoSn) {
                                    int k = i;
                                    do { // sinh lai toi khi khong trung kho va khong trung lo
                                        serial = "IMEI" + id.replace("PRD", "") + (System.currentTimeMillis() % 1000000) + String.format("%02d", k++);
                                    } while (prd.isSerialExists(serial) || batchSerials.contains(serial));
                                    System.out.printf(" - Máy [%d/%d] -> Tự động cấp IMEI: %s%n", i, qty, serial);
                                } else {
                                    while (true) {
                                        serial = InputHelper.readString(String.format(" - Seri/IMEI máy [%d/%d]: ", i, qty)).toUpperCase();
                                        if (prd.isSerialExists(serial) || batchSerials.contains(serial)) {
                                            System.out.println("   [X] Seri/IMEI này đã có trong kho hoặc trong lô đang nhập! Vui lòng nhập lại.");
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
                                System.out.printf("[OK] Đã tạo thành công sản phẩm mới '%s' (Mã: %s) và lưu %d máy vào kho!%n", name, id, qty);
                            } else {
                                System.out.println("[X] Lỗi khi lưu sản phẩm mới!");
                            }
                        }
                    }

                    // TỔNG KẾT PHIẾU NHẬP SAU KHI THOÁT VÒNG LẶP WHILE
                    if (sessionTotalQty > 0) {
                        System.out.println("\n==============================================================");
                        System.out.println("       BIÊN BẢN TỔNG KẾT PHIẾU NHẬP HÀNG (GOODS RECEIPT)      ");
                        System.out.println("==============================================================");
                        System.out.println(" Nhà cung cấp  : " + supplier);
                        System.out.println(" Ngày nhập     : " + importDate);
                        System.out.println(" Số dòng mặt hàng: " + sessionItemCount + " dòng");
                        System.out.println(" Tổng số máy nhập: " + sessionTotalQty + " chiếc");
                        System.out.printf(" Tổng tiền nhập: %,.0f VND%n", sessionTotalCost);
                        System.out.println("==============================================================");
                        System.out.println("[OK] Dữ liệu đã được đồng bộ vào 'products.txt' và 'product_details.txt'!");
                    } else {
                        System.out.println("[!] Không có sản phẩm nào được nhập trong phiên này.");
                    }
                    break;
                }

                case 4: {
                    System.out.println("\n--- CẬP NHẬT THÔNG TIN / GIÁ BÁN SẢN PHẨM ---");
                    String rawId = InputHelper.readString("Nhập mã ID sản phẩm cần sửa (ví dụ PRD01): ");
                    String id = ProductManager.normalizeId(rawId);
                    Product p = prd.findByID(id);
                    if (p == null) {
                        System.out.println("[X] Không tìm thấy sản phẩm có mã: " + id);
                    } else {
                        System.out.println("Thông tin hiện tại:");
                        p.displayInfo();
                        String newName = p.getName();
                        if (InputHelper.readYesNo("Bạn có muốn đổi tên sản phẩm không? (Y/N): ")) {
                            newName = InputHelper.readText("Nhập tên mới: ");
                        }

                        double inputPrice = InputHelper.readDouble("Nhập giá mới (nhập 0 nếu giữ nguyên): ", 0);
                        double newPrice = (inputPrice == 0) ? p.getPrice() : inputPrice;

                        int inputThreshold = InputHelper.readInt("Nhập ngưỡng tồn kho mới (nhập -1 nếu giữ nguyên): ", -1, 10000);
                        int newThreshold = (inputThreshold == -1) ? p.getMinStockThreshold() : inputThreshold;

                        boolean ok = prd.updateProductInfo(p.getId(), newName, newPrice, newThreshold);
                        if (ok) {
                            System.out.println("[OK] Cập nhật thông tin sản phẩm thành công!");
                        } else {
                            System.out.println("[X] Cập nhật thất bại!");
                        }
                    }
                    break;
                }

                case 5: {
                    System.out.println("\n--- XÓA SẢN PHẨM KHỎI KHO THEO MÃ ID ---");
                    String rawId = InputHelper.readString("Nhập mã ID sản phẩm cần xóa: ");
                    String id = ProductManager.normalizeId(rawId);
                    Product p = prd.findByID(id);
                    if (p == null) {
                        System.out.println("[X] Không tìm thấy sản phẩm có mã: " + id);
                    } else {
                        System.out.println("Sản phẩm muốn xóa:");
                        p.displayInfo();
                        boolean confirm = InputHelper.readYesNo("Bạn có chắc chắn muốn xóa không? (Y/N): ");
                        if (confirm) {
                            boolean deleted = prd.deleteID(p.getId());
                            if (deleted) {
                                System.out.println("[OK] Đã xóa sản phẩm và toàn bộ chi tiết máy liên quan khỏi kho!");
                            } else {
                                System.out.println("[X] Xóa thất bại!");
                            }
                        } else {
                            System.out.println("[!] Đã hủy thao tác xóa.");
                        }
                    }
                    break;
                }

                case 6: {
                    System.out.println("\n--- TÌM KIẾM SẢN PHẨM THEO TÊN / THƯƠNG HIỆU ---");
                    String keyword = InputHelper.readString("Nhập từ khóa tìm kiếm (Tên máy hoặc Tên hãng): ");
                    List<Product> results = prd.findByNameOrBrand(keyword);
                    if (results.isEmpty()) {
                        System.out.println("[!] Không tìm thấy sản phẩm nào khớp với từ khóa: " + keyword);
                    } else {
                        System.out.printf("Tìm thấy %d sản phẩm phù hợp:%n", results.size());
                        for (Product p : results) {
                            p.displayInfo();
                        }
                    }
                    break;
                }

                case 7: {
                    System.out.println("\n--- CẢNH BÁO: DANH SÁCH SẢN PHẨM SẮP HẾT HÀNG ---");
                    List<Product> lowStock = prd.getLowStockProducts();
                    if (lowStock.isEmpty()) {
                        System.out.println("[OK] Tồn kho an toàn! Hiện không có sản phẩm nào chạm ngưỡng hết hàng.");
                    } else {
                        System.out.printf("[!] CÓ %d SẢN PHẨM ĐANG CHẠM HOẶC DƯỚI NGƯỠNG BÁO ĐỘNG:%n", lowStock.size());
                        for (Product p : lowStock) {
                            p.displayInfo();
                        }
                    }
                    break;
                }

                case 8: {
                    System.out.println("\n--- TÍNH GIÁ CHIẾT KHẤU KHUYẾN MÃI (IDiscountable) ---");
                    String rawId = InputHelper.readString("Nhập mã ID sản phẩm muốn tính giảm giá: ");
                    String id = ProductManager.normalizeId(rawId);
                    Product p = prd.findByID(id);
                    if (p == null) {
                        System.out.println("[X] Không tìm thấy sản phẩm có mã: " + id);
                    } else if (!(p instanceof IDiscountable)) {
                        System.out.println("[!] Sản phẩm này (Featurephone) không hỗ trợ chiết khấu IDiscountable!");
                    } else {
                        System.out.printf("Sản phẩm: %s | Giá gốc: %,.0f VND%n", p.getName(), p.getPrice());
                        double rate = InputHelper.readDouble("Nhập tỷ lệ chiết khấu (ví dụ 0.1 cho 10%, 0.2 cho 20%): ", 0.0, 1.0);

                        // [TC08] Trước đây gọi ngay prd.applyDiscount(): hàm này setPrice + saveToFile,
                        // nên giá gốc bị ghi đè và lần sau giảm chồng lên giá đã giảm
                        // (29.990.000 -> 26.991.000 -> 24.291.900).
                        // Giờ: chỉ TÍNH thử giá sau giảm (đa hình qua IDiscountable), hỏi Y/N rồi mới lưu.
                        double preview = ((IDiscountable) p).calculateDiscountedPrice(rate);
                        System.out.printf("Giá sau giảm %.0f%%: %,.0f VND%n", rate * 100, preview);
                        if (preview <= 0 || preview == p.getPrice()) {
                            System.out.println("[X] Không áp dụng được chiết khấu!");
                        } else if (InputHelper.readYesNo("Lưu giá mới này vào 'products.txt'? (Y/N): ")) {
                            prd.applyDiscount(p.getId(), rate);   // [TC08] chỉ đổi giá khi người dùng đồng ý
                            System.out.printf("[OK] Giá mới của '%s': %,.0f VND (Đã đồng bộ vào 'products.txt')%n", p.getName(), p.getPrice());
                        } else {
                            System.out.println("[OK] Chỉ xem thử, giá bán giữ nguyên.");
                        }
                    }
                    break;
                }

                // ==========================================
                // CASE 9: TEST TÍNH NĂNG ĐỘC QUYỀN
                // ==========================================
                case 9: {
                    System.out.println("\n--- KIỂM TRA TÍNH NĂNG ĐỘC QUYỀN CỦA TỪNG DÒNG SẢN PHẨM ---");
                    System.out.println(" [1] Thu cũ đổi mới (applyTradeInDiscount: Giảm 10% cho Smartphone)");
                    System.out.println(" [2] Bật / Tắt chế độ khẩn cấp SOS (toggleEmergencySOS cho Featurephone)");
                    System.out.println(" [3] Kiểm tra Phụ kiện có tương thích với điện thoại không (isCompatibleWith)");
                    System.out.println(" [0] Quay lại Menu chính");

                    int subChoice = InputHelper.readInt("➤ Chọn tính năng muốn test (0-3): ", 0, 3);
                    switch (subChoice) {
                        case 1: {
                            System.out.println("\n>> TEST: THU CŨ ĐỔI MỚI (applyTradeInDiscount)");
                            String rawId = InputHelper.readString("Nhập mã ID Smartphone (ví dụ PRD01, PRD02): ");
                            String id = ProductManager.normalizeId(rawId);
                            Product p = prd.findByID(id);
                            if (p == null) {
                                System.out.println("[X] Không tìm thấy sản phẩm có mã: " + id);
                            } else if (!(p instanceof Smartphone)) {
                                System.out.println("[X] Sản phẩm này không phải là Smartphone!");
                            } else {
                                Smartphone sp = (Smartphone) p;
                                System.out.printf("Điện thoại: %s | Giá niêm yết: %,.0f VND%n", sp.getName(), sp.getPrice());
                                double tradeInPrice = sp.applyTradeInDiscount();
                                System.out.printf("[OK] Giá sau khi trợ giá Thu cũ đổi mới (giảm 10%%): %,.0f VND%n", tradeInPrice);
                            }
                            break;
                        }

                        case 2: {
                            System.out.println("\n>> TEST: PHÍM KHẨN CẤP SOS (toggleEmergencySOS)");
                            String rawId = InputHelper.readString("Nhập mã ID Featurephone (ví dụ PRD26, PRD27): ");
                            String id = ProductManager.normalizeId(rawId);
                            Product p = prd.findByID(id);
                            if (p == null) {
                                System.out.println("[X] Không tìm thấy sản phẩm có mã: " + id);
                            } else if (!(p instanceof Featurephone)) {
                                System.out.println("[X] Sản phẩm này không phải là Điện thoại phổ thông (Featurephone)!");
                            } else {
                                Featurephone fp = (Featurephone) p;
                                System.out.printf("Điện thoại: %s | Trạng thái SOS hiện tại: %s%n",
                                        fp.getName(), fp.isSosModeEnable() ? "BẬT" : "TẮT");
                                System.out.println(">> Thực hiện đảo trạng thái phím SOS:");
                                fp.toggleEmergencySOS();
                            }
                            break;
                        }

                        case 3: {
                            System.out.println("\n>> TEST: TƯƠNG THÍCH PHỤ KIỆN (isCompatibleWith)");
                            String rawId = InputHelper.readString("Nhập mã ID Phụ kiện (ví dụ PRD38, PRD39, PRD41): ");
                            String id = ProductManager.normalizeId(rawId);
                            Product p = prd.findByID(id);
                            if (p == null) {
                                System.out.println("[X] Không tìm thấy phụ kiện có mã: " + id);
                            } else if (!(p instanceof Accessory)) {
                                System.out.println("[X] Sản phẩm này không phải là Phụ kiện (Accessory)!");
                            } else {
                                Accessory acc = (Accessory) p;
                                System.out.printf("Phụ kiện: %s (Loại: %s)%n", acc.getName(), acc.getAccessoryType());
                                String phoneName = InputHelper.readString("Nhập tên dòng điện thoại muốn kiểm tra (ví dụ: iPhone 15, Samsung S24): ");
                                acc.isCompatibleWith(phoneName);
                            }
                            break;
                        }

                        case 0:
                            System.out.println("[!] Quay lại Menu chính.");
                            break;
                    }
                    break;
                }

                case 0:
                    System.out.println("\n[OK] Đang lưu toàn bộ dữ liệu vào file và thoát Menu Kho...");
                    prd.saveToFile();
                    prd.saveDetailsToFile();
                    running = false;
                    break;
            }
        }
    }
}