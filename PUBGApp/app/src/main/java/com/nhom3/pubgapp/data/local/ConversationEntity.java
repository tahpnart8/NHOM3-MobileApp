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
        throw new UnsupportedOperationException("NOT_READY");
    }

    public Conversation toModel() {
        throw new UnsupportedOperationException("NOT_READY");
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
