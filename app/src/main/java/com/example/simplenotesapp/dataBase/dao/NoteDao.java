package com.example.simplenotesapp.dataBase.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;
import androidx.room.Upsert;

import com.example.simplenotesapp.dataBase.pojo.PreviewNoteWithItemsThemes;
import com.example.simplenotesapp.dataBase.entity.NoteEntity;

import java.util.List;

@Dao
public interface NoteDao {

    @Transaction // Required because Room runs multiple queries to fetch the Note + Theme
    @Query("SELECT * FROM notes WHERE userId = :userId ORDER BY createdAt DESC")
    LiveData<List<PreviewNoteWithItemsThemes>> getNotesForUser(long userId);



    @Transaction
    @Query("SELECT *, " +
            // 1. Собираем превью (чекбоксы + текст)
            "(SELECT GROUP_CONCAT(" +
            "  CASE WHEN type = 1 THEN (CASE WHEN checked = 1 THEN '☑ ' ELSE '☐ ' END) ELSE '' END || text, " +
            "  '\n') " +
            " FROM (SELECT * FROM note_items WHERE noteId = notes.id ORDER BY id ASC)" +
            ") AS previewText, " +

            // 2. Заголовок (используем твое поле previewTitle)
            "CASE WHEN previewTitle IS NULL OR previewTitle = '' THEN 'Без названия' ELSE previewTitle END AS displayTitle, " +

            // 3. ЦВЕТ (Убрал лишнюю решетку внутри COALESCE)
            "'#' || COALESCE((SELECT color FROM themes WHERE id = notes.themeId), 'D3D3D3') AS displayColor " +

            "FROM notes WHERE userId = :userId ORDER BY createdAt DESC")
    LiveData<List<PreviewNoteWithItemsThemes>> getNotesWithPreview(long userId);






    @Upsert
    void upsert(NoteEntity note);

    @Update
    void update(NoteEntity note);

    @Delete
    void delete(NoteEntity note);

    // выдает одну заметку один раз и завершается
    @Query("SELECT * FROM notes WHERE id = :id")
    NoteEntity getNoteById(long id);


}