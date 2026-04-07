package com.example.simplenotesapp.dataBase.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import androidx.room.Upsert;

import com.example.simplenotesapp.dataBase.entity.NoteEntity;

import java.util.List;

@Dao
public interface NoteDao {

    @Query("SELECT * FROM notes WHERE userId = :userId AND (themeId = :themeId OR :themeId = -1) AND (title LIKE '%' || :query || '%' OR text LIKE '%' || :query || '%') ORDER BY createdAt DESC")
    LiveData<List<NoteEntity>> getNotesByThemeAndQuery(long userId, long themeId, String query);

    // GET NOTES
    @Query("SELECT * FROM notes WHERE userId = :userId ORDER BY createdAt DESC")
    LiveData<List<NoteEntity>> getNotesForUser(long userId);

    @Query("SELECT * FROM notes WHERE note_id = :noteId")
    LiveData<NoteEntity> getNoteById(long noteId);

    @Query("SELECT * FROM notes WHERE note_id = :noteId")
    NoteEntity getNoteByIdSync(long noteId);
    @Query("SELECT * FROM notes WHERE themeId = :themeId")
    List<NoteEntity> getNotesByThemeIdSync(long themeId);

    // SEARCH
    @Query("SELECT * FROM notes WHERE userId = :userId AND (title LIKE '%' || :query || '%' OR text LIKE '%' || :query || '%') ORDER BY createdAt DESC")
    LiveData<List<NoteEntity>> searchNotes(long userId, String query);

    // LAST CREATED
    @Query("SELECT * FROM notes ORDER BY note_id DESC LIMIT 1")
    LiveData<NoteEntity> getLastCreatedNoteLiveData();

    @Query("SELECT * FROM notes WHERE userId = :userId ORDER BY note_id DESC LIMIT 1")
    LiveData<NoteEntity> getLastCreatedNoteForUserLiveData(long userId);

    // COUNT
    @Query("SELECT COUNT(*) FROM notes WHERE userId = :userId")
    LiveData<Integer> getNotesCount(long userId);

    // INSERT
    @Insert
    long insert(NoteEntity note);

    @Insert
    void insertAll(List<NoteEntity> notes);

    // UPDATE
    @Update
    void update(NoteEntity note);

    // UPSERT
    @Upsert
    void upsert(NoteEntity note);

    // DELETE
    @Delete
    void delete(NoteEntity note);

    @Query("DELETE FROM notes WHERE userId = :userId")
    void deleteAllNotesForUser(long userId);

    // TOUCH FOR REFRESH (обновляет updatedAt для триггера LiveData)
    @Query("UPDATE notes SET createdAt = :timestamp WHERE userId = :userId")
    void touchNotesForUser(long userId, long timestamp);
}