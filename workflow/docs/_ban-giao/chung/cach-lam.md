# Cách làm, đọc một lần trước khi bắt đầu

Mọi lệnh dưới đây chạy trong **Git Bash** (chuột phải trong thư mục repo, chọn "Open Git Bash here"), không phải CMD hay PowerShell.

## Chuẩn bị một lần duy nhất

Nếu chưa chạy bao giờ thì chạy, để bật các hook bảo vệ cho máy mình:

```bash
sh workflow/scripts/setup-dev.sh
```

Kiểm tra danh tính Git đã đúng chưa (email phải là email đã gắn với tài khoản GitHub của bạn, nếu không CI sẽ từ chối commit):

```bash
git config user.name
git config user.email
```

## Khuôn làm một issue

Bốn bước, lặp lại y hệt cho mọi issue. Phần `<...>` thì README riêng của bạn ghi sẵn giá trị cụ thể, cứ chép nguyên khối lệnh trong đó.

**Bước 1. Tạo nhánh làm việc, luôn từ `main` mới nhất.**

```bash
git fetch origin
git switch -c <ten-nhanh> origin/main
```

**Bước 2. Lấy file từ thư mục bàn giao về đúng chỗ.** README của bạn có sẵn khối lệnh này cho từng issue, chép nguyên rồi dán.

Lệnh dùng dạng `git show` nên file rơi thẳng vào đúng đường dẫn cần nộp, không phải kéo thả tay, và thư mục `_ban-giao` không bị lẫn vào commit của bạn.

**Bước 3. Viết phần nội dung.** Đây là phần việc thật, không có lệnh nào làm thay được: mở file README ở thư mục đích và điền các mục theo đúng mẫu. README của bạn có một ví dụ đã điền đầy đủ để làm khuôn.

**Bước 4. Commit và đẩy lên.**

```bash
git add <cac-duong-dan-that>
git commit -m "<cau-commit>"
git push -u origin HEAD
```

Rồi vào GitHub mở Pull Request. Chọn mẫu ngắn dành cho tài liệu bằng cách thêm `&template=tai-lieu.md` vào cuối địa chỉ trang mở Pull Request, hoặc mở bằng lệnh:

```bash
gh pr create --template tai-lieu.md
```

Mẫu ngắn chỉ có 4 mục. Bắt buộc nhất là dòng `Closes #<số issue>` (hoặc `Refs #<số issue>` khi việc chưa xong hẳn, README của bạn ghi rõ dùng cái nào).

## Những chỗ hay sai

| Hiện tượng | Nguyên nhân và cách sửa |
| --- | --- |
| Hook chặn lúc `git push` | Tên nhánh sai. Phải đúng dạng `<loại>/<số issue>-<vài-từ>`, ví dụ `docs/25-giao-dien-nhom-a-dot-1`. Chép đúng tên nhánh trong README của bạn. |
| CI `policy` đỏ, báo thiếu `Closes` | Thân Pull Request chưa có dòng `Closes #N`. Bấm Edit ở phần mô tả, thêm vào, CI tự chạy lại. |
| CI `policy` đỏ, báo phải viết tiếng Việt | Tiêu đề hoặc mô tả Pull Request đang viết tiếng Anh, hoặc quá ngắn. Viết lại bằng tiếng Việt có dấu. |
| CI báo tác giả commit là `NONE` | Email Git chưa gắn với tài khoản GitHub. Vào GitHub, Settings, Emails để thêm. |
| Commit bị chặn vì file quá lớn | Ảnh trên 5 MB. Xuất lại ảnh ở độ phân giải thấp hơn. |
| Bị conflict khi có người merge trước | Chạy `git fetch origin` rồi `git merge origin/main`, mở file bị báo, giữ cả phần của mình lẫn phần của người kia, không xóa phần ai cả. Không chắc thì nhắn Phát. |

## Ba điều không được làm

1. Không tự merge Pull Request của mình, kể cả khi CI đã xanh. Phát hoặc Long duyệt và merge.
2. Không sửa file nằm ngoài phạm vi issue của mình. Mỗi issue ghi rõ "File được phép sửa".
3. Không commit thư mục `workflow/docs/_ban-giao/` vào Pull Request của mình.
