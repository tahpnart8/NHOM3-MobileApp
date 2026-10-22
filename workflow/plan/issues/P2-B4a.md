TITLE: feat(feed): tìm kiếm tin đăng, bộ lọc cơ bản và tìm người dùng
LABELS: type:feature, area:feed, module:b, priority:p1
ASSIGNEES: tranannhtu21012006
PHASE: P2

Plan item: P2-B4a

### Bối cảnh
1. PUBGApp (Pre-owned Users' Bargain Grounds) là ứng dụng Android mua bán, trao đổi, cho tặng đồ cũ, viết bằng Java 17, XML Views và ViewBinding, dùng Firebase (Authentication, Firestore gói Spark) và Room.
2. Module B (Tin đăng và khám phá, người làm: Trần Anh Tú) lo đăng tin, kệ hàng, trang chủ, tìm kiếm, chi tiết tin và tin đã lưu.
3. Issue này làm màn B02 `SearchFragment` với hai tab "Tin đăng" và "Người dùng": tìm tin theo từ khóa, bộ lọc cơ bản (danh mục, giá, tình trạng, hình thức), ba cách sắp xếp, lịch sử tìm kiếm trên máy, và tìm người dùng theo tên (UC14, UC15).
4. Đọc trước: `workflow/docs/ui/b-kham-pha-tin-dang/README.md#b02-tìm-kiếm-và-bộ-lọc`, `workflow/docs/product/use-case/uc-b-tin-dang.md#uc14-tìm-kiếm-tin-đăng` cùng `#uc15-tìm-kiếm-người-dùng`, `workflow/docs/architecture/luong-xu-ly.md` mục "3. Xem bảng tin, có mạng và mất mạng".
5. Đã có trên `main`: khung `SearchFragment` và `fragment_listing_search.xml`, `HistoryDao` và `RecentSearchEntity` (P1-05), `ListingRepository.getFeed` làm thật cùng `ListingCardAdapter` (từ P2-B3), mọi ViewModel đã có `loading`, `error`, `done` kèm getter công khai (`SearchViewModel` còn có `categories`, `loadCategories()`, `loadNext()`, và `searchUsers(String keyword)` nhận từ khóa riêng), các hành động toàn cục `action_global_sign_in` và `action_global_user_profile` trong `nav_main.xml`, các thành phần chung `view_empty_state.xml`, `view_error_state.xml`, `view_avatar.xml`, `view_offline_banner.xml`.

### Mã tính năng
F-05

### Mã việc kế hoạch
P2-B4a. Nửa đầu của P2-B4 (tìm kiếm và bộ lọc). Nửa sau là P2-B4b (khu vực, bán kính, sắp xếp "Người bán đánh giá tốt"). Tách vì khu vực và bán kính cần quyền vị trí, `GeoUtils` và đọc hồ sơ người bán, và còn chờ trả lời của leader.

### Module
B. Tin đăng và khám phá (Trần Anh Tú)

### STT trong bảng đặc tả
23, 24, 25, 26, 28, 31, 32, 33, 103. Tất cả có Phạm vi "Đợt này". Lịch sử tìm kiếm hiện trên B02 (khu vực "Tìm gần đây") được làm cùng issue này vì cùng màn; dòng `recordSearch` trong `data.md` ghi STT 38. STT 27 và 34 làm ở P2-B4b.

### Màn hình mockup
B02 (ảnh `workflow/docs/ui/b-kham-pha-tin-dang/b02-tim-kiem-bo-loc.png`). Ảnh chưa vẽ: tab "Người dùng", bộ lọc khu vực và bán kính (P2-B4b), bốn cách sắp xếp (ở đây làm ba cách đầu); ảnh vẽ nút quay lại, đúng là B02 là tab gốc, không có nút quay lại, và kết quả hiện ngay tại B02. Ảnh có 4 tình trạng, đúng là 5 (thêm "Như mới").

### Làm gì và vì sao
Người dùng ở tab Tìm kiếm gõ từ khóa, thấy các từ khóa tìm gần đây (lưu trên máy), mở bảng bộ lọc để chọn danh mục, khoảng giá (kèm mốc gợi ý nhanh), tình trạng (chọn nhiều trong 5 giá trị), hình thức giao dịch (chọn một hoặc nhiều) và cách sắp xếp (Mới đăng, Giá thấp đến cao, Giá cao đến thấp), bấm "Áp dụng" và xem kết quả ngay trên B02. "Xóa lọc" đưa bộ lọc về mặc định. Tìm theo từ khóa chỉ khớp nguyên từ. Màn này mở được cả khi chưa đăng nhập: Khách dùng đủ tab "Tin đăng"; Khách bấm tab "Người dùng" thì được đưa tới A01 và vẫn ở tab "Tin đăng". Người đã đăng nhập bấm tab "Người dùng", gõ tên hiển thị và thấy danh sách tài khoản ACTIVE khớp nguyên từ (ảnh đại diện, tên, khu vực); bấm một dòng thì sang D02. Kết quả chỉ gồm tin AVAILABLE. Issue này phục vụ dòng barem R-Chapter 4 (RecyclerView hiển thị lưới tin hai cột và danh sách người dùng theo dòng ngang; input control của bộ lọc: ô từ khóa, ô nhập khoảng giá có kiểm hợp lệ, ô chọn nhiều cho tình trạng và hình thức giao dịch; điều hướng bằng `TabLayout` hai tab và các hành động toàn cục `action_global_listing_detail`, `action_global_user_profile`, `action_global_sign_in`), dòng R-Chapter 3 (unit test JUnit `ListingRepositoryTest` và `SearchViewModelTest`), dòng R-Chapter 7 (async qua `DataCallback`) và phần Room (lịch sử tìm kiếm lưu trong bảng `recent_search`).

### Tiêu chí nghiệm thu
- [ ] Luồng chính chạy trên máy ảo với Firebase thật: gõ "iphone" thì kết quả là các tin AVAILABLE có từ "iphone" trong `searchKeywords`. Truy vấn dùng từ dài nhất trong ô tìm kiếm (đã bỏ dấu, viết thường) với `array-contains`; các từ còn lại và mọi tiêu chí khác lọc trên máy; tin RESERVED, SOLD, HIDDEN, DRAFT, REMOVED không xuất hiện. PR ghi rõ từ khóa và kết quả đã thử.
- [ ] Không có từ khóa nhưng có bộ lọc: kết quả lấy bằng `getFeed(filter, ...)` với cùng bộ lọc.
- [ ] Mỗi tiêu chí lọc đúng: danh mục (STT 24), giá từ và đến (STT 25, giá 0 là không giới hạn, có các mốc gợi ý nhanh), tình trạng chọn nhiều trong 5 giá trị (STT 26), hình thức chọn nhiều (STT 28). Chọn nhiều tiêu chí cùng lúc thì kết quả thỏa tất cả.
- [ ] Sắp xếp Mới đăng, Giá thấp đến cao, Giá cao đến thấp (STT 33, 31, 32) cho kết quả đúng thứ tự trong số tin đã tải (xem câu hỏi 2).
- [ ] Nhập giá "Đến" thấp hơn "Từ": lỗi màu đỏ ngay dưới ô giá "Mức giá đến không thể nhỏ hơn mức giá từ" và nút "Áp dụng" bị vô hiệu hóa.
- [ ] Mỗi lần tìm có từ khóa không rỗng thì `recordSearch` ghi vào `recent_search` (trên máy, tối đa 20 dòng, từ khóa là khóa chính nên không trùng); khu vực "Tìm gần đây" đọc bằng `getRecentSearches` và ẩn khi rỗng; chạm một từ khóa thì điền vào ô và tìm ngay. Lịch sử không gửi lên máy chủ.
- [ ] Kết quả hiện dạng lưới hai cột kèm số tin tìm được ("Tìm thấy N kết quả"); bấm một thẻ sang B03 bằng `action_global_listing_detail`.
- [ ] Không có kết quả: hiện "Không tìm thấy kết quả nào phù hợp với '<từ khóa>'. Hãy thử dùng từ khóa khác hoặc xóa bớt bộ lọc nhé!" kèm nút "Xóa bộ lọc" (`view_empty_state.xml`).
- [ ] Chế độ khách: chưa đăng nhập vẫn tìm, lọc, sắp xếp được tin đăng và ghi lịch sử trên máy; bấm tab "Người dùng" đưa tới A01 bằng `action_global_sign_in` và vẫn ở tab "Tin đăng".
- [ ] Tab "Người dùng" (đã đăng nhập): gõ tên rồi bấm phím "Tìm kiếm" thì `SearchViewModel.searchUsers(String keyword)` nhận đúng chữ trong ô của tab này (không dùng chung ô của tab "Tin đăng"), hiện các dòng ngang gồm ảnh đại diện (`view_avatar.xml`), tên hiển thị và khu vực, chỉ tài khoản ACTIVE, sắp theo số đánh giá giảm, tối đa 20 người; chưa tìm: hiện ô nhập và dòng hướng dẫn; không có kết quả: "Không tìm thấy người dùng nào phù hợp"; mất mạng: "Không có kết nối mạng. Vui lòng kiểm tra lại kết nối rồi thử lại"; bấm một dòng sang D02 bằng `action_global_user_profile` với `userId`.
- [ ] Kết quả tìm người dùng không có email, số điện thoại hay thông tin định danh, dù tài liệu `users` có các trường đó.
- [ ] Đủ bốn trạng thái ở cả hai tab: đang tải (khi `SearchViewModel.loading` bật), có dữ liệu, rỗng, lỗi (`view_error_state.xml` kèm "Thử lại", nội dung từ `SearchViewModel.error`, hiện một lần rồi xóa). Phương thức của module khác trả `DataCallback.NOT_READY` thì hiện đúng lỗi đó. Danh sách danh mục trong bảng lọc lấy từ `SearchViewModel.categories` (nạp bằng `loadCategories()`).
- [ ] Từ khóa, bộ lọc đã áp dụng, kết quả tin và kết quả người dùng nằm trong `SearchViewModel`: xoay màn hình không mất và không tìm lại.
- [ ] Mọi thao tác mạng và Room chạy ngoài luồng chính.
- [ ] Việc chọn từ dài nhất, kiểm khớp nguyên từ, áp bộ lọc trên máy, kiểm khoảng giá và sắp kết quả nằm trong phương thức package-private `static`, có unit test `ListingRepositoryTest` và `SearchViewModelTest` (đủ ca: nhiều từ, từ có dấu, bộ lọc giá có giá 0, tình trạng rỗng là mọi tình trạng, nhiều hình thức, từ chỉ khớp một phần thì không khớp).
- [ ] `./gradlew assembleDebug testDebugUnitTest lintDebug` và `sh workflow/scripts/check-java-style.sh` đều qua.
- [ ] PR liệt kê từng lớp, phương thức, trường công khai mới kèm dòng tài liệu định nghĩa (R16).

### Tài liệu phải đọc
- `AGENTS.md` (R12, R15 đến R23), `workflow/plan/so-huu.md` mục 3, 4, 5, 6.
- `workflow/docs/ui/b-kham-pha-tin-dang/README.md` mục B02 (đặc biệt "Trạng thái đặc biệt" và "Khác với ảnh") và `workflow/docs/ui/README.md` mục "Điều hướng" (chế độ khách).
- `workflow/docs/product/use-case/uc-b-tin-dang.md` UC14 (luồng 1a, 2a, 2c, ngoại lệ 2e, 3e, BR1 đến BR4, BR6) và UC15.
- `workflow/docs/architecture/luong-xu-ly.md` mục 3 (điểm 2 và 3).
- `workflow/docs/architecture/dac-ta-lop/data.md#listingrepository` (dòng `getFeed`, `search`, `recordSearch`, `getRecentSearches`), `#userrepository` (dòng `searchUsers`), `#categoryrepository`.
- `workflow/docs/architecture/dac-ta-lop/viewmodel.md#searchviewmodel`.
- `workflow/docs/architecture/dac-ta-lop/model.md#searchfilter`, `#listing` (`searchKeywords`), `#user` (`searchKeywords`, `displayName`, `avatarBase64`, `province`, `status`).
- `workflow/docs/architecture/dac-ta-lop/util.md#formatutils` (`removeAccents`, `toKeywords`).
- `workflow/docs/architecture/dac-ta-lop/data-local.md#historydao`, `#recentsearchentity`; `workflow/docs/data/room-schema.md#recent_search`.
- `workflow/docs/data/firestore-schema.md#listings` (bảng truy vấn của `search` và `getFeed`), `#users` (truy vấn `searchUsers`), mục "Chỉ mục ghép dự kiến".
- `workflow/docs/architecture/dac-ta-lop/enums.md#sortorder`, `#itemcondition`, `#tradetype`, `#userstatus`.
- `workflow/docs/data/security-rules.md` mục 1 (users: đọc cần đăng nhập), mục 10 (đoạn "Đọc"), quy ước chung số 8 và 9.
- `workflow/docs/conventions/giao-dien.md` và `.agents/skills/pubg-ui/SKILL.md`; `workflow/memory/code/firebase.md`; `workflow/docs/product/barem-diem.md` (dòng barem mà issue phục vụ).

### Lớp, trường, trạng thái sẽ dùng
- `SearchFragment`; `SearchViewModel` với `setKeyword(String text)`, `applyFilter(SearchFilter filter)`, `search()`, `loadNext()`, `searchUsers(String keyword)`, `loadCategories()`, `loadRecentSearches()`, thuộc tính `filter` (`SearchFilter`) và các LiveData `results` (`LiveData<List<Listing>>`), `userResults` (`LiveData<List<User>>`), `categories` (`LiveData<List<Category>>`), `recentSearches` (`LiveData<List<String>>`), `loading` (`LiveData<Boolean>`), `error` (`LiveData<String>`), `done` (`LiveData<Boolean>`), mỗi LiveData có getter công khai.
- `ListingRepository.search(SearchFilter filter, Timestamp after, DataCallback<List<Listing>> cb)`, `recordSearch(String keyword)`, `getRecentSearches(int limit, DataCallback<List<String>> cb)`; mở rộng thân `getFeed` (đã làm ở P2-B3) cho lọc trên máy.
- `UserRepository.searchUsers(String keyword, DataCallback<List<User>> cb)` (chưa có chủ trong `so-huu.md` mục 5, xem câu hỏi 1).
- `SearchFilter`: `keyword`, `categoryId`, `minPrice`, `maxPrice`, `conditions`, `tradeTypes`, `sortOrder`; `hasPriceRange()`, `reset()`.
- `SortOrder` (`NEWEST`, `PRICE_ASC`, `PRICE_DESC`), `ItemCondition`, `TradeType`, `UserStatus.ACTIVE`.
- `FormatUtils.removeAccents(String)`, `toKeywords(String)`, `formatPrice(long)`.
- `CategoryRepository.getAll(DataCallback<List<Category>> cb)`, `getChildren(String parentId, DataCallback<List<Category>> cb)` (Phát, chỉ gọi).
- `HistoryDao.insertSearch(RecentSearchEntity)`, `getRecentSearches(int)`, `trimSearches(int)`; `RecentSearchEntity` (`keyword`, `searchedAtMillis`).
- `User`: `displayName`, `avatarBase64`, `province`, `searchKeywords`, `ratingCount`, `status`; `Listing`: `searchKeywords`, `price`, `itemCondition`, `tradeTypes`, `categoryId`, `createdAt`.
- `SessionManager.getInstance()`, `isLoggedIn()`.
- Hành động toàn cục `action_global_listing_detail` (tham số `listingId`), `action_global_user_profile` (tham số `userId`), `action_global_sign_in` (Khách bấm tab "Người dùng").

Tên nội bộ do issue này đặt (package-private hoặc lớp giao diện, ghi vào PR ở mục tên mới): trong `ListingRepository` các hàm `static String pickQueryWord(String keyword)` và `static boolean matchesLocalFilter(Listing listing, SearchFilter filter)`; trong `SearchViewModel` các hàm `static boolean isPriceRangeValid(long minPrice, long maxPrice)` và `static List<Listing> sortResults(List<Listing> results, SortOrder order)`; lớp giao diện `UserResultAdapter`; layout `item_listing_user.xml`, `layout_listing_filter_panel.xml`.

Chi tiết triển khai, ghi một dòng bằng skill `pubg-record-decision`: mốc giá gợi ý nhanh (ví dụ dưới 500.000, 500.000 đến 1.000.000, 1.000.000 đến 5.000.000, 5.000.000 đến 10.000.000, trên 10.000.000 đồng; tài liệu chưa quy định các mốc); mỗi tab của B02 có ô nhập riêng: tab "Tin đăng" gọi `setKeyword(text)` rồi `search()`, tab "Người dùng" gọi thẳng `searchUsers(text)`.

### File được tạo hoặc sửa
- [sửa] `PUBGApp/app/src/main/java/com/nhom3/pubgapp/ui/listing/SearchFragment.java`
- [tạo] `PUBGApp/app/src/main/java/com/nhom3/pubgapp/ui/listing/UserResultAdapter.java`
- [sửa] `PUBGApp/app/src/main/res/layout/fragment_listing_search.xml`
- [tạo] `PUBGApp/app/src/main/res/layout/layout_listing_filter_panel.xml` (bảng bộ lọc)
- [tạo] `PUBGApp/app/src/main/res/layout/item_listing_user.xml` (một dòng kết quả người dùng)
- [sửa] `PUBGApp/app/src/main/res/values/strings_listing.xml`
- [sửa] `PUBGApp/app/src/main/java/com/nhom3/pubgapp/viewmodel/SearchViewModel.java` (chỉ thân `setKeyword`, `applyFilter`, `search`, `loadNext`, `searchUsers`, `loadCategories`, `loadRecentSearches`, việc đẩy dữ liệu vào các LiveData đã có, và hai hàm package-private ở trên)
- [sửa] `PUBGApp/app/src/main/java/com/nhom3/pubgapp/data/ListingRepository.java` (chỉ thân `search`, `recordSearch`, `getRecentSearches`; mở rộng thân `getFeed` cho giá, tình trạng, hình thức; hai hàm package-private ở trên)
- [sửa] `PUBGApp/app/src/main/java/com/nhom3/pubgapp/data/UserRepository.java` (chỉ thân `searchUsers`, và chỉ khi leader xác nhận giao việc này cho Tú ở câu hỏi 1)
- [tạo] `PUBGApp/app/src/test/java/com/nhom3/pubgapp/viewmodel/SearchViewModelTest.java`
- [tạo] `PUBGApp/app/src/test/java/com/nhom3/pubgapp/data/ListingRepositoryTest.java`

Dùng lại `ListingCardAdapter` và `item_listing_card.xml` (P2-B3, P1).

### Các bước đề nghị
Người làm có thể đổi thứ tự nhưng không được đổi tên nào ở trên.
1. Trong `ListingRepository` viết `static String pickQueryWord(String keyword)` (dùng `FormatUtils.removeAccents` rồi `FormatUtils.toKeywords`, trả từ dài nhất, rỗng khi không có từ) và `static boolean matchesLocalFilter(Listing listing, SearchFilter filter)` (kiểm mọi từ còn lại của từ khóa nằm nguyên từ trong `searchKeywords`, giá trong khoảng khi `hasPriceRange()`, `itemCondition` nằm trong `conditions` khi `conditions` không rỗng, ít nhất một hình thức của tin nằm trong `tradeTypes` khi `tradeTypes` không rỗng). Trong `SearchViewModel` viết `static boolean isPriceRangeValid(long minPrice, long maxPrice)` (true khi một trong hai bằng 0 hoặc `maxPrice` không nhỏ hơn `minPrice`) và `static List<Listing> sortResults(List<Listing> results, SortOrder order)`.
2. Viết `ListingRepositoryTest` và `SearchViewModelTest` cho các hàm trên rồi chạy `./gradlew testDebugUnitTest`.
3. Làm thân `ListingRepository.search(SearchFilter filter, Timestamp after, DataCallback<List<Listing>> cb)`: truy vấn `searchKeywords array-contains <pickQueryWord(filter.keyword)>`, `status == AVAILABLE`, sắp `createdAt` giảm, `limit(20)`, `startAfter(after)` khi có; lọc phần còn lại bằng `matchesLocalFilter`; `onSuccess` với danh sách đã lọc. Mở rộng thân `getFeed` để cũng gọi `matchesLocalFilter` cho giá, tình trạng, hình thức. Khi lọc trên máy làm trang thiếu thì xem câu hỏi 3.
4. Làm thân `ListingRepository.recordSearch(String keyword)` (`HistoryDao.insertSearch(new RecentSearchEntity ...)` rồi `trimSearches(20)`, ngoài luồng chính; bỏ qua từ khóa rỗng) và `getRecentSearches(int limit, DataCallback<List<String>> cb)` (`HistoryDao.getRecentSearches(limit)`, trả `keyword` mới nhất trước).
5. Nếu leader xác nhận ở câu hỏi 1: làm thân `UserRepository.searchUsers(String keyword, DataCallback<List<User>> cb)` theo `data.md`: từ dài nhất của `keyword` đã bỏ dấu, `searchKeywords array-contains`, `status == ACTIVE`, sắp `ratingCount` giảm, tối đa 20, không phân trang.
6. Làm thân `SearchViewModel`: `setKeyword`, `applyFilter` (đặt `filter`, gọi `search()`), `search()` (gọi `ListingRepository.search`, sắp bằng `sortResults`, ghi `recordSearch` khi từ khóa không rỗng, đổ vào `results`), `loadNext()` (tải trang kế bằng `createdAt` của tin cuối, nối vào `results`, bỏ qua khi đang tải hoặc đã hết dữ liệu), `searchUsers(String keyword)` (gọi `UserRepository.searchUsers(keyword, cb)`, đổ vào `userResults`), `loadCategories()` (gọi `CategoryRepository.getAll`, đổ vào `categories`), `loadRecentSearches()`. Mọi phương thức bật `loading` khi bắt đầu và tắt khi xong, lỗi đặt vào `error`. Dùng lớp ẩn danh `DataCallback`, không lambda.
7. Dựng `fragment_listing_search.xml`: `TabLayout` hai tab; tab "Tin đăng" có ô từ khóa, nút mở và đóng bảng lọc, khu vực "Tìm gần đây" (các thẻ từ khóa), `layout_listing_filter_panel.xml` (danh mục, khoảng giá và mốc nhanh, tình trạng, hình thức, sắp xếp, hai nút "Xóa lọc" và "Áp dụng"), dòng đếm kết quả, `RecyclerView` lưới hai cột; tab "Người dùng" có ô nhập, dòng hướng dẫn, `RecyclerView` dòng ngang. Có `view_empty_state.xml`, `view_error_state.xml`, `view_offline_banner.xml`. Màu và khoảng cách lấy từ token; id view theo lowerCamelCase `<loại><Việc>` (ví dụ `editKeyword`, `recyclerResults`, `buttonApplyFilter`).
8. Làm `UserResultAdapter` và `item_listing_user.xml` (ảnh đại diện từ `avatarBase64`, tên, khu vực; không hiện email hay số điện thoại).
9. Làm `SearchFragment`: bảng lọc dựng `SearchFilter` mới khi bấm "Áp dụng" (kiểm `isPriceRangeValid`, sai thì hiện lỗi và vô hiệu nút), "Xóa lọc" gọi `SearchFilter.reset()`; chạm từ khóa gần đây thì điền ô và tìm; bấm thẻ kết quả sang B03. Gọi `loadCategories()` khi mở màn hình. Tab "Người dùng": nếu `SessionManager.getInstance().isLoggedIn()` là false thì gọi `action_global_sign_in` và giữ tab "Tin đăng"; nếu true thì phím "Tìm kiếm" gọi `searchUsers(text)` với chữ trong ô của tab này; bấm dòng thì `action_global_user_profile` với `userId`.
10. Thêm chuỗi vào `strings_listing.xml` (khóa theo mẫu `<module>_<màn>_<việc>`, ví dụ `listing_search_filter_apply`); mọi nhãn tình trạng và hình thức là string resource, không dùng tên hằng.
11. Chạy các lệnh ở mục "Tự kiểm trước khi mở Pull Request"; thử cả khi đã đăng nhập và khi chưa đăng nhập.

### File dùng chung cần thêm dòng
Không có. Nếu `search` hoặc `getFeed` báo thiếu chỉ mục ghép, ghi đường dẫn tạo chỉ mục vào mô tả PR để Phát gộp vào `firebase/firestore.indexes.json` (không tự sửa).

### Cấm đụng
- File chỉ Phát sửa: `AndroidManifest.xml`, `nav_main.xml`, `nav_admin.xml`, menu thanh điều hướng, `themes.xml`, `colors.xml`, `dimens.xml`, `strings_common.xml`, `AppDatabase`, `Converters`, mọi file Gradle, mọi file trong `firebase/`.
- Lớp `model` và `model.enums`: không sửa.
- `CategoryRepository`, `FormatUtils` (Phát): chỉ gọi.
- D02 (`UserProfileFragment`, Phúc): chỉ đi tới bằng `action_global_user_profile`.
- Phần `getFeed` xử lý kênh theo dõi và bộ nhớ đệm của P2-B3: chỉ mở rộng bộ lọc trên máy, không đổi hành vi khác.
- Các phương thức khác của `ListingRepository`, `UserRepository` và `FeedViewModel`.

### Phụ thuộc
P2-B3.

### Ngoài phạm vi
- Lọc khu vực (tỉnh), bán kính 1, 3, 5 km và sắp theo người bán đánh giá tốt: P2-B4b.
- Mục "Dành cho bạn": P2-B5.
- Tìm khi offline trên dữ liệu đệm: P3-Bb. Trong issue này mất mạng thì báo lỗi rõ ràng.
- Tìm người dùng theo email, số điện thoại hoặc tìm không dấu từng phần: không làm (STT 103 chỉ khớp nguyên từ theo tên hiển thị).
- Bố cục ngang và giữ nội dung bảng lọc chưa áp dụng khi xoay: P3-Ba.

### Tài liệu cần cập nhật
Không có, trừ khi các câu hỏi dưới đây đổi hành vi (khi đó cập nhật `viewmodel.md`, `so-huu.md`, `luong-xu-ly.md`, `firestore-schema.md` theo R7).

### Tự kiểm trước khi mở Pull Request
- [ ] `cd PUBGApp && ./gradlew testDebugUnitTest` chạy `ListingRepositoryTest` và `SearchViewModelTest`: toàn bộ ca qua.
- [ ] Tìm "iphone" khi có tin chứa từ này: kết quả đúng, tin không chứa từ này không xuất hiện; tìm "ipho" (một phần của từ): không có kết quả.
- [ ] Chọn danh mục, khoảng giá, hai tình trạng, một hình thức rồi "Áp dụng": mọi thẻ kết quả thỏa cả bốn tiêu chí; "Xóa lọc" đưa về mặc định.
- [ ] Nhập giá "Đến" nhỏ hơn "Từ": lỗi đỏ dưới ô, nút "Áp dụng" xám.
- [ ] Đổi sắp xếp giữa ba cách: thứ tự đổi đúng.
- [ ] Tìm hai từ khóa khác nhau rồi mở lại màn: khu vực "Tìm gần đây" có hai thẻ, mới nhất trước; chạm thẻ thì tìm ngay.
- [ ] Đã đăng nhập, tab "Người dùng", gõ tên một tài khoản có thật: thấy đúng tài khoản ACTIVE, bấm vào sang D02; tài khoản bị khóa không xuất hiện.
- [ ] Chưa đăng nhập: tìm tin đăng được; bấm tab "Người dùng" sang A01 và vẫn ở tab "Tin đăng".
- [ ] Bật chế độ máy bay rồi tìm: hiện lỗi mạng bằng tiếng Việt, không treo.
- [ ] Xoay màn hình sau khi có kết quả: từ khóa, bộ lọc đã áp dụng và kết quả không mất.
- [ ] `cd PUBGApp && ./gradlew assembleDebug testDebugUnitTest lintDebug` qua.
- [ ] `sh workflow/scripts/check-java-style.sh` không báo lỗi.
- [ ] Danh sách mọi lớp, phương thức, trường công khai mới kèm dòng tài liệu định nghĩa đã dán vào PR; tên nội bộ được ghi riêng (R16).

### Quy mô
- [x] Đây là một lát cắt dọc của một module, khoảng nửa ngày đến một ngày, một Pull Request.

### Câu hỏi cho leader
1. `UserRepository.searchUsers` (STT 103) vẫn không có chủ trong `so-huu.md` mục 5, và STT 103 chưa nằm trong danh sách STT của module B ở mục 1, dù `SearchViewModel.searchUsers(String keyword)` là của Tú. Đề xuất giao `UserRepository.searchUsers` cho Tú và ghi vào bảng chủ sở hữu. Chưa có xác nhận thì không viết thân `searchUsers` (R20).
2. `luong-xu-ly.md` mục 3 điểm 3 ghi sắp theo giá làm ở máy chủ nhờ chỉ mục `status, price`, nhưng con trỏ của `getFeed` và `search` chỉ là `createdAt`, không phân trang đúng khi sắp theo giá. Cách làm của issue này: sắp theo giá trên máy trong phạm vi các tin đã tải (giống `TOP_RATED`). Xin xác nhận, rồi bỏ hai chỉ mục theo giá khỏi `firestore-schema.md` hoặc đổi con trỏ.
3. `SearchViewModel.loadNext()` đã có, nhưng `data.md` (dòng `getFeed`) giao cho ViewModel việc tải tiếp tới khi đủ 20 tin sau khi lọc trên máy, trong khi callback của `search` và `getFeed` chỉ trả danh sách đã lọc: ViewModel không biết con trỏ thật của trang mạng (nếu một trang bị lọc hết thì lấy `createdAt` của tin nào?) và không biết đã hết dữ liệu chưa. Đề xuất: `ListingRepository.search` và `getFeed` tự lặp bên trong tới khi đủ 20 tin hoặc hết dữ liệu, `loadNext()` dùng `createdAt` của tin cuối trong `results`. Xin xác nhận, hoặc nói rõ con trỏ đi qua đâu. Trong lúc chờ, issue này chỉ hiện trang đầu kèm `loadNext()` theo cách đề xuất.
