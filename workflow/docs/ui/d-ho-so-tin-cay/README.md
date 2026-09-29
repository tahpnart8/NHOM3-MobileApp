# Nhóm D: Hồ sơ và tin cậy

6 màn hình, nộp theo 3 đợt: đợt 1 một màn (D01), đợt 2 hai màn (D02, D03), đợt 3 ba màn (D04, D05, D06).

## D01. Hồ sơ cá nhân

**Ảnh:** ![D01 Hồ sơ cá nhân](d01-ho-so-ca-nhan.png)

**Mục đích:** Trang cá nhân của chính người đang đăng nhập, là lối vào mọi mục quản lý liên quan tới bản thân: hàng đã bán, tin đã lưu, lịch sử giao dịch, uy tín, khiếu nại và cài đặt.

**Thành phần chính:**
- Đầu trang: ảnh đại diện chữ cái đầu, tên người dùng, khu vực (ví dụ Quận 1, TP.HCM), nút cài đặt nhanh ở góc phải
- Hàng ba số liệu: số món đã bán, điểm đánh giá trung bình, số người theo dõi
- Danh sách lối vào, mỗi dòng có biểu tượng và mũi tên sang phải: Sản phẩm của tôi, Tin đã lưu, Lịch sử giao dịch, Hồ sơ uy tín, Báo cáo & khiếu nại của tôi, Cài đặt tài khoản
- Mục "Đăng xuất" màu đỏ, tách riêng ở cuối danh sách
- Thanh điều hướng dưới cùng 5 mục: Trang chủ, Tìm kiếm, Đăng tin (nút tròn nổi ở giữa), Tin nhắn, Cá nhân; mục "Cá nhân" đang được chọn

**Hành động và điều hướng:**
- Bấm "Sản phẩm của tôi": sang B05 Kệ hàng của tôi
- Bấm "Tin đã lưu": sang B06 Tin đã lưu
- Bấm "Lịch sử giao dịch": sang C04 Lịch sử giao dịch
- Bấm "Hồ sơ uy tín": sang D03 Hồ sơ uy tín người bán
- Bấm "Báo cáo & khiếu nại của tôi": sang D05 Tạo khiếu nại
- Bấm "Cài đặt tài khoản": sang A04 Quản lý thông tin cá nhân
- Bấm ảnh đại diện hoặc tên: sang D02 Trang cá nhân
- Bấm "Đăng xuất": hỏi xác nhận, đồng ý thì về A01 Đăng nhập
- Bấm "Trang chủ" ở thanh dưới: sang B01 Trang chủ
- Bấm "Tìm kiếm" ở thanh dưới: sang B02 Tìm kiếm và bộ lọc
- Bấm "Đăng tin" ở thanh dưới: sang B04 Đăng tin
- Bấm "Tin nhắn" ở thanh dưới: sang C01 Danh sách tin nhắn

**Chức năng liên quan:** dòng 6, 7, 8 (chỉnh sửa thông tin, ảnh, địa chỉ), dòng 9 (xem đánh giá cá nhân), dòng 10 (quản lý kệ hàng), dòng 35 (xem danh sách đã lưu), dòng 50 (lịch sử giao dịch), dòng 56, 58 (lịch sử báo cáo, theo dõi khiếu nại) trong bảng đặc tả chức năng.

**Trạng thái đặc biệt:**
- Người dùng chưa bán món nào: ba số liệu hiện 0, mục "Sản phẩm của tôi" gợi ý đăng tin đầu tiên
- Chưa có đánh giá nào: ô điểm đánh giá hiện dấu gạch thay cho số
- Tài khoản chưa xác minh người bán: hiện thêm một dải nhắc gửi yêu cầu xác minh
- Mất mạng: vẫn hiện thông tin đã tải trước đó từ bộ nhớ đệm, kèm dải báo đang xem ngoại tuyến

## D02. Trang cá nhân

**Ảnh:** ![D02 Trang cá nhân](d02-trang-ca-nhan.png)

**Mục đích:** Trang công khai của chính người đang đăng nhập, cho họ thấy người khác nhìn hồ sơ của mình ra sao: thông tin giới thiệu, số liệu uy tín và các món đang bán, đã bán, đánh giá nhận được.

**Thành phần chính:**
- Thanh trên: nút quay lại, tiêu đề "Trang cá nhân", nút cài đặt ở góc phải
- Ảnh bìa màu xanh, ảnh đại diện chữ cái đầu đè lên mép ảnh bìa, nút "Chỉnh sửa trang" bên phải
- Tên người dùng kèm biểu tượng khiên, dòng khu vực và ngày tham gia (ví dụ Quận 1, TP.HCM, tham gia 03/2025), đoạn giới thiệu ngắn
- Hàng bốn số liệu: điểm đánh giá kèm năm sao và số lượt đánh giá, số món đã bán, tỉ lệ hoàn tất, số người theo dõi
- Ba tab: "Đang bán (6)", "Đã bán (27)", "Đánh giá"; tab "Đang bán" đang được chọn
- Lưới tin đăng hai cột, mỗi thẻ có ảnh, tên món, giá, tình trạng (Đã sử dụng, Như mới) và khu vực
- Thanh điều hướng dưới cùng 5 mục: Trang chủ, Tìm kiếm, Đăng tin, Tin nhắn, Cá nhân; mục "Cá nhân" đang được chọn

**Hành động và điều hướng:**
- Bấm nút quay lại: về D01 Hồ sơ cá nhân
- Bấm "Chỉnh sửa trang": sang A05 Chỉnh sửa hồ sơ
- Bấm tab "Đang bán", "Đã bán" hoặc "Đánh giá": đổi nội dung bên dưới ngay trên màn này
- Bấm một thẻ tin đăng: sang B03 Chi tiết tin đăng
- Bấm "Trang chủ", "Tìm kiếm", "Đăng tin", "Tin nhắn" ở thanh dưới: sang B01, B02, B04, C01

**Chức năng liên quan:** dòng 6, 7 (chỉnh sửa thông tin và ảnh hồ sơ, qua nút "Chỉnh sửa trang"), dòng 9 (xem đánh giá cá nhân), dòng 10 (kệ hàng: món đang bán, đã bán), dòng 33 (theo dõi, hiện số người theo dõi), dòng 53 (xem lịch sử đánh giá, tab "Đánh giá") trong bảng đặc tả chức năng.

**Trạng thái đặc biệt:**
- Chưa có món đang bán hoặc đã bán: tab tương ứng hiện dòng báo trống thay cho lưới tin đăng
- Chưa có đánh giá nào: ô điểm đánh giá hiện dấu gạch, tab "Đánh giá" hiện dòng báo trống
- Mất mạng: vẫn hiện thông tin và tin đăng đã tải trước đó từ bộ nhớ đệm, kèm dải báo đang xem ngoại tuyến

## D03. Hồ sơ uy tín người bán

**Ảnh:** ![D03 Hồ sơ uy tín người bán](d03-ho-so-uy-tin-nguoi-ban.png)

**Mục đích:** Trang người mua xem khi muốn biết một người bán có đáng tin không: số liệu uy tín, đánh giá gần đây của người đã giao dịch, và các món người đó đang bán, kèm nút theo dõi và nhắn tin.

**Thành phần chính:**
- Thanh trên: nút quay lại, tiêu đề "Hồ sơ người bán", nút ba chấm ở góc phải
- Ảnh đại diện chữ cái đầu, tên người bán kèm biểu tượng khiên, dòng khu vực và năm tham gia (ví dụ Quận 1, TP.HCM, tham gia 2024)
- Hai nút: "Theo dõi" (viền) và "Nhắn tin" (nền xanh)
- Hàng ba số liệu: điểm đánh giá kèm năm sao và số lượt đánh giá, tỷ lệ hoàn tất, số giao dịch
- Mục "Đánh giá gần đây": mỗi dòng có ảnh đại diện chữ cái đầu, tên người đánh giá, số sao và lời nhận xét
- Mục "Đang bán (6)": lưới tin đăng hai cột, mỗi thẻ có ảnh, tên món, giá, tình trạng và khu vực

**Hành động và điều hướng:**
- Bấm nút quay lại: về màn trước đó
- Bấm "Theo dõi": theo dõi người bán ngay trên màn này
- Bấm "Nhắn tin": sang C02 Trò chuyện và đề nghị giá với người bán này
- Bấm một thẻ tin đăng: sang B03 Chi tiết tin đăng

**Chức năng liên quan:** dòng 9 (xem công khai điểm đánh giá), dòng 33 (theo dõi người dùng khác), dòng 37 (gửi tin nhắn) trong bảng đặc tả chức năng.

**Trạng thái đặc biệt:**
- Đã theo dõi người bán này: nút "Theo dõi" đổi thành trạng thái đang theo dõi, bấm lại để bỏ theo dõi
- Người bán chưa có đánh giá: mục "Đánh giá gần đây" hiện dòng báo trống, ô điểm hiện dấu gạch
- Người bán không còn món nào đang bán: mục "Đang bán (0)" hiện dòng báo trống
- Mất mạng: vẫn hiện thông tin đã tải trước đó từ bộ nhớ đệm, kèm dải báo đang xem ngoại tuyến

## D04. Báo cáo tin đăng

**Ảnh:** chưa nộp

**Mục đích:**

**Thành phần chính:**
-

**Hành động và điều hướng:**
-

**Chức năng liên quan:**

**Trạng thái đặc biệt:**
-

## D05. Tạo khiếu nại

**Ảnh:** chưa nộp

**Mục đích:**

**Thành phần chính:**
-

**Hành động và điều hướng:**
-

**Chức năng liên quan:**

**Trạng thái đặc biệt:**
-

## D06. Admin: Xử lý khiếu nại

**Ảnh:** chưa nộp

**Mục đích:**

**Thành phần chính:**
-

**Hành động và điều hướng:**
-

**Chức năng liên quan:**

**Trạng thái đặc biệt:**
-
