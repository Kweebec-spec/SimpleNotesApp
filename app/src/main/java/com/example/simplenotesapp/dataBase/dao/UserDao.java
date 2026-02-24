package com.example.simplenotesapp.dataBase.dao;

import androidx.room.Dao;
import androidx.room.Query;
import androidx.room.Upsert;

import com.example.simplenotesapp.dataBase.entity.UserEntity;

@Dao
public interface UserDao {


    @Upsert
    Long upsert(UserEntity user); // Возвращает ID новой строки


    @Query("SELECT * FROM users WHERE email = :email COLLATE NOCASE LIMIT 1")
    UserEntity getUserByEmail(String email);

    @Query("SELECT EXISTS(SELECT 1 FROM users WHERE email = :email COLLATE NOCASE)")
    boolean isUserExists(String email);


    // Single<Boolean> для авторизации (Login)
    @Query("SELECT EXISTS(SELECT 1 FROM users WHERE email = :email AND password = :password)")
    boolean checkUser(String email, String password);


    // Удаление через Query тоже оборачиваем в Completable
    @Query("DELETE FROM users WHERE email = :email")
    void deleteUser(String email);
}

