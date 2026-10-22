# P0. Chốt tài liệu

**Thời gian:** 12/10. **Chủ:** Phát, cùng các công cụ AI.

**Mục tiêu:** trước khi code, mọi tài liệu khớp nhau và khớp các quyết định mới nhất, để AI của từng thành viên không phải đoán. Lần kiểm định thứ hai (12/10) soát toàn bộ tài liệu theo 4 vùng: giao diện, sản phẩm và phân tích, kiến trúc và dữ liệu, luật cho AI. Lần này tìm ra khoảng 270 chỗ lệch hoặc thiếu; P0 sửa hết chúng.

**Điều kiện hoàn thành:**

- Bốn Pull Request dưới đây đã merge.
- `sh workflow/scripts/tests/policy-test.sh` xanh trên `main`.
- Không còn tài liệu nào mô tả Cloud Storage, gói Blaze hay gửi video như thứ sẽ làm, không còn số lớp cũ hay cách đánh số chức năng cũ.

| Mã | Việc | Kết quả |
| --- | --- | --- |
| P0-01 | Mô hình theo gói Spark và theo kết quả kiểm định kiến trúc: ảnh Base64 trong collection `images`, bỏ video (STT 45 sang Phát triển sau), bỏ các bộ đếm do người dùng ghi chéo (thay bằng truy vấn đếm), thêm các phương thức còn thiếu. Cập nhật sơ đồ lớp, ERD, sơ đồ chức năng, đặc tả lớp, từ điển dữ liệu, máy trạng thái, luồng xử lý, kiến trúc; viết ma trận Security Rules; ghi quyết định | 93 lớp, 23 collection, 6 bảng Room, 91 chức năng Đợt này, các tài liệu sinh từ cùng một nguồn |
| P0-02 | Tài liệu sản phẩm, phân tích, giao diện, cài đặt: sổ 14 tính năng ở trạng thái `planned`, tóm tắt sản phẩm, thuật ngữ, giao diện đánh số lại theo STT và ghi rõ chỗ khác với ảnh, cài đặt Firebase gói Spark, quy ước code (gói `ui.<module>`, luật ánh xạ Firestore); xóa thư mục `_ban-giao/` | Tài liệu cho người đọc khớp mô hình |
| P0-03 | Luật cho AI: `AGENTS.md` thêm luật R16 đến R23 (tên lấy từ đặc tả, không làm chức năng Phát triển sau, trạng thái theo máy trạng thái, chỉ sửa file trong issue, file dùng chung có chủ, gói Spark, điều hướng, dừng khi tài liệu thiếu); skill, mẫu issue và Pull Request; `CODEOWNERS` thêm Hải Long cho `PUBGApp/`; nhãn `module:a` đến `module:e`; hướng dẫn | AI của mọi người làm theo cùng một luật, tự kiểm được phạm vi |
| P0-04 | Kế hoạch này và bản đồ sở hữu; tạo issue P1 và P2 trên GitHub Project, gán người | Cả nhóm nhận việc |
