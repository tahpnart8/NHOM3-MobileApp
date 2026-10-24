package com.nhom3.pubgapp.data.local;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.nhom3.pubgapp.model.Conversation;

// Copy of the conversation list.
@Entity(tableName = "conversation_cache")
public class ConversationEntity {

    @PrimaryKey
    @NonNull
    private String id = "";

    private String listingId;

    private String buyerId;

    private String sellerId;

    private String lastMessage;

    private long lastMessageAt;

    private long cachedAtMillis;

    // Room needs a public constructor it can call, then fills the fields through the setters.
    public ConversationEntity() {
    }

    public ConversationEntity fromModel(Conversation model) {
        if (model == null) {
            return this;
        }
        this.id = model.getId() != null ? model.getId() : "";
        this.listingId = model.getListingId();
        this.buyerId = model.getBuyerId();
        this.sellerId = model.getSellerId();
        this.lastMessage = model.getLastMessage();
        if (model.getLastMessageAt() != null) {
            this.lastMessageAt = model.getLastMessageAt().toDate().getTime();
        }
        this.cachedAtMillis = System.currentTimeMillis();
        return this;
    }

    public Conversation toModel() {
        Conversation conversation = new Conversation();
        conversation.setId(this.id);
        conversation.setListingId(this.listingId);
        conversation.setBuyerId(this.buyerId);
        conversation.setSellerId(this.sellerId);
        conversation.setLastMessage(this.lastMessage);
        if (this.lastMessageAt > 0) {
            java.util.Date date = new java.util.Date(this.lastMessageAt);
            conversation.setLastMessageAt(new com.google.firebase.Timestamp(date));
        }
        return conversation;
    }

    @NonNull
    public String getId() {
        return id;
    }

    public void setId(@NonNull String id) {
        this.id = id;
    }

    public String getListingId() {
        return listingId;
    }

    public void setListingId(String listingId) {
        this.listingId = listingId;
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

    public String getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }

    public long getLastMessageAt() {
        return lastMessageAt;
    }

    public void setLastMessageAt(long lastMessageAt) {
        this.lastMessageAt = lastMessageAt;
    }

    public long getCachedAtMillis() {
        return cachedAtMillis;
    }

    public void setCachedAtMillis(long cachedAtMillis) {
        this.cachedAtMillis = cachedAtMillis;
    }
}
