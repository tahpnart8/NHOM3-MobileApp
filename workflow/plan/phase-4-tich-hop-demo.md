# P4. Tích hợp và chuẩn bị demo

**Thời gian:** 26/10 (đóng băng code cuối ngày). **Chủ:** cả nhóm.

**Mục tiêu:**

- Có dữ liệu mẫu đủ để mọi màn hình và hệ gợi ý có nội dung.
- Một kịch bản demo chạy trọn từ đăng ký tới hoàn tất giao dịch và xử lý khiếu nại.
- Không còn lỗi chặn.

Sau 26/10 chỉ sửa lỗi chặn demo; 27/10 đến 30/10 dành cho báo cáo.

| Mã | Việc | Chủ | Phụ thuộc |
| --- | --- | --- | --- |
| P4-01 | Nút "Tạo dữ liệu mẫu" và "Xóa dữ liệu mẫu" trong khu quản trị, chỉ có ở bản debug. Ghi qua chính các repository. Mã tài liệu mẫu bắt đầu bằng `demo_` để xóa được. Đủ yêu cầu ở [goi-y.md](../docs/architecture/goi-y.md) mục "Yêu cầu với dữ liệu mẫu" | Tú | P2-B5, P2-E4 |
| P4-02 | Kịch bản demo: các bước bấm từng màn, tài khoản nào đóng vai nào (người mua, người bán, admin), kết quả mong đợi | Phát | P4-01 |
| P4-03 | Chạy thử kịch bản trên máy ảo của từng người, ghi lỗi thành issue `type:bug` | Cả nhóm | P4-02 |
| P4-04 | Sửa lỗi chặn từ P4-03, mỗi lỗi một Pull Request nhỏ | Chủ module có lỗi | P4-03 |
| P4-05 | Đối chiếu tài liệu với code bằng skill `pubg-doc-check`; sửa tài liệu cho khớp hoặc ghi chỗ lệch | Phát | P4-04 |
