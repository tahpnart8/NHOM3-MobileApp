package com.nhom3.pubgapp.ui.notification;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import com.google.firebase.Timestamp;
import com.nhom3.pubgapp.R;
import com.nhom3.pubgapp.model.AppNotification;
import com.nhom3.pubgapp.ui.main.MainActivity;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

// Shows a system notification for each new unread notice while the app is open.
// No push when the app is closed: that needs Cloud Functions, which the Spark plan does not have.
public class InAppNotifier {

    static final String CHANNEL_ID = "pubg_new_notices";
    private static final String PREFS_NAME = "in_app_notifier";
    private static final String KEY_PERMISSION_ASKED = "permissionAsked";

    private final Context appContext;
    // Ids already handled for the watched account, so a notice pops up once only.
    private final Set<String> seenIds = new HashSet<String>();
    private String watchedUid;
    private long watchStartMillis;

    public InAppNotifier(Context context) {
        this.appContext = context.getApplicationContext();
        createChannel();
    }

    // Called with every list the live listener delivers for the signed-in account.
    public void onNotificationsChanged(String uid, List<AppNotification> list) {
        if (uid == null || list == null) {
            return;
        }
        if (!uid.equals(watchedUid)) {
            startWatching(uid);
        }
        List<AppNotification> fresh = takeNew(list, uid, seenIds, watchStartMillis);
        for (AppNotification item : fresh) {
            show(item);
        }
    }

    // A new account (or a recreated screen) starts clean: only notices created from now on pop up.
    private void startWatching(String uid) {
        watchedUid = uid;
        watchStartMillis = System.currentTimeMillis();
        seenIds.clear();
    }

    // Pops up only notices not seen before, unread, for this account and created after watching began.
    // Every id is remembered, so an old or read notice never pops up later.
    static List<AppNotification> takeNew(List<AppNotification> list, String uid,
            Set<String> seenIds, long sinceMillis) {
        List<AppNotification> fresh = new ArrayList<AppNotification>();
        for (AppNotification item : list) {
            String id = item.getId();
            if (id == null || seenIds.contains(id)) {
                continue;
            }
            seenIds.add(id);
            if (isNewForUser(item, uid, sinceMillis)) {
                fresh.add(item);
            }
        }
        return fresh;
    }

    private static boolean isNewForUser(AppNotification item, String uid, long sinceMillis) {
        if (item.isRead() || !uid.equals(item.getRecipientId())) {
            return false;
        }
        Timestamp createdAt = item.getCreatedAt();
        if (createdAt == null) {
            return false;
        }
        return createdAt.toDate().getTime() >= sinceMillis;
    }

    // Android 13+ asks the user once; after a refusal we do not ask again, the bell still works.
    public boolean shouldAskPermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            return false;
        }
        if (hasPermission()) {
            return false;
        }
        SharedPreferences prefs = appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return !prefs.getBoolean(KEY_PERMISSION_ASKED, false);
    }

    public void rememberPermissionAsked() {
        SharedPreferences prefs = appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putBoolean(KEY_PERMISSION_ASKED, true).apply();
    }

    private boolean hasPermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            return true;
        }
        int state = ContextCompat.checkSelfPermission(appContext, Manifest.permission.POST_NOTIFICATIONS);
        return state == PackageManager.PERMISSION_GRANTED;
    }

    // One channel for every notice type; Android 8+ shows nothing without a channel.
    private void createChannel() {
        NotificationChannel channel = new NotificationChannel(CHANNEL_ID,
                appContext.getString(R.string.notification_channel_name),
                NotificationManager.IMPORTANCE_DEFAULT);
        channel.setDescription(appContext.getString(R.string.notification_channel_description));
        NotificationManager manager = appContext.getSystemService(NotificationManager.class);
        if (manager != null) {
            manager.createNotificationChannel(channel);
        }
    }

    private void show(AppNotification item) {
        // Without the permission the notice still shows in the bell and the list, just not as a pop-up.
        if (!hasPermission()) {
            return;
        }
        NotificationCompat.Builder builder = new NotificationCompat.Builder(appContext, CHANNEL_ID);
        builder.setSmallIcon(R.drawable.ic_notification_small);
        builder.setContentTitle(item.getTitle());
        builder.setContentText(item.getBody());
        builder.setStyle(new NotificationCompat.BigTextStyle().bigText(item.getBody()));
        builder.setPriority(NotificationCompat.PRIORITY_DEFAULT);
        builder.setContentIntent(buildOpenAppIntent());
        builder.setAutoCancel(true);
        try {
            NotificationManagerCompat.from(appContext).notify(item.getId().hashCode(), builder.build());
        } catch (SecurityException e) {
            // The user can revoke the permission at any moment; a missed pop-up is harmless.
        }
    }

    // A tap brings the running MainActivity forward instead of starting a second one.
    private PendingIntent buildOpenAppIntent() {
        Intent open = new Intent(appContext, MainActivity.class);
        open.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP
                | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        return PendingIntent.getActivity(appContext, 0, open,
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
    }
}
