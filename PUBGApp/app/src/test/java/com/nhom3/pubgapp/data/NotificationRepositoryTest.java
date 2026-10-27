package com.nhom3.pubgapp.data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.nhom3.pubgapp.model.AppNotification;
import com.nhom3.pubgapp.model.enums.NotificationType;

import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Unit tests for notification data truncation, default texts and validation rules.
public class NotificationRepositoryTest {

    @Test
    public void truncate_handlesNullAndShortStrings() {
        assertEquals("", NotificationRepository.truncate(null, 100));
        assertEquals("", NotificationRepository.truncate("", 100));
        assertEquals("hello", NotificationRepository.truncate("hello", 100));
    }

    @Test
    public void truncate_cutsLongStringsAtExactLimit() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 400; i++) {
            sb.append("A");
        }
        String string400 = sb.toString();

        String truncated100 = NotificationRepository.truncate(string400, 100);
        assertEquals(100, truncated100.length());

        String truncated300 = NotificationRepository.truncate(string400, 300);
        assertEquals(300, truncated300.length());
    }

    @Test
    public void getDefaultTitle_coversAllTenNotificationTypes() {
        NotificationType[] types = NotificationType.values();
        for (int i = 0; i < types.length; i++) {
            NotificationType type = types[i];
            String title = NotificationRepository.getDefaultTitle(type);
            assertNotNull(title);
            assertFalse(title.trim().isEmpty());
            assertTrue(title.length() <= NotificationRepository.MAX_TITLE_LENGTH);
        }
    }

    @Test
    public void getDefaultBody_coversAllTenNotificationTypes() {
        NotificationType[] types = NotificationType.values();
        for (int i = 0; i < types.length; i++) {
            NotificationType type = types[i];
            String body = NotificationRepository.getDefaultBody(type);
            assertNotNull(body);
            assertFalse(body.trim().isEmpty());
            assertTrue(body.length() <= NotificationRepository.MAX_BODY_LENGTH);
        }
    }

    @Test
    public void buildNotificationData_truncates400CharactersTitleAndBody() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 400; i++) {
            sb.append("X");
        }
        String text400 = sb.toString();

        AppNotification notification = new AppNotification();
        notification.setRecipientId("user_receiver_123");
        notification.setType(NotificationType.DEAL_UPDATE);
        notification.setTitle(text400);
        notification.setBody(text400);
        notification.setTargetId("deal_target_999");

        Map<String, Object> data = NotificationRepository.buildNotificationData(notification);

        assertEquals("user_receiver_123", data.get("recipientId"));
        assertEquals("DEAL_UPDATE", data.get("type"));
        assertEquals(Boolean.FALSE, data.get("isRead"));

        String titleResult = (String) data.get("title");
        assertNotNull(titleResult);
        assertEquals(100, titleResult.length());

        String bodyResult = (String) data.get("body");
        assertNotNull(bodyResult);
        assertEquals(300, bodyResult.length());

        String targetResult = (String) data.get("targetId");
        assertEquals("deal_target_999", targetResult);
    }

    @Test
    public void buildNotificationData_usesDefaultStringsWhenEmptyOrNull() {
        AppNotification notification = new AppNotification();
        notification.setRecipientId("user_1");
        notification.setType(NotificationType.NEW_MESSAGE);
        notification.setTitle(null);
        notification.setBody("   ");
        notification.setTargetId(null);

        Map<String, Object> data = NotificationRepository.buildNotificationData(notification);

        assertEquals("Tin nhắn mới", data.get("title"));
        assertEquals("Bạn có tin nhắn mới trong cuộc trò chuyện.", data.get("body"));
        assertEquals("", data.get("targetId"));
    }

    @Test
    public void buildNotificationData_preservesShortStrings() {
        AppNotification notification = new AppNotification();
        notification.setRecipientId("user_1");
        notification.setType(NotificationType.NEW_OFFER);
        notification.setTitle("Đề nghị giá iPhone");
        notification.setBody("Người mua đề nghị 5.000.000đ");
        notification.setTargetId("offer_123");

        Map<String, Object> data = NotificationRepository.buildNotificationData(notification);

        assertEquals("Đề nghị giá iPhone", data.get("title"));
        assertEquals("Người mua đề nghị 5.000.000đ", data.get("body"));
        assertEquals("offer_123", data.get("targetId"));
    }

    @Test
    public void validateNotification_rejectsNullNotificationAndRecipient() {
        assertEquals("Thông báo không hợp lệ.", NotificationRepository.validateNotification(null));

        AppNotification noRecipient = new AppNotification();
        noRecipient.setType(NotificationType.WARNING);
        assertEquals("Thông báo không hợp lệ.", NotificationRepository.validateNotification(noRecipient));

        AppNotification emptyRecipient = new AppNotification();
        emptyRecipient.setRecipientId("   ");
        emptyRecipient.setType(NotificationType.WARNING);
        assertEquals("Thông báo không hợp lệ.", NotificationRepository.validateNotification(emptyRecipient));
    }

    @Test
    public void validateNotification_rejectsNullType() {
        AppNotification noType = new AppNotification();
        noType.setRecipientId("user_123");
        noType.setType(null);

        assertEquals("Loại thông báo không hợp lệ.", NotificationRepository.validateNotification(noType));
    }

    @Test
    public void validateNotification_acceptsValidNotification() {
        AppNotification valid = new AppNotification();
        valid.setRecipientId("user_123");
        valid.setType(NotificationType.NEW_MESSAGE);

        org.junit.Assert.assertNull(NotificationRepository.validateNotification(valid));
    }

    @Test
    public void withoutDisabledTypes_hidesOnlyTheSwitchedOffTypes() {
        List<AppNotification> list = new ArrayList<AppNotification>();
        AppNotification message = new AppNotification();
        message.setType(NotificationType.NEW_MESSAGE);
        AppNotification offer = new AppNotification();
        offer.setType(NotificationType.NEW_OFFER);
        list.add(message);
        list.add(offer);

        Map<String, Object> prefs = new HashMap<String, Object>();
        prefs.put("NEW_MESSAGE", Boolean.FALSE);
        prefs.put("NEW_OFFER", Boolean.TRUE);

        List<AppNotification> kept = NotificationRepository.withoutDisabledTypes(list, prefs);
        assertEquals(1, kept.size());
        assertEquals(NotificationType.NEW_OFFER, kept.get(0).getType());
    }

    @Test
    public void withoutDisabledTypes_keepsEverythingWithoutPreferences() {
        List<AppNotification> list = new ArrayList<AppNotification>();
        AppNotification message = new AppNotification();
        message.setType(NotificationType.NEW_MESSAGE);
        list.add(message);

        assertEquals(1, NotificationRepository.withoutDisabledTypes(list, null).size());
        assertEquals(1, NotificationRepository.withoutDisabledTypes(list,
                new HashMap<String, Object>()).size());
    }
}
