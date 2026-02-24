package com.example.simplenotesapp.dataBase.pojo;

import androidx.room.Embedded;
import androidx.room.Relation;

import com.example.simplenotesapp.dataBase.entity.NoteEntity;
import com.example.simplenotesapp.dataBase.entity.UserEntity;

import java.util.List;

public class user_with_notes {
    @Embedded
    public UserEntity userEntity;

    @Relation(
            parentColumn = "id",
            entityColumn = "noteId"
    )
    public List<NoteEntity> notes;


}
