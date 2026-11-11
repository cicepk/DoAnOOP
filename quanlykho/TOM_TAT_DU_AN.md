# TÓM TẮT DỰ ÁN (PROJECT SUMMARY)
# HỆ THỐNG BÁN HÀNG ĐIỆN THOẠI OFFLINE (SMARTPHONE STORE)
> **Ngôn ngữ:** Java (OOP Console Application)  
> **Quy mô nhóm:** Đúng 4 thành viên  
> **Quy mô kiến trúc:** Đúng 20 Class thực thể + 4 Interface chuẩn (Mỗi người đúng 5 Class + 1 Interface)  
> **Mô hình phân công Menu độc lập:** **4 THÀNH VIÊN = 4 BẢNG MENU RIÊNG BIỆT (Mỗi bạn làm trọn vẹn 1 Menu)**  
> **Luồng vận hành:** Đăng nhập tập trung ➔ Nhận diện Role (ADMIN / WAREHOUSE / SALES) ➔ Chuyển ngay vào Menu tương ứng của từng thành viên.

---

## 1. Mục Tiêu & Kiến Trúc "1 Thành Viên = 1 Menu Độc Lập"

Để phân chia công việc rạch ròi 100%, không bị chồng chéo code và khi thi vấn đáp mỗi thành viên chỉ cần demo trọn vẹn đúng Menu của mình:
- **Khởi động ứng dụng (Entry Point):** Mới vào hiển thị **Màn hình Đăng nhập (Login)**. Hệ thống tra cứu tài khoản trong `data/accounts.txt`, xác định `Role` trong 3 vai trò nội bộ của cửa hàng (bỏ role `CUSTOMER` vì đây là hệ thống POS tại quầy):
  - `Role = ADMIN` ➔ Mở **Menu ADMIN** (Do **Thành viên 1** code 100% - kiêm quyền điều hướng sang CustomerMenu và phân quyền xóa khách).
  - `Role = WAREHOUSE` ➔ Mở **Menu KHO HÀNG** (Do **Thành viên 2** code 100%).
  - `Role = SALES` ➔ Mở **Menu BÁN HÀNG** (Do **Thành viên 3** code 100% - kiêm quyền điều hướng sang CustomerMenu phục vụ khách tại quầy).
  *(Thành viên 4 chịu trách nhiệm 100% cho **MENU KHÁCH HÀNG & DỊCH VỤ (CustomerMenu)**, được Admin và Bán hàng gọi ra trực tiếp tại quầy POS).*
- **Liên kết giữa các thành viên:** Menu của mỗi bạn chỉ tập trung gọi các class do chính bạn đó viết. Nếu cần liên kết (ví dụ: Bán hàng cần trừ tồn kho), chỉ gọi đúng **1 hàm kết quả duy nhất** từ Manager của bạn khác (ví dụ `product.decreaseStock()`).

---

## 2. Bảng Phân Bổ Chuẩn: 4 Thành Viên = 20 Class = 4 Interface = 4 Menu

| Thành viên | Phân hệ nghiệp vụ | 1 Interface chuẩn | 5 Class thực thể đảm nhiệm (Mỗi class có CRUD + 1 thuộc tính riêng + 1 hàm riêng) | 1 Menu chuyên trách (100% tự làm) |
| :--- | :--- | :--- | :--- | :--- |
| **👤 THÀNH VIÊN 1** | **Quản trị Hệ thống & Nhân sự** | `ISalaryCalculable` | 1. `Account`<br>2. `Employee`<br>3. `AdminStaff`<br>4. `WarehouseStaff`<br>5. `SalesStaff` | **MENU ADMIN**<br>*(Kèm Màn hình Login/Register)* |
| **👤 THÀNH VIÊN 2** | **Quản lý Kho & Danh mục Sản phẩm** | `IDiscountable` | 6. `Product`<br>7. `Phone`<br>8. `Smartphone`<br>9. `FeaturePhone`<br>10. `Accessory` | **MENU KHO HÀNG** |
| **👤 THÀNH VIÊN 3** | **Quản lý Bán hàng & Hóa đơn** | `IPrintable` | 11. `Invoice`<br>12. `InvoiceDetail`<br>13. `Payment`<br>14. `CashPayment`<br>15. `ElectronicPayment` | **MENU BÁN HÀNG** |
| **👤 THÀNH VIÊN 4** | **Quản lý Khách hàng & Dịch vụ Hậu mãi** | `ILoyaltyRankable` | 16. `Customer`<br>17. `StandardCustomer`<br>18. `VipCustomer`<br>19. `CustomerReview`<br>20. `WarrantyClaim` | **MENU KHÁCH HÀNG & DỊCH VỤ**<br>*(CustomerMenu)* |

---

## 3. Sơ Đồ Cây Kiến Trúc Tổng Thể

```text
========================================================================================================================
                          HỆ THỐNG BÁN HÀNG ĐIỆN THOẠI OFFLINE (20 CLASS + 4 INTERFACE)
========================================================================================================================
│
├── 👤 [THÀNH VIÊN 1] MENU ADMIN (QUẢN TRỊ VIÊN & NHÂN SỰ)
│   ├── <<Interface>> ISalaryCalculable ────────────────────────────► Hàm: calculateSalary()
│   ├── Account [File: accounts.txt] ────────────────────────────────► Thuộc tính riêng: lastLoginTime
│   └── (Lớp Cha) Employee [File: employees.txt] ───────────────────► Thuộc tính riêng: hireYear
│       ├── AdminStaff (kế thừa Employee) ──────────────────────────► Thuộc tính riêng: managementBonus
│       ├── WarehouseStaff (kế thừa Employee) ──────────────────────► Thuộc tính riêng: warehouseZone
│       └── SalesStaff (kế thừa Employee) ──────────────────────────► Thuộc tính riêng: commissionRate
│
├── 👤 [THÀNH VIÊN 2] MENU KHO HÀNG (QUẢN LÝ SẢN PHẨM & TỒN KHO)
│   ├── <<Interface>> IDiscountable ────────────────────────────────► Hàm: calculateDiscountedPrice()
│   └── (Lớp Cha) Product [File: products.txt] ─────────────────────► Thuộc tính riêng: minStockThreshold
│       ├── (Lớp Con) Phone (kế thừa Product) ──────────────────────► Thuộc tính riêng: operatingSystem
│       │   ├── Smartphone (kế thừa Phone) ─────────────────────────► Thuộc tính riêng: storageCapacity & applyTradeInDiscount()
│       │   └── FeaturePhone (kế thừa Phone) ───────────────────────► Thuộc tính riêng: batteryLifeDays & toggleSosMode()
│       └── (Lớp Con) Accessory (kế thừa Product) ──────────────────► Thuộc tính riêng: accessoryType
│
├── 👤 [THÀNH VIÊN 3] MENU BÁN HÀNG (HÓA ĐƠN & GIAO DỊCH THANH TOÁN)
│   ├── <<Interface>> IPrintable ───────────────────────────────────► Hàm: printReceipt()
│   ├── Invoice [File: invoices.txt] ───────────────────────────────► Thuộc tính riêng: vatTaxRate
│   ├── InvoiceDetail [File: invoice_details.txt] ──────────────────► Thuộc tính riêng: quantity
│   └── (Lớp Cha) Payment [File: payments.txt] ─────────────────────► Thuộc tính riêng: transactionCode
│       ├── CashPayment (kế thừa Payment) ──────────────────────────► Thuộc tính riêng: cashGiven & calculateChange()
│       └── ElectronicPayment (kế thừa Payment) ────────────────────► Thuộc tính riêng: bankOrWalletName & generatePaymentQRCode()
│
└── 👤 [THÀNH VIÊN 4] MENU KHÁCH HÀNG & DỊCH VỤ (CUSTOMER & SERVICE MANAGEMENT)
    ├── <<Interface>> ILoyaltyRankable ─────────────────────────────► Hàm: evaluateRank()
    ├── (Lớp Cha) Customer [File: customers.txt] ───────────────────► Thuộc tính riêng: totalSpent & accumulateSpending()
    │   ├── StandardCustomer (kế thừa Customer) ────────────────────► Thuộc tính riêng: visitCount & upgradeToVip()
    │   └── VipCustomer (kế thừa Customer) ─────────────────────────► Thuộc tính riêng: vipDiscountRate & calculateVipDiscount()
    ├── CustomerReview [File: reviews.txt] ─────────────────────────► Thuộc tính riêng: ratingStars & isPositiveFeedback()
    └── WarrantyClaim [File: warranty_claims.txt] ──────────────────► Thuộc tính riêng: claimStatus & updateStatus()
========================================================================================================================
```

---

## 4. Chi Tiết 4 Bảng Menu - Mỗi Thành Viên Tự Phụ Trách 1 Menu

### 4.1. Màn Hình Đăng Nhập & Đăng Ký (Do Thành Viên 1 Phụ Trách)
- Nạp danh sách từ `data/accounts.txt`.
- Cho phép: `[1] Đăng nhập`, `[2] Đăng ký Tài khoản nhân viên mới`, `[0] Thoát`.
- Có sẵn 3 tài khoản mẫu nội bộ tương ứng 3 vai trò:
  - `admin` / `admin123` ➔ Role: **ADMIN** ➔ Mở Menu của **Thành viên 1** (Toàn quyền hệ thống; kiêm mục mở `CustomerMenu` của TV4; có quyền Xóa khách hàng).
  - `kho01` / `kho123` ➔ Role: **WAREHOUSE** ➔ Mở Menu của **Thành viên 2** (Quản lý kho, sản phẩm, giá bán, chiết khấu).
  - `banhang01` / `sales123` ➔ Role: **SALES** ➔ Mở Menu của **Thành viên 3** (Lập hóa đơn, thanh toán; kiêm mục mở `CustomerMenu` của TV4 phục vụ khách tại quầy).
  *(Role CUSTOMER đã được loại bỏ khỏi hệ thống đăng nhập do mô hình cửa hàng Offline POS).*

---

### 4.2. MENU 1: QUẢN TRỊ VIÊN (Do THÀNH VIÊN 1 Tự Code 100% - Đã Rút Gọn Tinh Giản)
Toàn bộ chức năng quản lý tài khoản, nhân sự cửa hàng, và tính lương tháng:
```text
==============================================================
             HỆ THỐNG QUẢN LÝ CỬA HÀNG ĐIỆN THOẠI
             MENU DÀNH CHO ADMIN (THÀNH VIÊN 1)
==============================================================
 [1] Xem danh sách toàn bộ tài khoản hệ thống (Account)
 [2] Đổi mật khẩu & Khóa / Mở khóa tài khoản (Account)
 [3] Xem danh sách toàn bộ nhân sự cửa hàng (Employee)
 [4] Thêm nhân viên mới (AdminStaff / WarehouseStaff / SalesStaff)
 [5] Cập nhật thông tin lương cơ bản & chức vụ theo Mã ID
 [6] Xóa nhân viên khỏi hệ thống theo Mã ID
 [7] Tìm kiếm nhân viên theo Tên hoặc Mã ID
 [8] Bảng tính lương chi tiết nhân viên tháng này (ISalaryCalculable)
 [9] Đăng xuất (Quay lại màn hình Đăng nhập)
 [0] Thoát toàn bộ chương trình
==============================================================
```

---

### 4.3. MENU 2: NHÂN VIÊN KHO (Do THÀNH VIÊN 2 Tự Code 100%)
Toàn bộ chức năng quản lý danh mục sản phẩm, bảng chi tiết từng con máy (Seri/IMEI), nhập hàng theo lô và tồn kho:
```text
==============================================================
             HỆ THỐNG QUẢN LÝ CỬA HÀNG ĐIỆN THOẠI
             MENU NHÂN VIÊN KHO (THÀNH VIÊN 2)
==============================================================
 [1] Xem danh mục toàn bộ sản phẩm & Số lượng tồn kho (Product)
 [2] Xem bảng chi tiết từng con máy (Serial/IMEI & Ngày nhập theo Hãng)
 [3] Lập phiếu nhập hàng vào kho (Nhập lô / Nhập mới - Vòng lặp while)
 [4] Cập nhật thông tin / Giá bán sản phẩm theo Mã ID (Update)
 [5] Xóa sản phẩm khỏi danh mục kho theo Mã ID (Delete)
 [6] Tìm kiếm sản phẩm theo Tên / Thương hiệu (Search)
 [7] Cảnh báo & Lọc danh sách sản phẩm sắp hết hàng (isLowStock)
 [8] Áp dụng chương trình giảm giá chiết khấu sản phẩm (IDiscountable)
 [9] Kiểm tra tính năng độc quyền (Thu cũ đổi mới / SOS / Tương thích)
 [0] Thoát toàn bộ chương trình (Tự động lưu dữ liệu)
==============================================================
```

---

### 4.4. MENU 3: NHÂN VIÊN BÁN HÀNG (Do THÀNH VIÊN 3 Tự Code 100%)
Toàn bộ chức năng lập hóa đơn, chi tiết đơn hàng, in phiếu tính tiền và thanh toán:
```text
==============================================================
             HỆ THỐNG QUẢN LÝ CỬA HÀNG ĐIỆN THOẠI
            MENU NHÂN VIÊN BÁN HÀNG (THÀNH VIÊN 3)
==============================================================
 [1] Xem danh sách toàn bộ hóa đơn đã lập (Invoice)
 [2] Lập Hóa đơn bán lẻ mới (Tạo Invoice & các InvoiceDetail)
     (*Gọi 1 lệnh trừ kho product.decreaseStock() của TV2)
 [3] Xem chi tiết các dòng sản phẩm trong hóa đơn (InvoiceDetail)
 [4] In phiếu hóa đơn thanh toán chuẩn VAT ra màn hình (IPrintable)
 [5] Xử lý Thanh toán Tiền mặt (CashPayment - Tự tính tiền thối)
 [6] Xử lý Thanh toán Chuyển khoản QR (ElectronicPayment - Tạo mã QR)
 [7] Tìm kiếm hóa đơn theo Mã ID (Search Invoice)
 [8] Hủy hóa đơn bán hàng theo Mã ID (Cancel/Delete Invoice)
 [9] Thống kê tổng doanh thu bán hàng trong ngày
 [10] Đăng xuất (Quay lại màn hình Đăng nhập)
 [0] Thoát toàn bộ chương trình
==============================================================
```

---

### 4.5. MENU 4: MENU KHÁCH HÀNG & DỊCH VỤ HẬU MÃI (Do THÀNH VIÊN 4 Tự Code 100%)
Dành cho nhân viên cửa hàng (Sales / Admin) thao tác tiếp đón khách hàng và dịch vụ tại quầy (POS):
```text
==============================================================
         HỆ THỐNG QUẢN LÝ CỬA HÀNG ĐIỆN THOẠI
     MENU KHÁCH HÀNG & DỊCH VỤ (THÀNH VIÊN 4 PHỤ TRÁCH)
==============================================================
 [1] Xem danh sách toàn bộ khách hàng & Chi tiết theo Mã ID (Customer)
 [2] Thêm khách hàng mới tại quầy (Create: StandardCustomer)
 [3] Cập nhật thông tin SĐT, Email theo Mã ID (Update Customer)
 [4] Xóa khách hàng khỏi hệ thống (Delete: Phân quyền chỉ ADMIN)
 [5] Tìm kiếm khách hàng theo Tên hoặc Số điện thoại (Search Customer)
 [6] Tra cứu & Tự động đánh giá xếp hạng thành viên (ILoyaltyRankable)
 [7] Nâng khách lên VIP (upgradeToVip) & Xem ưu đãi (calculateVipDiscount)
 [8] Tiếp nhận & Ghi nhận đánh giá phản hồi tại quầy (CustomerReview)
 [9] Xem lịch sử đánh giá phản hồi (lọc theo Mã KH / Sản phẩm)
 [10] Tiếp nhận máy lỗi & Tạo phiếu yêu cầu bảo hành (WarrantyClaim)
 [11] Tra cứu & Cập nhật tiến trình/trạng thái bảo hành (updateStatus)
 [0] Quay lại Menu trước (Admin / Sales)
==============================================================
```

---

## 5. Danh Mục File Dữ Liệu Lưu Trữ (Data Storage)

| Tên File | Thành viên phụ trách | Mô tả dữ liệu |
| :--- | :--- | :--- |
| `accounts.txt` | Thành viên 1 | Danh sách tài khoản đăng nhập nội bộ (Admin, Kho, Bán hàng) |
| `employees.txt` | Thành viên 1 | Danh sách nhân viên cửa hàng |
| `products.txt` | Thành viên 2 | Danh sách dòng sản phẩm (Smartphone, FeaturePhone, Accessory) |
| `product_details.txt` | Thành viên 2 | Bảng chi tiết từng con máy (Mã SP, Số Seri/IMEI, Ngày nhập, Giá nhập, Trạng thái) |
| `invoices.txt` | Thành viên 3 | Thông tin chung hóa đơn bán |
| `invoice_details.txt` | Thành viên 3 | Chi tiết các dòng mặt hàng trong hóa đơn |
| `payments.txt` | Thành viên 3 | Nhật ký thanh toán tiền mặt & chuyển khoản QR |
| `customers.txt` | Thành viên 4 | Thông tin khách hàng (StandardCustomer & VipCustomer) |
| `reviews.txt` | Thành viên 4 | Đánh giá nhận xét & chấm sao của khách tại quầy |
| `warranty_claims.txt` | Thành viên 4 | Phiếu tiếp nhận và trạng thái bảo hành máy |
