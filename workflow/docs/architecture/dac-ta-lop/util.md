# Đặc tả lớp: gói `com.nhom3.pubgapp.util`

Bản chữ của sơ đồ lớp cho gói này: 7 lớp. Mục lục và cách đọc ở [README.md](README.md).

Tiện ích dùng chung, không giữ trạng thái màn hình.

| Lớp | Loại | Trách nhiệm |
| --- | --- | --- |
| [`SessionManager`](#sessionmanager) | lớp (singleton) | Giữ người dùng đang đăng nhập và cờ admin cho toàn ứng dụng. |
| [`NetworkMonitor`](#networkmonitor) | lớp | Theo dõi kết nối mạng để chọn dữ liệu cục bộ hay dữ liệu mạng. |
| [`KeywordFilter`](#keywordfilter) | lớp | So khớp chuỗi với từ khóa cấm để gắn cờ tin đăng. |
| [`RecommendationEngine`](#recommendationengine) | lớp | Chấm điểm trên máy để gợi ý sản phẩm tương tự, mục Dành cho bạn và khoảng giá tham khảo. |
| [`GeoUtils`](#geoutils) | lớp | Tính khoảng cách và lọc theo bán kính (Firestore không truy vấn được theo bán kính). |
| [`FormatUtils`](#formatutils) | lớp | Định dạng giá, thời gian; bỏ dấu tiếng Việt để tìm kiếm. |
| [`AutoReleaseWorker`](#autoreleaseworker) | lớp (background job) | Việc chạy nền định kỳ trên máy: tự chuyển giao dịch hết đếm ngược sang Đã nhận và giải ngân; tự hoàn tất giao dịch Đã nhận sau 3 ngày. |

### SessionManager

| Mục | Nội dung |
| --- | --- |
| Định danh | `com.nhom3.pubgapp.util.SessionManager` |
| Loại | lớp (singleton) |
| Lưu trữ | Không lưu |
| Trách nhiệm | Giữ người dùng đang đăng nhập và cờ admin cho toàn ứng dụng. |
| Chức năng liên quan (STT) | 1 đến 3 |

**Thuộc tính**

| Tên | Kiểu | Truy cập | Ý nghĩa |
| --- | --- | --- | --- |
| `instance` | `SessionManager` | private, static | Thể hiện duy nhất (singleton). |
| `currentUser` | `User` | private | Hồ sơ người đang đăng nhập. |
| `admin` | `boolean` | private | Cờ admin, đặt sau khi đọc `admins/{uid}`; chỉ dùng để hiện giao diện, quyền thật do Security Rules quyết định. |

**Phương thức**

| Chữ ký | Mô tả | STT |
| --- | --- | --- |
| `public static SessionManager getInstance()` | Trả thể hiện duy nhất. |  |
| `public void start(User, boolean)` | Lưu người dùng và cờ admin sau khi đăng nhập. |  |
| `public void clear()` | Xóa phiên khi đăng xuất. |  |
| `public String getCurrentUserId()` | Mã người đang đăng nhập. |  |
| `public boolean isAdmin()` | Cờ admin để hiện giao diện quản trị. |  |
| `public boolean isLoggedIn()` | Đã có phiên đăng nhập hay chưa. |  |

**Dùng tới:** [`User`](model.md#user)

**Được dùng bởi:** [`AuthViewModel`](viewmodel.md#authviewmodel)

### NetworkMonitor

| Mục | Nội dung |
| --- | --- |
| Định danh | `com.nhom3.pubgapp.util.NetworkMonitor` |
| Loại | lớp |
| Lưu trữ | Không lưu |
| Trách nhiệm | Theo dõi kết nối mạng để chọn dữ liệu cục bộ hay dữ liệu mạng. |
| Chức năng liên quan (STT) | hạ tầng dùng chung |

**Thuộc tính**

| Tên | Kiểu | Truy cập | Ý nghĩa |
| --- | --- | --- | --- |
| `online` | `LiveData<Boolean>` | private | Trạng thái mạng để màn hình quan sát. |

**Phương thức**

| Chữ ký | Mô tả | STT |
| --- | --- | --- |
| `public void register(Context)` | Đăng ký theo dõi mạng; gọi một lần khi ứng dụng khởi động. |  |
| `public void unregister()` | Hủy theo dõi mạng. |  |
| `public boolean isOnline()` | Đang có mạng hay không. |  |

**Được dùng bởi:** [`FeedViewModel`](viewmodel.md#feedviewmodel)

### KeywordFilter

| Mục | Nội dung |
| --- | --- |
| Định danh | `com.nhom3.pubgapp.util.KeywordFilter` |
| Loại | lớp |
| Lưu trữ | Không lưu |
| Trách nhiệm | So khớp chuỗi với từ khóa cấm để gắn cờ tin đăng. |
| Chức năng liên quan (STT) | 78 |

**Thuộc tính**

| Tên | Kiểu | Truy cập | Ý nghĩa |
| --- | --- | --- | --- |
| `keywords` | `List<BannedKeyword>` | private | Danh sách từ khóa cấm đã tải. |

**Phương thức**

| Chữ ký | Mô tả | STT |
| --- | --- | --- |
| `public void setKeywords(List<BannedKeyword>)` | Nạp danh sách từ khóa cấm mới nhất. |  |
| `public boolean containsBanned(String)` | True khi chuỗi chứa ít nhất một từ khóa cấm. |  |
| `public List<String> findMatches(String)` | Trả các từ khóa cấm có trong chuỗi. |  |

**Dùng tới:** [`BannedKeyword`](model.md#bannedkeyword)

**Được dùng bởi:** [`ShopViewModel`](viewmodel.md#shopviewmodel)

### RecommendationEngine

| Mục | Nội dung |
| --- | --- |
| Định danh | `com.nhom3.pubgapp.util.RecommendationEngine` |
| Loại | lớp |
| Lưu trữ | Không lưu |
| Trách nhiệm | Chấm điểm trên máy để gợi ý sản phẩm tương tự, mục Dành cho bạn và khoảng giá tham khảo. |
| Chức năng liên quan (STT) | 29, 36 đến 38 |

**Phương thức**

| Chữ ký | Mô tả | STT |
| --- | --- | --- |
| `public List<Listing> similarTo(Listing, List<Listing>)` | Tin cùng danh mục, gần giá, sắp theo điểm giống nhau. | 36 |
| `public List<Listing> forYou(List<Listing>, List<String>, List<String>, List<String>, User)` | Chấm điểm theo [goi-y.md](../goi-y.md): danh mục đã xem, từ khóa đã tìm, danh mục của tin đã lưu, danh mục quan tâm, độ mới. Nhận danh sách thường, không nhận entity Room. | 29, 38 |
| `public long[] priceRange(Listing, List<Listing>)` | Khoảng giá [thấp, cao] từ các tin cùng danh mục và tình trạng; mảng rỗng khi quá ít tin để tính. | 37 |
| `private double score(Listing)` | Điểm phù hợp của một tin với người dùng. |  |

**Dùng tới:** [`Listing`](model.md#listing), [`User`](model.md#user)

**Được dùng bởi:** [`FeedViewModel`](viewmodel.md#feedviewmodel), [`ListingDetailViewModel`](viewmodel.md#listingdetailviewmodel)

### GeoUtils

| Mục | Nội dung |
| --- | --- |
| Định danh | `com.nhom3.pubgapp.util.GeoUtils` |
| Loại | lớp |
| Lưu trữ | Không lưu |
| Trách nhiệm | Tính khoảng cách và lọc theo bán kính (Firestore không truy vấn được theo bán kính). |
| Chức năng liên quan (STT) | 27 |

**Phương thức**

| Chữ ký | Mô tả | STT |
| --- | --- | --- |
| `public double distanceKm(GeoPoint, GeoPoint)` | Khoảng cách theo công thức haversine. | 27 |
| `public boolean isWithinRadius(GeoPoint, GeoPoint, int)` | True khi điểm nằm trong bán kính (km) quanh tâm. | 27 |
| `public void sortByDistance(List<Listing>, GeoPoint)` | Sắp danh sách tin theo khoảng cách tới tâm, gần trước. | 27 |

**Dùng tới:** [`Listing`](model.md#listing)

**Được dùng bởi:** [`SearchViewModel`](viewmodel.md#searchviewmodel)

### FormatUtils

| Mục | Nội dung |
| --- | --- |
| Định danh | `com.nhom3.pubgapp.util.FormatUtils` |
| Loại | lớp |
| Lưu trữ | Không lưu |
| Trách nhiệm | Định dạng giá, thời gian; bỏ dấu tiếng Việt để tìm kiếm. |
| Chức năng liên quan (STT) | 23 |

**Phương thức**

| Chữ ký | Mô tả | STT |
| --- | --- | --- |
| `public String formatPrice(long)` | Định dạng tiền, ví dụ 1.500.000 đ; 0 hiện Miễn phí. |  |
| `public String formatTime(Timestamp)` | Thời gian tương đối, ví dụ 5 phút trước. |  |
| `public String removeAccents(String)` | Bỏ dấu tiếng Việt và viết thường. | 23 |
| `public List<String> toKeywords(String)` | Tách chuỗi đã bỏ dấu thành các từ, bỏ trùng. | 23 |

**Được dùng bởi:** [`SearchViewModel`](viewmodel.md#searchviewmodel)

### AutoReleaseWorker

| Mục | Nội dung |
| --- | --- |
| Định danh | `com.nhom3.pubgapp.util.AutoReleaseWorker` |
| Loại | lớp (background job) |
| Kế thừa | `androidx.work.Worker` |
| Lưu trữ | Không lưu |
| Trách nhiệm | Việc chạy nền định kỳ trên máy: tự chuyển giao dịch hết đếm ngược sang Đã nhận và giải ngân; tự hoàn tất giao dịch Đã nhận sau 3 ngày. |
| Chức năng liên quan (STT) | 84 |

**Phương thức**

| Chữ ký | Mô tả | STT |
| --- | --- | --- |
| `public Result doWork()` | Chạy mỗi 15 phút bằng WorkManager: `releaseDueDeals`, `completeDueDeals`, `expirePendingDeals` cho người đang đăng nhập; chờ kết quả bằng `Tasks.await` vì `doWork` là đồng bộ. Ngoại lệ duy nhất của luật tầng: lớp ở `util` gọi repository. |  |

**Quan hệ trên sơ đồ lớp**

- `AutoReleaseWorker` kế thừa `Worker`. Trang 7. ViewModel và tiện ích.

**Dùng tới:** [`DealRepository`](data.md#dealrepository)
