# Kế hoạch dự án

Thư mục này sẽ chứa **lộ trình giao việc**: làm gì, theo thứ tự nào, việc nào phụ thuộc việc nào. Nó không mô tả hệ thống (việc đó của `docs/`) và không ghi lý do (việc đó của `memory/`).

**Trạng thái: chưa có kế hoạch.** Leader chưa nạp tài liệu yêu cầu và danh sách chức năng của ứng dụng, nên chưa ai được tạo issue mã nguồn. `docs/product/features.md` hiện chỉ có các dòng gợi ý ở trạng thái `proposed`, và theo `AGENTS.md` không ai làm tính năng `proposed`.

## Thứ tự khi bắt đầu

1. Leader nạp tài liệu yêu cầu và chức năng vào `docs/product/`.
2. Từ đó, lập danh sách tính năng chính thức trong `docs/product/features.md` (leader duyệt từng dòng lên `planned`).
3. Sau đó mới chia giai đoạn trong thư mục này và tạo issue từ kế hoạch bằng skill `pubg-new-issue`.

## Dự kiến cách tổ chức (chưa áp dụng, chờ leader xác nhận)

- Mỗi giai đoạn một file `phase-<n>-<tên>.md`, gồm mục tiêu, điều kiện hoàn thành và bảng việc.
- Mỗi việc có mã `P<giai đoạn>-<số>`, cột phụ thuộc, cột số issue, cột trạng thái. Thân issue ghi dòng `Plan item: P2-01`.
- Tiêu chí nghiệm thu nằm trong issue, không lặp lại ở đây.
- Không ghi ngày hay hạn chót; kế hoạch nói thứ tự và phụ thuộc, còn thời gian do nhóm quyết định.
- Chỉ leader sửa file trong thư mục này.
