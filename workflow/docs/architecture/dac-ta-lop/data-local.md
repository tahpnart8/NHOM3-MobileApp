# Đặc tả lớp: gói `com.nhom3.pubgapp.data.local`

Bản chữ của sơ đồ lớp cho gói này: 12 lớp. Mục lục và cách đọc ở [README.md](README.md).

Room: bộ nhớ đệm SQLite để xem lại trang đã xem khi mất mạng. Chỉ repository được dùng DAO.

| Lớp | Loại | Trách nhiệm |
| --- | --- | --- |
| [`AppDatabase`](#appdatabase) | lớp (Room database) | Cơ sở dữ liệu SQLite cục bộ; bị xóa sạch khi đăng xuất. |
| [`Converters`](#converters) | lớp (type converters) | Đổi kiểu danh sách và thời gian sang kiểu SQLite lưu được. |
| [`ListingEntity`](#listingentity) | lớp (entity) | Bản sao tin đăng đã xem hoặc trong bảng tin để xem lại khi mất mạng. |
| [`ConversationEntity`](#conversationentity) | lớp (entity) | Bản sao danh sách cuộc trò chuyện. |
| [`MessageEntity`](#messageentity) | lớp (entity) | Bản sao tin nhắn đã xem. |
| [`DealEntity`](#dealentity) | lớp (entity) | Bản sao lịch sử giao dịch của người dùng. |
| [`RecentViewEntity`](#recentviewentity) | lớp (entity) | Lịch sử xem tin, nguồn của mục Dành cho bạn; chỉ nằm trên máy. |
| [`RecentSearchEntity`](#recentsearchentity) | lớp (entity) | Lịch sử tìm kiếm, nguồn của mục Dành cho bạn; chỉ nằm trên máy. |
| [`ListingDao`](#listingdao) | interface | Truy vấn bảng listing_cache. |
| [`ChatDao`](#chatdao) | interface | Truy vấn conversation_cache và message_cache. |
| [`DealDao`](#dealdao) | interface | Truy vấn deal_cache. |
| [`HistoryDao`](#historydao) | interface | Truy vấn recent_view và recent_search. |

### AppDatabase

| Mục | Nội dung |
| --- | --- |
| Định danh | `com.nhom3.pubgapp.data.local.AppDatabase` |
| Loại | lớp (Room database) |
| Kế thừa | `androidx.room.RoomDatabase` |
| Lưu trữ | Không lưu |
| Trách nhiệm | Cơ sở dữ liệu SQLite cục bộ; bị xóa sạch khi đăng xuất. |
| Chức năng liên quan (STT) | 104 |

**Thuộc tính**

| Tên | Kiểu | Truy cập | Ý nghĩa |
| --- | --- | --- | --- |
| `INSTANCE` | `AppDatabase` | private, static final | Thể hiện duy nhất (singleton). |
| `DB_NAME` | `String` | private, static final | Tên tệp cơ sở dữ liệu. |

**Phương thức**

| Chữ ký | Mô tả | STT |
| --- | --- | --- |
| `public ListingDao listingDao()` | DAO của `listing_cache`. |  |
| `public ChatDao chatDao()` | DAO của `conversation_cache` và `message_cache`. |  |
| `public DealDao dealDao()` | DAO của `deal_cache`. |  |
| `public HistoryDao historyDao()` | DAO của `recent_view` và `recent_search`. |  |
| `public static AppDatabase getInstance(Context)` | Trả thể hiện duy nhất, tạo lần đầu bằng `Room.databaseBuilder`. |  |

**Quan hệ trên sơ đồ lớp**

- `AppDatabase` kế thừa `RoomDatabase`. Trang 6. Lưu trữ cục bộ (Room).
- Phụ thuộc: `AppDatabase` dùng `Converters` (dùng). Trang 6. Lưu trữ cục bộ (Room).
- Liên kết: `AppDatabase` [1] và `ListingDao` [1] (cung cấp). Trang 6. Lưu trữ cục bộ (Room).
- Liên kết: `AppDatabase` [1] và `HistoryDao` [1]. Trang 6. Lưu trữ cục bộ (Room).
- Liên kết: `AppDatabase` [1] và `ChatDao` [1]. Trang 6. Lưu trữ cục bộ (Room).
- Liên kết: `AppDatabase` [1] và `DealDao` [1]. Trang 6. Lưu trữ cục bộ (Room).

**Dùng tới:** [`ChatDao`](data-local.md#chatdao), [`DealDao`](data-local.md#dealdao), [`HistoryDao`](data-local.md#historydao), [`ListingDao`](data-local.md#listingdao)

### Converters

| Mục | Nội dung |
| --- | --- |
| Định danh | `com.nhom3.pubgapp.data.local.Converters` |
| Loại | lớp (type converters) |
| Lưu trữ | Không lưu |
| Trách nhiệm | Đổi kiểu danh sách và thời gian sang kiểu SQLite lưu được. |
| Chức năng liên quan (STT) | hạ tầng dùng chung |

**Phương thức**

| Chữ ký | Mô tả | STT |
| --- | --- | --- |
| `public String fromStringList(List<String>)` | Danh sách chuỗi sang chuỗi JSON. |  |
| `public List<String> toStringList(String)` | Chuỗi JSON sang danh sách chuỗi. |  |
| `public long fromTimestamp(Timestamp)` | `Timestamp` sang mili giây. |  |
| `public Timestamp toTimestamp(long)` | Mili giây sang `Timestamp`. |  |

**Quan hệ trên sơ đồ lớp**

- Phụ thuộc: `AppDatabase` dùng `Converters` (dùng). Trang 6. Lưu trữ cục bộ (Room).

### ListingEntity

| Mục | Nội dung |
| --- | --- |
| Định danh | `com.nhom3.pubgapp.data.local.ListingEntity` |
| Loại | lớp (entity) |
| Lưu trữ | Room, bảng `listing_cache`, xem [room-schema.md](../../data/room-schema.md#listing_cache) |
| Trách nhiệm | Bản sao tin đăng đã xem hoặc trong bảng tin để xem lại khi mất mạng. |
| Chức năng liên quan (STT) | hạ tầng dùng chung |

**Thuộc tính**

| Tên | Kiểu | Truy cập | Ý nghĩa |
| --- | --- | --- | --- |
| `id` | `String` | private | Khóa chính, bằng mã tài liệu Firestore. |
| `sellerId` | `String` | private | Bản sao `Listing.sellerId`. |
| `title` | `String` | private | Bản sao `Listing.title`. |
| `description` | `String` | private | Bản sao `Listing.description`. |
| `imageIdsJson` | `String` | private | Danh sách mã ảnh dạng chuỗi JSON (không đệm ảnh đầy đủ). |
| `coverThumb` | `String` | private | Bản sao `Listing.coverThumb`; ảnh duy nhất có khi offline. |
| `price` | `long` | private | Bản sao `Listing.price`. |
| `categoryId` | `String` | private | Bản sao `Listing.categoryId`. |
| `tradeTypesJson` | `String` | private | Danh sách hình thức giao dịch dạng chuỗi JSON. |
| `itemCondition` | `String` | private | Bản sao `Listing.itemCondition`. |
| `minOfferPrice` | `long` | private | Bản sao `Listing.minOfferPrice`. |
| `province` | `String` | private | Bản sao `Listing.province`. |
| `status` | `String` | private | Bản sao `Listing.status` (tên hằng enum dạng chuỗi). |
| `createdAtMillis` | `long` | private | Bản sao `Listing.createdAt` dạng mili giây. |
| `cachedAtMillis` | `long` | private | Lúc ghi vào bộ nhớ đệm. |

**Phương thức**

| Chữ ký | Mô tả | STT |
| --- | --- | --- |
| `public ListingEntity fromModel(Listing)` | Tạo bản ghi đệm từ `Listing`. |  |
| `public Listing toModel()` | Dựng lại `Listing` từ bản ghi đệm (thiếu các trường không lưu đệm). |  |

**Quan hệ trên sơ đồ lớp**

- Phụ thuộc: `ListingDao` dùng `ListingEntity`. Trang 6. Lưu trữ cục bộ (Room).
- Phụ thuộc: `ListingEntity` dùng `Listing` (chuyển đổi). Trang 6. Lưu trữ cục bộ (Room).

**Dùng tới:** [`Listing`](model.md#listing)

**Được dùng bởi:** [`ListingDao`](data-local.md#listingdao)

### ConversationEntity

| Mục | Nội dung |
| --- | --- |
| Định danh | `com.nhom3.pubgapp.data.local.ConversationEntity` |
| Loại | lớp (entity) |
| Lưu trữ | Room, bảng `conversation_cache`, xem [room-schema.md](../../data/room-schema.md#conversation_cache) |
| Trách nhiệm | Bản sao danh sách cuộc trò chuyện. |
| Chức năng liên quan (STT) | 43, 44 |

**Thuộc tính**

| Tên | Kiểu | Truy cập | Ý nghĩa |
| --- | --- | --- | --- |
| `id` | `String` | private | Khóa chính, bằng mã tài liệu Firestore. |
| `listingId` | `String` | private | Bản sao `Conversation.listingId`. |
| `buyerId` | `String` | private | Bản sao `Conversation.buyerId`. |
| `sellerId` | `String` | private | Bản sao `Conversation.sellerId`. |
| `lastMessage` | `String` | private | Bản sao `Conversation.lastMessage`. |
| `lastMessageAt` | `long` | private | Bản sao `Conversation.lastMessageAt` dạng mili giây. |
| `cachedAtMillis` | `long` | private | Lúc ghi vào bộ nhớ đệm. |

**Phương thức**

| Chữ ký | Mô tả | STT |
| --- | --- | --- |
| `public ConversationEntity fromModel(Conversation)` | Tạo bản ghi đệm từ `Conversation`. |  |
| `public Conversation toModel()` | Dựng lại `Conversation`. |  |

**Quan hệ trên sơ đồ lớp**

- Phụ thuộc: `ChatDao` dùng `ConversationEntity`. Trang 6. Lưu trữ cục bộ (Room).
- Phụ thuộc: `ConversationEntity` dùng `Conversation`. Trang 6. Lưu trữ cục bộ (Room).

**Dùng tới:** [`Conversation`](model.md#conversation)

**Được dùng bởi:** [`ChatDao`](data-local.md#chatdao)

### MessageEntity

| Mục | Nội dung |
| --- | --- |
| Định danh | `com.nhom3.pubgapp.data.local.MessageEntity` |
| Loại | lớp (entity) |
| Lưu trữ | Room, bảng `message_cache`, xem [room-schema.md](../../data/room-schema.md#message_cache) |
| Trách nhiệm | Bản sao tin nhắn đã xem. |
| Chức năng liên quan (STT) | 43, 44 |

**Thuộc tính**

| Tên | Kiểu | Truy cập | Ý nghĩa |
| --- | --- | --- | --- |
| `id` | `String` | private | Khóa chính, bằng mã tài liệu Firestore. |
| `conversationId` | `String` | private | Bản sao `ChatMessage.conversationId`. |
| `senderId` | `String` | private | Bản sao `ChatMessage.senderId`. |
| `type` | `String` | private | Bản sao `ChatMessage.type` (tên hằng enum dạng chuỗi). |
| `text` | `String` | private | Bản sao `ChatMessage.text`. |
| `imageId` | `String` | private | Bản sao `ChatMessage.imageId`; ảnh đầy đủ không có khi offline. |
| `offerId` | `String` | private | Bản sao `ChatMessage.offerId`. |
| `sentAtMillis` | `long` | private | Bản sao `ChatMessage.createdAt` dạng mili giây. |

**Phương thức**

| Chữ ký | Mô tả | STT |
| --- | --- | --- |
| `public MessageEntity fromModel(ChatMessage)` | Tạo bản ghi đệm từ `ChatMessage`. |  |
| `public ChatMessage toModel()` | Dựng lại `ChatMessage`. |  |

**Quan hệ trên sơ đồ lớp**

- Phụ thuộc: `ChatDao` dùng `MessageEntity`. Trang 6. Lưu trữ cục bộ (Room).
- Phụ thuộc: `MessageEntity` dùng `ChatMessage`. Trang 6. Lưu trữ cục bộ (Room).

**Dùng tới:** [`ChatMessage`](model.md#chatmessage)

**Được dùng bởi:** [`ChatDao`](data-local.md#chatdao)

### DealEntity

| Mục | Nội dung |
| --- | --- |
| Định danh | `com.nhom3.pubgapp.data.local.DealEntity` |
| Loại | lớp (entity) |
| Lưu trữ | Room, bảng `deal_cache`, xem [room-schema.md](../../data/room-schema.md#deal_cache) |
| Trách nhiệm | Bản sao lịch sử giao dịch của người dùng. |
| Chức năng liên quan (STT) | 57, 84 |

**Thuộc tính**

| Tên | Kiểu | Truy cập | Ý nghĩa |
| --- | --- | --- | --- |
| `id` | `String` | private | Khóa chính, bằng mã tài liệu Firestore. |
| `listingId` | `String` | private | Bản sao `Deal.listingId`. |
| `buyerId` | `String` | private | Bản sao `Deal.buyerId`. |
| `sellerId` | `String` | private | Bản sao `Deal.sellerId`. |
| `itemTitle` | `String` | private | Bản sao `Deal.itemTitle`. |
| `itemThumb` | `String` | private | Bản sao `Deal.itemThumb`. |
| `agreedPrice` | `long` | private | Bản sao `Deal.agreedPrice`. |
| `status` | `String` | private | Bản sao `Deal.status` (tên hằng enum dạng chuỗi). |
| `tradeType` | `String` | private | Bản sao `Deal.tradeType` dạng chuỗi, để lọc theo loại giao dịch khi mất mạng. |
| `deliveryMethod` | `String` | private | Bản sao `Deal.deliveryMethod` (tên hằng enum dạng chuỗi). |
| `autoReleaseAtMillis` | `long` | private | Hạn đếm ngược, để hiện đồng hồ cả khi offline. |
| `receivedAtMillis` | `long` | private | Bản sao `Deal.receivedAt` dạng mili giây. |
| `updatedAtMillis` | `long` | private | Bản sao `Deal.updatedAt` dạng mili giây. |

**Phương thức**

| Chữ ký | Mô tả | STT |
| --- | --- | --- |
| `public DealEntity fromModel(Deal)` | Tạo bản ghi đệm từ `Deal`. |  |
| `public Deal toModel()` | Dựng lại `Deal` (đủ để hiện danh sách và đồng hồ đếm ngược). |  |

**Quan hệ trên sơ đồ lớp**

- Phụ thuộc: `DealDao` dùng `DealEntity`. Trang 6. Lưu trữ cục bộ (Room).
- Phụ thuộc: `DealEntity` dùng `Deal`. Trang 6. Lưu trữ cục bộ (Room).

**Dùng tới:** [`Deal`](model.md#deal)

**Được dùng bởi:** [`DealDao`](data-local.md#dealdao)

### RecentViewEntity

| Mục | Nội dung |
| --- | --- |
| Định danh | `com.nhom3.pubgapp.data.local.RecentViewEntity` |
| Loại | lớp (entity) |
| Lưu trữ | Room, bảng `recent_view`, xem [room-schema.md](../../data/room-schema.md#recent_view) |
| Trách nhiệm | Lịch sử xem tin, nguồn của mục Dành cho bạn; chỉ nằm trên máy. |
| Chức năng liên quan (STT) | 38 |

**Thuộc tính**

| Tên | Kiểu | Truy cập | Ý nghĩa |
| --- | --- | --- | --- |
| `listingId` | `String` | private | Tin đã xem; khóa chính nên mỗi tin chỉ một dòng, xem lại thì cập nhật thời điểm. |
| `categoryId` | `String` | private | Danh mục của tin, nguồn của gợi ý theo danh mục. |
| `viewedAtMillis` | `long` | private | Lúc xem. |

**Quan hệ trên sơ đồ lớp**

- Phụ thuộc: `HistoryDao` dùng `RecentViewEntity`. Trang 6. Lưu trữ cục bộ (Room).

**Được dùng bởi:** [`HistoryDao`](data-local.md#historydao)

### RecentSearchEntity

| Mục | Nội dung |
| --- | --- |
| Định danh | `com.nhom3.pubgapp.data.local.RecentSearchEntity` |
| Loại | lớp (entity) |
| Lưu trữ | Room, bảng `recent_search`, xem [room-schema.md](../../data/room-schema.md#recent_search) |
| Trách nhiệm | Lịch sử tìm kiếm, nguồn của mục Dành cho bạn; chỉ nằm trên máy. |
| Chức năng liên quan (STT) | 38 |

**Thuộc tính**

| Tên | Kiểu | Truy cập | Ý nghĩa |
| --- | --- | --- | --- |
| `keyword` | `String` | private | Từ khóa đã tìm. |
| `searchedAtMillis` | `long` | private | Lúc tìm. |

**Quan hệ trên sơ đồ lớp**

- Phụ thuộc: `HistoryDao` dùng `RecentSearchEntity`. Trang 6. Lưu trữ cục bộ (Room).

**Được dùng bởi:** [`HistoryDao`](data-local.md#historydao)

### ListingDao

| Mục | Nội dung |
| --- | --- |
| Định danh | `com.nhom3.pubgapp.data.local.ListingDao` |
| Loại | interface |
| Lưu trữ | Không lưu |
| Trách nhiệm | Truy vấn bảng listing_cache. |
| Chức năng liên quan (STT) | hạ tầng dùng chung |

**Phương thức**

| Chữ ký | Mô tả | STT |
| --- | --- | --- |
| `public void upsertAll(List<ListingEntity>)` | Chèn hoặc thay các tin. |  |
| `public List<ListingEntity> getFeed()` | Tin đã đệm đang AVAILABLE, mới đăng trước. |  |
| `public ListingEntity getById(String)` | Một tin đã đệm. |  |
| `public void trim(int)` | Giữ số tin đệm mới nhất truyền vào, không xóa tin có trong `recent_view`. |  |
| `public void clearAll()` | Xóa mọi tin đệm (khi đăng xuất). |  |

**Quan hệ trên sơ đồ lớp**

- Liên kết: `AppDatabase` [1] và `ListingDao` [1] (cung cấp). Trang 6. Lưu trữ cục bộ (Room).
- Phụ thuộc: `ListingDao` dùng `ListingEntity`. Trang 6. Lưu trữ cục bộ (Room).

**Dùng tới:** [`ListingEntity`](data-local.md#listingentity), [`ListingRepository`](data.md#listingrepository)

**Được dùng bởi:** [`AppDatabase`](data-local.md#appdatabase), [`ListingRepository`](data.md#listingrepository)

### ChatDao

| Mục | Nội dung |
| --- | --- |
| Định danh | `com.nhom3.pubgapp.data.local.ChatDao` |
| Loại | interface |
| Lưu trữ | Không lưu |
| Trách nhiệm | Truy vấn conversation_cache và message_cache. |
| Chức năng liên quan (STT) | 43, 44 |

**Phương thức**

| Chữ ký | Mô tả | STT |
| --- | --- | --- |
| `public void upsertConversations(List<ConversationEntity>)` | Chèn hoặc thay các cuộc trò chuyện. |  |
| `public List<ConversationEntity> getConversations()` | Cuộc trò chuyện đã đệm, mới nhất trước. |  |
| `public void upsertMessages(List<MessageEntity>)` | Chèn hoặc thay các tin nhắn. |  |
| `public List<MessageEntity> getMessages(String)` | Tin nhắn đã đệm của một cuộc trò chuyện, cũ trước. |  |
| `public void clearAll()` | Xóa sạch conversation_cache và message_cache; gọi khi đăng xuất hoặc xóa tài khoản. |  |

**Quan hệ trên sơ đồ lớp**

- Liên kết: `AppDatabase` [1] và `ChatDao` [1]. Trang 6. Lưu trữ cục bộ (Room).
- Phụ thuộc: `ChatDao` dùng `ConversationEntity`. Trang 6. Lưu trữ cục bộ (Room).
- Phụ thuộc: `ChatDao` dùng `MessageEntity`. Trang 6. Lưu trữ cục bộ (Room).

**Dùng tới:** [`ChatRepository`](data.md#chatrepository), [`ConversationEntity`](data-local.md#conversationentity), [`MessageEntity`](data-local.md#messageentity)

**Được dùng bởi:** [`AppDatabase`](data-local.md#appdatabase), [`ChatRepository`](data.md#chatrepository)

### DealDao

| Mục | Nội dung |
| --- | --- |
| Định danh | `com.nhom3.pubgapp.data.local.DealDao` |
| Loại | interface |
| Lưu trữ | Không lưu |
| Trách nhiệm | Truy vấn deal_cache. |
| Chức năng liên quan (STT) | 57 |

**Phương thức**

| Chữ ký | Mô tả | STT |
| --- | --- | --- |
| `public void upsertAll(List<DealEntity>)` | Chèn hoặc thay các giao dịch. |  |
| `public List<DealEntity> getMyDeals(String)` | Giao dịch đã đệm của một người dùng. |  |
| `public void clearAll()` | Xóa sạch deal_cache; gọi khi đăng xuất hoặc xóa tài khoản. |  |

**Quan hệ trên sơ đồ lớp**

- Liên kết: `AppDatabase` [1] và `DealDao` [1]. Trang 6. Lưu trữ cục bộ (Room).
- Phụ thuộc: `DealDao` dùng `DealEntity`. Trang 6. Lưu trữ cục bộ (Room).

**Dùng tới:** [`DealEntity`](data-local.md#dealentity), [`DealRepository`](data.md#dealrepository)

**Được dùng bởi:** [`AppDatabase`](data-local.md#appdatabase), [`DealRepository`](data.md#dealrepository)

### HistoryDao

| Mục | Nội dung |
| --- | --- |
| Định danh | `com.nhom3.pubgapp.data.local.HistoryDao` |
| Loại | interface |
| Lưu trữ | Không lưu |
| Trách nhiệm | Truy vấn recent_view và recent_search. |
| Chức năng liên quan (STT) | 38 |

**Phương thức**

| Chữ ký | Mô tả | STT |
| --- | --- | --- |
| `public void insertView(RecentViewEntity)` | Ghi một lần xem tin (thay nếu đã có). |  |
| `public List<RecentViewEntity> getRecentViews(int)` | Các lần xem gần nhất, giới hạn số dòng. |  |
| `public void insertSearch(RecentSearchEntity)` | Ghi một lần tìm kiếm. |  |
| `public List<RecentSearchEntity> getRecentSearches(int)` | Các lần tìm gần nhất, giới hạn số dòng. |  |
| `public void trimViews(int)` | Chỉ giữ số lần xem mới nhất truyền vào. |  |
| `public void trimSearches(int)` | Chỉ giữ số từ khóa mới nhất truyền vào. |  |
| `public void clearAll()` | Xóa lịch sử xem và tìm kiếm. |  |

**Quan hệ trên sơ đồ lớp**

- Liên kết: `AppDatabase` [1] và `HistoryDao` [1]. Trang 6. Lưu trữ cục bộ (Room).
- Phụ thuộc: `HistoryDao` dùng `RecentViewEntity`. Trang 6. Lưu trữ cục bộ (Room).
- Phụ thuộc: `HistoryDao` dùng `RecentSearchEntity`. Trang 6. Lưu trữ cục bộ (Room).

**Dùng tới:** [`ListingRepository`](data.md#listingrepository), [`RecentSearchEntity`](data-local.md#recentsearchentity), [`RecentViewEntity`](data-local.md#recentviewentity)

**Được dùng bởi:** [`AppDatabase`](data-local.md#appdatabase), [`ListingRepository`](data.md#listingrepository)
