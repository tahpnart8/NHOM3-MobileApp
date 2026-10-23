package com.nhom3.pubgapp;

import com.nhom3.pubgapp.model.AppNotification;
import com.nhom3.pubgapp.model.ProfileStats;
import com.nhom3.pubgapp.model.UserAddress;
import com.nhom3.pubgapp.model.enums.NotificationType;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class UserAddressTest {

    @Test
    public void testGetFullText() {
        UserAddress address = new UserAddress();
        address.setDetail("123 Nguyễn Văn Cừ");
        address.setDistrict("Quận 5");
        address.setProvince("TP. Hồ Chí Minh");
        assertEquals("123 Nguyễn Văn Cừ, Quận 5, TP. Hồ Chí Minh", address.getFullText());
    }

    @Test
    public void testGetFullTextPartial() {
        UserAddress address = new UserAddress();
        address.setDetail("Phường 4");
        address.setProvince("TP. Hồ Chí Minh");
        assertEquals("Phường 4, TP. Hồ Chí Minh", address.getFullText());
    }

    @Test
    public void testProfileStatsCompletionRate() {
        ProfileStats stats = new ProfileStats();
        assertEquals(0.0, stats.completionRate(), 0.001);

        stats.setDealCount(10);
        stats.setCompletedDealCount(8);
        assertEquals(0.8, stats.completionRate(), 0.001);
    }

    @Test
    public void testAppNotificationMarkRead() {
        AppNotification notification = new AppNotification();
        notification.setType(NotificationType.DEAL_UPDATE);
        notification.setTitle("Thông báo");
        notification.setBody("Nội dung");
        assertFalse(notification.isRead());

        notification.markRead();
        assertTrue(notification.isRead());
    }
}
