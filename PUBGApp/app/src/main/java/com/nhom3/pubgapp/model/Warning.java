package com.nhom3.pubgapp.model;

import com.nhom3.pubgapp.model.enums.UserStatus;

public class Warning extends BaseDocument {
    private String userId;
    private int level;
    private String reason;
    private String issuedBy;
    private String targetId;

    public Warning() {
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getIssuedBy() {
        return issuedBy;
    }

    public void setIssuedBy(String issuedBy) {
        this.issuedBy = issuedBy;
    }

    public String getTargetId() {
        return targetId;
    }

    public void setTargetId(String targetId) {
        this.targetId = targetId;
    }

    public UserStatus resultingStatus() {
        throw new UnsupportedOperationException("NOT_READY");
    }
}
