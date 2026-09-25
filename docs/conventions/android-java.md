# Quy ước viết code

Áp dụng cho mọi người và mọi AI. Bản rút gọn cho AI nằm ở `.agents/rules/`.

## Ngôn ngữ và công cụ

- Java 17. **Không dùng Kotlin, không dùng Jetpack Compose.**
- Giao diện XML với **ViewBinding**; không dùng `findViewById`.
- Phiên bản thư viện chỉ ghi ở `PUBGApp/gradle/libs.versions.toml`. Không tự thêm thư viện; cần thì mở issue `chore(deps)`.

## Đặt tên

| Thứ | Quy ước | Ví dụ |
| --- | --- | --- |
| Gói | `com.nhom3.pubgapp.feature.<tính-năng>.{ui,data,model}` | `...feature.listing.ui` |
| Lớp | `PascalCase` | `ListingRepository` |
| Hàm, biến | `camelCase` | `loadListings()` |
| Hằng | `UPPER_SNAKE_CASE` | `MAX_PHOTOS` |
| Layout | `<loại>_<tính-năng>_<tên>.xml` | `activity_auth_login.xml`, `item_listing_card.xml` |
| Id view | `<viết-tắt>_<tên>` chữ thường có gạch dưới | `btn_sign_in`, `tv_price`, `rv_listings` |
| Chuỗi | `<tính-năng>_<tên>` trong `strings_<tính-năng>.xml` | `auth_sign_in` |

Định danh và chú thích trong code viết tiếng Anh.

## Tài nguyên và xoay màn hình

- Không ghi chữ cứng trong layout hoặc code. Mọi chữ hiển thị là string resource; ngôn ngữ mặc định là tiếng Việt.
- Mỗi tính năng có file `strings_<tính-năng>.xml` riêng để hai người không cùng sửa `strings.xml`.
- Mỗi màn hình phải dùng được khi xoay: dùng `ConstraintLayout`, hoặc thêm `layout-land`, hoặc `sw600dp`. Trạng thái để trong `ViewModel`, không để trong Activity.
- Màu và kiểu chữ lấy từ `themes.xml` và `colors.xml`. Chỉ sửa file dùng chung này qua issue `area:ui`.
- `AndroidManifest.xml`: chỉ thêm dòng cần thiết, không sắp xếp lại.

## Luồng và lỗi

- Không làm việc mạng, đĩa hay cơ sở dữ liệu trên luồng chính.
- Khi thiếu quyền hoặc mất mạng, hiển thị thông báo rõ ràng cho người dùng. Không nuốt lỗi âm thầm.

## Điểm dễ xung đột

| File | Luật |
| --- | --- |
| `AndroidManifest.xml` | Chỉ thêm dòng |
| `libs.versions.toml`, `app/build.gradle.kts` | Chỉ qua issue `chore(deps)`, Pull Request nhỏ, merge nhanh |
| `strings.xml`, `colors.xml`, `themes.xml` | Xem trên |
| `MainActivity`, điều hướng | Gắn tính năng qua một điểm đăng ký duy nhất (sẽ chốt trong `architecture/overview.md`) |
