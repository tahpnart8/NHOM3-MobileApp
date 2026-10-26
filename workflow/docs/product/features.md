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

14 tính năng, gom từ 91 chức năng "Đợt này" của [bảng đặc tả chức năng](dac-ta-chuc-nang.md) (106 chức năng trừ 15 chức năng Phát triển sau). 15 chức năng Phát triển sau (STT 45, 83, 85, 86, 92 đến 102) không có mã ở đây và không ai làm. Ba chức năng thêm ngày 2026-10-22 (104 Đăng xuất, 105 Thêm gỡ quyền admin, 106 Đăng nhập khu vực quản trị) đã được xếp vào F-01 và F-14. Mọi dòng đã được leader duyệt ở trạng thái `planned`.

| Mã | Tính năng | STT | Trạng thái | Người làm | Issue | Chạm tới |
| --- | --- | --- | --- | --- | --- | --- |
| F-01 | Đăng ký, đăng nhập (email và Google), đăng xuất, quên và đổi mật khẩu, chọn danh mục quan tâm | 1, 2, 3, 11, 35, 104 | done | Nguyễn Thùy Ngân | #89, #90, #205, #207 | Firebase Auth; `users`, `identity`, `categories` |
| F-02 | Hồ sơ cá nhân, ảnh, địa chỉ, định danh, cài đặt thông báo, vô hiệu hóa tài khoản | 4, 5, 6, 9, 10, 12 | done | Nguyễn Thùy Ngân | #84, #208, #209, #210, #291 | `users`, `addresses`, `identity`, `images` |
| F-03 | Thông báo trong ứng dụng | 67 | done | Nguyễn Thùy Ngân | #108, #163 | `notifications` |
| F-04 | Đăng tin, lưu nháp, kệ hàng | 13 đến 22 | done | Trần Anh Tú | #91, #92, #93, #216, #219, #220 | `listings`, `categories`, `images` |
| F-05 | Tìm kiếm tin đăng và người dùng, bộ lọc, sắp xếp, kênh xem | 23 đến 34, 103 | done | Trần Anh Tú | #94, #211, #218, #224 | `listings`, `users`, `follows`; Room `listing_cache`, `recent_search` |
| F-06 | Gợi ý: tin tương tự, giá tham khảo, Dành cho bạn | 36, 37, 38 | done | Trần Anh Tú | #221, #222, #223, #265 | `listings`; Room `recent_view`, `recent_search` |
| F-07 | Lưu tin | 40, 41, 42 | done | Trần Anh Tú | #95 | `savedListings` |
| F-08 | Chat chữ và ảnh, trạng thái hoạt động | 43, 44, 46 | done | Nguyễn Lê Hải Long | #96, #97 | `conversations`, `messages`, `images`; Room `conversation_cache`, `message_cache` |
| F-09 | Offer một chạm và giao dịch | 47 đến 55 | done | Nguyễn Lê Hải Long | #98, #99, #270 | `offers`, `deals`, `images`; Room `deal_cache` |
| F-10 | Thanh toán mô phỏng trong ứng dụng, lịch sử giao dịch và thanh toán, đánh giá | 56 đến 59 | done | Nguyễn Lê Hải Long | #100, #229, #230 | `payments`, `deals`, `reviews` |
| F-11 | Hồ sơ công khai, uy tín, theo dõi | 7, 8, 39, 60 | done | Nguyễn Hoàng Phúc | #101, #102, #217, #237, #238, #239, #246 | `users`, `follows`, `reviews` |
| F-12 | Báo cáo và khiếu nại | 61 đến 66 | done | Nguyễn Hoàng Phúc | #103, #240, #241, #242, #260 | `reports`, `complaints`, `images` |
| F-13 | Admin xử lý khiếu nại | 87 đến 91 | done | Nguyễn Hoàng Phúc | #104, #243, #244, #245 | `complaints`, `adminNote`, `notifications` |
| F-14 | Admin người dùng, nội dung, danh mục, giao dịch, chuyển tiền tự động, tài khoản admin | 68 đến 82, 84, 105, 106 | in-progress | Trần Đức Phát | #105, #106, #107, #212, #226, #227, #228, #231, #232; đang làm: #234 | `users`, `warnings`, `sellerVerifications`, `admins`, `activityLogs`, `reports`, `bannedKeywords`, `categories`, `listings`, `deals`, `payments` |

STT 73 (duyệt yêu cầu xác thực tài khoản) thuộc F-14 ở phía admin; phía người dùng (biểu mẫu gửi yêu cầu) do Phúc làm cùng F-11, theo [so-huu.md](../../plan/so-huu.md).

## Phạm vi đợt rút gọn 2026-10-22

Còn 4 ngày code, nên đợt này làm 24 trong 33 màn hình. Bảng dưới ghi phần **bị bỏ lại** của từng tính năng. Tính năng không có trong bảng thì làm trọn vẹn.

Cột STT dùng số của [bảng đặc tả chức năng](dac-ta-chuc-nang.md). Các STT bị bỏ **vẫn mang Phạm vi "Đợt này"** trong bảng đó, nên luật R17 không chặn được chúng; bảng này là chỗ duy nhất nói chúng bị bỏ. Ai code thì đọc [`ke-hoach-rut-gon.md`](../../plan/ke-hoach-rut-gon.md) mục 4 trước khi bắt đầu.

| Mã | Phần bỏ lại | STT bỏ |
| --- | --- | --- |
| F-02 | Quản lý thông tin cá nhân (A04), địa chỉ giao nhận (A06), cài đặt thông báo, vô hiệu hóa tài khoản | 4, 6, 10, 12 |
| F-01 | Đổi mật khẩu khi đang đăng nhập | 11 |
| F-05 | Lọc theo bán kính (vì bỏ A06 nên không có nguồn tọa độ), tìm kiếm người dùng | 27 phần bán kính, 103 |
| F-06 | Toàn bộ: tin tương tự, giá tham khảo, mục Dành cho bạn, `RecommendationEngine` | 36, 37, 38 |
| F-10 | Lịch sử giao dịch và lịch sử thanh toán (C04) | 57, 58 |
| F-12 | Toàn bộ khiếu nại (D05) và màn Báo cáo của tôi (D07) | 64, 65, 66 |
| F-13 | Toàn bộ: admin xử lý khiếu nại (D06) | 87 đến 91 |
| F-14 | Quản lý danh mục (E05), giám sát giao dịch (E06), tài khoản admin (E07), giải ngân tự động nếu không còn giờ | 79, 80, 81, 82, 84, 105 |

Phần giữ lại chạy trọn mạch demo: khách xem tin khi tắt mạng, đăng nhập Google, gửi yêu cầu xác thực, admin duyệt, đăng tin kèm ảnh chụp, nhắn tin, gửi đề nghị, chấp nhận, thanh toán mô phỏng, giao, nhận, đánh giá, admin khóa tài khoản và ẩn tin.

Báo cáo cuối kỳ phải nói thẳng phần bỏ lại này, đừng viết là đã làm.

## Yêu cầu môn học

Ba yêu cầu này cắt ngang mọi tính năng, không có mã `F-xx` và không phải chức năng trong bảng đặc tả (trừ đăng nhập Google).

| Yêu cầu | Cách đáp ứng |
| --- | --- |
| Dùng Firebase | Authentication, Firestore, gói Spark (không dùng Cloud Storage) |
| Giao diện thích ứng khi xoay màn hình, giữ nguyên trạng thái | Layout `layout-land` hoặc `ConstraintLayout`; trạng thái nằm trong ViewModel |
| Xem lại trang đã xem khi mất mạng (SQLite) | Room làm bộ nhớ đệm, xem [room-schema.md](../data/room-schema.md) |
| Đăng nhập bằng tài khoản Google | STT 2, thuộc F-01 |

Cột "Chạm tới" dùng tên collection và bảng thật trong [firestore-schema.md](../data/firestore-schema.md) và [room-schema.md](../data/room-schema.md); đổi chúng cần hỏi leader (`AGENTS.md`, luật R6).
