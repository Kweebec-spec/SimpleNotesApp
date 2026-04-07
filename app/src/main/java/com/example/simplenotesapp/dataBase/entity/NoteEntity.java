package com.example.simplenotesapp.dataBase.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "notes",
        foreignKeys = {
                @ForeignKey(
                        entity = UserEntity.class,
                        parentColumns = "id",
                        childColumns = "userId",
                        onDelete = ForeignKey.CASCADE
                ),
                @ForeignKey(
                        entity = ThemeEntity.class,
                        parentColumns = "id",
                        childColumns = "themeId",
                        onDelete = ForeignKey.SET_NULL
                )
        },
        indices = {
                @Index(value = {"userId"}, name = "index_notes_user_id"),   // index for foreign key
                @Index(value = {"themeId"}, name = "index_notes_theme_id")
        }
)
public class NoteEntity {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "note_id")
    private long id;

    @ColumnInfo(name = "themeId")
    private Long themeId;

    @ColumnInfo(name = "userId")
    private long userId;

    @ColumnInfo(name = "title")
    private String title;

    @ColumnInfo(name = "text")
    private String text;

    @ColumnInfo(name = "createdAt")
    private long createdAt;

    @ColumnInfo(name = "color")
    private String color;


    // Getters and Setters
    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public Long getThemeId() { return themeId; }
    public void setThemeId(Long themeId) { this.themeId = themeId; }

    public long getUserId() { return userId; }
    public void setUserId(long userId) { this.userId = userId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }

    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }
}