# Bảng khái niệm và định nghĩa

Các thuật ngữ dùng xuyên suốt dự án PUBGApp, để cả nhóm hiểu thống nhất.

| Thuật ngữ | Định nghĩa | Ví dụ |
| --- | --- | --- |
| Tin đăng (listing) | Một bài viết do người bán tạo ra trên ứng dụng để giới thiệu món đồ cần bán, trao đổi hoặc cho tặng. Mỗi tin đăng chứa tiêu đề, mô tả, ảnh, giá và tình trạng. | Tin bán laptop cũ, giá 5 triệu, tình trạng "đã qua sử dụng" |
| Kệ hàng (store shelf) | Tập hợp tất cả tin đăng đang hoạt động của một người bán, hiển thị trên trang cá nhân của họ. | Kệ hàng của người bán A có 12 tin đăng, gồm quần áo và đồ điện tử |
| Offer (đề nghị giá) | Đề xuất mà người mua gửi cho người bán để thương lượng giá bán khác với giá niêm yết. Người bán có thể chấp nhận, từ chối hoặc đề xuất lại. | Tin niêm yết 500.000đ, người mua đề nghị 400.000đ |
| Giao dịch (transaction) | Quy trình hoàn chỉnh từ khi người mua xác nhận mua đến khi hàng được giao và tiền được giải ngân. Gồm các trạng thái: chờ xử lý, đang giao, hoàn thành, đã huỷ. | Người mua bấm "Mua ngay", người bán xác nhận, giao hàng, người mua nhận và hoàn tất |
| Ký quỹ (escrow) | Cơ chế giữ tiền tạm thời của người mua trong hệ thống. Tiền chỉ được chuyển cho người bán khi người mua xác nhận đã nhận hàng hoặc hết thời hạn khiếu nại. Trong PUBGApp, ký quỹ được mô phỏng, không dùng tiền thật. | Người mua trả 300.000đ, hệ thống giữ số tiền này cho đến khi người mua bấm "Đã nhận hàng" |
| Giải ngân (disbursement) | Hành động chuyển tiền từ trạng thái ký quỹ sang tài khoản người bán sau khi giao dịch hoàn tất. Trong PUBGApp, giải ngân diễn ra tự động khi người mua xác nhận nhận hàng hoặc hết thời hạn khiếu nại. | Sau khi người mua xác nhận, 300.000đ được tự động cộng vào số dư người bán |
| Mã vận đơn (tracking number) | Dãy ký tự do người bán nhập để người mua theo dõi trạng thái vận chuyển. Người bán tự nhập mã này khi gửi hàng qua đơn vị vận chuyển bên ngoài. | Mã vận đơn GHN: ABCD1234 |
| Khiếu nại (dispute) | Yêu cầu do người mua hoặc người bán gửi lên hệ thống khi phát sinh vấn đề trong giao dịch (hàng không đúng mô tả, hư hỏng, không nhận được hàng). Quản trị viên xem xét và đưa ra phán quyết. | Người mua khiếu nại vì nhận được điện thoại bị vỡ màn hình, khác mô tả trong tin đăng |
| Báo cáo (report) | Hành động thông báo cho hệ thống về nội dung vi phạm, chẳng hạn tin đăng lừa đảo, hàng cấm hoặc hành vi quấy rối. Quản trị viên sẽ xem xét và xử lý. | Người dùng bấm "Báo cáo tin đăng" vì nghi ngờ hàng giả |
| Đánh giá uy tín (reputation rating) | Điểm số và nhận xét mà người mua để lại cho người bán (và ngược lại) sau khi hoàn tất giao dịch. Thang điểm từ 1 đến 5 sao kèm bình luận. Điểm này hiển thị công khai trên hồ sơ. | Sau khi nhận hàng, người mua đánh giá người bán 5 sao kèm lời nhận xét "Giao hàng nhanh, đóng gói cẩn thận" |
| Danh mục (category) | Nhóm phân loại các tin đăng theo chủ đề để người dùng dễ tìm kiếm và duyệt. Danh mục do quản trị viên tạo và quản lý. | Điện tử, Quần áo, Sách, Đồ gia dụng, Xe cộ |
| Tình trạng món đồ (item condition) | Mô tả mức độ mới hoặc cũ của món đồ được rao bán, do người bán tự khai khi tạo tin đăng. | Mới 100%, Như mới (dùng dưới 1 tháng), Đã qua sử dụng (còn tốt), Cần sửa chữa |
| Người mua (buyer) | Người dùng thực hiện hành động tìm kiếm, xem, nhắn tin và mua đồ trên ứng dụng. Một tài khoản có thể vừa là người mua vừa là người bán. | Ngân tìm và mua chiếc áo trên PUBGApp |
| Người bán (seller) | Người dùng tạo tin đăng để bán, trao đổi hoặc cho tặng đồ cũ. Một tài khoản có thể vừa là người mua vừa là người bán. | Long đăng bán chiếc bàn học cũ trên PUBGApp |
| Quản trị viên (admin) | Người có quyền quản lý hệ thống: duyệt hoặc ẩn tin đăng, xử lý khiếu nại, quản lý danh mục và giám sát giao dịch. Thao tác đơn giản, không có thống kê phức tạp. | Quản trị viên ẩn một tin đăng bị nhiều người báo cáo |
| Theo dõi (follow) | Hành động đánh dấu một người bán để nhận thông báo khi họ đăng tin mới. | Ngân theo dõi shop của Phúc để biết khi nào có đồ công nghệ mới |
| Tin đã lưu (saved listing) | Tin đăng mà người dùng đánh dấu để xem lại sau, lưu vào danh sách riêng trên tài khoản. | Ngân lưu 3 tin bán laptop để so sánh giá trước khi quyết định mua |
