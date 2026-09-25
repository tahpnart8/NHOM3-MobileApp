---
trigger: glob
globs: "**/*.java"
description: Java conventions for the PUBGApp Android code
---

# Java code in PUBGApp

Detail: `docs/conventions/android-java.md`. Architecture: `docs/architecture/overview.md`.

- Java 17. No Kotlin, no Jetpack Compose.
- **Plain, explicit Java (AGENTS.md R15, skill `pubg-code`).** Write the type, one statement per line. No lambdas, `->`, method references, streams, `var`, records, text blocks. Anonymous classes for listeners, `for` loops. Small methods and classes. Comments of one or two lines that say why, never what. Lasting knowledge goes in `memory/code/`, not in comments. Run `sh scripts/check-java-style.sh` before you finish.
- Package `com.nhom3.pubgapp.feature.<name>.{ui,data,model}`; shared code in `com.nhom3.pubgapp.common`. A change stays inside the package the issue names.
- ViewBinding, never `findViewById`.
- No network, disk or database work on the main thread. Rotation must not lose a screen's state.
- Do not use an AndroidX, Firebase or Room API you have not verified: check with the `context7` MCP server or the official docs, or write "unverified".
- Do not add a dependency. Versions live only in `PUBGApp/gradle/libs.versions.toml`.
- Who the user is and what they may do comes from Firebase Authentication and the Security Rules, never from data the app sends.
