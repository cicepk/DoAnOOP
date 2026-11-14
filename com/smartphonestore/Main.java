package com.smartphonestore;

import com.smartphonestore.manager.AccountManager;
import com.smartphonestore.manager.EmployeeManager;
import com.smartphonestore.manager.ProductManager;
import com.smartphonestore.view.LoginView;

public class Main {
    public static void main(String[] args) {
        // Mỗi manager chỉ tạo 1 lần ở đây rồi truyền xuống các menu
        AccountManager accountManager = new AccountManager();
        EmployeeManager employeeManager = new EmployeeManager();
        ProductManager productManager = new ProductManager();

        new LoginView(accountManager, employeeManager, productManager).start();
    }
}
