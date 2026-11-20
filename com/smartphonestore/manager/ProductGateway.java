package com.smartphonestore.manager;

import com.smartphonestore.model.product.SaleProduct;
import java.util.List;

public interface ProductGateway {
    SaleProduct findById(String productId);
    List<SaleProduct> findByNameOrBrand(String keyword);
    boolean decreaseStock(String productId, int quantity);
    boolean isSerialAvailable(String serial);
    boolean markSerialSold(String serial);
    boolean receiveTradeInSerial(String serial, String productId, double estimatedValue);
}
