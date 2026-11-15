Menu cũ → menu mới
#### 1. Chức năng
Menu cũ	Quyết định
[1] Xem thông tin cá nhân (của chính mình)	`Đổi`: xem danh sách khách và xem chi tiết một khách theo ID
[2] Cập nhật SĐT, Email	                    `Giữ`, nhân viên nhập ID khách rồi sửa hộ
[3] Tra cứu hạng thành viên	                `Giữ`, nhập ID rồi gọi evaluateRank()
[4] Xem đặc quyền VIP	                    `Giữ`, nhân viên dùng khi tính tiền cho khách VIP (calculateVipDiscount)
[5] Gửi đánh giá	                        `Giữ`, nhân viên ghi nhận đánh giá của khách tại quầy
[6] Lịch sử đánh giá	                    `Giữ`, lọc theo ID khách
[7] Gửi yêu cầu bảo hành	                `Giữ`, nhân viên tiếp nhận máy và tạo phiếu
[8] Tra cứu tiến trình bảo hành	            `Giữ`, thêm cập nhật trạng thái (updateStatus), vì khách không tự xem được
[9] Đăng xuất	                            `Bỏ`, đổi thành “Quay lại menu trước” vì đăng xuất thuộc menu của Admin/Bán hàng
#### 2.Chức năng phải thêm

Khách không tự đăng ký, nên nhân viên phải tạo hồ sơ. Checklist nghiệm thu cũng yêu cầu CRUD đầy đủ, mà menu cũ thiếu phần này:

Thêm khách hàng mới
Xóa khách hàng (nên chỉ cho ADMIN)
Tìm kiếm khách theo tên hoặc SĐT
Nâng khách lên VIP (upgradeToVip)

#### 3.Những chỗ ảnh hưởng đến thành viên khác

`1`.Role CUSTOMER không còn dùng. Báo TV1 bỏ khỏi enum Role và accounts.txt, để Main và LoginView chỉ điều hướng 3 vai trò ADMIN, WAREHOUSE, SALES.
`2`.Đổi tên UserMenu thành CustomerMenu và đặt vào view/. Constructor chỉ cần CustomerManager và role hiện tại, không còn customerId.
`3`.Cách gọi menu. TV1 thêm một mục “Quản lý khách hàng” vào AdminMenu, TV3 thêm vào SalesMenu, cả hai cùng gọi new CustomerMenu(manager, role).show(). Hãy bàn với hai bạn này.
`4`.Bảo hành. Tài liệu quy chuẩn ghi WAREHOUSE xử lý WarrantyClaim, nhưng menu Kho của TV2 không có chức năng này. Theo ý bạn, chỉ 
`5`.Sales và Admin xem được, nên bảo hành nằm hoàn toàn ở menu của bạn. Hãy báo TV2 để khỏi trùng việc.
Phân quyền xóa. Truyền Role vào menu rồi kiểm tra if (role != Role.ADMIN) trước khi cho xóa.