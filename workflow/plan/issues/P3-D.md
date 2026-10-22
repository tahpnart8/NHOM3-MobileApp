TITLE: feat(ui): bố cục ngang và giữ nội dung đang soạn của module D
LABELS: type:feature, area:ui, module:d, priority:p2
ASSIGNEES: PmSubin
PHASE: P3

Plan item: P3-D

### Bối cảnh
- PUBGApp là ứng dụng Android mua bán đồ cũ kết hợp mạng xã hội, viết bằng Java 17, XML Views với ViewBinding, Firebase Authentication và Cloud Firestore gói Spark (không Cloud Storage, không Kotlin).
- Module D "Hồ sơ và tin cậy" do Phúc (`PmSubin`) làm: hồ sơ cá nhân và công khai, uy tín, theo dõi, gửi yêu cầu xác thực tài khoản, báo cáo, khiếu nại, và phía admin xử lý khiếu nại.
- Issue này làm yêu cầu môn học "giao diện thích ứng khi xoay màn hình, giữ nguyên trạng thái" cho các màn D01 đến D06 của module D: bố cục ngang và giữ nội dung đang soạn (biểu mẫu xác thực, báo cáo, khiếu nại, quyết định của admin). Không thêm tính năng.
- Đọc trước ba tài liệu: `workflow/plan/phase-3-hoan-thien.md` (dòng P3-D và danh sách kiểm tra); `workflow/docs/ui/d-ho-so-tin-cay/README.md` (mục "Trạng thái đặc biệt" của từng màn); `workflow/docs/conventions/android-java.md` mục "Tài nguyên và xoay màn hình".
- Đã có trên `main`: toàn bộ P2-D1a đến P2-D6b (mọi màn D01 đến D07 chạy được ở chế độ dọc), mọi ViewModel có `loading`, `error`, `done` và getter công khai, thành phần dùng chung `view_offline_banner.xml` và token màu của P1.

### Mã tính năng
F-11, F-12, F-13 (Hồ sơ công khai, uy tín, theo dõi; Báo cáo và khiếu nại; Admin xử lý khiếu nại). Việc này là yêu cầu môn học áp lên các màn của module D (`features.md` mục "Yêu cầu môn học"), không phải một tính năng mới.

### Mã việc kế hoạch
P3-D

### Module
D. Hồ sơ và tin cậy (Phúc)

### STT trong bảng đặc tả
Không áp dụng (yêu cầu môn học cắt ngang các tính năng). Các màn thuộc STT 7, 8, 39, 60 đến 66, 73 (phía người dùng), 87 đến 91.

### Màn hình mockup
D01, D02, D03, D04, D05, D06 (cả `AdminComplaintListFragment` và `AdminComplaintDetailFragment`). D07 không nằm trong P3-D theo kế hoạch (xem Câu hỏi 1).

### Làm gì và vì sao
Hai yêu cầu cứng của giảng viên áp lên module D: xoay màn hình không làm mất trạng thái, và layout thích ứng khi xoay. Sau issue này sáu màn của module D có bố cục ngang dùng được (không cắt nội dung, không phải cuộn ngang) và khi xoay màn hình: không đọc lại dữ liệu đã tải, không mất tab đang chọn và vị trí cuộn, không mất nội dung người dùng đang soạn dở: số CCCD và hai ảnh của biểu mẫu xác thực tài khoản ở D01, lý do và mô tả (cùng ảnh đính kèm nếu đã có) của biểu mẫu báo cáo D04, lý do, mô tả và danh sách ảnh bằng chứng của biểu mẫu khiếu nại D05, lời giải thích gửi hai bên, ghi chú nội bộ và mức ưu tiên đang chọn của phần quyết định ở D06; câu lỗi đã hiện và tín hiệu `done` đã dùng không hiện lại sau khi xoay. Việc này chỉ đóng gói lại ở cuối giai đoạn, nên mọi màn của module D phải xong ở P2 trước (P2-D1a đến P2-D6b). Vì ViewModel của module D không có thuộc tính công khai cho nội dung đang soạn (R20, không thêm thuộc tính công khai), nội dung đang soạn được giữ bằng trạng thái lưu của Fragment (`onSaveInstanceState`) và trạng thái tự giữ của các view có `android:id`; dữ liệu đã tải thì nằm sẵn trong ViewModel. Một số phần đã làm sẵn ở P2 (biểu mẫu xác thực, biểu mẫu báo cáo và khiếu nại giữ ảnh); issue này kiểm lại toàn bộ và làm nốt chỗ còn thiếu, cùng các bản `layout-land`. Nội dung biểu mẫu cũng phải sống khi ứng dụng bị thoát tạm thời (UC31 NFR3). Issue này phục vụ các dòng barem R-Chapter 5 (tài nguyên thay thế `res/layout-land/`, layout thích ứng với hướng màn hình, token trong `giao-dien.md`) và R-Chapter 1, 2, 6, 7 (vòng đời Activity và Fragment, giữ trạng thái khi xoay bằng ViewModel và `onSaveInstanceState`, kiểm tra kết nối mạng bằng `NetworkMonitor`).

### Tiêu chí nghiệm thu
- [ ] Mỗi màn D01, D02, D03, D04, D05, D06 danh sách và D06 chi tiết có bản `layout-land` (hoặc `ConstraintLayout` đã thích ứng) dùng được trên máy ảo ở chế độ ngang: không chữ bị cắt khỏi màn, không phải cuộn ngang, nút chính vẫn bấm được, vùng cuộn dọc dùng được. Lưới tin của D02 và D03 tăng số cột ở chế độ ngang.
- [ ] Xoay từng màn trong sáu màn trên (D06 tính hai màn), ghi lại kết quả cho từng màn trong PR: không tải lại dữ liệu (không có lượt đọc Firestore mới sau khi xoay, kiểm bằng Logcat hoặc Firebase Console), không mất tab đang chọn, không mất vị trí cuộn.
- [ ] Xoay màn hình không làm hiện lại câu lỗi đã hiện (`error`) và không kích hoạt lần thứ hai việc đóng màn hay quay lại theo `done`: dùng đúng cách đặt lại mà Câu hỏi 2 của P2-D1a chốt.
- [ ] D01: xoay giữa lúc đã gõ số CCCD và chọn một hoặc hai ảnh của biểu mẫu xác thực thì số CCCD, ảnh xem trước và nút "Gửi yêu cầu" còn đúng; đóng ứng dụng tạm thời (Home rồi mở lại, hoặc tiến trình bị hệ thống thu hồi) cũng vậy. Hộp thoại xác nhận "Đăng xuất" đang mở vẫn mở sau khi xoay, và đồng ý chỉ đăng xuất một lần.
- [ ] D02 và D03 giữ tab, danh sách và trạng thái nút Theo dõi qua xoay.
- [ ] D04: xoay giữa lúc đã chọn một lý do và gõ mô tả (và đã chọn ảnh) thì lý do đã chọn, nút "Gửi báo cáo" bật hay khóa đúng và nội dung còn nguyên. Mất mạng khi gửi: lý do, mô tả, ảnh còn nguyên.
- [ ] D05: xoay giữa lúc đã chọn lý do, gõ mô tả và đã chọn 1 đến 5 ảnh bằng chứng thì tất cả còn nguyên, kể cả thứ tự và số lượng ảnh (dùng `onSaveInstanceState` lưu danh sách `Uri`; không đọc lại ảnh từ Firestore). Thẻ giao dịch không đọc lại.
- [ ] D06 chi tiết: xoay giữa lúc đã gõ "Lời giải thích gửi hai bên", đã gõ "Ghi chú nội bộ" và đã chọn mức ưu tiên thì ba thứ còn nguyên; hộp thoại xác nhận quyết định đang mở vẫn mở sau khi xoay và nút đồng ý vẫn gửi đúng một lần (không gửi hai lần). D06 danh sách giữ chip đang chọn và vị trí cuộn.
- [ ] Khách chưa đăng nhập vẫn bị đưa tới A01 (`action_global_sign_in`) khi cố mở D01, D02, D03, D04 (kể cả khi đang ở chế độ ngang); sau "Đăng xuất" ở D01 ứng dụng về B01 chế độ khách (`action_global_tab` về Trang chủ, không về A01), kể cả khi xoay giữa lúc xác nhận.
- [ ] Bật chế độ máy bay sau khi đã mở các màn D: dải báo offline hiện, mọi nút ghi của module D (Gửi yêu cầu xác thực, Theo dõi, Gửi báo cáo, Hủy báo cáo, Gửi khiếu nại, Rút khiếu nại, Bổ sung bằng chứng, các nút quyết định, Lưu ghi chú, đổi mức ưu tiên) bị khóa. Mạng rớt giữa chừng khi đang ghi: sau 15 giây hiện lỗi, màn không treo.
- [ ] Mọi thao tác mạng thất bại ở các màn D hiện câu tiếng Việt (đọc hồ sơ, đếm số liệu, đọc đánh giá, gửi yêu cầu xác thực, gửi báo cáo, tạo khiếu nại, quyết định, tải ảnh): thử từng ca và ghi vào PR ca nào đã thử; không còn lỗi hiện tiếng Anh hay tên ngoại lệ.
- [ ] Không còn phương thức nào của module D trả `DataCallback.NOT_READY` trong phạm vi Đợt này (điều kiện hoàn thành P2): PR liệt kê các phương thức của module D kèm kết quả kiểm: `UserRepository.getStats` (nếu giao cho Phúc), `getVerification` (nếu giao cho Phúc), `requestVerification`, `follow`, `unfollow`, `getFollowingIds`; `ReviewRepository.getReceived`, `getGiven`; `ReportRepository.create`, `cancel`, `getMyReports`, `exportCsv`; mọi phương thức của `ComplaintRepository`; `ProfileViewModel.loadProfile`, `loadUserListings` và `loadVerification` (nếu giao cho Phúc), `follow`, `unfollow`, `requestVerification`; `TrustViewModel.loadReviews`, `submitReport`, `cancelReport`, `loadMyReports`, `exportReports`, `submitComplaint`, `addEvidence`, `loadMyComplaints`, `loadComplaint`, `withdrawComplaint`; mọi phương thức của `AdminComplaintViewModel`.
- [ ] Tài khoản thường không thấy nút "Khu vực quản trị" ở D01 và không đọc được `complaints/*/adminNote/*` (kiểm bằng tay một lần, ghi kết quả; kiểm tra đầy đủ thuộc P3-R).
- [ ] Các bản `layout-land` không viết thẳng màu, cỡ chữ, khoảng cách: chỉ dùng token của `giao-dien.md` mục 1 đến 3; ngoài các tệp layout-land và logic giữ trạng thái, không đổi hành vi hay chuỗi của P2 (R3); id view giữ nguyên dạng `<loại><Việc>` của bản dọc; mọi chữ mới là string resource trong `strings_profile.xml`, khóa theo `<module>_<màn>_<việc>`.
- [ ] Không có phương thức quy tắc mới, nên không có unit test mới bắt buộc; PR ghi rõ như vậy.
- [ ] `cd PUBGApp && ./gradlew assembleDebug testDebugUnitTest lintDebug` và `sh workflow/scripts/check-java-style.sh` đều qua; PR ghi đúng lệnh và kết quả thật, cùng thiết bị hoặc máy ảo đã thử xoay (R1).
- [ ] PR liệt kê mọi lớp, phương thức, trường công khai mới kèm dòng tài liệu định nghĩa (R16); nếu không có thì ghi "không có".

### Tài liệu phải đọc
- `AGENTS.md` mục 6: R3, R15, R19, R20 và mục 7 (xoay màn hình, ViewBinding, chuỗi).
- `workflow/plan/phase-3-hoan-thien.md` (dòng P3-D và Danh sách kiểm tra cuối tệp).
- `workflow/plan/so-huu.md` mục 3 (D01 đến D07), mục 4 (`action_global_sign_in`, `action_global_tab`), mục 5 (`ProfileViewModel`, `TrustViewModel`, `AdminComplaintViewModel` của Phúc), mục 6.
- `workflow/docs/product/barem-diem.md` (dòng barem mà issue này phục vụ, nhất là các dòng chương 2 và chương 5).
- `workflow/docs/ui/README.md` (điều hướng, thanh dưới, chế độ khách) và `workflow/docs/ui/d-ho-so-tin-cay/README.md` toàn bộ (mục "Trạng thái đặc biệt" của từng màn).
- `workflow/docs/product/use-case/uc-d-ho-so-tin-cay.md` (NFR3 của UC31 và UC33) và `uc-c-giao-dich.md` (NFR2 của UC24).
- `workflow/docs/conventions/giao-dien.md` mục 1 đến 6.
- `workflow/docs/architecture/overview.md` mục 8 (xoay màn hình, offline, lỗi) và mục 10 (kịch bản chất lượng).
- `workflow/docs/conventions/android-java.md` mục "Tài nguyên và xoay màn hình".
- `workflow/docs/architecture/dac-ta-lop/viewmodel.md#profileviewmodel`, `#trustviewmodel`, `#admincomplaintviewmodel` (để biết thuộc tính nào đã có).
- `workflow/docs/data/security-rules.md` mục 20 (adminNote) cho phép thử một lần.
- `workflow/memory/code/README.md`, `workflow/memory/code/firebase.md`, skill `pubg-code` và `pubg-ui`.

### Lớp, trường, trạng thái sẽ dùng
Không thêm lớp, trường hay trạng thái nghiệp vụ nào. Chỉ dùng lại những gì P2-D1a đến P2-D6b đã dùng:

- Các Fragment `MyProfileFragment`, `UserProfileFragment`, `SellerTrustFragment`, `ReportFragment`, `CreateComplaintFragment`, `AdminComplaintListFragment`, `AdminComplaintDetailFragment`.
- Các adapter giao diện đã tạo ở P2 (`ProfileListingAdapter`, `ReviewAdapter`, `EvidenceThumbAdapter`, `AdminComplaintAdapter`, `AdminChatLogAdapter`).
- `NetworkMonitor.getInstance().isOnline()`, `SessionManager.getInstance().isLoggedIn()`, `DataCallback.NOT_READY` và dải báo offline dùng chung (P1-06); hành động toàn cục `action_global_sign_in` và `action_global_tab`.
- Các LiveData `loading`, `error`, `done` và getter công khai của `ProfileViewModel`, `TrustViewModel`, `AdminComplaintViewModel` (chỉ quan sát).
- Kỹ thuật giữ trạng thái: `onSaveInstanceState` của Fragment và trạng thái tự giữ của view có `android:id`.

### File được tạo hoặc sửa
- [tạo] PUBGApp/app/src/main/res/layout-land/fragment_profile_my_profile.xml
- [tạo] PUBGApp/app/src/main/res/layout-land/fragment_profile_user_profile.xml
- [tạo] PUBGApp/app/src/main/res/layout-land/fragment_trust_seller_trust.xml
- [tạo] PUBGApp/app/src/main/res/layout-land/fragment_trust_report.xml
- [tạo] PUBGApp/app/src/main/res/layout-land/fragment_trust_create_complaint.xml
- [tạo] PUBGApp/app/src/main/res/layout-land/fragment_trust_admin_complaint_list.xml
- [tạo] PUBGApp/app/src/main/res/layout-land/fragment_trust_admin_complaint_detail.xml
- [sửa] PUBGApp/app/src/main/java/com/nhom3/pubgapp/ui/profile/MyProfileFragment.java
- [sửa] PUBGApp/app/src/main/java/com/nhom3/pubgapp/ui/profile/UserProfileFragment.java
- [sửa] PUBGApp/app/src/main/java/com/nhom3/pubgapp/ui/trust/SellerTrustFragment.java
- [sửa] PUBGApp/app/src/main/java/com/nhom3/pubgapp/ui/trust/ReportFragment.java
- [sửa] PUBGApp/app/src/main/java/com/nhom3/pubgapp/ui/trust/CreateComplaintFragment.java
- [sửa] PUBGApp/app/src/main/java/com/nhom3/pubgapp/ui/admin/complaint/AdminComplaintListFragment.java
- [sửa] PUBGApp/app/src/main/java/com/nhom3/pubgapp/ui/admin/complaint/AdminComplaintDetailFragment.java
- [sửa] PUBGApp/app/src/main/res/values/strings_profile.xml (chỉ nếu cần chuỗi báo lỗi tiếng Việt còn thiếu)

Chỉ tạo một layout-land hay sửa một Fragment khi màn đó thật sự cần; nếu một layout dọc đã dùng `ConstraintLayout` và đạt tiêu chí ngang thì bỏ tệp layout-land tương ứng ra khỏi PR và ghi lý do. Tên gói và tên layout theo khung P1-06.

### Các bước đề nghị
Có thể làm theo thứ tự khác, nhưng không được đổi tên lớp, phương thức, trường nào.

1. Đọc `workflow/plan/phase-3-hoan-thien.md`, `workflow/docs/conventions/android-java.md` mục "Tài nguyên và xoay màn hình", skill `pubg-code` và `pubg-ui`. Chạy ứng dụng trên máy ảo và xoay từng màn D01 đến D06 ở chế độ dọc hiện có để ghi lại màn nào mất dữ liệu (đây là danh sách việc thật).
2. Với từng màn đã ghi lại, quyết định: màn đã dùng `ConstraintLayout` có đạt chế độ ngang không (nếu có thì không tạo layout-land); màn nào cần bản `layout-land` thì tạo tệp cùng tên trong `res/layout-land/`, giữ nguyên mọi `android:id` của bản dọc để ViewBinding và `onSaveInstanceState` vẫn đúng.
3. D01 và D02: bản ngang cho đầu trang và danh sách (đặt hai cột nếu cần); lưới tin của D02 và D03 tăng số cột khi ngang.
4. D01: kiểm biểu mẫu xác thực: số CCCD tự giữ nhờ `android:id`, hai `Uri` ảnh giữ bằng `onSaveInstanceState` (đã làm ở P2-D1b, nếu thiếu thì bổ sung); hộp thoại xác nhận "Đăng xuất" sống qua xoay (dùng hộp thoại gắn với vòng đời Fragment, không tự dựng bằng biến cục bộ).
5. D03 và D02: tab đang chọn, danh sách và vị trí cuộn lấy từ ViewModel hoặc được Fragment giữ; không gọi tải lại khi `ViewModel` đã có dữ liệu.
6. D04: các `RadioButton` lý do và ô mô tả có `android:id`; danh sách `Uri` ảnh giữ bằng `onSaveInstanceState`.
7. D05: danh sách `Uri` ảnh bằng chứng giữ bằng `onSaveInstanceState`; thẻ giao dịch lấy từ ViewModel, không đọc lại.
8. D06 chi tiết: ô "Lời giải thích gửi hai bên", ô "Ghi chú nội bộ" và bộ chọn mức ưu tiên có `android:id`; hộp thoại xác nhận quyết định gắn với vòng đời Fragment để khi xoay không gửi hai lần (khóa nút đồng ý sau lần bấm đầu cho tới khi có kết quả).
9. Mọi màn: kiểm Fragment chỉ hiện `error` một lần và chỉ phản ứng với `done` một lần sau khi xoay, theo cách đặt lại mà Câu hỏi 2 của P2-D1a chốt.
10. Kiểm dải báo offline và việc khóa các nút ghi liệt kê ở mục tiêu chí; thêm chuỗi báo lỗi còn thiếu vào `strings_profile.xml` (tiếng Việt có dấu).
11. Kiểm danh sách phương thức của module D không còn `NOT_READY` (bước liệt kê ở mục tiêu chí): mỗi phương thức một lần chạy thật hoặc một dòng ghi lý do chưa chạy được.
12. Kiểm tay các tiêu chí ở trên trên máy ảo, ghi kết quả từng màn vào PR; chạy các lệnh ở mục "Tự kiểm trước khi mở Pull Request" rồi mở PR.

### File dùng chung cần thêm dòng
Không có.

### Cấm đụng
- Leader-only: `AndroidManifest.xml` (không khai báo `configChanges` để tránh giữ Activity khi xoay, trừ khi leader yêu cầu), `nav_main.xml`, `nav_admin.xml`, `themes.xml`, `colors.xml`, `dimens.xml`, `strings_common.xml`, `AppDatabase`, `Converters`, mọi file Gradle, `firebase/`.
- Mọi layout, Fragment và ViewModel của các module A, B, C, E, kể cả màn mà module D chỉ đi tới (A04, B03, C03...).
- Hành vi nghiệp vụ của P2: không đổi chữ ký, thân repository hay ViewModel của module D ngoài những dòng cần để giữ trạng thái; không thêm thuộc tính hay phương thức công khai vào `ProfileViewModel`, `TrustViewModel`, `AdminComplaintViewModel` (R20).
- `MyReportsFragment` (D07): xem Câu hỏi 1.

### Phụ thuộc
P2-D1a, P2-D1b, P2-D2, P2-D3, P2-D4a, P2-D4b, P2-D5a, P2-D5b, P2-D6a, P2-D6b (theo `phase-3-hoan-thien.md`: P2-D1 đến P2-D6). Phần dải báo offline và khóa nút ghi phụ thuộc thành phần dùng chung của P1-06.

### Ngoài phạm vi
- Bộ nhớ đệm Room cho màn của module D (Câu hỏi 4 của P2-D1a): `room-schema.md` không có bảng cho hồ sơ, đánh giá, báo cáo, khiếu nại.
- Security Rules đầy đủ, chỉ mục ghép và kịch bản tấn công: P3-R (Phát).
- Rà test mọi quy tắc trong `trang-thai.md`: P3-T (Phát, chủ module bổ sung).
- Layout ngang cho màn của module khác; màn D07 (xem Câu hỏi 1).
- Thay đổi hành vi, thêm tính năng, đổi chữ trên giao diện ngoài chuỗi báo lỗi còn thiếu.

### Tài liệu cần cập nhật
Không có, trừ khi phải đổi hành vi. Nếu Câu hỏi 1 được trả lời bằng thay đổi phạm vi, Phát cập nhật `workflow/plan/phase-3-hoan-thien.md`.

### Tự kiểm trước khi mở Pull Request
- [ ] `cd PUBGApp && ./gradlew assembleDebug testDebugUnitTest lintDebug` kết thúc `BUILD SUCCESSFUL`.
- [ ] `sh workflow/scripts/check-java-style.sh` không báo lỗi nào.
- [ ] Trên máy ảo, xoay từng màn D01 đến D06 sang ngang rồi về dọc: không có chữ bị cắt, không cuộn ngang, nút chính bấm được; ghi vào PR kết quả từng màn.
- [ ] Mỗi lần xoay, mở Logcat hoặc tab Firestore của Firebase Console: không có lượt đọc mới.
- [ ] D01: gõ số CCCD, chọn một ảnh, xoay, rồi nhấn Home và mở lại: số CCCD và ảnh còn đó.
- [ ] D04: chọn lý do, gõ mô tả, xoay: cả hai còn. D05: chọn lý do, gõ mô tả, chọn 3 ảnh, xoay: tất cả còn.
- [ ] D06 chi tiết: gõ lời giải thích và ghi chú, chọn mức ưu tiên, bấm một nút quyết định để hộp thoại hiện, xoay: hộp thoại còn, nội dung còn; đồng ý: chỉ một lần ghi trong Firebase Console.
- [ ] Gây một lỗi (tắt mạng rồi gửi), thấy câu lỗi một lần, xoay: câu lỗi không hiện lại.
- [ ] Đăng xuất để thành khách rồi mở lần lượt D01, D02, D03, D04: cả bốn đưa tới A01, kể cả ở chế độ ngang.
- [ ] Bật chế độ máy bay: các nút ghi liệt kê ở mục tiêu chí bị khóa, dải offline hiện.
- [ ] Mỗi phương thức trong danh sách "không còn NOT_READY" có một dòng kết quả trong PR.
- [ ] Tài khoản thường: không thấy nút "Khu vực quản trị", đọc ghi chú nội bộ bị Rules từ chối.
- [ ] PR liệt kê mọi lớp, phương thức, trường công khai mới kèm dòng tài liệu định nghĩa (R16), hoặc ghi "không có".

### Quy mô
- [x] Đây là một lát cắt dọc của một module, khoảng nửa ngày đến một ngày, một Pull Request.

### Câu hỏi cho leader
1. `phase-3-hoan-thien.md` ghi P3-D là "bố cục ngang D01 đến D06", không có D07 (`MyReportsFragment`, hai tab Báo cáo của tôi và Khiếu nại của tôi), dù D07 là màn của module D và có hộp thoại, danh sách, chọn ảnh bổ sung bằng chứng và chia sẻ tệp CSV (`exportedFile` giữ giá trị sau xoay nên hộp chia sẻ có thể mở lại nếu không có cách đặt lại). Đọc 1: D07 thuộc P3-D như các màn kia. Đọc 2 (mặc định trong issue này): D07 ngoài phạm vi, chỉ cần dùng được ở chế độ ngang nhờ `ConstraintLayout` đã dùng ở P2-D4b và P2-D5b (R19: không làm file không có trong kế hoạch). Xác nhận; nếu Đọc 1 thì thêm `layout-land/fragment_trust_my_reports.xml` và `MyReportsFragment.java` vào danh sách file.
2. README D01, D02, D03, D07 ghi "Mất mạng: vẫn hiện thông tin đã tải trước đó từ bộ nhớ đệm" và `overview.md` mục 10 yêu cầu hiện dữ liệu đã đệm kèm dải offline cho bảng tin, chi tiết tin, chat, lịch sử giao dịch; nhưng `room-schema.md` không có bảng nào cho hồ sơ, đánh giá, báo cáo hay khiếu nại và P3-D không có việc offline. Đọc 1 (mặc định trong issue này): module D không đệm Room; offline chỉ hiện dải báo, khóa nút ghi, giữ dữ liệu đã tải trong ViewModel và báo "Không có kết nối" khi đọc lỗi. Đọc 2: thêm bảng Room cho module D (đổi bảng phải hỏi trước, R6, và `AppDatabase` là file của Phát). Xác nhận.
3. Kế hoạch P3-D chỉ nhắc "giữ nội dung báo cáo và khiếu nại đang soạn khi xoay"; biểu mẫu xác thực tài khoản (số CCCD và hai ảnh ở D01) cũng phải giữ nội dung (UC31 NFR3). Issue này gộp nó vào. Xác nhận, và cập nhật dòng P3-D trong `phase-3-hoan-thien.md` cho đủ.
