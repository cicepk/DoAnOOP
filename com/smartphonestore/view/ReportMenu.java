package com.smartphonestore.view;

import com.smartphonestore.manager.ProductManager;
import com.smartphonestore.manager.SalesManager;
import com.smartphonestore.model.product.Product;
import com.smartphonestore.util.DateUtil;
import com.smartphonestore.util.InputHelper;
import java.time.LocalDate;
import java.time.Year;
import java.util.List;
import java.util.Map;

/**
 * Admin [1] Báo cáo thống kê.
 * Không tự tính gì: chỉ gọi hàm doanh thu của SalesManager và hàm tồn kho của ProductManager.
 */
public class ReportMenu {
    private final SalesManager salesManager;
    private final ProductManager productManager;

    public ReportMenu(SalesManager salesManager, ProductManager productManager) {
        this.salesManager = salesManager;
        this.productManager = productManager;
    }

    public void show() {
        while (true) {
            System.out.println("\n--- BÁO CÁO THỐNG KÊ ---");
            System.out.println(" [1] Doanh thu (theo ngày / theo tháng)");
            System.out.println(" [2] Sản phẩm bán chạy");
            System.out.println(" [3] Hàng sắp hết");
            System.out.println(" [0] Quay lại");
            int choice = InputHelper.readInt("=> Chọn (0-3): ", 0, 3);
            switch (choice) {
                case 1: revenueReport(); break;
                case 2: topSelling(); break;
                case 3: lowStock(); break;
                case 0: return;
            }
        }
    }

    private void revenueReport() {
        System.out.println(" [1] Theo ngày   [2] Theo tháng");
        int type = InputHelper.readInt("=> Chọn (1-2): ", 1, 2);
        if (type == 1) {
            String text = InputHelper.readOptionalString("=> Ngày (dd/MM/yyyy, bỏ trống = hôm nay): ");
            LocalDate date;
            try {
                date = (text == null || text.trim().isEmpty()) ? LocalDate.now() : DateUtil.parse(text);
            } catch (RuntimeException e) {
                System.out.println("[X] Ngày không hợp lệ.");
                return;
            }
            System.out.printf("Doanh thu ngày %s: %,.0f VNĐ%n",
                DateUtil.format(date), salesManager.revenueByDate(date));
        } else {
            int month = InputHelper.readInt("=> Tháng (1-12): ", 1, 12);
            int year = InputHelper.readInt("=> Năm: ", 2000, Year.now().getValue());
            System.out.printf("Doanh thu tháng %d/%d: %,.0f VNĐ%n",
                month, year, salesManager.revenueByMonth(month, year));
        }
    }

    private void topSelling() {
        int n = InputHelper.readInt("=> Xem top bao nhiêu sản phẩm (1-20): ", 1, 20);
        Map<String, Integer> top = salesManager.topSellingProducts(n); // mã SP -> số máy đã bán
        if (top.isEmpty()) {
            System.out.println("(Chưa có hóa đơn đã thanh toán)");
            return;
        }
        System.out.printf("%-4s | %-8s | %-30s | %s%n", "Hạng", "Mã SP", "Tên sản phẩm", "Đã bán");
        int rank = 1;
        for (Map.Entry<String, Integer> entry : top.entrySet()) {
            Product p = productManager.findByID(entry.getKey());
            System.out.printf("%-4d | %-8s | %-30s | %d%n",
                rank++, entry.getKey(), p == null ? "(đã xóa)" : p.getName(), entry.getValue());
        }
    }

    private void lowStock() {
        List<Product> list = productManager.getLowStockProducts();
        if (list.isEmpty()) {
            System.out.println("[✓] Không có sản phẩm nào sắp hết.");
            return;
        }
        System.out.printf("%-8s | %-30s | %s%n", "Mã SP", "Tên sản phẩm", "Còn lại");
        for (Product p : list) {
            System.out.printf("%-8s | %-30s | %d%n", p.getId(), p.getName(), p.getStockQuantity());
        }
    }
}
