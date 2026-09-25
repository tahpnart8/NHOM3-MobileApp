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

Định danh và chú thích trong code viết tiếng Anh, chú thích ngắn (xem "Phong cách viết code" bên dưới).

## Phong cách viết code

Code phải đọc được ngay từ lần đầu bởi một sinh viên đã học xong các bài giảng. **Rõ ràng và ngắn gọn**, không "thông minh". Áp dụng cho dòng bạn viết hoặc sửa; không viết lại code cũ chỉ vì phong cách. Chi tiết cho AI ở skill `pubg-code`; kiểm tra tự động bằng `sh scripts/check-java-style.sh`.

| Nên | Không nên |
| --- | --- |
| Ghi rõ kiểu: `String email = ...` | `var email = ...` |
| Listener là lớp ẩn danh `new View.OnClickListener() { ... }` hoặc gọi một hàm có tên | Lambda `v -> ...`, dấu `->` trong `switch`, tham chiếu hàm `Foo::bar` |
| Vòng lặp `for (Listing listing : listings)` | `stream()`, `map()`, `filter()`, `Collectors` |
| Một câu lệnh mỗi dòng, đặt tên cho giá trị trung gian | Chuỗi gọi hàm dài |
| Hàm khoảng 30 dòng trở xuống, lồng nhau tối đa 2 cấp, `return` sớm | Hàm dài, `if` lồng `if` |
| Lớp làm một việc, khoảng 300 dòng trở xuống | Lớp "làm mọi thứ" |
| Hằng số có tên: `MAX_PHOTOS`, tên trường Firestore định nghĩa một lần | Số và chuỗi "ma thuật" rải rác |
| Bắt lỗi rồi báo cho người dùng | `catch` rỗng, `printStackTrace()`, `System.out` |

**Chú thích tối đa 1 đến 2 dòng**, chỉ nói **vì sao** (một cách xử lý vòng, một cái bẫy của thư viện), không nhắc lại điều code đã nói. Không chú thích cho từng hàm, không viết khối chú thích dài, không để code bị comment, không để `TODO` trong code (mở issue), không ghi tác giả hay ngày.

**Kiến thức nằm ở `memory/code/`, không nằm trong code.** Khi bạn (hoặc AI) học được điều mà code không tự nói được, như một cái bẫy của Firebase, lý do một câu truy vấn có điều kiện lạ, hay một cách làm đã thử và thất bại, ghi vào `memory/code/<khu-vực>.md` và để lại trong code tối đa một dòng chú thích. Cách viết một mục nằm ở `memory/code/README.md`. Việc chọn giữa các phương án thật sự thì ghi ở `memory/decisions.md`.

Ví dụ:

```java
// Không nên
saveButton.setOnClickListener(v -> repository.getListings().stream()
        .filter(l -> l.getPrice() > 0).map(Listing::getId).forEach(this::save));

// Nên
saveButton.setOnClickListener(new View.OnClickListener() {
    @Override
    public void onClick(View view) {
        saveListingsWithPrice();
    }
});
```

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
