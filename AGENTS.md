# AGENTS.md

The single rulebook for every AI tool working in this repository (Antigravity, Claude Code, Codex, any other).
`CLAUDE.md` imports this file; there is deliberately no `GEMINI.md`, so no second copy can disagree.
People are not bound by this file; it exists because a tool can change many files fast and can report work it did not do.

## 1. The project

**PUBGApp** (Pre-owned Users' Bargain Grounds) is an Android app for buying, selling and sharing second-hand goods. It is a social network combined with a marketplace, like Facebook Marketplace, plus seller and buyer management and order handling in the spirit of Shopee. It is a university course project (Mobile App development, team of 5). The teacher requires: Firebase, a layout that adapts on rotation, offline use of pages already viewed (SQLite), and supporting APIs such as Google sign-in. The admin side stays simple.

## 2. Stack (facts, not wishes)

- Java 17 only. **No Kotlin, no Jetpack Compose.** XML Views with ViewBinding. minSdk 30, compileSdk and targetSdk 37.
- Project directory: `PUBGApp/`. Package: `com.nhom3.pubgapp`. Build: Gradle wrapper, Kotlin DSL scripts.
- **Versions live in one place: `PUBGApp/gradle/libs.versions.toml`.** Never copy a version number into a document or another file.
- Planned, **not yet in the build**: Firebase Authentication (email and Google), Cloud Firestore, Cloud Storage, Room for the offline cache. Do not write code that assumes they are configured until `docs/architecture/overview.md` says so.

## 3. Read this at the start of a session

1. `memory/people.md`, `memory/preferences.md`, and the last entries of `memory/decisions.md`.
2. `docs/product/features.md`: the register of features. Work only on a feature that has an ID and a status other than `proposed`.
3. The issue you are working on, and every document it names.
4. The topic document you need, and no more: `docs/architecture/`, `docs/data/`, `docs/conventions/`.

## 4. Where things are

| Path | Holds |
| --- | --- |
| `PUBGApp/` | The Android project |
| `docs/` | What the system is: product, architecture, data schemas, conventions, workflow. Vietnamese |
| `memory/` | Why it is that way: decisions (append only), preferences, people |
| `.agents/` | Source of rules, workflows and skills for Antigravity and Codex |
| `.claude/` | Claude Code settings, sub-agents and the mirror of the skills |
| `.github/` | Issue and pull request templates, naming rules, CI, code owners, team list |
| `.githooks/`, `scripts/` | Local guards and helper scripts |

## 5. Commands

```
cd PUBGApp
./gradlew assembleDebug testDebugUnitTest lintDebug     # Windows: gradlew.bat
sh scripts/setup-dev.sh                                  # once per clone; Windows: scripts/setup-dev.ps1
sh scripts/tests/policy-test.sh                          # after touching scripts/ or .githooks/
```

## 6. The rules

**R1. Say what you actually ran.** Give the exact command and its real result. A check that failed, was skipped or could not run here is reported as such. Never call untested code working, and never say "tested on the emulator" unless you ran it.

**R2. Evidence, or say it is unverified.** Do not invent an API, class, method or Gradle coordinate. Look up Firebase, AndroidX and Room signatures with the `context7` MCP server or the official documentation. If you cannot verify something, write "unverified" and say what would verify it.

**R3. Stay inside the issue.** Change only what the issue asks, inside the files or packages it allows. No drive-by refactor, no formatting churn, no renamed identifiers. Report dead code you notice; do not delete it.

**R4. Never delete or rename existing feature code.** A feature in `docs/product/features.md` is protected. Removing or renaming its files, classes or methods needs an issue labelled `type:removal` opened by the leader, and the pull request must contain the line `Removal-Issue: #N`. CI fails a deletion under `PUBGApp/app/src/main` without it.

**R5. No new dependency without an issue.** Add nothing to `libs.versions.toml` or a Gradle script unless the issue is a `chore(deps)` issue that names it. Look the current version up; do not recall it from memory.

**R6. Ask first, in three cases.** (a) Changing a Firestore collection or Room table that already exists. (b) Adding or changing a dependency. (c) Removing or renaming a feature. Everything else that is undecided is not a stop: choose the reasonable option, record one line with the `pubg-record-decision` skill, and continue.

**R7. Documents change with the code.** A pull request that changes a data schema or the behaviour of a feature updates `docs/data/*` and `docs/product/features.md` in the same pull request. Do not edit a document to make it match code you could not get right; if code and document disagree, say so.

**R8. No AI attribution, ever.** Only the 5 collaborators may appear as contributors. Never add a `Co-authored-by` trailer, a "Generated with" or "Made with" line, a robot emoji byline, or any mention of an AI tool as author, in a commit message, pull request title or body, issue, or comment. Commits are authored by the human whose machine you are on. Never run `git config user.name` or `user.email`. The hooks, CI and the repository rules reject all of the above; do not look for a way around them.

**R9. Git discipline.** Branch from the latest `main` as `type/issue-slug` (for example `feat/12-google-sign-in`). One issue per pull request. Commit subjects are `type(scope): imperative summary`. Never push to `main`, never force push, never use `--no-verify`. The full grammar is `.github/NAMING.md`.

**R10. You do not decide that work is done.** No AI step approves, merges or marks its own work verified. A passing build is not a decision. When the leader asks you to merge a specific pull request, do it (`gh pr merge --squash`); they decided and you are the hands.

**R11. No secrets.** Never commit keys, keystores, tokens, passwords or `local.properties`. `google-services.json` is the only configuration file that is committed: it holds public identifiers, and the Firebase Security Rules are what protect the data.

**R12. Authority is never taken from the client.** Who a user is and what role they hold come from Firebase Authentication and the Security Rules, never from a field the app sends. Do not fake an authenticated user, not even in a test seam. When access is missing or unclear, deny and show a clear message; do not fall back silently.

**R13. Smallest change that does the job.** Reuse what exists before writing new code. Non-trivial logic leaves one runnable check behind, the smallest test that fails if the logic breaks.

## 7. Code conventions (short form)

Details are in `docs/conventions/android-java.md`.

- Code lives in `com.nhom3.pubgapp.feature.<name>.{ui,data,model}`; shared code in `com.nhom3.pubgapp.common`.
- Use ViewBinding, not `findViewById`. Every user visible string is a resource; a feature's strings go in its own `strings_<feature>.xml`.
- No network or disk work on the main thread. Every screen must survive rotation without losing its state.
- Identifiers and comments in English. The user interface language is Vietnamese by default.

## 8. Workflow and skills

Issue, then branch, then pull request, then the leader reviews and squash merges. The skills below carry out each step the same way for everyone; their source is `.agents/skills/`.

| Skill | Use it to |
| --- | --- |
| `pubg-new-issue` | Turn an idea into a correctly formed issue |
| `pubg-start-task` | Begin an issue: read it, create the branch, state the scope fence |
| `pubg-open-pr` | Verify, then open the pull request from the template |
| `pubg-review-pr` | Leader only: judge a pull request against its issue on the merged tree |
| `pubg-record-decision` | Append a decision to `memory/decisions.md` |
| `pubg-doc-check` | Find where documents and code disagree |

## 9. Language

Talk to people in the language they use (the team writes Vietnamese). Files for AI tools, code, commits, issues and pull requests are English. `docs/` and `README.md` are Vietnamese. Do not use emoji in any written artifact.
