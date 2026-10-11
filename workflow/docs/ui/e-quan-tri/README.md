# Nhóm E: Quản trị

6 màn hình, nộp theo 3 đợt: đợt 1 một màn (E01), đợt 2 hai màn (E02, E03), đợt 3 ba màn (E04, E05, E06).

## E01. Admin: Bảng điều khiển

**Ảnh:** ![E01 Admin Bảng điều khiển](e01-admin-bang-dieu-khien.png)

**Mục đích:** Màn hình đầu tiên admin thấy sau khi đăng nhập: liệt kê những việc đang chờ xử lý và dẫn sang các mục quản trị chi tiết.

**Thành phần chính:**
- Tiêu đề "Bảng điều khiển" và dòng chào "Xin chào, Admin"
- Danh sách "Cần xử lý": mỗi dòng là một việc cụ thể (khiếu nại mới, tin bị báo cáo, tài khoản bị báo cáo), kèm nhãn trạng thái "Mới" hoặc "Chờ" và mũi tên điều hướng
- Lưới "Lối tắt quản lý" 4 nút: Người dùng, Tin đăng, Khiếu nại, Danh mục

**Khác với ảnh:**
- 4 ô số liệu tổng (1.240 người dùng, 850 tin đăng, 420 giao dịch, 18 khiếu nại) thuộc chức năng "Số liệu tổng quan", là **phát triển sau**, chưa làm.
- Dòng "Yêu cầu giải ngân, chờ duyệt" không làm: giải ngân đã đổi sang tự động, admin không duyệt tay.

**Hành động và điều hướng:**
- Bấm một dòng khiếu nại trong "Cần xử lý": sang D06 Admin: Xử lý khiếu nại
- Bấm một dòng tin bị báo cáo: sang E04 Admin: Kiểm duyệt nội dung
- Bấm một dòng tài khoản bị báo cáo: sang E03 Admin: Chi tiết người dùng
- Bấm nút "Người dùng": sang E02 Admin: Danh sách người dùng
- Bấm nút "Tin đăng": sang E04 Admin: Kiểm duyệt nội dung
- Bấm nút "Khiếu nại": sang D06 Admin: Xử lý khiếu nại
- Bấm nút "Danh mục": sang E05 Admin: Quản lý danh mục

**Chức năng liên quan:** danh sách và lọc người dùng, xử lý tin bị báo cáo, danh sách khiếu nại (số dòng sẽ ghi lại khi bảng đặc tả được đánh số lại).

**Trạng thái đặc biệt:**
- Không có việc nào cần xử lý: hiện dòng chữ báo đã xử lý hết, vẫn giữ lưới lối tắt
- Mất mạng: hiện danh sách đã tải trước đó từ bộ nhớ đệm, kèm dải báo đang xem ngoại tuyến

## E02. Admin: Danh sách người dùng

**Ảnh:** ![E02 Admin Danh sách người dùng](e02-admin-danh-sach-nguoi-dung.png)

**Mục đích:** Cho admin xem toàn bộ người dùng của hệ thống, lọc theo trạng thái tài khoản để tìm nhanh người cần xử lý.

**Thành phần chính:**
- Ô tìm kiếm "Tìm theo tên, số điện thoại"
- Hàng lọc theo trạng thái: Tất cả, Đang hoạt động, Đã khóa, Chờ (xác minh)
- Danh sách người dùng, mỗi dòng có ảnh đại diện chữ cái đầu, tên, số điện thoại (che một phần), thông tin phụ (ngày tham gia hoặc số giao dịch), nhãn trạng thái màu theo tình trạng tài khoản

**Hành động và điều hướng:**
- Bấm một người dùng: sang E03 Admin: Chi tiết người dùng
- Gõ vào ô tìm kiếm: lọc danh sách ngay tại màn này, không chuyển màn
- Bấm một nút lọc trạng thái: lọc lại danh sách theo trạng thái đã chọn

**Chức năng liên quan:** dòng 60 (danh sách và lọc người dùng), dòng 62, 63 (khóa, mở khóa tài khoản), dòng 65 (duyệt yêu cầu xác minh người bán) trong bảng đặc tả chức năng.

**Trạng thái đặc biệt:**
- Tìm không ra kết quả: hiện dòng báo không có người dùng nào khớp
- Tài khoản đã khóa: nhãn trạng thái màu đỏ ghi "Đã khóa"
- Tài khoản có báo cáo vi phạm: nhãn trạng thái màu cam ghi "Cảnh báo"
- Mất mạng: hiện lại danh sách đã tải trước đó từ bộ nhớ đệm, kèm dải báo đang xem ngoại tuyến

## E03. Admin: Chi tiết người dùng

**Ảnh:** ![E03 Admin Chi tiết người dùng](e03-admin-chi-tiet-nguoi-dung.png)

**Mục đích:** Cho admin xem đầy đủ thông tin và lịch sử vi phạm của một người dùng, rồi cảnh báo hoặc khóa tài khoản khi cần.

**Thành phần chính:**
- Đầu trang: ảnh đại diện chữ cái đầu, tên, số điện thoại (che một phần), ngày tham gia
- Ba số liệu: số giao dịch, điểm đánh giá trung bình, số lần bị báo cáo
- Danh sách "Lịch sử vi phạm": mỗi dòng có ngày, loại xử lý (cảnh báo) và lý do
- Dải cảnh báo màu cam khi đã cảnh báo đủ số lần, nêu rõ quy tắc tự động khóa nếu vi phạm tiếp
- Ô nhập "Lý do xử lý (nếu khóa)", chỉ admin thấy, không hiện cho người dùng
- Hai nút cuối trang: "Cảnh báo" (nút phụ) và "Khóa tài khoản" (nút chính, màu đỏ)

**Hành động và điều hướng:**
- Bấm nút "quay lại" ở đầu trang: về E02 Admin: Danh sách người dùng
- Bấm "Cảnh báo": ghi thêm một dòng vào lịch sử vi phạm với bậc cảnh báo tăng dần, vẫn ở màn này
- Bấm "Khóa tài khoản": yêu cầu nhập lý do trước, sau đó khóa tài khoản, vẫn ở màn này với trạng thái cập nhật

**Chức năng liên quan:** dòng 61 (xem hồ sơ người dùng), dòng 62, 63 (khóa, mở khóa tài khoản), dòng 64 (cảnh báo theo bậc), dòng 66 (nhật ký hoạt động) trong bảng đặc tả chức năng.

**Trạng thái đặc biệt:**
- Cảnh báo lần 1: chỉ cảnh báo, tài khoản vẫn hoạt động bình thường
- Cảnh báo lần 2: tự động khóa tạm theo quy tắc ba bậc, nút "Khóa tài khoản" đổi thành "Mở khóa tạm"
- Cảnh báo lần 3: khóa vĩnh viễn, không còn nút mở khóa
- Tài khoản đã khóa từ trước: nút chính đổi thành "Mở khóa tài khoản"

## E04. Admin: Kiểm duyệt nội dung

**Ảnh:** ![E04 Admin Kiểm duyệt nội dung](e04-admin-kiem-duyet-noi-dung.png)

**Mục đích:** Cho admin xem các tin đăng bị người dùng báo cáo và quyết định gỡ tin hay bỏ qua báo cáo.

**Thành phần chính:**
- Tiêu đề "Tin bị báo cáo"
- Ba nút lọc theo trạng thái xử lý: Chờ xử lý, Đã gỡ, Đã bỏ qua
- Danh sách tin bị báo cáo, mỗi dòng có ảnh thu nhỏ, tên tin, lý do báo cáo (màu đỏ), số lượt báo cáo, hai nút "Bỏ qua" và "Gỡ tin"

**Hành động và điều hướng:**
- Bấm "Bỏ qua": tin chuyển sang trạng thái Đã bỏ qua, biến mất khỏi danh sách Chờ xử lý, vẫn ở màn này
- Bấm "Gỡ tin": tin bị ẩn khỏi ứng dụng, chuyển sang trạng thái Đã gỡ, vẫn ở màn này
- Bấm một dòng tin (ngoài hai nút): sang B03 Chi tiết tin đăng để xem đầy đủ trước khi quyết định
- Bấm một nút lọc trạng thái: lọc lại danh sách theo trạng thái đã chọn

**Chức năng liên quan:** dòng 67 (xử lý tin bị báo cáo), dòng 68 (ẩn, gỡ, khôi phục tin), dòng 69 (gỡ hàng loạt tin của tài khoản bị khóa), dòng 70 (lọc từ khóa cấm) trong bảng đặc tả chức năng.

**Trạng thái đặc biệt:**
- Không có tin nào đang chờ xử lý: hiện dòng chữ báo trống
- Tin đã gỡ trước đó: có thể khôi phục lại, nút "Gỡ tin" đổi thành "Khôi phục"
- Mất mạng: hiện lại danh sách đã tải trước đó từ bộ nhớ đệm, kèm dải báo đang xem ngoại tuyến

## E05. Admin: Quản lý danh mục

**Ảnh:** ![E05 Admin Quản lý danh mục](e05-admin-quan-ly-danh-muc.png)

**Mục đích:** Cho admin thêm, sửa, xóa danh mục sản phẩm dùng chung cho toàn ứng dụng.

**Thành phần chính:**
- Tiêu đề "Danh mục" và nút "+" ở góc phải để thêm danh mục mới
- Danh sách danh mục, mỗi dòng có biểu tượng nhãn, tên danh mục, số tin đăng thuộc danh mục đó, nút "..." để sửa hoặc xóa
- Nút "Thêm danh mục mới" cố định ở cuối màn hình

**Hành động và điều hướng:**
- Bấm "+" hoặc "Thêm danh mục mới": mở hộp nhập tên danh mục mới, vẫn ở màn này sau khi lưu
- Bấm "...": mở menu Sửa hoặc Xóa cho danh mục đó
- Bấm một danh mục (ngoài nút "..."): mở màn cấu hình thuộc tính tình trạng riêng cho danh mục đó (chưa có mã màn hình riêng, coi như một lớp phủ trên chính màn E05)

**Chức năng liên quan:** dòng 71 (quản lý danh mục và danh mục con), dòng 72 (cấu hình thuộc tính tình trạng theo danh mục) trong bảng đặc tả chức năng.

**Trạng thái đặc biệt:**
- Xóa danh mục đang có tin đăng: hỏi xác nhận, nêu rõ số tin đăng sẽ mất danh mục
- Đặt tên trùng danh mục đã có: báo lỗi ngay dưới ô nhập, không cho lưu

## E06. Admin: Giám sát giao dịch

**Ảnh:** ![E06 Admin Giám sát giao dịch](e06-admin-giam-sat-giao-dich.png)

**Mục đích:** Cho admin theo dõi tình trạng mọi giao dịch trong hệ thống và những giao dịch đang có khiếu nại.

**Thành phần chính:**
- Tiêu đề "Giám sát giao dịch"
- Hàng lọc theo trạng thái: Tất cả, Chờ giải ngân, Khiếu nại
- Danh sách giao dịch, mỗi dòng có mã giao dịch, tên món đồ, giá và nhãn trạng thái màu

**Khác với ảnh:**
- Nút lọc "Bất thường" và dòng cảnh báo "Đứng yên quá 3 ngày" thuộc chức năng "Cảnh báo giao dịch bất thường", là **phát triển sau**, chưa làm.

**Hành động và điều hướng:**
- Bấm một giao dịch: sang C03 Chi tiết giao dịch để xem đầy đủ statusHistory
- Bấm một nút lọc trạng thái: lọc lại danh sách theo trạng thái đã chọn

**Chức năng liên quan:** theo dõi danh sách giao dịch theo trạng thái, chi tiết giao dịch kèm statusHistory (số dòng sẽ ghi lại khi bảng đặc tả được đánh số lại).

**Trạng thái đặc biệt:**
- Trạng thái "Chờ giải ngân" chỉ hiển thị thông tin, không có nút duyệt tay: giải ngân diễn ra tự động khi người mua xác nhận đã nhận hoặc khi hết thời hạn đếm ngược
- Mất mạng: hiện lại danh sách đã tải trước đó từ bộ nhớ đệm, kèm dải báo đang xem ngoại tuyến
