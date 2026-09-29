# Việc của Hoàng Phúc

Đọc [`../chung/cach-lam.md`](../chung/cach-lam.md) một lần trước, rồi quay lại đây.

| Issue | Việc | Bắt đầu được chưa |
| --- | --- | --- |
| [#34](https://github.com/tahpnart8/NHOM3-MobileApp/issues/34) | Giao diện D01 Hồ sơ cá nhân | **Ngay bây giờ** |
| [#35](https://github.com/tahpnart8/NHOM3-MobileApp/issues/35) | Giao diện D02, D03 | Sau khi #34 merge |
| [#36](https://github.com/tahpnart8/NHOM3-MobileApp/issues/36) | Giao diện D04, D05, D06 | Sau khi #35 merge |
| [#21](https://github.com/tahpnart8/NHOM3-MobileApp/issues/21) | Danh sách lớp dự kiến | Chờ #20 merge xong cả 3 phần |

Issue #24 (ERD) đã xong và đã đóng, bạn không phải làm gì thêm ở đó nữa.

Ngân và Tú đang chờ #21 của bạn để làm sơ đồ lớp, nên khi #20 vừa merge xong thì làm #21 sớm giúp nhé.

---

## Issue #34, #35, #36: giao diện nhóm D

### Issue #34, màn D01 Hồ sơ cá nhân

```bash
git fetch origin
git switch -c docs/34-giao-dien-nhom-d-dot-1 origin/main
BG=origin/docs/0-ban-giao-tam
git show $BG:workflow/docs/_ban-giao/phuc/giao-dien/d01-ho-so-ca-nhan.png > workflow/docs/ui/d-ho-so-tin-cay/d01-ho-so-ca-nhan.png
```

Mở `workflow/docs/ui/d-ho-so-tin-cay/README.md`, tìm mục **D01. Hồ sơ cá nhân**, điền đủ 5 phần. Bản đã điền sẵn để làm khuôn, xem ảnh đối chiếu rồi chỉnh nếu thấy chưa khớp:

```markdown
## D01. Hồ sơ cá nhân

**Ảnh:** ![D01 Hồ sơ cá nhân](d01-ho-so-ca-nhan.png)

**Mục đích:** Trang cá nhân của chính người đang đăng nhập, là lối vào mọi mục quản lý liên quan tới bản thân: hàng đã bán, tin đã lưu, lịch sử giao dịch, uy tín, khiếu nại và cài đặt.

**Thành phần chính:**
- Đầu trang: ảnh đại diện chữ cái đầu, tên người dùng, khu vực (ví dụ Quận 1, TP.HCM), nút cài đặt nhanh ở góc phải
- Hàng ba số liệu: số món đã bán, điểm đánh giá trung bình, số người theo dõi
- Danh sách lối vào, mỗi dòng có biểu tượng và mũi tên sang phải: Sản phẩm của tôi, Tin đã lưu, Lịch sử giao dịch, Hồ sơ uy tín, Báo cáo và khiếu nại của tôi, Cài đặt tài khoản
- Mục "Đăng xuất" màu đỏ, tách riêng ở cuối danh sách
- Thanh điều hướng dưới cùng 5 mục, mục "Cá nhân" đang được chọn

**Hành động và điều hướng:**
- Bấm "Sản phẩm của tôi": sang B05 Kệ hàng của tôi
- Bấm "Tin đã lưu": sang B06 Tin đã lưu
- Bấm "Lịch sử giao dịch": sang C04 Lịch sử giao dịch
- Bấm "Hồ sơ uy tín": sang D03 Hồ sơ uy tín người bán
- Bấm "Báo cáo và khiếu nại của tôi": sang D05 Tạo khiếu nại
- Bấm "Cài đặt tài khoản": sang A04 Quản lý thông tin cá nhân
- Bấm ảnh đại diện hoặc tên: sang D02 Trang cá nhân
- Bấm "Đăng xuất": hỏi xác nhận, đồng ý thì về A01 Đăng nhập
- Bấm "Trang chủ" ở thanh dưới: sang B01 Trang chủ

**Chức năng liên quan:** dòng 6, 7, 8 (chỉnh sửa thông tin, ảnh, địa chỉ), dòng 9 (xem đánh giá cá nhân), dòng 10 (quản lý kệ hàng), dòng 35 (xem danh sách đã lưu), dòng 50 (lịch sử giao dịch), dòng 56, 58 (lịch sử báo cáo, theo dõi khiếu nại) trong bảng đặc tả chức năng.

**Trạng thái đặc biệt:**
- Người dùng chưa bán món nào: ba số liệu hiện 0, mục "Sản phẩm của tôi" gợi ý đăng tin đầu tiên
- Chưa có đánh giá nào: ô điểm đánh giá hiện dấu gạch thay cho số
- Tài khoản chưa xác minh người bán: hiện thêm một dải nhắc gửi yêu cầu xác minh
- Mất mạng: vẫn hiện thông tin đã tải trước đó từ bộ nhớ đệm, kèm dải báo đang xem ngoại tuyến
```

```bash
git add workflow/docs/ui/d-ho-so-tin-cay/
git commit -m "docs(ui): thêm giao diện nhóm D đợt 1"
git push -u origin HEAD
```

Pull Request ghi `Closes #34`.

### Issue #35, màn D02 và D03

```bash
git fetch origin
git switch -c docs/35-giao-dien-nhom-d-dot-2 origin/main
BG=origin/docs/0-ban-giao-tam
git show $BG:workflow/docs/_ban-giao/phuc/giao-dien/d02-trang-ca-nhan.png > workflow/docs/ui/d-ho-so-tin-cay/d02-trang-ca-nhan.png
git show $BG:workflow/docs/_ban-giao/phuc/giao-dien/d03-ho-so-uy-tin-nguoi-ban.png > workflow/docs/ui/d-ho-so-tin-cay/d03-ho-so-uy-tin-nguoi-ban.png
```

Điền mục **D02. Trang cá nhân** và **D03. Hồ sơ uy tín người bán**. Commit `docs(ui): thêm giao diện nhóm D đợt 2`, Pull Request ghi `Closes #35`.

### Issue #36, màn D04, D05, D06

```bash
git fetch origin
git switch -c docs/36-giao-dien-nhom-d-dot-3 origin/main
BG=origin/docs/0-ban-giao-tam
git show $BG:workflow/docs/_ban-giao/phuc/giao-dien/d04-bao-cao-tin-dang.png > workflow/docs/ui/d-ho-so-tin-cay/d04-bao-cao-tin-dang.png
git show $BG:workflow/docs/_ban-giao/phuc/giao-dien/d05-tao-khieu-nai.png > workflow/docs/ui/d-ho-so-tin-cay/d05-tao-khieu-nai.png
git show $BG:workflow/docs/_ban-giao/phuc/giao-dien/d06-admin-xu-ly-khieu-nai.png > workflow/docs/ui/d-ho-so-tin-cay/d06-admin-xu-ly-khieu-nai.png
```

Điền ba mục D04, D05, D06. Commit `docs(ui): thêm giao diện nhóm D đợt 3`, Pull Request ghi `Closes #36`.

---

## Issue #21: danh sách lớp dự kiến

**Chờ #20 merge xong cả ba phần** (Ngân, Tú, Long). Kiểm tra bằng cách mở `workflow/docs/product/dac-ta-chuc-nang.md` trên GitHub, thấy đủ 94 dòng là tới lượt bạn.

```bash
git fetch origin
git switch -c docs/21-du-kien-lop origin/main
BG=origin/docs/0-ban-giao-tam
git show $BG:workflow/docs/_ban-giao/phuc/du-kien-lop/danh-sach-lop.md > workflow/docs/analysis/du-kien-lop.md
```

File vừa lấy về đã có sẵn **85 lớp chia theo 6 gói**, mỗi lớp kèm một câu trách nhiệm. Việc của bạn là hoàn thiện nó cho đủ tiêu chí của issue:

1. **Sửa tiêu đề dòng đầu** cho đúng là tài liệu chính thức, bỏ chữ "tham khảo cho ..." đi.
2. **Thêm một cột** vào mỗi bảng: `Dòng chức năng liên quan`, ghi số STT trong bảng đặc tả chức năng mà lớp đó phục vụ. Ví dụ:

```markdown
| Lớp | Trách nhiệm | Dòng chức năng liên quan |
| --- | --- | --- |
| User | Người dùng: hồ sơ công khai, trạng thái tài khoản, điểm đánh giá. | 3, 4, 5, 6, 9 |
| Listing | Tin đăng bán, trao đổi hoặc cho tặng; nhiều hình thức cùng lúc, giới hạn mức offer. | 10, 12, 13, 14, 15, 16, 17 |
```

Cách tra nhanh: mở `workflow/docs/product/dac-ta-chuc-nang.md` (lúc này đã có đủ 94 dòng), đọc từng dòng chức năng rồi hỏi "dòng này cần lớp nào", ghi ngược số dòng vào lớp tương ứng. Lớp hạ tầng không phục vụ riêng dòng nào (ví dụ `BaseDocument`, `Converters`, `NetworkMonitor`) thì ghi `hạ tầng dùng chung`.

3. Mở `workflow/docs/analysis/README.md`, điền `@PmSubin` vào cột "Người phụ trách" ở dòng `du-kien-lop.md`.

```bash
git add workflow/docs/analysis/du-kien-lop.md workflow/docs/analysis/README.md
git commit -m "docs(docs): thêm danh sách lớp dự kiến"
git push -u origin HEAD
```

Pull Request ghi `Closes #21`.
