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
> Đọc trước khi code, đúng ba tệp này: `workflow/plan/ke-hoach-rut-gon.md` (ai làm gì, ai mở tệp nào), `workflow/plan/issues/README.md` (ba loại dòng trong bản nháp đã lạc hậu), `workflow/guide/07-bat-dau-dot-code.md` (mục 2 là đoạn prompt dán vào trợ lý AI, dùng chung cho cả nhóm).
>
> Chạy `sh workflow/scripts/setup-dev.sh` một lần để bật hook.
>
> Issue [#81](https://github.com/tahpnart8/NHOM3-MobileApp/issues/81), [#82](https://github.com/tahpnart8/NHOM3-MobileApp/issues/82), [#83](https://github.com/tahpnart8/NHOM3-MobileApp/issues/83) là của Phát và phải vào `main` trước tất cả, vì chúng dựng khung 93 lớp để Pull Request của mọi người biên dịch được. Trong lúc chờ, cứ đọc issue của mình và bản nháp của nó, đừng viết code.
>
> Phát vào `main` xong sẽ nhắn. Từ lúc đó mỗi người dán đoạn prompt ở mục 2 của trang guide trên vào một phiên chat mới, trợ lý AI sẽ tự tra issue của bạn và làm tuần tự. Không ai chờ ai.
>
> Một nguyên tắc duy nhất phải nhớ: **gọi phương thức của người khác thì được, sửa tệp của người khác thì không.**

## 2. Prompt kích hoạt phiên làm việc, dán cái này

Một khối, dùng chung cho cả bốn người, **không có số issue**. Dán vào một phiên chat mới và trợ lý AI tự tra xem issue nào được gán cho bạn, tự sắp thứ tự theo kế hoạch, rồi làm liên tục hết issue này sang issue khác, mỗi issue một Pull Request.

Prompt không chép luật vào mà bảo nó lên repo đọc. Sửa luật trong repo là cả nhóm nhận bản mới, không có hai bản lệch nhau.

```
Repo này là đồ án Android PUBGApp. Phiên này bạn làm liên tục hết các issue đang gán cho tôi, theo đúng thứ tự, mỗi issue một Pull Request. Đừng hỏi tôi từng bước, cứ chạy.

BƯỚC 1, NẠP LUẬT VÀ QUY TRÌNH. Đọc trong repo, theo thứ tự:
- AGENTS.md ở gốc repo. Đây là luật, không phải gợi ý; 23 luật ở mục 6 áp dụng cho mọi bước bạn làm.
- .agents/rules/ cả bốn tệp.
- .agents/skills/pubg-start-task/SKILL.md, pubg-sync-branch, pubg-code, pubg-ui, pubg-open-pr. Đây là quy trình bạn chạy cho từng issue, làm theo đúng từng bước trong đó.
- workflow/plan/ke-hoach-rut-gon.md đọc hết, và workflow/plan/issues/README.md.

BƯỚC 2, LẬP DANH SÁCH VIỆC CỦA TÔI. Chạy:
  gh issue list --assignee @me --state open --limit 50
Đối chiếu từng issue với bảng ở ke-hoach-rut-gon.md mục 3 để biết nó thuộc đợt nào: issue GitHub số N là số thứ tự N trừ 80 trong kế hoạch. Rồi sắp thứ tự làm, theo ba quy tắc này:
- Đợt nhỏ làm trước đợt lớn: đợt 1, rồi 2, rồi 3, rồi 4.
- Trong cùng một đợt thì theo thứ tự số tăng dần.
- Tôn trọng các chuỗi phụ thuộc mà kế hoạch nêu, và cả ghi chú chuyển người ở đầu một số issue.
Đưa tôi xem danh sách đã sắp, một dòng một issue, rồi làm luôn, không chờ tôi trả lời.

BƯỚC 3, KIỂM CỬA TRƯỚC KHI CODE. Khung 93 lớp phải có trên main rồi, nếu không thì mọi thứ bạn viết sẽ không biên dịch được. Chạy git fetch origin && git switch main && git pull, rồi tìm tệp DataCallback.java trong PUBGApp/app/src/main/java/com/nhom3/pubgapp/. Không thấy thì dừng hẳn, nói tôi "khung lớp chưa vào main", và đừng tự tạo lớp nào.

BƯỚC 4, VÒNG LẶP. Với từng issue trong danh sách, theo thứ tự, làm xong hẳn rồi mới sang issue sau:
 1. git switch main && git pull
 2. gh issue view <số>. Mở bản nháp mà issue trỏ tới trong workflow/plan/issues/, và mọi mục tài liệu issue nhắc tên, bắt buộc có workflow/docs/architecture/dac-ta-lop/ cho mọi lớp bạn sẽ chạm.
 3. Tạo nhánh <type>/<số>-<slug> từ main.
 4. Chia việc thành 4 đến 8 bước nhỏ. Mỗi bước build được một mình và có một commit riêng, tiêu đề dạng type(scope): câu mô tả.
 5. Chạy cd PUBGApp && ./gradlew assembleDebug testDebugUnitTest lintDebug, và sh workflow/scripts/check-java-style.sh.
 6. Mở Pull Request theo .github/PULL_REQUEST_TEMPLATE.md, tiếng Việt, có dòng Closes #<số>.
 7. Báo tôi một đoạn ngắn: số Pull Request, danh sách tệp đã sửa, kết quả thật của lệnh build, và mọi lớp hoặc phương thức hoặc trường công khai mới kèm dòng tài liệu định nghĩa nó.
 8. Sang issue tiếp theo. Đừng merge Pull Request nào, leader merge.

HAI TÌNH HUỐNG PHỤ THUỘC:
- Issue tiếp theo cần code của một Pull Request của chính tôi còn chưa merge: tạo nhánh từ nhánh đó thay vì từ main, và ghi rõ chuyện đó trong Pull Request.
- Issue tiếp theo cần code của người khác còn chưa vào main: bỏ qua nó, làm issue sau đó, và nói tôi biết issue nào đang treo và chờ cái gì.

HAI THỨ QUYẾT ĐỊNH ĐÚNG SAI:
- Danh sách tệp trong issue là hàng rào. Tệp không có trong danh sách thì không sửa, kể cả lỗi một dòng: ghi nó vào Pull Request cho tôi.
- Mọi tên lớp, trường, phương thức, enum, collection phải chép đúng từ workflow/docs/architecture/dac-ta-lop/, workflow/docs/data/ hoặc workflow/docs/architecture/trang-thai.md. Tên không có ở đó thì dừng, đừng bịa và đừng đổi tên tài liệu có sẵn.

TUYỆT ĐỐI KHÔNG GHI CÔNG CHO AI, KHÔNG CÓ NGOẠI LỆ (luật R8). Trong mọi thông điệp commit, tiêu đề và nội dung Pull Request, comment issue: không dòng Co-authored-by, không dòng kiểu "Generated with" hay "Made with", không emoji robot, không nhắc tên bất cứ công cụ AI nào như tác giả hay người hỗ trợ. Commit mang tên người đang ngồi máy, tức tôi. Đừng chạy git config user.name hay user.email, đừng đổi danh tính tác giả. Hook và CI chặn những dòng này, nhưng chặn ở bước push nên tôi sẽ phải ngồi sửa lại commit: đừng viết ra ngay từ đầu.

CHỈ DỪNG VÀ HỎI TÔI TRONG BA TRƯỜNG HỢP: thiếu một tên hoặc một phương thức thuộc người khác (luật R20), tài liệu mâu thuẫn nhau (R23), hoặc bạn cần sửa một tệp đóng băng ở ke-hoach-rut-gon.md mục 2. Lúc đó comment vào issue bằng tiếng Việt, gắn nhãn blocked, nói tôi, rồi chuyển sang issue tiếp theo chứ đừng ngồi chờ. Mọi chỗ mơ hồ khác thì tự chọn cách hợp lý, ghi một dòng lý do vào Pull Request, và đi tiếp.

VIỆC TÔI LÀM, KHÔNG PHẢI BẠN: thử trên máy ảo. Gradle và check-java-style.sh thì bạn chạy, dán lệnh và kết quả thật; chưa chạy được thì nói chưa chạy, đừng viết là đã test.

HẾT PHIÊN: khi không còn issue nào làm được nữa, liệt kê cho tôi ba thứ: issue nào đã mở Pull Request, issue nào đang treo và chờ cái gì, và chỗ nào bạn đã tự quyết mà tôi nên xem lại.
```

Trợ lý AI nào có lệnh gạch chéo đọc `.agents/workflows/` thì nó tự gọi `/start-task`, `/code`, `/open-pr` ở bước 4. Không có thì nó đọc thẳng các tệp `SKILL.md`, kết quả như nhau.

Phiên dài thì model hay quên luật. Hết mỗi hai ba issue nên mở phiên mới và dán lại khối này: nó tự tra `gh issue list --assignee @me` nên biết đi tiếp từ đâu, không cần bạn nhắc.

Nếu công cụ của bạn không đọc được tệp trong repo, dán mục 3 thay cho khối này, và làm một issue một phiên.

## 3. Prompt đầy đủ, khi công cụ không đọc được tệp trong repo, hoặc khi bạn muốn duyệt từng bước

Khối này chép sẵn luật vào prompt, và dừng lại ở ba chặng để bạn duyệt. Trợ lý AI đọc được tệp thì nó tự đọc chỗ này; không đọc được thì bạn dán tay. Thay `<SỐ>` bằng số issue. **Mỗi issue một phiên chat mới.**

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
- Tuyệt đối không ghi công cho AI (luật R8): không dòng Co-authored-by, không "Generated with" hay "Made with", không emoji robot, không nhắc tên công cụ AI như tác giả hay người hỗ trợ, ở bất cứ đâu trong commit, Pull Request hay comment. Commit mang tên người đang ngồi máy. Đừng chạy git config user.name hay user.email.
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
3. **Đọc thông điệp commit trước khi push.** Chỉ cần nhìn xuống dòng cuối: không có dòng Co-authored-by, không có dòng ghi công cụ nào. Chỉ năm người trong nhóm được xuất hiện với tư cách người đóng góp (luật R8). Hook chặn được, nhưng chặn lúc push thì bạn phải `git commit --amend` lại, mất thì giờ. Thấy trợ lý AI tự thêm dòng đó một lần thì nhắc nó ngay trong phiên, đừng để nó lặp.
