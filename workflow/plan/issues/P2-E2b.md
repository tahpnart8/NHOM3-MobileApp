TITLE: feat(admin): hồ sơ người dùng, lịch sử và nhật ký hoạt động
LABELS: type:feature, area:admin, module:e, priority:p1
ASSIGNEES: tahpnart8
PHASE: P2

Plan item: P2-E2b (phần hai của P2-E2; xem P2-E2a về lý do tách)

### Bối cảnh
1. PUBGApp là ứng dụng Android mua bán, trao đổi và cho tặng đồ cũ kiêm mạng xã hội, viết bằng Java 17, giao diện XML với ViewBinding, dữ liệu trên Firebase Authentication và Cloud Firestore gói Spark, bộ nhớ đệm Room.
2. Module E (Quản trị, người làm Phát) gồm các màn E01 đến E07 chạy trong `AdminActivity` và việc chạy nền của giao dịch; quyền admin không bao giờ lấy từ phía ứng dụng (R12): admin là người có tài liệu `admins/{uid}`, `SessionManager.isAdmin()` chỉ để hiện hay ẩn giao diện, quyền thật do Security Rules quyết định.
3. Issue này làm phần chỉ xem của màn E03 `AdminUserDetailFragment` (Chi tiết người dùng): đầu trang, ba số liệu, thông tin định danh, các tab lịch sử và nhật ký hoạt động (STT 69, 74, UC34).
4. Đọc trước: `workflow/docs/ui/e-quan-tri/README.md` mục E03; `workflow/docs/product/use-case/uc-e-quan-tri.md` mục UC34 (BR2 về số CCCD); `workflow/docs/architecture/dac-ta-lop/viewmodel.md#adminuserviewmodel`.
5. Đã có trên `main`: khung P1, P2-E1 (E01), P2-E2a (E02 mở E03 kèm mã người dùng, hàm `AdminUserFilterRules.maskPhone`); khung Fragment E03 trống.

### Mã tính năng
F-14 (Admin người dùng, nội dung, danh mục, giao dịch, chuyển tiền tự động, tài khoản admin)

### Mã việc kế hoạch
P2-E2, tách thành P2-E2a, P2-E2b (STT 69, 74), P2-E2c, P2-E2d. Xem P2-E2a về lý do tách.

### Module
E. Quản trị và nền móng (Phát)

### STT trong bảng đặc tả
69 (xem chi tiết hồ sơ: thông tin cá nhân, lịch sử đăng tin, giao dịch, đánh giá, vi phạm), 74 (nhật ký hoạt động)

### Màn hình mockup
E03 `AdminUserDetailFragment`, ảnh `workflow/docs/ui/e-quan-tri/e03-admin-chi-tiet-nguoi-dung.png` (chỉ vẽ phần "Lịch sử vi phạm"). Phần còn lại làm theo README mục E03 "Khác với ảnh": các tab Tin đăng, Giao dịch, Đánh giá, Nhật ký hoạt động, khối định danh (không có số CCCD). Khối "Yêu cầu xác thực tài khoản" là P2-E2d; các nút Cảnh báo, Khóa tài khoản, Mở khóa, Gỡ toàn bộ tin là P2-E2c.

### Làm gì và vì sao
Admin bấm một người ở E02 (hoặc "Xem người dùng" ở E04) và vào E03. Đầu trang cho thấy ảnh đại diện chữ cái đầu, tên, số điện thoại che một phần, ngày tham gia, trạng thái tài khoản và trạng thái xác thực. Dưới đó là ba số liệu: số giao dịch, điểm đánh giá trung bình, số lần bị báo cáo. Khối "Thông tin định danh" hiện ngày sinh, giới tính, địa chỉ thường trú; **không hiện số CCCD ở đây**: số CCCD chỉ hiện trong khối yêu cầu xác thực (P2-E2d, UC34 BR2). Năm tab "Tin đăng", "Giao dịch", "Đánh giá", "Lịch sử vi phạm" (mở sẵn như ảnh), "Nhật ký hoạt động" cho thấy toàn bộ lịch sử của tài khoản đó; bấm một dòng tin hoặc giao dịch thì sang màn chi tiết của admin. Màn chỉ xem (UC34 BR1). Mọi dữ liệu admin đọc ở đây (hồ sơ, `private/identity`, cảnh báo, nhật ký) do Security Rules cho phép riêng admin; ứng dụng không tự khai quyền (R12). Về barem (`workflow/docs/product/barem-diem.md`): issue này phục vụ dòng R-Chapter 4 bằng RecyclerView (một `AdminHistoryAdapter` dùng chung cho năm tab) và điều hướng người dùng (TabLayout năm tab, nút quay lại, điều hướng sang `AdminListingDetailFragment` và `AdminDealDetailFragment`), và dòng R-Chapter 1, 2, 6, 7 bằng `DataCallback` bất đồng bộ cho các lệnh đọc hồ sơ (chương 7, async) và `NetworkMonitor` cho dải báo offline (chương 7, kiểm tra kết nối mạng).

### Tiêu chí nghiệm thu
- [ ] Với Firebase thật: mở E03 của một tài khoản thì đọc `users/{id}`, `users/{id}/private/identity`, `users/{id}/warnings`, và các danh sách tin, giao dịch, đánh giá của tài khoản đó; mọi trường nêu ở "Làm gì và vì sao" hiện đúng giá trị.
- [ ] Số CCCD không xuất hiện ở bất kỳ chỗ nào của E03 trong issue này (kể cả khi `UserIdentity.nationalId` đã được đọc về).
- [ ] Ba số liệu: số giao dịch lấy từ `stats` (`ProfileStats.dealCount`); điểm trung bình lấy từ `selectedUser` qua `User.ratingAverage()` kèm `ratingCount` (dùng `view_rating`); số lần bị báo cáo là số báo cáo còn chờ nhắm vào người này (`ReportRepository.countByTarget`, Câu hỏi 5), nhưng `AdminUserViewModel` chưa có thuộc tính hay phụ thuộc `ReportRepository` cho số này: ô đó chờ Câu hỏi 1, hai số liệu kia làm ngay.
- [ ] Tab "Lịch sử vi phạm": mỗi dòng có ngày, loại xử lý (cảnh báo, kèm bậc) và lý do, mới nhất trước. Tab "Nhật ký hoạt động": mỗi dòng có tên hành động (`action`), đối tượng (`targetType`, `targetId`) và thời điểm, mới nhất trước.
- [ ] Tab "Tin đăng" hiện tên tin, giá, nhãn trạng thái tin (`giao-dien.md` mục 4); tab "Giao dịch" hiện tên món (`itemTitle`), giá, nhãn trạng thái giao dịch; tab "Đánh giá" hiện số sao tổng và nhận xét (ba tab này chờ Câu hỏi 1, vì chưa có thuộc tính giữ tin đăng, giao dịch, đánh giá). Bấm dòng tin sang `AdminListingDetailFragment`, dòng giao dịch sang `AdminDealDetailFragment` (hai màn do P2-E3b, P2-E5 làm; chưa có thì dòng không bấm được, không crash).
- [ ] Tài khoản đã xóa (`DELETED`): vẫn mở được, nhãn "Đã xóa", chỉ xem (README E03), và không hiện nút hành động nào (nút do P2-E2c thêm).
- [ ] Một nguồn dữ liệu lỗi thì câu tiếng Việt lấy từ `error` hiện kèm "Thử lại"; dữ liệu của các nguồn đã tải xong (`selectedUser`, `identity`, `stats`, `warnings`, `logs`) vẫn hiện, không bị xóa; phương thức của module khác còn `NOT_READY` hiện đúng lỗi đó, không giả dữ liệu. Tiến trình đang tải bật theo `loading`. Mất mạng: dải `view_offline_banner`, nội dung đã tải vẫn còn.
- [ ] Trạng thái màn hình (hồ sơ, danh sách từng tab, tab đang chọn) giữ qua xoay màn hình; xoay không đọc lại mạng.
- [ ] Không có chuyển trạng thái nào (màn chỉ xem), nên không áp `trang-thai.md`; PR ghi rõ như vậy.
- [ ] Không có phương thức quy tắc mới nên không có unit test mới; PR ghi rõ "không có quy tắc mới".
- [ ] `./gradlew assembleDebug testDebugUnitTest lintDebug` và `sh workflow/scripts/check-java-style.sh` đều qua; PR ghi đúng lệnh và kết quả thật (R1).
- [ ] PR liệt kê mọi lớp, phương thức, trường công khai mới kèm dòng tài liệu định nghĩa (R16); `AdminHistoryAdapter` là lớp giao diện không có trong `dac-ta-lop`, ghi vào PR như lớp mới không có dòng tài liệu.

### Tài liệu phải đọc
- `AGENTS.md` mục 6: R12, R16, R17, R20, R22, R23.
- `workflow/plan/so-huu.md` mục 2, 3 (E03), 5 (các repository và ViewModel nêu ở dưới, ai sở hữu), 6.
- `workflow/docs/product/dac-ta-chuc-nang.md` dòng STT 69 và 74.
- `workflow/docs/ui/e-quan-tri/README.md` mục E03 (đầu trang, ba số liệu, khối định danh, các tab, Trạng thái đặc biệt, Khác với ảnh).
- `workflow/docs/product/use-case/uc-e-quan-tri.md` mục UC34 (BR1, BR2, BR3, ngoại lệ 1a và 3a).
- `workflow/docs/architecture/dac-ta-lop/viewmodel.md#adminuserviewmodel`; `data.md#adminrepository` (dòng `getWarnings`, `getActivityLogs`), `#userrepository` (dòng `getProfile`, `getIdentity`, `getStats`), `#listingrepository` (dòng `getBySeller`), `#dealrepository` (dòng `getMyDeals`), `#reviewrepository` (dòng `getReceived`), `#reportrepository` (dòng `countByTarget`).
- `workflow/docs/architecture/dac-ta-lop/model.md#user`, `#useridentity`, `#warning`, `#activitylog`, `#listing`, `#deal`, `#review`, `#profilestats`.
- `workflow/docs/architecture/dac-ta-lop/enums.md#userstatus`, `#verificationstatus`, `#gender`, `#listingstatus`, `#dealstatus`.
- `workflow/docs/data/firestore-schema.md#users`, `#identity`, `#warnings`, `#activitylogs`; `workflow/docs/data/security-rules.md` mục 1, 3 (identity), 7 (warnings), 22 (activityLogs).
- `workflow/docs/conventions/giao-dien.md` mục 1 đến 6 và 8; `workflow/memory/code/README.md`, `workflow/memory/code/firebase.md`, `workflow/docs/conventions/android-java.md`, skill `pubg-code`, skill `pubg-ui`.

### Lớp, trường, trạng thái sẽ dùng
- `AdminUserDetailFragment` (E03).
- `AdminUserViewModel`: thuộc tính `selectedUser` (`LiveData<User>`), `identity` (`LiveData<UserIdentity>`), `stats` (`LiveData<ProfileStats>`), `warnings` (`LiveData<List<Warning>>`), `logs` (`LiveData<List<ActivityLog>>`), `loading`, `error`; `public void loadProfile(String userId)`, `public void loadActivityLog(String userId)` (thay thân). Chưa có thuộc tính nào giữ tin đăng, giao dịch, đánh giá và số báo cáo của người đang xem: Câu hỏi 1.
- `AdminRepository`: `public void getWarnings(String id, DataCallback<List<Warning>> cb)`, `public void getActivityLogs(String id, DataCallback<List<ActivityLog>> cb)` (thay thân).
- Chỉ gọi (thân của người khác, có thể còn `NOT_READY`): `UserRepository.getProfile(String uid, DataCallback<User> cb)`, `UserRepository.getIdentity(String uid, DataCallback<UserIdentity> cb)`, `UserRepository.getStats(String uid, DataCallback<ProfileStats> cb)`, `ListingRepository.getBySeller(String sellerId, DataCallback<List<Listing>> cb)`, `DealRepository.getMyDeals(String uid, DataCallback<List<Deal>> cb)`, `ReviewRepository.getReceived(String uid, DataCallback<List<Review>> cb)`, `ReportRepository.countByTarget(String targetId, DataCallback<Integer> cb)`.
- `User`: `displayName`, `phone`, `status`, `verificationStatus`, `ratingSum`, `ratingCount`, `warningCount`, `createdAt`; `ratingAverage()`. `UserIdentity`: `dateOfBirth`, `gender`, `permanentAddress` (không dùng `nationalId`). `Warning`: `level`, `reason`, `createdAt`. `ActivityLog`: `action`, `targetType`, `targetId`, `createdAt`. `Listing`: `title`, `price`, `status`. `Deal`: `itemTitle`, `agreedPrice`, `status`. `Review`: `rating`, `comment`. `ProfileStats`: `dealCount`.
- `UserStatus`, `VerificationStatus`, `ListingStatus`, `DealStatus`, `Gender`.
- `AdminUserFilterRules.maskPhone(String)` (của P2-E2a, chỉ gọi). `NetworkMonitor.getInstance()`, thuộc tính `online`.
- Firestore: `users/{id}`, `users/{id}/private/identity`, `users/{id}/warnings/{id}`, `activityLogs` (chỉ mục `userId`+`createdAt`), `listings`, `deals`, `reviews`, `reports`.

### File được tạo hoặc sửa
- [sửa] PUBGApp/app/src/main/java/com/nhom3/pubgapp/ui/admin/AdminUserDetailFragment.java
- [tạo] PUBGApp/app/src/main/java/com/nhom3/pubgapp/ui/admin/AdminHistoryAdapter.java
- [sửa] PUBGApp/app/src/main/res/layout/fragment_admin_user_detail.xml (đầu trang, ba số liệu, khối định danh, TabLayout và danh sách; chừa hai khối trống `visibility="gone"` có id cố định cho khối hành động và khối yêu cầu xác thực, do P2-E2c và P2-E2d điền)
- [tạo] PUBGApp/app/src/main/res/layout/item_admin_history_row.xml
- [sửa] PUBGApp/app/src/main/res/values/strings_admin.xml
- [sửa] PUBGApp/app/src/main/res/navigation/nav_admin.xml (chỉ nếu thiếu hành động từ E03 sang `AdminListingDetailFragment` kèm mã tin và sang `AdminDealDetailFragment` kèm mã giao dịch)
- [sửa] PUBGApp/app/src/main/java/com/nhom3/pubgapp/viewmodel/AdminUserViewModel.java (chỉ thân `loadProfile`, `loadActivityLog`)
- [sửa] PUBGApp/app/src/main/java/com/nhom3/pubgapp/data/AdminRepository.java (chỉ thân `getWarnings`, `getActivityLogs`)

Tên layout theo `so-huu.md` mục 2 và khung P1-06; nếu khung đã đặt tên khác thì theo khung.

### Các bước đề nghị
Có thể làm theo thứ tự khác, nhưng không được đổi tên lớp, phương thức, thuộc tính nào.
1. `AdminRepository.getWarnings(String id, DataCallback<List<Warning>> cb)`: đọc collection con `users/{id}/warnings`, sắp `createdAt` giảm dần (không cần chỉ mục), đổi thành `Warning` bằng `toObject(Warning.class)`, trả qua `cb.onSuccess`; lỗi qua `fail(cb, e)`.
2. `AdminRepository.getActivityLogs(String id, DataCallback<List<ActivityLog>> cb)`: truy vấn `activityLogs` với `userId == id`, sắp `createdAt` giảm dần (cần chỉ mục `userId`+`createdAt`, có trong "Chỉ mục ghép dự kiến"); giới hạn số dòng bằng hằng riêng `private static final int LOG_LIMIT` (ví dụ 100).
3. `AdminUserViewModel.loadProfile(String userId)`: bật `loading`; gọi lần lượt `UserRepository.getProfile` (đặt vào `selectedUser`), `getIdentity` (đặt vào `identity`), `getStats` (đặt vào `stats`) và `AdminRepository.getWarnings` (đặt vào `warnings`); phần `ListingRepository.getBySeller`, `DealRepository.getMyDeals`, `ReviewRepository.getReceived`, `ReportRepository.countByTarget` chờ Câu hỏi 1 vì chưa có thuộc tính nhận kết quả. Một nguồn lỗi đặt câu tiếng Việt vào `error` và không được xóa dữ liệu của nguồn khác; tắt `loading` khi nguồn cuối xong. `loadActivityLog(String userId)` gọi `AdminRepository.getActivityLogs`, đặt vào `logs`, cùng cách bật và tắt `loading`, lỗi vào `error`.
4. Layout `fragment_admin_user_detail.xml`: đầu trang (`view_detail_toolbar` có nút quay lại, `view_avatar`, tên, số điện thoại, ngày tham gia, hai `view_status_chip`), hàng ba số liệu (`view_rating` cho điểm), khối định danh, TabLayout năm tab, một RecyclerView dùng chung cho cả năm tab, `view_empty_state`, `view_error_state`, tiến trình đang tải, `view_offline_banner`, và hai khối trống chừa cho P2-E2c, P2-E2d. Màu, khoảng cách, cỡ chữ lấy từ token (`giao-dien.md` mục 1 đến 3), không viết thẳng giá trị.
5. `AdminHistoryAdapter` và `item_admin_history_row.xml`: một kiểu dòng chung gồm tiêu đề, dòng phụ, ngày và một chip trạng thái tùy chọn; Fragment đổi `Warning`, `ActivityLog`, `Listing`, `Deal`, `Review` thành dòng theo từng tab. Dòng tin và dòng giao dịch mang mã đích để điều hướng.
6. `AdminUserDetailFragment`: lấy mã người dùng từ tham số điều hướng, gọi `loadProfile(userId)` và `loadActivityLog(userId)` một lần (không gọi lại khi xoay); đổi tab thì chỉ đổi danh sách đang hiện, không đọc lại mạng. Số điện thoại qua `AdminUserFilterRules.maskPhone`. Không gán `nationalId` vào bất kỳ View nào.
7. Nhãn trạng thái: tài khoản và tin, giao dịch theo `giao-dien.md` mục 4; trạng thái xác thực `PENDING`, `APPROVED`, `REJECTED` theo mục 4; `NONE` dùng nhãn "Chưa gửi yêu cầu" (`enums.md#verificationstatus`) với màu `colorOnSurfaceVariant` (ghi vào PR).
8. Điều hướng: bấm dòng tin hoặc giao dịch gọi hành động trong `nav_admin.xml` kèm mã; nếu đích chưa có nội dung thì dòng không bấm được.
9. Chuỗi trong `strings_admin.xml` theo mẫu `admin_user_detail_<what>`: nhãn các khối và tab, ba số liệu, nhãn giới tính, "Cảnh báo bậc %d", dòng báo trống của từng tab, câu lỗi, "Thử lại".
10. Thử trên máy ảo như mục "Tự kiểm trước khi mở Pull Request"; xoay màn hình; bật chế độ máy bay.
11. Chạy hai lệnh kiểm ở mục "Tự kiểm trước khi mở Pull Request" và ghi kết quả vào PR.

### File dùng chung cần thêm dòng
Không có. Ghi vào mô tả PR để P3-R gộp: luật đọc của admin cho `users/{uid}/private/identity` (security-rules.md mục 3), `warnings` (mục 7), `activityLogs` (mục 22), đọc `deals` và `reports` của admin (mục 14, 18); chỉ mục `activityLogs`: `userId`+`createdAt`; chỉ mục `listings`: `sellerId`+`createdAt` và `sellerId`+`status`+`createdAt`; chỉ mục `deals`: `buyerId`+`updatedAt` và `sellerId`+`updatedAt`; chỉ mục `reviews`: `revieweeId`+`createdAt`.

### Cấm đụng
- Leader-only không có trong danh sách trên: `AndroidManifest.xml`, `nav_main.xml`, `themes.xml`, `colors.xml`, `dimens.xml`, `strings_common.xml`, `AppDatabase`, `Converters`, mọi file Gradle, `firebase/`.
- Thân các phương thức khác của `AdminUserViewModel` và `AdminRepository` (E2a, E2c, E2d, E7): `filterUsers`, `lock`, `unlock`, `warn`, `loadVerificationRequests`, `reviewVerification`, `loadAdmins`, `grantAdmin`, `revokeAdmin`, `listUsers`, `lockUser`, `unlockUser`, `warnUser`...
- Phương thức của module khác chỉ gọi, không sửa: `UserRepository.getProfile`, `getIdentity`, `getStats` (Ngân, Phúc), `ListingRepository.getBySeller` (Tú), `DealRepository.getMyDeals` (Hải Long), `ReviewRepository.getReceived` (Phúc).
- `AdminListingDetailFragment` (P2-E3b) và `AdminDealDetailFragment` (P2-E5): chỉ điều hướng tới, không sửa.

### Phụ thuộc
P2-E2a (nav E02 sang E03, `maskPhone`), P1-04, P1-06. Mềm: P2-E3b, P2-E5 (hai màn đích), và các thân của module khác nêu ở trên. Ba tab Tin đăng, Giao dịch, Đánh giá và ô số lần bị báo cáo bị chặn bởi Câu hỏi 1 (R20: thiếu thuộc tính thì gắn nhãn `blocked` cho phần đó); đầu trang, số giao dịch, điểm đánh giá, khối định danh, tab Lịch sử vi phạm và Nhật ký hoạt động làm được ngay.

### Ngoài phạm vi
- Khóa, mở khóa, cảnh báo, gỡ toàn bộ tin (P2-E2c); khối yêu cầu xác thực, số CCCD, ảnh giấy tờ, Duyệt và Từ chối (P2-E2d).
- Sửa bất kỳ dữ liệu nào của người dùng: không làm (UC34 BR1).
- Ghi nhật ký mỗi lượt admin xem hồ sơ (UC34 BR3): xem Câu hỏi 6; nhật ký thao tác admin có giao diện là STT 100, Phát triển sau.
- Bố cục ngang: P3-E.

### Tài liệu cần cập nhật
Không có, trừ khi Câu hỏi 1 được trả lời bằng thêm thuộc tính: khi đó cập nhật `workflow/docs/architecture/dac-ta-lop/viewmodel.md#adminuserviewmodel`.

### Tự kiểm trước khi mở Pull Request
- [ ] Mở E03 của một tài khoản có đủ dữ liệu (tin, giao dịch, đánh giá, cảnh báo): đầu trang, ba số liệu, định danh, năm tab đều đúng giá trị; không thấy số CCCD ở đâu.
- [ ] Chuyển lần lượt năm tab: mỗi tab hiện danh sách của nó; tab không có dữ liệu hiện dòng báo trống, không hiện màn trắng.
- [ ] Bấm một dòng tin và một dòng giao dịch: sang đúng màn chi tiết hoặc không bấm được nếu màn đó chưa có, không crash.
- [ ] Mở E03 của tài khoản `DELETED`: nhãn "Đã xóa", không có nút hành động.
- [ ] Bật chế độ máy bay rồi mở lại màn: dải offline hiện, hoặc câu lỗi tiếng Việt kèm "Thử lại" cho khối chưa tải được.
- [ ] `cd PUBGApp && ./gradlew assembleDebug testDebugUnitTest lintDebug` kết thúc `BUILD SUCCESSFUL`.
- [ ] `sh workflow/scripts/check-java-style.sh` không báo lỗi.
- [ ] `grep -rn "nationalId" PUBGApp/app/src/main/java/com/nhom3/pubgapp/ui/admin/AdminUserDetailFragment.java` không ra dòng nào.
- [ ] Xoay màn hình khi đang ở tab thứ ba: vẫn tab thứ ba, danh sách còn nguyên, không đọc lại mạng.
- [ ] Đã liệt kê trong PR mọi lớp, phương thức, trường công khai mới kèm dòng tài liệu định nghĩa (R16), hoặc ghi "không có".

### Quy mô
- [x] Đây là một lát cắt dọc của một module, khoảng nửa ngày đến một ngày, một Pull Request.

### Câu hỏi cho leader
1. `AdminUserViewModel` nay có `selectedUser`, `identity`, `stats`, `warnings`, `logs`, nhưng `loadProfile` vẫn được mô tả đọc "tin đăng, giao dịch, đánh giá" của người dùng mà không có thuộc tính nào nhận chúng, và `ReportRepository` không có trong "Dùng tới" của `AdminUserViewModel` nên không đọc được số báo cáo. Ba tab Tin đăng, Giao dịch, Đánh giá và ô "số lần bị báo cáo" thiếu chỗ giữ dữ liệu. Đề xuất bổ sung vào `viewmodel.md#adminuserviewmodel`: `userListings` (`LiveData<List<Listing>>`), `userDeals` (`LiveData<List<Deal>>`), `userReviews` (`LiveData<List<Review>>`), `reportCount` (`LiveData<Integer>`) và `ReportRepository` vào "Dùng tới"; tên do leader chốt. Việc này chặn bốn phần nói trên.
2. `UserRepository.getStats` không có người sở hữu trong `so-huu.md` mục 5 (module D cũng đã hỏi, đề xuất giao Phúc). Nếu chưa làm thì số giao dịch của E03 hiện lỗi `NOT_READY`.
3. `ListingRepository.getBySeller` thêm điều kiện `status in [AVAILABLE, RESERVED, SOLD]` với người xem khác chính chủ, nên admin không thấy tin DRAFT, HIDDEN, REMOVED của người này, trong khi Rules cho admin đọc mọi trạng thái và admin cần thấy tin đã ẩn, đã gỡ để xét vi phạm. Tạm thời hiện đúng những gì `getBySeller` trả. Cần quyết: cho admin đọc đủ (ví dụ nhận biết người gọi là admin trong `getBySeller`, hoặc thêm một phương thức riêng) hay chấp nhận thiếu.
4. `DealRepository.getMyDeals(String uid, ...)` mô tả "giao dịch mà người dùng là người mua hoặc người bán" và "ghi vào Room". Tạm thời truyền mã người đang xem hồ sơ; cần xác nhận phương thức chạy được với `uid` khác người đăng nhập khi người gọi là admin, và việc ghi Room không đưa giao dịch của người khác vào bộ nhớ đệm của admin.
5. "Số lần bị báo cáo" ở E03: `ReportRepository.countByTarget` chỉ đếm báo cáo còn OPEN. Tạm thời hiện số báo cáo còn chờ với nhãn "Báo cáo chờ xử lý". Nếu cần tổng số báo cáo từng nhận thì phải thêm phương thức.
6. UC34 BR3 nói mọi lượt admin xem hồ sơ chi tiết được ghi log, nhưng `AdminRepository.getWarnings`, `getActivityLogs` và `UserRepository.getProfile` không có bước ghi log, và STT 100 (nhật ký thao tác admin) là Phát triển sau. Tạm thời không ghi log lượt xem. Xác nhận. (Lưu ý `BaseRepository.logActivity` nhận `ReportTarget` cho `targetType`, chỉ có `LISTING`, `USER`, `MESSAGE`.)
7. UC34 ngoại lệ 3a nói tài khoản đã xóa thì "báo không còn khả dụng", còn README E03 nói tài khoản đã xóa "chỉ xem". Tạm thời theo README (mở được, chỉ xem, nhãn "Đã xóa"). Xác nhận.
8. `viewmodel.md` ghi `identity` "chỉ hiện trong khối duyệt xác thực", còn README E03 có khối "Thông tin định danh" riêng ở đầu trang (ngày sinh, giới tính, địa chỉ thường trú, không có CCCD) và số CCCD chỉ nằm trong khối yêu cầu xác thực. Issue này theo README. Xác nhận, và sửa chữ ở `viewmodel.md`.
