#!/bin/sh
# .agents/skills is the source; .claude/skills is a mirror because Claude Code does not read .agents/.
#   sync-ai-skills.sh           copy .agents/skills over .claude/skills
#   sync-ai-skills.sh --check   fail when the two differ (used by CI)
set -eu
root=$(cd "$(dirname "$0")/.." && pwd)
src="$root/.agents/skills"
dst="$root/.claude/skills"

if [ "${1:-}" = "--check" ]; then
    if diff -r "$src" "$dst" >/dev/null 2>&1; then
        echo "skills mirror is in sync"
        exit 0
    fi
    echo "policy: .claude/skills differs from .agents/skills." >&2
    echo "  Edit .agents/skills, then run: sh scripts/sync-ai-skills.sh" >&2
    diff -rq "$src" "$dst" >&2 || true
    exit 1
fi

rm -rf "$dst"
mkdir -p "$dst"
cp -R "$src/." "$dst/"
echo "copied .agents/skills to .claude/skills"
