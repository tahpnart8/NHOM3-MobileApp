package com.nhom3.pubgapp.model;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentId;

// Setters are public because Firestore cannot fill protected fields without them.
public abstract class BaseDocument {
    @DocumentId
    protected String id;
    protected Timestamp createdAt;
    protected Timestamp updatedAt;

    public BaseDocument() {
    }

    public void setId(String id) {
        this.id = id;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public Timestamp getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Timestamp updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getId() {
        return id;
    }

    public void touch() {
        throw new UnsupportedOperationException("NOT_READY");
    }
}
