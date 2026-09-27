# Tài liệu dự án PUBGApp

Thư mục này mô tả **hệ thống là gì**. Lý do đằng sau các lựa chọn nằm ở `workflow/memory/`. Mỗi chủ đề chỉ có **một** tài liệu sở hữu; tài liệu khác chỉ dẫn link, không chép lại.

| Chủ đề | Tài liệu sở hữu | Nội dung |
| --- | --- | --- |
| Sản phẩm là gì, cho ai | [product/brief.md](product/brief.md) | Mục tiêu, người dùng, phạm vi |
| Danh sách tính năng | [product/features.md](product/features.md) | Mã `F-xx`, trạng thái, người làm. **AI chỉ làm tính năng có mã ở đây** |
| Kiến trúc ứng dụng | [architecture/overview.md](architecture/overview.md) | Các lớp, cấu trúc gói, Firebase, offline |
| Dữ liệu trên Firestore | [data/firestore-schema.md](data/firestore-schema.md) | Collection, trường, quy tắc |
| Dữ liệu cục bộ Room | [data/room-schema.md](data/room-schema.md) | Bảng SQLite, phiên bản |
| Quy ước viết code | [conventions/android-java.md](conventions/android-java.md) | Đặt tên, tài nguyên, luồng |
| Cách làm việc nhóm | [workflow/huong-dan-lam-viec.md](workflow/huong-dan-lam-viec.md) | Cài đặt, Issue, Branch, PR |
| Cấu hình GitHub | [workflow/cau-hinh-github.md](workflow/cau-hinh-github.md) | Ruleset của `main`, cách gỡ khi check chặn mọi PR, chi phí |
| Cài đặt Firebase | [workflow/firebase-setup.md](workflow/firebase-setup.md) | Tạo project, gói Blaze, SHA-1 từng máy, `google-services.json` |
| Kế hoạch giao việc | [../plan/README.md](../plan/README.md) | Giai đoạn và việc; **chưa có, chờ tài liệu yêu cầu** |

## Phân tích và thiết kế sơ bộ

Ba giai đoạn đầu của đồ án, trước khi vào giai đoạn code.

| Chủ đề | Tài liệu sở hữu | Nội dung |
| --- | --- | --- |
| Phân tích đề bài | [analysis/README.md](analysis/README.md) | BACCM, nền tảng tương tự, nhu cầu người dùng, thuật ngữ, dự kiến lớp |
| Đặc tả chức năng | [product/dac-ta-chuc-nang.md](product/dac-ta-chuc-nang.md) | Bảng chi tiết từng chức năng: STT, User, Module, Tên chức năng, Chức năng con, Đặc tả |
| Sơ đồ chức năng, sơ đồ lớp, ERD | [diagrams/README.md](diagrams/README.md) | File `.drawio` và ảnh xuất của 3 sơ đồ |
| Giao diện (mockup) | [ui/README.md](ui/README.md) | 30 màn hình, chia 5 nhóm theo luồng nghiệp vụ |
| Cách nộp tài liệu ba giai đoạn trên | [workflow/nop-tai-lieu.md](workflow/nop-tai-lieu.md) | Quy trình nhánh, commit, Pull Request cho việc tài liệu |

Cách nói chuyện với AI (prompt, vòng lặp Explore, Plan, Implement, Review) nằm ở [`workflow/guide/`](../guide/README.md). Luật cho AI nằm ở [`AGENTS.md`](../../AGENTS.md) (tiếng Anh). Quy tắc đặt tên nằm ở [`.github/NAMING.md`](../../.github/NAMING.md).

**Quy tắc:** đổi cấu trúc dữ liệu hoặc hành vi một tính năng thì sửa tài liệu tương ứng **trong cùng Pull Request**. Không sửa tài liệu cho khớp với code sai; nếu hai bên lệch nhau, nói ra và để người quyết định xử lý.
