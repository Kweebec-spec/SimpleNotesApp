package com.example.simplenotesapp.model;

public class Note {
    private long id;
    private long themeId;
    private long userid;
    private String title;
    private String content;
    private long timestamp;
    private String color;


    public Note() {

    }


    public Note(long id, long themeId, long userid, String title, String content, long timestamp, String color) {
        this.id = id;
        this.themeId = themeId;
        this.userid = userid;
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


    public long getThemeId() {
        return themeId;
    }

    public void setThemeId(long themeId) {
        this.themeId = themeId;
    }

    public long getUserid() {
        return userid;
    }

    public void setUserid(long userid) {
        this.userid = userid;
    }
}
