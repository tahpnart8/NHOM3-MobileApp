package com.nhom3.pubgapp.model;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.ServerTimestamp;

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

    // A null value is replaced by the server clock when the object is written, as the Rules demand.
    @ServerTimestamp
    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    @ServerTimestamp
    public Timestamp getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Timestamp updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getId() {
        return id;
    }

    // Clears updatedAt so the next write stamps it with server time; a device clock would fail the Rules.
    public void touch() {
        updatedAt = null;
    }
}
