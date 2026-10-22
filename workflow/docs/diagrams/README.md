# Sơ đồ mô hình hóa

Các sơ đồ của đồ án, dựa trên [bảng đặc tả chức năng](../product/dac-ta-chuc-nang.md). Mọi sơ đồ chỉ vẽ các chức năng **Đợt này**; chức năng **Phát triển sau** không có trên sơ đồ.

Mỗi sơ đồ có một **bản chữ** để code theo: ảnh dành cho người xem nhanh và cho báo cáo, còn công cụ AI đọc bản chữ. Khi ảnh và bản chữ khác nhau, bản chữ đúng và ảnh phải vẽ lại.

| Sơ đồ | Thư mục | Bản chữ |
| --- | --- | --- |
| Biểu đồ phân rã chức năng và sơ đồ tính năng | [so-do-chuc-nang/](so-do-chuc-nang/README.md) | [bảng đặc tả chức năng](../product/dac-ta-chuc-nang.md), [sổ tính năng](../product/features.md) |
| Sơ đồ use case | [use-case/](use-case/README.md) | [đặc tả use case](../product/use-case/README.md) |
| Sơ đồ lớp (chia gói theo tầng) | [so-do-lop/](so-do-lop/README.md) | [đặc tả lớp](../architecture/dac-ta-lop/README.md) |
| Sơ đồ quan hệ thực thể (ERD, Firestore và Room) | [erd/](erd/README.md) | [cấu trúc Firestore](../data/firestore-schema.md), [cấu trúc Room](../data/room-schema.md) |
| Sơ đồ máy trạng thái | [may-trang-thai/](may-trang-thai/README.md) | [máy trạng thái](../architecture/trang-thai.md) |
| Sơ đồ tuần tự các luồng chính | (vẽ bằng Mermaid ngay trong tài liệu) | [luồng xử lý](../architecture/luong-xu-ly.md) |

Thư mục nào có tệp `.drawio` gốc thì mở được bằng draw.io desktop hoặc app.diagrams.net; ảnh `.png` là bản xuất.
