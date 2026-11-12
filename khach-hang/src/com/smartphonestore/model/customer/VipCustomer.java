package com.smartphonestore.model.customer;

public class VipCustomer extends Customer implements ILoyaltyRankable {
    public static final double DEFAULT_DISCOUNT_RATE = 0.05;   // 5%
    private double vipDiscountRate;

    public VipCustomer(String id, String fullName, String phoneNumber, String email, double totalSpent, double vipDiscountRate) {
        super(id, fullName, phoneNumber, email, totalSpent);
        setVipDiscountRate(vipDiscountRate);
    }

    // ---------- Hàm riêng ----------
    /** Số tiền được giảm trên một đơn hàng. */
    public double calculateVipDiscount(double orderTotal) {
        if (orderTotal < 0) throw new IllegalArgumentException("Giá trị đơn hàng không được âm");
        return orderTotal * vipDiscountRate;
    }

    public double getVipDiscountRate() { return vipDiscountRate; }

    public void setVipDiscountRate(double rate) {
        if (rate < 0 || rate > 1) throw new IllegalArgumentException("Tỷ lệ giảm giá phải từ 0 đến 1");
        this.vipDiscountRate = rate;
    }

    @Override
    public String getType() {
        return "VIP";
    }

    @Override
    public String evaluateRank() {
        return String.format("VIP - Giảm %.0f%% mọi đơn hàng", vipDiscountRate * 100);
    }

    // Thứ tự cột: VIP|id|tên|sđt|email|tổng chi tiêu|tỷ lệ giảm
    @Override
    public String toFileLine() {
        return String.join("|", getType(), getId(), getFullName(), getPhoneNumber(), getEmail(),
                plain(getTotalSpent()), String.valueOf(vipDiscountRate));
    }

    @Override
    public void display() {
        System.out.printf("%-8s %-22s %-12s %-24s %,14.0f %-9s Giảm giá: %.0f%%%n",
                getId(), getFullName(), getPhoneNumber(), getEmail(),
                getTotalSpent(), getType(), vipDiscountRate * 100);
    }
}
