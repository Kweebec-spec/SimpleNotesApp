package com.example.simplenotesapp;

import android.app.Application;
import android.content.SharedPreferences;

import androidx.appcompat.app.AppCompatDelegate;

import com.example.simplenotesapp.dataBase.AppDatabase;
import com.example.simplenotesapp.repository.NotesRepository;
import com.example.simplenotesapp.repository.ThemeRepository;
import com.example.simplenotesapp.repository.UserRepository;
import com.google.android.material.color.DynamicColors;

public class MyApplication extends Application {


    AppDatabase appDatabase;
    NotesRepository notesRepository;
    UserRepository userRepository;
    ThemeRepository themeRepository;
    @Override
    public void onCreate() {
        super.onCreate();
        appDatabase = AppDatabase.getInstance(this);
        notesRepository = NotesRepository.getInstance(appDatabase.noteDao(), appDatabase.noteItemDao());
        userRepository = UserRepository.getInstance(appDatabase.userDao());
        themeRepository = ThemeRepository.getInstance(appDatabase.themeDao());



        SharedPreferences prefs = getSharedPreferences("userPrefs", MODE_PRIVATE);
        boolean isDark = prefs.getBoolean("is_dark_mode", false);

        AppCompatDelegate.setDefaultNightMode(isDark ?
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
}
