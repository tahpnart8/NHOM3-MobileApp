# 01. Khởi tạo

Hai việc: cài máy một lần, và mở đầu mỗi phiên AI cho đúng. Cả hai đều ngắn.

## A. Máy của bạn (một lần)

Làm theo mục 1 của [docs/workflow/huong-dan-lam-viec.md](../docs/workflow/huong-dan-lam-viec.md): đặt danh tính Git bằng email đã gắn vào tài khoản GitHub, clone, chạy `scripts\setup-dev.ps1`, mở `PUBGApp/` bằng Android Studio.

Sau đó dán prompt này vào AI để kiểm tra nhanh (không sửa gì):

```
Kiểm tra môi trường của tôi, KHÔNG sửa file nào. Chạy và báo kết quả thật từng lệnh:
1. git config core.hooksPath          (phải ra .githooks)
2. git var GIT_AUTHOR_IDENT           (phải là tên và email của tôi, không phải tên một công cụ AI)
3. sh scripts/check-contributors.sh identity
4. git branch --show-current và git status --short
Nếu lệnh nào lỗi hoặc kết quả không như mong đợi, nói rõ và chỉ tôi cách sửa. Đừng tự chạy git config user.*.
```

Nếu kết quả 1 trống: chạy `scripts\setup-dev.ps1` rồi thử lại. Nếu kết quả 2 là tên lạ: sửa bằng `git config --global user.name` và `user.email` **do bạn gõ**, không nhờ AI.

## B. Mở đầu phiên AI (mỗi lần mở chat mới)

Mục đích: AI đọc đúng tài liệu và nói lại hiểu gì, để bạn phát hiện sớm nếu nó hiểu sai. Tốn vài phút, tiết kiệm nhiều giờ.

```
Bạn đang làm việc trong repo PUBGApp (Android, Java 17, XML Views, Firebase). Trước khi làm gì, hãy đọc:
AGENTS.md, memory/people.md, memory/preferences.md, các mục cuối của memory/decisions.md, docs/product/features.md.
Chưa sửa file nào. Sau khi đọc, trả lời ngắn gọn:
1. Dự án là gì, và chức năng nào đã được duyệt để làm (trạng thái khác proposed)?
2. Ba luật bạn dễ vi phạm nhất khi làm việc ở đây là gì? Nêu số R.
3. Bạn không biết hoặc chưa chắc điều gì? Nếu không chắc, nói "không chắc", đừng đoán.
```

Đọc câu trả lời. Nếu AI nêu một luật không có trong `AGENTS.md`, hoặc nói tính năng `proposed` là "đã duyệt", nó đang bịa: bảo nó đọc lại file đó.

**Được rút gọn khi:** bạn chỉ hỏi một câu ngắn (ví dụ "hàm này làm gì") hoặc sửa một lỗi chính tả. Phiên nào có sửa code thì nên có bước này.

## C. Biến ý tưởng thành issue

Mọi việc code bắt đầu từ một issue. Dùng workflow có sẵn:

```
/new-issue Tôi muốn: <mô tả ý tưởng bằng lời của bạn>.
Hãy: tìm issue trùng (gh issue list --search), lấy mã tính năng F-xx từ docs/product/features.md (đừng tự bịa mã), hỏi tôi tối đa 3 câu nếu còn thiếu, rồi soạn nội dung theo form trong .github/ISSUE_TEMPLATE và CHO TÔI XEM TRƯỚC. Chỉ tạo issue khi tôi nói "tạo".
```

Bạn kiểm gì trong bản xem trước:

- **Tiêu chí nghiệm thu** là danh sách kiểm được trên máy thật, không phải câu chung chung như "chạy tốt".
- **Phạm vi**: nêu tên gói hoặc file cụ thể, và có mục "Ngoài phạm vi".
- **Kích thước**: một lát cắt của một tính năng. Nếu AI đề xuất làm cả một tính năng lớn trong một issue, bảo nó tách.
- Tính năng chưa có mã trong `features.md` hoặc còn `proposed`: đừng tạo issue mã nguồn, nhờ leader duyệt tính năng trước.

Không chắc issue nên lớn hay nhỏ, hoặc chưa rõ yêu cầu: hỏi leader trước khi tạo.
