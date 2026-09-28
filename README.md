<div align="center">

# PUBGApp

### Pre-owned Users' Bargain Grounds

Chợ đồ cũ kết hợp mạng xã hội cho Android — đăng bán, trao đổi, cho tặng, trò chuyện và giao dịch trong cùng một ứng dụng.

[![Build](https://github.com/tahpnart8/NHOM3-MobileApp/actions/workflows/build.yml/badge.svg)](https://github.com/tahpnart8/NHOM3-MobileApp/actions/workflows/build.yml)
![Java](https://img.shields.io/badge/Java-17-orange?logo=openjdk&logoColor=white)
![Platform](https://img.shields.io/badge/Platform-Android-3DDC84?logo=android&logoColor=white)
![minSdk](https://img.shields.io/badge/minSdk-30-3DDC84?logo=android&logoColor=white)
![Status](https://img.shields.io/badge/Tr%E1%BA%A1ng%20th%C3%A1i-%C4%90ang%20ph%C3%A1t%20tri%E1%BB%83n-yellow)
![License](https://img.shields.io/badge/Gi%E1%BA%A5y%20ph%C3%A9p-%C4%90%E1%BB%93%20%C3%A1n%20m%C3%B4n%20h%E1%BB%8Dc-lightgrey)

</div>

---

## Giới thiệu

**PUBGApp** là ứng dụng Android cho việc mua bán, trao đổi và cho tặng đồ cũ, nơi người dùng vừa kết nối và tương tác với nhau như trên một mạng xã hội, vừa đăng tin, thương lượng và hoàn tất giao dịch như trên một sàn thương mại điện tử thu nhỏ. Đây là đồ án môn **Phát triển ứng dụng Mobile**, thực hiện bởi nhóm 3.

Phần mạng xã hội lo việc theo dõi, trò chuyện và đánh giá lẫn nhau; phần thương mại điện tử lo việc đăng tin, thương lượng giá, xử lý giao dịch và giao hàng. Cả hai gói gọn trong một trải nghiệm nhẹ nhàng, dễ dùng trên điện thoại.

## Tính năng chính

- **Tài khoản & hồ sơ** — đăng ký, đăng nhập, chỉnh sửa thông tin cá nhân, ảnh đại diện, địa chỉ.
- **Đăng bán & quản lý tin** — tạo tin đăng với hình ảnh, tình trạng món đồ, hình thức giao dịch (bán / trao đổi / cho tặng, kết hợp được).
- **Tìm kiếm & đề xuất** — lọc theo từ khóa, danh mục, khoảng giá, tình trạng, khu vực; gợi ý sản phẩm theo sở thích.
- **Kết nối** — theo dõi người dùng, lưu tin yêu thích, trò chuyện trực tiếp, gửi đề nghị giá (offer một chạm).
- **Giao dịch & thanh toán** — xác nhận giao dịch, thanh toán qua ký quỹ mô phỏng, tự động giải ngân cho người bán sau khi giao hàng thành công.
- **Tin cậy & cộng đồng** — đánh giá đối tác giao dịch, báo cáo vi phạm, khiếu nại và tranh chấp.
- **Quản trị** — kiểm duyệt nội dung, quản lý người dùng và danh mục, giám sát giao dịch ở mức cơ bản.

## Công nghệ

| Thành phần | Lựa chọn |
| --- | --- |
| Ngôn ngữ | Java 17 |
| Giao diện | XML Views + ViewBinding (không Kotlin, không Compose) |
| Tương thích | minSdk 30 · compileSdk / targetSdk 37 |
| Build | Gradle Kotlin DSL |
| Backend (dự kiến) | Firebase Authentication, Cloud Firestore, Cloud Storage |
| Offline (dự kiến) | Room (SQLite) |

## Chạy thử

1. Cài Android Studio, mở thư mục `PUBGApp/`.
2. Chờ Gradle sync (lần đầu tự tải JDK 25).
3. Chạy trên emulator hoặc thiết bị thật, API 30 trở lên.

Hoặc dùng dòng lệnh:

```bash
cd PUBGApp
./gradlew assembleDebug testDebugUnitTest lintDebug   # Windows: gradlew.bat
```

## Nhóm thực hiện

| Thành viên | GitHub | Vai trò |
| --- | --- | --- |
| Trần Đức Phát | [@tahpnart8](https://github.com/tahpnart8) | Nhóm trưởng |
| Nguyễn Lê Hải Long | [@Contest451](https://github.com/Contest451) | Kỹ sư phần mềm |
| Nguyễn Thúy Ngân | [@loopy-tnw](https://github.com/loopy-tnw) | Kỹ sư phần mềm |
| Trần Anh Tú | [@tranannhtu21012006](https://github.com/tranannhtu21012006) | Kỹ sư phần mềm |
| Nguyễn Hoàng Phúc | [@PmSubin](https://github.com/PmSubin) | Kỹ sư phần mềm |

<div align="center">

Đồ án môn học — không phải sản phẩm thương mại.

</div>
