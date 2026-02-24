package com.example.simplenotesapp.dataBase.entity;

import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "users",
        indices = {@Index(value = {"email"}, unique = true)} // Email должен быть уникальным!
)
public class UserEntity {
    @PrimaryKey(autoGenerate = true)
    public Long id;

    public String username;
    public String password;
    public String email;
}
