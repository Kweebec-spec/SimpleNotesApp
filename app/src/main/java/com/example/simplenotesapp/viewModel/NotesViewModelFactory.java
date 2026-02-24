package com.example.simplenotesapp.viewModel;

import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.example.simplenotesapp.repository.NotesRepository;
import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProvider;

public class NotesViewModelFactory implements ViewModelProvider.Factory {
    private final NotesRepository repository;
    private final long userId;

    public NotesViewModelFactory(NotesRepository repository, long userId) {
        this.repository = repository;
        this.userId = userId;
    }

    @NonNull
    @Override
    @SuppressWarnings("unchecked")
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(NotesViewModel.class)) {
            return (T) new NotesViewModel(repository, userId);
        }
        throw new IllegalArgumentException("Unknown ViewModel class");
    }
}


