# ĐỀ ÁN THIẾT KẾ LẠI HỆ THỐNG POS BÁN HÀNG OFFLINE TẠI CỬA HÀNG
## PHÂN QUYỀN CHUẨN THỰC TẾ & BẢO TOÀN 100% CẤU TRÚC OOP CỦA 4 THÀNH VIÊN

---

## 1. BỐI CẢNH & PHÂN TÍCH YÊU CẦU THỰC TẾ CỬA HÀNG

### 1.1. Bản chất hệ thống: Phần mềm POS Offline tại quầy
* Đây là ứng dụng máy tính để bàn (Desktop POS) đặt tại các quầy làm việc trong cửa hàng bán lẻ điện thoại.
* **Quy tắc thực tế:** Máy tính này là **tài sản nội bộ của cửa hàng**, chỉ dành cho **nhân viên đăng nhập ca làm việc**. Khách hàng bước vào cửa hàng mua máy hoặc bảo hành sẽ giao dịch trực tiếp với nhân viên, **khách không tự lấy máy tính của quầy để đăng ký tài khoản hay đăng nhập**.
* Do đó:
  * **BỎ HOÀN TOÀN** mục *"Đăng ký tài khoản khách hàng"* ở màn hình khởi động.
  * Màn hình chính chỉ có: **ĐĂNG NHẬP NỘI BỘ (STAFF LOGIN)**.

### 1.2. Phân cấp quyền hạn (RBAC):
1. **ADMIN (Quản trị viên / Cửa hàng trưởng):** 
   * Có **FULL QUYỀN** quản lý toàn bộ hoạt động của cửa hàng.
   * Menu Admin được phân thành các phân hệ lớn rõ ràng:
     * **Mục 1: Quản lý Nhân sự & Tài khoản** (Nhân viên, Tài khoản, Xem thông tin & Bảng lương).
     * **Mục 2: Chăm sóc Khách hàng & Bảo hành** (Hồ sơ khách hàng, Tiếp nhận & Xử lý bảo hành).
     * **Mục 3: Quản lý Kho hàng & Sản phẩm** (Toàn quyền kiểm kho, nhập kho, duyệt giá của TV2).
     * **Mục 4: Quản lý Bán hàng & Doanh thu** (Xem hóa đơn, tổng kết doanh thu bán lẻ của TV3).
2. **NHÂN VIÊN KHO (Warehouse Staff - Thành viên 2):**
   * Đăng nhập vào chỉ thấy đúng **Menu Kho hàng**: quản lý tồn kho, serial/IMEI máy, nhập lô, chiết khấu, tương thích phụ kiện.
   * **Bảo mật:** Không xem được bảng lương nhân viên khác, không can thiệp hóa đơn thu ngân.
3. **NHÂN VIÊN BÁN HÀNG & THU NGÂN (Sales Staff - Thành viên 3):**
   * Đăng nhập vào chỉ thấy đúng **Menu Bán hàng**: lập hóa đơn bán lẻ theo số serial máy, thanh toán (tiền mặt / QR), in bill VAT, xem doanh số ca làm việc.
   * **Bảo mật:** Không tự ý sửa giá gốc sản phẩm trong kho, không xem lương người khác.
4. **NHÂN VIÊN QUẦY CSKH & BẢO HÀNH (Customer Service Staff - Thành viên 4):**
   * Đăng nhập vào quầy tiếp tân/bảo hành: tạo hồ sơ khách mua hàng, tra cứu tích điểm VIP, tiếp nhận máy bảo hành theo serial, theo dõi tiến độ sửa chữa máy.

---

## 2. BẢNG PHÂN BỔ 20 CLASS & 4 INTERFACE (BẢO TOÀN 100% CÔNG SỨC NHÓM)

> [!IMPORTANT]
> Toàn bộ 20 Class thực thể và 4 Interface ban đầu được giữ nguyên vẹn 100%, không bị xóa hay giảm bớt. Mỗi thành viên bảo toàn trọn vẹn điểm số phần OOP của mình:

| Thành viên | Phân hệ nghiệp vụ chuyên trách | Interface đảm nhiệm | 5 Class thực thể đảm nhiệm |
| :--- | :--- | :---: | :--- |
| **Thành viên 1** | **Quản trị Nhân sự & Tài khoản nội bộ** | `ISalaryCalculable` | `Account`, `Employee`, `AdminStaff`, `WarehouseStaff`, `SalesStaff` |
| **Thành viên 2** *(Phát)* | **Quản lý Kho hàng & Danh mục Sản phẩm** | `IDiscountable` | `Product`, `Phone`, `Smartphone`, `Featurephone`, `Accessory` |
| **Thành viên 3** | **Quản lý Bán hàng, Thu ngân & Hóa đơn** | `IPrintable` | `Invoice`, `InvoiceDetail`, `Payment`, `CashPayment`, `ElectronicPayment` |
| **Thành viên 4** | **Quầy Chăm sóc Khách hàng & Dịch vụ Bảo hành** | `ILoyaltyRankable` | `Customer`, `StandardCustomer`, `VipCustomer`, `CustomerReview`, `WarrantyClaim` |

---

## 3. THIẾT KẾ CẤU TRÚC GIAO DIỆN CONSOLE CHI TIẾT (UI MOCKUP)

```
                            [MÀN HÌNH KHỞI ĐỘNG HỆ THỐNG POS]
                                (Chỉ có Nhân viên đăng nhập)
                                             │
               ┌─────────────────────────────┼─────────────────────────────┐
               ▼                             ▼                             ▼
       [VAI TRÒ: ADMIN]            [VAI TRÒ: WAREHOUSE]           [VAI TRÒ: SALES / CSKH]
               │                             │                             │
    MENU TỔNG ADMIN (FULL)           MENU KHO HÀNG (TV2)           MENU THU NGÂN BÁN HÀNG (TV3)
 ├─ 1. Quản lý Nhân sự & TK        ├─ 1. Xem danh mục & tồn kho  ├─ 1. Lập hóa đơn mới (trừ serial)
 ├─ 2. CSKH & Dịch vụ Bảo hành     ├─ 2. Chi tiết số IMEI/Seri   ├─ 2. Thanh toán tiền mặt / QR
 ├─ 3. Quản lý Kho hàng (TV2)      ├─ 3. Lập phiếu nhập kho      ├─ 3. In phiếu hóa đơn chuẩn VAT
 └─ 4. Bán hàng & Doanh thu (TV3)  └─ ...                        └─ 4. Thống kê doanh thu ca
```

---

### MÀN HÌNH 1: ĐĂNG NHẬP NỘI BỘ DÀNH CHO NHÂN VIÊN
*(Bỏ hoàn toàn mục Đăng ký của khách hàng)*

```text
==============================================================
       HỆ THỐNG POS BÁN HÀNG & QUẢN TRỊ CỬA HÀNG ĐIỆN THOẠI
                       (PHẦN MỀM NỘI BỘ)
==============================================================
 [1] Đăng nhập vào ca làm việc
 [0] Thoát ứng dụng
==============================================================
➤ Lựa chọn: 1

--- ĐĂNG NHẬP TÀI KHOẢN NHÂN VIÊN ---
=> Tên đăng nhập: admin (hoặc kho01, banhang01, cskh01)
=> Mật khẩu: ******

[✓] ĐĂNG NHẬP THÀNH CÔNG!
Xin chào: Huỳnh Văn Phát | Vai trò: Quản lý Cửa hàng (Admin)
Lần đăng nhập trước: 16/11/2026 08:45:20
```

---

### MÀN HÌNH 2: MENU TỔNG DÀNH CHO ADMIN (CỬA HÀNG TRƯỞNG - FULL QUYỀN)

Khi tài khoản `ADMIN` đăng nhập, hệ thống hiển thị bảng điều khiển trung tâm với các phân hệ lớn:

```text
==============================================================
                 BẢNG ĐIỀU HÀNH TỔNG QUAN (ADMIN)
==============================================================
 [1] Phân hệ Quản lý Nhân sự & Tài khoản
 [2] Phân hệ Chăm sóc Khách hàng & Bảo hành
 [3] Phân hệ Quản lý Kho hàng & Sản phẩm (Toàn quyền Kho - TV2)
 [4] Phân hệ Quản lý Bán hàng & Doanh thu (Toàn quyền Thu ngân - TV3)

 [9] Đổi mật khẩu tài khoản Admin
 [0] Đăng xuất ca làm việc
==============================================================
➤ Chọn phân hệ (0-4, 9): 
```

---

### CHI TIẾT CÁC SUB-MENU TRONG ADMIN:

#### 📂 PHÂN HỆ 1: QUẢN LÝ NHÂN SỰ & TÀI KHOẢN (MENU 1 CỦA ADMIN)
Đúng theo thiết kế phân nhóm của nhóm:

```text
==============================================================
              PHÂN HỆ QUẢN LÝ NHÂN SỰ & TÀI KHOẢN
==============================================================
 1.1. QUẢN LÝ NHÂN VIÊN:
   [1] Thêm nhân viên mới (Admin / Kho / Bán hàng)
   [2] Chỉnh sửa thông tin nhân viên (Họ tên, SĐT, Lương cơ bản, Phụ cấp)
   [3] Xóa nhân viên theo Mã ID (Có bảo vệ: không tự xóa chính mình)

 1.2. QUẢN LÝ TÀI KHOẢN NỘI BỘ:
   [4] Xem danh sách toàn bộ tài khoản nhân sự
   [5] Cấp tài khoản mới cho nhân viên
   [6] Đổi mật khẩu / Cấp lại mật khẩu nhân viên
   [7] Khóa / Mở khóa tài khoản nhân viên (ACTIVE <-> LOCKED)

 1.3. XEM THÔNG TIN & BÁO CÁO NHÂN SỰ:
   [8] Xem danh sách nhân sự chi tiết & Tìm kiếm theo Tên / Mã ID
   [9] Bảng tính lương tổng hợp tháng này (Đa hình calculateSalary)
   [10] Báo cáo doanh số bán hàng và hoa hồng của nhân viên kinh doanh

   [0] Quay lại Menu Tổng Admin
==============================================================
➤ Chọn chức năng (0-10): 
```

---

#### 📂 PHÂN HỆ 2: CHĂM SÓC KHÁCH HÀNG & BẢO HÀNH (MENU 2 CỦA ADMIN)
Đúng theo thiết kế phân nhóm của nhóm:

```text
==============================================================
          PHÂN HỆ CHĂM SÓC KHÁCH HÀNG & DỊCH VỤ BẢO HÀNH
==============================================================
 2.1. QUẢN LÝ HỒ SƠ KHÁCH HÀNG:
   [1] Xem danh sách toàn bộ khách hàng cửa hàng
   [2] Tìm kiếm khách hàng theo Tên hoặc Số điện thoại
   [3] Xem thông tin chi tiết & Lịch sử tích lũy chi tiêu của khách
   [4] Thêm hồ sơ khách hàng mới
   [5] Chỉnh sửa thông tin liên hệ của khách hàng (SĐT, Email)
   [6] Tra cứu hạng thành viên & Tiến trình tích lũy lên VIP
   [7] Kích hoạt nâng hạng VIP (Hưởng chiết khấu 5% trọn đời)
   [8] Xóa hồ sơ khách hàng (Chỉ Admin có quyền)

 2.2. DỊCH VỤ BẢO HÀNH & HẬU MÃI:
   [9] Tiếp nhận phiếu yêu cầu bảo hành mới (Kiểm tra IMEI máy đã bán)
   [10] Tra cứu thông tin bảo hành & Tiến độ sửa chữa máy theo số Seri
   [11] Cập nhật trạng thái phiếu bảo hành (Đang sửa / Hoàn tất / Từ chối)
   [12] Xem danh sách đánh giá & phản hồi chất lượng từ khách hàng

   [0] Quay lại Menu Tổng Admin
==============================================================
➤ Chọn chức năng (0-12): 
```

---

#### 📂 PHÂN HỆ 3: QUẢN LÝ KHO HÀNG & SẢN PHẨM (MENU 3 CỦA ADMIN)
Khi Admin chọn `[3]`, Admin có toàn quyền truy cập trực tiếp vào toàn bộ chức năng Kho của Thành viên 2 (`WarehouseMenu`):

```text
==============================================================
                 QUẢN LÝ KHO HÀNG & SẢN PHẨM (TV2)
==============================================================
 [1] Xem danh mục toàn bộ sản phẩm & Số lượng tồn kho
 [2] Xem bảng chi tiết từng con máy (Serial/IMEI, Ngày nhập, Giá nhập, NCC)
 [3] Lập phiếu nhập hàng vào kho (Nhập lô hàng / Nhập sản phẩm mới)
 [4] Cập nhật thông tin & Giá bán niêm yết theo Mã ID
 [5] Xóa sản phẩm khỏi kho (Bảo toàn dữ liệu: chặn xóa máy đã bán/bảo hành)
 [6] Tìm kiếm sản phẩm theo Tên / Thương hiệu
 [7] Cảnh báo & Lọc danh sách sản phẩm sắp hết hàng (isLowStock)
 [8] Áp dụng chương trình chiết khấu sản phẩm (IDiscountable)
 [9] Kiểm tra tính năng độc quyền (Thu cũ đổi mới / SOS / Tương thích)
 [0] Quay lại Menu Tổng Admin
==============================================================
➤ Chọn chức năng (0-9): 
```

---

#### 📂 PHÂN HỆ 4: QUẢN LÝ BÁN HÀNG & DOANH THU (MENU 4 CỦA ADMIN)
Khi Admin chọn `[4]`, Admin có thể kiểm tra tình hình kinh doanh của quầy thu ngân (TV3):

```text
==============================================================
              QUẢN LÝ BÁN HÀNG & DOANH THU (TV3)
==============================================================
 [1] Xem danh sách toàn bộ hóa đơn bán lẻ đã lập
 [2] Tra cứu chi tiết từng dòng mặt hàng trong hóa đơn
 [3] Thống kê tổng doanh thu bán hàng đã thanh toán hôm nay
 [4] In lại phiếu hóa đơn thanh toán chuẩn VAT (IPrintable)
 [5] Giám sát lịch sử thanh toán Tiền mặt & Chuyển khoản QR
 [0] Quay lại Menu Tổng Admin
==============================================================
➤ Chọn chức năng (0-5): 
```

---

### MÀN HÌNH 3: MENU KHI NHÂN VIÊN KHO ĐĂNG NHẬP (`WarehouseMenu` - TV2)
Nhân viên kho đăng nhập bằng tài khoản `kho01`: **Chỉ thấy đúng Menu Kho, bảo mật 100%** (không xem được lương, không xem hóa đơn bán hàng):

```text
==============================================================
              MENU QUẢN LÝ KHO HÀNG (THÀNH VIÊN 2)
==============================================================
 [1] Xem danh mục toàn bộ sản phẩm & Tồn kho
 [2] Xem bảng chi tiết từng máy (Số Serial/IMEI & Ngày giờ nhập)
 [3] Lập phiếu nhập hàng vào kho (Nhập lô hàng / Nhập sản phẩm mới)
 [4] Cập nhật thông tin / Giá bán sản phẩm theo Mã ID
 [5] Xóa sản phẩm khỏi danh mục kho (Có kiểm tra bảo toàn máy đã bán)
 [6] Tìm kiếm sản phẩm theo Tên / Thương hiệu
 [7] Cảnh báo & Lọc danh sách hàng sắp hết (isLowStock)
 [8] Áp dụng chương trình chiết khấu sản phẩm (IDiscountable)
 [9] Kiểm tra tính năng độc quyền (Thu cũ đổi mới / SOS / Tương thích)
 [0] Đăng xuất ca làm việc
==============================================================
➤ Chọn chức năng (0-9): 
```

---

### MÀN HÌNH 4: MENU KHI NHÂN VIÊN BÁN HÀNG ĐĂNG NHẬP (`SalesMenu` - TV3)
Nhân viên bán hàng đăng nhập bằng tài khoản `banhang01`: **Chỉ thấy chức năng thu ngân tại quầy**:

```text
==============================================================
             MENU THU NGÂN & BÁN HÀNG (THÀNH VIÊN 3)
==============================================================
 [1] Lập hóa đơn bán lẻ mới (*Tự động trừ kho theo số Serial máy)
 [2] Xem danh sách toàn bộ hóa đơn trong ca làm việc
 [3] Xem chi tiết dòng sản phẩm trong hóa đơn
 [4] Xử lý thanh toán Tiền mặt (CashPayment - Tính tiền thối)
 [5] Xử lý thanh toán Chuyển khoản QR (ElectronicPayment)
 [6] In hóa đơn thanh toán chuẩn VAT (IPrintable)
 [7] Thống kê doanh thu thu ngân trong ngày
 [8] Hủy hóa đơn chưa thanh toán (Hoàn trả serial về kho)
 [0] Đăng xuất ca làm việc
==============================================================
➤ Chọn chức năng (0-8): 
```

---

### MÀN HÌNH 5: MENU QUẦY CHĂM SÓC KHÁCH HÀNG & BẢO HÀNH (TV4)
Nhân viên CSKH/Bảo hành đăng nhập tại quầy tiếp tân:

```text
==============================================================
        MENU CHĂM SÓC KHÁCH HÀNG & BẢO HÀNH (THÀNH VIÊN 4)
==============================================================
 [1] Xem danh sách khách hàng thân thiết
 [2] Tra cứu thông tin khách hàng theo SĐT / Mã khách hàng
 [3] Tạo hồ sơ khách hàng mới
 [4] Chỉnh sửa thông tin liên hệ của khách hàng
 [5] Kiểm tra hạng thành viên & Tiến trình tích lũy lên VIP
 [6] Kích hoạt nâng hạng VIP (Giảm 5% trọn đời)
 [7] Tiếp nhận yêu cầu bảo hành máy mới (Kiểm tra serial đã bán)
 [8] Tra cứu lịch sử & Tiến độ sửa chữa bảo hành theo Serial máy
 [9] Cập nhật trạng thái xử lý bảo hành (Đang sửa / Hoàn tất / Từ chối)
 [10] Ghi nhận đánh giá & phản hồi chất lượng từ khách hàng
 [0] Đăng xuất ca làm việc
==============================================================
➤ Chọn chức năng (0-10): 
```

---

## 4. TỔNG KẾT ƯU ĐIỂM KHI TRÌNH BÀY VẤN ĐÁP VỚI THẦY

1. **Chuẩn 100% thực tế cửa hàng bán lẻ (POS Offline):**
   * Giải quyết triệt để vấn đề thầy chê: Máy tính quầy chỉ dành cho nhân viên làm việc, không có chuyện khách hàng tự mở máy lên bấm đăng ký tài khoản.
2. **Phân quyền Admin mạch lạc, toàn diện:**
   * Đúng theo ý nhóm: Admin chia rõ 2 khối lớn: *Quản lý Nhân sự/Tài khoản* và *Chăm sóc khách hàng/Bảo hành*, đồng thời liên kết trực tiếp vào Kho (TV2) và Thu ngân (TV3).
3. **Phân quyền bảo mật cao (Role Isolation):**
   * Nhân viên Kho không soi được lương, không xem trộm hóa đơn.
   * Nhân viên Bán hàng không đổi được giá gốc sản phẩm.
4. **Bảo toàn 100% điểm OOP của 4 thành viên:**
   * Toàn bộ 20 Class, 4 Interface, cơ chế đọc/ghi 10 file text giữ nguyên vẹn. Khi giảng viên vấn đáp từng bạn, ai cũng có đúng 5 Class + 1 Interface để thuyết minh.
