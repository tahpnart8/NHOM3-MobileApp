# Barem điểm và chỗ dự án đáp ứng từng dòng

Thầy chấm đồ án theo 11 dòng dưới đây. Tệp này nói **mỗi dòng được đáp ứng ở đâu trong dự án**, để không ai làm xong rồi mới phát hiện thiếu một dòng. Nội dung các chương là các bài giảng trong thư mục `lecture` của thư mục mẹ `Mobile_App` (tên tệp `0N.x`, nên chương N gồm mọi tệp bắt đầu bằng `0N.`).

Đồ án là **100% của điểm thi cuối kỳ**, tức 50% điểm môn (slide `00 Android Developer Syllabus.pptx`, mục Assessment).

## 1. Nhóm điểm code

| Dòng barem | Nghĩa | Dự án đáp ứng ở đâu |
| --- | --- | --- |
| C-Code organization | Code chia tầng rõ, đặt tên nhất quán, không trộn việc | Gói chia theo tầng: `model`, `model.enums`, `data`, `data.local`, `util`, `viewmodel`, `ui.<module>` ([conventions/android-java.md](../conventions/android-java.md), [so-huu.md](../../plan/so-huu.md) mục 2). Luật viết Java ở skill `pubg-code`, kiểm bằng `workflow/scripts/check-java-style.sh` |
| C-Functions | Chức năng chạy đủ, đúng luồng | 91 chức năng "Đợt này" ở [dac-ta-chuc-nang.md](dac-ta-chuc-nang.md), gom thành 14 tính năng ở [features.md](features.md), mỗi chức năng có use case trong [use-case/](use-case/README.md) |
| C-APIs | Dùng API ngoài đúng cách | Firebase Authentication (email và Google), Cloud Firestore, Room; đăng nhập Google là yêu cầu riêng của thầy. Xem [overview.md](../architecture/overview.md) và [firebase-setup.md](../workflow/firebase-setup.md) |
| C-GUI | Giao diện nhất quán, dùng được | 33 mã màn hình ở [ui/README.md](../ui/README.md); hệ thống thiết kế (token màu, thang khoảng cách, bốn trạng thái) ở [conventions/giao-dien.md](../conventions/giao-dien.md); skill `pubg-ui` là luật thi hành |
| C-Task distribution | Chia việc đều và chứng minh được | [so-huu.md](../../plan/so-huu.md) chia module, màn hình, lớp và từng phương thức cho 5 người; mỗi issue có một người làm, mỗi Pull Request một người; lịch sử Git là bằng chứng |

## 2. Nhóm điểm theo chương bài giảng

| Dòng barem | Chương gồm những gì | Dự án đáp ứng ở đâu |
| --- | --- | --- |
| R-Chapter 1, 2, 6, 7 | **1**: giới thiệu Android, layout và resource, text và scrolling view. **2**: Activity và Intent, vòng đời và trạng thái, implicit Intent. **6**: UI testing. **7**: async task, kiểm tra kết nối mạng, broadcast, service | Xem mục 3 dưới đây: đây là dòng gộp bốn chương nên dễ hở nhất |
| R-Chapter 3 | Debugger của Android Studio, app testing (unit test JUnit), Android Support Library | Unit test: P3-T và tiêu chí "mỗi quy tắc một unit test" trong từng issue. Support Library: toàn bộ AndroidX trong [libs.versions.toml](../../../PUBGApp/gradle/libs.versions.toml) |
| R-Chapter 4 | Button và ảnh bấm được, input control, menu và picker, điều hướng người dùng, RecyclerView | RecyclerView: mọi màn có danh sách. Điều hướng: Navigation component, ba Activity, hành động toàn cục ([so-huu.md](../../plan/so-huu.md) mục 4). Menu và picker: xem mục 3 |
| R-Chapter 5 | Drawable, style, theme; Material Design; resource cho layout thích ứng | [conventions/giao-dien.md](../conventions/giao-dien.md) mục 1 đến 3 (token, Material Design), các việc P3-A đến P3-E (bản `layout-land`) |

## 3. Bảng kỹ thuật: chương nào đòi gì, làm ở đâu

Cột cuối ghi "**có**" khi kế hoạch đã có chỗ làm, "**hở**" khi chưa ai làm.

| Chương | Kỹ thuật thầy dạy | Chỗ dự án dùng nó | Trạng thái |
| --- | --- | --- | --- |
| 1 | Layout, resource, text và scrolling view | Mọi màn; `res/values/` chia theo module | có |
| 2 | Activity, Intent tường minh | Ba Activity (`AuthActivity`, `MainActivity`, `AdminActivity`), đi lại giữa chúng | có |
| 2 | Vòng đời và giữ trạng thái khi xoay | Trạng thái nằm trong ViewModel; các việc P3-A đến P3-E | có |
| 2 | **Implicit Intent** | Chọn ảnh từ thư viện và chụp ảnh (đăng tin, chat, vận đơn, bằng chứng, xác thực); chia sẻ tệp CSV của "Báo cáo của tôi" (`ACTION_SEND`) | có, nhưng phải nêu tên kỹ thuật trong issue để chấm được |
| 3 | Debugger | Không phải chức năng; nêu trong báo cáo và buổi bảo vệ | có |
| 3 | **Unit test JUnit** | P3-T; mỗi issue có tiêu chí "một unit test cho mỗi phương thức quy tắc" | có |
| 3 | Support Library (AndroidX) | Toàn bộ thư viện trong `libs.versions.toml` | có |
| 4 | Button, input control | Mọi biểu mẫu | có |
| 4 | **Menu và picker** | Menu overflow trên app bar của B05 (kệ hàng) và E04; `DatePicker` cho ngày sinh (A02) và khoảng ngày tham gia (E02) | có, nhưng phải nêu tên kỹ thuật trong issue |
| 4 | Điều hướng | Navigation component, hành động toàn cục | có |
| 4 | RecyclerView | Mọi danh sách và lưới | có |
| 5 | Drawable, style, theme | `colors.xml`, `themes.xml`, `giao-dien.md` mục 1 | có |
| 5 | Material Design | Material Components, token màu, chip trạng thái | có |
| 5 | **Layout thích ứng, thư mục resource thay thế** | `res/layout-land/`, các việc P3 | có |
| 6 | **UI testing (Espresso)** | Chưa có việc nào trong kế hoạch, dù `espresso-core` đã có trong build | **hở** |
| 7 | **Async** | Mọi lệnh gọi Firebase đi qua `DataCallback`, không chạy trên luồng chính | có |
| 7 | **Kiểm tra kết nối mạng** | `NetworkMonitor`, dải báo offline, bộ nhớ đệm Room | có |
| 7 | **Broadcast** | Chưa có chỗ nào gửi hay nhận broadcast | **hở** |
| 7 | **Service** | `AutoReleaseWorker` là `Worker` của WorkManager, không phải `Service` | **hở một phần** |

Ngoài barem nhưng thầy có dạy và có yêu cầu riêng: chương 9 (`SharedPreferences`, app settings) chưa dùng ở đâu; chương 10 (SQLite, Room, LiveData, ViewModel) đã dùng đầy đủ; chương 8 (notification, alarm) dự án dùng thông báo trong ứng dụng (tài liệu Firestore), không dùng notification của hệ điều hành.

## 4. Ba chỗ hở, đề xuất bịt

Ba dòng dưới đây chưa có việc nào trong `workflow/plan/`. Leader quyết có thêm hay không; thêm thì mở issue mới, không nhét vào issue đang có (luật R19).

| Chỗ hở | Đề xuất | Vì sao đáng làm |
| --- | --- | --- |
| UI testing (chương 6) | Một việc mới `P3-U`: 3 đến 5 kịch bản Espresso cho các luồng xương sống (đăng nhập, đăng tin, gửi đề nghị, duyệt xác thực) | Đây là một dòng barem riêng, hiện bằng không. `espresso-core` đã có trong build nên không cần thêm thư viện |
| Broadcast (chương 7) | `AutoReleaseWorker` gửi một broadcast nội bộ khi nó chuyển một khoản thanh toán sang đã giải ngân; màn C03 và C04 đang mở thì nhận và tự cập nhật | Việc này có thật trong luồng nghiệp vụ, không phải làm cho có. Gắn vào `P2-E6b` hoặc một việc nhỏ riêng |
| Service (chương 7) | Hoặc viết một `Service` ngắn cho việc nén và lưu nhiều ảnh khi đăng tin, hoặc giữ WorkManager và giải thích trong báo cáo rằng WorkManager là bản thay thế hiện đại của `Service` cho việc chạy nền có điều kiện | Nếu chọn cách giải thích thì phải viết hẳn một mục trong báo cáo, kèm trích slide 7.4, chứ không bỏ trống |

## 5. Hai dòng còn lại

| Dòng barem | Chuẩn bị ở đâu |
| --- | --- |
| Presentation | Buổi bảo vệ: dựa trên [features.md](features.md), [ui/README.md](../ui/README.md) và bản demo. Mỗi người trình bày phần module của mình, khớp với [so-huu.md](../../plan/so-huu.md) để chứng minh dòng C-Task distribution |
| QA | Thầy hỏi bất kỳ chỗ nào. Người trả lời phải nắm tài liệu của module mình: [trang-thai.md](../architecture/trang-thai.md), [security-rules.md](../data/security-rules.md) và use case của module đó |

Mọi issue phải ghi dòng barem mà nó phục vụ, trong mục "Làm gì và vì sao". Thiếu dòng đó thì lúc bảo vệ không ai chỉ ra được chỗ nào trong code đáp ứng chương nào.
