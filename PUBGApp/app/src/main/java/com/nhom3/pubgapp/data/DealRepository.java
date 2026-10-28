package com.nhom3.pubgapp.data;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.WriteBatch;
import com.nhom3.pubgapp.data.local.AppDatabase;
import com.nhom3.pubgapp.data.local.DealDao;
import com.nhom3.pubgapp.data.local.DealEntity;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DealRepository extends BaseRepository {

    private static final String TAG = "DealRepository";
    private static final String COLLECTION_DEALS = "deals";
    private static final String COLLECTION_PAYMENTS = "payments";
    private static final String COLLECTION_LISTINGS = "listings";

    private final NotificationRepository notificationRepository = new NotificationRepository();
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    private DealDao getDealDao() {
        try {
            Context context = FirebaseApp.getInstance().getApplicationContext();
            if (context != null) {
                return AppDatabase.getInstance(context).dealDao();
            }
        } catch (Exception e) {
            Log.w(TAG, "Cannot access Room database", e);
        }
        return null;
    }

    public static Deal buildFromOffer(Offer offer, Listing listing) {
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
        deal.setExchangeItem(offer.getExchangeItem() != null ? offer.getExchangeItem() : "");
        deal.setDeliveryMethod(null);
        deal.setDeliveryAddress("");
        deal.setAgreedTerms("");
        deal.setCancelReason("");
        deal.setCarrierName("");
        deal.setTrackingCode("");
        deal.setTrackingImageId("");
        deal.setReceivedImageId("");

        if (listing != null) {
            deal.setItemTitle(listing.getTitle() != null ? listing.getTitle() : "");
            deal.setItemThumb(listing.getCoverThumb() != null ? listing.getCoverThumb() : "");
        } else {
            deal.setItemTitle("");
            deal.setItemThumb("");
        }

        deal.setStatus(DealStatus.PENDING_CONFIRM);
        deal.setSellerConfirmed(true);
        deal.setBuyerConfirmed(false);

        List<StatusChange> history = new ArrayList<StatusChange>();
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
                        if (!task.isSuccessful()) {
                            fail(cb, task.getException());
                            return;
                        }
                        DocumentSnapshot snapshot = task.getResult();
                        if (snapshot == null || !snapshot.exists()) {
                            cb.onError("Không tìm thấy giao dịch.");
                            return;
                        }
                        Deal deal = snapshot.toObject(Deal.class);
                        if (deal != null) {
                            deal.setId(snapshot.getId());
                        }
                        cb.onSuccess(deal);
                    }
                });
    }

    // Live version of getById for C03: a step taken by the other party shows at once. Null when deleted.
    public ListenerRegistration observeDeal(String dealId, DataCallback<Deal> cb) {
        return listenDocument(db.collection(COLLECTION_DEALS).document(dealId), Deal.class, cb);
    }

    // Live version of getPayment: null until the buyer pays.
    public ListenerRegistration observePayment(String dealId, DataCallback<Payment> cb) {
        return listenDocument(db.collection(COLLECTION_PAYMENTS).document(dealId), Payment.class, cb);
    }

    public void getPayment(String dealId, DataCallback<Payment> cb) {
        db.collection(COLLECTION_PAYMENTS).document(dealId).get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (!task.isSuccessful()) {
                            fail(cb, task.getException());
                            return;
                        }
                        DocumentSnapshot snapshot = task.getResult();
                        if (snapshot == null || !snapshot.exists()) {
                            cb.onSuccess(null);
                            return;
                        }
                        Payment payment = snapshot.toObject(Payment.class);
                        if (payment != null) {
                            payment.setId(snapshot.getId());
                        }
                        cb.onSuccess(payment);
                    }
                });
    }

    public void updateTerms(Deal deal, DataCallback<Void> cb) {
        String currentUid = currentUserId();
        if (currentUid.isEmpty()) {
            cb.onError("Chưa đăng nhập.");
            return;
        }
        if (!deal.isParticipant(currentUid)) {
            cb.onError("Bạn không phải thành viên của giao dịch này.");
            return;
        }
        if (deal.getStatus() != DealStatus.PENDING_CONFIRM) {
            cb.onError("Chỉ có thể sửa điều khoản khi đang chờ xác nhận.");
            return;
        }

        WriteBatch batch = db.batch();
        DocumentReference dealRef = db.collection(COLLECTION_DEALS).document(deal.getId());

        Map<String, Object> updates = new HashMap<String, Object>();
        updates.put("deliveryMethod", deal.getDeliveryMethod() != null ? deal.getDeliveryMethod().name() : null);
        updates.put("deliveryAddress", deal.getDeliveryAddress() != null ? deal.getDeliveryAddress() : "");
        updates.put("scheduledAt", deal.getScheduledAt());
        updates.put("agreedTerms", deal.getAgreedTerms() != null ? deal.getAgreedTerms() : "");
        updates.put("exchangeItem", deal.getExchangeItem() != null ? deal.getExchangeItem() : "");
        updates.put("agreedPrice", deal.getAgreedPrice());
        updates.put("buyerConfirmed", false);
        updates.put("sellerConfirmed", false);
        updates.put("updatedAt", FieldValue.serverTimestamp());
        batch.update(dealRef, updates);

        String otherParty = currentUid.equals(deal.getBuyerId()) ? deal.getSellerId() : deal.getBuyerId();
        sendNotification(otherParty, NotificationType.DEAL_UPDATE,
                "Điều khoản giao dịch đã cập nhật",
                "Đối tác đã sửa đổi điều khoản giao dịch. Vui lòng xác nhận lại.",
                deal.getId(), batch);

        commitBatch(batch, cb);
    }

    public void confirm(String dealId, DataCallback<Void> cb) {
        String currentUid = currentUserId();
        if (currentUid.isEmpty()) {
            cb.onError("Chưa đăng nhập.");
            return;
        }

        DocumentReference dealRef = db.collection(COLLECTION_DEALS).document(dealId);
        dealRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (!task.isSuccessful()) {
                    fail(cb, task.getException());
                    return;
                }
                DocumentSnapshot snapshot = task.getResult();
                if (snapshot == null || !snapshot.exists()) {
                    cb.onError("Không tìm thấy giao dịch.");
                    return;
                }
                Deal deal = snapshot.toObject(Deal.class);
                if (deal == null) {
                    cb.onError("Dữ liệu giao dịch không hợp lệ.");
                    return;
                }
                deal.setId(dealId);

                if (!deal.isParticipant(currentUid)) {
                    cb.onError("Bạn không phải thành viên của giao dịch này.");
                    return;
                }
                if (deal.getStatus() != DealStatus.PENDING_CONFIRM) {
                    cb.onError("Giao dịch không ở trạng thái chờ xác nhận.");
                    return;
                }
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

                boolean isBuyer = currentUid.equals(deal.getBuyerId());
                boolean otherConfirmed = isBuyer ? deal.isSellerConfirmed() : deal.isBuyerConfirmed();

                WriteBatch batch = db.batch();
                Map<String, Object> updates = new HashMap<String, Object>();

                if (isBuyer) {
                    updates.put("buyerConfirmed", true);
                } else {
                    updates.put("sellerConfirmed", true);
                }

                if (otherConfirmed) {
                    if (!deal.canMoveTo(DealStatus.CONFIRMED)) {
                        cb.onError("Không thể chuyển sang trạng thái đã xác nhận.");
                        return;
                    }
                    deal.confirmBy(currentUid);
                    updates.put("status", DealStatus.CONFIRMED.name());
                    addStatusHistory(deal, DealStatus.PENDING_CONFIRM, DealStatus.CONFIRMED, currentUid, "");
                    updates.put("statusHistory", deal.getStatusHistory());
                }

                updates.put("updatedAt", FieldValue.serverTimestamp());
                batch.update(dealRef, updates);

                String otherParty = isBuyer ? deal.getSellerId() : deal.getBuyerId();
                sendNotification(otherParty, NotificationType.DEAL_UPDATE,
                        "Cập nhật xác nhận giao dịch",
                        "Đối tác đã xác nhận giao dịch.", dealId, batch);

                commitBatch(batch, cb);
            }
        });
    }

    public void pay(String dealId, PaymentMethod method, DataCallback<Void> cb) {
        String currentUid = currentUserId();
        if (currentUid.isEmpty()) {
            cb.onError("Chưa đăng nhập.");
            return;
        }

        DocumentReference dealRef = db.collection(COLLECTION_DEALS).document(dealId);
        dealRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (!task.isSuccessful()) {
                    fail(cb, task.getException());
                    return;
                }
                DocumentSnapshot snapshot = task.getResult();
                if (snapshot == null || !snapshot.exists()) {
                    cb.onError("Không tìm thấy giao dịch.");
                    return;
                }
                Deal deal = snapshot.toObject(Deal.class);
                if (deal == null || deal.getStatus() != DealStatus.CONFIRMED) {
                    cb.onError("Giao dịch chưa được xác nhận để thanh toán.");
                    return;
                }
                if (!deal.needsPayment()) {
                    cb.onError("Giao dịch này không yêu cầu thanh toán.");
                    return;
                }
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

                WriteBatch batch = db.batch();
                DocumentReference paymentRef = db.collection(COLLECTION_PAYMENTS).document(dealId);
                batch.set(paymentRef, payment);

                sendNotification(deal.getSellerId(), NotificationType.PAYMENT_UPDATE,
                        "Đã thanh toán ký quỹ",
                        "Người mua đã thanh toán tiền ký quỹ vào hệ thống.",
                        dealId, batch);

                commitBatch(batch, cb);
            }
        });
    }

    public void markShipped(String dealId, String carrier, String trackingCode,
                            String imageId, DataCallback<Void> cb) {
        String currentUid = currentUserId();
        if (currentUid.isEmpty()) {
            cb.onError("Chưa đăng nhập.");
            return;
        }

        DocumentReference dealRef = db.collection(COLLECTION_DEALS).document(dealId);
        dealRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (!task.isSuccessful()) {
                    fail(cb, task.getException());
                    return;
                }
                DocumentSnapshot snapshot = task.getResult();
                if (snapshot == null || !snapshot.exists()) {
                    cb.onError("Không tìm thấy giao dịch.");
                    return;
                }
                Deal deal = snapshot.toObject(Deal.class);
                if (deal == null) {
                    cb.onError("Dữ liệu giao dịch không hợp lệ.");
                    return;
                }
                deal.setId(dealId);

                if (!currentUid.equals(deal.getSellerId())) {
                    cb.onError("Chỉ người bán mới được xác nhận giao hàng.");
                    return;
                }
                if (!deal.canMoveTo(DealStatus.SHIPPED)) {
                    cb.onError("Giao dịch chưa ở trạng thái sẵn sàng giao.");
                    return;
                }
                if (carrier == null || carrier.trim().isEmpty()
                        || trackingCode == null || trackingCode.trim().isEmpty()
                        || imageId == null || imageId.trim().isEmpty()) {
                    cb.onError("Không đủ thông tin bằng chứng vận đơn để giao hàng.");
                    return;
                }

                if (deal.needsPayment()) {
                    checkPaymentBeforeShipping(deal, carrier, trackingCode, imageId, dealRef, currentUid, cb);
                } else {
                    executeMarkShipped(deal, carrier, trackingCode, imageId, dealRef, currentUid, cb);
                }
            }
        });
    }

    private void checkPaymentBeforeShipping(Deal deal, String carrier, String trackingCode,
                                           String imageId, DocumentReference dealRef,
                                           String currentUid, DataCallback<Void> cb) {
        db.collection(COLLECTION_PAYMENTS).document(deal.getId()).get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (!task.isSuccessful()) {
                            fail(cb, task.getException());
                            return;
                        }
                        DocumentSnapshot snapshot = task.getResult();
                        if (snapshot == null || !snapshot.exists()) {
                            cb.onError("Chưa tìm thấy khoản thanh toán ký quỹ.");
                            return;
                        }
                        Payment payment = snapshot.toObject(Payment.class);
                        if (payment == null || !payment.isHeld()) {
                            cb.onError("Khoản ký quỹ chưa được thanh toán.");
                            return;
                        }
                        executeMarkShipped(deal, carrier, trackingCode, imageId, dealRef, currentUid, cb);
                    }
                });
    }

    private void executeMarkShipped(Deal deal, String carrier, String trackingCode,
                                    String imageId, DocumentReference dealRef,
                                    String currentUid, DataCallback<Void> cb) {
        deal.markShipped(carrier, trackingCode, imageId);
        if (deal.getStatus() != DealStatus.SHIPPED) {
            cb.onError("Không đủ điều kiện để chuyển sang đã giao hàng.");
            return;
        }

        WriteBatch batch = db.batch();
        Map<String, Object> updates = new HashMap<String, Object>();
        updates.put("status", DealStatus.SHIPPED.name());
        updates.put("carrierName", carrier);
        updates.put("trackingCode", trackingCode);
        updates.put("trackingImageId", imageId);
        updates.put("shippedAt", FieldValue.serverTimestamp());
        updates.put("autoReleaseAt", deal.getAutoReleaseAt());

        addStatusHistory(deal, DealStatus.CONFIRMED, DealStatus.SHIPPED, currentUid, "");
        updates.put("statusHistory", deal.getStatusHistory());
        updates.put("updatedAt", FieldValue.serverTimestamp());
        batch.update(dealRef, updates);

        sendNotification(deal.getBuyerId(), NotificationType.DEAL_UPDATE,
                "Đơn hàng đang được giao",
                "Người bán đã gửi hàng kèm mã vận đơn " + trackingCode,
                deal.getId(), batch);

        commitBatch(batch, cb);
    }

    public void markReceived(String dealId, String imageId, DataCallback<Void> cb) {
        String currentUid = currentUserId();
        if (currentUid.isEmpty()) {
            cb.onError("Chưa đăng nhập.");
            return;
        }

        DocumentReference dealRef = db.collection(COLLECTION_DEALS).document(dealId);
        dealRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (!task.isSuccessful()) {
                    fail(cb, task.getException());
                    return;
                }
                DocumentSnapshot snapshot = task.getResult();
                if (snapshot == null || !snapshot.exists()) {
                    cb.onError("Không tìm thấy giao dịch.");
                    return;
                }
                Deal deal = snapshot.toObject(Deal.class);
                if (deal == null) {
                    cb.onError("Dữ liệu giao dịch không hợp lệ.");
                    return;
                }
                deal.setId(dealId);

                if (!currentUid.equals(deal.getBuyerId())) {
                    cb.onError("Chỉ người mua mới được xác nhận đã nhận hàng.");
                    return;
                }
                if (!deal.canMoveTo(DealStatus.RECEIVED)) {
                    cb.onError("Giao dịch không ở trạng thái có thể nhận hàng.");
                    return;
                }
                if (deal.getDeliveryMethod() == DeliveryMethod.MEET_UP && deal.getStatus() != DealStatus.CONFIRMED) {
                    cb.onError("Gặp trực tiếp chỉ nhận hàng khi đã xác nhận.");
                    return;
                }
                if (deal.getDeliveryMethod() == DeliveryMethod.SHIPPING && deal.getStatus() != DealStatus.SHIPPED) {
                    cb.onError("Giao qua bưu điện chỉ nhận hàng khi đã gửi hàng.");
                    return;
                }
                if (imageId == null || imageId.trim().isEmpty()) {
                    cb.onError("Bắt buộc kèm ảnh chụp khi nhận hàng.");
                    return;
                }

                if (deal.needsPayment()) {
                    checkAndReleasePayment(deal, imageId, dealRef, currentUid, cb);
                } else {
                    executeMarkReceived(deal, imageId, dealRef, currentUid, false, cb);
                }
            }
        });
    }

    private void checkAndReleasePayment(Deal deal, String imageId, DocumentReference dealRef,
                                        String currentUid, DataCallback<Void> cb) {
        DocumentReference payRef = db.collection(COLLECTION_PAYMENTS).document(deal.getId());
        payRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (!task.isSuccessful()) {
                    fail(cb, task.getException());
                    return;
                }
                DocumentSnapshot snapshot = task.getResult();
                boolean shouldReleasePayment = false;
                if (snapshot != null && snapshot.exists()) {
                    Payment payment = snapshot.toObject(Payment.class);
                    if (payment != null && payment.isHeld()) {
                        shouldReleasePayment = true;
                    }
                }
                executeMarkReceived(deal, imageId, dealRef, currentUid, shouldReleasePayment, cb);
            }
        });
    }

    private void executeMarkReceived(Deal deal, String imageId, DocumentReference dealRef,
                                     String currentUid, boolean releasePayment,
                                     DataCallback<Void> cb) {
        DealStatus from = deal.getStatus();
        deal.markReceived(imageId);

        WriteBatch batch = db.batch();
        Map<String, Object> updates = new HashMap<String, Object>();
        updates.put("status", DealStatus.RECEIVED.name());
        updates.put("receivedAt", FieldValue.serverTimestamp());
        updates.put("receivedImageId", imageId);

        addStatusHistory(deal, from, DealStatus.RECEIVED, currentUid, "");
        updates.put("statusHistory", deal.getStatusHistory());
        updates.put("updatedAt", FieldValue.serverTimestamp());
        batch.update(dealRef, updates);

        if (releasePayment) {
            DocumentReference payRef = db.collection(COLLECTION_PAYMENTS).document(deal.getId());
            Map<String, Object> payUpdates = new HashMap<String, Object>();
            payUpdates.put("status", PaymentStatus.RELEASED.name());
            payUpdates.put("releasedAt", FieldValue.serverTimestamp());
            payUpdates.put("updatedAt", FieldValue.serverTimestamp());
            batch.update(payRef, payUpdates);
        }

        // The listing becomes SOLD only in completeDeal; the Rules refuse RESERVED to SOLD before COMPLETED.
        sendNotification(deal.getSellerId(), NotificationType.PAYMENT_UPDATE,
                "Người mua đã nhận hàng",
                "Người mua đã xác nhận nhận hàng. Tiền ký quỹ đã được giải ngân.",
                deal.getId(), batch);

        commitBatch(batch, cb);
    }

    public void completeDeal(String dealId, DataCallback<Void> cb) {
        DocumentReference dealRef = db.collection(COLLECTION_DEALS).document(dealId);
        dealRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (!task.isSuccessful()) {
                    fail(cb, task.getException());
                    return;
                }
                DocumentSnapshot snapshot = task.getResult();
                if (snapshot == null || !snapshot.exists()) {
                    cb.onError("Không tìm thấy giao dịch.");
                    return;
                }
                Deal deal = snapshot.toObject(Deal.class);
                if (deal == null) {
                    cb.onError("Dữ liệu giao dịch không hợp lệ.");
                    return;
                }
                deal.setId(dealId);

                if (!deal.canMoveTo(DealStatus.COMPLETED)) {
                    cb.onError("Giao dịch không ở trạng thái có thể hoàn tất.");
                    return;
                }

                if (deal.getListingId() != null && !deal.getListingId().trim().isEmpty()) {
                    DocumentReference listingRef = db.collection(COLLECTION_LISTINGS)
                            .document(deal.getListingId());
                    listingRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> listingTask) {
                            if (!listingTask.isSuccessful()) {
                                fail(cb, listingTask.getException());
                                return;
                            }
                            DocumentSnapshot listingSnap = listingTask.getResult();
                            Listing listing = null;
                            if (listingSnap != null && listingSnap.exists()) {
                                listing = listingSnap.toObject(Listing.class);
                            }
                            if (!shouldUpdateListingToSold(listing, dealId)) {
                                cb.onError("Tin đăng không ở trạng thái giữ chỗ cho giao dịch này.");
                                return;
                            }
                            executeCompleteDeal(deal, dealRef, listingRef, cb);
                        }
                    });
                } else {
                    executeCompleteDeal(deal, dealRef, null, cb);
                }
            }
        });
    }

    private void executeCompleteDeal(Deal deal, DocumentReference dealRef,
                                     DocumentReference listingRef, DataCallback<Void> cb) {
        DealStatus from = deal.getStatus();
        deal.complete();

        WriteBatch batch = db.batch();
        Map<String, Object> updates = new HashMap<String, Object>();
        updates.put("status", DealStatus.COMPLETED.name());

        addStatusHistory(deal, from, DealStatus.COMPLETED, "system", "");
        updates.put("statusHistory", deal.getStatusHistory());
        updates.put("updatedAt", FieldValue.serverTimestamp());
        batch.update(dealRef, updates);

        if (listingRef != null) {
            Map<String, Object> listUpdates = new HashMap<String, Object>();
            listUpdates.put("status", ListingStatus.SOLD.name());
            listUpdates.put("updatedAt", FieldValue.serverTimestamp());
            batch.update(listingRef, listUpdates);
        }

        String currentUid = currentUserId();
        String partnerId = getOtherPartyId(deal, currentUid);
        if (!partnerId.isEmpty() && !partnerId.equals(currentUid)) {
            sendNotification(partnerId, NotificationType.DEAL_UPDATE,
                    "Giao dịch hoàn tất", "Giao dịch đã kết thúc thành công.",
                    deal.getId(), batch);
        } else {
            if (deal.getBuyerId() != null && !deal.getBuyerId().equals(currentUid)) {
                sendNotification(deal.getBuyerId(), NotificationType.DEAL_UPDATE,
                        "Giao dịch hoàn tất", "Giao dịch đã kết thúc thành công.",
                        deal.getId(), batch);
            }
            if (deal.getSellerId() != null && !deal.getSellerId().equals(currentUid)) {
                sendNotification(deal.getSellerId(), NotificationType.DEAL_UPDATE,
                        "Giao dịch hoàn tất", "Giao dịch đã kết thúc thành công.",
                        deal.getId(), batch);
            }
        }

        commitBatch(batch, cb);
    }

    static String getOtherPartyId(Deal deal, String currentUid) {
        if (deal == null || currentUid == null) {
            return "";
        }
        if (currentUid.equals(deal.getBuyerId())) {
            return deal.getSellerId() != null ? deal.getSellerId() : "";
        }
        if (currentUid.equals(deal.getSellerId())) {
            return deal.getBuyerId() != null ? deal.getBuyerId() : "";
        }
        return "";
    }

    static boolean shouldUpdateListingToSold(Listing listing, String dealId) {
        if (listing == null || dealId == null || dealId.trim().isEmpty()) {
            return false;
        }
        return listing.getStatus() == ListingStatus.RESERVED
                && dealId.equals(listing.getReservedDealId());
    }

    public void releaseDueDeals(DataCallback<Integer> cb) {
        String uid = currentUserId();
        if (uid.isEmpty()) {
            cb.onError("Chưa đăng nhập.");
            return;
        }
        Timestamp now = Timestamp.now();

        Query buyerQuery = db.collection(COLLECTION_DEALS)
                .whereEqualTo("buyerId", uid)
                .whereEqualTo("status", DealStatus.SHIPPED.name());

        buyerQuery.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> buyerTask) {
                if (!buyerTask.isSuccessful()) {
                    fail(cb, buyerTask.getException());
                    return;
                }
                Query sellerQuery = db.collection(COLLECTION_DEALS)
                        .whereEqualTo("sellerId", uid)
                        .whereEqualTo("status", DealStatus.SHIPPED.name());

                sellerQuery.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> sellerTask) {
                        if (!sellerTask.isSuccessful()) {
                            fail(cb, sellerTask.getException());
                            return;
                        }
                        Map<String, Deal> dueDeals = new HashMap<String, Deal>();
                        collectDueDeals(buyerTask.getResult(), dueDeals, now);
                        collectDueDeals(sellerTask.getResult(), dueDeals, now);

                        if (dueDeals.isEmpty()) {
                            cb.onSuccess(0);
                            return;
                        }

                        WriteBatch batch = db.batch();
                        for (Deal deal : dueDeals.values()) {
                            deal.autoReceive();
                            DocumentReference dRef = db.collection(COLLECTION_DEALS).document(deal.getId());
                            Map<String, Object> updates = new HashMap<String, Object>();
                            updates.put("status", DealStatus.RECEIVED.name());
                            updates.put("receivedAt", FieldValue.serverTimestamp());
                            addStatusHistory(deal, DealStatus.SHIPPED, DealStatus.RECEIVED, "system", "");
                            updates.put("statusHistory", deal.getStatusHistory());
                            updates.put("updatedAt", FieldValue.serverTimestamp());
                            batch.update(dRef, updates);

                            if (deal.needsPayment()) {
                                DocumentReference payRef = db.collection(COLLECTION_PAYMENTS).document(deal.getId());
                                Map<String, Object> payUpdates = new HashMap<String, Object>();
                                payUpdates.put("status", PaymentStatus.RELEASED.name());
                                payUpdates.put("releasedAt", FieldValue.serverTimestamp());
                                payUpdates.put("updatedAt", FieldValue.serverTimestamp());
                                batch.update(payRef, payUpdates);
                            }
                        }

                        final int count = dueDeals.size();
                        batch.commit().addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> commitTask) {
                                if (commitTask.isSuccessful()) {
                                    cb.onSuccess(count);
                                } else {
                                    fail(cb, commitTask.getException());
                                }
                            }
                        });
                    }
                });
            }
        });
    }

    private void collectDueDeals(QuerySnapshot snapshot, Map<String, Deal> dueDeals, Timestamp now) {
        if (snapshot == null) {
            return;
        }
        for (QueryDocumentSnapshot doc : snapshot) {
            Deal deal = doc.toObject(Deal.class);
            deal.setId(doc.getId());
            if (deal.isAutoReleaseDue(now)) {
                dueDeals.put(deal.getId(), deal);
            }
        }
    }

    public void completeDueDeals(DataCallback<Integer> cb) {
        String uid = currentUserId();
        if (uid.isEmpty()) {
            cb.onError("Chưa đăng nhập.");
            return;
        }
        Timestamp now = Timestamp.now();

        db.collection(COLLECTION_DEALS)
                .whereEqualTo("buyerId", uid)
                .whereEqualTo("status", DealStatus.RECEIVED.name())
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> buyerTask) {
                        if (!buyerTask.isSuccessful()) {
                            fail(cb, buyerTask.getException());
                            return;
                        }
                        db.collection(COLLECTION_DEALS)
                                .whereEqualTo("sellerId", uid)
                                .whereEqualTo("status", DealStatus.RECEIVED.name())
                                .get()
                                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<QuerySnapshot> sellerTask) {
                                        if (!sellerTask.isSuccessful()) {
                                            fail(cb, sellerTask.getException());
                                            return;
                                        }
                                        Map<String, Deal> receivedDeals = new HashMap<String, Deal>();
                                        collectReceivedDueDeals(buyerTask.getResult(), receivedDeals, now);
                                        collectReceivedDueDeals(sellerTask.getResult(), receivedDeals, now);

                                        int count = 0;
                                        for (Deal deal : receivedDeals.values()) {
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
                                        cb.onSuccess(count);
                                    }
                                });
                    }
                });
    }

    private void collectReceivedDueDeals(QuerySnapshot snapshot, Map<String, Deal> map, Timestamp now) {
        if (snapshot == null) {
            return;
        }
        for (QueryDocumentSnapshot doc : snapshot) {
            Deal deal = doc.toObject(Deal.class);
            deal.setId(doc.getId());
            if (deal.isAutoCompleteDue(now)) {
                map.put(deal.getId(), deal);
            }
        }
    }

    public void expirePendingDeals(DataCallback<Integer> cb) {
        String uid = currentUserId();
        if (uid.isEmpty()) {
            cb.onError("Chưa đăng nhập.");
            return;
        }
        Timestamp now = Timestamp.now();

        db.collection(COLLECTION_DEALS)
                .whereEqualTo("buyerId", uid)
                .whereEqualTo("status", DealStatus.PENDING_CONFIRM.name())
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> buyerTask) {
                        if (!buyerTask.isSuccessful()) {
                            fail(cb, buyerTask.getException());
                            return;
                        }
                        db.collection(COLLECTION_DEALS)
                                .whereEqualTo("sellerId", uid)
                                .whereEqualTo("status", DealStatus.PENDING_CONFIRM.name())
                                .get()
                                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<QuerySnapshot> sellerTask) {
                                        if (!sellerTask.isSuccessful()) {
                                            fail(cb, sellerTask.getException());
                                            return;
                                        }
                                        Map<String, Deal> expiredDeals = new HashMap<String, Deal>();
                                        collectExpiredDeals(buyerTask.getResult(), expiredDeals, now);
                                        collectExpiredDeals(sellerTask.getResult(), expiredDeals, now);

                                        int count = 0;
                                        for (Deal deal : expiredDeals.values()) {
                                            executeCancelInternal(deal.getId(), "system",
                                                    "Hết hạn chờ xác nhận",
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
                                        cb.onSuccess(count);
                                    }
                                });
                    }
                });
    }

    private void collectExpiredDeals(QuerySnapshot snapshot, Map<String, Deal> map, Timestamp now) {
        if (snapshot == null) {
            return;
        }
        for (QueryDocumentSnapshot doc : snapshot) {
            Deal deal = doc.toObject(Deal.class);
            deal.setId(doc.getId());
            if (deal.isPendingExpired(now)) {
                map.put(deal.getId(), deal);
            }
        }
    }

    public void cancel(String dealId, String reason, DataCallback<Void> cb) {
        String currentUid = currentUserId();
        if (currentUid.isEmpty()) {
            cb.onError("Chưa đăng nhập.");
            return;
        }
        if (reason == null || reason.trim().isEmpty()) {
            cb.onError("Bắt buộc phải có lý do hủy.");
            return;
        }
        executeCancelInternal(dealId, currentUid, reason.trim(), cb);
    }

    private void executeCancelInternal(String dealId, String operatorId, String reason,
                                       DataCallback<Void> cb) {
        DocumentReference dealRef = db.collection(COLLECTION_DEALS).document(dealId);
        dealRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (!task.isSuccessful()) {
                    fail(cb, task.getException());
                    return;
                }
                DocumentSnapshot snapshot = task.getResult();
                if (snapshot == null || !snapshot.exists()) {
                    cb.onError("Không tìm thấy giao dịch.");
                    return;
                }
                Deal deal = snapshot.toObject(Deal.class);
                if (deal == null) {
                    cb.onError("Dữ liệu giao dịch không hợp lệ.");
                    return;
                }
                deal.setId(dealId);

                if (!"system".equals(operatorId) && !deal.isParticipant(operatorId)) {
                    cb.onError("Bạn không phải thành viên của giao dịch này.");
                    return;
                }
                if (!deal.canMoveTo(DealStatus.CANCELLED)) {
                    cb.onError("Không thể hủy giao dịch ở trạng thái hiện tại.");
                    return;
                }

                if (deal.needsPayment()) {
                    checkAndRefundPayment(deal, dealRef, operatorId, reason, cb);
                } else {
                    executeCancelBatch(deal, dealRef, operatorId, reason, false, cb);
                }
            }
        });
    }

    private void checkAndRefundPayment(Deal deal, DocumentReference dealRef,
                                       String operatorId, String reason,
                                       DataCallback<Void> cb) {
        DocumentReference payRef = db.collection(COLLECTION_PAYMENTS).document(deal.getId());
        payRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (!task.isSuccessful()) {
                    fail(cb, task.getException());
                    return;
                }
                DocumentSnapshot snapshot = task.getResult();
                boolean shouldRefund = false;
                if (snapshot != null && snapshot.exists()) {
                    Payment payment = snapshot.toObject(Payment.class);
                    if (payment != null && payment.isHeld()) {
                        shouldRefund = true;
                    }
                }
                executeCancelBatch(deal, dealRef, operatorId, reason, shouldRefund, cb);
            }
        });
    }

    private void executeCancelBatch(Deal deal, DocumentReference dealRef,
                                    String operatorId, String reason,
                                    boolean refundPayment, DataCallback<Void> cb) {
        DealStatus from = deal.getStatus();
        deal.cancel(operatorId, reason);

        WriteBatch batch = db.batch();
        Map<String, Object> updates = new HashMap<String, Object>();
        updates.put("status", DealStatus.CANCELLED.name());
        updates.put("cancelReason", reason);

        addStatusHistory(deal, from, DealStatus.CANCELLED, operatorId, reason);
        updates.put("statusHistory", deal.getStatusHistory());
        updates.put("updatedAt", FieldValue.serverTimestamp());
        batch.update(dealRef, updates);

        if (refundPayment) {
            DocumentReference payRef = db.collection(COLLECTION_PAYMENTS).document(deal.getId());
            Map<String, Object> payUpdates = new HashMap<String, Object>();
            payUpdates.put("status", PaymentStatus.REFUNDED.name());
            payUpdates.put("releasedAt", FieldValue.serverTimestamp());
            payUpdates.put("updatedAt", FieldValue.serverTimestamp());
            batch.update(payRef, payUpdates);
        }

        if (deal.getListingId() != null) {
            DocumentReference listingRef = db.collection(COLLECTION_LISTINGS)
                    .document(deal.getListingId());
            Map<String, Object> listUpdates = new HashMap<String, Object>();
            listUpdates.put("status", ListingStatus.AVAILABLE.name());
            listUpdates.put("reservedDealId", null);
            listUpdates.put("updatedAt", FieldValue.serverTimestamp());
            batch.update(listingRef, listUpdates);
        }

        String otherParty = operatorId.equals(deal.getBuyerId()) ? deal.getSellerId() : deal.getBuyerId();
        sendNotification(otherParty, NotificationType.DEAL_UPDATE,
                "Giao dịch đã bị hủy", "Lý do: " + reason, deal.getId(), batch);

        commitBatch(batch, cb);
    }

    public void getMyDeals(String uid, DataCallback<List<Deal>> cb) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                DealDao dao = getDealDao();
                if (dao != null) {
                    List<DealEntity> cached = dao.getMyDeals(uid);
                    if (cached != null && !cached.isEmpty()) {
                        List<Deal> cachedList = new ArrayList<Deal>();
                        for (int i = 0; i < cached.size(); i++) {
                            cachedList.add(cached.get(i).toModel());
                        }
                        cb.onSuccess(cachedList);
                    }
                }
            }
        });

        db.collection(COLLECTION_DEALS).whereEqualTo("buyerId", uid).get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> buyerTask) {
                        if (!buyerTask.isSuccessful()) {
                            fail(cb, buyerTask.getException());
                            return;
                        }
                        db.collection(COLLECTION_DEALS).whereEqualTo("sellerId", uid).get()
                                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<QuerySnapshot> sellerTask) {
                                        if (!sellerTask.isSuccessful()) {
                                            fail(cb, sellerTask.getException());
                                            return;
                                        }
                                        Map<String, Deal> map = new HashMap<String, Deal>();
                                        addDealsToMap(buyerTask.getResult(), map);
                                        addDealsToMap(sellerTask.getResult(), map);

                                        List<Deal> list = new ArrayList<Deal>(map.values());
                                        sortDealsByDate(list);
                                        cb.onSuccess(list);

                                        executor.execute(new Runnable() {
                                            @Override
                                            public void run() {
                                                DealDao dao = getDealDao();
                                                if (dao != null && !list.isEmpty()) {
                                                    List<DealEntity> entities = new ArrayList<DealEntity>();
                                                    for (int i = 0; i < list.size(); i++) {
                                                        entities.add(new DealEntity().fromModel(list.get(i)));
                                                    }
                                                    dao.upsertAll(entities);
                                                }
                                            }
                                        });
                                    }
                                });
                    }
                });
    }

    // Live version of getMyDeals: shows the Room copy first, then every change of a deal I buy or sell.
    public ListenerRegistration observeMyDeals(final String uid, final DataCallback<List<Deal>> cb) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                DealDao dao = getDealDao();
                if (dao == null) {
                    return;
                }
                List<DealEntity> cached = dao.getMyDeals(uid);
                if (cached == null || cached.isEmpty()) {
                    return;
                }
                List<Deal> cachedList = new ArrayList<Deal>();
                for (int i = 0; i < cached.size(); i++) {
                    cachedList.add(cached.get(i).toModel());
                }
                cb.onSuccess(cachedList);
            }
        });
        Query asBuyer = db.collection(COLLECTION_DEALS).whereEqualTo("buyerId", uid);
        Query asSeller = db.collection(COLLECTION_DEALS).whereEqualTo("sellerId", uid);
        return listenEitherQuery(asBuyer, asSeller, Deal.class, new DataCallback<List<Deal>>() {
            @Override
            public void onSuccess(List<Deal> result) {
                sortDealsByDate(result);
                cb.onSuccess(result);
                cacheDeals(result);
            }

            @Override
            public void onError(String message) {
                cb.onError(message);
            }
        });
    }

    // Live version of getPayments for the payments tab of C04.
    public ListenerRegistration observePayments(String uid, final DataCallback<List<Payment>> cb) {
        Query asPayer = db.collection(COLLECTION_PAYMENTS).whereEqualTo("payerId", uid);
        Query asPayee = db.collection(COLLECTION_PAYMENTS).whereEqualTo("payeeId", uid);
        return listenEitherQuery(asPayer, asPayee, Payment.class, new DataCallback<List<Payment>>() {
            @Override
            public void onSuccess(List<Payment> result) {
                sortPaymentsByDate(result);
                cb.onSuccess(result);
            }

            @Override
            public void onError(String message) {
                cb.onError(message);
            }
        });
    }

    private void cacheDeals(final List<Deal> list) {
        if (list.isEmpty()) {
            return;
        }
        executor.execute(new Runnable() {
            @Override
            public void run() {
                DealDao dao = getDealDao();
                if (dao == null) {
                    return;
                }
                List<DealEntity> entities = new ArrayList<DealEntity>();
                for (int i = 0; i < list.size(); i++) {
                    entities.add(new DealEntity().fromModel(list.get(i)));
                }
                dao.upsertAll(entities);
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
                            fail(cb, payerTask.getException());
                            return;
                        }
                        db.collection(COLLECTION_PAYMENTS).whereEqualTo("payeeId", uid).get()
                                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<QuerySnapshot> payeeTask) {
                                        if (!payeeTask.isSuccessful()) {
                                            fail(cb, payeeTask.getException());
                                            return;
                                        }
                                        Map<String, Payment> map = new HashMap<String, Payment>();
                                        addPaymentsToMap(payerTask.getResult(), map);
                                        addPaymentsToMap(payeeTask.getResult(), map);

                                        List<Payment> list = new ArrayList<Payment>(map.values());
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
                    fail(cb, task.getException());
                    return;
                }
                List<Deal> list = new ArrayList<Deal>();
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

    // Live version of listAll for E06; a null status means every deal.
    public ListenerRegistration observeAll(DealStatus status, DataCallback<List<Deal>> cb) {
        Query query = db.collection(COLLECTION_DEALS);
        if (status != null) {
            query = query.whereEqualTo("status", status.name());
        }
        query = query.orderBy("updatedAt", Query.Direction.DESCENDING);
        return listenQuery(query, Deal.class, cb);
    }

    private void addStatusHistory(Deal deal, DealStatus from, DealStatus to,
                                  String changedBy, String reason) {
        List<StatusChange> history = deal.getStatusHistory();
        if (history == null) {
            history = new ArrayList<StatusChange>();
        }
        StatusChange sc = new StatusChange();
        sc.setFromStatus(from);
        sc.setToStatus(to);
        sc.setChangedBy(changedBy);
        sc.setReason(reason != null ? reason : "");
        sc.setChangedAt(Timestamp.now());
        history.add(sc);
        deal.setStatusHistory(history);
    }

    private void sendNotification(String recipientId, NotificationType type,
                                  String title, String body, String targetId,
                                  WriteBatch batch) {
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
        notificationRepository.create(notif, batch, new DataCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
            }

            @Override
            public void onError(String message) {
            }
        });
    }

    private void commitBatch(WriteBatch batch, DataCallback<Void> cb) {
        batch.commit().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    cb.onSuccess(null);
                } else {
                    fail(cb, task.getException());
                }
            }
        });
    }
}
