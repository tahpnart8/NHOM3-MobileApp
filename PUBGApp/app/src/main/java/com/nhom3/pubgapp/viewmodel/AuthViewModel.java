package com.nhom3.pubgapp.viewmodel;

import android.content.Context;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.FirebaseApp;
import com.nhom3.pubgapp.data.AdminRepository;
import com.nhom3.pubgapp.data.AuthRepository;
import com.nhom3.pubgapp.data.DataCallback;
import com.nhom3.pubgapp.data.UserRepository;
import com.nhom3.pubgapp.data.local.AppDatabase;
import com.nhom3.pubgapp.model.Category;
import com.nhom3.pubgapp.model.User;
import com.nhom3.pubgapp.model.UserIdentity;
import com.nhom3.pubgapp.model.enums.UserStatus;
import com.nhom3.pubgapp.util.SessionManager;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

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

    private final AuthRepository authRepository;
    private final UserRepository userRepository;
    private final AdminRepository adminRepository;
    private final SessionManager sessionManager;

    public AuthViewModel() {
        this.authRepository = new AuthRepository();
        this.userRepository = new UserRepository();
        this.adminRepository = new AdminRepository();
        this.sessionManager = SessionManager.getInstance();
    }

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

    enum LoginDecision {
        ALLOW,
        CLEAR_EXPIRED_LOCK,
        LOCKED,
        DELETED
    }

    static LoginDecision evaluateUserStatus(User user) {
        if (user.isDeleted()) {
            return LoginDecision.DELETED;
        }
        if (user.isLocked()) {
            return LoginDecision.LOCKED;
        }
        if (user.getStatus() == UserStatus.TEMP_LOCKED && !user.isLocked()) {
            return LoginDecision.CLEAR_EXPIRED_LOCK;
        }
        return LoginDecision.ALLOW;
    }

    private String getLockErrorMessage(User user) {
        if (user.getStatus() == UserStatus.PERM_LOCKED) {
            String reason = user.getLockReason();
            if (reason != null && !reason.isEmpty()) {
                return "Tài khoản đã bị khóa vĩnh viễn. Lý do: " + reason;
            }
            return "Tài khoản đã bị khóa vĩnh viễn.";
        }
        if (user.getStatus() == UserStatus.TEMP_LOCKED) {
            String reason = user.getLockReason();
            String untilStr = "";
            if (user.getLockedUntil() != null) {
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
                untilStr = sdf.format(user.getLockedUntil().toDate());
            }
            if (reason != null && !reason.isEmpty()) {
                return "Tài khoản bị tạm khóa đến " + untilStr + ". Lý do: " + reason;
            }
            return "Tài khoản bị tạm khóa đến " + untilStr + ".";
        }
        return "Tài khoản đang bị khóa.";
    }

    private void startUserSession(final User user) {
        adminRepository.isAdmin(user.getId(), new DataCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean isAdmin) {
                boolean adminFlag = isAdmin != null && isAdmin;
                sessionManager.start(user, adminFlag);
                userRepository.touchLastActive(user.getId());
                loading.postValue(false);
                loggedIn.postValue(true);
            }

            @Override
            public void onError(String message) {
                authRepository.signOut();
                loading.postValue(false);
                error.postValue(message != null ? message : "Không thể xác thực quyền người dùng.");
            }
        });
    }

    private void processUserSession(final User user) {
        LoginDecision decision = evaluateUserStatus(user);
        if (decision == LoginDecision.DELETED) {
            authRepository.signOut();
            loading.postValue(false);
            error.postValue("Tài khoản đã xóa.");
            return;
        }
        if (decision == LoginDecision.LOCKED) {
            authRepository.signOut();
            loading.postValue(false);
            error.postValue(getLockErrorMessage(user));
            return;
        }
        if (decision == LoginDecision.CLEAR_EXPIRED_LOCK) {
            userRepository.clearExpiredLock(user.getId(), new DataCallback<Void>() {
                @Override
                public void onSuccess(Void result) {
                    user.setStatus(UserStatus.ACTIVE);
                    user.setLockedUntil(null);
                    user.setLockReason("");
                    startUserSession(user);
                }

                @Override
                public void onError(String message) {
                    authRepository.signOut();
                    loading.postValue(false);
                    error.postValue("Tài khoản đang bị khóa tạm thời. Vui lòng thử lại sau.");
                }
            });
            return;
        }
        startUserSession(user);
    }

    public void login(final String email, final String password) {
        loading.setValue(true);
        error.setValue(null);
        authRepository.signIn(email, password, new DataCallback<User>() {
            @Override
            public void onSuccess(final User user) {
                authRepository.reloadUser(new DataCallback<Boolean>() {
                    @Override
                    public void onSuccess(Boolean verified) {
                        if (!authRepository.isEmailVerified()) {
                            loading.postValue(false);
                            signUpStep.postValue(2);
                            return;
                        }
                        if (user == null) {
                            loading.postValue(false);
                            needsProfile.postValue(true);
                            return;
                        }
                        processUserSession(user);
                    }

                    @Override
                    public void onError(String message) {
                        if (!authRepository.isEmailVerified()) {
                            loading.postValue(false);
                            signUpStep.postValue(2);
                            return;
                        }
                        if (user == null) {
                            loading.postValue(false);
                            needsProfile.postValue(true);
                            return;
                        }
                        processUserSession(user);
                    }
                });
            }

            @Override
            public void onError(String message) {
                loading.postValue(false);
                error.postValue(message != null ? message : "Email hoặc mật khẩu không đúng.");
            }
        });
    }

    public void loginWithGoogle(String idToken) {
        loading.setValue(true);
        error.setValue(null);
        authRepository.signInWithGoogle(idToken, new DataCallback<User>() {
            @Override
            public void onSuccess(User user) {
                if (user == null) {
                    loading.postValue(false);
                    needsProfile.postValue(true);
                    return;
                }
                processUserSession(user);
            }

            @Override
            public void onError(String message) {
                loading.postValue(false);
                error.postValue(message != null ? message : "Không thể đăng nhập bằng Google.");
            }
        });
    }

    public void restoreSession() {
        if (sessionManager.isLoggedIn()) {
            return;
        }
        final String uid = authRepository.getCurrentUserId();
        if (uid == null || uid.isEmpty()) {
            return;
        }
        loading.setValue(true);
        authRepository.reloadUser(new DataCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean verified) {
                if (!authRepository.isGoogleAccount() && !authRepository.isEmailVerified()) {
                    loading.postValue(false);
                    return;
                }
                userRepository.getProfile(uid, new DataCallback<User>() {
                    @Override
                    public void onSuccess(User user) {
                        if (user == null) {
                            loading.postValue(false);
                            needsProfile.postValue(true);
                            return;
                        }
                        processUserSession(user);
                    }

                    @Override
                    public void onError(String message) {
                        loading.postValue(false);
                    }
                });
            }

            @Override
            public void onError(String message) {
                loading.postValue(false);
            }
        });
    }

    public void resetPassword(String email) {
        loading.setValue(true);
        error.setValue(null);
        done.setValue(false);
        authRepository.sendPasswordReset(email, new DataCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                loading.postValue(false);
                done.postValue(true);
            }

            @Override
            public void onError(String message) {
                loading.postValue(false);
                String errMsg = message != null ? message : "Không thể gửi liên kết đặt lại mật khẩu.";
                error.postValue(errMsg);
            }
        });
    }

    public void changePassword(String current, String next) {
        error.setValue(DataCallback.NOT_READY);
    }

    public boolean isGoogleAccount() {
        return authRepository.isGoogleAccount();
    }

    public void logout() {
        authRepository.signOut();
        sessionManager.clear();
        clearRoomDatabase();
        loggedIn.setValue(false);
        needsProfile.setValue(false);
        signUpStep.setValue(1);
        emailVerified.setValue(false);
    }

    private void clearRoomDatabase() {
        try {
            FirebaseApp app = FirebaseApp.getInstance();
            if (app != null) {
                Context context = app.getApplicationContext();
                if (context != null) {
                    final AppDatabase db = AppDatabase.getInstance(context);
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                db.listingDao().clearAll();
                                db.chatDao().clearAll();
                                db.dealDao().clearAll();
                                db.historyDao().clearAll();
                            } catch (Exception e) {
                                Log.w("AuthViewModel", "Cannot clear Room", e);
                            }
                        }
                    }).start();
                }
            }
        } catch (Exception e) {
            Log.w("AuthViewModel", "Cannot access app context for Room", e);
        }
    }

    public void clearError() {
        error.setValue(null);
    }

    public void clearDone() {
        done.setValue(false);
    }
}
