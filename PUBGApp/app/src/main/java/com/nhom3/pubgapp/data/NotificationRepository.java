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
import com.nhom3.pubgapp.model.enums.NotificationType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NotificationRepository extends BaseRepository {

    private static final String COLLECTION_NOTIFICATIONS = "notifications";
    private static final String COLLECTION_USERS = "users";

    static final int MAX_TITLE_LENGTH = 100;
    static final int MAX_BODY_LENGTH = 300;
    static final int MAX_TARGET_ID_LENGTH = 200;

    static String truncate(String text, int limit) {
        if (text == null) {
            return "";
        }
        if (text.length() <= limit) {
            return text;
        }
        return text.substring(0, limit);
    }

    static String getDefaultTitle(NotificationType type) {
        if (type == null) {
            return "Thông báo";
        }
        switch (type) {
            case NEW_MESSAGE:
                return "Tin nhắn mới";
            case NEW_OFFER:
                return "Đề nghị giá mới";
            case OFFER_RESULT:
                return "Kết quả đề nghị giá";
            case DEAL_UPDATE:
                return "Cập nhật đơn hàng";
            case PAYMENT_UPDATE:
                return "Cập nhật thanh toán";
            case NEW_REVIEW:
                return "Đánh giá mới";
            case COMPLAINT_RESULT:
                return "Kết quả khiếu nại";
            case WARNING:
                return "Cảnh báo từ quản trị viên";
            case LISTING_MODERATED:
                return "Kết quả duyệt tin đăng";
            case VERIFICATION_RESULT:
                return "Kết quả xác thực tài khoản";
            default:
                return "Thông báo";
        }
    }

    static String getDefaultBody(NotificationType type) {
        if (type == null) {
            return "Bạn có thông báo mới.";
        }
        switch (type) {
            case NEW_MESSAGE:
                return "Bạn có tin nhắn mới trong cuộc trò chuyện.";
            case NEW_OFFER:
                return "Bạn nhận được một đề nghị giá mới cho tin đăng.";
            case OFFER_RESULT:
                return "Đề nghị giá của bạn đã có cập nhật mới.";
            case DEAL_UPDATE:
                return "Giao dịch của bạn đã có thay đổi trạng thái.";
            case PAYMENT_UPDATE:
                return "Thông tin thanh toán của bạn đã được cập nhật.";
            case NEW_REVIEW:
                return "Bạn nhận được một đánh giá mới từ đối tác.";
            case COMPLAINT_RESULT:
                return "Khiếu nại của bạn đã có kết quả xử lý từ quản trị viên.";
            case WARNING:
                return "Tài khoản của bạn nhận được cảnh báo vi phạm quy chuẩn.";
            case LISTING_MODERATED:
                return "Tin đăng của bạn đã được quản trị viên xử lý kiểm duyệt.";
            case VERIFICATION_RESULT:
                return "Yêu cầu xác thực tài khoản của bạn đã có kết quả.";
            default:
                return "Bạn có thông báo mới.";
        }
    }

    static Map<String, Object> buildNotificationData(AppNotification notification) {
        Map<String, Object> data = new HashMap<String, Object>();
        if (notification == null) {
            return data;
        }
        data.put("recipientId", notification.getRecipientId());
        data.put("type", notification.getType() != null ? notification.getType().name() : null);

        String rawTitle = notification.getTitle();
        if (rawTitle == null || rawTitle.trim().isEmpty()) {
            rawTitle = getDefaultTitle(notification.getType());
        }
        data.put("title", truncate(rawTitle, MAX_TITLE_LENGTH));

        String rawBody = notification.getBody();
        if (rawBody == null || rawBody.trim().isEmpty()) {
            rawBody = getDefaultBody(notification.getType());
        }
        data.put("body", truncate(rawBody, MAX_BODY_LENGTH));

        String targetId = notification.getTargetId();
        data.put("targetId", targetId != null ? truncate(targetId, MAX_TARGET_ID_LENGTH) : "");
        data.put("isRead", false);
        data.put("createdAt", FieldValue.serverTimestamp());
        data.put("updatedAt", FieldValue.serverTimestamp());
        return data;
    }

    public void getForUser(final String uid, final DataCallback<List<AppNotification>> cb) {
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
                        hideDisabledTypes(uid, list, cb);
                    }
                });
    }

    // Drops the notices whose type the user switched off in the notification settings.
    private void hideDisabledTypes(String uid, final List<AppNotification> list,
                                   final DataCallback<List<AppNotification>> cb) {
        db.collection(COLLECTION_USERS).document(uid).get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        Map<String, Object> prefs = null;
                        if (task.isSuccessful() && task.getResult() != null) {
                            prefs = (Map<String, Object>) task.getResult().get("notificationPrefs");
                        }
                        cb.onSuccess(withoutDisabledTypes(list, prefs));
                    }
                });
    }

    static List<AppNotification> withoutDisabledTypes(List<AppNotification> list, Map<String, Object> prefs) {
        if (list == null || prefs == null || prefs.isEmpty()) {
            return list;
        }
        List<AppNotification> kept = new ArrayList<AppNotification>();
        for (int i = 0; i < list.size(); i++) {
            AppNotification notification = list.get(i);
            NotificationType type = notification.getType();
            if (type != null && Boolean.FALSE.equals(prefs.get(type.name()))) {
                continue;
            }
            kept.add(notification);
        }
        return kept;
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

    static String validateNotification(AppNotification notification) {
        if (notification == null || notification.getRecipientId() == null
                || notification.getRecipientId().trim().isEmpty()) {
            return "Thông báo không hợp lệ.";
        }
        if (notification.getType() == null) {
            return "Loại thông báo không hợp lệ.";
        }
        return null;
    }

    public void create(final AppNotification notification, final WriteBatch batch, final DataCallback<Void> cb) {
        String validationError = validateNotification(notification);
        if (validationError != null) {
            if (cb != null) {
                cb.onError(validationError);
            }
            return;
        }

        // The caller commits the batch right after this call, so add the notice now; getForUser
        // hides the types the user switched off.
        if (batch != null) {
            insertNotification(notification, batch, cb);
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
                                    if (cb != null) {
                                        cb.onSuccess(null);
                                    }
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
        Map<String, Object> data = buildNotificationData(notification);

        if (batch != null) {
            batch.set(docRef, data);
            if (cb != null) {
                cb.onSuccess(null);
            }
        } else {
            docRef.set(data).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        if (cb != null) {
                            cb.onSuccess(null);
                        }
                    } else {
                        fail(cb, task.getException());
                    }
                }
            });
        }
    }
}
