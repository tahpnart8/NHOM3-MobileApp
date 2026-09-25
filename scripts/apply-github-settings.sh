#!/bin/sh
# Apply the repository settings that back the contributor policy. Leader only (needs admin). Safe to run again.
#
#   sh scripts/apply-github-settings.sh           apply, then print what GitHub reports back
#   sh scripts/apply-github-settings.sh --show    only print what GitHub reports
#
# Two rulesets on main, split on purpose:
#   main-checks  no deletion, no force push, linear history, `build` and `policy` green and up to date.
#                Nobody bypasses it, the leader included.
#   main-review  pull request required, 1 approval from a code owner, squash only. The admin role may
#                bypass it inside a pull request only, because the leader cannot approve their own PR.
# Squash merges use the pull request title and an EMPTY body, so no trailer from a branch commit can
# reach main.
set -eu
root=$(cd "$(dirname "$0")/.." && pwd)
repo=${REPO:-$(gh repo view --json nameWithOwner --jq .nameWithOwner)}

show() {
    echo "== repository"
    gh api "repos/$repo" --jq '{visibility, allow_squash_merge, allow_merge_commit, allow_rebase_merge, squash_merge_commit_title, squash_merge_commit_message, delete_branch_on_merge, allow_update_branch, allow_auto_merge, has_wiki, has_discussions, secret_scanning: .security_and_analysis.secret_scanning.status, push_protection: .security_and_analysis.secret_scanning_push_protection.status}'
    echo "== rulesets"
    for id in $(gh api "repos/$repo/rulesets" --jq '.[].id'); do
        gh api "repos/$repo/rulesets/$id" --jq '{name, enforcement, bypass: [.bypass_actors[] | "\(.actor_type):\(.actor_id):\(.bypass_mode)"], rules: [.rules[].type]}'
    done
    echo "== actions"
    gh api "repos/$repo/actions/permissions/workflow"; echo
    echo "== collaborators"
    gh api "repos/$repo/collaborators" --jq '.[] | "\(.login) \(.role_name)"'
    echo "== dependabot security updates"
    gh api "repos/$repo/automated-security-fixes" --jq '.enabled' 2>/dev/null || echo "not available"
    echo "== interaction limit"
    gh api "repos/$repo/interaction-limits" --jq 'if .limit then "\(.limit) until \(.expires_at)" else "none" end' 2>/dev/null || echo "none"
}

if [ "${1:-}" = "--show" ]; then
    show
    exit 0
fi

echo "applying to $repo"

gh api -X PATCH "repos/$repo" --silent \
    -F allow_squash_merge=true -F allow_merge_commit=false -F allow_rebase_merge=false \
    -f squash_merge_commit_title=PR_TITLE -f squash_merge_commit_message=BLANK \
    -F delete_branch_on_merge=true -F allow_update_branch=true -F allow_auto_merge=false \
    -F has_wiki=false -F has_discussions=false
echo "merge settings: squash only, title only, delete branch after merge"

printf '{"security_and_analysis":{"secret_scanning":{"status":"enabled"},"secret_scanning_push_protection":{"status":"enabled"}}}' |
    gh api -X PATCH "repos/$repo" --silent --input - && echo "secret scanning and push protection: on" ||
    echo "secret scanning: GitHub refused (it may not be offered for this repository); continuing"

# Dependabot security updates open pull requests as dependabot[bot], which would be a contributor.
gh api -X DELETE "repos/$repo/automated-security-fixes" --silent && echo "dependabot security updates: off" ||
    echo "dependabot security updates: could not turn off; check Settings > Code security"

printf '{"default_workflow_permissions":"read","can_approve_pull_request_reviews":false}' |
    gh api -X PUT "repos/$repo/actions/permissions/workflow" --silent --input -
echo "actions: read-only token, cannot approve pull requests"

printf '{"approval_policy":"all_external_contributors"}' |
    gh api -X PUT "repos/$repo/actions/permissions/fork-pr-contributor-approval" --silent --input - &&
    echo "actions: workflows from outside contributors wait for approval" ||
    echo "actions: could not set fork approval policy; set it in Settings > Actions"

# Collaborators only may open issues, pull requests and comments. GitHub caps this at six months.
printf '{"limit":"collaborators_only","expiry":"six_months"}' |
    gh api -X PUT "repos/$repo/interaction-limits" --silent --input - &&
    echo "interactions: collaborators only, six months (run this script again to renew)" ||
    echo "interactions: could not set the limit; set it in Settings > Moderation options"

for file in "$root"/.github/rulesets/*.json; do
    name=$(sed -n 's/.*"name": *"\([^"]*\)".*/\1/p' "$file" | head -n 1)
    id=$(gh api "repos/$repo/rulesets" --jq ".[] | select(.name == \"$name\") | .id")
    if [ -n "$id" ]; then
        gh api -X PUT "repos/$repo/rulesets/$id" --silent --input "$file"
        echo "ruleset $name: updated"
    else
        gh api -X POST "repos/$repo/rulesets" --silent --input "$file"
        echo "ruleset $name: created"
    fi
done

echo
show
