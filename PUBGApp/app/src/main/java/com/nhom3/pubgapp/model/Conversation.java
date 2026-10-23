package com.nhom3.pubgapp.model;

import com.google.firebase.Timestamp;

public class Conversation extends BaseDocument {
    private String buyerId;
    private String sellerId;
    private String listingId;
    private String lastMessage;
    private Timestamp lastMessageAt;

    public Conversation() {
    }

    public String getBuyerId() {
        return buyerId;
    }

    public void setBuyerId(String buyerId) {
        this.buyerId = buyerId;
    }

    public String getSellerId() {
        return sellerId;
    }

    public void setSellerId(String sellerId) {
        this.sellerId = sellerId;
    }

    public String getListingId() {
        return listingId;
    }

    public void setListingId(String listingId) {
        this.listingId = listingId;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }

    public Timestamp getLastMessageAt() {
        return lastMessageAt;
    }

    public void setLastMessageAt(Timestamp lastMessageAt) {
        this.lastMessageAt = lastMessageAt;
    }

    public boolean isParticipant(String userId) {
        if (userId == null) {
            return false;
        }
        return userId.equals(buyerId) || userId.equals(sellerId);
    }

    public String otherParty(String userId) {
        if (userId == null) {
            return null;
        }
        if (userId.equals(buyerId)) {
            return sellerId;
        }
        if (userId.equals(sellerId)) {
            return buyerId;
        }
        return null;
    }
}
