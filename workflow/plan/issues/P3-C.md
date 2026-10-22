TITLE: feat(ui): bố cục ngang và offline cho chat, giao dịch
LABELS: type:feature, area:ui, module:c, priority:p2
ASSIGNEES: Contest451
PHASE: P3

Plan item: P3-C

### Bối cảnh
1. PUBGApp (Pre-owned Users' Bargain Grounds) là ứng dụng Android mua bán, trao đổi và cho tặng đồ đã qua sử dụng: Java 17, XML Views với ViewBinding, Firebase Authentication và Cloud Firestore gói Spark (không Cloud Storage), Room làm bộ nhớ đệm, minSdk 30.
2. Module C (Chat và giao dịch) do Nguyễn Lê Hải Long (`Contest451`) sở hữu: gói giao diện `com.nhom3.pubgapp.ui.trade`, tiền tố layout `trade_`, chuỗi trong `strings_trade.xml`, đồ thị điều hướng `nav_trade.xml`.
3. Giai đoạn P3 (hoàn thiện) làm ba yêu cầu cứng của giảng viên trên mọi màn: xoay màn hình không mất trạng thái, xem lại trang đã xem khi mất mạng, Firebase được bảo vệ bằng Security Rules. Issue này làm hai yêu cầu đầu cho sáu màn của module C: C01, C02, C03, C04, C05 và C07 (`DealPaymentFragment`, màn thanh toán mô phỏng thêm ngày 2026-10-22).
4. Đọc trước: `workflow/plan/phase-3-hoan-thien.md` (mục tiêu và "Danh sách kiểm tra"); `workflow/docs/ui/c-ket-noi-giao-dich/README.md` (mục "Trạng thái đặc biệt" của từng màn); `workflow/docs/data/room-schema.md`.
5. Trên `main` đã có: toàn bộ P2-C1 đến P2-C7 (sáu màn chạy được ở bố cục dọc, Room đệm danh sách chat, tin nhắn và giao dịch), `NetworkMonitor` và dải ngoại tuyến `view_offline_banner.xml` do Phát dựng ở P1.

### Mã tính năng
F-08, F-09, F-10

### Mã việc kế hoạch
P3-C

### Module
C. Chat và giao dịch (Hải Long)

### STT trong bảng đặc tả
Không thêm chức năng mới. Hoàn thiện yêu cầu môn học cho STT 43, 44, 46 đến 59 (xoay màn hình và xem lại khi ngoại tuyến).

### Màn hình mockup
C01 Danh sách tin nhắn, C02 Trò chuyện và đề nghị giá, C03 Chi tiết giao dịch, C04 Lịch sử giao dịch và thanh toán, C05 Viết đánh giá, C07 Thanh toán giao dịch (chưa có ảnh).

### Làm gì và vì sao
Giảng viên yêu cầu mọi màn hình xoay ngang vẫn dùng được và không mất trạng thái, và các trang đã xem vẫn xem lại được khi mất mạng. Việc này làm hai phần cho sáu màn của module C. Thứ nhất, bố cục ngang (`layout-land`) cho C01 đến C05 và C07, kèm kiểm tra rằng xoay màn hình không tải lại dữ liệu, không mất chữ đang gõ, không mất vị trí cuộn, không mất bước đang làm dở (bước và phương thức đã chọn ở C07, ba hàng chip và tab ở C04, ảnh chờ gửi, các vùng sửa điều khoản, hủy, giao hàng, nhận hàng ở C03). Thứ hai, kiểm và sửa đường ngoại tuyến cho danh sách chat, tin nhắn đã xem, lịch sử giao dịch và đồng hồ đếm ngược: dữ liệu từ Room hiện cùng dải báo ngoại tuyến, mọi nút ghi bị khóa. Đây là kiểm tra theo danh sách của `phase-3-hoan-thien.md`, thực hiện trên một máy ảo và ghi kết quả vào Pull Request. Issue này phục vụ các dòng barem R-Chapter 5 (resource cho layout thích ứng: thư mục `res/layout-land/` cho sáu màn), R-Chapter 1, 2, 6, 7 (chương 2: vòng đời và giữ trạng thái khi xoay màn hình; chương 7: kiểm tra kết nối mạng bằng `NetworkMonitor`, dải ngoại tuyến và bộ nhớ đệm Room để xem lại trang đã xem khi mất mạng) và C-GUI (giao diện dùng được ở cả hai chiều).

### Tiêu chí nghiệm thu
Bố cục ngang:
- [ ] Mỗi màn C01 (`ConversationListFragment`), C02 (`ChatFragment`), C03 (`DealDetailFragment`), C04 (`DealHistoryFragment`), C05 (`WriteReviewFragment`), C07 (`DealPaymentFragment`) có bản `layout-land` đủ mọi thành phần của bản dọc: không cắt chữ, không che nút, cuộn được khi nội dung dài. Mọi `id` trong bản ngang trùng bản dọc để ViewBinding không trả `null`.
- [ ] C02 ngang: ô nhập tin nhắn và nút gửi vẫn thấy khi bàn phím mở; danh sách tin nhắn còn đủ chỗ hiện. Chế độ `windowSoftInputMode` nằm ở `AndroidManifest.xml` của Phát, xem Câu hỏi 1.
- [ ] C03 ngang: đủ thanh tiến trình, thẻ sản phẩm, khối thông tin, hộp thông tin thanh toán, khối đếm ngược, biểu mẫu sửa điều khoản, vùng nhập lý do hủy, biểu mẫu giao hàng, hộp xác nhận nhận hàng với ảnh, các nút hành động.
- [ ] C04 ngang: ba hàng chip lọc (trạng thái, loại giao dịch, vai trò) và hai tab vẫn thao tác được; danh sách giao dịch dùng được chiều rộng (hai cột hoặc thẻ rộng, người làm chọn).
- [ ] C05 ngang: năm sao tổng thể, ba dãy sao tiêu chí, ô nhận xét và nút "Gửi đánh giá" đều thấy hoặc cuộn tới được.
- [ ] C07 ngang: ba phương thức thanh toán, số tiền, nút "Tiếp tục", "Xác nhận thanh toán", "Đổi phương thức", "Hủy" và biên lai đều thấy hoặc cuộn tới được.

Giữ trạng thái khi xoay (kiểm từng màn: xoay dọc sang ngang rồi ngược lại ít nhất hai vòng, trước và sau khi dữ liệu tải xong):
- [ ] C01: danh sách đã tải, chữ trong ô tìm kiếm và vị trí cuộn còn nguyên; không có lần tải mạng mới (kiểm bằng nhật ký hoặc đếm lần gọi).
- [ ] C02: danh sách tin nhắn, vị trí cuộn, chữ đang gõ, ảnh đã chọn chờ gửi, `ListenerRegistration` chỉ có một (không nhân đôi tin nhắn sau khi xoay); tin nhắn mới tới trong lúc xoay vẫn hiện đúng một lần; thẻ đề nghị giá giữ trạng thái và hai nút "Từ chối", "Chấp nhận" không gửi lệnh lần hai.
- [ ] C03: giao dịch và khoản thanh toán đã tải, các vùng đang mở cùng chữ đã nhập (đơn vị vận chuyển, mã vận đơn, lý do hủy, điều khoản đang sửa) và ảnh đã chọn (ảnh vận đơn, ảnh nhận hàng), đồng hồ đếm ngược chạy tiếp không nhảy số, trạng thái "đang xử lý" của nút (không gửi lệnh lần hai).
- [ ] C04: tab đang chọn, ba chip đang chọn, danh sách và vị trí cuộn.
- [ ] C05: số sao đã chọn ở cả bốn dãy và nhận xét đang gõ.
- [ ] C07: bước đang xem, phương thức đã chọn, nút "Xác nhận thanh toán" đang khóa khi đang xử lý, biên lai; xoay giữa chừng không tạo hai khoản thanh toán.
- [ ] Dữ liệu và trạng thái tải nằm trong `ChatViewModel`, `DealViewModel`, `TrustViewModel` (`messages`, `deal`, `payment`, `deals`, `payments`, `myReview`, `loading`, `error`, `done`), không nằm trong Fragment. Trạng thái chỉ thuộc giao diện (ảnh đã chọn, vùng đang mở, bước và phương thức ở C07, tab và chip ở C04, số sao ở C05) không có tên trong `viewmodel.md`, nên mỗi Fragment giữ bằng `onSaveInstanceState` với khóa `private static final` như đã quyết ở P2-C4 đến P2-C7; trạng thái của các `EditText` do Android tự khôi phục theo `id` được phép dùng. Không thêm thuộc tính công khai vào ViewModel (R16).

Ngoại tuyến (bật chế độ máy bay sau khi đã xem từng trang; kiểm lại khi bật mạng trở lại):
- [ ] C01: danh sách chat đã xem vẫn hiện từ `conversation_cache`, có dải ngoại tuyến; bấm vào một dòng vẫn vào được C02.
- [ ] C02: tin nhắn của cuộc trò chuyện đã xem vẫn hiện từ `message_cache`; ảnh trong chat chỉ có mã ảnh nên hiện khung chờ thay cho ảnh đầy đủ; ô nhập, nút đính kèm ảnh, nút gửi bị khóa; thẻ đề nghị giá hiện theo mức Room cho phép (Câu hỏi 2) và hai nút "Từ chối", "Chấp nhận" bị khóa.
- [ ] C04: danh sách giao dịch đã xem hiện từ `deal_cache`, ba hàng chip (trạng thái, loại giao dịch nhờ cột `tradeType` của `deal_cache`, vai trò) vẫn chạy trên dữ liệu đệm; tab "Thanh toán" hiện thông báo cần có mạng vì không có bảng Room cho `payments` (đã chốt).
- [ ] C03: phần giao dịch có trong `deal_cache` (tên, ảnh, giá, `tradeType`, trạng thái, phương thức giao nhận) hiện lại, còn địa chỉ, thời gian hẹn, điều kiện và vận đơn ghi "cần có mạng để xem" (theo `data-local.md#dealentity`); đồng hồ đếm ngược vẫn chạy từ `autoReleaseAtMillis`; mọi nút ghi bị khóa: Sửa điều khoản, Xác nhận, Hủy giao dịch, Thanh toán, Xác nhận đã giao, Xác nhận đã nhận hàng, Viết đánh giá.
- [ ] C05 và C07: nút "Gửi đánh giá" và "Xác nhận thanh toán" bị khóa khi ngoại tuyến; C07 không mở được "Tiếp tục" tới bước ghi khi không có mạng và báo cần có mạng.
- [ ] Khi bật mạng lại: dải ngoại tuyến biến mất, các nút mở khóa và dữ liệu được tải mới mà không cần thoát màn (quan sát `NetworkMonitor` qua LiveData `online`).
- [ ] Không màn nào của module C hiện dữ liệu đệm mà không có dải ngoại tuyến, và không màn nào hiện dải này khi đang có mạng.
- [ ] Đăng xuất thì ba bảng `conversation_cache`, `message_cache`, `deal_cache` bị xóa sạch (kiểm bằng cách đăng nhập tài khoản khác sau khi đăng xuất và thấy danh sách rỗng). Việc xóa Room thuộc Phát và Ngân; nếu thiếu thì ghi vào Pull Request và báo họ, không tự sửa.
- [ ] Mọi thao tác mạng của sáu màn thất bại (mất mạng giữa chừng, quá thời hạn chờ 15 giây, Rules từ chối, thiếu chỉ mục, `NOT_READY` của module khác) đều hiện câu báo lỗi tiếng Việt; không còn vòng xoay vô tận, không lỗi nuốt âm thầm.
- [ ] Mọi chữ hiển thị mới là string resource trong `strings_trade.xml`.

Chung:
- [ ] Kết quả danh sách kiểm tra trên (đánh dấu đạt hoặc không đạt cho từng mục và từng màn, kèm cách đã kiểm trên máy ảo) ghi trong mô tả Pull Request, như `phase-3-hoan-thien.md` yêu cầu. Không ghi "đã kiểm trên máy ảo" khi chưa chạy (R1).
- [ ] Issue này không đổi hành vi quy tắc nghiệp vụ; không có chuyển trạng thái nào mới. Nếu phải sửa thân một phương thức đã làm ở P2 để đọc Room khi ngoại tuyến, test của phương thức đó vẫn xanh và có thêm ca cho nhánh ngoại tuyến nếu có logic mới (`ChatRepositoryTest`, `DealRepositoryTest`).
- [ ] `./gradlew assembleDebug testDebugUnitTest lintDebug` và `sh workflow/scripts/check-java-style.sh` đều qua.
- [ ] Pull Request liệt kê mọi lớp, phương thức, trường công khai mới kèm dòng tài liệu định nghĩa nó (R16); dự kiến không có.

### Tài liệu phải đọc
- `workflow/plan/phase-3-hoan-thien.md` (mục tiêu và "Danh sách kiểm tra"; bảng còn ghi module C là C01 đến C05, xem Câu hỏi 3).
- `workflow/plan/so-huu.md` mục 2, 3, 5, 6.
- `workflow/docs/product/features.md` mục "Yêu cầu môn học".
- `workflow/docs/ui/c-ket-noi-giao-dich/README.md` (mục "Trạng thái đặc biệt" của C01, C02, C03, C04, C05, C07).
- `workflow/docs/architecture/luong-xu-ly.md` mục 3 (điểm 4, 5, 6) và phần "Quy ước chung" (kiểm `NetworkMonitor.getInstance().isOnline()`, thời hạn chờ 15 giây).
- `workflow/docs/architecture/dac-ta-lop/util.md#networkmonitor`; `dac-ta-lop/data-local.md` (`ConversationEntity`, `MessageEntity`, `DealEntity`, `ChatDao`, `DealDao`); `dac-ta-lop/viewmodel.md#chatviewmodel`, `#dealviewmodel`, `#trustviewmodel`.
- `workflow/docs/data/room-schema.md` (bảng `conversation_cache`, `message_cache`, `deal_cache`, quy tắc "Room chỉ là bộ nhớ đệm", xóa khi đăng xuất).
- `workflow/docs/conventions/android-java.md` mục "Tài nguyên và xoay màn hình"; `workflow/docs/conventions/giao-dien.md` mục 6; skill `pubg-ui`.

### Lớp, trường, trạng thái sẽ dùng
- Không thêm lớp, phương thức hay trường công khai. Dùng lại các thứ đã làm ở P2-C1 đến P2-C7: `ChatViewModel`, `DealViewModel`, `TrustViewModel`; `ChatRepository`, `DealRepository`, `ReviewRepository` (chỉ nhánh đọc Room khi ngoại tuyến); `ConversationEntity`, `MessageEntity`, `DealEntity`; `ChatDao`, `DealDao`.
- `NetworkMonitor.getInstance().isOnline()` và LiveData `online` (Phát); thành phần dải ngoại tuyến `view_offline_banner.xml` do P1-06 dựng (Phát).
- Không có chuyển trạng thái nào được thêm hay đổi.

### File được tạo hoặc sửa
- [tạo] `PUBGApp/app/src/main/res/layout-land/fragment_trade_conversation_list.xml`
- [tạo] `PUBGApp/app/src/main/res/layout-land/fragment_trade_chat.xml`
- [tạo] `PUBGApp/app/src/main/res/layout-land/fragment_trade_deal_detail.xml`
- [tạo] `PUBGApp/app/src/main/res/layout-land/fragment_trade_deal_history.xml`
- [tạo] `PUBGApp/app/src/main/res/layout-land/fragment_trade_write_review.xml`
- [tạo] `PUBGApp/app/src/main/res/layout-land/fragment_trade_deal_payment.xml`
- [sửa] `PUBGApp/app/src/main/java/com/nhom3/pubgapp/ui/trade/ConversationListFragment.java`
- [sửa] `PUBGApp/app/src/main/java/com/nhom3/pubgapp/ui/trade/ChatFragment.java`
- [sửa] `PUBGApp/app/src/main/java/com/nhom3/pubgapp/ui/trade/DealDetailFragment.java`
- [sửa] `PUBGApp/app/src/main/java/com/nhom3/pubgapp/ui/trade/DealHistoryFragment.java`
- [sửa] `PUBGApp/app/src/main/java/com/nhom3/pubgapp/ui/trade/WriteReviewFragment.java`
- [sửa] `PUBGApp/app/src/main/java/com/nhom3/pubgapp/ui/trade/DealPaymentFragment.java`
- [sửa] `PUBGApp/app/src/main/java/com/nhom3/pubgapp/viewmodel/ChatViewModel.java` (chỉ thân phương thức đã có, để giữ trạng thái)
- [sửa] `PUBGApp/app/src/main/java/com/nhom3/pubgapp/viewmodel/DealViewModel.java` (như trên)
- [sửa] `PUBGApp/app/src/main/java/com/nhom3/pubgapp/viewmodel/TrustViewModel.java` (chỉ thân `submitReview`)
- [sửa] `PUBGApp/app/src/main/java/com/nhom3/pubgapp/data/ChatRepository.java` (chỉ nhánh đọc Room khi ngoại tuyến của `getConversations`, `observeMessages`)
- [sửa] `PUBGApp/app/src/main/java/com/nhom3/pubgapp/data/DealRepository.java` (chỉ nhánh đọc Room khi ngoại tuyến của `getMyDeals`, `getById`)
- [sửa] `PUBGApp/app/src/main/res/values/strings_trade.xml`
- [sửa] `PUBGApp/app/src/test/java/com/nhom3/pubgapp/data/ChatRepositoryTest.java`, `PUBGApp/app/src/test/java/com/nhom3/pubgapp/data/DealRepositoryTest.java` (chỉ khi có logic mới)

### Các bước đề nghị
Người làm có thể đi theo thứ tự khác, nhưng không được đổi tên lớp, phương thức, trường nào so với tài liệu.

1. Chạy ứng dụng trên một máy ảo, đi qua sáu màn ở bố cục dọc và ghi lại (bảng trong mô tả Pull Request) màn nào hiện mất trạng thái khi xoay, màn nào không hiện được khi ngoại tuyến; đây là danh sách việc thật.
2. Sửa chỗ giữ trạng thái: dữ liệu mất khi xoay thì đưa vào LiveData đã có của ViewModel (`ChatViewModel`, `DealViewModel`, `TrustViewModel`); trạng thái chỉ thuộc giao diện thì giữ bằng `onSaveInstanceState` của Fragment; không thêm tên công khai mới; chỉ sửa thân phương thức đã có.
3. Viết bản `layout-land` cho C01, C04, C05 (đơn giản nhất), giữ nguyên `id` và `ConstraintLayout`; dùng token của `giao-dien.md`, không viết thẳng màu, cỡ, khoảng cách.
4. Viết `layout-land` cho C02 (bàn phím) và C07; xử lý chế độ bàn phím theo Câu hỏi 1.
5. Viết `layout-land` cho C03: bản dài nhất, dùng vùng cuộn cho khối thông tin, biểu mẫu và nút hành động.
6. Kiểm đường ngoại tuyến từng màn theo danh sách; sửa nhánh đọc Room trong `ChatRepository`, `DealRepository` nếu thiếu, và khóa các nút ghi khi `NetworkMonitor.getInstance().isOnline()` sai.
7. Rà lỗi: mọi lỗi mạng có câu tiếng Việt, không vòng xoay vô tận.
8. Chạy lại toàn bộ danh sách kiểm tra, điền kết quả vào mô tả Pull Request (đạt hoặc không đạt, cách đã kiểm).
9. Chạy các lệnh ở mục "Tự kiểm trước khi mở Pull Request", rồi mở Pull Request. Nếu quá một ngày thì tách hai Pull Request (bố cục ngang và giữ trạng thái; kiểm ngoại tuyến) và báo trong issue.

### File dùng chung cần thêm dòng
Không có. Cần `android:windowSoftInputMode` cho chat ngang: ghi vào mô tả Pull Request để Phát thêm vào `AndroidManifest.xml` (Câu hỏi 1).

### Cấm đụng
- Leader chỉ sửa: `AndroidManifest.xml`, `nav_main.xml`, `nav_admin.xml`, `themes.xml`, `colors.xml`, `dimens.xml` (không thêm `values-land/dimens.xml`; đặt kích thước trong layout bằng token đã có), `strings_common.xml`, `AppDatabase`, `Converters`, mọi file Gradle, thư mục `firebase/`.
- Lớp `model` và `model.enums`: không sửa. Không đổi bảng Room.
- File của module khác: màn B03 (Tú), D01 đến D07 (Phúc), A04 đến A06 và C06 (Ngân), E01 đến E07 (Phát); thành phần dải ngoại tuyến chung (Phát).
- Việc xóa Room khi đăng xuất, `AutoReleaseWorker`, `DealRepository.releaseDueDeals`, `completeDueDeals`, `expirePendingDeals` (Phát): không đụng.
- Security Rules thật và kịch bản tấn công (P3-R, Phát).

### Phụ thuộc
P2-C1, P2-C2, P2-C3a, P2-C3b, P2-C4, P2-C5a, P2-C5b, P2-C6, P2-C7. Cần dải ngoại tuyến từ P1-06 và `NetworkMonitor` từ P1-04. Đường vào C01 và C04 cần tab Tin nhắn và D01 (Phát và Phúc) để kiểm đầy đủ.

### Ngoài phạm vi
- Bố cục ngang của module khác (P3-A, P3-B, P3-D, P3-E).
- Security Rules thật, chỉ mục ghép, kiểm kịch bản tấn công (P3-R, Phát).
- Rà soát test cho mọi quy tắc trong `trang-thai.md` (P3-T, Phát điều phối, Hải Long bổ sung test cho phần của mình ở đó).
- Ghi dữ liệu khi ngoại tuyến (gửi sau): không hỗ trợ trong đợt này, nút ghi bị khóa.
- Bố cục máy tính bảng, đa cửa sổ.

### Tài liệu cần cập nhật
Không có, trừ khi phải đổi hành vi. Câu hỏi 3 buộc leader sửa `phase-3-hoan-thien.md`.

### Tự kiểm trước khi mở Pull Request
- [ ] `cd PUBGApp && ./gradlew assembleDebug testDebugUnitTest lintDebug` kết thúc `BUILD SUCCESSFUL`.
- [ ] `sh workflow/scripts/check-java-style.sh` không báo lỗi trên các dòng đã thêm.
- [ ] Từng màn C01, C02, C03, C04, C05, C07: xoay dọc sang ngang rồi ngược lại hai vòng, không tải lại dữ liệu, không mất chữ đang gõ, không mất vị trí cuộn, không mất bước hay lựa chọn đang làm dở; ghi kết quả từng màn.
- [ ] Từng màn ở bố cục ngang: mọi nút thấy hoặc cuộn tới được, không bị bàn phím che.
- [ ] Bật chế độ máy bay sau khi đã xem C01, C02, C03, C04: dữ liệu đệm vẫn hiện, có dải ngoại tuyến, mọi nút ghi bị khóa; tab "Thanh toán" của C04 báo cần có mạng.
- [ ] Bật lại mạng: dải biến mất, nút mở khóa, dữ liệu tải lại mà không thoát màn.
- [ ] Đăng xuất rồi đăng nhập tài khoản khác: danh sách chat và lịch sử giao dịch của tài khoản trước không còn.
- [ ] Mọi chữ mới là string resource tiếng Việt, mọi lỗi mạng có câu báo tiếng Việt.
- [ ] Liệt kê trong Pull Request mọi lớp, phương thức, trường công khai mới kèm dòng tài liệu định nghĩa (R16); dự kiến không có.

### Quy mô
- [x] Đây là một lát cắt dọc của một module, khoảng nửa ngày đến một ngày, một Pull Request.

Ghi chú về quy mô: sáu bố cục ngang cộng một vòng kiểm ngoại tuyến cho sáu màn có thể lấn sang hơn một ngày. Nếu vậy, người làm tách thành hai Pull Request (bố cục ngang và giữ trạng thái; kiểm ngoại tuyến) và báo leader trong issue, không bỏ bớt mục.

### Câu hỏi cho leader
1. **Bàn phím che ô nhập chat ở chế độ ngang.** Chế độ xử lý bàn phím (`android:windowSoftInputMode`, ví dụ `adjustResize`) khai ở `AndroidManifest.xml`, file chỉ Phát sửa. Cần Phát đặt cho `MainActivity` (hoặc cho phép Hải Long sửa một dòng theo `so-huu.md` mục 6).
2. **Ngoại tuyến của thẻ đề nghị giá.** Room không có bảng offer và `ChatRepository` không có phương thức đọc `Offer` (Câu hỏi 1 của P2-C3a), nên khi ngoại tuyến thẻ đề nghị giá chỉ có thể hiện chỗ giữ (loại tin nhắn "Đề nghị giá", không có số tiền hay trạng thái). Danh sách kiểm tra P3 chỉ nêu "danh sách chat, tin nhắn đã xem, lịch sử giao dịch, đồng hồ đếm ngược". Xác nhận chấp nhận chỗ giữ này. Phần địa chỉ, thời gian hẹn, điều kiện và vận đơn của C03 cũng không có ngoại tuyến vì `deal_cache` không có các cột đó (`data-local.md#dealentity`); phần này theo đúng đặc tả nên không cần quyết thêm.
3. **Kế hoạch P3 chưa tính C07.** `phase-3-hoan-thien.md` ghi P3-C là "bố cục ngang C01 đến C05" và danh sách kiểm tra ghi "30 màn", trong khi module C nay có thêm C07 `DealPaymentFragment` (tổng 33 mã màn hình). Issue này làm cả C07; cần sửa kế hoạch cho khớp.
