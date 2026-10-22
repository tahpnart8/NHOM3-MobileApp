# Cài đặt Firebase

Hướng dẫn tạo và cấu hình Firebase project cho cả nhóm. **Trạng thái: chưa thực hiện.** Leader làm các bước 1 đến 6 và 8 đến 10; mỗi thành viên chỉ làm bước 7. Tên nút trong Firebase Console có thể khác chút so với bên dưới vì giao diện thay đổi; các thuật ngữ là chính xác.

**Cả nhóm dùng chung một Firebase project.** Không ai tạo project riêng.

## 1. Gói Spark, không gắn thẻ (đọc trước)

Project chạy ở gói **Spark** (miễn phí), **không gắn thẻ thanh toán và không bao giờ nâng lên Blaze**. Hệ quả:

- **Không dùng Cloud Storage.** Từ 03/02/2026, Cloud Storage for Firebase chỉ dùng được ở gói Blaze; ở gói Spark mọi lời gọi Storage trả lỗi 402 hoặc 403 ([Firebase FAQ về thay đổi Storage](https://firebase.google.com/docs/storage/faqs-storage-changes-announced-sept-2024)). Ảnh được nén rồi lưu dạng Base64 trong Firestore (collection `images`, ảnh thu nhỏ nằm sẵn trong tài liệu chứa nó). Không gửi video.
- **Không dùng Cloud Functions** (cũng cần Blaze). Các việc nền như tự giải ngân chạy trên máy bằng `AutoReleaseWorker`; luật chặn gian lận nằm ở Security Rules.
- **Gói Spark có hạn mức mỗi ngày và mỗi tháng.** Hạn mức cụ thể của Firestore và Authentication, và việc vượt hạn mức thì báo lỗi hay tính phí: chưa xác minh, kiểm trên trang giá của Firebase trước buổi demo.
- Mỗi tài liệu Firestore tối đa 1 MiB, nên mỗi ảnh phải nhỏ. Mức giới hạn ảnh nằm ở mục `images` của [firestore-schema.md](../data/firestore-schema.md).

Quyết định này thay các quyết định ngày 2026-09-25 về Cloud Storage trong `workflow/memory/decisions.md`.

## 2. Tạo project và mời thành viên

1. Đăng nhập [Firebase Console](https://console.firebase.google.com) bằng tài khoản Google của leader, tạo project mới.
2. Project settings, mục Users and permissions: thêm email Google của 4 thành viên với vai trò **Editor**.

## 3. Bật đăng nhập

Build, mục Authentication, tab Sign-in method: bật **Email/Password** và **Google**. Khi bật Google, chọn email hỗ trợ của project. Không bật đăng nhập bằng số điện thoại.

## 4. Tạo Firestore

Build, mục Firestore Database, Create database. Chọn chế độ **production** (khóa hết; luật bảo mật nạp ở bước 5). **Vị trí (location) không đổi được sau khi tạo**: leader chọn vùng tùy ý, ưu tiên vùng gần Việt Nam để chậm trễ thấp, và ghi vào `workflow/memory/decisions.md`. Ở gói Spark không có ràng buộc vùng miễn phí như Storage từng có.

## 5. Nạp Security Rules và chỉ mục

Luật và chỉ mục nằm trong repo, thư mục `firebase/` (`firestore.rules`, `firestore.indexes.json`), thiết kế theo [security-rules.md](../data/security-rules.md). Leader nạp bằng Firebase CLI (`firebase deploy --only firestore`) hoặc dán vào Console, mục Firestore Database, tab Rules và Indexes. Mỗi lần luật đổi trong repo thì nạp lại. Chỉ leader sửa thư mục `firebase/`.

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
- Dữ liệu được bảo vệ bằng **Security Rules**, không phải bằng khóa. Không đưa ứng dụng vào dùng thật khi Firestore còn ở chế độ mở.
- GitHub secret scanning có thể cảnh báo về khóa API của Google; sau khi giới hạn khóa, đóng cảnh báo với lý do "used in tests" hoặc tương đương.

## 10. Quản trị viên

Ứng dụng nhận diện quản trị viên bằng dữ liệu phía server, không bằng trường ứng dụng gửi lên (`AGENTS.md` R12): một người dùng là admin khi có tài liệu `admins/{uid}`. Mọi admin có quyền như nhau.

Security Rules không cho ứng dụng ghi vào `admins`, nên **leader tạo tay**:

1. Người cần làm admin đăng ký tài khoản trong ứng dụng.
2. Leader mở Authentication, tab Users, chép **User UID** của tài khoản đó.
3. Leader mở Firestore Database, tạo collection `admins` (lần đầu), thêm tài liệu có mã bằng đúng UID đó. Tài liệu chỉ cần tồn tại; có thể để một trường tùy ý, ví dụ `note`.
4. Người đó đăng xuất rồi đăng nhập lại; D01 hiện nút vào khu quản trị.
