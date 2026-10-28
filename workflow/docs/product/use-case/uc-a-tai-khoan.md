# Đặc tả use case: A. Tài khoản và thông báo

Module A gồm 8 use case, từ UC01 đến UC08: đăng ký, đăng nhập, đặt lại mật khẩu, đăng xuất, quản lý thông tin cá nhân, quản lý địa chỉ giao nhận, xem thông báo và xóa tài khoản. Bản này dựa trên bản nháp của nhóm, đã sửa theo các quyết định của leader ngày 2026-10-22; từng chỗ sửa nằm ở mục "Ghi chú sửa đổi" cuối tệp.

## UC01 Đăng ký tài khoản

| Mục | Nội dung |
| --- | --- |
| Mã use case | UC01 |
| Tên use case | Đăng ký tài khoản |
| Tác nhân | Khách (người dùng chưa có tài khoản đăng nhập) |
| Mô tả | Cho phép khách đăng ký tạo tài khoản người dùng mới trên ứng dụng PUBGApp thông qua việc nhập Gmail hoặc liên kết tài khoản Google, xác thực email bằng liên kết do hệ thống gửi về email, sau đó hoàn tất biểu mẫu thông tin cá nhân và chọn danh mục quan tâm để trở thành Người dùng chính thức. |
| Mức ưu tiên | Cao |
| Khởi phát | Khách nhấn nút "Tạo tài khoản" |
| Tiền điều kiện | 1. Thiết bị có kết nối mạng Internet.<br>2. Ứng dụng đã được cài đặt và đang ở trạng thái chưa đăng nhập. |
| Hậu điều kiện | 1. Tài khoản mới được khởi tạo thành công trên hệ thống.<br>2. Khách chuyển vai trò thành Người dùng (User) đang hoạt động (ACTIVE).<br>3. Các danh mục quan tâm khách đã chọn được lưu vào hồ sơ.<br>4. Người dùng sau khi đăng nhập được điều hướng đến Trang chủ. |

**Luồng cơ bản**

| Bước | Hành động của tác nhân | Phản hồi của hệ thống |
| --- | --- | --- |
| 1 | Khách nhấn nút "Đăng ký" | Hiển thị giao diện màn hình Đăng ký tài khoản gồm: ô nhập Email, nút "Tiếp tục" và tùy chọn "Đăng ký bằng Google". |
| 2 | Khách nhập địa chỉ Gmail và nhấn "Tiếp tục". | Hệ thống kiểm tra định dạng email và kiểm tra email đã tồn tại trong hệ thống chưa. |
| 3 |  | Hệ thống gửi một liên kết xác thực đến Gmail của khách và chuyển sang màn hình "Kiểm tra hộp thư", nhắc khách mở thư và bấm liên kết, kèm nút "Gửi lại liên kết". |
| 4 | Khách mở hộp thư và bấm vào liên kết xác thực trong thư. | Ứng dụng được mở lại và hệ thống xác thực liên kết: kiểm tra liên kết hợp lệ, còn hiệu lực và đúng với email đã nhập. |
| 5 |  | Hệ thống xác thực email thành công và chuyển sang màn hình "Thiết lập thông tin tài khoản" gồm: các ô nhập Mật khẩu, Xác nhận mật khẩu, Họ và tên hiển thị, Số điện thoại và Khu vực (Tỉnh/Thành phố). |
| 6 | Khách nhập đầy đủ thông tin; sau đó nhấn "Tiếp tục". | Hệ thống kiểm tra tính hợp lệ của dữ liệu (mật khẩu tối thiểu 8 ký tự gồm chữ và số, định dạng số điện thoại), rồi hiển thị màn hình "Chọn danh mục quan tâm" với danh sách các danh mục món đồ. |
| 7 | Khách chọn một hoặc nhiều danh mục quan tâm; sau đó nhấn "Hoàn tất đăng ký". | Hệ thống ghi nhận các danh mục đã chọn để dùng cho việc gợi ý tin. |
| 8 |  | Hệ thống tạo tài khoản mới, khởi tạo hồ sơ người dùng (User với trạng thái ACTIVE), tự động đăng nhập và chuyển người dùng đến Trang chủ kèm thông báo chào mừng. |

**Luồng thay thế**

| Bước | Hành động của tác nhân | Phản hồi của hệ thống |
| --- | --- | --- |
| 1a | Đăng ký nhanh bằng tài khoản Google |  |
| 1a.1 | Khách nhấn nút "Đăng ký bằng Google" | Hệ thống kích hoạt dịch vụ Google Sign-In và hiển thị danh sách các tài khoản Google hiện có trên máy (hoặc mở hộp thoại đăng nhập của Google) |
| 1a.2 | Khách chọn tài khoản Google muốn dùng để đăng ký. | Hệ thống nhận thông tin tài khoản từ Google. Email của tài khoản Google đã được Google xác thực nên hệ thống không gửi liên kết xác thực và chuyển thẳng sang màn hình "Thiết lập thông tin tài khoản". |
| 1a.3 | Tiếp tục bước 5 của luồng cơ bản để hoàn tất quá trình. |  |
| 1b | Khách ấn nút quay lại | Hệ thống trở về giao diện màn hình trước đó |
| 3a | Yêu cầu gửi lại liên kết |  |
| 3a.1 | Tại bước 3, nếu khách chưa nhận được thư hoặc liên kết đã hết hiệu lực, khách nhấn nút "Gửi lại liên kết". | Hệ thống gửi một liên kết xác thực mới vào Gmail của khách. Khách tiếp tục thực hiện bước 4 với liên kết mới. |

**Luồng ngoại lệ**

| Bước | Hành động của tác nhân | Phản hồi của hệ thống |
| --- | --- | --- |
| 2a | Nhập Email không hợp lệ hoặc đã tồn tại | Hệ thống giữ nguyên màn hình, hiển thị thông báo lỗi tương ứng và yêu cầu khách sửa lại email. |
| 4a | Bấm liên kết xác thực không hợp lệ hoặc đã hết hiệu lực | Hệ thống báo lỗi: "Liên kết xác thực không hợp lệ hoặc đã hết hạn" và cho khách quay lại màn hình "Kiểm tra hộp thư" để nhấn "Gửi lại liên kết". |
| 6a | Dữ liệu trên biểu mẫu thông tin không hợp lệ | Hệ thống bôi đỏ ô dữ liệu lỗi kèm thông báo hướng dẫn cụ thể dưới từng ô. |
| 6b | Mất kết nối mạng (tại bước 6 hoặc bước 7) | Hệ thống hiển thị thông báo "Mất kết nối mạng, vui lòng kiểm tra lại đường truyền" và giữ nguyên hiện trạng dữ liệu mà khách đang điền dở. |

**Quy tắc nghiệp vụ**

- BR1. Địa chỉ email đăng ký phải có định dạng hợp lệ theo quy định của hệ thống.
- BR2. Mỗi địa chỉ email chỉ được liên kết với một tài khoản duy nhất.
- BR3. Liên kết xác thực chỉ được gửi tới email khách đã nhập và chỉ có hiệu lực trong một thời hạn nhất định kể từ khi được gửi.
- BR4. Tài khoản được nhận diện bằng địa chỉ email (hoặc tài khoản Google); hệ thống không dùng tên tài khoản riêng.
- BR5. Mật khẩu tài khoản phải có tối thiểu 8 ký tự, gồm cả chữ và số
- BR6. Mật khẩu xác nhận phải giống hoàn toàn mật khẩu tài khoản
- BR7. Hệ thống chỉ tạo tài khoản khi các trường dữ liệu đã được điền đầy đủ
- BR8. Khách phải xác thực email xong mới được điền thông tin tài khoản.

**Yêu cầu phi chức năng**

- NFR1. Mật khẩu phải được mã hóa trước khi lưu trữ. Liên kết xác thực chỉ được gửi tới đúng hòm thư yêu cầu.
- NFR2. Thời gian hệ thống xử lý và gửi liên kết xác thực về máy chủ thư của người dùng không vượt quá 5 giây trong điều kiện mạng bình thường.
- NFR3. Màn hình và toàn bộ dữ liệu đang nhập dở phải giữ nguyên trạng thái, không bị mất dữ liệu khi người dùng xoay ngang hoặc xoay dọc thiết bị.

## UC02 Đăng nhập

| Mục | Nội dung |
| --- | --- |
| Mã use case | UC02 |
| Tên use case | Đăng nhập |
| Tác nhân | Khách (người dùng đã có tài khoản nhưng chưa đăng nhập) |
| Mô tả | Người dùng nhập email, mật khẩu đã đăng ký sau đó bấm nút đăng nhập. |
| Mức ưu tiên | Cao |
| Khởi phát | Chọn chức năng đăng nhập |
| Tiền điều kiện | Có tài khoản đã đăng ký<br>Nhập đúng email và mật khẩu |
| Hậu điều kiện | Hệ thống chuyển vào màn hình trang chủ<br>Trạng thái tài khoản chuyển từ khách qua người dùng |

**Luồng cơ bản**

| Bước | Hành động của tác nhân | Phản hồi của hệ thống |
| --- | --- | --- |
| 1 | Chọn chức năng đăng nhập ở trang chủ | Hệ thống chuyển qua giao diện đăng nhập |
| 2 | Chọn đăng nhập bằng email | Hiển thị form nhập email, mật khẩu |
| 3 | Nhập email, mật khẩu vào form, ấn nút đăng nhập | Hệ thống kiểm tra dữ liệu nhập vào |
| 4 |  | Hệ thống chuyển tới màn hình trang chủ, trạng thái đã đăng nhập |

**Luồng thay thế**

| Bước | Hành động của tác nhân | Phản hồi của hệ thống |
| --- | --- | --- |
| 2a | Đăng nhập bằng tài khoản Google |  |
| 2a.1 | Chọn đăng nhập bằng google | Chuyển qua giao diện chọn tài khoản google |
| 2a.2 | Chọn tài khoản google đã đăng ký để đăng nhập | Kiểm tra tài khoản đã đăng ký người dùng chưa |
| 2a.3 | Nhập mật khẩu cho tài khoản google | Hệ thống kiểm tra dữ liệu nhập vào trùng khớp |
| 2a.4 |  | Hệ thống chuyển qua giao diện trang chủ, trạng thái đã đăng nhập |
| 2b | Đăng nhập bằng tài khoản Google lần đầu, chưa có hồ sơ (sau bước 2a.2) | Hệ thống chuyển sang màn hình đăng ký ở chế độ hoàn tất hồ sơ để khách điền thông tin còn thiếu (xem UC01); hoàn tất thì chuyển tới màn hình trang chủ. |

**Luồng ngoại lệ**

| Bước | Hành động của tác nhân | Phản hồi của hệ thống |
| --- | --- | --- |
| 3a | Nhập sai email, mật khẩu | Hệ thống từ chối đăng nhập, thông báo nhập sai email hoặc mật khẩu |
| 3b | Tài khoản chưa được đăng ký | Hệ thống từ chối đăng nhập, thông báo tài khoản chưa được đăng ký |
| 3c | Tài khoản đã bị xóa (trạng thái DELETED) | Hệ thống từ chối đăng nhập, thông báo "Tài khoản đã xóa" và đăng xuất. |
| 3d | Tài khoản đang bị khóa (khóa tạm hoặc vĩnh viễn) | Hệ thống thông báo rõ lý do và thời hạn khóa rồi đăng xuất. Khóa tạm đã hết hạn thì cho vào bình thường. |

**Quy tắc nghiệp vụ**

- BR1. Chỉ tài khoản đã đăng ký mới đăng nhập được, bằng email và mật khẩu hoặc bằng tài khoản Google. Không đăng nhập bằng số điện thoại và không đăng nhập bằng tên tài khoản.
- BR2. Tài khoản đã xóa (DELETED) không đăng nhập được.
- BR3. Tài khoản đang bị khóa không vào được hệ thống trong thời hạn khóa.

**Yêu cầu phi chức năng**

- NFR1. Đăng nhập được vào tài khoản của người dùng
- NFR2. Trong lúc hệ thống đang xử lý yêu cầu đăng nhập, nút đăng nhập chuyển sang trạng thái chờ và không bấm lại được.
- NFR3. Màn hình và dữ liệu đang nhập dở phải giữ nguyên trạng thái, không bị mất dữ liệu khi người dùng xoay ngang hoặc xoay dọc thiết bị.

## UC03 Đặt lại mật khẩu

| Mục | Nội dung |
| --- | --- |
| Mã use case | UC03 |
| Tên use case | Đặt lại mật khẩu |
| Tác nhân | Khách (người dùng chưa đăng nhập) |
| Mô tả | Khách có thể đặt lại mật khẩu trong trường hợp quên mật khẩu. Việc đổi mật khẩu khi đang đăng nhập nằm ở UC05. |
| Mức ưu tiên | Cao |
| Khởi phát | Khách chọn "Quên mật khẩu" trên form đăng nhập |
| Tiền điều kiện | 1. Tài khoản đã được đăng ký xác thực từ trước<br>2. Nhập đúng địa chỉ email của tài khoản |
| Hậu điều kiện | 1. Đổi được mật khẩu mới trên trang web của Firebase<br>2. Quay lại ứng dụng đăng nhập thành công bằng mật khẩu mới<br>3. Lần sau đăng nhập vẫn vào được hệ thống bằng mật khẩu mới |

**Luồng cơ bản**

| Bước | Hành động của tác nhân | Phản hồi của hệ thống |
| --- | --- | --- |
| 1 | Chọn "đăng nhập" tại trang chủ | Hiển thị form đăng nhập cho người dùng |
| 2 | Chọn "quên mật khẩu" ở trên form | Hệ thống hiển thị màn hình "Quên mật khẩu" gồm ô nhập Email và nút "Gửi liên kết". |
| 3 | Khách nhập email của tài khoản và nhấn "Gửi liên kết". | Hệ thống kiểm tra định dạng email. |
| 4 |  | Hệ thống hiển thị thông báo đã gửi liên kết đặt lại mật khẩu, nhắc khách kiểm tra hộp thư và quay về màn hình đăng nhập (A01). Nếu email đã được đăng ký, Firebase gửi email chứa liên kết đặt lại mật khẩu đến hộp thư của khách (do cơ chế chống dò tài khoản, hệ thống không báo email có tồn tại hay không). |
| 5 | Khách kiểm tra hộp thư và bấm vào liên kết đặt lại mật khẩu trong thư. | Trình duyệt mở trang web đặt lại mật khẩu của Firebase. Firebase kiểm tra liên kết còn hợp lệ, còn hiệu lực và hiển thị form nhập mật khẩu mới. |
| 6 | Khách nhập mật khẩu mới trên trang web của Firebase và nhấn xác nhận. | Trang web của Firebase kiểm tra tính hợp lệ của mật khẩu mới (theo chính sách mật khẩu của Firebase, mặc định tối thiểu 6 ký tự) và lưu mật khẩu mới. |
| 7 | Khách quay lại ứng dụng PUBGApp và đăng nhập bằng mật khẩu mới. | Hệ thống xác thực thông tin đăng nhập mới và chuyển người dùng đến Trang chủ. |

**Luồng thay thế**

| Bước | Hành động của tác nhân | Phản hồi của hệ thống |
| --- | --- | --- |
| 3a | Khách nhấn nút quay lại hoặc "Đăng nhập" | Hệ thống trở về form đăng nhập, không gửi email. |
| 4a | Khách chưa nhận được thư nên quay lại nhập email và nhấn "Gửi liên kết" một lần nữa | Hệ thống gửi một liên kết đặt lại mật khẩu mới vào Gmail của khách. Khách tiếp tục thực hiện bước 5 với liên kết mới. |

**Luồng ngoại lệ**

| Bước | Hành động của tác nhân | Phản hồi của hệ thống |
| --- | --- | --- |
| 3b | Nhập email sai định dạng hoặc bỏ trống | Hệ thống báo lỗi màu đỏ dưới ô nhập email ("Email không đúng định dạng" hoặc "Vui lòng nhập email"), không gửi yêu cầu và giữ nguyên màn hình. Nếu nhập email chưa đăng ký, hệ thống vẫn hiện thông báo gửi thành công chung để chống dò tài khoản (email enumeration protection) nhưng Firebase không gửi thư. |
| 3c | Gửi quá nhiều yêu cầu liên tiếp | Hệ thống thông báo tạm khóa chức năng này trong vài phút. |
| 5a | Bấm liên kết không hợp lệ hoặc đã hết hiệu lực | Trang web của Firebase từ chối đặt lại mật khẩu, thông báo liên kết không hợp lệ hoặc đã hết hạn. Khách cần yêu cầu gửi lại liên kết mới từ ứng dụng. |
| 6a | Mật khẩu mới không hợp lệ theo quy định của Firebase | Trang web của Firebase hiển thị thông báo lỗi và yêu cầu nhập lại mật khẩu hợp lệ. |

**Quy tắc nghiệp vụ**

- BR1. Chỉ tài khoản đã đăng ký mới nhận được thư đặt lại mật khẩu. Ứng dụng không báo email có tồn tại trong hệ thống hay không để chống dò tài khoản (email enumeration protection).
- BR2. Liên kết đặt lại mật khẩu chỉ được gửi tới email của tài khoản và chỉ có hiệu lực trong một thời hạn nhất định do Firebase quy định kể từ khi được gửi. Hệ thống không gửi mã qua tin nhắn SMS.
- BR3. Mật khẩu mới được thiết lập trên trang web của Firebase theo chính sách mật khẩu của Firebase (mặc định tối thiểu 6 ký tự). Sau khi đặt lại mật khẩu thành công trên trang web, người dùng quay lại ứng dụng để đăng nhập.

**Yêu cầu phi chức năng**

- NFR1. Mật khẩu mới phải được mã hóa trước khi lưu trữ. Liên kết đặt lại mật khẩu chỉ được gửi tới đúng hòm thư của tài khoản.
- NFR2. Màn hình và dữ liệu đang nhập dở phải giữ nguyên trạng thái, không bị mất dữ liệu khi người dùng xoay ngang hoặc xoay dọc thiết bị.

## UC04 Đăng xuất

| Mục | Nội dung |
| --- | --- |
| Mã use case | UC04 |
| Tên use case | Đăng xuất |
| Tác nhân | Người dùng (đã đăng nhập) |
| Mô tả | Cho phép người dùng có thể đăng xuất ra khỏi tài khoản đang sử dụng |
| Mức ưu tiên | Cao |
| Khởi phát | Người dùng nhấn nút "Đăng xuất" tại màn hình Quản lý thông tin cá nhân (A04) |
| Tiền điều kiện | 1. Người dùng đã đăng nhập vào hệ thống. |
| Hậu điều kiện | 1. Người dùng đăng xuất được khỏi tài khoản hiện tại<br>2. Người dùng được đưa về trang chủ ở chế độ khách |

**Luồng cơ bản**

| Bước | Hành động của tác nhân | Phản hồi của hệ thống |
| --- | --- | --- |
| 1 | Người dùng nhấn nút "Đăng xuất" trên màn hình Quản lý thông tin cá nhân (A04) | Hiển thị màn hình xác nhận đăng xuất khỏi tài khoản |
| 2 | Người dùng ấn xác nhận đăng xuất | Hệ thống chuyển người dùng về trang chủ, trạng thái chưa đăng nhập (khách) |

**Luồng thay thế**

Không có.

**Luồng ngoại lệ**

| Bước | Hành động của tác nhân | Phản hồi của hệ thống |
| --- | --- | --- |
| 2a | Chưa ấn xác nhận đăng xuất | Hệ thống giữ nguyên trạng thái đăng nhập của tài khoản |

**Quy tắc nghiệp vụ**

- BR1. Chỉ người dùng đang đăng nhập mới đăng xuất được.
- BR2. Hệ thống chỉ đăng xuất sau khi người dùng xác nhận.
- BR3. Đăng xuất chỉ kết thúc phiên đăng nhập, không xóa tài khoản và không làm mất dữ liệu của tài khoản. Việc xóa tài khoản thuộc UC08.

**Yêu cầu phi chức năng**

- NFR1. Sau khi đăng xuất, người dùng phải đăng nhập lại mới dùng được các chức năng dành cho người dùng đã đăng nhập.
- NFR2. Màn hình xác nhận phải giữ nguyên trạng thái khi người dùng xoay ngang hoặc xoay dọc thiết bị.

## UC05 Quản lý thông tin cá nhân

| Mục | Nội dung |
| --- | --- |
| Mã use case | UC05 |
| Tên use case | Quản lý thông tin cá nhân |
| Tác nhân | Người dùng (đã đăng nhập) |
| Mô tả | Cho phép người dùng có thể xem, chỉnh sửa thông tin cá nhân thông qua chức năng cài đặt trên hệ thống. Cũng tại đây, người dùng bật tắt từng loại thông báo và đổi mật khẩu khi đang đăng nhập. Tài khoản đăng nhập bằng Google không có mật khẩu nên không có mục đổi mật khẩu. |
| Mức ưu tiên | Cao |
| Khởi phát | Người dùng nhấn "sửa thông tin " tại màn hình Quản lý thông tin cá nhân (A04) |
| Tiền điều kiện | 1. Người dùng đã đăng nhập vào hệ thống. |
| Hậu điều kiện | 1. Người dùng sửa được thông tin mà mình muốn, hệ thống cập nhật lại và hiển thị đúng những gì người dùng vừa thay đổi |

**Luồng cơ bản**

| Bước | Hành động của tác nhân | Phản hồi của hệ thống |
| --- | --- | --- |
| 1 | Người dùng nhấn nút "Sửa thông tin" trên màn hình Quản lý thông tin cá nhân (A04) | Hệ thống hiển thị màn hình thông tin cá nhân với thông tin hiện tại của người dùng để người dùng xem |
| 2 | Người dùng nhập lại các thông tin cá nhân cần sửa, | Hệ thống từ chối việc chỉnh sửa CCCD, Gmail; số điện thoại được phép sửa |
| 3 | Người dùng nhấn nút "Lưu những thay đổi" | Hệ thống kiểm tra dữ liệu nhập vào đúng định dạng(chữ, số, gmail). Hệ thống ghi nhận những thay đổi và chuyển về màn hình thông tin cá nhân |

**Luồng thay thế**

| Bước | Hành động của tác nhân | Phản hồi của hệ thống |
| --- | --- | --- |
| 1a | Bật tắt từng loại thông báo |  |
| 1a.1 | Người dùng nhấn "Cài đặt thông báo" trên màn hình Quản lý thông tin cá nhân (A04) | Hệ thống hiển thị công tắc bật hoặc tắt cho từng loại thông báo: tin nhắn mới, đề nghị giá mới, cập nhật giao dịch, theo lựa chọn hiện tại. |
| 1a.2 | Người dùng gạt công tắc của loại thông báo muốn bật hoặc tắt | Hệ thống lưu lựa chọn. Loại thông báo đã tắt không còn hiện trong danh sách thông báo (UC07). |
| 1b | Đổi mật khẩu khi đang đăng nhập (chỉ với tài khoản đăng nhập bằng email, xem BR6) |  |
| 1b.1 | Người dùng nhấn "Đổi mật khẩu" trên màn hình Quản lý thông tin cá nhân (A04) | Hệ thống hiển thị các ô: Mật khẩu hiện tại, Mật khẩu mới, Xác nhận mật khẩu mới và nút "Đổi mật khẩu". |
| 1b.2 | Người dùng nhập lại mật khẩu hiện tại, nhập mật khẩu mới và xác nhận mật khẩu mới, sau đó nhấn "Đổi mật khẩu" | Hệ thống kiểm tra mật khẩu hiện tại, kiểm tra mật khẩu mới hợp lệ và khớp với ô xác nhận. |
| 1b.3 |  | Hệ thống cập nhật mật khẩu mới và thông báo đổi mật khẩu thành công. |

**Luồng ngoại lệ**

| Bước | Hành động của tác nhân | Phản hồi của hệ thống |
| --- | --- | --- |
| 3a | Người dùng nhập sai định dạng dữ liệu | Hệ thống từ chối, thông báo dữ liệu nhập phải là chữ cái, số. |
| 3b | Người dùng không chọn "lưu những thay đổi" | Hệ thống vẫn giữ nguyên thông tin cũ, không ghi nhận thông tin mới |
| 1b.2a | Người dùng nhập sai mật khẩu hiện tại | Hệ thống báo lỗi dưới ô Mật khẩu hiện tại và không đổi mật khẩu. |
| 1b.2b | Mật khẩu mới không hợp lệ hoặc không khớp với ô xác nhận | Hệ thống báo lỗi dưới ô tương ứng và không đổi mật khẩu. |

**Quy tắc nghiệp vụ**

- BR1. Người dùng chỉ xem và sửa được thông tin cá nhân của chính mình.
- BR2. Số điện thoại được chỉnh sửa. Số CCCD và Gmail không được chỉnh sửa trong chức năng này.
- BR3. Dữ liệu nhập vào phải đúng định dạng (chữ, số, gmail); thay đổi chỉ được ghi nhận khi người dùng chọn lưu.
- BR4. Người dùng chỉ bật hoặc tắt được ba loại thông báo: tin nhắn mới, đề nghị giá mới, cập nhật giao dịch.
- BR5. Đổi mật khẩu phải nhập đúng mật khẩu hiện tại; mật khẩu mới phải có tối thiểu 8 ký tự, gồm cả chữ và số, và xác nhận phải giống hoàn toàn mật khẩu mới.
- BR6. Tài khoản đăng nhập bằng Google không có mật khẩu, nên mục "Đổi mật khẩu" bị ẩn với tài khoản đó; luồng 1b chỉ áp dụng cho tài khoản đăng nhập bằng email.

**Yêu cầu phi chức năng**

- NFR1. Mật khẩu mới phải được mã hóa trước khi lưu trữ.
- NFR2. Màn hình và toàn bộ dữ liệu đang nhập dở phải giữ nguyên trạng thái, không bị mất dữ liệu khi người dùng xoay ngang hoặc xoay dọc thiết bị.

## UC06 Quản lý địa chỉ giao nhận

| Mục | Nội dung |
| --- | --- |
| Mã use case | UC06 |
| Tên use case | Quản lý địa chỉ giao nhận |
| Tác nhân | Người dùng (đã đăng nhập) |
| Mô tả | Cho phép người dùng thực hiện các chức năng quản lý địa chỉ giao, nhận như xem danh sách, thêm, sửa, xóa địa chỉ. Luồng cơ bản là thêm địa chỉ mới; sửa và xóa là các luồng thay thế. |
| Mức ưu tiên | Cao |
| Khởi phát | Nhấn "địa chỉ" trong màn hình Quản lý thông tin cá nhân (A04) |
| Tiền điều kiện | 1. Thiết bị có kết nối mạng Internet.<br>2. Ứng dụng đã được cài đặt và đang ở trạng thái đã đăng nhập. |
| Hậu điều kiện | Người dùng có thể thêm, sửa, xóa địa chỉ của mình, hệ thống lưu và hiển thị không thay đổi. |

**Luồng cơ bản**

| Bước | Hành động của tác nhân | Phản hồi của hệ thống |
| --- | --- | --- |
| 1 | Người dùng nhấn "địa chỉ" trong màn hình Quản lý thông tin cá nhân (A04) | Hệ thống hiển thị danh sách địa chỉ hiện tại kèm thông tin (sdt,<br>họ tên) và nút "chỉnh sửa địa chỉ", "thêm". |
| 2 | Người dùng nhấn "thêm" | Hệ thống hiển thị form nhập thông tin địa chỉ mới gồm số,đường,quận/phường, tỉnh/thành phố, nước. |
| 3 | Người dùng nhập đầy đủ thông tin và ấn xác nhận | Hệ thống kiểm tra dữ liệu nhập vào đúng định dạng, đủ các mục<br>cần điền và lưu vào danh sách địa chỉ của người dùng |
| 4 |  | Hệ thống cập nhật và hiển thị lên danh sách |

**Luồng thay thế**

| Bước | Hành động của tác nhân | Phản hồi của hệ thống |
| --- | --- | --- |
| 1a | Chỉnh sửa địa chỉ đã có |  |
| 1a.1 | Người dùng nhấn "chỉnh sửa địa chỉ" | Hệ thống hiển thị nút select để người dùng chọn địa chỉ muốn<br>thực hiện chỉnh sửa |
| 1b | Sửa địa chỉ (sau bước 1a.1) |  |
| 1b.1 | Người dùng chọn địa chỉ muốn sửa và chọn<br>"chỉnh sửa thông tin" | Hệ thống hiển thị các thông tin hiện tại của địa chỉ được chọn |
| 1b.2 | Người dùng nhập dữ liệu mới vào các ô thông<br>tin cần sửa và xác nhận | Hệ thống kiểm tra dữ liệu nhập vào đúng định dạng, đủ các<br>mục yêu cầu |
| 1b.3 |  | Hệ thống cập nhật lại địa chỉ và hiển thị |
| 1c | Xóa địa chỉ (sau bước 1a.1) |  |
| 1c.1 | Người dùng chọn địa chỉ và chọn "xóa" | Hệ thống hiển thị pop up yêu cầu người dùng xác nhận "xóa địa<br>chỉ" |
| 1c.2 | Người dùng chọn "xác nhận" | Hệ thống xóa thông tin địa chỉ được chọn, lưu và cập nhật hiển<br>thị |

**Luồng ngoại lệ**

| Bước | Hành động của tác nhân | Phản hồi của hệ thống |
| --- | --- | --- |
| 1c.2a | Người dùng xóa địa chỉ đang có giao dịch ở trạng thái đang thực hiện | Hệ thống báo đang có giao dịch được thực hiện với địa chỉ này<br>và không cho xóa |
| 3a | Người dùng nhập dữ liệu không đúng định dạng,<br>không đủ các mục yêu cầu (tại bước 3 hoặc bước 1b.2) | Hệ thống yêu cầu nhập lại và nút xác nhận bị vô hiệu hóa |
| 3b | Mất mạng (tại bước 3 hoặc bước 1b.2) | Hệ thống không lưu địa chỉ mới, chỉ giữ nguyên dữ liệu đang<br>nhập |

**Quy tắc nghiệp vụ**

- BR1. Mỗi địa chỉ phải điền đủ các mục (số, đường, quận/phường, tỉnh/thành phố, nước) và đúng định dạng thì mới được lưu.
- BR2. Địa chỉ đang có giao dịch chưa kết thúc không được xóa.
- BR3. Người dùng chỉ xem, thêm, sửa, xóa được địa chỉ của chính mình.
- BR4. Việc xóa địa chỉ chỉ thực hiện sau khi người dùng xác nhận.

**Yêu cầu phi chức năng**

- NFR1. Khi mất mạng, hệ thống không lưu thay đổi nhưng giữ nguyên dữ liệu người dùng đang nhập.
- NFR2. Màn hình và toàn bộ dữ liệu đang nhập dở phải giữ nguyên trạng thái, không bị mất dữ liệu khi người dùng xoay ngang hoặc xoay dọc thiết bị.

## UC07 Xem thông báo

| Mục | Nội dung |
| --- | --- |
| Mã use case | UC07 |
| Tên use case | Xem thông báo |
| Tác nhân | Người dùng (đã đăng nhập) |
| Mô tả | Cho phép người dùng xem danh sách thông báo về đề nghị giá, giao dịch, tin nhắn, đánh giá, khiếu nại và cảnh báo; đánh dấu thông báo đã đọc; mở một thông báo thì được đưa tới màn hình liên quan. |
| Mức ưu tiên | Trung bình |
| Khởi phát | Người dùng nhấn biểu tượng chuông trên thanh trên của trang chủ |
| Tiền điều kiện | 1. Người dùng đã đăng nhập vào hệ thống.<br>2. Thiết bị có kết nối mạng Internet. |
| Hậu điều kiện | 1. Danh sách thông báo của người dùng được hiển thị.<br>2. Thông báo người dùng đã mở được đánh dấu là đã đọc, số thông báo chưa đọc giảm tương ứng.<br>3. Người dùng được đưa tới màn hình liên quan đến thông báo đã mở. |

**Luồng cơ bản**

| Bước | Hành động của tác nhân | Phản hồi của hệ thống |
| --- | --- | --- |
| 1 | Người dùng nhấn biểu tượng chuông trên thanh trên của trang chủ | Hệ thống mở màn hình Thông báo (C06) và hiển thị danh sách thông báo của người dùng, mới nhất ở trên cùng. Mỗi dòng gồm biểu tượng phân loại, tiêu đề in đậm, nội dung tóm tắt và thời gian nhận; thông báo chưa đọc có chấm tròn màu cam ở bên phải. |
| 2 | Người dùng nhấn vào một thông báo | Hệ thống đánh dấu thông báo đó là đã đọc: chấm tròn màu cam biến mất và số thông báo chưa đọc trên biểu tượng chuông giảm đi. |
| 3 |  | Hệ thống chuyển người dùng tới màn hình tương ứng với loại thông báo (xem Quy tắc nghiệp vụ BR4). |
| 4 | Người dùng nhấn nút quay lại để trở về màn hình Thông báo | Hệ thống hiển thị lại danh sách; thông báo vừa mở đã ở trạng thái đã đọc. |

**Luồng thay thế**

| Bước | Hành động của tác nhân | Phản hồi của hệ thống |
| --- | --- | --- |
| 1a | Người dùng chưa có thông báo nào | Hệ thống hiển thị hình minh họa và dòng chữ "Bạn chưa có thông báo mới". |
| 1b | Người dùng kéo xuống cuối danh sách | Hệ thống tải thêm các thông báo cũ hơn và hiển thị nối tiếp danh sách. |
| 1c | Người dùng nhấn nút quay lại | Hệ thống trở về màn hình đã gọi, không thay đổi gì. |
| 2a | Người dùng nhấn vào thông báo đã đọc từ trước | Hệ thống chuyển tới màn hình tương ứng như bước 3, không ghi nhận lại việc đánh dấu đã đọc. |

**Luồng ngoại lệ**

| Bước | Hành động của tác nhân | Phản hồi của hệ thống |
| --- | --- | --- |
| 1d | Mất kết nối mạng khi mở màn hình Thông báo | Hệ thống hiển thị thông báo "Không có kết nối mạng. Vui lòng thử lại." và không cập nhật danh sách. |
| 2b | Tài khoản không còn được phép thực hiện thao tác (không còn ACTIVE) nên không ghi nhận được việc đánh dấu đã đọc | Hệ thống hiển thị thông báo "Tài khoản của bạn hiện không thể thực hiện thao tác này." và không chuyển màn hình. |
| 3a | Nội dung mà thông báo trỏ tới không còn (ví dụ tin đã bị ẩn) | Màn hình đích hiển thị thông báo nội dung không còn, người dùng có thể quay lại danh sách. |

**Quy tắc nghiệp vụ**

- BR1. Mỗi người dùng chỉ thấy các thông báo gửi cho chính mình.
- BR2. Loại thông báo mà người dùng đã tắt trong Cài đặt thông báo (UC05) thì không hiện trong danh sách.
- BR3. Thông báo thuộc các nhóm: tin nhắn, đề nghị giá, giao dịch, thanh toán, đánh giá, khiếu nại, cảnh báo, kết quả xác thực tài khoản và tin bị admin xử lý.
- BR4. Màn hình đến khi mở thông báo phụ thuộc loại thông báo: tin nhắn mới, đề nghị giá mới và kết quả đề nghị giá tới màn hình Trò chuyện và đề nghị giá (C02); cập nhật giao dịch và cập nhật thanh toán tới màn hình Chi tiết giao dịch (C03); đánh giá mới tới hồ sơ uy tín của chính người dùng ở chế độ chỉ xem (D03); kết quả khiếu nại tới mục "Khiếu nại của tôi" (D07); cảnh báo hoặc khóa tài khoản và kết quả xác thực tài khoản tới Hồ sơ cá nhân (D01); tin bị admin ẩn, gỡ hoặc khôi phục tới Kệ hàng của tôi (B05).
- BR5. Mở một thông báo chưa đọc thì thông báo đó chuyển sang đã đọc.

**Yêu cầu phi chức năng**

- NFR1. Danh sách thông báo được sắp theo thời gian, mới nhất ở trên cùng, và tải thêm khi người dùng kéo xuống cuối danh sách.
- NFR2. Màn hình và vị trí cuộn trong danh sách phải giữ nguyên trạng thái, không bị mất khi người dùng xoay ngang hoặc xoay dọc thiết bị.

## UC08 Xóa tài khoản

| Mục | Nội dung |
| --- | --- |
| Mã use case | UC08 |
| Tên use case | Xóa tài khoản |
| Tác nhân | Người dùng (có tài khoản và đã đăng nhập) |
| Mô tả | Cho phép người dùng đã đăng nhập vào ứng dụng PUBGApp xóa tài khoản của mình sau khi xác thực lại bằng mật khẩu (tài khoản Google thì đăng nhập lại bằng Google). Đây là xóa mềm: tài khoản chuyển sang trạng thái DELETED, hồ sơ và tin đăng bị ẩn, còn lịch sử giao dịch và đánh giá được giữ lại cho đối tác. |
| Mức ưu tiên | Cao |
| Khởi phát | Người dùng nhấn "xóa tài khoản" |
| Tiền điều kiện | 1. Thiết bị có kết nối mạng Internet.<br>2. Ứng dụng đã được cài đặt và đang ở trạng thái đã đăng nhập. |
| Hậu điều kiện | 1. Tài khoản chuyển sang trạng thái DELETED; hồ sơ và tin đăng của người dùng bị ẩn, thông tin đăng nhập bị xóa; lịch sử giao dịch và đánh giá được giữ lại cho đối tác.<br>2. Người dùng chuyển trạng thái sang khách (chưa đăng nhập) và được điều hướng tới trang chủ |

**Luồng cơ bản**

| Bước | Hành động của tác nhân | Phản hồi của hệ thống |
| --- | --- | --- |
| 1 | Người dùng nhấn "xóa tài khoản" ở trên màn hình cài đặt tài khoản (A05) | Hiển thị pop up cảnh báo hậu quả của việc xóa tài khoản (ẩn hồ sơ và tin đăng, xóa thông tin đăng nhập, giữ lịch sử giao dịch và đánh giá cho đối tác), yêu cầu người dùng nhấn "xác nhận" |
| 2 | Người dùng nhấn "xác nhận" ở trên pop up | Hệ thống yêu cầu xác thực lại bằng cách hiển thị ô nhập lại mật khẩu hiện tại. |
| 3 | Người dùng nhập mật khẩu hiện tại và nhấn "Xác nhận". | Hệ thống xác thực lại người dùng: kiểm tra mật khẩu vừa nhập có đúng không. |
| 4 |  | Hệ thống kiểm tra tài khoản không còn giao dịch chưa kết thúc, rồi trong cùng một lần ghi đặt tài khoản sang trạng thái DELETED, ẩn các tin đăng đang bán và từ chối các đề nghị giá đang chờ. |
| 5 |  | Hệ thống xóa thông tin đăng nhập của tài khoản và đăng xuất người dùng. |
| 6 |  | Hệ thống điều hướng màn hình người dùng về trang chủ ở trạng thái khách |

**Luồng thay thế**

| Bước | Hành động của tác nhân | Phản hồi của hệ thống |
| --- | --- | --- |
| 1a | Ấn nút quay lại | Hệ thống trở về giao diện màn hình trước đó |
| 2a | Tài khoản đăng nhập bằng Google |  |
| 2a.1 | Người dùng nhấn "xác nhận" ở trên pop up | Hệ thống mở hộp thoại đăng nhập lại bằng Google. |
| 2a.2 | Người dùng chọn lại tài khoản Google đang dùng | Hệ thống xác thực lại thành công và tiếp tục bước 4 của luồng cơ bản. |

**Luồng ngoại lệ**

| Bước | Hành động của tác nhân | Phản hồi của hệ thống |
| --- | --- | --- |
| 2b | Không nhấn "xác nhận" để xóa tài khoản | Hệ thống không yêu cầu xác thực lại và không thay đổi tài khoản. |
| 3a | Nhập mật khẩu không đúng | Hệ thống báo lỗi dưới ô mật khẩu và giữ nguyên màn hình để người dùng nhập lại. |
| 4a | Tài khoản còn giao dịch chưa kết thúc | Hệ thống không cho xóa, nêu rõ lý do, tài khoản và dữ liệu giữ nguyên trạng thái ACTIVE. |
| 4b | Mất kết nối mạng | Hệ thống hiển thị thông báo "Mất kết nối mạng, vui lòng kiểm tra lại đường truyền" và không thay đổi tài khoản. |
| 5a | Không xóa được thông tin đăng nhập | Hệ thống thử xóa lại. Nếu tài khoản (đã ở trạng thái DELETED) đăng nhập lại thì bị báo "Tài khoản đã xóa" và bị đăng xuất. |

**Quy tắc nghiệp vụ**

- BR1. Chỉ chủ tài khoản đang đăng nhập mới xóa được tài khoản của mình.
- BR2. Người dùng phải xác thực lại (nhập lại mật khẩu, hoặc đăng nhập lại bằng Google) trước khi tài khoản bị xóa.
- BR3. Không cho xóa tài khoản khi người dùng còn giao dịch chưa kết thúc.
- BR4. Xóa tài khoản là xóa mềm: tài khoản chuyển sang trạng thái DELETED, không xóa dữ liệu khỏi hệ thống. DELETED là trạng thái cuối, không khôi phục được.
- BR5. Khi tài khoản bị xóa, hồ sơ và tin đăng của người dùng bị ẩn (tin đang bán chuyển sang ẩn) và các đề nghị giá đang chờ bị từ chối.
- BR6. Lịch sử giao dịch và đánh giá được giữ lại cho đối tác.

**Yêu cầu phi chức năng**

- NFR1. Mật khẩu người dùng nhập lại để xác thực chỉ dùng cho việc xác thực, không được lưu lại.
- NFR2. Việc đặt trạng thái DELETED, ẩn tin đăng và từ chối đề nghị giá phải thực hiện trong cùng một lần ghi, không để lại trạng thái làm dở.
- NFR3. Màn hình và toàn bộ dữ liệu đang nhập dở phải giữ nguyên trạng thái, không bị mất dữ liệu khi người dùng xoay ngang hoặc xoay dọc thiết bị.
- NFR4. Tài khoản đã xóa không thể đăng nhập lại, không thể được tìm kiếm thấy và không thực hiện được các chức năng khác.

## Ghi chú sửa đổi

Mọi chỗ khác với bản nháp của nhóm, mỗi dòng một chỗ. Tiền tố cho biết lý do: D1 đến D11 là quyết định của leader ngày 2026-10-22; "Lỗi" là lỗi của bản nháp; "Điền" là điền mục để trống theo luật viết 3; "Đánh số" là đánh số lại theo luật viết 4; "Mới" là phần viết mới hoàn toàn.

### Toàn tệp

- Đánh số. Luồng thay thế và ngoại lệ đổi sang dạng `<số bước>a`, `<số bước>b`, bước con dạng `1a.1`. Mọi bước rỗng (dòng chỉ có mã) và dòng tiêu đề lặp bị bỏ; mọi "Tại bước N" trỏ tới bước có thật.
- Lỗi. Gõ sai "ngừoi dùng" thành "người dùng", "Hệ thổng" thành "Hệ thống", "đăng kí" thành "đăng ký" (UC08 đã bỏ câu chứa chữ này).
- Lỗi. Chính tả "xoá" đổi thành "xóa" ở toàn tệp cho thống nhất với tên use case "Xóa tài khoản".
- Lỗi. "màn hình cài đặt (MH04)" đổi thành "màn hình Quản lý thông tin cá nhân (A04)" ở UC04, UC05, UC06, vì mã màn hình trong tài liệu giao diện là A04 (luật viết 6). Theo tài liệu giao diện, các nút "Đăng xuất", "Sửa thông tin", "địa chỉ", "Cài đặt thông báo", "Đổi mật khẩu" nằm ở A04; "Xóa tài khoản" nằm ở A05.
- D9. Bản nháp không coi Google hay Firebase là tác nhân; giữ nguyên, không thêm tác nhân phụ nào.

### UC01 Đăng ký tài khoản

- D1. Mô tả: bỏ "xác thực bảo mật qua mã OTP gửi về email", thay bằng "xác thực email bằng liên kết do hệ thống gửi về email".
- D1. Luồng cơ bản bước 3 và 4: bỏ mã OTP 6 chữ số, đồng hồ đếm ngược 60 giây và màn hình "Xác thực OTP". Bước 3 nay gửi liên kết xác thực và hiện màn hình "Kiểm tra hộp thư" có nút "Gửi lại liên kết"; bước 4 nay là khách bấm liên kết trong thư, ứng dụng mở lại và xác thực. Bước 5 vẫn là "xác thực xong mới điền thông tin".
- D1. Luồng thay thế 1a.2: bỏ "gửi mã OTP xác nhận về Gmail" của tài khoản Google, thay bằng chuyển thẳng sang màn hình thiết lập thông tin. 1a.3 đổi từ "tiếp tục bước 4" thành "tiếp tục bước 5" theo đánh số mới.
- D1. Luồng thay thế "4a Yêu cầu gửi lại mã OTP" (4a1) thành 3a, 3a.1 "Yêu cầu gửi lại liên kết": bỏ điều kiện "bộ đếm ngược về 0" và việc khởi động lại bộ đếm; nút thành "Gửi lại liên kết". Bỏ câu "hủy mã OTP cũ" vì chưa chắc liên kết cũ bị vô hiệu khi gửi liên kết mới.
- D1. Ngoại lệ 4a "Nhập sai mã OTP" thành "Bấm liên kết xác thực không hợp lệ hoặc đã hết hiệu lực"; câu báo lỗi đổi tương ứng.
- D1. BR3 (mã OTP 6 chữ số, 60 giây) viết lại thành quy tắc về liên kết xác thực. Thêm BR8 ghi lại thứ tự "xác thực email xong mới điền thông tin" (bản nháp chỉ có ngầm trong luồng).
- D1. NFR1 và NFR2: "mã OTP" đổi thành "liên kết xác thực"; giữ con số 5 giây của tác giả.
- D11. Thêm bước 7 "chọn danh mục quan tâm" vào luồng cơ bản. Bước 6 đổi nút thành "Tiếp tục" để sang bước 7; nút "Hoàn tất đăng ký" chuyển sang bước 7; tạo tài khoản thành bước 8. Hậu điều kiện thêm mục 3 (lưu danh mục quan tâm). Ngoại lệ 6b nêu rõ áp dụng cho bước 6 và 7.
- Lỗi. Dòng "1a Ấn nút quay lại" lặp mã 1a với "Đăng ký nhanh bằng tài khoản Google": đánh lại thành 1b.
- Lỗi. Mã 4a dùng cho cả luồng thay thế lẫn ngoại lệ: nay luồng thay thế là 3a, ngoại lệ là 4a.
- Lỗi. Khởi phát nói nút "Tạo tài khoản" còn bước 1 nói nút "Đăng ký": giữ nguyên chữ của tác giả, chưa biết nút nào đúng (xem mục chưa giải quyết).
- Mật khẩu. Bước 6 và BR5 đổi từ "6-14 ký tự, đủ các loại ký tự" thành "tối thiểu 8 ký tự, gồm chữ và số", theo mockup A02 (quyết định leader 2026-10-22).
- Bỏ tên tài khoản. Bước 5 bỏ ô "Tên tài khoản"; BR4 (tên tài khoản duy nhất, 6-30 ký tự) thay bằng quy tắc "tài khoản nhận diện bằng email, không có tên tài khoản riêng", giữ số thứ tự BR để không lệch các tham chiếu (quyết định leader 2026-10-22).

### UC02 Đăng nhập

- Điền. Mức ưu tiên "Không có" đổi thành Cao (đăng nhập là điều kiện để dùng mọi chức năng của người dùng).
- Lỗi. Tác nhân đổi từ "Người dùng" thành "Khách (người dùng đã có tài khoản nhưng chưa đăng nhập)", vì người đang đăng nhập chưa phải người dùng đã đăng nhập; thống nhất với UC03. Các câu khác giữ nguyên.
- Đánh số. Luồng thay thế bản nháp bắt đầu từ bước 2 và dùng số trơn, nay thành 2a, 2a.1 đến 2a.4. Ngoại lệ bản nháp không có mã bước và chữ nằm ở cột Bước, nay là 3a, 3b với mô tả ở cột hành động.
- Mới (lấy từ tài liệu giao diện A01, luật viết 3). Luồng thay thế 2b: đăng nhập Google lần đầu thì sang chế độ hoàn tất hồ sơ. Ngoại lệ 3c: tài khoản đã xóa (DELETED) không vào được, báo "Tài khoản đã xóa". Ngoại lệ 3d: tài khoản bị khóa, báo lý do và thời hạn rồi đăng xuất.
- Điền. BR1 đến BR3 (đăng ký mới đăng nhập được, không đăng nhập bằng số điện thoại, tài khoản DELETED và tài khoản bị khóa không vào được) và NFR2, NFR3 (nút chờ khi đang xử lý, giữ dữ liệu khi xoay) lấy từ tài liệu giao diện A01, STT 2 và quy tắc xoay màn hình của dự án. NFR1 là câu của tác giả, giữ nguyên.
- Bỏ tên tài khoản. Mô tả, Tiền điều kiện, bước 2 và 3, ngoại lệ 3a: "tài khoản" dùng để đăng nhập đổi thành "email". BR1 ghi thêm "không đăng nhập bằng tên tài khoản" (quyết định leader 2026-10-22).

### UC03 Đặt lại mật khẩu

- Lỗi. Luồng cơ bản bước 2 và 3 không có bước nhập email. Thêm bước 3 "nhập email và nhấn Gửi liên kết" (hệ thống kiểm tra định dạng); bước 2 nay mở màn hình "Quên mật khẩu" có ô Email.
- D1 (suy ra, cần leader xác nhận). Bản nháp UC03 cũng dùng mã OTP 6 chữ số, 60 giây và màn hình "Xác thực OTP". Lý do của D1 (Firebase không gửi OTP qua email) áp dụng như nhau, và STT 3 cùng màn hình A03 ghi rõ là gửi liên kết đặt lại mật khẩu qua email, không gửi mã. Nên hai bước OTP cũ thành bước 4 (gửi email chứa liên kết) và bước 5 (bấm liên kết mở trang web Firebase); bước nhập mật khẩu mới và bước đăng nhập lại thành 6, 7.
- Lỗi. Ngoại lệ "Nhập sai OTP / OTP quá hạn ... từ chối đăng nhập" dùng nhầm cụm "từ chối đăng nhập" trong luồng đặt lại mật khẩu. Thay bằng ngoại lệ 5a (liên kết không hợp lệ hoặc hết hạn trên trang web Firebase); ngoại lệ 3b chỉ kiểm tra định dạng email hoặc bỏ trống (nhập email chưa đăng ký vẫn nhận thông báo chung để chống dò tài khoản theo cơ chế email enumeration protection của Firebase), 3c (gửi quá nhiều yêu cầu), và 6a (mật khẩu không hợp lệ trên trang web Firebase).
- Lỗi. Khởi phát bản nháp trùng nguyên văn Tiền điều kiện (khác một cụm "xác thực"); viết lại thành hành động của khách ("chọn Quên mật khẩu trên form đăng nhập"). Tiền điều kiện mục 2 đổi "Nhập đúng tên tài khoản" thành "Nhập đúng địa chỉ email của tài khoản", vì hệ thống gửi thư theo email.
- Điền. Luồng thay thế bản nháp chỉ có bước 1 rỗng: thêm 3a (quay lại) và 4a (gửi lại liên kết). Mô tả bỏ cụm "muốn thay đổi" và chỉ về UC05, vì đổi mật khẩu khi đang đăng nhập nay ở UC05 (D11).
- Điền. BR1 đến BR3 và NFR1, NFR2 lấy từ nội dung use case, STT 3 và hành vi thực tế của Firebase Authentication (Issue #329).
- Mật khẩu và đăng nhập. Bước 5-7 và BR3 mô tả đúng luồng của Firebase: đặt mật khẩu mới trên trang web của Firebase (mặc định tối thiểu 6 ký tự), sau đó quay lại ứng dụng đăng nhập tại A01, không tự động đăng nhập (Issue #329).

### UC04 Đăng xuất

- Lỗi. Khởi phát "Khách nhấn nút" đổi thành "Người dùng nhấn nút", vì tác nhân là người dùng đã đăng nhập.
- Lỗi. Luồng thay thế 1A "xóa tài khoản" thuộc UC08, đã bỏ khỏi UC04; luồng thay thế nay ghi "Không có" (D10 giữ UC04 như một chức năng chính thức).
- Đánh số. Bỏ bước 3 rỗng của luồng cơ bản và các dòng "1", "2" không mã của luồng thay thế; bỏ mục "2." rỗng của Tiền điều kiện. "EF1: Chưa ấn xác nhận đăng xuất" thành ngoại lệ 2a, với mô tả đưa về đúng cột.
- Điền. BR1 đến BR3 và NFR1, NFR2 lấy từ nội dung use case.
- Đăng xuất về trang chủ khách. Hậu điều kiện thêm mục 2: người dùng được đưa về trang chủ ở chế độ khách. Bước 2 của luồng cơ bản vốn đã nói vậy, không sửa (quyết định leader 2026-10-22).

### UC05 Quản lý thông tin cá nhân

- Lỗi. Tên là "quản lý", mô tả có "xem, chỉnh sửa" nhưng luồng cơ bản chỉ sửa. Bước 1 nay nói rõ hệ thống hiển thị thông tin hiện tại để người dùng xem trước khi sửa; Mô tả thêm hai chức năng mới của D11.
- Lỗi. Phản hồi "từ chối việc chỉnh sửa SDT, CCCD, Gmail" ở bước 2 bản nháp xảy ra trước khi lưu. Giữ nguyên chữ của tác giả ở bước 2 và ghi thành quy tắc BR2 (xem mục chưa giải quyết về mâu thuẫn với tài liệu giao diện).
- D11. Thêm luồng thay thế 1a "Bật tắt từng loại thông báo" (1a.1, 1a.2): ba loại tin nhắn mới, đề nghị giá mới, cập nhật giao dịch; loại đã tắt không hiện ở UC07. Thêm BR4.
- D11. Thêm luồng thay thế 1b "Đổi mật khẩu khi đang đăng nhập" (1b.1 đến 1b.3): nhập lại mật khẩu hiện tại rồi nhập mật khẩu mới. Thêm ngoại lệ 1b.2a (sai mật khẩu hiện tại), 1b.2b (mật khẩu mới không hợp lệ hoặc không khớp), BR5 và NFR1.
- Đánh số. Luồng thay thế bản nháp (1A, 1, 2 đều rỗng) bỏ và thay bằng 1a, 1b. "EF1:", "EF2:" thành 3a, 3b gắn với bước 3; mô tả của EF2 vốn nằm lệch cột, nay đưa về đúng cột.
- Điền. BR1, BR3 và NFR2 lấy từ nội dung use case và quy tắc xoay màn hình của dự án.
- Số điện thoại, CCCD. Bước 2 và BR2 sửa: số điện thoại sửa được, số CCCD và Gmail vẫn không sửa được (quyết định leader 2026-10-22).
- Mật khẩu. BR5 theo quy tắc "tối thiểu 8 ký tự, gồm chữ và số" (quyết định leader 2026-10-22).
- Tài khoản Google. Mô tả thêm câu "tài khoản đăng nhập bằng Google không có mật khẩu nên không có mục đổi mật khẩu"; luồng thay thế 1b ghi chỉ áp dụng cho tài khoản đăng nhập bằng email; thêm BR6 ẩn mục "Đổi mật khẩu" với tài khoản Google (quyết định leader 2026-10-22).

### UC06 Quản lý địa chỉ giao nhận

- Lỗi. Tên và mô tả nói thêm, sửa, xóa nhưng luồng cơ bản chỉ thêm. Giữ cấu trúc của tác giả (luồng cơ bản là thêm, sửa và xóa là luồng thay thế) và nói rõ điều đó trong Mô tả. Luồng thay thế tổ chức lại: 1a vào chế độ chỉnh sửa, 1b sửa, 1c xóa (bản nháp chia 2a đến 2e theo thứ tự xóa rồi sửa); chữ các bước giữ nguyên.
- Lỗi. Ngoại lệ "2f" nói "chỉnh sửa địa chỉ" nhưng phản hồi nói "không cho xóa": hiểu là xóa, nay là 1c.2a "xóa địa chỉ đang có giao dịch ở trạng thái đang thực hiện". Cách hiểu này là suy đoán (xem mục chưa giải quyết).
- Đánh số. Mã ngoại lệ 2f, 2h, 2i (nhảy cóc) thành 1c.2a, 3a, 3b. Hai dòng không mã ("Hệ thống cập nhật và hiển thị lên danh sách", "Hệ thống cập nhật lại địa chỉ và hiển thị") thành bước 4 của luồng cơ bản và bước 1b.3.
- Điền. BR1 đến BR4 và NFR1, NFR2 lấy từ nội dung use case và quy tắc xoay màn hình của dự án. Tác nhân thêm "(đã đăng nhập)" cho thống nhất với tiền điều kiện. Khởi phát "địa chỉ trong trang cài đặt" đổi thành màn hình Quản lý thông tin cá nhân (A04).

### UC07 Xem thông báo (viết mới, leader cần duyệt)

- Mới. Toàn bộ use case do tôi viết từ đầu vì bản nháp không có UC07 (nhảy từ UC06 sang UC08). Nguồn: STT 67, tài liệu giao diện C06 (`NotificationListFragment`, mở từ chuông ở B01) và STT 10 (loại đã tắt không hiện). Luồng cơ bản 4 bước: mở chuông, nhấn thông báo (đánh dấu đã đọc), chuyển màn hình tương ứng, quay lại.
- Mới. Mức ưu tiên Trung bình là do tôi chọn. Bảng "loại thông báo, màn hình đích" của C06 được viết thành BR4 bằng lời, bỏ tên enum.
- Mới. Luồng thay thế (danh sách trống, tải thêm, quay lại, mở thông báo đã đọc) và ngoại lệ (mất mạng, tài khoản không còn ACTIVE, nội dung đích không còn) lấy từ C06 và các nguyên tắc chung của dự án; không có trong bản nháp của nhóm.
- Mới. Mô tả bám câu của STT 67; BR3 thêm "thanh toán", "kết quả xác minh người bán" và "tin bị admin xử lý" vì C06 liệt kê các loại này. Thông báo hệ thống do admin gửi hàng loạt không được nhắc tới (phát triển sau, luật R17).
- Từ ngữ. BR3 và BR4 đổi "kết quả xác minh người bán" thành "kết quả xác thực tài khoản". Tên kỹ thuật `verificationStatus` và `sellerVerifications` không xuất hiện trong tệp này (quyết định leader 2026-10-22).

### UC08 Xóa tài khoản

- Lỗi. Bản nháp có hai khối UC08 giống nhau gần như từng chữ. Giữ khối thứ nhất vì đầy đủ hơn. Khối thứ hai chỉ khác ở luồng thay thế: khối thứ nhất có các dòng 1a, 1a.1, 1a.2, 1a.3, 4a, 4a1, khối thứ hai chỉ có 1a.1 và 4a1; cả hai khối đều chỉ có đúng một dòng có nội dung ("1a Ấn nút quay lại"), nên khối bị bỏ không chứa nội dung nào mà khối giữ lại không có.
- Lỗi. BR1 đến BR7 và NFR1 đến NFR3 của cả hai khối UC08 sao nguyên văn từ UC01 (quy tắc đăng ký, mật khẩu, mã OTP). Viết lại toàn bộ thành quy tắc xóa mềm: xác thực lại, không xóa khi còn giao dịch chưa kết thúc, tài khoản DELETED, hồ sơ và tin đăng bị ẩn, đề nghị giá chờ bị từ chối, lịch sử giao dịch và đánh giá giữ lại cho đối tác. NFR4 của tác giả ("không thể đăng nhập lại, không thể tìm kiếm thấy") giữ ý, bỏ cụm "xóa hoàn toàn khỏi hệ thống" vì trái với xóa mềm. BR4 và NFR5 của bản nháp chỉ có mã không có chữ, đã bỏ.
- Lỗi. Mô tả và Hậu điều kiện bản nháp ghi "xóa hoàn toàn khỏi hệ thống" và xác nhận bằng mã OTP gửi qua Gmail. Sửa thành xóa mềm (DELETED) và xác thực lại bằng mật khẩu hoặc đăng nhập lại Google, theo STT 12, mockup A05 và `trang-thai.md` (thứ tự: xác thực lại, kiểm tra giao dịch chưa kết thúc rồi đặt DELETED, ẩn tin và từ chối đề nghị giá trong một lô, xóa đăng nhập, đăng xuất).
- Lỗi. Bước 3 gọi tác nhân là "Khách" trong khi tác nhân là người dùng đã đăng nhập; bước này nay là "Người dùng nhập mật khẩu hiện tại" (bản nháp: nhập OTP).
- D1 (suy ra, cần leader xác nhận). Bản nháp xác nhận xóa bằng OTP gửi qua Gmail; lý do của D1 áp dụng như nhau, và STT 12 cùng A05 nói rõ là nhập lại mật khẩu. Nên bỏ OTP, đồng hồ 60 giây và màn hình "Xác thực OTP"; luồng thay thế 2a là đăng nhập lại bằng Google.
- Đánh số. Bước 6 rỗng bỏ (bản nháp có 6 bước, hai bước cuối nay là 5 và 6 với nội dung mới); ngoại lệ 6a, 6b rỗng bỏ. "4a Không nhấn xác nhận ... không gửi OTP" giữ ý thành 2b ("không yêu cầu xác thực lại"); "2a Nhập mã OTP không đúng" thành 3a (mật khẩu sai). Dòng "1a Ấn nút quay lại" giữ mã 1a vì không còn dòng nào trùng mã. Thêm ngoại lệ 4a (còn giao dịch chưa kết thúc), 4b (mất mạng) và 5a (không xóa được thông tin đăng nhập) theo STT 12, A05 và `trang-thai.md`.
- Điền. Hậu điều kiện nêu rõ tin đăng bị ẩn, để nhất quán với phần còn lại của hệ thống. Nút "xóa tài khoản" nay ghi là ở màn hình cài đặt tài khoản (A05).
- Đăng xuất. UC04 (bước 2) và UC08 (bước 6, Hậu điều kiện 2) vốn đã đưa người dùng về trang chủ ở chế độ khách, đúng quyết định; không sửa chữ ở UC08 (quyết định leader 2026-10-22).

### Mục chưa giải quyết (leader quyết)

- Tài liệu giao diện A01 và A02 vẫn mô tả đăng ký xong thì tạo tài khoản rồi gửi email xác thực sau (người dùng về A01 để đăng nhập, A01 có trạng thái "chưa xác thực email"). D1 đảo thứ tự thành xác thực email trước, điền hồ sơ sau, nên A02 và A01 cần sửa theo, ngoài phạm vi tệp này.
- Quy tắc mật khẩu: bản nháp ghi 6-14 ký tự, đủ các loại ký tự (UC01 BR5); A02 ghi ít nhất 8 ký tự gồm chữ và số. Giữ nguyên bản nháp, UC03 BR3 và UC05 BR5 theo cùng quy tắc đó.
- Bản nháp UC01 có "Tên tài khoản" (BR4: duy nhất, 6-30 ký tự) và UC02, UC03 nói đăng nhập bằng "tài khoản"; A01, A02 và STT 2 chỉ có email và mật khẩu, không có tên tài khoản. Giữ nguyên chữ của nhóm.
- Bản nháp UC01 không có số CCCD, ngày sinh, giới tính, địa chỉ thường trú (STT 9) và đồng ý điều khoản (STT 1) mà A02 bước 2 có; UC05 không có ảnh đại diện (STT 5) và UC06 không có địa chỉ mặc định (STT 6). Không thêm vì ngoài nội dung bản nháp.
- UC05: bản nháp ghi số điện thoại và CCCD không sửa được, trong khi A05 cho sửa số điện thoại ở `EditProfileFragment` và cho sửa thông tin định danh ở `AccountSettingsFragment`. Giữ chữ của nhóm.
- UC01 bước 7: chưa rõ chọn danh mục quan tâm có bắt buộc, tối thiểu mấy danh mục. Chưa ghi giới hạn nào.
- UC01 luồng 1a: tài khoản Google bỏ qua bước liên kết xác thực (email đã được Google xác thực). Đây là suy ra từ cách làm trong tài liệu A02, chưa có trong D1.
- Thời hạn hiệu lực của liên kết xác thực và liên kết đặt lại mật khẩu, và việc liên kết cũ có bị vô hiệu khi gửi lại hay không, chưa kiểm chứng với Firebase Authentication; tệp chỉ ghi "một thời hạn nhất định".
- UC03: Đã giải quyết theo Issue #329 khớp hành vi của Firebase. Khách đặt lại mật khẩu trên trang web Firebase, sau đó quay lại ứng dụng để đăng nhập tại A01 (không tự động đăng nhập). UC04 và UC08 giữ lời của nhóm là về trang chủ ở trạng thái khách; A04 và A05 nói về A01 Đăng nhập.
- UC06 1c.2a: chưa rõ có cho sửa (không chỉ xóa) địa chỉ đang gắn với giao dịch chưa kết thúc không.
- Tài khoản đăng nhập bằng Google không có mật khẩu: luồng đổi mật khẩu ở UC05 chưa nói trường hợp này.
