TITLE: feat(auth): đăng ký bước 1 và màn chờ xác thực email
LABELS: type:feature, area:auth, module:a, priority:p1
ASSIGNEES: loopy-tnw
PHASE: P2

Plan item: P2-A2a

### Bối cảnh
- PUBGApp (Pre-owned Users' Bargain Grounds) là ứng dụng Android mua bán đồ cũ, viết bằng Java 17 và XML Views với ViewBinding, dùng Firebase gói Spark (Authentication, Firestore) và Room.
- Module A (Tài khoản và thông báo) do Ngân (`loopy-tnw`) phụ trách: đăng ký, đăng nhập, hồ sơ, địa chỉ, cài đặt tài khoản, thông báo.
- Issue này làm nửa đầu của màn A02 `RegisterFragment`: bước 1 (tên hiển thị, email, mật khẩu) và màn chờ xác thực email "Kiểm tra hộp thư"; bước 2 (hồ sơ) làm ở P2-A2b.
- Đọc trước: `workflow/docs/ui/a-tai-khoan/README.md` mục "A02. Đăng ký", `workflow/docs/product/use-case/uc-a-tai-khoan.md` mục UC01, `workflow/docs/architecture/dac-ta-lop/data.md#authrepository`.
- Đã có trên `main`: khung `RegisterFragment` trống, mọi lớp đủ chữ ký, và (sau P2-A1a) `AuthRepository.reloadUser`, `isEmailVerified`, `signOut`, `LoginFragment` có liên kết sang A02.

### Mã tính năng
F-01

### Mã việc kế hoạch
P2-A2a, nửa đầu của P2-A2 trong `workflow/plan/phase-2-tinh-nang.md` (nửa sau là P2-A2b). Tách vì sau quyết định ngày 2026-10-22 thứ tự đăng ký đổi thành: email và mật khẩu, gửi liên kết, bấm liên kết, rồi mới điền hồ sơ. Màn chờ xác thực, kiểm tra đã xác thực và gửi lại liên kết là một khối việc riêng, cộng với bước 2 (định danh, danh mục quan tâm, điều khoản) thì quá một ngày. Kế hoạch ghi "gửi email xác thực" ở cuối đăng ký; nay nó ở đầu.

### Module
A. Tài khoản và thông báo (Ngân)

### STT trong bảng đặc tả
1 (phần tạo tài khoản và xác thực email; phần hồ sơ ở P2-A2b). Use case UC01 (luồng cơ bản bước 1 đến 5, luồng thay thế 1b và 3a, ngoại lệ 2a và 4a).

### Màn hình mockup
A02 (bước 1 như ảnh; màn chờ xác thực chưa có ảnh)

### Làm gì và vì sao
Sau issue này người mới đăng ký được tài khoản email theo đúng thứ tự mới, không có mã OTP và không có đồng hồ 60 giây. Ở bước 1 người dùng nhập tên hiển thị, email, mật khẩu (tối thiểu 8 ký tự, gồm chữ và số) và xác nhận mật khẩu, rồi bấm "Tiếp tục". `AuthViewModel.startSignUp(String email, String password)` tạo tài khoản trên Firebase Authentication và gửi liên kết xác thực đến email (`AuthRepository.signUp`), rồi đặt `signUpStep` bằng 2 để `RegisterFragment` hiện màn chờ "Kiểm tra hộp thư" (nằm ngay trong `RegisterFragment`, không tạo lớp mới) với email vừa nhập, nút "Gửi lại liên kết" (`AuthViewModel.resendVerificationLink`) và nút quay lại về bước 1. Người dùng mở thư và bấm liên kết; khi màn hình quay lại phía trước, `AuthViewModel.checkEmailVerified` làm mới cờ xác thực bằng `AuthRepository.reloadUser`, và nếu đã xác thực thì `emailVerified` thành true, `signUpStep` thành 3 và Fragment sang bước điền hồ sơ (gọi "bước 2" trong tài liệu giao diện). Chưa xác thực thì không mở được bước điền hồ sơ. Email chưa xác thực khi đăng nhập ở A01 cũng đưa người dùng tới màn chờ này (P2-A1a đặt `signUpStep` bằng 2). Tên hiển thị gõ ở bước 1 nằm trong ô nhập của layout và được dùng lại ở P2-A2b khi dựng `User` cho `completeProfile` (QA18). Issue này phục vụ các dòng barem R-Chapter 4 (input control: ô nhập có kiểm tra dữ liệu và báo lỗi dưới ô, nút có trạng thái chờ), R-Chapter 1, 2, 6, 7 (vòng đời Fragment: kiểm tra đã xác thực khi màn quay lại phía trước; giữ bước đang đứng khi xoay nhờ `signUpStep` trong ViewModel; kiểm tra mạng bằng `NetworkMonitor`; async qua `DataCallback`), C-APIs (Firebase Authentication, liên kết xác thực email) và R-Chapter 3 (unit test JUnit cho quy tắc mật khẩu).

### Tiêu chí nghiệm thu
- [ ] Nhập đủ bước 1 hợp lệ rồi "Tiếp tục": trên Firebase Console, Authentication có người dùng email mới (chưa xác thực); email chứa liên kết xác thực thật sự tới hộp thư (thử bằng email thật); `signUpStep` thành 2 và màn hình sang "Kiểm tra hộp thư". Chưa có tài liệu nào trong `users` (hồ sơ tạo ở P2-A2b).
- [ ] Bấm liên kết trong thư rồi quay lại ứng dụng: `checkEmailVerified` chạy khi màn hình quay lại phía trước, `emailVerified` thành true, `signUpStep` thành 3 và ứng dụng sang bước điền hồ sơ mà không phải đăng nhập lại. (Chưa kiểm chứng: sau khi bấm liên kết, `isEmailVerified` chỉ đúng sau `reloadUser`; ghi kết quả thật vào Pull Request.) Trong lúc chờ P2-A2b, khung bước điền hồ sơ chỉ cần hiện tiêu đề "Hoàn tất hồ sơ" trống.
- [ ] Chưa bấm liên kết mà quay lại ứng dụng: ở lại màn chờ, hiện dòng nhắc cố định theo QA2 (g), không báo lỗi lạ và không sang bước điền hồ sơ.
- [ ] "Gửi lại liên kết" gửi một thư mới (kiểm bằng hộp thư) và khóa nút trong ít giây để tránh bấm liên tục. Bấm quá nhiều lần liên tiếp và Firebase từ chối thì hiện câu tiếng Việt "Bạn đã gửi quá nhiều lần, hãy thử lại sau ít phút".
- [ ] Nút quay lại (và nút Back của hệ thống) ở màn chờ về bước 1 để sửa email; việc đặt `signUpStep` về 1 và xử lý tài khoản chưa xác thực đã tạo, để đăng ký lại đúng email đó không bị báo "đã tồn tại", làm theo trả lời QA2 (f). Chưa có trả lời thì chưa làm nút này và ghi rõ trong Pull Request.
- [ ] Không có ô nhập mã, không có đồng hồ đếm ngược 60 giây, không có chữ "OTP" ở bất kỳ đâu trong giao diện và chuỗi.
- [ ] Email sai định dạng hoặc đã được đăng ký: lỗi tiếng Việt màu đỏ ngay dưới ô email, ở lại bước 1. Tên hiển thị trống: lỗi dưới ô tên. Mật khẩu dưới 8 ký tự, hoặc thiếu chữ, hoặc thiếu số: lỗi dưới ô mật khẩu ("Mật khẩu cần ít nhất 8 ký tự, gồm chữ và số"). Xác nhận không khớp: lỗi dưới ô xác nhận. Mọi lỗi nhập hiện trước khi gọi mạng.
- [ ] Đăng nhập ở A01 bằng tài khoản email chưa xác thực: A01 chuyển tới màn chờ này với email đã gõ (P2-A1a đặt `signUpStep` bằng 2 và truyền email qua tham số điều hướng); màn chờ hiện đúng email đó.
- [ ] Nút "Tiếp tục" và "Gửi lại liên kết" chuyển sang trạng thái chờ khi `loading` là true, không bấm lại được. Mất mạng: hiện "Mất kết nối mạng, vui lòng kiểm tra lại đường truyền", chữ đã nhập còn nguyên.
- [ ] Xoay màn hình giữ chữ đã nhập ở bước 1, bước đang đứng (`signUpStep`) và email đang hiện ở màn chờ. Bước đang đứng nằm trong `AuthViewModel`, không nằm trong Fragment, nên xoay lúc đang chờ không gửi thư lần hai.
- [ ] Không có thao tác mạng trên luồng chính. Mật khẩu không được ghi vào log, không lưu ở đâu ngoài lời gọi `AuthRepository.signUp`.
- [ ] Kiểm thử đơn vị: quy tắc mật khẩu (tối thiểu 8 ký tự, có chữ và số) và quy tắc "xác nhận mật khẩu khớp" viết thành phương thức tĩnh không công khai (mức package), thuần Java, trong `AuthViewModel`; `AuthViewModelTest` kiểm: hợp lệ, đúng 8 ký tự, dưới 8 ký tự, chỉ chữ, chỉ số, rỗng, không khớp.
- [ ] `./gradlew assembleDebug testDebugUnitTest lintDebug` và `sh workflow/scripts/check-java-style.sh` đều qua; Pull Request ghi đúng lệnh đã chạy và kết quả thật (R1).
- [ ] Pull Request liệt kê mọi lớp, phương thức, trường công khai mới kèm dòng tài liệu định nghĩa nó (R16). Issue này không cần tên công khai mới ngoài đặc tả: `startSignUp`, `resendVerificationLink`, `checkEmailVerified`, `signUpStep`, `emailVerified` đã có ở `viewmodel.md#authviewmodel`.

### Tài liệu phải đọc
- `AGENTS.md` mục 6: R2, R12, R15, R16, R17, R19, R20, R23.
- `workflow/plan/so-huu.md` mục 3, 5, 6.
- `workflow/docs/product/dac-ta-chuc-nang.md` dòng STT 1.
- `workflow/docs/product/use-case/uc-a-tai-khoan.md` mục UC01.
- `workflow/docs/ui/a-tai-khoan/README.md` mục A01 (trạng thái "chưa xác thực email") và A02 (kể cả "Khác với ảnh").
- `workflow/docs/architecture/luong-xu-ly.md` quy ước chung ở đầu tệp (mục 1 bước 5).
- `workflow/docs/architecture/dac-ta-lop/viewmodel.md#authviewmodel`, `data.md#authrepository`.
- `workflow/docs/data/security-rules.md` mục "Giới hạn" (dòng `request.auth.token.email_verified`).
- `workflow/docs/conventions/giao-dien.md` mục 1 đến 3 và 6; `workflow/docs/conventions/android-java.md`.
- `workflow/memory/code/README.md`, `workflow/memory/code/firebase.md`, và skill `pubg-code`, `pubg-ui`.
- `workflow/docs/product/barem-diem.md` mục 3 (dòng barem và kỹ thuật thầy dạy mà issue này phải nêu tên).

### Lớp, trường, trạng thái sẽ dùng
- `AuthRepository` (`data.md#authrepository`): `public void signUp(String email, String password, DataCallback<String> cb)` (trả `uid`; tạo tài khoản rồi gửi email xác thực), `public void sendVerificationEmail(DataCallback<Void> cb)` (thay thân hai phương thức này); chỉ gọi `public void reloadUser(DataCallback<Boolean> cb)` và `public boolean isEmailVerified()` (đã làm ở P2-A1a); `public void deleteLogin(DataCallback<Void> cb)` chỉ khi QA2 (f) được chấp nhận.
- `AuthViewModel` (`viewmodel.md#authviewmodel`): thuộc tính `signUpStep` (`LiveData<Integer>`: 1 là email và mật khẩu, 2 là màn chờ xác thực, 3 là hồ sơ), `emailVerified` (`LiveData<Boolean>`), `loading`, `error`, `done` (mỗi cái có getter công khai, tên getter xem QA1); phương thức `public void startSignUp(String email, String password)`, `public void resendVerificationLink()`, `public void checkEmailVerified()` (thay thân). Đặc tả không còn `register(...)`; không tạo lại phương thức đó.
- Chỉ gọi: `NetworkMonitor.getInstance()` và `isOnline()`, `DataCallback<T>` với `onSuccess(T result)` và `onError(String message)`.
- Firebase Authentication: tạo người dùng email và mật khẩu, gửi email xác thực. Không ghi Firestore trong issue này.
- Lớp giao diện: `RegisterFragment` (bước 1 và màn chờ xác thực trong cùng lớp), `LoginFragment` (chỉ điều hướng tới).
- Không tạo lớp mới.

### File được tạo hoặc sửa
Chép nguyên danh sách này vào kế hoạch của bạn (R19). Nếu khung P1-06 đặt tên file khác thì giữ tên đã có.
- [sửa] `PUBGApp/app/src/main/java/com/nhom3/pubgapp/ui/account/RegisterFragment.java` (bước 1 và màn chờ xác thực)
- [sửa] `PUBGApp/app/src/main/res/layout/fragment_account_register.xml` (khối bước 1, khối chờ xác thực, khung bước 2 trống)
- [sửa] `PUBGApp/app/src/main/java/com/nhom3/pubgapp/viewmodel/AuthViewModel.java` (chỉ thân `startSignUp`, `resendVerificationLink`, `checkEmailVerified` và các phương thức tĩnh không công khai của quy tắc nhập liệu)
- [sửa] `PUBGApp/app/src/main/java/com/nhom3/pubgapp/data/AuthRepository.java` (chỉ thân `signUp`, `sendVerificationEmail`, và `deleteLogin` nếu QA2 (f) được chấp nhận)
- [sửa] `PUBGApp/app/src/main/res/values/strings_account.xml`
- [sửa] `PUBGApp/app/src/main/res/navigation/nav_auth.xml` (chỉ khi cần thêm hành động)
- [tạo hoặc sửa] `PUBGApp/app/src/test/java/com/nhom3/pubgapp/viewmodel/AuthViewModelTest.java`

### Các bước đề nghị
Bạn có thể làm theo thứ tự khác, nhưng không được đổi tên lớp, phương thức hay trường nào.
1. Đọc các tài liệu ở mục trên và kiểm QA2 đã có trả lời chưa (chỉ phần nút quay lại và dòng nhắc ở màn chờ phụ thuộc vào nó).
2. Viết phương thức tĩnh không công khai (mức package) kiểm mật khẩu "tối thiểu 8 ký tự, có chữ và có số" và phương thức kiểm "xác nhận khớp" trong `AuthViewModel`, kèm `AuthViewModelTest`. Ghi lựa chọn đặt quy tắc ở đây bằng skill `pubg-record-decision`.
3. `AuthRepository.signUp(String email, String password, DataCallback<String> cb)`: tạo người dùng Firebase Authentication, gửi email xác thực, trả `uid`; email đã tồn tại thì báo bằng câu tiếng Việt rõ ràng qua `BaseRepository.fail`. `AuthRepository.sendVerificationEmail(DataCallback<Void> cb)`: gửi lại liên kết cho người dùng hiện tại.
4. `AuthViewModel.startSignUp(String email, String password)`: kiểm quy tắc ở bước 2, đặt `loading` true, gọi `AuthRepository.signUp`, thành công thì đặt `signUpStep` bằng 2, lỗi thì đặt `error`, cuối cùng đặt `loading` false. `AuthViewModel.resendVerificationLink()`: gọi `AuthRepository.sendVerificationEmail`, lỗi vào `error`. `AuthViewModel.checkEmailVerified()`: gọi `AuthRepository.reloadUser`; kết quả true thì đặt `emailVerified` true và `signUpStep` bằng 3; kết quả false thì không đổi gì.
5. Nếu QA2 (f) được chấp nhận: viết `AuthRepository.deleteLogin(DataCallback<Void> cb)` và gọi nó khi người dùng quay lại bước 1 từ màn chờ, qua phương thức mà leader chốt; ghi rõ trong Pull Request là P2-A5b dùng lại `deleteLogin`.
6. Layout `fragment_account_register.xml`: ba khối trong một layout (bước 1, chờ xác thực, khung bước điền hồ sơ trống), chỉ hiện một khối theo `signUpStep`. Bước 1: tiêu đề "Đăng ký", một dòng hướng dẫn, ô "Tên hiển thị", "Email", "Mật khẩu", "Xác nhận mật khẩu" (hai ô mật khẩu có ẩn hiện ký tự), nút "Tiếp tục", dòng cuối "Đã có tài khoản? Đăng nhập". Chờ xác thực: tiêu đề "Kiểm tra hộp thư", dòng giải thích đã gửi liên kết tới email và nhắc mở thư, email đã nhập, nút "Gửi lại liên kết", nút quay lại. Dùng token trong `giao-dien.md`; không viết thẳng màu, cỡ chữ, khoảng cách. Id view viết lowerCamelCase theo mẫu `<loại><Việc>` (ví dụ trong `android-java.md`: `buttonSignIn`, `textPrice`, `recyclerListings`), khóa chuỗi theo mẫu `<module>_<màn>_<việc>`. Nút "Đăng nhập bằng Google" của A02 làm ở P2-A2b.
7. `RegisterFragment`: ViewBinding; quan sát `signUpStep`, `emailVerified`, `loading` và `error` qua getter (tên theo QA1); kiểm tên, email, mật khẩu, xác nhận trong Fragment trước khi gọi ViewModel; trong `onResume`, khi `signUpStep` là 2, gọi `checkEmailVerified()` để ứng dụng nhận ra email đã xác thực khi người dùng quay lại từ hộp thư; xử lý nút Back của hệ thống cho khớp nút quay lại; "Đăng nhập" về A01. Email hiện ở màn chờ lấy từ ô email của bước 1, hoặc từ tham số điều hướng do A01 truyền sang.
8. Chuỗi vào `strings_account.xml` (khóa dạng `account_register_<việc>`), gồm các câu lỗi và dòng nhắc ở màn chờ; không viết chữ cứng.
9. Thử trên máy ảo với email thật: đủ luồng bước 1, màn chờ, bấm liên kết, quay lại; gửi lại liên kết; quay lại sửa email; email đã tồn tại; mất mạng; xoay màn hình ở từng bước. Ghi kết quả vào Pull Request.
10. Chạy các lệnh ở mục "Tự kiểm trước khi mở Pull Request" và ghi kết quả thật.

### File dùng chung cần thêm dòng
Không có.

### Cấm đụng
- `AndroidManifest.xml`, `nav_main.xml`, `nav_admin.xml`, `themes.xml`, `colors.xml`, `dimens.xml`, `strings_common.xml`, `AppDatabase`, `Converters`, mọi file Gradle, mọi file trong `firebase/`.
- `SessionManager`, `NetworkMonitor` (của Phát): chỉ gọi.
- `UserRepository` (không đụng trong issue này; `createProfile` thuộc P2-A2b).
- Các phương thức khác của `AuthRepository` và `AuthViewModel` không nêu ở trên. Lớp `model` và `model.enums`: không sửa.

### Phụ thuộc
P2-A1a (và P1-01, P1-04, P1-06). Phần nút quay lại ở màn chờ cần leader trả lời QA2 (f). Việc khác phụ thuộc issue này: P2-A2b (bước điền hồ sơ), P2-A5a (dùng lại quy tắc mật khẩu).

### Ngoài phạm vi
- Bước 2: số điện thoại, tỉnh và quận, thông tin định danh, danh mục quan tâm, đồng ý điều khoản, tạo hồ sơ (P2-A2b). Chế độ hoàn tất hồ sơ sau Google và nút "Đăng nhập bằng Google" trên A02 (P2-A2b).
- Gửi mã OTP, đồng hồ đếm ngược, đăng ký bằng số điện thoại: không làm.
- Chặn đăng tin khi chưa được admin duyệt xác thực tài khoản (`User.canSell`, `User.canBuy`; biểu mẫu xác thực là của module D, màn D01).
- Bố cục ngang (P3-A).

### Tài liệu cần cập nhật
Cập nhật `workflow/docs/architecture/dac-ta-lop/viewmodel.md` (mục `AuthViewModel`) trong cùng Pull Request khi leader chốt phương thức quay lại bước 1 ở QA2 (R7). Sửa UC01 nếu QA21 được trả lời theo hướng khác.

### Tự kiểm trước khi mở Pull Request
- [ ] Đăng ký với email thật: thư chứa liên kết tới hộp thư, Authentication có người dùng mới, `users` chưa có tài liệu.
- [ ] Bấm liên kết rồi quay lại ứng dụng: tự sang khung bước 2.
- [ ] Chưa bấm liên kết: ở lại màn chờ, không sang bước 2.
- [ ] "Gửi lại liên kết": thư mới tới; nút quay lại về bước 1 và đăng ký lại cùng email không báo đã tồn tại (nếu QA2 (f) được chấp nhận).
- [ ] Nhập mật khẩu 7 ký tự, mật khẩu chỉ chữ, mật khẩu chỉ số, xác nhận lệch: mỗi trường hợp có lỗi đỏ đúng ô, không gọi mạng.
- [ ] Không có chữ OTP và không có đếm ngược 60 giây trong giao diện.
- [ ] Bật chế độ máy bay rồi bấm "Tiếp tục" và "Gửi lại liên kết": thấy "Mất kết nối mạng, vui lòng kiểm tra lại đường truyền".
- [ ] Xoay màn hình ở bước 1 và ở màn chờ: chữ và bước đang đứng còn nguyên, không gửi thư lần hai.
- [ ] `cd PUBGApp && ./gradlew assembleDebug testDebugUnitTest lintDebug` kết thúc `BUILD SUCCESSFUL`, và `AuthViewModelTest` chạy qua.
- [ ] `sh workflow/scripts/check-java-style.sh` không báo lỗi.
- [ ] Danh sách lớp, phương thức, trường công khai mới đã ghi vào Pull Request kèm dòng tài liệu (R16), hoặc ghi rõ là không có.

### Quy mô
- [x] Đây là một lát cắt dọc của một module, khoảng nửa ngày đến một ngày, một Pull Request.

### Câu hỏi cho leader
- **QA1. Tên getter, và cách xóa `error` và `done`.** `viewmodel.md` ghi mọi ViewModel có `loading`, `error`, `done` và getter công khai cho mọi LiveData, nhưng không chép tên getter. Issue này dùng quy ước JavaBean `get` cộng tên thuộc tính (`getLoading()`, `getError()`, `getDone()`, và tương tự cho các LiveData khác). Cột Ý nghĩa cũng nói màn hình "xóa `error` sau khi đã hiện" và "đặt `done` về false sau khi dùng", nhưng LiveData là `private` và đặc tả không có phương thức công khai nào làm việc đó. Cho tới khi có trả lời, Fragment chỉ quan sát và hiện, không thêm phương thức công khai nào vào ViewModel (R20); hệ quả là sau khi xoay màn hình câu lỗi cũ có thể hiện lại. Xin ghi quy ước getter và cách xóa vào `viewmodel.md`.
- **QA2. Đăng ký bằng liên kết xác thực: phần còn thiếu.** `viewmodel.md` đã có `startSignUp`, `resendVerificationLink`, `checkEmailVerified`, `signUpStep` và `emailVerified`. Còn thiếu hai chỗ. (f) Nút quay lại ở màn chờ để sửa email cần đưa `signUpStep` về 1, nhưng không có phương thức công khai nào làm việc đó (LiveData là `private`); và tài khoản chưa xác thực đã tạo trên Firebase sẽ chặn việc đăng ký lại cùng email. Đề xuất một phương thức đưa `signUpStep` về 1 (tên chưa chính thức: `backToSignUp()`) và gọi `AuthRepository.deleteLogin` trước đó (chưa kiểm chứng: Firebase cho xóa tài khoản vừa tạo mà không cần xác thực lại). (g) Liên kết trong thư mở trang web của Firebase, ứng dụng không nhận được tín hiệu "liên kết hỏng"; ứng dụng chỉ biết email vẫn chưa xác thực sau `reloadUser`, nên câu "Liên kết xác thực không hợp lệ hoặc đã hết hạn" của A02 không phân biệt được với "chưa bấm". Đề xuất một dòng nhắc cố định ở màn chờ ("Chưa thấy email được xác thực. Nếu liên kết báo hết hạn, hãy bấm Gửi lại liên kết"). Xin chốt hai mục.
- **QA3 (phần liên quan đến A02). Email đã xác thực nhưng chưa có `users/{uid}`** (người dùng tắt ứng dụng giữa chừng khi đăng ký). Đề xuất A01 (và `restoreSession`) đặt `signUpStep` bằng 3 để `RegisterFragment` mở thẳng ở bước điền hồ sơ. Xin xác nhận; cùng câu hỏi ở P2-A1a.
- **QA18. Tên hiển thị gõ ở bước 1.** A02 cho nhập "Tên hiển thị" ở bước 1, nhưng `startSignUp(String email, String password)` không nhận tên và `AuthViewModel` không có chỗ giữ tên tới bước điền hồ sơ, trong khi `completeProfile(User user, ...)` cần `User.displayName`. Issue để tên trong ô nhập của layout (ba khối cùng một layout nên View tự giữ chữ khi xoay) và Fragment đọc lúc dựng `User` ở P2-A2b. Xin xác nhận, hoặc thêm tham số hay LiveData cho tên.
- **QA21. Chỗ lệch giữa UC01 và tài liệu giao diện về đăng ký.** UC01 cho bước 1 chỉ nhập email (mật khẩu, họ tên, số điện thoại, khu vực nhập sau khi xác thực), còn A02 và `viewmodel.md` (`startSignUp(email, password)`) cho bước 1 gồm tên hiển thị, email, mật khẩu và xác nhận mật khẩu. Issue làm theo A02. Xin xác nhận và sửa UC01 cho khớp.
