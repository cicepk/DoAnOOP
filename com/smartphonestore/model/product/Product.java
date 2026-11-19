package com.smartphonestore.model.product;

import com.smartphonestore.exception.ValidationException;

public abstract class Product {
    private String id;
    private String name;
    private String brand; // hãng
    private double price;
    private int stockQuantity;
    // thuộc tính riêng biệt, vd còn 3 cái thì cảnh báo
    private int minStockThreshold;
    // Tỷ lệ chiết khấu giảm giá (0.0 đến 1.0; 0 là không giảm)
    private double discountRate = 0.0;

    public Product(String id, String name, String brand, double price, int stockQuantity, int minStockThreshold) {
        setId(id);
        setName(name);   // Kích hoạt kiểm tra tên
        setBrand(brand); // Kích hoạt kiểm tra hãng
        setPrice(price); // Kích hoạt kiểm tra giá > 0
        setStockQuantity(stockQuantity); // Kích hoạt kiểm tra tồn kho >= 0
        setMinStockThreshold(minStockThreshold); // Kích hoạt kiểm tra ngưỡng >= 0
    }

    // Hàm nghiệp vụ riêng biệt
    // Kiểm tra hàng sắp hết
    public boolean isLowStock() {
        return this.stockQuantity <= this.minStockThreshold;
    }

    // Hàm nhập thêm hàng vào kho , tăng số lượng tồn
    public void increaseStock(int amount) {
        if (amount > 0) {
            this.stockQuantity += amount;
        }
    }

    // Xuất kho/ bán hàng (giảm amount tồn)
    public boolean decreaseStock(int amount) {
        if (amount > 0 && this.stockQuantity >= amount) {
            this.stockQuantity -= amount;
            return true;
        }
        return false; // nếu ko đủ hàng
    }

    // Hàm chuyển thông tin vào file product.txt
    public abstract String toFileString();

    // Hàm in thông tin sản phẩm đẹp mắt ra màn hình
    public abstract void displayInfo();

    // Hàm tính thời gian bảo hành (tháng)
    public abstract int getWarrantyMonths();

    public String getId() {
        return id;
    }

    public void setId(String id) {
        if (id == null || !id.trim().toUpperCase().matches("^PRD\\d{2,}$")) {
            throw new ValidationException("[LỖI] Mã sản phẩm không hợp lệ! Phải có dạng PRDxx (ví dụ: PRD01, PRD02).");
        }
        this.id = id.trim().toUpperCase();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new ValidationException("[LỖI]: Tên sản phẩm không được để trống!");
        }
        this.name = name.trim();
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        if (brand == null || brand.trim().isEmpty()) {
            throw new ValidationException("[LỖI]: Hãng sản xuất không được để trống!");
        }
        this.brand = brand.trim();
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        if (price <= 0) {
            throw new ValidationException("[LỖI]: Giá bán sản phẩm phải lớn hơn 0 VNĐ!");
        }
        this.price = price;
    }

    public int getStockQuantity() {
        return stockQuantity;
    }

    public void setStockQuantity(int stockQuantity) {
        if (stockQuantity < 0) {
            throw new ValidationException("[LỖI]: Số lượng tồn kho không được âm!");
        }
        this.stockQuantity = stockQuantity;
    }

    public int getMinStockThreshold() {
        return minStockThreshold;
    }

    public void setMinStockThreshold(int minStockThreshold) {
        if (minStockThreshold < 0) {
            throw new ValidationException("[LỖI]: Ngưỡng cảnh báo tồn kho tối thiểu không được âm!");
        }
        this.minStockThreshold = minStockThreshold;
    }

    public double getDiscountRate() {
        return discountRate;
    }

    public void setDiscountRate(double discountRate) {
        if (discountRate < 0.0) {
            this.discountRate = 0.0;
        } else if (discountRate > 1.0) {
            this.discountRate = 1.0;
        } else {
            this.discountRate = discountRate;
        }
    }

    // Giá bán thực tế cho khách hàng (sau khi trừ chiết khấu nếu có)
    public double getSalePrice() {
        if (discountRate > 0) {
            return price * (1.0 - discountRate);
        }
        return price;
    }
}
