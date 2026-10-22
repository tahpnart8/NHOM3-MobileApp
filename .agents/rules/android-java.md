---
trigger: glob
globs: "**/*.java"
description: Java conventions for the PUBGApp Android code
---

# Java code in PUBGApp

Detail: `workflow/docs/conventions/android-java.md`. Architecture: `workflow/docs/architecture/overview.md`.

- Java 17. No Kotlin, no Jetpack Compose.
- **Plain, explicit Java (AGENTS.md R15, skill `pubg-code`).** Write the type, one statement per line. No lambdas, `->`, method references, streams, `var`, records, text blocks. Anonymous classes for listeners, `for` loops. Small methods and classes. Comments of one or two lines that say why, never what. Lasting knowledge goes in `workflow/memory/code/`, not in comments. Run `sh workflow/scripts/check-java-style.sh` before you finish.
- Packages are split by layer: `model`, `model.enums`, `data`, `data.local`, `util`, `viewmodel`, and `ui.<module>` for Fragments, Adapters and Activities (module: `account`, `notification`, `listing`, `trade`, `profile`, `trust`, `admin`, `common`, `main`; see `workflow/plan/so-huu.md` section 2). A class goes in the package that `workflow/docs/architecture/dac-ta-lop/` names for it. A change stays inside the files the issue lists.
- Class, method, field and enum names come from the specification (AGENTS.md R16). Replace only the method bodies your issue assigns to you (R20).
- ViewBinding, never `findViewById`.
- No network, disk or database work on the main thread. Rotation must not lose a screen's state.
- Do not use an AndroidX, Firebase or Room API you have not verified: check with the `context7` MCP server or the official docs, or write "unverified".
- Do not add a dependency. Versions live only in `PUBGApp/gradle/libs.versions.toml`.
- Who the user is and what they may do comes from Firebase Authentication and the Security Rules, never from data the app sends.
