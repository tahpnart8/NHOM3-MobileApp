---
name: pubg-record-decision
description: Append a durable decision to memory/decisions.md in the required shape. Use after choosing a technology, data shape, rule or workflow, or when a missing decision was settled by picking a reasonable option.
---

`memory/decisions.md` is append only. You add to the end; you never change what is already there.

## Steps

1. **Decide it is a decision.** Routine implementation is not one. Log a choice between real alternatives that others must respect later.
2. **Read the last three entries** of `memory/decisions.md` so you do not contradict or duplicate one. If you are replacing an earlier decision, note its title.
3. **Write the entry** from `memory/decision-template.md`. Title: `## YYYY-MM-DD - <title>` with the real absolute date. Fill Decision, Context, Alternatives rejected (at least one, with the reason), Impact, Owner. English. No secrets, no emoji.
4. **Append only.** Add it at the end of the file. If it supersedes an earlier entry, that entry gets a `### Status` section added at its end, naming the new entry; nothing else in it changes. Check with `git diff --numstat memory/decisions.md`: the deleted count must be 0.
5. **Say it was recorded**, with the title, in your report.

## Not allowed

Editing, reordering or deleting an existing entry. Pasting a token, key or personal data.
