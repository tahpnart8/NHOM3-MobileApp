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
| `colorOutline` | `?attr/colorOutline` | Viền ô nhập, đường kẻ phân cách | `#E7E2D6` |
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

## 2. Khoảng cách, bo góc, độ nổi

Thang khoảng cách là bội của 4. Không dùng giá trị ngoài thang.

| Token | Giá trị | Dùng cho |
| --- | --- | --- |
| `space_xs` | 4dp | Khe giữa biểu tượng và chữ |
| `space_sm` | 8dp | Khe giữa hai dòng trong một thẻ |
| `space_md` | 16dp | Lề trái phải của màn hình, lề trong thẻ |
| `space_lg` | 24dp | Khe giữa hai nhóm nội dung |
| `space_xl` | 32dp | Khe trên dưới của một mục lớn |
| `radius_sm` | 8dp | Chip, ô nhập |
| `radius_md` | 12dp | Thẻ, nút |
| `radius_lg` | 16dp | Hộp thoại, bảng trượt từ dưới lên |
| `elevation_card` | 2dp | Thẻ |
| `touch_min` | 48dp | Cạnh nhỏ nhất của mọi thứ bấm được |

## 3. Thang chữ

Dùng text appearance của theme, không đặt `textSize` tay.

| Token | Cỡ gợi ý | Dùng cho |
| --- | --- | --- |
| `textAppearanceHeadlineSmall` | 24sp | Tiêu đề màn hình trống, tên người dùng trên trang cá nhân |
| `textAppearanceTitleMedium` | 16sp đậm | Tiêu đề thẻ, tên món đồ |
| `textAppearanceBodyMedium` | 14sp | Chữ thường |
| `textAppearanceBodySmall` | 12sp | Chữ phụ, thời gian, khu vực |
| `textAppearanceLabelLarge` | 14sp đậm | Chữ trên nút |

Giá cả luôn dùng `textAppearanceTitleMedium` kèm `colorPrimary`, để mắt bắt được giá ở mọi màn hình.

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
| Nạp ảnh Base64 vào `ImageView` (lớp Java) | `ui/common/ImageLoader.java` | Mọi màn hình có ảnh: thẻ tin, chat, hồ sơ, bằng chứng |
| Tô chữ và màu cho nhãn trạng thái (lớp Java) | `ui/common/StatusChips.java` | Mọi màn hình có `view_status_chip` |

**Hai lớp Java ở cuối bảng là bắt buộc dùng, không ai tự viết lại.** Giải mã ảnh Base64 trên luồng chính đã xảy ra ba lần (các Pull Request #129, #133 và #135, xem issue #140): giao diện giật, ảnh 1024 px có thể làm tràn bộ nhớ, và `ViewHolder` bị dùng lại thì ảnh gán nhầm sang dòng khác. `ImageLoader.load` giải mã ngoài luồng chính, thu nhỏ khi giải mã, có ảnh giữ chỗ và ảnh dự phòng, và chỉ gán ảnh khi `ImageView` vẫn còn cần đúng ảnh đó. `StatusChips.bind` đặt chữ và màu cho `view_status_chip` đúng bảng ở mục 4, với một phương thức nạp chồng cho mỗi enum trạng thái; chữ lấy từ khóa `common_status_<đối tượng>_<trạng thái>` trong `strings_common.xml`. Màn hình nào cần giải mã ảnh hoặc tô nhãn thì gọi hai lớp này; cần thêm hành vi thì mở issue `area:ui`, đừng sao chép sang module mình.

`item_listing_card.xml` có chiều rộng `match_parent`, nên dùng được cả trong lưới hai cột (B01) lẫn danh sách một cột (B05, B06, D02). Chiều cao ảnh bìa lấy từ token `listing_card_image_height`; ảnh đại diện lớn lấy từ `avatar_size_lg`.

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
