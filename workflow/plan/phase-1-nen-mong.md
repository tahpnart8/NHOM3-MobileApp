# P1. Nền móng

**Thời gian:** 13/10. **Chủ:** Phát, trừ P1-09 là việc của cả nhóm.

**Mục tiêu:** sau giai đoạn này, mỗi thành viên mở project là có đủ:

- ba Activity và khung của mọi màn hình nối sẵn điều hướng;
- toàn bộ lớp mô hình, enum, repository, DAO đúng tên và chữ ký trong `workflow/docs/architecture/dac-ta-lop/`;
- Firebase chạy được, ảnh lưu được.

Từ đó P2 chỉ còn việc điền nội dung, không ai phải tạo file dùng chung.

**Điều kiện hoàn thành:**

- `./gradlew assembleDebug testDebugUnitTest lintDebug` xanh trên `main`.
- App mở ra khung màn đăng nhập A01. Màn này làm thật ở P2-A1, việc đầu tiên của giai đoạn 2.
- Với người đã đăng nhập, app vào `MainActivity` có 5 tab; mỗi tab hiện khung trống ghi mã màn hình.
- `check-java-style.sh` không báo lỗi.

| Mã | Việc | Phụ thuộc | Kết quả |
| --- | --- | --- | --- |
| P1-01 | Tạo Firebase project gói **Spark**: bật Authentication (email, Google), tạo Firestore, thêm app Android `com.nhom3.pubgapp`, nhận SHA-1 của 5 máy, mời 4 thành viên, commit `google-services.json`, làm theo `workflow/docs/workflow/firebase-setup.md` | P0-02 | Project Firebase dùng được |
| P1-02 | Pull Request `build(deps)`: Firebase BoM (Auth, Firestore), plugin google-services, Credential Manager và Google ID (đăng nhập Google), Room (runtime, compiler), WorkManager, Navigation (fragment, ui), Lifecycle (ViewModel, LiveData), Glide. Phiên bản tra trang chính thức lúc làm | P1-01 | Build xanh với thư viện mới |
| P1-03 | Gói `model` và `model.enums`: mọi lớp và enum đúng đặc tả, bốn luật ánh xạ Firestore (`@DocumentId`, `@Exclude`, `@PropertyName`, giờ máy chủ), hằng tên trường Firestore, quy tắc nghiệp vụ (`Deal.canMoveTo`, `Listing.acceptsOffer`, `Warning.resultingStatus`...). Unit test cho mọi phương thức có quy tắc | P1-02 | Lớp miền và test xanh |
| P1-04 | Gói `data` và `util`: `DataCallback` (kèm hằng `NOT_READY`), `BaseRepository`, 12 repository đủ chữ ký (thân chờ người làm P2), `MediaRepository` làm thật (nén, Base64 vào collection `images`, ảnh thu nhỏ, bộ nhớ đệm), `PubgApplication` khởi tạo `NetworkMonitor`, Room và WorkManager, `NotificationRepository.create`, `CategoryRepository.getAll` và `getChildren`, `SessionManager`, `NetworkMonitor`, `FormatUtils`, `KeywordFilter`. Chạy việc mạng và Room ngoài luồng chính bằng một bộ thực thi dùng chung | P1-03 | Repository gọi được, phương thức chưa làm báo lỗi rõ |
| P1-05 | Gói `data.local`: `AppDatabase`, `Converters`, 6 entity, 4 DAO đúng `room-schema.md` | P1-03 | Room tạo được cơ sở dữ liệu |
| P1-06 | Khung giao diện: `AuthActivity`, `MainActivity` (thanh dưới 5 tab), `AdminActivity`; khung của mọi Fragment ở `so-huu.md` mục 3; `nav_main.xml` với mọi hành động toàn cục ở `so-huu.md` mục 4; các đồ thị con và file chuỗi theo module; theme, màu, cỡ chữ theo mockup; thành phần chung (trạng thái rỗng, đang tải, dải báo offline) | P1-02 | Điều hướng đủ mọi màn, mỗi màn hiện mã của nó |
| P1-07 | `firebase/firestore.rules` bản đầu theo `workflow/docs/data/security-rules.md` phần nền: phải đăng nhập; chặn đọc `users/*/private/*` và `complaints/*/adminNote/*` trừ chủ và admin; chặn ghi `admins`; giới hạn kích thước ảnh. Hướng dẫn dán vào Console | P1-01 | Luật nền đã áp lên project |
| P1-08 | Ghi `workflow/memory/code/firebase.md` và `room.md`: các bẫy đã gặp khi dựng nền | P1-04, P1-05 | Ghi chú cho cả nhóm |
| P1-09 | **Cả nhóm**: chạy `setup-dev`, gửi SHA-1, đăng nhập thử, đọc tài liệu module của mình | P1-01 | Mọi máy build và đăng nhập được |

Thứ tự Phát làm trong ngày: P1-01, P1-02, P1-06 (để thành viên bắt đầu làm layout sớm), P1-03, P1-04, P1-05, P1-07, P1-08.
