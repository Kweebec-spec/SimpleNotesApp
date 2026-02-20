package com.example.simplenotesapp.dataBases.notes;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "notes")
public class ThemeEntity {

    @PrimaryKey(autoGenerate = true)
    public String name;;
    public String color;
    public long id;

}
