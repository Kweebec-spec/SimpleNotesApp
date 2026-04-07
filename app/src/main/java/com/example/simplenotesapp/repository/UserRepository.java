package com.example.simplenotesapp.repository;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.example.simplenotesapp.dataBase.dao.UserDao;
import com.example.simplenotesapp.dataBase.entity.UserEntity;
import com.example.simplenotesapp.model.Converter;
import com.example.simplenotesapp.model.User;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class UserRepository {

    private static final String TAG = "UserRepository";
    private final UserDao userDao;
    private final ExecutorService executor;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    private static UserRepository instance;

    private UserRepository(UserDao userDao) {
        this.userDao = userDao;
        this.executor = Executors.newSingleThreadExecutor();
    }

    public static synchronized UserRepository getInstance(UserDao userDao) {
        if (instance == null) {
            instance = new UserRepository(userDao);
        }
        return instance;
    }

    // ============ Callback интерфейсы ============

    public interface Callback<T> {
        void onComplete(T result);
    }

    public interface UserCallback {
        void onSuccess(User user);
        void onError(String error);
    }

    // ============ Основные методы ============

    // Проверка существования пользователя
    public void isUserExists(String email, Callback<Boolean> callback) {
        executor.execute(() -> {
            try {
                boolean exists = userDao.isUserExists(email);
                mainHandler.post(() -> callback.onComplete(exists));
            } catch (Exception e) {
                Log.e(TAG, "Error checking user existence", e);
                mainHandler.post(() -> callback.onComplete(false));
            }
        });
    }

    // Добавление нового пользователя
    public void addUser(User user, Callback<User> callback) {
        executor.execute(() -> {
            try {
                UserEntity entity = Converter.toEntity(user);
                long newId = userDao.upsert(entity);

                if (newId != -1) {
                    user.setId(newId);
                    mainHandler.post(() -> callback.onComplete(user));
                } else {
                    mainHandler.post(() -> callback.onComplete(null));
                }
            } catch (Exception e) {
                Log.e(TAG, "Error adding user", e);
                mainHandler.post(() -> callback.onComplete(null));
            }
        });
    }

    // Получение пользователя по email (основной метод)
    public void getUserByEmail(String email, Callback<User> callback) {
        executor.execute(() -> {
            try {
                UserEntity entity = userDao.getUserByEmail(email);
                User user = (entity != null) ? Converter.toModel(entity) : null;
                mainHandler.post(() -> callback.onComplete(user));
            } catch (Exception e) {
                Log.e(TAG, "Error getting user by email", e);
                mainHandler.post(() -> callback.onComplete(null));
            }
        });
    }

    // Второй метод для получения пользователя (альтернативный)
    public void getUserByEmail(String email, UserCallback callback) {
        executor.execute(() -> {
            try {
                UserEntity entity = userDao.getUserByEmail(email);
                if (entity != null) {
                    User user = Converter.toModel(entity);
                    mainHandler.post(() -> callback.onSuccess(user));
                } else {
                    mainHandler.post(() -> callback.onError("User not found"));
                }
            } catch (Exception e) {
                Log.e(TAG, "Error getting user by email", e);
                mainHandler.post(() -> callback.onError(e.getMessage()));
            }
        });
    }

    // Удаление пользователя
    public void deleteUser(String email) {
        executor.execute(() -> {
            try {
                userDao.deleteUser(email);
                Log.d(TAG, "User deleted: " + email);
            } catch (Exception e) {
                Log.e(TAG, "Error deleting user", e);
            }
        });
    }

    // Проверка логина
    public void checkUserCredentials(String email, String password, Callback<Boolean> callback) {
        executor.execute(() -> {
            try {
                boolean isValid = userDao.checkUser(email, password);
                mainHandler.post(() -> callback.onComplete(isValid));
            } catch (Exception e) {
                Log.e(TAG, "Error checking credentials", e);
                mainHandler.post(() -> callback.onComplete(false));
            }
        });
    }

    // Получение пользователя по ID
    public void getUserById(long userId, Callback<User> callback) {
        executor.execute(() -> {
            try {
                UserEntity entity = userDao.getUserById(userId);
                User user = (entity != null) ? Converter.toModel(entity) : null;
                mainHandler.post(() -> callback.onComplete(user));
            } catch (Exception e) {
                Log.e(TAG, "Error getting user by id", e);
                mainHandler.post(() -> callback.onComplete(null));
            }
        });
    }
}