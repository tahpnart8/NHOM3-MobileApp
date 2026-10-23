package com.nhom3.pubgapp.model;

import com.google.firebase.firestore.Exclude;

public class Review extends BaseDocument {
    private String dealId;
    private String reviewerId;
    private String revieweeId;
    private int rating;
    private int accuracyRating;
    private int attitudeRating;
    private int commitmentRating;
    private String comment;

    public Review() {
    }

    public String getDealId() {
        return dealId;
    }

    public void setDealId(String dealId) {
        this.dealId = dealId;
    }

    public String getReviewerId() {
        return reviewerId;
    }

    public void setReviewerId(String reviewerId) {
        this.reviewerId = reviewerId;
    }

    public String getRevieweeId() {
        return revieweeId;
    }

    public void setRevieweeId(String revieweeId) {
        this.revieweeId = revieweeId;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public int getAccuracyRating() {
        return accuracyRating;
    }

    public void setAccuracyRating(int accuracyRating) {
        this.accuracyRating = accuracyRating;
    }

    public int getAttitudeRating() {
        return attitudeRating;
    }

    public void setAttitudeRating(int attitudeRating) {
        this.attitudeRating = attitudeRating;
    }

    public int getCommitmentRating() {
        return commitmentRating;
    }

    public void setCommitmentRating(int commitmentRating) {
        this.commitmentRating = commitmentRating;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    @Exclude
    public boolean isValidRating() {
        throw new UnsupportedOperationException("NOT_READY");
    }
}
