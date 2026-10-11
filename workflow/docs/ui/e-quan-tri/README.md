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

**Ảnh:** chưa nộp

**Mục đích:**

**Thành phần chính:**
-

**Hành động và điều hướng:**
-

**Chức năng liên quan:**

**Trạng thái đặc biệt:**
-

## E03. Admin: Chi tiết người dùng

**Ảnh:** chưa nộp

**Mục đích:**

**Thành phần chính:**
-

**Hành động và điều hướng:**
-

**Chức năng liên quan:**

**Trạng thái đặc biệt:**
-

## E04. Admin: Kiểm duyệt nội dung

**Ảnh:** chưa nộp

**Mục đích:**

**Thành phần chính:**
-

**Hành động và điều hướng:**
-

**Chức năng liên quan:**

**Trạng thái đặc biệt:**
-

## E05. Admin: Quản lý danh mục

**Ảnh:** chưa nộp

**Mục đích:**

**Thành phần chính:**
-

**Hành động và điều hướng:**
-

**Chức năng liên quan:**

**Trạng thái đặc biệt:**
-

## E06. Admin: Giám sát giao dịch

**Ảnh:** chưa nộp

**Mục đích:**

**Thành phần chính:**
-

**Hành động và điều hướng:**
-

**Chức năng liên quan:**

**Trạng thái đặc biệt:**
-
