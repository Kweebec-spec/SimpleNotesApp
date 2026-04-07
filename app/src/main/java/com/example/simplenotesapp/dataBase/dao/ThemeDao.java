package com.example.simplenotesapp.dataBase.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Query;
import androidx.room.Upsert;

import com.example.simplenotesapp.dataBase.entity.ThemeEntity;

import java.util.List;

@Dao
public interface ThemeDao {

    @Upsert
    void upsert(ThemeEntity themeEntity);

    @Delete
    void delete(ThemeEntity themeEntity);

    // Получить темы только для конкретного пользователя
    @Query("SELECT * FROM themes WHERE userId = :userId ORDER BY id DESC")
    LiveData<List<ThemeEntity>> getThemesForUser(long userId);

    // Проверить существование темы с таким именем для пользователя
    @Query("SELECT EXISTS(SELECT 1 FROM themes WHERE userId = :userId AND name = :name COLLATE NOCASE)")
    boolean isThemeNameExists(long userId, String name);

    // Получить тему по имени для пользователя
    @Query("SELECT * FROM themes WHERE userId = :userId AND name = :name COLLATE NOCASE LIMIT 1")
    ThemeEntity getThemeByName(long userId, String name);
}