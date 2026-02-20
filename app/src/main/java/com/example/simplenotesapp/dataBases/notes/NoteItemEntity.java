package com.example.simplenotesapp.dataBases.notes;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;



@Entity(
        tableName = "note_items",
        foreignKeys = @ForeignKey(
                entity = NoteEntity.class,
                parentColumns = "id",
                childColumns = "noteId",
                onDelete = ForeignKey.CASCADE
        ),
        indices = @Index("noteId")
)
public class NoteItemEntity {

    @PrimaryKey(autoGenerate = true)
    public long id;

    public long noteId;

    public int position;

    public int type; // 0 = TEXT, 1 = TASK

    public String text;

    public boolean checked;
}

