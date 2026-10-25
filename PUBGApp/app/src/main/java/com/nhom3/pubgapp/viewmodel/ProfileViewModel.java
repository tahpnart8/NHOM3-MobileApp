package com.nhom3.pubgapp.viewmodel;

import android.net.Uri;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.nhom3.pubgapp.data.AuthRepository;
import com.nhom3.pubgapp.data.DataCallback;
import com.nhom3.pubgapp.data.ListingRepository;
import com.nhom3.pubgapp.data.MediaRepository;
import com.nhom3.pubgapp.data.UserRepository;
import com.nhom3.pubgapp.model.Listing;
import com.nhom3.pubgapp.model.ProfileStats;
import com.nhom3.pubgapp.model.SellerVerification;
import com.nhom3.pubgapp.model.User;
import com.nhom3.pubgapp.model.UserAddress;
import com.nhom3.pubgapp.model.UserIdentity;
import com.nhom3.pubgapp.model.enums.ImagePurpose;
import com.nhom3.pubgapp.model.enums.ListingStatus;
import com.nhom3.pubgapp.model.enums.VerificationStatus;
import com.nhom3.pubgapp.util.NetworkMonitor;
import com.nhom3.pubgapp.util.SessionManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// State for the profile, address, saved-listing and verification screens. Action bodies report NOT_READY until the repositories are filled.
public class ProfileViewModel extends ViewModel {

    private final MutableLiveData<User> profile = new MutableLiveData<>();
    private final MutableLiveData<ProfileStats> stats = new MutableLiveData<>();
    private final MutableLiveData<List<UserAddress>> addresses = new MutableLiveData<>();
    private final MutableLiveData<List<Listing>> savedListings = new MutableLiveData<>();
    private final MutableLiveData<List<Listing>> userListings = new MutableLiveData<>();
    private final MutableLiveData<UserIdentity> identity = new MutableLiveData<>();
    private final MutableLiveData<SellerVerification> verification = new MutableLiveData<>();
    private final MutableLiveData<Boolean> following = new MutableLiveData<>();
    private final MutableLiveData<Boolean> loading = new MutableLiveData<>();
    private final MutableLiveData<String> error = new MutableLiveData<>();
    private final MutableLiveData<Boolean> done = new MutableLiveData<>();

    static final int NATIONAL_ID_LENGTH = 12;
    static final int FORM_OK = 0;
    static final int FORM_MISSING_NATIONAL_ID = 1;
    static final int FORM_BAD_NATIONAL_ID = 2;
    static final int FORM_MISSING_ID_IMAGE = 3;
    static final int FORM_MISSING_PORTRAIT_IMAGE = 4;

    // ViewModels have no Context, so messages are Vietnamese constants, like the repositories.
    private static final String MESSAGE_NOT_SIGNED_IN = "Bạn cần đăng nhập để xem hồ sơ.";
    private static final String MESSAGE_OFFLINE = "Không có kết nối.";
    private static final String MESSAGE_MISSING_NATIONAL_ID = "Hãy nhập số CCCD.";
    private static final String MESSAGE_BAD_NATIONAL_ID = "Số CCCD phải gồm đúng 12 chữ số.";
    private static final String MESSAGE_MISSING_ID_IMAGE = "Hãy chọn ảnh giấy tờ.";
    private static final String MESSAGE_MISSING_PORTRAIT_IMAGE = "Hãy chọn ảnh chân dung cầm giấy tờ.";
    private static final String MESSAGE_EMAIL_NOT_VERIFIED =
            "Hãy xác thực email trước khi gửi yêu cầu xác thực tài khoản.";
    private static final String MESSAGE_ALREADY_PENDING = "Yêu cầu xác thực tài khoản đang chờ duyệt.";
    private static final String MESSAGE_ALREADY_APPROVED = "Tài khoản đã xác thực.";
    private static final String MESSAGE_NO_RESUBMIT_LEFT = "Đã hết số lần gửi lại, hãy liên hệ admin.";
    private static final String MESSAGE_CANNOT_FOLLOW_SELF = "Bạn không thể tự theo dõi chính mình.";
    private static final String MESSAGE_STATS_FAILED = "Không tải được thống kê, bấm Thử lại.";

    private final UserRepository userRepository = new UserRepository();
    private final AuthRepository authRepository = new AuthRepository();
    private final MediaRepository mediaRepository = new MediaRepository();
    private final ListingRepository listingRepository = new ListingRepository();

    // The uid whose profile is in profile, so a rotation does not read users/{uid} again.
    private String loadedUid;
    // The uid of the last read started, so a late answer for a previous account is dropped.
    private String requestedUid;
    // The seller whose listings are in userListings, so a rotation does not query again.
    private String listingsUid;

    public LiveData<User> getProfile() {
        return profile;
    }

    public LiveData<ProfileStats> getStats() {
        return stats;
    }

    public LiveData<List<UserAddress>> getAddresses() {
        return addresses;
    }

    public LiveData<List<Listing>> getSavedListings() {
        return savedListings;
    }

    public LiveData<List<Listing>> getUserListings() {
        return userListings;
    }

    public LiveData<UserIdentity> getIdentity() {
        return identity;
    }

    public LiveData<SellerVerification> getVerification() {
        return verification;
    }

    public LiveData<Boolean> getFollowing() {
        return following;
    }

    public LiveData<Boolean> getLoading() {
        return loading;
    }

    public LiveData<String> getError() {
        return error;
    }

    public LiveData<Boolean> getDone() {
        return done;
    }

    public void loadProfile(String uid) {
        if (uid == null || uid.isEmpty()) {
            error.setValue(MESSAGE_NOT_SIGNED_IN);
            return;
        }
        if (Boolean.TRUE.equals(loading.getValue()) && uid.equals(requestedUid)) {
            return;
        }
        if (!uid.equals(loadedUid)) {
            clearPreviousUser();
            fetchProfile(uid);
            return;
        }
        if (profile.getValue() == null) {
            fetchProfile(uid);
            return;
        }
        // A failed count leaves stats empty, so opening the screen or Retry reads it again.
        if (needsStatsRead(isOwnPage(uid), stats.getValue() != null)) {
            loading.setValue(true);
            fetchStats(uid);
        }
        // A failed read of the follow state keeps the button locked, so opening the screen again retries it.
        if (following.getValue() == null && !isOwnPage(uid)) {
            fetchFollowing(uid);
        }
    }

    public void updateProfile(User user) {
        if (!NetworkMonitor.getInstance().isOnline()) {
            error.setValue(MESSAGE_OFFLINE);
            return;
        }
        loading.setValue(true);
        userRepository.updateProfile(user, new DataCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                profile.postValue(user);
                loading.postValue(false);
                done.postValue(true);
            }

            @Override
            public void onError(String message) {
                loading.postValue(false);
                error.postValue(message);
            }
        });
    }

    public void changeAvatar(Uri uri) {
        User current = profile.getValue();
        if (current == null) {
            error.setValue(MESSAGE_NOT_SIGNED_IN);
            return;
        }
        if (!NetworkMonitor.getInstance().isOnline()) {
            error.setValue(MESSAGE_OFFLINE);
            return;
        }
        loading.setValue(true);
        mediaRepository.makeThumb(uri, new DataCallback<String>() {
            @Override
            public void onSuccess(String base64) {
                saveAvatar(current, base64);
            }

            @Override
            public void onError(String message) {
                loading.postValue(false);
                error.postValue(message);
            }
        });
    }

    public void loadAddresses() {
        error.setValue(DataCallback.NOT_READY);
    }

    public void saveAddress(UserAddress address) {
        error.setValue(DataCallback.NOT_READY);
    }

    public void deleteAddress(String id) {
        error.setValue(DataCallback.NOT_READY);
    }

    // uid is the person being followed; the follower is always the signed-in user.
    public void follow(String uid) {
        String me = followerIdOrReport(uid);
        if (me == null) {
            return;
        }
        loading.setValue(true);
        userRepository.follow(me, uid, new FollowCallback(true));
    }

    public void unfollow(String uid) {
        String me = followerIdOrReport(uid);
        if (me == null) {
            return;
        }
        loading.setValue(true);
        userRepository.unfollow(me, uid, new FollowCallback(false));
    }

    public void loadSaved() {
        String uid = SessionManager.getInstance().getCurrentUserId();
        if (uid == null) {
            error.setValue(MESSAGE_NOT_SIGNED_IN);
            return;
        }
        if (Boolean.TRUE.equals(loading.getValue())) {
            return;
        }
        loading.setValue(true);
        userRepository.getSavedIds(uid, new DataCallback<List<String>>() {
            @Override
            public void onSuccess(List<String> savedIds) {
                fetchSavedListings(savedIds);
            }

            @Override
            public void onError(String message) {
                loading.postValue(false);
                error.postValue(message);
            }
        });
    }

    // The card leaves the list at once and comes back if the write fails.
    public void unsaveListing(String listingId) {
        String uid = SessionManager.getInstance().getCurrentUserId();
        if (uid == null) {
            error.setValue(MESSAGE_NOT_SIGNED_IN);
            return;
        }
        if (!NetworkMonitor.getInstance().isOnline()) {
            error.setValue(MESSAGE_OFFLINE);
            return;
        }
        List<Listing> before = savedListings.getValue();
        savedListings.setValue(withoutListing(before, listingId));
        userRepository.unsaveListing(uid, listingId, new DataCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                done.postValue(true);
            }

            @Override
            public void onError(String message) {
                savedListings.postValue(before);
                error.postValue(message);
            }
        });
    }

    // Keeps AVAILABLE, RESERVED and SOLD as the repository returns them; each screen filters by tab.
    public void loadUserListings(String uid) {
        if (uid == null || uid.isEmpty()) {
            error.setValue(MESSAGE_NOT_SIGNED_IN);
            return;
        }
        if (uid.equals(listingsUid) && userListings.getValue() != null) {
            return;
        }
        listingsUid = uid;
        userListings.setValue(null);
        listingRepository.getBySeller(uid, new DataCallback<List<Listing>>() {
            @Override
            public void onSuccess(List<Listing> result) {
                if (!uid.equals(listingsUid)) {
                    return;
                }
                userListings.postValue(result);
            }

            @Override
            public void onError(String message) {
                error.postValue(message);
            }
        });
    }

    public void requestVerification(String nationalId, List<Uri> imageUris) {
        if (Boolean.TRUE.equals(loading.getValue())) {
            return;
        }
        int formResult = checkVerificationForm(nationalId, hasImageAt(imageUris, 0), hasImageAt(imageUris, 1));
        String formMessage = formErrorMessage(formResult);
        if (formMessage != null) {
            error.setValue(formMessage);
            return;
        }
        String statusMessage = verificationBlockMessage();
        if (statusMessage != null) {
            error.setValue(statusMessage);
            return;
        }
        if (!NetworkMonitor.getInstance().isOnline()) {
            error.setValue(MESSAGE_OFFLINE);
            return;
        }
        List<Uri> twoImages = new ArrayList<Uri>();
        twoImages.add(imageUris.get(0));
        twoImages.add(imageUris.get(1));
        loading.setValue(true);
        checkEmailThenSubmit(nationalId.trim(), twoImages);
    }

    public void loadVerification() {
        String uid = SessionManager.getInstance().getCurrentUserId();
        if (uid == null) {
            error.setValue(MESSAGE_NOT_SIGNED_IN);
            return;
        }
        userRepository.getVerification(uid, new DataCallback<SellerVerification>() {
            @Override
            public void onSuccess(SellerVerification result) {
                verification.postValue(result);
            }

            @Override
            public void onError(String message) {
                error.postValue(message);
            }
        });
    }

    public void loadIdentity() {
        error.setValue(DataCallback.NOT_READY);
    }

    public void saveIdentity(UserIdentity identity) {
        error.setValue(DataCallback.NOT_READY);
    }

    public void updateNotificationPrefs(Map<String, Boolean> prefs) {
        error.setValue(DataCallback.NOT_READY);
    }

    public void deactivateAccount(String password) {
        error.setValue(DataCallback.NOT_READY);
    }

    public void deactivateWithGoogle(String idToken) {
        error.setValue(DataCallback.NOT_READY);
    }

    // The ViewModel outlives a sign out, so another account must never see the previous one's data.
    private void clearPreviousUser() {
        loadedUid = null;
        profile.setValue(null);
        stats.setValue(null);
        verification.setValue(null);
        following.setValue(null);
    }

    private void fetchProfile(String uid) {
        requestedUid = uid;
        loading.setValue(true);
        userRepository.getProfile(uid, new DataCallback<User>() {
            @Override
            public void onSuccess(User result) {
                if (!uid.equals(requestedUid)) {
                    return;
                }
                loadedUid = uid;
                profile.postValue(result);
                fetchFollowing(uid);
                if (isOwnPage(uid)) {
                    fetchStats(uid);
                } else {
                    loading.postValue(false);
                }
            }

            @Override
            public void onError(String message) {
                loading.postValue(false);
                error.postValue(message);
            }
        });
    }

    // Counted on every open (trang-thai.md, common rules), never read from a stored field.
    private void fetchStats(String uid) {
        userRepository.getStats(uid, new DataCallback<ProfileStats>() {
            @Override
            public void onSuccess(ProfileStats result) {
                if (!uid.equals(requestedUid)) {
                    return;
                }
                stats.postValue(result);
                loading.postValue(false);
            }

            // The raw Firestore text is in the log; the user gets one short sentence.
            @Override
            public void onError(String message) {
                loading.postValue(false);
                error.postValue(MESSAGE_STATS_FAILED);
            }
        });
    }

    // The Rules let a user read only deals they are part of, so the counts exist only on the own page.
    private boolean isOwnPage(String uid) {
        String me = SessionManager.getInstance().getCurrentUserId();
        return me != null && !me.isEmpty() && me.equals(uid);
    }

    // Reads every saved listing at once and joins the answers in the saved order.
    private void fetchSavedListings(List<String> savedIds) {
        List<String> ids = new ArrayList<String>();
        if (savedIds != null) {
            ids.addAll(savedIds);
        }
        if (ids.isEmpty()) {
            savedListings.postValue(new ArrayList<Listing>());
            loading.postValue(false);
            return;
        }
        SavedCollector collector = new SavedCollector(ids);
        for (String id : ids) {
            listingRepository.getById(id, new SavedListingCallback(collector, id));
        }
    }

    private static List<Listing> withoutListing(List<Listing> listings, String listingId) {
        List<Listing> result = new ArrayList<Listing>();
        if (listings == null) {
            return result;
        }
        for (Listing listing : listings) {
            if (listingId == null || !listingId.equals(listing.getId())) {
                result.add(listing);
            }
        }
        return result;
    }

    // A listing that could not be read stays as a card holding only its id, so it can still be unsaved.
    static List<Listing> buildSavedList(List<String> savedIds, Map<String, Listing> found) {
        List<Listing> result = new ArrayList<Listing>();
        if (savedIds == null) {
            return result;
        }
        for (String id : savedIds) {
            Listing listing = null;
            if (found != null) {
                listing = found.get(id);
            }
            if (listing == null) {
                listing = new Listing();
                listing.setId(id);
            }
            result.add(listing);
        }
        return result;
    }

    static boolean isUnavailable(Listing listing) {
        if (listing == null || listing.getStatus() == null) {
            return true;
        }
        ListingStatus status = listing.getStatus();
        return status == ListingStatus.HIDDEN || status == ListingStatus.REMOVED || status == ListingStatus.DRAFT;
    }

    // Callbacks may come back on different threads, so the shared map and counter are synchronized.
    private final class SavedCollector {
        private final List<String> savedIds;
        private final Map<String, Listing> found = new HashMap<String, Listing>();
        private int answered;

        SavedCollector(List<String> savedIds) {
            this.savedIds = savedIds;
        }

        synchronized void answer(String id, Listing listing) {
            if (listing != null) {
                found.put(id, listing);
            }
            answered = answered + 1;
            if (answered < savedIds.size()) {
                return;
            }
            savedListings.postValue(buildSavedList(savedIds, found));
            loading.postValue(false);
        }
    }

    private static final class SavedListingCallback implements DataCallback<Listing> {
        private final SavedCollector collector;
        private final String id;

        SavedListingCallback(SavedCollector collector, String id) {
            this.collector = collector;
            this.id = id;
        }

        @Override
        public void onSuccess(Listing result) {
            collector.answer(id, result);
        }

        // A hidden or removed listing is refused by the Rules; it becomes a "no longer available" card.
        @Override
        public void onError(String message) {
            collector.answer(id, null);
        }
    }

    // Only someone else's page has a Follow button, so the own profile skips this read.
    private void fetchFollowing(String uid) {
        String me = SessionManager.getInstance().getCurrentUserId();
        if (me == null || me.isEmpty() || me.equals(uid)) {
            return;
        }
        userRepository.getFollowingIds(me, new DataCallback<List<String>>() {
            @Override
            public void onSuccess(List<String> result) {
                if (!uid.equals(requestedUid)) {
                    return;
                }
                following.postValue(result != null && result.contains(uid));
            }

            @Override
            public void onError(String message) {
                error.postValue(message);
            }
        });
    }

    // Returns the signed-in uid, or null after putting the reason into error.
    private String followerIdOrReport(String uid) {
        if (Boolean.TRUE.equals(loading.getValue())) {
            return null;
        }
        String me = SessionManager.getInstance().getCurrentUserId();
        if (me == null || me.isEmpty()) {
            error.setValue(MESSAGE_NOT_SIGNED_IN);
            return null;
        }
        if (uid == null || uid.isEmpty() || me.equals(uid)) {
            error.setValue(MESSAGE_CANNOT_FOLLOW_SELF);
            return null;
        }
        if (!NetworkMonitor.getInstance().isOnline()) {
            error.setValue(MESSAGE_OFFLINE);
            return null;
        }
        return me;
    }

    // On success the flag flips; the follower count is not read again because it is only shown on the own page.
    private final class FollowCallback implements DataCallback<Void> {
        private final boolean nowFollowing;

        FollowCallback(boolean nowFollowing) {
            this.nowFollowing = nowFollowing;
        }

        @Override
        public void onSuccess(Void result) {
            following.postValue(nowFollowing);
            loading.postValue(false);
            done.postValue(true);
        }

        @Override
        public void onError(String message) {
            loading.postValue(false);
            error.postValue(message);
        }
    }

    private void saveAvatar(User user, String base64) {
        String previous = user.getAvatarBase64();
        user.setAvatarBase64(base64);
        userRepository.updateProfile(user, new DataCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                profile.postValue(user);
                loading.postValue(false);
            }

            @Override
            public void onError(String message) {
                user.setAvatarBase64(previous);
                loading.postValue(false);
                error.postValue(message);
            }
        });
    }

    private VerificationStatus currentVerificationStatus() {
        User current = profile.getValue();
        if (current == null || current.getVerificationStatus() == null) {
            return VerificationStatus.NONE;
        }
        return current.getVerificationStatus();
    }

    // Returns why the current state forbids a request, or null when it may be sent.
    private String verificationBlockMessage() {
        VerificationStatus status = currentVerificationStatus();
        if (status == VerificationStatus.PENDING) {
            return MESSAGE_ALREADY_PENDING;
        }
        if (status == VerificationStatus.APPROVED) {
            return MESSAGE_ALREADY_APPROVED;
        }
        SellerVerification previous = verification.getValue();
        if (status == VerificationStatus.REJECTED && previous != null && !previous.canResubmit()) {
            return MESSAGE_NO_RESUBMIT_LEFT;
        }
        return null;
    }

    // The email flag is reloaded first, so an unverified account is told why instead of the Rules refusing.
    private void checkEmailThenSubmit(String nationalId, List<Uri> imageUris) {
        authRepository.reloadUser(new DataCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean result) {
                boolean emailVerified = authRepository.isEmailVerified();
                if (!canSubmitVerification(currentVerificationStatus(), emailVerified)) {
                    loading.postValue(false);
                    error.postValue(MESSAGE_EMAIL_NOT_VERIFIED);
                    return;
                }
                saveVerificationImages(nationalId, imageUris, new ArrayList<String>());
            }

            @Override
            public void onError(String message) {
                loading.postValue(false);
                error.postValue(message);
            }
        });
    }

    // One image at a time keeps imageIds in order: identity paper first, portrait second.
    private void saveVerificationImages(String nationalId, List<Uri> imageUris, List<String> savedIds) {
        if (savedIds.size() == imageUris.size()) {
            sendVerification(nationalId, savedIds);
            return;
        }
        Uri next = imageUris.get(savedIds.size());
        mediaRepository.saveImage(next, ImagePurpose.VERIFICATION, new DataCallback<String>() {
            @Override
            public void onSuccess(String imageId) {
                savedIds.add(imageId);
                saveVerificationImages(nationalId, imageUris, savedIds);
            }

            @Override
            public void onError(String message) {
                failVerification(savedIds, message);
            }
        });
    }

    private void sendVerification(String nationalId, List<String> imageIds) {
        String uid = SessionManager.getInstance().getCurrentUserId();
        SellerVerification request = new SellerVerification();
        request.setUserId(uid);
        request.setNationalId(nationalId);
        request.setImageIds(imageIds);
        request.setStatus(VerificationStatus.PENDING);
        userRepository.requestVerification(uid, request, new DataCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                loading.postValue(false);
                done.postValue(true);
                refreshProfile(uid);
            }

            @Override
            public void onError(String message) {
                failVerification(imageIds, message);
            }
        });
    }

    // Reads users/{uid} again so the block on D01 switches to PENDING.
    private void refreshProfile(String uid) {
        userRepository.getProfile(uid, new DataCallback<User>() {
            @Override
            public void onSuccess(User result) {
                profile.postValue(result);
            }

            @Override
            public void onError(String message) {
                error.postValue(message);
            }
        });
    }

    // Images saved by a failed attempt are deleted so they do not stay in Firestore unused.
    private void failVerification(List<String> savedIds, String message) {
        for (String imageId : savedIds) {
            mediaRepository.delete(imageId, new DataCallback<Void>() {
                @Override
                public void onSuccess(Void result) {
                }

                @Override
                public void onError(String deleteMessage) {
                    error.postValue(deleteMessage);
                }
            });
        }
        loading.postValue(false);
        error.postValue(message);
    }

    // Takes flags instead of the Uri list so the JVM unit test needs no Android Uri.
    static int checkVerificationForm(String nationalId, boolean hasIdImage, boolean hasPortraitImage) {
        if (nationalId == null || nationalId.trim().isEmpty()) {
            return FORM_MISSING_NATIONAL_ID;
        }
        if (!isNationalIdValid(nationalId.trim())) {
            return FORM_BAD_NATIONAL_ID;
        }
        if (!hasIdImage) {
            return FORM_MISSING_ID_IMAGE;
        }
        if (!hasPortraitImage) {
            return FORM_MISSING_PORTRAIT_IMAGE;
        }
        return FORM_OK;
    }

    private static boolean hasImageAt(List<Uri> imageUris, int index) {
        return imageUris != null && imageUris.size() > index && imageUris.get(index) != null;
    }

    static boolean isNationalIdValid(String nationalId) {
        if (nationalId.length() != NATIONAL_ID_LENGTH) {
            return false;
        }
        for (int i = 0; i < nationalId.length(); i++) {
            char digit = nationalId.charAt(i);
            if (digit < '0' || digit > '9') {
                return false;
            }
        }
        return true;
    }

    // Same uid is skipped only when the counts are there or the page is not the own one.
    static boolean needsStatsRead(boolean ownPage, boolean hasStats) {
        return ownPage && !hasStats;
    }

    static boolean canSubmitVerification(VerificationStatus status, boolean emailVerified) {
        if (!emailVerified) {
            return false;
        }
        return status == VerificationStatus.NONE || status == VerificationStatus.REJECTED;
    }

    private static String formErrorMessage(int formResult) {
        if (formResult == FORM_MISSING_NATIONAL_ID) {
            return MESSAGE_MISSING_NATIONAL_ID;
        }
        if (formResult == FORM_BAD_NATIONAL_ID) {
            return MESSAGE_BAD_NATIONAL_ID;
        }
        if (formResult == FORM_MISSING_ID_IMAGE) {
            return MESSAGE_MISSING_ID_IMAGE;
        }
        if (formResult == FORM_MISSING_PORTRAIT_IMAGE) {
            return MESSAGE_MISSING_PORTRAIT_IMAGE;
        }
        return null;
    }

    public void clearError() {
        error.setValue(null);
    }

    public void clearDone() {
        done.setValue(false);
    }
}
