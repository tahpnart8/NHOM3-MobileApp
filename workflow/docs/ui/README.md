# Giao diện (mockup)

Giai đoạn 2 của đồ án. 30 màn hình, vẽ bằng Figma, nộp dạng ảnh PNG khung điện thoại dọc (tỉ lệ 390 x 844), chia 5 nhóm theo luồng nghiệp vụ để không ai sửa chung một file.

Mỗi nhóm nộp theo 3 đợt: đợt 1 một màn, đợt 2 hai màn, đợt 3 ba màn. Xem chi tiết từng màn trong README của nhóm.

## Mục lục 30 màn hình

| Mã | Tên màn hình | Nhóm |
| --- | --- | --- |
| A01 | Đăng nhập | [a-tai-khoan](a-tai-khoan/README.md) |
| A02 | Đăng ký | [a-tai-khoan](a-tai-khoan/README.md) |
| A03 | Quên mật khẩu | [a-tai-khoan](a-tai-khoan/README.md) |
| A04 | Quản lý thông tin cá nhân | [a-tai-khoan](a-tai-khoan/README.md) |
| A05 | Chỉnh sửa hồ sơ | [a-tai-khoan](a-tai-khoan/README.md) |
| A06 | Quản lý địa chỉ | [a-tai-khoan](a-tai-khoan/README.md) |
| B01 | Trang chủ | [b-kham-pha-tin-dang](b-kham-pha-tin-dang/README.md) |
| B02 | Tìm kiếm và bộ lọc | [b-kham-pha-tin-dang](b-kham-pha-tin-dang/README.md) |
| B03 | Chi tiết tin đăng | [b-kham-pha-tin-dang](b-kham-pha-tin-dang/README.md) |
| B04 | Đăng tin | [b-kham-pha-tin-dang](b-kham-pha-tin-dang/README.md) |
| B05 | Kệ hàng của tôi | [b-kham-pha-tin-dang](b-kham-pha-tin-dang/README.md) |
| B06 | Tin đã lưu | [b-kham-pha-tin-dang](b-kham-pha-tin-dang/README.md) |
| C01 | Danh sách tin nhắn | [c-ket-noi-giao-dich](c-ket-noi-giao-dich/README.md) |
| C02 | Trò chuyện và đề nghị giá | [c-ket-noi-giao-dich](c-ket-noi-giao-dich/README.md) |
| C03 | Chi tiết giao dịch | [c-ket-noi-giao-dich](c-ket-noi-giao-dich/README.md) |
| C04 | Lịch sử giao dịch | [c-ket-noi-giao-dich](c-ket-noi-giao-dich/README.md) |
| C05 | Viết đánh giá | [c-ket-noi-giao-dich](c-ket-noi-giao-dich/README.md) |
| C06 | Thông báo | [c-ket-noi-giao-dich](c-ket-noi-giao-dich/README.md) |
| D01 | Hồ sơ cá nhân | [d-ho-so-tin-cay](d-ho-so-tin-cay/README.md) |
| D02 | Trang cá nhân | [d-ho-so-tin-cay](d-ho-so-tin-cay/README.md) |
| D03 | Hồ sơ uy tín người bán | [d-ho-so-tin-cay](d-ho-so-tin-cay/README.md) |
| D04 | Báo cáo tin đăng | [d-ho-so-tin-cay](d-ho-so-tin-cay/README.md) |
| D05 | Tạo khiếu nại | [d-ho-so-tin-cay](d-ho-so-tin-cay/README.md) |
| D06 | Admin: Xử lý khiếu nại | [d-ho-so-tin-cay](d-ho-so-tin-cay/README.md) |
| E01 | Admin: Bảng điều khiển | [e-quan-tri](e-quan-tri/README.md) |
| E02 | Admin: Danh sách người dùng | [e-quan-tri](e-quan-tri/README.md) |
| E03 | Admin: Chi tiết người dùng | [e-quan-tri](e-quan-tri/README.md) |
| E04 | Admin: Kiểm duyệt nội dung | [e-quan-tri](e-quan-tri/README.md) |
| E05 | Admin: Quản lý danh mục | [e-quan-tri](e-quan-tri/README.md) |
| E06 | Admin: Giám sát giao dịch | [e-quan-tri](e-quan-tri/README.md) |

## Ngoài phạm vi đợt này

- Bố cục xoay ngang (landscape) cho các màn chính: để lại cho giai đoạn code.
- Màn E01 có 4 số liệu nhanh dạng ô đếm (người dùng, tin đăng, giao dịch, khiếu nại) và một danh sách "Cần xử lý" chỉ để điều hướng sang màn chi tiết tương ứng; không có biểu đồ xu hướng, thống kê theo danh mục hay xuất báo cáo (những phần đó ở dòng 84 đến 88 của bảng đặc tả, vẫn ngoài phạm vi).
- Màn E06 chỉ hiển thị trạng thái giải ngân (đã tự động), không có nút duyệt tay.

## Quy ước tên file ảnh

`ui/<nhóm>/<mã>-<tên-khong-dau>.png`, ví dụ `ui/a-tai-khoan/a01-dang-nhap.png`.
