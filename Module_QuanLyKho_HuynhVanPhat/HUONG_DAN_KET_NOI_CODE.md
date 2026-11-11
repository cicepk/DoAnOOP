# MODULE QUẢN LÝ KHO & SẢN PHẨM (THÀNH VIÊN 2 - HUỲNH VĂN PHÁT)

Thư mục này chứa toàn bộ mã nguồn, cấu trúc dữ liệu và tài liệu thiết kế của Module Quản lý Kho hàng & Sản phẩm thuộc Đồ án OOP Cửa hàng Điện thoại (`SmartphoneStore`).

---

## 1. Cấu trúc thư mục

```
Module_QuanLyKho_HuynhVanPhat/
├── src/com/smartphonestore/
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
├── data/
│   ├── products.txt                # Danh mục 50 sản phẩm với tồn kho
│   ├── product_details.txt         # 403 con máy vật lý chi tiết từng IMEI
│   └── ... (các file text mẫu khác)
└── *.md                            # Tài liệu phân công, quy chuẩn code, tóm tắt dự án
```

---

## 2. Hướng dẫn các thành viên khác tích hợp

### Cách gọi Menu Quản lý kho từ Menu chính (`Main.java`):
```java
import src.com.smartphonestore.view.WarehouseMenu;

// Trong switch-case của menu chính khi đăng nhập với vai trò Quản lý kho:
WarehouseMenu.displayMenu();
```

### Cách lấy thông tin hoặc xuất kho từ Module Bán hàng (Thành viên 3):
```java
import src.com.smartphonestore.manager.ProductManager;
import src.com.smartphonestore.model.product.Product;

ProductManager prdManager = new ProductManager();

// Tìm kiếm sản phẩm:
Product p = prdManager.findByID("PRD01");

// Bán hàng / Giảm tồn kho:
boolean success = prdManager.decreaseStock("PRD01", 1);
```
