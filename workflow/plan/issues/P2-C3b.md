TITLE: feat(deal): người bán chấp nhận hoặc từ chối đề nghị
LABELS: type:feature, area:deal, module:c, priority:p1
ASSIGNEES: Contest451
PHASE: P2

Plan item: P2-C3b

### Bối cảnh
1. PUBGApp (Pre-owned Users' Bargain Grounds) là ứng dụng Android mua bán, trao đổi và cho tặng đồ đã qua sử dụng: Java 17, XML Views với ViewBinding, Firebase Authentication và Cloud Firestore gói Spark (không Cloud Storage), Room làm bộ nhớ đệm, minSdk 30.
2. Module C (Chat và giao dịch) do Nguyễn Lê Hải Long (`Contest451`) sở hữu: gói giao diện `com.nhom3.pubgapp.ui.trade`, tiền tố layout `trade_`, chuỗi trong `strings_trade.xml`, đồ thị điều hướng `nav_trade.xml`.
3. Issue này làm phía người bán của đề nghị giá (UC27): hai nút "Từ chối" và "Chấp nhận" trên thẻ đề nghị ở màn C02 `ChatFragment`, `ChatRepository.respondToOffer` (lô ghi tạo giao dịch) và `DealRepository.buildFromOffer`.
4. Đọc trước: `workflow/docs/product/use-case/uc-c-giao-dich.md` mục UC27; `workflow/docs/architecture/trang-thai.md` mục 1, 2 và 3; `workflow/docs/architecture/dac-ta-lop/data.md#chatrepository` và `#dealrepository`.
5. Trên `main` đã có: khung P1 (mọi lớp đủ chữ ký, `Offer.accept`, `Offer.reject`, `Deal` và `Listing.markSold` đã làm kèm test ở P1-03), và từ P2-C1 đến P2-C3a: `ChatFragment`, `MessageAdapter` với thẻ đề nghị (`item_trade_message_offer.xml`), `ChatRepository.sendOffer`, `ChatViewModel.makeOffer`.

### Mã tính năng
F-09

### Mã việc kế hoạch
P2-C3b (phần chấp nhận và từ chối của P2-C3)

### Module
C. Chat và giao dịch (Hải Long)

### STT trong bảng đặc tả
48, 49 (và STT 50 ở phần tạo giao dịch từ đề nghị)

### Màn hình mockup
C02 (Trò chuyện và đề nghị giá): hai nút "Từ chối" và "Chấp nhận" trên thẻ đề nghị của người bán. Sau khi chấp nhận, người bán được đưa sang C03.

### Làm gì và vì sao
Người bán thấy hai nút trên thẻ đề nghị đang chờ. Bấm "Từ chối" thì đề nghị sang REJECTED và người mua nhận thông báo. Bấm "Chấp nhận" thì một lô ghi duy nhất làm năm việc: đề nghị sang ACCEPTED, mọi đề nghị PENDING khác của cùng tin sang REJECTED, tạo giao dịch ở PENDING_CONFIRM (người bán đã xác nhận sẵn, giá thống nhất bằng số tiền đề nghị), tin sang RESERVED kèm `reservedDealId`, và ghi thông báo OFFER_RESULT cho các người mua. Người bán được đưa sang C03 để cùng người mua xem điều khoản và xác nhận (P2-C4). Chỉ tài khoản đã được admin duyệt xác thực mới trả lời được (chấp nhận là tạo giao dịch, Security Rules kiểm `verified()` khi tạo `deals`); tài khoản chưa duyệt thấy hai nút bị chặn kèm lời giải thích. Việc này là điểm nối giữa chat và giao dịch nên mọi chuyển trạng thái và điều kiện được liệt kê dưới đây. Luồng: UC27; `luong-xu-ly.md` mục 4. Kế hoạch ghi P2-C3 là một việc, được tách thành P2-C3a (gửi đề nghị) và P2-C3b (issue này) vì lô ghi chấp nhận có rủi ro cao nhất của module C. Issue này phục vụ các dòng barem R-Chapter 3 (unit test JUnit: `DealRepositoryTest` cho `buildFromOffer` và `ChatRepositoryTest` cho các quyết định trả lời đề nghị, phần quy tắc quan trọng nhất của module), R-Chapter 4 (RecyclerView: hai nút "Từ chối" và "Chấp nhận" nằm trên kiểu item thẻ đề nghị của `MessageAdapter`; điều hướng sang C03 bằng `action_global_deal_detail`) và R-Chapter 1, 2, 6, 7 (bất đồng bộ qua `DataCallback`, kiểm tra kết nối mạng bằng `NetworkMonitor`).

### Tiêu chí nghiệm thu
Các chuyển trạng thái của issue này (theo `trang-thai.md` mục 1, 2 và 3), mỗi dòng có điều kiện kiểm trước khi ghi:

| Từ | Sự kiện | Đến | Điều kiện kiểm | Phương thức |
| --- | --- | --- | --- | --- |
| Offer `PENDING` | Người bán chấp nhận | `ACCEPTED` | Người đang đăng nhập là `offer.sellerId`; `User.canSell(emailVerified)` đúng (kiểm phía ứng dụng); offer đang `PENDING`; tin đang `AVAILABLE` (đọc ngay trước lô) | `Offer.accept`, `ChatRepository.respondToOffer(offerId, true, cb)` |
| Offer `PENDING` | Người bán từ chối | `REJECTED` | Người đang đăng nhập là `offer.sellerId`; `User.canSell(emailVerified)` đúng; offer đang `PENDING` | `Offer.reject`, `ChatRepository.respondToOffer(offerId, false, cb)` |
| Offer khác `PENDING` của cùng tin | Một offer khác vừa được chấp nhận | `REJECTED` | Cùng lô với việc chấp nhận; danh sách lấy bằng một truy vấn `listingId`, `status` PENDING chạy trước lô, bỏ chính offer đang chấp nhận | `Offer.reject`, `ChatRepository.respondToOffer` |
| Offer `PENDING` | Chấp nhận khi tin không còn `AVAILABLE` | `REJECTED` | Tin đã `RESERVED`, `SOLD`, `HIDDEN` hoặc `REMOVED` lúc bấm: offer bị từ chối và báo lỗi, không tạo giao dịch | `ChatRepository.respondToOffer` |
| Tin `AVAILABLE` | Chấp nhận offer | `RESERVED` | Cùng lô với offer `ACCEPTED` và `Deal`; chỉ ghi `status`, `reservedDealId` (bằng mã giao dịch mới), `updatedAt` | `ChatRepository.respondToOffer` |
| (chưa có) | Chấp nhận offer | `Deal` ở `PENDING_CONFIRM` | Cùng lô; dựng bằng `DealRepository.buildFromOffer`: ITEM thành EXCHANGE, MONEY và MONEY_AND_ITEM thành SELL, `agreedPrice` bằng `offer.amount`, `sellerConfirmed` true, `buyerConfirmed` false, chụp `itemTitle` và `itemThumb`, `statusHistory` có đúng một dòng (`fromStatus` rỗng, `toStatus` PENDING_CONFIRM, `changedBy` là người bán) | `DealRepository.buildFromOffer`, `ChatRepository.respondToOffer` |

- [ ] Chấp nhận chạy với Firebase thật: sau một lần bấm "Chấp nhận", trong Firebase Console thấy cùng lúc `offers/{id}` ACCEPTED, mọi offer PENDING khác của tin REJECTED, `deals/{dealId}` mới ở PENDING_CONFIRM với `offerId`, `buyerId`, `sellerId`, `listingId` đúng, `listings/{listingId}` ở RESERVED với `reservedDealId` bằng `dealId`. `onSuccess` trả `dealId` cho `ChatViewModel`; Fragment lấy `dealId` từ `Listing.reservedDealId` sau khi `done` bật và `loadListing()` xong (Câu hỏi 4), rồi chuyển C02 của người bán sang C03 bằng `action_global_deal_detail` (`dealId`).
- [ ] Lô ghi hoặc thành công hết hoặc không đổi gì (kiểm bằng cách ngắt mạng giữa chừng hoặc dùng offer không hợp lệ: không có trạng thái nửa chừng như offer ACCEPTED mà tin vẫn AVAILABLE).
- [ ] Từ chối chạy: `offers/{id}` sang REJECTED, tin và giao dịch không đổi, người mua nhận thông báo OFFER_RESULT với `targetId` là mã cuộc trò chuyện; thẻ đổi sang "Đã từ chối" và người bán vẫn nhắn tin được.
- [ ] Thông báo OFFER_RESULT: người mua được chấp nhận nhận `targetId` là `dealId`; mỗi người mua bị từ chối (trực tiếp hoặc do offer khác được chấp nhận) nhận `targetId` là mã cuộc trò chuyện của offer của họ (`trang-thai.md` mục 9). Thông báo do `ChatRepository` ghi trong chính lô chấp nhận hoặc từ chối, bằng `NotificationRepository.create(AppNotification notification, WriteBatch batch, DataCallback<Void> cb)`, không phải ViewModel; người mua đã tắt loại OFFER_RESULT trong `notificationPrefs` thì hàm này bỏ qua, không ghi.
- [ ] Tin không còn `AVAILABLE` lúc bấm "Chấp nhận" (đang giữ chỗ do giao dịch khác, đã bán, ẩn, đã gỡ): offer sang REJECTED, thông báo OFFER_RESULT cho người mua, không tạo giao dịch, không đổi tin, màn người bán hiện lỗi tiếng Việt rõ ràng (ví dụ "Tin không còn để chấp nhận").
- [ ] Offer đã được trả lời hoặc người bấm không phải người bán: từ chối bằng thông báo tiếng Việt, không ghi gì.
- [ ] Tài khoản người bán chưa được duyệt xác thực: hai nút "Từ chối" và "Chấp nhận" bị chặn, màn hiện lời giải thích cần được admin duyệt xác thực trước khi mua bán kèm nút "Đăng ký xác thực" gọi `action_global_my_profile` (cách Fragment biết để hiện: Câu hỏi 5); không có tài liệu nào được ghi.
- [ ] Hai nút chỉ hiện cho người bán khi thẻ ở PENDING. Người mua không bao giờ thấy nút. Thẻ ACCEPTED và REJECTED không còn nút. Bấm nút thì cả hai nút bị khóa cho tới khi có kết quả (chống bấm đúp).
- [ ] Kiểm lô chấp nhận với từ hai offer PENDING trở lên: hai tài khoản người mua gửi hai đề nghị cho cùng một tin, người bán chấp nhận một, offer còn lại tự sang REJECTED. Các điều kiện Security Rules đầy đủ (nhất là giới hạn số lần `get()` của một lô, `security-rules.md` mục "Giới hạn") chỉ kiểm được khi Rules thật được áp ở P3-R; nếu khi đó lô bị từ chối vì vượt giới hạn thì báo Phát, không tự cắt bớt bước.
- [ ] `DealRepository.buildFromOffer` chỉ dựng đối tượng, không ghi. Unit test `DealRepositoryTest`: ITEM cho `tradeType` EXCHANGE; MONEY và MONEY_AND_ITEM cho SELL; `agreedPrice` bằng `offer.amount`; `status` PENDING_CONFIRM; `sellerConfirmed` true, `buyerConfirmed` false; `statusHistory` đúng một phần tử với `toStatus` PENDING_CONFIRM; `offerId`, `buyerId`, `sellerId`, `listingId` sao chép đúng.
- [ ] `ChatRepositoryTest` thêm các ca: chỉ người bán mới được trả lời; offer không PENDING bị từ chối; danh sách offer PENDING khác loại bỏ chính offer đang chấp nhận; tin không AVAILABLE dẫn tới nhánh "từ chối offer rồi báo lỗi". Phần quyết định tách thành phương thức `static` cấp gói không dùng Firebase, không thêm phương thức công khai.
- [ ] Mọi chuyển trạng thái đi qua phương thức có tài liệu: `Offer.accept`, `Offer.reject`; không code nào gán trực tiếp `Offer.status`. `Listing.status` sang RESERVED chỉ ghi trong `respondToOffer` (nơi duy nhất `trang-thai.md` mục 1 cho phép); `model.md#listing` chưa có phương thức nào cho bước AVAILABLE sang RESERVED (Câu hỏi 2), nên người làm không tự thêm phương thức công khai mới cho `Listing`.
- [ ] Trạng thái màn nằm trong `ChatViewModel`: `loading` khóa hai nút trong lúc `respondToOffer` chạy, `error` mang câu lỗi tiếng Việt, `done` bật một lần khi xong (Fragment đặt lại sau khi dùng); xoay màn hình khi đang chờ kết quả không gửi lệnh lần hai, kết quả không bị mất.
- [ ] Mất mạng: hai nút bị khóa; thời hạn chờ 15 giây, quá hạn thì báo lỗi.
- [ ] `./gradlew assembleDebug testDebugUnitTest lintDebug` và `sh workflow/scripts/check-java-style.sh` đều qua.
- [ ] Pull Request liệt kê mọi lớp, phương thức, trường công khai mới kèm dòng tài liệu định nghĩa nó (R16).

### Tài liệu phải đọc
- `workflow/plan/so-huu.md` mục 3, 4 (`action_global_deal_detail`), 5, 6.
- `workflow/docs/product/dac-ta-chuc-nang.md` dòng STT 47 đến 50.
- `workflow/docs/product/use-case/uc-c-giao-dich.md` mục UC27 (toàn bộ).
- `workflow/docs/ui/c-ket-noi-giao-dich/README.md` mục C02 (đoạn "Hành động và điều hướng" cho Chấp nhận, Từ chối; "Trạng thái đặc biệt").
- `workflow/docs/architecture/trang-thai.md` mục 1 (dòng "AVAILABLE sang RESERVED" và đoạn "Mỗi lần tin rời AVAILABLE"), mục 2 (toàn bộ), mục 3 (dòng đầu tiên của bảng), mục 8 (xác thực), mục 9 (OFFER_RESULT).
- `workflow/docs/architecture/luong-xu-ly.md` mục 4.
- `workflow/docs/architecture/dac-ta-lop/data.md#chatrepository` (`respondToOffer`), `#dealrepository` (`buildFromOffer`); `dac-ta-lop/model.md#offer`, `#deal`, `#listing` (`reservedDealId`), `#statuschange`, `#user` (`canSell`); `dac-ta-lop/viewmodel.md#chatviewmodel`.
- `workflow/docs/data/firestore-schema.md#offers`, `#deals`, `#statushistory`, `#listings`; `security-rules.md` mục 13 (các dòng Chấp nhận, Từ chối), mục 10 (dòng "Giữ chỗ khi chấp nhận offer"), mục 14 (dòng "Tạo") và mục "Giới hạn" (dòng "Số lần đọc trong một lô").
- `workflow/docs/conventions/giao-dien.md` mục 4 và skill `pubg-ui`.

### Lớp, trường, trạng thái sẽ dùng
- `ChatRepository.respondToOffer(String offerId, boolean accept, DataCallback<String> cb)`.
- `ChatViewModel.respondToOffer(String offerId, boolean accept)`; thuộc tính `loading`, `error`, `done` (kiểu `LiveData`, getter công khai theo khung P1); `listing` (`LiveData<Listing>`) và `public void loadListing()` để lấy `Listing.reservedDealId`.
- `DealRepository.buildFromOffer(Offer offer)` trả `Deal` (chữ ký đúng như `data.md#dealrepository`; Câu hỏi 1).
- `Offer`: `listingId`, `conversationId`, `buyerId`, `sellerId`, `offerType`, `amount`, `exchangeItem`, `status`; `accept()`, `reject()`. `OfferStatus`: `PENDING`, `ACCEPTED`, `REJECTED`. `OfferType`: `MONEY`, `ITEM`, `MONEY_AND_ITEM`.
- `Deal`: `listingId`, `offerId`, `buyerId`, `sellerId`, `tradeType`, `agreedPrice`, `exchangeItem`, `itemTitle`, `itemThumb`, `status`, `buyerConfirmed`, `sellerConfirmed`, `statusHistory`. `DealStatus.PENDING_CONFIRM`. `TradeType`: `SELL`, `EXCHANGE`. `StatusChange`: `fromStatus`, `toStatus`, `changedBy`, `reason`, `changedAt`.
- `Listing`: `status`, `reservedDealId`, `coverThumb`, `title`; `isAvailable()`. `ListingStatus.AVAILABLE`, `ListingStatus.RESERVED`.
- `User.canSell(boolean)` (cùng điều kiện với `canBuy`; Câu hỏi 5).
- `NotificationType.OFFER_RESULT`.
- Phương thức của người khác, chỉ gọi: `NotificationRepository.create(AppNotification notification, WriteBatch batch, DataCallback<Void> cb)` (Phát; truyền chính lô ghi đang dựng); đọc `listings/{id}` bằng kết nối `db` hoặc `ListingRepository.getById` (Tú); `UserRepository.getProfile`, `AuthRepository.isEmailVerified` (Ngân).
- Hành động toàn cục (Phát tạo trong `nav_main.xml`): `action_global_deal_detail` (`dealId`); `action_global_my_profile` cho nút "Đăng ký xác thực".

### File được tạo hoặc sửa
- [sửa] `PUBGApp/app/src/main/java/com/nhom3/pubgapp/data/ChatRepository.java` (chỉ thân `respondToOffer`)
- [sửa] `PUBGApp/app/src/main/java/com/nhom3/pubgapp/data/DealRepository.java` (chỉ thân `buildFromOffer`)
- [sửa] `PUBGApp/app/src/main/java/com/nhom3/pubgapp/viewmodel/ChatViewModel.java` (chỉ thân `respondToOffer`)
- [sửa] `PUBGApp/app/src/main/java/com/nhom3/pubgapp/ui/trade/MessageAdapter.java` (thêm hai nút trên thẻ offer của người bán)
- [sửa] `PUBGApp/app/src/main/java/com/nhom3/pubgapp/ui/trade/ChatFragment.java`
- [sửa] `PUBGApp/app/src/main/res/layout/item_trade_message_offer.xml` (do P2-C3a tạo)
- [sửa] `PUBGApp/app/src/main/res/values/strings_trade.xml`
- [sửa] `PUBGApp/app/src/test/java/com/nhom3/pubgapp/data/ChatRepositoryTest.java`
- [tạo] `PUBGApp/app/src/test/java/com/nhom3/pubgapp/data/DealRepositoryTest.java`

### Các bước đề nghị
Người làm có thể đi theo thứ tự khác, nhưng không được đổi tên lớp, phương thức, trường nào so với tài liệu.

1. Đọc `Offer.java`, `Deal.java`, `Listing.java`, `StatusChange.java` do P1-03 tạo và khung của `ChatRepository.respondToOffer(String offerId, boolean accept, DataCallback<String> cb)`, `DealRepository.buildFromOffer(Offer offer)`; chép nguyên danh sách file ở trên vào kế hoạch. Việc gán `itemTitle` và `itemThumb` chờ Câu hỏi 1.
2. Điền `DealRepository.buildFromOffer(Offer offer)`: ITEM thành `TradeType.EXCHANGE`, MONEY và MONEY_AND_ITEM thành `TradeType.SELL`; `agreedPrice` bằng `offer.amount`; `status` `DealStatus.PENDING_CONFIRM`; `sellerConfirmed` true, `buyerConfirmed` false; sao chép `offerId`, `listingId`, `buyerId`, `sellerId`, `exchangeItem`; dòng lịch sử đầu tiên (`StatusChange` với `fromStatus` rỗng, `toStatus` `PENDING_CONFIRM`, `changedBy` là người bán), đúng như mô tả `buildFromOffer` ở `data.md#dealrepository`. Không ghi gì lên mạng.
3. Viết phương thức `static` cấp gói trong `ChatRepository` quyết định việc trả lời: chỉ người bán, offer phải PENDING, tin phải AVAILABLE (nếu không thì nhánh từ chối offer rồi báo lỗi), và phương thức lọc danh sách offer PENDING khác bỏ chính offer đang chấp nhận.
4. Điền `ChatRepository.respondToOffer`: đọc offer và tin; gọi phương thức quyết định; truy vấn các offer PENDING khác (ngoài lô); dựng `Deal` bằng `DealRepository.buildFromOffer`; một lô ghi gồm offer (`Offer.accept` hoặc `Offer.reject`), các offer khác, `deals/{id}`, tin (`status` RESERVED và `reservedDealId`, Câu hỏi 2) khi chấp nhận; ghi thông báo OFFER_RESULT theo bảng ở mục 9 `trang-thai.md` bằng `NotificationRepository.create(notification, batch, cb)` với chính lô đang dựng; thời hạn chờ 15 giây; `cb.onSuccess(dealId)` khi chấp nhận.
5. Điền `ChatViewModel.respondToOffer(String offerId, boolean accept)`: kiểm `User.canSell(emailVerified)` (Câu hỏi 5); đủ điều kiện thì gọi repository ngoài luồng chính; bật `loading` trong lúc gọi, đặt `error` khi lỗi, bật `done` khi xong (Câu hỏi 4 về `dealId`).
6. Thêm hai nút "Từ chối" và "Chấp nhận" vào `item_trade_message_offer.xml` và `MessageAdapter`: chỉ hiện cho người bán khi thẻ PENDING; khóa khi đang xử lý hoặc khi tài khoản chưa xác thực (kèm lời giải thích và nút "Đăng ký xác thực" gọi `action_global_my_profile`).
7. Điền `ChatFragment`: quan sát `done`, `error`, `loading`; chấp nhận thành công thì gọi `loadListing()` và khi `listing` có `reservedDealId` thì `action_global_deal_detail` với `dealId` đó (Câu hỏi 4); lỗi thì hiện câu tiếng Việt.
8. Thêm chuỗi vào `strings_trade.xml` (khóa theo mẫu `<module>_<màn>_<việc>`, ví dụ `trade_chat_offer_accept`); id hai nút đặt `<loại><Việc>` kiểu lowerCamelCase (`buttonOfferAccept`, `buttonOfferReject`).
9. Viết `DealRepositoryTest` (cho `buildFromOffer`) và bổ sung `ChatRepositoryTest`.
10. Chạy các lệnh ở mục "Tự kiểm trước khi mở Pull Request", thử trên hai máy ảo và ít nhất hai người mua, rồi mở Pull Request.

### File dùng chung cần thêm dòng
Không có.

### Cấm đụng
- Leader chỉ sửa: `AndroidManifest.xml`, `nav_main.xml`, `nav_admin.xml`, `themes.xml`, `colors.xml`, `dimens.xml`, `strings_common.xml`, `AppDatabase`, `Converters`, mọi file Gradle, thư mục `firebase/` (kể cả `firestore.rules`).
- Lớp `model` và `model.enums` (`Offer`, `Deal`, `Listing`, `StatusChange`): không sửa, không thêm phương thức.
- `ListingRepository.setStatus`, `setHidden` (Tú) và các phương thức khác làm tin rời AVAILABLE: không đụng; chúng tự từ chối offer PENDING ở lô của chúng.
- `DealRepository.releaseDueDeals`, `completeDueDeals`, `listAll` và `DealViewModel.checkAutoRelease`: của Phát. `DealRepository.expirePendingDeals`: của Phát.
- Phần còn lại của `DealRepository` (`updateTerms`, `confirm`, ...): thuộc P2-C4 trở đi.

### Phụ thuộc
P2-C3a (thẻ đề nghị, cách đọc `Offer`). P1-03 (`Offer.accept`, `Offer.reject`, `Deal` đầy đủ), P1-04. Cần P2-C4 để màn C03 hiện giao dịch sau khi chuyển; trước đó C03 là màn khung trống, kiểm kết quả bằng Firebase Console. Tài khoản người bán và người mua thử phải có `verificationStatus` APPROVED (đặt tay trên Console khi chưa có D01 và E03).

### Ngoài phạm vi
- Sửa điều khoản, xác nhận, hủy giao dịch (P2-C4).
- Từ chối offer khi tin rời AVAILABLE do ẩn, đánh dấu đã bán, admin gỡ, vô hiệu hóa tài khoản: do các phương thức ở module của Tú, Phát, Ngân làm.
- Hủy đề nghị, đề xuất lại: không có trong đặc tả. Người bán ra giá ngược lại thì thương lượng qua chat (UC27 BR4).
- Giao dịch hết hạn xác nhận sau 3 ngày (`expirePendingDeals`, Phát).

### Tài liệu cần cập nhật
Không có, trừ khi phải đổi hành vi. Nếu leader chốt đổi chữ ký `buildFromOffer` (Câu hỏi 1) thì cập nhật `data.md` và `trang-thai.md` mục 3; nếu thêm phương thức cho `Listing` (Câu hỏi 2) thì cập nhật `model.md` và `trang-thai.md` mục 1.

### Tự kiểm trước khi mở Pull Request
- [ ] `cd PUBGApp && ./gradlew assembleDebug testDebugUnitTest lintDebug` kết thúc `BUILD SUCCESSFUL`.
- [ ] `sh workflow/scripts/check-java-style.sh` không báo lỗi trên các dòng đã thêm.
- [ ] Hai người mua gửi hai đề nghị cho cùng một tin; người bán đã duyệt xác thực bấm "Chấp nhận" một đề nghị: Firebase Console có offer đó ACCEPTED, offer kia REJECTED, một tài liệu `deals` PENDING_CONFIRM, tin RESERVED với `reservedDealId` đúng, hai thông báo OFFER_RESULT; C02 chuyển sang C03.
- [ ] Bấm "Từ chối" ở một đề nghị khác: chỉ offer đó sang REJECTED, tin vẫn AVAILABLE, không có `deals` mới, người mua nhận thông báo.
- [ ] Đổi tin sang HIDDEN trên Console rồi bấm "Chấp nhận": offer sang REJECTED, màn hiện lỗi tiếng Việt, không có `deals` mới.
- [ ] Tài khoản người bán chưa duyệt xác thực: hai nút bị chặn kèm lời giải thích, không ghi gì.
- [ ] Người mua mở cùng cuộc trò chuyện: không thấy nút nào trên thẻ.
- [ ] Bật chế độ máy bay: hai nút bị khóa. Xoay màn hình ngay sau khi bấm: không tạo hai giao dịch.
- [ ] Liệt kê trong Pull Request mọi lớp, phương thức, trường công khai mới kèm dòng tài liệu định nghĩa (R16).

### Quy mô
- [x] Đây là một lát cắt dọc của một module, khoảng nửa ngày đến một ngày, một Pull Request.

### Câu hỏi cho leader
1. **`buildFromOffer` thiếu dữ liệu tin.** `data.md#dealrepository` mô tả `buildFromOffer` là chụp tiêu đề và ảnh bìa tin, nhưng chữ ký `DealRepository.buildFromOffer(Offer offer)` trả `Deal` ngay, không có `DataCallback` và không nhận `Listing`; từ một `Offer` không lấy được `itemTitle` và `itemThumb` mà không đọc mạng. Đề xuất: giữ chữ ký, để `respondToOffer` (đã đọc tin để kiểm AVAILABLE) gán `itemTitle` và `itemThumb` vào `Deal` sau khi `buildFromOffer` trả về; hoặc đổi chữ ký thành `buildFromOffer(Offer offer, Listing listing)`. Cần chốt trước khi viết, vì đổi chữ ký là đổi tài liệu (R16, R20).
2. **`Listing` thiếu phương thức cho AVAILABLE sang RESERVED.** `model.md#listing` có `release()` (RESERVED về AVAILABLE) và `markSold()` nhưng không có phương thức nào cho bước chấp nhận offer, trong khi `trang-thai.md` mục 1 ghi bước này do `ChatRepository.respondToOffer` làm và luật R18 cấm ghi thẳng trường `status`. Đề xuất: thêm `Listing.reserve(String dealId)` (đặt RESERVED và `reservedDealId`, chỉ nhận khi AVAILABLE) và kèm unit test ở P1-03. Trong lúc chờ, người làm ghi `status` và `reservedDealId` ở đúng một chỗ trong `respondToOffer`, chú thích một dòng.
3. **Đọc `Offer` cho thẻ.** `respondToOffer` nhận `offerId` và tự đọc `Offer` đầy đủ trong `ChatRepository`, nên không cần phương thức mới; nhưng cách thẻ cập nhật trạng thái cho cả hai bên khi người bán trả lời vẫn phụ thuộc quyết định ở Câu hỏi 1 của P2-C3a.
4. **`dealId` của giao dịch vừa tạo không có chỗ đưa ra màn hình.** `ChatRepository.respondToOffer` trả `dealId` qua `DataCallback<String>`, nhưng `ChatViewModel` chỉ có `done` (kiểu `Boolean`) và không có LiveData giữ `dealId`. Cách làm tạm theo tên có sẵn: sau khi `done` bật, Fragment gọi `loadListing()` rồi đọc `Listing.reservedDealId` từ `listing`. Cần leader xác nhận cách này, hoặc thêm một LiveData vào đặc tả.
5. **`canSell` hay `canBuy` cho người bán trả lời, và nguồn dữ liệu.** `model.md#user` ghi `canBuy` "dùng cho việc gửi đề nghị giá và tạo giao dịch", nhưng người tạo giao dịch (khi chấp nhận) là người bán. Hai hàm cùng điều kiện nên kết quả như nhau; issue này dùng `canSell` cho người bán và `canBuy` cho người mua. Cần leader xác nhận cách gọi, và chốt chỗ lấy hồ sơ cùng cờ email và cách báo cho giao diện như Câu hỏi 3 của P2-C3a.
6. **Hình thức GIVEAWAY.** `buildFromOffer` chỉ sinh `tradeType` SELL hoặc EXCHANGE và Rules mục 14 (dòng "Tạo") cũng chỉ cho hai giá trị đó, nên `TradeType.GIVEAWAY` của `Deal` không có đường sinh nào; bộ lọc "Cho tặng" của C04 sẽ luôn rỗng. Với tin cho tặng (`price` 0), offer MONEY với `amount` 0 cho giao dịch SELL có `agreedPrice` 0. Xác nhận đây là ý định, hay `buildFromOffer` và Rules cần nhận biết tin cho tặng.
