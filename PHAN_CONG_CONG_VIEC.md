# PHÂN CÔNG CÔNG VIỆC CHI TIẾT (TEAM WORKLOAD DISTRIBUTION)
# HỆ THỐNG BÁN HÀNG ĐIỆN THOẠI OFFLINE (4 THÀNH VIÊN = 4 MENU ĐỘC LẬP)

---

## 1. Nguyên Tắc Cốt Lõi: "1 Thành Viên = 1 Menu Riêng Biệt = 5 Class + 1 Interface"

Để đảm bảo quyền lợi và trách nhiệm của từng thành viên rõ ràng, không ai làm hộ ai, và khi giảng viên chấm thi vấn đáp từng bạn chỉ cần mở đúng Menu của mình để demo:
- **Số lượng thành viên:** Đúng **4 thành viên**.
- **Số lượng Class & Interface:** Đúng **20 Class thực thể + 4 Interface chuẩn** (Mỗi bạn làm đúng 5 Class + 1 Interface).
- **Phân công Menu độc lập:** **Mỗi thành viên chịu trách nhiệm 100% cho 1 Bảng Menu riêng biệt**:
  - **Thành viên 1:** Màn hình Đăng nhập + **Menu ADMIN** (Quản lý hệ thống, tài khoản, nhân sự, tính lương).
  - **Thành viên 2:** **Menu KHO HÀNG** (Quản lý sản phẩm, tồn kho, quản lý số Seri/IMEI, vòng đời thiết bị).
  - **Thành viên 3:** **Menu BÁN HÀNG** (Lập hóa đơn, chi tiết dòng hàng theo serial, in hóa đơn VAT, thanh toán tiền mặt/chuyển khoản).
  - **Thành viên 4:** **MENU KHÁCH HÀNG & DỊCH VỤ (CustomerMenu)** (Quản lý hồ sơ khách hàng tại quầy, CRUD khách, tra cứu & thăng hạng VIP, tiếp nhận đánh giá phản hồi, lập & theo dõi tiến trình bảo hành máy).
- **Liên kết chéo chuẩn hóa:** Các module gọi nhau thông qua các hàm dịch vụ công khai (`Manager`), không can thiệp sâu vào nội bộ của nhau (ví dụ: TV3 bán hàng gọi `productManager.sellSerial()`; TV4 tiếp nhận bảo hành gọi `productManager.setWarrantyStatus()`).

---

## 2. Bảng Tổng Hợp Phân Bổ 20 Class & 4 Menu Cho 4 Thành Viên

| Thành viên | Phân hệ nghiệp vụ | 1 Interface chuẩn | 5 Class thực thể đảm nhiệm (Mỗi class có CRUD + 1 thuộc tính riêng + 1 hàm riêng) | 1 Menu chuyên trách (100% tự code) |
| :--- | :--- | :--- | :--- | :--- |
| **👤 THÀNH VIÊN 1** | **Quản trị Hệ thống & Nhân sự** | `ISalaryCalculable` | 1. `Account`<br>2. `Employee`<br>3. `AdminStaff`<br>4. `WarehouseStaff`<br>5. `SalesStaff` | **MENU ADMIN**<br>*(+ Màn hình Login/Register)* |
| **👤 THÀNH VIÊN 2** | **Quản lý Kho & Danh mục Sản phẩm** | `IDiscountable` | 6. `Product`<br>7. `Phone`<br>8. `Smartphone`<br>9. `FeaturePhone`<br>10. `Accessory`<br>*(+ `ProductDetail` quản lý Serial)* | **MENU KHO HÀNG** |
| **👤 THÀNH VIÊN 3** | **Quản lý Bán hàng & Thanh toán** | `IPrintable` | 11. `Invoice`<br>12. `InvoiceDetail`<br>13. `Payment`<br>14. `CashPayment`<br>15. `ElectronicPayment` | **MENU BÁN HÀNG** |
| **👤 THÀNH VIÊN 4** | **Quản lý Khách hàng & Dịch vụ Hậu mãi** | `ILoyaltyRankable` | 16. `Customer`<br>17. `StandardCustomer`<br>18. `VipCustomer`<br>19. `CustomerReview`<br>20. `WarrantyClaim` | **MENU KHÁCH HÀNG & DỊCH VỤ**<br>*(CustomerMenu)* |

---

## 3. Chi Tiết Phân Công Từng Thành Viên & Bảng Menu Tương Ứng

---

### 👤 THÀNH VIÊN 1: QUẢN TRỊ HỆ THỐNG & NHÂN SỰ
> **Menu chuyên trách:** **MÀN HÌNH ĐĂNG NHẬP** + **MENU ADMIN (showAdminMenu)**  
> **Trách nhiệm code:** Toàn bộ việc xác thực tài khoản, phân quyền đăng nhập, quản lý nhân viên và xuất bảng lương.

#### 1. Interface đảm nhiệm:
- **`ISalaryCalculable`**: Khai báo hàm `double calculateSalary()` để các lớp con `Employee` tính lương tháng.

#### 2. Danh mục 5 Class thực thể:
1. **Class `Account`** (Tài khoản người dùng đăng nhập hệ thống):
   - *Thuộc tính chung:* `id`, `username`, `password`, `role` (ADMIN, WAREHOUSE, SALES), `status` (ACTIVE/LOCKED)
   - *Thuộc tính riêng:* `lastLoginTime` (Thời gian ghi nhận lần đăng nhập cuối cùng, kiểu `String`)
   - *Phương thức riêng:* `boolean checkLogin(String user, String pass)` và `void changePassword(String newPass)`
   - *File lưu trữ:* `data/accounts.txt`
2. **Class `Employee`** (Lớp cha trừu tượng cho mọi nhân viên):
   - *Thuộc tính chung:* `id`, `fullName`, `phoneNumber`, `baseSalary`, `role`
   - *Thuộc tính riêng:* `hireYear` (Năm bắt đầu vào làm việc tại cửa hàng, kiểu `int`)
   - *Phương thức riêng:* `int getSeniorityYears()` = `currentYear - hireYear` (Tính số năm thâm niên)
   - *File lưu trữ:* `data/employees.txt`
3. **Class `AdminStaff`** (Cửa hàng trưởng / Quản trị viên - kế thừa `Employee`, implements `ISalaryCalculable`):
   - *Thuộc tính riêng:* `managementBonus` (Phụ cấp trách nhiệm quản lý cửa hàng, kiểu `double`)
   - *Phương thức riêng:* Ghi đè `calculateSalary()` = `baseSalary + managementBonus`
4. **Class `WarehouseStaff`** (Nhân viên thủ kho - kế thừa `Employee`, implements `ISalaryCalculable`):
   - *Thuộc tính riêng:* `warehouseZone` (Khu vực kho phụ trách chính: KHO_A, KHO_B, kiểu `String`)
   - *Phương thức riêng:* Ghi đè `calculateSalary()` = `baseSalary + (overtimeHours * 50_000)`
5. **Class `SalesStaff`** (Nhân viên tư vấn bán hàng - kế thừa `Employee`, implements `ISalaryCalculable`):
   - *Thuộc tính riêng:* `commissionRate` (Tỷ lệ hoa hồng hưởng theo doanh số, ví dụ `0.03` tương ứng 3%, kiểu `double`)
   - *Phương thức riêng:* Ghi đè `calculateSalary()` = `baseSalary + (monthlyRevenue * commissionRate)`

#### 3. Toàn bộ Menu do Thành viên 1 làm (showAdminMenu):
- `[1] Xem danh sách toàn bộ tài khoản hệ thống (Account)`
- `[2] Đổi mật khẩu & Khóa / Mở khóa tài khoản người dùng (Account)`
- `[3] Xem danh sách toàn bộ nhân sự cửa hàng (Employee)`
- `[4] Thêm nhân viên mới (AdminStaff, WarehouseStaff, SalesStaff)`
- `[5] Cập nhật thông tin lương cơ bản & chức vụ theo Mã ID`
- `[6] Xóa nhân viên khỏi hệ thống theo Mã ID`
- `[7] Tìm kiếm nhân viên theo Tên hoặc Mã ID`
- `[8] Bảng tính lương chi tiết nhân viên tháng này (ISalaryCalculable.calculateSalary)`
- `[9] Đăng xuất` & `[0] Thoát toàn bộ chương trình`

---

### 👤 THÀNH VIÊN 2: QUẢN LÝ KHO & SẢN PHẨM
> **Menu chuyên trách:** **MENU KHO HÀNG (showWarehouseMenu)**  
> **Trách nhiệm code:** Toàn bộ việc quản lý sản phẩm trong kho (Smartphone, FeaturePhone, Phụ kiện), quản lý vòng đời số Seri/IMEI cho cả nhóm, cập nhật giá, nhập hàng tăng tồn kho và cảnh báo hết hàng.

#### 1. Interface đảm nhiệm:
- **`IDiscountable`**: Khai báo hàm `double calculateDiscountedPrice(double discountRate)` để tính giá sau chiết khấu.

#### 2. Danh mục 5 Class thực thể:
6. **Class `Product`** (Lớp cha trừu tượng cho toàn bộ mặt hàng):
   - *Thuộc tính chung:* `id`, `name`, `brand`, `price`, `stockQuantity`
   - *Thuộc tính riêng:* `minStockThreshold` (Ngưỡng tồn kho tối thiểu cần cảnh báo nhập thêm, kiểu `int`)
   - *Phương thức trừu tượng mới:* `public abstract int getWarrantyMonths();` (Số tháng bảo hành chuẩn cho mọi sản phẩm)
   - *Phương thức riêng:* `boolean isLowStock()` = `stockQuantity <= minStockThreshold`
   - *File lưu trữ:* `data/products.txt`
7. **Class `Phone`** (Lớp con trừu tượng kế thừa `Product`):
   - *Thuộc tính riêng:* `operatingSystem` (Hệ điều hành của máy: iOS, Android, KaiOS, kiểu `String`)
   - *Phương thức riêng:* Ghi đè `@Override public int getWarrantyMonths()` (iOS/Apple: 12 tháng, Android: 18 tháng)
8. **Class `Smartphone`** (Điện thoại thông minh - kế thừa `Phone`, implements `IDiscountable`):
   - *Thuộc tính riêng:* `storageCapacity` (Dung lượng bộ nhớ trong: 128GB, 256GB, 512GB, kiểu `int`)
   - *Phương thức riêng:* `double applyTradeInDiscount()` (Tính giá trợ giá thu cũ đổi mới)
9. **Class `FeaturePhone`** (Điện thoại bàn phím phổ thông - kế thừa `Phone`):
   - *Thuộc tính riêng:* `batteryLifeDays` (Thời lượng pin sử dụng liên tục tính theo ngày, kiểu `int`)
   - *Phương thức riêng:* `void toggleEmergencySOS()` (Bật/tắt chế độ phím gọi khẩn cấp SOS)
10. **Class `Accessory`** (Phụ kiện điện thoại - kế thừa `Product`, implements `IDiscountable`):
    - *Thuộc tính riêng:* `accessoryType` (Loại phụ kiện: SAC, TAI_NGHE, OP_LUNG, CUONG_LUC, kiểu `String`)
    - *Phương thức riêng:* Ghi đè `@Override public int getWarrantyMonths() { return 6; }` (Phụ kiện bảo hành 6 tháng) và `boolean isCompatibleWith(String brand)`
11. *(Class bổ trợ quản lý máy vật lý)* **`ProductDetail`**:
    - *Thuộc tính:* `productId`, `productName`, `serialNumber`, `importDate` (`dd/MM/yyyy HH:mm:ss`), `importPrice`, `supplier`, `status` (`IN_STOCK`, `SOLD`, `WARRANTY`).
    - *File lưu trữ:* `data/product_details.txt`

#### 3. Các phương thức nghiệp vụ TV2 cung cấp cho các thành viên khác trong `ProductManager`:
- `boolean sellSerial(String serial)`: TV3 gọi khi xuất bán máy $\rightarrow$ chuyển `IN_STOCK` thành `SOLD`, trừ tồn kho 1 con.
- `boolean returnSerial(String serial)`: TV3 gọi khi hủy hóa đơn $\rightarrow$ chuyển `SOLD` về `IN_STOCK`, cộng lại tồn kho 1 con.
- `boolean setWarrantyStatus(String serial, boolean inWarranty)`: TV4 gọi khi tiếp nhận/hoàn tất bảo hành $\rightarrow$ chuyển đổi giữa `SOLD` $\leftrightarrow$ `WARRANTY`.
- `String checkSellable(String serial)`: Kiểm tra máy có đủ điều kiện bán hay không.
- `void syncStockFromDetails()`: Đồng bộ số lượng tồn kho `stockQuantity` trong `products.txt` theo đúng số lượng serial `IN_STOCK`.
- `double applyDiscount(String id, Double rate)`: Chỉ chiết khấu giá bán trong `products.txt`, tuyệt đối không ghi đè `importPrice` trong `product_details.txt`.

#### 4. Toàn bộ Menu do Thành viên 2 làm (WarehouseMenu):
- `[1] Xem danh mục toàn bộ sản phẩm & Số lượng tồn kho (Product)`
- `[2] Xem bảng chi tiết từng con máy (Serial/IMEI, Ngày giờ nhập, Giá nhập, Trạng thái)`
- `[3] Lập phiếu nhập hàng vào kho (Nhập theo đợt / Nhập mới - Gợi ý ngày giờ dd/MM/yyyy HH:mm:ss)`
- `[4] Cập nhật thông tin / Giá bán sản phẩm theo Mã ID`
- `[5] Xóa sản phẩm khỏi danh mục kho theo Mã ID`
- `[6] Tìm kiếm sản phẩm theo Tên / Thương hiệu`
- `[7] Cảnh báo & Lọc danh sách sản phẩm sắp hết hàng (isLowStock)`
- `[8] Áp dụng chương trình chiết khấu sản phẩm (IDiscountable)`
- `[9] Kiểm tra tính năng độc quyền (Thu cũ đổi mới / SOS / Tương thích phụ kiện)`
- `[0] Thoát toàn bộ chương trình`

---

### 👤 THÀNH VIÊN 3: QUẢN LÝ BÁN HÀNG & HÓA ĐƠN
> **Menu chuyên trách:** **MENU BÁN HÀNG (showSalesMenu)**  
> **Trách nhiệm code:** Toàn bộ việc lập hóa đơn bán lẻ, chi tiết dòng hàng theo từng số Serial/IMEI, in phiếu thanh toán chuẩn VAT, và xử lý thanh toán tiền mặt/chuyển khoản QR.

#### 1. Interface đảm nhiệm:
- **`IPrintable`**: Khai báo hàm `void printReceipt()` để in hóa đơn bán lẻ chuẩn ra console.

#### 2. Danh mục 5 Class thực thể:
11. **Class `Invoice`** (Hóa đơn bán lẻ - implements `IPrintable`):
    - *Thuộc tính chung:* `id`, `customerId`, `salesStaffId`, `creationDate`, `totalAmount`, `status` (PAID/CANCELLED)
    - *Thuộc tính riêng:* `vatTaxRate` (Mức thuế suất VAT áp dụng, ví dụ `0.08` tức 8%, kiểu `double`)
    - *Phương thức riêng:* `double calculateFinalTotal()` = `totalAmount * (1 + vatTaxRate)`
    - *File lưu trữ:* `data/invoices.txt`
12. **Class `InvoiceDetail`** (Dòng chi tiết mặt hàng trong hóa đơn):
    - *Thuộc tính:* `invoiceId`, `productId`, `productName`, `serialNumber`, `unitPrice`, `quantity` (luôn cố định là `1` cho mỗi số serial)
    - *Phương thức riêng:* `double calculateLineTotal()` = `unitPrice * quantity`
    - *File lưu trữ:* `data/invoice_details.txt` (6 cột: `invoiceId|productId|productName|serialNumber|unitPrice|quantity`)
13. **Class `Payment`** (Lớp cha trừu tượng cho phương thức thanh toán):
    - *Thuộc tính chung:* `paymentId`, `invoiceId`, `amount`, `paymentDate`
    - *Thuộc tính riêng:* `transactionCode` (Mã tham chiếu giao dịch thanh toán duy nhất, kiểu `String`)
    - *Phương thức riêng:* `boolean verifyPaymentStatus()` (Kiểm tra trạng thái hoàn tất giao dịch)
    - *File lưu trữ:* `data/payments.txt`
14. **Class `CashPayment`** (Thanh toán tiền mặt - kế thừa `Payment`):
    - *Thuộc tính riêng:* `cashGiven` (Số tiền mặt khách đưa cho thu ngân, kiểu `double`)
    - *Phương thức riêng:* `double calculateChange()` = `cashGiven - amount` (Tính tiền thừa thối lại)
15. **Class `ElectronicPayment`** (Thanh toán chuyển khoản / Quét mã QR - kế thừa `Payment`):
    - *Thuộc tính riêng:* `bankOrWalletName` (Tên ngân hàng hoặc ví điện tử: Vietcombank, Momo, kiểu `String`)
    - *Phương thức riêng:* `String generatePaymentQRCode()` (Sinh chuỗi mã QR chuyển khoản)

#### 3. Các hàm TV3 cung cấp cho TV4 trong `SalesManager`:
- `String getSaleDateBySerial(String serial)`: Trả về ngày bán của con máy theo số serial từ hóa đơn đã thanh toán (`PAID`).
- `String getBuyerIdBySerial(String serial)`: Trả về mã khách hàng (`customerId`) đã mua con máy đó.

#### 4. Toàn bộ Menu do Thành viên 3 làm (SalesMenu):
- `[1] Xem danh sách toàn bộ hóa đơn đã lập (Invoice)`
- `[2] Lập Hóa đơn bán lẻ mới (Chọn Serial và gọi productManager.sellSerial(serial))`
- `[3] Xem chi tiết các dòng sản phẩm trong hóa đơn (InvoiceDetail có số Serial)`
- `[4] In phiếu hóa đơn thanh toán chuẩn VAT ra màn hình (IPrintable.printReceipt)`
- `[5] Xử lý Thanh toán Tiền mặt (CashPayment - Tự tính tiền thối cho khách)`
- `[6] Xử lý Thanh toán Chuyển khoản QR (ElectronicPayment - Tạo chuỗi mã QR)`
- `[7] Tìm kiếm hóa đơn theo Mã ID`
- `[8] Hủy hóa đơn bán hàng theo Mã ID (Gọi productManager.returnSerial(serial))`
- `[9] Thống kê tổng doanh thu bán lẻ trong ngày (So khớp ngày bằng DateUtil)`
- `[10] Đăng xuất` & `[0] Thoát toàn bộ chương trình`

---

### 👤 THÀNH VIÊN 4: QUẢN LÝ KHÁCH HÀNG & DỊCH VỤ HẬU MÃI
> **Menu chuyên trách:** **MENU QUẢN LÝ KHÁCH HÀNG & DỊCH VỤ (CustomerMenu)**  
> **Trách nhiệm code:** Toàn bộ việc quản lý khách hàng tại quầy (CRUD), tra cứu & thăng hạng VIP, tiếp nhận đánh giá review và quản lý toàn diện quy trình tiếp nhận & xử lý bảo hành máy.

#### 1. Interface đảm nhiệm:
- **`ILoyaltyRankable`**: Khai báo hàm `String evaluateRank()` để hệ thống tự động kiểm tra và trả về xếp hạng thành viên (Standard hoặc VIP).

#### 2. Danh mục 5 Class thực thể:
16. **Class `Customer`** (Lớp cha trừu tượng đại diện cho hồ sơ khách hàng):
    - *Thuộc tính chung:* `id`, `fullName`, `phoneNumber`, `email`
    - *Thuộc tính riêng:* `totalSpent` (Tổng số tiền đã tích lũy mua sắm từ trước đến nay, kiểu `double`)
    - *Phương thức riêng:* `void accumulateSpending(double amount)` (Tự động cộng dồn doanh số khi khách mua hàng)
    - *File lưu trữ:* `data/customers.txt`
17. **Class `StandardCustomer`** (Khách hàng thông thường - kế thừa `Customer`, implements `ILoyaltyRankable`):
    - *Thuộc tính riêng:* `visitCount` (Số lần đã đến mua sắm tại cửa hàng, kiểu `int`)
    - *Phương thức riêng:* `boolean isEligibleForUpgrade()` = `totalSpent >= 15_000_000` & `VipCustomer upgradeToVip()`
18. **Class `VipCustomer`** (Khách hàng thân thiết VIP - kế thừa `Customer`, implements `ILoyaltyRankable`):
    - *Thuộc tính riêng:* `vipDiscountRate` (Tỷ lệ giảm giá cố định cho khách VIP, ví dụ `0.05` tức 5%, kiểu `double`)
    - *Phương thức riêng:* `double calculateVipDiscount(double orderTotal)` = `orderTotal * vipDiscountRate`
19. **Class `CustomerReview`** (Đánh giá nhận xét của khách hàng):
    - *Thuộc tính:* `reviewId`, `customerId`, `productId`, `ratingStars`, `comment`, `reviewDate`
    - *Phương thức riêng:* `boolean isPositiveFeedback()` = `ratingStars >= 4`
    - *File lưu trữ:* `data/reviews.txt`
20. **Class `WarrantyClaim`** (Phiếu yêu cầu tiếp nhận bảo hành máy):
    - *Cấu trúc 9 cột chuẩn mới:* `id|mã khách|mã sản phẩm|seri|lỗi|ngày giờ nhận|trạng thái|ngày bán|số tháng BH`
    - *Phương thức riêng:* `void updateStatus(String newStatus)`, `LocalDate getWarrantyExpiryDate()`, `boolean isUnderWarrantyOn(LocalDate date)`
    - *File lưu trữ:* `data/warranty_claims.txt`

#### 3. Toàn bộ Menu do Thành viên 4 làm (CustomerMenu):
- `[1] Xem danh sách toàn bộ khách hàng & Xem chi tiết theo Mã ID`
- `[2] Thêm khách hàng mới tại quầy (StandardCustomer)`
- `[3] Cập nhật thông tin SĐT, Email khách hàng theo Mã ID`
- `[4] Xóa khách hàng khỏi hệ thống (Phân quyền chỉ ADMIN)`
- `[5] Tìm kiếm khách hàng theo Tên hoặc Số điện thoại`
- `[6] Tra cứu & Tự động đánh giá xếp hạng thành viên (ILoyaltyRankable.evaluateRank)`
- `[7] Nâng khách lên VIP (upgradeToVip) & Tra cứu đặc quyền giảm giá (calculateVipDiscount)`
- `[8] Tiếp nhận & Ghi nhận đánh giá phản hồi từ khách tại quầy (CustomerReview)`
- `[9] Xem lịch sử đánh giá phản hồi (lọc theo Mã KH hoặc Sản phẩm)`
- `[10] Tiếp nhận máy lỗi & Tạo phiếu bảo hành mới (Kiểm tra Serial, gọi setWarrantyStatus(true))`
- `[11] Tra cứu & Cập nhật trạng thái bảo hành (Khi đóng phiếu gọi setWarrantyStatus(false))`
- `[0] Quay lại Menu trước (Admin / Sales)`

---

## 4. Checklist Nghiệm Thu Đồ Án Chuẩn Ba-Rem

- [x] **Màn hình Đăng nhập:** Hoạt động trơn tru, nạp từ `accounts.txt`.
- [x] **4 Bảng Menu Độc Lập:**
  - [x] TV1: **Menu Admin** (Quản trị, Tài khoản, Nhân viên, Lương).
  - [x] TV2: **Menu Kho** (Sản phẩm, Smartphone, FeaturePhone, Phụ kiện, Serial/IMEI, Tồn kho).
  - [x] TV3: **Menu Bán hàng** (Hóa đơn, Chi tiết hóa đơn theo serial, In phiếu, Thanh toán).
  - [x] TV4: **Menu Khách hàng & Dịch vụ** (Khách hàng, VIP, Đánh giá sao, Bảo hành 9 cột).
- [x] **Đúng số lượng 20 Class + 4 Interface:** Đảm bảo đủ 20 file class thực thể, mỗi bạn đúng 5 class và 1 interface.
- [x] **CRUD & File I/O UTF-8:** Dữ liệu lưu trong thư mục `data/` với 7 file text đồng bộ hoàn hảo, không bị mất mát dữ liệu khi khởi động lại.
