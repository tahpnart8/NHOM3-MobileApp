# Nhóm A: Tài khoản và thông tin cá nhân

6 màn hình, nộp theo 3 đợt: đợt 1 một màn (A01), đợt 2 hai màn (A02, A03), đợt 3 ba màn (A04, A05, A06).

## A01. Đăng nhập

**Ảnh:** ![A01 Đăng nhập](a01-dang-nhap.png)

**Mục đích:** Cho người dùng đã có tài khoản vào ứng dụng, bằng email hoặc số điện thoại, hoặc bằng tài khoản Google.

**Thành phần chính:**
- Logo và tên ứng dụng ĐồCũ
- Tiêu đề "Chào mừng trở lại" và một dòng giới thiệu ngắn
- Ô nhập "Email hoặc số điện thoại"
- Ô nhập "Mật khẩu", có thể ẩn hiện ký tự
- Liên kết "Quên mật khẩu?" nằm dưới ô mật khẩu, canh phải
- Nút "Đăng nhập" là nút chính
- Đường kẻ phân cách với chữ "hoặc"
- Nút "Đăng nhập bằng Google" có biểu tượng Google
- Dòng cuối trang: "Chưa có tài khoản? Đăng ký ngay"

**Hành động và điều hướng:**
- Bấm "Đăng nhập" khi đúng thông tin: sang B01 Trang chủ
- Bấm "Đăng nhập bằng Google": sang B01 Trang chủ sau khi chọn tài khoản Google
- Bấm "Quên mật khẩu?": sang A03 Quên mật khẩu
- Bấm "Đăng ký ngay": sang A02 Đăng ký

**Chức năng liên quan:** dòng 1, 2, 3 trong bảng đặc tả chức năng (đăng ký, đăng nhập kèm đăng nhập Google, quên mật khẩu).

**Trạng thái đặc biệt:**
- Sai email hoặc mật khẩu: hiện dòng báo lỗi màu đỏ ngay dưới ô nhập sai, không chuyển màn
- Tài khoản đang bị khóa: hiện thông báo nêu rõ lý do và thời hạn khóa
- Đang gửi yêu cầu: nút "Đăng nhập" chuyển sang trạng thái chờ, không bấm lại được
- Chưa xác thực email: nhắc người dùng mở hộp thư để xác thực trước khi vào

## A02. Đăng ký

**Ảnh:** ![A02 Đăng ký](a02-dang-ky.png)

**Mục đích:** Cho phép người dùng chưa có tài khoản tạo tài khoản mới bằng email.

**Thành phần chính:**
- Tiêu đề "Đăng ký" và một dòng hướng dẫn ngắn
- Ô nhập "Tên hiển thị"
- Ô nhập "Email"
- Ô nhập "Mật khẩu", có thể ẩn hiện ký tự
- Ô nhập "Xác nhận mật khẩu", có thể ẩn hiện ký tự
- Nút "Đăng ký" là nút chính
- Đường kẻ phân cách với chữ "hoặc"
- Nút "Đăng nhập bằng Google" có biểu tượng Google
- Dòng cuối trang: "Đã có tài khoản? Đăng nhập"

**Hành động và điều hướng:**
- Bấm "Đăng ký" khi thông tin hợp lệ: hiển thị thông báo yêu cầu xác thực email, sau đó sang A01 Đăng nhập
- Bấm "Đăng nhập bằng Google": sang B01 Trang chủ (nếu chọn tài khoản hợp lệ)
- Bấm "Đăng nhập": trở về A01 Đăng nhập

**Chức năng liên quan:** dòng 1, 2 trong bảng đặc tả chức năng (đăng ký, đăng nhập kèm đăng nhập Google).

**Trạng thái đặc biệt:**
- Email đã tồn tại: hiện dòng báo lỗi màu đỏ ngay dưới ô email
- Mật khẩu không khớp: hiện báo lỗi dưới ô xác nhận mật khẩu
- Mật khẩu quá yếu: hiện báo lỗi (ít nhất 8 ký tự, gồm chữ và số)
- Đang gửi yêu cầu: nút "Đăng ký" chuyển sang trạng thái chờ, không bấm lại được

## A03. Quên mật khẩu

**Ảnh:** ![A03 Quên mật khẩu](a03-quen-mat-khau.png)

**Mục đích:** Hỗ trợ người dùng lấy lại quyền truy cập khi quên mật khẩu bằng cách gửi liên kết đặt lại mật khẩu qua email.

**Thành phần chính:**
- Nút mũi tên quay lại ở góc trái trên
- Tiêu đề "Quên mật khẩu" và dòng giải thích "Nhập email của bạn để nhận liên kết đặt lại mật khẩu"
- Ô nhập "Email"
- Nút "Gửi liên kết" là nút chính
- Dòng cuối trang: "Nhớ mật khẩu? Đăng nhập"

**Hành động và điều hướng:**
- Bấm "Gửi liên kết" khi email hợp lệ: thông báo thành công và giữ ở màn hình hiện tại hoặc quay về A01 Đăng nhập
- Bấm nút quay lại hoặc "Đăng nhập": trở về A01 Đăng nhập

**Chức năng liên quan:** dòng 3 trong bảng đặc tả chức năng (quên mật khẩu).

**Trạng thái đặc biệt:**
- Email chưa được đăng ký hoặc sai định dạng: báo lỗi màu đỏ dưới ô nhập
- Đang gửi yêu cầu: nút "Gửi liên kết" chuyển trạng thái chờ
- Đã gửi quá nhiều yêu cầu: thông báo tạm khóa chức năng này trong vài phút

## A04. Quản lý thông tin cá nhân

**Ảnh:** chưa nộp

**Mục đích:**

**Thành phần chính:**
-

**Hành động và điều hướng:**
-

**Chức năng liên quan:**

**Trạng thái đặc biệt:**
-

## A05. Chỉnh sửa hồ sơ

**Ảnh:** chưa nộp

**Mục đích:**

**Thành phần chính:**
-

**Hành động và điều hướng:**
-

**Chức năng liên quan:**

**Trạng thái đặc biệt:**
-

## A06. Quản lý địa chỉ

**Ảnh:** chưa nộp

**Mục đích:**

**Thành phần chính:**
-

**Hành động và điều hướng:**
-

**Chức năng liên quan:**

**Trạng thái đặc biệt:**
-
