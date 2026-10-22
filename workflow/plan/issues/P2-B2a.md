TITLE: feat(listing): kệ hàng của tôi với tab, sắp xếp và tin nháp
LABELS: type:feature, area:listing, module:b, priority:p1
ASSIGNEES: tranannhtu21012006
PHASE: P2

Plan item: P2-B2a

### Bối cảnh
1. PUBGApp (Pre-owned Users' Bargain Grounds) là ứng dụng Android mua bán, trao đổi, cho tặng đồ cũ, viết bằng Java 17, XML Views và ViewBinding, dùng Firebase (Authentication, Firestore gói Spark) và Room.
2. Module B (Tin đăng và khám phá, người làm: Trần Anh Tú) lo đăng tin, kệ hàng, trang chủ, tìm kiếm, chi tiết tin và tin đã lưu.
3. Issue này làm màn B05 `MyShopFragment` (mở từ D01): danh sách tin của tôi chia 4 tab, sắp xếp, lọc danh mục, đăng bản nháp và xóa tin nháp (UC09, UC11).
4. Đọc trước: `workflow/docs/ui/b-kham-pha-tin-dang/README.md#b05-kệ-hàng-của-tôi`, `workflow/docs/product/use-case/uc-b-tin-dang.md#uc09-quản-lý-tin-nháp` cùng `#uc11-quản-lý-kệ-hàng`, `workflow/docs/architecture/trang-thai.md` mục 1.
5. Đã có trên `main`: khung `MyShopFragment` và `fragment_listing_my_shop.xml`, mọi lớp đủ chữ ký (thân chưa làm trả `DataCallback.NOT_READY`), `ShopViewModel.createListing` và `saveDraft` (từ P2-B1a, P2-B1b) để có tin nháp mà thử, mọi ViewModel đã có `loading`, `error`, `done` kèm getter công khai (`ShopViewModel` còn có `categories` và `loadCategories()`), các hành động toàn cục `action_global_my_profile` và `action_global_listing_detail` trong `nav_main.xml`, thành phần chung `view_status_chip.xml`, `view_empty_state.xml`, `view_error_state.xml`, `view_offline_banner.xml`, `view_detail_toolbar.xml` (giao-dien.md mục 5, P1).

### Mã tính năng
F-04

### Mã việc kế hoạch
P2-B2a. Nửa đầu của P2-B2 (kệ hàng của tôi). Nửa sau là P2-B2b (sửa, ẩn hoặc hiện, đánh dấu đã bán, mở bán lại). Tách vì P2-B2 gồm 6 phương thức repository, chế độ sửa của B04 và menu theo 7 trạng thái, quá một ngày.

### Module
B. Tin đăng và khám phá (Trần Anh Tú)

### STT trong bảng đặc tả
13 (phần xem danh sách và xóa tin nháp), 14 (sắp xếp), 22 (tab Nháp, đăng bản nháp). Cả ba có Phạm vi "Đợt này". STT 73 chỉ là điều kiện chặn mục "Đăng" của tin nháp.

### Màn hình mockup
B05 (ảnh `workflow/docs/ui/b-kham-pha-tin-dang/b05-ke-hang-cua-toi.png`). Ảnh chỉ có 3 tab và nhãn "TẠM ẨN"; đúng là 4 tab (thêm "Đã ẩn") và nhãn "ĐÃ ẨN" (mục "Khác với ảnh" của B05). Menu "Chỉnh sửa", "Ẩn tin", "Hiện lại", "Đánh dấu đã bán", "Mở bán lại" làm ở P2-B2b.

### Làm gì và vì sao
Người bán mở "Kệ hàng của tôi" từ D01 (hành động toàn cục `action_global_my_shop`) và thấy mọi tin của mình chia thành 4 tab: Đang hoạt động (AVAILABLE và RESERVED), Đã bán (SOLD), Đã ẩn (HIDDEN và REMOVED), Nháp (DRAFT, kèm số lượng tin nháp). Mỗi thẻ có ảnh bìa thu nhỏ, tên, giá và nhãn trạng thái đúng tên chuẩn của `trang-thai.md`: ĐANG BÁN, ĐANG GIỮ CHỖ, ĐÃ BÁN, ĐÃ ẨN, ĐÃ GỠ, NHÁP. Người bán sắp xếp, lọc theo danh mục, bấm thẻ để mở B03. Với tin nháp, menu ba chấm có "Đăng" (DRAFT sang AVAILABLE qua `Listing.publish` và `ListingRepository.publish`) và "Xóa tin" (xóa hẳn, chỉ tin DRAFT; tin đã đăng chỉ có thể ẩn). Tài khoản chưa được admin duyệt xác thực vẫn xem được toàn bộ B05 nhưng thấy dải báo ở đầu màn hình và bị chặn ở mục "Đăng" (UC17 luồng 1a, ngoại lệ 1e). Issue này phục vụ dòng barem R-Chapter 4 (RecyclerView hiển thị danh sách thẻ ngang kèm `TabLayout` 4 tab; menu overflow trên app bar của B05 cho sắp xếp và lọc danh mục, và `PopupMenu` ba chấm trên từng thẻ; điều hướng bằng Navigation component với hành động toàn cục `action_global_listing_detail`), dòng R-Chapter 7 (async: mọi lệnh đọc, ghi Firestore qua `DataCallback`) và dòng R-Chapter 3 (unit test JUnit cho `filterByTab` và `sortAndFilter` trong `ShopViewModelTest`).

### Tiêu chí nghiệm thu
- [ ] Luồng chính chạy trên máy ảo với Firebase thật: B05 đọc `listings` có `sellerId` là người đang đăng nhập bằng một truy vấn của chính chủ (thấy mọi trạng thái), mỗi tin nằm đúng một tab: Đang hoạt động (AVAILABLE, RESERVED), Đã bán (SOLD), Đã ẩn (HIDDEN, REMOVED), Nháp (DRAFT). PR ghi rõ đã kiểm cả bốn tab.
- [ ] Nhãn trạng thái đúng và dùng `view_status_chip.xml` với màu theo `giao-dien.md` mục 4: Đang bán, Đang giữ chỗ, Đã bán, Đã ẩn (cho cả tin tự ẩn lẫn tin admin ẩn, không có nhãn "TẠM ẨN" hay "ADMIN ẨN"), Đã gỡ, Nháp.
- [ ] Tab Nháp hiện số tin nháp hiện có. Sắp xếp và lọc danh mục nằm trong menu overflow của thanh trên (danh sách danh mục lấy từ `ShopViewModel.categories`, nạp bằng `loadCategories()`). Đổi cách sắp xếp hoặc chọn danh mục thì danh sách đổi ngay, không gọi lại mạng; chọn danh mục thì chỉ còn tin của danh mục đó.
- [ ] Tab không có tin: hiện ô trống (`view_empty_state.xml`) "Bạn chưa có tin đăng nào ở mục này"; tab Nháp rỗng hiện "Bạn chưa có bản nháp nào. Tin đăng lưu dở sẽ xuất hiện tại đây".
- [ ] "Đăng" một tin nháp đủ thông tin bằng tài khoản APPROVED: tài liệu `listings` đổi `status` từ DRAFT sang AVAILABLE, có `searchKeywords`, `flagged` đúng; tin chuyển từ tab Nháp sang tab Đang hoạt động, hiện "Đăng tin thành công!".
- [ ] "Đăng" một tin nháp thiếu thông tin: hiện "Tin đăng cần có tối thiểu tiêu đề, danh mục, 1 hình ảnh và một hình thức giao dịch để có thể công khai", không ghi gì lên Firebase.
- [ ] Tài khoản chưa APPROVED: đầu màn hình có dải "Tài khoản của bạn chưa được xác thực nên chưa thể đăng tin" kèm nút "Đăng ký xác thực" (sang D01 bằng `action_global_my_profile`); bấm "Đăng" ở tin nháp hiện hộp thoại nêu lý do (PENDING thì nói đang chờ admin duyệt); các tab và danh sách vẫn xem được.
- [ ] "Xóa tin" chỉ có ở tin nháp, hỏi "Bạn có chắc chắn muốn xóa bản nháp này không?", xóa hẳn tài liệu DRAFT rồi hiện "Đã xóa bản nháp". Tin ở trạng thái khác không có mục xóa; nếu repository bị gọi xóa tin không phải nháp thì trả "Chỉ tin nháp mới xóa được. Tin đã đăng chỉ có thể ẩn đi, hãy chọn Ẩn tin".
- [ ] Đủ bốn trạng thái: đang tải (khi `ShopViewModel.loading` bật), có dữ liệu, rỗng, lỗi (`view_error_state.xml` kèm "Thử lại", nội dung lấy từ `ShopViewModel.error`, hiện một lần rồi xóa). Khi `ShopViewModel.done` bật sau đăng bản nháp hoặc xóa nháp thì hiện Snackbar tương ứng rồi đặt lại false.
- [ ] Phương thức của module khác trả `DataCallback.NOT_READY` thì màn hình hiện đúng lỗi đó, không giả dữ liệu.
- [ ] Offline: danh sách đã tải vẫn hiện kèm `view_offline_banner.xml`; mục "Đăng" và "Xóa tin" bị khóa kèm dòng giải thích.
- [ ] Xoay màn hình giữ tab đang chọn, cách sắp xếp, bộ lọc danh mục và vị trí cuộn; không tải lại.
- [ ] Việc chia tab, sắp xếp, lọc nằm trong các phương thức package-private `static` của `ShopViewModel`, có unit test trong `ShopViewModelTest`: tin ở đúng tab của mình, sắp `SortOrder.NEWEST` theo `createdAt` giảm dần, lọc theo `categoryId`, `categoryId` rỗng là mọi danh mục.
- [ ] `./gradlew assembleDebug testDebugUnitTest lintDebug` và `sh workflow/scripts/check-java-style.sh` đều qua.
- [ ] PR liệt kê từng lớp, phương thức, trường công khai mới kèm dòng tài liệu định nghĩa (R16).

### Tài liệu phải đọc
- `AGENTS.md` (R12, R15 đến R23), `workflow/plan/so-huu.md` mục 3, 4, 5, 6.
- `workflow/docs/ui/b-kham-pha-tin-dang/README.md` mục B05 (đặc biệt "Trạng thái đặc biệt" và "Khác với ảnh").
- `workflow/docs/product/use-case/uc-b-tin-dang.md` UC09, UC11 (BR1 đến BR3) và UC17 (luồng 1a, ngoại lệ 1e).
- `workflow/docs/architecture/trang-thai.md` mục 1 (dòng "Đăng bản nháp" và các gạch đầu dòng sau bảng) và mục 8.
- `workflow/docs/architecture/dac-ta-lop/data.md#listingrepository` (dòng `getBySeller`, `publish`, `delete`), `#categoryrepository`.
- `workflow/docs/architecture/dac-ta-lop/model.md#listing` (`publish`, `isDraft`), `#user`.
- `workflow/docs/architecture/dac-ta-lop/viewmodel.md#shopviewmodel`, `#profileviewmodel`.
- `workflow/docs/architecture/dac-ta-lop/enums.md#sortorder`, `#listingstatus`, `#verificationstatus`.
- `workflow/docs/data/firestore-schema.md#listings` (bảng truy vấn: `getBySeller` chính chủ).
- `workflow/docs/data/security-rules.md` mục 10 (dòng "Đăng bản nháp", "Xóa", "Chặn riêng cho chủ tin").
- `workflow/docs/conventions/giao-dien.md` mục 4 (màu theo trạng thái), mục 5, mục 6 và `.agents/skills/pubg-ui/SKILL.md`.
- `workflow/memory/code/firebase.md` (bẫy "Security rules are not filters").
- `workflow/docs/product/barem-diem.md` (dòng barem mà issue phục vụ).

### Lớp, trường, trạng thái sẽ dùng
- `MyShopFragment`; `ShopViewModel.loadMyListings(SortOrder order, String categoryId)`, `loadCategories()`, `publishDraft(String id)`, `deleteListing(String id)`, các LiveData `myListings` (`LiveData<List<Listing>>`), `categories` (`LiveData<List<Category>>`), `loading` (`LiveData<Boolean>`), `error` (`LiveData<String>`), `done` (`LiveData<Boolean>`), mỗi LiveData có getter công khai.
- `ListingRepository.getBySeller(String sellerId, DataCallback<List<Listing>> cb)`, `publish(Listing listing, DataCallback<String> cb)`, `delete(String id, DataCallback<Void> cb)`.
- `Listing.publish()`, `Listing.isDraft()`; trường `status`, `statusReason`, `categoryId`, `coverThumb`, `title`, `price`, `createdAt`.
- `ListingStatus` (`DRAFT`, `AVAILABLE`, `RESERVED`, `SOLD`, `HIDDEN`, `REMOVED`); `SortOrder.NEWEST`.
- `ProfileViewModel.profile` (`LiveData<User>`) cùng `loadProfile(String uid)` (module D, chỉ quan sát); `User.verificationStatus`; `VerificationStatus` (`NONE`, `PENDING`, `APPROVED`, `REJECTED`).
- `CategoryRepository.getAll(DataCallback<List<Category>> cb)` (Phát, chỉ gọi, qua `ShopViewModel`).
- `SessionManager.getInstance()`, `getCurrentUserId()`; `NetworkMonitor.getInstance()`, `isOnline()`; `BaseRepository.currentUserId()`, `logActivity(String action, ReportTarget targetType, String targetId)`; `ReportTarget.LISTING`.
- Hành động toàn cục `action_global_listing_detail` (tham số `listingId`), `action_global_my_profile` (nút "Đăng ký xác thực").

Tên nội bộ do issue này đặt (package-private, ghi vào PR ở mục tên mới): trong `ShopViewModel` các hàm `static List<Listing> filterByTab(List<Listing> listings, int tab)` (với hằng `TAB_ACTIVE`, `TAB_SOLD`, `TAB_HIDDEN`, `TAB_DRAFT`) và `static List<Listing> sortAndFilter(List<Listing> listings, SortOrder order, String categoryId)`; lớp giao diện `ShopListingAdapter`; layout `item_listing_shop.xml`; menu `menu_listing_shop.xml` (overflow của thanh trên) và `menu_listing_shop_item.xml` (ba chấm của thẻ).

Chi tiết triển khai, ghi một dòng bằng skill `pubg-record-decision`: `ShopViewModel` giữ danh sách đầy đủ của người bán trong một trường private, mỗi lần gọi `loadMyListings` chỉ sắp và lọc lại nó khi đã có; `categoryId` rỗng nghĩa là mọi danh mục; chọn tab là trạng thái của Fragment (giữ bằng `onSaveInstanceState`). Đăng bản nháp từ B05 thì ở lại B05 (theo UC09 bước 5). Sắp xếp và lọc là menu overflow trên thanh trên (mục "Sắp xếp" chỉ có "Mới đăng" ứng với `SortOrder.NEWEST`, mục "Lọc theo danh mục" mở hộp chọn); thao tác trên thẻ là `PopupMenu` ba chấm.

### File được tạo hoặc sửa
- [sửa] `PUBGApp/app/src/main/java/com/nhom3/pubgapp/ui/listing/MyShopFragment.java`
- [tạo] `PUBGApp/app/src/main/java/com/nhom3/pubgapp/ui/listing/ShopListingAdapter.java`
- [sửa] `PUBGApp/app/src/main/res/layout/fragment_listing_my_shop.xml`
- [tạo] `PUBGApp/app/src/main/res/layout/item_listing_shop.xml`
- [tạo] `PUBGApp/app/src/main/res/menu/menu_listing_shop.xml` (menu overflow của thanh trên: sắp xếp, lọc danh mục)
- [tạo] `PUBGApp/app/src/main/res/menu/menu_listing_shop_item.xml` (menu ba chấm; bản này chỉ có mục của tin nháp, P2-B2b thêm các mục còn lại)
- [sửa] `PUBGApp/app/src/main/res/values/strings_listing.xml`
- [sửa] `PUBGApp/app/src/main/java/com/nhom3/pubgapp/viewmodel/ShopViewModel.java` (chỉ thân `loadMyListings`, `loadCategories`, `publishDraft`, `deleteListing`, việc đẩy dữ liệu vào `myListings`, `categories`, `loading`, `error`, `done`, và các hàm package-private ở trên)
- [sửa] `PUBGApp/app/src/main/java/com/nhom3/pubgapp/data/ListingRepository.java` (chỉ thân `getBySeller`, `publish`, `delete`)
- [sửa] `PUBGApp/app/src/test/java/com/nhom3/pubgapp/viewmodel/ShopViewModelTest.java`

### Các bước đề nghị
Người làm có thể đổi thứ tự nhưng không được đổi tên nào ở trên.
1. Trong `ShopViewModel` viết hai hàm package-private: `static List<Listing> filterByTab(List<Listing> listings, int tab)` (AVAILABLE và RESERVED cho `TAB_ACTIVE`, SOLD cho `TAB_SOLD`, HIDDEN và REMOVED cho `TAB_HIDDEN`, DRAFT cho `TAB_DRAFT`) và `static List<Listing> sortAndFilter(List<Listing> listings, SortOrder order, String categoryId)` (lọc theo `categoryId` khi không rỗng; `NEWEST` sắp `createdAt` giảm dần).
2. Thêm các ca vào `ShopViewModelTest` cho hai hàm trên rồi chạy `./gradlew testDebugUnitTest`.
3. Làm thân `ListingRepository.getBySeller(String sellerId, DataCallback<List<Listing>> cb)`: khi `sellerId` bằng `currentUserId()` thì truy vấn `sellerId ==`, sắp `createdAt` giảm (chính chủ thấy mọi trạng thái); người khác thì thêm `status in [AVAILABLE, RESERVED, SOLD]` (firestore-schema.md, bảng truy vấn của `listings`; module D dùng nhánh này ở D02).
4. Làm thân `ListingRepository.publish(Listing listing, DataCallback<String> cb)`: chỉ nhận tin DRAFT của chính người gọi; chạy đúng các bước của `create` (kiểm `User.canSell`, đặt `flagged`, `Listing.buildSearchKeywords()`), rồi gọi `Listing.publish()` và ghi `status` AVAILABLE (Rules dòng "Đăng bản nháp"); ghi `logActivity("LISTING_CREATED", ReportTarget.LISTING, id)`; thiếu thông tin thì `onError` với câu ở tiêu chí nghiệm thu.
5. Làm thân `ListingRepository.delete(String id, DataCallback<Void> cb)`: đọc tin, nếu `status` không phải DRAFT thì `onError` "Chỉ tin nháp mới xóa được. Tin đã đăng chỉ có thể ẩn đi, hãy chọn Ẩn tin"; ngược lại xóa tài liệu (Rules dòng "Xóa").
6. Làm thân `ShopViewModel.loadMyListings(SortOrder order, String categoryId)` (lấy `sellerId` từ `SessionManager.getCurrentUserId()`, gọi `getBySeller` một lần rồi sắp, lọc bằng `sortAndFilter`), `publishDraft(String id)` (tìm tin trong danh sách đầy đủ theo `id`, gọi `ListingRepository.publish`, rồi nạp lại danh sách), `deleteListing(String id)` (gọi `ListingRepository.delete`, rồi nạp lại). Bật `loading` khi bắt đầu và tắt khi xong; kết quả đưa vào `myListings`, lỗi vào `error`, ghi xong thì bật `done`. `loadCategories()` gọi `CategoryRepository.getAll` và đưa vào `categories`. Dùng lớp ẩn danh `DataCallback`, không lambda.
7. Dựng `fragment_listing_my_shop.xml`: `view_detail_toolbar.xml` (nút quay lại về D01, tiêu đề "Kệ hàng của tôi", menu overflow `menu_listing_shop.xml` cho sắp xếp và lọc danh mục), dải báo xác thực, `TabLayout` 4 tab, `RecyclerView`, `view_empty_state.xml`, `view_error_state.xml`, `view_offline_banner.xml`, thanh tiến trình. Màu, khoảng cách, cỡ chữ lấy từ token; id view theo lowerCamelCase `<loại><Việc>` (ví dụ `recyclerListings`, `tabLayoutStatus`).
8. Làm `ShopListingAdapter` và `item_listing_shop.xml` (thẻ ngang: ảnh bìa giải mã từ `coverThumb` ngoài luồng chính, tên, giá bằng `FormatUtils.formatPrice(long)`, `view_status_chip.xml`, nút ba chấm mở `PopupMenu` với `menu_listing_shop_item.xml`). Menu ba chấm của tin nháp: "Đăng", "Xóa tin".
9. Làm `MyShopFragment`: quan sát `myListings`, chuyển tab, hộp thoại xác nhận xóa, gọi `ShopViewModel.loadCategories()`, nhận menu overflow của thanh trên, hiện dải báo xác thực theo `ProfileViewModel.profile` (gọi `loadProfile(SessionManager.getInstance().getCurrentUserId())`), nút "Đăng ký xác thực" gọi `action_global_my_profile`, chặn "Đăng" bằng một `AlertDialog` do phương thức private của Fragment dựng khi chưa APPROVED (PENDING thì nói đang chờ), khóa các mục ghi khi `NetworkMonitor.getInstance().isOnline()` là false, bấm thẻ thì `action_global_listing_detail` với `listingId`.
10. Thêm chuỗi vào `strings_listing.xml` (khóa theo mẫu `<module>_<màn>_<việc>`, ví dụ `listing_shop_tab_draft`); ghi các nhãn trạng thái đúng như trong tiêu chí nghiệm thu.
11. Chạy các lệnh ở mục "Tự kiểm trước khi mở Pull Request", thử bằng một tài khoản APPROVED và một tài khoản chưa APPROVED.

### File dùng chung cần thêm dòng
Không có. `nav_listing.xml` là file của chính module nhưng issue này không cần sửa.

### Cấm đụng
- File chỉ Phát sửa: `AndroidManifest.xml`, `nav_main.xml`, `nav_admin.xml`, menu thanh điều hướng, `themes.xml`, `colors.xml`, `dimens.xml`, `strings_common.xml`, `AppDatabase`, `Converters`, mọi file Gradle, mọi file trong `firebase/` (nếu truy vấn báo thiếu chỉ mục thì ghi đường dẫn tạo chỉ mục vào mô tả PR).
- Lớp `model` và `model.enums`: không sửa.
- `CategoryRepository` (Phát), `FormatUtils` (Phát): chỉ gọi.
- `ProfileViewModel.profile`, `loadProfile` (Phúc): chỉ quan sát.
- D01 (`MyProfileFragment`, Phúc) mở B05: chỉ dùng hành động toàn cục đã có, không sửa D01.
- Các phương thức khác của `ListingRepository` và `ShopViewModel`: thuộc P2-B1a, P2-B1b, P2-B2b.

### Phụ thuộc
P2-B1a, P2-B1b (cần có tin nháp để thử). Gọi được `getBySeller` ngay khi `main` có P1-04. Dùng, không chờ: P2-D1 (`loadProfile`).

### Ngoài phạm vi
- Menu "Chỉnh sửa", "Ẩn tin", "Hiện lại", "Đánh dấu đã bán", "Mở bán lại" và chế độ sửa của B04: P2-B2b.
- Nhãn phụ "CÓ ĐỀ NGHỊ" trên thẻ: chưa làm, xem câu hỏi 1.
- Sắp xếp "cũ nhất": chờ câu hỏi 2.
- Tin REMOVED và tin do admin ẩn chỉ để xem; không có thao tác nào cho người bán.
- Bố cục ngang: P3-Ba. Xem kệ hàng khi mất mạng đầy đủ: P3-Bb.

### Tài liệu cần cập nhật
Không có, trừ khi các câu hỏi dưới đây đổi hành vi (khi đó cập nhật `viewmodel.md`, `enums.md`, `so-huu.md` theo R7).

### Tự kiểm trước khi mở Pull Request
- [ ] `cd PUBGApp && ./gradlew testDebugUnitTest` chạy `ShopViewModelTest`: ca chia tab, sắp xếp, lọc danh mục đều qua.
- [ ] Chuẩn bị 6 tin của một người bán (đủ 6 trạng thái, đặt trạng thái trên Console nếu cần), mở B05 từ D01: mỗi tin ở đúng tab, nhãn đúng tên, tab Nháp hiện số tin nháp.
- [ ] Đăng một tin nháp đủ thông tin bằng tài khoản APPROVED: Console đổi `status` sang AVAILABLE, tin sang tab Đang hoạt động.
- [ ] Đăng một tin nháp thiếu ảnh: hiện lời báo, Console không đổi.
- [ ] Bằng tài khoản chưa APPROVED: dải báo hiện ở đầu, "Đăng" ra hộp thoại, các tab vẫn xem được.
- [ ] Xóa một tin nháp: Console không còn tài liệu đó; tin đã đăng không có mục "Xóa tin".
- [ ] Bật chế độ máy bay sau khi đã tải: danh sách vẫn hiện, có dải báo offline, mục "Đăng" và "Xóa tin" khóa.
- [ ] Xoay màn hình ở tab Nháp đã cuộn xuống: giữ tab, giữ vị trí cuộn, không tải lại.
- [ ] `cd PUBGApp && ./gradlew assembleDebug testDebugUnitTest lintDebug` qua.
- [ ] `sh workflow/scripts/check-java-style.sh` không báo lỗi.
- [ ] Danh sách mọi lớp, phương thức, trường công khai mới kèm dòng tài liệu định nghĩa đã dán vào PR; tên nội bộ được ghi riêng (R16).

### Quy mô
- [x] Đây là một lát cắt dọc của một module, khoảng nửa ngày đến một ngày, một Pull Request.

### Câu hỏi cho leader
1. Nhãn phụ "CÓ ĐỀ NGHỊ" của B05 cần biết tin có offer PENDING hay không, nhưng không có phương thức công khai nào trong `data.md` trả thông tin này (`ChatRepository` chỉ có `sendOffer`, `respondToOffer`). R16 cấm tự đặt tên. Đề xuất thêm một phương thức đếm offer PENDING theo tin vào `ListingRepository` hoặc `ChatRepository` và cập nhật đặc tả; nếu không, bỏ nhãn này.
2. STT 14 và UC11 bước 1a1 ghi sắp theo thời gian "mới nhất, cũ nhất", nhưng `SortOrder` không có giá trị "cũ nhất trước". Issue này chỉ sắp `NEWEST`. Nếu cần thêm "cũ nhất" thì phải thêm giá trị enum (R6).
3. Xóa tin nháp có kèm xóa các tài liệu `images` của tin đó không? `ListingRepository.delete` chỉ ghi "chỉ xóa hẳn tin DRAFT". Nếu không xóa, ảnh mồ côi chiếm hạn mức Firestore gói Spark. Đề xuất: xóa luôn bằng `MediaRepository.delete` cho từng mã trong `imageIds` (chủ ảnh được xóa). Xin xác nhận.
4. Đăng bản nháp từ B05: UC09 bước 5 nói ở lại danh sách (tin sang tab Đang hoạt động), UC17 bước 5 nói chuyển sang B03. Cách hiểu mặc định: ở lại B05. Xin xác nhận.
