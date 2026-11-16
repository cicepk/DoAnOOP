# MẪU CODE THAM KHẢO & TRIỂN KHAI HỆ THỐNG
# HỆ THỐNG BÁN HÀNG ĐIỆN THOẠI OFFLINE (JAVA CONSOLE)

Tài liệu này cung cấp mã nguồn khung (Skeleton / Boilerplate Code) chuẩn mực, sẵn sàng biên dịch và chạy thử. Mã nguồn triển khai trực tiếp luồng: **Màn hình Đăng nhập ➔ Điều hướng theo 3 Vai trò (Admin, Kho, Bán hàng) ➔ Bảng Menu riêng biệt từng vai trò**.

---

## 1. Định Nghĩa Vai Trò & Tài Khoản Đăng Nhập

### 1.1. Enum `Role.java`
```java
package com.smartphonestore.model.enums;

public enum Role {
    ADMIN("Quản trị viên"),
    WAREHOUSE("Nhân viên kho"),
    SALES("Nhân viên bán hàng");

    private final String description;

    Role(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public static Role fromString(String roleStr) {
        if (roleStr == null) return SALES;
        for (Role r : Role.values()) {
            if (r.name().equalsIgnoreCase(roleStr.trim())) {
                return r;
            }
        }
        return SALES;
    }
}
```

### 1.2. Model `Account.java` (Thành viên 1)
```java
package com.smartphonestore.model.auth;

import com.smartphonestore.model.enums.Role;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Account {
    private String id;
    private String username;
    private String password;
    private Role role;
    private String status; // ACTIVE, LOCKED
    private String lastLoginTime;

    public Account(String id, String username, String password, Role role, String status, String lastLoginTime) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.role = role;
        this.status = status;
        this.lastLoginTime = lastLoginTime;
    }

    public boolean checkLogin(String user, String pass) {
        return this.username.equalsIgnoreCase(user) && this.password.equals(pass) && "ACTIVE".equalsIgnoreCase(this.status);
    }

    public void changePassword(String newPass) {
        this.password = newPass;
    }

    public void updateLastLoginTime() {
        this.lastLoginTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));
    }

    public String getId() { return id; }
    public String getUsername() { return username; }
    public Role getRole() { return role; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getLastLoginTime() { return lastLoginTime; }
}
```

---

## 2. Quản Lý Kho & Vòng Đời Serial: `ProductManager.java` (Thành viên 2)

```java
package com.smartphonestore.manager;

import com.smartphonestore.model.product.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;

public class ProductManager {
    private final List<Product> products = new ArrayList<>();
    private final List<ProductDetail> productDetails = new ArrayList<>();
    private final String filePath = "data/products.txt";
    private final String detailsFilePath = "data/product_details.txt";

    public static final String IN_STOCK = "IN_STOCK";
    public static final String SOLD = "SOLD";
    public static final String WARRANTY = "WARRANTY";

    public ProductManager() {
        loadFromFile();
        loadDetailsFromFile();
        syncStockFromDetails();
    }

    // Đếm số lượng serial đang thực tế IN_STOCK
    public int countInStock(String productId) {
        int count = 0;
        for (ProductDetail d : productDetails) {
            if (d.getProductId().equalsIgnoreCase(productId) && IN_STOCK.equals(d.getStatus())) {
                count++;
            }
        }
        return count;
    }

    // Tự động đồng bộ tồn kho từ file chi tiết sang file sản phẩm
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

    // Kiểm tra máy có đủ điều kiện bán
    public String checkSellable(String serial) {
        ProductDetail d = findDetailBySerial(serial);
        if (d == null) return "Không tìm thấy serial " + serial + " trong kho";
        if (!IN_STOCK.equals(d.getStatus())) return "Serial " + serial + " đang ở trạng thái: " + d.getStatus();
        if (findByID(d.getProductId()) == null) return "Sản phẩm không tồn tại trong danh mục";
        return null;
    }

    // TV3 gọi khi xuất bán máy: IN_STOCK -> SOLD, tồn kho -1
    public boolean sellSerial(String serial) {
        if (checkSellable(serial) != null) return false;
        ProductDetail d = findDetailBySerial(serial);
        Product p = findByID(d.getProductId());
        if (!p.decreaseStock(1)) return false;
        d.setStatus(SOLD);
        return saveToFile() && saveDetailsToFile();
    }

    // TV3 gọi khi hủy đơn: SOLD -> IN_STOCK, tồn kho +1
    public boolean returnSerial(String serial) {
        ProductDetail d = findDetailBySerial(serial);
        if (d == null || !SOLD.equals(d.getStatus())) return false;
        Product p = findByID(d.getProductId());
        if (p == null) return false;
        d.setStatus(IN_STOCK);
        p.increaseStock(1);
        return saveToFile() && saveDetailsToFile();
    }

    // TV4 gọi khi tiếp nhận/hoàn tất bảo hành: SOLD <-> WARRANTY
    public boolean setWarrantyStatus(String serial, boolean inWarranty) {
        ProductDetail d = findDetailBySerial(serial);
        if (d == null) return false;
        String from = inWarranty ? SOLD : WARRANTY;
        if (!from.equals(d.getStatus())) return false;
        d.setStatus(inWarranty ? WARRANTY : SOLD);
        return saveDetailsToFile();
    }

    // Áp dụng chiết khấu: CHỈ đổi giá bán niêm yết trong products.txt, KHÔNG đổi importPrice
    public double applyDiscount(String id, Double discountRate) {
        Product p = findByID(id);
        if (!(p instanceof IDiscountable)) return -1;
        double newPrice = ((IDiscountable) p).calculateDiscountedPrice(discountRate);
        if (newPrice > 0 && newPrice != p.getPrice()) {
            p.setPrice(newPrice);
            saveToFile();
        }
        return newPrice;
    }

    public Product findByID(String id) {
        if (id == null) return null;
        for (Product p : products) {
            if (p.getId().equalsIgnoreCase(id.trim())) return p;
        }
        return null;
    }

    public ProductDetail findDetailBySerial(String serial) {
        if (serial == null) return null;
        for (ProductDetail d : productDetails) {
            if (d.getSerialNumber().equalsIgnoreCase(serial.trim())) return d;
        }
        return null;
    }

    // Load/Save file chuẩn UTF-8
    // ...
}
```

---

## 3. Lớp Cha & Đa Hình Bảo Hành: `Product.java`, `Phone.java`, `Accessory.java`

### 3.1. Lớp Cha `Product.java`
```java
package com.smartphonestore.model.product;

public abstract class Product {
    private String id;
    private String name;
    private String brand;
    private double price;
    private int stockQuantity;
    private int minStockThreshold;

    public Product(String id, String name, String brand, double price, int stockQuantity, int minStockThreshold) {
        this.id = id;
        this.name = name;
        this.brand = brand;
        this.price = price;
        this.stockQuantity = stockQuantity;
        this.minStockThreshold = minStockThreshold;
    }

    // Phương thức trừu tượng: Mọi sản phẩm bắt buộc phải có thời hạn bảo hành
    public abstract int getWarrantyMonths();

    public boolean isLowStock() {
        return this.stockQuantity <= this.minStockThreshold;
    }

    public void increaseStock(int amount) {
        if (amount > 0) this.stockQuantity += amount;
    }

    public boolean decreaseStock(int amount) {
        if (amount > 0 && this.stockQuantity >= amount) {
            this.stockQuantity -= amount;
            return true;
        }
        return false;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getBrand() { return brand; }
    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }
    public int getStockQuantity() { return stockQuantity; }
    public void setStockQuantity(int qty) { this.stockQuantity = qty; }
    public int getMinStockThreshold() { return minStockThreshold; }
}
```

### 3.2. Lớp Con `Phone.java`
```java
package com.smartphonestore.model.product;

public abstract class Phone extends Product {
    private String operatingSystem;

    public Phone(String id, String name, String brand, double price, int stockQuantity, int minStockThreshold, String operatingSystem) {
        super(id, name, brand, price, stockQuantity, minStockThreshold);
        this.operatingSystem = operatingSystem;
    }

    @Override
    public int getWarrantyMonths() {
        if ("IOS".equalsIgnoreCase(operatingSystem) || "Apple".equalsIgnoreCase(getBrand())) {
            return 12; // iPhone bảo hành 12 tháng
        } else if ("Android".equalsIgnoreCase(operatingSystem)) {
            return 18; // Android bảo hành 18 tháng
        }
        return 6;
    }
}
```

### 3.3. Lớp Con `Accessory.java`
```java
package com.smartphonestore.model.product;

public class Accessory extends Product implements IDiscountable {
    private String accessoryType; // SAC, TAI_NGHE, OP_LUNG, CUONG_LUC

    public Accessory(String id, String name, String brand, double price, int stockQuantity, int minStockThreshold, String accessoryType) {
        super(id, name, brand, price, stockQuantity, minStockThreshold);
        this.accessoryType = accessoryType;
    }

    @Override
    public int getWarrantyMonths() {
        return 6; // Phụ kiện bảo hành cố định 6 tháng
    }

    @Override
    public double calculateDiscountedPrice(double discountRate) {
        if (discountRate < 0.0 || discountRate > 0.5) return getPrice();
        return getPrice() * (1 - discountRate);
    }
}
```

---

## 4. Chi Tiết Hóa Đơn Có Serial: `InvoiceDetail.java` (Thành viên 3)

```java
package com.smartphonestore.model.sale;

public class InvoiceDetail {
    private final String invoiceId;
    private final String productId;
    private final String productName;
    private final String serialNumber;
    private final double unitPrice;
    private final int quantity = 1; // Mỗi serial tương ứng đúng 1 máy

    public InvoiceDetail(String invoiceId, String productId, String productName, String serialNumber, double unitPrice) {
        this.invoiceId = invoiceId;
        this.productId = productId;
        this.productName = productName;
        this.serialNumber = serialNumber;
        this.unitPrice = unitPrice;
    }

    // Format lưu file: invoiceId|productId|productName|serialNumber|unitPrice|quantity
    public String toFileString() {
        return String.join("|", invoiceId, productId, productName, serialNumber,
                String.format(java.util.Locale.US, "%.0f", unitPrice), "1");
    }

    public String getInvoiceId() { return invoiceId; }
    public String getProductId() { return productId; }
    public String getProductName() { return productName; }
    public String getSerialNumber() { return serialNumber; }
    public double getUnitPrice() { return unitPrice; }
}
```

---

## 5. Phiếu Tiếp Nhận Bảo Hành: `WarrantyClaim.java` (Thành viên 4)

```java
package com.smartphonestore.model.customer;

import com.smartphonestore.util.DateUtil;
import java.time.LocalDate;

public class WarrantyClaim {
    private final String claimId;
    private final String customerId;
    private final String productId;
    private final String serialNumber;
    private String issueDescription;
    private final String receivedDate;     // dd/MM/yyyy HH:mm:ss
    private String claimStatus;            // TIEP_NHAN, DANG_SUA, HOAN_TAT, TU_CHOI
    private final String saleDate;         // dd/MM/yyyy
    private final int warrantyMonths;      // Lấy từ Product.getWarrantyMonths()

    public WarrantyClaim(String claimId, String customerId, String productId, String serialNumber,
                         String issueDescription, String receivedDate, String claimStatus,
                         String saleDate, int warrantyMonths) {
        this.claimId = claimId;
        this.customerId = customerId;
        this.productId = productId;
        this.serialNumber = serialNumber;
        this.issueDescription = issueDescription;
        this.receivedDate = receivedDate;
        this.claimStatus = claimStatus;
        this.saleDate = saleDate;
        this.warrantyMonths = warrantyMonths;
    }

    // Format lưu 9 cột chuẩn mới:
    // id|mã khách|mã sản phẩm|seri|lỗi|ngày giờ nhận|trạng thái|ngày bán|số tháng BH
    public String toFileLine() {
        return String.join("|", claimId, customerId, productId, serialNumber, issueDescription,
                receivedDate, claimStatus, saleDate, String.valueOf(warrantyMonths));
    }

    public static WarrantyClaim fromFileLine(String line) {
        String[] f = line.split("\\|", -1);
        if (f.length < 9) throw new IllegalArgumentException("Dòng bảo hành phải có đủ 9 cột");
        return new WarrantyClaim(f[0], f[1], f[2], f[3], f[4], f[5], f[6], f[7], Integer.parseInt(f[8]));
    }

    public void updateStatus(String newStatus) {
        this.claimStatus = newStatus;
    }

    public boolean isClosed() {
        return "HOAN_TAT".equalsIgnoreCase(claimStatus) || "TU_CHOI".equalsIgnoreCase(claimStatus);
    }

    public String getClaimId() { return claimId; }
    public String getSerialNumber() { return serialNumber; }
    public String getClaimStatus() { return claimStatus; }
}
```

---

## 6. Điểm Vào Hệ Thống: `Main.java`

```java
package com.smartphonestore;

import com.smartphonestore.manager.*;
import com.smartphonestore.view.*;

public class Main {
    public static void main(String[] args) {
        AccountManager accountManager = new AccountManager();
        EmployeeManager employeeManager = new EmployeeManager();
        ProductManager productManager = new ProductManager();
        SalesManager salesManager = new SalesManager(productManager);
        CustomerManager customerManager = new CustomerManager(salesManager, productManager);

        new LoginView(accountManager, employeeManager, productManager, salesManager, customerManager).start();
    }
}
```
