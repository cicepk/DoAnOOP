package com.smartphonestore.model.customer;

import com.smartphonestore.exception.ValidationException;
import java.math.BigDecimal;

/** Lớp cha trừu tượng của hồ sơ khách hàng (StandardCustomer, VipCustomer). */
public abstract class Customer implements ILoyaltyRankable{
    // SĐT: 10 số, bắt đầu bằng 0. Nếu nhóm chọn quy tắc khác thì chỉ cần đổi ở đây.
    public static final String PHONE_REGEX = "0\\d{9,10}";
    private static final String EMAIL_REGEX = "[^@\\s|]+@[^@\\s|]+\\.[^@\\s|]+";

    private final String id;
    private String fullName;
    private String phoneNumber;
    private String email;
    private double totalSpent;

    public Customer(String id, String fullName, String phoneNumber, String email, double totalSpent) {
        if (id == null || id.trim().isEmpty() || id.contains("|")) {
            throw new ValidationException("Mã khách hàng không hợp lệ");
        }
        if (Double.isNaN(totalSpent) || totalSpent < 0) {
            throw new ValidationException("Tổng chi tiêu không được âm");
        }
        this.id = id.trim();
        setFullName(fullName);       // dữ liệu sai thì ném lỗi ngay khi tạo đối tượng
        setPhoneNumber(phoneNumber);
        setEmail(email);
        this.totalSpent = totalSpent;
    }

    // ---------- Hàm riêng của Customer ----------
    /** Cộng dồn tiền khách vừa mua vào tổng chi tiêu. */
    public void accumulateSpending(double amount) {
        if (amount <= 0) throw new ValidationException("Số tiền phải lớn hơn 0");
        this.totalSpent += amount;
    }

    /** Trừ tiền khi hóa đơn bị hủy / hoàn tiền (không để tổng chi tiêu âm). */
    public void reduceSpending(double amount) {
        if (amount <= 0) throw new ValidationException("Số tiền phải lớn hơn 0");
        this.totalSpent = Math.max(0, this.totalSpent - amount);
    }

    public abstract String getType();       // "STANDARD" hoặc "VIP"
    public abstract String toFileLine();    // dòng ghi vào file
    public abstract void display();         // in 1 dòng ra bảng
    @Override 
    public abstract String evaluateRank();  // đánh giá cấp bậc

    /** Số thực -> chuỗi thường, không dạng 2.0E7, để file dễ đọc. */
    protected static String plain(double value) {
        return BigDecimal.valueOf(value).stripTrailingZeros().toPlainString();
    }

    public static void printHeader() {
        System.out.printf("%-8s %-22s %-12s %-24s %14s %-9s %s%n",
                "Mã", "Họ tên", "SĐT", "Email", "Đã chi tiêu", "Hạng", "Thông tin riêng");
    }

    // ---------- Getter / Setter có kiểm tra ----------
    public String getId() { return id; }

    public String getFullName() { return fullName; }

    public final void setFullName(String n) {
        if (n == null || n.trim().isEmpty()) throw new IllegalArgumentException("Họ tên không được để trống");
        if (n.contains("|")) throw new ValidationException("Họ tên không được chứa ký tự |");
        this.fullName = n.trim();
    }

    public String getPhoneNumber() { return phoneNumber; }

    public final void setPhoneNumber(String p) {
        if (p == null || !p.trim().matches(PHONE_REGEX)) {
            throw new ValidationException("SĐT phải gồm 10 hoặc 11 số và bắt đầu bằng số 0");
        }
        this.phoneNumber = p.trim();
    }

    public String getEmail() { return email; }

    public final void setEmail(String e) {
        if (e == null || !e.trim().matches(EMAIL_REGEX)) {
            throw new ValidationException("Email không hợp lệ (ví dụ: ten@gmail.com)");
        }
        this.email = e.trim();
    }

    public double getTotalSpent() { return totalSpent; }
}
