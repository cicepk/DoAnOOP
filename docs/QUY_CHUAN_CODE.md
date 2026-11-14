# QUY CHUẨN CODE JAVA & THIẾT KẾ KIẾN TRÚC
# HỆ THỐNG BÁN HÀNG ĐIỆN THOẠI OFFLINE (4 THÀNH VIÊN - 20 CLASS)

---

## 1. Cấu Trúc Gói (Package Structure) Chuẩn Dự Án

Để mã nguồn của cả 4 thành viên khi ghép lại không bị xung đột, dự án tuân theo cấu trúc phân tầng (Layered Architecture):

```text
src/                                           # Gốc repo - chạy chương trình từ thư mục này
├── data/                                      # File .txt dữ liệu dùng chung (đọc bằng đường dẫn "data/xxx.txt")
├── docs/                                      # Tài liệu dự án (*.md)
└── com/
    └── smartphonestore/
        ├── Main.java                          # Điểm vào duy nhất (Main Entry Point)
        │
        ├── model/                             # Chứa 20 Class thực thể & 4 Interface
        │   ├── enums/                         # Role.java, OrderStatus.java, ...
        │   ├── auth/                          # Account.java, ISalaryCalculable.java, Employee.java, ...
        │   ├── product/                       # IDiscountable.java, Product.java, Phone.java, ...
        │   ├── sale/                          # IPrintable.java, Invoice.java, Payment.java, ...
        │   └── customer/                      # ILoyaltyRankable.java, Customer.java, ...
        │
        ├── manager/                           # Các lớp xử lý nghiệp vụ & CRUD
        │   ├── AccountManager.java            # Quản lý tài khoản & xác thực đăng nhập
        │   ├── EmployeeManager.java           # Quản lý nhân viên & tính lương
        │   ├── ProductManager.java            # Quản lý kho, sản phẩm & tồn kho
        │   ├── InvoiceManager.java            # Quản lý bán hàng & thanh toán
        │   └── CustomerManager.java           # Quản lý khách hàng, tích điểm, bảo hành
        │
        ├── view/                              # Giao diện Console & Menu theo phân quyền
        │   ├── LoginView.java                 # Màn hình đăng nhập
        │   ├── AdminMenu.java                 # Menu chức năng của Admin
        │   ├── WarehouseMenu.java             # Menu chức năng của Nhân viên kho
        │   └── SalesMenu.java                 # Menu chức năng của Nhân viên bán hàng
        │
        ├── exception/                         # Ngoại lệ dùng chung (ValidationException.java)
        │
        └── util/                              # Thư viện tiện ích dùng chung
            ├── FileIO.java                    # Đọc/ghi file UTF-8 an toàn
            └── InputHelper.java               # Đọc Scanner không bị trôi lệnh, kiểm tra số
```

---

## 2. Quy Ước Đặt Tên & Viết Code OOP

1. **Đặt tên chuẩn Java:**
   - **Tên Class & Interface:** Viết theo kiểu `PascalCase` (Ví dụ: `Product`, `Smartphone`, `ISalaryCalculable`).
   - **Tên Biến & Phương thức:** Viết theo kiểu `camelCase` (Ví dụ: `stockQuantity`, `calculateSalary()`, `checkLogin()`).
   - **Hằng số & Enum:** Viết theo kiểu `UPPER_SNAKE_CASE` (Ví dụ: `ADMIN`, `WAREHOUSE`, `SALES`, `DEFAULT_VAT_RATE`).
2. **Đóng gói thuộc tính (Encapsulation):**
   - Toàn bộ thuộc tính phải để `private` hoặc `protected` (đối với lớp cha để lớp con truy xuất).
   - Cung cấp đầy đủ các phương thức `getter` và `setter` có kèm kiểm tra hợp lệ.
3. **Kế thừa & Đa hình (Inheritance & Polymorphism):**
   - Khi ghi đè phương thức từ lớp cha hoặc interface, bắt buộc sử dụng annotation `@Override`.
   - Constructor lớp con phải gọi `super(...)` truyền dữ liệu khởi tạo cho lớp cha trước khi gán thuộc tính riêng.

---

## 3. Quy Chuẩn Xử Lý Nhập Liệu Console (Tránh Lỗi Trôi Lệnh)

1. **Tuyệt đối không dùng trực tiếp `scanner.nextInt()` hay `scanner.nextDouble()` rồi sau đó gọi `scanner.nextLine()`:**
   - Lý do: Phím Enter để lại ký tự `\n` trong bộ đệm khiến lần nhập chuỗi tiếp theo bị trôi qua mất dữ liệu.
2. **Quy chuẩn đọc mọi kiểu dữ liệu qua `scanner.nextLine()`:**
   - Luôn đọc chuỗi bằng `scanner.nextLine().trim()`.
   - Chuyển đổi số trong khối `try-catch` bắt lỗi `NumberFormatException`:

```java
public class InputHelper {
    private static final Scanner scanner = new Scanner(System.in);

    public static String readString(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            if (!input.isEmpty()) return input;
            System.out.println("(!) Dữ liệu không được để trống. Vui lòng nhập lại!");
        }
    }

    public static int readInt(String prompt, int min, int max) {
        while (true) {
            System.out.print(prompt);
            try {
                int value = Integer.parseInt(scanner.nextLine().trim());
                if (value >= min && value <= max) return value;
                System.out.printf("(!) Vui lòng nhập số trong khoảng [%d - %d].%n", min, max);
            } catch (NumberFormatException e) {
                System.out.println("(!) Lỗi: Phải nhập số nguyên hợp lệ!");
            }
        }
    }

    public static double readDouble(String prompt, double min) {
        while (true) {
            System.out.print(prompt);
            try {
                double value = Double.parseDouble(scanner.nextLine().trim());
                if (value >= min) return value;
                System.out.printf("(!) Giá trị phải >= %.0f.%n", min);
            } catch (NumberFormatException e) {
                System.out.println("(!) Lỗi: Phải nhập số thực hợp lệ!");
            }
        }
    }
}
```

---

## 4. Quy Chuẩn Đọc / Ghi File Dữ Liệu (.txt UTF-8)

1. **Định dạng dữ liệu:**
   - Dùng dấu gạch đứng `|` làm ký tự phân tách trường.
   - Khi parse dữ liệu, sử dụng `line.split("\\|", -1)` để tránh bị mất cột rỗng ở cuối dòng.
2. **Sử dụng `try-with-resources` để tự động đóng file:**
   - Luôn chỉ định rõ mã hóa `StandardCharsets.UTF_8` để không bị lỗi font tiếng Việt trên Windows.

```java
// Đọc file an toàn
public static List<String> readLines(String filePath) {
    List<String> lines = new ArrayList<>();
    Path path = Paths.get(filePath);
    if (!Files.exists(path)) return lines;

    try (BufferedReader br = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
        String line;
        while ((line = br.readLine()) != null) {
            line = line.trim();
            if (!line.isEmpty()) {
                lines.add(line);
            }
        }
    } catch (IOException e) {
        System.err.println("Lỗi đọc file: " + filePath + " - " + e.getMessage());
    }
    return lines;
}

// Ghi file an toàn
public static boolean writeLines(String filePath, List<String> lines) {
    Path path = Paths.get(filePath);
    try {
        if (path.getParent() != null && !Files.exists(path.getParent())) {
            Files.createDirectories(path.getParent());
        }
        try (BufferedWriter bw = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
            for (String line : lines) {
                bw.write(line);
                bw.newLine();
            }
            return true;
        }
    } catch (IOException e) {
        System.err.println("Lỗi ghi file: " + filePath + " - " + e.getMessage());
        return false;
    }
}
```

---

## 5. Quy Chuẩn Phân Quyền & Kiểm Soát 4 Role

1. **Enum `Role` phân định 4 vai trò rõ ràng:**
   ```java
   public enum Role {
       ADMIN("Quản trị viên"),
       WAREHOUSE("Nhân viên kho"),
       SALES("Nhân viên bán hàng"),
       CUSTOMER("Khách hàng / User");

       private final String displayName;
       Role(String displayName) { this.displayName = displayName; }
       public String getDisplayName() { return displayName; }
   }
   ```
2. **Kiểm tra quyền trước khi thực thi nghiệp vụ nhạy cảm:**
   - Quản trị viên (`ADMIN`): Có quyền thực thi tất cả các chức năng quản trị, nhân sự, tài khoản, doanh thu.
   - Nhân viên kho (`WAREHOUSE`): Thao tác trên `ProductManager`, cập nhật tồn kho, xử lý `WarrantyClaim`.
   - Nhân viên bán hàng (`SALES`): Tạo `Invoice`, lập đơn, thanh toán, quản lý và chăm sóc `Customer`.
   - Khách hàng (`CUSTOMER` / `USER`): Tra cứu sản phẩm, tự đặt mua hàng, thanh toán, xem điểm tích lũy, gửi đánh giá review và yêu cầu bảo hành.

---

## 6. Tiêu Chuẩn Phương Thức Riêng & Thuộc Tính Riêng (Ba-rem Chấm Điểm)

1. **Mỗi Class bắt buộc có ít nhất 1 phương thức riêng biệt** phục vụ nghiệp vụ của nó:
   - Không được chỉ có getter/setter đơn thuần.
   - Phương thức riêng phải có công thức tính toán hoặc logic điều kiện rõ ràng (ví dụ: `calculateSalary()`, `isLowStock()`, `calculateChange()`, `isEligibleForUpgrade()`).
2. **Khi in thông tin (`toString()` hoặc `display()`):**
   - Phải in rõ ràng cả các thuộc tính chung và thuộc tính riêng biệt của class đó ra màn hình console theo định dạng bảng hoặc dòng có căn lề đẹp mắt (`System.out.printf`).
