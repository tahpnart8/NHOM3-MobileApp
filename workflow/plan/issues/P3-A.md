TITLE: feat(ui): bố cục ngang và báo lỗi mất mạng cho module tài khoản
LABELS: type:feature, area:ui, module:a, priority:p2
ASSIGNEES: loopy-tnw
PHASE: P3

Plan item: P3-A

### Bối cảnh
- PUBGApp (Pre-owned Users' Bargain Grounds) là ứng dụng Android mua bán đồ cũ, viết bằng Java 17 và XML Views với ViewBinding, dùng Firebase gói Spark (Authentication, Firestore) và Room.
- Module A (Tài khoản và thông báo) do Ngân (`loopy-tnw`) phụ trách: đăng ký, đăng nhập, hồ sơ, địa chỉ, cài đặt tài khoản, thông báo.
- Issue này hoàn thiện các màn A01 đến A06 và C06 theo yêu cầu môn học: bố cục ngang, giữ trạng thái khi xoay, báo lỗi rõ khi mất mạng.
- Đọc trước: `workflow/plan/phase-3-hoan-thien.md` (mục tiêu và danh sách kiểm tra cuối tệp), `workflow/docs/conventions/giao-dien.md` mục 6, `workflow/docs/conventions/android-java.md` mục "Tài nguyên và xoay màn hình" và "Luồng và lỗi".
- Đã có trên `main`: toàn bộ P2-A1a, P2-A1b, P2-A2a, P2-A2b, P2-A3, P2-A4, P2-A5a, P2-A5b, P2-A6 đã merge; mọi màn của module A chạy được ở bố cục dọc; thành phần chung `ui.common` (dải báo offline, trạng thái rỗng, đang tải) của P1-06.

### Mã tính năng
F-01, F-02, F-03 (yêu cầu môn học cắt ngang: xoay màn hình giữ trạng thái, báo lỗi khi mất mạng)

### Mã việc kế hoạch
P3-A

### Module
A. Tài khoản và thông báo (Ngân)

### STT trong bảng đặc tả
Không áp dụng (không thêm chức năng mới; hoàn thiện các màn của STT 1 đến 6, 9 đến 12, 35, 67, 104 đã làm ở P2-A1a đến P2-A6). Đây là yêu cầu môn học ghi ở `workflow/docs/product/features.md` mục "Yêu cầu môn học".

### Màn hình mockup
A01, A02, A03, A04, A05, A06, C06 (chín Fragment: `LoginFragment`, `RegisterFragment`, `ForgotPasswordFragment`, `PersonalInfoFragment`, `EditProfileFragment`, `AccountSettingsFragment`, `AddressListFragment`, `AddressEditFragment`, `NotificationListFragment`)

### Làm gì và vì sao
Giảng viên yêu cầu giao diện thích ứng khi xoay màn hình mà không mất trạng thái, và báo lỗi rõ ràng khi mất mạng. Sau issue này mỗi màn của module A có bố cục ngang đọc được và dùng được, xoay màn hình ở bất kỳ lúc nào (kể cả đang gõ, đang gửi, đang mở hộp thoại, đang ở màn chờ xác thực email) không mất chữ đang nhập, không tải lại dữ liệu đã có, không mất vị trí cuộn, không gửi yêu cầu lần hai. Khi mất mạng, các nút ghi của module A bị khóa hoặc báo "Mất kết nối mạng, vui lòng kiểm tra lại đường truyền" sau tối đa 15 giây, các màn đọc dữ liệu hiện câu báo lỗi tiếng Việt thay vì trống im lặng. Các màn của module A không nằm trong danh sách "xem lại khi offline" của `phase-3-hoan-thien.md` và `room-schema.md` không có bảng cho chúng, nên issue này không thêm bộ nhớ đệm Room nào. Issue này cũng kiểm lại hai mục của danh sách P3 thuộc module A: đăng xuất thì Room bị xóa sạch, và tài khoản thường không đọc được `users/{uid khác}/private/identity`. Issue này phục vụ các dòng barem R-Chapter 5 (tài nguyên thay thế cho layout thích ứng: thư mục `res/layout-land`), R-Chapter 1, 2, 6, 7 (vòng đời Activity và Fragment, giữ trạng thái khi xoay; kiểm tra kết nối mạng bằng `NetworkMonitor`; async qua `DataCallback`) và C-GUI (giao diện nhất quán và dùng được ở cả hai hướng).

### Tiêu chí nghiệm thu
- [ ] Mỗi trong chín màn trên có một file bố cục ngang cùng tên trong `res/layout-land/`, với mọi view có cùng `id` và cùng kiểu với bố cục dọc (để ViewBinding sinh cùng một lớp binding và Fragment không phải thêm nhánh `if`). Màn nào có nhiều ô nhập (A02, A05, A06) cuộn được trong chế độ ngang, không bị che ô nhập bởi bàn phím.
- [ ] Trên máy ảo, xoay qua lại từng màn trong chín màn: không tải lại dữ liệu (mạng và Firestore không bị gọi lại; kiểm bằng Logcat hoặc bằng cách bật chế độ máy bay sau khi màn đã tải rồi xoay), không mất chữ đang nhập, không mất vị trí cuộn của danh sách địa chỉ và danh sách thông báo.
- [ ] Trạng thái riêng của từng màn được giữ qua lần xoay: A01 chữ trong hai ô và trạng thái đang gửi; A02 bước đang đứng (`signUpStep`: nhập tài khoản, chờ xác thực email, hồ sơ), chữ ở cả hai bước, email hiện ở màn chờ, danh mục quan tâm đã chọn, ô đồng ý điều khoản, ngày sinh và giới tính đã chọn, chế độ hoàn tất hồ sơ (`needsProfile`); A03 chữ email; A05 chữ đang sửa và cờ "có thay đổi chưa lưu"; khối định danh, đổi mật khẩu và công tắc thông báo trong `AccountSettingsFragment`; A06 chữ trong form và công tắc mặc định. Dữ liệu tải về nằm trong ViewModel; chữ đang gõ giữ bằng trạng thái lưu của View hoặc của Fragment.
- [ ] Hộp thoại có ô nhập hoặc chọn (xác nhận xóa tài khoản có ô mật khẩu, hộp chọn ngày sinh) vẫn còn và giữ nội dung sau khi xoay, không crash, không rò cửa sổ (`WindowLeaked` trong Logcat). Hộp thoại xác nhận chỉ có hai nút (đăng xuất, xóa địa chỉ, thoát khi chưa lưu) cũng không crash và không để lại hai hộp thoại chồng nhau; chấp nhận việc chúng đóng lại khi xoay nếu không thể giữ.
- [ ] Mất mạng (bật chế độ máy bay): nút đăng nhập, đăng nhập Google, gửi liên kết, "Tiếp tục" và "Gửi lại liên kết" của đăng ký, hoàn tất đăng ký, lưu hồ sơ, đổi ảnh, lưu định danh, lưu địa chỉ, xóa địa chỉ, đổi mật khẩu, công tắc thông báo, xóa tài khoản đều khóa hoặc báo "Mất kết nối mạng, vui lòng kiểm tra lại đường truyền" (không treo quá 15 giây). Dải báo offline hiện ở các màn trong `MainActivity` dùng thành phần chung của P1-06 (không tự viết lại); `AuthActivity` báo bằng câu lỗi của màn.
- [ ] Các màn đọc dữ liệu (A04 và A05 tải hồ sơ, khối định danh, A06 tải địa chỉ, C06 tải thông báo) hiện câu lỗi tiếng Việt kèm nút "Thử lại" khi mất mạng hoặc khi Rules từ chối, không để màn trống im lặng và không giả dữ liệu.
- [ ] Rà mọi lời gọi mạng của module A (`login`, `loginWithGoogle`, `restoreSession`, `resetPassword`, `startSignUp`, `resendVerificationLink`, `checkEmailVerified`, `completeProfile`, `loadCategories`, `changePassword`, `logout` của `AuthViewModel`; `loadAddresses`, `updateProfile`, `changeAvatar`, `loadIdentity`, `saveIdentity`, `saveAddress`, `deleteAddress`, `updateNotificationPrefs`, `deactivateAccount`, `deactivateWithGoogle` của `ProfileViewModel`; `load` và `markRead` của `NotificationViewModel`): mỗi lỗi hiện một câu tiếng Việt, không có `catch` rỗng, không `printStackTrace`. Lập bảng kết quả trong Pull Request.
- [ ] Đăng xuất thì Room bị xóa sạch: xem tin rồi đăng xuất và xác nhận bảng `listing_cache` trống. Việc này chạy qua `AuthViewModel.logout` đã làm ở P2-A5a; nếu sai thì sửa ở đúng phương thức đó.
- [ ] Tài khoản thường không đọc được `users/{uid khác}/private/identity`: thử bằng tài khoản thứ hai hoặc Rules Playground và ghi kết quả vào Pull Request (Security Rules là việc của P3-R, Phát; ở đây chỉ ghi kết quả quan sát được).
- [ ] Bố cục ngang dùng chuỗi, màu, kiểu, khoảng cách từ tài nguyên có sẵn (`strings_account.xml`, `colors.xml`, `themes.xml`, `dimens.xml`); không thêm màu hay kiểu mới vào các file dùng chung của Phát. Không ghi chữ cứng.
- [ ] Kiểm thử đơn vị: không có quy tắc mới. Việc rà test theo `trang-thai.md` thuộc P3-T; Ngân bổ sung test cho quy tắc của module A nếu P3-T phát hiện thiếu.
- [ ] `./gradlew assembleDebug testDebugUnitTest lintDebug` và `sh workflow/scripts/check-java-style.sh` đều qua; Pull Request ghi đúng lệnh đã chạy và kết quả thật (R1), kèm bảng "màn hình, xoay dọc sang ngang, xoay ngang sang dọc, máy bay: đạt hoặc không đạt" cho chín màn, ghi trên máy ảo nào.
- [ ] Pull Request liệt kê mọi lớp, phương thức, trường công khai mới kèm dòng tài liệu định nghĩa nó (R16). Issue này không cần tên công khai mới. Nếu phát hiện cần thêm trường trạng thái công khai trong ViewModel thì dừng và hỏi leader.

### Tài liệu phải đọc
- `AGENTS.md` mục 6: R3, R15, R16, R19, R20, R23, và mục 7 (quy ước về xoay màn hình).
- `workflow/plan/phase-3-hoan-thien.md` (mục tiêu và danh sách kiểm tra cuối tệp).
- `workflow/plan/so-huu.md` mục 2, 3, 6.
- `workflow/docs/product/features.md` mục "Yêu cầu môn học".
- `workflow/docs/ui/a-tai-khoan/README.md` (mọi màn A01 đến A06, mục "Trạng thái đặc biệt" của từng màn) và `workflow/docs/ui/c-ket-noi-giao-dich/README.md` mục C06.
- `workflow/docs/ui/README.md` mục "Điều hướng" và "Ngoài phạm vi đợt này" (bố cục ngang để giai đoạn code).
- `workflow/docs/conventions/android-java.md` mục "Tài nguyên và xoay màn hình" và "Luồng và lỗi"; `workflow/docs/conventions/giao-dien.md` mục 5 và 6.
- `workflow/docs/architecture/luong-xu-ly.md` quy ước chung ở đầu tệp (kiểm `NetworkMonitor.isOnline()`, thời hạn chờ 15 giây).
- `workflow/docs/architecture/overview.md` mục 10 (kịch bản chất lượng); `workflow/docs/data/room-schema.md` (Room xóa sạch khi đăng xuất).
- `workflow/docs/architecture/dac-ta-lop/util.md#networkmonitor`; `viewmodel.md#authviewmodel`, `#profileviewmodel`, `#notificationviewmodel`.
- `workflow/memory/code/README.md`, `workflow/memory/code/firebase.md`, và skill `pubg-code`, `pubg-ui`.
- `workflow/docs/product/barem-diem.md` mục 3 (dòng barem và kỹ thuật thầy dạy mà issue này phải nêu tên).

### Lớp, trường, trạng thái sẽ dùng
- Chỉ dùng tên đã có và đã dùng ở P2-A1a đến P2-A6; không có tên mới.
- `NetworkMonitor.getInstance()` và `isOnline()` (của Phát, chỉ gọi). Đặc tả không có getter cho LiveData `online`, nên issue này chỉ kiểm `isOnline()` ngay trước mỗi lần ghi.
- `AuthViewModel`, `ProfileViewModel`, `NotificationViewModel` và các LiveData đã ghi trong đặc tả, kể cả `loading`, `error`, `done` (chỉ sửa thân nếu phát hiện trạng thái bị mất khi xoay).
- Lớp giao diện: chín Fragment liệt kê ở trên, `AuthActivity`.
- `UserStatus`, `NotificationType`, `Gender` chỉ qua các màn đã làm.

### File được tạo hoặc sửa
Chép nguyên danh sách này vào kế hoạch của bạn (R19). Nếu khung P1-06 đặt tên file khác thì giữ tên đã có.
- [tạo] `PUBGApp/app/src/main/res/layout-land/fragment_account_login.xml`
- [tạo] `PUBGApp/app/src/main/res/layout-land/fragment_account_register.xml`
- [tạo] `PUBGApp/app/src/main/res/layout-land/fragment_account_forgot_password.xml`
- [tạo] `PUBGApp/app/src/main/res/layout-land/fragment_account_personal_info.xml`
- [tạo] `PUBGApp/app/src/main/res/layout-land/fragment_account_edit_profile.xml`
- [tạo] `PUBGApp/app/src/main/res/layout-land/fragment_account_settings.xml`
- [tạo] `PUBGApp/app/src/main/res/layout-land/fragment_account_address_list.xml`
- [tạo] `PUBGApp/app/src/main/res/layout-land/fragment_account_address_edit.xml`
- [tạo] `PUBGApp/app/src/main/res/layout-land/fragment_notification_list.xml`
- [sửa] `PUBGApp/app/src/main/java/com/nhom3/pubgapp/ui/account/*.java` và `ui/notification/NotificationListFragment.java` (chỉ chỗ trạng thái bị mất khi xoay, nút ghi chưa khóa khi offline, lỗi chưa hiện; không đổi chữ ký)
- [sửa] `PUBGApp/app/src/main/java/com/nhom3/pubgapp/viewmodel/AuthViewModel.java`, `ProfileViewModel.java`, `NotificationViewModel.java` (chỉ thân phương thức đã có, nếu phát hiện lỗi giữ trạng thái)
- [sửa] `PUBGApp/app/src/main/res/values/strings_account.xml` (chỉ câu báo lỗi còn thiếu)

### Các bước đề nghị
Bạn có thể làm theo thứ tự khác, nhưng không được đổi tên lớp, phương thức hay trường nào.
1. Đọc các tài liệu ở mục trên. Chạy từng màn ở bố cục dọc trên máy ảo và lập danh sách `id` của mọi view trong chín layout dọc, để layout ngang dùng đúng các `id` đó.
2. Tạo `layout-land` cho ba màn của `AuthActivity`: `fragment_account_login.xml`, `fragment_account_forgot_password.xml`, `fragment_account_register.xml` (ba khối: bước 1, chờ xác thực, bước 2; khối bước 2 cuộn được). Dùng `ConstraintLayout` và `ScrollView` khi nhiều ô nhập.
3. Tạo `layout-land` cho ba màn của `MainActivity` thuộc hồ sơ: `fragment_account_personal_info.xml`, `fragment_account_edit_profile.xml`, `fragment_account_settings.xml` (bốn khối: định danh, đổi mật khẩu, cài đặt thông báo, xóa tài khoản).
4. Tạo `layout-land` cho `fragment_account_address_list.xml`, `fragment_account_address_edit.xml`, `fragment_notification_list.xml` (có thể hai cột nếu danh sách đủ rộng, nhưng giữ `id` như bố cục dọc).
5. Rà trạng thái: với từng Fragment, xoay máy ở mọi trạng thái nêu trong tiêu chí; nơi nào mất chữ, mất bước, hay tải lại thì chuyển trạng thái vào ViewModel hoặc vào trạng thái lưu của View. Không đổi chữ ký, không thêm phương thức công khai (R20); cần thêm trường công khai thì dừng và hỏi leader.
6. Rà hộp thoại: hộp thoại có ô mật khẩu (xóa tài khoản) và hộp chọn ngày sinh giữ nội dung qua lần xoay; hộp thoại hai nút không crash, không chồng nhau.
7. Rà mất mạng: với từng nút ghi, kiểm `NetworkMonitor.getInstance().isOnline()` trước khi gọi và khóa nút hoặc báo lỗi khi offline; với từng màn đọc, hiện `view_error_state.xml` có nút "Thử lại".
8. Lập bảng lời gọi mạng của module A và câu lỗi tiếng Việt tương ứng; sửa nơi còn `catch` rỗng hoặc `printStackTrace`. Thêm câu còn thiếu vào `strings_account.xml`.
9. Kiểm hai mục của danh sách P3: đăng xuất thì `listing_cache` trống; tài khoản thứ hai không đọc được định danh của người khác. Ghi kết quả.
10. Lập bảng "màn hình, xoay dọc sang ngang, xoay ngang sang dọc, máy bay: đạt hoặc không đạt" cho chín màn trên một máy ảo, chụp ảnh màn hình ngang đính vào Pull Request.
11. Chạy các lệnh ở mục "Tự kiểm trước khi mở Pull Request" và ghi kết quả thật.

### File dùng chung cần thêm dòng
Không có. Nếu cần màu, kiểu hoặc kích thước mới thì mở issue `area:ui` cho Phát, không tự sửa `colors.xml`, `themes.xml`, `dimens.xml`.

### Cấm đụng
- `AndroidManifest.xml` (không khai `android:screenOrientation` hay `configChanges` để "chống xoay"; xoay phải thực sự hoạt động), `nav_main.xml`, `nav_admin.xml`, `themes.xml`, `colors.xml`, `dimens.xml`, `strings_common.xml`, `AppDatabase`, `Converters`, mọi file Gradle, mọi file trong `firebase/`.
- Thành phần chung `ui.common` (dải báo offline, trạng thái rỗng, đang tải) của Phát: chỉ dùng.
- `MainActivity`, `NetworkMonitor`, `SessionManager` (của Phát).
- Mọi file bố cục, Fragment, ViewModel của module B, C, D, E.
- Lớp `model` và `model.enums`: không sửa. Không đổi chữ ký, không thêm phương thức công khai (R20).

### Phụ thuộc
P2-A1a, P2-A1b, P2-A2a, P2-A2b, P2-A3, P2-A4, P2-A5a, P2-A5b, P2-A6 (mọi màn phải tồn tại và chạy được). P1-06 (dải báo offline và thành phần chung). Việc rà bảo mật Rules cùng Phát ở P3-R.

### Ngoài phạm vi
- Bố cục ngang và xoay của các module B, C, D, E (P3-B, P3-C, P3-D, P3-E).
- Bộ nhớ đệm Room cho màn của module A (không thuộc yêu cầu offline của đợt này).
- Ghi dữ liệu khi offline rồi gửi sau: không hỗ trợ (`luong-xu-ly.md` mục 3, bước 6).
- Viết hoặc sửa Security Rules (P3-R, Phát).
- Chế độ khách, thanh trên có nút "Đăng nhập" của `MainActivity` (Phát).
- Layout riêng cho máy tính bảng (`sw600dp`): không yêu cầu.

### Tài liệu cần cập nhật
Không có.

### Tự kiểm trước khi mở Pull Request
- [ ] Chín file `layout-land` có đủ, mỗi file dùng cùng `id` với bản dọc (build không báo thiếu binding).
- [ ] Mỗi màn: xoay dọc sang ngang rồi ngang sang dọc, chữ đang gõ và vị trí cuộn còn nguyên.
- [ ] A02: xoay ở bước 1, ở màn chờ xác thực và ở bước 2: bước đang đứng và chữ còn nguyên, không gửi thư lần hai.
- [ ] Hộp thoại mật khẩu của xóa tài khoản: xoay khi đang mở, chữ còn nguyên, không crash.
- [ ] Bật chế độ máy bay: mỗi nút ghi của module A khóa hoặc báo "Mất kết nối mạng, vui lòng kiểm tra lại đường truyền"; mỗi màn đọc hiện lỗi kèm nút "Thử lại".
- [ ] Xem tin rồi đăng xuất: bảng `listing_cache` trống.
- [ ] Tài khoản thứ hai đọc `users/{uid khác}/private/identity`: bị từ chối (hoặc ghi rõ chưa kiểm được vì Rules chưa nạp).
- [ ] `cd PUBGApp && ./gradlew assembleDebug testDebugUnitTest lintDebug` kết thúc `BUILD SUCCESSFUL`.
- [ ] `sh workflow/scripts/check-java-style.sh` không báo lỗi.
- [ ] Danh sách lớp, phương thức, trường công khai mới đã ghi vào Pull Request kèm dòng tài liệu (R16), hoặc ghi rõ là không có.

### Quy mô
- [x] Đây là một lát cắt dọc của một module, khoảng nửa ngày đến một ngày, một Pull Request.

### Câu hỏi cho leader
- **QA1. Tên getter, và cách xóa `error` và `done`.** `viewmodel.md` ghi mọi ViewModel có `loading`, `error`, `done` và getter công khai cho mọi LiveData, nhưng không chép tên getter. Issue này dùng quy ước JavaBean `get` cộng tên thuộc tính (`getLoading()`, `getError()`, `getDone()`, và tương tự cho các LiveData khác). Cột Ý nghĩa cũng nói màn hình "xóa `error` sau khi đã hiện" và "đặt `done` về false sau khi dùng", nhưng LiveData là `private` và đặc tả không có phương thức công khai nào làm việc đó. Cho tới khi có trả lời, Fragment chỉ quan sát và hiện, không thêm phương thức công khai nào vào ViewModel (R20); hệ quả là sau khi xoay màn hình câu lỗi cũ có thể hiện lại. Xin ghi quy ước getter và cách xóa vào `viewmodel.md`.
