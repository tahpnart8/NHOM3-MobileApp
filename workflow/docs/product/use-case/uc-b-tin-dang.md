# Đặc tả use case: B. Tin đăng và khám phá

Module B gồm 9 use case từ UC09 đến UC17: quản lý tin nháp, tạo tin đăng, quản lý kệ hàng, xem kênh tin, quản lý tin đã lưu, tìm kiếm tin đăng, tìm kiếm người dùng, xem chi tiết tin đăng và đăng tin. Màn hình thuộc module: B01 đến B06 (xem `workflow/docs/ui/b-kham-pha-tin-dang/README.md`). Chỉ UC12, UC14 và UC16 mở cho Khách; mọi use case còn lại cần tài khoản đã đăng nhập. Chỉ UC17 Đăng tin cần thêm tài khoản đã được admin duyệt xác thực; UC09, UC10 và UC11 mở cho mọi tài khoản đã đăng nhập, vì tài khoản chưa được duyệt vẫn soạn và lưu nháp được, chỉ không bấm Đăng được (quyết định leader 2026-10-22).

## UC09 Quản lý tin nháp

| Mục | Nội dung |
| --- | --- |
| Mã use case | UC09 |
| Tên use case | Quản lý tin nháp |
| Tác nhân | Người dùng |
| Mô tả | Cho phép người bán theo dõi danh sách các bài đăng đang lưu tạm dưới dạng bản nháp, mở lại bản nháp để tiếp tục chỉnh sửa/bổ sung thông tin còn thiếu, công khai đăng tin lên sàn giao dịch (UC17 Đăng tin) hoặc xóa bỏ vĩnh viễn tin nháp không còn nhu cầu. |
| Mức ưu tiên | Trung bình |
| Khởi phát | Người bán nhấn vào tab "Nháp" tại màn hình Kệ hàng của tôi (B05). |
| Tiền điều kiện | 1. Người dùng đã đăng nhập tài khoản vào ứng dụng.<br>2. Người dùng đã từng lưu ít nhất một bài đăng ở trạng thái bản nháp.<br>3. Thiết bị có kết nối mạng Internet (hoặc truy xuất từ bộ nhớ đệm SQLite Room). |
| Hậu điều kiện | 1. Nếu đăng tin: Tin chuyển trạng thái từ DRAFT sang AVAILABLE (đang bán), được công khai trên Trang chủ và chuyển từ tab "Nháp" sang tab "Đang hoạt động".<br>2. Nếu lưu lại nháp: Dữ liệu mới được cập nhật và tiếp tục lưu ở trạng thái DRAFT.<br>3. Nếu xóa: Bản ghi tin nháp bị xóa vĩnh viễn khỏi hệ thống cơ sở dữ liệu. |

**Luồng cơ bản**

| Bước | Hành động của tác nhân | Phản hồi của hệ thống |
| --- | --- | --- |
| 1 | Người bán nhấn chọn tab "Nháp" trên thanh phân loại của màn hình Kệ hàng của tôi (Trigger). | Hệ thống truy vấn và hiển thị danh sách các bài đăng đang có trạng thái DRAFT, kèm số lượng tin nháp hiện có. |
| 2 | Người bán nhấn vào một thẻ tin nháp muốn đăng (hoặc nhấn nút ba chấm ... và chọn "Chỉnh sửa"). | Hệ thống mở màn hình biểu mẫu Đăng tin (B04) với các thông tin đã lưu trước đó của bản nháp được điền sẵn vào các ô tương ứng. |
| 3 | Người bán rà soát, điền bổ sung các thông tin còn thiếu (ví dụ: thêm ảnh chụp, bổ sung mô tả, chọn danh mục, điều chỉnh giá bán) và nhấn nút "Đăng tin". | Hệ thống kiểm tra điều kiện xuất bản tin theo UC17 Đăng tin: bắt buộc đủ tiêu đề, danh mục, tối thiểu 1 ảnh thực tế và tối thiểu một hình thức giao dịch. |
| 4 |  | Hệ thống thực hiện phương thức xuất bản (Listing.publish()): chuyển trạng thái tin từ DRAFT sang AVAILABLE, tự động trích xuất các từ khóa tìm kiếm (searchKeywords) và lưu lên cơ sở dữ liệu Firestore. |
| 5 |  | Hiển thị thông báo "Đăng tin thành công!", tự động gỡ tin khỏi tab "Nháp", đưa tin sang tab "Đang hoạt động" và công khai trên Bảng tin toàn hệ thống. |

**Luồng thay thế**

| Bước | Hành động của tác nhân | Phản hồi của hệ thống |
| --- | --- | --- |
| 1a | Xóa tin nháp |  |
| 1a1 | Tại thẻ tin nháp ở màn hình danh sách, người bán nhấn nút ba chấm (...) và chọn "Xóa tin". | Hệ thống hiển thị hộp thoại xác nhận: "Bạn có chắc chắn muốn xóa bản nháp này không?". |
| 1a2 | Người bán nhấn "Xóa". | Hệ thống xóa vĩnh viễn tài liệu tin nháp khỏi cơ sở dữ liệu Firestore và bộ nhớ đệm Room. |
| 1a3 |  | Cập nhật lại giao diện tab "Nháp" (gỡ bỏ thẻ tin vừa xóa) và hiển thị thông báo "Đã xóa bản nháp". |
| 3a | Tiếp tục lưu nháp sau khi chỉnh sửa |  |
| 3a1 | Người bán chỉnh sửa/bổ sung một số thông tin nhưng chưa muốn đăng công khai, nhấn nút "Lưu nháp" (hoặc nhấn nút Quay lại và chọn "Lưu thay đổi"). | Hệ thống không bắt buộc phải có đầy đủ mọi trường thông tin. |
| 3a2 |  | Cập nhật các thông tin mới vào bản ghi tin nháp hiện tại, giữ nguyên trạng thái DRAFT trong cơ sở dữ liệu. |
| 3a3 |  | Hiển thị thông báo "Đã lưu bản nháp" và quay trở lại màn hình danh sách tab "Nháp". |

**Luồng ngoại lệ**

| Bước | Hành động của tác nhân | Phản hồi của hệ thống |
| --- | --- | --- |
| 1e | Tab nháp trống (Không có bản nháp nào)<br>Tại bước 1, nếu người bán chưa từng lưu nháp bài đăng nào | Hệ thống hiển thị hình minh họa trống kèm thông điệp: "Bạn chưa có bản nháp nào. Tin đăng lưu dở sẽ xuất hiện tại đây". |
| 3e | Thiếu thông tin bắt buộc khi thực hiện Đăng tin chính thức<br>Tại bước 3, nếu người bán nhấn "Đăng tin" nhưng bản nháp vẫn còn thiếu thông tin cốt lõi (ví dụ: chưa có ảnh, chưa nhập tiêu đề hoặc chưa chọn danh mục) | Hệ thống chặn lệnh đăng tin, viền đỏ các ô còn thiếu và hiển thị cảnh báo: "Tin đăng cần có tối thiểu tiêu đề, danh mục và 1 hình ảnh để có thể công khai". Người bán phải bổ sung đủ mới được đăng. |
| 4e | Mất kết nối mạng Internet<br>Tại bước 4 (hoặc khi lưu, xóa ở bước 3a2 và 1a2), nếu xảy ra mất kết nối mạng | Hệ thống thông báo lỗi đường truyền: "Không thể đồng bộ dữ liệu với máy chủ, vui lòng kiểm tra lại kết nối mạng". |

**Quy tắc nghiệp vụ**

- BR1 (Tính riêng tư của tin nháp): Tin ở trạng thái ListingStatus.DRAFT hoàn toàn mang tính riêng tư: chỉ duy nhất chủ tài khoản nhìn thấy trong tab "Nháp", không xuất hiện trên Bảng tin, không tìm kiếm được và người khác không thể xem hay gửi offer.
- BR2 (Điều kiện công khai tin): Phương thức publish() chỉ kích hoạt thành công khi tin nháp đã được bổ sung đầy đủ: tiêu đề hợp lệ, danh mục, ít nhất 1 ảnh chụp và tối thiểu một hình thức giao dịch (Bán/Trao đổi/Cho tặng).
- BR3 (Quy định xóa bản nháp): Vì tin nháp chưa bao giờ tham gia vào chu trình giao dịch, người bán có toàn quyền xóa bỏ vĩnh viễn khỏi hệ thống bất cứ lúc nào mà không gặp bất kỳ ràng buộc nào về lịch sử đơn hàng.

**Yêu cầu phi chức năng**

- Hiệu năng & Tải ngoại tuyến: Danh sách tin nháp phải được đồng bộ và lưu trữ tại bộ nhớ đệm cục bộ (SQLite Room) để người bán có thể mở ra xem hoặc soạn thảo tiếp ngay cả khi thiết bị mất mạng.
- Tính toàn vẹn dữ liệu: Khi chuyển từ màn hình Kệ hàng sang màn hình Đăng tin và ngược lại, mọi dữ liệu chữ và ảnh của bản nháp phải được giữ nguyên vẹn 100%.

## UC10 Tạo tin đăng

| Mục | Nội dung |
| --- | --- |
| Mã use case | UC10 |
| Tên use case | Tạo tin đăng |
| Tác nhân | Người dùng |
| Mô tả | Cho phép người bán soạn thảo bài đăng về món đồ cũ cần bán, trao đổi hoặc cho tặng; bao gồm chọn ảnh sản phẩm, điền thông tin chi tiết (tên, danh mục, mô tả, tình trạng, giá, hình thức giao dịch, khu vực); sau đó lựa chọn công khai tin lên bảng tin (UC17 Đăng tin mở rộng use case này) hoặc lưu lại dưới dạng bản nháp để hoàn thiện sau. |
| Mức ưu tiên | Cao |
| Khởi phát | Người bán nhấn nút "Đăng tin" |
| Tiền điều kiện | 1. Người bán đã đăng nhập tài khoản vào ứng dụng.<br>2. Tài khoản đang ở trạng thái hoạt động bình thường (ACTIVE), không bị khóa quyền đăng tin.<br>3. Thiết bị có kết nối mạng Internet |
| Hậu điều kiện | 1. Tin đăng mới được khởi tạo và lưu trữ trên cơ sở dữ liệu hệ thống (Firestore listings).<br>2. Nếu chọn Đăng tin: trạng thái là AVAILABLE (đang bán, hiển thị công khai trên Bảng tin trang chủ và Kệ hàng cá nhân).<br>3. Nếu chọn Lưu nháp: trạng thái là DRAFT (chỉ hiển thị riêng tư tại mục "Nháp" trong Kệ hàng của tôi). |

**Luồng cơ bản**

| Bước | Hành động của tác nhân | Phản hồi của hệ thống |
| --- | --- | --- |
| 1 | Người bán nhấn nút "Đăng tin" trên ứng dụng | Hiển thị màn hình biểu mẫu "Đăng sản phẩm" với các trường trống: Tiêu đề, Danh mục, Ảnh, Mô tả, Tình trạng món đồ, Hình thức giao dịch, Giá bán, Cấu hình nhận Offer, Khu vực; kèm nút "Lưu nháp" và nút "Đăng tin". |
| 2 | Người bán nhấn biểu tượng máy ảnh, chọn ảnh chụp sản phẩm từ thư viện hoặc chụp ảnh mới. | Đưa ảnh vào bộ nhớ tạm trên thiết bị, hiển thị lưới ảnh thu nhỏ kèm biểu tượng xóa ảnh trên từng ảnh. |
| 3 | Người bán nhập Tiêu đề tin đăng và chọn Danh mục sản phẩm tương ứng. | Tiếp nhận tiêu đề và cập nhật danh mục đã chọn. |
| 4 | Người bán nhập nội dung Mô tả chi tiết và chọn 1 trong 5 mức Tình trạng món đồ (Mới, Như mới, Đã sử dụng, Đã sửa chữa, Có lỗi). | Ghi nhận mô tả và đánh dấu thẻ tình trạng được chọn. |
| 5 | Người bán chọn Hình thức giao dịch là "Bán" và nhập Giá bán mong muốn. | Hiển thị số tiền đã định dạng (VD: 500.000 đ) và bật tùy chọn cho phép thương lượng/offer. |
| 6 | Người bán thiết lập mục "Trả giá 1 chạm (Offer)": chọn loại offer chấp nhận (tiền, vật trao đổi) và nhập mức giá sàn tối thiểu chấp nhận đàm phán (VD: 400.000 đ). | Ghi nhận cấu hình offer và mức giá sàn để tự động chặn các mức trả giá quá thấp từ người mua. |
| 7 | Người bán kiểm tra thông tin Khu vực (mặc định lấy theo địa chỉ hồ sơ cá nhân hoặc chọn khu vực khác) và nhấn nút "Đăng tin". | Kiểm tra tính hợp lệ và đầy đủ của các trường bắt buộc (tiêu đề, danh mục, ít nhất 1 ảnh, tình trạng, ít nhất một hình thức giao dịch, giá bán > 0 khi có hình thức Bán). Phần xuất bản tin do UC17 Đăng tin mô tả. |
| 8 |  | Hệ thống nén ảnh ngay trên thiết bị (ảnh đầy đủ cạnh dài tối đa 1024 px, không quá 300 KB trước khi mã hóa; ảnh thu nhỏ 320 px, không quá 20 KB), mã hóa Base64 và lưu cùng bài đăng trong tài liệu Firestore với trạng thái AVAILABLE, đồng thời trích xuất từ khóa tìm kiếm (searchKeywords). |
| 9 |  | Hiển thị thông báo "Đăng tin thành công!" và điều hướng người bán sang màn hình Chi tiết tin đăng (B03) của tin vừa tạo. |

**Luồng thay thế**

| Bước | Hành động của tác nhân | Phản hồi của hệ thống |
| --- | --- | --- |
| 5a | Hình thức giao dịch Trao đổi |  |
| 5a1 | Người bán tích chọn hình thức "Trao đổi". | Hệ thống hiển thị thêm ô nhập: "Món đồ mong muốn đổi lại" và cho phép cấu hình bù/trừ tiền nếu có. |
| 5a2 | Người bán nhập tên món đồ muốn đổi (VD: "Muốn đổi lấy máy tính bảng hoặc bù thêm tiền"). Tiếp tục thực hiện từ Bước 6 của Basic Path. | Hệ thống lưu loại giao dịch vào thuộc tính tin đăng. |
| 5b | Hình thức giao dịch cho tặng |  |
| 5b1 | Người bán tích chọn hình thức "Cho tặng" (Miễn phí 0đ). | Hệ thống tự động gán giá trị Giá bán = 0, làm mờ/ẩn ô nhập giá tiền và khóa tính năng trả giá. |
| 5b2 | Người bán kiểm tra thông tin và tiếp tục thực hiện từ Bước 6 của Basic Path. | Hệ thống lưu loại giao dịch vào thuộc tính tin đăng. |
| 7a | Lưu tin nháp (lưu tạm để sửa và đăng sau) |  |
| 7a1 | Người bán nhấn nút "Lưu nháp" (hoặc nhấn nút Quay lại và chọn "Lưu vào bản nháp" khi có hộp thoại xác nhận). | Hệ thống bỏ qua việc kiểm tra đầy đủ các trường bắt buộc (chỉ cần có tiêu đề hoặc ít nhất 1 ảnh hoặc mô tả). |
| 7a2 |  | Lưu thông tin bài đăng vào hệ thống (chưa công khai trên trang chủ hay tìm kiếm). |
| 7a3 |  | Hiển thị thông báo "Đã lưu vào bản nháp", chuyển người bán về tab "Nháp" trong màn hình Kệ hàng của tôi (B05). |

**Luồng ngoại lệ**

| Bước | Hành động của tác nhân | Phản hồi của hệ thống |
| --- | --- | --- |
| 2e | Tải lên quá số lượng ảnh cho phép<br>Tại Bước 2, nếu người bán chọn nhiều hơn 5 ảnh từ thiết bị | Hệ thống thông báo lỗi: "Chỉ được tải lên tối đa 5 hình ảnh cho mỗi tin đăng" và tự động ẩn nút thêm ảnh khi đã đủ 5 ảnh. |
| 5e | Giá bán không hợp lệ<br>Tại Bước 5, người bán nhập giá bán là số âm hoặc bằng 0 (đối với hình thức Bán) | Hệ thống hiển thị thông báo lỗi dưới ô giá: "Giá bán phải lớn hơn 0 đối với hình thức Bán" |
| 7e | Thiếu các trường thông tin bắt buộc khi Đăng tin<br>Tại Bước 7, nếu người bán nhấn "Đăng tin" nhưng chưa chọn ảnh, chưa nhập tiêu đề hoặc chưa chọn danh mục/tình trạng | Hệ thống cuộn màn hình đến vị trí bị thiếu, viền đỏ ô dữ liệu và hiển thị cảnh báo: "Vui lòng cung cấp đầy đủ thông tin bắt buộc và tối thiểu 1 hình ảnh". |
| 8e | Mất kết nối mạng trong quá trình lưu tin<br>Tại Bước 8, nếu bị ngắt mạng khi đang lưu tin lên cơ sở dữ liệu | Hệ thống thông báo: "Lưu tin thất bại do lỗi kết nối mạng. Nội dung bạn đã nhập vẫn được giữ nguyên", giữ nguyên dữ liệu và ảnh đã chọn trên form để người dùng bấm thử lại. |

**Quy tắc nghiệp vụ**

- 1. Mỗi tin đăng công khai bắt buộc có từ 1 đến 5 ảnh chụp thực tế. Mỗi ảnh được nén trên thiết bị trước khi lưu: ảnh đầy đủ có cạnh dài tối đa 1024 px và không quá 300 KB trước khi mã hóa Base64; ảnh thu nhỏ dùng cho danh sách có cạnh dài 320 px và không quá 20 KB.
- 2. Bắt buộc chọn 1 trong 5 giá trị chuẩn hóa (ItemCondition): MỚI (NEW), NHƯ MỚI (LIKE_NEW), ĐÃ SỬ DỤNG (USED), ĐÃ SỬA CHỮA (REPAIRED), CÓ LỖI (DEFECTIVE).
- 3. Tin lưu nháp (DRAFT) không bắt buộc phải thỏa mãn các điều kiện để công khai, chỉ cần có tiêu đề hoặc ít nhất 1 ảnh hoặc mô tả; người bán có thể mở lại từ Kệ hàng để chỉnh sửa và đăng bất cứ lúc nào.
- 4. Cho phép chọn đơn lẻ hoặc kết hợp cả Bán và Trao đổi. Riêng hình thức Cho tặng thì giá luôn cố định bằng 0.

**Yêu cầu phi chức năng**

- 1. Nén ảnh tự động phía máy khách, mã hóa Base64 và lưu trong tài liệu Firestore, không dùng Cloud Storage; kích thước ảnh nằm trong giới hạn của quy tắc 1 để tối ưu tốc độ tải tin (thời gian đăng tin hoàn tất dưới 5 giây ở điều kiện mạng bình thường).
- 2. Khi xoay ngang/dọc màn hình điện thoại, toàn bộ dữ liệu đang nhập dở trên form và danh sách ảnh đã chọn không bị reset/mất dữ liệu.

## UC11 Quản lý kệ hàng

| Mục | Nội dung |
| --- | --- |
| Mã use case | UC11 |
| Tên use case | Quản lý kệ hàng |
| Tác nhân | Người dùng |
| Mô tả | Cho phép người bán theo dõi và quản lý toàn bộ các bài đăng của bản thân trên kệ hàng: sắp xếp và lọc sản phẩm theo thời gian/danh mục/trạng thái (đang hoạt động, đã bán, đã ẩn, nháp); chỉnh sửa thông tin bài đăng; thay đổi trạng thái sản phẩm (đánh dấu đã bán, mở bán lại, ẩn/hiện lại tin) và xóa tin nháp khỏi kệ hàng. |
| Mức ưu tiên | Cao |
| Khởi phát | Người bán nhấn vào mục "Sản phẩm của tôi" tại màn hình Hồ sơ cá nhân. |
| Tiền điều kiện | 1. Người dùng đã đăng nhập tài khoản vào ứng dụng.<br>2. Thiết bị có kết nối mạng Internet (hoặc sử dụng dữ liệu đã lưu trong bộ nhớ đệm Room nếu ngoại tuyến). |
| Hậu điều kiện | 1. Thông tin và trạng thái sản phẩm được cập nhật chính xác trên hệ thống cơ sở dữ liệu (Firestore listings và SQLite Room).<br>2. Danh sách sản phẩm trên kệ hàng phản ánh đúng các thay đổi (chỉnh sửa, lọc, ẩn hoặc xóa). |

**Luồng cơ bản**

| Bước | Hành động của tác nhân | Phản hồi của hệ thống |
| --- | --- | --- |
| 1 | Người bán nhấn chọn "Sản phẩm của tôi" tại màn hình Hồ sơ cá nhân (Trigger). | Hiển thị màn hình "Kệ hàng của tôi" (màn hình B05) với tab mặc định là "Đang hoạt động", hiển thị danh sách các bài đăng được sắp xếp theo thời gian từ mới nhất đến cũ nhất. |
| 2 | Người bán tìm đến sản phẩm cần cập nhật, nhấn vào biểu tượng dấu ba chấm (...) trên thẻ sản phẩm và chọn "Chỉnh sửa". | Hiển thị biểu mẫu chỉnh sửa tin đăng với toàn bộ dữ liệu hiện tại của sản phẩm được điền sẵn (tiêu đề, ảnh, mô tả, giá, danh mục, khu vực...). |
| 3 | Người bán thay đổi các thông tin mong muốn (ví dụ: điều chỉnh lại giá bán, bổ sung mô tả hoặc cập nhật ảnh mới) và nhấn nút "Lưu thay đổi". | Kiểm tra tính hợp lệ của dữ liệu vừa chỉnh sửa (giá bán hợp lệ, ảnh và tiêu đề không được để trống). |
| 4 |  | Hệ thống cập nhật các thông tin mới vào cơ sở dữ liệu Firestore và làm mới bộ nhớ đệm SQLite Room. |
| 5 |  | Hiển thị thông báo "Cập nhật sản phẩm thành công!" và đưa người bán quay lại màn hình Kệ hàng của tôi với thông tin đã được làm mới. |

**Luồng thay thế**

| Bước | Hành động của tác nhân | Phản hồi của hệ thống |
| --- | --- | --- |
| 1a | Sắp xếp và Lọc sản phẩm trên kệ hàng |  |
| 1a1 | Người bán nhấn vào thanh phân loại trạng thái để chuyển sang tab "Đã bán", "Đã ẩn" hoặc "Nháp"; hoặc chọn bộ lọc Danh mục / Thời gian (mới nhất, cũ nhất). | Hệ thống truy vấn dữ liệu từ bộ nhớ đệm hoặc máy chủ tương ứng với tiêu chí lọc/sắp xếp vừa chọn. |
| 1a2 |  | Làm mới danh sách hiển thị trên kệ hàng: chỉ hiển thị các sản phẩm thuộc tab/danh mục đã chọn và sắp xếp đúng theo thứ tự thời gian yêu cầu. |
| 2a | Đánh dấu đã bán hoặc Mở bán lại sản phẩm |  |
| 2a1 | Tại menu ba chấm của một tin ở tab "Đang hoạt động", người bán chọn "Đánh dấu đã bán" (bán ngoài ứng dụng). | Hệ thống kiểm tra sản phẩm hiện không có giao dịch nào đang tiến hành trong app, sau đó chuyển trạng thái tin sang SOLD. |
| 2a2 |  | Hiển thị thông báo "Đã chuyển sản phẩm sang mục Đã bán", đồng thời di chuyển thẻ sản phẩm từ tab "Đang hoạt động" sang tab "Đã bán". (Trường hợp ngược lại: tại tab "Đã bán", nếu người bán chọn "Mở bán lại", hệ thống chuyển trạng thái về AVAILABLE và đưa lại về tab "Đang hoạt động"). |
| 2b | Ẩn hoặc Hiện lại tin đăng |  |
| 2b1 | Tại menu ba chấm của một sản phẩm, người bán chọn "Ẩn tin". | Hệ thống cập nhật trạng thái tin sang HIDDEN (tạm thời không hiển thị trên Bảng tin công khai và Tìm kiếm). |
| 2b2 |  | Gắn nhãn trạng thái màu xám "ĐÃ ẨN" trên thẻ sản phẩm trên kệ hàng của người bán, chuyển thẻ sang tab "Đã ẩn" và hiển thị thông báo "Tin đăng đã được ẩn". (Nếu tin đang ở trạng thái đã ẩn, người bán chọn "Hiện lại tin", hệ thống gỡ nhãn ẩn và công khai lại tin trên toàn sàn). |
| 2c | Xóa tin nháp khỏi kệ hàng |  |
| 2c1 | Tại tab "Nháp", ở menu ba chấm của một tin nháp, người bán chọn "Xóa tin". | Hiển thị hộp thoại xác nhận: "Bạn có chắc chắn muốn xóa bài đăng này không? Thao tác này không thể hoàn tác". |
| 2c2 | Người bán nhấn "Xác nhận xóa". | Kiểm tra điều kiện: tin đang ở trạng thái nháp (chỉ tin nháp được xóa hẳn, xem 2e). |
| 2c3 |  | Hệ thống xóa vĩnh viễn bài đăng khỏi hệ thống, gỡ thẻ sản phẩm khỏi màn hình Kệ hàng và hiển thị thông báo "Đã xóa bài đăng". |

**Luồng ngoại lệ**

| Bước | Hành động của tác nhân | Phản hồi của hệ thống |
| --- | --- | --- |
| 2e | Không thể xóa tin đăng đã đăng công khai<br>Tại Bước 2c2, nếu tin không còn là tin nháp (tin đã đăng, dù đã hay chưa có giao dịch mua/bán) | Hệ thống thông báo lỗi: "Chỉ tin nháp mới xóa được. Tin đã đăng chỉ có thể ẩn đi, hãy chọn Ẩn tin", hủy bỏ thao tác xóa. |
| 2f | Không thể đánh dấu đã bán do đang có giao dịch dở dang<br>Tại Bước 2a1, nếu sản phẩm đang nằm trong một đơn hàng đang xử lý (đang giao, chờ thanh toán) | Hệ thống cảnh báo: "Sản phẩm đang trong quá trình giao dịch với người mua, không thể thay đổi trạng thái thủ công". |
| 3e | Dữ liệu chỉnh sửa không hợp lệ<br>Tại Bước 3, nếu người bán xóa sạch ảnh hoặc nhập giá bán bằng 0 | Hệ thống báo lỗi tương ứng dưới trường dữ liệu và không cho phép lưu thay đổi. |
| 4e | Mất kết nối mạng Internet<br>Tại Bước 4 hoặc bất kỳ thao tác lưu/đổi trạng thái nào (2a, 2b, 2c), nếu không có mạng | Hệ thống thông báo: "Không có kết nối mạng. Dữ liệu chưa thể đồng bộ lên máy chủ, vui lòng thử lại sau". |

**Quy tắc nghiệp vụ**

- BR1 (Phân loại trạng thái kệ hàng):<br>Đang hoạt động: Các tin ở trạng thái AVAILABLE (đang bán) hoặc RESERVED (đang giữ chỗ).<br>Đã bán: Các tin ở trạng thái SOLD (đã giao dịch xong hoặc người bán tự đánh dấu đã bán).<br>Đã ẩn: Các tin ở trạng thái HIDDEN (do người bán tự ẩn hoặc do Admin ẩn) và REMOVED (đã gỡ).<br>Nháp: Các tin ở trạng thái DRAFT (chưa hoàn thiện, chưa công khai).
- BR2 (Quy định xóa tin): Chỉ cho phép xóa hẳn (delete) tin ở trạng thái nháp. Tin đã đăng công khai muốn bỏ khỏi kệ thì dùng "Ẩn tin", để giao dịch, cuộc trò chuyện và tin đã lưu không bao giờ trỏ tới một tin đã biến mất.
- BR3 (Quyền hiện lại tin): Tin do người bán tự ẩn thì người bán được quyền bấm "Hiện lại tin". Nếu tin bị Admin ẩn do vi phạm chính sách thì người bán không có quyền tự hiện lại.

**Yêu cầu phi chức năng**

- Khả năng sử dụng ngoại tuyến (Offline Availability): Khi mất mạng, người bán vẫn có thể mở và xem lại toàn bộ danh sách sản phẩm trên kệ hàng đã xem trước đó nhờ bộ nhớ đệm SQLite Room.
- Hiệu năng (Performance): Thao tác chuyển tab lọc hoặc thay đổi thứ tự sắp xếp phản hồi tức thì (dưới 0.5 giây đối với dữ liệu cục bộ).

## UC12 Xem kênh tin

| Mục | Nội dung |
| --- | --- |
| Mã use case | UC12 |
| Tên use case | Xem kênh tin |
| Tác nhân | Người dùng, Khách |
| Mô tả | Cho phép người dùng và khách duyệt xem bảng tin các bài đăng bán/trao đổi/cho tặng đồ cũ trên ứng dụng thông qua hai kênh riêng biệt: Kênh chung (hiển thị toàn bộ tin đăng mới nhất trên sàn kèm gợi ý phù hợp, trong đó có mục "Dành cho bạn") và Kênh đang theo dõi (chỉ hiển thị bài đăng từ những người bán mà tài khoản đã bấm theo dõi, chỉ dành cho người dùng đã đăng nhập). |
| Mức ưu tiên | Cao |
| Khởi phát | Người dùng/Khách mở ứng dụng (vào Trang chủ) hoặc nhấn vào tab "Trang chủ" trên thanh điều hướng dưới cùng. |
| Tiền điều kiện | 1. Khách không cần đăng nhập để xem Kênh chung; Kênh đang theo dõi cần tài khoản đã đăng nhập.<br>2. Thiết bị có kết nối mạng Internet (hoặc tải dữ liệu đã lưu từ bộ nhớ đệm SQLite Room nếu ngoại tuyến). |
| Hậu điều kiện | Danh sách bài đăng tương ứng với kênh tin được hiển thị trực quan dưới dạng lưới 2 cột kèm ảnh, tiêu đề, mức giá, tình trạng và khu vực để người dùng tiếp tục khám phá. |

**Luồng cơ bản**

| Bước | Hành động của tác nhân | Phản hồi của hệ thống |
| --- | --- | --- |
| 1 | Người dùng mở ứng dụng hoặc nhấn vào biểu tượng "Trang chủ" trên thanh điều hướng (Trigger). | Hiển thị màn hình Trang chủ (B01) với thanh tìm kiếm, chuông thông báo, hàng lọc danh mục nhanh và 2 tab chuyển kênh ("Kênh chung" và "Đang theo dõi"). Mặc định chọn tab "Kênh chung". |
| 2 |  | Hệ thống truy vấn từ Firestore danh sách các bài đăng đang hoạt động (status = AVAILABLE), sắp xếp theo thời gian đăng mới nhất kết hợp thuật toán gợi ý theo sở thích (RecommendationEngine). |
| 3 |  | Hiển thị danh sách tin đăng theo dạng lưới 2 cột: mỗi thẻ gồm ảnh đại diện, tiêu đề, giá tiền, nhãn tình trạng món đồ và khu vực đăng tin. |
| 4 | Người dùng cuộn dọc màn hình xuống dưới để tiếp tục khám phá thêm tin đăng. | Hệ thống tự động kích hoạt cơ chế tải phân trang (pagination/infinite scroll), tải thêm bài đăng cũ hơn và nối tiếp vào cuối danh sách. |
| 5 | Người dùng nhấn vào một thẻ bài đăng bất kỳ trên lưới. | Hệ thống điều hướng người dùng sang màn hình Chi tiết tin đăng của món đồ đó. |

**Luồng thay thế**

| Bước | Hành động của tác nhân | Phản hồi của hệ thống |
| --- | --- | --- |
| 2a | Xem mục "Dành cho bạn" (gợi ý trên Kênh chung) |  |
| 2a1 |  | Hệ thống đọc lịch sử xem và lịch sử tìm kiếm chỉ lưu trên thiết bị, cùng danh mục quan tâm mà người dùng đã chọn khi đăng ký và danh mục của các tin người dùng đã lưu (nếu đã đăng nhập), rồi chọn ra các tin đang bán phù hợp. |
| 2a2 |  | Hiển thị mục "Dành cho bạn" là một dải ngang các tin gợi ý phía trên hàng lọc danh mục nhanh, bên trên lưới tin đăng của Bước 3. |
| 2a3 | Người dùng nhấn vào một thẻ tin trong mục "Dành cho bạn". | Hệ thống điều hướng người dùng sang màn hình Chi tiết tin đăng của món đồ đó (UC16). |
| 3a | Chuyển sang xem "Kênh đang theo dõi" |  |
| 3a1 | Người dùng nhấn chuyển sang tab "Đang theo dõi". | Hệ thống kiểm tra danh sách followingIds (những người bán mà tài khoản này đang theo dõi). |
| 3a2 |  | Hệ thống truy vấn các bài đăng có sellerId nằm trong danh sách đang theo dõi và có trạng thái AVAILABLE. |
| 3a3 |  | Cập nhật lại lưới tin đăng: chỉ hiển thị các bài viết được đăng bởi những người bán mà người dùng đã bấm follow, sắp xếp theo thời gian đăng từ mới đến cũ. Tiếp tục từ Bước 4 của Basic Path. |
| 3b | Làm mới danh sách tin đăng |  |
| 3b1 | Người dùng vuốt màn hình từ cạnh trên xuống dưới (thao tác kéo để làm mới). | Hiển thị biểu tượng xoay tải dữ liệu (loading spinner). |
| 3b2 |  | Hệ thống gửi yêu cầu lấy các bài đăng mới nhất vừa được đăng tải, đồng bộ lại dữ liệu vào bộ nhớ đệm Room. |
| 3b3 |  | Cập nhật lại danh sách bài đăng ở đầu trang và ẩn biểu tượng tải. |

**Luồng ngoại lệ**

| Bước | Hành động của tác nhân | Phản hồi của hệ thống |
| --- | --- | --- |
| 1e | Mất kết nối mạng Internet (Ngoại tuyến)<br>Tại Bước 1 hoặc bước 3a1 khi chuyển kênh, nếu thiết bị mất mạng: | Hệ thống tự động truy xuất các bài đăng đã xem gần đây được lưu trong bộ nhớ đệm SQLite Room, hiển thị dải thông báo màu xám ở đầu trang: "Đang xem ngoại tuyến. Một số tin đăng có thể chưa được cập nhật mới nhất". |
| 2e | Chưa có dữ liệu để gợi ý<br>Tại Bước 2a1, nếu thiết bị chưa có lịch sử xem, lịch sử tìm kiếm, người dùng chưa chọn danh mục quan tâm và chưa lưu tin nào: | Hệ thống ẩn mục "Dành cho bạn"; Kênh chung vẫn hiển thị lưới tin đăng bình thường. |
| 3e | Khách (chưa đăng nhập) bấm xem Kênh đang theo dõi<br>Tại Bước 3a1, nếu người sử dụng là Khách vãng lai: | Hệ thống hiển thị hộp thoại thông báo: "Vui lòng đăng nhập để xem bài đăng từ những người bạn theo dõi", kèm hai nút "Hủy" và "Đăng nhập" (nút "Đăng nhập" đưa Khách tới màn hình đăng nhập). |
| 3f | Chưa theo dõi người bán nào hoặc người bán chưa có tin mới (Kênh theo dõi trống)<br>Tại Bước 3a3, nếu người dùng chưa bấm theo dõi ai, hoặc những người theo dõi chưa đăng bài nào: | Hệ thống hiển thị hình minh họa trống kèm thông báo: "Chưa có bài đăng nào từ người bạn theo dõi. Hãy khám phá và theo dõi các người bán uy tín nhé!", kèm nút gợi ý quay lại "Kênh chung". |

**Quy tắc nghiệp vụ**

- BR1 (Phạm vi hiển thị tin): Cả hai kênh tin chỉ được phép hiển thị các bài đăng có trạng thái ListingStatus.AVAILABLE. Các tin ở trạng thái Nháp (DRAFT), Đã ẩn (HIDDEN), Đã bán (SOLD) hoặc Đã gỡ (REMOVED) tuyệt đối không xuất hiện trên kênh xem.
- BR2 (Quyền riêng tư Kênh đang theo dõi): Kênh đang theo dõi chỉ áp dụng cho tài khoản Người dùng đã đăng nhập.
- BR3 (Gợi ý Bảng tin): Kênh chung ưu tiên kết hợp bài đăng mới đăng gần nhất và các bài đăng thuộc danh mục người dùng đã chọn quan tâm khi tạo tài khoản.
- BR4 (Mục "Dành cho bạn"): Lịch sử xem và lịch sử tìm kiếm dùng để gợi ý chỉ được lưu trên thiết bị, không gửi lên máy chủ. Danh mục quan tâm và tin đã lưu (gợi ý theo danh mục của tin đã lưu) chỉ có với người dùng đã đăng nhập. Khách vẫn thấy mục này nếu thiết bị đã có lịch sử.

**Yêu cầu phi chức năng**

- Khả năng sử dụng ngoại tuyến (Offline-first): Hỗ trợ lưu trữ cục bộ các bài đăng gần nhất bằng SQLite Room theo đúng yêu cầu đồ án, đảm bảo người dùng vẫn lướt xem được nội dung cũ khi mất mạng.
- Hiệu năng cuộn (Smooth Scrolling): Tải ảnh dạng lười (lazy loading) và phân trang mượt mà, không gây hiện tượng giật/lag khi người dùng lướt bảng tin liên tục.
- Thích ứng màn hình: Giữ nguyên vị trí cuộn trang và bố cục lưới 2 cột thích ứng khi xoay ngang/dọc thiết bị.

## UC13 Quản lý tin đã lưu

| Mục | Nội dung |
| --- | --- |
| Mã use case | UC13 |
| Tên use case | Quản lý tin đã lưu |
| Tác nhân | Người dùng |
| Mô tả | Cho phép người dùng đã đăng nhập theo dõi và quản lý danh sách các bài đăng yêu thích đã lưu trước đó để tiện cân nhắc, so sánh giá; mở xem lại chi tiết bài đăng hoặc gỡ bỏ tin khỏi danh sách yêu thích khi không còn nhu cầu. |
| Mức ưu tiên | Trung bình |
| Khởi phát | Người dùng nhấn vào mục "Tin đã lưu" tại màn hình Hồ sơ cá nhân. |
| Tiền điều kiện | 1. Người dùng đã đăng nhập tài khoản vào ứng dụng (Khách không dùng được chức năng này).<br>2. Thiết bị có kết nối mạng Internet (hoặc tải danh sách từ bộ nhớ đệm SQLite Room nếu ngoại tuyến). |
| Hậu điều kiện | Danh sách bài đăng yêu thích được cập nhật chính xác trên hệ thống cơ sở dữ liệu (Firestore savedListings và Room cache) và hiển thị tương ứng trên màn hình người dùng. |

**Luồng cơ bản**

| Bước | Hành động của tác nhân | Phản hồi của hệ thống |
| --- | --- | --- |
| 1 | Người dùng nhấn vào mục "Tin đã lưu" tại màn hình Hồ sơ cá nhân (Trigger). | Hiển thị màn hình "Tin đã lưu" (B06) với nút quay lại, tiêu đề và dòng thông báo số lượng tin đã lưu (VD: "Đã lưu 4 tin · Chạm vào biểu tượng trái tim trên tin đăng để bỏ lưu"). |
| 2 |  | Hệ thống truy vấn danh sách các bài đăng mà người dùng đã lưu, tải đầy đủ thông tin: ảnh đại diện, tiêu đề, mức giá, tình trạng món đồ và khu vực. |
| 3 |  | Hiển thị các bài đăng dưới dạng lưới 2 cột, trên mỗi thẻ có biểu tượng trái tim màu đỏ (đang lưu). |
| 4 | Người dùng nhấn vào một thẻ bài đăng bất kỳ trên danh sách. | Hệ thống điều hướng người dùng sang màn hình B03 Chi tiết tin đăng để xem nội dung đầy đủ hoặc nhắn tin/trả giá với người bán. |

**Luồng thay thế**

| Bước | Hành động của tác nhân | Phản hồi của hệ thống |
| --- | --- | --- |
| 3a | Bỏ lưu một bài đăng khỏi danh sách yêu thích |  |
| 3a1 | Người dùng chạm vào biểu tượng trái tim màu đỏ trên thẻ bài đăng (hoặc nút "Bỏ lưu" trong màn hình chi tiết). | Biểu tượng trái tim đổi từ màu đỏ sang màu viền xám rỗng. |
| 3a2 |  | Hệ thống gửi yêu cầu gỡ bỏ mã bài đăng khỏi danh sách yêu thích của người dùng trên Firestore (savedListings) và xóa khỏi bộ nhớ đệm Room. |
| 3a3 |  | Hiển thị thông báo nhanh (Toast/Snackbar): "Đã bỏ lưu tin đăng", tự động gỡ thẻ bài đăng đó khỏi danh sách B06 và cập nhật lại bộ đếm số lượng tin đã lưu. |
| 4a | Lưu một tin từ màn hình Chi tiết tin đăng |  |
| 4a1 | Tại màn hình Chi tiết tin đăng (B03), người dùng nhấn vào biểu tượng trái tim viền xám (chưa lưu). | Biểu tượng trái tim lập tức chuyển sang màu đỏ nổi bật. |
| 4a2 |  | Hệ thống ghi nhận và lưu bài đăng này vào danh sách savedListings của tài khoản trên Firestore và SQLite Room. |
| 4a3 |  | Hiển thị thông báo "Đã lưu tin vào danh sách yêu thích". Tin này sẽ xuất hiện trên màn hình B06 ở lần truy cập tiếp theo. |

**Luồng ngoại lệ**

| Bước | Hành động của tác nhân | Phản hồi của hệ thống |
| --- | --- | --- |
| 1e | Mất kết nối mạng Internet<br>Tại Bước 1, nếu mất mạng: | Hệ thống tự động tải lại danh sách các tin đã lưu trước đó từ cơ sở dữ liệu cục bộ SQLite Room và hiển thị dải thông báo "Đang xem ngoại tuyến". Các thao tác bỏ lưu sẽ được tạm ghi nhận trên máy và đồng bộ lên máy chủ ngay khi có mạng trở lại. |
| 2e | Chưa lưu bài đăng nào (Danh sách trống)<br>Tại Bước 2, nếu người dùng chưa từng lưu bài đăng nào hoặc đã bỏ lưu hết: | Hệ thống hiển thị hình minh họa trống kèm thông điệp: "Bạn chưa lưu tin nào. Chạm vào biểu tượng trái tim ở các bài đăng để lưu lại xem sau nhé!", kèm nút dẫn về Trang chủ để khám phá. |
| 2f | Tin đã lưu bị ẩn, gỡ hoặc đã bán xong<br>Tại Bước 2, nếu một bài đăng trong danh sách đã chuyển sang trạng thái SOLD (đã bán) hoặc bị gỡ/ẩn: | Hệ thống vẫn giữ thẻ bài đăng trong danh sách nhưng làm mờ hình ảnh; tin đã bán gắn nhãn xám "ĐÃ BÁN", tin bị ẩn hoặc gỡ hiện "Tin không còn". Khi người dùng nhấn vào tin không còn, hệ thống thông báo: "Món đồ này hiện không còn khả dụng" và gợi ý bỏ lưu tin. |
| 4e | Khách (chưa đăng nhập) bấm biểu tượng lưu tin<br>Tại Bước 4a1, nếu người xem chưa đăng nhập: | Hệ thống đưa Khách tới màn hình đăng nhập và không ghi nhận việc lưu tin. |

**Quy tắc nghiệp vụ**

- BR1 (Quyền sử dụng): Tính năng Lưu tin và Quản lý tin đã lưu bắt buộc phải có tài khoản Người dùng đã đăng nhập. Khách vãng lai khi bấm vào biểu tượng lưu tin sẽ nhận được yêu cầu đăng nhập.
- BR2 (Độc lập trạng thái): Việc lưu tin không giữ chỗ hay cam kết mua hàng; bài đăng vẫn hiển thị bình thường cho người khác mua cho đến khi người bán chốt giao dịch.
- BR3 (Tính cá nhân): Danh sách tin đã lưu là dữ liệu riêng tư của từng tài khoản, không hiển thị công khai trên hồ sơ cho người khác thấy.

**Yêu cầu phi chức năng**

- Khả năng sử dụng ngoại tuyến (Offline Availability): Danh sách tin đã lưu được lưu trữ trên SQLite Room để người dùng vẫn mở ra xem lại danh sách và chi tiết các món đồ yêu thích ngay cả khi không có mạng.
- Đồng bộ thời gian thực (Real-time sync): Thao tác bấm lưu / bỏ lưu phản hồi trên giao diện ngay tức thì (dưới 0.2 giây) để tạo cảm giác mượt mà cho người dùng.

## UC14 Tìm kiếm tin đăng

| Mục | Nội dung |
| --- | --- |
| Mã use case | UC14 |
| Tên use case | Tìm kiếm tin đăng |
| Tác nhân | Người dùng, Khách |
| Mô tả | Cho phép người dùng và khách tìm kiếm các món đồ cũ trên ứng dụng bằng từ khóa (tên sản phẩm, mô tả) kết hợp với các bộ lọc nâng cao (danh mục, khoảng giá, tình trạng món đồ, hình thức giao dịch, khu vực và bán kính) và sắp xếp kết quả theo bốn cách (mới đăng, giá thấp đến cao, giá cao đến thấp, người bán đánh giá tốt) để nhanh chóng tìm thấy sản phẩm mong muốn. |
| Mức ưu tiên | Cao |
| Khởi phát | Người dùng/Khách nhấn vào thanh tìm kiếm tại màn hình Trang chủ (B01) hoặc chuyển sang màn hình Tìm kiếm và bộ lọc (B02). |
| Tiền điều kiện | 1. Khách không cần đăng nhập để tìm kiếm tin đăng.<br>2. Thiết bị có kết nối Internet (hoặc tìm kiếm trong dữ liệu đệm SQLite Room nếu ngoại tuyến). |
| Hậu điều kiện | Danh sách bài đăng thỏa mãn từ khóa và các điều kiện lọc được hiển thị trực quan dạng lưới 2 cột. |

**Luồng cơ bản**

| Bước | Hành động của tác nhân | Phản hồi của hệ thống |
| --- | --- | --- |
| 1 | Người dùng nhấn vào ô tìm kiếm trên màn hình Trang chủ (B01) (Trigger). | Chuyển sang màn hình Tìm kiếm và bộ lọc (B02) với bàn phím ảo tự động mở, hiển thị các từ khóa đã tìm kiếm gần đây (nếu có). |
| 2 | Người dùng nhập từ khóa tìm kiếm (VD: "iPhone 13") và nhấn phím "Tìm kiếm" trên bàn phím. | Lưu từ khóa vào lịch sử tìm kiếm cục bộ trên máy. |
| 3 |  | Hệ thống truy vấn các bài đăng có searchKeywords chứa từ khóa đã nhập và có trạng thái AVAILABLE. |
| 4 |  | Hiển thị danh sách kết quả tìm kiếm dạng lưới 2 cột kèm số lượng kết quả tìm thấy. |
| 5 | Người dùng nhấn vào một bài đăng trong kết quả. | Điều hướng người dùng sang màn hình Chi tiết tin đăng (B03). |

**Luồng thay thế**

| Bước | Hành động của tác nhân | Phản hồi của hệ thống |
| --- | --- | --- |
| 1a | Chọn từ khóa lịch sử gần đây |  |
| 1a1 | Người dùng nhấn vào một thẻ từ khóa trong danh sách "Tìm gần đây" (VD: "xe đạp"). | Hệ thống tự động điền từ khóa đó vào ô tìm kiếm và thực hiện truy vấn ngay lập tức mà không cần gõ phím. Tiếp tục từ Bước 3 của Basic Path. |
| 2a | Kết hợp bộ lọc nâng cao (Danh mục, Khoảng giá, Tình trạng, Hình thức) |  |
| 2a1 | Người dùng chọn một hoặc nhiều tiêu chí lọc trên màn hình B02: chọn Danh mục, nhập Khoảng giá (Từ - Đến), chọn Tình trạng món đồ (Mới, Đã sử dụng...), chọn Hình thức giao dịch (Bán, Trao đổi, Cho tặng). | Đánh dấu các thẻ lọc đã chọn và kiểm tra giá trị khoảng giá hợp lệ (giá Đến ≥ giá Từ). |
| 2a2 | Người dùng nhấn nút "Áp dụng". | Hệ thống kết hợp từ khóa cùng các điều kiện lọc để lọc danh sách bài đăng từ cơ sở dữ liệu. Tiếp tục thực hiện từ Bước 4 của Basic Path. |
| 2b | Lọc theo khu vực và bán kính (STT 27) |  |
| 2b1 | Người dùng chọn một tỉnh hoặc thành phố ở mục "Khu vực" trên màn hình B02 (`SearchFilter.province`). | Đánh dấu thẻ tỉnh hoặc thành phố đã chọn. |
| 2b2 | Người dùng chọn bán kính 1 km, 3 km hoặc 5 km quanh vị trí hiện tại (`SearchFilter.radiusKm`). | Lần đầu, hệ thống hỏi quyền truy cập vị trí; khi được cấp quyền thì lấy vị trí hiện tại làm tâm tìm (`SearchFilter.center`). |
| 2b3 | Người dùng nhấn nút "Áp dụng". | Hệ thống kết hợp từ khóa với điều kiện tỉnh hoặc thành phố và bán kính, giữ lại các tin nằm trong phạm vi, ưu tiên hiển thị tin ở gần. Tiếp tục thực hiện từ Bước 4 của Basic Path. |
| 2c | Sắp xếp kết quả (STT 31 đến 34) |  |
| 2c1 | Người dùng chọn một trong bốn cách sắp xếp ở mục "Sắp xếp" trên màn hình B02: "Mới đăng", "Giá thấp đến cao", "Giá cao đến thấp" hoặc "Người bán đánh giá tốt", rồi nhấn nút "Áp dụng". | Hệ thống ghi cách sắp xếp đã chọn vào `SearchFilter.sortOrder` (lần lượt `NEWEST`, `PRICE_ASC`, `PRICE_DESC`, `TOP_RATED`). |
| 2c2 |  | Hệ thống sắp xếp lại danh sách kết quả theo cách đã chọn: tin đăng gần đây nhất xếp đầu, giá tăng dần, giá giảm dần, hoặc người bán có điểm đánh giá cao hơn xếp trước. Tiếp tục thực hiện từ Bước 4 của Basic Path. |

**Luồng ngoại lệ**

| Bước | Hành động của tác nhân | Phản hồi của hệ thống |
| --- | --- | --- |
| 2e | Nhập khoảng giá không hợp lệ<br>Tại Bước 2a1, nếu người dùng nhập giá "Đến" nhỏ hơn giá "Từ": | Hệ thống hiển thị cảnh báo đỏ dưới ô giá: "Mức giá đến không thể nhỏ hơn mức giá từ" và vô hiệu hóa nút "Áp dụng". |
| 2f | Không được cấp quyền vị trí khi lọc theo bán kính<br>Tại Bước 2b2, nếu người dùng không cấp quyền truy cập vị trí: | Hệ thống không lọc theo bán kính (vẫn áp dụng các tiêu chí khác, kể cả tỉnh hoặc thành phố) và báo rằng chưa lọc theo bán kính được vì thiếu quyền vị trí. |
| 3e | Không tìm thấy kết quả phù hợp<br>Tại Bước 3, nếu không có bài đăng nào khớp với từ khóa hoặc bộ lọc: | Hệ thống hiển thị hình minh họa trống kèm thông báo: "Không tìm thấy kết quả nào phù hợp với 'từ khóa'. Hãy thử dùng từ khóa khác hoặc xóa bớt bộ lọc nhé!", kèm nút "Xóa bộ lọc". |

**Quy tắc nghiệp vụ**

- BR1 (Phạm vi kết quả): Kết quả tìm kiếm chỉ gồm các tin đăng có trạng thái AVAILABLE; tin nháp, đang giữ chỗ, đã ẩn, đã bán và đã gỡ không xuất hiện.
- BR2 (Khớp từ khóa): Từ khóa được so với searchKeywords của tin đăng; khi có bộ lọc, tin phải thỏa đồng thời từ khóa và mọi điều kiện lọc đã chọn.
- BR3 (Quyền sử dụng): Khách không cần đăng nhập vẫn được tìm kiếm tin đăng.
- BR4 (Lịch sử tìm kiếm): Các từ khóa đã tìm chỉ được lưu trên thiết bị, không gửi lên máy chủ.
- BR5 (Khu vực và bán kính): Bán kính chỉ có ba mức 1 km, 3 km và 5 km, tính quanh vị trí hiện tại; không cấp quyền vị trí thì không lọc theo bán kính. Tin không có tọa độ không xuất hiện khi lọc theo bán kính.
- BR6 (Sắp xếp): Có bốn cách sắp xếp kết quả: mới đăng, giá thấp đến cao, giá cao đến thấp, người bán đánh giá tốt (STT 33, 31, 32, 34). Đây là sắp xếp kết quả, không loại bớt tin.

**Yêu cầu phi chức năng**

- Khả năng sử dụng ngoại tuyến (Offline Availability): Khi mất mạng, người dùng vẫn tìm được trong các tin đã lưu đệm bằng SQLite Room.
- Thích ứng màn hình: Giữ nguyên từ khóa, các bộ lọc đang chọn và kết quả khi xoay ngang/dọc thiết bị.

## UC15 Tìm kiếm người dùng

| Mục | Nội dung |
| --- | --- |
| Mã use case | UC15 |
| Tên use case | Tìm kiếm người dùng |
| Tác nhân | Người dùng |
| Mô tả | Cho phép người dùng đã đăng nhập tìm một người dùng khác theo tên hiển thị và mở trang cá nhân của người đó để xem hồ sơ, các món đang bán và đánh giá. |
| Mức ưu tiên | Thấp |
| Khởi phát | Người dùng bấm tab "Người dùng" (cạnh tab "Tin đăng") tại màn hình Tìm kiếm (B02) và gõ tên hiển thị của người cần tìm. |
| Tiền điều kiện | 1. Người dùng đã đăng nhập vào hệ thống (Khách không được tìm kiếm người dùng).<br>2. Thiết bị có kết nối mạng Internet. |
| Hậu điều kiện | Danh sách tài khoản người dùng khớp với tên đã nhập được hiển thị kèm thông tin công khai tóm tắt (ảnh đại diện, tên hiển thị, khu vực); người dùng mở được trang cá nhân của người được chọn. |

**Luồng cơ bản**

| Bước | Hành động của tác nhân | Phản hồi của hệ thống |
| --- | --- | --- |
| 1 | Người dùng mở màn hình Tìm kiếm (B02) và bấm tab "Người dùng" (Trigger). | Hiển thị tab "Người dùng" với ô nhập tên hiển thị của người cần tìm. |
| 2 | Người dùng nhập tên hiển thị vào ô tìm kiếm và nhấn phím "Tìm kiếm" trên bàn phím. | Hệ thống gửi truy vấn đến bảng users trên Firestore, so khớp tên với trường searchKeywords (từ đã bỏ dấu, viết thường, chỉ khớp nguyên từ) và chỉ lấy các tài khoản đang hoạt động (ACTIVE). |
| 3 |  | Hiển thị danh sách kết quả tìm kiếm dưới dạng các thẻ ngang: ảnh đại diện, tên hiển thị và khu vực. |
| 4 | Người dùng nhấn vào một dòng tài khoản trong kết quả. | Hệ thống điều hướng sang màn hình D02 Trang cá nhân của người đó. |

**Luồng thay thế**

| Bước | Hành động của tác nhân | Phản hồi của hệ thống |
| --- | --- | --- |
| Không có |  |  |

**Luồng ngoại lệ**

| Bước | Hành động của tác nhân | Phản hồi của hệ thống |
| --- | --- | --- |
| 1e | Khách (chưa đăng nhập) bấm tab "Người dùng"<br>Tại Bước 1, nếu người xem chưa đăng nhập: | Hệ thống đưa Khách tới màn hình đăng nhập (A01), không mở tab "Người dùng". |
| 2e | Mất kết nối mạng Internet<br>Tại Bước 2, nếu thiết bị không có mạng: | Hệ thống thông báo: "Không có kết nối mạng. Vui lòng kiểm tra lại kết nối rồi thử lại". |
| 2f | Không tìm thấy tài khoản người dùng nào<br>Tại Bước 2, nếu không có tài khoản nào khớp với tên tìm kiếm: | Hệ thống hiển thị thông báo: "Không tìm thấy người dùng nào phù hợp". |

**Quy tắc nghiệp vụ**

- BR1 (Phạm vi tìm kiếm): Chỉ tìm theo tên hiển thị; không tìm theo email hay số điện thoại.
- BR2 (Tài khoản hiển thị): Chỉ hiện các tài khoản đang hoạt động (ACTIVE).
- BR3 (Cách khớp tên): Chỉ khớp nguyên từ trên searchKeywords đã bỏ dấu và viết thường.
- BR4 (Quyền sử dụng): Chỉ người dùng đã đăng nhập mới tìm kiếm người dùng và xem được hồ sơ người khác.

**Yêu cầu phi chức năng**

- Quyền riêng tư: Kết quả chỉ chứa thông tin công khai (ảnh đại diện, tên hiển thị, khu vực), không có email, số điện thoại hay thông tin định danh.
- Thích ứng màn hình: Giữ nguyên từ khóa và kết quả khi xoay ngang/dọc thiết bị.

## UC16 Xem chi tiết tin đăng

| Mục | Nội dung |
| --- | --- |
| Mã use case | UC16 |
| Tên use case | Xem chi tiết tin đăng |
| Tác nhân | Người dùng, Khách |
| Mô tả | Cung cấp góc nhìn chi tiết và toàn diện nhất về một món đồ cũ được rao bán: xem bộ sưu tập ảnh độ phân giải cao, giá bán, khoảng giá tham khảo thị trường, tình trạng chi tiết từng bộ phận, thông tin hồ sơ uy tín của người bán; đồng thời cung cấp các nút thao tác nhanh: Lưu tin, Theo dõi người bán, Nhắn tin hoặc Đề nghị giá (Offer). Khách chỉ xem được nội dung tin; các thao tác nhanh đều cần đăng nhập. |
| Mức ưu tiên | Cao |
| Khởi phát | Người dùng/Khách nhấn vào một thẻ bài đăng từ Trang chủ (B01), Kênh đang theo dõi, Kết quả tìm kiếm (B02) hoặc Tin đã lưu (B06). |
| Tiền điều kiện | 1. Khách không cần đăng nhập để xem chi tiết tin.<br>2. Bài đăng được chọn đang tồn tại trong hệ thống. |
| Hậu điều kiện | Toàn bộ dữ liệu chi tiết của bài đăng được hiển thị; đồng thời hệ thống tự động ghi nhận món đồ này vào lịch sử xem trên thiết bị để phục vụ thuật toán gợi ý "Dành cho bạn". |

**Luồng cơ bản**

| Bước | Hành động của tác nhân | Phản hồi của hệ thống |
| --- | --- | --- |
| 1 | Người dùng nhấn vào một thẻ bài đăng bất kỳ (Trigger). | Hệ thống truy xuất dữ liệu chi tiết bài đăng từ Firestore (hoặc SQLite Room) và mở màn hình Chi tiết tin đăng (B03). |
| 2 |  | Hiển thị giao diện màn hình B03 gồm:<br>- Băng chuyền hình ảnh (kèm số thứ tự ảnh, VD: 1/5).<br>- Tên sản phẩm, Nhãn hình thức giao dịch (Bán/Trao đổi/Cho tặng), Giá bán.<br>- Dải gợi ý khoảng giá tham khảo trên thị trường.<br>- Thẻ tóm tắt người bán (Avatar, tên hiển thị, số sao đánh giá, số giao dịch đã bán, nút "Theo dõi").<br>- Bảng thông số tình trạng chi tiết.<br>- Nội dung mô tả chi tiết và khu vực giao dịch.<br>- Thanh công cụ dưới cùng cố định: Nút "Nhắn tin" và nút "Đề nghị giá". |
| 3 | Người dùng vuốt ngang băng chuyền ảnh để xem các góc chụp khác nhau của sản phẩm. | Hệ thống chuyển đổi mượt mà giữa các ảnh và cập nhật chỉ số ảnh (VD: 2/5, 3/5). |
| 4 | Người dùng nhấn vào nút "Nhắn tin" ở thanh điều hướng dưới cùng. | Hệ thống tự động tạo (hoặc mở lại) cuộc trò chuyện với người bán tại màn hình C02 Trò chuyện và đề nghị giá, tự động đính kèm banner sản phẩm này lên đầu khung chat để hai bên tiện trao đổi. |

**Luồng thay thế**

| Bước | Hành động của tác nhân | Phản hồi của hệ thống |
| --- | --- | --- |
| 2a | Người bán tự xem bài đăng của chính mình (khi người dùng mở xem tin đăng do chính tài khoản mình đăng) |  |
| 2a1 | Người dùng mở xem bài đăng thuộc sở hữu của mình. | Hệ thống nhận diện sellerId trùng với uid đang đăng nhập. |
| 2a2 |  | Tự động ẩn hai nút "Nhắn tin" và "Đề nghị giá" ở thanh dưới cùng; thay thế bằng hai nút thao tác của người bán: "Chỉnh sửa" (dẫn sang form sửa B04) và "Đã bán". |
| 2b | Xem hồ sơ người bán |  |
| 2b1 | Người dùng nhấn vào Thẻ thông tin người bán (Avatar hoặc tên hiển thị). | Hệ thống điều hướng người dùng sang màn hình D02 Trang cá nhân của người bán. |
| 2b2 | Người dùng nhấn vào liên kết "Xem đánh giá" trên thẻ người bán. | Hệ thống điều hướng người dùng sang màn hình D03 Hồ sơ uy tín người bán để xem đánh giá sao, tỷ lệ hoàn tất và danh sách các món đồ khác mà người đó đang bán. |
| 4a | Người mua gửi đề nghị giá một chạm (Offer) |  |
| 4a1 | Người dùng nhấn vào nút "Đề nghị giá". | Hiển thị hộp thoại (bottom sheet) đề nghị giá kèm các mức giá gợi ý nhanh (VD: -5%, -10%, -15% so với giá niêm yết) hoặc ô tự nhập số tiền mong muốn. |
| 4a2 | Người dùng chọn mức giá đề nghị và nhấn "Gửi đề nghị". | Hệ thống kiểm tra mức giá không được thấp hơn giá sàn minOfferPrice mà người bán đã cài đặt. |
| 4a3 |  | Tạo thẻ đề nghị giá gửi thẳng vào khung chat với người bán, chuyển người dùng sang màn hình C02 và gửi thông báo NEW_OFFER cho người bán. |

**Luồng ngoại lệ**

| Bước | Hành động của tác nhân | Phản hồi của hệ thống |
| --- | --- | --- |
| 1e | Bài đăng đã bán, đang giữ chỗ hoặc không còn hiển thị<br>Tại Bước 1, nếu bài đăng đã hoàn tất giao dịch, đang giữ chỗ cho một giao dịch, hoặc đã ở trạng thái đã ẩn hoặc đã gỡ: | Tin đã bán: hệ thống làm mờ ảnh sản phẩm, hiển thị đè nhãn xám "ĐÃ BÁN" và vô hiệu hóa các nút "Đề nghị giá" / "Nhắn tin". Tin đang giữ chỗ: hiển thị nhãn "ĐANG GIỮ CHỖ", vô hiệu hóa nút "Đề nghị giá", nút "Nhắn tin" vẫn dùng được. Tin đã ẩn hoặc đã gỡ: người khác không mở được, hệ thống hiển thị "Tin không còn". |
| 4e | Khách vãng lai bấm "Nhắn tin" hoặc "Đề nghị giá"<br>Tại Bước 4 hoặc Bước 4a1, nếu người xem chưa đăng nhập: | Hệ thống hiển thị hộp thoại yêu cầu: "Vui lòng đăng nhập để trao đổi và thương lượng giá với người bán", kèm nút chuyển sang màn hình Đăng nhập (A01). |
| 4f | Khách vãng lai bấm Lưu tin, Theo dõi, Báo cáo tin hoặc xem hồ sơ người bán<br>Tại Bước 2 hoặc bước 2b1, nếu người xem chưa đăng nhập: | Hệ thống đưa Khách tới màn hình Đăng nhập (A01); không lưu tin, không theo dõi, không mở hồ sơ người bán. |
| 4g | Đề nghị giá thấp hơn mức sàn người bán cho phép<br>Tại Bước 4a2, nếu người mua nhập số tiền thấp hơn minOfferPrice: | Hệ thống từ chối gửi và thông báo lỗi: "Mức giá đề nghị quá thấp so với mức giá tối thiểu người bán có thể chấp nhận". |

**Quy tắc nghiệp vụ**

- BR1 (Phạm vi tin đăng hiển thị): Tìm kiếm chỉ trả về các tin có trạng thái AVAILABLE. Xem chi tiết áp dụng cho tin đang bán (AVAILABLE), đang giữ chỗ (RESERVED) và đã bán (SOLD) với các nút bị khóa như ở ngoại lệ 1e; riêng người bán xem tin của chính mình thì có thể xem cả tin HIDDEN hoặc DRAFT.
- BR2 (Khoảng giá tham khảo): Dải giá tham khảo trên màn hình chi tiết được hệ thống tự động tính toán cục bộ dựa trên trung bình cộng giá của các sản phẩm tương tự cùng danh mục và cùng tình trạng, không lưu trữ cố định trên Firestore.
- BR3 (Quyền của Khách): Khách chỉ xem được nội dung tin. Nhắn tin, đề nghị giá, lưu tin, theo dõi, báo cáo và xem hồ sơ người bán đều cần đăng nhập.

**Yêu cầu phi chức năng**

- Khả năng sử dụng ngoại tuyến: Khi mất mạng, hiển thị lại tin đã xem từ bộ nhớ đệm SQLite Room; không gửi được đề nghị giá.
- Hiệu năng: Ảnh trong băng chuyền được tải theo kiểu lười (lazy loading), vuốt chuyển ảnh mượt, không giật.
- Thích ứng màn hình: Giữ nguyên vị trí ảnh đang xem và vị trí cuộn khi xoay ngang/dọc thiết bị.

## UC17 Đăng tin

| Mục | Nội dung |
| --- | --- |
| Mã use case | UC17 |
| Tên use case | Đăng tin |
| Tác nhân | Người dùng đã xác thực |
| Mô tả | Cho phép người bán công khai tin đăng của mình lên sàn: ngay sau khi điền xong biểu mẫu Tạo tin đăng, hoặc về sau từ một bản nháp đã lưu. Hệ thống kiểm tra tin có tiêu đề, danh mục, từ 1 đến 5 ảnh và ít nhất một hình thức giao dịch, sau đó chuyển tin sang trạng thái đang bán (AVAILABLE) và đưa lên Trang chủ. Use case này mở rộng UC10 Tạo tin đăng tại điểm người bán nhấn nút "Đăng tin". |
| Mức ưu tiên | Cao |
| Khởi phát | Người bán nhấn nút "Đăng tin" ở cuối biểu mẫu Đăng sản phẩm (B04), hoặc chọn "Đăng" trên một thẻ tin nháp tại tab "Nháp" của Kệ hàng của tôi (B05). |
| Tiền điều kiện | 1. Người bán đã đăng nhập tài khoản vào ứng dụng.<br>2. Tài khoản đã được admin duyệt xác thực.<br>3. Tài khoản đang ở trạng thái hoạt động bình thường (ACTIVE).<br>4. Có một tin đang soạn trong biểu mẫu B04 (tin mới) hoặc một tin nháp (DRAFT) của chính người bán.<br>5. Thiết bị có kết nối mạng Internet. |
| Hậu điều kiện | 1. Tin chuyển sang trạng thái AVAILABLE (đang bán), có searchKeywords, hiển thị công khai trên Trang chủ (Kênh chung), kết quả tìm kiếm và tab "Đang hoạt động" của Kệ hàng của tôi.<br>2. Nếu đăng từ bản nháp: tin được gỡ khỏi tab "Nháp".<br>3. Nếu đăng thất bại: tin giữ nguyên trạng thái trước đó (chưa đăng hoặc DRAFT) và nội dung người bán đã nhập không bị mất. |

**Luồng cơ bản**

| Bước | Hành động của tác nhân | Phản hồi của hệ thống |
| --- | --- | --- |
| 1 | Người bán nhấn nút "Đăng tin" trên biểu mẫu B04 sau khi điền xong (tin mới ở UC10, hoặc bản nháp được mở lại ở UC09) (Trigger). | Hệ thống kiểm tra tài khoản được phép bán (đã được admin duyệt xác thực) và thiết bị có kết nối mạng. |
| 2 |  | Hệ thống kiểm tra điều kiện công khai: có tiêu đề, có danh mục, có từ 1 đến 5 ảnh, chọn ít nhất một hình thức giao dịch (Bán, Trao đổi, Cho tặng); nếu có hình thức Bán thì giá bán phải lớn hơn 0. |
| 3 |  | Hệ thống nén ảnh trên thiết bị (ảnh đầy đủ cạnh dài tối đa 1024 px, không quá 300 KB trước khi mã hóa; ảnh thu nhỏ 320 px, không quá 20 KB), mã hóa Base64, trích xuất từ khóa tìm kiếm (searchKeywords) và kiểm tra tin có chứa từ khóa cấm hay không. |
| 4 |  | Hệ thống lưu tin vào Firestore (collection listings) với trạng thái AVAILABLE: tin mới được tạo trực tiếp ở trạng thái đang bán, bản nháp được chuyển từ DRAFT sang AVAILABLE bằng Listing.publish(). |
| 5 |  | Hiển thị thông báo "Đăng tin thành công!", điều hướng người bán sang màn hình Chi tiết tin đăng (B03) của tin vừa đăng; tin xuất hiện trên Trang chủ. Nếu đăng từ bản nháp, tin được gỡ khỏi tab "Nháp" và đưa sang tab "Đang hoạt động". |

**Luồng thay thế**

| Bước | Hành động của tác nhân | Phản hồi của hệ thống |
| --- | --- | --- |
| 1a | Đăng ngay từ thẻ tin nháp trên kệ hàng |  |
| 1a1 | Tại tab "Nháp" của màn hình Kệ hàng của tôi (B05), người bán nhấn nút ba chấm (...) trên thẻ tin nháp và chọn "Đăng". | Hệ thống kiểm tra tài khoản và kết nối mạng. Tiếp tục thực hiện từ Bước 2 của Basic Path. |
| 3a | Tin chứa từ khóa cấm |  |
| 3a1 |  | Hệ thống vẫn cho đăng tin, gắn cờ tin để admin xem xét, và tiếp tục từ Bước 4 của Basic Path. |
| 4a | Người bán chưa có địa chỉ mặc định |  |
| 4a1 |  | Hệ thống nhắc người bán thêm địa chỉ mặc định ở màn hình Địa chỉ giao nhận (A06). Tin vẫn được đăng nhưng không có tọa độ nên không xuất hiện khi người mua lọc theo bán kính. Tiếp tục từ Bước 4 của Basic Path. |

**Luồng ngoại lệ**

| Bước | Hành động của tác nhân | Phản hồi của hệ thống |
| --- | --- | --- |
| 1e | Tài khoản chưa được admin duyệt xác thực<br>Tại Bước 1, nếu người bán nhấn "Đăng tin" khi tài khoản chưa được xác thực: | Hệ thống từ chối đăng và báo rõ lý do: "Tài khoản của bạn chưa được xác thực nên chưa thể đăng tin". Tin không bị mất, người bán vẫn lưu nháp được. |
| 1f | Mất kết nối mạng Internet<br>Tại Bước 1, nếu thiết bị không có mạng: | Hệ thống không đăng tin, thông báo: "Không có kết nối mạng. Vui lòng thử lại khi có mạng", giữ nguyên nội dung đã nhập trên form. |
| 2e | Thiếu thông tin bắt buộc để công khai<br>Tại Bước 2, nếu tin chưa có tiêu đề, chưa chọn danh mục, chưa có ảnh nào hoặc chưa chọn hình thức giao dịch: | Hệ thống chặn lệnh đăng tin, viền đỏ các ô còn thiếu và hiển thị cảnh báo: "Tin đăng cần có tối thiểu tiêu đề, danh mục, 1 hình ảnh và một hình thức giao dịch để có thể công khai". Người bán bổ sung đủ mới được đăng. |
| 2f | Giá bán không hợp lệ<br>Tại Bước 2, nếu tin có hình thức Bán nhưng giá bán bằng 0 hoặc là số âm: | Hệ thống hiển thị thông báo lỗi dưới ô giá: "Giá bán phải lớn hơn 0 đối với hình thức Bán" và không đăng tin. |
| 4e | Lỗi khi lưu tin<br>Tại Bước 4, nếu mất mạng hoặc ghi vào Firestore thất bại: | Hệ thống thông báo: "Đăng tin thất bại, vui lòng thử lại", giữ nguyên nội dung và ảnh đã chọn trên form; tin vẫn ở trạng thái trước đó và chưa công khai. |

**Quy tắc nghiệp vụ**

- BR1 (Điều kiện công khai tin): Tin chỉ được đăng khi có tiêu đề, danh mục, từ 1 đến 5 ảnh và ít nhất một hình thức giao dịch (Bán/Trao đổi/Cho tặng); có hình thức Bán thì giá bán lớn hơn 0.
- BR2 (Quyền đăng tin): Chỉ chủ tin, có tài khoản đang hoạt động (ACTIVE) và đã được admin duyệt xác thực, mới đăng được tin. Quyền này do Firebase Authentication và Security Rules quyết định, không lấy từ dữ liệu ứng dụng gửi lên.
- BR3 (Chuyển trạng thái): Trạng thái tin chỉ đổi qua các phương thức đã định (tạo tin mới thành AVAILABLE; Listing.publish() từ DRAFT sang AVAILABLE), không ghi trực tiếp trường trạng thái.
- BR4 (Ảnh): Mỗi tin có tối đa 5 ảnh; ảnh nén trên thiết bị và lưu dạng Base64 trong tài liệu Firestore, không dùng Cloud Storage.

**Yêu cầu phi chức năng**

- Hiệu năng: Thời gian đăng tin hoàn tất dưới 5 giây ở điều kiện mạng bình thường.
- Thích ứng màn hình: Khi xoay ngang/dọc màn hình, dữ liệu đang nhập và danh sách ảnh đã chọn không bị mất.

## Ghi chú sửa đổi

So với bản nháp `local/use-case-md/uc-b-tin-dang.md`, ngày 2026-10-22. Mã bước: luồng thay thế dùng `<số bước>a`, `<số bước>b` với bước con `2a1`, `2a2`; luồng ngoại lệ dùng `<số bước>e`, `<số bước>f`, `<số bước>g` ở cột Bước, thay cho chữ "Không có" của bản nháp. Các dòng "Tại Bước N" đã được kiểm lại, đều trỏ tới bước có thật.

Mã và tên use case

- Khối "UC11 Quản lý tin nháp" đổi thành UC09 (bản nháp có hai khối cùng mã UC11). Thứ tự cuối: UC09, UC10, UC11, UC12, UC13, UC14, UC15, UC16, UC17.
- UC15: tên "Tìm kiếm ng dùng" thành "Tìm kiếm người dùng"; mọi chỗ viết "ng" cho "người" ("ng bán", "ng dùng") viết đủ chữ.
- UC17 Đăng tin: VIẾT MỚI hoàn toàn, không có trong bản nháp. Đây là use case mở rộng UC10 trên sơ đồ. Căn cứ: STT 22 và mục 1 của `workflow/docs/architecture/trang-thai.md` (điều kiện đăng, `Listing.publish`), màn hình B04 và B05. Đã thêm câu trỏ sang UC17 vào UC09 (mô tả, bước 3) và UC10 (mô tả, bước 7).

Quyết định D3 (ảnh Base64, không dùng Cloud Storage), UC10

- Bước 8: bỏ "tải ảnh lên Cloud Storage"; nay là nén ảnh trên thiết bị, mã hóa Base64, lưu trong tài liệu Firestore, kèm giới hạn 1024 px / 300 KB và 320 px / 20 KB.
- Bước 2: "Tải ảnh lên bộ nhớ tạm" thành "Đưa ảnh vào bộ nhớ tạm trên thiết bị" để không hiểu là tải lên máy chủ. Bước 1 ("Tải ảnh" thành "Ảnh") và mô tả ("tải ảnh sản phẩm" thành "chọn ảnh sản phẩm") đổi cùng lý do.
- Ngoại lệ 7E (nay 8e): bỏ "Tải ảnh thất bại", "tải ảnh lên cloud", "tạm lưu trong bộ nhớ máy"; nay là lỗi lưu tin khi mất mạng, nội dung form được giữ nguyên (khớp mục "Mất mạng" của B04).
- Yêu cầu phi chức năng 1: bỏ "đẩy lên Storage", nay là nén trên máy, Base64, lưu trong Firestore.
- Quy tắc nghiệp vụ 1: "dung lượng mỗi ảnh không quá 5MB" thay bằng giới hạn sau khi nén theo D3.
- UC09, yêu cầu phi chức năng 2: "đường dẫn ảnh" thành "ảnh", vì ảnh nằm trong tài liệu, không còn đường dẫn Storage.

Quyết định D5 (chỉ UC12, UC14, UC16 mở cho Khách)

- Tác nhân: UC12, UC14, UC16 ghi "Người dùng, Khách"; UC09, UC10, UC11, UC13, UC15 ghi "Người dùng"; chỉ UC17 ghi "Người dùng đã xác thực". Tiền điều kiện của mỗi use case nói rõ có cần đăng nhập hay không.
- UC15: viết lại hoàn toàn. Bản nháp ghi tác nhân "Admin, ng dùng" nhưng các bước chỉ mô tả admin (màn E01 đến E03, chip lọc trạng thái khóa, kết quả có email, vai trò), sai cả hai phía. Nay là người dùng thường tìm người dùng khác theo tên hiển thị rồi mở trang cá nhân D02, theo STT 103 (so khớp `users.searchKeywords`, chỉ nguyên từ, chỉ tài khoản ACTIVE, không tìm theo email hay số điện thoại). Bỏ luồng lọc trạng thái (3A) cùng chip "Chờ xác minh" không có ở bước 1; luồng thay thế để "Không có". Kết quả chỉ còn thông tin công khai (ảnh đại diện, tên hiển thị, khu vực).
- UC15: thêm ngoại lệ Khách chọn tìm người dùng (đưa tới đăng nhập) và ngoại lệ mất mạng (vì tiền điều kiện đòi có mạng). Ngoại lệ EF1 của bản nháp (nay 2f) bị bọc dấu ngoặc kép thừa quanh cả ô: đã bỏ.
- UC16: ngoại lệ Khách của bản nháp (EF2, nay 4e) vẫn giữ cho "Nhắn tin" và "Đề nghị giá"; thêm 4f cho Lưu tin, Theo dõi, Báo cáo tin và xem hồ sơ người bán (đưa Khách tới đăng nhập) và BR3 về quyền của Khách.
- UC13: thêm ngoại lệ 4e Khách bấm lưu tin (bản nháp chỉ nói trong BR1).
- UC12: ngoại lệ Khách bấm Kênh đang theo dõi (nay 3e) giữ nguyên; nói rõ nút "Đăng nhập" đưa tới màn hình đăng nhập.

Quyết định D6 (cần tài khoản đã xác thực)

- UC17: thêm vào tiền điều kiện "Tài khoản đã được admin duyệt xác thực" và ngoại lệ 1e cho tài khoản chưa xác thực.
- UC09, UC10, UC11: bỏ lại tiền điều kiện "Tài khoản đã được admin duyệt xác thực" và đổi tác nhân về "Người dùng" (quyết định leader 2026-10-22, câu 8): tài khoản chưa được duyệt vẫn mở được B04 và B05, vẫn soạn và lưu nháp được, chỉ bị chặn ở nút Đăng của UC17.

Quyết định D11, mục "Dành cho bạn"

- UC12: thêm luồng thay thế 2a "Xem mục Dành cho bạn" (lịch sử xem và lịch sử tìm kiếm chỉ lưu trên thiết bị, cùng danh mục quan tâm chọn lúc đăng ký), ngoại lệ 2e (chưa có dữ liệu thì ẩn mục, theo B01), quy tắc BR4, và nhắc mục này trong mô tả. UC16 vốn đã ghi lịch sử xem cho thuật toán "Dành cho bạn", giữ nguyên.

Lỗi đã sửa, UC10

- Ngoại lệ lệch một hàng: 2E nằm cạnh phản hồi của 6E, 5E cạnh phản hồi của 2E, 6E cạnh phản hồi của 5E. Nay mỗi điều kiện đi với đúng phản hồi: 2e (quá 5 ảnh) với "Chỉ được tải lên tối đa 5 hình ảnh", 5e (giá) với "Giá bán phải lớn hơn 0", 7e (thiếu trường) với "Vui lòng cung cấp đầy đủ thông tin bắt buộc".
- Xóa ô lạc chứa câu đùa "Giá cao quá nó có hú k :))".
- Ví dụ giá sàn 800.000 đ cao hơn giá bán ví dụ 500.000 đ: đổi giá sàn ví dụ thành 400.000 đ.
- Ngoại lệ 6E ghi "Tại Bước 6" nhưng nút "Đăng tin" được nhấn ở bước 7: nay là 7e, "Tại Bước 7". Ngoại lệ mất mạng ghi "Tại Bước 7" nhưng việc lưu xảy ra ở bước 8: nay là 8e, "Tại Bước 8".
- Quy tắc 3 và nhánh 6a1 mâu thuẫn về điều kiện của bản nháp (quy tắc 3 nói không bắt buộc đủ điều kiện, 6a1 nói chỉ cần tiêu đề hoặc ảnh hoặc mô tả): thống nhất theo 6a1 (nay 7a1) và viết lại quy tắc 3 cho khớp. Nhánh "6A Lưu tin nháp" gắn vào bước 6 trong khi nút "Lưu nháp" nằm cùng nút "Đăng tin" ở bước 7: đổi thành 7a, đặt sau 5a và 5b.
- Bước 7: "giá bán > 0" trái với hình thức Cho tặng (giá luôn bằng 0, bước 5b) nên sửa thành "giá bán > 0 khi có hình thức Bán" và thêm "ít nhất một hình thức giao dịch" cho khớp B04 và UC17.
- Bước 9: "Chi tiết tin đăng vừa tạo hoặc Kệ hàng của tôi" thành chỉ Chi tiết tin đăng (B03), vì B04 ghi rõ đăng xong sang B03 của tin mới.

Lỗi đã sửa, các use case khác

- UC09: ô Khởi phát bị cụt "tại màn hình" nay ghi đủ "tại màn hình Kệ hàng của tôi (B05)". Tác nhân "Người dùng đã đăng nhập" thành "Người dùng" cho thống nhất với UC10 và UC11. Bước 3 bản nháp đòi "tình trạng và giá bán hợp lệ", mâu thuẫn với BR2 (tiêu đề, danh mục, 1 ảnh, hình thức giao dịch) nên nay trỏ sang UC17. Menu "Xóa nháp" thành "Xóa tin" theo B05. Bỏ dấu cách thừa ở tiêu đề "Xóa tin nháp". Ngoại lệ "4E, 2E" (mã không có trong luồng) thành 4e, kèm trỏ tới các bước 3a2 và 1a2.
- UC11: ô Khởi phát nói "Kệ hàng của tôi" còn bước 1 nói "Sản phẩm của tôi": thống nhất thành "Sản phẩm của tôi", là tên mục trên D01; "Kệ hàng của tôi" là tên màn hình B05. Tham chiếu "Bước 2d2" và "Bước 2b1" không tồn tại: nay là 2c2 (xác nhận xóa) và 2a1 (đánh dấu đã bán); nhánh 2A, 2B, 2C đổi thành 2a, 2b, 2c với bước con đánh số.
- UC11, theo tài liệu hệ thống (B05, `trang-thai.md`): BR1 thêm tab "Đã ẩn" (HIDDEN và REMOVED), đưa RESERVED (đang giữ chỗ) vào "Đang hoạt động" và bỏ HIDDEN khỏi tab này; mô tả, bước 1a1 và 2b2 cập nhật theo. Xóa hẳn chỉ áp dụng cho tin nháp: nhánh 2C thành "Xóa tin nháp khỏi kệ hàng", mô tả và BR2 viết lại, ngoại lệ "đã từng có giao dịch" giữ thông báo của tác giả nhưng gắn với tin không còn là tin nháp (2e). Đây là chỗ bản nháp trái với tài liệu hệ thống; leader cần xác nhận.
- UC12: "Bước 1a2" và "Bước 1a1" không tồn tại: nay là 3a3 (kênh theo dõi trống) và 3a1 (Khách chuyển kênh); nhánh 3A, 3B thành 3a, 3b. Tiền điều kiện tách Khách / Kênh đang theo dõi. Ngoại lệ đánh lại mã theo thứ tự bước.
- UC13: nhánh 4A không có hàng tiêu đề: thêm hàng tiêu đề "Lưu một tin từ màn hình Chi tiết tin đăng" và đánh số 4a1 đến 4a3; nhánh 3A thành 3a với 3a1 đến 3a3. Ngoại lệ tin không còn: nhãn "NGƯNG BÁN" thay bằng "Tin không còn" cho tin ẩn hoặc gỡ, "ĐÃ BÁN" giữ cho tin đã bán, theo B06.
- UC14: "Bước 2a1" không tồn tại: nay có bước 2a1; nhánh 1A đặt trước 2A theo thứ tự bước (1a, 2a). Ngoại lệ đánh lại mã 2e, 3e theo thứ tự bước.
- UC16: "Bước 2a1" và "Bước 2a2" không tồn tại: nay là 4a1 và 4a2 (nhánh gửi đề nghị giá gắn vào bước 4, song song với "Nhắn tin"; ngoại lệ trỏ 4a1, 4a2). Tiêu đề "2B. Xem hồ sơ uy tín ng bán" nằm ở cột Bước và 0A đặt sau 2B: nay cả ba nhánh có mã ở cột Bước và theo thứ tự 2a (người bán tự xem, trước là 0A), 2b (hồ sơ người bán), 4a (đề nghị giá). Nhánh 2b: chạm thẻ người bán mở D02 Trang cá nhân, chạm "Xem đánh giá" mở D03 (bản nháp ghi chạm thẻ mở D03, trái với B03). Ngoại lệ EF1 tách thành ba trường hợp theo B03 (đã bán, đang giữ chỗ, ẩn hoặc gỡ), vì bản nháp ghi nhãn "TẠM ẨN" hiện cho mọi người, trái với B03 ("Tin không còn"); BR1 sửa cho khớp. "Tên Shop" thành "tên hiển thị" và nút "Chỉnh sửa tin" thành "Chỉnh sửa" theo B03.

Mục để trống đã điền

- Mức ưu tiên: UC09 Trung bình, UC12 Cao, UC13 Trung bình, UC14 Cao, UC15 Thấp, UC16 Cao (UC10, UC11 giữ Cao; UC17 Cao).
- Quy tắc nghiệp vụ: UC14 (BR1 đến BR4) và UC15 (BR1 đến BR4), lấy từ nội dung use case, B02, STT 103 và STT 38.
- Yêu cầu phi chức năng: UC14 (ngoại tuyến, xoay màn hình), UC15 (quyền riêng tư, xoay màn hình), UC16 (ngoại tuyến theo B03, lazy loading, xoay màn hình).

Quyết định leader 2026-10-22, nhóm B (tin đăng, tìm kiếm)

- UC14, mô tả: thêm "bán kính" cho khu vực và bốn cách sắp xếp; quyết định leader 2026-10-22 (STT 27, 31 đến 34).
- UC14, luồng thay thế: thêm nhánh 2b "Lọc theo khu vực và bán kính" (2b1 đến 2b3, dùng `province`, `radiusKm`, `center`) theo STT 27, và nhánh 2c "Sắp xếp kết quả" (2c1, 2c2, dùng `sortOrder` gồm `NEWEST`, `PRICE_ASC`, `PRICE_DESC`, `TOP_RATED`) theo STT 31 đến 34; quyết định leader 2026-10-22. Cả hai nhánh quay về Bước 4 của luồng cơ bản.
- UC14, luồng ngoại lệ: thêm 2f (không cấp quyền vị trí thì không lọc theo bán kính, theo B02); quyết định leader 2026-10-22.
- UC14, quy tắc nghiệp vụ: thêm BR5 (khu vực và bán kính: ba mức 1, 3, 5 km, không cấp quyền thì không lọc, tin không có tọa độ không xuất hiện, theo B02) và BR6 (bốn cách sắp xếp); quyết định leader 2026-10-22.
- UC14, BR1: "tạm ẩn" thành "đã ẩn" và thêm "đang giữ chỗ" cho khớp tên trạng thái trong `trang-thai.md`; quyết định leader 2026-10-22 (mục 6).
- UC15, ô Khởi phát và bước 1: "chuyển sang tìm người dùng" thành bấm tab "Người dùng" cạnh tab "Tin đăng" trên B02 (B02 nay có tab này); quyết định leader 2026-10-22 (STT 103).
- UC15, ngoại lệ 1e: Khách bấm tab "Người dùng" thì được đưa tới màn hình đăng nhập (A01), không mở tab; quyết định leader 2026-10-22.
- UC12, bước 2a1: mục "Dành cho bạn" lấy thêm danh mục của các tin đã lưu (chỉ khi đã đăng nhập), cạnh lịch sử xem, lịch sử tìm kiếm và danh mục quan tâm, theo STT 38 và `goi-y.md`; quyết định leader 2026-10-22.
- UC12, ngoại lệ 2e và BR4: điều kiện ẩn mục "Dành cho bạn" thêm "chưa lưu tin nào"; BR4 nói rõ tin đã lưu chỉ có với người dùng đã đăng nhập; quyết định leader 2026-10-22.
- UC12, BR1: "Tạm ẩn (HIDDEN)" thành "Đã ẩn (HIDDEN)" theo tên trạng thái chuẩn; quyết định leader 2026-10-22 (mục 6).
- UC11, mô tả, tiêu đề nhánh 2b và bước 2b2: "tạm ẩn/hiện tin" thành "ẩn/hiện lại tin" và nhãn "TẠM ẨN" thành "ĐÃ ẨN" (tên chuẩn của HIDDEN trong `trang-thai.md` và `giao-dien.md`); quyết định leader 2026-10-22 (mục 6).
- UC11, BR1: "người bán tự tạm ẩn" thành "người bán tự ẩn"; quyết định leader 2026-10-22 (mục 6).
- UC11, ngoại lệ 2e: thông báo cũ "Không thể xóa tin đăng đã có lịch sử giao dịch" ngụ ý tin chưa có giao dịch thì xóa được; nay là "Chỉ tin nháp mới xóa được. Tin đã đăng chỉ có thể ẩn đi, hãy chọn Ẩn tin", áp dụng cho mọi tin đã đăng; quyết định leader 2026-10-22 (mục 5, chỉ xóa được tin nháp).
- UC16, ngoại lệ 1e: "Tin bị người bán tạm ẩn hoặc đã gỡ" thành "Tin đã ẩn hoặc đã gỡ", và "đã bị ẩn/gỡ" thành "đã ở trạng thái đã ẩn hoặc đã gỡ", theo tên trạng thái chuẩn; quyết định leader 2026-10-22 (mục 6).
- Mục 4 (thống nhất "xác thực tài khoản"): đã rà cả tệp, không còn chữ "xác minh người bán" hay "huy hiệu đã xác minh" trong nội dung use case (chỉ còn trong mục ghi chú này khi trích bản nháp); không sửa gì ở tệp này; quyết định leader 2026-10-22.
- Màn B02 (`workflow/docs/ui/b-kham-pha-tin-dang/README.md`): thêm tab "Người dùng" cạnh tab "Tin đăng" (ô tìm theo tên hiển thị, kết quả mở D02, chỉ tài khoản đang hoạt động, Khách bấm thì tới A01, trạng thái trống và mất mạng), thêm STT 103 vào "Chức năng liên quan" và ghi vào "Khác với ảnh" rằng ảnh chưa vẽ tab này; quyết định leader 2026-10-22 (mục 2).
- Màn B01 (cùng tệp giao diện): hàng "Dành cho bạn" ghi đủ bốn nguồn (lịch sử xem, lịch sử tìm kiếm, tin đã lưu, danh mục quan tâm) và điều kiện ẩn hàng thêm "danh mục quan tâm hay tin đã lưu"; quyết định leader 2026-10-22 (mục 3), khớp UC12.
- Màn B03 (cùng tệp giao diện): "huy hiệu đã xác minh (nếu có)" thành nhãn "Tài khoản đã xác thực" (khi admin đã duyệt) và thêm vào "Khác với ảnh" rằng ảnh không vẽ nhãn này; quyết định leader 2026-10-22 (mục 4).
- Màn B05 (cùng tệp giao diện): nhãn trạng thái "TẠM ẨN" và "ADMIN ẨN" gộp thành "ĐÃ ẨN" (HIDDEN), mục menu "Tạm ẩn do chính mình" và "Admin ẩn" đổi thành "Đã ẩn do chính mình" và "Đã ẩn do admin"; thêm vào "Khác với ảnh" rằng ảnh vẽ nhãn "TẠM ẨN" trong tab "Đang hoạt động" còn đúng là "ĐÃ ẨN" ở tab "Đã ẩn"; quyết định leader 2026-10-22 (mục 6).
- Màn B06 (cùng tệp giao diện): bỏ chữ "xóa" khỏi "Tin gốc đã bị ẩn, gỡ, xóa hoặc không đọc được", vì chỉ tin nháp mới xóa được và tin đã lưu luôn là tin đã đăng; quyết định leader 2026-10-22 (mục 5).
