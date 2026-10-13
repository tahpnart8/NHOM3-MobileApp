# Thuật toán gợi ý

Đặc tả của `RecommendationEngine` (gói `util`): tin tương tự (STT 36), giá tham khảo (STT 37), mục Dành cho bạn (STT 38). Mọi phép tính chạy **trên máy**, không lưu kết quả lên Firestore, không dùng thư viện học máy. Công thức cố định để code, test và dữ liệu mẫu cùng khớp; đổi công thức thì sửa tài liệu này trong cùng Pull Request.

## Dữ liệu đầu vào

| Đầu vào | Lấy từ | Giới hạn để tiết kiệm lượt đọc Firestore |
| --- | --- | --- |
| Tin ứng viên | `ListingRepository.getSimilar` (cùng danh mục) hoặc `getFeed` (kênh chung) | Tối đa 100 tin AVAILABLE mới nhất |
| Danh mục đã xem | `ListingRepository.getRecentViews(20)` (đọc Room trên máy) | 20 lần xem gần nhất |
| Từ khóa đã tìm | `ListingRepository.getRecentSearches(10)` (đọc Room trên máy) | 10 từ khóa gần nhất |
| Danh mục quan tâm | `User.interestedCategoryIds` | |
| Danh mục của tin đã lưu | `UserRepository.getSavedIds` rồi danh mục của từng tin | |

Luôn loại khỏi kết quả: tin không ở trạng thái AVAILABLE, tin của chính người đang xem, tin đang được xem (với tin tương tự).

## 1. Tin tương tự: `similarTo(Listing target, List<Listing> candidates)`

Ứng viên: cùng `categoryId` với tin đang xem. Điểm của mỗi ứng viên `c`:

```
diem = 1.0
     + 0.5  nếu c.itemCondition == target.itemCondition
     + 0.3  nếu c.province == target.province
     + 1.0  * dogan_gia
     + 1.0  * trung_tu_khoa

dogan_gia     = 1 - min(1, |c.price - target.price| / max(target.price, 1))
                (bằng 0 khi một trong hai tin có giá 0)
trung_tu_khoa = |giao của searchKeywords| / |hợp của searchKeywords|   (Jaccard, 0 khi cả hai rỗng)
```

Sắp xếp điểm giảm dần, bằng điểm thì tin mới hơn trước. Trả **tối đa 10 tin**. Ít ứng viên thì trả ít hơn; không có ứng viên thì trả danh sách rỗng và màn hình ẩn mục này.

## 2. Giá tham khảo: `priceRange(Listing target, List<Listing> candidates)`

Trả mảng `long[]` hai phần tử `{thấp, cao}`, hoặc mảng rỗng khi không đủ dữ liệu.

1. Tập giá P1: các tin cùng `categoryId` **và** cùng `itemCondition`, giá lớn hơn 0, khác tin đang xem.
2. Nếu P1 có **ít nhất 5** giá thì dùng P1. Nếu không, dùng P2: cùng `categoryId`, mọi tình trạng, giá lớn hơn 0. Nếu P2 cũng ít hơn 5 giá thì trả mảng rỗng.
3. Sắp tăng dần. `thấp` là phân vị 25, `cao` là phân vị 75, theo cách "hạng gần nhất": phần tử ở vị trí `ceil(p * n) - 1` (đếm từ 0).
4. Làm tròn cả hai về bội số gần nhất của 10.000 đồng.

Màn hình chi tiết tin hiện "Giá tham khảo: thấp đến cao" và ghi rõ đã dùng tập nào ("cùng tình trạng" hoặc "cùng danh mục").

## 3. Dành cho bạn: `forYou(List<Listing> candidates, List<String> viewedCategoryIds, List<String> recentKeywords, List<String> savedCategoryIds, User user)`

`RecommendationEngine` chỉ nhận danh sách thường, không nhận entity Room, để giữ luật tầng (gói `util` không dùng `data.local`).

Điểm của mỗi ứng viên `c`:

```
diem = 3.0 * ty_le_xem_danh_muc
     + 2.0  nếu một từ khóa của 10 lần tìm gần nhất (đã bỏ dấu) nằm trong c.searchKeywords
     + 1.0  nếu c.categoryId nằm trong user.interestedCategoryIds
     + 0.5  nếu c.categoryId trùng danh mục của một tin đã lưu
     + 1.0 / (1 + so_ngay_tu_luc_dang / 7)

ty_le_xem_danh_muc = số lần xem thuộc c.categoryId / tổng số lần xem   (0 khi chưa xem gì)
```

Sắp xếp điểm giảm dần, bằng điểm thì tin mới hơn trước. Trả **tối đa 20 tin**. Người dùng mới, chưa có lịch sử và danh mục quan tâm thì mọi điểm chỉ còn phần thời gian, kết quả là các tin mới nhất.

## Kiểm thử bắt buộc

Unit test thuần Java (không cần thiết bị), tối thiểu:

| Hàm | Trường hợp |
| --- | --- |
| `similarTo` | Tin cùng tình trạng và gần giá đứng trước; không trả tin của chính tin đang xem; tối đa 10 |
| `priceRange` | Đủ 5 tin cùng tình trạng thì dùng P1; 3 tin cùng tình trạng và 6 tin cùng danh mục thì dùng P2; dưới 5 thì rỗng; làm tròn 10.000 |
| `forYou` | Người đã xem nhiều tin một danh mục thấy danh mục đó trước; người mới nhận tin mới nhất; tối đa 20 |

## Yêu cầu với dữ liệu mẫu

Nút tạo dữ liệu mẫu (bản debug) phải tạo đủ để ba hàm trên ra kết quả không rỗng khi demo:

- Ít nhất 6 danh mục gốc, mỗi danh mục 2 danh mục con.
- Ít nhất 60 tin AVAILABLE; mỗi danh mục con ít nhất 4 tin.
- Ít nhất 4 danh mục con có **từ 5 tin trở lên cùng một tình trạng**, giá trải rộng, để giá tham khảo dùng được tập P1; các danh mục con còn lại có từ 5 tin trở lên tính chung mọi tình trạng, để dùng tập P2.
- Tiêu đề có từ khóa lặp lại giữa các tin cùng danh mục (ví dụ "iphone", "macbook", "xe dap") để tin tương tự và tìm kiếm có kết quả.
- Tin rải ở ít nhất 3 tỉnh, trong đó TP.HCM có tọa độ trong bán kính 1, 3 và 5 km quanh trung tâm Quận 1, để thử bộ lọc bán kính.
- Ít nhất 8 hồ sơ người bán mẫu có điểm đánh giá khác nhau, để thử sắp xếp "Đánh giá tốt".
