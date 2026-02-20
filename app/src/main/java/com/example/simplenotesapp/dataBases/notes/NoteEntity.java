package com.example.simplenotesapp.dataBases.notes;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import java.util.List;
@Entity(
        tableName = "notes",
        foreignKeys = @ForeignKey(
                entity = ThemeEntity.class,
                parentColumns = "id",
                childColumns = "themeId",
                onDelete = ForeignKey.SET_NULL
        ),
        indices = @Index("themeId")
)
public class NoteEntity {

    @PrimaryKey(autoGenerate = true)
    public long id;

    public String previewTitle;
    public String previewText;
    public long createdAt;
    public String color;

    public boolean hasChecklist;
}

