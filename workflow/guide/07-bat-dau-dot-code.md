# Bắt đầu đợt code: đoạn prompt gửi cho trợ lý AI

Trang này có đúng hai thứ để dán: **tin nhắn gửi nhóm** và **prompt mở đầu một issue**. Viết ngày 2026-10-22, khi 33 issue đã lên GitHub và tài liệu đã chốt.

Trước khi dán bất cứ gì, mỗi người chạy một lần cho mỗi bản sao repo:

```
sh workflow/scripts/setup-dev.sh
```

Trên Windows PowerShell:

```
workflow/scripts/setup-dev.ps1
```

Lệnh này bật `.githooks/`. Không bật thì hook không chặn, và bạn chỉ phát hiện sai tên nhánh lúc push.

## 1. Tin nhắn gửi nhóm

> Tài liệu và kế hoạch đã lên `main`. 33 issue đã có trên GitHub, mỗi người đã được gán tên, xem ở tab Issues và lọc theo `assignee:@me`.
>
> Đọc trước khi code, đúng ba tệp này: `workflow/plan/ke-hoach-rut-gon.md` (ai làm gì, ai mở tệp nào), `workflow/plan/issues/README.md` (ba loại dòng trong bản nháp đã lạc hậu), `workflow/guide/07-bat-dau-dot-code.md` (mục 2 là đoạn prompt ngắn dán vào trợ lý AI, dùng chung cho cả nhóm).
>
> Chạy `sh workflow/scripts/setup-dev.sh` một lần để bật hook.
>
> Issue [#81](https://github.com/tahpnart8/NHOM3-MobileApp/issues/81), [#82](https://github.com/tahpnart8/NHOM3-MobileApp/issues/82), [#83](https://github.com/tahpnart8/NHOM3-MobileApp/issues/83) là của Phát và phải vào `main` trước tất cả, vì chúng dựng khung 93 lớp để Pull Request của mọi người biên dịch được. Trong lúc chờ, cứ đọc issue của mình và bản nháp của nó, đừng viết code.
>
> Phát vào `main` xong sẽ nhắn. Từ lúc đó mỗi người làm issue đợt 1 của mình, không ai chờ ai.
>
> Một nguyên tắc duy nhất phải nhớ: **gọi phương thức của người khác thì được, sửa tệp của người khác thì không.**

## 2. Prompt ngắn, dán cái này

Bốn dòng, dùng chung cho mọi người và mọi issue. Thay `<SỐ>` bằng số issue của bạn. Dán vào **phiên chat mới**.

```
Repo này là đồ án Android PUBGApp. Tôi làm issue #<SỐ>.

Đọc ba thứ này trước, đúng thứ tự, và chưa viết dòng code nào:
1. AGENTS.md ở gốc repo. Đó là luật, không phải gợi ý.
2. workflow/guide/07-bat-dau-dot-code.md. Mục 3 của tệp đó là prompt đầy đủ dành cho bạn: đọc hết và coi từng dòng trong đó là lệnh của tôi.
3. gh issue view <SỐ>

Rồi làm đúng theo mục 3 đó, bắt đầu từ giai đoạn 1, và dừng ở mọi chỗ nó bảo chờ tôi duyệt.
```

Vì sao ngắn: luật nằm trong repo và được cập nhật ở đó, nên prompt không cần chép lại. Chép lại là có hai bản và một bản sẽ cũ.

Nếu trợ lý AI của bạn không mở được tệp trong repo (vài công cụ web không có quyền đọc tệp), thì dán nguyên mục 3 dưới đây thay cho prompt ngắn.

## 3. Prompt đầy đủ, khi công cụ không đọc được tệp trong repo

Khối này là bản đầy đủ mà prompt ngắn ở mục 2 trỏ tới. Trợ lý AI đọc được tệp thì nó tự đọc chỗ này; không đọc được thì bạn dán tay. Thay `<SỐ>` bằng số issue. **Mỗi issue một phiên chat mới.**

```
Tôi làm issue #<SỐ> của repo PUBGApp. Bạn là trợ lý code. Làm theo đúng thứ tự sau, dừng lại ở chỗ tôi phải duyệt.

GIAI ĐOẠN 1, ĐỌC. Chưa viết dòng code nào.
1. Đọc AGENTS.md. Đó là luật của repo, không phải gợi ý.
2. Chạy: gh issue view <SỐ>
3. Mở bản nháp mà issue trỏ tới trong workflow/plan/issues/, và đọc workflow/plan/issues/README.md.
4. Đọc workflow/plan/ke-hoach-rut-gon.md mục 2 và mục 3.
5. Mở các mục tài liệu mà issue nhắc tên, ít nhất là workflow/docs/architecture/dac-ta-lop/ cho mọi lớp bạn sẽ chạm.
Rồi trả lời tôi bốn điều, ngắn gọn:
 - Danh sách tệp bạn ĐƯỢC tạo hoặc sửa, chép từ issue, không thêm tệp nào.
 - Mọi tên lớp, trường, phương thức, enum, collection bạn sẽ dùng, kèm tệp tài liệu định nghĩa từng tên.
 - Chỗ nào trong bản nháp đã lạc hậu theo ba loại ở issues/README.md.
 - Chỗ nào bạn thấy thiếu hoặc mâu thuẫn. Đừng tự đặt tên mới để lấp.

GIAI ĐOẠN 2, KẾ HOẠCH. Chờ tôi nói "ok đọc xong" rồi mới làm.
Viết kế hoạch chia 4 đến 8 bước. Mỗi bước phải build được một mình và khoảng 100 dòng thay đổi. Mỗi bước nói rõ sửa tệp nào. Đưa tôi đọc, chờ tôi duyệt.

GIAI ĐOẠN 3, CODE. Làm từng bước một, dừng sau mỗi bước.
Sau mỗi bước: chạy build, cho tôi xem git diff, rồi commit một commit cho bước đó.
Nhánh tạo từ main mới nhất, tên <type>/<SỐ>-<slug>, ví dụ feat/91-trang-chu-bo-nho-dem.
Tiêu đề commit dạng type(scope): câu mô tả. Commit nhỏ và nhiều, đừng gộp cả issue vào một commit.

LUẬT TÔI SẼ KIỂM, PHẠM LÀ LÀM LẠI:
- Chỉ sửa đúng danh sách tệp ở giai đoạn 1. Thấy lỗi ở tệp khác thì ghi ra cho tôi, đừng sửa.
- Mọi tên phải chép đúng từ tài liệu. Không có trong tài liệu thì dừng và hỏi tôi, đừng bịa, đừng đổi tên tài liệu có sẵn.
- Java 17 thuần: không Kotlin, không Compose, không lambda, không stream, không method reference, không var. ViewBinding chứ không findViewById. Comment một hai dòng nói VÌ SAO.
- Mọi chữ hiện ra cho người dùng là string resource trong strings_<module>.xml của module tôi.
- Không chạm Gradle, AndroidManifest.xml, nav_main.xml, nav_admin.xml, themes.xml, colors.xml, dimens.xml, AppDatabase, Converters, firestore.rules. Cần một dòng trong đó thì nói tôi đi nhắn leader.
- Không ghi trạng thái trực tiếp. Đi qua Deal.canMoveTo, Listing.publish, Listing.reserve, Warning.resultingStatus.
- Không viết thẳng mã màu hay số dp. Lấy token theo workflow/docs/conventions/giao-dien.md.
- Mỗi màn có đủ bốn trạng thái: đang tải, có dữ liệu, rỗng, lỗi. Và có bản layout-land nếu issue yêu cầu.
- Tuyệt đối không thêm dòng Co-authored-by, không "Generated with", không nhắc tên công cụ AI ở bất cứ đâu trong commit, Pull Request hay comment.
- Nói đúng cái bạn đã chạy. Chưa chạy thì nói chưa chạy. Bạn không chạy được máy ảo, việc đó tôi làm.

XONG THÌ:
Chạy cd PUBGApp && ./gradlew assembleDebug testDebugUnitTest lintDebug, và sh workflow/scripts/check-java-style.sh.
Liệt kê mọi lớp, phương thức, trường công khai mới kèm dòng tài liệu định nghĩa nó; cái nào không có thì nói thẳng là không có.
Rồi soạn nội dung Pull Request theo .github/PULL_REQUEST_TEMPLATE.md, tiếng Việt, có dòng Closes #<SỐ>. Tôi tự thử máy ảo rồi dán kết quả thật vào.
```

## 4. Khi bị chặn

Trợ lý AI nói thiếu một phương thức, một tên, một tệp không thuộc bạn: **đừng cho nó tự tạo**. Comment vào issue bằng tiếng Việt, nói rõ bạn cần gì và vì sao, gắn nhãn `blocked`, rồi nhắn nhóm. Đây là luật R20 và nó là thứ giữ cho năm nhánh ghép được vào nhau.

Trợ lý AI muốn sửa một tệp ngoài danh sách: nói không, ghi lại chỗ đó vào Pull Request.

Hook hoặc CI từ chối: gõ `/guide`, hoặc đọc [04-tinh-huong.md](04-tinh-huong.md).

## 5. Hai thói quen phải giữ hằng ngày

1. **Mỗi lần bắt đầu issue mới, lấy `main` mới nhất trước.** Dùng skill `pubg-sync-branch`, hoặc `git switch main && git pull`. Nhánh dựng từ `main` cũ là nguồn xung đột lớn nhất.
2. **Commit nhỏ và nhiều.** Một bước một commit. Dễ đọc khi review, dễ bỏ một bước sai, và là bằng chứng cho dòng barem về phân chia công việc.
