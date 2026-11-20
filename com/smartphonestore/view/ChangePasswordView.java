package com.smartphonestore.view;

import com.smartphonestore.manager.AccountManager;
import com.smartphonestore.model.auth.Account;
import com.smartphonestore.util.InputHelper;

/**
 * Đổi mật khẩu của chính người đang đăng nhập.
 * Dùng chung cho AdminMenu, WarehouseMenu, SalesMenu (giống kiểu PurchaseHistoryView):
 *     ChangePasswordView.show(accountManager, currentUser);
 */
public final class ChangePasswordView {
    private ChangePasswordView() {}

    public static void show(AccountManager accountManager, Account currentUser) {
        System.out.println("\n--- ĐỔI MẬT KHẨU (" + currentUser.getUsername() + ") ---");
        String oldPass = InputHelper.readString("=> Mật khẩu hiện tại: ");
        String newPass = InputHelper.readString("=> Mật khẩu mới (từ 6 ký tự): ");
        String confirm = InputHelper.readString("=> Nhập lại mật khẩu mới: ");

        if (!newPass.equals(confirm)) {
            System.out.println("[X] Mật khẩu nhập lại không khớp!");
            return;
        }
        String error = accountManager.changeOwnPassword(currentUser.getId(), oldPass, newPass);
        if (error == null) {
            System.out.println("[✓] Đổi mật khẩu thành công!");
        } else {
            System.out.println("[X] " + error);
        }
    }
}
