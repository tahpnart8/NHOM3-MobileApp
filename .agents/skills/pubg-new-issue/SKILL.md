---
name: pubg-new-issue
description: Turn a task idea into a correctly formed GitHub issue for PUBGApp (feature, bug, chore), written in Vietnamese. Use when someone wants to create, file, log or plan a task, before any code is written.
---

Create one issue that another person or AI can pick up and finish without asking questions. Do not write code.

**The issue is written in Vietnamese**, title and body (AGENTS.md R14), because people read it. The form headings below are Vietnamese; use them exactly. Keep in their original form: code names, file paths, commands, and quoted error messages. Write plain Vietnamese with accents. Do not translate identifiers such as `ListingRepository`.

## Steps

1. **Understand the request.** If what or why is unclear, ask at most three short questions, in Vietnamese. Do not guess the goal.
2. **Check for duplicates.** Run `gh issue list --state open --search "<2 or 3 keywords>" --limit 10`. If an open issue already covers it, show it and stop.
3. **Find the feature, the plan item and the STT rows.** Read `workflow/docs/product/features.md` (a `feature` or `bug` issue needs a feature `F-xx` whose status is not `proposed`), the item in `workflow/plan/` (code such as `P2-B3`) and the rows in `workflow/docs/product/dac-ta-chuc-nang.md`. Never invent an ID, a plan code or an STT. If no plan item covers the idea, or an STT row is "Phát triển sau", create no code issue: tell the requester to ask the leader. If the feature is not registered, create a `chore` issue "Đăng ký tính năng <tên>" (label `area:docs`) for the leader to approve.
4. **Pick the form.** `feature` (new behaviour), `bug` (wrong behaviour), `chore` (build, dependency, tooling, docs). Never create a `removal` issue; only the leader does.
5. **Fill the body with the same headings as the form** in `.github/ISSUE_TEMPLATE/<form>.yml` (the CLI cannot open the form). Each heading is written as a level 3 heading (`### Mã tính năng`). Feature form:
   - `Mã tính năng` (F-xx), `Mã việc kế hoạch` (also keep a line `Plan item: P2-B3`), `Module` (A to E), `STT trong bảng đặc tả` (only "Đợt này" rows), `Màn hình mockup`
   - `Làm gì và vì sao`
   - `Tiêu chí nghiệm thu`: a checklist, each line verifiable on a device; include "màn hình giữ nguyên trạng thái khi xoay" for any screen
   - `Tài liệu phải đọc`: exact sections of `dac-ta-lop/`, `data/`, `trang-thai.md`, the mockup
   - `Lớp, trường, trạng thái sẽ dùng`: only names you found in the specification, each with its document; never a name you made up
   - `File được tạo hoặc sửa`: full paths, one per line, marked [tạo] or [sửa]; for a shared class, the exact methods whose bodies the person fills (`workflow/plan/so-huu.md` section 5). Every file must belong to the module in `Module`
   - `File dùng chung cần thêm dòng`: shared files from `so-huu.md` section 6 and the lines to add; the issue must be agreed with the file's owner
   - `Cấm đụng`, `Phụ thuộc` (plan codes and classes that must already be on `main`), `Ngoài phạm vi`, `Tài liệu cần cập nhật`, `Quy mô` (checkbox)
   Bug form: `Mã tính năng`, `Mã việc kế hoạch`, `Module`, `STT trong bảng đặc tả`, `Các bước tái hiện`, `Kết quả mong đợi và kết quả thực tế`, `Thiết bị và bản Android`, `File được tạo hoặc sửa`, `Cấm đụng`. Chore form: `Mã việc kế hoạch`, `Làm gì và vì sao`, `Tiêu chí nghiệm thu`, `File được phép sửa`, `Cấm đụng`, `Phụ thuộc`.
6. **Check the size.** One vertical slice (layout, Fragment, ViewModel, the assigned repository methods and their test) of one module, half a day to one day of work. If it is bigger, needs files of two modules, or touches two features, split it and create the pieces.
7. **Show the draft first** when the person asked to see it (the guide does), and create only after they say so. Otherwise go on.
8. **Create it.** Title: an imperative Vietnamese sentence, no prefix, no issue number, for example `Thêm đăng nhập bằng Google vào màn hình đăng nhập`. Then run:
   `gh issue create --title "<title>" --body-file <file> --label "type:<type>" --label "area:<area>"`
   Add `--assignee <login>` only when the requester asked for that person. Never assign an issue to someone else on your own.
9. **Report**, in Vietnamese, the issue URL and number, and which acceptance criteria you could not make specific and why.

## Not allowed

No emoji, no AI attribution, no invented feature ID, plan code, STT or class name, no "Phát triển sau" row, no label outside the list in `.github/NAMING.md`, and no issue body in English.
