# Bản đồ sở hữu

Ai làm phần nào, ai được sửa file nào. Mục tiêu: không ai phải chờ ai và hai Pull Request không sửa cùng một chỗ. Mỗi issue code ghi rõ danh sách file được sửa; file không có trong danh sách thì không sửa, kể cả khi AI đề nghị "sửa luôn cho tiện".

## 1. Năm module

| Module | Người sở hữu | GitHub | Chức năng (STT) | Màn hình |
| --- | --- | --- | --- | --- |
| **A. Tài khoản và thông báo** | Nguyễn Thùy Ngân | `loopy-tnw` | 1 đến 6, 9 đến 12, 35, 67 | A01 đến A06, C06 |
| **B. Tin đăng và khám phá** | Trần Anh Tú | `tranannhtu21012006` | 13 đến 34, 36 đến 38, 40 đến 42 | B01 đến B06 |
| **C. Chat và giao dịch** | Nguyễn Lê Hải Long | `Contest451` | 43, 44, 46 đến 59 | C01 đến C05 |
| **D. Hồ sơ và tin cậy** | Nguyễn Hoàng Phúc | `PmSubin` | 7, 8, 39, 60 đến 66, 87 đến 91 | D01 đến D07 |
| **E. Quản trị và nền móng** | Trần Đức Phát (leader) | `tahpnart8` | 68 đến 82, 84 | E01 đến E06, khung ứng dụng |

- STT 45 (gửi video) là **Phát triển sau** từ ngày 2026-10-12 vì không dùng Cloud Storage.
- Các STT Phát triển sau khác (83, 85, 86, 92 đến 102) không thuộc ai.
- STT 73 (xác minh người bán): Phúc làm phía người dùng (nút gửi yêu cầu trên D01), Phát làm phía admin (duyệt trên E03).

## 2. Gói và file theo module

Mỗi module có gói giao diện riêng, tiền tố tên file riêng, file chuỗi riêng và đồ thị điều hướng riêng. Không đặt file vào gói của module khác.

| Module | Gói giao diện | Tiền tố layout, drawable, id chuỗi | File chuỗi | Đồ thị điều hướng |
| --- | --- | --- | --- | --- |
| A | `ui.account`, `ui.notification` | `account_`, `notification_` | `strings_account.xml` | `nav_auth.xml`, `nav_account.xml` |
| B | `ui.listing` | `listing_` | `strings_listing.xml` | `nav_listing.xml` |
| C | `ui.trade` | `trade_` | `strings_trade.xml` | `nav_trade.xml` |
| D | `ui.profile`, `ui.trust`, `ui.admin.complaint` | `profile_`, `trust_` | `strings_profile.xml` | `nav_profile.xml`, `nav_admin_complaint.xml` |
| E | `ui.admin`, `ui.common`, `ui.main` | `admin_`, `common_` | `strings_admin.xml`, `strings_common.xml` | `nav_main.xml`, `nav_admin.xml` |

Gói đầy đủ là `com.nhom3.pubgapp.ui.<tên>`. Layout đặt tên `fragment_<tiền tố><màn>.xml`, `item_<tiền tố><mục>.xml`, ví dụ `fragment_listing_detail.xml`.

## 3. Màn hình và lớp giao diện

Tên lớp chốt ở đây để đồ thị điều hướng và lối tắt giữa các module trỏ đúng ngay từ P1. Khung trống của mọi lớp do Phát tạo ở P1-06; chủ module điền nội dung.

| Mã | Lớp | Activity chứa | Chủ |
| --- | --- | --- | --- |
| A01 | `LoginFragment` | `AuthActivity` | Ngân |
| A02 | `RegisterFragment` (cũng là bước hoàn tất hồ sơ khi đăng nhập Google lần đầu) | `AuthActivity` | Ngân |
| A03 | `ForgotPasswordFragment` | `AuthActivity` | Ngân |
| A04 | `PersonalInfoFragment` | `MainActivity` | Ngân |
| A05 | `EditProfileFragment`, `AccountSettingsFragment` | `MainActivity` | Ngân |
| A06 | `AddressListFragment`, `AddressEditFragment` | `MainActivity` | Ngân |
| C06 | `NotificationListFragment` | `MainActivity` | Ngân |
| B01 | `HomeFragment` | `MainActivity` (tab Trang chủ) | Tú |
| B02 | `SearchFragment` | `MainActivity` (tab Tìm kiếm) | Tú |
| B03 | `ListingDetailFragment` | `MainActivity` | Tú |
| B04 | `PostListingFragment` | `MainActivity` (tab Đăng tin) | Tú |
| B05 | `MyShopFragment` | `MainActivity` | Tú |
| B06 | `SavedListingsFragment` | `MainActivity` | Tú |
| C01 | `ConversationListFragment` | `MainActivity` (tab Tin nhắn) | Hải Long |
| C02 | `ChatFragment` | `MainActivity` | Hải Long |
| C03 | `DealDetailFragment` | `MainActivity` | Hải Long |
| C04 | `DealHistoryFragment` | `MainActivity` | Hải Long |
| C05 | `WriteReviewFragment` | `MainActivity` | Hải Long |
| D01 | `MyProfileFragment` | `MainActivity` (tab Cá nhân) | Phúc |
| D02 | `UserProfileFragment` | `MainActivity` | Phúc |
| D03 | `SellerTrustFragment` | `MainActivity` | Phúc |
| D04 | `ReportFragment` | `MainActivity` | Phúc |
| D05 | `CreateComplaintFragment` | `MainActivity` | Phúc |
| D06 | `AdminComplaintListFragment`, `AdminComplaintDetailFragment` | `AdminActivity` | Phúc |
| D07 (chưa có ảnh) | `MyReportsFragment`: hai thẻ Báo cáo của tôi và Khiếu nại của tôi | `MainActivity` | Phúc |
| E01 | `AdminDashboardFragment` | `AdminActivity` | Phát |
| E02 | `AdminUserListFragment` | `AdminActivity` | Phát |
| E03 | `AdminUserDetailFragment` | `AdminActivity` | Phát |
| E04 | `AdminModerationFragment`, `AdminListingDetailFragment` | `AdminActivity` | Phát |
| E05 | `AdminCategoryFragment` | `AdminActivity` | Phát |
| E06 | `AdminDealMonitorFragment`, `AdminDealDetailFragment` | `AdminActivity` | Phát |

**Ba Activity:**

- `AuthActivity` (Ngân): đăng nhập, đăng ký, quên mật khẩu.
- `MainActivity` (Phát): thanh điều hướng dưới gồm 5 tab Trang chủ, Tìm kiếm, Đăng tin, Tin nhắn, Cá nhân.
- `AdminActivity` (Phát): mở từ D01 khi `SessionManager.isAdmin()`.

## 4. Đi từ module này sang module khác

Không ai sửa đồ thị điều hướng của người khác. Mọi lối đi chéo module là **hành động toàn cục** trong `nav_main.xml`, do Phát tạo sẵn ở P1-06. Chủ module chỉ gọi:

| Hành động | Đích | Tham số | Ai gọi |
| --- | --- | --- | --- |
| `action_global_listing_detail` | B03 | `listingId` | B, C, D, A (thông báo) |
| `action_global_chat` | C02 | `listingId`, `conversationId` (có thể rỗng) | B03 |
| `action_global_deal_detail` | C03 | `dealId` | C, D, A (thông báo) |
| `action_global_write_review` | C05 | `dealId` | C03 |
| `action_global_user_profile` | D02 | `userId` | B03, C02, C03 |
| `action_global_seller_trust` | D03 | `userId` | B03, D02 |
| `action_global_report` | D04 | `targetType`, `targetId` | B03, C02, D02 |
| `action_global_create_complaint` | D05 | `dealId` | C03 |
| `action_global_my_shop` | B05 | | D01 |
| `action_global_saved_listings` | B06 | | D01 |
| `action_global_deal_history` | C04 | | D01 |
| `action_global_my_reports` | D07 | | D01 |
| `action_global_personal_info`, `action_global_edit_profile`, `action_global_address_list`, `action_global_account_settings` | A04, A05, A06 | | D01 |
| `action_global_notifications` | C06 | | Thanh trên của `MainActivity` |

Cần thêm một hành động toàn cục thì mở issue cho Phát; không tự thêm.

## 5. Lớp dùng chung: ai sửa phương thức nào

Các lớp ở `model`, `model.enums`, `data`, `data.local`, `util` được Phát tạo **đầy đủ chữ ký** ở P1, đúng như `workflow/docs/architecture/dac-ta-lop/`.

- Phương thức chưa làm có thân gọi `cb.onError(...)` với hằng `DataCallback.NOT_READY`, để màn hình báo lỗi rõ ràng thay vì chạy sai.
- Ở P2, mỗi người **chỉ thay thân các phương thức được giao** trong issue của mình. Không đổi chữ ký, không thêm phương thức công khai mới, không sửa phương thức của người khác.
- Thiếu một phương thức thì ghi vào issue và báo Phát; Phát thêm bằng một Pull Request nhỏ, rồi cập nhật đặc tả lớp.

| Lớp | Phương thức | Chủ |
| --- | --- | --- |
| `AuthRepository` | tất cả | Ngân |
| `UserRepository` | `getProfile`, `updateProfile`, `saveAddress`, `deleteAddress`, `getAddresses`, `saveIdentity`, `getIdentity`, `updateNotificationPrefs`, `touchLastActive`, `deactivate` | Ngân |
| `UserRepository` | `saveListing`, `unsaveListing`, `getSavedIds` | Tú |
| `UserRepository` | `follow`, `unfollow`, `getFollowingIds`, `requestVerification` | Phúc |
| `NotificationRepository` | `getForUser`, `markRead` | Ngân |
| `NotificationRepository` | `create` | Phát (P1, mọi người dùng) |
| `MediaRepository` | tất cả | Phát (P1, mọi người dùng) |
| `CategoryRepository` | `getAll`, `getChildren` | Phát (P1) |
| `CategoryRepository` | `add`, `update`, `delete` | Phát |
| `ListingRepository` | tất cả | Tú |
| `ChatRepository` | tất cả | Hải Long |
| `DealRepository` | tất cả trừ hai dòng dưới | Hải Long |
| `DealRepository` | `releaseDueDeals`, `completeDueDeals`, `listAll` | Phát |
| `ReviewRepository` | `create` | Hải Long |
| `ReviewRepository` | `getReceived`, `getGiven` | Phúc |
| `ReportRepository` | `create`, `cancel`, `getMyReports`, `exportCsv` | Phúc |
| `ReportRepository` | `getByStatus`, `countByTarget`, `closeByTarget` | Phát |
| `ComplaintRepository` | tất cả | Phúc |
| `AdminRepository` | tất cả | Phát |
| `RecommendationEngine`, `GeoUtils` | tất cả | Tú |
| `KeywordFilter`, `SessionManager`, `NetworkMonitor`, `FormatUtils`, `AutoReleaseWorker` | tất cả | Phát |
| `AuthViewModel`, `NotificationViewModel` | tất cả | Ngân |
| `ProfileViewModel` | `updateProfile`, `changeAvatar`, `saveAddress`, `deleteAddress`, `loadIdentity`, `saveIdentity`, `updateNotificationPrefs`, `deactivateAccount` | Ngân |
| `ProfileViewModel` | `loadProfile`, `follow`, `unfollow`, `requestVerification` | Phúc |
| `ProfileViewModel` | `loadSaved` | Tú |
| `ShopViewModel`, `FeedViewModel`, `SearchViewModel`, `ListingDetailViewModel` | tất cả | Tú |
| `ChatViewModel`, `DealViewModel` | tất cả trừ `checkAutoRelease` | Hải Long |
| `DealViewModel` | `checkAutoRelease` | Phát |
| `TrustViewModel` | `submitReview` | Hải Long |
| `TrustViewModel` | phần còn lại | Phúc |
| `AdminComplaintViewModel` | tất cả | Phúc |
| `AdminUserViewModel`, `AdminContentViewModel`, `AdminDealViewModel` | tất cả | Phát |
| DAO và entity Room | `ListingDao`, `HistoryDao`, `ListingEntity`, `RecentViewEntity`, `RecentSearchEntity` | Tú |
| DAO và entity Room | `ChatDao`, `DealDao`, `ConversationEntity`, `MessageEntity`, `DealEntity` | Hải Long |

Lớp mô hình miền (`model`) và enum: Phát tạo ở P1 kèm unit test cho các quy tắc; sau đó **không ai sửa** nếu không có issue riêng.

## 6. File chung chỉ một người sửa

| File | Chủ | Người khác muốn đổi thì |
| --- | --- | --- |
| `PUBGApp/gradle/libs.versions.toml`, `PUBGApp/app/build.gradle.kts` | Phát | Mở issue `build(deps)` |
| `AndroidManifest.xml` | Phát (khai báo đủ 3 Activity, quyền mạng, quyền ảnh ở P1) | Mở issue |
| `res/values/colors.xml`, `themes.xml`, `dimens.xml`, `strings_common.xml` | Phát | Mở issue `area:ui` |
| `nav_main.xml`, `nav_admin.xml`, menu của thanh điều hướng | Phát | Mở issue |
| `AppDatabase`, `Converters` | Phát (đủ 6 entity, 4 DAO ở P1) | Mở issue; đổi bảng cần hỏi trước (R6) |
| `firebase/firestore.rules`, `firebase/firestore.indexes.json` | Phát | Ghi luật cần có vào mô tả Pull Request, Phát gộp vào |
| `workflow/docs/architecture/`, `workflow/docs/data/` | Phát | Sửa trong cùng Pull Request khi đổi hành vi (R7), Phát duyệt |
| `workflow/plan/` | Phát | Đề xuất trong issue |
