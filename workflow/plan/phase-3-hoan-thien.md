# P3. Hoàn thiện theo yêu cầu môn học

**Thời gian:** 23/10 đến 25/10. **Chủ:** mỗi module một người, cùng Phát.

**Mục tiêu:** đạt đủ ba yêu cầu cứng của giảng viên trên mọi màn hình:

- xoay màn hình không mất trạng thái;
- xem lại trang đã xem khi mất mạng;
- Firebase được bảo vệ bằng Security Rules thật.

Ngoài ra, mỗi module có test cho các quy tắc của mình.

**Điều kiện hoàn thành:** danh sách kiểm tra ở cuối tệp này đạt hết trên một máy ảo, có ghi lại kết quả trong Pull Request.

| Mã | Việc | Chủ | Phụ thuộc |
| --- | --- | --- | --- |
| P3-A | Module A: bố cục ngang cho các màn A01 đến A06 và C06, giữ nội dung đang nhập khi xoay, báo lỗi mất mạng rõ ràng | Ngân | P2-A1 đến P2-A6 |
| P3-B | Module B: bố cục ngang B01 đến B06; offline cho trang chủ, chi tiết tin, lịch sử tìm; giữ bộ lọc và vị trí cuộn khi xoay | Tú | P2-B1 đến P2-B7 |
| P3-C | Module C: bố cục ngang C01 đến C05; offline cho danh sách chat, tin nhắn đã xem, lịch sử giao dịch, đồng hồ đếm ngược | Hải Long | P2-C1 đến P2-C7 |
| P3-D | Module D: bố cục ngang D01 đến D06; giữ nội dung báo cáo và khiếu nại đang soạn khi xoay | Phúc | P2-D1 đến P2-D6 |
| P3-E | Module E: bố cục ngang E01 đến E06 | Phát | P2-E1 đến P2-E6 |
| P3-R | Security Rules đầy đủ theo `firestore-schema.md` và `trang-thai.md`; tạo các chỉ mục ghép; kiểm tra bằng tay các kịch bản tấn công ở `overview.md` mục 10 | Phát | P2 xong |
| P3-T | Rà test: mỗi quy tắc trong `trang-thai.md` có ít nhất một unit test; `RecommendationEngine` đủ test ở `goi-y.md` | Phát, chủ module bổ sung | P2 xong |

## Danh sách kiểm tra

- [ ] Xoay từng màn trong 30 màn: không tải lại dữ liệu, không mất chữ đang nhập, không mất vị trí cuộn.
- [ ] Bật chế độ máy bay sau khi đã xem: trang chủ, chi tiết tin đã xem, danh sách chat, tin nhắn đã xem, lịch sử giao dịch vẫn hiện, có dải báo offline. Các nút ghi bị khóa.
- [ ] Đăng xuất thì bộ nhớ đệm Room bị xóa.
- [ ] Tài khoản thường không đọc được `users/{uid khác}/private/identity` và `complaints/*/adminNote/*`.
- [ ] Tài khoản thường không tự ghi `admins/{uid}`, không tự đổi `status` của mình (trừ DELETED và hết hạn khóa), không tự giải ngân trước hạn.
- [ ] Mọi thao tác mạng thất bại đều hiện câu báo lỗi tiếng Việt.
