TITLE: feat(listing): đăng tin mới với ảnh, hình thức và offer
LABELS: type:feature, area:listing, module:b, priority:p1
ASSIGNEES: tranannhtu21012006
PHASE: P2

Plan item: P2-B1a

### Bối cảnh
1. PUBGApp (Pre-owned Users' Bargain Grounds) là ứng dụng Android mua bán, trao đổi, cho tặng đồ cũ, viết bằng Java 17, XML Views và ViewBinding, dùng Firebase (Authentication, Firestore gói Spark) và Room.
2. Module B (Tin đăng và khám phá, người làm: Trần Anh Tú) lo đăng tin, kệ hàng, trang chủ, tìm kiếm, chi tiết tin và tin đã lưu.
3. Issue này làm màn B04 `PostListingFragment` ở chế độ đăng tin mới (tab Đăng tin): biểu mẫu, ghi ảnh, ghi tin AVAILABLE và cửa xác thực tài khoản của nút Đăng tin.
4. Đọc trước: `workflow/docs/ui/b-kham-pha-tin-dang/README.md#b04-đăng-tin`, `workflow/docs/product/use-case/uc-b-tin-dang.md#uc10-tạo-tin-đăng` cùng `#uc17-đăng-tin`, `workflow/docs/architecture/luong-xu-ly.md` mục "2. Đăng tin".
5. Đã có trên `main` (P1): khung `PostListingFragment` và `fragment_listing_post.xml`, mọi lớp đủ chữ ký (thân chưa làm trả `DataCallback.NOT_READY`), `MediaRepository` làm thật (`saveImage`, `makeThumb`, `getImage`, `delete`), `CategoryRepository.getAll`, `KeywordFilter`, `SessionManager`, `NetworkMonitor` (có `getInstance()`), `nav_listing.xml`; mọi ViewModel đã có `loading`, `error`, `done` kèm getter công khai, `ShopViewModel` đã có `categories`, `createdListingId`, `loadCategories()`; `nav_main.xml` đã có các hành động toàn cục `action_global_my_profile` và `action_global_sign_in`.

### Mã tính năng
F-04

### Mã việc kế hoạch
P2-B1a. Nửa đầu của P2-B1 (đăng tin và lưu nháp). Nửa sau là P2-B1b (thuộc tính tình trạng theo danh mục và lưu nháp). Tách vì một mình biểu mẫu B04 kèm nén ảnh, kiểm tra, từ khóa cấm, offer và cửa xác thực đã đầy một ngày.

### Module
B. Tin đăng và khám phá (Trần Anh Tú)

### STT trong bảng đặc tả
13 (phần tạo tin), 16 (tên, mô tả, tình trạng chung), 17, 18, 19, 20, 21. Các dòng này đều có Phạm vi "Đợt này". STT 22 (lưu nháp) làm ở P2-B1b. STT 73 chỉ là điều kiện chặn nút Đăng tin ở đây; phần gửi yêu cầu xác thực là của D01 (module D).

### Màn hình mockup
B04 ở chế độ đăng tin mới, mở từ tab Đăng tin (ảnh `workflow/docs/ui/b-kham-pha-tin-dang/b04-dang-tin.png`). Ảnh chưa vẽ: offer mong muốn, thuộc tính tình trạng theo danh mục (P2-B1b), nút Lưu nháp (P2-B1b), dải báo và hộp thoại "tài khoản chưa được xác thực". Chế độ sửa tin là việc của P2-B2b.

### Làm gì và vì sao
Người bán mở tab Đăng tin, chọn từ 1 đến 5 ảnh trong thư viện, nhập tên, chọn danh mục, nhập mô tả, chọn tình trạng, hình thức giao dịch (chọn nhiều), giá, loại offer nhận kèm mức tiền tối thiểu và tỉnh, rồi bấm "Đăng tin". Khi đủ thông tin, ảnh được nén và ghi vào `images`, tin được ghi vào `listings` với trạng thái AVAILABLE, màn hình chuyển sang B03 của tin vừa đăng (luồng "2. Đăng tin" của `luong-xu-ly.md`). Tin chứa từ khóa cấm vẫn đăng được nhưng bị gắn cờ và người bán được báo trước. Đăng tin cần tài khoản đã được admin duyệt xác thực (`User.canSell`, Security Rules kiểm lại bằng `verified()`): tài khoản chưa được duyệt vẫn mở được B04 và nhập biểu mẫu, chỉ bị chặn ở nút Đăng tin, và thấy dải báo cùng hộp thoại nêu lý do (UC17 ngoại lệ 1e). Mất mạng thì nút Đăng tin bị khóa và nội dung đang nhập được giữ nguyên. Issue này phục vụ dòng barem R-Chapter 4 (input control của biểu mẫu B04: ô nhập chữ, ô đánh dấu hình thức giao dịch, thẻ chọn tình trạng, danh sách chọn danh mục và tỉnh; điều hướng bằng Navigation component với hành động toàn cục), dòng R-Chapter 1, 2, 6, 7 (implicit Intent chọn ảnh từ thư viện, và chụp ảnh mới khi câu hỏi 3 được trả lời; async qua `DataCallback`, nén ảnh và ghi Firestore ngoài luồng chính), dòng R-Chapter 3 (unit test JUnit `ShopViewModelTest`) và dòng C-APIs (Cloud Firestore).

### Tiêu chí nghiệm thu
- [ ] Luồng chính chạy trên máy ảo với Firebase thật, bằng tài khoản có `verificationStatus` APPROVED và email đã xác thực: sau khi đăng, `images` có 1 đến 5 tài liệu mới (`purpose` LISTING, `ownerId` là người đăng), `listings` có một tài liệu mới `status` AVAILABLE với `imageIds`, `coverThumb`, `searchKeywords`, `sellerId` là người đang đăng nhập, và `activityLogs` có một dòng `LISTING_CREATED`. PR ghi rõ đã kiểm trên Firebase Console.
- [ ] `sellerId`, `status`, `statusReason`, `flagged`, `searchKeywords` do repository đặt, không lấy từ giá trị màn hình gửi lên (R12).
- [ ] Cửa xác thực: tài khoản `verificationStatus` khác APPROVED mở được B04, thấy dải báo "Tài khoản của bạn chưa được xác thực nên chưa thể đăng tin" ở đầu form. Bấm "Đăng tin" thì hiện hộp thoại nêu lý do, có nút "Đăng ký xác thực" (sang D01 bằng hành động toàn cục `action_global_my_profile`) và nút đóng; nếu `verificationStatus` là PENDING thì hộp thoại nói yêu cầu đang chờ admin duyệt. Không ghi gì lên Firebase, nội dung đã nhập còn nguyên.
- [ ] Thiếu tiêu đề, danh mục, tình trạng, ảnh (ít nhất 1, nhiều nhất 5) hoặc hình thức giao dịch: viền đỏ đúng ô, cuộn tới ô đầu tiên bị thiếu, hiện "Tin đăng cần có tối thiểu tiêu đề, danh mục, 1 hình ảnh và một hình thức giao dịch để có thể công khai"; không gọi mạng, không nén ảnh.
- [ ] Ô giá chỉ hiện khi chọn "Bán". Có "Bán" mà giá bằng 0 hoặc âm: lỗi dưới ô giá "Giá bán phải lớn hơn 0 đối với hình thức Bán". Không chọn "Bán" thì `price` ghi là 0; chọn "Cho tặng" thì `price` là 0, ô giá và phần offer bị ẩn (xem câu hỏi 1).
- [ ] Chọn quá 5 ảnh: báo "Chỉ được tải lên tối đa 5 hình ảnh cho mỗi tin đăng"; đủ 5 ảnh thì ẩn nút thêm ảnh; xóa được từng ảnh đã chọn trước khi đăng.
- [ ] Phần "Offer mong muốn": chỉ hiện các loại hợp với hình thức đã chọn (Tiền khi có "Bán", Vật trao đổi khi có "Trao đổi", Tiền và vật khi có cả hai), ghi vào `acceptedOfferTypes` bằng `OfferType` (`MONEY`, `ITEM`, `MONEY_AND_ITEM`); ô "Mức tiền đề nghị tối thiểu" (0 là không giới hạn) ghi vào `minOfferPrice` và chỉ hiện khi có loại offer có tiền.
- [ ] Tin chứa từ khóa cấm: vẫn đăng, `flagged` là true, người bán thấy thông báo báo trước ("Tin của bạn có từ khóa cần chú ý, admin sẽ xem xét"); không chứa thì không hiện thông báo.
- [ ] Một ảnh ghi lỗi thì dừng, xóa các ảnh đã ghi của lần này bằng `MediaRepository.delete`, báo lỗi, không ghi tin. `ListingRepository.create` lỗi sau khi ảnh đã ghi thì cũng xóa các ảnh đó (`luong-xu-ly.md` mục 2, điểm 5). Lỗi ghi tin hiện "Đăng tin thất bại, vui lòng thử lại" và nội dung form còn nguyên.
- [ ] Offline: nút "Đăng tin" bị khóa kèm dòng "Không có kết nối mạng. Vui lòng thử lại khi có mạng"; chữ và ảnh đang chọn vẫn còn.
- [ ] Đăng xong hiện "Đăng tin thành công!" và chuyển sang B03 bằng `action_global_listing_detail` với `listingId` là mã tin vừa tạo.
- [ ] Tỉnh điền sẵn theo địa chỉ mặc định (nạp bằng `ProfileViewModel.loadAddresses()`), tọa độ `location` lấy từ địa chỉ mặc định. Chưa có địa chỉ mặc định: nhắc thêm địa chỉ ở A06 (`action_global_address_list`), tin vẫn đăng được với `location` rỗng.
- [ ] Phương thức của module khác trả `DataCallback.NOT_READY` thì màn hình hiện đúng lỗi đó, vẫn cho chọn tỉnh tay, không điền giả.
- [ ] Trạng thái màn hình lấy từ ViewModel: `ShopViewModel.loading` bật khi đang nén và ghi (hiện trạng thái đang tải và khóa nút), `ShopViewModel.error` hiện một lần rồi xóa (xoay màn hình không hiện lại), `ShopViewModel.createdListingId` dẫn sang B03, `ShopViewModel.categories` đổ vào ô chọn danh mục (nạp bằng `loadCategories()`).
- [ ] Xoay màn hình giữ nguyên chữ đang nhập, thẻ đã chọn và danh sách ảnh đã chọn (lưu bằng `onSaveInstanceState` của Fragment); xoay trong lúc đang đăng không đăng hai lần.
- [ ] Nén ảnh, ghi Firestore và đọc Room chạy ngoài luồng chính.
- [ ] Mọi quy tắc kiểm biểu mẫu nằm trong phương thức package-private `static` của `ShopViewModel` và có unit test `ShopViewModelTest`: thiếu từng trường bắt buộc, 0 ảnh, 6 ảnh, có "Bán" mà giá 0, không "Bán" thì giá về 0, "Cho tặng" thì giá về 0.
- [ ] `./gradlew assembleDebug testDebugUnitTest lintDebug` và `sh workflow/scripts/check-java-style.sh` đều qua.
- [ ] PR liệt kê từng lớp, phương thức, trường công khai mới kèm dòng tài liệu định nghĩa (R16); tên không có trong tài liệu được ghi riêng.

### Tài liệu phải đọc
- `AGENTS.md` (R12, R15 đến R23), `workflow/plan/so-huu.md` mục 3, 4, 5, 6.
- `workflow/docs/ui/b-kham-pha-tin-dang/README.md` mục B04, kể cả "Trạng thái đặc biệt" và "Khác với ảnh".
- `workflow/docs/product/use-case/uc-b-tin-dang.md` UC10 (luồng thay thế 5a, 5b, 7a và ngoại lệ) và UC17.
- `workflow/docs/architecture/luong-xu-ly.md` mục 2.
- `workflow/docs/architecture/dac-ta-lop/data.md#listingrepository`, `#mediarepository`, `#categoryrepository`, `#userrepository` (dòng `getProfile`).
- `workflow/docs/architecture/dac-ta-lop/model.md#listing`, `#user` (`canSell`), `#bannedkeyword`, `#useraddress`.
- `workflow/docs/architecture/dac-ta-lop/viewmodel.md#shopviewmodel`, `#profileviewmodel`.
- `workflow/docs/architecture/dac-ta-lop/util.md#keywordfilter`, `#sessionmanager`, `#networkmonitor`.
- `workflow/docs/architecture/dac-ta-lop/enums.md#listingstatus`, `#tradetype`, `#itemcondition`, `#offertype`, `#imagepurpose`, `#verificationstatus`.
- `workflow/docs/architecture/trang-thai.md` mục 1 (dòng "Đăng tin") và mục 8.
- `workflow/docs/data/firestore-schema.md#listings`, `#images`, `#bannedkeywords`.
- `workflow/docs/data/security-rules.md` mục 10 (dòng "Tạo và đăng"), mục 23, mục 24, quy ước chung số 10.
- `workflow/docs/conventions/giao-dien.md` (token, bốn trạng thái) và `.agents/skills/pubg-ui/SKILL.md`.
- `workflow/memory/code/firebase.md`.
- `workflow/docs/product/barem-diem.md` (dòng barem mà issue phục vụ).

### Lớp, trường, trạng thái sẽ dùng
Chỉ các tên có trong tài liệu, viết đúng như dưới đây.
- Màn hình: `PostListingFragment`; `ShopViewModel` với `createListing(Listing listing, List<Uri> imageUris)`, `loadCategories()` và các LiveData `keywordFlag` (`LiveData<Boolean>`), `categories` (`LiveData<List<Category>>`), `createdListingId` (`LiveData<String>`), `loading` (`LiveData<Boolean>`), `error` (`LiveData<String>`), `done` (`LiveData<Boolean>`), mỗi LiveData có getter công khai.
- `ListingRepository.create(Listing listing, DataCallback<String> cb)`, `ListingRepository.getBannedKeywords(DataCallback<List<BannedKeyword>> cb)`.
- `MediaRepository.saveImage(Uri uri, ImagePurpose purpose, DataCallback<String> cb)`, `makeThumb(Uri uri, DataCallback<String> cb)`, `delete(String id, DataCallback<Void> cb)` (đã làm thật ở P1-04, chỉ gọi).
- `KeywordFilter.setKeywords(List<BannedKeyword>)`, `containsBanned(String)`; `BannedKeyword.matches(String)`; `FormatUtils.removeAccents(String)`.
- `CategoryRepository.getAll(DataCallback<List<Category>> cb)`; `Category.isRoot()`, trường `parentId`, `name`, `isActive`.
- `ProfileViewModel.profile` (`LiveData<User>`) cùng `loadProfile(String uid)`, và `ProfileViewModel.addresses` (`LiveData<List<UserAddress>>`) cùng `loadAddresses()`: của module D và A, chỉ gọi và quan sát. `SessionManager` không có getter cho `User` hiện tại, nên `verificationStatus` đọc qua `ProfileViewModel.profile` sau `loadProfile(SessionManager.getInstance().getCurrentUserId())`.
- `User.verificationStatus`, `User.canSell(boolean)` (do repository gọi), `VerificationStatus` (`NONE`, `PENDING`, `APPROVED`, `REJECTED`).
- `UserAddress`: `isDefault`, `province`, `location`.
- `Listing`: `categoryId`, `title`, `description`, `imageIds`, `coverThumb`, `itemCondition`, `tradeTypes`, `price`, `acceptedOfferTypes`, `minOfferPrice`, `province`, `location`, `status`, `statusReason`, `flagged`, `searchKeywords`; `Listing.buildSearchKeywords()`.
- Enum: `ListingStatus.AVAILABLE`; `ItemCondition` (`NEW`, `LIKE_NEW`, `USED`, `REPAIRED`, `DEFECTIVE`); `TradeType` (`SELL`, `EXCHANGE`, `GIVEAWAY`); `OfferType`; `ImagePurpose.LISTING`; `ReportTarget.LISTING`.
- `BaseRepository.currentUserId()`, `logActivity(String action, ReportTarget targetType, String targetId)`, `fail(DataCallback, Exception)` (đã có từ P1-04).
- `SessionManager.getInstance()`, `getCurrentUserId()`, `isLoggedIn()`; `NetworkMonitor.getInstance()`, `isOnline()`.
- Hành động toàn cục: `action_global_listing_detail` (tham số `listingId`), `action_global_address_list`, `action_global_my_profile` (nút "Đăng ký xác thực").

Tên nội bộ do issue này đặt (package-private hoặc private, không nằm trong `dac-ta-lop`, ghi vào PR ở mục tên mới): trong `ShopViewModel` các hàm `static List<String> findMissingFields(Listing listing, int imageCount)` và `static long normalizePrice(List<TradeType> tradeTypes, long typedPrice)` cùng các hằng chuỗi `FIELD_TITLE`, `FIELD_CATEGORY`, `FIELD_CONDITION`, `FIELD_IMAGES`, `FIELD_TRADE_TYPE`, `FIELD_PRICE`; lớp giao diện `PostImageAdapter` (xem "File được tạo hoặc sửa").

Chi tiết triển khai, ghi một dòng bằng skill `pubg-record-decision`: loại offer chọn sẵn (Tiền khi chọn "Bán", Vật trao đổi khi chọn "Trao đổi"); danh sách tỉnh là mảng chuỗi tài nguyên riêng trong `strings_listing.xml` và `Listing.province` lưu đúng chuỗi trong mảng (xem câu hỏi 2); dòng nhật ký `LISTING_CREATED` dùng `ReportTarget.LISTING` (chữ ký của `logActivity` đã quy định).

### File được tạo hoặc sửa
- [sửa] `PUBGApp/app/src/main/java/com/nhom3/pubgapp/ui/listing/PostListingFragment.java`
- [tạo] `PUBGApp/app/src/main/java/com/nhom3/pubgapp/ui/listing/PostImageAdapter.java` (danh sách ảnh đã chọn)
- [sửa] `PUBGApp/app/src/main/res/layout/fragment_listing_post.xml`
- [tạo] `PUBGApp/app/src/main/res/layout/item_listing_post_image.xml`
- [sửa] `PUBGApp/app/src/main/res/values/strings_listing.xml`
- [sửa] `PUBGApp/app/src/main/res/navigation/nav_listing.xml` (chỉ khi P1 chưa khai báo lối đi từ B04 sang B03 hoặc sang A06; nếu đã có thì không đụng)
- [sửa] `PUBGApp/app/src/main/java/com/nhom3/pubgapp/viewmodel/ShopViewModel.java` (chỉ thân `createListing` và `loadCategories`, việc đẩy dữ liệu vào các LiveData đã có `keywordFlag`, `categories`, `createdListingId`, `loading`, `error`, `done`, và hai hàm package-private ở trên)
- [sửa] `PUBGApp/app/src/main/java/com/nhom3/pubgapp/data/ListingRepository.java` (chỉ thân `create`, `getBannedKeywords`)
- [tạo] `PUBGApp/app/src/test/java/com/nhom3/pubgapp/viewmodel/ShopViewModelTest.java`

Tên layout theo quy ước `fragment_<tiền tố><màn>.xml`; nếu P1-06 đã đặt tên khác cho khung của B04 thì dùng tên đã có. Adapter là lớp giao diện, không có trong `dac-ta-lop`; tên do issue này đặt.

### Các bước đề nghị
Người làm có thể đổi thứ tự nhưng không được đổi tên nào ở trên.
1. Đọc `workflow/memory/code/firebase.md` và các tài liệu ở mục trên. Tra cách chọn nhiều ảnh trong thư viện (`ActivityResultContracts.PickMultipleVisualMedia`) bằng tài liệu chính thức của AndroidX rồi mới dùng (R2); chụp ảnh bằng camera làm khi câu hỏi 3 được trả lời.
2. Trong `ShopViewModel` viết hai hàm package-private `static List<String> findMissingFields(Listing listing, int imageCount)` (trả các hằng `FIELD_*` còn thiếu theo thứ tự trên form, rỗng khi đủ) và `static long normalizePrice(List<TradeType> tradeTypes, long typedPrice)` (trả 0 khi không có `SELL` hoặc có `GIVEAWAY`, ngược lại trả `typedPrice`). Điều kiện đủ: tiêu đề không rỗng, `categoryId` không rỗng, `itemCondition` đã chọn, `imageCount` từ 1 đến 5, `tradeTypes` không rỗng, và nếu có `SELL` thì giá lớn hơn 0.
3. Viết `ShopViewModelTest` cho hai hàm trên ngay từ đầu (đủ các ca ở tiêu chí nghiệm thu) và chạy `./gradlew testDebugUnitTest`.
4. Làm thân `ListingRepository.getBannedKeywords(DataCallback<List<BannedKeyword>> cb)`: đọc collection `bannedKeywords` (mọi người đã đăng nhập đọc được).
5. Làm thân `ListingRepository.create(Listing listing, DataCallback<String> cb)` theo `data.md` và `luong-xu-ly.md` mục 2: lấy `sellerId` từ `currentUserId()`; kiểm `User.canSell` (đọc hồ sơ người đăng và cờ email đã xác thực, không thêm phương thức công khai), lỗi thì `onError` với "Tài khoản của bạn chưa được xác thực nên chưa thể đăng tin"; đặt `status` AVAILABLE và `statusReason` rỗng; đặt `flagged` true khi một `BannedKeyword.matches(...)` đúng với tiêu đề cộng mô tả đã bỏ dấu và viết thường bằng `FormatUtils.removeAccents`; gọi `Listing.buildSearchKeywords()`; ghi `listings` với `createdAt` và `updatedAt` bằng `FieldValue.serverTimestamp()`; gọi `logActivity("LISTING_CREATED", "LISTING", id)`; trả mã tin qua `onSuccess`.
6. Làm thân `ShopViewModel.createListing(Listing listing, List<Uri> imageUris)`: (a) kiểm bằng `findMissingFields`, thiếu thì dừng và báo, không gọi mạng; (b) gọi `ListingRepository.getBannedKeywords`, nạp `KeywordFilter.setKeywords`, đặt `keywordFlag` bằng `containsBanned` trên tiêu đề cộng mô tả; (c) ghi từng ảnh bằng `MediaRepository.saveImage(uri, ImagePurpose.LISTING, cb)` lần lượt, gom mã ảnh; (d) `MediaRepository.makeThumb` trên ảnh đầu làm `coverThumb`; (e) đặt `imageIds`, `coverThumb`, rồi gọi `ListingRepository.create`; (f) lỗi ở bất kỳ bước nào thì `MediaRepository.delete` các ảnh đã ghi. Bật `loading` khi bắt đầu và tắt khi xong; thành công thì đặt `createdListingId` rồi `done`; lỗi thì đặt `error`. Viết thành các phương thức private nhỏ, dùng lớp ẩn danh `DataCallback`, không dùng lambda.
7. Dựng `fragment_listing_post.xml` bằng `ConstraintLayout` trong một `NestedScrollView`, lấy màu, cỡ chữ, khoảng cách từ token ở `giao-dien.md`: dải báo xác thực, lưới ảnh, tên, danh mục, mô tả, thẻ tình trạng, thẻ hình thức, giá, offer, tỉnh, nút "Đăng tin". Id view theo lowerCamelCase `<loại><Việc>` (ví dụ `buttonPost`, `editTitle`, `recyclerImages`), khóa chuỗi theo `<module>_<màn>_<việc>` (ví dụ `listing_post_button_publish`). Đủ bốn trạng thái: đang tải (khi `loading` bật), có dữ liệu, lỗi (dải lỗi từ `error` kèm nút "Thử lại"), và chỗ báo thiếu. Mọi chữ là string resource trong `strings_listing.xml`.
8. Làm `PostImageAdapter` và `item_listing_post_image.xml`: ô ảnh có nút xóa, ô cuối là nút thêm ảnh (ẩn khi đủ 5).
9. Làm `PostListingFragment`: chọn ảnh, dựng `Listing` từ biểu mẫu (không đặt `sellerId`, `status`), chọn tỉnh, đọc `ProfileViewModel.addresses` để điền sẵn tỉnh và `location`, gọi `ShopViewModel.loadCategories()` rồi quan sát `categories` cho ô danh mục, quan sát `ProfileViewModel.profile` để hiện dải báo khi `verificationStatus` khác `APPROVED`. Bấm "Đăng tin": nếu `NetworkMonitor.getInstance().isOnline()` là false thì báo offline; nếu hồ sơ cho thấy chưa APPROVED thì hiện hộp thoại `AlertDialog` do một phương thức private của Fragment dựng (PENDING thì nói đang chờ), nút "Đăng ký xác thực" gọi `action_global_my_profile`; còn lại gọi `ShopViewModel.createListing`. Giữ chữ, thẻ và ảnh bằng `onSaveInstanceState`.
10. Khi `createdListingId` có giá trị (thành công): Snackbar "Đăng tin thành công!" rồi `action_global_listing_detail` với `listingId`. Khi `error` có giá trị: hiện nguyên câu tiếng Việt do repository trả rồi xóa.
11. Chạy các lệnh ở mục "Tự kiểm trước khi mở Pull Request", thử trên máy ảo với hai tài khoản (đã APPROVED và chưa APPROVED), ghi bẫy gặp phải (nén ảnh, Photo Picker, giới hạn tài liệu Firestore) vào `workflow/memory/code/firebase.md` nếu có.

### File dùng chung cần thêm dòng
Không có, trừ các chuỗi trong `strings_listing.xml` của chính module. Các LiveData `loading`, `error`, `done`, `createdListingId`, `categories` của `ShopViewModel` đã có từ P1, không thêm.

### Cấm đụng
- File chỉ Phát sửa: `AndroidManifest.xml`, `nav_main.xml`, `nav_admin.xml`, menu thanh điều hướng, `themes.xml`, `colors.xml`, `dimens.xml`, `strings_common.xml`, `AppDatabase`, `Converters`, mọi file Gradle, mọi file trong `firebase/` (nếu `create` báo thiếu luật hay chỉ mục thì ghi vào mô tả PR để Phát gộp).
- Lớp `model` và `model.enums` (kể cả `Listing`, `User`): không sửa.
- `MediaRepository`, `KeywordFilter`, `FormatUtils`, `NetworkMonitor`, `SessionManager`, `CategoryRepository`, `BaseRepository` (Phát): chỉ gọi.
- `ProfileViewModel.profile`, `loadProfile` (Phúc) và `ProfileViewModel.addresses`, `loadAddresses` (Ngân): chỉ gọi và quan sát, có thể còn trả `NOT_READY`.
- Màn D01 và A06 (module D, A): chỉ đi tới bằng hành động toàn cục `action_global_my_profile` và `action_global_address_list`.
- Các phương thức khác của `ListingRepository` và `ShopViewModel`: thuộc P2-B1b, P2-B2a, P2-B2b.

### Phụ thuộc
P1-04, P1-05, P1-06. Cần đăng nhập thật để thử: P2-A1. Dùng, không chờ: P2-A4 (địa chỉ mặc định), P2-D1 (`loadProfile`). Chưa xong thì hiện lỗi `NOT_READY`.

### Ngoài phạm vi
- Thuộc tính tình trạng theo danh mục và nút "Lưu nháp": P2-B1b.
- Chế độ sửa tin, ẩn tin, đánh dấu đã bán: P2-B2b.
- Chụp ảnh bằng camera (cần `FileProvider` trong `AndroidManifest.xml`, file của Phát): câu hỏi 3. Trong lúc chờ, issue này chỉ chọn ảnh trong thư viện.
- Chặn Khách khi bấm tab Đăng tin (đưa sang A01 bằng `action_global_sign_in`): việc của `MainActivity` (Phát). Gửi yêu cầu xác thực: D01 (Phúc).
- Gửi video (STT 45) và mọi chức năng "Phát triển sau"; bố cục ngang: P3-Ba.

### Tài liệu cần cập nhật
Không có, trừ khi các câu hỏi dưới đây đổi hành vi (khi đó cập nhật `viewmodel.md`, `luong-xu-ly.md`, `dac-ta-chuc-nang.md` theo R7).

### Tự kiểm trước khi mở Pull Request
- [ ] `cd PUBGApp && ./gradlew testDebugUnitTest` chạy `ShopViewModelTest`: toàn bộ ca qua.
- [ ] Đăng một tin đủ thông tin bằng tài khoản APPROVED: Firebase Console có đúng 1 tài liệu `listings` AVAILABLE, số tài liệu `images` mới bằng số ảnh, 1 dòng `activityLogs` `LISTING_CREATED`; màn hình sang B03.
- [ ] Bằng tài khoản chưa APPROVED: dải báo hiện ở đầu form, bấm "Đăng tin" ra hộp thoại, Console không có tài liệu mới.
- [ ] Bỏ trống từng trường bắt buộc: ô đó viền đỏ và Console không có tài liệu mới.
- [ ] Nhập tiêu đề chứa một từ khóa cấm có sẵn trong `bannedKeywords`: có thông báo báo trước và tài liệu tin có `flagged` true.
- [ ] Bật chế độ máy bay: nút "Đăng tin" khóa và có dòng giải thích; chữ và ảnh vẫn còn.
- [ ] Xoay màn hình khi đã nhập chữ, chọn thẻ và 3 ảnh: không mất gì; xoay khi đang đăng: không có hai tin.
- [ ] `cd PUBGApp && ./gradlew assembleDebug testDebugUnitTest lintDebug` qua, không báo chữ cứng hay thiếu `contentDescription`.
- [ ] `sh workflow/scripts/check-java-style.sh` không báo lỗi (không lambda, không `var`, chú thích tối đa 2 dòng).
- [ ] Danh sách mọi lớp, phương thức, trường công khai mới kèm dòng tài liệu định nghĩa đã dán vào PR; tên nội bộ do issue đặt được ghi riêng (R16).

### Quy mô
- [x] Đây là một lát cắt dọc của một module, khoảng nửa ngày đến một ngày, một Pull Request.

### Câu hỏi cho leader
1. Quy tắc Security Rules "có GIVEAWAY thì giá bằng 0" xung đột với "giá bắt buộc khi có Bán" nếu chọn đồng thời Bán và Cho tặng; UC10 bước 5b1 nói chọn "Cho tặng" thì khóa trả giá. Đề xuất: chọn "Cho tặng" thì tự bỏ chọn "Bán" và "Trao đổi", giá 0, `acceptedOfferTypes` rỗng và `minOfferPrice` 0; "Bán" kết hợp được với "Trao đổi". Nếu đồng ý, ghi vào `dac-ta-chuc-nang.md` STT 18.
2. Danh sách tỉnh, thành phố dùng chung cho A06, B02 và B04 chưa có nguồn. Đề xuất Phát thêm một mảng chuỗi dùng chung trong `strings_common.xml`; trong lúc chờ module B dùng mảng riêng trong `strings_listing.xml`. Giá trị phải thống nhất với dữ liệu mẫu (ví dụ "TP.HCM", "Hà Nội", "Đà Nẵng").
3. Chụp ảnh bằng camera (STT 17, UC10 bước 2) cần một nơi để máy ảnh ghi ảnh: `FileProvider` trong `AndroidManifest.xml` (file của Phát, hiện chưa có) hoặc một Uri tạo bằng `MediaStore` không cần `FileProvider` (chưa kiểm chứng, phải tra tài liệu AndroidX trước khi dùng). Phát chọn cách nào và có thêm vào manifest không?
4. UC10 luồng 5a1 nói biểu mẫu có ô "Món đồ mong muốn đổi lại" khi chọn "Trao đổi", nhưng `Listing` không có trường lưu và B04 không vẽ ô này. Đề xuất không làm ô này (người bán ghi vào mô tả); xin xác nhận hoặc thêm trường (R6).
5. Tình trạng có bắt buộc khi đăng không? UC10 bước 7 bắt buộc; B04, UC17 BR1, `Listing.publish` và Security Rules không liệt kê. Cách hiểu mặc định: biểu mẫu bắt buộc chọn tình trạng khi bấm "Đăng tin", nhưng `Listing.publish` không kiểm. Xin xác nhận.
