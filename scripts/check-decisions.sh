#!/bin/sh
# memory/decisions.md is append-only. Refuse a staged change that deletes or rewrites a line of it.
set -eu
line=$(git diff --cached --numstat -- memory/decisions.md)
[ -n "$line" ] || exit 0
deleted=$(printf '%s\n' "$line" | awk '{print $2}')
if [ "$deleted" != "0" ]; then
    echo "policy: memory/decisions.md is append-only; this change removes or rewrites $deleted line(s)." >&2
    echo "  Add a new entry at the end instead, and mark the old one superseded with a Status section." >&2
    exit 1
fi
