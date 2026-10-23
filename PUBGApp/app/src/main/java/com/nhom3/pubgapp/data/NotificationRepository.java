package com.nhom3.pubgapp.data;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.WriteBatch;
import com.nhom3.pubgapp.model.AppNotification;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NotificationRepository extends BaseRepository {

    private static final String COLLECTION_NOTIFICATIONS = "notifications";
    private static final String COLLECTION_USERS = "users";

    public void getForUser(String uid, final DataCallback<List<AppNotification>> cb) {
        db.collection(COLLECTION_NOTIFICATIONS)
                .whereEqualTo("recipientId", uid)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .limit(50)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (!task.isSuccessful()) {
                            fail(cb, task.getException());
                            return;
                        }
                        List<AppNotification> list = new ArrayList<AppNotification>();
                        if (task.getResult() != null) {
                            for (DocumentSnapshot doc : task.getResult().getDocuments()) {
                                AppNotification notification = doc.toObject(AppNotification.class);
                                if (notification != null) {
                                    notification.setId(doc.getId());
                                    list.add(notification);
                                }
                            }
                        }
                        cb.onSuccess(list);
                    }
                });
    }

    public void markRead(String id, final DataCallback<Void> cb) {
        db.collection(COLLECTION_NOTIFICATIONS).document(id)
                .update("isRead", true, "updatedAt", FieldValue.serverTimestamp())
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            cb.onSuccess(null);
                        } else {
                            fail(cb, task.getException());
                        }
                    }
                });
    }

    public void create(final AppNotification notification, final WriteBatch batch, final DataCallback<Void> cb) {
        if (notification == null || notification.getRecipientId() == null) {
            cb.onError("Thông báo không hợp lệ.");
            return;
        }

        db.collection(COLLECTION_USERS).document(notification.getRecipientId()).get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (!task.isSuccessful()) {
                            fail(cb, task.getException());
                            return;
                        }
                        DocumentSnapshot userDoc = task.getResult();
                        if (userDoc != null && userDoc.exists()) {
                            Map<String, Object> prefs = (Map<String, Object>) userDoc.get("notificationPrefs");
                            if (prefs != null && notification.getType() != null) {
                                Object enabled = prefs.get(notification.getType().name());
                                if (Boolean.FALSE.equals(enabled)) {
                                    // User turned off this notification type.
                                    cb.onSuccess(null);
                                    return;
                                }
                            }
                        }
                        insertNotification(notification, batch, cb);
                    }
                });
    }

    private void insertNotification(AppNotification notification, WriteBatch batch, final DataCallback<Void> cb) {
        DocumentReference docRef = db.collection(COLLECTION_NOTIFICATIONS).document();
        Map<String, Object> data = new HashMap<String, Object>();
        data.put("recipientId", notification.getRecipientId());
        data.put("type", notification.getType() != null ? notification.getType().name() : null);
        data.put("title", notification.getTitle() != null ? notification.getTitle() : "");
        data.put("body", notification.getBody() != null ? notification.getBody() : "");
        data.put("targetId", notification.getTargetId() != null ? notification.getTargetId() : "");
        data.put("isRead", false);
        data.put("createdAt", FieldValue.serverTimestamp());
        data.put("updatedAt", FieldValue.serverTimestamp());

        if (batch != null) {
            batch.set(docRef, data);
            cb.onSuccess(null);
        } else {
            docRef.set(data).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        cb.onSuccess(null);
                    } else {
                        fail(cb, task.getException());
                    }
                }
            });
        }
    }
}
