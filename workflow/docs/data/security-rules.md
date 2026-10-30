# Ma trận Security Rules

Đặc tả quyền đọc và ghi của Cloud Firestore cho PUBGApp, theo từng collection của [firestore-schema.md](firestore-schema.md) (23 collection và mảng lồng `deals.statusHistory`). Đây là bản **đặc tả để viết file `firebase/firestore.rules`**, không phải file luật. Chuyển trạng thái theo [trang-thai.md](../architecture/trang-thai.md); luồng ghi theo [luong-xu-ly.md](../architecture/luong-xu-ly.md).

Vì sao cần bản này: ứng dụng không có máy chủ riêng (gói Spark, không Cloud Functions), nên Security Rules là chỗ **duy nhất** chặn dữ liệu bị ghi sai. Mọi giá trị mà ứng dụng gửi lên đều có thể bị người dùng làm giả; điều gì quan trọng (tiền ký quỹ, thời hạn đếm ngược, vai trò admin, điểm đánh giá, khóa tài khoản) phải được Rules kiểm lại ở đây (`AGENTS.md`, luật R12).

Chỗ nào chưa chắc chắn về cú pháp hoặc giới hạn của Firestore Rules thì ghi "(kiểm lại)": cần đối chiếu tài liệu chính thức của Firebase hoặc thử trên Rules Playground trước khi chép vào file luật.

## Quy ước chung

Các hàm mô tả dưới đây dùng lặp lại ở mọi bảng:

| Tên | Nghĩa |
| --- | --- |
| `signedIn()` | `request.auth != null` |
| `me` | `request.auth.uid` |
| `isAdmin()` | Tồn tại tài liệu `admins/{me}` (`exists`) |
| `writerActive()` | `get(users/{me}).data.status == 'ACTIVE'` |
| `verified()` | `get(users/{me}).data.verificationStatus == 'APPROVED'`: tài khoản đã được admin duyệt xác thực (quyết định 2026-10-22) |
| `onlyKeys([...])` | `request.resource.data.diff(resource.data).affectedKeys().hasOnly([...])`: lần sửa chỉ được đụng các trường liệt kê |
| `now` | `request.time`, giờ của máy chủ Firestore |
| `serverStamp(f)` | Trường `f` trong dữ liệu mới bằng `request.time`, tức ứng dụng đã ghi `FieldValue.serverTimestamp()` |
| `after(path)` | `getAfter(path)`: tài liệu như sẽ có **sau** lô ghi đang chạy; dùng để ràng buộc các tài liệu ghi cùng lô |
| `before(path)` | `get(path)`: tài liệu **trước** lô ghi |

Luật chung:

1. **Mọi lần tạo, sửa, xóa đòi `writerActive()`**, nghĩa là tài liệu `users/{me}` đang ACTIVE. Đọc chỉ đòi `signedIn()` trừ khi bảng ghi khác. Người đã bị khóa trong lúc dùng ứng dụng vì vậy không ghi được nữa. `get()` trong Rules đọc trạng thái **trước** lô, nên các lệnh ghi cùng lô với việc người dùng tự đặt DELETED vẫn qua.
2. **Ngoại lệ duy nhất** cho luật 1: tự mở khóa khi hết hạn khóa tạm (bảng `users`, dòng "Hết hạn khóa").
3. Hai trường thời gian `createdAt` và `updatedAt` của mọi tài liệu phải bằng `request.time` ở lần ghi tương ứng (ghi tạo: cả hai; ghi sửa: `updatedAt`); `createdAt` không bao giờ đổi sau khi tạo.
4. Tài liệu có mã cố định phải khớp: `users/{uid}` với `uid` của người ghi; `follows/{followerId_followedId}`; `conversations/{listingId_buyerId}`; `payments/{dealId}`; `reviews/{dealId_reviewerId}`; `complaints/{dealId}`; `sellerVerifications/{userId}`.
5. Chuỗi có giới hạn độ dài (`size()`) để chặn ghi tài liệu khổng lồ; các con số ở bảng chỉ là mức đề xuất, chốt khi code.
6. Hằng thời gian viết `duration.value(N, 'd')` (kiểm lại cú pháp).
7. Mỗi yêu cầu có giới hạn số lần `get()`/`exists()`/`getAfter()` (khoảng 10 lần cho một yêu cầu đơn, 20 cho một lô; kiểm lại). Vì vậy lô ghi phải gọn: hàm `writerActive()` chỉ đếm một lần khi cùng đường dẫn (kiểm lại), và mỗi lệnh ghi trong lô được đánh giá riêng.
8. Không có dòng nào cho phép `list` hay `get` rộng hơn bảng; mặc định mọi đường dẫn không nêu là **từ chối**. Rules không phải bộ lọc: truy vấn của ứng dụng phải tự mang đủ điều kiện (ví dụ `status in [AVAILABLE, RESERVED, SOLD]`), không thì cả truy vấn bị từ chối.
9. **Khách chưa đăng nhập** đọc được đúng ba thứ (quyết định 2026-10-22): tin đăng đang mở (`listings` với `status` thuộc AVAILABLE, RESERVED, SOLD), danh mục (`categories`) và ảnh (`images`). Mọi collection khác vẫn đòi `signedIn()`, kể cả `users`: vì vậy màn chi tiết tin đăng ở chế độ khách không hiện khối thông tin người bán.
10. **Xác thực tài khoản là điều kiện ghi** (quyết định 2026-10-22): mọi lệnh tạo tin đăng đã đăng, tạo offer và tạo giao dịch đòi thêm `verified()`. Xem, tìm kiếm, nhắn tin, lưu tin và theo dõi không đòi.

## Mục lục

| # | Collection | Đường dẫn |
| ---: | --- | --- |
| 1 | [users](#1-users) | `users/{uid}` |
| 2 | [addresses](#2-addresses) | `users/{uid}/addresses/{id}` |
| 3 | [identity](#3-identity) | `users/{uid}/private/identity` |
| 4 | [savedListings](#4-savedlistings) | `users/{uid}/saved/{listingId}` |
| 5 | [follows](#5-follows) | `follows/{followerId_followedId}` |
| 6 | [sellerVerifications](#6-sellerverifications) | `sellerVerifications/{userId}` |
| 7 | [warnings](#7-warnings) | `users/{uid}/warnings/{id}` |
| 8 | [admins](#8-admins) | `admins/{uid}` |
| 9 | [categories](#9-categories) | `categories/{id}` |
| 10 | [listings](#10-listings) | `listings/{id}` |
| 11 | [conversations](#11-conversations) | `conversations/{listingId_buyerId}` |
| 12 | [messages](#12-messages) | `conversations/{id}/messages/{id}` |
| 13 | [offers](#13-offers) | `offers/{id}` |
| 14 | [deals](#14-deals) | `deals/{id}` |
| 15 | [statusHistory](#15-statushistory) | mảng `deals/{id}.statusHistory` |
| 16 | [payments](#16-payments) | `payments/{dealId}` |
| 17 | [reviews](#17-reviews) | `reviews/{dealId_reviewerId}` |
| 18 | [reports](#18-reports) | `reports/{id}` |
| 19 | [complaints](#19-complaints) | `complaints/{dealId}` |
| 20 | [adminNote](#20-adminnote) | `complaints/{id}/adminNote/note` |
| 21 | [notifications](#21-notifications) | `notifications/{id}` |
| 22 | [activityLogs](#22-activitylogs) | `activityLogs/{id}` |
| 23 | [bannedKeywords](#23-bannedkeywords) | `bannedKeywords/{id}` |
| 24 | [images](#24-images) | `images/{id}` |

## 1. users

Đường dẫn `users/{uid}`. Hồ sơ công khai cho mọi người đã đăng nhập (chấp nhận rủi ro lộ `email`, `phone`, `warningCount`, `lockedUntil`, `lockReason`, xem overview.md mục 11).

| Thao tác | Ai | Điều kiện | Lý do |
| --- | --- | --- | --- |
| Đọc | Người đã đăng nhập | `signedIn()` | Hiện tên, ảnh, điểm của người bán và đối tác; truy vấn `count()` |
| Tạo | Chính chủ | `me == uid`; `writerActive()` **không** áp dụng vì tài liệu chưa có (thay bằng `me == uid`); khóa chỉ gồm các trường hồ sơ trong `email, displayName, phone, avatarBase64, bio, province, district, interestedCategoryIds, termsAcceptedAt, notificationPrefs, lastActiveAt, lockedUntil, lockReason, status, verificationStatus, ratingSum, ratingCount, warningCount, searchKeywords, createdAt, updatedAt`; **giá trị khởi tạo cố định:** `status == 'ACTIVE'`, `verificationStatus == 'NONE'`, `ratingSum == 0`, `ratingCount == 0`, `warningCount == 0`, `lockedUntil == null`, `lockReason == ''`; `email == request.auth.token.email`; `avatarBase64.size()` dưới mức 20 KB sau mã hóa (khoảng 28.000 ký tự, kiểm lại); `serverStamp(createdAt)` | Chặn tự tạo hồ sơ với điểm 5 sao, đã xác thực hoặc số cảnh báo giả |
| Sửa hồ sơ | Chính chủ | `me == uid`; `writerActive()`; `onlyKeys([displayName, phone, avatarBase64, bio, province, district, interestedCategoryIds, notificationPrefs, lastActiveAt, searchKeywords, updatedAt])`; `avatarBase64.size()` như trên; `searchKeywords` là list và `searchKeywords.size()` tối đa 20 | Người dùng chỉ sửa phần hồ sơ của mình. Rules không kiểm được `searchKeywords` có đúng là các từ của `displayName`, chỉ chặn nhồi quá 20 từ; xem Giới hạn |
| Xin xác thực | Chính chủ | `me == uid`; `writerActive()`; `onlyKeys([verificationStatus, updatedAt])`; trước `NONE` hoặc `REJECTED`, sau `PENDING`; `after(sellerVerifications/uid).status == 'PENDING'` | Hai trường xác thực luôn cùng giá trị, ghi cùng lô |
| Hết hạn khóa | Chính chủ | `me == uid`; **không** đòi `writerActive()`; trước `TEMP_LOCKED` và `now > resource.data.lockedUntil`; sau `ACTIVE`; `onlyKeys([status, lockedUntil, lockReason, updatedAt])`; `lockedUntil == null`, `lockReason == ''` | Cho người bị khóa tạm tự vào lại khi đã hết hạn; mốc so với giờ Firestore nên chỉnh giờ máy không giúp được |
| Tự vô hiệu hóa | Chính chủ | `me == uid`; `writerActive()`; trước `ACTIVE`, sau `DELETED`; `onlyKeys([status, updatedAt])` | DELETED là cuối; việc "không còn giao dịch chưa kết thúc" kiểm ở ứng dụng (xem Giới hạn) |
| Khóa, mở khóa, cảnh báo | Admin | `isAdmin()`; `writerActive()`; `onlyKeys([status, lockedUntil, lockReason, warningCount, updatedAt])`; `status` sau là ACTIVE, TEMP_LOCKED hoặc PERM_LOCKED (không DELETED); TEMP_LOCKED thì `lockedUntil > now`; PERM_LOCKED thì `lockedUntil == null`; có `lockReason` khác rỗng khi khóa; nếu `warningCount` đổi thì `warningCount == resource.data.warningCount + 1` và `after(users/uid/warnings/{wid}).level == warningCount` | Khóa theo bậc; số cảnh báo chỉ tăng 1 kèm một tài liệu `Warning` |
| Duyệt xác thực | Admin | `isAdmin()`; `writerActive()`; `onlyKeys([verificationStatus, updatedAt])`; trước `PENDING`, sau `APPROVED` hoặc `REJECTED`; `after(sellerVerifications/uid).status == verificationStatus sau` | Hai tài liệu cùng giá trị |
| Ghi điểm đánh giá | Người đánh giá (bất kỳ người đã đăng nhập khác `uid`) | `writerActive()`; `me != uid`; `onlyKeys([ratingSum, ratingCount, lastReviewId, updatedAt])`; `ratingCount == resource.data.ratingCount + 1`; `lastReviewId` khác `resource.data.lastReviewId`; gọi `rv = after(reviews/$(request.resource.data.lastReviewId))`: `rv.data.revieweeId == uid`, `rv.data.reviewerId == me`, `ratingSum == resource.data.ratingSum + rv.data.rating`; `serverStamp(updatedAt)` | Điểm chỉ cộng đúng bằng điểm của đánh giá ghi cùng lô. Rules chỉ `getAfter` được đường dẫn biết trước, mà tài liệu `users` không biết `dealId`, nên lô ghi phải tự khai mã đánh giá qua `lastReviewId`. Điều kiện `lastReviewId` phải đổi là thứ chặn cộng điểm hai lần bằng cùng một đánh giá |
| Đổi cờ khác | Bất kỳ ai khác ngoài các dòng trên | Từ chối | Mọi trường còn lại (`status`, `warningCount`...) không do chính chủ ghi |
| Xóa | Không ai | Từ chối | DELETED thay cho xóa hẳn; lịch sử giữ cho đối tác |

## 2. addresses

Đường dẫn `users/{uid}/addresses/{id}`. Địa chỉ giao nhận riêng tư.

| Thao tác | Ai | Điều kiện | Lý do |
| --- | --- | --- | --- |
| Đọc | Chính chủ | `me == uid` | Địa chỉ không công khai; địa chỉ giao hàng được chép dạng chữ vào `deals.deliveryAddress` |
| Tạo, sửa | Chính chủ | `me == uid`; `writerActive()`; `userId == uid`; chuỗi có `size()` giới hạn | Mỗi người tự quản địa chỉ của mình |
| Xóa | Chính chủ | `me == uid`; `writerActive()` | |

Rules không đảm bảo "mỗi người một địa chỉ mặc định"; ứng dụng giữ (xem Giới hạn).

## 3. identity

Đường dẫn `users/{uid}/private/identity`. Số CCCD và thông tin định danh.

| Thao tác | Ai | Điều kiện | Lý do |
| --- | --- | --- | --- |
| Đọc | Chính chủ hoặc admin | `me == uid || isAdmin()` | Quyết định 2026-10-11: chỉ chủ tài khoản và admin xem được; màn E03 của admin đọc khi duyệt xác thực tài khoản |
| Tạo | Chính chủ | `me == uid`; `writerActive()` (khi đăng ký lần đầu, hồ sơ `users/{uid}` đã có trong cùng lô hoặc `after(users/uid).status == 'ACTIVE'`); `userId == uid`; `nationalId.matches('[0-9]{12}')` (kiểm lại cú pháp regex); `gender` thuộc MALE, FEMALE, OTHER | CCCD đủ 12 chữ số |
| Sửa | Chính chủ | `me == uid`; `writerActive()`; cùng ràng buộc dữ liệu như tạo; `userId` không đổi | |
| Xóa | Không ai | Từ chối | Giữ cho đối tác sau khi xóa tài khoản |

Admin **không** ghi được tài liệu này. (`firestore-schema.md` mục identity hiện ghi "đọc, ghi" cho admin: cần sửa thành chỉ đọc nếu chấp nhận ma trận này.)

## 4. savedListings

Đường dẫn `users/{uid}/saved/{listingId}`.

| Thao tác | Ai | Điều kiện | Lý do |
| --- | --- | --- | --- |
| Đọc | Chính chủ | `me == uid` | Danh sách yêu thích riêng tư |
| Tạo | Chính chủ | `me == uid`; `writerActive()`; `userId == uid`; `listingId == mã trong đường dẫn`; `serverStamp(savedAt)` | |
| Sửa | Không ai | Từ chối | Tài liệu chỉ có hai trạng thái: có và không có |
| Xóa | Chính chủ | `me == uid`; `writerActive()` | Bỏ lưu |

## 5. follows

Đường dẫn `follows/{followerId_followedId}`.

| Thao tác | Ai | Điều kiện | Lý do |
| --- | --- | --- | --- |
| Đọc | Người đã đăng nhập | `signedIn()` | `count()` số người theo dõi cho hồ sơ người khác; truy vấn `followerId == me` cho kênh theo dõi (truy vấn tổng hợp cần được đọc các tài liệu khớp) |
| Tạo | Người theo dõi | `writerActive()`; mã tài liệu bằng `followerId + '_' + followedId`; `followerId == me`; `followedId != me`; `serverStamp(createdAt)` | Mã cố định nên mỗi cặp một tài liệu |
| Sửa | Không ai | Từ chối | |
| Xóa | Người theo dõi | `writerActive()`; `resource.data.followerId == me` | Bỏ theo dõi |

## 6. sellerVerifications

Đường dẫn `sellerVerifications/{userId}`.

| Thao tác | Ai | Điều kiện | Lý do |
| --- | --- | --- | --- |
| Đọc | Chính chủ hoặc admin | `me == userId || isAdmin()` | Người gửi xem trạng thái; admin duyệt |
| Gửi lần đầu | Chính chủ | `me == userId`; `writerActive()`; `request.auth.token.email_verified == true` (kiểm lại thời điểm token được làm mới); `status == 'PENDING'`; `reviewedBy == null`; `resubmitCount == 0`; `nationalId.matches('[0-9]{12}')` (kiểm lại cú pháp regex); `imageIds.size() == 2`; `after(users/userId).verificationStatus == 'PENDING'` | Chỉ người đã xác thực email xin xác thực tài khoản. Hai ảnh là ảnh giấy tờ và ảnh chân dung cầm giấy tờ; Rules không kiểm được `purpose` của từng ảnh, xem Giới hạn |
| Gửi lại | Chính chủ | Như trên; trước `REJECTED`, sau `PENDING`; `onlyKeys([nationalId, imageIds, status, note, resubmitCount, reviewedBy, reviewedAt, updatedAt])` với `note` rỗng, `reviewedBy == null`; `resubmitCount == resource.data.resubmitCount + 1` và `resubmitCount <= 3` | Sau khi bị từ chối được sửa và gửi lại, tối đa 3 lần (`SellerVerification.MAX_RESUBMIT`); hết lượt thì phải liên hệ admin |
| Duyệt | Admin | `isAdmin()`; `writerActive()`; trước `PENDING`, sau `APPROVED`; `onlyKeys([status, reviewedBy, reviewedAt, updatedAt])` (admin không sửa `nationalId` hay `imageIds`); `reviewedBy == me`; `serverStamp(reviewedAt)`; `after(users/userId).verificationStatus == 'APPROVED'` | |
| Từ chối | Admin | Như duyệt nhưng sau `REJECTED`, thêm `note` khác rỗng trong `onlyKeys` và `note.size() > 0` | Từ chối bắt buộc có lý do |
| Xóa | Không ai | Từ chối | |

## 7. warnings

Đường dẫn `users/{uid}/warnings/{id}`.

| Thao tác | Ai | Điều kiện | Lý do |
| --- | --- | --- | --- |
| Đọc | Admin | `isAdmin()` | Chỉ màn quản trị dùng (STT 69). Người bị cảnh báo thấy lý do qua `users.lockReason` và thông báo WARNING |
| Tạo | Admin | `isAdmin()`; `writerActive()`; `userId == uid`; `issuedBy == me`; `level == after(users/uid).warningCount` (cùng lô tăng số cảnh báo); `reason.size() > 0` | Mỗi cảnh báo gắn đúng một lần tăng số |
| Sửa, xóa | Không ai | Từ chối | Lịch sử cảnh báo không sửa được |

## 8. admins

Đường dẫn `admins/{uid}`. Chỉ cần tồn tại. Tài liệu admin đầu tiên do leader tạo tay trên Firebase Console, vì phải đã là admin mới cấp được quyền admin.

| Thao tác | Ai | Điều kiện | Lý do |
| --- | --- | --- | --- |
| Đọc | Chính chủ hoặc admin | `me == uid \|\| isAdmin()` | Chính chủ đọc để biết có hiện khu quản trị không; admin liệt kê cả collection cho màn Quản lý tài khoản admin (UC42) |
| Cấp quyền admin | Admin | `isAdmin()`; `writerActive()`; `uid != me`; khóa chỉ gồm `uid, createdAt`; `uid` bằng mã trong đường dẫn; `serverStamp(createdAt)`; `before(users/uid).data.status == 'ACTIVE'` | Quyết định 2026-10-22 (UC42): admin cấp quyền cho một tài khoản đang hoạt động ngay trong ứng dụng. Tài khoản tự khai quyền vẫn bị chặn vì người ghi phải đã là admin |
| Sửa | Không ai | Từ chối | Tài liệu chỉ có hai trạng thái: có và không có |
| Gỡ quyền admin | Admin | `isAdmin()`; `writerActive()`; `uid != me` | UC42 BR3: admin không tự gỡ quyền của chính mình. BR4 "phải còn ít nhất một Admin" **không** ép được trong Rules vì không đếm được, xem Giới hạn |

## 9. categories

Đường dẫn `categories/{id}`.

| Thao tác | Ai | Điều kiện | Lý do |
| --- | --- | --- | --- |
| Đọc | Mọi người, kể cả khách | Không điều kiện | Khách lọc tìm kiếm và xem chi tiết tin đăng được (quyết định 2026-10-22); danh mục không phải dữ liệu riêng tư |
| Tạo | Admin | `isAdmin()`; `writerActive()`; khóa chỉ gồm `name, parentId, conditionFields, isActive, createdAt, updatedAt`; `name.size() > 0`; nếu `parentId` khác rỗng thì `before(categories/parentId).data.parentId == ''` (chỉ hai cấp) | |
| Sửa | Admin | `isAdmin()`; `writerActive()`; `onlyKeys([name, parentId, conditionFields, isActive, updatedAt])` | Sửa tên, thuộc tính tình trạng, ẩn danh mục |
| Xóa | Admin | `isAdmin()`; `writerActive()` | "Không xóa khi còn tin hoặc danh mục con" kiểm ở ứng dụng bằng `count()` (xem Giới hạn) |

Không còn trường `listingCount`, nên người dùng thường không ghi vào `categories`.

## 10. listings

Đường dẫn `listings/{id}`. Phần phức tạp nhất vì nhiều bên ghi.

**Đọc.** Mọi người, **kể cả khách chưa đăng nhập**, đọc được tin có `status` thuộc AVAILABLE, RESERVED, SOLD (quyết định 2026-10-22: khách xem được trang chủ, tìm kiếm tin đăng và chi tiết tin đăng). Tin DRAFT, HIDDEN, REMOVED chỉ chủ tin và admin đọc. Truy vấn của người xem khác phải mang `status in [AVAILABLE, RESERVED, SOLD]`.

| Thao tác | Ai | Điều kiện | Lý do |
| --- | --- | --- | --- |
| Tạo bản nháp | Người bán | `writerActive()`; `sellerId == me`; `status == 'DRAFT'`; `statusReason == ''`; `serverStamp(createdAt)` | |
| Tạo và đăng | Người bán | `writerActive()`; `verified()`; `request.auth.token.email_verified == true` (kiểm lại); `sellerId == me`; `status == 'AVAILABLE'`; `statusReason == ''`; `imageIds.size()` từ 1 đến 5; `coverThumb.size()` dưới mức 20 KB sau mã hóa; `tradeTypes` không rỗng; nếu có GIVEAWAY thì `price == 0`; `title.size() > 0` | Điều kiện đăng của `User.canSell` và `Listing.publish`. `verified()` là cửa mới từ 2026-10-22: tài khoản chưa được admin duyệt xác thực không đăng tin được |
| Đăng bản nháp | Người bán | `resource.data.sellerId == me`; trước `DRAFT`, sau `AVAILABLE`; `onlyKeys` gồm các trường nội dung và `status, searchKeywords, flagged, updatedAt`; như "Tạo và đăng" | |
| Sửa nội dung | Người bán | `resource.data.sellerId == me`; `writerActive()`; trước thuộc DRAFT, AVAILABLE, hoặc HIDDEN với `statusReason == ''`; `onlyKeys([categoryId, title, description, imageIds, coverThumb, itemCondition, conditionValues, tradeTypes, price, acceptedOfferTypes, minOfferPrice, province, location, searchKeywords, flagged, updatedAt])`; `flagged` chỉ được giữ nguyên hoặc đổi false thành true, không đổi true về false | Bỏ cờ chỉ do admin; cờ do máy người đăng tính nên chỉ cho siết thêm |
| Tự ẩn | Người bán | `resource.data.sellerId == me`; `writerActive()`; trước `AVAILABLE`, sau `HIDDEN`; `statusReason` sau bằng `''`; `onlyKeys([status, statusReason, updatedAt])` | Cùng lô từ chối offer PENDING (ứng dụng, xem Giới hạn) |
| Hiện lại | Người bán | Trước `HIDDEN` **và** `resource.data.statusReason == ''`; sau `AVAILABLE`; `onlyKeys([status, updatedAt])` | Tin do admin ẩn (`statusReason` khác rỗng) thì người bán không hiện lại được |
| Đánh dấu đã bán, mở bán lại | Người bán | `resource.data.sellerId == me`; `writerActive()`; `(AVAILABLE, SOLD)` hoặc `(SOLD, AVAILABLE)`; `onlyKeys([status, updatedAt])`; `statusReason` giữ `''` | Bán ngoài ứng dụng. "Chưa có giao dịch COMPLETED" không kiểm được trong Rules, xem Giới hạn |
| Giữ chỗ khi chấp nhận offer | Người bán | `resource.data.sellerId == me`; `writerActive()`; trước `AVAILABLE`, sau `RESERVED`; `onlyKeys([status, reservedDealId, updatedAt])`; `after(offers/{offerId}).status == 'ACCEPTED'` và offer thuộc tin này; `after(deals/{reservedDealId})` tồn tại, `listingId == id`, `sellerId == me`, `status == 'PENDING_CONFIRM'` | Cùng lô với offer ACCEPTED và `Deal`. **Cần trường `reservedDealId` trên tin (đề xuất mới, phải hỏi leader theo R6):** không có nó thì Rules không biết giao dịch nào đang giữ tin để cho bên khác nhả giữ chỗ |
| Nhả giữ chỗ khi giao dịch hủy | Người mua, người bán (hoặc admin cho quyết định khiếu nại) | `writerActive()`; trước `RESERVED`, sau `AVAILABLE`; `onlyKeys([status, reservedDealId, updatedAt])`; `after(deals/{resource.data.reservedDealId}).status == 'CANCELLED'`; người ghi là `buyerId` hoặc `sellerId` của giao dịch đó, hoặc `isAdmin()` | Người mua không sở hữu tin nhưng được nhả giữ chỗ khi hủy hoặc hết hạn xác nhận; phụ thuộc `reservedDealId` |
| Bán xong khi hoàn tất | Người mua hoặc người bán của giao dịch | `writerActive()`; trước `RESERVED`, sau `SOLD`; `onlyKeys([status, updatedAt])`; `after(deals/{resource.data.reservedDealId}).status == 'COMPLETED'` và người ghi là một bên của giao dịch đó | Cùng lô với `completeDeal` |
| Admin ẩn | Admin | `isAdmin()`; `writerActive()`; trước `AVAILABLE`, sau `HIDDEN`; `onlyKeys([status, statusReason, updatedAt])`; `statusReason.size() > 0` | Bắt buộc lý do |
| Admin khôi phục | Admin | Trước `HIDDEN`, sau `AVAILABLE`; `onlyKeys([status, statusReason, updatedAt])`; `statusReason == ''` | |
| Admin gỡ | Admin | Trước thuộc DRAFT, AVAILABLE, HIDDEN; sau `REMOVED`; `onlyKeys([status, statusReason, updatedAt])` | Gỡ hàng loạt khi khóa tài khoản |
| Admin bỏ cờ | Admin | `isAdmin()`; `onlyKeys([flagged, updatedAt])`; `flagged` sau bằng false | Chỉ admin được hạ cờ |
| Chủ tài khoản vô hiệu hóa | Người bán | `resource.data.sellerId == me`; trước `AVAILABLE`, sau `HIDDEN`; `statusReason == 'Tài khoản đã xóa'`; `after(users/me).status == 'DELETED'`; `onlyKeys([status, statusReason, updatedAt])` | Ngoại lệ có chủ đích cho luật "người bán không tự ghi `statusReason`" |
| Chặn riêng cho chủ tin | Người bán | Không bao giờ được: đổi `sellerId`; ghi `statusReason` khác `''` (trừ dòng vô hiệu hóa); đổi `status` khi `resource.data.status` là REMOVED; đổi `status` khỏi HIDDEN khi `statusReason != ''`; đổi `flagged` từ true về false | Tin do admin ẩn hoặc gỡ không bị chủ tin tự mở |
| Xóa | Người bán | `resource.data.sellerId == me`; `writerActive()`; `resource.data.status == 'DRAFT'` | Chỉ nháp mới xóa hẳn; tin đã đăng thì ẩn |

## 11. conversations

Đường dẫn `conversations/{listingId_buyerId}`.

| Thao tác | Ai | Điều kiện | Lý do |
| --- | --- | --- | --- |
| Đọc | Hai bên của cuộc trò chuyện, hoặc admin | `resource.data.buyerId == me \|\| resource.data.sellerId == me \|\| isAdmin()`; truy vấn của ứng dụng mang `buyerId == me` hoặc `sellerId == me` | Chỉ người trong cuộc đọc; admin đọc khi xử lý khiếu nại |
| Tạo | Người mua | `writerActive()`; mã bằng `listingId + '_' + me`; `buyerId == me`; `sellerId == before(listings/listingId).data.sellerId`; `buyerId != sellerId`; `before(listings/listingId).data.status` thuộc AVAILABLE, RESERVED; `serverStamp(createdAt)` | Một người mua một cuộc trò chuyện cho một tin; không tự nhắn cho mình |
| Sửa xem trước | Một trong hai bên | `writerActive()`; `me` là `buyerId` hoặc `sellerId`; `onlyKeys([lastMessage, lastMessageAt, updatedAt])`; `serverStamp(lastMessageAt)`; `lastMessage.size()` giới hạn | Cập nhật tin nhắn cuối |
| Xóa | Không ai | Từ chối | |

## 12. messages

Đường dẫn `conversations/{id}/messages/{messageId}`.

| Thao tác | Ai | Điều kiện | Lý do |
| --- | --- | --- | --- |
| Đọc | Hai bên của cuộc trò chuyện, hoặc admin | `get(conversations/id)` có `me` là `buyerId` hoặc `sellerId`, hoặc `isAdmin()` | Chỉ người trong cuộc; admin đọc một lần khi xử lý khiếu nại |
| Tạo chữ | Một trong hai bên | `writerActive()`; `me` thuộc cuộc trò chuyện; `senderId == me`; `type == 'TEXT'`; `text.size()` từ 1 đến 2000; `imageId` và `offerId` rỗng; `serverStamp(createdAt)` | |
| Tạo ảnh | Một trong hai bên | Như trên với `type == 'IMAGE'`; `exists(images/imageId)` và `get(images/imageId).data.ownerId == me` | Ảnh phải do người gửi tải lên |
| Tạo offer | Người mua | `type == 'OFFER'`; `senderId == me`; `me == buyerId` của cuộc trò chuyện; `after(offers/offerId)` tồn tại, `buyerId == me`, `conversationId == id`, `status == 'PENDING'` | Tin nhắn offer đi cùng tài liệu offer |
| Sửa, xóa | Không ai | Từ chối | Tin nhắn không sửa, không xóa |

Không có `type == 'VIDEO'`: video trong chat không làm (STT 45 Phát triển sau).

## 13. offers

Đường dẫn `offers/{id}`.

| Thao tác | Ai | Điều kiện | Lý do |
| --- | --- | --- | --- |
| Đọc | Người mua, người bán của offer, hoặc admin | `resource.data.buyerId == me \|\| resource.data.sellerId == me \|\| isAdmin()` | |
| Tạo | Người mua | `writerActive()`; `verified()`; `buyerId == me`; `status == 'PENDING'`; đặt `l = before(listings/listingId).data`: `l.status == 'AVAILABLE'`, `sellerId == l.sellerId`, `buyerId != sellerId`; `conversationId == listingId + '_' + me`; `offerType` thuộc `l.acceptedOfferTypes`; nếu offer có tiền thì `amount >= l.minOfferPrice`; nếu offer chỉ có vật thì `amount == 0` và `exchangeItem.size() > 0`; `serverStamp(createdAt)` | Chỉ gửi khi tin AVAILABLE; đúng loại và mức giá người bán chấp nhận |
| Chấp nhận | Người bán | `writerActive()`; `resource.data.sellerId == me`; trước `PENDING`, sau `ACCEPTED`; `onlyKeys([status, updatedAt])`; `after(listings/listingId).status == 'RESERVED'`; `after(deals/{reservedDealId})` tồn tại với `offerId == id` | Chấp nhận luôn đi cùng giữ chỗ tin và tạo giao dịch |
| Từ chối | Người bán | `writerActive()`; `resource.data.sellerId == me`; trước `PENDING`, sau `REJECTED`; `onlyKeys([status, updatedAt])` | Cũng dùng khi hệ thống từ chối các offer khác hoặc khi tin rời AVAILABLE, vì ở các lô đó người ghi là người bán |
| Admin từ chối khi ẩn tin | Admin | `isAdmin()`; `writerActive()`; trước `PENDING`, sau `REJECTED`; `onlyKeys([status, updatedAt])`; `after(listings/listingId).status` thuộc HIDDEN, REMOVED | Cùng lô với `hideListing` hoặc `removeAllListingsOf` |
| Sửa, xóa khác | Không ai | Từ chối | Người mua không hủy, không sửa offer (STT 47) |

## 14. deals

Đường dẫn `deals/{id}`. Chuyển trạng thái nào không có dòng dưới đây thì bị cấm. Mọi dòng sửa đều có `writerActive()`, và nếu `status` đổi thì `statusHistory` dài thêm đúng một phần tử có `fromStatus` bằng trạng thái cũ, `toStatus` bằng trạng thái mới (xem mục 15). Các trường không đổi bao giờ: `listingId, offerId, buyerId, sellerId, tradeType, itemTitle, itemThumb, createdAt`.

Ký hiệu: `D` là `resource.data`; `D'` là `request.resource.data`; "một bên" là `me == D.buyerId || me == D.sellerId`.

| Thao tác | Ai | Điều kiện | Lý do |
| --- | --- | --- | --- |
| Đọc | Hai bên, hoặc admin | `D.buyerId == me \|\| D.sellerId == me \|\| isAdmin()` | |
| Tạo | Người bán | `writerActive()`; `verified()`; `D'.sellerId == me`; `D'.status == 'PENDING_CONFIRM'`; `D'.sellerConfirmed == true`, `D'.buyerConfirmed == false`; `o = after(offers/D'.offerId).data`: `o.status == 'ACCEPTED'`, `o.sellerId == me`, `D'.buyerId == o.buyerId`, `D'.listingId == o.listingId`; `after(listings/D'.listingId).status == 'RESERVED'`; `D'.agreedPrice == o.amount`; `D'.tradeType` là EXCHANGE khi `o.offerType == 'ITEM'`, ngược lại SELL; `statusHistory.size() == 1`; không có `shippedAt`, `autoReleaseAt`, `receivedAt` | Chỉ tạo cùng lô chấp nhận offer; giá lấy từ offer |
| Sửa điều khoản | Người bán | `me == D.sellerId`; trước `PENDING_CONFIRM`, sau `PENDING_CONFIRM`; `onlyKeys([deliveryMethod, deliveryAddress, scheduledAt, agreedTerms, exchangeItem, agreedPrice, buyerConfirmed, sellerConfirmed, updatedAt])`; `D'.buyerConfirmed == false` và `D'.sellerConfirmed == true`; `statusHistory` không đổi | Chỉ người bán sửa; người mua phải chấp nhận lại. Sang CONFIRMED rồi thì không nhánh nào cho sửa điều khoản |
| Xác nhận (bên thứ nhất) | Một bên | Trước `PENDING_CONFIRM`, sau `PENDING_CONFIRM`; `onlyKeys([cờ của chính mình, updatedAt])`: người mua chỉ bật `buyerConfirmed`, người bán chỉ bật `sellerConfirmed`; cờ của bên kia đang false; `deliveryMethod` khác rỗng | Mỗi bên chỉ bật cờ của mình |
| Xác nhận (bên thứ hai) | Một bên | Như trên nhưng cờ của bên kia đang true; `D'.status == 'CONFIRMED'`; `onlyKeys([cờ của mình, status, statusHistory, updatedAt])`; nếu `deliveryMethod == 'SHIPPING'` thì `deliveryAddress.size() > 0` | Sang CONFIRMED khi đủ hai xác nhận |
| Hết hạn xác nhận | Một bên (đại diện hệ thống) | Trước `PENDING_CONFIRM`, sau `CANCELLED`; `now > D.createdAt + duration.value(3, 'd')`; `onlyKeys([status, statusHistory, updatedAt])`; `after(listings/D.listingId).status == 'AVAILABLE'` | Giải phóng tin bị giữ chỗ mãi mãi; người đổi ghi `system` (chuỗi do ứng dụng ghi, Rules không dựa vào) |
| Hủy | Một bên | Trước `PENDING_CONFIRM` hoặc `CONFIRMED`; sau `CANCELLED`; `onlyKeys([status, cancelReason, statusHistory, updatedAt])`; `D'.cancelReason.size() > 0`; nếu `exists(payments/id)` và đang `HELD` thì `after(payments/id).status == 'REFUNDED'` | Bắt buộc lý do; hoàn tiền cùng lô |
| Giao hàng | Người bán | `me == D.sellerId`; trước `CONFIRMED`, sau `SHIPPED`; `D.deliveryMethod == 'SHIPPING'`; `onlyKeys([status, carrierName, trackingCode, trackingImageId, shippedAt, autoReleaseAt, statusHistory, updatedAt])`; `carrierName` và `trackingCode` khác rỗng; `get(images/trackingImageId).data.ownerId == me`; **`serverStamp(shippedAt)`** và **`autoReleaseOk()`**: `autoReleaseAt` nằm trong khoảng `request.time + 7 ngày` cộng trừ 5 phút; nếu `D.agreedPrice > 0` thì `get(payments/id).data.status == 'HELD'` | Đồng hồ đếm ngược không thể rút ngắn đáng kể. Ứng dụng ghi `autoReleaseAt` bằng giờ máy nên không khớp tuyệt đối giờ máy chủ; sai số 5 phút là chỗ chừa cho lệch đồng hồ, nhỏ so với 7 ngày |
| Nhận hàng (gặp trực tiếp) | Người mua | `me == D.buyerId`; trước `CONFIRMED`, sau `RECEIVED`; `D.deliveryMethod == 'MEET_UP'`; `onlyKeys([status, receivedAt, receivedImageId, statusHistory, updatedAt])`; `serverStamp(receivedAt)`; `receivedImageId.size() > 0`; nếu `D.agreedPrice > 0` thì `get(payments/id).data.status == 'HELD'` và `after(payments/id).status == 'RELEASED'` | Người mua xác nhận bằng tay thì bắt buộc kèm một ảnh chụp lúc nhận |
| Nhận hàng (giao qua vận chuyển) | Người mua | `me == D.buyerId`; trước `SHIPPED`, sau `RECEIVED`; như dòng trên | |
| Hết đếm ngược | Một bên (đại diện hệ thống) | Trước `SHIPPED`, sau `RECEIVED`; **`now >= D.autoReleaseAt`**; `onlyKeys([status, receivedAt, statusHistory, updatedAt])`; `serverStamp(receivedAt)`; `receivedImageId` không đổi (vẫn rỗng); nếu `D.agreedPrice > 0` thì `after(payments/id).status == 'RELEASED'` | Người bán cũng chạy được, vì mốc là giờ máy chủ. Không có ảnh vì không ai bấm nhận |
| Mở khiếu nại | Một bên | Trước `SHIPPED` và `now < D.autoReleaseAt`, hoặc trước `CONFIRMED` và `D.deliveryMethod == 'MEET_UP'`; sau `DISPUTED`; `onlyKeys([status, statusHistory, updatedAt])`; `after(complaints/id)` tồn tại với `complainantId == me` | Chỉ trong thời gian đếm ngược; khiếu nại ghi cùng lô |
| Rút khiếu nại | Người khiếu nại | Trước `DISPUTED`; sau bằng `fromStatus` của phần tử cuối cùng của `D.statusHistory` (đó là dòng có `toStatus == 'DISPUTED'`, kiểm lại cách lấy phần tử cuối); `onlyKeys([status, statusHistory, updatedAt])`; `after(complaints/id).status == 'WITHDRAWN'` và `complainantId == me` | `autoReleaseAt` giữ nguyên |
| Quyết định hoàn tiền, trả hàng, hủy | Admin | `isAdmin()`; trước `DISPUTED`, sau `CANCELLED`; `after(complaints/id).decision` thuộc REFUND, RETURN_ITEM, CANCEL_DEAL và `.status == 'RESOLVED'`; `onlyKeys([status, statusHistory, updatedAt])`; nếu `exists(payments/id)` thì `after(payments/id).status == 'REFUNDED'`; `after(listings/D.listingId).status == 'AVAILABLE'` | |
| Quyết định bác khiếu nại | Admin | `isAdmin()`; trước `DISPUTED`, sau `RECEIVED`; `after(complaints/id).decision == 'REJECT'`; `serverStamp(receivedAt)`; nếu `exists(payments/id)` thì `after(payments/id).status == 'RELEASED'` | |
| Hoàn tất (hai bên đã đánh giá) | Một bên | Trước `RECEIVED`, sau `COMPLETED`; `existsAfter(reviews/{id}_{D.buyerId})` và `existsAfter(reviews/{id}_{D.sellerId})` (cả hai đánh giá có mặt sau lô); `onlyKeys([status, statusHistory, updatedAt])`; `after(listings/D.listingId).status == 'SOLD'` | `completeDeal` là nơi duy nhất ghi COMPLETED |
| Hoàn tất (đủ 3 ngày) | Một bên (đại diện hệ thống) | Trước `RECEIVED`, sau `COMPLETED`; `now > D.receivedAt + duration.value(3, 'd')`; như dòng trên cho tin SOLD | |
| Xóa | Không ai | Từ chối | Lịch sử giao dịch giữ nguyên |

Quyết định REQUEST_EVIDENCE và bổ sung bằng chứng **không** ghi vào tài liệu `deals` (giao dịch giữ DISPUTED), nên không có dòng ở bảng này.

## 15. statusHistory

Mảng lồng `deals/{id}.statusHistory`, không phải collection riêng, nên không có luật đường dẫn riêng. Các ràng buộc dưới đây nằm trong luật của `deals` (mục 14):

| Thao tác | Ai | Điều kiện | Lý do |
| --- | --- | --- | --- |
| Thêm phần tử | Người đang đổi `status` của giao dịch | `D'.statusHistory.size() == D.statusHistory.size() + 1` (hoặc `== 1` ở lần tạo); `D'.statusHistory[cuối].fromStatus == D.status`; `D'.statusHistory[cuối].toStatus == D'.status`; các phần tử cũ không đổi (`D'.statusHistory[0:n] == D.statusHistory`, kiểm lại cú pháp cắt danh sách) | Lịch sử chỉ thêm, không sửa |
| Không thêm khi trạng thái giữ nguyên | Mọi người | Nếu `D'.status == D.status` thì `D'.statusHistory == D.statusHistory` | Quy ước "chỉ dòng thật sự đổi trạng thái" |
| Sửa, xóa phần tử | Không ai | Từ chối | |

`changedAt` trong phần tử dùng giờ máy (mảng không cho `serverTimestamp`), và `changedBy` là chuỗi do ứng dụng ghi; Rules **không** dựa vào hai trường này.

## 16. payments

Đường dẫn `payments/{dealId}`.

| Thao tác | Ai | Điều kiện | Lý do |
| --- | --- | --- | --- |
| Đọc | Người trả, người nhận, hoặc admin | `resource.data.payerId == me \|\| resource.data.payeeId == me \|\| isAdmin()` | Lịch sử thanh toán (STT 58) |
| Tạo (giữ tiền) | Người mua | `writerActive()`; `d = before(deals/dealId).data`; `me == d.buyerId`; `d.status == 'CONFIRMED'`; `d.agreedPrice > 0`; `amount == d.agreedPrice`; `payerId == d.buyerId`; `payeeId == d.sellerId`; `status == 'HELD'`; `method` thuộc CARD, E_WALLET, BANK_TRANSFER; `serverStamp(heldAt)`; `releasedAt == null` | Tiền chỉ được giữ khi giao dịch đã xác nhận, đúng số tiền thỏa thuận. `method` chỉ để hiện lại trên biên lai; không lệnh ghi nào sau đó được sửa nó |
| Giải ngân khi người mua nhận | Người mua | `me == payerId`; trước `HELD`, sau `RELEASED`; `onlyKeys([status, releasedAt, updatedAt])`; `serverStamp(releasedAt)`; `after(deals/dealId).status == 'RECEIVED'` và `before(deals/dealId).data.status` thuộc SHIPPED, CONFIRMED (CONFIRMED chỉ khi `deliveryMethod == 'MEET_UP'`) | Cùng lô giao dịch sang RECEIVED |
| Giải ngân khi hết đếm ngược | Một bên của giao dịch | `me` là `payerId` hoặc `payeeId`; trước `HELD`, sau `RELEASED`; `before(deals/dealId).data.status == 'SHIPPED'`; **`now >= before(deals/dealId).data.autoReleaseAt`**; `after(deals/dealId).status == 'RECEIVED'`; `serverStamp(releasedAt)` | Người bán chạy được vì mốc là giờ máy chủ; không có trường nào do người ghi tự khai |
| Giải ngân khi bác khiếu nại | Admin | `isAdmin()`; trước `HELD`, sau `RELEASED`; `after(complaints/dealId).decision == 'REJECT'`; `after(deals/dealId).status == 'RECEIVED'` | |
| Hoàn tiền khi hủy | Một bên của giao dịch | Trước `HELD`, sau `REFUNDED`; `onlyKeys([status, releasedAt, updatedAt])`; `serverStamp(releasedAt)`; `after(deals/dealId).status == 'CANCELLED'` | Cùng lô giao dịch CANCELLED |
| Hoàn tiền khi quyết định khiếu nại | Admin | `isAdmin()`; trước `HELD`, sau `REFUNDED`; `after(complaints/dealId).decision` thuộc REFUND, RETURN_ITEM, CANCEL_DEAL; `after(deals/dealId).status == 'CANCELLED'` | |
| Sửa khác, xóa | Không ai | Từ chối | RELEASED và REFUNDED là cuối |

Tiền ở đây chỉ là bản ghi mô phỏng, nhưng Rules vẫn giữ các ràng buộc như tiền thật để chứng minh thiết kế.

## 17. reviews

Đường dẫn `reviews/{dealId_reviewerId}`.

| Thao tác | Ai | Điều kiện | Lý do |
| --- | --- | --- | --- |
| Đọc | Người đã đăng nhập | `signedIn()` | Điểm đánh giá hiển thị công khai (STT 7, 60) |
| Tạo | Người đánh giá | `writerActive()`; mã bằng `dealId + '_' + me`; `reviewerId == me`; `d = before(deals/dealId).data`; `me` là `d.buyerId` hoặc `d.sellerId`; `revieweeId` là bên còn lại của `d`; `d.status` thuộc RECEIVED, COMPLETED; `rating`, `accuracyRating`, `attitudeRating`, `commitmentRating` đều từ 1 đến 5; `comment.size()` không quá 500; `after(users/revieweeId)` có `ratingCount == before(users/revieweeId).data.ratingCount + 1` và `ratingSum == before(users/revieweeId).data.ratingSum + rating`; `serverStamp(createdAt)` | Mã cố định nên mỗi bên một đánh giá cho mỗi giao dịch; điểm cộng cho người được đánh giá đúng bằng điểm viết, cùng lô |
| Sửa, xóa | Không ai | Từ chối | Đánh giá không sửa được (điểm đã cộng) |

## 18. reports

Đường dẫn `reports/{id}`.

| Thao tác | Ai | Điều kiện | Lý do |
| --- | --- | --- | --- |
| Đọc | Người báo cáo, hoặc admin | `resource.data.reporterId == me \|\| isAdmin()` | Xem lịch sử báo cáo của mình (STT 63); admin xử lý (STT 75) |
| Tạo | Người dùng | `writerActive()`; `reporterId == me`; `status == 'OPEN'`; `handledBy == null`; `targetType` thuộc LISTING, USER, MESSAGE; `reason` thuộc năm lý do định sẵn; `description.size()` không quá 1000; `evidenceImageIds.size()` không quá 3; `conversationId` khác rỗng khi và chỉ khi `targetType == 'MESSAGE'`; `serverStamp(createdAt)` | Ảnh đính kèm là tùy chọn, tối đa 3 (quyết định 2026-10-22). Báo cáo tin nhắn phải mang mã cuộc trò chuyện, không thì admin mở ra không đọc được gì |
| Hủy | Người báo cáo | `resource.data.reporterId == me`; trước `OPEN`, sau `CANCELLED`; `onlyKeys([status, updatedAt])` | |
| Xử lý | Admin | `isAdmin()`; `writerActive()`; trước `OPEN`, sau `RESOLVED` hoặc `DISMISSED`; `onlyKeys([status, handledBy, updatedAt])`; `handledBy == me` | |
| Xóa | Không ai | Từ chối | |

Tạo báo cáo không sửa tài liệu người bị báo cáo, nên không có cổng ghi chéo.

## 19. complaints

Đường dẫn `complaints/{dealId}`: mỗi giao dịch tối đa một khiếu nại trong cả đời giao dịch, vì mã là `dealId` và Rules không cho xóa.

| Thao tác | Ai | Điều kiện | Lý do |
| --- | --- | --- | --- |
| Đọc | Người khiếu nại, bên còn lại của giao dịch, hoặc admin | `resource.data.complainantId == me`; hoặc `get(deals/id)` có `me` là `buyerId` hoặc `sellerId`; hoặc `isAdmin()` | Bên bị khiếu nại biết và xem kết quả; `adminNote` thì không (mục 20) |
| Tạo | Một bên của giao dịch | `writerActive()`; `d = before(deals/id).data`; `me` là `d.buyerId` hoặc `d.sellerId`; `complainantId == me`; `dealId == id`; trước `d.status == 'SHIPPED'` và `now < d.autoReleaseAt`, hoặc `d.status == 'CONFIRMED'` và `d.deliveryMethod == 'MEET_UP'`; `after(deals/id).status == 'DISPUTED'`; `status == 'OPEN'`; `priority == 'MEDIUM'`; `decision == null`; `evidenceImageIds.size()` từ 1 đến 5 và ảnh đầu thuộc người ghi (`get(images/evidenceImageIds[0]).data.ownerId == me`, các ảnh sau không kiểm được); `description.size()` không quá 2000; `serverStamp(createdAt)` | Chỉ mở trong thời gian đếm ngược; mã cố định chặn mở lại; deal sang DISPUTED cùng lô |
| Bổ sung bằng chứng | Người khiếu nại | `resource.data.complainantId == me`; `writerActive()`; trước `NEEDS_EVIDENCE`, sau `OPEN`; `onlyKeys([evidenceImageIds, status, updatedAt])`; `evidenceImageIds.size()` lớn hơn trước và không quá trước cộng 5; các phần tử cũ giữ nguyên (kiểm lại cách so mảng) | Chỉ thêm, không xóa ảnh cũ |
| Rút đơn | Người khiếu nại | `resource.data.complainantId == me`; trước `OPEN` hoặc `NEEDS_EVIDENCE`, sau `WITHDRAWN`; `onlyKeys([status, updatedAt])`; `after(deals/id)` quay về trạng thái trước khiếu nại (xem mục 14) | Rút là cuối cùng |
| Đặt mức ưu tiên | Admin | `isAdmin()`; `writerActive()`; `onlyKeys([priority, updatedAt])`; `priority` thuộc LOW, MEDIUM, HIGH | |
| Yêu cầu bổ sung bằng chứng | Admin | `isAdmin()`; `writerActive()`; trước `OPEN`, sau `NEEDS_EVIDENCE`; `onlyKeys([status, decision, evidenceRequest, handledBy, updatedAt])`; `decision == 'REQUEST_EVIDENCE'`; `evidenceRequest.size() > 0`; `handledBy == me` | |
| Ra quyết định | Admin | `isAdmin()`; `writerActive()`; trước `OPEN` hoặc `NEEDS_EVIDENCE`, sau `RESOLVED`; `onlyKeys([status, decision, resolutionMessage, handledBy, updatedAt])`; `decision` thuộc REFUND, RETURN_ITEM, CANCEL_DEAL, REJECT; `handledBy == me`; hệ quả lên `deals`, `payments`, `listings` kiểm ở các mục 14, 16, 10 | |
| Xóa | Không ai | Từ chối | |

## 20. adminNote

Đường dẫn `complaints/{id}/adminNote/note`. Ghi chú nội bộ.

| Thao tác | Ai | Điều kiện | Lý do |
| --- | --- | --- | --- |
| Đọc | Admin | `isAdmin()` | Người dùng không đọc được, kể cả người khiếu nại (quyết định đã chốt) |
| Tạo, sửa | Admin | `isAdmin()`; `writerActive()`; `complaintId == id`; `adminId == me`; `text.size()` không quá 2000; `serverStamp(updatedAt)` | |
| Xóa | Không ai | Từ chối | |

## 21. notifications

Đường dẫn `notifications/{id}`.

| Thao tác | Ai | Điều kiện | Lý do |
| --- | --- | --- | --- |
| Đọc | Người nhận | `resource.data.recipientId == me`; truy vấn mang `recipientId == me` | Thông báo của ai người ấy đọc |
| Tạo | Người đã đăng nhập và ACTIVE | `writerActive()`; `recipientId != me`; `isRead == false`; `type` thuộc 10 loại của `NotificationType`; `title.size()` không quá 100; `body.size()` không quá 300; `serverStamp(createdAt)` | Thông báo được ghi cho người khác ngay trong lô của thao tác làm đổi trạng thái. **Chấp nhận rủi ro giả mạo:** Rules không đối chiếu nội dung thông báo với sự việc thật |
| Tạo (tăng cứng, tùy chọn) | Người ghi | Tùy `type`: DEAL_UPDATE và PAYMENT_UPDATE đòi `targetId` là giao dịch mà cả người ghi và `recipientId` là một bên; NEW_MESSAGE, NEW_OFFER đòi `targetId` là cuộc trò chuyện của cả hai; WARNING, LISTING_MODERATED, VERIFICATION_RESULT, COMPLAINT_RESULT đòi `isAdmin()` | Giảm giả mạo với giá là thêm vài lần `get()` (vướng giới hạn số lần đọc của lô, kiểm lại); chỉ làm nếu còn dư |
| Đánh dấu đã đọc | Người nhận | `resource.data.recipientId == me`; `writerActive()`; `onlyKeys([isRead, updatedAt])`; `isRead` từ false sang true | |
| Xóa | Không ai | Từ chối | |

## 22. activityLogs

Đường dẫn `activityLogs/{id}`.

| Thao tác | Ai | Điều kiện | Lý do |
| --- | --- | --- | --- |
| Đọc | Admin | `isAdmin()` | Nhật ký theo dõi một tài khoản (STT 74) |
| Tạo | Người thực hiện | `writerActive()`; `userId == me`; `action.size()` không quá 60; `targetType.size()` và `targetId.size()` giới hạn; `serverStamp(createdAt)` | Không ghi hộ người khác; người dùng không đọc hay sửa được |
| Sửa, xóa | Không ai | Từ chối | Nhật ký chỉ thêm |

## 23. bannedKeywords

Đường dẫn `bannedKeywords/{id}`.

| Thao tác | Ai | Điều kiện | Lý do |
| --- | --- | --- | --- |
| Đọc | Người đã đăng nhập | `signedIn()` | Máy người bán cần danh sách để gắn cờ trước khi đăng (`KeywordFilter`). Hệ quả: ai cũng đọc được danh sách từ cấm (chấp nhận) |
| Tạo | Admin | `isAdmin()`; `writerActive()`; `createdBy == me`; `keyword` đã bỏ dấu, viết thường, `size()` từ 2 đến 60 | |
| Xóa | Admin | `isAdmin()`; `writerActive()` | |
| Sửa | Không ai | Từ chối | Muốn đổi thì xóa rồi thêm |

## 24. images

Đường dẫn `images/{id}`. Ảnh nén dạng Base64, thay cho Cloud Storage.

| Thao tác | Ai | Điều kiện | Lý do |
| --- | --- | --- | --- |
| Đọc | Mọi người, kể cả khách | Không điều kiện | Khách phải xem được ảnh tin đăng (quyết định 2026-10-22). Mã ảnh là mã ngẫu nhiên khó đoán nhưng không phải bí mật: ai biết mã đều đọc được, kể cả ảnh chat, vận đơn và bằng chứng (xem Giới hạn) |
| Tạo | Người tải lên | `writerActive()`; `ownerId == me`; `purpose` thuộc LISTING, CHAT, WAYBILL, RECEIVED, EVIDENCE, VERIFICATION; `data is string` và `data.size() < 600000` (khoảng 400 KB Base64 cho ảnh 300 KB, còn dư an toàn dưới 1 MiB); `width` và `height` không quá 1024; `sizeBytes` không quá 307200 (300 KB); `serverStamp(createdAt)` | Giới hạn dung lượng thay cho luật của Storage; chống lấp đầy hạn mức Spark |
| Sửa | Không ai | Từ chối | Ảnh bất biến; muốn đổi thì tải ảnh mới |
| Xóa | Người tải lên | `resource.data.ownerId == me`; `writerActive()` | Dọn ảnh khi lưu thất bại hoặc bỏ ảnh khỏi tin |

Ảnh thu nhỏ (`coverThumb`, `itemThumb`, `avatarBase64`) không phải tài liệu `images`: chúng là chuỗi nằm trong tài liệu `listings`, `deals`, `users` và bị giới hạn kích thước ở các mục 10, 14, 1.

## Giới hạn

Điều Rules **không** làm được, nên phải do ứng dụng giữ hoặc được chấp nhận là rủi ro. Mọi mục dưới đây cần người viết code và người duyệt biết để không tin Rules quá mức.

| Điều Rules không làm được | Hệ quả | Cách xử lý |
| --- | --- | --- |
| Chạy truy vấn (không có `where` hay `count` trong Rules) | Các luật "không còn giao dịch chưa kết thúc khi xóa tài khoản", "tin chưa có giao dịch COMPLETED mới mở bán lại", "không xóa danh mục khi còn tin hoặc danh mục con", "mỗi người một địa chỉ mặc định", "từ chối mọi offer PENDING khi tin rời AVAILABLE" không ép được | Ứng dụng kiểm bằng truy vấn trước khi ghi; máy bị sửa có thể bỏ qua. Chấp nhận trong phạm vi đồ án |
| Đảm bảo duy nhất khi mã tài liệu là mã ngẫu nhiên | Chỉ các collection có mã cố định (`follows`, `conversations`, `payments`, `reviews`, `complaints`, `sellerVerifications`) giữ được "mỗi cặp một tài liệu"; báo cáo (`reports`) và offer cùng người, cùng tin có thể trùng | Chấp nhận trùng báo cáo; chặn offer PENDING trùng ở ứng dụng |
| Biết giao dịch nào đang giữ một tin | Người mua không thể nhả giữ chỗ, và hoàn tất không thể đặt SOLD, nếu không có `reservedDealId` | Đề xuất thêm trường `Listing.reservedDealId` (đổi collection đã có nên phải hỏi leader theo R6). Nếu không thêm: chỉ người bán và admin được đổi trạng thái tin, và nhả giữ chỗ khi người mua hủy sẽ phụ thuộc máy người bán |
| Tin vào giờ máy | `StatusChange.changedAt`, `lastActiveAt`, `scheduledAt`, `User.isLocked` trên giao diện dùng giờ máy, nên chỉnh được | Rules không dựa vào chúng; mọi mốc có hậu quả (`shippedAt`, `autoReleaseAt`, `receivedAt`, `lockedUntil`, hết hạn xác nhận 3 ngày) so với `request.time` |
| Kiểm từng phần tử của mảng mã ảnh | Chỉ kiểm được phần tử đầu và số lượng; người dùng có thể trỏ `imageIds` (tin đăng và yêu cầu xác thực) hoặc `evidenceImageIds` vào ảnh của người khác, và Rules không kiểm được `purpose` của từng ảnh | Chấp nhận: ảnh đã công khai (mục 24). Với yêu cầu xác thực, admin nhìn ảnh rồi mới duyệt nên ảnh sai sẽ bị từ chối |
| Giữ bí mật ảnh chat, vận đơn, bằng chứng | Mọi người đã đăng nhập biết mã ảnh là đọc được | Mã ngẫu nhiên khó đoán; chấp nhận. Muốn kín hơn cần tách ảnh thành tài liệu con của cuộc trò chuyện hoặc giao dịch (đổi thiết kế) |
| Ngăn giả mạo thông báo | Người dùng gửi thông báo tùy ý cho người khác | Chấp nhận (overview.md mục 11); có thể tăng cứng ở mục 21 nếu còn dư giới hạn đọc |
| Kiểm đúng `flagged` | Cờ từ khóa cấm do máy người đăng tính; Rules chỉ chặn hạ cờ | Admin xem danh sách tin có cờ và bỏ cờ |
| Kiểm `users.searchKeywords` đúng là các từ của `displayName` | Người dùng có thể nhồi tới 20 từ bất kỳ để lọt vào kết quả tìm người dùng (STT 103) | Chấp nhận trong phạm vi đồ án; Rules chặn quá 20 từ, kết quả chỉ hiện tài khoản ACTIVE |
| Chống lạm dụng khối lượng | Mỗi người có thể tạo nhiều offer, báo cáo, ảnh trong hạn mức Spark | Chấp nhận; theo dõi mức dùng trước buổi demo |
| Chứng minh bằng chứng vận đơn thật | Ảnh và mã vận đơn có thể giả | Đây là rủi ro nghiệp vụ, khiếu nại trong thời gian đếm ngược là cách xử lý |
| Kiểm điểm ba tiêu chí con của đánh giá | Chỉ kiểm giá trị 1 đến 5; chỉ điểm tổng thể (`rating`) được cộng vào `ratingSum` | Ba tiêu chí con chỉ để hiển thị |
| Số lần đọc trong một lô | Lô chấp nhận offer (offer, các offer khác, giao dịch, tin, thông báo) và lô tạo đánh giá có thể vượt giới hạn số lần `get()` (kiểm lại giới hạn hiện hành) | Giữ lô gọn; cắt bớt kiểm tra tăng cứng (mục 21) nếu vượt; thử trên Rules Playground |
| Mốc `request.auth.token.email_verified` | Token đăng nhập chỉ mang giá trị mới sau khi làm mới | Ứng dụng gọi `AuthRepository.reloadUser` và buộc làm mới token sau khi xác thực email (kiểm lại) |
| Quyền admin dùng chung | Mọi admin có quyền như nhau (quyết định 2026-10-11); không có nhật ký thao tác admin (STT 100 là Phát triển sau) | Chấp nhận |
| Đếm số admin còn lại | UC42 BR4 "phải còn ít nhất một Admin" không ép được, vì Rules không đếm được tài liệu | Ứng dụng đếm trước khi gỡ quyền; một admin cố tình vẫn gỡ được hết. Chấp nhận trong phạm vi đồ án |
| Giấu thông tin người bán với khách | Khách đọc được `listings` và `images` nhưng không đọc được `users`, nên màn chi tiết tin ở chế độ khách thiếu tên và điểm người bán | Giao diện ẩn khối người bán khi chưa đăng nhập và mời đăng nhập; không chép tên người bán vào tài liệu tin |
| Ảnh chat, vận đơn, bằng chứng nay công khai | Từ khi `images` cho khách đọc, ai biết mã ảnh đều đọc được dù chưa đăng nhập | Mã ngẫu nhiên khó đoán; chấp nhận. Muốn kín hơn phải tách ảnh tin đăng thành collection riêng |

Khi viết file `firebase/firestore.rules`: mỗi bảng ở trên thành một khối `match`; các hàm ở "Quy ước chung" thành hàm trong file luật; mỗi chuyển trạng thái thành một nhánh `allow update` riêng để lỗi từ chối dễ truy ra. Luật nào ghi "(kiểm lại)" phải thử trên Rules Playground hoặc bằng bộ kiểm thử của Firebase Emulator trước khi gộp.
