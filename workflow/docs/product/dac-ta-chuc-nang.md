# Bảng đặc tả chức năng

Nguồn sự thật về **hệ thống làm những gì**. Mọi tài liệu khác (sơ đồ chức năng, sơ đồ lớp, ERD, giao diện, sổ tính năng) nhắc tới một chức năng đều dùng **số STT của bảng này**.

- **102 chức năng**: 87 chức năng **Đợt này**, làm trong giai đoạn phát triển sắp tới; 15 chức năng **Phát triển sau**, giữ lại làm hướng phát triển, **không code trong giai đoạn này**.
- Nguồn: bảng chức năng gốc của nhóm (92 dòng) và 10 chức năng bổ sung theo mockup giao diện, ghi "Bổ sung theo mockup" ở cột Ghi chú. Đánh số lại ngày 2026-10-11; bảng đối chiếu với số dòng của bảng gốc nằm ở cuối tài liệu.
- Cột **Ghi chú** ghi các quyết định thiết kế đã chốt (ví dụ giải ngân tự động, bằng chứng vận đơn). Khi ghi chú và mockup giao diện khác nhau, ghi chú đúng.

| STT | User | Module | Tên chức năng | Chức năng con | Đặc tả | Phạm vi | Ghi chú |
| --- | --- | --- | --- | --- | --- | --- | --- |
| 1 | Người dùng | Quản lý tài khoản và hồ sơ | Đăng ký / đăng nhập | Đăng ký | Tạo tài khoản người dùng mới | Đợt này | Xác thực email. Khai báo họ tên, email, số điện thoại, khu vực, mật khẩu; đồng ý Điều khoản sử dụng và Chính sách bảo mật (văn bản tĩnh trong ứng dụng). Số CCCD khai lúc đăng ký được cất riêng tư (xem Khai báo thông tin định danh) |
| 2 | Người dùng | Quản lý tài khoản và hồ sơ | Đăng ký / đăng nhập | Đăng nhập | Truy cập vào ứng dụng bằng email và mật khẩu đã đăng ký, hoặc bằng tài khoản Google | Đợt này | Đăng nhập Google là yêu cầu của môn học. Không đăng nhập bằng số điện thoại |
| 3 | Người dùng | Quản lý tài khoản và hồ sơ | Đăng ký / đăng nhập | Quên mật khẩu | Xác thực người dùng và cho phép đặt lại mật khẩu | Đợt này | Gửi email chứa đường dẫn đặt lại mật khẩu. Không gửi mã qua SMS |
| 4 | Người dùng | Quản lý tài khoản và hồ sơ | Quản lý trang cá nhân | Chỉnh sửa thông tin cá nhân | CRUD thông tin cá nhân: họ tên, số điện thoại, khu vực, giới thiệu | Đợt này |  |
| 5 | Người dùng | Quản lý tài khoản và hồ sơ | Quản lý trang cá nhân | Chỉnh sửa ảnh hồ sơ | CRUD ảnh đại diện | Đợt này |  |
| 6 | Người dùng | Quản lý tài khoản và hồ sơ | Quản lý trang cá nhân | Chỉnh sửa địa chỉ | CRUD địa chỉ giao nhận, chọn một địa chỉ mặc định | Đợt này |  |
| 7 | Người dùng | Quản lý tài khoản và hồ sơ | Quản lý trang cá nhân | Xem đánh giá cá nhân | Hiển thị công khai điểm đánh giá bản thân | Đợt này |  |
| 8 | Người dùng | Quản lý tài khoản và hồ sơ | Quản lý trang cá nhân | Xem số liệu hồ sơ | Hiển thị số món đã bán, số người theo dõi, số giao dịch và tỷ lệ hoàn tất | Đợt này | Bổ sung theo mockup D01, D02, D03. |
| 9 | Người dùng | Quản lý tài khoản và hồ sơ | Quản lý trang cá nhân | Khai báo thông tin định danh | Khai báo số CCCD, ngày sinh, giới tính, địa chỉ thường trú để xác minh danh tính | Đợt này | Bổ sung theo mockup A02, A04. Cất riêng tư: chỉ chủ tài khoản và admin xem được. |
| 10 | Người dùng | Quản lý tài khoản và hồ sơ | Cài đặt tài khoản | Cài đặt thông báo | Bật hoặc tắt từng loại thông báo: tin nhắn mới, đề nghị giá mới, cập nhật giao dịch | Đợt này | Bổ sung theo mockup A05. Mục khuyến mãi, tin tức chưa làm vì cần gửi broadcast (phát triển sau). |
| 11 | Người dùng | Quản lý tài khoản và hồ sơ | Cài đặt tài khoản | Đổi mật khẩu | Đổi mật khẩu sau khi nhập lại mật khẩu hiện tại | Đợt này | Bổ sung theo mockup A05. |
| 12 | Người dùng | Quản lý tài khoản và hồ sơ | Cài đặt tài khoản | Xóa tài khoản | Vô hiệu hóa tài khoản: ẩn hồ sơ và tin đăng, xóa thông tin đăng nhập, giữ lịch sử giao dịch và đánh giá cho đối tác | Đợt này | Bổ sung theo mockup A05. Không cho xóa khi còn giao dịch chưa kết thúc. |
| 13 | Người dùng | Quản lý bán hàng | Quản lý kệ hàng | Chỉnh sửa sản phẩm trên kệ | CRUD các thông tin, sản phẩm muốn bán, trạng thái (đã bán, chưa bán, ...) | Đợt này | Trạng thái tin: đang bán, đang giữ chỗ, đã bán, ẩn, đã gỡ, nháp |
| 14 | Người dùng | Quản lý bán hàng | Quản lý kệ hàng | Sắp xếp sản phẩm | Cho phép chỉnh sửa các sản phẩm hiển thị theo danh mục, thời gian | Đợt này |  |
| 15 | Người dùng | Quản lý bán hàng | Quản lý kệ hàng | Ẩn hoặc hiện lại tin | Người bán tạm ẩn tin của mình khỏi kênh xem và hiện lại khi muốn | Đợt này | Bổ sung theo mockup B05. |
| 16 | Người dùng | Quản lý bán hàng | Quản lý bài đăng | Chỉnh sửa nội dung | Mô tả tình trạng món đồ và yêu cầu nếu có | Đợt này | Tình trạng chọn 1 trong 5: Mới, Như mới, Đã sử dụng, Đã sửa chữa, Có lỗi; kèm thuộc tính chi tiết theo danh mục |
| 17 | Người dùng | Quản lý bán hàng | Quản lý bài đăng | Chỉnh sửa hình ảnh | Thêm hình ảnh chụp từ máy hoặc chọn từ thư viện | Đợt này | Tối đa 5 ảnh mỗi tin |
| 18 | Người dùng | Quản lý bán hàng | Quản lý bài đăng | Chỉnh sửa hình thức giao dịch | Chọn hình thức giao dịch mong muốn dạng ô đánh dấu (Bán, Trao đổi, Cho tặng), cho phép kết hợp (vừa đổi đồ vừa kèm theo tiền) | Đợt này |  |
| 19 | Người dùng | Quản lý bán hàng | Quản lý bài đăng | Chỉnh sửa giá cả | Cho phép người đăng nhập giá bán | Đợt này |  |
| 20 | Người dùng | Quản lý bán hàng | Quản lý bài đăng | Offer mong muốn | Cho phép người bán chọn các loại offer mong muốn (tiền, vật trao đổi, vừa tiền vừa vật) và giới hạn mức giá offer tối thiểu | Đợt này | Mockup B04 chưa vẽ phần này, vẫn phải làm |
| 21 | Người dùng | Quản lý bán hàng | Quản lý bài đăng | Danh mục | Chọn danh mục cho món đồ | Đợt này |  |
| 22 | Người dùng | Quản lý bán hàng | Quản lý bài đăng | Lưu nháp | Lưu tin đang soạn dưới dạng nháp, chưa hiển thị cho người khác, đăng sau | Đợt này | Bổ sung theo mockup B05. |
| 23 | Người dùng | Tìm kiếm | Tìm kiếm | Từ khóa | Tìm kiếm vật phẩm theo từ khóa | Đợt này |  |
| 24 | Người dùng | Tìm kiếm | Tìm kiếm | Danh mục | Người dùng chọn một danh mục, hệ thống trả về toàn bộ tin đăng thuộc danh mục đó | Đợt này |  |
| 25 | Người dùng | Tìm kiếm | Tìm kiếm | Khoảng giá | Người dùng nhập giá tối thiểu và giá tối đa; có các mốc gợi ý nhanh | Đợt này |  |
| 26 | Người dùng | Tìm kiếm | Tìm kiếm | Tình trạng | Lọc nhiều lựa chọn theo tình trạng: Mới, Như mới, Đã sử dụng, Đã sửa chữa, Có lỗi | Đợt này |  |
| 27 | Người dùng | Tìm kiếm | Tìm kiếm | Khu vực | Lọc theo tỉnh hoặc thành phố; tìm theo bán kính từ vị trí hiện tại (1 km, 3 km, 5 km); ưu tiên hiển thị tin ở gần | Đợt này | Mockup B02 chưa vẽ bộ lọc này, vẫn phải làm |
| 28 | Người dùng | Tìm kiếm | Tìm kiếm | Hình thức giao dịch | Bộ lọc gồm 3 hình thức: bán, trao đổi, cho tặng; chọn một hoặc kết hợp nhiều hình thức | Đợt này |  |
| 29 | Người dùng | Tìm kiếm | Kênh xem | Kênh chung | Hiển thị mọi tin đăng bài mới nhất toàn app và được hệ thống đề xuất phù hợp | Đợt này |  |
| 30 | Người dùng | Tìm kiếm | Kênh xem | Kênh đang theo dõi | Chỉ hiển thị tin đăng bài từ những người dùng đã follow | Đợt này |  |
| 31 | Người dùng | Tìm kiếm | Lọc | Giá thấp đến cao | Lọc các bài đăng theo giá thấp đến cao | Đợt này | Thực chất là sắp xếp kết quả. Mockup chưa vẽ, vẫn phải làm |
| 32 | Người dùng | Tìm kiếm | Lọc | Giá cao đến thấp | Lọc các bài đăng theo giá cao đến thấp | Đợt này | Thực chất là sắp xếp kết quả |
| 33 | Người dùng | Tìm kiếm | Lọc | Mới đăng | Lọc các bài đăng theo thời gian đăng từ gần thời điểm hiện tại nhất tới xa nhất | Đợt này | Thực chất là sắp xếp kết quả |
| 34 | Người dùng | Tìm kiếm | Lọc | Đánh giá tốt | Lọc các người bán có đánh giá tốt | Đợt này | Thực chất là sắp xếp kết quả |
| 35 | Người dùng | Tìm kiếm | Đề xuất | Danh mục món đồ quan tâm | Khi đăng ký tài khoản, cho người dùng chọn danh mục quan tâm | Đợt này |  |
| 36 | Người dùng | Tìm kiếm | Đề xuất | Gợi ý sản phẩm tương tự | Hiển thị sản phẩm tương tự khi xem chi tiết một tin: cùng danh mục, tình trạng, khoảng giá | Đợt này |  |
| 37 | Người dùng | Tìm kiếm | Đề xuất | Giá tham khảo | Hiển thị khoảng giá tham khảo khi xem chi tiết tin, tính từ các tin tương tự cùng danh mục và tình trạng | Đợt này | Bổ sung theo mockup B03. Tính trên máy, không lưu. |
| 38 | Người dùng | Tìm kiếm | Đề xuất | Tạo mục "Dành cho bạn" | Gợi ý tin đăng dựa trên lịch sử xem, lịch sử tìm kiếm và tin đã lưu | Đợt này | Lịch sử xem và tìm kiếm chỉ lưu trên máy |
| 39 | Người dùng | Kết nối | Theo dõi | Theo dõi người dùng | Cho phép người dùng theo dõi người dùng khác | Đợt này |  |
| 40 | Người dùng | Kết nối | Lưu tin yêu thích | Lưu tin | Cho phép lưu lại một tin đăng để xem lại sau | Đợt này |  |
| 41 | Người dùng | Kết nối | Lưu tin yêu thích | Xem danh sách đã lưu | Xem lại toàn bộ tin đăng đã lưu | Đợt này |  |
| 42 | Người dùng | Kết nối | Lưu tin yêu thích | Bỏ lưu | Gỡ một tin khỏi danh sách đã lưu | Đợt này |  |
| 43 | Người dùng | Kết nối | Chat | Gửi tin nhắn văn bản | Cho phép người dùng gửi tin nhắn chữ | Đợt này |  |
| 44 | Người dùng | Kết nối | Chat | Gửi hình ảnh | Cho phép người dùng gửi hình ảnh | Đợt này |  |
| 45 | Người dùng | Kết nối | Chat | Gửi video | Cho phép người dùng gửi video | **Phát triển sau** | Chuyển sang Phát triển sau ngày 2026-10-12: gói Firebase Spark không có Cloud Storage, video không lưu được trong Firestore |
| 46 | Người dùng | Kết nối | Chat | Trạng thái hoạt động | Hiển thị người đối thoại đang hoạt động hay thời điểm hoạt động gần nhất | Đợt này | Bổ sung theo mockup C02. |
| 47 | Người dùng | Kết nối | Offer (đề nghị 1 chạm) | Gửi đề nghị | Gửi đề nghị cho người đăng bài theo loại người bán chấp nhận: tiền, vật trao đổi, hoặc cả hai | Đợt này | Đề nghị chỉ được chấp nhận hoặc từ chối; không có hủy, không có đề xuất lại |
| 48 | Người dùng | Kết nối | Offer (đề nghị 1 chạm) | Chấp nhận đề nghị | Người bán chấp nhận đề nghị | Đợt này | Chấp nhận thì hệ thống tạo giao dịch |
| 49 | Người dùng | Kết nối | Offer (đề nghị 1 chạm) | Từ chối đề nghị | Người bán từ chối đề nghị | Đợt này |  |
| 50 | Người dùng | Giao dịch, thanh toán | Tạo giao dịch | Xem thông tin giao dịch | Kiểm tra hình ảnh, nội dung, mức giá, món đồ giao dịch, phương thức, mức giá thanh toán, địa điểm giao nhận, thời gian, điều kiện đã thống nhất giữa hai bên | Đợt này | Giao dịch được tạo khi người bán chấp nhận đề nghị |
| 51 | Người dùng | Giao dịch, thanh toán | Tạo giao dịch | Chỉnh sửa thông tin giao dịch | Chỉnh sửa hình ảnh, nội dung, mức giá, phương thức, địa điểm giao nhận, thời gian | Đợt này |  |
| 52 | Người dùng | Giao dịch, thanh toán | Tạo giao dịch | Xác nhận giao dịch | Xác nhận toàn bộ thông tin đã đúng với thoả thuận | Đợt này |  |
| 53 | Người dùng | Giao dịch, thanh toán | Tạo giao dịch | Xác nhận đã nhận | Người mua hoặc người trao đổi xác nhận đã nhận món đồ | Đợt này | Giao dịch sang Đã nhận và tiền ký quỹ chuyển ngay cho người bán. Người mua không xác nhận thì hệ thống tự chuyển khi hết đếm ngược. Giao dịch Hoàn tất khi hai bên đã đánh giá hoặc sau 3 ngày |
| 54 | Người dùng | Giao dịch, thanh toán | Tạo giao dịch | Xác nhận đã giao | Người bán hoặc người đăng bài xác nhận đã gửi món đồ | Đợt này | Bắt buộc ảnh mã vận đơn, tên đơn vị vận chuyển và mã vận đơn; bắt đầu đếm ngược tự giải ngân |
| 55 | Người dùng | Giao dịch, thanh toán | Tạo giao dịch | Hủy giao dịch có lý do | Hủy giao dịch trước khi gửi món đồ, bắt buộc nêu lý do | Đợt này | Hoàn tiền nếu tiền đang được giữ |
| 56 | Người dùng | Giao dịch, thanh toán | Thanh toán | Thanh toán qua bên thứ ba | Người mua gửi tiền vào bên thứ ba (ví trung gian), tiền được giữ tới khi giải ngân | Đợt này | Ký quỹ mô phỏng: không có cổng thanh toán thật, không có số dư ví. Giao dịch trao đổi hoặc cho tặng không cần thanh toán |
| 57 | Người dùng | Giao dịch, thanh toán | Lịch sử giao dịch | Xem lịch sử giao dịch | Xem những giao dịch đã thực hiện, ở cả vai trò người mua và người bán | Đợt này |  |
| 58 | Người dùng | Giao dịch, thanh toán | Lịch sử thanh toán | Xem lịch sử thanh toán | Xem lịch sử thanh toán | Đợt này |  |
| 59 | Người dùng | Đánh giá & bảo vệ giao dịch | Đánh giá | Đánh giá đối tác | Người dùng có thể đánh giá người thực hiện giao dịch với mình | Đợt này | Chấm sao tổng thể và 3 tiêu chí: độ chính xác thông tin, thái độ giao dịch, đúng cam kết. Mỗi bên đánh giá một lần |
| 60 | Người dùng | Đánh giá & bảo vệ giao dịch | Đánh giá | Xem lịch sử đánh giá | Người dùng có thể xem ai đã đánh giá mình, và mình đã đánh giá ai | Đợt này |  |
| 61 | Người dùng | Đánh giá & bảo vệ giao dịch | Báo cáo | Tạo báo cáo | Tạo báo cáo đối với tin đăng, hành vi hoặc phát ngôn của người khác | Đợt này | Chọn lý do có sẵn: nghi ngờ hàng giả, thông tin sai lệch, dấu hiệu lừa đảo, nội dung không phù hợp, khác; kèm mô tả thêm |
| 62 | Người dùng | Đánh giá & bảo vệ giao dịch | Báo cáo | Hủy báo cáo | Hủy một báo cáo mình đã gửi khi báo cáo chưa được xử lý | Đợt này |  |
| 63 | Người dùng | Đánh giá & bảo vệ giao dịch | Báo cáo | Xem lịch sử báo cáo | Cho phép người dùng xem và xuất lịch sử báo cáo | Đợt này |  |
| 64 | Người dùng | Đánh giá & bảo vệ giao dịch | Khiếu nại & tranh chấp | Tạo đơn khiếu nại | Tạo đơn khiếu nại cho một giao dịch, gồm lý do, mô tả và ảnh bằng chứng | Đợt này | Lý do có sẵn: hàng không đúng mô tả, không nhận được hàng, hàng bị hư hỏng, khác; tối thiểu 1 ảnh. Giao dịch tạm đóng băng và dừng đếm ngược |
| 65 | Người dùng | Đánh giá & bảo vệ giao dịch | Khiếu nại & tranh chấp | Theo dõi tình trạng đơn khiếu nại | Cho phép người dùng theo dõi tình trạng đơn khiếu nại | Đợt này |  |
| 66 | Người dùng | Đánh giá & bảo vệ giao dịch | Khiếu nại & tranh chấp | Gỡ bỏ đơn khiếu nại | Cho phép người dùng hủy đơn khiếu nại | Đợt này |  |
| 67 | Người dùng | Thông báo | Thông báo | Xem thông báo | Xem danh sách thông báo về đề nghị giá, giao dịch, tin nhắn, đánh giá, khiếu nại, cảnh báo; đánh dấu đã đọc | Đợt này | Bổ sung theo mockup C06. Thông báo bảo trì hệ thống cần gửi broadcast (phát triển sau). |
| 68 | Admin | Quản lý người dùng | Danh sách người dùng | Tìm kiếm, lọc người dùng | Lọc theo trạng thái, ngày tham gia app, khu vực | Đợt này |  |
| 69 | Admin | Quản lý người dùng | Hồ sơ người dùng | Xem chi tiết hồ sơ | Thông tin cá nhân, lịch sử đăng tin, giao dịch, đánh giá, vi phạm | Đợt này |  |
| 70 | Admin | Quản lý người dùng | Khóa/mở khóa tài khoản | Khóa tài khoản | Khóa các tài khoản vi phạm (tạm thời, vĩnh viễn) | Đợt này |  |
| 71 | Admin | Quản lý người dùng | Khóa/mở khóa tài khoản | Mở khóa tài khoản | Mở khóa tài khoản bị khóa | Đợt này |  |
| 72 | Admin | Quản lý người dùng | Khóa/mở khóa tài khoản | Cảnh báo theo bậc | Vi phạm lần 1 cảnh báo, lần 2 khóa tạm, lần 3 khóa vĩnh viễn | Đợt này | Quy tắc cố định 3 bậc, không cấu hình được |
| 73 | Admin | Quản lý người dùng | Xác minh tài khoản | Duyệt yêu cầu xác minh | Áp dụng nếu người dùng đã xác thực email và muốn đăng ký trở thành người bán hàng | Đợt này |  |
| 74 | Admin | Quản lý người dùng | Nhật ký hoạt động | Xem nhật ký hoạt động | Theo dõi hành vi một tài khoản cụ thể | Đợt này |  |
| 75 | Admin | Kiểm duyệt nội dung | Xử lý tin bị báo cáo | Danh sách tin bị báo cáo | Kèm số lượt báo cáo, lý do | Đợt này |  |
| 76 | Admin | Kiểm duyệt nội dung | Xử lý tin bị báo cáo | Ẩn, gỡ tin vi phạm | Có lý do, có thể khôi phục nếu duyệt oan | Đợt này |  |
| 77 | Admin | Kiểm duyệt nội dung | Xử lý tin bị báo cáo | Gỡ hàng loạt | Gỡ toàn bộ tin của tài khoản bị khóa | Đợt này |  |
| 78 | Admin | Kiểm duyệt nội dung | Lọc từ khóa cấm | Danh sách từ khóa cấm | Tự động gắn cờ tin cần chú ý khi chứa từ khóa cấm, so khớp chuỗi | Đợt này |  |
| 79 | Admin | Quản lý danh mục | Quản lý danh mục sản phẩm | Thêm, sửa, xóa danh mục | Gồm danh mục con | Đợt này | Hiển thị số tin đăng trong mỗi danh mục |
| 80 | Admin | Quản lý danh mục | Quản lý danh mục sản phẩm | Cấu hình thuộc tính tình trạng | Theo từng danh mục, khớp conditionFields | Đợt này |  |
| 81 | Admin | Giám sát giao dịch | Theo dõi giao dịch | Danh sách giao dịch | Lọc theo trạng thái | Đợt này |  |
| 82 | Admin | Giám sát giao dịch | Theo dõi giao dịch | Chi tiết giao dịch | Gồm statusHistory | Đợt này |  |
| 83 | Admin | Giám sát giao dịch | Theo dõi giao dịch | Cảnh báo giao dịch bất thường | Đứng yên quá lâu, hoặc một tài khoản hủy liên tiếp nhiều lần | **Phát triển sau** |  |
| 84 | Admin | Giám sát giao dịch | Giải ngân | Giải ngân tự động | Tiền ký quỹ tự chuyển cho người bán khi người mua xác nhận đã nhận hoặc khi hết đếm ngược; admin chỉ theo dõi | Đợt này | Thay cho "Duyệt yêu cầu giải ngân" trong bảng gốc: admin không duyệt tay |
| 85 | Admin | Giám sát giao dịch | Can thiệp giao dịch | Đổi trạng thái thủ công | Trường hợp đặc biệt, có ghi log lý do | **Phát triển sau** |  |
| 86 | Admin | Giám sát giao dịch | Phê duyệt giao dịch | Duyệt hoặc từ chối ở bước chờ duyệt |  | **Phát triển sau** |  |
| 87 | Admin | Xử lý báo cáo, khiếu nại | Danh sách khiếu nại | Khiếu nại chờ xử lý, đã xử lý | Phân loại mức ưu tiên | Đợt này | Phân loại mức ưu tiên |
| 88 | Admin | Xử lý báo cáo, khiếu nại | Hồ sơ khiếu nại | Xem chi tiết hồ sơ | Lịch sử chat, ảnh bằng chứng, thông tin giao dịch liên quan | Đợt này |  |
| 89 | Admin | Xử lý báo cáo, khiếu nại | Ra quyết định xử lý | Hoàn tiền, hoàn trả, hủy, bác khiếu nại, yêu cầu bổ sung bằng chứng | Hoàn tiền, hoàn trả, hủy, bác khiếu nại, yêu cầu bổ sung bằng chứng | Đợt này |  |
| 90 | Admin | Xử lý báo cáo, khiếu nại | Ra quyết định xử lý | Ghi chú nội bộ | Không hiển thị cho người dùng | Đợt này |  |
| 91 | Admin | Xử lý báo cáo, khiếu nại | Ra quyết định xử lý | Gửi thông báo kết quả | Gửi thông báo kết quả cho các bên liên quan | Đợt này |  |
| 92 | Admin | Thống kê, báo cáo | Dashboard tổng quan | Số liệu tổng quan |  | **Phát triển sau** |  |
| 93 | Admin | Thống kê, báo cáo | Dashboard tổng quan | Biểu đồ xu hướng |  | **Phát triển sau** |  |
| 94 | Admin | Thống kê, báo cáo | Dashboard tổng quan | Thống kê theo danh mục |  | **Phát triển sau** |  |
| 95 | Admin | Thống kê, báo cáo | Dashboard tổng quan | Tỷ lệ thành công, hủy, tranh chấp |  | **Phát triển sau** |  |
| 96 | Admin | Thống kê, báo cáo | Dashboard tổng quan | Xuất báo cáo | Dạng excel | **Phát triển sau** |  |
| 97 | Admin | Cấu hình hệ thống | Cấu hình tham số vận hành | Thiết lập thời hạn khiếu nại, thời hạn tự động hoàn tất |  | **Phát triển sau** |  |
| 98 | Admin | Cấu hình hệ thống | Quản lý nội dung tĩnh | Điều khoản, chính sách, FAQ |  | **Phát triển sau** | Văn bản điều khoản vẫn có sẵn trong ứng dụng; đây là chức năng để admin sửa nội dung đó |
| 99 | Admin | Cấu hình hệ thống | Thông báo hệ thống | Gửi broadcast | Ví dụ thông báo bảo trì | **Phát triển sau** |  |
| 100 | Admin | Nhật ký, bảo mật quản trị | Nhật ký hoạt động admin | Ghi log thao tác admin | Ai làm gì, khi nào, trên đối tượng nào | **Phát triển sau** |  |
| 101 | Admin | Nhật ký, bảo mật quản trị | Phân quyền admin | Phân vai trò | Ví dụ Moderator chỉ xử lý nội dung, không được duyệt giải ngân | **Phát triển sau** |  |
| 102 | Admin | Nhật ký, bảo mật quản trị | Quản lý phiên đăng nhập | Quản lý phiên đăng nhập admin |  | **Phát triển sau** |  |

## Chức năng phát triển sau

15 chức năng dưới đây **không được đưa vào giai đoạn phát triển sắp tới**: không có trong sổ tính năng, không có lớp riêng trên sơ đồ lớp, không có màn hình riêng. Mockup có vẽ phần nào thuộc nhóm này thì tài liệu giao diện ghi rõ "phát triển sau, chưa làm".

| STT | Chức năng |
| --- | --- |
| 45 | Chat: Gửi video |
| 83 | Theo dõi giao dịch: Cảnh báo giao dịch bất thường |
| 85 | Can thiệp giao dịch: Đổi trạng thái thủ công |
| 86 | Phê duyệt giao dịch: Duyệt hoặc từ chối ở bước chờ duyệt |
| 92 | Dashboard tổng quan: Số liệu tổng quan |
| 93 | Dashboard tổng quan: Biểu đồ xu hướng |
| 94 | Dashboard tổng quan: Thống kê theo danh mục |
| 95 | Dashboard tổng quan: Tỷ lệ thành công, hủy, tranh chấp |
| 96 | Dashboard tổng quan: Xuất báo cáo |
| 97 | Cấu hình tham số vận hành: Thiết lập thời hạn khiếu nại, thời hạn tự động hoàn tất |
| 98 | Quản lý nội dung tĩnh: Điều khoản, chính sách, FAQ |
| 99 | Thông báo hệ thống: Gửi broadcast |
| 100 | Nhật ký hoạt động admin: Ghi log thao tác admin |
| 101 | Phân quyền admin: Phân vai trò |
| 102 | Quản lý phiên đăng nhập: Quản lý phiên đăng nhập admin |

## Đối chiếu với số dòng của bảng gốc

Chỉ dùng để tra tài liệu cũ. Từ ngày 2026-10-11, mọi tài liệu dùng số STT ở trên.

| STT | Dòng bảng gốc | STT | Dòng bảng gốc | STT | Dòng bảng gốc |
| --- | --- | --- | --- | --- | --- |
| 1 | 3 | 35 | 30 | 69 | 61 |
| 2 | 4 | 36 | 31 | 70 | 62 |
| 3 | 5 | 37 | mới | 71 | 63 |
| 4 | 6 | 38 | 32 | 72 | 64 |
| 5 | 7 | 39 | 33 | 73 | 65 |
| 6 | 8 | 40 | 34 | 74 | 66 |
| 7 | 9 | 41 | 35 | 75 | 67 |
| 8 | mới | 42 | 36 | 76 | 68 |
| 9 | mới | 43 | 37 | 77 | 69 |
| 10 | mới | 44 | 38 | 78 | 70 |
| 11 | mới | 45 | 39 | 79 | 71 |
| 12 | mới | 46 | mới | 80 | 72 |
| 13 | 10 | 47 | 40 | 81 | 73 |
| 14 | 11 | 48 | 41 | 82 | 74 |
| 15 | mới | 49 | 42 | 83 | 75 |
| 16 | 12 | 50 | 43 | 84 | 76 |
| 17 | 13 | 51 | 44 | 85 | 77 |
| 18 | 14 | 52 | 45 | 86 | 78 |
| 19 | 15 | 53 | 46 | 87 | 79 |
| 20 | 16 | 54 | 47 | 88 | 80 |
| 21 | 17 | 55 | 48 | 89 | 81 |
| 22 | mới | 56 | 49 | 90 | 82 |
| 23 | 18 | 57 | 50 | 91 | 83 |
| 24 | 19 | 58 | 51 | 92 | 84 |
| 25 | 20 | 59 | 52 | 93 | 85 |
| 26 | 21 | 60 | 53 | 94 | 86 |
| 27 | 22 | 61 | 54 | 95 | 87 |
| 28 | 23 | 62 | 55 | 96 | 88 |
| 29 | 24 | 63 | 56 | 97 | 89 |
| 30 | 25 | 64 | 57 | 98 | 90 |
| 31 | 26 | 65 | 58 | 99 | 91 |
| 32 | 27 | 66 | 59 | 100 | 92 |
| 33 | 28 | 67 | mới | 101 | 93 |
| 34 | 29 | 68 | 60 | 102 | 94 |
