package com.example.simplenotesapp.repository;

import android.util.Log;

import com.example.simplenotesapp.dataBase.dao.NoteDao;
import com.example.simplenotesapp.dataBase.entity.NoteEntity;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import androidx.lifecycle.LiveData;

public class NotesRepository {

    private static final String TAG = "NotesRepository";
    private final NoteDao noteDao;
    private final ExecutorService executor = Executors.newFixedThreadPool(4);

    private static NotesRepository instance;

    private NotesRepository(NoteDao noteDao) {
        this.noteDao = noteDao;
    }

    public static synchronized NotesRepository getInstance(NoteDao noteDao) {
        if (instance == null) {
            instance = new NotesRepository(noteDao);
        }
        return instance;
    }


    // ============ LIVE DATA METHODS ============
    public LiveData<List<NoteEntity>> getNotesByThemeAndQuery(long userId, long themeId, String query) {
        return noteDao.getNotesByThemeAndQuery(userId, themeId, query);
    }
    public List<NoteEntity> getNotesByThemeIdSync(long themeId) {
        return noteDao.getNotesByThemeIdSync(themeId);
    }



    public LiveData<List<NoteEntity>> getNotesForUser(long userId) {
        return noteDao.getNotesForUser(userId);
    }

    public LiveData<NoteEntity> getNoteById(long noteId) {
        return noteDao.getNoteById(noteId);
    }

    public LiveData<List<NoteEntity>> searchNotes(long userId, String query) {
        return noteDao.searchNotes(userId, query);
    }

    public LiveData<NoteEntity> getLastCreatedNote() {
        return noteDao.getLastCreatedNoteLiveData();
    }

    public LiveData<NoteEntity> getLastCreatedNoteForUser(long userId) {
        return noteDao.getLastCreatedNoteForUserLiveData(userId);
    }

    public LiveData<Integer> getNotesCount(long userId) {
        return noteDao.getNotesCount(userId);
    }

    // ============ SYNC METHODS ============

    public NoteEntity getNoteByIdSync(long noteId) {
        try {
            return noteDao.getNoteByIdSync(noteId);
        } catch (Exception e) {
            Log.e(TAG, "Error getting note by id sync", e);
            return null;
        }
    }

    // ============ INSERT METHODS ============

    public long insertNoteAndGetId(NoteEntity note) {
        return noteDao.insert(note);
    }

    public void insertNotes(List<NoteEntity> notes) {
        executor.execute(() -> {
            try {
                noteDao.insertAll(notes);
            } catch (Exception e) {
                Log.e(TAG, "Error inserting notes", e);
            }
        });
    }

    // ============ UPDATE METHODS ============

    public void updateNote(NoteEntity note) {
        executor.execute(() -> {
            try {
                noteDao.update(note);
            } catch (Exception e) {
                Log.e(TAG, "Error updating note", e);
            }
        });
    }

    // ============ UPSERT METHODS ============

    public void upsertNote(NoteEntity note) {
        executor.execute(() -> {
            try {
                noteDao.upsert(note);
            } catch (Exception e) {
                Log.e(TAG, "Error upserting note", e);
            }
        });
    }

    // ============ DELETE METHODS ============

    public void deleteNote(NoteEntity note) {
        executor.execute(() -> {
            try {
                noteDao.delete(note);
            } catch (Exception e) {
                Log.e(TAG, "Error deleting note", e);
            }
        });
    }

    public void deleteAllNotesForUser(long userId) {
        executor.execute(() -> {
            try {
                noteDao.deleteAllNotesForUser(userId);
            } catch (Exception e) {
                Log.e(TAG, "Error deleting all notes for user", e);
            }
        });
    }

    // ============ REFRESH METHOD ============

    /**
     * Обновляет LiveData путем обновления поля updatedAt
     * Вызывайте этот метод когда нужно принудительно обновить список заметок
     */
    public void refreshNotes(long userId) {
        executor.execute(() -> {
            try {
                long currentTime = System.currentTimeMillis();
                noteDao.touchNotesForUser(userId, currentTime);
                Log.d(TAG, "Notes refreshed for user: " + userId);
            } catch (Exception e) {
                Log.e(TAG, "Error refreshing notes", e);
            }
        });
    }



    // ============ CLEANUP ============

    public void shutdown() {
        if (executor != null && !executor.isShutdown()) {
            executor.shutdown();
            try {
                if (!executor.awaitTermination(3, TimeUnit.SECONDS)) {
                    executor.shutdownNow();
                }
            } catch (InterruptedException e) {
                executor.shutdownNow();
            }
        }
    }
}