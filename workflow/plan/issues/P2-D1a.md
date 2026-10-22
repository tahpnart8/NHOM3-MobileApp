TITLE: feat(profile): hồ sơ cá nhân, lối tắt và đăng xuất
LABELS: type:feature, area:profile, module:d, priority:p1
ASSIGNEES: PmSubin
PHASE: P2

Plan item: P2-D1a

### Bối cảnh
- PUBGApp là ứng dụng Android mua bán đồ cũ kết hợp mạng xã hội, viết bằng Java 17, XML Views với ViewBinding, Firebase Authentication và Cloud Firestore gói Spark (không Cloud Storage, không Kotlin).
- Module D "Hồ sơ và tin cậy" do Phúc (`PmSubin`) làm: hồ sơ cá nhân và công khai, uy tín, theo dõi, gửi yêu cầu xác thực tài khoản, báo cáo, khiếu nại, và phía admin xử lý khiếu nại.
- Issue này làm màn D01 `MyProfileFragment` (tab Cá nhân): số liệu, lối tắt sang màn khác, nút "Khu vực quản trị", nút "Đăng xuất" và khối hiển thị trạng thái xác thực tài khoản. Biểu mẫu gửi yêu cầu xác thực là issue P2-D1b.
- Đọc trước ba tài liệu: `workflow/docs/ui/d-ho-so-tin-cay/README.md` mục D01; `workflow/docs/product/use-case/uc-d-ho-so-tin-cay.md` mục UC30; `workflow/docs/architecture/dac-ta-lop/viewmodel.md` mục ProfileViewModel.
- Đã có trên `main` sau P1: khung `MyProfileFragment` trống, `nav_main.xml` với các hành động toàn cục (kể cả `action_global_sign_in`, `action_global_tab`, `action_global_my_profile`), mọi repository đủ chữ ký (thân chưa làm trả `DataCallback.NOT_READY`), mọi ViewModel có sẵn `loading`, `error`, `done` cùng getter công khai cho mọi LiveData, `MediaRepository` làm thật, thành phần giao diện dùng chung (`view_avatar.xml`, `view_rating.xml`, `view_status_chip.xml`, `view_empty_state.xml`, `view_error_state.xml`, `view_offline_banner.xml`) và token màu trong `colors.xml`.

### Mã tính năng
F-11 (Hồ sơ công khai, uy tín, theo dõi). Nút "Đăng xuất" thuộc F-01 (STT 104), nút "Khu vực quản trị" là lối vào của STT 106 (F-14); ở đây chỉ làm hai nút trên D01, hàm của chúng là của module A và module E.

### Mã việc kế hoạch
P2-D1, tách thành P2-D1a (màn D01 và các nút trên đó) và P2-D1b (biểu mẫu gửi yêu cầu xác thực). Lý do tách: biểu mẫu có ô nhập số CCCD, hai ảnh, hai tài liệu Firestore và luật gửi lại tối đa 3 lần, đủ một ngày riêng; ghép với phần còn lại của D01 thì vượt một ngày.

### Module
D. Hồ sơ và tin cậy (Phúc)

### STT trong bảng đặc tả
8 (số liệu hồ sơ), 7 (điểm đánh giá hiển thị trên D01), 73 (chỉ phần hiển thị trạng thái xác thực, phía người dùng), 104 (chỉ nút "Đăng xuất"), 106 (chỉ nút "Khu vực quản trị"). Use case: UC30, UC04, UC43.

### Màn hình mockup
D01 (`MyProfileFragment`, tab Cá nhân, hiện thanh dưới). Khối xác thực tài khoản và nút "Khu vực quản trị" chưa có ảnh; làm theo mục "Thành phần chính", "Trạng thái đặc biệt" và "Khác với ảnh" của D01 trong `workflow/docs/ui/d-ho-so-tin-cay/README.md`.

### Làm gì và vì sao
Sau issue này tab Cá nhân hiện hồ sơ thật của người đang đăng nhập: ảnh đại diện (chữ cái đầu khi chưa có ảnh), tên, khu vực, ba số liệu (số món đã bán, điểm đánh giá trung bình, số người theo dõi) và danh sách lối vào tới các màn của module khác (sản phẩm của tôi, tin đã lưu, lịch sử giao dịch, hồ sơ uy tín, báo cáo và khiếu nại của tôi, cài đặt tài khoản). Khối xác thực tài khoản cho người dùng biết đúng trạng thái: chưa gửi, đang chờ duyệt, bị từ chối, đã duyệt (huy hiệu "Tài khoản đã xác thực"), và nói rõ rằng chưa được duyệt thì chưa đăng tin, gửi đề nghị, tạo giao dịch được. Tài khoản admin thấy thêm nút "Khu vực quản trị" mở `AdminActivity`. Nút "Đăng xuất" hỏi xác nhận rồi đưa người dùng về trang chủ B01 ở chế độ khách, không về màn Đăng nhập. Số liệu hồ sơ không lưu sẵn mà đếm lúc mở màn (`ProfileStats`, `trang-thai.md` phần quy ước chung). `ProfileViewModel.loadProfile` viết ở đây được D02 và D03 dùng lại. Issue này phục vụ các dòng barem R-Chapter 4 (điều hướng bằng Navigation component và hành động toàn cục), R-Chapter 1, 2, 6, 7 (Intent tường minh mở `AdminActivity`, kiểm tra kết nối mạng bằng `NetworkMonitor`, async qua `DataCallback`) và C-GUI (token màu, bốn trạng thái của màn hình).

### Tiêu chí nghiệm thu
- [ ] Mở tab Cá nhân với Firebase thật: hiện `displayName`, `province`, `district` của `users/{uid}`; ba số liệu lấy từ `ProfileStats.soldCount`, `User.ratingAverage()` và `ProfileStats.followerCount` (đếm bằng `count()` trên `deals` và `follows`, không đọc trường lưu sẵn). Chưa có đánh giá (`ratingCount` bằng 0) thì ô điểm hiện dấu gạch (ký tự `-`); chưa bán món nào thì số liệu hiện 0 và dòng "Sản phẩm của tôi" gợi ý đăng tin đầu tiên.
- [ ] Ảnh đại diện lấy từ `User.avatarBase64` (giải mã ngoài luồng chính); chuỗi rỗng hoặc lỗi giải mã thì hiện chữ cái đầu của `displayName` bằng `view_avatar.xml`.
- [ ] Khối xác thực tài khoản đọc `User.verificationStatus` và hiện đúng bốn trạng thái: NONE (chữ báo chưa xác thực, nói rõ chưa đăng tin, gửi đề nghị, tạo giao dịch được; vẫn xem, tìm kiếm, nhắn tin, theo dõi bình thường; không có chip vì `giao-dien.md` mục 4 không có hàng cho NONE), PENDING (chip "Đang chờ duyệt", `colorWarning`), REJECTED (chip "Bị từ chối", `colorError`), APPROVED (huy hiệu "Tài khoản đã xác thực", chip "Đã xác thực", `colorSuccess`). Chip vẽ bằng `view_status_chip.xml`, chữ và màu lấy đúng `giao-dien.md` mục 4. Không có chữ "xác minh người bán" ở bất kỳ đâu. Nút gửi yêu cầu, biểu mẫu, lý do từ chối và số lần gửi lại còn lại là việc của P2-D1b, không làm ở đây.
- [ ] Các dòng điều hướng đúng đích: Sản phẩm của tôi (`action_global_my_shop`), Tin đã lưu (`action_global_saved_listings`), Lịch sử giao dịch (`action_global_deal_history`), Báo cáo và khiếu nại của tôi (`action_global_my_reports`), Cài đặt tài khoản và nút cài đặt ở góc phải (`action_global_personal_info`, đích A04), Hồ sơ uy tín (`action_global_seller_trust` với `userId` là chính mình, D03 chế độ chỉ xem), bấm ảnh đại diện hoặc tên (`action_global_user_profile` với `userId` là chính mình, D02; xem Câu hỏi 5).
- [ ] Mở D01 bằng `action_global_my_profile` (nút "Đăng ký xác thực" ở C02, C03) hay bằng tab Cá nhân đều ra cùng một màn với cùng khối xác thực; D01 không cần xử lý riêng cho hai lối vào.
- [ ] Nút "Khu vực quản trị" chỉ hiện khi `SessionManager.getInstance().isAdmin()` là true; bấm mở `AdminActivity` (E01). Tài khoản thường không thấy nút.
- [ ] "Đăng xuất" hiện hộp thoại hỏi xác nhận; "Hủy" thì không làm gì; đồng ý thì gọi `AuthViewModel.logout()`; khi `AuthViewModel` báo `done` (xem Câu hỏi 2) thì Fragment đi `action_global_tab` về tab Trang chủ (`tabId`: xem Câu hỏi 3). Kết quả: ứng dụng ở trang chủ B01 chế độ khách (thanh trên có nút "Đăng nhập", không có chuông và ảnh đại diện), không hiện A01, và nút Back không đưa về D01. Phiên `SessionManager` và Room đã bị xóa (do `AuthViewModel.logout` làm).
- [ ] Khách chưa đăng nhập không ở lại D01: `SessionManager.getInstance().isLoggedIn()` là false thì Fragment đi `action_global_sign_in` (đích A01), không hiện dữ liệu.
- [ ] Mỗi đường lỗi hiện câu tiếng Việt lấy từ `error` của `ProfileViewModel`, không im lặng: đọc hồ sơ lỗi, đếm số liệu lỗi (có nút "Thử lại" bằng `view_error_state.xml`); phương thức của module khác còn `NOT_READY` thì hiện đúng câu lỗi đó, không giả dữ liệu.
- [ ] Màn có đủ bốn trạng thái: đang tải (theo `loading`, khi chờ `profile` và `stats`), có dữ liệu, rỗng (chưa bán món nào, chưa có đánh giá), lỗi.
- [ ] `profile` và `stats` nằm trong `ProfileViewModel`: xoay màn hình không đọc lại `users/{uid}` và không đếm lại số liệu.
- [ ] Offline: `view_offline_banner.xml` hiện khi `NetworkMonitor.getInstance().isOnline()` là false; mục "Đăng xuất" vẫn dùng được; thông tin đã tải giữ trên màn trong lúc ViewModel còn sống (Room không có bảng cho hồ sơ, xem Câu hỏi 4).
- [ ] Không có chuyển trạng thái nào được ghi trong issue này; không đoạn nào ghi thẳng `verificationStatus` hay `status` (R18).
- [ ] Không có phương thức quy tắc mới, nên không có unit test mới bắt buộc; PR ghi tên test đã có ở P1 cho `User.ratingAverage` và `ProfileStats.completionRate` (hai quy tắc màn này dùng) và ghi "không có quy tắc mới".
- [ ] Layout không viết thẳng màu, cỡ chữ, khoảng cách: chỉ dùng token của `giao-dien.md` mục 1 đến 3 (`colorPrimary`, `colorOnSurface`, `space_md`, `textAppearanceTitleMedium`...); id view theo `<loại><Việc>` kiểu lowerCamelCase (ví dụ `buttonLogout`, `textDisplayName`); mọi chữ hiển thị là string resource tiếng Việt có dấu trong `strings_profile.xml`, khóa theo `<module>_<màn>_<việc>` (ví dụ `profile_my_profile_logout`), không emoji.
- [ ] `cd PUBGApp && ./gradlew assembleDebug testDebugUnitTest lintDebug` và `sh workflow/scripts/check-java-style.sh` đều qua; PR ghi đúng lệnh và kết quả thật (R1).
- [ ] PR liệt kê mọi lớp, phương thức, trường công khai mới kèm dòng tài liệu định nghĩa (R16); nếu không có thì ghi "không có".

### Tài liệu phải đọc
- `AGENTS.md` mục 6: R12, R16, R17, R18, R20, R22, R23.
- `workflow/plan/so-huu.md` mục 3 (D01), mục 4 (hành động toàn cục, kể cả `action_global_sign_in`, `action_global_tab`, `action_global_my_profile`), mục 5 (dòng `UserRepository` và `ProfileViewModel` của Phúc), mục 6.
- `workflow/docs/product/barem-diem.md` (dòng barem mà issue này phục vụ).
- `workflow/docs/product/dac-ta-chuc-nang.md` các dòng STT 7, 8, 73, 104, 106.
- `workflow/docs/product/use-case/uc-d-ho-so-tin-cay.md` mục UC30; `uc-a-tai-khoan.md` mục UC04 (Đăng xuất).
- `workflow/docs/ui/README.md` mục "Điều hướng" và "Chế độ khách"; `workflow/docs/ui/d-ho-so-tin-cay/README.md` mục D01.
- `workflow/docs/conventions/giao-dien.md` mục 1 đến 6 (token màu, khoảng cách, chữ, màu theo trạng thái, thành phần dùng lại, bốn trạng thái).
- `workflow/docs/architecture/dac-ta-lop/viewmodel.md#profileviewmodel`, `#authviewmodel`
- `workflow/docs/architecture/dac-ta-lop/data.md#userrepository`
- `workflow/docs/architecture/dac-ta-lop/model.md#user`, `#profilestats`
- `workflow/docs/architecture/dac-ta-lop/enums.md#verificationstatus`
- `workflow/docs/architecture/dac-ta-lop/util.md#sessionmanager`, `#networkmonitor`
- `workflow/docs/architecture/trang-thai.md` phần quy ước chung (số liệu là `count()`) và mục 8 (trạng thái xác thực).
- `workflow/docs/data/firestore-schema.md#users`, `#follows`; `workflow/docs/data/security-rules.md` mục 1 (users), mục 5 (follows, vì `count()` đọc `follows`).
- `workflow/docs/architecture/overview.md` mục 8 (offline, lỗi, thời hạn chờ ghi 15 giây).
- `workflow/memory/code/README.md`, `workflow/memory/code/firebase.md`, `workflow/docs/conventions/android-java.md`, skill `pubg-code` và `pubg-ui` trước khi viết Java và layout.

### Lớp, trường, trạng thái sẽ dùng
Tên đúng như tài liệu; không đặt tên mới.

- `MyProfileFragment` (D01, `MainActivity`).
- `ProfileViewModel`: thuộc tính `profile` (`LiveData<User>`), `stats` (`LiveData<ProfileStats>`), `loading` (`LiveData<Boolean>`), `error` (`LiveData<String>`); mỗi LiveData có getter công khai (tên theo quy ước `get` cộng tên thuộc tính, ví dụ `getProfile()`, `getStats()`, `getLoading()`, `getError()`; chép đúng chữ trong `ProfileViewModel.java` do P1 tạo); phương thức được thay thân: `public void loadProfile(String uid)`.
- `UserRepository`: `public void getProfile(String uid, DataCallback<User> cb)` (của Ngân, chỉ gọi); `public void getStats(String uid, DataCallback<ProfileStats> cb)` (chưa có chủ, xem Câu hỏi 1).
- `User`: `displayName`, `avatarBase64`, `province`, `district`, `verificationStatus`, `ratingSum`, `ratingCount`; `public double ratingAverage()`.
- `ProfileStats`: `soldCount`, `followerCount`, `dealCount`, `completedDealCount`; `public double completionRate()`.
- `VerificationStatus`: NONE, PENDING, APPROVED, REJECTED.
- `SessionManager`: `public static SessionManager getInstance()`, `public String getCurrentUserId()`, `public boolean isAdmin()`, `public boolean isLoggedIn()`.
- `AuthViewModel`: `public void logout()` và `done` (`LiveData<Boolean>`) cùng getter (của Ngân, chỉ gọi và quan sát; thân có thể còn `NOT_READY`).
- `NetworkMonitor`: `public static NetworkMonitor getInstance()`, `public boolean isOnline()`; `DataCallback`: `onSuccess(T result)`, `onError(String message)`, hằng `NOT_READY`.
- Firestore: `users/{uid}`; đếm trên `deals` và `follows`.
- Hành động toàn cục (chỉ gọi): `action_global_my_shop`, `action_global_saved_listings`, `action_global_deal_history`, `action_global_my_reports`, `action_global_personal_info`, `action_global_user_profile` (tham số `userId`), `action_global_seller_trust` (tham số `userId`), `action_global_sign_in` (Khách bị đưa tới A01), `action_global_tab` (tham số `tabId`, về Trang chủ sau khi đăng xuất). `action_global_my_profile` là đích D01, chỉ nhận.

### File được tạo hoặc sửa
- [sửa] PUBGApp/app/src/main/java/com/nhom3/pubgapp/ui/profile/MyProfileFragment.java
- [sửa] PUBGApp/app/src/main/res/layout/fragment_profile_my_profile.xml
- [sửa] PUBGApp/app/src/main/res/values/strings_profile.xml
- [sửa] PUBGApp/app/src/main/java/com/nhom3/pubgapp/viewmodel/ProfileViewModel.java (chỉ thân `loadProfile`)
- [sửa] PUBGApp/app/src/main/java/com/nhom3/pubgapp/data/UserRepository.java (chỉ thân `getStats`, và chỉ khi Câu hỏi 1 được trả lời là giao cho Phúc)

Tên gói và tên layout theo khung P1-06; nếu khung đã dùng tên khác thì giữ tên đã có.

### Các bước đề nghị
Có thể làm theo thứ tự khác, nhưng không được đổi tên lớp, phương thức, trường nào.

1. Đọc `workflow/memory/code/README.md` và `workflow/memory/code/firebase.md`, rồi skill `pubg-code` và `pubg-ui`. Mở `MyProfileFragment.java`, `fragment_profile_my_profile.xml` và `strings_profile.xml` do P1 tạo để biết tên đã có, và mở `ProfileViewModel.java` để chép đúng tên getter của `profile`, `stats`, `loading`, `error`.
2. (Chỉ khi Câu hỏi 1 trả lời "giao cho Phúc".) Thay thân `UserRepository.getStats(String uid, DataCallback<ProfileStats> cb)`: đếm bằng truy vấn tổng hợp `count()` bốn con số của `ProfileStats` (`soldCount`: giao dịch COMPLETED mà `uid` là người bán; `followerCount`: số tài liệu `follows` có `followedId` bằng `uid`; `dealCount`: giao dịch tham gia, mọi trạng thái trừ CANCELLED; `completedDealCount`: giao dịch COMPLETED ở cả hai vai trò). Trả `ProfileStats` bằng `cb.onSuccess`, lỗi bằng `cb.onError`. Truy vấn nào Firebase báo thiếu chỉ mục thì ghi liên kết tạo chỉ mục vào mô tả PR.
3. Thay thân `ProfileViewModel.loadProfile(String uid)`: gọi `UserRepository.getProfile(uid, cb)` rồi `UserRepository.getStats(uid, cb)`; đưa kết quả vào `profile` và `stats`, bật `loading` trong lúc chờ, đưa câu lỗi của `onError` vào `error`. Không đọc lại nếu `profile` đã có dữ liệu cho cùng `uid`.
4. Viết `fragment_profile_my_profile.xml` bằng `ConstraintLayout` trong vùng cuộn: đầu trang (`view_avatar.xml`, tên, khu vực, nút cài đặt), hàng ba số liệu, danh sách dòng lối vào (biểu tượng, chữ, mũi tên), khối xác thực tài khoản, nút "Khu vực quản trị" (mặc định ẩn), mục "Đăng xuất" màu `colorError` tách riêng ở cuối, cùng các thành phần `view_empty_state`, `view_error_state`, `view_offline_banner` và một chỉ báo đang tải. Chỉ dùng token, id view dạng `<loại><Việc>`.
5. Trong `MyProfileFragment`, dùng ViewBinding: lấy `uid` bằng `SessionManager.getInstance().getCurrentUserId()`; nếu `SessionManager.getInstance().isLoggedIn()` là false thì đi `action_global_sign_in` và dừng. Lấy `ProfileViewModel` bằng `ViewModelProvider`; chỉ gọi `loadProfile(uid)` khi `profile` chưa có dữ liệu để xoay màn hình không tải lại.
6. Quan sát `profile`, `stats`, `loading`, `error` qua các getter: điền tên, khu vực ("quận, tỉnh"), ảnh đại diện, ba số liệu (`soldCount`, `ratingAverage()` kèm dấu gạch khi `ratingCount` bằng 0, `followerCount`); đổi giữa bốn trạng thái (đang tải, có dữ liệu, rỗng, lỗi).
7. Gắn listener cho các dòng lối vào bằng lớp ẩn danh `new View.OnClickListener()` (không lambda) và gọi đúng hành động toàn cục đã liệt kê ở mục tiêu chí; truyền `userId` là `SessionManager.getInstance().getCurrentUserId()` cho `action_global_user_profile` và `action_global_seller_trust`.
8. Khối xác thực tài khoản: đọc `verificationStatus` của `User` trong `profile` (getter theo quy ước của lớp mô hình) và hiện chữ, chip `view_status_chip.xml` đúng bốn trạng thái. Ở NONE và REJECTED chỉ hiện chữ giải thích (nút, biểu mẫu, lý do từ chối thêm ở P2-D1b).
9. Nút "Khu vực quản trị": hiện khi `SessionManager.getInstance().isAdmin()`; bấm thì mở `AdminActivity` bằng `Intent` tường minh (lớp do P1-06 tạo, dùng đúng gói khung đã đặt). Cờ này chỉ để hiện giao diện, quyền thật do Security Rules.
10. "Đăng xuất": hộp thoại xác nhận; đồng ý thì gọi `AuthViewModel.logout()`; quan sát `done` của `AuthViewModel` rồi đi `action_global_tab` về Trang chủ (dùng đúng kiểu và giá trị `tabId` khai trong `nav_main.xml` do P1-06 tạo, xem Câu hỏi 3). Không đưa tới A01. Chọn `done` làm tín hiệu "đã đăng xuất xong" là chi tiết cài đặt: ghi một dòng bằng skill `pubg-record-decision`.
11. Thêm toàn bộ chuỗi vào `strings_profile.xml`, khóa theo `<module>_<màn>_<việc>` (ví dụ `profile_my_profile_logout`); kiểm bằng skill `pubg-ui` rằng không còn giá trị màu, cỡ chữ, khoảng cách viết thẳng.
12. Chạy các lệnh ở mục "Tự kiểm trước khi mở Pull Request" rồi mở PR.

### File dùng chung cần thêm dòng
Không có. Chỉ mục ghép Firestore nào mới cần (nếu Firebase báo thiếu) thì ghi vào mô tả Pull Request để Phát gộp vào `firebase/firestore.indexes.json`.

### Cấm đụng
- Leader-only: `AndroidManifest.xml`, `nav_main.xml`, `nav_admin.xml`, `themes.xml`, `colors.xml`, `dimens.xml`, `strings_common.xml`, `AppDatabase`, `Converters`, mọi file Gradle, `firebase/`.
- Module A: thân của `UserRepository.getProfile`, `AuthViewModel.logout`, `AuthRepository.signOut`, `SessionManager.clear`; màn A04, A05, A06 và `AuthActivity` (chỉ gọi, có thể còn `NOT_READY`).
- Module B: `MyShopFragment`, `SavedListingsFragment`. Module C: `DealHistoryFragment`. Module E: `AdminActivity`, `MainActivity`, `AdminDashboardFragment`, thanh điều hướng dưới.
- `UserRepository.follow`, `unfollow`, `getFollowingIds` là của P2-D2; `UserRepository.requestVerification`, `UserRepository.getVerification` và `ProfileViewModel.requestVerification`, `loadVerification` là của P2-D1b.
- Mọi lớp trong `model`, `model.enums` (không ai sửa nếu không có issue riêng).

### Phụ thuộc
P1-04, P1-06 (khung giao diện, hành động toàn cục, thành phần chung). Phương thức của module khác cần có trên `main`: `UserRepository.getProfile` (P2-A3), `AuthViewModel.logout` (module A, STT 104). Còn `NOT_READY` thì màn hiện đúng lỗi đó.

### Ngoài phạm vi
- Nút "Đăng ký xác thực", biểu mẫu gửi yêu cầu, lý do từ chối và số lần gửi lại: P2-D1b.
- D02 (trang cá nhân, theo dõi), D03 (hồ sơ uy tín), D07 và mọi thứ báo cáo, khiếu nại: các issue P2-D2 đến P2-D6b.
- Chỉnh sửa hồ sơ, ảnh đại diện, địa chỉ, cài đặt tài khoản: module A. Hàm `AuthViewModel.logout`: module A.
- Phía admin của STT 73 (duyệt trên E03): module E.
- Bố cục ngang: P3-D.
- Bộ nhớ đệm Room cho hồ sơ: không có bảng nào (Câu hỏi 4).

### Tài liệu cần cập nhật
Không có, trừ khi phải đổi hành vi. Nếu Câu hỏi 1 làm thay đổi chủ sở hữu của `UserRepository.getStats`, leader cập nhật `workflow/plan/so-huu.md` mục 5.

### Tự kiểm trước khi mở Pull Request
- [ ] `cd PUBGApp && ./gradlew assembleDebug testDebugUnitTest lintDebug` kết thúc `BUILD SUCCESSFUL`.
- [ ] `sh workflow/scripts/check-java-style.sh` không báo lỗi nào.
- [ ] Trên máy ảo, đăng nhập một tài khoản thường rồi mở tab Cá nhân: thấy tên, khu vực, ba số liệu thật; Firebase Console có đúng các tài liệu `users/{uid}` đã đọc.
- [ ] Tài khoản chưa được duyệt xác thực: khối xác thực báo chưa mua bán được, không có nút gửi (nút thuộc P2-D1b). Sửa tay `verificationStatus` thành PENDING, APPROVED, REJECTED trong Firebase Console rồi mở lại màn: chip đổi đúng "Đang chờ duyệt", "Đã xác thực" (kèm huy hiệu "Tài khoản đã xác thực"), "Bị từ chối".
- [ ] Tài khoản thường không thấy nút "Khu vực quản trị"; tài khoản có tài liệu `admins/{uid}` thấy và bấm vào được E01.
- [ ] Bấm từng dòng lối vào và nút cài đặt: mỗi dòng mở đúng màn đích đã liệt kê (màn đích có thể còn là khung trống).
- [ ] Bấm "Đăng xuất" rồi "Hủy": vẫn ở D01. Bấm lại, đồng ý: về trang chủ B01 chế độ khách, không hiện màn Đăng nhập; bấm Back không quay về D01.
- [ ] Đã đăng xuất (khách), bấm tab Cá nhân: được đưa tới A01.
- [ ] Bật chế độ máy bay: dải báo offline hiện, màn không treo, "Đăng xuất" vẫn bấm được.
- [ ] Xoay màn hình: số liệu không đọc lại (không có lượt đọc Firestore mới trong Logcat), nội dung giữ nguyên.
- [ ] Mọi chữ trên màn có dấu tiếng Việt, không còn chữ "xác minh người bán".
- [ ] PR liệt kê mọi lớp, phương thức, trường công khai mới kèm dòng tài liệu định nghĩa (R16), hoặc ghi "không có".

### Quy mô
- [x] Đây là một lát cắt dọc của một module, khoảng nửa ngày đến một ngày, một Pull Request.

### Câu hỏi cho leader
1. `UserRepository.getStats` có trong `data.md` (STT 8) nhưng không có trong bảng chủ sở hữu ở `so-huu.md` mục 5 (cũng như `createProfile`, `clearExpiredLock`, `searchUsers`, `getVerification`), trong khi D01, D02 và D03 đều cần nó. Đọc 1 (khuyến nghị): giao `getStats` cho Phúc, làm ngay trong P2-D1a. Đọc 2: giao cho Ngân. Chờ trả lời; nếu chưa có thì phần số liệu của D01 hiện lỗi `NOT_READY` rõ ràng.
2. `viewmodel.md` nói màn hình "xóa" `error` sau khi đã hiện và "đặt lại false" `done` sau khi dùng, nhưng mọi LiveData là `private` kèm getter và tài liệu không có phương thức nào để xóa hay đặt lại. Ngoài ra tài liệu nói có getter công khai cho mọi LiveData nhưng không viết tên getter (issue này suy ra `getProfile()`, `getError()`... và bảo chép đúng chữ trong tệp P1 tạo). Đề nghị: leader chốt tên getter và một cách đặt lại `error` và `done` (ví dụ một phương thức, hoặc getter trả `MutableLiveData`), áp cho cả 14 ViewModel. Tới khi có, Fragment chỉ quan sát `error` và `done`, không tự đặt lại.
3. `so-huu.md` mục 4 có `action_global_tab` với tham số `tabId` nhưng tài liệu không nói `tabId` kiểu gì và giá trị nào ứng với tab Trang chủ (B01). Issue này dùng đúng kiểu và giá trị khai trong `nav_main.xml` do P1-06 tạo. Xác nhận, và ghi giá trị vào `so-huu.md` mục 4 để cả năm module dùng cùng một cách.
4. README D01 ghi "Mất mạng: vẫn hiện thông tin đã tải trước đó từ bộ nhớ đệm", nhưng `room-schema.md` chỉ có 6 bảng (tin, lịch sử xem, lịch sử tìm, cuộc trò chuyện, tin nhắn, giao dịch), không có bảng cho hồ sơ. Đọc 1 (mặc định trong issue này): module D không đệm Room; offline chỉ hiện dải báo, khóa nút ghi và giữ dữ liệu đã tải trong ViewModel. Đọc 2: thêm bảng Room (đổi bảng phải hỏi trước, R6, và `AppDatabase` là file của Phát). Xác nhận.
5. `so-huu.md` mục 4 ghi `action_global_user_profile` được gọi từ B03, C02, C03 và `action_global_seller_trust` từ B03, D02; D01 không có trong cột "Ai gọi", nhưng README D01 bắt D01 mở D02 và D03 của chính mình (và README D03 bắt D03 mở D02). Giả định trong issue này: D01 gọi hai hành động toàn cục đó với `userId` của chính mình, không sửa file nào. Xác nhận, hoặc cho cách đi khác.
6. `so-huu.md` mục 2 cho module D ba gói `ui.profile`, `ui.trust`, `ui.admin.complaint` nhưng không nói Fragment nào ở gói nào. Giả định trong cả mười một issue module D: D01 và D02 ở `ui.profile`; D03, D04, D05, D07 ở `ui.trust`; D06 ở `ui.admin.complaint`. Theo khung đã tạo ở P1-06 nếu khác.
