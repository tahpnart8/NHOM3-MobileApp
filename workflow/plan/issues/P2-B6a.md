TITLE: feat(listing): chi tiết tin đăng, lưu tin và chế độ khách
LABELS: type:feature, area:listing, module:b, priority:p1
ASSIGNEES: tranannhtu21012006
PHASE: P2

Plan item: P2-B6a

### Bối cảnh
1. PUBGApp (Pre-owned Users' Bargain Grounds) là ứng dụng Android mua bán, trao đổi, cho tặng đồ cũ, viết bằng Java 17, XML Views và ViewBinding, dùng Firebase (Authentication, Firestore gói Spark) và Room.
2. Module B (Tin đăng và khám phá, người làm: Trần Anh Tú) lo đăng tin, kệ hàng, trang chủ, tìm kiếm, chi tiết tin và tin đã lưu.
3. Issue này làm phần hiển thị của màn B03 `ListingDetailFragment`: băng chuyền ảnh, thông tin tin, thẻ người bán, tình trạng chi tiết, nhãn trạng thái tin, nút của chủ tin, lưu tin, ghi lịch sử xem và chế độ khách (UC16, UC13 luồng 4a).
4. Đọc trước: `workflow/docs/ui/b-kham-pha-tin-dang/README.md#b03-chi-tiết-tin-đăng`, `workflow/docs/product/use-case/uc-b-tin-dang.md#uc16-xem-chi-tiết-tin-đăng`, `workflow/docs/data/security-rules.md` quy ước chung số 9 (Khách chỉ đọc `listings`, `categories`, `images`).
5. Đã có trên `main`: khung `ListingDetailFragment` và `fragment_listing_detail.xml`, `ListingRepository.getById` và `UserRepository.getSavedIds` (từ P2-B5), `ListingCardAdapter` (từ P2-B3), chế độ sửa tin của B04 và `ShopViewModel.changeStatus` (từ P2-B2b), `MediaRepository.getImage` (P1-04), mọi ViewModel đã có `loading`, `error`, `done` kèm getter công khai (`ListingDetailViewModel` còn có `images`, `sellerStats`, `followingSeller`, `loadImages()`, `toggleFollowSeller()` và dùng cả `MediaRepository`), các hành động toàn cục `action_global_sign_in` và `action_global_listing_detail` trong `nav_main.xml`, thành phần chung `view_avatar.xml`, `view_rating.xml`, `view_status_chip.xml`, `view_detail_toolbar.xml`.

### Mã tính năng
F-07 (lưu tin) và F-04 (hiển thị tin, nút của chủ tin)

### Mã việc kế hoạch
P2-B6a. Nửa đầu của P2-B6 (chi tiết tin): phần hiển thị, lưu tin và chế độ khách. Nửa sau là P2-B6b (nút nhắn tin, đề nghị giá, theo dõi, báo cáo, liên kết sang màn khác, tin tương tự, giá tham khảo). Tách vì B03 gồm băng chuyền ảnh, thẻ người bán, bốn trạng thái tin, chế độ chủ tin, chế độ khách, lưu tin, ghi lịch sử xem và nhiều lối điều hướng, quá một ngày.

### Module
B. Tin đăng và khám phá (Trần Anh Tú)

### STT trong bảng đặc tả
40 và 42 (lưu tin, bỏ lưu từ B03), 13 và 15 (nút "Chỉnh sửa" và "Đã bán" với tin của chính mình). STT 36, 37 làm ở P2-B6b; STT 39, 43, 47, 61 là nút điều hướng của P2-B6b. Tất cả có Phạm vi "Đợt này".

### Màn hình mockup
B03 (ảnh `workflow/docs/ui/b-kham-pha-tin-dang/b03-chi-tiet-tin-dang.png`). Ảnh đếm "1/6" (tối đa là 5 ảnh), chưa vẽ nhãn "Tài khoản đã xác thực", mục "Tin tương tự", liên kết "Xem đánh giá", và trạng thái khách; các phần này vẫn phải làm (mục "Khác với ảnh" của B03).

### Làm gì và vì sao
Người dùng và Khách bấm một thẻ tin ở bất kỳ đâu và vào B03 thấy: băng chuyền ảnh (đếm "1/5"), nhãn hình thức (BÁN, TRAO ĐỔI, CHO TẶNG, có thể nhiều nhãn), tình trạng, tên, giá, khu "Tình trạng chi tiết" (từ `conditionValues`), mô tả, khu vực, thẻ người bán (ảnh đại diện, tên hiển thị, điểm sao, nhãn "Tài khoản đã xác thực" khi `verificationStatus` là APPROVED) và thanh hành động dưới cùng. Khách không đọc được `users` nên khối người bán bị ẩn và thay bằng lời mời đăng nhập; Khách bấm nút thả tim thì được đưa tới A01, không lưu tin. Người đã đăng nhập bấm trái tim để lưu hoặc bỏ lưu tin (`users/{uid}/saved/{listingId}`). Mỗi lần mở tin ghi một lần xem vào Room để làm nguồn cho "Dành cho bạn". Tin của chính mình hiện "Chỉnh sửa" và "Đã bán" thay cho "Nhắn tin" và "Đề nghị giá". Tin RESERVED, SOLD hiện đúng nhãn và trạng thái nút; tin ẩn hoặc gỡ với người khác hiện "Tin không còn". Ảnh bìa thu nhỏ hiện ngay, ảnh đầy đủ được tải sau và thay vào. Issue này phục vụ dòng barem R-Chapter 4 (RecyclerView ngang kèm `PagerSnapHelper` làm băng chuyền ảnh; nút trái tim là nút bấm kiểu ảnh; điều hướng bằng Navigation component với các hành động toàn cục `action_global_sign_in`, `action_global_listing_detail`), dòng R-Chapter 1, 2, 6, 7 (async: đọc Firestore và ghi `recent_view` của Room qua `DataCallback`, ngoài luồng chính; trạng thái giữ trong ViewModel nên qua được xoay màn hình) và dòng R-Chapter 3 (unit test JUnit `ListingDetailViewModelTest`).

### Tiêu chí nghiệm thu
- [ ] Luồng chính chạy trên máy ảo với Firebase thật: mở B03 từ trang chủ đọc `listings/{id}`, `users/{sellerId}` (khi đã đăng nhập) và hiện ảnh; số ảnh đúng và đếm "n/tổng" cập nhật khi vuốt ngang (ảnh đầy đủ đọc bằng `ListingDetailViewModel.loadImages()`, `images` có dữ liệu thì thay ảnh bìa thu nhỏ).
- [ ] Chế độ khách: chưa đăng nhập vẫn mở được B03 từ trang chủ và từ kết quả tìm kiếm; thấy ảnh, giá, nhãn, tình trạng, tình trạng chi tiết, mô tả, khu vực; khối thẻ người bán bị ẩn và có dòng mời đăng nhập kèm nút sang A01; bấm nút thả tim sang A01 bằng `action_global_sign_in`, không có tài liệu nào được ghi (xem câu hỏi 2).
- [ ] Nút trái tim (đã đăng nhập): bấm lưu thì có `users/{uid}/saved/{listingId}` với `savedAt` và hiện "Đã lưu tin vào danh sách yêu thích"; bấm lại thì tài liệu bị xóa và hiện "Đã bỏ lưu tin đăng"; biểu tượng đúng với trạng thái khi mở lại màn; phản hồi trên giao diện ngay, lỗi thì trả lại trạng thái cũ và báo. PR ghi rõ đã kiểm trên Firebase Console.
- [ ] Mỗi lần mở tin thành công (kể cả Khách) thì `recent_view` có một dòng (`listingId`, `categoryId`, `viewedAtMillis`), xem lại thì cập nhật thời điểm, bảng được cắt còn 50 dòng.
- [ ] Thẻ người bán (đã đăng nhập) hiện tên hiển thị, ảnh đại diện (hoặc chữ cái đầu), điểm sao `User.ratingAverage()` cùng `ratingCount` qua `view_rating.xml`, và nhãn "Tài khoản đã xác thực" chỉ khi `verificationStatus` là APPROVED (không dùng chữ "đã xác minh"). Số đã bán lấy từ `ListingDetailViewModel.sellerStats` (`ProfileStats.soldCount`), xem câu hỏi 1.
- [ ] Khu "Tình trạng chi tiết" liệt kê `conditionValues`; không có thì ẩn khu này. Nhãn hình thức hiện đủ theo `tradeTypes`; giá 0 hiện "Miễn phí".
- [ ] Trạng thái tin: RESERVED hiện nhãn "ĐANG GIỮ CHỖ", nút "Đề nghị giá" xám, "Nhắn tin" vẫn dùng; SOLD hiện nhãn "ĐÃ BÁN" đè lên ảnh đã làm mờ và cả hai nút xám; tin DRAFT, HIDDEN, REMOVED hoặc không đọc được (`getById` trả `null`, hay Rules từ chối) với người không phải chủ tin hiện "Tin không còn". Chủ tin mở tin DRAFT, HIDDEN, REMOVED của mình vẫn xem được và thấy nhãn trạng thái chuẩn (Nháp, Đã ẩn, Đã gỡ).
- [ ] Tin của chính mình (`sellerId` bằng `SessionManager.getCurrentUserId()`): nút "Chỉnh sửa" mở B04 ở chế độ sửa (P2-B2b) và nút "Đã bán" gọi `ShopViewModel.changeStatus` sang SOLD (chỉ khi AVAILABLE; ngược lại xám).
- [ ] Phương thức của module khác trả `DataCallback.NOT_READY` thì màn hình hiện đúng lỗi đó, không giả dữ liệu. Mỗi lỗi mạng hiện câu tiếng Việt. Đủ bốn trạng thái: đang tải (khi `ListingDetailViewModel.loading` bật), có dữ liệu, "Tin không còn" (rỗng), lỗi kèm "Thử lại" (nội dung từ `error`, hiện một lần rồi xóa).
- [ ] Xoay màn hình không tải lại tin (`listing` còn trong ViewModel); vị trí băng chuyền và vị trí cuộn được hoàn thiện ở P3-Ba.
- [ ] Mọi thao tác mạng và Room chạy ngoài luồng chính; nút lưu bị khóa khi offline kèm dòng giải thích.
- [ ] Quy tắc hiển thị nằm trong phương thức package-private `static` của `ListingDetailViewModel` và có unit test `ListingDetailViewModelTest`: ai xem được tin theo `status`, và trạng thái hai nút "Nhắn tin", "Đề nghị giá" theo `status` (AVAILABLE cả hai; RESERVED chỉ nhắn tin; SOLD không nút nào; chủ tin không có hai nút này).
- [ ] `./gradlew assembleDebug testDebugUnitTest lintDebug` và `sh workflow/scripts/check-java-style.sh` đều qua.
- [ ] PR liệt kê từng lớp, phương thức, trường công khai mới kèm dòng tài liệu định nghĩa (R16).

### Tài liệu phải đọc
- `AGENTS.md` (R12, R15 đến R23), `workflow/plan/so-huu.md` mục 3, 4, 5, 6.
- `workflow/docs/ui/b-kham-pha-tin-dang/README.md` mục B03 (đủ "Hành động và điều hướng", "Trạng thái đặc biệt", "Khác với ảnh") và `workflow/docs/ui/README.md` mục "Điều hướng" (chế độ khách).
- `workflow/docs/product/use-case/uc-b-tin-dang.md` UC16 (luồng cơ bản, 2a, ngoại lệ 1e, 4e, 4f, BR1 đến BR3) và UC13 (luồng 4a, ngoại lệ 4e).
- `workflow/docs/architecture/luong-xu-ly.md` mục 3, điểm 5 và 7.
- `workflow/docs/architecture/dac-ta-lop/viewmodel.md#listingdetailviewmodel`, `#shopviewmodel` (`changeStatus`).
- `workflow/docs/architecture/dac-ta-lop/data.md#listingrepository` (dòng `getById`, `recordView`), `#userrepository` (dòng `saveListing`, `unsaveListing`, `getSavedIds`, `getProfile`), `#mediarepository` (dòng `getImage`).
- `workflow/docs/architecture/dac-ta-lop/model.md#listing`, `#user` (`ratingAverage`, `verificationStatus`), `#mediaimage` (`toBytes`).
- `workflow/docs/architecture/dac-ta-lop/data-local.md#historydao`, `#recentviewentity`.
- `workflow/docs/data/firestore-schema.md#savedlistings`, `#images`; `workflow/docs/data/room-schema.md#recent_view`.
- `workflow/docs/data/security-rules.md` mục 4 (savedListings), mục 10 (đoạn "Đọc"), mục 24 (images), quy ước chung số 9, mục "Giới hạn" dòng "Giấu thông tin người bán với khách".
- `workflow/docs/architecture/trang-thai.md` mục 1 và `workflow/docs/conventions/giao-dien.md` mục 4 (nhãn và màu trạng thái tin).
- `workflow/memory/code/firebase.md`.
- `workflow/docs/product/barem-diem.md` (dòng barem mà issue phục vụ).

### Lớp, trường, trạng thái sẽ dùng
- `ListingDetailFragment`; `ListingDetailViewModel.load(String listingId)`, `loadImages()`, `toggleSave()`, và các LiveData `listing` (`LiveData<Listing>`), `seller` (`LiveData<User>`), `sellerStats` (`LiveData<ProfileStats>`), `images` (`LiveData<List<byte[]>>`), `saved` (`LiveData<Boolean>`), `loading` (`LiveData<Boolean>`), `error` (`LiveData<String>`), `done` (`LiveData<Boolean>`), mỗi LiveData có getter công khai. (`followingSeller`, `toggleFollowSeller()`, `similar`, `priceRange` thuộc P2-B6b.)
- `ListingRepository.getById(String id, DataCallback<Listing> cb)` (đã có từ P2-B5), `recordView(Listing listing)`.
- `UserRepository.saveListing(String uid, String listingId, DataCallback<Void> cb)`, `unsaveListing(String uid, String listingId, DataCallback<Void> cb)`, `getSavedIds(String uid, DataCallback<List<String>> cb)` (đã có từ P2-B5); `UserRepository.getProfile(String uid, DataCallback<User> cb)` (của Ngân, chỉ gọi), `UserRepository.getStats(String uid, DataCallback<ProfileStats> cb)` (chưa có chủ trong `so-huu.md` mục 5, xem câu hỏi 1); `ProfileStats.soldCount`.
- `MediaRepository.getImage(String id, DataCallback<byte[]> cb)` (gọi qua `ListingDetailViewModel.loadImages()`).
- `ShopViewModel.changeStatus(String id, ListingStatus status)` (làm ở P2-B2b, chỉ gọi).
- `Listing`: `sellerId`, `title`, `description`, `imageIds`, `coverThumb`, `itemCondition`, `conditionValues`, `tradeTypes`, `price`, `status`, `statusReason`, `province`; `User`: `displayName`, `avatarBase64`, `verificationStatus`, `ratingCount`, `ratingAverage()`.
- `ListingStatus` (`DRAFT`, `AVAILABLE`, `RESERVED`, `SOLD`, `HIDDEN`, `REMOVED`), `VerificationStatus.APPROVED`, `TradeType`, `ItemCondition`.
- `HistoryDao.insertView(RecentViewEntity)`, `trimViews(int)`.
- Hành động toàn cục `action_global_sign_in` (Khách bấm trái tim hoặc lời mời đăng nhập), `action_global_listing_detail`.
- `SessionManager.getInstance()`, `isLoggedIn()`, `getCurrentUserId()`; `NetworkMonitor.getInstance()`, `isOnline()`; `FormatUtils.formatPrice(long)`.

Tên nội bộ do issue này đặt (package-private hoặc lớp giao diện, ghi vào PR ở mục tên mới): trong `ListingDetailViewModel` các hàm `static boolean canShowListing(Listing listing, String viewerId)`, `static boolean canStartChat(Listing listing, String viewerId)` và `static boolean canSendOffer(Listing listing, String viewerId)` (P2-B6b dùng lại); lớp `ListingImagePagerAdapter`; layout `item_listing_detail_image.xml`.

### File được tạo hoặc sửa
- [sửa] `PUBGApp/app/src/main/java/com/nhom3/pubgapp/ui/listing/ListingDetailFragment.java`
- [tạo] `PUBGApp/app/src/main/java/com/nhom3/pubgapp/ui/listing/ListingImagePagerAdapter.java` (băng chuyền ảnh bằng `RecyclerView` ngang với `PagerSnapHelper`, không thêm thư viện)
- [sửa] `PUBGApp/app/src/main/res/layout/fragment_listing_detail.xml`
- [tạo] `PUBGApp/app/src/main/res/layout/item_listing_detail_image.xml`
- [sửa] `PUBGApp/app/src/main/res/values/strings_listing.xml`
- [sửa] `PUBGApp/app/src/main/java/com/nhom3/pubgapp/viewmodel/ListingDetailViewModel.java` (chỉ thân `load`, `loadImages`, `toggleSave`, việc đẩy dữ liệu vào `listing`, `seller`, `sellerStats`, `images`, `saved`, `loading`, `error`, `done`, và các hàm package-private ở trên)
- [sửa] `PUBGApp/app/src/main/java/com/nhom3/pubgapp/data/ListingRepository.java` (chỉ thân `recordView`)
- [sửa] `PUBGApp/app/src/main/java/com/nhom3/pubgapp/data/UserRepository.java` (chỉ thân `saveListing`, `unsaveListing`; thêm `getStats` chỉ khi leader giao cho Tú ở câu hỏi 1)
- [tạo] `PUBGApp/app/src/test/java/com/nhom3/pubgapp/viewmodel/ListingDetailViewModelTest.java`

### Các bước đề nghị
Người làm có thể đổi thứ tự nhưng không được đổi tên nào ở trên.
1. Trong `ListingDetailViewModel` viết ba hàm package-private: `static boolean canShowListing(Listing listing, String viewerId)` (false khi `listing` null; true khi `status` là AVAILABLE, RESERVED hoặc SOLD; với DRAFT, HIDDEN, REMOVED chỉ true khi `viewerId` bằng `sellerId`), `static boolean canStartChat(Listing listing, String viewerId)` (true khi AVAILABLE hoặc RESERVED và `viewerId` khác `sellerId`) và `static boolean canSendOffer(Listing listing, String viewerId)` (true khi AVAILABLE và `viewerId` khác `sellerId`).
2. Viết `ListingDetailViewModelTest` cho ba hàm trên rồi chạy `./gradlew testDebugUnitTest`.
3. Làm thân `ListingRepository.recordView(Listing listing)`: ngoài luồng chính, `HistoryDao.insertView` một `RecentViewEntity` (`listingId`, `categoryId`, `viewedAtMillis`), rồi `HistoryDao.trimViews(50)`.
4. Làm thân `UserRepository.saveListing` (tạo `users/{uid}/saved/{listingId}` với `userId`, `listingId`, `savedAt` bằng `FieldValue.serverTimestamp()`; Rules mục 4) và `unsaveListing` (xóa tài liệu đó).
5. Làm thân `ListingDetailViewModel.load(String listingId)`: `ListingRepository.getById`; nếu có tin thì `ListingRepository.recordView`; nếu `SessionManager.getInstance().isLoggedIn()` thì thêm `UserRepository.getProfile(sellerId)` đặt `seller`, `UserRepository.getStats(sellerId)` đặt `sellerStats` (chỉ khi câu hỏi 1 đã được trả lời, nếu chưa thì để trống và ẩn dòng "đã bán") và `UserRepository.getSavedIds` để đặt `saved`; Khách thì bỏ qua cả ba. Bật `loading` khi bắt đầu và tắt khi xong; lỗi đặt vào `error`. Dùng lớp ẩn danh `DataCallback`, không lambda.
6. Làm thân `ListingDetailViewModel.toggleSave()`: đổi `saved` ngay, gọi `saveListing` hoặc `unsaveListing`, lỗi thì trả lại giá trị cũ và báo.
7. Dựng `fragment_listing_detail.xml` bằng `ConstraintLayout` trong `NestedScrollView`: `view_detail_toolbar.xml` (quay lại, nút trái tim), băng chuyền ảnh với số ảnh, nhãn hình thức và tình trạng (dùng `view_status_chip.xml` cho nhãn trạng thái), tên, giá (`textAppearanceTitleMedium` kèm `colorPrimary`), thẻ người bán, "Tình trạng chi tiết", mô tả, khu vực, thanh hành động cố định dưới cùng (chỗ cho "Nhắn tin", "Đề nghị giá", "Chỉnh sửa", "Đã bán"), `view_error_state.xml`, `view_offline_banner.xml`, thanh tiến trình. Màu, khoảng cách, cỡ chữ lấy từ token; id view theo lowerCamelCase `<loại><Việc>` (ví dụ `recyclerImages`, `buttonSave`, `textPrice`).
8. Làm `ListingImagePagerAdapter` và `item_listing_detail_image.xml`: giải mã ảnh ngoài luồng chính, có ảnh giữ chỗ và ảnh dự phòng. Làm thân `ListingDetailViewModel.loadImages()`: gọi `MediaRepository.getImage(String id, DataCallback<byte[]> cb)` cho từng mã trong `Listing.imageIds` ngoài luồng chính, gom theo đúng thứ tự vào `images`; Fragment hiện `coverThumb` làm ảnh đầu cho tới khi `images` có dữ liệu.
9. Làm `ListingDetailFragment`: quan sát `listing`, `seller`, `sellerStats`, `images`, `saved`, `loading`, `error`; sau khi `listing` có dữ liệu thì gọi `loadImages()`; dùng `canShowListing` để chọn giữa nội dung và "Tin không còn"; áp trạng thái nút bằng `canStartChat`, `canSendOffer` (khóa nút bằng cách làm xám, việc bấm làm ở P2-B6b); với chủ tin hiện "Chỉnh sửa" (mở B04 sửa bằng đích điều hướng của P2-B2b, mang `listingId`) và "Đã bán" (gọi `ShopViewModel.changeStatus(id, ListingStatus.SOLD)` khi AVAILABLE); trái tim: Khách thì gọi `action_global_sign_in`, đã đăng nhập thì kiểm `NetworkMonitor.getInstance().isOnline()` rồi `toggleSave()`. Khách thì ẩn khối thẻ người bán và hiện lời mời đăng nhập kèm nút gọi `action_global_sign_in`.
10. Thêm chuỗi vào `strings_listing.xml` (khóa theo mẫu `<module>_<màn>_<việc>`, ví dụ `listing_detail_price_label`); mọi chữ nhãn là string resource.
11. Chạy các lệnh ở mục "Tự kiểm trước khi mở Pull Request", thử bằng tài khoản người mua, tài khoản chủ tin và Khách.

### File dùng chung cần thêm dòng
Không có.

### Cấm đụng
- File chỉ Phát sửa: `AndroidManifest.xml`, `nav_main.xml`, `nav_admin.xml`, menu thanh điều hướng, `themes.xml`, `colors.xml`, `dimens.xml`, `strings_common.xml`, `AppDatabase`, `Converters`, mọi file Gradle, mọi file trong `firebase/`.
- Lớp `model` và `model.enums`: không sửa.
- `UserRepository.getProfile` (Ngân), `UserRepository.follow`, `unfollow`, `getFollowingIds` (Phúc): chỉ gọi `getProfile`; hai phương thức theo dõi để P2-B6b.
- `MediaRepository`, `MediaImage` (Phát): chỉ gọi.
- `ShopViewModel` (module B, nhưng thuộc P2-B2a và P2-B2b): chỉ gọi `changeStatus`.
- `ChatViewModel`, `ChatRepository` (Hải Long), `TrustViewModel` (Phúc), D01 đến D04: không sửa, đi qua hành động toàn cục ở P2-B6b.
- Các phương thức khác của `ListingDetailViewModel` (`loadSimilar`, `loadPriceRange`, `toggleFollowSeller` và LiveData `followingSeller`: P2-B6b).

### Phụ thuộc
P2-B5 (cần `getById`, `getSavedIds`), P2-B2b (nút "Chỉnh sửa" mở chế độ sửa của B04). Dùng, không chờ: P2-A3 (`UserRepository.getProfile`).

### Ngoài phạm vi
- Nút "Nhắn tin", "Đề nghị giá", "Theo dõi", "Báo cáo tin", thẻ người bán mở D02, liên kết "Xem đánh giá", tin tương tự, giá tham khảo: P2-B6b.
- Dòng "đã bán" của người bán: chờ câu hỏi 1.
- Xem tin đã đệm khi offline đầy đủ và khóa nút ghi theo mạng: P3-Bb (issue này chỉ khóa nút lưu khi `isOnline()` là false).
- Chia sẻ tin; thả tim ở thẻ tin trang chủ.
- Chặn Khách ở thanh dưới: `MainActivity` (Phát). Bố cục ngang và giữ vị trí băng chuyền khi xoay: P3-Ba.

### Tài liệu cần cập nhật
Không có, trừ khi các câu hỏi dưới đây đổi hành vi (khi đó cập nhật `viewmodel.md`, `thanh-phan.md` và mục B03 của `workflow/docs/ui/b-kham-pha-tin-dang/README.md` theo R7).

### Tự kiểm trước khi mở Pull Request
- [ ] `cd PUBGApp && ./gradlew testDebugUnitTest` chạy `ListingDetailViewModelTest`: toàn bộ ca qua.
- [ ] Đã đăng nhập, mở một tin AVAILABLE của người khác: thấy ảnh, giá, nhãn, thẻ người bán có sao, nút "Nhắn tin" và "Đề nghị giá" ở trạng thái dùng được.
- [ ] Bấm trái tim: Console có `users/{uid}/saved/{listingId}`; bấm lại: tài liệu mất.
- [ ] Mở tin, rồi xem Room (hoặc mở "Dành cho bạn" sau đó): có một dòng `recent_view` cho tin đó.
- [ ] Đặt trên Console một tin sang RESERVED rồi SOLD: nhãn và trạng thái nút đúng; đặt sang HIDDEN rồi mở bằng tài khoản khác: "Tin không còn"; mở bằng tài khoản chủ tin: vẫn xem được.
- [ ] Mở tin của chính mình: thấy "Chỉnh sửa" và "Đã bán"; bấm "Đã bán" thì Console đổi `status` sang SOLD.
- [ ] Chưa đăng nhập: xem được tin, khối người bán ẩn, trái tim đưa sang A01, Console không có tài liệu mới.
- [ ] Bật chế độ máy bay: nút trái tim khóa kèm dòng giải thích.
- [ ] `cd PUBGApp && ./gradlew assembleDebug testDebugUnitTest lintDebug` qua.
- [ ] `sh workflow/scripts/check-java-style.sh` không báo lỗi.
- [ ] Danh sách mọi lớp, phương thức, trường công khai mới kèm dòng tài liệu định nghĩa đã dán vào PR; tên nội bộ được ghi riêng (R16).

### Quy mô
- [x] Đây là một lát cắt dọc của một module, khoảng nửa ngày đến một ngày, một Pull Request.

### Câu hỏi cho leader
1. `ListingDetailViewModel.sellerStats` (số đã bán, hoàn tất của người bán) lấy từ `UserRepository.getStats(String uid, DataCallback<ProfileStats> cb)`, nhưng `so-huu.md` mục 5 vẫn không giao `getStats` cho ai (cũng như `searchUsers`, `getVerification`, `createProfile`, `clearExpiredLock`). Đề xuất giao `getStats` cho Phúc (D01, D02 cũng dùng) hoặc cho Tú. Chưa có xác nhận thì không viết thân `getStats` (R20) và B03 ẩn dòng "đã bán".
2. Mục "Trạng thái đặc biệt" của B03 ghi Khách "xem được ... thẻ tóm tắt người bán", nhưng Security Rules không cho Khách đọc `users` (`security-rules.md` quy ước chung số 9 và dòng "Giấu thông tin người bán với khách" ở mục "Giới hạn"). Issue này ẩn khối người bán với Khách và mời đăng nhập. Xin xác nhận và sửa mục B03 của README giao diện.
