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
 * Phân hệ Quản lý Kho hàng (KHO).
 * Tuân thủ 100% sơ đồ cây menu trong KE_HOACH_TAI_CAU_TRUC_MENU.md.
 * Nhận Account currentUser để phân quyền: Ẩn mục ★ [1][5] Xóa SP và [5] Giảm giá với Nhân viên kho.
 */
public class WarehouseMenu {
    private final ProductManager prd;
    private final Account currentUser;

    // Constructor chuẩn theo kế hoạch tái cấu trúc menu
    public WarehouseMenu(ProductManager prd, Account currentUser) {
        this.prd = prd;
        this.currentUser = currentUser;
    }

    // Overload khởi tạo mặc định tài khoản Nhân viên kho
    public WarehouseMenu(ProductManager prd) {
        this(prd, new Account("ACC02", "kho01", "kho123", Role.WAREHOUSE, "ACTIVE", DateUtil.nowText(), "EMP02"));
    }

    // Hàm main khởi chạy trực tiếp phân hệ Kho hàng (mặc định phiên làm việc Nhân viên kho)
    public static void main(String[] args) {
        ProductManager manager = new ProductManager();
        Account warehouseStaff = new Account("ACC02", "kho01", "kho123", Role.WAREHOUSE, "ACTIVE", DateUtil.nowText(), "EMP02");
        new WarehouseMenu(manager, warehouseStaff).displayMenu();
    }

    // Kiểm tra quyền Quản trị viên (Admin)
    private boolean isAdmin() {
        return currentUser != null && currentUser.getRole() == Role.ADMIN;
    }

    // =========================================================================
    // MENU CHÍNH: KHO
    // =========================================================================
    public void displayMenu() {
        boolean running = true;
        while (running) {
            String roleName = (currentUser != null && currentUser.getRole() != null)
                    ? currentUser.getRole().getDisplayName() : "Nhân viên kho";
            String userName = (currentUser != null) ? currentUser.getUsername() : "kho01";
            String staffId = (currentUser != null && currentUser.getLinkedId() != null)
                    ? currentUser.getLinkedId() : "EMP02";

            System.out.println("\n==============================================================");
            System.out.println("                       QUẢN LÝ KHO HÀNG                       ");
            System.out.println("==============================================================");
            System.out.printf(" Tài khoản: %-10s │ Vai trò: %-15s │ Mã NV: %s%n",
                    userName, roleName, staffId);
            System.out.println("--------------------------------------------------------------");
            System.out.println(" [1] Danh sách sản phẩm");
            System.out.println(" [2] Quản lý nhập kho");
            System.out.println(" [3] Cảnh báo hàng sắp hết");
            System.out.println(" [4] Kiểm tra tồn kho (đối chiếu số lượng với serial thực tế)");
            if (isAdmin()) {
                System.out.println(" [5] ★ Quản lý giảm giá");
            }
            System.out.println(" [6] Đổi mật khẩu");
            System.out.println(" [0] " + (isAdmin() ? "Quay lại Menu Quản trị" : "Đăng xuất"));
            System.out.println("==============================================================");

            int choice = InputHelper.readInt("➤ Chọn chức năng: ", 0, 6);

            switch (choice) {
                case 1:
                    productListMenu();
                    break;
                case 2:
                    importManagementMenu();
                    break;
                case 3:
                    showLowStockAlert();
                    break;
                case 4:
                    checkStockAudit();
                    break;
                case 5:
                    if (!isAdmin()) {
                        System.out.println("\n[!] Chức năng này chỉ dành cho Quản trị viên (Admin)!");
                        break;
                    }
                    discountMenu();
                    break;
                case 6:
                    changePassword();
                    break;
                case 0:
                    System.out.println("\n[OK] Đang lưu dữ liệu và kết thúc ca làm việc Kho...");
                    prd.saveToFile();
                    prd.saveDetailsToFile();
                    prd.saveReceiptsToFile();
                    running = false;
                    break;
            }
        }
    }

    // =========================================================================
    // [1] DANH SÁCH SẢN PHẨM
    // =========================================================================
    private void productListMenu() {
        boolean inList = true;
        while (inList) {
            System.out.println("\n--------------------------------------------------------------");
            System.out.println("                      DANH SÁCH SẢN PHẨM                      ");
            System.out.println("--------------------------------------------------------------");
            System.out.println(" [1] Xem tất cả");
            System.out.println(" [2] Tìm kiếm");
            System.out.println(" [3] Thêm sản phẩm mới");
            System.out.println(" [4] Sửa thông tin sản phẩm");
            if (isAdmin()) {
                System.out.println(" [5] ★ Xóa sản phẩm");
            }
            System.out.println(" [0] Quay lại");
            System.out.println("--------------------------------------------------------------");

            int choice = InputHelper.readInt("➤ Chọn chức năng: ", 0, 5);

            switch (choice) {
                case 1:
                    showAllProducts();
                    break;
                case 2:
                    searchProduct();
                    break;
                case 3:
                    addProduct();
                    break;
                case 4:
                    updateProduct();
                    break;
                case 5:
                    if (!isAdmin()) {
                        System.out.println("\n[!] Bạn không có quyền xóa sản phẩm. Chức năng này chỉ dành cho Quản trị viên (Admin)!");
                        break;
                    }
                    deleteProduct();
                    break;
                case 0:
                    inList = false;
                    break;
            }
        }
    }

    // [1][1] Xem tất cả
    private void showAllProducts() {
        System.out.println("\n=========================================================================================================");
        System.out.println("                                        DANH SÁCH TOÀN BỘ SẢN PHẨM                                       ");
        System.out.println("=========================================================================================================");
        List<Product> list = prd.getALL();
        if (list.isEmpty()) {
            System.out.println(" [!] Kho hàng hiện đang trống, chưa có sản phẩm nào!");
            return;
        }

        for (Product p : list) {
            p.displayInfo();
        }
        System.out.println("=========================================================================================================");
        System.out.printf("Tổng số dòng sản phẩm: %d dòng.%n", list.size());

        System.out.println("\nTùy chọn xem chi tiết:");
        System.out.println(" [1] Xem chi tiết từng con máy (Serial / IMEI & Ngày nhập)");
        System.out.println(" [2] Kiểm tra tính năng kỹ thuật chi tiết (Phím SOS, Thu cũ đổi mới, Tương thích phụ kiện)");
        System.out.println(" [0] Quay lại Menu Sản phẩm");
        int subChoice = InputHelper.readInt("➤ Lựa chọn (0-2): ", 0, 2);

        if (subChoice == 1) {
            showDetailSerials();
        } else if (subChoice == 2) {
            checkSpecialFeatures();
        }
    }

    // Xem chi tiết từng con máy (Serial / IMEI)
    private void showDetailSerials() {
        System.out.println("\n--------------------------------------------------------------");
        System.out.println("            TRA CỨU CHI TIẾT TỪNG THIẾT BỊ (SERIAL/IMEI)      ");
        System.out.println("--------------------------------------------------------------");
        System.out.println(" [1] Xem tất cả thiết bị trong kho");
        System.out.println(" [2] Lọc thiết bị theo Hãng (Apple, Samsung, Xiaomi...)");
        System.out.println(" [3] Lọc thiết bị theo Mã sản phẩm (ví dụ PRD01)");
        System.out.println(" [0] Quay lại");

        int mode = InputHelper.readInt("➤ Chọn chế độ lọc (0-3): ", 0, 3);
        List<ProductDetail> details;
        String title;

        if (mode == 1) {
            details = prd.getAllProductDetails();
            title = "TẤT CẢ THIẾT BỊ";
        } else if (mode == 2) {
            String brand = InputHelper.readString("Nhập tên hãng cần lọc: ");
            details = prd.getDetailsByBrand(brand);
            title = "HÃNG: " + brand.toUpperCase();
        } else if (mode == 3) {
            String rawId = InputHelper.readString("Nhập mã sản phẩm (ví dụ PRD01): ");
            String pid = ProductManager.normalizeId(rawId);
            details = prd.getDetailsByProductId(pid);
            Product p = prd.findByID(pid);
            title = "MÃ SP: " + pid.toUpperCase() + (p != null ? " (" + p.getName() + ")" : "");
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
            for (ProductDetail d : details) {
                Product p = prd.findByID(d.getProductId());
                String pName = (p != null) ? p.getName() : d.getProductName();
                String pBrand = (p != null) ? p.getBrand() : "N/A";
                d.displayRow(pName, pBrand);

                if (ProductManager.IN_STOCK.equalsIgnoreCase(d.getStatus())) inStockCount++;
                else soldCount++;
            }
            System.out.println("=================================================================================================================================");
            System.out.printf("Tổng cộng: %d thiết bị (Trong kho: %d │ Đã xuất bán: %d)%n",
                    details.size(), inStockCount, soldCount);
        }
    }

    // Kiểm tra tính năng kỹ thuật chi tiết
    private void checkSpecialFeatures() {
        System.out.println("\n--------------------------------------------------------------");
        System.out.println("                  KIỂM TRA TÍNH NĂNG KỸ THUẬT                 ");
        System.out.println("--------------------------------------------------------------");
        System.out.println(" [1] Trợ giá Thu cũ đổi mới Smartphone (applyTradeInDiscount: Giảm 10%)");
        System.out.println(" [2] Bật / Tắt phím gọi khẩn cấp SOS Featurephone (toggleEmergencySOS)");
        System.out.println(" [3] Kiểm tra tính tương thích Phụ kiện với Điện thoại (isCompatibleWith)");
        System.out.println(" [0] Quay lại");

        int mode = InputHelper.readInt("➤ Lựa chọn tính năng (0-3): ", 0, 3);
        switch (mode) {
            case 1: {
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
                String rawId = InputHelper.readString("Nhập mã ID Phụ kiện (ví dụ PRD38, PRD39): ");
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

    // [1][2] Tìm kiếm
    private void searchProduct() {
        System.out.println("\n--------------------------------------------------------------");
        System.out.println("                       TÌM KIẾM SẢN PHẨM                      ");
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

    // [1][3] Thêm sản phẩm mới
    private void addProduct() {
        System.out.println("\n--------------------------------------------------------------");
        System.out.println("                      THÊM SẢN PHẨM MỚI                       ");
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
        System.out.println(" [3] Accessory (Phụ kiện)");
        int typeChoice = InputHelper.readInt("➤ Phân loại (1-3): ", 1, 3);

        String name = InputHelper.readString("Nhập tên sản phẩm: ");
        String brand = InputHelper.readString("Nhập hãng sản xuất (Apple, Samsung, Xiaomi...): ");
        double sellPrice = InputHelper.readDouble("Nhập giá niêm yết bán cho khách (VND): ", 1);
        int threshold = InputHelper.readInt("Nhập định mức tồn kho an toàn tối thiểu: ", 0, 10000);

        Product newProduct;
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
            return;
        } catch (Exception ex) {
            System.out.println("[X] Lỗi khởi tạo: " + ex.getMessage());
            return;
        }

        boolean ok = prd.add(newProduct);
        if (ok) {
            System.out.printf("[OK] Thêm thành công sản phẩm '%s' (Mã: %s)!%n", name, id);
            System.out.println("     (Tồn kho ban đầu = 0. Vui lòng vào 'Quản lý nhập kho' để nhập lô hàng đầu tiên).");
        } else {
            System.out.println("[X] Thêm sản phẩm thất bại!");
        }
    }

    // [1][4] Sửa thông tin sản phẩm
    private void updateProduct() {
        System.out.println("\n--------------------------------------------------------------");
        System.out.println("                    SỬA THÔNG TIN SẢN PHẨM                    ");
        System.out.println("--------------------------------------------------------------");
        String rawId = InputHelper.readString("➤ Nhập mã ID sản phẩm cần sửa (ví dụ PRD01): ");
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
            newName = InputHelper.readString("Nhập tên mới: ");
        }

        double inputPrice = InputHelper.readDouble("Nhập giá niêm yết mới (nhập 0 nếu giữ nguyên): ", 0);
        double newPrice = (inputPrice == 0) ? p.getPrice() : inputPrice;

        int inputThreshold = InputHelper.readInt("Nhập ngưỡng cảnh báo tồn tối thiểu mới (nhập -1 nếu giữ nguyên): ", -1, 10000);
        int newThreshold = (inputThreshold == -1) ? p.getMinStockThreshold() : inputThreshold;

        boolean ok = prd.updateProductInfo(p.getId(), newName, newPrice, newThreshold);
        if (ok) {
            System.out.println("[OK] Cập nhật thông tin sản phẩm thành công!");
        } else {
            System.out.println("[X] Cập nhật thất bại!");
        }
    }

    // [1][5] ★ Xóa sản phẩm (Chỉ Admin)
    private void deleteProduct() {
        System.out.println("\n--------------------------------------------------------------");
        System.out.println("               ★ XÓA SẢN PHẨM (CHỈ ADMIN)                     ");
        System.out.println("--------------------------------------------------------------");
        String rawId = InputHelper.readString("➤ Nhập mã ID sản phẩm muốn xóa: ");
        String id = ProductManager.normalizeId(rawId);
        Product p = prd.findByID(id);

        if (p == null) {
            System.out.println("[X] Không tìm thấy sản phẩm có mã: " + id);
            return;
        }

        System.out.println("Thông tin sản phẩm chuẩn bị xóa:");
        p.displayInfo();
        System.out.println(" [LƯU Ý]: Hệ thống sẽ từ chối xóa nếu sản phẩm đã có máy ĐÃ BÁN hoặc ĐANG BẢO HÀNH.");

        boolean confirm = InputHelper.readYesNo("Bạn có chắc chắn muốn xóa sản phẩm này không? (Y/N): ");
        if (confirm) {
            boolean deleted = prd.deleteID(p.getId());
            if (deleted) {
                System.out.println("[OK] Đã xóa sản phẩm và toàn bộ bản ghi chi tiết máy tồn kho!");
            } else {
                System.out.println("[X] Xóa thất bại! Sản phẩm đang có máy liên quan đến hóa đơn hoặc bảo hành.");
            }
        } else {
            System.out.println("[!] Đã hủy thao tác xóa.");
        }
    }

    // =========================================================================
    // [2] QUẢN LÝ NHẬP KHO
    // =========================================================================
    private void importManagementMenu() {
        boolean inImport = true;
        while (inImport) {
            System.out.println("\n--------------------------------------------------------------");
            System.out.println("                       QUẢN LÝ NHẬP KHO                       ");
            System.out.println("--------------------------------------------------------------");
            System.out.println(" [1] Tạo phiếu nhập");
            System.out.println(" [2] Xem lịch sử phiếu nhập");
            System.out.println(" [0] Quay lại");
            System.out.println("--------------------------------------------------------------");

            int choice = InputHelper.readInt("➤ Chọn chức năng: ", 0, 2);
            switch (choice) {
                case 1:
                    createImportReceipt();
                    break;
                case 2:
                    viewReceiptHistory();
                    break;
                case 0:
                    inImport = false;
                    break;
            }
        }
    }

    // [2][1] Tạo phiếu nhập
    private void createImportReceipt() {
        System.out.println("\n==============================================================");
        System.out.println("                       TẠO PHIẾU NHẬP                         ");
        System.out.println("==============================================================");

        String receiptId = prd.nextReceiptId();
        String staffId = (currentUser != null && currentUser.getLinkedId() != null)
                ? currentUser.getLinkedId() : "EMP02";

        System.out.printf(" Mã phiếu nhập: %s │ Mã nhân viên: %s%n", receiptId, staffId);

        String supplier = "FPT_Synnex";
        if (!InputHelper.readYesNo("Sử dụng Nhà cung cấp mặc định 'FPT_Synnex'? (Y/N): ")) {
            supplier = InputHelper.readString("Nhập tên Nhà cung cấp: ");
        }

        String importDate = DateUtil.nowText();
        if (!InputHelper.readYesNo("Sử dụng thời gian nhập kho hiện tại (" + importDate + ")? (Y/N): ")) {
            while (true) {
                String text = InputHelper.readString("Nhập ngày giờ nhập kho [dd/MM/yyyy HH:mm:ss]: ");
                try {
                    importDate = DateUtil.formatDateTime(DateUtil.parseDateTime(text));
                    break;
                } catch (IllegalArgumentException e) {
                    System.out.println("[X] " + e.getMessage());
                }
            }
        }

        int sessionItemCount = 0;
        int sessionTotalQty = 0;
        double sessionTotalCost = 0;

        while (true) {
            System.out.println("\n--------------------------------------------------------------");
            String rawId = InputHelper.readString("➤ Nhập Mã sản phẩm muốn nhập (gõ '0' để dừng): ");

            if ("0".equals(rawId.trim())) {
                System.out.println("[OK] Kết thúc danh sách mặt hàng trong phiếu nhập!");
                break;
            }

            String id = ProductManager.normalizeId(rawId);
            Product p = prd.findByID(id);

            if (p != null) {
                System.out.printf(">> [SẢN PHẨM CÓ SẴN]: %s │ Hãng: %s │ Tồn hiện tại: %d chiếc%n",
                        p.getName(), p.getBrand(), p.getStockQuantity());

                int qty = InputHelper.readInt("Nhập số lượng máy nhập: ", 1, 100000);
                double importPrice = InputHelper.readDouble("Nhập giá nhập mỗi chiếc (VND): ", 1);

                List<ProductDetail> batchDetails = new ArrayList<>();
                Set<String> batchSerials = new HashSet<>();
                boolean autoSn = InputHelper.readYesNo(
                        String.format("Bạn có muốn tự động sinh IMEI/Seri cho %d máy không? (Y/N): ", qty));

                for (int i = 1; i <= qty; i++) {
                    String serial;
                    if (autoSn) {
                        int k = i;
                        do {
                            serial = "IMEI" + id.replace("PRD", "") + (System.currentTimeMillis() % 1000000)
                                    + String.format("%02d", k++);
                        } while (prd.isSerialExists(serial) || batchSerials.contains(serial));
                        System.out.printf("  - Máy [%d/%d] -> Tự cấp IMEI: %s%n", i, qty, serial);
                    } else {
                        while (true) {
                            serial = InputHelper.readString(String.format("  - Nhập Seri/IMEI máy [%d/%d]: ", i, qty)).toUpperCase();
                            if (prd.isSerialExists(serial) || batchSerials.contains(serial)) {
                                System.out.println("    [X] Seri/IMEI này đã có trong kho! Vui lòng nhập lại.");
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
                if (!id.matches("^PRD\\d{2,}$")) {
                    String autoId = prd.generateNextID();
                    System.out.printf(">> [THÔNG BÁO]: Mã '%s' chưa đúng chuẩn. Tự cấp mã: %s%n", id, autoId);
                    id = autoId;
                }

                System.out.printf(">> [SẢN PHẨM MỚI]: Mã '%s' chưa có trong danh mục!%n", id);
                if (!InputHelper.readYesNo("Bạn có muốn khai báo sản phẩm mới này luôn không? (Y/N): ")) {
                    System.out.println("[!] Bỏ qua mã này.");
                    continue;
                }

                System.out.println("\n--- KHAI BÁO THÔNG SỐ SẢN PHẨM MỚI ---");
                System.out.println(" [1] Smartphone (Điện thoại thông minh)");
                System.out.println(" [2] Featurephone (Điện thoại phổ thông)");
                System.out.println(" [3] Accessory (Phụ kiện)");
                int typeChoice = InputHelper.readInt("Chọn (1-3): ", 1, 3);

                String name = InputHelper.readString("Nhập tên sản phẩm: ");
                String brand = InputHelper.readString("Nhập hãng sản xuất: ");
                double sellPrice = InputHelper.readDouble("Nhập giá bán niêm yết (VND): ", 1);
                int threshold = InputHelper.readInt("Nhập ngưỡng cảnh báo tồn tối thiểu: ", 0, 10000);
                int qty = InputHelper.readInt("Nhập số lượng nhập của lô: ", 1, 100000);
                double importPrice = InputHelper.readDouble("Nhập giá nhập vốn mỗi chiếc (VND): ", 1);

                Product newProduct;
                try {
                    if (typeChoice == 1) {
                        String os = InputHelper.readString("Nhập hệ điều hành (iOS / Android): ");
                        int storage = InputHelper.readInt("Nhập dung lượng ROM (GB): ", 1, 2048);
                        newProduct = new Smartphone(id, name, brand, sellPrice, qty, threshold, os, storage);
                    } else if (typeChoice == 2) {
                        int battery = InputHelper.readInt("Nhập thời lượng pin chờ (số ngày): ", 1, 60);
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
                    System.out.println("[X] Lỗi: " + ex.getMessage());
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
                        System.out.printf("  - Máy [%d/%d] -> Tự cấp IMEI: %s%n", i, qty, serial);
                    } else {
                        while (true) {
                            serial = InputHelper.readString(String.format("  - Nhập Seri/IMEI [%d/%d]: ", i, qty)).toUpperCase();
                            if (prd.isSerialExists(serial) || batchSerials.contains(serial)) {
                                System.out.println("    [X] Seri/IMEI này đã có trong kho! Vui lòng nhập lại.");
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

        if (sessionTotalQty > 0) {
            ImportReceipt receipt = new ImportReceipt(receiptId, staffId, importDate, supplier, sessionTotalQty, sessionTotalCost);
            prd.addReceipt(receipt);

            System.out.println("\n==============================================================");
            System.out.println("              BIÊN BẢN TỔNG KẾT PHIẾU NHẬP                    ");
            System.out.println("==============================================================");
            System.out.println(" Mã phiếu nhập   : " + receiptId);
            System.out.println(" Nhân viên lập   : " + staffId);
            System.out.println(" Nhà cung cấp    : " + supplier);
            System.out.println(" Thời gian nhập  : " + importDate);
            System.out.println(" Số dòng mặt hàng: " + sessionItemCount + " dòng");
            System.out.println(" Tổng số máy nhập: " + sessionTotalQty + " chiếc");
            System.out.printf(" Tổng tiền nhập  : %,.0f VND%n", sessionTotalCost);
            System.out.println("==============================================================");
            System.out.println("[OK] Dữ liệu đã được lưu vào 'import_receipts.txt' và 'product_details.txt'!");
        } else {
            System.out.println("[!] Không có sản phẩm nào được nhập trong phiên này.");
        }
    }

    // [2][2] Xem lịch sử phiếu nhập
    private void viewReceiptHistory() {
        System.out.println("\n=========================================================================================================");
        System.out.println("                                      LỊCH SỬ PHIẾU NHẬP KHO                                             ");
        System.out.println("=========================================================================================================");
        System.out.printf(" %-8s │ %-8s │ %-19s │ %-20s │ %-14s │ %-18s%n",
                "MÃ PHIẾU", "MÃ NV", "NGÀY GIỜ NHẬP", "NHÀ CUNG CẤP", "TỔNG SỐ LƯỢNG", "TỔNG TIỀN VỐN");
        System.out.println("──────────┼──────────┼─────────────────────┼──────────────────────┼────────────────┼───────────────────");

        List<ImportReceipt> receipts = prd.getAllReceipts();
        if (receipts.isEmpty()) {
            System.out.println("  [!] Hiện chưa có phiếu nhập kho nào.");
        } else {
            int totalQty = 0;
            double totalCost = 0;
            for (ImportReceipt r : receipts) {
                r.displayRow();
                totalQty += r.getTotalQuantity();
                totalCost += r.getTotalAmount();
            }
            System.out.println("=========================================================================================================");
            System.out.printf("Tổng cộng: %d phiếu nhập │ Tổng số thiết bị: %d chiếc │ Tổng tiền nhập: %,.0f đ%n",
                    receipts.size(), totalQty, totalCost);
        }
    }

    // =========================================================================
    // [3] CẢNH BÁO HÀNG SẮP HẾT
    // =========================================================================
    private void showLowStockAlert() {
        System.out.println("\n=========================================================================================================");
        System.out.println("                                      CẢNH BÁO HÀNG SẮP HẾT                                              ");
        System.out.println("=========================================================================================================");
        List<Product> lowStock = prd.getLowStockProducts();
        if (lowStock.isEmpty()) {
            System.out.println(" [OK] Tồn kho an toàn! Hiện không có sản phẩm nào chạm ngưỡng hết hàng.");
        } else {
            System.out.printf(" [!] CÓ %d SẢN PHẨM ĐANG CHẠM HOẶC DƯỚI NGƯỠNG BÁO ĐỘNG:%n", lowStock.size());
            System.out.println("---------------------------------------------------------------------------------------------------------");
            for (Product p : lowStock) {
                p.displayInfo();
            }
        }
        System.out.println("=========================================================================================================");
    }

    // =========================================================================
    // [4] KIỂM TRA TỒN KHO (ĐỐI CHIẾU SỐ LƯỢNG VỚI SERIAL THỰC TẾ)
    // =========================================================================
    private void checkStockAudit() {
        System.out.println("\n=========================================================================================================");
        System.out.println("                     KIỂM TRA TỒN KHO (ĐỐI CHIẾU SỐ LƯỢNG VỚI SERIAL THỰC TẾ)                            ");
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
            System.out.println(" [OK] KẾT QUẢ ĐỐI CHIẾU: Số lượng tồn kho hoàn toàn khớp chính xác 100% với Serial thực tế!");
        } else {
            System.out.printf(" [!] CẢNH BÁO: Phát hiện %d sản phẩm bị lệch số lượng giữa sổ sách và Serial thực tế!%n",
                    discrepancyCount);
            boolean syncNow = InputHelper.readYesNo("➤ Bạn có muốn sửa bằng cách đồng bộ lại theo Serial thực tế không? (Y/N): ");
            if (syncNow) {
                prd.syncStockFromDetails();
                System.out.println("[OK] Đã hoàn tất đồng bộ tồn kho từ chi tiết Serial thực tế!");
            }
        }
    }

    // =========================================================================
    // [5] ★ QUẢN LÝ GIẢM GIÁ (CHỈ ADMIN)
    // =========================================================================
    private void discountMenu() {
        boolean inDiscount = true;
        while (inDiscount) {
            System.out.println("\n--------------------------------------------------------------");
            System.out.println("                    ★ QUẢN LÝ GIẢM GIÁ                        ");
            System.out.println("--------------------------------------------------------------");
            System.out.println(" [1] Thiết lập giảm giá (Mã SP, % giảm)");
            System.out.println(" [2] Xem danh sách đang giảm giá");
            System.out.println(" [0] Quay lại");
            System.out.println("--------------------------------------------------------------");

            int choice = InputHelper.readInt("➤ Chọn chức năng: ", 0, 2);
            switch (choice) {
                case 1:
                    setDiscount();
                    break;
                case 2:
                    viewDiscountedProducts();
                    break;
                case 0:
                    inDiscount = false;
                    break;
            }
        }
    }

    // [5][1] Thiết lập giảm giá
    private void setDiscount() {
        System.out.println("\n--------------------------------------------------------------");
        System.out.println("                     THIẾT LẬP GIẢM GIÁ                       ");
        System.out.println("--------------------------------------------------------------");
        String rawId = InputHelper.readString("➤ Nhập mã ID sản phẩm muốn cài đặt giảm giá: ");
        String id = ProductManager.normalizeId(rawId);
        Product p = prd.findByID(id);

        if (p == null) {
            System.out.println("[X] Không tìm thấy sản phẩm có mã: " + id);
            return;
        }

        if (!(p instanceof IDiscountable)) {
            System.out.println("[!] Sản phẩm này (Featurephone) không hỗ trợ chiết khấu giảm giá!");
            return;
        }

        System.out.printf("Sản phẩm: %s │ Giá niêm yết: %,.0f đ%n", p.getName(), p.getPrice());
        if (p.getDiscountRate() > 0) {
            System.out.printf(" (Hiện đang giảm: %.0f%% │ Giá bán thực tế: %,.0f đ)%n",
                    p.getDiscountRate() * 100, p.getSalePrice());
        }

        double rate = InputHelper.readDouble("Nhập tỷ lệ giảm giá (0.1 cho 10%, 0.2 cho 20%, gõ 0 để hủy giảm): ", 0.0, 1.0);

        if (p instanceof Accessory && rate > 0.50) {
            System.out.println("[!] CẢNH BÁO: Phụ kiện chỉ được giảm giá tối đa 50%!");
            if (InputHelper.readYesNo("Bạn có muốn đặt giảm ở mức trần 50% không? (Y/N): ")) {
                rate = 0.50;
            } else {
                System.out.println("[!] Đã hủy thao tác giảm giá.");
                return;
            }
        }

        double salePrice = prd.applyDiscount(p.getId(), rate);
        if (rate == 0) {
            System.out.printf("[OK] Đã hủy giảm giá cho '%s'. Giá bán trở về giá gốc: %,.0f đ%n",
                    p.getName(), p.getPrice());
        } else {
            System.out.printf("[OK] Đã thiết lập giảm giá %.0f%% cho '%s'!%n", rate * 100, p.getName());
            System.out.printf("     Giá gốc: %,.0f đ  ==>  Giá bán sau giảm: %,.0f đ (Không ghi đè giá gốc)%n",
                    p.getPrice(), salePrice);
        }
    }

    // [5][2] Xem danh sách đang giảm giá
    private void viewDiscountedProducts() {
        System.out.println("\n=========================================================================================================");
        System.out.println("                                  DANH SÁCH ĐANG GIẢM GIÁ                                                ");
        System.out.println("=========================================================================================================");
        System.out.printf(" %-7s │ %-28s │ %-10s │ %-15s │ %-10s │ %-16s%n",
                "MÃ SP", "TÊN SẢN PHẨM", "HÃNG", "GIÁ NIÊM YẾT", "GIẢM GIÁ", "GIÁ BÁN SAU GIẢM");
        System.out.println("────────┼──────────────────────────────┼────────────┼─────────────────┼────────────┼──────────────────");

        List<Product> discounted = prd.getDiscountedProducts();
        if (discounted.isEmpty()) {
            System.out.println("  [!] Hiện tại không có sản phẩm nào đang giảm giá.");
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
            System.out.printf("Tổng cộng: %d sản phẩm đang giảm giá.%n", discounted.size());
        }
    }

    // =========================================================================
    // [6] ĐỔI MẬT KHẨU
    // =========================================================================
    private void changePassword() {
        System.out.println("\n--------------------------------------------------------------");
        System.out.println("                         ĐỔI MẬT KHẨU                         ");
        System.out.println("--------------------------------------------------------------");
        if (currentUser == null) {
            System.out.println("[!] Phiên làm việc không xác định tài khoản.");
            return;
        }

        System.out.println("Tài khoản: " + currentUser.getUsername());
        String oldPass = InputHelper.readString("➤ Nhập mật khẩu cũ: ");

        if (!currentUser.checkLogin(currentUser.getUsername(), oldPass)) {
            System.out.println("[X] Mật khẩu cũ không chính xác! Đổi mật khẩu thất bại.");
            return;
        }

        String newPass;
        while (true) {
            newPass = InputHelper.readString("➤ Nhập mật khẩu mới (tối thiểu 6 ký tự, không chứa '|'): ");
            if (newPass.length() < 6 || newPass.contains("|")) {
                System.out.println("[X] Mật khẩu mới không hợp lệ! Vui lòng thử lại.");
                continue;
            }
            if (newPass.equals(oldPass)) {
                System.out.println("[X] Mật khẩu mới không được trùng với mật khẩu cũ!");
                continue;
            }
            break;
        }

        String confirmPass = InputHelper.readString("➤ Xác nhận lại mật khẩu mới: ");
        if (!newPass.equals(confirmPass)) {
            System.out.println("[X] Xác nhận mật khẩu không khớp! Hủy đổi mật khẩu.");
            return;
        }

        currentUser.changePassword(newPass);

        // Lưu mật khẩu mới vào data/accounts.txt
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

        System.out.println("[OK] Đổi mật khẩu thành công!");
    }
}
