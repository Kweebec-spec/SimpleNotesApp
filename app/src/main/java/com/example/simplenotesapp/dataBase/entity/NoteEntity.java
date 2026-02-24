package com.example.simplenotesapp.dataBase.entity;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

/**
 * @Entity: Defines this class as a table in SQLite.
 * foreignKeys: Defines constraints between tables to ensure data integrity.
 * indices: Optimizes database performance for specific columns.
 */
@Entity(
        tableName = "notes",
        foreignKeys = {
                // 1. Connection to ThemeEntity
                @ForeignKey(
                        entity = ThemeEntity.class,
                        parentColumns = "id",        // The ID in Theme table
                        childColumns = "themeId",     // The ID in this Note table
                        onDelete = ForeignKey.SET_NULL // If theme is deleted, keep the note but set themeId to null
                ),
                // 2. NEW Connection to UserEntity
                @ForeignKey(
                        entity = UserEntity.class,    // The parent class
                        parentColumns = "id",         // The PrimaryKey of User
                        childColumns = "userId",      // The Foreign Key column below
                        onDelete = ForeignKey.CASCADE  // If user is deleted, delete all their notes automatically
                )
        },
        // @Index: Creates a lookup table for these columns.
        // Without this, Room has to scan every row to find notes for a specific user, which is slow.
        indices = {
                @Index("themeId"),
                @Index("userId")
        }
)

public class NoteEntity {

    @PrimaryKey(autoGenerate = true)
    public Long id;

    // Foreign Key columns
    public Long themeId;  // Links to ThemeEntity.id
    public long userId;   // Links to UserEntity.id (NEW)

    public String previewTitle;
    public String previewText;
    public long createdAt;
    public String color;
    public boolean hasChecklist;

    // Standard constructor, getters, and setters follow...
}