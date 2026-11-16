# BÁO CÁO CẬP NHẬT & ĐỒNG BỘ MODULE KHÁCH HÀNG & BẢO HÀNH (TV4)
# HỆ THỐNG SMARTPHONE STORE (OFFLINE POS)

---

## 1. Chuyển Đổi Menu: UserMenu (Cũ) ➔ CustomerMenu (Mới)

Vì hệ thống vận hành theo mô hình bán lẻ tại quầy (POS), khách hàng không trực tiếp đăng nhập vào máy tính của cửa hàng. Phân hệ của TV4 được thiết kế lại thành **Menu Quản lý Khách hàng & Dịch vụ hậu mãi** đặt tại quầy thu ngân:

| Chức năng Menu cũ | Quyết định & Hướng xử lý mới |
| :--- | :--- |
| `[1] Xem thông tin cá nhân` | **Đổi**: Xem danh sách toàn bộ khách hàng và xem chi tiết một khách hàng theo Mã ID (`CUSxx`). |
| `[2] Cập nhật SĐT, Email` | **Giữ**: Nhân viên nhập ID khách hàng cần cập nhật rồi sửa thông tin trực tiếp. |
| `[3] Tra cứu hạng thành viên` | **Giữ**: Nhập ID khách và gọi hàm đa hình `evaluateRank()` từ interface `ILoyaltyRankable`. |
| `[4] Xem đặc quyền VIP` | **Giữ**: Áp dụng khi thanh toán để tính chiết khấu `calculateVipDiscount()` cho khách VIP. |
| `[5] Gửi đánh giá phản hồi` | **Giữ**: Nhân viên ghi nhận review đánh giá từ khách tại quầy (`CustomerReview`). |
| `[6] Lịch sử đánh giá` | **Giữ**: Lọc lịch sử review theo Mã khách hàng (`customerId`) hoặc Mã sản phẩm (`productId`). |
| `[7] Gửi yêu cầu bảo hành` | **Nâng cấp**: Tiếp nhận máy lỗi, kiểm tra số Serial/IMEI đã bán và tự động tạo phiếu `WarrantyClaim`. |
| `[8] Tra cứu tiến trình BH` | **Nâng cấp**: Tra cứu theo mã phiếu hoặc serial, cập nhật trạng thái xử lý (`updateStatus`). |
| `[9] Đăng xuất` | **Đổi**: "Quay lại Menu trước" (Admin / Sales). |

---

## 2. Bổ Sung Các Chức Năng Bắt Buộc (CRUD Chuẩn)
- **Thêm khách hàng mới tại quầy:** Mặc định tạo đối tượng `StandardCustomer` (`visitCount = 1`, `totalSpent = 0`).
- **Tìm kiếm khách hàng:** Tìm linh hoạt theo Tên khách hàng hoặc Số điện thoại.
- **Thăng hạng VIP:** Khi tổng chi tiêu `totalSpent >= 15.000.000đ`, gọi hàm `upgradeToVip()` chuyển thành `VipCustomer` với tỷ lệ giảm giá 5% (`vipDiscountRate = 0.05`).
- **Phân quyền xóa khách hàng:** Chỉ tài khoản có vai trò `Role.ADMIN` mới được phép xóa hồ sơ khách hàng.

---

## 3. Cấu Trúc File Lưu Mới Của `warranty_claims.txt` (9 Cột Chuẩn)

File [data/warranty_claims.txt](file:///c:/SmartphoneStoreDocs/data/warranty_claims.txt) được chuẩn hóa theo đúng cấu trúc lưu mới:
```text
# Format: id|mã khách|mã sản phẩm|seri|lỗi|ngày giờ nhận|trạng thái|ngày bán|số tháng BH
```

### Chi tiết các cột:
1. `id`: Mã phiếu bảo hành (`CLM01`, `CLM02`, ... sinh tự động theo `%02d`).
2. `mã khách`: Mã khách hàng trong hệ thống (`CUS01`, `CUS02`, ...).
3. `mã sản phẩm`: Mã dòng sản phẩm (`PRD01`, `PRD02`, `PRD38`, ...).
4. `seri`: Số Serial / IMEI duy nhất của thiết bị vật lý mang đi bảo hành.
5. `lỗi`: Mô tả chi tiết tình trạng hư hỏng của máy.
6. `ngày giờ nhận`: Thời điểm nhân viên lập phiếu tiếp nhận (`dd/MM/yyyy HH:mm:ss`).
7. `trạng thái`: Thuộc 1 trong 4 trạng thái:
   - `TIEP_NHAN`: Mới tiếp nhận máy, đang chờ kỹ thuật viên kiểm tra.
   - `DANG_SUA`: Máy đang trong quá trình thay thế linh kiện / sửa chữa.
   - `HOAN_TAT`: Đã sửa xong hoặc bảo dưỡng xong, đã bàn giao lại cho khách.
   - `TU_CHOI`: Lỗi do người dùng làm rơi vỡ/vào nước, từ chối bảo hành miễn phí.
8. `ngày bán`: Ngày lập hóa đơn mua máy (`dd/MM/yyyy` hoặc `dd/MM/yyyy HH:mm:ss`, tra cứu từ hóa đơn của TV3).
9. `số tháng BH`: Thời hạn bảo hành tính theo tháng:
   - Điện thoại Android (Samsung, Xiaomi, OPPO): `18` tháng.
   - Điện thoại iOS (iPhone): `12` tháng.
   - Phụ kiện (Củ sạc, tai nghe): `6` tháng.

---

## 4. Cơ Chế Phối Hợp & Đồng Bộ Chéo Với Các Thành Viên Khác

### 4.1. Phối hợp với Thành viên 2 (Quản lý Kho - ProductManager)
- **Đổi trạng thái Serial trong kho:**
  - Khi tiếp nhận máy bảo hành (`TIEP_NHAN`, `DANG_SUA`), TV4 gọi:
    ```java
    productManager.setWarrantyStatus(serialNumber, true); // Chuyển SOLD -> WARRANTY
    ```
  - Khi đóng phiếu bảo hành (`HOAN_TAT`, `TU_CHOI`) và trả máy lại cho khách, TV4 gọi:
    ```java
    productManager.setWarrantyStatus(serialNumber, false); // Chuyển WARRANTY -> SOLD
    ```
- **Lấy số tháng bảo hành đa hình:**
  - Nhờ TV2 đã đưa phương thức trừu tượng `public abstract int getWarrantyMonths();` lên lớp cha `Product`, TV4 chỉ cần gọi:
    ```java
    Product p = productManager.findByID(productId);
    int months = p.getWarrantyMonths(); // Áp dụng cho cả Phone và Accessory (6 tháng)!
    ```

### 4.2. Phối hợp với Thành viên 3 (Quản lý Bán hàng - SalesManager)
- Khi tiếp nhận máy bảo hành theo số Serial, TV4 tra cứu thông tin hóa đơn bán hợp lệ từ `SalesManager`:
  ```java
  String saleDate = salesManager.getSaleDateBySerial(serialNumber);
  String buyerId = salesManager.getBuyerIdBySerial(serialNumber);
  ```
  *(Chỉ những serial nằm trong hóa đơn có trạng thái `PAID` mới đủ điều kiện bảo hành).*

### 4.3. Phối hợp với Thành viên 1 (Quản trị Admin & Login)
- Loại bỏ vai trò `Role.CUSTOMER` khỏi enum `Role.java` và `accounts.txt`.
- TV1 thêm mục `[Quản lý Khách hàng & Dịch vụ]` vào `AdminMenu`, gọi `new CustomerMenu(customerManager, currentRole).displayMenu()`.
- TV3 cũng gọi tương tự từ `SalesMenu`.
