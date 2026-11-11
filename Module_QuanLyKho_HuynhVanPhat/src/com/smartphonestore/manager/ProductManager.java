package src.com.smartphonestore.manager;

import src.com.smartphonestore.model.product.*;
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

    // Hàm khởi tạo: nạp dữ liệu từ cả 2 file
    public ProductManager() {
        loadFromFile();
        loadDetailsFromFile();
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
            boolean priceChanged = false;                           //Tên mới khác tên cũ
            if (newName != null && !newName.trim().isEmpty() && !newName.trim().equals(p.getName())) {
                p.setName(newName.trim());
                nameChanged = true;
            }
            if (newPrice > 0 && newPrice != p.getPrice()) {
                p.setPrice(newPrice);
                priceChanged = true;
            }
            if (newThreshold >= 0) {
                p.setMinStockThreshold(newThreshold);
            }

            // Đồng bộ tên và giá mới sang toàn bộ chi tiết con máy trong product_details.txt
            if (nameChanged || priceChanged) {
                for (ProductDetail d : productDetails) {
                    if (d.getProductId().equalsIgnoreCase(p.getId())) {
                        if (nameChanged) d.setProductName(p.getName());//nếu True thì gán tên mới
                        if (priceChanged) d.setImportPrice(newPrice);// giá mới 
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

    // Xóa sản phẩm theo ID (xóa luôn cả chi tiết máy liên quan)
    public boolean deleteID(String id) {
        for (int i = 0; i < products.size(); i++) {
            if (products.get(i).getId().equalsIgnoreCase(id.trim())) {
                products.remove(i);
                productDetails.removeIf(d -> d.getProductId().equalsIgnoreCase(id.trim()));
                saveToFile();
                saveDetailsToFile();
                return true;
            }
        }
        return false;
    }

    // Nhập hàng tăng số lượng tồn kho (đơn giản)
    public boolean StockIn(String id, int quantity) {
        Product p = findByID(id);
        if (p != null && quantity > 0) {
            p.increaseStock(quantity);
            return saveToFile();
        }
        return false;
    }

    // Xuất hàng / Bán hàng (TV3 gọi)
    public boolean decreaseStock(String id, int quantity) {
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

    // Áp dụng giảm giá và cập nhật giá mới vào cả 2 file: products.txt và product_details.txt
    public double applyDiscount(String id, Double discountRate) {
        Product p = findByID(id);
        if (p instanceof IDiscountable) {
            IDiscountable item = (IDiscountable) p;
            double discountedPrice = item.calculateDiscountedPrice(discountRate);
            if (discountedPrice > 0 && discountedPrice != p.getPrice()) {
                p.setPrice(discountedPrice);
                // Đồng bộ giá mới vào toàn bộ con máy thuộc mã sản phẩm này trong product_details.txt
                for (ProductDetail d : productDetails) {
                    if (d.getProductId().equalsIgnoreCase(p.getId())) {
                        d.setImportPrice(discountedPrice);
                    }
                }
                saveToFile();
                saveDetailsToFile();
            }
            return discountedPrice;
        }
        return -1;
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
        Path path = Paths.get(filePath);
        if (!Files.exists(path)) return;

        try (BufferedReader br = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.replace("\uFEFF", "").trim();
                if (line.isEmpty() || line.startsWith("#")) continue;

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
                    }
                } catch (Exception ex) {
                    System.err.println("Bo qua dong loi trong products.txt: " + ex.getMessage());
                }
            }
        } catch (Exception e) {
            System.err.println("Loi nap file products.txt: " + e.getMessage());
        }
    }

    // Ghi toàn bộ danh sách sản phẩm xuống products.txt
    public boolean saveToFile() {
        try {
            Path path = Paths.get(filePath);
            if (products.isEmpty() && Files.exists(path) && Files.size(path) > 200) {
                System.err.println("[CANH BAO]: Danh sach san pham trong bo nho rong, huy ghi de de bao ve file products.txt!");
                return false;
            }
            if (path.getParent() != null && !Files.exists(path.getParent())) {
                Files.createDirectories(path.getParent());
            }

            // Sắp xếp sản phẩm theo mã ID (PRD01, PRD02...)
            products.sort((p1, p2) -> p1.getId().compareToIgnoreCase(p2.getId()));

            List<String> lines = new ArrayList<>();
            lines.add("# Format: id|name|brand|price|stockQuantity|productType(SMARTPHONE/FEATUREPHONE/ACCESSORY)|minThreshold|extraValue(Storage/BatteryDays/AccessoryType)");
            for (Product p : products) {
                lines.add(p.toFileString());
            }
            Files.write(path, lines, StandardCharsets.UTF_8);
            return true;
        } catch (Exception e) {
            System.err.println("Loi luu file products.txt: " + e.getMessage());
            return false;
        }
    }

    // Nạp dữ liệu từ product_details.txt
    public void loadDetailsFromFile() {
        productDetails.clear();
        Path path = Paths.get(detailsFilePath);
        if (!Files.exists(path)) return;

        try (BufferedReader br = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.replace("\uFEFF", "").trim();
                if (line.isEmpty() || line.startsWith("#")) continue;

                try {
                    // Format mới: productId|productName|serialNumber|importDate|importPrice|supplier|status (7 cột)
                    String[] parts = line.split("\\|", -1);
                    if (parts.length >= 7) {
                        String productId = parts[0].trim();
                        String productName = parts[1].trim();
                        String serialNumber = parts[2].trim();
                        String importDate = parts[3].trim();
                        double importPrice = Double.parseDouble(parts[4].trim());
                        String supplier = parts[5].trim();
                        String status = parts[6].trim();

                        productDetails.add(new ProductDetail(productId, productName, serialNumber, importDate, importPrice, supplier, status));
                    } else if (parts.length >= 6) {
                        // Tương thích ngược với format 6 cột cũ
                        String productId = parts[0].trim();
                        String serialNumber = parts[1].trim();
                        String importDate = parts[2].trim();
                        double importPrice = Double.parseDouble(parts[3].trim());
                        String supplier = parts[4].trim();
                        String status = parts[5].trim();
                        Product p = findByID(productId);
                        String pName = p != null ? p.getName() : "San pham " + productId;

                        productDetails.add(new ProductDetail(productId, pName, serialNumber, importDate, importPrice, supplier, status));
                    }
                } catch (Exception ex) {
                    System.err.println("Bo qua dong loi trong product_details.txt: " + ex.getMessage());
                }
            }
        } catch (Exception e) {
            System.err.println("Loi nap file product_details.txt: " + e.getMessage());
        }
    }

    // Ghi toàn bộ chi tiết máy xuống product_details.txt
    public boolean saveDetailsToFile() {
        try {
            Path path = Paths.get(detailsFilePath);
            if (productDetails.isEmpty() && Files.exists(path) && Files.size(path) > 200) {
                System.err.println("[CANH BAO]: Danh sach chi tiet trong bo nho rong, huy ghi de de bao ve file product_details.txt!");
                return false;
            }
            if (path.getParent() != null && !Files.exists(path.getParent())) {
                Files.createDirectories(path.getParent());
            }

            // Sắp xếp chi tiết máy gom theo mã sản phẩm (PRD01, PRD02...) rồi đến số Seri
            productDetails.sort((d1, d2) -> {
                int cmp = d1.getProductId().compareToIgnoreCase(d2.getProductId());
                if (cmp != 0) return cmp;
                return d1.getSerialNumber().compareToIgnoreCase(d2.getSerialNumber());
            });

            List<String> lines = new ArrayList<>();
            lines.add("# Format: productId|productName|serialNumber|importDate|importPrice|supplier|status");
            for (ProductDetail d : productDetails) {
                lines.add(d.toFileString());
            }
            Files.write(path, lines, StandardCharsets.UTF_8);
            return true;
        } catch (Exception e) {
            System.err.println("Loi luu file product_details.txt: " + e.getMessage());
            return false;
        }
    }
}
