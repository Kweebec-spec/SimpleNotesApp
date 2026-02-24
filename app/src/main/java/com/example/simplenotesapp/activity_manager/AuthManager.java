package com.example.simplenotesapp.activity_manager;

import android.content.Context;
import android.content.SharedPreferences;
import com.example.simplenotesapp.MyApplication;
import com.example.simplenotesapp.model.User;
import com.example.simplenotesapp.repository.UserRepository;

public class AuthManager {
    private final SharedPreferences prefs;
    private final UserRepository userRepository;

    public AuthManager(Context context) {
        this.prefs = context.getSharedPreferences("userPrefs", Context.MODE_PRIVATE);

        // ПРАВИЛЬНО: берем репозиторий из нашего Application
        // Это гарантирует, что мы используем тот же объект, что и везде
        this.userRepository = ((MyApplication) context.getApplicationContext()).getUserRepository();
    }

    public void login(User user, boolean shouldRemember) {
        // Оптимизация: открываем один раз, записываем всё и сохраняем один раз
        prefs.edit()
                .putLong("userid", user.getId())
                .putBoolean("remember_me", shouldRemember)
                .apply();
    }

    public void logout() {
        boolean currentTheme = prefs.getBoolean("is_dark_mode", false);

        // Очищаем и тут же возвращаем тему в одной транзакции
        prefs.edit()
                .clear()
                .putBoolean("is_dark_mode", currentTheme)
                .apply();
    }

    public void setRemember_me(boolean flag) {
        prefs.edit().putBoolean("remember_me", flag).apply();
    }

    public boolean Y_remember_me() {
        return prefs.getBoolean("remember_me", false);
    }

    public Long getId() {
        // Если ID не найден, лучше возвращать -1, чтобы знать, что юзера нет
        return prefs.getLong("userid", -1L);
    }
}
