# Việc của Hải Long

Đọc [`../chung/cach-lam.md`](../chung/cach-lam.md) một lần trước, rồi quay lại đây.

| Issue | Việc | Bắt đầu được chưa |
| --- | --- | --- |
| [#31](https://github.com/tahpnart8/NHOM3-MobileApp/issues/31) | Giao diện C01 Danh sách tin nhắn | **Ngay bây giờ** |
| [#32](https://github.com/tahpnart8/NHOM3-MobileApp/issues/32) | Giao diện C02, C03 | Sau khi #31 merge |
| [#33](https://github.com/tahpnart8/NHOM3-MobileApp/issues/33) | Giao diện C04, C05, C06 | Sau khi #32 merge |
| [#20](https://github.com/tahpnart8/NHOM3-MobileApp/issues/20) | Bảng đặc tả chức năng, dòng 60 đến 94 | Chờ phần dòng 33-59 của Tú merge |

Bạn cũng đang là người duyệt Pull Request tài liệu cùng với Phát, nên ai nộp bài xong là xem giúp nhé.

---

## Issue #31, #32, #33: giao diện nhóm C

### Issue #31, màn C01 Danh sách tin nhắn

```bash
git fetch origin
git switch -c docs/31-giao-dien-nhom-c-dot-1 origin/main
BG=origin/docs/0-ban-giao-tam
git show $BG:workflow/docs/_ban-giao/long/giao-dien/c01-danh-sach-tin-nhan.png > workflow/docs/ui/c-ket-noi-giao-dich/c01-danh-sach-tin-nhan.png
```

Mở `workflow/docs/ui/c-ket-noi-giao-dich/README.md`, tìm mục **C01. Danh sách tin nhắn**, điền đủ 5 phần. Bản đã điền sẵn để làm khuôn, xem ảnh đối chiếu rồi chỉnh nếu thấy chưa khớp:

```markdown
## C01. Danh sách tin nhắn

**Ảnh:** ![C01 Danh sách tin nhắn](c01-danh-sach-tin-nhan.png)

**Mục đích:** Cho người dùng xem toàn bộ cuộc trò chuyện đang có với người mua hoặc người bán, và biết cuộc nào có tin chưa đọc.

**Thành phần chính:**
- Tiêu đề "Tin nhắn"
- Ô tìm kiếm với gợi ý "Tìm cuộc trò chuyện"
- Danh sách cuộc trò chuyện, mỗi dòng gồm: ảnh đại diện chữ cái đầu, tên người đối thoại, đoạn tin nhắn gần nhất, thời điểm gần nhất
- Chấm tròn màu cam ở cuối dòng khi cuộc trò chuyện đó có tin chưa đọc
- Dòng có đề nghị giá hiển thị nội dung dạng "Đề nghị của bạn: 3.500.000đ"
- Thanh điều hướng dưới cùng 5 mục, mục "Tin nhắn" đang được chọn

**Hành động và điều hướng:**
- Bấm một cuộc trò chuyện: sang C02 Trò chuyện và đề nghị giá
- Gõ vào ô tìm kiếm: lọc danh sách ngay tại màn này, không chuyển màn
- Bấm "Trang chủ" ở thanh dưới: sang B01 Trang chủ
- Bấm "Cá nhân" ở thanh dưới: sang D01 Hồ sơ cá nhân

**Chức năng liên quan:** dòng 37, 38, 39 (chat gửi chữ, ảnh, video), dòng 40, 41, 42 (offer một chạm) trong bảng đặc tả chức năng.

**Trạng thái đặc biệt:**
- Chưa có cuộc trò chuyện nào: hiện dòng chữ gợi ý người dùng vào xem tin đăng và nhắn cho người bán
- Tin chưa đọc: tên và nội dung in đậm, kèm chấm tròn màu cam
- Mất mạng: hiện lại danh sách đã tải trước đó từ bộ nhớ đệm, kèm dải báo đang xem ngoại tuyến
- Tìm không ra kết quả: hiện dòng báo không có cuộc trò chuyện nào khớp
```

```bash
git add workflow/docs/ui/c-ket-noi-giao-dich/
git commit -m "docs(ui): thêm giao diện nhóm C đợt 1"
git push -u origin HEAD
```

Pull Request ghi `Closes #31`.

### Issue #32, màn C02 và C03

```bash
git fetch origin
git switch -c docs/32-giao-dien-nhom-c-dot-2 origin/main
BG=origin/docs/0-ban-giao-tam
git show $BG:workflow/docs/_ban-giao/long/giao-dien/c02-tro-chuyen-de-nghi-gia.png > workflow/docs/ui/c-ket-noi-giao-dich/c02-tro-chuyen-de-nghi-gia.png
git show $BG:workflow/docs/_ban-giao/long/giao-dien/c03-chi-tiet-giao-dich.png > workflow/docs/ui/c-ket-noi-giao-dich/c03-chi-tiet-giao-dich.png
```

Điền mục **C02. Trò chuyện và đề nghị giá** và **C03. Chi tiết giao dịch**. Commit `docs(ui): thêm giao diện nhóm C đợt 2`, Pull Request ghi `Closes #32`.

### Issue #33, màn C04, C05, C06

```bash
git fetch origin
git switch -c docs/33-giao-dien-nhom-c-dot-3 origin/main
BG=origin/docs/0-ban-giao-tam
git show $BG:workflow/docs/_ban-giao/long/giao-dien/c04-lich-su-giao-dich.png > workflow/docs/ui/c-ket-noi-giao-dich/c04-lich-su-giao-dich.png
git show $BG:workflow/docs/_ban-giao/long/giao-dien/c05-viet-danh-gia.png > workflow/docs/ui/c-ket-noi-giao-dich/c05-viet-danh-gia.png
git show $BG:workflow/docs/_ban-giao/long/giao-dien/c06-thong-bao.png > workflow/docs/ui/c-ket-noi-giao-dich/c06-thong-bao.png
```

Điền ba mục C04, C05, C06. Commit `docs(ui): thêm giao diện nhóm C đợt 3`, Pull Request ghi `Closes #33`.

---

## Issue #20: bảng đặc tả chức năng, dòng 60 đến 94

**Chờ Tú merge phần dòng 33-59 trước.** Bạn là người cuối, nên Pull Request của bạn đóng luôn issue.

```bash
git fetch origin
git switch -c docs/20-dac-ta-chuc-nang-phan-3 origin/main
BG=origin/docs/0-ban-giao-tam
git show $BG:workflow/docs/_ban-giao/chung/dac-ta-chuc-nang-goc.html > dac-ta-goc.html
```

Mở `dac-ta-goc.html` bằng trình duyệt để đọc. File tạm, **xóa trước khi commit**.

Việc cần làm: thêm tiếp dòng 60 đến 94 vào cuối bảng, không sửa dòng của Ngân và Tú.

Lưu ý khi chép, phần của bạn có điểm khác hai người trước:

- Cột **User** của dòng 60 đến 94 đều là `Admin`.
- **14 dòng ngoài phạm vi** phải ghi rõ ở cột Ghi chú, không được bỏ sót. Danh sách chính xác:

| Dòng | Tên chức năng | Ghi ở cột Ghi chú |
| --- | --- | --- |
| 75 | Cảnh báo giao dịch bất thường | Ngoài phạm vi đợt này |
| 77 | Can thiệp giao dịch (đổi trạng thái thủ công) | Ngoài phạm vi đợt này |
| 78 | Phê duyệt giao dịch | Ngoài phạm vi đợt này |
| 84 đến 88 | Thống kê, báo cáo, biểu đồ, xuất báo cáo | Ngoài phạm vi đợt này |
| 89 | Cấu hình hệ thống | Ngoài phạm vi đợt này |
| 90 | Quản lý nội dung tĩnh | Ngoài phạm vi đợt này |
| 91 | Thông báo hệ thống | Ngoài phạm vi đợt này |
| 92 | Nhật ký, bảo mật quản trị | Ngoài phạm vi đợt này |
| 93 | Phân quyền admin | Ngoài phạm vi đợt này |
| 94 | Quản lý phiên đăng nhập admin | Ngoài phạm vi đợt này |

- **Dòng 76 (Giải ngân) thì khác**: vẫn làm, nhưng cách làm đã đổi. Cột Ghi chú ghi: `Đã đổi sang giải ngân tự động khi người mua xác nhận đã nhận hàng, hoặc khi hết thời hạn đếm ngược. Admin không duyệt tay.`

```bash
rm dac-ta-goc.html
git add workflow/docs/product/dac-ta-chuc-nang.md
git commit -m "docs(docs): thêm bảng đặc tả chức năng dòng 60 đến 94"
git push -u origin HEAD
```

Pull Request ghi **`Closes #20`**, vì đây là phần cuối cùng của issue.
