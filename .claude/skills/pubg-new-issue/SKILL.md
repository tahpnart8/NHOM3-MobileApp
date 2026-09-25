---
name: pubg-new-issue
description: Turn a task idea into a correctly formed GitHub issue for PUBGApp (feature, bug, chore). Use when someone wants to create, file, log or plan a task, before any code is written.
---

Create one issue that another person or AI can pick up and finish without asking questions. Do not write code.

## Steps

1. **Understand the request.** If what or why is unclear, ask at most three short questions. Do not guess the goal.
2. **Check for duplicates.** Run `gh issue list --state open --search "<2 or 3 keywords>" --limit 10`. If an open issue already covers it, show it and stop.
3. **Find the feature.** Read `docs/product/features.md`. A `feature` or `bug` issue needs an existing feature ID whose status is not `proposed`. If the feature is not registered, do not invent an ID: create a `chore` issue "Register the <name> feature" (label `area:docs`) and tell the requester that the leader must approve the entry first.
4. **Pick the form.** `feature` (new behaviour), `bug` (wrong behaviour), `chore` (build, dependency, tooling, docs). Never create a `removal` issue; only the leader does.
5. **Fill the body with the same headings as the form** in `.github/ISSUE_TEMPLATE/<form>.yml` (the CLI cannot open the form):
   - Feature ID
   - What and why
   - Acceptance criteria: a checklist, each line verifiable on a device; include "keeps its state when rotated" for any screen
   - Packages or files allowed to change: exact paths, for example `com.nhom3.pubgapp.feature.auth`
   - Out of scope
   - Documents to update
6. **Check the size.** One vertical slice of one feature, under about 300 changed lines. If it is bigger or touches two features, split it and create the pieces.
7. **Create it.** Title: an imperative sentence in sentence case, no prefix, no issue number. Then run:
   `gh issue create --title "<title>" --body-file <file> --label "type:<type>" --label "area:<area>"`
   Add `--assignee <login>` only when the requester asked for that person. Never assign an issue to someone else on your own.
8. **Report** the issue URL and number. State which acceptance criteria you could not make specific and why.

## Not allowed

No emoji, no AI attribution, no invented feature ID, no label outside the list in `.github/NAMING.md`.
