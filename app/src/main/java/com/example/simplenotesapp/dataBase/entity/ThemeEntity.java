package com.example.simplenotesapp.dataBase.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "themes")
public class ThemeEntity {

    @PrimaryKey(autoGenerate = true)
    public long id;
    public String name;
    public String color;


}
