package com.example.simplenotesapp.receivers;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import com.example.simplenotesapp.MyApplication;
import com.example.simplenotesapp.R;
import com.example.simplenotesapp.activity_manager.NotificationPrefsManager;
import com.example.simplenotesapp.activitys.MainActivity;
import com.example.simplenotesapp.utils.NotificationHelper;

public class NotificationReceiver extends BroadcastReceiver {
    private static final String TAG = "NotificationReceiver";


    @Override
    public void onReceive(Context context, Intent intent) {
        try {
            Log.d(TAG, "Notification received");

            // Check if notifications are enabled globally
            NotificationPrefsManager nm = ((MyApplication) context.getApplicationContext()).getNotificationPrefsManager();
            boolean notifications_enabled = nm.isEnabled();

            if (!notifications_enabled) {
                Log.d(TAG, "Notifications disabled");
                return;
            }

            long themeId = nm.getThemeId();
            String themeName = nm.getThemeName();





            // Create notification channel
            NotificationHelper.createNotificationChannel(context);

            // Intent to open the app
            Intent notificationIntent = new Intent(context, MainActivity.class);
            notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

            if (themeId != -1) {
                notificationIntent.putExtra("filter_theme_id", themeId);
            }

            // Pending intent flags
            int flags = PendingIntent.FLAG_UPDATE_CURRENT;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                flags |= PendingIntent.FLAG_IMMUTABLE;
            }

            PendingIntent pendingIntent = PendingIntent.getActivity(
                    context, 0, notificationIntent, flags);

            // Build notification text
            String contentText = (themeName != null && !themeName.isEmpty())
                    ? "You have notes waiting from \"" + themeName + "\" theme!"
                    : "You have notes waiting for your attention!";

            // Build notification
            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, NotificationHelper.CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_notification)
                    .setContentTitle("Notes Reminder")
                    .setContentText(contentText)
                    .setStyle(new NotificationCompat.BigTextStyle().bigText(contentText))
                    .setContentIntent(pendingIntent)
                    .setColor(ContextCompat.getColor(context, R.color.blue))
                    .setColorized(true)
                    .setAutoCancel(true)
                    .setDefaults(NotificationCompat.DEFAULT_ALL);

            // Show notification
            NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            manager.notify((int) System.currentTimeMillis(), builder.build());

            // Reschedule for next time
            NotificationHelper.scheduleNotification(context, nm.getHour(), nm.getMinute(), nm.getFrequency());

        } catch (Exception e) {
            Log.e(TAG, "Error in notification receiver", e);
            e.printStackTrace();
        }
    }
}