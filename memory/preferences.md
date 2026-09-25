# Preferences

Standing instructions from the team leader (`tahpnart8`) that outlive a single task. Decisions about the system go in `decisions.md`, not here.

## How the team works

- **The leader reviews and merges every pull request.** Members open pull requests; nobody else merges.
- **Most code is written by AI tools inside Antigravity** (Gemini and Claude models) by the engineers; the leader uses Claude Code to direct the project and to review. The repository must therefore give any AI enough context to avoid inventing things, breaking working code or deleting designed features.
- **Everyone works the same way**: issue, branch, pull request, review, squash merge. No personal variations of naming, templates or flow.

## Contributors

- **Only the five people in `people.md` may be contributors.** AI tools must never appear as author, co-author or contributor anywhere: no `Co-authored-by`, no "Generated with", no bot account. Enforced by hooks, CI and repository rules.

## Communication

- Talk to the leader in Vietnamese.
- Files for AI tools, code, commit messages, issues and pull requests are in English. `docs/` and `README.md` are in Vietnamese.
- No emoji in any written artifact.
- The leader cares about the technical side only, not about the course report text.

## Reporting

- Report the exact command run and its real result. A check that failed, was skipped or could not run is reported as such.
- Never describe untested work as working. Green CI is a claim to check on the merged tree, not a conclusion.
