package com.nhom3.pubgapp.model;

import com.google.firebase.firestore.PropertyName;
import com.nhom3.pubgapp.model.enums.NotificationType;

public class AppNotification extends BaseDocument {
    private String recipientId;
    private NotificationType type;
    private String title;
    private String body;
    private String targetId;
    private boolean isRead;

    public AppNotification() {
    }

    public String getRecipientId() {
        return recipientId;
    }

    public void setRecipientId(String recipientId) {
        this.recipientId = recipientId;
    }

    public NotificationType getType() {
        return type;
    }

    public void setType(NotificationType type) {
        this.type = type;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getTargetId() {
        return targetId;
    }

    public void setTargetId(String targetId) {
        this.targetId = targetId;
    }

    @PropertyName("isRead")
    public boolean isRead() {
        return isRead;
    }

    @PropertyName("isRead")
    public void setRead(boolean isRead) {
        this.isRead = isRead;
    }

    public void markRead() {
        this.isRead = true;
    }
}

