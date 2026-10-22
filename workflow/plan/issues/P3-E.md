TITLE: feat(ui): bố cục ngang và chốt chất lượng cho khu quản trị
LABELS: type:feature, area:ui, module:e, priority:p2
ASSIGNEES: tahpnart8
PHASE: P3

Plan item: P3-E

### Bối cảnh
- PUBGApp là ứng dụng Android mua bán đồ cũ kết hợp mạng xã hội, viết bằng Java 17, XML Views với ViewBinding, Firebase Authentication và Cloud Firestore gói Spark (không Cloud Storage, không Kotlin).
- Module E "Quản trị" do Phát (`tahpnart8`) làm: bảng điều khiển, quản lý người dùng, duyệt xác thực tài khoản, kiểm duyệt nội dung, danh mục, giám sát giao dịch, tài khoản admin và các việc chạy nền của giao dịch.
- Issue này làm yêu cầu môn học "giao diện thích ứng khi xoay màn hình, giữ nguyên trạng thái" cho toàn bộ khu quản trị: các màn E01 đến E07 và hai màn phụ `AdminListingDetailFragment`, `AdminDealDetailFragment`. Không thêm tính năng.
- Đọc trước ba tài liệu: `workflow/plan/phase-3-hoan-thien.md` (dòng P3-E và danh sách kiểm tra cuối tệp); `workflow/docs/ui/e-quan-tri/README.md` (mục "Trạng thái đặc biệt" của từng màn); `workflow/docs/conventions/android-java.md` mục "Tài nguyên và xoay màn hình".
- Đã có trên `main`: toàn bộ P2-E1 đến P2-E7 (mọi màn của khu quản trị chạy được ở chế độ dọc), thành phần dùng chung `view_offline_banner.xml`, `view_empty_state.xml`, `view_error_state.xml` và token màu của P1.

### Mã tính năng
F-13, F-14 (Admin xử lý khiếu nại ở phần lối vào; Admin người dùng, nội dung, danh mục, giao dịch, chuyển tiền tự động, tài khoản admin). Việc này là yêu cầu môn học áp lên các màn của module E (`features.md` mục "Yêu cầu môn học"), không phải một tính năng mới.

### Mã việc kế hoạch
P3-E

### Module
E. Quản trị (Phát)

### STT trong bảng đặc tả
Không áp dụng (yêu cầu môn học cắt ngang các tính năng). Các màn thuộc STT 68 đến 82, 84, 105, 106.

### Màn hình mockup
E01, E02, E03, E04, E05, E06, E07 (chưa có ảnh), cùng hai màn phụ không có mã riêng là `AdminListingDetailFragment` và `AdminDealDetailFragment`. Màn D06 nằm trong `AdminActivity` nhưng thuộc module D, đã làm ở P3-D.

### Làm gì và vì sao
Hai yêu cầu cứng của giảng viên áp lên khu quản trị: xoay màn hình không làm mất trạng thái, và layout thích ứng khi xoay. Khu quản trị là nơi dễ hỏng nhất khi xoay, vì màn nào cũng có bộ lọc, danh sách dài và hộp thoại nhập lý do: E02 có ô tìm kiếm cùng ba bộ lọc, E03 có hộp thoại khóa tài khoản và cảnh cáo kèm lý do, E04 có ba tab và hộp thoại ẩn tin kèm lý do, E05 có biểu mẫu danh mục, E06 có chip trạng thái, E07 có hộp nhập email và hai pop up xác nhận. Mất một ô lý do đang gõ nghĩa là admin phải làm lại từ đầu, và tệ hơn, một hộp thoại dựng sai vòng đời sẽ gửi quyết định hai lần khi xoay. Sau issue này mọi màn của khu quản trị có bố cục ngang dùng được và xoay không mất bộ lọc, tab, vị trí cuộn hay nội dung đang gõ. Vì mặt công khai của ba ViewModel admin đã chốt ở `dac-ta-lop/viewmodel.md` (R20, không thêm thuộc tính công khai), nội dung đang soạn được giữ bằng `onSaveInstanceState` của Fragment và trạng thái tự giữ của các view có `android:id`; dữ liệu đã tải nằm sẵn trong ViewModel nên không đọc lại. Issue này cũng là chốt chất lượng cuối của module E: mọi phương thức của module trong phạm vi Đợt này không còn trả `DataCallback.NOT_READY`.

### Tiêu chí nghiệm thu
- [ ] Mỗi màn E01, E02, E03, E04, E05, E06, E07 và hai màn phụ có bản `layout-land` (hoặc `ConstraintLayout` đã thích ứng) dùng được trên máy ảo ở chế độ ngang: không chữ bị cắt khỏi màn, không phải cuộn ngang, nút chính vẫn bấm được, vùng cuộn dọc dùng được. Lưới lối tắt của E01 tăng số cột khi ngang.
- [ ] Xoay từng màn ở trên, ghi kết quả từng màn vào Pull Request: không tải lại dữ liệu (không có lượt đọc Firestore mới sau khi xoay, kiểm bằng Logcat hoặc Firebase Console), không mất tab đang chọn, không mất vị trí cuộn.
- [ ] E02: xoay giữa lúc đã gõ từ khóa tìm kiếm và đã chọn bộ lọc trạng thái, khoảng ngày tham gia, khu vực thì cả bốn thứ còn nguyên và danh sách không tải lại.
- [ ] E03: xoay giữa lúc hộp thoại "Khóa tài khoản" hoặc "Cảnh cáo" đang mở và đã gõ lý do thì hộp thoại còn mở, lý do còn nguyên, thời hạn khóa đang chọn còn nguyên; bấm Xác nhận chỉ ghi **một** lần (kiểm trên Firebase Console: `users/{uid}` chỉ đổi một lần, `users/{uid}/warnings` chỉ thêm một tài liệu).
- [ ] E03 khối duyệt xác thực: xoay giữa lúc đang xem hai ảnh và đã gõ lý do từ chối thì ảnh không tải lại và lý do còn nguyên.
- [ ] E04: xoay giữ đúng tab đang chọn trong ba tab (Báo cáo, Tin bị gắn cờ, Từ khóa cấm), giữ bộ lọc trạng thái báo cáo và vị trí cuộn; hộp thoại "Ẩn tin" đang mở kèm lý do sống qua xoay và chỉ ghi một lần.
- [ ] E05: xoay giữa lúc đang thêm hoặc sửa một danh mục thì tên, danh mục cha đang chọn, công tắc hiển thị và danh sách thuộc tính tình trạng đang nhập còn nguyên.
- [ ] E06: xoay giữ chip trạng thái đang chọn và vị trí cuộn; màn `AdminDealDetailFragment` giữ phần lịch sử trạng thái đã mở.
- [ ] E07: xoay giữa lúc đã gõ email vào hộp "Thêm admin" thì email còn nguyên; pop up xác nhận cấp quyền (có tên và email) và pop up xác nhận gỡ quyền sống qua xoay và chỉ ghi một lần (kiểm trên Firebase Console: `admins/{uid}` chỉ được tạo hoặc xóa một lần).
- [ ] Tài khoản không phải admin không mở được `AdminActivity`: thử bằng cách gỡ quyền admin của một tài khoản đang đăng nhập rồi mở lại ứng dụng, nút "Khu vực quản trị" trên D01 biến mất; nếu cố mở thì Security Rules từ chối mọi lượt đọc và màn hiện lỗi tiếng Việt, không hiện dữ liệu rỗng im lặng (R12).
- [ ] Bật chế độ máy bay sau khi đã mở các màn E: dải báo offline hiện, mọi nút ghi của khu quản trị bị khóa (khóa, mở khóa, cảnh cáo, duyệt hoặc từ chối xác thực, ẩn, khôi phục, gỡ tin, gỡ toàn bộ tin, thêm hoặc xóa từ khóa, lưu hoặc xóa danh mục, cấp hoặc gỡ quyền admin). Mạng rớt giữa chừng khi đang ghi: sau 15 giây hiện lỗi, màn không treo.
- [ ] Mọi thao tác mạng thất bại ở các màn E hiện câu tiếng Việt: thử từng ca và ghi vào Pull Request ca nào đã thử; không còn lỗi hiện tiếng Anh hay tên ngoại lệ.
- [ ] Bốn trạng thái của mỗi màn có danh sách (đang tải, có dữ liệu, rỗng, lỗi) hiện đúng ở cả chế độ dọc và ngang; ô trống có biểu tượng, một câu nói thiếu gì và nút đi tiếp nếu có (`giao-dien.md` mục 6).
- [ ] Không còn phương thức nào của module E trả `DataCallback.NOT_READY` trong phạm vi Đợt này: Pull Request liệt kê và ghi kết quả từng phương thức của `AdminRepository`, `CategoryRepository`, `DealRepository.releaseDueDeals`, `completeDueDeals`, `expirePendingDeals`, `listAll`, cùng mọi phương thức của `AdminUserViewModel`, `AdminContentViewModel`, `AdminDealViewModel`.
- [ ] `AutoReleaseWorker` vẫn chạy đúng sau khi xoay và sau khi đóng mở lại ứng dụng: ghi lại một lần chạy thật trong Pull Request.
- [ ] Các bản `layout-land` không viết thẳng màu, cỡ chữ, khoảng cách: chỉ dùng token của `giao-dien.md` mục 1 đến 3; ngoài các tệp layout-land và logic giữ trạng thái, không đổi hành vi hay chuỗi của P2 (R3); mọi chữ mới là string resource trong `strings_admin.xml`.
- [ ] Không có phương thức quy tắc mới, nên không có unit test mới bắt buộc; Pull Request ghi rõ như vậy.
- [ ] `cd PUBGApp && ./gradlew assembleDebug testDebugUnitTest lintDebug` và `sh workflow/scripts/check-java-style.sh` đều qua; Pull Request ghi đúng lệnh và kết quả thật, cùng thiết bị hoặc máy ảo đã thử xoay (R1).
- [ ] Pull Request liệt kê mọi lớp, phương thức, trường công khai mới kèm dòng tài liệu định nghĩa (R16); nếu không có thì ghi "không có".

### Tài liệu phải đọc
- `AGENTS.md` mục 6: R3, R12, R15, R19, R20, R22 và mục 7 (xoay màn hình, ViewBinding, chuỗi).
- `workflow/plan/phase-3-hoan-thien.md` (dòng P3-E và Danh sách kiểm tra cuối tệp).
- `workflow/plan/so-huu.md` mục 3 (E01 đến E07 và hai màn phụ), mục 4 (hành động toàn cục của `AdminActivity`), mục 5 (ba ViewModel admin, `AdminRepository`, `CategoryRepository`), mục 6.
- `workflow/docs/ui/README.md` (điều hướng, `AdminActivity` không có thanh dưới) và `workflow/docs/ui/e-quan-tri/README.md` toàn bộ.
- `workflow/docs/product/use-case/uc-e-quan-tri.md`, nhất là yêu cầu phi chức năng "Dữ liệu form giữ nguyên khi xoay màn hình" của UC43 và các quy tắc ghi log.
- `workflow/docs/conventions/giao-dien.md` mục 1 đến 6 (token màu, bốn trạng thái, bảng màu trạng thái).
- `workflow/docs/architecture/overview.md` mục 8 (xoay màn hình, offline, lỗi) và mục 10 (kịch bản chất lượng).
- `workflow/docs/conventions/android-java.md` mục "Tài nguyên và xoay màn hình".
- `workflow/docs/architecture/dac-ta-lop/viewmodel.md#adminuserviewmodel`, `#admincontentviewmodel`, `#admindealviewmodel` (để biết thuộc tính nào đã có, nhất là `loading`, `error`, `done`).
- `workflow/docs/data/security-rules.md` mục 8 (admins) cho phép thử quyền một lần.
- `workflow/memory/code/README.md`, `workflow/memory/code/firebase.md`, skill `pubg-code` và `pubg-ui`.

### Lớp, trường, trạng thái sẽ dùng
Không thêm lớp, trường hay trạng thái nghiệp vụ nào. Chỉ dùng lại những gì P2-E1 đến P2-E7 đã dùng:

- Các Fragment `AdminDashboardFragment`, `AdminUserListFragment`, `AdminUserDetailFragment`, `AdminModerationFragment`, `AdminCategoryFragment`, `AdminDealMonitorFragment`, `AdminAccountFragment`, `AdminListingDetailFragment`, `AdminDealDetailFragment`.
- Các adapter giao diện đã tạo ở P2 trong gói `ui.admin`.
- `AdminUserViewModel`, `AdminContentViewModel`, `AdminDealViewModel`: chỉ đọc các thuộc tính đã có, trong đó `loading`, `error`, `done` là nguồn của trạng thái đang tải, câu lỗi và việc đóng hộp thoại sau khi ghi xong.
- `NetworkMonitor.getInstance()`, `NetworkMonitor.isOnline()`, `SessionManager.getInstance().isAdmin()`, `DataCallback.NOT_READY`, và các thành phần dùng chung `view_offline_banner.xml`, `view_empty_state.xml`, `view_error_state.xml` (P1-06).
- Kỹ thuật giữ trạng thái: `onSaveInstanceState` của Fragment và trạng thái tự giữ của view có `android:id`.

### File được tạo hoặc sửa
- [tạo] PUBGApp/app/src/main/res/layout-land/fragment_admin_dashboard.xml
- [tạo] PUBGApp/app/src/main/res/layout-land/fragment_admin_user_list.xml
- [tạo] PUBGApp/app/src/main/res/layout-land/fragment_admin_user_detail.xml
- [tạo] PUBGApp/app/src/main/res/layout-land/fragment_admin_moderation.xml
- [tạo] PUBGApp/app/src/main/res/layout-land/fragment_admin_category.xml
- [tạo] PUBGApp/app/src/main/res/layout-land/fragment_admin_deal_monitor.xml
- [tạo] PUBGApp/app/src/main/res/layout-land/fragment_admin_account.xml
- [tạo] PUBGApp/app/src/main/res/layout-land/fragment_admin_listing_detail.xml
- [tạo] PUBGApp/app/src/main/res/layout-land/fragment_admin_deal_detail.xml
- [sửa] PUBGApp/app/src/main/java/com/nhom3/pubgapp/ui/admin/AdminDashboardFragment.java
- [sửa] PUBGApp/app/src/main/java/com/nhom3/pubgapp/ui/admin/AdminUserListFragment.java
- [sửa] PUBGApp/app/src/main/java/com/nhom3/pubgapp/ui/admin/AdminUserDetailFragment.java
- [sửa] PUBGApp/app/src/main/java/com/nhom3/pubgapp/ui/admin/AdminModerationFragment.java
- [sửa] PUBGApp/app/src/main/java/com/nhom3/pubgapp/ui/admin/AdminCategoryFragment.java
- [sửa] PUBGApp/app/src/main/java/com/nhom3/pubgapp/ui/admin/AdminDealMonitorFragment.java
- [sửa] PUBGApp/app/src/main/java/com/nhom3/pubgapp/ui/admin/AdminAccountFragment.java
- [sửa] PUBGApp/app/src/main/java/com/nhom3/pubgapp/ui/admin/AdminListingDetailFragment.java
- [sửa] PUBGApp/app/src/main/java/com/nhom3/pubgapp/ui/admin/AdminDealDetailFragment.java
- [sửa] PUBGApp/app/src/main/res/values/strings_admin.xml (chỉ nếu cần chuỗi báo lỗi tiếng Việt còn thiếu)

Chỉ tạo một layout-land hay sửa một Fragment khi màn đó thật sự cần; nếu một layout dọc đã dùng `ConstraintLayout` và đạt tiêu chí ngang thì bỏ tệp layout-land tương ứng ra khỏi Pull Request và ghi lý do. Tên gói và tên layout theo khung P1-06.

### Các bước đề nghị
Có thể làm theo thứ tự khác, nhưng không được đổi tên lớp, phương thức, trường nào.

1. Đọc `workflow/plan/phase-3-hoan-thien.md`, `workflow/docs/conventions/android-java.md` mục "Tài nguyên và xoay màn hình", skill `pubg-code` và `pubg-ui`. Chạy ứng dụng bằng một tài khoản admin, mở lần lượt chín màn của khu quản trị ở chế độ dọc và xoay từng màn, ghi lại màn nào mất dữ liệu. Danh sách đó là việc thật của issue này.
2. Với từng màn đã ghi lại, quyết định: layout dọc đã dùng `ConstraintLayout` có đạt chế độ ngang không (nếu có thì không tạo layout-land); màn nào cần bản ngang thì tạo tệp cùng tên trong `res/layout-land/`, giữ nguyên mọi `android:id` của bản dọc để ViewBinding và `onSaveInstanceState` vẫn đúng.
3. E01: lưới lối tắt tăng số cột khi ngang; danh sách "Cần xử lý" giữ vị trí cuộn, lấy dữ liệu từ `AdminUserViewModel` và `AdminContentViewModel` đã tải, không gọi lại.
4. E02: ô tìm kiếm và ba bộ lọc có `android:id`; giá trị đang chọn giữ bằng `onSaveInstanceState`, danh sách lấy từ `AdminUserViewModel.users` đã có, không gọi lại `filterUsers` khi xoay.
5. E03: dựng hộp thoại "Khóa tài khoản", "Cảnh cáo" và "Từ chối xác thực" bằng `DialogFragment` gắn vòng đời Fragment, không dựng bằng biến cục bộ; ô lý do và bộ chọn thời hạn có `android:id`. Khóa nút Xác nhận ngay sau lần bấm đầu cho tới khi `done` hoặc `error` trả về, để xoay không gửi hai lần. Hai ảnh xác thực lấy từ `AdminUserViewModel`, không đọc lại.
6. E04: tab đang chọn và bộ lọc trạng thái báo cáo giữ bằng `onSaveInstanceState`; hộp thoại "Ẩn tin" kèm lý do làm như bước 5.
7. E05: biểu mẫu danh mục (tên, danh mục cha, công tắc hiển thị, danh sách thuộc tính tình trạng) có `android:id` và giữ qua xoay; danh sách danh mục lấy từ `AdminContentViewModel.categories`.
8. E06 và `AdminDealDetailFragment`: chip trạng thái và phần lịch sử trạng thái đã mở giữ qua xoay.
9. E07: ô email có `android:id`; hai pop up xác nhận làm như bước 5, dùng `AdminUserViewModel.pendingAdmin` để hiện tên và email nên xoay không phải tìm lại theo email.
10. Rà bốn trạng thái của mọi màn có danh sách bằng `view_empty_state.xml` và `view_error_state.xml`; rà dải báo offline và việc khóa nút ghi liệt kê ở mục tiêu chí; thêm chuỗi báo lỗi còn thiếu vào `strings_admin.xml` (tiếng Việt có dấu).
11. Kiểm danh sách phương thức của module E không còn `NOT_READY`: mỗi phương thức một lần chạy thật hoặc một dòng ghi lý do chưa chạy được.
12. Thử quyền một lần: gỡ quyền admin của một tài khoản rồi mở lại ứng dụng, xác nhận nút "Khu vực quản trị" biến mất và Rules từ chối.
13. Kiểm tay các tiêu chí trên máy ảo, ghi kết quả từng màn, rồi chạy các lệnh ở mục "Tự kiểm trước khi mở Pull Request" và mở Pull Request.

### File dùng chung cần thêm dòng
Không có. `nav_admin.xml` và `strings_admin.xml` là file của Phát, nhưng issue này không thêm đích điều hướng mới.

### Cấm đụng
- `AndroidManifest.xml`: không khai báo `configChanges` cho Activity để né việc xoay, trừ khi leader yêu cầu. Các file dùng chung khác (`nav_main.xml`, `themes.xml`, `colors.xml`, `dimens.xml`, `strings_common.xml`, `AppDatabase`, `Converters`, mọi file Gradle, `firebase/`) không sửa trong issue này dù chúng thuộc Phát.
- Mọi layout, Fragment và ViewModel của các module A, B, C, D, kể cả màn D06 nằm trong `AdminActivity` (thuộc Phúc, đã làm ở P3-D).
- Hành vi nghiệp vụ của P2: không đổi chữ ký, thân repository hay ViewModel của module E ngoài những dòng cần để giữ trạng thái; không thêm thuộc tính hay phương thức công khai vào ba ViewModel admin (R20).

### Phụ thuộc
P2-E1, P2-E2a, P2-E2b, P2-E2c, P2-E2d, P2-E3a, P2-E3b, P2-E3c, P2-E4, P2-E5, P2-E6a, P2-E6b, P2-E7. Phần dải báo offline và các thành phần trạng thái dùng chung phụ thuộc P1-06. Nên làm sau P3-D để màn D06 trong `AdminActivity` đã xong, tránh hai người sửa cùng lúc trong một Activity.

### Ngoài phạm vi
- Bộ nhớ đệm Room cho các màn quản trị: `room-schema.md` không có bảng nào cho dữ liệu admin, và P3-E không có việc offline. Mất mạng thì hiện dải báo, giữ dữ liệu đã tải trong ViewModel và báo "Không có kết nối" khi đọc lỗi.
- Security Rules đầy đủ, chỉ mục ghép và kịch bản tấn công: P3-R.
- Rà test mọi quy tắc trong `trang-thai.md`: P3-T.
- Ô số liệu tổng của E01 (STT 92), nút duyệt giải ngân tay (STT 86), lọc giao dịch bất thường (STT 83), thông báo hệ thống do admin gửi (STT 99): đều là "Phát triển sau" (R17).
- Layout ngang cho màn của module khác, kể cả D06.
- Thay đổi hành vi, thêm tính năng, đổi chữ trên giao diện ngoài chuỗi báo lỗi còn thiếu.

### Tài liệu cần cập nhật
`workflow/plan/phase-3-hoan-thien.md`: dòng P3-E hiện ghi "bố cục ngang E01 đến E06", thiếu E07 và hai màn phụ, và cột phụ thuộc ghi "P2-E1 đến P2-E6" trong khi các việc đã tách thành E1, E2a đến E2d, E3a đến E3c, E4, E5, E6a, E6b, E7. Phát sửa dòng này khi dọn kế hoạch; không sửa trong Pull Request của issue này.

### Tự kiểm trước khi mở Pull Request
- [ ] `cd PUBGApp && ./gradlew assembleDebug testDebugUnitTest lintDebug` kết thúc `BUILD SUCCESSFUL`.
- [ ] `sh workflow/scripts/check-java-style.sh` không báo lỗi nào.
- [ ] Trên máy ảo với tài khoản admin, xoay từng màn trong chín màn sang ngang rồi về dọc: không có chữ bị cắt, không cuộn ngang, nút chính bấm được; ghi vào Pull Request kết quả từng màn.
- [ ] Mỗi lần xoay, mở Logcat hoặc tab Firestore của Firebase Console: không có lượt đọc mới.
- [ ] E02: gõ từ khóa, chọn đủ ba bộ lọc, xoay: cả bốn thứ còn nguyên, danh sách không tải lại.
- [ ] E03: mở hộp thoại Khóa tài khoản, gõ lý do, chọn thời hạn, xoay, bấm Xác nhận: trên Firebase Console `users/{uid}` chỉ đổi một lần và chỉ có một tài liệu mới trong `users/{uid}/warnings`.
- [ ] E04: chuyển sang tab Tin bị gắn cờ, cuộn xuống, xoay: đúng tab và đúng vị trí cuộn.
- [ ] E05: mở biểu mẫu sửa một danh mục, gõ tên và thêm một thuộc tính tình trạng, xoay: còn nguyên.
- [ ] E07: gõ email vào hộp Thêm admin, bấm Tiếp tục để pop up hiện tên và email, xoay, bấm Xác nhận: trên Firebase Console chỉ có một tài liệu `admins/{uid}` được tạo.
- [ ] Gỡ quyền admin của tài khoản đang dùng rồi mở lại ứng dụng: nút "Khu vực quản trị" trên D01 biến mất; cố mở khu quản trị thì hiện lỗi tiếng Việt, không hiện danh sách rỗng im lặng.
- [ ] Bật chế độ máy bay: mọi nút ghi của khu quản trị bị khóa, dải offline hiện.
- [ ] Mỗi phương thức trong danh sách "không còn NOT_READY" có một dòng kết quả trong Pull Request.
- [ ] Một lần chạy thật của `AutoReleaseWorker` được ghi lại trong Pull Request.
- [ ] Pull Request liệt kê mọi lớp, phương thức, trường công khai mới kèm dòng tài liệu định nghĩa (R16), hoặc ghi "không có".

### Quy mô
- [x] Đây là một lát cắt dọc của một module, khoảng nửa ngày đến một ngày, một Pull Request.

### Câu hỏi cho leader
1. `phase-3-hoan-thien.md` ghi P3-E là "bố cục ngang E01 đến E06". Issue này làm thêm E07 (`AdminAccountFragment`, màn mới của UC42) và hai màn phụ `AdminListingDetailFragment`, `AdminDealDetailFragment`, vì cả ba đều nằm trong `AdminActivity` và cũng phải xoay được. Xác nhận, và sửa dòng P3-E khi dọn kế hoạch.
2. Khu quản trị chạy trên điện thoại dọc theo mockup, nhưng admin thật sẽ hay dùng máy tính bảng. Đọc mặc định trong issue này: chỉ làm `layout-land` cho điện thoại, không làm bộ `sw600dp` riêng cho máy tính bảng. Xác nhận, hoặc nói rõ nếu buổi demo sẽ chạy trên máy tính bảng.
