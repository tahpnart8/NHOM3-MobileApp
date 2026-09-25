# 05. Khi nào được phá lệ

Guide này sinh ra để giúp bạn, không phải để bắt bạn. Bạn chịu trách nhiệm về **kết quả** (code đúng, đọc được, không phá thứ của người khác), không phải về việc làm đủ mọi bước. Trang này nói rõ cái nào bỏ qua được, cái nào nên giữ, và cách phá lệ cho đúng.

## Bỏ qua thoải mái khi hợp lý

Không cần xin phép và không cần giải thích, trừ khi bạn thấy nó đáng nói:

| Bước trong guide | Bỏ hoặc rút gọn khi |
| --- | --- |
| Prompt mở đầu phiên | Bạn chỉ hỏi một câu, hoặc sửa một lỗi chính tả |
| Explore rồi Plan thành hai bước riêng | Việc nhỏ hoặc bạn đã biết rõ code cần sửa: gộp thành hai câu |
| Chia nhiều bước, mỗi bước một commit | Thay đổi dưới khoảng 30 dòng |
| AI tự review và cross-check bằng AI khác | PR chỉ sửa tài liệu, chữ, màu, vài dòng; hoặc leader đã trực tiếp xem cùng bạn |
| Lưu kế hoạch vào `.agents/tmp/` | Phiên ngắn, không sợ mất ngữ cảnh |
| Dùng đúng prompt mẫu | Luôn được. Prompt trong guide là khuôn mẫu; viết cách của bạn nếu ra kết quả tốt hơn. Giữ ý chính: AI cho thấy nó hiểu, bạn duyệt, làm từng bước, có bằng chứng |
| Dùng lệnh gạch chéo (`/start-task`, ...) | Bạn tự tạo branch và tự mở PR đúng quy ước |
| Cập nhật `docs/` | Thay đổi không đổi hành vi hay cấu trúc dữ liệu (ví dụ chỉnh giao diện nhỏ) |

## Nên giữ, và lý do

Đây không phải luật để làm khó bạn. Mỗi điều đều gắn với một hậu quả cụ thể đã hoặc dễ xảy ra:

| Nên giữ | Vì sao |
| --- | --- |
| Đọc `git diff` trước khi commit | AI có thể xóa hoặc sửa thứ bạn không thấy; PR sẽ mang tên bạn |
| Nói đúng lệnh và kết quả thật trong PR | Cả nhóm dựa vào đó để biết cái gì đã được kiểm |
| Tự chạy trên emulator luồng chính, xoay màn hình và chế độ máy bay | AI không làm được, và đây là yêu cầu của môn học |
| Hỏi leader khi đụng cấu trúc Firestore hoặc Room đã có, thư viện, hoặc xóa tính năng (R6) | Thiệt hại lan sang việc của người khác |
| Một issue, một branch, một PR | Để leader đọc nổi và để hoàn tác được từng việc |

Những điều này không có máy nào ép, nên nếu bạn phá thì phần lớn chỉ mình bạn thấy hậu quả. Cũng vì vậy, nếu phá, hãy làm có chủ ý (xem cách ở dưới).

## Máy sẽ chặn; đừng cố lách

Những điều dưới đây có hook trên máy hoặc CI trên GitHub kiểm tự động. Phá chúng chỉ làm PR của bạn đỏ và mất thời gian, nên coi là ranh giới thay vì gợi ý:

| Ranh giới | Vì sao tồn tại |
| --- | --- |
| Không có dòng `Co-authored-by`, "Generated with", hay tên công cụ AI ở commit, PR, issue | Chỉ 5 thành viên được hiện là contributor trên GitHub; nhóm đã cấu hình mọi lớp để giữ điều này |
| Không đẩy lên `main`, không force push, không `--no-verify` | Mọi thay đổi phải qua PR và qua kiểm tra; lịch sử không được viết lại |
| Không commit `local.properties`, khóa, mật khẩu, token | Repo công khai; lộ khóa không rút lại được |
| Tên branch, tiêu đề PR, subject commit đúng khuôn (`.github/NAMING.md`) | Lịch sử đọc được và máy kiểm được |
| PR ghi `Closes #N` hoặc `Closes: none` | Mỗi PR gắn với một việc |
| Xóa hoặc đổi tên code trong `PUBGApp/app/src/main` cần issue `type:removal` do leader mở | Bảo vệ tính năng đã thiết kế khỏi bị AI xóa nhầm |
| Sửa Gradle hoặc thêm thư viện chỉ trong PR `build(...)` hoặc `chore(...)` | Mọi người dùng chung một bộ thư viện |
| Không sửa hay xóa dòng cũ trong `memory/decisions.md` | Sổ quyết định là lịch sử |

**Nếu bạn thấy một ranh giới ở trên đang cản một việc hợp lý:** đừng tìm cách lách, hãy nhắn leader. Leader có thể mở issue `type:removal`, đổi luật, hoặc giải thích. Đó là cách đúng để đổi ranh giới.

## Cách phá lệ đúng

Bỏ qua một bước "nên giữ" là quyền của bạn. Làm như sau:

1. **Hiểu vì sao bước đó tồn tại** (bảng trên).
2. **Nếu bạn vẫn thấy phá là hợp lý, cứ phá**, rồi ghi một dòng trong phần Summary của PR bắt đầu bằng `Deviation:`. Ví dụ:
   `Deviation: bỏ bước cross-check bằng AI vì PR chỉ đổi 12 dòng chữ trong strings_auth.xml.`
   Một dòng là đủ. Nó không phải đơn xin phép; nó giúp leader biết đọc PR ở đâu.
3. **Nếu bạn phá cùng một chỗ nhiều lần**, có thể bước đó không hợp với dự án. Mở issue `chore` gắn nhãn `area:docs` đề xuất sửa guide hoặc `AGENTS.md`. Chỉ leader sửa các file đó.

## Khi AI không chịu làm vì luật

`AGENTS.md` là luật cho công cụ AI, không phải luật cho bạn. Nếu AI từ chối vì một luật mà bạn muốn phá có chủ đích ở phần "Nên giữ" hoặc "Bỏ qua thoải mái", bạn nói thẳng lý do và giới hạn:

```
Tôi biết luật <số R> yêu cầu <điều đó>. Lần này tôi chọn bỏ qua vì <lý do>. Chỉ làm <phạm vi hẹp>, và ghi lại điều đã bỏ qua để tôi đưa vào dòng Deviation của PR.
```

Điều này **không** áp dụng cho phần "Máy sẽ chặn": AI sẽ không (và không nên) thêm dòng đồng tác giả, đẩy lên `main` hay commit khóa bí mật, kể cả khi bạn bảo. Nếu bạn thật sự cần một trong số đó, nhắn leader.

## Bản thân guide cũng có thể sai

Guide viết trước khi cả nhóm dùng thử. Nếu một prompt cho kết quả tệ, hoặc một bước làm bạn chậm mà không giúp gì, hãy nói với leader. Prompt tốt hơn của bạn đáng được đưa vào đây.
