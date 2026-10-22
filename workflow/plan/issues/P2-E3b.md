TITLE: feat(admin): chi tiết tin cho admin, khôi phục và gỡ tin
LABELS: type:feature, area:admin, module:e, priority:p1
ASSIGNEES: tahpnart8
PHASE: P2

Plan item: P2-E3b (phần hai của P2-E3; xem P2-E3a về lý do tách)

### Bối cảnh
1. PUBGApp là ứng dụng Android mua bán, trao đổi và cho tặng đồ cũ kiêm mạng xã hội, viết bằng Java 17, giao diện XML với ViewBinding, dữ liệu trên Firebase Authentication và Cloud Firestore gói Spark, bộ nhớ đệm Room.
2. Module E (Quản trị, người làm Phát) gồm các màn E01 đến E07 chạy trong `AdminActivity` và việc chạy nền của giao dịch; quyền admin không bao giờ lấy từ phía ứng dụng (R12): admin là người có tài liệu `admins/{uid}`, `SessionManager.isAdmin()` chỉ để hiện hay ẩn giao diện, quyền thật do Security Rules quyết định.
3. Issue này làm màn phụ `AdminListingDetailFragment` (chi tiết tin cho admin, mở từ E04 và từ tab "Tin đăng" của E03): xem đầy đủ tin, ẩn, khôi phục khi duyệt oan và gỡ tin (STT 76, UC37).
4. Đọc trước: `workflow/docs/ui/e-quan-tri/README.md` mục E04 (đoạn nói về `AdminListingDetailFragment`); `workflow/docs/architecture/trang-thai.md` mục 1 (các dòng admin ẩn, khôi phục, gỡ); `workflow/docs/product/use-case/uc-e-quan-tri.md` mục UC37.
5. Đã có trên `main`: khung P1, P2-E3a (tab Báo cáo, `AdminRepository.hideListing`, `AdminContentViewModel.hide`, `closeReports`, hộp thoại lý do `dialog_admin_reason`), P2-E1.

### Mã tính năng
F-14 (Admin người dùng, nội dung, danh mục, giao dịch, chuyển tiền tự động, tài khoản admin)

### Mã việc kế hoạch
P2-E3, tách thành P2-E3a, P2-E3b (STT 76, phần khôi phục và gỡ), P2-E3c. Xem P2-E3a về lý do tách.

### Module
E. Quản trị và nền móng (Phát)

### STT trong bảng đặc tả
76 (ẩn, gỡ tin vi phạm: có lý do, có thể khôi phục nếu duyệt oan)

### Màn hình mockup
`AdminListingDetailFragment`, chưa có ảnh. Làm theo README mục E04: bấm một dòng tin thì sang màn này để xem đầy đủ trước khi quyết định, có các nút "Ẩn tin" hoặc "Khôi phục"; không dùng B03 vì B03 là màn của người mua. Nút "Gỡ tin" nằm ở màn này theo README E04 và UC37 (`AdminRepository.removeListing`, `Listing.remove`), không nằm ở danh sách E04.

### Làm gì và vì sao
Admin mở một tin (từ dòng báo cáo ở E04, hoặc từ tab "Tin đăng" của hồ sơ ở E03) và thấy đủ nội dung tin: ảnh, tiêu đề, giá, mô tả, tình trạng và thuộc tính, hình thức giao dịch, khu vực, trạng thái và lý do ẩn nếu có, cờ từ khóa cấm nếu có; các lý do và mô tả của báo cáo đang chờ; mã chủ tin kèm lối sang E03 (tên và số lần chủ tin đã bị cảnh báo chờ Câu hỏi 1). Từ đây admin có thể: "Ẩn tin" (tin đang bán, lý do bắt buộc), "Khôi phục" (tin do admin ẩn, đưa về đang bán, xóa lý do), "Gỡ tin" (lý do bắt buộc; nặng hơn ẩn, **không khôi phục được**, người bán không tự mở lại). Tin do admin ẩn thì người bán không tự hiện lại được (`trang-thai.md` mục 1). Mọi nút chỉ là giao diện; admin hay không do Security Rules quyết định lúc ghi (R12). Về barem (`workflow/docs/product/barem-diem.md`): issue này phục vụ dòng R-Chapter 4 bằng input control (ô lý do bắt buộc trong `dialog_admin_reason`, ba nút "Ẩn tin", "Khôi phục", "Gỡ tin"), hộp thoại xác nhận (menu và picker) và điều hướng người dùng (nút quay lại, sang E03); dòng R-Chapter 1, 2, 6, 7 bằng `DataCallback` bất đồng bộ (chương 7, async) và `NetworkMonitor` khóa nút khi offline (chương 7, kiểm tra kết nối mạng); dòng R-Chapter 3 bằng unit test JUnit `AdminListingActionRulesTest`.

### Tiêu chí nghiệm thu
- [ ] Với Firebase thật: mở màn bằng mã tin thì đọc `listings/{id}` (bất kể trạng thái: AVAILABLE, HIDDEN, REMOVED, DRAFT, RESERVED, SOLD) và hiện các trường nêu ở "Làm gì và vì sao"; ảnh bìa lấy từ `Listing.coverThumb` (đã nằm trong tài liệu tin, giải mã ngoài luồng chính; ảnh hỏng hiện ảnh dự phòng, không làm sập màn); các ảnh đầy đủ theo `imageIds` đọc bằng `MediaRepository.getImage` khi Câu hỏi 1 được trả lời.
- [ ] Khối báo cáo hiện các báo cáo `OPEN` của tin (lấy từ `reports`, lọc theo `targetId` trên máy): nhãn lý do (`ReportReason`) và mô tả; khối chủ tin hiện mã chủ tin và nút sang E03 (tên và `warningCount` chờ Câu hỏi 1).
- [ ] "Ẩn tin" chỉ cho tin `AVAILABLE`: bắt buộc lý do; kết quả như P2-E3a (tin `HIDDEN` kèm `statusReason`, offer `PENDING` thành `REJECTED`, thông báo `LISTING_MODERATED`, báo cáo `OPEN` thành `RESOLVED`).
- [ ] "Khôi phục" chỉ cho tin `HIDDEN` có `statusReason` khác rỗng (do admin ẩn): tin về `AVAILABLE`, `statusReason` rỗng, chủ tin nhận thông báo `LISTING_MODERATED`. Tin `HIDDEN` do chủ tin tự ẩn (`statusReason` rỗng) không có nút này.
- [ ] "Gỡ tin" cho tin `DRAFT`, `AVAILABLE` hoặc `HIDDEN`: bắt buộc lý do, hộp thoại xác nhận nói rõ "không khôi phục được"; kết quả `listings/{id}.status` thành `REMOVED`, `statusReason` bằng lý do; offer `PENDING` của tin đang `AVAILABLE` thành `REJECTED` kèm thông báo `OFFER_RESULT`; chủ tin nhận thông báo `LISTING_MODERATED`; báo cáo `OPEN` của tin thành `RESOLVED`. Tin `REMOVED` hiện chỉ đọc, không nút nào.
- [ ] Tin `RESERVED` và `SOLD`: chỉ đọc, ba nút bị khóa kèm câu giải thích (Security Rules chỉ cho ẩn từ `AVAILABLE` và gỡ từ `DRAFT`, `AVAILABLE`, `HIDDEN`).
- [ ] Mọi nút cần lý do không gọi mạng khi lý do trống (báo lỗi dưới ô, giữ hộp thoại). Mỗi đường lỗi hiện câu tiếng Việt lấy từ `error`, không im lặng: tin đã bị chủ xóa hoặc không đọc được (hiện "Tin không còn"), Rules từ chối, mất mạng, quá 15 giây. Offline hoặc `loading` là true: nút bị khóa, dải offline hiện; hộp thoại đóng khi `done` bật.
- [ ] Tin hiện đang xem, ảnh đã giải mã, hộp thoại và lý do đang gõ giữ qua xoay màn hình; xoay không đọc lại mạng.
- [ ] Trạng thái tin chỉ đổi theo `trang-thai.md` mục 1 qua `AdminRepository.hideListing`, `restoreListing`, `removeListing`; dùng `Listing.hide(String reason)`, `Listing.restore()` và `Listing.remove(String reason)` (R18). Thông báo `LISTING_MODERATED` và `OFFER_RESULT` nằm trong cùng lô ghi (`NotificationRepository.create(notification, batch, cb)`).
- [ ] Unit test cho hàm quy tắc của issue: `AdminListingActionRulesTest` (tên hàm ở bước 1 mục Các bước đề nghị).
- [ ] `./gradlew assembleDebug testDebugUnitTest lintDebug` và `sh workflow/scripts/check-java-style.sh` đều qua; PR ghi đúng lệnh và kết quả thật (R1).
- [ ] PR liệt kê mọi lớp, phương thức, trường công khai mới kèm dòng tài liệu định nghĩa (R16); `AdminListingActionRules` là lớp giao diện không có trong `dac-ta-lop`, ghi vào PR như lớp mới không có dòng tài liệu.

### Tài liệu phải đọc
- `AGENTS.md` mục 6: R12, R16, R17, R18, R20, R22, R23.
- `workflow/plan/so-huu.md` mục 2, 3 (E04 gồm `AdminListingDetailFragment`), 5, 6.
- `workflow/docs/product/dac-ta-chuc-nang.md` dòng STT 76.
- `workflow/docs/ui/e-quan-tri/README.md` mục E04 (đoạn về màn chi tiết, "Trạng thái đặc biệt", "Khác với ảnh").
- `workflow/docs/product/use-case/uc-e-quan-tri.md` mục UC37 (luồng cơ bản bước 2 đến 4, BR2, BR4, BR5).
- `workflow/docs/architecture/trang-thai.md` mục 1 (các dòng admin ẩn, admin khôi phục, gỡ hàng loạt và đoạn ghi chú sau bảng), mục 2, mục 9.
- `workflow/docs/architecture/dac-ta-lop/data.md#adminrepository` (dòng `restoreListing`, `removeListing`, `hideListing`), `#listingrepository` (dòng `getById`), `#mediarepository` (dòng `getImage`), `#userrepository` (dòng `getProfile`), `#reportrepository` (dòng `getByStatus`); `viewmodel.md#admincontentviewmodel`.
- `workflow/docs/architecture/dac-ta-lop/model.md#listing`, `#report`, `#user`, `#offer`, `#appnotification`; `enums.md#listingstatus`, `#reportreason`, `#tradetype`, `#itemcondition`.
- `workflow/docs/data/firestore-schema.md#listings`; `workflow/docs/data/security-rules.md` mục 10 (dòng "Admin ẩn", "Admin khôi phục", "Admin gỡ", "Chặn riêng cho chủ tin"), 13, 18, 21.
- `workflow/docs/conventions/giao-dien.md` mục 1 đến 7 (nhãn trạng thái tin ở mục 4; ảnh ở mục 7) và 8; `workflow/memory/code/README.md`, `workflow/memory/code/firebase.md`, `workflow/docs/conventions/android-java.md`, skill `pubg-code`, skill `pubg-ui`.

### Lớp, trường, trạng thái sẽ dùng
- `AdminListingDetailFragment`.
- `AdminContentViewModel`: thuộc tính `selectedListing` (`LiveData<Listing>`), `reports` (`LiveData<List<Report>>`), `loading`, `error`, `done`; `public void loadListing(String listingId)`, `public void restore(String listingId)`, `public void remove(String listingId, String reason)` (thay thân); `public void loadReports(String status, ReportTarget targetType)`, `public void hide(String listingId, String reason)`, `public void closeReports(String listingId, boolean dismissed)` (đã làm ở P2-E3a, chỉ gọi).
- `AdminRepository`: `public void restoreListing(String id, DataCallback<Void> cb)`, `public void removeListing(String id, String reason, DataCallback<Void> cb)` (thay thân); `hideListing(String id, String reason, DataCallback<Void> cb)` (đã làm ở P2-E3a, chỉ gọi).
- Chỉ gọi (thân của người khác, có thể còn `NOT_READY`): `ListingRepository.getById(String id, DataCallback<Listing> cb)`, `ReportRepository.getByStatus(ReportStatus status, ReportTarget targetType, DataCallback<List<Report>> cb)` (qua `loadReports`). `MediaRepository.getImage(String id, DataCallback<byte[]> cb)` và `UserRepository.getProfile(String uid, DataCallback<User> cb)` không có trong "Dùng tới" của `AdminContentViewModel` nên chưa gọi được từ màn này: Câu hỏi 1.
- `BaseRepository`: `currentUserId()`, `logActivity(String action, ReportTarget targetType, String targetId)`, `fail(...)`. `NotificationRepository.create(AppNotification notification, WriteBatch batch, DataCallback<Void> cb)` (chỉ repository gọi).
- `Listing`: `sellerId`, `title`, `description`, `imageIds`, `coverThumb`, `itemCondition`, `conditionValues`, `tradeTypes`, `price`, `province`, `status`, `statusReason`, `flagged`; `public void hide(String reason)`, `public void restore()`, `public void remove(String reason)`. `Offer`: `listingId`, `buyerId`, `conversationId`, `status`, `public void reject()`. `Report`: `reason`, `description`, `status`, `targetId`.
- `ListingStatus`: `DRAFT`, `AVAILABLE`, `RESERVED`, `SOLD`, `HIDDEN`, `REMOVED`. `ReportReason`. `ReportTarget.LISTING`. `OfferStatus`: `PENDING`, `REJECTED`. `NotificationType`: `LISTING_MODERATED`, `OFFER_RESULT`.
- `FormatUtils`: `public String formatPrice(long)` (chỉ gọi). `NetworkMonitor`: `public static NetworkMonitor getInstance()`, thuộc tính `online`.
- Firestore: `listings/{id}` (admin chỉ đổi `status`, `statusReason`, `updatedAt`), `offers/{id}` (chỉ `status`, `updatedAt`), `reports`, `notifications`, `activityLogs`.

### File được tạo hoặc sửa
- [sửa] PUBGApp/app/src/main/java/com/nhom3/pubgapp/ui/admin/AdminListingDetailFragment.java
- [tạo] PUBGApp/app/src/main/java/com/nhom3/pubgapp/ui/admin/AdminListingActionRules.java
- [sửa] PUBGApp/app/src/main/res/layout/fragment_admin_listing_detail.xml
- [sửa] PUBGApp/app/src/main/res/values/strings_admin.xml
- [sửa] PUBGApp/app/src/main/java/com/nhom3/pubgapp/viewmodel/AdminContentViewModel.java (chỉ thân `loadListing`, `restore`, `remove`)
- [sửa] PUBGApp/app/src/main/java/com/nhom3/pubgapp/data/AdminRepository.java (chỉ thân `restoreListing`, `removeListing`)
- [tạo] PUBGApp/app/src/test/java/com/nhom3/pubgapp/ui/admin/AdminListingActionRulesTest.java

Dùng lại `dialog_admin_reason.xml` do P2-E3a tạo. Tên layout theo `so-huu.md` mục 2 và khung P1-06; nếu khung đã đặt tên khác thì theo khung.

### Các bước đề nghị
Có thể làm theo thứ tự khác, nhưng không được đổi tên lớp, phương thức, thuộc tính nào.
1. Tạo `AdminListingActionRules` (thuần Java) với các hàm tĩnh: `public static boolean canHide(ListingStatus status)` (chỉ `AVAILABLE`), `public static boolean canRestore(ListingStatus status, String statusReason)` (`HIDDEN` và `statusReason` khác `null` và khác rỗng), `public static boolean canRemove(ListingStatus status)` (`DRAFT`, `AVAILABLE`, `HIDDEN`). Viết `AdminListingActionRulesTest` trước: đủ sáu giá trị `ListingStatus` cho `canHide` và `canRemove`; `HIDDEN` với lý do `null`, rỗng, có chữ và `AVAILABLE` có lý do cho `canRestore`.
2. `AdminRepository.restoreListing(String id, DataCallback<Void> cb)`: đọc tin, từ chối nếu không `HIDDEN` hoặc `statusReason` rỗng (điều kiện chặt hơn tài liệu: Câu hỏi 3); đọc `users/{sellerId}` và từ chối nếu chủ tin `DELETED`; gọi `Listing.restore()` rồi ghi chỉ `status`, `statusReason`, `updatedAt`; thông báo `LISTING_MODERATED` cho chủ tin đưa vào cùng lô bằng `NotificationRepository.create(notification, batch, cb)` (cách `create` thêm vào lô cần đọc thân của P1, chưa kiểm chứng); sau khi ghi thành công thì `logActivity`.
3. `AdminRepository.removeListing(String id, String reason, DataCallback<Void> cb)`: từ chối nếu `reason` rỗng hoặc tin không ở `DRAFT`, `AVAILABLE`, `HIDDEN`; gọi `Listing.remove(reason)` rồi một lô ghi: `listings/{id}` chỉ `status` (thành `REMOVED`), `statusReason`, `updatedAt`; nếu tin đang `AVAILABLE` thì mọi `offers` có `listingId` đó và `status == PENDING` đổi bằng `Offer.reject()` trong cùng lô (chỉ `status`, `updatedAt`); thông báo `LISTING_MODERATED` cho chủ tin và `OFFER_RESULT` cho từng người mua (`targetId` là `conversationId`) đưa vào cùng lô bằng `NotificationRepository.create(notification, batch, cb)`, chờ `cb` rồi mới commit lô; sau lô thành công thì `logActivity`.
4. `AdminContentViewModel`: `loadListing(String listingId)` gọi `ListingRepository.getById` và đặt vào `selectedListing`; báo cáo của tin lấy từ `reports` sau `loadReports(ReportStatus.OPEN.name(), ReportTarget.LISTING)` rồi lọc theo `targetId` trên máy (kiểu `String` của tham số trạng thái: Câu hỏi 1 của P2-E3a); `restore(String listingId)` và `remove(String listingId, String reason)` gọi repository tương ứng ngoài luồng chính, `remove` thành công thì gọi tiếp `closeReports(listingId, false)`; xong thì bật `done` và tải lại tin để màn hiện trạng thái mới; `loading` bật trong lúc chạy, lỗi đặt câu tiếng Việt vào `error`.
5. Layout `fragment_admin_listing_detail.xml`: `view_detail_toolbar` (nút quay lại), vùng ảnh cuộn ngang với ảnh giữ chỗ và ảnh dự phòng, khối thông tin tin, nhãn trạng thái (`view_status_chip`) cùng dòng lý do khi có, khối báo cáo, khối chủ tin, ba nút ("Ẩn tin", "Khôi phục", "Gỡ tin"; "Gỡ tin" dùng `colorError`), `view_empty_state` cho "Tin không còn", `view_error_state`, tiến trình đang tải, `view_offline_banner`. Màu, khoảng cách, cỡ chữ theo token (`giao-dien.md` mục 1 đến 3). Giá qua `FormatUtils.formatPrice`.
6. `AdminListingDetailFragment`: lấy mã tin từ tham số điều hướng, tải tin một lần; bật hoặc khóa từng nút bằng `AdminListingActionRules` theo `status`, `statusReason`, `NetworkMonitor.getInstance().online` và `loading`; mỗi nút mở `dialog_admin_reason` (lý do bắt buộc, trừ "Khôi phục" chỉ cần hộp xác nhận); "Gỡ tin" có thêm câu cảnh báo không khôi phục được. Ảnh bìa giải mã ngoài luồng chính; các ảnh đầy đủ chờ Câu hỏi 1.
7. Điều hướng: nút "Xem người dùng" ở khối chủ tin sang E03 kèm `sellerId`; quay lại về màn đã gọi (E04 hoặc E03).
8. Chuỗi trong `strings_admin.xml` theo mẫu `admin_listing_detail_<what>`: nhãn khối, ba nút, hộp xác nhận, câu giải thích khi nút bị khóa, "Tin không còn", câu lỗi, "Thử lại".
9. Thử trên máy ảo: ẩn, khôi phục, rồi gỡ ba tin khác nhau; mở tin RESERVED; xoay màn hình; bật chế độ máy bay.
10. Chạy hai lệnh kiểm ở mục "Tự kiểm trước khi mở Pull Request" và ghi kết quả vào PR.

### File dùng chung cần thêm dòng
Không có. Ghi vào mô tả PR để P3-R gộp: luật "Admin ẩn", "Admin khôi phục", "Admin gỡ" của `listings` (security-rules.md mục 10), "Admin từ chối khi ẩn tin" của `offers` (mục 13), tạo `notifications` (mục 21).

### Cấm đụng
- Leader-only không có trong danh sách trên: `AndroidManifest.xml`, `nav_main.xml`, `nav_admin.xml`, `themes.xml`, `colors.xml`, `dimens.xml`, `strings_common.xml`, `AppDatabase`, `Converters`, mọi file Gradle, `firebase/`. Nếu thiếu hành động điều hướng tới màn này thì đã được P2-E3a thêm; nếu vẫn thiếu thì ghi vào issue và báo, không tự sửa `nav_admin.xml` ngoài danh sách (R19).
- Thân các phương thức khác của `AdminContentViewModel`, `AdminRepository` (E2c, E3a, E3c, E4): `hide`, `hideListing`, `removeAllOf`, `removeAllListingsOf`, `clearFlag`, `addKeyword`...
- Module B (Tú): màn B03 `ListingDetailFragment` và `ListingRepository` (chỉ gọi `getById`). Module D: `ReportRepository` phía người dùng.
- `Listing`, `Offer` (lớp mô hình: chỉ gọi, không thêm phương thức).

### Phụ thuộc
P2-E3a, P1-04, P1-06. Mềm: P2-E2b (lối vào từ tab "Tin đăng" của E03), P2-B1 (tin thật). Phần ảnh đầy đủ, tên chủ tin và `warningCount` bị chặn bởi Câu hỏi 1 (R20: thiếu phụ thuộc thì gắn nhãn `blocked` cho phần đó); phần còn lại làm được ngay.

### Ngoài phạm vi
- Sửa nội dung tin, đổi giá, đổi danh mục của người bán: không làm. Bỏ cờ từ khóa cấm: P2-E3c. Gỡ toàn bộ tin của tài khoản bị khóa: P2-E2c.
- Khóa hay cảnh báo chủ tin: E03 (P2-E2c); màn này chỉ dẫn sang.
- Xử lý giao dịch gắn với tin RESERVED: xem Câu hỏi 6 của P2-E3a (UC37 ngoại lệ 4a).
- Bố cục ngang: P3-E.

### Tài liệu cần cập nhật
`workflow/docs/architecture/trang-thai.md` mục 9: dòng `LISTING_MODERATED` chỉ nêu `hideListing` và `restoreListing`, còn `data.md#adminrepository` ghi `removeListing` cũng gửi `LISTING_MODERATED` (và từ chối offer `PENDING` kèm `OFFER_RESULT`); thêm `AdminRepository.removeListing` vào hai dòng đó trong cùng Pull Request (R7, Câu hỏi 2). Ngoài ra không có, trừ khi Câu hỏi 1 hoặc 3 được trả lời bằng đổi tài liệu: khi đó cập nhật `viewmodel.md#admincontentviewmodel` hoặc `data.md#adminrepository`.

### Tự kiểm trước khi mở Pull Request
- [ ] Mở một tin `AVAILABLE` từ dòng báo cáo: thấy ảnh, mô tả, báo cáo đang chờ, tên chủ tin và `warningCount`.
- [ ] "Ẩn tin" với lý do: tin `HIDDEN` kèm lý do; mở lại màn thì thấy nút "Khôi phục" thay "Ẩn tin"; "Khôi phục": tin về `AVAILABLE`, lý do rỗng.
- [ ] "Gỡ tin" ở một tin khác: sau khi xác nhận, `REMOVED` trên Console, offer `PENDING` (nếu có) thành `REJECTED`, mở lại màn thì không còn nút nào.
- [ ] Mở tin `RESERVED`: ba nút bị khóa kèm câu giải thích.
- [ ] Để trống lý do ở "Ẩn tin" và "Gỡ tin": báo lỗi, không ghi gì.
- [ ] Tắt mạng: nút bị khóa; bật mạng rồi "Thử lại".
- [ ] `cd PUBGApp && ./gradlew testDebugUnitTest --tests "*AdminListingActionRulesTest"` báo các test đều qua.
- [ ] `cd PUBGApp && ./gradlew assembleDebug testDebugUnitTest lintDebug` kết thúc `BUILD SUCCESSFUL`.
- [ ] `sh workflow/scripts/check-java-style.sh` không báo lỗi.
- [ ] Xoay màn hình khi hộp thoại mở và ảnh đã tải: ảnh, hộp thoại, chữ đang gõ còn nguyên, không đọc lại ảnh từ mạng.
- [ ] Đã liệt kê trong PR mọi lớp, phương thức, trường công khai mới kèm dòng tài liệu định nghĩa (R16), hoặc ghi "không có".

### Quy mô
- [x] Đây là một lát cắt dọc của một module, khoảng nửa ngày đến một ngày, một Pull Request.

### Câu hỏi cho leader
1. `AdminContentViewModel` có `selectedListing` và `loadListing` cho màn này, nhưng "Dùng tới" của nó không có `MediaRepository` và `UserRepository`, và không có thuộc tính nào cho ảnh đầy đủ của tin hay hồ sơ chủ tin. Hệ quả: màn chỉ hiện ảnh bìa (`Listing.coverThumb`) và mã chủ tin (không có tên, không có `warningCount`). Đề xuất: thêm hai phụ thuộc đó kèm thuộc tính nhận kết quả; tên do leader chốt. Việc này chặn phần ảnh đầy đủ và khối chủ tin.
2. `trang-thai.md` mục 9 chưa nêu `AdminRepository.removeListing` ở các dòng `LISTING_MODERATED` và `OFFER_RESULT`, trong khi `data.md#adminrepository` ghi `removeListing` gửi `LISTING_MODERATED` cho người đăng (và `trang-thai.md` mục 1, mục 2 nói tin rời AVAILABLE thì từ chối offer `PENDING` kèm thông báo). Issue này theo `data.md` và mục 1. Xác nhận và sửa mục 9.
3. `restoreListing` mô tả "Đưa tin HIDDEN về AVAILABLE", còn Rules cho admin khôi phục khi sau là `AVAILABLE` với `statusReason == ''`, không đòi `statusReason` trước phải khác rỗng. Tạm thời chỉ cho khôi phục khi `statusReason` khác rỗng (tin do admin ẩn); và từ chối khi chủ tin đã `DELETED` (tin ẩn do vô hiệu hóa có `statusReason` "Tài khoản đã xóa"). Hai điều kiện này không có trong tài liệu; xác nhận.
4. UC37 bước 2 đòi hiện "lịch sử vi phạm của chủ tin". Tạm thời chỉ có lối sang E03 (nơi có lịch sử vi phạm đầy đủ). Xác nhận.
