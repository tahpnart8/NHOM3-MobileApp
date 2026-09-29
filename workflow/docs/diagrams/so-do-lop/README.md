# Sơ đồ lớp

## 1. Kiến trúc MVVM kết hợp Repository

Sơ đồ thể hiện 85 lớp và giao diện, được tổ chức theo kiến trúc **MVVM (Model-View-ViewModel)** kết hợp mẫu thiết kế **Repository**. Đây là kiến trúc được chọn để đáp ứng hai yêu cầu chính của môn học: chia tách logic nghiệp vụ khỏi giao diện, và có khả năng hoạt động offline.

Ba tầng chính của ứng dụng:

- **Tầng View (Activity/Fragment):** Không có mặt trong sơ đồ này. Lý do: đây là sơ đồ lớp phân tích miền dữ liệu và nghiệp vụ, tập trung vào cách tổ chức logic.
- **Tầng ViewModel:** Đóng vai trò cầu nối. Chứa 12 lớp (như `AuthViewModel`, `ListingViewModel`, `ChatViewModel`). Các lớp này giữ trạng thái cho giao diện, gọi hàm từ Repository, và không trực tiếp truy cập cơ sở dữ liệu.
- **Tầng Data (Repository, Model, Local):** Là phần cốt lõi của sơ đồ, chứa 73 lớp.
  - **Repository (12 lớp):** Ẩn chi tiết lưu trữ (như `AuthRepository`, `ListingRepository`). Tầng ViewModel chỉ biết Repository, không biết Firebase hay Room. Tất cả đều kế thừa `BaseRepository`.
  - **Local (6 lớp):** Chịu trách nhiệm bộ nhớ đệm ngoại tuyến bằng Room SQLite (yêu cầu môn học), bao gồm `AppDatabase`, `ListingDao`, `ChatDao` và các Entity tương ứng. `NetworkMonitor` quyết định lúc nào dùng Local.
  - **Model:** Chứa 17 lớp tài liệu chính (thực thể Firestore) và 20 kiểu liệt kê (Enum).

## 2. Mô hình miền và kiểu liệt kê

**Nhóm lớp mô hình miền (Document/Entity):**
Gồm 17 lớp đại diện cho các thực thể lưu trong Cloud Firestore. Tất cả đều kế thừa lớp trừu tượng `BaseDocument` chứa hai thuộc tính chung `id` (mã tự sinh) và `createdAt`/`updatedAt` (thời điểm). Ví dụ: `User`, `Listing`, `Deal`, `Message`, `Complaint`.

**Nhóm kiểu liệt kê (Enum):**
Ứng dụng sử dụng 20 kiểu liệt kê để giới hạn giá trị của các trạng thái và loại dữ liệu, tránh sai sót khi nhập chuỗi thủ công. Nhóm này rất đa dạng, phản ánh tính chất nghiệp vụ phức tạp của một sàn giao dịch kết hợp mạng xã hội:
- **Trạng thái thực thể:** `UserStatus`, `ListingStatus`, `DealStatus`, `PaymentStatus`, `ReportStatus`, `ComplaintStatus`.
- **Hành vi và hình thức:** `TransactionType` (Bán, Trao đổi, Cho/Tặng), `TargetRole` (Mua, Bán), `ActivityType` (nhật ký hoạt động).
- **Phân loại khác:** `TargetType`, `ReportTarget`, `Priority`, `ComplaintDecision`.

## 3. Thể hiện OOP

Sơ đồ tuân thủ chặt chẽ 4 tính chất của Lập trình Hướng đối tượng:

- **Đóng gói (Encapsulation):** Mọi thuộc tính của các lớp tài liệu đều để private (dấu `-`), truy cập qua getter/setter (không vẽ getter/setter để tránh rối sơ đồ, luật mặc định). Repository đóng gói logic mạng.
- **Kế thừa (Inheritance):** Thể hiện rõ nhất ở lớp `BaseDocument` (mũi tên rỗng). 17 lớp thực thể đều kế thừa `BaseDocument`. `BaseRepository` là lớp cha của 12 repository, chứa các hàm dùng chung như ghi log hoạt động.
- **Đa hình (Polymorphism):** Giao diện `DataCallback<T>` được cài đặt (implement) theo nhiều kiểu khác nhau trong các ViewModel để nhận kết quả từ Repository, thay thế cho lambda expression theo đúng luật viết mã của nhóm.
- **Trừu tượng (Abstraction):** Tầng ViewModel chỉ giao tiếp với Repository qua các hàm trừu tượng (như `loadListings()`, `submitReport()`), không biết bên dưới lấy dữ liệu từ Firebase hay Room.

## 4. Các trang sơ đồ

### Trang 0: Tổng quan và chú giải
![Trang 0: Tổng quan và chú giải](class-0-tong-quan.png)

### Trang 1: Người dùng, danh mục và tin đăng
![Trang 1: Người dùng, danh mục và tin đăng](class-1-nguoi-dung.png)

### Trang 2: Nhắn tin, đề nghị và tìm kiếm
![Trang 2: Nhắn tin, đề nghị và tìm kiếm](class-2-tin-dang-tim-kiem.png)

### Trang 3: Trao đổi, thanh toán và đánh giá
![Trang 3: Trao đổi, thanh toán và đánh giá](class-3-trao-doi-giao-dich.png)

### Trang 4: Độ tin cậy và quản trị (Admin)
![Trang 4: Độ tin cậy và quản trị](class-4-tin-cay-quan-tri.png)

## 5. Thống kê số lượng lớp

(Chưa điền - để phần sau điền nốt)

| Gói | Document/Entity | Enum | Dao/Database | Repository/Base | ViewModel | **Tổng cộng** |
| --- | --- | --- | --- | --- | --- | --- |
| `model` | | | | | | |
| `enums` | | | | | | |
| `local` | | | | | | |
| `data` | | | | | | |
| `viewmodel` | | | | | | |
| `util` | | | | | | |
| **Tổng** | | | | | | **85** |
