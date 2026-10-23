package com.nhom3.pubgapp.data;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public abstract class BaseRepository {

    protected FirebaseFirestore db = FirebaseFirestore.getInstance();
    protected FirebaseAuth auth = FirebaseAuth.getInstance();

    protected String currentUserId() {
        throw new UnsupportedOperationException("NOT_READY");
    }

    protected void logActivity(String action, String targetType, String targetId) {
        throw new UnsupportedOperationException("NOT_READY");
    }

    protected void fail(DataCallback<?> cb, Exception error) {
        throw new UnsupportedOperationException("NOT_READY");
    }
}
