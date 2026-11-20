package com.smartphonestore.manager;

public interface CustomerGateway {
    String findCustomerIdByPhone(String phone);
    String findCustomerIdById(String customerId);
    String addCustomer(String name, String phone);
    double getVipDiscountRate(String customerId);
}
