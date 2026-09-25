# Cấu hình GitHub của repo

Những gì đã được áp dụng cho `tahpnart8/NHOM3-MobileApp`, để leader nhớ và gỡ nhanh khi có sự cố. Nguồn sự thật là code: `scripts/apply-github-settings.sh` và hai file trong `.github/rulesets/`. Xem trạng thái thật bất cứ lúc nào bằng `sh scripts/apply-github-settings.sh --show`, không sửa tay trên giao diện rồi quên ghi lại.

## Đã áp dụng

| Cấu hình | Giá trị |
| --- | --- |
| Cách merge | Chỉ **squash merge**. Commit trên `main` có tiêu đề là tiêu đề PR và **thân trống**, nên không dòng đồng tác giả nào lọt vào được |
| Nhánh sau khi merge | Tự xóa |
| Nút "Update branch" | Bật |
| Wiki, Discussions | Tắt |
| Secret scanning và push protection | Bật |
| Quyền mặc định của workflow | Chỉ đọc; workflow không được duyệt PR |
| Workflow từ người ngoài | Phải chờ duyệt |
| Giới hạn tương tác | Chỉ collaborator được mở issue, PR và bình luận, **đến 2027-03-25** (GitHub giới hạn tối đa 6 tháng) |
| Dependabot security updates | Tắt (nó mở PR dưới tên một bot, sẽ thành contributor) |

**Ruleset `main-checks`** (không ai được bỏ qua, kể cả leader): cấm xóa `main`, cấm force push, bắt buộc lịch sử tuyến tính, và bắt buộc hai check **`build`** và **`policy`** đều xanh trên bản đã cập nhật với `main`.

**Ruleset `main-review`**: mọi thay đổi vào `main` phải qua Pull Request, có **1 approval của người trong `CODEOWNERS`** (leader), các thảo luận đã được giải quyết, review cũ bị hủy khi có commit mới, chỉ squash. Vai trò admin được bỏ qua ruleset này **bên trong một PR**, vì leader không thể tự approve PR của chính mình.

## Việc định kỳ

- **Trước 2027-03-25:** chạy lại `sh scripts/apply-github-settings.sh` để gia hạn giới hạn tương tác. Script chạy lại được nhiều lần.
- Có thành viên mới: thêm vào `.github/team.txt` và `memory/people.md` bằng một PR, rồi mời họ làm collaborator.

## Khi một check bắt buộc chặn mọi PR

Ruleset `main-checks` không cho bỏ qua, nên nếu job `policy` (hoặc `build`) lỗi vì chính nó có bug, mọi PR bị chặn, kể cả PR sửa bug đó (job `policy` luôn chạy bản đang có trên `main`). Cách gỡ, theo thứ tự:

1. Xem lỗi thật: `gh pr checks <số PR>` rồi `gh run view <id> --log-failed`. Có thể lỗi là do PR, không phải do bug của check.
2. Nếu chắc chắn là bug của check: vào **Settings, Rules, Rulesets, `main-checks`**, đổi **Enforcement status** sang **Disabled**. Việc này chỉ admin làm được.
3. Merge PR sửa lỗi qua đường bình thường.
4. Khóa lại: đặt lại **Active** trên giao diện, hoặc chạy `sh scripts/apply-github-settings.sh` (nó khôi phục ruleset đúng như trong file).
5. Ghi một dòng vào `memory/decisions.md` nếu sự cố đổi cách bạn làm việc.

## Chi phí

Repo này công khai, nên GitHub Actions trên runner chuẩn **miễn phí**. Trang Billing của tài khoản vẫn hiện "gross usage" (giá niêm yết của số phút đã chạy), nhưng có dòng "included usage" trừ đúng bằng số đó, nên số tiền phải trả là 0. Đã đo ngày 2026-09-25: mỗi lần chạy `build` khoảng 3 phút tính tròn, 9 lần chạy là 25 phút, tương ứng 0,15 đô giá niêm yết và được miễn hoàn toàn.

Giữ nguyên hai điều kiện này để chi phí luôn bằng 0:

- **Giữ repo ở chế độ công khai.** Repo riêng tư ở gói Free chỉ có 2.000 phút mỗi tháng, và các ruleset trên repo riêng tư cần gói trả phí (theo hiểu biết của người viết, chưa kiểm lại).
- **Đặt ngân sách bằng 0 đô** ở **Billing, Budgets and alerts** với tùy chọn dừng sử dụng khi chạm hạn mức, và không bật Copilot trả phí, Codespaces hay runner lớn.

Repo `tahpnart8/tahpnart8` (hồ sơ cá nhân) và `sift` cũng có workflow riêng và cũng hiện trong cùng trang Billing; chúng không thuộc dự án này.
