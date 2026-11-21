package com.smartphonestore.view;

import com.smartphonestore.manager.ProductManager;
import com.smartphonestore.manager.SalesManager;
import com.smartphonestore.model.product.Product;
import com.smartphonestore.model.product.ProductDetail;
import com.smartphonestore.model.sale.Invoice;
import com.smartphonestore.model.sale.InvoiceDetail;
import java.util.List;

/**
 * In lịch sử mua hàng của 1 khách.
 * Dùng chung cho CustomerManagementMenu (admin/sales chọn khách)
 */
public final class PurchaseHistoryView {
    private PurchaseHistoryView() {}

    public static void print(String customerId, SalesManager salesManager, ProductManager productManager) {
        List<Invoice> invoices = salesManager.getInvoicesByCustomer(customerId);
        if (invoices.isEmpty()) {
            System.out.println("Chưa có hóa đơn nào.");
            return;
        }
        int paidCount = 0;
        double paidTotal = 0;
        for (Invoice inv : invoices) {
            System.out.printf("%s | %s | %s | %,.0f đ (gồm VAT)%n",
                    inv.getId(), inv.getCreationDate(), inv.getStatus(), inv.calculateFinalTotal());
            for (InvoiceDetail d : salesManager.getDetailsByInvoice(inv.getId())) {
                ProductDetail pd = productManager.findDetailBySerial(d.getSerialNumber());
                Product p = (pd == null) ? null : productManager.findByID(pd.getProductId());
                System.out.printf("      - %s | seri %s | %,.0f đ%n",
                        p == null ? "(không rõ sản phẩm)" : p.getName(),
                        d.getSerialNumber(), d.calculateLineTotal());
            }
            if ("PAID".equals(inv.getStatus())) {
                paidCount++;
                paidTotal += inv.calculateFinalTotal();
            }
        }
        System.out.printf("Đã thanh toán: %d hóa đơn, tổng %,.0f đ%n", paidCount, paidTotal);
    }
}
