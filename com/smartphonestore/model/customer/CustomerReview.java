package com.smartphonestore.model.customer;

public class CustomerReview {
    private final String reviewId;
    private final String customerId;
    private final String productId;
    private final String reviewDate;   // dd/MM/yyyy
    private int ratingStars;           // 1 đến 5 sao
    private String comment;

    public CustomerReview(String reviewId, String customerId, String productId, int ratingStars, String comment, String reviewDate) {
        this.reviewId = reviewId;
        this.customerId = customerId;
        this.productId = productId;
        setRatingStars(ratingStars);
        setComment(comment);
        this.reviewDate = reviewDate;
    }

    // ---------- Hàm riêng ----------
    public boolean isPositiveFeedback() {
        return ratingStars >= 4;
    }

    public void setRatingStars(int s) {
        if (s < 1 || s > 5) throw new IllegalArgumentException("Số sao phải từ 1 đến 5");
        this.ratingStars = s;
    }

    public void setComment(String comment) {
        this.comment = comment == null ? "" : comment.trim().replace("|", " ");
    }

    public String toFileLine() {
        return String.join("|", reviewId, customerId, productId, reviewDate,
                String.valueOf(ratingStars), comment);
    }

    public static CustomerReview fromFileLine(String line) {
        String[] f = line.split("\\|", -1);
        if (f.length < 6) throw new IllegalArgumentException("Dòng đánh giá phải có 6 cột");
        return new CustomerReview(f[0], f[1], f[2],  Integer.parseInt(f[4]), f[5], f[6]);
    }

    public static void printHeader() {
        System.out.printf("%-8s %-8s %-8s %-19s %-6s %-14s %s%n",
                "Mã", "Khách", "SP", "Ngày", "Sao", "Loại", "Nhận xét");
    }

    public void display() {
        System.out.printf("%-8s %-8s %-8s %-19s %-6s %-14s %s%n", reviewId, customerId, productId,
                reviewDate, ratingStars + " sao", isPositiveFeedback() ? "Tích cực" : "Cần cải thiện", comment);
    }

    public String getReviewId() { return reviewId; }
    public String getCustomerId() { return customerId; }
    public String getProductId() { return productId; }
    public String getReviewDate() { return reviewDate; }
    public int getRatingStars() { return ratingStars; }
    public String getComment() { return comment; }
}
