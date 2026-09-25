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
