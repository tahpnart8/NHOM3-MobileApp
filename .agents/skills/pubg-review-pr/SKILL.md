---
name: pubg-review-pr
description: Leader only. Review a PUBGApp pull request against its issue by building and testing the merged tree, and report a recommendation. Use when the leader asks to review, check or judge a pull request. Does not approve or merge unless told.
---

The question a review answers: **if this merges right now, is `main` still correct?** The diff alone does not answer it. Green CI on the branch is evidence about the branch, not about `main`.

## Steps

1. **Read.** `gh pr view N --json title,body,author,headRefName,baseRefName,files,statusCheckRollup` and the linked issue (`gh issue view <closed issue>`). Note the acceptance criteria and the scope fence.
2. **Get the merged tree.** Remember the current branch. `git fetch origin pull/N/head:pr-N`, `git switch pr-N`, `git merge origin/main`. If it conflicts, that is a finding; abort the merge with `git merge --abort` and report.
3. **Build and test the merged tree.** From `PUBGApp/`: `./gradlew assembleDebug testDebugUnitTest lintDebug`. Report the exact result.
4. **Check each acceptance criterion** one by one against the code, not against the pull request description. Mark each met, not met, or cannot verify without a device.
5. **Look for lost work.** `git diff --diff-filter=DR --name-status origin/main...pr-N` lists deleted and renamed files. Also scan the diff for large removed blocks in feature code. Any removal without an approved `type:removal` issue and `Removal-Issue: #N` is a blocking finding (AGENTS.md R4).
6. **Check the fence and the rules.** Files outside the issue's scope; new dependencies (R5); changes to an existing Firestore collection or Room table without approval (R6); authority taken from the client (R12); secrets (R11); network or disk work on the main thread; missing rotation handling.
7. **Check documents.** `docs/data/*` and `docs/product/features.md` match the change (R7). Use the `pubg-doc-check` skill if unsure.
8. **Check contributors.** Commits authored only by the team, no `Co-authored-by`, no AI byline (R8).
9. **Return to the original branch** and delete the local `pr-N` branch.
10. **Report, in Vietnamese,** in this order: recommendation, then findings with file and line, each labelled `blocking`, `major`, `minor` or `question`, then the exact commands you ran and their results, then what you could not check. A finding that the pull request title or body is not in Vietnamese is a `major` one (AGENTS.md R14). Any comment the leader asks you to draft for the pull request is also Vietnamese, without the name of any AI tool.

## Recommendation values

`READY`, `CHANGES NEEDED`, or `BLOCKED`. This is a recommendation. **You never approve or merge on your own initiative.** When the leader tells you to merge, run `gh pr merge N --squash` (the ruleset supplies the message); they decided and you are the hands.

## Read only

Do not edit the pull request's files to make it pass. A reviewer who patches quietly destroys the value of the review.
