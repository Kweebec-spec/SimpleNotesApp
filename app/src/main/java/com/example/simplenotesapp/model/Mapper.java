package com.example.simplenotesapp.model;

import com.example.simplenotesapp.dataBases.notes.NoteEntity;
import com.example.simplenotesapp.dataBases.notes.ThemeEntity;
import com.example.simplenotesapp.dataBases.users.UserEntity;

import java.util.ArrayList;
import java.util.List;

public class Mapper {

    //User mapper functions
    public static User toModel(UserEntity userEntity) {

        return new User(
                userEntity.username,
                userEntity.email,
                userEntity.password
        );
    }

    public static UserEntity toEntity(User userModel) {
        UserEntity e = new UserEntity();
        e.username = userModel.getUsername();
        e.email = userModel.getEmail();
        e.password = userModel.getPassword();
        return e;
    }


    //Notes mapper functions
    public static Note toModel(NoteEntity noteEntity) {

        return new Note(
                noteEntity.id,
                noteEntity.previewTitle,
                noteEntity.previewText,
                noteEntity.createdAt,
                noteEntity.color
        );
    }

    public static List<Note> listToModel(List<NoteEntity> noteEntityList) {
        List<Note> notes = new ArrayList<>();
        for(NoteEntity entity : noteEntityList){
            notes.add(Mapper.toModel(entity));
        }
        return notes;

    }

    public static NoteEntity toEntity(Note noteModel) {
        NoteEntity e = new NoteEntity();

        e.previewTitle = noteModel.getTitle();
        e.previewText = noteModel.getContent();
        e.createdAt = noteModel.getTimeStamp();
        e.color = noteModel.getColor();
        return e;

    }

    // Theme mapper functions
    public static ThemeModel toModel(ThemeEntity themeEntity){
        return new ThemeModel(themeEntity.name, themeEntity.color, themeEntity.id);
    }

    public static ThemeEntity toEntity(ThemeModel themeModel){
        ThemeEntity e = new ThemeEntity();
        e.name = themeModel.getName();
        e.color = themeModel.getColor();
        e.id = themeModel.getId();
        return e;
    }
}
