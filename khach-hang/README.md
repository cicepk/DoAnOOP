# PHẦN CỦA TV4: KHÁCH HÀNG, ĐÁNH GIÁ, BẢO HÀNH

#### 1. Thư mục

src/    <- PHẦN NỘP của TV4 (chép vào dự án chung)
  com/smartphonestore/model/customer/   ILoyaltyRankable, Customer, StandardCustomer, VipCustomer, CustomerReview, WarrantyClaim
  com/smartphonestore/manager/          CustomerManager
  com/smartphonestore/view/             CustomerManagementMenu (Admin + Bán hàng), CustomerMenu (khách)
  com/smartphonestore/util/             DateUtil (định dạng ngày DÙNG CHUNG cả nhóm)



## 2. Hai menu

| Menu | Ai dùng | Cách gọi | :--- | :--- | :--- |
| `CustomerManagementMenu` | ADMIN, SALES (role khác sẽ bị từ chối) | `new CustomerManagementMenu(customerManager, accountManager, invoiceManager, productManager, role).show();` rồi quay lại menu trước |


| `CustomerMenu` | khách đăng nhập (role CUSTOMER) | `boolean dangXuat = new CustomerMenu(customerManager, maKhach productManager).show();` true = Đăng xuất, false = Thoát chương trình |

- Cả nhóm chỉ tạo MỘT `CustomerManager` trong `Main` rồi truyền xuống. Mỗi manager giữ dữ liệu trong bộ nhớ, tạo hai bản sẽ ghi đè file của nhau.
- Menu khách không bao giờ hỏi mã khách. Mã lấy từ tài khoản đăng nhập nên khách chỉ thấy dữ liệu của mình.
- Chỉ ADMIN được xóa khách và xóa phiếu bảo hành. Quyền này kiểm tra ở menu.

## 3. Những hàm TV1/TV2/TV3 cần viết cho phần TV4 (hợp đồng)

| Người | Hàm | Ý nghĩa |
| :--- | :--- | :--- |
| TV1 | `Role.CUSTOMER` | thêm vào enum |
| TV1 | `Account`: đổi `employeeId` thành `linkedId` | cột cuối của `accounts.txt` chứa mã nhân viên (tài khoản nhân viên) hoặc mã khách KH001 (tài khoản khách). Có thể thêm cột riêng nếu muốn |
| TV1 | `AccountManager.createCustomerAccount(String customerId, String username, String password)` trả `boolean` | tạo tài khoản role CUSTOMER. false nếu trùng username hoặc khách đã có tài khoản |
| TV1 | `AccountManager.removeAccountsByOwnerId(String ownerId)` | xóa tài khoản gắn với khách bị xóa. Lý do xóa thay vì khóa: mã khách có thể được cấp lại, tài khoản cũ bị mở khóa sẽ đăng nhập nhầm vào khách mới |
| TV1 | `LoginView` | role CUSTOMER chuyển sang `CustomerMenu`. `AdminMenu` thêm mục "Quản lý khách hàng" gọi `CustomerManagementMenu` |
| TV2 | `ProductManager.existsProduct(String productId)` trả `boolean` | khách chỉ đánh giá sản phẩm có thật |
| TV2 | `ProductManager.getWarrantyMonths(String productId)` trả `int` | lấy từ `Phone.getWarrantyMonths()`, phụ kiện trả 0 |
| TV3 | `InvoiceManager.getBuyerIdBySerial(String serial)` | mã khách mua seri. `null` nếu chưa bán hoặc hóa đơn đã hủy (hủy hóa đơn thì seri phải về kho) |
| TV3 | `InvoiceManager.getProductIdBySerial(String serial)` | mã sản phẩm của seri |
| TV3 | `InvoiceManager.getSaleDateBySerial(String serial)` | ngày bán dạng `dd/MM/yyyy` (ghi bằng `DateUtil.format`) |
| TV3 | gọi `customerManager.recordPurchase(customerId, amount)` | sau khi thanh toán xong |
| TV3 | gọi `customerManager.refundPurchase(customerId, amount)` | khi hủy hóa đơn đã ghi nhận |
| TV3 | gọi `customerManager.getVipDiscount(customerId, orderTotal)` | số tiền giảm khi tính tiền (khách thường trả 0). Hóa đơn phải có `customerId` hợp lệ |
| TV3 | `SalesMenu` | thêm mục "Quản lý khách hàng" gọi `CustomerManagementMenu` |

Mọi lỗi nghiệp vụ trong phần TV4 ném `IllegalArgumentException` kèm thông báo tiếng Việt, và menu tự bắt để in ra.

## 4. Quy tắc nghiệp vụ đã cài

- Khách mới do nhân viên tạo, luôn là STANDARD. Tài khoản đăng nhập do nhân viên cấp (lúc thêm khách hoặc mục [10]).
- SĐT: 10 chữ số, bắt đầu bằng 0, không được trùng khách khác. Đổi quy tắc tại hằng `Customer.PHONE_REGEX`.
- Nâng VIP: chỉ khi `totalSpent >= 15.000.000`. Tạo `VipCustomer` mới, giữ nguyên mã, thông tin và tổng chi tiêu (số lần mua `visitCount` chỉ có ở STANDARD nên không mang sang).
- Xóa khách: chỉ khi chưa mua hàng (`totalSpent == 0`) và chưa có phiếu bảo hành. Đánh giá của khách bị xóa theo.
- Khách chỉ sửa / xóa được đánh giá của chính mình.
- Bảo hành: nhân viên chỉ nhập SỐ SERI. Hệ thống lấy người mua, mã sản phẩm, ngày bán (TV3) và số tháng bảo hành (TV2). Hạn bảo hành = ngày bán + số tháng (đúng ngày cuối hạn vẫn nhận). Từ chối khi: seri chưa bán, hóa đơn hủy, sản phẩm không bảo hành, hết hạn, hoặc seri đang có phiếu chưa đóng.
- Khách xem phiếu: "Dự kiến hoàn thành" = ngày tiếp nhận + 5 ngày (hằng `WarrantyClaim.PROCESSING_DAYS`). Phiếu đã đóng hiện "Đã hoàn tất" hoặc "Yêu cầu bị từ chối".

## 5. Định dạng file (đúng quy chuẩn `|`, UTF-8)

- `data/customers.txt` (7 cột): `STANDARD|id|tên|sđt|email|tổng chi tiêu|số lần mua` hoặc `VIP|id|tên|sđt|email|tổng chi tiêu|tỷ lệ giảm`
- `data/reviews.txt` (6 cột): `id|mã khách|mã sản phẩm|ngày|số sao|nhận xét`
- `data/warranty_claims.txt` (9 cột): `id|mã khách|mã sản phẩm|seri|lỗi|ngày bán|số tháng BH|ngày nhận|trạng thái`
