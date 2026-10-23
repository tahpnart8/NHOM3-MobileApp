package com.nhom3.pubgapp.model;

import com.nhom3.pubgapp.model.enums.UserStatus;

public class Warning extends BaseDocument {
    private static final int TEMPORARY_LOCK_LEVEL = 2;
    private static final int PERMANENT_LOCK_LEVEL = 3;

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

    // Level 1 only warns; the lock ladder is fixed by trang-thai.md section 7.
    public UserStatus resultingStatus() {
        if (level >= PERMANENT_LOCK_LEVEL) {
            return UserStatus.PERM_LOCKED;
        }
        if (level == TEMPORARY_LOCK_LEVEL) {
            return UserStatus.TEMP_LOCKED;
        }
        return UserStatus.ACTIVE;
    }
}
