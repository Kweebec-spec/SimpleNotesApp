package com.example.simplenotesapp.dataBases.notes;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;
import androidx.room.Upsert;

import com.example.simplenotesapp.dataBases.notes.NoteEntity;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;
import java.util.List;

@Dao
public interface NoteDao {

    @Query("SELECT * FROM notes ORDER BY createdAt DESC")
    LiveData<List<NoteEntity>> getAll();

    @Upsert
    void upsert(NoteEntity note);

    @Update
    void update(NoteEntity note);

    @Delete
    void delete(NoteEntity note);


    // выдает одну заметку один раз и завершается
    @Query("SELECT * FROM notes WHERE id = :id")
    NoteEntity getById(long id);
}