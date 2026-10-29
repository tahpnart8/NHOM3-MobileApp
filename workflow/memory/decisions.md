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

## 2026-09-25 - Media files, images and videos, stay in Cloud Storage within the free tier

### Decision
All media files, images and videos, are stored in Cloud Storage for Firebase, as decided for images in the entry "Product images are stored in Cloud Storage on the Blaze plan". The leader confirmed this after raising a $0 budget and accepts attaching a card to the Blaze plan. The project stays inside the Cloud Storage Always Free tier: 5 GB-months of storage in `us-central1`, `us-east1` or `us-west1`, 5,000 Class A and 50,000 Class B operations, and 100 GB of egress from North America each month. The bucket is created in one of those three regions. Storage security rules cap the size and type of every file and require sign-in. A low budget alert is set; it warns and does not cap spending.

### Context
The leader asked whether media under 5 GB is free. It is, but only in those regions and only with a billing account attached (Google Cloud free tier page, checked 2026-09-25). Video was not covered by the earlier entry.

### Alternatives rejected
| Alternative | Why not |
| --- | --- |
| Compressed images as Base64 in Firestore | Avoids the card, but the 1 MiB document limit makes it unusable for video and awkward for images; offered on 2026-09-25 and declined by the leader |
| An outside media host | Adds a second service and an SDK for no gain within the free tier |

### Impact
`docs/workflow/firebase-setup.md` section 1. When media features are designed: size and duration caps, and the Storage rules in `firebase/storage.rules`. Anyone can register an account in the app and the API key is public in the repository, so the rules are the only protection against filling the 5 GB.

### Owner
tahpnart8

## 2026-09-27 - Java packages are split by layer, not by feature

### Decision
Code under `PUBGApp/app/src/main/java/com/nhom3/pubgapp/` is organized by architectural layer: `model` (domain classes), `model.enums` (enum types), `data` (repositories), `data.local` (Room entities and DAOs), `util` (utility classes), `viewmodel` (ViewModel classes). This matches the 85 class names already fixed in the class diagram and ERD produced for the course submission.

### Context
`AGENTS.md` section 7 and `docs/conventions/android-java.md` said code lives in `com.nhom3.pubgapp.feature.<name>.{ui,data,model}`, one package per feature. The class diagram was drawn before code existed and grouped by layer instead, because that is how a small student team keeps track of which class does what kind of work. Starting code before resolving the mismatch would leave two contradicting sources of truth.

### Alternatives rejected
| Alternative | Why not |
| --- | --- |
| Keep feature based packages, redraw the class diagram | The diagram is already finished and cross checked against the 78 in scope rows of the functions description; redrawing it wastes the work already done |
| Mixed: feature packages for UI, layer packages for everything else | Two different rules in one codebase, harder for a student team to remember than one rule |

### Impact
`AGENTS.md` section 7, `docs/conventions/android-java.md`. No code exists yet under `PUBGApp/app/src/main/java/`, so nothing is renamed.

### Owner
tahpnart8

## 2026-09-29 - A pull request that changes no code takes a short path

### Decision
A pull request whose diff touches no file under `PUBGApp/` is a document pull request and is treated differently from a code one. It uses the four heading template `.github/PULL_REQUEST_TEMPLATE/tai-lieu.md`. The `build` workflow still runs and still reports the required `build` context, but skips the JDK, Gradle and the Android build. `pubg-open-pr` and `pubg-review-pr` both start by deciding which path applies, and neither runs Gradle on the short path. Two merge guards were turned off repository wide at the same time: `strict_required_status_checks_policy` in the `main-checks` ruleset and `dismiss_stale_reviews_on_push` in `main-review`. The requirement of one approving review from a code owner stays.

### Context
Three phases of the course project are document work: analysis, diagrams, interface mockups, 24 issues open at once. Every one of them was paying the cost of a workflow built for Java. The long template has 8 sections of which 5 are meaningless for a document, so authors left it blank and the one line CI actually needs, `Closes #N`, got lost; that is what made pull request 43 fail. The `build` workflow ran the whole Android toolchain on Markdown only pull requests: 62 and 61 seconds measured on the two most recent ones. And because a branch had to be up to date with `main`, every merge invalidated every other open pull request, so each one needed an Update branch, a CI re-run and, because that is a push, a second review.

### Alternatives rejected
| Alternative | Why not |
| --- | --- |
| `paths-ignore` on the `build` workflow | `build` is a required status check. A workflow that does not run never reports, so the check stays pending and the pull request becomes unmergeable instead of fast |
| Drop the `Closes #N` requirement from `ci-policy.sh` | It is what closes the issue automatically and what shows the marker a traceable line from issue to merge. The problem was the template hiding it, not the rule |
| A separate ruleset that relaxes the rules only for document branches | A ruleset conditions on the base branch, which is always `main`, never on the changed paths or the head branch. Not expressible |
| Remove the approving review for document pull requests | Cannot be scoped to documents either, and the review is the evidence the marker looks for |
| One pull request per round instead of per issue | Fewer reviews, but it blurs the one issue one pull request trail and one document needing a fix would hold up the rest. Rejected by the leader on 2026-09-29 |

### Impact
`.github/PULL_REQUEST_TEMPLATE/tai-lieu.md` (new), `.github/workflows/build.yml`, `.github/rulesets/main-checks.json`, `.github/rulesets/main-review.json`, `.github/NAMING.md`, `AGENTS.md` section 8, `.agents/skills/pubg-open-pr/`, `.agents/skills/pubg-review-pr/` and the `.claude/skills/` mirror, `workflow/scripts/apply-github-settings.sh` comments, `workflow/scripts/tests/policy-test.sh` (3 tests added, 82 total), `workflow/docs/workflow/nop-tai-lieu.md`. What a code pull request must do did not change, except that its branch no longer has to be up to date with `main`: the reviewer builds the merged tree, and `build` runs on `main` after every merge.

### Owner
tahpnart8

## 2026-09-29 - Hai Long is a second code owner for documents

### Decision
`.github/CODEOWNERS` gains a scoped line: `/workflow/docs/ @tahpnart8 @Contest451`. Everything else keeps the leader as the sole owner. Because a pull request whose changed paths match more than one owner needs only one of them to approve (GitHub's rule for multiple owners on the same CODEOWNERS line), a pull request that touches only `workflow/docs/` can be approved and merged by either the leader or Nguyen Le Hai Long. No collaborator role changed: Hai Long already held `write`, which is what merging a satisfied pull request needs: the change is only about who counts as a required reviewer.

### Context
24 document issues are open at once for this phase (analysis, diagrams, interface mockups), and every one of them needed the leader specifically to approve, which is the same bottleneck the short document template and the faster `build` workflow (decision above) were built to relieve. Hai Long had already reviewed cleanly as an author on pull request 43. The leader asked directly for this change.

### Alternatives rejected
| Alternative | Why not |
| --- | --- |
| Make Hai Long a repository admin | Far more than the ask; admin also bypasses `main-checks`, which nobody but the leader should |
| Raise `required_approving_review_count` to 2 for everyone | Slower, not faster, and applies to code pull requests too |
| A path scoped ruleset instead of CODEOWNERS | GitHub rulesets condition on the base branch, not on which paths changed; not expressible |

### Impact
`.github/CODEOWNERS`, `workflow/docs/workflow/cau-hinh-github.md`, `workflow/docs/workflow/huong-dan-lam-viec.md`, `workflow/memory/people.md`. No change to collaborator roles or to either ruleset.

### Owner
tahpnart8

## 2026-10-11 - One numbering for every function, and a "Phát triển sau" scope mark

### Decision
`workflow/docs/product/dac-ta-chuc-nang.md` is renumbered from 1 with no gaps or duplicates: the 92 functions of the team's original sheet plus 10 functions that the interface mockups need, 102 rows in all. Every row carries a scope: 88 are "Đợt này" and 14 are "Phát triển sau" (rows 83, 85, 86, 92 to 102). A "Phát triển sau" function is kept as a future direction only: it is not in `features.md`, has no class of its own on the class diagram, is not drawn on the function diagram, and is not coded in the coming phase, even where a mockup draws part of it. Every other document refers to a function by this STT. A mapping table from the old sheet row to the new STT sits at the end of the function table, for reading older documents.

### Context
The function table on `main` had been renumbered by hand and repeated two rows, while the class diagram, the function diagram and the interface documents each used a different numbering. A tool that reads "row 75" could land on the wrong function in each document.

### Alternatives rejected
| Alternative | Why not |
| --- | --- |
| Keep the original sheet row numbers (3 to 94) | They start at 3 and leave no room for the functions the mockups added |
| Delete the out of scope functions from the table | The leader wants them kept as the direction for later work |

### Impact
`workflow/docs/product/dac-ta-chuc-nang.md`, the three diagrams and their READMEs, `workflow/docs/analysis/du-kien-lop.md`. The interface documents and `features.md` move to the new STT in later pull requests.

### Owner
tahpnart8

## 2026-10-11 - The model is extended to match the interface mockups

### Decision
Where a mockup shows something the model lacked, the model grows to match, except where the mockup draws a "Phát triển sau" function. Added: profile counters (sold, followers, deals, completion rate), notification settings, change password, deactivate account, private identity data, save a listing as draft, seller hides or shows a listing, a reference price range, partner activity status, a notification list, three review criteria (accuracy, attitude, commitment), preset reasons for reports and complaints, and a `RECEIVED` step in a deal. Not added, because the function is "Phát triển sau": admin dashboard counters, a manual release request, an "abnormal deal" filter, configurable lock durations, maintenance broadcasts. Three smaller rules: a seller hiding a listing reuses `ListingStatus.HIDDEN`; there is no per conversation unread dot; a pending offer cannot be cancelled, only accepted or rejected.

### Context
The mockups were drawn after the class diagram and the ERD, so the two had drifted apart. The leader chose to extend the model rather than cut the mockups.

### Alternatives rejected
| Alternative | Why not |
| --- | --- |
| Cut the mockups down to the old model | Loses screens the team already designed and the teacher will see |
| Add everything the mockups show, including the future admin parts | Pulls "Phát triển sau" work into the coming phase |

### Impact
Class diagram (89 classes: model 21, enums 21, data 14, data.local 12, util 7, viewmodel 14), ERD (22 Firestore collections, 1 embedded array, 6 Room tables), `workflow/docs/data/firestore-schema.md`, `workflow/docs/data/room-schema.md`.

### Owner
tahpnart8

## 2026-10-11 - Accounts, identity data and the end of a deal

### Decision
- Sign in is by email and password or by Google only. The phone number is a profile field, not a sign in method; no SMS code anywhere.
- Identity data (national ID number, date of birth, gender, permanent address) lives in `users/{uid}/private/identity`, class `UserIdentity`, readable only by the owner and admins.
- Deleting an account deactivates it: `UserStatus.DELETED`, profile and listings hidden, sign in removed, deal history and reviews kept for the other party. Not allowed while a deal is still open.
- A deal goes `SHIPPED` to `RECEIVED` when the buyer confirms receipt, or automatically when the countdown from `autoReleaseAt` runs out; the payment is released at that moment. It goes `RECEIVED` to `COMPLETED` when both sides have reviewed, or 3 days (`Deal.AUTO_COMPLETE_DAYS`) after `receivedAt`. Shipping needs carrier, tracking code and a photo of the waybill.
- `ItemCondition` has five values: `NEW`, `LIKE_NEW`, `USED`, `REPAIRED`, `DEFECTIVE`.
- An admin is a user with a document at `admins/{uid}`; all admins have the same rights.

### Context
The mockups and the original function table disagreed on these points, and each one changes a field or a status that code will depend on.

### Alternatives rejected
| Alternative | Why not |
| --- | --- |
| Phone number sign in | Needs SMS, which costs money on Firebase; email and Google already meet the course requirement |
| Identity fields on the public `users` document | Anyone signed in could read a national ID number |
| Hard delete of the account | Breaks the other party's deal history and reviews |
| Release the payment only at `COMPLETED` | The seller would wait for reviews that the buyer may never write |

### Impact
`workflow/docs/data/firestore-schema.md`, class diagram pages 1 and 3, ERD pages 1 and 3, function table notes.

### Owner
tahpnart8

## 2026-10-12 - Remaining rules for deals, offers, complaints, listings and accounts

### Decision
Rules that the function table and the mockups left open, settled so the state machines in `workflow/docs/architecture/trang-thai.md` are complete:
- The shipping countdown is 7 days: `Deal.AUTO_RELEASE_DAYS = 7`, so `autoReleaseAt = shippedAt + 7 days`.
- A meet-up deal (`DeliveryMethod.MEET_UP`) has no `SHIPPED` step and no countdown: the buyer confirms receipt straight from `CONFIRMED`, and the payment is released then.
- A complaint can be opened only while the deal is `SHIPPED`, or `CONFIRMED` for a meet-up, and only one at a time. Outcomes: `REFUND`, `RETURN_ITEM` and `CANCEL_DEAL` cancel the deal and refund a held payment; `REJECT` moves the deal to `RECEIVED` and releases the payment; `REQUEST_EVIDENCE` keeps the deal `DISPUTED`. Withdrawing a complaint returns the deal to its previous status with the original `autoReleaseAt`.
- Accepting an offer reserves the listing and rejects every other pending offer on it. A cancelled deal puts the listing back to `AVAILABLE`; a completed deal marks it `SOLD`.
- Editing the terms of a deal clears both confirmations.
- Profile counters: `dealCount` grows when a deal is created, `completedDealCount` and the seller's `soldCount` when it completes.
- Seller verification is a badge, not a requirement to post: `User.canSell()` needs an active, unlocked account with a verified email.
- An expired temporary lock is lifted by the app at the next sign-in; Security Rules allow that write only after `lockedUntil` has passed.
- A seller can mark a listing sold by hand when it was sold outside the app, and reopen it.

### Context
Writing the state machines and data dictionary as text showed transitions with no rule behind them. None of these changes a collection, a field or a dependency, so they are recorded here instead of being asked first (AGENTS.md R6).

### Alternatives rejected
| Alternative | Why not |
| --- | --- |
| Countdown of 3 days, like auto completion | Starts at shipping, not delivery; delivery inside Vietnam often takes several days |
| Seller verification required to post | Blocks every new user, including giveaways; the mockups only show a reminder banner |
| Withdrawing a complaint restarts the countdown | Lets a buyer delay payment again and again |

### Impact
`workflow/docs/architecture/trang-thai.md`, `luong-xu-ly.md`, `dac-ta-lop/`, `workflow/docs/data/firestore-schema.md`. Known gap: no method yet lets a complainant add evidence after `REQUEST_EVIDENCE`; it is listed under risks in `workflow/docs/architecture/overview.md`.

### Owner
tahpnart8

## 2026-10-12 - Free Spark plan: images as Base64 in Firestore, no video

### Decision
The Firebase project stays on the free Spark plan with no billing card. Cloud Storage and Cloud Functions are not used. Images are compressed on the phone and stored as Base64 strings in Firestore:

- **Full images** (long edge 1024 px, at most 300 KB of JPEG) each go in their own document of the new collection `images` (class `MediaImage`, enum `ImagePurpose`).
- **Small thumbnails** (320 px for a listing cover, 256 px for an avatar, at most 20 KB) sit inline in `listings.coverThumb`, `deals.itemThumb` and `users.avatarBase64`. Lists and the Room cache therefore never load full images.

Fields that held Storage URLs now hold image ids: `Listing.imageIds`, `ChatMessage.imageId`, `Deal.trackingImageId` and `Complaint.evidenceImageIds`.

Sending video in chat (function STT 45) moves to "Phát triển sau", and `MessageType.VIDEO` is removed. That leaves 87 functions in "Đợt này" and 15 in "Phát triển sau".

This supersedes three earlier entries:

- 2026-09-25 "Product images are stored in Cloud Storage on the Blaze plan".
- 2026-09-25 "Media files, images and videos, stay in Cloud Storage within the free tier".
- The mention of Cloud Storage in "Backend and offline strategy".

### Context
On 2026-10-12 the leader confirmed that the project must cost nothing and that no card will be attached. Cloud Storage has required the Blaze plan since 2026-02-03. Three options were offered: Blaze with a card, Base64 in Firestore, or an outside image host. The leader chose Base64 in Firestore.

### Alternatives rejected
| Alternative | Why not |
| --- | --- |
| Blaze plan with a card, usage kept in the free tier | The leader will not attach a card |
| Outside image host with an unsigned upload preset | Adds a second service and SDK, and has no Security Rules: anyone who reads the preset from the public repository can upload |
| Full images inside the listing document | The 1 MiB document limit allows about two images, and every list page would download them |

### Impact
- Changed: the class diagram (92 classes), the ERD (23 collections), `workflow/docs/data/*`, `workflow/docs/architecture/*`, `workflow/docs/workflow/firebase-setup.md` and the mockup notes for chat.
- The Spark quotas (stored data, reads and writes per day) become the limit to watch.

### Owner
tahpnart8

## 2026-10-12 - Navigation, UI packages, module owners and code review

### Decision
- **Navigation:**
  - Three Activities. `AuthActivity` holds sign in, sign up and password reset. `MainActivity` holds a bottom bar with five tabs: Trang chủ, Tìm kiếm, Đăng tin, Tin nhắn and Cá nhân. `AdminActivity` is opened from the personal page when the user is an admin.
  - Every screen is a Fragment, using the Navigation component, with one nested graph per module.
  - Cross-module links go only through global actions in `nav_main.xml`.
- **UI packages:** the layer packages of 2026-09-27 gain `ui.<module>`: `ui.account`, `ui.notification`, `ui.listing`, `ui.trade`, `ui.profile`, `ui.trust`, `ui.admin`, `ui.admin.complaint`, `ui.common` and `ui.main`.
- **Module owners:**
  - A: Nguyen Thuy Ngan.
  - B: Tran Anh Tu.
  - C: Nguyen Le Hai Long.
  - D: Nguyen Hoang Phuc.
  - E: Tran Duc Phat.

  Each person owns a module from data to screens, matching the mockup group they drew. `workflow/plan/so-huu.md` maps who may edit which file and which repository method.
- **Shared classes are pre-built:** in phase 1 the leader creates every class with its full signature. Methods not yet written fail with `DataCallback.NOT_READY`. Members only replace the bodies of the methods assigned to them.
- **Code review:** Hai Long becomes a code owner of `PUBGApp/` alongside the leader, except for Gradle files, `google-services.json`, `firebase/` and `AndroidManifest.xml`. Either of them may approve and merge a code pull request they did not author.
- **Issues:** each issue is one vertical slice of half a day to one day, with an exact list of the files it may touch.
- **Dates:** code freeze on 2026-10-26, submission on 2026-10-30.
- **Sample data:** a debug-only button writes it through the repositories, with document ids that start with `demo_`.

### Context
Thirty-odd pull requests in ten days would all wait on the leader. Five people working with AI tools will also collide on shared files unless ownership is decided in advance. Navigation and the UI packages were the last open questions blocking interface code.

### Alternatives rejected
| Alternative | Why not |
| --- | --- |
| Many Activities, one per screen | Hard to keep one bottom bar and the state across tabs |
| Split the work by layer (data, UI) | People would wait on each other for every screen |
| The leader alone reviews code | The leader becomes the bottleneck in a two-week build |

### Impact
`AGENTS.md`, `.github/CODEOWNERS`, `workflow/plan/`, `workflow/docs/conventions/android-java.md`, `workflow/docs/architecture/overview.md`, `workflow/memory/people.md` and `workflow/memory/preferences.md`.

### Owner
tahpnart8

## 2026-10-12 - Data and behaviour fixes from the second documentation audit

### Decision
- **No cross-user counters:**
  - Removed: `User.soldCount`, `followerCount`, `dealCount`, `completedDealCount`, `reportCount` and `Category.listingCount`.
  - Firestore aggregation queries count them when a screen needs them. `UserRepository.getStats` returns the new value class `ProfileStats`.
  - Only the rating stays stored, as `ratingSum` and `ratingCount`, incremented in the same batch as the review.
- **One owner per side effect:**
  - The repository that changes a state writes the notification for it. ViewModels never create notifications.
  - `DealRepository.completeDeal` is the only place where a deal becomes COMPLETED.
- **Uniqueness through document ids:**
  - A review is `reviews/{dealId}_{reviewerId}`.
  - A complaint is `complaints/{dealId}`. A deal therefore has at most one complaint ever, and withdrawing it is final.
  - A complaint can be opened only before `autoReleaseAt`.
- **Deals that stall:**
  - A deal still in PENDING_CONFIRM after 3 days is cancelled by the automatic sweep (`DealRepository.expirePendingDeals`).
  - When a listing leaves AVAILABLE for any reason other than an accepted offer, its pending offers are rejected in the same batch.
  - Only DRAFT listings can be deleted. Other listings are hidden.
- **Accounts:**
  - `User.lockReason` is added.
  - A warning of level 3 or more locks the account permanently, and unlocking does not reset `warningCount`.
  - `User.canSell` takes the email-verified flag.
  - Deleting an account requires signing in again first.
  - A first Google sign-in goes through a profile completion step.
- **Firestore mapping rules:**
  - `BaseDocument.id` uses `@DocumentId` and is not stored.
  - Computed `isX()` and `getX()` methods use `@Exclude`.
  - Booleans named `isX` use `@PropertyName`.
  - `createdAt`, `updatedAt` and `shippedAt` use server time.
- **Paging:**
  - Paging uses a cursor on `createdAt`, not page numbers.
  - Sorting by price is done on the server.
  - Sorting by seller rating is done on the device, within the loaded page.
- **Security Rules** are specified in `workflow/docs/data/security-rules.md` before coding starts.

### Context
The second audit (2026-10-12) read the architecture and data documents as the engineers who will code from them would. It found five problems:

- counters that any client could forge;
- side effects with two owners;
- states that could wait forever;
- methods that the flows needed but no class had;
- Firestore mapping traps that silently store wrong field names.

### Alternatives rejected
| Alternative | Why not |
| --- | --- |
| Keep the counters and guard each one with a Security Rule | Needs one rule per counter and per cross-user write, which is easy to get wrong; aggregation queries are cheap at this scale |
| Leave timeouts to an admin override | The override (STT 85) is "Phát triển sau" |

### Impact
The class diagram, the ERD, `workflow/docs/architecture/*`, `workflow/docs/data/*` and `workflow/plan/phase-2-tinh-nang.md`.

### Owner
tahpnart8

## 2026-10-22 - Guest browsing, verification as a trading gate, admin accounts in the app

### Decision
- **Guests** (not signed in) may read exactly three things: the home feed, listing search and a listing's detail page. Security Rules therefore make `listings` (AVAILABLE, RESERVED, SOLD), `categories` and `images` world readable; `users` still needs `signedIn()`, so the seller block of a listing is hidden from guests. Every other action sends the guest to the sign in screen.
- **Seller verification is a gate, not a badge.** An account posts a listing, sends an offer or creates a deal only when `User.verificationStatus` is `APPROVED`. The model gets `User.canBuy(boolean)` beside `User.canSell(boolean)`, with the same condition; Rules add `verified()` to the create rules of `listings`, `offers` and `deals`. An unverified account still browses, searches, chats, saves and follows.
- **Admin accounts are managed inside the app** (UC42, row 105). `AdminRepository` gets `listAdmins`, `findUserByEmail`, `grantAdmin`, `revokeAdmin`; `AdminUserViewModel` gets `loadAdmins`, `grantAdmin`, `revokeAdmin`; screen E07 `AdminAccountFragment` is added with no mockup yet. Rules let an admin create and delete `admins/{uid}` for someone else, never for themselves. The first admin document is still made by hand in the Firebase Console.
- **Admin sign in (UC43, row 106) gets no screen of its own.** An admin signs in on A01 like everyone else and opens the admin area from the button on D01.
- **Payment is simulated inside Firestore.** No bank, no payment gateway: a `Payment` document held at HELD and moved to RELEASED. Feature F-10 is renamed from "ký quỹ mô phỏng" accordingly.

### Context
On 2026-10-22 the team redrew the use case model (43 use cases) and the leader settled 14 points; four of them (Q4, Q7, Q9, Q10) change system behaviour, so the architecture, data and interface documents had to follow.

### Alternatives rejected
| Alternative | Why not |
| --- | --- |
| Let guests read `users` too, so the seller block works | Decision Q7 keeps profiles behind sign in; the listing page hides the block instead |
| Rename `canSell` to one method for both sides | Six documents reference `canSell`; two named methods keep each call site readable and let the two conditions diverge later |
| A separate admin sign in screen for UC43 | UC43's flow is the ordinary sign in; a second screen would duplicate A01 and invite a client side role claim |
| Keep `admins/{uid}` write only from the Firebase Console | UC42 is in scope this round, and the console is not available to the admin using the app |

### Impact
`workflow/docs/architecture/trang-thai.md`, `workflow/docs/data/security-rules.md`, `workflow/docs/product/features.md`, every `workflow/docs/ui/` README, the generated `dac-ta-lop/`, `firestore-schema.md` and `dac-ta-chuc-nang.md`, and the sources `local/diagram-tools/model.py`, `erd_model.py`, `data_text.py`, `tech_text.py`, `tech_text_v3.py`. Class diagram pages 1, 5 and 7 re-rendered.

### Owner
tahpnart8

## 2026-10-22 - The 31 open points the leader settled after the use case rewrite

### Decision
Data shape:
- `sellerVerifications` gains `nationalId` (the number the user types in the request, kept beside the copy in `users/{uid}/private/identity`), `imageIds` (two images: the document and a portrait holding it) and `resubmitCount`. A rejected request may be resent 3 times (`SellerVerification.MAX_RESUBMIT`); after that the user must contact an admin.
- `payments` gains `method`, a new enum `PaymentMethod` with `CARD`, `E_WALLET`, `BANK_TRANSFER`. It is shown on the receipt only; the payment itself stays simulated.
- `deals` gains `receivedImageId`: the buyer must attach one photo when confirming receipt. An automatic receipt at the end of the countdown leaves it empty.
- `ImagePurpose` gains `VERIFICATION` and `RECEIVED`.
- No price range on an offer: `minOfferPrice` stays the only limit.

Behaviour:
- Password: at least 8 characters, letters and digits. No "user name" sign in: email or Google only.
- Phone number is editable, the national ID number is not. A Google account has no password, so the change password section is hidden for it.
- Signing out goes to the home feed in guest mode, not to the sign in screen.
- An unverified account still opens B04 and B05 and saves drafts; only the Publish button is blocked. Cancelling a deal and filing a complaint never require verification.
- Warnings keep the three tiers of `trang-thai.md`, not "three warnings then locked".
- An admin may hide one listing, remove one listing (`AdminRepository.removeListing`, new) and remove all listings of an account. The national ID number is shown only inside a verification request, never on the general profile.
- Reporting a message stays. Keyword filtering does not run on review comments. Only a draft listing can be deleted. "Cảnh cáo" is not a complaint decision.
- Display names of statuses come from `trang-thai.md`; the whole interface says "xác thực tài khoản", not "xác minh người bán".
- New screens, no mockup yet: C07 `DealPaymentFragment` (payment) and E07 `AdminAccountFragment` (admin accounts), making 33 screen codes. B02 gains a "Người dùng" tab for UC15, UC14 gains the area, radius and four sort orders, C04 gains the deal type and role filters, UC39 gains sub categories and condition fields.

### Context
The use case rewrite of 2026-10-22 left 31 places where the specification, the mockups and the system documents disagreed. The leader answered all of them in one pass.

### Alternatives rejected
| Alternative | Why not |
| --- | --- |
| Keep the national ID only in `users/{uid}/private/identity` | The admin must compare what the user typed in the request with the photos; the profile copy can be edited independently |
| Drop the photo at receipt instead of adding a field | The leader kept it: it is the buyer's own evidence if a complaint follows |
| Give an unverified account no access to the posting screens at all | They may prepare and save drafts while waiting for approval |

### Impact
Sources `local/diagram-tools/model.py`, `erd_model.py`, `functions.py`, `data_text.py`, `tech_text*.py`, `build_tech_docs.py`; all five use case files and all five interface READMEs plus their index; `trang-thai.md`, `security-rules.md`. Class diagram pages 1, 2, 3, 5 and 7 re-rendered and a `PaymentMethod` box added; the ERD rebuilt from `erd_model.py`.

### Owner
tahpnart8

## 2026-10-22 - Colour values read out of the mockups

### Decision
The colour table of `workflow/docs/conventions/giao-dien.md` is filled from the 30 mockup PNGs, counted pixel by pixel, not from the Figma file (which this session did not have):

- `colorPrimary` and `colorSuccess` `#1F6F54`, `colorOnPrimary` and `colorSurface` `#FFFFFF`, `colorOnSurface` `#1E1E1C`, `colorOnSurfaceVariant` `#6E6B62`, `colorBackground` `#FAF8F4`, `colorOutline` `#E7E2D6`, `colorError` `#B2412F`, `colorWarning` `#9E5223`, `colorInfo` `#27628C`.
- Chip fills, declared directly in `colors.xml`: `#E4F0EA` under green text, `#F5E4D8` under warning text; an error label is filled with `colorError` and white text, because the mockups have no pale red.
- `colorSuccess` is deliberately the same value as `colorPrimary`: the mockups contain one green.
- `colorInfo` is invented. The mockups use three meaningful colours only (green, orange, brick red) and the design system needs a fourth for CONFIRMED and SHIPPED.
- `colorWarning` is the mockup orange `#C0632B` darkened to `#9E5223`, because `#C0632B` on the beige chip is 3.35:1, under the 4.5 that WCAG AA asks of small text. Every other pair in the table was checked and passes.

### Context
Step 4 of the 2026-10-22 plan. The token names were already fixed; only the values were missing, and layouts cannot be written without them.

### Alternatives rejected
| Alternative | Why not |
| --- | --- |
| Wait for the Figma file | It blocks every layout; the mockups are exports of that same file, so the values match unless Figma says otherwise, and the document says Figma wins |
| Keep `#C0632B` exactly as drawn | Warning text on the beige chip would fail contrast; the darkened value is the same hue and saturation |
| Add a separate `colorOnWarningContainer` token | The token list is already agreed and copied into the pubg-ui skill; one darker value solves it without a twelfth token |

### Impact
`workflow/docs/conventions/giao-dien.md` sections 1 and 9. `PUBGApp/app/src/main/res/values/colors.xml` and `themes.xml` will copy this table when P1-03 is done.

### Owner
tahpnart8

## 2026-10-22 - One naming rule for view ids and strings

### Decision
A view id is `<type><Thing>` in lowerCamelCase (`buttonSignIn`, `textPrice`, `recyclerListings`), and a string key is
`<module>_<screen>_<what>` (`listing_detail_price_label`). `workflow/docs/conventions/android-java.md`,
`.agents/rules/android-resources.md` and the `pubg-ui` skill now all say this.

### Context
The three documents disagreed: two said `btn_sign_in` and `account_sign_in`, the skill said `buttonSubmit` and
`listing_detail_price_label`. Five people writing 33 screens from three rulebooks would have produced three styles. An
issue-drafting pass found the conflict before any layout existed.

### Alternatives rejected
| Alternative | Why not |
| --- | --- |
| Keep `btn_sign_in`, the older Android habit | ViewBinding renames it to `binding.btnSignIn`, so the XML and the Java read differently; the camelCase id makes them identical, which is one less thing for a first-year student to hold in their head |
| Let each module choose | The shared `view_*.xml` pieces are included by every module |

### Impact
`workflow/docs/conventions/android-java.md` rows "Id view" and "Chuỗi", `.agents/rules/android-resources.md`, and the
mirror in `.claude/skills/`. No code exists yet, so nothing has to be renamed.

### Owner
tahpnart8

## 2026-10-22 - The 14 gaps the issue drafting pass uncovered

### Decision
Spec:
- Every one of the 14 ViewModels gains the same three fields: `loading: LiveData<Boolean>`, `error: LiveData<String>`, `done: LiveData<Boolean>`. They are the data behind the four states (loading, data, empty, error) that `giao-dien.md` section 6 demands, and `done` fires once per completed write so a screen can close a dialog without keeping its own flag.
- Each ViewModel also gains the data its screen actually shows and the methods to load it: the full list is in `dac-ta-lop/viewmodel.md`. The biggest ones are `ProfileViewModel.verification`, `userListings` and `following`; `ListingDetailViewModel.images`, `sellerStats` and `followingSeller`; `AdminUserViewModel.selectedUser`, `verifications` and `pendingAdmin`; `AdminContentViewModel.reports`, `selectedListing` and `flagged`.
- `AuthViewModel.register` is replaced by `startSignUp(email, password)`, `resendVerificationLink()` and `checkEmailVerified()`, because the registration order changed on 2026-10-22 and the old single method could not express it.
- `NotificationRepository.create(notification, batch, cb)` takes the write batch, so a notification really is written in the same batch as the status change `trang-thai.md` section 9 requires; pass `null` for a standalone write.
- New rule methods, because R18 forbids writing a status field directly: `Listing.remove(reason)`, `Listing.release()`, `Deal.dispute()`, `Deal.resolveDispute(DealStatus)`. Also `AuthRepository.isGoogleAccount()`, `UserRepository.getVerification(uid, cb)`, `AdminRepository.getFlaggedListings(cb)`, and `NetworkMonitor.getInstance()`.
- New fields: `reports.evidenceImageIds` (at most 3, optional) and `reports.conversationId` (only when the target is a message); Room `deal_cache.tradeType`. No Room table for payments: the payment history says "needs a connection" when offline.
- Five global navigation actions: `action_global_sign_in`, `action_global_my_profile`, `action_global_tab`, `action_global_deal_payment`, `action_global_admin_accounts`.

Behaviour, where a use case asked for something the design cannot do:
- A level 3 warning locks the account; removing that account's listings stays a separate button the admin presses.
- Removing one listing is a button on the admin's listing detail screen, not in the E04 list.
- Dropped: auto hiding a listing after 20 reports, cancelling open deals when an account is locked, refusing a national ID already used by another account, and the "new follower" notification. None of them can be enforced without Cloud Functions or a field that does not exist.

### Context
Five agents drafted 57 issues against the specification and each of them hit the same walls. A gap that stops five readers is a gap in the specification, not five misunderstandings.

### Alternatives rejected
| Alternative | Why not |
| --- | --- |
| Let each module add the LiveData it needs | Five people would invent five names and R16 would reject all of them at review |
| Keep `NotificationRepository.create` asynchronous and call it after the batch | A crash between the two writes loses the notification silently, and the state machine already promised one batch |
| Keep the four dropped rules and implement them "best effort" on the client | A rule the client can skip is not a rule; better to say plainly that the free plan cannot enforce it |

### Impact
`local/diagram-tools/model.py`, `erd_model.py`, `tech_text*.py`; the generated `dac-ta-lop/`, `firestore-schema.md`, `room-schema.md`; `trang-thai.md`, `security-rules.md`, `so-huu.md` section 4; the use case files of modules D and E and the interface READMEs. Class diagram pages 1, 3, 4, 5, 6, 7 rebuilt and the ERD regenerated.

### Owner
tahpnart8

## 2026-10-22 - Grading rubric mapped, and the plumbing the refresh pass proved missing

### Decision
- `workflow/docs/product/barem-diem.md` is new: it maps the teacher's 11 grading rows to the lecture chapters in `Mobile_App/lecture` and to the exact place in this project that answers each one. Every issue now names, in one sentence, the rubric row it serves and the technique it demonstrates, using the teacher's own words (RecyclerView, implicit Intent, menu and picker, adaptive layouts, async, connectivity check, JUnit).
- **Getter convention**, written into the generated `dac-ta-lop/viewmodel.md`: every LiveData is `private` with exactly one public `get<Name>()` returning `LiveData<...>`, never `MutableLiveData`. R16 treats that paragraph as the line defining all of them, so the documents do not list a hundred getters. The same applies to `NetworkMonitor.online`.
- Every ViewModel gains `clearError()` and `clearDone()`, because the convention says the screen clears those two fields and a private LiveData gave it no way to.
- Other plumbing the five drafting agents each hit independently: `AuthRepository.getCurrentUserId()`, `AuthViewModel.isGoogleAccount()`, `SessionManager.getCurrentUser()`, `ChatDao.clearAll()`, `DealDao.clearAll()`, `Listing.reserve(dealId)`, `SellerVerification.remainingResubmits()`, and `DealRepository.buildFromOffer(offer, listing)` so the deal can snapshot the listing's title and thumbnail.
- `AdminUserViewModel` and `AdminContentViewModel` gain the data their screens show but had no source for: the viewed user's listings, deals, reviews and report count, the two verification photos, a report's attached images, the reported conversation, and the per-category listing counts.
- `BaseRepository.logActivity(action, targetType, targetId)` takes a plain `String` target type, not `ReportTarget`: the activity log points at categories, keywords and admin accounts too, which that enum does not cover. `ReportRepository.getByStatus` and `AdminContentViewModel.loadReports` keep `ReportTarget`.
- `action_global_tab` takes `tabId` out of `home`, `search`, `post`, `chat`, `profile`.
- `NotificationRepository.create(notification, batch, cb)`: a non-null batch means the method only adds its write to that batch and calls back at once, and the caller commits, so the notification and the status change succeed or fail together.
- Colour rows added for `VerificationStatus.NONE` and the three `Priority` values.

### Context
Five agents drafted 58 issues, then refreshed them against the updated specification. The refresh is what surfaced the plumbing: a gap that stops one reader is a misunderstanding, a gap that stops five is a hole in the specification.

### Alternatives rejected
| Alternative | Why not |
| --- | --- |
| List every getter in `viewmodel.md` | A hundred table rows that say nothing, and R16 is satisfied just as well by one paragraph stating the rule |
| Let screens keep their own loading and error flags | Then rotating the screen loses them, which is exactly what the course requires to survive |
| Type `logActivity`'s target as `ReportTarget` | Was tried and produced `logActivity(String, ReportTarget, String)` in the generated docs, which cannot log a category or an admin change |

### Impact
`workflow/docs/product/barem-diem.md` (new), the generated `dac-ta-lop/` and `firestore-schema.md`, `trang-thai.md`, `luong-xu-ly.md`, `conventions/giao-dien.md`, `plan/so-huu.md` section 4, and all 58 issue drafts in `local/issues/`. Class diagram pages 1, 2, 3, 5, 6, 7 rebuilt; ERD regenerated.

### Owner
tahpnart8

## 2026-10-22 - A trimmed plan of 33 issues, and skeletons before bodies

### Decision
Four days of coding are left, so `workflow/plan/ke-hoach-rut-gon.md` replaces the phase files for this round: 24 of 33 screens, 33 issues, one person per issue.

- **Skeletons first.** Issue 2 of wave 0 creates all 93 classes at once from `dac-ta-lop/`: fields, getters, 23 enums, every repository signature with a `DataCallback.NOT_READY` body, every ViewModel with its LiveData, getters, `clearError` and `clearDone`. After it lands, every later pull request compiles whatever order it arrives in. This is what makes five people working in parallel safe, and it is the kind of mechanical generation an AI assistant does well.
- **One file, one person.** `so-huu.md` section 5 split the methods of a shared class between people, which meant three people opening `UserRepository.java`. For this round each file has exactly one owner: `UserRepository` and `NotificationRepository` go to Ngân whole, `MediaRepository` and `CategoryRepository` to Tú, `ReviewRepository` to Hải Long, `ProfileViewModel` and `TrustViewModel` to Phúc. Screen A05 moves to Phúc and B06 to Phúc, because both use `ProfileViewModel`. A Room entity and its DAO belong to whoever owns the model they convert.
- **Calling someone else's method is fine; editing their file is not.** That sentence is the whole scheduling rule.
- **Cut, and said so.** A04, A06, account settings, `RecommendationEngine` with similar listings and the price hint, user search, the radius filter (no coordinate source once A06 goes), C04, complaints with D05, D06 and D07, E05, E06, E07. `features.md` now has a section naming every dropped row, because the function table still marks them "Đợt này" and R17 therefore cannot protect them.
- **`DataCallback.NOT_READY` is now in the specification.** R20 and `so-huu.md` both relied on a constant no document defined.
- Defaults settled so nobody waits: `autoReleaseAt` is written as device time plus 7 days with a 5 minute tolerance in the Rules; the repository appends the `StatusChange` row, not the `Deal` method; notification text is a Vietnamese constant inside `NotificationRepository`; a non-null `batch` means the method only adds its write and the caller commits.

### Context
Three independent audit passes over the plan and the 58 issue drafts. The first version of this plan said wave 1 would write every repository body, which collided with 58 drafts written on the opposite assumption (that P1 pre-created everything and each issue fills in a few bodies). Two issues would have written the same method, and nobody would have created the Fragment frames, the seven shared `view_*.xml` pieces, the sub navigation graphs or the ViewModel classes. The audits also found the base Rules would have blocked guests, contradicting the decision made the same day, and that `FileProvider` was needed by three screens but owned by no issue.

### Alternatives rejected
| Alternative | Why not |
| --- | --- |
| Keep the per-method split of `so-huu.md` | It is the single largest source of merge conflicts, and four days leave no room for gỡ merge |
| Have each person create their own classes in wave 1 | `Deal` references `Listing`, `ChatRepository` calls `NotificationRepository`, `AppDatabase` lists other people's entities: no branch would compile until the others merged, and CI builds every pull request |
| Rewrite all 58 drafts to match the new plan | The drafts are still the best record of names and acceptance criteria; the plan says which lines to ignore instead |
| Keep all 33 screens | Nothing is built yet; a demo that runs end to end on 24 screens scores better than 33 half-finished ones |

### Impact
`workflow/plan/ke-hoach-rut-gon.md` (new), `so-huu.md` (an override note plus the ownership rows), `features.md` (the cut scope), `barem-diem.md`, `luong-xu-ly.md` (three signatures), `thuat-ngu.md`, `conventions/giao-dien.md`, the generated `dac-ta-lop/`, and the stale counts in `docs/README.md`, `overview.md`, `brief.md`, `phase-0`, `phase-3`. The class diagram's Firebase box no longer claims Cloud Storage.

### Owner
tahpnart8

## 2026-10-23 - Seven calls the first review round forced, and one name added to `users`

### Decision
Twelve pull requests were reviewed in one pass. Six of them waited on a judgement only the leader could make, so those are settled here rather than in a comment thread that the next person will not find.

- **`lastReviewId` joins `users`.** The `reviews` create branch demands that the reviewee's `ratingCount` rise by exactly one and `ratingSum` by exactly the new rating, but no `users` branch allowed that write, so no review could be stored and "complete when both sides have reviewed" was unreachable. Rules can only `getAfter` a path it knows in advance, and a `users` document does not know the `dealId` that the review id is built from. So the batch now declares the review id in `lastReviewId`, and the rule follows that pointer. Requiring `lastReviewId` to differ from the stored one is what stops the same review being counted twice.
- **Deal counts stay hidden on other people's profiles.** `getStats` queries `deals` by `sellerId` and `buyerId`, and the `deals` read rule admits only the buyer, the seller and admins, so every number on D02 and D03 is denied when viewing anyone else. Denormalising the counters into `users` is the correct fix and is not affordable with two days left, because each would need its own write branch, the same thicket `lastReviewId` just crossed. D02 and D03 therefore hide those figures off one's own page and keep the rating, which already lives in `users`. `UserRepository.getFollowerCount` is added so the follower figure survives, since `follows` is readable by anyone signed in.
- **`ChatRepository.getConversation` is added.** C02 can open from a notification with only an id in hand, and the specification offered no legitimate place to read one conversation, which pushed a Firestore call into a ViewModel. Hải Long was right not to invent it.
- **Only B03 must ship a `layout-land`.** An earlier call demanded one for B03, C02 and C03. C02 is a vertical `RecyclerView` that stretches correctly on its own; the requirement bought nothing and cost a file. B03 keeps it because of the large image carousel.
- **`categories` tightens to `serverStamp('updatedAt')`.** The loose branch existed to accommodate `CategoryRepository.update`, which calls `set(POJO)`; that call fails anyway, because a `Category` carrying no `createdAt` writes null into it and `onlyKeys` rejects the change. Loosening the rule removed one of two reasons for refusal, so it was not worth the only exception in the file.
- **Re-following stays a refusal.** `follows` allows no update, so a `set` onto an existing document is denied. The follow button is a toggle and the screen knows the current state, so follow is a create and unfollow a delete. A duplicate `set` from a second device is caught and read as "already following" rather than shown as an error.
- **Reporting your own listing is left possible.** UC33 forbids it for every target type, but enforcing it for listings needs a `sellerId` read and probably a navigation argument, and the damage is nil: an admin dismisses it. The deviation is recorded in the module D README instead.

### Context
Nothing had been reviewed before this pass, and the same five mistakes appeared across six branches: `setValue` in a repository callback, `Timestamp.now()` for `createdAt` or `updatedAt`, `set(POJO)` on an update, a Rules-required string left null, and Base64 decoded on a bare thread with no recycle guard. Two faults were worse than the pattern. `UserRepository.createProfile` writes an empty `email` while the rule demands it equal `request.auth.token.email`, so nobody can finish registering and therefore nobody on the team can exercise anything against real Firebase; the same method never assigns the id back, so the session opens on a `User` with a null id. Both were named in the review of the pull request that introduced them and both were filed under "worth fixing too" rather than as blockers, which is the review error that let them reach `main`.

### Alternatives rejected
| Alternative | Why not |
| --- | --- |
| Denormalise deal counters into `users` now | Every counter needs its own Rules branch tying the increment to a real deal, the problem `lastReviewId` just took a day to solve |
| Widen the `deals` read rule so any seller's completed deals are public | It exposes the buyer of every sale; the leak is worse than a missing number |
| Let the leader patch `createProfile` directly | It is Ngân's file, she has three pull requests already reworking it, and a silent edit would collide with every one of them |
| Relax the 100-byte commit subject limit to rescue a blocked branch | The two subjects are 90 and 103 characters, over any sane limit; the byte-versus-character flaw is real but separate |

### Impact
`erd_model.py` and `model.py` in the generator (hence `firestore-schema.md`, `model.md`, `data.md`, `room-schema.md`, `erd/README.md`), `security-rules.md` (the rating row rewritten, the `autoReleaseAt` tolerance), `trang-thai.md` and `luong-xu-ly.md` (the same tolerance), and `decisions.md`. `reports` and `complaints` gain the `updatedAt` their rules already required. Four DAO methods added in an earlier session reached `room-schema.md` at last.

### Owner
tahpnart8

## 2026-10-23 - Rules and indexes actually deployed, and five calls the second review round forced

### Decision
- **The rules and the 40 composite indexes are live.** `firebase.json` and `.firebaserc` were missing, so nobody had a way to run `firebase deploy` and nothing had ever been uploaded; the two files are now in the repository and both deploys have run. The 79 KB ruleset compiled server side, which closes the worry that it might exceed the per-ruleset size limit, and it also proves the two constructs nothing had verified are at least syntactically valid: the list slice in `historyAppended()` and `let` inside a function body. Their semantics are still untested.
- **`TrustViewModel.averageCriteria` stays public and static, and joins the specification.** The earlier call was to push it down into `ui.trust` as private, on the grounds that averaging three criteria is screen arithmetic rather than ViewModel work. That reasoning still holds, but `TrustViewModel` belongs to one person under the trimmed plan, so a public constant there endangers nobody, and the move would have cost a relocated method, a relocated test package and another review round. With two days left it bought tidiness and nothing else.
- **`NEW_MESSAGE`, `NEW_OFFER` and `OFFER_RESULT` all navigate to C02.** They were returning an unknown-action sentinel, so tapping the three most common notifications did nothing at all. Offers live inside the conversation thread and have no screen of their own, so the chat screen is the right destination for all three.
- **`Payment.getHeldAt()` gains `@ServerTimestamp`.** The rules require `serverStamp('heldAt')`, and unlike `createdAt` the field had no annotation, so leaving it null wrote null and deleting the device-time assignment was not enough on its own. Writing the whole payment document as a map would have worked too; the annotation is one line and matches what the field means, a moment the server stamps once.
- **A shared class needs an entry point for every source its callers hold.** `ImageLoader` took only a Base64 string, while `MediaRepository.getImage` returns `byte[]` and a freshly picked image is a `Uri`. Two screens therefore hand-rolled a decode and both got it wrong, which is the fifth and sixth time that bug has appeared. The class now has three overloads and `workflow/memory/code/ui.md` says which to call for which source. The lesson generalises: when the same mistake keeps arriving from different people, the shared class is missing a door, not the people missing the rule.

### Context
Twelve pull requests merged in one day. Six of them had to be reviewed twice, because the first round found the same five mistakes spread across six branches of one chain.

Two review errors of my own are worth recording. I told three pull requests that the rules were already enforcing their writes, which was false and unverified; the fixes were right but the urgency I attached to them was invented, and that may have misordered someone's work. And in the review of the pull request that introduced `UserRepository.createProfile`, I noticed it trusted the client's email and filed that under "worth fixing too" rather than as a blocker. It was a blocker: the rules demand the email equal `request.auth.token.email`, the method wrote an empty string, so nobody could finish registering and the whole team was locked out of testing against real Firebase for most of the day.

### Alternatives rejected
| Alternative | Why not |
| --- | --- |
| Paste the rules into the Console by hand | It works for rules and not at all for 40 composite indexes, and it leaves deployment tied to one person's machine |
| Hold the D02 pull request until `averageCriteria` moved packages | Two days left, and the move protects nobody because the file has a single owner |
| Patch `createProfile` myself once it proved to be the team's blocker | It is Ngân's file, she had three pull requests already touching it, and a silent edit would have collided with every one |
| Leave the three notification types returning the unknown sentinel until QA15 was answered | They are the three most frequent notifications; silence on tap is worse than an imperfect destination |

### Impact
`firebase.json` and `.firebaserc` (new), the live Firestore project. `model.py`, `erd_model.py`, `tech_text_v3.py`, `tech_text.py` and `build_tech_docs.py` in the generator, hence the regenerated `dac-ta-lop/viewmodel.md`. The module C and module D UI READMEs record three deviations each. `ImageLoader` gained two overloads and `workflow/memory/code/ui.md` is the first note for the UI area.

### Owner
tahpnart8

## 2026-10-23 - Unified input validation in AuthViewModel

### Decision
`AuthViewModel` is the single source of truth for account input validations (`isPasswordValid`, `isPasswordConfirmationMatching`, `isPhoneValid`). `RegisterFragment` and other UI components reuse these package-private methods instead of maintaining duplicate private validation implementations.

### Context
Review of PR #153 pointed out that `isPasswordValid`, `isPhoneValid`, and `isPasswordConfirmationMatching` had duplicated implementations in `AuthViewModel` and `RegisterFragment`, causing unit tests to test the ViewModel copy while the fragment executed its own private copy.

### Alternatives rejected
| Alternative | Why not |
| --- | --- |
| Keep duplicate private methods in Fragments | Disagrees with unit tests and causes divergence when validation rules change |
| Move validation to a new utility class | AGENTS.md R16 forbids inventing new public classes without an issue or specification update |

### Impact
`AuthViewModel.java`, `RegisterFragment.java`.

### Owner
loopy-tnw

## 2026-10-25 - Pull requests merge with rebase, so every commit of every person stays on main

### Decision
Pull requests are merged with **rebase and merge**. Squash stays enabled but is not used. The ruleset `main-review` asks for no approval count (the leader and Claude review in comments and merge), allows `rebase` and `squash`, and the repository no longer deletes head branches on merge.

A branch must hold no merge commit, because GitHub reports `rebaseable: false` for it and cannot rebase and merge it. CI now fails a pull request that contains a merge commit. Catching up with `main` is done with `git rebase origin/main` and `git push --force-with-lease` on one's own work branch (never `main`), after a backup branch, as the `pubg-sync-branch` skill says. A branch that is only behind `main` needs nothing.

### Context
The course grades the number and spread of commits per person. With squash, a pull request of ten commits became one, and the pull requests Claude opened for the leader were all credited to the leader, so main showed 43 commits for the leader and 12 to 16 for each other member.

### Alternatives rejected
| Alternative | Why not |
| --- | --- |
| Keep squash and ask for more pull requests | Splits real work into artificial pull requests and still gives one commit each |
| Plain merge commits | The ruleset `required_linear_history` forbids them, and they bury commits in merge noise |
| Rebase merge but keep merging `main` into branches | GitHub cannot rebase a branch with a merge commit |

### Impact
`AGENTS.md` (R9, R10, section 8), `CLAUDE.md`, skills `pubg-sync-branch` and `pubg-review-pr`, `.github/NAMING.md`, `.github/rulesets/main-review.json`, `workflow/scripts/ci-policy.sh`, `apply-github-settings.sh`, workflow documents.

### Owner
tahpnart8

## 2026-10-25 - The whole scope returns: every STT marked "Đợt này" is built, not only the shortened set

### Decision
The cut of 2026-10-22 (`ke-hoach-rut-gon.md` section 4) is lifted. All 91 STT rows whose scope is "Đợt này" and all 43 use cases are built by 2026-10-27, with the restored screens `AccountSettingsFragment`, `AddressListFragment`, `AddressEditFragment`, `DealHistoryFragment`, `CreateComplaintFragment`, `MyReportsFragment`, `AdminComplaintListFragment`, `AdminComplaintDetailFragment`, `AdminAccountFragment`, `AdminCategoryFragment`, `AdminDealMonitorFragment` and `AdminDealDetailFragment`. `PersonalInfoFragment` (A04) stays out: every item it held lives on D01 and A05.

The leader laid the navigation shells first (empty Fragments, layouts, titles, destinations and actions) so each later issue only fills bodies, as R20 requires. Ownership moves to spread the work: E05 and E06 to Hải Long, E07 and user search (STT 103) to Ngân, `User.lastReviewId` to Phúc, `Converters` to Hải Long. The leader keeps review, merging and Firebase.

### Context
A full run on a device on 2026-10-25 showed the shortened scope was met at the code level but covered only 61 of 91 STT rows. The repositories for almost every cut feature already existed, so what remained was view models and screens, cheap to do in two days.

### Alternatives rejected
| Alternative | Why not |
| --- | --- |
| Keep the shortened scope | Leaves 26 STT and nine use cases unmet in a course graded on them |
| Let each owner add their own navigation destinations | Breaks R20 (navigation graphs are leader files) and makes five people edit `nav_main.xml` at once |

### Impact
`nav_main.xml`, `nav_admin.xml`, twelve new Fragments and layouts, `so-huu.md`.

### Owner
tahpnart8

## 2026-10-28 - Google sign-in goes through Credential Manager first, the browser page only as a fallback

### Decision
`AuthRepository.signInWithGoogleBrowser` now asks the system Google account picker through AndroidX Credential Manager (`GetSignInWithGoogleOption` with `default_web_client_id`) and exchanges the Google ID token with `signInWithGoogle`. Only when Credential Manager fails for a reason other than the user cancelling does it open Firebase's browser page as before. The method name and signature stay, so no screen or view model changes. New dependencies: `androidx.credentials:credentials` and `credentials-play-services-auth` 1.6.0, `com.google.android.libraries.identity.googleid:googleid` 1.2.1 (latest stable on Google Maven on 2026-10-28).

### Context
The browser flow (`startActivityForSignInWithProvider`) failed repeatedly on the emulator with "Unable to process request due to missing initial state": Chrome loses the redirect state between leaving and returning. The leader could not sign in as admin, which blocked the admin use cases of test pass #305. `google-services.json` already holds Android OAuth clients for the SHA-1 of all five members, so Credential Manager needs no Firebase change.

### Alternatives rejected
| Alternative | Why not |
| --- | --- |
| Keep only the browser flow and clear Chrome data when it fails | Not a fix; users cannot be asked to clear Chrome |
| Credential Manager only, no fallback | A device without Google Play services could not sign in with Google at all |

### Impact
`libs.versions.toml`, `app/build.gradle.kts`, `AuthRepository.java`, `dac-ta-lop/data.md`, `luong-xu-ly.md`.

### Owner
tahpnart8

## 2026-10-28 - Screens whose data other people change listen with snapshot listeners

### Decision
Every repository read that a screen shows and that another party can change gets a live twin named `observe...` that returns a `ListenerRegistration` (built on `BaseRepository.listenDocument`, `listenQuery` and `listenEitherQuery`). The old `get...` methods stay for write flows and one-off checks. ViewModels keep their listeners in a `ListenerSet` and clear it in `onCleared`. Live now: own and admin-viewed profile, own verification, listing detail, deal detail and payment, deal and payment history, conversation list, notifications and badge, my reports and complaints, and the admin queues (verification requests, open reports, complaints, deal monitor and deal detail).

### Context
The leader changed a verification status in the Firebase Console during test pass #305 and the app did not follow until the screen was reopened. Only chat messages and offers were live; nothing re-read on resume (issue #340).

### Alternatives rejected
| Alternative | Why not |
| --- | --- |
| Re-read every screen in `onResume` | Still stale while the screen is open, and it reads the whole list again each time |
| Turn the existing `get...` methods into listeners | Write flows and one-off checks call them too and would leak listeners |
| Make the feed and search live too | They page with a cursor; a listener per page costs reads for little gain. They keep pull to refresh |

### Impact
`BaseRepository` and nine repositories, `ListenerSet` and ten ViewModels, `dac-ta-lop/data.md`, `viewmodel.md`, `overview.md`, `luong-xu-ly.md`. Counts (`count()`) stay one-shot because Firestore has no listener for aggregates.

### Owner
tahpnart8

## 2026-10-28 - UI refresh: full Material 3 roles, Be Vietnam Pro, theme-owned shapes

### Decision
The app gets a new coat of paint without any behaviour change. `Theme.PUBGApp` declares every Material 3 colour role derived from the existing palette, with one warm orange accent (`#C2410C`, `colorTertiary`) reserved for count badges and "Mới" or sale-type labels; prices and main buttons stay green. The bundled font is Be Vietnam Pro (SIL OFL 1.1, four weights, licence in `PUBGApp/licenses/`), applied through all 15 text appearances. Shapes and elevation are owned by the component styles in `res/values/styles.xml` (pill buttons and chips, 16dp cards, 12dp text fields, 24dp dialogs and sheets); layouts no longer set `cornerRadius`, `cardCornerRadius` or `cardElevation`, and name `Widget.PUBGApp.*` styles instead of `Widget.Material3.*`.

### Context
Leader-approved refresh towards a modern marketplace look (issue #349). Before it, unset roles fell back to the Material purple, per-view radius and elevation attributes blocked any theme change, and there was no font.

### Alternatives rejected
- Overriding only the global `shapeAppearanceCorner*` tokens and keeping `Widget.Material3.*` in layouts: chips and text fields share those tokens with other components, so a pill chip would have leaked into menus and fields.
- Downloadable Google Fonts: needs Play services at runtime and gives no font offline on first launch; bundling costs about 550 KB.
- A second accent colour for prices: the mockups make the green price the anchor of every card.

### Impact
`colors.xml`, `themes.xml`, `styles.xml`, `dimens.xml` (`radius_xl`), `res/color/`, `res/font/`, every layout (attribute removal and style names only), `giao-dien.md` sections 1a, 2, 3 and 5a.

### Owner
tahpnart8

## 2026-10-29 - C07 checkout stays simulated: an e-commerce style flow with no payment gateway

### Decision
C07 becomes a multi-screen checkout that looks like an online shop: choose the method, choose a bank or e-wallet from a static local list, a "Cổng thanh toán mô phỏng PUBGApp" screen with a 10 minute countdown, a read-only test card, a sample QR drawn on the device or an "open the wallet" confirmation, a test OTP shown on screen, then processing and a receipt. Everything before the last step is display only. The single write is still `DealViewModel.pay(PaymentMethod)`, which creates `payments/{dealId}` as before; the chosen bank or wallet lives only in navigation arguments and saved state. No Firestore field, no enum value and no dependency is added. Banks appear by name and short code on tinted monogram tiles in the app colours; no real logo or brand colour is used.

### Context
The plain radio list of C07 did not read as a payment during the demo, and the leader asked for a realistic flow (issue #391, refs #380). A real gateway is impossible here: the Spark plan has no server side to receive gateway callbacks or keep merchant secrets, and a student team has no licence to hold buyers' money. The point "Payment is simulated inside Firestore" of "2026-10-22 - Guest browsing, verification as a trading gate, admin accounts in the app" still holds; this entry only changes how the screens look.

### Alternatives rejected
- A real or sandbox gateway (VNPay, MoMo or Stripe test mode): needs a server for the signed callback and a merchant account, neither of which fits the Spark plan or the course; a sandbox key in the app would also be a secret in the client (R11).
- Storing the chosen bank or wallet in `payments`: it would add a field to a documented schema for data that means nothing, since no bank is contacted.
- A QR library such as ZXing to draw a real QR: a new dependency for a picture that must not be scannable anyway, because there is no account to pay into.
- Text fields for card number and expiry, validated locally: they teach users to type real card data into a student app; a fixed read-only test card shows the same step without that risk.

### Impact
`ui/trade/` (`DealPaymentFragment`, new `CheckoutProviderFragment`, `CheckoutGatewayFragment`, `CheckoutResultFragment`, `PaymentProvider`, `PaymentProviderAdapter`, `SampleQrCode`, `CheckoutSummary`), `nav_main.xml`, `strings_trade.xml`, `dimens.xml` (`qr_max_size`), `workflow/docs/ui/c-ket-noi-giao-dich/README.md` section C07, `dac-ta-chuc-nang.md` STT 56, `so-huu.md` section 3, `dac-ta-lop/README.md`.

### Owner
tahpnart8
