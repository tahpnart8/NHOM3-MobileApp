package com.nhom3.pubgapp.data;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.WriteBatch;
import com.nhom3.pubgapp.model.AppNotification;
import com.nhom3.pubgapp.model.Deal;
import com.nhom3.pubgapp.model.Listing;
import com.nhom3.pubgapp.model.Offer;
import com.nhom3.pubgapp.model.Payment;
import com.nhom3.pubgapp.model.StatusChange;
import com.nhom3.pubgapp.model.enums.DealStatus;
import com.nhom3.pubgapp.model.enums.DeliveryMethod;
import com.nhom3.pubgapp.model.enums.ListingStatus;
import com.nhom3.pubgapp.model.enums.NotificationType;
import com.nhom3.pubgapp.model.enums.OfferType;
import com.nhom3.pubgapp.model.enums.PaymentMethod;
import com.nhom3.pubgapp.model.enums.PaymentStatus;
import com.nhom3.pubgapp.model.enums.TradeType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DealRepository extends BaseRepository {

    private static final String COLLECTION_DEALS = "deals";
    private static final String COLLECTION_PAYMENTS = "payments";
    private static final String COLLECTION_LISTINGS = "listings";
    private static final String COLLECTION_NOTIFICATIONS = "notifications";

    public Deal buildFromOffer(Offer offer, Listing listing) {
        Deal deal = new Deal();
        deal.setListingId(offer.getListingId());
        deal.setOfferId(offer.getId());
        deal.setBuyerId(offer.getBuyerId());
        deal.setSellerId(offer.getSellerId());

        if (offer.getOfferType() == OfferType.ITEM) {
            deal.setTradeType(TradeType.EXCHANGE);
        } else {
            deal.setTradeType(TradeType.SELL);
        }

        deal.setAgreedPrice(offer.getAmount());
        deal.setExchangeItem(offer.getExchangeItem());
        deal.setDeliveryMethod(DeliveryMethod.MEET_UP);
        deal.setDeliveryAddress("");
        deal.setAgreedTerms("");

        if (listing != null) {
            deal.setItemTitle(listing.getTitle());
            deal.setItemThumb(listing.getCoverThumb());
        } else {
            deal.setItemTitle("");
            deal.setItemThumb("");
        }

        deal.setStatus(DealStatus.PENDING_CONFIRM);
        deal.setSellerConfirmed(true);
        deal.setBuyerConfirmed(false);
        deal.setCreatedAt(Timestamp.now());
        deal.setUpdatedAt(Timestamp.now());

        List<StatusChange> history = new ArrayList<>();
        StatusChange initial = new StatusChange();
        initial.setFromStatus(null);
        initial.setToStatus(DealStatus.PENDING_CONFIRM);
        initial.setChangedBy(offer.getSellerId());
        initial.setReason("");
        initial.setChangedAt(Timestamp.now());
        history.add(initial);
        deal.setStatusHistory(history);

        return deal;
    }

    public void getById(String dealId, DataCallback<Deal> cb) {
        db.collection(COLLECTION_DEALS).document(dealId).get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (!task.isSuccessful() || task.getResult() == null || !task.getResult().exists()) {
                            cb.onError(getErrorMessage(task.getException(), "Không tìm thấy giao dịch."));
                            return;
                        }
                        Deal deal = task.getResult().toObject(Deal.class);
                        if (deal != null) {
                            deal.setId(task.getResult().getId());
                        }
                        cb.onSuccess(deal);
                    }
                });
    }

    public void getPayment(String dealId, DataCallback<Payment> cb) {
        db.collection(COLLECTION_PAYMENTS).document(dealId).get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (!task.isSuccessful() || task.getResult() == null || !task.getResult().exists()) {
                            cb.onError(getErrorMessage(task.getException(),
                                    "Không tìm thấy khoản ký quỹ."));
                            return;
                        }
                        Payment payment = task.getResult().toObject(Payment.class);
                        if (payment != null) {
                            payment.setId(task.getResult().getId());
                        }
                        cb.onSuccess(payment);
                    }
                });
    }

    public void updateTerms(Deal deal, DataCallback<Void> cb) {
        // Khi sửa điều khoản: xóa cả hai cờ xác nhận, giữ nguyên trạng thái
        deal.setBuyerConfirmed(false);
        deal.setSellerConfirmed(false);
        deal.setUpdatedAt(Timestamp.now());

        WriteBatch batch = db.batch();
        DocumentReference dealRef = db.collection(COLLECTION_DEALS).document(deal.getId());
        batch.set(dealRef, deal);

        String currentUid = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : "";
        String otherParty = deal.isParticipant(currentUid)
                ? (currentUid.equals(deal.getBuyerId()) ? deal.getSellerId() : deal.getBuyerId())
                : null;
        if (otherParty != null) {
            queueNotification(otherParty, NotificationType.DEAL_UPDATE,
                    "Điều khoản giao dịch đã cập nhật",
                    "Đối tác đã sửa đổi điều khoản giao dịch. Vui lòng xác nhận lại.",
                    deal.getId(), batch);
        }

        commitBatch(batch, cb, "Lỗi cập nhật điều khoản.");
    }

    public void confirm(String dealId, DataCallback<Void> cb) {
        DocumentReference dealRef = db.collection(COLLECTION_DEALS).document(dealId);
        dealRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (!task.isSuccessful() || task.getResult() == null || !task.getResult().exists()) {
                    cb.onError("Không tìm thấy giao dịch.");
                    return;
                }
                Deal deal = task.getResult().toObject(Deal.class);
                if (deal == null) {
                    cb.onError("Dữ liệu giao dịch không hợp lệ.");
                    return;
                }
                deal.setId(dealId);

                if (deal.getDeliveryMethod() == null) {
                    cb.onError("Chưa chọn phương thức giao nhận.");
                    return;
                }
                if (deal.getDeliveryMethod() == DeliveryMethod.SHIPPING) {
                    String addr = deal.getDeliveryAddress();
                    if (addr == null || addr.trim().isEmpty()) {
                        cb.onError("Chưa có địa chỉ giao hàng.");
                        return;
                    }
                }

                String currentUid = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : "";
                DealStatus previousStatus = deal.getStatus();
                deal.confirmBy(currentUid);

                WriteBatch batch = db.batch();
                if (previousStatus == DealStatus.PENDING_CONFIRM
                        && deal.getStatus() == DealStatus.CONFIRMED) {
                    addStatusHistory(deal, previousStatus, DealStatus.CONFIRMED, currentUid, "");
                }
                deal.setUpdatedAt(Timestamp.now());
                batch.set(dealRef, deal);

                String otherParty = currentUid.equals(deal.getBuyerId())
                        ? deal.getSellerId() : deal.getBuyerId();
                queueNotification(otherParty, NotificationType.DEAL_UPDATE,
                        "Cập nhật xác nhận giao dịch",
                        "Đối tác đã xác nhận giao dịch.", dealId, batch);

                commitBatch(batch, cb, "Lỗi xác nhận giao dịch.");
            }
        });
    }

    public void pay(String dealId, PaymentMethod method, DataCallback<Void> cb) {
        DocumentReference dealRef = db.collection(COLLECTION_DEALS).document(dealId);
        dealRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (!task.isSuccessful() || task.getResult() == null || !task.getResult().exists()) {
                    cb.onError("Không tìm thấy giao dịch.");
                    return;
                }
                Deal deal = task.getResult().toObject(Deal.class);
                if (deal == null || deal.getStatus() != DealStatus.CONFIRMED) {
                    cb.onError("Giao dịch chưa được xác nhận để thanh toán.");
                    return;
                }
                if (!deal.needsPayment()) {
                    cb.onError("Giao dịch này không yêu cầu thanh toán.");
                    return;
                }

                String currentUid = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : "";
                if (!currentUid.equals(deal.getBuyerId())) {
                    cb.onError("Chỉ người mua mới được thanh toán.");
                    return;
                }

                Payment payment = new Payment();
                payment.setDealId(dealId);
                payment.setPayerId(deal.getBuyerId());
                payment.setPayeeId(deal.getSellerId());
                payment.setAmount(deal.getAgreedPrice());
                payment.setStatus(PaymentStatus.HELD);
                payment.setMethod(method);
                payment.setHeldAt(Timestamp.now());
                payment.setCreatedAt(Timestamp.now());

                WriteBatch batch = db.batch();
                DocumentReference paymentRef = db.collection(COLLECTION_PAYMENTS).document(dealId);
                batch.set(paymentRef, payment);

                queueNotification(deal.getSellerId(), NotificationType.PAYMENT_UPDATE,
                        "Đã thanh toán ký quỹ",
                        "Người mua đã thanh toán tiền ký quỹ vào hệ thống.",
                        dealId, batch);

                commitBatch(batch, cb, "Lỗi thanh toán.");
            }
        });
    }

    public void markShipped(String dealId, String carrier, String trackingCode,
                            String imageId, DataCallback<Void> cb) {
        DocumentReference dealRef = db.collection(COLLECTION_DEALS).document(dealId);
        dealRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (!task.isSuccessful() || task.getResult() == null || !task.getResult().exists()) {
                    cb.onError("Không tìm thấy giao dịch.");
                    return;
                }
                Deal deal = task.getResult().toObject(Deal.class);
                if (deal == null || deal.getStatus() != DealStatus.CONFIRMED) {
                    cb.onError("Giao dịch chưa ở trạng thái sẵn sàng giao.");
                    return;
                }
                deal.setId(dealId);

                if (deal.needsPayment()) {
                    checkPaymentBeforeShipping(deal, carrier, trackingCode, imageId, dealRef, cb);
                } else {
                    executeMarkShipped(deal, carrier, trackingCode, imageId, dealRef, cb);
                }
            }
        });
    }

    private void checkPaymentBeforeShipping(Deal deal, String carrier, String trackingCode,
                                           String imageId, DocumentReference dealRef,
                                           DataCallback<Void> cb) {
        db.collection(COLLECTION_PAYMENTS).document(deal.getId()).get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (!task.isSuccessful() || task.getResult() == null || !task.getResult().exists()) {
                            cb.onError("Chưa tìm thấy khoản thanh toán ký quỹ.");
                            return;
                        }
                        Payment payment = task.getResult().toObject(Payment.class);
                        if (payment == null || !payment.isHeld()) {
                            cb.onError("Khoản ký quỹ chưa được thanh toán.");
                            return;
                        }
                        executeMarkShipped(deal, carrier, trackingCode, imageId, dealRef, cb);
                    }
                });
    }

    private void executeMarkShipped(Deal deal, String carrier, String trackingCode,
                                    String imageId, DocumentReference dealRef,
                                    DataCallback<Void> cb) {
        deal.markShipped(carrier, trackingCode, imageId);
        if (deal.getStatus() != DealStatus.SHIPPED) {
            cb.onError("Không đủ thông tin bằng chứng vận đơn để giao hàng.");
            return;
        }

        String currentUid = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : deal.getSellerId();
        addStatusHistory(deal, DealStatus.CONFIRMED, DealStatus.SHIPPED, currentUid, "");
        deal.setUpdatedAt(Timestamp.now());

        WriteBatch batch = db.batch();
        batch.set(dealRef, deal);

        queueNotification(deal.getBuyerId(), NotificationType.DEAL_UPDATE,
                "Đơn hàng đang được giao",
                "Người bán đã gửi hàng kèm mã vận đơn " + trackingCode,
                deal.getId(), batch);

        commitBatch(batch, cb, "Lỗi cập nhật giao hàng.");
    }

    public void markReceived(String dealId, String imageId, DataCallback<Void> cb) {
        DocumentReference dealRef = db.collection(COLLECTION_DEALS).document(dealId);
        dealRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (!task.isSuccessful() || task.getResult() == null || !task.getResult().exists()) {
                    cb.onError("Không tìm thấy giao dịch.");
                    return;
                }
                Deal deal = task.getResult().toObject(Deal.class);
                if (deal == null) {
                    cb.onError("Dữ liệu giao dịch không hợp lệ.");
                    return;
                }
                deal.setId(dealId);
                executeMarkReceived(deal, imageId, dealRef, cb);
            }
        });
    }

    private void executeMarkReceived(Deal deal, String imageId, DocumentReference dealRef,
                                     DataCallback<Void> cb) {
        DealStatus from = deal.getStatus();
        deal.markReceived(imageId);

        String currentUid = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : deal.getBuyerId();
        addStatusHistory(deal, from, DealStatus.RECEIVED, currentUid, "");
        deal.setUpdatedAt(Timestamp.now());

        WriteBatch batch = db.batch();
        batch.set(dealRef, deal);

        // Giải ngân khoản ký quỹ nếu có
        if (deal.needsPayment()) {
            DocumentReference payRef = db.collection(COLLECTION_PAYMENTS).document(deal.getId());
            batch.update(payRef, "status", PaymentStatus.RELEASED.name(),
                    "releasedAt", deal.getReceivedAt());
        }

        queueNotification(deal.getSellerId(), NotificationType.PAYMENT_UPDATE,
                "Người mua đã nhận hàng",
                "Người mua đã xác nhận nhận hàng. Tiền ký quỹ đã được giải ngân.",
                deal.getId(), batch);

        commitBatch(batch, cb, "Lỗi xác nhận đã nhận hàng.");
    }

    public void completeDeal(String dealId, DataCallback<Void> cb) {
        DocumentReference dealRef = db.collection(COLLECTION_DEALS).document(dealId);
        dealRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (!task.isSuccessful() || task.getResult() == null || !task.getResult().exists()) {
                    cb.onError("Không tìm thấy giao dịch.");
                    return;
                }
                Deal deal = task.getResult().toObject(Deal.class);
                if (deal == null) {
                    cb.onError("Dữ liệu giao dịch không hợp lệ.");
                    return;
                }
                deal.setId(dealId);
                DealStatus from = deal.getStatus();
                deal.complete();

                addStatusHistory(deal, from, DealStatus.COMPLETED, "system", "");
                deal.setUpdatedAt(Timestamp.now());

                WriteBatch batch = db.batch();
                batch.set(dealRef, deal);

                // Cập nhật tin đăng sang SOLD
                if (deal.getListingId() != null) {
                    DocumentReference listingRef = db.collection(COLLECTION_LISTINGS)
                            .document(deal.getListingId());
                    batch.update(listingRef, "status", ListingStatus.SOLD.name());
                }

                // Thông báo cho cả hai bên
                queueNotification(deal.getBuyerId(), NotificationType.DEAL_UPDATE,
                        "Giao dịch hoàn tất", "Giao dịch đã kết thúc thành công.",
                        dealId, batch);
                queueNotification(deal.getSellerId(), NotificationType.DEAL_UPDATE,
                        "Giao dịch hoàn tất", "Giao dịch đã kết thúc thành công.",
                        dealId, batch);

                commitBatch(batch, cb, "Lỗi hoàn tất giao dịch.");
            }
        });
    }

    public void releaseDueDeals(DataCallback<Integer> cb) {
        String uid = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : "";
        Timestamp now = Timestamp.now();

        Query buyerQuery = db.collection(COLLECTION_DEALS)
                .whereEqualTo("buyerId", uid)
                .whereEqualTo("status", DealStatus.SHIPPED.name());

        buyerQuery.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (!task.isSuccessful()) {
                    cb.onError(getErrorMessage(task.getException(), "Lỗi kiểm tra giao dịch đến hạn."));
                    return;
                }
                int count = 0;
                WriteBatch batch = db.batch();
                if (task.getResult() != null) {
                    for (QueryDocumentSnapshot doc : task.getResult()) {
                        Deal deal = doc.toObject(Deal.class);
                        deal.setId(doc.getId());
                        if (deal.isAutoReleaseDue(now)) {
                            deal.autoReceive();
                            addStatusHistory(deal, DealStatus.SHIPPED, DealStatus.RECEIVED, "system", "");
                            deal.setUpdatedAt(now);
                            batch.set(doc.getReference(), deal);
                            if (deal.needsPayment()) {
                                DocumentReference payRef = db.collection(COLLECTION_PAYMENTS)
                                        .document(deal.getId());
                                batch.update(payRef, "status", PaymentStatus.RELEASED.name(),
                                        "releasedAt", now);
                            }
                            count++;
                        }
                    }
                }
                final int finalCount = count;
                batch.commit().addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> commitTask) {
                        if (commitTask.isSuccessful()) {
                            cb.onSuccess(finalCount);
                        } else {
                            cb.onError(getErrorMessage(commitTask.getException(),
                                    "Lỗi cập nhật giải ngân tự động."));
                        }
                    }
                });
            }
        });
    }

    public void completeDueDeals(DataCallback<Integer> cb) {
        String uid = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : "";
        Timestamp now = Timestamp.now();

        db.collection(COLLECTION_DEALS)
                .whereEqualTo("buyerId", uid)
                .whereEqualTo("status", DealStatus.RECEIVED.name())
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (!task.isSuccessful() || task.getResult() == null) {
                            cb.onSuccess(0);
                            return;
                        }
                        int count = 0;
                        for (QueryDocumentSnapshot doc : task.getResult()) {
                            Deal deal = doc.toObject(Deal.class);
                            deal.setId(doc.getId());
                            if (deal.isAutoCompleteDue(now)) {
                                completeDeal(deal.getId(), new DataCallback<Void>() {
                                    @Override
                                    public void onSuccess(Void result) {
                                    }

                                    @Override
                                    public void onError(String message) {
                                    }
                                });
                                count++;
                            }
                        }
                        cb.onSuccess(count);
                    }
                });
    }

    public void expirePendingDeals(DataCallback<Integer> cb) {
        String uid = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : "";
        Timestamp now = Timestamp.now();

        db.collection(COLLECTION_DEALS)
                .whereEqualTo("buyerId", uid)
                .whereEqualTo("status", DealStatus.PENDING_CONFIRM.name())
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (!task.isSuccessful() || task.getResult() == null) {
                            cb.onSuccess(0);
                            return;
                        }
                        int count = 0;
                        for (QueryDocumentSnapshot doc : task.getResult()) {
                            Deal deal = doc.toObject(Deal.class);
                            deal.setId(doc.getId());
                            if (deal.isPendingExpired(now)) {
                                cancel(deal.getId(), "Quá 3 ngày không đủ xác nhận",
                                        new DataCallback<Void>() {
                                            @Override
                                            public void onSuccess(Void result) {
                                            }

                                            @Override
                                            public void onError(String message) {
                                            }
                                        });
                                count++;
                            }
                        }
                        cb.onSuccess(count);
                    }
                });
    }

    public void cancel(String dealId, String reason, DataCallback<Void> cb) {
        DocumentReference dealRef = db.collection(COLLECTION_DEALS).document(dealId);
        dealRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (!task.isSuccessful() || task.getResult() == null || !task.getResult().exists()) {
                    cb.onError("Không tìm thấy giao dịch.");
                    return;
                }
                Deal deal = task.getResult().toObject(Deal.class);
                if (deal == null) {
                    cb.onError("Dữ liệu giao dịch không hợp lệ.");
                    return;
                }
                deal.setId(dealId);
                String currentUid = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : "";
                DealStatus from = deal.getStatus();
                deal.cancel(currentUid, reason);

                addStatusHistory(deal, from, DealStatus.CANCELLED, currentUid, reason);
                deal.setUpdatedAt(Timestamp.now());

                WriteBatch batch = db.batch();
                batch.set(dealRef, deal);

                // Tin đăng trở về AVAILABLE
                if (deal.getListingId() != null) {
                    DocumentReference listingRef = db.collection(COLLECTION_LISTINGS)
                            .document(deal.getListingId());
                    batch.update(listingRef, "status", ListingStatus.AVAILABLE.name());
                }

                // Hoàn tiền ký quỹ nếu có
                if (deal.needsPayment()) {
                    DocumentReference payRef = db.collection(COLLECTION_PAYMENTS).document(dealId);
                    batch.update(payRef, "status", PaymentStatus.REFUNDED.name(),
                            "releasedAt", Timestamp.now());
                }

                String otherParty = currentUid.equals(deal.getBuyerId())
                        ? deal.getSellerId() : deal.getBuyerId();
                queueNotification(otherParty, NotificationType.DEAL_UPDATE,
                        "Giao dịch đã bị hủy",
                        "Lý do: " + reason, dealId, batch);

                commitBatch(batch, cb, "Lỗi hủy giao dịch.");
            }
        });
    }

    public void getMyDeals(String uid, DataCallback<List<Deal>> cb) {
        db.collection(COLLECTION_DEALS).whereEqualTo("buyerId", uid).get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> buyerTask) {
                        if (!buyerTask.isSuccessful()) {
                            cb.onError(getErrorMessage(buyerTask.getException(), "Lỗi tải giao dịch."));
                            return;
                        }
                        db.collection(COLLECTION_DEALS).whereEqualTo("sellerId", uid).get()
                                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<QuerySnapshot> sellerTask) {
                                        if (!sellerTask.isSuccessful()) {
                                            cb.onError(getErrorMessage(sellerTask.getException(),
                                                    "Lỗi tải giao dịch."));
                                            return;
                                        }
                                        Map<String, Deal> map = new HashMap<>();
                                        addDealsToMap(buyerTask.getResult(), map);
                                        addDealsToMap(sellerTask.getResult(), map);

                                        List<Deal> list = new ArrayList<>(map.values());
                                        sortDealsByDate(list);
                                        cb.onSuccess(list);
                                    }
                                });
                    }
                });
    }

    private void addDealsToMap(QuerySnapshot snapshot, Map<String, Deal> map) {
        if (snapshot == null) {
            return;
        }
        for (QueryDocumentSnapshot doc : snapshot) {
            Deal deal = doc.toObject(Deal.class);
            deal.setId(doc.getId());
            map.put(deal.getId(), deal);
        }
    }

    private void sortDealsByDate(List<Deal> list) {
        Collections.sort(list, new Comparator<Deal>() {
            @Override
            public int compare(Deal a, Deal b) {
                Timestamp tA = a.getUpdatedAt();
                Timestamp tB = b.getUpdatedAt();
                if (tA == null && tB == null) {
                    return 0;
                }
                if (tA == null) {
                    return 1;
                }
                if (tB == null) {
                    return -1;
                }
                return tB.compareTo(tA);
            }
        });
    }

    public void getPayments(String uid, DataCallback<List<Payment>> cb) {
        db.collection(COLLECTION_PAYMENTS).whereEqualTo("payerId", uid).get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> payerTask) {
                        if (!payerTask.isSuccessful()) {
                            cb.onError(getErrorMessage(payerTask.getException(), "Lỗi tải thanh toán."));
                            return;
                        }
                        db.collection(COLLECTION_PAYMENTS).whereEqualTo("payeeId", uid).get()
                                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<QuerySnapshot> payeeTask) {
                                        if (!payeeTask.isSuccessful()) {
                                            cb.onError(getErrorMessage(payeeTask.getException(),
                                                    "Lỗi tải thanh toán."));
                                            return;
                                        }
                                        Map<String, Payment> map = new HashMap<>();
                                        addPaymentsToMap(payerTask.getResult(), map);
                                        addPaymentsToMap(payeeTask.getResult(), map);

                                        List<Payment> list = new ArrayList<>(map.values());
                                        sortPaymentsByDate(list);
                                        cb.onSuccess(list);
                                    }
                                });
                    }
                });
    }

    private void addPaymentsToMap(QuerySnapshot snapshot, Map<String, Payment> map) {
        if (snapshot == null) {
            return;
        }
        for (QueryDocumentSnapshot doc : snapshot) {
            Payment payment = doc.toObject(Payment.class);
            payment.setId(doc.getId());
            map.put(payment.getId(), payment);
        }
    }

    private void sortPaymentsByDate(List<Payment> list) {
        Collections.sort(list, new Comparator<Payment>() {
            @Override
            public int compare(Payment a, Payment b) {
                Timestamp tA = a.getHeldAt();
                Timestamp tB = b.getHeldAt();
                if (tA == null && tB == null) {
                    return 0;
                }
                if (tA == null) {
                    return 1;
                }
                if (tB == null) {
                    return -1;
                }
                return tB.compareTo(tA);
            }
        });
    }

    public void listAll(DealStatus status, DataCallback<List<Deal>> cb) {
        Query query;
        if (status != null) {
            query = db.collection(COLLECTION_DEALS)
                    .whereEqualTo("status", status.name())
                    .orderBy("updatedAt", Query.Direction.DESCENDING);
        } else {
            query = db.collection(COLLECTION_DEALS)
                    .orderBy("updatedAt", Query.Direction.DESCENDING);
        }
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (!task.isSuccessful()) {
                    cb.onError(getErrorMessage(task.getException(), "Lỗi tải danh sách giao dịch."));
                    return;
                }
                List<Deal> list = new ArrayList<>();
                if (task.getResult() != null) {
                    for (QueryDocumentSnapshot doc : task.getResult()) {
                        Deal deal = doc.toObject(Deal.class);
                        deal.setId(doc.getId());
                        list.add(deal);
                    }
                }
                cb.onSuccess(list);
            }
        });
    }

    private void addStatusHistory(Deal deal, DealStatus from, DealStatus to,
                                  String changedBy, String reason) {
        List<StatusChange> history = deal.getStatusHistory();
        if (history == null) {
            history = new ArrayList<>();
        }
        StatusChange sc = new StatusChange();
        sc.setFromStatus(from);
        sc.setToStatus(to);
        sc.setChangedBy(changedBy);
        sc.setReason(reason);
        sc.setChangedAt(Timestamp.now());
        history.add(sc);
        deal.setStatusHistory(history);
    }

    private void queueNotification(String recipientId, NotificationType type,
                                   String title, String body, String targetId, WriteBatch batch) {
        if (recipientId == null || recipientId.trim().isEmpty()) {
            return;
        }
        AppNotification notif = new AppNotification();
        notif.setRecipientId(recipientId);
        notif.setType(type);
        notif.setTitle(title);
        notif.setBody(body);
        notif.setTargetId(targetId);
        notif.setRead(false);
        notif.setCreatedAt(Timestamp.now());
        DocumentReference notifRef = db.collection(COLLECTION_NOTIFICATIONS).document();
        notif.setId(notifRef.getId());
        batch.set(notifRef, notif);
    }

    private void commitBatch(WriteBatch batch, DataCallback<Void> cb, String defaultErrorMessage) {
        batch.commit().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    cb.onSuccess(null);
                } else {
                    cb.onError(getErrorMessage(task.getException(), defaultErrorMessage));
                }
            }
        });
    }

    private String getErrorMessage(Exception exception, String fallback) {
        if (exception != null && exception.getMessage() != null) {
            return exception.getMessage();
        }
        return fallback;
    }
}
