TITLE: feat(auth): đăng ký bước 2, hồ sơ, định danh và danh mục quan tâm
LABELS: type:feature, area:auth, module:a, priority:p1
ASSIGNEES: loopy-tnw
PHASE: P2

Plan item: P2-A2b

### Bối cảnh
- PUBGApp (Pre-owned Users' Bargain Grounds) là ứng dụng Android mua bán đồ cũ, viết bằng Java 17 và XML Views với ViewBinding, dùng Firebase gói Spark (Authentication, Firestore) và Room.
- Module A (Tài khoản và thông báo) do Ngân (`loopy-tnw`) phụ trách: đăng ký, đăng nhập, hồ sơ, địa chỉ, cài đặt tài khoản, thông báo.
- Issue này làm nửa sau của màn A02 `RegisterFragment`: bước 2 điền hồ sơ (điện thoại, khu vực, thông tin định danh, danh mục quan tâm, đồng ý điều khoản), chế độ hoàn tất hồ sơ sau lần đăng nhập Google đầu tiên, và nút "Đăng nhập bằng Google" trên A02.
- Đọc trước: `workflow/docs/ui/a-tai-khoan/README.md` mục "A02. Đăng ký", `workflow/docs/architecture/luong-xu-ly.md` mục 1, `workflow/docs/data/firestore-schema.md#users` và `#identity`.
- Đã có trên `main` (sau P2-A2a và P2-A1b): bước 1, màn chờ xác thực và khung bước 2 của `RegisterFragment`; nút Google và `AuthViewModel.loginWithGoogle` trên A01, đặt `needsProfile`; `CategoryRepository.getAll` làm thật ở P1-04.

### Mã tính năng
F-01, F-02 (STT 9 thuộc F-02)

### Mã việc kế hoạch
P2-A2b, nửa sau của P2-A2 trong `workflow/plan/phase-2-tinh-nang.md` (nửa đầu là P2-A2a, làm trước).

### Module
A. Tài khoản và thông báo (Ngân)

### STT trong bảng đặc tả
1 (phần hồ sơ và đồng ý điều khoản), 2 (nút đăng nhập Google trên A02), 9, 35. Use case UC01 (luồng cơ bản bước 5 đến 8, luồng thay thế 1a, ngoại lệ 6a và 6b), UC02 luồng thay thế 2b.

### Màn hình mockup
A02 (bước 2 và chế độ hoàn tất hồ sơ chưa có ảnh)

### Làm gì và vì sao
Sau issue này người dùng đã xác thực email (hoặc vừa đăng nhập Google lần đầu) điền nốt hồ sơ trên A02 và vào ứng dụng. Bước 2 gồm: số điện thoại; tỉnh hoặc thành phố và quận hoặc huyện; khối "Thông tin định danh" (số CCCD 12 chữ số, ngày sinh, giới tính Nam, Nữ, Khác, địa chỉ thường trú) với ghi chú chỉ bạn và admin xem được; chọn nhiều danh mục quan tâm (STT 35); ô đánh dấu "Tôi đồng ý Điều khoản sử dụng và Chính sách bảo mật" cùng hai liên kết mở văn bản tĩnh trong ứng dụng; nút "Hoàn tất đăng ký". `AuthViewModel.completeProfile(User user, UserIdentity identity, List<String> categoryIds)` (dùng cho cả đăng ký email khi `signUpStep` bằng 3 và Google lần đầu) gọi `UserRepository.createProfile`, ghi `users/{uid}` (ACTIVE, `verificationStatus` NONE, `ratingSum`, `ratingCount`, `warningCount` bằng 0, `termsAcceptedAt`, `interestedCategoryIds`) và `users/{uid}/private/identity` trong một lô ghi. Sau đó đi tiếp như đăng nhập thành công (đọc `admins/{uid}`, `SessionManager.start`, `touchLastActive`, đặt `loggedIn`) và vào B01 kèm một dòng chào mừng. Số CCCD chỉ chủ tài khoản và admin đọc được. Chế độ hoàn tất hồ sơ (khi `needsProfile` là true, sau Google lần đầu) bỏ bước 1 và màn chờ, vào thẳng bước 2, tên điền sẵn và sửa được. Theo `luong-xu-ly.md` mục 1 bước 2 và `trang-thai.md` mục 7 dòng đầu. Issue này phục vụ các dòng barem R-Chapter 4 (picker: `DatePicker` hộp chọn ngày cho ngày sinh; `RecyclerView` cho danh sách danh mục chọn nhiều; input control: ô đánh dấu đồng ý điều khoản và nhóm chọn giới tính), C-APIs (Cloud Firestore với `WriteBatch` ghi hai tài liệu một lần; đăng nhập Google), R-Chapter 1, 2, 6, 7 (giữ trạng thái khi xoay nhờ ViewModel, kiểm tra mạng bằng `NetworkMonitor`, async qua `DataCallback`) và R-Chapter 3 (unit test JUnit cho quy tắc số điện thoại nếu QA9 chốt).

### Tiêu chí nghiệm thu
- [ ] Đăng ký email đủ bước (A2a rồi A2b): trên Firebase Console, `users/{uid}` có `status` `ACTIVE`, `verificationStatus` `NONE`, `ratingSum`, `ratingCount`, `warningCount` bằng 0, `termsAcceptedAt`, `interestedCategoryIds`, `province`, `district`, `phone`, `displayName`, `email`, `searchKeywords`; `users/{uid}/private/identity` có `userId`, `nationalId`, `dateOfBirth`, `gender`, `permanentAddress`. Hai tài liệu cùng được tạo hoặc cùng không tạo (một lô ghi).
- [ ] Sau khi hoàn tất: `SessionManager.isLoggedIn()` là true, ứng dụng vào B01 của `MainActivity` kèm một dòng chào mừng, không phải đăng nhập lại ở A01.
- [ ] Chế độ hoàn tất hồ sơ: đăng nhập Google lần đầu (P2-A1b) mở A02 không có bước 1 và không có màn chờ; tên điền sẵn nếu đọc được (QA8, nếu chưa thì để trống); điền bước 2 và bấm "Hoàn tất đăng ký" thì tạo `users/{uid}` và `private/identity`, rồi vào B01. Bấm quay lại khi chưa hoàn tất thì `AuthViewModel.logout()` (đặc tả: `AuthRepository.signOut`, xóa Room và `SessionManager`) rồi về A01, không để phiên Firebase không có hồ sơ.
- [ ] Nút "Đăng nhập bằng Google" trên A02 chạy cùng đường với A01 (cùng `AuthViewModel.loginWithGoogle`): lần đầu thì sang chế độ hoàn tất hồ sơ, đã có hồ sơ thì vào B01.
- [ ] `users/{uid}.email` và `uid` lấy từ phiên Firebase hiện tại, không lấy từ chữ người dùng nhập hay từ `User` do Fragment đưa vào (R12). `createProfile` chỉ ghi các khóa mà `security-rules.md` mục 1 cho phép ở dòng "Tạo", với giá trị khởi tạo cố định; ghi thêm khóa lạ làm Rules từ chối. `createdAt`, `updatedAt` và `termsAcceptedAt` bằng giờ máy chủ.
- [ ] Số CCCD không đủ 12 chữ số, thiếu ngày sinh hoặc thiếu giới tính: lỗi tiếng Việt dưới ô tương ứng, không gọi mạng (điều kiện đủ dùng `UserIdentity.isComplete()`). Số điện thoại sai định dạng hoặc trống: lỗi dưới ô (định dạng theo QA9). Chưa đánh dấu đồng ý điều khoản thì nút "Hoàn tất đăng ký" không bấm được. Hai liên kết mở văn bản Điều khoản sử dụng và Chính sách bảo mật (văn bản tĩnh trong `strings_account.xml`, viết ngắn).
- [ ] Danh mục quan tâm lấy từ `CategoryRepository.getAll` qua `AuthViewModel.loadCategories()` và `categories`, cho chọn nhiều; các mã đã chọn ghi vào `interestedCategoryIds`. Không chọn danh mục nào vẫn hoàn tất được.
- [ ] Ô tỉnh và quận theo trả lời QA10 (chưa có trả lời thì hai ô nhập chữ tự do). Giá trị ghi vào `province` và `district`.
- [ ] `createProfile` lỗi (Rules từ chối, mất mạng): hiện câu tiếng Việt, ở lại bước 2 với chữ đã nhập còn nguyên để thử lại; không để dữ liệu nửa chừng trong `users`.
- [ ] Nút "Hoàn tất đăng ký" chuyển sang trạng thái chờ khi `loading` là true, không bấm lại được. Mất mạng hoặc quá 15 giây: hiện "Mất kết nối mạng, vui lòng kiểm tra lại đường truyền", chữ đã nhập còn nguyên.
- [ ] Xoay màn hình ở bước 2 giữ chữ đã nhập, ngày sinh và giới tính đã chọn, các danh mục đã chọn, ô đồng ý điều khoản, và chế độ đang đứng. Danh sách danh mục đã tải nằm trong `AuthViewModel.categories`, không tải lại sau khi xoay; tập mã đang chọn do Fragment giữ qua lần xoay (xem bước 6).
- [ ] Kiểm thử đơn vị: quy tắc số điện thoại (nếu QA9 chốt) viết thành phương thức tĩnh không công khai, thuần Java, có test trong `AuthViewModelTest` (hợp lệ, thiếu số, thừa số, không bắt đầu bằng 0, rỗng). `UserIdentity.isComplete()` đã có test ở P1-03. Nếu không có quy tắc mới thì nêu rõ trong Pull Request.
- [ ] `./gradlew assembleDebug testDebugUnitTest lintDebug` và `sh workflow/scripts/check-java-style.sh` đều qua; Pull Request ghi đúng lệnh đã chạy và kết quả thật (R1).
- [ ] Pull Request liệt kê mọi lớp, phương thức, trường công khai mới kèm dòng tài liệu định nghĩa nó (R16). Lớp mới dự kiến duy nhất là `CategoryChoiceAdapter` (lớp giao diện trong `ui.account`, ngoài danh sách của đặc tả lớp, nêu rõ trong Pull Request).

### Tài liệu phải đọc
- `AGENTS.md` mục 6: R12, R15, R16, R17, R19, R20, R21, R23.
- `workflow/plan/so-huu.md` mục 3, 5, 6.
- `workflow/docs/product/dac-ta-chuc-nang.md` dòng STT 1, 2, 9, 35.
- `workflow/docs/product/use-case/uc-a-tai-khoan.md` mục UC01 và UC02.
- `workflow/docs/ui/a-tai-khoan/README.md` mục A02 (kể cả "Khác với ảnh" và "Chế độ hoàn tất hồ sơ").
- `workflow/docs/architecture/luong-xu-ly.md` mục 1 (sơ đồ và bước 2).
- `workflow/docs/architecture/trang-thai.md` mục 7 (dòng đầu: tạo hồ sơ).
- `workflow/docs/architecture/dac-ta-lop/viewmodel.md#authviewmodel`, `data.md#userrepository`, `data.md#categoryrepository`, `model.md#user`, `model.md#useridentity`, `model.md#category`, `enums.md#gender`.
- `workflow/docs/data/firestore-schema.md#users`, `#identity`, `#categories`.
- `workflow/docs/data/security-rules.md` mục 1 (dòng "Tạo"), mục 3 (identity), mục 9 (categories).
- `workflow/docs/conventions/android-java.md` (bốn luật ánh xạ Firestore), `workflow/docs/conventions/giao-dien.md`, `workflow/memory/code/firebase.md`, `workflow/memory/code/README.md`, và skill `pubg-code`, `pubg-ui`.
- `workflow/docs/product/barem-diem.md` mục 3 (dòng barem và kỹ thuật thầy dạy mà issue này phải nêu tên).

### Lớp, trường, trạng thái sẽ dùng
- `AuthViewModel` (`viewmodel.md#authviewmodel`): thuộc tính `needsProfile` (`LiveData<Boolean>`), `signUpStep` (`LiveData<Integer>`), `categories` (`LiveData<List<Category>>`), `loggedIn`, `loading`, `error`, `done` (mỗi cái có getter công khai, tên getter xem QA1); phương thức `public void completeProfile(User user, UserIdentity identity, List<String> categoryIds)` và `public void loadCategories()` (thay thân), cùng phần đầu của `public void logout()` (xem bước 7).
- `UserRepository` (`data.md#userrepository`): `public void createProfile(User user, UserIdentity identity, DataCallback<Void> cb)` (thay thân; xem QA6).
- Chỉ gọi: `AuthViewModel.loginWithGoogle(String idToken)` (đã làm ở P2-A1b); `CategoryRepository.getAll(DataCallback<List<Category>> cb)` (của Phát, `AuthViewModel` dùng qua `loadCategories`); `AdminRepository.isAdmin`, `SessionManager.start`, `UserRepository.touchLastActive` (qua phần quyết định chung đã viết ở P2-A1a); `NetworkMonitor.getInstance()` và `isOnline()`.
- `User` (`model.md#user`): `email`, `displayName`, `phone`, `province`, `district`, `interestedCategoryIds`, `termsAcceptedAt`, `status`, `verificationStatus`, `ratingSum`, `ratingCount`, `warningCount`, `notificationPrefs`, `lastActiveAt`, `buildSearchKeywords()`. `UserStatus.ACTIVE`, `VerificationStatus.NONE`.
- `UserIdentity` (`model.md#useridentity`): `userId`, `nationalId`, `dateOfBirth`, `gender`, `permanentAddress`, `isComplete()`. `Gender`: `MALE`, `FEMALE`, `OTHER` (chữ Nam, Nữ, Khác lấy từ string resource, không hiện tên hằng).
- `Category` (`model.md#category`): `name`, `parentId`, `isActive`.
- Firestore: `users/{uid}`, `users/{uid}/private/identity` (ghi trong một lô), `categories/{id}` (chỉ đọc).
- Lớp giao diện: `RegisterFragment`.
- Lớp mới ngoài đặc tả, thuộc gói giao diện của module: `CategoryChoiceAdapter`.

### File được tạo hoặc sửa
Chép nguyên danh sách này vào kế hoạch của bạn (R19). Nếu khung P1-06 đặt tên file khác thì giữ tên đã có.
- [sửa] `PUBGApp/app/src/main/java/com/nhom3/pubgapp/ui/account/RegisterFragment.java` (bước 2, chế độ hoàn tất hồ sơ, nút Google)
- [sửa] `PUBGApp/app/src/main/res/layout/fragment_account_register.xml` (khối bước 2, nút Google trên bước 1)
- [tạo] `PUBGApp/app/src/main/java/com/nhom3/pubgapp/ui/account/CategoryChoiceAdapter.java`
- [tạo] `PUBGApp/app/src/main/res/layout/item_account_category_choice.xml`
- [sửa] `PUBGApp/app/src/main/java/com/nhom3/pubgapp/viewmodel/AuthViewModel.java` (chỉ thân `completeProfile`, `loadCategories`, và phần đầu của `logout`: `AuthRepository.signOut()` rồi `SessionManager.clear()`)
- [sửa] `PUBGApp/app/src/main/java/com/nhom3/pubgapp/data/UserRepository.java` (chỉ thân `createProfile`)
- [sửa] `PUBGApp/app/src/main/res/values/strings_account.xml` (chữ giao diện, văn bản điều khoản và chính sách)
- [sửa] `PUBGApp/app/src/main/res/navigation/nav_auth.xml` (chỉ khi cần thêm hành động)
- [tạo hoặc sửa] `PUBGApp/app/src/test/java/com/nhom3/pubgapp/viewmodel/AuthViewModelTest.java`

### Các bước đề nghị
Bạn có thể làm theo thứ tự khác, nhưng không được đổi tên lớp, phương thức hay trường nào.
1. Đọc các tài liệu ở mục trên. Kiểm QA6, QA8, QA10 đã có trả lời chưa; `UserRepository.createProfile` chưa được giao thì issue `blocked` (R20).
2. `UserRepository.createProfile(User user, UserIdentity identity, DataCallback<Void> cb)`: lấy `uid` và `email` từ phiên Firebase hiện tại; đặt `status` ACTIVE, `verificationStatus` NONE, `ratingSum`, `ratingCount`, `warningCount` bằng 0, `termsAcceptedAt` bằng giờ máy chủ, gọi `User.buildSearchKeywords()`; đặt `identity.userId` bằng `uid`; ghi `users/{uid}` và `users/{uid}/private/identity` trong một `WriteBatch`; chỉ ghi các khóa mà Rules cho phép. Lỗi đi qua `BaseRepository.fail`.
3. `AuthViewModel.completeProfile(User user, UserIdentity identity, List<String> categoryIds)`: đặt `loading` true; đặt `interestedCategoryIds` bằng `categoryIds`; gọi `UserRepository.createProfile`; thành công thì chạy tiếp phần quyết định chung đã viết ở P2-A1a (`AdminRepository.isAdmin`, `SessionManager.start`, `UserRepository.touchLastActive`, đặt `loggedIn`); cuối cùng đặt `loading` false. Không sao chép phần quyết định chung sang chỗ khác.
4. `AuthViewModel.loadCategories()`: gọi `CategoryRepository.getAll` ngoài luồng chính và đưa kết quả vào `categories`; Fragment gọi một lần khi mở bước điền hồ sơ, danh sách nằm trong ViewModel để không tải lại khi xoay.
5. Layout bước điền hồ sơ trong `fragment_account_register.xml`: ô "Số điện thoại"; tỉnh và quận (theo QA10); khối "Thông tin định danh" (ô CCCD 12 chữ số, ô ngày sinh chỉ đọc và mở `DatePicker` hộp chọn ngày của hệ thống khi bấm, ba lựa chọn giới tính, ô địa chỉ thường trú, dòng ghi chú chỉ bạn và admin xem được); danh sách danh mục chọn nhiều bằng `RecyclerView`; ô đánh dấu đồng ý kèm hai liên kết; nút "Hoàn tất đăng ký". Thêm đường kẻ "hoặc" và nút "Đăng nhập bằng Google" vào bước 1. Dùng token trong `giao-dien.md`, không viết thẳng giá trị. Id view viết lowerCamelCase theo mẫu `<loại><Việc>` (ví dụ trong `android-java.md`: `buttonSignIn`, `textPrice`, `recyclerListings`), khóa chuỗi theo mẫu `<module>_<màn>_<việc>`. Giữ chữ ngày đã chọn trong ô chỉ đọc để View tự giữ qua lần xoay.
6. `CategoryChoiceAdapter` và `item_account_category_choice.xml`: mỗi dòng một ô đánh dấu và tên danh mục. `AuthViewModel` chỉ có `categories` (danh sách để chọn), không có chỗ giữ tập mã đang chọn, nên tập này do Fragment giữ trong trạng thái lưu (`onSaveInstanceState`) để sống qua lần xoay; ghi lựa chọn này bằng skill `pubg-record-decision`.
7. `RegisterFragment`: hiện bước điền hồ sơ khi `signUpStep` là 3; hiện thẳng bước này khi `needsProfile` là true (bỏ bước 1 và màn chờ); gom dữ liệu thành `User` và `UserIdentity`, kiểm `UserIdentity.isComplete()` và điện thoại trước khi gọi `completeProfile`; hai liên kết điều khoản mở một hộp thoại chứa văn bản tĩnh; nút quay lại ở chế độ hoàn tất hồ sơ thì gọi `AuthViewModel.logout()` rồi về A01. Thân `logout` do P2-A5a làm đầy đủ; ở issue này làm trước phần `AuthRepository.signOut()` và `SessionManager.clear()` của nó, P2-A5a thêm bước xóa Room (QA5 của P2-A5a). Khi `loggedIn` là true thì hiện dòng chào mừng rồi mở `MainActivity` bằng Intent tường minh với cờ xóa ngăn xếp. Nút Google dùng lại đúng đoạn lấy `idToken` của `LoginFragment` (P2-A1b) và gọi `AuthViewModel.loginWithGoogle`.
8. Chuỗi vào `strings_account.xml` (khóa dạng `account_register_<việc>`), gồm văn bản Điều khoản sử dụng và Chính sách bảo mật (ngắn, khoảng một trang); không viết chữ cứng.
9. Quy tắc số điện thoại (nếu QA9 chốt) là phương thức tĩnh không công khai kèm test trong `AuthViewModelTest`.
10. Thử trên máy ảo: đăng ký email trọn vẹn; Google lần đầu; thiếu từng trường; không đánh dấu điều khoản; mất mạng; xoay màn hình ở bước điền hồ sơ. Đối chiếu tài liệu trên Console. Ghi kết quả vào Pull Request.
11. Chạy các lệnh ở mục "Tự kiểm trước khi mở Pull Request" và ghi kết quả thật.

### File dùng chung cần thêm dòng
Không có.

### Cấm đụng
- `AndroidManifest.xml`, `nav_main.xml`, `nav_admin.xml`, `themes.xml`, `colors.xml`, `dimens.xml`, `strings_common.xml`, `AppDatabase`, `Converters`, mọi file Gradle, mọi file trong `firebase/`.
- `CategoryRepository.getAll` (của Phát), `AdminRepository.isAdmin`, `SessionManager`, `NetworkMonitor`: chỉ gọi.
- Lớp `model` (`User`, `UserIdentity`, `Category`) và enum: không sửa.
- Các phương thức khác của `AuthRepository`, `UserRepository`, `AuthViewModel` (P2-A5a thêm bước xóa Room vào `logout`).
- `MainActivity` và B01 (của Phát và Tú): chỉ mở bằng Intent, không sửa; dòng chào mừng hiện ở A02 trước khi mở.

### Phụ thuộc
P2-A2a (bước 1 và màn chờ), P2-A1b (nút Google và `needsProfile`), P1-04 (`CategoryRepository.getAll`), P1-06, P1-07. Việc khác phụ thuộc issue này: P2-A5a (hoàn thiện `AuthViewModel.logout`), mọi việc cần tài khoản có hồ sơ đầy đủ, nhất là P2-A3.

### Ngoài phạm vi
- Sửa thông tin định danh sau này (P2-A3, `AccountSettingsFragment`).
- Gửi yêu cầu xác thực tài khoản (biểu mẫu ở D01, của Phúc) và duyệt (E03, của Phát). Đăng ký không đòi admin duyệt; người mới chưa mua bán được cho tới khi được duyệt.
- Quản lý văn bản điều khoản bởi admin (STT 98, Phát triển sau): văn bản tĩnh nằm sẵn trong ứng dụng.
- Chế độ khách, chặn tab (Phát). Bố cục ngang (P3-A).

### Tài liệu cần cập nhật
Không có, trừ khi QA10 hoặc QA11 được trả lời bằng cách đổi đặc tả; khi đó sửa `dac-ta-lop/viewmodel.md` hoặc tài liệu liên quan trong cùng Pull Request (R7).

### Tự kiểm trước khi mở Pull Request
- [ ] Đăng ký email trọn vẹn: trên Console có `users/{uid}` và `users/{uid}/private/identity` đủ trường, `status` ACTIVE, `verificationStatus` NONE.
- [ ] Hoàn tất xong thì vào B01 kèm dòng chào mừng.
- [ ] Đăng nhập Google lần đầu rồi hoàn tất hồ sơ: bước 1 và màn chờ không hiện, tạo được hồ sơ.
- [ ] Nhập CCCD 11 chữ số, bỏ trống ngày sinh, bỏ trống giới tính: mỗi lỗi hiện đúng ô, không gọi mạng.
- [ ] Chưa đánh dấu điều khoản: nút "Hoàn tất đăng ký" không bấm được; hai liên kết mở văn bản.
- [ ] Bật chế độ máy bay rồi bấm "Hoàn tất đăng ký": thấy "Mất kết nối mạng, vui lòng kiểm tra lại đường truyền", chữ còn nguyên, `users` không có tài liệu mới.
- [ ] Xoay màn hình ở bước 2 sau khi chọn ngày, giới tính, hai danh mục, đánh dấu điều khoản: tất cả còn nguyên.
- [ ] `cd PUBGApp && ./gradlew assembleDebug testDebugUnitTest lintDebug` kết thúc `BUILD SUCCESSFUL`.
- [ ] `sh workflow/scripts/check-java-style.sh` không báo lỗi.
- [ ] Danh sách lớp, phương thức, trường công khai mới đã ghi vào Pull Request kèm dòng tài liệu (R16), hoặc ghi rõ là không có.

### Quy mô
- [x] Đây là một lát cắt dọc của một module, khoảng nửa ngày đến một ngày, một Pull Request.

### Câu hỏi cho leader
- **QA1. Tên getter, và cách xóa `error` và `done`.** `viewmodel.md` ghi mọi ViewModel có `loading`, `error`, `done` và getter công khai cho mọi LiveData, nhưng không chép tên getter. Issue này dùng quy ước JavaBean `get` cộng tên thuộc tính (`getLoading()`, `getError()`, `getDone()`, và tương tự cho các LiveData khác). Cột Ý nghĩa cũng nói màn hình "xóa `error` sau khi đã hiện" và "đặt `done` về false sau khi dùng", nhưng LiveData là `private` và đặc tả không có phương thức công khai nào làm việc đó. Cho tới khi có trả lời, Fragment chỉ quan sát và hiện, không thêm phương thức công khai nào vào ViewModel (R20); hệ quả là sau khi xoay màn hình câu lỗi cũ có thể hiện lại. Xin ghi quy ước getter và cách xóa vào `viewmodel.md`.
- **QA6. Chủ của `UserRepository.createProfile`.** Phương thức này vẫn không nằm trong danh sách của Ngân ở `so-huu.md` mục 5, nhưng `trang-thai.md` mục 7 ghi `AuthViewModel` gọi nó. Xin giao cho Ngân để thay thân `NOT_READY`. Chưa giao thì issue này `blocked` (R20).
- **QA8. Tên hiển thị của tài khoản Google.** Chế độ hoàn tất hồ sơ sau lần đăng nhập Google đầu tiên điền sẵn tên (A02 ghi "tên điền sẵn, sửa được"), nhưng `AuthRepository` không có cách đọc tên của tài khoản Google hiện tại (`signInWithGoogle` trả `null` khi chưa có hồ sơ). Nếu không bổ sung thì ô tên để trống ở chế độ này.
- **QA9. Quy tắc nhập liệu còn thiếu con số.** Tài liệu cho số CCCD (12 chữ số, `UserIdentity.isComplete()`) và mật khẩu (tối thiểu 8 ký tự, gồm chữ và số), nhưng không cho định dạng số điện thoại và giới hạn độ dài tên hiển thị, giới thiệu. Đề xuất: số điện thoại 10 chữ số bắt đầu bằng 0, viết thành phương thức tĩnh không công khai (mức package), kiểm thử trong cùng gói. Giới hạn ký tự phải khớp giới hạn `size()` mà Phát viết trong Rules (đầu `security-rules.md` ghi các con số chỉ là mức đề xuất). Xin xác nhận.
- **QA10. Danh sách tỉnh và quận.** `User.province`, `User.district`, `UserAddress.province`, `UserAddress.district` là chuỗi; A02, A05, A06 ghi "chọn Tỉnh hoặc thành phố" và "Quận hoặc huyện"; B02 cũng lọc theo tỉnh. Không có tài liệu nào ghi danh sách nằm ở đâu hay do ai giữ. Module A và B phải dùng cùng một chuỗi cho cùng một tỉnh (tài liệu mẫu dùng "TP.HCM"), nếu không bộ lọc khu vực của Tú không khớp. Xin chốt: một mảng chuỗi tỉnh chung trong `strings_common.xml` (của Phát) và quận nhập chữ tự do, hoặc cách khác. Chưa chốt thì làm ô nhập chữ tự do cho cả tỉnh và quận và ghi rõ trong Pull Request.
- **QA11. Danh mục quan tâm.** (b) UC01 chưa rõ chọn danh mục quan tâm có bắt buộc không và tối thiểu mấy danh mục; issue làm theo hướng không bắt buộc, chọn nhiều tùy ý. (c) `RecommendationEngine` so khớp `Listing.categoryId` với `interestedCategoryIds`; nếu người dùng chọn danh mục gốc có con thì không bao giờ khớp. Đề xuất chỉ cho chọn danh mục con và danh mục gốc không có con. Xin xác nhận.
