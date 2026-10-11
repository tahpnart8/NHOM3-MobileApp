# Cấu trúc dữ liệu Firestore

Nguồn sự thật cho mọi collection, khớp ERD ở [`../diagrams/erd/`](../diagrams/erd/README.md) và các lớp ở [`../diagrams/so-do-lop/`](../diagrams/so-do-lop/README.md). Firebase chưa được thêm vào build; đây là thiết kế đã chốt để code theo, không phải dữ liệu đang có.

Quy tắc:

- Đổi collection hoặc trường **đã có ở đây** cần hỏi leader trước (`AGENTS.md`, luật R6), rồi sửa tài liệu này, ERD và sơ đồ lớp trong cùng Pull Request.
- Tên collection: số nhiều, chữ thường. Tên trường: `camelCase`. Thời gian dùng kiểu `Timestamp`.
- Trường kiểu liệt kê lưu **tên hằng enum** dưới dạng chuỗi (ví dụ `"SHIPPED"`), chỉ dùng các giá trị ghi ở cột Ghi chú.
- Firestore không có khóa ngoại thật: cột FK là mã tài liệu khác; ứng dụng và Security Rules giữ quan hệ đúng.
- Quyền hạn không bao giờ lấy từ trường do ứng dụng gửi lên; Security Rules quyết định (`AGENTS.md`, luật R12).

## Tổng quan

**22 collection** (kể cả collection con) và **1 mảng lồng** (`deals.statusHistory`).

| Collection | Đường dẫn | Lớp Java | Mục đích |
| --- | --- | --- | --- |
| [`users`](#users) | `users/{uid}` | `User` | Hồ sơ người dùng. |
| [`addresses`](#addresses) | `users/{uid}/addresses/{id}` | `UserAddress` | Địa chỉ giao nhận. |
| [`identity`](#identity) | `users/{uid}/private/identity` | `UserIdentity` | Thông tin định danh riêng tư; Security Rules chỉ cho chủ tài khoản và admin đọc. |
| [`savedListings`](#savedlistings) | `users/{uid}/saved/{listingId}` | (không có lớp riêng) | Tin đã lưu (quan hệ nhiều-nhiều). |
| [`follows`](#follows) | `follows/{followerId_followedId}` | (không có lớp riêng) | Theo dõi (quan hệ nhiều-nhiều tự thân của users). |
| [`sellerVerifications`](#sellerverifications) | `sellerVerifications/{userId}` | `SellerVerification` | Yêu cầu xác minh người bán. |
| [`warnings`](#warnings) | `users/{uid}/warnings/{id}` | `Warning` | Cảnh báo vi phạm theo bậc. |
| [`admins`](#admins) | `admins/{uid}` | (không có lớp riêng) | Chỉ cần tồn tại: Security Rules dùng nó để nhận diện admin. |
| [`categories`](#categories) | `categories/{id}` | `Category` | Danh mục và danh mục con. |
| [`listings`](#listings) | `listings/{id}` | `Listing` | Tin đăng. |
| [`conversations`](#conversations) | `conversations/{listingId_buyerId}` | `Conversation` | Cuộc trò chuyện. |
| [`messages`](#messages) | `conversations/{id}/messages/{id}` | `ChatMessage` | Tin nhắn. |
| [`offers`](#offers) | `offers/{id}` | `Offer` | Offer một chạm. |
| [`deals`](#deals) | `deals/{id}` | `Deal` | Giao dịch. |
| [`statusHistory`](#statushistory) | `deals/{id}.statusHistory[ ]` | `StatusChange` | Mảng nhúng trong deals, không phải collection riêng. |
| [`payments`](#payments) | `payments/{dealId}` | `Payment` | Khoản ký quỹ mô phỏng. |
| [`reviews`](#reviews) | `reviews/{id}` | `Review` | Đánh giá đối tác. |
| [`reports`](#reports) | `reports/{id}` | `Report` | Báo cáo vi phạm. |
| [`complaints`](#complaints) | `complaints/{id}` | `Complaint` | Khiếu nại. |
| [`adminNote`](#adminnote) | `complaints/{id}/adminNote/note` | (không có lớp riêng) | Ghi chú nội bộ ở tài liệu con để người dùng không đọc được. |
| [`notifications`](#notifications) | `notifications/{id}` | `AppNotification` | Thông báo. |
| [`activityLogs`](#activitylogs) | `activityLogs/{id}` | `ActivityLog` | Nhật ký hoạt động. |
| [`bannedKeywords`](#bannedkeywords) | `bannedKeywords/{id}` | `BannedKeyword` | Từ khóa cấm. |

## users

- **Đường dẫn:** `users/{uid}`
- **Lớp Java:** `User`
- **Mục đích:** Hồ sơ người dùng.

| Trường | Kiểu | Khóa | Ghi chú |
| --- | --- | --- | --- |
| `uid` | string | PK |  |
| `email` | string |  |  |
| `displayName` | string |  |  |
| `phone` | string |  |  |
| `avatarUrl` | string |  |  |
| `bio` | string |  |  |
| `status` | string |  | một trong: ACTIVE, TEMP_LOCKED, PERM_LOCKED, DELETED |
| `lockedUntil` | timestamp |  |  |
| `verificationStatus` | string |  | một trong: NONE, PENDING, APPROVED, REJECTED |
| `ratingAverage` | number |  |  |
| `ratingCount` | number |  |  |
| `warningCount` | number |  |  |
| `interestedCategoryIds` | array<string> |  | tham chiếu categories |
| `province` | string |  |  |
| `district` | string |  |  |
| `termsAcceptedAt` | timestamp |  |  |
| `notificationPrefs` | map |  |  |
| `lastActiveAt` | timestamp |  |  |
| `soldCount` | number |  |  |
| `followerCount` | number |  |  |
| `dealCount` | number |  |  |
| `completedDealCount` | number |  |  |
| `reportCount` | number |  |  |
| `createdAt` | timestamp |  |  |
| `updatedAt` | timestamp |  |  |

## addresses

- **Đường dẫn:** `users/{uid}/addresses/{id}`
- **Lớp Java:** `UserAddress`
- **Mục đích:** Địa chỉ giao nhận.

| Trường | Kiểu | Khóa | Ghi chú |
| --- | --- | --- | --- |
| `id` | string | PK |  |
| `userId` | string | FK | tham chiếu users |
| `label` | string |  |  |
| `receiverName` | string |  |  |
| `phone` | string |  |  |
| `province` | string |  |  |
| `district` | string |  |  |
| `detail` | string |  |  |
| `location` | geopoint |  |  |
| `isDefault` | boolean |  |  |

## identity

- **Đường dẫn:** `users/{uid}/private/identity`
- **Lớp Java:** `UserIdentity`
- **Mục đích:** Thông tin định danh riêng tư; Security Rules chỉ cho chủ tài khoản và admin đọc.
- **Quyền đã chốt:** Chỉ chính chủ tài khoản và admin đọc, ghi. Không hiển thị công khai.

| Trường | Kiểu | Khóa | Ghi chú |
| --- | --- | --- | --- |
| `userId` | string | PK,FK | tham chiếu users |
| `nationalId` | string |  |  |
| `dateOfBirth` | timestamp |  |  |
| `gender` | string |  | một trong: MALE, FEMALE, OTHER |
| `permanentAddress` | string |  |  |
| `updatedAt` | timestamp |  |  |

## savedListings

- **Đường dẫn:** `users/{uid}/saved/{listingId}`
- **Lớp Java:** không có lớp riêng
- **Mục đích:** Tin đã lưu (quan hệ nhiều-nhiều).

| Trường | Kiểu | Khóa | Ghi chú |
| --- | --- | --- | --- |
| `userId` | string | PK,FK | tham chiếu users |
| `listingId` | string | PK,FK | tham chiếu listings |
| `savedAt` | timestamp |  |  |

## follows

- **Đường dẫn:** `follows/{followerId_followedId}`
- **Lớp Java:** không có lớp riêng
- **Mục đích:** Theo dõi (quan hệ nhiều-nhiều tự thân của users).

| Trường | Kiểu | Khóa | Ghi chú |
| --- | --- | --- | --- |
| `id` | string | PK |  |
| `followerId` | string | FK | tham chiếu users |
| `followedId` | string | FK | tham chiếu users |
| `createdAt` | timestamp |  |  |

## sellerVerifications

- **Đường dẫn:** `sellerVerifications/{userId}`
- **Lớp Java:** `SellerVerification`
- **Mục đích:** Yêu cầu xác minh người bán.

| Trường | Kiểu | Khóa | Ghi chú |
| --- | --- | --- | --- |
| `userId` | string | PK,FK | tham chiếu users |
| `status` | string |  | một trong: NONE, PENDING, APPROVED, REJECTED |
| `note` | string |  |  |
| `reviewedBy` | string | FK | tham chiếu users |
| `reviewedAt` | timestamp |  |  |

## warnings

- **Đường dẫn:** `users/{uid}/warnings/{id}`
- **Lớp Java:** `Warning`
- **Mục đích:** Cảnh báo vi phạm theo bậc.

| Trường | Kiểu | Khóa | Ghi chú |
| --- | --- | --- | --- |
| `id` | string | PK |  |
| `userId` | string | FK | tham chiếu users |
| `level` | number |  |  |
| `reason` | string |  |  |
| `issuedBy` | string | FK | tham chiếu users |
| `targetId` | string |  |  |
| `createdAt` | timestamp |  |  |

## admins

- **Đường dẫn:** `admins/{uid}`
- **Lớp Java:** không có lớp riêng
- **Mục đích:** Chỉ cần tồn tại: Security Rules dùng nó để nhận diện admin.
- **Quyền đã chốt:** Không ai ghi từ ứng dụng; leader tạo tay trên Firebase Console. Security Rules đọc để biết ai là admin.

| Trường | Kiểu | Khóa | Ghi chú |
| --- | --- | --- | --- |
| `uid` | string | PK,FK | tham chiếu users |
| `createdAt` | timestamp |  |  |

## categories

- **Đường dẫn:** `categories/{id}`
- **Lớp Java:** `Category`
- **Mục đích:** Danh mục và danh mục con.

| Trường | Kiểu | Khóa | Ghi chú |
| --- | --- | --- | --- |
| `id` | string | PK |  |
| `name` | string |  |  |
| `parentId` | string | FK | tham chiếu categories |
| `conditionFields` | array<string> |  |  |
| `isActive` | boolean |  |  |
| `listingCount` | number |  |  |

## listings

- **Đường dẫn:** `listings/{id}`
- **Lớp Java:** `Listing`
- **Mục đích:** Tin đăng.

| Trường | Kiểu | Khóa | Ghi chú |
| --- | --- | --- | --- |
| `id` | string | PK |  |
| `sellerId` | string | FK | tham chiếu users |
| `categoryId` | string | FK | tham chiếu categories |
| `title` | string |  |  |
| `description` | string |  |  |
| `imageUrls` | array<string> |  |  |
| `itemCondition` | string |  | một trong: NEW, LIKE_NEW, USED, REPAIRED, DEFECTIVE |
| `conditionValues` | map |  |  |
| `tradeTypes` | array<string> |  | mỗi phần tử là một trong: SELL, EXCHANGE, GIVEAWAY |
| `price` | number |  |  |
| `acceptedOfferTypes` | array<string> |  | mỗi phần tử là một trong: MONEY, ITEM, MONEY_AND_ITEM |
| `minOfferPrice` | number |  |  |
| `province` | string |  |  |
| `location` | geopoint |  |  |
| `status` | string |  | một trong: DRAFT, AVAILABLE, RESERVED, SOLD, HIDDEN, REMOVED |
| `statusReason` | string |  |  |
| `searchKeywords` | array<string> |  |  |
| `flagged` | boolean |  |  |
| `createdAt` | timestamp |  |  |
| `updatedAt` | timestamp |  |  |

## conversations

- **Đường dẫn:** `conversations/{listingId_buyerId}`
- **Lớp Java:** `Conversation`
- **Mục đích:** Cuộc trò chuyện.

| Trường | Kiểu | Khóa | Ghi chú |
| --- | --- | --- | --- |
| `id` | string | PK |  |
| `listingId` | string | FK | tham chiếu listings |
| `buyerId` | string | FK | tham chiếu users |
| `sellerId` | string | FK | tham chiếu users |
| `lastMessage` | string |  |  |
| `lastMessageAt` | timestamp |  |  |
| `createdAt` | timestamp |  |  |

## messages

- **Đường dẫn:** `conversations/{id}/messages/{id}`
- **Lớp Java:** `ChatMessage`
- **Mục đích:** Tin nhắn.

| Trường | Kiểu | Khóa | Ghi chú |
| --- | --- | --- | --- |
| `id` | string | PK |  |
| `conversationId` | string | FK | tham chiếu conversations |
| `senderId` | string | FK | tham chiếu users |
| `type` | string |  | một trong: TEXT, IMAGE, VIDEO, OFFER |
| `text` | string |  |  |
| `mediaUrl` | string |  |  |
| `offerId` | string | FK | tham chiếu offers |
| `createdAt` | timestamp |  |  |

## offers

- **Đường dẫn:** `offers/{id}`
- **Lớp Java:** `Offer`
- **Mục đích:** Offer một chạm.

| Trường | Kiểu | Khóa | Ghi chú |
| --- | --- | --- | --- |
| `id` | string | PK |  |
| `listingId` | string | FK | tham chiếu listings |
| `conversationId` | string | FK | tham chiếu conversations |
| `buyerId` | string | FK | tham chiếu users |
| `sellerId` | string | FK | tham chiếu users |
| `offerType` | string |  | một trong: MONEY, ITEM, MONEY_AND_ITEM |
| `amount` | number |  |  |
| `exchangeItem` | string |  |  |
| `status` | string |  | một trong: PENDING, ACCEPTED, REJECTED |
| `createdAt` | timestamp |  |  |

## deals

- **Đường dẫn:** `deals/{id}`
- **Lớp Java:** `Deal`
- **Mục đích:** Giao dịch.

| Trường | Kiểu | Khóa | Ghi chú |
| --- | --- | --- | --- |
| `id` | string | PK |  |
| `listingId` | string | FK | tham chiếu listings |
| `offerId` | string | FK | tham chiếu offers |
| `buyerId` | string | FK | tham chiếu users |
| `sellerId` | string | FK | tham chiếu users |
| `tradeType` | string |  | một trong: SELL, EXCHANGE, GIVEAWAY |
| `agreedPrice` | number |  |  |
| `exchangeItem` | string |  |  |
| `deliveryMethod` | string |  | một trong: MEET_UP, SHIPPING |
| `deliveryAddress` | string |  |  |
| `scheduledAt` | timestamp |  |  |
| `agreedTerms` | string |  |  |
| `itemTitle` | string |  | bản chụp từ tin đăng lúc tạo giao dịch |
| `itemImageUrl` | string |  | bản chụp từ tin đăng lúc tạo giao dịch |
| `status` | string |  | một trong: PENDING_CONFIRM, CONFIRMED, SHIPPED, RECEIVED, COMPLETED, CANCELLED, DISPUTED |
| `buyerConfirmed` | boolean |  |  |
| `sellerConfirmed` | boolean |  |  |
| `cancelReason` | string |  |  |
| `carrierName` | string |  |  |
| `trackingCode` | string |  |  |
| `trackingImageUrl` | string |  |  |
| `shippedAt` | timestamp |  |  |
| `autoReleaseAt` | timestamp |  |  |
| `receivedAt` | timestamp |  |  |
| `statusHistory` | array<map> |  | mảng lồng, mỗi phần tử theo mục statusHistory |

## statusHistory

- **Đường dẫn:** `deals/{id}.statusHistory[ ]` (mảng lồng trong tài liệu `deals`, không phải collection riêng)
- **Lớp Java:** `StatusChange`
- **Mục đích:** Mảng nhúng trong deals, không phải collection riêng.

| Trường | Kiểu | Khóa | Ghi chú |
| --- | --- | --- | --- |
| `fromStatus` | string |  | một trong: PENDING_CONFIRM, CONFIRMED, SHIPPED, RECEIVED, COMPLETED, CANCELLED, DISPUTED |
| `toStatus` | string |  | một trong: PENDING_CONFIRM, CONFIRMED, SHIPPED, RECEIVED, COMPLETED, CANCELLED, DISPUTED |
| `changedBy` | string |  | mã người dùng thực hiện lần chuyển |
| `reason` | string |  |  |
| `changedAt` | timestamp |  |  |

## payments

- **Đường dẫn:** `payments/{dealId}`
- **Lớp Java:** `Payment`
- **Mục đích:** Khoản ký quỹ mô phỏng.
- **Quyền đã chốt:** Chuyển sang RELEASED chỉ khi người mua xác nhận, hoặc khi giờ của Firestore đã qua `deals.autoReleaseAt` và giao dịch còn SHIPPED.

| Trường | Kiểu | Khóa | Ghi chú |
| --- | --- | --- | --- |
| `dealId` | string | PK,FK | tham chiếu deals |
| `payerId` | string | FK | tham chiếu users |
| `payeeId` | string | FK | tham chiếu users |
| `amount` | number |  |  |
| `status` | string |  | một trong: HELD, RELEASED, REFUNDED |
| `heldAt` | timestamp |  |  |
| `releasedAt` | timestamp |  |  |

## reviews

- **Đường dẫn:** `reviews/{id}`
- **Lớp Java:** `Review`
- **Mục đích:** Đánh giá đối tác.

| Trường | Kiểu | Khóa | Ghi chú |
| --- | --- | --- | --- |
| `id` | string | PK |  |
| `dealId` | string | FK | tham chiếu deals |
| `reviewerId` | string | FK | tham chiếu users |
| `revieweeId` | string | FK | tham chiếu users |
| `rating` | number |  |  |
| `accuracyRating` | number |  |  |
| `attitudeRating` | number |  |  |
| `commitmentRating` | number |  |  |
| `comment` | string |  |  |
| `createdAt` | timestamp |  |  |

## reports

- **Đường dẫn:** `reports/{id}`
- **Lớp Java:** `Report`
- **Mục đích:** Báo cáo vi phạm.

| Trường | Kiểu | Khóa | Ghi chú |
| --- | --- | --- | --- |
| `id` | string | PK |  |
| `reporterId` | string | FK | tham chiếu users |
| `targetType` | string |  | một trong: LISTING, USER, MESSAGE |
| `targetId` | string |  | tham chiếu listings / users / messages |
| `reason` | string |  | một trong: FAKE_ITEM, MISLEADING_INFO, SCAM, INAPPROPRIATE, OTHER |
| `description` | string |  |  |
| `status` | string |  | một trong: OPEN, RESOLVED, DISMISSED, CANCELLED |
| `handledBy` | string | FK | tham chiếu users |
| `createdAt` | timestamp |  |  |

## complaints

- **Đường dẫn:** `complaints/{id}`
- **Lớp Java:** `Complaint`
- **Mục đích:** Khiếu nại.

| Trường | Kiểu | Khóa | Ghi chú |
| --- | --- | --- | --- |
| `id` | string | PK |  |
| `complainantId` | string | FK | tham chiếu users |
| `dealId` | string | FK | tham chiếu deals |
| `reason` | string |  | một trong: NOT_AS_DESCRIBED, NOT_RECEIVED, DAMAGED, OTHER |
| `description` | string |  |  |
| `evidenceUrls` | array<string> |  |  |
| `status` | string |  | một trong: OPEN, NEEDS_EVIDENCE, RESOLVED, WITHDRAWN |
| `priority` | string |  | một trong: LOW, MEDIUM, HIGH |
| `decision` | string |  | một trong: REFUND, RETURN_ITEM, CANCEL_DEAL, REJECT, REQUEST_EVIDENCE |
| `resolutionMessage` | string |  |  |
| `handledBy` | string | FK | tham chiếu users |
| `createdAt` | timestamp |  |  |

## adminNote

- **Đường dẫn:** `complaints/{id}/adminNote/note`
- **Lớp Java:** không có lớp riêng
- **Mục đích:** Ghi chú nội bộ ở tài liệu con để người dùng không đọc được.
- **Quyền đã chốt:** Chỉ admin đọc, ghi. Người dùng không đọc được, kể cả người khiếu nại.

| Trường | Kiểu | Khóa | Ghi chú |
| --- | --- | --- | --- |
| `complaintId` | string | PK,FK | tham chiếu complaints |
| `text` | string |  |  |
| `adminId` | string | FK | tham chiếu users |
| `updatedAt` | timestamp |  |  |

## notifications

- **Đường dẫn:** `notifications/{id}`
- **Lớp Java:** `AppNotification`
- **Mục đích:** Thông báo.

| Trường | Kiểu | Khóa | Ghi chú |
| --- | --- | --- | --- |
| `id` | string | PK |  |
| `recipientId` | string | FK | tham chiếu users |
| `type` | string |  | một trong: NEW_MESSAGE, NEW_OFFER, OFFER_RESULT, DEAL_UPDATE, PAYMENT_UPDATE, NEW_REVIEW, COMPLAINT_RESULT, WARNING, LISTING_MODERATED, VERIFICATION_RESULT |
| `title` | string |  |  |
| `body` | string |  |  |
| `targetId` | string |  |  |
| `isRead` | boolean |  |  |
| `createdAt` | timestamp |  |  |

## activityLogs

- **Đường dẫn:** `activityLogs/{id}`
- **Lớp Java:** `ActivityLog`
- **Mục đích:** Nhật ký hoạt động.

| Trường | Kiểu | Khóa | Ghi chú |
| --- | --- | --- | --- |
| `id` | string | PK |  |
| `userId` | string | FK | tham chiếu users |
| `action` | string |  |  |
| `targetType` | string |  |  |
| `targetId` | string |  |  |
| `createdAt` | timestamp |  |  |

## bannedKeywords

- **Đường dẫn:** `bannedKeywords/{id}`
- **Lớp Java:** `BannedKeyword`
- **Mục đích:** Từ khóa cấm.

| Trường | Kiểu | Khóa | Ghi chú |
| --- | --- | --- | --- |
| `id` | string | PK |  |
| `keyword` | string |  |  |
| `createdBy` | string | FK | tham chiếu users |
| `createdAt` | timestamp |  |  |

## Quyền đọc và ghi còn lại

Ngoài các mục "Quyền đã chốt" ở trên, quyền của từng collection được chốt khi viết Security Rules ở giai đoạn code, theo nguyên tắc: phải đăng nhập; chỉ người trong cuộc (hai bên của giao dịch, cuộc trò chuyện, đơn khiếu nại) đọc dữ liệu của cuộc đó; admin xác định bằng `admins/{uid}`. Khi chốt, ghi vào tài liệu này.
