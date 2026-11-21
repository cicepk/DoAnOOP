package com.smartphonestore.manager;

import com.smartphonestore.model.product.*;
import com.smartphonestore.util.DateUtil;
import com.smartphonestore.util.FileIO;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;

public class ProductManager {
    // Danh sách toàn bộ dòng sản phẩm (Master Data)
    private final List<Product> products = new ArrayList<>();
    // Đường dẫn tới file products.txt
    private final String filePath = "data/products.txt";

    // Danh sách chi tiết từng con máy / Serial (Transaction Data)
    private final List<ProductDetail> productDetails = new ArrayList<>();
    // Đường dẫn tới file product_details.txt
    private final String detailsFilePath = "data/product_details.txt";

    // Hằng số trạng thái Serial/IMEI dùng chung cho toàn hệ thống
    public static final String IN_STOCK = "IN_STOCK";
    public static final String SOLD = "SOLD";
    public static final String WARRANTY = "WARRANTY";
    public static final String TRADE_IN = "TRADE_IN";

    // Danh sách phiếu nhập kho hàng hóa (Goods Receipt Notes)
    private final List<ImportReceipt> receipts = new ArrayList<>();
    // Đường dẫn tới file import_receipts.txt
    private final String receiptsFilePath = "data/import_receipts.txt";

    // Hàm khởi tạo: nạp dữ liệu từ cả 3 file và tự động đồng bộ tồn kho
    public ProductManager() {
        loadFromFile();
        loadDetailsFromFile();
        loadReceiptsFromFile();
        syncStockFromDetails();
    }

    // ==================== QUẢN LÝ DANH MỤC SẢN PHẨM ====================

    // Lấy toàn bộ sản phẩm
    public List<Product> getALL() {
        return new ArrayList<>(products);
    }

    // Chuẩn hóa mã ID sản phẩm (ví dụ "5" -> "PRD05", "prd5" -> "PRD05")
    public static String normalizeId(String id) {
        if (id == null) return "";
        String s = id.trim().toUpperCase();
        if (s.matches("^\\d+$")) { // chỉ nhập toàn số
            return String.format("PRD%02d", Integer.parseInt(s)); // ép kiểu 
        }
        if (s.matches("^PRD\\d$")) { //chỉ nhập 1 số
            return "PRD0" + s.substring(3); // cắt và chèn thêm số 0
        }
        return s;
    }

    // Tìm kiếm theo mã ID
    public Product findByID(String id) {
        if (id == null) return null;
        String searchId = normalizeId(id);
        for (Product p : products) {
            if (p.getId().equalsIgnoreCase(searchId)) {
                return p;
            }
        }
        return null;
    }

    // Tìm kiếm theo tên hoặc hãng
    public List<Product> findByNameOrBrand(String keyword) {
        List<Product> result = new ArrayList<>();
        if (keyword == null || keyword.trim().isEmpty()) return result;
        String kw = keyword.trim().toLowerCase();
        for (Product p : products) {
            if (p.getName().toLowerCase().contains(kw) || p.getBrand().toLowerCase().contains(kw)) {
                result.add(p);
            }
        }
        return result;
    }

    // Thêm sản phẩm mới vào danh mục
    public boolean add(Product p) {
        if (p == null || findByID(p.getId()) != null) {
            return false; // rỗng hoặc trùng
        }
        products.add(p);
        return saveToFile();
    }

    // Cập nhật thông tin và giá sản phẩm theo mã ID
    public boolean updateProductInfo(String id, String newName, double newPrice, int newThreshold) {
        Product p = findByID(id);
        if (p != null) {
            boolean nameChanged = false;
            if (newName != null && !newName.trim().isEmpty() && !newName.trim().equals(p.getName())) {
                p.setName(newName.trim());
                nameChanged = true;
            }
            if (newPrice > 0 && newPrice != p.getPrice()) {
                p.setPrice(newPrice);
            }
            if (newThreshold >= 0) {
                p.setMinStockThreshold(newThreshold);
            }

            // Đồng bộ tên mới sang toàn bộ chi tiết con máy trong product_details.txt (KHÔNG sửa importPrice)
            if (nameChanged) {
                for (ProductDetail d : productDetails) {
                    if (d.getProductId().equalsIgnoreCase(p.getId())) {
                        d.setProductName(p.getName());
                    }
                }
                saveDetailsToFile();
            }

            return saveToFile();
        }
        return false;
    }

    // Giữ overload cũ để tương thích
    public boolean updateProductInfo(String id, String name, String newName, double newPrice, int newThreshold) {
        return updateProductInfo(id, newName, newPrice, newThreshold);
    }

    // Xóa sản phẩm theo ID (chỉ cho phép xóa khi KHÔNG CÓ máy nào ở trạng thái SOLD hoặc WARRANTY)
    public boolean deleteID(String id) {
        if (id == null || id.trim().isEmpty()) return false;
        String pid = normalizeId(id);
        Product p = findByID(pid);
        if (p == null) return false;

        // Kiểm tra xem sản phẩm có máy nào đã bán (SOLD) hoặc đang bảo hành (WARRANTY) không
        for (ProductDetail d : productDetails) {
            if (d.getProductId().equalsIgnoreCase(pid)) {
                if (SOLD.equalsIgnoreCase(d.getStatus()) || WARRANTY.equalsIgnoreCase(d.getStatus())) {
                    System.out.printf("[!] KHÔNG THỂ XÓA: Sản phẩm %s đang có con máy ở trạng thái '%s'!%n",
                            pid, d.getStatus());
                    System.out.println("    (Xóa sẽ làm mất dữ liệu lịch sử bên Hóa đơn bán hàng hoặc Phiếu bảo hành).");
                    return false;
                }
            }
        }

        // Nếu chỉ có hàng IN_STOCK hoặc chưa có chi tiết máy -> Cho phép xóa an toàn
        products.removeIf(item -> item.getId().equalsIgnoreCase(pid));
        productDetails.removeIf(d -> d.getProductId().equalsIgnoreCase(pid));
        saveToFile();
        saveDetailsToFile();
        return true;
    }

    // ==================== CÁC HÀM XỬ LÝ SERIAL CHO TOÀN BỘ NHÓM ====================

    // Đếm số lượng serial đang thực tế IN_STOCK
    public int countInStock(String productId) {
        int n = 0;
        for (ProductDetail d : productDetails) {
            if (d.getProductId().equalsIgnoreCase(productId) && IN_STOCK.equals(d.getStatus())) n++;
        }
        return n;
    }

    // Lúc nạp file: nếu stockQuantity lệch với số serial IN_STOCK thì lấy theo serial
    public void syncStockFromDetails() {
        boolean changed = false;
        for (Product p : products) {
            int real = countInStock(p.getId());
            if (p.getStockQuantity() != real) {
                p.setStockQuantity(real);
                changed = true;
            }
        }
        if (changed) saveToFile();
    }

    // Trả về null nếu bán được, ngược lại trả về câu báo lỗi
    public String checkSellable(String serial) {
        ProductDetail d = findDetailBySerial(serial);
        if (d == null) return "Không có serial " + serial + " trong kho";
        if (!IN_STOCK.equals(d.getStatus())) return "Serial " + serial + " đang ở trạng thái " + d.getStatus();
        if (findByID(d.getProductId()) == null) return "Sản phẩm " + d.getProductId() + " không còn trong danh mục";
        return null;// nếu bán đc trả null, còn ko thì trả về trạng thái
    }

    // TV3 gọi khi lập hóa đơn: IN_STOCK -> SOLD, tồn kho -1, lưu cả 2 file
    public boolean sellSerial(String serial) {
        if (checkSellable(serial) != null) return false;
        ProductDetail d = findDetailBySerial(serial);
        Product p = findByID(d.getProductId());
        if (!p.decreaseStock(1)) return false;
        d.setStatus(SOLD);
        return saveToFile() && saveDetailsToFile();
    }

    // TV3 gọi khi hủy hóa đơn: SOLD -> IN_STOCK, tồn kho +1
    public boolean returnSerial(String serial) {
        ProductDetail d = findDetailBySerial(serial);
        if (d == null || !SOLD.equals(d.getStatus())) return false;
        Product p = findByID(d.getProductId());
        if (p == null) return false;
        d.setStatus(IN_STOCK);
        p.increaseStock(1);
        return saveToFile() && saveDetailsToFile();
    }

    // TV4 gọi: SOLD <-> WARRANTY (không đụng tồn kho vì máy là của khách)
    public boolean setWarrantyStatus(String serial, boolean inWarranty) {
        ProductDetail d = findDetailBySerial(serial);
        if (d == null) return false;
        String from = inWarranty ? SOLD : WARRANTY;
        if (!from.equals(d.getStatus())) return false;
        d.setStatus(inWarranty ? WARRANTY : SOLD);
        return saveDetailsToFile();
    }

    // Nội bộ: Giảm tồn kho (chỉ dùng nội bộ nếu cần)
    private boolean decreaseStock(String id, int quantity) {
        Product p = findByID(id);
        if (p != null && quantity > 0) {
            boolean ok = p.decreaseStock(quantity);
            if (ok) {
                saveToFile();
            }
            return ok;
        }
        return false;
    }

    // Áp dụng tỷ lệ chiết khấu giảm giá (Lưu tỷ lệ discountRate, KHÔNG ghi đè giá gốc)
    public double applyDiscount(String id, Double discountRate) {
        Product p = findByID(id);
        if (p == null || !(p instanceof IDiscountable)) return -1;
        p.setDiscountRate(discountRate != null ? discountRate : 0.0);
        saveToFile();
        return p.getSalePrice();
    }

    // Lấy danh sách sản phẩm đang có chương trình giảm giá
    public List<Product> getDiscountedProducts() {
        List<Product> list = new ArrayList<>();
        for (Product p : products) {
            if (p.getDiscountRate() > 0) {
                list.add(p);
            }
        }
        return list;
    }

    // TV3 gọi khi thực hiện Thu cũ đổi mới: Nhận con máy cũ của khách vào kho
    public boolean addTradeInSerial(String productId, String productName, String serial, double tradeInPrice, String staffId) {
        if (isSerialExists(serial)) return false;
        String now = DateUtil.nowText();
        ProductDetail detail = new ProductDetail(productId, productName, serial, now, tradeInPrice, "THU_CU_DOI_MOI", TRADE_IN, "PN00");
        productDetails.add(detail);
        return saveDetailsToFile();
    }
// TV3 gọi khi HỦY hóa đơn thu cũ đổi mới: Trả máy cũ lại cho khách (xóa khỏi kho)
public boolean removeTradeInSerial(String serial) {
    if (serial == null || serial.trim().isEmpty()) return false;
    String s = serial.trim();
    boolean removed = productDetails.removeIf(d -> 
        d.getSerialNumber().equalsIgnoreCase(s) && TRADE_IN.equals(d.getStatus())
    );
    if (removed) {
        saveDetailsToFile();
    }
    return removed;
}
    // Tự động sinh mã sản phẩm tiếp theo (PRD10, PRD11...)
    public String generateNextID() {
        int maxIndex = 0;
        for (Product p : products) {
            if (p.getId().startsWith("PRD")) {
                try {
                    int num = Integer.parseInt(p.getId().substring(3));
                    if (num > maxIndex) maxIndex = num;
                } catch (NumberFormatException ignored) {}
            }
        }
        return String.format("PRD%02d", maxIndex + 1);
    }

    // Lấy danh sách các sản phẩm sắp hết hàng (dưới ngưỡng tối thiểu)
    public List<Product> getLowStockProducts() {
        List<Product> result = new ArrayList<>();
        for (Product p : products) {
            if (p.isLowStock()) {
                result.add(p);
            }
        }
        return result;
    }

    // ==================== QUẢN LÝ BẢNG CHI TIẾT SẢN PHẨM (SERIAL/IMEI) ====================

    // Lấy toàn bộ danh sách chi tiết con máy
    public List<ProductDetail> getAllProductDetails() {
        return new ArrayList<>(productDetails);
    }

    // Lọc chi tiết con máy theo Mã sản phẩm (PRD01)
    public List<ProductDetail> getDetailsByProductId(String productId) {
        List<ProductDetail> result = new ArrayList<>();
        if (productId == null || productId.trim().isEmpty()) return result;
        String pid = normalizeId(productId);
        for (ProductDetail d : productDetails) {
            if (d.getProductId().equalsIgnoreCase(pid)) {
                result.add(d);
            }
        }
        return result;
    }

    // Lọc chi tiết con máy theo Hãng sản xuất (Apple, Samsung, Xiaomi...)
    public List<ProductDetail> getDetailsByBrand(String brand) {
        List<ProductDetail> result = new ArrayList<>();
        if (brand == null || brand.trim().isEmpty()) return result;
        String b = brand.trim().toLowerCase();
        for (ProductDetail d : productDetails) {
            Product p = findByID(d.getProductId());
            if (p != null && p.getBrand().toLowerCase().contains(b)) {
                result.add(d);
            }
        }
        return result;
    }

    // Kiểm tra xem số Seri / IMEI đã tồn tại trong hệ thống chưa
    public boolean isSerialExists(String serial) {
        if (serial == null || serial.trim().isEmpty()) return false;
        String sn = serial.trim().toUpperCase();
        for (ProductDetail d : productDetails) {
            if (d.getSerialNumber().equalsIgnoreCase(sn)) {
                return true;
            }
        }
        return false;
    }

    // Tìm chi tiết con máy theo số Seri
    public ProductDetail findDetailBySerial(String serial) {
        if (serial == null) return null;
        for (ProductDetail d : productDetails) {
            if (d.getSerialNumber().equalsIgnoreCase(serial.trim())) {
                return d;
            }
        }
        return null;
    }

    // Thêm danh sách chi tiết máy và lưu file
    public boolean addProductDetails(List<ProductDetail> newDetails) {
        if (newDetails == null || newDetails.isEmpty()) return false;
        for (ProductDetail d : newDetails) {
            if (!isSerialExists(d.getSerialNumber())) {
                productDetails.add(d);
            }
        }
        return saveDetailsToFile();
    }

    // ==================== ĐỌC VÀ GHI FILE TEXT ====================

    // Nạp dữ liệu từ products.txt
    public void loadFromFile() {
        products.clear();
        List<String> lines = FileIO.readLines(filePath);
        for (String line : lines) {
            try {
                String[] parts = line.split("\\|", -1);
                if (parts.length >= 8) {
                    String id = parts[0].trim();
                    String name = parts[1].trim();
                    String brand = parts[2].trim();
                    double price = Double.parseDouble(parts[3].trim());
                    int stock = Integer.parseInt(parts[4].trim());
                    String type = parts[5].trim().toUpperCase();
                    int threshold = Integer.parseInt(parts[6].trim());
                    String extra = parts[7].trim();

                    switch (type) {
                        case "SMARTPHONE": {
                            int storage = Integer.parseInt(extra);
                            String os = brand.equalsIgnoreCase("Apple") ? "iOS" : "Android";
                            products.add(new Smartphone(id, name, brand, price, stock, threshold, os, storage));
                            break;
                        }
                        case "FEATUREPHONE": {
                            int battery = Integer.parseInt(extra);
                            products.add(new Featurephone(id, name, brand, price, stock, threshold, "KaiOS", battery));
                            break;
                        }
                        case "ACCESSORY": {
                            products.add(new Accessory(id, name, brand, price, stock, threshold, extra));
                            break;
                        }
                    }

                    if (!products.isEmpty() && parts.length >= 9) {
                        try {
                            double dr = Double.parseDouble(parts[8].trim());
                            products.get(products.size() - 1).setDiscountRate(dr);
                        } catch (NumberFormatException ignored) {}
                    }
                }
            } catch (Exception ex) {
                System.err.println("Bỏ qua dòng lỗi trong products.txt: " + ex.getMessage());
            }
        }
    }

    // Ghi toàn bộ danh sách sản phẩm xuống products.txt
    public boolean saveToFile() {
        Path path = Paths.get(filePath);
        if (products.isEmpty() && Files.exists(path)) {
            try {
                if (Files.size(path) > 200) {
                    System.err.println("[CẢNH BÁO]: Danh sách sản phẩm trong bộ nhớ rỗng, hủy ghi đè để bảo vệ file products.txt!");
                    return false;
                }
            } catch (IOException ignored) {}
        }

        // Sắp xếp sản phẩm theo mã ID (PRD01, PRD02...)
        products.sort((p1, p2) -> p1.getId().compareToIgnoreCase(p2.getId()));

        List<String> lines = new ArrayList<>();
        // KHÔNG tự thêm dòng "# Format..." vì FileIO.writeLines tự động bảo lưu dòng chú thích # có sẵn
        for (Product p : products) {
            String line = p.toFileString();
            if (p.getDiscountRate() > 0) {
                line += String.format(Locale.US, "|%.2f", p.getDiscountRate());
            }
            lines.add(line);
        }
        return FileIO.writeLines(filePath, lines);
    }

    // Nạp dữ liệu từ product_details.txt
    public void loadDetailsFromFile() {
        productDetails.clear();
        List<String> lines = FileIO.readLines(detailsFilePath);
        for (String line : lines) {
            try {
                // Format: productId|productName|serialNumber|importDate|importPrice|supplier|status[|receiptId]
                String[] parts = line.split("\\|", -1);
                if (parts.length >= 8) {
                    String productId = parts[0].trim();
                    String productName = parts[1].trim();
                    String serialNumber = parts[2].trim();
                    String importDate = parts[3].trim();
                    double importPrice = Double.parseDouble(parts[4].trim());
                    String supplier = parts[5].trim();
                    String status = parts[6].trim();
                    String receiptId = parts[7].trim();

                    productDetails.add(new ProductDetail(productId, productName, serialNumber, importDate, importPrice, supplier, status, receiptId));
                } else if (parts.length >= 7) {
                    String productId = parts[0].trim();
                    String productName = parts[1].trim();
                    String serialNumber = parts[2].trim();
                    String importDate = parts[3].trim();
                    double importPrice = Double.parseDouble(parts[4].trim());
                    String supplier = parts[5].trim();
                    String status = parts[6].trim();

                    productDetails.add(new ProductDetail(productId, productName, serialNumber, importDate, importPrice, supplier, status, "PN00"));
                } else if (parts.length >= 6) {
                    // Tương thích ngược với format 6 cột cũ
                    String productId = parts[0].trim();
                    String serialNumber = parts[1].trim();
                    String importDate = parts[2].trim();
                    double importPrice = Double.parseDouble(parts[3].trim());
                    String supplier = parts[4].trim();
                    String status = parts[5].trim();
                    Product p = findByID(productId);
                    String pName = p != null ? p.getName() : "Sản phẩm " + productId;

                    productDetails.add(new ProductDetail(productId, pName, serialNumber, importDate, importPrice, supplier, status, "PN00"));
                }
            } catch (Exception ex) {
                System.err.println("Bỏ qua dòng lỗi trong product_details.txt: " + ex.getMessage());
            }
        }
    }

    // Ghi toàn bộ chi tiết máy xuống product_details.txt
    public boolean saveDetailsToFile() {
        Path path = Paths.get(detailsFilePath);
        if (productDetails.isEmpty() && Files.exists(path)) {
            try {
                if (Files.size(path) > 200) {
                    System.err.println("[CẢNH BÁO]: Danh sách chi tiết trong bộ nhớ rỗng, hủy ghi đè để bảo vệ file product_details.txt!");
                    return false;
                }
            } catch (IOException ignored) {}
        }

        // Sắp xếp chi tiết máy gom theo mã sản phẩm (PRD01, PRD02...) rồi đến số Seri
        productDetails.sort((d1, d2) -> {
            int cmp = d1.getProductId().compareToIgnoreCase(d2.getProductId());
            if (cmp != 0) return cmp;
            return d1.getSerialNumber().compareToIgnoreCase(d2.getSerialNumber());
        });

        List<String> lines = new ArrayList<>();
        // KHÔNG tự thêm dòng "# Format..." vì FileIO.writeLines tự động bảo lưu dòng chú thích # có sẵn
        for (ProductDetail d : productDetails) {
            lines.add(d.toFileString());
        }
        return FileIO.writeLines(detailsFilePath, lines);
    }

    // ==================== QUẢN LÝ BIÊN BẢN PHIẾU NHẬP KHO (INBOUND RECEIPTS) ====================

    // Lấy toàn bộ danh sách biên bản phiếu nhập kho
    public List<ImportReceipt> getAllReceipts() {
        return new ArrayList<>(receipts);
    }

    // Tìm kiếm biên bản phiếu nhập theo Mã phiếu (PN01, PN02...)
    public ImportReceipt findReceiptById(String id) {
        if (id == null || id.trim().isEmpty()) return null;
        String searchId = id.trim().toUpperCase();
        for (ImportReceipt r : receipts) {
            if (r.getReceiptId().equalsIgnoreCase(searchId)) {
                return r;
            }
        }
        return null;
    }

    // Tự động sinh mã phiếu nhập tiếp theo (PN01, PN02...)
    public String nextReceiptId() {
        int maxIndex = 0;
        for (ImportReceipt r : receipts) {
            if (r.getReceiptId().startsWith("PN")) {
                try {
                    int num = Integer.parseInt(r.getReceiptId().substring(2));
                    if (num > maxIndex) maxIndex = num;
                } catch (NumberFormatException ignored) {}
            }
        }
        return String.format("PN%02d", maxIndex + 1);
    }

    // Thêm biên bản phiếu nhập mới và tự động lưu file
    public boolean addReceipt(ImportReceipt receipt) {
        if (receipt == null || findReceiptById(receipt.getReceiptId()) != null) {
            return false;
        }
        receipts.add(receipt);
        return saveReceiptsToFile();
    }

    // Nạp dữ liệu biên bản phiếu nhập từ import_receipts.txt
    public void loadReceiptsFromFile() {
        receipts.clear();
        List<String> lines = FileIO.readLines(receiptsFilePath);
        for (String line : lines) {
            try {
                String[] parts = line.split("\\|", -1);
                if (parts.length >= 6) {
                    String rId = parts[0].trim();
                    String staffId = parts[1].trim();
                    String date = parts[2].trim();
                    String supplier = parts[3].trim();
                    int qty = Integer.parseInt(parts[4].trim());
                    double cost = Double.parseDouble(parts[5].trim());
                    receipts.add(new ImportReceipt(rId, staffId, date, supplier, qty, cost));
                }
            } catch (Exception ex) {
                System.err.println("Bỏ qua dòng lỗi trong import_receipts.txt: " + ex.getMessage());
            }
        }
    }

    // Ghi toàn bộ danh sách phiếu nhập xuống import_receipts.txt
    public boolean saveReceiptsToFile() {
        receipts.sort((r1, r2) -> r1.getReceiptId().compareToIgnoreCase(r2.getReceiptId()));
        List<String> lines = new ArrayList<>();
        for (ImportReceipt r : receipts) {
            lines.add(r.toFileString());
        }
        return FileIO.writeLines(receiptsFilePath, lines);
    }
}
