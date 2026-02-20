package com.example.simplenotesapp.dataBases.notes;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.simplenotesapp.dataBases.notes.NoteDao;
import com.example.simplenotesapp.dataBases.notes.NoteEntity;

@Database(entities = {NoteEntity.class, NoteItemEntity.class}, version = 1)
public abstract class NotesDatabase extends RoomDatabase {

    private static volatile NotesDatabase INSTANCE;

    public abstract NoteDao noteDao();
    public abstract NoteItemDao noteItemDao();

    public static synchronized NotesDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            INSTANCE = Room.databaseBuilder(
                    context.getApplicationContext(),
                    NotesDatabase.class,
                    "notes.db"
            ).build();
        }
        return INSTANCE;
    }
}
