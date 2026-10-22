# Giao diện (mockup)

Giai đoạn 2 của đồ án. 30 màn hình có ảnh, vẽ bằng Figma, nộp dạng ảnh PNG khung điện thoại dọc (tỉ lệ 390 x 844), cộng 3 màn hình bổ sung chưa có ảnh (C07, D07 và E07), tổng cộng 33 mã màn hình. Chia 5 nhóm theo luồng nghiệp vụ để không ai sửa chung một file.

Mỗi nhóm nộp theo 3 đợt: đợt 1 một màn, đợt 2 hai màn, đợt 3 ba màn. Xem chi tiết từng màn trong README của nhóm.

**Mọi số trong tài liệu giao diện là số STT của [bảng đặc tả chức năng](../product/dac-ta-chuc-nang.md)** (đánh số lại ngày 2026-10-11). Không dùng số dòng của bảng gốc. Khi ảnh mockup và mô tả chữ khác nhau, mô tả chữ đúng; từng README nhóm có mục "Khác với ảnh" liệt kê chỗ khác.

## Mục lục 33 mã màn hình

Tên lớp lấy từ [bản đồ sở hữu](../../plan/so-huu.md) mục 3. Một mã màn hình có thể gồm nhiều lớp Fragment.

| Mã | Tên màn hình | Lớp giao diện | Activity | Nhóm |
| --- | --- | --- | --- | --- |
| A01 | Đăng nhập | `LoginFragment` | `AuthActivity` | [a-tai-khoan](a-tai-khoan/README.md) |
| A02 | Đăng ký (và hoàn tất hồ sơ sau lần đăng nhập Google đầu tiên) | `RegisterFragment` | `AuthActivity` | [a-tai-khoan](a-tai-khoan/README.md) |
| A03 | Quên mật khẩu | `ForgotPasswordFragment` | `AuthActivity` | [a-tai-khoan](a-tai-khoan/README.md) |
| A04 | Quản lý thông tin cá nhân | `PersonalInfoFragment` | `MainActivity` | [a-tai-khoan](a-tai-khoan/README.md) |
| A05 | Chỉnh sửa hồ sơ và cài đặt tài khoản | `EditProfileFragment`, `AccountSettingsFragment` | `MainActivity` | [a-tai-khoan](a-tai-khoan/README.md) |
| A06 | Quản lý địa chỉ | `AddressListFragment`, `AddressEditFragment` | `MainActivity` | [a-tai-khoan](a-tai-khoan/README.md) |
| B01 | Trang chủ | `HomeFragment` | `MainActivity` (tab Trang chủ) | [b-kham-pha-tin-dang](b-kham-pha-tin-dang/README.md) |
| B02 | Tìm kiếm và bộ lọc | `SearchFragment` | `MainActivity` (tab Tìm kiếm) | [b-kham-pha-tin-dang](b-kham-pha-tin-dang/README.md) |
| B03 | Chi tiết tin đăng | `ListingDetailFragment` | `MainActivity` | [b-kham-pha-tin-dang](b-kham-pha-tin-dang/README.md) |
| B04 | Đăng tin | `PostListingFragment` | `MainActivity` (tab Đăng tin) | [b-kham-pha-tin-dang](b-kham-pha-tin-dang/README.md) |
| B05 | Kệ hàng của tôi | `MyShopFragment` | `MainActivity` | [b-kham-pha-tin-dang](b-kham-pha-tin-dang/README.md) |
| B06 | Tin đã lưu | `SavedListingsFragment` | `MainActivity` | [b-kham-pha-tin-dang](b-kham-pha-tin-dang/README.md) |
| C01 | Danh sách tin nhắn | `ConversationListFragment` | `MainActivity` (tab Tin nhắn) | [c-ket-noi-giao-dich](c-ket-noi-giao-dich/README.md) |
| C02 | Trò chuyện và đề nghị giá | `ChatFragment` | `MainActivity` | [c-ket-noi-giao-dich](c-ket-noi-giao-dich/README.md) |
| C03 | Chi tiết giao dịch | `DealDetailFragment` | `MainActivity` | [c-ket-noi-giao-dich](c-ket-noi-giao-dich/README.md) |
| C04 | Lịch sử giao dịch và thanh toán | `DealHistoryFragment` | `MainActivity` | [c-ket-noi-giao-dich](c-ket-noi-giao-dich/README.md) |
| C05 | Viết đánh giá | `WriteReviewFragment` | `MainActivity` | [c-ket-noi-giao-dich](c-ket-noi-giao-dich/README.md) |
| C06 | Thông báo | `NotificationListFragment` | `MainActivity` | [c-ket-noi-giao-dich](c-ket-noi-giao-dich/README.md) |
| C07 (chưa có ảnh) | Thanh toán giao dịch | `DealPaymentFragment` | `MainActivity` | [c-ket-noi-giao-dich](c-ket-noi-giao-dich/README.md) |
| D01 | Hồ sơ cá nhân | `MyProfileFragment` | `MainActivity` (tab Cá nhân) | [d-ho-so-tin-cay](d-ho-so-tin-cay/README.md) |
| D02 | Trang cá nhân | `UserProfileFragment` | `MainActivity` | [d-ho-so-tin-cay](d-ho-so-tin-cay/README.md) |
| D03 | Hồ sơ uy tín người bán | `SellerTrustFragment` | `MainActivity` | [d-ho-so-tin-cay](d-ho-so-tin-cay/README.md) |
| D04 | Báo cáo | `ReportFragment` | `MainActivity` | [d-ho-so-tin-cay](d-ho-so-tin-cay/README.md) |
| D05 | Tạo khiếu nại | `CreateComplaintFragment` | `MainActivity` | [d-ho-so-tin-cay](d-ho-so-tin-cay/README.md) |
| D06 | Admin: Xử lý khiếu nại | `AdminComplaintListFragment`, `AdminComplaintDetailFragment` | `AdminActivity` | [d-ho-so-tin-cay](d-ho-so-tin-cay/README.md) |
| D07 (chưa có ảnh) | Báo cáo và khiếu nại của tôi | `MyReportsFragment` | `MainActivity` | [d-ho-so-tin-cay](d-ho-so-tin-cay/README.md) |
| E01 | Admin: Bảng điều khiển | `AdminDashboardFragment` | `AdminActivity` | [e-quan-tri](e-quan-tri/README.md) |
| E02 | Admin: Danh sách người dùng | `AdminUserListFragment` | `AdminActivity` | [e-quan-tri](e-quan-tri/README.md) |
| E03 | Admin: Chi tiết người dùng | `AdminUserDetailFragment` | `AdminActivity` | [e-quan-tri](e-quan-tri/README.md) |
| E04 | Admin: Kiểm duyệt nội dung | `AdminModerationFragment` | `AdminActivity` | [e-quan-tri](e-quan-tri/README.md) |
| E05 | Admin: Quản lý danh mục | `AdminCategoryFragment` | `AdminActivity` | [e-quan-tri](e-quan-tri/README.md) |
| E06 | Admin: Giám sát giao dịch | `AdminDealMonitorFragment` | `AdminActivity` | [e-quan-tri](e-quan-tri/README.md) |
| E07 (chưa có ảnh) | Admin: Quản lý tài khoản admin | `AdminAccountFragment` | `AdminActivity` | [e-quan-tri](e-quan-tri/README.md) |

**Màn hình phụ không có mã riêng và chưa có ảnh**, nằm trong `AdminActivity`:

- `AdminListingDetailFragment`: chi tiết tin cho admin, mở từ E04.
- `AdminDealDetailFragment`: chi tiết giao dịch kèm `statusHistory` cho admin, mở từ E06.

## Điều hướng

Ứng dụng có ba Activity. Mỗi Activity dùng Navigation component, mỗi module có đồ thị điều hướng riêng; mọi lối đi chéo module dùng các hành động toàn cục trong `nav_main.xml` (xem bảng ở [bản đồ sở hữu](../../plan/so-huu.md) mục 4, ví dụ `action_global_sign_in`, `action_global_my_profile`, `action_global_tab`).

| Activity | Chứa | Vào bằng cách nào |
| --- | --- | --- |
| `AuthActivity` | A01, A02, A03 | Khách bấm "Đăng nhập" hoặc "Đăng ký" ở B01, hoặc bị đưa tới A01 khi bấm một thao tác cần đăng nhập (xem mục "Chế độ khách") |
| `MainActivity` | Mọi màn hình của người dùng (nhóm A từ A04, B, C, D trừ D06). Mở được cả khi chưa đăng nhập (chế độ khách), nhưng khi đó chỉ vào được B01, B02, B03 | Mở ứng dụng: chưa có phiên đăng nhập thì vào B01 ở chế độ khách. Sau khi đăng nhập thành công thì vào B01 đầy đủ. Đăng nhập luôn vào B01, kể cả tài khoản admin |
| `AdminActivity` | E01 đến E07, D06 và hai màn hình phụ của admin | Nút "Khu vực quản trị" trên D01, chỉ hiện khi tài khoản là admin (`SessionManager.isAdmin()`). Không có màn đăng nhập riêng cho admin: UC43 (STT 106) là lối đi này, admin đăng nhập ở A01 như mọi người |

**Chế độ khách.** Người chưa đăng nhập (Khách) xem được đúng ba thứ: danh sách tin trên trang chủ (B01), tìm kiếm tin đăng (B02) và chi tiết tin đăng (B03). Khách không xem hồ sơ người khác, không tìm người dùng, không nhắn tin, không lưu tin, không gửi đề nghị, không theo dõi, không báo cáo. Mọi thao tác ngoài ba thứ trên đưa Khách tới A01 bằng `action_global_sign_in` (có chỗ hiện hộp thoại "Vui lòng đăng nhập" trước, xem README nhóm B). Mọi màn hình còn lại cần phiên đăng nhập. Chi tiết từng màn ở [nhóm B](b-kham-pha-tin-dang/README.md).

**Điều kiện đã được admin duyệt xác thực.** Đăng tin, gửi đề nghị và tạo giao dịch chỉ dành cho tài khoản đã được admin duyệt xác thực (không còn là huy hiệu). Tài khoản chưa được duyệt vẫn xem, tìm kiếm, nhắn tin và theo dõi bình thường; khi bấm một thao tác mua bán, màn hình báo rõ lý do và dẫn tới chỗ gửi yêu cầu xác thực ở D01 (nút "Đăng ký xác thực", `action_global_my_profile`).

**Thanh điều hướng dưới (5 tab)** chỉ có trong `MainActivity`: Trang chủ (B01), Tìm kiếm (B02), Đăng tin (B04, nút tròn nổi ở giữa), Tin nhắn (C01), Cá nhân (D01). Ở chế độ khách chỉ hai tab Trang chủ và Tìm kiếm dùng được; bấm Đăng tin, Tin nhắn hoặc Cá nhân đưa Khách tới A01.

- Thanh dưới **chỉ hiện ở 5 màn hình gốc của tab**: B01, B02, B04, C01, D01.
- Mọi màn hình khác của `MainActivity` là **màn hình chi tiết**: có nút quay lại ở góc trái trên, ẩn thanh dưới. Quay lại trả về màn hình đã gọi nó. C07 Thanh toán giao dịch chỉ mở từ nút "Thanh toán" trên C03 (`action_global_deal_payment`).
- `AuthActivity` không có thanh dưới. `AdminActivity` không có thanh dưới; E01 là màn hình gốc, các màn hình khác (kể cả E07, mở từ ô "Tài khoản quản trị" trên E01 bằng `action_global_admin_accounts`) có nút quay lại, và E01 có nút "Về ứng dụng" để quay lại `MainActivity`.
- Chuông thông báo nằm ở thanh trên của `MainActivity` (hiện trên B01) và mở C06. Ở chế độ khách chuông và ảnh đại diện không hiện, thay bằng nút "Đăng nhập" (xem B01).
- Một số ảnh mockup (B05, B06, C06, D02) vẽ thanh dưới trên màn hình chi tiết. Theo quy tắc trên, thanh dưới không hiện ở đó; xem mục "Khác với ảnh" của từng nhóm.

**Bốn trạng thái và ViewModel.** Bốn trạng thái mà mục 6 của [giao diện](../conventions/giao-dien.md) đòi ở mọi màn hình (đang tải, có dữ liệu, rỗng, lỗi) nay có nguồn dữ liệu thật là ba thuộc tính `loading`, `error`, `done` mà mọi ViewModel đều có (xem [viewmodel.md](../architecture/dac-ta-lop/viewmodel.md)). Màn hình đọc ba thuộc tính đó thay vì tự giữ cờ trong Fragment. `done` bật lên đúng một lần khi một thao tác ghi xong, để màn hình đóng hộp thoại hoặc quay lại.

**Ảnh người dùng gửi** (ảnh tin đăng, ảnh trong chat, ảnh vận đơn, ảnh bằng chứng khiếu nại, ảnh bằng chứng báo cáo, ảnh đại diện): nén trên máy rồi lưu trong Firestore, tối đa 5 ảnh mỗi tin, tối đa 3 ảnh mỗi báo cáo. Dự án dùng gói Firebase miễn phí, không có Cloud Storage. **Không có video** ở bất kỳ màn hình nào (STT 45 là Phát triển sau).

## Ngoài phạm vi đợt này

- Bố cục xoay ngang (landscape) cho các màn chính: để lại cho giai đoạn code.
- Màn E01: 4 ô số liệu tổng (người dùng, tin đăng, giao dịch, khiếu nại) có trong mockup nhưng thuộc chức năng "Số liệu tổng quan" (STT 92), là **phát triển sau**, chưa làm. Đợt này E01 chỉ gồm danh sách "Cần xử lý" và lưới lối tắt quản lý.
- Màn E06 chỉ hiển thị trạng thái giải ngân (đã tự động, STT 84), không có nút duyệt tay (STT 86). Nút lọc "Bất thường" và dòng "Đứng yên quá 3 ngày" (STT 83) là phát triển sau.
- Gửi video trong chat (STT 45) và thông báo hệ thống do admin gửi (STT 99) là phát triển sau.

## Quy ước tên file ảnh

`ui/<nhóm>/<mã>-<tên-khong-dau>.png`, ví dụ `ui/a-tai-khoan/a01-dang-nhap.png`. Màn hình ghi "chưa có ảnh" chưa có file ảnh.
