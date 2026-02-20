package com.example.simplenotesapp.repository;


import com.example.simplenotesapp.dataBases.notes.NoteDao;
import com.example.simplenotesapp.dataBases.notes.NoteEntity;
import com.example.simplenotesapp.dataBases.notes.NoteItemDao;
import com.example.simplenotesapp.dataBases.notes.NoteItemEntity;
import com.example.simplenotesapp.dataBases.notes.NotesDatabase;

import java.util.List;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;

// Repository = переводчик + логика данных



import androidx.lifecycle.LiveData;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class NotesRepository {

    private final NoteDao noteDao;
    private final NoteItemDao itemDao;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public NotesRepository(NotesDatabase db) {
        noteDao = db.noteDao();
        itemDao = db.noteItemDao();
    }

    // LiveData Room сам обновляет
    public LiveData<List<NoteEntity>> getAllNotes() {
        return noteDao.getAll();
    }

    public LiveData<List<NoteItemEntity>> getItems(long noteId) {
        return itemDao.getItems(noteId);
    }

    // CRUD — вручную в фоне
    public void insertNote(NoteEntity note) {
        executor.execute(() -> noteDao.upsert(note));
    }

    public void updateNote(NoteEntity note) {
        executor.execute(() -> noteDao.update(note));
    }

    public void deleteNote(NoteEntity note) {
        executor.execute(() -> noteDao.delete(note));
    }

    public void insertItem(NoteItemEntity item) {
        executor.execute(() -> itemDao.upsert(item));
    }

    public void updateItem(NoteItemEntity item) {
        executor.execute(() -> itemDao.update(item));
    }

    public void deleteItem(NoteItemEntity item) {
        executor.execute(() -> itemDao.delete(item));
    }
}
