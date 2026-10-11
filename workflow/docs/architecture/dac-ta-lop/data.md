# Đặc tả lớp: gói `com.nhom3.pubgapp.data`

Bản chữ của sơ đồ lớp cho gói này: 14 lớp. Mục lục và cách đọc ở [README.md](README.md).

Tầng truy cập dữ liệu. ViewModel chỉ nói chuyện với repository, không gọi Firebase hay Room trực tiếp. Mọi phương thức bất đồng bộ trả kết quả qua `DataCallback<T>` (không dùng lambda, theo luật viết code). Kiểu tham số ghi theo thiết kế; khi code mà cần đổi thì sửa tài liệu này trong cùng Pull Request.

| Lớp | Loại | Trách nhiệm |
| --- | --- | --- |
| [`DataCallback`](#datacallback) | interface | Giao diện gọi lại chung: repository báo kết quả hoặc lỗi cho ViewModel. |
| [`BaseRepository`](#baserepository) | lớp trừu tượng | Lớp cha của mọi repository: giữ kết nối Firebase, ghi nhật ký, xử lý lỗi chung. |
| [`AuthRepository`](#authrepository) | lớp (repository) | Đăng ký, đăng nhập, quên mật khẩu, xác thực email, đổi mật khẩu, xóa thông tin đăng nhập. |
| [`UserRepository`](#userrepository) | lớp (repository) | Hồ sơ, địa chỉ, thông tin định danh, cài đặt thông báo, theo dõi, lưu tin, yêu cầu xác minh, vô hiệu hóa tài khoản. |
| [`CategoryRepository`](#categoryrepository) | lớp (repository) | Danh mục và danh mục con; admin thêm, sửa, xóa. |
| [`ListingRepository`](#listingrepository) | lớp (repository) | Tin đăng, tìm kiếm, kênh xem; đọc bộ nhớ đệm trước rồi mới gọi mạng. |
| [`MediaRepository`](#mediarepository) | lớp (repository) | Tải ảnh và video lên Cloud Storage. |
| [`ChatRepository`](#chatrepository) | lớp (repository) | Chat theo thời gian thực và offer một chạm. |
| [`DealRepository`](#dealrepository) | lớp (repository) | Vòng đời giao dịch và ký quỹ; giải ngân khi người mua nhận hàng hoặc khi hết thời hạn đếm ngược; tự hoàn tất sau khi hai bên đánh giá hoặc sau 3 ngày. |
| [`ReviewRepository`](#reviewrepository) | lớp (repository) | Đánh giá và cập nhật điểm trung bình của người được đánh giá. |
| [`ReportRepository`](#reportrepository) | lớp (repository) | Báo cáo vi phạm; người dùng xem, hủy và xuất lịch sử. |
| [`ComplaintRepository`](#complaintrepository) | lớp (repository) | Khiếu nại; ghi chú nội bộ nằm ở tài liệu con chỉ admin đọc được. |
| [`NotificationRepository`](#notificationrepository) | lớp (repository) | Thông báo cho từng người dùng. |
| [`AdminRepository`](#adminrepository) | lớp (repository) | Thao tác dành riêng cho admin: người dùng, xác minh, kiểm duyệt, từ khóa cấm. |

### DataCallback

| Mục | Nội dung |
| --- | --- |
| Định danh | `com.nhom3.pubgapp.data.DataCallback` |
| Loại | interface |
| Lưu trữ | Không lưu |
| Trách nhiệm | Giao diện gọi lại chung: repository báo kết quả hoặc lỗi cho ViewModel. |
| Chức năng liên quan (STT) | hạ tầng dùng chung |

**Phương thức**

| Chữ ký | Mô tả | STT |
| --- | --- | --- |
| `public void onSuccess(T result)` | Gọi khi thao tác thành công, kèm kết quả. |  |
| `public void onError(String message)` | Gọi khi thất bại, kèm câu báo lỗi đã dịch sang tiếng Việt để hiện cho người dùng. |  |

**Quan hệ trên sơ đồ lớp**

- Phụ thuộc: `BaseRepository` dùng `DataCallback` (báo kết quả). Trang 5. Tầng dữ liệu.

**Được dùng bởi:** [`AdminRepository`](data.md#adminrepository), [`AuthRepository`](data.md#authrepository), [`BaseRepository`](data.md#baserepository), [`CategoryRepository`](data.md#categoryrepository), [`ChatRepository`](data.md#chatrepository), [`ComplaintRepository`](data.md#complaintrepository), [`DealRepository`](data.md#dealrepository), [`ListingRepository`](data.md#listingrepository), [`MediaRepository`](data.md#mediarepository), [`NotificationRepository`](data.md#notificationrepository), [`ReportRepository`](data.md#reportrepository), [`ReviewRepository`](data.md#reviewrepository), [`UserRepository`](data.md#userrepository)

### BaseRepository

| Mục | Nội dung |
| --- | --- |
| Định danh | `com.nhom3.pubgapp.data.BaseRepository` |
| Loại | lớp trừu tượng |
| Lưu trữ | Không lưu |
| Trách nhiệm | Lớp cha của mọi repository: giữ kết nối Firebase, ghi nhật ký, xử lý lỗi chung. |
| Chức năng liên quan (STT) | 74 |

**Thuộc tính**

| Tên | Kiểu | Truy cập | Ý nghĩa |
| --- | --- | --- | --- |
| `db` | `FirebaseFirestore` | protected | Kết nối Cloud Firestore dùng chung. |
| `auth` | `FirebaseAuth` | protected | Kết nối Firebase Authentication dùng chung. |

**Phương thức**

| Chữ ký | Mô tả | STT |
| --- | --- | --- |
| `protected String currentUserId()` | Trả `uid` của người đang đăng nhập; rỗng khi chưa đăng nhập. |  |
| `protected void logActivity(String action, String targetType, String targetId)` | Ghi một `ActivityLog` cho hành động quan trọng. | 74 |
| `protected void fail(DataCallback, Exception)` | Dịch ngoại lệ Firebase thành câu báo lỗi và gọi `onError`. |  |

**Quan hệ trên sơ đồ lớp**

- `AuthRepository` kế thừa `BaseRepository`. Trang 5. Tầng dữ liệu.
- `UserRepository` kế thừa `BaseRepository`. Trang 5. Tầng dữ liệu.
- `CategoryRepository` kế thừa `BaseRepository`. Trang 5. Tầng dữ liệu.
- `ListingRepository` kế thừa `BaseRepository`. Trang 5. Tầng dữ liệu.
- `MediaRepository` kế thừa `BaseRepository`. Trang 5. Tầng dữ liệu.
- `ChatRepository` kế thừa `BaseRepository`. Trang 5. Tầng dữ liệu.
- `ReviewRepository` kế thừa `BaseRepository`. Trang 5. Tầng dữ liệu.
- `ReportRepository` kế thừa `BaseRepository`. Trang 5. Tầng dữ liệu.
- `ComplaintRepository` kế thừa `BaseRepository`. Trang 5. Tầng dữ liệu.
- `NotificationRepository` kế thừa `BaseRepository`. Trang 5. Tầng dữ liệu.
- `AdminRepository` kế thừa `BaseRepository`. Trang 5. Tầng dữ liệu.
- `DealRepository` kế thừa `BaseRepository`. Trang 5. Tầng dữ liệu.
- Phụ thuộc: `BaseRepository` dùng `DataCallback` (báo kết quả). Trang 5. Tầng dữ liệu.

**Dùng tới:** [`DataCallback`](data.md#datacallback)

### AuthRepository

| Mục | Nội dung |
| --- | --- |
| Định danh | `com.nhom3.pubgapp.data.AuthRepository` |
| Loại | lớp (repository) |
| Kế thừa | [`BaseRepository`](data.md#baserepository) |
| Lưu trữ | Không lưu |
| Trách nhiệm | Đăng ký, đăng nhập, quên mật khẩu, xác thực email, đổi mật khẩu, xóa thông tin đăng nhập. |
| Chức năng liên quan (STT) | 1 đến 3, 11, 12 |

**Phương thức**

| Chữ ký | Mô tả | STT |
| --- | --- | --- |
| `public void signUp(String email, String password, DataCallback<String> cb)` | Tạo tài khoản email và mật khẩu trên Firebase Authentication, rồi gửi email xác thực. | 1 đến 3 |
| `public void sendVerificationEmail(DataCallback<Void> cb)` | Gửi lại email xác thực. | 1 đến 3 |
| `public void signIn(String email, String password, DataCallback<User> cb)` | Đăng nhập bằng email và mật khẩu. Tài khoản bị khóa hoặc đã vô hiệu hóa thì báo lỗi và đăng xuất. | 1 đến 3 |
| `public void signInWithGoogle(String idToken, DataCallback<User> cb)` | Đăng nhập bằng mã của Google Sign-In; lần đầu thì tạo tài liệu `users/{uid}`. | 1 đến 3 |
| `public void sendPasswordReset(String email, DataCallback<Void> cb)` | Gửi email đặt lại mật khẩu. | 1 đến 3 |
| `public void changePassword(String current, String next, DataCallback<Void> cb)` | Xác thực lại bằng mật khẩu hiện tại rồi đổi sang mật khẩu mới. | 11 |
| `public void deleteLogin(DataCallback<Void> cb)` | Xóa tài khoản đăng nhập trên Firebase Authentication (bước cuối của vô hiệu hóa). | 12 |
| `public void signOut()` | Đăng xuất; người gọi phải xóa bộ nhớ đệm Room và `SessionManager`. |  |
| `public boolean isEmailVerified()` | True khi email đã xác thực. |  |

**Quan hệ trên sơ đồ lớp**

- `AuthRepository` kế thừa `BaseRepository`. Trang 5. Tầng dữ liệu.

**Dùng tới:** [`DataCallback`](data.md#datacallback), [`User`](model.md#user)

**Được dùng bởi:** [`AuthViewModel`](viewmodel.md#authviewmodel), [`ProfileViewModel`](viewmodel.md#profileviewmodel)

### UserRepository

| Mục | Nội dung |
| --- | --- |
| Định danh | `com.nhom3.pubgapp.data.UserRepository` |
| Loại | lớp (repository) |
| Kế thừa | [`BaseRepository`](data.md#baserepository) |
| Lưu trữ | Không lưu |
| Trách nhiệm | Hồ sơ, địa chỉ, thông tin định danh, cài đặt thông báo, theo dõi, lưu tin, yêu cầu xác minh, vô hiệu hóa tài khoản. |
| Chức năng liên quan (STT) | 4, 6, 9, 10, 12, 30, 39 đến 42, 46, 73 |

**Phương thức**

| Chữ ký | Mô tả | STT |
| --- | --- | --- |
| `public void getProfile(String uid, DataCallback<User> cb)` | Đọc `users/{uid}`. |  |
| `public void updateProfile(User user, DataCallback<Void> cb)` | Ghi các trường hồ sơ mà chính chủ được sửa (tên, điện thoại, khu vực, giới thiệu, ảnh, danh mục quan tâm). | 4 |
| `public void saveAddress(String uid, UserAddress address, DataCallback<Void> cb)` | Tạo hoặc sửa một địa chỉ; nếu đặt mặc định thì bỏ cờ mặc định của địa chỉ khác. | 6 |
| `public void deleteAddress(String uid, String id, DataCallback<Void> cb)` | Xóa một địa chỉ. | 6 |
| `public void getAddresses(String uid, DataCallback<List<UserAddress>> cb)` | Đọc mọi địa chỉ của người dùng, địa chỉ mặc định trước. |  |
| `public void follow(String uid, String otherId, DataCallback<Void> cb)` | Tạo `follows/{uid_otherId}` và tăng `followerCount` của người được theo dõi. | 39 |
| `public void unfollow(String uid, String otherId, DataCallback<Void> cb)` | Xóa tài liệu theo dõi và giảm `followerCount`. | 39 |
| `public void getFollowingIds(String uid, DataCallback<List<String>> cb)` | Mã những người mà người dùng đang theo dõi. | 30 |
| `public void saveListing(String uid, String listingId, DataCallback<Void> cb)` | Tạo `users/{uid}/saved/{listingId}`. | 40 đến 42 |
| `public void unsaveListing(String uid, String listingId, DataCallback<Void> cb)` | Xóa `users/{uid}/saved/{listingId}`. | 40 đến 42 |
| `public void getSavedIds(String uid, DataCallback<List<String>> cb)` | Mã các tin đã lưu, mới lưu trước. | 40 đến 42 |
| `public void requestVerification(String uid, DataCallback<Void> cb)` | Tạo hoặc gửi lại `sellerVerifications/{uid}` với trạng thái PENDING. | 73 |
| `public void saveIdentity(String uid, UserIdentity identity, DataCallback<Void> cb)` | Ghi `users/{uid}/private/identity`. | 9 |
| `public void getIdentity(String uid, DataCallback<UserIdentity> cb)` | Đọc `users/{uid}/private/identity`; chỉ chính chủ và admin đọc được. | 9 |
| `public void updateNotificationPrefs(String uid, Map<String,Boolean> prefs, DataCallback<Void> cb)` | Ghi `notificationPrefs`. | 10 |
| `public void touchLastActive(String uid)` | Ghi `lastActiveAt`; gọi khi mở ứng dụng và khi vào chat. | 46 |
| `public void deactivate(String uid, DataCallback<Void> cb)` | Từ chối nếu còn giao dịch chưa kết thúc; nếu không, đặt `status` DELETED và ẩn mọi tin của người này. | 12 |

**Quan hệ trên sơ đồ lớp**

- `UserRepository` kế thừa `BaseRepository`. Trang 5. Tầng dữ liệu.

**Dùng tới:** [`DataCallback`](data.md#datacallback), [`User`](model.md#user), [`UserAddress`](model.md#useraddress), [`UserIdentity`](model.md#useridentity)

**Được dùng bởi:** [`AdminUserViewModel`](viewmodel.md#adminuserviewmodel), [`AuthViewModel`](viewmodel.md#authviewmodel), [`ChatViewModel`](viewmodel.md#chatviewmodel), [`DealViewModel`](viewmodel.md#dealviewmodel), [`FeedViewModel`](viewmodel.md#feedviewmodel), [`ListingDetailViewModel`](viewmodel.md#listingdetailviewmodel), [`ProfileViewModel`](viewmodel.md#profileviewmodel)

### CategoryRepository

| Mục | Nội dung |
| --- | --- |
| Định danh | `com.nhom3.pubgapp.data.CategoryRepository` |
| Loại | lớp (repository) |
| Kế thừa | [`BaseRepository`](data.md#baserepository) |
| Lưu trữ | Không lưu |
| Trách nhiệm | Danh mục và danh mục con; admin thêm, sửa, xóa. |
| Chức năng liên quan (STT) | 21, 24, 79 |

**Phương thức**

| Chữ ký | Mô tả | STT |
| --- | --- | --- |
| `public void getAll(DataCallback<List<Category>> cb)` | Mọi danh mục còn dùng, kèm danh mục con. | 21 |
| `public void getChildren(String parentId, DataCallback<List<Category>> cb)` | Danh mục con của một danh mục. | 24 |
| `public void add(Category category, DataCallback<String> cb)` | Admin thêm danh mục. | 79 |
| `public void update(Category category, DataCallback<Void> cb)` | Admin sửa tên, danh mục cha, thuộc tính tình trạng hoặc ẩn danh mục. | 79 |
| `public void delete(String id, DataCallback<Void> cb)` | Admin xóa danh mục; từ chối nếu còn tin hoặc còn danh mục con, khi đó chỉ được ẩn (`isActive` false). | 79 |

**Quan hệ trên sơ đồ lớp**

- `CategoryRepository` kế thừa `BaseRepository`. Trang 5. Tầng dữ liệu.

**Dùng tới:** [`Category`](model.md#category), [`DataCallback`](data.md#datacallback)

**Được dùng bởi:** [`AdminContentViewModel`](viewmodel.md#admincontentviewmodel), [`SearchViewModel`](viewmodel.md#searchviewmodel), [`ShopViewModel`](viewmodel.md#shopviewmodel)

### ListingRepository

| Mục | Nội dung |
| --- | --- |
| Định danh | `com.nhom3.pubgapp.data.ListingRepository` |
| Loại | lớp (repository) |
| Kế thừa | [`BaseRepository`](data.md#baserepository) |
| Lưu trữ | Không lưu |
| Trách nhiệm | Tin đăng, tìm kiếm, kênh xem; đọc bộ nhớ đệm trước rồi mới gọi mạng. |
| Chức năng liên quan (STT) | 13 đến 15, 22, 23, 29, 36, 38 |

**Phương thức**

| Chữ ký | Mô tả | STT |
| --- | --- | --- |
| `public void create(Listing listing, DataCallback<String> cb)` | Gắn cờ nếu chứa từ khóa cấm, tạo `searchKeywords`, ghi tin AVAILABLE, tăng `Category.listingCount`. | 13 |
| `public void saveDraft(Listing listing, DataCallback<String> cb)` | Ghi tin với trạng thái DRAFT; không hiện cho người khác. | 22 |
| `public void update(Listing listing, DataCallback<Void> cb)` | Người đăng sửa tin; gắn cờ lại nếu chứa từ khóa cấm, tạo lại `searchKeywords`. | 13 |
| `public void setHidden(String id, boolean hidden, DataCallback<Void> cb)` | Người bán tự ẩn (HIDDEN, lý do rỗng) hoặc hiện lại (AVAILABLE). | 15 |
| `public void delete(String id, DataCallback<Void> cb)` | Người đăng xóa tin chưa có giao dịch (DRAFT, AVAILABLE, HIDDEN); tin đã có giao dịch thì không xóa được. | 13 |
| `public void getById(String id, DataCallback<Listing> cb)` | Đọc một tin; offline thì đọc `listing_cache`. |  |
| `public void getBySeller(String sellerId, DataCallback<List<Listing>> cb)` | Tin của một người bán, sắp theo thời gian; với chính chủ thì gồm cả nháp và tin ẩn. | 14 |
| `public void getFeed(SearchFilter filter, int page, DataCallback<List<Listing>> cb)` | Tin AVAILABLE theo bộ lọc, mỗi lần một trang; đọc Room trước khi offline, ghi kết quả mới vào Room. | 29 |
| `public void search(SearchFilter filter, DataCallback<List<Listing>> cb)` | Truy vấn theo từ khóa đầu tiên (`array-contains`) và các điều kiện bằng, phần còn lại lọc và sắp trên máy. | 23 |
| `public void getSimilar(Listing listing, DataCallback<List<Listing>> cb)` | Tin AVAILABLE cùng danh mục để `RecommendationEngine` chấm điểm. | 36 |
| `public void recordView(Listing listing)` | Ghi `RecentViewEntity` và cập nhật `ListingEntity.viewedAtMillis`. | 38 |
| `public void recordSearch(String keyword)` | Ghi `RecentSearchEntity`. | 38 |

**Quan hệ trên sơ đồ lớp**

- `ListingRepository` kế thừa `BaseRepository`. Trang 5. Tầng dữ liệu.

**Dùng tới:** [`DataCallback`](data.md#datacallback), [`HistoryDao`](data-local.md#historydao), [`Listing`](model.md#listing), [`ListingDao`](data-local.md#listingdao), [`SearchFilter`](model.md#searchfilter)

**Được dùng bởi:** [`AdminContentViewModel`](viewmodel.md#admincontentviewmodel), [`AdminUserViewModel`](viewmodel.md#adminuserviewmodel), [`FeedViewModel`](viewmodel.md#feedviewmodel), [`HistoryDao`](data-local.md#historydao), [`ListingDao`](data-local.md#listingdao), [`ListingDetailViewModel`](viewmodel.md#listingdetailviewmodel), [`ProfileViewModel`](viewmodel.md#profileviewmodel), [`SearchViewModel`](viewmodel.md#searchviewmodel), [`ShopViewModel`](viewmodel.md#shopviewmodel)

### MediaRepository

| Mục | Nội dung |
| --- | --- |
| Định danh | `com.nhom3.pubgapp.data.MediaRepository` |
| Loại | lớp (repository) |
| Kế thừa | [`BaseRepository`](data.md#baserepository) |
| Lưu trữ | Không lưu |
| Trách nhiệm | Tải ảnh và video lên Cloud Storage. |
| Chức năng liên quan (STT) | 5, 17, 43 đến 45, 54 |

**Phương thức**

| Chữ ký | Mô tả | STT |
| --- | --- | --- |
| `public void uploadImage(Uri uri, String path, DataCallback<String> cb)` | Nén ảnh rồi tải lên Cloud Storage theo đường dẫn truyền vào, trả đường dẫn tải về. | 5, 17, 54 |
| `public void uploadVideo(Uri uri, String path, DataCallback<String> cb)` | Tải video lên Cloud Storage, trả đường dẫn tải về. | 43 đến 45 |
| `public void delete(String url, DataCallback<Void> cb)` | Xóa tệp trên Cloud Storage theo đường dẫn tải về. |  |

**Quan hệ trên sơ đồ lớp**

- `MediaRepository` kế thừa `BaseRepository`. Trang 5. Tầng dữ liệu.

**Dùng tới:** [`DataCallback`](data.md#datacallback)

**Được dùng bởi:** [`ChatViewModel`](viewmodel.md#chatviewmodel), [`DealViewModel`](viewmodel.md#dealviewmodel), [`ProfileViewModel`](viewmodel.md#profileviewmodel), [`ShopViewModel`](viewmodel.md#shopviewmodel), [`TrustViewModel`](viewmodel.md#trustviewmodel)

### ChatRepository

| Mục | Nội dung |
| --- | --- |
| Định danh | `com.nhom3.pubgapp.data.ChatRepository` |
| Loại | lớp (repository) |
| Kế thừa | [`BaseRepository`](data.md#baserepository) |
| Lưu trữ | Không lưu |
| Trách nhiệm | Chat theo thời gian thực và offer một chạm. |
| Chức năng liên quan (STT) | 43 đến 45, 47 đến 49 |

**Phương thức**

| Chữ ký | Mô tả | STT |
| --- | --- | --- |
| `public void getOrCreateConversation(String listingId, String buyerId, DataCallback<Conversation> cb)` | Đọc hoặc tạo `conversations/{listingId_buyerId}`. |  |
| `public void getConversations(String uid, DataCallback<List<Conversation>> cb)` | Cuộc trò chuyện mà người dùng là người mua hoặc người bán, mới nhất trước; ghi vào Room. |  |
| `public void observeMessages(String id, DataCallback<List<ChatMessage>> cb)` | Lắng nghe tin nhắn theo thời gian thực; ghi tin nhắn mới vào Room. |  |
| `public void sendText(String id, String text, DataCallback<Void> cb)` | Ghi tin nhắn TEXT, cập nhật `lastMessage`, gửi thông báo NEW_MESSAGE nếu người nhận bật. | 43 đến 45 |
| `public void sendMedia(String id, Uri uri, MessageType type, DataCallback<Void> cb)` | Tải tệp qua `MediaRepository`, ghi tin nhắn IMAGE hoặc VIDEO. | 43 đến 45 |
| `public void sendOffer(Offer offer, DataCallback<String> cb)` | Kiểm tra `Listing.acceptsOffer`, ghi `Offer` PENDING và một tin nhắn loại OFFER, gửi thông báo NEW_OFFER. | 47 đến 49 |
| `public void respondToOffer(String offerId, boolean accept, DataCallback<String> cb)` | Chấp nhận hoặc từ chối; chấp nhận thì gọi `DealRepository.createFromOffer` và đặt tin RESERVED. | 47 đến 49 |

**Quan hệ trên sơ đồ lớp**

- `ChatRepository` kế thừa `BaseRepository`. Trang 5. Tầng dữ liệu.
- Phụ thuộc: `ChatRepository` dùng `DealRepository` (tạo giao dịch khi chấp nhận offer). Trang 5. Tầng dữ liệu.

**Dùng tới:** [`ChatDao`](data-local.md#chatdao), [`ChatMessage`](model.md#chatmessage), [`Conversation`](model.md#conversation), [`DataCallback`](data.md#datacallback), [`MessageType`](enums.md#messagetype), [`Offer`](model.md#offer)

**Được dùng bởi:** [`AdminComplaintViewModel`](viewmodel.md#admincomplaintviewmodel), [`ChatDao`](data-local.md#chatdao), [`ChatViewModel`](viewmodel.md#chatviewmodel)

### DealRepository

| Mục | Nội dung |
| --- | --- |
| Định danh | `com.nhom3.pubgapp.data.DealRepository` |
| Loại | lớp (repository) |
| Kế thừa | [`BaseRepository`](data.md#baserepository) |
| Lưu trữ | Không lưu |
| Trách nhiệm | Vòng đời giao dịch và ký quỹ; giải ngân khi người mua nhận hàng hoặc khi hết thời hạn đếm ngược; tự hoàn tất sau khi hai bên đánh giá hoặc sau 3 ngày. |
| Chức năng liên quan (STT) | 50 đến 58, 81, 84 |

**Phương thức**

| Chữ ký | Mô tả | STT |
| --- | --- | --- |
| `public void createFromOffer(Offer offer, DataCallback<Deal> cb)` | Tạo `Deal` PENDING_CONFIRM, chụp tiêu đề và ảnh tin, thêm dòng lịch sử đầu tiên, tăng `dealCount` của hai bên. | 50 |
| `public void updateTerms(Deal deal, DataCallback<Void> cb)` | Sửa điều khoản khi còn PENDING_CONFIRM; xóa cả hai cờ xác nhận để hai bên xác nhận lại. | 51 |
| `public void confirm(String dealId, DataCallback<Void> cb)` | Gọi `Deal.confirmBy` cho người đang đăng nhập. | 52 |
| `public void pay(String dealId, DataCallback<Void> cb)` | Tạo `payments/{dealId}` HELD; chỉ người mua, chỉ khi CONFIRMED và `needsPayment`. | 56 |
| `public void markShipped(String dealId, String carrier, String trackingCode, String imageUrl, DataCallback<Void> cb)` | Gọi `Deal.markShipped`, ghi lịch sử, gửi thông báo cho người mua. | 54 |
| `public void markReceived(String dealId, DataCallback<Void> cb)` | Gọi `Deal.markReceived`, giải ngân `Payment` nếu có, gửi thông báo cho người bán. | 53, 84 |
| `public void releaseDueDeals(DataCallback<Integer> cb)` | Tìm giao dịch SHIPPED của người dùng hiện tại đã qua `autoReleaseAt`, chuyển sang RECEIVED và giải ngân. | 84 |
| `public void completeDueDeals(DataCallback<Integer> cb)` | Tìm giao dịch RECEIVED đủ điều kiện hoàn tất, chuyển sang COMPLETED, đặt tin SOLD, tăng `completedDealCount` của hai bên và `soldCount` của người bán. | 53 |
| `public void cancel(String dealId, String reason, DataCallback<Void> cb)` | Hủy có lý do, hoàn `Payment` nếu đang giữ, đưa tin về AVAILABLE. | 55 |
| `public void getMyDeals(String uid, DataCallback<List<Deal>> cb)` | Giao dịch mà người dùng là người mua hoặc người bán, mới cập nhật trước; ghi vào Room. | 57 |
| `public void getPayments(String uid, DataCallback<List<Payment>> cb)` | Khoản ký quỹ mà người dùng trả hoặc nhận. | 58 |
| `public void listAll(DealStatus status, DataCallback<List<Deal>> cb)` | Admin: giao dịch theo trạng thái, rỗng là mọi trạng thái. | 81 |

**Quan hệ trên sơ đồ lớp**

- `DealRepository` kế thừa `BaseRepository`. Trang 5. Tầng dữ liệu.
- Phụ thuộc: `ChatRepository` dùng `DealRepository` (tạo giao dịch khi chấp nhận offer). Trang 5. Tầng dữ liệu.

**Dùng tới:** [`DataCallback`](data.md#datacallback), [`Deal`](model.md#deal), [`DealDao`](data-local.md#dealdao), [`DealStatus`](enums.md#dealstatus), [`Offer`](model.md#offer), [`Payment`](model.md#payment)

**Được dùng bởi:** [`AdminComplaintViewModel`](viewmodel.md#admincomplaintviewmodel), [`AdminDealViewModel`](viewmodel.md#admindealviewmodel), [`AdminUserViewModel`](viewmodel.md#adminuserviewmodel), [`AutoReleaseWorker`](util.md#autoreleaseworker), [`ChatViewModel`](viewmodel.md#chatviewmodel), [`DealDao`](data-local.md#dealdao), [`DealViewModel`](viewmodel.md#dealviewmodel)

### ReviewRepository

| Mục | Nội dung |
| --- | --- |
| Định danh | `com.nhom3.pubgapp.data.ReviewRepository` |
| Loại | lớp (repository) |
| Kế thừa | [`BaseRepository`](data.md#baserepository) |
| Lưu trữ | Không lưu |
| Trách nhiệm | Đánh giá và cập nhật điểm trung bình của người được đánh giá. |
| Chức năng liên quan (STT) | 7, 59, 60 |

**Phương thức**

| Chữ ký | Mô tả | STT |
| --- | --- | --- |
| `public void create(Review review, DataCallback<Void> cb)` | Ghi đánh giá nếu giao dịch đã RECEIVED hoặc COMPLETED và người này chưa đánh giá; gọi `User.applyRating`; hai bên đều đánh giá thì hoàn tất giao dịch. | 7, 59 |
| `public void getReceived(String uid, DataCallback<List<Review>> cb)` | Đánh giá người dùng đã nhận. | 60 |
| `public void getGiven(String uid, DataCallback<List<Review>> cb)` | Đánh giá người dùng đã viết. | 60 |

**Quan hệ trên sơ đồ lớp**

- `ReviewRepository` kế thừa `BaseRepository`. Trang 5. Tầng dữ liệu.

**Dùng tới:** [`DataCallback`](data.md#datacallback), [`Review`](model.md#review)

**Được dùng bởi:** [`AdminUserViewModel`](viewmodel.md#adminuserviewmodel), [`TrustViewModel`](viewmodel.md#trustviewmodel)

### ReportRepository

| Mục | Nội dung |
| --- | --- |
| Định danh | `com.nhom3.pubgapp.data.ReportRepository` |
| Loại | lớp (repository) |
| Kế thừa | [`BaseRepository`](data.md#baserepository) |
| Lưu trữ | Không lưu |
| Trách nhiệm | Báo cáo vi phạm; người dùng xem, hủy và xuất lịch sử. |
| Chức năng liên quan (STT) | 61 đến 63, 75 |

**Phương thức**

| Chữ ký | Mô tả | STT |
| --- | --- | --- |
| `public void create(Report report, DataCallback<Void> cb)` | Ghi báo cáo OPEN; tăng `reportCount` khi đối tượng là người dùng. | 61 |
| `public void cancel(String reportId, DataCallback<Void> cb)` | Người báo cáo hủy báo cáo còn OPEN. | 62 |
| `public void getMyReports(String uid, DataCallback<List<Report>> cb)` | Báo cáo người dùng đã gửi. | 63 |
| `public File exportCsv(List<Report> reports)` | Ghi danh sách báo cáo ra tệp CSV trong bộ nhớ của ứng dụng; chạy ngoài luồng chính. | 63 |
| `public void getByStatus(ReportStatus status, DataCallback<List<Report>> cb)` | Admin: báo cáo theo trạng thái. | 75 |
| `public void countByTarget(String targetId, DataCallback<Integer> cb)` | Số báo cáo OPEN của một đối tượng. | 75 |
| `public void closeByTarget(String targetId, boolean dismissed, String adminId, DataCallback<Void> cb)` | Đóng mọi báo cáo OPEN của một đối tượng: RESOLVED hoặc DISMISSED. | 75 |

**Quan hệ trên sơ đồ lớp**

- `ReportRepository` kế thừa `BaseRepository`. Trang 5. Tầng dữ liệu.

**Dùng tới:** [`DataCallback`](data.md#datacallback), [`Report`](model.md#report), [`ReportStatus`](enums.md#reportstatus)

**Được dùng bởi:** [`AdminContentViewModel`](viewmodel.md#admincontentviewmodel), [`TrustViewModel`](viewmodel.md#trustviewmodel)

### ComplaintRepository

| Mục | Nội dung |
| --- | --- |
| Định danh | `com.nhom3.pubgapp.data.ComplaintRepository` |
| Loại | lớp (repository) |
| Kế thừa | [`BaseRepository`](data.md#baserepository) |
| Lưu trữ | Không lưu |
| Trách nhiệm | Khiếu nại; ghi chú nội bộ nằm ở tài liệu con chỉ admin đọc được. |
| Chức năng liên quan (STT) | 64 đến 66, 87 đến 90 |

**Phương thức**

| Chữ ký | Mô tả | STT |
| --- | --- | --- |
| `public void create(Complaint complaint, DataCallback<Void> cb)` | Ghi khiếu nại OPEN và chuyển giao dịch sang DISPUTED (đếm ngược dừng). | 64 |
| `public void getMyComplaints(String uid, DataCallback<List<Complaint>> cb)` | Khiếu nại người dùng đã gửi. | 65 |
| `public void withdraw(String id, DataCallback<Void> cb)` | Rút đơn; giao dịch về trạng thái trước khi khiếu nại, hạn đếm ngược giữ nguyên. | 66 |
| `public void getByStatus(ComplaintStatus status, Priority priority, DataCallback<List<Complaint>> cb)` | Admin: khiếu nại theo trạng thái và mức ưu tiên. | 87 |
| `public void getDetail(String id, DataCallback<Complaint> cb)` | Admin: một khiếu nại kèm giao dịch; lịch sử chat đọc qua `ChatRepository`. | 88 |
| `public void decide(String id, ComplaintDecision decision, String message, DataCallback<Void> cb)` | Ghi quyết định và áp hệ quả lên giao dịch và khoản ký quỹ theo `trang-thai.md`. | 89 |
| `public void saveAdminNote(String id, String note, DataCallback<Void> cb)` | Ghi `complaints/{id}/adminNote/note`. | 90 |

**Quan hệ trên sơ đồ lớp**

- `ComplaintRepository` kế thừa `BaseRepository`. Trang 5. Tầng dữ liệu.

**Dùng tới:** [`Complaint`](model.md#complaint), [`ComplaintDecision`](enums.md#complaintdecision), [`ComplaintStatus`](enums.md#complaintstatus), [`DataCallback`](data.md#datacallback), [`Priority`](enums.md#priority)

**Được dùng bởi:** [`AdminComplaintViewModel`](viewmodel.md#admincomplaintviewmodel), [`TrustViewModel`](viewmodel.md#trustviewmodel)

### NotificationRepository

| Mục | Nội dung |
| --- | --- |
| Định danh | `com.nhom3.pubgapp.data.NotificationRepository` |
| Loại | lớp (repository) |
| Kế thừa | [`BaseRepository`](data.md#baserepository) |
| Lưu trữ | Không lưu |
| Trách nhiệm | Thông báo cho từng người dùng. |
| Chức năng liên quan (STT) | 67, 91 |

**Phương thức**

| Chữ ký | Mô tả | STT |
| --- | --- | --- |
| `public void getForUser(String uid, DataCallback<List<AppNotification>> cb)` | Thông báo của người dùng, mới nhất trước. | 67, 91 |
| `public void markRead(String id, DataCallback<Void> cb)` | Đánh dấu đã đọc. | 67, 91 |
| `public void create(AppNotification notification, DataCallback<Void> cb)` | Ghi một thông báo, bỏ qua nếu người nhận đã tắt loại này trong `notificationPrefs`. | 91 |

**Quan hệ trên sơ đồ lớp**

- `NotificationRepository` kế thừa `BaseRepository`. Trang 5. Tầng dữ liệu.

**Dùng tới:** [`AppNotification`](model.md#appnotification), [`DataCallback`](data.md#datacallback)

**Được dùng bởi:** [`AdminComplaintViewModel`](viewmodel.md#admincomplaintviewmodel), [`AdminContentViewModel`](viewmodel.md#admincontentviewmodel), [`AdminUserViewModel`](viewmodel.md#adminuserviewmodel), [`AutoReleaseWorker`](util.md#autoreleaseworker), [`ChatViewModel`](viewmodel.md#chatviewmodel), [`DealViewModel`](viewmodel.md#dealviewmodel), [`NotificationViewModel`](viewmodel.md#notificationviewmodel)

### AdminRepository

| Mục | Nội dung |
| --- | --- |
| Định danh | `com.nhom3.pubgapp.data.AdminRepository` |
| Loại | lớp (repository) |
| Kế thừa | [`BaseRepository`](data.md#baserepository) |
| Lưu trữ | Không lưu |
| Trách nhiệm | Thao tác dành riêng cho admin: người dùng, xác minh, kiểm duyệt, từ khóa cấm. |
| Chức năng liên quan (STT) | 68 đến 74, 76 đến 78 |

**Phương thức**

| Chữ ký | Mô tả | STT |
| --- | --- | --- |
| `public void isAdmin(String uid, DataCallback<Boolean> cb)` | True khi tồn tại `admins/{uid}`. |  |
| `public void listUsers(UserStatus status, Timestamp from, Timestamp to, String province, DataCallback<List<User>> cb)` | Người dùng lọc theo trạng thái, khoảng ngày tham gia, khu vực. | 68 |
| `public void lockUser(String id, Timestamp until, DataCallback<Void> cb)` | Đặt TEMP_LOCKED kèm hạn, hoặc PERM_LOCKED khi hạn rỗng. | 70, 71 |
| `public void unlockUser(String id, DataCallback<Void> cb)` | Đặt lại ACTIVE và xóa `lockedUntil`. | 70, 71 |
| `public void warnUser(String id, Warning warning, DataCallback<Void> cb)` | Tăng `warningCount`, ghi `Warning`, áp `Warning.resultingStatus`, gửi thông báo WARNING. | 72 |
| `public void getWarnings(String id, DataCallback<List<Warning>> cb)` | Các lần cảnh báo của một người dùng. | 69 |
| `public void getVerificationRequests(DataCallback<List<SellerVerification>> cb)` | Yêu cầu xác minh đang PENDING. | 73 |
| `public void reviewVerification(String id, boolean approve, DataCallback<Void> cb)` | Duyệt hoặc từ chối, cập nhật `User.verificationStatus`, gửi thông báo VERIFICATION_RESULT. | 73 |
| `public void getActivityLogs(String id, DataCallback<List<ActivityLog>> cb)` | Nhật ký hoạt động của một tài khoản. | 74 |
| `public void hideListing(String id, String reason, DataCallback<Void> cb)` | Đặt tin HIDDEN kèm lý do, gửi thông báo LISTING_MODERATED cho người đăng. | 76 |
| `public void restoreListing(String id, DataCallback<Void> cb)` | Đưa tin HIDDEN về AVAILABLE, gửi thông báo. | 76 |
| `public void removeAllListingsOf(String userId, DataCallback<Void> cb)` | Đặt mọi tin của tài khoản sang REMOVED. | 77 |
| `public void getKeywords(DataCallback<List<BannedKeyword>> cb)` | Danh sách từ khóa cấm. | 78 |
| `public void addKeyword(String word, DataCallback<Void> cb)` | Thêm từ khóa cấm (lưu dạng đã bỏ dấu). | 78 |
| `public void removeKeyword(String id, DataCallback<Void> cb)` | Xóa từ khóa cấm. | 78 |

**Quan hệ trên sơ đồ lớp**

- `AdminRepository` kế thừa `BaseRepository`. Trang 5. Tầng dữ liệu.

**Dùng tới:** [`ActivityLog`](model.md#activitylog), [`BannedKeyword`](model.md#bannedkeyword), [`DataCallback`](data.md#datacallback), [`SellerVerification`](model.md#sellerverification), [`User`](model.md#user), [`UserStatus`](enums.md#userstatus), [`Warning`](model.md#warning)

**Được dùng bởi:** [`AdminContentViewModel`](viewmodel.md#admincontentviewmodel), [`AdminUserViewModel`](viewmodel.md#adminuserviewmodel)
