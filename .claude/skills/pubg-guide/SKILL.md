---
name: pubg-guide
description: Point a PUBGApp contributor to the right page of the team guide in guide/ when they are stuck, when a hook, CI check or rule refuses something, when they are about to skip or break a workflow step, or when they ask how the team workflow works. A reminder to the person, not a gate.
---

The guide in `guide/` is written for the person coding, in Vietnamese. It is advice. This skill helps that person find the right page at the moment they need it. It never blocks anyone.

## When to use it

- The person is stuck or says they do not know what to do next.
- A hook, a CI check, or a rule just refused something and the person does not know why.
- You notice the person is about to do something the guide warns against (see "Speak up once" below).
- The person asks how the team workflow, prompts or review process work.

## Steps

1. **Find the situation.** Match the error text or what the person said to a row of the table below. If none fits, read `guide/README.md` and pick the closest page.
2. **Read the section** of the guide page you are about to recommend. Do not answer from memory; the page is the source and it may have changed.
3. **Tell the person, in Vietnamese and in a few lines:** what went wrong (quote the real message if there is one), the guide page and section that covers it, and the one next action. No lecture.
4. **Offer to do the fix** when it is inside the person's current task and reversible. Do not run anything destructive without their say.

| Situation | Guide page |
| --- | --- |
| Starting a session, checking the machine, turning an idea into an issue | `guide/01-khoi-tao.md` |
| Working an issue: exploring, planning, implementing in steps, self-review | `guide/02-vong-lap-lam-task.md` |
| Finished code: opening the PR, asking another AI to cross-check, asking the leader, answering review comments | `guide/03-ket-thuc-task.md` |
| Build or CI red, hook refusal, merge conflict, AI invented an API, AI deleted code, `Co-authored-by`, `DEVELOPER_ERROR`, lost state on rotation, AI forgot the rules | `guide/04-tinh-huong.md` |
| Wants to skip a step, or a rule seems to block a reasonable thing | `guide/05-khi-nao-duoc-pha-le.md` |
| AI wrote wordy, lambda-heavy or over-commented code, or the person asks how the code should look | `guide/04-tinh-huong.md` (section on code style) and the `pubg-code` skill |
| The person is the leader: reviewing, merging, opening work | `guide/06-danh-cho-leader.md` |

## The guide is flexible

- If the person wants to skip or change a step, say in one line what that costs, then let them. It is their call. Suggest a `Deviation:` line in the pull request summary (`guide/05-khi-nao-duoc-pha-le.md`) and move on.
- Do not repeat a reminder the person has already dismissed, and do not treat a skipped guide step as an error.
- The exception is the list "Máy sẽ chặn" in `guide/05-khi-nao-duoc-pha-le.md`: no AI attribution, no push to `main`, no secrets, and the naming and removal checks. Tooling rejects these anyway. Tell the person that plainly and point them to the leader if they really need one.

## Speak up once

Say it once, briefly, and continue if they carry on:

- They are about to commit or push on `main`.
- They are coding with no issue, or the change is large and there is no plan.
- They are about to commit AI-written code they have not read (`git diff` not looked at).
- They tell you a check "passed" and you cannot see the command or its result.
- They ask you to add a dependency, change an existing Firestore or Room structure, or remove a feature without the leader's decision (AGENTS.md R6).

## Not allowed

Do not edit anything in `guide/` unless the leader asked for it. If a page is wrong or missing something, tell the person to raise it with the leader.
