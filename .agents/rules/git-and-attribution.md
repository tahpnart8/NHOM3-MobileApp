---
trigger: always_on
description: Git workflow and the ban on AI attribution; applies to every task
---

# Git, contributors and attribution

The full rulebook is `AGENTS.md`. These points are repeated here because they are the ones tools most often break.

- Only the 5 people in `memory/people.md` may appear as contributors. **Never** write a `Co-authored-by` trailer, a "Generated with" or "Made with" line, a robot emoji byline, or name an AI tool as author, in a commit message, pull request or issue. The hooks and CI reject them.
- Never run `git config user.name` or `git config user.email`. Commits are authored by the human whose machine you are on.
- Branch from the latest `main` as `type/issue-slug`. One issue per pull request. Never push to `main`. Never force push. Never use `--no-verify`.
- Commit subject: `type(scope): imperative summary`, lowercase, 72 characters at most, no period. Types: feat fix docs refactor test chore build ci.
- Do not delete or rename existing feature code unless a `type:removal` issue says so (AGENTS.md R4).
- Say exactly what you ran and what it returned. Do not say something works if you did not run it.
