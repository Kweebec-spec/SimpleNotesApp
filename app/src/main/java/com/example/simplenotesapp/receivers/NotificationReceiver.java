package com.example.simplenotesapp.helper;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import androidx.core.app.NotificationCompat;

import com.example.simplenotesapp.R;
import com.example.simplenotesapp.activitys.MainNotesActivity;
import com.example.simplenotesapp.utils.NotificationHelper;

public class NotificationReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        long themeId = intent.getLongExtra("themeId", -1);
        
        // Проверяем, включены ли уведомления в bottom sheet (глобальный переключатель)
        SharedPreferences bottomSheetPrefs = context.getSharedPreferences("bottom_sheet_prefs", Context.MODE_PRIVATE);
        boolean notificationsGlobal = bottomSheetPrefs.getBoolean("notifications_enabled", false);
        
        if (!notificationsGlobal) {
            return; // если выключено в bottom sheet, не показываем
        }

        Intent notificationIntent = new Intent(context, MainNotesActivity.class);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                0,
                notificationIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, NotificationHelper.CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification) // создайте иконку
                .setContentTitle("Напоминание о заметках")
                .setContentText("У вас есть заметки, которые требуют внимания!")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify((int) System.currentTimeMillis(), builder.build());

        // Перепланировать следующее уведомление
        NotificationHelper.scheduleNotification(context);
    }
}