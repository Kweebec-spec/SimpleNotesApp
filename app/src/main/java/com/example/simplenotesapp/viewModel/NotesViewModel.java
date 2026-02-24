package com.example.simplenotesapp.viewModel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.example.simplenotesapp.dataBase.entity.NoteEntity;
import com.example.simplenotesapp.dataBase.pojo.PreviewNoteWithItemsThemes;
import com.example.simplenotesapp.repository.NotesRepository;

import java.util.List;

public class NotesViewModel extends ViewModel {
    private final NotesRepository repository;
    private final LiveData<List<PreviewNoteWithItemsThemes>> notes;
    private final long userId; // Добавьте сохранение userId в поле класса

    public NotesViewModel(NotesRepository repository, long userId) {
        this.repository = repository;
        this.userId = userId; // Сохраняем ID, полученный при входе
        this.notes = repository.getNotesWithPreview(userId);
    }

    public void upsertNote(NoteEntity note) {
        // Убеждаемся, что перед отправкой в БД заметка привязана к пользователю
        note.userId = this.userId;
        repository.upsertNote(note);
    }

    public LiveData<List<PreviewNoteWithItemsThemes>> getAllNotes() {
        return notes;
    }

    public void updateNote(NoteEntity note) {
        repository.upsertNote(note);
    }

    public void deleteNote(NoteEntity note) {
        repository.deleteNote(note);
    }
}