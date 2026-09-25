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
5. Before you write Java: the `pubg-code` skill, and `memory/code/README.md` with the file for the area you are about to touch.

## 4. Where things are

| Path | Holds |
| --- | --- |
| `PUBGApp/` | The Android project |
| `docs/` | What the system is: product, architecture, data schemas, conventions, workflow. Vietnamese |
| `plan/` | Delivery plan: phases and work items. Empty until the leader loads the requirements |
| `guide/` | Advice for people using AI on this repository: prompts for the explore, plan, implement and review loop and for ending a task. Vietnamese. Not a rulebook |
| `memory/` | Why it is that way: decisions (append only), preferences, people. `memory/code/` holds what people and tools learn about the code and the libraries, so it stays out of code comments |
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

**R4. Never delete or rename existing feature code.** A feature in `docs/product/features.md` is protected. Removing or renaming its files, classes or methods needs an issue labelled `type:removal` opened by the leader, and the pull request must contain the line `Removal-Issue: #N`. CI fails a deletion or rename under `PUBGApp/app/src/main` without it, and flags any file there that loses more than 40 lines. When resolving a merge conflict, keep both sides; use the `pubg-sync-branch` skill.

**R5. No new dependency without an issue.** Add nothing to `libs.versions.toml` or a Gradle script unless the issue is a `chore(deps)` or `build` issue that names it. Look the current version up; do not recall it from memory. CI fails a pull request that changes a Gradle file unless its title starts with `build(` or `chore(`.

**R6. Ask first, in three cases.** (a) Changing a Firestore collection or Room table that already exists. (b) Adding or changing a dependency. (c) Removing or renaming a feature. Everything else that is undecided is not a stop: choose the reasonable option, record one line with the `pubg-record-decision` skill, and continue.

**R7. Documents change with the code.** A pull request that changes a data schema or the behaviour of a feature updates `docs/data/*` and `docs/product/features.md` in the same pull request. Do not edit a document to make it match code you could not get right; if code and document disagree, say so.

**R8. No AI attribution, ever.** Only the 5 collaborators may appear as contributors. Never add a `Co-authored-by` trailer, a "Generated with" or "Made with" line, a robot emoji byline, or any mention of an AI tool as author, in a commit message, pull request title or body, issue, or comment. Commits are authored by the human whose machine you are on. Never run `git config user.name` or `user.email`. The hooks, CI and the repository rules reject all of the above; do not look for a way around them.

**R9. Git discipline.** Branch from the latest `main` as `type/issue-slug` (for example `feat/12-google-sign-in`). One issue per pull request. Commit subjects are `type(scope): imperative summary`. Never push to `main`, never force push, never use `--no-verify`. The full grammar is `.github/NAMING.md`.

**R10. You do not decide that work is done.** No AI step approves, merges or marks its own work verified. A passing build is not a decision. When the leader asks you to merge a specific pull request, do it (`gh pr merge --squash`); they decided and you are the hands.

**R11. No secrets.** Never commit keys, keystores, tokens, passwords or `local.properties`. `google-services.json` is the only configuration file that is committed: it holds public identifiers, and the Firebase Security Rules are what protect the data.

**R12. Authority is never taken from the client.** Who a user is and what role they hold come from Firebase Authentication and the Security Rules, never from a field the app sends. Do not fake an authenticated user, not even in a test seam. When access is missing or unclear, deny and show a clear message; do not fall back silently.

**R13. Smallest change that does the job.** Reuse what exists before writing new code. Non-trivial logic leaves one runnable check behind, the smallest test that fails if the logic breaks.

**R14. Issues and pull requests are written in Vietnamese.** Title and body, because people read them, not only tools. Fill the issue forms and the pull request template as they are (their headings are Vietnamese). Keep in English: the `type(scope):` prefix of a title, the keywords `Closes`, `Refs`, `Removal-Issue` and `Deviation`, and anything quoted verbatim (code names, paths, commands, error messages). Write plain, correct Vietnamese with accents; do not translate identifiers. CI rejects a pull request whose title or body has no Vietnamese in it; nothing checks issues, so this rule is yours to keep. Commit messages may be English or Vietnamese.

**R15. Write plain, explicit Java, and keep knowledge out of comments.** Follow the `pubg-code` skill for every line you write or change: explicit types, no lambdas, method references, streams or `var`, small methods, comments of one or two lines that say why. Run `sh scripts/check-java-style.sh` before you finish. What you learn about the code or a library that the code cannot say goes in `memory/code/`, not in comments. The style applies to the lines you write or change; do not rewrite old code for it (R3).

## 7. Code conventions (short form)

Details are in `docs/conventions/android-java.md`.

- Code lives in `com.nhom3.pubgapp.feature.<name>.{ui,data,model}`; shared code in `com.nhom3.pubgapp.common`.
- Use ViewBinding, not `findViewById`. Every user visible string is a resource; a feature's strings go in its own `strings_<feature>.xml`.
- No network or disk work on the main thread. Every screen must survive rotation without losing its state.
- Plain, explicit Java (R15): no lambdas, streams or `var`; comments of one or two lines; lasting knowledge in `memory/code/`. Detail in the `pubg-code` skill and `docs/conventions/android-java.md`.
- Identifiers and comments in English. The user interface language is Vietnamese by default.

## 8. Workflow and skills

Issue, then branch, then pull request, then the leader reviews and squash merges. The skills below carry out each step the same way for everyone; their source is `.agents/skills/`.

| Skill | Use it to |
| --- | --- |
| `pubg-new-issue` | Turn an idea into a correctly formed issue |
| `pubg-start-task` | Begin an issue: read it, create the branch, state the scope fence |
| `pubg-code` | Write Java the PUBGApp way: plain, explicit, short comments, notes in `memory/code/` |
| `pubg-sync-branch` | Merge the latest `main` into your branch and resolve conflicts without dropping anyone's code |
| `pubg-open-pr` | Verify, then open the pull request from the template |
| `pubg-review-pr` | Leader only: judge a pull request against its issue on the merged tree |
| `pubg-record-decision` | Append a decision to `memory/decisions.md` |
| `pubg-doc-check` | Find where documents and code disagree |
| `pubg-guide` | Point the person to the right page of `guide/` when they are stuck or something was refused |

**The guide is advice, not law.** `guide/` teaches people how to direct you well. Only the rules in section 6 bind you. When a person is stuck, or a hook, a CI check or a rule refuses something, use `pubg-guide` to point them to the right page in one or two lines. If they choose to skip a step of the guide, say once what it costs and let them; do not treat it as an error and do not repeat the reminder. The exceptions are the things tooling rejects anyway (R8, R9, R11 and the naming, removal and dependency checks): those you never help bypass.

## 9. Language

Talk to people in the language they use (the team writes Vietnamese).

| Written in | What |
| --- | --- |
| Vietnamese | **Issues and pull requests** (title and body, rule R14), `docs/`, `guide/`, `plan/`, `README.md`, comments you post for a person to read, and every message to the person you work with |
| English | Files for AI tools (`AGENTS.md`, `.agents/`, `.claude/`), `memory/`, code identifiers and code comments, and the `type(scope):` prefix of a title |
| Either | Commit messages |

Do not use emoji in any written artifact.
