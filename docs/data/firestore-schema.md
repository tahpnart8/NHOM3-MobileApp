# Cấu trúc dữ liệu Firestore

Nguồn sự thật cho mọi collection. **Hiện chưa có collection nào**, vì Firebase chưa được cấu hình.

Quy tắc:

- Đổi collection hoặc trường **đã có** cần hỏi leader trước (`AGENTS.md`, luật R6).
- Thêm collection hoặc trường: ghi vào đây trong **cùng Pull Request** với code.
- Tên collection: số nhiều, chữ thường (`listings`). Tên trường: `camelCase`. Thời gian dùng kiểu `Timestamp` của Firestore.
- Không lưu quyền hạn (vai trò) trong trường do ứng dụng tự gửi; quyền do Security Rules kiểm tra.

## Mẫu một collection

| Trường | Kiểu | Bắt buộc | Ghi chú |
| --- | --- | --- | --- |
| | | | |

Mỗi collection có một mục riêng: mục đích, đường dẫn, bảng trường, ai được đọc và ghi (trích Security Rules), và tính năng `F-xx` sử dụng nó.
