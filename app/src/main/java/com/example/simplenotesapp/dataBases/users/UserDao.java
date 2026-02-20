package com.example.simplenotesapp.dataBases.users;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Upsert;

import io.reactivex.rxjava3.core.Single;

@Dao
public interface UserDao {


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(UserEntity user);


    @Query("SELECT EXISTS(SELECT 1 FROM users WHERE email = :email)")
    boolean isUserExists(String email);


    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    UserEntity getUserByEmail(String email);

    // Single<Boolean> для авторизации (Login)
    @Query("SELECT EXISTS(SELECT 1 FROM users WHERE email = :email AND password = :password)")
    boolean checkUser(String email, String password);


    // Удаление через Query тоже оборачиваем в Completable
    @Query("DELETE FROM users WHERE email = :email")
    void deleteUser(String email);
}

