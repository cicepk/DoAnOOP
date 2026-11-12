package com.smartphonestore.manager;

/**
 * Cau noi toi kho hang cua Thanh vien 2.
 * Adapter khi ghep nhom se goi ProductManager.decreaseStock(productId, quantity).
 */
public interface InventoryGateway {
    ProductInfo findById(String productId);
    boolean decreaseStock(String productId, int quantity);
    default boolean increaseStock(String productId, int quantity) { return false; }

    class ProductInfo {
        public final String id, name;
        public final double price;
        public ProductInfo(String id, String name, double price) {
            this.id = id; this.name = name; this.price = price;
        }
    }
}
