TITLE: feat(auth): đăng nhập Google và quên mật khẩu
LABELS: type:feature, area:auth, module:a, priority:p1
ASSIGNEES: loopy-tnw
PHASE: P2

Plan item: P2-A1b

### Bối cảnh
- PUBGApp (Pre-owned Users' Bargain Grounds) là ứng dụng Android mua bán đồ cũ, viết bằng Java 17 và XML Views với ViewBinding, dùng Firebase gói Spark (Authentication, Firestore) và Room.
- Module A (Tài khoản và thông báo) do Ngân (`loopy-tnw`) phụ trách: đăng ký, đăng nhập, hồ sơ, địa chỉ, cài đặt tài khoản, thông báo.
- Issue này thêm nút "Đăng nhập bằng Google" vào A01 `LoginFragment` và làm màn A03 `ForgotPasswordFragment` (gửi liên kết đặt lại mật khẩu).
- Đọc trước: `workflow/docs/ui/a-tai-khoan/README.md` mục "A01. Đăng nhập" và "A03. Quên mật khẩu", `workflow/docs/architecture/luong-xu-ly.md` mục 1, `workflow/memory/code/firebase.md` mục "Google sign-in uses Credential Manager and the web client ID".
- Đã có trên `main`: khung các màn, mọi lớp đủ chữ ký, và (sau P2-A1a) đăng nhập email với phần quyết định cho vào, `AuthRepository.signOut`, `SessionManager.start`.

### Mã tính năng
F-01

### Mã việc kế hoạch
P2-A1b, nửa sau của P2-A1 trong `workflow/plan/phase-2-tinh-nang.md` (nửa đầu là P2-A1a, làm trước).

### Module
A. Tài khoản và thông báo (Ngân)

### STT trong bảng đặc tả
2 (phần đăng nhập bằng tài khoản Google, yêu cầu bắt buộc của môn học), 3. Use case UC02 (luồng thay thế 2a, 2b) và UC03.

### Màn hình mockup
A01, A03

### Làm gì và vì sao
Sau issue này người dùng bấm "Đăng nhập bằng Google" ở A01 để chọn tài khoản Google và vào ứng dụng; đây là yêu cầu bắt buộc của môn học. Tài khoản Google đã có email được Google xác thực nên không qua màn chờ xác thực. Lần đầu (chưa có `users/{uid}`) thì `AuthRepository.signInWithGoogle` trả `null`, `AuthViewModel` đặt `needsProfile` và màn hình sang A02 ở chế độ hoàn tất hồ sơ (nội dung màn đó làm ở P2-A2b). Đã có hồ sơ thì đi qua cùng phần quyết định đã viết ở P2-A1a (DELETED, khóa, khóa hết hạn, `admins/{uid}`, `SessionManager.start`, `touchLastActive`), dùng lại, không viết hai bản. Người dùng cũng vào A03 từ liên kết "Quên mật khẩu?" để nhập email và nhận liên kết đặt lại mật khẩu qua email (không gửi mã qua SMS). Lấy `idToken` ở Fragment bằng Credential Manager với `GetGoogleIdOption` (xem `firebase.md`), rồi đưa cho `AuthViewModel.loginWithGoogle`. Chữ ký chính xác của Credential Manager phải tra bằng context7 hoặc tài liệu chính thức trước khi viết (R2); trong issue này chúng là "chưa kiểm chứng". Issue này phục vụ các dòng barem C-APIs (đăng nhập Google bằng Credential Manager và Firebase Authentication, yêu cầu riêng của thầy) và R-Chapter 1, 2, 6, 7 (giữ trạng thái khi xoay nhờ ViewModel, kiểm tra kết nối mạng bằng `NetworkMonitor`, mọi lời gọi chạy bất đồng bộ qua `DataCallback` không trên luồng chính).

### Tiêu chí nghiệm thu
- [ ] Trên máy đã đăng ký SHA-1 (`firebase-setup.md` mục 7), bấm "Đăng nhập bằng Google" mở hộp chọn tài khoản; chọn tài khoản đã có `users/{uid}` thì vào B01. Trên Firebase Console, Authentication có người dùng nhà cung cấp Google và `users/{uid}.lastActiveAt` được ghi.
- [ ] Chọn tài khoản Google chưa có `users/{uid}` (lần đầu): `needsProfile` thành true, màn hình sang A02 ở chế độ hoàn tất hồ sơ. Issue này chưa tạo `users/{uid}`; trên Console thấy Authentication có người dùng mới nhưng `users` chưa có tài liệu. Việc ghi hồ sơ làm ở P2-A2b.
- [ ] Tài khoản Google đã có hồ sơ nhưng `DELETED`, đang khóa, hoặc khóa tạm đã hết hạn: cùng kết quả như các tiêu chí tương ứng của P2-A1a (báo "Tài khoản đã xóa" hoặc lý do khóa rồi đăng xuất, hoặc `clearExpiredLock` rồi vào).
- [ ] Người dùng đóng hộp chọn tài khoản mà không chọn: hiện thông báo tiếng Việt ngắn, ở lại A01, không báo lỗi lạ. Thiếu SHA-1 hoặc `google-services.json` cũ (lỗi `DEVELOPER_ERROR`) hoặc không có mạng: hiện câu tiếng Việt nói rõ không đăng nhập được bằng Google, không im lặng, không treo.
- [ ] A03: nhập email đúng định dạng và bấm "Gửi liên kết" thì `AuthRepository.sendPasswordReset` được gọi, thư đặt lại mật khẩu thật sự tới hộp thư (thử bằng một email thật), hiện thông báo thành công rồi quay về A01 (theo A03, xem QA17).
- [ ] A03: email sai định dạng hoặc trống báo lỗi màu đỏ dưới ô nhập, không gọi mạng. Gửi quá nhiều lần liên tiếp thì Firebase từ chối: hiện câu tiếng Việt nói chức năng tạm khóa vài phút. Email chưa đăng ký: Firebase có thể vẫn trả thành công (chưa kiểm chứng); ghi kết quả thật vào Pull Request. Nếu Firebase trả thành công thì hiện cùng câu thông báo thành công, không tiết lộ email có tồn tại hay không.
- [ ] "Nhớ mật khẩu? Đăng nhập" và nút quay lại ở A03 về A01. Nút "Gửi liên kết" chờ và không bấm lại được khi đang gửi.
- [ ] Xoay màn hình ở A01 và A03 giữ chữ đã nhập, trạng thái chờ và `needsProfile`; hộp chọn tài khoản Google đang mở không gây gửi lần hai. Trạng thái nằm trong `AuthViewModel`.
- [ ] Mất mạng ở cả hai màn hiện "Mất kết nối mạng, vui lòng kiểm tra lại đường truyền", nút không treo; nút chờ theo `loading`, câu lỗi lấy từ `error`.
- [ ] Mọi chuyển trạng thái tài khoản (khóa hết hạn) vẫn chỉ đi qua `UserRepository.clearExpiredLock`; issue này không ghi `User.status`.
- [ ] Kiểm thử đơn vị: không có phương thức quy tắc công khai mới; quyết định cho vào đã có test hoặc đã được thử tay ở P2-A1a. Ghi rõ điều đó trong Pull Request.
- [ ] `./gradlew assembleDebug testDebugUnitTest lintDebug` và `sh workflow/scripts/check-java-style.sh` đều qua; Pull Request ghi đúng lệnh đã chạy và kết quả thật (R1). Không thêm thư viện: Credential Manager và Google ID đã có từ P1-02 (R5).
- [ ] Pull Request liệt kê mọi lớp, phương thức, trường công khai mới kèm dòng tài liệu định nghĩa nó (R16).

### Tài liệu phải đọc
- `AGENTS.md` mục 6: R2, R5, R12, R15, R16, R19, R20, R22, R23.
- `workflow/plan/so-huu.md` mục 3, 5, 6.
- `workflow/docs/product/dac-ta-chuc-nang.md` dòng STT 2 và 3.
- `workflow/docs/product/use-case/uc-a-tai-khoan.md` mục UC02 (luồng thay thế 2a, 2b) và UC03.
- `workflow/docs/ui/a-tai-khoan/README.md` mục A01 và A03.
- `workflow/docs/architecture/luong-xu-ly.md` mục 1 (sơ đồ và bước 1 đến 3).
- `workflow/docs/architecture/trang-thai.md` mục 7.
- `workflow/docs/architecture/dac-ta-lop/viewmodel.md#authviewmodel`, `data.md#authrepository`, `data.md#userrepository`, `model.md#user`.
- `workflow/docs/workflow/firebase-setup.md` mục 3, 7, 8 (SHA-1, `google-services.json`).
- `workflow/memory/code/firebase.md`, `workflow/memory/code/README.md`, `workflow/docs/conventions/giao-dien.md`, `workflow/docs/conventions/android-java.md`, và skill `pubg-code`, `pubg-ui`.
- `workflow/docs/product/barem-diem.md` mục 3 (dòng barem và kỹ thuật thầy dạy mà issue này phải nêu tên).

### Lớp, trường, trạng thái sẽ dùng
- `AuthViewModel` (`viewmodel.md#authviewmodel`): thuộc tính `loggedIn`, `needsProfile`, `loading`, `error`, `done` (mỗi cái có getter công khai, tên getter xem QA1); phương thức `public void loginWithGoogle(String idToken)`, `public void resetPassword(String email)` (thay thân).
- `AuthRepository` (`data.md#authrepository`): `public void signInWithGoogle(String idToken, DataCallback<User> cb)` (trả `null` khi chưa có `users/{uid}`), `public void sendPasswordReset(String email, DataCallback<Void> cb)` (thay thân).
- Chỉ gọi, không sửa: `AdminRepository.isAdmin`, `UserRepository.clearExpiredLock`, `UserRepository.touchLastActive`, `SessionManager.start`, `AuthRepository.signOut` (đã làm ở P2-A1a); `NetworkMonitor.getInstance()` và `isOnline()`.
- `User`: `isLocked()`, `isDeleted()`, `status`, `lockedUntil`, `lockReason`. `UserStatus`: `ACTIVE`, `TEMP_LOCKED`, `PERM_LOCKED`, `DELETED`.
- Firestore: `users/{uid}` (đọc để biết có hồ sơ chưa), `admins/{uid}` (đọc). Firebase Authentication: nhà cung cấp Google, email đặt lại mật khẩu.
- Lớp giao diện: `LoginFragment`, `ForgotPasswordFragment`, `RegisterFragment` (chỉ điều hướng tới, nội dung ở P2-A2a và P2-A2b). `AuthViewModel` dùng chung phạm vi `AuthActivity` để A01, A02, A03 cùng thấy `needsProfile`; không thêm tham số điều hướng.
- Không tạo lớp, trường hay phương thức công khai mới.

### File được tạo hoặc sửa
Chép nguyên danh sách này vào kế hoạch của bạn (R19). Nếu khung P1-06 đặt tên file khác thì giữ tên đã có.
- [sửa] `PUBGApp/app/src/main/java/com/nhom3/pubgapp/ui/account/LoginFragment.java` (nút Google, nhận `idToken`, điều hướng khi `needsProfile`)
- [sửa] `PUBGApp/app/src/main/res/layout/fragment_account_login.xml` (thêm đường kẻ "hoặc" và nút "Đăng nhập bằng Google")
- [sửa] `PUBGApp/app/src/main/java/com/nhom3/pubgapp/ui/account/ForgotPasswordFragment.java`
- [sửa] `PUBGApp/app/src/main/res/layout/fragment_account_forgot_password.xml`
- [sửa] `PUBGApp/app/src/main/java/com/nhom3/pubgapp/viewmodel/AuthViewModel.java` (chỉ thân `loginWithGoogle`, `resetPassword`)
- [sửa] `PUBGApp/app/src/main/java/com/nhom3/pubgapp/data/AuthRepository.java` (chỉ thân `signInWithGoogle`, `sendPasswordReset`)
- [sửa] `PUBGApp/app/src/main/res/values/strings_account.xml`
- [sửa] `PUBGApp/app/src/main/res/navigation/nav_auth.xml` (chỉ khi cần thêm hành động A01 sang A03 và A01 sang A02)

### Các bước đề nghị
Bạn có thể làm theo thứ tự khác, nhưng không được đổi tên lớp, phương thức hay trường nào.
1. Đọc các tài liệu ở mục trên. Tra chữ ký Credential Manager (`GetGoogleIdOption`, `setServerClientId` nhận web client ID `default_web_client_id`) bằng context7 hoặc tài liệu chính thức; ghi nguồn và ngày tra vào Pull Request (R2).
2. `AuthRepository.signInWithGoogle(String idToken, DataCallback<User> cb)`: đổi `idToken` lấy phiên Firebase (`GoogleAuthProvider.getCredential(idToken, null)` rồi `signInWithCredential`, theo `firebase.md`), rồi đọc `users/{uid}`; trả `User`, hoặc `null` khi chưa có hồ sơ.
3. `AuthViewModel.loginWithGoogle(String idToken)`: đặt `loading` true rồi gọi bước 2; kết quả `null` thì đặt `needsProfile` true, `loading` false và dừng; kết quả `User` thì chạy phần quyết định chung đã viết ở P2-A1a (DELETED, khóa, khóa hết hạn, `AdminRepository.isAdmin`, `SessionManager.start`, `UserRepository.touchLastActive`, `loggedIn`). Không sao chép phần đó sang chỗ khác.
4. `LoginFragment`: thêm nút "Đăng nhập bằng Google" (có biểu tượng Google) dưới đường kẻ "hoặc"; bấm thì lấy `idToken` qua Credential Manager rồi gọi `AuthViewModel.loginWithGoogle`. Người dùng hủy hộp chọn thì hiện câu ngắn, không gọi ViewModel. Khi `needsProfile` là true thì sang A02 (`RegisterFragment`) bằng hành động trong `nav_auth.xml`.
5. `AuthRepository.sendPasswordReset(String email, DataCallback<Void> cb)` và `AuthViewModel.resetPassword(String email)`: gửi email đặt lại mật khẩu; lỗi (sai định dạng, quá nhiều lần gửi, mất mạng) đi vào `error` bằng câu tiếng Việt.
6. Layout `fragment_account_forgot_password.xml` (Id view viết lowerCamelCase theo mẫu `<loại><Việc>` (ví dụ trong `android-java.md`: `buttonSignIn`, `textPrice`, `recyclerListings`), khóa chuỗi theo mẫu `<module>_<màn>_<việc>`.) và `ForgotPasswordFragment`: nút quay lại góc trái, tiêu đề "Quên mật khẩu", dòng "Nhập email của bạn để nhận liên kết đặt lại mật khẩu", ô Email, nút "Gửi liên kết", dòng cuối "Nhớ mật khẩu? Đăng nhập". Thành công thì hiện thông báo rồi về A01. Có đủ bốn trạng thái: đang gửi (nút chờ), nhập liệu, lỗi dưới ô, mất mạng.
7. Chuỗi mới vào `strings_account.xml` (khóa dạng `account_login_<việc>` và `account_forgot_password_<việc>`), không viết chữ cứng.
8. Thử trên máy thật hoặc máy ảo có Google Play: lần Google đầu (chưa có hồ sơ), lần Google đã có hồ sơ, hủy hộp chọn, gửi liên kết bằng email thật. Ghi kết quả (kể cả hành vi với email chưa đăng ký) vào Pull Request.
9. Chạy các lệnh ở mục "Tự kiểm trước khi mở Pull Request" và ghi kết quả thật.

### File dùng chung cần thêm dòng
Không có. `google-services.json` và SHA-1 do Phát lo ở P1-01.

### Cấm đụng
- `AndroidManifest.xml`, `nav_main.xml`, `nav_admin.xml`, `themes.xml`, `colors.xml`, `dimens.xml`, `strings_common.xml`, `AppDatabase`, `Converters`, mọi file Gradle, `PUBGApp/app/google-services.json`, mọi file trong `firebase/`.
- `AdminRepository.isAdmin`, `SessionManager`, `NetworkMonitor` (của Phát): chỉ gọi.
- Các phương thức khác của `AuthRepository`, `UserRepository`, `AuthViewModel` không nêu ở trên.
- Nội dung `RegisterFragment` (P2-A2a, P2-A2b): ở issue này chỉ điều hướng tới nó.

### Phụ thuộc
P2-A1a (và các phụ thuộc P1 của nó). Cần `google-services.json` mới và SHA-1 máy mình đã thêm vào Firebase (P1-01, P1-09). Việc khác phụ thuộc issue này: P2-A2b (chế độ hoàn tất hồ sơ sau Google).

### Ngoài phạm vi
- Ghi `users/{uid}` và `private/identity` cho người mới (P2-A2b: `AuthViewModel.completeProfile` và `UserRepository.createProfile`).
- Đăng nhập email và khôi phục phiên (P2-A1a). Đổi mật khẩu khi đã đăng nhập (P2-A5a).
- Đăng ký bằng email và màn chờ xác thực (P2-A2a).
- Đặt lại mật khẩu bằng SMS, đăng nhập bằng số điện thoại: không làm.
- Trang đặt mật khẩu mới do Firebase lo; ứng dụng không có màn "nhập mật khẩu mới".
- Bố cục ngang (P3-A).

### Tài liệu cần cập nhật
Không có, trừ khi kiểm chứng cho thấy Firebase không báo lỗi với email chưa đăng ký; khi đó sửa dòng "Email chưa được đăng ký" ở `workflow/docs/ui/a-tai-khoan/README.md` mục A03 và UC03 ngoại lệ 3b cho khớp, trong cùng Pull Request (R7).

### Tự kiểm trước khi mở Pull Request
- [ ] Bấm "Đăng nhập bằng Google" với tài khoản đã có hồ sơ: vào B01.
- [ ] Bấm với tài khoản Google chưa có hồ sơ: sang A02 ở chế độ hoàn tất hồ sơ, `users` chưa có tài liệu mới.
- [ ] Đóng hộp chọn tài khoản: thấy câu tiếng Việt ngắn, ở lại A01.
- [ ] A03: gửi liên kết bằng email thật: thư tới hộp thư, thấy thông báo thành công và về A01.
- [ ] A03: để trống hoặc sai định dạng email: lỗi đỏ dưới ô, không gọi mạng.
- [ ] Bật chế độ máy bay rồi bấm cả hai nút: thấy "Mất kết nối mạng, vui lòng kiểm tra lại đường truyền".
- [ ] Xoay màn hình ở A01 và A03: chữ và trạng thái chờ còn nguyên.
- [ ] `cd PUBGApp && ./gradlew assembleDebug testDebugUnitTest lintDebug` kết thúc `BUILD SUCCESSFUL`.
- [ ] `sh workflow/scripts/check-java-style.sh` không báo lỗi.
- [ ] Danh sách lớp, phương thức, trường công khai mới đã ghi vào Pull Request kèm dòng tài liệu (R16), hoặc ghi rõ là không có.

### Quy mô
- [x] Đây là một lát cắt dọc của một module, khoảng nửa ngày đến một ngày, một Pull Request.

### Câu hỏi cho leader
- **QA1. Tên getter, và cách xóa `error` và `done`.** `viewmodel.md` ghi mọi ViewModel có `loading`, `error`, `done` và getter công khai cho mọi LiveData, nhưng không chép tên getter. Issue này dùng quy ước JavaBean `get` cộng tên thuộc tính (`getLoading()`, `getError()`, `getDone()`, và tương tự cho các LiveData khác). Cột Ý nghĩa cũng nói màn hình "xóa `error` sau khi đã hiện" và "đặt `done` về false sau khi dùng", nhưng LiveData là `private` và đặc tả không có phương thức công khai nào làm việc đó. Cho tới khi có trả lời, Fragment chỉ quan sát và hiện, không thêm phương thức công khai nào vào ViewModel (R20); hệ quả là sau khi xoay màn hình câu lỗi cũ có thể hiện lại. Xin ghi quy ước getter và cách xóa vào `viewmodel.md`.
- **QA17. Sau khi gửi liên kết đặt lại mật khẩu.** UC03 bước 7 vẫn ghi hệ thống tự đăng nhập và đưa tới Trang chủ, còn A03 ghi thông báo thành công rồi quay về A01. Firebase đặt lại mật khẩu trên trang web, ứng dụng không nhận được mật khẩu mới nên không thể tự đăng nhập. Issue làm theo A03 (về A01). Xin xác nhận và sửa UC03 bước 7.
