package com.nhom3.pubgapp.viewmodel;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.nhom3.pubgapp.data.AdminRepository;
import com.nhom3.pubgapp.data.AuthRepository;
import com.nhom3.pubgapp.data.CategoryRepository;
import com.nhom3.pubgapp.data.DataCallback;
import com.nhom3.pubgapp.data.MediaRepository;
import com.nhom3.pubgapp.data.UserRepository;
import com.nhom3.pubgapp.data.local.AppDatabase;
import com.nhom3.pubgapp.model.Category;
import com.nhom3.pubgapp.model.User;
import com.nhom3.pubgapp.model.UserIdentity;
import com.nhom3.pubgapp.model.enums.UserStatus;
import com.nhom3.pubgapp.util.SessionManager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

// State for the sign-up, sign-in and password screens. Action bodies report NOT_READY until the repositories are filled.
public class AuthViewModel extends ViewModel {

    private static final String TAG = "AuthViewModel";
    // Avatar size from firestore-schema.md, users.avatarBase64.
    private static final int GOOGLE_AVATAR_SIZE = 256;
    private static final String GOOGLE_PHOTO_SIZE_MARK = "=s";
    private static final int GOOGLE_PHOTO_TIMEOUT_MS = 10000;
    private static final String GOOGLE_AVATAR_FILE = "google_avatar.jpg";
    private static final int COPY_BUFFER_BYTES = 8192;

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
    private final CategoryRepository categoryRepository;
    private final SessionManager sessionManager;
    private final MediaRepository mediaRepository = new MediaRepository();
    private final ExecutorService avatarExecutor = Executors.newSingleThreadExecutor();

    public AuthViewModel() {
        this.authRepository = new AuthRepository();
        this.userRepository = new UserRepository();
        this.adminRepository = new AdminRepository();
        this.categoryRepository = new CategoryRepository();
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
        if (!isPasswordValid(password)) {
            error.setValue("Mật khẩu cần ít nhất 8 ký tự, gồm chữ và số.");
            return;
        }
        loading.setValue(true);
        error.setValue(null);
        authRepository.signUp(email, password, new DataCallback<String>() {
            @Override
            public void onSuccess(String uid) {
                loading.postValue(false);
                signUpStep.postValue(2);
            }

            @Override
            public void onError(String message) {
                loading.postValue(false);
                error.postValue(message != null ? message : "Không thể tạo tài khoản.");
            }
        });
    }

    public void resendVerificationLink() {
        loading.setValue(true);
        error.setValue(null);
        done.setValue(false);
        authRepository.sendVerificationEmail(new DataCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                loading.postValue(false);
                done.postValue(true);
            }

            @Override
            public void onError(String message) {
                loading.postValue(false);
                if (message != null && message.contains("TOO_MANY_ATTEMPTS_TRY_LATER")) {
                    error.postValue("Bạn đã gửi quá nhiều lần, hãy thử lại sau ít phút.");
                } else {
                    error.postValue(message != null ? message : "Không thể gửi lại liên kết xác thực.");
                }
            }
        });
    }

    public void checkEmailVerified() {
        authRepository.reloadUser(new DataCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean verified) {
                if (verified != null && verified) {
                    emailVerified.postValue(true);
                    signUpStep.postValue(3);
                }
            }

            @Override
            public void onError(String message) {
                // Keep waiting on screen 2 without error
            }
        });
    }

    public void completeProfile(final User user, UserIdentity identity, List<String> categoryIds) {
        loading.setValue(true);
        error.setValue(null);
        if (categoryIds != null) {
            user.setInterestedCategoryIds(categoryIds);
        }
        userRepository.createProfile(user, identity, new DataCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                startUserSession(user);
            }

            @Override
            public void onError(String message) {
                loading.postValue(false);
                error.postValue(message != null ? message : "Không thể tạo hồ sơ.");
            }
        });
    }

    public void loadCategories() {
        categoryRepository.getAll(new DataCallback<List<Category>>() {
            @Override
            public void onSuccess(List<Category> list) {
                categories.postValue(list);
            }

            @Override
            public void onError(String message) {
                error.postValue(message != null ? message : "Không thể tải danh mục.");
            }
        });
    }

    public static boolean isPasswordValid(String password) {
        if (password == null || password.length() < 8) {
            return false;
        }
        boolean hasLetter = false;
        boolean hasDigit = false;
        for (int i = 0; i < password.length(); i++) {
            char c = password.charAt(i);
            if (Character.isLetter(c)) {
                hasLetter = true;
            } else if (Character.isDigit(c)) {
                hasDigit = true;
            }
        }
        return hasLetter && hasDigit;
    }

    public static boolean isPasswordConfirmationMatching(String password, String confirmation) {
        if (password == null || confirmation == null) {
            return false;
        }
        return password.equals(confirmation);
    }

    public static boolean isPhoneValid(String phone) {
        if (phone == null || phone.length() != 10) {
            return false;
        }
        if (phone.charAt(0) != '0') {
            return false;
        }
        for (int i = 0; i < phone.length(); i++) {
            if (!Character.isDigit(phone.charAt(i))) {
                return false;
            }
        }
        return true;
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

    // A Google account takes its Google photo only while the user has not set an avatar of their own.
    static boolean needsGoogleAvatar(boolean isGoogleAccount, String avatarBase64, String photoUrl) {
        if (!isGoogleAccount) {
            return false;
        }
        if (avatarBase64 != null && !avatarBase64.isEmpty()) {
            return false;
        }
        return photoUrl != null && !photoUrl.isEmpty();
    }

    // Google photo URLs end with a size suffix such as "=s96-c"; replacing it asks Google for a larger copy.
    static String sizeGooglePhotoUrl(String photoUrl, int size) {
        int sizeStart = photoUrl.lastIndexOf(GOOGLE_PHOTO_SIZE_MARK);
        int pathEnd = photoUrl.lastIndexOf('/');
        if (sizeStart <= pathEnd) {
            return photoUrl;
        }
        String base = photoUrl.substring(0, sizeStart);
        return base + GOOGLE_PHOTO_SIZE_MARK + size + "-c";
    }

    // Runs after the session starts, so a failed download never blocks signing in.
    private void fillGoogleAvatar(final User user) {
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser == null || firebaseUser.getPhotoUrl() == null) {
            return;
        }
        String photoUrl = firebaseUser.getPhotoUrl().toString();
        if (!needsGoogleAvatar(authRepository.isGoogleAccount(), user.getAvatarBase64(), photoUrl)) {
            return;
        }
        final String sizedUrl = sizeGooglePhotoUrl(photoUrl, GOOGLE_AVATAR_SIZE);
        avatarExecutor.execute(new Runnable() {
            @Override
            public void run() {
                downloadGoogleAvatar(user, sizedUrl);
            }
        });
    }

    private void downloadGoogleAvatar(final User user, String photoUrl) {
        final File file;
        try {
            file = downloadToCache(photoUrl);
        } catch (IOException e) {
            Log.w(TAG, "Cannot download the Google photo", e);
            return;
        }
        // makeThumb applies the avatar size cap, the same as when the user picks a photo.
        mediaRepository.makeThumb(Uri.fromFile(file), new DataCallback<String>() {
            @Override
            public void onSuccess(String base64) {
                file.delete();
                saveGoogleAvatar(user, base64);
            }

            @Override
            public void onError(String message) {
                file.delete();
                Log.w(TAG, "Cannot compress the Google photo: " + message);
            }
        });
    }

    private File downloadToCache(String photoUrl) throws IOException {
        Context context = FirebaseApp.getInstance().getApplicationContext();
        File file = new File(context.getCacheDir(), GOOGLE_AVATAR_FILE);
        URL url = new URL(photoUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setConnectTimeout(GOOGLE_PHOTO_TIMEOUT_MS);
        connection.setReadTimeout(GOOGLE_PHOTO_TIMEOUT_MS);
        try {
            int responseCode = connection.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_OK) {
                throw new IOException("Google photo answered HTTP " + responseCode);
            }
            copyToFile(connection.getInputStream(), file);
        } finally {
            connection.disconnect();
        }
        return file;
    }

    private void copyToFile(InputStream input, File file) throws IOException {
        FileOutputStream output = new FileOutputStream(file);
        try {
            byte[] buffer = new byte[COPY_BUFFER_BYTES];
            int read = input.read(buffer);
            while (read != -1) {
                output.write(buffer, 0, read);
                read = input.read(buffer);
            }
        } finally {
            output.close();
            input.close();
        }
    }

    private void saveGoogleAvatar(User user, String base64) {
        user.setAvatarBase64(base64);
        userRepository.updateProfile(user, new DataCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                Log.i(TAG, "Saved the Google photo as avatar");
            }

            @Override
            public void onError(String message) {
                Log.w(TAG, "Cannot save the Google photo as avatar: " + message);
            }
        });
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
                fillGoogleAvatar(user);
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
        authRepository.signInWithGoogle(idToken, googleSignInCallback());
    }

    // Opens Google's sign-in page in the browser. The Activity is only handed on, never kept.
    public void loginWithGoogleBrowser(Activity activity) {
        loading.setValue(true);
        error.setValue(null);
        authRepository.signInWithGoogleBrowser(activity, googleSignInCallback());
    }

    // Both Google paths end the same way: a profile enters the session, no profile asks for one.
    private DataCallback<User> googleSignInCallback() {
        return new DataCallback<User>() {
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
        };
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

    // Re-authenticates with current password then updates to next password.
    public void changePassword(String current, String next) {
        if (current == null || current.isEmpty()) {
            error.setValue("Vui lòng nhập mật khẩu hiện tại.");
            return;
        }
        if (next == null || next.isEmpty()) {
            error.setValue("Vui lòng nhập mật khẩu mới.");
            return;
        }
        if (!isPasswordValid(next)) {
            error.setValue("Mật khẩu cần ít nhất 8 ký tự, gồm chữ và số.");
            return;
        }
        if (current.equals(next)) {
            error.setValue("Mật khẩu mới không được trùng với mật khẩu hiện tại.");
            return;
        }
        loading.setValue(true);
        error.setValue(null);
        done.setValue(false);
        authRepository.changePassword(current, next, new DataCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                loading.postValue(false);
                done.postValue(true);
            }

            @Override
            public void onError(String message) {
                loading.postValue(false);
                String errMsg = message != null ? message : "Không thể đổi mật khẩu.";
                if (errMsg.contains("Email hoặc mật khẩu không đúng")) {
                    errMsg = "Mật khẩu hiện tại không đúng.";
                }
                error.postValue(errMsg);
            }
        });
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
