package com.example.simplenotesapp.viewModel;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;

import com.example.simplenotesapp.dataBase.entity.NoteEntity;
import com.example.simplenotesapp.model.Converter;
import com.example.simplenotesapp.model.Note;
import com.example.simplenotesapp.repository.NotesRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class NotesViewModel extends ViewModel {

    private static final String TAG = "NotesViewModel";
    private final NotesRepository repository;

    private final MutableLiveData<Long> currentNoteId = new MutableLiveData<>();
    private final long userId;
    private final LiveData<List<NoteEntity>> allNotes; // исходные entity
    private final MutableLiveData<Boolean> isNotesEmpty = new MutableLiveData<>(true);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    private final MutableLiveData<Long> lastCreatedNoteId = new MutableLiveData<>();
    private final MutableLiveData<Note> lastDeletedNote = new MutableLiveData<>();

    // Поля для фильтрации
    private final MutableLiveData<Long> currentThemeFilter = new MutableLiveData<>(-1L);
    private final MutableLiveData<String> currentSearchQuery = new MutableLiveData<>("");
    private final MediatorLiveData<List<NoteEntity>> filteredNotes = new MediatorLiveData<>();

    // Источники для динамического добавления/удаления
    private LiveData<List<NoteEntity>> currentSource;

    // Преобразованные списки для UI
    private final MediatorLiveData<List<Note>> uiNotes = new MediatorLiveData<>();

    //  currentNoteModel is declared without initializer (will be set in constructor)
    private LiveData<Note> currentNoteModel;

    public NotesViewModel(NotesRepository repository, long userId) {
        this.repository = repository;
        this.userId = userId;
        this.allNotes = repository.getNotesForUser(userId);

        // Now repository is already assigned, safe to build the transformation
        currentNoteModel = Transformations.switchMap(currentNoteId, id ->
                id != null && id > 0 ? Transformations.map(repository.getNoteById(id), Converter::toModel) : null);

        allNotes.observeForever(notes -> {
            isNotesEmpty.setValue(notes == null || notes.isEmpty());
        });

        // Инициализируем filteredNotes значением allNotes
        filteredNotes.setValue(allNotes.getValue());
        setupFilteredNotes();

        // Преобразуем filteredNotes (список entity) в uiNotes (список моделей)
        uiNotes.addSource(filteredNotes, entities -> {
            List<Note> models = new ArrayList<>();
            if (entities != null) {
                for (NoteEntity entity : entities) {
                    models.add(Converter.toModel(entity));
                }
            }
            uiNotes.setValue(models);
        });
    }

    private void setupFilteredNotes() {
        filteredNotes.addSource(currentSearchQuery, query -> updateFilteredNotes());
        filteredNotes.addSource(currentThemeFilter, themeId -> updateFilteredNotes());
        filteredNotes.addSource(allNotes, notes -> updateFilteredNotes());
    }

    private void updateFilteredNotes() {
        String query = currentSearchQuery.getValue();
        Long themeId = currentThemeFilter.getValue();

        LiveData<List<NoteEntity>> newSource = null;

        if (themeId != null && themeId != -1) {
            newSource = repository.getNotesByThemeAndQuery(userId, themeId, query != null ? query : "");
        } else if (query != null && !query.isEmpty()) {
            newSource = repository.searchNotes(userId, query);
        } else {
            // Если нет фильтров, используем allNotes
            filteredNotes.setValue(allNotes.getValue());
            if (currentSource != null) {
                filteredNotes.removeSource(currentSource);
                currentSource = null;
            }
            return;
        }

        if (currentSource != newSource) {
            if (currentSource != null) {
                filteredNotes.removeSource(currentSource);
            }
            currentSource = newSource;
            if (newSource != null) {
                filteredNotes.addSource(newSource, notes -> filteredNotes.setValue(notes));
            }
        }
    }

    // Публичные методы для UI (работают с моделями)
    public void filterByTheme(long themeId) {
        currentThemeFilter.setValue(themeId);
    }

    public void searchNotes(String query) {
        currentSearchQuery.setValue(query);
    }

    public LiveData<List<Note>> getFilteredNotes() {
        return uiNotes;
    }

    public LiveData<Note> getCurrentNote() {
        return currentNoteModel;
    }

    public void setCurrentNoteId(long id) {
        if (id > 0) {
            currentNoteId.setValue(id);
        }
    }

    public Long getCurrentNoteId() {
        return currentNoteId.getValue();
    }

    public void clearCurrentNote() {
        currentNoteId.setValue(null);
    }

    // Методы для работы с заметками (принимают модели)
    public void createNote(Note note, OnNoteCreatedCallback callback) {
        if (userId <= 0) {
            Log.e(TAG, "Cannot create note: invalid userId = " + userId);
            if (callback != null) {
                mainHandler.post(() -> callback.onError(new Exception("User not authenticated")));
            }
            return;
        }
        NoteEntity entity = Converter.toEntity(note);
        entity.setUserId(userId);
        entity.setCreatedAt(System.currentTimeMillis());
        entity.setThemeId(note.getThemeId() > 0 ? note.getThemeId() : null);
        entity.setColor(note.getColor());

        executor.execute(() -> {
            try {
                long noteId = repository.insertNoteAndGetId(entity);
                Log.d(TAG, "Note created with ID: " + noteId);
                mainHandler.post(() -> {
                    lastCreatedNoteId.setValue(noteId);
                    if (callback != null) callback.onSuccess(noteId);
                });
            } catch (Exception e) {
                Log.e(TAG, "Error creating note", e);
                mainHandler.post(() -> {
                    if (callback != null) callback.onError(e);
                    errorMessage.setValue("Error creating note: " + e.getMessage());
                });
            }
        });
    }

    public void updateNote(Note note) {
        if (note == null) return;
        NoteEntity entity = Converter.toEntity(note);
        entity.setUserId(userId);
        // themeId: если <=0, то null (удалить тему)
        entity.setThemeId(note.getThemeId() > 0 ? note.getThemeId() : null);
        entity.setColor(note.getColor());
        // Если заметка новая (id=0), установим время создания
        if (entity.getId() <= 0) {
            entity.setCreatedAt(System.currentTimeMillis());
        }
        executor.execute(() -> {
            try {
                repository.updateNote(entity);
                mainHandler.post(() -> errorMessage.setValue(null));
            } catch (Exception e) {
                Log.e(TAG, "Error updating note", e);
                mainHandler.post(() -> errorMessage.setValue("Error updating note: " + e.getMessage()));
            }
        });
    }

    public void deleteNote(Note note) {
        if (note == null || note.getId() <= 0) return;
        NoteEntity entity = Converter.toEntity(note);
        executor.execute(() -> {
            try {
                repository.deleteNote(entity);
                mainHandler.post(() -> {
                    lastDeletedNote.setValue(note);
                    errorMessage.setValue(null);
                    if (currentNoteId.getValue() != null && currentNoteId.getValue().equals(note.getId())) {
                        currentNoteId.setValue(null);
                    }
                });
            } catch (Exception e) {
                Log.e(TAG, "Error deleting note", e);
                mainHandler.post(() -> errorMessage.setValue("Error deleting note: " + e.getMessage()));
            }
        });
    }

    public void deleteAllNotes() {
        executor.execute(() -> {
            try {
                repository.deleteAllNotesForUser(userId);
                mainHandler.post(() -> {
                    errorMessage.setValue(null);
                    currentNoteId.setValue(null);
                });
            } catch (Exception e) {
                Log.e(TAG, "Error deleting all notes", e);
                mainHandler.post(() -> errorMessage.setValue("Error deleting all notes: " + e.getMessage()));
            }
        });
    }

    public void resetThemeForNotes(long themeId) {
        executor.execute(() -> {
            try {
                List<NoteEntity> notesWithTheme = repository.getNotesByThemeIdSync(themeId);
                for (NoteEntity note : notesWithTheme) {
                    note.setThemeId(null);
                    note.setColor(null);
                    repository.updateNote(note);
                }
                Log.d(TAG, "Reset theme for " + notesWithTheme.size() + " notes");
                mainHandler.post(this::refreshNotes);
            } catch (Exception e) {
                Log.e(TAG, "Error resetting theme for notes", e);
                mainHandler.post(() -> errorMessage.setValue("Error resetting theme: " + e.getMessage()));
            }
        });
    }

    // Вспомогательные методы
    public LiveData<Integer> getNotesCount() {
        return repository.getNotesCount(userId);
    }

    public void refreshNotes() {
        repository.refreshNotes(userId);
    }

    public void clearError() {
        errorMessage.setValue(null);
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public LiveData<Boolean> isNotesEmpty() {
        return isNotesEmpty;
    }

    public LiveData<Note> getLastDeletedNote() {
        return lastDeletedNote;
    }

    public interface OnNoteCreatedCallback {
        void onSuccess(long noteId);
        void onError(Exception e);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        executor.shutdown();
    }
}