#!/bin/sh
# Style check for Java you added or changed. The rules are in .agents/skills/pubg-code/SKILL.md.
# It reports and exits 1 when it finds something; a person or an AI decides what to do about each line.
#
#   sh scripts/check-java-style.sh                  lines added compared with main (committed, uncommitted, new files)
#   sh scripts/check-java-style.sh --base <ref>     compare with another ref
#   sh scripts/check-java-style.sh --files <file>   every line of the given files
#
# Only added lines are checked in the first two modes, so old code is never a finding (AGENTS.md R3).
set -eu

mode=diff
base=""
files=""
while [ $# -gt 0 ]; do
    case "$1" in
        --base) base=$2; shift 2 ;;
        --files) mode=files; shift; files="$*"; break ;;
        *) echo "usage: check-java-style.sh [--base <ref>] | --files <file>..." >&2; exit 2 ;;
    esac
done

# Prints "file<TAB>line<TAB>text" for every line to check.
lines_from_diff() {
    if [ -z "$base" ]; then
        if git rev-parse --verify -q origin/main >/dev/null; then base=origin/main
        elif git rev-parse --verify -q main >/dev/null; then base=main
        else base=HEAD; fi
    fi
    fork=$(git merge-base "$base" HEAD)
    git diff --unified=0 "$fork" -- 'PUBGApp/*.java' | awk '
        /^\+\+\+ / { file = substr($0, 7); next }
        /^@@/ { start = substr($3, 2); sub(/,.*/, "", start); line = start + 0; next }
        /^\+/ { printf "%s\t%d\t%s\n", file, line, substr($0, 2); line++ }
    '
    for new_file in $(git ls-files --others --exclude-standard -- 'PUBGApp/*.java'); do
        lines_from_file "$new_file"
    done
}

lines_from_file() {
    awk '{ printf "%s\t%d\t%s\n", FILENAME, NR, $0 }' "$1"
}

check() {
    awk -F '\t' '
    function report(id, message) {
        printf "%s:%s: %s: %s\n", file, number, id, message
        found++
    }
    {
        file = $1
        number = $2
        text = $3
        for (i = 4; i <= NF; i++) text = text "\t" $i

        trimmed = text
        sub(/^[ \t]+/, "", trimmed)
        is_comment = (trimmed ~ /^(\/\/|\/\*|\*)/)

        # A comment block of three or more lines in a row.
        if (is_comment && file == last_file && number == last_number + 1 && last_was_comment) run++
        else if (is_comment) run = 1
        else run = 0
        if (run == 3) report("long-comment", "comment longer than 2 lines; keep the why in one or two lines, put lasting knowledge in memory/code/")
        last_file = file; last_number = number; last_was_comment = is_comment

        if (is_comment) {
            if (trimmed ~ /^\/\/.*[;{}][ \t]*$/) report("commented-code", "commented-out code; delete it, git keeps the history")
            if (trimmed ~ /^\/\/[ \t]*[-=*#~][-=*#~][-=*#~][-=*#~]+/ || trimmed ~ /^\/\*[ \t]*[-=*#~][-=*#~][-=*#~][-=*#~]+/) report("banner-comment", "decorative comment line")
            if (trimmed ~ /@author|Created by|Created on|Modified by/) report("history-comment", "author or date comment; git has it")
            if (trimmed ~ /(TODO|FIXME|XXX|HACK)/) report("todo", "open an issue or write it in memory/code/ instead of leaving a marker")
            next
        }

        code = text
        gsub(/"([^"\\]|\\.)*"/, "\"\"", code)
        sub(/\/\/.*$/, "", code)

        if (code ~ /->/) report("lambda", "lambda or arrow; use an anonymous class or a named method")
        if (code ~ /::/) report("method-reference", "method reference; call the method in an ordinary statement")
        if (code ~ /\.stream\(|\.parallelStream\(|Collectors\./) report("stream", "stream pipeline; use a for loop")
        if (code ~ /(^|[^A-Za-z0-9_])var[ \t]+[A-Za-z_]/) report("var", "var; write the type")
        if (text ~ /"""/) report("text-block", "text block; use a string resource or a plain string")
        if (code ~ /(^|[^A-Za-z0-9_])record[ \t]+[A-Z]/) report("record", "record; write a plain class")
        if (code ~ /instanceof[ \t]+[A-Za-z_.<>?]+[ \t]+[a-z][A-Za-z0-9_]*[ \t]*(\)|&&|\|\|)/) report("instanceof-pattern", "pattern variable; cast on the next line")
        if (code ~ /System\.(out|err)\.print|printStackTrace\(/) report("debug-output", "console output; use Log or show the error to the user")
        if (code ~ /catch[ \t]*\([^)]*\)[ \t]*\{[ \t]*\}/) report("empty-catch", "empty catch swallows an error")
        if (text ~ /(TODO|FIXME|XXX|HACK)/) report("todo", "open an issue or write it in memory/code/ instead of leaving a marker")
        if (length(text) > 120) report("long-line", "line longer than 120 characters")
    }
    END {
        if (found > 0) {
            printf "\n%d finding(s). Fix them, or say why a line is better as it is. Rules: .agents/skills/pubg-code/SKILL.md\n", found
            exit 1
        }
    }
    '
}

if [ "$mode" = files ]; then
    for path in $files; do
        lines_from_file "$path"
    done | check
else
    lines_from_diff | check
fi
