#!/bin/sh
# Server side policy for one pull request. Runs in .github/workflows/policy.yml.
#
# It runs under pull_request_target with the code of the DEFAULT branch checked out, so a pull request
# cannot edit this script to pass its own check. It only reads the pull request through the API and
# never executes anything from the pull request.
#
# Environment: GH_TOKEN, REPO (owner/name), PR (pull request number).
# Rules: only the people in .github/team.txt may author commits; no AI byline and no co-author trailer
# except a team member's GitHub noreply address (what "Commit suggestion" writes); branch, title and commit
# subjects follow .github/NAMING.md; a pull request that deletes or renames app code names an approved
# removal issue; Gradle files change only in a build or chore pull request; memory/decisions.md only grows.
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

# strip_comments <text>: removes <!-- ... --> blocks, so the hints in the pull request template (which
# mention "Closes: none" and "Removal-Issue") cannot satisfy a check on their own.
strip_comments() {
    printf '%s\n' "$1" | awk '{
        line = $0; out = ""
        while (length(line) > 0) {
            if (incomment) {
                i = index(line, "-->")
                if (i == 0) { line = "" } else { line = substr(line, i + 3); incomment = 0 }
            } else {
                i = index(line, "<!--")
                if (i == 0) { out = out line; line = "" } else { out = out substr(line, 1, i - 1); line = substr(line, i + 4); incomment = 1 }
            }
        }
        print out
    }'
}

# body_links_issue <body without comments>
body_links_issue() {
    printf '%s\n' "$1" | grep -Eiq '(closes|refs) #[0-9]+|closes:[[:space:]]*none|^reverts [^ ]*#[0-9]+'
}

# without_team_coauthors <message>: drops the co-author trailers GitHub writes when a team member's review
# suggestion is committed. They use the member's noreply address, <id>+<login>@users.noreply.github.com.
# Any other co-author trailer stays in the message and is rejected.
without_team_coauthors() {
    _logins=$(team_logins | paste -sd '|' -)
    printf '%s\n' "$1" | grep -Eiv "^[[:space:]]*co-authored-by[[:space:]]*:.*<[0-9]+\\+(${_logins})@users\\.noreply\\.github\\.com>[[:space:]]*\$" || true
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
    _msg=$(without_team_coauthors "$_msg")
    if ! _out=$(check_message "$_msg"); then
        note "commit $_sha: $(printf '%s' "$_out" | tr '\n' ' ')"
    fi
}

# require_removal_issue <what> <pull request body>
require_removal_issue() {
    issue=$(printf '%s\n' "$2" | sed -n 's/.*[Rr]emoval-[Ii]ssue:[[:space:]]*#\([0-9][0-9]*\).*/\1/p' | head -n 1)
    if [ -z "$issue" ]; then
        note "$1: add a line 'Removal-Issue: #N' pointing to an issue labelled type:removal (AGENTS.md rule R4)."
    elif ! gh api "repos/$REPO/issues/$issue" --jq '[.labels[].name] | join(",")' 2>/dev/null | grep -q 'type:removal'; then
        note "$1: issue #$issue does not exist or does not carry the label type:removal."
    fi
}

# check_files_row <filename> <status> <deletions> <previous filename> <pull request title> <pull request body>
check_files_row() {
    case "$1" in
        PUBGApp/app/src/main/*)
            if [ "$2" = "removed" ]; then
                require_removal_issue "deleted file $1" "$6"
            elif [ "$2" = "modified" ] && [ "$3" -gt 40 ]; then
                echo "::warning::$1 loses $3 lines. The reviewer should confirm no designed behaviour was removed (AGENTS.md rule R4)."
            fi
            ;;
    esac
    case "$4" in
        PUBGApp/app/src/main/*)
            if [ "$2" = "renamed" ]; then
                require_removal_issue "renamed file $4 to $1" "$6"
            fi
            ;;
    esac
    case "$1" in
        PUBGApp/gradle/libs.versions.toml | PUBGApp/gradle/wrapper/* | PUBGApp/*.gradle.kts | PUBGApp/app/*.gradle.kts | PUBGApp/gradle.properties | PUBGApp/gradle/gradle-daemon-jvm.properties)
            printf '%s\n' "$5" | grep -Eq '^(build|chore)[(:]' ||
                note "$1 changes the build or its dependencies, which happens only in a pull request titled build(...) or chore(deps) (AGENTS.md rule R5)."
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
    raw_body=$(gh api "repos/$REPO/pulls/$PR" --jq '.body // ""')
    body=$(strip_comments "$raw_body")

    is_team "$author" || note "pull request author '$author' is not on the team list."
    branch_ok "$head_ref" || note "branch '$head_ref' must look like type/issue-slug, for example feat/12-google-sign-in."
    subject_ok "$title" || note "pull request title '$title' must be a conventional commit subject: type(scope): summary."
    if byline_bad "$title
$raw_body"; then
        note "the pull request title or body carries a Co-authored-by trailer or an AI byline."
    fi
    body_links_issue "$body" ||
        note "the pull request body must say 'Closes #N' (or 'Refs #N', or 'Closes: none') outside the template comments."

    # Issues and pull requests are read by people, so they are written in Vietnamese (AGENTS.md R14). The
    # type and scope of the title and the keywords Closes, Refs, Removal-Issue and Deviation stay English.
    case "$title" in
        Revert\ * | Merge\ *) ;;
        *)
            vi_text_ok "${title#*: }" 1 ||
                note "tiêu đề PR phải viết bằng tiếng Việt có dấu (type và scope giữ tiếng Anh): '$title'"
            vi_text_ok "$body" 12 ||
                note "mô tả PR phải viết bằng tiếng Việt có dấu (các từ khóa Closes, Removal-Issue, Deviation giữ nguyên)."
            ;;
    esac

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

    gh api --paginate "repos/$REPO/pulls/$PR/files" --jq '.[] | [.filename, .status, .deletions, (.previous_filename // "-")] | @tsv' >"$tmp"
    while IFS="$TAB" read -r file status deleted previous; do
        [ -n "$file" ] || continue
        check_files_row "$file" "$status" "$deleted" "$previous" "$title" "$body"
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
