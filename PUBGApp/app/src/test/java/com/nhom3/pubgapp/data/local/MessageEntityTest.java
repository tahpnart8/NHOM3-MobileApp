package com.nhom3.pubgapp.data.local;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import com.google.firebase.Timestamp;
import com.nhom3.pubgapp.model.ChatMessage;
import com.nhom3.pubgapp.model.enums.MessageType;

import org.junit.Test;

import java.util.Date;

// Unit test for MessageEntity Room mapping.
public class MessageEntityTest {

    @Test
    public void fromModelAndToModel_preservesFields() {
        ChatMessage message = new ChatMessage();
        message.setId("msg_123");
        message.setConversationId("conv_456");
        message.setSenderId("user_789");
        message.setType(MessageType.TEXT);
        message.setText("Xin chào!");
        message.setImageId("img_001");
        message.setOfferId("offer_002");
        Timestamp now = new Timestamp(new Date(1700000000000L));
        message.setCreatedAt(now);

        MessageEntity entity = new MessageEntity().fromModel(message);
        ChatMessage converted = entity.toModel();

        assertEquals("msg_123", converted.getId());
        assertEquals("conv_456", converted.getConversationId());
        assertEquals("user_789", converted.getSenderId());
        assertEquals(MessageType.TEXT, converted.getType());
        assertEquals("Xin chào!", converted.getText());
        assertEquals("img_001", converted.getImageId());
        assertEquals("offer_002", converted.getOfferId());
        assertNotNull(converted.getCreatedAt());
        assertEquals(now.toDate().getTime(), converted.getCreatedAt().toDate().getTime());
    }

    @Test
    public void fromModel_nullHandling() {
        MessageEntity entity = new MessageEntity();
        MessageEntity result = entity.fromModel(null);
        assertNotNull(result);
        assertEquals("", result.getId());
    }
}
