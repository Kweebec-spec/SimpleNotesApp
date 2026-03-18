package com.example.simplenotesapp.helper;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import com.example.simplenotesapp.R;
import com.example.simplenotesapp.NotificationReceiver;

import java.util.Calendar;

public class NotificationHelper {
    private static final String CHANNEL_ID = "notes_channel";
    private static final String CHANNEL_NAME = "Notes Notifications";
    private static final int NOTIFICATION_ID = 1001;
    private static final String PREFS_NAME = "notification_prefs";
    private static final String KEY_ENABLED = "notifications_enabled";
    private static final String KEY_FREQUENCY = "notification_frequency";
    private static final String KEY_TIME_HOUR = "notification_hour";
    private static final String KEY_TIME_MINUTE = "notification_minute";
    private static final String KEY_THEME_ID = "notification_theme_id";

    public static void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            channel.setDescription("Channel for notes reminders");
            NotificationManager manager = context.getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }
    }

    public static void scheduleNotification(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        boolean enabled = prefs.getBoolean(KEY_ENABLED, false);
        if (!enabled) {
            cancelNotification(context);
            return;
        }

        int hour = prefs.getInt(KEY_TIME_HOUR, 9); // default 9 AM
        int minute = prefs.getInt(KEY_TIME_MINUTE, 0);
        int frequency = prefs.getInt(KEY_FREQUENCY, 0); // 0=day,1=week,2=month,3=year
        long themeId = prefs.getLong(KEY_THEME_ID, -1);

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);

        if (calendar.getTimeInMillis() < System.currentTimeMillis()) {
            switch (frequency) {
                case 0: calendar.add(Calendar.DAY_OF_YEAR, 1); break;
                case 1: calendar.add(Calendar.WEEK_OF_YEAR, 1); break;
                case 2: calendar.add(Calendar.MONTH, 1); break;
                case 3: calendar.add(Calendar.YEAR, 1); break;
            }
        }

        Intent intent = new Intent(context, NotificationReceiver.class);
        intent.putExtra("themeId", themeId);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
    }

    public static void cancelNotification(Context context) {
        Intent intent = new Intent(context, NotificationReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(pendingIntent);
    }

    public static void saveNotificationSettings(Context context, boolean enabled, int frequency, int hour, int minute, long themeId) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit()
                .putBoolean(KEY_ENABLED, enabled)
                .putInt(KEY_FREQUENCY, frequency)
                .putInt(KEY_TIME_HOUR, hour)
                .putInt(KEY_TIME_MINUTE, minute)
                .putLong(KEY_THEME_ID, themeId)
                .apply();
        scheduleNotification(context);
    }

    public static boolean isNotificationsEnabled(Context context) {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).getBoolean(KEY_ENABLED, false);
    }
}