package com.example.simplenotesapp.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.example.simplenotesapp.MyApplication;
import com.example.simplenotesapp.activity_manager.NotificationPrefsManager;
import com.example.simplenotesapp.utils.NotificationHelper;

public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (!Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) return;

        //  берём данные через NotificationManager
        MyApplication app = (MyApplication) context.getApplicationContext();
        NotificationPrefsManager notificationPrefsManager = app.getNotificationPrefsManager();

        if (notificationPrefsManager.isEnabled()) {
            NotificationHelper.scheduleNotification(
                    context,
                    notificationPrefsManager.getHour(),
                    notificationPrefsManager.getMinute(),
                    notificationPrefsManager.getFrequency()
            );
        }
    }
}