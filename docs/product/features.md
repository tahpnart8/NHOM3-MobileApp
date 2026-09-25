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

Các dòng bên dưới là **gợi ý ban đầu** để leader duyệt, ngoại trừ F-01 đến F-03 là yêu cầu bắt buộc của môn học (nhưng vẫn ở `proposed` cho đến khi leader duyệt cách làm).

| Mã | Tính năng | Trạng thái | Người làm | Issue | Chạm tới |
| --- | --- | --- | --- | --- | --- |
| F-01 | Đăng nhập và tài khoản, gồm đăng nhập Google (Firebase Authentication) | proposed | | | Firebase Auth; Firestore `users` |
| F-02 | Bộ nhớ đệm offline bằng Room: xem lại trang đã xem khi mất mạng | proposed | | | Room; mọi màn hình có danh sách hoặc chi tiết |
| F-03 | Giao diện thích ứng khi xoay màn hình, giữ nguyên trạng thái | proposed | | | Layout `layout-land`, ViewModel |
| F-04 | Đăng bán và quản lý tin của người bán | proposed | | | Firestore `listings`; Storage (ảnh) |
| F-05 | Bảng tin và tìm kiếm | proposed | | | Firestore `listings` |
| F-06 | Nhắn tin giữa người mua và người bán | proposed | | | Firestore `chats` |
| F-07 | Đơn hàng và giao dịch | proposed | | | Firestore `orders` |
| F-08 | Hồ sơ người dùng và đánh giá | proposed | | | Firestore `users`, `reviews` |
| F-09 | Quản trị đơn giản | proposed | | | Firestore `listings`, `users` |

Cột "Chạm tới" cho biết tính năng đụng những collection hoặc bảng nào, để biết khi nào phải cập nhật `docs/data/`. Tên collection chỉ là dự kiến cho đến khi có trong [firestore-schema.md](../data/firestore-schema.md).
