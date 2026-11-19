package com.smartphonestore.view;

import com.smartphonestore.exception.ValidationException;
import com.smartphonestore.manager.ProductManager;
import com.smartphonestore.model.auth.Account;
import com.smartphonestore.model.enums.Role;
import com.smartphonestore.model.product.*;
import com.smartphonestore.util.DateUtil;
import com.smartphonestore.util.FileIO;
import com.smartphonestore.util.InputHelper;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * Phân hệ Quản lý Kho hàng & Danh mục Sản phẩm (WMS - Warehouse Management System).
 * Chịu trách nhiệm toàn bộ nghiệp vụ quản lý sản phẩm, chi tiết Serial/IMEI,
 * kiểm kê đối chiếu tồn kho, lập phiếu nhập hàng và chính sách giá chiết khấu.
 */
public class WarehouseMenu {
    private final ProductManager prd;
    private final Account currentUser;

    // Constructor chuẩn hệ thống nhận ProductManager và Account của ca đăng nhập
    public WarehouseMenu(ProductManager prd, Account currentUser) {
        this.prd = prd;
        this.currentUser = currentUser;
    }

    // Constructor overload tiện ích (dùng khi chạy test độc lập hoặc tích hợp nhanh)
    public WarehouseMenu(ProductManager prd) {
        this(prd, new Account("ACC02", "kho01", "kho123", Role.WAREHOUSE, "ACTIVE", DateUtil.nowText(), "EMP02"));
    }

    // Hàm main khởi chạy độc lập Module Kho hàng để kiểm thử nghiệp vụ
    public static void main(String[] args) {
        ProductManager manager = new ProductManager();
        System.out.println("==============================================================");
        System.out.println("      KHỞI CHẠY THỬ NGHIỆM ĐỘC LẬP PHÂN HỆ QUẢN LÝ KHO HÀNG   ");
        System.out.println("==============================================================");
        System.out.println(" Lựa chọn vai trò người dùng để kiểm thử:");
        System.out.println("  [1] Nhân viên kho (Role: WAREHOUSE - Ẩn các chức năng ★ Quản trị)");
        System.out.println("  [2] Quản trị viên (Role: ADMIN - Toàn quyền truy cập)");
        int choice = InputHelper.readInt("➤ Chọn vai trò test (1-2, mặc định 1): ", 1, 2);

        Account sessionUser;
        if (choice == 2) {
            sessionUser = new Account("ACC01", "admin", "admin123", Role.ADMIN, "ACTIVE", DateUtil.nowText(), "EMP01");
        } else {
            sessionUser = new Account("ACC02", "kho01", "kho123", Role.WAREHOUSE, "ACTIVE", DateUtil.nowText(), "EMP02");
        }

        new WarehouseMenu(manager, sessionUser).displayMenu();
    }

    // Kiểm tra quyền Quản trị viên (Admin)
    private boolean isAdmin() {
        return currentUser != null && currentUser.getRole() == Role.ADMIN;
    }

    // =========================================================================
    // MENU CHÍNH: HỆ THỐNG QUẢN LÝ KHO HÀNG
    // =========================================================================
    public void displayMenu() {
        boolean running = true;
        while (running) {
            String roleName = (currentUser != null && currentUser.getRole() != null)
                    ? currentUser.getRole().getDisplayName() : "Nhân viên kho";
            String userName = (currentUser != null) ? currentUser.getUsername() : "kho01";
            String staffId = (currentUser != null && currentUser.getLinkedId() != null)
                    ? currentUser.getLinkedId() : "EMP02";

            System.out.println("\n========================================================================");
            System.out.println("             HỆ THỐNG QUẢN LÝ KHO HÀNG & SẢN PHẨM (WMS)                 ");
            System.out.println("========================================================================");
            System.out.printf(" Tài khoản: %-12s │ Vai trò: %-18s │ Mã ca trực: %s%n",
                    userName, roleName, staffId);
            System.out.println("------------------------------------------------------------------------");
            System.out.println(" [1] Quản lý Danh mục & Thông tin Sản phẩm");
            System.out.println(" [2] Quản lý Nghiệp vụ Nhập kho Hàng hóa");
            System.out.println(" [3] Giám sát & Cảnh báo Tồn kho Tối thiểu");
            System.out.println(" [4] Kiểm kê & Đối chiếu Tồn kho Thực tế");
            if (isAdmin()) {
                System.out.println(" [5] ★ Quản lý Chính sách Chiết khấu & Khuyến mãi");
            }
            System.out.println(" [6] Thiết lập Bảo mật & Đổi mật khẩu Cá nhân");
            System.out.println(" [0] Đăng xuất ca làm việc / Quay lại Menu trước");
            System.out.println("========================================================================");

            int choice = InputHelper.readInt("➤ Lựa chọn chức năng: ", 0, 6);

            switch (choice) {
                case 1:
                    manageCatalogMenu();
                    break;
                case 2:
                    manageImportMenu();
                    break;
                case 3:
                    showLowStockAlert();
                    break;
                case 4:
                    auditAndReconcileStock();
                    break;
                case 5:
                    if (isAdmin()) {
                        manageDiscountMenu();
                    } else {
                        System.out.println("\n[!] CẢNH BÁO: Chức năng này yêu cầu quyền Quản trị viên (Admin)!");
                    }
                    break;
                case 6:
                    handleChangePassword();
                    break;
                case 0:
                    System.out.println("\n[OK] Đang tự động lưu toàn bộ dữ liệu kho hàng...");
                    prd.saveToFile();
                    prd.saveDetailsToFile();
                    prd.saveReceiptsToFile();
                    System.out.println("[OK] Dữ liệu an toàn. Kết thúc phiên làm việc Kho hàng!");
                    running = false;
                    break;
            }
        }
    }

    // =========================================================================
    // PHÂN HỆ 1: QUẢN LÝ DANH MỤC & THÔNG TIN SẢN PHẨM
    // =========================================================================
    private void manageCatalogMenu() {
        boolean inCatalog = true;
        while (inCatalog) {
            System.out.println("\n------------------------------------------------------------------------");
            System.out.println("                 QUẢN LÝ DANH MỤC & THÔNG TIN SẢN PHẨM                  ");
            System.out.println("------------------------------------------------------------------------");
            System.out.println(" [1] Tra cứu toàn bộ Danh mục Sản phẩm (Tồn kho & Giá bán niêm yết)");
            System.out.println(" [2] Tra cứu Chi tiết Thiết bị (Số Serial / IMEI & Lịch sử nhập)");
            System.out.println(" [3] Tra cứu & Tìm kiếm Sản phẩm theo Tên / Hãng sản xuất");
            System.out.println(" [4] Khai báo & Thêm Sản phẩm mới vào Danh mục");
            System.out.println(" [5] Hiệu chỉnh Thông tin & Giá bán Niêm yết Sản phẩm");
            System.out.println(" [6] Kiểm tra Đặc tính Kỹ thuật Sản phẩm (SOS / Thu cũ / Tương thích)");
            if (isAdmin()) {
                System.out.println(" [7] ★ Thu hồi & Xóa Sản phẩm khỏi Danh mục");
            }
            System.out.println(" [0] Quay lại Menu chính");
            System.out.println("------------------------------------------------------------------------");

            int choice = InputHelper.readInt("➤ Chọn chức năng (0-" + (isAdmin() ? "7" : "6") + "): ", 0, 7);

            switch (choice) {
                case 1:
                    showCatalog();
                    break;
                case 2:
                    showDetailSerials();
                    break;
                case 3:
                    handleSearchProduct();
                    break;
                case 4:
                    handleAddProduct();
                    break;
                case 5:
                    handleUpdateProduct();
                    break;
                case 6:
                    handleSpecialFeatures();
                    break;
                case 7:
                    if (isAdmin()) {
                        handleDeleteProduct();
                    } else {
                        System.out.println("\n[!] CẢNH BÁO: Bạn không có quyền xóa sản phẩm. Chức năng chỉ dành cho Admin!");
                    }
                    break;
                case 0:
                    inCatalog = false;
                    break;
            }
        }
    }

    // 1.1 Tra cứu toàn bộ danh mục sản phẩm
    private void showCatalog() {
        System.out.println("\n=========================================================================================================");
        System.out.println("                                  DANH MỤC TOÀN BỘ SẢN PHẨM TRONG KHO                                    ");
        System.out.println("=========================================================================================================");
        List<Product> list = prd.getALL();
        if (list.isEmpty()) {
            System.out.println(" [!] Kho hàng hiện đang trống, chưa có sản phẩm nào được lưu trữ!");
        } else {
            for (Product p : list) {
                p.displayInfo();
            }
            System.out.println("=========================================================================================================");
            System.out.printf(" Tổng số dòng sản phẩm đang quản lý: %d dòng sản phẩm.%n", list.size());
        }
    }

    // 1.2 Tra cứu chi tiết từng thiết bị (Số Serial / IMEI)
    private void showDetailSerials() {
        System.out.println("\n--------------------------------------------------------------");
        System.out.println("          TRA CỨU CHI TIẾT THIẾT BỊ THEO SỐ SERIAL / IMEI      ");
        System.out.println("--------------------------------------------------------------");
        System.out.println(" [1] Tra cứu toàn bộ thiết bị vật lý trong kho");
        System.out.println(" [2] Lọc danh sách thiết bị theo Hãng (Apple, Samsung, Xiaomi...)");
        System.out.println(" [3] Lọc danh sách thiết bị theo Mã sản phẩm (ví dụ PRD01)");
        System.out.println(" [0] Quay lại Menu trước");

        int mode = InputHelper.readInt("➤ Chọn chế độ lọc (0-3): ", 0, 3);
        List<ProductDetail> details;
        String title;

        if (mode == 1) {
            details = prd.getAllProductDetails();
            title = "TẤT CẢ THIẾT BỊ VẬT LÝ TRONG KHO";
        } else if (mode == 2) {
            String brand = InputHelper.readString("Nhập tên hãng sản xuất cần lọc (Apple, Samsung, Xiaomi, Nokia...): ");
            details = prd.getDetailsByBrand(brand);
            title = "HÃNG SẢN XUẤT: " + brand.toUpperCase();
        } else if (mode == 3) {
            String rawId = InputHelper.readString("Nhập mã sản phẩm cần xem (ví dụ PRD01, PRD02): ");
            String pid = ProductManager.normalizeId(rawId);
            details = prd.getDetailsByProductId(pid);
            Product p = prd.findByID(pid);
            title = "MÃ SẢN PHẨM: " + pid.toUpperCase() + (p != null ? " (" + p.getName() + ")" : "");
        } else {
            return;
        }

        System.out.println("\n=================================================================================================================================");
        System.out.println("                                         BẢNG CHI TIẾT THIẾT BỊ - " + title);
        System.out.println("=================================================================================================================================");
        System.out.printf(" %-7s │ %-26s │ %-8s │ %-18s │ %-19s │ %-14s │ %-14s │ %-10s%n",
                "MÃ SP", "TÊN SẢN PHẨM", "HÃNG", "SỐ SERI / IMEI", "NGÀY GIỜ NHẬP", "GIÁ NHẬP VỐN", "NHÀ CUNG CẤP", "TRẠNG THÁI");
        System.out.println("────────┼────────────────────────────┼──────────┼────────────────────┼─────────────────────┼────────────────┼────────────────┼───────────");

        if (details.isEmpty()) {
            System.out.println("  [!] Không tìm thấy thiết bị nào khớp với điều kiện tra cứu.");
        } else {
            int inStockCount = 0;
            int soldCount = 0;
            int warrantyCount = 0;
            int tradeInCount = 0;

            for (ProductDetail d : details) {
                Product p = prd.findByID(d.getProductId());
                String pName = (p != null) ? p.getName() : d.getProductName();
                String pBrand = (p != null) ? p.getBrand() : "N/A";
                d.displayRow(pName, pBrand);

                if (ProductManager.IN_STOCK.equalsIgnoreCase(d.getStatus())) inStockCount++;
                else if (ProductManager.SOLD.equalsIgnoreCase(d.getStatus())) soldCount++;
                else if (ProductManager.WARRANTY.equalsIgnoreCase(d.getStatus())) warrantyCount++;
                else if (ProductManager.TRADE_IN.equalsIgnoreCase(d.getStatus())) tradeInCount++;
            }

            System.out.println("=================================================================================================================================");
            System.out.printf("Tổng cộng: %d thiết bị │ [Trong kho: %d] │ [Đã xuất bán: %d] │ [Đang bảo hành: %d]%s%n",
                    details.size(), inStockCount, soldCount, warrantyCount,
                    (tradeInCount > 0 ? " │ [Máy thu cũ: " + tradeInCount + "]" : ""));
        }
    }

    // 1.3 Tìm kiếm sản phẩm theo tên hoặc hãng sản xuất
    private void handleSearchProduct() {
        System.out.println("\n--------------------------------------------------------------");
        System.out.println("         TRA CỨU & TÌM KIẾM SẢN PHẨM TRONG DANH MỤC           ");
        System.out.println("--------------------------------------------------------------");
        String keyword = InputHelper.readString("➤ Nhập từ khóa tìm kiếm (Tên sản phẩm hoặc Hãng sản xuất): ");
        List<Product> results = prd.findByNameOrBrand(keyword);

        if (results.isEmpty()) {
            System.out.println(" [!] Không tìm thấy sản phẩm nào khớp với từ khóa: \"" + keyword + "\"");
        } else {
            System.out.printf(" [OK] Tìm thấy %d sản phẩm phù hợp:%n", results.size());
            for (Product p : results) {
                p.displayInfo();
            }
        }
    }

    // 1.4 Khai báo & Thêm sản phẩm mới vào danh mục
    private void handleAddProduct() {
        System.out.println("\n--------------------------------------------------------------");
        System.out.println("              KHAI BÁO SẢN PHẨM MỚI VÀO DANH MỤC              ");
        System.out.println("--------------------------------------------------------------");
        String nextId = prd.generateNextID();
        System.out.println(" [Gợi ý] Mã sản phẩm tiếp theo trong hệ thống: " + nextId);
        String rawId = InputHelper.readString("Nhập mã sản phẩm (nhấn Enter để lấy " + nextId + "): ");
        String id = (rawId == null || rawId.trim().isEmpty()) ? nextId : ProductManager.normalizeId(rawId);

        if (prd.findByID(id) != null) {
            System.out.println("[X] Lỗi: Mã sản phẩm " + id + " đã tồn tại trong danh mục!");
            return;
        }

        System.out.println("Chọn phân loại dòng sản phẩm:");
        System.out.println(" [1] Smartphone (Điện thoại thông minh)");
        System.out.println(" [2] Featurephone (Điện thoại phổ thông)");
        System.out.println(" [3] Accessory (Phụ kiện điện tử)");
        int typeChoice = InputHelper.readInt("➤ Phân loại (1-3): ", 1, 3);

        String name = InputHelper.readString("Nhập tên sản phẩm: ");
        String brand = InputHelper.readString("Nhập hãng sản xuất (Apple, Samsung, Xiaomi...): ");
        double sellPrice = InputHelper.readDouble("Nhập giá niêm yết cho khách hàng (VND): ", 1);
        int threshold = InputHelper.readInt("Nhập định mức tồn kho an toàn tối thiểu: ", 0, 10000);

        Product newProduct = null;
        try {
            if (typeChoice == 1) {
                String os = InputHelper.readString("Nhập hệ điều hành (iOS / Android): ");
                int storage = InputHelper.readInt("Nhập dung lượng ROM (GB, ví dụ 128, 256): ", 1, 2048);
                newProduct = new Smartphone(id, name, brand, sellPrice, 0, threshold, os, storage);
            } else if (typeChoice == 2) {
                int battery = InputHelper.readInt("Nhập thời lượng pin chờ (số ngày, ví dụ 7, 10): ", 1, 60);
                newProduct = new Featurephone(id, name, brand, sellPrice, 0, threshold, "KaiOS", battery);
            } else {
                String accType;
                while (true) {
                    accType = InputHelper.readString("Nhập loại phụ kiện (SAC, TAI_NGHE, OP_LUNG, CUONG_LUC): ").toUpperCase();
                    if (accType.matches("^(SAC|TAI_NGHE|OP_LUNG|CUONG_LUC)$")) break;
                    System.out.println("[X] Phụ kiện chỉ thuộc một trong các loại: SAC, TAI_NGHE, OP_LUNG, CUONG_LUC!");
                }
                newProduct = new Accessory(id, name, brand, sellPrice, 0, threshold, accType);
            }
        } catch (ValidationException ve) {
            System.out.println(ve.getMessage());
            System.out.println("[X] Khai báo thất bại do dữ liệu không hợp lệ!");
            return;
        } catch (Exception ex) {
            System.out.println("[X] Lỗi khởi tạo: " + ex.getMessage());
            return;
        }

        boolean ok = prd.add(newProduct);
        if (ok) {
            System.out.printf("[OK] Khai báo thành công sản phẩm '%s' (Mã: %s)!%n", name, id);
            System.out.println("     (Sản phẩm hiện có tồn kho = 0. Bạn có thể dùng chức năng 'Nhập kho' để nhập lô hàng đầu tiên).");
        } else {
            System.out.println("[X] Thêm sản phẩm thất bại!");
        }
    }

    // 1.5 Hiệu chỉnh thông tin & giá bán sản phẩm
    private void handleUpdateProduct() {
        System.out.println("\n--------------------------------------------------------------");
        System.out.println("           HIỆU CHỈNH THÔNG TIN & GIÁ BÁN NIÊM YẾT            ");
        System.out.println("--------------------------------------------------------------");
        String rawId = InputHelper.readString("➤ Nhập mã ID sản phẩm cần chỉnh sửa (ví dụ PRD01): ");
        String id = ProductManager.normalizeId(rawId);
        Product p = prd.findByID(id);

        if (p == null) {
            System.out.println("[X] Không tìm thấy sản phẩm có mã: " + id);
            return;
        }

        System.out.println("Thông tin sản phẩm hiện tại:");
        p.displayInfo();

        String newName = p.getName();
        if (InputHelper.readYesNo("Bạn có muốn đổi tên sản phẩm không? (Y/N): ")) {
            newName = InputHelper.readString("Nhập tên thương mại mới: ");
        }

        double inputPrice = InputHelper.readDouble("Nhập giá niêm yết mới (nhập 0 nếu giữ nguyên): ", 0);
        double newPrice = (inputPrice == 0) ? p.getPrice() : inputPrice;

        int inputThreshold = InputHelper.readInt("Nhập ngưỡng cảnh báo tồn tối thiểu mới (nhập -1 nếu giữ nguyên): ", -1, 10000);
        int newThreshold = (inputThreshold == -1) ? p.getMinStockThreshold() : inputThreshold;

        boolean ok = prd.updateProductInfo(p.getId(), newName, newPrice, newThreshold);
        if (ok) {
            System.out.println("[OK] Cập nhật thông tin và giá niêm yết thành công!");
        } else {
            System.out.println("[X] Hiệu chỉnh sản phẩm thất bại!");
        }
    }

    // 1.6 Kiểm tra đặc tính kỹ thuật sản phẩm (SOS, Thu cũ, Tương thích)
    private void handleSpecialFeatures() {
        System.out.println("\n--------------------------------------------------------------");
        System.out.println("         KIỂM TRA ĐẶC TÍNH KỸ THUẬT & TÍNH NĂNG ĐỘC QUYỀN     ");
        System.out.println("--------------------------------------------------------------");
        System.out.println(" [1] Trợ giá Thu cũ đổi mới Smartphone (applyTradeInDiscount: Giảm 10%)");
        System.out.println(" [2] Bật / Tắt phím gọi khẩn cấp SOS Featurephone (toggleEmergencySOS)");
        System.out.println(" [3] Kiểm tra tính tương thích Phụ kiện với Điện thoại (isCompatibleWith)");
        System.out.println(" [0] Quay lại Menu trước");

        int mode = InputHelper.readInt("➤ Lựa chọn tính năng kiểm tra (0-3): ", 0, 3);
        switch (mode) {
            case 1: {
                System.out.println("\n>> KIỂM TRA CHÍNH SÁCH THU CŨ ĐỔI MỚI (Smartphone):");
                String rawId = InputHelper.readString("Nhập mã ID Smartphone (ví dụ PRD01, PRD02): ");
                String id = ProductManager.normalizeId(rawId);
                Product p = prd.findByID(id);
                if (p == null) {
                    System.out.println("[X] Không tìm thấy sản phẩm có mã: " + id);
                } else if (!(p instanceof Smartphone)) {
                    System.out.println("[X] Sản phẩm này không thuộc dòng Smartphone!");
                } else {
                    Smartphone sp = (Smartphone) p;
                    System.out.printf("Điện thoại: %s │ Giá niêm yết: %,.0f đ%n", sp.getName(), sp.getPrice());
                    double tradeInPrice = sp.applyTradeInDiscount();
                    System.out.printf("[OK] Giá thanh toán sau trợ giá Thu cũ đổi mới (giảm 10%%): %,.0f đ%n", tradeInPrice);
                }
                break;
            }
            case 2: {
                System.out.println("\n>> KIỂM TRA PHÍM KHẨN CẤP SOS (Featurephone):");
                String rawId = InputHelper.readString("Nhập mã ID Featurephone (ví dụ PRD26, PRD27): ");
                String id = ProductManager.normalizeId(rawId);
                Product p = prd.findByID(id);
                if (p == null) {
                    System.out.println("[X] Không tìm thấy sản phẩm có mã: " + id);
                } else if (!(p instanceof Featurephone)) {
                    System.out.println("[X] Sản phẩm này không thuộc dòng Điện thoại phổ thông (Featurephone)!");
                } else {
                    Featurephone fp = (Featurephone) p;
                    System.out.printf("Thiết bị: %s │ Trạng thái SOS hiện tại: %s%n",
                            fp.getName(), fp.isSosModeEnable() ? "ĐANG BẬT" : "ĐANG TẮT");
                    System.out.println(">> Thực hiện đảo trạng thái phím khẩn cấp SOS:");
                    fp.toggleEmergencySOS();
                }
                break;
            }
            case 3: {
                System.out.println("\n>> KIỂM TRA ĐỘ TƯƠNG THÍCH PHỤ KIỆN (Accessory):");
                String rawId = InputHelper.readString("Nhập mã ID Phụ kiện (ví dụ PRD38, PRD39, PRD41): ");
                String id = ProductManager.normalizeId(rawId);
                Product p = prd.findByID(id);
                if (p == null) {
                    System.out.println("[X] Không tìm thấy phụ kiện có mã: " + id);
                } else if (!(p instanceof Accessory)) {
                    System.out.println("[X] Sản phẩm này không thuộc phân loại Phụ kiện (Accessory)!");
                } else {
                    Accessory acc = (Accessory) p;
                    System.out.printf("Phụ kiện: %s (Chủng loại: %s)%n", acc.getName(), acc.getAccessoryType());
                    String phoneModel = InputHelper.readString("Nhập tên thiết bị điện thoại muốn ghép nối (ví dụ: iPhone 15, Galaxy S24): ");
                    acc.isCompatibleWith(phoneModel);
                }
                break;
            }
            case 0:
                break;
        }
    }

    // 1.7 ★ Thu hồi & Xóa sản phẩm khỏi danh mục (Chỉ Quản trị viên)
    private void handleDeleteProduct() {
        System.out.println("\n--------------------------------------------------------------");
        System.out.println("         ★ THU HỒI & XÓA SẢN PHẨM (QUYỀN QUẢN TRỊ VIÊN)       ");
        System.out.println("--------------------------------------------------------------");
        String rawId = InputHelper.readString("➤ Nhập mã ID sản phẩm muốn xóa khỏi hệ thống: ");
        String id = ProductManager.normalizeId(rawId);
        Product p = prd.findByID(id);

        if (p == null) {
            System.out.println("[X] Không tìm thấy sản phẩm có mã: " + id);
            return;
        }

        System.out.println("Thông tin sản phẩm chuẩn bị xóa:");
        p.displayInfo();
        System.out.println(" [LƯU Ý]: Hệ thống sẽ từ chối xóa nếu sản phẩm đã phát sinh con máy ĐÃ BÁN hoặc ĐANG BẢO HÀNH.");

        boolean confirm = InputHelper.readYesNo("Bạn có thực sự chắc chắn muốn xóa sản phẩm này không? (Y/N): ");
        if (confirm) {
            boolean deleted = prd.deleteID(p.getId());
            if (deleted) {
                System.out.println("[OK] Đã xóa sản phẩm và toàn bộ bản ghi chi tiết máy tồn kho thành công!");
            } else {
                System.out.println("[X] Xóa thất bại! Vui lòng kiểm tra lại trạng thái các con máy liên quan.");
            }
        } else {
            System.out.println("[!] Thao tác xóa đã được hủy bỏ an toàn.");
        }
    }

    // =========================================================================
    // PHÂN HỆ 2: QUẢN LÝ NGHIỆP VỤ NHẬP KHO HÀNG HÓA
    // =========================================================================
    private void manageImportMenu() {
        boolean inImport = true;
        while (inImport) {
            System.out.println("\n------------------------------------------------------------------------");
            System.out.println("                  QUẢN LÝ NGHIỆP VỤ NHẬP KHO HÀNG HÓA                   ");
            System.out.println("------------------------------------------------------------------------");
            System.out.println(" [1] Lập biên bản Phiếu nhập kho (Nhập lô hàng / Sản phẩm mới)");
            System.out.println(" [2] Tra cứu Lịch sử các Biên bản Phiếu nhập kho đã lưu");
            System.out.println(" [0] Quay lại Menu chính");
            System.out.println("------------------------------------------------------------------------");

            int choice = InputHelper.readInt("➤ Lựa chọn chức năng (0-2): ", 0, 2);
            switch (choice) {
                case 1:
                    handleImportGoods();
                    break;
                case 2:
                    showImportReceiptHistory();
                    break;
                case 0:
                    inImport = false;
                    break;
            }
        }
    }

    // 2.1 Lập phiếu nhập hàng vào kho
    private void handleImportGoods() {
        System.out.println("\n==============================================================");
        System.out.println("       LẬP BIÊN BẢN PHIẾU NHẬP HÀNG VÀO KHO (GOODS RECEIPT)   ");
        System.out.println("==============================================================");

        String receiptId = prd.nextReceiptId();
        String staffId = (currentUser != null && currentUser.getLinkedId() != null)
                ? currentUser.getLinkedId() : "EMP02";

        System.out.printf(" Mã phiếu nhập tự động: %s │ Nhân viên phụ trách: %s%n", receiptId, staffId);

        String supplier = "FPT_Synnex";
        if (!InputHelper.readYesNo("Sử dụng Nhà cung cấp mặc định 'FPT_Synnex'? (Y/N): ")) {
            supplier = InputHelper.readString("Nhập tên Nhà cung cấp lô hàng: ");
        }

        String importDate = DateUtil.nowText();
        if (!InputHelper.readYesNo("Sử dụng thời gian nhập kho hiện tại (" + importDate + ")? (Y/N): ")) {
            while (true) {
                String text = InputHelper.readString("Nhập ngày giờ nhập kho [dd/MM/yyyy HH:mm:ss]: ");
                try {
                    importDate = DateUtil.formatDateTime(DateUtil.parseDateTime(text));
                    break;
                } catch (IllegalArgumentException e) {
                    System.out.println("[X] Định dạng thời gian không đúng: " + e.getMessage());
                }
            }
        }

        int sessionItemCount = 0;
        int sessionTotalQty = 0;
        double sessionTotalCost = 0;

        // Vòng lặp nhập các mặt hàng trong cùng 1 phiếu
        while (true) {
            System.out.println("\n--------------------------------------------------------------");
            String rawId = InputHelper.readString("➤ Nhập Mã sản phẩm muốn nhập (hoặc gõ '0' để hoàn tất phiếu): ");

            if ("0".equals(rawId.trim())) {
                System.out.println("[OK] Đã hoàn tất danh sách mặt hàng cần nhập.");
                break;
            }

            String id = ProductManager.normalizeId(rawId);
            Product p = prd.findByID(id);

            if (p != null) {
                // Sản phẩm đã có trong danh mục
                System.out.printf(">> [SẢN PHẨM ĐÃ CÓ]: %s │ Hãng: %s │ Tồn hiện tại: %d chiếc%n",
                        p.getName(), p.getBrand(), p.getStockQuantity());

                int qty = InputHelper.readInt("Nhập số lượng máy nhập kho: ", 1, 100000);
                double importPrice = InputHelper.readDouble("Nhập giá nhập vốn mỗi chiếc (VND): ", 1);

                List<ProductDetail> batchDetails = new ArrayList<>();
                Set<String> batchSerials = new HashSet<>();
                boolean autoSn = InputHelper.readYesNo(
                        String.format("Bạn có muốn hệ thống tự động sinh IMEI/Seri cho %d máy không? (Y/N): ", qty));

                for (int i = 1; i <= qty; i++) {
                    String serial;
                    if (autoSn) {
                        int k = i;
                        do {
                            serial = "IMEI" + id.replace("PRD", "") + (System.currentTimeMillis() % 1000000)
                                    + String.format("%02d", k++);
                        } while (prd.isSerialExists(serial) || batchSerials.contains(serial));
                        System.out.printf("  - Thiết bị [%d/%d] -> Cấp IMEI: %s%n", i, qty, serial);
                    } else {
                        while (true) {
                            serial = InputHelper.readString(String.format("  - Nhập Seri/IMEI thiết bị [%d/%d]: ", i, qty)).toUpperCase();
                            if (prd.isSerialExists(serial) || batchSerials.contains(serial)) {
                                System.out.println("    [X] Seri/IMEI này đã tồn tại! Vui lòng nhập số khác.");
                                continue;
                            }
                            break;
                        }
                    }
                    batchSerials.add(serial);
                    batchDetails.add(new ProductDetail(p.getId(), p.getName(), serial, importDate, importPrice, supplier,
                            ProductManager.IN_STOCK, receiptId));
                }

                prd.addProductDetails(batchDetails);
                prd.syncStockFromDetails();

                sessionItemCount++;
                sessionTotalQty += qty;
                sessionTotalCost += (qty * importPrice);

                System.out.printf("[OK] Nhập thành công %d chiếc '%s'! Tồn kho mới: %d chiếc%n",
                        qty, p.getName(), p.getStockQuantity());

            } else {
                // Khai báo sản phẩm mới trong quá trình lập phiếu
                if (!id.matches("^PRD\\d{2,}$")) {
                    String autoId = prd.generateNextID();
                    System.out.printf(">> [THÔNG BÁO]: Mã '%s' chưa đúng chuẩn. Hệ thống tự cấp mã: %s%n", id, autoId);
                    id = autoId;
                }

                System.out.printf(">> [SẢN PHẨM MỚI]: Mã '%s' chưa từng có trong danh mục kho!%n", id);
                if (!InputHelper.readYesNo("Bạn có muốn khai báo sản phẩm mới này vào danh mục không? (Y/N): ")) {
                    System.out.println("[!] Bỏ qua sản phẩm này.");
                    continue;
                }

                System.out.println("\n--- KHAI BÁO THÔNG SỐ SẢN PHẨM MỚI ---");
                System.out.println(" [1] Smartphone (Điện thoại thông minh)");
                System.out.println(" [2] Featurephone (Điện thoại phổ thông)");
                System.out.println(" [3] Accessory (Phụ kiện)");
                int typeChoice = InputHelper.readInt("Phân loại (1-3): ", 1, 3);

                String name = InputHelper.readString("Nhập tên sản phẩm: ");
                String brand = InputHelper.readString("Nhập hãng sản xuất: ");
                double sellPrice = InputHelper.readDouble("Nhập giá niêm yết bán lẻ (VND): ", 1);
                int threshold = InputHelper.readInt("Nhập ngưỡng tồn kho an toàn: ", 0, 10000);
                int qty = InputHelper.readInt("Nhập số lượng nhập của lô: ", 1, 100000);
                double importPrice = InputHelper.readDouble("Nhập giá nhập vốn mỗi chiếc (VND): ", 1);

                Product newProduct;
                try {
                    if (typeChoice == 1) {
                        String os = InputHelper.readString("Nhập hệ điều hành (iOS / Android): ");
                        int storage = InputHelper.readInt("Nhập dung lượng ROM (GB): ", 1, 2048);
                        newProduct = new Smartphone(id, name, brand, sellPrice, qty, threshold, os, storage);
                    } else if (typeChoice == 2) {
                        int battery = InputHelper.readInt("Nhập pin chờ (số ngày): ", 1, 60);
                        newProduct = new Featurephone(id, name, brand, sellPrice, qty, threshold, "KaiOS", battery);
                    } else {
                        String accType;
                        while (true) {
                            accType = InputHelper.readString("Nhập loại phụ kiện (SAC, TAI_NGHE, OP_LUNG, CUONG_LUC): ").toUpperCase();
                            if (accType.matches("^(SAC|TAI_NGHE|OP_LUNG|CUONG_LUC)$")) break;
                            System.out.println("[X] Chỉ chọn: SAC, TAI_NGHE, OP_LUNG hoặc CUONG_LUC!");
                        }
                        newProduct = new Accessory(id, name, brand, sellPrice, qty, threshold, accType);
                    }
                } catch (ValidationException ve) {
                    System.out.println(ve.getMessage());
                    continue;
                } catch (Exception ex) {
                    System.out.println("[X] Lỗi khởi tạo: " + ex.getMessage());
                    continue;
                }

                List<ProductDetail> batchDetails = new ArrayList<>();
                Set<String> batchSerials = new HashSet<>();
                boolean autoSn = InputHelper.readYesNo(
                        String.format("Bạn có muốn tự động sinh Seri/IMEI cho cả lô %d máy không? (Y/N): ", qty));

                for (int i = 1; i <= qty; i++) {
                    String serial;
                    if (autoSn) {
                        int k = i;
                        do {
                            serial = "IMEI" + id.replace("PRD", "") + (System.currentTimeMillis() % 1000000)
                                    + String.format("%02d", k++);
                        } while (prd.isSerialExists(serial) || batchSerials.contains(serial));
                        System.out.printf("  - Thiết bị [%d/%d] -> Cấp IMEI: %s%n", i, qty, serial);
                    } else {
                        while (true) {
                            serial = InputHelper.readString(String.format("  - Nhập Seri/IMEI [%d/%d]: ", i, qty)).toUpperCase();
                            if (prd.isSerialExists(serial) || batchSerials.contains(serial)) {
                                System.out.println("    [X] Seri/IMEI đã tồn tại! Vui lòng nhập lại.");
                                continue;
                            }
                            break;
                        }
                    }
                    batchSerials.add(serial);
                    batchDetails.add(new ProductDetail(id, name, serial, importDate, importPrice, supplier,
                            ProductManager.IN_STOCK, receiptId));
                }

                boolean added = prd.add(newProduct);
                if (added) {
                    prd.addProductDetails(batchDetails);
                    prd.syncStockFromDetails();
                    sessionItemCount++;
                    sessionTotalQty += qty;
                    sessionTotalCost += (qty * importPrice);
                    System.out.printf("[OK] Đã tạo sản phẩm mới '%s' và nhập %d con máy vào kho!%n", name, qty);
                } else {
                    System.out.println("[X] Khai báo sản phẩm mới thất bại!");
                }
            }
        }

        // Tổng kết và lưu biên bản phiếu nhập
        if (sessionTotalQty > 0) {
            ImportReceipt receipt = new ImportReceipt(receiptId, staffId, importDate, supplier, sessionTotalQty, sessionTotalCost);
            prd.addReceipt(receipt);

            System.out.println("\n==============================================================");
            System.out.println("       BIÊN BẢN TỔNG KẾT PHIẾU NHẬP KHO (GOODS RECEIPT)       ");
            System.out.println("==============================================================");
            System.out.println(" Mã phiếu nhập   : " + receiptId);
            System.out.println(" Nhân viên lập   : " + staffId);
            System.out.println(" Nhà cung cấp    : " + supplier);
            System.out.println(" Thời gian nhập  : " + importDate);
            System.out.println(" Số dòng mặt hàng: " + sessionItemCount + " dòng");
            System.out.println(" Tổng số máy về  : " + sessionTotalQty + " chiếc");
            System.out.printf(" Tổng giá trị vốn: %,.0f VND%n", sessionTotalCost);
            System.out.println("==============================================================");
            System.out.println("[OK] Phiếu nhập đã được ghi nhận vào 'import_receipts.txt' và 'product_details.txt'!");
        } else {
            System.out.println("[!] Không có mặt hàng nào được nhập trong phiên này.");
        }
    }

    // 2.2 Tra cứu lịch sử biên bản phiếu nhập kho
    private void showImportReceiptHistory() {
        System.out.println("\n=========================================================================================================");
        System.out.println("                                LỊCH SỬ CÁC BIÊN BẢN PHIẾU NHẬP KHO                                      ");
        System.out.println("=========================================================================================================");
        System.out.printf(" %-8s │ %-8s │ %-19s │ %-20s │ %-14s │ %-18s%n",
                "MÃ PHIẾU", "MÃ NV", "NGÀY GIỜ NHẬP", "NHÀ CUNG CẤP", "TỔNG SỐ LƯỢNG", "TỔNG TIỀN VỐN");
        System.out.println("──────────┼──────────┼─────────────────────┼──────────────────────┼────────────────┼───────────────────");

        List<ImportReceipt> receipts = prd.getAllReceipts();
        if (receipts.isEmpty()) {
            System.out.println("  [!] Hiện chưa có biên bản phiếu nhập kho nào được ghi nhận trong hệ thống.");
        } else {
            int totalQty = 0;
            double totalCost = 0;
            for (ImportReceipt r : receipts) {
                r.displayRow();
                totalQty += r.getTotalQuantity();
                totalCost += r.getTotalAmount();
            }
            System.out.println("=========================================================================================================");
            System.out.printf("Tổng cộng: %d biên bản phiếu nhập │ Tổng số thiết bị: %d chiếc │ Tổng vốn nhập: %,.0f đ%n",
                    receipts.size(), totalQty, totalCost);
        }
    }

    // =========================================================================
    // PHÂN HỆ 3: GIÁM SÁT & CẢNH BÁO TỒN KHO TỐI THIỂU
    // =========================================================================
    private void showLowStockAlert() {
        System.out.println("\n=========================================================================================================");
        System.out.println("                            GIÁM SÁT & CẢNH BÁO TỒN KHO DƯỚI NGƯỠNG AN TOÀN                              ");
        System.out.println("=========================================================================================================");
        List<Product> lowStock = prd.getLowStockProducts();
        if (lowStock.isEmpty()) {
            System.out.println(" [OK] Tồn kho an toàn! Toàn bộ sản phẩm đều đang duy trì mức tồn trên ngưỡng cảnh báo.");
        } else {
            System.out.printf(" [!] CẢNH BÁO: PHÁT HIỆN %d SẢN PHẨM ĐANG CHẠM HOẶC DƯỚI NGƯỠNG AN TOÀN TỐI THIỂU:%n", lowStock.size());
            System.out.println("---------------------------------------------------------------------------------------------------------");
            for (Product p : lowStock) {
                p.displayInfo();
            }
            System.out.println("---------------------------------------------------------------------------------------------------------");
            System.out.println(" >> Khuyến nghị: Nhân viên kho cần sớm lên kế hoạch lập Phiếu nhập hàng cho các sản phẩm trên.");
        }
    }

    // =========================================================================
    // PHÂN HỆ 4: KIỂM KÊ & ĐỐI CHIẾU TỒN KHO THỰC TẾ
    // =========================================================================
    private void auditAndReconcileStock() {
        System.out.println("\n=========================================================================================================");
        System.out.println("                     KIỂM KÊ & ĐỐI CHIẾU TỒN KHO SỔ SÁCH VỚI THỰC TẾ (SERIAL/IMEI)                       ");
        System.out.println("=========================================================================================================");
        System.out.printf(" %-7s │ %-28s │ %-10s │ %-10s │ %-10s │ %-12s%n",
                "MÃ SP", "TÊN SẢN PHẨM", "SỔ SÁCH", "SERIAL KHO", "CHÊNH LỆCH", "ĐÁNH GIÁ");
        System.out.println("────────┼──────────────────────────────┼────────────┼────────────┼────────────┼─────────────");

        List<Product> products = prd.getALL();
        int discrepancyCount = 0;

        for (Product p : products) {
            int bookStock = p.getStockQuantity();
            int physicalStock = prd.countInStock(p.getId());
            int diff = physicalStock - bookStock;

            String statusVi = (diff == 0) ? "Khớp chuẩn" : (diff > 0 ? "[+] Thừa " + diff : "[-] Thiếu " + Math.abs(diff));
            System.out.printf(" %-7s │ %-28s │ %8d   │ %8d   │ %+8d   │ %-12s%n",
                    p.getId(),
                    (p.getName().length() > 28 ? p.getName().substring(0, 25) + "..." : p.getName()),
                    bookStock, physicalStock, diff, statusVi);

            if (diff != 0) {
                discrepancyCount++;
            }
        }
        System.out.println("=========================================================================================================");

        if (discrepancyCount == 0) {
            System.out.println(" [OK] KẾT QUẢ KIỂM KÊ: Tồn kho hoàn toàn chính xác 100% giữa sổ sách và thiết bị thực tế!");
        } else {
            System.out.printf(" [!] CẢNH BÁO: Phát hiện %d sản phẩm có sự chênh lệch giữa số lượng sổ sách và Serial thực tế!%n",
                    discrepancyCount);
            boolean syncNow = InputHelper.readYesNo("➤ Bạn có muốn thực hiện 1-Click tự động đồng bộ lại tồn kho theo Serial thực tế không? (Y/N): ");
            if (syncNow) {
                prd.syncStockFromDetails();
                System.out.println("[OK] Đã hoàn tất đồng bộ! Dữ liệu tồn kho hiện đã khớp tuyệt đối với số Serial IN_STOCK.");
            }
        }
    }

    // =========================================================================
    // PHÂN HỆ 5: ★ QUẢN LÝ CHÍNH SÁCH CHIẾT KHẤU & KHUYẾN MÃI (CHỈ ADMIN)
    // =========================================================================
    private void manageDiscountMenu() {
        boolean inDiscount = true;
        while (inDiscount) {
            System.out.println("\n------------------------------------------------------------------------");
            System.out.println("        ★ QUẢN LÝ CHÍNH SÁCH CHIẾT KHẤU & KHUYẾN MÃI (ADMIN)           ");
            System.out.println("------------------------------------------------------------------------");
            System.out.println(" [1] Thiết lập Tỷ lệ Chiết khấu cho Sản phẩm (IDiscountable)");
            System.out.println(" [2] Tra cứu Danh sách Sản phẩm đang Áp dụng Chiết khấu");
            System.out.println(" [0] Quay lại Menu chính");
            System.out.println("------------------------------------------------------------------------");

            int choice = InputHelper.readInt("➤ Lựa chọn chức năng (0-2): ", 0, 2);
            switch (choice) {
                case 1:
                    handleSetDiscount();
                    break;
                case 2:
                    showDiscountedProducts();
                    break;
                case 0:
                    inDiscount = false;
                    break;
            }
        }
    }

    // 5.1 Thiết lập tỷ lệ chiết khấu
    private void handleSetDiscount() {
        System.out.println("\n--------------------------------------------------------------");
        System.out.println("         THIẾT LẬP CHƯƠNG TRÌNH CHIẾT KHẤU (IDiscountable)     ");
        System.out.println("--------------------------------------------------------------");
        String rawId = InputHelper.readString("➤ Nhập mã ID sản phẩm muốn cài đặt giảm giá: ");
        String id = ProductManager.normalizeId(rawId);
        Product p = prd.findByID(id);

        if (p == null) {
            System.out.println("[X] Không tìm thấy sản phẩm có mã: " + id);
            return;
        }

        if (!(p instanceof IDiscountable)) {
            System.out.println("[!] Dòng sản phẩm này (Featurephone) không hỗ trợ chính sách chiết khấu IDiscountable!");
            return;
        }

        System.out.printf("Sản phẩm: %s │ Giá niêm yết: %,.0f đ%n", p.getName(), p.getPrice());
        if (p.getDiscountRate() > 0) {
            System.out.printf(" (Hiện đang áp dụng chiết khấu: %.0f%% - Giá bán hiện tại: %,.0f đ)%n",
                    p.getDiscountRate() * 100, p.getSalePrice());
        }

        double rate = InputHelper.readDouble("Nhập tỷ lệ chiết khấu (0.1 cho 10%, 0.2 cho 20%, gõ 0 để hủy giảm giá): ", 0.0, 1.0);

        // Quy định trần giảm giá cho Phụ kiện: không quá 50%
        if (p instanceof Accessory && rate > 0.50) {
            System.out.println("[!] CẢNH BÁO: Theo quy định, Phụ kiện chỉ được chiết khấu tối đa 50% (0.5)!");
            boolean confirm = InputHelper.readYesNo("Bạn có muốn điều chỉnh tỷ lệ về mức trần 50% không? (Y/N): ");
            if (confirm) {
                rate = 0.50;
            } else {
                System.out.println("[!] Đã hủy thiết lập giảm giá.");
                return;
            }
        }

        double salePrice = prd.applyDiscount(p.getId(), rate);
        if (rate == 0) {
            System.out.printf("[OK] Đã hủy chiết khấu cho sản phẩm '%s'. Giá bán trở về giá niêm yết: %,.0f đ%n",
                    p.getName(), p.getPrice());
        } else {
            System.out.printf("[OK] Áp dụng thành công chiết khấu %.0f%% cho '%s'!%n", rate * 100, p.getName());
            System.out.printf("     Giá niêm yết gốc: %,.0f đ  ==>  Giá bán ưu đãi: %,.0f đ%n",
                    p.getPrice(), salePrice);
        }
    }

    // 5.2 Tra cứu danh sách sản phẩm đang giảm giá
    private void showDiscountedProducts() {
        System.out.println("\n=========================================================================================================");
        System.out.println("                             DANH SÁCH SẢN PHẨM ĐANG ÁP DỤNG CHIẾT KHẤU                                  ");
        System.out.println("=========================================================================================================");
        System.out.printf(" %-7s │ %-28s │ %-10s │ %-15s │ %-10s │ %-16s%n",
                "MÃ SP", "TÊN SẢN PHẨM", "HÃNG", "GIÁ NIÊM YẾT", "GIẢM GIÁ", "GIÁ BÁN ƯU ĐÃI");
        System.out.println("────────┼──────────────────────────────┼────────────┼─────────────────┼────────────┼──────────────────");

        List<Product> discounted = prd.getDiscountedProducts();
        if (discounted.isEmpty()) {
            System.out.println("  [!] Hiện tại không có sản phẩm nào đang trong chương trình giảm giá.");
        } else {
            for (Product p : discounted) {
                System.out.printf(" %-7s │ %-28s │ %-10s │ %,13.0f đ │ %8.0f %%  │ %,14.0f đ%n",
                        p.getId(),
                        (p.getName().length() > 28 ? p.getName().substring(0, 25) + "..." : p.getName()),
                        p.getBrand(),
                        p.getPrice(),
                        p.getDiscountRate() * 100,
                        p.getSalePrice());
            }
            System.out.println("=========================================================================================================");
            System.out.printf("Tổng cộng: %d sản phẩm đang có chương trình khuyến mãi ưu đãi giá.%n", discounted.size());
        }
    }

    // =========================================================================
    // PHÂN HỆ 6: THIẾT LẬP BẢO MẬT & ĐỔI MẬT KHẨU CÁ NHÂN
    // =========================================================================
    private void handleChangePassword() {
        System.out.println("\n--------------------------------------------------------------");
        System.out.println("                 THIẾT LẬP ĐỔI MẬT KHẨU CÁ NHÂN               ");
        System.out.println("--------------------------------------------------------------");
        if (currentUser == null) {
            System.out.println("[!] Phiên làm việc không xác định tài khoản người dùng.");
            return;
        }

        System.out.println("Tài khoản đang đăng nhập: " + currentUser.getUsername());
        String oldPass = InputHelper.readString("➤ Nhập mật khẩu hiện tại: ");

        if (!currentUser.checkLogin(currentUser.getUsername(), oldPass)) {
            System.out.println("[X] Mật khẩu hiện tại không chính xác! Đổi mật khẩu thất bại.");
            return;
        }

        String newPass;
        while (true) {
            newPass = InputHelper.readString("➤ Nhập mật khẩu mới (tối thiểu 6 ký tự, không chứa ký tự '|'): ");
            if (newPass.length() < 6 || newPass.contains("|")) {
                System.out.println("[X] Mật khẩu mới không đạt chuẩn! Vui lòng thử lại.");
                continue;
            }
            if (newPass.equals(oldPass)) {
                System.out.println("[X] Mật khẩu mới không được trùng lặp với mật khẩu hiện tại!");
                continue;
            }
            break;
        }

        String confirmPass = InputHelper.readString("➤ Xác nhận lại mật khẩu mới: ");
        if (!newPass.equals(confirmPass)) {
            System.out.println("[X] Mật khẩu xác nhận không trùng khớp! Hủy thao tác đổi mật khẩu.");
            return;
        }

        currentUser.changePassword(newPass);

        // Lưu mật khẩu mới vào data/accounts.txt nếu tồn tại
        try {
            Path accPath = Paths.get("data/accounts.txt");
            if (Files.exists(accPath)) {
                List<String> lines = FileIO.readLines("data/accounts.txt");
                List<String> newLines = new ArrayList<>();
                for (String line : lines) {
                    String[] parts = line.split("\\|", -1);
                    if (parts.length >= 7 && parts[0].trim().equalsIgnoreCase(currentUser.getId())) {
                        newLines.add(currentUser.toFileString());
                    } else {
                        newLines.add(line);
                    }
                }
                FileIO.writeLines("data/accounts.txt", newLines);
            }
        } catch (Exception ignored) {}

        System.out.println("[OK] Đổi mật khẩu cá nhân thành công! Vui lòng ghi nhớ mật khẩu mới của bạn.");
    }
}
