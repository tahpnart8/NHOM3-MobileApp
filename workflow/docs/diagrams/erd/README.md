# Sơ đồ quan hệ thực thể (ERD)

Mô hình dữ liệu cho Cloud Firestore và Room (SQLite cục bộ), khớp với các lớp trong `diagrams/so-do-lop/`.

Nộp vào đây:

- `erd.drawio`: file gốc nhiều trang, mở bằng draw.io.
- Ảnh `.png` xuất ra của từng trang.
- Nội dung README này được thay bằng bản tóm tắt đầy đủ: danh sách collection Firestore, bảng Room, quan hệ giữa chúng.

Cập nhật bảng dưới đây khi nộp:

| Mục | Số lượng |
| --- | --- |
| Collection Firestore | |
| Bảng Room | |

## Trang 3 đến 5

Ảnh xuất từ draw.io của trang 3, 4, 5. File gốc `erd.drawio` và các trang còn lại sẽ nộp sau. Hộp viền nét đứt trong ảnh là collection đã vẽ ở trang khác, chỉ đặt vào để thể hiện quan hệ.

### Trang 3. Giao dịch, thanh toán, đánh giá (Firestore)

![ERD trang 3](erd-3-giao-dich-thanh-toan-danh-gia.png)

| Collection | Đường dẫn | Ghi chú |
| --- | --- | --- |
| `deals` | `deals/{id}` | Giao dịch giữa người mua và người bán, lưu bản chụp `itemTitle`, `itemImageUrl` của tin đăng |
| `statusHistory` | `deals/{id}.statusHistory[]` | Mảng map nằm trong `deals`, không phải collection riêng |
| `payments` | `payments/{dealId}` | Mỗi giao dịch có tối đa một thanh toán, id tài liệu chính là `dealId` |
| `reviews` | `reviews/{id}` | Đánh giá sau giao dịch, có `reviewerId` và `revieweeId` |

Quan hệ chính:
- `deals` tham chiếu `listings` (`listingId`), `offers` (`offerId`), `users` (`buyerId`, `sellerId`).
- `deals` 1 - 0..1 `payments`; `deals` 1 - 0..n `reviews`.
- `payments` và `reviews` tham chiếu `users` (`payerId`, `payeeId`; `reviewerId`, `revieweeId`).

### Trang 4. Tin cậy, quản trị (Firestore)

![ERD trang 4](erd-4-tin-cay-quan-tri.png)

| Collection | Đường dẫn | Ghi chú |
| --- | --- | --- |
| `reports` | `reports/{id}` | Báo cáo vi phạm; `targetType` và `targetId` trỏ tới tin đăng, người dùng hoặc tin nhắn |
| `complaints` | `complaints/{id}` | Khiếu nại về một giao dịch |
| `adminNote` | `complaints/{id}/adminNote/note` | Subcollection, một ghi chú của admin cho mỗi khiếu nại |
| `bannedKeywords` | `bannedKeywords/{id}` | Từ khóa bị cấm, do admin tạo |
| `notifications` | `notifications/{id}` | Thông báo gửi tới người dùng |
| `activityLogs` | `activityLogs/{id}` | Nhật ký hoạt động của người dùng |

Quan hệ chính:
- `users` 1 - 0..n `reports` (`reporterId`), `complaints` (`complainantId`), `notifications` (`recipientId`), `activityLogs` (`userId`), `bannedKeywords` (`createdBy`).
- `complaints` tham chiếu `deals` (`dealId`); `complaints` 1 - 0..1 `adminNote`.
- `reports` và `complaints` ghi người xử lý ở `handledBy`, tham chiếu `users`.

### Trang 5. Room (SQLite)

![ERD trang 5](erd-5-room-sqlite.png)

| Bảng | Entity | Bản sao của |
| --- | --- | --- |
| `conversation_cache` | `ConversationEntity` | `conversations` |
| `message_cache` | `MessageEntity` | `messages` |
| `listing_cache` | `ListingEntity` | `listings` |
| `recent_view` | `RecentViewEntity` | Không, chỉ lưu cục bộ |
| `deal_cache` | `DealEntity` | `deals` |
| `recent_search` | `RecentSearchEntity` | Không, chỉ lưu cục bộ |

Quan hệ chính:
- `conversation_cache` 1 - 0..n `message_cache` (khóa ngoại `conversationId`).
- `recent_view` và `deal_cache` trỏ tới `listing_cache` qua `listingId`.
- Các bảng `*_cache` là bộ nhớ đệm để xem lại khi mất mạng; Firestore vẫn là nguồn dữ liệu chính.
