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

    @Override
    public void onCreate() {
        super.onCreate();



        SharedPreferences prefs = getSharedPreferences("userPrefs", MODE_PRIVATE);
        boolean isDark = prefs.getBoolean("is_dark_mode", false);

        AppCompatDelegate.setDefaultNightMode(isDark ?
                AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);

        // Применяем динамические цвета и тему при запуске
        DynamicColors.applyToActivitiesIfAvailable(this);
    }

    // МЕТОДЫ ДОЛЖНЫ БЫТЬ ТУТ (ВНЕ onCreate)

    public NotesRepository getNotesRepository() {
        // Убедись, что название базы AppDatabase или NoteDatabase (как у тебя в проекте)
        AppDatabase db = AppDatabase.getInstance(this);
        // Передаем оба DAO, как мы писали в прошлом шаге
        return NotesRepository.getInstance(db.noteDao(), db.noteItemDao());
    }

    // Если у тебя есть отдельный репозиторий для пользователей
    public UserRepository getUserRepository() {
        AppDatabase db = AppDatabase.getInstance(this);
        return UserRepository.getInstance(db.userDao());
    }

    public ThemeRepository getThemeRepository() {
        AppDatabase db = AppDatabase.getInstance(this);
        return ThemeRepository.getInstance(db.themeDao());
    }
}
