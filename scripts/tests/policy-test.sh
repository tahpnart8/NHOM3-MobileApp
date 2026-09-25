#!/bin/sh
# Proves the contributor policy rejects what it claims to reject and accepts a normal commit.
# Run: sh scripts/tests/policy-test.sh      Exit code 0 means every case behaved as expected.
root=$(cd "$(dirname "$0")/../.." && pwd)
pass=0
failed=0

ok() { # description, command...  (must succeed)
    d=$1; shift
    if "$@" >/dev/null 2>&1; then pass=$((pass + 1)); else failed=$((failed + 1)); echo "FAIL (expected accept): $d"; fi
}
no() { # description, command...  (must fail)
    d=$1; shift
    if "$@" >/dev/null 2>&1; then failed=$((failed + 1)); echo "FAIL (expected reject): $d"; else pass=$((pass + 1)); fi
}

# ---- rules --------------------------------------------------------------------------------------
. "$root/scripts/lib/policy.sh"

ok "plain subject"            subject_ok "feat(auth): add google sign-in button"
ok "no scope"                 subject_ok "fix: handle a null user"
ok "merge commit"             subject_ok "Merge branch 'main' into feat/1-x"
no "no type"                  subject_ok "Add stuff"
no "capital after colon"      subject_ok "feat(auth): Add button"
no "trailing period"          subject_ok "feat: adds button."
no "unknown type"             subject_ok "feature: add button"
no "too long"                 subject_ok "feat: aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"

ok "branch"                   branch_ok "feat/12-google-sign-in"
no "branch wrong type"        branch_ok "feature/12-google"
no "branch without issue"     branch_ok "feat/google-sign-in"
no "branch underscore"        branch_ok "feat/12_google"
no "branch upper case"        branch_ok "Feat/12-google"
no "main is not a work branch" branch_ok "main"

no "claude trailer"           check_message "feat: x

Co-Authored-By: Claude <noreply@anthropic.com>"
no "any co-author"            check_message "feat: x

Co-authored-by: Someone <someone@example.com>"
no "generated with footer"    check_message "feat: x

Generated with [Claude Code](https://claude.com/claude-code)"
no "robot emoji"              check_message "feat: x

🤖 made by a tool"
ok "cursor is an Android word" check_message "fix(offline): close the Cursor after each query"
ok "ordinary body"            check_message "feat(auth): add google sign-in button

Adds the button and the Firebase credential exchange.
Closes #12"

no "identity Claude"          check_identity "Claude" "noreply@anthropic.com" author
no "identity Gemini"          check_identity "Gemini CLI" "gemini@example.com" author
no "identity copilot bot"     check_identity "github-copilot[bot]" "1+Copilot@users.noreply.github.com" author
no "identity Antigravity"     check_identity "Antigravity Agent" "a@example.com" author
ok "identity human"           check_identity "Tran Duc Phat" "tranducphat1836@gmail.com" author
ok "identity human vietnamese" check_identity "Nguyễn Thúy Ngân" "ngan@example.com" author

# ---- server side functions ----------------------------------------------------------------------
export POLICY_SOURCE_ONLY=1 POLICY_SCRIPTS_DIR="$root/scripts"
. "$root/scripts/ci-policy.sh"
set +e
set +u
b64() { printf '%s' "$1" | base64 | tr -d '\n'; }
count_problems() { problems=0; "$@" >/dev/null 2>&1; echo "$problems"; }

good=$(b64 "feat(auth): add google sign-in button")
[ "$(count_problems check_commit_row aaaaaaaa1 tahpnart8 tahpnart8 "Tran Duc Phat" a@b.c "$good")" = "0" ] && pass=$((pass + 1)) || { failed=$((failed + 1)); echo "FAIL: team member commit rejected"; }
[ "$(count_problems check_commit_row aaaaaaaa1 TAHPNART8 web-flow "Tran Duc Phat" a@b.c "$good")" = "0" ] && pass=$((pass + 1)) || { failed=$((failed + 1)); echo "FAIL: login case or web-flow rejected"; }
[ "$(count_problems check_commit_row aaaaaaaa2 NONE NONE "Someone" a@b.c "$good")" != "0" ] && pass=$((pass + 1)) || { failed=$((failed + 1)); echo "FAIL: unlinked e-mail accepted"; }
[ "$(count_problems check_commit_row aaaaaaaa3 stranger stranger "Stranger" a@b.c "$good")" != "0" ] && pass=$((pass + 1)) || { failed=$((failed + 1)); echo "FAIL: outsider accepted"; }
[ "$(count_problems check_commit_row aaaaaaaa4 tahpnart8 tahpnart8 "Claude" noreply@anthropic.com "$good")" != "0" ] && pass=$((pass + 1)) || { failed=$((failed + 1)); echo "FAIL: AI identity accepted"; }
trailer=$(b64 "feat: x

Co-authored-by: Gemini <g@example.com>")
[ "$(count_problems check_commit_row aaaaaaaa5 tahpnart8 tahpnart8 "Tran Duc Phat" a@b.c "$trailer")" != "0" ] && pass=$((pass + 1)) || { failed=$((failed + 1)); echo "FAIL: co-author trailer accepted"; }
[ "$(count_problems check_files_row memory/decisions.md modified 3 "")" != "0" ] && pass=$((pass + 1)) || { failed=$((failed + 1)); echo "FAIL: decisions rewrite accepted"; }
[ "$(count_problems check_files_row memory/decisions.md modified 0 "")" = "0" ] && pass=$((pass + 1)) || { failed=$((failed + 1)); echo "FAIL: decisions append rejected"; }
[ "$(count_problems check_files_row PUBGApp/app/src/main/java/x/Y.java removed 40 "Closes #1")" != "0" ] && pass=$((pass + 1)) || { failed=$((failed + 1)); echo "FAIL: deletion without Removal-Issue accepted"; }
[ "$(count_problems check_files_row PUBGApp/app/src/main/java/x/Y.java modified 40 "Closes #1")" = "0" ] && pass=$((pass + 1)) || { failed=$((failed + 1)); echo "FAIL: plain edit rejected"; }
set -u

# ---- hooks in a throwaway repository ------------------------------------------------------------
tmp=$(mktemp -d)
trap 'rm -rf "$tmp"' EXIT
git init -q "$tmp/repo"
cd "$tmp/repo"
git config user.name "Tran Duc Phat"
git config user.email "human@example.com"
git config core.hooksPath "$root/.githooks"
git config commit.gpgsign false
mkdir memory
printf '# Decisions\n\n## 2026-09-25 - First\n' >memory/decisions.md
git add memory/decisions.md

ok "clean commit passes the hooks"    git commit -q -m "chore(docs): add the decision ledger"
git commit -q --allow-empty -m "feat: x

Co-Authored-By: Claude <noreply@anthropic.com>" >/dev/null 2>&1 && { failed=$((failed + 1)); echo "FAIL: hook accepted a Claude trailer"; } || pass=$((pass + 1))
git commit -q --allow-empty -m "Add stuff" >/dev/null 2>&1 && { failed=$((failed + 1)); echo "FAIL: hook accepted a bad subject"; } || pass=$((pass + 1))
GIT_AUTHOR_NAME="Gemini" GIT_AUTHOR_EMAIL="g@example.com" git commit -q --allow-empty -m "feat: x" >/dev/null 2>&1 && { failed=$((failed + 1)); echo "FAIL: hook accepted an AI author"; } || pass=$((pass + 1))

printf 'x\n' >local.properties
git add local.properties
git commit -q -m "chore: add local properties" >/dev/null 2>&1 && { failed=$((failed + 1)); echo "FAIL: pre-commit accepted local.properties"; } || pass=$((pass + 1))
git reset -q
rm -f local.properties

printf '# Decisions\n\n## 2026-09-25 - Rewritten\n' >memory/decisions.md
git add memory/decisions.md
no "rewriting the ledger is refused" sh "$root/scripts/check-decisions.sh"
printf '# Decisions\n\n## 2026-09-25 - First\n\n## 2026-09-26 - Second\n' >memory/decisions.md
git add memory/decisions.md
ok "appending to the ledger is allowed" sh "$root/scripts/check-decisions.sh"
git reset -q --hard

sha=$(git rev-parse HEAD)
no "push to main is refused"      sh -c "printf 'refs/heads/main $sha refs/heads/main 0000000000000000000000000000000000000000\n' | sh '$root/.githooks/pre-push' origin url"
no "bad branch name is refused"   sh -c "printf 'refs/heads/mywork $sha refs/heads/mywork 0000000000000000000000000000000000000000\n' | sh '$root/.githooks/pre-push' origin url"
ok "good branch name is allowed"  sh -c "printf 'refs/heads/feat/1-x $sha refs/heads/feat/1-x 0000000000000000000000000000000000000000\n' | sh '$root/.githooks/pre-push' origin url"

echo "policy tests: $pass passed, $failed failed"
[ "$failed" -eq 0 ]
