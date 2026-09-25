# Naming

The single authority for how branches, commits, pull requests, issues and labels are named. `scripts/lib/policy.sh` enforces the grammar of branches and commit subjects; keep the two in step.

## Branches

```text
<type>/<issue number>-<slug>
```

`type` is one of `feat fix docs refactor test chore build ci`. The slug is 2 to 6 lowercase words joined by `-`, describing the work rather than the files. There is no person's name in a branch; the pull request shows who wrote it.

```text
feat/12-google-sign-in
fix/31-listing-photo-rotation
chore/1-bootstrap-repo
```

Branch from the latest `main`. One issue, one branch, one pull request. The only other accepted shape is `revert-<n>-...`, which GitHub creates when someone presses Revert on a merged pull request.

## Commit subjects

```text
<type>(<scope>): <imperative summary>
```

Lowercase, imperative, 72 characters at most, no trailing period. The scope is optional and names an area: `auth`, `listing`, `feed`, `chat`, `order`, `profile`, `admin`, `offline`, `ui`, `build`, `docs`, `repo`. The type list is closed.

| Type | Use for |
| --- | --- |
| `feat` | New behaviour a user can see |
| `fix` | Behaviour that was wrong |
| `docs` | Documents only |
| `refactor` | Structure changes, behaviour does not |
| `test` | Tests only |
| `chore` | Housekeeping, dependencies, repository mechanics |
| `build` | Gradle and packaging |
| `ci` | Workflow files |

The body, when there is one, says why. It never contains a `Co-authored-by` line or any AI byline.

## Pull requests

- The title is the primary commit subject, word for word. It becomes the commit on `main` (squash merge).
- The body follows `.github/PULL_REQUEST_TEMPLATE.md` and contains `Closes #N` (repeat the keyword before every number: `Closes #1, closes #2`), or `Closes: none`.
- A pull request that deletes code under `PUBGApp/app/src/main` also contains `Removal-Issue: #N`.

## Issues

An imperative sentence in sentence case, no prefix and no identifier: `Add Google sign-in to the login screen`. Use a form: `feature`, `bug`, `chore`, or `removal` (leader only).

## Labels

`type:feature type:bug type:chore type:docs type:removal`, `area:auth area:listing area:feed area:chat area:order area:profile area:admin area:offline area:ui area:build area:docs`, `priority:p0 p1 p2`, `needs-leader-decision`, `blocked`. Created by `scripts/create-labels.sh`.

## Code

Java packages `com.nhom3.pubgapp.feature.<name>`. Layout files `<screen>_<feature>.xml` style is in `docs/conventions/android-java.md`.
