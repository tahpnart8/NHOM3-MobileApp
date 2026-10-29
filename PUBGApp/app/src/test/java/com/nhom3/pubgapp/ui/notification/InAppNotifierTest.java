package com.nhom3.pubgapp.ui.notification;

import static org.junit.Assert.assertEquals;

import com.google.firebase.Timestamp;
import com.nhom3.pubgapp.model.AppNotification;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class InAppNotifierTest {

    private static final String UID = "userA";
    private static final long START = 1000000L;

    private static AppNotification notice(String id, String recipient, boolean read, long createdMillis) {
        AppNotification item = new AppNotification();
        item.setId(id);
        item.setRecipientId(recipient);
        item.setRead(read);
        item.setCreatedAt(new Timestamp(new Date(createdMillis)));
        return item;
    }

    @Test
    public void takeNew_keepsOnlyUnreadOwnNoticesCreatedAfterStart() {
        List<AppNotification> list = new ArrayList<AppNotification>();
        list.add(notice("new", UID, false, START + 10));
        list.add(notice("old", UID, false, START - 10));
        list.add(notice("read", UID, true, START + 10));
        list.add(notice("other", "userB", false, START + 10));
        Set<String> seen = new HashSet<String>();

        List<AppNotification> fresh = InAppNotifier.takeNew(list, UID, seen, START);

        assertEquals(1, fresh.size());
        assertEquals("new", fresh.get(0).getId());
        assertEquals(4, seen.size());
    }

    @Test
    public void takeNew_sameNoticeTwice_popsUpOnce() {
        List<AppNotification> list = new ArrayList<AppNotification>();
        list.add(notice("new", UID, false, START + 10));
        Set<String> seen = new HashSet<String>();

        InAppNotifier.takeNew(list, UID, seen, START);
        List<AppNotification> second = InAppNotifier.takeNew(list, UID, seen, START);

        assertEquals(0, second.size());
    }
}
