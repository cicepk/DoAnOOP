package com.smartphonestore.manager;

import com.smartphonestore.model.customer.*;
import com.smartphonestore.util.DateUtil;
import com.smartphonestore.util.FileIO;
import java.time.LocalDate;
import java.util.*;

/**
 * Quản lý khách hàng, đánh giá và phiếu bảo hành (TV4).
 * Manager này KHÔNG gọi manager của người khác. Dữ liệu từ TV2/TV3 (ngày bán, số tháng bảo hành)
 * do menu lấy rồi truyền vào addClaim(...), nên không bị phụ thuộc vòng với InvoiceManager.
 */
public class CustomerManager {
    private static final String CUSTOMER_FILE = "data/customers.txt";
    private static final String REVIEW_FILE = "data/reviews.txt";
    private static final String CLAIM_FILE = "data/warranty_claims.txt";

    private Customer[] customers = new Customer[0];
    private final List<CustomerReview> reviews = new ArrayList<>();
    private final List<WarrantyClaim> warrantyClaims = new ArrayList<>();

    public CustomerManager() {
        loadAll();
    }

    // =====================================================================
    //  ĐỌC / GHI FILE
    // =====================================================================
    private Customer parseCustomer(String line) {
        String[] f = line.split("\\|", -1);
        if (f.length < 7) throw new IllegalArgumentException("Dòng khách hàng phải có 7 cột");
        double spent = Double.parseDouble(f[5]);
        if (f[0].equals("VIP")) {
            return new VipCustomer(f[1], f[2], f[3], f[4], spent, Double.parseDouble(f[6]));
        }
        if (f[0].equals("STANDARD")) {
            return new StandardCustomer(f[1], f[2], f[3], f[4], spent, Integer.parseInt(f[6]));
        }
        throw new IllegalArgumentException("Loại khách không hợp lệ: " + f[0]);
    }

    public void loadAll() {
        List<String> lines = FileIO.readLines(CUSTOMER_FILE);
        Customer[] temp = new Customer[lines.size()];
        int n = 0;
        for (String line : lines) {
            try {
                temp[n] = parseCustomer(line);
                n++;                       // chỉ tăng khi đọc thành công
            } catch (Exception e) {
                System.err.println("Lỗi đọc file khách hàng ở dòng [" + line + "]: " + e.getMessage());
            }
        }
        customers = Arrays.copyOf(temp, n);   // cắt ô thừa, mảng không còn null

        reviews.clear();
        for (String line : FileIO.readLines(REVIEW_FILE)) {
            try {
                reviews.add(CustomerReview.fromFileLine(line));
            } catch (Exception e) {
                System.err.println("Lỗi đọc file đánh giá ở dòng [" + line + "]: " + e.getMessage());
            }
        }

        warrantyClaims.clear();
        for (String line : FileIO.readLines(CLAIM_FILE)) {
            try {
                warrantyClaims.add(WarrantyClaim.fromFileLine(line));
            } catch (Exception e) {
                System.err.println("Lỗi đọc file bảo hành ở dòng [" + line + "]: " + e.getMessage());
            }
        }
    }

    public void saveAll() {
        List<String> a = new ArrayList<>();
        List<String> b = new ArrayList<>();
        List<String> c = new ArrayList<>();
        for (Customer x : customers) a.add(x.toFileLine());
        for (CustomerReview r : reviews) b.add(r.toFileLine());
        for (WarrantyClaim w : warrantyClaims) c.add(w.toFileLine());
        FileIO.writeLines(CUSTOMER_FILE, a);
        FileIO.writeLines(REVIEW_FILE, b);
        FileIO.writeLines(CLAIM_FILE, c);
    }

    /** Phần số của mã (KH012 -> 12). Mã sai định dạng coi như 0 để không làm sập chương trình. */
    private static int suffixOf(String id) {
        try {
            return Integer.parseInt(id.substring(2));
        } catch (RuntimeException e) {
            return 0;
        }
    }

    // =====================================================================
    //  KHÁCH HÀNG
    // =====================================================================
    public String nextCustomerId() {
        int max = 0;
        for (Customer c : customers) max = Math.max(max, suffixOf(c.getId()));
        return String.format("KH%03d", max + 1);
    }

    public Customer findById(String id) {
        if (id == null) return null;
        for (Customer c : customers) {
            if (c.getId().equalsIgnoreCase(id.trim())) return c;
        }
        return null;
    }

    public Customer findByPhone(String phone) {
        if (phone == null) return null;
        for (Customer c : customers) {
            if (c.getPhoneNumber().equals(phone.trim())) return c;
        }
        return null;
    }

    private Customer requireCustomer(String id) {
        Customer c = findById(id);
        if (c == null) throw new IllegalArgumentException("Không tìm thấy khách hàng " + id);
        return c;
    }

    /** Ném lỗi nếu SĐT đã thuộc về khách khác (excludeId = khách đang được sửa, hoặc null khi thêm mới). */
    private void checkPhoneNotUsed(String phone, String excludeId) {
        Customer other = findByPhone(phone);
        if (other != null && !other.getId().equalsIgnoreCase(excludeId)) {
            throw new IllegalArgumentException("SĐT này đã thuộc về khách " + other.getId() + " - " + other.getFullName());
        }
    }

    /** Thêm khách mới (luôn là STANDARD). Dữ liệu sai thì ném lỗi, mảng chưa bị đổi. */
    public Customer addCustomer(String name, String phone, String email) {
        Customer c = new StandardCustomer(nextCustomerId(), name, phone, email, 0, 0);
        checkPhoneNotUsed(c.getPhoneNumber(), null);
        Customer[] temp = new Customer[customers.length + 1];
        for (int i = 0; i < customers.length; i++) temp[i] = customers[i];
        temp[customers.length] = c;
        customers = temp; 
        saveAll();
        return c;
    }

    /** Sửa họ tên, SĐT, email. Sai một trường thì trả cả ba về như cũ. */
    public void updateCustomer(String id, String name, String phone, String email) {
        Customer c = requireCustomer(id);
        String oldName = c.getFullName();
        String oldPhone = c.getPhoneNumber();
        String oldEmail = c.getEmail();
        try {
            c.setFullName(name);
            c.setPhoneNumber(phone);
            c.setEmail(email);
            checkPhoneNotUsed(c.getPhoneNumber(), c.getId());
        } catch (IllegalArgumentException e) {
            c.setFullName(oldName);
            c.setPhoneNumber(oldPhone);
            c.setEmail(oldEmail);
            throw e;
        }
        saveAll();
    }

    /** Khách tự sửa SĐT / email của mình (không đổi họ tên). */
    public void updateContact(String id, String phone, String email) {
        updateCustomer(id, requireCustomer(id).getFullName(), phone, email);
    }

    /**
     * Xóa khách. Chỉ xóa được khi khách chưa mua hàng và chưa có phiếu bảo hành nào.
     * Đánh giá của khách bị xóa theo. Việc xóa tài khoản đăng nhập do menu gọi AccountManager (TV1).
     * @return false nếu không tìm thấy mã
     */
    public boolean deleteCustomer(String id) {
        Customer target = findById(id);
        if (target == null) return false;
        if (target.getTotalSpent() > 0) {
            throw new IllegalArgumentException("Khách đã có giao dịch mua hàng, không thể xóa");
        }
        if (!getClaimsByCustomer(target.getId()).isEmpty()) {
            throw new IllegalArgumentException("Khách còn lịch sử bảo hành, không thể xóa");
        }
        reviews.removeIf(r -> r.getCustomerId().equalsIgnoreCase(target.getId()));
        Customer[] temp = new Customer[customers.length - 1];
        int j = 0;
        for (Customer c : customers) {
            if (c != target) temp[j++] = c;
        }
        customers = temp;
        saveAll();
        return true;
    }

    public Customer[] search(String keyword) {
        List<Customer> result = new ArrayList<>();
        String k = keyword.trim().toLowerCase();
        for (Customer c : customers) {
            if (c.getFullName().toLowerCase().contains(k) || c.getPhoneNumber().contains(k)) {
                result.add(c);
            }
        }
        return result.toArray(new Customer[0]);
    }

    /** Trả về bản sao để bên ngoài không sửa được mảng gốc. */
    public Customer[] getAllCustomers() {
        return Arrays.copyOf(customers, customers.length);
    }

    /** Nâng STANDARD lên VIP: tạo VipCustomer mới, giữ nguyên mã, thông tin và tổng chi tiêu. */
    public Customer upgradeToVip(String id) {
        Customer old = requireCustomer(id);
        if (!(old instanceof StandardCustomer)) {
            throw new IllegalArgumentException("Khách hàng đã là VIP");
        }
        StandardCustomer sc = (StandardCustomer) old;
        if (!sc.isEligibleForUpgrade()) {
            throw new IllegalArgumentException(String.format(
                    "Chưa đủ điều kiện lên VIP: còn thiếu %,.0f đ", StandardCustomer.VIP_THRESHOLD - sc.getTotalSpent()));
        }
        Customer vip = new VipCustomer(sc.getId(), sc.getFullName(), sc.getPhoneNumber(), sc.getEmail(),
                sc.getTotalSpent(), VipCustomer.DEFAULT_DISCOUNT_RATE);
        for (int i = 0; i < customers.length; i++) {
            if (customers[i] == old) customers[i] = vip;
        }
        saveAll();
        return vip;
    }

    // ---------- Hàm TV3 gọi ----------
    /** TV3 gọi sau khi thanh toán xong: cộng chi tiêu, tăng số lần mua (khách STANDARD). */
    public void recordPurchase(String customerId, double amount) {
        Customer c = requireCustomer(customerId);
        c.accumulateSpending(amount);
        if (c instanceof StandardCustomer) ((StandardCustomer) c).increaseVisit();
        saveAll();
    }

    /** TV3 gọi khi hủy hóa đơn đã ghi nhận: trừ lại số tiền. */
    public void refundPurchase(String customerId, double amount) {
        requireCustomer(customerId).reduceSpending(amount);
        saveAll();
    }

    /** TV3 gọi khi tính tiền: số tiền khách được giảm (khách STANDARD trả về 0). */
    public double getVipDiscount(String customerId, double orderTotal) {
        Customer c = requireCustomer(customerId);
        if (c instanceof VipCustomer) return ((VipCustomer) c).calculateVipDiscount(orderTotal);
        return 0;
    }

    // =====================================================================
    //  ĐÁNH GIÁ
    // =====================================================================
    public String nextReviewId() {
        int max = 0;
        for (CustomerReview r : reviews) max = Math.max(max, suffixOf(r.getReviewId()));
        return String.format("DG%03d", max + 1);
    }

    public CustomerReview findReview(String reviewId) {
        for (CustomerReview r : reviews) {
            if (r.getReviewId().equalsIgnoreCase(reviewId.trim())) return r;
        }
        return null;
    }

    public CustomerReview addReview(String customerId, String productId, int ratingStars, String comment) {
        Customer c = requireCustomer(customerId);
        CustomerReview r = new CustomerReview(nextReviewId(), c.getId(), productId, DateUtil.today(), ratingStars, comment);
        reviews.add(r);
        saveAll();
        return r;
    }

    private CustomerReview requireOwnReview(String reviewId, String customerId) {
        CustomerReview r = findReview(reviewId);
        if (r == null) throw new IllegalArgumentException("Không tìm thấy đánh giá " + reviewId);
        if (!r.getCustomerId().equalsIgnoreCase(customerId)) {
            throw new IllegalArgumentException("Đánh giá này không phải của bạn");
        }
        return r;
    }

    /** Khách sửa đánh giá của CHÍNH MÌNH. */
    public void updateReview(String reviewId, String customerId, int ratingStars, String comment) {
        CustomerReview r = requireOwnReview(reviewId, customerId);
        r.setRatingStars(ratingStars);
        r.setComment(comment);
        saveAll();
    }

    /** Khách xóa đánh giá của CHÍNH MÌNH. */
    public void deleteReview(String reviewId, String customerId) {
        CustomerReview r = requireOwnReview(reviewId, customerId);
        reviews.remove(r);
        saveAll();
    }

    public List<CustomerReview> getReviewsByCustomer(String customerId) {
        List<CustomerReview> result = new ArrayList<>();
        for (CustomerReview r : reviews) {
            if (r.getCustomerId().equalsIgnoreCase(customerId)) result.add(r);
        }
        return result;
    }

    public List<CustomerReview> getReviewsByProduct(String productId) {
        List<CustomerReview> result = new ArrayList<>();
        for (CustomerReview r : reviews) {
            if (r.getProductId().equalsIgnoreCase(productId)) result.add(r);
        }
        return result;
    }

    /** Điểm sao trung bình của sản phẩm (0 nếu chưa có đánh giá). */
    public double getAverageRating(String productId) {
        List<CustomerReview> list = getReviewsByProduct(productId);
        if (list.isEmpty()) return 0;
        int sum = 0;
        for (CustomerReview r : list) sum += r.getRatingStars();
        return (double) sum / list.size();
    }

    // =====================================================================
    //  BẢO HÀNH
    // =====================================================================
    public String nextClaimId() {
        int max = 0;
        for (WarrantyClaim w : warrantyClaims) max = Math.max(max, suffixOf(w.getClaimId()));
        return String.format("BH%03d", max + 1);
    }

    private static String normalizeSerial(String serial) {
        if (serial == null || serial.trim().isEmpty()) {
            throw new IllegalArgumentException("Số seri không được để trống");
        }
        String s = serial.trim().toUpperCase();
        if (s.contains("|") || s.matches(".*\\s.*")) {
            throw new IllegalArgumentException("Số seri không được chứa khoảng trắng hoặc ký tự |");
        }
        return s;
    }

    public boolean hasOpenClaim(String serial) {
        String sn = normalizeSerial(serial);
        for (WarrantyClaim w : warrantyClaims) {
            if (w.getSerialNumber().equals(sn) && !w.isClosed()) return true;
        }
        return false;
    }

    /**
     * Tiếp nhận bảo hành. Menu lấy trước từ TV3 (người mua, mã sản phẩm, ngày bán) và TV2 (số tháng bảo hành).
     * Từ chối khi: không có bảo hành, seri đang có phiếu chưa đóng, hoặc đã hết hạn.
     */
    public WarrantyClaim addClaim(String serial, String productId, String customerId, LocalDate saleDate, int warrantyMonths, String issue) {
        Customer c = requireCustomer(customerId);
        String sn = normalizeSerial(serial);
        if (warrantyMonths <= 0) {
            throw new IllegalArgumentException("Sản phẩm này không có chế độ bảo hành");
        }
        if (hasOpenClaim(sn)) {
            throw new IllegalArgumentException("Seri " + sn + " đang có phiếu bảo hành chưa hoàn tất");
        }
        LocalDate today = LocalDate.now();
        WarrantyClaim w = new WarrantyClaim(nextClaimId(), c.getId(), productId, sn, issue, saleDate, warrantyMonths, today);
        if (!w.isUnderWarrantyOn(today)) {
            throw new IllegalArgumentException("Seri " + sn + " đã hết hạn bảo hành từ ngày "+ DateUtil.format(w.getWarrantyExpiryDate()));
        }
        warrantyClaims.add(w);
        saveAll();
        return w;
    }

    public WarrantyClaim findClaimById(String claimId) {
        for (WarrantyClaim w : warrantyClaims) {
            if (w.getClaimId().equalsIgnoreCase(claimId.trim())) return w;
        }
        return null;
    }

    public List<WarrantyClaim> getClaimsByCustomer(String customerId) {
        List<WarrantyClaim> result = new ArrayList<>();
        for (WarrantyClaim w : warrantyClaims) {
            if (w.getCustomerId().equalsIgnoreCase(customerId)) result.add(w);
        }
        return result;
    }

    public List<WarrantyClaim> getClaimsBySerial(String serial) {
        String sn = normalizeSerial(serial);
        List<WarrantyClaim> result = new ArrayList<>();
        for (WarrantyClaim w : warrantyClaims) {
            if (w.getSerialNumber().equals(sn)) result.add(w);
        }
        return result;
    }

    public int countOpenClaims(String customerId) {
        int n = 0;
        for (WarrantyClaim w : getClaimsByCustomer(customerId)) {
            if (!w.isClosed()) n++;
        }
        return n;
    }

    /** @return false nếu không tìm thấy phiếu; ném lỗi nếu phiếu đã đóng hoặc trạng thái sai */
    public boolean updateClaimStatus(String claimId, String newStatus) {
        WarrantyClaim w = findClaimById(claimId);
        if (w == null) return false;
        w.updateStatus(newStatus);
        saveAll();
        return true;
    }

    /** Chỉ xóa được phiếu đã đóng (Hoàn tất / Từ chối). Menu kiểm tra quyền Admin. */
    public boolean deleteClaim(String claimId) {
        WarrantyClaim w = findClaimById(claimId);
        if (w == null) return false;
        if (!w.isClosed()) throw new IllegalArgumentException("Chỉ xóa được phiếu đã hoàn tất hoặc bị từ chối");
        warrantyClaims.remove(w);
        saveAll();
        return true;
    }
}
