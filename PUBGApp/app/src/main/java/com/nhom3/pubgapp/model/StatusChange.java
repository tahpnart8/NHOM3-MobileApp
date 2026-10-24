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
        if (toStatus == null) {
            return "";
        }
        switch (toStatus) {
            case PENDING_CONFIRM:
                return "Tạo giao dịch, chờ hai bên xác nhận";
            case CONFIRMED:
                return "Hai bên đã xác nhận điều khoản giao dịch";
            case SHIPPED:
                return "Người bán đã giao hàng";
            case RECEIVED:
                return "Đã nhận hàng";
            case COMPLETED:
                return "Giao dịch đã hoàn tất";
            case CANCELLED:
                if (reason != null && !reason.trim().isEmpty()) {
                    return "Giao dịch đã hủy: " + reason;
                }
                return "Giao dịch đã hủy";
            case DISPUTED:
                if (reason != null && !reason.trim().isEmpty()) {
                    return "Đang khiếu nại: " + reason;
                }
                return "Đang khiếu nại";
            default:
                return toStatus.name();
        }
    }
}
