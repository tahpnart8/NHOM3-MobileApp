TITLE: feat(deal): giao hàng, xác nhận đã nhận kèm ảnh, đếm ngược
LABELS: type:feature, area:deal, module:c, priority:p1
ASSIGNEES: Contest451
PHASE: P2

Plan item: P2-C5b

### Bối cảnh
1. PUBGApp (Pre-owned Users' Bargain Grounds) là ứng dụng Android mua bán, trao đổi và cho tặng đồ đã qua sử dụng: Java 17, XML Views với ViewBinding, Firebase Authentication và Cloud Firestore gói Spark (không Cloud Storage, ảnh lưu Base64 trong Firestore), Room làm bộ nhớ đệm, minSdk 30.
2. Module C (Chat và giao dịch) do Nguyễn Lê Hải Long (`Contest451`) sở hữu: gói giao diện `com.nhom3.pubgapp.ui.trade`, tiền tố layout `trade_`, chuỗi trong `strings_trade.xml`, đồ thị điều hướng `nav_trade.xml`.
3. Issue này hoàn thiện nửa sau của màn C03 Chi tiết giao dịch (`DealDetailFragment`): người bán xác nhận đã giao kèm ảnh vận đơn (UC28), người mua xác nhận đã nhận kèm một ảnh (UC22), đồng hồ đếm ngược 7 ngày, lối sang khiếu nại.
4. Đọc trước: `workflow/docs/product/use-case/uc-c-giao-dich.md` mục UC28 và UC22; `workflow/docs/ui/c-ket-noi-giao-dich/README.md` mục C03; `workflow/docs/architecture/trang-thai.md` mục 3 và 4.
5. Trên `main` đã có: khung P1 (mọi lớp đủ chữ ký, `MediaRepository` làm thật, `Deal.markShipped`, `Deal.markReceived`, `Payment.release` kèm test), và từ P2-C4 và P2-C5a: C03 với tiến trình, thông tin giao dịch, sửa điều khoản, xác nhận, hủy, `DealRepository.pay`, màn C07 và `Payment` HELD.

### Mã tính năng
F-09, F-10

### Mã việc kế hoạch
P2-C5b (phần giao hàng, nhận hàng và đếm ngược của P2-C5)

### Module
C. Chat và giao dịch (Hải Long)

### STT trong bảng đặc tả
53, 54, 84 (phần hiển thị đồng hồ; việc tự chuyển khi hết hạn là của Phát)

### Màn hình mockup
C03 (Chi tiết giao dịch): biểu mẫu "Xác nhận đã giao" của người bán, hộp xác nhận "Xác nhận đã nhận hàng" của người mua, khối thông tin vận đơn và ảnh nhận hàng, khối đếm ngược, nút "Khiếu nại giao dịch" và "Xem khiếu nại". Ảnh: `workflow/docs/ui/c-ket-noi-giao-dich/c03-chi-tiet-giao-dich.png` (ảnh chưa vẽ biểu mẫu giao hàng, đếm ngược và bước chọn ảnh nhận hàng; vẫn phải làm theo README).

### Làm gì và vì sao
Với giao dịch gửi vận chuyển đã "Đã xác nhận" (và đã thanh toán nếu giá lớn hơn 0), người bán nhập đơn vị vận chuyển, mã vận đơn, chụp hoặc chọn ảnh vận đơn rồi bấm "Xác nhận đã giao": giao dịch sang "Đã giao" và bắt đầu đồng hồ đếm ngược 7 ngày hiện cho cả hai bên. Người mua bấm "Xác nhận đã nhận hàng" kèm một ảnh món đồ đã nhận (bắt buộc, cả gửi vận chuyển lẫn gặp trực tiếp): giao dịch sang "Đã nhận" và khoản thanh toán chuyển từ "Đang giữ" sang "Đã giải ngân cho người bán" trong cùng lô ghi. Trong thời gian đếm ngược hai bên có lối sang khiếu nại của module D. Hết đếm ngược thì máy của một trong hai bên (việc của Phát) tự chuyển sang "Đã nhận", khi đó không có ảnh nhận hàng. Chỉ tài khoản đã được admin duyệt xác thực mới giao hoặc xác nhận nhận được; khiếu nại và hủy thì không đòi xác thực. Luồng: UC28, UC22; `luong-xu-ly.md` mục 5. Kế hoạch ghi P2-C5 là một việc, được tách thành P2-C5a (thanh toán, màn C07) và P2-C5b (issue này). Issue này phục vụ các dòng barem R-Chapter 1, 2, 6, 7 (chương 2: implicit Intent để chọn ảnh vận đơn và ảnh nhận hàng bằng bộ chọn ảnh của hệ thống qua `ActivityResultContracts`, chụp ảnh nếu leader duyệt Câu hỏi 4, và vòng đời: đồng hồ đếm ngược dừng ở `onStop` rồi chạy lại khi màn hiển thị; chương 7: bất đồng bộ qua `DataCallback`, kiểm tra kết nối mạng bằng `NetworkMonitor`, đồng hồ vẫn chạy ngoại tuyến nhờ Room), R-Chapter 3 (unit test JUnit cho các quy tắc `Deal.markShipped`, `Deal.markReceived` và phép tính thời gian còn lại) và R-Chapter 4 (input control: hai ô nhập đơn vị vận chuyển và mã vận đơn, nút "Xác nhận" chỉ bấm được khi đủ điều kiện).

### Tiêu chí nghiệm thu
Các chuyển trạng thái của issue này (theo `trang-thai.md` mục 3 và 4):

| Từ | Sự kiện | Đến | Điều kiện kiểm trước khi ghi | Phương thức | Hệ quả trong cùng lô |
| --- | --- | --- | --- | --- | --- |
| `CONFIRMED` | Người bán giao hàng | `SHIPPED` | Người gọi là `sellerId`; `User.canSell(emailVerified)` đúng; `deliveryMethod` là SHIPPING; `Deal.hasShippingProof()` đúng; nếu `needsPayment()` thì `payments/{dealId}` tồn tại và đang `HELD` | `Deal.markShipped(carrier, trackingCode, imageId)`, `DealRepository.markShipped(dealId, carrier, trackingCode, imageId, cb)` | Ghi `carrierName`, `trackingCode`, `trackingImageId`; `shippedAt` giờ máy chủ; `autoReleaseAt` bằng `shippedAt` cộng 7 ngày (Câu hỏi 1); thêm đúng một dòng `statusHistory` (`CONFIRMED` sang `SHIPPED`); thông báo DEAL_UPDATE cho người mua |
| `CONFIRMED` | Nhận hàng khi gặp trực tiếp | `RECEIVED` | Người gọi là `buyerId`; `User.canBuy(emailVerified)` đúng; `deliveryMethod` là MEET_UP; có ảnh nhận hàng; nếu `needsPayment()` thì `Payment` đang `HELD` | `Deal.markReceived(imageId)`, `DealRepository.markReceived(dealId, imageId, cb)` | `receivedAt` giờ máy chủ; `receivedImageId`; thêm đúng một dòng `statusHistory` (`CONFIRMED` sang `RECEIVED`); `Payment` `HELD` sang `RELEASED` (`Payment.release()`, `releasedAt` giờ máy chủ) nếu có; thông báo PAYMENT_UPDATE cho người bán |
| `SHIPPED` | Người mua xác nhận đã nhận | `RECEIVED` | Người gọi là `buyerId`; `User.canBuy` đúng; có ảnh nhận hàng; nếu `needsPayment()` thì `Payment` đang `HELD`; người mua xác nhận được bất cứ lúc nào khi còn `SHIPPED` | `Deal.markReceived(imageId)`, `DealRepository.markReceived` | Như dòng trên (`SHIPPED` sang `RECEIVED`) |
| `Payment` `HELD` | Giao dịch sang `RECEIVED` | `RELEASED` | Cùng lô với việc giao dịch sang `RECEIVED` | `Payment.release()` | `releasedAt` giờ máy chủ; không có đường nào khác trong issue này đặt `RELEASED` |

- [ ] Giao hàng chạy với Firebase thật (UC28): người bán chọn hoặc chụp ảnh vận đơn, nhập đơn vị vận chuyển và mã vận đơn, bấm "Xác nhận đã giao" thì `DealViewModel.markShipped(carrier, trackingCode, photoUri)` gọi `MediaRepository.saveImage(photoUri, ImagePurpose.WAYBILL, cb)` lấy `imageId`, rồi `DealRepository.markShipped`. Kết quả: `deals/{id}` ở `SHIPPED`, có `carrierName`, `trackingCode`, `trackingImageId`, `shippedAt`, `autoReleaseAt`, `statusHistory` dài thêm đúng một dòng; người mua nhận DEAL_UPDATE.
- [ ] Nút "Xác nhận" của biểu mẫu giao hàng chưa bấm được khi thiếu một trong ba: ảnh, tên đơn vị, mã vận đơn (chữ rỗng sau khi bỏ khoảng trắng tính là thiếu). `Deal.markShipped` và `DealRepository.markShipped` cũng từ chối khi thiếu (UC28 ngoại lệ 2a).
- [ ] Giao hàng bị từ chối với thông báo tiếng Việt khi: không phải người bán; `deliveryMethod` là MEET_UP (không hiện nút); giao dịch chưa `CONFIRMED`; đã hủy hoặc đang tranh chấp; giá lớn hơn 0 mà chưa có `Payment` `HELD` (người bán thấy dòng "Chờ người mua thanh toán" thay cho biểu mẫu, UC28 ngoại lệ 1c). Giao dịch giá 0 giao được không cần `Payment`.
- [ ] Ảnh không đọc được hoặc vượt giới hạn dung lượng thì `MediaRepository.saveImage` trả lỗi: hiện câu yêu cầu chọn ảnh khác, giữ nguyên chữ đã nhập (UC28 ngoại lệ 2b, 2c). Ghi `markShipped` lỗi sau khi ảnh đã ghi thì xóa ảnh đó bằng `MediaRepository.delete` và báo lỗi, không để ảnh mồ côi.
- [ ] Nhận hàng chạy với Firebase thật (UC22): người mua bấm "Xác nhận đã nhận hàng" thì hiện hộp nhắc (ngay trong màn): xác nhận không thể thu hồi, sau khi xác nhận không khiếu nại được nữa, và yêu cầu chụp hoặc chọn một ảnh món đã nhận. Nút "Xác nhận" của hộp chưa bấm được khi chưa có ảnh. Bấm "Xác nhận" thì `DealViewModel.markReceived(photoUri)` gọi `MediaRepository.saveImage(photoUri, ImagePurpose.RECEIVED, cb)` lấy `imageId`, rồi `DealRepository.markReceived(dealId, imageId, cb)` (repository nhận mã ảnh đã lưu, không tự lưu ảnh, giống `markShipped`). Kết quả cùng lúc: `deals/{id}` sang `RECEIVED` có `receivedAt`, `receivedImageId` và dòng lịch sử mới; `payments/{dealId}` sang `RELEASED` có `releasedAt` (nếu có); người bán nhận PAYMENT_UPDATE (`targetId` là `dealId`). Hộp thanh toán của C03 hiện "Đã giải ngân cho người bán".
- [ ] Nhận hàng gặp trực tiếp chạy: giao dịch `CONFIRMED` có `deliveryMethod` MEET_UP, người mua (đã thanh toán nếu giá lớn hơn 0) bấm "Xác nhận đã nhận hàng" kèm ảnh thì sang `RECEIVED` và giải ngân như trên, không qua `SHIPPED`, không có đếm ngược; người bán thấy "Chờ người mua".
- [ ] Giao dịch giá 0 (trao đổi, cho tặng): "Xác nhận đã nhận hàng" chạy không cần `Payment`, không tạo thông báo PAYMENT_UPDATE (đúng bảng ở `trang-thai.md` mục 9; xem Câu hỏi 3).
- [ ] Nhận hàng bị từ chối với thông báo tiếng Việt, không ghi gì: người gọi không phải người mua; giao dịch gửi vận chuyển mà người bán chưa giao (UC22 ngoại lệ 1a, nút không hiện); giao dịch gặp trực tiếp giá lớn hơn 0 mà `Payment` chưa `HELD` (nút ẩn, hiện "Hãy thanh toán trước"); giao dịch đã hủy hoặc đang tranh chấp (ngoại lệ 1b); giao dịch đã `RECEIVED` (bấm đúp hoặc hai máy); chưa có ảnh.
- [ ] Ảnh nhận hàng chỉ có một ảnh; ghi `markReceived` lỗi sau khi ảnh đã ghi thì xóa ảnh bằng `MediaRepository.delete`. Sau khi nhận, C03 của cả hai bên hiện ảnh nhận hàng (đọc bằng `MediaRepository.getImage`, chỉ khi có mạng); giao dịch tự nhận do hết đếm ngược thì không có ảnh và không hiện khối ảnh.
- [ ] Sau khi giao: C03 của cả hai bên hiện đơn vị vận chuyển, mã vận đơn và ảnh vận đơn (đọc bằng `MediaRepository.getImage`, chỉ khi có mạng).
- [ ] Đồng hồ đếm ngược: khi giao dịch `SHIPPED`, C03 của cả hai bên hiện "Còn N ngày để xác nhận" (khi còn dưới 1 ngày hiện giờ và phút) tính từ `autoReleaseAt`, cập nhật theo thời gian khi màn mở. Hết hạn thì hiện "Đã hết đếm ngược, hệ thống sẽ tự chuyển sang Đã nhận" và ẩn nút "Khiếu nại giao dịch". Bộ đếm dừng khi màn không hiển thị và chạy lại khi hiển thị trở lại; xoay màn hình không làm mất hoặc nhảy số. Giờ máy chỉ để hiển thị; mốc thật do Security Rules so với giờ máy chủ.
- [ ] Đồng hồ hiện cả khi mất mạng, lấy `autoReleaseAtMillis` từ Room (`DealEntity`); có dải ngoại tuyến; các nút ghi bị khóa.
- [ ] Việc tự chuyển `SHIPPED` sang `RECEIVED` khi hết hạn (`DealRepository.releaseDueDeals`, `Deal.autoReceive`) là của Phát; issue này chỉ hiển thị đồng hồ và không tự gọi nó. Khi Phát xong, C03 đang mở cập nhật sang "Đã nhận" sau lần tải lại.
- [ ] Nút trên C03 theo bảng "Hành động theo trạng thái và vai trò" của README nhóm C: `SHIPPED`: người mua có "Xác nhận đã nhận hàng" và "Khiếu nại giao dịch", người bán có đồng hồ và "Khiếu nại giao dịch"; `CONFIRMED` gửi vận chuyển: người mua thấy "Chờ người bán giao", người bán có "Xác nhận đã giao"; `CONFIRMED` gặp trực tiếp: người mua có "Xác nhận đã nhận hàng" (sau khi thanh toán nếu giá lớn hơn 0) và "Khiếu nại giao dịch", người bán thấy "Chờ người mua"; `DISPUTED` (nhãn "Tranh chấp"): cả hai có "Xem khiếu nại" gọi `action_global_my_reports`; `RECEIVED` và `COMPLETED`: không còn nút khiếu nại.
- [ ] "Khiếu nại giao dịch" chỉ hiện khi giao dịch `SHIPPED` và còn trước `autoReleaseAt` (theo giờ máy, chỉ để hiện nút), hoặc `CONFIRMED` gặp trực tiếp; bấm thì gọi `action_global_create_complaint` với `dealId`. Nút này không bao giờ bị chặn vì chưa xác thực (UC24 BR6). Đích là màn của module D; nếu còn là khung trống thì vẫn gọi đúng hành động.
- [ ] Tài khoản chưa được duyệt xác thực: "Xác nhận đã giao" và "Xác nhận đã nhận hàng" bị chặn kèm lời giải thích và nút "Đăng ký xác thực" gọi `action_global_my_profile` (cách Fragment biết để hiện: Câu hỏi 7); "Khiếu nại giao dịch" và "Hủy giao dịch" vẫn dùng được.
- [ ] Mọi chuyển trạng thái đi qua `Deal.markShipped`, `Deal.markReceived` và `Payment.release`; không code nào gán trực tiếp `Deal.status` hay `Payment.status`; chuyển không có trong `trang-thai.md` bị từ chối (`Deal.canMoveTo`).
- [ ] Mất mạng khi bấm: nút khóa; thời hạn chờ 15 giây, quá hạn thì báo lỗi tiếng Việt.
- [ ] Trạng thái nằm trong `DealViewModel` (`deal`, `payment`): xoay màn hình giữ giao dịch và khoản thanh toán, không gửi lệnh lần hai. Chữ đã nhập là `EditText` có `id` nên Android tự khôi phục; ảnh đã chọn (`Uri`) và vùng đang mở (biểu mẫu giao hàng, hộp xác nhận nhận hàng) không có tên trong `viewmodel.md`, nên giữ bằng `onSaveInstanceState` của Fragment với khóa `private static final`, không thêm thuộc tính công khai vào ViewModel (R16); ghi một dòng bằng skill `pubg-record-decision`. Trạng thái "đang xử lý" của nút lấy từ `DealViewModel.loading`; `done` bật một lần khi lệnh ghi xong, Fragment đặt lại `done` và xóa `error` sau khi dùng.
- [ ] Mọi lỗi (Rules từ chối, mất mạng, `NOT_READY`) đi qua `DealViewModel.error` và hiện thông báo tiếng Việt; không nuốt lỗi.
- [ ] Unit test `DealRepositoryTest` (phần quyết định tách thành phương thức `static` cấp gói không dùng Firebase, không thêm phương thức công khai): `markShipped` từ chối khi MEET_UP, thiếu bằng chứng, người bán chưa có `Payment` `HELD` khi giá lớn hơn 0, và chấp nhận khi giá 0 không có `Payment`; mốc `autoReleaseAt` bằng `shippedAt` cộng 7 ngày (hàm tính mốc nhận thời điểm làm tham số); `markReceived` chấp nhận `SHIPPED` và `CONFIRMED` gặp trực tiếp, từ chối `CONFIRMED` giao vận chuyển, từ chối người bán, từ chối khi thiếu ảnh, từ chối khi giá lớn hơn 0 mà `Payment` không `HELD`, chấp nhận khi giá 0 không có `Payment`; chỉ giải ngân khi `Payment` đang `HELD`. `DealViewModelTest`: phép tính thời gian còn lại (nhận `autoReleaseAt` và thời điểm hiện tại làm tham số: còn nhiều ngày, còn dưới 1 ngày, đúng 0, đã quá hạn) và điều kiện hiện từng nút theo trạng thái, vai trò, xác thực. Không thêm thư viện kiểm thử (R5).
- [ ] `./gradlew assembleDebug testDebugUnitTest lintDebug` và `sh workflow/scripts/check-java-style.sh` đều qua.
- [ ] Pull Request liệt kê mọi lớp, phương thức, trường công khai mới kèm dòng tài liệu định nghĩa nó (R16).

### Tài liệu phải đọc
- `workflow/plan/so-huu.md` mục 3, 4, 5, 6.
- `workflow/docs/product/dac-ta-chuc-nang.md` dòng STT 53, 54 (và STT 45 là Phát triển sau).
- `workflow/docs/product/use-case/uc-c-giao-dich.md` mục UC28 và UC22 (toàn bộ), UC24 BR6.
- `workflow/docs/ui/c-ket-noi-giao-dich/README.md` mục C03 (bảng hành động, "Trạng thái đặc biệt", "Khác với ảnh").
- `workflow/docs/architecture/trang-thai.md` mục 3 (dòng "Giao hàng", "Nhận hàng khi gặp trực tiếp", "Người mua xác nhận đã nhận", "Hết đếm ngược"), mục 4, mục 8, mục 9.
- `workflow/docs/architecture/luong-xu-ly.md` mục 5 (điểm 2, 3, 4, 6; sơ đồ còn ghi `markReceived(dealId, cb)` theo bản cũ, không theo; xem Câu hỏi 5).
- `workflow/docs/architecture/dac-ta-lop/data.md#dealrepository`, `#mediarepository`; `dac-ta-lop/viewmodel.md#dealviewmodel`; `dac-ta-lop/model.md#deal` (`markShipped`, `markReceived`, `hasShippingProof`, `needsPayment`, `receivedImageId`, `AUTO_RELEASE_DAYS`), `#payment`, `#user`; `dac-ta-lop/enums.md#imagepurpose`, `#paymentstatus`; `dac-ta-lop/data-local.md#dealentity`.
- `workflow/docs/data/firestore-schema.md#deals`, `#payments`, `#images`; `security-rules.md` mục 14 (dòng "Giao hàng", "Nhận hàng", "Hết đếm ngược"), mục 16 (dòng "Giải ngân khi người mua nhận"), mục 24 (images).
- `workflow/docs/conventions/giao-dien.md` mục 4 và 7; skill `pubg-ui`.

### Lớp, trường, trạng thái sẽ dùng
- `DealRepository.markShipped(String dealId, String carrier, String trackingCode, String imageId, DataCallback<Void> cb)`; `DealRepository.markReceived(String dealId, String imageId, DataCallback<Void> cb)`. Cả hai nhận mã ảnh đã được `DealViewModel` lưu trước qua `MediaRepository`.
- `DealViewModel.markShipped(String carrier, String trackingCode, Uri photoUri)`; `DealViewModel.markReceived(Uri photoUri)`; thuộc tính `deal`, `payment`, `loading`, `error`, `done` (kiểu `LiveData`, getter công khai theo khung P1). Lớp này "dùng tới" `MediaRepository`.
- `Deal`: `agreedPrice`, `deliveryMethod`, `carrierName`, `trackingCode`, `trackingImageId`, `shippedAt`, `autoReleaseAt`, `receivedAt`, `receivedImageId`, `status`, `statusHistory`; `markShipped(String carrier, String trackingCode, String imageId)`, `markReceived(String imageId)`, `hasShippingProof()`, `needsPayment()`, `canMoveTo(DealStatus)`. `DealStatus`: `CONFIRMED`, `SHIPPED`, `RECEIVED`, `DISPUTED`. `DeliveryMethod`: `SHIPPING`, `MEET_UP`.
- `Payment`: `status`, `releasedAt`; `isHeld()`, `release()`. `PaymentStatus`: `HELD`, `RELEASED`.
- `User.canSell(boolean)`, `User.canBuy(boolean)`.
- `ImagePurpose`: `WAYBILL`, `RECEIVED`. `MediaRepository.saveImage(Uri uri, ImagePurpose purpose, DataCallback<String> cb)`, `getImage(String id, DataCallback<byte[]> cb)`, `delete(String id, DataCallback<Void> cb)`.
- `NotificationType`: `PAYMENT_UPDATE`, `DEAL_UPDATE`, ghi bằng `NotificationRepository.create(AppNotification notification, WriteBatch batch, DataCallback<Void> cb)` với chính lô đang dựng.
- `DealEntity.autoReleaseAtMillis`, `DealEntity.receivedAtMillis`.
- `FormatUtils.formatTime(Timestamp)`, `NetworkMonitor.getInstance().isOnline()`.
- Phương thức của người khác, chỉ gọi: `MediaRepository.*` và `NotificationRepository.create` (Phát); `UserRepository.getProfile`, `AuthRepository.isEmailVerified` (Ngân).
- Hành động toàn cục (Phát tạo trong `nav_main.xml`): `action_global_create_complaint` (`dealId`), `action_global_my_reports` (nút "Xem khiếu nại", đích D07), `action_global_my_profile` cho nút "Đăng ký xác thực".

### File được tạo hoặc sửa
- [sửa] `PUBGApp/app/src/main/java/com/nhom3/pubgapp/ui/trade/DealDetailFragment.java`
- [sửa] `PUBGApp/app/src/main/res/layout/fragment_trade_deal_detail.xml`
- [sửa] `PUBGApp/app/src/main/res/values/strings_trade.xml`
- [sửa] `PUBGApp/app/src/main/java/com/nhom3/pubgapp/viewmodel/DealViewModel.java` (chỉ thân `markShipped`, `markReceived`)
- [sửa] `PUBGApp/app/src/main/java/com/nhom3/pubgapp/data/DealRepository.java` (chỉ thân `markShipped`, `markReceived`)
- [sửa] `PUBGApp/app/src/test/java/com/nhom3/pubgapp/data/DealRepositoryTest.java`
- [sửa] `PUBGApp/app/src/test/java/com/nhom3/pubgapp/viewmodel/DealViewModelTest.java`

### Các bước đề nghị
Người làm có thể đi theo thứ tự khác, nhưng không được đổi tên lớp, phương thức, trường nào so với tài liệu.

1. Đọc `Deal.java` (`markShipped`, `markReceived`, `hasShippingProof`), `Payment.java` (`release`) do P1-03 tạo và khung `DealRepository.markShipped`, `markReceived`, `DealViewModel.markShipped`, `markReceived`; chép nguyên danh sách file ở trên vào kế hoạch.
2. Viết phương thức `static` cấp gói trong `DealRepository` quyết định điều kiện giao hàng (người bán, SHIPPING, đủ bằng chứng, `Payment` HELD khi giá lớn hơn 0) và điều kiện nhận hàng (người mua, `SHIPPED` hoặc `CONFIRMED` gặp trực tiếp, có ảnh, `Payment` HELD khi giá lớn hơn 0), cùng phương thức tính `autoReleaseAt` từ `shippedAt` (nhận mốc thời gian làm tham số).
3. Điền `DealRepository.markShipped`: kiểm đăng nhập và `NetworkMonitor.getInstance().isOnline()`; đọc giao dịch và khoản thanh toán; gọi phương thức quyết định rồi `Deal.markShipped`; ghi trong một lô: các trường vận đơn, `shippedAt` giờ máy chủ, `autoReleaseAt` (Câu hỏi 1), `status` `SHIPPED`, dòng lịch sử (Câu hỏi 2); thông báo DEAL_UPDATE cho người mua bằng `NotificationRepository.create(notification, batch, cb)` với chính lô đang dựng; thời hạn chờ 15 giây.
4. Điền `DealRepository.markReceived`: như trên cho người mua; ghi trong một lô: `Deal.markReceived(imageId)` (`receivedImageId`), `receivedAt` giờ máy chủ, `status` `RECEIVED`, dòng lịch sử, `Payment.release()` nếu có; thông báo PAYMENT_UPDATE cho người bán khi có `Payment`, cùng lô, bằng `NotificationRepository.create(notification, batch, cb)`.
5. Điền `DealViewModel.markShipped(String carrier, String trackingCode, Uri photoUri)` và `markReceived(Uri photoUri)`: kiểm `User.canSell` hoặc `User.canBuy` (nguồn hồ sơ và cờ email: Câu hỏi 7); `MediaRepository.saveImage` với `ImagePurpose.WAYBILL` hoặc `ImagePurpose.RECEIVED` để lấy `imageId`; gọi repository; lỗi sau khi lưu ảnh thì `MediaRepository.delete`; bật `loading` để khóa lệnh thứ hai khi đang xử lý, đặt `error` khi lỗi, bật `done` khi xong.
6. Viết phần layout C03 còn thiếu trong `fragment_trade_deal_detail.xml`: biểu mẫu giao hàng (hai ô nhập, nút chọn ảnh, ảnh xem trước, nút "Xác nhận"), hộp xác nhận nhận hàng (câu nhắc, nút chọn ảnh, nút "Xác nhận"), khối thông tin vận đơn và ảnh nhận hàng, khối đếm ngược, các nút khiếu nại; token của `giao-dien.md`. Id view đặt `<loại><Việc>` kiểu lowerCamelCase (ví dụ `editTrackingCode`, `buttonConfirmShipped`, `buttonConfirmReceived`, `textCountdown`), khóa chuỗi đặt `<module>_<màn>_<việc>` (ví dụ `trade_deal_detail_confirm_shipped`).
7. Điền `DealDetailFragment`: hiện từng nút theo bảng trạng thái và vai trò; chọn ảnh bằng bộ chọn ảnh của hệ thống qua `ActivityResultContracts` (tra cách dùng bằng context7; chụp ảnh mới phụ thuộc Câu hỏi 4); vẽ đồng hồ bằng `CountDownTimer` hoặc `Handler` dừng ở `onStop`, tính thời gian còn lại bằng phương thức `static` cấp gói nhận `autoReleaseAt` và giờ hiện tại; hiện ảnh bằng `MediaRepository.getImage` ngoài luồng chính.
8. Thêm chuỗi vào `strings_trade.xml`; viết các ca kiểm thử ở `DealRepositoryTest` và `DealViewModelTest`.
9. Chạy các lệnh ở mục "Tự kiểm trước khi mở Pull Request", thử trên hai máy ảo, rồi mở Pull Request.

### File dùng chung cần thêm dòng
Không có. `AndroidManifest.xml` đã khai quyền ảnh ở P1; chụp ảnh bằng camera cần `FileProvider` (Câu hỏi 4) do Phát thêm.

### Cấm đụng
- Leader chỉ sửa: `AndroidManifest.xml`, `nav_main.xml`, `nav_admin.xml`, `themes.xml`, `colors.xml`, `dimens.xml`, `strings_common.xml`, `AppDatabase`, `Converters`, mọi file Gradle, thư mục `firebase/` (kể cả `firestore.rules`).
- Lớp `model` và `model.enums` (kể cả hằng `Deal.AUTO_RELEASE_DAYS`): không sửa.
- Của Phát: `DealRepository.releaseDueDeals`, `completeDueDeals`, `listAll`, `expirePendingDeals`; `DealViewModel.checkAutoRelease`; `AutoReleaseWorker`; `MediaRepository`; `NotificationRepository.create`.
- Của Phúc: `ComplaintRepository` và màn D05, D07 (chỉ gọi hành động toàn cục).
- `completeDeal` thuộc P2-C7; `pay` thuộc P2-C5a; chưa đụng.

### Phụ thuộc
P2-C5a (khoản thanh toán `HELD` do `pay` tạo; điều kiện để giao hàng và nhận hàng khi giá lớn hơn 0) và P2-C4 (C03). Trước khi P2-C5a xong, tạo `payments/{dealId}` HELD tay trên Firebase Console để thử. Tài khoản thử phải có `verificationStatus` APPROVED.

### Ngoài phạm vi
- Khiếu nại trong thời gian đếm ngược (module D: D05, D07, `ComplaintRepository`); issue này chỉ có nút dẫn tới.
- Tự động chuyển `SHIPPED` sang `RECEIVED` và giải ngân khi hết hạn, `AutoReleaseWorker` (Phát, P2-E6).
- Viết đánh giá và hoàn tất giao dịch (P2-C7).
- Giao nhận bằng video, nhiều ảnh vận đơn hay nhiều ảnh nhận hàng: không có (một ảnh vận đơn, một ảnh nhận hàng).
- Dịch vụ thanh toán thật, chuyển tiền ra khỏi hệ thống: không có (khoản thanh toán chỉ là bản ghi mô phỏng).

### Tài liệu cần cập nhật
Không có, trừ khi phải đổi hành vi. Câu hỏi 1 có thể buộc leader sửa `security-rules.md` mục 14 hoặc `model.md`; Câu hỏi 5 buộc sửa `luong-xu-ly.md` mục 5.

### Tự kiểm trước khi mở Pull Request
- [ ] `cd PUBGApp && ./gradlew assembleDebug testDebugUnitTest lintDebug` kết thúc `BUILD SUCCESSFUL`.
- [ ] `sh workflow/scripts/check-java-style.sh` không báo lỗi trên các dòng đã thêm.
- [ ] Giao dịch gửi vận chuyển CONFIRMED, đã thanh toán: người bán đã duyệt xác thực nhập đơn vị, mã, chọn ảnh, bấm "Xác nhận đã giao": Firebase Console có `deals` SHIPPED với `shippedAt`, `autoReleaseAt` cách 7 ngày, `trackingImageId` trỏ tới `images` có `purpose` WAYBILL, `statusHistory` dài thêm một dòng.
- [ ] Bỏ trống mã vận đơn hoặc chưa chọn ảnh: nút "Xác nhận" chưa bấm được.
- [ ] Giao dịch CONFIRMED giá lớn hơn 0 chưa thanh toán: người bán thấy "Chờ người mua thanh toán", không có biểu mẫu.
- [ ] Hai bên cùng thấy "Còn N ngày để xác nhận". Bật chế độ máy bay: đồng hồ vẫn chạy, nút ghi bị khóa. Xoay màn hình: số không nhảy.
- [ ] Người mua bấm "Xác nhận đã nhận hàng": chưa chọn ảnh thì nút "Xác nhận" của hộp chưa bấm được; chọn ảnh rồi xác nhận: `deals` RECEIVED có `receivedImageId` (ảnh `purpose` RECEIVED), `payments` RELEASED, người bán có thông báo PAYMENT_UPDATE.
- [ ] Giao dịch gặp trực tiếp CONFIRMED (đã thanh toán): người mua xác nhận kèm ảnh được ngay; người bán thấy "Chờ người mua".
- [ ] Giao dịch giá 0: xác nhận nhận hàng chạy không cần `payments`, người bán không có thông báo PAYMENT_UPDATE.
- [ ] Tài khoản chưa duyệt xác thực: "Xác nhận đã giao" và "Xác nhận đã nhận hàng" bị chặn kèm lời giải thích, "Khiếu nại giao dịch" vẫn bấm được và mở D05.
- [ ] Xoay màn hình khi đang nhập mã vận đơn và khi đã chọn ảnh: chữ và ảnh còn nguyên, không gửi lệnh hai lần.
- [ ] Liệt kê trong Pull Request mọi lớp, phương thức, trường công khai mới kèm dòng tài liệu định nghĩa (R16).

### Quy mô
- [x] Đây là một lát cắt dọc của một module, khoảng nửa ngày đến một ngày, một Pull Request.

### Câu hỏi cho leader
1. **Hạn đếm ngược không thể bằng đúng `request.time` cộng 7 ngày.** `trang-thai.md` mục 3 và `security-rules.md` mục 14 (dòng "Giao hàng") đòi `autoReleaseAt` bằng đúng giờ máy chủ cộng 7 ngày, trong khi ứng dụng chỉ ghi được `shippedAt` bằng `FieldValue.serverTimestamp()`; không có cách ghi "giờ máy chủ cộng 7 ngày" từ máy khách nên hai giá trị sẽ không bao giờ khớp đúng. Thêm nữa `Deal.AUTO_RELEASE_DAYS` là `private static final`, repository không đọc được. Cần chốt: Rules cho phép sai số quanh `request.time` cộng 7 ngày (ví dụ 5 phút), cách repository lấy hằng 7 (công khai hay lặp lại), và hôm nay phải ghi `autoReleaseAt` thế nào. Trước khi có trả lời, người làm ghi `autoReleaseAt` bằng giờ máy cộng 7 ngày và `shippedAt` bằng giờ máy chủ; khi P3-R áp Rules thật thì phải sửa lại.
2. **Ai thêm dòng `StatusChange`.** `model.md` không nói `Deal.markShipped`, `Deal.markReceived` (và các phương thức chuyển trạng thái khác) có tự thêm một `StatusChange` vào `statusHistory` hay repository thêm; riêng `markShipped` còn không nhận mã người đổi. Xem Câu hỏi 4 của P2-C4; chặn `markShipped` và `markReceived` cho tới khi có một câu trong `model.md`.
3. **Thông báo cho người bán khi giá 0.** `trang-thai.md` mục 9 chỉ ghi PAYMENT_UPDATE cho người bán khi `markReceived`; giao dịch giá 0 không có `Payment` nên người bán không nhận gì khi người mua xác nhận đã nhận, dù giao dịch đã sang `RECEIVED`. Người làm theo bảng (không ghi thông báo); xác nhận đây là ý định, hay cần DEAL_UPDATE cho người bán.
4. **Chụp ảnh bằng camera.** UC22 và UC28 yêu cầu "chụp hoặc chọn" ảnh, và `barem-diem.md` ghi implicit Intent gồm cả chụp ảnh vận đơn. Chụp đủ 1024 px cần `FileProvider` khai trong `AndroidManifest.xml` và một tệp trong `res/xml`, đều là file chỉ leader sửa và chưa có trong tài liệu. Đề xuất: Phát khai sẵn; chưa có thì module C chỉ làm "chọn từ thư viện" ở đợt này (vẫn là implicit Intent).
5. **`luong-xu-ly.md` mục 5 còn chữ ký cũ.** Sơ đồ ở mục 5 vẫn vẽ `markReceived(dealId, cb)` và `DealViewModel.markReceived()` không ảnh, và gọi khoản thanh toán là ký quỹ; `data.md#dealrepository` và `viewmodel.md#dealviewmodel` đã đúng (`markReceived(String dealId, String imageId, DataCallback<Void> cb)`, `markReceived(Uri photoUri)`, ViewModel lưu ảnh trước). Issue này theo `data.md` và `viewmodel.md`; cần sửa sơ đồ trong `luong-xu-ly.md` (R7).
6. **Nút khiếu nại của người bán khi gặp trực tiếp.** UC24 và Rules mục 14 (dòng "Mở khiếu nại") cho "một bên" khiếu nại khi đã xác nhận gặp trực tiếp, nhưng bảng "Hành động" của C03 chỉ cho người mua nút "Khiếu nại giao dịch" ở dòng "Đã xác nhận, gặp trực tiếp" (người bán chỉ "Chờ người mua"). Issue này theo bảng; cần chốt có thêm nút cho người bán hay không.
7. **Chỗ lấy hồ sơ và cờ email cho `canSell` và `canBuy`, và cách báo cho giao diện.** Giống Câu hỏi 3 của P2-C3a: `SessionManager` không trả `User`, `DealViewModel` không có `AuthRepository` trong "Dùng tới" và không có LiveData giữ hồ sơ của chính mình, nên Fragment chưa biết lúc nào hiện lời giải thích kèm nút `action_global_my_profile`. Người bán dùng `User.canSell`, người mua dùng `User.canBuy`; hai hàm cùng điều kiện.
