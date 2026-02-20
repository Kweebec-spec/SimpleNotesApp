package com.example.simplenotesapp.viewModel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.example.simplenotesapp.dataBases.notes.NoteEntity;
import com.example.simplenotesapp.repository.NotesRepository;

import java.util.List;

public class NotesViewModel extends ViewModel {

    private final NotesRepository repository;
    private final LiveData<List<NoteEntity>> allNotes;

    public NotesViewModel(NotesRepository repository) {
        this.repository = repository;
        this.allNotes = repository.getAllNotes();
    }

    public LiveData<List<NoteEntity>> getAllNotes() {
        return allNotes;
    }

    public void insertNote(NoteEntity note) {
        repository.insertNote(note);
    }

    public void updateNote(NoteEntity note) {
        repository.updateNote(note);
    }

    public void deleteNote(NoteEntity note) {
        repository.deleteNote(note);
    }
}
