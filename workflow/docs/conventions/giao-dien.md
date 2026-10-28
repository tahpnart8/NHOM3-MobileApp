# Hệ thống thiết kế giao diện

Nguồn sự thật về **giao diện trông như thế nào**. Năm người làm 33 màn hình; không có tài liệu này thì ra năm ứng dụng khác nhau.

Cách dùng: lấy **tên token** ở đây mà viết layout, đừng viết thẳng giá trị. Luật thi hành nằm trong skill [`pubg-ui`](../../../.agents/skills/pubg-ui/SKILL.md). Ai sửa được tệp nào thì xem [`so-huu.md`](../../plan/so-huu.md) mục 6: `colors.xml`, `themes.xml`, `dimens.xml` và `strings_common.xml` chỉ leader sửa, qua issue gắn nhãn `area:ui`.

## 1. Token màu

Mã màu rút ra từ 30 ảnh mockup ngày 2026-10-22 bằng cách đếm điểm ảnh, không phải từ tệp Figma gốc. Mỗi mã dưới đây là màu có thật trong ảnh, trừ `colorInfo` (xem ghi chú). Khi làm P1-03 (tạo `colors.xml` và `themes.xml`) thì chép thẳng bảng này; nếu bản Figma gốc có mã khác thì Figma thắng và sửa lại ở đây trong cùng Pull Request. Viết layout thì dùng **tên token**, không viết thẳng mã màu.

| Token | Thuộc tính theme | Dùng cho | Giá trị |
| --- | --- | --- | --- |
| `colorPrimary` | `?attr/colorPrimary` | Nút chính, chip đang chọn, chỉ báo tab | `#1F6F54` |
| `colorOnPrimary` | `?attr/colorOnPrimary` | Chữ và biểu tượng nằm trên nền chính | `#FFFFFF` |
| `colorSurface` | `?attr/colorSurface` | Nền thẻ, nền hộp thoại | `#FFFFFF` |
| `colorOnSurface` | `?attr/colorOnSurface` | Chữ chính | `#1E1E1C` |
| `colorOnSurfaceVariant` | `?attr/colorOnSurfaceVariant` | Chữ phụ, nhãn, chú thích | `#6E6B62` |
| `colorBackground` | `?android:attr/colorBackground` | Nền màn hình | `#FAF8F4` |
| `colorOutline` | `?attr/colorOutline` | Đường kẻ phân cách, viền thẻ phẳng và chip (viền ô nhập dùng `outline_strong`, mục 1a) | `#E7E2D6` |
| `colorError` | `?attr/colorError` | Lỗi, nút nguy hiểm, nhãn khóa tài khoản | `#B2412F` |
| `colorWarning` | `@color/warning` | Cảnh báo, nhãn chờ xử lý | `#9E5223` |
| `colorSuccess` | `@color/success` | Thành công, nhãn hoàn tất | `#1F6F54` |
| `colorInfo` | `@color/info` | Dải thông tin, dải ngoại tuyến | `#27628C` |

Ba token cuối không có trong bộ Material mặc định nên khai thêm trong `colors.xml`.

Ba điều cần biết khi đọc bảng trên:

- **`colorSuccess` bằng đúng `colorPrimary`.** Mockup chỉ có một màu xanh lá. Nhãn "Đang bán", "Hoàn tất", "Đã xác thực" vì vậy dùng chung màu với nút chính; phân biệt bằng nền chip chứ không bằng màu chữ.
- **`colorInfo` không có trong mockup.** Mockup chỉ dùng ba màu có nghĩa: xanh lá, cam và đỏ gạch. Mã `#27628C` là màu xanh dương tôi chọn thêm, cùng độ đậm và độ bão hòa với `#1F6F54` để không chọi với bảng màu ấm. Nếu nhóm muốn bỏ hẳn màu thứ tư thì phải đổi mục 4: CONFIRMED và SHIPPED sẽ trùng màu với một nhóm trạng thái khác.
- **`colorWarning` đậm hơn mockup một bậc.** Mockup dùng `#C0632B`, nhưng đặt trên nền chip be `#F5E4D8` thì tỉ lệ tương phản chỉ 3.35, dưới mức 4.5 mà WCAG AA đòi cho chữ nhỏ. `#9E5223` là cùng màu đó tối đi (cùng hệ màu, cùng độ bão hòa), đạt 4.61 trên nền be và 5.37 trên nền màn hình. Mắt thường gần như không thấy khác; nếu nhóm muốn giữ đúng mockup thì phải chấp nhận chữ khó đọc hoặc đổi nền chip.
- **Ba màu nền của chip** không phải token theme, khai thẳng trong `colors.xml`: nền xanh nhạt `#E4F0EA` đi với chữ `colorPrimary` hoặc `colorSuccess`; nền be `#F5E4D8` đi với chữ `colorWarning`; nhãn lỗi thì tô kín `colorError` với chữ `colorOnPrimary`, vì mockup không có nền đỏ nhạt.

Mọi cặp chữ và nền trong bảng trên đã được kiểm tỉ lệ tương phản theo WCAG AA (chữ nhỏ cần 4.5). Thấp nhất là chip be `#9E5223` trên `#F5E4D8` với 4.61; cao nhất là chữ chính trên thẻ với 16.70. Thêm màu mới thì kiểm lại, đừng đoán bằng mắt.

### 1a. Các vai màu còn lại của Material 3

Từ đợt làm mới giao diện (issue #349), theme khai đủ mọi vai màu của Material 3, suy ra từ bảng trên, để không thành phần nào rơi về màu tím mặc định. Layout vẫn dùng dạng `?attr/...`.

| Thuộc tính theme | Giá trị | Dùng cho | Cặp chữ trên nền, tỉ lệ |
| --- | --- | --- | --- |
| `colorPrimaryContainer` / `colorOnPrimaryContainer` | `#CDEBDD` / `#0B3B2B` | Chip lọc đang chọn, nền viên thuốc của tab đang chọn ở thanh dưới | 9.87; `colorPrimary` trên nền này 4.78 |
| `colorSecondary` / `colorOnSecondary` | `#3F5A4D` / `#FFFFFF` | Thành phần Material cần màu phụ | 7.55 |
| `colorSecondaryContainer` / `colorOnSecondaryContainer` | `#D9EDE3` / `#0B3B2B` | Nút tonal | 10.25 |
| `colorTertiary` / `colorOnTertiary` | `#C2410C` / `#FFFFFF` | **Màu nhấn duy nhất**: huy hiệu đếm số, nhãn "Mới" và nhãn loại tin. Không dùng cho giá, không dùng cho nút | 5.18; chữ `colorTertiary` trên nền màn hình 4.88 |
| `colorTertiaryContainer` / `colorOnTertiaryContainer` | `#FFE3D3` / `#6B2A07` | Nền nhãn nhấn nhạt | 8.77 |
| `colorSurfaceContainerLowest` | `#FFFFFF` | Như `colorSurface` | |
| `colorSurfaceContainerLow` | `#F7F4EE` | Nền nhóm nhẹ | `colorOnSurfaceVariant` 4.85 |
| `colorSurfaceContainer`, `colorSurfaceVariant` | `#F3EFE8` | Nền ô tìm kiếm, khối phụ trong thẻ | `colorOnSurfaceVariant` 4.65 |
| `colorSurfaceContainerHigh` | `#EFEBE3` | Nền khối nổi hơn | chỉ dùng `colorOnSurface` (`colorOnSurfaceVariant` chỉ 4.48) |
| `colorSurfaceContainerHighest` | `#EBE6DD` | Nền khối nổi nhất | chỉ dùng `colorOnSurface` (13.43) |
| `colorSurfaceDim` | `#E2DDD3` | Nền mờ | |
| `colorSurfaceInverse` / `colorOnSurfaceInverse` / `colorPrimaryInverse` | `#2F2E2B` / `#F4F1EA` / `#8FD3B4` | Snackbar và nút hành động của nó | 12.04; nút 7.85 |
| `colorOutlineVariant` | `#EFEBE2` | Đường kẻ phân cách rất nhạt | không mang chữ |
| `colorOnError` | `#FFFFFF` | Chữ trên nền `colorError` | 5.69 |
| `colorErrorContainer` / `colorOnErrorContainer` | `#F9DED8` / `#5C1A10` | Khối báo lỗi nhạt | 10.23; chữ `colorError` trên nền này 4.46, không dùng cho chữ nhỏ |

Hai màu không phải vai Material, khai trong `colors.xml`:

- `outline_strong` `#8F8A7E`: viền ô nhập lúc chưa chọn. Viền của một điều khiển cần 3:1 so với nền; `colorOutline` `#E7E2D6` chỉ 1.2 nên chỉ dùng cho đường kẻ và viền thẻ. Đạt 3.44 trên nền trắng.
- `primary_dark` `#17593F`: đầu đậm của dải chuyển màu xanh (`bg_top_bar`, thanh trên của Trang chủ). Chữ trắng trên đó 8.28, trên đầu nhạt `colorPrimary` 6.07.

## 2. Khoảng cách, bo góc, độ nổi

Thang khoảng cách là bội của 4. Không dùng giá trị ngoài thang.

| Token | Giá trị | Dùng cho |
| --- | --- | --- |
| `space_xs` | 4dp | Khe giữa biểu tượng và chữ |
| `space_sm` | 8dp | Khe giữa hai dòng trong một thẻ |
| `space_md` | 16dp | Lề trái phải của màn hình, lề trong thẻ |
| `space_lg` | 24dp | Khe giữa hai nhóm nội dung |
| `space_xl` | 32dp | Khe trên dưới của một mục lớn |
| `radius_sm` | 8dp | Khối nhỏ (`shapeAppearanceCornerSmall`) |
| `radius_md` | 12dp | Ô nhập, snackbar |
| `radius_lg` | 16dp | Thẻ |
| `radius_xl` | 24dp | Hộp thoại, bảng trượt từ dưới lên (chỉ hai góc trên) |
| `elevation_card` | 2dp | Thẻ |
| `touch_min` | 48dp | Cạnh nhỏ nhất của mọi thứ bấm được |

Nút và chip luôn **bo tròn hẳn** (hình viên thuốc, `ShapeAppearance.PUBGApp.Full`), không có token cỡ.

**Bo góc và độ nổi do theme lo, layout không tự đặt.** Không viết `app:cornerRadius`, `app:cardCornerRadius` hay `app:cardElevation` trong layout; style ở mục 5a đã đặt sẵn. Ngoại lệ duy nhất là thẻ phẳng có viền (`app:cardElevation="0dp"` kèm `app:strokeWidth`), ví dụ bong bóng tin nhắn.

## 3. Thang chữ

Dùng text appearance của theme, không đặt `textSize` tay.

Font của cả app là **Be Vietnam Pro** (giấy phép SIL Open Font License 1.1, văn bản ở `PUBGApp/licenses/be-vietnam-pro-OFL.txt`), đóng gói trong `res/font/` với 4 độ đậm 400, 500, 600, 700 qua họ `@font/be_vietnam_pro`. Font này vẽ dấu tiếng Việt cân và rõ hơn font hệ thống, và giống nhau trên mọi máy. Font đi vào view qua text appearance: mỗi text appearance bên dưới khai `fontFamily` và độ đậm, nên view nào có `android:textAppearance` (mọi chữ theo luật mục 3, nút, chip, ô nhập, tab) đều dùng Be Vietnam Pro. Đừng đặt `fontFamily` ở theme hay trong layout: AppCompat coi nó là thuộc tính của view, nạp lại font ở độ đậm 400 và làm mất mọi chữ đậm (thấy trên emulator, issue #351).

Cả 15 text appearance của Material 3 đều được ghi đè trong `themes.xml` (cỡ giữ theo Material 3, font và độ đậm là của dự án):

| Token | Cỡ | Độ đậm | Dùng cho |
| --- | --- | --- | --- |
| `textAppearanceDisplayLarge`, `Medium`, `Small` | 57, 45, 36sp | 600 | Số lớn (thẻ thống kê admin) |
| `textAppearanceHeadlineLarge`, `Medium` | 32, 28sp | 600 | Tiêu đề lớn |
| `textAppearanceHeadlineSmall` | 24sp | 600 | Tiêu đề màn hình trống, tên người dùng trên trang cá nhân |
| `textAppearanceTitleLarge` | 22sp | 600 | Tiêu đề thanh trên |
| `textAppearanceTitleMedium` | 16sp | 700 | Tiêu đề thẻ, tên món đồ, **giá** |
| `textAppearanceTitleSmall` | 14sp | 600 | Tiêu đề nhóm nhỏ |
| `textAppearanceBodyLarge` | 16sp | 400 | Chữ thường cỡ lớn |
| `textAppearanceBodyMedium` | 14sp | 400 | Chữ thường |
| `textAppearanceBodySmall` | 12sp | 400 | Chữ phụ, thời gian, khu vực |
| `textAppearanceLabelLarge` | 14sp | 600 | Chữ trên nút, chữ tab |
| `textAppearanceLabelMedium` | 12sp | 500 | Chữ thanh điều hướng dưới |
| `textAppearanceLabelSmall` | 11sp | 500 | Chữ huy hiệu, nhãn nhỏ |

Giá cả luôn dùng `textAppearanceTitleMedium` kèm `colorPrimary`, để mắt bắt được giá ở mọi màn hình. Độ đậm 700 của `TitleMedium` là để giá nổi nhất trên thẻ. Hai ngoại lệ có chủ ý (issue #353): tên món đồ trên `item_listing_card` dùng `TitleSmall` để giá đứng trên nó một bậc; màn Chi tiết tin cho giá `HeadlineSmall` và tên `TitleLarge`, vì đó là màn hình của một món đồ. Nhãn loại tin (bán, trao đổi, tặng) dùng màu nhấn `colorTertiary`. Ô tìm kiếm luôn là viên thuốc không viền: trắng trên nền xanh ở Trang chủ, `colorSurfaceContainer` ở màn Tìm kiếm.

## 4. Màu theo trạng thái

Cùng một trạng thái phải cùng một màu ở mọi màn hình. Tên trạng thái lấy từ [`trang-thai.md`](../architecture/trang-thai.md), không đặt tên khác.

| Đối tượng | Trạng thái | Nhãn hiển thị | Token màu |
| --- | --- | --- | --- |
| Tin đăng | DRAFT | Nháp | `colorOnSurfaceVariant` |
| Tin đăng | AVAILABLE | Đang bán | `colorSuccess` |
| Tin đăng | RESERVED | Đang giữ chỗ | `colorWarning` |
| Tin đăng | SOLD | Đã bán | `colorOnSurfaceVariant` |
| Tin đăng | HIDDEN | Đã ẩn | `colorOnSurfaceVariant` |
| Tin đăng | REMOVED | Đã gỡ | `colorError` |
| Giao dịch | PENDING_CONFIRM | Chờ xác nhận | `colorWarning` |
| Giao dịch | CONFIRMED | Đã xác nhận | `colorInfo` |
| Giao dịch | SHIPPED | Đã giao | `colorInfo` |
| Giao dịch | RECEIVED | Đã nhận | `colorSuccess` |
| Giao dịch | COMPLETED | Hoàn tất | `colorSuccess` |
| Giao dịch | CANCELLED | Đã hủy | `colorOnSurfaceVariant` |
| Giao dịch | DISPUTED | Tranh chấp | `colorError` |
| Đề nghị | PENDING | Chờ người bán trả lời | `colorWarning` |
| Đề nghị | ACCEPTED | Đã chấp nhận | `colorSuccess` |
| Đề nghị | REJECTED | Đã từ chối | `colorOnSurfaceVariant` |
| Tài khoản | ACTIVE | Đang hoạt động | `colorSuccess` |
| Tài khoản | TEMP_LOCKED | Khóa tạm | `colorWarning` |
| Tài khoản | PERM_LOCKED | Khóa vĩnh viễn | `colorError` |
| Tài khoản | DELETED | Đã xóa | `colorOnSurfaceVariant` |
| Xác thực | NONE | Chưa gửi yêu cầu | `colorOnSurfaceVariant` |
| Xác thực | PENDING | Đang chờ duyệt | `colorWarning` |
| Xác thực | APPROVED | Đã xác thực | `colorSuccess` |
| Xác thực | REJECTED | Bị từ chối | `colorError` |
| Báo cáo | OPEN | Chờ xử lý | `colorWarning` |
| Báo cáo | RESOLVED | Đã xử lý, có vi phạm | `colorSuccess` |
| Báo cáo | DISMISSED | Đã xem, không vi phạm | `colorOnSurfaceVariant` |
| Báo cáo | CANCELLED | Người báo cáo đã hủy | `colorOnSurfaceVariant` |
| Khiếu nại | OPEN | Chờ xử lý | `colorWarning` |
| Khiếu nại | NEEDS_EVIDENCE | Chờ bổ sung bằng chứng | `colorWarning` |
| Khiếu nại | RESOLVED | Đã có quyết định | `colorSuccess` |
| Khiếu nại | WITHDRAWN | Người khiếu nại đã rút | `colorOnSurfaceVariant` |
| Thanh toán | HELD | Đang giữ | `colorWarning` |
| Thanh toán | RELEASED | Đã giải ngân cho người bán | `colorSuccess` |
| Thanh toán | REFUNDED | Đã hoàn cho người mua | `colorOnSurfaceVariant` |
| Mức ưu tiên | LOW | Thấp | `colorOnSurfaceVariant` |
| Mức ưu tiên | MEDIUM | Trung bình | `colorInfo` |
| Mức ưu tiên | HIGH | Cao | `colorError` |

Cột "Nhãn hiển thị" là **chữ ngắn vẽ trong chip**, và chính bảng này là nguồn của nó (quyết định leader 2026-10-22). [`dac-ta-lop/enums.md`](../architecture/dac-ta-lop/enums.md) giải nghĩa dài hơn cho người đọc tài liệu, ví dụ `SHIPPED` ở đó là "Đã giao, đang đếm ngược" còn chip chỉ ghi "Đã giao". Hai chỗ không mâu thuẫn, nhưng đừng chép câu giải nghĩa vào chip.

Nhãn trạng thái vẽ bằng thành phần `view_status_chip` ở mục 5, không ai tự vẽ lại.

## 5. Thành phần dùng lại

Cái nào xuất hiện ở hai màn hình trở lên thì làm một layout dùng chung, đặt trong `res/layout/` với tiền tố `view_`. Leader tạo ở P1, các module chỉ `include`.

| Thành phần | Tệp | Xuất hiện ở |
| --- | --- | --- |
| Thẻ tin đăng (ảnh bìa, tên, giá, tình trạng, khu vực) | `item_listing_card.xml` | B01, B02, B05, B06, D02 |
| Nhãn trạng thái | `view_status_chip.xml` | Khắp nơi, xem mục 4 |
| Ảnh đại diện chữ cái đầu | `view_avatar.xml` | C01, C02, D01, D02, E02 |
| Hàng sao đánh giá | `view_rating.xml` | B03, C05, D01, D02, D03 |
| Ô trống | `view_empty_state.xml` | Mọi màn hình có danh sách |
| Ô lỗi kèm nút Thử lại | `view_error_state.xml` | Mọi màn hình có tải dữ liệu |
| Dải ngoại tuyến | `view_offline_banner.xml` | B01, B02, B03, C01, C03, C04, D01 |
| Thanh trên của màn chi tiết (nút quay lại, tiêu đề) | `view_detail_toolbar.xml` | Mọi màn hình không phải gốc của tab |
| Nạp ảnh vào `ImageView` (lớp Java) | `ui/common/ImageLoader.java` | Mọi màn hình có ảnh: thẻ tin, chat, hồ sơ, bằng chứng |
| Tô chữ và màu cho nhãn trạng thái (lớp Java) | `ui/common/StatusChips.java` | Mọi màn hình có `view_status_chip` |
| Điền ảnh đại diện vào `view_avatar` (lớp Java) | `ui/common/AvatarBinder.java` | Mọi chỗ có `view_avatar`: thanh trên Trang chủ, hồ sơ, chat, đánh giá, tìm người dùng, quản trị |

**Các lớp Java ở cuối bảng là bắt buộc dùng, không ai tự viết lại.** Giải mã ảnh Base64 trên luồng chính đã xảy ra ba lần (các Pull Request #129, #133 và #135, xem issue #140): giao diện giật, ảnh 1024 px có thể làm tràn bộ nhớ, và `ViewHolder` bị dùng lại thì ảnh gán nhầm sang dòng khác. `ImageLoader.load` giải mã ngoài luồng chính, thu nhỏ khi giải mã, có ảnh giữ chỗ và ảnh dự phòng, và chỉ gán ảnh khi `ImageView` vẫn còn cần đúng ảnh đó. **Có ba bản nạp chồng, chọn theo chỗ bạn đang giữ ảnh:** chuỗi Base64 đọc từ tài liệu Firestore thì `load(String, ...)`; `byte[]` do `MediaRepository.getImage` trả thì `load(byte[], imageId, ...)` và `imageId` chính là chốt chống gán nhầm nên phải truyền đúng mã của ảnh đó; `Uri` người dùng vừa chọn từ bộ chọn ảnh thì `load(Uri, ...)`. Thiếu một trong ba cửa vào này là lý do hai adapter đã phải tự viết và tự viết sai, nên nguồn ảnh mới thì thêm bản nạp chồng thứ tư, đừng giải mã trong adapter. `StatusChips.bind` đặt chữ và màu cho `view_status_chip` đúng bảng ở mục 4, với một phương thức nạp chồng cho mỗi enum trạng thái; chữ lấy từ khóa `common_status_<đối tượng>_<trạng thái>` trong `strings_common.xml`. Màn hình nào cần giải mã ảnh hoặc tô nhãn thì gọi hai lớp này; cần thêm hành vi thì mở issue `area:ui`, đừng sao chép sang module mình.

**Ảnh đại diện:** gọi `AvatarBinder.bind(ViewAvatarBinding avatar, String displayName, String avatarBase64)`, không tự đặt `textInitial` hay ẩn hiện `imageAvatar`. Lớp này luôn đặt chữ cái đầu của tên, rồi nạp ảnh qua `ImageLoader` đè lên chữ; chưa có ảnh, đang giải mã hoặc ảnh lỗi thì chữ cái vẫn hiện, không bao giờ còn vòng tròn trống.

Kiểu của các thành phần trên (issue #351):

- **Thanh trên của Trang chủ** (`layoutTopBar` trong `activity_main.xml`): nền `bg_top_bar` chuyển từ `primary_dark` sang `colorPrimary`, tên app và chuông màu `colorOnPrimary`, ảnh đại diện có vòng trắng `bg_avatar_ring` dày `badge_stroke`.
- **Huy hiệu đếm số** (`bg_badge_count`): nền `colorTertiary` (màu nhấn), viền trắng `badge_stroke`, chữ `colorOnTertiary` cỡ `LabelSmall`.
- **Thanh chi tiết** (`view_detail_toolbar`): nền trắng có đường kẻ `colorOutline` ở đáy (`bg_toolbar_divider`), tiêu đề `TitleLarge`. Màn admin dùng cùng thanh này.
- **Ô trống và ô lỗi**: biểu tượng trong vòng tròn `state_icon_circle` (`bg_icon_circle`), ô trống nền `colorPrimaryContainer` với biểu tượng `colorPrimary`, ô lỗi nền `colorErrorContainer` với biểu tượng `colorError`; tiêu đề `TitleLarge`, lời giải thích `BodyMedium` màu `colorOnSurfaceVariant`; nút viên thuốc.
- **Nhãn trạng thái** (`view_status_chip`): viên thuốc, chữ `LabelMedium`; màu vẫn do `StatusChips` đặt theo mục 4.
- **Dải ngoại tuyến**: chữ `LabelLarge` trắng trên `colorInfo` (6.54).
- **Hàng sao**: số điểm `LabelLarge` màu `colorOnSurface`, số lượt `BodySmall` màu `colorOnSurfaceVariant`.

`item_listing_card.xml` có chiều rộng `match_parent`, nên dùng được cả trong lưới hai cột (B01) lẫn danh sách một cột (B05, B06, D02). Chiều cao ảnh bìa lấy từ token `listing_card_image_height`; ảnh đại diện lớn lấy từ `avatar_size_lg`.

## 5a. Style thành phần

Mọi style thành phần nằm trong `res/values/styles.xml`, và `Theme.PUBGApp` đặt chúng làm mặc định. Một view không ghi `style` thì đã có style mặc định; chỉ ghi `style` để chọn biến thể, và **luôn chọn `Widget.PUBGApp.*`, không trỏ thẳng vào `Widget.Material3.*`**, nếu không màu, font và hình khối của dự án không áp được.

| Thành phần | Style | Mặc định qua thuộc tính theme | Trông thế nào |
| --- | --- | --- | --- |
| Nút chính (tô xanh) | `Widget.PUBGApp.Button` | `materialButtonStyle` | Viên thuốc, nền `colorPrimary`. Mỗi màn hình một nút chính |
| Nút viền | `Widget.PUBGApp.Button.OutlinedButton` | `materialButtonOutlinedStyle` | Viên thuốc, viền và chữ xanh (`res/color/button_outlined_stroke.xml`). Nút nguy hiểm đặt thêm `app:strokeColor="?attr/colorError"` |
| Nút chữ | `Widget.PUBGApp.Button.TextButton` | `borderlessButtonStyle` | Chữ xanh, không nền |
| Nút tonal | `Widget.PUBGApp.Button.TonalButton` | `materialButtonTonalStyle` | Viên thuốc, nền `colorSecondaryContainer` |
| Thẻ | `Widget.PUBGApp.CardView` | `materialCardViewStyle` | Nền trắng, bo 16dp, bóng `elevation_card`, không viền |
| Chip lọc | `Widget.PUBGApp.Chip.Filter` | (không có, phải ghi `style`) | Viên thuốc; khi chọn: nền `colorPrimaryContainer`, viền và chữ xanh đậm |
| Chip thường | `Widget.PUBGApp.Chip.Assist` | `chipStyle` | Viên thuốc, viền `colorOutline` |
| Ô nhập | `Widget.PUBGApp.TextInputLayout.OutlinedBox` | `textInputStyle`, `textInputOutlinedStyle` | Nền trắng, bo 12dp, viền `outline_strong`, xanh khi đang gõ |
| Ô chọn thả xuống | `Widget.PUBGApp.TextInputLayout.OutlinedBox.ExposedDropdownMenu` | `textInputOutlinedExposedDropdownMenuStyle` | Như ô nhập |
| Thanh điều hướng dưới | `Widget.PUBGApp.BottomNavigationView` | `bottomNavigationStyle` | Nền trắng; tab đang chọn có nền viên thuốc `colorPrimaryContainer`, biểu tượng và chữ xanh |
| Tab | `Widget.PUBGApp.TabLayout` | `tabStyle` | Chữ `LabelLarge`, tab chọn và gạch chỉ báo màu xanh |
| Toolbar | `Widget.PUBGApp.Toolbar` | `toolbarStyle` | Tiêu đề `TitleLarge` |
| Snackbar | `Widget.PUBGApp.Snackbar` | `snackbarStyle` | Bo 12dp, nền `colorSurfaceInverse` |
| Hộp thoại | `MaterialAlertDialog.PUBGApp` | `materialAlertDialogTheme` | Nền trắng, bo 24dp |
| Bảng trượt từ dưới lên | `Widget.PUBGApp.BottomSheet` | `bottomSheetDialogTheme` | Nền trắng, bo 24dp hai góc trên |

Câu báo lỗi nằm trong màn hình (ví dụ `textError` ở A01 đến A03, issue #360) là khối `bg_error_banner`: nền `colorErrorContainer`, bo `radius_md`, đệm `space_md`, chữ `BodyMedium` màu `colorOnErrorContainer` (10.23). Tiêu đề lớn của các màn đăng nhập, đăng ký, quên mật khẩu dùng `HeadlineMedium` màu `colorPrimary`.

Ô nhập trần (không có `TextInputLayout`), ví dụ ô nhập tin nhắn ở C02, dùng nền viên thuốc `bg_input_pill` (`colorSurfaceContainer`, bo `radius_xl`) với đệm `space_md` ngang, `space_sm` dọc. Bong bóng tin nhắn nhận là thẻ trắng có bóng, không viền; bong bóng gửi tô `colorPrimary` (issue #355).

Theme tắt `elevationOverlayEnabled`: Material 3 mặc định phủ một lớp `colorPrimary` mờ lên mọi bề mặt có bóng, làm thẻ và thanh dưới ngả xanh xám (thấy trên emulator). Bề mặt của PUBGApp luôn là `colorSurface` trắng; độ nổi chỉ thể hiện bằng bóng.

Trang Cá nhân (D01, issue #359): đầu trang là khối `bg_profile_header` (cùng dải xanh với thanh trên của Trang chủ, bo `radius_lg`, đệm `space_md`) với tên, khu vực và nút "Sửa" màu `colorOnPrimary`. Mỗi dòng menu có biểu tượng bên trái qua `drawableStart`, tô `colorPrimary`, cách chữ `space_md`; các dòng cách nhau bằng `MaterialDivider`. Màn hình nào có danh sách dòng menu thì theo cùng kiểu này. Số thống kê trên trang hồ sơ (D01, D02, D03: đã bán, tỉ lệ hoàn tất, giao dịch, người theo dõi, điểm) dùng `TitleLarge` màu `colorPrimary`, nhãn dưới số `BodySmall` màu `colorOnSurfaceVariant`. Ảnh bìa của D02 dùng cùng dải `bg_top_bar`.

Thanh nút cố định ở đáy màn hình (ví dụ Chi tiết tin, ô nhập tin nhắn) dùng khoảng đệm đều hai đầu. Việc chừa chỗ cho thanh cử chỉ của hệ thống do Activity lo bằng window insets: ở các tab, `BottomNavigationView` tự đệm; ở màn không phải tab, root của `MainActivity` đệm đáy bằng chiều cao thanh hệ thống (issue #366). Layout không tự cộng thêm khoảng đệm cho việc này.

## 6. Bốn trạng thái mà màn hình nào cũng phải có

Đang tải, có dữ liệu, rỗng, lỗi. Màn hình chỉ làm đường hạnh phúc là chưa xong. Chi tiết trong skill `pubg-ui` mục 2.

Ô trống không được là màn hình trắng: phải có biểu tượng, một câu nói thiếu gì, và nút đưa người dùng đi tiếp nếu có.

## 7. Ảnh

Gói Firebase miễn phí không có Cloud Storage, nên ảnh là chuỗi Base64 nằm trong tài liệu Firestore (luật R21).

| Loại | Cạnh dài | Dung lượng tối đa |
| --- | --- | --- |
| Ảnh đầy đủ | 1024 px | 300 KB trước khi mã hóa |
| Ảnh thu nhỏ của tin đăng | 320 px | 20 KB |
| Ảnh đại diện | 256 px | 20 KB |

Tối đa 5 ảnh một tin. Luôn có ảnh giữ chỗ lúc đang giải mã và ảnh dự phòng khi hỏng. Giải mã ngoài luồng chính để danh sách không giật. Không có video ở bất kỳ màn hình nào.

## 8. Tiếng Việt

Mọi chữ hiển thị là string resource, tiếng Việt có dấu, không emoji. Chữ trên nút là động từ ("Đăng tin", "Gửi đề nghị", "Thử lại"), không phải danh từ. Thông báo lỗi nói được hai điều: chuyện gì xảy ra và làm gì tiếp.

## 9. Còn thiếu

- Bố cục xoay ngang cho các màn chính, để lại giai đoạn code.
- Biểu tượng: chốt dùng bộ Material Symbols, chưa liệt kê từng cái.
