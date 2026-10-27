# Đặc tả lớp: gói `com.nhom3.pubgapp.data`

Bản chữ của sơ đồ lớp cho gói này: 14 lớp. Mục lục và cách đọc ở [README.md](README.md).

Tầng truy cập dữ liệu. ViewModel chỉ nói chuyện với repository, không gọi Firebase hay Room trực tiếp. Mọi phương thức bất đồng bộ trả kết quả qua `DataCallback<T>` (không dùng lambda, theo luật viết code). Kiểu tham số ghi theo thiết kế; khi code mà cần đổi thì sửa tài liệu này trong cùng Pull Request.

| Lớp | Loại | Trách nhiệm |
| --- | --- | --- |
| [`DataCallback`](#datacallback) | interface | Giao diện gọi lại chung: repository báo kết quả hoặc lỗi cho ViewModel. |
| [`BaseRepository`](#baserepository) | lớp trừu tượng | Lớp cha của mọi repository: giữ kết nối Firebase, ghi nhật ký, xử lý lỗi chung. |
| [`AuthRepository`](#authrepository) | lớp (repository) | Đăng ký, đăng nhập, quên mật khẩu, xác thực email, đổi mật khẩu, xóa thông tin đăng nhập. |
| [`UserRepository`](#userrepository) | lớp (repository) | Hồ sơ, địa chỉ, thông tin định danh, cài đặt thông báo, theo dõi, lưu tin, yêu cầu xác thực tài khoản, vô hiệu hóa tài khoản. |
| [`CategoryRepository`](#categoryrepository) | lớp (repository) | Danh mục và danh mục con; admin thêm, sửa, xóa. |
| [`ListingRepository`](#listingrepository) | lớp (repository) | Tin đăng, tìm kiếm, kênh xem; đọc bộ nhớ đệm trước rồi mới gọi mạng. |
| [`MediaRepository`](#mediarepository) | lớp (repository) | Nén ảnh, lưu dạng Base64 trong Firestore, tạo ảnh thu nhỏ, đọc ảnh kèm bộ nhớ đệm. |
| [`ChatRepository`](#chatrepository) | lớp (repository) | Chat theo thời gian thực và offer một chạm. |
| [`DealRepository`](#dealrepository) | lớp (repository) | Vòng đời giao dịch và ký quỹ; giải ngân khi người mua nhận hàng hoặc khi hết thời hạn đếm ngược; tự hoàn tất sau khi hai bên đánh giá hoặc sau 3 ngày. |
| [`ReviewRepository`](#reviewrepository) | lớp (repository) | Đánh giá và cập nhật điểm trung bình của người được đánh giá. |
| [`ReportRepository`](#reportrepository) | lớp (repository) | Báo cáo vi phạm; người dùng xem, hủy và xuất lịch sử. |
| [`ComplaintRepository`](#complaintrepository) | lớp (repository) | Khiếu nại; ghi chú nội bộ nằm ở tài liệu con chỉ admin đọc được. |
| [`NotificationRepository`](#notificationrepository) | lớp (repository) | Thông báo cho từng người dùng. |
| [`AdminRepository`](#adminrepository) | lớp (repository) | Thao tác dành riêng cho admin: người dùng, xác thực tài khoản, kiểm duyệt, từ khóa cấm, danh sách tài khoản admin và việc cấp hoặc gỡ quyền admin. |

### DataCallback

| Mục | Nội dung |
| --- | --- |
| Định danh | `com.nhom3.pubgapp.data.DataCallback` |
| Loại | interface |
| Lưu trữ | Không lưu |
| Trách nhiệm | Giao diện gọi lại chung: repository báo kết quả hoặc lỗi cho ViewModel. |
| Chức năng liên quan (STT) | hạ tầng dùng chung |

**Thuộc tính**

| Tên | Kiểu | Truy cập | Ý nghĩa |
| --- | --- | --- | --- |
| `NOT_READY` | `String` | public |  |

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
| `protected void logActivity(String action, String targetType, String targetId)` | Ghi một `ActivityLog`. `action` là chuỗi hằng viết hoa có gạch dưới, ví dụ `LISTING_CREATED`, `USER_LOCKED`, `USER_WARNED`, `LISTING_HIDDEN`, `LISTING_REMOVED`, `CATEGORY_CREATED`, `KEYWORD_ADDED`, `ADMIN_GRANTED`, `ADMIN_REVOKED`. `targetType` là chuỗi tự do (`listing`, `user`, `message`, `category`, `keyword`, `admin`, `deal`, `complaint`, `report`), không phải enum, vì nhật ký trỏ tới nhiều loại hơn `ReportTarget`. | 74 |
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
| Chức năng liên quan (STT) | 1 đến 3, 11, 12, 104 |

**Phương thức**

| Chữ ký | Mô tả | STT |
| --- | --- | --- |
| `public void signUp(String email, String password, DataCallback<String> cb)` | Tạo tài khoản email và mật khẩu trên Firebase Authentication, rồi gửi email xác thực. | 1 đến 3 |
| `public void sendVerificationEmail(DataCallback<Void> cb)` | Gửi lại email xác thực. | 1 đến 3 |
| `public void signIn(String email, String password, DataCallback<User> cb)` | Đăng nhập bằng email và mật khẩu, trả `User` kể cả khi tài khoản đang khóa; ViewModel quyết định cho vào hay không. | 1 đến 3 |
| `public void signInWithGoogle(String idToken, DataCallback<User> cb)` | Đăng nhập bằng mã của Google Sign-In. Trả `User`, hoặc `null` khi chưa có tài liệu `users/{uid}` (lần đầu): khi đó ứng dụng mở bước hoàn tất hồ sơ. | 1 đến 3 |
| `public void signInWithGoogleBrowser(Activity activity, DataCallback<User> cb)` | Trước hết mở hộp chọn tài khoản Google của hệ thống qua Credential Manager (`GetSignInWithGoogleOption` với `default_web_client_id`), lấy mã Google rồi đi tiếp như `signInWithGoogle`; máy chưa có tài khoản thì hệ thống mời thêm tài khoản. Chỉ khi Credential Manager lỗi (không phải người dùng hủy) mới mở trang đăng nhập Google trong trình duyệt qua Firebase Authentication (luôn hỏi chọn tài khoản), vì trình duyệt có thể mất trạng thái chuyển hướng ("missing initial state"). Kết quả giống `signInWithGoogle`: `User`, hoặc `null` khi chưa có `users/{uid}`. Đóng trang giữa chừng thì trả lỗi "Đăng nhập Google chưa hoàn tất". | 1 đến 3 |
| `public void sendPasswordReset(String email, DataCallback<Void> cb)` | Gửi email đặt lại mật khẩu. | 1 đến 3 |
| `public void changePassword(String current, String next, DataCallback<Void> cb)` | Xác thực lại bằng mật khẩu hiện tại rồi đổi sang mật khẩu mới. | 11 |
| `public String getCurrentUserId()` | Mã người dùng của phiên Firebase hiện tại, hoặc rỗng khi chưa đăng nhập; `AuthViewModel.restoreSession` cần nó. |  |
| `public boolean isGoogleAccount()` | True khi phiên hiện tại đăng nhập bằng Google, nên không có mật khẩu để đổi. |  |
| `public void deleteLogin(DataCallback<Void> cb)` | Xóa tài khoản đăng nhập trên Firebase Authentication; phải gọi ngay sau khi xác thực lại. | 12 |
| `public void reauthenticate(String password, DataCallback<Void> cb)` | Xác thực lại bằng mật khẩu hiện tại; cần trước khi đổi mật khẩu hoặc xóa đăng nhập. |  |
| `public void reauthenticateWithGoogle(String idToken, DataCallback<Void> cb)` | Xác thực lại bằng mã Google mới cho tài khoản đăng nhập bằng Google. |  |
| `public void reloadUser(DataCallback<Boolean> cb)` | Tải lại thông tin đăng nhập để cờ email đã xác thực là mới nhất. |  |
| `public void signOut()` | Đăng xuất; người gọi phải xóa bộ nhớ đệm Room và `SessionManager`. | 104 |
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
| Trách nhiệm | Hồ sơ, địa chỉ, thông tin định danh, cài đặt thông báo, theo dõi, lưu tin, yêu cầu xác thực tài khoản, vô hiệu hóa tài khoản. |
| Chức năng liên quan (STT) | 4, 6, 8 đến 10, 12, 30, 39 đến 42, 46, 73, 103 |

**Phương thức**

| Chữ ký | Mô tả | STT |
| --- | --- | --- |
| `public void createProfile(User user, UserIdentity identity, DataCallback<Void> cb)` | Tạo `users/{uid}` (ACTIVE, `verificationStatus` NONE, `ratingSum` và `ratingCount` bằng 0, `termsAcceptedAt`, `searchKeywords` từ `User.buildSearchKeywords`) và `users/{uid}/private/identity` trong một lô ghi. |  |
| `public void getProfile(String uid, DataCallback<User> cb)` | Đọc `users/{uid}`. |  |
| `public void updateProfile(User user, DataCallback<Void> cb)` | Ghi các trường hồ sơ mà chính chủ được sửa (tên, điện thoại, khu vực, giới thiệu, ảnh, danh mục quan tâm); gọi `User.buildSearchKeywords` lại khi `displayName` đổi. | 4 |
| `public void searchUsers(String keyword, DataCallback<List<User>> cb)` | Tìm người dùng theo tên: `searchKeywords array-contains` từ dài nhất trong ô tìm kiếm (đã bỏ dấu), `status == ACTIVE`, sắp `ratingCount` giảm, tối đa 20 người, không phân trang. Chỉ khớp nguyên từ. | 103 |
| `public void getStats(String uid, DataCallback<ProfileStats> cb)` | Đếm bằng truy vấn tổng hợp `count()`: giao dịch đã bán, người theo dõi, giao dịch tham gia, giao dịch hoàn tất. Trả `ProfileStats`. | 8 |
| `public void clearExpiredLock(String uid, DataCallback<Void> cb)` | Chủ tài khoản ghi lại ACTIVE khi khóa tạm đã hết hạn; Security Rules chỉ cho phép khi giờ máy chủ đã qua `lockedUntil`. |  |
| `public void saveAddress(String uid, UserAddress address, DataCallback<Void> cb)` | Tạo hoặc sửa một địa chỉ; nếu đặt mặc định thì bỏ cờ mặc định của địa chỉ khác. | 6 |
| `public void deleteAddress(String uid, String id, DataCallback<Void> cb)` | Xóa một địa chỉ. | 6 |
| `public void getAddresses(String uid, DataCallback<List<UserAddress>> cb)` | Đọc mọi địa chỉ của người dùng, địa chỉ mặc định trước. |  |
| `public void follow(String uid, String otherId, DataCallback<Void> cb)` | Tạo `follows/{uid_otherId}`. | 39 |
| `public void unfollow(String uid, String otherId, DataCallback<Void> cb)` | Xóa `follows/{uid_otherId}`. | 39 |
| `public void getFollowingIds(String uid, DataCallback<List<String>> cb)` | Mã những người mà người dùng đang theo dõi. | 30 |
| `public void getFollowerCount(String uid, DataCallback<Integer> cb)` | Đếm số người đang theo dõi một người dùng bằng truy vấn `count()` trên `follows` với `followedId == uid`. Tách khỏi `getStats` vì `follows` ai đăng nhập cũng đọc được, còn `deals` thì không, nên số này hiện được cả trên trang cá nhân của người khác. |  |
| `public void saveListing(String uid, String listingId, DataCallback<Void> cb)` | Tạo `users/{uid}/saved/{listingId}`. | 40 đến 42 |
| `public void unsaveListing(String uid, String listingId, DataCallback<Void> cb)` | Xóa `users/{uid}/saved/{listingId}`. | 40 đến 42 |
| `public void getSavedIds(String uid, DataCallback<List<String>> cb)` | Mã các tin đã lưu, mới lưu trước. | 40 đến 42 |
| `public void requestVerification(String uid, SellerVerification verification, DataCallback<Void> cb)` | Tạo hoặc gửi lại `sellerVerifications/{uid}` với trạng thái PENDING, kèm số CCCD và hai mã ảnh; lần gửi lại tăng `resubmitCount` và bị chặn khi `canResubmit()` false. | 73 |
| `public void getVerification(String uid, DataCallback<SellerVerification> cb)` | Đọc `sellerVerifications/{uid}` của chính người dùng, để D01 hiện trạng thái và lý do từ chối. |  |
| `public void saveIdentity(String uid, UserIdentity identity, DataCallback<Void> cb)` | Ghi `users/{uid}/private/identity`. | 9 |
| `public void getIdentity(String uid, DataCallback<UserIdentity> cb)` | Đọc `users/{uid}/private/identity`; chỉ chính chủ và admin đọc được. | 9 |
| `public void updateNotificationPrefs(String uid, Map<String,Boolean> prefs, DataCallback<Void> cb)` | Ghi `notificationPrefs`. | 10 |
| `public void touchLastActive(String uid)` | Ghi `lastActiveAt`; gọi khi mở ứng dụng và khi vào chat. | 46 |
| `public void deactivate(String uid, DataCallback<Void> cb)` | Từ chối nếu còn giao dịch chưa kết thúc; nếu không, đặt `status` DELETED và ẩn mọi tin của người này. | 12 |

**Quan hệ trên sơ đồ lớp**

- `UserRepository` kế thừa `BaseRepository`. Trang 5. Tầng dữ liệu.

**Dùng tới:** [`DataCallback`](data.md#datacallback), [`ProfileStats`](model.md#profilestats), [`SellerVerification`](model.md#sellerverification), [`User`](model.md#user), [`UserAddress`](model.md#useraddress), [`UserIdentity`](model.md#useridentity)

**Được dùng bởi:** [`AdminContentViewModel`](viewmodel.md#admincontentviewmodel), [`AdminDealViewModel`](viewmodel.md#admindealviewmodel), [`AdminUserViewModel`](viewmodel.md#adminuserviewmodel), [`AuthViewModel`](viewmodel.md#authviewmodel), [`ChatViewModel`](viewmodel.md#chatviewmodel), [`DealViewModel`](viewmodel.md#dealviewmodel), [`FeedViewModel`](viewmodel.md#feedviewmodel), [`ListingDetailViewModel`](viewmodel.md#listingdetailviewmodel), [`ProfileViewModel`](viewmodel.md#profileviewmodel), [`SearchViewModel`](viewmodel.md#searchviewmodel)

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
| `public void getAllForAdmin(DataCallback<List<Category>> cb)` | Mọi danh mục kể cả đã ẩn (`isActive` false), cho màn quản lý danh mục. | 79 |
| `public void getChildren(String parentId, DataCallback<List<Category>> cb)` | Danh mục con của một danh mục. | 24 |
| `public void add(Category category, DataCallback<String> cb)` | Admin thêm danh mục. | 79 |
| `public void update(Category category, DataCallback<Void> cb)` | Admin sửa tên, danh mục cha, thuộc tính tình trạng hoặc ẩn danh mục. | 79 |
| `public void delete(String id, DataCallback<Void> cb)` | Admin xóa danh mục; từ chối nếu còn danh mục con hoặc còn tin (đếm bằng `count()`), khi đó chỉ được ẩn (`isActive` false). | 79 |

**Quan hệ trên sơ đồ lớp**

- `CategoryRepository` kế thừa `BaseRepository`. Trang 5. Tầng dữ liệu.

**Dùng tới:** [`Category`](model.md#category), [`DataCallback`](data.md#datacallback)

**Được dùng bởi:** [`AdminContentViewModel`](viewmodel.md#admincontentviewmodel), [`AuthViewModel`](viewmodel.md#authviewmodel), [`FeedViewModel`](viewmodel.md#feedviewmodel), [`SearchViewModel`](viewmodel.md#searchviewmodel), [`ShopViewModel`](viewmodel.md#shopviewmodel)

### ListingRepository

| Mục | Nội dung |
| --- | --- |
| Định danh | `com.nhom3.pubgapp.data.ListingRepository` |
| Loại | lớp (repository) |
| Kế thừa | [`BaseRepository`](data.md#baserepository) |
| Lưu trữ | Không lưu |
| Trách nhiệm | Tin đăng, tìm kiếm, kênh xem; đọc bộ nhớ đệm trước rồi mới gọi mạng. |
| Chức năng liên quan (STT) | 13 đến 15, 22, 23, 29, 36, 38, 78 |

**Phương thức**

| Chữ ký | Mô tả | STT |
| --- | --- | --- |
| `public void create(Listing listing, DataCallback<String> cb)` | Kiểm tra `User.canSell`, gắn cờ nếu chứa từ khóa cấm, tạo `searchKeywords`, ghi tin AVAILABLE. Ảnh đã được ViewModel lưu trước qua `MediaRepository`; tin chỉ nhận mã ảnh và ảnh bìa thu nhỏ. | 13 |
| `public void saveDraft(Listing listing, DataCallback<String> cb)` | Ghi tin với trạng thái DRAFT; không hiện cho người khác. | 22 |
| `public void publish(Listing listing, DataCallback<String> cb)` | Đăng bản nháp: chạy đúng các bước của `create` trên tin DRAFT rồi chuyển sang AVAILABLE. |  |
| `public void update(Listing listing, DataCallback<Void> cb)` | Người đăng sửa tin; gắn cờ lại nếu chứa từ khóa cấm, tạo lại `searchKeywords`. | 13 |
| `public void setHidden(String id, boolean hidden, DataCallback<Void> cb)` | Người bán tự ẩn (HIDDEN, lý do rỗng) hoặc hiện lại (chỉ khi lý do rỗng). Ẩn thì từ chối mọi offer PENDING của tin trong cùng lô ghi. | 15 |
| `public void setStatus(String id, ListingStatus from, ListingStatus to, DataCallback<Void> cb)` | Đổi trạng thái theo cặp người bán được phép (AVAILABLE sang SOLD khi bán ngoài ứng dụng, SOLD sang AVAILABLE khi chưa có giao dịch COMPLETED); từ chối cặp khác. Rời AVAILABLE thì từ chối offer PENDING cùng lô. |  |
| `public void delete(String id, DataCallback<Void> cb)` | Chỉ xóa hẳn tin DRAFT. Tin khác muốn bỏ thì ẩn (`setHidden`). | 13 |
| `public void getById(String id, DataCallback<Listing> cb)` | Đọc một tin; offline thì đọc `listing_cache`. |  |
| `public void getBySeller(String sellerId, DataCallback<List<Listing>> cb)` | Tin của một người bán, mới trước. Người xem khác thêm điều kiện `status in [AVAILABLE, RESERVED, SOLD]`; chính chủ thấy mọi trạng thái. | 14 |
| `public void getFeed(SearchFilter filter, Timestamp after, DataCallback<List<Listing>> cb)` | Tin AVAILABLE, 20 tin một trang, con trỏ là `createdAt` của tin cuối trang trước (`after`, rỗng ở trang đầu). Lọc phía máy chủ tối đa một điều kiện bằng (danh mục hoặc tỉnh); bộ lọc còn lại lọc trên máy, ViewModel tải tiếp tới khi đủ 20 hoặc hết. Có mạng thì ghi trang mới vào Room. | 29 |
| `public void search(SearchFilter filter, Timestamp after, DataCallback<List<Listing>> cb)` | Truy vấn `searchKeywords array-contains` từ dài nhất trong ô tìm kiếm (đã bỏ dấu), `status == AVAILABLE`, con trỏ như `getFeed`; phần còn lại lọc trên máy. Chỉ khớp nguyên từ. | 23 |
| `public void getSimilar(Listing listing, DataCallback<List<Listing>> cb)` | Tin AVAILABLE cùng danh mục để `RecommendationEngine` chấm điểm. | 36 |
| `public void recordView(Listing listing)` | Ghi `RecentViewEntity` (thay nếu đã có), rồi cắt còn 50 dòng. | 38 |
| `public void recordSearch(String keyword)` | Ghi `RecentSearchEntity` (từ khóa là khóa chính nên không trùng), rồi cắt còn 20 dòng. | 38 |
| `public void getBannedKeywords(DataCallback<List<BannedKeyword>> cb)` | Đọc danh sách từ khóa cấm để `KeywordFilter` gắn cờ khi người bán soạn tin (mọi người đã đăng nhập đọc được danh sách này). | 78 |
| `public void getRecentViews(int limit, DataCallback<List<String>> cb)` | Danh sách mã danh mục (categoryId) của các lần xem gần nhất trên máy, giới hạn số dòng. |  |
| `public void getRecentSearches(int limit, DataCallback<List<String>> cb)` | Các từ khóa đã tìm gần nhất trên máy, giới hạn số dòng. |  |

**Quan hệ trên sơ đồ lớp**

- `ListingRepository` kế thừa `BaseRepository`. Trang 5. Tầng dữ liệu.

**Dùng tới:** [`BannedKeyword`](model.md#bannedkeyword), [`DataCallback`](data.md#datacallback), [`HistoryDao`](data-local.md#historydao), [`Listing`](model.md#listing), [`ListingDao`](data-local.md#listingdao), [`ListingStatus`](enums.md#listingstatus), [`SearchFilter`](model.md#searchfilter)

**Được dùng bởi:** [`AdminContentViewModel`](viewmodel.md#admincontentviewmodel), [`AdminUserViewModel`](viewmodel.md#adminuserviewmodel), [`ChatViewModel`](viewmodel.md#chatviewmodel), [`FeedViewModel`](viewmodel.md#feedviewmodel), [`HistoryDao`](data-local.md#historydao), [`ListingDao`](data-local.md#listingdao), [`ListingDetailViewModel`](viewmodel.md#listingdetailviewmodel), [`ProfileViewModel`](viewmodel.md#profileviewmodel), [`SearchViewModel`](viewmodel.md#searchviewmodel), [`ShopViewModel`](viewmodel.md#shopviewmodel)

### MediaRepository

| Mục | Nội dung |
| --- | --- |
| Định danh | `com.nhom3.pubgapp.data.MediaRepository` |
| Loại | lớp (repository) |
| Kế thừa | [`BaseRepository`](data.md#baserepository) |
| Lưu trữ | Không lưu |
| Trách nhiệm | Nén ảnh, lưu dạng Base64 trong Firestore, tạo ảnh thu nhỏ, đọc ảnh kèm bộ nhớ đệm. |
| Chức năng liên quan (STT) | 5, 17, 43, 44, 54 |

**Phương thức**

| Chữ ký | Mô tả | STT |
| --- | --- | --- |
| `public void saveImage(Uri uri, ImagePurpose purpose, DataCallback<String> cb)` | Nén ảnh (cạnh dài 1024 px, JPEG, tối đa 300 KB), mã hóa Base64, ghi `images/{id}` với mục đích truyền vào; trả mã ảnh. | 17, 43, 44, 54 |
| `public void makeThumb(Uri uri, DataCallback<String> cb)` | Tạo ảnh thu nhỏ (320 px cho tin, 256 px cho ảnh đại diện; tối đa 20 KB) dạng Base64; không ghi lên mạng. | 5, 17 |
| `public void getImage(String id, DataCallback<byte[]> cb)` | Đọc `images/{id}` và trả mảng byte; giữ bộ nhớ đệm LRU trong RAM để không đọc lại. | 17 |
| `public void delete(String id, DataCallback<Void> cb)` | Xóa `images/{id}`; chỉ người tải lên xóa được. |  |

**Quan hệ trên sơ đồ lớp**

- `MediaRepository` kế thừa `BaseRepository`. Trang 5. Tầng dữ liệu.

**Dùng tới:** [`DataCallback`](data.md#datacallback), [`ImagePurpose`](enums.md#imagepurpose)

**Được dùng bởi:** [`AdminContentViewModel`](viewmodel.md#admincontentviewmodel), [`AdminUserViewModel`](viewmodel.md#adminuserviewmodel), [`ChatViewModel`](viewmodel.md#chatviewmodel), [`DealViewModel`](viewmodel.md#dealviewmodel), [`ListingDetailViewModel`](viewmodel.md#listingdetailviewmodel), [`ProfileViewModel`](viewmodel.md#profileviewmodel), [`ShopViewModel`](viewmodel.md#shopviewmodel), [`TrustViewModel`](viewmodel.md#trustviewmodel)

### ChatRepository

| Mục | Nội dung |
| --- | --- |
| Định danh | `com.nhom3.pubgapp.data.ChatRepository` |
| Loại | lớp (repository) |
| Kế thừa | [`BaseRepository`](data.md#baserepository) |
| Lưu trữ | Không lưu |
| Trách nhiệm | Chat theo thời gian thực và offer một chạm. |
| Chức năng liên quan (STT) | 43, 44, 47 đến 50 |

**Phương thức**

| Chữ ký | Mô tả | STT |
| --- | --- | --- |
| `public void getOrCreateConversation(String listingId, String buyerId, DataCallback<Conversation> cb)` | Đọc hoặc tạo `conversations/{listingId_buyerId}`. |  |
| `public void getConversation(String id, DataCallback<Conversation> cb)` | Đọc một cuộc trò chuyện theo mã, cho trường hợp mở C02 từ thông báo hoặc từ danh sách mà chưa có đối tượng trong tay. |  |
| `public void getConversations(String uid, DataCallback<List<Conversation>> cb)` | Cuộc trò chuyện mà người dùng là người mua hoặc người bán, mới nhất trước; ghi vào Room. |  |
| `public ListenerRegistration observeMessages(String id, DataCallback<List<ChatMessage>> cb)` | Lắng nghe 50 tin nhắn mới nhất theo thời gian thực, trả `ListenerRegistration`; ViewModel gỡ ở `onCleared`. Ghi tin mới vào Room. |  |
| `public void loadOlder(String id, Timestamp before, DataCallback<List<ChatMessage>> cb)` | Tải thêm 50 tin nhắn cũ hơn mốc truyền vào. |  |
| `public void getMessagesOnce(String id, DataCallback<List<ChatMessage>> cb)` | Đọc một lần toàn bộ tin nhắn của một cuộc trò chuyện (admin xem khi xử lý khiếu nại). |  |
| `public void sendText(String id, String text, DataCallback<Void> cb)` | Ghi tin nhắn TEXT, cập nhật `lastMessage`, ghi thông báo NEW_MESSAGE cho bên kia. | 43, 44 |
| `public void sendImage(String id, Uri uri, DataCallback<Void> cb)` | Lưu ảnh qua `MediaRepository`, ghi tin nhắn IMAGE với mã ảnh, ghi thông báo NEW_MESSAGE. | 43, 44 |
| `public void sendOffer(Offer offer, DataCallback<String> cb)` | Kiểm tra `Listing.acceptsOffer`, ghi `Offer` PENDING và một tin nhắn loại OFFER, ghi thông báo NEW_OFFER. Security Rules từ chối nếu tin không còn AVAILABLE. | 47 đến 49 |
| `public void respondToOffer(String offerId, boolean accept, DataCallback<String> cb)` | Trong một lô ghi: offer ACCEPTED hoặc REJECTED; nếu chấp nhận thì các offer PENDING khác của tin sang REJECTED, giao dịch dựng bằng `DealRepository.buildFromOffer`, tin sang RESERVED và ghi `reservedDealId`. Ghi thông báo OFFER_RESULT. Tin không còn AVAILABLE thì từ chối offer và báo lỗi. | 47 đến 50 |
| `public ListenerRegistration observeOffers(String conversationId, DataCallback<Void> cb)` | Lắng nghe các `Offer` của một cuộc trò chuyện theo thời gian thực, để thẻ đề nghị trong chat đổi theo khi người bán chấp nhận hoặc từ chối. Nhớ gỡ `ListenerRegistration` khi màn hình đóng. |  |

**Quan hệ trên sơ đồ lớp**

- `ChatRepository` kế thừa `BaseRepository`. Trang 5. Tầng dữ liệu.
- Phụ thuộc: `ChatRepository` dùng `DealRepository` (tạo giao dịch khi chấp nhận offer). Trang 5. Tầng dữ liệu.

**Dùng tới:** [`ChatDao`](data-local.md#chatdao), [`ChatMessage`](model.md#chatmessage), [`Conversation`](model.md#conversation), [`DataCallback`](data.md#datacallback), [`Offer`](model.md#offer)

**Được dùng bởi:** [`AdminComplaintViewModel`](viewmodel.md#admincomplaintviewmodel), [`AdminContentViewModel`](viewmodel.md#admincontentviewmodel), [`ChatDao`](data-local.md#chatdao), [`ChatViewModel`](viewmodel.md#chatviewmodel)

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
| `public Deal buildFromOffer(Offer offer, Listing listing)` | Dựng một `Deal` từ đề nghị đã được chấp nhận, chụp `itemTitle` và `itemThumb` từ `Listing` truyền vào; chưa ghi lên Firestore. | 50 |
| `public void getById(String dealId, DataCallback<Deal> cb)` | Đọc một giao dịch. |  |
| `public void getPayment(String dealId, DataCallback<Payment> cb)` | Đọc `payments/{dealId}`; rỗng khi chưa thanh toán. |  |
| `public void updateTerms(Deal deal, DataCallback<Void> cb)` | Sửa điều khoản khi còn PENDING_CONFIRM; xóa cả hai cờ xác nhận để hai bên xác nhận lại. | 51 |
| `public void confirm(String dealId, DataCallback<Void> cb)` | Gọi `Deal.confirmBy`. Từ chối nếu chưa chọn phương thức giao nhận, hoặc giao qua vận chuyển mà chưa có địa chỉ. | 52 |
| `public void pay(String dealId, PaymentMethod method, DataCallback<Void> cb)` | Tạo `payments/{dealId}` HELD; chỉ người mua, chỉ khi CONFIRMED và `needsPayment`. | 56 |
| `public void markShipped(String dealId, String carrier, String trackingCode, String imageId, DataCallback<Void> cb)` | Kiểm tra `Payment` đang HELD nếu `needsPayment()`, gọi `Deal.markShipped`, ghi `shippedAt` giờ máy chủ và `autoReleaseAt` sau 7 ngày, ghi lịch sử, thông báo DEAL_UPDATE cho người mua. | 54 |
| `public void markReceived(String dealId, String imageId, DataCallback<Void> cb)` | Nhận mã ảnh đã được ViewModel lưu trước qua `MediaRepository` (giống `markShipped`); kiểm tra `Payment` đang HELD nếu `needsPayment()`, gọi `Deal.markReceived`, `Payment` sang RELEASED trong cùng lô ghi, thông báo PAYMENT_UPDATE cho người bán. | 53, 84 |
| `public void completeDeal(String dealId, DataCallback<Void> cb)` | Nơi duy nhất chuyển RECEIVED sang COMPLETED: giao dịch COMPLETED, tin SOLD, thông báo DEAL_UPDATE cho hai bên. Gọi từ `ReviewRepository.create` và `completeDueDeals`. |  |
| `public void releaseDueDeals(DataCallback<Integer> cb)` | Tìm giao dịch SHIPPED của người dùng hiện tại (ở cả hai vai trò) đã qua `autoReleaseAt`, chuyển sang RECEIVED và giải ngân. | 84 |
| `public void completeDueDeals(DataCallback<Integer> cb)` | Tìm giao dịch RECEIVED đã quá 3 ngày từ `receivedAt` và gọi `completeDeal`. | 53 |
| `public void expirePendingDeals(DataCallback<Integer> cb)` | Giao dịch PENDING_CONFIRM quá 3 ngày (`Deal.isPendingExpired`) sang CANCELLED, người đổi là `system`, tin về AVAILABLE. |  |
| `public void cancel(String dealId, String reason, DataCallback<Void> cb)` | Hủy có lý do, hoàn `Payment` nếu đang giữ, đưa tin về AVAILABLE (bỏ qua nếu tin không còn). | 55 |
| `public void getMyDeals(String uid, DataCallback<List<Deal>> cb)` | Giao dịch mà người dùng là người mua hoặc người bán, mới cập nhật trước; ghi vào Room. | 57 |
| `public void getPayments(String uid, DataCallback<List<Payment>> cb)` | Khoản ký quỹ mà người dùng trả hoặc nhận. | 58 |
| `public void listAll(DealStatus status, DataCallback<List<Deal>> cb)` | Admin: giao dịch theo trạng thái, rỗng là mọi trạng thái. | 81 |

**Quan hệ trên sơ đồ lớp**

- `DealRepository` kế thừa `BaseRepository`. Trang 5. Tầng dữ liệu.
- Phụ thuộc: `ChatRepository` dùng `DealRepository` (tạo giao dịch khi chấp nhận offer). Trang 5. Tầng dữ liệu.

**Dùng tới:** [`DataCallback`](data.md#datacallback), [`Deal`](model.md#deal), [`DealDao`](data-local.md#dealdao), [`DealStatus`](enums.md#dealstatus), [`Listing`](model.md#listing), [`Offer`](model.md#offer), [`Payment`](model.md#payment), [`PaymentMethod`](enums.md#paymentmethod)

**Được dùng bởi:** [`AdminComplaintViewModel`](viewmodel.md#admincomplaintviewmodel), [`AdminDealViewModel`](viewmodel.md#admindealviewmodel), [`AdminUserViewModel`](viewmodel.md#adminuserviewmodel), [`AutoReleaseWorker`](util.md#autoreleaseworker), [`DealDao`](data-local.md#dealdao), [`DealViewModel`](viewmodel.md#dealviewmodel)

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
| `public void create(Review review, DataCallback<Void> cb)` | Ghi `reviews/{dealId}_{reviewerId}` (mỗi bên một lần) khi giao dịch RECEIVED hoặc COMPLETED; tăng `ratingSum`, `ratingCount` của người được đánh giá cùng lô; thông báo NEW_REVIEW. Hai bên đều đã đánh giá thì gọi `DealRepository.completeDeal`. | 7, 59 |
| `public void getByDeal(String dealId, DataCallback<List<Review>> cb)` | Các đánh giá của một giao dịch (0 đến 2), để biết mình đã đánh giá chưa. |  |
| `public void getReceived(String uid, DataCallback<List<Review>> cb)` | Đánh giá người dùng đã nhận. | 60 |
| `public void getGiven(String uid, DataCallback<List<Review>> cb)` | Đánh giá người dùng đã viết. | 60 |

**Quan hệ trên sơ đồ lớp**

- `ReviewRepository` kế thừa `BaseRepository`. Trang 5. Tầng dữ liệu.

**Dùng tới:** [`DataCallback`](data.md#datacallback), [`Review`](model.md#review)

**Được dùng bởi:** [`AdminUserViewModel`](viewmodel.md#adminuserviewmodel), [`DealViewModel`](viewmodel.md#dealviewmodel), [`TrustViewModel`](viewmodel.md#trustviewmodel)

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
| `public void create(Report report, DataCallback<Void> cb)` | Ghi báo cáo OPEN. | 61 |
| `public void cancel(String reportId, DataCallback<Void> cb)` | Người báo cáo hủy báo cáo còn OPEN. | 62 |
| `public void getMyReports(String uid, DataCallback<List<Report>> cb)` | Báo cáo người dùng đã gửi. | 63 |
| `public File exportCsv(List<Report> reports)` | Ghi danh sách báo cáo ra tệp CSV trong bộ nhớ của ứng dụng; chạy ngoài luồng chính. | 63 |
| `public void getByStatus(ReportStatus status, ReportTarget targetType, DataCallback<List<Report>> cb)` | Admin: báo cáo theo trạng thái. | 75 |
| `public void countByTarget(String targetId, DataCallback<Integer> cb)` | Số báo cáo OPEN của một đối tượng. | 75 |
| `public void closeByTarget(String targetId, boolean dismissed, String adminId, DataCallback<Void> cb)` | Đóng mọi báo cáo OPEN của một đối tượng: RESOLVED hoặc DISMISSED. | 75 |

**Quan hệ trên sơ đồ lớp**

- `ReportRepository` kế thừa `BaseRepository`. Trang 5. Tầng dữ liệu.

**Dùng tới:** [`DataCallback`](data.md#datacallback), [`Report`](model.md#report), [`ReportStatus`](enums.md#reportstatus), [`ReportTarget`](enums.md#reporttarget)

**Được dùng bởi:** [`AdminContentViewModel`](viewmodel.md#admincontentviewmodel), [`AdminUserViewModel`](viewmodel.md#adminuserviewmodel), [`TrustViewModel`](viewmodel.md#trustviewmodel)

### ComplaintRepository

| Mục | Nội dung |
| --- | --- |
| Định danh | `com.nhom3.pubgapp.data.ComplaintRepository` |
| Loại | lớp (repository) |
| Kế thừa | [`BaseRepository`](data.md#baserepository) |
| Lưu trữ | Không lưu |
| Trách nhiệm | Khiếu nại; ghi chú nội bộ nằm ở tài liệu con chỉ admin đọc được. |
| Chức năng liên quan (STT) | 64 đến 66, 87 đến 91 |

**Phương thức**

| Chữ ký | Mô tả | STT |
| --- | --- | --- |
| `public void create(Complaint complaint, DataCallback<Void> cb)` | Ghi `complaints/{dealId}` (mỗi giao dịch tối đa một khiếu nại) khi giao dịch SHIPPED và chưa qua `autoReleaseAt`, hoặc CONFIRMED khi gặp trực tiếp. Giao dịch sang DISPUTED; thông báo DEAL_UPDATE cho bên kia. | 64 |
| `public void getMyComplaints(String uid, DataCallback<List<Complaint>> cb)` | Khiếu nại người dùng đã gửi. | 65 |
| `public void withdraw(String id, DataCallback<Void> cb)` | Rút đơn; giao dịch về `Deal.statusBeforeDispute()`, hạn đếm ngược giữ nguyên. Không mở lại được. | 66 |
| `public void getByStatus(ComplaintStatus status, Priority priority, DataCallback<List<Complaint>> cb)` | Admin: khiếu nại theo trạng thái và mức ưu tiên. | 87 |
| `public void getDetail(String id, DataCallback<Complaint> cb)` | Admin: một khiếu nại kèm giao dịch; lịch sử chat đọc qua `ChatRepository`. | 88 |
| `public void decide(String id, ComplaintDecision decision, String message, DataCallback<Void> cb)` | Ghi quyết định, áp hệ quả lên giao dịch và khoản ký quỹ theo `trang-thai.md` trong một lô ghi, ghi thông báo COMPLAINT_RESULT cho hai bên. | 89, 91 |
| `public void saveAdminNote(String id, String note, DataCallback<Void> cb)` | Ghi `complaints/{id}/adminNote/note`. | 90 |
| `public void addEvidence(String id, List<String> imageIds, DataCallback<Void> cb)` | Người khiếu nại thêm ảnh khi đơn NEEDS_EVIDENCE; đơn về OPEN. | 64 |
| `public void setPriority(String id, Priority priority, DataCallback<Void> cb)` | Admin đặt mức ưu tiên. |  |

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
| `public void create(AppNotification notification, WriteBatch batch, DataCallback<Void> cb)` | Ghi một thông báo cho người nhận, bỏ qua nếu người nhận đã tắt loại này. Chỉ repository gọi, ngay trong thao tác làm đổi trạng thái; ViewModel không gọi. Tham số `batch`: khác null thì phương thức **chỉ thêm lệnh ghi vào lô đó rồi gọi lại ngay**, bên gọi mới commit, nên thông báo và việc đổi trạng thái cùng thành công hoặc cùng thất bại (`trang-thai.md` mục 9); bằng null thì phương thức tự ghi và tự commit. Gửi cho nhiều người thì gọi nhiều lần vào cùng một lô rồi commit một lần. | 91 |

**Quan hệ trên sơ đồ lớp**

- `NotificationRepository` kế thừa `BaseRepository`. Trang 5. Tầng dữ liệu.

**Dùng tới:** [`AppNotification`](model.md#appnotification), [`DataCallback`](data.md#datacallback)

**Được dùng bởi:** [`NotificationViewModel`](viewmodel.md#notificationviewmodel)

### AdminRepository

| Mục | Nội dung |
| --- | --- |
| Định danh | `com.nhom3.pubgapp.data.AdminRepository` |
| Loại | lớp (repository) |
| Kế thừa | [`BaseRepository`](data.md#baserepository) |
| Lưu trữ | Không lưu |
| Trách nhiệm | Thao tác dành riêng cho admin: người dùng, xác thực tài khoản, kiểm duyệt, từ khóa cấm, danh sách tài khoản admin và việc cấp hoặc gỡ quyền admin. |
| Chức năng liên quan (STT) | 68 đến 74, 76 đến 78, 105, 106 |

**Phương thức**

| Chữ ký | Mô tả | STT |
| --- | --- | --- |
| `public void isAdmin(String uid, DataCallback<Boolean> cb)` | True khi tồn tại `admins/{uid}`. | 106 |
| `public void listUsers(UserStatus status, Timestamp from, Timestamp to, String province, String keyword, DataCallback<List<User>> cb)` | Người dùng lọc theo trạng thái, khoảng ngày tham gia, khu vực, và từ khóa tên hoặc email. | 68 |
| `public void lockUser(String id, Timestamp until, String reason, DataCallback<Void> cb)` | Đặt TEMP_LOCKED kèm hạn, hoặc PERM_LOCKED khi hạn rỗng; ghi `lockReason`; thông báo WARNING. | 70, 71 |
| `public void unlockUser(String id, DataCallback<Void> cb)` | Đặt lại ACTIVE, xóa `lockedUntil` và `lockReason`; không đổi `warningCount`. | 70, 71 |
| `public void warnUser(String id, Warning warning, Timestamp until, DataCallback<Void> cb)` | Tăng `warningCount`, ghi `Warning`, áp `Warning.resultingStatus` (bậc 2 dùng hạn `until` admin chọn), ghi `lockReason`, thông báo WARNING. | 72 |
| `public void getWarnings(String id, DataCallback<List<Warning>> cb)` | Các lần cảnh báo của một người dùng. | 69 |
| `public void getVerificationRequests(DataCallback<List<SellerVerification>> cb)` | Yêu cầu xác thực tài khoản đang PENDING. | 73 |
| `public void reviewVerification(String id, boolean approve, String note, DataCallback<Void> cb)` | Duyệt hoặc từ chối kèm ghi chú, cập nhật `User.verificationStatus`, thông báo VERIFICATION_RESULT. | 73 |
| `public void getActivityLogs(String id, DataCallback<List<ActivityLog>> cb)` | Nhật ký hoạt động của một tài khoản. | 74 |
| `public void hideListing(String id, String reason, DataCallback<Void> cb)` | Đặt tin HIDDEN kèm lý do bắt buộc, từ chối offer PENDING của tin, thông báo LISTING_MODERATED cho người đăng. | 76 |
| `public void restoreListing(String id, DataCallback<Void> cb)` | Đưa tin HIDDEN về AVAILABLE, gửi thông báo. | 76 |
| `public void removeListing(String id, String reason, DataCallback<Void> cb)` | Đặt một tin sang REMOVED kèm lý do, gửi thông báo LISTING_MODERATED cho người đăng; nặng hơn ẩn tin vì người bán không khôi phục được (UC37). | 76 |
| `public void removeAllListingsOf(String userId, DataCallback<Void> cb)` | Đặt mọi tin của tài khoản sang REMOVED. | 77 |
| `public void getKeywords(DataCallback<List<BannedKeyword>> cb)` | Danh sách từ khóa cấm. | 78 |
| `public void addKeyword(String word, DataCallback<Void> cb)` | Thêm từ khóa cấm (lưu dạng đã bỏ dấu). | 78 |
| `public void removeKeyword(String id, DataCallback<Void> cb)` | Xóa từ khóa cấm. | 78 |
| `public void clearFlag(String listingId, DataCallback<Void> cb)` | Admin bỏ cờ từ khóa cấm của một tin sau khi xem và thấy không vi phạm. | 78 |
| `public void getFlaggedListings(DataCallback<List<Listing>> cb)` | Tin có `flagged` bằng true, cho tab Tin bị gắn cờ của E04. |  |
| `public void listAdmins(DataCallback<List<User>> cb)` | Đọc `admins`, rồi đọc hồ sơ tương ứng để hiện tên và email. | 105 |
| `public void findUserByEmail(String email, DataCallback<User> cb)` | Tìm một tài khoản theo email để cấp quyền admin; chỉ trả tài khoản ACTIVE. | 105 |
| `public void grantAdmin(String userId, DataCallback<Void> cb)` | Tạo `admins/{userId}` và ghi nhật ký; từ chối khi tài khoản đã là admin. | 105 |
| `public void revokeAdmin(String userId, DataCallback<Void> cb)` | Xóa `admins/{userId}` và ghi nhật ký; từ chối khi `userId` là chính người đang thao tác, và khi đó là admin cuối cùng. | 105 |

**Quan hệ trên sơ đồ lớp**

- `AdminRepository` kế thừa `BaseRepository`. Trang 5. Tầng dữ liệu.

**Dùng tới:** [`ActivityLog`](model.md#activitylog), [`BannedKeyword`](model.md#bannedkeyword), [`DataCallback`](data.md#datacallback), [`Listing`](model.md#listing), [`SellerVerification`](model.md#sellerverification), [`User`](model.md#user), [`UserStatus`](enums.md#userstatus), [`Warning`](model.md#warning)

**Được dùng bởi:** [`AdminContentViewModel`](viewmodel.md#admincontentviewmodel), [`AdminUserViewModel`](viewmodel.md#adminuserviewmodel), [`AuthViewModel`](viewmodel.md#authviewmodel)

### DemoDataBuilder

| Mục | Nội dung |
| --- | --- |
| Định danh | `com.nhom3.pubgapp.data.DemoDataBuilder` |
| Loại | lớp (phạm vi gói, chỉ có phương thức tĩnh) |
| Kế thừa | Không |
| Lưu trữ | Không lưu |
| Trách nhiệm | Dựng dữ liệu mẫu cho bản demo, không gọi Firebase nên kiểm bằng test JVM được: 6 danh mục gốc, 12 danh mục con và 72 tin đăng (6 tin mỗi danh mục con) đủ yêu cầu ở `goi-y.md`, mục "Yêu cầu với dữ liệu mẫu". Mọi mã tài liệu bắt đầu bằng `demo_`. |
| Chức năng liên quan (STT) | 35, 36, 37, 38 (kết quả gợi ý), 27 (lọc bán kính) |

**Phương thức**

| Chữ ký | Mô tả | STT |
| --- | --- | --- |
| `static List<Category> buildRootCategories()` | Sáu danh mục gốc, mã `demo_cat_r1` đến `demo_cat_r6`. |  |
| `static List<Category> buildChildCategories()` | Mười hai danh mục con, mỗi danh mục gốc hai con, có `conditionFields`. |  |
| `static List<Listing> buildListings(List<String> imageIds, List<String> thumbs)` | 72 tin AVAILABLE mã `demo_listing_01` đến `demo_listing_72`; bốn danh mục con đầu cùng tình trạng USED để dùng tập giá P1, các con còn lại hỗn hợp để dùng tập P2; giá trải rộng; ba tỉnh; tin ở TP.HCM cách tâm Quận 1 khoảng 0,5, 2, 4 và 8 km. | 36, 37, 38, 27 |

**Dùng tới:** [`Category`](model.md#category), [`Listing`](model.md#listing)

**Được dùng bởi:** [`DemoDataSeeder`](data.md#demodataseeder)

### DemoDataSeeder

| Mục | Nội dung |
| --- | --- |
| Định danh | `com.nhom3.pubgapp.data.DemoDataSeeder` |
| Loại | lớp |
| Kế thừa | Không |
| Lưu trữ | `categories`, `listings`, `images` (Firestore) |
| Trách nhiệm | Chỉ dùng ở bản debug, chỉ cho tài khoản admin: tạo hoặc xóa dữ liệu mẫu do `DemoDataBuilder` dựng. Tin đăng ghi qua `ListingRepository.create`, ảnh mẫu vẽ trên máy rồi lưu qua `MediaRepository`. Tạo lại thì bỏ qua cái đã có và hiện lại tin đã ẩn. |
| Chức năng liên quan (STT) | 36, 37, 38 |

**Phương thức**

| Chữ ký | Mô tả | STT |
| --- | --- | --- |
| `public static boolean isRunning()` | Đang có một lượt tạo hoặc xóa chạy. |  |
| `public void seed(ProgressListener progress, DataCallback<Integer> cb)` | Tạo danh mục còn thiếu (gốc trước, con sau), tạo tin còn thiếu, hiện lại tin `demo_` đã ẩn; trả số tin đã tạo hoặc hiện lại. |  |
| `public void clear(ProgressListener progress, DataCallback<Integer> cb)` | Ẩn mọi tin `demo_` đang bán bằng `ListingRepository.setHidden` (luật chỉ cho xóa tin nháp) rồi xóa danh mục `demo_cat_`; trả số tin đã ẩn. |  |

**Dùng tới:** [`DataCallback`](data.md#datacallback), [`ListingRepository`](data.md#listingrepository), [`MediaRepository`](data.md#mediarepository), [`DemoDataBuilder`](data.md#demodatabuilder)

**Được dùng bởi:** màn E01 `AdminDashboardFragment` (khối `DemoDataPanel`)
