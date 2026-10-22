---
name: pubg-start-task
description: Begin work on a PUBGApp issue: read it, claim it, create the correctly named branch from the latest main, and state the scope fence before any code. Use when told to work on, start, pick up or implement an issue number.
---

Prepare to implement issue `N`. This skill ends with a plan; writing code starts after it.

## Steps

1. **Read the issue.** `gh issue view N --json number,title,body,labels,assignees,state`. Stop if it is closed, is a `type:removal` issue you were not told to do, or is assigned to a different person than the one you are working for.
2. **Read what it names.** The feature row (`F-xx`) in `workflow/docs/product/features.md`; the plan item (for example `P2-B3`) in `workflow/plan/`; the STT rows in `workflow/docs/product/dac-ta-chuc-nang.md` (stop if any is "Phát triển sau"); the mockup codes in `workflow/docs/ui/`; the sections of `workflow/docs/architecture/dac-ta-lop/`, `trang-thai.md` and `workflow/docs/data/` that the issue lists; and `workflow/plan/so-huu.md` sections 3 to 6 for your module. Read `workflow/memory/code/README.md` and the `workflow/memory/code/` file for the area you will code in. Do not read more than you need.
   Check the issue is complete: module, `F-xx`, plan item, STT rows, names used, file list, dependencies. If one is missing, stop and say which (AGENTS.md R23).
3. **Check the module and the dependencies.** The module in the issue must be the one that owns the files it lists (`so-huu.md`). Every class, method or file listed under "Phụ thuộc" must already be on `origin/main`. If not, stop, do not create it, and tell the person to label the issue `blocked`.
4. **Claim it.** If unassigned, `gh issue edit N --add-assignee @me`.
5. **Get a clean base.** `git status` must be clean; if not, stop and tell the user. Then `git fetch origin` and `git switch -c <type>/N-<slug> origin/main`. Type comes from the issue (`type:feature` gives `feat`, `type:bug` gives `fix`, `type:chore` gives `chore` or `build` or `docs`). The slug is 2 to 6 lowercase words. Do not start from a stale branch.
6. **Install the guards once.** If `git config core.hooksPath` is not `.githooks`, tell the user to run `sh workflow/scripts/setup-dev.sh` (Windows: `workflow/scripts/setup-dev.ps1`).
7. **State the scope fence.** Copy it from the issue, verbatim: the files you may create or change (marked [tạo] or [sửa]), the shared files with the lines you may add, and the forbidden files. Do not widen it and do not write your own. Add what is out of scope, the existing code you must not delete or rename (AGENTS.md R4), and the documents you will update (R7). If the work needs a file that is not listed, stop and comment on the issue (R19).
8. **Propose a short plan**: the steps, the files each step touches, how you will verify it. List the names you will use with the document line that defines each (R16). Anything that needs an AGENTS.md R6 question (existing schema, dependency, removal) or a name that is not in the specification is asked now.
9. **Wait** for the requester's go-ahead if the plan involves a choice they did not make. Otherwise start.

## Report

In Vietnamese: the branch name, the scope fence, the plan, and anything unverified. Never claim a check ran that did not. Any comment you post on the issue is in Vietnamese too (AGENTS.md R14).
