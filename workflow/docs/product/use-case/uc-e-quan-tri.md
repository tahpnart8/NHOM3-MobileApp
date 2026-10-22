# Đặc tả use case: E. Quản trị

Module E gồm mười use case UC34 đến UC43. Tác nhân duy nhất là Admin. Các use case chạy trong khu vực quản trị (`AdminActivity`), trừ UC43 là bước đăng nhập để vào khu vực đó. Danh sách cuối cùng:

| Mã | Tên use case |
| --- | --- |
| UC34 | Tra cứu và xem hồ sơ người dùng |
| UC35 | Xử lý vi phạm tài khoản |
| UC36 | Duyệt yêu cầu xác thực người dùng |
| UC37 | Kiểm duyệt tin bị báo cáo |
| UC38 | Quản lý từ khóa cấm |
| UC39 | Quản lý danh mục |
| UC40 | Tra cứu và xem chi tiết giao dịch |
| UC41 | Xử lý khiếu nại |
| UC42 | Quản lý tài khoản admin |
| UC43 | Đăng nhập quản trị |

## UC34 Tra cứu và xem hồ sơ người dùng

| Mục | Nội dung |
| --- | --- |
| Mã use case | UC34 |
| Tên use case | Tra cứu và xem hồ sơ người dùng |
| Tác nhân | Admin |
| Mô tả | Cho phép Admin tìm kiếm người dùng và xem hồ sơ đầy đủ: thông tin tài khoản, trạng thái xác thực, điểm uy tín, số lần cảnh cáo, lịch sử báo cáo và giao dịch liên quan. |
| Mức ưu tiên | Cao |
| Khởi phát | Admin nhấn “Quản lý người dùng” trên trang quản trị, hoặc nhấn vào một người dùng từ hàng chờ báo cáo/khiếu nại |
| Tiền điều kiện | 1. Thiết bị có kết nối mạng Internet.<br>2. Admin đã đăng nhập. |
| Hậu điều kiện | Danh sách hoặc hồ sơ người dùng được hiển thị, dữ liệu không thay đổi. |

**Luồng cơ bản**

| Bước | Hành động của tác nhân | Phản hồi của hệ thống |
| --- | --- | --- |
| 1 | Admin nhấn “Quản lý người dùng” | Hệ thống hiển thị danh sách người dùng, mới tham gia trước (tên, email, trạng thái xác thực, trạng thái tài khoản) |
| 2 | Admin nhập từ khóa (tên hoặc email) và nhấn “Tìm kiếm” | Hệ thống hiển thị danh sách kết quả khớp |
| 3 | Admin chọn một người dùng | Hệ thống hiển thị hồ sơ đầy đủ: thông tin tài khoản, trạng thái xác thực, điểm uy tín, số lần cảnh cáo, báo cáo đã nhận, giao dịch gần đây |

**Luồng thay thế**

| Bước | Hành động của tác nhân | Phản hồi của hệ thống |
| --- | --- | --- |
| 1a | Admin mở hồ sơ từ hàng chờ báo cáo (UC35) hoặc khiếu nại (UC41) | Hệ thống chuyển thẳng vào hồ sơ người dùng liên quan, bỏ qua bước 1 và 2 |

**Luồng ngoại lệ**

| Bước | Hành động của tác nhân | Phản hồi của hệ thống |
| --- | --- | --- |
| 1a | Lỗi tải dữ liệu hoặc mất mạng | Hệ thống báo lỗi và cho phép thử lại |
| 2a | Không có người dùng khớp từ khóa | Hệ thống hiển thị “Không tìm thấy người dùng” |
| 3a | Tài khoản đã bị xóa | Hệ thống báo không còn khả dụng |

**Quy tắc nghiệp vụ**

- BR1. UC này chỉ xem, không sửa dữ liệu; thao tác xử lý nằm ở UC35 và UC36.
- BR2. Số CCCD chỉ hiển thị khi Admin xem yêu cầu xác thực (UC36), không hiển thị ở hồ sơ chung.
- BR3. Mọi lượt Admin xem hồ sơ chi tiết được ghi log.
- BR4. Danh sách phân trang khi quá dài.

**Yêu cầu phi chức năng**

- NFR1. Kết quả tìm kiếm trả về trong vòng 3 giây.
- NFR2. Dữ liệu cá nhân chỉ truyền qua kênh mã hóa (HTTPS).

## UC35 Xử lý vi phạm tài khoản

| Mục | Nội dung |
| --- | --- |
| Mã use case | UC35 |
| Tên use case | Xử lý vi phạm tài khoản |
| Tác nhân | Admin |
| Mô tả | Cho phép Admin xem các báo cáo vi phạm nhằm vào một người dùng (từ UC33) và ra quyết định: bỏ qua báo cáo, cảnh cáo hoặc khóa tài khoản. Cảnh cáo đi theo ba bậc: bậc 1 chỉ cảnh cáo và tài khoản vẫn hoạt động, bậc 2 khóa tạm, bậc 3 trở lên khóa vĩnh viễn. Hệ thống chỉ khóa tài khoản theo ba bậc, không tự gỡ tin; gỡ tin của tài khoản bị khóa là một thao tác riêng của Admin. |
| Mức ưu tiên | Cao |
| Khởi phát | Admin nhấn vào một báo cáo trong hàng chờ “Báo cáo người dùng”, hoặc nhấn “Xử lý vi phạm” ở hồ sơ người dùng |
| Tiền điều kiện | 1. Thiết bị có kết nối mạng Internet.<br>2. Admin đã đăng nhập trang quản trị.<br>3. Có ít nhất một báo cáo ở trạng thái “Chờ xử lý” nhằm vào người dùng này, hoặc Admin phát hiện vi phạm từ hồ sơ. |
| Hậu điều kiện | 1. Báo cáo chuyển sang “Đã xử lý” kèm quyết định và lý do.<br>2. Tài khoản bị xử lý chịu hậu quả tương ứng (cảnh cáo theo bậc, khóa).<br>3. Người bị xử lý và người báo cáo nhận được thông báo.<br>4. Quyết định được ghi log. |

**Luồng cơ bản**

| Bước | Hành động của tác nhân | Phản hồi của hệ thống |
| --- | --- | --- |
| 1 | Admin mở hàng chờ “Báo cáo người dùng” và chọn một báo cáo | Hệ thống hiển thị chi tiết: người bị báo cáo, lý do, mô tả, bằng chứng, số lần cảnh cáo hiện tại, các báo cáo trước đó |
| 2 | Admin chọn quyết định: “Bỏ qua”, “Cảnh cáo” hoặc “Khóa tài khoản”, nhập lý do (chọn thêm thời hạn khóa khi khóa tạm hoặc khi cảnh cáo bậc 2) và nhấn “Xác nhận” | Hệ thống kiểm tra đã nhập đủ yêu cầu |
| 3 |  | Hệ thống lưu quyết định, cập nhật trạng thái báo cáo thành “Đã xử lý” |
| 4 |  | Nếu “Cảnh cáo”: hệ thống tăng số lần cảnh cáo lên 1 và áp dụng theo bậc mới: bậc 1 chỉ cảnh cáo, tài khoản vẫn hoạt động; bậc 2 khóa tạm đến hạn Admin đã chọn; bậc 3 trở lên khóa vĩnh viễn (`Warning.resultingStatus()`). Hệ thống chỉ khóa tài khoản, không tự gỡ tin của tài khoản đó |
| 5 |  | Hệ thống gửi thông báo cho người bị xử lý (kèm lý do) và cho người báo cáo (kết quả chung, không tiết lộ chi tiết) |

**Luồng thay thế**

| Bước | Hành động của tác nhân | Phản hồi của hệ thống |
| --- | --- | --- |
| Không có | Không có | Không có |

**Luồng ngoại lệ**

| Bước | Hành động của tác nhân | Phản hồi của hệ thống |
| --- | --- | --- |
| 1a | Báo cáo đã được Admin khác xử lý | Hệ thống báo “Báo cáo đã được xử lý” và làm mới danh sách |
| 1b | Người bị báo cáo đã xóa tài khoản | Hệ thống đóng báo cáo và ghi chú “Tài khoản không còn tồn tại” |
| 2a | Admin chưa nhập lý do | Hệ thống báo lỗi, giữ nguyên form |
| 3a | Mất mạng | Hệ thống báo lỗi và giữ nguyên trạng thái |

**Quy tắc nghiệp vụ**

- BR1. Cảnh cáo theo ba bậc, quy tắc cố định: bậc 1 chỉ cảnh cáo, tài khoản vẫn hoạt động; bậc 2 khóa tạm; bậc 3 trở lên khóa vĩnh viễn.
- BR2. Tài khoản bị khóa (tạm hoặc vĩnh viễn) không đăng nhập được và không tìm thấy trong hệ thống; hệ thống không tự gỡ tin của tài khoản bị khóa; Admin gỡ toàn bộ tin bằng thao tác riêng “Gỡ toàn bộ tin” ở hồ sơ người dùng (`AdminRepository.removeAllListingsOf`).
- BR3. Giao dịch đang chạy của tài khoản bị khóa vẫn chạy tiếp; hai bên tự hủy hoặc mở khiếu nại như bình thường.
- BR4. Mỗi quyết định bắt buộc có lý do và được ghi log.
- BR5. Người báo cáo không được biết chi tiết hình thức xử phạt.
- BR6. Số lần cảnh cáo tồn tại vĩnh viễn; mở khóa tài khoản không đặt lại số lần cảnh cáo.

**Yêu cầu phi chức năng**

- NFR1. Thông báo kết quả đến người liên quan trong vòng 5 giây.
- NFR2. Mọi quyết định có ghi log gồm Admin thực hiện, thời điểm, lý do để truy vết.

## UC36 Duyệt yêu cầu xác thực người dùng

| Mục | Nội dung |
| --- | --- |
| Mã use case | UC36 |
| Tên use case | Duyệt yêu cầu xác thực người dùng |
| Tác nhân | Admin |
| Mô tả | Cho phép Admin xem các yêu cầu xác thực tài khoản (từ UC31) và duyệt hoặc từ chối. Duyệt yêu cầu là cửa mở quyền mua và bán: tài khoản được duyệt mới đăng tin, gửi đề nghị và tạo giao dịch được; tài khoản bị từ chối vẫn xem, tìm kiếm, nhắn tin và theo dõi được như bình thường. |
| Mức ưu tiên | Cao |
| Khởi phát | Admin nhấn “Yêu cầu xác thực” |
| Tiền điều kiện | 1. Thiết bị có kết nối mạng Internet.<br>2. Admin đã đăng nhập trang quản trị.<br>3. Có ít nhất một yêu cầu ở trạng thái “Chờ duyệt”. |
| Hậu điều kiện | 1. Yêu cầu chuyển sang “Đã duyệt” hoặc “Bị từ chối”.<br>2. Nếu duyệt: tài khoản chuyển sang “Đã xác thực”, người đó từ đây đăng tin, gửi đề nghị và tạo giao dịch được.<br>3. Nếu từ chối: tài khoản vẫn chưa xác thực, vẫn xem, tìm kiếm, nhắn tin và theo dõi được nhưng chưa đăng tin, gửi đề nghị hay tạo giao dịch được; lý do từ chối được lưu.<br>4. Người dùng nhận được thông báo kết quả. |

**Luồng cơ bản**

| Bước | Hành động của tác nhân | Phản hồi của hệ thống |
| --- | --- | --- |
| 1 | Admin nhấn “Yêu cầu xác thực” | Hệ thống hiển thị danh sách yêu cầu “Chờ duyệt”, cũ nhất trước (tên, thời điểm gửi) |
| 2 | Admin chọn một yêu cầu | Hệ thống hiển thị chi tiết: họ tên, số CCCD do người gửi khai, ảnh giấy tờ, ảnh chân dung cầm giấy tờ và số lần đã gửi lại |
| 3 | Admin đối chiếu số CCCD với hai ảnh, tự xem số CCCD đó đã gắn với tài khoản khác chưa (hệ thống không tự kiểm, xem BR4), rồi nhấn “Duyệt” | Hệ thống cập nhật trạng thái yêu cầu thành “Đã duyệt” |
| 4 |  | Hệ thống chuyển tài khoản sang “Đã xác thực” và mở quyền đăng tin, gửi đề nghị, tạo giao dịch cho tài khoản đó (trường hợp từ chối: xem luồng thay thế 3a và 3b) |
| 5 |  | Hệ thống gửi thông báo kết quả cho người dùng |

**Luồng thay thế**

| Bước | Hành động của tác nhân | Phản hồi của hệ thống |
| --- | --- | --- |
| 3a | Admin nhấn “Từ chối” | Hệ thống hiển thị ô nhập lý do từ chối |
| 3b | Admin nhập lý do, sau đó nhấn “Xác nhận” | Hệ thống lưu quyết định “Bị từ chối” cùng lý do, giữ tài khoản ở trạng thái chưa xác thực và gửi thông báo cho người dùng. Mở lại quyền gửi yêu cầu xác thực cho tài khoản đó |

**Luồng ngoại lệ**

| Bước | Hành động của tác nhân | Phản hồi của hệ thống |
| --- | --- | --- |
| 1a | Không có yêu cầu chờ duyệt | Hệ thống hiển thị “Không có yêu cầu nào” |
| 2a | Yêu cầu đã được xử lý bởi Admin khác | Hệ thống báo “Yêu cầu đã được xử lý” và làm mới danh sách |
| 2b | Người dùng đã bị khóa hoặc xóa tài khoản | Hệ thống đóng yêu cầu và báo không thể duyệt |
| 3a | Mất mạng | Hệ thống báo lỗi và giữ nguyên trạng thái |
| 3b | Admin từ chối nhưng chưa nhập lý do (ở luồng thay thế 3b) | Hệ thống báo lỗi, giữ nguyên pop up |

**Quy tắc nghiệp vụ**

- BR1. Yêu cầu do Admin duyệt thủ công.
- BR2. Từ chối bắt buộc có lý do; người dùng được gửi lại tối đa 3 lần theo giới hạn ở UC31 (BR6), hệ thống đếm bằng `resubmitCount`.
- BR3. Số CCCD, ảnh giấy tờ và ảnh chân dung cầm giấy tờ chỉ Admin xem được, không hiển thị công khai; Admin chỉ thấy các dữ liệu này khi mở yêu cầu xác thực, không thấy ở hồ sơ chung (UC34).
- BR4. Hệ thống không kiểm số CCCD đã dùng cho tài khoản khác, vì `sellerVerifications` chỉ chủ tài khoản và Admin đọc được và Security Rules không ép được số CCCD duy nhất. Admin có trách nhiệm đối chiếu bằng mắt khi duyệt ở màn E03; nếu số CCCD đã gắn với một tài khoản khác thì Admin từ chối và ghi lý do.
- BR5. Thời hạn xử lý yêu cầu tối đa sau 48 tiếng.
- BR6. Xác thực là điều kiện bắt buộc để đăng tin, gửi đề nghị và tạo giao dịch. Tài khoản chưa xác thực hoặc bị từ chối vẫn xem, tìm kiếm, nhắn tin và theo dõi được.
- BR7. Yêu cầu xác thực lưu số CCCD người gửi khai (`nationalId`), mã các ảnh giấy tờ và ảnh chân dung cầm giấy tờ (`imageIds`) và số lần đã gửi lại (`resubmitCount`).

**Yêu cầu phi chức năng**

- NFR1. Số CCCD và hai ảnh của yêu cầu chỉ chủ tài khoản và Admin đọc được, do Security Rules chặn (`workflow/docs/data/security-rules.md` mục 3 và mục 6). Dự án dùng gói Firebase Spark, không có Cloud Functions nên không mã hóa thêm ở phía máy chủ; đường truyền đã là HTTPS. Mọi lượt Admin mở yêu cầu đều được ghi log.
- NFR2. Thông báo kết quả đến người dùng trong vòng 5 giây.

## UC37 Kiểm duyệt tin bị báo cáo

| Mục | Nội dung |
| --- | --- |
| Mã use case | UC37 |
| Tên use case | Kiểm duyệt tin bị báo cáo |
| Tác nhân | Admin |
| Mô tả | Cho phép Admin xem các tin đăng bị báo cáo (từ UC33) hoặc bị hệ thống gắn cờ do chứa từ khóa cấm (UC38), và quyết định giữ tin, ẩn tin hoặc gỡ tin. |
| Mức ưu tiên | Cao |
| Khởi phát | Admin nhấn “Tin bị báo cáo” |
| Tiền điều kiện | 1. Thiết bị có kết nối mạng Internet.<br>2. Admin đã đăng nhập trang quản trị.<br>3. Có ít nhất một tin ở trạng thái “Chờ kiểm duyệt”. |
| Hậu điều kiện | 1. Tin được giữ nguyên, ẩn hoặc gỡ theo quyết định.<br>2. Báo cáo chuyển sang “Đã xử lý”.<br>3. Chủ tin và người báo cáo nhận được thông báo. |

**Luồng cơ bản**

| Bước | Hành động của tác nhân | Phản hồi của hệ thống |
| --- | --- | --- |
| 1 | Admin nhấn “Tin bị báo cáo” | Hệ thống hiển thị danh sách tin chờ kiểm duyệt, nhiều báo cáo nhất trước (tiêu đề, số báo cáo, nguồn: người dùng báo cáo/hệ thống gắn cờ); ở danh sách chỉ có thao tác “Ẩn tin”, muốn gỡ tin phải mở chi tiết tin |
| 2 | Admin chọn một tin | Hệ thống mở màn chi tiết tin (`AdminListingDetailFragment`) hiển thị nội dung tin, ảnh, các lý do báo cáo, lịch sử vi phạm của chủ tin |
| 3 | Admin chọn quyết định “Giữ tin”, “Ẩn tin” hoặc “Gỡ tin” (“Gỡ tin” chỉ có ở màn chi tiết này, bắt buộc lý do) và nhập lý do | Hệ thống hiển thị pop up xác nhận |
| 4 | Admin nhấn “Xác nhận” | Hệ thống thực thi quyết định và cập nhật trạng thái báo cáo thành “Đã xử lý” |
| 5 |  | Hệ thống gửi thông báo cho chủ tin (kèm lý do) và cho người báo cáo |

**Luồng thay thế**

| Bước | Hành động của tác nhân | Phản hồi của hệ thống |
| --- | --- | --- |
| Không có | Không có | Không có |

**Luồng ngoại lệ**

| Bước | Hành động của tác nhân | Phản hồi của hệ thống |
| --- | --- | --- |
| 1a | Không có tin nào chờ kiểm duyệt | Hệ thống hiển thị “Không có tin cần kiểm duyệt” |
| 2a | Tin đã bị chủ tin xóa | Hệ thống đóng báo cáo và ghi chú “Tin không còn tồn tại” |
| 3a | Chưa nhập lý do | Hệ thống báo lỗi, giữ nguyên form |
| 4a | Tin đang thuộc một giao dịch có hiệu lực | Hệ thống cảnh báo và yêu cầu Admin xác nhận cách xử lý giao dịch |
| 4b | Tin đã được Admin khác xử lý | Hệ thống báo “Báo cáo đã được xử lý” và làm mới danh sách |
| 4c | Mất mạng | Hệ thống báo lỗi và giữ nguyên trạng thái |

**Quy tắc nghiệp vụ**

- BR1. Tin đăng không cần Admin duyệt trước khi đăng; Admin chỉ kiểm duyệt khi có báo cáo hoặc hệ thống gắn cờ.
- BR2. Tin bị Admin “Ẩn” thì chủ tin không tự hiện lại được, chỉ Admin khôi phục được; tin bị “Gỡ” thì không khôi phục.
- BR3. Hệ thống không tự ẩn tin theo số lượt báo cáo; danh sách ở bước 1 sắp theo số lượt báo cáo giảm dần để Admin thấy tin bị báo nhiều nhất trước.
- BR4. Mỗi quyết định bắt buộc có lý do và được ghi log.
- BR5. “Gỡ tin” là bước nặng hơn “Ẩn tin”: chỉ làm ở màn chi tiết tin (`AdminListingDetailFragment`), không có ở danh sách; bắt buộc lý do (`AdminRepository.removeListing`, `Listing.remove`) và người bán không khôi phục được. Gỡ toàn bộ tin của tài khoản bị khóa là thao tác riêng ở UC35 (STT 77), không thuộc UC này.

**Yêu cầu phi chức năng**

- NFR1. Quyết định có hiệu lực trong vòng 5 giây.
- NFR2. Danh sách tải trong vòng 3 giây.

## UC38 Quản lý từ khóa cấm

| Mục | Nội dung |
| --- | --- |
| Mã use case | UC38 |
| Tên use case | Quản lý từ khóa cấm |
| Tác nhân | Admin |
| Mô tả | Cho phép Admin xem, thêm, sửa, xóa các từ khóa cấm. Hệ thống dùng danh sách này để tự động chặn hoặc gắn cờ tin đăng, tin nhắn và nhận xét chứa từ khóa vi phạm. |
| Mức ưu tiên | Trung bình |
| Khởi phát | Admin nhấn “Từ khóa cấm” trên trang quản trị |
| Tiền điều kiện | 1. Thiết bị có kết nối mạng Internet.<br>2. Admin đã đăng nhập trang quản trị. |
| Hậu điều kiện | Danh sách từ khóa cấm được cập nhật và có hiệu lực với các nội dung đăng mới. |

**Luồng cơ bản**

| Bước | Hành động của tác nhân | Phản hồi của hệ thống |
| --- | --- | --- |
| 1 | Admin nhấn “Từ khóa cấm” | Hệ thống hiển thị danh sách từ khóa hiện có (từ khóa, mức xử lý, ngày thêm) |
| 2 | Admin nhấn “Thêm từ khóa”, nhập từ khóa và nhấn “Lưu” | Hệ thống kiểm tra từ khóa (không rỗng, chưa tồn tại) |
| 3 |  | Hệ thống lưu từ khóa, cập nhật danh sách và áp dụng cho nội dung đăng mới |

**Luồng thay thế**

| Bước | Hành động của tác nhân | Phản hồi của hệ thống |
| --- | --- | --- |
| 2a | Admin chọn một từ khóa và nhấn “Sửa”, đổi nội dung hoặc mức xử lý, nhấn “Lưu” | Hệ thống cập nhật từ khóa và danh sách |
| 2b | Admin chọn một từ khóa và nhấn “Xóa” rồi “Xác nhận” | Hệ thống xóa từ khóa khỏi danh sách; từ khóa đó thôi có hiệu lực với nội dung đăng mới |

**Luồng ngoại lệ**

| Bước | Hành động của tác nhân | Phản hồi của hệ thống |
| --- | --- | --- |
| 2a | Từ khóa rỗng | Hệ thống báo lỗi, giữ nguyên form |
| 2b | Từ khóa đã tồn tại | Hệ thống báo “Từ khóa đã có trong danh sách” |
| 2c | Từ khóa quá dài hoặc chứa ký tự không hợp lệ | Hệ thống báo lỗi định dạng |
| 3a | Mất mạng | Hệ thống báo lỗi và giữ nguyên nội dung đã nhập |

**Quy tắc nghiệp vụ**

- BR1. Từ khóa được so khớp theo chuỗi, không phân biệt hoa thường và dấu.
- BR2. Mỗi từ khóa chỉ xuất hiện một lần trong danh sách.
- BR3. Từ khóa mới chỉ có hiệu lực với nội dung đăng mới; nội dung đã đăng trước đó không bị rà lại.
- BR4. Tin đăng chứa từ khóa cấm được hệ thống gắn cờ và đưa vào danh sách chờ kiểm duyệt; việc giữ, ẩn hay gỡ tin do Admin quyết định ở UC37.

**Yêu cầu phi chức năng**

- NFR1. Từ khóa mới có hiệu lực trong vòng 10 giây.
- NFR2. Kiểm tra từ khóa không làm chậm thao tác đăng tin hoặc gửi tin nhắn quá 1 giây.

## UC39 Quản lý danh mục

| Mục | Nội dung |
| --- | --- |
| Mã use case | UC39 |
| Tên use case | Quản lý danh mục |
| Tác nhân | Admin |
| Mô tả | Cho phép Admin xem, thêm, sửa, ẩn hoặc xóa các danh mục sản phẩm (ví dụ: đồ điện tử, thời trang, đồ gia dụng) mà người dùng chọn khi đăng tin và dùng để lọc tìm kiếm. Danh mục có hai cấp (danh mục gốc và danh mục con, STT 79); Admin cũng cấu hình các thuộc tính tình trạng riêng của từng danh mục (STT 80). |
| Mức ưu tiên | Trung bình |
| Khởi phát | Admin nhấn “Danh mục” trên trang quản trị |
| Tiền điều kiện | 1. Thiết bị có kết nối mạng Internet.<br>2. Admin đã đăng nhập trang quản trị. |
| Hậu điều kiện | Danh mục được cập nhật và hiển thị đúng ở ứng dụng. |

**Luồng cơ bản**

| Bước | Hành động của tác nhân | Phản hồi của hệ thống |
| --- | --- | --- |
| 1 | Admin nhấn “Danh mục” | Hệ thống hiển thị danh sách danh mục, danh mục con thụt vào dưới danh mục cha (tên, trạng thái, số tin đang dùng) |
| 2 | Admin nhấn “Thêm danh mục”, nhập tên, chọn “Danh mục cha” nếu muốn thêm danh mục con (để trống là danh mục gốc), rồi nhấn “Lưu” | Hệ thống kiểm tra tên (không rỗng, chưa trùng) và danh mục cha (phải là danh mục gốc) |
| 3 |  | Hệ thống lưu danh mục và cập nhật danh sách |

**Luồng thay thế**

| Bước | Hành động của tác nhân | Phản hồi của hệ thống |
| --- | --- | --- |
| 2a | Admin chọn danh mục và nhấn “Sửa”, đổi tên rồi “Lưu” | Hệ thống cập nhật tên, các tin đang dùng danh mục tự hiển thị tên mới |
| 2b | Admin nhấn “Ẩn” một danh mục | Hệ thống ẩn danh mục khỏi lựa chọn đăng tin mới, tin cũ vẫn giữ |
| 2c | Admin nhấn “Xóa” rồi “Xác nhận” | Hệ thống xóa danh mục nếu không còn tin sử dụng và không còn danh mục con |
| 2d | Admin nhấn vào một danh mục, thêm hoặc xóa tên thuộc tính tình trạng (ví dụ: Dung lượng pin, Bảo hành) rồi nhấn “Lưu” | Hệ thống cập nhật danh sách thuộc tính tình trạng của danh mục; form đăng tin của danh mục đó hiện các thuộc tính này |

**Luồng ngoại lệ**

| Bước | Hành động của tác nhân | Phản hồi của hệ thống |
| --- | --- | --- |
| 2a | Tên danh mục rỗng | Hệ thống báo lỗi, giữ nguyên form |
| 2b | Tên danh mục đã tồn tại | Hệ thống báo “Tên danh mục đã có trong danh sách” |
| 2c | Xóa danh mục đang có tin sử dụng hoặc đang có danh mục con | Hệ thống từ chối xóa, nêu số tin hoặc số danh mục con, gợi ý dùng “Ẩn” hoặc chuyển tin sang danh mục khác |
| 2d | Tên thuộc tính tình trạng rỗng hoặc trùng trong cùng danh mục (luồng thay thế 2d) | Hệ thống báo lỗi, giữ nguyên form |
| 3a | Mất mạng | Hệ thống báo lỗi và giữ nguyên nội dung đã nhập |

**Quy tắc nghiệp vụ**

- BR1. Tên danh mục là duy nhất.
- BR2. Danh mục đang có tin đăng hoặc đang có danh mục con chỉ được ẩn, không được xóa.
- BR3. Danh mục bị ẩn không xuất hiện ở lựa chọn khi đăng tin mới nhưng vẫn dùng được để lọc tin cũ.
- BR4. Mỗi tin đăng thuộc ít nhất một danh mục.
- BR5. Mọi thay đổi danh mục được ghi log.
- BR6. Danh mục chỉ có hai cấp: danh mục gốc và danh mục con; danh mục con không có danh mục con. Danh mục cha để trống nghĩa là danh mục gốc (`Category.parentId`).
- BR7. Thuộc tính tình trạng được cấu hình riêng cho từng danh mục, lưu trong `Category.conditionFields`; tên thuộc tính không trùng trong cùng danh mục.

**Yêu cầu phi chức năng**

- NFR1. Thay đổi danh mục hiển thị ở ứng dụng trong vòng 10 giây.
- NFR2. Danh sách tải trong vòng 3 giây.

## UC40 Tra cứu và xem chi tiết giao dịch

| Mục | Nội dung |
| --- | --- |
| Mã use case | UC40 |
| Tên use case | Tra cứu và xem chi tiết giao dịch |
| Tác nhân | Admin |
| Mô tả | Cho phép Admin tìm kiếm và xem chi tiết mọi giao dịch trên hệ thống: hai bên, điều khoản giao dịch, trạng thái, mốc thời gian, trạng thái khoản ký quỹ và lịch sử chat liên quan khi có khiếu nại. Admin chỉ theo dõi, không can thiệp: việc giải ngân do hệ thống tự thực hiện, Admin không bao giờ giải ngân bằng tay. |
| Mức ưu tiên | Trung bình |
| Khởi phát | Admin nhấn “Giao dịch” trên trang quản trị |
| Tiền điều kiện | 1. Thiết bị có kết nối mạng Internet.<br>2. Admin đã đăng nhập trang quản trị. |
| Hậu điều kiện | Danh sách hoặc chi tiết giao dịch được hiển thị, dữ liệu không thay đổi. |

**Luồng cơ bản**

| Bước | Hành động của tác nhân | Phản hồi của hệ thống |
| --- | --- | --- |
| 1 | Admin nhấn “Giao dịch” | Hệ thống hiển thị danh sách giao dịch, mới nhất trước (mã giao dịch, hai bên, loại, trạng thái, ngày) |
| 2 | Admin nhập từ khóa (mã giao dịch, tên người dùng) hoặc chọn bộ lọc và nhấn “Tìm kiếm” | Hệ thống hiển thị danh sách kết quả |
| 3 | Admin chọn một giao dịch | Hệ thống hiển thị chi tiết: điều khoản giao dịch, các mốc trạng thái, xác nhận giao/nhận, trạng thái khoản ký quỹ (đang giữ, đã giải ngân hoặc đã hoàn), khiếu nại (nếu có) |

**Luồng thay thế**

| Bước | Hành động của tác nhân | Phản hồi của hệ thống |
| --- | --- | --- |
| Không có | Không có | Không có |

**Luồng ngoại lệ**

| Bước | Hành động của tác nhân | Phản hồi của hệ thống |
| --- | --- | --- |
| 1a | Lỗi tải dữ liệu hoặc mất mạng | Hệ thống báo lỗi và cho phép thử lại |
| 2a | Không có giao dịch khớp | Hệ thống hiển thị “Không tìm thấy giao dịch” |

**Quy tắc nghiệp vụ**

- BR1. UC này chỉ xem; Admin không sửa, hủy, giải ngân hay hoàn tiền trực tiếp từ đây. Khoản ký quỹ do hệ thống tự giải ngân khi người mua xác nhận đã nhận hoặc khi hết đếm ngược.
- BR2. Nội dung chat chỉ hiển thị khi giao dịch có khiếu nại đang xử lý.
- BR3. Thông tin thanh toán hiển thị ở mức trạng thái khoản ký quỹ. Thanh toán là mô phỏng, hệ thống không lưu và không hiển thị thông tin tài khoản ngân hàng của người dùng.
- BR4. Mọi lượt xem chi tiết được ghi log.
- BR5. Danh sách phân trang khi quá dài.

**Yêu cầu phi chức năng**

- NFR1. Kết quả tìm kiếm trả về trong vòng 3 giây.
- NFR2. Trạng thái khoản ký quỹ hiển thị khớp với dữ liệu thanh toán mới nhất của hệ thống.

## UC41 Xử lý khiếu nại

| Mục | Nội dung |
| --- | --- |
| Mã use case | UC41 |
| Tên use case | Xử lý khiếu nại |
| Tác nhân | Admin |
| Mô tả | Cho phép Admin xem khiếu nại giao dịch (từ UC24) cùng bằng chứng của hai bên và ra quyết định: hoàn tiền, hoàn trả sản phẩm, hủy giao dịch, hoặc bác khiếu nại. Admin chỉ ra quyết định; việc thực thi (hoàn tiền hoặc giải ngân khoản ký quỹ, cập nhật trạng thái) do hệ thống thực hiện tự động. |
| Mức ưu tiên | Cao |
| Khởi phát | Admin nhấn vào một khiếu nại trong hàng chờ “Khiếu nại” |
| Tiền điều kiện | 1. Thiết bị có kết nối mạng Internet.<br>2. Admin đã đăng nhập trang quản trị.<br>3. Có khiếu nại ở trạng thái “Chờ xử lý”. |
| Hậu điều kiện | 1. Khiếu nại chuyển sang “Đã xử lý” kèm quyết định và lý do.<br>2. Giao dịch được cập nhật theo quyết định: chuyển sang “đã nhận” và khoản ký quỹ được giải ngân cho người bán (bác khiếu nại), hoặc chuyển sang “đã hủy” và khoản ký quỹ được hoàn lại cho người mua (hoàn tiền, hoàn trả sản phẩm, hủy giao dịch).<br>3. Hai bên nhận được thông báo.<br>4. Quyết định được ghi log. |

**Luồng cơ bản**

| Bước | Hành động của tác nhân | Phản hồi của hệ thống |
| --- | --- | --- |
| 1 | Admin mở hàng chờ “Khiếu nại” và chọn một khiếu nại | Hệ thống hiển thị chi tiết: giao dịch, điều khoản giao dịch, lý do và bằng chứng của bên khiếu nại, trạng thái khoản ký quỹ |
| 2 | Admin xem bằng chứng, lịch sử chat và thông tin giao dịch | Hệ thống hiển thị lịch sử chat của giao dịch và hồ sơ hai bên |
| 3 | Admin chọn quyết định: “Hoàn tiền cho người mua”, “Hoàn trả sản phẩm”, “Hủy giao dịch” hoặc “Bác khiếu nại” (giải ngân cho người bán), nhập lý do gửi cho hai bên | Hệ thống hiển thị pop up xác nhận tóm tắt hậu quả |
| 4 | Admin nhấn “Xác nhận” | Hệ thống lưu quyết định, cập nhật khiếu nại thành “Đã xử lý” |
| 5 |  | Hệ thống thực thi tự động trên khoản ký quỹ đang giữ: hoàn lại cho người mua rồi chuyển giao dịch sang “đã hủy” và hiện lại tin đăng thành “đang bán” (hoàn tiền, hoàn trả sản phẩm, hủy giao dịch); hoặc giải ngân cho người bán rồi chuyển giao dịch sang “đã nhận” (bác khiếu nại) |
| 6 |  | Hệ thống gửi thông báo kết quả và lý do cho hai bên |

**Luồng thay thế**

| Bước | Hành động của tác nhân | Phản hồi của hệ thống |
| --- | --- | --- |
| 1a | Admin mở khiếu nại từ chi tiết giao dịch (UC40) | Hệ thống chuyển thẳng vào chi tiết khiếu nại |
| 1b | Admin chọn “Mức ưu tiên” (Thấp, Trung bình, Cao) cho khiếu nại | Hệ thống lưu ngay mức ưu tiên và cập nhật nhãn ở danh sách |
| 2a | Admin yêu cầu bên khiếu nại bổ sung bằng chứng, nhập nội dung yêu cầu | Hệ thống gửi yêu cầu cho bên khiếu nại và thông báo cho bên còn lại, khiếu nại chuyển sang “Chờ bổ sung bằng chứng”; khi có bằng chứng mới khiếu nại về “Chờ xử lý” và Admin quay lại bước 2 |
| 2b | Admin nhập “Ghi chú nội bộ” | Hệ thống lưu ghi chú; ghi chú chỉ Admin thấy, không hiển thị cho người dùng |

**Luồng ngoại lệ**

| Bước | Hành động của tác nhân | Phản hồi của hệ thống |
| --- | --- | --- |
| 1a | Không có khiếu nại chờ xử lý | Hệ thống hiển thị “Không có khiếu nại nào” |
| 3a | Chưa chọn quyết định hoặc chưa nhập lý do | Hệ thống báo lỗi, giữ nguyên form |
| 4a | Khiếu nại đã được Admin khác xử lý | Hệ thống báo “Khiếu nại đã được xử lý” và làm mới danh sách |
| 4b | Mất mạng | Hệ thống báo lỗi và giữ nguyên trạng thái |
| 5a | Hệ thống không thực thi được quyết định (ví dụ không ghi được dữ liệu) | Hệ thống giữ nguyên khiếu nại, giao dịch và khoản ký quỹ, chưa gửi thông báo, báo lỗi và cho Admin xác nhận lại |

**Quy tắc nghiệp vụ**

- BR1. Admin chỉ ra quyết định, không thao tác trực tiếp vào tiền; hệ thống thực thi tự động.
- BR2. Các hình thức xử lý: hoàn tiền, hoàn trả sản phẩm, hủy giao dịch, bác khiếu nại. Hoàn trả sản phẩm hoàn tiền ngay; việc trả hàng diễn ra ngoài ứng dụng và không được kiểm chứng.
- BR3. Mỗi khiếu nại chỉ có một quyết định cuối cùng; không sửa lại sau khi đã thực thi.
- BR4. Giao dịch tặng và trao đổi (không có tiền) không có khoản ký quỹ, nên chỉ áp dụng hủy giao dịch hoặc bác khiếu nại.
- BR5. Mỗi quyết định bắt buộc có lý do và được ghi log.
- BR6. Thời hạn xử lý khiếu nại tối đa 2 ngày.
- BR7. Cảnh cáo hoặc khóa tài khoản vi phạm không nằm trong quyết định khiếu nại; Admin làm ở UC35, mở từ hồ sơ người dùng (UC34).

**Yêu cầu phi chức năng**

- NFR1. Thông báo kết quả đến hai bên trong vòng 5 giây.
- NFR2. Mọi bước hoàn tiền/giải ngân được hệ thống ghi log để đối soát.
- NFR3. Bằng chứng và lịch sử chat chỉ Admin và hai bên liên quan xem được.

## UC42 Quản lý tài khoản admin

| Mục | Nội dung |
| --- | --- |
| Mã use case | UC42 |
| Tên use case | Quản lý tài khoản admin |
| Tác nhân | Admin |
| Mô tả | Cho phép Admin xem danh sách các tài khoản Admin, thêm Admin mới bằng cách cấp quyền quản trị cho một tài khoản người dùng đã đăng ký, và gỡ quyền Admin của một Admin khác trên trang quản trị. Admin không được tự gỡ quyền của chính mình, và hệ thống luôn phải còn ít nhất một Admin. Phân quyền chi tiết theo nhóm việc là future work. |
| Mức ưu tiên | Thấp |
| Khởi phát | Admin nhấn “Tài khoản quản trị” trên trang quản trị |
| Tiền điều kiện | 1. Thiết bị có kết nối mạng Internet.<br>2. Admin đã đăng nhập trang quản trị. |
| Hậu điều kiện | 1. Danh sách tài khoản Admin được cập nhật theo thao tác: tài khoản được cấp quyền vào được trang quản trị, tài khoản bị gỡ quyền không còn vào được và trở về người dùng thường.<br>2. Thao tác được ghi log. |

**Luồng cơ bản**

| Bước | Hành động của tác nhân | Phản hồi của hệ thống |
| --- | --- | --- |
| 1 | Admin nhấn “Tài khoản quản trị” | Hệ thống hiển thị danh sách tài khoản Admin (tên, email) |
| 2 | Admin nhấn “Thêm admin”, nhập email của một tài khoản người dùng đã đăng ký và nhấn “Tiếp tục” | Hệ thống kiểm tra email (hợp lệ, thuộc một tài khoản đang hoạt động, chưa phải Admin) và hiển thị pop up xác nhận kèm tên và email của tài khoản đó |
| 3 | Admin nhấn “Xác nhận” | Hệ thống cấp quyền Admin cho tài khoản, ghi log và cập nhật danh sách |

**Luồng thay thế**

| Bước | Hành động của tác nhân | Phản hồi của hệ thống |
| --- | --- | --- |
| 2a | Admin chọn một Admin khác trong danh sách và nhấn “Gỡ quyền admin” rồi “Xác nhận” | Hệ thống gỡ quyền Admin của tài khoản đó, ghi log và cập nhật danh sách. Tài khoản không bị xóa: trở về người dùng thường, vẫn đăng nhập được nhưng không vào được trang quản trị |

**Luồng ngoại lệ**

| Bước | Hành động của tác nhân | Phản hồi của hệ thống |
| --- | --- | --- |
| 2a | Email không hợp lệ | Hệ thống báo lỗi, giữ nguyên form |
| 2b | Email không thuộc tài khoản nào, hoặc tài khoản đã bị khóa hoặc xóa | Hệ thống báo không tìm thấy tài khoản có thể cấp quyền, giữ nguyên form |
| 2c | Tài khoản đã là Admin | Hệ thống báo “Tài khoản này đã là Admin” |
| 2d | Admin chọn gỡ quyền của chính tài khoản mình (luồng thay thế 2a) | Hệ thống từ chối, báo Admin không được tự gỡ quyền của chính mình |
| 2e | Việc gỡ quyền khiến không còn Admin nào (luồng thay thế 2a) | Hệ thống từ chối, phải còn ít nhất một Admin |
| 3a | Mất mạng | Hệ thống báo lỗi và giữ nguyên trạng thái |

**Quy tắc nghiệp vụ**

- BR1. Chỉ Admin mới cấp được quyền Admin cho tài khoản khác; không có đăng ký công khai.
- BR2. Gỡ quyền Admin không xóa tài khoản: tài khoản trở về người dùng thường, dữ liệu và log truy vết được giữ nguyên.
- BR3. Admin không được tự gỡ quyền của chính mình.
- BR4. Phải còn ít nhất một Admin.
- BR5. Mọi thao tác trên tài khoản Admin được ghi log.
- BR6. Hiện chưa phân quyền theo vai trò: mọi Admin có quyền như nhau (future work).

**Yêu cầu phi chức năng**

- NFR1. Quyền Admin do dữ liệu phía máy chủ quyết định, ứng dụng không tự khai quyền.
- NFR2. Tài khoản Admin bị gỡ quyền mất quyền vào trang quản trị trong vòng 5 giây.

## UC43 Đăng nhập quản trị

| Mục | Nội dung |
| --- | --- |
| Mã use case | UC43 |
| Tên use case | Đăng nhập quản trị |
| Tác nhân | Admin |
| Mô tả | Cho phép Admin đăng nhập vào hệ thống với vai trò quản trị |
| Mức ưu tiên | Cao |
| Khởi phát | Admin nhấn “Đăng nhập” trên trang chủ hệ thống |
| Tiền điều kiện | 1. Thiết bị có kết nối mạng Internet.<br>2. Đã có tài khoản được cấp vai trò Admin. |
| Hậu điều kiện | Đăng nhập được vào hệ thống với vai trò Admin và thực hiện được các chức năng quản trị |

**Luồng cơ bản**

| Bước | Hành động của tác nhân | Phản hồi của hệ thống |
| --- | --- | --- |
| 1 | Admin nhấn “Đăng nhập” | Hệ thống chuyển qua màn hình đăng nhập, lựa chọn cách thức đăng nhập |
| 2 | Admin chọn đăng nhập bằng tài khoản | Hiển thị form nhập tài khoản, mật khẩu |
| 3 | Nhập tài khoản, mật khẩu vào form, ấn nút đăng nhập | Hệ thống kiểm tra dữ liệu nhập vào |
| 4 |  | Hệ thống chuyển tới màn hình trang chủ, đăng nhập với vai trò admin |

**Luồng thay thế**

| Bước | Hành động của tác nhân | Phản hồi của hệ thống |
| --- | --- | --- |
| 2a | Chọn đăng nhập bằng google | Chuyển qua giao diện chọn tài khoản google |
| 2b | Chọn tài khoản google đã được cấp vai trò admin để đăng nhập | Kiểm tra tài khoản đã được cấp vai trò admin chưa |
| 2c | Nhập mật khẩu cho tài khoản google | Hệ thống kiểm tra dữ liệu nhập vào trùng khớp |
| 2d |  | Hệ thống chuyển qua giao diện trang chủ, trạng thái đã đăng nhập |

**Luồng ngoại lệ**

| Bước | Hành động của tác nhân | Phản hồi của hệ thống |
| --- | --- | --- |
| 3a | Nhập sai tài khoản, mật khẩu | Hệ thống từ chối đăng nhập, thông báo nhập sai tài khoản hoặc mật khẩu |
| 3b | Tài khoản chưa được cấp vai trò admin (ở bước 3 hoặc luồng thay thế 2b) | Hệ thống từ chối đăng nhập, thông báo tài khoản chưa được đăng ký |

**Quy tắc nghiệp vụ**

- BR1. Chỉ tài khoản được Admin cấp vai trò (UC42) mới đăng nhập vào hệ thống với vai trò admin được.

**Yêu cầu phi chức năng**

- NFR1. Mật khẩu Admin được băm, không lưu dạng rõ.
- NFR2. Dữ liệu form giữ nguyên khi xoay màn hình.

## Ghi chú sửa đổi

Mọi chỗ khác bản nháp `local/use-case-md/uc-e-quan-tri.md`, mỗi dòng một chỗ. Chỗ nào không nêu ở đây là giữ nguyên chữ của tác giả (chỉ bỏ khoảng trắng thừa ở đầu, cuối ô).

Chung cho cả tệp:

- Thêm đoạn mở đầu và bảng danh sách mười use case ở đầu tệp, bỏ dòng "Nguồn" và mục "Ghi chú chuyển đổi" của bản chuyển đổi. Lý do: tệp này là bản cuối, không còn là bản chuyển từ bảng tính.
- Bỏ các dấu `<br>` ngắt giữa câu (UC34 bước 3, UC34 luồng thay thế 1a, UC35 Khởi phát và Tiền điều kiện 3, UC36 luồng thay thế 3b, UC38 luồng thay thế 2a, UC39 luồng thay thế, UC41 luồng thay thế, UC42 luồng thay thế, UC43 luồng thay thế 2b). Chỉ giữ `<br>` giữa các mục đánh số của Tiền điều kiện và Hậu điều kiện. Lý do: ngắt dòng do bấm xuống dòng trong bảng tính, không có nghĩa.
- Đánh số lại các bước ngoại lệ theo luật 4: mỗi bước gắn với bước cơ bản mà nó rẽ nhánh, xếp tăng dần; luồng thay thế và luồng ngoại lệ mỗi bảng đánh số riêng (nên một bảng thay thế và một bảng ngoại lệ có thể cùng có "2a"). Chi tiết ở từng use case bên dưới.
- Bỏ các dấu cách thừa ở cuối ô Tác nhân ("Admin "), ô Khởi phát của UC36 và dấu tab đầu ô Mức ưu tiên của UC36.
- Ô Khởi phát và nhiều ô khác: giữ nguyên dấu ngoặc kép cong; nơi bản nháp dùng dấu ngoặc kép thẳng (UC35 bước 2 "xác nhận", UC36 "từ chối" và "xác nhận", UC43 Khởi phát "đăng nhập") được đổi thành dấu cong và viết hoa chữ đầu tên nút cho đồng nhất.
- Kiểm tra tham chiếu chéo: UC33 (Báo cáo vi phạm, module D) đúng ở UC35 và UC37; UC31 (Đăng ký xác thực người dùng, module D) và BR6 của nó đúng ở UC36; UC24 (Khiếu nại giao dịch, module C) đúng ở UC41; UC38 đúng ở UC37; UC35, UC36, UC40, UC41 nhắc nhau trong tệp này đều đúng. Không phải sửa số hiệu nào. Một tham chiếu sai nằm ngoài tệp này được ghi ở báo cáo của agent.

UC34:

- Ngoại lệ đổi thứ tự từ 1b, 1c, 2a thành 1a, 2a, 3a: lỗi tải dữ liệu/mất mạng thành 1a (xảy ra khi tải danh sách, bước 1), không có người dùng khớp thành 2a (bước tìm kiếm), tài khoản đã bị xóa thành 3a (bước chọn người dùng). Lý do: luật 4.
- Ngoại lệ "Tài khoản đã bị xóa": sửa "Hệ thống  báo" (hai dấu cách) thành một dấu cách.
- BR2: thêm dấu chấm cuối câu.
- NFR2: thêm khoảng trắng sau "NFR2." (bản nháp viết "NFR2.Dữ liệu").

UC35:

- Ngoại lệ đổi thứ tự từ 1a, 2a, 1b, 1c thành 1a, 1b, 2a, 3a. "Người bị báo cáo đã xóa tài khoản" từ 1c thành 1b; "Mất mạng" từ 1b thành 3a vì phản hồi "giữ nguyên trạng thái" là lúc lưu quyết định (bước 3), không phải lúc mở báo cáo. Lý do: luật 4.
- Bước 2: dấu ngoặc thẳng quanh "xác nhận" đổi thành “Xác nhận”.
- BR3: "huỷ" thành "hủy" cho khớp cách viết của cả tài liệu.

UC36:

- Tiêu đề: bản nháp ghi "Xử lý yêu cầu xác thực người dùng", sửa thành "Duyệt yêu cầu xác thực người dùng" theo tên trong mô hình cuối và theo hàng Tên use case.
- Mô tả, Hậu điều kiện, bước 4, luồng thay thế 3b và BR6 mới: áp dụng quyết định D6. Bản nháp coi xác thực là huy hiệu ("nhận huy hiệu “Tài khoản đã xác thực”", "gắn huy hiệu"); nay duyệt là điều kiện để đăng tin, gửi đề nghị và tạo giao dịch, còn bị từ chối thì vẫn xem, tìm kiếm, nhắn tin và theo dõi được. Bỏ chữ "huy hiệu" ở mọi chỗ trong use case này. BR6 là quy tắc thêm từ D6, không phải quy tắc tác giả viết.
- Hậu điều kiện: tách thành bốn mục, thêm mục 3 mô tả trường hợp từ chối (D6) và giữ mục thông báo thành mục 4.
- Bước 3: nút "Xác nhận" đổi thành “Duyệt”. Lý do: luồng thay thế 3b cũng dùng nút "Xác nhận" cho việc từ chối, hai nút cùng tên làm đường duyệt và đường từ chối không phân biệt được; màn E03 dùng nút “Duyệt” và “Từ chối”. Trạng thái yêu cầu ghi rõ “Đã duyệt”.
- Bước 4: bỏ vế "Nếu từ chối: hệ thống lưu lý do" vì đã nằm ở luồng thay thế 3a, 3b; thêm câu trỏ tới 3a, 3b.
- Luồng thay thế 3b: "gửi thông báo cho đối phương" sửa thành "cho người dùng" (sao chép nhầm từ use case có hai bên); thêm tên trạng thái “Bị từ chối” và lý do vào phần lưu.
- Ngoại lệ đổi thứ tự từ 1a, 2a, 2b, 2c, 2d thành 1a, 2a, 2b, 3a, 3b: "Yêu cầu đã được xử lý bởi Admin khác" thành 2a, "Người dùng đã bị khóa hoặc xóa" thành 2b, "Mất mạng" thành 3a, "Từ chối nhưng chưa nhập lý do" thành 3b (thêm chú thích "ở luồng thay thế 3b" vì nó xảy ra sau khi nhập lý do ở 3b). Lý do: luật 4.

UC37:

- Ngoại lệ đổi thứ tự từ 1c, 1b, 3a, 4a, 4b, 2a thành 1a, 2a, 3a, 4a, 4b, 4c: "Không có tin chờ" từ 1c thành 1a; "Tin đã bị chủ tin xóa" từ 1b thành 2a; "Mất mạng" từ 2a thành 4c (lúc xác nhận, vì "giữ nguyên trạng thái"). Lý do: luật 4.
- BR3: ngưỡng báo cáo giữ 20 lần. Ngưỡng 10 ở BR5 của UC38 trong bản nháp là bản sao nhầm của BR5 UC33 (nay UC33 ghi 20); dùng 20 ở mọi nơi.
- Quyết định "Xóa tin" đổi thành “Gỡ tin” (mô tả, hậu điều kiện, bước 3, BR2). Lý do: luật 6, trạng thái tin đăng trong tài liệu hệ thống là “ẩn” và “đã gỡ”, không có “đã xóa”; STT 76 cũng ghi "Ẩn, gỡ tin vi phạm". Còn ngoại lệ 2a "Tin đã bị chủ tin xóa" giữ nguyên chữ "xóa" vì đó là thao tác của chủ tin, không phải trạng thái.

UC38:

- Bước 4 của luồng cơ bản ("đưa báo cáo vào hàng chờ Admin... Cảm ơn bạn đã báo cáo") bị bỏ. Lý do: câu của use case người dùng báo cáo vi phạm (UC33), sao chép nhầm.
- Quy tắc nghiệp vụ BR1 đến BR5 của bản nháp bị thay hết. Lý do: cả năm quy tắc nói về báo cáo người dùng và tin đăng (chỉ báo cáo người dùng và tin đăng, một báo cáo chờ mỗi đối tượng, ẩn danh người báo cáo, báo cáo spam, ngưỡng 10 lần), thuộc UC33, không thuộc quản lý từ khóa cấm. Bốn quy tắc mới BR1 đến BR4 lấy từ chính nội dung use case này: so khớp theo chuỗi không phân biệt hoa thường và dấu (STT 78 và mô tả E04), từ khóa không trùng (ngoại lệ 2b), có hiệu lực với nội dung đăng mới (Hậu điều kiện), tin chứa từ khóa được gắn cờ rồi do Admin quyết định ở UC37 (mô tả UC37).
- Thêm luồng thay thế 2b (xóa từ khóa). Lý do: Mô tả nói "xem, thêm, sửa, xóa" nhưng bản nháp không có luồng xóa; màn E04 có nút xóa trên từng dòng.
- Ngoại lệ đánh lại từ 2b, 2c, 2d, 3a thành 2a, 2b, 2c, 3a vì mã ngoại lệ không được nối tiếp chữ cái của luồng thay thế (luật 4).

UC39:

- Bước 1 của luồng cơ bản: “Từ khóa cấm” sửa thành “Danh mục”. Lý do: sao chép nhầm từ UC38; Khởi phát và phản hồi đều là danh mục.
- Ngoại lệ đánh lại từ 2d, 2e, 2f, 3a thành 2a, 2b, 2c, 3a vì mã ngoại lệ không được nối tiếp chữ cái của luồng thay thế (luật 4).

UC40:

- Mô tả, bước 3, BR1, BR3, NFR2: áp dụng quyết định D2 và D7. "điều khoản hợp đồng mini" thành "điều khoản giao dịch"; "trạng thái thanh toán escrow" và "trạng thái escrow" thành "trạng thái khoản ký quỹ" (theo cách gọi ở màn E06); câu "hoàn tiền" và "dữ liệu ngân hàng gần nhất" viết lại thành hành vi của hệ thống.
- Mô tả: thêm vế "việc giải ngân do hệ thống tự thực hiện, Admin không bao giờ giải ngân bằng tay". Lý do: STT 84 và 86; admin chỉ theo dõi.
- BR1: thêm "giải ngân" vào những thứ Admin không làm được, và câu nói khoản ký quỹ do hệ thống tự giải ngân khi người mua xác nhận đã nhận hoặc khi hết đếm ngược.
- BR3: "không hiển thị thông tin tài khoản ngân hàng của người dùng" viết lại thành: thanh toán là mô phỏng, hệ thống không lưu và không hiển thị thông tin ngân hàng (D2).
- NFR2: "khớp với dữ liệu ngân hàng gần nhất" thành "khớp với dữ liệu thanh toán mới nhất của hệ thống" (D2, D9).
- Ngoại lệ bắt đầu từ 2b (không có 2a) nên đánh lại thành 1a (lỗi tải/mất mạng, đứng đầu vì xảy ra từ lúc tải danh sách) và 2a (không có giao dịch khớp). Lý do: luật 4.

UC41:

- Mô tả, Hậu điều kiện 2, bước 1, bước 5, ngoại lệ 5a, BR4 và NFR2: áp dụng quyết định D2 và D7. Bỏ "ngân hàng" ("do hệ thống và ngân hàng thực hiện", "yêu cầu ngân hàng hoàn tiền hoặc giải ngân", "Ngân hàng hoàn tiền/giải ngân thất bại") và viết thành hành vi của hệ thống trên khoản ký quỹ mô phỏng. "escrow" thành "khoản ký quỹ"; "điều khoản" thêm chữ "giao dịch".
- Ngoại lệ 4a cũ ("Ngân hàng hoàn tiền/giải ngân thất bại... giữ khiếu nại ở “Đang thực thi”, thử lại tự động") viết lại thành 5a "Hệ thống không thực thi được quyết định": giữ nguyên khiếu nại, giao dịch và khoản ký quỹ, chưa gửi thông báo, cho Admin xác nhận lại. Lý do: không còn ngân hàng bên ngoài để thử lại; “Đang thực thi” không phải trạng thái khiếu nại trong tài liệu hệ thống (OPEN, NEEDS_EVIDENCE, RESOLVED, WITHDRAWN); các thay đổi được ghi cùng một lô nên không có trạng thái nửa chừng.
- Các lựa chọn quyết định ở bước 3, Mô tả, BR2 và BR4 đổi theo màn D06 và STT 89: bản nháp có “Hoàn tiền cho người mua”, “Giải ngân cho người bán” (bác khiếu nại), “Hủy giao dịch” và “kèm Cảnh cáo”; nay là “Hoàn tiền cho người mua”, “Hoàn trả sản phẩm”, “Hủy giao dịch”, “Bác khiếu nại” (giải ngân cho người bán). Thêm “Hoàn trả sản phẩm” (có ở D06 và STT 89, thiếu ở bản nháp). Bỏ “Cảnh cáo” khỏi quyết định khiếu nại vì D06 và STT 89 không có; thay bằng BR7 trỏ tới UC35. Đây là chỗ leader nên xác nhận, vì BR3 của UC24 (module C) vẫn nhắc "cảnh cáo tài khoản".
- Bước 3: thêm "nhập lý do gửi cho hai bên" (ô “Lời giải thích gửi hai bên” ở D06; bản nháp chỉ ghi "nhập lý do").
- Bước 5 viết rõ hậu quả theo trạng thái giao dịch trong tài liệu hệ thống: hoàn tiền, hoàn trả sản phẩm, hủy giao dịch cùng dẫn tới giao dịch “đã hủy”, khoản ký quỹ hoàn lại người mua, tin về “đang bán”; bác khiếu nại dẫn tới giao dịch “đã nhận”, khoản ký quỹ giải ngân cho người bán. Bỏ vế "tăng số lần cảnh cáo (đủ 3 thì khóa tài khoản)" cùng lý do bỏ “Cảnh cáo” ở trên.
- Thêm luồng thay thế 1b (chọn Mức ưu tiên) và 2b (Ghi chú nội bộ). Lý do: STT 87 và 90, màn D06; bản nháp thiếu.
- Luồng thay thế 2a: "yêu cầu hai bên bổ sung bằng chứng" sửa thành yêu cầu bên khiếu nại bổ sung, thông báo cho bên còn lại, trạng thái khiếu nại gọi là “Chờ bổ sung bằng chứng” và khi có bằng chứng mới thì về “Chờ xử lý”. Lý do: theo D06, D07 và trang-thai.md chỉ bên khiếu nại bổ sung bằng chứng; bản nháp ghi “Chờ bổ sung” và không nói trạng thái sau khi bổ sung.
- BR7 mới: cảnh cáo hoặc khóa tài khoản làm ở UC35 (xem trên).
- Ngoại lệ đổi thứ tự từ 2b, 3a, 4a, 4b, 5a thành 1a, 3a, 4a, 4b, 5a: "Không có khiếu nại chờ xử lý" từ 2b thành 1a; "Khiếu nại đã được Admin khác xử lý" từ 5a thành 4a (lúc nhấn “Xác nhận”); "Mất mạng" từ 4b giữ 4b; ngân hàng thất bại cũ 4a thành 5a (lúc thực thi, bước 5). Lý do: luật 4.

UC42 (viết lại, xem báo cáo của agent):

- Hàng Tên use case: bản nháp ghi "Xử lý khiếu nại", sao chép từ UC41; sửa thành "Quản lý tài khoản admin" theo tiêu đề và danh sách use case cuối.
- Ngoại lệ 2f cũ ("Admin tự vô hiệu hóa chính tài khoản mình" kèm phản hồi "giữ khiếu nại ở “Đang thực thi”, thử lại tự động và ghi lỗi để đối soát") có phản hồi là của UC41. Thay bằng ngoại lệ 2d (Admin tự gỡ quyền của chính mình) với phản hồi đúng: hệ thống từ chối.
- Viết lại cả use case theo yêu cầu của leader: xem danh sách Admin, thêm Admin mới, gỡ quyền Admin, với quy tắc không tự gỡ quyền của chính mình và phải còn ít nhất một Admin. Bỏ các nội dung không còn hợp: tạo tài khoản bằng họ tên, email và mật khẩu tạm, vô hiệu hóa, kích hoạt lại, đặt lại mật khẩu, gửi thông tin đăng nhập qua email, cột "trạng thái" và "lần đăng nhập gần nhất", ngoại lệ mật khẩu tạm sai quy định (2e cũ), NFR1 về băm mật khẩu. Lý do: tài liệu hệ thống định nghĩa Admin là người dùng được cấp quyền, không phải một loại tài khoản riêng; gỡ quyền thay cho vô hiệu hóa, và việc tạo hộ tài khoản đăng nhập bằng mật khẩu tạm không làm được trong khuôn Firebase gói miễn phí (R12, R21).
- "Thêm admin" làm theo cách cấp quyền cho tài khoản đã đăng ký, tìm theo email, có pop up xác nhận. Đây là chỗ chọn của agent vì leader chỉ nói "thêm admin mới"; xem mục chưa giải quyết trong báo cáo.
- Ngoại lệ đánh lại từ 2d đến 2h thành 2a đến 2e và 3a (mất mạng), thêm 2b (email không thuộc tài khoản đang hoạt động) và 2c (đã là Admin) là kiểm tra theo bước 2 mới; ngoại lệ "Admin cuối cùng" (2h cũ) thành 2e và gắn với việc gỡ quyền.
- BR1 đến BR6 và NFR1, NFR2 viết lại theo nội dung mới (BR1 và BR5 giữ ý của tác giả; BR2 từ "không xóa, chỉ vô hiệu hóa" thành "gỡ quyền không xóa tài khoản"; BR3 mới tách từ ngoại lệ 2f; BR4 là BR3 cũ; BR6 là BR5 cũ). NFR2 đổi "mất hiệu lực đăng nhập" thành "mất quyền vào trang quản trị" vì tài khoản vẫn đăng nhập được với tư cách người dùng thường. NFR1 mới nói quyền Admin do máy chủ quyết định (R12).

UC43:

- Khởi phát: thêm chủ ngữ "Admin", bỏ dấu cách đầu ô, viết hoa chữ đầu, đổi dấu ngoặc kép thẳng thành “Đăng nhập”.
- Luồng thay thế: hàng cuối không số bước và không có hành động ("Hệ thống chuyển qua giao diện trang chủ...") được đánh số 2d, ô Hành động để trống.
- Luồng ngoại lệ đổi thứ tự từ 3a, 2d thành 3a, 3b: "Tài khoản chưa được tạo với vai trò admin" thành 3b, kèm chú thích "ở bước 3 hoặc luồng thay thế 2b" vì điều kiện này xảy ra ở cả hai đường đăng nhập. Lý do: luật 4.
- Tiền điều kiện 2: "Đã có tài khoản admin và mật khẩu được cấp" đổi thành "Đã có tài khoản được cấp vai trò Admin". BR1: "do admin cấp vai trò và tạo mới" đổi thành "do Admin cấp vai trò (UC42)". Lý do: sau khi UC42 viết lại, Admin được cấp quyền cho tài khoản đã đăng ký, không còn tạo tài khoản kèm mật khẩu được cấp.
- Luồng thay thế 2b: "đã được cấp tài khoản admin" đổi thành "đã được cấp vai trò admin" cho khớp với hàng phản hồi bên cạnh.

Quyết định leader 2026-10-22 (nhóm E), mỗi dòng một chỗ:

- UC34 bước 2: ô từ khóa “(tên/email/mã người dùng)” sửa thành “(tên hoặc email)”. Theo quyết định leader 2026-10-22 (UC34 giữ ô tìm kiếm theo tên hoặc email); bỏ “mã người dùng” vì quyết định chỉ nêu tên và email.
- UC34 BR2 giữ nguyên: đã đúng quyết định (số CCCD chỉ hiện khi xem yêu cầu xác thực ở UC36, không hiện ở hồ sơ chung). Không sửa chữ.
- UC35 Mô tả, bước 4, BR1: bỏ “đủ 3 lần cảnh cáo thì tài khoản bị khóa”, viết lại theo ba bậc của trang-thai.md mục 7 (bậc 1 chỉ cảnh cáo, tài khoản vẫn hoạt động; bậc 2 khóa tạm; bậc 3 trở lên khóa vĩnh viễn). Theo quyết định leader 2026-10-22.
- UC35 Hậu điều kiện 2: “(cảnh cáo, khóa)” thành “(cảnh cáo theo bậc, khóa)”. Theo quyết định leader 2026-10-22 (ba bậc).
- UC35 bước 2: thêm “chọn thêm thời hạn khóa khi khóa tạm hoặc khi cảnh cáo bậc 2”. Hệ quả của ba bậc: bậc 2 là khóa tạm nên cần thời hạn (`warnUser(id, warning, until)`, E03 có bộ chọn “Thời hạn khóa”). Theo quyết định leader 2026-10-22.
- UC35 BR2: bỏ vế “tin đăng bị ẩn” của tài khoản bị khóa; thay bằng Admin gỡ toàn bộ tin bằng “Gỡ toàn bộ tin” (trang-thai.md mục 1, dòng `removeAllListingsOf`, STT 77: khóa không tự đổi trạng thái tin). Theo quyết định leader 2026-10-22 (điểm 4: gỡ tin là bước nặng hơn, dùng khi khóa tài khoản).
- UC35 BR6: thêm “mở khóa tài khoản không đặt lại số lần cảnh cáo”. Theo quyết định leader 2026-10-22 (trang-thai.md mục 7).
- UC36 bước 2: chi tiết yêu cầu thêm “số CCCD do người gửi khai”, “ảnh giấy tờ, ảnh chân dung cầm giấy tờ” và “số lần đã gửi lại”. Theo quyết định leader 2026-10-22 (điểm 6: `nationalId`, `imageIds`, `resubmitCount`).
- UC36 bước 3: Admin “đối chiếu số CCCD với hai ảnh rồi nhấn “Duyệt””. Theo quyết định leader 2026-10-22 (điểm 6: admin xem số CCCD và hai ảnh rồi duyệt hoặc từ chối).
- UC36 BR2: thêm “tối đa 3 lần” và `resubmitCount`. Theo quyết định leader 2026-10-22 (điểm 6); khớp UC31 BR6.
- UC36 BR3: thêm ảnh giấy tờ, ảnh chân dung cầm giấy tờ vào dữ liệu chỉ Admin xem, và nói rõ Admin chỉ thấy khi mở yêu cầu xác thực, không thấy ở hồ sơ chung. Theo quyết định leader 2026-10-22 (điểm 3 và 6).
- UC36 BR7 mới: yêu cầu xác thực lưu `nationalId`, `imageIds`, `resubmitCount`. Theo quyết định leader 2026-10-22 (điểm 6).
- UC37 BR2: viết lại thành “tin bị Admin Ẩn thì chủ tin không tự hiện lại được, chỉ Admin khôi phục; tin bị Gỡ thì không khôi phục”. Theo quyết định leader 2026-10-22 (điểm 4) và trang-thai.md mục 1 (HIDDEN do admin ẩn, người bán không hiện lại được).
- UC37 BR5 mới: “Gỡ tin” nặng hơn “Ẩn tin”, dùng khi khóa tài khoản (STT 77), không khôi phục được. Theo quyết định leader 2026-10-22 (điểm 4). Bước 3 giữ nguyên cả “Ẩn tin” và “Gỡ tin”.
- UC39 Mô tả, bước 1, bước 2: thêm danh mục con hai cấp (`Category.parentId`, STT 79); chọn “Danh mục cha” khi thêm, để trống là danh mục gốc. Theo quyết định leader 2026-10-22 (điểm 5).
- UC39 luồng thay thế 2c: thêm điều kiện “không còn danh mục con” khi xóa. Theo quyết định leader 2026-10-22 (điểm 5); khớp E05 (không xóa danh mục đang có danh mục con).
- UC39 luồng thay thế 2d mới: cấu hình thuộc tính tình trạng của từng danh mục (`Category.conditionFields`, STT 80). Theo quyết định leader 2026-10-22 (điểm 5). Ngoại lệ 2d mới: tên thuộc tính rỗng hoặc trùng.
- UC39 ngoại lệ 2c: thêm “hoặc đang có danh mục con” và nêu số tin hoặc số danh mục con. Theo quyết định leader 2026-10-22 (điểm 5); khớp E05.
- UC39 BR2: thêm “hoặc đang có danh mục con”. BR6 mới (hai cấp, `Category.parentId`) và BR7 mới (`Category.conditionFields`). Theo quyết định leader 2026-10-22 (điểm 5).
- README giao diện E01: “yêu cầu xác minh” và bộ lọc “Chờ xác minh” thành “yêu cầu xác thực” và “Chờ xác thực” (Thành phần chính, Hành động, Chức năng liên quan). Theo quyết định leader 2026-10-22 (điểm 7: thống nhất “xác thực tài khoản”).
- README giao diện E02: thêm ô tìm kiếm theo tên hoặc email ở Mục đích, Thành phần chính, Hành động, Chức năng liên quan; giữ cả ba bộ lọc; sửa bullet “Khác với ảnh” từ “không làm ô tìm kiếm” thành ô tìm theo tên hoặc email (ảnh vẽ “Tìm theo tên, số điện thoại”, không tìm theo số điện thoại). “Chờ xác minh” thành “Chờ xác thực”. Theo quyết định leader 2026-10-22 (điểm 2 và 7).
- README giao diện E03: bỏ số CCCD khỏi khối “Thông tin định danh” (hồ sơ chung), chuyển vào khối “Yêu cầu xác thực tài khoản” cùng ảnh giấy tờ, ảnh chân dung cầm giấy tờ và số lần gửi lại; thêm bullet “Khác với ảnh” tương ứng. Theo quyết định leader 2026-10-22 (điểm 3 và 6).
- README giao diện E03: dải cảnh báo màu cam và mục “Cảnh báo lần 3” viết lại theo ba bậc (bậc 3 trở lên khóa vĩnh viễn, Admin gỡ toàn bộ tin bằng nút “Gỡ toàn bộ tin”, hệ thống không tự gỡ). Theo quyết định leader 2026-10-22 (điểm 1 và 4).
- README giao diện E03: mọi chữ “xác minh” hiện cho người dùng đọc thành “xác thực tài khoản” hoặc “xác thực” (Mục đích, Đầu trang, tên khối, Hành động, Chức năng liên quan, Trạng thái đặc biệt, Khác với ảnh). Theo quyết định leader 2026-10-22 (điểm 7).
- README giao diện E04: Mục đích, Hành động “Ẩn tin”, Trạng thái đặc biệt và bullet “Khác với ảnh” đầu tiên viết lại cho khớp UC37: ẩn tin thì người bán không tự hiện lại; gỡ tin là bước nặng hơn, không khôi phục, làm bằng “Gỡ toàn bộ tin” ở E03 khi khóa tài khoản. Theo quyết định leader 2026-10-22 (điểm 4).
- README giao diện E04: ba nút lọc trạng thái báo cáo đổi tên theo `ReportStatus` trong enums.md: “Đã ẩn” thành “Đã xử lý (có vi phạm)”, “Đã bỏ qua” thành “Đã xem (không vi phạm)”; nút “Bỏ qua” ghi trạng thái “Đã xem, không vi phạm”; thêm bullet “Khác với ảnh”. Theo quyết định leader 2026-10-22 (điểm 8); “Đã ẩn” không phải trạng thái báo cáo và sai với báo cáo về người dùng, tin nhắn.
- README giao diện E06: chip và nhãn “Đang giao” thành “Đã giao” (`SHIPPED`), chip “Khiếu nại” thành “Đang khiếu nại” (`DISPUTED`), theo `DealStatus` trong enums.md. Theo quyết định leader 2026-10-22 (điểm 8).

Quyết định leader 2026-10-22, đợt hai (nhóm E), mỗi dòng một chỗ:

- UC35 Mô tả, bước 4 và BR2: nói rõ hệ thống chỉ khóa tài khoản theo ba bậc (`Warning.resultingStatus()`), không tự gỡ tin; BR2 ghi việc gỡ tin là thao tác riêng Admin bấm "Gỡ toàn bộ tin" (`AdminRepository.removeAllListingsOf`). Theo quyết định leader 2026-10-22, đợt hai, điểm 1.
- UC35 BR3: "Giao dịch đang dang dở của tài khoản bị khóa sẽ hủy" thay bằng: giao dịch đang chạy vẫn chạy tiếp, hai bên tự hủy hoặc mở khiếu nại như bình thường. Lý do: `trang-thai.md` mục 3 không có chuyển trạng thái nào hủy giao dịch vì khóa tài khoản. Theo quyết định leader 2026-10-22, đợt hai, điểm 4.
- UC36 bước 3 và BR4: bỏ quy tắc "một số CCCD chỉ gắn với một tài khoản xác thực"; thay bằng việc Admin đối chiếu bằng mắt khi duyệt ở E03 và có trách nhiệm này (nếu số đã gắn với tài khoản khác thì từ chối kèm lý do). Giữ nguyên số BR4 nên các BR sau không đổi. Theo quyết định leader 2026-10-22, đợt hai, điểm 5.
- UC37 bước 1, 2, 3 và BR5: danh sách chỉ có "Ẩn tin"; mở một tin ra màn chi tiết `AdminListingDetailFragment` mới có "Gỡ tin", bắt buộc lý do, người bán không khôi phục được (`AdminRepository.removeListing`, `Listing.remove`). BR5 bỏ vế "dùng khi khóa tài khoản", việc gỡ toàn bộ tin đó nay thuộc UC35. Theo quyết định leader 2026-10-22, đợt hai, điểm 2.
- UC37 BR3: bỏ quy tắc "tin bị báo cáo vượt ngưỡng 20 lần được hệ thống tạm ẩn" (gói Spark không có Cloud Functions); thay bằng danh sách sắp theo số lượt báo cáo giảm dần, Admin thấy tin bị báo nhiều nhất trước. Giữ nguyên số BR3. Theo quyết định leader 2026-10-22, đợt hai, điểm 3.
- UC36 NFR1: bỏ câu "Số CCCD được lưu mã hóa" cho khớp UC31 NFR1 đã sửa trước đó; gói Spark không có Cloud Functions để mã hóa phía máy chủ. Thay bằng ràng buộc có thật của Security Rules, giữ nguyên vế ghi log (quyết định leader 2026-10-22, đợt hai).
