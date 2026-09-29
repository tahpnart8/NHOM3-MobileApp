# Việc của Anh Tú

Đọc [`../chung/cach-lam.md`](../chung/cach-lam.md) một lần trước, rồi quay lại đây.

| Issue | Việc | Bắt đầu được chưa |
| --- | --- | --- |
| [#28](https://github.com/tahpnart8/NHOM3-MobileApp/issues/28) | Giao diện B01 Trang chủ | **Ngay bây giờ** |
| [#29](https://github.com/tahpnart8/NHOM3-MobileApp/issues/29) | Giao diện B02, B03 | Sau khi #28 merge |
| [#30](https://github.com/tahpnart8/NHOM3-MobileApp/issues/30) | Giao diện B04, B05, B06 | Sau khi #29 merge |
| [#20](https://github.com/tahpnart8/NHOM3-MobileApp/issues/20) | Bảng đặc tả chức năng, dòng 33 đến 59 | Chờ phần dòng 1-32 của Ngân merge |
| [#23](https://github.com/tahpnart8/NHOM3-MobileApp/issues/23) | Sơ đồ lớp, trang 5 đến 7 | Chờ phần trang 0-4 của Ngân merge |

Gợi ý thứ tự: làm #28 ngay hôm nay vì không phải chờ ai. #20 thì để ý khi Ngân merge xong là tới lượt bạn, Long đang chờ bạn.

---

## Issue #28, #29, #30: giao diện nhóm B

### Issue #28, màn B01 Trang chủ

```bash
git fetch origin
git switch -c docs/28-giao-dien-nhom-b-dot-1 origin/main
BG=origin/docs/0-ban-giao-tam
git show $BG:workflow/docs/_ban-giao/tu/giao-dien/b01-trang-chu.png > workflow/docs/ui/b-kham-pha-tin-dang/b01-trang-chu.png
```

Mở `workflow/docs/ui/b-kham-pha-tin-dang/README.md`, tìm mục **B01. Trang chủ**, điền đủ 5 phần. Bản đã điền sẵn để bạn làm khuôn, xem ảnh đối chiếu rồi chỉnh nếu thấy chưa khớp:

```markdown
## B01. Trang chủ

**Ảnh:** ![B01 Trang chủ](b01-trang-chu.png)

**Mục đích:** Màn hình chính sau khi đăng nhập, nơi người dùng lướt xem tin đăng mới và tìm nhanh món đồ mình cần.

**Thành phần chính:**
- Thanh trên cùng: tên ứng dụng ĐồCũ, chuông thông báo có chấm đỏ khi có thông báo chưa đọc, ảnh đại diện người dùng
- Ô tìm kiếm với gợi ý "Tìm iPhone, xe đạp, sách..."
- Hai thẻ chuyển kênh: "Kênh chung" và "Đang theo dõi"
- Hàng lọc nhanh theo danh mục: Tất cả, Điện thoại, Thời trang, Xe cộ
- Lưới tin đăng hai cột, mỗi thẻ có ảnh, tên món đồ, giá, tình trạng và khu vực
- Thanh điều hướng dưới cùng 5 mục: Trang chủ, Tìm kiếm, Đăng tin (nút tròn nổi ở giữa), Tin nhắn, Cá nhân

**Hành động và điều hướng:**
- Bấm ô tìm kiếm: sang B02 Tìm kiếm và bộ lọc
- Bấm một thẻ tin đăng: sang B03 Chi tiết tin đăng
- Bấm nút tròn "Đăng tin": sang B04 Đăng tin
- Bấm "Tin nhắn" ở thanh dưới: sang C01 Danh sách tin nhắn
- Bấm "Cá nhân" ở thanh dưới: sang D01 Hồ sơ cá nhân
- Bấm chuông thông báo: sang C06 Thông báo
- Chuyển thẻ "Đang theo dõi": vẫn ở màn này, lưới chỉ còn tin của người đang theo dõi

**Chức năng liên quan:** dòng 24, 25 (kênh chung, kênh đang theo dõi), dòng 17, 19 (danh mục), dòng 31, 32 (gợi ý sản phẩm, mục Dành cho bạn) trong bảng đặc tả chức năng.

**Trạng thái đặc biệt:**
- Chưa có tin nào khớp bộ lọc: hiện dòng chữ báo trống thay cho lưới
- Đang tải thêm khi cuộn xuống: hiện các ô xám mờ ở cuối lưới
- Mất mạng: hiện lại các tin đã xem gần đây lấy từ bộ nhớ đệm, kèm dải báo đang xem ngoại tuyến
- Thẻ "Đang theo dõi" khi chưa theo dõi ai: gợi ý người dùng tìm và theo dõi người bán
```

```bash
git add workflow/docs/ui/b-kham-pha-tin-dang/
git commit -m "docs(ui): thêm giao diện nhóm B đợt 1"
git push -u origin HEAD
```

Pull Request ghi `Closes #28`.

### Issue #29, màn B02 và B03

```bash
git fetch origin
git switch -c docs/29-giao-dien-nhom-b-dot-2 origin/main
BG=origin/docs/0-ban-giao-tam
git show $BG:workflow/docs/_ban-giao/tu/giao-dien/b02-tim-kiem-bo-loc.png > workflow/docs/ui/b-kham-pha-tin-dang/b02-tim-kiem-bo-loc.png
git show $BG:workflow/docs/_ban-giao/tu/giao-dien/b03-chi-tiet-tin-dang.png > workflow/docs/ui/b-kham-pha-tin-dang/b03-chi-tiet-tin-dang.png
```

Điền mục **B02. Tìm kiếm và bộ lọc** và **B03. Chi tiết tin đăng**. Commit `docs(ui): thêm giao diện nhóm B đợt 2`, Pull Request ghi `Closes #29`.

### Issue #30, màn B04, B05, B06

```bash
git fetch origin
git switch -c docs/30-giao-dien-nhom-b-dot-3 origin/main
BG=origin/docs/0-ban-giao-tam
git show $BG:workflow/docs/_ban-giao/tu/giao-dien/b04-dang-tin.png > workflow/docs/ui/b-kham-pha-tin-dang/b04-dang-tin.png
git show $BG:workflow/docs/_ban-giao/tu/giao-dien/b05-ke-hang-cua-toi.png > workflow/docs/ui/b-kham-pha-tin-dang/b05-ke-hang-cua-toi.png
git show $BG:workflow/docs/_ban-giao/tu/giao-dien/b06-tin-da-luu.png > workflow/docs/ui/b-kham-pha-tin-dang/b06-tin-da-luu.png
```

Điền ba mục B04, B05, B06. Commit `docs(ui): thêm giao diện nhóm B đợt 3`, Pull Request ghi `Closes #30`.

---

## Issue #20: bảng đặc tả chức năng, dòng 33 đến 59

**Chờ Ngân merge phần dòng 1-32 trước.** Kiểm tra bằng cách mở `workflow/docs/product/dac-ta-chuc-nang.md` trên GitHub, thấy đã có bảng với 32 dòng là tới lượt bạn.

```bash
git fetch origin
git switch -c docs/20-dac-ta-chuc-nang-phan-2 origin/main
BG=origin/docs/0-ban-giao-tam
git show $BG:workflow/docs/_ban-giao/chung/dac-ta-chuc-nang-goc.html > dac-ta-goc.html
```

Mở `dac-ta-goc.html` bằng trình duyệt để đọc. Đây là file tạm, **xóa trước khi commit**.

Việc cần làm: mở `workflow/docs/product/dac-ta-chuc-nang.md`, **thêm tiếp dòng 33 đến 59 vào cuối bảng Ngân đã tạo**. Không sửa dòng nào của Ngân, không tạo bảng mới.

Lưu ý khi chép:

- Cột **User** của dòng 33 đến 59 đều là `Người dùng`.
- Ghi lặp lại tên Module ở mỗi dòng, vì Markdown không gộp ô dọc được.
- Phạm vi của bạn gồm các module: kết nối (theo dõi, lưu tin), chat, offer một chạm, giao dịch và thanh toán, đánh giá, báo cáo, khiếu nại.
- Dòng 33 đến 59 **không có dòng nào ngoài phạm vi**, cột Ghi chú để trống hết.
- Dừng đúng ở dòng 59, không lấn sang dòng 60 của Long.

```bash
rm dac-ta-goc.html
git add workflow/docs/product/dac-ta-chuc-nang.md
git commit -m "docs(docs): thêm bảng đặc tả chức năng dòng 33 đến 59"
git push -u origin HEAD
```

Pull Request ghi **`Refs #20`**, không phải `Closes`.

---

## Issue #23: sơ đồ lớp, trang 5 đến 7

**Chờ Ngân merge phần trang 0-4 trước.** Khi tới lượt:

```bash
git fetch origin
git switch -c docs/23-so-do-lop-phan-2 origin/main
BG=origin/docs/0-ban-giao-tam
D=workflow/docs/diagrams/so-do-lop
git show $BG:workflow/docs/_ban-giao/tu/so-do-lop/class-5-tang-du-lieu.png > $D/class-5-tang-du-lieu.png
git show $BG:workflow/docs/_ban-giao/tu/so-do-lop/class-6-luu-tru-cuc-bo-room.png > $D/class-6-luu-tru-cuc-bo-room.png
git show $BG:workflow/docs/_ban-giao/tu/so-do-lop/class-7-viewmodel-va-tien-ich.png > $D/class-7-viewmodel-va-tien-ich.png
```

File `class-diagram.drawio` Ngân đã nộp rồi, bạn không cần nộp lại.

Phần README: viết tiếp vào `workflow/docs/diagrams/so-do-lop/README.md`, lấy nội dung từ [`so-do-lop/noi-dung-tham-khao.md`](so-do-lop/noi-dung-tham-khao.md) trong thư mục này:

- Mô tả nhóm lớp **truy cập dữ liệu** (trang 5), **lưu trữ cục bộ Room** (trang 6), **tiện ích** và **ViewModel** (trang 7), lấy ở phần sau mục 3
- Mục luồng hoạt động chính (mục 5)
- Bảng truy vết từ dòng chức năng tới lớp (mục 7)
- Mục lớp hạ tầng (mục 8) và độ lệch số lớp, giới hạn đã biết (mục 9)
- Chèn 3 ảnh trang 5, 6, 7 kèm tiêu đề từng trang
- **Điền nốt bảng số lớp mỗi gói** ở cuối README: model 20, model.enums 18, data 14, data.local 12, util 7, viewmodel 14, tổng 85

```bash
git add workflow/docs/diagrams/so-do-lop/
git commit -m "docs(docs): thêm sơ đồ lớp trang 5 đến 7"
git push -u origin HEAD
```

Pull Request ghi **`Closes #23`**, vì đây là phần cuối của issue.
