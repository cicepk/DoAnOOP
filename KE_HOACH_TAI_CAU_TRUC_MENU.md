# KẾ HOẠCH TÁI CẤU TRÚC MENU – CỬA HÀNG ĐIỆN THOẠI OFFLINE

> Áp dụng cho toàn nhóm (TV1 – TV4). Đọc hết mục 1 → 3 trước khi sửa code, mục 4 là phần việc riêng của từng người.

---

## 1. Mục tiêu

1. **Cửa hàng offline, chỉ nhân viên dùng phần mềm.** Khách hàng không có tài khoản, không đăng nhập; mọi việc của khách (mua, tích điểm, bảo hành, góp ý) đều do nhân viên thao tác tại quầy. Hồ sơ khách (`Customer`) vẫn giữ.
2. **Admin = chủ / quản lý cửa hàng, có toàn quyền.** Admin dùng lại nguyên menu Kho và menu Bán hàng (không viết lại), cộng thêm các chức năng chỉ Admin được làm (đánh dấu ★).
3. **Menu lớn chứa menu nhỏ.** Menu lớn chia theo vai trò, menu nhỏ chia theo nghiệp vụ, mỗi chức năng chỉ có **một** chỗ trong cây.
4. **Phân quyền giống ngoài đời:** bán hàng không sửa kho, kho không quyết giá, chỉ admin được xóa dữ liệu quan trọng; mỗi người tự giữ mật khẩu của mình.
5. **Không viết lại nghiệp vụ.** Các hàm xử lý hiện có (`addEmployee`, `createInvoice`, `receiveClaim`, nhập hàng…) giữ nguyên, chủ yếu sắp xếp lại vị trí trên menu.

---

## 2. Sơ đồ cây menu mới

Ký hiệu: **★ = chỉ Admin thấy** (cùng một menu, nhân viên mở thì mục ★ bị ẩn).

```
HỆ THỐNG QUẢN LÝ CỬA HÀNG ĐIỆN THOẠI
├── [1] Đăng nhập ──► vào menu theo vai trò
└── [0] Thoát chương trình


═══════════════════════════════ ADMIN ═══════════════════════════════
ADMIN
├── [1] Báo cáo thống kê
│   ├── [1] Doanh thu (theo ngày / theo tháng)
│   ├── [2] Sản phẩm bán chạy
│   └── [3] Hàng sắp hết
│
├── [2] Quản lý nhân sự
│   ├── [1] Cập nhật nhân viên
│   │   ├── [1] Thêm nhân viên (tạo tài khoản kèm theo)
│   │   ├── [2] Sửa thông tin (Tên, Chức vụ, Lương)
│   │   ├── [3] Xóa / Cho nghỉ việc
│   │   ├── [4] Cấp lại mật khẩu
│   │   └── [5] Khóa / Mở khóa tài khoản
│   └── [2] Xem thông tin
│       ├── [1] Danh sách nhân viên
│       ├── [2] Tài khoản nhân viên
│       ├── [3] Bảng lương
│       └── [4] Doanh số nhân viên
│
├── [3] Quản lý kho ─────────► menu KHO (đầy đủ + mục ★)
├── [4] Quản lý bán hàng ────► menu BÁN HÀNG (đầy đủ + mục ★)
├── [5] Đổi mật khẩu
└── [0] Đăng xuất


══════════════════════════ NHÂN VIÊN BÁN HÀNG ═══════════════════════
BÁN HÀNG
├── [1] Bán hàng
│   ├── [1] Lập hóa đơn
│   ├── [2] Thanh toán (tiền mặt / chuyển khoản)
│   ├── [3] In hóa đơn
│   └── [4] Hủy hóa đơn chưa thanh toán
│
├── [2] Tra cứu
│   ├── [1] Hóa đơn
│   └── [2] Tồn kho (chỉ xem)
│
├── [3] Chăm sóc khách hàng
│   ├── [1] Hồ sơ khách hàng
│   │   ├── [1] Danh sách khách hàng
│   │   ├── [2] Tìm kiếm (tên / SĐT)
│   │   ├── [3] Thông tin chi tiết (Tên, Mã, SĐT, Hạng, Lịch sử mua – chỉ xem)
│   │   ├── [4] Thêm khách hàng
│   │   ├── [5] Sửa thông tin liên hệ
│   │   └── [6] ★ Xóa khách hàng
│   └── [2] Bảo hành
│       ├── [1] Tiếp nhận bảo hành (theo SĐT)
│       ├── [2] Tra cứu phiếu (SĐT / Seri / Mã đơn)
│       ├── [3] Cập nhật trạng thái phiếu
│       └── [4] ★ Xóa phiếu đã đóng
│
├── [4] Doanh số của tôi
│
├── [5] Thu cũ đổi mới  (một chức năng, chạy lần lượt các bước)
│        ① Kiểm tra máy cũ ──► N: dừng lại
│        │                  └► Y: tiếp tục
│        ② Tính tiền bù trừ
│        ③ Cập nhật kho (+ máy cũ vào kho, − máy mới xuất kho)
│        ④ Ghi nhận doanh thu
│        ⑤ In hóa đơn đổi mới
│
├── [6] Đổi mật khẩu
└── [0] Đăng xuất


═════════════════════════════ NHÂN VIÊN KHO ═════════════════════════
KHO
├── [1] Danh sách sản phẩm
│   ├── [1] Xem tất cả
│   ├── [2] Tìm kiếm
│   ├── [3] Thêm sản phẩm mới
│   ├── [4] Sửa thông tin sản phẩm
│   └── [5] ★ Xóa sản phẩm
│
├── [2] Quản lý nhập kho
│   ├── [1] Tạo phiếu nhập (Mã phiếu, Mã NV, Thông tin SP)
│   └── [2] Xem lịch sử phiếu nhập
│
├── [3] Cảnh báo hàng sắp hết
├── [4] Kiểm tra tồn kho (đối chiếu số lượng với serial thực tế)
│
├── [5] ★ Quản lý giảm giá
│   ├── [1] Thiết lập giảm giá (Mã SP, % giảm)
│   └── [2] Xem danh sách đang giảm giá
│
├── [6] Đổi mật khẩu
└── [0] Đăng xuất
```

### Vì sao chia như vậy

| Quyết định | Lý do |
|---|---|
| Bỏ tài khoản khách | Cửa hàng offline: khách đọc SĐT tại quầy, nhân viên tra và thao tác. |
| Admin dùng lại menu Kho / Bán hàng | "Toàn quyền" = đi tới được mọi chức năng; dùng chung class thì không trùng code, sửa một chỗ là đúng cả hai. |
| Không có mục Chăm sóc KH riêng ở Admin | Admin đã vào được qua Quản lý bán hàng → [3]; thêm nữa chỉ là một lối vào trùng. |
| Nhân sự chia *Cập nhật* / *Xem* | Theo ví dụ của thầy; mỗi chức năng chỉ xuất hiện một lần. |
| "Thêm tài khoản" gộp vào "Thêm nhân viên" | Nhân viên nào cũng cần tài khoản; tài khoản không gắn nhân viên thì không biết ai dùng. |
| Xóa nhân viên → cho nghỉ việc nếu đã có hóa đơn | Hóa đơn cũ ghi mã EMPxx; xóa hẳn thì báo cáo doanh số mất người lập. |
| Giảm giá, xóa sản phẩm, xóa khách là ★ | Giá bán do chủ quyết; xóa dữ liệu làm mất lịch sử hóa đơn / bảo hành. |
| Thu cũ đổi mới là 1 mục chạy tuần tự | Đó là các bước của một quy trình; tách thành mục riêng thì có thể bấm "cập nhật kho" khi chưa kiểm tra máy. |
| Ai cũng tự đổi mật khẩu, admin chỉ cấp lại | Mỗi hóa đơn / phiếu nhập ghi mã nhân viên – chỉ có giá trị khi chỉ chính người đó biết mật khẩu. |
| Lịch sử mua chỉ xem | Lịch sử sinh từ hóa đơn; cho thêm tay thì làm giả được chi tiêu để lên VIP. |

---

## 3. Quy tắc chung khi sửa code

1. **Menu dùng chung nhận `Account currentUser`** (không chỉ `Role` hay `staffId`): lấy được cả vai trò (ẩn mục ★) lẫn mã EMPxx (ghi vào hóa đơn, phiếu nhập).
2. **Mục ★ chặn ở 2 chỗ:** không in ra khi in menu **và** kiểm tra lại trong `switch` (người dùng vẫn gõ được số dù không thấy mục).
3. **Mỗi menu con là một hàm riêng** (`staffMenu()`, `importMenu()`…), không viết logic dài trong `case`.
4. **`[0]` luôn là quay lại cấp trên.** Chỉ màn hình đăng nhập có "Thoát chương trình". **Không gọi `System.exit` trong menu con** (manager đã tự lưu file sau mỗi thao tác nên không mất dữ liệu).
5. **Không viết lại nghiệp vụ** – chỉ chuyển vị trí gọi hàm. Cần thêm hàm thì thêm vào manager của người phụ trách, không viết tắt trong menu của người khác.
6. **Giao diện không ghi "TV1…TV4", "THÀNH VIÊN 2"** – phần mềm thật không có.
7. **Đổi định dạng file data phải đọc được dòng cũ** (thiếu cột thì dùng giá trị mặc định) để không hỏng dữ liệu đang có.
8. **Mỗi giai đoạn xong phải chạy được** và test lại kịch bản trong `KICH_BAN_TEST.md` trước khi sang giai đoạn sau.
9. **Git:** commit phần đang dở trước, tạo nhánh riêng cho đợt sửa này; mỗi người commit phần của mình.

---

## 4. Việc cần sửa của từng thành viên

### 👤 TV1 – Đăng nhập, Admin, Nhân sự, Tài khoản

| File | Việc cần làm |
|---|---|
| `model/enums/Role.java` | Bỏ `CUSTOMER` và `isStaff()`. |
| `model/auth/Account.java` | `isValidLink` chỉ nhận `EMPxx`. |
| `manager/AccountManager.java` | Bỏ `createCustomerAccount`, `removeAccountsByOwnerId`. Thêm `changeOwnPassword(id, oldPass, newPass)` (kiểm tra mật khẩu cũ). |
| `view/LoginView.java` | Bỏ `[2] Đăng ký`, `handleRegister`, nhánh `CUSTOMER`. Truyền `Account` vào cả 3 menu (`WarehouseMenu`, `SalesMenu`, `AdminMenu`). |
| `view/AdminMenu.java` | Menu chính 5 mục theo cây. Thêm `staffMenu()` → `updateStaffMenu()` / `viewStaffMenu()`. Tách `manageAccount()` thành `resetPassword()` và `toggleLock()`. [3] gọi `new WarehouseMenu(productManager, currentAccount)`, [4] gọi `new SalesMenu(..., currentAccount)`. Bỏ `openCustomerMenu()` và `System.exit`. |
| `AdminMenu.deleteEmployee()` | Nhân viên đã có hóa đơn → chỉ khóa tài khoản, báo "đã cho nghỉ việc"; chưa có → xóa như cũ. (Dùng `SalesManager.hasInvoicesOfStaff` của TV3.) |
| `view/ChangePasswordView.java` *(mới)* | Hàm `static` đổi mật khẩu dùng chung cho 3 menu (giống kiểu `PurchaseHistoryView`). |
| `view/ReportMenu.java` *(mới)* | Admin [1]: doanh thu ngày/tháng, SP bán chạy, hàng sắp hết (gọi hàm của TV3, TV2). |
| `AdminMenu` → Xem thông tin → Doanh số NV | Gọi `SalesManager.revenueByStaff`. |
| *(nên làm)* Bảng lương | `SalesStaff.monthlyRevenue` đang gõ tay trong `employees.txt` → lấy từ `revenueByStaff` để hoa hồng khớp hóa đơn thật. |
| *(tùy chọn)* `accounts.txt` thêm cột `mustChangePassword` | Bắt đổi mật khẩu sau khi được cấp lại / tài khoản mới; loader đọc được cả dòng 7 và 8 cột. |
| `data/accounts.txt` | Xóa ACC05–ACC10 (tài khoản khách), sửa dòng `# Format` bỏ `CUSTOMER` / `CUSxx`. |

**Cần từ người khác:** TV3 – `hasInvoicesOfStaff`, `revenueByDate`, `revenueByMonth`, `revenueByStaff`, `topSellingProducts`. TV2 – `getLowStockProducts` (đã có).

---

### 👤 TV2 – Kho

| File | Việc cần làm |
|---|---|
| `view/WarehouseMenu.java` – **bước 1** | Tách từng `case` trong `displayMenu()` (≈500 dòng) thành hàm riêng: `showAll()`, `showSerials()`, `importGoods()`, `updateProduct()`, `deleteProduct()`, `search()`, `lowStock()`, `discount()`. **Chưa đổi chức năng** – chạy lại thấy y như cũ mới sang bước 2. |
| `view/WarehouseMenu.java` – **bước 2** | Dựng menu theo cây (6 nhóm). Constructor nhận `(ProductManager, Account)`. Ẩn ★ [1][5] Xóa SP và [5] Giảm giá với nhân viên kho. Bỏ hàm `main()`, bỏ chữ "THÀNH VIÊN 2". Thêm [6] Đổi mật khẩu. |
| Phiếu nhập | Model mới `model/product/ImportReceipt.java` (id, staffId, ngày, nhà cung cấp, số máy, tổng tiền) + file `data/import_receipts.txt`. Thêm cột `receiptId` vào `product_details.txt` (dòng cũ mặc định `PN00`). `importGoods()` sinh mã `PNxx`, lấy mã NV từ `currentUser`, lưu phiếu khi nhập xong. |
| `manager/ProductManager.java` | Thêm đọc/ghi phiếu nhập, `getAllReceipts()`, `nextReceiptId()`. |
| Kiểm tra tồn kho (đối chiếu) | So `stockQuantity` với `countInStock(id)` (đều đã có); lệch thì báo và hỏi sửa bằng `syncStockFromDetails()`. |
| Giảm giá | Thêm cột `discountRate` vào `products.txt` (mặc định `0`), thêm `Product.getSalePrice()`. `applyDiscount` chỉ lưu tỷ lệ, **không ghi đè giá gốc**. Thêm "Xem danh sách đang giảm giá"; đặt `0%` là hủy giảm. |
| Xóa sản phẩm ★ | `deleteID` đã chặn SP có máy SOLD / đang bảo hành – chỉ cần ẩn với nhân viên kho. |
| Thu cũ đổi mới (hỗ trợ TV3) | Thêm trạng thái serial `TRADE_IN` và hàm nhận máy cũ vào kho, ví dụ `addTradeInSerial(...)`. |

**Cung cấp cho người khác:** `getSalePrice()` (TV3 dùng khi lập hóa đơn), `getALL()` / `findByNameOrBrand()` (TV3 tra tồn kho), hàm nhận máy thu cũ (TV3).

---

### 👤 TV3 – Bán hàng, Thanh toán, Thu cũ đổi mới

| File | Việc cần làm |
|---|---|
| `view/SalesMenu.java` | Constructor nhận `Account` thay cho `staffId`. Menu 6 nhóm theo cây. Bỏ `[0] System.exit`, bỏ chữ "(TV4)". Thêm [6] Đổi mật khẩu. |
| [2] Tra cứu → Tồn kho | Chỉ xem: gọi `productManager.getALL()` / `findByNameOrBrand()`, in tên, giá bán, số lượng còn. |
| [3] Chăm sóc KH | Gọi `new CustomerManagementMenu(..., currentUser)`. |
| [4] Doanh số của tôi | Doanh thu hôm nay và tháng này của chính `staffId` đang đăng nhập. |
| `createInvoice()` | Tra khách theo **mã hoặc SĐT** (`customerManager.findByPhone` đã có); không thấy thì hỏi tạo nhanh (`addCustomer` đã có) hoặc bán `KHACHLE`. |
| `manager/SalesManager.java` | Thêm `hasInvoicesOfStaff(id)`, `revenueByDate(LocalDate)`, `revenueByMonth(month, year)`, `revenueByStaff(staffId, month, year)`, `topSellingProducts(n)` (đếm `invoice_details` của hóa đơn PAID). |
| `SalesManager.buildDetail()` | Đổi `product.getPrice()` → `product.getSalePrice()` (sau khi TV2 thêm giảm giá). |
| [5] Thu cũ đổi mới | Một hàm chạy tuần tự: ① kiểm tra máy cũ (Y/N, N thì dừng) → ② định giá & tính bù trừ → ③ lập hóa đơn máy mới, giá trị máy cũ cộng vào `discountAmount` → máy cũ `TRADE_IN` (hàm TV2), máy mới `SOLD` (`sellSerial`) → ④ doanh thu tự đúng vì tính từ hóa đơn → ⑤ in hóa đơn có dòng "Trừ máy thu cũ". Lưu `data/trade_ins.txt` (mã, mã HĐ, seri cũ, giá thu, ngày). |

**Cần từ người khác:** TV2 – `getSalePrice()`, hàm nhận máy thu cũ. TV4 – `findByPhone`, `addCustomer`, `getVipDiscount` (đã có).
**Cung cấp cho người khác:** các hàm báo cáo/doanh số cho TV1.

---

### 👤 TV4 – Khách hàng & Bảo hành

| File | Việc cần làm |
|---|---|
| `view/CustomerMenu.java` | **Xóa file** (khách không còn đăng nhập). Trước khi xóa, chuyển logic `updateContact` (và `addReview` nếu giữ đánh giá) sang `CustomerManagementMenu`. |
| `view/CustomerManagementMenu.java` | Constructor nhận `Account currentUser` thay cho `Role`; bỏ tham số `accountManager`. Bỏ `createAccountFor` (dòng 187, đang gọi ở dòng 127) và lời gọi `removeAccountsByOwnerId` (dòng 157). |
| | Chia 2 nhóm: **[1] Hồ sơ** (danh sách, tìm, chi tiết, thêm, sửa thông tin liên hệ, ★ xóa) và **[2] Bảo hành** (tiếp nhận theo SĐT, tra cứu, cập nhật trạng thái, ★ xóa phiếu đã đóng). |
| Thông tin chi tiết | Gộp các mục cũ [2] chi tiết + [6] hạng VIP + [7] lịch sử mua thành một màn hình. |
| Sửa thông tin liên hệ | Dùng `updateCustomer` / `updateContact` đã có trong `CustomerManager`. |
| Tiếp nhận bảo hành theo SĐT | Nhập SĐT → liệt kê các máy khách đã mua → chọn seri → báo còn/hết hạn bảo hành. |
| Tra cứu phiếu theo mã đơn | Mã đơn → seri qua `salesManager.getDetailsByInvoice` → `getClaimsBySerial` (không cần đổi file). |
| `view/PurchaseHistoryView.java` | Chỉ sửa chú thích (không còn `CustomerMenu` dùng). |
| Data | `customers.txt`, `reviews.txt`, `warranty_claims.txt` **giữ nguyên**. |

**Cần từ người khác:** TV3 – `getInvoicesByCustomer`, `getDetailsByInvoice` (đã có).

---

## 5. Thứ tự thực hiện

| Giai đoạn | Nội dung | Ai | Đổi file data? |
|---|---|---|---|
| 1 | Bỏ khách đăng nhập | TV1, TV4 | Chỉ xóa 6 dòng `accounts.txt` |
| 2 | Sắp lại menu theo cây, truyền `Account`, mục ★ | Cả 4 (mỗi người file của mình) | Không |
| 3 | Đổi mật khẩu, cấp lại MK, cho nghỉ việc | TV1 (+ hàm TV3) | Không (cột `mustChangePassword` là tùy chọn) |
| 4 | Báo cáo, doanh số | TV3 viết hàm, TV1 làm menu | Không |
| 5 | Phiếu nhập, đối chiếu tồn, giảm giá | TV2 (+ TV3 sửa `buildDetail`) | Có: `products.txt`, `product_details.txt`, `import_receipts.txt` |
| 6 | Thu cũ đổi mới | TV3 (+ hàm TV2) | Có: `trade_ins.txt`, trạng thái `TRADE_IN` |
| 7 | Cập nhật `KICH_BAN_TEST.md`, `SO_DO_CLASS_UML.drawio`, `TOM_TAT_DU_AN.md` | Cả nhóm | – |

Giai đoạn 1 → 4 không đụng định dạng dữ liệu nên làm trước; sau mỗi giai đoạn chương trình phải chạy được.

---

## 6. Việc cần nhóm chốt trước khi code

- [ ] **Đánh giá, SOS, tương thích phụ kiện:** cây mới không có chỗ cho `CustomerReview`, `Featurephone.toggleEmergencySOS`, `Accessory.isCompatibleWith`. Nếu vẫn nộp theo `PHAN_CONG_CONG_VIEC.md` thì nên giữ:
  - CSKH thêm **[3] Đánh giá, góp ý** (ghi nhận, xem, ★ xóa).
  - Bán hàng → Tra cứu thêm **Kiểm tra phụ kiện tương thích**.
  - SOS đưa vào **Xem chi tiết sản phẩm** của Kho.
- [ ] **Luật thu cũ đổi mới:** thu cả máy mua nơi khác hay chỉ máy cửa hàng đã bán? Định giá máy cũ theo cách nào (ví dụ % giá nhập, có khung tối đa)? Máy cũ thu về có bán lại không?
- [ ] **Giảm giá:** giữ ★ (chỉ Admin) hay cho nhân viên kho dùng?
- [ ] **Cột `mustChangePassword`:** có làm không?
