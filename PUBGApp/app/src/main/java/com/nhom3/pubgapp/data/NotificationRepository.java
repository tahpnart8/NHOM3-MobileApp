package com.nhom3.pubgapp.data;

import com.google.firebase.firestore.WriteBatch;
import com.nhom3.pubgapp.model.AppNotification;
import java.util.List;

// Skeleton: the module owner fills each body as the issue assigns it.
public class NotificationRepository extends BaseRepository {

    public void getForUser(String uid, DataCallback<List<AppNotification>> cb) {
        cb.onError(DataCallback.NOT_READY);
    }

    public void markRead(String id, DataCallback<Void> cb) {
        cb.onError(DataCallback.NOT_READY);
    }

    public void create(AppNotification notification, WriteBatch batch, DataCallback<Void> cb) {
        cb.onError(DataCallback.NOT_READY);
    }
}
