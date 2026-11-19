package com.smartphonestore.model.product;

import com.smartphonestore.exception.ValidationException;

/**
 * Đại diện cho một Phiếu nhập kho hàng hóa (Goods Receipt Note).
 * Quản lý thông tin từng lô hàng nhập: Mã phiếu (PNxx), Mã nhân viên thực hiện,
 * Ngày giờ nhập, Nhà cung cấp, Tổng số lượng máy và Tổng giá trị tiền vốn.
 */
public class ImportReceipt {
    private String receiptId;      // Khóa chính: PN01, PN02...
    private String staffId;        // Mã nhân viên phụ trách nhập kho (EMPxx)
    private String importDate;     // Thời gian lập phiếu (dd/MM/yyyy HH:mm:ss)
    private String supplier;       // Nhà cung cấp thiết bị
    private int totalQuantity;     // Tổng số lượng máy trong phiếu
    private double totalAmount;    // Tổng giá trị nhập vốn của lô hàng (VND)

    public ImportReceipt(String receiptId, String staffId, String importDate, String supplier, int totalQuantity, double totalAmount) {
        setReceiptId(receiptId);
        setStaffId(staffId);
        setImportDate(importDate);
        setSupplier(supplier);
        setTotalQuantity(totalQuantity);
        setTotalAmount(totalAmount);
    }

    public String toFileString() {
        return String.format("%s|%s|%s|%s|%d|%.0f",
                receiptId, staffId, importDate, supplier, totalQuantity, totalAmount);
    }

    public void displayRow() {
        System.out.printf(" %-8s │ %-8s │ %-19s │ %-20s │ %8d chiếc │ %,15.0f đ%n",
                receiptId,
                staffId != null ? staffId : "EMP02",
                importDate,
                supplier != null ? supplier : "N/A",
                totalQuantity,
                totalAmount);
    }

    public String getReceiptId() {
        return receiptId;
    }

    public void setReceiptId(String receiptId) {
        if (receiptId == null || !receiptId.trim().toUpperCase().matches("^PN\\d{2,}$")) {
            throw new ValidationException("[LỖI] Mã phiếu nhập không hợp lệ! Phải có định dạng PNxx (ví dụ: PN01, PN02).");
        }
        this.receiptId = receiptId.trim().toUpperCase();
    }

    public String getStaffId() {
        return staffId;
    }

    public void setStaffId(String staffId) {
        if (staffId == null || staffId.trim().isEmpty()) {
            this.staffId = "EMP02"; // Mặc định nhân viên kho
        } else {
            this.staffId = staffId.trim().toUpperCase();
        }
    }

    public String getImportDate() {
        return importDate;
    }

    public void setImportDate(String importDate) {
        if (importDate == null || importDate.trim().isEmpty()) {
            throw new ValidationException("[LỖI] Thời gian nhập kho không được để trống!");
        }
        this.importDate = importDate.trim();
    }

    public String getSupplier() {
        return supplier;
    }

    public void setSupplier(String supplier) {
        if (supplier == null || supplier.trim().isEmpty()) {
            throw new ValidationException("[LỖI] Tên nhà cung cấp không được để trống!");
        }
        this.supplier = supplier.trim();
    }

    public int getTotalQuantity() {
        return totalQuantity;
    }

    public void setTotalQuantity(int totalQuantity) {
        if (totalQuantity <= 0) {
            throw new ValidationException("[LỖI] Tổng số lượng nhập phải lớn hơn 0!");
        }
        this.totalQuantity = totalQuantity;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(double totalAmount) {
        if (totalAmount <= 0) {
            throw new ValidationException("[LỖI] Tổng tiền nhập kho phải lớn hơn 0 VNĐ!");
        }
        this.totalAmount = totalAmount;
    }
}
