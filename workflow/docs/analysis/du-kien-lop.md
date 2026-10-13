# Danh sách lớp dự kiến

**92 lớp Java** chia theo tầng như đã chốt trong `AGENTS.md` mục 7. Danh sách này khớp từng lớp với sơ đồ lớp ở [`../diagrams/so-do-lop/`](../diagrams/so-do-lop/README.md). Cột "Chức năng liên quan" là số STT trong [bảng đặc tả chức năng](../product/dac-ta-chuc-nang.md).

| Gói | Số lớp |
| --- | ---: |
| `com.nhom3.pubgapp.model` | 23 |
| `com.nhom3.pubgapp.model.enums` | 22 |
| `com.nhom3.pubgapp.data` | 14 |
| `com.nhom3.pubgapp.data.local` | 12 |
| `com.nhom3.pubgapp.util` | 7 |
| `com.nhom3.pubgapp.viewmodel` | 14 |
| **Tổng** | **92** |

## Gói model, 23 lớp

| Lớp | Trách nhiệm | Chức năng liên quan |
| --- | --- | --- |
| `BaseDocument` | Lớp cha của mọi tài liệu Firestore: mã, thời điểm tạo và sửa. | hạ tầng dùng chung |
| `User` | Người dùng: hồ sơ công khai, khu vực, trạng thái tài khoản, tổng điểm đánh giá, cài đặt thông báo. | 1 đến 5, 7, 10, 12, 31 đến 35, 46, 59, 70 đến 73 |
| `ProfileStats` | Số liệu hồ sơ đếm bằng truy vấn tổng hợp của Firestore lúc mở hồ sơ; không lưu thành trường. | 8 |
| `UserAddress` | Địa chỉ giao nhận của một người dùng (mỗi người có nhiều địa chỉ). | 6 |
| `UserIdentity` | Thông tin định danh riêng tư (số CCCD, ngày sinh, giới tính, địa chỉ thường trú); chỉ chủ tài khoản và admin đọc được. | 1 đến 3, 9 |
| `SellerVerification` | Yêu cầu xác minh để trở thành người bán, do admin duyệt. | 73 |
| `Warning` | Một lần cảnh báo vi phạm; bậc 1 cảnh báo, bậc 2 khóa tạm, bậc 3 khóa vĩnh viễn. | 72 |
| `AppNotification` | Thông báo gửi cho một người dùng (offer, giao dịch, kết quả khiếu nại, cảnh báo). | 67, 91 |
| `Category` | Danh mục sản phẩm, có danh mục con và danh sách thuộc tính tình trạng riêng. | 21, 79, 80 |
| `Listing` | Tin đăng bán, trao đổi hoặc cho tặng; nhiều hình thức cùng lúc, giới hạn mức offer. | 13, 15 đến 23, 27, 50, 76, 78, 80 |
| `SearchFilter` | Đối tượng giá trị gom mọi tiêu chí tìm kiếm và lọc; không lưu trong Firestore. | 23 đến 28, 30 đến 34 |
| `Conversation` | Cuộc trò chuyện giữa người mua và người bán về một tin. | 43, 44 |
| `ChatMessage` | Tin nhắn chữ, ảnh hoặc một offer trong cuộc trò chuyện. | 43, 44 |
| `Offer` | Đề nghị một chạm: tiền, vật trao đổi, hoặc cả hai. | 47 đến 49 |
| `Deal` | Giao dịch giữa hai bên; kèm bằng chứng vận đơn, mốc đếm ngược tự giải ngân, bước Đã nhận trước Hoàn tất. | 50, 52 đến 56, 82, 84 |
| `StatusChange` | Một dòng lịch sử trạng thái của giao dịch (statusHistory), nằm trong Deal. | 82 |
| `Payment` | Khoản tiền ký quỹ mô phỏng: giữ, chuyển cho người bán, hoặc hoàn lại. | 53, 55, 56, 84 |
| `Review` | Đánh giá đối tác sau một giao dịch: sao tổng thể và 3 tiêu chí; mỗi bên đánh giá một lần. | 59 |
| `Report` | Báo cáo hành vi, phát ngôn hoặc tin đăng vi phạm. | 61, 62, 75 |
| `Complaint` | Đơn khiếu nại giao dịch kèm bằng chứng; adminNote là ghi chú nội bộ của admin. | 64, 66, 87, 89, 90 |
| `ActivityLog` | Nhật ký hành động quan trọng của một tài khoản để admin theo dõi. | 74 |
| `BannedKeyword` | Một từ khóa cấm; tin đăng chứa nó bị gắn cờ cần chú ý. | 78 |
| `MediaImage` | Một ảnh đã nén, lưu dạng Base64 trong Firestore (gói Spark không có Cloud Storage). | 17 |

## Gói model.enums, 22 lớp

| Lớp | Trách nhiệm | Chức năng liên quan |
| --- | --- | --- |
| `UserStatus` | Trạng thái tài khoản. | 12, 68 |
| `VerificationStatus` | Trạng thái xác minh người bán. | 73 |
| `Gender` | Giới tính trong thông tin định danh. | 9 |
| `NotificationType` | Loại thông báo. | 47 đến 49, 67 |
| `ListingStatus` | Trạng thái tin đăng. | 13, 22 |
| `TradeType` | Hình thức giao dịch: bán, trao đổi, cho tặng. | 18 |
| `ItemCondition` | Tình trạng món đồ: mới, như mới, đã sử dụng, đã sửa chữa, có lỗi. | 16, 26 |
| `SortOrder` | Cách sắp xếp kết quả. | 14, 31 đến 34 |
| `MessageType` | Loại tin nhắn. | 43, 44 |
| `ImagePurpose` | Ảnh dùng cho việc gì. | 17 |
| `OfferType` | Loại offer: tiền, vật, hoặc cả hai. | 20 |
| `OfferStatus` | Trạng thái offer. | 47 đến 49 |
| `DealStatus` | Trạng thái giao dịch. | 52 |
| `DeliveryMethod` | Phương thức giao nhận. | 50 |
| `PaymentStatus` | Trạng thái khoản ký quỹ. | 56, 84 |
| `ReportTarget` | Đối tượng bị báo cáo. | 61 |
| `ReportReason` | Lý do báo cáo chọn sẵn. | 61 |
| `ReportStatus` | Trạng thái báo cáo. | 62 |
| `ComplaintStatus` | Trạng thái khiếu nại. | 65 |
| `ComplaintReason` | Lý do khiếu nại chọn sẵn. | 64 |
| `ComplaintDecision` | Quyết định xử lý khiếu nại. | 89 |
| `Priority` | Mức ưu tiên khiếu nại. | 87 |

## Gói data, 14 lớp

| Lớp | Trách nhiệm | Chức năng liên quan |
| --- | --- | --- |
| `DataCallback` | Giao diện gọi lại chung: repository báo kết quả hoặc lỗi cho ViewModel. | hạ tầng dùng chung |
| `BaseRepository` | Lớp cha của mọi repository: giữ kết nối Firebase, ghi nhật ký, xử lý lỗi chung. | 74 |
| `AuthRepository` | Đăng ký, đăng nhập, quên mật khẩu, xác thực email, đổi mật khẩu, xóa thông tin đăng nhập. | 1 đến 3, 11, 12 |
| `UserRepository` | Hồ sơ, địa chỉ, thông tin định danh, cài đặt thông báo, theo dõi, lưu tin, yêu cầu xác minh, vô hiệu hóa tài khoản. | 4, 6, 8 đến 10, 12, 30, 39 đến 42, 46, 73 |
| `CategoryRepository` | Danh mục và danh mục con; admin thêm, sửa, xóa. | 21, 24, 79 |
| `ListingRepository` | Tin đăng, tìm kiếm, kênh xem; đọc bộ nhớ đệm trước rồi mới gọi mạng. | 13 đến 15, 22, 23, 29, 36, 38, 78 |
| `MediaRepository` | Nén ảnh, lưu dạng Base64 trong Firestore, tạo ảnh thu nhỏ, đọc ảnh kèm bộ nhớ đệm. | 5, 17, 43, 44, 54 |
| `ChatRepository` | Chat theo thời gian thực và offer một chạm. | 43, 44, 47 đến 50 |
| `DealRepository` | Vòng đời giao dịch và ký quỹ; giải ngân khi người mua nhận hàng hoặc khi hết thời hạn đếm ngược; tự hoàn tất sau khi hai bên đánh giá hoặc sau 3 ngày. | 50 đến 58, 81, 84 |
| `ReviewRepository` | Đánh giá và cập nhật điểm trung bình của người được đánh giá. | 7, 59, 60 |
| `ReportRepository` | Báo cáo vi phạm; người dùng xem, hủy và xuất lịch sử. | 61 đến 63, 75 |
| `ComplaintRepository` | Khiếu nại; ghi chú nội bộ nằm ở tài liệu con chỉ admin đọc được. | 64 đến 66, 87 đến 91 |
| `NotificationRepository` | Thông báo cho từng người dùng. | 67, 91 |
| `AdminRepository` | Thao tác dành riêng cho admin: người dùng, xác minh, kiểm duyệt, từ khóa cấm. | 68 đến 74, 76 đến 78 |

## Gói data.local, 12 lớp

| Lớp | Trách nhiệm | Chức năng liên quan |
| --- | --- | --- |
| `AppDatabase` | Cơ sở dữ liệu SQLite cục bộ; bị xóa sạch khi đăng xuất. | hạ tầng dùng chung |
| `Converters` | Đổi kiểu danh sách và thời gian sang kiểu SQLite lưu được. | hạ tầng dùng chung |
| `ListingEntity` | Bản sao tin đăng đã xem hoặc trong bảng tin để xem lại khi mất mạng. | hạ tầng dùng chung |
| `ConversationEntity` | Bản sao danh sách cuộc trò chuyện. | 43, 44 |
| `MessageEntity` | Bản sao tin nhắn đã xem. | 43, 44 |
| `DealEntity` | Bản sao lịch sử giao dịch của người dùng. | 57, 84 |
| `RecentViewEntity` | Lịch sử xem tin, nguồn của mục Dành cho bạn; chỉ nằm trên máy. | 38 |
| `RecentSearchEntity` | Lịch sử tìm kiếm, nguồn của mục Dành cho bạn; chỉ nằm trên máy. | 38 |
| `ListingDao` | Truy vấn bảng listing_cache. | hạ tầng dùng chung |
| `ChatDao` | Truy vấn conversation_cache và message_cache. | 43, 44 |
| `DealDao` | Truy vấn deal_cache. | 57 |
| `HistoryDao` | Truy vấn recent_view và recent_search. | 38 |

## Gói util, 7 lớp

| Lớp | Trách nhiệm | Chức năng liên quan |
| --- | --- | --- |
| `SessionManager` | Giữ người dùng đang đăng nhập và cờ admin cho toàn ứng dụng. | 1 đến 3 |
| `NetworkMonitor` | Theo dõi kết nối mạng để chọn dữ liệu cục bộ hay dữ liệu mạng. | hạ tầng dùng chung |
| `KeywordFilter` | So khớp chuỗi với từ khóa cấm để gắn cờ tin đăng. | 78 |
| `RecommendationEngine` | Chấm điểm trên máy để gợi ý sản phẩm tương tự, mục Dành cho bạn và khoảng giá tham khảo. | 29, 36 đến 38 |
| `GeoUtils` | Tính khoảng cách và lọc theo bán kính (Firestore không truy vấn được theo bán kính). | 27 |
| `FormatUtils` | Định dạng giá, thời gian; bỏ dấu tiếng Việt để tìm kiếm. | 23 |
| `AutoReleaseWorker` | Việc chạy nền định kỳ trên máy: tự chuyển giao dịch hết đếm ngược sang Đã nhận và giải ngân; tự hoàn tất giao dịch Đã nhận sau 3 ngày. | 84 |

## Gói viewmodel, 14 lớp

| Lớp | Trách nhiệm | Chức năng liên quan |
| --- | --- | --- |
| `AuthViewModel` | Đăng ký (kèm đồng ý điều khoản và số CCCD), đăng nhập, quên mật khẩu, đổi mật khẩu, chọn danh mục quan tâm. | 1 đến 3, 11, 35 |
| `ProfileViewModel` | Hồ sơ, ảnh, địa chỉ, thông tin định danh, cài đặt thông báo, theo dõi, danh sách đã lưu, yêu cầu xác minh, xóa tài khoản. | 4 đến 10, 12, 39 đến 42, 73 |
| `ShopViewModel` | Kệ hàng của người bán: đăng, lưu nháp, sửa, ẩn hoặc hiện, đổi trạng thái, sắp xếp. | 13 đến 15, 17, 22, 78 |
| `FeedViewModel` | Kênh chung, kênh đang theo dõi, sắp xếp, mục Dành cho bạn. | 29, 30, 38 |
| `SearchViewModel` | Từ khóa, bộ lọc và lịch sử tìm kiếm. | 23, 38 |
| `ListingDetailViewModel` | Chi tiết tin, người bán, tin tương tự, giá tham khảo, lưu tin. | 36, 37, 40 đến 42 |
| `ChatViewModel` | Danh sách chat, trạng thái hoạt động của người đối thoại, gửi chữ, ảnh và offer. | 43, 44, 46 đến 49 |
| `DealViewModel` | Tạo, sửa, xác nhận, thanh toán, giao (kèm vận đơn), nhận, hoàn tất, hủy, đếm ngược và lịch sử giao dịch. | 50 đến 58, 84 |
| `TrustViewModel` | Đánh giá, báo cáo và khiếu nại phía người dùng. | 59 đến 66 |
| `NotificationViewModel` | Danh sách thông báo và số chưa đọc. | 67, 91 |
| `AdminUserViewModel` | Admin: người dùng, hồ sơ, khóa, cảnh báo, xác minh, nhật ký. | 68 đến 74 |
| `AdminContentViewModel` | Admin: tin bị báo cáo, ẩn và gỡ, từ khóa cấm, danh mục. | 75 đến 80 |
| `AdminDealViewModel` | Admin: theo dõi danh sách và chi tiết giao dịch. | 81, 82 |
| `AdminComplaintViewModel` | Admin: xử lý khiếu nại, ra quyết định, ghi chú nội bộ, thông báo. | 87 đến 90 |
