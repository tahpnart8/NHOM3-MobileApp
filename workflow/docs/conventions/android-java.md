# Quy ước viết code

Áp dụng cho mọi người và mọi AI. Bản rút gọn cho AI nằm ở `.agents/rules/`.

## Ngôn ngữ và công cụ

- Java 17. **Không dùng Kotlin, không dùng Jetpack Compose.**
- Giao diện XML với **ViewBinding**; không dùng `findViewById`.
- Phiên bản thư viện chỉ ghi ở `PUBGApp/gradle/libs.versions.toml`. Không tự thêm thư viện; cần thì mở issue `chore(deps)`.

## Đặt tên

| Thứ | Quy ước | Ví dụ |
| --- | --- | --- |
| Gói dữ liệu và logic | Chia theo tầng: `com.nhom3.pubgapp.{model,model.enums,data,data.local,util,viewmodel}` | `...model.Listing`, `...viewmodel.ShopViewModel` |
| Gói giao diện | Chia theo module: `com.nhom3.pubgapp.ui.<account\|notification\|listing\|trade\|profile\|trust\|admin\|common\|main>`; màn quản trị khiếu nại ở `ui.admin.complaint`. Mỗi module một gói, không đặt file vào gói của module khác ([so-huu.md](../../plan/so-huu.md)) | `...ui.listing.ListingDetailFragment` |
| Lớp | `PascalCase` | `ListingRepository` |
| Hàm, biến | `camelCase` | `loadListings()` |
| Hằng | `UPPER_SNAKE_CASE` | `MAX_PHOTOS` |
| Layout | Màn hình là Fragment: `fragment_<tiền-tố><màn>.xml`; mục trong danh sách: `item_<tiền-tố><mục>.xml`. Chỉ ba Activity có `activity_auth.xml`, `activity_main.xml`, `activity_admin.xml` | `fragment_listing_detail.xml`, `item_listing_card.xml` |
| Id view | `<loại><Việc>` kiểu lowerCamelCase, trùng luôn tên trường ViewBinding sinh ra | `buttonSignIn`, `textPrice`, `recyclerListings` |
| Chuỗi | `<module>_<màn>_<việc>` trong `strings_<module>.xml` | `listing_detail_price_label` trong `strings_listing.xml` |

Định danh và chú thích trong code viết tiếng Anh, chú thích ngắn (xem "Phong cách viết code" bên dưới).

## Phong cách viết code

Code phải đọc được ngay từ lần đầu bởi một sinh viên đã học xong các bài giảng. **Rõ ràng và ngắn gọn**, không "thông minh". Áp dụng cho dòng bạn viết hoặc sửa; không viết lại code cũ chỉ vì phong cách. Chi tiết cho AI ở skill `pubg-code`; kiểm tra tự động bằng `sh workflow/scripts/check-java-style.sh`.

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

**Kiến thức nằm ở `workflow/memory/code/`, không nằm trong code.** Khi bạn (hoặc AI) học được điều mà code không tự nói được, như một cái bẫy của Firebase, lý do một câu truy vấn có điều kiện lạ, hay một cách làm đã thử và thất bại, ghi vào `workflow/memory/code/<khu-vực>.md` và để lại trong code tối đa một dòng chú thích. Cách viết một mục nằm ở `workflow/memory/code/README.md`. Việc chọn giữa các phương án thật sự thì ghi ở `workflow/memory/decisions.md`.

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
- Mỗi module có file `strings_<module>.xml` riêng (`strings_account.xml`, `strings_listing.xml`, `strings_trade.xml`, `strings_profile.xml`, `strings_admin.xml`, `strings_common.xml`) để hai người không cùng sửa `strings.xml`.
- Mỗi màn hình phải dùng được khi xoay: dùng `ConstraintLayout`, hoặc thêm `layout-land`, hoặc `sw600dp`. Trạng thái để trong `ViewModel`, không để trong Activity hay Fragment.
- Màu và kiểu chữ lấy từ `themes.xml` và `colors.xml`. Chỉ sửa file dùng chung này qua issue `area:ui`.
- `AndroidManifest.xml`: chỉ thêm dòng cần thiết, không sắp xếp lại.

## Điều hướng

- Ba Activity: `AuthActivity` (đăng nhập, đăng ký, quên mật khẩu), `MainActivity` (thanh điều hướng dưới gồm 5 tab Trang chủ, Tìm kiếm, Đăng tin, Tin nhắn, Cá nhân) và `AdminActivity` (mở từ màn Cá nhân khi người dùng là admin). Mọi màn hình khác là Fragment, dùng Navigation component.
- Mỗi module có đồ thị điều hướng riêng (`nav_account.xml`, `nav_listing.xml`, `nav_trade.xml`, `nav_profile.xml`, `nav_admin.xml`...). Đi từ module này sang module khác chỉ qua **hành động toàn cục** trong `nav_main.xml`; không sửa đồ thị của người khác.
- Hai nơi đăng ký dùng chung, chỉ leader sửa: `nav_main.xml` (hành động toàn cục) và file menu của thanh điều hướng dưới. Cần thêm một hành động toàn cục hoặc một tab thì mở issue cho leader.
- Ứng dụng có một nút nạp dữ liệu mẫu, **chỉ có ở bản debug, không có trong bản release** (cách làm cụ thể, ví dụ thư mục `src/debug/` hoặc điều kiện `BuildConfig.DEBUG`, do issue của nút này chốt). Dữ liệu mẫu dùng mã có tiền tố `demo_` và phải thỏa [goi-y.md](../architecture/goi-y.md).

## Ánh xạ Firestore sang Java

Bốn luật cho mọi lớp trong `model`, để Firestore đọc và ghi đúng:

1. `BaseDocument.id` gắn `@DocumentId`; mã tài liệu không được lưu thành trường.
2. Mọi phương thức tính toán dạng `isX()` hoặc `getX()` không tương ứng một trường gắn `@Exclude`, nếu không Firestore sẽ ghi nó như một trường.
3. Trường boolean có tên bắt đầu bằng `is` (ví dụ `isDefault`, `isActive`, `isRead`) gắn `@PropertyName` với tên trường đã ghi trong [firestore-schema.md](../data/firestore-schema.md).
4. `createdAt` và `updatedAt` ghi bằng `FieldValue.serverTimestamp()`, không dùng giờ máy.

Chi tiết và lý do ghi ở `workflow/memory/code/firebase.md`.

## Luồng và lỗi

- Không làm việc mạng, đĩa hay cơ sở dữ liệu trên luồng chính.
- Khi thiếu quyền hoặc mất mạng, hiển thị thông báo rõ ràng cho người dùng. Không nuốt lỗi âm thầm.

## Điểm dễ xung đột

| File | Luật |
| --- | --- |
| `AndroidManifest.xml` | Chỉ thêm dòng |
| `libs.versions.toml`, `app/build.gradle.kts` | Chỉ qua issue `chore(deps)`, Pull Request nhỏ, merge nhanh |
| `strings.xml`, `colors.xml`, `themes.xml` | Xem trên |
| `nav_main.xml`, file menu thanh điều hướng dưới | Chỉ leader sửa; xem mục "Điều hướng" |
| `strings_<module>.xml`, đồ thị `nav_<module>.xml` | Chỉ chủ module sửa |
