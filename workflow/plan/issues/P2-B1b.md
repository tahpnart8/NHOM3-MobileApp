TITLE: feat(listing): lưu nháp và thuộc tính tình trạng theo danh mục
LABELS: type:feature, area:listing, module:b, priority:p1
ASSIGNEES: tranannhtu21012006
PHASE: P2

Plan item: P2-B1b

### Bối cảnh
1. PUBGApp (Pre-owned Users' Bargain Grounds) là ứng dụng Android mua bán, trao đổi, cho tặng đồ cũ, viết bằng Java 17, XML Views và ViewBinding, dùng Firebase (Authentication, Firestore gói Spark) và Room.
2. Module B (Tin đăng và khám phá, người làm: Trần Anh Tú) lo đăng tin, kệ hàng, trang chủ, tìm kiếm, chi tiết tin và tin đã lưu.
3. Issue này thêm vào màn B04 `PostListingFragment` các ô thuộc tính tình trạng theo danh mục và nút "Lưu nháp" (UC09 luồng 3a, UC10 luồng 7a).
4. Đọc trước: `workflow/docs/ui/b-kham-pha-tin-dang/README.md#b04-đăng-tin`, `workflow/docs/product/use-case/uc-b-tin-dang.md#uc09-quản-lý-tin-nháp` cùng `#uc10-tạo-tin-đăng`, `workflow/docs/architecture/luong-xu-ly.md` mục "2. Đăng tin" (điểm 4).
5. Đã có trên `main`: khung B04 và biểu mẫu đăng tin mới, `ShopViewModel.createListing` và `ListingRepository.create` (từ P2-B1a); `CategoryRepository.getAll`; mọi ViewModel đã có `loading`, `error`, `done` kèm getter công khai, `ShopViewModel` đã có `categories`, `createdListingId`, `loadCategories()`; mọi lớp còn lại đủ chữ ký (thân chưa làm trả `DataCallback.NOT_READY`).

### Mã tính năng
F-04

### Mã việc kế hoạch
P2-B1b. Nửa sau của P2-B1, làm sau P2-B1a (cùng sửa `PostListingFragment` và `ShopViewModel`, nên không làm song song).

### Module
B. Tin đăng và khám phá (Trần Anh Tú)

### STT trong bảng đặc tả
16 (thuộc tính tình trạng chi tiết theo danh mục), 22 (lưu nháp). Cả hai có Phạm vi "Đợt này".

### Màn hình mockup
B04: phần "Thuộc tính tình trạng của danh mục" và nút "Lưu nháp". Cả hai chưa có trong ảnh `workflow/docs/ui/b-kham-pha-tin-dang/b04-dang-tin.png` nhưng vẫn phải làm (mục "Khác với ảnh" của B04).

### Làm gì và vì sao
Khi người bán chọn một danh mục, biểu mẫu B04 hiện thêm các ô nhập theo `Category.conditionFields` của danh mục đó (ví dụ Dung lượng pin, Bảo hành); giá trị nhập được ghi vào `Listing.conditionValues` cho cả tin đăng lẫn tin nháp, và hiện ở mục "Tình trạng chi tiết" của B03. Người bán cũng bấm được "Lưu nháp": tin được ghi với trạng thái DRAFT, không bắt buộc đủ thông tin (chỉ cần có tiêu đề, hoặc mô tả, hoặc ít nhất 1 ảnh), chưa hiện cho người khác, và đăng sau từ kệ hàng B05. Lưu nháp không cần tài khoản đã được xác thực: tài khoản chưa được duyệt vẫn soạn và lưu nháp được, chỉ bị chặn ở nút Đăng tin (quyết định leader 2026-10-22, UC09 và UC17). Luồng này đi cùng đường với đăng tin nhưng gọi `saveDraft` và không kiểm đủ thông tin. Issue này phục vụ dòng barem R-Chapter 4 (input control: các ô nhập chữ được dựng động theo thuộc tính của danh mục đã chọn; điều hướng bằng Navigation component với hành động toàn cục `action_global_my_shop`), dòng R-Chapter 7 (async: nén ảnh và ghi Firestore qua `DataCallback`, không chạy trên luồng chính) và dòng R-Chapter 3 (unit test JUnit cho `buildConditionValues` và `canSaveDraft` trong `ShopViewModelTest`).

### Tiêu chí nghiệm thu
- [ ] Chọn danh mục thì các ô thuộc tính hiện đúng theo `Category.conditionFields` của danh mục đó; đổi sang danh mục khác thì ô cũ biến mất và giá trị của ô không còn bị giữ. Danh mục không có thuộc tính thì không hiện ô nào.
- [ ] Đăng tin với thuộc tính đã nhập: `listings/{id}.conditionValues` có đúng khóa là tên thuộc tính của danh mục và giá trị người bán nhập; ô để trống thì không ghi khóa.
- [ ] Bấm "Lưu nháp" khi mới nhập vài trường (kể cả chưa có ảnh): ghi một tài liệu `listings` có `status` DRAFT, `sellerId` là người đang đăng nhập, `statusReason` rỗng; ảnh đã chọn (0 đến 5) được ghi vào `images`, `imageIds` đúng số ảnh, `coverThumb` chỉ có khi có ít nhất một ảnh. PR ghi rõ đã kiểm trên Firebase Console.
- [ ] Lưu nháp bằng tài khoản chưa được xác thực (`verificationStatus` khác APPROVED) vẫn thành công, không hiện hộp thoại xác thực.
- [ ] Chưa nhập gì (không tiêu đề, không mô tả, không ảnh) mà bấm "Lưu nháp": báo "Hãy nhập tiêu đề, mô tả hoặc chọn ít nhất 1 ảnh để lưu nháp" và không ghi gì.
- [ ] Tin nháp không có trong `getFeed`, `search` và không hiện cho tài khoản khác (kiểm bằng tài khoản thứ hai).
- [ ] Lưu xong hiện "Đã lưu bản nháp" và chuyển tới B05 bằng `action_global_my_shop` (không chọn sẵn được tab Nháp, xem câu hỏi 1).
- [ ] Lưu nháp ghi ảnh lỗi thì xóa các ảnh đã ghi của lần này, báo lỗi tiếng Việt, không ghi tin, nội dung form còn nguyên.
- [ ] Offline: nút "Lưu nháp" bị khóa kèm dòng "Không thể đồng bộ dữ liệu với máy chủ, vui lòng kiểm tra lại kết nối mạng"; chữ đang nhập được giữ.
- [ ] Trạng thái DRAFT chỉ ra đời qua `ListingRepository.saveDraft`; màn hình không ghi trực tiếp trường `status` (R18, `trang-thai.md` mục 1, dòng "Lưu nháp").
- [ ] Xoay màn hình giữ nguyên danh mục đã chọn và giá trị các ô thuộc tính.
- [ ] Phương thức của module khác trả `DataCallback.NOT_READY` thì màn hình hiện đúng lỗi đó.
- [ ] Trạng thái lấy từ ViewModel: `ShopViewModel.loading` bật trong lúc ghi ảnh và tin nháp (khóa hai nút), `ShopViewModel.error` hiện một lần rồi xóa, `ShopViewModel.done` bật đúng một lần khi lưu nháp xong để màn hình sang B05 rồi được đặt lại false.
- [ ] Unit test mở rộng `ShopViewModelTest` cho hai hàm package-private: ghép `conditionValues` (bỏ ô trống, bỏ khóa không thuộc danh mục) và `canSaveDraft` (có tiêu đề, hoặc mô tả, hoặc 1 ảnh thì được; trống cả ba thì không).
- [ ] `./gradlew assembleDebug testDebugUnitTest lintDebug` và `sh workflow/scripts/check-java-style.sh` đều qua.
- [ ] PR liệt kê từng lớp, phương thức, trường công khai mới kèm dòng tài liệu định nghĩa (R16).

### Tài liệu phải đọc
- `AGENTS.md` (R12, R15 đến R23), `workflow/plan/so-huu.md` mục 3, 4, 5, 6.
- `workflow/docs/ui/b-kham-pha-tin-dang/README.md` mục B04 (đặc biệt "Thành phần chính", "Hành động và điều hướng", "Khác với ảnh").
- `workflow/docs/product/use-case/uc-b-tin-dang.md` UC09 (luồng 3a, ngoại lệ 4e, BR1 đến BR3), UC10 (luồng 7a, quy tắc 3).
- `workflow/docs/architecture/luong-xu-ly.md` mục 2, điểm 4 và 5.
- `workflow/docs/architecture/dac-ta-lop/data.md#listingrepository` (dòng `saveDraft`), `#categoryrepository`.
- `workflow/docs/architecture/dac-ta-lop/model.md#category` (`conditionFields`, `hasConditionField`) và `#listing` (`conditionValues`, `isDraft`).
- `workflow/docs/architecture/dac-ta-lop/viewmodel.md#shopviewmodel`.
- `workflow/docs/architecture/trang-thai.md` mục 1 (dòng "Lưu nháp").
- `workflow/docs/data/firestore-schema.md#listings`, `#categories`.
- `workflow/docs/data/security-rules.md` mục 10, dòng "Tạo bản nháp" và "Sửa nội dung".
- `workflow/docs/product/barem-diem.md` (dòng barem mà issue phục vụ).

### Lớp, trường, trạng thái sẽ dùng
- `PostListingFragment`; `ShopViewModel.saveDraft(Listing listing, List<Uri> imageUris)` và các LiveData đã có `loading` (`LiveData<Boolean>`), `error` (`LiveData<String>`), `done` (`LiveData<Boolean>`), `categories` (`LiveData<List<Category>>`), mỗi LiveData có getter công khai.
- `ListingRepository.saveDraft(Listing listing, DataCallback<String> cb)`.
- `Category`: `conditionFields` (`List<String>`), `hasConditionField(String)`, `isRoot()`; `Listing`: `conditionValues` (`Map<String,String>`), `isDraft()`, `status`, `statusReason`, `imageIds`, `coverThumb`, `title`, `description`.
- `ListingStatus.DRAFT`.
- `MediaRepository.saveImage(Uri uri, ImagePurpose purpose, DataCallback<String> cb)`, `makeThumb(Uri uri, DataCallback<String> cb)`, `delete(String id, DataCallback<Void> cb)` (chỉ gọi); `ImagePurpose.LISTING`.
- `BaseRepository.currentUserId()`, `NetworkMonitor.getInstance()`, `isOnline()`.
- Hành động toàn cục `action_global_my_shop`.

Tên nội bộ do issue này đặt (package-private, ghi vào PR ở mục tên mới): trong `ShopViewModel` các hàm `static Map<String,String> buildConditionValues(List<String> conditionFields, Map<String,String> typed)` và `static boolean canSaveDraft(Listing listing, int imageCount)`; layout `item_listing_condition_field.xml`.

Chi tiết triển khai, ghi một dòng bằng skill `pubg-record-decision`: `saveDraft` nhận `Listing` chưa có `id` thì tạo tài liệu mới, đã có `id` thì cập nhật đúng tin nháp đó (`data.md` chỉ ghi "ghi tin với trạng thái DRAFT"; P2-B2b dùng cách này để sửa tin nháp); tin nháp không cần `searchKeywords` và `flagged`, hai trường này được tính lại khi đăng bản nháp.

### File được tạo hoặc sửa
- [sửa] `PUBGApp/app/src/main/java/com/nhom3/pubgapp/ui/listing/PostListingFragment.java`
- [sửa] `PUBGApp/app/src/main/res/layout/fragment_listing_post.xml`
- [tạo] `PUBGApp/app/src/main/res/layout/item_listing_condition_field.xml` (một ô thuộc tính)
- [sửa] `PUBGApp/app/src/main/res/values/strings_listing.xml`
- [sửa] `PUBGApp/app/src/main/java/com/nhom3/pubgapp/viewmodel/ShopViewModel.java` (chỉ thân `saveDraft`, việc đẩy dữ liệu vào `loading`, `error`, `done`, và hai hàm package-private ở trên)
- [sửa] `PUBGApp/app/src/main/java/com/nhom3/pubgapp/data/ListingRepository.java` (chỉ thân `saveDraft`)
- [sửa] `PUBGApp/app/src/test/java/com/nhom3/pubgapp/viewmodel/ShopViewModelTest.java`

### Các bước đề nghị
Người làm có thể đổi thứ tự nhưng không được đổi tên nào ở trên.
1. Trong `ShopViewModel` viết hai hàm package-private: `static Map<String,String> buildConditionValues(List<String> conditionFields, Map<String,String> typed)` (chỉ giữ khóa có trong `conditionFields` và giá trị không rỗng) và `static boolean canSaveDraft(Listing listing, int imageCount)` (true khi `title` không rỗng, hoặc `description` không rỗng, hoặc `imageCount` lớn hơn 0).
2. Mở rộng `ShopViewModelTest` cho hai hàm trên, chạy `./gradlew testDebugUnitTest`.
3. Làm thân `ListingRepository.saveDraft(Listing listing, DataCallback<String> cb)`: `sellerId` lấy từ `currentUserId()`, `status` DRAFT, `statusReason` rỗng, không kiểm `User.canSell`, không bắt buộc đủ thông tin. `Listing` chưa có `id` thì tạo mới (Rules dòng "Tạo bản nháp": `createdAt` và `updatedAt` bằng `FieldValue.serverTimestamp()`); đã có `id` thì chỉ cập nhật các trường nội dung và `updatedAt` (Rules dòng "Sửa nội dung"), không ghi lại `createdAt`. Trả mã tin qua `onSuccess`.
4. Làm thân `ShopViewModel.saveDraft(Listing listing, List<Uri> imageUris)`: kiểm `canSaveDraft`, không đủ thì dừng và báo; ghi từng ảnh bằng `MediaRepository.saveImage(uri, ImagePurpose.LISTING, cb)`; nếu có ảnh thì `makeThumb` ảnh đầu làm `coverThumb`; đặt `imageIds`; gọi `ListingRepository.saveDraft`; lỗi thì `MediaRepository.delete` các ảnh vừa ghi. Bật `loading` khi bắt đầu và tắt khi xong; thành công thì bật `done`, lỗi thì đặt `error`. Dùng lớp ẩn danh `DataCallback`, không lambda.
5. Mở rộng `createListing` (đã làm ở P2-B1a) chỉ ở chỗ nhận `Listing` có `conditionValues`: không cần đổi chữ ký, vì `conditionValues` nằm sẵn trong `Listing` do Fragment dựng.
6. Trong `fragment_listing_post.xml` thêm vùng chứa các ô thuộc tính và nút "Lưu nháp" cạnh nút "Đăng tin". Làm `item_listing_condition_field.xml` (nhãn là tên thuộc tính, một ô nhập chữ; id view lowerCamelCase như `textFieldName`, `editFieldValue`).
7. Trong `PostListingFragment`: khi danh mục đổi, dựng lại các ô theo `Category.conditionFields`, giữ giá trị đã nhập của ô còn tồn tại (`Category.hasConditionField(String)`), bỏ giá trị của ô không còn. Dựng `conditionValues` bằng `buildConditionValues` cho cả "Đăng tin" lẫn "Lưu nháp". Lưu giá trị các ô bằng `onSaveInstanceState`.
8. Nút "Lưu nháp": nếu `NetworkMonitor.getInstance().isOnline()` là false thì báo offline; còn lại gọi `ShopViewModel.saveDraft`. Không hiện hộp thoại xác thực. Khi `done` bật thì Snackbar "Đã lưu bản nháp" rồi `action_global_my_shop`; khi `error` có giá trị thì hiện câu lỗi rồi xóa.
9. Thêm chuỗi vào `strings_listing.xml` (khóa theo mẫu `<module>_<màn>_<việc>`, ví dụ `listing_post_button_save_draft`).
10. Chạy các lệnh ở mục "Tự kiểm trước khi mở Pull Request", thử bằng hai tài khoản (xem tin nháp chỉ chủ thấy).

### File dùng chung cần thêm dòng
Không có.

### Cấm đụng
- File chỉ Phát sửa: `AndroidManifest.xml`, `nav_main.xml`, `nav_admin.xml`, menu thanh điều hướng, `themes.xml`, `colors.xml`, `dimens.xml`, `strings_common.xml`, `AppDatabase`, `Converters`, mọi file Gradle, mọi file trong `firebase/`.
- Lớp `model` và `model.enums`: không sửa.
- `CategoryRepository` (Phát): chỉ gọi `getAll` (qua `ShopViewModel.loadCategories()`). Cấu hình `conditionFields` là việc của admin (P2-E4); issue này chỉ đọc.
- Các phương thức khác của `ListingRepository` và `ShopViewModel`: thuộc P2-B1a, P2-B2a, P2-B2b.

### Phụ thuộc
P2-B1a.

### Ngoài phạm vi
- Mở một tin nháp để sửa rồi lưu hoặc đăng: P2-B2b (chế độ sửa của B04) và P2-B2a (đăng, xóa nháp ở B05).
- Quản lý danh mục và `conditionFields` phía admin (STT 80): P2-E4.
- Chọn sẵn tab Nháp khi mở B05: chờ câu hỏi 1.
- Mọi chức năng "Phát triển sau"; bố cục ngang: P3-Ba.

### Tài liệu cần cập nhật
Không có, trừ khi các câu hỏi dưới đây đổi hành vi (khi đó cập nhật `viewmodel.md`, `so-huu.md` theo R7).

### Tự kiểm trước khi mở Pull Request
- [ ] `cd PUBGApp && ./gradlew testDebugUnitTest` chạy `ShopViewModelTest`: ca ghép `conditionValues` và `canSaveDraft` đều qua.
- [ ] Chọn danh mục "Điện thoại" (hoặc danh mục có thuộc tính): các ô thuộc tính hiện; đổi danh mục: ô cũ biến mất.
- [ ] Bằng tài khoản chưa APPROVED, nhập tiêu đề rồi bấm "Lưu nháp": Console có 1 tài liệu `listings` `status` DRAFT; màn hình sang B05; không có hộp thoại xác thực.
- [ ] Bằng tài khoản thứ hai, mở trang chủ và tìm đúng tiêu đề đó: không thấy tin nháp.
- [ ] Để trống mọi thứ rồi bấm "Lưu nháp": hiện lời báo và không có tài liệu mới.
- [ ] Bật chế độ máy bay: nút "Lưu nháp" khóa kèm dòng giải thích, chữ còn nguyên.
- [ ] Xoay màn hình khi đã chọn danh mục và nhập giá trị thuộc tính: không mất gì.
- [ ] `cd PUBGApp && ./gradlew assembleDebug testDebugUnitTest lintDebug` qua.
- [ ] `sh workflow/scripts/check-java-style.sh` không báo lỗi.
- [ ] Danh sách mọi lớp, phương thức, trường công khai mới kèm dòng tài liệu định nghĩa đã dán vào PR; tên nội bộ được ghi riêng (R16).

### Quy mô
- [x] Đây là một lát cắt dọc của một module, khoảng nửa ngày đến một ngày, một Pull Request.

### Câu hỏi cho leader
1. Sau khi lưu nháp, UC10 luồng 7a3 muốn "chuyển về tab Nháp trong B05", nhưng `action_global_my_shop` không có tham số nên không chọn sẵn được tab. Đề xuất Phát thêm một tham số tùy chọn chỉ tab vào hành động này, hoặc chấp nhận mở B05 ở tab mặc định.
2. `ListingRepository.saveDraft` chỉ ghi "ghi tin với trạng thái DRAFT". Cách hiểu mặc định của issue này: chưa có `id` thì tạo mới, đã có `id` thì cập nhật tin nháp đó. Xin xác nhận, hoặc cho một phương thức riêng để sửa nháp.
