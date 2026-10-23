package com.nhom3.pubgapp.data;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import java.util.HashMap;
import java.util.Map;

public abstract class BaseRepository {

    private static final String TAG = "BaseRepository";

    private static final String COLLECTION_ACTIVITY_LOGS = "activityLogs";
    private static final String FIELD_USER_ID = "userId";
    private static final String FIELD_ACTION = "action";
    private static final String FIELD_TARGET_TYPE = "targetType";
    private static final String FIELD_TARGET_ID = "targetId";
    private static final String FIELD_CREATED_AT = "createdAt";

    private static final String MESSAGE_PERMISSION_DENIED =
            "Bạn không có quyền thực hiện thao tác này."
                    + " Hãy đăng nhập lại hoặc liên hệ quản trị viên.";
    private static final String MESSAGE_UNAVAILABLE =
            "Không kết nối được tới máy chủ. Hãy kiểm tra mạng rồi thử lại.";
    private static final String MESSAGE_NOT_FOUND =
            "Không tìm thấy dữ liệu. Có thể nó đã bị xóa, hãy làm mới màn hình.";
    private static final String MESSAGE_FAILED_PRECONDITION =
            "Dữ liệu chưa sẵn sàng cho thao tác này, có thể thiếu chỉ mục Firestore."
                    + " Hãy báo cho nhóm phát triển.";
    private static final String MESSAGE_UNAUTHENTICATED =
            "Phiên đăng nhập đã hết hạn. Hãy đăng nhập lại.";
    private static final String MESSAGE_ABORTED =
            "Thao tác bị gián đoạn vì dữ liệu vừa thay đổi. Hãy thử lại.";
    private static final String MESSAGE_AUTH_INVALID_CREDENTIAL =
            "Email hoặc mật khẩu không đúng. Hãy kiểm tra lại rồi thử lại.";
    private static final String MESSAGE_AUTH_INVALID_USER =
            "Tài khoản không tồn tại hoặc đã bị vô hiệu hóa. Hãy liên hệ quản trị viên.";
    private static final String MESSAGE_AUTH_COLLISION =
            "Email này đã được dùng cho một tài khoản khác. Hãy dùng email khác hoặc đăng nhập.";
    private static final String MESSAGE_AUTH_WEAK_PASSWORD =
            "Mật khẩu quá yếu. Hãy chọn mật khẩu dài hơn và khó đoán hơn.";
    private static final String MESSAGE_AUTH_RECENT_LOGIN =
            "Thao tác này cần đăng nhập lại. Hãy nhập lại mật khẩu rồi thử lại.";
    private static final String MESSAGE_TOO_MANY_REQUESTS =
            "Bạn thao tác quá nhiều lần. Hãy đợi một lúc rồi thử lại.";
    private static final String MESSAGE_AUTH_GENERIC =
            "Không xác thực được tài khoản. Hãy đăng nhập lại.";
    private static final String MESSAGE_GENERIC =
            "Đã có lỗi xảy ra. Hãy thử lại sau ít phút.";

    protected FirebaseFirestore db = FirebaseFirestore.getInstance();
    protected FirebaseAuth auth = FirebaseAuth.getInstance();

    protected String currentUserId() {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            return "";
        }
        return user.getUid();
    }

    // Fire and forget: a failed log must never break the action that was already done.
    protected void logActivity(String action, String targetType, String targetId) {
        String userId = currentUserId();
        if (userId.isEmpty()) {
            return;
        }
        Map<String, Object> entry = new HashMap<String, Object>();
        entry.put(FIELD_USER_ID, userId);
        entry.put(FIELD_ACTION, action);
        entry.put(FIELD_TARGET_TYPE, targetType);
        entry.put(FIELD_TARGET_ID, targetId);
        entry.put(FIELD_CREATED_AT, FieldValue.serverTimestamp());
        db.collection(COLLECTION_ACTIVITY_LOGS).add(entry).addOnFailureListener(
                new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception error) {
                        Log.w(TAG, "Could not write the activity log", error);
                    }
                });
    }

    protected void fail(DataCallback<?> cb, Exception error) {
        Log.w(TAG, "Operation failed", error);
        cb.onError(describeError(error));
    }

    private String describeError(Exception error) {
        if (error instanceof FirebaseFirestoreException) {
            return describeFirestoreError((FirebaseFirestoreException) error);
        }
        if (error instanceof FirebaseAuthException) {
            return describeAuthError((FirebaseAuthException) error);
        }
        if (error instanceof FirebaseNetworkException) {
            return MESSAGE_UNAVAILABLE;
        }
        if (error instanceof FirebaseTooManyRequestsException) {
            return MESSAGE_TOO_MANY_REQUESTS;
        }
        return MESSAGE_GENERIC;
    }

    private String describeFirestoreError(FirebaseFirestoreException error) {
        switch (error.getCode()) {
            case PERMISSION_DENIED:
                return MESSAGE_PERMISSION_DENIED;
            case UNAVAILABLE:
            case DEADLINE_EXCEEDED:
                return MESSAGE_UNAVAILABLE;
            case NOT_FOUND:
                return MESSAGE_NOT_FOUND;
            case FAILED_PRECONDITION:
                return MESSAGE_FAILED_PRECONDITION;
            case UNAUTHENTICATED:
                return MESSAGE_UNAUTHENTICATED;
            case ABORTED:
                return MESSAGE_ABORTED;
            default:
                return MESSAGE_GENERIC;
        }
    }

    private String describeAuthError(FirebaseAuthException error) {
        if (error instanceof FirebaseAuthInvalidCredentialsException) {
            return MESSAGE_AUTH_INVALID_CREDENTIAL;
        }
        if (error instanceof FirebaseAuthInvalidUserException) {
            return MESSAGE_AUTH_INVALID_USER;
        }
        if (error instanceof FirebaseAuthUserCollisionException) {
            return MESSAGE_AUTH_COLLISION;
        }
        if (error instanceof FirebaseAuthWeakPasswordException) {
            return MESSAGE_AUTH_WEAK_PASSWORD;
        }
        if (error instanceof FirebaseAuthRecentLoginRequiredException) {
            return MESSAGE_AUTH_RECENT_LOGIN;
        }
        return MESSAGE_AUTH_GENERIC;
    }
}
