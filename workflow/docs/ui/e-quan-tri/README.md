# Nhóm E: Quản trị

6 màn hình có ảnh, nộp theo 3 đợt: đợt 1 một màn (E01), đợt 2 hai màn (E02, E03), đợt 3 ba màn (E04, E05, E06). Cộng màn E07 (chưa có ảnh), hai màn hình phụ chưa có ảnh (`AdminListingDetailFragment`, `AdminDealDetailFragment`) và màn D06 (xử lý khiếu nại, ở [nhóm D](../d-ho-so-tin-cay/README.md)). Số trong tài liệu là STT của [bảng đặc tả chức năng](../../product/dac-ta-chuc-nang.md). Điều hướng chung ở [README giao diện](../README.md#điều-hướng).

Toàn bộ nhóm này chạy trong `AdminActivity`, có đồ thị điều hướng riêng, không có thanh điều hướng dưới. Admin đăng nhập ở A01 như mọi người (vào B01) rồi bấm "Khu vực quản trị" trên D01. **Đăng nhập quản trị (UC43, STT 106) không có màn hình riêng.** Admin đăng nhập ở A01 như mọi người, rồi vào khu vực quản trị bằng nút "Khu vực quản trị" trên D01. Tài khoản không có quyền admin thì không thấy nút này; nếu cố mở `AdminActivity` bằng cách khác thì bị từ chối, vì quyền admin do Security Rules quyết định chứ ứng dụng không tự khai quyền.

Mọi màn hình quản trị chỉ dùng để xem và xử lý; phía admin không có nút duyệt giải ngân tay (STT 86 là phát triển sau).

## E01. Admin: Bảng điều khiển

**Ảnh:** ![E01 Admin Bảng điều khiển](e01-admin-bang-dieu-khien.png)

**Lớp:** `AdminDashboardFragment` trong `AdminActivity`. Màn hình gốc của khu vực quản trị.

**Mục đích:** Màn hình đầu tiên admin thấy khi vào khu vực quản trị: liệt kê những việc đang chờ xử lý và dẫn sang các mục quản trị chi tiết.

**Thành phần chính:**
- Tiêu đề "Bảng điều khiển" và dòng chào "Xin chào, Admin"
- Danh sách "Cần xử lý": mỗi dòng là một việc cụ thể (khiếu nại mới, báo cáo mới, yêu cầu xác thực chờ duyệt), kèm nhãn trạng thái "Mới" hoặc "Chờ" và mũi tên điều hướng
- Lưới "Lối tắt quản lý" 6 nút: Người dùng, Tin bị báo cáo, Khiếu nại, Giao dịch, Danh mục, Tài khoản quản trị
- Nút "Về ứng dụng" để quay lại `MainActivity`

**Hành động và điều hướng:**
- Bấm một dòng khiếu nại trong "Cần xử lý": sang chi tiết khiếu nại D06 (`AdminComplaintDetailFragment`)
- Bấm một dòng báo cáo: sang E04 Admin: Kiểm duyệt nội dung
- Bấm một dòng yêu cầu xác thực: sang E02 với bộ lọc "Chờ xác thực"
- Bấm nút "Người dùng": sang E02 Admin: Danh sách người dùng
- Bấm nút "Tin bị báo cáo": sang E04 Admin: Kiểm duyệt nội dung
- Bấm nút "Khiếu nại": sang D06 danh sách khiếu nại (`AdminComplaintListFragment`)
- Bấm nút "Giao dịch": sang E06 Admin: Giám sát giao dịch
- Bấm nút "Danh mục": sang E05 Admin: Quản lý danh mục
- Bấm nút "Tài khoản quản trị": sang E07 Admin: Quản lý tài khoản admin
- Bấm "Về ứng dụng": đóng `AdminActivity`, quay về `MainActivity`

**Chức năng liên quan:** STT 68 (danh sách và lọc người dùng), STT 73 (yêu cầu xác thực), STT 75 (danh sách tin bị báo cáo), STT 87 (danh sách khiếu nại), STT 81 (danh sách giao dịch), STT 79 (danh mục), STT 105 (qua E07).

**Trạng thái đặc biệt:**
- Không có việc nào cần xử lý: hiện dòng chữ báo đã xử lý hết, vẫn giữ lưới lối tắt
- Dòng khiếu nại là khiếu nại đang ở `OPEN` (khiếu nại `NEEDS_EVIDENCE` đang chờ người dùng gửi ảnh, không phải việc của admin). Khiếu nại, báo cáo và yêu cầu xác thực đều nghe theo thời gian thực, nên việc mới hiện ngay không cần tải lại; mỗi loại hiện tối đa 5 dòng
- Mất mạng: hiện danh sách đã tải trước đó từ bộ nhớ đệm, kèm dải báo đang xem ngoại tuyến

**Khác với ảnh:**
- Ảnh có 4 ô số liệu tổng (1.240 người dùng, 850 tin đăng, 420 giao dịch, 18 khiếu nại). Đó là chức năng "Số liệu tổng quan" (STT 92), là **phát triển sau**, chưa làm.
- Ảnh có dòng "Yêu cầu giải ngân, chờ duyệt". Không làm: giải ngân đã đổi sang tự động, admin không duyệt tay (STT 84).
- Ảnh có dòng "tài khoản bị báo cáo" trong "Cần xử lý". Không làm riêng: báo cáo người dùng và tin nhắn hiện chung trong E04.
- Ảnh vẽ lưới 4 nút (Người dùng, Tin đăng, Khiếu nại, Danh mục). Đúng là 6 nút: nút "Tin đăng" đổi tên thành "Tin bị báo cáo" (vì E04 chỉ có tin bị báo cáo), thêm nút "Giao dịch" dẫn tới E06 (ảnh không có đường vào E06). Có thêm nút "Tài khoản quản trị" dẫn sang E07 (ảnh chưa có ô này) và nút "Về ứng dụng".
- Ảnh giả định admin vào E01 ngay sau khi đăng nhập; đúng là vào qua nút "Khu vực quản trị" ở D01.

## E02. Admin: Danh sách người dùng

**Ảnh:** ![E02 Admin Danh sách người dùng](e02-admin-danh-sach-nguoi-dung.png)

**Lớp:** `AdminUserListFragment` trong `AdminActivity`.

**Mục đích:** Cho admin xem toàn bộ người dùng của hệ thống, tìm theo tên hoặc email và lọc theo trạng thái tài khoản, ngày tham gia và khu vực để tìm nhanh người cần xử lý.

**Thành phần chính:**
- Ô tìm kiếm theo tên hoặc email
- Hàng lọc theo trạng thái tài khoản: Tất cả, Đang hoạt động, Khóa tạm, Khóa vĩnh viễn, Đã xóa
- Bộ lọc "Ngày tham gia" (từ ngày, đến ngày) và "Tỉnh hoặc thành phố" (chưa có ảnh)
- Công tắc riêng "Chờ xác thực": chỉ hiện người có yêu cầu xác thực đang chờ duyệt. Dùng được cùng các bộ lọc khác
- Danh sách người dùng, mỗi dòng có ảnh đại diện chữ cái đầu, tên, số điện thoại (che một phần), thông tin phụ (ngày tham gia hoặc số giao dịch), nhãn trạng thái màu theo tình trạng tài khoản

**Hành động và điều hướng:**
- Bấm một người dùng: sang E03 Admin: Chi tiết người dùng
- Nhập từ khóa vào ô tìm kiếm: lọc danh sách theo tên hoặc email ngay tại màn này, dùng được cùng các bộ lọc khác
- Bấm một nút lọc hoặc đổi ngày, tỉnh: lọc lại danh sách ngay tại màn này

**Chức năng liên quan:** STT 68 (tìm kiếm theo tên hoặc email, lọc người dùng theo trạng thái, ngày tham gia, khu vực), STT 70, 71 (khóa, mở khóa, qua E03), STT 73 (duyệt yêu cầu xác thực, qua E03).

**Trạng thái đặc biệt:**
- Không có kết quả: hiện dòng báo không có người dùng nào khớp
- Làm mới: `AdminRepository.listUsers` không có bản nghe theo thời gian thực, nên danh sách được đọc lại mỗi khi admin quay lại màn hình (từ E03 hoặc từ nền), không đọc lại khi xoay màn hình
- Tài khoản bị khóa: nhãn màu đỏ ghi "Khóa tạm" hoặc "Khóa vĩnh viễn"
- Tài khoản đã từng bị cảnh báo: nhãn màu cam ghi "Cảnh báo" bên cạnh nhãn trạng thái
- Mất mạng: hiện lại danh sách đã tải trước đó từ bộ nhớ đệm, kèm dải báo đang xem ngoại tuyến

**Khác với ảnh:**
- Ảnh vẽ ô "Tìm theo tên, số điện thoại"; đúng là ô tìm theo tên hoặc email (UC34), không tìm theo số điện thoại. Vẫn giữ cả ba bộ lọc: trạng thái, ngày tham gia, khu vực.
- Ảnh vẽ chip "Đã khóa" gộp; đúng là hai trạng thái Khóa tạm và Khóa vĩnh viễn, thêm "Đã xóa".
- Ảnh vẽ chip "Chờ (xác minh)" ngang hàng với trạng thái tài khoản; đúng là công tắc "Chờ xác thực" riêng vì đây là trạng thái xác thực, không phải trạng thái tài khoản.
- Ảnh không vẽ lọc theo ngày tham gia và khu vực; vẫn phải làm (STT 68).

## E03. Admin: Chi tiết người dùng

**Ảnh:** ![E03 Admin Chi tiết người dùng](e03-admin-chi-tiet-nguoi-dung.png)

**Lớp:** `AdminUserDetailFragment` trong `AdminActivity`.

**Mục đích:** Cho admin xem đầy đủ thông tin và lịch sử của một người dùng, duyệt xác thực tài khoản, rồi cảnh báo hoặc khóa tài khoản khi cần.

**Thành phần chính:**
- Đầu trang: ảnh đại diện chữ cái đầu, tên, số điện thoại (che một phần), ngày tham gia, trạng thái tài khoản và trạng thái xác thực
- Ba số liệu: số giao dịch, điểm đánh giá trung bình, số lần bị báo cáo
- Khối "Thông tin định danh" (chưa có ảnh): ngày sinh, giới tính, địa chỉ thường trú. Chỉ admin thấy. Không hiện số CCCD ở khối này: số CCCD chỉ hiện trong khối "Yêu cầu xác thực tài khoản" bên dưới (UC34 BR2)
- Khối "Yêu cầu xác thực tài khoản" (chưa có ảnh), hiện khi có yêu cầu chờ duyệt: số CCCD người gửi khai (`nationalId`), ảnh giấy tờ và ảnh chân dung cầm giấy tờ (`imageIds`), số lần đã gửi lại (`resubmitCount`, tối đa 3 lần gửi lại), rồi nút "Duyệt" và nút "Từ chối" kèm ô lý do từ chối. Admin xem số CCCD và hai ảnh rồi mới duyệt hoặc từ chối; hệ thống không kiểm số CCCD đã dùng cho tài khoản khác, Admin tự đối chiếu bằng mắt ở khối này (UC36). Dưới hai nút có dòng nhắc hệ quả: duyệt là mở quyền đăng tin, gửi đề nghị và tạo giao dịch cho tài khoản này; từ chối thì tài khoản vẫn xem, tìm kiếm, nhắn tin và theo dõi được nhưng chưa mua bán được
- Các tab lịch sử (chỉ phần "Lịch sử vi phạm" có trong ảnh): "Tin đăng", "Giao dịch", "Đánh giá", "Lịch sử vi phạm", "Nhật ký hoạt động"
- Danh sách "Lịch sử vi phạm": mỗi dòng có ngày, loại xử lý (cảnh báo) và lý do
- Dải cảnh báo màu cam khi tài khoản đã bị cảnh báo, nêu rõ hậu quả của lần cảnh báo kế tiếp theo ba bậc (bậc 2 khóa tạm, bậc 3 trở lên khóa vĩnh viễn)
- Ô nhập "Lý do" (bắt buộc khi cảnh báo hoặc khóa), chỉ admin thấy, không hiện cho người dùng
- Bộ chọn "Thời hạn khóa" (chưa có ảnh): số ngày hoặc ngày hết hạn, dùng khi khóa tạm và khi cảnh báo bậc 2
- Các nút cuối trang: "Cảnh báo" (nút phụ), "Khóa tài khoản" (nút chính, màu đỏ), "Mở khóa" (khi tài khoản đang bị khóa), "Gỡ toàn bộ tin" (khi tài khoản đang bị khóa)

**Hành động và điều hướng:**
- Bấm nút "quay lại" ở đầu trang: về màn đã gọi (E02 hoặc E01)
- Bấm "Duyệt" hoặc "Từ chối" ở khối yêu cầu xác thực tài khoản: ghi kết quả, người dùng nhận thông báo; "Từ chối" bắt buộc nhập lý do. Xác thực tài khoản là điều kiện bắt buộc để mua bán, không phải huy hiệu: "Duyệt" chuyển tài khoản sang đã xác thực và mở quyền đăng tin, gửi đề nghị, tạo giao dịch; "Từ chối" giữ tài khoản ở trạng thái chưa xác thực, người dùng được gửi lại yêu cầu (tối đa 3 lần gửi lại)
- Bấm "Cảnh báo": bắt buộc nhập lý do, ghi thêm một dòng vào lịch sử vi phạm với bậc tăng dần, vẫn ở màn này
- Bấm "Khóa tài khoản": bắt buộc nhập lý do và chọn thời hạn (hoặc khóa vĩnh viễn), sau đó khóa tài khoản, vẫn ở màn này với trạng thái cập nhật
- Bấm "Mở khóa": mở khóa tài khoản đang bị khóa tạm hoặc khóa vĩnh viễn, đưa về đang hoạt động
- Bấm "Gỡ toàn bộ tin": gỡ mọi tin của tài khoản đang bị khóa (hộp thoại xác nhận), gọi `AdminRepository.removeAllListingsOf`. Đây là thao tác riêng admin bấm, không tự chạy khi khóa
- Bấm một dòng tab Tin đăng hoặc Giao dịch: xem chi tiết (tin: `AdminListingDetailFragment`, giao dịch: `AdminDealDetailFragment`)

**Chức năng liên quan:** STT 69 (xem chi tiết hồ sơ: tin đăng, giao dịch, đánh giá, vi phạm), STT 70, 71 (khóa, mở khóa), STT 72 (cảnh báo theo bậc), STT 73 (duyệt yêu cầu xác thực), STT 74 (nhật ký hoạt động), STT 77 (gỡ hàng loạt tin của tài khoản bị khóa).

**Trạng thái đặc biệt:**
- Cảnh báo lần 1: chỉ cảnh báo, tài khoản vẫn hoạt động bình thường
- Hệ thống chỉ khóa tài khoản theo ba bậc (`Warning.resultingStatus()`); việc gỡ tin của tài khoản bị khóa là thao tác riêng admin bấm
- Cảnh báo lần 2: tự động khóa tạm theo quy tắc ba bậc, thời hạn do admin chọn khi cảnh báo
- Cảnh báo lần 3 trở lên: khóa vĩnh viễn. Hệ thống không tự gỡ tin; admin gỡ toàn bộ tin bằng nút "Gỡ toàn bộ tin"
- Tài khoản đang bị khóa (tạm hoặc vĩnh viễn): nút chính đổi thành "Mở khóa", cả hai loại khóa đều mở khóa được. Mở khóa không đặt lại số lần cảnh báo
- Tài khoản đã xóa: chỉ xem
- Tài khoản bị khóa hoặc đã xóa mà đang có yêu cầu xác thực chờ duyệt: yêu cầu bị đóng, không duyệt được
- Mất mạng: hiện lại thông tin đã tải, các nút hành động bị khóa
- Làm mới: hồ sơ, yêu cầu xác thực và tab Giao dịch nghe theo thời gian thực (`observeProfile`, `observeVerificationRequests`, `observeMyDeals`). Thông tin định danh, số liệu, cảnh báo, tin đăng, đánh giá và số báo cáo chưa có bản `observe...`; chúng được đọc lại sau mỗi thao tác cảnh báo, khóa, mở khóa, duyệt của admin

**Khác với ảnh:**
- Ảnh chỉ vẽ phần "Lịch sử vi phạm". Các tab Tin đăng, Giao dịch, Đánh giá, Nhật ký hoạt động (STT 69, 74), khối định danh, khối yêu cầu xác thực tài khoản (STT 73) vẫn phải làm.
- Ảnh chưa vẽ khối yêu cầu xác thực tài khoản nên cũng chưa vẽ dòng nhắc hệ quả của "Duyệt" và "Từ chối" (xác thực tài khoản là điều kiện bắt buộc để mua bán, quyết định Q9).
- Số CCCD không hiện ở khối hồ sơ chung mà chỉ hiện trong khối yêu cầu xác thực tài khoản (khối này chưa có ảnh), cùng ảnh giấy tờ và ảnh chân dung cầm giấy tờ.
- Ảnh vẽ "Lý do xử lý (nếu khóa)"; lý do bắt buộc cả khi cảnh báo, và cần thêm thời hạn khóa.
- Ảnh vẽ "Cảnh báo lần 3: không còn nút mở khóa" và "Mở khóa tạm" ở lần 2; đúng là có nút "Mở khóa" cho cả khóa tạm và khóa vĩnh viễn.

## E04. Admin: Kiểm duyệt nội dung

**Ảnh:** ![E04 Admin Kiểm duyệt nội dung](e04-admin-kiem-duyet-noi-dung.png) (chỉ vẽ tab báo cáo tin đăng)

**Lớp:** `AdminModerationFragment` và `AdminListingDetailFragment` (chi tiết tin cho admin, chưa có ảnh), trong `AdminActivity`.

**Mục đích:** Cho admin xem các báo cáo của người dùng, các tin bị gắn cờ và danh sách từ khóa cấm, rồi quyết định bỏ qua báo cáo hay ẩn tin. Gỡ một tin là bước nặng hơn ẩn tin, làm ở màn chi tiết tin (`AdminListingDetailFragment`), không làm ở danh sách. Gỡ toàn bộ tin của tài khoản bị khóa là nút riêng ở E03.

**Thành phần chính:**
- Tiêu đề "Kiểm duyệt nội dung" với ba tab: "Báo cáo" (như ảnh), "Tin bị gắn cờ" và "Từ khóa cấm" (hai tab sau chưa có ảnh)
- Tab "Báo cáo":
  - Bộ lọc loại đối tượng: Tin đăng, Người dùng, Tin nhắn (chưa có ảnh)
  - Ba nút lọc theo trạng thái xử lý: Chờ xử lý, Đã xử lý (có vi phạm), Đã xem (không vi phạm)
  - Danh sách báo cáo gom theo đối tượng, sắp theo số lượt báo cáo giảm dần để thấy đối tượng bị báo nhiều nhất trước (hệ thống không tự ẩn tin khi bị báo nhiều), mỗi dòng có ảnh thu nhỏ, tên tin (hoặc tên người dùng, nội dung tin nhắn), lý do báo cáo (màu đỏ), số lượt báo cáo, hai nút "Bỏ qua" và "Ẩn tin" (với người dùng và tin nhắn: "Bỏ qua" và "Xem người dùng")
- Tab "Tin bị gắn cờ": danh sách tin chứa từ khóa cấm (tin vẫn đang đăng), mỗi dòng có hai nút "Ẩn tin" và "Bỏ cờ"
- Tab "Từ khóa cấm": danh sách từ khóa, ô thêm từ khóa mới và nút xóa trên từng dòng. So khớp theo chuỗi, không phân biệt hoa thường và dấu

**Hành động và điều hướng:**
- Bấm "Bỏ qua": đóng mọi báo cáo còn chờ của đối tượng đó với trạng thái "Đã xem, không vi phạm", vẫn ở màn này
- Bấm "Ẩn tin": bắt buộc nhập lý do. Tin chuyển sang đã ẩn, người đăng thấy lý do và không tự hiện lại được; mọi báo cáo còn chờ của tin chuyển sang đã xử lý có vi phạm
- Bấm "Xem người dùng" (báo cáo về người dùng hoặc tin nhắn): sang E03 của người bị báo cáo, để cảnh báo hoặc khóa
- Bấm một dòng tin (ngoài hai nút): sang `AdminListingDetailFragment` để xem đầy đủ trước khi quyết định, có các nút "Ẩn tin" hoặc "Khôi phục", và nút "Gỡ tin" (bắt buộc nhập lý do, gọi `AdminRepository.removeListing`; tin chuyển sang đã gỡ, người bán không khôi phục được)
- Bấm một nút lọc: lọc lại danh sách ngay tại màn này
- Thêm hoặc xóa từ khóa ở tab "Từ khóa cấm": lưu ngay

**Chức năng liên quan:** STT 75 (danh sách tin bị báo cáo, số lượt và lý do), STT 76 (ẩn tin có lý do, khôi phục khi duyệt oan), STT 76 (gỡ một tin có lý do, ở màn chi tiết tin), STT 77 (gỡ hàng loạt tin của tài khoản bị khóa, qua E03), STT 78 (từ khóa cấm và gắn cờ).

**Trạng thái đặc biệt:**
- Không có báo cáo nào đang chờ xử lý: hiện dòng chữ báo trống
- Tin đã ẩn bởi admin: nút "Ẩn tin" đổi thành "Khôi phục" (đưa tin về đang bán, xóa lý do). Tin đã gỡ (admin gỡ ở màn chi tiết tin, hoặc gỡ toàn bộ tin của tài khoản bị khóa qua E03) thì không khôi phục được
- Thêm từ khóa đã có: báo lỗi, không thêm
- Làm mới: hàng báo cáo và màn chi tiết tin (`ListingRepository.observeListing`) nghe theo thời gian thực. Tab "Tin bị gắn cờ" (`AdminRepository.getFlaggedListings`) chưa có bản nghe, nên được đọc lại mỗi khi admin quay lại E04 (từ màn chi tiết tin hoặc từ nền), không đọc lại khi xoay màn hình
- Mất mạng: hiện lại danh sách đã tải trước đó từ bộ nhớ đệm, kèm dải báo đang xem ngoại tuyến

**Khác với ảnh:**
- Ảnh vẽ nhãn "Đã gỡ" và nút "Gỡ tin" cho từng tin ở danh sách; danh sách này chỉ có "Ẩn tin" kèm lý do bắt buộc (nhãn "Đã ẩn"). Nút "Gỡ tin" nằm ở màn chi tiết tin (`AdminListingDetailFragment`), bắt buộc lý do, trạng thái "Đã gỡ", không khôi phục được. Gỡ toàn bộ tin khi khóa tài khoản là nút "Gỡ toàn bộ tin" riêng ở E03 (STT 77).
- Ảnh vẽ nhãn lọc "Đã ẩn" và "Đã bỏ qua" cho báo cáo; đúng là "Đã xử lý (có vi phạm)" và "Đã xem (không vi phạm)", vì báo cáo về người dùng và tin nhắn không có việc ẩn.
- Ảnh vẽ "Bỏ qua: tin chuyển sang Đã bỏ qua"; thực tế là các báo cáo còn chờ về tin đó được đóng, không đổi trạng thái tin.
- Ảnh chỉ vẽ báo cáo về tin đăng. Báo cáo về người dùng và tin nhắn, tab "Tin bị gắn cờ" và "Từ khóa cấm" vẫn phải làm.
- Màn chi tiết tin cho admin (`AdminListingDetailFragment`) chưa có ảnh; không dùng B03 vì B03 là màn của người mua. Màn này có nút "Gỡ tin" mà ảnh E04 vẽ ở danh sách.

## E05. Admin: Quản lý danh mục

**Ảnh:** ![E05 Admin Quản lý danh mục](e05-admin-quan-ly-danh-muc.png)

**Lớp:** `AdminCategoryFragment` trong `AdminActivity`.

**Mục đích:** Cho admin thêm, sửa, ẩn, xóa danh mục sản phẩm và danh mục con dùng chung cho toàn ứng dụng, và cấu hình thuộc tính tình trạng riêng của từng danh mục.

**Thành phần chính:**
- Tiêu đề "Danh mục" và nút "+" ở góc phải để thêm danh mục mới
- Danh sách danh mục, danh mục con thụt vào dưới danh mục cha. Mỗi dòng có biểu tượng nhãn, tên danh mục, số tin đăng thuộc danh mục đó, công tắc "Đang dùng", nút "..." để sửa hoặc xóa
- Nút "Thêm danh mục mới" cố định ở cuối màn hình
- Hộp "Thêm hoặc sửa danh mục" (chưa có ảnh): ô tên, bộ chọn "Danh mục cha" (để trống là danh mục gốc; chỉ có hai cấp), công tắc "Đang dùng"
- Màn cấu hình thuộc tính tình trạng của một danh mục: danh sách tên thuộc tính (ví dụ Dung lượng pin, Bảo hành), thêm hoặc xóa từng thuộc tính

**Hành động và điều hướng:**
- Bấm "+" hoặc "Thêm danh mục mới": mở hộp thêm danh mục, vẫn ở màn này sau khi lưu
- Bấm "...": mở menu Sửa hoặc Xóa cho danh mục đó
- Bấm công tắc "Đang dùng": tắt thì danh mục ẩn khỏi lựa chọn khi đăng tin
- Bấm một danh mục (ngoài nút "..."): mở màn cấu hình thuộc tính tình trạng riêng cho danh mục đó (chưa có mã màn hình riêng, coi như một lớp phủ trên chính màn E05)

**Chức năng liên quan:** STT 79 (thêm, sửa, xóa danh mục, gồm danh mục con; hiển thị số tin đăng), STT 80 (cấu hình thuộc tính tình trạng theo danh mục).

**Trạng thái đặc biệt:**
- Xóa danh mục đang có tin đăng hoặc đang có danh mục con: không cho xóa, thông báo nêu số tin hoặc số danh mục con và gợi ý tắt "Đang dùng" để ẩn
- Đặt tên trùng danh mục đã có: báo lỗi ngay dưới ô nhập, không cho lưu

**Khác với ảnh:**
- Ảnh vẽ cảnh báo khi xóa danh mục đang có tin ("số tin đăng sẽ mất danh mục"); không làm, thay bằng chặn xóa và ẩn danh mục.
- Ảnh không vẽ bộ chọn danh mục cha và công tắc "Đang dùng"; vẫn phải làm (STT 79).

## E06. Admin: Giám sát giao dịch

**Ảnh:** ![E06 Admin Giám sát giao dịch](e06-admin-giam-sat-giao-dich.png)

**Lớp:** `AdminDealMonitorFragment` và `AdminDealDetailFragment` (chi tiết giao dịch cho admin, chưa có ảnh), trong `AdminActivity`.

**Mục đích:** Cho admin theo dõi tình trạng mọi giao dịch trong hệ thống và những giao dịch đang có khiếu nại.

**Thành phần chính:**
- Tiêu đề "Giám sát giao dịch"
- Hàng lọc theo trạng thái giao dịch: Tất cả, Chờ xác nhận, Đã xác nhận, Đã giao (kèm đếm ngược tự chuyển tiền), Đã nhận, Hoàn tất, Đã hủy, Đang khiếu nại
- Danh sách giao dịch, mỗi dòng có mã giao dịch, tên món đồ, giá và nhãn trạng thái màu
- Màn chi tiết `AdminDealDetailFragment` (chưa có ảnh), chỉ xem: thông tin giao dịch, hai bên, tài liệu thanh toán (`Payment`) cùng trạng thái đang giữ (HELD) hoặc đã chuyển (RELEASED), thời hạn tự chuyển, và dòng thời gian toàn bộ lịch sử chuyển trạng thái (người đổi, lý do, thời điểm)

**Hành động và điều hướng:**
- Bấm một giao dịch: sang `AdminDealDetailFragment` để xem đầy đủ lịch sử chuyển trạng thái
- Bấm một nút lọc trạng thái: lọc lại danh sách theo trạng thái đã chọn
- Trong chi tiết, giao dịch tranh chấp có liên kết sang D06 chi tiết khiếu nại

**Chức năng liên quan:** STT 81 (danh sách giao dịch, lọc theo trạng thái), STT 82 (chi tiết giao dịch gồm lịch sử trạng thái), STT 84 (theo dõi giải ngân tự động).

**Trạng thái đặc biệt:**
- Giao dịch "Đã giao" chỉ hiển thị tài liệu thanh toán đang ở HELD và đếm ngược, không có nút duyệt tay: hệ thống tự chuyển tài liệu đó sang RELEASED khi người mua xác nhận đã nhận hoặc khi hết thời hạn đếm ngược. Thanh toán là mô phỏng trong Firestore, không có ngân hàng hay cổng thanh toán bên ngoài
- Mất mạng: hiện lại danh sách đã tải trước đó từ bộ nhớ đệm, kèm dải báo đang xem ngoại tuyến
- Mọi trạng thái giao dịch là chip `view_status_chip` qua `StatusChips`. Chip trung tính ("Đã hủy", và "Đã hoàn cho người mua" của thanh toán ở màn chi tiết) có nền `colorSurfaceVariant`, vì nền `colorSurface` mặc định trùng màu thẻ và trông như chữ xám thường

**Khác với ảnh:**
- Ảnh có chip "Chờ giải ngân" và "Khiếu nại". Đúng là các chip ứng với trạng thái giao dịch; "Chờ giải ngân" là giao dịch "Đã giao" (`SHIPPED`) có tài liệu thanh toán đang ở HELD; chip "Khiếu nại" đặt tên theo trạng thái "Tranh chấp" (`DISPUTED`).
- Nút lọc "Bất thường" và dòng cảnh báo "Đứng yên quá 3 ngày" trong ảnh thuộc chức năng "Cảnh báo giao dịch bất thường" (STT 83), là **phát triển sau**, chưa làm.
- Ảnh không có màn chi tiết cho admin; bấm giao dịch mở `AdminDealDetailFragment` trong `AdminActivity`, không mở C03 (C03 là màn của người mua và người bán).

## E07. Admin: Quản lý tài khoản admin

**Ảnh:** chưa có ảnh.

**Lớp:** `AdminAccountFragment` trong `AdminActivity`.

**Mục đích:** Cho admin xem danh sách các tài khoản admin, cấp quyền admin cho một tài khoản đã đăng ký và gỡ quyền admin của một admin khác.

**Thành phần chính:**
- Tiêu đề "Tài khoản quản trị" và nút "Thêm admin"
- Danh sách admin, mỗi dòng có ảnh đại diện chữ cái đầu, tên, email và nút "Gỡ quyền admin" (dòng của chính mình không có nút này)
- Hộp "Thêm admin": ô nhập email của tài khoản đã đăng ký và nút "Tiếp tục"
- Pop up xác nhận kèm tên và email của tài khoản sắp được cấp quyền, hai nút "Xác nhận" và "Hủy"
- Pop up xác nhận gỡ quyền admin, hai nút "Xác nhận" và "Hủy"

**Hành động và điều hướng:**
- Bấm nút "quay lại" ở đầu trang: về E01
- Bấm "Thêm admin": mở hộp nhập email
- Bấm "Tiếp tục": hệ thống kiểm tra email, nếu hợp lệ thì mở pop up xác nhận kèm tên và email
- Bấm "Xác nhận" ở pop up cấp quyền: cấp quyền admin cho tài khoản, ghi log, danh sách cập nhật, vẫn ở màn này
- Bấm "Gỡ quyền admin" rồi "Xác nhận": gỡ quyền admin của tài khoản đó, ghi log, danh sách cập nhật, vẫn ở màn này. Tài khoản không bị xóa, trở về người dùng thường, vẫn đăng nhập được nhưng không vào được khu vực quản trị

**Chức năng liên quan:** STT 105 (thêm, gỡ quyền admin, UC42).

**Trạng thái đặc biệt:**
- Email không hợp lệ: báo lỗi dưới ô nhập, giữ nguyên hộp
- Email không thuộc tài khoản nào, hoặc tài khoản đã bị khóa hoặc xóa: báo không tìm thấy tài khoản có thể cấp quyền, giữ nguyên hộp
- Tài khoản đã là admin: báo "Tài khoản này đã là Admin"
- Admin chọn gỡ quyền của chính mình: từ chối, báo admin không được tự gỡ quyền của chính mình
- Việc gỡ quyền khiến hệ thống không còn admin nào: từ chối, phải còn ít nhất một admin
- Mất mạng: báo lỗi và giữ nguyên trạng thái, các nút hành động bị khóa

**Khác với ảnh:**
- Màn này chưa có ảnh mockup. Lối vào là ô "Tài khoản quản trị" trong lưới lối tắt của E01 (ảnh E01 chưa có ô này).
