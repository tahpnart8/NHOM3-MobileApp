package com.nhom3.pubgapp.data;

import android.app.Activity;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuthWebException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GetTokenResult;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.OAuthProvider;
import com.google.firebase.auth.UserInfo;
import com.google.firebase.firestore.DocumentSnapshot;
import com.nhom3.pubgapp.model.User;

import java.util.List;

public class AuthRepository extends BaseRepository {

    private static final String COLLECTION_USERS = "users";
    // Shown when the browser page is closed or the sign-in cannot finish there.
    private static final String MESSAGE_GOOGLE_BROWSER_STOPPED =
            "Đăng nhập Google chưa hoàn tất. Hãy thử lại.";

    public void signUp(String email, String password, final DataCallback<String> cb) {
        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (!task.isSuccessful()) {
                            fail(cb, task.getException());
                            return;
                        }
                        FirebaseUser firebaseUser = auth.getCurrentUser();
                        if (firebaseUser != null) {
                            firebaseUser.sendEmailVerification();
                            cb.onSuccess(firebaseUser.getUid());
                        } else {
                            cb.onError("Không lấy được thông tin tài khoản vừa tạo.");
                        }
                    }
                });
    }

    public void sendVerificationEmail(final DataCallback<Void> cb) {
        FirebaseUser firebaseUser = auth.getCurrentUser();
        if (firebaseUser == null) {
            cb.onError("Chưa đăng nhập.");
            return;
        }
        firebaseUser.sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    cb.onSuccess(null);
                } else {
                    fail(cb, task.getException());
                }
            }
        });
    }

    public void signIn(String email, String password, final DataCallback<User> cb) {
        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (!task.isSuccessful()) {
                            fail(cb, task.getException());
                            return;
                        }
                        FirebaseUser firebaseUser = auth.getCurrentUser();
                        if (firebaseUser == null) {
                            cb.onError("Không tìm thấy phiên đăng nhập.");
                            return;
                        }
                        fetchUserProfile(firebaseUser.getUid(), cb);
                    }
                });
    }

    public void signInWithGoogle(String idToken, final DataCallback<User> cb) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        auth.signInWithCredential(credential)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (!task.isSuccessful()) {
                            fail(cb, task.getException());
                            return;
                        }
                        FirebaseUser firebaseUser = auth.getCurrentUser();
                        if (firebaseUser == null) {
                            cb.onError("Không tìm thấy phiên đăng nhập.");
                            return;
                        }
                        fetchUserProfile(firebaseUser.getUid(), cb);
                    }
                });
    }

    // Opens Google's own sign-in page in the browser, so the device needs no Google account of its own.
    public void signInWithGoogleBrowser(Activity activity, final DataCallback<User> cb) {
        OAuthProvider.Builder provider = OAuthProvider.newBuilder("google.com");
        // Always show the account chooser, even when the browser already knows an account.
        provider.addCustomParameter("prompt", "select_account");
        auth.startActivityForSignInWithProvider(activity, provider.build())
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (!task.isSuccessful()) {
                            if (task.getException() instanceof FirebaseAuthWebException) {
                                cb.onError(MESSAGE_GOOGLE_BROWSER_STOPPED);
                                return;
                            }
                            fail(cb, task.getException());
                            return;
                        }
                        FirebaseUser firebaseUser = auth.getCurrentUser();
                        if (firebaseUser == null) {
                            cb.onError("Không tìm thấy phiên đăng nhập.");
                            return;
                        }
                        fetchUserProfile(firebaseUser.getUid(), cb);
                    }
                });
    }

    private void fetchUserProfile(String uid, final DataCallback<User> cb) {
        db.collection(COLLECTION_USERS).document(uid).get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (!task.isSuccessful()) {
                            fail(cb, task.getException());
                            return;
                        }
                        DocumentSnapshot doc = task.getResult();
                        if (doc != null && doc.exists()) {
                            User user = doc.toObject(User.class);
                            cb.onSuccess(user);
                        } else {
                            cb.onSuccess(null);
                        }
                    }
                });
    }

    public void sendPasswordReset(String email, final DataCallback<Void> cb) {
        auth.sendPasswordResetEmail(email)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            cb.onSuccess(null);
                        } else {
                            fail(cb, task.getException());
                        }
                    }
                });
    }

    public void changePassword(String current, final String next, final DataCallback<Void> cb) {
        final FirebaseUser firebaseUser = auth.getCurrentUser();
        if (firebaseUser == null || firebaseUser.getEmail() == null) {
            cb.onError("Chưa đăng nhập.");
            return;
        }
        AuthCredential credential = EmailAuthProvider.getCredential(firebaseUser.getEmail(), current);
        firebaseUser.reauthenticate(credential).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> reauthTask) {
                if (!reauthTask.isSuccessful()) {
                    fail(cb, reauthTask.getException());
                    return;
                }
                firebaseUser.updatePassword(next).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> updateTask) {
                        if (updateTask.isSuccessful()) {
                            cb.onSuccess(null);
                        } else {
                            fail(cb, updateTask.getException());
                        }
                    }
                });
            }
        });
    }

    public String getCurrentUserId() {
        return currentUserId();
    }

    public boolean isGoogleAccount() {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            return false;
        }
        List<? extends UserInfo> providerData = user.getProviderData();
        for (UserInfo info : providerData) {
            if (GoogleAuthProvider.PROVIDER_ID.equals(info.getProviderId())) {
                return true;
            }
        }
        return false;
    }

    public void deleteLogin(final DataCallback<Void> cb) {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            cb.onError("Chưa đăng nhập.");
            return;
        }
        user.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    cb.onSuccess(null);
                } else {
                    fail(cb, task.getException());
                }
            }
        });
    }

    public void reauthenticate(String password, final DataCallback<Void> cb) {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null || user.getEmail() == null) {
            cb.onError("Chưa đăng nhập.");
            return;
        }
        AuthCredential credential = EmailAuthProvider.getCredential(user.getEmail(), password);
        user.reauthenticate(credential).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    cb.onSuccess(null);
                } else {
                    fail(cb, task.getException());
                }
            }
        });
    }

    public void reauthenticateWithGoogle(String idToken, final DataCallback<Void> cb) {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            cb.onError("Chưa đăng nhập.");
            return;
        }
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        user.reauthenticate(credential).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    cb.onSuccess(null);
                } else {
                    fail(cb, task.getException());
                }
            }
        });
    }

    public void reloadUser(final DataCallback<Boolean> cb) {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            cb.onError("Chưa đăng nhập.");
            return;
        }
        user.reload().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (!task.isSuccessful()) {
                    fail(cb, task.getException());
                    return;
                }
                final FirebaseUser refreshed = auth.getCurrentUser();
                final boolean verified = refreshed != null && refreshed.isEmailVerified();
                if (verified) {
                    refreshed.getIdToken(true).addOnCompleteListener(
                            new OnCompleteListener<GetTokenResult>() {
                                @Override
                                public void onComplete(@NonNull Task<GetTokenResult> tokenTask) {
                                    cb.onSuccess(Boolean.valueOf(true));
                                }
                            }
                    );
                } else {
                    cb.onSuccess(Boolean.valueOf(false));
                }
            }
        });
    }

    public void signOut() {
        auth.signOut();
    }

    public boolean isEmailVerified() {
        FirebaseUser user = auth.getCurrentUser();
        return user != null && user.isEmailVerified();
    }
}
