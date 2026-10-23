package com.nhom3.pubgapp.data;

import com.nhom3.pubgapp.model.Deal;
import com.nhom3.pubgapp.model.Listing;
import com.nhom3.pubgapp.model.Offer;
import com.nhom3.pubgapp.model.Payment;
import com.nhom3.pubgapp.model.enums.DealStatus;
import com.nhom3.pubgapp.model.enums.PaymentMethod;
import java.util.List;

// Skeleton: the module owner fills each body as the issue assigns it.
public class DealRepository extends BaseRepository {

    public Deal buildFromOffer(Offer offer, Listing listing) {
        throw new UnsupportedOperationException("NOT_READY");
    }

    public void getById(String dealId, DataCallback<Deal> cb) {
        cb.onError(DataCallback.NOT_READY);
    }

    public void getPayment(String dealId, DataCallback<Payment> cb) {
        cb.onError(DataCallback.NOT_READY);
    }

    public void updateTerms(Deal deal, DataCallback<Void> cb) {
        cb.onError(DataCallback.NOT_READY);
    }

    public void confirm(String dealId, DataCallback<Void> cb) {
        cb.onError(DataCallback.NOT_READY);
    }

    public void pay(String dealId, PaymentMethod method, DataCallback<Void> cb) {
        cb.onError(DataCallback.NOT_READY);
    }

    public void markShipped(String dealId, String carrier, String trackingCode, String imageId, DataCallback<Void> cb) {
        cb.onError(DataCallback.NOT_READY);
    }

    public void markReceived(String dealId, String imageId, DataCallback<Void> cb) {
        cb.onError(DataCallback.NOT_READY);
    }

    public void completeDeal(String dealId, DataCallback<Void> cb) {
        cb.onError(DataCallback.NOT_READY);
    }

    public void releaseDueDeals(DataCallback<Integer> cb) {
        cb.onError(DataCallback.NOT_READY);
    }

    public void completeDueDeals(DataCallback<Integer> cb) {
        cb.onError(DataCallback.NOT_READY);
    }

    public void expirePendingDeals(DataCallback<Integer> cb) {
        cb.onError(DataCallback.NOT_READY);
    }

    public void cancel(String dealId, String reason, DataCallback<Void> cb) {
        cb.onError(DataCallback.NOT_READY);
    }

    public void getMyDeals(String uid, DataCallback<List<Deal>> cb) {
        cb.onError(DataCallback.NOT_READY);
    }

    public void getPayments(String uid, DataCallback<List<Payment>> cb) {
        cb.onError(DataCallback.NOT_READY);
    }

    public void listAll(DealStatus status, DataCallback<List<Deal>> cb) {
        cb.onError(DataCallback.NOT_READY);
    }
}
