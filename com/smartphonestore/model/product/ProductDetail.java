package com.smartphonestore.model.product;

import com.smartphonestore.exception.ValidationException;

/**
 * Lớp đại diện cho một con máy / sản phẩm vật lý cụ thể trong kho.
 * Mỗi sản phẩm có số Seri / IMEI riêng biệt, tên sản phẩm, ngày nhập, giá nhập và nhà cung cấp.
 */
public class ProductDetail {
    private String productId;     // Khóa ngoại trỏ đến Product (PRD01, PRD02...)
    private String productName;   // Tên sản phẩm cụ thể
    private String serialNumber;  // Khóa chính: Số Seri / IMEI duy nhất cho từng máy
    private String importDate;    // Ngày giờ nhập kho (dd/MM/yyyy HH:mm:ss)
    private double importPrice;   // Giá nhập hàng từ nhà cung cấp (VND)
    private String supplier;      // Tên nhà cung cấp (FPT, Viettel, Digiworld...)
    private String status;        // Trạng thái: IN_STOCK (Trong kho), SOLD (Đã bán), WARRANTY (Bảo hành)

    public ProductDetail(String productId, String productName, String serialNumber, String importDate, double importPrice, String supplier, String status) {
        setProductId(productId);
        setProductName(productName);
        setSerialNumber(serialNumber);
        setImportDate(importDate);
        setImportPrice(importPrice);
        setSupplier(supplier);
        setStatus(status);
    }

    // Constructor tiện lợi khi mới nhập kho (mặc định trạng thái IN_STOCK)
    public ProductDetail(String productId, String productName, String serialNumber, String importDate, double importPrice, String supplier) {
        this(productId, productName, serialNumber, importDate, importPrice, supplier, "IN_STOCK");
    }

    // Chuyển đối tượng thành chuỗi lưu file theo định dạng:
    // productId|productName|serialNumber|importDate|importPrice|supplier|status
    public String toFileString() {
        return String.format("%s|%s|%s|%s|%.0f|%s|%s",
                productId, productName, serialNumber, importDate, importPrice, supplier, status);
    }

    // Hiển thị một dòng chi tiết đẹp mắt trên console
    public void displayRow(String brand) {
        String statusVi = "IN_STOCK".equalsIgnoreCase(status) ? "Trong kho" :
                         ("SOLD".equalsIgnoreCase(status) ? "Đã bán" : "Bảo hành");
        System.out.printf(" %-7s │ %-26s │ %-8s │ %-18s │ %-19s │ %,12.0f đ │ %-14s │ %-9s%n",
                productId,
                productName != null ? (productName.length() > 26 ? productName.substring(0, 23) + "..." : productName) : "N/A",
                brand != null ? brand : "N/A",
                serialNumber,
                importDate,
                importPrice,
                supplier != null ? supplier : "N/A",
                statusVi);
    }

    // Overload displayRow giữ tương thích
    public void displayRow(String pName, String brand) {
        if (pName != null && !pName.isEmpty()) {
            this.productName = pName;
        }
        displayRow(brand);
    }

    // ==================== GETTER / SETTER CÓ KIỂM TRA DỮ LIỆU ====================

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        if (productId == null || productId.trim().isEmpty()) {
            throw new ValidationException("[LỖI]: Mã sản phẩm không được để trống!");
        }
        this.productId = productId.trim().toUpperCase();
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        if (productName == null || productName.trim().isEmpty()) {
            this.productName = "Sản phẩm " + (productId != null ? productId : "");
        } else {
            this.productName = productName.trim();
        }
    }

    public String getSerialNumber() {
        return serialNumber;
    }

    public void setSerialNumber(String serialNumber) {
        if (serialNumber == null || serialNumber.trim().isEmpty()) {
            throw new ValidationException("[LỖI]: Số Seri/IMEI không được để trống!");
        }
        this.serialNumber = serialNumber.trim().toUpperCase();
    }

    public String getImportDate() {
        return importDate;
    }

    public void setImportDate(String importDate) {
        if (importDate == null || importDate.trim().isEmpty()) {
            throw new ValidationException("[LỖI]: Ngày nhập không được để trống!");
        }
        this.importDate = importDate.trim();
    }

    public double getImportPrice() {
        return importPrice;
    }

    public void setImportPrice(double importPrice) {
        if (importPrice <= 0) {
            throw new ValidationException("[LỖI]: Giá nhập sản phẩm phải lớn hơn 0 VNĐ!");
        }
        this.importPrice = importPrice;
    }

    public String getSupplier() {
        return supplier;
    }

    public void setSupplier(String supplier) {
        if (supplier == null || supplier.trim().isEmpty()) {
            throw new ValidationException("[LỖI]: Tên nhà cung cấp không được để trống!");
        }
        this.supplier = supplier.trim();
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        if (status == null || status.trim().isEmpty()) {
            this.status = "IN_STOCK";
        } else {
            String s = status.trim().toUpperCase();
            if (!s.matches("^(IN_STOCK|SOLD|WARRANTY)$")) {
                this.status = "IN_STOCK";
            } else {
                this.status = s;
            }
        }
    }
}
