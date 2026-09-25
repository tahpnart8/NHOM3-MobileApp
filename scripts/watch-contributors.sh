#!/bin/sh
# Fails when GitHub lists a contributor who is not on the team list. Runs weekly and on every push to main.
# It detects a breach after the fact; it cannot prevent one. Environment: GH_TOKEN, REPO.
set -eu
here=$(cd "$(dirname "$0")" && pwd)
team_file="$here/../.github/team.txt"
: "${GH_TOKEN:?}" "${REPO:?}"

team=$(sed 's/#.*//' "$team_file" | awk 'NF { print tolower($1) }')
bad=0
for login in $(gh api --paginate "repos/$REPO/contributors?anon=1" --jq '.[] | (.login // .email)'); do
    if ! printf '%s\n' "$team" | grep -qxF "$(printf '%s' "$login" | tr 'A-Z' 'a-z')"; then
        echo "::error::contributor '$login' is not on the team list"
        bad=1
    fi
done
[ "$bad" -eq 0 ] && echo "contributors match the team list"
exit "$bad"
