# Hướng dẫn làm việc nhóm

Cả nhóm làm **một kiểu duy nhất**. Quy tắc đặt tên chi tiết ở [`.github/NAMING.md`](../../.github/NAMING.md).

## 1. Cài đặt lần đầu (mỗi người, mỗi máy)

1. Cài Git for Windows, Android Studio, JDK không cần cài riêng (Gradle tự tải JDK 25 lần sync đầu, cần có mạng).
2. Đặt danh tính Git bằng **tên thật và email đã thêm vào tài khoản GitHub của bạn**:
   ```
   git config --global user.name "Tên của bạn"
   git config --global user.email "email-đã-liên-kết-với-GitHub"
   ```
   Nếu email không gắn với tài khoản GitHub, CI sẽ từ chối commit của bạn.
3. Clone repo, rồi chạy **một lần**: `scripts\setup-dev.ps1` (PowerShell). Lệnh này bật các hook bảo vệ cho bản clone của bạn.
4. Mở thư mục `PUBGApp/` bằng Android Studio (không mở thư mục gốc), chờ Sync xong, chạy thử trên emulator.
5. Firebase: sẽ bổ sung khi leader tạo project (mỗi người gửi SHA-1 của `debug.keystore` trên máy mình).

## 2. Một task đi từ đầu đến cuối

```
Issue  ->  nhận task  ->  branch từ main mới nhất  ->  commit nhỏ
   ->  Pull Request (Closes #N)  ->  CI xanh  ->  leader review  ->  squash merge
```

1. **Issue.** Mọi việc bắt đầu từ một issue dùng form (`feature`, `bug`, `chore`). Nhờ AI tạo bằng lệnh `/new-issue` cho đúng mẫu. Không có issue thì không có code.
2. **Nhận task.** Gán chính bạn (assignee) vào issue. Issue đã có người nhận thì không làm lại.
3. **Bắt đầu.** Dùng `/start-task` với số issue. Nó tạo branch dạng `feat/12-google-sign-in` từ `main` mới nhất và nêu rõ được sửa gì, không được sửa gì.
4. **Code.** Chỉ làm đúng phạm vi issue. Không tự xóa hay đổi tên chức năng đã có. Không thêm thư viện.
5. **Mở Pull Request.** Dùng `/open-pr`. Nó chạy build, kiểm tra phạm vi, rồi mở PR theo mẫu.
6. **Review.** Chỉ leader duyệt và merge. Sửa theo góp ý bằng commit mới trên cùng branch.

## 3. Những điều bị từ chối tự động

| Bị từ chối | Vì sao |
| --- | --- |
| Push thẳng lên `main` | Mọi thay đổi phải qua Pull Request |
| Commit có dòng `Co-authored-by` hoặc "Generated with" | Chỉ 5 thành viên được xuất hiện trong danh sách contributor |
| Tên branch hoặc tiêu đề commit sai định dạng | Để lịch sử nhất quán |
| PR xóa hoặc đổi tên file code mà không có `Removal-Issue: #N` | Bảo vệ chức năng đã thiết kế |
| PR sửa file Gradle hoặc thư viện mà tiêu đề không phải `build(...)` hay `chore(...)` | Thư viện chỉ được thêm qua issue riêng |
| PR có tác giả commit không thuộc 5 thành viên | Chỉ 5 thành viên được đóng góp |
| Xóa hoặc sửa dòng cũ trong `memory/decisions.md` | Sổ quyết định chỉ được ghi thêm |
| Commit `local.properties`, keystore, khóa bí mật | Bí mật và cấu hình riêng từng máy |

## 4. Dùng AI đúng cách (Antigravity, Claude Code, Codex)

- AI đọc `AGENTS.md` ở gốc repo. Đừng dán luật riêng của bạn vào prompt; nếu luật thiếu, nhờ leader sửa `AGENTS.md`.
- Lệnh có sẵn: `/new-issue`, `/start-task`, `/sync-branch`, `/open-pr`, `/record-decision`, `/doc-check`. `/review-pr` dành cho leader.
- Hãy yêu cầu AI **báo đúng lệnh đã chạy và kết quả thật**. Nếu nó nói "đã test trên emulator", hỏi lại xem nó đã chạy thật chưa.
- Tra API Firebase và AndroidX bằng MCP `context7` (đã cấu hình trong `.mcp.json` cho Claude Code; với Antigravity thêm server này trong phần MCP của ứng dụng: URL `https://mcp.context7.com/mcp`).

## 5. Khi gặp xung đột

- Dùng lệnh `/sync-branch` để đưa `main` mới nhất vào branch của bạn. Nó merge (không rebase), giữ code của cả hai bên, rồi build lại.
- Không giải quyết xung đột bằng cách xóa code của người khác hay chọn nguyên cả file của một bên. Nếu hai bên mâu thuẫn thật, hỏi leader.
- Nút **Update branch** trên trang PR cũng được, nhưng chỉ khi GitHub báo không có xung đột.
- Leader có thể đề xuất sửa trực tiếp trong review ("suggestion"). Bấm **Commit suggestion** là hợp lệ: dòng `Co-authored-by` mà GitHub tự thêm vào khi đó chỉ ghi tên người trong nhóm, và CI chấp nhận.
- Các file dễ xung đột và luật riêng nằm ở [conventions/android-java.md](../conventions/android-java.md).
