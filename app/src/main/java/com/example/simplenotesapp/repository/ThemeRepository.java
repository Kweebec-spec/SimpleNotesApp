package com.example.simplenotesapp.repository;
import androidx.lifecycle.LiveData;
import com.example.simplenotesapp.dataBase.dao.ThemeDao;
import com.example.simplenotesapp.dataBase.entity.ThemeEntity;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ThemeRepository {
    private final ThemeDao themeDao;
    // Оставляем SingleThreadExecutor, так как для настроек темы порядок важен
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    private static ThemeRepository instance;

    // 1. Конструктор теперь принимает DAO напрямую
    private ThemeRepository(ThemeDao themeDao) {
        this.themeDao = themeDao;
    }

    // 2. Метод getInstance тоже принимает DAO (как и в NotesRepository)
    public static synchronized ThemeRepository getInstance(ThemeDao themeDao) {
        if (instance == null) {
            instance = new ThemeRepository(themeDao);
        }
        return instance;
    }

    public LiveData<List<ThemeEntity>> getThemes() {
        return themeDao.getThemes();
    }

    public void upsert(ThemeEntity theme) {
        executor.execute(() -> themeDao.upsert(theme));
    }

    // Метод shutdown обычно не нужен, если репозиторий — Singleton (он живет до смерти приложения)
}
