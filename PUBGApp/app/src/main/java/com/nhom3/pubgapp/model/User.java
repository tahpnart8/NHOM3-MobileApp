package com.nhom3.pubgapp.model;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.Exclude;
import com.nhom3.pubgapp.model.enums.UserStatus;
import com.nhom3.pubgapp.model.enums.VerificationStatus;

import java.util.List;
import java.util.Map;

public class User extends BaseDocument {
    private String email;
    private String displayName;
    private String phone;
    private String avatarBase64;
    private String bio;
    private UserStatus status;
    private Timestamp lockedUntil;
    private VerificationStatus verificationStatus;
    private int ratingSum;
    private int ratingCount;
    private int warningCount;
    private List<String> interestedCategoryIds;
    private String province;
    private String district;
    private Timestamp termsAcceptedAt;
    private Map<String, Boolean> notificationPrefs;
    private Timestamp lastActiveAt;
    private String lockReason;
    private List<String> searchKeywords;
    // Empty like the value createProfile writes; only the review batch sets it (security-rules.md, users).
    private String lastReviewId = "";

    public User() {
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAvatarBase64() {
        return avatarBase64;
    }

    public void setAvatarBase64(String avatarBase64) {
        this.avatarBase64 = avatarBase64;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public UserStatus getStatus() {
        return status;
    }

    public void setStatus(UserStatus status) {
        this.status = status;
    }

    public Timestamp getLockedUntil() {
        return lockedUntil;
    }

    public void setLockedUntil(Timestamp lockedUntil) {
        this.lockedUntil = lockedUntil;
    }

    public VerificationStatus getVerificationStatus() {
        return verificationStatus;
    }

    public void setVerificationStatus(VerificationStatus verificationStatus) {
        this.verificationStatus = verificationStatus;
    }

    public int getRatingSum() {
        return ratingSum;
    }

    public void setRatingSum(int ratingSum) {
        this.ratingSum = ratingSum;
    }

    public int getRatingCount() {
        return ratingCount;
    }

    public void setRatingCount(int ratingCount) {
        this.ratingCount = ratingCount;
    }

    public int getWarningCount() {
        return warningCount;
    }

    public void setWarningCount(int warningCount) {
        this.warningCount = warningCount;
    }

    public List<String> getInterestedCategoryIds() {
        return interestedCategoryIds;
    }

    public void setInterestedCategoryIds(List<String> interestedCategoryIds) {
        this.interestedCategoryIds = interestedCategoryIds;
    }

    public String getProvince() {
        return province;
    }

    public void setProvince(String province) {
        this.province = province;
    }

    public String getDistrict() {
        return district;
    }

    public void setDistrict(String district) {
        this.district = district;
    }

    public Timestamp getTermsAcceptedAt() {
        return termsAcceptedAt;
    }

    public void setTermsAcceptedAt(Timestamp termsAcceptedAt) {
        this.termsAcceptedAt = termsAcceptedAt;
    }

    public Map<String, Boolean> getNotificationPrefs() {
        return notificationPrefs;
    }

    public void setNotificationPrefs(Map<String, Boolean> notificationPrefs) {
        this.notificationPrefs = notificationPrefs;
    }

    public Timestamp getLastActiveAt() {
        return lastActiveAt;
    }

    public void setLastActiveAt(Timestamp lastActiveAt) {
        this.lastActiveAt = lastActiveAt;
    }

    public String getLockReason() {
        return lockReason;
    }

    public void setLockReason(String lockReason) {
        this.lockReason = lockReason;
    }

    public List<String> getSearchKeywords() {
        return searchKeywords;
    }

    public void setSearchKeywords(List<String> searchKeywords) {
        this.searchKeywords = searchKeywords;
    }

    public String getLastReviewId() {
        return lastReviewId;
    }

    public void setLastReviewId(String lastReviewId) {
        this.lastReviewId = lastReviewId;
    }

    @Exclude
    public boolean isLocked() {
        if (status == UserStatus.PERM_LOCKED) {
            return true;
        }
        if (status == UserStatus.TEMP_LOCKED) {
            if (lockedUntil == null) {
                return true;
            }
            Timestamp now = Timestamp.now();
            return lockedUntil.compareTo(now) > 0;
        }
        return false;
    }

    @Exclude
    public boolean isDeleted() {
        return status == UserStatus.DELETED;
    }

    public boolean canSell(boolean emailVerified) {
        if (status != UserStatus.ACTIVE) {
            return false;
        }
        if (isLocked()) {
            return false;
        }
        if (!emailVerified) {
            return false;
        }
        return verificationStatus == VerificationStatus.APPROVED;
    }

    public boolean canBuy(boolean emailVerified) {
        return canSell(emailVerified);
    }

    public void applyRating(int stars) {
        this.ratingSum = this.ratingSum + stars;
        this.ratingCount = this.ratingCount + 1;
    }

    @Exclude
    public double ratingAverage() {
        if (ratingCount <= 0) {
            return 0.0;
        }
        return (double) ratingSum / (double) ratingCount;
    }

    public boolean isActiveSince(Timestamp since) {
        if (lastActiveAt == null || since == null) {
            return false;
        }
        return lastActiveAt.compareTo(since) > 0;
    }

    public void buildSearchKeywords() {
        com.nhom3.pubgapp.util.FormatUtils formatUtils = new com.nhom3.pubgapp.util.FormatUtils();
        List<String> keywords = formatUtils.toKeywords(displayName);
        if (keywords.size() > 20) {
            this.searchKeywords = new java.util.ArrayList<String>(keywords.subList(0, 20));
        } else {
            this.searchKeywords = keywords;
        }
    }
}

