# Sổ đăng ký tính năng

Nguồn sự thật về **tính năng nào tồn tại**. AI chỉ làm tính năng có mã `F-xx` ở đây và có trạng thái khác `proposed`. Muốn thêm hoặc đổi trạng thái, mở issue `chore` gắn nhãn `area:docs`; **chỉ leader duyệt** đưa tính năng từ `proposed` lên `planned`.

Tính năng đã có ở đây được **bảo vệ**: xóa hoặc đổi tên code của nó cần issue `type:removal` do leader mở (xem `AGENTS.md`, luật R4).

## Trạng thái

| Trạng thái | Nghĩa |
| --- | --- |
| `proposed` | Mới gợi ý, chưa được duyệt. **Không ai làm** |
| `planned` | Leader đã duyệt, chưa bắt đầu |
| `in-progress` | Đang có issue hoặc Pull Request mở |
| `done` | Đã merge vào `main` |

## Danh sách

14 tính năng, gom từ 87 chức năng "Đợt này" của [bảng đặc tả chức năng](dac-ta-chuc-nang.md) (88 chức năng trừ STT 45, gửi video, đã chuyển sang Phát triển sau). 15 chức năng Phát triển sau (STT 45, 83, 85, 86, 92 đến 102) không có mã ở đây và không ai làm. Mọi dòng đã được leader duyệt ở trạng thái `planned`.

| Mã | Tính năng | STT | Trạng thái | Người làm | Issue | Chạm tới |
| --- | --- | --- | --- | --- | --- | --- |
| F-01 | Đăng ký, đăng nhập (email và Google), quên và đổi mật khẩu, chọn danh mục quan tâm | 1, 2, 3, 11, 35 | planned | Nguyễn Thùy Ngân | | Firebase Auth; `users`, `identity`, `categories` |
| F-02 | Hồ sơ cá nhân, ảnh, địa chỉ, định danh, cài đặt thông báo, vô hiệu hóa tài khoản | 4, 5, 6, 9, 10, 12 | planned | Nguyễn Thùy Ngân | | `users`, `addresses`, `identity`, `images` |
| F-03 | Thông báo trong ứng dụng | 67 | planned | Nguyễn Thùy Ngân | | `notifications` |
| F-04 | Đăng tin, lưu nháp, kệ hàng | 13 đến 22 | planned | Trần Anh Tú | | `listings`, `categories`, `images` |
| F-05 | Tìm kiếm, bộ lọc, sắp xếp, kênh xem | 23 đến 34 | planned | Trần Anh Tú | | `listings`, `follows`; Room `listing_cache`, `recent_search` |
| F-06 | Gợi ý: tin tương tự, giá tham khảo, Dành cho bạn | 36, 37, 38 | planned | Trần Anh Tú | | `listings`; Room `recent_view`, `recent_search` |
| F-07 | Lưu tin | 40, 41, 42 | planned | Trần Anh Tú | | `savedListings` |
| F-08 | Chat chữ và ảnh, trạng thái hoạt động | 43, 44, 46 | planned | Nguyễn Lê Hải Long | | `conversations`, `messages`, `images`; Room `conversation_cache`, `message_cache` |
| F-09 | Offer một chạm và giao dịch | 47 đến 55 | planned | Nguyễn Lê Hải Long | | `offers`, `deals`, `images`; Room `deal_cache` |
| F-10 | Ký quỹ mô phỏng, lịch sử giao dịch và thanh toán, đánh giá | 56 đến 59 | planned | Nguyễn Lê Hải Long | | `payments`, `deals`, `reviews` |
| F-11 | Hồ sơ công khai, uy tín, theo dõi | 7, 8, 39, 60 | planned | Nguyễn Hoàng Phúc | | `users`, `follows`, `reviews` |
| F-12 | Báo cáo và khiếu nại | 61 đến 66 | planned | Nguyễn Hoàng Phúc | | `reports`, `complaints`, `images` |
| F-13 | Admin xử lý khiếu nại | 87 đến 91 | planned | Nguyễn Hoàng Phúc | | `complaints`, `adminNote`, `notifications` |
| F-14 | Admin người dùng, nội dung, danh mục, giao dịch, giải ngân tự động | 68 đến 82, 84 | planned | Trần Đức Phát | | `users`, `warnings`, `sellerVerifications`, `admins`, `activityLogs`, `reports`, `bannedKeywords`, `categories`, `listings`, `deals`, `payments` |

STT 73 (xác minh người bán) thuộc F-14 ở phía admin; phía người dùng (nút gửi yêu cầu) do Phúc làm cùng F-11, theo [so-huu.md](../../plan/so-huu.md).

## Yêu cầu môn học

Ba yêu cầu này cắt ngang mọi tính năng, không có mã `F-xx` và không phải chức năng trong bảng đặc tả (trừ đăng nhập Google).

| Yêu cầu | Cách đáp ứng |
| --- | --- |
| Dùng Firebase | Authentication, Firestore, gói Spark (không dùng Cloud Storage) |
| Giao diện thích ứng khi xoay màn hình, giữ nguyên trạng thái | Layout `layout-land` hoặc `ConstraintLayout`; trạng thái nằm trong ViewModel |
| Xem lại trang đã xem khi mất mạng (SQLite) | Room làm bộ nhớ đệm, xem [room-schema.md](../data/room-schema.md) |
| Đăng nhập bằng tài khoản Google | STT 2, thuộc F-01 |

Cột "Chạm tới" dùng tên collection và bảng thật trong [firestore-schema.md](../data/firestore-schema.md) và [room-schema.md](../data/room-schema.md); đổi chúng cần hỏi leader (`AGENTS.md`, luật R6).
