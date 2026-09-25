# 04. Tình huống thường gặp

Tra theo dấu hiệu bạn nhìn thấy. Mỗi mục nói nguyên nhân thường gặp và cách xử lý; nếu bạn dùng AI để sửa, có prompt kèm theo. Không thấy tình huống của mình: gõ `/guide` và mô tả, hoặc hỏi leader.

Mục lục: [AI bịa API](#ai-bịa-api-hoặc-lớp) · [AI xóa code cũ](#ai-xóa-hoặc-đổi-tên-code-cũ) · [Xung đột merge](#xung-đột-merge) · [Hook từ chối commit](#hook-từ-chối-commit-hoặc-push) · [CI policy đỏ](#ci-policy-đỏ) · [CI build đỏ](#ci-build-đỏ-nhưng-máy-bạn-xanh) · [Co-authored-by](#ai-thêm-co-authored-by-hoặc-generated-with) · [Task quá lớn](#task-quá-lớn-hoặc-ai-làm-lan-man) · [Issue mơ hồ](#issue-mơ-hồ) · [AI lặp vòng lỗi](#ai-lặp-vòng-sửa-lỗi-không-dứt) · [Cần thư viện mới](#cần-thêm-thư-viện) · [Đổi cấu trúc dữ liệu](#cần-đổi-cấu-trúc-firestore-hoặc-room-đã-có) · [AI quên luật](#ai-quên-luật-giữa-chừng) · [Lỡ commit lên main](#lỡ-commit-trên-main-hoặc-đặt-sai-tên-branch) · [Google sign-in lỗi](#đăng-nhập-google-báo-developer_error) · [Xoay màn hình, offline](#xoay-màn-hình-mất-trạng-thái-hoặc-offline-không-chạy)

## AI bịa API hoặc lớp

**Dấu hiệu:** lỗi build `cannot find symbol`, hoặc hàm Firebase, AndroidX, Room mà bạn chưa từng thấy trong tài liệu.

**Nguyên nhân:** model nhớ sai hoặc trộn phiên bản cũ. Firebase và Android đổi API thường xuyên (ví dụ đăng nhập Google nay dùng Credential Manager, không dùng `GoogleSignInClient` cũ).

```
Chỗ này build lỗi vì <tên lớp hoặc hàm> có thể không tồn tại. Đừng đoán tiếp. Dùng context7 (hoặc tài liệu chính thức của Firebase, AndroidX, Room) để tra chữ ký thật của <API>, cho tôi xem nguồn bạn tra, rồi mới sửa code. Nếu không tra được, nói "chưa xác minh".
```

Trong Claude Code, context7 đã cấu hình sẵn ở `.mcp.json`. Ở Antigravity, thêm server MCP `https://mcp.context7.com/mcp` trong phần cài đặt MCP.

## AI xóa hoặc đổi tên code cũ

**Dấu hiệu:** `git diff --stat` có nhiều dòng `-`, file lạ biến mất, hoặc CI báo `deleted file` hay `renamed file`.

**Cách xử lý:** phục hồi phần bị xóa, chỉ giữ phần thuộc issue.

```
Phần bạn vừa xóa hoặc đổi tên không thuộc issue #<N>. Chạy git diff --diff-filter=DR --name-status origin/main...HEAD và git diff --stat origin/main...HEAD, liệt kê mọi file hoặc đoạn bị xóa hay đổi tên. Phục hồi chúng từ origin/main (git checkout origin/main -- <file>), giữ nguyên phần thay đổi đúng của issue, build lại và cho tôi xem git diff --stat.
```

Việc xóa hoặc đổi tên tính năng đã có phải qua issue `type:removal` do leader mở; xem [05-khi-nao-duoc-pha-le.md](05-khi-nao-duoc-pha-le.md).

## Xung đột merge

Dùng `/sync-branch`. Đừng để AI chọn "toàn bộ bản của tôi" hoặc "toàn bộ bản của main" cho cả file: đó là cách tính năng của người khác biến mất. Nếu AI đề xuất vậy, từ chối:

```
Không chọn cả một bên. Với từng khối xung đột hãy giải thích bên nào đổi gì, rồi giữ cả hai thay đổi. Chỗ nào hai bên mâu thuẫn thật thì dừng và cho tôi xem cả hai phiên bản.
```

## Hook từ chối commit hoặc push

Hook chạy trên máy bạn và in lý do. Các lời từ chối thường gặp:

| Thông báo | Ý nghĩa và cách sửa |
| --- | --- |
| `bad subject: ...` | Subject không đúng `type(scope): mô tả`. Type thuộc `feat fix docs refactor test chore build ci`, phần mô tả không bắt đầu bằng chữ hoa và không có dấu chấm cuối, tối đa 100 byte (khoảng 55 chữ tiếng Việt). Phần mô tả viết tiếng Việt hoặc tiếng Anh đều được. Sửa rồi commit lại. |
| `the message carries a Co-authored-by trailer or an AI byline` | Có dòng đồng tác giả hoặc chữ "Generated with ..." trong message. Xóa dòng đó. |
| `the git identity in use is not allowed` | Tên hoặc email Git trông giống công cụ AI. Bạn tự đặt lại bằng `git config --global user.name` và `user.email`. |
| `pre-commit: refused. These files hold secrets or machine specific settings` | Bạn đang commit `local.properties`, khóa `.jks` hoặc `.env`. Bỏ ra khỏi commit: `git restore --staged <file>`. |
| `Larger than 5 MB` | File quá lớn. Hỏi leader trước khi thêm. |
| `A merge conflict marker is staged` | Còn dấu `<<<<<<<` trong file. Giải quyết xung đột xong mới commit. |
| `memory/decisions.md is append-only` | Bạn đang xóa hoặc sửa dòng cũ trong sổ quyết định. Hoàn tác và chỉ thêm mục mới ở cuối (`/record-decision`). |
| `pre-push: refused. Nobody pushes to main` | Bạn đang đứng ở `main`. Xem mục "Lỡ commit trên main". |
| `pre-push: refused. Branch '...' must look like type/issue-slug` | Tên branch sai. Đổi tên: `git branch -m feat/<N>-<slug>`. |

Không dùng `--no-verify` để lách: CI vẫn kiểm lại đúng những điều này trên PR.

## CI policy đỏ

Job `policy` đọc PR và từng commit qua API. Đọc dòng lỗi màu đỏ trong tab Checks:

| Thông báo | Cách sửa |
| --- | --- |
| `author login '...' is not on the team list ... login shows as NONE` | Email trong commit chưa gắn với tài khoản GitHub của bạn. Thêm email đó vào GitHub, Settings, Emails, hoặc đặt `user.email` là địa chỉ noreply của bạn (`<id>+<tên>@users.noreply.github.com`). Commit cũ giữ nguyên email; xem cách xử lý ở mục "Lỡ commit". |
| `branch '...' must look like type/issue-slug` | Tên branch sai. Tạo branch mới đúng tên từ commit hiện tại và mở PR mới; đóng PR cũ. |
| `pull request title '...' must be a conventional commit subject` | Sửa tiêu đề PR trên GitHub (nút Edit). Check tự chạy lại. |
| `tiêu đề PR phải viết bằng tiếng Việt có dấu` | Tiêu đề PR không có chữ tiếng Việt nào. Viết lại phần sau `type(scope):` bằng tiếng Việt có dấu. Máy đếm chữ cái có dấu, nên gõ không dấu cũng bị từ chối. |
| `mô tả PR phải viết bằng tiếng Việt có dấu` | Phần mô tả PR (ngoài comment `<!-- -->`) viết bằng tiếng Anh. Viết lại bằng tiếng Việt; tên lớp, đường dẫn và lệnh giữ nguyên. Nhờ AI: "Viết lại mô tả PR bằng tiếng Việt có dấu, giữ nguyên từ khóa Closes và tên code". |
| `the pull request body must say 'Closes #N'` | Sửa mô tả PR, thêm dòng `Closes #<N>` (hoặc `Closes: none`). Chữ nằm trong comment `<!-- -->` của template không tính. |
| `add a line 'Removal-Issue: #N'` (file bị xóa hoặc đổi tên) | Bạn xóa hoặc đổi tên code trong `PUBGApp/app/src/main`. Nếu là chủ ý, cần issue `type:removal` do leader mở, rồi thêm dòng đó vào PR. Nếu không phải chủ ý, phục hồi file. |
| `... changes the build or its dependencies` | Bạn sửa file Gradle trong PR không đặt tên `build(...)` hoặc `chore(...)`. Đổi tiêu đề nếu đây thật sự là việc build, nếu không thì bỏ thay đổi Gradle. |
| `memory/decisions.md is append-only` | Xem bảng hook ở trên. |
| `carries a Co-authored-by trailer or an AI byline` | Xem mục kế tiếp. |

## CI build đỏ nhưng máy bạn xanh

Chạy đúng lệnh của CI, từ thư mục `PUBGApp`: `gradlew.bat assembleDebug testDebugUnitTest lintDebug`. Hay gặp: `lintDebug` báo lỗi mà bạn chưa chạy; `main` có commit mới làm code của bạn không còn build (chạy `/sync-branch` rồi build lại). Xem log lỗi:

```
gh pr checks <N>
gh run view <id chạy> --log-failed
```

Khi build lỗi, CI đính kèm báo cáo lint ở mục Artifacts của lần chạy đó.

```
CI của PR #<N> đỏ. Chạy gh pr checks <N> và gh run view --log-failed để đọc log thật, tìm NGUYÊN NHÂN GỐC (dòng lỗi đầu tiên, không phải hệ quả), rồi tái hiện lỗi cục bộ bằng đúng lệnh CI đã chạy. Chưa sửa gì cho đến khi tái hiện được hoặc giải thích được vì sao không tái hiện được.
```

## AI thêm Co-authored-by hoặc "Generated with"

Nhiều công cụ AI tự thêm dòng này. Hook `commit-msg` sẽ từ chối commit đó, nên thường bạn chỉ cần bỏ dòng ra. Nếu commit đã đẩy lên PR:

- Chưa đẩy: `git commit --amend` và xóa dòng đó (an toàn vì chưa ai thấy).
- Đã đẩy: không force push. Lấy branch sạch mới: `git switch -c <tên branch đúng>` từ commit hiện tại, `git reset --soft origin/main`, commit lại **một** commit sạch, đẩy, mở PR mới rồi đóng PR cũ. Không chắc thì nhờ leader.

Nhắc AI trong phiên: "Không thêm Co-authored-by, Generated with hay tên công cụ AI vào commit, PR hoặc issue (AGENTS.md R8)."

## Task quá lớn hoặc AI làm lan man

**Dấu hiệu:** kế hoạch có hơn 6 bước, hoặc diff hơn khoảng 300 dòng, hoặc AI bắt đầu "cải thiện thêm" các phần khác.

```
Issue #<N> quá lớn cho một PR. Đề xuất cách tách thành 2 đến 4 issue nhỏ, mỗi issue là một lát cắt hoạt động được và build được, có tiêu chí nghiệm thu và phạm vi riêng. Chưa tạo gì; đưa danh sách cho tôi.
```

Sau đó dùng `/new-issue` cho từng phần, nói với leader để đóng issue gốc.

## Issue mơ hồ

Không tự chọn cách hiểu rồi làm cả issue. Hỏi ở chính issue (comment) hoặc nhóm chat. Nhờ AI soạn câu hỏi:

```
Issue #<N> có chỗ chưa rõ: <chỗ đó>. Soạn cho tôi một bình luận ngắn bằng tiếng Việt gửi leader, nêu rõ 2 cách hiểu khả dĩ và cách bạn nghiêng về, để leader chỉ cần chọn.
```

Trong lúc chờ, làm phần không phụ thuộc chỗ đó.

## AI lặp vòng sửa lỗi không dứt

Dùng prompt "Dừng lại, đừng sửa nữa" ở [02-vong-lap-lam-task.md](02-vong-lap-lam-task.md) bước 3. Nếu vẫn không ra, mở phiên chat mới, dán prompt mở đầu, đọc `.agents/tmp/plan-<N>.md` và mô tả lỗi cho model khác. Đổi model thường hiệu quả hơn lặp lại với cùng một model.

## Cần thêm thư viện

Không tự thêm vào Gradle. Mở một issue `chore` (dùng `/new-issue`) nêu tên thư viện, nguồn tra phiên bản, và vì sao cần. PR của nó phải có tiêu đề bắt đầu bằng `build(` hoặc `chore(`, và nên nhỏ, merge nhanh, vì mọi người cùng dùng `libs.versions.toml`.

## Cần đổi cấu trúc Firestore hoặc Room đã có

Hỏi leader trước (luật R6). Room còn cần tăng phiên bản cơ sở dữ liệu và viết migration.

```
Issue #<N> có thể cần đổi <collection hoặc bảng> đã có trong docs/data. Trước khi sửa, hãy đọc docs/data/*, nêu chính xác trường nào đổi, những màn hình và dữ liệu nào bị ảnh hưởng (tìm trong code, không đoán), và soạn một tin ngắn bằng tiếng Việt để tôi gửi leader xin quyết định. Chưa sửa code.
```

## AI quên luật giữa chừng

Phiên chat dài thì AI mờ dần các luật ở đầu. Dấu hiệu: nó bỏ qua phạm vi, tự thêm thư viện, quên đọc `AGENTS.md`.

Cách sửa rẻ nhất: mở phiên mới, dán prompt mở đầu ([01-khoi-tao.md](01-khoi-tao.md) mục B), rồi:

```
Tiếp tục issue #<N>. Đọc .agents/tmp/plan-<N>.md để biết kế hoạch và bước nào đã xong (xem git log và git status). Làm bước kế tiếp và chỉ bước đó.
```

## Lỡ commit trên main hoặc đặt sai tên branch

Trước hết bình tĩnh, chưa mất gì: hook `pre-push` không cho đẩy lên `main`.

```
git switch -c feat/<N>-<slug>      # giữ các commit vừa làm trên branch mới, đúng tên
git branch -f main origin/main     # đưa main trên máy về đúng bản của GitHub
```

Nếu đặt sai tên branch nhưng chưa đẩy: `git branch -m feat/<N>-<slug>`. Lỡ mất commit: `git reflog` liệt kê mọi nơi HEAD từng đứng, lấy lại bằng `git switch -c <tên> <mã commit>`.

## Đăng nhập Google báo DEVELOPER_ERROR

Nguyên nhân gần như luôn là SHA-1 của khóa debug trên máy bạn chưa được đăng ký, hoặc bạn chưa tải lại `google-services.json` sau khi thêm SHA-1. Xem mục 7 và 8 của [docs/workflow/firebase-setup.md](../docs/workflow/firebase-setup.md).

## Xoay màn hình mất trạng thái, hoặc offline không chạy

Xoay màn hình làm Activity bị tạo lại. Trạng thái phải nằm trong `ViewModel` hoặc `onSaveInstanceState`. Đừng "chữa" bằng `android:configChanges` trong manifest, vì nó che lỗi thay vì sửa.

Offline: màn hình phải hiển thị dữ liệu từ Room trước rồi mới cập nhật từ Firestore, và Room chỉ có dữ liệu của những trang bạn từng xem khi còn mạng. Chính sách bộ đệm sẽ nằm ở `docs/data/room-schema.md` khi có.

```
Màn hình <tên> mất <trạng thái gì> khi xoay ngang. Đọc lớp UI và ViewModel của nó, chỉ ra trạng thái nào đang nằm ở Activity hoặc Fragment thay vì ViewModel, và đề xuất sửa nhỏ nhất. Không dùng android:configChanges.
```
