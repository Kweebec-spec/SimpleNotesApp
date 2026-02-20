package com.example.simplenotesapp.repository;

import android.content.Context;

import com.example.simplenotesapp.dataBases.users.UserDao;
import com.example.simplenotesapp.dataBases.users.UserDatabase;
import com.example.simplenotesapp.dataBases.users.UserEntity;
import com.example.simplenotesapp.model.Mapper;
import com.example.simplenotesapp.model.User;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class UserRepository {

    private final UserDao userDao;
    private final ExecutorService executor;

    public UserRepository(Context context) {
        userDao = UserDatabase.getInstance(context).userDao();
        executor = Executors.newSingleThreadExecutor();
    }

    // Проверка существования пользователя
    public void isUserExists(String email, Callback<Boolean> callback) {
        executor.execute(() -> {
            boolean exists = userDao.isUserExists(email);
            callback.onComplete(exists);
        });
    }

    // Добавление пользователя
    public void addUser(User user) {
        executor.execute(() -> {
            UserEntity entity = Mapper.toEntity(user);
            userDao.insert(entity);
        });
    }

    // Получение пользователя
    public void getUserByEmail(String email, Callback<User> callback) {
        executor.execute(() -> {
            UserEntity entity = userDao.getUserByEmail(email);

            if (entity != null) {
                callback.onComplete(Mapper.toModel(entity));
            } else {
                callback.onComplete(null);
            }
        });
    }

    // Удаление
    public void deleteUser(String email) {
        executor.execute(() -> {
            userDao.deleteUser(email);
        });
    }
}
