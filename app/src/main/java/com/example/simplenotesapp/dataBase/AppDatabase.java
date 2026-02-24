package com.example.simplenotesapp.dataBase;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.simplenotesapp.dataBase.dao.NoteDao;
import com.example.simplenotesapp.dataBase.dao.NoteItemDao;
import com.example.simplenotesapp.dataBase.dao.ThemeDao;
import com.example.simplenotesapp.dataBase.entity.NoteEntity;
import com.example.simplenotesapp.dataBase.entity.NoteItemEntity;
import com.example.simplenotesapp.dataBase.entity.ThemeEntity;
import com.example.simplenotesapp.dataBase.dao.UserDao;
import com.example.simplenotesapp.dataBase.entity.UserEntity;

@Database(entities = {NoteEntity.class, NoteItemEntity.class, ThemeEntity.class, UserEntity.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {

    private static volatile AppDatabase INSTANCE;

    public abstract NoteDao noteDao();
    public abstract NoteItemDao noteItemDao();
    public abstract ThemeDao themeDao();
    public abstract UserDao userDao();

    public static synchronized AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            INSTANCE = Room.databaseBuilder(
                    context.getApplicationContext(),
                    AppDatabase.class,
                    "notes.db"
            ).fallbackToDestructiveMigration().build();
        }
        return INSTANCE;
    }
}
