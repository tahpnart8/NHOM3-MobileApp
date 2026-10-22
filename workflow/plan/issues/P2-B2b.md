TITLE: feat(listing): sửa, ẩn, hiện và đánh dấu đã bán tin
LABELS: type:feature, area:listing, module:b, priority:p1
ASSIGNEES: tranannhtu21012006
PHASE: P2

Plan item: P2-B2b

### Bối cảnh
1. PUBGApp (Pre-owned Users' Bargain Grounds) là ứng dụng Android mua bán, trao đổi, cho tặng đồ cũ, viết bằng Java 17, XML Views và ViewBinding, dùng Firebase (Authentication, Firestore gói Spark) và Room.
2. Module B (Tin đăng và khám phá, người làm: Trần Anh Tú) lo đăng tin, kệ hàng, trang chủ, tìm kiếm, chi tiết tin và tin đã lưu.
3. Issue này hoàn thiện menu ba chấm của B05 theo trạng thái tin (chỉnh sửa, ẩn, hiện lại, đánh dấu đã bán, mở bán lại) và làm chế độ sửa tin của B04 (UC11, UC09).
4. Đọc trước: `workflow/docs/ui/b-kham-pha-tin-dang/README.md#b05-kệ-hàng-của-tôi` (mục "Hành động và điều hướng"), `workflow/docs/product/use-case/uc-b-tin-dang.md#uc11-quản-lý-kệ-hàng`, `workflow/docs/architecture/trang-thai.md` mục 1 và 2.
5. Đã có trên `main`: B05 với 4 tab, sắp xếp, đăng và xóa tin nháp (P2-B2a); B04 đăng tin mới và lưu nháp, `ShopViewModel.createListing`, `saveDraft`, `ListingRepository.create`, `saveDraft` (P2-B1a, P2-B1b); mọi ViewModel đã có `loading`, `error`, `done` kèm getter công khai; `NotificationRepository.create(notification, batch, cb)` đã nhận lô ghi (P1); mọi lớp còn lại đủ chữ ký (thân chưa làm trả `DataCallback.NOT_READY`).

### Mã tính năng
F-04

### Mã việc kế hoạch
P2-B2b. Nửa sau của P2-B2 (kệ hàng của tôi), làm sau P2-B2a.

### Module
B. Tin đăng và khám phá (Trần Anh Tú)

### STT trong bảng đặc tả
13 (chỉnh sửa tin, đánh dấu đã bán và mở bán lại), 15 (ẩn hoặc hiện lại tin). Cả hai có Phạm vi "Đợt này".

### Màn hình mockup
B05 (menu ba chấm theo trạng thái, ảnh `workflow/docs/ui/b-kham-pha-tin-dang/b05-ke-hang-cua-toi.png`; ảnh vẽ cùng một menu cho mọi thẻ, đúng là menu đổi theo trạng thái) và B04 ở chế độ sửa tin.

### Làm gì và vì sao
Sau issue này menu ba chấm trên mỗi thẻ ở B05 đủ thao tác theo trạng thái của tin: tin đang bán có "Chỉnh sửa", "Đánh dấu đã bán", "Ẩn tin"; tin đã ẩn do chính mình có "Hiện lại", "Chỉnh sửa"; tin nháp có "Chỉnh sửa", "Đăng", "Xóa tin"; tin đã bán do người bán tự đánh dấu có "Mở bán lại" (từ chối với tin đã bán qua giao dịch hoàn tất); tin đang giữ chỗ và tin đã gỡ chỉ xem; tin đã ẩn do admin chỉ xem lý do admin ghi, người bán không hiện lại được. "Chỉnh sửa" mở B04 ở chế độ sửa, điền sẵn dữ liệu, nút chính "Lưu thay đổi" (tin nháp có thêm "Đăng tin"). Ẩn tin hoặc đánh dấu đã bán thì mọi offer PENDING của tin bị từ chối trong cùng lô ghi, kèm thông báo OFFER_RESULT cho từng người mua (`trang-thai.md` mục 1 và 2). Mọi thao tác ghi này không cần tài khoản xác thực ngoài nút "Đăng" của tin nháp (đã chặn ở P2-B2a). Khi sửa, người bán cũng thêm ảnh mới hoặc bỏ ảnh đã lưu được, vì `updateListing` nay nhận cả danh sách ảnh mới. Issue này phục vụ dòng barem R-Chapter 4 (menu: `PopupMenu` ba chấm đổi mục theo trạng thái của tin; input control của biểu mẫu sửa; điều hướng bằng Navigation component với đối số `listingId`), dòng R-Chapter 1, 2, 6, 7 (implicit Intent chọn thêm ảnh từ thư viện khi sửa tin; async qua `DataCallback`) và dòng R-Chapter 3 (unit test JUnit cho `allowedActions` và `isAllowedStatusChange` trong `ShopViewModelTest`).

### Tiêu chí nghiệm thu
- [ ] Menu đúng theo bảng trạng thái: AVAILABLE (Chỉnh sửa, Đánh dấu đã bán, Ẩn tin); RESERVED (chỉ xem, không mục đổi trạng thái); HIDDEN có `statusReason` rỗng (Hiện lại, Chỉnh sửa); HIDDEN có `statusReason` khác rỗng (chỉ xem lý do admin ghi); SOLD (Mở bán lại); DRAFT (Chỉnh sửa, Đăng, Xóa tin); REMOVED (chỉ xem). "Xóa tin" chỉ có ở tin nháp.
- [ ] "Chỉnh sửa" tin AVAILABLE, DRAFT hoặc HIDDEN do chính mình ẩn: B04 mở sẵn dữ liệu, nút chính "Lưu thay đổi". Tin AVAILABLE hoặc HIDDEN: "Lưu thay đổi" ghi `listings` đúng các trường được phép sửa (Rules mục 10, dòng "Sửa nội dung"), tạo lại `searchKeywords`, `flagged` chỉ đổi từ false sang true; thành công hiện "Cập nhật sản phẩm thành công!" và quay về B05. Tin nháp: "Lưu thay đổi" cập nhật tin nháp (vẫn DRAFT), "Đăng tin" lưu rồi đăng (có cửa xác thực như P2-B1a). Ảnh: thêm ảnh mới (tổng cộng tối đa 5 ảnh, tính cả ảnh đã lưu) và bỏ ảnh đã lưu đều được; `imageIds` đúng sau khi lưu, ảnh bị bỏ mất khỏi `images` (`MediaRepository.delete`), `coverThumb` ứng với ảnh đầu (xem câu hỏi 2).
- [ ] Sửa tin thiếu tiêu đề, danh mục, hình thức giao dịch hoặc có "Bán" mà giá bằng 0: báo lỗi dưới đúng ô và không ghi (UC11 ngoại lệ 3e). Sửa tin chứa từ khóa cấm: `flagged` thành true, người bán được báo.
- [ ] "Ẩn tin" tin AVAILABLE: `status` HIDDEN, `statusReason` rỗng, hiện "Tin đăng đã được ẩn", thẻ sang tab Đã ẩn với nhãn ĐÃ ẨN; tin biến khỏi `getFeed` của người khác. Mọi offer PENDING của tin sang REJECTED và mỗi người mua có một thông báo OFFER_RESULT, trong cùng lô ghi (thông báo ghi bằng `NotificationRepository.create(AppNotification notification, WriteBatch batch, DataCallback<Void> cb)` truyền lô đang dựng). PR ghi rõ đã thử với một offer PENDING thật (tạo bằng tài khoản thứ hai hoặc trực tiếp trên Console).
- [ ] "Hiện lại" tin tự ẩn: HIDDEN sang AVAILABLE. Tin do admin ẩn (`statusReason` khác rỗng) không có mục này, và nếu repository bị gọi thì từ chối bằng câu tiếng Việt.
- [ ] "Đánh dấu đã bán" tin AVAILABLE: sang SOLD, hiện "Đã chuyển sản phẩm sang mục Đã bán", thẻ sang tab Đã bán; cũng từ chối offer PENDING cùng lô. Tin RESERVED không có mục này; nếu bị gọi thì báo "Sản phẩm đang trong quá trình giao dịch với người mua, không thể thay đổi trạng thái thủ công".
- [ ] "Mở bán lại" tin SOLD: SOLD sang AVAILABLE khi tin chưa có giao dịch COMPLETED; có giao dịch hoàn tất thì `setStatus` từ chối với câu tiếng Việt nêu lý do và trạng thái không đổi (xem câu hỏi 1). `DealRepository` còn `NOT_READY` thì hiện đúng lỗi đó, không cho mở bán lại (R12).
- [ ] Mọi đổi trạng thái đi qua `ListingRepository.setHidden` hoặc `setStatus`; `setStatus` từ chối mọi cặp ngoài (AVAILABLE, SOLD) và (SOLD, AVAILABLE) bằng câu báo lỗi tiếng Việt (R18). Màn hình không ghi trực tiếp trường `status`.
- [ ] Tin đã ẩn do admin: chọn "Xem lý do" hiện nguyên `statusReason`; người bán không có cách hiện lại.
- [ ] Offline: mọi mục ghi (sửa, ẩn, hiện, đã bán, mở bán lại) bị khóa kèm dòng "Không có kết nối mạng. Dữ liệu chưa thể đồng bộ lên máy chủ, vui lòng thử lại sau".
- [ ] Phương thức của module khác trả `DataCallback.NOT_READY` thì màn hình hiện đúng lỗi đó, không giả dữ liệu.
- [ ] Xoay màn hình giữ nguyên chữ đang sửa trong B04 và menu không lỗi.
- [ ] Trạng thái lấy từ ViewModel: `ShopViewModel.loading` bật trong lúc ghi (khóa nút), `ShopViewModel.error` hiện một lần rồi xóa, `ShopViewModel.done` bật đúng một lần khi một thao tác ghi xong (B04 quay về B05, hiện Snackbar) rồi được đặt lại false.
- [ ] Unit test trong `ShopViewModelTest`: bảng thao tác theo trạng thái (đủ 7 dòng của tiêu chí đầu tiên, gồm HIDDEN tự ẩn so với admin ẩn), bảng cặp trạng thái được phép của `changeStatus`.
- [ ] `./gradlew assembleDebug testDebugUnitTest lintDebug` và `sh workflow/scripts/check-java-style.sh` đều qua.
- [ ] PR liệt kê từng lớp, phương thức, trường công khai mới kèm dòng tài liệu định nghĩa (R16).

### Tài liệu phải đọc
- `AGENTS.md` (R12, R15 đến R23), `workflow/plan/so-huu.md` mục 3, 4, 5, 6.
- `workflow/docs/ui/b-kham-pha-tin-dang/README.md` mục B04 (chế độ sửa) và B05 (menu theo trạng thái, "Khác với ảnh").
- `workflow/docs/product/use-case/uc-b-tin-dang.md` UC11 (luồng 2, 2a, 2b, ngoại lệ 2e, 2f, 3e, 4e, BR1 đến BR3) và UC09 (luồng 3a).
- `workflow/docs/architecture/trang-thai.md` mục 1 (cả bảng và các gạch đầu dòng sau bảng), mục 2 (dòng "Tin rời AVAILABLE") và mục 9 (OFFER_RESULT).
- `workflow/docs/architecture/dac-ta-lop/data.md#listingrepository` (dòng `update`, `setHidden`, `setStatus`, `saveDraft`, `publish`), `#notificationrepository`, `#dealrepository` (dòng `getMyDeals`).
- `workflow/docs/architecture/dac-ta-lop/model.md#listing` (`hide`, `restore`, `markSold`), `#offer` (`reject`), `#appnotification`.
- `workflow/docs/architecture/dac-ta-lop/viewmodel.md#shopviewmodel`.
- `workflow/docs/data/firestore-schema.md#listings`, `#offers`, `#notifications`.
- `workflow/docs/data/security-rules.md` mục 10 (dòng "Sửa nội dung", "Tự ẩn", "Hiện lại", "Đánh dấu đã bán, mở bán lại", "Chặn riêng cho chủ tin"), mục 13 (offers), mục 21 (notifications).
- `workflow/docs/architecture/dac-ta-lop/data.md#mediarepository`; `workflow/docs/product/barem-diem.md` (dòng barem mà issue phục vụ).

### Lớp, trường, trạng thái sẽ dùng
- `MyShopFragment`, `PostListingFragment`; `ShopViewModel.updateListing(Listing listing, List<Uri> imageUris)`, `changeStatus(String id, ListingStatus status)`, `setHidden(String id, boolean hidden)`, `saveDraft(Listing listing, List<Uri> imageUris)`, `publishDraft(String id)` (đã có), các LiveData `myListings` (`LiveData<List<Listing>>`), `loading` (`LiveData<Boolean>`), `error` (`LiveData<String>`), `done` (`LiveData<Boolean>`), mỗi LiveData có getter công khai.
- `MediaRepository.saveImage(Uri uri, ImagePurpose purpose, DataCallback<String> cb)`, `makeThumb(Uri uri, DataCallback<String> cb)`, `getImage(String id, DataCallback<byte[]> cb)`, `delete(String id, DataCallback<Void> cb)` (chỉ gọi); `ImagePurpose.LISTING`.
- `ListingRepository.update(Listing listing, DataCallback<Void> cb)`, `setHidden(String id, boolean hidden, DataCallback<Void> cb)`, `setStatus(String id, ListingStatus from, ListingStatus to, DataCallback<Void> cb)`.
- `Listing.hide(String reason)`, `Listing.restore()`, `Listing.markSold()`; trường `status`, `statusReason`, `flagged`, `conditionValues`, `reservedDealId`, `imageIds`.
- `Offer.reject()`, `OfferStatus` (`PENDING`, `REJECTED`); `NotificationType.OFFER_RESULT`; `NotificationRepository.create(AppNotification notification, WriteBatch batch, DataCallback<Void> cb)` (truyền lô đang dựng, không truyền `null`) và `AppNotification` (`recipientId`, `type`, `title`, `body`, `targetId`).
- `ListingStatus` (`DRAFT`, `AVAILABLE`, `RESERVED`, `SOLD`, `HIDDEN`, `REMOVED`).
- `Deal` và `DealStatus.COMPLETED`: truy vấn giao dịch COMPLETED của người bán ngay trong `ListingRepository.setStatus` (chỉ mục sẵn có `sellerId, status, createdAt` của `deals` trong `firestore-schema.md`).
- `NetworkMonitor.getInstance()`, `isOnline()`, `BaseRepository.currentUserId()`, `logActivity(String action, ReportTarget targetType, String targetId)`.
- Đối số điều hướng `listingId` cho `PostListingFragment`, khai báo trong `nav_listing.xml` của module.

Tên nội bộ do issue này đặt (package-private, ghi vào PR ở mục tên mới): trong `ShopViewModel` các hàm `static List<String> allowedActions(Listing listing)` (kèm hằng `ACTION_EDIT`, `ACTION_HIDE`, `ACTION_SHOW`, `ACTION_MARK_SOLD`, `ACTION_REOPEN`, `ACTION_PUBLISH`, `ACTION_DELETE`, `ACTION_VIEW_REASON`) và `static boolean isAllowedStatusChange(ListingStatus from, ListingStatus to)`.

Chi tiết triển khai, ghi một dòng bằng skill `pubg-record-decision`: B04 ở chế độ sửa lấy `Listing` hiện tại từ danh sách `ShopViewModel.myListings` theo `listingId` (gọi `loadMyListings(SortOrder.NEWEST, "")` nếu danh sách còn rỗng); `ShopViewModel` được lấy theo phạm vi Activity để B04 và B05 dùng cùng một thể hiện; `changeStatus` lấy `from` là `status` hiện có của tin trong danh sách đó.

### File được tạo hoặc sửa
- [sửa] `PUBGApp/app/src/main/java/com/nhom3/pubgapp/ui/listing/MyShopFragment.java`
- [sửa] `PUBGApp/app/src/main/java/com/nhom3/pubgapp/ui/listing/ShopListingAdapter.java`
- [sửa] `PUBGApp/app/src/main/java/com/nhom3/pubgapp/ui/listing/PostListingFragment.java`
- [sửa] `PUBGApp/app/src/main/java/com/nhom3/pubgapp/ui/listing/PostImageAdapter.java` (hiện cả ảnh đã lưu, đọc bằng `MediaRepository.getImage`, lẫn ảnh mới chọn)
- [sửa] `PUBGApp/app/src/main/res/layout/fragment_listing_post.xml`
- [sửa] `PUBGApp/app/src/main/res/layout/item_listing_shop.xml`
- [sửa] `PUBGApp/app/src/main/res/layout/item_listing_post_image.xml` (chỉ khi ô ảnh đã lưu cần thêm phần tử)
- [sửa] `PUBGApp/app/src/main/res/menu/menu_listing_shop_item.xml`
- [sửa] `PUBGApp/app/src/main/res/navigation/nav_listing.xml` (thêm đối số `listingId` cho `PostListingFragment`)
- [sửa] `PUBGApp/app/src/main/res/values/strings_listing.xml`
- [sửa] `PUBGApp/app/src/main/java/com/nhom3/pubgapp/viewmodel/ShopViewModel.java` (chỉ thân `updateListing`, `changeStatus`, `setHidden`, việc đẩy dữ liệu vào `myListings`, `loading`, `error`, `done`, và các hàm package-private ở trên)
- [sửa] `PUBGApp/app/src/main/java/com/nhom3/pubgapp/data/ListingRepository.java` (chỉ thân `update`, `setHidden`, `setStatus`)
- [sửa] `PUBGApp/app/src/test/java/com/nhom3/pubgapp/viewmodel/ShopViewModelTest.java`

### Các bước đề nghị
Người làm có thể đổi thứ tự nhưng không được đổi tên nào ở trên.
1. Trong `ShopViewModel` viết `static List<String> allowedActions(Listing listing)` theo bảng trong tiêu chí nghiệm thu (xét `status` và, với HIDDEN, `statusReason` rỗng hay không), và `static boolean isAllowedStatusChange(ListingStatus from, ListingStatus to)` (chỉ `AVAILABLE` sang `SOLD` và `SOLD` sang `AVAILABLE` là true). Viết unit test cho cả hai trong `ShopViewModelTest`, chạy `./gradlew testDebugUnitTest`.
2. Làm thân `ListingRepository.setHidden(String id, boolean hidden, DataCallback<Void> cb)`: đọc tin, chỉ chủ tin; `hidden` true thì từ AVAILABLE sang HIDDEN với `statusReason` rỗng (Rules dòng "Tự ẩn"), `hidden` false thì từ HIDDEN sang AVAILABLE chỉ khi `statusReason` rỗng (dòng "Hiện lại"), ngược lại `onError` bằng câu tiếng Việt nói tin do admin ẩn. Khi ẩn: tìm các offer PENDING của tin (collection `offers`, `listingId ==` và `status ==`), đặt REJECTED và ghi thông báo OFFER_RESULT cho từng `buyerId` bằng `NotificationRepository.create(AppNotification notification, WriteBatch batch, DataCallback<Void> cb)` truyền lô đang dựng (`targetId` là `conversationId` của offer, `trang-thai.md` mục 9), rồi ghi cả lô một lần.
3. Làm thân `ListingRepository.setStatus(String id, ListingStatus from, ListingStatus to, DataCallback<Void> cb)`: tự kiểm cặp `(from, to)` trong repository (chỉ `AVAILABLE` sang `SOLD` và `SOLD` sang `AVAILABLE` được phép, cặp khác thì `onError` bằng câu tiếng Việt; hàm `isAllowedStatusChange` ở `ShopViewModel` chỉ phục vụ giao diện); `from` AVAILABLE sang SOLD thì từ chối offer PENDING cùng lô như bước 2; `from` SOLD sang AVAILABLE thì truy vấn giao dịch COMPLETED của người bán và từ chối nếu có giao dịch của đúng tin này (xem câu hỏi 1).
4. Làm thân `ListingRepository.update(Listing listing, DataCallback<Void> cb)`: chỉ chủ tin và chỉ khi tin ở AVAILABLE, hoặc HIDDEN với `statusReason` rỗng (DRAFT đi qua `saveDraft`); chỉ ghi các khóa của dòng Rules "Sửa nội dung"; gọi `Listing.buildSearchKeywords()`; tính lại `flagged` bằng danh sách từ khóa cấm (`getBannedKeywords`) và chỉ cho false sang true; `logActivity(String action, ReportTarget targetType, String targetId)` nếu cần theo `trang-thai.md`.
5. Làm thân `ShopViewModel.updateListing(Listing listing, List<Uri> imageUris)`: `imageUris` là các ảnh mới chọn thêm, ảnh đã lưu nằm trong `listing.imageIds`; ghi từng ảnh mới bằng `MediaRepository.saveImage(uri, ImagePurpose.LISTING, cb)`, ghép mã mới vào `imageIds` (tối đa 5), gọi `makeThumb` để đặt lại `coverThumb` khi ảnh đầu là ảnh mới, gọi `ListingRepository.update`, rồi `MediaRepository.delete` các ảnh người bán đã bỏ (chỉ sau khi `update` thành công; lỗi giữa chừng thì xóa các ảnh mới vừa ghi). Làm thân `changeStatus(String id, ListingStatus status)` (lấy `from` từ danh sách hiện có) và `setHidden(String id, boolean hidden)`. Cả ba gọi repository ngoài luồng chính bằng lớp ẩn danh `DataCallback`, bật `loading` khi bắt đầu và tắt khi xong, lỗi vào `error`, xong thì bật `done` rồi nạp lại `myListings`.
6. Mở rộng menu ở `MyShopFragment` và `item_listing_shop.xml` bằng `allowedActions`; mục "Xem lý do" hiện hộp thoại chứa `statusReason`; "Ẩn tin" và "Đánh dấu đã bán" hỏi xác nhận một câu; khóa mọi mục ghi khi `NetworkMonitor.getInstance().isOnline()` là false.
7. Khai báo đối số `listingId` (kiểu chuỗi, mặc định rỗng) cho `PostListingFragment` trong `nav_listing.xml` và lối đi từ `MyShopFragment` sang `PostListingFragment` mang `listingId`.
8. Làm chế độ sửa trong `PostListingFragment`: khi `listingId` khác rỗng, lấy `Listing` từ `ShopViewModel.myListings`, điền sẵn mọi ô (kể cả thuộc tính theo danh mục), đổi nút chính thành "Lưu thay đổi"; tin nháp có thêm nút "Đăng tin". "Lưu thay đổi" gọi `updateListing` (AVAILABLE, HIDDEN) hoặc `saveDraft` (DRAFT); "Đăng tin" ở tin nháp gọi `saveDraft` rồi `publishDraft`, và chặn bằng cùng hộp thoại xác thực của P2-B1a (nút "Đăng ký xác thực" gọi `action_global_my_profile`). `PostImageAdapter` hiện ảnh đã lưu (đọc bằng `MediaRepository.getImage`) cùng ảnh mới chọn, cho bỏ từng ảnh, và `updateListing` nhận các ảnh mới. Chế độ sửa dùng lại cùng hàm kiểm `findMissingFields` của P2-B1a.
9. Thêm chuỗi vào `strings_listing.xml` (khóa theo mẫu `<module>_<màn>_<việc>`, ví dụ `listing_shop_action_hide` và `listing_post_edit_button_save`).
10. Chạy các lệnh ở mục "Tự kiểm trước khi mở Pull Request" và thử từng thao tác với một offer PENDING thật.

### File dùng chung cần thêm dòng
Không có file chung. `nav_listing.xml` là đồ thị riêng của module B.

### Cấm đụng
- File chỉ Phát sửa: `AndroidManifest.xml`, `nav_main.xml`, `nav_admin.xml`, menu thanh điều hướng chính, `themes.xml`, `colors.xml`, `dimens.xml`, `strings_common.xml`, `AppDatabase`, `Converters`, mọi file Gradle, mọi file trong `firebase/`.
- Lớp `model` và `model.enums`: không sửa.
- `NotificationRepository.create` (Phát), `MediaRepository` (Phát) và `DealRepository` (Hải Long, Phát): chỉ gọi, không sửa. `AdminRepository.hideListing`, `restoreListing` (Phát): không gọi từ màn người bán.
- Hủy giao dịch trả tin về AVAILABLE: module C.
- Các phương thức khác của `ListingRepository` và `ShopViewModel`: thuộc P2-B1a, P2-B1b, P2-B2a.

### Phụ thuộc
P2-B2a, P2-B1b. Nút "Chỉnh sửa" và "Đã bán" trên B03 cho tin của chính mình là việc của P2-B6a và gọi lại chế độ sửa của issue này.

### Ngoài phạm vi
- Nhãn "CÓ ĐỀ NGHỊ" (câu hỏi 1 của P2-B2a).
- Thao tác admin ẩn, khôi phục, gỡ tin: P2-E3.
- Ẩn thanh dưới khi B04 ở chế độ sửa: việc của `MainActivity` (câu hỏi 4).
- Bố cục ngang: P3-Ba.

### Tài liệu cần cập nhật
Không có, trừ khi các câu hỏi dưới đây đổi hành vi (khi đó sửa `viewmodel.md` theo R7).

### Tự kiểm trước khi mở Pull Request
- [ ] `cd PUBGApp && ./gradlew testDebugUnitTest` chạy `ShopViewModelTest`: bảng thao tác theo trạng thái và bảng cặp trạng thái đều qua.
- [ ] Bằng một tài khoản bán, thử từng thao tác theo menu: "Ẩn tin" (Console đổi `status` sang HIDDEN, `statusReason` rỗng), "Hiện lại" (về AVAILABLE), "Đánh dấu đã bán" (SOLD), "Mở bán lại" (về AVAILABLE).
- [ ] Tạo một offer PENDING trên tin (Console hoặc tài khoản thứ hai) rồi "Ẩn tin": offer chuyển REJECTED, có thông báo OFFER_RESULT cho người mua.
- [ ] Đặt trên Console một tin HIDDEN có `statusReason` khác rỗng: menu chỉ có "Xem lý do", không có "Hiện lại".
- [ ] "Chỉnh sửa" một tin đang bán: ô được điền sẵn; đổi giá rồi lưu: Console cập nhật `price` và `searchKeywords`; thử giá 0 với "Bán": bị chặn.
- [ ] "Chỉnh sửa" một tin nháp, bấm "Đăng tin" bằng tài khoản APPROVED: tin sang AVAILABLE.
- [ ] Bật chế độ máy bay: mọi mục ghi khóa kèm dòng giải thích.
- [ ] Xoay màn hình ở B04 khi đang sửa: không mất chữ đang sửa.
- [ ] Sửa ảnh: bỏ một ảnh đã lưu và thêm một ảnh mới rồi lưu: `imageIds` đổi đúng, Console `images` mất tài liệu ảnh đã bỏ và có tài liệu ảnh mới.
- [ ] `cd PUBGApp && ./gradlew assembleDebug testDebugUnitTest lintDebug` qua.
- [ ] `sh workflow/scripts/check-java-style.sh` không báo lỗi.
- [ ] Danh sách mọi lớp, phương thức, trường công khai mới kèm dòng tài liệu định nghĩa đã dán vào PR; tên nội bộ được ghi riêng (R16).

### Quy mô
- [x] Đây là một lát cắt dọc của một module, khoảng nửa ngày đến một ngày, một Pull Request.

### Câu hỏi cho leader
1. Menu B05 muốn ẩn "Mở bán lại" với tin SOLD do giao dịch hoàn tất, nhưng không có trường hay phương thức công khai nào cho màn hình biết điều đó (`reservedDealId` theo Rules vẫn còn sau khi hoàn tất nhưng `firestore-schema.md` ghi "rỗng ở trạng thái khác"). Cách hiểu mặc định: hiện "Mở bán lại" cho mọi tin SOLD, `setStatus` kiểm và từ chối bằng câu tiếng Việt. Xin xác nhận.
2. `updateListing(Listing listing, List<Uri> imageUris)` nhận ảnh mới, nhưng `MediaRepository.makeThumb` cần `Uri`, không tạo lại `coverThumb` từ ảnh đã lưu. Khi người bán bỏ ảnh đầu và ảnh đứng đầu mới là một ảnh đã lưu, `coverThumb` lấy từ đâu? Cách hiểu mặc định: giữ `coverThumb` cũ khi ảnh đầu không phải ảnh mới, và chỉ tạo lại khi ảnh đầu là ảnh vừa chọn. Xin xác nhận, hoặc cho một cách tạo thu nhỏ từ ảnh đã lưu.
3. `ShopViewModel` không có LiveData cho một tin đơn lẻ nên B04 ở chế độ sửa phải tìm tin trong `myListings`; mở từ B03 (P2-B6a) thì `myListings` có thể chưa nạp. Cách hiểu mặc định ở "Chi tiết triển khai" ở trên. Xin xác nhận hoặc thêm một thuộc tính.
4. B04 vừa là tab gốc (hiện thanh dưới) vừa là màn sửa (ẩn thanh dưới, có nút quay lại) trong cùng một lớp. Việc ẩn thanh dưới do `MainActivity` (Phát) quyết định, nên cần cách phân biệt, ví dụ một đích điều hướng riêng cho chế độ sửa trong `nav_listing.xml` dùng lại `PostListingFragment`. Xin chỉ cách làm.
