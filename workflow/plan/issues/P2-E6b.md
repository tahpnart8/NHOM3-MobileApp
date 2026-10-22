TITLE: feat(deal): tự hoàn tất, hết hạn xác nhận và việc chạy nền
LABELS: type:feature, area:deal, module:e, priority:p1
ASSIGNEES: tahpnart8
PHASE: P2

Plan item: P2-E6b (phần sau, cuối của P2-E6; xem P2-E6a về lý do tách)

### Bối cảnh
1. PUBGApp là ứng dụng Android mua bán, trao đổi và cho tặng đồ cũ kiêm mạng xã hội, viết bằng Java 17, giao diện XML với ViewBinding, dữ liệu trên Firebase Authentication và Cloud Firestore gói Spark (không có Cloud Functions, không có máy chủ riêng), bộ nhớ đệm Room, việc chạy nền bằng WorkManager.
2. Module E (Quản trị, người làm Phát) gồm các màn E01 đến E07 và việc chạy nền của giao dịch; việc tự động do máy của người dùng làm, quyền thật do Security Rules quyết định (R12): Rules so với giờ máy chủ Firestore, ứng dụng không tự khai được mốc thời gian hay quyền hoàn tất.
3. Issue này làm ba phần: `DealRepository.completeDueDeals` (tự hoàn tất giao dịch RECEIVED sau 3 ngày), `DealRepository.expirePendingDeals` (hủy giao dịch chờ xác nhận quá 3 ngày), và việc chạy nền `AutoReleaseWorker.doWork` cùng `DealViewModel.checkAutoRelease` chạy mỗi lần mở ứng dụng (STT 84, 53).
4. Đọc trước: `workflow/docs/architecture/trang-thai.md` mục 3 (các dòng "Quá 3 ngày kể từ createdAt", "Đủ 3 ngày", và đoạn về `completeDeal`); `workflow/docs/architecture/luong-xu-ly.md` mục 5 (điểm 5 và 6) và mục 1 (bước 6); `workflow/docs/architecture/dac-ta-lop/util.md#autoreleaseworker`.
5. Đã có trên `main`: khung P1 (`DealRepository` đủ chữ ký, `AutoReleaseWorker` khung, `PubgApplication` khởi tạo WorkManager theo P1-04, `Deal.isAutoCompleteDue`, `Deal.isPendingExpired`, `Deal.cancel` đã có test ở P1-03), P2-E6a (`releaseDueDeals`); `DealRepository.completeDeal` là việc của Hải Long (P2-C7).

### Mã tính năng
F-14 (Admin người dùng, nội dung, danh mục, giao dịch, chuyển tiền tự động, tài khoản admin)

### Mã việc kế hoạch
P2-E6, tách thành P2-E6a (`releaseDueDeals`) và P2-E6b (phần còn lại: STT 84 và phần tự hoàn tất của 53). Xem P2-E6a về lý do tách.

### Module
E. Quản trị và nền móng (Phát)

### STT trong bảng đặc tả
84 (việc chạy nền và kiểm tra khi mở ứng dụng cho giải ngân tự động), 53 (phần tự hoàn tất sau 3 ngày). Hết hạn xác nhận 3 ngày của giao dịch chưa có STT riêng trong bảng đặc tả; nó được nêu ở `trang-thai.md` mục 3 và `dac-ta-lop/data.md#dealrepository`.

### Màn hình mockup
Không có màn hình riêng. Hiệu quả nhìn thấy ở C03, C04 (module C) và E06 (P2-E5).

### Làm gì và vì sao
Không có máy chủ, nên việc theo thời hạn do máy của hai bên làm. Mỗi 15 phút `AutoReleaseWorker` chạy nền (WorkManager), và mỗi lần mở ứng dụng `DealViewModel.checkAutoRelease` chạy ngay; cả hai gọi lần lượt `releaseDueDeals` (đã làm ở P2-E6a), `completeDueDeals` và `expirePendingDeals` cho người đang đăng nhập. `completeDueDeals` tìm giao dịch RECEIVED đã quá 3 ngày kể từ `receivedAt` và gọi `completeDeal` của Hải Long (nơi duy nhất đặt COMPLETED và tin SOLD). `expirePendingDeals` tìm giao dịch PENDING_CONFIRM đã quá 3 ngày kể từ `createdAt` mà chưa đủ hai xác nhận, chuyển sang CANCELLED (người đổi `system`), trả tin về AVAILABLE và báo cả hai bên. Worker chạy ở luồng nền nên chờ từng lời gọi bất đồng bộ bằng `Tasks.await` (ngoại lệ duy nhất của luật tầng: một lớp ở `util` gọi repository). Nếu cả hai máy đều không chạy thì việc chậm tới lần mở ứng dụng kế tiếp (rủi ro đã chấp nhận, `overview.md` mục 11). Về barem (`workflow/docs/product/barem-diem.md`): đây là phần dự án đáp ứng chương 7 (dòng R-Chapter 1, 2, 6, 7) về việc chạy nền, bằng `AutoReleaseWorker` là `Worker` của WorkManager chạy `PeriodicWorkRequest` mỗi 15 phút với ràng buộc cần mạng, chờ `DataCallback` bằng `Tasks.await` (async); lưu ý `barem-diem.md` mục 3 ghi WorkManager không phải `Service` (dòng "hở một phần") và mục 4 để leader quyết có thêm `Service` hay viết giải thích trong báo cáo, issue này không tự thêm. Dòng R-Chapter 3 chỉ gián tiếp, vì `Deal.isAutoCompleteDue` và `Deal.isPendingExpired` là phương thức quy tắc đã có unit test JUnit ở P1-03; issue này không thêm test mới và không có thư viện kiểm thử WorkManager trong build (R5).

### Tiêu chí nghiệm thu
- [ ] Tự hoàn tất với Firebase thật: tạo giao dịch RECEIVED, sửa tay trên Console `deals/{id}.receivedAt` về hơn 3 ngày trước, gọi `completeDueDeals` bằng một trong hai bên: giao dịch thành COMPLETED, tin thành SOLD, hai bên có thông báo `DEAL_UPDATE` (do `completeDeal` ghi), callback trả số giao dịch đã hoàn tất. Chưa quá 3 ngày hoặc trạng thái khác thì không đổi; gọi lại lần hai trả 0.
- [ ] Hết hạn xác nhận: tạo giao dịch PENDING_CONFIRM, sửa tay `createdAt` về hơn 3 ngày trước, gọi `expirePendingDeals`: giao dịch thành CANCELLED, `statusHistory` dài thêm đúng một phần tử `PENDING_CONFIRM` sang `CANCELLED` với `changedBy` là `system`, tin về AVAILABLE (qua `Listing.release()`) và `reservedDealId` rỗng, hai bên nhận thông báo `DEAL_UPDATE` ghi trong cùng lô qua `NotificationRepository.create(notification, batch, cb)`; callback trả số giao dịch đã hủy. Giao dịch dưới 3 ngày hoặc khác trạng thái không đổi.
- [ ] `AutoReleaseWorker.doWork` chạy lần lượt `releaseDueDeals`, `completeDueDeals`, `expirePendingDeals`, chờ từng lời gọi xong (có hạn chờ) rồi mới sang lời gọi kế; một lời gọi lỗi không chặn hai lời gọi còn lại; luôn trả `Result.success()` để chu kỳ sau chạy lại, trừ khi bị ngắt (khi đó trả `Result.retry()`). Chưa đăng nhập thì mỗi lời gọi trả 0 và `doWork` vẫn thành công.
- [ ] Việc chạy nền được lên lịch đúng một lần, chu kỳ 15 phút (mức tối thiểu của WorkManager), tên duy nhất, chính sách giữ việc đang có; mở ứng dụng nhiều lần không sinh nhiều việc. Thử bằng cách xem `adb shell dumpsys jobscheduler` hoặc Logcat sau khi chờ một chu kỳ, và ghi kết quả vào PR.
- [ ] `DealViewModel.checkAutoRelease()` được gọi một lần mỗi khi `MainActivity` khởi động mới (không gọi lại khi xoay màn hình) và chỉ khi đã có phiên đăng nhập; gọi ba phương thức repository lần lượt, không chờ việc chạy nền.
- [ ] Lỗi (mất mạng, Rules từ chối, truy vấn thiếu chỉ mục) ở từng phương thức đi qua `onError` với câu tiếng Việt và được ghi bằng `Log` ở nơi gọi nền; không có dialog hay `Toast` làm phiền người dùng khi mở ứng dụng; không crash.
- [ ] Chuyển trạng thái chỉ theo `trang-thai.md` mục 3: PENDING_CONFIRM sang CANCELLED (hết hạn, `system`) và RECEIVED sang COMPLETED (qua `completeDeal`, không tự ghi COMPLETED); kiểm điều kiện bằng `Deal.isPendingExpired`, `Deal.isAutoCompleteDue`, `Deal.canMoveTo`; không viết thẳng `status` ngoài hàm đã có (R18); tin về AVAILABLE qua `Listing.release()`.
- [ ] Mỗi giao dịch hết hạn ghi trong một lô riêng chỉ chứa các khóa Rules cho phép (`deals`: `status`, `statusHistory`, `updatedAt`; `listings`: `status`, `reservedDealId`, `updatedAt`).
- [ ] Không có hàm quy tắc thuần mới: các quy tắc dùng (`Deal.isPendingExpired`, `Deal.isAutoCompleteDue`, `Deal.cancel`) đã có test ở P1-03; PR ghi rõ "không có quy tắc mới" và kèm kết quả thử tay ở trên. Thư viện kiểm thử WorkManager không có trong build (R5) nên không viết test cho Worker.
- [ ] `./gradlew assembleDebug testDebugUnitTest lintDebug` và `sh workflow/scripts/check-java-style.sh` đều qua; PR ghi đúng lệnh và kết quả thật (R1).
- [ ] PR liệt kê mọi lớp, phương thức, trường công khai mới kèm dòng tài liệu định nghĩa (R16), hoặc ghi "không có".

### Tài liệu phải đọc
- `AGENTS.md` mục 6: R5, R12, R16, R17, R18, R20, R21, R23.
- `workflow/plan/so-huu.md` mục 5 (`DealRepository`: `releaseDueDeals`, `completeDueDeals`, `listAll` của Phát; `DealViewModel.checkAutoRelease` của Phát; `AutoReleaseWorker` của Phát).
- `workflow/docs/product/dac-ta-chuc-nang.md` dòng STT 53 và 84.
- `workflow/docs/architecture/trang-thai.md` mục 1 (dòng "Giao dịch của tin bị hủy" và "hoàn tất"), mục 3 (bảng, đoạn "`DealRepository.completeDeal` là nơi duy nhất..."), mục 9.
- `workflow/docs/architecture/luong-xu-ly.md` mục 1 (bước 6) và mục 5 (điểm 5, 6).
- `workflow/docs/architecture/dac-ta-lop/data.md#dealrepository` (dòng `completeDeal`, `completeDueDeals`, `expirePendingDeals`, `releaseDueDeals`), `util.md#autoreleaseworker`, `viewmodel.md#dealviewmodel` (dòng `checkAutoRelease`).
- `workflow/docs/architecture/dac-ta-lop/model.md#deal` (các phương thức `isPendingExpired`, `isAutoCompleteDue`, `cancel`, `canMoveTo`, `complete`), `#listing` (trường `reservedDealId`, phương thức `markSold`), `#statuschange`.
- `workflow/docs/data/firestore-schema.md#deals`, `#listings`, và bảng "Chỉ mục ghép dự kiến" (các dòng `deals`); `workflow/docs/data/security-rules.md` mục 14 (dòng "Hết hạn xác nhận", "Hoàn tất (đủ 3 ngày)"), mục 10 (dòng "Nhả giữ chỗ khi giao dịch hủy"), mục "Luật chung" số 7.
- `workflow/docs/architecture/overview.md` mục 5 (khởi tạo ứng dụng) và mục 11 (rủi ro việc tự động).
- `workflow/memory/code/README.md`, `workflow/memory/code/firebase.md`, `workflow/docs/conventions/android-java.md`, skill `pubg-code`.

### Lớp, trường, trạng thái sẽ dùng
- `DealRepository`: `public void completeDueDeals(DataCallback<Integer> cb)`, `public void expirePendingDeals(DataCallback<Integer> cb)` (thay thân); chỉ gọi: `public void releaseDueDeals(DataCallback<Integer> cb)` (P2-E6a), `public void completeDeal(String dealId, DataCallback<Void> cb)` (của Hải Long, P2-C7; có thể còn `NOT_READY`).
- `AutoReleaseWorker`: `public Result doWork()` (thay thân).
- `DealViewModel`: `public void checkAutoRelease()` (thay thân; các phương thức khác của Hải Long).
- `Deal`: `buyerId`, `sellerId`, `listingId`, `status`, `createdAt`, `receivedAt`, `statusHistory`; `public boolean isPendingExpired(Timestamp)`, `public boolean isAutoCompleteDue(Timestamp)`, `public boolean canMoveTo(DealStatus)`, `public void cancel(String userId, String reason)`, `getId()`.
- `StatusChange`: `fromStatus`, `toStatus`, `changedBy` (giá trị `system`), `reason`, `changedAt`.
- `Listing`: `status`, `reservedDealId`, `public void release()` (RESERVED về AVAILABLE và xóa `reservedDealId`). `AppNotification`; `NotificationType.DEAL_UPDATE`; `NotificationRepository.create(AppNotification notification, WriteBatch batch, DataCallback<Void> cb)` (chỉ repository gọi; truyền lô ghi đang dựng).
- `DealStatus`: `PENDING_CONFIRM`, `CANCELLED`, `RECEIVED`, `COMPLETED`. `ListingStatus`: `RESERVED`, `AVAILABLE`.
- Thư viện đã có sẵn trong build từ P1-02 (không thêm, R5): `androidx.work.Worker`, `androidx.work.PeriodicWorkRequest`, `androidx.work.WorkManager`, `androidx.work.ExistingPeriodicWorkPolicy`, `com.google.android.gms.tasks.Tasks`, `com.google.android.gms.tasks.TaskCompletionSource`. Chữ ký đã tra tài liệu chính thức: `Tasks.await(Task<TResult> task, long timeout, TimeUnit unit)`, `TaskCompletionSource.setResult(TResult)`, `TaskCompletionSource.setException(Exception)`, `TaskCompletionSource.getTask()`, `new PeriodicWorkRequest.Builder(Class, long, TimeUnit)`, `WorkManager.enqueueUniquePeriodicWork(String, ExistingPeriodicWorkPolicy, PeriodicWorkRequest)`.
- Firestore: `deals/{id}`, `listings/{id}`, `notifications`. Chỉ mục `deals`: `buyerId`+`status`+`receivedAt`, `sellerId`+`status`+`receivedAt`, `buyerId`+`status`+`createdAt`, `sellerId`+`status`+`createdAt` (đã có trong "Chỉ mục ghép dự kiến").

### File được tạo hoặc sửa
- [sửa] PUBGApp/app/src/main/java/com/nhom3/pubgapp/data/DealRepository.java (chỉ thân `completeDueDeals`, `expirePendingDeals`)
- [sửa] PUBGApp/app/src/main/java/com/nhom3/pubgapp/util/AutoReleaseWorker.java (thân `doWork` và các hàm riêng chờ kết quả)
- [sửa] PUBGApp/app/src/main/java/com/nhom3/pubgapp/viewmodel/DealViewModel.java (chỉ thân `checkAutoRelease`)
- [sửa] PUBGApp/app/src/main/java/com/nhom3/pubgapp/ui/main/MainActivity.java (gọi `checkAutoRelease` một lần khi khởi động mới và đã đăng nhập; đường dẫn theo khung P1-06)
- [sửa] PUBGApp/app/src/main/java/com/nhom3/pubgapp/PubgApplication.java (chỉ nếu P1-04 chưa lên lịch `AutoReleaseWorker`; nếu đã có thì không đụng, ghi vào PR)
- [tạo] workflow/memory/code/android.md (một hoặc hai mục ngắn: chờ `DataCallback` trong `doWork` bằng `TaskCompletionSource` và `Tasks.await`; WorkManager tối thiểu 15 phút; có nguồn và ngày kiểm)

### Các bước đề nghị
Có thể làm theo thứ tự khác, nhưng không được đổi tên lớp, phương thức, thuộc tính nào.
1. `DealRepository.completeDueDeals(DataCallback<Integer> cb)`: `currentUserId()` rỗng thì `cb.onSuccess(0)`; chạy hai truy vấn `deals` (`buyerId == uid` và `sellerId == uid`), cả hai có `status == RECEIVED` và `receivedAt <=` giờ hiện tại (dùng chỉ mục `receivedAt` đã có; mốc 3 ngày do `Deal.isAutoCompleteDue` kiểm, không tự viết số 3 ở nơi khác vì hằng `AUTO_COMPLETE_DAYS` là private); khử trùng theo mã; với mỗi giao dịch kiểm `Deal.isAutoCompleteDue(now)`, rồi gọi `completeDeal(dealId, ...)` lần lượt từng giao dịch (không song song) và đếm thành công. Không tự ghi COMPLETED hay SOLD.
2. `DealRepository.expirePendingDeals(DataCallback<Integer> cb)`: như bước 1 nhưng `status == PENDING_CONFIRM` và `createdAt <=` giờ hiện tại; mốc 3 ngày do `Deal.isPendingExpired` kiểm; kiểm `Deal.isPendingExpired(now)` và `Deal.canMoveTo(DealStatus.CANCELLED)`. Với mỗi giao dịch, đọc `listings/{listingId}`; gọi `Deal.cancel("system", lý do hệ thống)` (lý do là một câu cố định, ví dụ "Quá 3 ngày chưa xác nhận") để dựng trạng thái và dòng lịch sử; nếu tin còn `RESERVED` với `reservedDealId` đúng giao dịch này thì gọi `Listing.release()` (tin không còn thì chỉ hủy giao dịch). Một lô ghi riêng cho giao dịch đó: `deals/{id}` chỉ `status`, `statusHistory`, `updatedAt` (không ghi `cancelReason` vì Rules mục 14 dòng "Hết hạn xác nhận" không cho; Câu hỏi 1), `listings/{listingId}` chỉ `status`, `reservedDealId`, `updatedAt` khi tin cần nhả, cùng thông báo `DEAL_UPDATE` cho cả `buyerId` và `sellerId` (`targetId` là mã giao dịch) đưa vào lô bằng `NotificationRepository.create(notification, batch, cb)`; chờ `cb` của từng lời gọi `create` rồi mới commit lô (cách `create` thêm vào lô cần đọc thân của P1, chưa kiểm chứng).
3. Cả hai phương thức trả `cb.onSuccess(số thành công)`; nếu không giao dịch nào thành công mà có lỗi thì `cb.onError(câu lỗi đầu tiên)`; lỗi truy vấn đi qua `fail(cb, e)`. Mỗi giao dịch lỗi không dừng các giao dịch còn lại.
4. `AutoReleaseWorker.doWork()`: tạo `DealRepository` đúng cách các ViewModel của P1-04 tạo repository; với mỗi trong ba lời gọi, tạo một `TaskCompletionSource<Integer>`, gọi phương thức repository với một `DataCallback<Integer>` là lớp ẩn danh gọi `setResult` ở `onSuccess` và `setException` ở `onError`, rồi `Tasks.await(source.getTask(), AWAIT_SECONDS, TimeUnit.SECONDS)` (hằng riêng `private static final long AWAIT_SECONDS`). Bắt `ExecutionException` và `TimeoutException` bằng `Log` rồi sang lời gọi kế; bắt `InterruptedException` thì đặt lại cờ ngắt và trả `Result.retry()`. Cuối cùng trả `Result.success()`. Viết ba lần gọi bằng một hàm riêng nhận một "việc" là lớp ẩn danh nhỏ để không lặp mã; hàm dưới 30 dòng.
5. Kiểm lịch chạy nền trong `PubgApplication`: phải có `PeriodicWorkRequest.Builder(AutoReleaseWorker.class, 15, TimeUnit.MINUTES)` với ràng buộc cần mạng, và `WorkManager.enqueueUniquePeriodicWork(tên duy nhất, ExistingPeriodicWorkPolicy.KEEP, yêu cầu)`. Có rồi thì không đụng; chưa có thì thêm đúng như vậy.
6. `DealViewModel.checkAutoRelease()`: gọi lần lượt `releaseDueDeals`, `completeDueDeals`, `expirePendingDeals` của `DealRepository` bằng ba `DataCallback<Integer>` là lớp ẩn danh; kết quả bỏ qua, lỗi chỉ ghi `Log` (không đặt vào `error`, để không hiện thông báo khi mở ứng dụng; không bật `loading` hay `done`); không giữ tham chiếu tới Activity. Gọi tiếp lời gọi sau trong `onSuccess` hoặc `onError` của lời gọi trước để chạy tuần tự.
7. `MainActivity.onCreate`: khi `savedInstanceState` là `null` và `SessionManager.getInstance().isLoggedIn()` là true thì gọi `checkAutoRelease` của một `DealViewModel` lấy bằng `ViewModelProvider`. Xoay màn hình không gọi lại. Phiên khôi phục bất đồng bộ: Câu hỏi 3.
8. Thử tay như mục "Tự kiểm trước khi mở Pull Request". Để thử Worker không chờ 15 phút, thêm tạm một nút cục bộ (không commit) đưa `OneTimeWorkRequest` của `AutoReleaseWorker` vào hàng đợi, rồi xem Logcat.
9. Ghi `workflow/memory/code/android.md`: mục "Chờ DataCallback trong Worker" (Fact: `doWork` chạy ở luồng nền, nên chặn bằng `Tasks.await` được, không bao giờ gọi trên luồng chính; Source: tài liệu `Tasks.await` của Google Play services, ngày kiểm) và mục "WorkManager chu kỳ tối thiểu 15 phút".
10. Chạy hai lệnh kiểm ở mục "Tự kiểm trước khi mở Pull Request" và ghi kết quả vào PR.

### File dùng chung cần thêm dòng
Không có. Ghi vào mô tả PR để P3-R gộp: luật "Hết hạn xác nhận" và "Hoàn tất (đủ 3 ngày)" của `deals` (security-rules.md mục 14), "Nhả giữ chỗ khi giao dịch hủy" của `listings` (mục 10), tạo `notifications` (mục 21); chỉ mục `deals`: `buyerId`+`status`+`receivedAt`, `sellerId`+`status`+`receivedAt`, `buyerId`+`status`+`createdAt`, `sellerId`+`status`+`createdAt`.

### Cấm đụng
- Leader-only không có trong danh sách trên: `AndroidManifest.xml` (không cần khai báo Worker), `nav_main.xml`, `nav_admin.xml`, `themes.xml`, `colors.xml`, `dimens.xml`, `strings_common.xml`, `AppDatabase`, `Converters`, mọi file Gradle (WorkManager và Firebase đã có từ P1-02), `firebase/`.
- Module C (Hải Long): mọi phương thức khác của `DealRepository` và `DealViewModel` (`completeDeal`, `cancel`, `markShipped`, `markReceived`, `pay`...), `DealDao`, `DealEntity`, màn C03 và C04.
- Thân `releaseDueDeals` (P2-E6a) và `listAll` (P2-E5).
- `Deal`, `Listing`, `StatusChange` (lớp mô hình: chỉ gọi, không thêm phương thức).

### Phụ thuộc
P2-E6a, P1-04. Mềm: P2-C7 (`completeDeal`; chưa xong thì `completeDueDeals` báo `NOT_READY`, hai phương thức còn lại vẫn chạy), P2-C3 (giao dịch thật để hết hạn).

### Ngoài phạm vi
- `completeDeal` (Hải Long), hiển thị đếm ngược và nút "Đã nhận" (C03).
- Thông báo cho người dùng khi việc nền chạy, màn hình báo kết quả: không làm.
- Cảnh báo giao dịch đứng yên quá lâu hoặc hủy liên tiếp (STT 83), xử lý giao dịch DISPUTED không có admin (STT 85): Phát triển sau.
- Chạy việc nền khi ứng dụng đã bị tắt hẳn bởi người dùng hoặc khi máy offline: giới hạn của WorkManager, chấp nhận.
- Đổi chu kỳ 15 phút hay hạn 3 ngày thành tham số admin chỉnh được (STT 97): Phát triển sau.

### Tài liệu cần cập nhật
`workflow/memory/code/android.md` (bước 9). Ngoài ra không có, trừ khi Câu hỏi 1 được trả lời bằng đổi tài liệu: khi đó cập nhật `workflow/docs/architecture/trang-thai.md` mục 3 và `security-rules.md` mục 14.

### Tự kiểm trước khi mở Pull Request
- [ ] Đã thử tay `completeDueDeals`: giao dịch RECEIVED có `receivedAt` sửa về trên 3 ngày thành COMPLETED, tin thành SOLD; gọi lại trả 0.
- [ ] Đã thử tay `expirePendingDeals`: giao dịch PENDING_CONFIRM có `createdAt` sửa về trên 3 ngày thành CANCELLED, `statusHistory` có dòng `system`, tin về AVAILABLE kèm `reservedDealId` rỗng, hai thông báo `DEAL_UPDATE`.
- [ ] Đã thử `AutoReleaseWorker` bằng nút tạm cục bộ (đã xóa trước khi commit): Logcat thấy ba lời gọi chạy lần lượt và kết thúc `Result.success()` kể cả khi một lời gọi lỗi (thử bằng cách tắt mạng giữa chừng).
- [ ] Đã kiểm hàng đợi: `adb shell dumpsys jobscheduler | grep -i pubgapp` hoặc tương đương cho thấy đúng một việc định kỳ.
- [ ] Mở ứng dụng với tài khoản có giao dịch quá hạn: giao dịch tự đổi trạng thái mà không có hộp thoại nào; xoay màn hình không chạy lại `checkAutoRelease` (xem Logcat).
- [ ] `cd PUBGApp && ./gradlew assembleDebug testDebugUnitTest lintDebug` kết thúc `BUILD SUCCESSFUL`.
- [ ] `sh workflow/scripts/check-java-style.sh` không báo lỗi.
- [ ] `git diff --stat` chỉ thấy các file trong danh sách trên; không có file layout hay chuỗi giao diện.
- [ ] Đã ghi `workflow/memory/code/android.md` và nói trong PR.
- [ ] Đã liệt kê trong PR mọi lớp, phương thức, trường công khai mới kèm dòng tài liệu định nghĩa (R16), hoặc ghi "không có".

### Quy mô
- [x] Đây là một lát cắt dọc của một module, khoảng nửa ngày đến một ngày, một Pull Request.

### Câu hỏi cho leader
1. Rules mục 14, dòng "Hết hạn xác nhận", chỉ cho ghi `status`, `statusHistory`, `updatedAt` nên không ghi được `cancelReason`, trong khi `Deal.cancel(String userId, String reason)` bắt buộc có lý do và là hàm duy nhất tài liệu có để sang CANCELLED khi hết hạn. Tạm thời gọi `Deal.cancel("system", ...)` để dựng dòng lịch sử (lý do nằm trong `StatusChange.reason`) và không ghi `cancelReason`. Hệ quả: C03 không thấy `cancelReason` của giao dịch hết hạn, chỉ thấy lý do ở lịch sử. Đề xuất: cho phép thêm `cancelReason` vào `onlyKeys` của dòng đó, hoặc xác nhận cách tạm thời.
2. `so-huu.md` mục 5 dòng `DealRepository` ghi "tất cả trừ hai dòng dưới" cho Hải Long, hai dòng dưới là `releaseDueDeals`, `completeDueDeals`, `listAll` của Phát; `expirePendingDeals` không nằm ở dòng nào, còn kế hoạch P2-E6 giao cho Phát. Tạm thời Phát làm `expirePendingDeals` trong issue này. Xác nhận và sửa `so-huu.md`.
3. `luong-xu-ly.md` mục 1 bước 6 nói `PubgApplication` hoặc màn hình khởi động gọi `AuthViewModel.restoreSession` rồi mới chạy `DealViewModel.checkAutoRelease`, nhưng `restoreSession` bất đồng bộ nên khi `MainActivity` khởi động `SessionManager` có thể chưa có phiên. Tạm thời gọi `checkAutoRelease` ở `MainActivity.onCreate` khi `isLoggedIn()` đã true; nếu phiên chưa sẵn sàng thì lần mở sau, hoặc việc chạy nền 15 phút, sẽ xử lý. Cần Ngân (P2-A1) cho biết `restoreSession` chạy ở đâu, để gọi `checkAutoRelease` đúng lúc.
4. Mỗi thiết bị đang đăng nhập chạy 6 truy vấn mỗi 15 phút (ba phương thức, mỗi phương thức hai vai trò), tương đương khoảng 576 lượt truy vấn mỗi ngày; chấp nhận được cho gói Spark (50.000 lượt đọc mỗi ngày) ở quy mô demo năm máy nếu mỗi truy vấn trả ít tài liệu. Xác nhận.
