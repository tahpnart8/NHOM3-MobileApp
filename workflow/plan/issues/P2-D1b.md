TITLE: feat(profile): biểu mẫu gửi yêu cầu xác thực tài khoản
LABELS: type:feature, area:profile, module:d, priority:p1
ASSIGNEES: PmSubin
PHASE: P2

Plan item: P2-D1b

### Bối cảnh
- PUBGApp là ứng dụng Android mua bán đồ cũ kết hợp mạng xã hội, viết bằng Java 17, XML Views với ViewBinding, Firebase Authentication và Cloud Firestore gói Spark (không Cloud Storage, không Kotlin; ảnh nén trên máy rồi lưu Base64 trong Firestore).
- Module D "Hồ sơ và tin cậy" do Phúc (`PmSubin`) làm: hồ sơ cá nhân và công khai, uy tín, theo dõi, gửi yêu cầu xác thực tài khoản, báo cáo, khiếu nại, và phía admin xử lý khiếu nại.
- Issue này thêm vào màn D01 `MyProfileFragment` biểu mẫu gửi yêu cầu xác thực tài khoản (số CCCD, ảnh giấy tờ, ảnh chân dung cầm giấy tờ), phần hiện lý do từ chối và số lần gửi lại còn lại, và toàn bộ logic gửi, gửi lại tối đa 3 lần. Xác thực là cửa mở quyền mua bán: chưa được admin duyệt thì không đăng tin, gửi đề nghị, tạo giao dịch được.
- Đọc trước ba tài liệu: `workflow/docs/product/use-case/uc-d-ho-so-tin-cay.md` mục UC31; `workflow/docs/architecture/trang-thai.md` mục 8; `workflow/docs/data/security-rules.md` mục 6 (sellerVerifications) và mục 1 (dòng "Xin xác thực").
- Đã có trên `main`: khung P1 (mọi ViewModel có `loading`, `error`, `done` và getter công khai), `MediaRepository.saveImage` và `MediaRepository.delete` làm thật, và P2-D1a (màn D01 đã hiện trạng thái xác thực, `ProfileViewModel.loadProfile`, `profile`).

### Mã tính năng
F-11 (Hồ sơ công khai, uy tín, theo dõi). STT 73 thuộc F-14 ở phía admin; phía người dùng (gửi yêu cầu) do module D làm cùng F-11, theo `workflow/docs/product/features.md` (dòng ghi chú dưới bảng) và `workflow/plan/so-huu.md` mục 1.

### Mã việc kế hoạch
P2-D1, tách thành P2-D1a (màn D01) và P2-D1b (biểu mẫu gửi yêu cầu xác thực). Lý do tách: xem P2-D1a.

### Module
D. Hồ sơ và tin cậy (Phúc)

### STT trong bảng đặc tả
73 (phía người dùng). Use case: UC31.

### Màn hình mockup
D01 (`MyProfileFragment`). Biểu mẫu chưa có ảnh; làm theo mục "Thành phần chính" (dòng "Biểu mẫu gửi yêu cầu xác thực tài khoản"), "Hành động và điều hướng", "Trạng thái đặc biệt" và "Khác với ảnh" của D01. Biểu mẫu nằm ngay trong `MyProfileFragment`, không phải màn hình mới (R22: `so-huu.md` mục 3 không có lớp nào cho nó).

### Làm gì và vì sao
Người dùng chưa được duyệt bấm nút "Đăng ký xác thực" ở khối xác thực trên D01 (hoặc đến D01 từ nút cùng tên ở C02, C03 bằng `action_global_my_profile`), thấy biểu mẫu gồm ô nhập số CCCD (12 chữ số), ô tải ảnh giấy tờ, ô tải ảnh chân dung cầm giấy tờ và nút "Gửi yêu cầu". Thiếu một trong ba thứ thì báo lỗi và giữ nguyên biểu mẫu; đủ thì hai ảnh được nén trên máy và lưu thành hai tài liệu `images` (mục đích `VERIFICATION`), rồi trong một lô ghi: tạo hoặc cập nhật `sellerVerifications/{uid}` ở PENDING kèm `nationalId`, `imageIds`, `resubmitCount`, và đặt `users/{uid}.verificationStatus` thành PENDING. Khối xác thực chuyển sang "Đang chờ duyệt". Nếu admin từ chối, khối đọc yêu cầu của chính mình (`ProfileViewModel.loadVerification`, kết quả ở `verification`) và hiện lý do admin ghi cùng nút "Gửi lại yêu cầu"; người dùng được sửa và gửi lại tối đa 3 lần (`SellerVerification.MAX_RESUBMIT`), hết lượt thì nút bị khóa và khối báo cần liên hệ admin. Chỉ tài khoản đã xác thực email mới gửi được (Security Rules đòi). Hệ thống không kiểm số CCCD đã dùng cho tài khoản khác (UC31 BR4): admin đối chiếu bằng mắt khi duyệt ở E03. Số CCCD không được mã hóa thêm ở phía máy chủ vì gói Spark không có Cloud Functions; chỉ chủ tài khoản và admin đọc được nhờ Security Rules (UC31 NFR1). Luồng này là UC31 và `trang-thai.md` mục 8; admin duyệt ở E03 (module E). Issue này phục vụ các dòng barem R-Chapter 4 (input control: ô nhập chỉ nhận chữ số, giới hạn 12 ký tự, nút gửi bật hay khóa theo dữ liệu), R-Chapter 1, 2, 6, 7 (implicit Intent để chọn ảnh giấy tờ và ảnh chân dung từ thư viện, async qua `DataCallback`) và R-Chapter 3 (unit test JUnit cho hàm kiểm dữ liệu biểu mẫu, và `SellerVerification.canResubmit` là quy tắc giới hạn gửi lại có test ở P1).

### Tiêu chí nghiệm thu
- [ ] Gửi lần đầu với Firebase thật (tài khoản đã xác thực email, `verificationStatus` NONE): nhập số CCCD 12 chữ số, chọn hai ảnh, bấm "Gửi yêu cầu". Kết quả đúng một lô ghi: (1) hai tài liệu `images` mới có `ownerId` là mình và `purpose` VERIFICATION; (2) `sellerVerifications/{uid}` có `userId`, `nationalId`, `imageIds` (đúng hai mã, phần tử đầu là ảnh giấy tờ, phần tử sau là ảnh chân dung cầm giấy tờ), `status` PENDING, `note` rỗng, `reviewedBy` rỗng, `resubmitCount` bằng 0; (3) `users/{uid}.verificationStatus` thành PENDING. Khối xác thực đổi thành "Đang chờ duyệt", không còn nút gửi, và hiện thông báo gửi thành công (theo `done`).
- [ ] Kiểm dữ liệu trước khi gọi mạng, mỗi lỗi một câu tiếng Việt riêng và giữ nguyên biểu mẫu: thiếu số CCCD; số CCCD không đủ 12 chữ số hoặc có ký tự khác chữ số; chưa chọn ảnh giấy tờ; chưa chọn ảnh chân dung cầm giấy tờ; tệp được chọn không phải ảnh (chỉ nhận ảnh, `ContentResolver.getType(Uri)` không bắt đầu bằng `image/`).
- [ ] Ảnh vượt dung lượng được nén bởi `MediaRepository.saveImage` (không tự nén lại); ảnh không nén được thì báo "không dùng được ảnh này" và giữ biểu mẫu.
- [ ] Chưa xác thực email: không gửi được, màn báo câu tiếng Việt yêu cầu xác thực email trước (kiểm `AuthRepository.isEmailVerified()` sau `AuthRepository.reloadUser`, không để Rules từ chối rồi mới báo).
- [ ] Trạng thái PENDING hoặc APPROVED: không có nút gửi, repository từ chối nếu vẫn bị gọi (câu "đang chờ duyệt" hoặc "đã xác thực").
- [ ] Bị từ chối (REJECTED): `ProfileViewModel.loadVerification()` đọc `sellerVerifications/{uid}` qua `UserRepository.getVerification`; khối hiện lý do admin đã ghi (`SellerVerification.note`) và, khi `SellerVerification.canResubmit()` là true, nút "Gửi lại yêu cầu" cùng số lần gửi lại còn lại (xem Câu hỏi 2); gửi lại với Firebase thật cho `sellerVerifications/{uid}` về PENDING với `nationalId` và `imageIds` mới, `note` rỗng, `reviewedBy` rỗng, `resubmitCount` tăng đúng 1, `createdAt` không đổi, và `users/{uid}.verificationStatus` về PENDING trong cùng lô.
- [ ] Hết lượt: sau lần gửi lại thứ 3 bị từ chối (`resubmitCount` bằng 3) khối báo đã hết số lần gửi lại và cần liên hệ admin, nút gửi lại biến mất hoặc bị khóa; `UserRepository.requestVerification` từ chối nếu vẫn bị gọi.
- [ ] Mất mạng hoặc quá 15 giây: báo "Không có kết nối" bằng tiếng Việt (qua `error`), giữ nguyên số CCCD và hai ảnh đã chọn, không treo màn, không để lô ghi chờ vô hạn; nút gửi khóa khi `NetworkMonitor.getInstance().isOnline()` là false hoặc `loading` là true.
- [ ] Nếu một ảnh lỗi hoặc lô ghi lỗi sau khi đã lưu ảnh thì xóa các ảnh đã lưu của lần thử đó bằng `MediaRepository.delete`, biểu mẫu giữ nguyên dữ liệu đã nhập.
- [ ] Xoay màn hình hoặc thoát ứng dụng tạm thời giữa lúc đang điền: số CCCD đã gõ và hai ảnh đã chọn còn nguyên (UC31 NFR3). Ô nhập có `android:id` để Android tự giữ chữ; danh sách `Uri` hai ảnh giữ bằng `onSaveInstanceState` của Fragment.
- [ ] Mọi chữ người dùng đọc là "xác thực tài khoản"; nhãn đã duyệt là "Tài khoản đã xác thực"; không có chữ "xác minh người bán" ở bất kỳ đâu. Số CCCD không hiện ở nơi nào công khai (chỉ trong biểu mẫu của chính chủ).
- [ ] Trạng thái xác thực chỉ đổi qua `UserRepository.requestVerification` (NONE hoặc REJECTED sang PENDING); không đoạn nào ghi thẳng `verificationStatus` từ Fragment hay ViewModel (R18).
- [ ] Unit test `ProfileViewModelTest`: hàm kiểm dữ liệu biểu mẫu (không public, tách riêng) với các ca số CCCD rỗng, 11 chữ số, 13 chữ số, có chữ cái, đúng 12 chữ số; thiếu ảnh, đủ hai ảnh; và hàm quyết định có cho gửi hay không theo cặp (`VerificationStatus`, đã xác thực email): NONE và đã xác thực cho phép; REJECTED và đã xác thực cho phép; PENDING, APPROVED, hoặc chưa xác thực email thì không. PR ghi tên test đã có ở P1 cho `SellerVerification.canResubmit`.
- [ ] Layout chỉ dùng token của `giao-dien.md`; id view theo `<loại><Việc>` kiểu lowerCamelCase (ví dụ `editNationalId`, `buttonSubmitVerification`); mọi chữ là string resource trong `strings_profile.xml`, khóa theo `<module>_<màn>_<việc>` (ví dụ `profile_my_profile_verify_submit`).
- [ ] `cd PUBGApp && ./gradlew assembleDebug testDebugUnitTest lintDebug` và `sh workflow/scripts/check-java-style.sh` đều qua; PR ghi đúng lệnh và kết quả thật (R1).
- [ ] PR liệt kê mọi lớp, phương thức, trường công khai mới kèm dòng tài liệu định nghĩa (R16); nếu không có thì ghi "không có".

### Tài liệu phải đọc
- `AGENTS.md` mục 6: R12, R16, R17, R18, R20, R21, R23.
- `workflow/plan/so-huu.md` mục 3 (D01), mục 4 (`action_global_my_profile`), mục 5 (dòng `UserRepository` `requestVerification` và `ProfileViewModel` `requestVerification` của Phúc), mục 6.
- `workflow/docs/product/barem-diem.md` (dòng barem mà issue này phục vụ).
- `workflow/docs/product/dac-ta-chuc-nang.md` dòng STT 73.
- `workflow/docs/product/use-case/uc-d-ho-so-tin-cay.md` mục UC31; `uc-e-quan-tri.md` mục UC36 (phía admin, để biết admin đọc gì).
- `workflow/docs/ui/d-ho-so-tin-cay/README.md` mục D01.
- `workflow/docs/architecture/dac-ta-lop/viewmodel.md#profileviewmodel`
- `workflow/docs/architecture/dac-ta-lop/data.md#userrepository`, `#mediarepository`, `#authrepository`
- `workflow/docs/architecture/dac-ta-lop/model.md#sellerverification`, `#user`
- `workflow/docs/architecture/dac-ta-lop/enums.md#verificationstatus`, `#imagepurpose`
- `workflow/docs/architecture/trang-thai.md` mục 8 (xác thực tài khoản).
- `workflow/docs/data/firestore-schema.md#sellerverifications`, `#users`, `#images`
- `workflow/docs/data/security-rules.md` mục 6 (sellerVerifications), mục 1 (dòng "Xin xác thực"), mục 24 (images) và bảng Giới hạn (dòng "Kiểm từng phần tử của mảng mã ảnh", dòng `email_verified`).
- `workflow/docs/architecture/overview.md` mục 8 (ghi nhiều tài liệu cùng lúc, offline, thời hạn chờ ghi 15 giây).
- `workflow/memory/code/README.md`, `workflow/memory/code/firebase.md`, `workflow/docs/conventions/android-java.md`, skill `pubg-code` và `pubg-ui`.

### Lớp, trường, trạng thái sẽ dùng
Tên đúng như tài liệu; không đặt tên mới.

- `MyProfileFragment` (D01).
- `ProfileViewModel`: thuộc tính `profile` (`LiveData<User>`), `verification` (`LiveData<SellerVerification>`), `loading`, `error`, `done` (kèm getter công khai, chép đúng chữ trong `ProfileViewModel.java` do P1 tạo); phương thức được thay thân: `public void requestVerification(String nationalId, List<Uri> imageUris)`, `public void loadVerification()` (chủ của `loadVerification`: xem Câu hỏi 1); dùng lại `loadProfile(String uid)` của P2-D1a.
- `UserRepository`: `public void requestVerification(String uid, SellerVerification verification, DataCallback<Void> cb)`; `public void getVerification(String uid, DataCallback<SellerVerification> cb)` (chủ: xem Câu hỏi 1).
- `SellerVerification`: `userId`, `nationalId`, `imageIds`, `status`, `note`, `resubmitCount`, `reviewedBy`, `reviewedAt`; hằng `MAX_RESUBMIT` (bằng 3); `public boolean canResubmit()`.
- `VerificationStatus`: NONE, PENDING, APPROVED, REJECTED. `ImagePurpose.VERIFICATION`.
- `MediaRepository`: `public void saveImage(Uri uri, ImagePurpose purpose, DataCallback<String> cb)`, `public void delete(String id, DataCallback<Void> cb)` (của Phát, đã làm thật ở P1-04; chỉ gọi).
- `AuthRepository`: `public boolean isEmailVerified()`, `public void reloadUser(DataCallback<Boolean> cb)` (của Ngân, chỉ gọi).
- `User`: `verificationStatus`. `SessionManager`: `getInstance()`, `getCurrentUserId()`. `NetworkMonitor`: `getInstance()`, `isOnline()`. `DataCallback.NOT_READY`.
- Firestore: `sellerVerifications/{userId}`, `users/{uid}`, `images/{id}`.
- Hành động toàn cục: `action_global_my_profile` (đích D01, chỉ nhận).

### File được tạo hoặc sửa
- [sửa] PUBGApp/app/src/main/java/com/nhom3/pubgapp/ui/profile/MyProfileFragment.java
- [sửa] PUBGApp/app/src/main/res/layout/fragment_profile_my_profile.xml (chỉ vùng khối xác thực và biểu mẫu)
- [sửa] PUBGApp/app/src/main/res/values/strings_profile.xml
- [sửa] PUBGApp/app/src/main/java/com/nhom3/pubgapp/viewmodel/ProfileViewModel.java (chỉ thân `requestVerification` và `loadVerification`)
- [sửa] PUBGApp/app/src/main/java/com/nhom3/pubgapp/data/UserRepository.java (chỉ thân `requestVerification` và `getVerification`)
- [tạo] PUBGApp/app/src/test/java/com/nhom3/pubgapp/viewmodel/ProfileViewModelTest.java

Hai hàm kiểm tra (dữ liệu biểu mẫu, và quyết định cho gửi hay không) là hàm không công khai (package-private, tĩnh), tách riêng để test; tên do bạn đặt, không tính vào danh sách R16, ghi tên vào PR. Danh sách hai ảnh dùng `ArrayList<Uri>` thường, không thêm lớp mới. Phần sửa `getVerification` và `loadVerification` chỉ làm khi Câu hỏi 1 được trả lời là giao cho Phúc.

### Các bước đề nghị
Có thể làm theo thứ tự khác, nhưng không được đổi tên lớp, phương thức, trường nào.

1. Đọc `workflow/memory/code/firebase.md` và skill `pubg-code`, `pubg-ui`. Mở `MyProfileFragment.java` và layout do P2-D1a để lại; mở `ProfileViewModel.java` để chép đúng tên getter.
2. Thay thân `UserRepository.requestVerification(String uid, SellerVerification verification, DataCallback<Void> cb)`: kiểm `NetworkMonitor.getInstance().isOnline()`; đọc `sellerVerifications/{uid}` hiện có (chính chủ được đọc, Rules mục 6). Nếu chưa có tài liệu: đây là lần gửi đầu, `resubmitCount` bằng 0. Nếu đã có và `status` là PENDING hoặc APPROVED: `cb.onError` bằng câu tiếng Việt. Nếu đã có và REJECTED: gọi `SellerVerification.canResubmit()` trên bản đã đọc; false thì `cb.onError` "đã hết số lần gửi lại, hãy liên hệ admin"; true thì đặt `resubmitCount` bằng số cũ cộng 1.
3. Cũng trong `requestVerification`, ghi bằng một `WriteBatch`: lần đầu thì tạo `sellerVerifications/{uid}` (`userId`, `nationalId`, `imageIds`, `status` PENDING, `note` rỗng, `reviewedBy` rỗng, `resubmitCount` 0, `createdAt` và `updatedAt` bằng `FieldValue.serverTimestamp()`); gửi lại thì chỉ cập nhật các trường Rules mục 6 cho phép (`nationalId`, `imageIds`, `status`, `note`, `resubmitCount`, `reviewedBy`, `reviewedAt`, `updatedAt`), không đổi `createdAt`. Cùng lô cập nhật `users/{uid}` chỉ hai trường `verificationStatus` (PENDING) và `updatedAt`. Chờ tối đa 15 giây, quá hạn thì `cb.onError("Không có kết nối")`. Lỗi Firebase đi qua `fail(DataCallback, Exception)` của `BaseRepository`.
4. (Khi Câu hỏi 1 trả lời "giao cho Phúc".) Thay thân `UserRepository.getVerification(String uid, DataCallback<SellerVerification> cb)`: đọc `sellerVerifications/{uid}` của chính mình; chưa có tài liệu thì trả `null` bằng `cb.onSuccess(null)`. Thay thân `ProfileViewModel.loadVerification()`: gọi `getVerification` với `SessionManager.getInstance().getCurrentUserId()` và đưa kết quả vào `verification`.
5. Thay thân `ProfileViewModel.requestVerification(String nationalId, List<Uri> imageUris)`: (a) kiểm dữ liệu bằng hàm kiểm riêng (CCCD đúng 12 chữ số, đúng hai `Uri`, thứ tự là ảnh giấy tờ rồi ảnh chân dung); (b) kiểm `AuthRepository.isEmailVerified()` và trạng thái hiện tại trong `profile` bằng hàm quyết định riêng; (c) bật `loading`, lưu lần lượt hai ảnh bằng `MediaRepository.saveImage(uri, ImagePurpose.VERIFICATION, cb)` để có hai mã ảnh theo đúng thứ tự; (d) dựng một `SellerVerification` (đặt `userId` là `SessionManager.getInstance().getCurrentUserId()`, `nationalId`, `imageIds`, `status` là `VerificationStatus.PENDING`) bằng các setter theo quy ước của lớp mô hình; (e) gọi `UserRepository.requestVerification(uid, verification, cb)`; (f) thành công thì bật `done` và đọc lại hồ sơ bằng `loadProfile(uid)` để khối đổi sang "Đang chờ duyệt"; (g) lỗi ở bước (c) hoặc (e) thì gọi `MediaRepository.delete(id, cb)` cho các ảnh đã lưu của lần này rồi đưa câu lỗi vào `error`.
6. Viết phần biểu mẫu trong `fragment_profile_my_profile.xml`: một khối ẩn mặc định, hiện khi bấm "Đăng ký xác thực" hoặc "Gửi lại yêu cầu", gồm ô nhập số CCCD (chỉ nhận chữ số, tối đa 12 ký tự), hai ô chọn ảnh có hình xem trước nhỏ, nút "Gửi yêu cầu", dòng nhắc "Chỉ nhận ảnh; ảnh được nén trên máy". Dùng token, không viết thẳng giá trị.
7. Trong `MyProfileFragment`, chọn ảnh bằng implicit Intent hệ thống qua Activity Result API của AndroidX (`registerForActivityResult` với bộ chọn ảnh hệ thống, ví dụ `ActivityResultContracts.PickVisualMedia`; kiểm tên lớp và cách dùng bằng tài liệu chính thức hoặc MCP `context7` trước khi viết, vì tên lớp này chưa được xác minh, R2). Kết quả trả về là `Uri`; dùng lớp ẩn danh, không lambda. Không thêm thư viện (R5). Chụp ảnh bằng máy ảnh chỉ làm khi Câu hỏi 4 được trả lời.
8. Giữ dữ liệu khi xoay: ô nhập có `android:id`; trong `onSaveInstanceState` lưu hai `Uri` đã chọn, trong `onViewCreated` đọc lại và hiện xem trước.
9. Điều khiển khối xác thực theo trạng thái: NONE hiện nút "Đăng ký xác thực"; PENDING hiện "Đang chờ duyệt" không nút; REJECTED gọi `loadVerification()`, hiện lý do `note` và nút "Gửi lại yêu cầu" nếu `canResubmit()` là true, còn không thì chữ "đã hết số lần gửi lại, hãy liên hệ admin"; APPROVED hiện huy hiệu "Tài khoản đã xác thực".
10. Khóa nút "Gửi yêu cầu" khi `NetworkMonitor.getInstance().isOnline()` là false hoặc `loading` đang true (chống bấm hai lần).
11. Viết `ProfileViewModelTest` cho hàm kiểm dữ liệu và hàm quyết định (các ca ở mục tiêu chí). Chạy được trên JVM, không cần Firebase.
12. Thêm mọi chuỗi vào `strings_profile.xml` (khóa `<module>_<màn>_<việc>`), tiếng Việt có dấu, không emoji; dùng "xác thực tài khoản". Chạy các lệnh ở mục "Tự kiểm trước khi mở Pull Request" rồi mở PR.

### File dùng chung cần thêm dòng
Ghi vào mô tả Pull Request để Phát gộp (`firebase/` là file của Phát, `so-huu.md` mục 6): luật gửi lần đầu và gửi lại của `sellerVerifications` ở `security-rules.md` mục 6 và dòng "Xin xác thực" của mục 1.

### Cấm đụng
- Leader-only: `AndroidManifest.xml`, `nav_main.xml`, `nav_admin.xml`, `themes.xml`, `colors.xml`, `dimens.xml`, `strings_common.xml`, `AppDatabase`, `Converters`, mọi file Gradle, `firebase/`.
- Module A: `AuthRepository.isEmailVerified`, `AuthRepository.reloadUser` (chỉ gọi; có thể còn `NOT_READY`); `UserRepository.getProfile` và các phương thức khác của Ngân.
- Module E: `AdminRepository.getVerificationRequests`, `AdminRepository.reviewVerification`, `AdminUserViewModel`, màn E03 (phía admin của STT 73). Phát: `MediaRepository`, `NotificationRepository.create` (chỉ gọi).
- Mọi lớp trong `model`, `model.enums`: không thêm trường hay phương thức vào `SellerVerification`, `User`.

### Phụ thuộc
P2-D1a (màn D01, `loadProfile`). Cần có trên `main`: `MediaRepository.saveImage`, `MediaRepository.delete` (P1-04); `AuthRepository.isEmailVerified`, `reloadUser` (P2-A1 hoặc P2-A2). Để thử luồng duyệt cần màn E03 (P2-E2); trong lúc chờ sửa tay `sellerVerifications/{uid}` và `users/{uid}.verificationStatus` trong Firebase Console.

### Ngoài phạm vi
- Màn D01 (số liệu, lối tắt, đăng xuất): P2-D1a.
- Phía admin: danh sách yêu cầu, xem số CCCD và hai ảnh, duyệt hoặc từ chối kèm lý do, thông báo kết quả: module E (P2-E2).
- Kiểm số CCCD đã dùng cho tài khoản khác: bỏ khỏi đặc tả (UC31 BR4 nay ghi hệ thống không kiểm, admin đối chiếu bằng mắt), không làm.
- Chặn đăng tin, gửi đề nghị, tạo giao dịch khi chưa xác thực (`User.canSell`, `User.canBuy`, Rules `verified()`): module B và module C.
- Sửa số CCCD trong `users/{uid}/private/identity` (STT 9): module A.
- Xóa ảnh xác thực cũ khi gửi lại: không làm, không có trong đặc tả.
- Bố cục ngang: P3-D.

### Tài liệu cần cập nhật
Không có, trừ khi phải đổi hành vi. Nếu Câu hỏi 2 được trả lời bằng đổi đặc tả lớp, Phát cập nhật `workflow/docs/architecture/dac-ta-lop/model.md#sellerverification`.

### Tự kiểm trước khi mở Pull Request
- [ ] `cd PUBGApp && ./gradlew assembleDebug testDebugUnitTest lintDebug` kết thúc `BUILD SUCCESSFUL`, trong đó `ProfileViewModelTest` chạy và qua.
- [ ] `sh workflow/scripts/check-java-style.sh` không báo lỗi nào.
- [ ] Tài khoản đã xác thực email, chưa được duyệt: mở D01, bấm "Đăng ký xác thực", để trống rồi bấm "Gửi yêu cầu": thấy câu lỗi tiếng Việt, biểu mẫu còn nguyên. Nhập số CCCD 11 chữ số: thấy lỗi riêng. Nhập 12 chữ số và chọn đủ hai ảnh rồi gửi: khối đổi thành "Đang chờ duyệt".
- [ ] Firebase Console sau khi gửi: có `sellerVerifications/{uid}` với `status` PENDING, `nationalId`, `imageIds` hai phần tử, `resubmitCount` 0; `users/{uid}.verificationStatus` là PENDING; hai tài liệu `images` có `purpose` VERIFICATION.
- [ ] Trong Firebase Console sửa `sellerVerifications/{uid}` và `users/{uid}` thành REJECTED kèm `note`: mở lại D01 thấy lý do và nút gửi lại; gửi lại: `resubmitCount` thành 1, `createdAt` không đổi. Đặt `resubmitCount` bằng 3 và REJECTED: nút gửi lại không còn, màn báo hết lượt.
- [ ] Tài khoản chưa xác thực email: gửi bị chặn với câu tiếng Việt nói rõ cần xác thực email.
- [ ] Bật chế độ máy bay: nút gửi bị khóa; tắt giữa lúc đang gửi thì sau 15 giây báo "Không có kết nối", số CCCD và hai ảnh vẫn còn.
- [ ] Xoay màn hình khi đã gõ số CCCD và chọn một ảnh: chữ và ảnh xem trước còn nguyên.
- [ ] Không còn chữ "xác minh người bán" trong `strings_profile.xml`; mọi chữ có dấu.
- [ ] PR liệt kê mọi lớp, phương thức, trường công khai mới kèm dòng tài liệu định nghĩa (R16), hoặc ghi "không có"; ghi tên hai hàm kiểm tra không công khai.

### Quy mô
- [x] Đây là một lát cắt dọc của một module, khoảng nửa ngày đến một ngày, một Pull Request.

### Câu hỏi cho leader
1. `UserRepository.getVerification(String uid, DataCallback<SellerVerification> cb)` và `ProfileViewModel.loadVerification()` đã có trong đặc tả, nhưng `so-huu.md` mục 5 chưa ghi chủ (bảng chỉ có `requestVerification` của Phúc). Cả hai phục vụ đúng khối xác thực của D01. Đọc 1 (khuyến nghị): giao cho Phúc, làm trong P2-D1b. Đọc 2: giao người khác. Chờ trả lời; nếu chưa có thì phần lý do từ chối và số lần gửi lại hiện lỗi `NOT_READY` rõ ràng, phần gửi lần đầu vẫn làm được.
2. `SellerVerification.MAX_RESUBMIT` có truy cập `private` và không có getter, nên màn không tính được "còn mấy lần gửi lại" (`MAX_RESUBMIT` trừ `resubmitCount`); `canResubmit()` chỉ trả đúng hoặc sai. Đặc tả `ProfileViewModel.verification` lại nói D01 hiện "số lần gửi lại còn lại". Đề nghị: leader cho hằng số công khai hoặc một getter. Tới khi có, màn chỉ hiện "còn quyền gửi lại" hoặc "đã hết lượt" theo `canResubmit()`.
3. Thứ tự của `SellerVerification.imageIds` chưa được tài liệu ghi rõ. Issue này đặt phần tử đầu là ảnh giấy tờ và phần tử sau là ảnh chân dung cầm giấy tờ (đúng thứ tự `model.md#sellerverification` liệt kê). E03 (Phát) đọc hai ảnh nên cần cùng quy ước; Rules không kiểm được `purpose` từng ảnh. Xác nhận thứ tự và báo Phát.
4. `barem-diem.md` kể implicit Intent gồm cả chụp ảnh (`MediaStore.ACTION_IMAGE_CAPTURE`), còn UC31 và README D01 chỉ có "ô tải ảnh". Đọc 1 (mặc định trong issue này): chỉ chọn ảnh từ thư viện bằng implicit Intent. Đọc 2: thêm chụp ảnh tại chỗ (có thể cần `FileProvider` và quyền camera, đều nằm trong `AndroidManifest.xml` của Phát). Xác nhận; câu trả lời áp luôn cho P2-D4a, P2-D5a và P2-D5b.
