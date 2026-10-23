package com.nhom3.pubgapp.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.nhom3.pubgapp.data.DataCallback;
import com.nhom3.pubgapp.model.Category;
import com.nhom3.pubgapp.model.User;
import com.nhom3.pubgapp.model.UserIdentity;

import java.util.List;

// State for the sign-up, sign-in and password screens. Action bodies report NOT_READY until the repositories are filled.
public class AuthViewModel extends ViewModel {

    private final MutableLiveData<Boolean> loggedIn = new MutableLiveData<>();
    private final MutableLiveData<Boolean> needsProfile = new MutableLiveData<>();
    private final MutableLiveData<Integer> signUpStep = new MutableLiveData<>();
    private final MutableLiveData<Boolean> emailVerified = new MutableLiveData<>();
    private final MutableLiveData<List<Category>> categories = new MutableLiveData<>();
    private final MutableLiveData<Boolean> loading = new MutableLiveData<>();
    private final MutableLiveData<String> error = new MutableLiveData<>();
    private final MutableLiveData<Boolean> done = new MutableLiveData<>();

    public LiveData<Boolean> getLoggedIn() {
        return loggedIn;
    }

    public LiveData<Boolean> getNeedsProfile() {
        return needsProfile;
    }

    public LiveData<Integer> getSignUpStep() {
        return signUpStep;
    }

    public LiveData<Boolean> getEmailVerified() {
        return emailVerified;
    }

    public LiveData<List<Category>> getCategories() {
        return categories;
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

    public void startSignUp(String email, String password) {
        error.setValue(DataCallback.NOT_READY);
    }

    public void resendVerificationLink() {
        error.setValue(DataCallback.NOT_READY);
    }

    public void checkEmailVerified() {
        error.setValue(DataCallback.NOT_READY);
    }

    public void completeProfile(User user, UserIdentity identity, List<String> categoryIds) {
        error.setValue(DataCallback.NOT_READY);
    }

    public void loadCategories() {
        error.setValue(DataCallback.NOT_READY);
    }

    public void login(String email, String password) {
        error.setValue(DataCallback.NOT_READY);
    }

    public void loginWithGoogle(String idToken) {
        error.setValue(DataCallback.NOT_READY);
    }

    public void restoreSession() {
        error.setValue(DataCallback.NOT_READY);
    }

    public void resetPassword(String email) {
        error.setValue(DataCallback.NOT_READY);
    }

    public void changePassword(String current, String next) {
        error.setValue(DataCallback.NOT_READY);
    }

    public boolean isGoogleAccount() {
        error.setValue(DataCallback.NOT_READY);
        return false;
    }

    public void logout() {
        error.setValue(DataCallback.NOT_READY);
    }

    public void clearError() {
        error.setValue(null);
    }

    public void clearDone() {
        done.setValue(false);
    }
}
