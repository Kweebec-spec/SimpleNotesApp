package com.example.simplenotesapp.dataBase.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Query;
import androidx.room.Update;
import androidx.room.Upsert;

import com.example.simplenotesapp.dataBase.entity.ThemeEntity;

import java.util.List;

@Dao
public interface ThemeDao {

    @Update
    void update(ThemeEntity themeEntity);
    @Delete
    void  delete(ThemeEntity themeEntity);
    @Upsert
    void upsert(ThemeEntity themeEntity);

    @Query("SELECT * FROM themes  ORDER BY id DESC")
    LiveData<List<ThemeEntity>> getThemes();

}
