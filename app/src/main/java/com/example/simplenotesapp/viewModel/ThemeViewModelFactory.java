package com.example.simplenotesapp.viewModel;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.example.simplenotesapp.repository.ThemeRepository;

public class ThemeViewModelFactory implements ViewModelProvider.Factory {

    private final ThemeRepository repository;
    private final long userId;

    public ThemeViewModelFactory(ThemeRepository repository, long userId) {
        this.repository = repository;
        this.userId = userId;
    }

    @NonNull
    @Override
    @SuppressWarnings("unchecked")
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(ThemeViewModel.class)) {
            return (T) new ThemeViewModel(repository, userId);
        }
        throw new IllegalArgumentException("Unknown ViewModel class");
    }
}