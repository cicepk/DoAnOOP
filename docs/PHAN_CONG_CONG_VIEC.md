# PHÂN CÔNG CÔNG VIỆC CHI TIẾT (TEAM WORKLOAD DISTRIBUTION)
# HỆ THỐNG BÁN HÀNG ĐIỆN THOẠI OFFLINE (4 THÀNH VIÊN = 4 MENU ĐỘC LẬP)

---

## 1. Nguyên Tắc Cốt Lõi: "1 Thành Viên = 1 Menu Riêng Biệt = 5 Class + 1 Interface"

Để đảm bảo quyền lợi và trách nhiệm của từng thành viên rõ ràng, không ai làm hộ ai, và khi giảng viên chấm thi vấn đáp từng bạn chỉ cần mở đúng Menu của mình để demo:
- **Số lượng thành viên:** Đúng **4 thành viên**.
- **Số lượng Class & Interface:** Đúng **20 Class thực thể + 4 Interface chuẩn** (Mỗi bạn làm đúng 5 Class + 1 Interface).
- **Phân công Menu độc lập:** **Mỗi thành viên chịu trách nhiệm 100% cho 1 Bảng Menu riêng biệt**:
  - **Thành viên 1:** Màn hình Đăng nhập/Đăng ký + **Menu ADMIN** (Quản lý hệ thống, tài khoản, nhân sự, tính lương).
  - **Thành viên 2:** **Menu KHO HÀNG** (Quản lý danh mục sản phẩm, điện thoại, phụ kiện, nhập hàng, tồn kho).
  - **Thành viên 3:** **Menu BÁN HÀNG** (Lập hóa đơn, chi tiết hóa đơn, in phiếu tính tiền, thanh toán).
  - **Thành viên 4:** **MENU KHÁCH HÀNG & DỊCH VỤ (CustomerMenu)** (Quản lý hồ sơ khách hàng tại quầy, CRUD khách, tra cứu & thăng hạng VIP, tiếp nhận đánh giá phản hồi, lập & theo dõi tiến trình bảo hành máy).
- **Liên kết chéo tối giản (Chỉ nhận 1 kết quả):** Các menu của mỗi bạn chỉ làm việc trên 5 class của bạn đó. Nếu cần liên kết với bạn khác, chỉ gọi **đúng 1 hàm kết quả** (ví dụ TV3 bán hàng gọi 1 lệnh `decreaseStock()` của TV2; TV1 xem tổng doanh thu gọi 1 hàm của TV3).

---

## 2. Bảng Tổng Hợp Phân Bổ 20 Class & 4 Menu Cho 4 Thành Viên

| Thành viên | Phân hệ nghiệp vụ | 1 Interface chuẩn | 5 Class thực thể đảm nhiệm (Mỗi class có CRUD + 1 thuộc tính riêng + 1 hàm riêng) | 1 Menu chuyên trách (100% tự code) |
| :--- | :--- | :--- | :--- | :--- |
| **👤 THÀNH VIÊN 1** | **Quản trị Hệ thống & Nhân sự** | `ISalaryCalculable` | 1. `Account`<br>2. `Employee`<br>3. `AdminStaff`<br>4. `WarehouseStaff`<br>5. `SalesStaff` | **MENU ADMIN**<br>*(+ Màn hình Login/Register)* |
| **👤 THÀNH VIÊN 2** | **Quản lý Kho & Danh mục Sản phẩm** | `IDiscountable` | 6. `Product`<br>7. `Phone`<br>8. `Smartphone`<br>9. `FeaturePhone`<br>10. `Accessory` | **MENU KHO HÀNG** |
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
   - *Thuộc tính chung:* `id`, `username`, `password`, `role` (ADMIN, WAREHOUSE, SALES, CUSTOMER), `status` (ACTIVE/LOCKED)
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

#### 3. Toàn bộ Menu do Thành viên 1 làm (showAdminMenu - Đã rút gọn tinh giản):
- `[1] Xem danh sách toàn bộ tài khoản hệ thống (Account)`
- `[2] Đổi mật khẩu & Khóa / Mở khóa tài khoản người dùng (Account)`
- `[3] Xem danh sách toàn bộ nhân sự cửa hàng (Employee)`
- `[4] Thêm nhân viên mới (AdminStaff, WarehouseStaff, SalesStaff)`
- `[5] Cập nhật thông tin lương cơ bản & chức vụ theo Mã ID`
- `[6] Xóa nhân viên khỏi hệ thống theo Mã ID`
- `[7] Tìm kiếm nhân viên theo Tên hoặc Mã ID`
- `[8] Bảng tính lương chi tiết nhân viên tháng này (ISalaryCalculable.calculateSalary)`
- `[9] Đăng xuất (Quay lại màn hình Đăng nhập)` & `[0] Thoát toàn bộ chương trình`

#### 4. Câu hỏi vấn đáp mẫu:
- *Thầy/cô hỏi:* "Em phụ trách menu nào và làm gì trong đó?"  
  ➔ *Trả lời:* "Em làm màn hình Login/Register và Menu Admin. Em quản lý 5 class: Account để đăng nhập/đổi pass và 4 class nhân sự Employee, AdminStaff, WarehouseStaff, SalesStaff để tính lương theo đa hình `calculateSalary()`."

---

### 👤 THÀNH VIÊN 2: QUẢN LÝ KHO & SẢN PHẨM
> **Menu chuyên trách:** **MENU KHO HÀNG (showWarehouseMenu)**  
> **Trách nhiệm code:** Toàn bộ việc quản lý sản phẩm trong kho (Smartphone, FeaturePhone, Phụ kiện), cập nhật giá, nhập hàng tăng tồn kho và cảnh báo hết hàng.

#### 1. Interface đảm nhiệm:
- **`IDiscountable`**: Khai báo hàm `double calculateDiscountedPrice(double discountRate)` để tính giá sau khuyến mãi.

#### 2. Danh mục 5 Class thực thể:
6. **Class `Product`** (Lớp cha trừu tượng cho toàn bộ mặt hàng):
   - *Thuộc tính chung:* `id`, `name`, `brand`, `price`, `stockQuantity`
   - *Thuộc tính riêng:* `minStockThreshold` (Ngưỡng tồn kho tối thiểu cần cảnh báo nhập thêm, kiểu `int`)
   - *Phương thức riêng:* `boolean isLowStock()` = `stockQuantity <= minStockThreshold` (Kiểm tra hàng sắp hết)
   - *File lưu trữ:* `data/products.txt`
7. **Class `Phone`** (Lớp con trừu tượng kế thừa `Product`):
   - *Thuộc tính riêng:* `operatingSystem` (Hệ điều hành của máy: iOS, Android, KaiOS, kiểu `String`)
   - *Phương thức riêng:* `int getWarrantyMonths()` (Thời gian bảo hành chuẩn theo hệ máy, ví dụ iOS 12 tháng)
8. **Class `Smartphone`** (Điện thoại thông minh - kế thừa `Phone`, implements `IDiscountable`):
   - *Thuộc tính riêng:* `storageCapacity` (Dung lượng bộ nhớ trong: 128GB, 256GB, 512GB, kiểu `int`)
   - *Phương thức riêng:* `double applyTradeInDiscount()` (Tính giá trợ giá thu cũ đổi mới)
9. **Class `FeaturePhone`** (Điện thoại bàn phím phổ thông - kế thừa `Phone`):
   - *Thuộc tính riêng:* `batteryLifeDays` (Thời lượng pin sử dụng liên tục tính theo ngày, kiểu `int`)
   - *Phương thức riêng:* `void toggleEmergencySOS()` (Bật/tắt chế độ phím gọi khẩn cấp SOS)
10. **Class `Accessory`** (Phụ kiện điện thoại - kế thừa `Product`, implements `IDiscountable`):
    - *Thuộc tính riêng:* `accessoryType` (Loại phụ kiện: SAC, TAI_NGHE, OP_LUNG, CUONG_LUC, kiểu `String`)
    - *Phương thức riêng:* `boolean isCompatibleWith(String brand)` (Kiểm tra phụ kiện tương thích hãng máy)

#### 3. Toàn bộ Menu do Thành viên 2 làm (showWarehouseMenu):
- `[1] Xem danh mục toàn bộ sản phẩm & Số lượng tồn kho (Product)`
- `[2] Xem bảng chi tiết từng con máy (Serial/IMEI & Ngày nhập theo Hãng - ProductDetail)`
- `[3] Lập phiếu nhập hàng vào kho (Nhập lô hàng / Nhập mới - Vòng lặp while đến khi gõ 0)`
- `[4] Cập nhật thông tin / Giá bán sản phẩm theo Mã ID (Update)`
- `[5] Xóa sản phẩm khỏi danh mục kho theo Mã ID (Delete)`
- `[6] Tìm kiếm sản phẩm theo Tên / Thương hiệu (Search)`
- `[7] Cảnh báo & Lọc danh sách sản phẩm sắp hết hàng (gọi isLowStock)`
- `[8] Áp dụng chương trình giảm giá chiết khấu sản phẩm (IDiscountable)`
- `[9] Kiểm tra tính năng độc quyền (Thu cũ đổi mới / SOS / Tương thích phụ kiện)`
- `[0] Thoát toàn bộ chương trình (Tự động lưu dữ liệu)`

#### 4. Câu hỏi vấn đáp mẫu:
- *Thầy/cô hỏi:* "Tính kế thừa và đa hình trong Menu Kho của em thể hiện ở đâu?"  
  ➔ *Trả lời:* "Lớp cha `Product` phân thành `Phone` và `Accessory`. Từ `Phone` kế thừa tiếp thành `Smartphone` và `FeaturePhone`. Khi thêm sản phẩm hoặc tính chiết khấu `calculateDiscountedPrice()`, em áp dụng đa hình từ Interface `IDiscountable`."

---

### 👤 THÀNH VIÊN 3: QUẢN LÝ BÁN HÀNG & HÓA ĐƠN
> **Menu chuyên trách:** **MENU BÁN HÀNG (showSalesMenu)**  
> **Trách nhiệm code:** Toàn bộ việc lập hóa đơn bán lẻ, chi tiết dòng hàng, in phiếu hóa đơn thanh toán chuẩn VAT, và xử lý thanh toán tiền mặt/chuyển khoản QR.

#### 1. Interface đảm nhiệm:
- **`IPrintable`**: Khai báo hàm `void printReceipt()` để in phiếu hóa đơn bán lẻ chuẩn ra console.

#### 2. Danh mục 5 Class thực thể:
11. **Class `Invoice`** (Hóa đơn bán lẻ - implements `IPrintable`):
    - *Thuộc tính chung:* `id`, `customerId`, `salesStaffId`, `creationDate`, `totalAmount`, `status` (PAID/CANCELLED)
    - *Thuộc tính riêng:* `vatTaxRate` (Mức thuế suất VAT áp dụng, ví dụ `0.08` tức 8%, kiểu `double`)
    - *Phương thức riêng:* `double calculateFinalTotal()` = `totalAmount * (1 + vatTaxRate)` (Tính tổng tiền gồm thuế)
    - *File lưu trữ:* `data/invoices.txt`
12. **Class `InvoiceDetail`** (Dòng chi tiết mặt hàng trong hóa đơn):
    - *Thuộc tính chung:* `invoiceId`, `productId`, `productName`, `unitPrice`
    - *Thuộc tính riêng:* `quantity` (Số lượng sản phẩm khách mua, kiểu `int`)
    - *Phương thức riêng:* `double calculateLineTotal()` = `unitPrice * quantity` (Thành tiền của từng dòng)
    - *File lưu trữ:* `data/invoice_details.txt`
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

#### 3. Toàn bộ Menu do Thành viên 3 làm (showSalesMenu):
- `[1] Xem danh sách toàn bộ hóa đơn đã lập (Invoice)`
- `[2] Lập Hóa đơn bán lẻ mới (Tạo Invoice & các InvoiceDetail)`  
  *(*Chỉ gọi 1 thao tác trừ kho `product.decreaseStock()` của TV2)*
- `[3] Xem chi tiết các dòng sản phẩm trong hóa đơn (InvoiceDetail)`
- `[4] In phiếu hóa đơn thanh toán chuẩn VAT ra màn hình (IPrintable.printReceipt)`
- `[5] Xử lý Thanh toán Tiền mặt (CashPayment - Tự tính tiền thối cho khách)`
- `[6] Xử lý Thanh toán Chuyển khoản QR (ElectronicPayment - Tạo chuỗi mã QR)`
- `[7] Tìm kiếm hóa đơn theo Mã ID (Search Invoice)`
- `[8] Hủy hóa đơn bán hàng theo Mã ID (Cancel Invoice)`
- `[9] Thống kê tổng doanh thu bán lẻ trong ngày`
- `[10] Đăng xuất` & `[0] Thoát toàn bộ chương trình`

#### 4. Câu hỏi vấn đáp mẫu:
- *Thầy/cô hỏi:* "Tại sao thanh toán lại tách thành CashPayment và ElectronicPayment?"  
  ➔ *Trả lời:* "Vì thanh toán tiền mặt có nghiệp vụ riêng là tính tiền thối `calculateChange()`, còn thanh toán điện tử có nghiệp vụ tạo mã chuyển khoản `generatePaymentQRCode()`. Lớp cha `Payment` áp dụng đa hình khi xử lý thanh toán hóa đơn."

---

### 👤 THÀNH VIÊN 4: QUẢN LÝ KHÁCH HÀNG & DỊCH VỤ HẬU MÃI (CUSTOMER & SERVICE MANAGEMENT)
> **Menu chuyên trách:** **MENU QUẢN LÝ KHÁCH HÀNG & DỊCH VỤ (CustomerMenu)**  
> **Bản chất vai trò:** Trong mô hình bán lẻ điện thoại offline tại quầy (POS), khách mua hàng không trực tiếp đăng nhập vào console của cửa hàng. Thay vào đó, **Thành viên 4 chịu trách nhiệm 100% cho phân hệ Quản lý Khách hàng & Dịch vụ hậu mãi** (tiếp nhận thông tin khách tại quầy, tra cứu/nâng hạng VIP, ghi nhận review đánh giá và xử lý phiếu bảo hành sửa chữa máy).

#### 1. Interface đảm nhiệm:
- **`ILoyaltyRankable`**: Khai báo hàm `String evaluateRank()` để hệ thống tự động kiểm tra và trả về xếp hạng thành viên (Standard hoặc VIP) cho khách hàng.

#### 2. Danh mục 5 Class thực thể:
16. **Class `Customer`** (Lớp cha trừu tượng đại diện cho hồ sơ khách hàng):
    - *Thuộc tính chung:* `id`, `fullName`, `phoneNumber`, `email`
    - *Thuộc tính riêng:* `totalSpent` (Tổng số tiền đã tích lũy mua sắm từ trước đến nay, kiểu `double`)
    - *Phương thức riêng:* `void accumulateSpending(double amount)` (Tự động cộng dồn doanh số khi khách mua hàng)
    - *File lưu trữ:* `data/customers.txt`
17. **Class `StandardCustomer`** (Khách hàng thông thường - kế thừa `Customer`, implements `ILoyaltyRankable`):
    - *Thuộc tính riêng:* `visitCount` (Số lần đã đến mua sắm tại cửa hàng, kiểu `int`)
    - *Phương thức riêng:* `boolean isEligibleForUpgrade()` = `totalSpent >= 15_000_000` (Kiểm tra xem khách đã đủ điều kiện nâng lên VIP chưa) & `VipCustomer upgradeToVip()` (Thăng hạng khách lên VIP)
18. **Class `VipCustomer`** (Khách hàng thân thiết VIP - kế thừa `Customer`, implements `ILoyaltyRankable`):
    - *Thuộc tính riêng:* `vipDiscountRate` (Tỷ lệ giảm giá cố định cho khách VIP, ví dụ `0.05` tức 5%, kiểu `double`)
    - *Phương thức riêng:* `double calculateVipDiscount(double orderTotal)` = `orderTotal * vipDiscountRate` (Tính số tiền khách VIP được miễn giảm khi thanh toán)
19. **Class `CustomerReview`** (Đánh giá nhận xét của khách hàng về sản phẩm/dịch vụ tại quầy):
    - *Thuộc tính chung:* `reviewId`, `customerId`, `productId`, `reviewDate`
    - *Thuộc tính riêng:* `ratingStars` (Số sao đánh giá từ 1 đến 5 sao, kiểu `int`)
    - *Phương thức riêng:* `boolean isPositiveFeedback()` = `ratingStars >= 4` (Kiểm tra đánh giá tích cực/hài lòng)
    - *File lưu trữ:* `data/reviews.txt`
20. **Class `WarrantyClaim`** (Phiếu yêu cầu tiếp nhận bảo hành máy của khách hàng):
    - *Thuộc tính chung:* `claimId`, `customerId`, `productId`, `issueDescription`, `createdDate`
    - *Thuộc tính riêng:* `claimStatus` (Trạng thái xử lý: TIEP_NHAN, DANG_SUA, HOAN_TAT, TU_CHOI, kiểu `String`)
    - *Phương thức riêng:* `void updateStatus(String newStatus)` (Cập nhật tiến trình/trạng thái xử lý bảo hành)
    - *File lưu trữ:* `data/warranty_claims.txt`

#### 3. Toàn bộ Menu do Thành viên 4 làm (CustomerMenu - Đã chuyển đổi chuẩn POS & CRUD):
- `[1] Xem danh sách toàn bộ khách hàng & Xem chi tiết theo Mã ID (Customer)`
- `[2] Thêm khách hàng mới tại quầy (Create: StandardCustomer)`
- `[3] Cập nhật thông tin SĐT, Email khách hàng theo Mã ID (Update)`
- `[4] Xóa khách hàng khỏi hệ thống theo Mã ID (Delete: Phân quyền chỉ ADMIN)`
- `[5] Tìm kiếm khách hàng theo Tên hoặc Số điện thoại (Search)`
- `[6] Tra cứu & Tự động đánh giá xếp hạng thành viên (ILoyaltyRankable.evaluateRank)`
- `[7] Nâng khách lên VIP (upgradeToVip) & Tra cứu đặc quyền giảm giá (calculateVipDiscount)`
- `[8] Tiếp nhận & Ghi nhận đánh giá phản hồi từ khách tại quầy (CustomerReview)`
- `[9] Xem lịch sử đánh giá phản hồi (lọc theo Mã KH hoặc Sản phẩm)`
- `[10] Tiếp nhận máy lỗi & Tạo phiếu yêu cầu bảo hành mới (Tạo WarrantyClaim)`
- `[11] Tra cứu & Cập nhật tiến trình/trạng thái bảo hành máy (WarrantyClaim.updateStatus)`
- `[0] Quay lại Menu trước (Admin / Sales)`

#### 4. Những điểm phối hợp & ảnh hưởng đến thành viên khác:
1. **Role CUSTOMER không còn dùng:** TV1 loại bỏ role `CUSTOMER` khỏi enum `Role.java` và `accounts.txt`. Hệ thống chỉ điều hướng 3 vai trò nội bộ: `ADMIN`, `WAREHOUSE`, `SALES`.
2. **Đổi tên class giao diện:** Đổi từ `UserMenu.java` thành `CustomerMenu.java` (đặt trong package `com.smartphonestore.view`). Constructor nhận `(CustomerManager manager, Role currentRole)`.
3. **Cách gọi Menu:** TV1 (Admin) và TV3 (Sales) thêm 1 mục *"Quản lý Khách hàng & Dịch vụ"* vào menu của mình, cả hai cùng gọi: `new CustomerMenu(customerManager, currentRole).displayMenu()`.
4. **Phân hệ Bảo hành (WarrantyClaim):** Thuộc toàn quyền quản lý của TV4 (tiếp nhận máy lỗi tại quầy và cập nhật trạng thái sửa chữa). Menu Kho (TV2) không cần làm bảo hành để tránh trùng lặp trách nhiệm.
5. **Phân quyền Xóa khách hàng:** Trong chức năng `[4]` Xóa khách, kiểm tra `if (currentRole != Role.ADMIN)` thì thông báo từ chối (Chỉ Quản trị viên Admin mới có quyền xóa dữ liệu khách hàng).

#### 5. Câu hỏi vấn đáp mẫu:
- *Thầy/cô hỏi:* "Tại sao ban đầu đề xuất User Portal mà sau đó em lại chuyển sang CustomerMenu quản lý tại quầy?"  
  ➔ *Trả lời:* "Dạ thưa thầy/cô, vì dự án của nhóm là Hệ thống bán hàng Offline tại cửa hàng (POS). Khách hàng không thể tự đăng nhập máy tính nội bộ của nhân viên để thao tác. Do đó em chuyển thành `CustomerMenu` để nhân viên bán hàng và Admin tạo hồ sơ khách tại quầy, tra cứu nâng hạng VIP theo Interface `ILoyaltyRankable`, ghi nhận review và quản lý toàn bộ quy trình bảo hành `WarrantyClaim` có cập nhật trạng thái `updateStatus()` ạ."

---

## 4. Checklist Nghiệm Thu Đồ Án Chuẩn Ba-Rem

- [ ] **Màn hình Đăng nhập:** Hoạt động trơn tru, nạp từ `accounts.txt`.
- [ ] **4 Bảng Menu Độc Lập:**
  - [ ] TV1 phụ trách trọn vẹn **Menu Admin** (Quản trị, Tài khoản, Nhân viên, Lương).
  - [ ] TV2 phụ trách trọn vẹn **Menu Kho** (Sản phẩm, Smartphone, FeaturePhone, Phụ kiện, Tồn kho).
  - [ ] TV3 phụ trách trọn vẹn **Menu Bán hàng** (Hóa đơn, Chi tiết hóa đơn, In phiếu, Thanh toán).
  - [ ] TV4 phụ trách trọn vẹn **Menu Khách hàng / User** (Khách hàng, VIP, Đánh giá sao, Bảo hành).
- [ ] **Đúng số lượng 20 Class + 4 Interface:** Đảm bảo đủ 20 file class thực thể, mỗi bạn đúng 5 class và 1 interface.
- [ ] **CRUD đầy đủ:** Mỗi bạn kiểm thử đủ 4 hàm Thêm, Sửa, Xóa, Tìm kiếm cho các thực thể mình đảm nhiệm.
- [ ] **Đọc/Ghi File UTF-8:** Dữ liệu lưu trong thư mục `data/`, tắt đi bật lại không bị mất mát dữ liệu.