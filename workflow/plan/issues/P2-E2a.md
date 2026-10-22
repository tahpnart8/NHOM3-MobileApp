TITLE: feat(admin): danh sách người dùng, tìm kiếm và bộ lọc
LABELS: type:feature, area:admin, module:e, priority:p1
ASSIGNEES: tahpnart8
PHASE: P2

Plan item: P2-E2a (phần đầu của P2-E2; các phần sau là P2-E2b, P2-E2c, P2-E2d)

### Bối cảnh
1. PUBGApp là ứng dụng Android mua bán, trao đổi và cho tặng đồ cũ kiêm mạng xã hội, viết bằng Java 17, giao diện XML với ViewBinding, dữ liệu trên Firebase Authentication và Cloud Firestore gói Spark, bộ nhớ đệm Room.
2. Module E (Quản trị, người làm Phát) gồm các màn E01 đến E07 chạy trong `AdminActivity` và việc chạy nền của giao dịch; quyền admin không bao giờ lấy từ phía ứng dụng (R12): admin là người có tài liệu `admins/{uid}`, `SessionManager.isAdmin()` chỉ để hiện hay ẩn giao diện, quyền thật do Security Rules quyết định.
3. Issue này làm màn E02 `AdminUserListFragment` (Danh sách người dùng): xem, tìm theo tên hoặc email, lọc theo trạng thái, ngày tham gia, khu vực và theo "Chờ xác thực" (STT 68, UC34).
4. Đọc trước: `workflow/docs/ui/e-quan-tri/README.md` mục E02; `workflow/docs/product/use-case/uc-e-quan-tri.md` mục UC34; `workflow/docs/architecture/dac-ta-lop/data.md#adminrepository` (dòng `listUsers`) và `viewmodel.md#adminuserviewmodel`.
5. Đã có trên `main`: khung P1 (lớp mô hình, repository đủ chữ ký trả `NOT_READY`, Fragment E02 trống, `nav_admin.xml`, `strings_admin.xml`, `view_*`), và P2-E1 (E01, `AdminActivity` chặn người không phải admin).

### Mã tính năng
F-14 (Admin người dùng, nội dung, danh mục, giao dịch, chuyển tiền tự động, tài khoản admin)

### Mã việc kế hoạch
P2-E2, tách thành P2-E2a (E02 danh sách và lọc: STT 68), P2-E2b (E03 hồ sơ chỉ xem: STT 69, 74), P2-E2c (khóa, mở khóa, cảnh báo ba bậc, gỡ toàn bộ tin: STT 70, 71, 72, 77), P2-E2d (duyệt yêu cầu xác thực tài khoản: STT 73). Lý do tách: kế hoạch gộp tất cả vào một việc nhưng đó là hai màn, bảy chức năng, bốn nhóm quy tắc ghi khác nhau (`users`, `warnings`, `sellerVerifications`, `listings`); một ngày không đủ và phần ghi cần review riêng trên `trang-thai.md` mục 7 và 8.

### Module
E. Quản trị và nền móng (Phát)

### STT trong bảng đặc tả
68

### Màn hình mockup
E02 `AdminUserListFragment`, ảnh `workflow/docs/ui/e-quan-tri/e02-admin-danh-sach-nguoi-dung.png`. Khác ảnh (README mục E02 thắng): ô tìm theo tên hoặc email (không phải tên, số điện thoại); trạng thái gồm Tất cả, Đang hoạt động, Khóa tạm, Khóa vĩnh viễn, Đã xóa; công tắc "Chờ xác thực" riêng; thêm bộ lọc ngày tham gia và tỉnh hoặc thành phố (ảnh chưa vẽ).

### Làm gì và vì sao
Admin vào E02 từ lưới lối tắt của E01 (ô "Người dùng") hoặc từ dòng yêu cầu xác thực của E01 (khi đó công tắc "Chờ xác thực" đang bật) và thấy danh sách mọi người dùng, mới tham gia trước. Admin gõ tên hoặc email vào ô tìm kiếm rồi bấm phím Tìm kiếm, bấm chip trạng thái, chọn khoảng ngày tham gia hoặc nhập tỉnh hoặc thành phố thì màn gọi `AdminUserViewModel.filterUsers` với đủ năm điều kiện (STT 68), và danh sách đổi ngay tại màn này. Mỗi dòng có ảnh đại diện chữ cái đầu, tên, số điện thoại che một phần, ngày tham gia, nhãn trạng thái tài khoản và, nếu tài khoản từng bị cảnh báo, thêm nhãn "Cảnh báo" màu cam. Bấm một dòng mở E03 của người đó (P2-E2b). Màn này chỉ xem, không sửa dữ liệu (UC34 BR1). Quyền xem toàn bộ `users` là của admin do Security Rules quyết định; giao diện không tự coi ai là admin (R12). Về barem (`workflow/docs/product/barem-diem.md`): issue này phục vụ dòng R-Chapter 4 bằng RecyclerView (`AdminUserAdapter`), input control (ô tìm kiếm, hàng chip lọc trạng thái, công tắc "Chờ xác thực") và menu và picker (`DatePickerDialog` chọn khoảng ngày tham gia); dòng R-Chapter 1, 2, 6, 7 bằng `DataCallback` bất đồng bộ cho lệnh đọc `users` (chương 7, async) và `NetworkMonitor` cho dải báo offline (chương 7, kiểm tra kết nối mạng); dòng R-Chapter 3 bằng unit test JUnit `AdminUserFilterRulesTest`.

### Tiêu chí nghiệm thu
- [ ] Với Firebase thật: vào E02 thì đọc `users` sắp `createdAt` giảm dần, hiện đủ các dòng; mỗi dòng có tên (`displayName`), số điện thoại che một phần, ngày tham gia (`createdAt`), nhãn trạng thái đúng nhãn và màu ở `giao-dien.md` mục 4 (Đang hoạt động, Khóa tạm, Khóa vĩnh viễn, Đã xóa), nhãn "Cảnh báo" cam khi `warningCount` lớn hơn 0.
- [ ] Chip trạng thái: Tất cả (không lọc), Đang hoạt động (`ACTIVE`), Khóa tạm (`TEMP_LOCKED`), Khóa vĩnh viễn (`PERM_LOCKED`), Đã xóa (`DELETED`) đọc lại `users` theo `status`.
- [ ] Bộ lọc "Ngày tham gia" (từ ngày, đến ngày) lọc `createdAt` từ đầu ngày "từ" đến hết ngày "đến" (ngày "đến" được tính trọn ngày); bộ lọc "Tỉnh hoặc thành phố" lọc `province` đúng chuỗi đã nhập; ba bộ lọc dùng được cùng nhau, kèm chip trạng thái.
- [ ] Ô tìm kiếm: gõ từ khóa rồi bấm phím Tìm kiếm thì `filterUsers` được gọi với từ khóa đó và danh sách chỉ còn người có tên hoặc email chứa từ khóa, không phân biệt hoa thường và dấu (cách khớp: Câu hỏi 1); dùng được cùng các bộ lọc khác; xóa trống ô thì đủ danh sách trở lại.
- [ ] Công tắc "Chờ xác thực" chỉ hiện người có `verificationStatus` là `PENDING`, dùng được cùng các bộ lọc khác.
- [ ] Số điện thoại hiện che một phần: giữ 3 chữ số cuối, các chữ số còn lại thành dấu `*`; không có số thì để trống, không hiện chữ "null".
- [ ] Bấm một dòng mở E03 với mã người dùng. Đích chưa có nội dung thật vẫn mở được khung trống của P1.
- [ ] Không có kết quả: hiện dòng báo không có người dùng nào khớp (`view_empty_state`). Lỗi đọc (kể cả thiếu chỉ mục hoặc Rules từ chối) hiện câu tiếng Việt lấy từ `error` kèm nút "Thử lại", không im lặng; tiến trình đang tải bật theo `loading`. Mất mạng: dải `view_offline_banner`, danh sách đã tải vẫn còn.
- [ ] Danh sách đã tải nằm trong `AdminUserViewModel`; chữ trong ô tìm kiếm, chip đang chọn, ngày, tỉnh, công tắc được giữ qua xoay màn hình bằng `onSaveInstanceState` của Fragment (ViewModel không có thuộc tính giữ bộ lọc và R20 cấm thêm); xoay màn hình không đọc lại `users`.
- [ ] Không có chuyển trạng thái nào (màn chỉ xem), nên không áp `trang-thai.md`; PR ghi rõ như vậy.
- [ ] Unit test cho các hàm quy tắc của issue: `AdminUserFilterRulesTest` kiểm `maskPhone` và `endOfDay` (tên hàm ở bước 1 mục Các bước đề nghị).
- [ ] `./gradlew assembleDebug testDebugUnitTest lintDebug` và `sh workflow/scripts/check-java-style.sh` đều qua; PR ghi đúng lệnh và kết quả thật (R1).
- [ ] PR liệt kê mọi lớp, phương thức, trường công khai mới kèm dòng tài liệu định nghĩa (R16). `AdminUserAdapter` và `AdminUserFilterRules` là lớp giao diện không có trong `dac-ta-lop`, ghi vào PR như lớp mới không có dòng tài liệu.

### Tài liệu phải đọc
- `AGENTS.md` mục 6: R12, R16, R17, R20, R22, R23.
- `workflow/plan/so-huu.md` mục 2, 3 (E02), 5 (`AdminRepository`, `AdminUserViewModel` của Phát), 6.
- `workflow/docs/product/dac-ta-chuc-nang.md` dòng STT 68.
- `workflow/docs/ui/e-quan-tri/README.md` mục E02 và "Khác với ảnh"; `workflow/docs/ui/README.md` mục "Điều hướng".
- `workflow/docs/product/use-case/uc-e-quan-tri.md` mục UC34 (luồng cơ bản, ngoại lệ 1a, 2a, 3a, BR1, BR4).
- `workflow/docs/architecture/dac-ta-lop/data.md#adminrepository`; `viewmodel.md#adminuserviewmodel`; `model.md#user`; `enums.md#userstatus`, `#verificationstatus`; `util.md#formatutils` (dòng `removeAccents`).
- `workflow/docs/data/firestore-schema.md#users` (trường và bảng "Chỉ mục ghép dự kiến"); `workflow/docs/data/security-rules.md` mục 1 (users, dòng Đọc).
- `workflow/docs/conventions/giao-dien.md` mục 1 đến 6 và 8.
- `workflow/memory/code/README.md`, `workflow/memory/code/firebase.md`, `workflow/docs/conventions/android-java.md`, skill `pubg-code`, skill `pubg-ui`.

### Lớp, trường, trạng thái sẽ dùng
- `AdminUserListFragment` (E02).
- `AdminUserViewModel`: thuộc tính `users` (`LiveData<List<User>>`), `loading` (`LiveData<Boolean>`), `error` (`LiveData<String>`); `public void filterUsers(UserStatus status, Timestamp from, Timestamp to, String province, String keyword)` (thay thân).
- `AdminRepository`: `public void listUsers(UserStatus status, Timestamp from, Timestamp to, String province, String keyword, DataCallback<List<User>> cb)` (thay thân).
- `User`: `displayName`, `email`, `phone`, `status`, `verificationStatus`, `warningCount`, `province`, `createdAt` (kế thừa từ `BaseDocument`), `getId()`.
- `UserStatus`: `ACTIVE`, `TEMP_LOCKED`, `PERM_LOCKED`, `DELETED`. `VerificationStatus.PENDING`.
- `FormatUtils`: `public String removeAccents(String)` (chỉ gọi, theo đúng cách lớp P1-04 đã khai báo).
- `NetworkMonitor`: `public static NetworkMonitor getInstance()`, thuộc tính `online`.
- Firestore: collection `users`; chỉ mục ghép `status`+`createdAt`, `province`+`createdAt`, `status`+`province`+`createdAt` (đã có trong "Chỉ mục ghép dự kiến").

### File được tạo hoặc sửa
- [sửa] PUBGApp/app/src/main/java/com/nhom3/pubgapp/ui/admin/AdminUserListFragment.java
- [tạo] PUBGApp/app/src/main/java/com/nhom3/pubgapp/ui/admin/AdminUserAdapter.java
- [tạo] PUBGApp/app/src/main/java/com/nhom3/pubgapp/ui/admin/AdminUserFilterRules.java
- [sửa] PUBGApp/app/src/main/res/layout/fragment_admin_user_list.xml
- [tạo] PUBGApp/app/src/main/res/layout/item_admin_user.xml
- [sửa] PUBGApp/app/src/main/res/values/strings_admin.xml
- [sửa] PUBGApp/app/src/main/res/navigation/nav_admin.xml (chỉ nếu thiếu hành động từ E02 sang E03 kèm tham số mã người dùng, và tham số boolean bật sẵn công tắc "Chờ xác thực" của E02)
- [sửa] PUBGApp/app/src/main/java/com/nhom3/pubgapp/viewmodel/AdminUserViewModel.java (chỉ thân `filterUsers`)
- [sửa] PUBGApp/app/src/main/java/com/nhom3/pubgapp/data/AdminRepository.java (chỉ thân `listUsers`)
- [tạo] PUBGApp/app/src/test/java/com/nhom3/pubgapp/ui/admin/AdminUserFilterRulesTest.java

Tên layout theo `so-huu.md` mục 2 và khung P1-06; nếu khung đã đặt tên khác thì theo khung.

### Các bước đề nghị
Có thể làm theo thứ tự khác, nhưng không được đổi tên lớp, phương thức, thuộc tính nào.
1. Tạo `AdminUserFilterRules` (lớp thuần Java, không dùng Android) với hai hàm tĩnh: `public static String maskPhone(String phone)` (giữ 3 chữ số cuối, phần còn lại thành `*`; `null` hoặc rỗng trả chuỗi rỗng) và `public static Date endOfDay(Date day)` (23:59:59.999 của chính ngày đó theo múi giờ máy). Viết `AdminUserFilterRulesTest` trước: số điện thoại ngắn hơn 3 chữ số, rỗng, `null`; ngày cuối tháng cho `endOfDay`.
2. `AdminRepository.listUsers`: truy vấn `users`, sắp `createdAt` giảm dần; thêm `whereEqualTo` trên `status` khi `status` khác `null`, `whereGreaterThanOrEqualTo` và `whereLessThanOrEqualTo` trên `createdAt` khi `from` và `to` khác `null`, `whereEqualTo` trên `province` khi chuỗi không rỗng; giới hạn số dòng bằng hằng `private static final int PAGE_SIZE` (Câu hỏi 2). Đổi tài liệu thành `User` bằng `toObject(User.class)`, dùng hằng tên trường của P1-03. Khi `keyword` không rỗng thì sau khi đọc, giữ lại những `User` có `displayName` hoặc `email` đã bỏ dấu (`FormatUtils.removeAccents`) chứa từ khóa đã bỏ dấu (Câu hỏi 1). Lỗi (kể cả thiếu chỉ mục) đi qua `fail(cb, e)`. Thiếu chỉ mục thì tạo theo đường dẫn Firestore báo, ghi vào PR.
3. `AdminUserViewModel.filterUsers(UserStatus status, Timestamp from, Timestamp to, String province, String keyword)`: bật `loading`, gọi `AdminRepository.listUsers`, đặt kết quả vào `users`, tắt `loading`; lỗi đặt câu tiếng Việt vào `error`. Không giữ tham chiếu tới Fragment hay View.
4. Layout `fragment_admin_user_list.xml`: ô tìm kiếm, hàng chip trạng thái (5 chip), hai ô chọn ngày, ô tỉnh hoặc thành phố, công tắc "Chờ xác thực", RecyclerView, `view_empty_state`, `view_error_state`, tiến trình đang tải, `view_offline_banner` bằng `include`. Màu, khoảng cách, cỡ chữ lấy từ token (`giao-dien.md` mục 1 đến 3); không viết mã màu hay số dp thẳng.
5. `item_admin_user.xml` và `AdminUserAdapter`: dùng `view_avatar` (chữ cái đầu của `displayName`), `view_status_chip` cho trạng thái tài khoản, thêm một chip "Cảnh báo" với `colorWarning` khi `warningCount` lớn hơn 0; số điện thoại qua `AdminUserFilterRules.maskPhone`. Adapter nhận danh sách đầy đủ và giữ danh sách đang hiện sau khi lọc; có một `OnUserClickListener` là lớp ẩn danh do Fragment truyền vào.
6. `AdminUserListFragment`: chọn ngày bằng `DatePickerDialog` của hệ thống (không thêm thư viện, R5); ngày "đến" đổi qua `AdminUserFilterRules.endOfDay` rồi thành `Timestamp`; mỗi lần đổi chip, ngày hoặc tỉnh thì gọi `filterUsers` với đủ năm điều kiện. Ô tìm kiếm dùng `OnEditorActionListener` là lớp ẩn danh cho phím Tìm kiếm của bàn phím (gọi `filterUsers` với từ khóa đang gõ) và một `TextWatcher` là lớp ẩn danh chỉ để gọi lại `filterUsers` với từ khóa rỗng khi ô được xóa trống; không gọi mạng ở mỗi chữ gõ để tiết kiệm lượt đọc Firestore. Công tắc "Chờ xác thực" lọc thêm theo `verificationStatus == VerificationStatus.PENDING` trên danh sách đã nhận (không có tham số cho nó ở `filterUsers`, Câu hỏi 1).
7. Giữ trạng thái qua xoay: danh sách nằm trong `users` của ViewModel; giá trị các ô lọc và công tắc ghi vào `onSaveInstanceState` rồi áp lại vào adapter mà không đọc lại mạng (ViewModel không có thuộc tính giữ bộ lọc).
8. Điều hướng: bấm dòng thì gọi hành động từ E02 sang E03 kèm mã người dùng (`User.getId()`); khi vào từ E01 với tham số boolean thì bật sẵn công tắc "Chờ xác thực".
9. Chuỗi trong `strings_admin.xml` theo mẫu `admin_user_list_<what>`: tên chip, nhãn ô lọc, "Chờ xác thực", "Cảnh báo", dòng báo không có người dùng nào khớp, câu lỗi, "Thử lại".
10. Thử trên máy ảo như mục "Tự kiểm trước khi mở Pull Request"; xoay màn hình; bật chế độ máy bay.
11. Chạy hai lệnh kiểm ở mục "Tự kiểm trước khi mở Pull Request" và ghi kết quả vào PR.

### File dùng chung cần thêm dòng
Không có. Ghi vào mô tả PR để P3-R gộp: luật đọc `users` của admin (security-rules.md mục 1, dòng Đọc: người đã đăng nhập đọc được); ba chỉ mục ghép của `users` đã liệt kê ở trên cần có trong `firebase/firestore.indexes.json`.

### Cấm đụng
- Leader-only không có trong danh sách trên: `AndroidManifest.xml`, `nav_main.xml`, `themes.xml`, `colors.xml`, `dimens.xml`, `strings_common.xml`, `AppDatabase`, `Converters`, mọi file Gradle, `firebase/`.
- Thân các phương thức khác của `AdminUserViewModel` và `AdminRepository` (thuộc P2-E2b, E2c, E2d, E7): `loadProfile`, `lock`, `unlock`, `warn`, `loadVerificationRequests`, `reviewVerification`, `loadActivityLog`, `loadAdmins`, `grantAdmin`, `revokeAdmin`, `lockUser`, `unlockUser`, `warnUser`...
- Module A: `UserRepository` (chỉ đọc `users` qua `AdminRepository`, không gọi `UserRepository.searchUsers`: đó là tìm người dùng cho người thường, STT 103).
- `FormatUtils` (chỉ gọi, không sửa).

### Phụ thuộc
P2-E1 (vào E02 từ E01), P1-04, P1-06.

### Ngoài phạm vi
- Hồ sơ chi tiết, tab lịch sử, nhật ký (P2-E2b); khóa, mở khóa, cảnh báo, gỡ tin (P2-E2c); duyệt xác thực (P2-E2d).
- Tìm người dùng theo số điện thoại, hoặc theo mã người dùng: không làm (UC34 chỉ tên hoặc email).
- Phân trang tải thêm: chờ Câu hỏi 2 (hiện chỉ một trang đầu).
- Ghi nhật ký lượt xem danh sách: không làm (STT 100 là Phát triển sau); lượt xem hồ sơ chi tiết xem Câu hỏi ở P2-E2b.
- Bố cục ngang: P3-E.

### Tài liệu cần cập nhật
Không có, trừ khi Câu hỏi 1 hoặc 2 được trả lời bằng đổi chữ ký: khi đó cập nhật `workflow/docs/architecture/dac-ta-lop/data.md#adminrepository` và `viewmodel.md#adminuserviewmodel`.

### Tự kiểm trước khi mở Pull Request
- [ ] Vào E02 với tài khoản admin: danh sách hiện, mới tham gia trước; mỗi dòng có chip trạng thái đúng màu.
- [ ] Chọn chip "Khóa tạm": chỉ còn tài khoản `TEMP_LOCKED`; chọn lại "Tất cả": đủ danh sách.
- [ ] Gõ một phần tên có dấu bằng chữ không dấu rồi bấm phím Tìm kiếm: dòng khớp còn lại, các dòng khác ẩn; xóa chữ thì đủ danh sách.
- [ ] Chọn khoảng ngày chứa đúng hôm nay làm ngày "đến": tài khoản tạo trong hôm nay vẫn có mặt.
- [ ] Bật "Chờ xác thực": chỉ còn người có yêu cầu xác thực chờ duyệt (tạo thử một yêu cầu bằng tài khoản thường nếu P2-D1 chưa xong, hoặc sửa tay `verificationStatus` trên Console cho tài khoản thử).
- [ ] Tắt mạng rồi bấm chip: hiện lỗi tiếng Việt và "Thử lại"; bật mạng rồi "Thử lại" tải được.
- [ ] `cd PUBGApp && ./gradlew testDebugUnitTest --tests "*AdminUserFilterRulesTest"` báo các test đều qua.
- [ ] `cd PUBGApp && ./gradlew assembleDebug testDebugUnitTest lintDebug` kết thúc `BUILD SUCCESSFUL`.
- [ ] `sh workflow/scripts/check-java-style.sh` không báo lỗi.
- [ ] Xoay màn hình khi đang có từ khóa, chip, ngày, tỉnh và công tắc: tất cả còn nguyên, danh sách không tải lại, vị trí cuộn giữ nguyên.
- [ ] Đã liệt kê trong PR mọi lớp, phương thức, trường công khai mới kèm dòng tài liệu định nghĩa (R16), hoặc ghi "không có".

### Quy mô
- [x] Đây là một lát cắt dọc của một module, khoảng nửa ngày đến một ngày, một Pull Request.

### Câu hỏi cho leader
1. `AdminRepository.listUsers` và `AdminUserViewModel.filterUsers` nay có tham số `keyword` ("từ khóa tên hoặc email"), nhưng tài liệu không nói cách khớp: Firestore không tìm chuỗi con trên `displayName` hay `email`, và `firestore-schema.md` không có chỉ mục nào cho việc này (chỉ mục `searchKeywords` dành cho `UserRepository.searchUsers`, tìm theo từ nguyên, không có email). Issue này đọc theo trạng thái, khoảng ngày, tỉnh rồi lọc từ khóa trên máy trong trang vừa đọc, nên người không nằm trong trang đã đọc sẽ không tìm thấy. Công tắc "Chờ xác thực" không có tham số nào, nên cũng lọc trên máy bằng `User.verificationStatus`. Xác nhận cách làm này, hoặc cho chỉ mục và cách khớp khác.
2. UC34 BR4 bắt phân trang khi danh sách dài, nhưng `listUsers` không có con trỏ. Tạm thời: đọc một trang đầu `PAGE_SIZE` dòng (hằng riêng, đề xuất 50) và không có "tải thêm". Đề xuất: thêm tham số con trỏ `Timestamp after` như `ListingRepository.getFeed`, hoặc xác nhận chỉ một trang cho đồ án.
3. Tài liệu không nói chọn tỉnh hoặc thành phố từ đâu (`ui/a-tai-khoan/README.md` chỉ ghi "Chọn tỉnh hoặc thành phố" ở A02, A05, A06 mà không nêu danh sách dùng chung nằm ở đâu). Tạm thời là ô nhập chữ, so khớp đúng chuỗi đã lưu trong `users.province`; nếu có danh sách tỉnh dùng chung thì nên dùng lại danh sách đó. Cho biết danh sách nằm ở đâu.
