#!/bin/sh
# Shared rules for the contributor and naming policy. POSIX sh only (Git for Windows, dash on CI).
# Sourced by check-contributors.sh (local hooks) and ci-policy.sh (GitHub Actions).
# Naming grammar is documented in .github/NAMING.md; keep the two in step.

TYPES='feat|fix|docs|refactor|test|chore|build|ci'
SUBJECT_RE="^(${TYPES})(\\([a-z0-9-]+\\))?: [a-z0-9].*[^.]\$"
BRANCH_RE="^(${TYPES})/[0-9]+-[a-z0-9]+(-[a-z0-9]+)*\$"
# The branch GitHub creates when someone presses Revert on a merged pull request.
REVERT_BRANCH_RE='^revert-[0-9]+-'

# A name or e-mail that identifies an AI tool or a bot. Matched against author and committer identities only.
IDENT_RE='claude|anthropic|gemini|antigravity|copilot|codex|openai|chatgpt|windsurf|devin|aider|\[bot\]|-bot@|^bot$'

# AI tool names, for recognising a byline such as "Generated with Claude Code". A plain mention of a tool
# ("update the claude code settings") is allowed; a byline that credits one is not.
AI_RE='claude|anthropic|gemini|antigravity|copilot|codex|openai|chatgpt|gpt-[0-9]|windsurf|devin|aider'

# An AI byline or any co-author trailer inside a commit message or a pull request text.
BYLINE_RE="^[[:space:]]*co-authored-by[[:space:]]*:|(generated|created|written|made|authored|assisted)( [a-z]+)? (with|by|using) .*(${AI_RE})|noreply@anthropic\\.com|\\[bot\\]|🤖"

# GitHub itself commits as web-flow when someone uses the web UI (Update branch button).
WEB_COMMITTER='web-flow'

# subject_ok <subject>: conventional subject, 72 characters at most.
subject_ok() {
    case "$1" in
        Merge\ *|Revert\ *) return 0 ;;
    esac
    [ "${#1}" -le 72 ] || return 1
    printf '%s\n' "$1" | LC_ALL=C grep -Eq "$SUBJECT_RE"
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
        echo "    expected 'type(scope): lowercase imperative summary', 72 characters at most, no trailing period."
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
