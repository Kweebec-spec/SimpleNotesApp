package com.example.simplenotesapp.viewModel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.ViewModel;

import com.example.simplenotesapp.dataBase.entity.ThemeEntity;
import com.example.simplenotesapp.model.Converter;
import com.example.simplenotesapp.model.ThemeModel;
import com.example.simplenotesapp.repository.ThemeRepository;

import java.util.ArrayList;
import java.util.List;

public class ThemeViewModel extends ViewModel {

    private final ThemeRepository repository;
    private final long userId;
    private final LiveData<List<ThemeEntity>> themesEntity;
    private final MediatorLiveData<List<ThemeModel>> themesModel = new MediatorLiveData<>();

    public ThemeViewModel(ThemeRepository repository, long userId) {
        this.repository = repository;
        this.userId = userId;
        this.themesEntity = repository.getThemesForUser(userId);

        // Преобразуем entity в модели
        themesModel.addSource(themesEntity, entities -> {
            List<ThemeModel> models = new ArrayList<>();
            if (entities != null) {
                for (ThemeEntity entity : entities) {
                    models.add(Converter.toModel(entity));
                }
            }
            themesModel.setValue(models);
        });
    }

    public LiveData<List<ThemeModel>> getThemes() {
        return themesModel;
    }

    public void upsert(ThemeModel theme) {
        ThemeEntity entity = Converter.toEntity(theme);
        entity.setUserId(userId);
        repository.upsert(entity);
    }

    public void delete(ThemeModel theme) {
        ThemeEntity entity = Converter.toEntity(theme);
        entity.setUserId(userId);
        repository.delete(entity);
    }

    public boolean isThemeNameExists(String name) {
        return repository.isThemeNameExists(userId, name);
    }
}