# Kế hoạch rút gọn: 33 issue, chia sao cho 5 người không đụng nhau

Viết ngày 2026-10-22. Đợt này làm 24 trong 33 màn hình, đủ để chạy trọn mạch demo và chạm đủ mọi dòng barem.

Tệp này **thắng** `phase-1-nen-mong.md` đến `phase-4-tich-hop-demo.md`, và thắng `so-huu.md` mục 5 ở phần chia phương thức.

**Không có lịch theo ngày.** Ai xong việc của mình thì lấy việc tiếp theo, càng nhanh càng tốt. Đồ án không ra trong một lần: xong một đợt thì cả nhóm rà lại, sửa, rồi đi tiếp. Thứ duy nhất phải tôn trọng là **thứ tự đợt** ở mục 3, vì đó là thứ tự phụ thuộc thật.

## 1. Hai cái chặn thật

Nhóm dùng trợ lý AI sinh code, nên viết 93 lớp không phải vấn đề. Hai thứ chặn là:

**Thứ nhất, xung đột tệp.** Năm người làm song song, mỗi lần hai người sửa cùng một tệp là một lần ngồi gỡ merge, và luật R4 bắt giữ cả hai phía. Nguyên tắc:

> **Gọi phương thức của người khác thì được. Sửa tệp của người khác thì không.**

**Thứ hai, thứ tự biên dịch.** `Deal` nhắc `Listing`, `ChatRepository` gọi `NotificationRepository`, `AppDatabase` nhắc entity của người khác. Nếu mỗi người tự tạo lớp của mình thì Pull Request của ai đẩy trước cũng không biên dịch được, và CI chạy `assembleDebug` trên từng Pull Request.

Cả hai được giải bằng **đợt 0: dựng khung trước, điền thân sau.**

- **Đợt 0** tạo khung của **toàn bộ 93 lớp**: đủ chữ ký, thân chỉ gọi `DataCallback.NOT_READY`; ViewModel đủ LiveData, getter, `clearError`, `clearDone`; enum đủ giá trị; mọi Fragment là khung rỗng; mọi `view_*.xml` và `strings_<module>.xml` có mặt. Việc này thuần sinh code từ `dac-ta-lop/`, đúng chỗ trợ lý AI mạnh.
- Sau đợt 0, **mọi Pull Request đều biên dịch được bất kể thứ tự**, vì mọi thứ người khác cần đã tồn tại.
- **Đợt 1** mỗi người điền thân repository mình sở hữu, một người một tệp, không ai mở tệp của người khác.
- **Đợt 2 và 3** mỗi người chỉ còn Fragment, layout, adapter và thân ViewModel của mình.

## 2. Bản đồ sở hữu tệp: ai mở tệp nào

Bảng này **thắng** `so-huu.md` mục 5, vốn chia theo từng phương thức nên ba người có thể cùng mở `UserRepository.java`.

| Người | Lớp model | Repository và tiện ích | ViewModel | Gói giao diện |
| --- | --- | --- | --- | --- |
| **Phát** | `ActivityLog`, `BannedKeyword`, `Warning` | `AdminRepository`, `KeywordFilter`, `AppDatabase`, `Converters` | `AdminUserViewModel`, `AdminContentViewModel`, `AdminDealViewModel` | `ui.admin`, `ui.main` |
| **Ngân** | `User`, `UserIdentity`, `UserAddress`, `AppNotification`, `ProfileStats` | `AuthRepository`, `UserRepository`, `NotificationRepository`, `SessionManager` | `AuthViewModel`, `NotificationViewModel` | `ui.account`, `ui.notification` |
| **Tú** | `Listing`, `Category`, `MediaImage`, `SearchFilter` | `ListingRepository`, `MediaRepository`, `CategoryRepository`, `GeoUtils`, `FormatUtils` | `ShopViewModel`, `FeedViewModel`, `SearchViewModel`, `ListingDetailViewModel` | `ui.listing` |
| **Hải Long** | `Conversation`, `ChatMessage`, `Offer`, `Deal`, `Payment`, `StatusChange`, `Review` | `ChatRepository`, `DealRepository`, `ReviewRepository` | `ChatViewModel`, `DealViewModel` | `ui.trade` |
| **Phúc** | `SellerVerification`, `Report`, `Complaint` | `ReportRepository`, `ComplaintRepository` | `ProfileViewModel`, `TrustViewModel` | `ui.profile`, `ui.trust` |

**Mọi enum và `BaseDocument` thuộc đợt 0** (Phát tạo một lượt), vì chúng chỉ là danh sách giá trị và mọi lớp của cả năm người đều nhắc tới.

**Entity và DAO của Room thuộc người sở hữu model tương ứng**, vì `fromModel` và `toModel` của chúng đọc đúng model đó: `ListingEntity` và `ListingDao` của Tú, `ConversationEntity`, `MessageEntity`, `ChatDao`, `DealEntity`, `DealDao` của Hải Long, `RecentViewEntity`, `RecentSearchEntity`, `HistoryDao` của Tú. Phát chỉ giữ `AppDatabase` và `Converters`, hai tệp chỉ liệt kê chúng.

**Gói `ui.common`** (bảy tệp `view_*.xml` dùng chung) thuộc Phát, tạo ở đợt 0 rồi đóng băng.

Bốn chỗ đổi so với `so-huu.md`, đổi vì chúng là bốn điểm xung đột nặng nhất:

1. **`UserRepository` về tay Ngân trọn vẹn**, kể cả xác thực, theo dõi, tìm người dùng, số liệu hồ sơ. Theo `so-huu.md` thì ba người cùng mở tệp này.
2. **`NotificationRepository` về tay Ngân.** Mọi repository khác chỉ gọi `create(notification, batch, cb)`. Mười một bản nháp còn ghi tệp này là của Phát: bỏ qua, theo bảng trên.
3. **Màn A05 sửa hồ sơ về tay Phúc**, cùng với D01, vì cả hai dùng `ProfileViewModel`.
4. **`TrustViewModel.submitReview` về tay Phúc**, không phải Hải Long. Màn C05 viết đánh giá là của Hải Long, nhưng nó gọi `submitReview` chứ không sửa tệp đó; Phúc điền thân phương thức này trong đợt 1.

**Tệp chỉ Phát mở, và chỉ trong đợt 0:** `AndroidManifest.xml`, `nav_main.xml`, `nav_admin.xml` và các đồ thị con, `themes.xml`, `colors.xml`, `dimens.xml`, `strings_common.xml`, menu thanh dưới, `AppDatabase`, `Converters`, `PubgApplication`, mọi tệp Gradle, `firebase/firestore.rules`. Sau đợt 0 chúng **đóng băng**: ai cần thêm một dòng thì nhắn Phát.

**`RecommendationEngine`** nằm trong phần bỏ lại (mục 4), nên không ai sở hữu nó đợt này.

## 3. Ba mươi ba issue, chia năm đợt

Mỗi dòng là một issue, một Pull Request, **một người**. Cột "Tra ở" là bản nháp trong [`issues/`](issues/README.md) để lấy tên lớp, chữ ký và tiêu chí nghiệm thu. Đọc [`issues/README.md`](issues/README.md) một lần trước khi mở bản nháp đầu tiên: nó nói ba loại dòng trong bản nháp đã lạc hậu.

**Số issue trên GitHub:** issue số `N` trong các bảng dưới đây là GitHub issue **#(N + 80)**. Issue 1 là [#81](https://github.com/tahpnart8/NHOM3-MobileApp/issues/81), issue 33 là [#113](https://github.com/tahpnart8/NHOM3-MobileApp/issues/113). Riêng [#80](https://github.com/tahpnart8/NHOM3-MobileApp/issues/80) là đợt tài liệu, không phải việc code. Nhánh đặt tên `<type>/<số GitHub>-<slug>`, ví dụ `feat/89-dang-nhap-email-google`.

**Hai điều phải biết khi đọc bản nháp:**

- Bản nháp viết trước kế hoạch này. Chỗ nào nó ghi "điền thân `XRepository.y`" mà `XRepository` không phải của bạn thì **bỏ qua dòng đó**: đợt 1 đã làm. Issue đợt 2 và 3 chỉ còn Fragment, layout, adapter, ViewModel.
- Chỗ nào bản nháp nhắc một tính năng ở mục 4 (bỏ lại) thì bỏ qua.

### Đợt 0: dựng khung, chỉ Phát làm (3 issue)

| # | Người | Issue | Tra ở |
| --- | --- | --- | --- |
| 1 | Phát | Firebase project gói Spark (Auth email và Google, Firestore), `google-services.json`, thư viện vào Gradle. Xong khi `assembleDebug` xanh và một tài khoản tạo tay đăng nhập được bằng code thử | `phase-1-nen-mong.md` P1-01, P1-02; `firebase-setup.md` |
| 2 | Phát | Khung **toàn bộ 93 lớp** từ `dac-ta-lop/`: model đủ trường và getter setter, 23 enum đủ giá trị, 12 repository đủ chữ ký với thân `NOT_READY`, 14 ViewModel đủ LiveData và getter cùng `clearError` `clearDone`, `DataCallback`, `BaseRepository`, `SessionManager`, `NetworkMonitor`, entity và DAO Room, `AppDatabase`, `Converters` | `dac-ta-lop/` cả sáu tệp; `room-schema.md` |
| 3 | Phát | Khung giao diện: `colors.xml` và `themes.xml` theo `giao-dien.md` mục 1, `dimens.xml`, ba Activity, `nav_main.xml`, `nav_admin.xml` và các đồ thị con, thanh dưới 5 tab, bảy `view_*.xml` dùng chung, `strings_<module>.xml` cho năm module, khung rỗng của 24 Fragment, `PubgApplication`, `firestore.rules` bản nền. Nhập tay 8 đến 10 danh mục trên Console.
**Chú ý ba chỗ `phase-1-nen-mong.md` P1-06 và P1-07 đã lạc hậu:** (a) rules nền phải cho **khách chưa đăng nhập đọc** `listings`, `categories`, `images` theo `security-rules.md` mục 9, chứ không phải "phải đăng nhập" như P1-07 viết; (b) phải có `FileProvider` trong `AndroidManifest.xml` cùng `res/xml/file_paths.xml` và quyền camera, vì ba màn cần chụp ảnh; (c) `nav_main.xml` **không** tạo đích và hành động toàn cục cho các màn bị bỏ ở mục 4, để không có nút dẫn vào khung trống | `giao-dien.md` mục 1 đến 5; `ui/README.md`; `so-huu.md` mục 3 |

Issue 1 lên `main` trước tất cả, rồi 2, rồi 3. Sau issue 2 thì mọi Pull Request của mọi người đều biên dịch được.

**Tài khoản thử:** Firebase Console tạo được tài khoản email nhưng **không** đánh dấu email đã xác thực được, mà A01 chặn email chưa xác thực. Nên đến khi issue 10 xong, cả nhóm thử bằng **đăng nhập Google**, hoặc đặt tay `users/{uid}` rồi dùng một tài khoản Google.

### Đợt 1: thân tầng dữ liệu và unit test (5 issue, chạy song song)

Mỗi người điền thân các lớp mình sở hữu, kèm unit test cho phương thức quy tắc của chính mình (luật R13: test đi cùng code).

| # | Người | Issue | Test bắt buộc |
| --- | --- | --- | --- |
| 4 | Ngân | Thân `User`, `UserIdentity`, `UserAddress`, `AppNotification`, `ProfileStats`; thân đủ `AuthRepository`, `UserRepository`, `NotificationRepository`, `SessionManager` | `User.canSell`, `User.canBuy`, `User.isLocked` |
| 5 | Tú | Thân `Listing`, `Category`, `MediaImage`, `SearchFilter`; thân đủ `ListingRepository`, `MediaRepository`, `CategoryRepository`, `GeoUtils`, `FormatUtils` | `Listing.publish`, `Listing.acceptsOffer`, `FormatUtils.removeAccents`, `GeoUtils.distanceKm` |
| 6 | Hải Long | Thân `Conversation`, `ChatMessage`, `Offer`, `Deal`, `Payment`, `StatusChange`, `Review`; thân đủ `ChatRepository`, `DealRepository`, `ReviewRepository` | `Deal.canMoveTo`, `Deal.isAutoReleaseDue`, `Deal.isPendingExpired`, `Payment.release` |
| 7 | Phúc | Thân `SellerVerification`, `Report`, `Complaint`; thân `ReportRepository`, `ComplaintRepository`, `TrustViewModel.submitReview`, các DAO Room | `SellerVerification.canResubmit`, `SellerVerification.remainingResubmits`, `Report.resolve` |
| 8 | Phát | Thân `Warning`, `ActivityLog`, `BannedKeyword`; thân đủ `AdminRepository`, `KeywordFilter` | `Warning.resultingStatus`, `KeywordFilter.containsBanned` |

Năm issue này không chạm tệp của nhau, nên gộp theo thứ tự ai xong trước.

### Đợt 2: màn hình xương sống (8 issue)

Mỗi issue làm **cả bản dọc và bản `layout-land`** của màn mình, cùng bốn trạng thái. Làm một lượt rẻ hơn mở lại layout ở đợt sau.

| # | Người | Màn | Tra ở |
| --- | --- | --- | --- |
| 9 | Ngân | A01 đăng nhập email và Google, khôi phục phiên, kiểm khóa, **và `AuthViewModel.logout` đầy đủ kể cả xóa sạch Room** | `P2-A1a`, `P2-A1b` |
| 10 | Ngân | A02 đăng ký bằng liên kết xác thực email, hoàn tất hồ sơ | `P2-A2a`, `P2-A2b` |
| 11 | Tú | B01 trang chủ: lưới tin, bộ nhớ đệm Room, dải báo offline, chế độ khách | `P2-B3` |
| 12 | Tú | B04 đăng tin: ảnh, danh mục, hình thức giao dịch, giá | `P2-B1a`, `P2-B1b` |
| 13 | Hải Long | C01 danh sách tin nhắn | `P2-C1` |
| 14 | Hải Long | C02 trò chuyện chữ và ảnh, thẻ đề nghị giá (`observeOffers`) | `P2-C2` |
| 15 | Phúc | D01 hồ sơ cá nhân, biểu mẫu xác thực, đăng xuất, **và A05 sửa hồ sơ** | `P2-D1a`, `P2-D1b`, `P2-A3` |
| 16 | Phát | E03 chi tiết người dùng: xem hồ sơ và **duyệt yêu cầu xác thực** | `P2-E2b`, `P2-E2d` |

**Issue 16 là cửa của đợt 3**, vì xác thực tài khoản là điều kiện đăng tin và gửi đề nghị. Phát làm nó trước hai màn admin còn lại. Trong lúc chờ, ai cần tài khoản đã xác thực thì đặt `verificationStatus` bằng `APPROVED` tay trên Console.

### Đợt 3: mạch mua bán trọn vòng (10 issue)

| # | Người | Màn | Tra ở |
| --- | --- | --- | --- |
| 17 | Tú | B03 chi tiết tin, lưu tin, **bộ nhớ đệm Room để xem lại khi mất mạng**. Nút "Đề nghị giá" chỉ mở bảng trượt của Hải Long, không tự dựng | `P2-B6a` |
| 18 | Tú | B02 tìm kiếm và bộ lọc, lịch sử tìm | `P2-B4a` |
| 19 | Tú | B05 kệ hàng của tôi: sửa, ẩn, đánh dấu đã bán | `P2-B2a`, `P2-B2b` |
| 20 | Hải Long | **Bảng trượt gửi đề nghị giá** (mở từ B03, nằm trong `ui.trade`), người bán chấp nhận hoặc từ chối, hệ thống tạo giao dịch và giữ chỗ tin | `P2-C3a`, `P2-C3b` |
| 21 | Hải Long | C03 chi tiết giao dịch: điều khoản, hai bên xác nhận, hủy, **và thân `DealViewModel.checkAutoRelease`** | `P2-C4` |
| 22 | Hải Long | C07 thanh toán mô phỏng | `P2-C5a` |
| 23 | Hải Long | Giao hàng kèm vận đơn, xác nhận đã nhận kèm ảnh, đếm ngược | `P2-C5b` |
| 24 | Phúc | D02 trang cá nhân và theo dõi; D03 hồ sơ uy tín | `P2-D2`, `P2-D3` |
| 25 | Phát | E01 bảng điều khiển, E02 danh sách người dùng | `P2-E1`, `P2-E2a` |
| 26 | Phát | E04 kiểm duyệt nội dung: danh sách báo cáo, ẩn tin, khôi phục, gỡ tin | `P2-E3a`, `P2-E3b` |

Trong đợt này 20 phải xong trước 21, và 21 trước 22 và 23: ba khúc liên tiếp của một luồng. Mọi issue khác chạy song song.

### Đợt 4: nối hệ thống, bảo mật, barem (7 issue)

| # | Người | Việc | Tra ở | Dòng barem |
| --- | --- | --- | --- | --- |
| 27 | Hải Long | C05 viết đánh giá, `completeDeal` chuyển giao dịch sang hoàn tất | `P2-C7` | C-Functions |
| 28 | Ngân | C06 thông báo trong ứng dụng, số chưa đọc trên chuông | `P2-A6` | C-Functions |
| 29 | Phát | **Nối `MainActivity`**: chuông và số chưa đọc, chặn tab khi là Khách, gọi `restoreSession` khi mở app, **gọi** `DealViewModel.checkAutoRelease` (thân do Hải Long viết ở issue 21) | `P2-E6b` | R-Chapter 2 |
| 30 | Phát | **Security Rules đầy đủ** theo `security-rules.md` và `firestore.indexes.json` đủ chỉ mục ghép; thử bằng tay các kịch bản ở `overview.md` mục 10 | `security-rules.md` | C-APIs, QA |
| 31 | Phát | E02 khóa, mở khóa, cảnh cáo ba bậc, gỡ toàn bộ tin; từ khóa cấm và tin bị gắn cờ | `P2-E2c`, `P2-E3c` | C-Functions |
| 32 | Ngân | 3 kịch bản Espresso: đăng nhập, đăng tin, admin duyệt xác thực | `barem-diem.md` mục 4 | R-Chapter 6 |
| 33 | Phúc | D04 báo cáo tin, người, tin nhắn; B06 tin đã lưu | `P2-D4a`, `P2-B7` | C-Functions |

Việc chạy nền (`AutoReleaseWorker`) và broadcast nội bộ: **làm nếu còn giờ**, gắn vào issue 29 của Phát. Không có nó thì giải ngân tự động chỉ chạy khi mở app, và báo cáo phải nói thẳng như vậy.

Cuối cùng, **không phải issue**: cả nhóm chạy mạch demo từ đầu đến cuối ba lần, mỗi người nói phần mình, chụp ảnh màn hình thật cho báo cáo.

## 4. Bỏ lại, và nói thẳng trong báo cáo

Đổi trạng thái trong `features.md` thành `proposed`, đừng để `planned` rồi không làm:

| Bỏ | Vì sao bỏ được |
| --- | --- |
| A04 quản lý thông tin, A06 địa chỉ, cài đặt thông báo, đổi mật khẩu, xóa tài khoản | Ngoài mạch demo. Địa chỉ giao hàng nhập tay vào `Deal.deliveryAddress` |
| **Lọc theo bán kính ở B02** | Vì bỏ A06 nên không có nguồn tọa độ, `Listing.location` luôn rỗng. Giữ lọc theo tỉnh, bỏ bán kính |
| `RecommendationEngine`, mục Dành cho bạn, tin tương tự, giá tham khảo, tìm kiếm người dùng | Nặng nhất nhóm B, không ai nhìn thấy trong buổi demo |
| C04 lịch sử giao dịch và thanh toán | Mọi thứ cần xem đã có ở C03 |
| D05, D06 khiếu nại, **D07 báo cáo của tôi** | Một luồng nghiệp vụ riêng, hai màn người dùng cộng hai màn admin. Bỏ D07 thì D01 **không** có lối "Báo cáo của tôi" |
| E05 danh mục, E06 giám sát giao dịch, E07 tài khoản admin | Danh mục nhập tay trên Console; hai màn kia chỉ để xem |

Còn 24 trong 33 màn. Mạch demo vẫn đi trọn một hơi: khách xem tin khi tắt mạng, đăng nhập Google, gửi yêu cầu xác thực, admin duyệt, đăng tin kèm ảnh chụp, nhắn tin, gửi đề nghị, chấp nhận, thanh toán mô phỏng, giao, nhận, đánh giá, admin khóa tài khoản và ẩn tin.

**Khi bỏ một màn, bỏ luôn lối đi tới nó.** Đợt 0 không tạo đích điều hướng cho các màn bị bỏ, và không màn nào được gọi hành động toàn cục trỏ tới chúng. Đích chết là lỗi hay gặp nhất khi cắt phạm vi.

## 5. Cách làm việc để không đụng nhau

- **Issue 1, 2, 3 lên `main` theo đúng thứ tự, trước mọi issue khác.** Đây là chỗ duy nhất cả nhóm phải chờ.
- Từ đợt 1, mỗi người đẩy nhánh của mình và Phát gộp trong ngày. Không ai chờ ai.
- **Mỗi lần bắt đầu một issue mới, chạy `pubg-sync-branch`** để lấy `main` mới nhất. Đây là thứ duy nhất cần kỷ luật hằng ngày.
- Cần một dòng trong tệp đóng băng của Phát thì nhắn trong nhóm. Đừng tự sửa.
- **Xong một đợt thì cả nhóm rà lại một lượt** rồi mới sang đợt sau: chạy mạch demo tới chỗ đã làm, ghi cái gì hỏng, sửa, rồi đi tiếp.
- Tải việc: Phát 9 issue nhưng phần lớn là sinh code và duyệt; Tú 6, Hải Long 6, Ngân 5, Phúc 4. Ai xong sớm thì lấy thêm từ mục 4, theo thứ tự `AutoReleaseWorker`, rồi tin tương tự, rồi C04.

## 6. Ba luật vẫn giữ, đừng bỏ để đi nhanh

1. **Tên lấy từ `dac-ta-lop/`** (R16). Đây là thứ duy nhất giữ cho năm nhánh ghép được vào nhau.
2. **Một issue một Pull Request một người, chỉ sửa tệp của mình** (R9, R19).
3. **Không ghi trạng thái trực tiếp** (R18): đi qua `Deal.canMoveTo`, `Listing.publish`, `Listing.reserve`, `Warning.resultingStatus`. Chúng cũng là chỗ lấy điểm unit test ở đợt 1.

Những luật khác (duyệt chéo, ghi quyết định, cập nhật tài liệu theo code) nếu phải bỏ để kịp thì bỏ, và nói thẳng trong báo cáo là đã bỏ.

## 7. Phần tài liệu còn treo: đừng làm nữa

Còn khoảng 166 câu hỏi mở trong các bản nháp issue. **Không trả lời trước.** Phần lớn tự rõ khi ngồi code màn đó. Ai gặp chỗ mơ hồ thì chọn cách hợp lý, ghi một dòng vào Pull Request, đi tiếp.

Bốn chỗ đã có cách hiểu mặc định, dùng luôn đừng hỏi lại:

- **`autoReleaseAt`**: ghi bằng giờ máy cộng 7 ngày; Security Rules cho sai số 5 phút so với `request.time`.
- **Dòng `StatusChange`**: repository thêm, không phải phương thức của `Deal`.
- **Chữ thông báo**: hằng tiếng Việt trong `NotificationRepository`, vì repository không có `Context`.
- **`NotificationRepository.create(notification, batch, cb)`**: `batch` khác null thì chỉ thêm lệnh ghi vào lô rồi gọi lại ngay, bên gọi commit.
