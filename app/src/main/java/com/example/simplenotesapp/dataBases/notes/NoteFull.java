package com.example.simplenotesapp.dataBases.notes;

import androidx.room.Embedded;
import androidx.room.Relation;

import java.util.List;

public class NoteFull {

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
}
