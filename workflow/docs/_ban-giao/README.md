# Thư mục bàn giao

Thư mục này chứa tài liệu nguồn và hướng dẫn làm việc cho từng người, gom sẵn để không ai phải đi tìm file.

**Mở đúng thư mục mang tên mình, làm theo README trong đó. Không cần đọc thư mục của người khác.**

| Người | Thư mục | Issue còn phải làm |
| --- | --- | --- |
| Thúy Ngân | [`ngan/`](ngan/README.md) | #20 (phần 1), #23 (phần 1), #25, #26, #27 |
| Anh Tú | [`tu/`](tu/README.md) | #20 (phần 2), #23 (phần 2), #28, #29, #30 |
| Hải Long | [`long/`](long/README.md) | #20 (phần 3), #31, #32, #33 |
| Hoàng Phúc | [`phuc/`](phuc/README.md) | #21, #34, #35, #36 |

Hướng dẫn chung về cách tạo nhánh, lấy file và mở Pull Request nằm ở [`chung/cach-lam.md`](chung/cach-lam.md). Đọc file đó một lần trước khi bắt đầu.

## Nên làm cái nào trước

**Nhóm giao diện (#25 đến #36) không phụ thuộc gì cả, ai cũng bắt đầu được ngay hôm nay.** Cứ làm nhóm đó trước trong lúc chờ.

Nhóm tài liệu đi theo dây chuyền, người sau phải chờ người trước merge xong:

```
#20 Ngân (dòng 1-32)  ->  #20 Tú (dòng 33-59)  ->  #20 Long (dòng 60-94)
        -> #21 Phúc  ->  #23 Ngân (trang 0-4)  ->  #23 Tú (trang 5-7)
```

Mỗi người tự xem trong README của mình phần "đang chờ ai" để biết khi nào tới lượt.

## Lưu ý

Thư mục `_ban-giao/` này là chỗ để tài liệu nguồn, **không phải nơi nộp bài**. Nộp bài là chép file sang đúng đường dẫn thật (`workflow/docs/ui/...`, `workflow/docs/diagrams/...`, `workflow/docs/product/...`) rồi commit đường dẫn thật đó. Đừng commit thư mục `_ban-giao/` vào Pull Request của mình.
