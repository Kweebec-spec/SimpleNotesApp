package com.example.simplenotesapp.viewModel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.example.simplenotesapp.dataBase.entity.ThemeEntity;
import com.example.simplenotesapp.repository.ThemeRepository;

import java.util.List;

public class ThemeViewModel extends ViewModel {

    private final ThemeRepository repository;
    private final LiveData<List<ThemeEntity>> themes;

    public ThemeViewModel(ThemeRepository repository) {
        this.repository = repository;
        themes = repository.getThemes();
    }

    public LiveData<List<ThemeEntity>> getThemes() {
        return themes;
    }

    public void upsert(ThemeEntity theme) {
        repository.upsert(theme);
    }
}
