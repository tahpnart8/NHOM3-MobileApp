# Tài liệu dự án PUBGApp

Thư mục này mô tả **hệ thống là gì**. Lý do đằng sau các lựa chọn nằm ở `memory/`. Mỗi chủ đề chỉ có **một** tài liệu sở hữu; tài liệu khác chỉ dẫn link, không chép lại.

| Chủ đề | Tài liệu sở hữu | Nội dung |
| --- | --- | --- |
| Sản phẩm là gì, cho ai | [product/brief.md](product/brief.md) | Mục tiêu, người dùng, phạm vi |
| Danh sách tính năng | [product/features.md](product/features.md) | Mã `F-xx`, trạng thái, người làm. **AI chỉ làm tính năng có mã ở đây** |
| Kiến trúc ứng dụng | [architecture/overview.md](architecture/overview.md) | Các lớp, cấu trúc gói, Firebase, offline |
| Dữ liệu trên Firestore | [data/firestore-schema.md](data/firestore-schema.md) | Collection, trường, quy tắc |
| Dữ liệu cục bộ Room | [data/room-schema.md](data/room-schema.md) | Bảng SQLite, phiên bản |
| Quy ước viết code | [conventions/android-java.md](conventions/android-java.md) | Đặt tên, tài nguyên, luồng |
| Cách làm việc nhóm | [workflow/huong-dan-lam-viec.md](workflow/huong-dan-lam-viec.md) | Cài đặt, Issue, Branch, PR |
| Cài đặt Firebase | [workflow/firebase-setup.md](workflow/firebase-setup.md) | Tạo project, gói Blaze, SHA-1 từng máy, `google-services.json` |
| Kế hoạch giao việc | [../plan/README.md](../plan/README.md) | Giai đoạn và việc; **chưa có, chờ tài liệu yêu cầu** |

Cách nói chuyện với AI (prompt, vòng lặp Explore, Plan, Implement, Review) nằm ở [`guide/`](../guide/README.md). Luật cho AI nằm ở [`AGENTS.md`](../AGENTS.md) (tiếng Anh). Quy tắc đặt tên nằm ở [`.github/NAMING.md`](../.github/NAMING.md).

**Quy tắc:** đổi cấu trúc dữ liệu hoặc hành vi một tính năng thì sửa tài liệu tương ứng **trong cùng Pull Request**. Không sửa tài liệu cho khớp với code sai; nếu hai bên lệch nhau, nói ra và để người quyết định xử lý.
