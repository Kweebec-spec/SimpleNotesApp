package com.example.simplenotesapp.repository;
import android.os.Handler;
import android.os.Looper;
import com.example.simplenotesapp.dataBase.dao.UserDao;
import com.example.simplenotesapp.dataBase.entity.UserEntity;
import com.example.simplenotesapp.model.Mapper;
import com.example.simplenotesapp.model.User;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class UserRepository {

    private final UserDao userDao;
    private final ExecutorService executor;
    private static UserRepository instance; // Добавили static

    // 1. Исправленный конструктор
    private UserRepository(UserDao userDao) {
        this.userDao = userDao; // ОБЯЗАТЕЛЬНО присваиваем
        this.executor = Executors.newSingleThreadExecutor();
    }

    // 2. Добавляем метод getInstance для Singleton
    public static synchronized UserRepository getInstance(UserDao userDao) {
        if (instance == null) {
            instance = new UserRepository(userDao);
        }
        return instance;
    }

    // Проверка существования пользователя
    public void isUserExists(String email, Callback<Boolean> callback) {
        executor.execute(() -> {
            boolean exists = userDao.isUserExists(email);
            // Важно: возвращаем результат в главный поток через Handler
            new Handler(Looper.getMainLooper()).post(() -> callback.onComplete(exists));
        });
    }

    public void addUser(User user, Callback<User> callback) {
        executor.execute(() -> {
            UserEntity entity = Mapper.toEntity(user);
            long newId = userDao.upsert(entity);

            if (newId != -1) { // Room возвращает -1 при ошибке
                user.setId(newId);
                new Handler(Looper.getMainLooper()).post(() -> callback.onComplete(user));
            } else {
                new Handler(Looper.getMainLooper()).post(() -> callback.onComplete(null));
            }
        });
    }

    public void getUserByEmail(String email, Callback<User> callback) {
        executor.execute(() -> {
            UserEntity entity = userDao.getUserByEmail(email);
            User user = (entity != null) ? Mapper.toModel(entity) : null;

            new Handler(Looper.getMainLooper()).post(() -> callback.onComplete(user));
        });
    }

    public void deleteUser(String email) {
        executor.execute(() -> userDao.deleteUser(email));
    }
}
