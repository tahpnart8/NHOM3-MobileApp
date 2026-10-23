package com.nhom3.pubgapp.data;

import com.google.firebase.Timestamp;
import com.nhom3.pubgapp.model.ActivityLog;
import com.nhom3.pubgapp.model.BannedKeyword;
import com.nhom3.pubgapp.model.Listing;
import com.nhom3.pubgapp.model.SellerVerification;
import com.nhom3.pubgapp.model.User;
import com.nhom3.pubgapp.model.Warning;
import com.nhom3.pubgapp.model.enums.UserStatus;
import java.util.List;

// Skeleton: the module owner fills each body as the issue assigns it.
public class AdminRepository extends BaseRepository {

    public void isAdmin(String uid, DataCallback<Boolean> cb) {
        cb.onError(DataCallback.NOT_READY);
    }

    public void listUsers(UserStatus status, Timestamp from, Timestamp to,
            String province, String keyword, DataCallback<List<User>> cb) {
        cb.onError(DataCallback.NOT_READY);
    }

    public void lockUser(String id, Timestamp until, String reason, DataCallback<Void> cb) {
        cb.onError(DataCallback.NOT_READY);
    }

    public void unlockUser(String id, DataCallback<Void> cb) {
        cb.onError(DataCallback.NOT_READY);
    }

    public void warnUser(String id, Warning warning, Timestamp until, DataCallback<Void> cb) {
        cb.onError(DataCallback.NOT_READY);
    }

    public void getWarnings(String id, DataCallback<List<Warning>> cb) {
        cb.onError(DataCallback.NOT_READY);
    }

    public void getVerificationRequests(DataCallback<List<SellerVerification>> cb) {
        cb.onError(DataCallback.NOT_READY);
    }

    public void reviewVerification(String id, boolean approve, String note, DataCallback<Void> cb) {
        cb.onError(DataCallback.NOT_READY);
    }

    public void getActivityLogs(String id, DataCallback<List<ActivityLog>> cb) {
        cb.onError(DataCallback.NOT_READY);
    }

    public void hideListing(String id, String reason, DataCallback<Void> cb) {
        cb.onError(DataCallback.NOT_READY);
    }

    public void restoreListing(String id, DataCallback<Void> cb) {
        cb.onError(DataCallback.NOT_READY);
    }

    public void removeListing(String id, String reason, DataCallback<Void> cb) {
        cb.onError(DataCallback.NOT_READY);
    }

    public void removeAllListingsOf(String userId, DataCallback<Void> cb) {
        cb.onError(DataCallback.NOT_READY);
    }

    public void getKeywords(DataCallback<List<BannedKeyword>> cb) {
        cb.onError(DataCallback.NOT_READY);
    }

    public void addKeyword(String word, DataCallback<Void> cb) {
        cb.onError(DataCallback.NOT_READY);
    }

    public void removeKeyword(String id, DataCallback<Void> cb) {
        cb.onError(DataCallback.NOT_READY);
    }

    public void clearFlag(String listingId, DataCallback<Void> cb) {
        cb.onError(DataCallback.NOT_READY);
    }

    public void getFlaggedListings(DataCallback<List<Listing>> cb) {
        cb.onError(DataCallback.NOT_READY);
    }

    public void listAdmins(DataCallback<List<User>> cb) {
        cb.onError(DataCallback.NOT_READY);
    }

    public void findUserByEmail(String email, DataCallback<User> cb) {
        cb.onError(DataCallback.NOT_READY);
    }

    public void grantAdmin(String userId, DataCallback<Void> cb) {
        cb.onError(DataCallback.NOT_READY);
    }

    public void revokeAdmin(String userId, DataCallback<Void> cb) {
        cb.onError(DataCallback.NOT_READY);
    }
}
