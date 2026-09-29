# Danh sách lớp dự kiến

85 lớp chia theo 6 gói như đã chốt trong `AGENTS.md` mục 7. Cột "Dòng chức năng liên quan" ghi số STT trong [bảng đặc tả chức năng](../product/dac-ta-chuc-nang.md); lớp hạ tầng không phục vụ riêng dòng nào thì ghi "hạ tầng dùng chung".


## Gói model (com.nhom3.pubgapp.model), 20 lớp

| Lớp | Trách nhiệm | Dòng chức năng liên quan |
| --- | --- | --- |
| BaseDocument | Lớp cha của mọi tài liệu Firestore: mã, thời điểm tạo và sửa. | hạ tầng dùng chung |
| User | Người dùng: hồ sơ công khai, trạng thái tài khoản, điểm đánh giá. | 1, 2, 4, 5, 7, 31, 33, 60, 61, 62, 63 |
| UserAddress | Địa chỉ giao nhận của một người dùng (mỗi người có nhiều địa chỉ). | 6 |
| SellerVerification | Yêu cầu xác minh để trở thành người bán, do admin duyệt. | 65 |
| Warning | Một lần cảnh báo vi phạm; bậc 1 cảnh báo, bậc 2 khóa tạm, bậc 3 khóa vĩnh viễn. | 64 |
| AppNotification | Thông báo gửi cho một người dùng (offer, giao dịch, kết quả khiếu nại, cảnh báo). | 83 |
| Category | Danh mục sản phẩm, có danh mục con và danh sách thuộc tính tình trạng riêng. | 15, 17, 28, 71, 72 |
| Listing | Tin đăng bán, trao đổi hoặc cho tặng; nhiều hình thức cùng lúc, giới hạn mức offer. | 8, 9, 10, 11, 12, 13, 14, 15, 22, 23, 32, 34, 35, 36, 67, 68, 69 |
| SearchFilter | Đối tượng giá trị gom mọi tiêu chí tìm kiếm và lọc; không lưu trong Firestore. | 16, 17, 18, 19, 20, 21, 24, 25, 26, 27 |
| Conversation | Cuộc trò chuyện giữa người mua và người bán về một tin. | 37, 38, 39 |
| ChatMessage | Tin nhắn chữ, ảnh, video hoặc một offer trong cuộc trò chuyện. | 37, 38, 39, 40 |
| Offer | Đề nghị một chạm: tiền, vật trao đổi, hoặc cả hai. | 14, 40, 41, 42 |
| Deal | Giao dịch giữa hai bên; kèm bằng chứng vận đơn và mốc đếm ngược tự giải ngân. | 43, 44, 45, 46, 47, 48, 50, 73, 74 |
| StatusChange | Một dòng lịch sử trạng thái của giao dịch (statusHistory), nằm trong Deal. | 45, 46, 47, 48, 74 |
| Payment | Khoản tiền ký quỹ mô phỏng: giữ, chuyển cho người bán, hoặc hoàn lại. | 49, 51, 76 |
| Review | Đánh giá đối tác sau một giao dịch; mỗi bên đánh giá một lần. | 7, 52, 53 |
| Report | Báo cáo hành vi, phát ngôn hoặc tin đăng vi phạm. | 54, 55, 56, 67 |
| Complaint | Đơn khiếu nại giao dịch kèm bằng chứng; adminNote là ghi chú nội bộ của admin. | 57, 58, 59, 79, 80, 81, 82 |
| ActivityLog | Nhật ký hành động quan trọng của một tài khoản để admin theo dõi. | 66 |
| BannedKeyword | Một từ khóa cấm; tin đăng chứa nó bị gắn cờ cần chú ý. | 70 |

## Gói enums (com.nhom3.pubgapp.model.enums), 18 lớp

| Lớp | Trách nhiệm | Dòng chức năng liên quan |
| --- | --- | --- |
| UserStatus | Trạng thái tài khoản. | 62, 63, 64 |
| VerificationStatus | Trạng thái xác minh người bán. | 65 |
| NotificationType | Loại thông báo. | 83 |
| ListingStatus | Trạng thái tin đăng. | 8, 68, 69 |
| TradeType | Hình thức giao dịch: bán, trao đổi, cho tặng. | 12, 21 |
| ItemCondition | Tình trạng món đồ. | 19, 72 |
| SortOrder | Cách sắp xếp kết quả. | 24, 25, 26, 27 |
| MessageType | Loại tin nhắn. | 37, 38, 39, 40 |
| OfferType | Loại offer: tiền, vật, hoặc cả hai. | 14, 40 |
| OfferStatus | Trạng thái offer. | 40, 41, 42 |
| DealStatus | Trạng thái giao dịch. | 45, 46, 47, 48, 73 |
| DeliveryMethod | Phương thức giao nhận. | 43, 44 |
| PaymentStatus | Trạng thái khoản ký quỹ. | 49, 76 |
| ReportTarget | Đối tượng bị báo cáo. | 54 |
| ReportStatus | Trạng thái báo cáo. | 54, 55, 56, 67 |
| ComplaintStatus | Trạng thái khiếu nại. | 58, 59, 79 |
| ComplaintDecision | Quyết định xử lý khiếu nại. | 81 |
| Priority | Mức ưu tiên khiếu nại. | 79 |

## Gói data (com.nhom3.pubgapp.data), 14 lớp

| Lớp | Trách nhiệm | Dòng chức năng liên quan |
| --- | --- | --- |
| DataCallback | Giao diện gọi lại chung: repository báo kết quả hoặc lỗi cho ViewModel. | hạ tầng dùng chung |
| BaseRepository | Lớp cha của mọi repository: giữ kết nối Firebase, ghi nhật ký, xử lý lỗi chung. | hạ tầng dùng chung |
| AuthRepository | Đăng ký, đăng nhập, quên mật khẩu, xác thực email. | 1, 2, 3 |
| UserRepository | Hồ sơ, địa chỉ, theo dõi, lưu tin, yêu cầu xác minh. | 4, 5, 6, 7, 31, 32, 33, 34, 35, 36, 65 |
| CategoryRepository | Danh mục và danh mục con; admin thêm, sửa, xóa. | 15, 17, 28, 71, 72 |
| ListingRepository | Tin đăng, tìm kiếm, kênh xem; đọc bộ nhớ đệm trước rồi mới gọi mạng. | 8, 9, 10, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27 |
| MediaRepository | Tải ảnh và video lên Cloud Storage. | 5, 11, 38, 39, 57 |
| ChatRepository | Chat theo thời gian thực và offer một chạm. | 37, 38, 39, 40, 41, 42 |
| DealRepository | Vòng đời giao dịch và ký quỹ; giải ngân khi người mua nhận hàng hoặc khi hết thời hạn đếm ngược. | 43, 44, 45, 46, 47, 48, 49, 50, 51, 76 |
| ReviewRepository | Đánh giá và cập nhật điểm trung bình của người được đánh giá. | 7, 52, 53 |
| ReportRepository | Báo cáo vi phạm; người dùng xem, hủy và xuất lịch sử. | 54, 55, 56 |
| ComplaintRepository | Khiếu nại; ghi chú nội bộ nằm ở tài liệu con chỉ admin đọc được. | 57, 58, 59, 80, 81, 82 |
| NotificationRepository | Thông báo cho từng người dùng. | 83 |
| AdminRepository | Thao tác dành riêng cho admin: người dùng, xác minh, kiểm duyệt, từ khóa cấm. | 60, 61, 62, 63, 64, 65, 66, 67, 68, 69, 70, 73, 74, 79 |

## Gói local (com.nhom3.pubgapp.data.local), 12 lớp

| Lớp | Trách nhiệm | Dòng chức năng liên quan |
| --- | --- | --- |
| AppDatabase | Cơ sở dữ liệu SQLite cục bộ; bị xóa sạch khi đăng xuất. | hạ tầng dùng chung |
| Converters | Đổi kiểu danh sách và thời gian sang kiểu SQLite lưu được. | hạ tầng dùng chung |
| ListingEntity | Bản sao tin đăng đã xem hoặc trong bảng tin để xem lại khi mất mạng. | 22, 23 |
| ConversationEntity | Bản sao danh sách cuộc trò chuyện. | 37 |
| MessageEntity | Bản sao tin nhắn đã xem. | 37, 38, 39 |
| DealEntity | Bản sao lịch sử giao dịch của người dùng. | 50 |
| RecentViewEntity | Lịch sử xem tin, nguồn của mục Dành cho bạn; chỉ nằm trên máy. | 30 |
| RecentSearchEntity | Lịch sử tìm kiếm, nguồn của mục Dành cho bạn; chỉ nằm trên máy. | 16, 30 |
| ListingDao | Truy vấn bảng listing_cache. | 22, 23 |
| ChatDao | Truy vấn conversation_cache và message_cache. | 37, 38, 39 |
| DealDao | Truy vấn deal_cache. | 50 |
| HistoryDao | Truy vấn recent_view và recent_search. | 16, 30 |

## Gói util (com.nhom3.pubgapp.util), 7 lớp

| Lớp | Trách nhiệm | Dòng chức năng liên quan |
| --- | --- | --- |
| SessionManager | Giữ người dùng đang đăng nhập và cờ admin cho toàn ứng dụng. | 2 |
| NetworkMonitor | Theo dõi kết nối mạng để chọn dữ liệu cục bộ hay dữ liệu mạng. | hạ tầng dùng chung |
| KeywordFilter | So khớp chuỗi với từ khóa cấm để gắn cờ tin đăng. | 70 |
| RecommendationEngine | Chấm điểm trên máy để gợi ý sản phẩm tương tự và mục Dành cho bạn. | 28, 29, 30 |
| GeoUtils | Tính khoảng cách và lọc theo bán kính (Firestore không truy vấn được theo bán kính). | 20 |
| FormatUtils | Định dạng giá, thời gian; bỏ dấu tiếng Việt để tìm kiếm. | 16, 18 |
| AutoReleaseWorker | Việc chạy nền định kỳ trên máy: tự giải ngân các giao dịch đã hết hạn đếm ngược. | 76 |

## Gói viewmodel (com.nhom3.pubgapp.viewmodel), 14 lớp

| Lớp | Trách nhiệm | Dòng chức năng liên quan |
| --- | --- | --- |
| AuthViewModel | Đăng ký, đăng nhập, quên mật khẩu, chọn danh mục quan tâm. | 1, 2, 3, 28 |
| ProfileViewModel | Hồ sơ, ảnh, địa chỉ, theo dõi, danh sách đã lưu, yêu cầu xác minh. | 4, 5, 6, 7, 31, 33, 35, 65 |
| ShopViewModel | Kệ hàng của người bán: đăng, sửa, đổi trạng thái, sắp xếp. | 8, 9, 10, 11, 12, 13, 14, 15 |
| FeedViewModel | Kênh chung, kênh đang theo dõi, sắp xếp, mục Dành cho bạn. | 22, 23, 24, 25, 26, 27, 30 |
| SearchViewModel | Từ khóa, bộ lọc và lịch sử tìm kiếm. | 16, 17, 18, 19, 20, 21 |
| ListingDetailViewModel | Chi tiết tin, người bán, tin tương tự, lưu tin. | 29, 32, 34, 36 |
| ChatViewModel | Danh sách chat, gửi chữ, ảnh, video và offer. | 37, 38, 39, 40, 41, 42 |
| DealViewModel | Tạo, sửa, xác nhận, thanh toán, giao (kèm vận đơn), nhận, hủy, đếm ngược và lịch sử giao dịch. | 43, 44, 45, 46, 47, 48, 49, 50, 51 |
| TrustViewModel | Đánh giá, báo cáo và khiếu nại phía người dùng. | 52, 53, 54, 55, 56, 57, 58, 59 |
| NotificationViewModel | Danh sách thông báo và số chưa đọc. | 83 |
| AdminUserViewModel | Admin: người dùng, hồ sơ, khóa, cảnh báo, xác minh, nhật ký. | 60, 61, 62, 63, 64, 65, 66 |
| AdminContentViewModel | Admin: tin bị báo cáo, ẩn và gỡ, từ khóa cấm, danh mục. | 67, 68, 69, 70, 71, 72 |
| AdminDealViewModel | Admin: theo dõi danh sách và chi tiết giao dịch. | 73, 74 |
| AdminComplaintViewModel | Admin: xử lý khiếu nại, ra quyết định, ghi chú nội bộ, thông báo. | 79, 80, 81, 82, 83 |
