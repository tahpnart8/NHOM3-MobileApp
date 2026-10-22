TITLE: feat(offline): xem lại tin đã xem khi mất mạng và khóa nút ghi
LABELS: type:feature, area:offline, module:b, priority:p2
ASSIGNEES: tranannhtu21012006
PHASE: P3

Plan item: P3-Bb

### Bối cảnh
1. PUBGApp (Pre-owned Users' Bargain Grounds) là ứng dụng Android mua bán, trao đổi, cho tặng đồ cũ, viết bằng Java 17, XML Views và ViewBinding, dùng Firebase (Authentication, Firestore gói Spark) và Room.
2. Module B (Tin đăng và khám phá, người làm: Trần Anh Tú) lo đăng tin, kệ hàng, trang chủ, tìm kiếm, chi tiết tin và tin đã lưu.
3. Issue này hoàn thiện và kiểm toàn bộ việc xem lại dữ liệu đã xem khi mất mạng (Room) cho B01 đến B06, và khóa mọi nút ghi khi offline; đây là yêu cầu cứng của môn học.
4. Đọc trước: `workflow/plan/phase-3-hoan-thien.md` ("Danh sách kiểm tra"), `workflow/docs/architecture/luong-xu-ly.md` mục "3. Xem bảng tin, có mạng và mất mạng", `workflow/docs/data/room-schema.md` (cả tệp, chú ý "Chính sách giữ").
5. Đã có trên `main`: bộ nhớ đệm `listing_cache` ghi và đọc ở `ListingRepository.getFeed`, `getById` (P2-B3, P2-B5), `recent_view` và `recent_search` ghi ở `recordView`, `recordSearch` (P2-B6a, P2-B4a), dải `view_offline_banner.xml` ở B01, B02, B03 (P1), `NetworkMonitor.getInstance()` (P1), mọi ViewModel có `loading`, `error`, `done` kèm getter công khai, và các màn B01 đến B06 đủ chức năng (P2-B1a đến P2-B7).

### Mã tính năng
F-04, F-05, F-06, F-07

### Mã việc kế hoạch
P3-Bb. Nửa sau của P3-B (offline cho trang chủ, chi tiết tin, lịch sử tìm), làm cùng hoặc sau P3-Ba.

### Module
B. Tin đăng và khám phá (Trần Anh Tú)

### STT trong bảng đặc tả
Không có STT riêng. Đây là yêu cầu cứng của môn học (xem lại trang đã xem khi mất mạng, dùng Room), ghi ở `workflow/docs/product/features.md` mục "Yêu cầu môn học".

### Màn hình mockup
B01, B02, B03, B04, B05, B06 (trạng thái "Mất mạng" của từng màn trong `workflow/docs/ui/b-kham-pha-tin-dang/README.md`).

### Làm gì và vì sao
Bật chế độ máy bay sau khi đã xem thì: trang chủ vẫn hiện các tin đã xem kèm dải báo offline; chi tiết tin đã xem vẫn mở được từ bộ nhớ đệm (chỉ có các trường và ảnh bìa thu nhỏ của `listing_cache`); lịch sử tìm kiếm vẫn hiện và tìm được trong các tin đã đệm; mọi nút ghi của module B (đăng tin, lưu nháp, sửa, ẩn, hiện, đánh dấu đã bán, lưu tin, bỏ lưu, đăng và xóa nháp) bị khóa kèm dòng giải thích. Khách (chưa đăng nhập) được đối xử như nhau ở các màn B01 đến B03. Đăng xuất thì bộ nhớ đệm Room bị xóa. Mạng rớt giữa chừng khi đang ghi thì sau 15 giây hiện lỗi chứ không treo (`overview.md` mục 8 và 10). Phần trang chủ và chi tiết tin đã có nền từ P2-B3 và P2-B5; issue này hoàn thiện và kiểm toàn bộ. Ghi dữ liệu khi offline rồi gửi sau không được hỗ trợ trong đợt này. Issue này phục vụ dòng barem R-Chapter 7 (kiểm tra kết nối mạng bằng `NetworkMonitor` để chọn dữ liệu đệm hay dữ liệu mạng và khóa nút ghi; async: mọi đọc Room và Firestore ngoài luồng chính qua `DataCallback`), dòng R-Chapter 3 (unit test JUnit `ListingRepositoryTest` cho `chooseSource` và `matchesCachedText`) và yêu cầu riêng của thầy về SQLite (Room) cho trang đã xem.

### Tiêu chí nghiệm thu
- [ ] Trang chủ: xem một lượt có mạng, bật chế độ máy bay, mở lại tab Trang chủ: tin đã đệm vẫn hiện (chỉ ảnh bìa thu nhỏ), có dải báo offline, không có vòng quay vô hạn. Chưa từng có dữ liệu đệm thì hiện ô trống rõ ràng chứ không báo lỗi giả. Khách cũng vậy.
- [ ] Trang chủ, kênh "Đang theo dõi" khi offline: hiện "Cần có mạng để xem kênh đang theo dõi", không hiện lẫn tin của kênh chung (bộ nhớ đệm không lưu quan hệ theo dõi).
- [ ] Hàng "Dành cho bạn" khi offline: dùng tin đã đệm làm ứng viên cùng lịch sử xem và tìm trên máy; không báo lỗi; điểm từ khóa và danh mục quan tâm có thể thiếu (tin đệm không có `searchKeywords`, hồ sơ không đọc được), chấp nhận.
- [ ] Chi tiết tin: tin đã xem một lần có mạng mở được khi offline từ `listing_cache` (qua `ListingRepository.getById`, đọc `ListingDao.getById` khi không có mạng hoặc khi đọc mạng lỗi), hiện tên, giá, tình trạng, khu vực, hình thức, mô tả, ảnh bìa thu nhỏ và dải báo offline. Ảnh đầy đủ, "Tình trạng chi tiết", thẻ người bán, tin tương tự và giá tham khảo không có khi offline: hiện chỗ trống rõ ràng hoặc chữ "Cần có mạng để xem", không giả dữ liệu. Tin chưa từng xem thì hiện "Chưa có dữ liệu để xem khi offline".
- [ ] Chi tiết tin offline: nút lưu tin, nhắn tin, đề nghị giá, báo cáo, theo dõi, chỉnh sửa, đã bán đều bị khóa (xám) kèm dòng giải thích.
- [ ] Tìm kiếm: offline vẫn hiện khu vực "Tìm gần đây" từ `recent_search`; bấm tìm tin đăng khi offline thì tìm trong các tin đã đệm (khớp không dấu trên `title` và `description`, kèm bộ lọc giá, tình trạng, hình thức, danh mục, tỉnh, sắp xếp; không lọc bán kính vì tin đệm không có tọa độ, và báo điều đó) kèm dải báo offline; tab "Người dùng" offline hiện "Không có kết nối mạng. Vui lòng kiểm tra lại kết nối rồi thử lại". Không treo.
- [ ] Đăng tin, lưu nháp, sửa tin, ẩn, hiện, đánh dấu đã bán, mở bán lại, đăng và xóa nháp ở B05, lưu tin, bỏ lưu: mọi nút khóa khi offline và mở lại khi có mạng trở lại (dải báo biến mất), chữ đang nhập không mất.
- [ ] Kệ hàng của tôi (B05) và Tin đã lưu (B06): xem câu hỏi 3. Cách làm mặc định: khi mở mới lúc offline hiện "Cần có mạng để xem" rõ ràng, không treo, không trống im lặng; danh sách đã tải trước đó trong phiên vẫn hiện khi bật máy bay rồi xoay màn hình.
- [ ] Mạng rớt giữa chừng lúc đang ghi (tắt mạng ngay sau khi bấm "Đăng tin"): sau 15 giây hiện lỗi tiếng Việt, nút mở lại, không có tài liệu ghi dở (ảnh đã ghi được dọn). Ghi bẫy vào `workflow/memory/code/` (Firestore xếp hàng lệnh ghi khi offline và không báo hoàn tất tới khi có mạng).
- [ ] Đăng xuất rồi đăng nhập bằng tài khoản khác: `listing_cache`, `recent_view`, `recent_search` rỗng (kiểm bằng cách xem trang chủ offline và mục "Tìm gần đây"). Việc xóa Room nằm trong `AuthViewModel.logout` (Ngân); nếu chưa xóa thì ghi lỗi vào issue của P2-A1 chứ không sửa file đó. `ListingDao.clearAll` và `HistoryDao.clearAll` thuộc module B phải đúng theo `room-schema.md`.
- [ ] Giữ giới hạn bộ nhớ đệm: `listing_cache` không quá 200 dòng (`trim(200)`) và không xóa tin có trong `recent_view`; `recent_view` tối đa 50 dòng; `recent_search` tối đa 20 dòng. Có test hoặc cách đo ghi trong PR.
- [ ] Unit test trong `ListingRepositoryTest` cho hai phương thức package-private `static`: chọn nguồn dữ liệu (có mạng thì dùng mạng rồi ghi đệm, mất mạng thì dùng đệm, đệm rỗng thì báo chưa có dữ liệu) và khớp từ khóa không dấu trên tin đệm.
- [ ] Mọi thao tác Room chạy ngoài luồng chính. Mỗi lỗi hiện câu tiếng Việt qua `error` của ViewModel (hiện một lần rồi xóa), trạng thái đang tải qua `loading`.
- [ ] `./gradlew assembleDebug testDebugUnitTest lintDebug` và `sh workflow/scripts/check-java-style.sh` đều qua.
- [ ] PR liệt kê từng lớp, phương thức, trường công khai mới kèm dòng tài liệu định nghĩa (R16) và ghi lại kết quả danh sách kiểm tra của `phase-3-hoan-thien.md` cho module B trên một máy ảo.

### Tài liệu phải đọc
- `AGENTS.md` (R3, R12, R15 đến R22), `workflow/plan/phase-3-hoan-thien.md` ("Danh sách kiểm tra").
- `workflow/docs/architecture/luong-xu-ly.md` mục 3 (đủ 7 điểm).
- `workflow/docs/architecture/overview.md` mục 8 ("Offline"), mục 10 và mục 11 (rủi ro Room và ảnh).
- `workflow/docs/data/room-schema.md` (cả tệp, chú ý "Chính sách giữ" và "Truy vấn của DAO").
- `workflow/docs/architecture/dac-ta-lop/data-local.md#listingentity`, `#listingdao`, `#historydao`.
- `workflow/docs/architecture/dac-ta-lop/data.md#listingrepository` (dòng `getById`, `getFeed`, `search`, `recordView`, `recordSearch`, `getRecentSearches`, `getRecentViews`).
- `workflow/docs/architecture/dac-ta-lop/util.md#networkmonitor`.
- `workflow/docs/ui/b-kham-pha-tin-dang/README.md` (các dòng "Mất mạng" của B01, B02, B03, B04) và `workflow/docs/ui/README.md` mục "Điều hướng".
- `workflow/docs/product/use-case/uc-b-tin-dang.md` UC09, UC11, UC12 (ngoại lệ 1e), UC13 (ngoại lệ 1e), UC14 (yêu cầu phi chức năng), UC15 (ngoại lệ 2e), UC16 (yêu cầu phi chức năng).
- `workflow/memory/code/firebase.md`, `workflow/memory/code/README.md`.
- `workflow/docs/product/barem-diem.md` (dòng barem mà issue phục vụ).

### Lớp, trường, trạng thái sẽ dùng
- `ListingRepository.getById`, `getFeed`, `search`, `recordView`, `recordSearch`, `getRecentSearches`, `getRecentViews` (thân đã có từ P2; chỉ bổ sung nhánh offline).
- `ListingDao.getById(String)`, `getFeed()`, `upsertAll`, `trim(int)`, `clearAll()`; `HistoryDao.getRecentSearches(int)`, `getRecentViews(int)`, `trimViews(int)`, `trimSearches(int)`, `clearAll()`.
- `ListingEntity.fromModel(Listing)`, `toModel()`; `RecentViewEntity`, `RecentSearchEntity`.
- `NetworkMonitor.getInstance()`, `isOnline()`; `FormatUtils.removeAccents(String)`.
- `FeedViewModel`, `SearchViewModel`, `ListingDetailViewModel`, `ShopViewModel`, `ProfileViewModel.loadSaved`, `unsaveListing` (chỉ phần offline), cùng các LiveData `loading`, `error`, `done` của chúng.
- Thành phần chung `view_offline_banner.xml`, `view_empty_state.xml`, `view_error_state.xml` (P1), chỉ dùng.

Tên nội bộ do issue này đặt (package-private, ghi vào PR ở mục tên mới): trong `ListingRepository` các hàm `static int chooseSource(boolean online, boolean hasCache)` (với hằng `SOURCE_NETWORK`, `SOURCE_CACHE`, `SOURCE_NONE`) và `static boolean matchesCachedText(Listing listing, String keyword)`.

Chi tiết triển khai, ghi một dòng bằng skill `pubg-record-decision`: tìm kiếm khi offline chạy trên `ListingDao.getFeed()` (tin AVAILABLE đã đệm, tối đa 200) và khớp theo văn bản `title` cộng `description` đã bỏ dấu, vì tin đệm không có `searchKeywords`; ghi `recent_view` và `recent_search` khi offline vẫn được vì chúng chỉ nằm trên máy.

### File được tạo hoặc sửa
- [sửa] `PUBGApp/app/src/main/java/com/nhom3/pubgapp/data/ListingRepository.java` (chỉ nhánh offline và đệm của `getById`, `getFeed`, `search`, và xử lý quá hạn 15 giây cho các thân do module B làm)
- [sửa] `PUBGApp/app/src/main/java/com/nhom3/pubgapp/data/local/ListingEntity.java` (chỉ khi `toModel` thiếu trường cần cho chi tiết offline)
- [sửa] `PUBGApp/app/src/main/java/com/nhom3/pubgapp/viewmodel/FeedViewModel.java`
- [sửa] `PUBGApp/app/src/main/java/com/nhom3/pubgapp/viewmodel/SearchViewModel.java`
- [sửa] `PUBGApp/app/src/main/java/com/nhom3/pubgapp/viewmodel/ListingDetailViewModel.java`
- [sửa] `PUBGApp/app/src/main/java/com/nhom3/pubgapp/viewmodel/ShopViewModel.java`
- [sửa] `PUBGApp/app/src/main/java/com/nhom3/pubgapp/viewmodel/ProfileViewModel.java` (chỉ `loadSaved`, `unsaveListing`)
- [sửa] `PUBGApp/app/src/main/java/com/nhom3/pubgapp/ui/listing/HomeFragment.java`
- [sửa] `PUBGApp/app/src/main/java/com/nhom3/pubgapp/ui/listing/SearchFragment.java`
- [sửa] `PUBGApp/app/src/main/java/com/nhom3/pubgapp/ui/listing/ListingDetailFragment.java`
- [sửa] `PUBGApp/app/src/main/java/com/nhom3/pubgapp/ui/listing/PostListingFragment.java`
- [sửa] `PUBGApp/app/src/main/java/com/nhom3/pubgapp/ui/listing/MyShopFragment.java`
- [sửa] `PUBGApp/app/src/main/java/com/nhom3/pubgapp/ui/listing/SavedListingsFragment.java`
- [sửa] `PUBGApp/app/src/main/res/values/strings_listing.xml`
- [sửa] `PUBGApp/app/src/test/java/com/nhom3/pubgapp/data/ListingRepositoryTest.java`
- [tạo] `workflow/memory/code/room.md` (nếu P1-08 đã tạo thì [sửa], chỉ thêm mục của module B)

Chỉ sửa ViewModel và Fragment ở chỗ liên quan tới offline; không đổi hành vi online.

### Các bước đề nghị
Người làm có thể đổi thứ tự nhưng không được đổi tên nào ở trên.
1. Đọc `room-schema.md` và `luong-xu-ly.md` mục 3, rồi lập bảng chiều từng màn B01 đến B06: dữ liệu nào lấy từ Room, dữ liệu nào cần mạng, nút nào ghi.
2. Trong `ListingRepository` viết `static int chooseSource(boolean online, boolean hasCache)` và `static boolean matchesCachedText(Listing listing, String keyword)` (so khớp chuỗi đã bỏ dấu, viết thường bằng `FormatUtils.removeAccents` trên `title` và `description`). Thêm ca vào `ListingRepositoryTest`: có mạng chọn mạng, mất mạng có đệm chọn đệm, mất mạng đệm rỗng chọn "không có", khớp có dấu và không dấu, từ không có trong văn bản thì không khớp. Chạy `./gradlew testDebugUnitTest`.
3. Bổ sung nhánh offline cho `ListingRepository.getById` (đọc `ListingDao.getById`, `toModel`; trả `null` kèm lý do "chưa có dữ liệu" khi không có), `getFeed` (giữ dữ liệu đệm, không đệm kênh theo dõi), `search` (đọc `ListingDao.getFeed()` rồi lọc bằng `matchesCachedText` và `matchesLocalFilter`, bỏ bán kính).
4. Với mọi thân ghi do module B làm (`create`, `saveDraft`, `publish`, `update`, `setHidden`, `setStatus`, `delete`, `saveListing`, `unsaveListing`): kiểm `NetworkMonitor.isOnline()` trước khi ghi và có thời hạn chờ 15 giây theo `overview.md` mục 8; quá hạn thì `onError` "Không có kết nối mạng..." và dọn ảnh đã ghi. Dùng cách đặt thời hạn chung nếu P1-04 đã có (câu hỏi 1).
5. Trong ViewModel và Fragment: dải báo offline (`view_offline_banner.xml`) theo `NetworkMonitor.getInstance().isOnline()`, khóa mọi nút ghi kèm dòng giải thích và mở lại khi có mạng; kênh "Đang theo dõi", chi tiết tin, tìm kiếm, B05, B06 hiện đúng chữ ở tiêu chí nghiệm thu.
6. Chi tiết tin offline: nếu `toModel` thiếu trường cần cho phần hiển thị cơ bản (xem câu hỏi 2), chỉ sửa `ListingEntity.toModel` để dựng đủ các trường đã lưu; không đổi bảng.
7. Kiểm đăng xuất xóa Room: đọc `AuthViewModel.logout` (chỉ đọc), thử đăng xuất rồi đăng nhập tài khoản khác. Thiếu thì ghi vào issue của P2-A1.
8. Thử toàn bộ bằng một máy ảo: xem trang chủ, mở 3 tin, tìm 2 từ khóa, bật chế độ máy bay, đi qua B01 đến B06; tắt mạng giữa lúc đăng tin. Ghi bẫy vào `workflow/memory/code/room.md`.
9. Chạy các lệnh ở mục "Tự kiểm trước khi mở Pull Request".

### File dùng chung cần thêm dòng
Không có.

### Cấm đụng
- File chỉ Phát sửa: `AndroidManifest.xml`, `nav_main.xml`, `nav_admin.xml`, menu thanh điều hướng, `themes.xml`, `colors.xml`, `dimens.xml`, `strings_common.xml`, `AppDatabase`, `Converters`, mọi file Gradle, mọi file trong `firebase/`.
- Lớp `model` và `model.enums`: không sửa.
- `NetworkMonitor`, `BaseRepository` (Phát): chỉ dùng. `AuthViewModel.logout` (Ngân): chỉ kiểm, không sửa.
- Màn của module khác (chat, giao dịch, hồ sơ, quản trị): không sửa.
- `ChatDao`, `DealDao` và các entity của Hải Long: không sửa.

### Phụ thuộc
P2-B1a, P2-B1b, P2-B2a, P2-B2b, P2-B3, P2-B4a, P2-B4b, P2-B5, P2-B6a, P2-B6b, P2-B7, P2-A1 (đăng xuất xóa Room).

### Ngoài phạm vi
- Ghi dữ liệu khi offline rồi gửi sau: không hỗ trợ trong đợt này (`luong-xu-ly.md` mục 3, điểm 6).
- Đệm ảnh đầy đủ vào Room (cấm, `room-schema.md`).
- Đệm quan hệ theo dõi để xem kênh theo dõi khi offline.
- Offline của chat, giao dịch, hồ sơ: P3-C, P3-D.
- Bố cục ngang: P3-Ba.

### Tài liệu cần cập nhật
Không có, trừ khi các câu hỏi dưới đây đổi hành vi (khi đó cập nhật `room-schema.md`, `data-local.md`, `uc-b-tin-dang.md` theo R7).

### Tự kiểm trước khi mở Pull Request
- [ ] `cd PUBGApp && ./gradlew testDebugUnitTest` chạy `ListingRepositoryTest`: ca chọn nguồn và khớp từ khóa đệm qua.
- [ ] Xem trang chủ có mạng, bật chế độ máy bay, mở lại tab Trang chủ: tin vẫn hiện, có dải báo offline, không quay vô hạn.
- [ ] Chạm kênh "Đang theo dõi" khi offline: hiện "Cần có mạng để xem kênh đang theo dõi".
- [ ] Mở lại một tin đã xem khi offline: hiện tên, giá, ảnh bìa; các nút bị khóa kèm dòng giải thích; mở một tin chưa xem: "Chưa có dữ liệu để xem khi offline".
- [ ] Tìm một từ khóa khi offline: kết quả từ tin đã đệm; tab "Người dùng": báo mất mạng.
- [ ] Mở B04, nhập chữ, chọn ảnh khi offline: nút "Đăng tin" và "Lưu nháp" khóa; bật lại mạng: mở khóa, chữ không mất.
- [ ] Tắt mạng ngay sau khi bấm "Đăng tin": sau 15 giây hiện lỗi, Console không có tài liệu ghi dở.
- [ ] Đăng xuất, đăng nhập tài khoản khác, bật chế độ máy bay: trang chủ và "Tìm gần đây" rỗng.
- [ ] Chạy danh sách kiểm tra module B trong `phase-3-hoan-thien.md` và dán kết quả vào PR.
- [ ] `cd PUBGApp && ./gradlew assembleDebug testDebugUnitTest lintDebug` qua.
- [ ] `sh workflow/scripts/check-java-style.sh` không báo lỗi.
- [ ] Danh sách mọi lớp, phương thức, trường công khai mới kèm dòng tài liệu định nghĩa đã dán vào PR; tên nội bộ được ghi riêng (R16).

### Quy mô
- [x] Đây là một lát cắt dọc của một module, khoảng nửa ngày đến một ngày, một Pull Request.

### Câu hỏi cho leader
1. Thời hạn chờ 15 giây khi ghi lúc mạng rớt (`overview.md` mục 8 và 10) là việc chung của mọi repository, nhưng `BaseRepository` chưa nói có sẵn cách đặt thời hạn hay chưa. Nếu P1-04 đã có thì module B dùng; nếu chưa thì đề xuất Phát thêm một lần cho cả nhóm thay vì mỗi module tự viết.
2. Bộ nhớ đệm chi tiết tin offline chỉ có các trường của `ListingEntity` (không có `conditionValues`, `location`, `acceptedOfferTypes`, `searchKeywords`, `statusReason`, `flagged`). Xin xác nhận như vậy là đủ cho demo (mục "Tình trạng chi tiết" và bán kính không có khi offline); nếu cần thì phải đổi bảng `listing_cache` (R6, cần quyết định).
3. UC09 và UC11 (yêu cầu phi chức năng) nói kệ hàng và tin nháp xem được, UC13 nói tin đã lưu xem được khi mất mạng, nhưng `room-schema.md` chỉ có `listing_cache` của tin AVAILABLE trong bảng tin và `ListingDao` không có truy vấn theo người bán hay danh sách mã đã lưu, nên B05 và B06 không dựng được từ Room. Đề xuất: chấp nhận "Cần có mạng để xem" cho B05 và B06 khi mở mới lúc offline và sửa các yêu cầu phi chức năng của UC09, UC11, UC13; hoặc thêm bảng Room cho kệ hàng và tin đã lưu (R6, cần hỏi trước, tăng phiên bản cơ sở dữ liệu). Xin chọn.
4. UC09 yêu cầu phi chức năng "soạn thảo tiếp ngay cả khi mất mạng" mâu thuẫn với "không ghi khi offline" (`overview.md` mục 8). Cách hiểu mặc định: soạn được nội dung ngay trên biểu mẫu, nhưng "Lưu nháp" và "Đăng tin" bị khóa khi offline. Xin xác nhận.
5. `NetworkMonitor` nay có `getInstance()` nhưng `online` (`LiveData<Boolean>`) vẫn là thuộc tính private không có getter trong `util.md`, nên dải báo offline và việc mở khóa nút khi có mạng trở lại phải dựa vào `isOnline()` ở từng lần mở màn hình hoặc bấm. Nếu cần dải đổi và nút mở khóa theo thời gian thực thì Phát thêm một getter cho `online` và cập nhật `util.md`.
