# Cài đặt Firebase

Hướng dẫn tạo và cấu hình Firebase project cho cả nhóm. **Trạng thái: chưa thực hiện.** Leader làm các bước 1 đến 6 một lần; mỗi thành viên chỉ làm bước 7. Tên nút trong Firebase Console có thể khác chút so với bên dưới vì giao diện thay đổi; các thuật ngữ là chính xác.

**Cả nhóm dùng chung một Firebase project.** Không ai tạo project riêng.

## 1. Gói Blaze và thẻ thanh toán (đọc trước)

Từ **03/02/2026**, Cloud Storage for Firebase chỉ dùng được khi project ở gói **Blaze** (trả theo mức dùng, phải gắn tài khoản thanh toán). Với gói Spark, mọi lời gọi Storage trả lỗi 402 hoặc 403. Blaze vẫn có mức miễn phí: bucket mới dạng `PROJECT_ID.firebasestorage.app` hưởng mức "Always Free" của Google Cloud Storage nếu đặt ở vùng `us-central1`, `us-east1` hoặc `us-west1`. Nguồn: [Firebase FAQ về thay đổi Storage](https://firebase.google.com/docs/storage/faqs-storage-changes-announced-sept-2024).

Chỉ một người (nên là leader) gắn thẻ. Quyết định dùng Cloud Storage nằm ở `memory/decisions.md`.

**Leader đã xác nhận: giữ Cloud Storage cho mọi tệp media, gồm cả ảnh và video.** Chi phí bằng 0 khi dùng trong hạn mức miễn phí "Always Free" của Google Cloud Storage ([nguồn](https://docs.cloud.google.com/free/docs/free-cloud-features), kiểm ngày 2026-09-25), **nhưng vẫn phải gắn thẻ** vào tài khoản thanh toán, kể cả khi không mất đồng nào:

| Hạn mức mỗi tháng | Con số | Điều kiện |
| --- | --- | --- |
| Dung lượng lưu | 5 GB-tháng | Chỉ tính ở `us-central1`, `us-east1`, `us-west1`; các vùng khác **không** được miễn phí |
| Thao tác ghi, liệt kê (Class A), gồm mỗi lần tải một tệp lên | 5.000 | |
| Thao tác đọc (Class B), gồm mỗi lần tải một tệp xuống | 50.000 | |
| Dữ liệu ra ngoài | 100 GB | Tính từ Bắc Mỹ tới nơi nhận (trừ Trung Quốc và Úc) |

Vượt hạn mức nào thì phần vượt tính tiền theo bảng giá của Google Cloud. Vì vậy:

- **Chọn vùng của bucket là một trong ba vùng Mỹ ở trên** khi tạo Storage. Vùng không đổi được sau khi tạo.
- **Đặt cảnh báo ngân sách thấp** (ví dụ vài đô). Cảnh báo chỉ nhắn tin cho bạn, **không tự chặn** chi tiêu.
- **Luật Storage phải giới hạn kích thước và loại tệp** (ảnh nhỏ, video có trần dung lượng) và bắt buộc đăng nhập. Ai cũng tự đăng ký được tài khoản trong ứng dụng và `google-services.json` nằm công khai trong repo, nên luật là thứ duy nhất ngăn một người lạ đẩy đầy 5 GB.
- Video tốn nhiều hơn ảnh: mỗi lần xem là một lần tải xuống (tính vào 50.000 thao tác đọc và 100 GB dữ liệu ra). Với đồ án và buổi demo thì rất dư, nhưng nên giới hạn thời lượng hoặc dung lượng mỗi video khi thiết kế tính năng.
- Chưa xác minh: hạn mức miễn phí chính xác của Firestore và Authentication theo vùng.

## 2. Tạo project và mời thành viên

1. Đăng nhập [Firebase Console](https://console.firebase.google.com) bằng tài khoản Google của leader, tạo project mới.
2. Project settings, mục Users and permissions: thêm email Google của 4 thành viên với vai trò **Editor**.

## 3. Bật đăng nhập

Build, mục Authentication, tab Sign-in method: bật **Email/Password** và **Google**. Khi bật Google, chọn email hỗ trợ của project.

## 4. Tạo Firestore

Build, mục Firestore Database, Create database. Chọn chế độ **production** (khóa hết; luật bảo mật sẽ nạp sau). **Vị trí (location) không đổi được sau khi tạo**: leader chọn và ghi vào `memory/decisions.md`.

## 5. Tạo Cloud Storage

Sau khi đã ở gói Blaze: Build, mục Storage, Get started. Chọn vùng theo mục 1.

## 6. Đăng ký ứng dụng Android

Project settings, Add app, chọn Android. Package name phải là đúng `com.nhom3.pubgapp`. Sau bước 7 mới tải `google-services.json`.

## 7. Mỗi thành viên: SHA-1 của khóa debug

Đăng nhập Google chỉ chạy được trên máy có SHA-1 đã đăng ký; nếu không, ứng dụng báo lỗi `DEVELOPER_ERROR`. Mỗi máy có khóa debug riêng.

1. Chạy ứng dụng ít nhất một lần từ Android Studio để tệp khóa được tạo.
2. Mở PowerShell, chạy (đã kiểm chứng trên máy leader; `keytool` nằm trong thư mục `bin` của JDK, có trên PATH khi cài JDK):

   ```powershell
   keytool -list -v -keystore "$env:USERPROFILE\.android\debug.keystore" -alias androiddebugkey -storepass android -keypass android
   ```

3. Lấy dòng `SHA1:` (chuỗi các cặp ký tự hex cách nhau bằng dấu hai chấm) và gửi cho leader trong nhóm chat. Không dán SHA-1 vào issue hoặc commit.
4. Leader thêm vào: Project settings, Your apps, ứng dụng Android, Add fingerprint.

Cách khác nếu không có `keytool`: chạy `gradlew.bat signingReport` trong `PUBGApp/` và lấy SHA1 của biến thể `debug`.

## 8. Tải `google-services.json`

**Chỉ tải sau khi mọi SHA-1 đã được thêm.** Thêm SHA-1 về sau thì phải tải lại tệp, vì tệp chứa thông tin OAuth sinh theo SHA-1. Đặt tệp ở `PUBGApp/app/google-services.json` và commit qua một issue (đây là tệp cấu hình duy nhất được phép commit; `AGENTS.md` R11).

## 9. Bảo vệ khóa API vì repo công khai

`google-services.json` chứa khóa API. Khóa này là định danh, không phải bí mật, nhưng repo công khai nên cần giới hạn:

- Trong Google Cloud Console, mục APIs and Services, Credentials: giới hạn khóa Android theo **package `com.nhom3.pubgapp` và SHA-1**.
- Dữ liệu được bảo vệ bằng **Security Rules**, không phải bằng khóa. Không đưa ứng dụng vào dùng thật khi Firestore và Storage còn ở chế độ mở.
- GitHub secret scanning có thể cảnh báo về khóa API của Google; sau khi giới hạn khóa, đóng cảnh báo với lý do "used in tests" hoặc tương đương.

## 10. Quản trị viên

Ứng dụng nhận diện quản trị viên bằng dữ liệu phía server, không bằng trường ứng dụng gửi lên (`AGENTS.md` R12). Cách cụ thể sẽ được chốt khi có yêu cầu chức năng quản trị.
