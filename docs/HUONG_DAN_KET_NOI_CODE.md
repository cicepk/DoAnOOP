# MODULE QUẢN LÝ KHO & SẢN PHẨM (THÀNH VIÊN 2 - HUỲNH VĂN PHÁT)

Tài liệu này mô tả mã nguồn, dữ liệu và cách tích hợp Module Quản lý Kho hàng & Sản phẩm thuộc Đồ án OOP Cửa hàng Điện thoại (`SmartphoneStore`).
Code của module nằm chung trong package `com.smartphonestore` với các thành viên khác (không còn thư mục riêng `quanlykho/`).

---

## 1. Cấu trúc thư mục (tính từ gốc repo)

```
src/                                # Gốc repo - chạy chương trình từ thư mục này
├── com/smartphonestore/
│   ├── model/product/
│   │   ├── Product.java           # Lớp trừu tượng cha cho toàn bộ sản phẩm
│   │   ├── Phone.java             # Lớp trung gian cho các dòng điện thoại
│   │   ├── Smartphone.java        # Điện thoại thông minh (có ROM, thu cũ đổi mới)
│   │   ├── Featurephone.java      # Điện thoại phổ thông (có SOS khẩn cấp, pin trâu)
│   │   ├── Accessory.java          # Phụ kiện (Sạc, Tai nghe, Ốp lưng...)
│   │   ├── IDiscountable.java      # Interface chiết khấu giảm giá
│   │   └── ProductDetail.java      # Quản lý từng con máy vật lý (IMEI, ngày nhập, giá, NCC, trạng thái)
│   ├── manager/
│   │   └── ProductManager.java     # Quản lý CRUD sản phẩm, đọc/ghi file text an toàn
│   ├── view/
│   │   └── WarehouseMenu.java      # Giao diện Console Menu Nhân viên kho (10 chức năng)
│   └── exception/
│       └── ValidationException.java # Ngoại lệ kiểm tra dữ liệu đầu vào
├── data/                           # Dữ liệu dùng chung của cả nhóm (đường dẫn tương đối "data/...")
│   ├── products.txt                # Danh mục 50 sản phẩm với tồn kho
│   ├── product_details.txt         # 403 con máy vật lý chi tiết từng IMEI
│   └── ... (các file text mẫu khác)
└── docs/*.md                       # Tài liệu phân công, quy chuẩn code, tóm tắt dự án
```

---

## 2. Hướng dẫn các thành viên khác tích hợp

### Cách gọi Menu Quản lý kho từ Menu chính (`Main.java`):
```java
import com.smartphonestore.view.WarehouseMenu;

// Trong switch-case của menu chính khi đăng nhập với vai trò Quản lý kho:
WarehouseMenu.displayMenu();
```

### Cách lấy thông tin hoặc xuất kho từ Module Bán hàng (Thành viên 3):
```java
import com.smartphonestore.manager.ProductManager;
import com.smartphonestore.model.product.Product;

ProductManager prdManager = new ProductManager();

// Tìm kiếm sản phẩm:
Product p = prdManager.findByID("PRD01");

// Bán hàng / Giảm tồn kho:
boolean success = prdManager.decreaseStock("PRD01", 1);
```
