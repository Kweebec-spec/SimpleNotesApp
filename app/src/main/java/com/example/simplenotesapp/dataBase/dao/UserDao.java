package com.example.simplenotesapp.dataBase.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import androidx.room.Upsert;

import com.example.simplenotesapp.dataBase.entity.UserEntity;

import java.util.List;

@Dao
public interface UserDao {

    // ============ INSERT ============

    @Insert
    long insert(UserEntity user);

    @Insert
    List<Long> insertAll(List<UserEntity> users);

    // ============ UPDATE ============

    @Update
    int update(UserEntity user);

    @Update
    int updateAll(List<UserEntity> users);

    // ============ UPSERT ============

    @Upsert
    Long upsert(UserEntity user);

    @Upsert
    List<Long> upsertAll(List<UserEntity> users);

    // ============ DELETE ============

    @Delete
    int delete(UserEntity user);

    @Delete
    int deleteAll(List<UserEntity> users);

    @Query("DELETE FROM users WHERE email = :email")
    int deleteUser(String email);

    @Query("DELETE FROM users WHERE id = :userId")
    int deleteUserById(long userId);

    @Query("DELETE FROM users")
    void deleteAllUsers();

    // ============ GET BY ID/EMAIL ============

    @Query("SELECT * FROM users WHERE id = :userId LIMIT 1")
    UserEntity getUserById(long userId);

    @Query("SELECT * FROM users WHERE id = :userId")
    LiveData<UserEntity> getUserByIdLiveData(long userId);

    @Query("SELECT * FROM users WHERE email = :email COLLATE NOCASE LIMIT 1")
    UserEntity getUserByEmail(String email);

    @Query("SELECT * FROM users WHERE email = :email COLLATE NOCASE")
    LiveData<UserEntity> getUserByEmailLiveData(String email);

    // ============ GET ALL USERS ============

    @Query("SELECT * FROM users ORDER BY username ASC")
    List<UserEntity> getAllUsers();

    @Query("SELECT * FROM users ORDER BY username ASC")
    LiveData<List<UserEntity>> getAllUsersLiveData();

    // ============ CHECK EXISTS ============

    @Query("SELECT EXISTS(SELECT 1 FROM users WHERE email = :email COLLATE NOCASE)")
    boolean isUserExists(String email);

    @Query("SELECT EXISTS(SELECT 1 FROM users WHERE id = :userId)")
    boolean isUserExistsById(long userId);

    @Query("SELECT EXISTS(SELECT 1 FROM users WHERE username = :username COLLATE NOCASE)")
    boolean isUsernameExists(String username);

    // ============ AUTHENTICATION ============

    @Query("SELECT EXISTS(SELECT 1 FROM users WHERE email = :email AND password = :password)")
    boolean checkUser(String email, String password);

    @Query("SELECT * FROM users WHERE email = :email AND password = :password LIMIT 1")
    UserEntity loginUser(String email, String password);

    // ============ COUNT ============

    @Query("SELECT COUNT(*) FROM users")
    int getUsersCount();

    @Query("SELECT COUNT(*) FROM users WHERE email = :email")
    int getUserCountByEmail(String email);

    // ============ UPDATE SPECIFIC FIELDS ============

    @Query("UPDATE users SET username = :username WHERE id = :userId")
    int updateUsername(long userId, String username);

    @Query("UPDATE users SET password = :password WHERE id = :userId")
    int updatePassword(long userId, String password);

    @Query("UPDATE users SET email = :email WHERE id = :userId")
    int updateEmail(long userId, String email);




}