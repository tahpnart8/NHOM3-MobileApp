TITLE: feat(admin): tin bị gắn cờ và từ khóa cấm
LABELS: type:feature, area:admin, module:e, priority:p1
ASSIGNEES: tahpnart8
PHASE: P2

Plan item: P2-E3c (phần ba, cuối của P2-E3; xem P2-E3a về lý do tách)

### Bối cảnh
1. PUBGApp là ứng dụng Android mua bán, trao đổi và cho tặng đồ cũ kiêm mạng xã hội, viết bằng Java 17, giao diện XML với ViewBinding, dữ liệu trên Firebase Authentication và Cloud Firestore gói Spark, bộ nhớ đệm Room.
2. Module E (Quản trị, người làm Phát) gồm các màn E01 đến E07 chạy trong `AdminActivity` và việc chạy nền của giao dịch; quyền admin không bao giờ lấy từ phía ứng dụng (R12): admin là người có tài liệu `admins/{uid}`, `SessionManager.isAdmin()` chỉ để hiện hay ẩn giao diện, quyền thật do Security Rules quyết định.
3. Issue này thêm hai tab "Tin bị gắn cờ" và "Từ khóa cấm" vào màn E04 `AdminModerationFragment`: xem tin chứa từ khóa cấm để ẩn hoặc bỏ cờ, và thêm, xóa từ khóa cấm (STT 78, UC38).
4. Đọc trước: `workflow/docs/ui/e-quan-tri/README.md` mục E04 (hai tab sau); `workflow/docs/product/use-case/uc-e-quan-tri.md` mục UC38; `workflow/docs/data/firestore-schema.md#bannedkeywords`.
5. Đã có trên `main`: khung P1, P2-E3a (`AdminModerationFragment` với TabLayout một tab "Báo cáo", `dialog_admin_reason.xml`, `AdminContentViewModel.hide`), P2-E3b; `KeywordFilter` và `BannedKeyword.matches` đã làm ở P1-03, P1-04.

### Mã tính năng
F-14 (Admin người dùng, nội dung, danh mục, giao dịch, chuyển tiền tự động, tài khoản admin)

### Mã việc kế hoạch
P2-E3, tách thành P2-E3a, P2-E3b, P2-E3c (STT 78). Xem P2-E3a về lý do tách.

### Module
E. Quản trị và nền móng (Phát)

### STT trong bảng đặc tả
78

### Màn hình mockup
E04 `AdminModerationFragment`, ảnh `workflow/docs/ui/e-quan-tri/e04-admin-kiem-duyet-noi-dung.png` (chỉ vẽ tab báo cáo). Hai tab "Tin bị gắn cờ" và "Từ khóa cấm" chưa có ảnh, làm theo README mục E04: tab gắn cờ có danh sách tin chứa từ khóa cấm (tin vẫn đang đăng), mỗi dòng hai nút "Ẩn tin" và "Bỏ cờ"; tab từ khóa có danh sách từ khóa, ô thêm từ khóa mới và nút xóa trên từng dòng.

### Làm gì và vì sao
Khi người bán đăng hoặc sửa tin, máy người đó so khớp tiêu đề và mô tả với danh sách từ khóa cấm và đặt `flagged` là true nếu trùng (`ShopViewModel` của Tú, `KeywordFilter`). Tin vẫn được đăng. Admin vào tab "Tin bị gắn cờ" để xem các tin đó và quyết định: "Ẩn tin" (lý do bắt buộc, như P2-E3a) hoặc "Bỏ cờ" khi thấy không vi phạm (chỉ admin hạ được cờ, `AdminRepository.clearFlag`). Tab "Từ khóa cấm" cho admin thêm từ khóa (lưu dạng đã bỏ dấu và viết thường), xem danh sách và xóa; so khớp theo chuỗi, không phân biệt hoa thường và dấu; từ khóa mới chỉ có hiệu lực với tin đăng mới và sửa sau đó, tin đã đăng không bị rà lại (UC38 BR3). Quyền thêm, xóa từ khóa và bỏ cờ là của admin do Security Rules quyết định; mọi người đã đăng nhập đọc được danh sách (security-rules.md mục 23) nên máy người bán gắn cờ được (R12). Về barem (`workflow/docs/product/barem-diem.md`): issue này phục vụ dòng R-Chapter 4 bằng RecyclerView (`AdminKeywordAdapter`, `AdminFlaggedAdapter`), input control (ô thêm từ khóa có kiểm tra, nút xóa trên từng dòng), hộp thoại xác nhận xóa từ khóa và hộp thoại lý do ẩn tin (menu và picker) và điều hướng người dùng (hai tab thêm vào TabLayout của E04); dòng R-Chapter 1, 2, 6, 7 bằng `DataCallback` bất đồng bộ (chương 7, async) và `NetworkMonitor` khóa nút ghi khi offline (chương 7, kiểm tra kết nối mạng); dòng R-Chapter 3 bằng unit test JUnit `AdminKeywordRulesTest`.

### Tiêu chí nghiệm thu
- [ ] Với Firebase thật: thêm từ khóa "hàng giả" thì tạo `bannedKeywords/{id}` với `keyword` là "hang gia" (đã bỏ dấu, viết thường), `createdBy` là admin đang đăng nhập, `createdAt` giờ máy chủ; danh sách tab hiện thêm dòng mới ngay.
- [ ] Từ khóa rỗng, quá ngắn (dưới 2 ký tự), quá dài (trên 60 ký tự) hoặc chứa ký tự ngoài chữ cái, chữ số, khoảng trắng: báo lỗi dưới ô nhập, giữ nguyên nội dung, không gọi mạng. Từ khóa trùng (sau khi bỏ dấu và viết thường) với một từ đã có: báo "Từ khóa đã có trong danh sách", không tạo thêm.
- [ ] Xóa từ khóa: có hộp thoại xác nhận; sau xác nhận tài liệu `bannedKeywords/{id}` bị xóa và dòng biến khỏi danh sách.
- [ ] Sau khi thêm "hang gia", đăng một tin có chữ "Hàng Giả" trong tiêu đề bằng tài khoản người bán đã xác thực (khi P2-B1 xong) thì tin có `flagged` true; sau khi xóa từ khóa, tin đăng mới chứa chữ đó không bị gắn cờ.
- [ ] Tab "Tin bị gắn cờ" (nạp bằng `loadFlagged()`, dữ liệu ở `flagged`) hiện các tin `flagged` true còn đang đăng, mỗi dòng có ảnh thu nhỏ, tên tin, ngày đăng và hai nút. "Bỏ cờ" đặt `flagged` thành false (chỉ `flagged`, `updatedAt` đổi) và dòng biến khỏi tab. "Ẩn tin" mở hộp thoại lý do bắt buộc, kết quả như P2-E3a. Bấm dòng mở `AdminListingDetailFragment`.
- [ ] Mỗi tab có đủ bốn trạng thái: đang tải, có dữ liệu, rỗng (tab gắn cờ: "Không có tin nào bị gắn cờ"; tab từ khóa: "Chưa có từ khóa cấm nào"), lỗi kèm "Thử lại". Lỗi bất kỳ (Rules từ chối, thiếu chỉ mục, mất mạng, quá 15 giây) hiện câu tiếng Việt lấy từ `error`, không im lặng; tiến trình đang tải theo `loading`; hộp thoại đóng khi `done` bật. Offline: nút ghi bị khóa, dải offline hiện.
- [ ] Tab đang chọn, ô từ khóa đang gõ, danh sách đã tải giữ qua xoay màn hình; xoay không đọc lại mạng.
- [ ] `clearFlag` chỉ đổi `flagged`; không có chuyển trạng thái của tin hay báo cáo trong issue này, ngoài nút "Ẩn tin" đã nêu ở P2-E3a (qua `AdminRepository.hideListing`, R18).
- [ ] Unit test cho hàm quy tắc của issue: `AdminKeywordRulesTest` (tên hàm ở bước 1 mục Các bước đề nghị).
- [ ] `./gradlew assembleDebug testDebugUnitTest lintDebug` và `sh workflow/scripts/check-java-style.sh` đều qua; PR ghi đúng lệnh và kết quả thật (R1).
- [ ] PR liệt kê mọi lớp, phương thức, trường công khai mới kèm dòng tài liệu định nghĩa (R16); `AdminKeywordRules`, `AdminKeywordAdapter`, `AdminFlaggedAdapter` là lớp giao diện không có trong `dac-ta-lop`, ghi vào PR như lớp mới không có dòng tài liệu.

### Tài liệu phải đọc
- `AGENTS.md` mục 6: R12, R16, R17, R18, R20, R22, R23.
- `workflow/plan/so-huu.md` mục 2, 3 (E04), 5 (`AdminRepository`, `AdminContentViewModel`, `KeywordFilter` của Phát; `ListingRepository` của Tú), 6.
- `workflow/docs/product/dac-ta-chuc-nang.md` dòng STT 78.
- `workflow/docs/ui/e-quan-tri/README.md` mục E04 (tab "Tin bị gắn cờ", "Từ khóa cấm", "Trạng thái đặc biệt").
- `workflow/docs/product/use-case/uc-e-quan-tri.md` mục UC38 (luồng, ngoại lệ, BR1 đến BR4, NFR).
- `workflow/docs/architecture/dac-ta-lop/data.md#adminrepository` (dòng `getKeywords`, `addKeyword`, `removeKeyword`, `clearFlag`, `hideListing`), `#listingrepository` (dòng `getBannedKeywords`, để biết phía người bán); `viewmodel.md#admincontentviewmodel`; `util.md#keywordfilter`, `#formatutils` (dòng `removeAccents`).
- `workflow/docs/architecture/dac-ta-lop/model.md#bannedkeyword`, `#listing` (trường `flagged`).
- `workflow/docs/data/firestore-schema.md#bannedkeywords`, `#listings`; `workflow/docs/data/security-rules.md` mục 23 (bannedKeywords), 10 (dòng "Admin bỏ cờ", "Sửa nội dung").
- `workflow/docs/architecture/luong-xu-ly.md` mục 2 (bước 3).
- `workflow/docs/conventions/giao-dien.md` mục 1 đến 6 và 8; `workflow/memory/code/README.md`, `workflow/memory/code/firebase.md`, `workflow/docs/conventions/android-java.md`, skill `pubg-code`, skill `pubg-ui`.

### Lớp, trường, trạng thái sẽ dùng
- `AdminModerationFragment` (E04, hai tab thêm).
- `AdminContentViewModel`: thuộc tính `keywords` (`LiveData<List<BannedKeyword>>`), `flagged` (`LiveData<List<Listing>>`), `loading`, `error`, `done`; `public void loadKeywords()`, `public void addKeyword(String word)`, `public void removeKeyword(String id)`, `public void clearFlag(String listingId)`, `public void loadFlagged()` (thay thân); `hide(String listingId, String reason)` (đã làm ở P2-E3a, chỉ gọi).
- `AdminRepository`: `public void getKeywords(DataCallback<List<BannedKeyword>> cb)`, `public void addKeyword(String word, DataCallback<Void> cb)`, `public void removeKeyword(String id, DataCallback<Void> cb)`, `public void clearFlag(String listingId, DataCallback<Void> cb)`, `public void getFlaggedListings(DataCallback<List<Listing>> cb)` (thay thân).
- `BannedKeyword`: `keyword`, `createdBy`, `createdAt` (kế thừa từ `BaseDocument`), `public boolean matches(String)` (chỉ để hiểu cách so khớp). `Listing`: `flagged`, `title`, `coverThumb`, `status`.
- `FormatUtils`: `public String removeAccents(String)` (bỏ dấu và viết thường; chỉ gọi).
- `BaseRepository`: `currentUserId()`, `logActivity(String action, ReportTarget targetType, String targetId)`, `fail(...)`. `NetworkMonitor`: `public static NetworkMonitor getInstance()`, thuộc tính `online`.
- `ListingStatus.AVAILABLE`.
- Firestore: `bannedKeywords/{id}` (`keyword`, `createdBy`, `createdAt`), `listings/{id}` (admin chỉ đổi `flagged` thành false cùng `updatedAt`).

### File được tạo hoặc sửa
- [sửa] PUBGApp/app/src/main/java/com/nhom3/pubgapp/ui/admin/AdminModerationFragment.java
- [tạo] PUBGApp/app/src/main/java/com/nhom3/pubgapp/ui/admin/AdminKeywordAdapter.java
- [tạo] PUBGApp/app/src/main/java/com/nhom3/pubgapp/ui/admin/AdminFlaggedAdapter.java
- [tạo] PUBGApp/app/src/main/java/com/nhom3/pubgapp/ui/admin/AdminKeywordRules.java
- [sửa] PUBGApp/app/src/main/res/layout/fragment_admin_moderation.xml (thêm hai tab và hai khối nội dung)
- [tạo] PUBGApp/app/src/main/res/layout/item_admin_keyword.xml
- [tạo] PUBGApp/app/src/main/res/layout/item_admin_flagged_listing.xml
- [sửa] PUBGApp/app/src/main/res/values/strings_admin.xml
- [sửa] PUBGApp/app/src/main/java/com/nhom3/pubgapp/viewmodel/AdminContentViewModel.java (chỉ thân `loadKeywords`, `addKeyword`, `removeKeyword`, `clearFlag`, `loadFlagged`)
- [sửa] PUBGApp/app/src/main/java/com/nhom3/pubgapp/data/AdminRepository.java (chỉ thân `getKeywords`, `addKeyword`, `removeKeyword`, `clearFlag`, `getFlaggedListings`)
- [tạo] PUBGApp/app/src/test/java/com/nhom3/pubgapp/ui/admin/AdminKeywordRulesTest.java

Tên layout theo `so-huu.md` mục 2 và khung P1-06; nếu khung đã đặt tên khác thì theo khung.

### Các bước đề nghị
Có thể làm theo thứ tự khác, nhưng không được đổi tên lớp, phương thức, thuộc tính nào.
1. Tạo `AdminKeywordRules` (thuần Java) với `public static boolean isValidKeyword(String raw)`: sau khi bỏ khoảng trắng hai đầu, độ dài từ 2 đến 60, và mọi ký tự là chữ cái (kể cả có dấu), chữ số hoặc khoảng trắng (`Character.isLetterOrDigit`, `Character.isWhitespace`); `null` thì false. Viết `AdminKeywordRulesTest` trước: `null`, rỗng, một ký tự, đúng 2 ký tự, đúng 60 và 61 ký tự, chữ có dấu, có số, có dấu `@` hoặc `%`, có khoảng trắng giữa.
2. `AdminRepository.getKeywords(DataCallback<List<BannedKeyword>> cb)`: đọc cả collection `bannedKeywords`, đổi bằng `toObject(BannedKeyword.class)`, trả qua `cb.onSuccess`; lỗi qua `fail(cb, e)`. `AdminRepository.getFlaggedListings(DataCallback<List<Listing>> cb)`: truy vấn `listings` với `flagged == true` và `status == AVAILABLE` (hai điều kiện bằng, không cần chỉ mục ghép; điều kiện `status` là cách hiểu "tin vẫn đang đăng" của README E04: Câu hỏi 1), đổi bằng `toObject(Listing.class)`, sắp `createdAt` giảm dần trên máy; lỗi qua `fail(cb, e)`.
3. `AdminRepository.addKeyword(String word, DataCallback<Void> cb)`: chuẩn hóa `word` bằng `FormatUtils.removeAccents` rồi bỏ khoảng trắng thừa hai đầu và gộp khoảng trắng liên tiếp; từ chối với câu tiếng Việt nếu rỗng; truy vấn `bannedKeywords` có `keyword` bằng giá trị chuẩn hóa, nếu có thì báo "Từ khóa đã có trong danh sách"; ngược lại tạo tài liệu với `keyword`, `createdBy` bằng `currentUserId()`, `createdAt` và `updatedAt` giờ máy chủ; sau đó `logActivity`.
4. `AdminRepository.removeKeyword(String id, DataCallback<Void> cb)`: xóa `bannedKeywords/{id}`; sau đó `logActivity`.
5. `AdminRepository.clearFlag(String listingId, DataCallback<Void> cb)`: ghi chỉ `flagged` bằng false và `updatedAt` vào `listings/{listingId}` (đúng `onlyKeys` ở security-rules.md mục 10, dòng "Admin bỏ cờ"); sau đó `logActivity`.
6. `AdminContentViewModel`: `loadKeywords()` gọi `getKeywords` và đặt vào `keywords`; `loadFlagged()` gọi `AdminRepository.getFlaggedListings` và đặt vào `flagged`; `addKeyword(String word)`, `removeKeyword(String id)`, `clearFlag(String listingId)` gọi repository tương ứng ngoài luồng chính, thành công thì bật `done` và tải lại danh sách tương ứng. Mỗi lệnh bật `loading` lúc chạy và tắt khi xong; lỗi đặt câu tiếng Việt vào `error`.
7. Layout: thêm hai tab vào TabLayout của E04. Tab "Từ khóa cấm": ô nhập kèm nút "Thêm", RecyclerView (`item_admin_keyword.xml`: từ khóa, ngày thêm, nút xóa), `view_empty_state`, `view_error_state`, tiến trình. Tab "Tin bị gắn cờ": RecyclerView (`item_admin_flagged_listing.xml`: ảnh thu nhỏ, tên, ngày, hai nút), cùng các trạng thái. Màu, khoảng cách, cỡ chữ theo token (`giao-dien.md` mục 1 đến 3); nút xóa có `contentDescription`.
8. `AdminModerationFragment`: nút "Thêm" kiểm `AdminKeywordRules.isValidKeyword`, sai thì đặt lỗi dưới ô; đúng thì gọi `addKeyword` rồi xóa ô khi thành công. Xóa từ khóa mở hộp xác nhận. "Bỏ cờ" gọi `clearFlag`; "Ẩn tin" dùng `dialog_admin_reason` rồi `hide`; bấm dòng tin sang `AdminListingDetailFragment`. Mọi nút ghi khóa khi `NetworkMonitor.getInstance().online` là false hoặc `loading` là true; hiện `error` rồi xóa nó; đóng hộp thoại khi `done` bật rồi đặt lại.
9. Chuỗi trong `strings_admin.xml` theo mẫu `admin_moderation_flagged_<what>`, `admin_moderation_keyword_<what>`: tên tab, nhãn ô, các câu lỗi (rỗng, độ dài, ký tự không hợp lệ, trùng), hộp xác nhận xóa, hai dòng báo trống.
10. Thử trên máy ảo: thêm, thêm trùng, thêm ký tự lạ, xóa; đặt tay `flagged` true cho hai tin trên Console rồi thử "Bỏ cờ" và "Ẩn tin"; xoay màn hình; bật chế độ máy bay.
11. Chạy hai lệnh kiểm ở mục "Tự kiểm trước khi mở Pull Request" và ghi kết quả vào PR.

### File dùng chung cần thêm dòng
Không có. Ghi vào mô tả PR để P3-R gộp: luật tạo, xóa `bannedKeywords` của admin và đọc của người đã đăng nhập (security-rules.md mục 23), luật "Admin bỏ cờ" của `listings` (mục 10).

### Cấm đụng
- Leader-only không có trong danh sách trên: `AndroidManifest.xml`, `nav_main.xml`, `nav_admin.xml`, `themes.xml`, `colors.xml`, `dimens.xml`, `strings_common.xml`, `AppDatabase`, `Converters`, mọi file Gradle, `firebase/`.
- Thân các phương thức khác của `AdminContentViewModel`, `AdminRepository` (E2c, E3a, E3b, E4).
- Module B (Tú): `ShopViewModel`, `ListingRepository` (kể cả `getBannedKeywords`; phía gắn cờ khi đăng tin), màn B04.
- `KeywordFilter` và `BannedKeyword` (đã xong ở P1; nếu phát hiện lỗi thì ghi vào issue, không sửa, R19).

### Phụ thuộc
P2-E3a (khung TabLayout của E04), P1-04, P1-06. Mềm: P2-B1 (kiểm tin đăng bị gắn cờ thật). Mọi tên cần dùng đã có trong tài liệu, không có phần nào bị chặn.

### Ngoài phạm vi
- Sửa từ khóa và "mức xử lý" của từ khóa: không có trong `BannedKeyword` hay README E04, xem Câu hỏi 2.
- Rà lại tin đã đăng khi thêm từ khóa mới (UC38 BR3); chặn hẳn đăng tin chứa từ khóa (chỉ gắn cờ, `luong-xu-ly.md` mục 2 bước 3); lọc từ khóa trong nhận xét đánh giá (đã bỏ theo quyết định 2026-10-22).
- Gắn cờ lúc người bán đăng hoặc sửa tin: P2-B1 (Tú).
- Bố cục ngang: P3-E.

### Tài liệu cần cập nhật
Không có, trừ khi Câu hỏi 1 hoặc 2 được trả lời bằng đổi tài liệu: khi đó cập nhật `workflow/docs/architecture/dac-ta-lop/data.md#adminrepository`, `viewmodel.md#admincontentviewmodel` và `workflow/docs/product/use-case/uc-e-quan-tri.md` mục UC38.

### Tự kiểm trước khi mở Pull Request
- [ ] Thêm từ khóa "Hàng Giả": trên Console có `bannedKeywords/{id}` với `keyword` "hang gia"; thêm lại "hang gia" báo trùng.
- [ ] Thêm từ khóa rỗng, "a", chuỗi 61 ký tự, "ab@": mỗi lần báo lỗi dưới ô, Console không có tài liệu mới.
- [ ] Xóa một từ khóa: hộp xác nhận, sau đó dòng và tài liệu biến mất.
- [ ] Tab gắn cờ: tin có `flagged` true hiện; "Bỏ cờ" đặt `flagged` false trên Console; "Ẩn tin" với lý do đặt tin `HIDDEN`.
- [ ] Tắt mạng: các nút bị khóa, dải offline hiện.
- [ ] `cd PUBGApp && ./gradlew testDebugUnitTest --tests "*AdminKeywordRulesTest"` báo các test đều qua.
- [ ] `cd PUBGApp && ./gradlew assembleDebug testDebugUnitTest lintDebug` kết thúc `BUILD SUCCESSFUL`.
- [ ] `sh workflow/scripts/check-java-style.sh` không báo lỗi.
- [ ] Xoay màn hình khi đang ở tab "Từ khóa cấm" với chữ đang gõ dở: vẫn tab đó, chữ còn nguyên, danh sách không tải lại.
- [ ] Đã liệt kê trong PR mọi lớp, phương thức, trường công khai mới kèm dòng tài liệu định nghĩa (R16), hoặc ghi "không có".

### Quy mô
- [x] Đây là một lát cắt dọc của một module, khoảng nửa ngày đến một ngày, một Pull Request.

### Câu hỏi cho leader
1. `AdminRepository.getFlaggedListings` được mô tả "Tin có `flagged` bằng true", không nói có lọc trạng thái hay không, còn README E04 viết "tin chứa từ khóa cấm (tin vẫn đang đăng)". Issue này thêm điều kiện `status == AVAILABLE` (sau khi admin ẩn tin thì tin rời tab) và sắp xếp trên máy; `firestore-schema.md` không có chỉ mục nào cho `flagged`, nên nếu muốn sắp `createdAt` ở máy chủ thì cần thêm chỉ mục. Xác nhận cách hiểu "vẫn đang đăng", và thêm chỉ mục nếu cần.
2. UC38 (luồng thay thế 2a và bước 1) nói có "Sửa" từ khóa và cột "mức xử lý", nhưng `BannedKeyword` chỉ có `keyword` và `createdBy`, Rules cấm sửa ("muốn đổi thì xóa rồi thêm"), `AdminRepository` chỉ có `addKeyword`, `removeKeyword`, và README E04 chỉ có thêm và xóa. Mô tả UC38 và NFR2 còn nhắc tin nhắn và nhận xét, trong khi lọc từ khóa chỉ chạy trên tin đăng (nhận xét đánh giá đã bỏ theo quyết định 2026-10-22). Tạm thời làm đúng README: thêm, xóa, không có sửa và không có mức xử lý. Xác nhận và sửa UC38.
3. UC38 ngoại lệ 2c nói "ký tự không hợp lệ" nhưng không định nghĩa. Tạm thời hợp lệ là chữ cái (kể cả có dấu), chữ số, khoảng trắng; độ dài 2 đến 60 theo Rules mục 23. Xác nhận.
