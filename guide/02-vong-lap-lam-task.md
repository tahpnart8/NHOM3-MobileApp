# 02. Vòng lặp làm một task

Bốn bước: **Explore, Plan, Implement, Review**. Giữa các bước có một điểm bạn dừng lại và kiểm. Đó là chỗ bạn "cầm lái"; nếu bỏ hết các điểm dừng, AI chạy một mạch và bạn chỉ còn biết kết quả sau cùng.

```
Explore  ->  [bạn kiểm]  ->  Plan  ->  [bạn duyệt]  ->  Implement (từng bước, mỗi bước 1 commit)  ->  Review
   chỉ đọc                 chưa code                    build xanh mới sang bước sau              AI tự soi
```

Chuẩn bị: một phiên chat **mới** cho mỗi issue, đã dán prompt mở đầu ở [01-khoi-tao.md](01-khoi-tao.md) mục B. Thay `<N>` bằng số issue của bạn.

**Ghi nhớ ra file.** Bảo AI lưu kế hoạch vào `.agents/tmp/plan-<N>.md`. Thư mục này không bị commit. Nếu chat quá dài hoặc AI quên, mở phiên mới, đọc lại file đó và làm tiếp; bạn không phải giải thích lại từ đầu.

## Bước 0. Nhận task

```
/start-task <N>
```

Workflow này đọc issue, gán bạn làm người thực hiện, tạo branch `type/N-slug` từ `main` mới nhất, nêu phạm vi được sửa và đề xuất kế hoạch ngắn. Nếu công cụ của bạn không có lệnh gạch chéo, dán: "Làm theo skill `.agents/skills/pubg-start-task/SKILL.md` cho issue #<N>."

Nó cũng gộp phần Explore và Plan cho việc nhỏ. Với việc lớn, dùng hai prompt dưới đây để có kiểm soát tốt hơn.

## Bước 1. Explore (chỉ đọc)

```
Issue #<N>. CHƯA ĐƯỢC SỬA FILE NÀO. Hãy khám phá:
1. Chạy gh issue view <N> và đọc mọi tài liệu mà issue nhắc tới (docs/...).
2. Tìm trong PUBGApp/ những gì ĐÃ CÓ liên quan: lớp, layout, string, nav graph mà issue sẽ đụng hoặc có thể tái sử dụng. Dùng tìm kiếm thật (grep, glob), không đoán tên.
3. Trả lời bằng danh sách:
   a) file sẽ phải sửa
   b) file, lớp hoặc hàm có thể tái sử dụng
   c) code hiện có KHÔNG được xóa hay đổi tên
   d) chỗ nào issue mơ hồ, thiếu, hoặc mâu thuẫn với docs/
   e) API Firebase, AndroidX hoặc Room bạn định dùng, và đã tra bằng context7 hay chưa
Mỗi đường dẫn phải là file bạn đã mở thật. Chỗ chưa mở thì ghi "chưa kiểm tra".
```

**Bạn kiểm (2 phút):** mở thử 2 hoặc 3 đường dẫn AI nêu. Có tồn tại không, có đúng như nó mô tả không? Mục d) có nội dung nào không? Nếu AI nói "issue rõ ràng, không có gì mơ hồ" trong khi bạn thấy chỗ chưa rõ, hãy chỉ ra cho nó. Nếu AI nêu tên lớp không có thật, bảo nó tìm lại và nhắc rằng `AGENTS.md` R2 cấm bịa.

## Bước 2. Plan (chưa code)

```
Dựa trên phần khám phá, lập kế hoạch cho issue #<N>. CHƯA CODE. Kế hoạch gồm:
1. Các bước nhỏ theo thứ tự. Mỗi bước tối đa khoảng 100 dòng thay đổi và build được sau bước đó.
2. Với mỗi bước: file sẽ chạm, và lệnh kiểm tra bạn sẽ chạy.
3. Bảng đối chiếu: mỗi dòng tiêu chí nghiệm thu của issue thuộc về bước nào. Tiêu chí nào chưa có bước, nói rõ.
4. Điều bạn cần tôi quyết định, đặc biệt ba trường hợp hỏi trước ở AGENTS.md R6: đổi cấu trúc Firestore hoặc Room đã có, thêm hoặc đổi thư viện, xóa hoặc đổi tên tính năng.
5. Tài liệu phải sửa cùng PR (docs/data, docs/product/features.md).
Lưu kế hoạch vào .agents/tmp/plan-<N>.md rồi DỪNG, chờ tôi duyệt.
```

**Bạn duyệt (3 phút).** Kiểm ba điều:

- Có bước nào ra ngoài phạm vi của issue không? Nếu có, bỏ.
- Mọi tiêu chí nghiệm thu đều có bước?
- Bước nào lớn hơn bạn đọc nổi thì bảo AI tách.

Trả lời "duyệt" hoặc nêu chỗ sửa. Nếu AI đưa ra chọn lựa ở mục 4, quyết định rồi bảo nó ghi lại bằng `/record-decision` khi nó đáng nhớ.

## Bước 3. Implement (từng bước)

Lặp lại cho từng bước `<k>` của kế hoạch:

```
Duyệt. Làm BƯỚC <k> trong .agents/tmp/plan-<N>.md và chỉ bước đó.
Khi xong: chạy build (nói đúng lệnh bạn chạy, từ thư mục PUBGApp), cho tôi xem git diff --stat, rồi nêu ngắn gọn: đã sửa gì, và điều gì bạn CHƯA kiểm chứng. Nếu build xanh, commit với subject theo .github/NAMING.md. Sau đó dừng.
```

**Bạn kiểm sau mỗi bước (1 đến 2 phút):**

- Đọc `git diff` của bước đó, ít nhất lướt qua. Có file nào ngoài kế hoạch không?
- Có dòng bị xóa nhiều bất thường không? Tính năng cũ có thể biến mất ở đây.
- Chạy app nếu bước có giao diện. AI không chạy được emulator; chỉ bạn nhìn thấy màn hình.

**Dấu hiệu AI đang lạc, cần dừng lại:**

- Sửa lỗi build lần thứ ba mà vẫn lỗi, hoặc mỗi lần sửa lại đẻ ra lỗi mới.
- Nói "should work" hoặc "chắc là chạy" mà không có kết quả lệnh.
- Bắt đầu sửa file ngoài kế hoạch "cho tiện".

Khi đó dán:

```
Dừng lại, đừng sửa nữa. Nói cho tôi: 1) nguyên nhân gốc bạn nghĩ là gì và bằng chứng nào (log, dòng code), 2) bạn đã thử những gì, 3) bạn đề xuất hướng nào tiếp theo. Nếu bạn không chắc nguyên nhân, nói "không chắc" và đề xuất cách tìm ra nó (ví dụ thêm log, đọc tài liệu API).
```

Nếu AI đã sửa lan ra ngoài phạm vi:

```
Phần thay đổi ngoài phạm vi issue #<N> phải bỏ. Chạy git diff, liệt kê các file hoặc đoạn không thuộc kế hoạch, hoàn tác CHÍNH XÁC những phần đó (không đụng phần đúng), rồi build lại và cho tôi xem git diff --stat.
```

## Bước 4. Review (AI tự soi)

Khi mọi bước xong, trước khi mở PR:

```
Tự review toàn bộ thay đổi của issue #<N> như một reviewer khó tính. Chạy git diff origin/main...HEAD và kiểm từng mục, chỉ báo cáo, KHÔNG tự sửa:
1. Từng dòng tiêu chí nghiệm thu của issue: đạt, không đạt, hay chưa kiểm chứng (nói vì sao).
2. File ngoài phạm vi; file hoặc hàm bị xóa hoặc đổi tên (git diff --diff-filter=DR --name-status origin/main...HEAD).
3. Thư viện mới; thay đổi cấu trúc Firestore hoặc Room đã có.
4. Nhánh lỗi: mất mạng, chưa đăng nhập, dữ liệu rỗng, người dùng bấm hai lần.
5. Xoay màn hình có mất trạng thái không; chuỗi hard-code trong layout; việc nặng trên luồng chính.
6. Tài liệu cần sửa cùng PR chưa.
Liệt kê vấn đề theo mức blocking, major, minor. Tôi sẽ quyết định sửa gì.
```

Bạn quyết định sửa cái nào, rồi bảo AI sửa từng cái (mỗi cái một commit nhỏ). Mục 1 là quan trọng nhất: tiêu chí nào AI ghi "chưa kiểm chứng" là việc **bạn** phải tự chạy trên máy.

**Bạn tự chạy trên emulator** (AI không làm thay được), tối thiểu:

- Luồng chính của issue, từ đầu đến cuối.
- Xoay ngang rồi xoay dọc: chữ đã gõ, vị trí cuộn, hộp thoại còn nguyên.
- Bật chế độ máy bay: trang bạn đã xem còn hiển thị, trang chưa xem có thông báo rõ ràng.

Xong bước này thì sang [03-ket-thuc-task.md](03-ket-thuc-task.md).

## Rút gọn vòng lặp

Việc nhỏ (sửa chữ, chỉnh màu, sửa lỗi một dòng, chỉ đổi tài liệu): gộp Explore và Plan thành hai câu, bỏ bước 4 nếu diff dưới khoảng 30 dòng. Điều không đổi: đọc `git diff` trước khi commit, và build xanh trước khi mở PR.
