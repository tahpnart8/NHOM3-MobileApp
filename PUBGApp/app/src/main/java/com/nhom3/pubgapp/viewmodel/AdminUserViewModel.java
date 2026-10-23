package com.nhom3.pubgapp.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.google.firebase.Timestamp;

import com.nhom3.pubgapp.data.DataCallback;
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

import java.util.List;

// State for the admin user management screens. Action bodies report NOT_READY until the repositories are filled.
public class AdminUserViewModel extends ViewModel {

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
        error.setValue(DataCallback.NOT_READY);
    }

    public void loadProfile(String userId) {
        error.setValue(DataCallback.NOT_READY);
    }

    public void lock(String userId, Timestamp until, String reason) {
        error.setValue(DataCallback.NOT_READY);
    }

    public void unlock(String userId) {
        error.setValue(DataCallback.NOT_READY);
    }

    public void warn(String userId, String reason, Timestamp until) {
        error.setValue(DataCallback.NOT_READY);
    }

    public void loadVerificationRequests() {
        error.setValue(DataCallback.NOT_READY);
    }

    public void reviewVerification(String userId, boolean approve, String note) {
        error.setValue(DataCallback.NOT_READY);
    }

    public void loadVerificationImages(String userId) {
        error.setValue(DataCallback.NOT_READY);
    }

    public void loadActivityLog(String userId) {
        error.setValue(DataCallback.NOT_READY);
    }

    public void loadAdmins() {
        error.setValue(DataCallback.NOT_READY);
    }

    public void findAdminCandidate(String email) {
        error.setValue(DataCallback.NOT_READY);
    }

    public void grantAdmin(String userId) {
        error.setValue(DataCallback.NOT_READY);
    }

    public void revokeAdmin(String userId) {
        error.setValue(DataCallback.NOT_READY);
    }

    public void clearError() {
        error.setValue(null);
    }

    public void clearDone() {
        done.setValue(false);
    }
}
