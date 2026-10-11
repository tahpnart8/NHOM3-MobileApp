# Cấu trúc dữ liệu cục bộ (Room, SQLite)

Nguồn sự thật cho mọi bảng cục bộ, khớp trang Room của [ERD](../diagrams/erd/README.md) và gói `data.local` của [sơ đồ lớp](../diagrams/so-do-lop/README.md). Room chưa được thêm vào build; đây là thiết kế đã chốt.

Quy tắc:

- Room chỉ là **bộ nhớ đệm** để xem lại trang đã xem khi mất mạng; nguồn dữ liệu chính là Firestore. Ứng dụng đọc Room trước, gọi Firebase, rồi ghi lại vào Room.
- Toàn bộ cơ sở dữ liệu bị xóa sạch khi đăng xuất.
- Đổi bảng hoặc cột đã có cần hỏi leader trước (R6), tăng phiên bản cơ sở dữ liệu và viết migration.

## Phiên bản cơ sở dữ liệu

Phiên bản **1** (thiết kế ban đầu). Tên cơ sở dữ liệu: hằng `AppDatabase.DB_NAME`.

## Tổng quan

| Bảng | Entity | Sao lưu từ | Mục đích |
| --- | --- | --- | --- |
| `listing_cache` | `ListingEntity` | `listings` | Bản sao tin đăng. |
| `conversation_cache` | `ConversationEntity` | `conversations` | Bản sao cuộc trò chuyện. |
| `message_cache` | `MessageEntity` | `conversations/messages` | Bản sao tin nhắn. |
| `deal_cache` | `DealEntity` | `deals` | Bản sao giao dịch. |
| `recent_view` | `RecentViewEntity` | (chỉ trên máy) | Lịch sử xem, chỉ ở trên máy. |
| `recent_search` | `RecentSearchEntity` | (chỉ trên máy) | Lịch sử tìm kiếm, chỉ ở trên máy. |

## listing_cache

- **Entity:** `ListingEntity`
- **Sao lưu từ:** listings

| Cột | Kiểu SQLite | Khóa | Ghi chú |
| --- | --- | --- | --- |
| `id` | TEXT | PK |  |
| `sellerId` | TEXT |  |  |
| `title` | TEXT |  |  |
| `description` | TEXT |  |  |
| `imageUrlsJson` | TEXT |  | danh sách lưu dạng chuỗi JSON (qua `Converters`) |
| `price` | LONG |  |  |
| `categoryId` | TEXT |  |  |
| `tradeTypesJson` | TEXT |  | danh sách lưu dạng chuỗi JSON (qua `Converters`) |
| `province` | TEXT |  |  |
| `status` | TEXT |  |  |
| `createdAtMillis` | LONG |  | thời gian dạng mili giây |
| `cachedAtMillis` | LONG |  | thời gian dạng mili giây |
| `viewedAtMillis` | LONG |  | thời gian dạng mili giây |

## conversation_cache

- **Entity:** `ConversationEntity`
- **Sao lưu từ:** conversations

| Cột | Kiểu SQLite | Khóa | Ghi chú |
| --- | --- | --- | --- |
| `id` | TEXT | PK |  |
| `listingId` | TEXT |  |  |
| `buyerId` | TEXT |  |  |
| `sellerId` | TEXT |  |  |
| `lastMessage` | TEXT |  |  |
| `lastMessageAt` | LONG |  |  |
| `cachedAtMillis` | LONG |  | thời gian dạng mili giây |

## message_cache

- **Entity:** `MessageEntity`
- **Sao lưu từ:** conversations/messages

| Cột | Kiểu SQLite | Khóa | Ghi chú |
| --- | --- | --- | --- |
| `id` | TEXT | PK |  |
| `conversationId` | TEXT | FK | tham chiếu conversation_cache |
| `senderId` | TEXT |  |  |
| `type` | TEXT |  |  |
| `text` | TEXT |  |  |
| `mediaUrl` | TEXT |  |  |
| `sentAtMillis` | LONG |  | thời gian dạng mili giây |

## deal_cache

- **Entity:** `DealEntity`
- **Sao lưu từ:** deals

| Cột | Kiểu SQLite | Khóa | Ghi chú |
| --- | --- | --- | --- |
| `id` | TEXT | PK |  |
| `listingId` | TEXT |  |  |
| `buyerId` | TEXT |  |  |
| `sellerId` | TEXT |  |  |
| `itemTitle` | TEXT |  |  |
| `itemImageUrl` | TEXT |  |  |
| `agreedPrice` | LONG |  |  |
| `status` | TEXT |  |  |
| `autoReleaseAtMillis` | LONG |  | thời gian dạng mili giây |
| `updatedAtMillis` | LONG |  | thời gian dạng mili giây |

## recent_view

- **Entity:** `RecentViewEntity`
- **Sao lưu từ:** (chỉ trên máy)

| Cột | Kiểu SQLite | Khóa | Ghi chú |
| --- | --- | --- | --- |
| `listingId` | TEXT | PK |  |
| `categoryId` | TEXT |  |  |
| `viewedAtMillis` | LONG |  | thời gian dạng mili giây |

## recent_search

- **Entity:** `RecentSearchEntity`
- **Sao lưu từ:** (chỉ trên máy)

| Cột | Kiểu SQLite | Khóa | Ghi chú |
| --- | --- | --- | --- |
| `id` | LONG | PK |  |
| `keyword` | TEXT |  |  |
| `searchedAtMillis` | LONG |  | thời gian dạng mili giây |
