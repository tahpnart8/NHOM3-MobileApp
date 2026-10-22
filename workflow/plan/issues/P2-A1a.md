TITLE: feat(auth): đăng nhập email, kiểm tra khóa và khôi phục phiên
LABELS: type:feature, area:auth, module:a, priority:p0
ASSIGNEES: loopy-tnw
PHASE: P2

Plan item: P2-A1a

### Bối cảnh
- PUBGApp (Pre-owned Users' Bargain Grounds) là ứng dụng Android mua bán đồ cũ, viết bằng Java 17 và XML Views với ViewBinding, dùng Firebase gói Spark (Authentication, Firestore) và Room.
- Module A (Tài khoản và thông báo) do Ngân (`loopy-tnw`) phụ trách: đăng ký, đăng nhập, hồ sơ, địa chỉ, cài đặt tài khoản, thông báo.
- Issue này làm màn A01 `LoginFragment` (đăng nhập bằng email và mật khẩu) và phần khôi phục phiên `AuthViewModel.restoreSession`; đăng nhập Google và quên mật khẩu ở P2-A1b.
- Đọc trước: `workflow/docs/ui/a-tai-khoan/README.md` mục "A01. Đăng nhập", `workflow/docs/architecture/luong-xu-ly.md` mục 1, `workflow/docs/product/use-case/uc-a-tai-khoan.md` mục UC02.
- Đã có trên `main` sau P1: mọi lớp đủ chữ ký (thân chưa làm trả `DataCallback.NOT_READY`), `AuthActivity` và `LoginFragment` là khung trống, `nav_auth.xml`, `strings_account.xml`, `SessionManager`, `NetworkMonitor` (có `getInstance()`), mã màu thật trong `colors.xml`.

### Mã tính năng
F-01

### Mã việc kế hoạch
P2-A1a, nửa đầu của P2-A1 trong `workflow/plan/phase-2-tinh-nang.md` (nửa sau là P2-A1b). Tách vì P2-A1 gồm cả Credential Manager của Google và quên mật khẩu, quá một ngày. Kế hoạch còn ghi "mở `MainActivity` hoặc ở lại `AuthActivity`"; theo tài liệu mới (chế độ khách) `MainActivity` mở được cả khi chưa đăng nhập, `AuthActivity` chỉ mở từ nút "Đăng nhập" trên B01 hoặc khi Khách bấm một thao tác cần đăng nhập.

### Module
A. Tài khoản và thông báo (Ngân)

### STT trong bảng đặc tả
2 (phần đăng nhập bằng email và mật khẩu; phần Google ở P2-A1b). Use case UC02 (luồng cơ bản, ngoại lệ 3a, 3c, 3d).

### Màn hình mockup
A01

### Làm gì và vì sao
Sau issue này người đã có tài khoản email đăng nhập được ở A01 rồi vào B01 của `MainActivity`; mở lại ứng dụng khi Firebase còn phiên thì phiên được dựng lại, còn không thì người dùng ở chế độ khách (không bị ép đăng nhập). Luồng theo `luong-xu-ly.md` mục 1, bước 3 đến 6. `AuthViewModel.login` gọi `AuthRepository.signIn`, rồi quyết định cho vào hay không: tài khoản DELETED thì báo "Tài khoản đã xóa" và đăng xuất; đang khóa thì hiện `lockReason` cùng hạn khóa rồi đăng xuất; khóa tạm đã hết hạn thì gọi `UserRepository.clearExpiredLock` rồi đi tiếp; sau đó đọc `admins/{uid}` qua `AdminRepository.isAdmin`, gọi `SessionManager.start`, ghi `lastActiveAt` bằng `UserRepository.touchLastActive` và đặt `loggedIn`. Đăng nhập thành công luôn vào B01, kể cả tài khoản admin (khu quản trị mở từ D01). Email chưa xác thực thì không cho vào: `AuthViewModel` đặt `signUpStep` bằng 2 và A01 chuyển sang màn chờ xác thực của A02 (làm ở P2-A2a). `AuthViewModel.restoreSession` chạy lại đúng các kiểm tra ấy khi Firebase còn phiên. Bốn trạng thái của A01 (đang xử lý, nhập liệu, lỗi, mất mạng) đọc từ `loading` và `error`; quyền thật luôn do Security Rules kiểm tra, cờ admin chỉ để hiện giao diện (R12). Issue này phục vụ các dòng barem C-APIs (Firebase Authentication, đăng nhập bằng email), R-Chapter 1, 2, 6, 7 (Intent tường minh mở `MainActivity` từ `AuthActivity`, giữ trạng thái khi xoay nhờ ViewModel, kiểm tra kết nối mạng bằng `NetworkMonitor`, mọi lời gọi chạy bất đồng bộ qua `DataCallback` không trên luồng chính) và R-Chapter 3 (unit test JUnit cho phần quyết định cho vào).

### Tiêu chí nghiệm thu
- [ ] Đăng nhập bằng email và mật khẩu đúng của tài khoản `ACTIVE` đã xác thực email và đã có hồ sơ: vào B01 của `MainActivity`. `SessionManager.isLoggedIn()` là true, `getCurrentUserId()` bằng `uid` Firebase, và `users/{uid}.lastActiveAt` cùng `updatedAt` được ghi lại (xem trên Firebase Console).
- [ ] Tài khoản có tài liệu `admins/{uid}` thì `SessionManager.isAdmin()` là true; không có thì false. Admin cũng vào B01, không vào thẳng khu quản trị.
- [ ] Email trống hoặc sai định dạng, mật khẩu trống: báo lỗi tiếng Việt ngay dưới ô tương ứng trước khi gọi mạng. Sai email hoặc mật khẩu: dòng báo lỗi màu đỏ dưới ô mật khẩu, không chuyển màn, không đổi dữ liệu. Firebase có thể không phân biệt "sai mật khẩu" với "chưa đăng ký" (chưa kiểm chứng); nếu vậy dùng một câu chung "Email hoặc mật khẩu không đúng".
- [ ] Tài khoản `DELETED`: hiện "Tài khoản đã xóa", `AuthRepository.signOut()` được gọi, ở lại A01, `SessionManager.isLoggedIn()` là false.
- [ ] Tài khoản `PERM_LOCKED`, hoặc `TEMP_LOCKED` chưa hết hạn: hiện `lockReason` và (với khóa tạm) hạn khóa theo ngày giờ đầy đủ, đăng xuất, ở lại A01.
- [ ] Tài khoản `TEMP_LOCKED` đã qua `lockedUntil`: `UserRepository.clearExpiredLock` được gọi, `users/{uid}` về `ACTIVE`, `lockedUntil` rỗng, `lockReason` là chuỗi rỗng (đúng dòng "Hết hạn khóa" của `security-rules.md` mục 1), rồi vào ứng dụng bình thường. Thử bằng cách sửa tay `status` và `lockedUntil` trên Console.
- [ ] Email chưa xác thực: không vào ứng dụng; `AuthViewModel` đặt `signUpStep` bằng 2 và A01 chuyển sang màn chờ xác thực của A02, kèm email vừa nhập (theo `a-tai-khoan/README.md` mục A01 và `viewmodel.md`, thuộc tính `signUpStep`). Chỗ lệch với `luong-xu-ly.md` mục 1 bước 5 xem QA3.
- [ ] Khôi phục phiên: tắt hẳn ứng dụng rồi mở lại khi còn phiên thì `SessionManager` được dựng lại mà không qua form A01 (cần chỗ gọi `restoreSession` của Phát và cách lấy `uid` của phiên còn lại, xem QA7). Không còn phiên thì `restoreSession` không làm gì: `SessionManager.isLoggedIn()` là false, `error` rỗng, người dùng ở chế độ khách. Nếu lúc đó tài khoản đã bị khóa hoặc `DELETED` thì `restoreSession` chặn như khi đăng nhập (đăng xuất, đặt câu lý do vào `error`).
- [ ] Nút "Đăng nhập" chuyển sang trạng thái chờ và không bấm lại được khi `loading` là true. Mất mạng: hiện "Mất kết nối mạng, vui lòng kiểm tra lại đường truyền", màn hình không treo.
- [ ] Xoay màn hình ở A01 giữ chữ trong hai ô, trạng thái chờ và thông báo lỗi (trạng thái nằm trong `AuthViewModel`, không nằm trong Fragment), nên xoay lúc đang gửi không gửi lần hai.
- [ ] Không có thao tác mạng hoặc đọc đĩa trên luồng chính. `User.status` chỉ được ghi bởi `UserRepository.clearExpiredLock` (TEMP_LOCKED sang ACTIVE), đúng `trang-thai.md` mục 7; không ghi trực tiếp ở chỗ khác.
- [ ] Kiểm thử đơn vị: không có phương thức quy tắc công khai mới (`User.isLocked` và `User.isDeleted` đã có test ở P1-03). Nếu tách được quyết định "cho vào, báo đã xóa, báo khóa hay xóa khóa hết hạn" thành phương thức tĩnh không công khai, thuần Java, trong `AuthViewModel` thì thêm `AuthViewModelTest` (cùng gói `viewmodel`) với các trường hợp ACTIVE, DELETED, PERM_LOCKED, TEMP_LOCKED chưa hết hạn, TEMP_LOCKED đã hết hạn. Nếu không tách được thì thử tay đủ 5 trường hợp và ghi kết quả vào Pull Request.
- [ ] `./gradlew assembleDebug testDebugUnitTest lintDebug` và `sh workflow/scripts/check-java-style.sh` đều qua; Pull Request ghi đúng lệnh đã chạy và kết quả thật (R1).
- [ ] Pull Request liệt kê mọi lớp, phương thức, trường công khai mới kèm dòng tài liệu định nghĩa nó (R16). Issue này không cần tên công khai mới ngoài đặc tả.

### Tài liệu phải đọc
- `AGENTS.md` mục 6: R12, R15, R16, R19, R20, R22, R23.
- `workflow/plan/so-huu.md` mục 2, 3, 5, 6.
- `workflow/docs/product/dac-ta-chuc-nang.md` dòng STT 2.
- `workflow/docs/product/use-case/uc-a-tai-khoan.md` mục UC02.
- `workflow/docs/ui/README.md` mục "Điều hướng" và "Chế độ khách"; `workflow/docs/ui/a-tai-khoan/README.md` mục A01 (kể cả "Khác với ảnh": chỉ có ô Email).
- `workflow/docs/architecture/luong-xu-ly.md` mục 1 (bước 3 đến 6) và quy ước chung ở đầu tệp.
- `workflow/docs/architecture/trang-thai.md` mục 7 (`User.status`, thứ tự kiểm tra khóa).
- `workflow/docs/architecture/dac-ta-lop/viewmodel.md#authviewmodel`, `data.md#authrepository`, `data.md#userrepository`, `data.md#adminrepository`, `util.md#sessionmanager`, `model.md#user`, `enums.md#userstatus`.
- `workflow/docs/data/firestore-schema.md#users` và `#admins`; `workflow/docs/data/security-rules.md` mục 1 (dòng "Hết hạn khóa") và mục 8 (admins).
- `workflow/docs/conventions/giao-dien.md` mục 1 đến 3 và 6 (token, bốn trạng thái), `workflow/docs/conventions/android-java.md`.
- `workflow/memory/code/README.md`, `workflow/memory/code/firebase.md`, và skill `pubg-code`, `pubg-ui`.
- `workflow/docs/product/barem-diem.md` mục 3 (dòng barem và kỹ thuật thầy dạy mà issue này phải nêu tên).

### Lớp, trường, trạng thái sẽ dùng
Chỉ tên đã có trong tài liệu, viết đúng như sau.
- `AuthViewModel` (`viewmodel.md#authviewmodel`): thuộc tính `loggedIn` (`LiveData<Boolean>`), `needsProfile` (`LiveData<Boolean>`), `signUpStep` (`LiveData<Integer>`: 1 là email và mật khẩu, 2 là màn chờ xác thực, 3 là hồ sơ), `loading` (`LiveData<Boolean>`), `error` (`LiveData<String>`), `done` (`LiveData<Boolean>`), mỗi thuộc tính có getter công khai (tên getter xem QA1); phương thức `public void login(String email, String password)` và `public void restoreSession()` (thay thân).
- `AuthRepository` (`data.md#authrepository`): `public void signIn(String email, String password, DataCallback<User> cb)`, `public void signOut()`, `public boolean isEmailVerified()`, `public void reloadUser(DataCallback<Boolean> cb)` (thay thân).
- `UserRepository` (`data.md#userrepository`): `public void getProfile(String uid, DataCallback<User> cb)`, `public void clearExpiredLock(String uid, DataCallback<Void> cb)`, `public void touchLastActive(String uid)` (thay thân; xem QA6).
- Chỉ gọi, không sửa: `AdminRepository.isAdmin(String uid, DataCallback<Boolean> cb)`; `SessionManager.getInstance()`, `start(User, boolean)`, `clear()`, `getCurrentUserId()`, `isAdmin()`, `isLoggedIn()`; `NetworkMonitor.getInstance()` và `isOnline()`; `DataCallback<T>` với `onSuccess(T result)` và `onError(String message)`.
- `User` (`model.md#user`): `email`, `displayName`, `status`, `lockedUntil`, `lockReason`, `isLocked()`, `isDeleted()`. `UserStatus`: `ACTIVE`, `TEMP_LOCKED`, `PERM_LOCKED`, `DELETED`.
- Firestore: `users/{uid}` (đọc; khi hết hạn khóa ghi `status`, `lockedUntil`, `lockReason`, `updatedAt`; `touchLastActive` ghi `lastActiveAt`, `updatedAt`), `admins/{uid}` (chỉ đọc).
- Lớp giao diện: `LoginFragment`; `MainActivity` (chỉ mở bằng Intent).
- Đi tới A01 từ màn khác: hành động toàn cục `action_global_sign_in` trong `nav_main.xml` (của Phát; mọi màn gọi nó khi Khách bấm một thao tác cần đăng nhập, `so-huu.md` mục 4). Ngân chỉ bảo đảm A01 mở được từ đó, không sửa hành động.
- Không tạo lớp, trường hay phương thức công khai mới.

### File được tạo hoặc sửa
Chép nguyên danh sách này vào kế hoạch của bạn (R19). Nếu khung P1-06 đặt tên file khác thì giữ tên đã có.
- [sửa] `PUBGApp/app/src/main/java/com/nhom3/pubgapp/ui/account/LoginFragment.java`
- [sửa] `PUBGApp/app/src/main/res/layout/fragment_account_login.xml`
- [sửa] `PUBGApp/app/src/main/java/com/nhom3/pubgapp/viewmodel/AuthViewModel.java` (chỉ thân `login`, `restoreSession`)
- [sửa] `PUBGApp/app/src/main/java/com/nhom3/pubgapp/data/AuthRepository.java` (chỉ thân `signIn`, `signOut`, `isEmailVerified`, `reloadUser`)
- [sửa] `PUBGApp/app/src/main/java/com/nhom3/pubgapp/data/UserRepository.java` (chỉ thân `getProfile`, `clearExpiredLock`, `touchLastActive`)
- [sửa] `PUBGApp/app/src/main/res/values/strings_account.xml`
- [sửa] `PUBGApp/app/src/main/res/navigation/nav_auth.xml` (chỉ khi cần thêm hành động từ A01 sang A02 và A03; đồ thị này thuộc module A)
- [tạo] `PUBGApp/app/src/test/java/com/nhom3/pubgapp/viewmodel/AuthViewModelTest.java` (chỉ khi tách được phần thuần Java, xem tiêu chí kiểm thử)

### Các bước đề nghị
Bạn có thể làm theo thứ tự khác, nhưng không được đổi tên lớp, phương thức hay trường nào.
1. Đọc các tài liệu ở mục trên. Mở `main` và kiểm: `AuthRepository`, `UserRepository`, `AdminRepository`, `AuthViewModel` có đủ chữ ký; `AdminRepository.isAdmin` đã làm thật chưa. Nếu còn `NOT_READY` thì ghi vào issue và đánh `blocked` (R20), không tự viết.
2. `AuthRepository.signOut()`: đăng xuất Firebase (không xóa Room và `SessionManager`, người gọi làm). Làm `public boolean isEmailVerified()` và `public void reloadUser(DataCallback<Boolean> cb)`; kết quả `Boolean` của `reloadUser` là cờ email đã xác thực (đúng như mô tả thuộc tính `emailVerified` ở `viewmodel.md#authviewmodel`).
3. `AuthRepository.signIn(String email, String password, DataCallback<User> cb)`: đăng nhập Firebase Authentication rồi đọc `users/{uid}`, trả `User` kể cả khi đang khóa (ViewModel quyết định). Lỗi Firebase đi qua `BaseRepository.fail` thành câu tiếng Việt.
4. `UserRepository.getProfile(String uid, DataCallback<User> cb)`, `UserRepository.clearExpiredLock(String uid, DataCallback<Void> cb)` (ghi `status` ACTIVE, `lockedUntil` rỗng, `lockReason` chuỗi rỗng, `updatedAt`), `UserRepository.touchLastActive(String uid)` (ghi `lastActiveAt`, `updatedAt`).
5. `AuthViewModel.login(String email, String password)`, theo đúng thứ tự: đặt `loading` true; `AuthRepository.signIn`; email chưa xác thực (`reloadUser` rồi `isEmailVerified()` false) thì đặt `signUpStep` bằng 2 và dừng (QA3); `User.isDeleted()` thì báo "Tài khoản đã xóa" và `signOut`; `User.isLocked()` thì đặt `lockReason` và hạn khóa vào `error` rồi `signOut`; `status` là TEMP_LOCKED mà `isLocked()` false thì `UserRepository.clearExpiredLock`; `AdminRepository.isAdmin`; `SessionManager.start(User, boolean)`; `UserRepository.touchLastActive`; đặt `loggedIn` true; cuối cùng đặt `loading` false. Mọi lỗi đi vào `error`. Viết các bước chung này ở một phương thức riêng không công khai để `restoreSession` và P2-A1b dùng lại.
6. `AuthViewModel.restoreSession()`: nếu `SessionManager.isLoggedIn()` đã true thì thôi; nếu Firebase không còn phiên thì không làm gì; nếu còn thì đọc lại `users/{uid}` và chạy lại các kiểm tra ở bước 5. Phần lấy `uid` của phiên còn lại làm theo trả lời QA7.
7. Layout `fragment_account_login.xml`: logo, tiêu đề "Chào mừng trở lại", ô Email, ô Mật khẩu (có ẩn hiện ký tự), liên kết "Quên mật khẩu?", nút "Đăng nhập", dòng cuối "Chưa có tài khoản? Đăng ký ngay". Chưa có nút Google (thêm ở P2-A1b). Dùng token trong `giao-dien.md`, không viết thẳng màu, cỡ chữ hay khoảng cách. Id view viết lowerCamelCase theo mẫu `<loại><Việc>` (ví dụ trong `android-java.md`: `buttonSignIn`, `textPrice`, `recyclerListings`), khóa chuỗi theo mẫu `<module>_<màn>_<việc>`. Chữ nằm trong `strings_account.xml`, khóa có dạng `account_login_<việc>`.
8. `LoginFragment`: dùng ViewBinding, quan sát `loggedIn`, `signUpStep`, `loading`, `error` qua getter (tên theo QA1). Khi `loggedIn` là true thì mở `MainActivity` bằng Intent tường minh với cờ xóa ngăn xếp và đóng `AuthActivity`; khi `signUpStep` là 2 thì sang A02 bằng hành động trong `nav_auth.xml`, kèm email người dùng vừa gõ làm tham số điều hướng (tên tham số là chi tiết cục bộ của `nav_auth.xml`, ghi lại bằng skill `pubg-record-decision`); "Quên mật khẩu?" sang A03 và "Đăng ký ngay" sang A02 bằng hành động trong `nav_auth.xml`. Có đủ bốn trạng thái: đang xử lý (nút chờ), nhập liệu, lỗi hiện dưới ô, mất mạng. Kiểm định dạng email và ô trống trong Fragment trước khi gọi `login`; kiểm `NetworkMonitor.getInstance().isOnline()` trước khi gọi mạng.
9. `AuthViewModelTest` (nếu tách được phần quyết định) và thử tay 5 kịch bản khóa trên emulator.
10. Tạo tài khoản thử và ghi các bước vào Pull Request: trên Firebase Console, Authentication thêm người dùng email và mật khẩu; Firestore tạo `users/{uid}` đủ trường theo `firestore-schema.md#users` (`status` ACTIVE, `verificationStatus` NONE, `ratingSum`, `ratingCount`, `warningCount` bằng 0); một tài khoản admin theo `firebase-setup.md` mục 10. Vướng email chưa xác thực xem QA3.
11. Chạy các lệnh ở mục "Tự kiểm trước khi mở Pull Request" và ghi kết quả thật.

### File dùng chung cần thêm dòng
Không có.

### Cấm đụng
- `AndroidManifest.xml`, `nav_main.xml`, `nav_admin.xml`, `themes.xml`, `colors.xml`, `dimens.xml`, `strings_common.xml`, `AppDatabase`, `Converters`, mọi file Gradle, mọi file trong `firebase/`.
- `PubgApplication`, `MainActivity`, `SessionManager`, `NetworkMonitor` (của Phát): chỉ gọi.
- `AdminRepository.isAdmin` (của Phát): chỉ gọi.
- Các phương thức khác của `AuthRepository`, `UserRepository`, `AuthViewModel` không nêu ở trên (thuộc P2-A1b, P2-A2a đến P2-A5b hoặc của module khác).
- Lớp `model` và `model.enums`: không sửa.

### Phụ thuộc
P1-01, P1-03, P1-04 (`SessionManager`, `AdminRepository.isAdmin`, `BaseRepository`, `DataCallback`), P1-06 (khung `AuthActivity`, `LoginFragment`, `nav_auth.xml`), P1-07 (Security Rules nền). Việc khác phụ thuộc issue này: P2-A1b, P2-A2a, P2-A3 đến P2-A6 và việc thử của cả nhóm.

### Ngoài phạm vi
- Đăng nhập Google và quên mật khẩu (P2-A1b). Đăng ký, màn chờ xác thực email, hoàn tất hồ sơ (P2-A2a, P2-A2b).
- Đăng xuất: nút ở A04 và `AuthViewModel.logout` (P2-A5a).
- Chỗ gọi `restoreSession` khi mở ứng dụng và hiện câu lý do (Phát, QA7); chặn các tab và thao tác của Khách (B01, B02, B03 và `MainActivity`, của Phát và Tú).
- Chạy `DealViewModel.checkAutoRelease` sau khi khôi phục phiên (luồng 5, của Phát và Hải Long). Gọi `touchLastActive` khi vào chat (của Hải Long).
- Đăng nhập bằng số điện thoại hoặc tên tài khoản: không làm.
- Chặn khóa trong lúc đang dùng ứng dụng: do Security Rules từ chối lần ghi kế tiếp.
- Bố cục ngang (P3-A).

### Tài liệu cần cập nhật
Không có, trừ khi leader trả lời QA3 theo hướng đổi hành vi (khi đó sửa `luong-xu-ly.md` mục 1 bước 5 hoặc `a-tai-khoan/README.md` mục A01 trong cùng Pull Request, R7).

### Tự kiểm trước khi mở Pull Request
- [ ] Đăng nhập tài khoản `ACTIVE` đã xác thực: vào B01, `users/{uid}.lastActiveAt` đổi trên Console.
- [ ] Nhập sai mật khẩu: thấy dòng đỏ dưới ô mật khẩu, không chuyển màn.
- [ ] Sửa tay `status` thành `DELETED` rồi đăng nhập: thấy "Tài khoản đã xóa", ở lại A01.
- [ ] Sửa tay thành `TEMP_LOCKED` với `lockedUntil` trong tương lai: thấy lý do và hạn khóa. Sửa `lockedUntil` về quá khứ: vào được, `status` về `ACTIVE`.
- [ ] Đăng nhập bằng tài khoản email chưa xác thực: không vào B01, thấy màn chờ xác thực của A02 (hoặc ghi rõ chưa kiểm được vì chưa có P2-A2a).
- [ ] Bật chế độ máy bay rồi bấm "Đăng nhập": thấy "Mất kết nối mạng, vui lòng kiểm tra lại đường truyền", nút không treo.
- [ ] Xoay màn hình khi đã gõ chữ và khi nút đang chờ: chữ còn nguyên, không gửi hai lần.
- [ ] Tắt hẳn ứng dụng rồi mở lại khi còn phiên (hoặc ghi rõ chưa kiểm vì chưa có chỗ gọi `restoreSession`).
- [ ] `cd PUBGApp && ./gradlew assembleDebug testDebugUnitTest lintDebug` kết thúc `BUILD SUCCESSFUL`.
- [ ] `sh workflow/scripts/check-java-style.sh` không báo lỗi.
- [ ] Danh sách lớp, phương thức, trường công khai mới đã ghi vào Pull Request kèm dòng tài liệu (R16), hoặc ghi rõ là không có.

### Quy mô
- [x] Đây là một lát cắt dọc của một module, khoảng nửa ngày đến một ngày, một Pull Request.

### Câu hỏi cho leader
- **QA1. Tên getter, và cách xóa `error` và `done`.** `viewmodel.md` ghi mọi ViewModel có `loading`, `error`, `done` và getter công khai cho mọi LiveData, nhưng không chép tên getter. Issue này dùng quy ước JavaBean `get` cộng tên thuộc tính (`getLoading()`, `getError()`, `getDone()`, và tương tự cho các LiveData khác). Cột Ý nghĩa cũng nói màn hình "xóa `error` sau khi đã hiện" và "đặt `done` về false sau khi dùng", nhưng LiveData là `private` và đặc tả không có phương thức công khai nào làm việc đó. Cho tới khi có trả lời, Fragment chỉ quan sát và hiện, không thêm phương thức công khai nào vào ViewModel (R20); hệ quả là sau khi xoay màn hình câu lỗi cũ có thể hiện lại. Xin ghi quy ước getter và cách xóa vào `viewmodel.md`.
- **QA3. Đăng nhập hoặc khôi phục phiên khi tài khoản Firebase có nhưng chưa đủ.** (a) Email chưa xác thực: `a-tai-khoan/README.md` mục A01 ghi chặn và đưa sang màn chờ xác thực, còn `luong-xu-ly.md` mục 1 bước 5 ghi "nhắc xác thực và không cho đăng tin", và mô tả `login` ở `viewmodel.md` không có bước kiểm email. Issue làm theo A01 (chặn, đặt `signUpStep` bằng 2); xin xác nhận và sửa `luong-xu-ly.md` cùng `viewmodel.md`. (b) Email đã xác thực nhưng chưa có `users/{uid}` (người dùng tắt ứng dụng giữa chừng khi đăng ký): đặc tả `signIn` chỉ nói trả `User`. Đề xuất như đăng nhập Google lần đầu: trả `null`, `AuthViewModel` đặt `signUpStep` bằng 3, A02 mở ở bước điền hồ sơ. (c) `restoreSession` gặp hai trường hợp trên: đề xuất không gọi `SessionManager.start`, không đăng xuất, người dùng ở chế độ khách. (d) Tài khoản thử tạo tay trên Firebase Console có email chưa xác thực (chưa kiểm chứng: Console không cho đánh dấu đã xác thực), nên bị chặn ở A01 trước khi P2-A2a xong; xin chỉ cách tạo tài khoản thử cho cả nhóm (ví dụ tài khoản Google, vì email Google luôn đã xác thực).
- **QA6. Chủ của vài phương thức mà module A gọi.** (a) `UserRepository.clearExpiredLock` (và `createProfile` ở P2-A2b) vẫn không nằm trong danh sách của Ngân ở `so-huu.md` mục 5, nhưng `trang-thai.md` mục 7 ghi `AuthViewModel` gọi chúng. Xin giao cho Ngân để thay thân `NOT_READY`. (b) `AdminRepository.isAdmin` thuộc Phát (`so-huu.md` mục 5) nhưng P1-04 không liệt kê nó và không việc P2-E nào nhắc tới. Xin xác nhận Phát viết thật trước P2-A1a; nếu còn `NOT_READY` thì đăng nhập không hoàn tất được.
- **QA7. Khôi phục phiên.** (a) `AuthViewModel.restoreSession` cần `uid` của phiên Firebase còn lại, nhưng `AuthRepository` không có phương thức công khai nào trả `uid` (`BaseRepository.currentUserId()` là `protected`), `SessionManager` còn trống lúc đó, và ViewModel không được gọi `FirebaseAuth` trực tiếp. Xin thêm một phương thức đọc `uid` vào `AuthRepository` (đề xuất `getCurrentUserId()`, chưa chính thức) và cập nhật `data.md`. (b) `luong-xu-ly.md` mục 1 bước 6 vẫn ghi "`PubgApplication` hoặc màn hình khởi động" gọi `restoreSession`. Xin chốt nơi gọi (đề xuất `MainActivity`, vì chế độ khách mở thẳng `MainActivity`) và nơi hiện câu lý do trong `error` khi tài khoản bị khóa hoặc đã xóa; Phát làm phần gọi, Ngân chỉ viết thân `restoreSession`.
