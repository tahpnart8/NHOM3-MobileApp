# **Tính năng cơ bản của sàn TMĐT và nền tảng giao dịch đồ cũ**

## **1\. Giới thiệu**

Mục tiêu của báo cáo là tổng hợp tập tính năng cơ bản mà các sàn TMĐT và nền tảng giao dịch đồ cũ phổ biến đang cung cấp, sau đó đối chiếu với phạm vi chức năng hiện có của đồ án để xác nhận phần đã bao phủ đủ và phần có thể cân nhắc bổ sung.

Phạm vi khảo sát gồm năm nền tảng, chia thành hai nhóm để việc đối chiếu có ý nghĩa hơn:

* Nhóm sát với mô hình đồ án, giao dịch ngang hàng (C2C) cho đồ đã qua sử dụng: Chợ Tốt, Facebook Marketplace, Vinted. Vinted không hoạt động chính thức tại Việt Nam nhưng được đưa vào vì có cơ chế bảo vệ giao dịch và xác thực sản phẩm thuộc loại tiên tiến nhất trong nhóm đồ cũ.  
* Nhóm sàn TMĐT bán hàng nói chung, dùng làm chuẩn tham chiếu về mức độ chuẩn hóa quy trình: Shopee, TikTok Shop.

Nguồn thông tin: trang chính thức và trung tâm trợ giúp của từng nền tảng, báo chí công nghệ và kinh tế trong nước, không sử dụng số liệu nội bộ chưa công bố. Do chính sách các sàn thay đổi thường xuyên, số liệu trong báo cáo phản ánh thời điểm khảo sát và cần kiểm tra lại trước khi trích dẫn chính thức.

## **2\. Tổng quan nền tảng**

| Nền tảng | Mô hình hoạt động | Đối tượng chính |
| :---- | :---- | :---- |
| Chợ Tốt | C2C rao vặt, không bắt buộc qua trung gian, có tùy chọn thanh toán đảm bảo | Người dùng phổ thông tại Việt Nam, mọi ngành hàng |
| Facebook Marketplace | C2C tích hợp mạng xã hội, hai luồng song song: tại chỗ hoặc qua Checkout | Người dùng Facebook, giao dịch địa phương là chủ đạo |
| Vinted | C2C chuyên biệt thời trang đã qua sử dụng, bắt buộc giao dịch qua nền tảng | Người mua bán quần áo, phụ kiện cũ, chủ yếu châu Âu và Mỹ |
| Shopee | Marketplace tập trung, lai B2C và C2C, có kho vận và thanh toán qua sàn | Người mua sắm đại chúng, hàng mới là chủ đạo |
| TikTok Shop | Thương mại kết hợp nội dung, bán qua video và livestream, thanh toán qua sàn | Người dùng TikTok, thiên về mua sắm ngẫu hứng theo nội dung |

## **3\. So sánh tính năng theo chín nhóm**

| Nhóm function | Chợ Tốt | Facebook Marketplace | Vinted | Shopee | TikTok Shop | PUBGApp có làm hay không |
| ----- | ----- | ----- | ----- | ----- | ----- | ----- |
| 1\. Tài khoản, hồ sơ | Đăng ký, xác thực tài khoản, hồ sơ người bán tùy biến | Dùng tài khoản Facebook có sẵn, tín hiệu tin cậy từ bạn chung | Đăng ký email hoặc liên kết mạng xã hội, hồ sơ có rating | Hồ sơ Shop, phân hạng thành viên | Hồ sơ người bán, yêu cầu xác minh giấy tờ và xác minh định kỳ | Có |
| 2\. Đăng, quản lý sản phẩm | Đăng miễn phí, không giới hạn, sửa không cần duyệt | Đăng nhanh, tích hợp kênh bán bên thứ ba cho seller lớn | Đăng bằng ảnh, mô tả, có dịch vụ xác thực món hàng giá trị cao | Quản lý shop, tồn kho, biến thể sản phẩm | Gắn sản phẩm vào video, livestream, hồ sơ cá nhân | Có |
| 3\. Tìm kiếm, khám phá | Từ khóa, bộ lọc, ưu tiên theo vị trí | Từ khóa, bộ lọc, ưu tiên theo vị trí | Từ khóa, bộ lọc danh mục, giá, tình trạng | Từ khóa, bộ lọc, gợi ý thuật toán, flash sale | Khám phá qua nội dung, livestream, ít phụ thuộc tìm kiếm chủ động | Có |
| 4\. Kết nối, thương lượng | Chat trực tiếp | Chat qua Messenger, có thể thương lượng | Chat, có nút đề nghị giá một chạm | Chat có sẵn nhưng giá niêm yết là chủ đạo | Chủ yếu qua bình luận, livestream, ít cơ chế thương lượng 1-1 chính thức | Có, thêm offer một chạm mà cả năm nền tảng đều chưa có đầy đủ |
| 5\. Quy trình đặt hàng | Có thể ngoài nền tảng hoặc qua Thanh Toán Đảm Bảo | Hai luồng: tại chỗ hoặc Checkout | Bắt buộc qua nền tảng | Chuẩn hóa hoàn toàn, có trạng thái đơn hàng | Chuẩn hóa hoàn toàn, có trạng thái đơn hàng | Có |
| 6\. Thanh toán | Có, tùy chọn: ví điện tử qua Thanh Toán Đảm Bảo, hoặc tiền mặt | Tiền mặt (không bảo vệ) hoặc thẻ, PayPal qua Checkout (có bảo vệ) | Bắt buộc qua nền tảng, giữ tiền đến khi người mua xác nhận | Bắt buộc qua sàn, giữ tiền đến hết thời hạn xử lý | Bắt buộc qua sàn, giữ tiền đến hết thời hạn xử lý | Có, nhưng ký quỹ mô phỏng nội bộ, không có cổng thanh toán thật |
| 7\. Vận chuyển, giao nhận | Tự thỏa thuận gặp mặt hoặc qua đối tác vận chuyển | Gặp trực tiếp hoặc ship qua Checkout | Tích hợp, tem vận chuyển in sẵn, theo dõi thời gian thực | Tích hợp mạng lưới vận chuyển, theo dõi đầy đủ | Tích hợp mạng lưới vận chuyển, theo dõi đầy đủ | Có ở mức tối thiểu: người bán tự điền đơn vị vận chuyển và mã vận đơn kèm ảnh, chưa tích hợp API hãng vận chuyển thật |
| 8\. Đánh giá, hồ sơ uy tín | Có, đánh giá và nhận xét cơ bản | Có, feedback kết hợp hồ sơ xã hội | Có, rating gắn hồ sơ, ảnh hưởng khả năng bán | Có, rating gắn Shop | Có, rating gắn hồ sơ người bán | Có |
| 9\. Bảo vệ, khiếu nại, tranh chấp | Hoàn tiền 100% nếu không nhận hàng hoặc sai mô tả khi dùng Thanh Toán Đảm Bảo, có tư vấn xử lý tranh chấp, báo cáo tin vi phạm | Purchase Protection chỉ áp dụng đơn qua Checkout, hoàn tiền khi không giao, hư hỏng, sai mô tả, gian lận | Buyer Protection bắt buộc mọi đơn, quy trình tranh chấp dựa trên bằng chứng hai bên, nền tảng ra quyết định cuối | Trả hàng, hoàn tiền trong 15 ngày, người bán có quyền khiếu nại ngược, có cơ chế phát hiện lạm dụng chính sách | Hoàn tiền, có chính sách hoàn gấp đôi khi hàng sai mô tả nghiêm trọng, kiểm duyệt và khóa tài khoản vi phạm | Có |

## **4\. Baseline và đối chiếu với phạm vi chức năng đồ án**

Từ bảng so sánh, tập tính năng mà phần lớn năm nền tảng đều có, gọi là baseline, gồm: tài khoản và xác thực, đăng và quản lý tin, tìm kiếm và bộ lọc, chat giữa hai bên, trạng thái đơn hàng hoặc giao dịch, đánh giá sau giao dịch, cơ chế báo cáo vi phạm và xử lý khiếu nại hoàn tiền.

Đối chiếu với file 03, các nhóm này đã có feature tương ứng: F-U01 đến F-U04 (tài khoản, hồ sơ uy tín), F-A01, F-A03, F-A04 (đăng, sửa, tìm kiếm), F-B01 (chat), F-C03, F-C04 (trạng thái, xác nhận giao dịch), F-D01 đến F-D04 (đánh giá, báo cáo, khiếu nại). Phạm vi chức năng hiện tại của đồ án bao phủ đủ baseline, không có nhóm nào trong chín nhóm bị bỏ trống hoàn toàn.

Hai điểm khác biệt của đồ án so với năm nền tảng tham chiếu, và đây là khác biệt hợp lý nên giữ nguyên chứ không phải thiếu sót:

* F-B02, cho phép đề nghị theo ba hình thức mua, trao đổi, cho tặng. Trong năm nền tảng khảo sát, không nền tảng nào có luồng trao đổi hiện vật hoặc cho tặng chính thức, chỉ có cơ chế đề nghị giá một chiều cho việc mua. Đây là điểm đặc thù của đề tài, đúng với định hướng BACCM đã chốt.  
* F-C05, thanh toán trung gian mô phỏng. Bốn trong năm nền tảng, trừ Chợ Tốt coi đây là tùy chọn, đều coi escrow là cơ chế bắt buộc hoặc mặc định chứ không phải tính năng phụ. Điều này củng cố thêm lý do nên cân nhắc nâng mức ưu tiên của F-C05, hiện đang ở mức S, tùy vào năng lực triển khai của team trong học kỳ.

Các tính năng ở nền tảng tham chiếu mà file 03 chưa thể hiện rõ, có thể cân nhắc bổ sung ở mức tinh chỉnh, không phải bổ sung module mới:

* Cơ chế đề nghị giá nhanh một chạm, khác với luồng thương lượng nhiều bước hiện có ở F-B03.  
* Dịch vụ xác thực bổ sung cho sản phẩm giá trị cao, như Vinted đang làm, có thể gắn thêm vào F-A02.  
* Nhánh xử lý khi khiếu nại có dấu hiệu lạm dụng chính sách hoàn tiền, như Shopee đang áp dụng, hiện F-D04 chưa đề cập.  
* Mốc thời gian cụ thể cho từng bước khiếu nại, ví dụ hạn gửi khiếu nại sau khi nhận hàng, hạn phản hồi của bên bị khiếu nại. F-D03 và F-D04 hiện mô tả chức năng nhưng chưa có con số, cần bổ sung khi viết Use Case.

## **5\. Khuyến nghị bổ sung cho file Function**

| Mã đề xuất | Mô tả | Gắn vào feature |
| :---: | ----- | :---: |
| R1 | Bổ sung luồng đề nghị giá nhanh một chạm, song song với thương lượng nhiều bước | F-B03 |
| R2 | Bổ sung tùy chọn yêu cầu xác thực tình trạng qua ảnh hoặc video bổ sung cho sản phẩm giá trị cao | F-A02 |
| R3 | Bổ sung nhánh xử lý khi khiếu nại có dấu hiệu lạm dụng, dựa trên lịch sử vi phạm của tài khoản | F-D04 |
| R4 | Quy định mốc thời gian cụ thể cho khiếu nại: hạn gửi sau khi nhận hàng, hạn phản hồi, hạn admin ra quyết định | F-D03, F-D04 |

Nhận định chung: phạm vi chức năng hiện tại của đồ án đã bao phủ đủ baseline của một nền tảng giao dịch, không phát hiện thiếu sót ở mức nghiêm trọng. Các đề xuất trên đều là tinh chỉnh, ở mức ưu tiên S hoặc C, riêng R4 nên xử lý sớm vì ảnh hưởng trực tiếp đến độ rõ ràng khi đặc tả Use Case cho nhóm D.

## **Nguồn tham khảo**

* Trang và trung tâm trợ giúp chính thức: Vinted Help Centre, Meta Purchase Protection, TikTok Shop Seller Policies.  
* Báo chí trong nước: VnEconomy, VnExpress, Tạp chí Công Thương, Báo Lào Cai, Sapo Blog, GHN Blog, FPT Shop, Brands Vietnam, Doanh Nhân Plus.  
* Bài phân tích độc lập: Surfshark Blog, Smallbiztrends, MyWifeQuitHerJob, Closo, Vendoo Blog.

Ghi chú: danh sách trên là nhóm nguồn đã dùng, không phải trích dẫn nguyên văn. Khi đưa số liệu cụ thể (thời gian, tỷ lệ phí) vào tài liệu chính thức của đồ án, nên kiểm tra lại trực tiếp trên trang chính thức tại thời điểm nộp bài vì chính sách các sàn thay đổi khá thường xuyên.