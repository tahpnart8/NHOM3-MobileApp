# Đặc tả lớp: gói `com.nhom3.pubgapp.viewmodel`

Bản chữ của sơ đồ lớp cho gói này: 14 ViewModel và lớp phụ `ListenerSet`. Mục lục và cách đọc ở [README.md](README.md).

Mỗi ViewModel phục vụ một nhóm màn hình, giữ trạng thái bằng LiveData để sống qua lần xoay màn hình, chỉ gọi repository và tiện ích. Không giữ tham chiếu tới Activity, Fragment hay View.

**Quy ước getter (quyết định 2026-10-22).** Mọi thuộc tính LiveData dưới đây là `private` và có đúng một getter công khai tên `get<TênThuộcTính>()` viết hoa chữ đầu, trả `LiveData<...>` chứ không trả `MutableLiveData`. Ví dụ `error` có `getError()`, `loading` có `getLoading()`, `myListings` có `getMyListings()`. Bảng dưới đây không liệt kê từng getter vì chúng suy ra được từ quy ước này; luật R16 coi quy ước này là dòng tài liệu định nghĩa chúng. Cùng quy ước áp cho `NetworkMonitor.online`, tức `getOnline()`.

**Ba thuộc tính mọi ViewModel đều có.** `loading` để màn hình hiện trạng thái đang tải và khóa nút gửi; `error` là câu lỗi tiếng Việt, màn hình xóa sau khi đã hiện để xoay màn hình không hiện lại; `done` bật lên đúng một lần khi một thao tác ghi xong rồi màn hình đặt lại false. Bốn trạng thái mà [`conventions/giao-dien.md`](../../conventions/giao-dien.md) mục 6 đòi (đang tải, có dữ liệu, rỗng, lỗi) đọc từ ba thuộc tính này, không phải từ cờ riêng trong Fragment.

**Dữ liệu theo thời gian thực (quyết định 2026-10-28).** Các hàm `load...` của màn có dữ liệu do người khác đổi (hồ sơ, xác thực, chi tiết tin, chi tiết và lịch sử giao dịch, danh sách chat, thông báo, báo cáo và khiếu nại của tôi, và các hàng chờ của admin) gắn listener qua hàm `observe...` của repository thay vì đọc một lần. Listener nằm trong `ListenerSet` của ViewModel: gọi lại `load...` với cùng đối tượng thì không gắn thêm, đổi đối tượng thì thay listener cũ, `onCleared` gỡ hết. Callback của listener có thể chạy nhiều lần; `loading` chỉ tắt ở lần trả lời đầu. Listener bị lỗi thì Firestore dừng nó, ViewModel quên nó để lần mở sau (hoặc nút Thử lại) gắn lại.

| Lớp | Loại | Trách nhiệm |
| --- | --- | --- |
| [`AuthViewModel`](#authviewmodel) | lớp (ViewModel) | Đăng ký (kèm đồng ý điều khoản và số CCCD), đăng nhập, quên mật khẩu, đổi mật khẩu, chọn danh mục quan tâm. |
| [`ProfileViewModel`](#profileviewmodel) | lớp (ViewModel) | Hồ sơ, ảnh, địa chỉ, thông tin định danh, cài đặt thông báo, theo dõi, danh sách đã lưu, yêu cầu xác thực tài khoản, xóa tài khoản. |
| [`ShopViewModel`](#shopviewmodel) | lớp (ViewModel) | Kệ hàng của người bán: đăng, lưu nháp, sửa, ẩn hoặc hiện, đổi trạng thái, sắp xếp. |
| [`FeedViewModel`](#feedviewmodel) | lớp (ViewModel) | Kênh chung, kênh đang theo dõi, sắp xếp, mục Dành cho bạn. |
| [`SearchViewModel`](#searchviewmodel) | lớp (ViewModel) | Từ khóa, bộ lọc, lịch sử tìm kiếm, và kết quả tìm người dùng. |
| [`ListingDetailViewModel`](#listingdetailviewmodel) | lớp (ViewModel) | Chi tiết tin, người bán, tin tương tự, giá tham khảo, lưu tin. |
| [`ChatViewModel`](#chatviewmodel) | lớp (ViewModel) | Danh sách chat, trạng thái hoạt động của người đối thoại, gửi chữ, ảnh và offer. |
| [`DealViewModel`](#dealviewmodel) | lớp (ViewModel) | Tạo, sửa, xác nhận, thanh toán, giao (kèm vận đơn), nhận, hoàn tất, hủy, đếm ngược và lịch sử giao dịch. |
| [`TrustViewModel`](#trustviewmodel) | lớp (ViewModel) | Đánh giá, báo cáo và khiếu nại phía người dùng. |
| [`NotificationViewModel`](#notificationviewmodel) | lớp (ViewModel) | Danh sách thông báo và số chưa đọc. |
| [`AdminUserViewModel`](#adminuserviewmodel) | lớp (ViewModel) | Admin: người dùng, hồ sơ, khóa, cảnh báo, xác thực tài khoản, nhật ký. |
| [`AdminContentViewModel`](#admincontentviewmodel) | lớp (ViewModel) | Admin: tin bị báo cáo, ẩn và gỡ, từ khóa cấm, danh mục. |
| [`AdminDealViewModel`](#admindealviewmodel) | lớp (ViewModel) | Admin: theo dõi danh sách và chi tiết giao dịch. |
| [`AdminComplaintViewModel`](#admincomplaintviewmodel) | lớp (ViewModel) | Admin: xử lý khiếu nại, ra quyết định, ghi chú nội bộ, thông báo. |
| [`ListenerSet`](#listenerset) | lớp (tiện ích) | Giữ các `ListenerRegistration` của một ViewModel theo khóa; gỡ hết ở `onCleared`. |

### AuthViewModel

| Mục | Nội dung |
| --- | --- |
| Định danh | `com.nhom3.pubgapp.viewmodel.AuthViewModel` |
| Loại | lớp (ViewModel) |
| Kế thừa | `androidx.lifecycle.ViewModel` |
| Lưu trữ | Không lưu |
| Trách nhiệm | Đăng ký (kèm đồng ý điều khoản và số CCCD), đăng nhập, quên mật khẩu, đổi mật khẩu, chọn danh mục quan tâm. |
| Chức năng liên quan (STT) | 1 đến 3, 11, 35, 104, 106 |

**Thuộc tính**

| Tên | Kiểu | Truy cập | Ý nghĩa |
| --- | --- | --- | --- |
| `loggedIn` | `LiveData<Boolean>` | private | Đăng nhập thành công hay chưa; màn hình chuyển trang khi giá trị là true. |
| `needsProfile` | `LiveData<Boolean>` | private | Dữ liệu màn hình quan sát (`Boolean`); giữ qua lần xoay màn hình. |
| `signUpStep` | `LiveData<Integer>` | private | Bước đăng ký đang đứng: 1 là email và mật khẩu, 2 là màn chờ xác thực, 3 là hồ sơ. |
| `emailVerified` | `LiveData<Boolean>` | private | Kết quả lần kiểm tra gần nhất của `AuthRepository.reloadUser`; true thì mở được bước 3. |
| `categories` | `LiveData<List<Category>>` | private | Danh mục để người dùng chọn danh mục quan tâm ở bước 3 (STT 35). |
| `loading` | `LiveData<Boolean>` | private | True trong lúc đang gọi mạng hoặc đọc đĩa; màn hình hiện trạng thái đang tải và khóa nút gửi. |
| `error` | `LiveData<String>` | private | Câu lỗi tiếng Việt cho màn hình hiện; rỗng khi không có lỗi. Màn hình xóa sau khi đã hiện, để xoay màn hình không hiện lại. |
| `done` | `LiveData<Boolean>` | private | Bật lên đúng một lần khi một thao tác ghi xong, để màn hình đóng hoặc quay lại; màn hình đặt lại false sau khi dùng. |

**Phương thức**

| Chữ ký | Mô tả | STT |
| --- | --- | --- |
| `public void startSignUp(String email, String password)` | Tạo tài khoản bằng email và mật khẩu rồi gửi liên kết xác thực; đặt `signUpStep` bằng 2. | 1 đến 3 |
| `public void resendVerificationLink()` | Gửi lại liên kết xác thực; màn chờ khóa nút trong ít giây để tránh bấm liên tục. | 1 đến 3 |
| `public void checkEmailVerified()` | Gọi `AuthRepository.reloadUser`; nếu đã xác thực thì đặt `signUpStep` bằng 3. | 1 đến 3 |
| `public void completeProfile(User user, UserIdentity identity, List<String> categoryIds)` | Bước hoàn tất hồ sơ cho người đăng nhập Google lần đầu: `UserRepository.createProfile`. | 1 đến 3, 35 |
| `public void loadCategories()` | `CategoryRepository.getAll` cho bước chọn danh mục quan tâm. | 35 |
| `public void login(String email, String password)` | `AuthRepository.signIn`; tài khoản DELETED thì báo đã xóa và đăng xuất; khóa tạm hết hạn thì `clearExpiredLock`; đang khóa thì hiện `lockReason` và hạn rồi đăng xuất; còn lại đọc `admins/{uid}` và `SessionManager.start`. | 1 đến 3, 106 |
| `public void loginWithGoogle(String idToken)` | Như `login` nhưng qua `signInWithGoogle`; lần đầu (chưa có hồ sơ) thì đặt `needsProfile` để mở bước hoàn tất hồ sơ. | 1 đến 3 |
| `public void loginWithGoogleBrowser(Activity activity)` | Đường mà nút "Đăng nhập bằng Google" dùng: gọi `signInWithGoogleBrowser`, kết thúc giống `loginWithGoogle` (vào phiên, hoặc đặt `needsProfile` lần đầu). `Activity` chỉ được chuyển tiếp, ViewModel không giữ lại. | 1 đến 3 |
| `public void restoreSession()` | Chạy khi mở ứng dụng nếu Firebase còn phiên: đọc lại `users/{uid}`, kiểm tra khóa và DELETED, đọc lại `admins/{uid}`, rồi `SessionManager.start`. | 106 |
| `public void resetPassword(String email)` | `AuthRepository.sendPasswordReset`. | 1 đến 3 |
| `public void changePassword(String current, String next)` | Gọi `AuthRepository.changePassword` ngoài luồng chính, đưa kết quả hoặc lỗi vào LiveData. | 11 |
| `public boolean isGoogleAccount()` | `AuthRepository.isGoogleAccount()`; màn A04 và A05 ẩn mục đổi mật khẩu khi true. |  |
| `public void logout()` | `AuthRepository.signOut`, xóa Room và `SessionManager`. | 104 |
| `public void clearError()` | Đặt `error` về rỗng sau khi màn hình đã hiện câu lỗi, để xoay màn hình không hiện lại. |  |
| `public void clearDone()` | Đặt `done` về false sau khi màn hình đã phản ứng (đóng hộp thoại, quay lại), để không chạy hai lần. |  |

**Quan hệ trên sơ đồ lớp**

- `AuthViewModel` kế thừa `ViewModel`. Trang 7. ViewModel và tiện ích.

**Dùng tới:** [`AdminRepository`](data.md#adminrepository), [`AuthRepository`](data.md#authrepository), [`Category`](model.md#category), [`CategoryRepository`](data.md#categoryrepository), [`SessionManager`](util.md#sessionmanager), [`User`](model.md#user), [`UserIdentity`](model.md#useridentity), [`UserRepository`](data.md#userrepository)

### ProfileViewModel

| Mục | Nội dung |
| --- | --- |
| Định danh | `com.nhom3.pubgapp.viewmodel.ProfileViewModel` |
| Loại | lớp (ViewModel) |
| Kế thừa | `androidx.lifecycle.ViewModel` |
| Lưu trữ | Không lưu |
| Trách nhiệm | Hồ sơ, ảnh, địa chỉ, thông tin định danh, cài đặt thông báo, theo dõi, danh sách đã lưu, yêu cầu xác thực tài khoản, xóa tài khoản. |
| Chức năng liên quan (STT) | 4 đến 10, 12, 39 đến 42, 73 |

**Thuộc tính**

| Tên | Kiểu | Truy cập | Ý nghĩa |
| --- | --- | --- | --- |
| `profile` | `LiveData<User>` | private | Dữ liệu màn hình quan sát (`User`); giữ qua lần xoay màn hình. |
| `stats` | `LiveData<ProfileStats>` | private | Dữ liệu màn hình quan sát (`ProfileStats`); giữ qua lần xoay màn hình. |
| `addresses` | `LiveData<List<UserAddress>>` | private | Dữ liệu màn hình quan sát (`List<UserAddress>`); giữ qua lần xoay màn hình. |
| `savedListings` | `LiveData<List<Listing>>` | private | Dữ liệu màn hình quan sát (`List<Listing>`); giữ qua lần xoay màn hình. |
| `userListings` | `LiveData<List<Listing>>` | private | Tin đang bán của người đang xem, dùng cho trang cá nhân D02 và hồ sơ uy tín D03. |
| `identity` | `LiveData<UserIdentity>` | private | Dữ liệu màn hình quan sát (`UserIdentity`); giữ qua lần xoay màn hình. |
| `verification` | `LiveData<SellerVerification>` | private | Yêu cầu xác thực của chính người dùng, để D01 hiện trạng thái, lý do từ chối và số lần gửi lại còn lại. |
| `following` | `LiveData<Boolean>` | private | True khi người đang đăng nhập đang theo dõi người được xem; nút trên D02 đọc cờ này. |
| `loading` | `LiveData<Boolean>` | private | True trong lúc đang gọi mạng hoặc đọc đĩa; màn hình hiện trạng thái đang tải và khóa nút gửi. |
| `error` | `LiveData<String>` | private | Câu lỗi tiếng Việt cho màn hình hiện; rỗng khi không có lỗi. Màn hình xóa sau khi đã hiện, để xoay màn hình không hiện lại. |
| `done` | `LiveData<Boolean>` | private | Bật lên đúng một lần khi một thao tác ghi xong, để màn hình đóng hoặc quay lại; màn hình đặt lại false sau khi dùng. |

**Phương thức**

| Chữ ký | Mô tả | STT |
| --- | --- | --- |
| `public void loadProfile(String uid)` | Đọc hồ sơ và `UserRepository.getStats`. | 7, 8 |
| `public void updateProfile(User user)` | Gọi `UserRepository.updateProfile` ngoài luồng chính, đưa kết quả hoặc lỗi vào LiveData. | 4 |
| `public void changeAvatar(Uri uri)` | Tạo ảnh 256 px qua `MediaRepository.makeThumb` rồi ghi `avatarBase64`. | 5 |
| `public void loadAddresses()` | `UserRepository.getAddresses`. |  |
| `public void saveAddress(UserAddress address)` | Gọi `UserRepository.saveAddress` ngoài luồng chính, đưa kết quả hoặc lỗi vào LiveData. | 6 |
| `public void deleteAddress(String id)` | Gọi `UserRepository.deleteAddress` ngoài luồng chính, đưa kết quả hoặc lỗi vào LiveData. | 6 |
| `public void follow(String uid)` | Gọi `UserRepository.follow` ngoài luồng chính, đưa kết quả hoặc lỗi vào LiveData. | 39 |
| `public void unfollow(String uid)` | Gọi `UserRepository.unfollow` ngoài luồng chính, đưa kết quả hoặc lỗi vào LiveData. | 39 |
| `public void loadSaved()` | Đọc mã tin đã lưu rồi đọc từng tin. | 40 đến 42 |
| `public void unsaveListing(String listingId)` | `UserRepository.unsaveListing`, rồi bỏ tin khỏi `savedListings`. |  |
| `public void loadUserListings(String uid)` | `ListingRepository.getBySeller`, chỉ tin đang bán khi xem hồ sơ người khác. |  |
| `public void requestVerification(String nationalId, List<Uri> imageUris)` | Gọi `UserRepository.requestVerification` ngoài luồng chính, đưa kết quả hoặc lỗi vào LiveData. | 73 |
| `public void loadVerification()` | `UserRepository.getVerification`. |  |
| `public void loadIdentity()` | `UserRepository.getIdentity` cho chính chủ. | 9 |
| `public void saveIdentity(UserIdentity identity)` | Gọi `UserRepository.saveIdentity` ngoài luồng chính, đưa kết quả hoặc lỗi vào LiveData. | 9 |
| `public void updateNotificationPrefs(Map<String,Boolean> prefs)` | Gọi `UserRepository.updateNotificationPrefs` ngoài luồng chính, đưa kết quả hoặc lỗi vào LiveData. | 10 |
| `public void deactivateAccount(String password)` | Xác thực lại, rồi `UserRepository.deactivate`, rồi `AuthRepository.deleteLogin`, rồi đăng xuất và xóa Room. | 12 |
| `public void deactivateWithGoogle(String idToken)` | Như `deactivateAccount` nhưng xác thực lại bằng `AuthRepository.reauthenticateWithGoogle`. |  |
| `public void clearError()` | Đặt `error` về rỗng sau khi màn hình đã hiện câu lỗi, để xoay màn hình không hiện lại. |  |
| `public void clearDone()` | Đặt `done` về false sau khi màn hình đã phản ứng (đóng hộp thoại, quay lại), để không chạy hai lần. |  |

**Quan hệ trên sơ đồ lớp**

- `ProfileViewModel` kế thừa `ViewModel`. Trang 7. ViewModel và tiện ích.

**Dùng tới:** [`AuthRepository`](data.md#authrepository), [`Listing`](model.md#listing), [`ListingRepository`](data.md#listingrepository), [`MediaRepository`](data.md#mediarepository), [`ProfileStats`](model.md#profilestats), [`SellerVerification`](model.md#sellerverification), [`User`](model.md#user), [`UserAddress`](model.md#useraddress), [`UserIdentity`](model.md#useridentity), [`UserRepository`](data.md#userrepository)

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
| `categories` | `LiveData<List<Category>>` | private | Danh mục cho ô chọn của biểu mẫu đăng tin và cho bộ lọc kệ hàng. |
| `createdListingId` | `LiveData<String>` | private | Mã tin vừa tạo hoặc vừa đăng, để màn hình mở thẳng B03. |
| `loading` | `LiveData<Boolean>` | private | True trong lúc đang gọi mạng hoặc đọc đĩa; màn hình hiện trạng thái đang tải và khóa nút gửi. |
| `error` | `LiveData<String>` | private | Câu lỗi tiếng Việt cho màn hình hiện; rỗng khi không có lỗi. Màn hình xóa sau khi đã hiện, để xoay màn hình không hiện lại. |
| `done` | `LiveData<Boolean>` | private | Bật lên đúng một lần khi một thao tác ghi xong, để màn hình đóng hoặc quay lại; màn hình đặt lại false sau khi dùng. |

**Phương thức**

| Chữ ký | Mô tả | STT |
| --- | --- | --- |
| `public void loadMyListings(SortOrder order, String categoryId)` | Tin của chính mình, sắp theo cách chọn và lọc theo danh mục. | 14 |
| `public void loadCategories()` | `CategoryRepository.getAll`. |  |
| `public void createListing(Listing listing, List<Uri> imageUris)` | Kiểm tra từ khóa cấm, tải ảnh, rồi `ListingRepository.create`. | 13, 17, 78 |
| `public void saveDraft(Listing listing, List<Uri> imageUris)` | Gọi `ListingRepository.saveDraft` ngoài luồng chính, đưa kết quả hoặc lỗi vào LiveData. | 22 |
| `public void publishDraft(String id)` | Kiểm tra đủ thông tin (`Listing.publish`) rồi ghi. | 22 |
| `public void updateListing(Listing listing, List<Uri> imageUris)` | `ListingRepository.update`. | 13 |
| `public void changeStatus(String id, ListingStatus status)` | Đổi trạng thái tin trong các bước người bán được phép (xem `trang-thai.md`). | 13 |
| `public void setHidden(String id, boolean hidden)` | Gọi `ListingRepository.setHidden` ngoài luồng chính, đưa kết quả hoặc lỗi vào LiveData. | 15 |
| `public void deleteListing(String id)` | `ListingRepository.delete`. | 13 |
| `public void clearError()` | Đặt `error` về rỗng sau khi màn hình đã hiện câu lỗi, để xoay màn hình không hiện lại. |  |
| `public void clearDone()` | Đặt `done` về false sau khi màn hình đã phản ứng (đóng hộp thoại, quay lại), để không chạy hai lần. |  |

**Quan hệ trên sơ đồ lớp**

- `ShopViewModel` kế thừa `ViewModel`. Trang 7. ViewModel và tiện ích.

**Dùng tới:** [`Category`](model.md#category), [`CategoryRepository`](data.md#categoryrepository), [`KeywordFilter`](util.md#keywordfilter), [`Listing`](model.md#listing), [`ListingRepository`](data.md#listingrepository), [`ListingStatus`](enums.md#listingstatus), [`MediaRepository`](data.md#mediarepository), [`SortOrder`](enums.md#sortorder)

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
| `categories` | `LiveData<List<Category>>` | private | Danh mục cho hàng lọc nhanh trên trang chủ (STT 24). |
| `loading` | `LiveData<Boolean>` | private | True trong lúc đang gọi mạng hoặc đọc đĩa; màn hình hiện trạng thái đang tải và khóa nút gửi. |
| `error` | `LiveData<String>` | private | Câu lỗi tiếng Việt cho màn hình hiện; rỗng khi không có lỗi. Màn hình xóa sau khi đã hiện, để xoay màn hình không hiện lại. |
| `done` | `LiveData<Boolean>` | private | Bật lên đúng một lần khi một thao tác ghi xong, để màn hình đóng hoặc quay lại; màn hình đặt lại false sau khi dùng. |

**Phương thức**

| Chữ ký | Mô tả | STT |
| --- | --- | --- |
| `public void loadFeed(boolean followingOnly, SortOrder order)` | Trang đầu của kênh chung hoặc kênh đang theo dõi. | 29, 30 |
| `public void loadCategories()` | `CategoryRepository.getAll`. |  |
| `public void loadNext()` | Trang kế tiếp. | 29 |
| `public void loadForYou()` | Đọc lịch sử xem, tìm kiếm và tin đã lưu rồi gọi `RecommendationEngine.forYou`. | 38 |
| `public void refresh()` | Tải lại từ đầu. |  |
| `public void clearError()` | Đặt `error` về rỗng sau khi màn hình đã hiện câu lỗi, để xoay màn hình không hiện lại. |  |
| `public void clearDone()` | Đặt `done` về false sau khi màn hình đã phản ứng (đóng hộp thoại, quay lại), để không chạy hai lần. |  |

**Quan hệ trên sơ đồ lớp**

- `FeedViewModel` kế thừa `ViewModel`. Trang 7. ViewModel và tiện ích.

**Dùng tới:** [`Category`](model.md#category), [`CategoryRepository`](data.md#categoryrepository), [`Listing`](model.md#listing), [`ListingRepository`](data.md#listingrepository), [`NetworkMonitor`](util.md#networkmonitor), [`RecommendationEngine`](util.md#recommendationengine), [`SortOrder`](enums.md#sortorder), [`UserRepository`](data.md#userrepository)

### SearchViewModel

| Mục | Nội dung |
| --- | --- |
| Định danh | `com.nhom3.pubgapp.viewmodel.SearchViewModel` |
| Loại | lớp (ViewModel) |
| Kế thừa | `androidx.lifecycle.ViewModel` |
| Lưu trữ | Không lưu |
| Trách nhiệm | Từ khóa, bộ lọc, lịch sử tìm kiếm, và kết quả tìm người dùng. |
| Chức năng liên quan (STT) | 23, 38, 103 |

**Thuộc tính**

| Tên | Kiểu | Truy cập | Ý nghĩa |
| --- | --- | --- | --- |
| `filter` | `SearchFilter` | private | Bộ lọc hiện tại; giữ trong ViewModel để không mất khi xoay màn hình. |
| `results` | `LiveData<List<Listing>>` | private | Dữ liệu màn hình quan sát (`List<Listing>`); giữ qua lần xoay màn hình. |
| `userResults` | `LiveData<List<User>>` | private | Kết quả tìm người dùng của tab Người dùng trên B02; rỗng khi ô tìm kiếm trống. |
| `categories` | `LiveData<List<Category>>` | private | Danh mục cho bộ lọc tìm kiếm. |
| `recentSearches` | `LiveData<List<String>>` | private | Dữ liệu màn hình quan sát (`List<String>`); giữ qua lần xoay màn hình. |
| `loading` | `LiveData<Boolean>` | private | True trong lúc đang gọi mạng hoặc đọc đĩa; màn hình hiện trạng thái đang tải và khóa nút gửi. |
| `error` | `LiveData<String>` | private | Câu lỗi tiếng Việt cho màn hình hiện; rỗng khi không có lỗi. Màn hình xóa sau khi đã hiện, để xoay màn hình không hiện lại. |
| `done` | `LiveData<Boolean>` | private | Bật lên đúng một lần khi một thao tác ghi xong, để màn hình đóng hoặc quay lại; màn hình đặt lại false sau khi dùng. |

**Phương thức**

| Chữ ký | Mô tả | STT |
| --- | --- | --- |
| `public void setKeyword(String text)` | Đặt từ khóa vào bộ lọc. | 23 |
| `public void applyFilter(SearchFilter filter)` | Thay bộ lọc và tìm lại. |  |
| `public void search()` | Gọi `ListingRepository.search` ngoài luồng chính, đưa kết quả hoặc lỗi vào LiveData. | 23 |
| `public void loadNext()` | Tải trang kết quả tiếp theo bằng con trỏ của lần gọi trước. |  |
| `public void searchUsers(String keyword)` | Gọi `UserRepository.searchUsers` với từ khóa hiện tại và đổ vào `userResults`; giữ kết quả qua xoay màn hình. | 103 |
| `public void loadCategories()` | `CategoryRepository.getAll`. |  |
| `public void loadRecentSearches()` | Đọc lịch sử tìm kiếm trên máy. | 38 |
| `public void clearError()` | Đặt `error` về rỗng sau khi màn hình đã hiện câu lỗi, để xoay màn hình không hiện lại. |  |
| `public void clearDone()` | Đặt `done` về false sau khi màn hình đã phản ứng (đóng hộp thoại, quay lại), để không chạy hai lần. |  |

**Quan hệ trên sơ đồ lớp**

- `SearchViewModel` kế thừa `ViewModel`. Trang 7. ViewModel và tiện ích.

**Dùng tới:** [`Category`](model.md#category), [`CategoryRepository`](data.md#categoryrepository), [`FormatUtils`](util.md#formatutils), [`GeoUtils`](util.md#geoutils), [`Listing`](model.md#listing), [`ListingRepository`](data.md#listingrepository), [`SearchFilter`](model.md#searchfilter), [`User`](model.md#user), [`UserRepository`](data.md#userrepository)

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
| `sellerStats` | `LiveData<ProfileStats>` | private | Số liệu hồ sơ của người bán (đã bán, hoàn tất) hiện trên thẻ người bán. |
| `images` | `LiveData<List<byte[]>>` | private | Ảnh đầy đủ đã giải mã của tin, tải sau khi màn hình đã hiện ảnh bìa thu nhỏ. |
| `similar` | `LiveData<List<Listing>>` | private | Dữ liệu màn hình quan sát (`List<Listing>`); giữ qua lần xoay màn hình. |
| `saved` | `LiveData<Boolean>` | private | Tin đang xem đã được lưu hay chưa. |
| `followingSeller` | `LiveData<Boolean>` | private | True khi đang theo dõi người bán; nút Theo dõi đọc cờ này. |
| `priceRange` | `LiveData<long[]>` | private | Khoảng giá tham khảo [thấp, cao]; mảng rỗng khi không đủ dữ liệu. |
| `loading` | `LiveData<Boolean>` | private | True trong lúc đang gọi mạng hoặc đọc đĩa; màn hình hiện trạng thái đang tải và khóa nút gửi. |
| `error` | `LiveData<String>` | private | Câu lỗi tiếng Việt cho màn hình hiện; rỗng khi không có lỗi. Màn hình xóa sau khi đã hiện, để xoay màn hình không hiện lại. |
| `done` | `LiveData<Boolean>` | private | Bật lên đúng một lần khi một thao tác ghi xong, để màn hình đóng hoặc quay lại; màn hình đặt lại false sau khi dùng. |

**Phương thức**

| Chữ ký | Mô tả | STT |
| --- | --- | --- |
| `public void load(String listingId)` | Đọc tin và người bán, ghi lịch sử xem. |  |
| `public void loadImages()` | `MediaRepository.getImage` cho từng mã trong `Listing.imageIds`, ngoài luồng chính. |  |
| `public void toggleSave()` | Lưu hoặc bỏ lưu tin. | 40 đến 42 |
| `public void toggleFollowSeller()` | `UserRepository.follow` hoặc `unfollow` người bán của tin đang xem. |  |
| `public void loadSimilar()` | Tin tương tự qua `RecommendationEngine.similarTo`. | 36 |
| `public void loadPriceRange()` | Khoảng giá tham khảo qua `RecommendationEngine.priceRange`. | 37 |
| `public void clearError()` | Đặt `error` về rỗng sau khi màn hình đã hiện câu lỗi, để xoay màn hình không hiện lại. |  |
| `public void clearDone()` | Đặt `done` về false sau khi màn hình đã phản ứng (đóng hộp thoại, quay lại), để không chạy hai lần. |  |

**Quan hệ trên sơ đồ lớp**

- `ListingDetailViewModel` kế thừa `ViewModel`. Trang 7. ViewModel và tiện ích.

**Dùng tới:** [`Listing`](model.md#listing), [`ListingRepository`](data.md#listingrepository), [`MediaRepository`](data.md#mediarepository), [`ProfileStats`](model.md#profilestats), [`RecommendationEngine`](util.md#recommendationengine), [`User`](model.md#user), [`UserRepository`](data.md#userrepository)

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
| `listing` | `LiveData<Listing>` | private | Tin đăng của cuộc trò chuyện, hiện ở dải trên cùng màn chat. |
| `offers` | `LiveData<List<Offer>>` | private | Dữ liệu màn hình quan sát (`List<Offer>`); giữ qua lần xoay màn hình. |
| `loading` | `LiveData<Boolean>` | private | True trong lúc đang gọi mạng hoặc đọc đĩa; màn hình hiện trạng thái đang tải và khóa nút gửi. |
| `error` | `LiveData<String>` | private | Câu lỗi tiếng Việt cho màn hình hiện; rỗng khi không có lỗi. Màn hình xóa sau khi đã hiện, để xoay màn hình không hiện lại. |
| `done` | `LiveData<Boolean>` | private | Bật lên đúng một lần khi một thao tác ghi xong, để màn hình đóng hoặc quay lại; màn hình đặt lại false sau khi dùng. |

**Phương thức**

| Chữ ký | Mô tả | STT |
| --- | --- | --- |
| `public void loadConversations()` | Danh sách chat của người dùng. |  |
| `public void openConversation(String listingId)` | Mở hoặc tạo cuộc trò chuyện về một tin, bắt đầu lắng nghe tin nhắn. |  |
| `public void openConversationById(String id)` | Mở cuộc trò chuyện theo mã (người bán, hoặc từ thông báo). |  |
| `public void loadOlder()` | Tải thêm tin nhắn cũ. |  |
| `public void loadPartner()` | Đọc hồ sơ bên còn lại để hiện tên và trạng thái hoạt động. | 46 |
| `public void loadListing()` | `ListingRepository.observeListing` (lắng nghe thời gian thực, giữ trong `ListenerSet`) cho dải tin trên cùng màn chat, để trạng thái và giá đổi ngay khi đề nghị được chấp nhận. |  |
| `public void closeConversation(String screenOpenedWith)` | Màn chat gọi khi bị đóng hẳn (không gọi khi xoay màn hình): gỡ listener tin nhắn, đề nghị và tin đăng nếu cuộc trò chuyện đang mở vẫn là cuộc mà màn đó đã mở bằng mã `screenOpenedWith` (mã tin hoặc mã cuộc trò chuyện). |  |
| `public void observeOffers()` | `ChatRepository.observeOffers` cho cuộc trò chuyện đang mở; kết quả vào `offers`. |  |
| `public void sendText(String text)` | Gọi `ChatRepository.sendText` ngoài luồng chính, đưa kết quả hoặc lỗi vào LiveData. | 43, 44 |
| `public void sendImage(Uri uri)` | `ChatRepository.sendImage`. | 43, 44 |
| `public void makeOffer(Offer offer)` | `ChatRepository.sendOffer`. | 47 đến 49 |
| `public void respondToOffer(String offerId, boolean accept)` | Gọi `ChatRepository.respondToOffer` ngoài luồng chính, đưa kết quả hoặc lỗi vào LiveData. | 47 đến 49 |
| `public void clearError()` | Đặt `error` về rỗng sau khi màn hình đã hiện câu lỗi, để xoay màn hình không hiện lại. |  |
| `public void clearDone()` | Đặt `done` về false sau khi màn hình đã phản ứng (đóng hộp thoại, quay lại), để không chạy hai lần. |  |

**Quan hệ trên sơ đồ lớp**

- `ChatViewModel` kế thừa `ViewModel`. Trang 7. ViewModel và tiện ích.

**Dùng tới:** [`ChatMessage`](model.md#chatmessage), [`ChatRepository`](data.md#chatrepository), [`Conversation`](model.md#conversation), [`Listing`](model.md#listing), [`ListingRepository`](data.md#listingrepository), [`MediaRepository`](data.md#mediarepository), [`Offer`](model.md#offer), [`User`](model.md#user), [`UserRepository`](data.md#userrepository)

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
| `payment` | `LiveData<Payment>` | private | Khoản thanh toán của giao dịch đang mở; null khi giao dịch không cần trả tiền. |
| `payments` | `LiveData<List<Payment>>` | private | Dữ liệu màn hình quan sát (`List<Payment>`); giữ qua lần xoay màn hình. |
| `myReview` | `LiveData<Review>` | private | Đánh giá người đang đăng nhập đã viết cho giao dịch này; null khi chưa viết. |
| `loading` | `LiveData<Boolean>` | private | True trong lúc đang gọi mạng hoặc đọc đĩa; màn hình hiện trạng thái đang tải và khóa nút gửi. |
| `error` | `LiveData<String>` | private | Câu lỗi tiếng Việt cho màn hình hiện; rỗng khi không có lỗi. Màn hình xóa sau khi đã hiện, để xoay màn hình không hiện lại. |
| `done` | `LiveData<Boolean>` | private | Bật lên đúng một lần khi một thao tác ghi xong, để màn hình đóng hoặc quay lại; màn hình đặt lại false sau khi dùng. |

**Phương thức**

| Chữ ký | Mô tả | STT |
| --- | --- | --- |
| `public void loadDeal(String id)` | `DealRepository.getById` và `getPayment`. | 50 |
| `public void updateTerms(Deal deal)` | Gọi `DealRepository.updateTerms` ngoài luồng chính, đưa kết quả hoặc lỗi vào LiveData. | 51 |
| `public void confirm()` | Gọi `DealRepository.confirm` ngoài luồng chính, đưa kết quả hoặc lỗi vào LiveData. | 52 |
| `public void pay(PaymentMethod method)` | Gọi `DealRepository.pay` ngoài luồng chính, đưa kết quả hoặc lỗi vào LiveData. | 56 |
| `public void loadMyReview()` | `ReviewRepository.getByDeal` để biết người đang xem đã đánh giá chưa (ẩn hoặc hiện nút Viết đánh giá). |  |
| `public void markShipped(String carrier, String trackingCode, Uri photoUri)` | Gọi `DealRepository.markShipped` ngoài luồng chính, đưa kết quả hoặc lỗi vào LiveData. | 54 |
| `public void markReceived(Uri photoUri)` | Gọi `DealRepository.markReceived` ngoài luồng chính, đưa kết quả hoặc lỗi vào LiveData. | 53 |
| `public void checkAutoRelease()` | Gọi khi mở ứng dụng: chạy `releaseDueDeals`, `completeDueDeals`, `expirePendingDeals` ngay, không chờ việc chạy nền. | 84 |
| `public void cancel(String reason)` | Gọi `DealRepository.cancel` ngoài luồng chính, đưa kết quả hoặc lỗi vào LiveData. | 55 |
| `public void loadHistory()` | `DealRepository.getMyDeals`. | 57 |
| `public void loadPayments()` | `DealRepository.getPayments`. | 58 |
| `public void clearError()` | Đặt `error` về rỗng sau khi màn hình đã hiện câu lỗi, để xoay màn hình không hiện lại. |  |
| `public void clearDone()` | Đặt `done` về false sau khi màn hình đã phản ứng (đóng hộp thoại, quay lại), để không chạy hai lần. |  |

**Quan hệ trên sơ đồ lớp**

- `DealViewModel` kế thừa `ViewModel`. Trang 7. ViewModel và tiện ích.

**Dùng tới:** [`Deal`](model.md#deal), [`DealRepository`](data.md#dealrepository), [`MediaRepository`](data.md#mediarepository), [`Payment`](model.md#payment), [`PaymentMethod`](enums.md#paymentmethod), [`Review`](model.md#review), [`ReviewRepository`](data.md#reviewrepository), [`UserRepository`](data.md#userrepository)

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
| `selectedComplaint` | `LiveData<Complaint>` | private | Khiếu nại đang mở trên màn theo dõi khiếu nại của người dùng. |
| `exportedFile` | `LiveData<File>` | private | Tệp CSV vừa xuất, để màn hình mở trình chia sẻ của hệ điều hành. |
| `loading` | `LiveData<Boolean>` | private | True trong lúc đang gọi mạng hoặc đọc đĩa; màn hình hiện trạng thái đang tải và khóa nút gửi. |
| `error` | `LiveData<String>` | private | Câu lỗi tiếng Việt cho màn hình hiện; rỗng khi không có lỗi. Màn hình xóa sau khi đã hiện, để xoay màn hình không hiện lại. |
| `done` | `LiveData<Boolean>` | private | Bật lên đúng một lần khi một thao tác ghi xong, để màn hình đóng hoặc quay lại; màn hình đặt lại false sau khi dùng. |

**Phương thức**

| Chữ ký | Mô tả | STT |
| --- | --- | --- |
| `public void submitReview(Review review)` | `ReviewRepository.create`. | 59 |
| `public void loadReviews(String uid, boolean received)` | Đánh giá đã nhận hoặc đã viết, tùy tham số. | 60 |
| `public void submitReport(Report report, List<Uri> imageUris)` | `ReportRepository.create`. | 61 |
| `public void cancelReport(String id)` | `ReportRepository.cancel`. | 62 |
| `public void loadMyReports()` | `ReportRepository.getMyReports`. | 63 |
| `public void exportReports()` | `ReportRepository.exportCsv` rồi mở hộp chia sẻ tệp. | 63 |
| `public void submitComplaint(Complaint complaint, List<Uri> evidenceUris)` | Tải ảnh bằng chứng (tối thiểu 1) rồi `ComplaintRepository.create`. | 64 |
| `public void addEvidence(String id, List<Uri> evidenceUris)` | Lưu ảnh qua `MediaRepository`, rồi `ComplaintRepository.addEvidence`. | 64 |
| `public void loadMyComplaints()` | `ComplaintRepository.getMyComplaints`. | 65 |
| `public void loadComplaint(String id)` | `ComplaintRepository.getDetail` cho màn theo dõi khiếu nại. |  |
| `public void withdrawComplaint(String id)` | `ComplaintRepository.withdraw`. | 66 |
| `public static double[] averageCriteria(List<Review> reviews)` | Điểm trung bình của ba tiêu chí (chính xác, thái độ, giữ hẹn) từ danh sách đánh giá đã nhận, trả về mảng ba phần tử theo thứ tự đó. Tĩnh và thuần tính toán nên gọi được từ test không cần vòng đời Android. |  |
| `public void clearError()` | Đặt `error` về rỗng sau khi màn hình đã hiện câu lỗi, để xoay màn hình không hiện lại. |  |
| `public void clearDone()` | Đặt `done` về false sau khi màn hình đã phản ứng (đóng hộp thoại, quay lại), để không chạy hai lần. |  |

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
| `loading` | `LiveData<Boolean>` | private | True trong lúc đang gọi mạng hoặc đọc đĩa; màn hình hiện trạng thái đang tải và khóa nút gửi. |
| `error` | `LiveData<String>` | private | Câu lỗi tiếng Việt cho màn hình hiện; rỗng khi không có lỗi. Màn hình xóa sau khi đã hiện, để xoay màn hình không hiện lại. |
| `done` | `LiveData<Boolean>` | private | Bật lên đúng một lần khi một thao tác ghi xong, để màn hình đóng hoặc quay lại; màn hình đặt lại false sau khi dùng. |

**Phương thức**

| Chữ ký | Mô tả | STT |
| --- | --- | --- |
| `public void load()` | Đọc thông báo và đếm số chưa đọc. | 67, 91 |
| `public void markRead(String id)` | Gọi `NotificationRepository.markRead` ngoài luồng chính, đưa kết quả hoặc lỗi vào LiveData. | 67, 91 |
| `public void clearError()` | Đặt `error` về rỗng sau khi màn hình đã hiện câu lỗi, để xoay màn hình không hiện lại. |  |
| `public void clearDone()` | Đặt `done` về false sau khi màn hình đã phản ứng (đóng hộp thoại, quay lại), để không chạy hai lần. |  |

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
| Trách nhiệm | Admin: người dùng, hồ sơ, khóa, cảnh báo, xác thực tài khoản, nhật ký. |
| Chức năng liên quan (STT) | 68 đến 74, 105 |

**Thuộc tính**

| Tên | Kiểu | Truy cập | Ý nghĩa |
| --- | --- | --- | --- |
| `users` | `LiveData<List<User>>` | private | Dữ liệu màn hình quan sát (`List<User>`); giữ qua lần xoay màn hình. |
| `selectedUser` | `LiveData<User>` | private | Người dùng đang xem ở màn chi tiết E03. |
| `identity` | `LiveData<UserIdentity>` | private | Thông tin định danh của người đang xem; chỉ hiện trong khối duyệt xác thực. |
| `stats` | `LiveData<ProfileStats>` | private | Số liệu hồ sơ của người đang xem. |
| `warnings` | `LiveData<List<Warning>>` | private | Dữ liệu màn hình quan sát (`List<Warning>`); giữ qua lần xoay màn hình. |
| `logs` | `LiveData<List<ActivityLog>>` | private | Dữ liệu màn hình quan sát (`List<ActivityLog>`); giữ qua lần xoay màn hình. |
| `verifications` | `LiveData<List<SellerVerification>>` | private | Các yêu cầu xác thực đang chờ duyệt. |
| `userListings` | `LiveData<List<Listing>>` | private | Tin đăng của người đang xem, tab Tin đăng của E03; admin thấy cả tin nháp, đã ẩn và đã gỡ. |
| `userDeals` | `LiveData<List<Deal>>` | private | Giao dịch gần đây của người đang xem, tab Giao dịch của E03. |
| `userReviews` | `LiveData<List<Review>>` | private | Đánh giá người đang xem đã nhận, tab Đánh giá của E03. |
| `reportCount` | `LiveData<Integer>` | private | Số báo cáo đang chờ xử lý nhằm vào người đang xem, hiện ở đầu E03. |
| `verificationImages` | `LiveData<List<byte[]>>` | private | Hai ảnh của yêu cầu xác thực đã giải mã: phần tử đầu là ảnh giấy tờ, phần tử sau là ảnh chân dung cầm giấy tờ. |
| `admins` | `LiveData<List<User>>` | private | Danh sách tài khoản admin cho màn Quản lý tài khoản admin. |
| `pendingAdmin` | `LiveData<User>` | private | Tài khoản vừa tìm theo email, chờ admin bấm Xác nhận để cấp quyền (UC42). |
| `loading` | `LiveData<Boolean>` | private | True trong lúc đang gọi mạng hoặc đọc đĩa; màn hình hiện trạng thái đang tải và khóa nút gửi. |
| `error` | `LiveData<String>` | private | Câu lỗi tiếng Việt cho màn hình hiện; rỗng khi không có lỗi. Màn hình xóa sau khi đã hiện, để xoay màn hình không hiện lại. |
| `done` | `LiveData<Boolean>` | private | Bật lên đúng một lần khi một thao tác ghi xong, để màn hình đóng hoặc quay lại; màn hình đặt lại false sau khi dùng. |

**Phương thức**

| Chữ ký | Mô tả | STT |
| --- | --- | --- |
| `public void filterUsers(UserStatus status, Timestamp from, Timestamp to, String province, String keyword)` | `AdminRepository.listUsers`. | 68 |
| `public void loadProfile(String userId)` | Hồ sơ, thông tin định danh, tin đăng, giao dịch, đánh giá và cảnh báo của một người dùng. | 69 |
| `public void lock(String userId, Timestamp until, String reason)` | `AdminRepository.lockUser`. | 70, 71 |
| `public void unlock(String userId)` | `AdminRepository.unlockUser`. | 70, 71 |
| `public void warn(String userId, String reason, Timestamp until)` | `AdminRepository.warnUser`. Hệ thống chỉ khóa tài khoản theo bậc (`Warning.resultingStatus`); việc gỡ tin của tài khoản bị khóa là thao tác riêng admin bấm (quyết định 2026-10-22). | 72 |
| `public void loadVerificationRequests()` | `AdminRepository.getVerificationRequests`. | 73 |
| `public void reviewVerification(String userId, boolean approve, String note)` | Gọi `AdminRepository.reviewVerification` ngoài luồng chính, đưa kết quả hoặc lỗi vào LiveData. | 73 |
| `public void loadVerificationImages(String userId)` | `MediaRepository.getImage` cho hai mã trong `SellerVerification.imageIds`, ngoài luồng chính. |  |
| `public void loadActivityLog(String userId)` | `AdminRepository.getActivityLogs`. | 74 |
| `public void loadAdmins()` | `AdminRepository.listAdmins`. | 105 |
| `public void findAdminCandidate(String email)` | `AdminRepository.findUserByEmail`; kết quả vào `pendingAdmin` để pop up hiện tên và email. |  |
| `public void grantAdmin(String userId)` | `AdminRepository.findUserByEmail` để xác nhận tên và email, rồi `AdminRepository.grantAdmin`. | 105 |
| `public void revokeAdmin(String userId)` | `AdminRepository.revokeAdmin`. | 105 |
| `public void clearError()` | Đặt `error` về rỗng sau khi màn hình đã hiện câu lỗi, để xoay màn hình không hiện lại. |  |
| `public void clearDone()` | Đặt `done` về false sau khi màn hình đã phản ứng (đóng hộp thoại, quay lại), để không chạy hai lần. |  |
| `public void clearError()` | Đặt `error` về rỗng sau khi màn hình đã hiện câu lỗi, để xoay màn hình không hiện lại. |  |
| `public void clearDone()` | Đặt `done` về false sau khi màn hình đã phản ứng (đóng hộp thoại, quay lại), để không chạy hai lần. |  |

**Quan hệ trên sơ đồ lớp**

- `AdminUserViewModel` kế thừa `ViewModel`. Trang 7. ViewModel và tiện ích.

**Dùng tới:** [`ActivityLog`](model.md#activitylog), [`AdminRepository`](data.md#adminrepository), [`Deal`](model.md#deal), [`DealRepository`](data.md#dealrepository), [`Listing`](model.md#listing), [`ListingRepository`](data.md#listingrepository), [`MediaRepository`](data.md#mediarepository), [`ProfileStats`](model.md#profilestats), [`ReportRepository`](data.md#reportrepository), [`Review`](model.md#review), [`ReviewRepository`](data.md#reviewrepository), [`SellerVerification`](model.md#sellerverification), [`User`](model.md#user), [`UserIdentity`](model.md#useridentity), [`UserRepository`](data.md#userrepository), [`UserStatus`](enums.md#userstatus), [`Warning`](model.md#warning)

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
| `reports` | `LiveData<List<Report>>` | private | Báo cáo theo trạng thái và loại đối tượng, cho tab Báo cáo của E04. |
| `reportedListings` | `LiveData<List<Listing>>` | private | Dữ liệu màn hình quan sát (`List<Listing>`); giữ qua lần xoay màn hình. |
| `selectedListing` | `LiveData<Listing>` | private | Tin đang xem ở màn chi tiết tin của khu quản trị. |
| `flagged` | `LiveData<List<Listing>>` | private | Tin bị gắn cờ từ khóa cấm, cho tab Tin bị gắn cờ của E04. |
| `reportEvidence` | `LiveData<List<byte[]>>` | private | Ảnh đính kèm của báo cáo đang mở, đã giải mã; rỗng khi người báo cáo không đính kèm. |
| `reportedMessages` | `LiveData<List<ChatMessage>>` | private | Đoạn trò chuyện chứa tin nhắn bị báo cáo, đọc theo `Report.conversationId`; rỗng khi báo cáo không nhằm vào tin nhắn. |
| `keywords` | `LiveData<List<BannedKeyword>>` | private | Dữ liệu màn hình quan sát (`List<BannedKeyword>`); giữ qua lần xoay màn hình. |
| `categories` | `LiveData<List<Category>>` | private | Dữ liệu màn hình quan sát (`List<Category>`); giữ qua lần xoay màn hình. |
| `categoryCounts` | `LiveData<Map<String,Integer>>` | private | Số tin đăng của từng danh mục, khóa là mã danh mục; E05 hiện cạnh tên danh mục và dùng để chặn xóa danh mục còn tin. |
| `loading` | `LiveData<Boolean>` | private | True trong lúc đang gọi mạng hoặc đọc đĩa; màn hình hiện trạng thái đang tải và khóa nút gửi. |
| `error` | `LiveData<String>` | private | Câu lỗi tiếng Việt cho màn hình hiện; rỗng khi không có lỗi. Màn hình xóa sau khi đã hiện, để xoay màn hình không hiện lại. |
| `done` | `LiveData<Boolean>` | private | Bật lên đúng một lần khi một thao tác ghi xong, để màn hình đóng hoặc quay lại; màn hình đặt lại false sau khi dùng. |

**Phương thức**

| Chữ ký | Mô tả | STT |
| --- | --- | --- |
| `public void loadReports(ReportStatus status, ReportTarget targetType)` | `ReportRepository.getByStatus` theo trạng thái và loại đối tượng. |  |
| `public void loadReportedListings()` | Báo cáo OPEN nhắm vào tin, gom theo tin kèm số lượt và lý do. | 75 |
| `public void loadListing(String listingId)` | `ListingRepository.getById` cho màn chi tiết tin của khu quản trị. |  |
| `public void loadReportEvidence(Report report)` | `MediaRepository.getImage` cho từng mã trong `Report.evidenceImageIds`. |  |
| `public void loadReportedConversation(String conversationId)` | `ChatRepository.getMessagesOnce` cho đoạn chat của báo cáo tin nhắn. |  |
| `public void closeReports(String listingId, boolean dismissed)` | `ReportRepository.closeByTarget`. | 75 |
| `public void hide(String listingId, String reason)` | `AdminRepository.hideListing`. | 76 |
| `public void restore(String listingId)` | `AdminRepository.restoreListing`. | 76 |
| `public void remove(String listingId, String reason)` | `AdminRepository.removeListing`. | 76 |
| `public void removeAllOf(String userId)` | `AdminRepository.removeAllListingsOf`. | 77 |
| `public void loadFlagged()` | `AdminRepository.getFlaggedListings`. |  |
| `public void clearFlag(String listingId)` | Gọi `AdminRepository.clearFlag` ngoài luồng chính, đưa kết quả hoặc lỗi vào LiveData. | 78 |
| `public void loadKeywords()` | `AdminRepository.getKeywords`. |  |
| `public void addKeyword(String word)` | Gọi `AdminRepository.addKeyword` ngoài luồng chính, đưa kết quả hoặc lỗi vào LiveData. | 78 |
| `public void removeKeyword(String id)` | Gọi `AdminRepository.removeKeyword` ngoài luồng chính, đưa kết quả hoặc lỗi vào LiveData. | 78 |
| `public void loadCategories()` | `CategoryRepository.getAllForAdmin`. |  |
| `public void loadCategoryCounts()` | Một truy vấn `count()` trên `listings` cho mỗi danh mục; gọi một lần khi mở E05. |  |
| `public void saveCategory(Category category)` | Thêm hoặc sửa danh mục. | 79 |
| `public void deleteCategory(String id)` | `CategoryRepository.delete`. | 79 |
| `public void setConditionFields(String id, List<String> fields)` | Ghi `conditionFields` của danh mục. | 80 |
| `public void clearError()` | Đặt `error` về rỗng sau khi màn hình đã hiện câu lỗi, để xoay màn hình không hiện lại. |  |
| `public void clearDone()` | Đặt `done` về false sau khi màn hình đã phản ứng (đóng hộp thoại, quay lại), để không chạy hai lần. |  |

**Quan hệ trên sơ đồ lớp**

- `AdminContentViewModel` kế thừa `ViewModel`. Trang 7. ViewModel và tiện ích.

**Dùng tới:** [`AdminRepository`](data.md#adminrepository), [`BannedKeyword`](model.md#bannedkeyword), [`Category`](model.md#category), [`CategoryRepository`](data.md#categoryrepository), [`ChatMessage`](model.md#chatmessage), [`ChatRepository`](data.md#chatrepository), [`Listing`](model.md#listing), [`ListingRepository`](data.md#listingrepository), [`MediaRepository`](data.md#mediarepository), [`Report`](model.md#report), [`ReportRepository`](data.md#reportrepository), [`ReportStatus`](enums.md#reportstatus), [`ReportTarget`](enums.md#reporttarget), [`UserRepository`](data.md#userrepository)

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
| `payment` | `LiveData<Payment>` | private | Khoản thanh toán của giao dịch đang xem. |
| `buyer` | `LiveData<User>` | private | Hồ sơ người mua, để màn chi tiết hiện tên thay vì mã. |
| `seller` | `LiveData<User>` | private | Hồ sơ người bán, để màn chi tiết hiện tên thay vì mã. |
| `loading` | `LiveData<Boolean>` | private | True trong lúc đang gọi mạng hoặc đọc đĩa; màn hình hiện trạng thái đang tải và khóa nút gửi. |
| `error` | `LiveData<String>` | private | Câu lỗi tiếng Việt cho màn hình hiện; rỗng khi không có lỗi. Màn hình xóa sau khi đã hiện, để xoay màn hình không hiện lại. |
| `done` | `LiveData<Boolean>` | private | Bật lên đúng một lần khi một thao tác ghi xong, để màn hình đóng hoặc quay lại; màn hình đặt lại false sau khi dùng. |

**Phương thức**

| Chữ ký | Mô tả | STT |
| --- | --- | --- |
| `public void loadDeals(DealStatus status)` | `DealRepository.listAll`. | 81 |
| `public void loadDetail(String dealId)` | Một giao dịch kèm `statusHistory` và khoản ký quỹ. | 82 |
| `public void clearError()` | Đặt `error` về rỗng sau khi màn hình đã hiện câu lỗi, để xoay màn hình không hiện lại. |  |
| `public void clearDone()` | Đặt `done` về false sau khi màn hình đã phản ứng (đóng hộp thoại, quay lại), để không chạy hai lần. |  |

**Quan hệ trên sơ đồ lớp**

- `AdminDealViewModel` kế thừa `ViewModel`. Trang 7. ViewModel và tiện ích.

**Dùng tới:** [`Deal`](model.md#deal), [`DealRepository`](data.md#dealrepository), [`DealStatus`](enums.md#dealstatus), [`Payment`](model.md#payment), [`User`](model.md#user), [`UserRepository`](data.md#userrepository)

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
| `deal` | `LiveData<Deal>` | private | Giao dịch bị khiếu nại, hiện trong hồ sơ khiếu nại. |
| `messages` | `LiveData<List<ChatMessage>>` | private | Lịch sử trò chuyện của hai bên, admin đọc khi xử lý khiếu nại. |
| `loading` | `LiveData<Boolean>` | private | True trong lúc đang gọi mạng hoặc đọc đĩa; màn hình hiện trạng thái đang tải và khóa nút gửi. |
| `error` | `LiveData<String>` | private | Câu lỗi tiếng Việt cho màn hình hiện; rỗng khi không có lỗi. Màn hình xóa sau khi đã hiện, để xoay màn hình không hiện lại. |
| `done` | `LiveData<Boolean>` | private | Bật lên đúng một lần khi một thao tác ghi xong, để màn hình đóng hoặc quay lại; màn hình đặt lại false sau khi dùng. |

**Phương thức**

| Chữ ký | Mô tả | STT |
| --- | --- | --- |
| `public void loadComplaints(ComplaintStatus status, Priority priority)` | `ComplaintRepository.getByStatus`. | 87 |
| `public void loadDetail(String id)` | `ComplaintRepository.getDetail` kèm lịch sử chat của giao dịch. | 88 |
| `public void setPriority(String id, Priority priority)` | `ComplaintRepository.setPriority`. |  |
| `public void decide(String id, ComplaintDecision decision, String message)` | `ComplaintRepository.decide` (repository ghi thông báo cho hai bên). | 89 |
| `public void saveNote(String id, String note)` | `ComplaintRepository.saveAdminNote`. | 90 |
| `public void clearError()` | Đặt `error` về rỗng sau khi màn hình đã hiện câu lỗi, để xoay màn hình không hiện lại. |  |
| `public void clearDone()` | Đặt `done` về false sau khi màn hình đã phản ứng (đóng hộp thoại, quay lại), để không chạy hai lần. |  |
| `public void clearError()` | Đặt `error` về rỗng sau khi màn hình đã hiện câu lỗi, để xoay màn hình không hiện lại. |  |
| `public void clearDone()` | Đặt `done` về false sau khi màn hình đã phản ứng (đóng hộp thoại, quay lại), để không chạy hai lần. |  |

**Quan hệ trên sơ đồ lớp**

- `AdminComplaintViewModel` kế thừa `ViewModel`. Trang 7. ViewModel và tiện ích.

**Dùng tới:** [`ChatMessage`](model.md#chatmessage), [`ChatRepository`](data.md#chatrepository), [`Complaint`](model.md#complaint), [`ComplaintDecision`](enums.md#complaintdecision), [`ComplaintRepository`](data.md#complaintrepository), [`ComplaintStatus`](enums.md#complaintstatus), [`Deal`](model.md#deal), [`DealRepository`](data.md#dealrepository), [`Priority`](enums.md#priority)

### ListenerSet

| Mục | Nội dung |
| --- | --- |
| Định danh | `com.nhom3.pubgapp.viewmodel.ListenerSet` |
| Loại | lớp (tiện ích) |
| Lưu trữ | Không lưu |
| Trách nhiệm | Giữ các `ListenerRegistration` đang chạy của một ViewModel, mỗi khóa một listener, để mở lại màn không chồng listener và rời màn không để listener đọc ngầm. |

**Phương thức**

| Chữ ký | Mô tả | STT |
| --- | --- | --- |
| `public void put(String key, ListenerRegistration registration)` | Gỡ listener cũ cùng khóa (nếu có) rồi giữ listener mới. |  |
| `public boolean has(String key)` | True khi khóa đang có listener chạy; ViewModel dùng để không gắn lại. |  |
| `public void remove(String key)` | Gỡ và quên listener của khóa, ví dụ khi listener báo lỗi. |  |
| `public void clear()` | Gỡ mọi listener; gọi ở `onCleared`. |  |
