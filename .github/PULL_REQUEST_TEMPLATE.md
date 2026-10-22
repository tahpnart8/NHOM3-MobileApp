## Tóm tắt
<!-- Viết bằng tiếng Việt. Đã thay đổi gì và vì sao, trong 2 đến 4 câu. Nếu bạn bỏ một bước của workflow/guide/, thêm một dòng bắt đầu bằng "Deviation:" và nói lý do. -->

## Issue liên quan
Closes #
<!-- Điền số issue sau từ khóa. Nếu đóng nhiều issue, lặp từ khóa trước MỖI số, vì GitHub chỉ đóng issue đứng ngay sau từ khóa. Nếu không đóng issue nào, viết "Closes: none". Giữ từ khóa Closes bằng tiếng Anh. -->

## Tính năng và phạm vi
- Mã tính năng (workflow/docs/product/features.md):
- Mã việc kế hoạch (workflow/plan/, ví dụ P2-B3):
- Module (A đến E) và STT đã làm:
- File đã đổi (khớp danh sách "File được tạo hoặc sửa" của issue):
- File ngoài danh sách của issue: không có
<!-- Nếu có, liệt kê từng file và lý do. Thông thường phải bỏ file đó ra khỏi Pull Request. -->
- Cố ý không đụng tới:
- [ ] Không làm chức năng "Phát triển sau" và không dùng Cloud Storage hay video
- [ ] Mọi tên lớp, trường, phương thức, trạng thái đều có trong tài liệu đặc tả (dac-ta-lop, data, trang-thai); tôi chỉ thay thân các phương thức được giao

## Tiêu chí nghiệm thu
<!-- Chép danh sách từ issue. Chỉ đánh dấu ô nào đã thật sự kiểm. Để trống ô chưa làm xong. -->
- [ ]

## Code cũ
- [ ] Tôi không xóa hoặc đổi tên code của tính năng đã có
<!-- Nếu có xóa hoặc đổi tên, thêm dòng dưới đây kèm issue mang nhãn type:removal, nếu không thì xóa comment này:
Removal-Issue: #N -->

## Đã kiểm tra thế nào
<!-- Lệnh đã chạy và kết quả thật. Nói rõ phần nào chưa chạy được. Ghi thiết bị hoặc emulator và bản API nếu bạn đã chạy ứng dụng. -->
```text
./gradlew assembleDebug testDebugUnitTest lintDebug - <kết quả>
```

## Tài liệu
- [ ] `workflow/docs/data/*` và `workflow/docs/product/features.md` đã cập nhật, hoặc thay đổi này không ảnh hưởng tới chúng
- [ ] Quyết định đáng nhớ đã ghi vào `workflow/memory/decisions.md`, hoặc không có

## Ảnh chụp màn hình
<!-- Với thay đổi giao diện: chụp cả dọc và ngang. -->
