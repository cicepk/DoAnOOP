package com.smartphonestore.manager;

import com.smartphonestore.model.sale.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class SalesManager {
    private final ProductGateway products;
    private final CustomerGateway customers;
    private final Map<String, Invoice> invoices = new LinkedHashMap<>();
    private final List<TradeInRecord> tradeIns = new ArrayList<>();
    private int nextInvoiceNumber = 1;

    public SalesManager(ProductGateway products, CustomerGateway customers) {
        this.products = Objects.requireNonNull(products);
        this.customers = Objects.requireNonNull(customers);
    }

    public synchronized Invoice createInvoice(String staffId, String customerInput,
                                               Map<String, Integer> items) {
        if (staffId == null || staffId.isBlank()) throw new IllegalArgumentException("Thiếu mã nhân viên");
        String customerId = resolveCustomer(customerInput);
        if (items == null || items.isEmpty()) throw new IllegalArgumentException("Hóa đơn chưa có sản phẩm");
        Invoice invoice = new Invoice(nextInvoiceId(), staffId, customerId, LocalDateTime.now());
        for (var entry : items.entrySet()) {
            SaleProduct p = products.findById(entry.getKey());
            int qty = entry.getValue() == null ? 0 : entry.getValue();
            if (p == null) throw new IllegalArgumentException("Không tìm thấy sản phẩm: " + entry.getKey());
            if (qty <= 0 || qty > p.getStockQuantity()) throw new IllegalArgumentException("Số lượng tồn không đủ: " + p.getName());
            invoice.addDetail(new InvoiceDetail(p.getId(), p.getName(), null, qty, p.getSalePrice()));
        }
        invoices.put(invoice.getId(), invoice);
        return invoice;
    }

    private String resolveCustomer(String input) {
        if (input == null || input.isBlank() || input.equalsIgnoreCase("KHACHLE")) return "KHACHLE";
        String id = customers.findCustomerIdById(input.trim());
        if (id == null) id = customers.findCustomerIdByPhone(input.trim());
        return id == null ? "KHACHLE" : id;
    }

    public synchronized boolean payInvoice(String invoiceId, PaymentMethod method) {
        Invoice invoice = requireInvoice(invoiceId);
        if (invoice.getStatus() != InvoiceStatus.UNPAID) return false;
        for (InvoiceDetail d : invoice.getDetails()) {
            if (!products.decreaseStock(d.getProductId(), d.getQuantity())) return false;
        }
        invoice.setPaymentMethod(Objects.requireNonNull(method));
        invoice.setStatus(InvoiceStatus.PAID);
        return true;
    }

    public synchronized boolean cancelUnpaidInvoice(String invoiceId) {
        Invoice invoice = requireInvoice(invoiceId);
        if (invoice.getStatus() != InvoiceStatus.UNPAID) return false;
        invoice.setStatus(InvoiceStatus.CANCELLED);
        return true;
    }

    public Invoice findInvoice(String id) { return invoices.get(id); }
    public List<Invoice> getAllInvoices() { return List.copyOf(invoices.values()); }
    public List<Invoice> getInvoicesByCustomer(String customerId) {
        return invoices.values().stream().filter(i -> i.getCustomerId().equals(customerId)).toList();
    }
    public boolean hasInvoicesOfStaff(String staffId) {
        return invoices.values().stream().anyMatch(i -> i.getStaffId().equals(staffId));
    }
    public double revenueByDate(LocalDate date) {
        return paidInvoices().stream().filter(i -> i.getCreatedAt().toLocalDate().equals(date))
                .mapToDouble(Invoice::getTotal).sum();
    }
    public double revenueByMonth(int month, int year) {
        return paidInvoices().stream().filter(i -> i.getCreatedAt().getMonthValue() == month
                && i.getCreatedAt().getYear() == year).mapToDouble(Invoice::getTotal).sum();
    }
    public double revenueByStaff(String staffId, int month, int year) {
        return paidInvoices().stream().filter(i -> i.getStaffId().equals(staffId)
                && i.getCreatedAt().getMonthValue() == month && i.getCreatedAt().getYear() == year)
                .mapToDouble(Invoice::getTotal).sum();
    }
    public Map<String, Double> topSellingProducts(int n) {
        Map<String, Double> totals = new HashMap<>();
        paidInvoices().forEach(i -> i.getDetails().forEach(d ->
                totals.merge(d.getProductName(), (double)d.getQuantity(), Double::sum)));
        return totals.entrySet().stream().sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .limit(Math.max(0, n)).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
                        (a,b) -> a, LinkedHashMap::new));
    }
    public List<Invoice> paidInvoices() {
        return invoices.values().stream().filter(i -> i.getStatus() == InvoiceStatus.PAID).toList();
    }
    public synchronized Invoice createTradeInInvoice(String staffId, String customerInput,
            String oldSerial, String newProductId, int quantity, double tradeInValue) {
        if (oldSerial == null || oldSerial.isBlank() || !products.isSerialAvailable(oldSerial))
            throw new IllegalArgumentException("Serial máy cũ không hợp lệ hoặc không khả dụng");
        if (tradeInValue <= 0) throw new IllegalArgumentException("Giá thu máy cũ phải lớn hơn 0");
        SaleProduct p = products.findById(newProductId);
        if (p == null || quantity <= 0 || p.getStockQuantity() < quantity)
            throw new IllegalArgumentException("Máy mới không đủ tồn kho");
        Invoice invoice = createInvoice(staffId, customerInput, Map.of(newProductId, quantity));
        invoice.setDiscountAmount(Math.min(tradeInValue, invoice.getSubtotal()));
        if (!payInvoice(invoice.getId(), PaymentMethod.CASH)) {
            invoices.remove(invoice.getId());
            throw new IllegalStateException("Không thể xuất kho máy mới");
        }
        if (!products.receiveTradeInSerial(oldSerial, newProductId, tradeInValue))
            throw new IllegalStateException("Đã thanh toán nhưng chưa ghi nhận được máy cũ; cần đối soát kho");
        tradeIns.add(new TradeInRecord(invoice.getId(), oldSerial, tradeInValue, LocalDate.now()));
        return invoice;
    }
    public List<TradeInRecord> getTradeIns() { return List.copyOf(tradeIns); }
    private String nextInvoiceId() { return String.format("INV%04d", nextInvoiceNumber++); }
    private Invoice requireInvoice(String id) {
        Invoice i = invoices.get(id);
        if (i == null) throw new IllegalArgumentException("Không tìm thấy hóa đơn: " + id);
        return i;
    }
    public record TradeInRecord(String invoiceId, String oldSerial, double tradeInValue, LocalDate date) {}
}
