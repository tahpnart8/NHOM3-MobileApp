# Cấu trúc dữ liệu cục bộ (Room, SQLite)

Nguồn sự thật cho mọi bảng cục bộ. **Hiện chưa có bảng nào**, vì Room chưa được thêm vào build.

Quy tắc:

- Room chỉ là **bộ nhớ đệm** của dữ liệu Firestore để xem lại khi mất mạng (tính năng F-02).
- Đổi bảng hoặc cột đã có cần hỏi leader trước (R6) và tăng phiên bản cơ sở dữ liệu kèm migration.
- Ghi bảng mới vào đây trong cùng Pull Request với `@Entity`.

## Phiên bản cơ sở dữ liệu

Chưa có.

## Mẫu một bảng

| Cột | Kiểu | Khóa | Ghi chú |
| --- | --- | --- | --- |
| | | | |
