package com.example.simplenotesapp.viewModel;

import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.example.simplenotesapp.repository.NotesRepository;
public class NotesViewModelFactory implements ViewModelProvider.Factory {

    private final NotesRepository repository;

    public NotesViewModelFactory(NotesRepository repository) {
        this.repository = repository;
    }

    @Override
    public <T extends ViewModel> T create(Class<T> modelClass) {
        return (T) new NotesViewModel(repository);
    }
}


