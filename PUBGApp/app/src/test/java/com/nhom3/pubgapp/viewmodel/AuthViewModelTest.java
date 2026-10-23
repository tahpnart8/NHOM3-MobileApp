package com.nhom3.pubgapp.viewmodel;

import static org.junit.Assert.assertEquals;

import com.google.firebase.Timestamp;
import com.nhom3.pubgapp.model.User;
import com.nhom3.pubgapp.model.enums.UserStatus;

import org.junit.Test;

import java.util.Date;

public class AuthViewModelTest {

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
}
