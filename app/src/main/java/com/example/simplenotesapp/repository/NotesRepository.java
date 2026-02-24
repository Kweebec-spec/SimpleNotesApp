package com.example.simplenotesapp.repository;
import com.example.simplenotesapp.dataBase.dao.NoteDao;
import com.example.simplenotesapp.dataBase.entity.NoteEntity;
import com.example.simplenotesapp.dataBase.dao.NoteItemDao;
import com.example.simplenotesapp.dataBase.entity.NoteItemEntity;
import com.example.simplenotesapp.dataBase.pojo.PreviewNoteWithItemsThemes;
import java.util.List;
import androidx.lifecycle.LiveData;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

// Repository = переводчик + логика данных

public class NotesRepository {

    private final NoteDao noteDao;
    private final NoteItemDao itemDao;
    private final ExecutorService executor = Executors.newFixedThreadPool(4);

    private static NotesRepository instance; // Исправили имя

    // 1. Конструктор теперь принимает ВСЕ нужные DAO
    private NotesRepository(NoteDao noteDao, NoteItemDao itemDao) {
        this.noteDao = noteDao;
        this.itemDao = itemDao;
    }

    // 2. Метод получения экземпляра принимает оба DAO
    public static synchronized NotesRepository getInstance(NoteDao noteDao, NoteItemDao itemDao) {
        if (instance == null) {
            instance = new NotesRepository(noteDao, itemDao);
        }
        return instance;
    }

    // LiveData работает в фоновом потоке сама, executor тут не нужен
    public LiveData<List<PreviewNoteWithItemsThemes>> getNotesForUser(long userId) {
        return noteDao.getNotesForUser(userId);
    }
    public LiveData<List<PreviewNoteWithItemsThemes>> getNotesWithPreview(long userId) {

        return noteDao.getNotesWithPreview(userId);
    }

    // Все изменения данных (Write) ОБЯЗАТЕЛЬНО через executor
    public void upsertNote(NoteEntity note) {
        executor.execute(() -> noteDao.upsert(note));
    }

    public void updateNote(NoteEntity note) {
        executor.execute(() -> noteDao.update(note));
    }

    public void deleteNote(NoteEntity note) {
        executor.execute(() -> noteDao.delete(note));
    }

    public void upsertItem(NoteItemEntity item) {
        executor.execute(() -> itemDao.upsert(item));
    }

    public void deleteItem(NoteItemEntity item) {
        executor.execute(() -> itemDao.delete(item));
    }
}
