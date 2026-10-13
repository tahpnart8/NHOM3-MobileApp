# Tổng quan sản phẩm

**PUBGApp** (Pre-owned Users' Bargain Grounds) là ứng dụng Android để mua, bán và chia sẻ đồ cũ. Ứng dụng kết hợp mạng xã hội với thương mại điện tử: gần với Facebook Marketplace, cộng thêm quản lý người mua, người bán và giao dịch theo kiểu Shopee.

Đây là đồ án môn Phát triển ứng dụng Mobile (nhóm 3, 5 thành viên).

## Người dùng

| Vai trò | Làm gì |
| --- | --- |
| Người mua | Xem bảng tin, tìm kiếm, xem chi tiết tin, nhắn tin, gửi đề nghị giá, theo dõi giao dịch |
| Người bán | Đăng và quản lý tin bán, trả lời tin nhắn, chấp nhận hoặc từ chối đề nghị, xử lý giao dịch |
| Quản trị | Màn hình riêng (`AdminActivity`) để quản lý người dùng, kiểm duyệt tin, quản lý danh mục, giám sát giao dịch và xử lý khiếu nại. Không có thống kê, biểu đồ hay phân quyền admin (phát triển sau) |

Một tài khoản có thể vừa mua vừa bán.

## Yêu cầu bắt buộc của môn học

1. Dùng **Firebase**.
2. Giao diện **thích ứng khi xoay màn hình**.
3. Đồng bộ dữ liệu về **SQLite** để ngắt mạng vẫn xem được những trang đã xem.
4. Khai thác API hỗ trợ, ví dụ **đăng nhập bằng tài khoản Google**.

Các yêu cầu này được ghi ở mục "Yêu cầu môn học" của [features.md](features.md); đăng nhập Google là STT 2 trong [bảng đặc tả chức năng](dac-ta-chuc-nang.md).

## Phạm vi

- **Làm trong giai đoạn này:** 87 chức năng "Đợt này" của bảng đặc tả chức năng, gom thành 14 tính năng trong [features.md](features.md).
- **Phát triển sau, không code:** 15 chức năng (STT 45, 83, 85, 86, 92 đến 102): gửi video trong chat, cảnh báo giao dịch bất thường, admin đổi trạng thái thủ công, duyệt tin trước khi hiện, bảng thống kê, cấu hình tham số, nội dung tĩnh, thông báo hệ thống, nhật ký và phân quyền admin.
- **Ngoài phạm vi:** thanh toán thật (ký quỹ chỉ mô phỏng), vận chuyển thật (người bán tự nhập đơn vị và mã vận đơn), phát hành lên Google Play.
- **Hạ tầng:** Firebase gói Spark miễn phí, không dùng Cloud Storage. Ảnh được nén và lưu dạng Base64 trong Firestore; không gửi video.
- **Mốc thời gian:** đóng băng code ngày 2026-10-26, nộp bài ngày 2026-10-30. Kế hoạch nằm ở [workflow/plan/](../../plan/README.md).
