package com.smartphonestore.model.product;

public class SaleProduct {
    private final String id, name;
    private final double salePrice;
    private final int stockQuantity;

    public SaleProduct(String id, String name, double salePrice, int stockQuantity) {
        this.id = id; this.name = name;
        if (salePrice < 0 || stockQuantity < 0) throw new IllegalArgumentException("Giá/tồn kho không hợp lệ");
        this.salePrice = salePrice; this.stockQuantity = stockQuantity;
    }
    public String getId() { return id; }
    public String getName() { return name; }
    public double getSalePrice() { return salePrice; }
    public int getStockQuantity() { return stockQuantity; }
}
