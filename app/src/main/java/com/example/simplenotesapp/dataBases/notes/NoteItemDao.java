package com.example.simplenotesapp.dataBases.notes;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Query;
import androidx.room.Update;
import androidx.room.Upsert;

import java.util.List;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Flowable;

@Dao
public interface NoteItemDao {

    @Query("SELECT * FROM note_items WHERE noteId = :noteId")
    LiveData<List<NoteItemEntity>> getItems(long noteId);

    @Upsert
    void upsert(NoteItemEntity item);

    @Update
    void update(NoteItemEntity item);

    @Delete
    void delete(NoteItemEntity item);
}

