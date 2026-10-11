# Đặc tả lớp: gói `com.nhom3.pubgapp.model`

Bản chữ của sơ đồ lớp cho gói này: 21 lớp. Mục lục và cách đọc ở [README.md](README.md).

Mô hình miền: các tài liệu Firestore và quy tắc nghiệp vụ nằm trong chính lớp. Không phụ thuộc Android, trừ kiểu `Timestamp` và `GeoPoint` của Firebase. Mỗi lớp tài liệu cần hàm dựng rỗng và getter, setter công khai để Firestore tự chuyển đổi (`toObject`).

| Lớp | Loại | Trách nhiệm |
| --- | --- | --- |
| [`BaseDocument`](#basedocument) | lớp trừu tượng | Lớp cha của mọi tài liệu Firestore: mã, thời điểm tạo và sửa. |
| [`User`](#user) | lớp | Người dùng: hồ sơ công khai, khu vực, trạng thái tài khoản, điểm đánh giá, số liệu hồ sơ, cài đặt thông báo. |
| [`UserAddress`](#useraddress) | lớp | Địa chỉ giao nhận của một người dùng (mỗi người có nhiều địa chỉ). |
| [`UserIdentity`](#useridentity) | lớp | Thông tin định danh riêng tư (số CCCD, ngày sinh, giới tính, địa chỉ thường trú); chỉ chủ tài khoản và admin đọc được. |
| [`SellerVerification`](#sellerverification) | lớp | Yêu cầu xác minh để trở thành người bán, do admin duyệt. |
| [`Warning`](#warning) | lớp | Một lần cảnh báo vi phạm; bậc 1 cảnh báo, bậc 2 khóa tạm, bậc 3 khóa vĩnh viễn. |
| [`AppNotification`](#appnotification) | lớp | Thông báo gửi cho một người dùng (offer, giao dịch, kết quả khiếu nại, cảnh báo). |
| [`Category`](#category) | lớp | Danh mục sản phẩm, có danh mục con và danh sách thuộc tính tình trạng riêng. |
| [`Listing`](#listing) | lớp | Tin đăng bán, trao đổi hoặc cho tặng; nhiều hình thức cùng lúc, giới hạn mức offer. |
| [`SearchFilter`](#searchfilter) | lớp (value object) | Đối tượng giá trị gom mọi tiêu chí tìm kiếm và lọc; không lưu trong Firestore. |
| [`Conversation`](#conversation) | lớp | Cuộc trò chuyện giữa người mua và người bán về một tin. |
| [`ChatMessage`](#chatmessage) | lớp | Tin nhắn chữ, ảnh, video hoặc một offer trong cuộc trò chuyện. |
| [`Offer`](#offer) | lớp | Đề nghị một chạm: tiền, vật trao đổi, hoặc cả hai. |
| [`Deal`](#deal) | lớp | Giao dịch giữa hai bên; kèm bằng chứng vận đơn, mốc đếm ngược tự giải ngân, bước Đã nhận trước Hoàn tất. |
| [`StatusChange`](#statuschange) | lớp (value object) | Một dòng lịch sử trạng thái của giao dịch (statusHistory), nằm trong Deal. |
| [`Payment`](#payment) | lớp | Khoản tiền ký quỹ mô phỏng: giữ, chuyển cho người bán, hoặc hoàn lại. |
| [`Review`](#review) | lớp | Đánh giá đối tác sau một giao dịch: sao tổng thể và 3 tiêu chí; mỗi bên đánh giá một lần. |
| [`Report`](#report) | lớp | Báo cáo hành vi, phát ngôn hoặc tin đăng vi phạm. |
| [`Complaint`](#complaint) | lớp | Đơn khiếu nại giao dịch kèm bằng chứng; adminNote là ghi chú nội bộ của admin. |
| [`ActivityLog`](#activitylog) | lớp | Nhật ký hành động quan trọng của một tài khoản để admin theo dõi. |
| [`BannedKeyword`](#bannedkeyword) | lớp | Một từ khóa cấm; tin đăng chứa nó bị gắn cờ cần chú ý. |

### BaseDocument

| Mục | Nội dung |
| --- | --- |
| Định danh | `com.nhom3.pubgapp.model.BaseDocument` |
| Loại | lớp trừu tượng |
| Lưu trữ | Không lưu |
| Trách nhiệm | Lớp cha của mọi tài liệu Firestore: mã, thời điểm tạo và sửa. |
| Chức năng liên quan (STT) | hạ tầng dùng chung |

**Thuộc tính**

| Tên | Kiểu | Truy cập | Ý nghĩa |
| --- | --- | --- | --- |
| `id` | `String` | protected | Mã tài liệu Firestore. Với `User` là `uid` của Firebase Authentication. |
| `createdAt` | `Timestamp` | protected | Thời điểm tạo, lấy giờ máy chủ Firestore lúc ghi. |
| `updatedAt` | `Timestamp` | protected | Thời điểm sửa gần nhất; `touch()` đặt lại trước mỗi lần ghi. |

**Phương thức**

| Chữ ký | Mô tả | STT |
| --- | --- | --- |
| `public String getId()` | Trả mã tài liệu. |  |
| `public void touch()` | Đặt `updatedAt` bằng thời điểm hiện tại; gọi trước mỗi lần ghi. |  |

**Quan hệ trên sơ đồ lớp**

- `User` kế thừa `BaseDocument`. Trang 1. Người dùng.
- `UserAddress` kế thừa `BaseDocument`. Trang 1. Người dùng.
- `Warning` kế thừa `BaseDocument`. Trang 1. Người dùng.
- `SellerVerification` kế thừa `BaseDocument`. Trang 1. Người dùng.
- `AppNotification` kế thừa `BaseDocument`. Trang 1. Người dùng.
- `Category` kế thừa `BaseDocument`. Trang 2. Tin đăng, tìm kiếm.
- `Listing` kế thừa `BaseDocument`. Trang 2. Tin đăng, tìm kiếm.
- `Conversation` kế thừa `BaseDocument`. Trang 3. Trao đổi, giao dịch.
- `ChatMessage` kế thừa `BaseDocument`. Trang 3. Trao đổi, giao dịch.
- `Offer` kế thừa `BaseDocument`. Trang 3. Trao đổi, giao dịch.
- `Deal` kế thừa `BaseDocument`. Trang 3. Trao đổi, giao dịch.
- `Payment` kế thừa `BaseDocument`. Trang 3. Trao đổi, giao dịch.
- `Review` kế thừa `BaseDocument`. Trang 4. Tin cậy, quản trị.
- `Report` kế thừa `BaseDocument`. Trang 4. Tin cậy, quản trị.
- `Complaint` kế thừa `BaseDocument`. Trang 4. Tin cậy, quản trị.
- `ActivityLog` kế thừa `BaseDocument`. Trang 4. Tin cậy, quản trị.
- `BannedKeyword` kế thừa `BaseDocument`. Trang 4. Tin cậy, quản trị.
- `UserIdentity` kế thừa `BaseDocument`. Trang 1. Người dùng.

### User

| Mục | Nội dung |
| --- | --- |
| Định danh | `com.nhom3.pubgapp.model.User` |
| Loại | lớp |
| Kế thừa | [`BaseDocument`](model.md#basedocument) |
| Lưu trữ | Firestore `users/{uid}`, xem [firestore-schema.md](../../data/firestore-schema.md#users) |
| Trách nhiệm | Người dùng: hồ sơ công khai, khu vực, trạng thái tài khoản, điểm đánh giá, số liệu hồ sơ, cài đặt thông báo. |
| Chức năng liên quan (STT) | 1 đến 5, 7, 8, 10, 12, 31 đến 35, 46, 59, 70 đến 73 |

Kế thừa từ `BaseDocument`: `id`, `createdAt`, `updatedAt`, `getId()`, `touch()`.

**Thuộc tính**

| Tên | Kiểu | Truy cập | Ý nghĩa |
| --- | --- | --- | --- |
| `email` | `String` | private | Email đăng nhập, lấy từ Firebase Authentication. |
| `displayName` | `String` | private | Họ tên hiển thị. |
| `phone` | `String` | private | Số điện thoại liên hệ. Không dùng để đăng nhập. |
| `avatarUrl` | `String` | private | Đường dẫn ảnh đại diện trên Cloud Storage. |
| `bio` | `String` | private | Đoạn giới thiệu ngắn trên trang cá nhân. |
| `status` | `UserStatus` | private | Trạng thái tài khoản. Admin đổi giữa ACTIVE và hai mức khóa; chính chủ chỉ được đổi sang DELETED khi vô hiệu hóa. |
| `lockedUntil` | `Timestamp` | private | Hạn khóa tạm. Rỗng khi không khóa hoặc khóa vĩnh viễn. |
| `verificationStatus` | `VerificationStatus` | private | Trạng thái xác minh người bán; APPROVED thì hồ sơ có huy hiệu đã xác minh. |
| `ratingAverage` | `double` | private | Điểm sao trung bình nhận được, 0 khi chưa có đánh giá. |
| `ratingCount` | `int` | private | Số đánh giá đã nhận. |
| `warningCount` | `int` | private | Số lần bị cảnh báo; quyết định bậc của lần cảnh báo kế tiếp. |
| `interestedCategoryIds` | `List<String>` | private | Danh mục quan tâm chọn lúc đăng ký; nguồn của gợi ý. |
| `province` | `String` | private | Tỉnh hoặc thành phố. |
| `district` | `String` | private | Quận hoặc huyện. |
| `termsAcceptedAt` | `Timestamp` | private | Lúc đồng ý Điều khoản sử dụng và Chính sách bảo mật khi đăng ký. |
| `notificationPrefs` | `Map<String,Boolean>` | private | Bật hoặc tắt từng loại thông báo. Khóa là tên hằng `NotificationType`, thiếu khóa nghĩa là bật. |
| `lastActiveAt` | `Timestamp` | private | Lần cuối ứng dụng của người này hoạt động; dùng cho trạng thái hoạt động trong chat. |
| `soldCount` | `int` | private | Số món đã bán hoặc đã trao (giao dịch hoàn tất với vai trò người bán). |
| `followerCount` | `int` | private | Số người đang theo dõi. |
| `dealCount` | `int` | private | Số giao dịch đã tạo, ở cả hai vai trò. |
| `completedDealCount` | `int` | private | Số giao dịch đã hoàn tất; chia cho `dealCount` ra tỷ lệ hoàn tất. |
| `reportCount` | `int` | private | Số báo cáo nhắm vào người này; admin dùng khi lọc. |

**Phương thức**

| Chữ ký | Mô tả | STT |
| --- | --- | --- |
| `public boolean isLocked()` | True khi `status` là PERM_LOCKED, hoặc TEMP_LOCKED và `lockedUntil` chưa qua. | 70, 71 |
| `public boolean isDeleted()` | True khi `status` là DELETED. | 12 |
| `public boolean canSell()` | True khi tài khoản ACTIVE, không bị khóa và đã xác thực email. Xác minh người bán không bắt buộc để đăng tin; nó chỉ thêm huy hiệu. | 73 |
| `public void applyRating(int)` | Cộng một đánh giá mới vào `ratingAverage` và tăng `ratingCount`. | 59 |
| `public double completionRate()` | `completedDealCount / dealCount`; 0 khi chưa có giao dịch. | 8 |
| `public boolean isActiveSince(Timestamp)` | True khi `lastActiveAt` sau mốc truyền vào; ứng dụng dùng mốc 5 phút trước để hiện Đang hoạt động. | 46 |

**Quan hệ trên sơ đồ lớp**

- `User` kế thừa `BaseDocument`. Trang 1. Người dùng.
- Hợp thành: một `User` chứa * `UserAddress` (vai trò `addresses`); phần con không tồn tại khi không có cha. Trang 1. Người dùng.
- Hợp thành: một `User` chứa * `Warning` (vai trò `warnings`); phần con không tồn tại khi không có cha. Trang 1. Người dùng.
- Liên kết: `User` [1] và `SellerVerification` [0..1] (requests). Trang 1. Người dùng.
- Liên kết: `AppNotification` [*] và `User` [1] (recipient). Trang 1. Người dùng.
- Liên kết: `User` [*] và `User` [*] (follows). Trang 1. Người dùng.
- Liên kết: `Listing` [*] và `User` [1] (seller). Trang 2. Tin đăng, tìm kiếm.
- Liên kết: `User` [*] và `Listing` [*] (saves). Trang 2. Tin đăng, tìm kiếm.
- Liên kết: `Conversation` [*] và `User` [2] (buyer, seller). Trang 3. Trao đổi, giao dịch.
- Liên kết: `Deal` [*] và `User` [2] (buyer, seller). Trang 3. Trao đổi, giao dịch.
- Liên kết: `Complaint` [*] và `User` [1] (complainant). Trang 4. Tin cậy, quản trị.
- Liên kết: `Review` [*] và `User` [2] (reviewer, reviewee). Trang 4. Tin cậy, quản trị.
- Liên kết: `ActivityLog` [*] và `User` [1] (performed by). Trang 4. Tin cậy, quản trị.
- Liên kết: `Report` [*] và `User` [1] (reporter). Trang 4. Tin cậy, quản trị.
- Hợp thành: một `User` chứa 0..1 `UserIdentity` (vai trò `identity`); phần con không tồn tại khi không có cha. Trang 1. Người dùng.

**Dùng tới:** [`UserStatus`](enums.md#userstatus), [`VerificationStatus`](enums.md#verificationstatus)

**Được dùng bởi:** [`AdminRepository`](data.md#adminrepository), [`AdminUserViewModel`](viewmodel.md#adminuserviewmodel), [`AuthRepository`](data.md#authrepository), [`AuthViewModel`](viewmodel.md#authviewmodel), [`ChatViewModel`](viewmodel.md#chatviewmodel), [`ListingDetailViewModel`](viewmodel.md#listingdetailviewmodel), [`ProfileViewModel`](viewmodel.md#profileviewmodel), [`RecommendationEngine`](util.md#recommendationengine), [`SessionManager`](util.md#sessionmanager), [`UserRepository`](data.md#userrepository)

### UserAddress

| Mục | Nội dung |
| --- | --- |
| Định danh | `com.nhom3.pubgapp.model.UserAddress` |
| Loại | lớp |
| Kế thừa | [`BaseDocument`](model.md#basedocument) |
| Lưu trữ | Firestore `users/{uid}/addresses/{id}`, xem [firestore-schema.md](../../data/firestore-schema.md#addresses) |
| Trách nhiệm | Địa chỉ giao nhận của một người dùng (mỗi người có nhiều địa chỉ). |
| Chức năng liên quan (STT) | 6 |

Kế thừa từ `BaseDocument`: `id`, `createdAt`, `updatedAt`, `getId()`, `touch()`.

**Thuộc tính**

| Tên | Kiểu | Truy cập | Ý nghĩa |
| --- | --- | --- | --- |
| `userId` | `String` | private | Chủ địa chỉ. |
| `label` | `String` | private | Nhãn do người dùng đặt, ví dụ Nhà, Công ty. |
| `receiverName` | `String` | private | Tên người nhận. |
| `phone` | `String` | private | Số điện thoại người nhận. |
| `province` | `String` | private | Tỉnh hoặc thành phố. |
| `district` | `String` | private | Quận hoặc huyện. |
| `detail` | `String` | private | Số nhà, đường, phường. |
| `location` | `GeoPoint` | private | Tọa độ, dùng cho lọc theo bán kính. Có thể rỗng. |
| `isDefault` | `boolean` | private | Địa chỉ mặc định; mỗi người chỉ một địa chỉ có giá trị true. |

**Phương thức**

| Chữ ký | Mô tả | STT |
| --- | --- | --- |
| `public String getFullText()` | Ghép chi tiết, quận, tỉnh thành một dòng địa chỉ. |  |

**Quan hệ trên sơ đồ lớp**

- `UserAddress` kế thừa `BaseDocument`. Trang 1. Người dùng.
- Hợp thành: một `User` chứa * `UserAddress` (vai trò `addresses`); phần con không tồn tại khi không có cha. Trang 1. Người dùng.

**Được dùng bởi:** [`ProfileViewModel`](viewmodel.md#profileviewmodel), [`UserRepository`](data.md#userrepository)

### UserIdentity

| Mục | Nội dung |
| --- | --- |
| Định danh | `com.nhom3.pubgapp.model.UserIdentity` |
| Loại | lớp |
| Kế thừa | [`BaseDocument`](model.md#basedocument) |
| Lưu trữ | Firestore `users/{uid}/private/identity`, xem [firestore-schema.md](../../data/firestore-schema.md#identity) |
| Trách nhiệm | Thông tin định danh riêng tư (số CCCD, ngày sinh, giới tính, địa chỉ thường trú); chỉ chủ tài khoản và admin đọc được. |
| Chức năng liên quan (STT) | 1 đến 3, 9 |

Kế thừa từ `BaseDocument`: `id`, `createdAt`, `updatedAt`, `getId()`, `touch()`.

**Thuộc tính**

| Tên | Kiểu | Truy cập | Ý nghĩa |
| --- | --- | --- | --- |
| `userId` | `String` | private | Chủ thông tin định danh, trùng mã tài liệu cha. |
| `nationalId` | `String` | private | Số căn cước công dân, 12 chữ số. Không hiển thị công khai. |
| `dateOfBirth` | `Timestamp` | private | Ngày sinh. |
| `gender` | `Gender` | private | Giới tính. |
| `permanentAddress` | `String` | private | Địa chỉ thường trú. |

**Phương thức**

| Chữ ký | Mô tả | STT |
| --- | --- | --- |
| `public boolean isComplete()` | True khi đủ số CCCD 12 chữ số, ngày sinh và giới tính. |  |

**Quan hệ trên sơ đồ lớp**

- `UserIdentity` kế thừa `BaseDocument`. Trang 1. Người dùng.
- Hợp thành: một `User` chứa 0..1 `UserIdentity` (vai trò `identity`); phần con không tồn tại khi không có cha. Trang 1. Người dùng.

**Dùng tới:** [`Gender`](enums.md#gender)

**Được dùng bởi:** [`AuthViewModel`](viewmodel.md#authviewmodel), [`ProfileViewModel`](viewmodel.md#profileviewmodel), [`UserRepository`](data.md#userrepository)

### SellerVerification

| Mục | Nội dung |
| --- | --- |
| Định danh | `com.nhom3.pubgapp.model.SellerVerification` |
| Loại | lớp |
| Kế thừa | [`BaseDocument`](model.md#basedocument) |
| Lưu trữ | Firestore `sellerVerifications/{userId}`, xem [firestore-schema.md](../../data/firestore-schema.md#sellerverifications) |
| Trách nhiệm | Yêu cầu xác minh để trở thành người bán, do admin duyệt. |
| Chức năng liên quan (STT) | 73 |

Kế thừa từ `BaseDocument`: `id`, `createdAt`, `updatedAt`, `getId()`, `touch()`.

**Thuộc tính**

| Tên | Kiểu | Truy cập | Ý nghĩa |
| --- | --- | --- | --- |
| `userId` | `String` | private | Người gửi yêu cầu; cũng là mã tài liệu. |
| `status` | `VerificationStatus` | private | Trạng thái yêu cầu. |
| `note` | `String` | private | Lý do từ chối do admin ghi. |
| `reviewedBy` | `String` | private | Admin đã xử lý. |
| `reviewedAt` | `Timestamp` | private | Lúc xử lý. |

**Phương thức**

| Chữ ký | Mô tả | STT |
| --- | --- | --- |
| `public void approve(String adminId)` | Đặt APPROVED, ghi admin và thời điểm; `User.verificationStatus` đổi theo. |  |
| `public void reject(String adminId, String note)` | Đặt REJECTED kèm ghi chú lý do. |  |

**Quan hệ trên sơ đồ lớp**

- `SellerVerification` kế thừa `BaseDocument`. Trang 1. Người dùng.
- Liên kết: `User` [1] và `SellerVerification` [0..1] (requests). Trang 1. Người dùng.

**Dùng tới:** [`VerificationStatus`](enums.md#verificationstatus)

**Được dùng bởi:** [`AdminRepository`](data.md#adminrepository)

### Warning

| Mục | Nội dung |
| --- | --- |
| Định danh | `com.nhom3.pubgapp.model.Warning` |
| Loại | lớp |
| Kế thừa | [`BaseDocument`](model.md#basedocument) |
| Lưu trữ | Firestore `users/{uid}/warnings/{id}`, xem [firestore-schema.md](../../data/firestore-schema.md#warnings) |
| Trách nhiệm | Một lần cảnh báo vi phạm; bậc 1 cảnh báo, bậc 2 khóa tạm, bậc 3 khóa vĩnh viễn. |
| Chức năng liên quan (STT) | 72 |

Kế thừa từ `BaseDocument`: `id`, `createdAt`, `updatedAt`, `getId()`, `touch()`.

**Thuộc tính**

| Tên | Kiểu | Truy cập | Ý nghĩa |
| --- | --- | --- | --- |
| `userId` | `String` | private | Người bị cảnh báo. |
| `level` | `int` | private | Bậc cảnh báo: 1, 2 hoặc 3, bằng `User.warningCount` sau khi cộng. |
| `reason` | `String` | private | Lý do cảnh báo, admin ghi. |
| `issuedBy` | `String` | private | Admin đã cảnh báo. |
| `targetId` | `String` | private | Tin, báo cáo hoặc khiếu nại dẫn tới cảnh báo. Có thể rỗng. |

**Phương thức**

| Chữ ký | Mô tả | STT |
| --- | --- | --- |
| `public UserStatus resultingStatus()` | Bậc 1 trả ACTIVE (chỉ cảnh báo), bậc 2 trả TEMP_LOCKED, bậc 3 trả PERM_LOCKED. | 72 |

**Quan hệ trên sơ đồ lớp**

- `Warning` kế thừa `BaseDocument`. Trang 1. Người dùng.
- Hợp thành: một `User` chứa * `Warning` (vai trò `warnings`); phần con không tồn tại khi không có cha. Trang 1. Người dùng.

**Dùng tới:** [`UserStatus`](enums.md#userstatus)

**Được dùng bởi:** [`AdminRepository`](data.md#adminrepository), [`AdminUserViewModel`](viewmodel.md#adminuserviewmodel)

### AppNotification

| Mục | Nội dung |
| --- | --- |
| Định danh | `com.nhom3.pubgapp.model.AppNotification` |
| Loại | lớp |
| Kế thừa | [`BaseDocument`](model.md#basedocument) |
| Lưu trữ | Firestore `notifications/{id}`, xem [firestore-schema.md](../../data/firestore-schema.md#notifications) |
| Trách nhiệm | Thông báo gửi cho một người dùng (offer, giao dịch, kết quả khiếu nại, cảnh báo). |
| Chức năng liên quan (STT) | 67, 91 |

Kế thừa từ `BaseDocument`: `id`, `createdAt`, `updatedAt`, `getId()`, `touch()`.

**Thuộc tính**

| Tên | Kiểu | Truy cập | Ý nghĩa |
| --- | --- | --- | --- |
| `recipientId` | `String` | private | Người nhận. |
| `type` | `NotificationType` | private | Loại thông báo; quyết định biểu tượng và màn hình mở ra khi chạm. |
| `title` | `String` | private | Tiêu đề ngắn. |
| `body` | `String` | private | Nội dung. |
| `targetId` | `String` | private | Mã đối tượng liên quan (giao dịch, offer, khiếu nại, tin) để mở đúng màn hình. |
| `isRead` | `boolean` | private | Đã đọc hay chưa. |

**Phương thức**

| Chữ ký | Mô tả | STT |
| --- | --- | --- |
| `public void markRead()` | Đặt `isRead` là true. |  |

**Quan hệ trên sơ đồ lớp**

- `AppNotification` kế thừa `BaseDocument`. Trang 1. Người dùng.
- Liên kết: `AppNotification` [*] và `User` [1] (recipient). Trang 1. Người dùng.

**Dùng tới:** [`NotificationType`](enums.md#notificationtype)

**Được dùng bởi:** [`NotificationRepository`](data.md#notificationrepository), [`NotificationViewModel`](viewmodel.md#notificationviewmodel)

### Category

| Mục | Nội dung |
| --- | --- |
| Định danh | `com.nhom3.pubgapp.model.Category` |
| Loại | lớp |
| Kế thừa | [`BaseDocument`](model.md#basedocument) |
| Lưu trữ | Firestore `categories/{id}`, xem [firestore-schema.md](../../data/firestore-schema.md#categories) |
| Trách nhiệm | Danh mục sản phẩm, có danh mục con và danh sách thuộc tính tình trạng riêng. |
| Chức năng liên quan (STT) | 21, 79, 80 |

Kế thừa từ `BaseDocument`: `id`, `createdAt`, `updatedAt`, `getId()`, `touch()`.

**Thuộc tính**

| Tên | Kiểu | Truy cập | Ý nghĩa |
| --- | --- | --- | --- |
| `name` | `String` | private | Tên danh mục. |
| `parentId` | `String` | private | Danh mục cha. Rỗng nghĩa là danh mục gốc. Chỉ có hai cấp. |
| `conditionFields` | `List<String>` | private | Tên các thuộc tính tình trạng riêng của danh mục, ví dụ Dung lượng pin, Bảo hành. |
| `isActive` | `boolean` | private | Danh mục còn dùng; false thì ẩn khỏi lựa chọn khi đăng tin. |
| `listingCount` | `int` | private | Số tin đang hiển thị trong danh mục, cập nhật khi đăng hoặc gỡ tin. |

**Phương thức**

| Chữ ký | Mô tả | STT |
| --- | --- | --- |
| `public boolean isRoot()` | True khi `parentId` rỗng. | 79 |
| `public boolean hasConditionField(String)` | True khi tên thuộc tính có trong `conditionFields`. | 80 |

**Quan hệ trên sơ đồ lớp**

- `Category` kế thừa `BaseDocument`. Trang 2. Tin đăng, tìm kiếm.
- Liên kết: `Category` [0..1] và `Category` [*] (parent). Trang 2. Tin đăng, tìm kiếm.
- Liên kết: `Listing` [*] và `Category` [1] (category). Trang 2. Tin đăng, tìm kiếm.

**Được dùng bởi:** [`AdminContentViewModel`](viewmodel.md#admincontentviewmodel), [`CategoryRepository`](data.md#categoryrepository)

### Listing

| Mục | Nội dung |
| --- | --- |
| Định danh | `com.nhom3.pubgapp.model.Listing` |
| Loại | lớp |
| Kế thừa | [`BaseDocument`](model.md#basedocument) |
| Lưu trữ | Firestore `listings/{id}`, xem [firestore-schema.md](../../data/firestore-schema.md#listings) |
| Trách nhiệm | Tin đăng bán, trao đổi hoặc cho tặng; nhiều hình thức cùng lúc, giới hạn mức offer. |
| Chức năng liên quan (STT) | 13, 15 đến 23, 27, 76, 78, 80 |

Kế thừa từ `BaseDocument`: `id`, `createdAt`, `updatedAt`, `getId()`, `touch()`.

**Thuộc tính**

| Tên | Kiểu | Truy cập | Ý nghĩa |
| --- | --- | --- | --- |
| `sellerId` | `String` | private | Người đăng. |
| `categoryId` | `String` | private | Danh mục, phải là danh mục con hoặc danh mục gốc không có con. |
| `title` | `String` | private | Tiêu đề tin. |
| `description` | `String` | private | Mô tả món đồ và yêu cầu của người đăng. |
| `imageUrls` | `List<String>` | private | Đường dẫn ảnh trên Cloud Storage, từ 1 đến 5 ảnh. |
| `itemCondition` | `ItemCondition` | private | Tình trạng chung của món đồ. |
| `conditionValues` | `Map<String,String>` | private | Giá trị của từng thuộc tính tình trạng; khóa lấy từ `Category.conditionFields`. |
| `tradeTypes` | `List<TradeType>` | private | Các hình thức chấp nhận, chọn được nhiều. Có GIVEAWAY thì giá bằng 0. |
| `price` | `long` | private | Giá đăng, đơn vị đồng. 0 khi chỉ trao đổi hoặc cho tặng. |
| `acceptedOfferTypes` | `List<OfferType>` | private | Các loại offer người bán nhận. |
| `minOfferPrice` | `long` | private | Mức tiền offer tối thiểu, 0 là không giới hạn. |
| `province` | `String` | private | Tỉnh hoặc thành phố, dùng cho lọc khu vực. |
| `location` | `GeoPoint` | private | Tọa độ món đồ, dùng cho lọc bán kính và sắp theo khoảng cách. |
| `status` | `ListingStatus` | private | Trạng thái tin, xem `trang-thai.md`. |
| `statusReason` | `String` | private | Lý do admin ẩn hoặc gỡ tin, hiển thị cho người đăng. |
| `searchKeywords` | `List<String>` | private | Các từ đã bỏ dấu, viết thường của tiêu đề và mô tả, để truy vấn `array-contains`. |
| `flagged` | `boolean` | private | Bị gắn cờ vì chứa từ khóa cấm; tin vẫn đăng nhưng hiện trong danh sách cần chú ý của admin. |

**Phương thức**

| Chữ ký | Mô tả | STT |
| --- | --- | --- |
| `public boolean isAvailable()` | True khi `status` là AVAILABLE. |  |
| `public boolean isDraft()` | True khi `status` là DRAFT. | 22 |
| `public void publish()` | Chuyển DRAFT sang AVAILABLE; yêu cầu đủ tiêu đề, danh mục, ít nhất 1 ảnh và một hình thức giao dịch. | 22 |
| `public boolean acceptsTradeType(TradeType)` | True khi hình thức nằm trong `tradeTypes`. | 18 |
| `public boolean acceptsOffer(Offer)` | True khi tin AVAILABLE, loại offer nằm trong `acceptedOfferTypes` và số tiền không dưới `minOfferPrice`. | 20 |
| `public void markSold()` | Chuyển sang SOLD khi giao dịch của tin hoàn tất. | 13 |
| `public void hide(String reason)` | Chuyển sang HIDDEN kèm lý do. Người bán tự ẩn thì lý do rỗng. | 15, 76 |
| `public void restore()` | Từ HIDDEN về AVAILABLE và xóa lý do. | 15, 76 |
| `public void buildSearchKeywords()` | Tạo `searchKeywords` từ tiêu đề và mô tả bằng `FormatUtils.toKeywords`; gọi trước khi ghi. | 23 |

**Quan hệ trên sơ đồ lớp**

- `Listing` kế thừa `BaseDocument`. Trang 2. Tin đăng, tìm kiếm.
- Liên kết: `Listing` [*] và `Category` [1] (category). Trang 2. Tin đăng, tìm kiếm.
- Liên kết: `Listing` [*] và `User` [1] (seller). Trang 2. Tin đăng, tìm kiếm.
- Liên kết: `User` [*] và `Listing` [*] (saves). Trang 2. Tin đăng, tìm kiếm.
- Phụ thuộc: `SearchFilter` dùng `Listing` (filters). Trang 2. Tin đăng, tìm kiếm.
- Liên kết: `Conversation` [*] và `Listing` [1] (about). Trang 3. Trao đổi, giao dịch.
- Liên kết: `Deal` [*] và `Listing` [1] (for). Trang 3. Trao đổi, giao dịch.
- Phụ thuộc: `Report` dùng `Listing` (target: Listing, User, ChatMessage). Trang 4. Tin cậy, quản trị.
- Phụ thuộc: `ListingEntity` dùng `Listing` (chuyển đổi). Trang 6. Lưu trữ cục bộ (Room).

**Dùng tới:** [`ItemCondition`](enums.md#itemcondition), [`ListingStatus`](enums.md#listingstatus), [`Offer`](model.md#offer), [`OfferType`](enums.md#offertype), [`TradeType`](enums.md#tradetype)

**Được dùng bởi:** [`AdminContentViewModel`](viewmodel.md#admincontentviewmodel), [`FeedViewModel`](viewmodel.md#feedviewmodel), [`GeoUtils`](util.md#geoutils), [`ListingDetailViewModel`](viewmodel.md#listingdetailviewmodel), [`ListingEntity`](data-local.md#listingentity), [`ListingRepository`](data.md#listingrepository), [`ProfileViewModel`](viewmodel.md#profileviewmodel), [`RecommendationEngine`](util.md#recommendationengine), [`SearchViewModel`](viewmodel.md#searchviewmodel), [`ShopViewModel`](viewmodel.md#shopviewmodel)

### SearchFilter

| Mục | Nội dung |
| --- | --- |
| Định danh | `com.nhom3.pubgapp.model.SearchFilter` |
| Loại | lớp (value object) |
| Lưu trữ | Không lưu riêng (đối tượng giá trị) |
| Trách nhiệm | Đối tượng giá trị gom mọi tiêu chí tìm kiếm và lọc; không lưu trong Firestore. |
| Chức năng liên quan (STT) | 23 đến 28, 30 đến 34 |

**Thuộc tính**

| Tên | Kiểu | Truy cập | Ý nghĩa |
| --- | --- | --- | --- |
| `keyword` | `String` | private | Từ khóa người dùng gõ. |
| `categoryId` | `String` | private | Danh mục cần lọc; rỗng là mọi danh mục. |
| `minPrice` | `long` | private | Giá thấp nhất, 0 là không giới hạn. |
| `maxPrice` | `long` | private | Giá cao nhất, 0 là không giới hạn. |
| `conditions` | `List<ItemCondition>` | private | Các tình trạng chấp nhận; rỗng là mọi tình trạng. |
| `province` | `String` | private | Tỉnh hoặc thành phố. |
| `center` | `GeoPoint` | private | Tâm tìm theo bán kính, thường là vị trí hiện tại. |
| `radiusKm` | `int` | private | Bán kính 1, 3 hoặc 5 km; 0 là không lọc bán kính. |
| `tradeTypes` | `List<TradeType>` | private | Các hình thức giao dịch cần lọc. |
| `sortOrder` | `SortOrder` | private | Cách sắp xếp kết quả. |
| `followingOnly` | `boolean` | private | Chỉ tin của người đang theo dõi. |

**Phương thức**

| Chữ ký | Mô tả | STT |
| --- | --- | --- |
| `public boolean hasPriceRange()` | True khi có giá thấp nhất hoặc cao nhất. | 25 |
| `public boolean hasRadius()` | True khi có tâm và bán kính lớn hơn 0. | 27 |
| `public void reset()` | Xóa mọi tiêu chí về mặc định. |  |

**Quan hệ trên sơ đồ lớp**

- Phụ thuộc: `SearchFilter` dùng `Listing` (filters). Trang 2. Tin đăng, tìm kiếm.

**Dùng tới:** [`ItemCondition`](enums.md#itemcondition), [`SortOrder`](enums.md#sortorder), [`TradeType`](enums.md#tradetype)

**Được dùng bởi:** [`ListingRepository`](data.md#listingrepository), [`SearchViewModel`](viewmodel.md#searchviewmodel)

### Conversation

| Mục | Nội dung |
| --- | --- |
| Định danh | `com.nhom3.pubgapp.model.Conversation` |
| Loại | lớp |
| Kế thừa | [`BaseDocument`](model.md#basedocument) |
| Lưu trữ | Firestore `conversations/{listingId_buyerId}`, xem [firestore-schema.md](../../data/firestore-schema.md#conversations) |
| Trách nhiệm | Cuộc trò chuyện giữa người mua và người bán về một tin. |
| Chức năng liên quan (STT) | 43 đến 45 |

Kế thừa từ `BaseDocument`: `id`, `createdAt`, `updatedAt`, `getId()`, `touch()`.

**Thuộc tính**

| Tên | Kiểu | Truy cập | Ý nghĩa |
| --- | --- | --- | --- |
| `buyerId` | `String` | private | Người hỏi mua. |
| `sellerId` | `String` | private | Người đăng tin. |
| `listingId` | `String` | private | Tin đang được bàn. Mã tài liệu là `listingId_buyerId` nên mỗi người mua chỉ có một cuộc trò chuyện cho một tin. |
| `lastMessage` | `String` | private | Bản xem trước tin nhắn cuối, hiện ở danh sách chat. |
| `lastMessageAt` | `Timestamp` | private | Lúc có tin nhắn cuối, dùng để sắp danh sách chat. |

**Phương thức**

| Chữ ký | Mô tả | STT |
| --- | --- | --- |
| `public boolean isParticipant(String userId)` | True khi người đó là người mua hoặc người bán của cuộc trò chuyện. |  |
| `public String otherParty(String userId)` | Trả mã của bên còn lại. |  |

**Quan hệ trên sơ đồ lớp**

- `Conversation` kế thừa `BaseDocument`. Trang 3. Trao đổi, giao dịch.
- Hợp thành: một `Conversation` chứa * `ChatMessage` (vai trò `messages`); phần con không tồn tại khi không có cha. Trang 3. Trao đổi, giao dịch.
- Liên kết: `Conversation` [*] và `Listing` [1] (about). Trang 3. Trao đổi, giao dịch.
- Liên kết: `Conversation` [*] và `User` [2] (buyer, seller). Trang 3. Trao đổi, giao dịch.
- Liên kết: `Offer` [*] và `Conversation` [1] (made in). Trang 3. Trao đổi, giao dịch.
- Phụ thuộc: `ConversationEntity` dùng `Conversation`. Trang 6. Lưu trữ cục bộ (Room).

**Được dùng bởi:** [`ChatRepository`](data.md#chatrepository), [`ChatViewModel`](viewmodel.md#chatviewmodel), [`ConversationEntity`](data-local.md#conversationentity)

### ChatMessage

| Mục | Nội dung |
| --- | --- |
| Định danh | `com.nhom3.pubgapp.model.ChatMessage` |
| Loại | lớp |
| Kế thừa | [`BaseDocument`](model.md#basedocument) |
| Lưu trữ | Firestore `conversations/{id}/messages/{id}`, xem [firestore-schema.md](../../data/firestore-schema.md#messages) |
| Trách nhiệm | Tin nhắn chữ, ảnh, video hoặc một offer trong cuộc trò chuyện. |
| Chức năng liên quan (STT) | 43 đến 45 |

Kế thừa từ `BaseDocument`: `id`, `createdAt`, `updatedAt`, `getId()`, `touch()`.

**Thuộc tính**

| Tên | Kiểu | Truy cập | Ý nghĩa |
| --- | --- | --- | --- |
| `conversationId` | `String` | private | Cuộc trò chuyện chứa tin nhắn. |
| `senderId` | `String` | private | Người gửi. |
| `type` | `MessageType` | private | Loại tin nhắn. |
| `text` | `String` | private | Nội dung chữ khi type là TEXT. |
| `mediaUrl` | `String` | private | Đường dẫn ảnh hoặc video khi type là IMAGE hoặc VIDEO. |
| `offerId` | `String` | private | Offer được gửi khi type là OFFER. |

**Phương thức**

| Chữ ký | Mô tả | STT |
| --- | --- | --- |
| `public boolean isMedia()` | True khi type là IMAGE hoặc VIDEO. |  |

**Quan hệ trên sơ đồ lớp**

- `ChatMessage` kế thừa `BaseDocument`. Trang 3. Trao đổi, giao dịch.
- Hợp thành: một `Conversation` chứa * `ChatMessage` (vai trò `messages`); phần con không tồn tại khi không có cha. Trang 3. Trao đổi, giao dịch.
- Liên kết: `ChatMessage` [0..1] và `Offer` [0..1] (carries). Trang 3. Trao đổi, giao dịch.
- Phụ thuộc: `MessageEntity` dùng `ChatMessage`. Trang 6. Lưu trữ cục bộ (Room).

**Dùng tới:** [`MessageType`](enums.md#messagetype)

**Được dùng bởi:** [`ChatRepository`](data.md#chatrepository), [`ChatViewModel`](viewmodel.md#chatviewmodel), [`MessageEntity`](data-local.md#messageentity)

### Offer

| Mục | Nội dung |
| --- | --- |
| Định danh | `com.nhom3.pubgapp.model.Offer` |
| Loại | lớp |
| Kế thừa | [`BaseDocument`](model.md#basedocument) |
| Lưu trữ | Firestore `offers/{id}`, xem [firestore-schema.md](../../data/firestore-schema.md#offers) |
| Trách nhiệm | Đề nghị một chạm: tiền, vật trao đổi, hoặc cả hai. |
| Chức năng liên quan (STT) | 47 đến 49 |

Kế thừa từ `BaseDocument`: `id`, `createdAt`, `updatedAt`, `getId()`, `touch()`.

**Thuộc tính**

| Tên | Kiểu | Truy cập | Ý nghĩa |
| --- | --- | --- | --- |
| `listingId` | `String` | private | Tin được đề nghị. |
| `conversationId` | `String` | private | Cuộc trò chuyện chứa offer. |
| `buyerId` | `String` | private | Người đề nghị. |
| `sellerId` | `String` | private | Người nhận đề nghị. |
| `offerType` | `OfferType` | private | Loại đề nghị; phải nằm trong `Listing.acceptedOfferTypes`. |
| `amount` | `long` | private | Số tiền đề nghị, đơn vị đồng; 0 khi chỉ đổi vật. |
| `exchangeItem` | `String` | private | Mô tả món đồ đem đổi khi có vật. |
| `status` | `OfferStatus` | private | Trạng thái offer. |

**Phương thức**

| Chữ ký | Mô tả | STT |
| --- | --- | --- |
| `public void accept()` | PENDING sang ACCEPTED. Sau đó repository tạo `Deal`. | 47 đến 49 |
| `public void reject()` | PENDING sang REJECTED. | 47 đến 49 |

**Quan hệ trên sơ đồ lớp**

- `Offer` kế thừa `BaseDocument`. Trang 3. Trao đổi, giao dịch.
- Liên kết: `Offer` [*] và `Conversation` [1] (made in). Trang 3. Trao đổi, giao dịch.
- Liên kết: `ChatMessage` [0..1] và `Offer` [0..1] (carries). Trang 3. Trao đổi, giao dịch.
- Liên kết: `Offer` [0..1] và `Deal` [0..1] (becomes). Trang 3. Trao đổi, giao dịch.

**Dùng tới:** [`OfferStatus`](enums.md#offerstatus), [`OfferType`](enums.md#offertype)

**Được dùng bởi:** [`ChatRepository`](data.md#chatrepository), [`ChatViewModel`](viewmodel.md#chatviewmodel), [`DealRepository`](data.md#dealrepository), [`Listing`](model.md#listing)

### Deal

| Mục | Nội dung |
| --- | --- |
| Định danh | `com.nhom3.pubgapp.model.Deal` |
| Loại | lớp |
| Kế thừa | [`BaseDocument`](model.md#basedocument) |
| Lưu trữ | Firestore `deals/{id}`, xem [firestore-schema.md](../../data/firestore-schema.md#deals) |
| Trách nhiệm | Giao dịch giữa hai bên; kèm bằng chứng vận đơn, mốc đếm ngược tự giải ngân, bước Đã nhận trước Hoàn tất. |
| Chức năng liên quan (STT) | 50, 52 đến 56, 82, 84 |

Kế thừa từ `BaseDocument`: `id`, `createdAt`, `updatedAt`, `getId()`, `touch()`.

**Thuộc tính**

| Tên | Kiểu | Truy cập | Ý nghĩa |
| --- | --- | --- | --- |
| `listingId` | `String` | private | Tin của giao dịch. |
| `offerId` | `String` | private | Offer đã được chấp nhận để tạo giao dịch. |
| `buyerId` | `String` | private | Người mua hoặc người nhận đổi. |
| `sellerId` | `String` | private | Người bán hoặc người đăng tin. |
| `tradeType` | `TradeType` | private | Hình thức của giao dịch này. |
| `agreedPrice` | `long` | private | Số tiền thống nhất, đơn vị đồng; 0 thì không cần thanh toán. |
| `exchangeItem` | `String` | private | Món đồ đem đổi, nếu có. |
| `deliveryMethod` | `DeliveryMethod` | private | Gặp trực tiếp hoặc gửi qua đơn vị vận chuyển. |
| `deliveryAddress` | `String` | private | Địa chỉ giao hoặc điểm hẹn, dạng chữ. |
| `scheduledAt` | `Timestamp` | private | Thời gian hẹn giao nhận. |
| `agreedTerms` | `String` | private | Điều kiện khác hai bên thống nhất. |
| `itemTitle` | `String` | private | Bản chụp tiêu đề tin lúc tạo giao dịch, để lịch sử không đổi khi tin bị sửa hoặc gỡ. |
| `itemImageUrl` | `String` | private | Bản chụp ảnh đầu tiên của tin lúc tạo giao dịch. |
| `status` | `DealStatus` | private | Trạng thái giao dịch, xem `trang-thai.md`. |
| `buyerConfirmed` | `boolean` | private | Người mua đã xác nhận thông tin giao dịch. |
| `sellerConfirmed` | `boolean` | private | Người bán đã xác nhận thông tin giao dịch. |
| `cancelReason` | `String` | private | Lý do hủy, bắt buộc khi hủy. |
| `carrierName` | `String` | private | Tên đơn vị vận chuyển. |
| `trackingCode` | `String` | private | Mã vận đơn. |
| `trackingImageUrl` | `String` | private | Ảnh chụp vận đơn trên Cloud Storage. |
| `shippedAt` | `Timestamp` | private | Lúc người bán xác nhận đã giao. |
| `autoReleaseAt` | `Timestamp` | private | Hạn đếm ngược: `shippedAt + AUTO_RELEASE_DAYS`. Hết hạn mà người mua chưa xác nhận thì tự sang RECEIVED. |
| `receivedAt` | `Timestamp` | private | Lúc sang RECEIVED (người mua xác nhận hoặc hết đếm ngược); tiền được giải ngân lúc này. |
| `AUTO_RELEASE_DAYS` | `int` | private, static final | Hằng số: 7 ngày đếm ngược kể từ lúc giao. |
| `AUTO_COMPLETE_DAYS` | `int` | private, static final | Hằng số: 3 ngày kể từ `receivedAt` thì tự hoàn tất. |
| `statusHistory` | `List<StatusChange>` | private | Lịch sử chuyển trạng thái, thêm một phần tử mỗi lần chuyển. |

**Phương thức**

| Chữ ký | Mô tả | STT |
| --- | --- | --- |
| `public boolean canMoveTo(DealStatus)` | True khi bảng chuyển trạng thái trong `trang-thai.md` cho phép đi từ trạng thái hiện tại sang trạng thái đích. | 52 |
| `public void confirmBy(String userId)` | Đặt cờ xác nhận của bên gọi; khi cả hai bên đã xác nhận thì PENDING_CONFIRM sang CONFIRMED. | 52 |
| `public void markShipped(String carrier, String trackingCode, String imageUrl)` | Ghi đơn vị vận chuyển, mã và ảnh vận đơn, `shippedAt`, `autoReleaseAt`; CONFIRMED sang SHIPPED. Từ chối nếu thiếu bằng chứng hoặc còn cần thanh toán mà chưa có tiền ký quỹ. | 54 |
| `public boolean hasShippingProof()` | True khi đủ cả `carrierName`, `trackingCode`, `trackingImageUrl`. | 54 |
| `public void markReceived()` | Người mua xác nhận: SHIPPED sang RECEIVED (hoặc CONFIRMED sang RECEIVED khi gặp trực tiếp), ghi `receivedAt`. | 53 |
| `public boolean isAutoReleaseDue(Timestamp)` | True khi đang SHIPPED và thời điểm truyền vào đã qua `autoReleaseAt`. | 84 |
| `public void autoReceive()` | Hệ thống tự chuyển SHIPPED sang RECEIVED khi hết đếm ngược, người đổi ghi là `system`. | 84 |
| `public boolean isAutoCompleteDue(Timestamp)` | True khi đang RECEIVED và đã qua `receivedAt + AUTO_COMPLETE_DAYS`. | 53 |
| `public void complete()` | RECEIVED sang COMPLETED. | 53 |
| `public void cancel(String userId, String reason)` | PENDING_CONFIRM hoặc CONFIRMED sang CANCELLED, bắt buộc có lý do. | 55 |
| `public boolean isParticipant(String userId)` | True khi người đó là người mua hoặc người bán. |  |
| `public boolean needsPayment()` | True khi `agreedPrice` lớn hơn 0. | 56 |

**Quan hệ trên sơ đồ lớp**

- `Deal` kế thừa `BaseDocument`. Trang 3. Trao đổi, giao dịch.
- Liên kết: `Offer` [0..1] và `Deal` [0..1] (becomes). Trang 3. Trao đổi, giao dịch.
- Liên kết: `Deal` [*] và `Listing` [1] (for). Trang 3. Trao đổi, giao dịch.
- Liên kết: `Deal` [*] và `User` [2] (buyer, seller). Trang 3. Trao đổi, giao dịch.
- Hợp thành: một `Deal` chứa * `StatusChange` (vai trò `statusHistory`); phần con không tồn tại khi không có cha. Trang 3. Trao đổi, giao dịch.
- Hợp thành: một `Deal` chứa 0..1 `Payment` (vai trò `escrow`); phần con không tồn tại khi không có cha. Trang 3. Trao đổi, giao dịch.
- Liên kết: `Complaint` [*] và `Deal` [1] (about). Trang 4. Tin cậy, quản trị.
- Liên kết: `Review` [0..2] và `Deal` [1] (about). Trang 4. Tin cậy, quản trị.
- Phụ thuộc: `DealEntity` dùng `Deal`. Trang 6. Lưu trữ cục bộ (Room).

**Dùng tới:** [`DealStatus`](enums.md#dealstatus), [`DeliveryMethod`](enums.md#deliverymethod), [`StatusChange`](model.md#statuschange), [`TradeType`](enums.md#tradetype)

**Được dùng bởi:** [`AdminDealViewModel`](viewmodel.md#admindealviewmodel), [`DealEntity`](data-local.md#dealentity), [`DealRepository`](data.md#dealrepository), [`DealViewModel`](viewmodel.md#dealviewmodel)

### StatusChange

| Mục | Nội dung |
| --- | --- |
| Định danh | `com.nhom3.pubgapp.model.StatusChange` |
| Loại | lớp (value object) |
| Lưu trữ | mảng lồng `deals/{id}.statusHistory[ ]`, xem [firestore-schema.md](../../data/firestore-schema.md#statushistory) |
| Trách nhiệm | Một dòng lịch sử trạng thái của giao dịch (statusHistory), nằm trong Deal. |
| Chức năng liên quan (STT) | 82 |

**Thuộc tính**

| Tên | Kiểu | Truy cập | Ý nghĩa |
| --- | --- | --- | --- |
| `fromStatus` | `DealStatus` | private | Trạng thái trước; rỗng ở phần tử đầu tiên. |
| `toStatus` | `DealStatus` | private | Trạng thái sau. |
| `changedBy` | `String` | private | Người làm thay đổi; `system` khi do việc chạy nền. |
| `reason` | `String` | private | Lý do, nếu có (hủy, khiếu nại). |
| `changedAt` | `Timestamp` | private | Lúc thay đổi. |

**Phương thức**

| Chữ ký | Mô tả | STT |
| --- | --- | --- |
| `public String describe()` | Câu mô tả tiếng Việt cho dòng lịch sử, ví dụ 'Người bán xác nhận đã giao'. |  |

**Quan hệ trên sơ đồ lớp**

- Hợp thành: một `Deal` chứa * `StatusChange` (vai trò `statusHistory`); phần con không tồn tại khi không có cha. Trang 3. Trao đổi, giao dịch.

**Dùng tới:** [`DealStatus`](enums.md#dealstatus)

**Được dùng bởi:** [`Deal`](model.md#deal)

### Payment

| Mục | Nội dung |
| --- | --- |
| Định danh | `com.nhom3.pubgapp.model.Payment` |
| Loại | lớp |
| Kế thừa | [`BaseDocument`](model.md#basedocument) |
| Lưu trữ | Firestore `payments/{dealId}`, xem [firestore-schema.md](../../data/firestore-schema.md#payments) |
| Trách nhiệm | Khoản tiền ký quỹ mô phỏng: giữ, chuyển cho người bán, hoặc hoàn lại. |
| Chức năng liên quan (STT) | 53, 55, 56, 84 |

Kế thừa từ `BaseDocument`: `id`, `createdAt`, `updatedAt`, `getId()`, `touch()`.

**Thuộc tính**

| Tên | Kiểu | Truy cập | Ý nghĩa |
| --- | --- | --- | --- |
| `dealId` | `String` | private | Giao dịch; cũng là mã tài liệu nên mỗi giao dịch có tối đa một khoản ký quỹ. |
| `payerId` | `String` | private | Người trả tiền (người mua). |
| `payeeId` | `String` | private | Người nhận tiền khi giải ngân (người bán). |
| `amount` | `long` | private | Số tiền, bằng `Deal.agreedPrice`. |
| `status` | `PaymentStatus` | private | Trạng thái khoản ký quỹ. |
| `heldAt` | `Timestamp` | private | Lúc người mua thanh toán và tiền bắt đầu được giữ. |
| `releasedAt` | `Timestamp` | private | Lúc giải ngân hoặc hoàn tiền. |

**Phương thức**

| Chữ ký | Mô tả | STT |
| --- | --- | --- |
| `public void release()` | HELD sang RELEASED, ghi `releasedAt`. | 53, 84 |
| `public void refund()` | HELD sang REFUNDED, ghi `releasedAt`. | 55 |
| `public boolean isHeld()` | True khi đang HELD. |  |

**Quan hệ trên sơ đồ lớp**

- `Payment` kế thừa `BaseDocument`. Trang 3. Trao đổi, giao dịch.
- Hợp thành: một `Deal` chứa 0..1 `Payment` (vai trò `escrow`); phần con không tồn tại khi không có cha. Trang 3. Trao đổi, giao dịch.

**Dùng tới:** [`PaymentStatus`](enums.md#paymentstatus)

**Được dùng bởi:** [`DealRepository`](data.md#dealrepository), [`DealViewModel`](viewmodel.md#dealviewmodel)

### Review

| Mục | Nội dung |
| --- | --- |
| Định danh | `com.nhom3.pubgapp.model.Review` |
| Loại | lớp |
| Kế thừa | [`BaseDocument`](model.md#basedocument) |
| Lưu trữ | Firestore `reviews/{id}`, xem [firestore-schema.md](../../data/firestore-schema.md#reviews) |
| Trách nhiệm | Đánh giá đối tác sau một giao dịch: sao tổng thể và 3 tiêu chí; mỗi bên đánh giá một lần. |
| Chức năng liên quan (STT) | 59 |

Kế thừa từ `BaseDocument`: `id`, `createdAt`, `updatedAt`, `getId()`, `touch()`.

**Thuộc tính**

| Tên | Kiểu | Truy cập | Ý nghĩa |
| --- | --- | --- | --- |
| `dealId` | `String` | private | Giao dịch được đánh giá. |
| `reviewerId` | `String` | private | Người viết đánh giá. |
| `revieweeId` | `String` | private | Người được đánh giá. |
| `rating` | `int` | private | Sao tổng thể, 1 đến 5. |
| `accuracyRating` | `int` | private | Sao cho độ chính xác thông tin, 1 đến 5. |
| `attitudeRating` | `int` | private | Sao cho thái độ giao dịch, 1 đến 5. |
| `commitmentRating` | `int` | private | Sao cho đúng cam kết, 1 đến 5. |
| `comment` | `String` | private | Nhận xét bằng chữ, có thể rỗng. |

**Phương thức**

| Chữ ký | Mô tả | STT |
| --- | --- | --- |
| `public boolean isValidRating()` | True khi cả 4 điểm sao nằm trong 1 đến 5. | 59 |

**Quan hệ trên sơ đồ lớp**

- `Review` kế thừa `BaseDocument`. Trang 4. Tin cậy, quản trị.
- Liên kết: `Review` [*] và `User` [2] (reviewer, reviewee). Trang 4. Tin cậy, quản trị.
- Liên kết: `Review` [0..2] và `Deal` [1] (about). Trang 4. Tin cậy, quản trị.

**Được dùng bởi:** [`ReviewRepository`](data.md#reviewrepository), [`TrustViewModel`](viewmodel.md#trustviewmodel)

### Report

| Mục | Nội dung |
| --- | --- |
| Định danh | `com.nhom3.pubgapp.model.Report` |
| Loại | lớp |
| Kế thừa | [`BaseDocument`](model.md#basedocument) |
| Lưu trữ | Firestore `reports/{id}`, xem [firestore-schema.md](../../data/firestore-schema.md#reports) |
| Trách nhiệm | Báo cáo hành vi, phát ngôn hoặc tin đăng vi phạm. |
| Chức năng liên quan (STT) | 61, 62, 75 |

Kế thừa từ `BaseDocument`: `id`, `createdAt`, `updatedAt`, `getId()`, `touch()`.

**Thuộc tính**

| Tên | Kiểu | Truy cập | Ý nghĩa |
| --- | --- | --- | --- |
| `reporterId` | `String` | private | Người báo cáo. |
| `targetType` | `ReportTarget` | private | Loại đối tượng bị báo cáo. |
| `targetId` | `String` | private | Mã tin, người dùng hoặc tin nhắn bị báo cáo, tùy `targetType`. |
| `reason` | `ReportReason` | private | Lý do chọn sẵn. |
| `description` | `String` | private | Mô tả thêm của người báo cáo. |
| `status` | `ReportStatus` | private | Trạng thái báo cáo. |
| `handledBy` | `String` | private | Admin đã xử lý. |

**Phương thức**

| Chữ ký | Mô tả | STT |
| --- | --- | --- |
| `public void cancel()` | OPEN sang CANCELLED; chỉ người báo cáo được hủy. | 62 |
| `public void resolve(String adminId)` | OPEN sang RESOLVED: admin xác nhận vi phạm và đã xử lý. | 75 |
| `public void dismiss(String adminId)` | OPEN sang DISMISSED: admin thấy không vi phạm. | 75 |

**Quan hệ trên sơ đồ lớp**

- `Report` kế thừa `BaseDocument`. Trang 4. Tin cậy, quản trị.
- Liên kết: `Report` [*] và `User` [1] (reporter). Trang 4. Tin cậy, quản trị.
- Phụ thuộc: `Report` dùng `Listing` (target: Listing, User, ChatMessage). Trang 4. Tin cậy, quản trị.

**Dùng tới:** [`ReportReason`](enums.md#reportreason), [`ReportStatus`](enums.md#reportstatus), [`ReportTarget`](enums.md#reporttarget)

**Được dùng bởi:** [`ReportRepository`](data.md#reportrepository), [`TrustViewModel`](viewmodel.md#trustviewmodel)

### Complaint

| Mục | Nội dung |
| --- | --- |
| Định danh | `com.nhom3.pubgapp.model.Complaint` |
| Loại | lớp |
| Kế thừa | [`BaseDocument`](model.md#basedocument) |
| Lưu trữ | Firestore `complaints/{id}`, xem [firestore-schema.md](../../data/firestore-schema.md#complaints) |
| Trách nhiệm | Đơn khiếu nại giao dịch kèm bằng chứng; adminNote là ghi chú nội bộ của admin. |
| Chức năng liên quan (STT) | 64, 66, 87, 89, 90 |

Kế thừa từ `BaseDocument`: `id`, `createdAt`, `updatedAt`, `getId()`, `touch()`.

**Thuộc tính**

| Tên | Kiểu | Truy cập | Ý nghĩa |
| --- | --- | --- | --- |
| `complainantId` | `String` | private | Người khiếu nại, là một bên của giao dịch. |
| `dealId` | `String` | private | Giao dịch bị khiếu nại. |
| `reason` | `ComplaintReason` | private | Lý do chọn sẵn. |
| `description` | `String` | private | Mô tả của người khiếu nại. |
| `evidenceUrls` | `List<String>` | private | Ảnh bằng chứng trên Cloud Storage, tối thiểu 1. |
| `status` | `ComplaintStatus` | private | Trạng thái khiếu nại. |
| `priority` | `Priority` | private | Mức ưu tiên do admin đặt; mặc định MEDIUM. |
| `decision` | `ComplaintDecision` | private | Quyết định của admin; rỗng khi chưa xử lý. |
| `resolutionMessage` | `String` | private | Lời giải thích kết quả gửi cho hai bên. |
| `adminNote` | `String` | private | Ghi chú nội bộ. Trên Firestore nằm ở tài liệu con `complaints/{id}/adminNote/note`, không nằm trong tài liệu khiếu nại. |
| `handledBy` | `String` | private | Admin đã xử lý. |

**Phương thức**

| Chữ ký | Mô tả | STT |
| --- | --- | --- |
| `public void withdraw()` | Người khiếu nại rút đơn khi còn OPEN hoặc NEEDS_EVIDENCE: sang WITHDRAWN. | 66 |
| `public void decide(ComplaintDecision, String adminId)` | Ghi quyết định và admin. REQUEST_EVIDENCE thì sang NEEDS_EVIDENCE, các quyết định khác sang RESOLVED. | 89 |
| `public boolean isOpen()` | True khi OPEN hoặc NEEDS_EVIDENCE. |  |

**Quan hệ trên sơ đồ lớp**

- `Complaint` kế thừa `BaseDocument`. Trang 4. Tin cậy, quản trị.
- Liên kết: `Complaint` [*] và `Deal` [1] (about). Trang 4. Tin cậy, quản trị.
- Liên kết: `Complaint` [*] và `User` [1] (complainant). Trang 4. Tin cậy, quản trị.

**Dùng tới:** [`ComplaintDecision`](enums.md#complaintdecision), [`ComplaintReason`](enums.md#complaintreason), [`ComplaintStatus`](enums.md#complaintstatus), [`Priority`](enums.md#priority)

**Được dùng bởi:** [`AdminComplaintViewModel`](viewmodel.md#admincomplaintviewmodel), [`ComplaintRepository`](data.md#complaintrepository), [`TrustViewModel`](viewmodel.md#trustviewmodel)

### ActivityLog

| Mục | Nội dung |
| --- | --- |
| Định danh | `com.nhom3.pubgapp.model.ActivityLog` |
| Loại | lớp |
| Kế thừa | [`BaseDocument`](model.md#basedocument) |
| Lưu trữ | Firestore `activityLogs/{id}`, xem [firestore-schema.md](../../data/firestore-schema.md#activitylogs) |
| Trách nhiệm | Nhật ký hành động quan trọng của một tài khoản để admin theo dõi. |
| Chức năng liên quan (STT) | 74 |

Kế thừa từ `BaseDocument`: `id`, `createdAt`, `updatedAt`, `getId()`, `touch()`.

**Thuộc tính**

| Tên | Kiểu | Truy cập | Ý nghĩa |
| --- | --- | --- | --- |
| `userId` | `String` | private | Tài khoản thực hiện hành động. |
| `action` | `String` | private | Tên hành động, ví dụ `LISTING_CREATED`, `DEAL_CANCELLED`. |
| `targetType` | `String` | private | Loại đối tượng bị tác động. |
| `targetId` | `String` | private | Mã đối tượng bị tác động. |

**Quan hệ trên sơ đồ lớp**

- `ActivityLog` kế thừa `BaseDocument`. Trang 4. Tin cậy, quản trị.
- Liên kết: `ActivityLog` [*] và `User` [1] (performed by). Trang 4. Tin cậy, quản trị.

**Được dùng bởi:** [`AdminRepository`](data.md#adminrepository), [`AdminUserViewModel`](viewmodel.md#adminuserviewmodel)

### BannedKeyword

| Mục | Nội dung |
| --- | --- |
| Định danh | `com.nhom3.pubgapp.model.BannedKeyword` |
| Loại | lớp |
| Kế thừa | [`BaseDocument`](model.md#basedocument) |
| Lưu trữ | Firestore `bannedKeywords/{id}`, xem [firestore-schema.md](../../data/firestore-schema.md#bannedkeywords) |
| Trách nhiệm | Một từ khóa cấm; tin đăng chứa nó bị gắn cờ cần chú ý. |
| Chức năng liên quan (STT) | 78 |

Kế thừa từ `BaseDocument`: `id`, `createdAt`, `updatedAt`, `getId()`, `touch()`.

**Thuộc tính**

| Tên | Kiểu | Truy cập | Ý nghĩa |
| --- | --- | --- | --- |
| `keyword` | `String` | private | Từ khóa cấm, lưu dạng đã bỏ dấu và viết thường. |
| `createdBy` | `String` | private | Admin đã thêm. |

**Phương thức**

| Chữ ký | Mô tả | STT |
| --- | --- | --- |
| `public boolean matches(String)` | True khi chuỗi (đã bỏ dấu, viết thường) chứa từ khóa. |  |

**Quan hệ trên sơ đồ lớp**

- `BannedKeyword` kế thừa `BaseDocument`. Trang 4. Tin cậy, quản trị.

**Được dùng bởi:** [`AdminContentViewModel`](viewmodel.md#admincontentviewmodel), [`AdminRepository`](data.md#adminrepository), [`KeywordFilter`](util.md#keywordfilter)
