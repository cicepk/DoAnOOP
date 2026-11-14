package com.smartphonestore;

import com.smartphonestore.manager.AccountManager;
import com.smartphonestore.manager.CustomerManager;
import com.smartphonestore.manager.EmployeeManager;
import com.smartphonestore.manager.ProductManager;
import com.smartphonestore.manager.SalesManager;
import com.smartphonestore.view.LoginView;

public class Main {
    public static void main(String[] args) {
        // Mỗi manager chỉ tạo 1 lần ở đây rồi truyền xuống các menu,
        // để mọi menu cùng đọc/ghi một bản dữ liệu duy nhất
        AccountManager accountManager = new AccountManager();
        EmployeeManager employeeManager = new EmployeeManager();
        ProductManager productManager = new ProductManager();
        SalesManager salesManager = new SalesManager(productManager); // TV3 đổi trạng thái serial qua TV2
        CustomerManager customerManager = new CustomerManager();

        new LoginView(accountManager, employeeManager, productManager,
                      salesManager, customerManager).start();
    }
}
