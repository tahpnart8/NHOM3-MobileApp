package com.nhom3.pubgapp.data;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.WriteBatch;
import com.nhom3.pubgapp.model.ActivityLog;
import com.nhom3.pubgapp.model.AppNotification;
import com.nhom3.pubgapp.model.BannedKeyword;
import com.nhom3.pubgapp.model.Listing;
import com.nhom3.pubgapp.model.Offer;
import com.nhom3.pubgapp.model.SellerVerification;
import com.nhom3.pubgapp.model.User;
import com.nhom3.pubgapp.model.Warning;
import com.nhom3.pubgapp.model.enums.ListingStatus;
import com.nhom3.pubgapp.model.enums.NotificationType;
import com.nhom3.pubgapp.model.enums.OfferStatus;
import com.nhom3.pubgapp.model.enums.UserStatus;
import com.nhom3.pubgapp.model.enums.VerificationStatus;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

// Everything an admin may do. Rules re-check each write, so the checks here are for clear messages.
public class AdminRepository extends BaseRepository {

    private static final String COLLECTION_USERS = "users";
    private static final String COLLECTION_WARNINGS = "warnings";
    private static final String COLLECTION_LISTINGS = "listings";
    private static final String COLLECTION_OFFERS = "offers";
    private static final String COLLECTION_ADMINS = "admins";
    private static final String COLLECTION_VERIFICATIONS = "sellerVerifications";
    private static final String COLLECTION_ACTIVITY_LOGS = "activityLogs";
    private static final String COLLECTION_BANNED_KEYWORDS = "bannedKeywords";

    private static final String FIELD_UID = "uid";
    private static final String FIELD_USER_ID = "userId";
    private static final String FIELD_SELLER_ID = "sellerId";
    private static final String FIELD_LISTING_ID = "listingId";
    private static final String FIELD_STATUS = "status";
    private static final String FIELD_STATUS_REASON = "statusReason";
    private static final String FIELD_LOCKED_UNTIL = "lockedUntil";
    private static final String FIELD_LOCK_REASON = "lockReason";
    private static final String FIELD_WARNING_COUNT = "warningCount";
    private static final String FIELD_PROVINCE = "province";
    private static final String FIELD_EMAIL = "email";
    private static final String FIELD_VERIFICATION_STATUS = "verificationStatus";
    private static final String FIELD_FLAGGED = "flagged";
    private static final String FIELD_KEYWORD = "keyword";
    private static final String FIELD_CREATED_BY = "createdBy";
    private static final String FIELD_REVIEWED_BY = "reviewedBy";
    private static final String FIELD_REVIEWED_AT = "reviewedAt";
    private static final String FIELD_NOTE = "note";
    private static final String FIELD_LEVEL = "level";
    private static final String FIELD_REASON = "reason";
    private static final String FIELD_ISSUED_BY = "issuedBy";
    private static final String FIELD_TARGET_ID = "targetId";
    private static final String FIELD_CREATED_AT = "createdAt";
    private static final String FIELD_UPDATED_AT = "updatedAt";

    private static final String TARGET_TYPE_USER = "user";
    private static final String TARGET_TYPE_LISTING = "listing";
    private static final String TARGET_TYPE_KEYWORD = "keyword";
    private static final String TARGET_TYPE_ADMIN = "admin";

    private static final String ACTION_USER_LOCKED = "USER_LOCKED";
    private static final String ACTION_USER_UNLOCKED = "USER_UNLOCKED";
    private static final String ACTION_USER_WARNED = "USER_WARNED";
    private static final String ACTION_USER_LISTINGS_REMOVED = "USER_LISTINGS_REMOVED";
    private static final String ACTION_VERIFICATION_APPROVED = "VERIFICATION_APPROVED";
    private static final String ACTION_VERIFICATION_REJECTED = "VERIFICATION_REJECTED";
    private static final String ACTION_LISTING_HIDDEN = "LISTING_HIDDEN";
    private static final String ACTION_LISTING_RESTORED = "LISTING_RESTORED";
    private static final String ACTION_LISTING_REMOVED = "LISTING_REMOVED";
    private static final String ACTION_LISTING_FLAG_CLEARED = "LISTING_FLAG_CLEARED";
    private static final String ACTION_KEYWORD_ADDED = "KEYWORD_ADDED";
    private static final String ACTION_KEYWORD_REMOVED = "KEYWORD_REMOVED";
    private static final String ACTION_ADMIN_GRANTED = "ADMIN_GRANTED";
    private static final String ACTION_ADMIN_REVOKED = "ADMIN_REVOKED";

    private static final int PAGE_SIZE = 50;
    private static final int LOG_LIMIT = 100;
    // One listing write plus two writes per pending offer must stay far below the 500 of a batch.
    private static final int MAX_OFFERS_PER_BATCH = 150;
    private static final int KEYWORD_MIN_LENGTH = 2;
    private static final int KEYWORD_MAX_LENGTH = 60;
    private static final int NOTICE_TITLE_LIMIT = 100;
    private static final int NOTICE_BODY_LIMIT = 300;
    private static final int NOTICE_REASON_LIMIT = 200;

    private static final Pattern COMBINING_MARKS = Pattern.compile("\\p{M}+");
    private static final Pattern WHITESPACE_RUN = Pattern.compile("\\s+");

    private static final String MESSAGE_NOT_SIGNED_IN =
            "Bạn cần đăng nhập để thực hiện thao tác này.";
    private static final String MESSAGE_ID_REQUIRED = "Thiếu mã của đối tượng cần xử lý.";
    private static final String MESSAGE_REASON_REQUIRED = "Hãy nhập lý do.";
    private static final String MESSAGE_USER_NOT_FOUND = "Không tìm thấy tài khoản này.";
    private static final String MESSAGE_USER_DELETED =
            "Tài khoản này đã bị xóa nên không thể xử lý.";
    private static final String MESSAGE_CANNOT_LOCK =
            "Tài khoản đang ở trạng thái không thể khóa theo cách này.";
    private static final String MESSAGE_LOCK_UNTIL_PAST = "Hạn khóa phải nằm sau thời điểm hiện tại.";
    private static final String MESSAGE_NOT_LOCKED = "Tài khoản này đang không bị khóa.";
    private static final String MESSAGE_WARN_NEEDS_UNTIL =
            "Cảnh báo bậc 2 khóa tạm tài khoản, hãy chọn hạn khóa sau thời điểm hiện tại.";
    private static final String MESSAGE_LISTING_NOT_FOUND = "Không tìm thấy tin đăng này.";
    private static final String MESSAGE_HIDE_ONLY_AVAILABLE = "Chỉ ẩn được tin đang hiển thị.";
    private static final String MESSAGE_RESTORE_ONLY_ADMIN_HIDDEN =
            "Chỉ khôi phục được tin do quản trị viên ẩn.";
    private static final String MESSAGE_OWNER_DELETED =
            "Chủ tin đã xóa tài khoản nên không khôi phục được tin.";
    private static final String MESSAGE_REMOVE_STATUS = "Không gỡ được tin ở trạng thái này.";
    private static final String MESSAGE_TOO_MANY_OFFERS =
            "Tin có quá nhiều đề nghị giá đang chờ, không xử lý được trong một lần."
                    + " Hãy báo cho nhóm phát triển.";
    private static final String MESSAGE_REMOVE_NEEDS_LOCK =
            "Chỉ gỡ hàng loạt tin của tài khoản đang bị khóa.";
    private static final String DEFAULT_REMOVAL_REASON = "Tài khoản bị khóa";
    private static final String MESSAGE_REMOVE_PARTIAL =
            "Đã gỡ %d trong %d tin, còn %d tin bị lỗi. Hãy thử lại.";
    private static final String MESSAGE_REJECT_NOTE_REQUIRED = "Hãy nhập lý do từ chối.";
    private static final String MESSAGE_VERIFICATION_NOT_FOUND = "Không tìm thấy yêu cầu xác thực.";
    private static final String MESSAGE_VERIFICATION_DONE = "Yêu cầu này đã được xử lý.";
    private static final String MESSAGE_KEYWORD_EMPTY = "Hãy nhập từ khóa.";
    private static final String MESSAGE_KEYWORD_LENGTH = "Từ khóa phải dài từ %d đến %d ký tự.";
    private static final String MESSAGE_KEYWORD_EXISTS = "Từ khóa đã có trong danh sách.";
    private static final String MESSAGE_EMAIL_REQUIRED = "Hãy nhập email cần tìm.";
    private static final String MESSAGE_EMAIL_NOT_FOUND =
            "Không có tài khoản đang hoạt động nào dùng email này.";
    private static final String MESSAGE_ADMIN_SELF_GRANT = "Bạn không thể tự cấp quyền cho chính mình.";
    private static final String MESSAGE_ALREADY_ADMIN = "Tài khoản này đã là quản trị viên.";
    private static final String MESSAGE_ADMIN_SELF_REVOKE =
            "Bạn không thể tự gỡ quyền quản trị của chính mình.";
    private static final String MESSAGE_NOT_ADMIN = "Tài khoản này không phải quản trị viên.";
    private static final String MESSAGE_LAST_ADMIN = "Không thể gỡ quản trị viên cuối cùng.";

    private static final String NOTICE_LOCK_TITLE = "Tài khoản của bạn đã bị khóa";
    private static final String NOTICE_LOCK_BODY = "Lý do: %s";
    private static final String NOTICE_WARN_TITLE = "Tài khoản của bạn bị cảnh báo";
    private static final String NOTICE_WARN_BODY = "Cảnh báo bậc %d. Lý do: %s";
    private static final String NOTICE_VERIFIED_TITLE = "Tài khoản đã được xác thực";
    private static final String NOTICE_VERIFIED_BODY = "Bạn có thể đăng tin và gửi đề nghị giá.";
    private static final String NOTICE_VERIFY_REJECTED_TITLE = "Yêu cầu xác thực bị từ chối";
    private static final String NOTICE_VERIFY_REJECTED_BODY = "Lý do: %s. Hãy sửa thông tin và gửi lại.";
    private static final String NOTICE_HIDDEN_TITLE = "Tin đăng của bạn đã bị ẩn";
    private static final String NOTICE_RESTORED_TITLE = "Tin đăng của bạn đã được khôi phục";
    private static final String NOTICE_RESTORED_BODY = "Tin đăng của bạn hiển thị trở lại.";
    private static final String NOTICE_REMOVED_TITLE = "Tin đăng của bạn đã bị gỡ";
    private static final String NOTICE_LISTING_BODY = "Lý do: %s";
    private static final String NOTICE_OFFER_TITLE = "Đề nghị giá bị từ chối";
    private static final String NOTICE_OFFER_BODY =
            "Tin đăng không còn nhận đề nghị nên đề nghị giá của bạn đã bị từ chối.";

    private final NotificationRepository notificationRepository = new NotificationRepository();

    public void isAdmin(String uid, final DataCallback<Boolean> cb) {
        if (isBlank(uid)) {
            cb.onSuccess(false);
            return;
        }
        db.collection(COLLECTION_ADMINS).document(uid).get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot snapshot) {
                        cb.onSuccess(snapshot.exists());
                    }
                })
                .addOnFailureListener(failureListener(cb));
    }

    public void listUsers(UserStatus status, Timestamp from, Timestamp to,
            String province, final String keyword, final DataCallback<List<User>> cb) {
        Query query = db.collection(COLLECTION_USERS);
        if (status != null) {
            query = query.whereEqualTo(FIELD_STATUS, status.name());
        }
        if (from != null) {
            query = query.whereGreaterThanOrEqualTo(FIELD_CREATED_AT, from);
        }
        if (to != null) {
            query = query.whereLessThanOrEqualTo(FIELD_CREATED_AT, to);
        }
        if (!isBlank(province)) {
            query = query.whereEqualTo(FIELD_PROVINCE, province.trim());
        }
        query = query.orderBy(FIELD_CREATED_AT, Query.Direction.DESCENDING).limit(PAGE_SIZE);
        query.get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot snapshot) {
                        List<User> users = snapshot.toObjects(User.class);
                        cb.onSuccess(filterUsersByKeyword(users, keyword));
                    }
                })
                .addOnFailureListener(failureListener(cb));
    }

    public void lockUser(final String id, final Timestamp until, final String reason,
            final DataCallback<Void> cb) {
        if (isBlank(reason)) {
            cb.onError(MESSAGE_REASON_REQUIRED);
            return;
        }
        if (until != null && !isInFuture(until)) {
            cb.onError(MESSAGE_LOCK_UNTIL_PAST);
            return;
        }
        getUserById(id, new StepCallback<User>(cb) {
            @Override
            public void onSuccess(User user) {
                applyLock(id, user, until, reason.trim(), cb);
            }
        });
    }

    public void unlockUser(final String id, final DataCallback<Void> cb) {
        getUserById(id, new StepCallback<User>(cb) {
            @Override
            public void onSuccess(User user) {
                if (!isLocked(user.getStatus())) {
                    cb.onError(MESSAGE_NOT_LOCKED);
                    return;
                }
                PendingWrite write = new PendingWrite(db.batch(), cb);
                write.logAs(ACTION_USER_UNLOCKED, TARGET_TYPE_USER, id);
                write.batch.update(userRef(id), lockFields(UserStatus.ACTIVE, null, ""));
                send(write);
            }
        });
    }

    public void warnUser(final String id, final Warning warning, final Timestamp until,
            final DataCallback<Void> cb) {
        if (warning == null || isBlank(warning.getReason())) {
            cb.onError(MESSAGE_REASON_REQUIRED);
            return;
        }
        final String adminId = currentUserId();
        if (adminId.isEmpty()) {
            cb.onError(MESSAGE_NOT_SIGNED_IN);
            return;
        }
        getUserById(id, new StepCallback<User>(cb) {
            @Override
            public void onSuccess(User user) {
                applyWarning(id, user, warning, until, adminId, cb);
            }
        });
    }

    public void getWarnings(String id, final DataCallback<List<Warning>> cb) {
        if (isBlank(id)) {
            cb.onError(MESSAGE_ID_REQUIRED);
            return;
        }
        userRef(id).collection(COLLECTION_WARNINGS)
                .orderBy(FIELD_CREATED_AT, Query.Direction.DESCENDING).get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot snapshot) {
                        cb.onSuccess(snapshot.toObjects(Warning.class));
                    }
                })
                .addOnFailureListener(failureListener(cb));
    }

    public void getVerificationRequests(final DataCallback<List<SellerVerification>> cb) {
        db.collection(COLLECTION_VERIFICATIONS)
                .whereEqualTo(FIELD_STATUS, VerificationStatus.PENDING.name())
                .orderBy(FIELD_CREATED_AT, Query.Direction.ASCENDING).get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot snapshot) {
                        cb.onSuccess(snapshot.toObjects(SellerVerification.class));
                    }
                })
                .addOnFailureListener(failureListener(cb));
    }

    public void reviewVerification(final String id, final boolean approve, final String note,
            final DataCallback<Void> cb) {
        final String cleanNote = note == null ? "" : note.trim();
        if (!approve && cleanNote.isEmpty()) {
            cb.onError(MESSAGE_REJECT_NOTE_REQUIRED);
            return;
        }
        final String adminId = currentUserId();
        if (adminId.isEmpty()) {
            cb.onError(MESSAGE_NOT_SIGNED_IN);
            return;
        }
        if (isBlank(id)) {
            cb.onError(MESSAGE_ID_REQUIRED);
            return;
        }
        db.collection(COLLECTION_VERIFICATIONS).document(id).get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot snapshot) {
                        SellerVerification request = snapshot.toObject(SellerVerification.class);
                        reviewLoadedRequest(id, request, approve, cleanNote, adminId, cb);
                    }
                })
                .addOnFailureListener(failureListener(cb));
    }

    public void getActivityLogs(String id, final DataCallback<List<ActivityLog>> cb) {
        if (isBlank(id)) {
            cb.onError(MESSAGE_ID_REQUIRED);
            return;
        }
        db.collection(COLLECTION_ACTIVITY_LOGS).whereEqualTo(FIELD_USER_ID, id)
                .orderBy(FIELD_CREATED_AT, Query.Direction.DESCENDING).limit(LOG_LIMIT).get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot snapshot) {
                        cb.onSuccess(snapshot.toObjects(ActivityLog.class));
                    }
                })
                .addOnFailureListener(failureListener(cb));
    }

    public void hideListing(String id, final String reason, final DataCallback<Void> cb) {
        if (isBlank(reason)) {
            cb.onError(MESSAGE_REASON_REQUIRED);
            return;
        }
        getListingById(id, new StepCallback<Listing>(cb) {
            @Override
            public void onSuccess(Listing listing) {
                if (listing.getStatus() != ListingStatus.AVAILABLE) {
                    cb.onError(MESSAGE_HIDE_ONLY_AVAILABLE);
                    return;
                }
                listing.hide(reason.trim());
                PendingWrite write = new PendingWrite(db.batch(), cb);
                write.logAs(ACTION_LISTING_HIDDEN, TARGET_TYPE_LISTING, listing.getId());
                write.notices.add(listingNotice(listing, NOTICE_HIDDEN_TITLE,
                        String.format(Locale.ROOT, NOTICE_LISTING_BODY, shorten(reason.trim()))));
                writeListingChange(write, listing, true);
            }
        });
    }

    public void restoreListing(String id, final DataCallback<Void> cb) {
        getListingById(id, new StepCallback<Listing>(cb) {
            @Override
            public void onSuccess(final Listing listing) {
                boolean hiddenByAdmin = listing.getStatus() == ListingStatus.HIDDEN
                        && !isBlank(listing.getStatusReason());
                if (!hiddenByAdmin) {
                    cb.onError(MESSAGE_RESTORE_ONLY_ADMIN_HIDDEN);
                    return;
                }
                getUserById(listing.getSellerId(), new StepCallback<User>(cb) {
                    @Override
                    public void onSuccess(User owner) {
                        restoreForOwner(listing, owner, cb);
                    }
                });
            }
        });
    }

    public void removeListing(String id, final String reason, final DataCallback<Void> cb) {
        if (isBlank(reason)) {
            cb.onError(MESSAGE_REASON_REQUIRED);
            return;
        }
        getListingById(id, new StepCallback<Listing>(cb) {
            @Override
            public void onSuccess(Listing listing) {
                if (!isRemovable(listing.getStatus())) {
                    cb.onError(MESSAGE_REMOVE_STATUS);
                    return;
                }
                boolean wasAvailable = listing.getStatus() == ListingStatus.AVAILABLE;
                listing.remove(reason.trim());
                PendingWrite write = new PendingWrite(db.batch(), cb);
                write.logAs(ACTION_LISTING_REMOVED, TARGET_TYPE_LISTING, listing.getId());
                write.notices.add(listingNotice(listing, NOTICE_REMOVED_TITLE,
                        String.format(Locale.ROOT, NOTICE_LISTING_BODY, shorten(reason.trim()))));
                writeListingChange(write, listing, wasAvailable);
            }
        });
    }

    public void removeAllListingsOf(final String userId, final DataCallback<Void> cb) {
        getUserById(userId, new StepCallback<User>(cb) {
            @Override
            public void onSuccess(User user) {
                if (!isLocked(user.getStatus())) {
                    cb.onError(MESSAGE_REMOVE_NEEDS_LOCK);
                    return;
                }
                loadRemovableListings(userId, user.getLockReason(), cb);
            }
        });
    }

    public void getKeywords(final DataCallback<List<BannedKeyword>> cb) {
        db.collection(COLLECTION_BANNED_KEYWORDS).get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot snapshot) {
                        cb.onSuccess(snapshot.toObjects(BannedKeyword.class));
                    }
                })
                .addOnFailureListener(failureListener(cb));
    }

    public void addKeyword(String word, final DataCallback<Void> cb) {
        final String normalized = normalize(word);
        if (normalized.isEmpty()) {
            cb.onError(MESSAGE_KEYWORD_EMPTY);
            return;
        }
        if (normalized.length() < KEYWORD_MIN_LENGTH || normalized.length() > KEYWORD_MAX_LENGTH) {
            cb.onError(String.format(Locale.ROOT, MESSAGE_KEYWORD_LENGTH,
                    KEYWORD_MIN_LENGTH, KEYWORD_MAX_LENGTH));
            return;
        }
        final String adminId = currentUserId();
        if (adminId.isEmpty()) {
            cb.onError(MESSAGE_NOT_SIGNED_IN);
            return;
        }
        db.collection(COLLECTION_BANNED_KEYWORDS).whereEqualTo(FIELD_KEYWORD, normalized).limit(1).get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot snapshot) {
                        if (!snapshot.isEmpty()) {
                            cb.onError(MESSAGE_KEYWORD_EXISTS);
                            return;
                        }
                        createKeyword(normalized, adminId, cb);
                    }
                })
                .addOnFailureListener(failureListener(cb));
    }

    public void removeKeyword(final String id, final DataCallback<Void> cb) {
        if (isBlank(id)) {
            cb.onError(MESSAGE_ID_REQUIRED);
            return;
        }
        db.collection(COLLECTION_BANNED_KEYWORDS).document(id).delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        logActivity(ACTION_KEYWORD_REMOVED, TARGET_TYPE_KEYWORD, id);
                        cb.onSuccess(null);
                    }
                })
                .addOnFailureListener(failureListener(cb));
    }

    public void clearFlag(final String listingId, final DataCallback<Void> cb) {
        if (isBlank(listingId)) {
            cb.onError(MESSAGE_ID_REQUIRED);
            return;
        }
        Map<String, Object> fields = new HashMap<String, Object>();
        fields.put(FIELD_FLAGGED, false);
        fields.put(FIELD_UPDATED_AT, FieldValue.serverTimestamp());
        listingRef(listingId).update(fields)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        logActivity(ACTION_LISTING_FLAG_CLEARED, TARGET_TYPE_LISTING, listingId);
                        cb.onSuccess(null);
                    }
                })
                .addOnFailureListener(failureListener(cb));
    }

    public void getFlaggedListings(final DataCallback<List<Listing>> cb) {
        db.collection(COLLECTION_LISTINGS)
                .whereEqualTo(FIELD_FLAGGED, true)
                .whereEqualTo(FIELD_STATUS, ListingStatus.AVAILABLE.name()).get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot snapshot) {
                        List<Listing> listings = snapshot.toObjects(Listing.class);
                        sortNewestFirst(listings);
                        cb.onSuccess(listings);
                    }
                })
                .addOnFailureListener(failureListener(cb));
    }

    public void listAdmins(final DataCallback<List<User>> cb) {
        db.collection(COLLECTION_ADMINS).get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot snapshot) {
                        List<DocumentSnapshot> documents = new ArrayList<DocumentSnapshot>(
                                snapshot.getDocuments());
                        sortOldestFirst(documents);
                        List<String> ids = new ArrayList<String>();
                        for (DocumentSnapshot document : documents) {
                            ids.add(document.getId());
                        }
                        loadAdminProfiles(ids, 0, new ArrayList<User>(), cb);
                    }
                })
                .addOnFailureListener(failureListener(cb));
    }

    public void findUserByEmail(String email, final DataCallback<User> cb) {
        if (isBlank(email)) {
            cb.onError(MESSAGE_EMAIL_REQUIRED);
            return;
        }
        db.collection(COLLECTION_USERS)
                .whereEqualTo(FIELD_EMAIL, email.trim().toLowerCase(Locale.ROOT))
                .whereEqualTo(FIELD_STATUS, UserStatus.ACTIVE.name()).limit(1).get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot snapshot) {
                        if (snapshot.isEmpty()) {
                            cb.onError(MESSAGE_EMAIL_NOT_FOUND);
                            return;
                        }
                        cb.onSuccess(snapshot.getDocuments().get(0).toObject(User.class));
                    }
                })
                .addOnFailureListener(failureListener(cb));
    }

    public void grantAdmin(final String userId, final DataCallback<Void> cb) {
        String adminId = currentUserId();
        if (adminId.isEmpty()) {
            cb.onError(MESSAGE_NOT_SIGNED_IN);
            return;
        }
        if (isBlank(userId)) {
            cb.onError(MESSAGE_ID_REQUIRED);
            return;
        }
        if (userId.equals(adminId)) {
            cb.onError(MESSAGE_ADMIN_SELF_GRANT);
            return;
        }
        db.collection(COLLECTION_ADMINS).document(userId).get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot snapshot) {
                        if (snapshot.exists()) {
                            cb.onError(MESSAGE_ALREADY_ADMIN);
                            return;
                        }
                        createAdmin(userId, cb);
                    }
                })
                .addOnFailureListener(failureListener(cb));
    }

    public void revokeAdmin(final String userId, final DataCallback<Void> cb) {
        String adminId = currentUserId();
        if (adminId.isEmpty()) {
            cb.onError(MESSAGE_NOT_SIGNED_IN);
            return;
        }
        if (isBlank(userId)) {
            cb.onError(MESSAGE_ID_REQUIRED);
            return;
        }
        if (userId.equals(adminId)) {
            cb.onError(MESSAGE_ADMIN_SELF_REVOKE);
            return;
        }
        db.collection(COLLECTION_ADMINS).get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot snapshot) {
                        deleteAdminIfAllowed(userId, snapshot.getDocuments(), cb);
                    }
                })
                .addOnFailureListener(failureListener(cb));
    }


    private void applyLock(String id, User user, Timestamp until, String reason, DataCallback<Void> cb) {
        UserStatus current = user.getStatus();
        if (current == UserStatus.DELETED) {
            cb.onError(MESSAGE_USER_DELETED);
            return;
        }
        UserStatus target = until == null ? UserStatus.PERM_LOCKED : UserStatus.TEMP_LOCKED;
        if (!canLock(current, target)) {
            cb.onError(MESSAGE_CANNOT_LOCK);
            return;
        }
        PendingWrite write = new PendingWrite(db.batch(), cb);
        write.logAs(ACTION_USER_LOCKED, TARGET_TYPE_USER, id);
        write.batch.update(userRef(id), lockFields(target, until, reason));
        write.notices.add(buildNotice(id, NotificationType.WARNING, NOTICE_LOCK_TITLE,
                String.format(Locale.ROOT, NOTICE_LOCK_BODY, shorten(reason)), id));
        send(write);
    }

    private void applyWarning(String id, User user, Warning warning, Timestamp until,
            String adminId, DataCallback<Void> cb) {
        UserStatus current = user.getStatus();
        if (current == UserStatus.DELETED) {
            cb.onError(MESSAGE_USER_DELETED);
            return;
        }
        int level = user.getWarningCount() + 1;
        warning.setUserId(id);
        warning.setLevel(level);
        warning.setIssuedBy(adminId);
        UserStatus next = statusAfterWarning(current, warning.resultingStatus());
        boolean changesStatus = next != current;
        if (changesStatus && next == UserStatus.TEMP_LOCKED && (until == null || !isInFuture(until))) {
            cb.onError(MESSAGE_WARN_NEEDS_UNTIL);
            return;
        }
        Map<String, Object> userFields = new HashMap<String, Object>();
        userFields.put(FIELD_WARNING_COUNT, level);
        userFields.put(FIELD_UPDATED_AT, FieldValue.serverTimestamp());
        if (changesStatus) {
            userFields.putAll(lockFields(next, until, warning.getReason().trim()));
        }
        PendingWrite write = new PendingWrite(db.batch(), cb);
        write.logAs(ACTION_USER_WARNED, TARGET_TYPE_USER, id);
        write.batch.update(userRef(id), userFields);
        write.batch.set(userRef(id).collection(COLLECTION_WARNINGS).document(), warningFields(warning));
        write.notices.add(buildNotice(id, NotificationType.WARNING, NOTICE_WARN_TITLE,
                String.format(Locale.ROOT, NOTICE_WARN_BODY, level, shorten(warning.getReason().trim())), id));
        send(write);
    }

    // A warning never lowers a lock: only a stricter lock replaces the current status.
    private UserStatus statusAfterWarning(UserStatus current, UserStatus wanted) {
        if (wanted == UserStatus.TEMP_LOCKED && current == UserStatus.ACTIVE) {
            return UserStatus.TEMP_LOCKED;
        }
        if (wanted == UserStatus.PERM_LOCKED
                && (current == UserStatus.ACTIVE || current == UserStatus.TEMP_LOCKED)) {
            return UserStatus.PERM_LOCKED;
        }
        return current;
    }

    private boolean canLock(UserStatus current, UserStatus target) {
        if (target == UserStatus.TEMP_LOCKED) {
            return current == UserStatus.ACTIVE;
        }
        return current == UserStatus.ACTIVE || current == UserStatus.TEMP_LOCKED;
    }

    private boolean isLocked(UserStatus status) {
        return status == UserStatus.TEMP_LOCKED || status == UserStatus.PERM_LOCKED;
    }

    // Keys match the "lock, unlock, warn" row of the users Rules exactly.
    private Map<String, Object> lockFields(UserStatus status, Timestamp until, String reason) {
        Map<String, Object> fields = new HashMap<String, Object>();
        fields.put(FIELD_STATUS, status.name());
        fields.put(FIELD_LOCKED_UNTIL, status == UserStatus.TEMP_LOCKED ? until : null);
        fields.put(FIELD_LOCK_REASON, reason);
        fields.put(FIELD_UPDATED_AT, FieldValue.serverTimestamp());
        return fields;
    }

    private Map<String, Object> warningFields(Warning warning) {
        Map<String, Object> fields = new HashMap<String, Object>();
        fields.put(FIELD_USER_ID, warning.getUserId());
        fields.put(FIELD_LEVEL, warning.getLevel());
        fields.put(FIELD_REASON, warning.getReason().trim());
        fields.put(FIELD_ISSUED_BY, warning.getIssuedBy());
        fields.put(FIELD_TARGET_ID, warning.getTargetId() == null ? "" : warning.getTargetId());
        fields.put(FIELD_CREATED_AT, FieldValue.serverTimestamp());
        return fields;
    }

    private List<User> filterUsersByKeyword(List<User> users, String keyword) {
        String needle = normalize(keyword);
        if (needle.isEmpty()) {
            return users;
        }
        List<User> matched = new ArrayList<User>();
        for (User user : users) {
            String haystack = normalize(user.getDisplayName()) + " " + normalize(user.getEmail());
            if (haystack.contains(needle)) {
                matched.add(user);
            }
        }
        return matched;
    }


    private void reviewLoadedRequest(String id, SellerVerification request, boolean approve,
            String note, String adminId, DataCallback<Void> cb) {
        if (request == null) {
            cb.onError(MESSAGE_VERIFICATION_NOT_FOUND);
            return;
        }
        if (request.getStatus() != VerificationStatus.PENDING) {
            cb.onError(MESSAGE_VERIFICATION_DONE);
            return;
        }
        if (approve) {
            request.approve(adminId);
        } else {
            request.reject(adminId, note);
        }
        writeReview(id, request, adminId, cb);
    }

    private void writeReview(final String id, final SellerVerification request, final String adminId,
            final DataCallback<Void> cb) {
        getUserById(id, new StepCallback<User>(cb) {
            @Override
            public void onSuccess(User user) {
                if (user.getStatus() == UserStatus.DELETED) {
                    cb.onError(MESSAGE_USER_DELETED);
                    return;
                }
                sendReview(id, request, adminId, cb);
            }
        });
    }

    private void sendReview(String id, SellerVerification request, String adminId, DataCallback<Void> cb) {
        VerificationStatus result = request.getStatus();
        boolean approved = result == VerificationStatus.APPROVED;
        Map<String, Object> requestFields = new HashMap<String, Object>();
        requestFields.put(FIELD_STATUS, result.name());
        requestFields.put(FIELD_REVIEWED_BY, adminId);
        requestFields.put(FIELD_REVIEWED_AT, FieldValue.serverTimestamp());
        requestFields.put(FIELD_UPDATED_AT, FieldValue.serverTimestamp());
        Map<String, Object> userFields = new HashMap<String, Object>();
        userFields.put(FIELD_VERIFICATION_STATUS, result.name());
        userFields.put(FIELD_UPDATED_AT, FieldValue.serverTimestamp());
        PendingWrite write = new PendingWrite(db.batch(), cb);
        if (approved) {
            write.logAs(ACTION_VERIFICATION_APPROVED, TARGET_TYPE_USER, id);
            write.notices.add(buildNotice(id, NotificationType.VERIFICATION_RESULT,
                    NOTICE_VERIFIED_TITLE, NOTICE_VERIFIED_BODY, id));
        } else {
            requestFields.put(FIELD_NOTE, request.getNote());
            write.logAs(ACTION_VERIFICATION_REJECTED, TARGET_TYPE_USER, id);
            write.notices.add(buildNotice(id, NotificationType.VERIFICATION_RESULT,
                    NOTICE_VERIFY_REJECTED_TITLE,
                    String.format(Locale.ROOT, NOTICE_VERIFY_REJECTED_BODY, shorten(request.getNote())), id));
        }
        write.batch.update(db.collection(COLLECTION_VERIFICATIONS).document(id), requestFields);
        write.batch.update(userRef(id), userFields);
        send(write);
    }


    private void restoreForOwner(Listing listing, User owner, DataCallback<Void> cb) {
        if (owner.getStatus() == UserStatus.DELETED) {
            cb.onError(MESSAGE_OWNER_DELETED);
            return;
        }
        listing.restore();
        PendingWrite write = new PendingWrite(db.batch(), cb);
        write.logAs(ACTION_LISTING_RESTORED, TARGET_TYPE_LISTING, listing.getId());
        write.notices.add(listingNotice(listing, NOTICE_RESTORED_TITLE, NOTICE_RESTORED_BODY));
        writeListingChange(write, listing, false);
    }

    private boolean isRemovable(ListingStatus status) {
        return status == ListingStatus.DRAFT || status == ListingStatus.AVAILABLE
                || status == ListingStatus.HIDDEN;
    }

    // Writes the state the model method already set; the status is never typed in here (R18).
    private void writeListingChange(PendingWrite write, Listing listing, boolean rejectOffers) {
        Map<String, Object> fields = new HashMap<String, Object>();
        fields.put(FIELD_STATUS, listing.getStatus().name());
        fields.put(FIELD_STATUS_REASON, listing.getStatusReason() == null ? "" : listing.getStatusReason());
        fields.put(FIELD_UPDATED_AT, FieldValue.serverTimestamp());
        write.batch.update(listingRef(listing.getId()), fields);
        if (rejectOffers) {
            rejectPendingOffers(write, listing.getId());
            return;
        }
        send(write);
    }

    private void rejectPendingOffers(final PendingWrite write, String listingId) {
        db.collection(COLLECTION_OFFERS)
                .whereEqualTo(FIELD_LISTING_ID, listingId)
                .whereEqualTo(FIELD_STATUS, OfferStatus.PENDING.name()).get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot snapshot) {
                        if (snapshot.size() > MAX_OFFERS_PER_BATCH) {
                            write.cb.onError(MESSAGE_TOO_MANY_OFFERS);
                            return;
                        }
                        for (DocumentSnapshot document : snapshot.getDocuments()) {
                            addOfferRejection(write, document);
                        }
                        send(write);
                    }
                })
                .addOnFailureListener(failureListener(write.cb));
    }

    private void addOfferRejection(PendingWrite write, DocumentSnapshot document) {
        Offer offer = document.toObject(Offer.class);
        if (offer == null) {
            return;
        }
        offer.reject();
        Map<String, Object> fields = new HashMap<String, Object>();
        fields.put(FIELD_STATUS, offer.getStatus().name());
        fields.put(FIELD_UPDATED_AT, FieldValue.serverTimestamp());
        write.batch.update(document.getReference(), fields);
        write.notices.add(buildNotice(offer.getBuyerId(), NotificationType.OFFER_RESULT,
                NOTICE_OFFER_TITLE, NOTICE_OFFER_BODY, offer.getConversationId()));
    }

    private AppNotification listingNotice(Listing listing, String title, String body) {
        return buildNotice(listing.getSellerId(), NotificationType.LISTING_MODERATED, title, body,
                listing.getId());
    }

    private void loadRemovableListings(final String userId, final String lockReason,
            final DataCallback<Void> cb) {
        List<String> statuses = new ArrayList<String>();
        statuses.add(ListingStatus.DRAFT.name());
        statuses.add(ListingStatus.AVAILABLE.name());
        statuses.add(ListingStatus.HIDDEN.name());
        db.collection(COLLECTION_LISTINGS)
                .whereEqualTo(FIELD_SELLER_ID, userId)
                .whereIn(FIELD_STATUS, statuses).get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot snapshot) {
                        List<Listing> listings = snapshot.toObjects(Listing.class);
                        String reason = isBlank(lockReason) ? DEFAULT_REMOVAL_REASON : lockReason.trim();
                        removeNext(new RemovalRun(userId, reason, listings, cb), 0);
                    }
                })
                .addOnFailureListener(failureListener(cb));
    }

    // One batch per listing keeps every batch inside the Rules limit on document reads.
    private void removeNext(final RemovalRun run, final int index) {
        if (index >= run.listings.size()) {
            finishRemoval(run);
            return;
        }
        Listing listing = run.listings.get(index);
        boolean wasAvailable = listing.getStatus() == ListingStatus.AVAILABLE;
        listing.remove(run.reason);
        DataCallback<Void> next = new DataCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                removeNext(run, index + 1);
            }

            @Override
            public void onError(String message) {
                run.failures++;
                removeNext(run, index + 1);
            }
        };
        writeListingChange(new PendingWrite(db.batch(), next), listing, wasAvailable);
    }

    private void finishRemoval(RemovalRun run) {
        int total = run.listings.size();
        int removed = total - run.failures;
        if (removed > 0) {
            logActivity(ACTION_USER_LISTINGS_REMOVED, TARGET_TYPE_USER, run.userId);
        }
        if (run.failures > 0) {
            run.cb.onError(String.format(Locale.ROOT, MESSAGE_REMOVE_PARTIAL, removed, total, run.failures));
            return;
        }
        run.cb.onSuccess(null);
    }


    private void createKeyword(String normalized, String adminId, final DataCallback<Void> cb) {
        Map<String, Object> fields = new HashMap<String, Object>();
        fields.put(FIELD_KEYWORD, normalized);
        fields.put(FIELD_CREATED_BY, adminId);
        fields.put(FIELD_CREATED_AT, FieldValue.serverTimestamp());
        db.collection(COLLECTION_BANNED_KEYWORDS).add(fields)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference reference) {
                        logActivity(ACTION_KEYWORD_ADDED, TARGET_TYPE_KEYWORD, reference.getId());
                        cb.onSuccess(null);
                    }
                })
                .addOnFailureListener(failureListener(cb));
    }

    private void createAdmin(final String userId, final DataCallback<Void> cb) {
        Map<String, Object> fields = new HashMap<String, Object>();
        fields.put(FIELD_UID, userId);
        fields.put(FIELD_CREATED_AT, FieldValue.serverTimestamp());
        db.collection(COLLECTION_ADMINS).document(userId).set(fields)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        logActivity(ACTION_ADMIN_GRANTED, TARGET_TYPE_ADMIN, userId);
                        cb.onSuccess(null);
                    }
                })
                .addOnFailureListener(failureListener(cb));
    }

    private void deleteAdminIfAllowed(final String userId, List<DocumentSnapshot> admins,
            final DataCallback<Void> cb) {
        boolean isListed = false;
        for (DocumentSnapshot admin : admins) {
            if (admin.getId().equals(userId)) {
                isListed = true;
            }
        }
        if (!isListed) {
            cb.onError(MESSAGE_NOT_ADMIN);
            return;
        }
        if (admins.size() <= 1) {
            cb.onError(MESSAGE_LAST_ADMIN);
            return;
        }
        db.collection(COLLECTION_ADMINS).document(userId).delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        logActivity(ACTION_ADMIN_REVOKED, TARGET_TYPE_ADMIN, userId);
                        cb.onSuccess(null);
                    }
                })
                .addOnFailureListener(failureListener(cb));
    }

    // One read per admin keeps the code simple; the list is a handful of people.
    private void loadAdminProfiles(final List<String> ids, final int index, final List<User> profiles,
            final DataCallback<List<User>> cb) {
        if (index >= ids.size()) {
            cb.onSuccess(profiles);
            return;
        }
        userRef(ids.get(index)).get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot snapshot) {
                        User profile = snapshot.toObject(User.class);
                        if (profile != null) {
                            profiles.add(profile);
                        }
                        loadAdminProfiles(ids, index + 1, profiles, cb);
                    }
                })
                .addOnFailureListener(failureListener(cb));
    }


    private DocumentReference userRef(String id) {
        return db.collection(COLLECTION_USERS).document(id);
    }

    private DocumentReference listingRef(String id) {
        return db.collection(COLLECTION_LISTINGS).document(id);
    }

    private void getUserById(String id, final DataCallback<User> cb) {
        if (isBlank(id)) {
            cb.onError(MESSAGE_USER_NOT_FOUND);
            return;
        }
        userRef(id).get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot snapshot) {
                        User user = snapshot.toObject(User.class);
                        if (user == null) {
                            cb.onError(MESSAGE_USER_NOT_FOUND);
                            return;
                        }
                        cb.onSuccess(user);
                    }
                })
                .addOnFailureListener(failureListener(cb));
    }

    private void getListingById(String id, final DataCallback<Listing> cb) {
        if (isBlank(id)) {
            cb.onError(MESSAGE_LISTING_NOT_FOUND);
            return;
        }
        listingRef(id).get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot snapshot) {
                        Listing listing = snapshot.toObject(Listing.class);
                        if (listing == null) {
                            cb.onError(MESSAGE_LISTING_NOT_FOUND);
                            return;
                        }
                        cb.onSuccess(listing);
                    }
                })
                .addOnFailureListener(failureListener(cb));
    }

    private OnFailureListener failureListener(final DataCallback<?> cb) {
        return new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception error) {
                fail(cb, error);
            }
        };
    }

    // Each notice is added to the batch by NotificationRepository.create; the batch is committed
    // only after every notice is in, so a notice can never be lost silently.
    private void send(PendingWrite write) {
        addNotice(write, 0);
    }

    private void addNotice(final PendingWrite write, final int index) {
        if (index >= write.notices.size()) {
            commit(write);
            return;
        }
        // The rules forbid notifying yourself, and the demo listings belong to the admin, so skip that notice.
        if (isNoticeForMe(write.notices.get(index))) {
            addNotice(write, index + 1);
            return;
        }
        notificationRepository.create(write.notices.get(index), write.batch,
                new StepCallback<Void>(write.cb) {
                    @Override
                    public void onSuccess(Void result) {
                        addNotice(write, index + 1);
                    }
                });
    }

    private boolean isNoticeForMe(AppNotification notice) {
        String myUid = currentUserId();
        return !myUid.isEmpty() && myUid.equals(notice.getRecipientId());
    }

    private void commit(final PendingWrite write) {
        write.batch.commit()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        if (write.logAction != null) {
                            logActivity(write.logAction, write.logTargetType, write.logTargetId);
                        }
                        write.cb.onSuccess(null);
                    }
                })
                .addOnFailureListener(failureListener(write.cb));
    }

    private AppNotification buildNotice(String recipientId, NotificationType type, String title,
            String body, String targetId) {
        AppNotification notice = new AppNotification();
        notice.setRecipientId(recipientId);
        notice.setType(type);
        notice.setTitle(truncate(title, NOTICE_TITLE_LIMIT));
        notice.setBody(truncate(body, NOTICE_BODY_LIMIT));
        notice.setTargetId(targetId);
        notice.setRead(false);
        return notice;
    }

    private String shorten(String reason) {
        return truncate(reason, NOTICE_REASON_LIMIT);
    }

    private String truncate(String text, int limit) {
        if (text == null) {
            return "";
        }
        if (text.length() <= limit) {
            return text;
        }
        return text.substring(0, limit);
    }

    private boolean isBlank(String text) {
        return text == null || text.trim().isEmpty();
    }

    private boolean isInFuture(Timestamp time) {
        return time.compareTo(Timestamp.now()) > 0;
    }

    // Own copy of the accent stripping: FormatUtils is another module's file and not filled yet.
    private String normalize(String text) {
        if (text == null) {
            return "";
        }
        String decomposed = Normalizer.normalize(text, Normalizer.Form.NFD);
        String withoutMarks = COMBINING_MARKS.matcher(decomposed).replaceAll("");
        String lower = withoutMarks.toLowerCase(Locale.ROOT).replace('đ', 'd');
        return WHITESPACE_RUN.matcher(lower.trim()).replaceAll(" ");
    }

    private void sortNewestFirst(List<Listing> listings) {
        Collections.sort(listings, new Comparator<Listing>() {
            @Override
            public int compare(Listing first, Listing second) {
                return compareTimes(second.getCreatedAt(), first.getCreatedAt());
            }
        });
    }

    private void sortOldestFirst(List<DocumentSnapshot> documents) {
        Collections.sort(documents, new Comparator<DocumentSnapshot>() {
            @Override
            public int compare(DocumentSnapshot first, DocumentSnapshot second) {
                return compareTimes(first.getTimestamp(FIELD_CREATED_AT),
                        second.getTimestamp(FIELD_CREATED_AT));
            }
        });
    }

    // A missing time sorts last.
    private int compareTimes(Timestamp first, Timestamp second) {
        if (first == null && second == null) {
            return 0;
        }
        if (first == null) {
            return 1;
        }
        if (second == null) {
            return -1;
        }
        return first.compareTo(second);
    }

    // Forwards a failed step to the caller's callback; subclasses only handle success.
    private abstract static class StepCallback<T> implements DataCallback<T> {
        private final DataCallback<?> failureTarget;

        StepCallback(DataCallback<?> failureTarget) {
            this.failureTarget = failureTarget;
        }

        @Override
        public void onError(String message) {
            failureTarget.onError(message);
        }
    }

    // A batch with the notices still to add and the activity log to write after it commits.
    private static class PendingWrite {
        final WriteBatch batch;
        final DataCallback<Void> cb;
        final List<AppNotification> notices = new ArrayList<AppNotification>();
        String logAction;
        String logTargetType;
        String logTargetId;

        PendingWrite(WriteBatch batch, DataCallback<Void> cb) {
            this.batch = batch;
            this.cb = cb;
        }

        void logAs(String action, String targetType, String targetId) {
            this.logAction = action;
            this.logTargetType = targetType;
            this.logTargetId = targetId;
        }
    }

    // State of removing every listing of one locked account, one listing after another.
    private static class RemovalRun {
        final String userId;
        final String reason;
        final List<Listing> listings;
        final DataCallback<Void> cb;
        int failures;

        RemovalRun(String userId, String reason, List<Listing> listings, DataCallback<Void> cb) {
            this.userId = userId;
            this.reason = reason;
            this.listings = listings;
            this.cb = cb;
        }
    }
}
