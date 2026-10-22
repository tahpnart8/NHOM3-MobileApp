# Đặc tả use case: C. Chat và giao dịch

Module C gồm 11 use case từ UC18 đến UC28, theo đúng vòng đời giao dịch: nhắn tin, gửi đề nghị, trả lời đề nghị, xác nhận điều khoản giao dịch, thanh toán, xác nhận đã giao, xác nhận đã nhận, hủy, khiếu nại, đánh giá và xem lịch sử. Trạng thái và chuyển trạng thái theo `workflow/docs/architecture/trang-thai.md` mục 2, 3 và 4; màn hình theo `workflow/docs/ui/c-ket-noi-giao-dich/README.md` (C01 đến C07).

Chuỗi nghiệp vụ chính: người mua gửi đề nghị (UC20), người bán chấp nhận (UC27), hệ thống tạo giao dịch ở trạng thái chờ xác nhận và đưa tin đăng sang đang giữ chỗ, hai bên xác nhận điều khoản (UC25), người mua thanh toán (UC21), người bán xác nhận đã giao kèm vận đơn (UC28), người mua xác nhận đã nhận hoặc hệ thống tự giải ngân khi hết đếm ngược (UC22), hai bên đánh giá nhau (UC26). Hủy (UC23) chỉ được trước khi giao hàng. Khiếu nại (UC24) chỉ trong thời gian đếm ngược. Nhắn tin (UC18) và xem lịch sử (UC19) đi cùng suốt chuỗi.

## UC18 Nhắn tin

| Mục | Nội dung |
| --- | --- |
| Mã use case | UC18 |
| Tên use case | Nhắn tin |
| Tác nhân | Người dùng (có tài khoản và đã đăng nhập) |
| Mô tả | Cho phép người dùng nhắn tin trực tiếp với người dùng khác thông qua màn hình trò chuyện, thường xuất phát từ một tin đăng, để hỏi thông tin và thương lượng. |
| Mức ưu tiên | Cao |
| Khởi phát | Người dùng nhấn tab Tin nhắn ở thanh điều hướng dưới |
| Tiền điều kiện | 1. Thiết bị có kết nối mạng Internet.<br>2. Ứng dụng đã được cài đặt và đang ở trạng thái đã đăng nhập.<br>3. Tài khoản đối phương không bị khóa hoặc xóa. |
| Hậu điều kiện | 1. Tin nhắn được lưu vào cuộc trò chuyện.<br>2. Đối phương nhận được thông báo tin nhắn mới. |

**Luồng cơ bản**

| Bước | Hành động của tác nhân | Phản hồi của hệ thống |
| --- | --- | --- |
| 1 | Người dùng nhấn tab Tin nhắn ở thanh điều hướng dưới | Hệ thống mở màn hình danh sách các cuộc trò chuyện (gắn với tin đăng) |
| 2 | Người dùng nhấn vào cuộc trò chuyện cụ thể | Hệ thống hiển thị lịch sử cuộc trò chuyện, màn hình cuộc trò chuyện |
| 3 | Người dùng nhập tin nhắn vào ô nhập tin nhắn và nhấn gửi | Hệ thống kiểm tra tin (không phải chuỗi rỗng), lưu lại tin nhắn người dùng nhập, hiển thị tin nhắn vừa gửi |
| 4 | Đối phương mở cuộc trò chuyện | Hệ thống hiển thị tin nhắn và đánh dấu "Đã xem" |

**Luồng thay thế**

| Bước | Hành động của tác nhân | Phản hồi của hệ thống |
| --- | --- | --- |
| 1a | Người dùng mở cuộc trò chuyện thông qua thông báo đề nghị giá cả vừa nhận/vừa được chấp nhận | Hệ thống chuyển màn hình từ màn hình thông báo qua màn hình cuộc trò chuyện, người dùng tiếp tục từ bước 3 |
| 3a | Người dùng gửi ảnh thay cho văn bản | Hệ thống lưu ảnh vào cuộc trò chuyện và hiển thị cho đối phương |
| 3b | Người dùng gửi đề nghị giá, hoặc người bán chấp nhận hay từ chối đề nghị ngay trong cuộc trò chuyện | Hệ thống xử lý theo UC20 (gửi đề nghị) hoặc UC27 (trả lời đề nghị) và hiển thị thẻ đề nghị trong cuộc trò chuyện |

**Luồng ngoại lệ**

| Bước | Hành động của tác nhân | Phản hồi của hệ thống |
| --- | --- | --- |
| 3c | Nhập tin nhắn là chuỗi rỗng | Nút gửi tin nhắn bị vô hiệu |
| 3d | Mất kết nối mạng | Hệ thống thông báo "Gửi thất bại" và cho phép gửi lại |
| 3e | Đối phương bị khóa/xóa tài khoản | Hệ thống hiển thị thông báo không thể nhắn tin cho người dùng này |
| 3f | Đề nghị giá bị từ chối | Thẻ đề nghị hiện "đã từ chối"; người dùng vẫn nhắn tin được, muốn đổi giá thì gửi đề nghị mới (UC20) sau khi đề nghị cũ đã được trả lời |

**Quy tắc nghiệp vụ**

- BR1. Mỗi cuộc trò chuyện gắn với một tin đăng; mỗi người mua chỉ có một cuộc trò chuyện cho mỗi tin.
- BR2. Không được tự gửi đề nghị để mở cuộc trò chuyện với chính mình.
- BR3. Giới hạn độ dài tin nhắn và dung lượng ảnh: ảnh được nén trên thiết bị, cạnh dài tối đa 1024 px và tối đa 300 KB trước khi mã hóa.
- BR4. Chỉ gửi được tin nhắn chữ và ảnh; không gửi video (phát triển sau).
- BR5. Tài khoản chưa được admin duyệt xác thực vẫn nhắn tin được.

**Yêu cầu phi chức năng**

- NFR1. Tin nhắn đến người nhận trong vòng 2 giây ở điều kiện mạng bình thường.
- NFR2. Nội dung chat giữ nguyên khi xoay màn hình hoặc thoát ứng dụng tạm thời.
- NFR3. Khi mất mạng, danh sách cuộc trò chuyện đã tải trước đó vẫn xem được, kèm dải báo đang xem ngoại tuyến.

## UC19 Xem lịch sử giao dịch

| Mục | Nội dung |
| --- | --- |
| Mã use case | UC19 |
| Tên use case | Xem lịch sử giao dịch |
| Tác nhân | Người dùng (đã đăng nhập) |
| Mô tả | Cho phép người dùng xem danh sách và chi tiết các giao dịch mình đã tham gia, cả với vai trò người mua và người bán; cũng xem được lịch sử thanh toán của mình. |
| Mức ưu tiên | Trung bình |
| Khởi phát | Người dùng nhấn “Lịch sử giao dịch” |
| Tiền điều kiện | 1. Thiết bị có kết nối mạng Internet.<br>2. Ứng dụng đã được cài đặt và đang ở trạng thái đã đăng nhập. |
| Hậu điều kiện | 1. Danh sách hoặc chi tiết giao dịch (hoặc danh sách thanh toán) được hiển thị, dữ liệu không thay đổi. |

**Luồng cơ bản**

| Bước | Hành động của tác nhân | Phản hồi của hệ thống |
| --- | --- | --- |
| 1 | Người dùng nhấn “Lịch sử giao dịch” | Hệ thống hiển thị danh sách giao dịch, mới nhất trước (món đồ, đối tác, loại, trạng thái, ngày) |
| 2 | Người dùng chọn bộ lọc (trạng thái, loại giao dịch, vai trò đã mua/đã bán) | Hệ thống cập nhật danh sách theo bộ lọc |
| 3 | Người dùng chọn một giao dịch | Hệ thống hiển thị chi tiết giao dịch |

**Luồng thay thế**

| Bước | Hành động của tác nhân | Phản hồi của hệ thống |
| --- | --- | --- |
| 1a | Người dùng chuyển sang thẻ “Thanh toán” để xem lịch sử thanh toán | Hệ thống hiển thị danh sách các khoản thanh toán của người dùng, mới nhất trước: tên món đồ, số tiền, trạng thái (đang giữ, đã giải ngân, đã hoàn), thời điểm giữ và thời điểm giải ngân hoặc hoàn, vai trò (đã trả hoặc đã nhận) |
| 1b | Người dùng chọn một khoản thanh toán | Hệ thống hiển thị chi tiết giao dịch của khoản thanh toán đó |
| 1c | Chưa có giao dịch | Hệ thống hiển thị “Bạn chưa có giao dịch nào” |
| 1d | Chưa có khoản thanh toán nào | Hệ thống hiển thị “Bạn chưa có thanh toán nào” |

**Luồng ngoại lệ**

| Bước | Hành động của tác nhân | Phản hồi của hệ thống |
| --- | --- | --- |
| 1e | Lỗi tải dữ liệu | Hệ thống báo lỗi và cho thử lại. |

**Quy tắc nghiệp vụ**

- BR1. Người dùng chỉ xem được giao dịch và thanh toán của chính mình.
- BR2. Danh sách phân trang khi quá dài.

**Yêu cầu phi chức năng**

- NFR1. Danh sách tải trong vòng 3 giây.
- NFR2. Vị trí cuộn được giữ khi quay lại từ chi tiết.

## UC20 Gửi đề nghị

| Mục | Nội dung |
| --- | --- |
| Mã use case | UC20 |
| Tên use case | Gửi đề nghị |
| Tác nhân | Người dùng (vai trò người mua, đã đăng nhập và tài khoản đã được admin duyệt xác thực) |
| Mô tả | Cho phép người mua đề xuất mức giá mong muốn cho một tin đăng. Người bán nhận thông báo và phản hồi bằng cách chấp nhận đề nghị hoặc từ chối |
| Mức ưu tiên | Cao |
| Khởi phát | Người mua nhấn "Đề nghị" ở trên tin đăng |
| Tiền điều kiện | 1. Thiết bị có kết nối mạng Internet.<br>2. Ứng dụng đã được cài đặt và đang ở trạng thái đã đăng nhập; tài khoản đã được admin duyệt xác thực.<br>3. Tin đăng đang ở trạng thái đang bán.<br>4. Người gửi không phải chủ tin đăng. |
| Hậu điều kiện | 1. Đề nghị được lưu với trạng thái “chờ người bán trả lời”.<br>2. Người bán nhận được thông báo. |

**Luồng cơ bản**

| Bước | Hành động của tác nhân | Phản hồi của hệ thống |
| --- | --- | --- |
| 1 | Người mua nhấn "Đề nghị" | Hệ thống kiểm tra trạng thái tin đăng (còn tồn tại và đang bán hay không), sau đó hiển thị ô nhập giá đề nghị |
| 2 | Người mua chọn loại đề nghị trong các loại người bán nhận (tiền, vật trao đổi, hoặc cả hai) và nhập giá vào ô nhập giá đề nghị dưới dạng chuỗi số (kèm mô tả vật đổi nếu có) | |
| 3 | Người mua nhấn "Gửi đề nghị" để xác nhận yêu cầu đề nghị giá cả | Hệ thống kiểm tra dữ liệu nhập vào (không phải chuỗi rỗng, con số phải nằm trong khoảng cho phép của người đăng tin), lưu lại giá đề nghị của người mua, gửi thông báo tới cho người bán |
| 4 | | Hệ thống lưu trạng thái đề nghị là "chờ người bán trả lời" và hiển thị đề nghị trong cuộc trò chuyện với người bán (tạo cuộc trò chuyện nếu chưa có) |

**Luồng thay thế**

| Bước | Hành động của tác nhân | Phản hồi của hệ thống |
| --- | --- | --- |
| 1a | Người mua nhấn “Quay lại” | Hệ thống trở về chi tiết tin đăng, không lưu gì. |

**Luồng ngoại lệ**

| Bước | Hành động của tác nhân | Phản hồi của hệ thống |
| --- | --- | --- |
| 1b | Tin đã bị ẩn hoặc không còn đang bán (đang giữ chỗ, đã bán, đã gỡ), kể cả khi tin đổi trạng thái giữa lúc người mua đang nhập | Hệ thống báo tin không còn khả dụng / thông báo đề nghị hết hạn |
| 1c | Người mua đã có đề nghị “chờ người bán trả lời” trên tin này | Hệ thống không cho phép gửi thêm đề nghị. |
| 3a | Giá ngoài khoảng cho phép | Hệ thống báo giá không hợp lệ và không gửi, không tiết lộ khoảng giá. |
| 3b | Giá không phải số hợp lệ | Hệ thống báo lỗi định dạng. |

**Quy tắc nghiệp vụ**

- BR1. Mức giá tối thiểu do người bán đặt khi đăng tin và được ẩn với người mua.
- BR2. Chỉ gửi được đề nghị khi giá nằm trong khoảng cho phép (không thấp hơn mức tối thiểu của người bán).
- BR3. Một người mua chỉ có một đề nghị đang chờ phản hồi trên mỗi tin
- BR4. Người bán muốn ra giá ngược lại thì thương lượng qua chat
- BR5. Người mua không hủy được đề nghị đã gửi và không có đề xuất lại; muốn đổi giá thì đợi người bán từ chối rồi gửi đề nghị mới.

**Yêu cầu phi chức năng**

- NFR1. Phản hồi kết quả gửi trong vòng 3 giây.
- NFR2. Khoảng giá của người bán không được lộ trong dữ liệu trả về phía người mua.

## UC21 Thanh toán cho giao dịch

| Mục | Nội dung |
| --- | --- |
| Mã use case | UC21 |
| Tên use case | Thanh toán cho giao dịch |
| Tác nhân | Người dùng (vai trò người mua, đã đăng nhập và tài khoản đã được admin duyệt xác thực) |
| Mô tả | Cho phép người mua thanh toán cho giao dịch mua bán sau khi hai bên đã xác nhận điều khoản. Màn hình làm giống ứng dụng thanh toán thật (chọn phương thức, màn xác nhận, biên lai) nhưng chỉ là mô phỏng: không gọi dịch vụ thanh toán bên ngoài và không có tiền thật. Người mua chọn một trong ba phương thức mô phỏng: thẻ ngân hàng, ví điện tử hoặc chuyển khoản; phương thức đã chọn được lưu trong khoản thanh toán. Hệ thống giữ khoản tiền bằng một khoản thanh toán ở trạng thái đang giữ cho tới khi giải ngân cho người bán hoặc hoàn lại cho người mua. Giao dịch trao đổi hoặc cho tặng không có thanh toán. |
| Mức ưu tiên | Cao |
| Khởi phát | Người mua nhấn "Thanh toán" trong chi tiết giao dịch đã xác nhận (hoặc được hệ thống chuyển sang ngay sau khi hai bên xác nhận điều khoản ở UC25) |
| Tiền điều kiện | 1. Thiết bị có kết nối mạng Internet.<br>2. Ứng dụng đã được cài đặt và đang ở trạng thái đã đăng nhập; tài khoản đã được admin duyệt xác thực.<br>3. Người thực hiện là người mua của giao dịch.<br>4. Giao dịch ở trạng thái đã xác nhận, loại mua bán, giá thống nhất lớn hơn 0.<br>5. Giao dịch chưa có khoản thanh toán. |
| Hậu điều kiện | 1. Khoản thanh toán được tạo với trạng thái “đang giữ”, số tiền bằng giá thống nhất của giao dịch, phương thức là phương thức người mua đã chọn.<br>2. Giao dịch vẫn ở trạng thái đã xác nhận.<br>3. Người bán nhận được thông báo có khoản thanh toán đang được giữ.<br>4. Người mua có biên lai thanh toán. |

**Luồng cơ bản**

| Bước | Hành động của tác nhân | Phản hồi của hệ thống |
| --- | --- | --- |
| 1 | Người mua mở chi tiết giao dịch đã xác nhận và nhấn "Thanh toán" | Hệ thống hiển thị màn hình thanh toán: tên món đồ, người bán, số tiền phải trả (bằng giá thống nhất, không sửa được) và danh sách ba phương thức thanh toán mô phỏng: thẻ ngân hàng, ví điện tử, chuyển khoản |
| 2 | Người mua chọn một trong ba phương thức thanh toán (thẻ ngân hàng, ví điện tử hoặc chuyển khoản) và nhấn "Tiếp tục" | Hệ thống hiển thị màn hình xác nhận: tóm tắt giao dịch, số tiền, phương thức đã chọn, lưu ý đây là thanh toán mô phỏng và tiền được giữ an toàn tới khi người mua nhận hàng |
| 3 | Người mua nhấn "Xác nhận thanh toán" | Hệ thống khóa nút xác nhận trong lúc xử lý và kiểm tra lại: người thực hiện là người mua, giao dịch vẫn ở trạng thái đã xác nhận, chưa có khoản thanh toán |
| 4 | | Hệ thống tạo khoản thanh toán “đang giữ” với số tiền bằng giá thống nhất và phương thức đã chọn, ghi thời điểm giữ, và gửi thông báo cho người bán |
| 5 | | Hệ thống hiển thị biên lai (số tiền, phương thức, thời điểm, trạng thái “đang giữ”) rồi trở về chi tiết giao dịch, nơi nút "Thanh toán" không còn |

**Luồng thay thế**

| Bước | Hành động của tác nhân | Phản hồi của hệ thống |
| --- | --- | --- |
| 1a | Người mua nhấn “Quay lại” ở màn hình thanh toán | Hệ thống trở về chi tiết giao dịch, không tạo khoản thanh toán. |
| 2a | Người mua nhấn “Đổi phương thức” ở màn hình xác nhận | Hệ thống trở về màn hình chọn phương thức, người mua tiếp tục từ bước 2. |
| 2b | Người mua nhấn “Hủy” ở màn hình xác nhận | Hệ thống trở về chi tiết giao dịch, không tạo khoản thanh toán. |

**Luồng ngoại lệ**

| Bước | Hành động của tác nhân | Phản hồi của hệ thống |
| --- | --- | --- |
| 1b | Giao dịch là trao đổi hoặc cho tặng (giá bằng 0) | Hệ thống không hiển thị nút "Thanh toán" và không có khoản thanh toán nào. |
| 2c | Người mua chưa chọn phương thức nào | Hệ thống giữ nút "Tiếp tục" chưa bấm được. |
| 3a | Giao dịch không còn ở trạng thái đã xác nhận (đã bị hủy trong lúc người mua đang thanh toán) | Hệ thống báo không thể thanh toán và không tạo khoản thanh toán. |
| 3b | Giao dịch đã có khoản thanh toán (bấm trùng hoặc đã thanh toán trên thiết bị khác) | Hệ thống báo giao dịch đã được thanh toán và hiển thị khoản thanh toán hiện có, không tạo thêm. |
| 3c | Mất mạng hoặc ghi dữ liệu lỗi | Hệ thống báo lỗi, không tạo khoản thanh toán, cho phép người mua thử lại. |

**Quy tắc nghiệp vụ**

- BR1. Thanh toán là mô phỏng: không gọi dịch vụ thanh toán bên ngoài, không có tiền thật, không có số dư ví. Tiền được giữ bằng một khoản thanh toán trạng thái “đang giữ” trong dữ liệu của hệ thống.
- BR2. Số tiền thanh toán bằng giá thống nhất của giao dịch; người mua không sửa được.
- BR3. Mỗi giao dịch có tối đa một khoản thanh toán; chỉ người mua của giao dịch được thanh toán, và chỉ khi giao dịch đã xác nhận.
- BR4. Khoản thanh toán “đang giữ” chuyển sang “đã giải ngân” cho người bán khi giao dịch sang đã nhận (UC22), và sang “đã hoàn” cho người mua khi giao dịch bị hủy (UC23) hoặc admin quyết định hoàn tiền sau khiếu nại (UC24). Người dùng không tự rút khoản đang giữ.
- BR5. Với giao dịch gửi vận chuyển, người bán chỉ xác nhận đã giao (UC28) sau khi người mua đã thanh toán.
- BR6. Phương thức thanh toán là một trong ba giá trị: thẻ ngân hàng (`CARD`), ví điện tử (`E_WALLET`), chuyển khoản (`BANK_TRANSFER`). Người mua bắt buộc chọn một; phương thức được lưu trong khoản thanh toán (`payments.method`) để hiện trên biên lai. Đây chỉ là nhãn của thanh toán mô phỏng: không dẫn tới dịch vụ bên ngoài, không làm thay đổi số tiền hay trạng thái của khoản thanh toán.

**Yêu cầu phi chức năng**

- NFR1. Kết quả thanh toán hiển thị trong vòng 3 giây ở điều kiện mạng bình thường.
- NFR2. Nút "Xác nhận thanh toán" bị khóa trong lúc xử lý để không tạo khoản thanh toán trùng.
- NFR3. Lựa chọn trên màn hình thanh toán giữ nguyên khi xoay màn hình.

## UC22 Xác nhận đã nhận hàng

| Mục | Nội dung |
| --- | --- |
| Mã use case | UC22 |
| Tên use case | Xác nhận đã nhận hàng |
| Tác nhân | Người dùng (vai trò người mua, đã đăng nhập và tài khoản đã được admin duyệt xác thực) |
| Mô tả | Cho phép người mua xác nhận đã nhận món đồ kèm một ảnh món đồ đã nhận; hệ thống giải ngân khoản tiền đang giữ (nếu có) cho người bán và mở việc đánh giá cho hai bên |
| Mức ưu tiên | Cao |
| Khởi phát | Người mua nhấn "Xác nhận đã nhận hàng" trong chi tiết giao dịch |
| Tiền điều kiện | 1. Thiết bị có kết nối mạng Internet.<br>2. Ứng dụng đã được cài đặt và đang ở trạng thái đã đăng nhập; tài khoản đã được admin duyệt xác thực.<br>3. Người thực hiện là người mua.<br>4. Giao dịch ở trạng thái đã giao (gửi vận chuyển), hoặc đã xác nhận (gặp trực tiếp); nếu là mua bán thì đã thanh toán (khoản thanh toán đang giữ).<br>5. Giao dịch không bị hủy hoặc đang tranh chấp. |
| Hậu điều kiện | 1. Hệ thống ghi nhận xác nhận nhận hàng và lưu ảnh nhận hàng, giao dịch chuyển sang đã nhận.<br>2. Khoản thanh toán (nếu có) chuyển từ đang giữ sang đã giải ngân cho người bán.<br>3. Hai bên được phép đánh giá nhau trên hệ thống. |

**Luồng cơ bản**

| Bước | Hành động của tác nhân | Phản hồi của hệ thống |
| --- | --- | --- |
| 1 | Người mua mở chi tiết giao dịch và nhấn "Xác nhận đã nhận hàng" | Hệ thống kiểm tra giao dịch đủ điều kiện và nhắc người mua: xác nhận không thể thu hồi, sau khi xác nhận không khiếu nại được nữa, đồng thời yêu cầu chụp hoặc chọn một ảnh món đồ đã nhận |
| 2 | Người mua chụp hoặc chọn một ảnh món đồ đã nhận và nhấn "Xác nhận" | Hệ thống kiểm tra đã có ảnh (nút "Xác nhận" chưa bấm được khi chưa có ảnh), nén ảnh trên thiết bị (cạnh dài tối đa 1024 px, tối đa 300 KB trước khi mã hóa), lưu ảnh nhận hàng, chuyển giao dịch sang đã nhận và ghi thời điểm nhận |
| 3 | | Trong cùng lần ghi, hệ thống chuyển khoản thanh toán từ đang giữ sang đã giải ngân cho người bán (giao dịch trao đổi hoặc cho tặng không có khoản này) |
| 4 | | Hệ thống thông báo cho người bán và người mua, đồng thời mở chức năng đánh giá đối tác cho hai bên (UC26) |

**Luồng thay thế**

| Bước | Hành động của tác nhân | Phản hồi của hệ thống |
| --- | --- | --- |
| 2a | Người mua nhấn “Quay lại” | Hệ thống trở về chi tiết giao dịch, trạng thái giữ nguyên |
| 2b | Người mua không xác nhận trong bảy ngày kể từ lúc người bán xác nhận đã giao, và không có khiếu nại | Hệ thống tự giải ngân khi hết đếm ngược: khi một trong hai bên mở ứng dụng hoặc khi tiến trình nền chạy, hệ thống chuyển giao dịch sang đã nhận với người đổi là hệ thống (không có ảnh nhận hàng), chuyển khoản thanh toán sang đã giải ngân cho người bán, thông báo cho hai bên, rồi tiếp tục như bước 4 |

**Luồng ngoại lệ**

| Bước | Hành động của tác nhân | Phản hồi của hệ thống |
| --- | --- | --- |
| 1a | Giao dịch gửi vận chuyển mà người bán chưa xác nhận đã giao | Hệ thống không cho xác nhận nhận hàng; giao dịch giữ trạng thái đã xác nhận, người mua chờ người bán xác nhận đã giao (UC28). |
| 1b | Giao dịch đang tranh chấp hoặc đã hủy | Hệ thống báo không thể xác nhận nhận hàng. |
| 2c | Mất mạng | Hệ thống báo lỗi và giữ nguyên trạng thái. |
| 2d | Ảnh nhận hàng không đọc được hoặc vượt giới hạn dung lượng | Hệ thống từ chối ảnh và yêu cầu chọn ảnh khác. |

**Quy tắc nghiệp vụ**

- BR1. Tiền chỉ được giải ngân khi giao dịch sang đã nhận: người mua xác nhận, hoặc hết đếm ngược bảy ngày.
- BR2. Xác nhận không thể thu hồi; sau khi đã nhận thì không khiếu nại được nữa.
- BR3. Nếu người mua không xác nhận sau 7 ngày kể từ lúc người bán xác nhận giao (và không có khiếu nại) thì số tiền sẽ tự động giải ngân cho người bán.
- BR4. Giao dịch gặp trực tiếp không có bước người bán xác nhận giao và không có đếm ngược; người mua xác nhận ngay khi giao dịch đã xác nhận (và đã thanh toán nếu có giá).
- BR5. Giao dịch chuyển sang hoàn tất khi hai bên đã đánh giá hoặc sau ba ngày kể từ lúc đã nhận (xem UC26).
- BR6. Người mua xác nhận đã nhận hàng bắt buộc kèm một ảnh món đồ đã nhận (một ảnh, cả giao dịch gửi vận chuyển lẫn gặp trực tiếp). Ảnh lưu trong kho ảnh và giao dịch trỏ tới ảnh đó (`deals.receivedImageId`, mục đích ảnh `RECEIVED`). Khi hệ thống tự giải ngân lúc hết đếm ngược thì giao dịch không có ảnh nhận hàng.

**Yêu cầu phi chức năng**

- NFR1. Khoản thanh toán chuyển sang đã giải ngân trong vòng 5 giây sau khi giao dịch sang đã nhận.
- NFR2. Mọi lần đổi trạng thái giao dịch và khoản thanh toán được ghi lại cùng thời điểm và người thực hiện để đối soát.
- NFR3. Ảnh nhận hàng được nén trên thiết bị trước khi lưu.

## UC23 Hủy giao dịch

| Mục | Nội dung |
| --- | --- |
| Mã use case | UC23 |
| Tên use case | Hủy giao dịch |
| Tác nhân | Người dùng (một trong hai bên của giao dịch, đã đăng nhập) |
| Mô tả | Cho phép một bên hủy giao dịch trước khi giao hàng (khi giao dịch đang chờ xác nhận hoặc đã xác nhận), bắt buộc nêu lý do. Khoản thanh toán đang giữ (nếu có) được hoàn lại cho người mua. |
| Mức ưu tiên | Trung bình |
| Khởi phát | Người dùng nhấn “Hủy giao dịch” trong khung chat hoặc chi tiết giao dịch |
| Tiền điều kiện | 1. Thiết bị có kết nối mạng Internet.<br>2. Ứng dụng đã được cài đặt và đang ở trạng thái đã đăng nhập; không đòi tài khoản đã được admin duyệt xác thực.<br>3. Người thực hiện là một trong hai bên của giao dịch.<br>4. Giao dịch đang ở trạng thái chờ xác nhận hoặc đã xác nhận (người bán chưa giao hàng, người mua chưa xác nhận đã nhận). |
| Hậu điều kiện | 1. Giao dịch chuyển sang “đã hủy”, kèm lý do hủy.<br>2. Khoản thanh toán đang giữ (nếu có) chuyển sang “đã hoàn” cho người mua.<br>3. Tin đăng quay về trạng thái đang bán.<br>4. Bên còn lại nhận được thông báo. |

**Luồng cơ bản**

| Bước | Hành động của tác nhân | Phản hồi của hệ thống |
| --- | --- | --- |
| 1 | Người dùng nhấn “Hủy giao dịch” bên trong màn hình trò chuyện hoặc chi tiết giao dịch | Hệ thống hiển thị pop up xác nhận, có ô lý do (bắt buộc) |
| 2 | Người dùng nhập lý do và nhấn “Xác nhận hủy” | Hệ thống đổi trạng thái giao dịch thành “đã hủy” và ghi lý do |
| 3 | | Trong cùng lần ghi, hệ thống chuyển khoản thanh toán đang giữ (nếu có) sang đã hoàn cho người mua và đưa tin đăng về đang bán |
| 4 | | Hệ thống gửi thông báo cho bên còn lại |

**Luồng thay thế**

| Bước | Hành động của tác nhân | Phản hồi của hệ thống |
| --- | --- | --- |
| 2a | Người dùng nhấn “Quay lại” | Hệ thống giữ nguyên trạng thái |
| 2b | Giao dịch ở trạng thái chờ xác nhận quá ba ngày kể từ lúc tạo mà hai bên chưa xác nhận đủ | Hệ thống tự hủy giao dịch với người đổi là hệ thống, đưa tin đăng về đang bán và thông báo cho hai bên |

**Luồng ngoại lệ**

| Bước | Hành động của tác nhân | Phản hồi của hệ thống |
| --- | --- | --- |
| 2c | Người dùng không nhập lý do | Hệ thống báo lý do là bắt buộc và không hủy |
| 2d | Giao dịch đã giao hoặc đang tranh chấp | Hệ thống không cho hủy và hướng dẫn dùng “Khiếu nại giao dịch” (UC24) khi còn trong thời gian đếm ngược |
| 2e | Giao dịch đã nhận, hoàn tất hoặc đã hủy | Hệ thống báo không thể hủy. |
| 2f | Mất mạng | Hệ thống báo lỗi và giữ nguyên trạng thái. |

**Quy tắc nghiệp vụ**

- BR1. Chỉ hủy được trước khi giao hàng, khi giao dịch đang chờ xác nhận hoặc đã xác nhận. Nếu người mua đã thanh toán thì khoản đang giữ được hoàn lại.
- BR2. Từ khi giao dịch đã giao, mọi vấn đề xử lý qua khiếu nại.
- BR3. Hủy giao dịch không ảnh hưởng điểm uy tín
- BR4. Lý do hủy là bắt buộc.
- BR5. Hủy giao dịch không đòi tài khoản đã được admin duyệt xác thực: ai đã vào được giao dịch thì phải thoát ra được.

**Yêu cầu phi chức năng**

- NFR1. Trạng thái hủy cập nhật cho bên kia trong vòng 3 giây.

## UC24 Khiếu nại giao dịch

| Mục | Nội dung |
| --- | --- |
| Mã use case | UC24 |
| Tên use case | Khiếu nại giao dịch |
| Tác nhân | Người dùng (bên khiếu nại: người mua hoặc người bán, đã đăng nhập) |
| Mô tả | Cho phép một bên gửi khiếu nại kèm bằng chứng khi có tranh chấp trong giao dịch đã có hiệu lực. Khiếu nại được chuyển tới Admin xử lý. |
| Mức ưu tiên | Cao |
| Khởi phát | Người dùng nhấn “Khiếu nại giao dịch” trong chi tiết giao dịch |
| Tiền điều kiện | 1. Thiết bị có kết nối mạng Internet.<br>2. Ứng dụng đã được cài đặt và đang ở trạng thái đã đăng nhập; không đòi tài khoản đã được admin duyệt xác thực.<br>3. Người thực hiện là một trong hai bên của giao dịch.<br>4. Giao dịch đang ở trạng thái đã giao và còn trong thời gian đếm ngược bảy ngày, hoặc đã xác nhận khi gặp trực tiếp.<br>5. Giao dịch chưa từng có khiếu nại. |
| Hậu điều kiện | 1. Khiếu nại được lưu, trạng thái “chờ xử lý”.<br>2. Giao dịch chuyển sang tranh chấp, đếm ngược dừng, tiền không được giải ngân.<br>3. Bên còn lại nhận được thông báo.<br>4. Admin thấy khiếu nại trong hàng chờ. |

**Luồng cơ bản**

| Bước | Hành động của tác nhân | Phản hồi của hệ thống |
| --- | --- | --- |
| 1 | Người dùng nhấn “Khiếu nại giao dịch” | Hệ thống hiển thị form khiếu nại (lý do chọn sẵn, mô tả, ảnh bằng chứng) |
| 2 | Người dùng chọn lý do, nhập mô tả, đính kèm ít nhất một ảnh bằng chứng (tối đa 5 ảnh) và nhấn “Gửi” | Hệ thống kiểm tra dữ liệu bắt buộc, số lượng ảnh và dung lượng ảnh |
| 3 | | Hệ thống lưu khiếu nại, chuyển giao dịch sang tranh chấp và dừng đếm ngược |
| 4 | | Hệ thống thông báo cho bên còn lại và đưa khiếu nại vào hàng chờ Admin |

**Luồng thay thế**

| Bước | Hành động của tác nhân | Phản hồi của hệ thống |
| --- | --- | --- |
| 1a | Người dùng nhấn “Quay lại” | Hệ thống không lưu gì. |

**Luồng ngoại lệ**

| Bước | Hành động của tác nhân | Phản hồi của hệ thống |
| --- | --- | --- |
| 2a | Thiếu lý do, thiếu mô tả hoặc chưa đính kèm ảnh nào | Hệ thống báo lỗi. |
| 2b | Ảnh vượt dung lượng hoặc sai định dạng | Hệ thống từ chối ảnh. |
| 2c | Giao dịch đã có khiếu nại (đang xử lý, đã xử lý hoặc đã rút) | Hệ thống không cho gửi thêm. |
| 2d | Đã hết đếm ngược bảy ngày, hoặc giao dịch đã nhận, hoàn tất hoặc đã hủy | Hệ thống báo không còn khiếu nại được. |
| 2e | Mất mạng | Hệ thống lưu lại những thông tin đã nhập |

**Quy tắc nghiệp vụ**

- BR1. Mỗi giao dịch chỉ có một khiếu nại; rút đơn là cuối cùng, không khiếu nại lại được.
- BR2. Khi có khiếu nại, khoản thanh toán đang giữ được giữ nguyên cho tới khi có quyết định.
- BR3. Kết quả xử lý do Admin quyết định: hoàn tiền cho người mua, trả hàng rồi hoàn tiền, hủy giao dịch, bác khiếu nại (giải ngân cho người bán), hoặc yêu cầu bổ sung bằng chứng.
- BR4. Hệ thống thực thi quyết định tự động, Admin không can thiệp trực tiếp vào tiền.
- BR5. Bằng chứng chỉ gồm ảnh, tối thiểu 1 ảnh và tối đa 5 ảnh; không gửi video (phát triển sau).
- BR6. Khiếu nại không đòi tài khoản đã được admin duyệt xác thực: ai đã vào được giao dịch thì phải thoát ra được.
- BR7. Cảnh cáo không phải là quyết định khiếu nại; admin cảnh cáo tài khoản bằng chức năng cảnh cáo ở module quản trị (UC35).

**Yêu cầu phi chức năng**

- NFR1. Bằng chứng tải lên được lưu an toàn, chỉ người liên quan và Admin xem được.
- NFR2. Dữ liệu form giữ nguyên khi xoay màn hình.

## UC25 Xác nhận điều khoản giao dịch

| Mục | Nội dung |
| --- | --- |
| Mã use case | UC25 |
| Tên use case | Xác nhận điều khoản giao dịch |
| Tác nhân | Người dùng (2 bên của giao dịch: người bán, người mua; đã đăng nhập và tài khoản đã được admin duyệt xác thực) |
| Mô tả | Cho phép hai bên xem điều khoản của giao dịch (loại giao dịch mua bán/trao đổi/tặng, thông tin món đồ, giá, cách giao nhận), sửa điều khoản khi cần và cùng xác nhận. Cả hai bên cùng xác nhận chung với điều khoản của hệ thống thì giao dịch có hiệu lực. |
| Mức ưu tiên | Cao |
| Khởi phát | Người dùng mở chi tiết giao dịch đang chờ xác nhận (người bán được chuyển tới ngay sau khi chấp nhận đề nghị ở UC27; người mua mở từ thông báo hoặc từ cuộc trò chuyện) |
| Tiền điều kiện | 1. Thiết bị có kết nối mạng Internet.<br>2. Ứng dụng đã được cài đặt và đang ở trạng thái đã đăng nhập; tài khoản đã được admin duyệt xác thực.<br>3. Người thực hiện là một trong hai bên của giao dịch.<br>4. Giao dịch đang ở trạng thái chờ xác nhận; tin đăng của giao dịch đang ở trạng thái đang giữ chỗ. |
| Hậu điều kiện | 1. Giao dịch chuyển sang “đã xác nhận”: mua bán chờ người mua thanh toán (UC21), trao đổi hoặc tặng thì hai bên giao nhận, không có thanh toán.<br>2. Điều khoản bị khóa, không sửa được nữa.<br>3. Tin đăng vẫn ở trạng thái đang giữ chỗ (không nhận đề nghị mới); quay lại đang bán nếu giao dịch bị hủy.<br>4. Hai bên nhận được thông báo. |

**Luồng cơ bản**

| Bước | Hành động của tác nhân | Phản hồi của hệ thống |
| --- | --- | --- |
| 1 | Người dùng mở chi tiết giao dịch đang chờ xác nhận | Hệ thống hiển thị điều khoản giao dịch (điền sẵn thông tin từ tin đăng và đề nghị đã chấp nhận) và trạng thái xác nhận của từng bên |
| 2 | Một bên nhấn “Sửa điều khoản”, chọn/chỉnh giá (nếu mua bán), hình thức giao nhận (gặp trực tiếp hoặc gửi vận chuyển), địa điểm hoặc địa chỉ giao nhận, thời gian hẹn, điều kiện khác, rồi nhấn “Lưu” | Hệ thống kiểm tra dữ liệu bắt buộc, lưu điều khoản, xóa xác nhận của cả hai bên và thông báo cho bên còn lại |
| 3 | Một bên xem nội dung và nhấn “Xác nhận” | Hệ thống kiểm tra điều khoản đã đủ, ghi nhận xác nhận của bên này và thông báo cho bên còn lại |
| 4 | Bên còn lại xem nội dung và nhấn “Xác nhận” | Khi cả hai đã xác nhận: hệ thống chuyển giao dịch sang đã xác nhận, khóa nội dung điều khoản, giữ tin đăng ở trạng thái đang giữ chỗ và thông báo cho hai bên |
| 5 | | Nếu là mua bán, hệ thống chuyển người mua sang bước thanh toán (UC21) |

**Luồng thay thế**

| Bước | Hành động của tác nhân | Phản hồi của hệ thống |
| --- | --- | --- |
| 3a | Bên còn lại nhấn “Sửa điều khoản” (yêu cầu chỉnh sửa) thay vì xác nhận | Hệ thống xóa xác nhận của cả hai bên; hai bên sửa rồi xác nhận lại từ bước 2. |
| 4a | Giao dịch ở trạng thái chờ xác nhận quá ba ngày kể từ lúc tạo mà hai bên chưa xác nhận đủ | Hệ thống tự hủy giao dịch với người đổi là hệ thống, đưa tin đăng về đang bán và thông báo cho hai bên. |

**Luồng ngoại lệ**

| Bước | Hành động của tác nhân | Phản hồi của hệ thống |
| --- | --- | --- |
| 3b | Một bên nhấn “Hủy” | Chuyển sang UC23 (Hủy giao dịch) |
| 3c | Thiếu thông tin bắt buộc (chưa chọn hình thức giao nhận, hoặc thiếu địa chỉ khi gửi vận chuyển) hoặc giá không hợp lệ | Hệ thống báo lỗi, giữ nguyên form. |
| 3d | Giao dịch đã bị hủy hoặc hết hạn xác nhận | Hệ thống báo giao dịch không còn hiệu lực và không ghi nhận xác nhận. |
| 4b | Mất mạng | Hệ thống giữ nguyên trạng thái và cho thử lại. |

**Quy tắc nghiệp vụ**

- BR1. Giao dịch chỉ có hiệu lực khi cả hai bên xác nhận.
- BR2. Sau khi cả hai xác nhận, điều khoản không được chỉnh sửa.
- BR3. Mỗi tin đăng chỉ có một giao dịch hoạt động tại một thời điểm.
- BR4. Giao dịch tặng và trao đổi không có bước thanh toán.
- BR5. Tin đăng ở trạng thái đang giữ chỗ từ lúc người bán chấp nhận đề nghị cho tới khi giao dịch hoàn tất (tin chuyển sang đã bán), và quay lại đang bán khi giao dịch bị hủy.
- BR6. Sửa điều khoản xóa xác nhận của cả hai bên; hai bên phải xác nhận lại.

**Yêu cầu phi chức năng**

- NFR1. Trạng thái xác nhận của hai bên được cập nhật gần thời gian thực (dưới 3 giây).
- NFR2. Dữ liệu form giữ nguyên khi xoay màn hình hoặc thoát ứng dụng tạm thời.

## UC26 Đánh giá đối tác

| Mục | Nội dung |
| --- | --- |
| Mã use case | UC26 |
| Tên use case | Đánh giá đối tác |
| Tác nhân | Người dùng (2 bên của giao dịch: người bán, người mua; đã đăng nhập) |
| Mô tả | Cho phép mỗi bên đánh giá bên còn lại sau khi đã nhận hàng. Đánh giá được tổng hợp vào hồ sơ uy tín của đối tác. |
| Mức ưu tiên | Trung bình |
| Khởi phát | Người dùng nhấn “Viết đánh giá” trong chi tiết giao dịch đã nhận hoặc hoàn tất, hoặc theo thông báo nhắc đánh giá |
| Tiền điều kiện | 1. Thiết bị có kết nối mạng Internet.<br>2. Ứng dụng đã được cài đặt và đang ở trạng thái đã đăng nhập.<br>3. Giao dịch đang ở trạng thái đã nhận hoặc hoàn tất.<br>4. Người dùng chưa đánh giá giao dịch này. |
| Hậu điều kiện | 1. Đánh giá được lưu.<br>2. Điểm uy tín của đối tác được cập nhật.<br>3. Đối tác nhận được thông báo.<br>4. Khi cả hai bên đã đánh giá, giao dịch chuyển sang hoàn tất. |

**Luồng cơ bản**

| Bước | Hành động của tác nhân | Phản hồi của hệ thống |
| --- | --- | --- |
| 1 | Người dùng nhấn “Viết đánh giá” | Hệ thống hiển thị form đánh giá |
| 2 | Người dùng chọn số sao tổng thể và số sao cho ba tiêu chí (độ chính xác thông tin, thái độ giao dịch, đúng cam kết), mỗi mục từ 1 đến 5, nhập nhận xét (tùy chọn) và nhấn “Gửi đánh giá” | Hệ thống kiểm tra số sao |
| 3 | | Hệ thống lưu đánh giá, tính lại điểm trung bình của đối tác |
| 4 | | Hệ thống thông báo cho đối tác và hiển thị đánh giá đã gửi |
| 5 | | Nếu cả hai bên đã đánh giá, hệ thống chuyển giao dịch sang hoàn tất và chuyển tin đăng sang đã bán |

**Luồng thay thế**

| Bước | Hành động của tác nhân | Phản hồi của hệ thống |
| --- | --- | --- |
| 1a | Người dùng nhấn “Để sau” | Hệ thống đóng form và vẫn cho đánh giá sau. |
| 5a | Chỉ một bên đánh giá, bên kia chưa đánh giá | Hệ thống tự chuyển giao dịch sang hoàn tất sau ba ngày kể từ lúc đã nhận. |

**Luồng ngoại lệ**

| Bước | Hành động của tác nhân | Phản hồi của hệ thống |
| --- | --- | --- |
| 1b | Đã đánh giá giao dịch này | Hệ thống không mở lại form, hiển thị đánh giá đã gửi và báo không thể đánh giá lại. |
| 2a | Chưa chọn đủ số sao | Hệ thống báo lỗi, nút “Gửi đánh giá” chưa bấm được. |
| 2b | Mất mạng | Hệ thống báo lỗi, giữ nguyên nội dung đã nhập. |

**Quy tắc nghiệp vụ**

- BR1. Chỉ đánh giá sau khi người mua đã nhận hàng (giao dịch đã nhận hoặc hoàn tất).
- BR2. Mỗi bên đánh giá một lần cho mỗi giao dịch.
- BR3. Áp dụng cho cả mua bán, trao đổi và tặng.
- BR4. Giao dịch chuyển sang hoàn tất khi hai bên đã đánh giá hoặc sau ba ngày kể từ lúc đã nhận.
- BR5. Nhận xét của đánh giá không qua kiểm từ khóa cấm; việc kiểm từ khóa cấm chỉ áp dụng cho tin đăng.

**Yêu cầu phi chức năng**

- NFR1. Điểm uy tín cập nhật trong vòng 5 giây.
- NFR2. Dữ liệu form giữ nguyên khi xoay màn hình.

## UC27 Trả lời đề nghị

| Mục | Nội dung |
| --- | --- |
| Mã use case | UC27 |
| Tên use case | Trả lời đề nghị |
| Tác nhân | Người dùng (vai trò người bán, đã đăng nhập và tài khoản đã được admin duyệt xác thực) |
| Mô tả | Cho phép người bán chấp nhận hoặc từ chối đề nghị mà một người mua gửi cho tin đăng của mình. Chấp nhận thì hệ thống tạo giao dịch chờ hai bên xác nhận điều khoản; từ chối thì đề nghị kết thúc. Người mua nhận thông báo kết quả. |
| Mức ưu tiên | Cao |
| Khởi phát | Người bán nhấn vào đề nghị trong mục trò chuyện |
| Tiền điều kiện | 1. Thiết bị có kết nối mạng Internet.<br>2. Ứng dụng đã được cài đặt và đang ở trạng thái đã đăng nhập; tài khoản đã được admin duyệt xác thực.<br>3. Người thực hiện là chủ tin đăng.<br>4. Đề nghị đang ở trạng thái “chờ người bán trả lời” và tin đăng đang ở trạng thái đang bán. |
| Hậu điều kiện | 1. Đề nghị chuyển sang “đã chấp nhận” hoặc “đã từ chối”.<br>2. Nếu chấp nhận: giao dịch được tạo ở trạng thái chờ xác nhận, tin đăng chuyển sang đang giữ chỗ, các đề nghị đang chờ khác của tin đó chuyển sang đã từ chối.<br>3. Người mua nhận được thông báo kết quả. |

**Luồng cơ bản**

| Bước | Hành động của tác nhân | Phản hồi của hệ thống |
| --- | --- | --- |
| 1 | Người bán vào trang danh sách cuộc trò chuyện, nhấn vào cuộc trò chuyện có đề nghị | Hệ thống mở cuộc trò chuyện, hiển thị thẻ đề nghị (loại đề nghị, số tiền, món đồ đổi nếu có, trạng thái) kèm tin đăng |
| 2 | Người bán nhấn “Chấp nhận” | Hệ thống kiểm tra đề nghị còn “chờ người bán trả lời” và tin đăng còn đang bán |
| 3 | | Trong một lần ghi, hệ thống chuyển đề nghị sang “đã chấp nhận”, tạo giao dịch ở trạng thái chờ xác nhận (người bán đã xác nhận sẵn, giá thống nhất bằng số tiền đề nghị), chuyển tin đăng sang đang giữ chỗ và chuyển các đề nghị đang chờ khác của tin đó sang “đã từ chối” |
| 4 | | Hệ thống thông báo kết quả cho người mua và đưa người bán tới chi tiết giao dịch để xác nhận điều khoản (UC25) |

**Luồng thay thế**

| Bước | Hành động của tác nhân | Phản hồi của hệ thống |
| --- | --- | --- |
| 1a | Người bán nhấn vào trang danh sách thông báo và nhấn vào thông báo đề nghị | Hệ thống hiển thị cuộc trò chuyện chứa đề nghị đó, người bán tiếp tục từ bước 2 |
| 2a | Người bán nhấn “Từ chối” | Hệ thống chuyển đề nghị sang “đã từ chối” và gửi thông báo cho người mua; không tạo giao dịch, tin đăng vẫn đang bán |
| 2b | Người bán nhấn “Quay lại” | Hệ thống trở về màn hình danh sách cuộc trò chuyện/ danh sách thông báo, đề nghị giữ nguyên “chờ người bán trả lời” |

**Luồng ngoại lệ**

| Bước | Hành động của tác nhân | Phản hồi của hệ thống |
| --- | --- | --- |
| 2c | Đề nghị đã được trả lời hoặc đã tự bị từ chối, hoặc tin đăng không còn đang bán (đang giữ chỗ do giao dịch khác, đã bán, ẩn, đã gỡ) | Hệ thống báo không thể xử lý. |
| 2d | Mất mạng | Hệ thống báo lỗi và giữ nguyên trạng thái. |

**Quy tắc nghiệp vụ**

- BR1. Chỉ chủ tin đăng được trả lời đề nghị gửi tới tin đó.
- BR2. Chấp nhận đề nghị tạo giao dịch ở trạng thái chờ xác nhận; giao dịch chỉ có hiệu lực (đã xác nhận) sau khi hai bên xác nhận điều khoản (UC25).
- BR3. Khi một đề nghị được chấp nhận, hoặc khi tin đăng không còn đang bán, mọi đề nghị đang chờ của tin đó tự chuyển sang đã từ chối.
- BR4. Người bán muốn ra giá ngược lại thì thương lượng qua chat

**Yêu cầu phi chức năng**

- NFR1. Thông báo kết quả đến người mua trong vòng 5 giây.

## UC28 Xác nhận đã giao hàng

| Mục | Nội dung |
| --- | --- |
| Mã use case | UC28 |
| Tên use case | Xác nhận đã giao hàng |
| Tác nhân | Người dùng (vai trò người bán, đã đăng nhập và tài khoản đã được admin duyệt xác thực) |
| Mô tả | Cho phép người bán xác nhận đã gửi món đồ qua đơn vị vận chuyển, bằng cách nhập tên đơn vị vận chuyển, mã vận đơn và chụp ảnh vận đơn. Giao dịch chuyển sang đã giao và bắt đầu đếm ngược bảy ngày để người mua xác nhận hoặc khiếu nại. |
| Mức ưu tiên | Cao |
| Khởi phát | Người bán nhấn "Xác nhận đã giao" trong chi tiết giao dịch |
| Tiền điều kiện | 1. Thiết bị có kết nối mạng Internet.<br>2. Ứng dụng đã được cài đặt và đang ở trạng thái đã đăng nhập; tài khoản đã được admin duyệt xác thực.<br>3. Người thực hiện là người bán của giao dịch.<br>4. Giao dịch đã xác nhận, hình thức giao nhận là gửi vận chuyển.<br>5. Nếu là mua bán: người mua đã thanh toán (khoản thanh toán đang giữ).<br>6. Giao dịch không bị hủy hoặc đang tranh chấp. |
| Hậu điều kiện | 1. Giao dịch chuyển sang “đã giao”, lưu đơn vị vận chuyển, mã vận đơn, ảnh vận đơn và thời điểm giao.<br>2. Đếm ngược bảy ngày tới hạn tự giải ngân bắt đầu.<br>3. Người mua nhận được thông báo. |

**Luồng cơ bản**

| Bước | Hành động của tác nhân | Phản hồi của hệ thống |
| --- | --- | --- |
| 1 | Người bán mở chi tiết giao dịch và nhấn "Xác nhận đã giao" | Hệ thống kiểm tra điều kiện của giao dịch và hiển thị biểu mẫu giao hàng: tên đơn vị vận chuyển, mã vận đơn, ảnh vận đơn |
| 2 | Người bán nhập tên đơn vị vận chuyển, nhập mã vận đơn, chụp hoặc chọn ảnh vận đơn và nhấn "Xác nhận" | Hệ thống kiểm tra đủ cả ba thông tin (nút "Xác nhận" chưa bấm được khi còn thiếu) và nén ảnh trên thiết bị (cạnh dài tối đa 1024 px, tối đa 300 KB trước khi mã hóa) |
| 3 | | Trong một lần ghi, hệ thống lưu ảnh vận đơn, đơn vị vận chuyển và mã vận đơn, chuyển giao dịch sang đã giao, ghi thời điểm giao theo giờ máy chủ và đặt hạn đếm ngược bằng thời điểm giao cộng bảy ngày |
| 4 | | Hệ thống thông báo cho người mua; chi tiết giao dịch hiển thị đếm ngược “Còn N ngày để xác nhận” cho hai bên |

**Luồng thay thế**

| Bước | Hành động của tác nhân | Phản hồi của hệ thống |
| --- | --- | --- |
| 1a | Người bán nhấn “Quay lại” | Hệ thống trở về chi tiết giao dịch, không lưu gì. |

**Luồng ngoại lệ**

| Bước | Hành động của tác nhân | Phản hồi của hệ thống |
| --- | --- | --- |
| 1b | Giao dịch có hình thức giao nhận là gặp trực tiếp | Hệ thống không hiển thị nút "Xác nhận đã giao"; người mua xác nhận đã nhận trực tiếp (UC22). |
| 1c | Giao dịch mua bán mà người mua chưa thanh toán | Hệ thống báo chờ người mua thanh toán (UC21) và không cho xác nhận đã giao. |
| 1d | Giao dịch đã bị hủy hoặc đang tranh chấp | Hệ thống báo không thể xác nhận đã giao. |
| 2a | Thiếu tên đơn vị vận chuyển, mã vận đơn hoặc ảnh vận đơn | Hệ thống báo lỗi và không chuyển trạng thái. |
| 2b | Ảnh vận đơn không đọc được hoặc vượt giới hạn dung lượng | Hệ thống từ chối ảnh và yêu cầu chọn ảnh khác. |
| 2c | Mất mạng | Hệ thống báo lỗi, giữ nguyên trạng thái và nội dung đã nhập. |

**Quy tắc nghiệp vụ**

- BR1. Chỉ người bán của giao dịch gửi vận chuyển được xác nhận đã giao.
- BR2. Bắt buộc có đủ tên đơn vị vận chuyển, mã vận đơn và ảnh vận đơn.
- BR3. Giao dịch mua bán chỉ xác nhận đã giao được sau khi người mua đã thanh toán.
- BR4. Hạn đếm ngược bảy ngày tính từ giờ máy chủ, không tính theo giờ trên máy người dùng.
- BR5. Xác nhận đã giao không thể thu hồi; từ lúc này không hủy được giao dịch, có vấn đề thì người mua hoặc người bán khiếu nại (UC24) trước khi hết đếm ngược.

**Yêu cầu phi chức năng**

- NFR1. Kết quả xác nhận hiển thị trong vòng 3 giây ở điều kiện mạng bình thường.
- NFR2. Dữ liệu biểu mẫu giao hàng giữ nguyên khi xoay màn hình.
- NFR3. Ảnh vận đơn được nén trên thiết bị và chỉ hai bên của giao dịch cùng Admin xem được.

## Ghi chú sửa đổi

Mỗi dòng là một chỗ đã sửa so với bản nháp `local/use-case-md/uc-c-giao-dich.md`, kèm lý do. D1 đến D11 là mười một quyết định của leader ngày 2026-10-22; "trang-thai.md" là `workflow/docs/architecture/trang-thai.md`; C01 đến C06 là mô tả màn hình ở `workflow/docs/ui/c-ket-noi-giao-dich/README.md`.

**Toàn tệp**

- Đánh lại mã theo danh sách chốt: nhắn tin là UC18 (ô mã nháp ghi UC19, tiêu đề nháp ghi "UCS 18"), trả lời đề nghị là UC27 (ô mã nháp ghi UC21, tiêu đề nháp ghi "UCS 27"), thêm UC21 và UC28, đổi tên UC25. Thứ tự use case theo UC18 đến UC28; nháp để "Xem lịch sử giao dịch" ở cuối.
- Mọi tên trạng thái đổi theo tài liệu hệ thống (luật viết số 6): giao dịch chờ xác nhận, đã xác nhận, đã giao, đã nhận, hoàn tất, đã hủy, tranh chấp; đề nghị "chờ người bán trả lời", "đã chấp nhận", "đã từ chối"; khoản thanh toán "đang giữ", "đã giải ngân", "đã hoàn"; tin đăng đang bán, đang giữ chỗ. Các tên không có trong tài liệu ("Đang thỏa thuận", "Chờ thanh toán", "Chờ giao nhận", "Chờ người bán xác nhận", "Đã đồng ý", "được chấp nhận") đã bỏ.
- Số bước đánh lại liền mạch, bỏ mọi dòng rỗng "Không có". Luồng thay thế và ngoại lệ dùng dạng `<số bước>a`, `<số bước>b` gắn với bước thật của luồng cơ bản; chữ cái không trùng nhau giữa luồng thay thế và ngoại lệ của cùng một bước (luật viết số 4).
- Điều kiện "tài khoản đã được admin duyệt xác thực" viết rõ trong tiền điều kiện UC20, UC21, UC22, UC25, UC27 và UC28 (D6).
- Mọi chỗ nháp nói tiền chuyển tới ngân hàng hoặc cổng thanh toán đã viết lại thành hành vi của hệ thống (D2, D9). Phần đặc tả ở trên không còn nhắc ngân hàng hay cổng thanh toán.
- Bản nháp gắn việc tạo giao dịch với nút "Tạo giao dịch" trong chat, nhưng `trang-thai.md` mục 3 tạo giao dịch ngay khi người bán chấp nhận đề nghị. Toàn bộ chuỗi UC20, UC27, UC25 viết theo `trang-thai.md`. Tin đăng sang đang giữ chỗ lúc người bán chấp nhận (UC27), không phải lúc hai bên xác nhận.

**UC18 Nhắn tin**

- Mã UC19 thành UC18 (nháp có hai use case cùng mã UC19).
- Khởi phát: "biểu tượng ô chat ở trang chủ" thành "tab Tin nhắn ở thanh điều hướng dưới" theo C01 và README giao diện (thanh dưới có tab Tin nhắn); bước 1 sửa tương ứng.
- Sửa lỗi chính tả "đề nghỉ" trong bước 3. Nửa sau bước 3 ("chấp nhận đề nghị để bắt đầu đoạn chat mới / hệ thống khởi tạo trang trò chuyện mới") bỏ khỏi luồng cơ bản vì cuộc trò chuyện có mã `listingId_buyerId` và đã tồn tại từ lúc người mua gửi đề nghị hoặc tin nhắn đầu tiên (firestore-schema.md, collection `conversations`); việc gửi và trả lời đề nghị chuyển thành luồng thay thế 3b trỏ tới UC20 và UC27.
- Bỏ hai dòng rỗng "Không có" của bước 5 và 6 trong luồng cơ bản; bỏ dòng rỗng "1a.3" và "6b".
- Dòng "1a Đối phương bị khóa/xóa tài khoản" nằm dưới dòng tiêu đề lặp (ô gộp bị vỡ). Đây là tình huống lỗi nên chuyển sang luồng ngoại lệ, gắn bước 3 (3e).
- "1a.1 Người dùng gửi ảnh thay cho văn bản" đổi thành 3a: gửi ảnh thay cho văn bản xảy ra ở bước 3, không phải bước 1.
- Ngoại lệ "2a Nhập tin nhắn là chuỗi rỗng" và "4a Mất kết nối mạng" đều xảy ra lúc gửi tin (bước 3), nên đánh lại thành 3c và 3d; bước 4 chỉ là đối phương đọc tin.
- Ngoại lệ "6a Từ chối đề nghị giá cả" tham chiếu bước 6 không tồn tại. Đổi thành 3f và sửa nội dung: nháp nói không hiện ô nhập tin nhắn, nhưng C02 luôn có thanh nhập liệu và chỉ đổi trạng thái thẻ đề nghị sang đã từ chối.
- BR1 "một/nhiều tin đăng" sửa thành "một tin đăng; mỗi người mua một cuộc trò chuyện cho mỗi tin" theo schema và C01.
- BR3 thêm giới hạn ảnh theo D3. BR4 trống: điền "chỉ chữ và ảnh, không video" (D4). Thêm BR5: tài khoản chưa xác thực vẫn nhắn tin được (D6). NFR3 trống: điền theo C01 (xem lại danh sách đã tải khi mất mạng).

**UC19 Xem lịch sử giao dịch**

- Thêm luồng thay thế 1a, 1b, 1d: xem lịch sử thanh toán (D11), theo tab "Thanh toán" của C04 (STT 58). Mô tả và hậu điều kiện nhắc thêm thanh toán.
- Dòng "1a Chưa có giao dịch" nằm dưới dòng tiêu đề lặp. Giữ trong luồng thay thế vì danh sách trống là trạng thái bình thường, không phải lỗi (ngoại lệ "Lỗi tải dữ liệu" đã có riêng); đánh lại 1c.
- Ngoại lệ "2a Lỗi tải dữ liệu" đánh lại 1e (lỗi xảy ra lúc tải danh sách ở bước 1).
- BR1 mở rộng "giao dịch và thanh toán của chính mình".

**UC20 Gửi đề nghị**

- Bỏ các dòng rỗng "1a" (luồng thay thế), "1a.1", "1a.3" và "2e". "1a Người mua nhấn Quay lại" giữ nguyên là 1a.
- Trạng thái đề nghị "Chờ phản hồi" / "chờ phản hồi" thống nhất thành "chờ người bán trả lời" (tên trong enums.md); tin đăng "đang hiển thị" thành "đang bán".
- Bước 2 thêm lựa chọn loại đề nghị (tiền, vật, cả hai) theo STT 47 và trang-thai.md mục 2; bước 4 thêm hiển thị đề nghị trong cuộc trò chuyện (C02).
- Các ngoại lệ đánh lại theo bước hệ thống phát hiện: tin không khả dụng và đã có đề nghị chờ thành 1b, 1c; giá ngoài khoảng và giá không hợp lệ thành 3a, 3b.
- BR1 và BR2: nháp ghi "khoảng tối thiểu - tối đa" nhưng schema chỉ có `minOfferPrice` (mức tối thiểu); viết rõ là mức tối thiểu, giữ cụm "khoảng cho phép" của tác giả. NFR2 giữ nguyên chữ "khoảng giá".
- Thêm BR5: người mua không hủy được đề nghị và không có đề xuất lại (trang-thai.md mục 2, STT 47).
- Tiền điều kiện 2 thêm "tài khoản đã được admin duyệt xác thực" (D6).

**UC21 Thanh toán cho giao dịch (viết mới)**

- Viết từ đầu theo D2 và trang-thai.md mục 3 (dòng "Người mua thanh toán") và mục 4. Màn hình giống ứng dụng thanh toán (chọn phương thức, màn xác nhận, biên lai) nhưng chỉ tạo khoản thanh toán "đang giữ" và thông báo cho người bán; không có dịch vụ ngoài, không có tiền thật. Giao dịch trao đổi hoặc cho tặng không có thanh toán.
- Danh sách phương thức thanh toán không có trong tài liệu hệ thống; use case chỉ nói "danh sách phương thức mô phỏng" và BR6 ghi phương thức không lưu thành dữ liệu của khoản thanh toán (schema `payments` không có trường phương thức).

**UC22 Xác nhận đã nhận hàng**

- Mô tả: bỏ chữ "xác nhận thành công, hoàn tất đối với giao dịch"; giao dịch sang "đã nhận", còn "hoàn tất" xảy ra khi hai bên đã đánh giá hoặc sau ba ngày (trang-thai.md mục 3). Hậu điều kiện 2 và bước 4 sửa tương ứng; thêm BR5.
- Bước 4 nháp "gửi yêu cầu giải ngân tới ngân hàng" viết lại: hệ thống chuyển khoản thanh toán từ đang giữ sang đã giải ngân cho người bán (D2, D9). NFR1 và NFR2 viết lại theo cùng ý (D2).
- Khởi phát: "trong đoạn chat" thành "trong chi tiết giao dịch" (nút nằm ở C03).
- Bỏ yêu cầu người mua chụp ảnh khi nhận hàng (nháp bước 1 và 2): không có trường nào lưu ảnh nhận hàng trong `deals`, STT 53 và C03 cũng không đòi. Thay bằng lời nhắc xác nhận không thể thu hồi.
- Bước 3 nháp "kiểm tra người bán đã xác nhận giao chưa" chuyển thành tiền điều kiện và ngoại lệ 1a; trạng thái "Chờ người bán xác nhận" không có trong tài liệu nên bỏ. Giao dịch gặp trực tiếp được xác nhận ngay khi đã xác nhận (BR4), theo trang-thai.md.
- Tiền điều kiện 4 sửa từ "đã có hiệu lực (mua bán: đã thanh toán)" thành trạng thái cụ thể.
- Thêm luồng thay thế 2b: hệ thống tự giải ngân khi hết đếm ngược bảy ngày mà người mua chưa xác nhận (D11, trang-thai.md dòng "Hết đếm ngược"). BR3 nháp "giải ngân/hoàn về" sửa thành "giải ngân cho người bán": tài liệu không có đường hoàn tiền tự động.
- BR1 nháp "chỉ giải ngân khi cả hai bên đã xác nhận" sửa: người bán không "xác nhận" ở bước giải ngân; điều kiện là giao dịch sang đã nhận. Bỏ dòng rỗng "1a"; "Quay lại" đánh lại 2a và trở về chi tiết giao dịch (không phải cuộc trò chuyện).

**UC23 Hủy giao dịch**

- Đổi chính tả "Huỷ" thành "Hủy" cho thống nhất với các use case khác và tài liệu.
- Phạm vi: nháp chỉ cho hủy khi "chưa đủ hai xác nhận" (trạng thái "Đang thỏa thuận"); `trang-thai.md` mục 3 cho hủy ở cả chờ xác nhận và đã xác nhận, tức mọi lúc trước khi giao hàng, bắt buộc có lý do và hoàn khoản đang giữ. Mô tả, tiền điều kiện 4, hậu điều kiện, luồng cơ bản, ngoại lệ và BR1, BR2 viết lại theo đó; thêm BR4 (lý do bắt buộc). Nháp ghi lý do là tùy chọn, mâu thuẫn với STT 55 và C03.
- Bỏ "hợp đồng mini" khỏi mô tả (D7). Bỏ dòng rỗng "1a" của luồng thay thế; "Người mua nhấn Quay lại" sửa thành "Người dùng nhấn Quay lại" vì cả hai bên đều hủy được; đánh lại 2a.
- Thêm bước 3 (hoàn khoản đang giữ, tin về đang bán) và luồng thay thế 2b (tự hủy sau ba ngày chờ xác nhận) theo trang-thai.md.
- Ngoại lệ "Giao dịch đã được cả hai bên xác nhận" thay bằng "đã giao hoặc đang tranh chấp" (2d); "đã hoàn tất hoặc đã hủy" thành "đã nhận, hoàn tất hoặc đã hủy" (2e); thêm 2c (thiếu lý do).

**UC24 Khiếu nại giao dịch**

- Bỏ "video": bước 2 chỉ đính kèm ảnh, tối thiểu một ảnh và tối đa năm ảnh (D4, STT 64); thêm BR5.
- Dòng "1a Thiếu lý do hoặc mô tả" nằm dưới dòng tiêu đề lặp: là lỗi nhập liệu nên chuyển sang luồng ngoại lệ, gắn bước 2 (2a), thêm trường hợp chưa có ảnh. "Tệp vượt dung lượng" thành "ảnh" (2b).
- Tiền điều kiện 4 và 5 sửa theo trang-thai.md: chỉ khiếu nại được khi đã giao còn trong đếm ngược bảy ngày, hoặc đã xác nhận khi gặp trực tiếp, và giao dịch chưa từng có khiếu nại. Thêm ngoại lệ 2d (hết hạn).
- Hậu điều kiện 2: "tạm dừng" thành tranh chấp và dừng đếm ngược; bước 3 "đóng băng giao dịch" viết cụ thể như vậy. BR1: một khiếu nại cho cả đời giao dịch, rút đơn là cuối cùng.
- BR2 bỏ chữ "escrow" thành "khoản thanh toán đang giữ". BR3 nháp có "cảnh cáo tài khoản (đủ 3 lần thì khóa)" không thuộc các quyết định khiếu nại (ComplaintDecision); viết lại bằng các quyết định có trong enums.md.
- Khởi phát: "Khiếu nại" thành "Khiếu nại giao dịch" theo C03.

**UC25 Xác nhận điều khoản giao dịch**

- Đổi tên (D7). Bỏ "Tạo hợp đồng", "hợp đồng mini", "hợp đồng quay về trạng thái nháp" (D7): giao dịch đã được tạo ở UC27, UC25 chỉ còn xem, sửa và xác nhận điều khoản. Luồng cơ bản viết lại thành 5 bước (xem, sửa điều khoản, xác nhận lần lượt, hệ thống đặt đã xác nhận, chuyển sang thanh toán).
- Hậu điều kiện: nháp ghi trạng thái "Chờ thanh toán"/"Chờ giao nhận" không có trong tài liệu; thay bằng "đã xác nhận". "Tin đăng tự động ẩn khỏi hệ thống" viết lại: tin đang giữ chỗ và quay lại đang bán nếu giao dịch bị hủy (D7); BR5 viết lại cùng ý.
- Tiền điều kiện 3 và 4 nháp (hai bên đang trò chuyện, tin chưa có giao dịch) thay bằng: giao dịch chờ xác nhận, tin đang giữ chỗ. Bổ sung điều kiện đã xác thực (D6).
- Bỏ dòng rỗng của bước 6 và hai ngoại lệ tham chiếu bước 6 (nháp 6a "Tin đăng đã có giao dịch khác" bỏ vì tin đã giữ chỗ từ UC27; 6b "Mất mạng" đánh lại 4b). Ngoại lệ 4a nháp "Thiếu thông tin" đánh lại 3c. "2a Một bên nhấn Hủy" đánh lại 3b, vẫn chuyển sang UC23.
- Luồng thay thế "Từ chối/Yêu cầu chỉnh sửa" (nằm dưới dòng tiêu đề lặp, nội dung đúng là luồng thay thế) thành 3a "Sửa điều khoản" theo STT 51: xóa xác nhận của cả hai bên.
- Thêm 4a: tự hủy sau ba ngày chờ xác nhận (trang-thai.md); thêm 3d: giao dịch đã hủy hoặc hết hạn; thêm BR6.

**UC26 Đánh giá đối tác**

- Bỏ dòng rỗng "6", "6a", "6b"; ngoại lệ "4a Đã đánh giá giao dịch này" tham chiếu bước không phù hợp, đánh lại 1b và sửa phản hồi theo C05 (không mở lại form).
- Dòng "1a Chưa chọn số sao" nằm dưới dòng tiêu đề lặp: là lỗi nhập liệu nên chuyển sang ngoại lệ (2a).
- Số sao: nháp chỉ một điểm 1 đến 5; theo STT 59 và C05 có sao tổng thể và ba tiêu chí, nên bước 2 viết thêm. Nút "Gửi" thành "Gửi đánh giá".
- "Sau khi giao dịch hoàn tất" sửa thành "sau khi đã nhận hàng" (đã nhận hoặc hoàn tất) ở mô tả, khởi phát, tiền điều kiện 3 và BR1, theo trang-thai.md và C05. Thêm bước 5 và luồng thay thế 5a: giao dịch sang hoàn tất khi hai bên đã đánh giá hoặc sau ba ngày; BR4 trống điền cùng ý.
- NFR2 trống: điền "dữ liệu form giữ nguyên khi xoay màn hình" (quy ước chung của ứng dụng). Thêm ngoại lệ 2c (mất mạng).
- Nháp ghi tin nhắn kiểm tra "từ khóa cấm" cho nhận xét; tài liệu hệ thống chỉ nói `KeywordFilter` gắn cờ tin đăng. Giữ nguyên chữ của tác giả, xem phần chưa giải quyết.

**UC27 Trả lời đề nghị**

- Mã UC21 thành UC27 (tiêu đề nháp ghi "UCS 27").
- Mô tả viết lại: câu "Người bán nhận thông báo và phản hồi bằng cách chấp nhận hoặc từ chối" sao chép từ UC20, không đúng với người bán trả lời.
- Bước cơ bản 3 và 4 nháp (kiểm tra giá trong khoảng cho phép, lưu giá đề nghị, "chờ phản hồi") sao chép nguyên văn từ UC20; thay bằng việc hệ thống ghi một lần: đề nghị đã chấp nhận, tạo giao dịch chờ xác nhận, tin đang giữ chỗ, các đề nghị chờ khác đã từ chối, thông báo người mua (trang-thai.md mục 2, 3).
- Tên trạng thái thống nhất: "Đã đồng ý", "được chấp nhận" thành "đã chấp nhận"; "đã từ chối" giữ; "Chờ phản hồi" thành "chờ người bán trả lời".
- Nháp gộp chấp nhận và từ chối trong một dòng bước 2 bằng dấu "/". Tách: chấp nhận là luồng cơ bản; từ chối là luồng thay thế 2a. Bỏ câu "xóa thông báo đề nghị đó" vì tài liệu hệ thống không có thao tác xóa thông báo.
- Bước 1: nháp "danh sách đề nghị sắp xếp theo thời gian" sửa theo C01 và C02: người bán mở cuộc trò chuyện chứa thẻ đề nghị. Bỏ dòng rỗng "1a.3"; "1a" mở qua thông báo giữ là 1a (C06: thông báo đề nghị mở C02); "Quay lại" đánh lại 2b.
- BR2 nháp "đồng ý đề nghị chưa phải là tạo giao dịch" sửa: chấp nhận tạo giao dịch chờ xác nhận (trang-thai.md mục 3). BR3 viết lại theo bảng offer. Mở rộng ngoại lệ 2c theo trạng thái tin.
- Tiền điều kiện 2 và 4 bổ sung (D6, tin đang bán). Hậu điều kiện thêm giao dịch, tin giữ chỗ.

**UC28 Xác nhận đã giao hàng (viết mới)**

- Viết từ đầu theo STT 54, C03 và trang-thai.md mục 3 (dòng "Giao hàng"): bắt buộc tên đơn vị vận chuyển, mã vận đơn, ảnh vận đơn; giao dịch sang đã giao; hạn đếm ngược bảy ngày tính theo giờ máy chủ. Ảnh vận đơn nén theo D3.
- Chỉ áp dụng cho hình thức gửi vận chuyển; gặp trực tiếp không có bước này (ngoại lệ 1b). Giao dịch mua bán phải đã thanh toán (ngoại lệ 1c).

**Cập nhật theo quyết định leader 2026-10-22 (đợt chốt các điểm còn treo, nhóm C)**

- UC21: thêm phương thức thanh toán lưu được. Mô tả, hậu điều kiện 1, bước 1, bước 2 và bước 4 nhắc ba phương thức thẻ ngân hàng, ví điện tử, chuyển khoản; thêm ngoại lệ 2c (chưa chọn phương thức thì "Tiếp tục" chưa bấm được); BR6 viết lại: phương thức là `CARD`, `E_WALLET` hoặc `BANK_TRANSFER`, lưu trong `payments.method`. Dòng cũ ở mục UC21 nói schema không có trường phương thức nay không còn đúng. Theo quyết định leader 2026-10-22.
- UC22: người mua xác nhận đã nhận hàng kèm một ảnh. Mô tả, hậu điều kiện 1, bước 1 và bước 2 sửa; thêm ngoại lệ 2d (ảnh lỗi hoặc quá dung lượng), BR6 (ảnh bắt buộc, `deals.receivedImageId`, `ImagePurpose` có giá trị `RECEIVED`) và NFR3; bước 2b ghi rõ tự giải ngân thì không có ảnh. Dòng cũ ở mục UC22 nói bỏ ảnh nhận hàng nay không còn đúng. Theo quyết định leader 2026-10-22.
- UC23 và UC24: bỏ chữ "đã xác thực" ở ô Tác nhân và tiền điều kiện 2 (nháp ghi "đã xác thực tài khoản", dễ hiểu thành phải được admin duyệt); viết rõ không đòi tài khoản đã được admin duyệt xác thực, thêm BR5 ở UC23 và BR6 ở UC24. Theo nền đã chốt: hủy và khiếu nại không đòi xác thực, ai đã vào được giao dịch thì phải thoát ra được (quyết định leader 2026-10-22).
- UC24: thêm BR7, cảnh cáo không phải quyết định khiếu nại, admin cảnh cáo bằng UC35. Danh sách quyết định ở BR3 vốn đã không có "Cảnh cáo", nên không xóa gì; theo STT 89 và `dac-ta-lop/enums.md` (quyết định leader 2026-10-22).
- UC26: bỏ kiểm từ khóa cấm trong nhận xét đánh giá: bước 2 chỉ kiểm số sao, xóa ngoại lệ "2b Nhận xét chứa từ khóa cấm", ngoại lệ "2c Mất mạng" đánh lại 2b, thêm BR5. `KeywordFilter` chỉ gắn cờ tin đăng. Dòng cũ ở mục UC26 nói giữ nguyên chữ của tác giả về từ khóa cấm nay không còn đúng. Theo quyết định leader 2026-10-22.
- UC19: bộ lọc trạng thái, loại giao dịch và vai trò đã có sẵn ở bước 2; không sửa chữ. Màn C04 được bổ sung hai bộ lọc còn thiếu (xem tệp giao diện nhóm C), theo quyết định leader 2026-10-22.
- Đầu tệp: danh sách màn hình tham chiếu đổi "C01 đến C06" thành "C01 đến C07" vì thêm màn C07 Thanh toán giao dịch (quyết định leader 2026-10-22).
- Tên trạng thái hiển thị (quyết định leader 2026-10-22, mục 7) và từ "xác thực tài khoản" (mục 8): đã rà cả tệp, không còn nhãn "Đang giao", "Đang khiếu nại", "Chờ hai bên xác nhận" hay cụm "xác minh người bán" nên không sửa chữ ở tệp này.

- UC19 và UC26: ô Tác nhân và tiền điều kiện bỏ cụm "đã xác thực" (quyết định leader 2026-10-22, câu 9): xem lịch sử giao dịch và đánh giá đối tác chỉ đòi đã đăng nhập, không đòi admin đã duyệt xác thực. Cụm cũ dễ bị đọc nhầm thành điều kiện xác thực tài khoản.
