# Nhóm B: Khám phá và tin đăng

6 màn hình, nộp theo 3 đợt: đợt 1 một màn (B01), đợt 2 hai màn (B02, B03), đợt 3 ba màn (B04, B05, B06).

## B01. Trang chủ

**Ảnh:** ![B01 Trang chủ](b01-trang-chu.png)

**Mục đích:** Màn hình chính sau khi đăng nhập, nơi người dùng lướt xem tin đăng mới và tìm nhanh món đồ mình cần.

**Thành phần chính:**
- Thanh trên cùng: tên ứng dụng ĐồCũ, chuông thông báo có chấm đỏ khi có thông báo chưa đọc, ảnh đại diện người dùng
- Ô tìm kiếm với gợi ý "Tìm iPhone, xe đạp, sách..."
- Hai thẻ chuyển kênh: "Kênh chung" và "Đang theo dõi"
- Hàng lọc nhanh theo danh mục: Tất cả, Điện thoại, Thời trang, Xe cộ
- Lưới tin đăng hai cột, mỗi thẻ có ảnh, tên món đồ, giá, tình trạng và khu vực
- Thanh điều hướng dưới cùng 5 mục: Trang chủ, Tìm kiếm, Đăng tin (nút tròn nổi ở giữa), Tin nhắn, Cá nhân

**Hành động và điều hướng:**
- Bấm ô tìm kiếm: sang B02 Tìm kiếm và bộ lọc
- Bấm một thẻ tin đăng: sang B03 Chi tiết tin đăng
- Bấm nút tròn "Đăng tin": sang B04 Đăng tin
- Bấm "Tin nhắn" ở thanh dưới: sang C01 Danh sách tin nhắn
- Bấm "Cá nhân" ở thanh dưới: sang D01 Hồ sơ cá nhân
- Bấm chuông thông báo: sang C06 Thông báo
- Chuyển thẻ "Đang theo dõi": vẫn ở màn này, lưới chỉ còn tin của người đang theo dõi

**Chức năng liên quan:** dòng 24, 25 (kênh chung, kênh đang theo dõi), dòng 17, 19 (danh mục), dòng 31, 32 (gợi ý sản phẩm, mục Dành cho bạn) trong bảng đặc tả chức năng.

**Trạng thái đặc biệt:**
- Chưa có tin nào khớp bộ lọc: hiện dòng chữ báo trống thay cho lưới
- Đang tải thêm khi cuộn xuống: hiện các ô xám mờ ở cuối lưới
- Mất mạng: hiện lại các tin đã xem gần đây lấy từ bộ nhớ đệm, kèm dải báo đang xem ngoại tuyến
- Thẻ "Đang theo dõi" khi chưa theo dõi ai: gợi ý người dùng tìm và theo dõi người bán

## B02. Tìm kiếm và bộ lọc

**Ảnh:** ![B02 Tìm kiếm và bộ lọc](b02-tim-kiem-bo-loc.png)

**Mục đích:** Hỗ trợ người dùng tra cứu tin đăng bằng từ khóa và thu hẹp phạm vi kết quả theo các tiêu chí (danh mục, khoảng giá, tình trạng, hình thức).

**Thành phần chính:**
- Thanh trên cùng: nút quay lại, ô nhập từ khóa tìm kiếm (đang nhập "iPhone 13"), nút đóng bộ lọc (hiển thị biểu tượng menu)
- Khu vực Tìm gần đây: các thẻ từ khóa lịch sử (iPhone 13, xe đạp, bàn học, áo khoác)
- Khu vực chọn tiêu chí lọc: Danh mục, Khoảng giá (Từ - Đến), Tình trạng (Mới, Đã sử dụng, Đã sửa chữa, Có lỗi), Hình thức (Bán, Trao đổi, Cho tặng)
- Thanh dưới cùng: Nút "Xóa lọc" và nút "Áp dụng"

**Hành động và điều hướng:**
- Bấm nút quay lại: trở về B01 Trang chủ
- Bấm vào một thẻ lịch sử tìm kiếm: điền ngay từ khóa đó vào ô tìm kiếm
- Bấm chọn các thẻ tiêu chí: thẻ đổi màu biểu thị trạng thái đang được chọn
- Bấm "Xóa lọc": bỏ chọn toàn bộ tiêu chí lọc hiện tại
- Bấm "Áp dụng": trở lại B01 Trang chủ với danh sách tin đăng đã được lọc theo tiêu chí
- Bấm nút menu lọc trên cùng: đóng bảng bộ lọc

**Chức năng liên quan:** dòng 27 (Tìm kiếm), 28 (Lọc tin), 29 (Lịch sử tìm kiếm) trong bảng đặc tả chức năng.

**Trạng thái đặc biệt:**
- Nhập giá "Đến" thấp hơn giá "Từ": hiển thị lỗi màu đỏ nhắc nhở ngay dưới ô giá
- Không có từ khóa tìm kiếm gần đây: ẩn khu vực "TÌM GẦN ĐÂY"

## B03. Chi tiết tin đăng

**Ảnh:** ![B03 Chi tiết tin đăng](b03-chi-tiet-tin-dang.png)

**Mục đích:** Hiển thị thông tin đầy đủ nhất về một món đồ, đồng thời cung cấp điểm chạm để người mua liên hệ người bán hoặc chốt giao dịch nhanh.

**Thành phần chính:**
- Khu vực ảnh phía trên: băng chuyền ảnh, đếm số ảnh (1/6), nút quay lại, nút thả tim (Lưu tin) và nút ba chấm (Menu phụ)
- Khối thông tin cốt lõi: Nhãn loại tin (BÁN), tình trạng (Đã sử dụng), Tên sản phẩm, Giá tiền, và dải gợi ý giá tham khảo dựa trên thị trường
- Thẻ người bán: Avatar, Tên shop (Minh Anh Shop), điểm đánh giá sao, số giao dịch, và nút "Theo dõi"
- Khu vực "Tình trạng chi tiết": liệt kê cụ thể các thông số do người bán nhập (Pin, Màn hình, Camera, Lịch sử sửa chữa)
- Thanh hành động dưới cùng cố định: Nút "Nhắn tin" và nút "Đề nghị giá" nổi bật

**Hành động và điều hướng:**
- Bấm nút quay lại: trở về màn hình trước đó (B01 hoặc màn hình nào đã gọi nó)
- Bấm nút thả tim: lưu tin vào danh sách yêu thích, biểu tượng tim chuyển sang màu đỏ
- Bấm nút "Theo dõi" ở thẻ người bán: theo dõi người bán này
- Bấm nút ba chấm: mở menu phụ (báo cáo tin, chia sẻ)
- Bấm thẻ người bán: sang D01 Hồ sơ cá nhân của người bán
- Bấm "Nhắn tin": chuyển sang C02 Cửa sổ chat với người bán, tự động đính kèm tin đăng này
- Bấm "Đề nghị giá": mở popup hoặc chuyển sang màn hình trả giá/chốt deal nhanh

**Chức năng liên quan:** dòng 30 (Xem chi tiết tin đăng), 37 (Lưu tin/Bỏ lưu), 36 (Theo dõi người bán), 44 (Đề nghị giá) trong bảng đặc tả chức năng.

**Trạng thái đặc biệt:**
- Tin đăng của chính mình: nút "Nhắn tin/Đề nghị giá" biến thành nút "Chỉnh sửa/Đã bán"
- Tin đã bán: nút bị làm xám, hiện nhãn "ĐÃ BÁN" đè lên ảnh sản phẩm
- Chưa đăng nhập: bấm "Nhắn tin" hoặc "Lưu tin" sẽ bật màn hình yêu cầu đăng nhập

## B04. Đăng tin

**Ảnh:** chưa nộp

**Mục đích:**

**Thành phần chính:**
-

**Hành động và điều hướng:**
-

**Chức năng liên quan:**

**Trạng thái đặc biệt:**
-

## B05. Kệ hàng của tôi

**Ảnh:** chưa nộp

**Mục đích:**

**Thành phần chính:**
-

**Hành động và điều hướng:**
-

**Chức năng liên quan:**

**Trạng thái đặc biệt:**
-

## B06. Tin đã lưu

**Ảnh:** chưa nộp

**Mục đích:**

**Thành phần chính:**
-

**Hành động và điều hướng:**
-

**Chức năng liên quan:**

**Trạng thái đặc biệt:**
-
