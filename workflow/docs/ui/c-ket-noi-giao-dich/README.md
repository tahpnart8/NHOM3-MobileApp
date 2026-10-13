# Nhóm C: Kết nối và giao dịch

6 màn hình, nộp theo 3 đợt: đợt 1 một màn (C01), đợt 2 hai màn (C02, C03), đợt 3 ba màn (C04, C05, C06). Số trong tài liệu là STT của [bảng đặc tả chức năng](../../product/dac-ta-chuc-nang.md). Trạng thái giao dịch, offer, ký quỹ theo [trang-thai.md](../../architecture/trang-thai.md). Điều hướng chung ở [README giao diện](../README.md#điều-hướng).

## C01. Danh sách tin nhắn

**Ảnh:** ![C01 Danh sách tin nhắn](c01-danh-sach-tin-nhan.png)

**Lớp:** `ConversationListFragment` trong `MainActivity`, tab Tin nhắn. Hiện thanh điều hướng dưới.

**Mục đích:** Cho người dùng xem toàn bộ cuộc trò chuyện đang có với người mua hoặc người bán, và biết cuộc nào có tin chưa đọc.

**Thành phần chính:**
- Tiêu đề "Tin nhắn"
- Ô tìm kiếm với gợi ý "Tìm cuộc trò chuyện"
- Danh sách cuộc trò chuyện, mỗi dòng gồm: ảnh đại diện chữ cái đầu, tên người đối thoại, đoạn tin nhắn gần nhất, thời điểm gần nhất
- Chấm tròn màu cam ở cuối dòng khi cuộc trò chuyện đó có tin chưa đọc
- Dòng có đề nghị giá hiển thị nội dung dạng "Đề nghị của bạn: 3.500.000đ"
- Thanh điều hướng dưới cùng 5 tab; tab "Tin nhắn" đang được chọn

**Hành động và điều hướng:**
- Bấm một cuộc trò chuyện: sang C02 Trò chuyện và đề nghị giá
- Gõ vào ô tìm kiếm: lọc danh sách ngay tại màn này, không chuyển màn
- Bấm các tab ở thanh dưới: sang B01, B02, B04, D01

**Chức năng liên quan:** STT 43, 44 (chat gửi chữ, ảnh), STT 47, 48, 49 (offer một chạm).

**Trạng thái đặc biệt:**
- Chưa có cuộc trò chuyện nào: hiện dòng chữ gợi ý người dùng vào xem tin đăng và nhắn cho người bán
- Tin chưa đọc: tên và nội dung in đậm, kèm chấm tròn màu cam
- Mất mạng: hiện lại danh sách đã tải trước đó từ bộ nhớ đệm, kèm dải báo đang xem ngoại tuyến
- Tìm không ra kết quả: hiện dòng báo không có cuộc trò chuyện nào khớp

**Khác với ảnh:** không có chỗ khác đáng kể. Cuộc trò chuyện luôn gắn với một tin đăng (mỗi người mua một cuộc cho mỗi tin).

## C02. Trò chuyện và đề nghị giá

**Ảnh:** ![C02 Trò chuyện và đề nghị giá](c02-tro-chuyen-de-nghi-gia.png)

**Lớp:** `ChatFragment` trong `MainActivity`. Màn hình chi tiết: có nút quay lại, ẩn thanh dưới. Mở từ C01, từ B03 (nút "Nhắn tin" hoặc sau khi gửi đề nghị), hoặc từ thông báo.

**Mục đích:** Cho phép người dùng trao đổi chi tiết về sản phẩm và thực hiện thương lượng bằng đề nghị một chạm trong khung chat.

**Thành phần chính:**
- Thanh tiêu đề trên cùng: nút quay lại, ảnh đại diện, tên người đối thoại, trạng thái hoạt động ("Đang hoạt động" hoặc thời điểm hoạt động gần nhất), nút tùy chọn (3 chấm)
- Banner sản phẩm đang trao đổi: ảnh thu nhỏ, tên sản phẩm, mức giá
- Khung nội dung chat: các bong bóng tin nhắn của mình và đối phương
- Thẻ "Đề nghị giá" trong luồng chat: hiện loại đề nghị (Tiền, Vật trao đổi, Tiền và vật), số tiền (ví dụ 6.800.000đ), món đồ đem đổi nếu có, và trạng thái (chờ trả lời, đã chấp nhận, đã từ chối). Với người bán, khi còn chờ trả lời, thẻ có hai nút "Từ chối" và "Chấp nhận"
- Thanh nhập liệu dưới cùng: nút đính kèm ảnh, ô nhập tin nhắn, nút gửi

**Hành động và điều hướng:**
- Bấm nút quay lại: về màn đã gọi (thường là C01)
- Bấm tên hoặc ảnh người đối thoại: sang D02 Trang cá nhân của người đó
- Bấm nút menu 3 chấm: mở menu "Xem trang cá nhân" (sang D02) và "Báo cáo người dùng" (sang D04)
- Bấm giữ một tin nhắn của đối phương (chưa có ảnh): "Báo cáo tin nhắn" (sang D04)
- Bấm vào banner sản phẩm: sang B03 Chi tiết tin đăng
- Bấm "Chấp nhận" trên thẻ đề nghị: tạo giao dịch ở trạng thái chờ xác nhận, tin chuyển sang đang giữ chỗ, các đề nghị chờ khác của tin đó tự bị từ chối, người mua nhận thông báo. Người bán được đưa sang C03
- Bấm "Từ chối": đề nghị chuyển sang đã từ chối, người mua nhận thông báo
- Bấm nút đính kèm: chọn một ảnh từ máy hoặc chụp ảnh (chỉ ảnh, không video; ảnh được nén trước khi gửi)
- Bấm nút gửi: đẩy tin nhắn chữ mới vào cuộc trò chuyện

**Chức năng liên quan:** STT 43, 44 (gửi chữ, gửi ảnh), 46 (trạng thái hoạt động), 47, 48, 49 (gửi, chấp nhận, từ chối đề nghị).

**Trạng thái đặc biệt:**
- Người gửi đề nghị: thẻ chỉ hiện "Đang chờ phản hồi", không có nút nào. Không hủy được đề nghị và không có đề xuất lại; muốn đổi giá thì đợi người bán từ chối rồi gửi đề nghị mới ở B03
- Tin không còn đang bán (đã giữ chỗ, đã bán, bị ẩn): thẻ đề nghị chờ không chấp nhận được, báo lỗi rõ ràng
- Khi mất mạng: nút gửi bị mờ hoặc tin nhắn vừa gửi hiện biểu tượng đang gửi (xoay vòng)

**Khác với ảnh:**
- Ảnh vẽ nút "Hủy đề nghị" cho người gửi; không làm (đề nghị chỉ được chấp nhận hoặc từ chối).
- Ảnh vẽ menu "chặn, báo cáo"; không có chức năng chặn. Có "Báo cáo" và "Xem trang cá nhân".
- Ảnh vẽ "gửi ảnh/video"; chỉ gửi ảnh (STT 45 là phát triển sau).
- Ảnh vẽ thẻ đề nghị chỉ có số tiền; thực tế có thêm loại đề nghị và món đồ trao đổi.
- Ảnh vẽ "Chấp nhận: chốt giao dịch"; thực tế chỉ tạo giao dịch chờ xác nhận, hai bên còn phải xác nhận ở C03.

## C03. Chi tiết giao dịch

**Ảnh:** ![C03 Chi tiết giao dịch](c03-chi-tiet-giao-dich.png)

**Lớp:** `DealDetailFragment` trong `MainActivity`. Màn hình chi tiết: có nút quay lại, ẩn thanh dưới. Giao dịch đang đếm ngược được kiểm tra tự động mỗi lần mở ứng dụng. Chỉ dành cho hai bên của giao dịch; admin xem ở `AdminDealDetailFragment` (E06).

**Mục đích:** Quản lý và theo dõi tiến trình của một giao dịch, cung cấp thông tin giao nhận và các hành động theo trạng thái.

**Thành phần chính:**
- Thanh tiêu đề: nút quay lại, mã giao dịch (ví dụ "#DL2049")
- Thanh tiến trình 5 bước: Chờ xác nhận, Đã xác nhận, Đang giao, Đã nhận, Hoàn tất. Bước hiện tại được đánh dấu nổi bật. Giao dịch gặp trực tiếp bỏ qua bước "Đang giao" (hiện mờ). Giao dịch đã hủy hiện nhãn "Đã hủy" kèm lý do; giao dịch đang khiếu nại hiện nhãn "Đang khiếu nại"
- Thẻ sản phẩm: ảnh thu nhỏ, tên, giá chốt
- Khối "Thông tin giao dịch": phương thức giao nhận (Gặp trực tiếp hoặc Gửi vận chuyển), địa điểm hoặc địa chỉ giao, thời gian hẹn, điều kiện khác hai bên thống nhất, món đồ trao đổi (nếu có). Khi đã giao: đơn vị vận chuyển, mã vận đơn, ảnh vận đơn
- Khối đếm ngược khi đang giao: "Còn N ngày để xác nhận" (7 ngày kể từ lúc giao, sau đó hệ thống tự chuyển sang đã nhận)
- Hộp thông tin ký quỹ: nhắc tiền đang được giữ an toàn ở ví trung gian (mô phỏng). Ẩn khi giá thống nhất bằng 0 (trao đổi hoặc cho tặng)
- Các nút hành động ở dưới, đổi theo trạng thái và vai trò (bảng dưới)

**Hành động theo trạng thái và vai trò:**

| Trạng thái | Người mua (hoặc người nhận đổi) | Người bán (hoặc người đăng) |
| --- | --- | --- |
| Chờ xác nhận | "Sửa điều khoản", "Xác nhận", "Hủy giao dịch" (bắt buộc lý do) | "Sửa điều khoản", "Xác nhận", "Hủy giao dịch" (bắt buộc lý do) |
| Đã xác nhận, giá lớn hơn 0 | "Thanh toán" (nếu chưa thanh toán), "Hủy giao dịch" | "Hủy giao dịch" |
| Đã xác nhận, gửi vận chuyển | Chờ người bán giao | "Xác nhận đã giao": bắt buộc ảnh vận đơn, tên đơn vị vận chuyển, mã vận đơn; chỉ làm được sau khi người mua đã thanh toán (nếu giá lớn hơn 0) |
| Đã xác nhận, gặp trực tiếp | "Xác nhận đã nhận hàng" (sau khi đã thanh toán nếu giá lớn hơn 0), "Khiếu nại giao dịch" | Chờ người mua |
| Đang giao | "Xác nhận đã nhận hàng", "Khiếu nại giao dịch" | Xem đếm ngược, "Khiếu nại giao dịch" |
| Đang khiếu nại | Xem khiếu nại (sang D07) | Xem khiếu nại (sang D07) |
| Đã nhận | "Viết đánh giá" (sang C05) nếu chưa đánh giá | "Viết đánh giá" nếu chưa đánh giá |
| Hoàn tất | "Viết đánh giá" nếu chưa đánh giá | "Viết đánh giá" nếu chưa đánh giá |
| Đã hủy | Chỉ xem, hiện lý do hủy | Chỉ xem, hiện lý do hủy |

- Không hủy được giao dịch từ "Đang giao" trở đi; có vấn đề thì khiếu nại.
- Sau "Đã nhận" tiền đã chuyển cho người bán nên không khiếu nại được nữa (vẫn báo cáo được).
- Giao dịch ở "Chờ xác nhận" quá 3 ngày mà hai bên chưa xác nhận thì hệ thống tự hủy.
- "Sửa điều khoản" xóa cả hai xác nhận, hai bên xác nhận lại.

**Hành động và điều hướng:**
- Bấm nút quay lại: về màn đã gọi (C02, C04 hoặc thông báo)
- Bấm "Xác nhận đã nhận hàng": giao dịch sang "Đã nhận", tiền ký quỹ chuyển ngay cho người bán
- Bấm "Khiếu nại giao dịch": sang D05 Tạo khiếu nại
- Bấm "Viết đánh giá": sang C05 Viết đánh giá
- Bấm tên người bên kia: sang D02

**Chức năng liên quan:** STT 50 (xem thông tin giao dịch), 51 (sửa điều khoản), 52 (xác nhận giao dịch), 53 (xác nhận đã nhận), 54 (xác nhận đã giao), 55 (hủy có lý do), 56 (thanh toán qua bên thứ ba, mô phỏng), 64 (khiếu nại), 59 (nút viết đánh giá), 84 (tự giải ngân khi hết đếm ngược).

**Trạng thái đặc biệt:**
- Giá thống nhất bằng 0: không có thanh toán, ẩn hộp ký quỹ và nút "Thanh toán"
- Thiếu ảnh, tên đơn vị hoặc mã vận đơn: nút "Xác nhận đã giao" chưa bấm được
- Mất mạng: hiện lại giao dịch đã tải từ bộ nhớ đệm, các nút hành động bị khóa

**Khác với ảnh:**
- Ảnh chỉ vẽ hai nút "Xác nhận đã nhận hàng" và "Khiếu nại giao dịch"; thực tế các nút đổi theo trạng thái và vai trò như bảng trên.
- Ảnh vẽ "Phương thức thanh toán (Ví trung gian)" như một dòng thông tin; không có lựa chọn "thanh toán trực tiếp". Ký quỹ áp dụng cho mọi giao dịch có giá lớn hơn 0, gặp trực tiếp hay gửi vận chuyển.
- Ảnh vẽ thanh tiến trình 5 bước đang ở bước "Đã xác nhận"; thực tế còn hai nhãn "Đã hủy" và "Đang khiếu nại" và đường gặp trực tiếp.
- Ảnh không vẽ khối đếm ngược 7 ngày và form giao hàng (ảnh vận đơn); vẫn phải làm.

## C04. Lịch sử giao dịch và thanh toán

**Ảnh:** ![C04 Lịch sử giao dịch](c04-lich-su-giao-dich.png) (chỉ vẽ tab "Giao dịch")

**Lớp:** `DealHistoryFragment` trong `MainActivity`. Màn hình chi tiết mở từ D01: có nút quay lại, ẩn thanh dưới.

**Mục đích:** Giúp người dùng xem lại toàn bộ các giao dịch đã thực hiện (cả vai trò người mua và người bán), và xem lịch sử thanh toán ký quỹ.

**Thành phần chính:**
- Thanh tiêu đề: nút quay lại, tiêu đề "Lịch sử giao dịch"
- Hai tab: "Giao dịch" (như ảnh) và "Thanh toán" (chưa có ảnh)
- Tab "Giao dịch":
  - Thanh bộ lọc trạng thái (dạng chip): Tất cả, Đang xử lý (chờ xác nhận, đã xác nhận, đang giao), Hoàn tất (đã nhận, hoàn tất), Khiếu nại, Đã hủy
  - Danh sách giao dịch: mỗi thẻ hiển thị ảnh sản phẩm, tên sản phẩm, giá tiền (màu cam), nhãn trạng thái (ví dụ "Hoàn tất" xanh lục, "Đang giao" cam, "Đã hủy" xám, "Khiếu nại" đỏ) và dòng "Vai trò: Người mua" hoặc "Người bán"
- Tab "Thanh toán": danh sách khoản ký quỹ, mỗi dòng có tên món đồ, số tiền, trạng thái (Đang giữ, Đã giải ngân, Đã hoàn), thời điểm giữ và giải ngân hoặc hoàn, và vai trò (đã trả hoặc đã nhận)

**Hành động và điều hướng:**
- Bấm nút quay lại: về D01 Hồ sơ cá nhân
- Bấm chọn các chip lọc trạng thái: lọc danh sách giao dịch ngay trên màn hình
- Bấm vào một thẻ giao dịch hoặc một dòng thanh toán: sang C03 Chi tiết giao dịch đó

**Chức năng liên quan:** STT 57 (xem lịch sử giao dịch), STT 58 (xem lịch sử thanh toán).

**Trạng thái đặc biệt:**
- Danh sách trống: hiện thông báo "Bạn chưa có giao dịch nào" hoặc "Bạn chưa có thanh toán nào"
- Mất mạng: hiện lại danh sách đã tải từ bộ nhớ đệm, kèm dải báo ngoại tuyến

**Khác với ảnh:**
- Ảnh chỉ có 4 chip (Tất cả, Hoàn tất, Đang xử lý, Đã hủy); thêm chip "Khiếu nại" vì có nhãn Khiếu nại.
- Ảnh không vẽ tab "Thanh toán" (STT 58); vẫn phải làm.

## C05. Viết đánh giá

**Ảnh:** ![C05 Viết đánh giá](c05-viet-danh-gia.png)

**Lớp:** `WriteReviewFragment` trong `MainActivity`. Màn hình chi tiết: có nút quay lại, ẩn thanh dưới. Chỉ mở từ nút "Viết đánh giá" trên C03 (hành động toàn cục `action_global_write_review`).

**Mục đích:** Cho phép người dùng đánh giá đối tác sau khi đã nhận hàng, nhằm xây dựng điểm uy tín trong cộng đồng. Viết được khi giao dịch ở "Đã nhận" hoặc "Hoàn tất", mỗi bên một lần.

**Thành phần chính:**
- Thanh tiêu đề: nút quay lại, tiêu đề "Đánh giá giao dịch"
- Khối thông tin đối tác: ảnh thu nhỏ, tên sản phẩm, dòng "Với <tên đối tác>"
- Đánh giá tổng thể: tiêu đề "Bạn đánh giá tổng thể thế nào?" kèm dãy 5 ngôi sao lớn
- Chi tiết đánh giá: ba tiêu chí (Độ chính xác thông tin, Thái độ giao dịch, Đúng cam kết) kèm sao riêng
- Ô nhận xét: khung nhập văn bản "Chia sẻ trải nghiệm của bạn..." (không bắt buộc)
- Nút "Gửi đánh giá": nút màu xanh nổi bật ở cuối màn hình

**Hành động và điều hướng:**
- Bấm vào các ngôi sao: chọn số sao (từ 1 đến 5) cho mục tương ứng
- Nhập văn bản vào ô nhận xét
- Bấm "Gửi đánh giá": lưu đánh giá, hiển thị thông báo thành công và quay về C03
- Bấm nút quay lại: hủy viết đánh giá và quay về C03

**Chức năng liên quan:** STT 59 (đánh giá đối tác).

**Trạng thái đặc biệt:**
- Nút "Gửi đánh giá" bị vô hiệu hóa (mờ đi) nếu người dùng chưa chọn đủ sao tổng thể và ba tiêu chí
- Đã đánh giá giao dịch này rồi: không mở lại biểu mẫu, hiện đánh giá đã gửi

**Khác với ảnh:**
- Ảnh có khu vực "Tải ảnh đính kèm" (thư viện, chụp ảnh mới); không làm, đánh giá không có ảnh.
- Ảnh ghi "sau khi giao dịch hoàn tất"; đúng là viết được ngay sau khi đã nhận hàng (trạng thái Đã nhận hoặc Hoàn tất).

## C06. Thông báo

**Ảnh:** ![C06 Thông báo](c06-thong-bao.png)

**Lớp:** `NotificationListFragment` trong `MainActivity`. Màn hình chi tiết mở từ chuông ở B01: có nút quay lại, ẩn thanh dưới.

**Mục đích:** Hiển thị các thông báo cập nhật về tin nhắn, đề nghị giá, giao dịch, đánh giá, khiếu nại, cảnh báo, kết quả xác minh và tin bị admin xử lý. Không có thông báo hệ thống do admin gửi hàng loạt (STT 99 là phát triển sau).

**Thành phần chính:**
- Thanh tiêu đề: nút quay lại, tiêu đề "Thông báo"
- Danh sách thông báo: mỗi dòng gồm biểu tượng phân loại, tiêu đề in đậm, nội dung tóm tắt, thời gian nhận (ví dụ "5 phút trước", "Hôm qua")
- Đánh dấu chưa đọc: chấm tròn màu cam ở bên phải các thông báo chưa bấm xem

**Hành động và điều hướng:**
- Bấm vào một thông báo: đánh dấu đã đọc và chuyển đến màn hình tương ứng theo loại:

| Loại thông báo | Màn hình đích |
| --- | --- |
| NEW_MESSAGE (tin nhắn mới) | C02 |
| NEW_OFFER (đề nghị giá mới) | C02 |
| OFFER_RESULT (đề nghị được chấp nhận hoặc từ chối) | C02 |
| DEAL_UPDATE (giao dịch đổi trạng thái) | C03 |
| PAYMENT_UPDATE (tiền được giữ, giải ngân, hoàn) | C03 |
| NEW_REVIEW (có đánh giá mới) | D03 của người được đánh giá (chính mình, chế độ chỉ xem) |
| COMPLAINT_RESULT (kết quả khiếu nại) | D07, thẻ "Khiếu nại của tôi" |
| WARNING (bị cảnh báo hoặc khóa) | D01 |
| LISTING_MODERATED (tin bị admin ẩn, gỡ hoặc khôi phục) | B05 |
| VERIFICATION_RESULT (kết quả xác minh người bán) | D01 |

- Cuộn dọc danh sách: xem thêm các thông báo cũ hơn
- Bấm nút quay lại: về màn đã gọi

**Chức năng liên quan:** STT 67 (xem thông báo, đánh dấu đã đọc), STT 10 (loại thông báo đã tắt thì không hiện).

**Trạng thái đặc biệt:**
- Trống: hiện hình minh họa "Bạn chưa có thông báo mới" khi không có bất kỳ thông báo nào
- Tải thêm: khi người dùng kéo xuống dưới cùng, ứng dụng tải thêm các thông báo cũ

**Khác với ảnh:**
- Ảnh vẽ thanh điều hướng dưới 5 mục; ở đây thanh dưới không hiện.
- Ảnh vẽ chung "thông báo hệ thống"; không làm thông báo bảo trì hoặc broadcast do admin gửi.
