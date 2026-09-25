---
name: pubg-sync-branch
description: Bring the latest main into the current PUBGApp work branch and resolve merge conflicts without losing anyone's code. Use when main has moved, when GitHub says the branch is out of date or has conflicts, or before opening a pull request.
---

Resolving a conflict is where designed features most often disappear: a tool picks "ours" or "theirs" for a whole file and silently drops a teammate's work. This skill exists to stop that.

## Steps

1. **Check the start.** `git status` must be clean (commit or stash first). `git branch --show-current` must be a work branch, never `main`.
2. **Merge, never rebase.** `git fetch origin`, then `git merge origin/main`. Rebasing rewrites commits that are already on the pull request; do not do it.
3. **No conflict:** go to step 6.
4. **For each conflicted file** (`git diff --name-only --diff-filter=U`):
   - Read both sides and the common base: `git show :1:<file>` (base), `:2:` (this branch), `:3:` (main).
   - Work out what each side intended. The default result **keeps both changes**. Code that main added belongs to someone else's merged feature and must survive.
   - Never resolve a whole file with `git checkout --ours` or `--theirs`, and never delete a conflict block to make it go away.
   - `AndroidManifest.xml`, `strings_*.xml`, `libs.versions.toml`: keep every entry from both sides, no duplicates, no reordering.
   - If the two sides contradict each other (both changed the same logic differently), stop and ask the user, showing both versions. Do not pick one yourself.
5. **Finish the merge.** Stage the resolved files and `git commit` (keep Git's default `Merge ...` message; no attribution lines).
6. **Verify the result.** From `PUBGApp/`: `./gradlew assembleDebug testDebugUnitTest lintDebug`. Then `git diff --stat origin/main...HEAD` and confirm only this issue's files differ from main; any file of another feature showing up there means a resolution went wrong.
7. **Push** with `git push` (never force).
8. **Report**: which files conflicted, how each was resolved and why, the build result, and anything you asked the user to decide.
