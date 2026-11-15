package com.smartphonestore.manager;

import com.smartphonestore.model.product.Product;
import com.smartphonestore.model.product.ProductDetail;
import com.smartphonestore.model.sale.Invoice;
import com.smartphonestore.model.sale.InvoiceDetail;
import com.smartphonestore.model.sale.Payment;
import com.smartphonestore.util.DateUtil;
import com.smartphonestore.util.FileIO;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Quan ly hoa don, chi tiet hoa don va thanh toan. ProductManager la nguon serial duy nhat. */
public class SalesManager {
    private static final String INVOICE_FILE = "data/invoices.txt";
    private static final String DETAIL_FILE = "data/invoice_details.txt";
    private static final String PAYMENT_FILE = "data/payments.txt";

    private final List<Invoice> invoices = new ArrayList<>();
    private final List<InvoiceDetail> details = new ArrayList<>();
    // Nap mot lan khi tao manager; moi lan thanh toan chi them mot dong vao danh sach nay.
    private final List<String> paymentLines = new ArrayList<>();
    private final ProductManager productManager;

    public SalesManager(ProductManager productManager) {
        if (productManager == null) throw new IllegalArgumentException("ProductManager không được null.");
        this.productManager = productManager;
        loadAll();
    }

    private void loadAll() {
        for (String line : FileIO.readLines(INVOICE_FILE)) {
            if (isDataLine(line)) try { invoices.add(Invoice.fromFileString(line)); }
            catch (RuntimeException e) { System.err.println("Bỏ qua dòng hóa đơn lỗi: " + line); }
        }
        for (String line : FileIO.readLines(DETAIL_FILE)) {
            if (isDataLine(line)) try { details.add(InvoiceDetail.fromFileString(line)); }
            catch (RuntimeException e) { System.err.println("Bỏ qua dòng chi tiết lỗi: " + line); }
        }
        for (String line : FileIO.readLines(PAYMENT_FILE)) {
            if (isDataLine(line)) paymentLines.add(line);
        }
    }

    private boolean isDataLine(String line) {
        return line != null && !line.trim().isEmpty() && !line.trim().startsWith("#");
    }
    private boolean saveInvoices() {
        List<String> lines = new ArrayList<>();
        for (Invoice invoice : invoices) lines.add(invoice.toFileString());
        return FileIO.writeLines(INVOICE_FILE, lines);
    }
    private boolean saveDetails() {
        List<String> lines = new ArrayList<>();
        for (InvoiceDetail detail : details) lines.add(detail.toFileString());
        return FileIO.writeLines(DETAIL_FILE, lines);
    }
    private boolean savePayments() {
        List<String> lines = new ArrayList<>(paymentLines);
        return FileIO.writeLines(PAYMENT_FILE, lines);
    }

    public List<Invoice> getAllInvoices() { return new ArrayList<>(invoices); }
    public List<InvoiceDetail> getDetailsByInvoice(String invoiceId) {
        List<InvoiceDetail> result = new ArrayList<>();
        for (InvoiceDetail detail : details)
            if (detail.getInvoiceId().equalsIgnoreCase(invoiceId)) result.add(detail);
        return result;
    }
    public Invoice findInvoice(String id) {
        if (id == null) return null;
        for (Invoice invoice : invoices) if (invoice.getId().equalsIgnoreCase(id.trim())) return invoice;
        return null;
    }
    public String nextInvoiceId() {
        int max = 0;
        for (Invoice invoice : invoices) {
            String id = invoice.getId();
            if (id.matches("INV\\d+")) max = Math.max(max, Integer.parseInt(id.substring(3)));
        }
        return String.format("INV%02d", max + 1);
    }

    /** Quet serial, tim san pham va lay gia ban tu ProductManager. */
    public InvoiceDetail buildDetail(String invoiceId, String serial) {
        String error = productManager.checkSellable(serial);
        if (error != null) throw new IllegalArgumentException(error);
        ProductDetail productDetail = productManager.findDetailBySerial(serial);
        Product product = productManager.findByID(productDetail.getProductId());
        if (product == null) throw new IllegalArgumentException("Không tìm thấy sản phẩm của serial này.");
        return new InvoiceDetail(invoiceId, product.getId(), product.getName(),
                productDetail.getSerialNumber(), product.getPrice());
    }

    public boolean createInvoice(String id, String customerId, String staffId, double vatRate,
                                 List<InvoiceDetail> newDetails) {
        return createInvoice(id, customerId, staffId, vatRate, 0, newDetails);
    }

    //Hàm mới: nhận thêm tiền giảm giá VIP do SalesMenu tính từ CustomerManager (TV4)
    public boolean createInvoice(String id, String customerId, String staffId, double vatRate,
                                 double discountAmount, List<InvoiceDetail> newDetails) {
        if (newDetails == null || newDetails.isEmpty())
            throw new IllegalArgumentException("Hóa đơn phải có ít nhất một điện thoại.");
        if (findInvoice(id) != null) throw new IllegalArgumentException("Mã hóa đơn đã tồn tại.");

        Set<String> seen = new HashSet<>();
        double subtotal = 0;
        for (InvoiceDetail detail : newDetails) {
            if (!id.equals(detail.getInvoiceId())) throw new IllegalArgumentException("Mã hóa đơn trong chi tiết không khớp.");
            String serial = detail.getSerialNumber().toUpperCase();
            if (!seen.add(serial)) throw new IllegalArgumentException("Serial bị lặp: " + serial);
            String error = productManager.checkSellable(serial);
            if (error != null) throw new IllegalArgumentException(error);
            subtotal += detail.calculateLineTotal();
        }
        // Kiểm tra giảm giá TRƯỚC khi đổi trạng thái serial,
        // nếu để sau thì giảm giá sai sẽ làm serial bị chuyển SOLD mà không có hóa đơn
        if (discountAmount < 0 || discountAmount > subtotal)
            throw new IllegalArgumentException("Tiền giảm giá không hợp lệ.");

        // Doi serial IN_STOCK -> SOLD; neu loi giua chung thi hoan tac cac serial da doi.
        List<String> soldSerials = new ArrayList<>();
        for (InvoiceDetail detail : newDetails) {
            if (!productManager.sellSerial(detail.getSerialNumber())) {
                for (String serial : soldSerials) productManager.returnSerial(serial);
                throw new IllegalStateException("Không thể cập nhật trạng thái serial " + detail.getSerialNumber());
            }
            soldSerials.add(detail.getSerialNumber());
        }

        // Truyền tiền giảm giá vào hóa đơn
        Invoice invoice = new Invoice(id, customerId, staffId, DateUtil.nowText(), subtotal,
                "UNPAID", vatRate, discountAmount, newDetails);
        invoices.add(invoice); details.addAll(newDetails);
        if (!saveInvoices() || !saveDetails()) {
            invoices.remove(invoice); details.removeAll(newDetails);
            saveInvoices();
            saveDetails();
            for (String serial : soldSerials) productManager.returnSerial(serial);
            throw new IllegalStateException("Không lưu được hóa đơn; đã hoàn tác serial.");
        }
        return true;
    }

    public boolean addPayment(Payment payment) {
        Invoice invoice = findInvoice(payment.getInvoiceId());
        if (invoice == null || !"UNPAID".equals(invoice.getStatus())) return false;
        payment.complete();
        invoice.setStatus("PAID");
        paymentLines.add(payment.toFileString());
        if (!saveInvoices() || !savePayments()) {
            invoice.setStatus("UNPAID");
            paymentLines.remove(paymentLines.size() - 1);
            saveInvoices();
            savePayments();
            return false;
        }
        return true;
    }

    /** Chi huy hoa don chua thanh toan va tra serial SOLD -> IN_STOCK. */
    public boolean cancelInvoice(String id) {
        Invoice invoice = findInvoice(id);
        if (invoice == null || !"UNPAID".equals(invoice.getStatus())) return false;
        List<String> returned = new ArrayList<>();
        for (InvoiceDetail detail : getDetailsByInvoice(id)) {
            if (!productManager.returnSerial(detail.getSerialNumber())) {
                for (String serial : returned) productManager.sellSerial(serial);
                return false;
            }
            returned.add(detail.getSerialNumber());
        }
        invoice.setStatus("CANCELLED");
        if (!saveInvoices()) {
            invoice.setStatus("UNPAID");
            for (String serial : returned) productManager.sellSerial(serial);
            return false;
        }
        return true;
    }

    private Invoice findPaidInvoiceBySerial(String serial) {
        if (serial == null) return null;
        for (InvoiceDetail detail : details) {
            if (detail.getSerialNumber().equalsIgnoreCase(serial.trim())) {
                Invoice invoice = findInvoice(detail.getInvoiceId());
                if (invoice != null && "PAID".equals(invoice.getStatus())) return invoice;
            }
        }
        return null;
    }
    public String getBuyerIdBySerial(String serial) {
        Invoice invoice = findPaidInvoiceBySerial(serial);
        return invoice == null || invoice.getCustomerId().isEmpty() ? null : invoice.getCustomerId();
    }
    /** Khách còn hóa đơn nào không (kể cả UNPAID / CANCELLED). Dùng để chặn xóa khách. */
    public boolean hasInvoicesOfCustomer(String customerId) {
        if (customerId == null || customerId.trim().isEmpty()) return false;
        for (Invoice invoice : invoices) {
            if (invoice.getCustomerId().equalsIgnoreCase(customerId.trim())) return true;
        }
        return false;
    }

    /** Các mã nhân viên đã từng lập hóa đơn. Dùng để không cấp lại mã nhân viên cũ. */
    public List<String> getAllStaffIds() {
        List<String> result = new ArrayList<>();
        for (Invoice invoice : invoices) result.add(invoice.getSalesStaffId());
        return result;
    }

    public String getSaleDateBySerial(String serial) {
        Invoice invoice = findPaidInvoiceBySerial(serial);
        return invoice == null ? null : invoice.getCreationDate();
    }

    /** Sinh ma thanh toan tiep theo theo quy uoc PAY01, PAY02, ... */
    public String nextPaymentId() {
        int max = 0;
        for (String line : paymentLines) {
            String[] fields = line.split("\\|", -1);
            if (fields.length > 0) {
                String id = fields[0].trim();
                if (id.matches("PAY\\d+")) {
                    try {
                        max = Math.max(max, Integer.parseInt(id.substring(3)));
                    } catch (NumberFormatException e) {
                        // Bo qua ma thanh toan khong hop le.
                    }
                }
            }
        }
        return String.format("PAY%02d", max + 1);
    }

    public double revenueToday() {
        LocalDate today = LocalDate.now();
        double total = 0;
        for (Invoice invoice : invoices) {
            if ("PAID".equals(invoice.getStatus())) {
                try {
                    if (DateUtil.parse(invoice.getCreationDate()).equals(today)) total += invoice.calculateFinalTotal();
                } catch (RuntimeException e) {
                    System.err.println("Ngày hóa đơn không hợp lệ: " + invoice.getId());
                }
            }
        }
        return total;
    }
}
