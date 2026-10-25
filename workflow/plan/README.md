# Kế hoạch thực hiện đồ án

Lộ trình từ tài liệu tới ứng dụng chạy được để demo: làm gì, theo thứ tự nào, ai làm, việc nào phụ thuộc việc nào. Thư mục này không mô tả hệ thống (việc của `workflow/docs/`) và không ghi lý do (việc của `workflow/memory/decisions.md`). Chỉ leader sửa thư mục này.

## Mốc thời gian

| Giai đoạn | Ngày | Mục tiêu | Tệp |
| --- | --- | --- | --- |
| P0. Chốt tài liệu | 12/10 | Mọi tài liệu khớp nhau và khớp quyết định mới nhất; luật cho AI đủ chặt để không bịa | [phase-0-tai-lieu.md](phase-0-tai-lieu.md) |
| P1. Nền móng | 13/10 | Firebase, thư viện, toàn bộ lớp đủ chữ ký, khung 3 Activity và các màn trống nối điều hướng | [phase-1-nen-mong.md](phase-1-nen-mong.md) |
| P2. Tính năng | 14/10 đến 22/10 | Mọi chức năng Đợt này chạy trên máy thật, 5 người làm song song theo module | [phase-2-tinh-nang.md](phase-2-tinh-nang.md) |
| P3. Hoàn thiện | 23/10 đến 25/10 | Xoay màn hình, offline, Security Rules thật, test quy tắc | [phase-3-hoan-thien.md](phase-3-hoan-thien.md) |
| P4. Tích hợp và demo | 26/10 | Dữ liệu mẫu, kịch bản demo, sửa lỗi chặn; **đóng băng code cuối ngày 26/10** | [phase-4-tich-hop-demo.md](phase-4-tich-hop-demo.md) |
| Báo cáo | 27/10 đến 30/10 | Viết báo cáo đồ án; **nộp 30/10** | (ngoài thư mục này) |

## Ai làm gì

Chia theo module, mỗi người sở hữu trọn một module từ dữ liệu tới màn hình. Ai được sửa file nào ở [so-huu.md](so-huu.md); đọc trước khi bắt đầu bất kỳ issue nào.

| Module | Người | Mã tính năng | Số việc ở P2 |
| --- | --- | --- | --- |
| A. Tài khoản và thông báo | Nguyễn Thùy Ngân | F-01, F-02, F-03 | 6 |
| B. Tin đăng và khám phá | Trần Anh Tú | F-04, F-05, F-06, F-07 | 7 |
| C. Chat và giao dịch | Nguyễn Lê Hải Long | F-08, F-09, F-10 | 7 |
| D. Hồ sơ và tin cậy | Nguyễn Hoàng Phúc | F-11, F-12, F-13 | 6 |
| E. Quản trị và nền móng | Trần Đức Phát (leader) | F-14, cả P0 và P1 | 6 |

**Review:** Phát hoặc Hải Long duyệt Pull Request trong `PUBGApp/`. File Gradle, `google-services.json`, `firebase/`, `AndroidManifest.xml` và mọi file luật chơi (`.github/`, `AGENTS.md`, `.agents/`, `workflow/scripts/`) chỉ Phát duyệt. Không ai tự duyệt Pull Request của mình.

## Đọc mã việc

- Mã việc có dạng `P<giai đoạn>-<số>` hoặc `P2-<module><số>`, ví dụ `P1-04`, `P2-B3`.
- Mỗi việc là một issue; thân issue có dòng `Plan item: P2-B3`. Cột Issue trong các bảng giai đoạn được điền khi issue đã tạo.
- Thứ tự trong một module tính theo cột Phụ thuộc. Giữa các module không có phụ thuộc, trừ P2-A1 (đăng nhập), việc cả nhóm cần để chạy thử.
- Tiêu chí nghiệm thu nằm trong issue, không lặp lại ở đây.

## Cách làm một issue code

1. Nhận issue (assignee là mình), chạy skill `pubg-start-task`: tạo nhánh từ `main` mới nhất, chép nguyên danh sách file được sửa của issue.
2. Cho AI đọc đúng các tài liệu issue liệt kê. Tên lớp, trường, phương thức, trạng thái lấy từ `workflow/docs/architecture/dac-ta-lop/` và `workflow/docs/data/`; chuyển trạng thái theo `workflow/docs/architecture/trang-thai.md`. Tài liệu thiếu tên cần dùng thì hỏi trong issue, không tự đặt.
3. Làm trong khoảng nửa ngày đến một ngày. Chạy `./gradlew assembleDebug testDebugUnitTest lintDebug` và `sh workflow/scripts/check-java-style.sh`, chạy thử trên máy ảo.
4. Mở Pull Request bằng skill `pubg-open-pr`, ghi lại đã thử gì trên máy ảo. Chờ duyệt; sửa theo góp ý; người duyệt rebase merge (mọi commit của bạn được giữ trên main).

## Nguyên tắc để không chặn nhau

- P1 tạo sẵn mọi file dùng chung: lớp mô hình, repository đủ chữ ký, DAO, đồ thị điều hướng, hành động toàn cục, file chuỗi theo module. P2 chỉ điền nội dung.
- Phương thức của người khác chưa xong thì trả lỗi `NOT_READY` rõ ràng; màn hình của mình hiện lỗi đó, không giả dữ liệu, không tự viết thay.
- Pull Request nhỏ, merge sớm; kéo `main` về (skill `pubg-sync-branch`) mỗi sáng.
- Vướng quyết định (tài liệu thiếu hoặc mâu thuẫn): bình luận trong issue và gắn nhãn `needs-leader-decision`, làm tiếp phần khác của issue trong lúc chờ.
