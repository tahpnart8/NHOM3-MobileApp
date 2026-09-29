# Nhóm A: Tài khoản và thông tin cá nhân

6 màn hình, nộp theo 3 đợt: đợt 1 một màn (A01), đợt 2 hai màn (A02, A03), đợt 3 ba màn (A04, A05, A06).

## A01. Đăng nhập

**Ảnh:** chưa nộp

**Mục đích:**

**Thành phần chính:**
-

**Hành động và điều hướng:**
-

**Chức năng liên quan:**

**Trạng thái đặc biệt:**
-

## A02. Đăng ký

**Ảnh:** chưa nộp

**Mục đích:**

**Thành phần chính:**
-

**Hành động và điều hướng:**
-

**Chức năng liên quan:**

**Trạng thái đặc biệt:**
-

## A03. Quên mật khẩu

**Ảnh:** chưa nộp

**Mục đích:**

**Thành phần chính:**
-

**Hành động và điều hướng:**
-

**Chức năng liên quan:**

**Trạng thái đặc biệt:**
-

## A04. Quản lý thông tin cá nhân

**Ảnh:** ![A04 Quản lý thông tin cá nhân](a04-quan-ly-thong-tin-ca-nhan.png)

**Mục đích:** Trung tâm quản lý các tùy chọn thiết lập, thông tin cá nhân và tài khoản của người dùng.

**Thành phần chính:**
- Nút mũi tên quay lại góc trái trên, tiêu đề "Quản lý thông tin"
- Danh sách các mục cài đặt:
  - Thông tin hồ sơ (Tên hiển thị, avatar)
  - Quản lý địa chỉ
  - Số điện thoại liên kết
  - Email liên kết
- Danh sách các cài đặt ứng dụng:
  - Đổi mật khẩu
  - Cài đặt thông báo
  - Ngôn ngữ (Mặc định: Tiếng Việt)
- Nút "Đăng xuất" màu đỏ nổi bật ở cuối danh sách

**Hành động và điều hướng:**
- Bấm "Thông tin hồ sơ": sang A05 Chỉnh sửa hồ sơ
- Bấm "Quản lý địa chỉ": sang A06 Quản lý địa chỉ
- Bấm "Đổi mật khẩu": hiển thị popup hoặc sang màn nhập mật khẩu mới
- Bấm "Đăng xuất": xóa phiên đăng nhập và chuyển về A01 Đăng nhập
- Bấm nút quay lại: sang D02 Trang cá nhân (hoặc B01 Trang chủ tùy luồng vào)

**Chức năng liên quan:** dòng 4, 5, 6 bảng đặc tả chức năng (chỉnh sửa thông tin cá nhân, ảnh hồ sơ, địa chỉ).

**Trạng thái đặc biệt:**
- Trạng thái chưa liên kết email/số điện thoại: hiển thị chữ "Chưa liên kết" màu xám bên cạnh mục đó
- Bấm Đăng xuất: hiện hộp thoại xác nhận "Bạn có chắc chắn muốn đăng xuất?" trước khi thực hiện

## A05. Chỉnh sửa hồ sơ

**Ảnh:** ![A05 Chỉnh sửa hồ sơ](a05-chinh-sua-ho-so.png)

**Mục đích:** Cho phép người dùng cập nhật thông tin hiển thị với công chúng (avatar, tên hiển thị, giới thiệu).

**Thành phần chính:**
- Nút quay lại góc trái trên, tiêu đề "Chỉnh sửa hồ sơ"
- Nút "Lưu" ở góc phải trên
- Khu vực Avatar: hình tròn lớn kèm nút bấm thay đổi ảnh (icon camera)
- Ô nhập "Tên hiển thị" (có giới hạn ký tự)
- Ô nhập "Giới thiệu ngắn" (bio) cho người dùng khác xem

**Hành động và điều hướng:**
- Bấm icon camera: mở thư viện ảnh máy hoặc camera để đổi avatar
- Bấm "Lưu": lưu các thay đổi lên máy chủ và quay về A04 Quản lý thông tin cá nhân
- Bấm nút quay lại: quay về A04 Quản lý thông tin cá nhân (không lưu thay đổi)

**Chức năng liên quan:** dòng 4, 5 trong bảng đặc tả chức năng (Chỉnh sửa thông tin cá nhân, Chỉnh sửa ảnh hồ sơ).

**Trạng thái đặc biệt:**
- Quá giới hạn ký tự: ô nhập hiện viền đỏ và báo chữ nhỏ bên dưới
- Thoát khi chưa lưu: hiện thông báo "Bạn có thay đổi chưa lưu. Bạn có muốn thoát?"
- Đang tải ảnh: hiển thị vòng xoay tải (loading spinner) trên avatar

## A06. Quản lý địa chỉ

**Ảnh:** ![A06 Quản lý địa chỉ](a06-quan-ly-dia-chi.png)

**Mục đích:** Quản lý danh sách các địa chỉ dùng để giao dịch, mua bán hoặc nhận hàng.

**Thành phần chính:**
- Nút quay lại góc trái trên, tiêu đề "Quản lý địa chỉ"
- Nút "Thêm địa chỉ mới" nổi bật
- Danh sách các thẻ địa chỉ đã lưu, mỗi thẻ hiển thị:
  - Tên người nhận và Số điện thoại
  - Chi tiết địa chỉ (Số nhà, đường, phường/xã, quận/huyện, tỉnh/thành)
  - Badge "Mặc định" cho địa chỉ chính
  - Nút "Sửa" và "Xóa"

**Hành động và điều hướng:**
- Bấm "Thêm địa chỉ mới": mở màn hình/popup nhập thông tin địa chỉ mới
- Bấm "Sửa" trên thẻ: mở form chỉnh sửa cho địa chỉ tương ứng
- Bấm "Xóa" trên thẻ: xóa địa chỉ khỏi danh sách
- Bấm nút quay lại: quay về A04 Quản lý thông tin cá nhân

**Chức năng liên quan:** dòng 6 trong bảng đặc tả chức năng (Chỉnh sửa địa chỉ).

**Trạng thái đặc biệt:**
- Danh sách trống: hiển thị hình minh họa trống và dòng chữ "Bạn chưa có địa chỉ nào"
- Xóa địa chỉ: bật hộp thoại xác nhận xóa
- Không thể xóa địa chỉ mặc định duy nhất: nếu chỉ có 1 địa chỉ, ẩn hoặc làm mờ nút xóa
