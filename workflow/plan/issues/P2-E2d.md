TITLE: feat(admin): duyệt yêu cầu xác thực tài khoản
LABELS: type:feature, area:admin, module:e, priority:p1
ASSIGNEES: tahpnart8
PHASE: P2

Plan item: P2-E2d (phần bốn, cuối của P2-E2; xem P2-E2a về lý do tách)

### Bối cảnh
1. PUBGApp là ứng dụng Android mua bán, trao đổi và cho tặng đồ cũ kiêm mạng xã hội, viết bằng Java 17, giao diện XML với ViewBinding, dữ liệu trên Firebase Authentication và Cloud Firestore gói Spark, bộ nhớ đệm Room.
2. Module E (Quản trị, người làm Phát) gồm các màn E01 đến E07 chạy trong `AdminActivity` và việc chạy nền của giao dịch; quyền admin không bao giờ lấy từ phía ứng dụng (R12): admin là người có tài liệu `admins/{uid}`, `SessionManager.isAdmin()` chỉ để hiện hay ẩn giao diện, quyền thật do Security Rules quyết định.
3. Issue này thêm khối "Yêu cầu xác thực tài khoản" vào màn E03 `AdminUserDetailFragment`: admin xem số CCCD và hai ảnh người dùng gửi rồi duyệt hoặc từ chối kèm lý do (STT 73, UC36).
4. Đọc trước: `workflow/docs/architecture/trang-thai.md` mục 8 (xác thực tài khoản); `workflow/docs/ui/e-quan-tri/README.md` mục E03 (khối "Yêu cầu xác thực tài khoản"); `workflow/docs/product/use-case/uc-e-quan-tri.md` mục UC36.
5. Đã có trên `main`: khung P1, P2-E1, P2-E2a, P2-E2b (E03 chỉ xem, khối yêu cầu xác thực chừa trống `gone`), P2-E2c (nút hành động); phía người dùng gửi yêu cầu là việc của Phúc (P2-D1, `UserRepository.requestVerification`).

### Mã tính năng
F-14 (Admin người dùng, nội dung, danh mục, giao dịch, chuyển tiền tự động, tài khoản admin). Phía người dùng của STT 73 thuộc F-11 (P2-D1).

### Mã việc kế hoạch
P2-E2, tách thành P2-E2a, P2-E2b, P2-E2c, P2-E2d (STT 73). Xem P2-E2a về lý do tách. Việc này riêng vì đây là cửa mở quyền mua bán: ghi hai tài liệu (`sellerVerifications` và `users`) cùng lúc kèm thông báo, và là chỗ duy nhất admin đọc số CCCD cùng ảnh giấy tờ.

### Module
E. Quản trị và nền móng (Phát)

### STT trong bảng đặc tả
73 (phía admin: duyệt yêu cầu xác thực tài khoản)

### Màn hình mockup
E03 `AdminUserDetailFragment`, ảnh `workflow/docs/ui/e-quan-tri/e03-admin-chi-tiet-nguoi-dung.png` (ảnh chưa vẽ khối này). Làm theo README mục E03: khối "Yêu cầu xác thực tài khoản" hiện khi có yêu cầu chờ duyệt, gồm số CCCD người gửi khai, ảnh giấy tờ, ảnh chân dung cầm giấy tờ, số lần đã gửi lại, nút "Duyệt", nút "Từ chối" kèm ô lý do, và dòng nhắc hệ quả.

### Làm gì và vì sao
Duyệt xác thực là **mở quyền mua bán**, không phải cấp huy hiệu: tài khoản được duyệt mới đăng tin, gửi đề nghị và tạo giao dịch được (`User.canSell`, `User.canBuy`, Rules `verified()`); tài khoản bị từ chối vẫn xem, tìm kiếm, nhắn tin, lưu tin và theo dõi bình thường. Admin vào E03 của người có yêu cầu chờ (đi từ dòng "Cần xử lý" của E01 qua E02 với công tắc "Chờ xác thực") và thấy khối yêu cầu: số CCCD do người gửi khai (`nationalId`), hai ảnh (`imageIds`) và số lần đã gửi lại (`resubmitCount`). Admin đối chiếu số CCCD với hai ảnh, rồi bấm "Duyệt", hoặc bấm "Từ chối" và nhập lý do (bắt buộc). Hệ thống cập nhật `sellerVerifications/{id}` và `users/{id}.verificationStatus` trong cùng một lô ghi rồi gửi thông báo `VERIFICATION_RESULT` cho người gửi. Số CCCD và ảnh chỉ hiện ở khối này, không hiện ở hồ sơ chung (UC34 BR2, UC36 BR3). Hệ thống không kiểm số CCCD có trùng với tài khoản khác không: admin tự đối chiếu bằng mắt ở khối này và nếu thấy trùng thì bấm "Từ chối" kèm lý do (UC36 BR4). Ai được duyệt do Security Rules quyết định (`isAdmin()`); ứng dụng không tự khai quyền (R12). Về barem (`workflow/docs/product/barem-diem.md`): issue này phục vụ dòng R-Chapter 4 bằng input control (ô lý do bắt buộc trong hộp thoại từ chối, hai nút "Duyệt" và "Từ chối") và hộp thoại (menu và picker); dòng R-Chapter 1, 2, 6, 7 bằng `DataCallback` bất đồng bộ cho lệnh ghi hai tài liệu cùng một lô, giải mã ảnh Base64 ngoài luồng chính (chương 7, async) và `NetworkMonitor` khóa hai nút khi offline (chương 7, kiểm tra kết nối mạng); dòng R-Chapter 3 bằng unit test JUnit `AdminVerificationRulesTest`.

### Tiêu chí nghiệm thu
- [ ] Với Firebase thật, bấm "Duyệt": `sellerVerifications/{id}.status` thành `APPROVED`, `reviewedBy` là admin đang đăng nhập, `reviewedAt` là giờ máy chủ; `users/{id}.verificationStatus` thành `APPROVED` trong cùng lô; người gửi có thông báo `VERIFICATION_RESULT` (`targetId` là mã người dùng), ghi trong cùng lô qua `NotificationRepository.create(notification, batch, cb)`. Sau đó `User.canSell(true)` và `canBuy(true)` của tài khoản đó là true.
- [ ] Bấm "Từ chối" với lý do: `status` thành `REJECTED`, `note` bằng lý do, `reviewedBy`, `reviewedAt` như trên; `users/{id}.verificationStatus` thành `REJECTED`; thông báo `VERIFICATION_RESULT` kèm lý do; tài khoản vẫn ở trạng thái chưa xác thực, và nếu `SellerVerification.canResubmit()` thì người dùng gửi lại được (tối đa 3 lần gửi lại).
- [ ] "Từ chối" để trống lý do: báo lỗi trong hộp thoại, giữ nguyên hộp thoại, không ghi gì.
- [ ] Khối hiện số CCCD, hai ảnh (ảnh giấy tờ, ảnh chân dung cầm giấy tờ, đọc bằng `MediaRepository.getImage` qua `AdminUserViewModel` (chỗ đưa ảnh sang Fragment chờ Câu hỏi 1), ảnh hỏng hoặc đã xóa hiện ảnh dự phòng, không làm sập màn), và "Đã gửi lại N lần (tối đa 3 lần)". Số CCCD không xuất hiện ở bất kỳ chỗ nào khác của ứng dụng và không được ghi vào `Log`.
- [ ] Yêu cầu đã được xử lý bởi admin khác (khi mở khối thì còn PENDING nhưng lúc bấm đã đổi): báo "Yêu cầu đã được xử lý", làm mới khối, không ghi đè.
- [ ] Tài khoản đã bị khóa hoặc đã xóa mà đang có yêu cầu chờ duyệt: khối hiện nhưng hai nút bị khóa kèm câu báo không thể duyệt; không ghi gì (Câu hỏi 2).
- [ ] Hệ thống không kiểm số CCCD trùng: không có truy vấn nào theo `nationalId` ở `reviewVerification`; admin tự đối chiếu bằng mắt và, nếu số đã gắn với tài khoản khác, bấm "Từ chối" kèm lý do (UC36 BR4).
- [ ] Mỗi đường lỗi hiện câu tiếng Việt lấy từ `error`, không im lặng: mất mạng, Rules từ chối, quá 15 giây, đọc ảnh lỗi. Khi offline hoặc khi `loading` là true hai nút bị khóa; hộp thoại từ chối đóng khi `done` bật.
- [ ] Yêu cầu, ô lý do đang gõ và hộp thoại đang mở giữ qua xoay màn hình; xoay không đọc lại mạng.
- [ ] Chuyển trạng thái chỉ theo `trang-thai.md` mục 8 (PENDING sang APPROVED hoặc REJECTED), qua `AdminRepository.reviewVerification`; `SellerVerification.approve(String adminId)` và `reject(String adminId, String note)` được dùng để đặt trường, không viết thẳng `status` (R18).
- [ ] Unit test cho hàm quy tắc của issue: `AdminVerificationRulesTest` (tên hàm ở bước 1 mục Các bước đề nghị).
- [ ] `./gradlew assembleDebug testDebugUnitTest lintDebug` và `sh workflow/scripts/check-java-style.sh` đều qua; PR ghi đúng lệnh và kết quả thật (R1).
- [ ] PR liệt kê mọi lớp, phương thức, trường công khai mới kèm dòng tài liệu định nghĩa (R16); `AdminVerificationRules` là lớp giao diện không có trong `dac-ta-lop`, ghi vào PR như lớp mới không có dòng tài liệu.

### Tài liệu phải đọc
- `AGENTS.md` mục 6: R12, R16, R17, R18, R20, R22, R23.
- `workflow/plan/so-huu.md` mục 1 (ghi chú STT 73: Phúc làm phía người dùng, Phát làm phía admin), 3 (E03), 5, 6.
- `workflow/docs/product/dac-ta-chuc-nang.md` dòng STT 73.
- `workflow/docs/ui/e-quan-tri/README.md` mục E03 (khối "Yêu cầu xác thực tài khoản", "Trạng thái đặc biệt", "Khác với ảnh").
- `workflow/docs/product/use-case/uc-e-quan-tri.md` mục UC36 (luồng, ngoại lệ, BR1 đến BR7, NFR1).
- `workflow/docs/architecture/trang-thai.md` mục 8 và mục 9 (dòng VERIFICATION_RESULT).
- `workflow/docs/architecture/dac-ta-lop/data.md#adminrepository` (dòng `getVerificationRequests`, `reviewVerification`), `#mediarepository` (dòng `getImage`), `#userrepository` (dòng `requestVerification`, để biết phía gửi); `viewmodel.md#adminuserviewmodel`.
- `workflow/docs/architecture/dac-ta-lop/model.md#sellerverification`, `#user`, `#appnotification`; `enums.md#verificationstatus`, `#imagepurpose`, `#notificationtype`.
- `workflow/docs/data/firestore-schema.md#sellerverifications`; `workflow/docs/data/security-rules.md` mục 6 (sellerVerifications, dòng Duyệt và Từ chối), mục 1 (dòng "Duyệt xác thực" của `users`), mục 3, mục 24.
- `workflow/docs/conventions/giao-dien.md` mục 1 đến 4 và 8; `workflow/memory/code/README.md`, `workflow/memory/code/firebase.md`, `workflow/docs/conventions/android-java.md`, skill `pubg-code`, skill `pubg-ui`.

### Lớp, trường, trạng thái sẽ dùng
- `AdminUserDetailFragment` (E03, khối yêu cầu xác thực).
- `AdminUserViewModel`: thuộc tính `verifications` (`LiveData<List<SellerVerification>>`), `selectedUser`, `loading`, `error`, `done`; `public void loadVerificationRequests()`, `public void reviewVerification(String userId, boolean approve, String note)` (thay thân). `viewmodel.md` ghi `AdminUserViewModel` dùng cả `MediaRepository` để đọc ảnh xác thực, nhưng chưa có thuộc tính hay phương thức đưa ảnh sang Fragment: Câu hỏi 1.
- `AdminRepository`: `public void getVerificationRequests(DataCallback<List<SellerVerification>> cb)`, `public void reviewVerification(String id, boolean approve, String note, DataCallback<Void> cb)` (thay thân).
- `MediaRepository`: `public void getImage(String id, DataCallback<byte[]> cb)` (chỉ gọi).
- `BaseRepository`: `protected String currentUserId()`, `protected void logActivity(String action, ReportTarget targetType, String targetId)`, `protected void fail(DataCallback, Exception)`. `NotificationRepository.create(AppNotification notification, WriteBatch batch, DataCallback<Void> cb)` (chỉ repository gọi; truyền lô ghi đang dựng).
- `SellerVerification`: `userId`, `nationalId`, `imageIds`, `status`, `note`, `resubmitCount`, `reviewedBy`, `reviewedAt`; `public void approve(String adminId)`, `public void reject(String adminId, String note)`, `public boolean canResubmit()`.
- `User`: `status`, `verificationStatus`. `UserStatus.ACTIVE`. `VerificationStatus`: `PENDING`, `APPROVED`, `REJECTED`. `ImagePurpose.VERIFICATION`. `NotificationType.VERIFICATION_RESULT`. `ReportTarget.USER`.
- `NetworkMonitor`: `public static NetworkMonitor getInstance()`, thuộc tính `online`.
- Firestore: `sellerVerifications/{userId}` (khóa được admin đổi: `status`, `note`, `reviewedBy`, `reviewedAt`, `updatedAt`; không đổi `nationalId`, `imageIds`), `users/{id}` (chỉ `verificationStatus`, `updatedAt`), `images/{id}`, `notifications`, `activityLogs`.

### File được tạo hoặc sửa
- [sửa] PUBGApp/app/src/main/java/com/nhom3/pubgapp/ui/admin/AdminUserDetailFragment.java
- [tạo] PUBGApp/app/src/main/java/com/nhom3/pubgapp/ui/admin/AdminVerificationRules.java
- [sửa] PUBGApp/app/src/main/res/layout/fragment_admin_user_detail.xml (điền khối yêu cầu xác thực đã chừa)
- [tạo] PUBGApp/app/src/main/res/layout/dialog_admin_verification_reject.xml
- [sửa] PUBGApp/app/src/main/res/values/strings_admin.xml
- [sửa] PUBGApp/app/src/main/java/com/nhom3/pubgapp/viewmodel/AdminUserViewModel.java (chỉ thân `loadVerificationRequests`, `reviewVerification`)
- [sửa] PUBGApp/app/src/main/java/com/nhom3/pubgapp/data/AdminRepository.java (chỉ thân `getVerificationRequests`, `reviewVerification`)
- [tạo] PUBGApp/app/src/test/java/com/nhom3/pubgapp/ui/admin/AdminVerificationRulesTest.java

Tên layout theo `so-huu.md` mục 2 và khung P1-06; nếu khung đã đặt tên khác thì theo khung.

### Các bước đề nghị
Có thể làm theo thứ tự khác, nhưng không được đổi tên lớp, phương thức, thuộc tính nào.
1. Tạo `AdminVerificationRules` (thuần Java) với `public static boolean canReview(UserStatus userStatus, VerificationStatus requestStatus)`: true khi `userStatus` là `ACTIVE` và `requestStatus` là `PENDING`. Viết `AdminVerificationRulesTest` trước: đủ bốn giá trị `UserStatus` nhân bốn giá trị `VerificationStatus` (chỉ một cặp cho kết quả true).
2. `AdminRepository.getVerificationRequests(DataCallback<List<SellerVerification>> cb)`: truy vấn `sellerVerifications` với `status == PENDING`, sắp `createdAt` tăng dần (cũ nhất trước; cần chỉ mục `status`+`createdAt`, có trong "Chỉ mục ghép dự kiến"); đổi thành `SellerVerification` bằng `toObject(SellerVerification.class)`; lỗi qua `fail(cb, e)`.
3. `AdminRepository.reviewVerification(String id, boolean approve, String note, DataCallback<Void> cb)`: (a) từ chối ngay nếu `approve` false và `note` rỗng; (b) đọc `sellerVerifications/{id}` và `users/{id}`, báo "Yêu cầu đã được xử lý" nếu yêu cầu không còn `PENDING`, báo không thể duyệt nếu người dùng không ở `ACTIVE` (repository không được gọi lớp `ui`, nên tự viết lại điều kiện giống `AdminVerificationRules.canReview`: người dùng `ACTIVE` và yêu cầu `PENDING`); (c) dựng `SellerVerification` từ tài liệu, gọi `approve(currentUserId())` hoặc `reject(currentUserId(), note)`; (d) một lô ghi: `sellerVerifications/{id}` chỉ các khóa `status`, `note` (khi từ chối), `reviewedBy`, `reviewedAt` (giờ máy chủ), `updatedAt`, và `users/{id}` chỉ `verificationStatus` (cùng giá trị với `status`) và `updatedAt`, đúng `onlyKeys` ở security-rules.md mục 1 và 6; thông báo `VERIFICATION_RESULT` cho `id` (`targetId` là `id`) đưa vào cùng lô bằng `NotificationRepository.create(notification, batch, cb)`, chữ nói "xác thực tài khoản", lý do nằm trong nội dung khi từ chối; chờ `cb` của `create` rồi mới commit lô (cách `create` thêm vào lô cần đọc thân của P1, chưa kiểm chứng); (e) sau khi lô thành công thì `logActivity`. Không có bước kiểm số CCCD trùng (UC36 BR4: admin tự đối chiếu). Lỗi bất kỳ qua `fail(cb, e)`.
4. `AdminUserViewModel.loadVerificationRequests()` bật `loading`, gọi `getVerificationRequests`, đặt kết quả vào `verifications` rồi tắt `loading`; `reviewVerification(String userId, boolean approve, String note)` gọi `AdminRepository.reviewVerification` ngoài luồng chính, xong thì bật `done`, tải lại `verifications` và `loadProfile(userId)` để E03 hiện trạng thái mới; lỗi đặt câu tiếng Việt vào `error`. Ảnh đọc bằng `MediaRepository.getImage` (đường đưa ảnh từ ViewModel sang Fragment: Câu hỏi 1).
5. Layout `fragment_admin_user_detail.xml`: điền khối đã chừa: nhãn số CCCD và giá trị, hai ô ảnh có nhãn ("Ảnh giấy tờ", "Ảnh chân dung cầm giấy tờ") dùng ảnh giữ chỗ lúc giải mã và ảnh dự phòng khi hỏng, dòng "Đã gửi lại N lần (tối đa 3 lần)", hai nút "Duyệt" (chính) và "Từ chối", dòng nhắc hệ quả: duyệt là mở quyền đăng tin, gửi đề nghị và tạo giao dịch cho tài khoản này; từ chối thì tài khoản vẫn xem, tìm kiếm, nhắn tin và theo dõi được nhưng chưa mua bán được.
6. `dialog_admin_verification_reject.xml`: ô lý do bắt buộc và hai nút "Xác nhận", "Hủy".
7. `AdminUserDetailFragment`: tìm trong `verifications` phần tử có `userId` bằng người đang xem; có thì hiện khối, không có thì ẩn. Bật hai nút bằng `AdminVerificationRules.canReview(User.status, yêu cầu.status)` và khi `NetworkMonitor.getInstance().online` là true và `loading` là false. Ảnh: giải mã ngoài luồng chính (Glide có sẵn trong build nhận mảng byte). Không gán `nationalId` vào bất kỳ View nào khác và không ghi nó vào `Log`.
8. Chuỗi trong `strings_admin.xml` theo mẫu `admin_verification_<what>`, mọi chữ nói "xác thực tài khoản", không dùng "xác minh người bán": tiêu đề khối, nhãn, dòng nhắc hệ quả, câu báo "Yêu cầu đã được xử lý", câu báo không thể duyệt, câu báo số CCCD đã gắn tài khoản khác, câu lỗi.
9. Thử trên máy ảo: tạo một yêu cầu (bằng P2-D1 nếu đã xong, hoặc tay: tài liệu `sellerVerifications/{uid}` với `userId`, `nationalId` 12 chữ số, `imageIds` hai mã ảnh thật, `status` `PENDING`, `note` rỗng, `resubmitCount` 0, `reviewedBy` rỗng; và đặt `users/{uid}.verificationStatus` thành `PENDING`), duyệt một yêu cầu, từ chối một yêu cầu.
10. Chạy hai lệnh kiểm ở mục "Tự kiểm trước khi mở Pull Request" và ghi kết quả vào PR.

### File dùng chung cần thêm dòng
Không có. Ghi vào mô tả PR để P3-R gộp: luật "Duyệt" và "Từ chối" của `sellerVerifications` (security-rules.md mục 6), "Duyệt xác thực" của `users` (mục 1), đọc `images` (mục 24), tạo `notifications` (mục 21); chỉ mục `sellerVerifications`: `status`+`createdAt`.

### Cấm đụng
- Leader-only không có trong danh sách trên: `AndroidManifest.xml`, `nav_main.xml`, `nav_admin.xml`, `themes.xml`, `colors.xml`, `dimens.xml`, `strings_common.xml`, `AppDatabase`, `Converters`, mọi file Gradle, `firebase/`.
- Module D (Phúc): `UserRepository.requestVerification` và nút gửi yêu cầu trên D01 (phía người dùng của STT 73).
- Thân các phương thức khác của `AdminUserViewModel`, `AdminRepository`, và hai khối khác của E03 (thuộc P2-E2b, P2-E2c).
- `SellerVerification`, `User` (lớp mô hình: chỉ gọi); `MediaRepository` (chỉ gọi).

### Phụ thuộc
P2-E2c (khối hành động của E03 đã có) hoặc ít nhất P2-E2b, P1-04, P1-06. Mềm: P2-D1 (tạo yêu cầu thật; trong lúc chờ tạo tay trên Console). Phần hiện hai ảnh bị chặn bởi Câu hỏi 1 (R20: thiếu chỗ đưa ảnh sang Fragment thì gắn nhãn `blocked` cho phần đó); số CCCD, hai nút Duyệt và Từ chối làm được ngay.

### Ngoài phạm vi
- Gửi, gửi lại yêu cầu xác thực, chọn ảnh, khai số CCCD: P2-D1 (Phúc). Kiểm đủ 12 chữ số khi gửi: phía người dùng.
- Danh sách riêng "Yêu cầu xác thực" như màn riêng: không có màn này; admin đi qua E01 và E02 (công tắc "Chờ xác thực").
- Hạn xử lý 48 giờ (UC36 BR5): không có cơ chế tự động trong thiết kế, không làm.
- Mã hóa số CCCD khi lưu: không làm. UC36 NFR1 đã bỏ câu "lưu mã hóa", thay bằng ràng buộc có thật của Security Rules mục 3 và 6 (chỉ chủ tài khoản và admin đọc). Ghi log mọi lượt mở yêu cầu (vế còn lại của NFR1): Câu hỏi 3. Nhật ký thao tác admin có giao diện: STT 100, Phát triển sau.
- Bố cục ngang: P3-E.

### Tài liệu cần cập nhật
Không có, trừ khi Câu hỏi 1 được trả lời bằng thêm thuộc tính hay phương thức: khi đó cập nhật `workflow/docs/architecture/dac-ta-lop/viewmodel.md#adminuserviewmodel`.

### Tự kiểm trước khi mở Pull Request
- [ ] Với yêu cầu `PENDING` thật: mở E03, thấy số CCCD, hai ảnh, "Đã gửi lại 0 lần (tối đa 3 lần)".
- [ ] Bấm "Duyệt": sau vài giây khối biến mất; trên Console `sellerVerifications/{id}.status` và `users/{id}.verificationStatus` cùng là `APPROVED`; có thông báo mới cho người đó.
- [ ] Với yêu cầu khác: "Từ chối" trống lý do báo lỗi và giữ hộp thoại; có lý do thì `REJECTED`, `note` đúng, thông báo kèm lý do.
- [ ] Hai quản trị viên cùng mở một yêu cầu: người thứ hai bấm sau thấy "Yêu cầu đã được xử lý".
- [ ] Đặt tay `users/{uid}.status` thành `TEMP_LOCKED` cho tài khoản có yêu cầu `PENDING`: hai nút bị khóa, có câu báo.
- [ ] Tắt mạng: hai nút bị khóa.
- [ ] `grep -rn "nationalId" PUBGApp/app/src/main/java/com/nhom3/pubgapp/ui/admin/` chỉ ra đúng một nơi gán chữ vào ô số CCCD, và không có `Log` nào chứa nó.
- [ ] `cd PUBGApp && ./gradlew testDebugUnitTest --tests "*AdminVerificationRulesTest"` báo các test đều qua.
- [ ] `cd PUBGApp && ./gradlew assembleDebug testDebugUnitTest lintDebug` kết thúc `BUILD SUCCESSFUL`.
- [ ] `sh workflow/scripts/check-java-style.sh` không báo lỗi.
- [ ] Xoay màn hình khi hộp thoại từ chối đang mở và lý do đã gõ dở: hộp thoại và chữ còn nguyên, ảnh không tải lại từ mạng.
- [ ] Đã liệt kê trong PR mọi lớp, phương thức, trường công khai mới kèm dòng tài liệu định nghĩa (R16), hoặc ghi "không có".

### Quy mô
- [x] Đây là một lát cắt dọc của một module, khoảng nửa ngày đến một ngày, một Pull Request.

### Câu hỏi cho leader
1. `viewmodel.md` ghi `AdminUserViewModel` dùng `MediaRepository` (đọc ảnh xác thực) nhưng không có thuộc tính hay phương thức nào đưa ảnh sang Fragment, và Fragment không được gọi repository trực tiếp. Đề xuất bổ sung: một thuộc tính trả hai ảnh của yêu cầu đang xem (ví dụ `verificationImages`, `LiveData<List<byte[]>>`) và phương thức nạp theo mã yêu cầu; tên do leader chốt. Việc này chặn phần hiện hai ảnh. E03 cần yêu cầu của một người cụ thể trong khi `getVerificationRequests` chỉ trả danh sách `PENDING`; tạm thời lọc theo `userId` trên máy.
2. UC36 ngoại lệ 2b nói người dùng đã bị khóa hoặc xóa thì "đóng yêu cầu", nhưng `VerificationStatus` không có giá trị "đã đóng" (chỉ NONE, PENDING, APPROVED, REJECTED) và `trang-thai.md` mục 8 không có chuyển trạng thái đó. Tạm thời: không ghi gì, khóa hai nút và báo không thể duyệt. Xác nhận, hoặc cho cách đóng (ví dụ ghi `REJECTED` kèm lý do hệ thống).
3. UC36 NFR1 vẫn nói "Mọi lượt Admin mở yêu cầu đều được ghi log" (cùng ý với UC34 BR3), nhưng `getVerificationRequests` và `reviewVerification` chỉ ghi log lúc duyệt hoặc từ chối, và STT 100 là Phát triển sau. Tạm thời chỉ ghi log lúc duyệt hoặc từ chối. Xác nhận và sửa NFR1.
4. Thứ tự hai mã trong `SellerVerification.imageIds` chỉ được mô tả là "ảnh giấy tờ và ảnh chân dung cầm giấy tờ", Rules không kiểm `purpose` từng ảnh. Tạm thời phần tử đầu là ảnh giấy tờ, phần tử thứ hai là ảnh chân dung cầm giấy tờ. Cần khớp với cách P2-D1 (Phúc) ghi `requestVerification`.
5. Chữ của thông báo `VERIFICATION_RESULT` do repository ghi (không có `Context` để đọc string resource): xem Câu hỏi 4 của P2-E2c.
