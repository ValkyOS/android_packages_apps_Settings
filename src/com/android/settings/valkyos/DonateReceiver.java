/*
 * Copyright (C) 2026 The ValkyOS Project
 * SPDX-License-Identifier: Apache-2.0
 *
 * Donation reminder, same timing as crDroid: 30 minutes after boot, then
 * every hour until the donation screen is opened, then once a month.
 * Nothing is sent anywhere; the timing is kept on the device.
 */

package com.android.settings.valkyos;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.SystemClock;
import android.os.UserManager;
import android.util.Log;

import com.android.settings.R;

public class DonateReceiver extends BroadcastReceiver {

    private static final String TAG = "ValkyOSDonate";

    private static final String PREFS = "valkyos_donate";
    private static final String KEY_LAST_SEEN = "last_seen";

    private static final String CHANNEL_ID = "valkyos_donate";
    private static final int NOTIFICATION_ID = 8990;
    private static final int REQ_ALARM = 7612;

    private static final long COOLDOWN_MIN = 30L * 24 * 60; // monthly
    private static final long INITIAL_DELAY_MIN = 30;        // after boot
    private static final long REPEAT_DELAY_MIN = 60;         // until opened

    static final String ACTION_NUDGE = "com.android.settings.valkyos.action.DONATE_NUDGE";

    @Override
    public void onReceive(Context ctx, Intent intent) {
        final String action = intent != null ? intent.getAction() : null;
        if (action == null) return;

        UserManager um = ctx.getSystemService(UserManager.class);
        if (um == null || !um.isSystemUser()) return;
        if (!um.isUserUnlocked()) return;

        if (cooldownActive(ctx)) return;

        if (Intent.ACTION_BOOT_COMPLETED.equals(action)
                || Intent.ACTION_USER_UNLOCKED.equals(action)) {
            scheduleNext(ctx, INITIAL_DELAY_MIN);
        } else if (ACTION_NUDGE.equals(action)) {
            showNotification(ctx);
        }
    }

    private static SharedPreferences prefs(Context ctx) {
        return ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    /** Called when the donation screen is shown: next reminder in a month. */
    static void markSeen(Context ctx) {
        prefs(ctx).edit().putLong(KEY_LAST_SEEN, System.currentTimeMillis()).apply();
        scheduleNext(ctx, COOLDOWN_MIN);
    }

    private static boolean cooldownActive(Context ctx) {
        long last = prefs(ctx).getLong(KEY_LAST_SEEN, 0L);
        if (last <= 0) return false;
        long elapsedMin = (System.currentTimeMillis() - last) / 60_000L;
        if (elapsedMin >= 0 && elapsedMin < COOLDOWN_MIN) {
            scheduleNext(ctx, Math.max(1, COOLDOWN_MIN - elapsedMin));
            return true;
        }
        return false;
    }

    private static void scheduleNext(Context ctx, long delayMin) {
        AlarmManager am = ctx.getSystemService(AlarmManager.class);
        if (am == null) return;
        PendingIntent pi = PendingIntent.getBroadcast(ctx, REQ_ALARM,
                new Intent(ACTION_NUDGE).setPackage(ctx.getPackageName()),
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        am.cancel(pi);
        am.setAndAllowWhileIdle(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                SystemClock.elapsedRealtime() + delayMin * 60_000L, pi);
    }

    private static void showNotification(Context ctx) {
        NotificationManager nm = ctx.getSystemService(NotificationManager.class);
        if (nm == null) return;

        NotificationChannel ch = new NotificationChannel(CHANNEL_ID,
                ctx.getString(R.string.valkyos_donate_channel_name),
                NotificationManager.IMPORTANCE_HIGH);
        ch.setDescription(ctx.getString(R.string.valkyos_donate_channel_desc));
        nm.createNotificationChannel(ch);

        PendingIntent contentPi = PendingIntent.getActivity(ctx, 0,
                new Intent(ctx, DonateActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        Notification n = new Notification.Builder(ctx, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_valkyos_donate)
                .setContentTitle(ctx.getString(R.string.valkyos_donate_title))
                .setContentText(ctx.getString(R.string.valkyos_donate_notification_text))
                .setContentIntent(contentPi)
                .setAutoCancel(true)
                .setOnlyAlertOnce(true)
                .build();
        nm.notify(NOTIFICATION_ID, n);
        Log.d(TAG, "Donation reminder shown");

        scheduleNext(ctx, REPEAT_DELAY_MIN);
    }

    static void cancelNotification(Context ctx) {
        NotificationManager nm = ctx.getSystemService(NotificationManager.class);
        if (nm != null) nm.cancel(NOTIFICATION_ID);
    }
}
