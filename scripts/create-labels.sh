#!/bin/sh
# Create or update the issue labels (names in English so tools can read them, descriptions in Vietnamese).
# Safe to run again. Needs the gh CLI logged in with write access.
set -eu

label() { # name, color, description
    gh label create "$1" --color "$2" --description "$3" --force >/dev/null
    echo "label: $1"
}

label "type:feature"  "0A6B68" "Hành vi mới mà người dùng nhìn thấy được"
label "type:bug"      "B3261E" "Hành vi đang sai"
label "type:chore"    "6B7A86" "Build, công cụ, thư viện, dọn dẹp"
label "type:docs"     "1F6FB2" "Chỉ sửa tài liệu"
label "type:removal"  "7A1F1F" "Chủ ý gỡ chức năng đã có, do leader mở"

for area in auth listing feed chat order profile admin offline ui build docs; do
    label "area:$area" "C9D6DF" "Khu vực: $area"
done

label "priority:p0" "B3261E" "Đang chặn cả nhóm"
label "priority:p1" "C77700" "Làm sớm"
label "priority:p2" "6B7A86" "Có thì tốt"
label "needs-leader-decision" "7B3FA0" "Đang chờ leader quyết định"
label "blocked" "5C5C5C" "Chưa làm được cho tới khi việc khác xong"
