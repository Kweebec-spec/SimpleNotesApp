package com.example.simplenotesapp.activity_manager;

import android.content.Context;
import android.content.SharedPreferences;

public class ThemeManager {

    private final  SharedPreferences prefs;
    private static final String PREFS_NAME = "theme_prefs";
    private static final String isDarkMode = "is_dark_mode";

    public ThemeManager(Context context) {
        this.prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }


    public void saveDarkModeThemePreference(boolean isDark) {
        prefs.edit().putBoolean(isDarkMode, isDark).apply();
    }

    public boolean isDarkModeActive() {
        return prefs.getBoolean(isDarkMode, false);
    }
}
