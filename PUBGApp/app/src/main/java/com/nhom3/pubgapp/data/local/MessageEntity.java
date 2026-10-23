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
        if (model == null) {
            return this;
        }
        this.id = model.getId() != null ? model.getId() : "";
        this.conversationId = model.getConversationId();
        this.senderId = model.getSenderId();
        if (model.getType() != null) {
            this.type = model.getType().name();
        }
        this.text = model.getText();
        this.imageId = model.getImageId();
        this.offerId = model.getOfferId();
        if (model.getCreatedAt() != null) {
            this.sentAtMillis = model.getCreatedAt().toDate().getTime();
        }
        return this;
    }

    public ChatMessage toModel() {
        ChatMessage message = new ChatMessage();
        message.setId(this.id);
        message.setConversationId(this.conversationId);
        message.setSenderId(this.senderId);
        if (this.type != null) {
            try {
                message.setType(com.nhom3.pubgapp.model.enums.MessageType.valueOf(this.type));
            } catch (IllegalArgumentException e) {
                // Ignore invalid type
            }
        }
        message.setText(this.text);
        message.setImageId(this.imageId);
        message.setOfferId(this.offerId);
        if (this.sentAtMillis > 0) {
            java.util.Date date = new java.util.Date(this.sentAtMillis);
            message.setCreatedAt(new com.google.firebase.Timestamp(date));
        }
        return message;
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
