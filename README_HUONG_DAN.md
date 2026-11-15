# MODULE THANH VIEN 3 – BAN HANG (BAN DA SUA THEO BAO LOI 13/11/2026)

## 1. Vi tri cac file trong du an

Ghep cac file trong `src/` vao `src/` cua du an chung, khong dat nguyen package Java trong `com/smartphonestore/banhang/`.

```text
src/com/smartphonestore/
├── manager/SalesManager.java
├── model/sale/
│   ├── IPrintable.java
│   ├── Invoice.java
│   ├── InvoiceDetail.java
│   ├── Payment.java
│   ├── CashPayment.java
│   └── ElectronicPayment.java
└── view/SalesMenu.java
```

Cac lop dung chung `ProductManager`, `Product`, `ProductDetail`, `CustomerManager`, `EmployeeManager`, `Employee`, `SalesStaff`, `DateUtil`, `FileIO`, `InputHelper` phai duoc lay tu cac thanh vien tuong ung. Khong tao ban sao cac lop nay trong module ban hang.

## 2. Cac loi TV3 da sua

- Bo `InventoryGateway`; `SalesManager` nhan mot `ProductManager` duy nhat tu `Main`.
- Ban hang theo tung serial/IMEI: moi `InvoiceDetail` bat buoc co mot serial va quantity = 1.
- Khi tao hoa don, goi `checkSellable()` va `sellSerial()`; neu luu hoa don that bai thi hoan tac bang `returnSerial()`.
- Khi huy hoa don chua thanh toan, goi `returnSerial()` de dua serial ve `IN_STOCK`. Khong cho huy hoa don da thanh toan.
- Them `getBuyerIdBySerial()` va `getSaleDateBySerial()`; chi tra thong tin tu hoa don `PAID` de TV4 tra cuu bao hanh.
- `savePayments()` khong doc lai file va noi them toan bo payment trong bo nho. Danh sach dong thanh toan duoc nap mot lan; moi lan chi them mot dong, nen khong lap giao dich cu.
- Dinh dang `payments.txt` dung 7 cot: `id|invoiceId|amount|paymentDate|method|transactionCode|extraData`.
- Ngay hoa don/thanh toan dung `DateUtil.nowText()`; doanh thu hom nay so sanh bang `DateUtil.parse()` va `DateUtil.today()`.
- Ma hoa don tang theo dang `INV01`, `INV02`...; khong tao ma `HD0001`.
- Dung `FileIO` va `InputHelper`, khong tu tao `Scanner` hay tu doc/ghi file bang `Files`.
- Sau thanh toan thanh cong, goi `CustomerManager.recordPurchase()` va cap nhat doanh so `SalesStaff.addMonthlyRevenue()`.
- So tien ghi file dung `String.format("%.0f", ...)`, khong ghi dang so mu khoa hoc.

## 3. Cach Main khoi tao va truyen manager

Theo mau thong nhat cua nhom, `Main` tao moi manager mot lan:

```java
ProductManager productManager = new ProductManager();
EmployeeManager employeeManager = new EmployeeManager();
CustomerManager customerManager = new CustomerManager();
SalesManager salesManager = new SalesManager(productManager);
```

Khi tai khoan dang nhap co role SALES:

```java
new SalesMenu(salesManager, customerManager, employeeManager,
              account.getLinkedId()).show();
```

Ten bien `account` co the khac tuy code TV1, nhung gia tri truyen vao phai la `linkedId` cua tai khoan nhan vien (ma EMPxx), khong dung NV01 mac dinh.

## 4. API TV2 ma module nay su dung

`ProductManager` can co cac phuong thuc da thong nhat trong bao loi:

- `String checkSellable(String serial)`
- `ProductDetail findDetailBySerial(String serial)`
- `Product findByID(String productId)`
- `boolean sellSerial(String serial)`
- `boolean returnSerial(String serial)`

`Product` can co `getId()`, `getName()`, `getPrice()`; `ProductDetail` can co `getProductId()`, `getSerialNumber()`.

## 5. File du lieu

Tao/giu cac file trong thu muc `data/`:

- `invoices.txt`
- `invoice_details.txt`
- `payments.txt`

Cac dong dau file `# Format:` duoc ghi lai khi luu. Khong tu y doi thu tu cot. `invoice_details.txt` moi dong la mot may, co serial rieng, quantity = 1.

## 6. Luu y truoc khi gop

Bo code nay da duoc chinh theo API duoc ghi trong `BAO_LOI_HUONG_SUA.md`. Tuy nhien, cac file Java cua TV2/TV1/TV4 khong nam trong goi tai lieu hien tai, nen can ghep vao repository va compile cung toan bo project de xac nhan ten package va chu ky API thuc te. Neu code TV2 chua co cac ham serial neu tren, TV2 phai bo sung truoc khi TV3 chay duoc.

Chuoi QR trong `ElectronicPayment` chi la chuoi tham chieu demo, khong tao ma QR ngan hang that.
