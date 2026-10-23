package com.nhom3.pubgapp.viewmodel;

import android.net.Uri;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.nhom3.pubgapp.data.DataCallback;
import com.nhom3.pubgapp.model.Listing;
import com.nhom3.pubgapp.model.ProfileStats;
import com.nhom3.pubgapp.model.SellerVerification;
import com.nhom3.pubgapp.model.User;
import com.nhom3.pubgapp.model.UserAddress;
import com.nhom3.pubgapp.model.UserIdentity;

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
        error.setValue(DataCallback.NOT_READY);
    }

    public void updateProfile(User user) {
        error.setValue(DataCallback.NOT_READY);
    }

    public void changeAvatar(Uri uri) {
        error.setValue(DataCallback.NOT_READY);
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

    public void follow(String uid) {
        error.setValue(DataCallback.NOT_READY);
    }

    public void unfollow(String uid) {
        error.setValue(DataCallback.NOT_READY);
    }

    public void loadSaved() {
        error.setValue(DataCallback.NOT_READY);
    }

    public void unsaveListing(String listingId) {
        error.setValue(DataCallback.NOT_READY);
    }

    public void loadUserListings(String uid) {
        error.setValue(DataCallback.NOT_READY);
    }

    public void requestVerification(String nationalId, List<Uri> imageUris) {
        error.setValue(DataCallback.NOT_READY);
    }

    public void loadVerification() {
        error.setValue(DataCallback.NOT_READY);
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

    public void clearError() {
        error.setValue(null);
    }

    public void clearDone() {
        done.setValue(false);
    }
}
