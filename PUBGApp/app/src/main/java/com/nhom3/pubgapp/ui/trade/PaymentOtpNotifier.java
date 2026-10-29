package com.nhom3.pubgapp.ui.trade;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import com.nhom3.pubgapp.R;

import java.util.Locale;
import java.util.Random;

// Delivers the checkout OTP as a local notification. Simulated: the code is made on the phone, no SMS is sent.
public class PaymentOtpNotifier {

    private static final String CHANNEL_ID = "pubg_payment_otp";
    private static final int NOTIFICATION_ID = 3950;
    private static final int OTP_BOUND = 1000000;
    private static final int PHONE_DIGITS_SHOWN = 3;
    private static final String MASK = "••••";

    private final Context appContext;

    public PaymentOtpNotifier(@NonNull Context context) {
        this.appContext = context.getApplicationContext();
        createChannel();
    }

    /** A new random 6 digit code, with leading zeros kept. */
    @NonNull
    public static String generateCode(@NonNull Random random) {
        int value = random.nextInt(OTP_BOUND);
        return String.format(Locale.ROOT, "%06d", value);
    }

    /** "••••123" from the last digits of a phone number, or null when there are too few digits. */
    @Nullable
    public static String maskPhone(@Nullable String phone) {
        if (phone == null) {
            return null;
        }
        String digits = phone.replaceAll("[^0-9]", "");
        if (digits.length() < PHONE_DIGITS_SHOWN) {
            return null;
        }
        return MASK + digits.substring(digits.length() - PHONE_DIGITS_SHOWN);
    }

    /** Shows the code as a notification. False means it could not, so the caller shows it on screen. */
    public boolean send(@NonNull String code) {
        if (!hasPermission()) {
            return false;
        }
        String text = appContext.getString(R.string.trade_checkout_otp_notification_text, code);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(appContext, CHANNEL_ID);
        builder.setSmallIcon(R.drawable.ic_notification_small);
        builder.setContentTitle(appContext.getString(R.string.trade_checkout_otp_notification_title));
        builder.setContentText(text);
        builder.setPriority(NotificationCompat.PRIORITY_HIGH);
        builder.setCategory(NotificationCompat.CATEGORY_MESSAGE);
        builder.setAutoCancel(true);
        try {
            NotificationManagerCompat.from(appContext).notify(NOTIFICATION_ID, builder.build());
            return true;
        } catch (SecurityException e) {
            return false;
        }
    }

    // Before Android 13 there is no runtime permission, only the user's switch in Settings.
    private boolean hasPermission() {
        boolean enabled = NotificationManagerCompat.from(appContext).areNotificationsEnabled();
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            return enabled;
        }
        int state = ContextCompat.checkSelfPermission(appContext, Manifest.permission.POST_NOTIFICATIONS);
        return enabled && state == PackageManager.PERMISSION_GRANTED;
    }

    // High importance so the code pops up over the checkout, like a bank OTP message.
    private void createChannel() {
        NotificationChannel channel = new NotificationChannel(CHANNEL_ID,
                appContext.getString(R.string.trade_checkout_otp_channel_name),
                NotificationManager.IMPORTANCE_HIGH);
        channel.setDescription(appContext.getString(R.string.trade_checkout_otp_channel_description));
        NotificationManager manager = appContext.getSystemService(NotificationManager.class);
        if (manager != null) {
            manager.createNotificationChannel(channel);
        }
    }
}
