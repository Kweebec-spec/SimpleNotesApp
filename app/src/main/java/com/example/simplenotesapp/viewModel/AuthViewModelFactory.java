package com.example.simplenotesapp.viewModel;

import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.example.simplenotesapp.repository.UserRepository;

public class AuthViewModelFactory implements ViewModelProvider.Factory {

    private final UserRepository repository;

    // ⬇️ СЮДА мы передаём все зависимости
    public AuthViewModelFactory(UserRepository repository) {
        this.repository = repository;
    }

    @Override
    public <T extends ViewModel> T create(Class<T> modelClass) {

        // ⬇️ Проверяем, какую ViewModel просят
        if (modelClass.isAssignableFrom(AuthViewModel.class)) {

            // ⬇️ Создаём её ПРАВИЛЬНО
            return (T) new AuthViewModel(repository);
        }

        throw new IllegalArgumentException("Unknown ViewModel class");
    }
}

