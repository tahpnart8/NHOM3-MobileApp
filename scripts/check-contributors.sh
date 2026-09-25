#!/bin/sh
# Local contributor policy: no AI byline, no co-author trailer, no AI or bot identity.
# The server side twin is scripts/ci-policy.sh. Both read the rules in scripts/lib/policy.sh.
#
#   check-contributors.sh message <file>          validate a commit message file (commit-msg hook)
#   check-contributors.sh identity                validate the git identity that will author commits
#   check-contributors.sh commit <sha>            validate one existing commit
#   check-contributors.sh unpushed <sha>          validate every commit of <sha> that no remote has (pre-push hook)
set -eu
here=$(cd "$(dirname "$0")" && pwd)
. "$here/lib/policy.sh"

refuse() {
    echo "" >&2
    echo "policy: refused. Contributors are limited to the 5 team members (see AGENTS.md rule R8)." >&2
    exit 1
}

check_commit() {
    _sha=$1
    _out=""
    _name=$(git log -1 --format='%an' "$_sha")
    _email=$(git log -1 --format='%ae' "$_sha")
    _cname=$(git log -1 --format='%cn' "$_sha")
    _cemail=$(git log -1 --format='%ce' "$_sha")
    _msg=$(git log -1 --format='%B' "$_sha")
    _o=$(check_identity "$_name" "$_email" author) || _out="$_out$_o
"
    _o=$(check_identity "$_cname" "$_cemail" committer) || _out="$_out$_o
"
    _o=$(check_message "$_msg") || _out="$_out$_o
"
    if [ -n "$_out" ]; then
        echo "policy: commit $(printf '%s' "$_sha" | cut -c1-8) has problems:" >&2
        printf '%s' "$_out" >&2
        return 1
    fi
    return 0
}

mode=${1:-}
case "$mode" in
    message)
        file=${2:?usage: check-contributors.sh message <file>}
        msg=$(grep -v '^#' "$file" || true)
        if ! out=$(check_message "$msg"); then
            echo "policy: this commit message is not accepted:" >&2
            echo "$out" >&2
            refuse
        fi
        ;;
    identity)
        name=$(git var GIT_AUTHOR_IDENT | sed 's/ <.*//')
        email=$(git var GIT_AUTHOR_IDENT | sed 's/.*<\(.*\)>.*/\1/')
        cname=$(git var GIT_COMMITTER_IDENT | sed 's/ <.*//')
        cemail=$(git var GIT_COMMITTER_IDENT | sed 's/.*<\(.*\)>.*/\1/')
        out=""
        o=$(check_identity "$name" "$email" author) || out="$out$o
"
        o=$(check_identity "$cname" "$cemail" committer) || out="$out$o
"
        if [ -n "$out" ]; then
            echo "policy: the git identity in use is not allowed:" >&2
            printf '%s' "$out" >&2
            echo "  Fix it with your own name and e-mail: git config user.name / user.email" >&2
            refuse
        fi
        ;;
    commit)
        check_commit "${2:?usage: check-contributors.sh commit <sha>}" || refuse
        ;;
    unpushed)
        fail=0
        for sha in $(git rev-list "${2:?usage: check-contributors.sh unpushed <sha>}" --not --remotes); do
            check_commit "$sha" || fail=1
        done
        [ "$fail" -eq 0 ] || refuse
        ;;
    *)
        echo "usage: check-contributors.sh message <file> | identity | commit <sha> | unpushed <sha>" >&2
        exit 2
        ;;
esac
