package com.smartphonestore;

import com.smartphonestore.manager.AccountManager;
import com.smartphonestore.manager.EmployeeManager;
import com.smartphonestore.view.LoginView;

public class Main {
    public static void main(String[] args) {
        // Đọc dữ liệu từ data/accounts.txt và data/employees.txt
        AccountManager accountManager = new AccountManager();
        EmployeeManager employeeManager = new EmployeeManager();

        new LoginView(accountManager, employeeManager).start();
    }
}
