# Bản nháp issue: chỗ tra tên lớp, chữ ký và tiêu chí

42 tệp trong thư mục này là **bản nháp chi tiết** của từng việc. Chúng không phải issue trên GitHub; issue trên GitHub chỉ ghi tóm tắt rồi trỏ về đây. Dùng chúng để tra **tên lớp, chữ ký phương thức chính xác, danh sách tệp được sửa và tiêu chí nghiệm thu**, vì luật R16 bắt mọi tên phải chép đúng từ tài liệu.

**Đọc [`ke-hoach-rut-gon.md`](../ke-hoach-rut-gon.md) trước.** Nó nói việc nào thuộc đợt nào, ai làm, và tra bản nháp nào.

## Ba chỗ bản nháp đã lạc hậu, bỏ qua giúp

Các tệp này viết **trước** kế hoạch rút gọn, nên có ba loại dòng không còn đúng:

1. **Dòng và bước nói "điền thân `XRepository.y`" khi `XRepository` không phải của bạn.** Đợt 1 của kế hoạch rút gọn giao mỗi repository cho đúng một người, viết hết một lượt. Issue đợt 2 và 3 chỉ còn Fragment, layout, adapter và thân ViewModel. Bảng sở hữu ở [`ke-hoach-rut-gon.md`](../ke-hoach-rut-gon.md) mục 2 là chuẩn, không phải [`so-huu.md`](../so-huu.md) mục 5.

2. **Mọi chỗ nhắc một tính năng đã bị bỏ.** Danh sách bỏ ở [`ke-hoach-rut-gon.md`](../ke-hoach-rut-gon.md) mục 4 và [`features.md`](../../docs/product/features.md) mục "Phạm vi đợt rút gọn". Hay gặp nhất: tin tương tự và giá tham khảo trong `P2-B6a`, tab "Người dùng" trong `P2-B4a`, địa chỉ trong `P2-B1a`, các lối tắt tới A04, A06, C04, D05, D06, D07, E05, E06, E07.

3. **Mục `### Câu hỏi cho leader` ở cuối mỗi tệp.** Nhiều câu đã được trả lời bằng chính đặc tả hiện tại (đặc tả đã bổ sung `loading`, `error`, `done`, `clearError`, `clearDone`, quy ước getter, `SessionManager.getCurrentUser`, `AuthRepository.getCurrentUserId`, `Listing.reserve`, `ChatRepository.observeOffers`, `DataCallback.NOT_READY` và nhiều tên khác). Gặp một câu hỏi thì **tra đặc tả trước**; nếu đặc tả đã có tên thì câu hỏi đó hết hiệu lực.

Bốn chỗ đã có cách hiểu mặc định, dùng luôn đừng hỏi lại: `autoReleaseAt` ghi bằng giờ máy cộng 7 ngày (Rules cho sai số 5 phút); dòng `StatusChange` do repository thêm, không phải phương thức của `Deal`; chữ thông báo là hằng tiếng Việt trong `NotificationRepository`; tham số `batch` khác null thì phương thức chỉ thêm lệnh ghi vào lô rồi gọi lại ngay, bên gọi commit.

## Nguồn sự thật, theo thứ tự ưu tiên

Khi bản nháp và tài liệu nói khác nhau, **tài liệu thắng**:

1. [`workflow/docs/architecture/dac-ta-lop/`](../../docs/architecture/dac-ta-lop/README.md): tên lớp, trường, chữ ký phương thức. Sinh tự động từ mô hình nên không bao giờ lệch sơ đồ.
2. [`workflow/docs/architecture/trang-thai.md`](../../docs/architecture/trang-thai.md): trạng thái đi từ đâu sang đâu, qua phương thức nào.
3. [`workflow/docs/data/`](../../docs/data/firestore-schema.md): collection, trường, luật Security Rules.
4. [`workflow/docs/ui/`](../../docs/ui/README.md) và [`conventions/giao-dien.md`](../../docs/conventions/giao-dien.md): màn hình, token màu, nhãn trạng thái, bốn trạng thái mỗi màn.
5. [`workflow/plan/ke-hoach-rut-gon.md`](../ke-hoach-rut-gon.md): ai làm gì, thứ tự, phần bỏ lại.
6. Bản nháp trong thư mục này: chi tiết còn lại.

## Khuôn 19 mục

Mỗi tệp theo cùng một khuôn, giống biểu mẫu issue ở `.github/ISSUE_TEMPLATE/feature.yml`. Ba mục đáng đọc kỹ nhất khi làm cùng trợ lý AI:

- `### Bối cảnh`: năm dòng định hướng, viết cho người chưa đọc đặc tả.
- `### Các bước đề nghị`: danh sách đánh số, mỗi bước kèm chữ ký phương thức chép từ `dac-ta-lop`. Được làm theo thứ tự khác, nhưng **không được đặt tên mới**.
- `### Tự kiểm trước khi mở Pull Request`: lệnh phải chạy và kết quả mong đợi. Dán kết quả thật vào Pull Request (luật R1).
