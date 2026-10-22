TITLE: feat(ui): bố cục ngang và giữ trạng thái khi xoay màn B01 đến B06
LABELS: type:feature, area:ui, module:b, priority:p2
ASSIGNEES: tranannhtu21012006
PHASE: P3

Plan item: P3-Ba

### Bối cảnh
1. PUBGApp (Pre-owned Users' Bargain Grounds) là ứng dụng Android mua bán, trao đổi, cho tặng đồ cũ, viết bằng Java 17, XML Views và ViewBinding, dùng Firebase (Authentication, Firestore gói Spark) và Room.
2. Module B (Tin đăng và khám phá, người làm: Trần Anh Tú) lo đăng tin, kệ hàng, trang chủ, tìm kiếm, chi tiết tin và tin đã lưu.
3. Issue này làm bố cục ngang (`layout-land`) và rà soát việc giữ trạng thái khi xoay cho sáu màn B01 đến B06, kể cả các phần mới của đợt viết lại use case (tab "Người dùng" của B02, hàng "Dành cho bạn", chế độ khách, dải báo xác thực).
4. Đọc trước: `workflow/plan/phase-3-hoan-thien.md` (mục tiêu và "Danh sách kiểm tra"), `workflow/docs/ui/b-kham-pha-tin-dang/README.md` (cả sáu màn), `workflow/docs/conventions/giao-dien.md` và `.agents/skills/pubg-ui/SKILL.md` mục 4.
5. Đã có trên `main`: toàn bộ chức năng P2-B1a đến P2-B7 của module B (sáu Fragment, các ViewModel kèm `loading`, `error`, `done` và getter công khai, adapter, layout dọc), giá trị màu thật trong `colors.xml` và `themes.xml` (P1-03), thành phần chung `view_*.xml`.

### Mã tính năng
F-04, F-05, F-06, F-07

### Mã việc kế hoạch
P3-Ba. Nửa đầu của P3-B (bố cục ngang và giữ trạng thái khi xoay). Nửa sau là P3-Bb (offline). Tách vì sáu màn cần bố cục ngang riêng, còn offline đụng tới repository và mạng.

### Module
B. Tin đăng và khám phá (Trần Anh Tú)

### STT trong bảng đặc tả
Không có STT riêng. Đây là yêu cầu cứng của môn học (giao diện thích ứng khi xoay màn hình và giữ nguyên trạng thái), ghi ở `workflow/docs/product/features.md` mục "Yêu cầu môn học".

### Màn hình mockup
B01, B02, B03, B04, B05, B06. Mockup chỉ vẽ chiều dọc; `workflow/docs/ui/README.md` mục "Ngoài phạm vi đợt này" để bố cục ngang cho giai đoạn code, nên bố cục ngang tự thiết kế theo token của `giao-dien.md`.

### Làm gì và vì sao
Cả sáu màn của module B dùng được ở chiều ngang, có bố cục riêng trong `layout-land`, và xoay qua lại không mất gì: không tải lại dữ liệu, không mất chữ đang nhập, không mất vị trí cuộn, bộ lọc, tab, ảnh đã chọn hay vị trí băng chuyền. Phần lớn trạng thái đã nằm trong ViewModel từ P2; issue này rà từng màn, sửa chỗ còn mất trạng thái và vẽ bố cục ngang. Các trạng thái chưa nằm trong ViewModel (chữ biểu mẫu, ảnh đã chọn, tab đang chọn, giá trị bảng lọc chưa bấm "Áp dụng", vị trí băng chuyền) giữ bằng `onSaveInstanceState` của Fragment. Issue này phục vụ dòng barem R-Chapter 5 (layout thích ứng và thư mục resource thay thế: sáu tệp trong `res/layout-land/` song song với `res/layout/`, cùng id view), dòng R-Chapter 2 (vòng đời Activity và Fragment, giữ trạng thái khi xoay bằng ViewModel và `onSaveInstanceState`) và dòng R-Chapter 1 (layout và resource: `ConstraintLayout`, token màu và khoảng cách).

### Tiêu chí nghiệm thu
- [ ] Mỗi màn B01 đến B06 có `layout-land` riêng, không bị cắt hay chồng chữ ở máy ảo điện thoại và ở máy ảo màn hình nhỏ. PR có ảnh chụp từng màn ở chiều ngang và chiều dọc.
- [ ] B01: lưới tin hiện 3 hoặc 4 cột theo chiều rộng (tính trong mã từ hướng màn hình, không đổi `dimens.xml`); kênh đang chọn, danh sách đã tải, vị trí cuộn còn nguyên sau khi xoay; dải "Dành cho bạn" giữ vị trí cuộn và không tính lại; ở chế độ khách nút "Đăng nhập" và hộp thoại mời đăng nhập vẫn đúng sau khi xoay.
- [ ] B02: tab đang chọn ("Tin đăng" hoặc "Người dùng"), từ khóa trong từng ô, bộ lọc đã chọn (kể cả khi chưa bấm "Áp dụng", gồm khu vực và bán kính), trạng thái mở hoặc đóng của bảng lọc, kết quả tin, kết quả người dùng và vị trí cuộn còn nguyên; ở chiều ngang bảng lọc và kết quả có thể đứng cạnh nhau; vị trí đã lấy cho bán kính được giữ và không xin quyền lại.
- [ ] B03: băng chuyền ảnh giữ ảnh đang xem; chiều ngang chia hai cột (ảnh bên trái, thông tin bên phải) hoặc cuộn dọc đủ nội dung; không tải lại ảnh đầy đủ khi xoay (`ListingDetailViewModel.images` còn trong ViewModel, không gọi lại `loadImages()`); trạng thái trái tim giữ nguyên; tin tương tự và giá tham khảo không tính lại; hộp thoại đăng nhập hay xác thực đang mở không lỗi khi xoay.
- [ ] B04: mọi chữ đã nhập, thẻ đã chọn, các thuộc tính theo danh mục, danh sách ảnh đã chọn (và thứ tự), chế độ đăng mới hay sửa tin còn nguyên; xoay giữa lúc đang đăng không đăng hai lần và không mất tiến trình; dải báo xác thực còn đúng.
- [ ] B05: tab, cách sắp xếp, bộ lọc danh mục và vị trí cuộn còn nguyên; menu ba chấm đang mở không làm lỗi khi xoay.
- [ ] B06: danh sách và vị trí cuộn còn nguyên.
- [ ] Lỗi đã hiện rồi (`error` của từng ViewModel) không hiện lại sau khi xoay, và `done` không kích hoạt lại thao tác đã xong (màn hình đặt lại về false sau khi dùng).
- [ ] Kiểm bằng cách bật chế độ máy bay rồi xoay: dữ liệu đã tải vẫn còn trên màn (chứng tỏ không đọc lại mạng).
- [ ] Không gây lỗi bộ nhớ: xoay liên tục 10 lần ở mỗi màn không treo hay văng ứng dụng (ghi kết quả vào PR).
- [ ] Mọi chữ mới nằm trong `strings_listing.xml`; mọi giá trị màu, khoảng cách, cỡ chữ lấy từ token (không viết thẳng mã màu). Không thêm quy tắc mới nên không cần unit test mới; test hiện có vẫn qua.
- [ ] `./gradlew assembleDebug testDebugUnitTest lintDebug` và `sh workflow/scripts/check-java-style.sh` đều qua.
- [ ] PR liệt kê từng lớp, phương thức, trường công khai mới kèm dòng tài liệu định nghĩa (R16); issue này không dự kiến thêm tên công khai nào.

### Tài liệu phải đọc
- `AGENTS.md` (R3, R15 đến R22), `workflow/plan/phase-3-hoan-thien.md`.
- `workflow/docs/product/features.md` mục "Yêu cầu môn học".
- `workflow/docs/ui/b-kham-pha-tin-dang/README.md` (cả sáu màn) và `workflow/docs/ui/README.md` mục "Điều hướng" và "Ngoài phạm vi đợt này".
- `workflow/docs/conventions/giao-dien.md` (mục 1 đến 6) và `.agents/skills/pubg-ui/SKILL.md` (mục 4 và 7).
- `workflow/docs/architecture/overview.md` mục 8 ("Xoay màn hình") và mục 10 (kịch bản xoay).
- `workflow/docs/conventions/android-java.md`.
- `workflow/docs/architecture/dac-ta-lop/viewmodel.md` (các mục `feedviewmodel`, `searchviewmodel`, `listingdetailviewmodel`, `shopviewmodel`, `profileviewmodel` phần `savedListings`).
- `workflow/docs/product/barem-diem.md` (dòng barem mà issue phục vụ).

### Lớp, trường, trạng thái sẽ dùng
Chỉ các lớp đã dùng ở P2: `HomeFragment`, `SearchFragment`, `ListingDetailFragment`, `PostListingFragment`, `MyShopFragment`, `SavedListingsFragment` và các ViewModel `FeedViewModel`, `SearchViewModel`, `ListingDetailViewModel`, `ShopViewModel`, `ProfileViewModel` (phần `loadSaved`, `unsaveListing`, `savedListings`). Không thêm tên công khai mới. Trạng thái chưa nằm trong ViewModel giữ bằng `onSaveInstanceState` của Fragment. Số cột lưới tính từ `Configuration.orientation` trong mã Fragment, không thêm `dimens.xml`.

### File được tạo hoặc sửa
- [tạo] `PUBGApp/app/src/main/res/layout-land/fragment_listing_home.xml`
- [tạo] `PUBGApp/app/src/main/res/layout-land/fragment_listing_search.xml`
- [tạo] `PUBGApp/app/src/main/res/layout-land/fragment_listing_detail.xml`
- [tạo] `PUBGApp/app/src/main/res/layout-land/fragment_listing_post.xml`
- [tạo] `PUBGApp/app/src/main/res/layout-land/fragment_listing_my_shop.xml`
- [tạo] `PUBGApp/app/src/main/res/layout-land/fragment_listing_saved.xml`
- [sửa] `PUBGApp/app/src/main/java/com/nhom3/pubgapp/ui/listing/HomeFragment.java`
- [sửa] `PUBGApp/app/src/main/java/com/nhom3/pubgapp/ui/listing/SearchFragment.java`
- [sửa] `PUBGApp/app/src/main/java/com/nhom3/pubgapp/ui/listing/ListingDetailFragment.java`
- [sửa] `PUBGApp/app/src/main/java/com/nhom3/pubgapp/ui/listing/PostListingFragment.java`
- [sửa] `PUBGApp/app/src/main/java/com/nhom3/pubgapp/ui/listing/MyShopFragment.java`
- [sửa] `PUBGApp/app/src/main/java/com/nhom3/pubgapp/ui/listing/SavedListingsFragment.java`
- [sửa] `PUBGApp/app/src/main/res/values/strings_listing.xml` (chỉ nếu bố cục ngang cần chuỗi mới)

Bố cục ngang giữ cùng id view (lowerCamelCase `<loại><Việc>`) với bố cục dọc để ViewBinding dùng chung một lớp binding; các ViewModel, repository và adapter đã có được dùng lại, không sửa thân của chúng trừ khi phát hiện trạng thái bị mất (khi đó ghi rõ ở PR).

### Các bước đề nghị
Người làm có thể đổi thứ tự nhưng không được đổi tên nào ở trên.
1. Chạy ứng dụng, với từng màn B01 đến B06 xoay ngang rồi dọc và ghi lại chỗ nào mất gì (danh sách, chữ, tab, bộ lọc, ảnh, vị trí cuộn). Ghi kết quả vào PR.
2. Với mỗi chỗ mất trạng thái, kiểm trạng thái đó đang nằm ở đâu: nếu thuộc ViewModel đã có thuộc tính (`feed`, `forYou`, `categories`, `results`, `userResults`, `filter`, `listing`, `seller`, `sellerStats`, `images`, `saved`, `followingSeller`, `similar`, `priceRange`, `myListings`, `createdListingId`, `savedListings`, và `loading`, `error`, `done` của từng ViewModel) thì sửa Fragment để quan sát lại thay vì gọi tải mới (chỉ gọi tải khi dữ liệu còn rỗng); nếu chưa thuộc ViewModel thì giữ bằng `onSaveInstanceState` và khôi phục trong `onViewCreated`.
3. B01: số cột lưới tính từ hướng màn hình (hai cột khi dọc, 3 hoặc 4 khi ngang); lưu và khôi phục vị trí cuộn của lưới và của dải "Dành cho bạn" (`LayoutManager.onSaveInstanceState`).
4. B02: lưu tab đang chọn, chữ ở hai ô nhập, các giá trị bảng lọc chưa áp dụng và trạng thái mở bảng; ở `layout-land` đặt bảng lọc bên trái, kết quả bên phải.
5. B03: lưu chỉ số ảnh đang xem của băng chuyền; `layout-land` chia hai cột (ảnh bên trái, thông tin cuộn bên phải); kiểm các hộp thoại (đăng nhập, xác thực) giữ nguyên khi xoay.
6. B04: kiểm lại toàn bộ trường của biểu mẫu, ô thuộc tính theo danh mục, ảnh đã chọn, chế độ sửa; `layout-land` cho phép cuộn đủ nội dung; khóa nút trong lúc đang đăng để xoay không tạo lần đăng thứ hai.
7. B05: lưu tab, cách sắp xếp, danh mục đang lọc; B06: lưu vị trí cuộn của lưới.
8. Vẽ sáu `layout-land` bằng `ConstraintLayout`, cùng id với bản dọc, màu và khoảng cách từ token. Mỗi màn có đủ bốn trạng thái (đang tải, có dữ liệu, rỗng, lỗi).
9. Thử xoay liên tục 10 lần ở mỗi màn; thử với chế độ máy bay (dữ liệu đã tải vẫn còn khi xoay).
10. Chạy các lệnh ở mục "Tự kiểm trước khi mở Pull Request".

### File dùng chung cần thêm dòng
Không có. Nếu phải sửa `AndroidManifest.xml` (ví dụ `configChanges`), không làm: Fragment phải sống qua xoay theo vòng đời chuẩn.

### Cấm đụng
- File chỉ Phát sửa: `AndroidManifest.xml`, `nav_main.xml`, `nav_admin.xml`, menu thanh điều hướng, `themes.xml`, `colors.xml`, `dimens.xml`, `strings_common.xml`, `AppDatabase`, `Converters`, mọi file Gradle, mọi file trong `firebase/`.
- Lớp `model` và `model.enums`: không sửa.
- Khung `MainActivity` và thanh điều hướng dưới (Phát): chỉ dùng. Màn của module khác (A, C, D, E): không sửa.
- Repository: không đổi hành vi (offline thuộc P3-Bb).

### Phụ thuộc
P2-B1a, P2-B1b, P2-B2a, P2-B2b, P2-B3, P2-B4a, P2-B4b, P2-B5, P2-B6a, P2-B6b, P2-B7.

### Ngoài phạm vi
- Xem lại dữ liệu khi mất mạng, khóa nút ghi: P3-Bb.
- Bố cục ngang của module khác; màn hình mới hoặc chức năng mới; mọi chức năng "Phát triển sau".
- Hỗ trợ máy tính bảng và chế độ nhiều cửa sổ.

### Tài liệu cần cập nhật
Không có.

### Tự kiểm trước khi mở Pull Request
- [ ] Với từng màn trong sáu màn: xoay ngang rồi dọc ba lần khi đã tải dữ liệu, đã gõ chữ và đã cuộn; không thấy vòng tải lại, không mất chữ, không mất vị trí cuộn.
- [ ] B02: gõ chữ ở cả hai tab, mở bảng lọc, chọn vài tiêu chí nhưng chưa bấm "Áp dụng", rồi xoay: mọi thứ còn nguyên.
- [ ] B03: vuốt tới ảnh thứ 3 rồi xoay: vẫn ở ảnh thứ 3.
- [ ] B04: nhập đủ biểu mẫu, chọn 3 ảnh, xoay: không mất gì; xoay trong lúc đang đăng: Console chỉ có một tin.
- [ ] Bật chế độ máy bay rồi xoay: dữ liệu đã tải vẫn còn trên màn.
- [ ] Xoay liên tục 10 lần ở mỗi màn: không treo, không văng.
- [ ] Ảnh chụp sáu màn ở chiều ngang đã dán vào PR; không có chữ cứng, màu viết thẳng hay thiếu `contentDescription` (`lintDebug` sạch).
- [ ] `cd PUBGApp && ./gradlew assembleDebug testDebugUnitTest lintDebug` qua.
- [ ] `sh workflow/scripts/check-java-style.sh` không báo lỗi.
- [ ] Danh sách tên công khai mới (nếu có) kèm dòng tài liệu định nghĩa đã dán vào PR (R16).

### Quy mô
- [x] Đây là một lát cắt dọc của một module, khoảng nửa ngày đến một ngày, một Pull Request.
