package com.nhom3.pubgapp.viewmodel;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.nhom3.pubgapp.model.ChatMessage;
import com.nhom3.pubgapp.model.User;
import com.nhom3.pubgapp.model.enums.UserStatus;
import com.nhom3.pubgapp.model.enums.VerificationStatus;

import org.junit.Test;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

// Tests the guard rules and the message merging of ChatViewModel without Firebase.
public class ChatViewModelTest {

    @Test
    public void canUserBuy_allowsOnlyApprovedAndEmailVerifiedAndActive() {
        User user = new User();
        user.setStatus(UserStatus.ACTIVE);
        user.setVerificationStatus(VerificationStatus.APPROVED);

        assertTrue(ChatViewModel.canUserBuy(user, true));
        assertFalse(ChatViewModel.canUserBuy(user, false));

        user.setVerificationStatus(VerificationStatus.PENDING);
        assertFalse(ChatViewModel.canUserBuy(user, true));

        user.setVerificationStatus(VerificationStatus.REJECTED);
        assertFalse(ChatViewModel.canUserBuy(user, true));

        user.setVerificationStatus(VerificationStatus.NONE);
        assertFalse(ChatViewModel.canUserBuy(user, true));

        user.setVerificationStatus(VerificationStatus.APPROVED);
        user.setStatus(UserStatus.TEMP_LOCKED);
        assertFalse(ChatViewModel.canUserBuy(user, true));

        user.setStatus(UserStatus.DELETED);
        assertFalse(ChatViewModel.canUserBuy(user, true));

        assertFalse(ChatViewModel.canUserBuy(null, true));
    }

    @Test
    public void canUserSell_allowsOnlyApprovedAndEmailVerifiedAndActive() {
        User user = new User();
        user.setStatus(UserStatus.ACTIVE);
        user.setVerificationStatus(VerificationStatus.APPROVED);

        assertTrue(ChatViewModel.canUserSell(user, true));
        assertFalse(ChatViewModel.canUserSell(user, false));

        user.setVerificationStatus(VerificationStatus.PENDING);
        assertFalse(ChatViewModel.canUserSell(user, true));

        user.setStatus(UserStatus.TEMP_LOCKED);
        assertFalse(ChatViewModel.canUserSell(user, true));

        assertFalse(ChatViewModel.canUserSell(null, true));
    }

    @Test
    public void mergeNewer_replacesKnownMessageInPlaceAndAppendsNewOne() {
        Map<String, ChatMessage> byId = new LinkedHashMap<String, ChatMessage>();
        List<ChatMessage> first = new ArrayList<ChatMessage>();
        first.add(message("a", "hello"));
        first.add(message("b", "hi"));
        ChatViewModel.mergeNewer(byId, first);

        List<ChatMessage> second = new ArrayList<ChatMessage>();
        second.add(message("b", "hi edited"));
        second.add(message("c", "new"));
        List<ChatMessage> merged = ChatViewModel.mergeNewer(byId, second);

        assertEquals(3, merged.size());
        assertEquals("a", merged.get(0).getId());
        assertEquals("hi edited", merged.get(1).getText());
        assertEquals("c", merged.get(2).getId());
    }

    @Test
    public void mergeOlder_putsOlderPageFirstWithoutDuplicates() {
        Map<String, ChatMessage> byId = new LinkedHashMap<String, ChatMessage>();
        List<ChatMessage> latest = new ArrayList<ChatMessage>();
        latest.add(message("c", "third"));
        latest.add(message("d", "fourth"));
        ChatViewModel.mergeNewer(byId, latest);

        List<ChatMessage> older = new ArrayList<ChatMessage>();
        older.add(message("a", "first"));
        older.add(message("b", "second"));
        older.add(message("c", "third"));
        List<ChatMessage> merged = ChatViewModel.mergeOlder(byId, older);

        assertEquals(4, merged.size());
        assertEquals("a", merged.get(0).getId());
        assertEquals("b", merged.get(1).getId());
        assertEquals("c", merged.get(2).getId());
        assertEquals("d", merged.get(3).getId());
        assertEquals(4, byId.size());
    }

    private static ChatMessage message(String id, String text) {
        ChatMessage message = new ChatMessage();
        message.setId(id);
        message.setText(text);
        return message;
    }
}
