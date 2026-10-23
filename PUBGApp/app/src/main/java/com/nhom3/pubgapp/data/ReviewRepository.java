package com.nhom3.pubgapp.data;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.WriteBatch;
import com.nhom3.pubgapp.model.AppNotification;
import com.nhom3.pubgapp.model.Review;
import com.nhom3.pubgapp.model.enums.NotificationType;

import java.util.ArrayList;
import java.util.List;

public class ReviewRepository extends BaseRepository {

    private static final String COLLECTION_REVIEWS = "reviews";
    private static final String COLLECTION_USERS = "users";
    private static final String COLLECTION_NOTIFICATIONS = "notifications";

    public void create(Review review, DataCallback<Void> cb) {
        if (review == null || review.getDealId() == null || review.getReviewerId() == null) {
            cb.onError("Thông tin đánh giá không hợp lệ.");
            return;
        }
        if (!review.isValidRating()) {
            cb.onError("Điểm đánh giá phải từ 1 đến 5 sao.");
            return;
        }

        String reviewId = review.getDealId() + "_" + review.getReviewerId();
        review.setId(reviewId);
        review.setCreatedAt(Timestamp.now());

        WriteBatch batch = db.batch();
        DocumentReference reviewRef = db.collection(COLLECTION_REVIEWS).document(reviewId);
        batch.set(reviewRef, review);

        // Cập nhật ratingSum và ratingCount của người được đánh giá
        if (review.getRevieweeId() != null) {
            DocumentReference userRef = db.collection(COLLECTION_USERS).document(review.getRevieweeId());
            batch.update(userRef, "ratingSum", FieldValue.increment(review.getRating()),
                    "ratingCount", FieldValue.increment(1));

            // Gửi thông báo cho người nhận đánh giá
            queueNotification(review.getRevieweeId(), NotificationType.NEW_REVIEW,
                    "Bạn có đánh giá mới",
                    "Đối tác đã gửi đánh giá cho giao dịch gần đây.",
                    review.getDealId(), batch);
        }

        batch.commit().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (!task.isSuccessful()) {
                    cb.onError(getErrorMessage(task.getException(), "Lỗi lưu đánh giá."));
                    return;
                }
                // Kiểm tra xem cả hai bên đã đánh giá chưa để hoàn tất giao dịch
                checkIfBothSidesReviewed(review.getDealId(), cb);
            }
        });
    }

    private void checkIfBothSidesReviewed(String dealId, DataCallback<Void> cb) {
        db.collection(COLLECTION_REVIEWS)
                .whereEqualTo("dealId", dealId)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful() && task.getResult() != null
                                && task.getResult().size() >= 2) {
                            DealRepository dealRepo = new DealRepository();
                            dealRepo.completeDeal(dealId, new DataCallback<Void>() {
                                @Override
                                public void onSuccess(Void result) {
                                    cb.onSuccess(null);
                                }

                                @Override
                                public void onError(String message) {
                                    // Vẫn coi là thành công vì review đã được ghi
                                    cb.onSuccess(null);
                                }
                            });
                        } else {
                            cb.onSuccess(null);
                        }
                    }
                });
    }

    public void getByDeal(String dealId, DataCallback<List<Review>> cb) {
        db.collection(COLLECTION_REVIEWS)
                .whereEqualTo("dealId", dealId)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (!task.isSuccessful()) {
                            cb.onError(getErrorMessage(task.getException(), "Lỗi tải đánh giá giao dịch."));
                            return;
                        }
                        List<Review> list = new ArrayList<>();
                        if (task.getResult() != null) {
                            for (QueryDocumentSnapshot doc : task.getResult()) {
                                Review rev = doc.toObject(Review.class);
                                rev.setId(doc.getId());
                                list.add(rev);
                            }
                        }
                        cb.onSuccess(list);
                    }
                });
    }

    public void getReceived(String uid, DataCallback<List<Review>> cb) {
        db.collection(COLLECTION_REVIEWS)
                .whereEqualTo("revieweeId", uid)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (!task.isSuccessful()) {
                            cb.onError(getErrorMessage(task.getException(), "Lỗi tải đánh giá đã nhận."));
                            return;
                        }
                        List<Review> list = new ArrayList<>();
                        if (task.getResult() != null) {
                            for (QueryDocumentSnapshot doc : task.getResult()) {
                                Review rev = doc.toObject(Review.class);
                                rev.setId(doc.getId());
                                list.add(rev);
                            }
                        }
                        cb.onSuccess(list);
                    }
                });
    }

    public void getGiven(String uid, DataCallback<List<Review>> cb) {
        db.collection(COLLECTION_REVIEWS)
                .whereEqualTo("reviewerId", uid)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (!task.isSuccessful()) {
                            cb.onError(getErrorMessage(task.getException(), "Lỗi tải đánh giá đã gửi."));
                            return;
                        }
                        List<Review> list = new ArrayList<>();
                        if (task.getResult() != null) {
                            for (QueryDocumentSnapshot doc : task.getResult()) {
                                Review rev = doc.toObject(Review.class);
                                rev.setId(doc.getId());
                                list.add(rev);
                            }
                        }
                        cb.onSuccess(list);
                    }
                });
    }

    private void queueNotification(String recipientId, NotificationType type,
                                   String title, String body, String targetId, WriteBatch batch) {
        if (recipientId == null || recipientId.trim().isEmpty()) {
            return;
        }
        AppNotification notif = new AppNotification();
        notif.setRecipientId(recipientId);
        notif.setType(type);
        notif.setTitle(title);
        notif.setBody(body);
        notif.setTargetId(targetId);
        notif.setRead(false);
        notif.setCreatedAt(Timestamp.now());
        DocumentReference notifRef = db.collection(COLLECTION_NOTIFICATIONS).document();
        notif.setId(notifRef.getId());
        batch.set(notifRef, notif);
    }

    private String getErrorMessage(Exception exception, String fallback) {
        if (exception != null && exception.getMessage() != null) {
            return exception.getMessage();
        }
        return fallback;
    }
}
