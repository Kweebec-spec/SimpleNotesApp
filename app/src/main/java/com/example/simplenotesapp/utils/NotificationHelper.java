package com.example.simplenotesapp.utils;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.util.Log;

import com.example.simplenotesapp.receivers.NotificationReceiver;

import java.util.Calendar;

public class NotificationHelper {
    private static final String TAG = "NotificationHelper";
    public static final String CHANNEL_ID = "notes_channel";

    public static void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                NotificationManager manager = context.getSystemService(NotificationManager.class);
                NotificationChannel channel = new NotificationChannel(
                        CHANNEL_ID,
                        "Notes Notifications",
                        NotificationManager.IMPORTANCE_HIGH
                );
                channel.setDescription("Channel for notes reminders");
                manager.createNotificationChannel(channel);
            } catch (Exception e) {
                Log.e(TAG, "Error creating notification channel", e);
            }
        }
    }

    // ✅ принимает данные параметрами, не читает SharedPreferences сам
    public static void scheduleNotification(Context context, int hour, int minute, int frequency) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
                if (alarmManager != null && !alarmManager.canScheduleExactAlarms()) {
                    Log.w(TAG, "Cannot schedule exact alarm: permission missing");
                    return;
                }
            }

            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.HOUR_OF_DAY, hour);
            calendar.set(Calendar.MINUTE, minute);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);

            long currentTime = System.currentTimeMillis();
            while (calendar.getTimeInMillis() <= currentTime) {
                switch (frequency) {
                    case 0: calendar.add(Calendar.DAY_OF_YEAR, 1); break;
                    case 1: calendar.add(Calendar.WEEK_OF_YEAR, 1); break;
                    case 2: calendar.add(Calendar.MONTH, 1); break;
                    case 3: calendar.add(Calendar.YEAR, 1); break;
                    default: calendar.add(Calendar.DAY_OF_YEAR, 1); break;
                }
            }

            PendingIntent pendingIntent = buildPendingIntent(context);
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

            if (alarmManager == null) return;

            alarmManager.cancel(pendingIntent);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
            } else {
                alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
            }

            Log.d(TAG, "Scheduled for: " + calendar.getTime());

        } catch (SecurityException e) {
            Log.e(TAG, "Security exception", e);
        } catch (Exception e) {
            Log.e(TAG, "Error scheduling", e);
        }
    }

    // ✅ cancelNotification остаётся — это работа с AlarmManager
    public static void cancelNotification(Context context) {
        try {
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            if (alarmManager != null) {
                alarmManager.cancel(buildPendingIntent(context));
                Log.d(TAG, "Notification cancelled");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error cancelling", e);
        }
    }

    // ✅ вынесли повторяющийся код в приватный метод
    private static PendingIntent buildPendingIntent(Context context) {
        Intent intent = new Intent(context, NotificationReceiver.class);
        int flags = PendingIntent.FLAG_UPDATE_CURRENT;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            flags |= PendingIntent.FLAG_IMMUTABLE;
        }
        return PendingIntent.getBroadcast(context, 0, intent, flags);
    }

}