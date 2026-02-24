package com.example.simplenotesapp.model;

import com.example.simplenotesapp.dataBase.pojo.PreviewNoteWithItemsThemes;
import com.example.simplenotesapp.dataBase.entity.NoteEntity;
import com.example.simplenotesapp.dataBase.entity.ThemeEntity;
import com.example.simplenotesapp.dataBase.entity.UserEntity;

import java.util.ArrayList;
import java.util.List;

public class Mapper {

    //User mapper functions
    public static User toModel(UserEntity userEntity) {

        return new User(
                userEntity.username,
                userEntity.password,
                userEntity.email,
                userEntity.id
        );
    }

    public static UserEntity toEntity(User userModel) {
        UserEntity e = new UserEntity();
        e.username = userModel.getUsername();
        e.email = userModel.getEmail();
        e.password = userModel.getPassword();
        e.id = userModel.getId(); // Устанавливаем реальный ID владельца
        // НЕ устанавливайте e.id. Оставьте его 0, чтобы Room сгенерировал новый ID при Insert,
        // ИЛИ используйте проверку, если вы обновляете существующего пользователя.
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

    // В классе Mapper.java
    public static List<Note> previewListToModel(List<PreviewNoteWithItemsThemes> relationList) {
        List<Note> notes = new ArrayList<>();
        if (relationList == null) return notes;

        for (PreviewNoteWithItemsThemes relation : relationList) {
            // Берем основную сущность заметки
            NoteEntity entity = relation.note;

            // Создаем модель.
            // Если в модели Note нужны данные из Theme или Items, передайте их сюда.
            Note noteModel = new Note(
                    entity.id,
                    entity.previewTitle,
                    entity.previewText,
                    entity.createdAt,
                    entity.color
            );
            notes.add(noteModel);
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
