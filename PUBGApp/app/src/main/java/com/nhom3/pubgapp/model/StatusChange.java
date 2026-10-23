package com.nhom3.pubgapp.model;

import com.google.firebase.Timestamp;
import com.nhom3.pubgapp.model.enums.DealStatus;

public class StatusChange {
    private DealStatus fromStatus;
    private DealStatus toStatus;
    private String changedBy;
    private String reason;
    private Timestamp changedAt;

    public StatusChange() {
    }

    public DealStatus getFromStatus() {
        return fromStatus;
    }

    public void setFromStatus(DealStatus fromStatus) {
        this.fromStatus = fromStatus;
    }

    public DealStatus getToStatus() {
        return toStatus;
    }

    public void setToStatus(DealStatus toStatus) {
        this.toStatus = toStatus;
    }

    public String getChangedBy() {
        return changedBy;
    }

    public void setChangedBy(String changedBy) {
        this.changedBy = changedBy;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public Timestamp getChangedAt() {
        return changedAt;
    }

    public void setChangedAt(Timestamp changedAt) {
        this.changedAt = changedAt;
    }

    public String describe() {
        throw new UnsupportedOperationException("NOT_READY");
    }
}
