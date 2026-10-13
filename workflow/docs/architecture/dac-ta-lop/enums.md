# Đặc tả lớp: gói `com.nhom3.pubgapp.model.enums`

Bản chữ của sơ đồ lớp cho gói này: 22 lớp. Mục lục và cách đọc ở [README.md](README.md).

Các kiểu liệt kê. Thêm giá trị mới là đổi cấu trúc dữ liệu: cần hỏi leader trước (`AGENTS.md`, luật R6).

| Lớp | Loại | Trách nhiệm |
| --- | --- | --- |
| [`UserStatus`](#userstatus) | enum | Trạng thái tài khoản. |
| [`VerificationStatus`](#verificationstatus) | enum | Trạng thái xác minh người bán. |
| [`Gender`](#gender) | enum | Giới tính trong thông tin định danh. |
| [`NotificationType`](#notificationtype) | enum | Loại thông báo. |
| [`ListingStatus`](#listingstatus) | enum | Trạng thái tin đăng. |
| [`TradeType`](#tradetype) | enum | Hình thức giao dịch: bán, trao đổi, cho tặng. |
| [`ItemCondition`](#itemcondition) | enum | Tình trạng món đồ: mới, như mới, đã sử dụng, đã sửa chữa, có lỗi. |
| [`SortOrder`](#sortorder) | enum | Cách sắp xếp kết quả. |
| [`MessageType`](#messagetype) | enum | Loại tin nhắn. |
| [`ImagePurpose`](#imagepurpose) | enum | Ảnh dùng cho việc gì. |
| [`OfferType`](#offertype) | enum | Loại offer: tiền, vật, hoặc cả hai. |
| [`OfferStatus`](#offerstatus) | enum | Trạng thái offer. |
| [`DealStatus`](#dealstatus) | enum | Trạng thái giao dịch. |
| [`DeliveryMethod`](#deliverymethod) | enum | Phương thức giao nhận. |
| [`PaymentStatus`](#paymentstatus) | enum | Trạng thái khoản ký quỹ. |
| [`ReportTarget`](#reporttarget) | enum | Đối tượng bị báo cáo. |
| [`ReportReason`](#reportreason) | enum | Lý do báo cáo chọn sẵn. |
| [`ReportStatus`](#reportstatus) | enum | Trạng thái báo cáo. |
| [`ComplaintStatus`](#complaintstatus) | enum | Trạng thái khiếu nại. |
| [`ComplaintReason`](#complaintreason) | enum | Lý do khiếu nại chọn sẵn. |
| [`ComplaintDecision`](#complaintdecision) | enum | Quyết định xử lý khiếu nại. |
| [`Priority`](#priority) | enum | Mức ưu tiên khiếu nại. |

### UserStatus

| Mục | Nội dung |
| --- | --- |
| Định danh | `com.nhom3.pubgapp.model.enums.UserStatus` |
| Loại | enum |
| Lưu trữ | Không lưu |
| Trách nhiệm | Trạng thái tài khoản. |
| Chức năng liên quan (STT) | 12, 68 |

| Giá trị | Nghĩa |
| --- | --- |
| `ACTIVE` | Đang hoạt động |
| `TEMP_LOCKED` | Khóa tạm thời |
| `PERM_LOCKED` | Khóa vĩnh viễn |
| `DELETED` | Đã vô hiệu hóa (người dùng tự xóa tài khoản) |

Lưu trên Firestore và Room bằng tên hằng (chuỗi). Chữ hiển thị lấy từ string resource, không dùng tên hằng.

**Được dùng bởi:** [`AdminRepository`](data.md#adminrepository), [`AdminUserViewModel`](viewmodel.md#adminuserviewmodel), [`User`](model.md#user), [`Warning`](model.md#warning)

### VerificationStatus

| Mục | Nội dung |
| --- | --- |
| Định danh | `com.nhom3.pubgapp.model.enums.VerificationStatus` |
| Loại | enum |
| Lưu trữ | Không lưu |
| Trách nhiệm | Trạng thái xác minh người bán. |
| Chức năng liên quan (STT) | 73 |

| Giá trị | Nghĩa |
| --- | --- |
| `NONE` | Chưa gửi yêu cầu |
| `PENDING` | Đang chờ duyệt |
| `APPROVED` | Đã xác minh |
| `REJECTED` | Bị từ chối |

Lưu trên Firestore và Room bằng tên hằng (chuỗi). Chữ hiển thị lấy từ string resource, không dùng tên hằng.

**Được dùng bởi:** [`SellerVerification`](model.md#sellerverification), [`User`](model.md#user)

### Gender

| Mục | Nội dung |
| --- | --- |
| Định danh | `com.nhom3.pubgapp.model.enums.Gender` |
| Loại | enum |
| Lưu trữ | Không lưu |
| Trách nhiệm | Giới tính trong thông tin định danh. |
| Chức năng liên quan (STT) | 9 |

| Giá trị | Nghĩa |
| --- | --- |
| `MALE` | Nam |
| `FEMALE` | Nữ |
| `OTHER` | Khác |

Lưu trên Firestore và Room bằng tên hằng (chuỗi). Chữ hiển thị lấy từ string resource, không dùng tên hằng.

**Được dùng bởi:** [`UserIdentity`](model.md#useridentity)

### NotificationType

| Mục | Nội dung |
| --- | --- |
| Định danh | `com.nhom3.pubgapp.model.enums.NotificationType` |
| Loại | enum |
| Lưu trữ | Không lưu |
| Trách nhiệm | Loại thông báo. |
| Chức năng liên quan (STT) | 47 đến 49, 67 |

| Giá trị | Nghĩa |
| --- | --- |
| `NEW_MESSAGE` | Tin nhắn mới |
| `NEW_OFFER` | Đề nghị giá mới |
| `OFFER_RESULT` | Đề nghị được chấp nhận hoặc bị từ chối |
| `DEAL_UPDATE` | Giao dịch đổi trạng thái |
| `PAYMENT_UPDATE` | Tiền được giữ, giải ngân hoặc hoàn |
| `NEW_REVIEW` | Có đánh giá mới |
| `COMPLAINT_RESULT` | Kết quả khiếu nại |
| `WARNING` | Bị cảnh báo hoặc khóa |
| `LISTING_MODERATED` | Tin bị admin ẩn, gỡ hoặc khôi phục |
| `VERIFICATION_RESULT` | Kết quả xác minh người bán |

Lưu trên Firestore và Room bằng tên hằng (chuỗi). Chữ hiển thị lấy từ string resource, không dùng tên hằng.

**Được dùng bởi:** [`AppNotification`](model.md#appnotification)

### ListingStatus

| Mục | Nội dung |
| --- | --- |
| Định danh | `com.nhom3.pubgapp.model.enums.ListingStatus` |
| Loại | enum |
| Lưu trữ | Không lưu |
| Trách nhiệm | Trạng thái tin đăng. |
| Chức năng liên quan (STT) | 13, 22 |

| Giá trị | Nghĩa |
| --- | --- |
| `DRAFT` | Nháp |
| `AVAILABLE` | Đang bán |
| `RESERVED` | Đang giữ chỗ (đã có giao dịch) |
| `SOLD` | Đã bán |
| `HIDDEN` | Đã ẩn (người bán tự ẩn hoặc admin ẩn) |
| `REMOVED` | Đã gỡ (admin) |

Lưu trên Firestore và Room bằng tên hằng (chuỗi). Chữ hiển thị lấy từ string resource, không dùng tên hằng.

**Được dùng bởi:** [`Listing`](model.md#listing), [`ListingRepository`](data.md#listingrepository), [`ShopViewModel`](viewmodel.md#shopviewmodel)

### TradeType

| Mục | Nội dung |
| --- | --- |
| Định danh | `com.nhom3.pubgapp.model.enums.TradeType` |
| Loại | enum |
| Lưu trữ | Không lưu |
| Trách nhiệm | Hình thức giao dịch: bán, trao đổi, cho tặng. |
| Chức năng liên quan (STT) | 18 |

| Giá trị | Nghĩa |
| --- | --- |
| `SELL` | Bán |
| `EXCHANGE` | Trao đổi |
| `GIVEAWAY` | Cho tặng |

Lưu trên Firestore và Room bằng tên hằng (chuỗi). Chữ hiển thị lấy từ string resource, không dùng tên hằng.

**Được dùng bởi:** [`Deal`](model.md#deal), [`Listing`](model.md#listing), [`SearchFilter`](model.md#searchfilter)

### ItemCondition

| Mục | Nội dung |
| --- | --- |
| Định danh | `com.nhom3.pubgapp.model.enums.ItemCondition` |
| Loại | enum |
| Lưu trữ | Không lưu |
| Trách nhiệm | Tình trạng món đồ: mới, như mới, đã sử dụng, đã sửa chữa, có lỗi. |
| Chức năng liên quan (STT) | 16, 26 |

| Giá trị | Nghĩa |
| --- | --- |
| `NEW` | Mới |
| `LIKE_NEW` | Như mới |
| `USED` | Đã sử dụng |
| `REPAIRED` | Đã sửa chữa |
| `DEFECTIVE` | Có lỗi |

Lưu trên Firestore và Room bằng tên hằng (chuỗi). Chữ hiển thị lấy từ string resource, không dùng tên hằng.

**Được dùng bởi:** [`Listing`](model.md#listing), [`SearchFilter`](model.md#searchfilter)

### SortOrder

| Mục | Nội dung |
| --- | --- |
| Định danh | `com.nhom3.pubgapp.model.enums.SortOrder` |
| Loại | enum |
| Lưu trữ | Không lưu |
| Trách nhiệm | Cách sắp xếp kết quả. |
| Chức năng liên quan (STT) | 14, 31 đến 34 |

| Giá trị | Nghĩa |
| --- | --- |
| `NEWEST` | Mới đăng |
| `PRICE_ASC` | Giá thấp đến cao |
| `PRICE_DESC` | Giá cao đến thấp |
| `TOP_RATED` | Người bán đánh giá tốt |

Lưu trên Firestore và Room bằng tên hằng (chuỗi). Chữ hiển thị lấy từ string resource, không dùng tên hằng.

**Được dùng bởi:** [`FeedViewModel`](viewmodel.md#feedviewmodel), [`SearchFilter`](model.md#searchfilter), [`ShopViewModel`](viewmodel.md#shopviewmodel)

### MessageType

| Mục | Nội dung |
| --- | --- |
| Định danh | `com.nhom3.pubgapp.model.enums.MessageType` |
| Loại | enum |
| Lưu trữ | Không lưu |
| Trách nhiệm | Loại tin nhắn. |
| Chức năng liên quan (STT) | 43, 44 |

| Giá trị | Nghĩa |
| --- | --- |
| `TEXT` | Chữ |
| `IMAGE` | Ảnh |
| `OFFER` | Đề nghị giá |

Lưu trên Firestore và Room bằng tên hằng (chuỗi). Chữ hiển thị lấy từ string resource, không dùng tên hằng.

**Được dùng bởi:** [`ChatMessage`](model.md#chatmessage)

### ImagePurpose

| Mục | Nội dung |
| --- | --- |
| Định danh | `com.nhom3.pubgapp.model.enums.ImagePurpose` |
| Loại | enum |
| Lưu trữ | Không lưu |
| Trách nhiệm | Ảnh dùng cho việc gì. |
| Chức năng liên quan (STT) | 17 |

| Giá trị | Nghĩa |
| --- | --- |
| `LISTING` | Ảnh tin đăng |
| `CHAT` | Ảnh gửi trong chat |
| `WAYBILL` | Ảnh vận đơn |
| `EVIDENCE` | Ảnh bằng chứng khiếu nại |

Lưu trên Firestore và Room bằng tên hằng (chuỗi). Chữ hiển thị lấy từ string resource, không dùng tên hằng.

**Được dùng bởi:** [`MediaImage`](model.md#mediaimage), [`MediaRepository`](data.md#mediarepository)

### OfferType

| Mục | Nội dung |
| --- | --- |
| Định danh | `com.nhom3.pubgapp.model.enums.OfferType` |
| Loại | enum |
| Lưu trữ | Không lưu |
| Trách nhiệm | Loại offer: tiền, vật, hoặc cả hai. |
| Chức năng liên quan (STT) | 20 |

| Giá trị | Nghĩa |
| --- | --- |
| `MONEY` | Tiền |
| `ITEM` | Vật trao đổi |
| `MONEY_AND_ITEM` | Tiền và vật |

Lưu trên Firestore và Room bằng tên hằng (chuỗi). Chữ hiển thị lấy từ string resource, không dùng tên hằng.

**Được dùng bởi:** [`Listing`](model.md#listing), [`Offer`](model.md#offer)

### OfferStatus

| Mục | Nội dung |
| --- | --- |
| Định danh | `com.nhom3.pubgapp.model.enums.OfferStatus` |
| Loại | enum |
| Lưu trữ | Không lưu |
| Trách nhiệm | Trạng thái offer. |
| Chức năng liên quan (STT) | 47 đến 49 |

| Giá trị | Nghĩa |
| --- | --- |
| `PENDING` | Chờ người bán trả lời |
| `ACCEPTED` | Đã chấp nhận |
| `REJECTED` | Đã từ chối |

Lưu trên Firestore và Room bằng tên hằng (chuỗi). Chữ hiển thị lấy từ string resource, không dùng tên hằng.

**Được dùng bởi:** [`Offer`](model.md#offer)

### DealStatus

| Mục | Nội dung |
| --- | --- |
| Định danh | `com.nhom3.pubgapp.model.enums.DealStatus` |
| Loại | enum |
| Lưu trữ | Không lưu |
| Trách nhiệm | Trạng thái giao dịch. |
| Chức năng liên quan (STT) | 52 |

| Giá trị | Nghĩa |
| --- | --- |
| `PENDING_CONFIRM` | Chờ hai bên xác nhận |
| `CONFIRMED` | Đã xác nhận |
| `SHIPPED` | Đã giao, đang đếm ngược |
| `RECEIVED` | Đã nhận, tiền đã giải ngân |
| `COMPLETED` | Hoàn tất |
| `CANCELLED` | Đã hủy |
| `DISPUTED` | Đang khiếu nại |

Lưu trên Firestore và Room bằng tên hằng (chuỗi). Chữ hiển thị lấy từ string resource, không dùng tên hằng.

**Được dùng bởi:** [`AdminDealViewModel`](viewmodel.md#admindealviewmodel), [`Deal`](model.md#deal), [`DealRepository`](data.md#dealrepository), [`StatusChange`](model.md#statuschange)

### DeliveryMethod

| Mục | Nội dung |
| --- | --- |
| Định danh | `com.nhom3.pubgapp.model.enums.DeliveryMethod` |
| Loại | enum |
| Lưu trữ | Không lưu |
| Trách nhiệm | Phương thức giao nhận. |
| Chức năng liên quan (STT) | 50 |

| Giá trị | Nghĩa |
| --- | --- |
| `MEET_UP` | Gặp trực tiếp |
| `SHIPPING` | Gửi qua đơn vị vận chuyển |

Lưu trên Firestore và Room bằng tên hằng (chuỗi). Chữ hiển thị lấy từ string resource, không dùng tên hằng.

**Được dùng bởi:** [`Deal`](model.md#deal)

### PaymentStatus

| Mục | Nội dung |
| --- | --- |
| Định danh | `com.nhom3.pubgapp.model.enums.PaymentStatus` |
| Loại | enum |
| Lưu trữ | Không lưu |
| Trách nhiệm | Trạng thái khoản ký quỹ. |
| Chức năng liên quan (STT) | 56, 84 |

| Giá trị | Nghĩa |
| --- | --- |
| `HELD` | Đang giữ |
| `RELEASED` | Đã giải ngân cho người bán |
| `REFUNDED` | Đã hoàn cho người mua |

Lưu trên Firestore và Room bằng tên hằng (chuỗi). Chữ hiển thị lấy từ string resource, không dùng tên hằng.

**Được dùng bởi:** [`Payment`](model.md#payment)

### ReportTarget

| Mục | Nội dung |
| --- | --- |
| Định danh | `com.nhom3.pubgapp.model.enums.ReportTarget` |
| Loại | enum |
| Lưu trữ | Không lưu |
| Trách nhiệm | Đối tượng bị báo cáo. |
| Chức năng liên quan (STT) | 61 |

| Giá trị | Nghĩa |
| --- | --- |
| `LISTING` | Tin đăng |
| `USER` | Người dùng |
| `MESSAGE` | Tin nhắn |

Lưu trên Firestore và Room bằng tên hằng (chuỗi). Chữ hiển thị lấy từ string resource, không dùng tên hằng.

**Được dùng bởi:** [`Report`](model.md#report)

### ReportReason

| Mục | Nội dung |
| --- | --- |
| Định danh | `com.nhom3.pubgapp.model.enums.ReportReason` |
| Loại | enum |
| Lưu trữ | Không lưu |
| Trách nhiệm | Lý do báo cáo chọn sẵn. |
| Chức năng liên quan (STT) | 61 |

| Giá trị | Nghĩa |
| --- | --- |
| `FAKE_ITEM` | Nghi ngờ hàng giả |
| `MISLEADING_INFO` | Thông tin sai lệch |
| `SCAM` | Dấu hiệu lừa đảo |
| `INAPPROPRIATE` | Nội dung không phù hợp |
| `OTHER` | Khác |

Lưu trên Firestore và Room bằng tên hằng (chuỗi). Chữ hiển thị lấy từ string resource, không dùng tên hằng.

**Được dùng bởi:** [`Report`](model.md#report)

### ReportStatus

| Mục | Nội dung |
| --- | --- |
| Định danh | `com.nhom3.pubgapp.model.enums.ReportStatus` |
| Loại | enum |
| Lưu trữ | Không lưu |
| Trách nhiệm | Trạng thái báo cáo. |
| Chức năng liên quan (STT) | 62 |

| Giá trị | Nghĩa |
| --- | --- |
| `OPEN` | Chờ xử lý |
| `RESOLVED` | Đã xử lý, có vi phạm |
| `DISMISSED` | Đã xem, không vi phạm |
| `CANCELLED` | Người báo cáo đã hủy |

Lưu trên Firestore và Room bằng tên hằng (chuỗi). Chữ hiển thị lấy từ string resource, không dùng tên hằng.

**Được dùng bởi:** [`Report`](model.md#report), [`ReportRepository`](data.md#reportrepository)

### ComplaintStatus

| Mục | Nội dung |
| --- | --- |
| Định danh | `com.nhom3.pubgapp.model.enums.ComplaintStatus` |
| Loại | enum |
| Lưu trữ | Không lưu |
| Trách nhiệm | Trạng thái khiếu nại. |
| Chức năng liên quan (STT) | 65 |

| Giá trị | Nghĩa |
| --- | --- |
| `OPEN` | Chờ xử lý |
| `NEEDS_EVIDENCE` | Chờ bổ sung bằng chứng |
| `RESOLVED` | Đã có quyết định |
| `WITHDRAWN` | Người khiếu nại đã rút |

Lưu trên Firestore và Room bằng tên hằng (chuỗi). Chữ hiển thị lấy từ string resource, không dùng tên hằng.

**Được dùng bởi:** [`AdminComplaintViewModel`](viewmodel.md#admincomplaintviewmodel), [`Complaint`](model.md#complaint), [`ComplaintRepository`](data.md#complaintrepository)

### ComplaintReason

| Mục | Nội dung |
| --- | --- |
| Định danh | `com.nhom3.pubgapp.model.enums.ComplaintReason` |
| Loại | enum |
| Lưu trữ | Không lưu |
| Trách nhiệm | Lý do khiếu nại chọn sẵn. |
| Chức năng liên quan (STT) | 64 |

| Giá trị | Nghĩa |
| --- | --- |
| `NOT_AS_DESCRIBED` | Hàng không đúng mô tả |
| `NOT_RECEIVED` | Không nhận được hàng |
| `DAMAGED` | Hàng bị hư hỏng |
| `OTHER` | Khác |

Lưu trên Firestore và Room bằng tên hằng (chuỗi). Chữ hiển thị lấy từ string resource, không dùng tên hằng.

**Được dùng bởi:** [`Complaint`](model.md#complaint)

### ComplaintDecision

| Mục | Nội dung |
| --- | --- |
| Định danh | `com.nhom3.pubgapp.model.enums.ComplaintDecision` |
| Loại | enum |
| Lưu trữ | Không lưu |
| Trách nhiệm | Quyết định xử lý khiếu nại. |
| Chức năng liên quan (STT) | 89 |

| Giá trị | Nghĩa |
| --- | --- |
| `REFUND` | Hoàn tiền cho người mua |
| `RETURN_ITEM` | Trả hàng rồi hoàn tiền |
| `CANCEL_DEAL` | Hủy giao dịch |
| `REJECT` | Bác khiếu nại, giải ngân cho người bán |
| `REQUEST_EVIDENCE` | Yêu cầu bổ sung bằng chứng |

Lưu trên Firestore và Room bằng tên hằng (chuỗi). Chữ hiển thị lấy từ string resource, không dùng tên hằng.

**Được dùng bởi:** [`AdminComplaintViewModel`](viewmodel.md#admincomplaintviewmodel), [`Complaint`](model.md#complaint), [`ComplaintRepository`](data.md#complaintrepository)

### Priority

| Mục | Nội dung |
| --- | --- |
| Định danh | `com.nhom3.pubgapp.model.enums.Priority` |
| Loại | enum |
| Lưu trữ | Không lưu |
| Trách nhiệm | Mức ưu tiên khiếu nại. |
| Chức năng liên quan (STT) | 87 |

| Giá trị | Nghĩa |
| --- | --- |
| `LOW` | Thấp |
| `MEDIUM` | Trung bình |
| `HIGH` | Cao |

Lưu trên Firestore và Room bằng tên hằng (chuỗi). Chữ hiển thị lấy từ string resource, không dùng tên hằng.

**Được dùng bởi:** [`AdminComplaintViewModel`](viewmodel.md#admincomplaintviewmodel), [`Complaint`](model.md#complaint), [`ComplaintRepository`](data.md#complaintrepository)
