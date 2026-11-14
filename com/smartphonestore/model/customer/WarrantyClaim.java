package com.smartphonestore.model.customer;

import com.smartphonestore.util.DateUtil;
import java.time.LocalDate;
import java.util.List;

/**
 * Phiếu tiếp nhận bảo hành. Gắn với một SỐ SERI cụ thể.
 * Hạn bảo hành = ngày bán (lấy từ hóa đơn của TV3) + số tháng bảo hành (lấy từ Phone.getWarrantyMonths() của TV2).
 * Ngày dự kiến hoàn thành = ngày tiếp nhận + 5 ngày.
 */
public class WarrantyClaim {
        public static final String TIEP_NHAN = "TIEP_NHAN";
        public static final String DANG_SUA = "DANG_SUA";
        public static final String HOAN_TAT = "HOAN_TAT";
        public static final String TU_CHOI = "TU_CHOI";
        public static final int PROCESSING_DAYS = 5;
        private static final List<String> VALID_STATUS = List.of(TIEP_NHAN, DANG_SUA, HOAN_TAT, TU_CHOI);

    private final String claimId;
    private final String customerId;
    private final String productId;
    private final String serialNumber;
    private String issueDescription;
    private final LocalDate createdDate;    // ngày tiếp nhận, lấy lúc tiếp nhận
     private String claimStatus;             // TIEP_NHAN, DANG_SUA, HOAN_TAT, TU_CHOI
    private final LocalDate saleDate;       // ngày bán, lấy từ hóa đơn lúc tiếp nhận
    private final int warrantyMonths;       // số tháng bảo hành, lấy lúc tiếp nhận
              
    /** Constructor đầy đủ, dùng khi đọc từ file. */
    public WarrantyClaim(String claimId, String customerId, String productId, String serialNumber,
                         String issueDescription,LocalDate createdDate,String claimStatus, LocalDate saleDate, int warrantyMonths
                          ) {
        if (isBlank(claimId) || isBlank(customerId) || isBlank(productId)) {
            throw new IllegalArgumentException("Thiếu mã phiếu, mã khách hoặc mã sản phẩm");
        }
        if (isBlank(serialNumber) || serialNumber.contains("|")) {
            throw new IllegalArgumentException("Số seri không hợp lệ");
        }
        if (saleDate == null || createdDate == null) {
            throw new IllegalArgumentException("Thiếu ngày bán hoặc ngày tiếp nhận");
        }
        if (warrantyMonths < 0) throw new IllegalArgumentException("Số tháng bảo hành không được âm");
        if (createdDate.isBefore(saleDate)) {
            throw new IllegalArgumentException("Ngày tiếp nhận không thể trước ngày bán");
        }
        this.claimId = claimId;
        this.customerId = customerId;
        this.productId = productId;
        this.serialNumber = serialNumber;
        setIssueDescription(issueDescription);
        this.createdDate = createdDate;
        this.claimStatus = checkStatus(claimStatus);
        this.saleDate = saleDate;
        this.warrantyMonths = warrantyMonths;
        
    }

    /** Constructor tạo phiếu mới: trạng thái ban đầu là TIEP_NHAN. */
    public WarrantyClaim(String claimId, String customerId, String productId, String serialNumber,
                         String issueDescription, LocalDate createdDate, LocalDate saleDate, int warrantyMonths ) {
        this(claimId, customerId, productId, serialNumber, issueDescription,
                createdDate, TIEP_NHAN, saleDate, warrantyMonths);
    }

    private static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    private static String checkStatus(String status) {
        String s = status == null ? null : status.trim().toUpperCase();
        if (s == null || !VALID_STATUS.contains(s)) {
            throw new IllegalArgumentException("Trạng thái bảo hành không hợp lệ: " + status
                    + " (chỉ nhận: TIEP_NHAN, DANG_SUA, HOAN_TAT, TU_CHOI)");
        }
        return s;
    }

    // ---------- Hàm riêng: bảo hành & tiến độ ----------
    public LocalDate getWarrantyExpiryDate() {
        return saleDate.plusMonths(warrantyMonths);
    }

    /** Còn bảo hành tại ngày đã cho không (ngày cuối hạn vẫn tính là còn). */
    public boolean isUnderWarrantyOn(LocalDate date) {
        return !date.isAfter(getWarrantyExpiryDate());
    }

    public LocalDate getExpectedCompletionDate() {
        return createdDate.plusDays(PROCESSING_DAYS);
    }

    /** Câu hiển thị cho khách ở mục "Phiếu bảo hành của tôi". */
    public String getExpectedCompletionText() {
        if (HOAN_TAT.equals(claimStatus)) return "Đã hoàn tất";
        if (TU_CHOI.equals(claimStatus)) return "Yêu cầu bị từ chối";
        String text = "Dự kiến hoàn thành: " + DateUtil.format(getExpectedCompletionDate());
        if (LocalDate.now().isAfter(getExpectedCompletionDate())) {
            text += " (đã quá hạn dự kiến, vui lòng liên hệ cửa hàng)";
        }
        return text;
    }

    public boolean isClosed() {
        return claimStatus.equals(HOAN_TAT) || claimStatus.equals(TU_CHOI);
    }

    public void updateStatus(String newStatus) {
        String s = checkStatus(newStatus);
        if (isClosed()) {
            throw new IllegalArgumentException("Phiếu đã đóng (" + getStatusDisplay() + "), không thể đổi trạng thái");
        }
        this.claimStatus = s;
    }

    public String getStatusDisplay() {
        switch (claimStatus) {
            case TIEP_NHAN: return "Tiếp nhận";
            case DANG_SUA:  return "Đang sửa";
            case HOAN_TAT:  return "Hoàn tất";
            default:        return "Từ chối";
        }
    }

    public void setIssueDescription(String desc) {
        if (isBlank(desc)) throw new IllegalArgumentException("Mô tả lỗi không được để trống");
        this.issueDescription = desc.trim().replace("|", " ");
    }

    // ---------- File: 9 cột ----------
    // claimId|customerId|productId|serial|lỗi|ngày bán|số tháng BH|ngày tiếp nhận|trạng thái
    public String toFileLine() {
        return String.join("|", claimId, customerId, productId, serialNumber, issueDescription,DateUtil.format(createdDate),claimStatus,DateUtil.format(saleDate), String.valueOf(warrantyMonths));
    }

    public static WarrantyClaim fromFileLine(String line) {
        String[] f = line.split("\\|", -1);
        if (f.length < 9) throw new IllegalArgumentException("Dòng bảo hành phải có 9 cột (có thể là dữ liệu cũ)");
        return new WarrantyClaim(f[0], f[1], f[2], f[3], f[4], DateUtil.parse(f[5]),f[6],
                DateUtil.parse(f[7]), Integer.parseInt(f[8]));
    }

    // ---------- Hiển thị ----------
    /** Bảng cho nhân viên. */
    public static void printHeader() {
        System.out.printf("%-8s %-8s %-8s %-19s %-19s %-19s %-10s %s%n",
                "Mã", "Khách", "SP", "Seri", "Ngày nhận", "Hạn BH", "Trạng thái", "Lỗi");
    }

    public void display() {
        System.out.printf("%-8s %-8s %-8s %-19s %-19s %-19s %-10s %s%n",
                claimId, customerId, productId, serialNumber, DateUtil.format(createdDate),
                DateUtil.format(getWarrantyExpiryDate()), getStatusDisplay(), issueDescription);
    }

    /** Bảng cho khách (kèm ngày dự kiến hoàn thành ở dòng dưới). */
    public static void printCustomerHeader() {
        System.out.printf("%-8s %-19s %-8s %-19s %-10s %s%n",
                "Mã", "Seri", "SP", "Ngày gửi", "Trạng thái", "Lỗi");
    }

    public void displayForCustomer() {
        System.out.printf("%-8s %-19s %-8s %-19s %-10s %s%n",
                claimId, serialNumber, productId, DateUtil.format(createdDate), getStatusDisplay(), issueDescription);
        System.out.println("         -> " + getExpectedCompletionText());
    }

    public String getClaimId() { return claimId; }
    public String getCustomerId() { return customerId; }
    public String getProductId() { return productId; }
    public String getSerialNumber() { return serialNumber; }
    public String getIssueDescription() { return issueDescription; }
    public LocalDate getSaleDate() { return saleDate; }
    public int getWarrantyMonths() { return warrantyMonths; }
    public LocalDate getCreatedDate() { return createdDate; }
    public String getClaimStatus() { return claimStatus; }
}
