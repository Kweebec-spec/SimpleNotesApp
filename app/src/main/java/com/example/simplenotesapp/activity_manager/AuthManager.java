package com.example.simplenotesapp.activity_manager;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.simplenotesapp.model.User;
import com.example.simplenotesapp.repository.UserRepository;

public class AuthManager {
    private UserRepository userRepository;
    private SharedPreferences prefs;

    public AuthManager(Context context) {
        this.userRepository = new UserRepository(context);
        this.prefs = context.getSharedPreferences("userPrefs", Context.MODE_PRIVATE);
    }

    public void login(User user, boolean shouldRemember) {
        if (shouldRemember) {
            // Сохраняем данные, только если галочка стоит
            prefs.edit().putString("email", user.getEmail()).apply();
            prefs.edit().putBoolean("remember_me", true).apply();
        } else {
            prefs.edit().putString("email", user.getEmail()).apply();
            prefs.edit().putBoolean("remember_me", false).apply();

        }
    }

    public void logout(){
        // 1. Сохраняем текущее состояние темы перед очисткой
        boolean currentTheme = prefs.getBoolean("is_dark_mode", false);

        // 2. Очищаем всё
        prefs.edit().clear().apply();

        // 3. Записываем тему обратно, чтобы она не сбросилась
        prefs.edit().putBoolean("is_dark_mode", currentTheme).apply();

    }


    public boolean Y_remember_me(){
        return  prefs.getBoolean("remember_me", false);
    }

}
