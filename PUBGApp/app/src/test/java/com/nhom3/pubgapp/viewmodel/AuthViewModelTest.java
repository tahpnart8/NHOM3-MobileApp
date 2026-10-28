package com.nhom3.pubgapp.viewmodel;

import static org.junit.Assert.assertEquals;

import com.google.firebase.Timestamp;
import com.nhom3.pubgapp.model.User;
import com.nhom3.pubgapp.model.enums.UserStatus;

import org.junit.Test;

import java.util.Date;

public class AuthViewModelTest {

    private static final String GOOGLE_PHOTO = "https://lh3.googleusercontent.com/a/abc=s96-c";

    @Test
    public void evaluateUserStatus_activeUser_returnsAllow() {
        User user = new User();
        user.setStatus(UserStatus.ACTIVE);

        AuthViewModel.LoginDecision decision = AuthViewModel.evaluateUserStatus(user);
        assertEquals(AuthViewModel.LoginDecision.ALLOW, decision);
    }

    @Test
    public void evaluateUserStatus_deletedUser_returnsDeleted() {
        User user = new User();
        user.setStatus(UserStatus.DELETED);

        AuthViewModel.LoginDecision decision = AuthViewModel.evaluateUserStatus(user);
        assertEquals(AuthViewModel.LoginDecision.DELETED, decision);
    }

    @Test
    public void evaluateUserStatus_permLockedUser_returnsLocked() {
        User user = new User();
        user.setStatus(UserStatus.PERM_LOCKED);
        user.setLockReason("Vi phạm quy định");

        AuthViewModel.LoginDecision decision = AuthViewModel.evaluateUserStatus(user);
        assertEquals(AuthViewModel.LoginDecision.LOCKED, decision);
    }

    @Test
    public void evaluateUserStatus_tempLockedNotExpired_returnsLocked() {
        User user = new User();
        user.setStatus(UserStatus.TEMP_LOCKED);
        // Lock expires 1 hour in the future
        Date futureDate = new Date(System.currentTimeMillis() + 3600 * 1000L);
        user.setLockedUntil(new Timestamp(futureDate));
        user.setLockReason("Khóa tạm 1 giờ");

        AuthViewModel.LoginDecision decision = AuthViewModel.evaluateUserStatus(user);
        assertEquals(AuthViewModel.LoginDecision.LOCKED, decision);
    }

    @Test
    public void evaluateUserStatus_tempLockedExpired_returnsClearExpiredLock() {
        User user = new User();
        user.setStatus(UserStatus.TEMP_LOCKED);
        // Lock expired 1 hour ago
        Date pastDate = new Date(System.currentTimeMillis() - 3600 * 1000L);
        user.setLockedUntil(new Timestamp(pastDate));

        AuthViewModel.LoginDecision decision = AuthViewModel.evaluateUserStatus(user);
        assertEquals(AuthViewModel.LoginDecision.CLEAR_EXPIRED_LOCK, decision);
    }

    @Test
    public void isPasswordValid_validPassword_returnsTrue() {
        assertEquals(true, AuthViewModel.isPasswordValid("Pass1234"));
    }

    @Test
    public void isPasswordValid_exactly8CharsWithLetterAndDigit_returnsTrue() {
        assertEquals(true, AuthViewModel.isPasswordValid("a1b2c3d4"));
    }

    @Test
    public void isPasswordValid_lessThan8Chars_returnsFalse() {
        assertEquals(false, AuthViewModel.isPasswordValid("Pass123"));
    }

    @Test
    public void isPasswordValid_onlyLetters_returnsFalse() {
        assertEquals(false, AuthViewModel.isPasswordValid("PasswordOnly"));
    }

    @Test
    public void isPasswordValid_onlyDigits_returnsFalse() {
        assertEquals(false, AuthViewModel.isPasswordValid("12345678"));
    }

    @Test
    public void isPasswordValid_emptyOrNull_returnsFalse() {
        assertEquals(false, AuthViewModel.isPasswordValid(""));
        assertEquals(false, AuthViewModel.isPasswordValid(null));
    }

    @Test
    public void isPasswordConfirmationMatching_matching_returnsTrue() {
        assertEquals(true, AuthViewModel.isPasswordConfirmationMatching("Pass1234", "Pass1234"));
    }

    @Test
    public void isPasswordConfirmationMatching_mismatch_returnsFalse() {
        assertEquals(false, AuthViewModel.isPasswordConfirmationMatching("Pass1234", "Pass9999"));
    }

    @Test
    public void isPasswordConfirmationMatching_null_returnsFalse() {
        assertEquals(false, AuthViewModel.isPasswordConfirmationMatching(null, "Pass1234"));
        assertEquals(false, AuthViewModel.isPasswordConfirmationMatching("Pass1234", null));
    }

    @Test
    public void isPhoneValid_valid10Digits_returnsTrue() {
        assertEquals(true, AuthViewModel.isPhoneValid("0901234567"));
    }

    @Test
    public void isPhoneValid_missingDigit_returnsFalse() {
        assertEquals(false, AuthViewModel.isPhoneValid("090123456"));
    }

    @Test
    public void isPhoneValid_extraDigit_returnsFalse() {
        assertEquals(false, AuthViewModel.isPhoneValid("09012345678"));
    }

    @Test
    public void isPhoneValid_notStartingWithZero_returnsFalse() {
        assertEquals(false, AuthViewModel.isPhoneValid("1901234567"));
    }

    @Test
    public void isPhoneValid_containsNonDigit_returnsFalse() {
        assertEquals(false, AuthViewModel.isPhoneValid("090123456a"));
    }

    @Test
    public void isPhoneValid_emptyOrNull_returnsFalse() {
        assertEquals(false, AuthViewModel.isPhoneValid(""));
        assertEquals(false, AuthViewModel.isPhoneValid(null));
    }

    @Test
    public void changePassword_samePassword_isDetected() {
        String current = "OldPass123";
        String next = "OldPass123";
        assertEquals(true, current.equals(next));
    }

    @Test
    public void changePassword_differentPassword_isValidated() {
        String current = "OldPass123";
        String next = "NewPass456";
        assertEquals(false, current.equals(next));
        assertEquals(true, AuthViewModel.isPasswordValid(next));
        assertEquals(true, AuthViewModel.isPasswordConfirmationMatching(next, "NewPass456"));
    }

    @Test
    public void changePassword_weakNewPassword_isRejected() {
        assertEquals(false, AuthViewModel.isPasswordValid("weak"));
        assertEquals(false, AuthViewModel.isPasswordValid("onlyletters"));
        assertEquals(false, AuthViewModel.isPasswordValid("12345678"));
    }

    @Test
    public void changePassword_confirmationMismatch_isRejected() {
        assertEquals(false, AuthViewModel.isPasswordConfirmationMatching("NewPass456", "OtherPass789"));
    }

    @Test
    public void needsGoogleAvatar_googleWithoutAvatar_isTrue() {
        assertEquals(true, AuthViewModel.needsGoogleAvatar(true, "", GOOGLE_PHOTO));
    }

    @Test
    public void needsGoogleAvatar_avatarAlreadySet_isFalse() {
        assertEquals(false, AuthViewModel.needsGoogleAvatar(true, "/9j/4AAQ", GOOGLE_PHOTO));
    }

    @Test
    public void needsGoogleAvatar_emailAccountOrNoPhoto_isFalse() {
        assertEquals(false, AuthViewModel.needsGoogleAvatar(false, null, GOOGLE_PHOTO));
        assertEquals(false, AuthViewModel.needsGoogleAvatar(true, null, null));
    }

    @Test
    public void sizeGooglePhotoUrl_replacesSizeSuffix() {
        String sized = AuthViewModel.sizeGooglePhotoUrl(GOOGLE_PHOTO, 256);
        assertEquals("https://lh3.googleusercontent.com/a/abc=s256-c", sized);
    }

    @Test
    public void sizeGooglePhotoUrl_withoutSuffix_isUnchanged() {
        String url = "https://lh3.googleusercontent.com/a=s/photo.jpg";
        assertEquals(url, AuthViewModel.sizeGooglePhotoUrl(url, 256));
    }

    @Test
    public void isInvalidAccountError_nullOrNetworkError_returnsFalse() {
        assertEquals(false, AuthViewModel.isInvalidAccountError(null));
        assertEquals(false, AuthViewModel.isInvalidAccountError("Không có kết nối mạng."));
        assertEquals(false, AuthViewModel.isInvalidAccountError("Connection reset"));
        assertEquals(false, AuthViewModel.isInvalidAccountError("Dịch vụ tạm thời gián đoạn."));
    }

    @Test
    public void isInvalidAccountError_disabledOrNotFoundUser_returnsTrue() {
        String errorMsg = "Tài khoản không tồn tại hoặc đã bị vô hiệu hóa."
                + " Hãy liên hệ quản trị viên.";
        assertEquals(true, AuthViewModel.isInvalidAccountError(errorMsg));
    }

    @Test
    public void isInvalidAccountError_notSignedIn_returnsTrue() {
        assertEquals(true, AuthViewModel.isInvalidAccountError("Chưa đăng nhập."));
    }
}
