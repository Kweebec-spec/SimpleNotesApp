package com.example.simplenotesapp.model;

public class Note {
    private long id;
    private String title;
    private String content;
    private long timestamp;
    private String color;

    public Note(long id, String previewTitle, String previewText, long createdAt, String color) { }

    public Note(long id, String title, String content, long timestamp) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.timestamp = timestamp;
        this.color = color;
    }

    // Getters and setters
    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public long getTimeStamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }


}
