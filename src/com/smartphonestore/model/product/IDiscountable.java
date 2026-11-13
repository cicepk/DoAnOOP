package com.smartphonestore.model.product;
public interface IDiscountable {
 /**
     * Tính giá bán sau khi áp dụng phần trăm chiết khấu
     * @param discountRate Tỷ lệ giảm giá (ví dụ: 0.1 tương đương 10%)
     * @return Giá bán mới sau chiết khấu
     */
    //Hàm nhận tỉ lệ giảm và trả về số tiền sau khi đã giảm
    double calculateDiscountedPrice(double discountRate);
    
}