#!/bin/sh
# Server side policy for one pull request. Runs in .github/workflows/policy.yml.
#
# It runs under pull_request_target with the code of the DEFAULT branch checked out, so a pull request
# cannot edit this script to pass its own check. It only reads the pull request through the API and
# never executes anything from the pull request.
#
# Environment: GH_TOKEN, REPO (owner/name), PR (pull request number).
# Rules: only the people in .github/team.txt may author commits; no co-author trailer or AI byline in
# commits, title or body; branch, title and commit subjects follow .github/NAMING.md; a pull request that
# deletes app code names an approved removal issue; memory/decisions.md only grows.
set -eu
here=${POLICY_SCRIPTS_DIR:-$(cd "$(dirname "$0")" && pwd)}
. "$here/lib/policy.sh"
team_file="$here/../.github/team.txt"

problems=0
TAB=$(printf '\t')
note() {
    echo "::error::$*"
    problems=$((problems + 1))
}

team_logins() {
    sed 's/#.*//' "$team_file" | awk 'NF { print tolower($1) }'
}

# is_team <login>
is_team() {
    [ -n "$1" ] && [ "$1" != "NONE" ] && team_logins | grep -qxF "$(printf '%s' "$1" | tr 'A-Z' 'a-z')"
}

# check_commit_row <sha> <author login> <committer login> <author name> <author email> <message base64>
check_commit_row() {
    _sha=$(printf '%s' "$1" | cut -c1-8)
    if ! is_team "$2"; then
        note "commit $_sha: author login '$2' is not on the team list (.github/team.txt). If the e-mail is not linked to a GitHub account the login shows as NONE."
    fi
    if ! is_team "$3" && [ "$3" != "$WEB_COMMITTER" ]; then
        note "commit $_sha: committer login '$3' is not on the team list."
    fi
    if ident_bad "$4 <$5>"; then
        note "commit $_sha: author identity '$4 <$5>' looks like an AI tool or a bot."
    fi
    _msg=$(printf '%s' "$6" | base64 -d)
    if ! _out=$(check_message "$_msg"); then
        note "commit $_sha: $(printf '%s' "$_out" | tr '\n' ' ')"
    fi
}

# check_files_row <filename> <status> <deletions> <pull request body>
check_files_row() {
    case "$1" in
        PUBGApp/app/src/main/*)
            if [ "$2" = "removed" ]; then
                issue=$(printf '%s\n' "$4" | sed -n 's/.*[Rr]emoval-[Ii]ssue:[[:space:]]*#\([0-9][0-9]*\).*/\1/p' | head -n 1)
                if [ -z "$issue" ]; then
                    note "deleted file $1: add a line 'Removal-Issue: #N' pointing to an issue labelled type:removal (AGENTS.md rule R4)."
                elif ! gh api "repos/$REPO/issues/$issue" --jq '[.labels[].name] | join(",")' | grep -q 'type:removal'; then
                    note "deleted file $1: issue #$issue does not carry the label type:removal."
                fi
            fi
            ;;
        memory/decisions.md)
            if [ "$2" = "removed" ] || [ "$3" != "0" ]; then
                note "memory/decisions.md is append-only, but this pull request removes or rewrites lines in it."
            fi
            ;;
    esac
}

main() {
    author=$(gh api "repos/$REPO/pulls/$PR" --jq '.user.login')
    head_ref=$(gh api "repos/$REPO/pulls/$PR" --jq '.head.ref')
    title=$(gh api "repos/$REPO/pulls/$PR" --jq '.title')
    body=$(gh api "repos/$REPO/pulls/$PR" --jq '.body // ""')

    is_team "$author" || note "pull request author '$author' is not on the team list."
    branch_ok "$head_ref" || note "branch '$head_ref' must look like type/issue-slug, for example feat/12-google-sign-in."
    subject_ok "$title" || note "pull request title '$title' must be a conventional commit subject: type(scope): summary."
    if byline_bad "$title
$body"; then
        note "the pull request title or body carries a Co-authored-by trailer or an AI byline."
    fi
    printf '%s\n' "$body" | grep -Eiq '(closes|refs) #[0-9]+|closes:[[:space:]]*none' ||
        note "the pull request body must say 'Closes #N' (or 'Refs #N', or 'Closes: none')."

    tmp=$(mktemp)
    trap 'rm -f "$tmp"' EXIT

    gh api --paginate "repos/$REPO/pulls/$PR/commits" --jq '.[] | [.sha, (.author.login // "NONE"), (.committer.login // "NONE"), .commit.author.name, .commit.author.email, (.commit.message | @base64)] | @tsv' >"$tmp"
    count=0
    while IFS="$TAB" read -r sha alogin clogin aname aemail msg64; do
        [ -n "$sha" ] || continue
        count=$((count + 1))
        check_commit_row "$sha" "$alogin" "$clogin" "$aname" "$aemail" "$msg64"
    done <"$tmp"
    echo "checked $count commit(s)"

    gh api --paginate "repos/$REPO/pulls/$PR/files" --jq '.[] | [.filename, .status, .deletions] | @tsv' >"$tmp"
    while IFS="$TAB" read -r file status deleted; do
        [ -n "$file" ] || continue
        check_files_row "$file" "$status" "$deleted" "$body"
    done <"$tmp"

    if [ "$problems" -gt 0 ]; then
        echo "policy failed with $problems problem(s)."
        exit 1
    fi
    echo "policy passed"
}

# Sourcing this file (the tests do) defines the functions without running the check.
if [ "${POLICY_SOURCE_ONLY:-0}" != "1" ]; then
    : "${GH_TOKEN:?}" "${REPO:?}" "${PR:?}"
    main
fi
