---
name: pubg-code
description: How to write Java for PUBGApp so a student can read it; plain, explicit, no lambdas, comments of one or two lines, and lasting knowledge kept in memory/code/ instead of in comments. Use whenever you write or change Java, before you write it and again before you finish.
---

Write the code a student who finished the course lectures can read on the first pass. Plain Java, one idea per line, names that say what they mean. Short beats clever, and short does not mean cramped.

These rules apply to **the lines you write or change**. Do not rewrite existing code for style (AGENTS.md R3); when you touch a method, leave it in the style of this skill.

## Do

- **Say it explicitly.** Write the type (`String email = ...`), one statement per line, and name each intermediate value instead of chaining calls. Up to two calls in a row (`text.trim().length()`) is fine; a builder chain is fine.
- **Listeners are anonymous classes or named methods.** For a click handler write `new View.OnClickListener() { @Override public void onClick(View view) { ... } }`, and move a long body into a private method the handler calls.
- **Loops are `for` loops.** Use the enhanced `for (Listing listing : listings)`.
- **Small methods that do one thing.** About 30 lines at most, at most two levels of nesting, and return early instead of nesting `else`. More than three parameters means you probably need a model object.
- **Small classes with one job.** About 300 lines at most. A Fragment or Activity only shows things and forwards events; logic lives in the ViewModel and the Repository; Firebase and Room calls live in the `data` package. Follow `docs/architecture/overview.md` for the layers.
- **Names are whole words.** Methods start with a verb (`loadListings`), booleans read as a question (`isSoldOut`, `hasPhotos`), no abbreviations except `id` and `url`.
- **No magic values.** Numbers and Firestore field names are `private static final` constants (`MAX_PHOTOS = 5`), defined once.
- **Handle errors where you can tell the user.** Pass a failure back to the caller (a callback or a result object), show a clear message, and never leave a `catch` empty (AGENTS.md R12). Check for `null` where it can happen, and mark it with `@Nullable` or `@NonNull`.
- **Leave a small check behind** for non-trivial logic: the smallest JUnit test that fails if the logic breaks (AGENTS.md R13). Formatters, validators and mappers get one.

## Do not

- No lambdas, no `->` in switch, no method references (`::`), no streams, no `Optional` chains.
- No `var`, records, text blocks, pattern `instanceof` (`x instanceof Foo foo`), sealed classes. The toolchain is Java 17 but the style is deliberately plainer.
- No `System.out`, no `printStackTrace()`. Use `Log` with a short message, or show the error to the user.
- No commented-out code, no `TODO` or `FIXME` in code, no author or date comments, no decorative banners.
- No new abstraction, interface or base class for a single use.

## Comments

- **At most one or two lines**, and only for **why** something is not obvious: a workaround, a platform trap, a rule you must not break. Never restate what the code says.
- No Javadoc on every method. A one-line Javadoc on a public method is fine when the name alone does not say enough.
- One line above a class is enough when its job is not obvious from its name.

## Where knowledge goes

**Not in the code.** When you learn something that a future reader needs and the code cannot say (a trap in Firebase, why a query has a condition, a pattern you chose, an approach that failed), write it in `memory/code/<area>.md` and keep the code comment to one line at most. What belongs there and how to write an entry: `memory/code/README.md`. Choices between real alternatives go to `memory/decisions.md` with the `pubg-record-decision` skill.

## Example

Not this:

```java
saveButton.setOnClickListener(v -> repository.getListings().stream()
        .filter(l -> l.getPrice() > 0).map(Listing::getId).forEach(this::save));
```

This:

```java
saveButton.setOnClickListener(new View.OnClickListener() {
    @Override
    public void onClick(View view) {
        saveListingsWithPrice();
    }
});

private void saveListingsWithPrice() {
    List<Listing> listings = repository.getListings();
    for (Listing listing : listings) {
        if (listing.getPrice() > 0) {
            save(listing.getId());
        }
    }
}
```

## Before you finish

1. Run `sh scripts/check-java-style.sh`. It lists the lambdas, streams, long comments and the other patterns above on the lines you added. Fix each finding, or say in your report why a line is better as it is. It is a checker, not a judge: it cannot tell whether a name is good or a method too long, so read your own diff once with that in mind.
2. Run `./gradlew assembleDebug testDebugUnitTest lintDebug` from `PUBGApp/` (AGENTS.md R1).
3. Write any lasting knowledge into `memory/code/` and tell the person you did.
