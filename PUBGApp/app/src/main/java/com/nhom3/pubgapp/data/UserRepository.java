package com.nhom3.pubgapp.data;

import com.nhom3.pubgapp.model.ProfileStats;
import com.nhom3.pubgapp.model.SellerVerification;
import com.nhom3.pubgapp.model.User;
import com.nhom3.pubgapp.model.UserAddress;
import com.nhom3.pubgapp.model.UserIdentity;
import java.util.List;
import java.util.Map;

// Skeleton: the module owner fills each body as the issue assigns it.
public class UserRepository extends BaseRepository {

    public void createProfile(User user, UserIdentity identity, DataCallback<Void> cb) {
        cb.onError(DataCallback.NOT_READY);
    }

    public void getProfile(String uid, DataCallback<User> cb) {
        cb.onError(DataCallback.NOT_READY);
    }

    public void updateProfile(User user, DataCallback<Void> cb) {
        cb.onError(DataCallback.NOT_READY);
    }

    public void searchUsers(String keyword, DataCallback<List<User>> cb) {
        cb.onError(DataCallback.NOT_READY);
    }

    public void getStats(String uid, DataCallback<ProfileStats> cb) {
        cb.onError(DataCallback.NOT_READY);
    }

    public void clearExpiredLock(String uid, DataCallback<Void> cb) {
        cb.onError(DataCallback.NOT_READY);
    }

    public void saveAddress(String uid, UserAddress address, DataCallback<Void> cb) {
        cb.onError(DataCallback.NOT_READY);
    }

    public void deleteAddress(String uid, String id, DataCallback<Void> cb) {
        cb.onError(DataCallback.NOT_READY);
    }

    public void getAddresses(String uid, DataCallback<List<UserAddress>> cb) {
        cb.onError(DataCallback.NOT_READY);
    }

    public void follow(String uid, String otherId, DataCallback<Void> cb) {
        cb.onError(DataCallback.NOT_READY);
    }

    public void unfollow(String uid, String otherId, DataCallback<Void> cb) {
        cb.onError(DataCallback.NOT_READY);
    }

    public void getFollowingIds(String uid, DataCallback<List<String>> cb) {
        cb.onError(DataCallback.NOT_READY);
    }

    public void saveListing(String uid, String listingId, DataCallback<Void> cb) {
        cb.onError(DataCallback.NOT_READY);
    }

    public void unsaveListing(String uid, String listingId, DataCallback<Void> cb) {
        cb.onError(DataCallback.NOT_READY);
    }

    public void getSavedIds(String uid, DataCallback<List<String>> cb) {
        cb.onError(DataCallback.NOT_READY);
    }

    public void requestVerification(String uid, SellerVerification verification, DataCallback<Void> cb) {
        cb.onError(DataCallback.NOT_READY);
    }

    public void getVerification(String uid, DataCallback<SellerVerification> cb) {
        cb.onError(DataCallback.NOT_READY);
    }

    public void saveIdentity(String uid, UserIdentity identity, DataCallback<Void> cb) {
        cb.onError(DataCallback.NOT_READY);
    }

    public void getIdentity(String uid, DataCallback<UserIdentity> cb) {
        cb.onError(DataCallback.NOT_READY);
    }

    public void updateNotificationPrefs(String uid, Map<String,Boolean> prefs, DataCallback<Void> cb) {
        cb.onError(DataCallback.NOT_READY);
    }

    public void touchLastActive(String uid) {
        throw new UnsupportedOperationException("NOT_READY");
    }

    public void deactivate(String uid, DataCallback<Void> cb) {
        cb.onError(DataCallback.NOT_READY);
    }
}
