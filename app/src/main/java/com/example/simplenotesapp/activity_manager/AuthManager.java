package com.example.simplenotesapp.activity_manager;

import android.content.Context;
import android.content.SharedPreferences;

public class AuthManager {
    private final  SharedPreferences prefs;
    private static final String PREF_NAME = "auth_prefs";
    private static final String KEY_USER_ID = "userid";
    private static final String KEY_REMEMBER_ME = "remember_me";
    private static final String KEY_USER_EMAIL = "user_email";
    private static final String KEY_USER_NAME = "user_name";

    public AuthManager(Context context) {
        this.prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public void login(long userId, String email, String username, boolean shouldRemember) {
        prefs.edit()
                .putLong(KEY_USER_ID, userId)
                .putString(KEY_USER_EMAIL, email)
                .putString(KEY_USER_NAME, username)
                .putBoolean(KEY_REMEMBER_ME, shouldRemember)
                .apply();
    }

    public void logout() {
        prefs.edit().clear().apply();
    }

    public void setRememberMe(boolean flag) {
        prefs.edit().putBoolean(KEY_REMEMBER_ME, flag).apply();
    }

    public boolean isRememberMe() {
        return prefs.getBoolean(KEY_REMEMBER_ME, false);
    }

    public Long getId() {
        if (!prefs.contains(KEY_USER_ID)) {
            return null;
        }
        return prefs.getLong(KEY_USER_ID, -1L);
    }

    public String getEmail() {
        return prefs.getString(KEY_USER_EMAIL, null);
    }

    public String getUserName() {
        return prefs.getString(KEY_USER_NAME, null);
    }

    public boolean isLoggedIn() {
        return prefs.contains(KEY_USER_ID) && prefs.getLong(KEY_USER_ID, -1L) > 0;
    }

    public void updateUserName(String newName) {
        prefs.edit().putString(KEY_USER_NAME, newName).apply();
    }

    public void updateUserEmail(String newEmail) {
        prefs.edit().putString(KEY_USER_EMAIL, newEmail).apply();
    }
}