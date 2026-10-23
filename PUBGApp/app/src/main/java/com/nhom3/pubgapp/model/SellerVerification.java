package com.nhom3.pubgapp.model;

import com.google.firebase.Timestamp;
import com.nhom3.pubgapp.model.enums.VerificationStatus;

import java.util.List;

public class SellerVerification extends BaseDocument {
    private static final int MAX_RESUBMIT = 3;

    private String userId;
    private String nationalId;
    private List<String> imageIds;
    private VerificationStatus status;
    private String note;
    private int resubmitCount;
    private String reviewedBy;
    private Timestamp reviewedAt;

    public SellerVerification() {
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getNationalId() {
        return nationalId;
    }

    public void setNationalId(String nationalId) {
        this.nationalId = nationalId;
    }

    public List<String> getImageIds() {
        return imageIds;
    }

    public void setImageIds(List<String> imageIds) {
        this.imageIds = imageIds;
    }

    public VerificationStatus getStatus() {
        return status;
    }

    public void setStatus(VerificationStatus status) {
        this.status = status;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public int getResubmitCount() {
        return resubmitCount;
    }

    public void setResubmitCount(int resubmitCount) {
        this.resubmitCount = resubmitCount;
    }

    public String getReviewedBy() {
        return reviewedBy;
    }

    public void setReviewedBy(String reviewedBy) {
        this.reviewedBy = reviewedBy;
    }

    public Timestamp getReviewedAt() {
        return reviewedAt;
    }

    public void setReviewedAt(Timestamp reviewedAt) {
        this.reviewedAt = reviewedAt;
    }

    public void approve(String adminId) {
        throw new UnsupportedOperationException("NOT_READY");
    }

    public void reject(String adminId, String note) {
        throw new UnsupportedOperationException("NOT_READY");
    }

    public boolean canResubmit() {
        throw new UnsupportedOperationException("NOT_READY");
    }

    public int remainingResubmits() {
        throw new UnsupportedOperationException("NOT_READY");
    }
}
