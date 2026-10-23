package com.nhom3.pubgapp.model;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.Exclude;
import com.nhom3.pubgapp.model.enums.PaymentMethod;
import com.nhom3.pubgapp.model.enums.PaymentStatus;

public class Payment extends BaseDocument {
    private String dealId;
    private String payerId;
    private String payeeId;
    private long amount;
    private PaymentStatus status;
    private PaymentMethod method;
    private Timestamp heldAt;
    private Timestamp releasedAt;

    public Payment() {
    }

    public String getDealId() {
        return dealId;
    }

    public void setDealId(String dealId) {
        this.dealId = dealId;
    }

    public String getPayerId() {
        return payerId;
    }

    public void setPayerId(String payerId) {
        this.payerId = payerId;
    }

    public String getPayeeId() {
        return payeeId;
    }

    public void setPayeeId(String payeeId) {
        this.payeeId = payeeId;
    }

    public long getAmount() {
        return amount;
    }

    public void setAmount(long amount) {
        this.amount = amount;
    }

    public PaymentStatus getStatus() {
        return status;
    }

    public void setStatus(PaymentStatus status) {
        this.status = status;
    }

    public PaymentMethod getMethod() {
        return method;
    }

    public void setMethod(PaymentMethod method) {
        this.method = method;
    }

    public Timestamp getHeldAt() {
        return heldAt;
    }

    public void setHeldAt(Timestamp heldAt) {
        this.heldAt = heldAt;
    }

    public Timestamp getReleasedAt() {
        return releasedAt;
    }

    public void setReleasedAt(Timestamp releasedAt) {
        this.releasedAt = releasedAt;
    }

    public void release() {
        throw new UnsupportedOperationException("NOT_READY");
    }

    public void refund() {
        throw new UnsupportedOperationException("NOT_READY");
    }

    @Exclude
    public boolean isHeld() {
        throw new UnsupportedOperationException("NOT_READY");
    }
}
