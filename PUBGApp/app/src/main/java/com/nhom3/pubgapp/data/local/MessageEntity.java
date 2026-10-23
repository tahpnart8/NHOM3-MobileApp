package com.nhom3.pubgapp.data.local;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import com.nhom3.pubgapp.model.ChatMessage;

// Copy of chat messages already seen.
// conversationId is indexed but not a ForeignKey, so messages can be cached before their conversation.
@Entity(tableName = "message_cache", indices = {@Index("conversationId")})
public class MessageEntity {

    @PrimaryKey
    @NonNull
    private String id = "";

    private String conversationId;

    private String senderId;

    private String type;

    private String text;

    private String imageId;

    private String offerId;

    private long sentAtMillis;

    // Room needs a public constructor it can call, then fills the fields through the setters.
    public MessageEntity() {
    }

    public MessageEntity fromModel(ChatMessage model) {
        throw new UnsupportedOperationException("NOT_READY");
    }

    public ChatMessage toModel() {
        throw new UnsupportedOperationException("NOT_READY");
    }

    @NonNull
    public String getId() {
        return id;
    }

    public void setId(@NonNull String id) {
        this.id = id;
    }

    public String getConversationId() {
        return conversationId;
    }

    public void setConversationId(String conversationId) {
        this.conversationId = conversationId;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getImageId() {
        return imageId;
    }

    public void setImageId(String imageId) {
        this.imageId = imageId;
    }

    public String getOfferId() {
        return offerId;
    }

    public void setOfferId(String offerId) {
        this.offerId = offerId;
    }

    public long getSentAtMillis() {
        return sentAtMillis;
    }

    public void setSentAtMillis(long sentAtMillis) {
        this.sentAtMillis = sentAtMillis;
    }
}
