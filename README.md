# PHẦN CỦA TV4: KHÁCH HÀNG, ĐÁNH GIÁ, BẢO HÀNH

Java 9 trở lên (có dùng `List.of` trong `WarrantyClaim`).

## 1. Thư mục

```
src/                                           <- PHẦN NỘP của TV4 (chép vào dự án chung)
  com/smartphonestore/model/customer/          ILoyaltyRankable, Customer, StandardCustomer, VipCustomer, CustomerReview, WarrantyClaim
  com/smartphonestore/manager/                 CustomerManager
  com/smartphonestore/view/                    CustomerManagementMenu (Admin + Bán hàng), CustomerMenu (khách)
  com/smartphonestore/util/                    DateUtil (định dạng ngày DÙNG CHUNG cả nhóm)
```

`TestCustomer.java` và `ManagerChecks.java` chỉ để thử riêng, KHÔNG nộp.

## 2. Hai menu

| Menu | Ai dùng | Cách gọi |
| :--- | :--- | :--- |
| `CustomerManagementMenu` | ADMIN, SALES (role khác bị từ chối) | `new CustomerManagementMenu(customerManager, accountManager, invoiceManager, productManager, role).show();` rồi quay lại menu trước |
| `CustomerMenu` | khách đăng nhập (role CUSTOMER) | `boolean dangXuat = new CustomerMenu(customerManager, account.getLinkedId(), productManager).show();` true = Đăng xuất, false = Thoát chương trình |

- Cả nhóm chỉ tạo MỘT `CustomerManager` trong `Main` rồi truyền xuống. Mỗi manager giữ dữ liệu trong bộ nhớ, tạo hai bản sẽ ghi đè file của nhau.
- Menu khách không bao giờ hỏi mã khách. Mã lấy từ tài khoản đăng nhập (`linkedId`) nên khách chỉ thấy dữ liệu của mình.
- Chỉ ADMIN được xóa khách và xóa phiếu bảo hành. Quyền này kiểm tra ở menu.

**Menu Quản lý khách hàng (Admin, Bán hàng):**

| Mục | Chức năng |
| :--- | :--- |
| [1] | Xem danh sách khách hàng |
| [2] | Xem chi tiết một khách theo mã |
| [3] | Thêm khách hàng mới (hỏi có cấp tài khoản ngay không) |
| [4] | Xóa khách hàng (chỉ Admin) |
| [5] | Tìm kiếm khách theo tên / SĐT |
| [6] | Tra cứu hạng và tiến trình lên VIP |
| [7] | Tính ưu đãi VIP cho một đơn hàng |
| [8] | Nâng khách lên VIP |
| [9] | Cấp tài khoản đăng nhập cho khách |
| [10] | Xem đánh giá (theo khách hoặc theo sản phẩm) |
| [11] | Tiếp nhận bảo hành (chỉ nhập số seri) |
| [12] | Tra cứu và xử lý phiếu bảo hành (xem, tra, cập nhật trạng thái, xóa phiếu đã đóng) |
| [0] | Quay lại menu trước |

Nhân viên KHÔNG sửa được thông tin khách. Chỉ khách tự sửa SĐT và email của mình ở menu Khách hàng.

**Menu Khách hàng:** [1] Hồ sơ và chi tiêu, [2] Cập nhật SĐT / email, [3] Hạng và tiến trình lên VIP, [4] Đặc quyền VIP, [5] Gửi đánh giá, [6] Đánh giá của tôi (xem, sửa, xóa), [7] Phiếu bảo hành của tôi, [8] Đăng xuất, [0] Thoát chương trình.

## 3. Hàm của TV1/TV2/TV3 mà phần TV4 gọi (hợp đồng)

| Người | Hàm / việc | Trạng thái | Ý nghĩa |
| :--- | :--- | :--- | :--- |
| TV1 | `Role.CUSTOMER` | đã có | role của tài khoản khách |
| TV1 | `Account.linkedId` | đã có | cột cuối của `accounts.txt`: mã nhân viên `EMPxx` hoặc mã khách `CUSxx` |
| TV1 | `AccountManager.createAccount(username, password, Role.CUSTOMER, customerId)` trả `String` | đã có | `null` là tạo thành công, khác `null` là lý do lỗi. TV4 in lý do này ra cho nhân viên |
| TV1 | `AccountManager.lockByLinkedId(customerId)` | đã có | khóa tài khoản của khách khi Admin xóa hồ sơ khách |
| TV1 | `AccountManager.removeByLinkedId(linkedId)` | ĐỀ XUẤT, chưa có | xóa hẳn tài khoản. Cần vì mã khách có thể được cấp lại (xóa `CUS04` rồi thêm khách mới thì lại là `CUS04`): khi đó `createAccount` báo "đã có tài khoản", và nếu Admin mở khóa tài khoản cũ thì người cũ đăng nhập vào hồ sơ của khách mới |
| TV1 | `LoginView` | cần làm | role `CUSTOMER` chuyển sang `CustomerMenu` (xem cách gọi ở mục 2) |
| TV1 | `AdminMenu` | cần làm | thêm mục "Quản lý khách hàng" gọi `CustomerManagementMenu` |
| TV1 | `Employee.isValidPhone` | kiểm tra lại | regex là `"0//d{9}"`, phải là `"0\\d{9}"`, nếu không không SĐT nào hợp lệ |
| TV2 | `ProductManager.findByID(id)` | đã có | khách chỉ đánh giá sản phẩm có thật. Lưu mã đã chuẩn hóa `p.getId()` |
| TV2 | `ProductManager.findDetailBySerial(serial)` | đã có | từ seri lấy `ProductDetail` (mã sản phẩm, trạng thái) |
| TV2 | `Phone.getWarrantyMonths()` | đã có | số tháng bảo hành (iOS hoặc Apple 12, Android 18, còn lại 6). Phụ kiện không phải `Phone` nên không có bảo hành |
| TV3 | `InvoiceManager.getBuyerIdBySerial(serial)` | CẦN VIẾT (tên tạm) | mã khách mua seri. `null` nếu chưa bán hoặc hóa đơn `CANCELLED` |
| TV3 | `InvoiceManager.getSaleDateBySerial(serial)` | CẦN VIẾT (tên tạm) | ngày bán = `creationDate` của hóa đơn, dạng `dd/MM/yyyy` (có kèm giờ cũng đọc được) |
| TV3 | `invoice_details` thêm cột `serialNumber` | ĐỀ XUẤT | hiện không có cột nào nối seri với hóa đơn. Đề xuất mỗi seri một dòng (`quantity = 1`), phụ kiện để trống. Khi bán seri chuyển sang `SOLD`, khi hủy hóa đơn trả về `IN_STOCK` |
| TV3 | gọi `customerManager.recordPurchase(customerId, amount)` | cần làm | sau khi thanh toán xong |
| TV3 | gọi `customerManager.refundPurchase(customerId, amount)` | cần làm | khi hủy hóa đơn đã ghi nhận |
| TV3 | gọi `customerManager.getVipDiscount(customerId, orderTotal)` | cần làm | số tiền giảm khi tính tiền (khách thường trả 0). Hóa đơn phải có `customerId` hợp lệ |
| TV3 | `SalesMenu` | cần làm | thêm mục "Quản lý khách hàng" gọi `CustomerManagementMenu` |

Mọi lỗi nghiệp vụ trong phần TV4 ném `IllegalArgumentException` kèm thông báo tiếng Việt, và menu tự bắt để in ra.

## 4. Quy tắc nghiệp vụ đã cài

- Khách mới do nhân viên tạo, luôn là STANDARD. Tài khoản đăng nhập do nhân viên cấp (lúc thêm khách hoặc mục [9]).
- SĐT: 10 chữ số, bắt đầu bằng 0, không được trùng khách khác. Đổi quy tắc tại hằng `Customer.PHONE_REGEX`.
- Chỉ khách tự sửa SĐT và email của mình. Nhân viên không sửa thông tin khách.
- Nâng VIP: chỉ khi `totalSpent >= 15.000.000`. Tạo `VipCustomer` mới, giữ nguyên mã, thông tin và tổng chi tiêu (số lần mua `visitCount` chỉ có ở STANDARD nên không mang sang).
- Xóa khách: chỉ Admin, và chỉ khi chưa mua hàng (`totalSpent == 0`) và chưa có phiếu bảo hành. Đánh giá của khách bị xóa theo, tài khoản liên kết bị khóa.
- Khách chỉ sửa và xóa được đánh giá của chính mình, và chỉ đánh giá sản phẩm có trong danh mục.
- Bảo hành: nhân viên chỉ nhập SỐ SERI. Hệ thống lấy mã sản phẩm và số tháng bảo hành (TV2), người mua và ngày bán (TV3). Hạn bảo hành = ngày bán + số tháng (đúng ngày cuối hạn vẫn nhận). Từ chối khi: seri không có trong kho, seri chưa bán hoặc hóa đơn hủy, sản phẩm không có bảo hành, hết hạn, hoặc seri đang có phiếu chưa đóng.
- Khách xem phiếu: "Dự kiến hoàn thành" = ngày tiếp nhận + 5 ngày (hằng `WarrantyClaim.PROCESSING_DAYS`). Phiếu đã đóng hiện "Đã hoàn tất" hoặc "Yêu cầu bị từ chối".
- Chỉ Admin được xóa phiếu bảo hành, và chỉ phiếu đã đóng (Hoàn tất hoặc Từ chối).

## 5. Định dạng file (theo cấu trúc chung của nhóm, quy chuẩn `|`, UTF-8)

Mã dùng hai chữ số: `CUS01`, `REV01`, `CLM01`. Ngày giờ dạng `dd/MM/yyyy HH:mm:ss` (dùng `DateUtil.nowText()`).

- `data/customers.txt` (7 cột): `id|tên|sđt|email|tổng chi tiêu|loại|cột riêng`. Cột riêng là số lần mua với STANDARD, tỷ lệ giảm với VIP.
  ```
  CUS01|Hoàng Văn Khách|0981112233|hoangkhach@gmail.com|4500000|STANDARD|2
  CUS03|Lê Văn Cường|0923456789|cuong@gmail.com|30000000|VIP|0.05
  ```
- `data/reviews.txt` (6 cột): `id|mã khách|mã sản phẩm|số sao|nhận xét|ngày giờ`
  ```
  REV01|CUS03|PRD01|5|Máy dùng rất mượt, pin trâu, nhân viên tư vấn nhiệt tình!|19/10/2026 10:00:00
  ```
- `data/warranty_claims.txt` (9 cột): 6 cột đầu theo nhóm, thêm 3 cột cuối để lưu seri: `id|mã khách|mã sản phẩm|seri|lỗi|ngày giờ nhận|trạng thái|ngày bán|số tháng BH`
  ```
  CLM01|CUS02|PRD03|SN002|Màn hình chớp tắt khi cắm sạc|18/10/2026 14:00:00|DANG_SUA|03/08/2026|12
  ```
- `data/accounts.txt` (của TV1): tài khoản khách có role `CUSTOMER` và cột cuối là mã khách, ví dụ `...|CUSTOMER|ACTIVE||CUS02`.

Dữ liệu thử cũ (mã `KH001`, `DG001`, `BH001`, phiếu bảo hành 6 hoặc 9 cột kiểu cũ) sẽ báo lỗi và bị bỏ qua khi nạp, rồi mất ở lần lưu sau. Hãy xóa 3 file dữ liệu cũ của TV4 trước khi chạy.

## 6. Cần sửa trong tài liệu nhóm

- `QUY_CHUAN_CODE.md`: thêm `CustomerManagementMenu`, `CustomerMenu`, `DateUtil` vào cấu trúc. Mục phân quyền: bảo hành do Admin và Sales xử lý (không phải WAREHOUSE), khách chỉ xem dữ liệu của chính mình.
- `PHAN_CONG_CONG_VIEC.md`: TV4 phụ trách 2 menu. Cập nhật câu "1 thành viên = 1 menu", checklist, câu vấn đáp mẫu của TV4, và mô tả `WarrantyClaim` (thêm seri, ngày bán, số tháng bảo hành).
- Nhóm đã có thêm `ProductDetail` và `ValidationException`, nên vượt 20 class. Cần hỏi thầy cô xem có chấp nhận không.
