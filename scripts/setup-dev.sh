#!/bin/sh
# One time setup per clone: turns on the git hooks and the commit template, and checks your git identity.
# Windows users run scripts/setup-dev.ps1 instead.
set -eu
root=$(cd "$(dirname "$0")/.." && pwd)
cd "$root"

git config core.hooksPath .githooks
git config commit.template .github/COMMIT_TEMPLATE.md
git config pull.ff only
git config fetch.prune true

name=$(git config user.name || true)
email=$(git config user.email || true)
if [ -z "$name" ] || [ -z "$email" ]; then
    echo "Set your own identity first, then run this script again:" >&2
    echo "  git config --global user.name  \"Your Name\"" >&2
    echo "  git config --global user.email \"the e-mail linked to your GitHub account\"" >&2
    exit 1
fi
sh scripts/check-contributors.sh identity

echo "Done. Hooks are active for this clone (core.hooksPath = .githooks)."
echo "Commits are authored as: $name <$email>"
echo "Make sure that e-mail is added to your GitHub account, or CI will reject your commits."
