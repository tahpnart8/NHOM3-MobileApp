---
name: pubg-start-task
description: Begin work on a PUBGApp issue: read it, claim it, create the correctly named branch from the latest main, and state the scope fence before any code. Use when told to work on, start, pick up or implement an issue number.
---

Prepare to implement issue `N`. This skill ends with a plan; writing code starts after it.

## Steps

1. **Read the issue.** `gh issue view N --json number,title,body,labels,assignees,state`. Stop if it is closed, is a `type:removal` issue you were not told to do, or is assigned to a different person than the one you are working for.
2. **Read what it names.** The feature row in `docs/product/features.md` and every document the issue lists. Read `docs/data/*` if the issue touches Firestore or Room. Do not read more than you need.
3. **Claim it.** If unassigned, `gh issue edit N --add-assignee @me`.
4. **Get a clean base.** `git status` must be clean; if not, stop and tell the user. Then `git fetch origin` and `git switch -c <type>/N-<slug> origin/main`. Type comes from the issue (`type:feature` gives `feat`, `type:bug` gives `fix`, `type:chore` gives `chore` or `build` or `docs`). The slug is 2 to 6 lowercase words. Do not start from a stale branch.
5. **Install the guards once.** If `git config core.hooksPath` is not `.githooks`, tell the user to run `sh scripts/setup-dev.sh` (Windows: `scripts/setup-dev.ps1`).
6. **State the scope fence.** Write down: the packages or files you may change, what is out of scope, the existing code you must not delete or rename (AGENTS.md R4), and the documents you will update (R7).
7. **Propose a short plan**: the steps, the files each step touches, how you will verify it. Anything that needs an AGENTS.md R6 question (existing schema, dependency, removal) is asked now.
8. **Wait** for the requester's go-ahead if the plan involves a choice they did not make. Otherwise start.

## Report

In Vietnamese: the branch name, the scope fence, the plan, and anything unverified. Never claim a check ran that did not. Any comment you post on the issue is in Vietnamese too (AGENTS.md R14).
