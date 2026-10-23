package com.nhom3.pubgapp.viewmodel;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.nhom3.pubgapp.model.User;
import com.nhom3.pubgapp.model.enums.VerificationStatus;

import org.junit.Test;

// Tests user verification guard rules in ChatViewModel without Firebase.
public class ChatViewModelTest {

    @Test
    public void canUserBuy_allowsOnlyApprovedAndEmailVerified() {
        User user = new User();
        user.setVerificationStatus(VerificationStatus.APPROVED);

        assertTrue(ChatViewModel.canUserBuy(user, true));
        assertFalse(ChatViewModel.canUserBuy(user, false));

        user.setVerificationStatus(VerificationStatus.PENDING);
        assertFalse(ChatViewModel.canUserBuy(user, true));

        user.setVerificationStatus(VerificationStatus.REJECTED);
        assertFalse(ChatViewModel.canUserBuy(user, true));

        user.setVerificationStatus(VerificationStatus.NONE);
        assertFalse(ChatViewModel.canUserBuy(user, true));

        assertFalse(ChatViewModel.canUserBuy(null, true));
    }

    @Test
    public void canUserSell_allowsOnlyApprovedAndEmailVerified() {
        User user = new User();
        user.setVerificationStatus(VerificationStatus.APPROVED);

        assertTrue(ChatViewModel.canUserSell(user, true));
        assertFalse(ChatViewModel.canUserSell(user, false));

        user.setVerificationStatus(VerificationStatus.PENDING);
        assertFalse(ChatViewModel.canUserSell(user, true));

        assertFalse(ChatViewModel.canUserSell(null, true));
    }
}
