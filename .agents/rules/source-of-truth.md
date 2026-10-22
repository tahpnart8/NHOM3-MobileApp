---
trigger: always_on
description: Names, scope and shared files come from the documents and the issue, never from memory; applies to every task
---

# Source of truth

The full text is `AGENTS.md` R16 to R23. These points are repeated because tools most often break them.

- Every class, field, method, enum value, collection, table and status must exist, spelled exactly, in `workflow/docs/architecture/dac-ta-lop/`, `workflow/docs/data/` or `workflow/docs/architecture/trang-thai.md`. A name that is not there: stop and ask in the issue, never invent it (R16).
- A function marked "Phát triển sau" in `workflow/docs/product/dac-ta-chuc-nang.md` is never built, not even as a stub. No Cloud Storage, no video: images are Base64 in Firestore (R17, R21).
- Statuses and transitions only as in `trang-thai.md` (R18).
- Touch only the files the issue lists (R19). Shared files and other modules' methods are not yours; replace only the method bodies assigned to you (R20, `workflow/plan/so-huu.md` sections 5 and 6).
- Three Activities only; new screens are Fragments in your module's `ui` package; cross-module navigation uses only the global actions in `nav_main.xml` (R22).
- A gap or a disagreement between the issue and the documents: comment on the issue in Vietnamese and wait (R23).
