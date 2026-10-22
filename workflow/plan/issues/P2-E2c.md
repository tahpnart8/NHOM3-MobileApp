TITLE: feat(admin): khóa, mở khóa, cảnh báo ba bậc, gỡ toàn bộ tin
LABELS: type:feature, area:admin, module:e, priority:p1
ASSIGNEES: tahpnart8
PHASE: P2

Plan item: P2-E2c (phần ba của P2-E2; xem P2-E2a về lý do tách)

### Bối cảnh
1. PUBGApp là ứng dụng Android mua bán, trao đổi và cho tặng đồ cũ kiêm mạng xã hội, viết bằng Java 17, giao diện XML với ViewBinding, dữ liệu trên Firebase Authentication và Cloud Firestore gói Spark, bộ nhớ đệm Room.
2. Module E (Quản trị, người làm Phát) gồm các màn E01 đến E07 chạy trong `AdminActivity` và việc chạy nền của giao dịch; quyền admin không bao giờ lấy từ phía ứng dụng (R12): admin là người có tài liệu `admins/{uid}`, `SessionManager.isAdmin()` chỉ để hiện hay ẩn giao diện, quyền thật do Security Rules quyết định.
3. Issue này thêm các nút hành động của màn E03 `AdminUserDetailFragment`: Cảnh báo theo ba bậc, Khóa tài khoản, Mở khóa, Gỡ toàn bộ tin (STT 70, 71, 72, 77, UC35).
4. Đọc trước: `workflow/docs/architecture/trang-thai.md` mục 7 (tài khoản, cảnh báo theo bậc) và mục 1 (dòng gỡ hàng loạt); `workflow/docs/ui/e-quan-tri/README.md` mục E03 (nút và Trạng thái đặc biệt); `workflow/docs/product/use-case/uc-e-quan-tri.md` mục UC35.
5. Đã có trên `main`: khung P1, P2-E1, P2-E2a, P2-E2b (E03 chỉ xem, hai khối trống chừa sẵn cho khối hành động và khối yêu cầu xác thực); lớp `Warning` với `resultingStatus()` và unit test của P1-03.

### Mã tính năng
F-14 (Admin người dùng, nội dung, danh mục, giao dịch, chuyển tiền tự động, tài khoản admin)

### Mã việc kế hoạch
P2-E2, tách thành P2-E2a, P2-E2b, P2-E2c (STT 70, 71, 72, 77), P2-E2d. Xem P2-E2a về lý do tách. Kế hoạch xếp STT 77 (gỡ hàng loạt) vào P2-E3, nhưng README E03 đặt nút "Gỡ toàn bộ tin" ở E03 nên issue này làm nó (phần giao diện ở E03, thân `removeAllListingsOf`).

### Module
E. Quản trị và nền móng (Phát)

### STT trong bảng đặc tả
70 (khóa), 71 (mở khóa), 72 (cảnh báo theo bậc), 77 (gỡ hàng loạt tin của tài khoản bị khóa)

### Màn hình mockup
E03 `AdminUserDetailFragment`, ảnh `workflow/docs/ui/e-quan-tri/e03-admin-chi-tiet-nguoi-dung.png`. README mục E03 thắng ảnh: lý do bắt buộc cả khi cảnh báo; có thêm bộ chọn "Thời hạn khóa"; nút "Mở khóa" cho cả khóa tạm và khóa vĩnh viễn; có nút "Gỡ toàn bộ tin". Dải cảnh báo màu cam nêu hậu quả theo ba bậc.

### Làm gì và vì sao
Ở E03 của một tài khoản đang hoạt động, admin nhập "Lý do" (bắt buộc, chỉ admin thấy) rồi bấm "Cảnh báo" hoặc "Khóa tài khoản". Cảnh báo theo ba bậc cố định (`trang-thai.md` mục 7, `luong-xu-ly.md` mục 7): lần 1 chỉ cảnh báo, tài khoản vẫn hoạt động; lần 2 khóa tạm đến hạn admin chọn ở "Thời hạn khóa"; lần 3 trở lên khóa vĩnh viễn; mỗi lần tăng `warningCount` và ghi một `Warning`. Mở khóa không đặt lại số đếm. "Khóa tài khoản" cho chọn khóa tạm theo số ngày hoặc khóa vĩnh viễn. Với tài khoản đang bị khóa, nút chính đổi thành "Mở khóa" và hiện nút "Gỡ toàn bộ tin": hệ thống không tự gỡ tin khi khóa, admin gỡ bằng nút này, mọi tin DRAFT, AVAILABLE, HIDDEN của tài khoản thành REMOVED. Người bị cảnh báo hoặc khóa nhận thông báo `WARNING`. Mọi ghi chỉ thành công khi Security Rules thấy người ghi là admin đang hoạt động (R12); nút ẩn hay hiện chỉ là giao diện. Về barem (`workflow/docs/product/barem-diem.md`): issue này phục vụ dòng R-Chapter 4 bằng input control (ô "Lý do", ô số ngày và công tắc "Vĩnh viễn" của bộ chọn "Thời hạn khóa", bốn nút) và hộp thoại xác nhận có nhập lý do (menu và picker); dòng R-Chapter 3 bằng unit test JUnit `AdminUserActionRulesTest`, trong đó quy tắc ba bậc lấy từ `Warning.resultingStatus()` (đã có test ở P1-03); dòng R-Chapter 1, 2, 6, 7 bằng `DataCallback` bất đồng bộ cho mọi lệnh ghi (chương 7, async) và `NetworkMonitor` khóa nút khi offline (chương 7, kiểm tra kết nối mạng).

### Tiêu chí nghiệm thu
- [ ] Cảnh báo lần 1 với Firebase thật: `users/{id}.warningCount` tăng từ 0 lên 1, `status` giữ `ACTIVE`, có thêm `users/{id}/warnings/{wid}` với `level` 1, `reason`, `issuedBy` là admin đang đăng nhập; người bị cảnh báo có thông báo `WARNING` trong `notifications`; tab "Lịch sử vi phạm" của E03 hiện thêm dòng mới.
- [ ] Cảnh báo lần 2 (bắt buộc chọn thời hạn): `status` thành `TEMP_LOCKED`, `lockedUntil` bằng hạn đã chọn, `lockReason` bằng lý do; lần 3 trở lên: `status` `PERM_LOCKED`, `lockedUntil` rỗng, `lockReason` bằng lý do. `warningCount` luôn tăng đúng 1 và `level` của `Warning` mới bằng `warningCount` sau khi cộng.
- [ ] Mở khóa một tài khoản bị khóa tạm hoặc vĩnh viễn: `status` về `ACTIVE`, `lockedUntil` và `lockReason` bị xóa, `warningCount` giữ nguyên; cảnh báo kế tiếp của tài khoản đó có `level` bằng `warningCount` cũ cộng 1 (ví dụ sau khi mở khóa vĩnh viễn ở `warningCount` 3, lần cảnh báo kế tiếp là bậc 4 và vẫn `PERM_LOCKED`).
- [ ] "Khóa tài khoản": chọn thời hạn theo số ngày thì `TEMP_LOCKED` kèm `lockedUntil` ở tương lai; chọn "Vĩnh viễn" thì `PERM_LOCKED` với `lockedUntil` rỗng; bắt buộc có lý do; người bị khóa nhận thông báo `WARNING`.
- [ ] Tài khoản bị khóa không đăng nhập được (kiểm bằng luồng của P2-A1 khi đã xong): hiện `lockReason` và hạn khóa rồi đăng xuất.
- [ ] "Gỡ toàn bộ tin" chỉ bấm được khi tài khoản đang `TEMP_LOCKED` hoặc `PERM_LOCKED`; có hộp thoại xác nhận; sau khi xác nhận mọi tin DRAFT, AVAILABLE, HIDDEN của tài khoản thành `REMOVED`, mọi offer `PENDING` của tin đang AVAILABLE thành `REJECTED` kèm thông báo `OFFER_RESULT` cho từng người mua; tin RESERVED, SOLD, REMOVED không bị đụng; chủ tin không có thông báo riêng (`trang-thai.md` mục 9). Tin REMOVED không khôi phục được.
- [ ] Ẩn hiện nút theo trạng thái: `ACTIVE` có "Cảnh báo" và "Khóa tài khoản"; `TEMP_LOCKED` và `PERM_LOCKED` có "Mở khóa" và "Gỡ toàn bộ tin"; `DELETED` không có nút nào.
- [ ] Mỗi đường lỗi hiện câu tiếng Việt, không im lặng: lý do để trống (báo dưới ô nhập, giữ nguyên form), lần 2 chưa chọn thời hạn, số ngày không hợp lệ, Rules từ chối, mất mạng, quá 15 giây. Khi offline các nút hành động bị khóa.
- [ ] Thông báo `WARNING` (và `OFFER_RESULT` ở "Gỡ toàn bộ tin") nằm trong cùng lô ghi với thay đổi trạng thái, qua `NotificationRepository.create(notification, batch, cb)`: không có ca nào mà `users/{id}` đã đổi nhưng thông báo chưa có.
- [ ] Trong lúc `loading` là true các nút và nút Xác nhận bị khóa; hộp thoại đóng khi `done` bật; câu lỗi tiếng Việt lấy từ `error`.
- [ ] Lý do, thời hạn đang nhập và hộp thoại đang mở giữ qua xoay màn hình; xoay không đọc lại mạng.
- [ ] Mọi thay đổi trạng thái tài khoản đi qua `AdminRepository.lockUser`, `unlockUser`, `warnUser` và theo đúng bảng `trang-thai.md` mục 7; trạng thái sau cảnh báo lấy từ `Warning.resultingStatus()`, không viết thẳng `status`; tin chỉ sang `REMOVED` theo bảng mục 1, qua `Listing.remove(String reason)` (R18).
- [ ] Unit test cho các hàm quy tắc của issue: `AdminUserActionRulesTest` (tên hàm ở bước 1 mục Các bước đề nghị).
- [ ] `./gradlew assembleDebug testDebugUnitTest lintDebug` và `sh workflow/scripts/check-java-style.sh` đều qua; PR ghi đúng lệnh và kết quả thật (R1).
- [ ] PR liệt kê mọi lớp, phương thức, trường công khai mới kèm dòng tài liệu định nghĩa (R16); `AdminUserActionRules` là lớp giao diện không có trong `dac-ta-lop`, ghi vào PR như lớp mới không có dòng tài liệu.

### Tài liệu phải đọc
- `AGENTS.md` mục 6: R12, R16, R17, R18, R20, R22, R23.
- `workflow/plan/so-huu.md` mục 2, 3 (E03), 5 (`AdminRepository`, `AdminUserViewModel`, `AdminContentViewModel` của Phát), 6.
- `workflow/docs/product/dac-ta-chuc-nang.md` dòng STT 70, 71, 72, 77.
- `workflow/docs/ui/e-quan-tri/README.md` mục E03 (nút, "Trạng thái đặc biệt", "Khác với ảnh").
- `workflow/docs/product/use-case/uc-e-quan-tri.md` mục UC35 (BR1 đến BR6) và UC37 (BR5).
- `workflow/docs/architecture/trang-thai.md` mục 1 (dòng gỡ hàng loạt, ghi chú mỗi lần tin rời AVAILABLE), mục 2 (dòng "Tin rời AVAILABLE"), mục 7, mục 9 (WARNING, OFFER_RESULT).
- `workflow/docs/architecture/luong-xu-ly.md` mục 7.
- `workflow/docs/architecture/dac-ta-lop/data.md#adminrepository` (dòng `lockUser`, `unlockUser`, `warnUser`, `removeAllListingsOf`), `#notificationrepository`; `viewmodel.md#adminuserviewmodel`, `#admincontentviewmodel`.
- `workflow/docs/architecture/dac-ta-lop/model.md#user`, `#warning`, `#listing`, `#offer`, `#appnotification`; `enums.md#userstatus`, `#listingstatus`, `#offerstatus`, `#notificationtype`.
- `workflow/docs/data/firestore-schema.md#users`, `#warnings`, `#listings`, `#offers`, `#notifications`, `#activitylogs`; `workflow/docs/data/security-rules.md` mục 1 (dòng "Khóa, mở khóa, cảnh báo"), 7, 10 (dòng "Admin gỡ"), 13 (dòng "Admin từ chối khi ẩn tin"), 21, 22.
- `workflow/docs/conventions/giao-dien.md` mục 1 đến 4 và 8; `workflow/memory/code/README.md`, `workflow/memory/code/firebase.md`, `workflow/docs/conventions/android-java.md`, skill `pubg-code`, skill `pubg-ui`.

### Lớp, trường, trạng thái sẽ dùng
- `AdminUserDetailFragment` (E03, khối hành động).
- `AdminUserViewModel`: thuộc tính `selectedUser`, `loading`, `error`, `done`; `public void lock(String userId, Timestamp until, String reason)`, `public void unlock(String userId)`, `public void warn(String userId, String reason, Timestamp until)` (thay thân).
- `AdminContentViewModel`: `public void removeAllOf(String userId)` (thay thân).
- `AdminRepository`: `public void lockUser(String id, Timestamp until, String reason, DataCallback<Void> cb)`, `public void unlockUser(String id, DataCallback<Void> cb)`, `public void warnUser(String id, Warning warning, Timestamp until, DataCallback<Void> cb)`, `public void removeAllListingsOf(String userId, DataCallback<Void> cb)` (thay thân).
- `BaseRepository`: `protected String currentUserId()`, `protected void logActivity(String action, ReportTarget targetType, String targetId)`, `protected void fail(DataCallback, Exception)`.
- `NotificationRepository`: `public void create(AppNotification notification, WriteBatch batch, DataCallback<Void> cb)` (chỉ repository gọi; truyền lô ghi đang dựng để thông báo nằm trong cùng lô, `null` khi ghi lẻ).
- `Warning`: `userId`, `level`, `reason`, `issuedBy`, `targetId`, `public UserStatus resultingStatus()`.
- `User`: `status`, `lockedUntil`, `lockReason`, `warningCount`, `isLocked()`.
- `Listing`: `status`, `statusReason`, `sellerId`, `public void remove(String reason)` (đặt REMOVED kèm lý do, chỉ admin gọi); `Offer`: `listingId`, `buyerId`, `conversationId`, `status`, `public void reject()`.
- `AppNotification`: `recipientId`, `type`, `title`, `body`, `targetId`, `isRead`.
- `UserStatus`: `ACTIVE`, `TEMP_LOCKED`, `PERM_LOCKED`, `DELETED`. `ListingStatus`: `DRAFT`, `AVAILABLE`, `HIDDEN`, `REMOVED`. `OfferStatus`: `PENDING`, `REJECTED`. `NotificationType`: `WARNING`, `OFFER_RESULT`. `ReportTarget.USER`.
- `NetworkMonitor`: `public static NetworkMonitor getInstance()`, thuộc tính `online`.
- Firestore: `users/{id}` (chỉ các khóa `status`, `lockedUntil`, `lockReason`, `warningCount`, `updatedAt`), `users/{id}/warnings/{wid}`, `listings/{id}` (chỉ `status`, `statusReason`, `updatedAt`), `offers/{id}` (chỉ `status`, `updatedAt`), `notifications`, `activityLogs`.

### File được tạo hoặc sửa
- [sửa] PUBGApp/app/src/main/java/com/nhom3/pubgapp/ui/admin/AdminUserDetailFragment.java
- [tạo] PUBGApp/app/src/main/java/com/nhom3/pubgapp/ui/admin/AdminUserActionRules.java
- [sửa] PUBGApp/app/src/main/res/layout/fragment_admin_user_detail.xml (điền khối hành động: dải cam, ô "Lý do", bộ chọn "Thời hạn khóa", bốn nút)
- [tạo] PUBGApp/app/src/main/res/layout/dialog_admin_user_action.xml
- [sửa] PUBGApp/app/src/main/res/values/strings_admin.xml
- [sửa] PUBGApp/app/src/main/java/com/nhom3/pubgapp/viewmodel/AdminUserViewModel.java (chỉ thân `lock`, `unlock`, `warn`)
- [sửa] PUBGApp/app/src/main/java/com/nhom3/pubgapp/viewmodel/AdminContentViewModel.java (chỉ thân `removeAllOf`)
- [sửa] PUBGApp/app/src/main/java/com/nhom3/pubgapp/data/AdminRepository.java (chỉ thân `lockUser`, `unlockUser`, `warnUser`, `removeAllListingsOf`)
- [tạo] PUBGApp/app/src/test/java/com/nhom3/pubgapp/ui/admin/AdminUserActionRulesTest.java

Tên layout theo `so-huu.md` mục 2 và khung P1-06; nếu khung đã đặt tên khác thì theo khung.

### Các bước đề nghị
Có thể làm theo thứ tự khác, nhưng không được đổi tên lớp, phương thức, thuộc tính nào.
1. Tạo `AdminUserActionRules` (thuần Java) với các hàm tĩnh: `public static boolean canWarn(UserStatus status)` và `canLock(UserStatus status)` (chỉ `ACTIVE`), `canUnlock(UserStatus status)` và `canRemoveAllListings(UserStatus status)` (chỉ `TEMP_LOCKED`, `PERM_LOCKED`), `public static boolean needsLockDuration(int warningCount)` (true khi lần cảnh báo kế tiếp, bậc `warningCount + 1`, có `Warning.resultingStatus()` là `TEMP_LOCKED`; dựng một `Warning`, đặt `level` bằng `warningCount + 1` rồi gọi `resultingStatus()`, không tự viết lại luật ba bậc), `public static boolean isReasonValid(String reason)` (không `null`, không toàn khoảng trắng), `public static Date lockUntil(long nowMillis, int days)` (trả `null` khi `days` nhỏ hơn 1, ngược lại là `nowMillis` cộng `days` ngày). Viết `AdminUserActionRulesTest` trước: đủ bốn trạng thái cho bốn hàm `can...`; `warningCount` 0, 1, 2, 3, 7 cho `needsLockDuration`; lý do `null`, rỗng, toàn khoảng trắng, hợp lệ; `days` 0, -1, 1, 30.
2. `AdminRepository.lockUser(String id, Timestamp until, String reason, DataCallback<Void> cb)`: từ chối nếu `reason` rỗng, nếu tài khoản không tồn tại hoặc `DELETED`; `until` khác `null` thì `TEMP_LOCKED` kèm `lockedUntil` (phải sau giờ hiện tại), `until` rỗng thì `PERM_LOCKED` với `lockedUntil` rỗng; ghi `lockReason`; một lô ghi chỉ các khóa `status`, `lockedUntil`, `lockReason`, `updatedAt` của `users/{id}` (đúng danh sách `onlyKeys` ở security-rules.md mục 1); thông báo `WARNING` cho người bị khóa đưa vào cùng lô bằng `NotificationRepository.create(notification, batch, cb)` (chờ `cb` báo xong rồi mới commit lô; cách `create` thêm vào lô cần đọc thân của P1, chưa kiểm chứng); sau khi lô thành công thì `logActivity`.
3. `AdminRepository.unlockUser(String id, DataCallback<Void> cb)`: đặt `ACTIVE`, xóa `lockedUntil` và `lockReason`, **không** đụng `warningCount`; từ chối nếu tài khoản không bị khóa.
4. `AdminRepository.warnUser(String id, Warning warning, Timestamp until, DataCallback<Void> cb)`: đọc `users/{id}` để lấy `warningCount`; đặt `warning.level` bằng `warningCount + 1`, `issuedBy` bằng `currentUserId()`, `userId` bằng `id`, `targetId` để rỗng; lấy `Warning.resultingStatus()` làm trạng thái mới (bậc 2 cần `until` khác `null` và ở tương lai, bậc 3 trở lên `lockedUntil` rỗng, bậc 1 giữ `ACTIVE` và không ghi `lockedUntil`); một lô ghi: `users/{id}` (`warningCount`, `status`, `lockedUntil`, `lockReason`, `updatedAt`) và `users/{id}/warnings/{wid}`; thông báo `WARNING` cho người bị cảnh báo đưa vào cùng lô bằng `NotificationRepository.create(notification, batch, cb)`; sau khi lô thành công thì `logActivity`. Từ chối nếu lý do rỗng hoặc tài khoản `DELETED`.
5. `AdminRepository.removeAllListingsOf(String userId, DataCallback<Void> cb)`: từ chối nếu tài khoản không ở `TEMP_LOCKED` hoặc `PERM_LOCKED`; đọc mọi tin của `userId` có `status` thuộc `DRAFT`, `AVAILABLE`, `HIDDEN` (Rules không cho gỡ trạng thái khác); với mỗi tin một lô ghi riêng (để không vượt giới hạn số lần đọc của một lô, security-rules.md "Luật chung" mục 7): gọi `Listing.remove(String reason)` với `reason` là `lockReason` của tài khoản (rỗng thì dùng một câu cố định "Tài khoản bị khóa", ghi một dòng quyết định bằng skill `pubg-record-decision`) rồi ghi chỉ `status`, `statusReason`, `updatedAt`; nếu tin đang AVAILABLE thì đọc mọi `offers` có `listingId` đó và `status` `PENDING`, đổi từng cái bằng `Offer.reject()` trong cùng lô và đưa thông báo `OFFER_RESULT` cho người mua vào lô bằng `NotificationRepository.create(notification, batch, cb)` (`targetId` là `conversationId`); chủ tin không có thông báo riêng (`trang-thai.md` mục 9). Quá 500 thao tác thì chia lô. Lỗi một tin không dừng các tin còn lại, nhưng phải báo tổng kết lỗi.
6. `AdminUserViewModel.lock`, `unlock`, `warn` và `AdminContentViewModel.removeAllOf`: gọi đúng một phương thức repository tương ứng ngoài luồng chính; `warn` **không** gọi `removeAllListingsOf` sau bậc 3 (quyết định 2026-10-22, UC35 BR2; chữ cũ còn ở `viewmodel.md` và `luong-xu-ly.md` mục 7: Câu hỏi 1); bật `loading` trong lúc chạy, thành công thì bật `done` rồi tải lại hồ sơ bằng `AdminUserViewModel.loadProfile(userId)` để E03 hiện trạng thái mới, lỗi thì đặt câu tiếng Việt vào `error`.
7. `fragment_admin_user_detail.xml`: điền khối hành động đã chừa: dải cam hậu quả lần cảnh báo kế tiếp (chữ khác nhau cho lần 1, lần 2, lần 3 trở lên theo `needsLockDuration` và `warningCount`), ô "Lý do", bộ chọn "Thời hạn khóa" (ô số ngày kiểu số nguyên và công tắc "Vĩnh viễn" cho khóa tay), bốn nút: "Cảnh báo" (phụ), "Khóa tài khoản" (chính, `colorError`), "Mở khóa", "Gỡ toàn bộ tin". Màu và khoảng cách theo token (`giao-dien.md` mục 1 đến 3).
8. `AdminUserDetailFragment`: hiện hoặc ẩn từng nút bằng bốn hàm `can...` của `AdminUserActionRules` theo `User.status` đang hiển thị; kiểm lý do bằng `isReasonValid`, đổi số ngày thành `Timestamp` bằng `lockUntil`; mỗi hành động mở hộp thoại xác nhận (`dialog_admin_user_action.xml`) tóm tắt hậu quả rồi mới gọi ViewModel. Khóa các nút khi `NetworkMonitor.getInstance().online` là false hoặc `loading` là true; đóng hộp thoại khi `done` bật rồi đặt lại; hiện `error` rồi xóa nó.
9. Chuỗi trong `strings_admin.xml` theo mẫu `admin_user_action_<what>`: nhãn nút, nhãn ô, ba chữ hậu quả của dải cam, câu hỏi xác nhận, câu lỗi cho từng đường lỗi nêu ở Tiêu chí nghiệm thu.
10. Thử trên máy ảo như mục "Tự kiểm trước khi mở Pull Request".
11. Chạy hai lệnh kiểm ở mục "Tự kiểm trước khi mở Pull Request" và ghi kết quả vào PR.

### File dùng chung cần thêm dòng
Không có. Ghi vào mô tả PR để P3-R gộp: luật "Khóa, mở khóa, cảnh báo" của `users` (security-rules.md mục 1), `warnings` (mục 7), "Admin gỡ" của `listings` (mục 10), "Admin từ chối khi ẩn tin" của `offers` (mục 13), tạo `notifications` (mục 21) và `activityLogs` (mục 22); chỉ mục `listings`: `sellerId`+`status`+`createdAt`.

### Cấm đụng
- Leader-only không có trong danh sách trên: `AndroidManifest.xml`, `nav_main.xml`, `nav_admin.xml`, `themes.xml`, `colors.xml`, `dimens.xml`, `strings_common.xml`, `AppDatabase`, `Converters`, mọi file Gradle, `firebase/`.
- Thân các phương thức khác của `AdminUserViewModel`, `AdminContentViewModel`, `AdminRepository` (thuộc việc khác của module E): `filterUsers`, `loadProfile`, `loadActivityLog`, `loadVerificationRequests`, `reviewVerification`, `hide`, `restore`, `remove`, `hideListing`, `restoreListing`, `removeListing`...
- `Warning.resultingStatus()` và `User` (lớp mô hình: không sửa, chỉ gọi).
- Module A: `AuthViewModel.login` (nơi chặn tài khoản đang khóa); module B: `ListingRepository` (đừng dùng để gỡ tin, mọi thay đổi trạng thái tin của admin nằm ở `AdminRepository`).
- `NotificationRepository.create` (chỉ gọi, không sửa).

### Phụ thuộc
P2-E2b (E03 và hai khối trống), P1-04, P1-06. Mềm: P2-A1 (kiểm đăng nhập bị chặn).

### Ngoài phạm vi
- Duyệt yêu cầu xác thực (P2-E2d). Đóng báo cáo của người bị xử lý: xem Câu hỏi 5 (E04 chỉ đóng báo cáo bằng nút "Bỏ qua" hoặc "Ẩn tin", P2-E3a).
- Hủy giao dịch đang dang dở của tài khoản bị khóa: không làm. Theo UC35 BR3 giao dịch đang chạy vẫn chạy tiếp, hai bên tự hủy hoặc mở khiếu nại như bình thường; `trang-thai.md` mục 3 không có dòng hủy vì khóa tài khoản.
- Khóa vĩnh viễn một tài khoản đang khóa tạm bằng nút "Khóa tài khoản": README E03 chỉ có "Mở khóa" cho tài khoản đang khóa, nên không làm; bảng `trang-thai.md` mục 7 cho phép chuyển này nhưng giao diện không mở lối đó.
- Cấu hình thời hạn khóa mặc định: Phát triển sau (`trang-thai.md` mục 7). Gỡ một tin đơn lẻ: P2-E3b.
- Bố cục ngang: P3-E.

### Tài liệu cần cập nhật
`workflow/docs/architecture/dac-ta-lop/viewmodel.md#adminuserviewmodel` (mô tả `warn`: bỏ vế "bậc 3 thì gỡ hàng loạt tin của tài khoản") và `workflow/docs/architecture/luong-xu-ly.md` mục 7 (bỏ bước 3), vì hai chỗ này còn chữ cũ trái với UC35 BR2 và `trang-thai.md` mục 1 (Câu hỏi 1). Sửa trong cùng Pull Request (R7).

### Tự kiểm trước khi mở Pull Request
- [ ] Cảnh báo ba lần liên tiếp một tài khoản thử: sau lần 1 vẫn `ACTIVE`, sau lần 2 là `TEMP_LOCKED` đến hạn đã chọn, sau lần 3 là `PERM_LOCKED`; `warningCount` là 3 và có ba dòng trong "Lịch sử vi phạm".
- [ ] Mở khóa tài khoản đó: `ACTIVE`, `warningCount` vẫn 3; cảnh báo thêm một lần: bậc 4, `PERM_LOCKED`.
- [ ] Khóa tay một tài khoản khác 7 ngày rồi mở khóa; khóa vĩnh viễn rồi mở khóa; mỗi lần có thông báo `WARNING` cho người đó (xem bằng tài khoản của họ ở C06 khi P2-A6 xong, hoặc trên Console).
- [ ] Gỡ toàn bộ tin của tài khoản đang khóa: tin DRAFT, AVAILABLE, HIDDEN thành `REMOVED` trên Console; tin AVAILABLE có offer `PENDING` thì offer thành `REJECTED`.
- [ ] Bấm "Cảnh báo" với lý do trống: lỗi dưới ô, không gọi mạng. Lần cảnh báo thứ hai không chọn hạn: lỗi, không ghi gì.
- [ ] Tắt mạng: bốn nút bị khóa.
- [ ] `cd PUBGApp && ./gradlew testDebugUnitTest --tests "*AdminUserActionRulesTest"` báo các test đều qua.
- [ ] `cd PUBGApp && ./gradlew assembleDebug testDebugUnitTest lintDebug` kết thúc `BUILD SUCCESSFUL`.
- [ ] `sh workflow/scripts/check-java-style.sh` không báo lỗi.
- [ ] Xoay màn hình khi hộp thoại đang mở và ô lý do đã gõ dở: hộp thoại và chữ còn nguyên.
- [ ] Đã liệt kê trong PR mọi lớp, phương thức, trường công khai mới kèm dòng tài liệu định nghĩa (R16), hoặc ghi "không có".

### Quy mô
- [x] Đây là một lát cắt dọc của một module, khoảng nửa ngày đến một ngày, một Pull Request.

### Câu hỏi cho leader
1. Hai tài liệu còn chữ cũ về bậc 3: `viewmodel.md` (`AdminUserViewModel.warn`: "bậc 3 thì gỡ hàng loạt tin của tài khoản") và `luong-xu-ly.md` mục 7 bước 3 ("Bậc 3 trở lên thì ViewModel gọi tiếp `removeAllListingsOf`"). Quyết định 2026-10-22, UC35 BR2, README E03 và `trang-thai.md` mục 1 nói hệ thống chỉ khóa, admin gỡ tin bằng nút "Gỡ toàn bộ tin". Issue này theo quyết định (`warn` không tự gỡ tin). Xác nhận và sửa hai chỗ chữ cũ.
2. Bậc 1 chỉ cảnh báo, tài khoản vẫn `ACTIVE`: có ghi `lockReason` không? `data.md` nói `warnUser` "ghi `lockReason`" cho mọi bậc, nhưng `lockReason` được hiện cho người bị khóa khi đăng nhập và bị xóa khi mở khóa. Tạm thời chỉ ghi `lockReason` ở bậc 2 trở lên; lý do bậc 1 nằm ở `Warning.reason` và trong thông báo. Xác nhận.
3. Chưa có tên hành động cho `logActivity(String action, ReportTarget targetType, String targetId)` của các thao tác admin; UC35 BR4 và NFR2 đòi ghi log mỗi quyết định nhưng `data.md` chỉ nói ghi nhật ký cho `grantAdmin`, `revokeAdmin`. Tạm thời ghi log cho cả bốn phương thức, theo mẫu `LISTING_CREATED`, `DEAL_CANCELLED` của `firestore-schema.md#activitylogs`: `USER_LOCKED`, `USER_UNLOCKED`, `USER_WARNED`, `USER_LISTINGS_REMOVED` với `targetType` là `ReportTarget.USER` và `targetId` là mã người bị xử lý. Bảng tên cho các việc khác của module E: `VERIFICATION_APPROVED`, `VERIFICATION_REJECTED` (P2-E2d, `ReportTarget.USER`); `LISTING_HIDDEN`, `LISTING_RESTORED`, `LISTING_REMOVED`, `LISTING_FLAG_CLEARED`, `REPORTS_CLOSED` (P2-E3a, E3b, `ReportTarget.LISTING`); `ADMIN_GRANTED`, `ADMIN_REVOKED` (P2-E7, `ReportTarget.USER`). Hai nhóm không có giá trị `ReportTarget` phù hợp (enum chỉ có `LISTING`, `USER`, `MESSAGE`): từ khóa cấm (`KEYWORD_ADDED`, `KEYWORD_REMOVED`) và danh mục (`CATEGORY_CREATED`, `CATEGORY_UPDATED`, `CATEGORY_DELETED`, P2-E4). Xác nhận bảng tên, và cho biết hai nhóm đó dùng `targetType` nào (hoặc bỏ ghi log ở đó). Lưu ý nhật ký ghi `userId` là admin thao tác (Rules), nên hiện ở "Nhật ký hoạt động" của admin, không của người bị xử lý.
4. Tiêu đề và nội dung thông báo là chữ tiếng Việt lưu vào Firestore, nhưng repository không có `Context` để đọc string resource. Tạm thời là hằng chuỗi tiếng Việt khai một lần trong `AdminRepository`. Xác nhận cách này cho mọi repository ghi thông báo (cũng là chỗ module khác sẽ gặp).
5. UC35 hậu điều kiện 1 và 3 nói báo cáo nhắm vào người dùng chuyển "Đã xử lý" khi admin cảnh báo hoặc khóa, và người báo cáo nhận thông báo kết quả (BR5), nhưng không có phương thức nào làm việc đó: `AdminContentViewModel.closeReports(String listingId, boolean dismissed)` đặt tên theo tin, và `trang-thai.md` mục 9 không có thông báo cho người báo cáo. Tạm thời cảnh báo hoặc khóa từ E03 không đóng báo cáo nào; báo cáo người dùng chỉ đóng bằng nút "Bỏ qua" ở E04. Xác nhận hoặc bổ sung.
6. Tài liệu không nói có cấm admin tự khóa, tự cảnh báo chính mình hoặc khóa một admin khác không. Tạm thời không chặn (Rules chỉ đòi người ghi đang `ACTIVE`). Xác nhận.
