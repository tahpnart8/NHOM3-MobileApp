# 03. Kết thúc task

Từ lúc code xong đến lúc PR được merge: mở PR, nhờ một AI khác kiểm chéo, nhờ leader duyệt, xử lý góp ý.

```
Code xong  ->  /sync-branch  ->  /open-pr  ->  Cross-check (AI khác)  ->  Nhắn leader  ->  Sửa theo góp ý  ->  Leader merge
```

## 1. Trước khi mở PR

Bạn đã chạy app trên emulator (xem cuối [02-vong-lap-lam-task.md](02-vong-lap-lam-task.md))? Nếu chưa, làm trước; mục "How it was tested" của PR phải nói thật phần nào bạn đã chạy.

Nếu `main` đã có commit mới (leader vừa merge PR khác), đưa nó vào branch của bạn:

```
/sync-branch
```

Workflow này merge `main` (không rebase), giữ code của cả hai bên khi có xung đột, và build lại. Nếu hai bên mâu thuẫn thật, nó dừng và hỏi bạn: đừng để AI tự chọn một bên.

## 2. Mở PR

```
/open-pr
Bổ sung cho phần "How it was tested": tôi đã tự chạy trên <emulator/thiết bị, API mấy>: <những gì bạn đã thử, ví dụ luồng chính, xoay màn hình, chế độ máy bay>. Phần tôi chưa chạy: <...>. Đừng ghi bất cứ điều gì tôi chưa nói là đã chạy.
```

Workflow này merge `main`, build, kiểm phạm vi, kiểm file bị xóa, kiểm commit, rồi mở PR theo template và ghi `Closes #<N>`. Kiểm kết quả:

- Tiêu đề PR đúng là subject của commit chính, ví dụ `feat(auth): add google sign-in button`.
- Danh sách tiêu chí nghiệm thu: chỉ đánh dấu ô nào bạn thật sự đã kiểm.
- Đợi CI chạy (khoảng 2 đến 3 phút). Xanh thì sang bước tiếp; đỏ thì xem [04-tinh-huong.md](04-tinh-huong.md).

## 3. Cross-check bằng AI khác

Mục đích: một "cặp mắt" chưa từng thấy đoạn code này soi lại. Nó bắt được những gì AI đã viết ra code không thấy, vì AI hay tin chính nó.

**Cách làm:**

- Mở **một phiên chat mới** (không dùng lại phiên đã code).
- Nếu được, dùng **model khác**. Code bằng Gemini thì kiểm bằng Claude, và ngược lại.
- Cho nó đọc số PR và **chỉ cho phép đọc**, không cho sửa.

```
Bạn là reviewer độc lập, CHỈ ĐỌC, không sửa file, không commit, không đăng gì lên GitHub. Hãy đọc AGENTS.md rồi review PR #<N>:
1. gh pr view <N> và gh issue view <số issue được đóng>. Ghi lại các tiêu chí nghiệm thu.
2. gh pr diff <N>. Với mỗi file ngoài các gói mà issue cho phép, báo lại.
3. Kiểm: từng tiêu chí nghiệm thu đạt hay không (nói bằng chứng là dòng code nào); file hoặc hàm bị xóa hay đổi tên; thư viện mới; đổi cấu trúc Firestore hoặc Room; nhánh lỗi (mất mạng, chưa đăng nhập, dữ liệu rỗng); luồng chính có bị chặn ở đâu; xoay màn hình; việc nặng trên luồng chính; chuỗi hard-code; quyền hạn lấy từ dữ liệu client (AGENTS.md R12); tài liệu chưa cập nhật.
4. Xanh CI chưa chắc là đúng. Nếu có thể, checkout PR, merge origin/main vào một branch tạm và tự chạy build; báo đúng lệnh và kết quả. Nếu không chạy được, nói "chưa chạy".
Trả lời: khuyến nghị (ổn / cần sửa / chặn), rồi danh sách vấn đề theo mức blocking, major, minor kèm file và dòng. Đừng khen chung chung. Chỗ nào bạn không chắc, ghi "không chắc" thay vì đoán.
```

**Bạn xử lý kết quả:**

1. Đọc từng vấn đề. Cái nào bạn đồng ý là đúng thì sửa (trong phiên đã code, hoặc phiên mới, mỗi cái một commit nhỏ trên cùng branch).
2. Cái nào bạn thấy reviewer hiểu sai thì bỏ qua và nói lý do trong bình luận PR.
3. Chạy lại build sau khi sửa, đẩy lên (`git push`, không force).

**Tóm tắt lên PR.** Nhờ AI soạn, bạn đọc rồi tự đăng:

```
Soạn một bình luận ngắn cho PR #<N> tóm tắt kết quả cross-check: những vấn đề đã sửa, những vấn đề tôi quyết định không sửa và lý do, và những điều chưa kiểm chứng. Viết bằng ngôn ngữ trung tính, như chính tôi viết. KHÔNG nêu tên công cụ AI hay model nào, KHÔNG có dòng "reviewed by", KHÔNG @ ai. Chỉ đưa nội dung cho tôi xem, đừng đăng.
```

Bạn đăng bằng `gh pr comment <N> --body-file <file>` hoặc dán vào trang PR.

**Lưu ý quan trọng về danh sách contributor.** Không cài Copilot, Codex hay bất kỳ GitHub App, bot review nào vào repo, và không @ một AI trong PR: chúng xuất hiện như một contributor, trái với luật của nhóm (`AGENTS.md` R8). Cross-check luôn diễn ra **trong máy của bạn**, còn lên GitHub chỉ là lời của bạn.

**Được bỏ qua khi:** PR chỉ sửa tài liệu, chữ hoặc màu, vài dòng. Khi đó nhờ chính leader xem là đủ.

## 4. Nhờ leader duyệt

GitHub tự yêu cầu leader review nhờ `CODEOWNERS`. Bạn chỉ cần nhắn nhóm một tin để leader biết:

```
PR #<N> <tiêu đề>: <một câu nói làm được gì>. CI xanh. Đã cross-check. Đã tự chạy: <luồng chính, xoay, máy bay>. Cần leader xem kỹ: <chỗ bạn thấy rủi ro hoặc chưa chắc>.
```

Nêu **chỗ bạn thấy rủi ro** là phần có giá trị nhất: leader biết đọc ở đâu.

Không hối thúc bằng cách merge hộ: chỉ leader duyệt và merge.

## 5. Khi leader để lại góp ý

```
Leader để lại góp ý ở PR #<N>. Chạy gh pr view <N> --comments để đọc. Với mỗi góp ý: nói bạn hiểu ý là gì, bạn đồng ý hay không và vì sao, và sẽ sửa thế nào. CHƯA SỬA. Chờ tôi xác nhận.
```

Sau khi bạn xác nhận:

```
Sửa theo các góp ý đã xác nhận. Mỗi góp ý một commit nhỏ trên cùng branch (không force push, không rebase, không tạo PR mới). Build lại, đẩy lên, rồi tóm tắt từng góp ý đã xử lý thế nào để tôi trả lời leader.
```

Bạn không đồng ý một góp ý: trả lời leader bằng lý do kỹ thuật cụ thể, không im lặng bỏ qua.

## 6. Sau khi PR được merge

```
git switch main
git pull
git branch -d <branch của bạn>
```

Issue tự đóng nhờ `Closes #<N>`. Nhận issue tiếp theo bằng một phiên chat mới.

Nếu task để lại điều đáng nhớ (chọn thư viện, đổi hướng thiết kế), ghi lại bằng `/record-decision`.
