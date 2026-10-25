---
name: pubg-sync-branch
description: Rebase the current PUBGApp work branch onto the latest main and resolve conflicts without losing anyone's code. Use only when GitHub says the branch has conflicts, or when the leader asks for it. Never merge main into a work branch.
---

Resolving a conflict is where designed features most often disappear: a tool picks "ours" or "theirs" for a whole file and silently drops a teammate's work. This skill exists to stop that.

Pull requests are merged with rebase and merge, so every commit of the branch lands on `main`. GitHub refuses that when the branch holds a merge commit, and CI fails such a pull request. So the branch is **rebased** onto `main`, never merged.

## When to run it

Only when GitHub shows "This branch has conflicts that must be resolved", or the leader asks. A branch that is merely behind `main` needs nothing: rebase and merge applies the commits on the newest `main` by itself.

## Steps

1. **Check the start.** `git status` must be clean (commit or stash first). `git branch --show-current` must be your own work branch, never `main`.
2. **Back up first.** `git branch backup/$(git branch --show-current)-before-rebase`. If anything goes wrong, this branch is your way back. Do not delete it until the pull request is merged.
3. **Rebase.** `git fetch origin`, then `git rebase origin/main`. Never `git merge origin/main`.
4. **For each conflict Git stops on** (`git diff --name-only --diff-filter=U`):
   - Read both sides. In a rebase, `HEAD` side (`<<<<<<<`) is `main` (other people's merged work) and the other side is your commit.
   - The default result **keeps both changes**. Code that main added belongs to someone else's merged feature and must survive.
   - Never resolve a whole file with `git checkout --ours` or `--theirs`, and never delete a conflict block to make it go away.
   - `AndroidManifest.xml`, `strings_*.xml`, `nav_*.xml`, `libs.versions.toml`: keep every entry from both sides, no duplicates, no reordering. These are shared files with one owner (`workflow/plan/so-huu.md` section 6); if your branch should not have touched one, say so.
   - If the two sides contradict each other (both changed the same logic differently), run `git rebase --abort` and ask the user, showing both versions. Do not pick one yourself.
   - Then `git add <files>` and `git rebase --continue`. Do not edit the commit message in a way that drops its subject grammar.
5. **Verify the result.** From `PUBGApp/`: `./gradlew assembleDebug testDebugUnitTest lintDebug`. Then `git diff --stat origin/main...HEAD` and confirm only this issue's files differ from main; any file of another feature showing up there means a resolution went wrong, so go back to the backup branch.
6. **Push your own branch only.** `git push --force-with-lease origin <your-branch>`. Never plain `--force`, never any push to `main`. If `--force-with-lease` is rejected, someone else pushed to your branch: stop and ask the user.
7. **Report**: which files conflicted, how each was resolved and why, the build result, the name of the backup branch, and anything you asked the user to decide.
