#!/bin/sh
# Shared rules for the contributor and naming policy. POSIX sh only (Git for Windows, dash on CI).
# Sourced by check-contributors.sh (local hooks) and ci-policy.sh (GitHub Actions).
# Naming grammar is documented in .github/NAMING.md; keep the two in step.

TYPES='feat|fix|docs|refactor|test|chore|build|ci'
# The summary after the colon may be English or Vietnamese: it must not start with a space or an ASCII
# capital, and must not end with a period. Checked with LC_ALL=C, so accented letters are just bytes.
SUBJECT_RE="^(${TYPES})(\\([a-z0-9-]+\\))?: [^[:space:][:upper:]].*[^.]\$"
# A subject is at most this many BYTES: about 72 English characters or 55 Vietnamese ones. Bytes, because
# dash (CI) counts bytes and bash (Git for Windows) counts characters, and both must give the same answer.
SUBJECT_MAX_BYTES=100

# The UTF-8 lead bytes of Vietnamese letters (U+00C0 to U+01B0 use C3, C4, C6; U+1EA0 to U+1EF9 use E1 BA or
# E1 BB) and of combining marks (CC, CD), for text typed in decomposed form. Symbols such as arrows, dashes
# and emoji start with E2 or F0 and are not counted.
VI_LEAD=$(printf '[\303\304\306\314\315]|\341[\272\273]')
BRANCH_RE="^(${TYPES})/[0-9]+-[a-z0-9]+(-[a-z0-9]+)*\$"
# The branch GitHub creates when someone presses Revert on a merged pull request.
REVERT_BRANCH_RE='^revert-[0-9]+-'

# A name or e-mail that identifies an AI tool or a bot. Matched against author and committer identities only.
IDENT_RE='claude|anthropic|gemini|antigravity|copilot|codex|openai|chatgpt|windsurf|devin|aider|\[bot\]|-bot@|^bot$'

# AI tool names, for recognising a byline such as "Generated with Claude Code". A plain mention of a tool
# ("update the claude code settings") is allowed; a byline that credits one is not.
AI_RE='claude|anthropic|gemini|antigravity|copilot|codex|openai|chatgpt|gpt-[0-9]|windsurf|devin|aider'

# An AI byline or any co-author trailer inside a commit message or a pull request text.
BYLINE_RE="^[[:space:]]*co-authored-by[[:space:]]*:|(generated|created|written|made|authored|assisted)( [a-z]+)? (with|by|using) .*(${AI_RE})|(tạo|viết|soạn|sinh)[^.]{0,20} (bởi|bằng|với|nhờ) .*(${AI_RE})|noreply@anthropic\\.com|\\[bot\\]|🤖"

# GitHub itself commits as web-flow when someone uses the web UI (Update branch button).
WEB_COMMITTER='web-flow'

# subject_ok <subject>: conventional subject, at most SUBJECT_MAX_BYTES bytes.
subject_ok() {
    case "$1" in
        Merge\ *|Revert\ *) return 0 ;;
    esac
    _bytes=$(printf '%s' "$1" | LC_ALL=C wc -c | tr -d ' ')
    [ "$_bytes" -le "$SUBJECT_MAX_BYTES" ] || return 1
    printf '%s\n' "$1" | LC_ALL=C grep -Eq "$SUBJECT_RE"
}

# vi_marks <text>: how many Vietnamese accented letters the text holds.
vi_marks() {
    printf '%s\n' "$1" | LC_ALL=C grep -Eo "$VI_LEAD" | wc -l | tr -d ' '
}

# vi_text_ok <text> <minimum>: true when the text holds at least <minimum> Vietnamese accented letters, which
# is how the policy tells Vietnamese prose from English. It is a heuristic, not a translator: it cannot tell
# good Vietnamese from bad, only that the text is written in it.
vi_text_ok() {
    [ "$(vi_marks "$1")" -ge "$2" ]
}

# branch_ok <branch name>
branch_ok() {
    printf '%s\n' "$1" | grep -Eq "$BRANCH_RE|$REVERT_BRANCH_RE"
}

# ident_bad <text>: true when the text names an AI tool or a bot.
ident_bad() {
    printf '%s\n' "$1" | grep -Eiq "$IDENT_RE"
}

# byline_bad <text>: true when the text carries an AI byline or a co-author trailer.
byline_bad() {
    printf '%s\n' "$1" | grep -Eiq "$BYLINE_RE"
}

# check_message <full commit message>: prints each problem, returns 1 when there is any.
check_message() {
    _msg=$1
    _bad=0
    _subject=$(printf '%s\n' "$_msg" | sed -n '1p')
    if ! subject_ok "$_subject"; then
        echo "  bad subject: '$_subject'"
        echo "    expected 'type(scope): summary', English or Vietnamese, not starting with a capital, no trailing period, at most $SUBJECT_MAX_BYTES bytes (about 55 Vietnamese letters)."
        echo "    types: $(printf '%s' "$TYPES" | tr '|' ' ')"
        _bad=1
    fi
    if byline_bad "$_msg"; then
        echo "  the message carries a Co-authored-by trailer or an AI byline."
        echo "    Only the 5 team members may appear as contributors. Remove that line."
        _bad=1
    fi
    return $_bad
}

# check_identity <name> <email> <label>: prints a problem when the identity looks like an AI tool or a bot.
check_identity() {
    if ident_bad "$1 <$2>"; then
        echo "  $3 identity '$1 <$2>' looks like an AI tool or a bot."
        return 1
    fi
    return 0
}
