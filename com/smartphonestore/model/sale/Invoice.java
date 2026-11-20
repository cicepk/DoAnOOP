package com.smartphonestore.model.sale;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Invoice {
    private final String id, staffId, customerId;
    private final LocalDateTime createdAt;
    private final List<InvoiceDetail> details = new ArrayList<>();
    private InvoiceStatus status = InvoiceStatus.UNPAID;
    private PaymentMethod paymentMethod;
    private double discountAmount;

    public Invoice(String id, String staffId, String customerId, LocalDateTime createdAt) {
        this.id = id; this.staffId = staffId; this.customerId = customerId; this.createdAt = createdAt;
    }
    public void addDetail(InvoiceDetail detail) { details.add(detail); }
    public String getId() { return id; }
    public String getStaffId() { return staffId; }
    public String getCustomerId() { return customerId; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public List<InvoiceDetail> getDetails() { return Collections.unmodifiableList(details); }
    public InvoiceStatus getStatus() { return status; }
    public void setStatus(InvoiceStatus status) { this.status = status; }
    public PaymentMethod getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(PaymentMethod method) { this.paymentMethod = method; }
    public double getDiscountAmount() { return discountAmount; }
    public void setDiscountAmount(double amount) {
        if (amount < 0) throw new IllegalArgumentException("Giảm trừ không được âm");
        discountAmount = amount;
    }
    public double getSubtotal() { return details.stream().mapToDouble(InvoiceDetail::getLineTotal).sum(); }
    public double getTotal() { return Math.max(0, getSubtotal() - discountAmount); }
}
