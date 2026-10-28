package com.nhom3.pubgapp.viewmodel;

import android.os.Handler;
import android.os.Looper;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.google.firebase.Timestamp;

import com.nhom3.pubgapp.data.AdminRepository;
import com.nhom3.pubgapp.data.DataCallback;
import com.nhom3.pubgapp.data.DealRepository;
import com.nhom3.pubgapp.data.ListingRepository;
import com.nhom3.pubgapp.data.MediaRepository;
import com.nhom3.pubgapp.data.ReportRepository;
import com.nhom3.pubgapp.data.ReviewRepository;
import com.nhom3.pubgapp.data.UserRepository;
import com.nhom3.pubgapp.model.ActivityLog;
import com.nhom3.pubgapp.model.Deal;
import com.nhom3.pubgapp.model.Listing;
import com.nhom3.pubgapp.model.ProfileStats;
import com.nhom3.pubgapp.model.Review;
import com.nhom3.pubgapp.model.SellerVerification;
import com.nhom3.pubgapp.model.User;
import com.nhom3.pubgapp.model.UserIdentity;
import com.nhom3.pubgapp.model.Warning;
import com.nhom3.pubgapp.model.enums.UserStatus;
import com.nhom3.pubgapp.util.SessionManager;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

// State for the admin user management screens. Action bodies report NOT_READY until the repositories are filled.
public class AdminUserViewModel extends ViewModel {

    private static final String MESSAGE_LOAD_USERS_FAILED = "Không tải được danh sách người dùng.";

    private final MutableLiveData<List<User>> users = new MutableLiveData<>();
    private final MutableLiveData<User> selectedUser = new MutableLiveData<>();
    private final MutableLiveData<UserIdentity> identity = new MutableLiveData<>();
    private final MutableLiveData<ProfileStats> stats = new MutableLiveData<>();
    private final MutableLiveData<List<Warning>> warnings = new MutableLiveData<>();
    private final MutableLiveData<List<ActivityLog>> logs = new MutableLiveData<>();
    private final MutableLiveData<List<SellerVerification>> verifications = new MutableLiveData<>();
    private final MutableLiveData<List<Listing>> userListings = new MutableLiveData<>();
    private final MutableLiveData<List<Deal>> userDeals = new MutableLiveData<>();
    private final MutableLiveData<List<Review>> userReviews = new MutableLiveData<>();
    private final MutableLiveData<Integer> reportCount = new MutableLiveData<>();
    private final MutableLiveData<List<byte[]>> verificationImages = new MutableLiveData<>();
    private final MutableLiveData<List<User>> admins = new MutableLiveData<>();
    private final MutableLiveData<User> pendingAdmin = new MutableLiveData<>();
    private final MutableLiveData<Boolean> loading = new MutableLiveData<>();
    private final MutableLiveData<String> error = new MutableLiveData<>();
    private final MutableLiveData<Boolean> done = new MutableLiveData<>();
    // Filters change fast; an answer to an older query must not replace the newest one.
    private int latestFilterRequest;

    private static final int PROFILE_READ_COUNT = 8;
    private static final String KEY_PROFILE = "profile";
    private static final String KEY_REQUESTS = "requests";
    private static final int IMAGE_COUNT = 2;
    private static final String MESSAGE_USER_REQUIRED = "Thiếu mã người dùng cần xem.";
    private static final String MESSAGE_REASON_REQUIRED = "Hãy nhập lý do từ chối.";
    private static final String MESSAGE_NO_REQUEST =
            "Không tìm thấy yêu cầu xác thực của tài khoản này.";
    private static final String MESSAGE_MISSING_IMAGES = "Yêu cầu xác thực thiếu ảnh giấy tờ.";
    private static final String MESSAGE_IMAGE_FAILED =
            "Không tải được ảnh giấy tờ. Hãy kiểm tra mạng rồi thử lại.";
    // Lock, unlock and warn.
    private static final long ACTION_TIMEOUT_MS = 15000L;
    private static final String MESSAGE_ACTION_SELF =
            "Bạn không thể cảnh báo hoặc khóa chính tài khoản của mình.";
    private static final String MESSAGE_ACTION_REASON_REQUIRED = "Hãy nhập lý do.";
    private static final String MESSAGE_ACTION_TIMEOUT =
            "Máy chủ chưa trả lời sau 15 giây. Thao tác có thể vẫn được ghi,"
                    + " hãy làm mới màn hình để kiểm tra.";

    private final AdminRepository adminRepository = new AdminRepository();
    private final UserRepository userRepository = new UserRepository();
    private final ListingRepository listingRepository = new ListingRepository();
    private final DealRepository dealRepository = new DealRepository();
    private final ReviewRepository reviewRepository = new ReviewRepository();
    private final ReportRepository reportRepository = new ReportRepository();
    private final MediaRepository mediaRepository = new MediaRepository();
    // The selected profile and the pending requests are live, so a change made elsewhere shows at once.
    private final ListenerSet listeners = new ListenerSet();

    // Reads still running; loading stays true until this is back to zero. Callbacks run on other threads.
    private final AtomicInteger pendingReads = new AtomicInteger(0);
    private final AtomicBoolean reviewing = new AtomicBoolean(false);
    private final AtomicBoolean acting = new AtomicBoolean(false);
    private final Handler timeoutHandler = new Handler(Looper.getMainLooper());
    // What was already asked for, so a rotation does not read again. Cleared when a read fails, so Retry works.
    private volatile String loadedProfileUserId;
    private volatile String loadedLogUserId;
    private volatile String loadedImagesUserId;
    private volatile boolean requestsAsked;

    public LiveData<List<User>> getUsers() {
        return users;
    }

    public LiveData<User> getSelectedUser() {
        return selectedUser;
    }

    public LiveData<UserIdentity> getIdentity() {
        return identity;
    }

    public LiveData<ProfileStats> getStats() {
        return stats;
    }

    public LiveData<List<Warning>> getWarnings() {
        return warnings;
    }

    public LiveData<List<ActivityLog>> getLogs() {
        return logs;
    }

    public LiveData<List<SellerVerification>> getVerifications() {
        return verifications;
    }

    public LiveData<List<Listing>> getUserListings() {
        return userListings;
    }

    public LiveData<List<Deal>> getUserDeals() {
        return userDeals;
    }

    public LiveData<List<Review>> getUserReviews() {
        return userReviews;
    }

    public LiveData<Integer> getReportCount() {
        return reportCount;
    }

    public LiveData<List<byte[]>> getVerificationImages() {
        return verificationImages;
    }

    public LiveData<List<User>> getAdmins() {
        return admins;
    }

    public LiveData<User> getPendingAdmin() {
        return pendingAdmin;
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

    public void filterUsers(UserStatus status, Timestamp from, Timestamp to, String province, String keyword) {
        latestFilterRequest++;
        final int requestId = latestFilterRequest;
        loading.setValue(true);
        error.setValue(null);
        adminRepository.listUsers(status, from, to, province, keyword, new DataCallback<List<User>>() {
            @Override
            public void onSuccess(List<User> result) {
                if (requestId != latestFilterRequest) {
                    return;
                }
                users.postValue(result);
                loading.postValue(false);
            }

            @Override
            public void onError(String message) {
                if (requestId != latestFilterRequest) {
                    return;
                }
                error.postValue(message != null ? message : MESSAGE_LOAD_USERS_FAILED);
                loading.postValue(false);
            }
        });
    }

    public void loadProfile(String userId) {
        if (userId == null || userId.isEmpty()) {
            error.setValue(MESSAGE_USER_REQUIRED);
            return;
        }
        if (userId.equals(loadedProfileUserId)) {
            return;
        }
        startProfileLoad(userId);
    }

    public void lock(String userId, Timestamp until, String reason) {
        if (!canStartAction(userId, reason, true)) {
            return;
        }
        adminRepository.lockUser(userId, until, reason.trim(), beginAction(userId));
    }

    public void unlock(String userId) {
        if (!canStartAction(userId, null, false)) {
            return;
        }
        adminRepository.unlockUser(userId, beginAction(userId));
    }

    public void warn(String userId, String reason, Timestamp until) {
        if (!canStartAction(userId, reason, true)) {
            return;
        }
        Warning warning = new Warning();
        warning.setReason(reason.trim());
        adminRepository.warnUser(userId, warning, until, beginAction(userId));
    }

    public void loadVerificationRequests() {
        if (requestsAsked) {
            return;
        }
        startRequestsLoad();
    }

    public void reviewVerification(final String userId, boolean approve, String note) {
        if (userId == null || userId.isEmpty()) {
            error.setValue(MESSAGE_USER_REQUIRED);
            return;
        }
        boolean reasonMissing = !approve && (note == null || note.trim().isEmpty());
        if (reasonMissing) {
            error.setValue(MESSAGE_REASON_REQUIRED);
            return;
        }
        if (!reviewing.compareAndSet(false, true)) {
            return;
        }
        beginReads(1);
        adminRepository.reviewVerification(userId, approve, note, new DataCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                reviewing.set(false);
                done.postValue(true);
                refreshAfterReview(userId);
                endRead();
            }

            @Override
            public void onError(String message) {
                reviewing.set(false);
                error.postValue(message);
                // Another admin may have decided the request already, so read its real state again.
                startRequestsLoad();
                endRead();
            }
        });
    }

    public void loadVerificationImages(String userId) {
        if (userId == null || userId.isEmpty()) {
            error.setValue(MESSAGE_USER_REQUIRED);
            return;
        }
        if (userId.equals(loadedImagesUserId)) {
            return;
        }
        SellerVerification request = findRequestOf(userId);
        if (request == null) {
            error.setValue(MESSAGE_NO_REQUEST);
            return;
        }
        List<String> imageIds = request.getImageIds();
        if (imageIds == null || imageIds.size() < IMAGE_COUNT) {
            error.setValue(MESSAGE_MISSING_IMAGES);
            return;
        }
        loadedImagesUserId = userId;
        readVerificationImages(userId, imageIds);
    }

    public void loadActivityLog(String userId) {
        if (userId == null || userId.isEmpty()) {
            error.setValue(MESSAGE_USER_REQUIRED);
            return;
        }
        if (userId.equals(loadedLogUserId)) {
            return;
        }
        loadedLogUserId = userId;
        beginReads(1);
        adminRepository.getActivityLogs(userId, new ReadCallback<List<ActivityLog>>(logs));
    }

    public void loadAdmins() {
        beginReads(1);
        adminRepository.listAdmins(new ReadCallback<List<User>>(admins));
    }

    public void findAdminCandidate(String email) {
        if (email == null || email.trim().isEmpty()) {
            error.setValue("Hãy nhập email cần tìm.");
            return;
        }
        pendingAdmin.setValue(null);
        beginReads(1);
        adminRepository.findUserByEmail(email.trim(), new DataCallback<User>() {
            @Override
            public void onSuccess(User user) {
                pendingAdmin.postValue(user);
                endRead();
            }

            @Override
            public void onError(String message) {
                pendingAdmin.postValue(null);
                error.postValue(message);
                endRead();
            }
        });
    }

    // An admin cannot grant or revoke their own admin status (R12).
    static boolean isSelfAdminAction(String targetUserId, String currentUserId) {
        if (targetUserId == null || currentUserId == null) {
            return false;
        }
        return targetUserId.equals(currentUserId);
    }

    public void grantAdmin(String userId) {
        if (userId == null || userId.isEmpty()) {
            error.setValue(MESSAGE_USER_REQUIRED);
            return;
        }
        String currentId = SessionManager.getInstance().getCurrentUserId();
        if (isSelfAdminAction(userId, currentId)) {
            error.setValue("Bạn không thể tự cấp quyền cho chính mình.");
            return;
        }
        beginReads(1);
        adminRepository.grantAdmin(userId, new DataCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                done.postValue(true);
                pendingAdmin.postValue(null);
                endRead();
                loadAdmins();
            }

            @Override
            public void onError(String message) {
                error.postValue(message);
                endRead();
            }
        });
    }

    public void revokeAdmin(String userId) {
        if (userId == null || userId.isEmpty()) {
            error.setValue(MESSAGE_USER_REQUIRED);
            return;
        }
        String currentId = SessionManager.getInstance().getCurrentUserId();
        if (isSelfAdminAction(userId, currentId)) {
            error.setValue("Bạn không thể tự gỡ quyền quản trị của chính mình.");
            return;
        }
        beginReads(1);
        adminRepository.revokeAdmin(userId, new DataCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                done.postValue(true);
                endRead();
                loadAdmins();
            }

            @Override
            public void onError(String message) {
                error.postValue(message);
                endRead();
            }
        });
    }

    public void clearError() {
        error.setValue(null);
    }

    public void clearDone() {
        done.setValue(false);
    }

    private boolean canStartAction(String userId, String reason, boolean needsReason) {
        if (userId == null || userId.isEmpty()) {
            error.setValue(MESSAGE_USER_REQUIRED);
            return false;
        }
        if (userId.equals(SessionManager.getInstance().getCurrentUserId())) {
            error.setValue(MESSAGE_ACTION_SELF);
            return false;
        }
        if (needsReason && (reason == null || reason.trim().isEmpty())) {
            error.setValue(MESSAGE_ACTION_REASON_REQUIRED);
            return false;
        }
        return !acting.get();
    }

    private ActionCallback beginAction(String userId) {
        acting.set(true);
        beginReads(1);
        return new ActionCallback(userId);
    }

    private void beginReads(int count) {
        pendingReads.addAndGet(count);
        loading.postValue(true);
    }

    private void endRead() {
        if (pendingReads.decrementAndGet() <= 0) {
            pendingReads.set(0);
            loading.postValue(false);
        }
    }

    // Safe from any thread: it only posts values, so a callback can start it again after a review.
    private void startProfileLoad(String userId) {
        loadedProfileUserId = userId;
        beginReads(PROFILE_READ_COUNT);
        listeners.put(KEY_PROFILE, userRepository.observeProfile(userId, new ReadCallback<User>(selectedUser)));
        userRepository.getIdentity(userId, new ReadCallback<UserIdentity>(identity));
        userRepository.getStats(userId, new ReadCallback<ProfileStats>(stats));
        adminRepository.getWarnings(userId, new ReadCallback<List<Warning>>(warnings));
        listingRepository.getBySeller(userId, new ReadCallback<List<Listing>>(userListings));
        dealRepository.getMyDeals(userId, new ReadCallback<List<Deal>>(userDeals));
        reviewRepository.getReceived(userId, new ReadCallback<List<Review>>(userReviews));
        reportRepository.countByTarget(userId, new ReadCallback<Integer>(reportCount));
    }

    private void startRequestsLoad() {
        requestsAsked = true;
        beginReads(1);
        listeners.put(KEY_REQUESTS, adminRepository.observeVerificationRequests(
                new ReadCallback<List<SellerVerification>>(verifications)));
    }

    private void refreshAfterReview(String userId) {
        startRequestsLoad();
        startProfileLoad(userId);
    }

    private void forgetLoaded() {
        loadedProfileUserId = null;
        loadedLogUserId = null;
        requestsAsked = false;
    }

    private SellerVerification findRequestOf(String userId) {
        List<SellerVerification> requests = verifications.getValue();
        if (requests == null) {
            return null;
        }
        for (SellerVerification request : requests) {
            if (userId.equals(request.getUserId())) {
                return request;
            }
        }
        return null;
    }

    // Element 0 is the identity document, element 1 the portrait holding it. A failed image stays empty.
    private void readVerificationImages(final String userId, List<String> imageIds) {
        final byte[][] results = new byte[IMAGE_COUNT][];
        for (int index = 0; index < IMAGE_COUNT; index++) {
            results[index] = new byte[0];
        }
        final AtomicInteger remaining = new AtomicInteger(IMAGE_COUNT);
        final AtomicBoolean anyFailed = new AtomicBoolean(false);
        for (int index = 0; index < IMAGE_COUNT; index++) {
            final int position = index;
            mediaRepository.getImage(imageIds.get(index), new DataCallback<byte[]>() {
                @Override
                public void onSuccess(byte[] bytes) {
                    if (bytes != null) {
                        results[position] = bytes;
                    }
                    finishImage(results, remaining, anyFailed);
                }

                @Override
                public void onError(String message) {
                    anyFailed.set(true);
                    finishImage(results, remaining, anyFailed);
                }
            });
        }
    }

    private void finishImage(byte[][] results, AtomicInteger remaining, AtomicBoolean anyFailed) {
        if (remaining.decrementAndGet() > 0) {
            return;
        }
        List<byte[]> images = new ArrayList<byte[]>();
        for (byte[] bytes : results) {
            images.add(bytes);
        }
        verificationImages.postValue(images);
        if (anyFailed.get()) {
            loadedImagesUserId = null;
            error.postValue(MESSAGE_IMAGE_FAILED);
        }
    }

    // Puts a result into one LiveData and counts the read as finished once, even if the source answers twice.
    private final class ReadCallback<T> implements DataCallback<T> {

        private final MutableLiveData<T> target;
        private final AtomicBoolean finished = new AtomicBoolean(false);

        ReadCallback(MutableLiveData<T> target) {
            this.target = target;
        }

        @Override
        public void onSuccess(T result) {
            target.postValue(result);
            finishOnce();
        }

        @Override
        public void onError(String message) {
            forgetLoaded();
            error.postValue(message);
            finishOnce();
        }

        private void finishOnce() {
            if (finished.compareAndSet(false, true)) {
                endRead();
            }
        }
    }

    // One lock, unlock or warn write: reloads the profile on success and gives up after 15 seconds.
    private final class ActionCallback implements DataCallback<Void> {

        private final String userId;
        private final AtomicBoolean finished = new AtomicBoolean(false);
        private final Runnable timeout = new Runnable() {
            @Override
            public void run() {
                if (finished.compareAndSet(false, true)) {
                    acting.set(false);
                    error.postValue(MESSAGE_ACTION_TIMEOUT);
                    endRead();
                }
            }
        };

        ActionCallback(String userId) {
            this.userId = userId;
            timeoutHandler.postDelayed(timeout, ACTION_TIMEOUT_MS);
        }

        @Override
        public void onSuccess(Void result) {
            if (!finished.compareAndSet(false, true)) {
                return;
            }
            timeoutHandler.removeCallbacks(timeout);
            acting.set(false);
            done.postValue(true);
            startProfileLoad(userId);
            endRead();
        }

        @Override
        public void onError(String message) {
            if (!finished.compareAndSet(false, true)) {
                return;
            }
            timeoutHandler.removeCallbacks(timeout);
            acting.set(false);
            error.postValue(message);
            endRead();
        }
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        listeners.clear();
    }
}
