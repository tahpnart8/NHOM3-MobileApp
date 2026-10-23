package com.nhom3.pubgapp.data;

import com.nhom3.pubgapp.model.User;

// Skeleton: the module owner fills each body as the issue assigns it.
public class AuthRepository extends BaseRepository {

    public void signUp(String email, String password, DataCallback<String> cb) {
        cb.onError(DataCallback.NOT_READY);
    }

    public void sendVerificationEmail(DataCallback<Void> cb) {
        cb.onError(DataCallback.NOT_READY);
    }

    public void signIn(String email, String password, DataCallback<User> cb) {
        cb.onError(DataCallback.NOT_READY);
    }

    public void signInWithGoogle(String idToken, DataCallback<User> cb) {
        cb.onError(DataCallback.NOT_READY);
    }

    public void sendPasswordReset(String email, DataCallback<Void> cb) {
        cb.onError(DataCallback.NOT_READY);
    }

    public void changePassword(String current, String next, DataCallback<Void> cb) {
        cb.onError(DataCallback.NOT_READY);
    }

    public String getCurrentUserId() {
        throw new UnsupportedOperationException("NOT_READY");
    }

    public boolean isGoogleAccount() {
        throw new UnsupportedOperationException("NOT_READY");
    }

    public void deleteLogin(DataCallback<Void> cb) {
        cb.onError(DataCallback.NOT_READY);
    }

    public void reauthenticate(String password, DataCallback<Void> cb) {
        cb.onError(DataCallback.NOT_READY);
    }

    public void reauthenticateWithGoogle(String idToken, DataCallback<Void> cb) {
        cb.onError(DataCallback.NOT_READY);
    }

    public void reloadUser(DataCallback<Boolean> cb) {
        cb.onError(DataCallback.NOT_READY);
    }

    public void signOut() {
        throw new UnsupportedOperationException("NOT_READY");
    }

    public boolean isEmailVerified() {
        throw new UnsupportedOperationException("NOT_READY");
    }
}
