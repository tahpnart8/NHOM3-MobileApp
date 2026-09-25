#!/bin/sh
# Create or update the issue labels. Safe to run again. Needs the gh CLI logged in with write access.
set -eu

label() { # name, color, description
    gh label create "$1" --color "$2" --description "$3" --force >/dev/null
    echo "label: $1"
}

label "type:feature"  "0A6B68" "New behaviour a user can see"
label "type:bug"      "B3261E" "Behaviour that is wrong"
label "type:chore"    "6B7A86" "Build, tooling, dependencies, housekeeping"
label "type:docs"     "1F6FB2" "Documentation only"
label "type:removal"  "7A1F1F" "Deliberate removal of existing functionality, opened by the leader"

for area in auth listing feed chat order profile admin offline ui build docs; do
    label "area:$area" "C9D6DF" "Area: $area"
done

label "priority:p0" "B3261E" "Blocks the team"
label "priority:p1" "C77700" "Do this sprint"
label "priority:p2" "6B7A86" "Nice to have"
label "needs-leader-decision" "7B3FA0" "Waiting for a decision from the leader"
label "blocked" "5C5C5C" "Cannot proceed until something else is done"
