# Hướng dẫn dùng AI trong dự án PUBGApp

Thư mục này dành cho **người code**. Nó không phải luật. Nó là bộ prompt và cách làm để bạn ra lệnh cho AI một cách có "não": AI làm nhanh, còn bạn giữ quyền quyết định và biết mình đang làm gì.

Luật dành cho AI nằm ở [`AGENTS.md`](../AGENTS.md). Thao tác Git và cài đặt máy nằm ở [docs/workflow/huong-dan-lam-viec.md](../docs/workflow/huong-dan-lam-viec.md). Thư mục này không lặp lại hai file đó, chỉ nói **cách nói chuyện với AI** để hai file đó được tuân thủ.

## Guide này mềm, không cứng

Bạn được phép bỏ bước, đổi thứ tự, hoặc dùng cách nói của riêng bạn khi có lý do. Hầu hết các bước ở đây là gợi ý cho việc thông thường; việc nhỏ thì đi tắt. Chỉ có một danh sách ngắn những điều **không nên phá** (vì máy sẽ chặn hoặc hậu quả không sửa lại được), và có cách phá lệ đúng khi cần. Xem [05-khi-nao-duoc-pha-le.md](05-khi-nao-duoc-pha-le.md).

## Một task đi qua những đâu

```
Issue  ->  [Khởi tạo phiên]  ->  Explore  ->  Plan  ->  Implement  ->  Review
                                    (bạn duyệt)   (từng bước nhỏ)   (AI tự soi, rồi AI khác soi)
       ->  Mở PR  ->  Cross-check  ->  Leader duyệt  ->  Merge
```

Điểm mấu chốt: **AI không được viết code trước khi bạn đã đọc và duyệt kế hoạch**, và **mỗi bước nhỏ phải build được**. Đó là cách bạn quản lý được thứ AI làm ra.

## Các trang

| Bạn muốn | Đọc |
| --- | --- |
| Bắt đầu một phiên làm việc với AI, hoặc biến ý tưởng thành issue | [01-khoi-tao.md](01-khoi-tao.md) |
| Làm một issue: Explore, Plan, Implement, Review | [02-vong-lap-lam-task.md](02-vong-lap-lam-task.md) |
| Xong task: mở PR, nhờ AI khác kiểm, nhờ leader duyệt | [03-ket-thuc-task.md](03-ket-thuc-task.md) |
| Đang gặp lỗi hoặc bị từ chối, không biết làm gì | [04-tinh-huong.md](04-tinh-huong.md) |
| Biết cái gì được bỏ qua, cái gì không | [05-khi-nao-duoc-pha-le.md](05-khi-nao-duoc-pha-le.md) |
| Bạn là leader | [06-danh-cho-leader.md](06-danh-cho-leader.md) |

Khi gặp lỗi hoặc không biết bước tiếp theo, gõ `/guide` (hoặc bảo AI dùng skill `pubg-guide`): AI sẽ chỉ bạn đúng trang cần đọc.

## Năm nguyên tắc để prompt có "não"

1. **Đưa AI số issue và tài liệu, đừng đưa trí nhớ của bạn.** Viết "issue #12, đọc `gh issue view 12`" thay vì tóm tắt lại. Tóm tắt của bạn dễ sai và AI sẽ tin nó.
2. **Bắt AI chứng minh nó hiểu trước khi làm.** Yêu cầu nó liệt kê file thật nó đã mở. Tên lớp mà AI không mở file để kiểm là tên có thể bịa.
3. **Chia nhỏ.** Một bước bằng khoảng 100 dòng thay đổi, build được, có một commit. Bước lớn thì AI dễ lạc và bạn không đọc nổi.
4. **Đòi bằng chứng.** "Đã build" phải kèm lệnh và kết quả thật. "Đã test trên emulator" là điều AI không làm được; chỉ bạn làm được.
5. **Bạn vẫn là chủ.** Đọc `git diff` trước khi commit. Không hiểu một đoạn code thì hỏi AI giải thích, đừng để nó vào PR.

## Dùng với công cụ nào

Prompt viết bằng tiếng Việt và chạy được ở Antigravity, Claude Code và Codex. Nếu model trả lời kém với tiếng Việt, dịch prompt sang tiếng Anh, ý không đổi, nhưng **giữ nguyên yêu cầu viết issue và PR bằng tiếng Việt**. Các lệnh gạch chéo (`/start-task`, `/open-pr`, ...) chỉ có ở công cụ đọc `.agents/workflows/`; không có thì dán nguyên prompt dài đi kèm.

**Issue và PR luôn viết bằng tiếng Việt** (tiêu đề và nội dung), vì người đọc là người thật. Prompt trong guide đã nhắc AI điều này; nếu AI viết tiếng Anh, bảo nó viết lại.

Mỗi task nên dùng **một phiên chat mới**. Phiên dài thì AI quên luật; xem [04-tinh-huong.md](04-tinh-huong.md) mục "AI quên luật".
