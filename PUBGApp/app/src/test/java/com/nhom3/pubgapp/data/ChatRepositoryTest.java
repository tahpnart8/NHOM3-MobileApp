package com.nhom3.pubgapp.data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.nhom3.pubgapp.model.enums.ListingStatus;

import org.junit.Test;

// Tests pure validation rules for conversation creation in ChatRepository without Firebase.
public class ChatRepositoryTest {

    @Test
    public void buildConversationId_concatenatesListingAndBuyer() {
        String id = ChatRepository.buildConversationId("list123", "buyer456");
        assertEquals("list123_buyer456", id);
    }

    @Test
    public void validateNewConversation_rejectsSameUser() {
        String error = ChatRepository.validateNewConversation("user1", "user1", ListingStatus.AVAILABLE);
        assertNotNull(error);
        assertEquals("Không thể nhắn tin cho chính mình.", error);
    }

    @Test
    public void validateNewConversation_rejectsEmptyBuyer() {
        String error = ChatRepository.validateNewConversation("", "seller1", ListingStatus.AVAILABLE);
        assertNotNull(error);
        assertEquals("Vui lòng đăng nhập để nhắn tin.", error);
    }

    @Test
    public void validateNewConversation_rejectsNonAvailableStatus() {
        String errorSold = ChatRepository.validateNewConversation("buyer1", "seller1", ListingStatus.SOLD);
        assertNotNull(errorSold);
        assertEquals("Tin đăng không còn hoạt động.", errorSold);

        String errorDraft = ChatRepository.validateNewConversation("buyer1", "seller1", ListingStatus.DRAFT);
        assertNotNull(errorDraft);
        assertEquals("Tin đăng không còn hoạt động.", errorDraft);
    }

    @Test
    public void validateNewConversation_acceptsAvailableAndReserved() {
        String resultAvail = ChatRepository.validateNewConversation("buyer1", "seller1", ListingStatus.AVAILABLE);
        assertNull(resultAvail);

        String resultReserved = ChatRepository.validateNewConversation("buyer1", "seller1", ListingStatus.RESERVED);
        assertNull(resultReserved);
    }

    @Test
    public void isListingStatusAllowed_returnsTrueOnlyForAvailableAndReserved() {
        assertTrue(ChatRepository.isListingStatusAllowed(ListingStatus.AVAILABLE));
        assertTrue(ChatRepository.isListingStatusAllowed(ListingStatus.RESERVED));
        assertFalse(ChatRepository.isListingStatusAllowed(ListingStatus.DRAFT));
        assertFalse(ChatRepository.isListingStatusAllowed(ListingStatus.SOLD));
        assertFalse(ChatRepository.isListingStatusAllowed(ListingStatus.HIDDEN));
        assertFalse(ChatRepository.isListingStatusAllowed(ListingStatus.REMOVED));
    }
}
