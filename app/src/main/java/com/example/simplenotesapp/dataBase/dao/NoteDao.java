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


    // Временно добавь этот метод для отладки
    @Query("SELECT * FROM notes WHERE userId = :userId LIMIT 1")
    LiveData<NoteEntity> testQuery(long userId);

    /**
     * Получает заметки с превью для конкретного пользователя
     */
    @Transaction
    @Query("SELECT " +
            // Поля из NoteEntity (перечисляем явно, чтобы избежать конфликтов)
            "notes.id, " +
            "notes.themeId, " +
            "notes.userId, " +
            "notes.previewTitle, " +
            "notes.previewText as notePreviewText, " +  // переименовываем, чтобы не путать с вычисляемым
            "notes.createdAt, " +
            "notes.color, " +
            "notes.hasChecklist, " +

            // 1. Вычисляем previewText из items
            "(SELECT GROUP_CONCAT(" +
            "  CASE WHEN type = 1 THEN (CASE WHEN checked = 1 THEN '☑ ' ELSE '☐ ' END) || text " +
            "       ELSE text END, " +
            "  '\n') " +
            " FROM note_items " +
            " WHERE note_items.noteId = notes.id " +
            " ORDER BY note_items.id ASC" +
            ") AS previewText, " +

            // 2. Формируем displayTitle
            "CASE WHEN notes.previewTitle IS NULL OR notes.previewTitle = '' " +
            "     THEN 'Без названия' " +
            "     ELSE notes.previewTitle " +
            "END AS displayTitle, " +

            // 3. Получаем цвет из темы
            "COALESCE((SELECT color FROM themes WHERE id = notes.themeId), '#D3D3D3') AS displayColor " +

            "FROM notes " +
            "WHERE notes.userId = :userId " +
            "ORDER BY notes.createdAt DESC")
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