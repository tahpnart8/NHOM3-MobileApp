---
name: pubg-doc-check
description: Find where PUBGApp documents and code disagree (feature register, Firestore and Room schemas, architecture). Use before a pull request that changes data or behaviour, or when asked whether the docs are up to date.
---

Documents are part of the code. This skill compares them and reports; it does not decide which side is right.

## Steps

1. **Feature register.** For every row in `docs/product/features.md` with status `in-progress` or `done`, confirm the code exists under `PUBGApp/app/src/main/java/com/nhom3/pubgapp/feature/<name>/`. For code with no row, list it. Search the code; do not trust a name.
2. **Firestore.** For every collection or field named in `docs/data/firestore-schema.md`, search the Java sources for it. For every collection the code reads or writes, confirm the document lists it, with the same field names and types.
3. **Room.** Compare each `@Entity` class in the sources with `docs/data/room-schema.md`: table name, columns, types, keys, and the database version.
4. **Architecture.** Confirm what `docs/architecture/overview.md` states as built (as opposed to proposed) is really in the code, including any Firebase service it says is configured.
5. **Report** three lists: documented but missing in code, in code but missing in documents, and present in both but different (quote both sides with file and line). Say which searches you ran. If a source you needed does not exist yet, say so instead of assuming.

## Not allowed

Editing a document so that it matches code you cannot justify (AGENTS.md R7). If code and document disagree, report it and let the owner of the issue decide.
