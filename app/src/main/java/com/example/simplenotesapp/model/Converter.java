package com.example.simplenotesapp.model;

import com.example.simplenotesapp.dataBase.entity.NoteEntity;
import com.example.simplenotesapp.dataBase.entity.ThemeEntity;
import com.example.simplenotesapp.dataBase.entity.UserEntity;

public class Converter {

    //User mapper functions
    public static User toModel(UserEntity userEntity) {

        return new User(
                userEntity.getUsername(),
                userEntity.getPassword(),
                userEntity.getEmail(),
                userEntity.getId()
        );
    }

    public static UserEntity toEntity(User userModel) {
        UserEntity e = new UserEntity();
        e.setUsername(userModel.getUsername());
        e.setEmail(userModel.getEmail());
        e.setPassword(userModel.getPassword());
        e.setId(userModel.getId()); // Устанавливаем реальный ID владельца
        // НЕ устанавливайте e.id. Оставьте его 0, чтобы Room сгенерировал новый ID при Insert,
        // ИЛИ используйте проверку, если вы обновляете существующего пользователя.
        return e;
    }


    public static Note toModel(NoteEntity noteEntity) {
        long themeId = noteEntity.getThemeId() != null ? noteEntity.getThemeId() : -1L;
        return new Note(
                noteEntity.getId(),
                themeId,
                noteEntity.getUserId(),
                noteEntity.getTitle(),
                noteEntity.getText(),
                noteEntity.getCreatedAt(),
                noteEntity.getColor()
        );
    }



    public static NoteEntity toEntity(Note noteModel) {
        NoteEntity e = new NoteEntity();
        e.setId(noteModel.getId());
        e.setThemeId(noteModel.getThemeId() > 0 ? noteModel.getThemeId() : null);
        e.setUserId(noteModel.getUserid());
        e.setTitle(noteModel.getTitle());
        e.setText(noteModel.getContent());
        e.setCreatedAt(noteModel.getTimeStamp());
        e.setColor(noteModel.getColor());
        return e;
    }


    // Theme mapper functions
    public static ThemeModel toModel(ThemeEntity themeEntity){
        return new ThemeModel(themeEntity.getName(),
                themeEntity.getColor(),
                themeEntity.getId());
    }

    public static ThemeEntity toEntity(ThemeModel themeModel){
        ThemeEntity e = new ThemeEntity();
        e.setName(themeModel.getName());
        e.setColor(themeModel.getColor());
        e.setId(themeModel.getId());
        return e;
    }
}
