@AGENTS.md

## Claude Code specifics

- The skills in `.claude/skills/` are a mirror of `.agents/skills/`, which is the source. Edit `.agents/skills/`, then run `sh scripts/sync-ai-skills.sh`. CI fails when the two differ.
- `.claude/agents/` holds two read only sub-agents: `planner` (turns an issue into a plan) and `reviewer` (judges a pull request). Neither can edit files, on purpose.
- `.claude/settings.json` empties the commit and pull request attribution. It is not enough on its own, because that setting has a known bug; you must still never write a `Co-Authored-By` line or a "Generated with" footer yourself (AGENTS.md rule R8).
- The leader uses Claude Code to direct the project and to review. When the leader asks you to merge a pull request, do it with `gh pr merge --squash`; the leader made the decision and you are the hands (AGENTS.md rule R10).
