# 06. Dành cho leader

Prompt cho người duyệt và merge. Dùng với Claude Code hoặc công cụ nào bạn chọn. Nguyên tắc: **AI đề xuất, bạn quyết định** (`AGENTS.md` R10). AI chỉ merge khi bạn nói rõ.

## Duyệt một PR

```
/review-pr <N>
```

Skill `pubg-review-pr` checkout PR, merge `origin/main` vào một branch tạm, build và test **cây đã merge** (xanh trên branch chưa chắc đúng trên `main`), đối chiếu từng tiêu chí nghiệm thu với code, tìm file bị xóa hoặc đổi tên, kiểm phạm vi, thư viện mới và tài liệu. Kết quả là khuyến nghị `READY`, `CHANGES NEEDED` hoặc `BLOCKED` kèm phát hiện theo file và dòng.

Điều nên đọc thêm bằng mắt của bạn:

- Dòng `Deviation:` trong phần "Tóm tắt" (nếu có): thành viên bỏ bước nào và vì sao, có hợp lý không.
- Bình luận cross-check của thành viên: có đúng là kết quả của một lượt review độc lập không, hay chỉ là câu xác nhận.
- Mục "Đã kiểm tra thế nào": phần nào đã chạy trên máy thật.

## Yêu cầu sửa

```
Viết bình luận review cho PR #<N> từ các phát hiện blocking và major ở trên. Mỗi ý gồm: file và dòng, vấn đề, cách sửa đề xuất. Viết bằng tiếng Việt, giọng lịch sự, ngắn, không nêu tên công cụ AI. Cho tôi xem trước, tôi sẽ đăng.
```

## Merge

Khi bạn đã quyết định:

```
Merge PR #<N> bằng squash. Trước đó xác nhận các check build và policy đều xanh, không có xung đột, và tiêu đề PR đúng khuôn. Sau khi merge, đọc lại main để chắc commit chỉ có tiêu đề PR và không có dòng đồng tác giả.
```

Lệnh sẽ là `gh pr merge <N> --squash`. Ruleset của repo đặt nội dung commit squash là tiêu đề PR với thân trống.

## Mở việc và giao việc

```
Đọc docs/product/features.md và các issue đang mở (gh issue list). Đề xuất 3 issue tiếp theo nên mở, mỗi issue là một lát cắt nhỏ, có tiêu chí nghiệm thu kiểm được và phạm vi rõ. Chưa tạo; đưa danh sách để tôi duyệt.
```

Giao việc bằng cách gán assignee cho issue. Tính năng phải ở trạng thái `planned` trong `features.md` thì mới nên có issue mã nguồn.

## Việc định kỳ

```
Kiểm tra sức khỏe repo và báo cáo, không sửa gì: 1) gh api repos/tahpnart8/NHOM3-MobileApp/contributors (chỉ được có 5 login trong .github/team.txt), 2) sh scripts/apply-github-settings.sh --show (ruleset, squash-only, giới hạn tương tác còn hạn không; giới hạn tối đa 6 tháng phải gia hạn), 3) gh pr list và gh issue list: PR nào treo, issue nào quá lớn, 4) docs/product/features.md có khớp với các PR đã merge không (dùng /doc-check).
```

## Khi thành viên báo "AI làm mất code"

```
Thành viên báo PR #<N> làm mất code ở <chỗ đó>. Chạy git log --diff-filter=D --stat và git diff origin/main...pr-<N> --diff-filter=DR để tìm file, hàm bị xóa hoặc đổi tên, so với issue. Cho tôi biết chính xác cái gì mất, do commit nào, và cách phục hồi từ lịch sử. Chưa sửa.
```
