package com.smartphonestore.model.customer;

import com.smartphonestore.exception.ValidationException;

public class StandardCustomer extends Customer  {
    public static final double VIP_THRESHOLD = 50_000_000;   // ngưỡng để lên VIP
    private int visitCount;                                  // số lần khách đã mua hàng

    public StandardCustomer(String id, String fullName, String phoneNumber, String email, double totalSpent, int visitCount) {
        super(id, fullName, phoneNumber, email, totalSpent);
        if (visitCount < 0) throw new ValidationException("Số lần mua không được âm");
        this.visitCount = visitCount;
    }

    // ---------- Hàm riêng ----------
    public boolean isEligibleForUpgrade() {
        return getTotalSpent() >= VIP_THRESHOLD;
    }

    public int getVisitCount() { return visitCount; }

    public void increaseVisit() { visitCount++; }

    @Override
    public String getType() {
        return "STANDARD";
    }

    @Override
    public String evaluateRank() {
        if (isEligibleForUpgrade()) return "STANDARD - Đủ điều kiện nâng lên VIP";
        return String.format("STANDARD - Còn thiếu %,.0f đ để lên VIP", VIP_THRESHOLD - getTotalSpent());
    }

    // Thứ tự cột: STANDARD|id|tên|sđt|email|tổng chi tiêu|số lần mua
    @Override
    public String toFileLine() {
        return String.join("|", getId(), getFullName(), getPhoneNumber(), getEmail(),
                plain(getTotalSpent()),getType(), String.valueOf(visitCount));
    }

    @Override
    public void display() {
        System.out.printf("%-8s %-22s %-12s %-24s %,14.0f %-9s Lượt mua: %d%n",
                getId(), getFullName(), getPhoneNumber(), getEmail(), getTotalSpent(), getType(), visitCount);
    }
}
