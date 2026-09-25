---
name: pubg-open-pr
description: Verify finished work and open the pull request for a PUBGApp issue from the template. Use when the implementation is done and the user wants to open, create or submit a pull request.
---

Open exactly one pull request for the current branch. Verify first; opening is the last step.

## Steps

1. **Confirm the branch.** `git branch --show-current` must match `type/N-slug`. Read issue N again: `gh issue view N`.
2. **Bring in the latest main.** `git fetch origin`, then `git merge origin/main`. Resolve conflicts carefully; never resolve by deleting the other side's code. If you cannot resolve one, stop and report it.
3. **Verify.** From `PUBGApp/`: `./gradlew assembleDebug testDebugUnitTest lintDebug` (Windows `gradlew.bat`). Keep the exact result. If it fails, fix it or report it; do not open the pull request with a red build unless the user tells you to.
4. **Check scope.** `git diff --stat origin/main...HEAD`. Every changed file must be inside the issue's allowed packages or files. List anything outside and remove it, or ask.
5. **Check for deletions.** `git diff --diff-filter=DR --name-status origin/main...HEAD`. Any deleted or renamed file under `PUBGApp/app/src/main` needs an approved `type:removal` issue and a `Removal-Issue: #N` line in the body. Without one, restore the file.
6. **Check documents.** If a schema or a feature's behaviour changed, `docs/data/*` and `docs/product/features.md` are changed in this branch (AGENTS.md R7).
7. **Check the commits.** `git log --format=%s origin/main..HEAD` must be conventional subjects. `git log --format=%B origin/main..HEAD` must contain no `Co-authored-by` and no AI byline. If they do, stop and tell the user; do not rewrite history yourself.
8. **Open it.** Push the branch (`git push -u origin HEAD`), then `gh pr create --title "<primary commit subject>" --body-file <file>`. The body follows `.github/PULL_REQUEST_TEMPLATE.md`: summary, `Closes #N`, feature ID, packages changed, the issue's acceptance checklist with only the truly done boxes ticked, the exact commands and results from step 3, screenshots in portrait and landscape for a screen change.
9. **Report** the pull request URL, the verification results, what you could not run (for example the emulator), and any acceptance criterion not met.

## Not allowed

No push to `main`, no force push, no `--no-verify`, no attribution lines in the title or body, no merging the pull request, no claim of emulator testing you did not do.
