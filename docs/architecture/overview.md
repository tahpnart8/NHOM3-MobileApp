# Kiến trúc ứng dụng

Trạng thái: **khung đề xuất, chờ leader duyệt**. Chỉ những gì ghi "đã có" mới có thật trong code. Khi leader chốt một mục, ghi quyết định vào `memory/decisions.md` và đổi chữ "đề xuất" thành "đã chốt" ở đây.

## Đã có

- Project Android `PUBGApp/`, gói `com.nhom3.pubgapp`, Java 17, XML Views, ViewBinding, minSdk 30.
- Chỉ có một màn hình mẫu `MainActivity`. **Chưa** có Firebase, Room hay điều hướng trong build.

## Đề xuất: các lớp

```
ui      Activity, Fragment, Adapter        chỉ hiển thị và bắt sự kiện
data    Repository, nguồn Firestore, Room  nơi duy nhất nói chuyện với Firebase và SQLite
model   lớp dữ liệu thuần                   không phụ thuộc Android
```

Màn hình lấy dữ liệu qua `ViewModel` và `LiveData` (bài 10.1 của môn học) để trạng thái sống sót khi xoay màn hình.

## Đề xuất: cấu trúc gói

```
com.nhom3.pubgapp
├── common                    dùng chung (tiện ích, lớp cơ sở, kết nối mạng)
└── feature
    ├── auth      {ui, data, model}
    ├── listing   {ui, data, model}
    └── ...       mỗi tính năng một gói
```

Chia theo tính năng để một issue chỉ đụng một gói, giảm xung đột giữa các thành viên.

## Đề xuất: điều hướng

Cần chốt: một Activity với nhiều Fragment và thanh điều hướng dưới, hay nhiều Activity. Mỗi tính năng gắn vào điều hướng qua **một điểm đăng ký duy nhất** để không nhiều người cùng sửa một file. Chưa chốt.

## Đề xuất: dữ liệu và offline

- Firestore là nguồn dữ liệu chính; Room chỉ là bộ nhớ đệm để đọc lại khi mất mạng.
- Luồng đọc: hiển thị dữ liệu Room ngay, cập nhật từ Firestore, ghi lại vào Room.
- Việc ghi dữ liệu khi offline chưa được quyết định.
- Không làm việc mạng hoặc đĩa trên luồng chính.

## Bảo mật

- Ai là ai và có quyền gì do **Firebase Authentication và Security Rules** quyết định, không phải trường mà ứng dụng gửi lên.
- `google-services.json` được commit (chỉ chứa định danh công khai). Phải giới hạn API key theo package `com.nhom3.pubgapp` và SHA-1, và viết Security Rules trước khi có dữ liệu thật.
- Mỗi thành viên đăng ký SHA-1 của `debug.keystore` trên máy mình vào Firebase, nếu không đăng nhập Google báo `DEVELOPER_ERROR`.
