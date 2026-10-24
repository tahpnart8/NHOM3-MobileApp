package com.nhom3.pubgapp.data;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.WriteBatch;
import com.nhom3.pubgapp.model.AppNotification;
import com.nhom3.pubgapp.model.Deal;
import com.nhom3.pubgapp.model.Review;
import com.nhom3.pubgapp.model.enums.DealStatus;
import com.nhom3.pubgapp.model.enums.NotificationType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReviewRepository extends BaseRepository {

    private static final String COLLECTION_REVIEWS = "reviews";
    private static final String COLLECTION_USERS = "users";
    private static final String COLLECTION_DEALS = "deals";

    private final NotificationRepository notificationRepository = new NotificationRepository();

    public void create(Review review, DataCallback<Void> cb) {
        String currentUid = currentUserId();
        if (currentUid.isEmpty()) {
            cb.onError("Chưa đăng nhập.");
            return;
        }
        if (review == null || review.getDealId() == null || review.getDealId().trim().isEmpty()) {
            cb.onError("Thông tin đánh giá không hợp lệ.");
            return;
        }
        if (!review.isValidRating()) {
            cb.onError("Điểm đánh giá phải từ 1 đến 5 sao.");
            return;
        }
        if (!isValidComment(review.getComment())) {
            cb.onError("Nội dung đánh giá không được vượt quá 500 ký tự.");
            return;
        }

        review.setReviewerId(currentUid);
        String reviewId = review.getDealId() + "_" + currentUid;
        review.setId(reviewId);

        DocumentReference reviewRef = db.collection(COLLECTION_REVIEWS).document(reviewId);
        reviewRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> checkTask) {
                if (!checkTask.isSuccessful()) {
                    fail(cb, checkTask.getException());
                    return;
                }
                DocumentSnapshot existingReview = checkTask.getResult();
                if (existingReview != null && existingReview.exists()) {
                    cb.onError("Bạn đã đánh giá giao dịch này rồi.");
                    return;
                }

                db.collection(COLLECTION_DEALS).document(review.getDealId()).get()
                        .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> dealTask) {
                                if (!dealTask.isSuccessful()) {
                                    fail(cb, dealTask.getException());
                                    return;
                                }
                                DocumentSnapshot dealDoc = dealTask.getResult();
                                if (dealDoc == null || !dealDoc.exists()) {
                                    cb.onError("Không tìm thấy giao dịch.");
                                    return;
                                }
                                Deal deal = dealDoc.toObject(Deal.class);
                                if (deal == null) {
                                    cb.onError("Dữ liệu giao dịch không hợp lệ.");
                                    return;
                                }

                                if (!deal.isParticipant(currentUid)) {
                                    cb.onError("Bạn không phải thành viên của giao dịch này.");
                                    return;
                                }
                                if (deal.getStatus() != DealStatus.RECEIVED
                                        && deal.getStatus() != DealStatus.COMPLETED) {
                                    cb.onError("Chỉ được đánh giá giao dịch "
                                            + "đã nhận hàng hoặc hoàn tất.");
                                    return;
                                }

                                String revieweeId = currentUid.equals(deal.getBuyerId())
                                        ? deal.getSellerId() : deal.getBuyerId();
                                review.setRevieweeId(revieweeId);

                                WriteBatch batch = db.batch();
                                batch.set(reviewRef, review);

                                DocumentReference userRef = db.collection(COLLECTION_USERS).document(revieweeId);
                                Map<String, Object> userUpdates = new HashMap<String, Object>();
                                userUpdates.put("ratingSum", FieldValue.increment(review.getRating()));
                                userUpdates.put("ratingCount", FieldValue.increment(1));
                                userUpdates.put("lastReviewId", reviewId);
                                userUpdates.put("updatedAt", FieldValue.serverTimestamp());
                                batch.update(userRef, userUpdates);

                                sendNotification(revieweeId, NotificationType.NEW_REVIEW,
                                        "Bạn có đánh giá mới",
                                        "Đối tác đã gửi đánh giá cho giao dịch gần đây.",
                                        review.getDealId(), batch);

                                batch.commit().addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> commitTask) {
                                        if (!commitTask.isSuccessful()) {
                                            fail(cb, commitTask.getException());
                                            return;
                                        }
                                        checkIfBothSidesReviewed(review.getDealId(), cb);
                                    }
                                });
                            }
                        });
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
                        if (!task.isSuccessful()) {
                            fail(cb, task.getException());
                            return;
                        }
                        if (task.getResult() != null && task.getResult().size() >= 2) {
                            DealRepository dealRepo = new DealRepository();
                            dealRepo.completeDeal(dealId, new DataCallback<Void>() {
                                @Override
                                public void onSuccess(Void result) {
                                    cb.onSuccess(null);
                                }

                                @Override
                                public void onError(String message) {
                                    cb.onError(message);
                                }
                            });
                        } else {
                            cb.onSuccess(null);
                        }
                    }
                });
    }

    static boolean isValidComment(String comment) {
        return comment == null || comment.length() <= 500;
    }

    public void getByDeal(String dealId, DataCallback<List<Review>> cb) {
        db.collection(COLLECTION_REVIEWS)
                .whereEqualTo("dealId", dealId)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (!task.isSuccessful()) {
                            fail(cb, task.getException());
                            return;
                        }
                        List<Review> list = new ArrayList<Review>();
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
                            fail(cb, task.getException());
                            return;
                        }
                        List<Review> list = new ArrayList<Review>();
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
                            fail(cb, task.getException());
                            return;
                        }
                        List<Review> list = new ArrayList<Review>();
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

    private void sendNotification(String recipientId, NotificationType type,
                                  String title, String body, String targetId,
                                  WriteBatch batch) {
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
        notificationRepository.create(notif, batch, new DataCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
            }

            @Override
            public void onError(String message) {
            }
        });
    }
}
