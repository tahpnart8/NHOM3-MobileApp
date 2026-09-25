# Code memory

What people and AI tools learn about the code and the platform **that the code itself cannot say**. It exists so knowledge stays out of code comments: the code keeps comments to one or two lines, and the lasting explanation lives here.

| File | Holds |
| --- | --- |
| `firebase.md` | Facts and traps of Firebase Authentication, Firestore and Cloud Storage |

Create a new file per area when you have the first entry for it (`android.md`, `room.md`, `ui.md`, `testing.md`). One file per area keeps five people from editing the same file.

## What belongs here

- A trap in a library or the platform that cost time or would cost the next person time.
- **Why** a piece of code has a shape that looks wrong but is right (a query condition, an ordering, a workaround).
- A pattern the project uses and where to find the reference implementation.
- An approach that was tried and failed, with the reason.

## What does not

- What the code or a document already says. Point to it instead.
- A choice between real alternatives: that is a decision, in `memory/decisions.md` (skill `pubg-record-decision`).
- Task progress, plans, or a description of a change: that is the pull request.
- A secret, a key, a token, or personal data.

## An entry

```markdown
### <short title>
- **Fact:** <one or two sentences>
- **Why it matters here:** <where in this project it bites; a file path if there is one>
- **Source:** <official page or file path>. **Checked:** <YYYY-MM-DD>, or **Unverified** and what would verify it.
```

## Rules

1. **One fact per entry, short.** If it needs a page, it belongs in `docs/`.
2. **Every entry has a source or is marked unverified.** Do not write a guess as a fact (AGENTS.md R2).
3. **Keep it true.** Unlike `decisions.md`, this folder is not append only: fix an entry that turned out wrong, delete one that no longer applies, and say so in the pull request.
4. **Read the file for your area before you code, and add to it when you finish.** The `pubg-code` skill asks for both.
5. Write in English.
