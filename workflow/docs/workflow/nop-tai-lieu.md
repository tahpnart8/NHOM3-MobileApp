# Nộp tài liệu (phân tích, sơ đồ, giao diện)

Áp dụng cho mọi issue thuộc ba giai đoạn đầu của đồ án: phân tích đề bài, sơ đồ, giao diện. Vẫn theo đúng luồng Issue, Branch, Pull Request như code, chỉ khác là kết quả là tài liệu chứ không phải code Java.

## Các bước

```text
1. git checkout main && git pull
2. git checkout -b docs/<số issue>-<vài-từ-ngắn>
   Ví dụ: docs/14-phan-tich-baccm, docs/22-giao-dien-nhom-a-dot-1
3. Đặt file vào đúng đường dẫn issue đã ghi
4. git add <các file>
5. git commit -m "docs(<phạm-vi>): <mô tả ngắn>"
   Phạm vi: dùng "docs" cho phân tích và sơ đồ, "ui" cho giao diện.
   Ví dụ: "docs(docs): thêm phân tích BACCM", "docs(ui): thêm giao diện nhóm A đợt 1"
6. git push -u origin <tên nhánh>
7. Mở Pull Request theo template có sẵn, tiêu đề giống commit,
   phần "Issue liên quan" ghi "Closes #<số issue>"
8. Chờ leader duyệt và merge. Không tự merge.
```

## Dùng AI cho nhanh

- `/start-task <số issue>`: AI đọc issue, tạo đúng nhánh, tóm tắt việc phải làm.
- `/open-pr`: AI kiểm file trước khi mở Pull Request.
- `/guide`: khi gặp lỗi hook hoặc CI mà không hiểu vì sao.

## Ba lỗi hay gặp

| Lỗi | Cách sửa |
| --- | --- |
| Chưa chạy `workflow\scripts\setup-dev.ps1` | Chạy một lần sau khi clone, trước khi commit đầu tiên |
| CI báo tác giả commit là `NONE` | Email Git chưa gắn với tài khoản GitHub của bạn, vào GitHub Settings kiểm tra |
| Tên nhánh bị hook chặn | Phải đúng dạng `<type>/<số issue>-<vài-từ>`, ví dụ `docs/14-...`, không được thiếu số issue |

## Riêng cho tài liệu, khác với code

- Không cần chạy `gradlew` hay build Android, vì không đụng vào `PUBGApp/`.
- Ảnh (PNG, sơ đồ) phải dưới 5 MB một file, `pre-commit` sẽ chặn file lớn hơn. File lớn thì nén hoặc xuất lại ở độ phân giải thấp hơn.
- Vẫn phải tuân luật R14: tiêu đề và nội dung issue, Pull Request viết tiếng Việt có dấu.
