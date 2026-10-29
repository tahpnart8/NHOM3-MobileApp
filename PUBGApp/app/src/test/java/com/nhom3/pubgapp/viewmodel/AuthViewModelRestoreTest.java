package com.nhom3.pubgapp.viewmodel;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class AuthViewModelRestoreTest {

    @Test
    public void decideRestoreFailure_networkError_keepsSignedIn() {
        AuthViewModel.RestoreFailure failure =
                AuthViewModel.decideRestoreFailure("Connection reset", true, false);
        assertEquals(AuthViewModel.RestoreFailure.KEEP_SIGNED_IN, failure);
    }

    @Test
    public void decideRestoreFailure_nullMessage_keepsSignedIn() {
        AuthViewModel.RestoreFailure failure = AuthViewModel.decideRestoreFailure(null, false, true);
        assertEquals(AuthViewModel.RestoreFailure.KEEP_SIGNED_IN, failure);
    }

    @Test
    public void decideRestoreFailure_disabledAccount_signsOut() {
        AuthViewModel.RestoreFailure failure = AuthViewModel.decideRestoreFailure(
                "Tài khoản không tồn tại hoặc đã bị vô hiệu hóa.", true, true);
        assertEquals(AuthViewModel.RestoreFailure.SIGN_OUT, failure);
    }

    @Test
    public void decideRestoreFailure_unverifiedEmailAccount_waitsForVerification() {
        AuthViewModel.RestoreFailure failure =
                AuthViewModel.decideRestoreFailure("Connection reset", false, false);
        assertEquals(AuthViewModel.RestoreFailure.WAIT_FOR_VERIFICATION, failure);
    }
}
