package com.nhom3.pubgapp.viewmodel;

import static org.junit.Assert.assertEquals;

import com.google.firebase.Timestamp;
import com.nhom3.pubgapp.model.User;
import com.nhom3.pubgapp.model.enums.UserStatus;

import org.junit.Test;

import java.util.Date;

public class AuthViewModelTest {

    @Test
    public void evaluateUserStatus_nullUser_returnsDeleted() {
        AuthViewModel.LoginDecision decision = AuthViewModel.evaluateUserStatus(null);
        assertEquals(AuthViewModel.LoginDecision.DELETED, decision);
    }

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
}
