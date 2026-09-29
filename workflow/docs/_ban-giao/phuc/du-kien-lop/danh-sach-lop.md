# Danh sách lớp dự kiến (85 lớp), tham khảo cho workflow/docs/analysis/du-kien-lop.md


## Gói model (com.nhom3.pubgapp.model), 20 lớp

| Lớp | Trách nhiệm |
| --- | --- |
| BaseDocument | Lớp cha của mọi tài liệu Firestore: mã, thời điểm tạo và sửa. |
| User | Người dùng: hồ sơ công khai, trạng thái tài khoản, điểm đánh giá. |
| UserAddress | Địa chỉ giao nhận của một người dùng (mỗi người có nhiều địa chỉ). |
| SellerVerification | Yêu cầu xác minh để trở thành người bán, do admin duyệt. |
| Warning | Một lần cảnh báo vi phạm; bậc 1 cảnh báo, bậc 2 khóa tạm, bậc 3 khóa vĩnh viễn. |
| AppNotification | Thông báo gửi cho một người dùng (offer, giao dịch, kết quả khiếu nại, cảnh báo). |
| Category | Danh mục sản phẩm, có danh mục con và danh sách thuộc tính tình trạng riêng. |
| Listing | Tin đăng bán, trao đổi hoặc cho tặng; nhiều hình thức cùng lúc, giới hạn mức offer. |
| SearchFilter | Đối tượng giá trị gom mọi tiêu chí tìm kiếm và lọc; không lưu trong Firestore. |
| Conversation | Cuộc trò chuyện giữa người mua và người bán về một tin. |
| ChatMessage | Tin nhắn chữ, ảnh, video hoặc một offer trong cuộc trò chuyện. |
| Offer | Đề nghị một chạm: tiền, vật trao đổi, hoặc cả hai. |
| Deal | Giao dịch giữa hai bên; kèm bằng chứng vận đơn và mốc đếm ngược tự giải ngân. |
| StatusChange | Một dòng lịch sử trạng thái của giao dịch (statusHistory), nằm trong Deal. |
| Payment | Khoản tiền ký quỹ mô phỏng: giữ, chuyển cho người bán, hoặc hoàn lại. |
| Review | Đánh giá đối tác sau một giao dịch; mỗi bên đánh giá một lần. |
| Report | Báo cáo hành vi, phát ngôn hoặc tin đăng vi phạm. |
| Complaint | Đơn khiếu nại giao dịch kèm bằng chứng; adminNote là ghi chú nội bộ của admin. |
| ActivityLog | Nhật ký hành động quan trọng của một tài khoản để admin theo dõi. |
| BannedKeyword | Một từ khóa cấm; tin đăng chứa nó bị gắn cờ cần chú ý. |

## Gói enums (com.nhom3.pubgapp.model.enums), 18 lớp

| Lớp | Trách nhiệm |
| --- | --- |
| UserStatus | Trạng thái tài khoản. |
| VerificationStatus | Trạng thái xác minh người bán. |
| NotificationType | Loại thông báo. |
| ListingStatus | Trạng thái tin đăng. |
| TradeType | Hình thức giao dịch: bán, trao đổi, cho tặng. |
| ItemCondition | Tình trạng món đồ. |
| SortOrder | Cách sắp xếp kết quả. |
| MessageType | Loại tin nhắn. |
| OfferType | Loại offer: tiền, vật, hoặc cả hai. |
| OfferStatus | Trạng thái offer. |
| DealStatus | Trạng thái giao dịch. |
| DeliveryMethod | Phương thức giao nhận. |
| PaymentStatus | Trạng thái khoản ký quỹ. |
| ReportTarget | Đối tượng bị báo cáo. |
| ReportStatus | Trạng thái báo cáo. |
| ComplaintStatus | Trạng thái khiếu nại. |
| ComplaintDecision | Quyết định xử lý khiếu nại. |
| Priority | Mức ưu tiên khiếu nại. |

## Gói data (com.nhom3.pubgapp.data), 14 lớp

| Lớp | Trách nhiệm |
| --- | --- |
| DataCallback | Giao diện gọi lại chung: repository báo kết quả hoặc lỗi cho ViewModel. |
| BaseRepository | Lớp cha của mọi repository: giữ kết nối Firebase, ghi nhật ký, xử lý lỗi chung. |
| AuthRepository | Đăng ký, đăng nhập, quên mật khẩu, xác thực email. |
| UserRepository | Hồ sơ, địa chỉ, theo dõi, lưu tin, yêu cầu xác minh. |
| CategoryRepository | Danh mục và danh mục con; admin thêm, sửa, xóa. |
| ListingRepository | Tin đăng, tìm kiếm, kênh xem; đọc bộ nhớ đệm trước rồi mới gọi mạng. |
| MediaRepository | Tải ảnh và video lên Cloud Storage. |
| ChatRepository | Chat theo thời gian thực và offer một chạm. |
| DealRepository | Vòng đời giao dịch và ký quỹ; giải ngân khi người mua nhận hàng hoặc khi hết thời hạn đếm ngược. |
| ReviewRepository | Đánh giá và cập nhật điểm trung bình của người được đánh giá. |
| ReportRepository | Báo cáo vi phạm; người dùng xem, hủy và xuất lịch sử. |
| ComplaintRepository | Khiếu nại; ghi chú nội bộ nằm ở tài liệu con chỉ admin đọc được. |
| NotificationRepository | Thông báo cho từng người dùng. |
| AdminRepository | Thao tác dành riêng cho admin: người dùng, xác minh, kiểm duyệt, từ khóa cấm. |

## Gói local (com.nhom3.pubgapp.data.local), 12 lớp

| Lớp | Trách nhiệm |
| --- | --- |
| AppDatabase | Cơ sở dữ liệu SQLite cục bộ; bị xóa sạch khi đăng xuất. |
| Converters | Đổi kiểu danh sách và thời gian sang kiểu SQLite lưu được. |
| ListingEntity | Bản sao tin đăng đã xem hoặc trong bảng tin để xem lại khi mất mạng. |
| ConversationEntity | Bản sao danh sách cuộc trò chuyện. |
| MessageEntity | Bản sao tin nhắn đã xem. |
| DealEntity | Bản sao lịch sử giao dịch của người dùng. |
| RecentViewEntity | Lịch sử xem tin, nguồn của mục Dành cho bạn; chỉ nằm trên máy. |
| RecentSearchEntity | Lịch sử tìm kiếm, nguồn của mục Dành cho bạn; chỉ nằm trên máy. |
| ListingDao | Truy vấn bảng listing_cache. |
| ChatDao | Truy vấn conversation_cache và message_cache. |
| DealDao | Truy vấn deal_cache. |
| HistoryDao | Truy vấn recent_view và recent_search. |

## Gói util (com.nhom3.pubgapp.util), 7 lớp

| Lớp | Trách nhiệm |
| --- | --- |
| SessionManager | Giữ người dùng đang đăng nhập và cờ admin cho toàn ứng dụng. |
| NetworkMonitor | Theo dõi kết nối mạng để chọn dữ liệu cục bộ hay dữ liệu mạng. |
| KeywordFilter | So khớp chuỗi với từ khóa cấm để gắn cờ tin đăng. |
| RecommendationEngine | Chấm điểm trên máy để gợi ý sản phẩm tương tự và mục Dành cho bạn. |
| GeoUtils | Tính khoảng cách và lọc theo bán kính (Firestore không truy vấn được theo bán kính). |
| FormatUtils | Định dạng giá, thời gian; bỏ dấu tiếng Việt để tìm kiếm. |
| AutoReleaseWorker | Việc chạy nền định kỳ trên máy: tự giải ngân các giao dịch đã hết hạn đếm ngược. |

## Gói viewmodel (com.nhom3.pubgapp.viewmodel), 14 lớp

| Lớp | Trách nhiệm |
| --- | --- |
| AuthViewModel | Đăng ký, đăng nhập, quên mật khẩu, chọn danh mục quan tâm. |
| ProfileViewModel | Hồ sơ, ảnh, địa chỉ, theo dõi, danh sách đã lưu, yêu cầu xác minh. |
| ShopViewModel | Kệ hàng của người bán: đăng, sửa, đổi trạng thái, sắp xếp. |
| FeedViewModel | Kênh chung, kênh đang theo dõi, sắp xếp, mục Dành cho bạn. |
| SearchViewModel | Từ khóa, bộ lọc và lịch sử tìm kiếm. |
| ListingDetailViewModel | Chi tiết tin, người bán, tin tương tự, lưu tin. |
| ChatViewModel | Danh sách chat, gửi chữ, ảnh, video và offer. |
| DealViewModel | Tạo, sửa, xác nhận, thanh toán, giao (kèm vận đơn), nhận, hủy, đếm ngược và lịch sử giao dịch. |
| TrustViewModel | Đánh giá, báo cáo và khiếu nại phía người dùng. |
| NotificationViewModel | Danh sách thông báo và số chưa đọc. |
| AdminUserViewModel | Admin: người dùng, hồ sơ, khóa, cảnh báo, xác minh, nhật ký. |
| AdminContentViewModel | Admin: tin bị báo cáo, ẩn và gỡ, từ khóa cấm, danh mục. |
| AdminDealViewModel | Admin: theo dõi danh sách và chi tiết giao dịch. |
| AdminComplaintViewModel | Admin: xử lý khiếu nại, ra quyết định, ghi chú nội bộ, thông báo. |