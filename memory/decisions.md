# Decisions

Append only. Never edit, reorder or delete an entry; see `README.md` in this folder. Newest entries go at the end.

## 2026-09-25 - Android stack: Java, XML Views, API 30 minimum

### Decision
Java 17 with XML Views and ViewBinding. minSdk 30, compileSdk 37, targetSdk 37. Gradle wrapper with Kotlin DSL scripts; versions pinned in `PUBGApp/gradle/libs.versions.toml`. The Gradle daemon runs on JDK 25, fetched automatically through the foojay resolver.

### Context
The course teaches Java and XML layouts. The whole team owns devices or emulators at API 30 or higher (Pixel 5 emulator is API 30).

### Alternatives rejected
| Alternative | Why not |
| --- | --- |
| Kotlin and Jetpack Compose | The course material, exercises and the teacher's expectations are Java and XML |
| minSdk 24 | Reaches older phones nobody on the team uses, at the cost of extra compatibility work |
| Java 11 (wizard default) | AI tools write records, text blocks and switch expressions, which need 17 |

### Impact
`AGENTS.md` section 2, `docs/conventions/android-java.md`, CI uses JDK 25 for Gradle.

### Owner
tahpnart8

## 2026-09-25 - Application id and project name

### Decision
Project name `PUBGApp` (Pre-owned Users' Bargain Grounds). Package and application id `com.nhom3.pubgapp`. The Android project lives in the `PUBGApp/` directory at the repository root.

### Context
The wizard generated `com.example.pubgapp`. Firebase binds to the application id when the app is registered, so the id had to be final before any Firebase setup.

### Alternatives rejected
| Alternative | Why not |
| --- | --- |
| Keep `com.example.pubgapp` | Google Play blocks the `com.example` prefix; renaming after Firebase registration is costly |
| Put the Android project at the repository root | Repository files (docs, memory, workflows) would mix with Gradle files |

### Impact
Java packages under `com.nhom3.pubgapp`; Firebase registration uses this id.

### Owner
tahpnart8

## 2026-09-25 - Backend and offline strategy

### Decision
Firebase is the backend (Authentication with Google sign-in, Cloud Firestore, Cloud Storage). Room over SQLite is the local cache so pages already viewed work without network. These are planned and not yet in the build.

### Context
The teacher requires Firebase, offline use of viewed pages with SQLite, and supporting APIs such as Google sign-in.

### Alternatives rejected
| Alternative | Why not |
| --- | --- |
| A custom REST server | No server budget or skill; Firebase is required anyway |
| Realtime Database | Firestore queries suit listings, orders and search better |

### Impact
`docs/architecture/overview.md`, `docs/data/*`. Nothing is added to the build until a `chore(deps)` issue does it.

### Owner
tahpnart8

## 2026-09-25 - Only the five team members are contributors

### Decision
No AI tool, bot or outside account may appear as author, co-author or contributor. Enforced in seven layers: (1) exactly five collaborators and no GitHub App; (2) rule R8 in `AGENTS.md`; (3) Claude Code settings; (4) local hooks `commit-msg`, `pre-commit`, `pre-push`; (5) the required CI check `policy`, which reads every commit of a pull request through the API and requires each author login to be in `.github/team.txt`; (6) a ruleset on `main` with squash-only merges whose commit message is the pull request title only; (7) a weekly check of the contributors list.

### Context
The team writes most code with AI tools. Their default behaviour adds a `Co-Authored-By` trailer, which makes the tool appear as a contributor on GitHub.

### Alternatives rejected
| Alternative | Why not |
| --- | --- |
| Rely on the tool setting that disables attribution | Claude Code has a reported bug where an empty setting still adds the trailer |
| Rely on client side hooks only | They are skipped by `--no-verify` and on a clone that never ran setup |
| Merge commits instead of squash | A commit body could carry a trailer through the merge |

### Impact
`.githooks/`, `scripts/`, `.github/workflows/policy.yml`, repository settings, `AGENTS.md` R8.

### Owner
tahpnart8

## 2026-09-25 - Workflow: issue, branch, pull request, squash merge

### Decision
Every change starts from an issue built with a form. Branches are `type/issue-slug`. Commit subjects are conventional (`type(scope): summary`) and the pull request title equals the primary commit subject. The leader reviews and squash merges every pull request. Skills under `.agents/skills` carry out each step for everyone.

### Context
Five people and several AI tools would otherwise each invent their own naming and process, and the course asks for evidence of work through branches and merges.

### Alternatives rejected
| Alternative | Why not |
| --- | --- |
| Free-form branches and messages | Inconsistent history, harder review, no machine checks |
| Person's name in the branch | The pull request already shows the author; names in branches add nothing |

### Impact
`.github/NAMING.md`, templates, `.githooks/`, `scripts/lib/policy.sh`.

### Owner
tahpnart8

## 2026-09-25 - One rulebook for every AI tool

### Decision
`AGENTS.md` is the only rulebook. `CLAUDE.md` imports it. There is no `GEMINI.md`. Skills have one source, `.agents/skills`, mirrored into `.claude/skills` by `scripts/sync-ai-skills.sh`, and CI fails when they differ. Serena is not used; the `context7` MCP server is used to look up real API signatures.

### Context
Antigravity reads `AGENTS.md` and `GEMINI.md`, with `GEMINI.md` taking priority, so two files could disagree. Claude Code reads only `CLAUDE.md` and `.claude/`.

### Alternatives rejected
| Alternative | Why not |
| --- | --- |
| Separate files per tool | They drift apart; a weaker model follows whichever it read |
| Symlinks between `.agents` and `.claude` | Unreliable on Windows checkouts |
| Adopt the company repository's OpenSpec process | Far heavier than a course project needs |

### Impact
`AGENTS.md`, `CLAUDE.md`, `.agents/`, `.claude/`, `.mcp.json`.

### Owner
tahpnart8

## 2026-09-25 - Language conventions

### Decision
Files for AI tools, code, commits, issues and pull requests are English. `docs/` and `README.md` are Vietnamese. The user interface is Vietnamese by default.

### Context
English keeps AI instructions short and consistent across models. The team and the course work in Vietnamese.

### Alternatives rejected
| Alternative | Why not |
| --- | --- |
| Everything in Vietnamese | More tokens per session and more variation of meaning between models |
| Everything in English | The team reads `docs/` more easily in Vietnamese |

### Impact
All authors.

### Owner
tahpnart8

### Status
Superseded in part by the entry "Issues and pull requests are written in Vietnamese" below: issues and pull requests are now Vietnamese. The rest of this entry stands.

## 2026-09-25 - Repository settings as code, and a stricter pull request policy

### Decision
The GitHub settings that back the contributor policy live in the repository: `.github/rulesets/*.json` and `scripts/apply-github-settings.sh`, which the leader runs and which prints back what GitHub reports. `main` gets two rulesets: `main-checks` (no deletion, no force push, linear history, `build` and `policy` green and up to date; no bypass for anyone) and `main-review` (pull request, one code owner approval, squash only; the admin role may bypass it inside a pull request, because the leader cannot approve their own). The `policy` check also fails a rename under `PUBGApp/app/src/main` without a removal issue, and a change to a Gradle file in a pull request whose title is not `build(...)` or `chore(...)`; it warns when a file there loses more than 40 lines. A co-author trailer is accepted only for a team member's GitHub noreply address, which is what "Commit suggestion" writes. A `pubg-sync-branch` skill resolves merge conflicts by keeping both sides.

### Context
The review before the first merge found that settings applied by hand are invisible and unrepeatable, that one ruleset with an admin bypass would also let the leader merge with red CI, that the byline pattern refused innocent messages such as "update the claude code settings", and that merge conflict resolution is where AI tools most often drop a teammate's code.

### Alternatives rejected
| Alternative | Why not |
| --- | --- |
| Configure the settings by hand in the web UI | Not reviewable, not repeatable, easy to drift |
| One ruleset with an admin bypass | The bypass would skip the status checks too |
| Reject every co-author trailer | Blocks GitHub's own review suggestions between team members |
| Metadata rulesets (commit message and branch name patterns) | Redundant with the empty squash body and the `policy` check, and may not be offered for a personal repository |

### Impact
`.github/rulesets/`, `scripts/apply-github-settings.sh`, `scripts/ci-policy.sh`, `scripts/lib/policy.sh`, `AGENTS.md` R4 and R5, `.agents/skills/pubg-sync-branch`.

### Owner
tahpnart8

## 2026-09-25 - Product images are stored in Cloud Storage on the Blaze plan

### Decision
Product and avatar images will be stored in Cloud Storage for Firebase. The Firebase project therefore runs on the Blaze plan, with one billing account owned by the leader, a low budget alert, and the bucket placed in an Always Free region (`us-central1`, `us-east1` or `us-west1`).

### Context
From 2026-02-03 Cloud Storage for Firebase requires the Blaze plan; on the Spark plan every Storage call fails with 402 or 403 (Firebase FAQ on Storage changes). The team had to choose between paying attention to billing and avoiding Storage.

### Alternatives rejected
| Alternative | Why not |
| --- | --- |
| Compressed images as Base64 inside Firestore | Needs no card, but works against the 1 MiB document limit and is not how a real product stores images; the leader preferred the standard service |
| Base64 first, move to Storage later | Costs a second round of image code |

### Impact
`docs/workflow/firebase-setup.md` (steps 1 and 5). No app code exists yet; the image feature is not designed until the requirements are loaded.

### Owner
tahpnart8

## 2026-09-25 - A flexible human guide in guide/, and a skill that points to it

### Decision
A `guide/` folder, in Vietnamese and addressed to the person coding, holds the prompts and practices for working with AI: starting a session, the explore, plan, implement and review loop with a checkpoint for the person after each stage, ending a task (pull request, cross-check by a second AI, asking the leader), a table of common situations, and a page on what may be skipped and what may not. The `pubg-guide` skill and the `/guide` workflow point a person to the right page when they are stuck or something is refused. The guide is advice: only what tooling rejects anyway (AI attribution, pushing to `main`, secrets, naming, removal and dependency checks) is treated as a boundary, and a person may skip any other step, noting a `Deviation:` line in the pull request summary. Cross-checks by a second AI happen on the person's machine and reach GitHub only as the person's own comment; no review bot or GitHub App is installed.

### Context
The team will write most code through AI tools and needs the same, controllable way of directing them. The repository rules in `AGENTS.md` bind tools; people needed something that shows them how to direct the tools without turning into another set of hard rules.

### Alternatives rejected
| Alternative | Why not |
| --- | --- |
| Put the prompts in `AGENTS.md` | It is loaded into every AI session, so prompts written for people cost tokens each time and blur who the file is addressed to |
| Make the guide mandatory, with CI checks on each step | The leader asked for a flexible guide; most steps cannot be checked by a machine and forcing them produces box ticking |
| Ask a review bot such as Copilot or Codex to cross-check on GitHub | It would appear as a contributor, which decision "Only the five team members are contributors" rules out |

### Impact
`guide/`, `.agents/skills/pubg-guide`, `.agents/workflows/guide.md` and its mirror in `.claude/skills`, `AGENTS.md` sections 4, 8 and 9, links from `README.md`, `docs/`, `.github/CONTRIBUTING.md`.

### Owner
tahpnart8

## 2026-09-25 - Issues and pull requests are written in Vietnamese

### Decision
The title and the body of every issue and every pull request are written in Vietnamese. What stays English: the `type(scope):` prefix of a title, the keywords `Closes`, `Refs`, `Removal-Issue` and `Deviation`, and anything quoted verbatim (code names, paths, commands, error messages). The issue forms and the pull request template are in Vietnamese. Commit messages may be English or Vietnamese: the subject rule now accepts both, with a limit of 100 bytes instead of 72 characters, because dash counts bytes and bash counts characters. CI rejects a pull request whose title or body contains no Vietnamese accented letters; nothing checks issues, so that half of the rule rests on the person and on the `pubg-new-issue` skill. Rule R14 in `AGENTS.md` carries it for AI tools.

### Context
People read issues and pull requests, not only tools; the leader asked for Vietnamese. The earlier language decision put issues and pull requests in English for the benefit of AI models.

### Alternatives rejected
| Alternative | Why not |
| --- | --- |
| Keep English and translate on request | Puts the burden on the reader every time |
| Require Vietnamese commit messages too | Not asked for; the pull request title is what reaches `main` after a squash merge |
| Detect the language with a model or a dictionary in CI | Heavy for a course project; counting accented letters catches text written in English, which is the failure that matters, and it is cheap and deterministic |
| Also check issues in CI | Would need a workflow with write access to comment or close issues; not worth a bot for a five person team |

### Impact
`scripts/lib/policy.sh`, `scripts/ci-policy.sh` and its tests, `.github/PULL_REQUEST_TEMPLATE.md`, `.github/ISSUE_TEMPLATE/`, `.github/NAMING.md`, `.agents/skills` (`pubg-new-issue`, `pubg-open-pr`, `pubg-review-pr`, `pubg-start-task`), `.claude/agents`, `AGENTS.md` R14 and section 9, `guide/`, `scripts/create-labels.sh`.

### Owner
tahpnart8

## 2026-09-25 - Plain explicit Java, and code knowledge kept in memory/code/

### Decision
Java is written plainly: explicit types, one statement per line, anonymous classes or named methods for listeners, `for` loops, small methods and classes, and comments of one or two lines that say why. No lambdas, method references, streams, `var`, records, text blocks or pattern `instanceof`, although the toolchain stays Java 17. Anything a future reader needs that the code cannot say (a library trap, why a query has an odd condition, an approach that failed) is written in `memory/code/<area>.md` and not in comments. The `pubg-code` skill carries the rules for AI tools, rule R15 in `AGENTS.md` binds them, `docs/conventions/android-java.md` states them for people, and `scripts/check-java-style.sh` reports violations on the lines a change adds. The checker is advisory: skills read its output, CI only runs its tests. The style applies to lines written or changed, never to a rewrite of old code.

### Context
The leader wants code a student can read on the first pass and found AI-written code wordy, lambda-heavy and heavily commented. Comments go stale and cannot be searched by area, while a small memory file per area can be kept true.

### Alternatives rejected
| Alternative | Why not |
| --- | --- |
| Fail CI on any style finding | A pattern checker cannot judge names or method length and would flag the wizard-generated `MainActivity`; the leader asked for guidance that can be relaxed |
| Store code knowledge in `decisions.md` | It is append only and holds choices between alternatives; code knowledge must be corrected and deleted when it stops being true |
| One file for all code notes | Five people would edit it at once; one file per area avoids that |
| Lower the language level to Java 8 to forbid lambdas | Android still builds Java 17 sources; the style is a choice of the team and the toolchain does not need to enforce it |

### Impact
`.agents/skills/pubg-code`, `.agents/workflows/code.md` and their mirror, `.agents/rules/android-java.md`, `AGENTS.md` (R15, sections 3, 4, 7, 8), `memory/code/`, `memory/README.md`, `scripts/check-java-style.sh` and its test (run by `build.yml`), `pubg-start-task`, `pubg-open-pr`, `pubg-review-pr` and `pubg-guide`, `docs/conventions/android-java.md`, `guide/`.

### Owner
tahpnart8
