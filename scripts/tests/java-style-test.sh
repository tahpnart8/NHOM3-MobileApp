#!/bin/sh
# Proves the Java style check finds what it claims to find and leaves clean code and old code alone.
# Run: sh scripts/tests/java-style-test.sh      Exit code 0 means every case behaved as expected.
root=$(cd "$(dirname "$0")/../.." && pwd)
checker="$root/scripts/check-java-style.sh"
pass=0
failed=0

# flags <description> <rule id> <java line>: the line must be reported with that rule id.
flags() {
    printf 'class A {\n%s\n}\n' "$3" >"$dir/A.java"
    if sh "$checker" --files "$dir/A.java" 2>&1 | grep -q ": $2:"; then
        pass=$((pass + 1))
    else
        failed=$((failed + 1))
        echo "FAIL (expected $2): $1"
    fi
}

# clean <description> <java line>: the line must not be reported.
clean() {
    printf 'class A {\n%s\n}\n' "$2" >"$dir/A.java"
    if sh "$checker" --files "$dir/A.java" >/dev/null 2>&1; then
        pass=$((pass + 1))
    else
        failed=$((failed + 1))
        echo "FAIL (expected clean): $1"
    fi
}

dir=$(mktemp -d)
trap 'rm -rf "$dir"' EXIT

flags "lambda"              lambda             '    Runnable r = () -> run();'
flags "arrow in a switch"   lambda             '    case 1 -> name = "a";'
flags "method reference"    method-reference   '    list.forEach(System.out::println);'
flags "stream"              stream             '    List<String> b = a.stream().sorted().toList();'
flags "collectors"          stream             '    x = Collectors.toList();'
flags "var"                 var                '    var name = "a";'
flags "text block"          text-block         '    String s = """'
flags "record"              record             '    record Point(int x, int y) { }'
flags "instanceof pattern"  instanceof-pattern '    if (o instanceof String text) { }'
flags "println"             debug-output       '    System.out.println("x");'
flags "printStackTrace"     debug-output       '    e.printStackTrace();'
flags "empty catch"         empty-catch        '    try { run(); } catch (Exception e) { }'
flags "todo in code"        todo               '    int a = 1; // TODO fix'
flags "todo comment"        todo               '    // TODO later'
flags "commented-out code"  commented-code     '    // list.add(item);'
flags "banner"              banner-comment     '    // ==========='
flags "author comment"      history-comment    '    // Created by someone'
flags "long line"           long-line          "    String s = \"$(printf 'a%.0s' $(seq 1 130))\";"

clean "plain statement"          '    int total = price * count;'
clean "one line comment"         '    // Firestore rules are not filters, so the query names the status.'
clean "anonymous class"          '    button.setOnClickListener(new View.OnClickListener() {'
clean "arrow inside a string"    '    String s = "a -> b";'
clean "arrow in a trailing note" '    int a = 1; // maps a -> b'
clean "colons in a string"       '    String s = "std::vector";'
clean "generic word var"         '    int variable = 1;'
clean "cast instead of pattern"  '    String text = (String) value;'
clean "catch that reports"       '    try { run(); } catch (Exception e) { show(e); }'

# three comment lines in a row
printf 'class A {\n    // one\n    // two\n    // three\n}\n' >"$dir/A.java"
if sh "$checker" --files "$dir/A.java" 2>&1 | grep -q ": long-comment:"; then pass=$((pass + 1)); else failed=$((failed + 1)); echo "FAIL: three comment lines"; fi
printf 'class A {\n    // one\n    // two\n}\n' >"$dir/A.java"
if sh "$checker" --files "$dir/A.java" >/dev/null 2>&1; then pass=$((pass + 1)); else failed=$((failed + 1)); echo "FAIL: two comment lines should pass"; fi

# diff mode: only added lines count, and new untracked files are read whole
repo="$dir/repo"
git init -q "$repo"
cd "$repo" || exit 1
git config user.name "Test"
git config user.email "test@example.com"
git config commit.gpgsign false
mkdir -p PUBGApp/app/src/main/java
printf 'class Old {\n    Runnable r = () -> run();\n}\n' >PUBGApp/app/src/main/java/Old.java
git add . && git commit -q -m "chore: base"

if sh "$checker" --base HEAD >/dev/null 2>&1; then pass=$((pass + 1)); else failed=$((failed + 1)); echo "FAIL: an old lambda must not be reported"; fi

printf 'class Old {\n    Runnable r = () -> run();\n    int total = 1;\n}\n' >PUBGApp/app/src/main/java/Old.java
if sh "$checker" --base HEAD >/dev/null 2>&1; then pass=$((pass + 1)); else failed=$((failed + 1)); echo "FAIL: a clean added line next to an old lambda"; fi

printf 'class Old {\n    Runnable r = () -> run();\n    Runnable s = () -> run();\n}\n' >PUBGApp/app/src/main/java/Old.java
out=$(sh "$checker" --base HEAD 2>&1 || true)
if printf '%s' "$out" | grep -q "Old.java:3: lambda" && ! printf '%s' "$out" | grep -q "Old.java:2:"; then pass=$((pass + 1)); else failed=$((failed + 1)); echo "FAIL: only the added line 3 must be reported: $out"; fi

git checkout -q -- .
printf 'class Fresh {\n    var x = 1;\n}\n' >PUBGApp/app/src/main/java/Fresh.java
if sh "$checker" --base HEAD 2>&1 | grep -q "Fresh.java:2: var"; then pass=$((pass + 1)); else failed=$((failed + 1)); echo "FAIL: a new untracked file must be read whole"; fi

echo "java style tests: $pass passed, $failed failed"
[ "$failed" -eq 0 ]
