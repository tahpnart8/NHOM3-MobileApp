# P2. Tính năng

**Thời gian:** 14/10 đến 22/10. **Chủ:** mỗi module một người, xem [so-huu.md](so-huu.md).

**Mục tiêu:** mọi chức năng Đợt này chạy được trên máy thật với Firebase thật.

**Cách làm một việc:**

1. Mỗi việc là một lát cắt dọc: layout, Fragment, ViewModel, phương thức repository và test của nó. Một việc là một issue và một Pull Request.
2. Trong một module, làm theo thứ tự trong bảng. Giữa các module không có phụ thuộc, trừ P2-A1 (đăng nhập), việc cả nhóm cần để chạy thử.
3. Phương thức của module khác mà mình cần nhưng chưa xong thì đã có sẵn khung báo `NOT_READY`. Màn hình của mình phải hiện lỗi đó rõ ràng, không giả dữ liệu. Không tự viết thay phần của người khác.

**Điều kiện hoàn thành:**

- Mọi việc dưới đây đã merge.
- Mỗi màn hình chạy được luồng chính trên máy ảo hoặc máy thật, có ghi lại trong Pull Request.
- Không còn phương thức nào trả `NOT_READY` trong phạm vi Đợt này.

## A. Tài khoản và thông báo (Ngân)

| Mã | Việc | STT | Màn | Phụ thuộc |
| --- | --- | --- | --- | --- |
| P2-A1 | Đăng nhập email và Google, quên mật khẩu, kiểm tra khóa và tài khoản đã xóa, đọc `admins/{uid}`, mở `MainActivity` hoặc ở lại `AuthActivity`. **Làm đầu tiên** | 2, 3 | A01, A03 | P1-04, P1-06 |
| P2-A2 | Đăng ký: thông tin, đồng ý điều khoản, số CCCD vào tài liệu riêng tư, chọn danh mục quan tâm, gửi email xác thực; cùng màn này làm bước hoàn tất hồ sơ sau lần đăng nhập Google đầu tiên | 1, 9, 35 | A02 | P2-A1 |
| P2-A3 | Quản lý thông tin cá nhân, chỉnh sửa hồ sơ, ảnh đại diện, thông tin định danh | 4, 5, 9 | A04, A05 | P2-A1 |
| P2-A4 | Quản lý địa chỉ giao nhận, địa chỉ mặc định | 6 | A06 | P2-A1 |
| P2-A5 | Cài đặt tài khoản: cài đặt thông báo, đổi mật khẩu (xác thực lại), xóa tài khoản (xác thực lại rồi vô hiệu hóa) | 10, 11, 12 | A05 | P2-A3 |
| P2-A6 | Danh sách thông báo, đánh dấu đã đọc, số chưa đọc trên biểu tượng chuông, chạm vào thì mở đúng màn | 67 | C06 | P2-A1 |

## B. Tin đăng và khám phá (Tú)

| Mã | Việc | STT | Màn | Phụ thuộc |
| --- | --- | --- | --- | --- |
| P2-B1 | Đăng tin và lưu nháp: danh mục, tình trạng và thuộc tính theo danh mục, 1 đến 5 ảnh, hình thức giao dịch, giá, offer mong muốn và mức tối thiểu, gắn cờ từ khóa cấm | 16 đến 22 | B04 | P1-04 |
| P2-B2 | Kệ hàng của tôi: danh sách, sắp xếp, sửa, ẩn hoặc hiện, đánh dấu đã bán, đăng bản nháp, xóa | 13, 14, 15 | B05 | P2-B1 |
| P2-B3 | Trang chủ: kênh chung và kênh đang theo dõi, phân trang 20 tin bằng con trỏ, bộ nhớ đệm Room (chỉ ảnh bìa thu nhỏ), dải báo offline | 29, 30 | B01 | P1-05 |
| P2-B4 | Tìm kiếm và bộ lọc: từ khóa, danh mục, giá, tình trạng, khu vực và bán kính, hình thức, 4 cách sắp xếp, lịch sử tìm kiếm | 23 đến 28, 31 đến 34 | B02 | P2-B3 |
| P2-B5 | `RecommendationEngine` đúng [goi-y.md](../docs/architecture/goi-y.md) kèm unit test; mục Dành cho bạn trên trang chủ | 36, 37, 38 | B01 | P2-B3 |
| P2-B6 | Chi tiết tin: ảnh, thông tin, người bán, tin tương tự, giá tham khảo, lưu tin, ghi lịch sử xem, nút chat, báo cáo, xem hồ sơ người bán | 36, 37, 40 | B03 | P2-B5 |
| P2-B7 | Tin đã lưu: danh sách, bỏ lưu | 41, 42 | B06 | P2-B6 |

## C. Chat và giao dịch (Hải Long)

| Mã | Việc | STT | Màn | Phụ thuộc |
| --- | --- | --- | --- | --- |
| P2-C1 | Danh sách tin nhắn, mở hoặc tạo cuộc trò chuyện từ chi tiết tin, bộ nhớ đệm Room | 43 | C01 | P1-05 |
| P2-C2 | Trò chuyện: gửi chữ và ảnh theo thời gian thực, trạng thái hoạt động của người đối thoại | 43, 44, 46 | C02 | P2-C1 |
| P2-C3 | Offer một chạm trong chat: gửi theo loại người bán nhận, chấp nhận, từ chối; chấp nhận thì tạo giao dịch và giữ chỗ tin | 47 đến 49 | C02 | P2-C2 |
| P2-C4 | Chi tiết giao dịch: xem, sửa điều khoản, hai bên xác nhận, hủy có lý do, lịch sử trạng thái | 50, 51, 52, 55 | C03 | P2-C3 |
| P2-C5 | Thanh toán ký quỹ mô phỏng, giao hàng kèm ảnh vận đơn, đồng hồ đếm ngược 7 ngày, xác nhận đã nhận và giải ngân, đường gặp trực tiếp | 53, 54, 56 | C03 | P2-C4 |
| P2-C6 | Lịch sử giao dịch và lịch sử thanh toán, bộ nhớ đệm Room | 57, 58 | C04 | P2-C4 |
| P2-C7 | Viết đánh giá (sao tổng và 3 tiêu chí), `DealRepository.completeDeal` (nơi duy nhất chuyển sang COMPLETED) khi hai bên đã đánh giá | 59 | C05 | P2-C5 |

## D. Hồ sơ và tin cậy (Phúc)

| Mã | Việc | STT | Màn | Phụ thuộc |
| --- | --- | --- | --- | --- |
| P2-D1 | Hồ sơ cá nhân: số liệu, lối tắt tới kệ hàng, tin đã lưu, lịch sử giao dịch, chỉnh sửa, cài đặt; gửi yêu cầu xác minh người bán; mở khu quản trị khi là admin | 8, 73 (phía người dùng) | D01 | P1-06 |
| P2-D2 | Trang cá nhân người khác: thông tin công khai, tin đang bán, theo dõi và bỏ theo dõi | 7, 39 | D02 | P2-D1 |
| P2-D3 | Hồ sơ uy tín người bán: điểm, 3 tiêu chí, tỷ lệ hoàn tất, lịch sử đánh giá đã nhận và đã cho | 7, 60 | D03 | P2-D2 |
| P2-D4 | Báo cáo tin, người hoặc tin nhắn: lý do chọn sẵn, mô tả; thẻ "Báo cáo của tôi" trên D07: hủy, xuất CSV | 61, 62, 63 | D04, D07 | P1-04 |
| P2-D5 | Khiếu nại: tạo kèm ảnh bằng chứng (mỗi giao dịch một đơn, trước khi hết đếm ngược); thẻ "Khiếu nại của tôi" trên D07: theo dõi, rút đơn, bổ sung bằng chứng khi admin yêu cầu | 64, 65, 66 | D05, D07 | P2-D4 |
| P2-D6 | Admin xử lý khiếu nại: danh sách theo trạng thái và ưu tiên, hồ sơ (giao dịch, bằng chứng, lịch sử chat), ghi chú nội bộ, ra quyết định và áp hệ quả, thông báo hai bên | 87 đến 91 | D06 | P2-D5 |

## E. Quản trị (Phát)

| Mã | Việc | STT | Màn | Phụ thuộc |
| --- | --- | --- | --- | --- |
| P2-E1 | Bảng điều khiển admin: danh sách "Cần xử lý" và lưới lối tắt (không có ô số liệu tổng) | 68, 75, 81, 87 (lối vào) | E01 | P1-06 |
| P2-E2 | Danh sách và chi tiết người dùng: lọc, hồ sơ, khóa, mở khóa, cảnh báo theo bậc, nhật ký hoạt động, duyệt xác minh | 68 đến 74 | E02, E03 | P2-E1 |
| P2-E3 | Kiểm duyệt nội dung: báo cáo theo loại đối tượng, xem tin trong khu quản trị, ẩn có lý do, khôi phục, gỡ hàng loạt, từ khóa cấm và tin bị gắn cờ | 75 đến 78 | E04 | P2-E1 |
| P2-E4 | Quản lý danh mục và thuộc tính tình trạng theo danh mục | 79, 80 | E05 | P2-E1 |
| P2-E5 | Giám sát giao dịch: danh sách theo trạng thái, màn chi tiết riêng trong khu quản trị kèm lịch sử | 81, 82 | E06 | P2-E1 |
| P2-E6 | Việc tự động của giao dịch: `releaseDueDeals`, `completeDueDeals`, `expirePendingDeals`, `AutoReleaseWorker` mỗi 15 phút, kiểm tra khi mở app | 84 | | P1-04 |
