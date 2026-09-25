# Memory

This folder records **why** the project is the way it is and what the leader has asked for durably. It is short on purpose. What the system **is** lives in `docs/`.

| File | Holds | Written when |
| --- | --- | --- |
| `people.md` | Who is on the team, their GitHub login and role | Someone joins, leaves or changes role |
| `preferences.md` | Standing instructions from the leader that outlive one task | The leader states a rule that should stay |
| `decisions.md` | Architecture and delivery decisions, in the order they were made | A decision sets or changes a technology, a data shape, a rule or the workflow |
| `decision-template.md` | The shape of one decision entry | Only when the template itself changes |
| `code/` | What people and tools learn about the code and the libraries that the code cannot say: traps, why something has its shape, patterns. One file per area. Not append only, kept true | You learn something a future reader needs (rules in `code/README.md`) |

## Rules

1. **`decisions.md` is append only.** Never edit, reorder or delete an old entry, even to fix wording. To retire a decision, add a `### Status` section to the old entry that names the entry which replaces it, and add the new entry at the end. A hook and CI reject any change that removes a line.
2. Every entry title carries an **absolute date**: `## 2026-09-25 - Title`. Never "today" or "last week".
3. An entry names the alternatives that were rejected and why. Without that it records an outcome, not a decision.
4. **No secrets** in this folder: no key, token, password. Refer to a path or an issue number instead.
5. Routine implementation is not a decision. Do not log "added a button". Log "chose Firestore over Realtime Database".
6. Use the `pubg-record-decision` skill to write an entry.
