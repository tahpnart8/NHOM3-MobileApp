TITLE: feat(trust): báo cáo tin đăng, người dùng và tin nhắn
LABELS: type:feature, area:trust, module:d, priority:p1
ASSIGNEES: PmSubin
PHASE: P2

Plan item: P2-D4a

### Bối cảnh
- PUBGApp là ứng dụng Android mua bán đồ cũ kết hợp mạng xã hội, viết bằng Java 17, XML Views với ViewBinding, Firebase Authentication và Cloud Firestore gói Spark (không Cloud Storage, không Kotlin; ảnh nén trên máy rồi lưu Base64 trong Firestore).
- Module D "Hồ sơ và tin cậy" do Phúc (`PmSubin`) làm: hồ sơ cá nhân và công khai, uy tín, theo dõi, gửi yêu cầu xác thực tài khoản, báo cáo, khiếu nại, và phía admin xử lý khiếu nại.
- Issue này làm màn D04 `ReportFragment` (nhận `targetType`, `targetId` và `conversationId`): biểu mẫu báo cáo một tin đăng, một người dùng hoặc một tin nhắn, kèm ô đính kèm tối đa 3 ảnh bằng chứng tùy chọn. Danh sách "Báo cáo của tôi" là issue P2-D4b.
- Đọc trước ba tài liệu: `workflow/docs/ui/d-ho-so-tin-cay/README.md` mục D04; `workflow/docs/product/use-case/uc-d-ho-so-tin-cay.md` mục UC33; `workflow/docs/architecture/trang-thai.md` mục 5.
- Đã có trên `main` sau P1: khung `ReportFragment` trống, hành động toàn cục `action_global_report` (tham số `targetType`, `targetId`, `conversationId`), `ReportRepository` đủ chữ ký (thân trả `NOT_READY`), mọi ViewModel có `loading`, `error`, `done` và getter công khai, `MediaRepository` làm thật, thành phần dùng chung.

### Mã tính năng
F-12 (Báo cáo và khiếu nại)

### Mã việc kế hoạch
P2-D4, tách thành P2-D4a (biểu mẫu báo cáo D04, STT 61) và P2-D4b (thẻ "Báo cáo của tôi" trên D07: hủy, xuất CSV, STT 62 và 63). Lý do tách: D04 nay có ba loại đối tượng và ô đính kèm ảnh; D07 có danh sách, hủy báo cáo và xuất tệp CSV; làm cả hai vượt một ngày.

### Module
D. Hồ sơ và tin cậy (Phúc)

### STT trong bảng đặc tả
61. Use case: UC33.

### Màn hình mockup
D04 (`ReportFragment`, nhận `targetType`, `targetId` và `conversationId`, màn chi tiết: nút quay lại, ẩn thanh dưới). Ảnh mockup chỉ vẽ báo cáo tin đăng; báo cáo người dùng và tin nhắn dùng cùng biểu mẫu chỉ đổi thẻ đối tượng; ô đính kèm ảnh chưa có trong ảnh; vẫn phải làm (xem mục "Khác với ảnh" của D04).

### Làm gì và vì sao
Người dùng đã đăng nhập báo cáo một tin đăng (từ menu ba chấm của B03), một người dùng (từ D02, D03 hoặc C02) hoặc một tin nhắn (từ C02, kèm mã cuộc trò chuyện chứa tin nhắn đó) có dấu hiệu vi phạm: chọn một trong năm lý do có sẵn (nghi ngờ hàng giả, thông tin sai lệch, dấu hiệu lừa đảo, nội dung không phù hợp, khác), nhập mô tả thêm không bắt buộc, có thể đính kèm tối đa 3 ảnh bằng chứng (không bắt buộc, chỉ ảnh, không video), rồi bấm "Gửi báo cáo" (STT 61). Ảnh được nén trên máy và lưu thành tài liệu `images` mục đích EVIDENCE, mã ảnh nằm ở `Report.evidenceImageIds`; báo cáo tin nhắn lưu thêm `Report.conversationId` để admin mở được đoạn chat. Báo cáo được ghi `OPEN` để admin xử lý ở E04; người bị báo cáo không biết ai báo cáo (UC33 BR3). Mỗi người chỉ có một báo cáo đang chờ xử lý cho mỗi đối tượng. Hệ thống không tự ẩn tin theo số lượt báo cáo: admin thấy số báo cáo ở E04 và tự quyết (UC33 BR5). Khách chưa đăng nhập không báo cáo được: mọi lối vào đưa Khách tới A01 bằng `action_global_sign_in`. Issue này phục vụ các dòng barem R-Chapter 4 (input control: `RadioGroup` chọn lý do, `EditText` mô tả giới hạn 1000 ký tự, nút gửi bật hay khóa theo dữ liệu; `RecyclerView` ngang cho ảnh xem trước), R-Chapter 1, 2, 6, 7 (implicit Intent để chọn ảnh bằng chứng từ thư viện, async qua `DataCallback`) và R-Chapter 3 (unit test JUnit cho hàm kiểm điều kiện gửi báo cáo).

### Tiêu chí nghiệm thu
- [ ] Mở D04 từ menu "Báo cáo tin" của B03 (`action_global_report`, `targetType` là `ReportTarget.LISTING`), từ D02 và D03 (`ReportTarget.USER`), từ C02 (`ReportTarget.USER` hoặc `ReportTarget.MESSAGE`; với MESSAGE thì có `conversationId`, các trường hợp khác `conversationId` rỗng). Tiêu đề màn đổi theo loại: "Báo cáo tin đăng", "Báo cáo người dùng", "Báo cáo tin nhắn".
- [ ] Năm lựa chọn lý do chọn một, mỗi lựa chọn có một dòng giải thích, theo thứ tự: Nghi ngờ hàng giả (`ReportReason.FAKE_ITEM`), Thông tin sai lệch (`MISLEADING_INFO`), Dấu hiệu lừa đảo (`SCAM`), Nội dung không phù hợp (`INAPPROPRIATE`), Khác (`OTHER`). Chọn lý do mới thì bỏ chọn lý do cũ. Nút "Gửi báo cáo" chưa bấm được khi chưa chọn lý do. Tên hằng không bao giờ hiện cho người dùng.
- [ ] Ô "Mô tả thêm (không bắt buộc)" tối đa 1000 ký tự (Rules `description.size()` không quá 1000); vượt thì không cho gõ thêm hoặc báo rõ.
- [ ] Ô đính kèm ảnh (tùy chọn): chọn tối đa 3 ảnh, chỉ nhận ảnh (`ContentResolver.getType(Uri)` bắt đầu bằng `image/`), tệp không phải ảnh bị từ chối kèm câu báo; đủ 3 ảnh thì ô thêm bị khóa, muốn thêm thì xóa bớt một ảnh; ảnh xem trước nằm trong một `RecyclerView` ngang bằng `EvidenceThumbAdapter`. Mỗi ảnh lưu bằng `MediaRepository.saveImage(uri, ImagePurpose.EVIDENCE, cb)` trước khi ghi báo cáo; ảnh lỗi hoặc ghi báo cáo lỗi thì xóa các ảnh đã lưu của lần đó bằng `MediaRepository.delete` và giữ nguyên lý do, mô tả, ảnh đã chọn trên màn.
- [ ] Gửi báo cáo với Firebase thật: tạo `reports/{id}` với `reporterId` là người đang đăng nhập (do repository lấy từ phiên, không từ màn hình), `targetType`, `targetId`, `reason`, `description`, `evidenceImageIds` (0 đến 3 mã), `conversationId` (khác rỗng khi và chỉ khi `targetType` là MESSAGE, Rules mục 18 đòi), `status` OPEN, `handledBy` rỗng, `createdAt` giờ máy chủ; quay về màn trước kèm thông báo "Cảm ơn bạn đã báo cáo".
- [ ] Đã có báo cáo `OPEN` của chính mình cho cùng `targetId`: báo "Bạn đã báo cáo nội dung này, đang chờ xử lý" và không ghi thêm (UC33 ngoại lệ 1b, kiểm ở `ReportRepository.create` vì Rules không ép duy nhất).
- [ ] Báo cáo chính mình (`ReportTarget.USER` mà `targetId` bằng mã của mình): không cho gửi, báo bằng tiếng Việt (UC33 tiền điều kiện 3).
- [ ] Mất mạng hoặc Rules từ chối (tài khoản không còn ACTIVE): hiện câu tiếng Việt từ `error`, giữ nguyên lý do, mô tả và ảnh đã chọn, không đóng màn (UC33 ngoại lệ 2c).
- [ ] Thẻ đối tượng bị báo cáo: tin đăng (ảnh, tên món, tên người bán), người dùng (ảnh đại diện, tên), tin nhắn (nội dung); đối tượng đã bị xóa hoặc ẩn thì báo "Nội dung không còn khả dụng" (UC33 ngoại lệ 1a). Xem Câu hỏi 1, phần này đánh dấu `blocked`; trong lúc chờ thẻ chỉ hiện loại đối tượng.
- [ ] Khách chưa đăng nhập (`SessionManager.getInstance().isLoggedIn()` là false) không ở lại D04: Fragment đi `action_global_sign_in`.
- [ ] Xoay màn hình giữa lúc đã chọn lý do, gõ mô tả và chọn ảnh: lý do đã chọn, nút "Gửi báo cáo" bật hay khóa đúng, mô tả và ảnh đã chọn còn nguyên (UC33 NFR3); các view có `android:id` để Android tự giữ, danh sách `Uri` giữ bằng `onSaveInstanceState`.
- [ ] Mỗi đường lỗi có câu tiếng Việt, không im lặng; phương thức của module khác còn `NOT_READY` thì hiện đúng lỗi đó, không giả dữ liệu. Phản hồi gửi báo cáo trong 3 giây ở mạng bình thường (UC33 NFR1).
- [ ] Offline: `view_offline_banner.xml` hiện, nút "Gửi báo cáo" khóa; ghi quá 15 giây thì báo "Không có kết nối".
- [ ] Gửi báo cáo không sửa tài liệu của người, tin hay tin nhắn bị báo cáo, và không có chuyển trạng thái nào ngoài tạo `OPEN` (R18, `trang-thai.md` mục 5). Không có đoạn nào tự ẩn tin theo số báo cáo (UC33 BR5 đã đổi).
- [ ] Unit test `TrustViewModelTest`: hàm kiểm điều kiện gửi báo cáo (không công khai, tách riêng) với các ca: chưa chọn lý do thì không cho gửi; đã chọn lý do thì cho gửi; báo cáo chính mình khi là `ReportTarget.USER` thì không cho gửi; báo cáo người khác thì cho gửi; 3 ảnh cho gửi, 4 ảnh không; `ReportTarget.MESSAGE` thiếu `conversationId` thì không cho gửi. Chạy được trên JVM, không cần Firebase. `Report.cancel()` đã có test ở P1 (không liên quan issue này).
- [ ] Layout chỉ dùng token của `giao-dien.md`; nút "Gửi báo cáo" tô `colorError` chữ `colorOnPrimary`; id view theo `<loại><Việc>` kiểu lowerCamelCase (ví dụ `radioReasonScam`, `buttonSubmitReport`); mọi chữ là string resource tiếng Việt có dấu trong `strings_profile.xml`, khóa theo `<module>_<màn>_<việc>` (ví dụ `trust_report_submit`).
- [ ] `cd PUBGApp && ./gradlew assembleDebug testDebugUnitTest lintDebug` và `sh workflow/scripts/check-java-style.sh` đều qua; PR ghi đúng lệnh và kết quả thật (R1).
- [ ] PR liệt kê mọi lớp, phương thức, trường công khai mới kèm dòng tài liệu định nghĩa (R16); nếu không có thì ghi "không có".

### Tài liệu phải đọc
- `AGENTS.md` mục 6: R12, R16, R17, R18, R20, R21, R23.
- `workflow/plan/so-huu.md` mục 3 (D04), mục 4 (`action_global_report`, `action_global_sign_in`), mục 5 (dòng `ReportRepository`: Phúc làm `create`, `cancel`, `getMyReports`, `exportCsv`; Phát làm `getByStatus`, `countByTarget`, `closeByTarget`; `TrustViewModel` "phần còn lại" của Phúc).
- `workflow/docs/product/barem-diem.md` (dòng barem mà issue này phục vụ).
- `workflow/docs/product/dac-ta-chuc-nang.md` dòng STT 61.
- `workflow/docs/product/use-case/uc-d-ho-so-tin-cay.md` mục UC33.
- `workflow/docs/ui/README.md` mục "Chế độ khách"; `workflow/docs/ui/d-ho-so-tin-cay/README.md` mục D04.
- `workflow/docs/conventions/giao-dien.md` mục 1 đến 8.
- `workflow/docs/architecture/dac-ta-lop/data.md#reportrepository`, `#mediarepository`
- `workflow/docs/architecture/dac-ta-lop/viewmodel.md#trustviewmodel`
- `workflow/docs/architecture/dac-ta-lop/model.md#report`
- `workflow/docs/architecture/dac-ta-lop/enums.md#reporttarget`, `#reportreason`, `#reportstatus`, `#imagepurpose`
- `workflow/docs/architecture/trang-thai.md` mục 5 (báo cáo).
- `workflow/docs/data/firestore-schema.md#reports`
- `workflow/docs/data/security-rules.md` mục 18 (reports) và bảng Giới hạn (dòng "Đảm bảo duy nhất khi mã tài liệu là mã ngẫu nhiên").
- `workflow/docs/architecture/overview.md` mục 8 (lỗi, offline, thời hạn chờ ghi).
- `workflow/memory/code/README.md`, `workflow/memory/code/firebase.md`, `workflow/docs/conventions/android-java.md`, skill `pubg-code` và `pubg-ui`.

### Lớp, trường, trạng thái sẽ dùng
Tên đúng như tài liệu; không đặt tên mới.

- `ReportFragment` (D04).
- `TrustViewModel`: thuộc tính `reports` (`LiveData<List<Report>>`), `loading`, `error`, `done` (kèm getter công khai, chép đúng chữ trong `TrustViewModel.java` do P1 tạo); phương thức được thay thân: `public void submitReport(Report report, List<Uri> imageUris)`.
- `ReportRepository`: `public void create(Report report, DataCallback<Void> cb)`.
- `Report`: `reporterId`, `targetType`, `targetId`, `reason`, `description`, `status`, `evidenceImageIds`, `conversationId`, `handledBy`.
- `ReportTarget`: LISTING, USER, MESSAGE. `ReportReason`: FAKE_ITEM, MISLEADING_INFO, SCAM, INAPPROPRIATE, OTHER. `ReportStatus.OPEN`. `ImagePurpose.EVIDENCE`.
- `MediaRepository`: `public void saveImage(Uri uri, ImagePurpose purpose, DataCallback<String> cb)`, `public void delete(String id, DataCallback<Void> cb)` (của Phát, chỉ gọi).
- `BaseRepository`: `protected String currentUserId()` (dùng trong `ReportRepository`).
- `SessionManager`: `getInstance()`, `getCurrentUserId()`, `isLoggedIn()`. `NetworkMonitor`: `getInstance()`, `isOnline()`. `DataCallback.NOT_READY`.
- Firestore: `reports/{id}`, `images/{id}`.
- Hành động toàn cục (chỉ nhận và gọi): `action_global_report` (tham số `targetType`, `targetId`, `conversationId`), `action_global_sign_in`.

### File được tạo hoặc sửa
- [sửa] PUBGApp/app/src/main/java/com/nhom3/pubgapp/ui/trust/ReportFragment.java
- [sửa] PUBGApp/app/src/main/res/layout/fragment_trust_report.xml
- [tạo] PUBGApp/app/src/main/java/com/nhom3/pubgapp/ui/trust/EvidenceThumbAdapter.java (dải ảnh bằng chứng xem trước kèm nút xóa; số ảnh tối đa là tham số của Fragment, P2-D5a dùng lại với 5)
- [tạo] PUBGApp/app/src/main/res/layout/item_trust_evidence_thumb.xml
- [sửa] PUBGApp/app/src/main/res/values/strings_profile.xml
- [sửa] PUBGApp/app/src/main/res/navigation/nav_profile.xml (chỉ nếu khung P1 thiếu tham số `targetType`, `targetId` hoặc `conversationId` của đích D04)
- [sửa] PUBGApp/app/src/main/java/com/nhom3/pubgapp/viewmodel/TrustViewModel.java (chỉ thân `submitReport` và hàm tĩnh không công khai kiểm điều kiện gửi)
- [sửa] PUBGApp/app/src/main/java/com/nhom3/pubgapp/data/ReportRepository.java (chỉ thân `create`)
- [tạo hoặc sửa] PUBGApp/app/src/test/java/com/nhom3/pubgapp/viewmodel/TrustViewModelTest.java (P2-D3 đã tạo; nếu chưa có trên `main` thì tạo, có rồi thì thêm ca)

`EvidenceThumbAdapter` là lớp giao diện, không có trong `dac-ta-lop`; ghi vào PR như một lớp mới không có dòng tài liệu. Hàm kiểm điều kiện gửi là hàm không công khai (package-private, tĩnh), tách riêng để test; tên do bạn đặt, không tính vào danh sách R16, ghi tên vào PR.

### Các bước đề nghị
Có thể làm theo thứ tự khác, nhưng không được đổi tên lớp, phương thức, trường nào.

1. Đọc `workflow/memory/code/firebase.md` và skill `pubg-code`, `pubg-ui`. Mở `ReportFragment.java` và `fragment_trust_report.xml` do P1 tạo; xem tham số `targetType`, `targetId`, `conversationId` mà khung đã khai ở nav graph; mở `TrustViewModel.java` để chép đúng tên getter.
2. Thay thân `ReportRepository.create(Report report, DataCallback<Void> cb)`: kiểm `NetworkMonitor.getInstance().isOnline()`; đặt `reporterId` bằng `currentUserId()` (R12: không tin giá trị màn hình gửi), `status` bằng `ReportStatus.OPEN`, `handledBy` rỗng; từ chối khi `evidenceImageIds` quá 3 mã, hoặc khi `conversationId` rỗng với `ReportTarget.MESSAGE`, hoặc khác rỗng với loại khác (Rules mục 18 cũng đòi); truy vấn `reports` có `reporterId` bằng mình, `targetId` bằng của báo cáo này và `status` OPEN, nếu có kết quả thì `cb.onError("Bạn đã báo cáo nội dung này, đang chờ xử lý")`; không có thì ghi `reports/{id}` mới với `createdAt` và `updatedAt` bằng `FieldValue.serverTimestamp()`. Chờ tối đa 15 giây, quá hạn `cb.onError("Không có kết nối")`.
3. Trong `TrustViewModel`, viết hàm tĩnh không công khai kiểm điều kiện gửi: có lý do; không phải báo cáo chính mình khi `ReportTarget.USER`; không quá 3 ảnh; có `conversationId` khi `ReportTarget.MESSAGE`.
4. Thay thân `TrustViewModel.submitReport(Report report, List<Uri> imageUris)`: kiểm bằng hàm ở bước 3; bật `loading`; lưu lần lượt từng ảnh bằng `MediaRepository.saveImage(uri, ImagePurpose.EVIDENCE, cb)` để có danh sách mã ảnh; đặt vào `Report.evidenceImageIds` bằng setter theo quy ước; gọi `ReportRepository.create`; thành công thì bật `done` (màn đóng), lỗi thì xóa ảnh đã lưu bằng `MediaRepository.delete(id, cb)` rồi đưa câu lỗi vào `error`.
5. Viết `fragment_trust_report.xml` bằng `ConstraintLayout` trong vùng cuộn: `view_detail_toolbar.xml`, thẻ đối tượng, câu hỏi "Vì sao bạn báo cáo?", `RadioGroup` năm lựa chọn có dòng giải thích, ô "Mô tả thêm (không bắt buộc)", ô đính kèm ảnh (biểu tượng máy ảnh và `RecyclerView` ngang xem trước), nút "Gửi báo cáo", `view_offline_banner`, chỉ báo đang gửi. Mỗi `RadioButton` và ô nhập có `android:id` (dạng `<loại><Việc>`) để xoay màn hình không mất lựa chọn.
6. Viết `EvidenceThumbAdapter` và `item_trust_evidence_thumb.xml`: ô ảnh xem trước kèm nút xóa. Dùng lớp ẩn danh, không lambda.
7. Trong `ReportFragment`: nếu `SessionManager.getInstance().isLoggedIn()` là false thì đi `action_global_sign_in` và dừng; lấy `targetType`, `targetId`, `conversationId` từ đối số; đặt tiêu đề theo loại; nối nút chọn lý do với `ReportReason`; chỉ cho bấm "Gửi báo cáo" khi đã chọn lý do và không offline và `loading` đang false.
8. Ô đính kèm ảnh: chọn ảnh bằng implicit Intent hệ thống qua Activity Result API của AndroidX (`registerForActivityResult` với bộ chọn ảnh hệ thống; kiểm tên lớp bằng tài liệu chính thức hoặc MCP `context7` trước khi viết, vì chưa xác minh, R2), kiểm tệp là ảnh, tối đa 3; giữ `Uri` đã chọn trong `onSaveInstanceState`. Chụp ảnh bằng máy ảnh chỉ làm khi Câu hỏi 4 của P2-D1b được trả lời.
9. Khi bấm gửi, dựng một `Report` (`targetType`, `targetId`, `reason`, `description`, `conversationId`) bằng setter theo quy ước của lớp mô hình rồi gọi `TrustViewModel.submitReport(report, imageUris)`. Dùng lớp ẩn danh, không lambda. Thành công (`done`) thì quay về màn trước kèm "Cảm ơn bạn đã báo cáo".
10. Thẻ đối tượng: chỉ hiện loại đối tượng cho tới khi Câu hỏi 1 được trả lời (đây là phương án tạm, không phải quyết định).
11. Viết test cho hàm ở bước 3 trong `TrustViewModelTest` (các ca ở mục tiêu chí).
12. Thêm mọi chuỗi vào `strings_profile.xml` (tiếng Việt có dấu, không emoji, khóa `<module>_<màn>_<việc>`), rồi chạy các lệnh ở mục "Tự kiểm trước khi mở Pull Request" và mở PR.

### File dùng chung cần thêm dòng
Ghi vào mô tả Pull Request để Phát gộp (`firebase/` là file của Phát, `so-huu.md` mục 6): luật tạo báo cáo ở `security-rules.md` mục 18 và chỉ mục ghép `reports` (`reporterId`, `createdAt`); truy vấn kiểm trùng ở bước 2 chỉ so bằng nên thường không cần chỉ mục mới, nếu Firebase báo thiếu thì ghi liên kết.

### Cấm đụng
- Leader-only: `AndroidManifest.xml`, `nav_main.xml`, `nav_admin.xml`, `themes.xml`, `colors.xml`, `dimens.xml`, `strings_common.xml`, `AppDatabase`, `Converters`, mọi file Gradle, `firebase/`.
- Phát: `ReportRepository.getByStatus`, `countByTarget`, `closeByTarget`; màn E04 (admin xem báo cáo); `AdminContentViewModel`; `MediaRepository`.
- Module B: `ListingDetailFragment` (nơi gọi D04 cho tin đăng). Module C: `ChatFragment` (nơi gọi D04 cho người dùng và tin nhắn). Hai nơi này chỉ gọi hành động toàn cục.
- Thân các phương thức `cancel`, `getMyReports`, `exportCsv` (P2-D4b), phương thức khiếu nại của `TrustViewModel` (P2-D5a, P2-D5b) và `submitReview`, `loadReviews`.
- Mọi lớp trong `model`, `model.enums`: không thêm trường vào `Report`.

### Phụ thuộc
P1-04, P1-06. Để thử luồng mở D04 từ B03 và C02 cần B03 (P2-B6) và C02 (P2-C2) có nút báo cáo; trong lúc chờ có thể mở D04 từ một nút tạm cục bộ không commit. Thứ tự làm trong module D: sau P2-D3 (dùng chung `TrustViewModelTest`).

### Ngoài phạm vi
- Thẻ "Báo cáo của tôi", hủy báo cáo, xuất CSV: P2-D4b.
- Phía admin xử lý báo cáo (STT 75): module E; gửi báo cáo không sửa tài liệu người bị báo cáo (`trang-thai.md` mục 5).
- Tự ẩn tin khi bị báo cáo nhiều lần: đã bỏ khỏi UC33 (BR5), không làm; cảnh cáo người báo cáo spam (BR4) là việc admin ở UC35.
- Báo cáo đánh giá (UC33 BR1: chưa hỗ trợ), chặn người dùng, video bằng chứng: không làm.
- Bố cục ngang và giữ chắc ảnh chọn dở khi xoay (ngoài view có id): P3-D.

### Tài liệu cần cập nhật
Không có, trừ khi phải đổi hành vi. Câu hỏi 1 có thể dẫn tới Phát cập nhật `viewmodel.md#trustviewmodel`.

### Tự kiểm trước khi mở Pull Request
- [ ] `cd PUBGApp && ./gradlew assembleDebug testDebugUnitTest lintDebug` kết thúc `BUILD SUCCESSFUL`, trong đó `TrustViewModelTest` chạy và qua.
- [ ] `sh workflow/scripts/check-java-style.sh` không báo lỗi nào.
- [ ] Mở D04 (từ B03 hoặc nút tạm cục bộ không commit) cho một tin: tiêu đề "Báo cáo tin đăng", chưa chọn lý do thì nút "Gửi báo cáo" chưa bấm được; chọn lý do, gõ mô tả, gửi: thấy "Cảm ơn bạn đã báo cáo" và quay lại. Firebase Console có `reports/{id}` với `status` OPEN, `reporterId` là tài khoản đang dùng.
- [ ] Gửi một báo cáo kèm 2 ảnh: Firebase Console có hai tài liệu `images` với `purpose` EVIDENCE và `reports/{id}.evidenceImageIds` có đúng hai mã. Thử thêm ảnh thứ 4: bị chặn.
- [ ] Gửi lại cho cùng tin khi báo cáo đầu còn `OPEN`: thấy câu "Bạn đã báo cáo nội dung này, đang chờ xử lý", không có tài liệu thứ hai.
- [ ] Mở D04 với `targetType` USER và `targetId` là mã của chính mình: không cho gửi, có câu giải thích.
- [ ] Mở D04 với `targetType` USER và MESSAGE (MESSAGE kèm `conversationId`): tiêu đề đổi "Báo cáo người dùng", "Báo cáo tin nhắn"; báo cáo tin nhắn có `reports/{id}.conversationId` đúng.
- [ ] Đăng xuất (khách) rồi cố mở D04: bị đưa tới A01.
- [ ] Tắt mạng (chế độ máy bay) rồi gửi: nút khóa; bật mạng giữa lúc đang gửi rồi hết 15 giây: báo "Không có kết nối", lý do, mô tả và ảnh vẫn còn.
- [ ] Xoay màn hình giữa lúc đã chọn lý do, gõ mô tả và chọn ảnh: cả ba còn nguyên.
- [ ] PR liệt kê mọi lớp, phương thức, trường công khai mới kèm dòng tài liệu định nghĩa (R16), hoặc ghi "không có"; ghi `EvidenceThumbAdapter` là lớp giao diện mới và tên hàm kiểm không công khai.

### Quy mô
- [x] Đây là một lát cắt dọc của một module, khoảng nửa ngày đến một ngày, một Pull Request.

### Câu hỏi cho leader
1. Thẻ đối tượng trên D04 (ảnh và tên tin cùng tên người bán, ảnh đại diện và tên người dùng, nội dung tin nhắn) cần đọc `Listing`, `User` hoặc `ChatMessage`, nhưng "Dùng tới" của `TrustViewModel` không có `ListingRepository`, `UserRepository`, `ChatRepository` và không có LiveData nào chứa kết quả; việc báo "Nội dung không còn khả dụng" (UC33 ngoại lệ 1a) và kiểm tin đăng không phải của chính người báo cáo (UC33 tiền điều kiện 3) cũng cần đọc đối tượng. Nay `action_global_report` đã mang `conversationId` nên đọc được tin nhắn bằng `ChatRepository.getMessagesOnce`, nhưng `TrustViewModel` vẫn không có chỗ dùng nó. Đề nghị: leader bổ sung đầu ra cho thẻ đối tượng vào `TrustViewModel` (tên do leader chốt). Tạm thời issue này chỉ hiện loại đối tượng, đây là phương án tạm, không phải quyết định.
2. UC33 ngoại lệ 2a nhắc "thiếu mô tả bắt buộc", còn README D04 ghi ô "Mô tả thêm (không bắt buộc)" và STT 61 không nói gì thêm. Issue này làm mô tả không bắt buộc. Xác nhận.
3. UC33 BR2 và ngoại lệ 1b đòi chặn báo cáo trùng khi còn `OPEN`, nhưng `security-rules.md` bảng Giới hạn ghi "Chấp nhận trùng báo cáo" vì mã tài liệu ngẫu nhiên. Issue này chặn ở ứng dụng bằng một truy vấn trong `ReportRepository.create` (máy bị sửa vẫn gửi trùng được). Xác nhận.
