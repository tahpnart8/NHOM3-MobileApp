TITLE: feat(chat): gửi đề nghị giá và hiển thị thẻ đề nghị
LABELS: type:feature, area:chat, module:c, priority:p1
ASSIGNEES: Contest451
PHASE: P2

Plan item: P2-C3a

### Bối cảnh
1. PUBGApp (Pre-owned Users' Bargain Grounds) là ứng dụng Android mua bán, trao đổi và cho tặng đồ đã qua sử dụng: Java 17, XML Views với ViewBinding, Firebase Authentication và Cloud Firestore gói Spark (không Cloud Storage), Room làm bộ nhớ đệm, minSdk 30.
2. Module C (Chat và giao dịch) do Nguyễn Lê Hải Long (`Contest451`) sở hữu: gói giao diện `com.nhom3.pubgapp.ui.trade`, tiền tố layout `trade_`, chuỗi trong `strings_trade.xml`, đồ thị điều hướng `nav_trade.xml`.
3. Issue này làm phía người mua của đề nghị giá (UC20): `ChatRepository.sendOffer`, `ChatViewModel.makeOffer` và thẻ "Đề nghị giá" hiện trong màn C02 `ChatFragment`; chấp nhận và từ chối làm ở P2-C3b.
4. Đọc trước: `workflow/docs/product/use-case/uc-c-giao-dich.md` mục UC20; `workflow/docs/architecture/trang-thai.md` mục 2 (Offer); `workflow/docs/ui/c-ket-noi-giao-dich/README.md` mục C02.
5. Trên `main` đã có: khung P1 (mọi lớp đủ chữ ký, `Listing.acceptsOffer`, `User.canBuy` đã làm kèm test ở P1-03), và từ P2-C1 và P2-C2: `ChatFragment`, `MessageAdapter`, `ChatRepository.getOrCreateConversation`, `sendText`, `sendImage`, `observeMessages`, `ChatViewModel` với `messages`, `partner`, `listing`, `loading`, `error` và `done`.

### Mã tính năng
F-09

### Mã việc kế hoạch
P2-C3a (phần gửi đề nghị của P2-C3)

### Module
C. Chat và giao dịch (Hải Long)

### STT trong bảng đặc tả
47 (gửi đề nghị). Chấp nhận và từ chối (STT 48, 49) làm ở P2-C3b.

### Màn hình mockup
C02 (Trò chuyện và đề nghị giá): thẻ "Đề nghị giá" trong luồng chat. Biểu mẫu nhập đề nghị nằm ở B03 (Chi tiết tin đăng, do Tú làm), xem Câu hỏi 1.

### Làm gì và vì sao
Người mua gửi một đề nghị một chạm cho người đăng tin: tiền, vật trao đổi, hoặc tiền và vật, đúng loại và mức tối thiểu mà người bán chấp nhận. Chỉ tài khoản đã được admin duyệt xác thực mới gửi được (`User.canBuy`, Security Rules kiểm lại bằng `verified()`); tài khoản chưa duyệt thấy lời giải thích kèm lối sang D01 (`action_global_my_profile`) để đăng ký xác thực. Đề nghị xuất hiện trong cuộc trò chuyện dưới dạng thẻ, hiện loại đề nghị, số tiền, món đồ đem đổi và trạng thái. Người gửi chỉ thấy nhãn "Chờ người bán trả lời", không có nút nào, vì đề nghị không hủy được và không có đề xuất lại (quyết định 2026-10-11). Người bán nhận thông báo NEW_OFFER. Một người mua chỉ có một đề nghị đang chờ trên mỗi tin. Luồng: UC20; `luong-xu-ly.md` mục 4 (phần gửi). Kế hoạch ghi P2-C3 là một việc, được tách thành P2-C3a (issue này, ít rủi ro) và P2-C3b (chấp nhận, từ chối, lô ghi tạo giao dịch) để mỗi Pull Request vừa một ngày và lô ghi chấp nhận được xem xét riêng. Issue này phục vụ các dòng barem R-Chapter 4 (RecyclerView: thẻ đề nghị là một kiểu item mới của `MessageAdapter`; điều hướng bằng `action_global_my_profile`), R-Chapter 1, 2, 6, 7 (bất đồng bộ qua `DataCallback`, kiểm tra kết nối mạng bằng `NetworkMonitor`) và R-Chapter 3 (unit test JUnit cho các quy tắc gửi đề nghị trong `ChatRepositoryTest` và `ChatViewModelTest`).

### Tiêu chí nghiệm thu
Chuyển trạng thái của issue này (theo `trang-thai.md` mục 2):

| Từ | Sự kiện | Đến | Điều kiện kiểm trước khi ghi |
| --- | --- | --- | --- |
| (chưa có) | Gửi đề nghị | `OfferStatus.PENDING` | `User.canBuy(emailVerified)` đúng; `Listing.acceptsOffer(offer)` đúng (tin `AVAILABLE`, `offerType` thuộc `acceptedOfferTypes`, `amount` không dưới `minOfferPrice` cho offer có tiền); người gửi không phải người đăng (`buyerId` khác `sellerId`); người gửi chưa có offer `PENDING` nào cho cùng tin |

- [ ] Luồng chính chạy với Firebase thật: tài khoản người mua đã được duyệt xác thực gửi đề nghị MONEY cho một tin AVAILABLE thì một lô ghi tạo `offers/{id}` với `status` PENDING, `buyerId` là người gửi, `sellerId` lấy từ tin, `conversationId` bằng `listingId + "_" + buyerId`, `offerType`, `amount`, `exchangeItem`, `createdAt` giờ máy chủ; tạo `conversations/{conversationId}/messages/{id}` với `type` OFFER, `offerId` là mã offer vừa tạo, `senderId` là người gửi; cập nhật `conversations/{id}.lastMessage` và `lastMessageAt`; ghi thông báo NEW_OFFER cho người bán (`targetId` là mã cuộc trò chuyện). `onSuccess` trả mã offer.
- [ ] Tài khoản chưa được duyệt xác thực (`verificationStatus` NONE, PENDING hoặc REJECTED) hoặc email chưa xác thực: `ChatViewModel.makeOffer` không gọi repository, màn hiện lời giải thích cần được admin duyệt xác thực trước khi mua bán, kèm nút "Đăng ký xác thực" gọi `action_global_my_profile` (sang D01). Cách Fragment biết để hiện lời giải thích và nút này phụ thuộc Câu hỏi 3. Nhắn tin vẫn dùng bình thường.
- [ ] Kiểm điều kiện trước khi ghi, mỗi lỗi một câu tiếng Việt riêng: tin đã bị ẩn hoặc không còn AVAILABLE (đang giữ chỗ, đã bán, đã gỡ), kể cả khi tin đổi trạng thái giữa lúc người mua đang nhập ("đề nghị đã hết hạn", UC20 ngoại lệ 1b); loại đề nghị người bán không nhận; số tiền không phù hợp; offer chỉ có vật mà `amount` khác 0 hoặc `exchangeItem` rỗng (Rules mục 13 cũng đòi); người gửi là chính người đăng; đã có offer PENDING của cùng người cho cùng tin (UC20 ngoại lệ 1c, truy vấn `listingId`, `buyerId`, `status` PENDING trước lô, vì Rules không chặn trùng).
- [ ] Số tiền không phù hợp thì chỉ báo chung "giá đề nghị chưa phù hợp, hãy thử mức khác"; câu báo không in ra mức tối thiểu của người bán (UC20 ngoại lệ 3a và NFR2). Xem Câu hỏi 4 về việc dữ liệu tin vẫn chứa `minOfferPrice`.
- [ ] Nếu cuộc trò chuyện chưa tồn tại (người mua gửi đề nghị ngay từ B03 mà chưa nhắn), `sendOffer` tự gọi `getOrCreateConversation` trước khi ghi lô. Đây là chi tiết triển khai trong cùng lớp, không thêm phương thức công khai.
- [ ] `lastMessage` của tin offer là một chuỗi trung lập lấy từ string resource kèm số tiền định dạng bằng `FormatUtils.formatPrice` (ví dụ "Đề nghị: 3.500.000 đ"), vì dòng này hiện cho cả hai bên. Mockup C01 vẽ "Đề nghị của bạn: ..." chỉ đúng cho người gửi; ghi chỗ khác biệt này vào mô tả Pull Request để leader sửa mục "Khác với ảnh" của README nhóm C.
- [ ] Thẻ đề nghị trong C02 hiện cho cả hai bên: loại (Tiền, Vật trao đổi, Tiền và vật, chữ từ string resource), số tiền (`FormatUtils.formatPrice`; ẩn khi chỉ có vật), món đồ đem đổi (khi có vật), và trạng thái: PENDING, ACCEPTED, REJECTED hiện lần lượt "Chờ người bán trả lời", "Đã chấp nhận", "Đã từ chối". Thẻ do người gửi thấy không có nút nào, chỉ nhãn "Chờ người bán trả lời". Thẻ của người bán ở trạng thái PENDING chưa có nút ở issue này (nút thêm ở P2-C3b).
- [ ] Thẻ cập nhật trạng thái khi người bán trả lời mà không cần mở lại màn (cách đọc `Offer` cho thẻ phụ thuộc Câu hỏi 1). Offer bị từ chối thì người dùng vẫn nhắn tin được; muốn đổi giá thì gửi đề nghị mới sau khi đề nghị cũ đã được trả lời (UC18 ngoại lệ 3f).
- [ ] Không có nút "Hủy đề nghị" và không có "đề xuất lại" ở bất kỳ đâu (mockup vẽ nhưng bỏ theo bảng đặc tả STT 47 và UC20 BR5).
- [ ] Mất mạng: không gửi được (`sendOffer` trả lỗi tiếng Việt nếu bị gọi); thẻ đã xem vẫn hiện ở dạng chỗ giữ chỉ có loại và trạng thái "chưa tải" khi chưa có dữ liệu offer đệm (Room không có bảng offer, xem Câu hỏi 1).
- [ ] `ChatRepository.sendOffer` là đường duy nhất tạo `Offer` PENDING; không có nơi khác tạo offer. Mọi đổi trạng thái offer sau đó đi qua `Offer.accept`, `Offer.reject` ở P2-C3b; không code nào gán trực tiếp `Offer.status` (R18).
- [ ] Trạng thái màn nằm trong `ChatViewModel`: `loading` khóa nút gửi trong lúc `makeOffer` chạy, `error` mang câu lỗi tiếng Việt, `done` bật một lần khi gửi xong (Fragment đặt lại sau khi dùng); xoay màn hình không gửi lại đề nghị, không mất thẻ.
- [ ] Unit test `ChatRepositoryTest`: từng điều kiện ở trên (tin không AVAILABLE, loại không nhận, dưới mức tối thiểu, ITEM mà `amount` khác 0, ITEM mà `exchangeItem` rỗng, người gửi là người đăng, đã có offer PENDING); nội dung `lastMessage` của offer. Thêm `ChatViewModelTest` cho quyết định chặn khi `User.canBuy` sai. Phần quyết định tách thành phương thức `static` cấp gói không dùng Firebase, không thêm phương thức công khai.
- [ ] `./gradlew assembleDebug testDebugUnitTest lintDebug` và `sh workflow/scripts/check-java-style.sh` đều qua.
- [ ] Pull Request liệt kê mọi lớp, phương thức, trường công khai mới kèm dòng tài liệu định nghĩa nó (R16).

### Tài liệu phải đọc
- `workflow/plan/so-huu.md` mục 3, 4, 5, 6.
- `workflow/docs/product/dac-ta-chuc-nang.md` dòng STT 47.
- `workflow/docs/product/use-case/uc-c-giao-dich.md` mục UC20 (toàn bộ) và UC18 ngoại lệ 3f.
- `workflow/docs/ui/c-ket-noi-giao-dich/README.md` mục C02 ("Trạng thái đặc biệt" và "Khác với ảnh"); `workflow/docs/ui/b-kham-pha-tin-dang/README.md` mục B03 (biểu mẫu đề nghị giá).
- `workflow/docs/architecture/trang-thai.md` mục 2 (Offer), mục 8 (xác thực là cửa mở quyền mua bán), mục 9 (NEW_OFFER do `sendOffer` ghi).
- `workflow/docs/architecture/luong-xu-ly.md` mục 4.
- `workflow/docs/architecture/dac-ta-lop/data.md#chatrepository`, `#authrepository` (`isEmailVerified`), `#userrepository` (`getProfile`); `dac-ta-lop/viewmodel.md#chatviewmodel`; `dac-ta-lop/model.md#offer`, `#listing` (`acceptsOffer`, `acceptedOfferTypes`, `minOfferPrice`), `#user` (`canBuy`), `#chatmessage`; `dac-ta-lop/enums.md#offertype`, `#offerstatus`, `#messagetype`.
- `workflow/docs/data/firestore-schema.md#offers`, `#messages`, `#conversations`; `security-rules.md` mục 12 (dòng "Tạo offer") và mục 13 (dòng "Tạo").
- `workflow/docs/conventions/giao-dien.md` mục 4 (màu theo trạng thái) và skill `pubg-ui`.

### Lớp, trường, trạng thái sẽ dùng
- `ChatRepository.sendOffer(Offer offer, DataCallback<String> cb)` (ghi `Offer` PENDING, một `ChatMessage` OFFER, thông báo NEW_OFFER trong cùng lô ghi, qua `NotificationRepository.create(AppNotification notification, WriteBatch batch, DataCallback<Void> cb)`); `ChatRepository.getOrCreateConversation(String listingId, String buyerId, DataCallback<Conversation> cb)`.
- `ChatViewModel.makeOffer(Offer offer)`; thuộc tính `loading`, `error`, `done` (kiểu `LiveData`, getter công khai theo khung P1); `listing` (`LiveData<Listing>`) và `loadListing()` nếu cần đọc tin.
- `Offer`: `listingId`, `conversationId`, `buyerId`, `sellerId`, `offerType`, `amount`, `exchangeItem`, `status`.
- `OfferType`: `MONEY`, `ITEM`, `MONEY_AND_ITEM`. `OfferStatus`: `PENDING` (nhãn "Chờ người bán trả lời"), `ACCEPTED`, `REJECTED`. `MessageType.OFFER`. `NotificationType.NEW_OFFER`.
- `Listing`: `status`, `acceptedOfferTypes`, `minOfferPrice`, `sellerId`; `acceptsOffer(Offer)`, `isAvailable()`. `ListingStatus.AVAILABLE`.
- `User.canBuy(boolean)`; `User.verificationStatus` và `VerificationStatus.APPROVED` (chỉ để hiện lời giải thích).
- `ChatMessage.offerId`, `Conversation.lastMessage`, `Conversation.lastMessageAt`.
- `FormatUtils.formatPrice(long)`; `NetworkMonitor.getInstance().isOnline()`.
- Phương thức của người khác, chỉ gọi: `ListingRepository.getById(String id, DataCallback<Listing> cb)` (Tú) hoặc đọc `listings/{listingId}` bằng kết nối `db` của `BaseRepository`; `UserRepository.getProfile(String uid, DataCallback<User> cb)` (Ngân); `AuthRepository.isEmailVerified()` (Ngân); `SessionManager.getInstance().getCurrentUserId()` (Phát). Nơi lấy hồ sơ và cờ email cho `canBuy` xem Câu hỏi 3.
- Hành động toàn cục (Phát tạo trong `nav_main.xml`): `action_global_my_profile` cho nút "Đăng ký xác thực" (đích D01, không tham số).

### File được tạo hoặc sửa
- [sửa] `PUBGApp/app/src/main/java/com/nhom3/pubgapp/data/ChatRepository.java` (chỉ thân `sendOffer`)
- [sửa] `PUBGApp/app/src/main/java/com/nhom3/pubgapp/viewmodel/ChatViewModel.java` (chỉ thân `makeOffer`)
- [sửa] `PUBGApp/app/src/main/java/com/nhom3/pubgapp/ui/trade/MessageAdapter.java` (do P2-C2 tạo; thêm kiểu hiển thị OFFER)
- [sửa] `PUBGApp/app/src/main/java/com/nhom3/pubgapp/ui/trade/ChatFragment.java`
- [tạo] `PUBGApp/app/src/main/res/layout/item_trade_message_offer.xml`
- [sửa] `PUBGApp/app/src/main/res/values/strings_trade.xml`
- [sửa] `PUBGApp/app/src/test/java/com/nhom3/pubgapp/data/ChatRepositoryTest.java`
- [tạo] `PUBGApp/app/src/test/java/com/nhom3/pubgapp/viewmodel/ChatViewModelTest.java`

### Các bước đề nghị
Người làm có thể đi theo thứ tự khác, nhưng không được đổi tên lớp, phương thức, trường nào so với tài liệu.

1. Đọc `Offer.java`, `Listing.java` (`acceptsOffer`), `User.java` (`canBuy`) do P1-03 tạo và `ChatRepository.sendOffer(Offer offer, DataCallback<String> cb)` ở khung; chép nguyên danh sách file ở trên vào kế hoạch.
2. Viết phương thức `static` cấp gói trong `ChatRepository` quyết định điều kiện gửi (tin AVAILABLE, loại nhận, mức tiền, ITEM có `exchangeItem` và `amount` bằng 0, người gửi khác người đăng) trả về câu lỗi hoặc rỗng; viết thêm phương thức `static` tạo chữ `lastMessage` của offer từ string resource đã truyền vào.
3. Điền `ChatRepository.sendOffer`: kiểm đã đăng nhập và `NetworkMonitor.getInstance().isOnline()`; đọc tin; gọi phương thức quyết định ở bước 2; truy vấn offer PENDING của cùng người cho cùng tin; gọi `getOrCreateConversation` nếu cần; ghi một lô gồm `offers/{id}`, tin nhắn OFFER và cập nhật `lastMessage`, `lastMessageAt`; ghi thông báo NEW_OFFER cho người bán bằng `NotificationRepository.create(notification, batch, cb)` với chính lô đang dựng (hàm bỏ qua nếu người bán đã tắt loại này); thời hạn chờ 15 giây.
4. Điền `ChatViewModel.makeOffer(Offer offer)`: kiểm `User.canBuy(emailVerified)` (nguồn hồ sơ và cờ email: Câu hỏi 3); đủ điều kiện thì gọi `ChatRepository.sendOffer` ngoài luồng chính; bật `loading` trong lúc gọi, đặt `error` khi lỗi, bật `done` khi gửi xong.
5. Viết `item_trade_message_offer.xml`: loại đề nghị, số tiền, món đồ đổi, nhãn trạng thái bằng thành phần chung `view_status_chip.xml` (màu theo `giao-dien.md` mục 4: PENDING `colorWarning`, ACCEPTED `colorSuccess`, REJECTED `colorOnSurfaceVariant`).
6. Thêm kiểu OFFER vào `MessageAdapter` (không lambda) và nối dữ liệu `Offer` cho thẻ theo cách leader chốt ở Câu hỏi 1.
7. Điền `ChatFragment` cho phần đề nghị: hiện lời giải thích và nút "Đăng ký xác thực" (`action_global_my_profile`) khi `makeOffer` bị chặn vì chưa xác thực (theo Câu hỏi 3); hiện lỗi từ `error`.
8. Thêm chuỗi vào `strings_trade.xml` (khóa theo mẫu `<module>_<màn>_<việc>`, ví dụ `trade_chat_offer_status_pending`); id view trong `item_trade_message_offer.xml` đặt `<loại><Việc>` kiểu lowerCamelCase (ví dụ `textOfferAmount`, `chipOfferStatus`).
9. Viết các ca kiểm thử ở `ChatRepositoryTest` và `ChatViewModelTest`.
10. Chạy các lệnh ở mục "Tự kiểm trước khi mở Pull Request", thử trên hai máy ảo, rồi mở Pull Request.

### File dùng chung cần thêm dòng
Không có.

### Cấm đụng
- Leader chỉ sửa: `AndroidManifest.xml`, `nav_main.xml`, `nav_admin.xml`, `themes.xml`, `colors.xml`, `dimens.xml`, `strings_common.xml`, `AppDatabase`, `Converters`, mọi file Gradle, thư mục `firebase/`.
- Lớp `model` và `model.enums`: không sửa, không thêm phương thức cho `Listing`, `Offer`, `User`.
- Biểu mẫu "Đề nghị giá" trên B03: file của Tú (`ListingDetailFragment` và layout tiền tố `listing_`), không sửa ở đây; Tú gọi `ChatViewModel.makeOffer`.
- Phương thức của người khác, chỉ gọi: `ListingRepository.getById` (Tú), `UserRepository.getProfile` và `AuthRepository.isEmailVerified` (Ngân), `NotificationRepository.create`, `FormatUtils`, `NetworkMonitor`, `SessionManager` (Phát).
- `ChatRepository.respondToOffer` và `ChatViewModel.respondToOffer` thuộc P2-C3b; chưa đụng.

### Phụ thuộc
P2-C2 (khung `ChatFragment`, `MessageAdapter`, listener tin nhắn). Cần biểu mẫu đề nghị giá ở B03 của Tú để chạy trọn luồng người mua (Câu hỏi 2); trước đó kiểm `sendOffer` bằng unit test và kiểm thẻ bằng cách tạo tài liệu `offers` và tin nhắn OFFER thủ công trên Firebase Console. Tài khoản thử phải có `verificationStatus` APPROVED (đặt tay trên Console khi chưa có D01 và E03).

### Ngoài phạm vi
- Chấp nhận, từ chối đề nghị và việc tạo giao dịch (P2-C3b).
- Hủy đề nghị, đề xuất lại, thương lượng nhiều vòng: không có trong đặc tả.
- Biểu mẫu nhập đề nghị trên B03 (Câu hỏi 2).
- Từ chối tự động các đề nghị khi tin rời AVAILABLE: các phương thức làm tin rời AVAILABLE (của Tú, Phát, Ngân) làm việc đó.
- Màn đăng ký xác thực (D01, của Phúc) và duyệt xác thực (E03, của Phát).

### Tài liệu cần cập nhật
Không có, trừ khi phải đổi hành vi. Câu trả lời cho Câu hỏi 1, 3, 4 có thể buộc leader sửa `data.md`, `viewmodel.md`, README nhóm C.

### Tự kiểm trước khi mở Pull Request
- [ ] `cd PUBGApp && ./gradlew assembleDebug testDebugUnitTest lintDebug` kết thúc `BUILD SUCCESSFUL`.
- [ ] `sh workflow/scripts/check-java-style.sh` không báo lỗi trên các dòng đã thêm.
- [ ] Tài khoản người mua đã duyệt xác thực gửi đề nghị MONEY: Firebase Console có `offers/{id}` PENDING, tin nhắn OFFER trong `messages`, `conversations/{id}.lastMessage` đổi, và một tài liệu `notifications` NEW_OFFER cho người bán.
- [ ] Gửi lần hai cho cùng tin khi đề nghị đầu còn PENDING: hiện câu "đã có đề nghị đang chờ", không có tài liệu mới.
- [ ] Gửi số tiền dưới mức tối thiểu: hiện câu báo chung, không in ra con số tối thiểu.
- [ ] Đặt tin sang RESERVED trên Console rồi gửi: hiện "tin không còn khả dụng", không ghi gì.
- [ ] Tài khoản chưa duyệt xác thực: không có tài liệu nào được tạo, hiện lời giải thích và nút "Đăng ký xác thực".
- [ ] Bật chế độ máy bay: không gửi được, thẻ đã xem vẫn hiện ở dạng chỗ giữ.
- [ ] Xoay màn hình khi vừa bấm gửi: không tạo hai đề nghị.
- [ ] Liệt kê trong Pull Request mọi lớp, phương thức, trường công khai mới kèm dòng tài liệu định nghĩa (R16).

### Quy mô
- [x] Đây là một lát cắt dọc của một module, khoảng nửa ngày đến một ngày, một Pull Request.

### Câu hỏi cho leader
1. **Đọc `Offer` cho thẻ đề nghị.** `ChatMessage` và `MessageEntity` chỉ mang `offerId`; `ChatRepository` không có phương thức đọc hay lắng nghe `Offer`, `ChatViewModel` không có LiveData cho offer, và Room không có bảng offer. Vì vậy thẻ không có dữ liệu để hiện loại, số tiền, trạng thái, và không cập nhật khi người bán trả lời. Cần thêm vào đặc tả một phương thức đọc hoặc lắng nghe offer (theo mã hoặc theo `conversationId`) và LiveData tương ứng. Người làm không tự đặt tên; phần thẻ đề nghị `blocked` cho tới khi có, phần `sendOffer` làm được ngay. Cần giải quyết trước P2-C3b.
2. **Ai làm biểu mẫu "Đề nghị giá" trên B03.** README nhóm B (B03) mô tả nút "Đề nghị giá" mở bảng trượt gồm loại đề nghị, số tiền, món đồ trao đổi, và README nhóm C ghi "gửi xong sang C02". Nhưng `phase-2-tinh-nang.md` P2-B6 (Tú) không liệt kê biểu mẫu này, P2-C3 (Hải Long) không liệt kê B03, và `so-huu.md` giao màn B03 cho Tú còn STT 47 cho module C. Không ai được giao. Đề xuất: thêm vào P2-B6 một dòng "biểu mẫu đề nghị giá, gọi `ChatViewModel.makeOffer`".
3. **Chỗ lấy hồ sơ và cờ email cho `User.canBuy`, và cách báo cho giao diện.** `canBuy(boolean)` cần hồ sơ người đang đăng nhập và cờ `AuthRepository.isEmailVerified()`. `SessionManager` chỉ có `getCurrentUserId()`, `isAdmin()`, `isLoggedIn()` (không trả `User`), `ChatViewModel` không có `AuthRepository` trong "Dùng tới" và không có LiveData giữ hồ sơ của chính mình, nên Fragment không có cách biết tài khoản chưa xác thực để hiện lời giải thích kèm nút `action_global_my_profile`. Đề xuất: `ChatViewModel` đọc hồ sơ bằng `UserRepository.getProfile(SessionManager.getInstance().getCurrentUserId(), cb)` và cờ bằng `AuthRepository.isEmailVerified()`; khi `canBuy` sai thì đặt `error` bằng câu giải thích, và đặc tả thêm một LiveData hoặc mã lỗi để Fragment phân biệt lỗi này với lỗi mạng. Trong lúc chờ, `ChatRepository.sendOffer` vẫn từ chối bằng câu tiếng Việt và Security Rules (`verified()`) là chốt chặn cuối.
4. **Mức tối thiểu của người bán có thật sự ẩn.** UC20 BR1 và NFR2 nói mức tối thiểu được ẩn với người mua, nhưng `Listing.minOfferPrice` nằm trong tài liệu `listings` ai cũng đọc được, `Listing.acceptsOffer` chạy trên máy người mua, và README B03 ghi ô số tiền "không thấp hơn mức tối thiểu của tin". Muốn ẩn thật thì không được đọc `minOfferPrice` ở phía mua, mà chỉ để Rules kiểm. Issue này làm theo cách an toàn hơn: không in con số ra màn hình hay câu lỗi, vẫn kiểm bằng `acceptsOffer`; cần leader chốt.
