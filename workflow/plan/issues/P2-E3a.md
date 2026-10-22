TITLE: feat(admin): kiểm duyệt, danh sách báo cáo và ẩn tin
LABELS: type:feature, area:admin, module:e, priority:p1
ASSIGNEES: tahpnart8
PHASE: P2

Plan item: P2-E3a (phần đầu của P2-E3; các phần sau là P2-E3b, P2-E3c)

### Bối cảnh
1. PUBGApp là ứng dụng Android mua bán, trao đổi và cho tặng đồ cũ kiêm mạng xã hội, viết bằng Java 17, giao diện XML với ViewBinding, dữ liệu trên Firebase Authentication và Cloud Firestore gói Spark, bộ nhớ đệm Room.
2. Module E (Quản trị, người làm Phát) gồm các màn E01 đến E07 chạy trong `AdminActivity` và việc chạy nền của giao dịch; quyền admin không bao giờ lấy từ phía ứng dụng (R12): admin là người có tài liệu `admins/{uid}`, `SessionManager.isAdmin()` chỉ để hiện hay ẩn giao diện, quyền thật do Security Rules quyết định.
3. Issue này làm màn E04 `AdminModerationFragment` với tab "Báo cáo": danh sách báo cáo theo loại đối tượng và trạng thái, bỏ qua báo cáo, ẩn tin có lý do (STT 75, 76, UC37).
4. Đọc trước: `workflow/docs/ui/e-quan-tri/README.md` mục E04; `workflow/docs/architecture/trang-thai.md` mục 5 (báo cáo) và mục 1 (dòng admin ẩn tin); `workflow/docs/product/use-case/uc-e-quan-tri.md` mục UC37.
5. Đã có trên `main`: khung P1 (Fragment E04 trống, `nav_admin.xml`, `strings_admin.xml`, `view_*`), P2-E1 (E01 mở E04); phía người dùng gửi báo cáo là P2-D4 của Phúc.

### Mã tính năng
F-14 (Admin người dùng, nội dung, danh mục, giao dịch, chuyển tiền tự động, tài khoản admin)

### Mã việc kế hoạch
P2-E3, tách thành P2-E3a (tab Báo cáo, ẩn tin: STT 75 và nửa đầu của 76), P2-E3b (chi tiết tin cho admin, khôi phục, gỡ tin: nửa sau của 76), P2-E3c (tab Tin bị gắn cờ và Từ khóa cấm: STT 78). Lý do tách: một màn có ba tab, hai màn con, ba nhóm quy tắc ghi khác nhau (`reports`, `listings` và `offers`, `bannedKeywords`); gộp thì vượt một ngày. STT 77 (gỡ hàng loạt) không nằm ở đây mà ở P2-E2c vì nút nằm ở E03.

### Module
E. Quản trị và nền móng (Phát)

### STT trong bảng đặc tả
75 (danh sách tin bị báo cáo, kèm số lượt, lý do; nhóm theo đối tượng gồm cả người dùng và tin nhắn theo `trang-thai.md` mục 5), 76 (phần ẩn tin có lý do)

### Màn hình mockup
E04 `AdminModerationFragment`, ảnh `workflow/docs/ui/e-quan-tri/e04-admin-kiem-duyet-noi-dung.png` (chỉ vẽ tab báo cáo tin đăng). README mục E04 thắng ảnh: không có nút "Gỡ tin" trên dòng; ba nút lọc trạng thái báo cáo là "Chờ xử lý", "Đã xử lý (có vi phạm)", "Đã xem (không vi phạm)"; thêm bộ lọc loại đối tượng; "Bỏ qua" không đổi trạng thái tin mà đóng các báo cáo còn chờ.

### Làm gì và vì sao
Admin vào E04 từ E01 (dòng báo cáo hoặc ô "Tin bị báo cáo") và thấy tab "Báo cáo": bộ lọc loại đối tượng (Tin đăng, Người dùng, Tin nhắn), ba nút lọc trạng thái, rồi danh sách báo cáo gom theo đối tượng và sắp theo số lượt báo cáo giảm dần để thấy đối tượng bị báo nhiều nhất trước (hệ thống không tự ẩn tin khi bị báo nhiều, UC37 BR3); mỗi dòng có ảnh thu nhỏ, tên tin (hoặc tên người dùng, nội dung tin nhắn), các lý do báo cáo (màu đỏ), số lượt báo cáo (STT 75). Với báo cáo về tin: "Bỏ qua" đóng mọi báo cáo còn chờ của tin đó thành "Đã xem, không vi phạm" (tin không đổi trạng thái); "Ẩn tin" bắt nhập lý do, đặt tin sang HIDDEN kèm `statusReason`, từ chối các offer còn chờ, thông báo cho người đăng và đóng các báo cáo thành "Đã xử lý, có vi phạm" (STT 76). Với báo cáo về người dùng hoặc tin nhắn: "Bỏ qua" và "Xem người dùng" (sang E03 của người bị báo cáo). Tin do admin ẩn thì chủ tin không tự hiện lại được (`trang-thai.md` mục 1). Quyền đọc `reports` và đổi tin là của admin do Security Rules quyết định, không do cờ trong ứng dụng (R12). Bấm một dòng tin mở `AdminListingDetailFragment` (P2-E3b), nơi duy nhất có nút "Gỡ tin"; danh sách này chỉ có "Ẩn tin". Dữ liệu danh sách lấy từ `AdminContentViewModel.reports` (báo cáo theo trạng thái và loại đối tượng) và `reportedListings` (tin bị báo cáo); trạng thái đang tải và câu lỗi lấy từ `loading` và `error`. Về barem (`workflow/docs/product/barem-diem.md`): issue này phục vụ dòng R-Chapter 4 bằng RecyclerView (`AdminReportAdapter`), input control (chip loại đối tượng, ba nút lọc trạng thái, ô lý do), hộp thoại nhập lý do ẩn tin (menu và picker) và điều hướng người dùng (TabLayout của E04, sang E03 và `AdminListingDetailFragment`); dòng R-Chapter 1, 2, 6, 7 bằng `DataCallback` bất đồng bộ (chương 7, async) và `NetworkMonitor` (chương 7, kiểm tra kết nối mạng); dòng R-Chapter 3 bằng unit test JUnit `AdminReportGroupingTest`.

### Tiêu chí nghiệm thu
- [ ] Với Firebase thật: mở tab "Báo cáo" thì `loadReports(status, targetType)` đọc `reports` theo trạng thái đang chọn (mặc định "Chờ xử lý" là `OPEN`; "Đã xử lý (có vi phạm)" là `RESOLVED`; "Đã xem (không vi phạm)" là `DISMISSED`) và loại đối tượng đang chọn (`LISTING`, `USER`, `MESSAGE`; không chọn loại nào là `null`, mọi loại); các báo cáo cùng `targetType` và `targetId` gom thành một dòng; danh sách nhóm sắp theo số lượt báo cáo giảm dần, cùng số lượt thì báo cáo mới nhất trước.
- [ ] Mỗi dòng nhóm hiện: tên đối tượng (tên tin; tên người dùng và nội dung tin nhắn chờ Câu hỏi 2, trước mắt hiện mã), ảnh thu nhỏ khi là tin (`Listing.coverThumb`), các lý do báo cáo đã bỏ trùng bằng chữ tiếng Việt (nhãn `ReportReason`, màu `colorError`), số lượt báo cáo trong nhóm. Báo cáo có `evidenceImageIds` hiện thêm số ảnh người báo cáo đính kèm (tối đa 3); chỗ hiện chính các ảnh đó và mở đoạn chat của báo cáo tin nhắn (`Report.conversationId`) chờ Câu hỏi 3.
- [ ] Dòng báo cáo về tin còn chờ có hai nút "Bỏ qua" và "Ẩn tin"; dòng báo cáo về người dùng có "Bỏ qua" và "Xem người dùng"; dòng báo cáo về tin nhắn chỉ có "Bỏ qua" cho tới khi Câu hỏi 3 được trả lời. Nhóm đã xử lý hoặc đã xem chỉ để đọc, không có nút; mỗi dòng có nhãn trạng thái báo cáo bằng `view_status_chip` đúng nhãn và màu ở `giao-dien.md` mục 4 (Báo cáo: Chờ xử lý, Đã xử lý có vi phạm, Đã xem không vi phạm).
- [ ] "Bỏ qua": mọi báo cáo `OPEN` của đối tượng thành `DISMISSED`, `handledBy` là admin đang đăng nhập, danh sách làm mới; tin giữ nguyên trạng thái.
- [ ] "Ẩn tin": bắt buộc lý do (để trống thì báo dưới ô, giữ hộp thoại); sau xác nhận `listings/{id}.status` thành `HIDDEN`, `statusReason` bằng lý do; mọi offer `PENDING` của tin thành `REJECTED` kèm thông báo `OFFER_RESULT` cho từng người mua; người đăng nhận thông báo `LISTING_MODERATED`; mọi báo cáo `OPEN` của tin thành `RESOLVED` với `handledBy` là admin. Người bán mở tin của mình thấy lý do và không hiện lại được.
- [ ] Tin không ở `AVAILABLE` (đang giữ chỗ, đã bán, đã ẩn, đã gỡ) thì "Ẩn tin" bị khóa kèm câu báo (Rules chỉ cho ẩn từ AVAILABLE, Câu hỏi 6).
- [ ] Tin đã bị chủ xóa hoặc không đọc được: dòng hiện "Tin không còn" và vẫn cho "Bỏ qua".
- [ ] Mỗi đường lỗi hiện câu tiếng Việt lấy từ `error`, không im lặng: đọc danh sách lỗi (kể cả thiếu chỉ mục), đóng báo cáo lỗi, ẩn tin bị Rules từ chối, mất mạng, quá 15 giây. Không có báo cáo nào: dòng báo trống "Không có báo cáo nào đang chờ xử lý". Khi offline hoặc khi `loading` là true các nút hành động bị khóa và dải offline hiện; hộp thoại lý do đóng khi `done` bật.
- [ ] Bộ lọc đang chọn, danh sách đã tải, hộp thoại và lý do đang gõ giữ qua xoay màn hình; xoay không đọc lại mạng.
- [ ] Chuyển trạng thái chỉ theo `trang-thai.md`: báo cáo OPEN sang RESOLVED hoặc DISMISSED qua `Report.resolve`, `Report.dismiss` và `ReportRepository.closeByTarget`; tin AVAILABLE sang HIDDEN qua `AdminRepository.hideListing` (dùng `Listing.hide(String reason)`), không viết thẳng `status` (R18). Thông báo `LISTING_MODERATED` và `OFFER_RESULT` nằm trong cùng lô ghi (`NotificationRepository.create(notification, batch, cb)`).
- [ ] Unit test cho hàm gom nhóm và sắp xếp báo cáo (số lượt, lý do bỏ trùng, ngày mới nhất, thứ tự theo số lượt giảm dần): `AdminReportGroupingTest`.
- [ ] `./gradlew assembleDebug testDebugUnitTest lintDebug` và `sh workflow/scripts/check-java-style.sh` đều qua; PR ghi đúng lệnh và kết quả thật (R1).
- [ ] PR liệt kê mọi lớp, phương thức, trường công khai mới kèm dòng tài liệu định nghĩa (R16); `AdminReportAdapter` và `AdminReportGrouping` là lớp giao diện không có trong `dac-ta-lop`, ghi vào PR như lớp mới không có dòng tài liệu.

### Tài liệu phải đọc
- `AGENTS.md` mục 6: R12, R16, R17, R18, R20, R22, R23.
- `workflow/plan/so-huu.md` mục 2, 3 (E04), 5 (`ReportRepository`: `getByStatus`, `countByTarget`, `closeByTarget` của Phát; `AdminRepository`, `AdminContentViewModel` của Phát), 6.
- `workflow/docs/product/dac-ta-chuc-nang.md` dòng STT 75 và 76.
- `workflow/docs/ui/e-quan-tri/README.md` mục E04 (tab "Báo cáo", Hành động, Trạng thái đặc biệt, Khác với ảnh).
- `workflow/docs/product/use-case/uc-e-quan-tri.md` mục UC37 (BR1 đến BR5, ngoại lệ) và UC35 (BR2).
- `workflow/docs/architecture/trang-thai.md` mục 1 (dòng admin ẩn tin; "Mỗi lần tin rời AVAILABLE"), mục 2, mục 5, mục 9 (LISTING_MODERATED, OFFER_RESULT).
- `workflow/docs/architecture/dac-ta-lop/data.md#reportrepository`, `#adminrepository` (dòng `hideListing`), `#listingrepository` (dòng `getById`), `#notificationrepository`; `viewmodel.md#admincontentviewmodel`.
- `workflow/docs/architecture/dac-ta-lop/model.md#report`, `#listing`, `#offer`, `#appnotification`; `enums.md#reporttarget`, `#reportreason`, `#reportstatus`, `#listingstatus`, `#offerstatus`, `#notificationtype`.
- `workflow/docs/data/firestore-schema.md#reports`, `#listings`, `#offers`; `workflow/docs/data/security-rules.md` mục 18 (reports), 10 (dòng "Admin ẩn"), 13 (dòng "Admin từ chối khi ẩn tin"), 21.
- `workflow/docs/conventions/giao-dien.md` mục 1 đến 6 và 8; `workflow/memory/code/README.md`, `workflow/memory/code/firebase.md`, `workflow/docs/conventions/android-java.md`, skill `pubg-code`, skill `pubg-ui`.

### Lớp, trường, trạng thái sẽ dùng
- `AdminModerationFragment` (E04, tab "Báo cáo").
- `AdminContentViewModel`: thuộc tính `reports` (`LiveData<List<Report>>`), `reportedListings` (`LiveData<List<Listing>>`), `loading`, `error`, `done`; `public void loadReports(String status, ReportTarget targetType)`, `public void loadReportedListings()`, `public void closeReports(String listingId, boolean dismissed)`, `public void hide(String listingId, String reason)` (thay thân).
- `ReportRepository`: `public void getByStatus(ReportStatus status, ReportTarget targetType, DataCallback<List<Report>> cb)`, `public void countByTarget(String targetId, DataCallback<Integer> cb)`, `public void closeByTarget(String targetId, boolean dismissed, String adminId, DataCallback<Void> cb)` (thay thân).
- `AdminRepository`: `public void hideListing(String id, String reason, DataCallback<Void> cb)` (thay thân).
- `ListingRepository.getById(String id, DataCallback<Listing> cb)` (của Tú, chỉ gọi). `NotificationRepository.create(AppNotification notification, WriteBatch batch, DataCallback<Void> cb)` (chỉ repository gọi). `BaseRepository`: `currentUserId()`, `logActivity(String action, ReportTarget targetType, String targetId)`, `fail(...)`.
- `Report`: `reporterId`, `targetType`, `targetId`, `reason`, `description`, `status`, `evidenceImageIds`, `conversationId`, `handledBy`, `public void resolve(String adminId)`, `public void dismiss(String adminId)`.
- `Listing`: `title`, `coverThumb`, `status`, `statusReason`, `sellerId`, `public void hide(String reason)`. `Offer`: `listingId`, `buyerId`, `conversationId`, `status`, `public void reject()`.
- `ReportTarget`: `LISTING`, `USER`, `MESSAGE`. `ReportReason`: `FAKE_ITEM`, `MISLEADING_INFO`, `SCAM`, `INAPPROPRIATE`, `OTHER`. `ReportStatus`: `OPEN`, `RESOLVED`, `DISMISSED`, `CANCELLED`. `ListingStatus`: `AVAILABLE`, `HIDDEN`. `OfferStatus`: `PENDING`, `REJECTED`. `NotificationType`: `LISTING_MODERATED`, `OFFER_RESULT`.
- `NetworkMonitor`: `public static NetworkMonitor getInstance()`, thuộc tính `online`.
- Firestore: `reports/{id}` (admin chỉ đổi `status`, `handledBy`, `updatedAt`), `listings/{id}` (chỉ `status`, `statusReason`, `updatedAt`), `offers/{id}` (chỉ `status`, `updatedAt`), `notifications`, `activityLogs`.

### File được tạo hoặc sửa
- [sửa] PUBGApp/app/src/main/java/com/nhom3/pubgapp/ui/admin/AdminModerationFragment.java
- [tạo] PUBGApp/app/src/main/java/com/nhom3/pubgapp/ui/admin/AdminReportAdapter.java
- [sửa] PUBGApp/app/src/main/res/layout/fragment_admin_moderation.xml (TabLayout chỉ có tab "Báo cáo"; hai tab còn lại do P2-E3c thêm)
- [tạo] PUBGApp/app/src/main/res/layout/item_admin_report.xml
- [tạo] PUBGApp/app/src/main/res/layout/dialog_admin_reason.xml
- [sửa] PUBGApp/app/src/main/res/values/strings_admin.xml
- [sửa] PUBGApp/app/src/main/res/navigation/nav_admin.xml (chỉ nếu thiếu hành động từ E04 sang E03 kèm mã người dùng và sang `AdminListingDetailFragment` kèm mã tin)
- [sửa] PUBGApp/app/src/main/java/com/nhom3/pubgapp/viewmodel/AdminContentViewModel.java (chỉ thân `loadReportedListings`, `closeReports`, `hide`)
- [sửa] PUBGApp/app/src/main/java/com/nhom3/pubgapp/data/ReportRepository.java (chỉ thân `getByStatus`, `countByTarget`, `closeByTarget`)
- [sửa] PUBGApp/app/src/main/java/com/nhom3/pubgapp/data/AdminRepository.java (chỉ thân `hideListing`)
- [tạo] PUBGApp/app/src/main/java/com/nhom3/pubgapp/ui/admin/AdminReportGrouping.java
- [tạo] PUBGApp/app/src/test/java/com/nhom3/pubgapp/ui/admin/AdminReportGroupingTest.java

Tên layout theo `so-huu.md` mục 2 và khung P1-06; nếu khung đã đặt tên khác thì theo khung.

### Các bước đề nghị
Có thể làm theo thứ tự khác, nhưng không được đổi tên lớp, phương thức, thuộc tính nào.
1. `ReportRepository.getByStatus(ReportStatus status, ReportTarget targetType, DataCallback<List<Report>> cb)`: truy vấn `reports` với `status ==`, thêm `targetType ==` khi `targetType` khác `null`, sắp `createdAt` giảm dần (chỉ mục `status`+`createdAt` và `targetType`+`status`+`createdAt`, đã có trong "Chỉ mục ghép dự kiến"); đổi thành `Report`; lỗi qua `fail(cb, e)`.
2. `ReportRepository.countByTarget(String targetId, DataCallback<Integer> cb)`: truy vấn tổng hợp `count()` trên `reports` với `targetId ==` và `status == OPEN`; trả `Integer`.
3. `ReportRepository.closeByTarget(String targetId, boolean dismissed, String adminId, DataCallback<Void> cb)`: đọc mọi báo cáo có `targetId` đó và `status == OPEN`; mỗi cái gọi `Report.dismiss(adminId)` khi `dismissed` true, ngược lại `Report.resolve(adminId)`; một lô ghi chỉ các khóa `status`, `handledBy`, `updatedAt` (đúng `onlyKeys` ở security-rules.md mục 18); quá 500 thì chia lô; sau đó `logActivity`.
4. `AdminRepository.hideListing(String id, String reason, DataCallback<Void> cb)`: từ chối nếu `reason` rỗng hoặc tin không ở `AVAILABLE`; gọi `Listing.hide(reason)` trên đối tượng đã đọc rồi một lô ghi: `listings/{id}` chỉ `status`, `statusReason`, `updatedAt`; mọi `offers` có `listingId` đó và `status == PENDING` đổi bằng `Offer.reject()` (chỉ `status`, `updatedAt`); thông báo `LISTING_MODERATED` cho `sellerId` (`targetId` là mã tin) và `OFFER_RESULT` cho từng người mua (`targetId` là `conversationId`) đưa vào cùng lô bằng `NotificationRepository.create(notification, batch, cb)`, chờ `cb` rồi mới commit lô (cách `create` thêm vào lô cần đọc thân của P1, chưa kiểm chứng); sau khi lô thành công thì `logActivity`.
5. `AdminContentViewModel`: `loadReports(String status, ReportTarget targetType)` bật `loading`, đổi `status` thành `ReportStatus` (kiểu `String` của chữ ký: Câu hỏi 1), gọi `ReportRepository.getByStatus` và đặt kết quả vào `reports`; `loadReportedListings()` đọc báo cáo `OPEN` nhắm vào tin, gom theo `targetId`, đọc từng tin bằng `ListingRepository.getById` và đặt vào `reportedListings` (cho tên và ảnh thu nhỏ của dòng). `closeReports(String listingId, boolean dismissed)` gọi `ReportRepository.closeByTarget` với `adminId` là `SessionManager.getInstance().getCurrentUserId()` (tham số tên `listingId` nhưng với báo cáo người dùng hoặc tin nhắn truyền `targetId` của báo cáo). `hide(String listingId, String reason)` gọi `AdminRepository.hideListing`, thành công thì gọi tiếp `closeReports(listingId, false)`, bật `done` và tải lại danh sách; lỗi đặt câu tiếng Việt vào `error`.
6. Viết lớp `AdminReportGrouping` (thuần Java) với một hàm gom nhóm và sắp xếp: nhận danh sách `Report`, trả danh sách nhóm (`targetId`, `targetType`, số lượt, tập `ReportReason` bỏ trùng, ngày mới nhất, tổng số ảnh đính kèm), sắp theo số lượt giảm dần rồi ngày mới nhất giảm dần. Viết `AdminReportGroupingTest` trước: danh sách rỗng; ba báo cáo cùng tin gồm hai lý do giống nhau; hai tin khác nhau thì tin nhiều lượt hơn đứng trước; hai tin cùng số lượt thì tin có báo cáo mới hơn đứng trước; một báo cáo người dùng và một báo cáo tin có cùng chữ số mã không bị gộp (khóa nhóm gồm cả `targetType`).
7. Layout `fragment_admin_moderation.xml` và `item_admin_report.xml`: TabLayout (một tab "Báo cáo"), hàng chip loại đối tượng, hàng ba nút trạng thái, RecyclerView, `view_empty_state`, `view_error_state`, tiến trình đang tải, `view_offline_banner`. Màu theo token; lý do báo cáo dùng `?attr/colorError`. `dialog_admin_reason.xml`: ô lý do bắt buộc, hai nút "Xác nhận", "Hủy".
8. `AdminReportAdapter`: nhận danh sách nhóm; dòng có ảnh thu nhỏ (giải mã `coverThumb` ngoài luồng chính, ảnh giữ chỗ và ảnh dự phòng), tên, các lý do, số lượt, hai nút theo loại đối tượng và trạng thái; `OnReportActionListener` là lớp ẩn danh do Fragment truyền vào.
9. `AdminModerationFragment`: chọn bộ lọc thì gọi lại `loadReports` (và `loadReportedListings` khi là trạng thái chờ xử lý); "Ẩn tin" mở `dialog_admin_reason`, xác nhận thì gọi `hide`; "Bỏ qua" gọi `closeReports(id, true)`; "Xem người dùng" và bấm dòng tin dùng hành động điều hướng trong `nav_admin.xml`. Khóa nút khi `NetworkMonitor.getInstance().online` là false hoặc `loading` là true; hiện `error` rồi xóa nó; đóng hộp thoại khi `done` bật rồi đặt lại.
10. Chuỗi trong `strings_admin.xml` theo mẫu `admin_moderation_<what>`: tên tab, chip loại đối tượng, ba nút trạng thái (đúng chữ ở README E04), nhãn năm lý do báo cáo, "Bỏ qua", "Ẩn tin", "Xem người dùng", "Tin không còn", dòng báo trống, câu lỗi.
11. Thử trên máy ảo: tạo hai báo cáo tin bằng hai tài khoản thường (P2-D4 nếu đã xong, hoặc tay trên Console: `reports/{id}` với `reporterId`, `targetType` `LISTING`, `targetId`, `reason`, `description`, `status` `OPEN`, `handledBy` rỗng); thử "Bỏ qua", "Ẩn tin"; xoay màn hình; bật chế độ máy bay.
12. Chạy hai lệnh kiểm ở mục "Tự kiểm trước khi mở Pull Request" và ghi kết quả vào PR.

### File dùng chung cần thêm dòng
Không có. Ghi vào mô tả PR để P3-R gộp: luật "Xử lý" của `reports` (security-rules.md mục 18), "Admin ẩn" của `listings` (mục 10), "Admin từ chối khi ẩn tin" của `offers` (mục 13), tạo `notifications` (mục 21); chỉ mục `reports`: `status`+`createdAt` và `targetType`+`status`+`createdAt`.

### Cấm đụng
- Leader-only không có trong danh sách trên: `AndroidManifest.xml`, `nav_main.xml`, `themes.xml`, `colors.xml`, `dimens.xml`, `strings_common.xml`, `AppDatabase`, `Converters`, mọi file Gradle, `firebase/`.
- Thân các phương thức khác của `AdminContentViewModel`, `AdminRepository` (thuộc P2-E2c, E3b, E3c, E4): `restore`, `remove`, `removeAllOf`, `clearFlag`, `loadKeywords`, `addKeyword`, `removeKeyword`, `saveCategory`, `deleteCategory`, `setConditionFields`, `restoreListing`, `removeListing`, `removeAllListingsOf`, `getKeywords`...
- Module B (Tú): `ListingRepository` (chỉ gọi `getById`, có thể còn `NOT_READY`; mọi thay đổi trạng thái tin của admin nằm ở `AdminRepository`). Module D (Phúc): `ReportRepository.create`, `cancel`, `getMyReports`, `exportCsv`, `TrustViewModel` (phía người dùng).
- `Report`, `Listing`, `Offer` (lớp mô hình: chỉ gọi).

### Phụ thuộc
P2-E1 (vào E04 từ E01), P1-04, P1-06. Mềm: P2-D4 (báo cáo thật từ người dùng; trong lúc chờ tạo tay), P2-B1 (tin thật). Tên người dùng, nội dung tin nhắn và ảnh đính kèm trong dòng báo cáo bị chặn bởi Câu hỏi 2 và 3 (R20: thiếu thuộc tính hoặc phụ thuộc thì gắn nhãn `blocked` cho phần đó); phần còn lại làm được ngay.

### Ngoài phạm vi
- Tab "Tin bị gắn cờ", "Từ khóa cấm", "Bỏ cờ": P2-E3c. Màn chi tiết tin, "Khôi phục", "Gỡ tin": P2-E3b. "Gỡ toàn bộ tin" của tài khoản bị khóa: P2-E2c.
- Cảnh báo hoặc khóa người bị báo cáo: P2-E2c (E04 chỉ dẫn sang E03).
- Tự ẩn tin khi vượt 20 báo cáo: bỏ theo quyết định 2026-10-22 (UC37 BR3, gói Spark không có Cloud Functions); thay bằng sắp danh sách theo số lượt giảm dần (bước 6).
- Thông báo kết quả cho người báo cáo: xem Câu hỏi 4. Gửi, hủy, xuất CSV báo cáo của người dùng: P2-D4.
- Bố cục ngang: P3-E.

### Tài liệu cần cập nhật
Không có, trừ khi Câu hỏi 1, 2 hoặc 3 được trả lời bằng đổi tài liệu: khi đó cập nhật `workflow/docs/architecture/dac-ta-lop/viewmodel.md#admincontentviewmodel` và `data.md#reportrepository`.

### Tự kiểm trước khi mở Pull Request
- [ ] Mở E04 với tài khoản admin: tab "Báo cáo" hiện báo cáo `OPEN` đã gom, đúng số lượt và lý do.
- [ ] Bấm "Bỏ qua" ở một tin: dòng biến khỏi "Chờ xử lý", xuất hiện ở "Đã xem (không vi phạm)"; tin vẫn `AVAILABLE` trên Console.
- [ ] Bấm "Ẩn tin" với lý do: tin `HIDDEN` kèm `statusReason`; offer `PENDING` của tin thành `REJECTED`; báo cáo thành `RESOLVED` và dòng sang "Đã xử lý (có vi phạm)"; tài khoản người đăng có thông báo `LISTING_MODERATED`; người đăng mở tin thấy lý do và không bấm hiện lại được (kiểm khi P2-B2 xong).
- [ ] "Ẩn tin" để trống lý do: báo lỗi, không ghi gì.
- [ ] Tắt mạng: nút bị khóa, dải offline hiện; bật mạng rồi "Thử lại" tải được.
- [ ] `cd PUBGApp && ./gradlew testDebugUnitTest --tests "*AdminReportGroupingTest"` báo các test đều qua.
- [ ] `cd PUBGApp && ./gradlew assembleDebug testDebugUnitTest lintDebug` kết thúc `BUILD SUCCESSFUL`.
- [ ] `sh workflow/scripts/check-java-style.sh` không báo lỗi.
- [ ] Xoay màn hình khi đang chọn bộ lọc "Đã xử lý" và hộp thoại lý do mở: bộ lọc, danh sách, hộp thoại và chữ đang gõ còn nguyên.
- [ ] Đã liệt kê trong PR mọi lớp, phương thức, trường công khai mới kèm dòng tài liệu định nghĩa (R16), hoặc ghi "không có".

### Quy mô
- [x] Đây là một lát cắt dọc của một module, khoảng nửa ngày đến một ngày, một Pull Request.

### Câu hỏi cho leader
1. `AdminContentViewModel.loadReports(String status, ReportTarget targetType)` nhận trạng thái kiểu `String`, còn `ReportRepository.getByStatus(ReportStatus status, ReportTarget targetType, DataCallback<List<Report>> cb)` nhận `ReportStatus`. Issue này truyền tên hằng của `ReportStatus` (ví dụ `ReportStatus.OPEN.name()`) và coi `null` ở `targetType` là mọi loại đối tượng (`firestore-schema.md#reports` ghi `targetType ==` là tùy chọn). Xác nhận, hoặc đổi chữ ký `loadReports` sang `ReportStatus` trong `viewmodel.md` trước khi code (R16).
2. Tên đối tượng trong dòng báo cáo: `AdminContentViewModel` không dùng `UserRepository` hay `ChatRepository`, và `reportedListings` chỉ gồm tin có báo cáo `OPEN`. Hệ quả: dòng báo cáo `USER` chỉ hiện mã người dùng (không có tên), dòng `MESSAGE` không hiện nội dung tin nhắn, và dòng báo cáo về tin ở hai tab "Đã xử lý", "Đã xem" không có tiêu đề và ảnh thu nhỏ (chỉ hiện mã tin). README E04 đòi tên người dùng và nội dung tin nhắn. Đề xuất: thêm `UserRepository` và `ChatRepository` vào "Dùng tới" của `AdminContentViewModel` kèm thuộc tính nhận kết quả; tên do leader chốt.
3. Báo cáo nay có `evidenceImageIds` và `conversationId`, nhưng `AdminContentViewModel` không dùng `MediaRepository` nên không đọc được ảnh đính kèm, và không dùng `ChatRepository` (`getMessagesOnce` dành cho admin) nên không mở được đoạn chat của báo cáo tin nhắn, cũng không biết ai là người gửi tin nhắn bị báo cáo để nút "Xem người dùng" sang đúng E03. Tạm thời: dòng báo cáo hiện số ảnh đính kèm, dòng báo cáo `MESSAGE` hiện lý do và mô tả và chỉ có nút "Bỏ qua". README E04 cũng chưa nói màn nào hiện ảnh và đoạn chat. Cần quyết cách hiện và thêm thuộc tính cho `AdminContentViewModel`.
4. UC37 bước 5 nói gửi thông báo cho chủ tin **và người báo cáo**, nhưng `trang-thai.md` mục 9 chỉ có `LISTING_MODERATED` cho người đăng; không có loại thông báo hay quy tắc cho người báo cáo. Tạm thời chỉ thông báo cho người đăng. Xác nhận.
5. Báo cáo `USER` và `MESSAGE` chỉ có đường đóng là "Bỏ qua" (`DISMISSED`); không có chỗ nào trong README E04 hay `viewmodel.md` để chuyển chúng sang `RESOLVED` sau khi admin cảnh báo hoặc khóa người đó ở E03 (xem Câu hỏi 5 của P2-E2c). Tạm thời giữ nguyên. Xác nhận hoặc bổ sung.
6. UC37 ngoại lệ 4a (tin đang thuộc giao dịch có hiệu lực thì cảnh báo và hỏi cách xử lý giao dịch) không có phương thức hay bảng chuyển trạng thái nào, và Security Rules chỉ cho admin ẩn tin từ `AVAILABLE`. Tạm thời khóa nút "Ẩn tin" với tin ở `RESERVED` và hiện câu "Tin đang giữ chỗ cho một giao dịch, chưa thể ẩn". Xác nhận.
7. UC37 có quyết định "Giữ tin"; README E04 đặt tên nút tương ứng là "Bỏ qua". Tạm thời "Giữ tin" của UC37 chính là nút "Bỏ qua". Xác nhận.
