package com.nhom3.pubgapp.model;

public class ProfileStats {
    private int soldCount;
    private int followerCount;
    private int dealCount;
    private int completedDealCount;

    public ProfileStats() {
    }

    public int getSoldCount() {
        return soldCount;
    }

    public void setSoldCount(int soldCount) {
        this.soldCount = soldCount;
    }

    public int getFollowerCount() {
        return followerCount;
    }

    public void setFollowerCount(int followerCount) {
        this.followerCount = followerCount;
    }

    public int getDealCount() {
        return dealCount;
    }

    public void setDealCount(int dealCount) {
        this.dealCount = dealCount;
    }

    public int getCompletedDealCount() {
        return completedDealCount;
    }

    public void setCompletedDealCount(int completedDealCount) {
        this.completedDealCount = completedDealCount;
    }

    public double completionRate() {
        throw new UnsupportedOperationException("NOT_READY");
    }
}
