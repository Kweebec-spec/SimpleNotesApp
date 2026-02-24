package com.example.simplenotesapp.dataBase.pojo;

import androidx.room.Embedded;
import androidx.room.Ignore;
import androidx.room.Relation;

import com.example.simplenotesapp.dataBase.entity.NoteEntity;
import com.example.simplenotesapp.dataBase.entity.NoteItemEntity;
import com.example.simplenotesapp.dataBase.entity.ThemeEntity;

import java.util.List;

public class PreviewNoteWithItemsThemes {

    @Embedded
    public NoteEntity note;

    @Relation(
            parentColumn = "id",
            entityColumn = "noteId"
    )
    public List<NoteItemEntity> items;

    @Relation(
            parentColumn = "themeId",
            entityColumn = "id"
    )
    public ThemeEntity theme;

    // Room не будет создавать колонку, но сможет записать сюда результат запроса
    @Ignore public String previewText;
    @Ignore public String displayTitle;
    @Ignore public String displayColor;
}
