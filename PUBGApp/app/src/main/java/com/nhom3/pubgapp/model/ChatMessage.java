package com.nhom3.pubgapp.model;

import com.google.firebase.firestore.Exclude;
import com.nhom3.pubgapp.model.enums.MessageType;

public class ChatMessage extends BaseDocument {
    private String conversationId = "";
    private String senderId = "";
    private MessageType type;
    private String text = "";
    private String imageId = "";
    private String offerId = "";

    public ChatMessage() {
    }

    public String getConversationId() {
        return conversationId;
    }

    public void setConversationId(String conversationId) {
        this.conversationId = conversationId != null ? conversationId : "";
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId != null ? senderId : "";
    }

    public MessageType getType() {
        return type;
    }

    public void setType(MessageType type) {
        this.type = type;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text != null ? text : "";
    }

    public String getImageId() {
        return imageId;
    }

    public void setImageId(String imageId) {
        this.imageId = imageId != null ? imageId : "";
    }

    public String getOfferId() {
        return offerId;
    }

    public void setOfferId(String offerId) {
        this.offerId = offerId != null ? offerId : "";
    }

    @Exclude
    public boolean isMedia() {
        return type == MessageType.IMAGE;
    }
}
