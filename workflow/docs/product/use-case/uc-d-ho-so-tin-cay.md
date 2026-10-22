# Đặc tả use case: D. Hồ sơ và tin cậy

Module D gồm năm use case: UC29 Xem hồ sơ của người khác, UC30 Xem hồ sơ uy tín cá nhân, UC31 Đăng ký xác thực người dùng, UC32 Theo dõi người dùng và UC33 Báo cáo vi phạm. Phía Admin của việc xác thực là UC36 Duyệt yêu cầu xác thực người dùng ở module E.

## UC29 Xem hồ sơ của người khác

| Mục | Nội dung |
| --- | --- |
| Mã use case | UC29 |
| Tên use case | Xem hồ sơ của người khác |
| Tác nhân | Người dùng (có tài khoản và đã đăng nhập) |
| Mô tả | Cho phép người dùng đã đăng nhập xem trang hồ sơ công khai của một người dùng khác: thông tin hiển thị, huy hiệu “Tài khoản đã xác thực”, điểm uy tín, đánh giá nhận được và các tin đang đăng, để đánh giá độ tin cậy trước khi giao dịch. |
| Mức ưu tiên | Cao |
| Khởi phát | Người xem nhấn vào ảnh đại diện hoặc tên người dùng ở tin đăng, kết quả tìm kiếm, cuộc trò chuyện hoặc phần đánh giá |
| Tiền điều kiện | 1. Thiết bị có kết nối mạng Internet.<br>2. Ứng dụng đã được cài đặt và người xem đã đăng nhập.<br>3. Tài khoản được xem tồn tại và không bị khóa hoặc xóa. |
| Hậu điều kiện | Hồ sơ của người dùng được hiển thị, dữ liệu không thay đổi. |

**Luồng cơ bản**

| Bước | Hành động của tác nhân | Phản hồi của hệ thống |
| --- | --- | --- |
| 1 | Người xem nhấn vào ảnh đại diện hoặc tên người dùng | Hệ thống hiển thị hồ sơ: ảnh đại diện, tên, huy hiệu “Tài khoản đã xác thực” (nếu đã được duyệt), ngày tham gia, điểm đánh giá trung bình, số giao dịch thành công, số người theo dõi |

**Luồng thay thế**

| Bước | Hành động của tác nhân | Phản hồi của hệ thống |
| --- | --- | --- |
| 1a | Người xem nhấn vào ảnh đại diện hoặc tên của chính mình | Hệ thống mở hồ sơ cá nhân của người xem (UC30) |
| 1b | Người xem nhấn vào một tin đăng trong hồ sơ | Hệ thống mở chi tiết tin đăng; khi quay lại, hồ sơ giữ nguyên vị trí cuộn |

**Luồng ngoại lệ**

| Bước | Hành động của tác nhân | Phản hồi của hệ thống |
| --- | --- | --- |
| 1c | Tài khoản được xem đã bị khóa hoặc xóa | Hệ thống báo “Người dùng không tồn tại hoặc không khả dụng” và quay về màn hình trước |
| 1d | Lỗi tải dữ liệu hoặc mất mạng | Hệ thống báo lỗi và cho phép thử lại |

**Quy tắc nghiệp vụ**

- BR1. Hồ sơ công khai không hiển thị email, số điện thoại, địa chỉ giao nhận và giấy tờ xác thực.
- BR2. Khách chưa đăng nhập không xem được hồ sơ của người khác; mọi thao tác mở hồ sơ đều đưa Khách tới màn hình đăng nhập.
- BR3. Huy hiệu “Tài khoản đã xác thực” chỉ hiển thị sau khi Admin duyệt.
- BR4. Danh sách đánh giá và tin đăng phân trang khi quá dài.

**Yêu cầu phi chức năng**

- NFR1. Hồ sơ tải trong vòng 3 giây ở điều kiện mạng bình thường.
- NFR2. Vị trí cuộn được giữ khi quay lại từ chi tiết tin đăng.

## UC30 Xem hồ sơ uy tín cá nhân

| Mục | Nội dung |
| --- | --- |
| Mã use case | UC30 |
| Tên use case | Xem hồ sơ uy tín cá nhân |
| Tác nhân | Người dùng (có tài khoản và đã đăng nhập) |
| Mô tả | Cho phép người dùng xem hồ sơ của chính mình: điểm uy tín, các đánh giá nhận được, số giao dịch, số người theo dõi và trạng thái xác thực tài khoản. Từ đây người dùng có thể đi tới chỉnh sửa thông tin hoặc đăng ký xác thực. |
| Mức ưu tiên | Trung bình |
| Khởi phát | Người dùng nhấn vào ảnh đại diện hoặc chức năng “Hồ sơ của tôi” |
| Tiền điều kiện | 1. Thiết bị có kết nối mạng Internet.<br>2. Ứng dụng đã được cài đặt và đang ở trạng thái đã đăng nhập. |
| Hậu điều kiện | Hồ sơ cá nhân được hiển thị, dữ liệu không thay đổi. |

**Luồng cơ bản**

| Bước | Hành động của tác nhân | Phản hồi của hệ thống |
| --- | --- | --- |
| 1 | Người dùng nhấn chức năng “Hồ sơ của tôi” | Hệ thống hiển thị tổng quan: ảnh đại diện, tên, huy hiệu “Tài khoản đã xác thực” (nếu đã được duyệt), ngày tham gia, điểm đánh giá trung bình, số giao dịch thành công, số đánh giá, số người theo dõi/đang theo dõi |

**Luồng thay thế**

| Bước | Hành động của tác nhân | Phản hồi của hệ thống |
| --- | --- | --- |
| 1a | Người dùng nhấn chỉnh sửa thông tin | Hệ thống mở màn hình chỉnh sửa thông tin cá nhân (UC05) |
| 1b | Người dùng chưa xác thực hoặc bị từ chối nhấn “Đăng ký xác thực” | Hệ thống mở form đăng ký xác thực (UC31) |

**Luồng ngoại lệ**

| Bước | Hành động của tác nhân | Phản hồi của hệ thống |
| --- | --- | --- |
| 1c | Lỗi tải dữ liệu hoặc mất mạng | Hệ thống báo lỗi và cho phép thử lại |

**Quy tắc nghiệp vụ**

- BR1. Điểm uy tín là điểm trung bình các đánh giá nhận được sau giao dịch hoàn tất (UC26).
- BR2. Chỉ chủ tài khoản thấy trạng thái xác thực chi tiết (đang chờ duyệt, lý do từ chối); người khác chỉ thấy huy hiệu “Tài khoản đã xác thực” khi đã được duyệt.
- BR3. Số lần bị cảnh cáo hiển thị cho chủ tài khoản.

**Yêu cầu phi chức năng**

- NFR1. Hồ sơ tải trong vòng 3 giây.
- NFR2. Điểm và trạng thái xác thực luôn khớp với dữ liệu mới nhất, không dùng dữ liệu cũ quá 5 giây.

## UC31 Đăng ký xác thực người dùng

| Mục | Nội dung |
| --- | --- |
| Mã use case | UC31 |
| Tên use case | Đăng ký xác thực người dùng |
| Tác nhân | Người dùng (có tài khoản, đã đăng nhập, chưa xác thực). Admin xử lý yêu cầu ở UC36 |
| Mô tả | Cho phép người dùng gửi yêu cầu xác thực tài khoản, kèm số CCCD, một ảnh giấy tờ và một ảnh chân dung cầm giấy tờ. Xác thực là điều kiện bắt buộc để mua và bán: chỉ tài khoản đã được Admin duyệt mới được đăng tin, gửi đề nghị và tạo giao dịch. Tài khoản chưa xác thực vẫn được xem, tìm kiếm, nhắn tin và theo dõi bình thường, nhưng không được mua bán. |
| Mức ưu tiên | Cao |
| Khởi phát | Người dùng nhấn “Đăng ký xác thực” ở hồ sơ, hoặc ở thông báo yêu cầu xác thực khi cố đăng tin hoặc giao dịch |
| Tiền điều kiện | 1. Thiết bị có kết nối mạng Internet.<br>2. Ứng dụng đã được cài đặt và đang ở trạng thái đã đăng nhập.<br>3. Tài khoản chưa xác thực (chưa gửi yêu cầu lần nào, hoặc yêu cầu gần nhất đã bị từ chối).<br>4. Không có yêu cầu xác thực nào đang chờ duyệt. |
| Hậu điều kiện | 1. Yêu cầu được lưu với trạng thái “Đang chờ duyệt”.<br>2. Admin thấy yêu cầu trong hàng chờ (UC36).<br>3. Khi được duyệt, tài khoản chuyển sang “Đã xác thực”, được đăng tin, gửi đề nghị và tạo giao dịch, và người dùng nhận thông báo.<br>4. Khi bị từ chối, yêu cầu chuyển sang “Bị từ chối”, người dùng nhận thông báo kèm lý do, tài khoản vẫn chưa được mua bán và được gửi lại yêu cầu trong giới hạn của BR6. |

**Luồng cơ bản**

| Bước | Hành động của tác nhân | Phản hồi của hệ thống |
| --- | --- | --- |
| 1 | Người dùng nhấn “Đăng ký xác thực” | Hệ thống hiển thị hướng dẫn và form xác thực gồm ô nhập số CCCD, ô tải ảnh giấy tờ, ô tải ảnh chân dung cầm giấy tờ và nút “Gửi yêu cầu” |
| 2 | Người dùng nhập số CCCD, chọn ảnh giấy tờ và ảnh chân dung cầm giấy tờ, rồi nhấn “Gửi yêu cầu” | Hệ thống kiểm tra dữ liệu bắt buộc, định dạng và dung lượng tệp ảnh |
| 3 |  | Hệ thống lưu hai ảnh (mục đích `VERIFICATION`) và lưu yêu cầu vào `sellerVerifications` với số CCCD ở `nationalId`, mã hai ảnh ở `imageIds`, trạng thái “Đang chờ duyệt”, rồi thông báo đã gửi thành công. Nếu là lần gửi lại thì `resubmitCount` tăng thêm 1 |
| 4 |  | Hệ thống đưa yêu cầu vào hàng chờ Admin (UC36) |
| 5 |  | Sau khi Admin duyệt, hệ thống cập nhật tài khoản thành “Đã xác thực”, cho phép tài khoản đăng tin, gửi đề nghị và tạo giao dịch, và gửi thông báo cho người dùng |

**Luồng thay thế**

| Bước | Hành động của tác nhân | Phản hồi của hệ thống |
| --- | --- | --- |
| 1a | Người dùng chưa xác thực cố đăng tin, gửi đề nghị hoặc tạo giao dịch | Hệ thống chặn thao tác, báo cần xác thực tài khoản trước khi mua bán và hiển thị lối vào “Đăng ký xác thực”; người dùng nhấn thì tiếp tục từ bước 1 |
| 5a | Admin từ chối yêu cầu và ghi lý do | Hệ thống cập nhật yêu cầu thành “Bị từ chối”, gửi thông báo kèm lý do cho người dùng; tài khoản vẫn chưa được đăng tin, gửi đề nghị hay tạo giao dịch |
| 5b | Người dùng nhấn “Gửi lại yêu cầu” sau khi bị từ chối | Hệ thống hiển thị lại form xác thực; người dùng tiếp tục từ bước 2 |

**Luồng ngoại lệ**

| Bước | Hành động của tác nhân | Phản hồi của hệ thống |
| --- | --- | --- |
| 1b | Tài khoản đã xác thực hoặc đã có yêu cầu đang chờ duyệt | Hệ thống báo trạng thái hiện tại (đã xác thực / đang chờ duyệt) và không cho gửi thêm |
| 1c | Người dùng đã gửi lại đủ 3 lần sau khi bị từ chối (`resubmitCount` bằng 3) | Hệ thống báo đã hết số lần gửi lại, cần liên hệ Admin, và không cho gửi thêm |
| 2a | Thiếu số CCCD, ảnh giấy tờ hoặc ảnh chân dung cầm giấy tờ | Hệ thống báo lỗi và giữ nguyên form |
| 2b | Tệp vượt dung lượng hoặc sai định dạng | Hệ thống từ chối tệp và báo lỗi |
| 2c | Mất mạng | Hệ thống báo lỗi và giữ lại thông tin đã nhập |

**Quy tắc nghiệp vụ**

- BR1. Xác thực là điều kiện bắt buộc để mua và bán: chỉ tài khoản đã được Admin duyệt mới được đăng tin, gửi và trả lời đề nghị, và tham gia giao dịch. Tài khoản chưa xác thực vẫn được xem, tìm kiếm, nhắn tin và theo dõi.
- BR2. Yêu cầu do Admin duyệt thủ công.
- BR3. Mỗi tài khoản chỉ có một yêu cầu “Đang chờ duyệt” tại một thời điểm.
- BR4. Hệ thống không kiểm số CCCD đã dùng cho tài khoản khác; Admin đối chiếu bằng mắt khi duyệt (UC36).
- BR5. Số CCCD chỉ Admin được xem, không hiển thị công khai.
- BR6. Sau khi bị từ chối, người dùng được sửa và gửi lại yêu cầu tối đa 3 lần (`resubmitCount` tối đa 3, tổng tối đa 4 lần gửi cho mỗi tài khoản). Hết 3 lần gửi lại thì không gửi thêm được và phải liên hệ Admin.
- BR7. Mỗi yêu cầu gồm hai ảnh bắt buộc: một ảnh giấy tờ và một ảnh chân dung cầm giấy tờ. Chỉ nhận ảnh, ảnh được nén trên thiết bị và lưu dạng Base64 trong Firestore.

**Yêu cầu phi chức năng**

- NFR1. Số CCCD và hai ảnh của yêu cầu chỉ chủ tài khoản và Admin đọc được, do Security Rules chặn (`workflow/docs/data/security-rules.md` mục 3 và mục 6). Dự án dùng gói Firebase Spark, không có Cloud Functions nên không mã hóa thêm ở phía máy chủ; đường truyền đã là HTTPS.
- NFR2. Thông báo kết quả đến người dùng trong vòng 5 giây sau khi Admin xử lý.
- NFR3. Dữ liệu form giữ nguyên khi xoay màn hình hoặc thoát ứng dụng tạm thời.

## UC32 Theo dõi người dùng

| Mục | Nội dung |
| --- | --- |
| Mã use case | UC32 |
| Tên use case | Theo dõi người dùng |
| Tác nhân | Người dùng (có tài khoản và đã đăng nhập) |
| Mô tả | Cho phép người dùng theo dõi hoặc bỏ theo dõi một người dùng khác. Tin đăng của những người đang theo dõi sẽ xuất hiện ở “Kênh tin người đã theo dõi”. |
| Mức ưu tiên | Trung bình |
| Khởi phát | Người dùng nhấn “Theo dõi” ở hồ sơ của người khác; hoặc nhấn “Đang theo dõi” để bỏ theo dõi |
| Tiền điều kiện | 1. Thiết bị có kết nối mạng Internet.<br>2. Ứng dụng đã được cài đặt và đang ở trạng thái đã đăng nhập.<br>3. Tài khoản được theo dõi không phải chính mình và không bị khóa hoặc xóa.<br>4. Người dùng chưa theo dõi tài khoản này (riêng luồng bỏ theo dõi 1b thì đang theo dõi). |
| Hậu điều kiện | 1. Quan hệ theo dõi được lưu.<br>2. Số người theo dõi của đối phương tăng thêm 1.<br>3. Tin đăng của đối phương xuất hiện ở kênh tin người đã theo dõi.<br>4. Với luồng bỏ theo dõi: quan hệ theo dõi bị gỡ, số người theo dõi của đối phương giảm 1 và tin đăng của đối phương không còn ở kênh tin người đã theo dõi. |

**Luồng cơ bản**

| Bước | Hành động của tác nhân | Phản hồi của hệ thống |
| --- | --- | --- |
| 1 | Người dùng nhấn “Theo dõi” tại hồ sơ cá nhân của người dùng khác | Hệ thống lưu quan hệ theo dõi |
| 2 |  | Hệ thống đổi nút thành “Đang theo dõi” và cập nhật số người theo dõi |

**Luồng thay thế**

| Bước | Hành động của tác nhân | Phản hồi của hệ thống |
| --- | --- | --- |
| 1a | Người dùng nhấn “Theo dõi” trên tin đăng | Hệ thống đổi nút thành “Đang theo dõi” và cập nhật số người theo dõi |
| 1b | Người dùng đang theo dõi nhấn “Đang theo dõi” để bỏ theo dõi | Hệ thống gỡ quan hệ theo dõi, đổi nút về “Theo dõi” và cập nhật số người theo dõi |

**Luồng ngoại lệ**

| Bước | Hành động của tác nhân | Phản hồi của hệ thống |
| --- | --- | --- |
| 1c | Đối phương đã bị khóa hoặc xóa tài khoản | Hệ thống báo “Không thể theo dõi người dùng này” |
| 1d | Dữ liệu lệch: người dùng đã theo dõi từ thiết bị khác | Hệ thống cập nhật lại nút thành “Đang theo dõi”, không tạo bản ghi trùng |
| 1e | Mất mạng | Hệ thống báo lỗi, nút trở về trạng thái trước đó |

**Quy tắc nghiệp vụ**

- BR1. Không được tự theo dõi chính mình.
- BR2. Theo dõi là một chiều, không cần đối phương đồng ý.
- BR3. Mỗi cặp người dùng chỉ có tối đa một quan hệ theo dõi.
- BR4. Khi tài khoản bị khóa hoặc xóa, quan hệ theo dõi liên quan bị gỡ.

**Yêu cầu phi chức năng**

- NFR1. Trạng thái nút và số người theo dõi cập nhật trong vòng 2 giây.

## UC33 Báo cáo vi phạm

| Mục | Nội dung |
| --- | --- |
| Mã use case | UC33 |
| Tên use case | Báo cáo vi phạm |
| Tác nhân | Người dùng (có tài khoản và đã đăng nhập) |
| Mô tả | Cho phép người dùng báo cáo một người dùng khác (từ hồ sơ), một tin đăng (từ chi tiết tin) hoặc một tin nhắn (từ cuộc trò chuyện) vi phạm quy định. Báo cáo được chuyển tới Admin xử lý. |
| Mức ưu tiên | Trung bình |
| Khởi phát | Người dùng nhấn “Báo cáo” ở hồ sơ người dùng, ở chi tiết tin đăng hoặc ở một tin nhắn trong cuộc trò chuyện |
| Tiền điều kiện | 1. Thiết bị có kết nối mạng Internet.<br>2. Ứng dụng đã được cài đặt và đang ở trạng thái đã đăng nhập.<br>3. Đối tượng bị báo cáo (người dùng, tin đăng hoặc tin nhắn) tồn tại và không phải của chính người báo cáo.<br>4. Người dùng chưa có báo cáo “Chờ xử lý” nào cho đối tượng này. |
| Hậu điều kiện | 1. Báo cáo được lưu với trạng thái “Chờ xử lý”.<br>2. Admin thấy báo cáo trong hàng chờ. |

**Luồng cơ bản**

| Bước | Hành động của tác nhân | Phản hồi của hệ thống |
| --- | --- | --- |
| 1 | Người dùng nhấn “Báo cáo” | Hệ thống hiển thị form báo cáo: danh sách lý do, ô mô tả, đính kèm ảnh (tùy chọn, tối đa 3 ảnh) |
| 2 | Người dùng chọn lý do, nhập mô tả và nhấn “Gửi báo cáo” | Hệ thống kiểm tra dữ liệu bắt buộc và dung lượng tệp |
| 3 |  | Hệ thống lưu ảnh đính kèm (nếu có, mục đích `EVIDENCE`) và lưu báo cáo, trạng thái “Chờ xử lý”, với mã các ảnh ở `reports.evidenceImageIds`; nếu đối tượng là tin nhắn thì lưu thêm mã cuộc trò chuyện ở `reports.conversationId` để Admin mở được đoạn chat bị báo cáo |
| 4 |  | Hệ thống đưa báo cáo vào hàng chờ Admin và hiển thị thông báo “Cảm ơn bạn đã báo cáo” |

**Luồng thay thế**

Không có

**Luồng ngoại lệ**

| Bước | Hành động của tác nhân | Phản hồi của hệ thống |
| --- | --- | --- |
| 1a | Đối tượng bị báo cáo đã bị xóa hoặc ẩn | Hệ thống báo “Nội dung không còn khả dụng” |
| 1b | Người dùng đã có báo cáo đang chờ xử lý cho đối tượng này | Hệ thống báo đã ghi nhận trước đó, không cho gửi thêm |
| 2a | Chưa chọn lý do hoặc thiếu mô tả bắt buộc | Hệ thống báo lỗi và giữ nguyên form |
| 2b | Tệp vượt dung lượng, sai định dạng hoặc đính kèm quá 3 ảnh | Hệ thống từ chối tệp |
| 2c | Mất mạng | Hệ thống giữ lại nội dung đã nhập và cho thử lại |

**Quy tắc nghiệp vụ**

- BR1. Báo cáo được người dùng, tin đăng và tin nhắn; chưa hỗ trợ báo cáo đánh giá (ghi vào future work).
- BR2. Mỗi người dùng chỉ có một báo cáo đang chờ xử lý cho mỗi đối tượng.
- BR3. Danh tính người báo cáo không hiển thị cho người bị báo cáo.
- BR4. Báo cáo spam có thể bị cảnh cáo.
- BR5. Hệ thống không tự ẩn tin theo số lượt báo cáo; danh sách báo cáo ở màn kiểm duyệt của Admin sắp theo số lượt báo cáo giảm dần để Admin thấy tin bị báo nhiều nhất trước (UC37).
- BR6. `reports.evidenceImageIds` là ảnh đính kèm tùy chọn, tối đa 3 ảnh, mục đích `EVIDENCE`; `reports.conversationId` chỉ điền khi đối tượng báo cáo là tin nhắn.

**Yêu cầu phi chức năng**

- NFR1. Phản hồi kết quả gửi báo cáo trong vòng 3 giây.
- NFR2. Nội dung và bằng chứng báo cáo chỉ Admin xem được.
- NFR3. Dữ liệu form giữ nguyên khi xoay màn hình.

## Ghi chú sửa đổi

Các chỗ dưới đây đã sửa so với bản nháp `local/use-case-md/uc-d-ho-so-tin-cay.md`. Phần không nhắc tới giữ nguyên chữ của tác giả.

**Chung**

- Dấu nháy thẳng ở UC32 luồng thay thế 1a ("theo dõi") và UC33 ngoại lệ 1a ("Nội dung không còn khả dụng") đổi thành dấu nháy cong cho thống nhất với phần còn lại.
- Bỏ phần "Ghi chú chuyển đổi" của bản nháp (đó là ghi chú của bước chuyển tệp, không thuộc đặc tả); các lỗi nêu trong đó được xử lý ở dưới.
- Các ô Mức ưu tiên, Quy tắc nghiệp vụ và Yêu cầu phi chức năng của năm use case đều đã có nội dung trong bản nháp, nên không điền thêm.

**UC29**

- Tác nhân: bỏ "Khách (người dùng đã đăng nhập thừa hưởng nhờ quan hệ kế thừa)", thay bằng "Người dùng (có tài khoản và đã đăng nhập)". Theo D5: Khách không xem được hồ sơ ai. Sơ đồ đã đổi theo.
- Mô tả: thêm "đã đăng nhập" sau "người dùng" cho khớp D5.
- Tiền điều kiện 2: "đã được cài đặt (đã đăng nhập hoặc chưa)" đổi thành "đã được cài đặt và người xem đã đăng nhập". Theo D5.
- BR2: bản nháp viết "Khách được xem hồ sơ nhưng không nhắn tin, theo dõi hoặc báo cáo được". Trái D5 nên viết lại: Khách không xem được hồ sơ của người khác, mọi thao tác mở hồ sơ đều đưa Khách tới màn hình đăng nhập.
- Luồng cơ bản bước 1: "ảnh đại diện/tên người dùng" đổi thành "ảnh đại diện hoặc tên người dùng" (viết rõ, không dùng dấu gạch chéo).
- Lỗi đánh số: luồng ngoại lệ bản nháp chỉ có 1c và 1d, không có 1a và 1b. Sửa bằng cách điền hai luồng thay thế 1a và 1b (xem dưới); nhờ vậy 1c và 1d giữ nguyên mã và dãy a, b, c, d liền nhau.
- Luồng thay thế bản nháp để trống. Thêm 1a (người xem nhấn vào chính mình thì mở hồ sơ cá nhân UC30) và 1b (nhấn một tin đăng trong hồ sơ thì mở chi tiết tin, khi quay lại giữ vị trí cuộn). 1b suy ra từ NFR2 và BR4 của chính bản nháp; 1a suy ra từ khác biệt giữa hồ sơ của mình và của người khác ở tài liệu giao diện D02.

**UC30**

- Tác nhân: "Người dùng ( có tài khoản và đã đăng nhập)" thừa dấu cách sau dấu mở ngoặc; sửa thành "Người dùng (có tài khoản và đã đăng nhập)".
- Luồng cơ bản bước 1: "tổng quan:ảnh đại diện" thiếu dấu cách, sửa thành "tổng quan: ảnh đại diện".
- Luồng ngoại lệ: trong nguồn, khối này nằm dưới ô tiêu đề để trống. Giữ nó là luồng ngoại lệ như các use case cùng nhóm; cần leader xác nhận.
- Luồng thay thế bản nháp để trống. Thêm 1a (mở chỉnh sửa thông tin cá nhân, UC05) và 1b (mở form đăng ký xác thực, UC31), vì mô tả của chính bản nháp nói "Từ đây người dùng có thể đi tới chỉnh sửa thông tin hoặc đăng ký xác thực".
- Ngoại lệ lỗi tải đổi từ 1a thành 1c vì 1a và 1b đã dùng cho luồng thay thế.
- Tham chiếu UC26 ở BR1: đã kiểm với danh sách cuối, UC26 là "Đánh giá đối tác", đúng với ý "đánh giá sau giao dịch". Giữ nguyên.
- Tham chiếu mới tới UC05 (Quản lý thông tin cá nhân) lấy theo bảng D11 của QUYET-DINH.md; chưa đối chiếu được với tệp module A cuối cùng.
- BR3 và NFR2 thiếu dấu chấm cuối câu; đã thêm.

**UC31 (thay đổi lớn nhất của module)**

- Theo D6, xác thực không còn là huy hiệu mà là điều kiện bắt buộc để mua và bán. Mô tả viết lại: bỏ "để nhận huy hiệu", nêu rõ chỉ tài khoản đã được Admin duyệt mới đăng tin, gửi đề nghị, tạo giao dịch; tài khoản chưa xác thực vẫn xem, tìm kiếm, nhắn tin, theo dõi nhưng không mua bán.
- Hậu điều kiện 3 thêm các quyền được mở khi duyệt; thêm hậu điều kiện 4 cho trường hợp bị từ chối.
- Tiền điều kiện 3: "Tài khoản chưa xác thực" bổ sung "(chưa gửi yêu cầu lần nào, hoặc yêu cầu gần nhất đã bị từ chối)" để luồng gửi lại hợp lệ.
- Luồng cơ bản bước 5: bản nháp "gắn huy hiệu" đổi thành "cho phép tài khoản đăng tin, gửi đề nghị và tạo giao dịch". Theo D6.
- BR1: gộp hai ý theo D6 (xác thực bắt buộc để mua bán; chưa xác thực vẫn xem, tìm kiếm, nhắn tin, theo dõi). Bản nháp viết "đăng tin, gửi/trả lời đề nghị và giao dịch"; giữ ý, bỏ dấu gạch chéo.
- Tham chiếu UC38 ở tác nhân, hậu điều kiện 2 và bước 4: sai với danh sách cuối (UC38 là "Quản lý từ khóa cấm"). Đổi thành UC36 Duyệt yêu cầu xác thực người dùng.
- Luồng "từ chối kèm lý do rồi gửi lại" mà bản nháp chỉ nhắc ở BR6 nay có chỗ trong luồng: thêm luồng thay thế 5a (Admin từ chối kèm lý do) và 5b (người dùng gửi lại, quay về bước 2). Luồng thay thế bản nháp để trống.
- Thêm luồng thay thế 1a: người dùng chưa xác thực cố đăng tin, gửi đề nghị hoặc tạo giao dịch thì bị chặn và được dẫn tới đăng ký xác thực. Đây là chỗ cái cổng của D6 hiện ra trong luồng, và khớp với ô Khởi phát của bản nháp ("thông báo yêu cầu xác thực khi cố đăng tin/giao dịch"). Bản nháp có sẵn ngoại lệ 1b mà thiếu 1a, nên 1a lấp đúng chỗ khuyết.
- BR6 không nêu con số. Tôi chọn tối đa 3 lần gửi lại (tổng 4 lần gửi cho mỗi tài khoản). Con số này do tôi tự chọn, chưa có trong tài liệu nào. Thêm ngoại lệ 1c (đã hết số lần gửi lại thì báo và không cho gửi thêm) để BR6 có chỗ áp dụng.
- Thống nhất "số CCCD": bản nháp viết "cccd" chữ thường ở luồng cơ bản bước 2 và BR4, "Số cccd" ở BR5 và NFR1, còn ngoại lệ 2c dùng từ "Giấy tờ". Tất cả đổi thành "số CCCD". Riêng 2c: "Giấy tờ đã được dùng để xác thực tài khoản khác" đổi thành "Số CCCD đã được dùng để xác thực tài khoản khác" và phản hồi "báo số CCCD đã được sử dụng".
- NFR1: "NFR1.Số cccd" thiếu dấu cách sau "NFR1."; đã thêm.
- Bước 2 phản hồi: câu "cccd được dùng để đăng ký xác thực tài khoản khác chưa" sửa cho trọn câu: "kiểm tra số CCCD đã được dùng để đăng ký xác thực tài khoản khác chưa".
- Ngoại lệ 2a: thêm "số CCCD" vào danh sách thứ bắt buộc, vì bước 2 bắt nhập số CCCD mà bản nháp bỏ sót ở đây.
- Mô tả: bản nháp không nhắc ảnh giấy tờ, chỉ có "ảnh chân dung" ở bước 2; mô tả viết "số CCCD và ảnh giấy tờ" theo yêu cầu của leader. Hai chỗ này chưa khớp nhau, cần leader chốt form gồm những ảnh nào.
- Khởi phát: "đăng tin/giao dịch" đổi thành "đăng tin hoặc giao dịch".

**UC32**

- Mô tả nhắc "bỏ theo dõi" nhưng bản nháp không có luồng bỏ theo dõi. Thêm luồng thay thế 1b (nhấn "Đang theo dõi" để bỏ theo dõi: gỡ quan hệ, đổi nút về "Theo dõi", cập nhật số người theo dõi). Cập nhật Khởi phát, Tiền điều kiện 4 và thêm Hậu điều kiện 4 cho luồng này. Theo tài liệu giao diện D03: "bấm lại để bỏ theo dõi".
- Luồng thay thế 1a: bản nháp có xuống dòng giữa câu ("trên tin" rồi "đăng"; "cập nhật" rồi "số người theo dõi"), bộ chuyển đổi để lại `<br>`. Đã nối thành câu liền.
- Ngoại lệ đổi mã từ 1b, 1c, 1d thành 1c, 1d, 1e vì luồng thay thế 1b mới chiếm mã 1b.
- Tiền điều kiện đã yêu cầu đăng nhập, nên khớp D5 (Khách không theo dõi được); không cần sửa thêm.

**UC33**

- Luồng thay thế bản nháp để trống. Cả hai đối tượng (người dùng, tin đăng) đã đi chung luồng cơ bản và không thấy nhánh nào bắt buộc phải có, nên giữ "Không có".
- BR5: "vượt ngưỡng lần (20)" thiếu chữ, sửa thành "vượt ngưỡng 20 lần".
- Tiền điều kiện đã yêu cầu đăng nhập, khớp D5 (Khách không báo cáo được).
- Bằng chứng chỉ là ảnh, đúng D4; bản nháp đã không nhắc video.

**Áp quyết định của leader ngày 2026-10-22 (đợt nhóm D)**

UC29
- Luồng cơ bản bước 1: "huy hiệu xác thực" đổi thành huy hiệu “Tài khoản đã xác thực” (nếu đã được duyệt), thống nhất cách gọi "xác thực tài khoản" (quyết định leader 2026-10-22, điểm 6).

UC30
- Luồng cơ bản bước 1: "huy hiệu xác thực" đổi thành huy hiệu “Tài khoản đã xác thực” (nếu đã được duyệt) (quyết định leader 2026-10-22, điểm 6).
- BR2: "chờ duyệt" đổi thành "đang chờ duyệt" cho đúng tên trạng thái của `VerificationStatus.PENDING`; "huy hiệu" ghi rõ là “Tài khoản đã xác thực” (quyết định leader 2026-10-22, điểm 6 và 7).

UC31
- Mô tả: "số CCCD và ảnh giấy tờ" đổi thành "số CCCD, một ảnh giấy tờ và một ảnh chân dung cầm giấy tờ"; chốt form gửi hai ảnh, hết chỗ lúc nói "ảnh chân dung", lúc nói "ảnh giấy tờ" (quyết định leader 2026-10-22, điểm 1).
- Luồng cơ bản bước 1: form xác thực nay gồm ô nhập số CCCD, ô tải ảnh giấy tờ, ô tải ảnh chân dung cầm giấy tờ và nút “Gửi yêu cầu” (quyết định leader 2026-10-22, điểm 1 và 4).
- Luồng cơ bản bước 2: bỏ "họ tên" và "ảnh chân dung"; người dùng nhập số CCCD và chọn hai ảnh (ảnh giấy tờ, ảnh chân dung cầm giấy tờ). Bỏ "họ tên" vì form do leader chốt (điểm 4) chỉ có số CCCD, hai ảnh và nút gửi, và lược đồ không có trường họ tên; cần leader xác nhận (quyết định leader 2026-10-22, điểm 1 và 4).
- Luồng cơ bản bước 3: ghi rõ hai ảnh lưu với mục đích `VERIFICATION`, yêu cầu lưu vào `sellerVerifications` với `nationalId` (số CCCD), `imageIds` (mã hai ảnh), và `resubmitCount` tăng thêm 1 ở lần gửi lại (quyết định leader 2026-10-22, điểm 1).
- Trạng thái “Chờ duyệt” đổi thành “Đang chờ duyệt” ở hậu điều kiện 1, bước 3 và BR3, theo tên trạng thái của `VerificationStatus.PENDING` (quyết định leader 2026-10-22, điểm 7).
- Ngoại lệ 1c: "đủ số lần cho phép" đổi thành "đủ 3 lần" (`resubmitCount` bằng 3), báo cần liên hệ Admin (quyết định leader 2026-10-22, điểm 2).
- Ngoại lệ 2a: thiếu số CCCD, ảnh giấy tờ hoặc ảnh chân dung cầm giấy tờ (bỏ "họ tên", "ảnh bắt buộc" nói rõ là hai ảnh) (quyết định leader 2026-10-22, điểm 1).
- BR6: chốt số lần gửi lại là 3, ghi bằng `resubmitCount`, hết thì phải liên hệ Admin. Con số 3 trước đây do người soạn tự chọn, nay là quyết định của leader (quyết định leader 2026-10-22, điểm 2).
- BR7 (mới): mỗi yêu cầu gồm hai ảnh bắt buộc, chỉ nhận ảnh, nén trên thiết bị và lưu Base64 trong Firestore (quyết định leader 2026-10-22, điểm 1; nền D3).

UC33
- Mô tả và Khởi phát: thêm tin nhắn (từ cuộc trò chuyện) vào các đối tượng báo cáo được, cạnh người dùng và tin đăng (quyết định leader 2026-10-22, điểm 3).
- Tiền điều kiện 3: đối tượng bị báo cáo là "người dùng, tin đăng hoặc tin nhắn" (quyết định leader 2026-10-22, điểm 3).
- BR1: bỏ "chưa hỗ trợ báo cáo tin nhắn", viết lại là báo cáo được người dùng, tin đăng và tin nhắn; báo cáo đánh giá vẫn chưa hỗ trợ (quyết định leader 2026-10-22, điểm 3).
- UC31 NFR1: bỏ câu "Số CCCD được lưu mã hóa" vì gói Spark không có Cloud Functions để mã hóa phía máy chủ. Thay bằng ràng buộc có thật: Security Rules chỉ cho chủ tài khoản và Admin đọc (quyết định leader 2026-10-22).

**Áp quyết định của leader 2026-10-22, đợt hai (nhóm D)**

UC31
- Bước 2 phản hồi: bỏ vế "kiểm tra số CCCD đã được dùng để đăng ký xác thực tài khoản khác chưa". Không làm được: `sellerVerifications` chỉ chủ tài khoản và Admin đọc, Security Rules không ép được số CCCD duy nhất (quyết định leader 2026-10-22, đợt hai, điểm 5).
- Ngoại lệ 2c (số CCCD đã dùng cho tài khoản khác) bỏ; ngoại lệ "Mất mạng" từ 2d thành 2c, không còn tham chiếu nào tới mã 2d (quyết định leader 2026-10-22, đợt hai, điểm 5).
- BR4 "Một số CCCD chỉ gắn với một tài khoản" thay bằng: hệ thống không kiểm số CCCD trùng, Admin đối chiếu bằng mắt khi duyệt (UC36). Giữ nguyên số BR4 nên BR5 đến BR7 và tham chiếu "BR6" không đổi (quyết định leader 2026-10-22, đợt hai, điểm 5).

UC32
- Bỏ bước 3 của luồng cơ bản (thông báo "có người theo dõi mới" cho đối phương), vì `NotificationType` không có giá trị tương ứng và nhóm không thêm loại thông báo mới. Đó là bước cuối nên không phải đánh số lại, và không còn chỗ nào khác nhắc tới bước này (quyết định leader 2026-10-22, đợt hai, điểm 6).

UC33
- Bước 1: đính kèm ảnh ghi rõ tối đa 3 ảnh. Bước 3: ghi ảnh lưu mục đích `EVIDENCE`, mã ảnh ở `reports.evidenceImageIds`, và `reports.conversationId` chỉ điền khi đối tượng là tin nhắn. Ngoại lệ 2b thêm trường hợp quá 3 ảnh. BR6 mới nêu hai trường này (quyết định leader 2026-10-22, đợt hai, lược đồ `reports` đã thêm hai trường).
- BR5 "tin bị báo cáo vượt ngưỡng 20 lần thì hệ thống tạm ẩn" thay bằng: hệ thống không tự ẩn tin, danh sách báo cáo của Admin sắp theo số lượt báo cáo giảm dần. Lý do: gói Spark không có Cloud Functions nên không ai chạy được quy tắc này (quyết định leader 2026-10-22, đợt hai, điểm 3).
