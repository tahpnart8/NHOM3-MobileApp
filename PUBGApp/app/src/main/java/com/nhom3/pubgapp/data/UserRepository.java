package com.nhom3.pubgapp.data;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.AggregateQuerySnapshot;
import com.google.firebase.firestore.AggregateSource;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.WriteBatch;
import com.nhom3.pubgapp.model.ProfileStats;
import com.nhom3.pubgapp.model.SellerVerification;
import com.nhom3.pubgapp.model.User;
import com.nhom3.pubgapp.model.UserAddress;
import com.nhom3.pubgapp.model.UserIdentity;
import com.nhom3.pubgapp.model.enums.UserStatus;
import com.nhom3.pubgapp.model.enums.VerificationStatus;
import com.nhom3.pubgapp.util.FormatUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserRepository extends BaseRepository {

    private static final String TAG = "UserRepository";

    private static final String COLLECTION_USERS = "users";
    private static final String COLLECTION_FOLLOWS = "follows";
    private static final String COLLECTION_SAVED = "saved";
    private static final String COLLECTION_SELLER_VERIFICATIONS = "sellerVerifications";
    private static final String COLLECTION_DEALS = "deals";
    private static final String COLLECTION_LISTINGS = "listings";

    private static final String SUB_ADDRESSES = "addresses";
    private static final String SUB_PRIVATE = "private";
    private static final String DOC_IDENTITY = "identity";

    public void createProfile(final User user, UserIdentity identity, final DataCallback<Void> cb) {
        String rawUid = user.getId();
        if (rawUid == null || rawUid.isEmpty()) {
            rawUid = currentUserId();
        }
        final String uid = rawUid;
        FirebaseUser fu = auth.getCurrentUser();
        if (fu == null) {
            cb.onError("Người dùng chưa đăng nhập.");
            return;
        }
        final String email = fu.getEmail();
        if (email == null || email.isEmpty()) {
            cb.onError("Không tìm thấy email của tài khoản.");
            return;
        }
        user.setStatus(UserStatus.ACTIVE);
        user.setVerificationStatus(VerificationStatus.NONE);
        user.setRatingSum(0);
        user.setRatingCount(0);
        user.setWarningCount(0);
        user.setLockedUntil(null);
        user.setLockReason("");
        if (user.getInterestedCategoryIds() == null) {
            user.setInterestedCategoryIds(new ArrayList<String>());
        }
        if (user.getNotificationPrefs() == null) {
            user.setNotificationPrefs(new HashMap<String, Boolean>());
        }
        user.buildSearchKeywords();

        Map<String, Object> userMap = new HashMap<String, Object>();
        userMap.put("email", email);
        userMap.put("displayName", user.getDisplayName() != null ? user.getDisplayName() : "");
        userMap.put("phone", user.getPhone() != null ? user.getPhone() : "");
        userMap.put("avatarBase64", user.getAvatarBase64() != null ? user.getAvatarBase64() : "");
        userMap.put("bio", user.getBio() != null ? user.getBio() : "");
        userMap.put("province", user.getProvince() != null ? user.getProvince() : "");
        userMap.put("district", user.getDistrict() != null ? user.getDistrict() : "");
        userMap.put("interestedCategoryIds", user.getInterestedCategoryIds());
        userMap.put("termsAcceptedAt", FieldValue.serverTimestamp());
        userMap.put("notificationPrefs", user.getNotificationPrefs());
        userMap.put("lastActiveAt", FieldValue.serverTimestamp());
        userMap.put("lockedUntil", null);
        userMap.put("lockReason", "");
        userMap.put("status", UserStatus.ACTIVE.name());
        userMap.put("verificationStatus", VerificationStatus.NONE.name());
        userMap.put("ratingSum", 0);
        userMap.put("ratingCount", 0);
        userMap.put("warningCount", 0);
        userMap.put("searchKeywords", user.getSearchKeywords());
        userMap.put("lastReviewId", "");
        userMap.put("createdAt", FieldValue.serverTimestamp());
        userMap.put("updatedAt", FieldValue.serverTimestamp());

        WriteBatch batch = db.batch();
        DocumentReference userRef = db.collection(COLLECTION_USERS).document(uid);
        batch.set(userRef, userMap);

        if (identity != null) {
            identity.setUserId(uid);
            Map<String, Object> idMap = new HashMap<String, Object>();
            idMap.put("userId", uid);
            idMap.put("nationalId", identity.getNationalId() != null ? identity.getNationalId() : "");
            idMap.put("dateOfBirth", identity.getDateOfBirth());
            idMap.put("gender", identity.getGender() != null ? identity.getGender().name() : null);
            idMap.put("permanentAddress", identity.getPermanentAddress() != null ? identity.getPermanentAddress() : "");
            idMap.put("createdAt", FieldValue.serverTimestamp());
            idMap.put("updatedAt", FieldValue.serverTimestamp());

            DocumentReference idRef = db.collection(COLLECTION_USERS)
                    .document(uid)
                    .collection(SUB_PRIVATE)
                    .document(DOC_IDENTITY);
            batch.set(idRef, idMap);
        }

        batch.commit().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    user.setId(uid);
                    user.setEmail(email);
                    cb.onSuccess(null);
                } else {
                    fail(cb, task.getException());
                }
            }
        });
    }

    public void getProfile(String uid, final DataCallback<User> cb) {
        db.collection(COLLECTION_USERS).document(uid).get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (!task.isSuccessful()) {
                            fail(cb, task.getException());
                            return;
                        }
                        DocumentSnapshot doc = task.getResult();
                        if (doc != null && doc.exists()) {
                            User user = doc.toObject(User.class);
                            cb.onSuccess(user);
                        } else {
                            cb.onError("Không tìm thấy người dùng.");
                        }
                    }
                });
    }

    public void updateProfile(User user, final DataCallback<Void> cb) {
        String uid = user.getId();
        if (uid == null || uid.isEmpty()) {
            uid = currentUserId();
        }
        user.buildSearchKeywords();
        Map<String, Object> updates = new HashMap<String, Object>();
        updates.put("displayName", user.getDisplayName());
        updates.put("phone", user.getPhone());
        updates.put("avatarBase64", user.getAvatarBase64());
        updates.put("bio", user.getBio());
        updates.put("province", user.getProvince());
        updates.put("district", user.getDistrict());
        updates.put("interestedCategoryIds", user.getInterestedCategoryIds());
        updates.put("searchKeywords", user.getSearchKeywords());
        updates.put("updatedAt", FieldValue.serverTimestamp());

        db.collection(COLLECTION_USERS).document(uid).update(updates)
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

    public void searchUsers(String keyword, final DataCallback<List<User>> cb) {
        FormatUtils formatUtils = new FormatUtils();
        List<String> keywords = formatUtils.toKeywords(keyword);
        if (keywords.isEmpty()) {
            cb.onSuccess(new ArrayList<User>());
            return;
        }
        String longestWord = keywords.get(0);
        for (String word : keywords) {
            if (word.length() > longestWord.length()) {
                longestWord = word;
            }
        }
        db.collection(COLLECTION_USERS)
                .whereEqualTo("status", UserStatus.ACTIVE.name())
                .whereArrayContains("searchKeywords", longestWord)
                .orderBy("ratingCount", Query.Direction.DESCENDING)
                .limit(20)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (!task.isSuccessful()) {
                            fail(cb, task.getException());
                            return;
                        }
                        List<User> list = new ArrayList<User>();
                        QuerySnapshot snapshot = task.getResult();
                        if (snapshot != null) {
                            for (DocumentSnapshot doc : snapshot.getDocuments()) {
                                User u = doc.toObject(User.class);
                                if (u != null) {
                                    list.add(u);
                                }
                            }
                        }
                        cb.onSuccess(list);
                    }
                });
    }

    private static class ProfileStatsFetcher {
        private final FirebaseFirestore db;
        private final String uid;
        private final DataCallback<ProfileStats> cb;
        private final ProfileStats stats = new ProfileStats();
        private int sellerDeals = 0;

        ProfileStatsFetcher(FirebaseFirestore db, String uid, DataCallback<ProfileStats> cb) {
            this.db = db;
            this.uid = uid;
            this.cb = cb;
        }

        void fetch() {
            fetchSoldDeals();
        }

        private void fetchSoldDeals() {
            db.collection(COLLECTION_DEALS)
                    .whereEqualTo("sellerId", uid)
                    .whereEqualTo("status", "COMPLETED")
                    .count()
                    .get(AggregateSource.SERVER)
                    .addOnSuccessListener(new OnSuccessListener<AggregateQuerySnapshot>() {
                        @Override
                        public void onSuccess(AggregateQuerySnapshot snapshot) {
                            stats.setSoldCount((int) snapshot.getCount());
                            fetchBuyerCompletedDeals();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            cb.onError(e != null ? e.getMessage() : "Lỗi tải thống kê");
                        }
                    });
        }

        private void fetchBuyerCompletedDeals() {
            db.collection(COLLECTION_DEALS)
                    .whereEqualTo("buyerId", uid)
                    .whereEqualTo("status", "COMPLETED")
                    .count()
                    .get(AggregateSource.SERVER)
                    .addOnSuccessListener(new OnSuccessListener<AggregateQuerySnapshot>() {
                        @Override
                        public void onSuccess(AggregateQuerySnapshot snapshot) {
                            int buyerCompleted = (int) snapshot.getCount();
                            stats.setCompletedDealCount(stats.getSoldCount() + buyerCompleted);
                            fetchFollowers();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            cb.onError(e != null ? e.getMessage() : "Lỗi tải thống kê");
                        }
                    });
        }

        private void fetchFollowers() {
            db.collection(COLLECTION_FOLLOWS)
                    .whereEqualTo("followedId", uid)
                    .count()
                    .get(AggregateSource.SERVER)
                    .addOnSuccessListener(new OnSuccessListener<AggregateQuerySnapshot>() {
                        @Override
                        public void onSuccess(AggregateQuerySnapshot snapshot) {
                            stats.setFollowerCount((int) snapshot.getCount());
                            fetchSellerDeals();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            cb.onError(e != null ? e.getMessage() : "Lỗi tải thống kê");
                        }
                    });
        }

        private void fetchSellerDeals() {
            db.collection(COLLECTION_DEALS)
                    .whereEqualTo("sellerId", uid)
                    .whereNotEqualTo("status", "CANCELLED")
                    .count()
                    .get(AggregateSource.SERVER)
                    .addOnSuccessListener(new OnSuccessListener<AggregateQuerySnapshot>() {
                        @Override
                        public void onSuccess(AggregateQuerySnapshot snapshot) {
                            sellerDeals = (int) snapshot.getCount();
                            fetchBuyerDeals();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            cb.onError(e != null ? e.getMessage() : "Lỗi tải thống kê");
                        }
                    });
        }

        private void fetchBuyerDeals() {
            db.collection(COLLECTION_DEALS)
                    .whereEqualTo("buyerId", uid)
                    .whereNotEqualTo("status", "CANCELLED")
                    .count()
                    .get(AggregateSource.SERVER)
                    .addOnSuccessListener(new OnSuccessListener<AggregateQuerySnapshot>() {
                        @Override
                        public void onSuccess(AggregateQuerySnapshot snapshot) {
                            int buyerDeals = (int) snapshot.getCount();
                            stats.setDealCount(sellerDeals + buyerDeals);
                            cb.onSuccess(stats);
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            cb.onError(e != null ? e.getMessage() : "Lỗi tải thống kê");
                        }
                    });
        }
    }

    public void getStats(final String uid, final DataCallback<ProfileStats> cb) {
        new ProfileStatsFetcher(db, uid, cb).fetch();
    }

    public void clearExpiredLock(String uid, final DataCallback<Void> cb) {
        Map<String, Object> updates = new HashMap<String, Object>();
        updates.put("status", UserStatus.ACTIVE.name());
        updates.put("lockedUntil", null);
        updates.put("lockReason", "");
        updates.put("updatedAt", FieldValue.serverTimestamp());

        db.collection(COLLECTION_USERS).document(uid).update(updates)
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

    public void saveAddress(final String uid, final UserAddress address, final DataCallback<Void> cb) {
        address.setUserId(uid);
        String addrId = address.getId();
        final DocumentReference ref;
        if (addrId == null || addrId.isEmpty()) {
            ref = db.collection(COLLECTION_USERS).document(uid).collection(SUB_ADDRESSES).document();
        } else {
            ref = db.collection(COLLECTION_USERS).document(uid).collection(SUB_ADDRESSES).document(addrId);
        }

        if (address.isDefault()) {
            db.collection(COLLECTION_USERS).document(uid).collection(SUB_ADDRESSES)
                    .whereEqualTo("isDefault", true)
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (!task.isSuccessful()) {
                                fail(cb, task.getException());
                                return;
                            }
                            WriteBatch batch = db.batch();
                            if (task.getResult() != null) {
                                for (DocumentSnapshot doc : task.getResult().getDocuments()) {
                                    if (!doc.getId().equals(ref.getId())) {
                                        batch.update(doc.getReference(),
                                                "isDefault", false,
                                                "updatedAt", FieldValue.serverTimestamp());
                                    }
                                }
                            }
                            address.touch();
                            batch.set(ref, address);
                            batch.commit().addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> commitTask) {
                                    if (commitTask.isSuccessful()) {
                                        cb.onSuccess(null);
                                    } else {
                                        fail(cb, commitTask.getException());
                                    }
                                }
                            });
                        }
                    });
        } else {
            address.touch();
            ref.set(address).addOnCompleteListener(new OnCompleteListener<Void>() {
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

    public void deleteAddress(String uid, String id, final DataCallback<Void> cb) {
        db.collection(COLLECTION_USERS).document(uid).collection(SUB_ADDRESSES).document(id).delete()
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

    public void getAddresses(String uid, final DataCallback<List<UserAddress>> cb) {
        db.collection(COLLECTION_USERS).document(uid).collection(SUB_ADDRESSES)
                .orderBy("isDefault", Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (!task.isSuccessful()) {
                            fail(cb, task.getException());
                            return;
                        }
                        List<UserAddress> list = new ArrayList<UserAddress>();
                        if (task.getResult() != null) {
                            for (DocumentSnapshot doc : task.getResult().getDocuments()) {
                                UserAddress addr = doc.toObject(UserAddress.class);
                                if (addr != null) {
                                    list.add(addr);
                                }
                            }
                        }
                        cb.onSuccess(list);
                    }
                });
    }

    public void follow(String uid, String otherId, final DataCallback<Void> cb) {
        String followId = uid + "_" + otherId;
        Map<String, Object> data = new HashMap<String, Object>();
        data.put("followerId", uid);
        data.put("followedId", otherId);
        data.put("createdAt", FieldValue.serverTimestamp());

        db.collection(COLLECTION_FOLLOWS).document(followId).set(data)
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

    public void unfollow(String uid, String otherId, final DataCallback<Void> cb) {
        String followId = uid + "_" + otherId;
        db.collection(COLLECTION_FOLLOWS).document(followId).delete()
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

    public void getFollowingIds(String uid, final DataCallback<List<String>> cb) {
        db.collection(COLLECTION_FOLLOWS).whereEqualTo("followerId", uid).get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (!task.isSuccessful()) {
                            fail(cb, task.getException());
                            return;
                        }
                        List<String> list = new ArrayList<String>();
                        if (task.getResult() != null) {
                            for (DocumentSnapshot doc : task.getResult().getDocuments()) {
                                String followedId = doc.getString("followedId");
                                if (followedId != null && !followedId.isEmpty()) {
                                    list.add(followedId);
                                }
                            }
                        }
                        cb.onSuccess(list);
                    }
                });
    }

    public void saveListing(String uid, String listingId, final DataCallback<Void> cb) {
        Map<String, Object> data = new HashMap<String, Object>();
        data.put("userId", uid);
        data.put("listingId", listingId);
        data.put("savedAt", FieldValue.serverTimestamp());

        db.collection(COLLECTION_USERS).document(uid).collection(COLLECTION_SAVED).document(listingId).set(data)
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

    public void unsaveListing(String uid, String listingId, final DataCallback<Void> cb) {
        db.collection(COLLECTION_USERS).document(uid).collection(COLLECTION_SAVED).document(listingId).delete()
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

    public void getSavedIds(String uid, final DataCallback<List<String>> cb) {
        db.collection(COLLECTION_USERS).document(uid).collection(COLLECTION_SAVED)
                .orderBy("savedAt", Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (!task.isSuccessful()) {
                            fail(cb, task.getException());
                            return;
                        }
                        List<String> list = new ArrayList<String>();
                        if (task.getResult() != null) {
                            for (DocumentSnapshot doc : task.getResult().getDocuments()) {
                                String listingId = doc.getString("listingId");
                                if (listingId != null && !listingId.isEmpty()) {
                                    list.add(listingId);
                                } else {
                                    list.add(doc.getId());
                                }
                            }
                        }
                        cb.onSuccess(list);
                    }
                });
    }

    public void requestVerification(
            final String uid,
            final SellerVerification verification,
            final DataCallback<Void> cb) {
        final DocumentReference verRef = db.collection(COLLECTION_SELLER_VERIFICATIONS).document(uid);
        verRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (!task.isSuccessful()) {
                    fail(cb, task.getException());
                    return;
                }
                DocumentSnapshot doc = task.getResult();
                WriteBatch batch = db.batch();
                DocumentReference userRef = db.collection(COLLECTION_USERS).document(uid);

                if (doc != null && doc.exists()) {
                    SellerVerification existing = doc.toObject(SellerVerification.class);
                    if (existing != null && !existing.canResubmit()) {
                        cb.onError("Đã hết số lần gửi lại, hãy liên hệ admin.");
                        return;
                    }
                    int newResubmitCount = existing != null ? existing.getResubmitCount() + 1 : 1;
                    Map<String, Object> updates = new HashMap<String, Object>();
                    updates.put("nationalId", verification.getNationalId());
                    updates.put("imageIds", verification.getImageIds());
                    updates.put("status", VerificationStatus.PENDING.name());
                    updates.put("note", "");
                    updates.put("resubmitCount", newResubmitCount);
                    updates.put("reviewedBy", null);
                    updates.put("reviewedAt", null);
                    updates.put("updatedAt", FieldValue.serverTimestamp());

                    batch.update(verRef, updates);
                    batch.update(userRef,
                            "verificationStatus", VerificationStatus.PENDING.name(),
                            "updatedAt", FieldValue.serverTimestamp());
                } else {
                    Map<String, Object> data = new HashMap<String, Object>();
                    data.put("userId", uid);
                    data.put("nationalId", verification.getNationalId());
                    data.put("imageIds", verification.getImageIds());
                    data.put("status", VerificationStatus.PENDING.name());
                    data.put("note", "");
                    data.put("resubmitCount", 0);
                    data.put("reviewedBy", null);
                    data.put("reviewedAt", null);
                    data.put("createdAt", FieldValue.serverTimestamp());
                    data.put("updatedAt", FieldValue.serverTimestamp());

                    batch.set(verRef, data);
                    batch.update(userRef,
                            "verificationStatus", VerificationStatus.PENDING.name(),
                            "updatedAt", FieldValue.serverTimestamp());
                }

                batch.commit().addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> commitTask) {
                        if (commitTask.isSuccessful()) {
                            cb.onSuccess(null);
                        } else {
                            fail(cb, commitTask.getException());
                        }
                    }
                });
            }
        });
    }

    public void getVerification(String uid, final DataCallback<SellerVerification> cb) {
        db.collection(COLLECTION_SELLER_VERIFICATIONS).document(uid).get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (!task.isSuccessful()) {
                            fail(cb, task.getException());
                            return;
                        }
                        DocumentSnapshot doc = task.getResult();
                        if (doc != null && doc.exists()) {
                            SellerVerification sv = doc.toObject(SellerVerification.class);
                            cb.onSuccess(sv);
                        } else {
                            cb.onSuccess(null);
                        }
                    }
                });
    }

    public void saveIdentity(final String uid, final UserIdentity identity, final DataCallback<Void> cb) {
        identity.setUserId(uid);
        final DocumentReference ref = db.collection(COLLECTION_USERS)
                .document(uid)
                .collection(SUB_PRIVATE)
                .document(DOC_IDENTITY);

        ref.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (!task.isSuccessful()) {
                    fail(cb, task.getException());
                    return;
                }
                DocumentSnapshot doc = task.getResult();
                if (doc != null && doc.exists()) {
                    Map<String, Object> updates = new HashMap<String, Object>();
                    updates.put("nationalId", identity.getNationalId());
                    updates.put("dateOfBirth", identity.getDateOfBirth());
                    updates.put("gender", identity.getGender() != null ? identity.getGender().name() : null);
                    updates.put("permanentAddress",
                            identity.getPermanentAddress() != null ? identity.getPermanentAddress() : "");
                    updates.put("updatedAt", FieldValue.serverTimestamp());

                    ref.update(updates).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> updateTask) {
                            if (updateTask.isSuccessful()) {
                                cb.onSuccess(null);
                            } else {
                                fail(cb, updateTask.getException());
                            }
                        }
                    });
                } else {
                    Map<String, Object> data = new HashMap<String, Object>();
                    data.put("userId", uid);
                    data.put("nationalId", identity.getNationalId());
                    data.put("dateOfBirth", identity.getDateOfBirth());
                    data.put("gender", identity.getGender() != null ? identity.getGender().name() : null);
                    data.put("permanentAddress",
                            identity.getPermanentAddress() != null ? identity.getPermanentAddress() : "");
                    data.put("createdAt", FieldValue.serverTimestamp());
                    data.put("updatedAt", FieldValue.serverTimestamp());

                    ref.set(data).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> setTask) {
                            if (setTask.isSuccessful()) {
                                cb.onSuccess(null);
                            } else {
                                fail(cb, setTask.getException());
                            }
                        }
                    });
                }
            }
        });
    }

    public void getIdentity(String uid, final DataCallback<UserIdentity> cb) {
        db.collection(COLLECTION_USERS)
                .document(uid)
                .collection(SUB_PRIVATE)
                .document(DOC_IDENTITY)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (!task.isSuccessful()) {
                            fail(cb, task.getException());
                            return;
                        }
                        DocumentSnapshot doc = task.getResult();
                        if (doc != null && doc.exists()) {
                            UserIdentity identity = doc.toObject(UserIdentity.class);
                            cb.onSuccess(identity);
                        } else {
                            cb.onSuccess(null);
                        }
                    }
                });
    }

    public void updateNotificationPrefs(String uid, Map<String, Boolean> prefs, final DataCallback<Void> cb) {
        db.collection(COLLECTION_USERS).document(uid)
                .update("notificationPrefs", prefs, "updatedAt", FieldValue.serverTimestamp())
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

    public void touchLastActive(String uid) {
        Map<String, Object> updates = new HashMap<String, Object>();
        updates.put("lastActiveAt", FieldValue.serverTimestamp());
        updates.put("updatedAt", FieldValue.serverTimestamp());
        db.collection(COLLECTION_USERS).document(uid).update(updates)
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Could not update lastActiveAt", e);
                    }
                });
    }

    private static class DeactivateHandler {
        private final FirebaseFirestore db;
        private final String uid;
        private final DataCallback<Void> cb;

        DeactivateHandler(FirebaseFirestore db, String uid, DataCallback<Void> cb) {
            this.db = db;
            this.uid = uid;
            this.cb = cb;
        }

        void run() {
            checkSellerDeals();
        }

        private void checkSellerDeals() {
            List<String> activeStatuses = Arrays.asList(
                    "PENDING_CONFIRM", "CONFIRMED", "SHIPPED", "RECEIVED", "DISPUTED"
            );
            db.collection(COLLECTION_DEALS)
                    .whereEqualTo("sellerId", uid)
                    .whereIn("status", activeStatuses)
                    .limit(1)
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (!task.isSuccessful()) {
                                Exception e = task.getException();
                                cb.onError(e != null ? e.getMessage() : "Lỗi kiểm tra giao dịch");
                                return;
                            }
                            if (task.getResult() != null && !task.getResult().isEmpty()) {
                                cb.onError("Bạn còn giao dịch chưa hoàn tất. "
                                        + "Không thể vô hiệu hóa tài khoản lúc này.");
                                return;
                            }
                            checkBuyerDeals();
                        }
                    });
        }

        private void checkBuyerDeals() {
            List<String> activeStatuses = Arrays.asList(
                    "PENDING_CONFIRM", "CONFIRMED", "SHIPPED", "RECEIVED", "DISPUTED"
            );
            db.collection(COLLECTION_DEALS)
                    .whereEqualTo("buyerId", uid)
                    .whereIn("status", activeStatuses)
                    .limit(1)
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (!task.isSuccessful()) {
                                Exception e = task.getException();
                                cb.onError(e != null ? e.getMessage() : "Lỗi kiểm tra giao dịch");
                                return;
                            }
                            if (task.getResult() != null && !task.getResult().isEmpty()) {
                                cb.onError("Bạn còn giao dịch chưa hoàn tất. "
                                        + "Không thể vô hiệu hóa tài khoản lúc này.");
                                return;
                            }
                            hideListingsAndDeactivate();
                        }
                    });
        }

        private void hideListingsAndDeactivate() {
            db.collection(COLLECTION_LISTINGS)
                    .whereEqualTo("sellerId", uid)
                    .whereEqualTo("status", "AVAILABLE")
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (!task.isSuccessful()) {
                                Exception e = task.getException();
                                cb.onError(e != null ? e.getMessage() : "Lỗi kiểm tra tin đăng");
                                return;
                            }
                            WriteBatch batch = db.batch();
                            DocumentReference userRef = db.collection(COLLECTION_USERS).document(uid);
                            batch.update(userRef, "status", UserStatus.DELETED.name(),
                                    "updatedAt", FieldValue.serverTimestamp());

                            if (task.getResult() != null) {
                                for (DocumentSnapshot doc : task.getResult().getDocuments()) {
                                    batch.update(doc.getReference(),
                                            "status", "HIDDEN",
                                            "statusReason", "Tài khoản đã xóa",
                                            "updatedAt", FieldValue.serverTimestamp());
                                }
                            }

                            batch.commit().addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> commitTask) {
                                    if (commitTask.isSuccessful()) {
                                        cb.onSuccess(null);
                                    } else {
                                        Exception e = commitTask.getException();
                                        cb.onError(e != null ? e.getMessage() : "Lỗi xóa tài khoản");
                                    }
                                }
                            });
                        }
                    });
        }
    }

    public void deactivate(final String uid, final DataCallback<Void> cb) {
        new DeactivateHandler(db, uid, cb).run();
    }
}
