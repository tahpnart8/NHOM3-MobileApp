---
name: pubg-new-issue
description: Turn a task idea into a correctly formed GitHub issue for PUBGApp (feature, bug, chore), written in Vietnamese. Use when someone wants to create, file, log or plan a task, before any code is written.
---

Create one issue that another person or AI can pick up and finish without asking questions. Do not write code.

**The issue is written in Vietnamese**, title and body (AGENTS.md R14), because people read it. The form headings below are Vietnamese; use them exactly. Keep in their original form: code names, file paths, commands, and quoted error messages. Write plain Vietnamese with accents. Do not translate identifiers such as `ListingRepository`.

## Steps

1. **Understand the request.** If what or why is unclear, ask at most three short questions, in Vietnamese. Do not guess the goal.
2. **Check for duplicates.** Run `gh issue list --state open --search "<2 or 3 keywords>" --limit 10`. If an open issue already covers it, show it and stop.
3. **Find the feature.** Read `docs/product/features.md`. A `feature` or `bug` issue needs an existing feature ID whose status is not `proposed`. If the feature is not registered, do not invent an ID: create a `chore` issue "Đăng ký tính năng <tên>" (label `area:docs`) and tell the requester that the leader must approve the entry first.
4. **Pick the form.** `feature` (new behaviour), `bug` (wrong behaviour), `chore` (build, dependency, tooling, docs). Never create a `removal` issue; only the leader does.
5. **Fill the body with the same headings as the form** in `.github/ISSUE_TEMPLATE/<form>.yml` (the CLI cannot open the form). Each heading is written as a level 3 heading (`### Mã tính năng`):
   - `Mã tính năng` (feature and bug forms only)
   - `Làm gì và vì sao` (`Các bước tái hiện` and `Kết quả mong đợi và kết quả thực tế` for a bug)
   - `Tiêu chí nghiệm thu`: a checklist, each line verifiable on a device; include "màn hình giữ nguyên trạng thái khi xoay" for any screen
   - `Gói hoặc file được phép sửa`: exact paths, for example `com.nhom3.pubgapp.feature.auth` (`File được phép sửa` in a chore)
   - `Ngoài phạm vi`
   - `Tài liệu cần cập nhật`
6. **Check the size.** One vertical slice of one feature, under about 300 changed lines. If it is bigger or touches two features, split it and create the pieces.
7. **Show the draft first** when the person asked to see it (the guide does), and create only after they say so. Otherwise go on.
8. **Create it.** Title: an imperative Vietnamese sentence, no prefix, no issue number, for example `Thêm đăng nhập bằng Google vào màn hình đăng nhập`. Then run:
   `gh issue create --title "<title>" --body-file <file> --label "type:<type>" --label "area:<area>"`
   Add `--assignee <login>` only when the requester asked for that person. Never assign an issue to someone else on your own.
9. **Report**, in Vietnamese, the issue URL and number, and which acceptance criteria you could not make specific and why.

## Not allowed

No emoji, no AI attribution, no invented feature ID, no label outside the list in `.github/NAMING.md`, and no issue body in English.
