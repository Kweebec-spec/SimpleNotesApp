package com.example.simplenotesapp;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatDelegate;

import com.example.simplenotesapp.activity_manager.AuthManager;
import com.example.simplenotesapp.activity_manager.NotificationPrefsManager;
import com.example.simplenotesapp.activity_manager.ThemeManager;
import com.example.simplenotesapp.dataBase.AppDatabase;
import com.example.simplenotesapp.repository.NotesRepository;
import com.example.simplenotesapp.repository.ThemeRepository;
import com.example.simplenotesapp.repository.UserRepository;
import com.example.simplenotesapp.utils.KeyboardUtils;
import com.example.simplenotesapp.utils.NotificationHelper;
import com.google.android.material.color.DynamicColors;

import androidx.annotation.NonNull;

public class MyApplication extends Application {


    private AppDatabase appDatabase;
    private NotesRepository notesRepository;
    private UserRepository userRepository;
    private ThemeRepository themeRepository;
    private AuthManager authManager;
    private ThemeManager themeManager;
    private NotificationPrefsManager notificationPrefsManager;

    @Override
    public void onCreate() {
        super.onCreate();
        NotificationHelper.createNotificationChannel(this);

        registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks() {

            @Override
            public void onActivityCreated(@NonNull Activity activity, Bundle savedInstanceState) {
                // Override dispatchTouchEvent for every activity automatically
                activity.getWindow().getDecorView().setOnTouchListener((v, event) -> {
                    KeyboardUtils.handleDispatchTouchEvent(event, activity);
                    return false;
                });
            }

            // --- required empty overrides ---
            @Override public void onActivityStarted(@NonNull Activity activity) {}
            @Override public void onActivityResumed(@NonNull Activity activity) {}
            @Override public void onActivityPaused(@NonNull Activity activity) {}
            @Override public void onActivityStopped(@NonNull Activity activity) {}
            @Override public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle outState) {}
            @Override public void onActivityDestroyed(@NonNull Activity activity) {}
        });

        appDatabase = AppDatabase.getInstance(this);
        notesRepository = NotesRepository.getInstance(appDatabase.noteDao());
        userRepository = UserRepository.getInstance(appDatabase.userDao());
        themeRepository = ThemeRepository.getInstance(appDatabase.themeDao());
        authManager = new AuthManager(this);
        themeManager = new ThemeManager(this);
        notificationPrefsManager = new NotificationPrefsManager(this);


        AppCompatDelegate.setDefaultNightMode(themeManager.isDarkModeActive() ?
                AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);


        // Применяем динамические цвета и тему при запуске
        DynamicColors.applyToActivitiesIfAvailable(this);
    }

    // Теперь просто возвращаем сохранённые экземпляры
    public NotesRepository getNotesRepository() {
        return notesRepository;
    }

    public UserRepository getUserRepository() {
        return userRepository;
    }

    public ThemeRepository getThemeRepository() {
        return themeRepository;
    }

    public AuthManager getAuthManager(){return  authManager; }
    public ThemeManager getThemeManager(){return  themeManager; }
    public NotificationPrefsManager getNotificationPrefsManager(){return notificationPrefsManager; }
}
