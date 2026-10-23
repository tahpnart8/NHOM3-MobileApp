package com.nhom3.pubgapp.data.local;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import com.google.firebase.Timestamp;
import com.nhom3.pubgapp.model.Conversation;

import org.junit.Test;

import java.util.Date;

// Unit test for ConversationEntity Room mapping.
public class ConversationEntityTest {

    @Test
    public void fromModelAndToModel_preservesFields() {
        Conversation original = new Conversation();
        original.setId("listing123_buyer456");
        original.setListingId("listing123");
        original.setBuyerId("buyer456");
        original.setSellerId("seller789");
        original.setLastMessage("Xin chào bạn");
        Timestamp now = new Timestamp(new Date(1700000000000L));
        original.setLastMessageAt(now);

        ConversationEntity entity = new ConversationEntity().fromModel(original);
        Conversation converted = entity.toModel();

        assertEquals("listing123_buyer456", converted.getId());
        assertEquals("listing123", converted.getListingId());
        assertEquals("buyer456", converted.getBuyerId());
        assertEquals("seller789", converted.getSellerId());
        assertEquals("Xin chào bạn", converted.getLastMessage());
        assertNotNull(converted.getLastMessageAt());
        assertEquals(now.toDate().getTime(), converted.getLastMessageAt().toDate().getTime());
    }

    @Test
    public void fromModel_nullHandling() {
        ConversationEntity entity = new ConversationEntity();
        ConversationEntity result = entity.fromModel(null);
        assertNotNull(result);
        assertEquals("", result.getId());
    }
}
