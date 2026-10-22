TITLE: feat(deal): thanh toán mô phỏng cho giao dịch (màn C07)
LABELS: type:feature, area:deal, module:c, priority:p1
ASSIGNEES: Contest451
PHASE: P2

Plan item: P2-C5a

### Bối cảnh
1. PUBGApp (Pre-owned Users' Bargain Grounds) là ứng dụng Android mua bán, trao đổi và cho tặng đồ đã qua sử dụng: Java 17, XML Views với ViewBinding, Firebase Authentication và Cloud Firestore gói Spark (không Cloud Storage), Room làm bộ nhớ đệm, minSdk 30.
2. Module C (Chat và giao dịch) do Nguyễn Lê Hải Long (`Contest451`) sở hữu: gói giao diện `com.nhom3.pubgapp.ui.trade`, tiền tố layout `trade_`, chuỗi trong `strings_trade.xml`, đồ thị điều hướng `nav_trade.xml`.
3. Issue này làm màn mới C07 Thanh toán giao dịch (`DealPaymentFragment`, chưa có ảnh mockup) và nút "Thanh toán" trên C03 (`DealDetailFragment`): thanh toán mô phỏng của người mua (UC21), không ngân hàng, không cổng thanh toán, không tiền thật.
4. Đọc trước: `workflow/docs/ui/c-ket-noi-giao-dich/README.md` mục C07 và mục C03; `workflow/docs/product/use-case/uc-c-giao-dich.md` mục UC21; `workflow/docs/architecture/trang-thai.md` mục 3 (dòng "Người mua thanh toán") và mục 4.
5. Trên `main` đã có: khung P1 (mọi lớp đủ chữ ký, enum `PaymentMethod`, lớp `Payment` có trường `method`, `Deal.needsPayment`), và từ P2-C1 đến P2-C4: `DealDetailFragment` với `DealViewModel.loadDeal`, `DealRepository.getById`, `getPayment`, `confirm`, `cancel`, `DealRepositoryTest`, `DealViewModelTest`.

### Mã tính năng
F-10

### Mã việc kế hoạch
P2-C5a (phần thanh toán của P2-C5, kèm màn C07)

### Module
C. Chat và giao dịch (Hải Long)

### STT trong bảng đặc tả
56

### Màn hình mockup
C07 Thanh toán giao dịch (chưa có ảnh; mô tả ở `workflow/docs/ui/c-ket-noi-giao-dich/README.md` mục C07) và nút "Thanh toán" cùng hộp thông tin thanh toán trên C03.

### Làm gì và vì sao
Khi giao dịch mua bán đã "Đã xác nhận" và giá lớn hơn 0, người mua bấm "Thanh toán" trên C03 để sang C07. C07 làm như một ứng dụng thanh toán thật nhưng chỉ là mô phỏng. Bước 1: tóm tắt giao dịch, số tiền phải trả (bằng giá chốt, không sửa được) và ba phương thức "Thẻ ngân hàng", "Ví điện tử", "Chuyển khoản" chọn một, nút "Tiếp tục" chưa bấm được khi chưa chọn. Bước 2: màn xác nhận có tóm tắt, số tiền, phương thức, lời lưu ý đây là thanh toán mô phỏng và tiền được giữ an toàn tới khi người mua nhận hàng, nút "Xác nhận thanh toán", "Đổi phương thức", "Hủy". Bước 3: biên lai với số tiền, phương thức, thời điểm giữ, trạng thái "Đang giữ", nút "Về chi tiết giao dịch". Khi xác nhận, hệ thống ghi một tài liệu `payments/{dealId}` ở trạng thái HELD kèm `method`, báo cho người bán, giao dịch vẫn ở "Đã xác nhận". Không có số dư ví và không dịch vụ bên ngoài; không dùng các chữ "ký quỹ" hay "ví trung gian" trong chuỗi hiển thị. Chỉ tài khoản đã được admin duyệt xác thực mới thanh toán được. Giao dịch trao đổi hoặc cho tặng (giá 0) không có thanh toán. Luồng: UC21; `luong-xu-ly.md` mục 5 (phần thanh toán). Kế hoạch ghi P2-C5 là một việc, được tách thành P2-C5a (issue này) và P2-C5b (giao hàng, nhận hàng, đếm ngược); màn C07 thêm sau ngày quyết định 2026-10-22 nên nằm ở đây. Issue này phục vụ các dòng barem R-Chapter 4 (input control: nhóm nút chọn một trong ba phương thức thanh toán và nút "Tiếp tục" chỉ bấm được khi đã chọn; điều hướng sang C07 bằng hành động toàn cục `action_global_deal_payment` rồi về C03), R-Chapter 1, 2, 6, 7 (bất đồng bộ qua `DataCallback`, chống bấm đúp bằng `loading`, kiểm tra kết nối mạng bằng `NetworkMonitor`, giữ bước và phương thức đã chọn khi xoay màn hình) và R-Chapter 3 (unit test JUnit cho các điều kiện thanh toán trong `DealRepositoryTest` và `DealViewModelTest`).

### Tiêu chí nghiệm thu
Chuyển trạng thái của issue này (theo `trang-thai.md` mục 3 và 4):

| Từ | Sự kiện | Đến | Điều kiện kiểm trước khi ghi | Phương thức | Hệ quả trong cùng lô |
| --- | --- | --- | --- | --- | --- |
| Giao dịch `CONFIRMED`, chưa có `Payment` | Người mua thanh toán | `Deal` giữ `CONFIRMED`; `Payment` `HELD` | Người gọi là `buyerId`; `User.canBuy(emailVerified)` đúng (phía ứng dụng); `Deal.needsPayment()` đúng; chưa tồn tại `payments/{dealId}`; `amount` bằng `agreedPrice`; phương thức đã chọn | `DealRepository.pay(dealId, method, cb)` | Tạo `payments/{dealId}` (`payerId` là người mua, `payeeId` là người bán, `amount`, `status` `HELD`, `method`, `heldAt` giờ máy chủ, `releasedAt` rỗng); không thêm dòng `statusHistory`; thông báo PAYMENT_UPDATE cho người bán (`targetId` là `dealId`) |

- [ ] Luồng chính chạy với Firebase thật: người mua đã duyệt xác thực mở giao dịch CONFIRMED có giá lớn hơn 0, bấm "Thanh toán", chọn một phương thức, bấm "Tiếp tục", bấm "Xác nhận thanh toán": có `payments/{dealId}` ở HELD, `amount` bằng `agreedPrice`, `method` đúng phương thức đã chọn (`CARD`, `E_WALLET` hoặc `BANK_TRANSFER`), `heldAt` giờ máy chủ; người bán nhận thông báo PAYMENT_UPDATE; `deals/{dealId}` vẫn CONFIRMED và `statusHistory` không đổi; biên lai hiện đúng số tiền, phương thức, thời điểm, "Đang giữ".
- [ ] Bước 1: nút "Tiếp tục" chưa bấm được khi chưa chọn phương thức (UC21 ngoại lệ 2c). Số tiền hiện bằng giá chốt, không có ô sửa (BR2). Nút quay lại về C03, không ghi gì (luồng thay thế 1a).
- [ ] Bước 2: "Đổi phương thức" về bước 1 giữ phương thức đã chọn để đổi (2a); "Hủy" về C03, không ghi gì (2b). Nút "Xác nhận thanh toán" khóa và hiện vòng xoay trong lúc xử lý để không tạo khoản trùng (NFR2).
- [ ] Bước 3 (biên lai): bấm "Về chi tiết giao dịch" về C03, nơi nút "Thanh toán" không còn.
- [ ] Bị từ chối với thông báo tiếng Việt, không ghi gì: người bấm không phải người mua; giao dịch không còn `CONFIRMED` (đã bị hủy lúc đang thanh toán, UC21 ngoại lệ 3a) kèm trở về C03; giá bằng 0; đã có khoản thanh toán (bấm trùng hoặc đã trả ở máy khác, ngoại lệ 3b): báo "giao dịch đã được thanh toán" và hiện khoản hiện có, không tạo thêm; mất mạng hoặc ghi lỗi (3c): báo lỗi, cho thử lại.
- [ ] Trên C03: nút "Thanh toán" chỉ hiện cho người mua khi giao dịch `CONFIRMED`, `needsPayment()` đúng và chưa có `Payment`; không hiện cho người bán, không hiện khi giá bằng 0 hoặc đã thanh toán. Sau khi thanh toán, hộp thông tin thanh toán của C03 hiện thêm trạng thái "Đang giữ" và tên phương thức; sau này cập nhật "Đã giải ngân cho người bán" hoặc "Đã hoàn cho người mua" theo `Payment.status`.
- [ ] Tài khoản chưa được duyệt xác thực: nút "Thanh toán" trên C03 bị chặn kèm lời giải thích và nút "Đăng ký xác thực" gọi `action_global_my_profile` (cách Fragment biết để hiện: Câu hỏi 3); không vào được C07. `DealViewModel.pay` cũng không gọi repository khi `User.canBuy` sai.
- [ ] Nếu C07 bị mở bởi người không phải người mua, hoặc khi giao dịch không đủ điều kiện (ví dụ từ thông báo cũ), màn báo lý do và quay về C03.
- [ ] Chữ hiển thị: "Thẻ ngân hàng", "Ví điện tử", "Chuyển khoản" lấy từ string resource theo `PaymentMethod`; trạng thái khoản thanh toán lấy đúng cột "Nhãn hiển thị" và màu của `giao-dien.md` mục 4: HELD "Đang giữ" (`colorWarning`), RELEASED "Đã giải ngân cho người bán" (`colorSuccess`), REFUNDED "Đã hoàn cho người mua" (`colorOnSurfaceVariant`), vẽ bằng `view_status_chip.xml`; không hiện tên hằng enum cho người dùng; không có chữ "ký quỹ", "ví trung gian", "ngân hàng giải ngân".
- [ ] Mất mạng: nút "Thanh toán" và "Xác nhận thanh toán" bị khóa, có dải ngoại tuyến; thời hạn chờ 15 giây, quá hạn thì báo lỗi tiếng Việt.
- [ ] Xoay màn hình ở C07: bước đang xem và phương thức đã chọn còn nguyên (UC21 NFR3), khoản thanh toán không bị tạo lần hai. Bước và phương thức đã chọn không có tên trong `viewmodel.md`, nên giữ bằng `onSaveInstanceState` của Fragment với khóa `private static final`, không thêm thuộc tính công khai vào ViewModel (R16); ghi một dòng bằng skill `pubg-record-decision`. Việc chống tạo trùng dựa vào `DealViewModel.loading` (khóa nút) và vào mã tài liệu `payments/{dealId}` cố định.
- [ ] Mọi lỗi (Rules từ chối, mất mạng, `NOT_READY`) đi qua `DealViewModel.error` và hiện thông báo tiếng Việt; không nuốt lỗi. C07 có đủ bốn trạng thái: đang tải giao dịch (`loading`), có dữ liệu (`deal`), giao dịch không thanh toán được (rỗng), lỗi kèm "Thử lại" (`error`). `done` bật một lần khi `pay` xong; Fragment đặt lại `done` và xóa `error` sau khi dùng.
- [ ] Khoản thanh toán chỉ tạo bằng `DealRepository.pay`; issue này không đổi trạng thái `Payment` (RELEASED làm ở P2-C5b, REFUNDED ở P2-C4). Không code nào gán trực tiếp `Payment.status` hay `Deal.status` (R18).
- [ ] Unit test `DealRepositoryTest` (phần quyết định tách thành phương thức `static` cấp gói không dùng Firebase, không thêm phương thức công khai): `pay` từ chối từng điều kiện ở trên (không phải người mua, không CONFIRMED, giá 0, đã có `Payment`, thiếu phương thức) và chấp nhận đúng một trường hợp; `amount` bằng `agreedPrice`. `DealViewModelTest`: `pay` bị chặn khi `User.canBuy` sai; điều kiện hiện nút "Thanh toán" theo trạng thái, vai trò, giá và `Payment`.
- [ ] `./gradlew assembleDebug testDebugUnitTest lintDebug` và `sh workflow/scripts/check-java-style.sh` đều qua.
- [ ] Pull Request liệt kê mọi lớp, phương thức, trường công khai mới kèm dòng tài liệu định nghĩa nó (R16); ghi rõ `DealPaymentFragment` chưa có dòng trong `so-huu.md` mục 3 (Câu hỏi 1).

### Tài liệu phải đọc
- `workflow/plan/so-huu.md` mục 3, 4, 5, 6 (lưu ý mục 3 chưa có C07, xem Câu hỏi 1).
- `workflow/docs/product/dac-ta-chuc-nang.md` dòng STT 56.
- `workflow/docs/product/use-case/uc-c-giao-dich.md` mục UC21 (toàn bộ).
- `workflow/docs/ui/c-ket-noi-giao-dich/README.md` mục C07 (toàn bộ) và mục C03 (bảng hành động, "Trạng thái đặc biệt", "Khác với ảnh").
- `workflow/docs/architecture/trang-thai.md` mục 3 (dòng "Người mua thanh toán"), mục 4, mục 8, mục 9 (PAYMENT_UPDATE).
- `workflow/docs/architecture/dac-ta-lop/data.md#dealrepository` (`pay`, `getPayment`); `dac-ta-lop/viewmodel.md#dealviewmodel` (`pay`); `dac-ta-lop/model.md#payment`, `#deal` (`needsPayment`), `#user` (`canBuy`); `dac-ta-lop/enums.md#paymentmethod`, `#paymentstatus`.
- `workflow/docs/data/firestore-schema.md#payments`, `#deals`; `security-rules.md` mục 16 (dòng "Tạo (giữ tiền)").
- `workflow/docs/conventions/giao-dien.md` mục 1 đến 8 và skill `pubg-ui`.

### Lớp, trường, trạng thái sẽ dùng
- `DealRepository.pay(String dealId, PaymentMethod method, DataCallback<Void> cb)`. Tạo `payments/{dealId}` HELD và ghi thông báo PAYMENT_UPDATE trong cùng một lô ghi.
- `DealRepository.getPayment(String dealId, DataCallback<Payment> cb)`, `DealRepository.getById(String dealId, DataCallback<Deal> cb)` (đã làm ở P2-C4).
- `DealViewModel.pay(PaymentMethod method)`; `DealViewModel.loadDeal(String id)`; thuộc tính `deal`, `payment` (`LiveData<Payment>`, null khi giao dịch không cần trả tiền hoặc chưa trả), `loading`, `error`, `done` (kiểu `LiveData`, getter công khai theo khung P1).
- `PaymentMethod`: `CARD`, `E_WALLET`, `BANK_TRANSFER`. `PaymentStatus`: `HELD`, `RELEASED`, `REFUNDED`.
- `Payment`: `dealId`, `payerId`, `payeeId`, `amount`, `status`, `method`, `heldAt`, `releasedAt`; `isHeld()`.
- `Deal`: `agreedPrice`, `status`, `buyerId`, `sellerId`, `itemTitle`, `itemThumb`, `deliveryMethod`; `needsPayment()`, `isParticipant(String userId)`. `DealStatus.CONFIRMED`.
- `User.canBuy(boolean)`.
- `NotificationType.PAYMENT_UPDATE`, ghi bằng `NotificationRepository.create(AppNotification notification, WriteBatch batch, DataCallback<Void> cb)` với chính lô đang dựng.
- `FormatUtils.formatPrice(long)`, `FormatUtils.formatTime(Timestamp)`; `NetworkMonitor.getInstance().isOnline()`.
- Phương thức của người khác, chỉ gọi: `UserRepository.getProfile`, `AuthRepository.isEmailVerified` (Ngân), `NotificationRepository.create` (Phát). Tên người bán ở tóm tắt của C07: Câu hỏi 2.
- Hành động toàn cục (Phát tạo trong `nav_main.xml`): `action_global_deal_payment` (tham số `dealId`; C03 gọi để sang C07, `so-huu.md` mục 4); `action_global_my_profile` cho nút "Đăng ký xác thực". Không thêm hành động nào mới.

### File được tạo hoặc sửa
- [sửa] `PUBGApp/app/src/main/java/com/nhom3/pubgapp/ui/trade/DealPaymentFragment.java` (khung do Phát tạo cùng đích C07 trong `nav_main.xml`; chưa có thì dừng và báo Phát, Câu hỏi 1)
- [tạo] `PUBGApp/app/src/main/res/layout/fragment_trade_deal_payment.xml` (nếu khung có sẵn thì [sửa])
- [sửa] `PUBGApp/app/src/main/java/com/nhom3/pubgapp/ui/trade/DealDetailFragment.java` (nút "Thanh toán", hộp thông tin thanh toán)
- [sửa] `PUBGApp/app/src/main/res/layout/fragment_trade_deal_detail.xml` (nút "Thanh toán", dòng trạng thái thanh toán trong hộp thông tin)
- [sửa] `PUBGApp/app/src/main/res/values/strings_trade.xml`
- [sửa] `PUBGApp/app/src/main/java/com/nhom3/pubgapp/viewmodel/DealViewModel.java` (chỉ thân `pay`)
- [sửa] `PUBGApp/app/src/main/java/com/nhom3/pubgapp/data/DealRepository.java` (chỉ thân `pay`)
- [sửa] `PUBGApp/app/src/test/java/com/nhom3/pubgapp/data/DealRepositoryTest.java`
- [sửa] `PUBGApp/app/src/test/java/com/nhom3/pubgapp/viewmodel/DealViewModelTest.java`

### Các bước đề nghị
Người làm có thể đi theo thứ tự khác, nhưng không được đổi tên lớp, phương thức, trường nào so với tài liệu.

1. Đọc `Payment.java`, `PaymentMethod.java`, `Deal.java` do P1-03 tạo, khung `DealRepository.pay`, `DealViewModel.pay` và xem P1-06 có tạo `DealPaymentFragment` chưa; chép nguyên danh sách file ở trên vào kế hoạch. Chữ ký đúng là `pay(String dealId, PaymentMethod method, DataCallback<Void> cb)` ở repository và `pay(PaymentMethod method)` ở ViewModel; nếu khung P1 trái với tài liệu này thì báo Phát, không tự đổi kiểu.
2. Viết phương thức `static` cấp gói trong `DealRepository` quyết định điều kiện thanh toán: người gọi là `buyerId`, `Deal.status` là `CONFIRMED`, `Deal.needsPayment()`, `Payment` chưa tồn tại, phương thức khác rỗng; trả câu lỗi tiếng Việt hoặc rỗng.
3. Điền `DealRepository.pay(String dealId, PaymentMethod method, DataCallback<Void> cb)`: kiểm đã đăng nhập và `NetworkMonitor.getInstance().isOnline()`; đọc giao dịch và `payments/{dealId}`; gọi phương thức quyết định; ghi `payments/{dealId}` với `dealId`, `payerId`, `payeeId`, `amount` bằng `agreedPrice`, `status` HELD, `method`, `heldAt` bằng `FieldValue.serverTimestamp()`, `releasedAt` rỗng; ghi thông báo PAYMENT_UPDATE cho người bán bằng `NotificationRepository.create(notification, batch, cb)` trong cùng lô với tài liệu `payments/{dealId}`; thời hạn chờ 15 giây.
4. Điền `DealViewModel.pay(PaymentMethod method)`: kiểm `User.canBuy(emailVerified)` (nguồn hồ sơ và cờ email: Câu hỏi 3); bật `loading` để khóa lệnh thứ hai khi đang xử lý, đặt `error` khi lỗi, bật `done` khi xong; gọi repository ngoài luồng chính; thành công thì tải lại giao dịch và khoản thanh toán; lỗi "đã thanh toán" thì tải lại để hiện khoản hiện có.
5. Từ nút "Thanh toán" của C03 gọi `action_global_deal_payment` với tham số `dealId`; hành động và đích C07 do Phát tạo trong `nav_main.xml`, không sửa file đó và không thêm đích vào `nav_trade.xml` (Câu hỏi 1).
6. Viết `fragment_trade_deal_payment.xml` với ba vùng bước 1, 2, 3 (chỉ một vùng hiện tại một lúc), nút theo UC21, vòng xoay, ô lỗi; token của `giao-dien.md`; mọi chữ là string resource trong `strings_trade.xml`. Id view đặt `<loại><Việc>` kiểu lowerCamelCase (ví dụ `buttonContinue`, `buttonConfirmPayment`, `radioMethodCard`), khóa chuỗi đặt `<module>_<màn>_<việc>` (ví dụ `trade_deal_payment_method_card`).
7. Điền `DealPaymentFragment`: ViewBinding; đọc `dealId` từ đối số, gọi `DealViewModel.loadDeal(dealId)`; chọn phương thức bằng nhóm nút chọn một, đổi chữ theo `PaymentMethod` bằng `switch` có `case` và `break` (không dùng `->`); chuyển bước; "Xác nhận thanh toán" gọi `pay`; hiện biên lai khi `Payment` có mặt; quay về C03 theo tiêu chí. Bước hiện tại và phương thức đã chọn giữ bằng `onSaveInstanceState` của Fragment (xem tiêu chí xoay màn hình).
8. Sửa `DealDetailFragment` và layout C03: nút "Thanh toán" theo điều kiện; chặn khi chưa xác thực kèm lời giải thích; hộp thông tin thanh toán hiện thêm trạng thái và phương thức khi có `Payment`.
9. Viết các ca kiểm thử ở `DealRepositoryTest` và `DealViewModelTest`.
10. Chạy các lệnh ở mục "Tự kiểm trước khi mở Pull Request", thử trên hai máy ảo, rồi mở Pull Request.

### File dùng chung cần thêm dòng
Không có. `action_global_deal_payment` và đích C07 đã có trong `nav_main.xml` theo `so-huu.md` mục 4; nếu thiếu thì dừng, mở issue cho Phát, không tự thêm.

### Cấm đụng
- Leader chỉ sửa: `AndroidManifest.xml`, `nav_main.xml`, `nav_admin.xml`, `themes.xml`, `colors.xml`, `dimens.xml`, `strings_common.xml`, `AppDatabase`, `Converters`, mọi file Gradle, thư mục `firebase/`.
- Lớp `model` và `model.enums` (`Payment`, `PaymentMethod`, `Deal`): không sửa.
- Của Phát trong `DealRepository`: `releaseDueDeals`, `completeDueDeals`, `listAll`, `expirePendingDeals`; trong `DealViewModel`: `checkAutoRelease`.
- `DealRepository.markShipped`, `markReceived`, `completeDeal`, `getMyDeals`, `getPayments` và các nút "Xác nhận đã giao", "Xác nhận đã nhận hàng", "Khiếu nại giao dịch", "Viết đánh giá" thuộc P2-C5b, P2-C6, P2-C7; chưa đụng.
- Của Ngân: `UserRepository.getProfile`, `AuthRepository.isEmailVerified`. Của Phúc: màn D01 (nơi đăng ký xác thực). Của Phát: `NotificationRepository.create`.

### Phụ thuộc
P2-C4 (C03, `loadDeal`, `getPayment`, hủy và hoàn tiền). P1-03 (`Payment`, `PaymentMethod`), P1-04. Tài khoản người mua thử phải có `verificationStatus` APPROVED (đặt tay trên Console khi chưa có D01 và E03).

### Ngoài phạm vi
- Giao hàng kèm vận đơn, xác nhận đã nhận, giải ngân, đếm ngược (P2-C5b).
- Danh sách và lịch sử thanh toán (tab "Thanh toán" của C04, P2-C6).
- Hoàn tiền: là phần của `cancel` ở P2-C4 và quyết định khiếu nại của module D.
- Dịch vụ thanh toán thật, số dư ví, nạp tiền, lưu thẻ, nhập số thẻ: không có (thanh toán mô phỏng, phương thức chỉ là nhãn lưu ở `payments.method`).
- Màn đăng ký xác thực (D01, của Phúc).

### Tài liệu cần cập nhật
Không có, trừ khi phải đổi hành vi. Leader thêm C07 vào `so-huu.md` mục 3 theo Câu hỏi 1.

### Tự kiểm trước khi mở Pull Request
- [ ] `cd PUBGApp && ./gradlew assembleDebug testDebugUnitTest lintDebug` kết thúc `BUILD SUCCESSFUL`.
- [ ] `sh workflow/scripts/check-java-style.sh` không báo lỗi trên các dòng đã thêm.
- [ ] Người mua đã duyệt xác thực, giao dịch CONFIRMED giá lớn hơn 0: bấm "Thanh toán", nút "Tiếp tục" mờ cho tới khi chọn phương thức.
- [ ] Chọn "Ví điện tử", "Tiếp tục", "Đổi phương thức" rồi chọn "Chuyển khoản", "Tiếp tục", "Xác nhận thanh toán": Firebase Console có `payments/{dealId}` HELD với `method` BANK_TRANSFER, `amount` đúng, `heldAt` có giờ; biên lai hiện đúng; `deals/{dealId}` vẫn CONFIRMED.
- [ ] Về C03: nút "Thanh toán" biến mất, hộp thông tin hiện "Đang giữ" và phương thức; người bán thấy thông báo PAYMENT_UPDATE.
- [ ] Hủy giao dịch (ở máy kia) lúc người mua đang ở bước 2, rồi bấm "Xác nhận thanh toán": hiện "không thể thanh toán", không có `payments` mới.
- [ ] Bấm "Xác nhận thanh toán" hai lần liền: chỉ một tài liệu `payments`, lần hai báo đã thanh toán hoặc bị khóa.
- [ ] Giao dịch giá 0: C03 không có nút "Thanh toán" và không có hộp thanh toán. Tài khoản chưa duyệt xác thực: nút bị chặn kèm lời giải thích.
- [ ] Bật chế độ máy bay: nút "Thanh toán" bị khóa. Xoay màn hình ở bước 2: vẫn bước 2, phương thức đã chọn còn nguyên.
- [ ] Liệt kê trong Pull Request mọi lớp, phương thức, trường công khai mới kèm dòng tài liệu định nghĩa (R16), nêu rõ phần C07 không có trong `so-huu.md` mục 3.

### Quy mô
- [x] Đây là một lát cắt dọc của một module, khoảng nửa ngày đến một ngày, một Pull Request.

### Câu hỏi cho leader
1. **C07 chưa có trong `so-huu.md` mục 3 và trong việc dựng khung P1-06.** `so-huu.md` mục 4 đã có hành động toàn cục `action_global_deal_payment` và `ui/README.md` đã có mã C07 với lớp `DealPaymentFragment`, nhưng `so-huu.md` mục 3 (nguồn của P1-06 khi dựng khung Fragment) chỉ liệt kê C01 đến C05 cho module C. Cần xác nhận Phát đã tạo khung `DealPaymentFragment`, đích C07 và hành động trong `nav_main.xml`, và thêm một dòng C07 vào `so-huu.md` mục 3. Nếu chưa có thì issue này `blocked` ở phần điều hướng (R20: người làm không tự tạo file của Phát).
2. **Tên người bán ở tóm tắt của C07.** README mục C07 đòi tóm tắt giao dịch có "người bán", nhưng `DealViewModel` không có LiveData giữ hồ sơ đối tác (xem Câu hỏi 2 của P2-C4). Trong lúc chờ, tóm tắt hiện ảnh và tên món (`Deal.itemThumb`, `Deal.itemTitle`) kèm chữ "Người bán", không giả tên.
3. **Chỗ lấy hồ sơ và cờ email cho `User.canBuy`, và cách báo cho giao diện.** Giống Câu hỏi 3 của P2-C3a: `SessionManager` không trả `User`, `DealViewModel` không có `AuthRepository` trong "Dùng tới" và không có LiveData giữ hồ sơ của chính mình, nên Fragment chưa biết lúc nào hiện lời giải thích kèm nút `action_global_my_profile`. Trong lúc chờ, `DealRepository.pay` vẫn từ chối bằng câu tiếng Việt.
