# Việc của Thúy Ngân

Đọc [`../chung/cach-lam.md`](../chung/cach-lam.md) một lần trước, rồi quay lại đây.

| Issue | Việc | Bắt đầu được chưa |
| --- | --- | --- |
| [#25](https://github.com/tahpnart8/NHOM3-MobileApp/issues/25) | Giao diện A01 Đăng nhập | **Ngay bây giờ** |
| [#26](https://github.com/tahpnart8/NHOM3-MobileApp/issues/26) | Giao diện A02, A03 | Sau khi #25 merge |
| [#27](https://github.com/tahpnart8/NHOM3-MobileApp/issues/27) | Giao diện A04, A05, A06 | Sau khi #26 merge |
| [#20](https://github.com/tahpnart8/NHOM3-MobileApp/issues/20) | Bảng đặc tả chức năng, dòng 1 đến 32 | **Ngay bây giờ**, bạn là người đầu tiên của issue này |
| [#23](https://github.com/tahpnart8/NHOM3-MobileApp/issues/23) | Sơ đồ lớp, trang 0 đến 4 | Chờ #21 của Phúc merge |

Gợi ý thứ tự: làm #25 trước cho quen tay, rồi #20 (vì Tú và Long đang chờ bạn xong mới tới lượt họ), sau đó quay lại #26, #27.

---

## Issue #25, #26, #27: giao diện nhóm A

### Issue #25, màn A01 Đăng nhập

```bash
git fetch origin
git switch -c docs/25-giao-dien-nhom-a-dot-1 origin/main
BG=origin/docs/0-ban-giao-tam
git show $BG:workflow/docs/_ban-giao/ngan/giao-dien/a01-dang-nhap.png > workflow/docs/ui/a-tai-khoan/a01-dang-nhap.png
```

Mở `workflow/docs/ui/a-tai-khoan/README.md`, tìm mục **A01. Đăng nhập**, điền đủ 5 phần. Dưới đây là bản đã điền sẵn cho A01, bạn xem ảnh đối chiếu rồi chỉnh lại nếu thấy chỗ nào chưa khớp:

```markdown
## A01. Đăng nhập

**Ảnh:** ![A01 Đăng nhập](a01-dang-nhap.png)

**Mục đích:** Cho người dùng đã có tài khoản vào ứng dụng, bằng email hoặc số điện thoại, hoặc bằng tài khoản Google.

**Thành phần chính:**
- Logo và tên ứng dụng ĐồCũ
- Tiêu đề "Chào mừng trở lại" và một dòng giới thiệu ngắn
- Ô nhập "Email hoặc số điện thoại"
- Ô nhập "Mật khẩu", có thể ẩn hiện ký tự
- Liên kết "Quên mật khẩu?" nằm dưới ô mật khẩu, canh phải
- Nút "Đăng nhập" là nút chính
- Đường kẻ phân cách với chữ "hoặc"
- Nút "Đăng nhập bằng Google" có biểu tượng Google
- Dòng cuối trang: "Chưa có tài khoản? Đăng ký ngay"

**Hành động và điều hướng:**
- Bấm "Đăng nhập" khi đúng thông tin: sang B01 Trang chủ
- Bấm "Đăng nhập bằng Google": sang B01 Trang chủ sau khi chọn tài khoản Google
- Bấm "Quên mật khẩu?": sang A03 Quên mật khẩu
- Bấm "Đăng ký ngay": sang A02 Đăng ký

**Chức năng liên quan:** dòng 3, 4, 5 trong bảng đặc tả chức năng (đăng ký, đăng nhập kèm đăng nhập Google, quên mật khẩu).

**Trạng thái đặc biệt:**
- Sai email hoặc mật khẩu: hiện dòng báo lỗi màu đỏ ngay dưới ô nhập sai, không chuyển màn
- Tài khoản đang bị khóa: hiện thông báo nêu rõ lý do và thời hạn khóa
- Đang gửi yêu cầu: nút "Đăng nhập" chuyển sang trạng thái chờ, không bấm lại được
- Chưa xác thực email: nhắc người dùng mở hộp thư để xác thực trước khi vào
```

Xong thì:

```bash
git add workflow/docs/ui/a-tai-khoan/
git commit -m "docs(ui): thêm giao diện nhóm A đợt 1"
git push -u origin HEAD
```

Mở Pull Request, mẫu ngắn tài liệu, ghi `Closes #25`.

### Issue #26, màn A02 và A03

```bash
git fetch origin
git switch -c docs/26-giao-dien-nhom-a-dot-2 origin/main
BG=origin/docs/0-ban-giao-tam
git show $BG:workflow/docs/_ban-giao/ngan/giao-dien/a02-dang-ky.png > workflow/docs/ui/a-tai-khoan/a02-dang-ky.png
git show $BG:workflow/docs/_ban-giao/ngan/giao-dien/a03-quen-mat-khau.png > workflow/docs/ui/a-tai-khoan/a03-quen-mat-khau.png
```

Điền mục **A02. Đăng ký** và **A03. Quên mật khẩu** theo đúng khuôn của A01. Commit `docs(ui): thêm giao diện nhóm A đợt 2`, Pull Request ghi `Closes #26`.

### Issue #27, màn A04, A05, A06

```bash
git fetch origin
git switch -c docs/27-giao-dien-nhom-a-dot-3 origin/main
BG=origin/docs/0-ban-giao-tam
git show $BG:workflow/docs/_ban-giao/ngan/giao-dien/a04-quan-ly-thong-tin-ca-nhan.png > workflow/docs/ui/a-tai-khoan/a04-quan-ly-thong-tin-ca-nhan.png
git show $BG:workflow/docs/_ban-giao/ngan/giao-dien/a05-chinh-sua-ho-so.png > workflow/docs/ui/a-tai-khoan/a05-chinh-sua-ho-so.png
git show $BG:workflow/docs/_ban-giao/ngan/giao-dien/a06-quan-ly-dia-chi.png > workflow/docs/ui/a-tai-khoan/a06-quan-ly-dia-chi.png
```

Điền ba mục A04, A05, A06. Commit `docs(ui): thêm giao diện nhóm A đợt 3`, Pull Request ghi `Closes #27`.

---

## Issue #20: bảng đặc tả chức năng, dòng 1 đến 32

Bạn làm **phần đầu tiên**, nên bạn là người tạo ra khung của cả bảng. Tú (dòng 33-59) và Long (dòng 60-94) chờ bạn merge xong mới bắt đầu, nên làm sớm giúp cả nhóm đỡ nghẽn.

```bash
git fetch origin
git switch -c docs/20-dac-ta-chuc-nang-phan-1 origin/main
BG=origin/docs/0-ban-giao-tam
git show $BG:workflow/docs/_ban-giao/chung/dac-ta-chuc-nang-goc.html > dac-ta-goc.html
```

File `dac-ta-goc.html` vừa lấy về nằm ở thư mục gốc repo, **mở bằng trình duyệt** (nhấp đúp) để xem bảng cho dễ đọc. Đây chỉ là file tạm để bạn đọc, **xóa đi trước khi commit** (lệnh xóa có ở cuối mục này).

Việc cần làm: mở `workflow/docs/product/dac-ta-chuc-nang.md`, thay toàn bộ nội dung đang có bằng khung dưới đây, rồi chép dòng 1 đến 32 từ file HTML vào bảng.

```markdown
# Bảng đặc tả chức năng

Tổng hợp từ các tài liệu phân tích ở `workflow/docs/analysis/`. Chức năng ngoài phạm vi đợt này được ghi rõ ở cột Ghi chú.

| STT | User | Module | Tên chức năng | Chức năng con | Đặc tả | Ghi chú |
| --- | --- | --- | --- | --- | --- | --- |
| 1 | Người dùng | Quản lý tài khoản và hồ sơ | Đăng nhập | | Truy cập vào ứng dụng bằng tài khoản đã đăng ký | |
| 2 | Người dùng | Quản lý tài khoản và hồ sơ | Quên mật khẩu | | Xác thực người dùng và cho phép đặt lại mật khẩu | |
```

Hai dòng trên là **ví dụ về định dạng**, nội dung thật lấy từ file HTML. Lưu ý khi chép:

- Cột **User** của dòng 1 đến 32 đều là `Người dùng`.
- Trong HTML, ô Module và ô User bị gộp dọc cho nhiều dòng liền nhau. Sang Markdown thì **không gộp được**, nên phải ghi lặp lại tên Module ở mỗi dòng.
- Cột **Chức năng con** để trống nếu dòng đó không có cấp con.
- Cột **Đặc tả** chép nguyên phần mô tả trong HTML, nếu ô đó trống thì tự viết một câu ngắn cho đủ nghĩa.
- Dòng 1 đến 32 **không có dòng nào ngoài phạm vi**, nên cột Ghi chú để trống hết.
- Dừng đúng ở dòng 32, không làm lấn sang dòng 33 của Tú.

Xong thì xóa file tạm và commit:

```bash
rm dac-ta-goc.html
git add workflow/docs/product/dac-ta-chuc-nang.md
git commit -m "docs(docs): thêm bảng đặc tả chức năng dòng 1 đến 32"
git push -u origin HEAD
```

Pull Request ghi **`Refs #20`**, không phải `Closes`, vì issue chỉ đóng khi Long nộp nốt phần cuối.

---

## Issue #23: sơ đồ lớp, trang 0 đến 4

**Chưa làm được, chờ #21 của Phúc merge.** Khi tới lượt:

```bash
git fetch origin
git switch -c docs/23-so-do-lop-phan-1 origin/main
BG=origin/docs/0-ban-giao-tam
D=workflow/docs/diagrams/so-do-lop
git show $BG:workflow/docs/_ban-giao/ngan/so-do-lop/class-diagram.drawio > $D/class-diagram.drawio
git show $BG:workflow/docs/_ban-giao/ngan/so-do-lop/class-0-tong-quan.png > $D/class-0-tong-quan.png
git show $BG:workflow/docs/_ban-giao/ngan/so-do-lop/class-1-nguoi-dung.png > $D/class-1-nguoi-dung.png
git show $BG:workflow/docs/_ban-giao/ngan/so-do-lop/class-2-tin-dang-tim-kiem.png > $D/class-2-tin-dang-tim-kiem.png
git show $BG:workflow/docs/_ban-giao/ngan/so-do-lop/class-3-trao-doi-giao-dich.png > $D/class-3-trao-doi-giao-dich.png
git show $BG:workflow/docs/_ban-giao/ngan/so-do-lop/class-4-tin-cay-quan-tri.png > $D/class-4-tin-cay-quan-tri.png
```

Bạn nộp cả file `class-diagram.drawio` vì đó là một file chung cho cả 8 trang, Tú không cần nộp lại.

Phần README: mở `workflow/docs/diagrams/so-do-lop/README.md`, viết các mục sau, lấy nội dung từ [`so-do-lop/noi-dung-tham-khao.md`](so-do-lop/noi-dung-tham-khao.md) trong thư mục này:

- Mục kiến trúc MVVM kết hợp Repository (mục 2 của file tham khảo)
- Mô tả nhóm lớp **mô hình miền** và **kiểu liệt kê** (phần đầu mục 3)
- Mục thể hiện OOP (mục 4)
- Chèn 5 ảnh trang 0 đến 4 kèm tiêu đề từng trang

Bảng số lớp mỗi gói ở cuối README thì **để nguyên chưa điền**, Tú điền nốt khi nộp phần sau.

```bash
git add workflow/docs/diagrams/so-do-lop/
git commit -m "docs(docs): thêm sơ đồ lớp trang 0 đến 4"
git push -u origin HEAD
```

Pull Request ghi **`Refs #23`**, không phải `Closes`.
