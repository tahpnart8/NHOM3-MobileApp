# Bảng đặc tả chức năng

Tổng hợp từ các tài liệu phân tích ở `workflow/docs/analysis/`. Chức năng ngoài phạm vi đợt này được ghi rõ ở cột Ghi chú.

| STT | User | Module | Tên chức năng | Chức năng con | Đặc tả | Ghi chú |
| --- | --- | --- | --- | --- | --- | --- |
| 1 | Người dùng | Quản lý tài khoản và hồ sơ | Đăng ký / đăng nhập | Đăng ký | Tạo tài khoản người dùng mới | Xác thực email |
| 2 | Người dùng | Quản lý tài khoản và hồ sơ | Đăng ký / đăng nhập | Đăng nhập | Truy cập vào ứng dụng bằng tài khoản đã đăng ký | |
| 3 | Người dùng | Quản lý tài khoản và hồ sơ | Đăng ký / đăng nhập | Quên mật khẩu | Xác thực người dùng và cho phép đặt lại mật khẩu | |
| 4 | Người dùng | Quản lý tài khoản và hồ sơ | Quản lý trang cá nhân | Chỉnh sửa thông tin cá nhân | CRUD thông tin cá nhân | |
| 5 | Người dùng | Quản lý tài khoản và hồ sơ | Quản lý trang cá nhân | Chỉnh sửa ảnh hồ sơ | CRUD ảnh đại diện | |
| 6 | Người dùng | Quản lý tài khoản và hồ sơ | Quản lý trang cá nhân | Chỉnh sửa địa chỉ | CRUD địa chỉ | |
| 7 | Người dùng | Quản lý tài khoản và hồ sơ | Quản lý trang cá nhân | Xem đánh giá cá nhân | Hiển thị công khai điểm đánh giá bản thân | |
| 8 | Người dùng | Quản lý bán hàng | Quản lý kệ hàng | Chỉnh sửa sản phẩm trên kệ | CRUD các thông tin, sản phẩm muốn bán, trạng thái (đã bán, chưa bán, ...) | |
| 9 | Người dùng | Quản lý bán hàng | Quản lý kệ hàng | Sắp xếp sản phẩm | Cho phép chỉnh sửa các sản phẩm hiển thị theo danh mục, thời gian | |
| 10 | Người dùng | Quản lý bán hàng | Quản lý bài đăng | Chỉnh sửa nội dung | Mô tả tình trạng món đồ và yêu cầu nếu có | |
| 11 | Người dùng | Quản lý bán hàng | Quản lý bài đăng | Chỉnh sửa hình ảnh | Thêm hình ảnh chụp từ phần mềm | |
| 12 | Người dùng | Quản lý bán hàng | Quản lý bài đăng | Chỉnh sửa hình thức giao dịch | Chọn hình thức giao dịch mong muốn dạng tick box (Bán, Trao đổi, Cho tặng), cho phép cả hybrid (vừa đổi đồ vừa kèm theo tiền) | |
| 13 | Người dùng | Quản lý bán hàng | Quản lý bài đăng | Chỉnh sửa giá cả | Cho phép người đăng nhập giá bán | |
| 14 | Người dùng | Quản lý bán hàng | Quản lý bài đăng | Offer mong muốn | Cho phép người bán chọn các loại offer mong muốn (tiền, vật trao đổi, vừa tiền vừa vật trao đổi) và giới hạn mức deal offer | |
| 15 | Người dùng | Quản lý bán hàng | Quản lý bài đăng | Danh mục | Chọn danh mục cho món đồ | |
| 16 | Người dùng | Tìm kiếm | Tìm kiếm | Từ khóa | Tìm kiếm vật phẩm theo từ khóa | |
| 17 | Người dùng | Tìm kiếm | Tìm kiếm | Danh mục | Người dùng có thể chọn một danh mục, hệ thống trả về toàn bộ bài đăng thuộc danh mục đã chọn | |
| 18 | Người dùng | Tìm kiếm | Tìm kiếm | Khoảng giá | Cho phép người dùng nhập giá tối thiểu và giá tối đa; cung cấp các mốc gợi ý nhanh | |
| 19 | Người dùng | Tìm kiếm | Tìm kiếm | Tình trạng | Hỗ trợ lọc nhiều lựa chọn theo tình trạng: mới (chưa qua sử dụng), đã qua sử dụng (70%), cần sửa chữa/thay thế linh kiện | |
| 20 | Người dùng | Tìm kiếm | Tìm kiếm | Khu vực | Lọc theo cấp hành chính (tỉnh/thành phố); hỗ trợ tìm kiếm theo bán kính vị trí hiện tại: 1km, 3km, 5km; ưu tiên hiển thị bài đăng gần người kiếm nhất | |
| 21 | Người dùng | Tìm kiếm | Tìm kiếm | Hình thức giao dịch | Bộ lọc checkbox gồm 3 hình thức: mua bán, trao đổi, cho/tặng; có thể chọn một hoặc kết hợp nhiều hình thức | |
| 22 | Người dùng | Tìm kiếm | Kênh xem | Kênh chung | Hiển thị mọi tin đăng bài mới nhất toàn app và được hệ thống đề xuất phù hợp | |
| 23 | Người dùng | Tìm kiếm | Kênh xem | Kênh đang theo dõi | Chỉ hiển thị tin đăng bài từ những người dùng đã follow | |
| 24 | Người dùng | Tìm kiếm | Lọc | Giá thấp đến cao | Lọc các bài đăng theo giá thấp đến cao | |
| 25 | Người dùng | Tìm kiếm | Lọc | Giá cao đến thấp | Lọc các bài đăng theo giá cao đến thấp | |
| 26 | Người dùng | Tìm kiếm | Lọc | Mới đăng | Lọc các bài đăng theo thời gian đăng từ gần thời điểm hiện tại nhất tới xa nhất | |
| 27 | Người dùng | Tìm kiếm | Lọc | Đánh giá tốt | Lọc các người bán có đánh giá tốt | |
| 28 | Người dùng | Tìm kiếm | Đề xuất | Danh mục món đồ quan tâm | Khi đăng ký tài khoản, cho người dùng chọn danh mục yêu thích | |
| 29 | Người dùng | Tìm kiếm | Đề xuất | Gợi ý sản phẩm tương tự | Hiển thị sản phẩm tương tự khi xem chi tiết một tin đăng, cùng danh mục, tình trạng, khoảng giá | |
| 30 | Người dùng | Tìm kiếm | Đề xuất | Tạo mục "Dành cho bạn" | Dựa trên lịch sử xem, tìm kiếm, tin bán đã lưu để gợi ý bài đăng phù hợp | |
| 31 | Người dùng | Kết nối | Theo dõi | | Cho phép người dùng theo dõi người dùng khác | |
| 32 | Người dùng | Kết nối | Lưu tin yêu thích | Lưu tin | Cho phép lưu lại một tin đăng để xem lại sau | |
| 33 | Người dùng | Kết nối | Theo dõi |  | Cho phép người dùng theo dõi người dùng khác | |
| 34 | Người dùng | Kết nối | Lưu tin yêu thích | Lưu tin | Cho phép lưu lại một tin đăng để xem lại sau | |
| 35 | Người dùng | Kết nối | Lưu tin yêu thích | Xem danh sách đã lưu | Xem lại toàn bộ tin đăng đã lưu | |
| 36 | Người dùng | Kết nối | Lưu tin yêu thích | Bỏ lưu | Gỡ một tin khỏi danh sách đã lưu | |
| 37 | Người dùng | Chat | Chat cá nhân | Gửi tin nhắn văn bản | Cho phép ngừoi dùng gửi tin nhắn chữ | |
| 38 | Người dùng | Chat | Chat cá nhân | Gửi hình ảnh | Cho phép người dùng gửi hình ảnh | |
| 39 | Người dùng | Chat | Chat cá nhân | Gửi video | Cho phép người dùng gửi video | |
| 40 | Người dùng | Offer một chạm | Đề nghị giá | Đề nghị | Gửi đề nghị mức giá cho người đăng bài | |
| 41 | Người dùng | Offer một chạm | Đề nghị giá | Chấp nhận đề nghị | Người bán chấp nhận đề nghị mức giá mới | |
| 42 | Người dùng | Offer một chạm | Đề nghị giá | Từ chối đề nghị | Người bán từ chối đề nghị mức giá mới | |
| 43 | Người dùng | Giao dịch và thanh toán | Chốt deal | Tạo giao dịch | Kiểm tra hình ảnh, nội dung, mức giá, món đồ giao dịch, phương thức, mức giá thanh toán, điạ điểm giao nhận, thời gian, điều kiện đã thống nhất giữa 2 bên. | |
| 44 | Người dùng | Giao dịch và thanh toán | Chốt deal | Chỉnh sửa thông tin giao dịch | Chỉnh sửa hình ảnh, nội dung, mức giá,phương thức, điạ điểm giao nhận, thời gian. | |
| 45 | Người dùng | Giao dịch và thanh toán | Chốt deal | Xác nhận giao dịch | Xác nhận toàn bộ thông tin đã đúng với thoả thuận | |
| 46 | Người dùng | Giao dịch và thanh toán | Trạng thái giao dịch | Xác nhận đã nhận | Người mua/trao đổi xác nhận đã nhận món đồ | |
| 47 | Người dùng | Giao dịch và thanh toán | Trạng thái giao dịch | Xác nhận đã giao | Người bán/đăng bài xác nhận đã gửi món đồ | |
| 48 | Người dùng | Giao dịch và thanh toán | Trạng thái giao dịch | Huỷ giao dịch có lí do | Bấm huỷ giao dịch trước khi thực hiện gửi món đồ | |
| 49 | Người dùng | Giao dịch và thanh toán | Thanh toán | Thanh toán qua bên thứ 3 | Người dùng gửi tiền vào bên thứ 3, sau đó chờ giải ngân | |
| 50 | Người dùng | Giao dịch và thanh toán | Lịch sử | Lịch sử giao dịch | Thông tin những giao dịch đã thực hiện | |
| 51 | Người dùng | Giao dịch và thanh toán | Lịch sử | Lịch sử thanh toán | Xem lịch sử thanh toán | |
| 52 | Người dùng | Đánh giá | Hệ thống điểm/sao | Đánh giá đối tác | Người dùng có thể đánh giá người thực hiện giao dịch với mình | |
| 53 | Người dùng | Đánh giá | Hệ thống điểm/sao | Xem lịch sử đánh giá | Người dùng có thể xem ai đã đánh giá mình, và mình đã đánh giá ai | |
| 54 | Người dùng | Báo cáo | Báo cáo | Tạo báo cáo | Tạo báo cáo đối với hành vi, phát ngôn của người khác | |
| 55 | Người dùng | Báo cáo | Báo cáo | Huỷ báo cáo |  | |
| 56 | Người dùng | Báo cáo | Báo cáo | Xem lịch sử báo cáo | Cho phép người dùng xem và xuất lịch sử báo cáo | |
| 57 | Người dùng | Khiếu nại | Khiếu nại | Tạo đơn khiếu nại | Cho phép người dùng tạo đơn khiếu nại ( gồm các thông tin, bằng chứng) | |
| 58 | Người dùng | Khiếu nại | Khiếu nại | Theo dõi tình trạng đơn khiếu nại | Cho phép người dùng theo dõi tình trạng đơn khiếu nại | |
| 59 | Người dùng | Khiếu nại | Khiếu nại | Gỡ bỏ đơn khiếu nại | Cho phép người dùng huỷ đơn khiếu nại | |
