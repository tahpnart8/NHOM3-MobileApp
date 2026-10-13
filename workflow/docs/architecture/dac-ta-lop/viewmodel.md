# Đặc tả lớp: gói `com.nhom3.pubgapp.viewmodel`

Bản chữ của sơ đồ lớp cho gói này: 14 lớp. Mục lục và cách đọc ở [README.md](README.md).

Mỗi ViewModel phục vụ một nhóm màn hình, giữ trạng thái bằng LiveData để sống qua lần xoay màn hình, chỉ gọi repository và tiện ích. Không giữ tham chiếu tới Activity, Fragment hay View.

| Lớp | Loại | Trách nhiệm |
| --- | --- | --- |
| [`AuthViewModel`](#authviewmodel) | lớp (ViewModel) | Đăng ký (kèm đồng ý điều khoản và số CCCD), đăng nhập, quên mật khẩu, đổi mật khẩu, chọn danh mục quan tâm. |
| [`ProfileViewModel`](#profileviewmodel) | lớp (ViewModel) | Hồ sơ, ảnh, địa chỉ, thông tin định danh, cài đặt thông báo, theo dõi, danh sách đã lưu, yêu cầu xác minh, xóa tài khoản. |
| [`ShopViewModel`](#shopviewmodel) | lớp (ViewModel) | Kệ hàng của người bán: đăng, lưu nháp, sửa, ẩn hoặc hiện, đổi trạng thái, sắp xếp. |
| [`FeedViewModel`](#feedviewmodel) | lớp (ViewModel) | Kênh chung, kênh đang theo dõi, sắp xếp, mục Dành cho bạn. |
| [`SearchViewModel`](#searchviewmodel) | lớp (ViewModel) | Từ khóa, bộ lọc và lịch sử tìm kiếm. |
| [`ListingDetailViewModel`](#listingdetailviewmodel) | lớp (ViewModel) | Chi tiết tin, người bán, tin tương tự, giá tham khảo, lưu tin. |
| [`ChatViewModel`](#chatviewmodel) | lớp (ViewModel) | Danh sách chat, trạng thái hoạt động của người đối thoại, gửi chữ, ảnh và offer. |
| [`DealViewModel`](#dealviewmodel) | lớp (ViewModel) | Tạo, sửa, xác nhận, thanh toán, giao (kèm vận đơn), nhận, hoàn tất, hủy, đếm ngược và lịch sử giao dịch. |
| [`TrustViewModel`](#trustviewmodel) | lớp (ViewModel) | Đánh giá, báo cáo và khiếu nại phía người dùng. |
| [`NotificationViewModel`](#notificationviewmodel) | lớp (ViewModel) | Danh sách thông báo và số chưa đọc. |
| [`AdminUserViewModel`](#adminuserviewmodel) | lớp (ViewModel) | Admin: người dùng, hồ sơ, khóa, cảnh báo, xác minh, nhật ký. |
| [`AdminContentViewModel`](#admincontentviewmodel) | lớp (ViewModel) | Admin: tin bị báo cáo, ẩn và gỡ, từ khóa cấm, danh mục. |
| [`AdminDealViewModel`](#admindealviewmodel) | lớp (ViewModel) | Admin: theo dõi danh sách và chi tiết giao dịch. |
| [`AdminComplaintViewModel`](#admincomplaintviewmodel) | lớp (ViewModel) | Admin: xử lý khiếu nại, ra quyết định, ghi chú nội bộ, thông báo. |

### AuthViewModel

| Mục | Nội dung |
| --- | --- |
| Định danh | `com.nhom3.pubgapp.viewmodel.AuthViewModel` |
| Loại | lớp (ViewModel) |
| Kế thừa | `androidx.lifecycle.ViewModel` |
| Lưu trữ | Không lưu |
| Trách nhiệm | Đăng ký (kèm đồng ý điều khoản và số CCCD), đăng nhập, quên mật khẩu, đổi mật khẩu, chọn danh mục quan tâm. |
| Chức năng liên quan (STT) | 1 đến 3, 11, 35 |

**Thuộc tính**

| Tên | Kiểu | Truy cập | Ý nghĩa |
| --- | --- | --- | --- |
| `loggedIn` | `LiveData<Boolean>` | private | Đăng nhập thành công hay chưa; màn hình chuyển trang khi giá trị là true. |
| `needsProfile` | `LiveData<Boolean>` | private | Dữ liệu màn hình quan sát (`Boolean`); giữ qua lần xoay màn hình. |
| `error` | `LiveData<String>` | private | Câu báo lỗi để màn hình hiện; rỗng khi không có lỗi. |

**Phương thức**

| Chữ ký | Mô tả | STT |
| --- | --- | --- |
| `public void register(User user, String password, UserIdentity identity, List<String> categoryIds)` | Đăng ký email: `AuthRepository.signUp`, rồi `UserRepository.createProfile` (kèm `termsAcceptedAt`, danh mục quan tâm, thông tin định danh), rồi gửi email xác thực. | 1 đến 3, 35 |
| `public void completeProfile(User user, UserIdentity identity, List<String> categoryIds)` | Bước hoàn tất hồ sơ cho người đăng nhập Google lần đầu: `UserRepository.createProfile`. |  |
| `public void login(String email, String password)` | `AuthRepository.signIn`; tài khoản DELETED thì báo đã xóa và đăng xuất; khóa tạm hết hạn thì `clearExpiredLock`; đang khóa thì hiện `lockReason` và hạn rồi đăng xuất; còn lại đọc `admins/{uid}` và `SessionManager.start`. | 1 đến 3 |
| `public void loginWithGoogle(String idToken)` | Như `login` nhưng qua `signInWithGoogle`; lần đầu (chưa có hồ sơ) thì đặt `needsProfile` để mở bước hoàn tất hồ sơ. | 1 đến 3 |
| `public void restoreSession()` | Chạy khi mở ứng dụng nếu Firebase còn phiên: đọc lại `users/{uid}`, kiểm tra khóa và DELETED, đọc lại `admins/{uid}`, rồi `SessionManager.start`. |  |
| `public void resetPassword(String email)` | `AuthRepository.sendPasswordReset`. | 1 đến 3 |
| `public void changePassword(String current, String next)` | Gọi `AuthRepository.changePassword` ngoài luồng chính, đưa kết quả hoặc lỗi vào LiveData. | 11 |
| `public void logout()` | `AuthRepository.signOut`, xóa Room và `SessionManager`. |  |

**Quan hệ trên sơ đồ lớp**

- `AuthViewModel` kế thừa `ViewModel`. Trang 7. ViewModel và tiện ích.

**Dùng tới:** [`AdminRepository`](data.md#adminrepository), [`AuthRepository`](data.md#authrepository), [`SessionManager`](util.md#sessionmanager), [`User`](model.md#user), [`UserIdentity`](model.md#useridentity), [`UserRepository`](data.md#userrepository)

### ProfileViewModel

| Mục | Nội dung |
| --- | --- |
| Định danh | `com.nhom3.pubgapp.viewmodel.ProfileViewModel` |
| Loại | lớp (ViewModel) |
| Kế thừa | `androidx.lifecycle.ViewModel` |
| Lưu trữ | Không lưu |
| Trách nhiệm | Hồ sơ, ảnh, địa chỉ, thông tin định danh, cài đặt thông báo, theo dõi, danh sách đã lưu, yêu cầu xác minh, xóa tài khoản. |
| Chức năng liên quan (STT) | 4 đến 10, 12, 39 đến 42, 73 |

**Thuộc tính**

| Tên | Kiểu | Truy cập | Ý nghĩa |
| --- | --- | --- | --- |
| `profile` | `LiveData<User>` | private | Dữ liệu màn hình quan sát (`User`); giữ qua lần xoay màn hình. |
| `stats` | `LiveData<ProfileStats>` | private | Dữ liệu màn hình quan sát (`ProfileStats`); giữ qua lần xoay màn hình. |
| `addresses` | `LiveData<List<UserAddress>>` | private | Dữ liệu màn hình quan sát (`List<UserAddress>`); giữ qua lần xoay màn hình. |
| `savedListings` | `LiveData<List<Listing>>` | private | Dữ liệu màn hình quan sát (`List<Listing>`); giữ qua lần xoay màn hình. |
| `identity` | `LiveData<UserIdentity>` | private | Dữ liệu màn hình quan sát (`UserIdentity`); giữ qua lần xoay màn hình. |

**Phương thức**

| Chữ ký | Mô tả | STT |
| --- | --- | --- |
| `public void loadProfile(String uid)` | Đọc hồ sơ và `UserRepository.getStats`. | 7, 8 |
| `public void updateProfile(User user)` | Gọi `UserRepository.updateProfile` ngoài luồng chính, đưa kết quả hoặc lỗi vào LiveData. | 4 |
| `public void changeAvatar(Uri uri)` | Tạo ảnh 256 px qua `MediaRepository.makeThumb` rồi ghi `avatarBase64`. | 5 |
| `public void saveAddress(UserAddress address)` | Gọi `UserRepository.saveAddress` ngoài luồng chính, đưa kết quả hoặc lỗi vào LiveData. | 6 |
| `public void deleteAddress(String id)` | Gọi `UserRepository.deleteAddress` ngoài luồng chính, đưa kết quả hoặc lỗi vào LiveData. | 6 |
| `public void follow(String uid)` | Gọi `UserRepository.follow` ngoài luồng chính, đưa kết quả hoặc lỗi vào LiveData. | 39 |
| `public void unfollow(String uid)` | Gọi `UserRepository.unfollow` ngoài luồng chính, đưa kết quả hoặc lỗi vào LiveData. | 39 |
| `public void loadSaved()` | Đọc mã tin đã lưu rồi đọc từng tin. | 40 đến 42 |
| `public void requestVerification()` | Gọi `UserRepository.requestVerification` ngoài luồng chính, đưa kết quả hoặc lỗi vào LiveData. | 73 |
| `public void loadIdentity()` | `UserRepository.getIdentity` cho chính chủ. | 9 |
| `public void saveIdentity(UserIdentity identity)` | Gọi `UserRepository.saveIdentity` ngoài luồng chính, đưa kết quả hoặc lỗi vào LiveData. | 9 |
| `public void updateNotificationPrefs(Map<String,Boolean> prefs)` | Gọi `UserRepository.updateNotificationPrefs` ngoài luồng chính, đưa kết quả hoặc lỗi vào LiveData. | 10 |
| `public void deactivateAccount(String password)` | Xác thực lại, rồi `UserRepository.deactivate`, rồi `AuthRepository.deleteLogin`, rồi đăng xuất và xóa Room. | 12 |

**Quan hệ trên sơ đồ lớp**

- `ProfileViewModel` kế thừa `ViewModel`. Trang 7. ViewModel và tiện ích.

**Dùng tới:** [`AuthRepository`](data.md#authrepository), [`Listing`](model.md#listing), [`ListingRepository`](data.md#listingrepository), [`MediaRepository`](data.md#mediarepository), [`ProfileStats`](model.md#profilestats), [`User`](model.md#user), [`UserAddress`](model.md#useraddress), [`UserIdentity`](model.md#useridentity), [`UserRepository`](data.md#userrepository)

### ShopViewModel

| Mục | Nội dung |
| --- | --- |
| Định danh | `com.nhom3.pubgapp.viewmodel.ShopViewModel` |
| Loại | lớp (ViewModel) |
| Kế thừa | `androidx.lifecycle.ViewModel` |
| Lưu trữ | Không lưu |
| Trách nhiệm | Kệ hàng của người bán: đăng, lưu nháp, sửa, ẩn hoặc hiện, đổi trạng thái, sắp xếp. |
| Chức năng liên quan (STT) | 13 đến 15, 17, 22, 78 |

**Thuộc tính**

| Tên | Kiểu | Truy cập | Ý nghĩa |
| --- | --- | --- | --- |
| `myListings` | `LiveData<List<Listing>>` | private | Dữ liệu màn hình quan sát (`List<Listing>`); giữ qua lần xoay màn hình. |
| `keywordFlag` | `LiveData<Boolean>` | private | True khi tin vừa soạn chứa từ khóa cấm, để báo người bán biết tin sẽ bị gắn cờ. |

**Phương thức**

| Chữ ký | Mô tả | STT |
| --- | --- | --- |
| `public void loadMyListings(SortOrder order, String categoryId)` | Tin của chính mình, sắp theo cách chọn và lọc theo danh mục. | 14 |
| `public void createListing(Listing listing, List<Uri> imageUris)` | Kiểm tra từ khóa cấm, tải ảnh, rồi `ListingRepository.create`. | 13, 17, 78 |
| `public void saveDraft(Listing listing, List<Uri> imageUris)` | Gọi `ListingRepository.saveDraft` ngoài luồng chính, đưa kết quả hoặc lỗi vào LiveData. | 22 |
| `public void publishDraft(String id)` | Kiểm tra đủ thông tin (`Listing.publish`) rồi ghi. | 22 |
| `public void updateListing(Listing listing)` | `ListingRepository.update`. | 13 |
| `public void changeStatus(String id, ListingStatus status)` | Đổi trạng thái tin trong các bước người bán được phép (xem `trang-thai.md`). | 13 |
| `public void setHidden(String id, boolean hidden)` | Gọi `ListingRepository.setHidden` ngoài luồng chính, đưa kết quả hoặc lỗi vào LiveData. | 15 |
| `public void deleteListing(String id)` | `ListingRepository.delete`. | 13 |

**Quan hệ trên sơ đồ lớp**

- `ShopViewModel` kế thừa `ViewModel`. Trang 7. ViewModel và tiện ích.

**Dùng tới:** [`CategoryRepository`](data.md#categoryrepository), [`KeywordFilter`](util.md#keywordfilter), [`Listing`](model.md#listing), [`ListingRepository`](data.md#listingrepository), [`ListingStatus`](enums.md#listingstatus), [`MediaRepository`](data.md#mediarepository), [`SortOrder`](enums.md#sortorder)

### FeedViewModel

| Mục | Nội dung |
| --- | --- |
| Định danh | `com.nhom3.pubgapp.viewmodel.FeedViewModel` |
| Loại | lớp (ViewModel) |
| Kế thừa | `androidx.lifecycle.ViewModel` |
| Lưu trữ | Không lưu |
| Trách nhiệm | Kênh chung, kênh đang theo dõi, sắp xếp, mục Dành cho bạn. |
| Chức năng liên quan (STT) | 29, 30, 38 |

**Thuộc tính**

| Tên | Kiểu | Truy cập | Ý nghĩa |
| --- | --- | --- | --- |
| `feed` | `LiveData<List<Listing>>` | private | Dữ liệu màn hình quan sát (`List<Listing>`); giữ qua lần xoay màn hình. |
| `forYou` | `LiveData<List<Listing>>` | private | Dữ liệu màn hình quan sát (`List<Listing>`); giữ qua lần xoay màn hình. |

**Phương thức**

| Chữ ký | Mô tả | STT |
| --- | --- | --- |
| `public void loadFeed(boolean followingOnly, SortOrder order)` | Trang đầu của kênh chung hoặc kênh đang theo dõi. | 29, 30 |
| `public void loadNext()` | Trang kế tiếp. | 29 |
| `public void loadForYou()` | Đọc lịch sử xem, tìm kiếm và tin đã lưu rồi gọi `RecommendationEngine.forYou`. | 38 |
| `public void refresh()` | Tải lại từ đầu. |  |

**Quan hệ trên sơ đồ lớp**

- `FeedViewModel` kế thừa `ViewModel`. Trang 7. ViewModel và tiện ích.

**Dùng tới:** [`Listing`](model.md#listing), [`ListingRepository`](data.md#listingrepository), [`NetworkMonitor`](util.md#networkmonitor), [`RecommendationEngine`](util.md#recommendationengine), [`SortOrder`](enums.md#sortorder), [`UserRepository`](data.md#userrepository)

### SearchViewModel

| Mục | Nội dung |
| --- | --- |
| Định danh | `com.nhom3.pubgapp.viewmodel.SearchViewModel` |
| Loại | lớp (ViewModel) |
| Kế thừa | `androidx.lifecycle.ViewModel` |
| Lưu trữ | Không lưu |
| Trách nhiệm | Từ khóa, bộ lọc và lịch sử tìm kiếm. |
| Chức năng liên quan (STT) | 23, 38 |

**Thuộc tính**

| Tên | Kiểu | Truy cập | Ý nghĩa |
| --- | --- | --- | --- |
| `filter` | `SearchFilter` | private | Bộ lọc hiện tại; giữ trong ViewModel để không mất khi xoay màn hình. |
| `results` | `LiveData<List<Listing>>` | private | Dữ liệu màn hình quan sát (`List<Listing>`); giữ qua lần xoay màn hình. |
| `recentSearches` | `LiveData<List<String>>` | private | Dữ liệu màn hình quan sát (`List<String>`); giữ qua lần xoay màn hình. |

**Phương thức**

| Chữ ký | Mô tả | STT |
| --- | --- | --- |
| `public void setKeyword(String text)` | Đặt từ khóa vào bộ lọc. | 23 |
| `public void applyFilter(SearchFilter filter)` | Thay bộ lọc và tìm lại. |  |
| `public void search()` | Gọi `ListingRepository.search` ngoài luồng chính, đưa kết quả hoặc lỗi vào LiveData. | 23 |
| `public void loadRecentSearches()` | Đọc lịch sử tìm kiếm trên máy. | 38 |

**Quan hệ trên sơ đồ lớp**

- `SearchViewModel` kế thừa `ViewModel`. Trang 7. ViewModel và tiện ích.

**Dùng tới:** [`CategoryRepository`](data.md#categoryrepository), [`FormatUtils`](util.md#formatutils), [`GeoUtils`](util.md#geoutils), [`Listing`](model.md#listing), [`ListingRepository`](data.md#listingrepository), [`SearchFilter`](model.md#searchfilter)

### ListingDetailViewModel

| Mục | Nội dung |
| --- | --- |
| Định danh | `com.nhom3.pubgapp.viewmodel.ListingDetailViewModel` |
| Loại | lớp (ViewModel) |
| Kế thừa | `androidx.lifecycle.ViewModel` |
| Lưu trữ | Không lưu |
| Trách nhiệm | Chi tiết tin, người bán, tin tương tự, giá tham khảo, lưu tin. |
| Chức năng liên quan (STT) | 36, 37, 40 đến 42 |

**Thuộc tính**

| Tên | Kiểu | Truy cập | Ý nghĩa |
| --- | --- | --- | --- |
| `listing` | `LiveData<Listing>` | private | Dữ liệu màn hình quan sát (`Listing`); giữ qua lần xoay màn hình. |
| `seller` | `LiveData<User>` | private | Dữ liệu màn hình quan sát (`User`); giữ qua lần xoay màn hình. |
| `similar` | `LiveData<List<Listing>>` | private | Dữ liệu màn hình quan sát (`List<Listing>`); giữ qua lần xoay màn hình. |
| `saved` | `LiveData<Boolean>` | private | Tin đang xem đã được lưu hay chưa. |
| `priceRange` | `LiveData<long[]>` | private | Khoảng giá tham khảo [thấp, cao]; mảng rỗng khi không đủ dữ liệu. |

**Phương thức**

| Chữ ký | Mô tả | STT |
| --- | --- | --- |
| `public void load(String listingId)` | Đọc tin và người bán, ghi lịch sử xem. |  |
| `public void toggleSave()` | Lưu hoặc bỏ lưu tin. | 40 đến 42 |
| `public void loadSimilar()` | Tin tương tự qua `RecommendationEngine.similarTo`. | 36 |
| `public void loadPriceRange()` | Khoảng giá tham khảo qua `RecommendationEngine.priceRange`. | 37 |

**Quan hệ trên sơ đồ lớp**

- `ListingDetailViewModel` kế thừa `ViewModel`. Trang 7. ViewModel và tiện ích.

**Dùng tới:** [`Listing`](model.md#listing), [`ListingRepository`](data.md#listingrepository), [`RecommendationEngine`](util.md#recommendationengine), [`User`](model.md#user), [`UserRepository`](data.md#userrepository)

### ChatViewModel

| Mục | Nội dung |
| --- | --- |
| Định danh | `com.nhom3.pubgapp.viewmodel.ChatViewModel` |
| Loại | lớp (ViewModel) |
| Kế thừa | `androidx.lifecycle.ViewModel` |
| Lưu trữ | Không lưu |
| Trách nhiệm | Danh sách chat, trạng thái hoạt động của người đối thoại, gửi chữ, ảnh và offer. |
| Chức năng liên quan (STT) | 43, 44, 46 đến 49 |

**Thuộc tính**

| Tên | Kiểu | Truy cập | Ý nghĩa |
| --- | --- | --- | --- |
| `conversations` | `LiveData<List<Conversation>>` | private | Dữ liệu màn hình quan sát (`List<Conversation>`); giữ qua lần xoay màn hình. |
| `messages` | `LiveData<List<ChatMessage>>` | private | Dữ liệu màn hình quan sát (`List<ChatMessage>`); giữ qua lần xoay màn hình. |
| `partner` | `LiveData<User>` | private | Dữ liệu màn hình quan sát (`User`); giữ qua lần xoay màn hình. |

**Phương thức**

| Chữ ký | Mô tả | STT |
| --- | --- | --- |
| `public void loadConversations()` | Danh sách chat của người dùng. |  |
| `public void openConversation(String listingId)` | Mở hoặc tạo cuộc trò chuyện về một tin, bắt đầu lắng nghe tin nhắn. |  |
| `public void openConversationById(String id)` | Mở cuộc trò chuyện theo mã (người bán, hoặc từ thông báo). |  |
| `public void loadOlder()` | Tải thêm tin nhắn cũ. |  |
| `public void loadPartner()` | Đọc hồ sơ bên còn lại để hiện tên và trạng thái hoạt động. | 46 |
| `public void sendText(String text)` | Gọi `ChatRepository.sendText` ngoài luồng chính, đưa kết quả hoặc lỗi vào LiveData. | 43, 44 |
| `public void sendImage(Uri uri)` | `ChatRepository.sendImage`. | 43, 44 |
| `public void makeOffer(Offer offer)` | `ChatRepository.sendOffer`. | 47 đến 49 |
| `public void respondToOffer(String offerId, boolean accept)` | Gọi `ChatRepository.respondToOffer` ngoài luồng chính, đưa kết quả hoặc lỗi vào LiveData. | 47 đến 49 |

**Quan hệ trên sơ đồ lớp**

- `ChatViewModel` kế thừa `ViewModel`. Trang 7. ViewModel và tiện ích.

**Dùng tới:** [`ChatMessage`](model.md#chatmessage), [`ChatRepository`](data.md#chatrepository), [`Conversation`](model.md#conversation), [`MediaRepository`](data.md#mediarepository), [`Offer`](model.md#offer), [`User`](model.md#user), [`UserRepository`](data.md#userrepository)

### DealViewModel

| Mục | Nội dung |
| --- | --- |
| Định danh | `com.nhom3.pubgapp.viewmodel.DealViewModel` |
| Loại | lớp (ViewModel) |
| Kế thừa | `androidx.lifecycle.ViewModel` |
| Lưu trữ | Không lưu |
| Trách nhiệm | Tạo, sửa, xác nhận, thanh toán, giao (kèm vận đơn), nhận, hoàn tất, hủy, đếm ngược và lịch sử giao dịch. |
| Chức năng liên quan (STT) | 50 đến 58, 84 |

**Thuộc tính**

| Tên | Kiểu | Truy cập | Ý nghĩa |
| --- | --- | --- | --- |
| `deal` | `LiveData<Deal>` | private | Dữ liệu màn hình quan sát (`Deal`); giữ qua lần xoay màn hình. |
| `deals` | `LiveData<List<Deal>>` | private | Dữ liệu màn hình quan sát (`List<Deal>`); giữ qua lần xoay màn hình. |
| `payments` | `LiveData<List<Payment>>` | private | Dữ liệu màn hình quan sát (`List<Payment>`); giữ qua lần xoay màn hình. |

**Phương thức**

| Chữ ký | Mô tả | STT |
| --- | --- | --- |
| `public void loadDeal(String id)` | `DealRepository.getById` và `getPayment`. | 50 |
| `public void updateTerms(Deal deal)` | Gọi `DealRepository.updateTerms` ngoài luồng chính, đưa kết quả hoặc lỗi vào LiveData. | 51 |
| `public void confirm()` | Gọi `DealRepository.confirm` ngoài luồng chính, đưa kết quả hoặc lỗi vào LiveData. | 52 |
| `public void pay()` | Gọi `DealRepository.pay` ngoài luồng chính, đưa kết quả hoặc lỗi vào LiveData. | 56 |
| `public void loadMyReview()` | `ReviewRepository.getByDeal` để biết người đang xem đã đánh giá chưa (ẩn hoặc hiện nút Viết đánh giá). |  |
| `public void markShipped(String carrier, String trackingCode, Uri photoUri)` | Gọi `DealRepository.markShipped` ngoài luồng chính, đưa kết quả hoặc lỗi vào LiveData. | 54 |
| `public void markReceived()` | Gọi `DealRepository.markReceived` ngoài luồng chính, đưa kết quả hoặc lỗi vào LiveData. | 53 |
| `public void checkAutoRelease()` | Gọi khi mở ứng dụng: chạy `releaseDueDeals`, `completeDueDeals`, `expirePendingDeals` ngay, không chờ việc chạy nền. | 84 |
| `public void cancel(String reason)` | Gọi `DealRepository.cancel` ngoài luồng chính, đưa kết quả hoặc lỗi vào LiveData. | 55 |
| `public void loadHistory()` | `DealRepository.getMyDeals`. | 57 |
| `public void loadPayments()` | `DealRepository.getPayments`. | 58 |

**Quan hệ trên sơ đồ lớp**

- `DealViewModel` kế thừa `ViewModel`. Trang 7. ViewModel và tiện ích.

**Dùng tới:** [`Deal`](model.md#deal), [`DealRepository`](data.md#dealrepository), [`MediaRepository`](data.md#mediarepository), [`Payment`](model.md#payment), [`ReviewRepository`](data.md#reviewrepository), [`UserRepository`](data.md#userrepository)

### TrustViewModel

| Mục | Nội dung |
| --- | --- |
| Định danh | `com.nhom3.pubgapp.viewmodel.TrustViewModel` |
| Loại | lớp (ViewModel) |
| Kế thừa | `androidx.lifecycle.ViewModel` |
| Lưu trữ | Không lưu |
| Trách nhiệm | Đánh giá, báo cáo và khiếu nại phía người dùng. |
| Chức năng liên quan (STT) | 59 đến 66 |

**Thuộc tính**

| Tên | Kiểu | Truy cập | Ý nghĩa |
| --- | --- | --- | --- |
| `reviews` | `LiveData<List<Review>>` | private | Dữ liệu màn hình quan sát (`List<Review>`); giữ qua lần xoay màn hình. |
| `reports` | `LiveData<List<Report>>` | private | Dữ liệu màn hình quan sát (`List<Report>`); giữ qua lần xoay màn hình. |
| `complaints` | `LiveData<List<Complaint>>` | private | Dữ liệu màn hình quan sát (`List<Complaint>`); giữ qua lần xoay màn hình. |

**Phương thức**

| Chữ ký | Mô tả | STT |
| --- | --- | --- |
| `public void submitReview(Review review)` | `ReviewRepository.create`. | 59 |
| `public void loadReviews(boolean received)` | Đánh giá đã nhận hoặc đã viết, tùy tham số. | 60 |
| `public void submitReport(Report report)` | `ReportRepository.create`. | 61 |
| `public void cancelReport(String id)` | `ReportRepository.cancel`. | 62 |
| `public void loadMyReports()` | `ReportRepository.getMyReports`. | 63 |
| `public void exportReports()` | `ReportRepository.exportCsv` rồi mở hộp chia sẻ tệp. | 63 |
| `public void submitComplaint(Complaint complaint, List<Uri> evidenceUris)` | Tải ảnh bằng chứng (tối thiểu 1) rồi `ComplaintRepository.create`. | 64 |
| `public void addEvidence(String id, List<Uri> evidenceUris)` | Lưu ảnh qua `MediaRepository`, rồi `ComplaintRepository.addEvidence`. | 64 |
| `public void loadMyComplaints()` | `ComplaintRepository.getMyComplaints`. | 65 |
| `public void withdrawComplaint(String id)` | `ComplaintRepository.withdraw`. | 66 |

**Quan hệ trên sơ đồ lớp**

- `TrustViewModel` kế thừa `ViewModel`. Trang 7. ViewModel và tiện ích.

**Dùng tới:** [`Complaint`](model.md#complaint), [`ComplaintRepository`](data.md#complaintrepository), [`MediaRepository`](data.md#mediarepository), [`Report`](model.md#report), [`ReportRepository`](data.md#reportrepository), [`Review`](model.md#review), [`ReviewRepository`](data.md#reviewrepository)

### NotificationViewModel

| Mục | Nội dung |
| --- | --- |
| Định danh | `com.nhom3.pubgapp.viewmodel.NotificationViewModel` |
| Loại | lớp (ViewModel) |
| Kế thừa | `androidx.lifecycle.ViewModel` |
| Lưu trữ | Không lưu |
| Trách nhiệm | Danh sách thông báo và số chưa đọc. |
| Chức năng liên quan (STT) | 67, 91 |

**Thuộc tính**

| Tên | Kiểu | Truy cập | Ý nghĩa |
| --- | --- | --- | --- |
| `notifications` | `LiveData<List<AppNotification>>` | private | Dữ liệu màn hình quan sát (`List<AppNotification>`); giữ qua lần xoay màn hình. |
| `unreadCount` | `LiveData<Integer>` | private | Số thông báo chưa đọc, hiện trên biểu tượng chuông. |

**Phương thức**

| Chữ ký | Mô tả | STT |
| --- | --- | --- |
| `public void load()` | Đọc thông báo và đếm số chưa đọc. | 67, 91 |
| `public void markRead(String id)` | Gọi `NotificationRepository.markRead` ngoài luồng chính, đưa kết quả hoặc lỗi vào LiveData. | 67, 91 |

**Quan hệ trên sơ đồ lớp**

- `NotificationViewModel` kế thừa `ViewModel`. Trang 7. ViewModel và tiện ích.

**Dùng tới:** [`AppNotification`](model.md#appnotification), [`NotificationRepository`](data.md#notificationrepository)

### AdminUserViewModel

| Mục | Nội dung |
| --- | --- |
| Định danh | `com.nhom3.pubgapp.viewmodel.AdminUserViewModel` |
| Loại | lớp (ViewModel) |
| Kế thừa | `androidx.lifecycle.ViewModel` |
| Lưu trữ | Không lưu |
| Trách nhiệm | Admin: người dùng, hồ sơ, khóa, cảnh báo, xác minh, nhật ký. |
| Chức năng liên quan (STT) | 68 đến 74 |

**Thuộc tính**

| Tên | Kiểu | Truy cập | Ý nghĩa |
| --- | --- | --- | --- |
| `users` | `LiveData<List<User>>` | private | Dữ liệu màn hình quan sát (`List<User>`); giữ qua lần xoay màn hình. |
| `warnings` | `LiveData<List<Warning>>` | private | Dữ liệu màn hình quan sát (`List<Warning>`); giữ qua lần xoay màn hình. |
| `logs` | `LiveData<List<ActivityLog>>` | private | Dữ liệu màn hình quan sát (`List<ActivityLog>`); giữ qua lần xoay màn hình. |

**Phương thức**

| Chữ ký | Mô tả | STT |
| --- | --- | --- |
| `public void filterUsers(UserStatus status, Timestamp from, Timestamp to, String province)` | `AdminRepository.listUsers`. | 68 |
| `public void loadProfile(String userId)` | Hồ sơ, thông tin định danh, tin đăng, giao dịch, đánh giá và cảnh báo của một người dùng. | 69 |
| `public void lock(String userId, Timestamp until, String reason)` | `AdminRepository.lockUser`. | 70, 71 |
| `public void unlock(String userId)` | `AdminRepository.unlockUser`. | 70, 71 |
| `public void warn(String userId, String reason, Timestamp until)` | `AdminRepository.warnUser`; bậc 3 thì gỡ hàng loạt tin của tài khoản. | 72 |
| `public void loadVerificationRequests()` | `AdminRepository.getVerificationRequests`. | 73 |
| `public void reviewVerification(String userId, boolean approve, String note)` | Gọi `AdminRepository.reviewVerification` ngoài luồng chính, đưa kết quả hoặc lỗi vào LiveData. | 73 |
| `public void loadActivityLog(String userId)` | `AdminRepository.getActivityLogs`. | 74 |

**Quan hệ trên sơ đồ lớp**

- `AdminUserViewModel` kế thừa `ViewModel`. Trang 7. ViewModel và tiện ích.

**Dùng tới:** [`ActivityLog`](model.md#activitylog), [`AdminRepository`](data.md#adminrepository), [`DealRepository`](data.md#dealrepository), [`ListingRepository`](data.md#listingrepository), [`ReviewRepository`](data.md#reviewrepository), [`User`](model.md#user), [`UserRepository`](data.md#userrepository), [`UserStatus`](enums.md#userstatus), [`Warning`](model.md#warning)

### AdminContentViewModel

| Mục | Nội dung |
| --- | --- |
| Định danh | `com.nhom3.pubgapp.viewmodel.AdminContentViewModel` |
| Loại | lớp (ViewModel) |
| Kế thừa | `androidx.lifecycle.ViewModel` |
| Lưu trữ | Không lưu |
| Trách nhiệm | Admin: tin bị báo cáo, ẩn và gỡ, từ khóa cấm, danh mục. |
| Chức năng liên quan (STT) | 75 đến 80 |

**Thuộc tính**

| Tên | Kiểu | Truy cập | Ý nghĩa |
| --- | --- | --- | --- |
| `reportedListings` | `LiveData<List<Listing>>` | private | Dữ liệu màn hình quan sát (`List<Listing>`); giữ qua lần xoay màn hình. |
| `keywords` | `LiveData<List<BannedKeyword>>` | private | Dữ liệu màn hình quan sát (`List<BannedKeyword>`); giữ qua lần xoay màn hình. |
| `categories` | `LiveData<List<Category>>` | private | Dữ liệu màn hình quan sát (`List<Category>`); giữ qua lần xoay màn hình. |

**Phương thức**

| Chữ ký | Mô tả | STT |
| --- | --- | --- |
| `public void loadReportedListings()` | Báo cáo OPEN nhắm vào tin, gom theo tin kèm số lượt và lý do. | 75 |
| `public void closeReports(String listingId, boolean dismissed)` | `ReportRepository.closeByTarget`. | 75 |
| `public void hide(String listingId, String reason)` | `AdminRepository.hideListing`. | 76 |
| `public void restore(String listingId)` | `AdminRepository.restoreListing`. | 76 |
| `public void removeAllOf(String userId)` | `AdminRepository.removeAllListingsOf`. | 77 |
| `public void clearFlag(String listingId)` | Gọi `AdminRepository.clearFlag` ngoài luồng chính, đưa kết quả hoặc lỗi vào LiveData. | 78 |
| `public void loadKeywords()` | `AdminRepository.getKeywords`. |  |
| `public void addKeyword(String word)` | Gọi `AdminRepository.addKeyword` ngoài luồng chính, đưa kết quả hoặc lỗi vào LiveData. | 78 |
| `public void removeKeyword(String id)` | Gọi `AdminRepository.removeKeyword` ngoài luồng chính, đưa kết quả hoặc lỗi vào LiveData. | 78 |
| `public void saveCategory(Category category)` | Thêm hoặc sửa danh mục. | 79 |
| `public void deleteCategory(String id)` | `CategoryRepository.delete`. | 79 |
| `public void setConditionFields(String id, List<String> fields)` | Ghi `conditionFields` của danh mục. | 80 |

**Quan hệ trên sơ đồ lớp**

- `AdminContentViewModel` kế thừa `ViewModel`. Trang 7. ViewModel và tiện ích.

**Dùng tới:** [`AdminRepository`](data.md#adminrepository), [`BannedKeyword`](model.md#bannedkeyword), [`Category`](model.md#category), [`CategoryRepository`](data.md#categoryrepository), [`Listing`](model.md#listing), [`ListingRepository`](data.md#listingrepository), [`ReportRepository`](data.md#reportrepository)

### AdminDealViewModel

| Mục | Nội dung |
| --- | --- |
| Định danh | `com.nhom3.pubgapp.viewmodel.AdminDealViewModel` |
| Loại | lớp (ViewModel) |
| Kế thừa | `androidx.lifecycle.ViewModel` |
| Lưu trữ | Không lưu |
| Trách nhiệm | Admin: theo dõi danh sách và chi tiết giao dịch. |
| Chức năng liên quan (STT) | 81, 82 |

**Thuộc tính**

| Tên | Kiểu | Truy cập | Ý nghĩa |
| --- | --- | --- | --- |
| `deals` | `LiveData<List<Deal>>` | private | Dữ liệu màn hình quan sát (`List<Deal>`); giữ qua lần xoay màn hình. |
| `selected` | `LiveData<Deal>` | private | Mục admin đang mở. |

**Phương thức**

| Chữ ký | Mô tả | STT |
| --- | --- | --- |
| `public void loadDeals(DealStatus status)` | `DealRepository.listAll`. | 81 |
| `public void loadDetail(String dealId)` | Một giao dịch kèm `statusHistory` và khoản ký quỹ. | 82 |

**Quan hệ trên sơ đồ lớp**

- `AdminDealViewModel` kế thừa `ViewModel`. Trang 7. ViewModel và tiện ích.

**Dùng tới:** [`Deal`](model.md#deal), [`DealRepository`](data.md#dealrepository), [`DealStatus`](enums.md#dealstatus)

### AdminComplaintViewModel

| Mục | Nội dung |
| --- | --- |
| Định danh | `com.nhom3.pubgapp.viewmodel.AdminComplaintViewModel` |
| Loại | lớp (ViewModel) |
| Kế thừa | `androidx.lifecycle.ViewModel` |
| Lưu trữ | Không lưu |
| Trách nhiệm | Admin: xử lý khiếu nại, ra quyết định, ghi chú nội bộ, thông báo. |
| Chức năng liên quan (STT) | 87 đến 90 |

**Thuộc tính**

| Tên | Kiểu | Truy cập | Ý nghĩa |
| --- | --- | --- | --- |
| `complaints` | `LiveData<List<Complaint>>` | private | Dữ liệu màn hình quan sát (`List<Complaint>`); giữ qua lần xoay màn hình. |
| `selected` | `LiveData<Complaint>` | private | Mục admin đang mở. |

**Phương thức**

| Chữ ký | Mô tả | STT |
| --- | --- | --- |
| `public void loadComplaints(ComplaintStatus status, Priority priority)` | `ComplaintRepository.getByStatus`. | 87 |
| `public void loadDetail(String id)` | `ComplaintRepository.getDetail` kèm lịch sử chat của giao dịch. | 88 |
| `public void setPriority(String id, Priority priority)` | `ComplaintRepository.setPriority`. |  |
| `public void decide(String id, ComplaintDecision decision, String message)` | `ComplaintRepository.decide` (repository ghi thông báo cho hai bên). | 89 |
| `public void saveNote(String id, String note)` | `ComplaintRepository.saveAdminNote`. | 90 |

**Quan hệ trên sơ đồ lớp**

- `AdminComplaintViewModel` kế thừa `ViewModel`. Trang 7. ViewModel và tiện ích.

**Dùng tới:** [`ChatRepository`](data.md#chatrepository), [`Complaint`](model.md#complaint), [`ComplaintDecision`](enums.md#complaintdecision), [`ComplaintRepository`](data.md#complaintrepository), [`ComplaintStatus`](enums.md#complaintstatus), [`DealRepository`](data.md#dealrepository), [`Priority`](enums.md#priority)
