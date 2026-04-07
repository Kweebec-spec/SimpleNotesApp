package com.example.simplenotesapp.repository;

import androidx.lifecycle.LiveData;
import com.example.simplenotesapp.dataBase.dao.ThemeDao;
import com.example.simplenotesapp.dataBase.entity.ThemeEntity;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ThemeRepository {
    private final ThemeDao themeDao;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    private static ThemeRepository instance;

    private ThemeRepository(ThemeDao themeDao) {
        this.themeDao = themeDao;
    }

    public static synchronized ThemeRepository getInstance(ThemeDao themeDao) {
        if (instance == null) {
            instance = new ThemeRepository(themeDao);
        }
        return instance;
    }

    public void delete(ThemeEntity theme) {
        executor.execute(() -> themeDao.delete(theme));
    }

    //  принимает userId и возвращает темы только для этого пользователя
    public LiveData<List<ThemeEntity>> getThemesForUser(long userId) {
        return themeDao.getThemesForUser(userId);
    }

    public void upsert(ThemeEntity theme) {
        executor.execute(() -> themeDao.upsert(theme));
    }

    // метод для проверки существования темы по имени
    public boolean isThemeNameExists(long userId, String name) {
        return themeDao.isThemeNameExists(userId, name);
    }
}