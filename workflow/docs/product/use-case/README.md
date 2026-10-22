# Use case

43 use case của PUBGApp, mỗi use case là một mục tiêu trọn vẹn của một tác nhân, gom theo 5 module. Bảng này là mục lục; đặc tả đầy đủ (13 mục: tác nhân, mô tả, mức ưu tiên, khởi phát, tiền điều kiện, hậu điều kiện, ba luồng, quy tắc nghiệp vụ, yêu cầu phi chức năng) nằm trong tệp của từng module. Sơ đồ use case ở [`../../diagrams/use-case/`](../../diagrams/use-case/README.md).

## Chức năng, tính năng, use case: ba cách cắt khác nhau

Ba thứ này hay bị trộn vào nhau. Chúng là ba cách cắt cùng một tập việc, không bao nhau, và **không được vẽ chung một sơ đồ**.

| Khái niệm | Là gì | Mã | Số lượng | Sơ đồ của nó |
| --- | --- | --- | --- | --- |
| Chức năng | Việc nhỏ nhất hệ thống làm được | STT của [bảng đặc tả chức năng](../dac-ta-chuc-nang.md) | 91 Đợt này | [Sơ đồ phân rã chức năng](../../diagrams/so-do-chuc-nang/README.md) |
| Tính năng | Gói giao hàng, gom chức năng theo người phụ trách và theo collection nó chạm | F-01 đến F-14 của [sổ tính năng](../features.md) | 14 | Sơ đồ tính năng |
| Use case | Mục tiêu trọn vẹn của một tác nhân, có kết thúc quan sát được | UC01 đến UC43 | 43 | Sơ đồ use case |

Một use case gom một hoặc vài chức năng; mọi chức năng Đợt này thuộc ít nhất một use case. Chức năng Phát triển sau không có use case.

## Quy ước đặt tên use case

Thêm hoặc sửa use case thì theo đúng 7 quy tắc này.

1. Tên là **cụm động từ chủ động**, nhìn từ phía tác nhân, nêu mục tiêu: Đăng tin, Hủy giao dịch.
2. Không đưa vào tên: tên màn hình, tên lớp, tên nút, tên mục giao diện, tên cơ chế kỹ thuật.
3. Không ghép hai mục tiêu khác nhau bằng chữ và. Nếu đúng là một cụm thêm, sửa, xóa, xem thì dùng Quản lý X.
4. Không dùng văn nói (của mình, của tôi). Dùng cá nhân hoặc nói rõ đối tượng.
5. Mỗi use case phải kết thúc được: dữ liệu đổi, hoặc tác nhân nhận được thông tin mình cần.
6. **Việc hệ thống tự chạy theo thời gian không phải use case.** Nó là luồng thay thế của một use case có người làm, cộng một quy tắc trong [máy trạng thái](../../architecture/trang-thai.md).
7. «include» và «extend» phải hiếm, và không dùng để phân rã chức năng. Mô hình này chỉ còn **một** quan hệ; xem mục Quan hệ.

## Tác nhân

| Tác nhân | Loại | Là ai |
| --- | --- | --- |
| Khách | Chính | Người chưa đăng nhập. Chỉ xem được trang chủ, tìm kiếm tin đăng và xem chi tiết tin đăng |
| Người dùng | Chính | Người đã đăng nhập, tài khoản ACTIVE. Kế thừa Khách |
| Người dùng đã xác thực | Chính | Người dùng đã được admin duyệt xác thực. Chỉ vai này mới đăng tin, gửi đề nghị và tạo giao dịch được. Kế thừa Người dùng |
| Người mua | Chính | Người dùng đã xác thực trong vai mua hoặc nhận đổi |
| Người bán | Chính | Người dùng đã xác thực trong vai đăng tin, bán hoặc cho |
| Admin | Chính | Người dùng có tài liệu `admins/{uid}`, làm việc trong `AdminActivity` |
| Google | Phụ | Dịch vụ Google Sign-In cấp mã đăng nhập |

Thang kế thừa: Khách, rồi Người dùng, rồi Người dùng đã xác thực, rồi Người mua và Người bán. Vai sau có mọi thứ vai trước có.

Chỉ **Người dùng đã xác thực** mới đăng tin, gửi đề nghị và tạo giao dịch được (quyết định ngày 2026-10-22). **Khách** chỉ xem được trang chủ, tìm kiếm tin đăng và xem chi tiết tin đăng.

## Danh sách

### A. Tài khoản và thông báo

Người phụ trách: Nguyễn Thùy Ngân.

| Use case | Tác nhân | STT | Màn hình |
| --- | --- | --- | --- |
| [UC01 Đăng ký tài khoản](uc-a-tai-khoan.md#uc01-đăng-ký-tài-khoản) | Khách, Google | 1, 35 | A02 |
| [UC02 Đăng nhập](uc-a-tai-khoan.md#uc02-đăng-nhập) | Khách, Google | 2 | A01 |
| [UC03 Đặt lại mật khẩu](uc-a-tai-khoan.md#uc03-đặt-lại-mật-khẩu) | Khách | 3 | A03 |
| [UC04 Đăng xuất](uc-a-tai-khoan.md#uc04-đăng-xuất) | Người dùng | 104 | D01, A05 |
| [UC05 Quản lý thông tin cá nhân](uc-a-tai-khoan.md#uc05-quản-lý-thông-tin-cá-nhân) | Người dùng | 4, 5, 10, 11 | A04, A05 |
| [UC06 Quản lý địa chỉ giao nhận](uc-a-tai-khoan.md#uc06-quản-lý-địa-chỉ-giao-nhận) | Người dùng | 6 | A06 |
| [UC07 Xem thông báo](uc-a-tai-khoan.md#uc07-xem-thông-báo) | Người dùng | 67 | C06 |
| [UC08 Xóa tài khoản](uc-a-tai-khoan.md#uc08-xóa-tài-khoản) | Người dùng | 12 | A05 |

### B. Tin đăng và khám phá

Người phụ trách: Trần Anh Tú.

| Use case | Tác nhân | STT | Màn hình |
| --- | --- | --- | --- |
| [UC09 Quản lý tin nháp](uc-b-tin-dang.md#uc09-quản-lý-tin-nháp) | Người dùng đã xác thực | 22 | B05 |
| [UC10 Tạo tin đăng](uc-b-tin-dang.md#uc10-tạo-tin-đăng) | Người dùng đã xác thực | 16, 17, 18, 19, 20, 21 | B04 |
| [UC11 Quản lý kệ hàng](uc-b-tin-dang.md#uc11-quản-lý-kệ-hàng) | Người dùng đã xác thực | 13, 14, 15 | B05 |
| [UC12 Xem kênh tin](uc-b-tin-dang.md#uc12-xem-kênh-tin) | Người dùng | 29, 30, 38 | B01 |
| [UC13 Quản lý tin đã lưu](uc-b-tin-dang.md#uc13-quản-lý-tin-đã-lưu) | Người dùng | 40, 41, 42 | B03, B06 |
| [UC14 Tìm kiếm tin đăng](uc-b-tin-dang.md#uc14-tìm-kiếm-tin-đăng) | Khách | 23, 24, 25, 26, 27, 28, 31, 32, 33, 34 | B02 |
| [UC15 Tìm kiếm người dùng](uc-b-tin-dang.md#uc15-tìm-kiếm-người-dùng) | Người dùng | 103 | B02 |
| [UC16 Xem chi tiết tin đăng](uc-b-tin-dang.md#uc16-xem-chi-tiết-tin-đăng) | Khách | 36, 37 | B03 |
| [UC17 Đăng tin](uc-b-tin-dang.md#uc17-đăng-tin) | Người dùng đã xác thực | 22 | B04, B05 |

### C. Chat và giao dịch

Người phụ trách: Nguyễn Lê Hải Long.

| Use case | Tác nhân | STT | Màn hình |
| --- | --- | --- | --- |
| [UC18 Nhắn tin](uc-c-giao-dich.md#uc18-nhắn-tin) | Người dùng | 43, 44, 46 | C01, C02 |
| [UC19 Xem lịch sử giao dịch](uc-c-giao-dich.md#uc19-xem-lịch-sử-giao-dịch) | Người dùng | 57, 58 | C04 |
| [UC20 Gửi đề nghị](uc-c-giao-dich.md#uc20-gửi-đề-nghị) | Người mua | 47 | B03, C02 |
| [UC21 Thanh toán cho giao dịch](uc-c-giao-dich.md#uc21-thanh-toán-cho-giao-dịch) | Người mua | 56 | C03 |
| [UC22 Xác nhận đã nhận hàng](uc-c-giao-dich.md#uc22-xác-nhận-đã-nhận-hàng) | Người mua | 53, 84 | C03 |
| [UC23 Hủy giao dịch](uc-c-giao-dich.md#uc23-hủy-giao-dịch) | Người mua, Người bán | 55 | C03 |
| [UC24 Khiếu nại giao dịch](uc-c-giao-dich.md#uc24-khiếu-nại-giao-dịch) | Người mua, Người bán | 64, 65, 66 | D05, D07 |
| [UC25 Xác nhận điều khoản giao dịch](uc-c-giao-dich.md#uc25-xác-nhận-điều-khoản-giao-dịch) | Người mua, Người bán | 50, 51, 52 | C03 |
| [UC26 Đánh giá đối tác](uc-c-giao-dich.md#uc26-đánh-giá-đối-tác) | Người mua, Người bán | 59 | C05 |
| [UC27 Trả lời đề nghị](uc-c-giao-dich.md#uc27-trả-lời-đề-nghị) | Người bán | 48, 49 | C02 |
| [UC28 Xác nhận đã giao hàng](uc-c-giao-dich.md#uc28-xác-nhận-đã-giao-hàng) | Người bán | 54 | C03 |

### D. Hồ sơ và tin cậy

Người phụ trách: Nguyễn Hoàng Phúc.

| Use case | Tác nhân | STT | Màn hình |
| --- | --- | --- | --- |
| [UC29 Xem hồ sơ của người khác](uc-d-ho-so-tin-cay.md#uc29-xem-hồ-sơ-của-người-khác) | Người dùng | 7 | D02, D03 |
| [UC30 Xem hồ sơ uy tín cá nhân](uc-d-ho-so-tin-cay.md#uc30-xem-hồ-sơ-uy-tín-cá-nhân) | Người dùng | 7, 8, 60 | D01, D02, D03 |
| [UC31 Đăng ký xác thực người dùng](uc-d-ho-so-tin-cay.md#uc31-đăng-ký-xác-thực-người-dùng) | Người dùng | 9, 73 | D01 |
| [UC32 Theo dõi người dùng](uc-d-ho-so-tin-cay.md#uc32-theo-dõi-người-dùng) | Người dùng | 39 | D02 |
| [UC33 Báo cáo vi phạm](uc-d-ho-so-tin-cay.md#uc33-báo-cáo-vi-phạm) | Người dùng | 61, 62, 63 | D04, D07 |

### E. Quản trị

Người phụ trách: Trần Đức Phát.

Cột cuối là **phân hệ**, tức nhóm chức năng dùng làm khung trên sơ đồ trang E. Phân hệ không phải use case.

| Use case | Tác nhân | STT | Màn hình | Phân hệ |
| --- | --- | --- | --- | --- |
| [UC34 Tra cứu và xem hồ sơ người dùng](uc-e-quan-tri.md#uc34-tra-cứu-và-xem-hồ-sơ-người-dùng) | Admin | 68, 69, 74 | E02, E03 | Quản lý người dùng |
| [UC35 Xử lý vi phạm tài khoản](uc-e-quan-tri.md#uc35-xử-lý-vi-phạm-tài-khoản) | Admin | 70, 71, 72, 77 | E03 | Quản lý người dùng |
| [UC36 Duyệt yêu cầu xác thực người dùng](uc-e-quan-tri.md#uc36-duyệt-yêu-cầu-xác-thực-người-dùng) | Admin | 73 | E02, E03 | Quản lý người dùng |
| [UC37 Kiểm duyệt tin bị báo cáo](uc-e-quan-tri.md#uc37-kiểm-duyệt-tin-bị-báo-cáo) | Admin | 75, 76 | E04 | Kiểm duyệt nội dung |
| [UC38 Quản lý từ khóa cấm](uc-e-quan-tri.md#uc38-quản-lý-từ-khóa-cấm) | Admin | 78 | E04 | Kiểm duyệt nội dung |
| [UC39 Quản lý danh mục](uc-e-quan-tri.md#uc39-quản-lý-danh-mục) | Admin | 79, 80 | E05 | Quản lý danh mục |
| [UC40 Tra cứu và xem chi tiết giao dịch](uc-e-quan-tri.md#uc40-tra-cứu-và-xem-chi-tiết-giao-dịch) | Admin | 81, 82 | E01, E06 | Giám sát giao dịch |
| [UC41 Xử lý khiếu nại](uc-e-quan-tri.md#uc41-xử-lý-khiếu-nại) | Admin | 87, 88, 89, 90, 91 | D06 |
| [UC42 Quản lý tài khoản admin](uc-e-quan-tri.md#uc42-quản-lý-tài-khoản-admin) | Admin | 105 | (chưa có ảnh) |
| [UC43 Đăng nhập quản trị](uc-e-quan-tri.md#uc43-đăng-nhập-quản-trị) | Admin | 106 | (chưa có ảnh) |

## Quan hệ

| Use case | Quan hệ | Use case gốc |
| --- | --- | --- |
| [UC17 Đăng tin](uc-b-tin-dang.md#uc17-đăng-tin) | «extend» | [UC10 Tạo tin đăng](uc-b-tin-dang.md#uc10-tạo-tin-đăng) |

Đó là quan hệ duy nhất của mô hình, và có lý: người bán đang soạn tin có thể dừng lại và lưu nháp, tức là một nhánh tùy chọn chen vào giữa luồng Đăng tin. Đăng một bản nháp đã lưu là luồng của UC10 bắt đầu từ trạng thái DRAFT, không phải use case riêng.

«extend»: use case bên trái có thể chen vào use case gốc khi có điều kiện. «include»: use case bên trái luôn dùng use case bên phải; mô hình này không có quan hệ «include» nào.

Những chỗ dễ nhầm là quan hệ nhưng **không phải**:

- Mở màn hình B khi đang ở màn hình A là **điều hướng giao diện** (ví dụ E02 sang E03, hay nút Lưu trên B03). Nó không tạo «include» hay «extend».
- Use case này chỉ làm được sau use case kia là **tiền điều kiện**, ghi trong đặc tả (ví dụ đánh giá đối tác cần giao dịch đã ở RECEIVED hoặc COMPLETED).
- Các bước con như nhập liệu, kiểm tra hợp lệ, tải ảnh là **bước trong luồng**, không tách thành use case.

## Tra ngược: chức năng sang use case

91 chức năng Đợt này, mỗi dòng thuộc ít nhất một use case. Phép kiểm trong `uc_model.py` bắt buộc điều này, nên không chức năng nào rơi ra ngoài.

| STT | Chức năng | Use case |
| --- | --- | --- |
| 1 | Đăng ký | UC01 |
| 2 | Đăng nhập | UC02 |
| 3 | Quên mật khẩu | UC03 |
| 4 | Chỉnh sửa thông tin cá nhân | UC05 |
| 5 | Chỉnh sửa ảnh hồ sơ | UC05 |
| 6 | Chỉnh sửa địa chỉ | UC06 |
| 7 | Xem đánh giá cá nhân | UC29, UC30 |
| 8 | Xem số liệu hồ sơ | UC30 |
| 9 | Khai báo thông tin định danh | UC31 |
| 10 | Cài đặt thông báo | UC05 (luồng thay thế) |
| 11 | Đổi mật khẩu | UC05 (luồng thay thế) |
| 12 | Xóa tài khoản | UC08 |
| 13 | Chỉnh sửa sản phẩm trên kệ | UC11 |
| 14 | Sắp xếp sản phẩm | UC11 |
| 15 | Ẩn hoặc hiện lại tin | UC11 |
| 16 | Chỉnh sửa nội dung | UC10 |
| 17 | Chỉnh sửa hình ảnh | UC10 |
| 18 | Chỉnh sửa hình thức giao dịch | UC10 |
| 19 | Chỉnh sửa giá cả | UC10 |
| 20 | Offer mong muốn | UC10 |
| 21 | Danh mục | UC10 |
| 22 | Lưu nháp | UC09, UC17 |
| 23 | Từ khóa | UC14 |
| 24 | Danh mục | UC14 |
| 25 | Khoảng giá | UC14 |
| 26 | Tình trạng | UC14 |
| 27 | Khu vực | UC14 |
| 28 | Hình thức giao dịch | UC14 |
| 29 | Kênh chung | UC12 |
| 30 | Kênh đang theo dõi | UC12 |
| 31 | Giá thấp đến cao | UC14 |
| 32 | Giá cao đến thấp | UC14 |
| 33 | Mới đăng | UC14 |
| 34 | Đánh giá tốt | UC14 |
| 35 | Danh mục món đồ quan tâm | UC01 |
| 36 | Gợi ý sản phẩm tương tự | UC16 |
| 37 | Giá tham khảo | UC16 |
| 38 | Tạo mục "Dành cho bạn" | UC12 (luồng thay thế) |
| 39 | Theo dõi người dùng | UC32 |
| 40 | Lưu tin | UC13 |
| 41 | Xem danh sách đã lưu | UC13 |
| 42 | Bỏ lưu | UC13 |
| 43 | Gửi tin nhắn văn bản | UC18 |
| 44 | Gửi hình ảnh | UC18 |
| 46 | Trạng thái hoạt động | UC18 |
| 47 | Gửi đề nghị | UC20 |
| 48 | Chấp nhận đề nghị | UC27 |
| 49 | Từ chối đề nghị | UC27 |
| 50 | Xem thông tin giao dịch | UC25 |
| 51 | Chỉnh sửa thông tin giao dịch | UC25 |
| 52 | Xác nhận giao dịch | UC25 |
| 53 | Xác nhận đã nhận | UC22 |
| 54 | Xác nhận đã giao | UC28 |
| 55 | Hủy giao dịch có lý do | UC23 |
| 56 | Thanh toán giao dịch | UC21 |
| 57 | Xem lịch sử giao dịch | UC19 |
| 58 | Xem lịch sử thanh toán | UC19 (luồng thay thế) |
| 59 | Đánh giá đối tác | UC26 |
| 60 | Xem lịch sử đánh giá | UC30 |
| 61 | Tạo báo cáo | UC33 |
| 62 | Hủy báo cáo | UC33 |
| 63 | Xem lịch sử báo cáo | UC33 |
| 64 | Tạo đơn khiếu nại | UC24 |
| 65 | Theo dõi tình trạng đơn khiếu nại | UC24 |
| 66 | Gỡ bỏ đơn khiếu nại | UC24 |
| 67 | Xem thông báo | UC07 |
| 68 | Tìm kiếm, lọc người dùng | UC34 |
| 69 | Xem chi tiết hồ sơ | UC34 |
| 70 | Khóa tài khoản | UC35 |
| 71 | Mở khóa tài khoản | UC35 |
| 72 | Cảnh báo theo bậc | UC35 |
| 73 | Duyệt yêu cầu xác thực tài khoản | UC31, UC36 |
| 74 | Xem nhật ký hoạt động | UC34 |
| 75 | Danh sách tin bị báo cáo | UC37 |
| 76 | Ẩn, gỡ tin vi phạm | UC37 |
| 77 | Gỡ hàng loạt | UC35 |
| 78 | Danh sách từ khóa cấm | UC38 |
| 79 | Thêm, sửa, xóa danh mục | UC39 |
| 80 | Cấu hình thuộc tính tình trạng | UC39 |
| 81 | Danh sách giao dịch | UC40 |
| 82 | Chi tiết giao dịch | UC40 |
| 84 | Giải ngân tự động | UC22 (luồng thay thế) |
| 87 | Khiếu nại chờ xử lý, đã xử lý | UC41 |
| 88 | Xem chi tiết hồ sơ | UC41 |
| 89 | Hoàn tiền, hoàn trả, hủy, bác khiếu nại, yêu cầu bổ sung bằng chứng | UC41 |
| 90 | Ghi chú nội bộ | UC41 |
| 91 | Gửi thông báo kết quả | UC41 |
| 103 | Tìm người dùng theo tên | UC15 |
| 104 | Đăng xuất | UC04 |
| 105 | Thêm, gỡ quyền admin | UC42 |
| 106 | Đăng nhập khu vực quản trị | UC43 |

Hai nhóm dòng cần đọc kỹ.

**Chức năng mà hai use case cùng chạm tới**, vì đó là hai mục tiêu của hai tác nhân khác nhau:

- STT 7: [UC29 Xem hồ sơ của người khác](uc-d-ho-so-tin-cay.md#uc29-xem-hồ-sơ-của-người-khác) và [UC30 Xem hồ sơ uy tín cá nhân](uc-d-ho-so-tin-cay.md#uc30-xem-hồ-sơ-uy-tín-cá-nhân).
- STT 22: [UC09 Quản lý tin nháp](uc-b-tin-dang.md#uc09-quản-lý-tin-nháp) và [UC17 Đăng tin](uc-b-tin-dang.md#uc17-đăng-tin).
- STT 73: [UC31 Đăng ký xác thực người dùng](uc-d-ho-so-tin-cay.md#uc31-đăng-ký-xác-thực-người-dùng) và [UC36 Duyệt yêu cầu xác thực người dùng](uc-e-quan-tri.md#uc36-duyệt-yêu-cầu-xác-thực-người-dùng).

**Chức năng nằm trong một luồng thay thế** chứ không phải mục tiêu chính của ai:

- STT 10: [UC05 Quản lý thông tin cá nhân](uc-a-tai-khoan.md#uc05-quản-lý-thông-tin-cá-nhân), bật tắt từng loại thông báo.
- STT 11: [UC05 Quản lý thông tin cá nhân](uc-a-tai-khoan.md#uc05-quản-lý-thông-tin-cá-nhân), đổi mật khẩu khi đang đăng nhập.
- STT 38: [UC12 Xem kênh tin](uc-b-tin-dang.md#uc12-xem-kênh-tin), mục Dành cho bạn trên trang chủ.
- STT 58: [UC19 Xem lịch sử giao dịch](uc-c-giao-dich.md#uc19-xem-lịch-sử-giao-dịch), tab lịch sử thanh toán.
- STT 84: [UC22 Xác nhận đã nhận hàng](uc-c-giao-dich.md#uc22-xác-nhận-đã-nhận-hàng), hết bảy ngày đếm ngược mà người mua chưa xác nhận thì hệ thống tự giải ngân.

## Lịch sử sửa mô hình

Rà soát ngày 2026-10-13 theo phản biện của leader. Thay đổi so với bản 41 use case trước đó:

- Bỏ hai tác nhân: Khách, Hệ thống hẹn giờ.
- Bỏ use case tự giải ngân và hoàn tất giao dịch; STT 84 thành luồng thay thế của UC25.
- Bỏ 7 quan hệ «include» và «extend» đặt sai, giữ lại 1.
- Tách Cài đặt tài khoản thành UC06 Đổi mật khẩu và UC07 Cài đặt thông báo.
- Đổi tên 13 use case theo quy ước ở trên.
- Chuyển STT 60 từ use case xem hồ sơ người khác sang use case xem hồ sơ cá nhân.
- Thêm UC15 Tìm kiếm người dùng (STT 103 mới).
- Đánh số lại toàn bộ một lần; bảng tra mã cũ sang mã mới ở `local/ra-soat-use-case.md` mục 7.
