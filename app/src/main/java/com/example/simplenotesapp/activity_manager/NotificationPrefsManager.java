package com.example.simplenotesapp.activity_manager;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.simplenotesapp.utils.NotificationHelper;

public class NotificationPrefsManager {
    private final SharedPreferences prefs;
    private static final String PREFS_NAME = "notification_prefs";
    private static final String KEY_ENABLED = "notifications_enabled";
    private static final String KEY_FREQUENCY = "notification_frequency";
    private static final String KEY_HOUR = "notification_hour";
    private static final String KEY_MINUTE = "notification_minute";
    private static final String KEY_THEME_ID = "notification_theme_id";
    private static final String KEY_THEME_NAME = "notification_theme_name";

    public NotificationPrefsManager(Context context) {
        this.prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    // Геттеры
    public boolean isEnabled() { return prefs.getBoolean(KEY_ENABLED, false); }
    public int getFrequency() { return prefs.getInt(KEY_FREQUENCY, 0); }
    public int getHour() { return prefs.getInt(KEY_HOUR, 9); }
    public int getMinute() { return prefs.getInt(KEY_MINUTE, 0); }
    public long getThemeId() { return prefs.getLong(KEY_THEME_ID, -1); }
    public String getThemeName() { return prefs.getString(KEY_THEME_NAME, null); }

    public void saveSettingsAndSchedule(Context context, boolean enabled, int frequency,
                                        int hour, int minute, long themeId, String themeName) {
        // сохраняем данные
        saveNotificationSettings(enabled, frequency, hour, minute, themeId, themeName);

        // запускаем или отменяем AlarmManager
        if (enabled) {
            NotificationHelper.scheduleNotification(context, hour, minute, frequency);
        } else {
            NotificationHelper.cancelNotification(context);
        }
    }
    // Сохранить всё за раз
    public void saveNotificationSettings(boolean enabled, int frequency, int hour,
                             int minute, long themeId, String themeName) {
        SharedPreferences.Editor editor = prefs.edit()
                .putBoolean(KEY_ENABLED, enabled)
                .putInt(KEY_FREQUENCY, frequency)
                .putInt(KEY_HOUR, hour)
                .putInt(KEY_MINUTE, minute)
                .putLong(KEY_THEME_ID, themeId);

        if (themeName != null) {
            editor.putString(KEY_THEME_NAME, themeName);
        } else {
            editor.remove(KEY_THEME_NAME);
        }

        editor.apply();
    }

    // Отдельные сеттеры — каждый свой editor
    public void setEnabled(boolean enabled) {
        prefs.edit().putBoolean(KEY_ENABLED, enabled).apply();
    }
    public void setFrequency(int frequency) {
        prefs.edit().putInt(KEY_FREQUENCY, frequency).apply();
    }
    public void setHour(int hour) {
        prefs.edit().putInt(KEY_HOUR, hour).apply();
    }
    public void setMinute(int minute) {
        prefs.edit().putInt(KEY_MINUTE, minute).apply();
    }
    public void setThemeId(long themeId) {
        prefs.edit().putLong(KEY_THEME_ID, themeId).apply();
    }
    public void setThemeName(String themeName) {
        prefs.edit().putString(KEY_THEME_NAME, themeName).apply();
    }
}